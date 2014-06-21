package gov.epa.nrmrl.std.lca.ht.tdb;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import harmonizationtool.dialog.GenericMessageBox;
import harmonizationtool.model.CuratorMD;
import harmonizationtool.model.DataSourceKeeper;
import harmonizationtool.model.ContactMD;
import harmonizationtool.model.DataSourceProvider;
import harmonizationtool.model.FileMD;
import harmonizationtool.query.QGetAllProperties;
import harmonizationtool.utils.Util;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FEDLCA;
import harmonizationtool.vocabulary.LCAHT;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.IServiceLocator;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ActiveTDB implements IHandler, IActiveTDB {
	public static Model model = null;
	public static Dataset TDBDataset = null;
	public static String tdbDir = null;
	public static GraphStore graphStore = null;
	private static ActiveTDB instance = null;

	// private List<IActiveTDBListener> activeTDBListeners = new
	// ArrayList<IActiveTDBListener>();

	public ActiveTDB() {
		System.out.println("created ActiveTDB");
		setInstance(this);
	}

	// public static ActiveTDB getInstance() {
	// return instance;
	// }

	public void finalize() {
		System.out.println("closing TDBDataset and model");
		cleanUp();
	}

	public static void replaceLiteral(Resource subject, Property predicate, String stringLiteral) {
		NodeIterator nodeIterator = model.listObjectsOfProperty(subject, predicate);
		while (nodeIterator.hasNext()) {
			RDFNode rdfNode = nodeIterator.next();
			if (rdfNode.isLiteral()) {
				model.removeAll(subject, predicate, rdfNode);
			}
		}
		System.out.println("Filename: " + stringLiteral + " added to TDB");
		model.add(subject, predicate, model.createTypedLiteral(stringLiteral));
	}

	public static void replaceLiteral(Resource subject, Property predicate, Long longLiteral) {
		NodeIterator nodeIterator = model.listObjectsOfProperty(subject, predicate);
		while (nodeIterator.hasNext()) {
			RDFNode rdfNode = nodeIterator.next();
			if (rdfNode.isLiteral()) {
				model.removeAll(subject, predicate, rdfNode);
			}
		}
		// NOTE A JAVA long BECOMES AN xsd:integer
		model.add(subject, predicate, model.createTypedLiteral(longLiteral));
	}

	public static void replaceLiteral(Resource subject, Property predicate, Date dateObject) {

		NodeIterator nodeIterator;
		try {
			nodeIterator = model.listObjectsOfProperty(subject, predicate);

			while (nodeIterator.hasNext()) {
				RDFNode rdfNode = nodeIterator.next();
				if (rdfNode.isLiteral()) {
					model.removeAll(subject, predicate, rdfNode);
				}
			}
			Literal dateLiteral = model.createTypedLiteral(dateObject);
			model.add(subject, predicate, model.createTypedLiteral(dateLiteral));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("subject: " + subject);
			System.out.println("predicate: " + predicate);

			e.printStackTrace();
		}
	}

	private void cleanUp() {
		try {
			TDBDataset.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			model.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			graphStore.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		System.out.println("added listener: " + handlerListener);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		openTDB();
		updateStatusLine();
		try {
			syncTDBToDataSourceKeeper();
		} catch (Exception e) {
			Exception e2 = new ExecutionException("***********THE TDB MAY BE BAD*******************");
			e2.printStackTrace();
			System.exit(1);
		}

		return null;
	}

	private void updateStatusLine() {

	}

	private static void openTDB() {
		while (model == null) {
			if ((Util.getPreferenceStore().getString("defaultTDB") == null) || (Util.getPreferenceStore().getString("defaultTDB") == "")) {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				StringBuilder b = new StringBuilder();
				b.append("The Harmonization Tool (HT) requires the user to specify a directory for the Triplestore DataBase (TDB). ");
				b.append("Please pick an existing TDB directory, or an empty one where the HT can create a new TDB.");
				new GenericMessageBox(shell, "Welcome!", b.toString());

				redirectToPreferences();
			}

			String defaultTDB = Util.getPreferenceStore().getString("defaultTDB");
			File defaultTDBFile = new File(defaultTDB);
			if (!defaultTDBFile.isDirectory()) {
				// ask user for TDB directory
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				StringBuilder b = new StringBuilder();
				b.append("Sorry, but the Default TDB set in your preferences is not an accessible directory. ");
				b.append("Please select an existing TDB directory, or an accessible directory for a new TDB.");
				new GenericMessageBox(shell, "Alert", b.toString());

				redirectToPreferences();
			} else {
				System.out.println("defaultTDBFile.list().length=" + defaultTDBFile.list().length);
				try {
					TDBDataset = TDBFactory.createDataset(defaultTDBFile.getPath());
					assert TDBDataset != null : "TDBDataset cannot be null";
					model = TDBDataset.getDefaultModel();
					graphStore = GraphStoreFactory.create(TDBDataset);

				} catch (Exception e1) {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					StringBuilder b = new StringBuilder();
					b.append("It appears that the HT can not create a TDB in the default directory. ");
					b.append("You may need to create a new TDB.");
					new GenericMessageBox(shell, "Error", b.toString());

					redirectToPreferences();
				}
			}
		}
	}

	private static void redirectToPreferences() {
		try {
			IServiceLocator serviceLocator = PlatformUI.getWorkbench();
			ICommandService commandService = (ICommandService) serviceLocator.getService(ICommandService.class);
			Command command = commandService.getCommand("org.eclipse.ui.window.preferences");
			command.executeWithChecks(new ExecutionEvent());
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (NotDefinedException e) {
			e.printStackTrace();
		} catch (NotEnabledException e) {
			e.printStackTrace();
		} catch (NotHandledException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Because the DataSourceKeeper does not contain DataSources, but the TDB might, we need to get
	 * DataSources info from the TDB. Each TDB subject which is rdf:type eco:DataSource should have
	 * a DataSourceProvider
	 */
	public static void syncTDBToDataSourceKeeper() {
		if (model == null) {
			openTDB();
		}
		assert model != null : "model should not be null";
		ResIterator iterator = model.listSubjectsWithProperty(RDF.type, ECO.DataSource);
		while (iterator.hasNext()) {
			Resource dataSourceRDFResource = iterator.next();

			int dataSourceIndex = DataSourceKeeper.getByTdbResource(dataSourceRDFResource);
			if (dataSourceIndex < 0) {
				DataSourceProvider dataSourceProvider = new DataSourceProvider();
				dataSourceProvider.setTdbResource(dataSourceRDFResource);
				ContactMD dataSourceMD = new ContactMD();
//				CuratorMD curatorMD = new CuratorMD();

				NodeIterator nodeIterator = model.listObjectsOfProperty(dataSourceRDFResource, LCAHT.containsFile);
				while (nodeIterator.hasNext()) {
					RDFNode fileMDResource = nodeIterator.next();
					if (!fileMDResource.isLiteral()) {
						FileMD fileMD = new FileMD(fileMDResource.asResource());
						fileMD.syncDataFromTDB();
						dataSourceProvider.addFileMD(fileMD);
					}
				}

				// RDFS.label <=> Name
				List<Statement> labelStatements = dataSourceRDFResource.listProperties(RDFS.label).toList();
				if (labelStatements.size() > 1) {
					Logger.getLogger("run").warn("# !Data set has " + dataSourceRDFResource.listProperties(RDFS.label).toList().size() + " labels:");
					for (int i = 0; i < labelStatements.size() - 2; i++) {
						Statement statement = labelStatements.get(i);
						Logger.getLogger("run").warn("#    - Removing label:   " + statement.getObject().toString());
						statement.remove();
					}
					String label = labelStatements.get(labelStatements.size() - 1).getObject().toString();
					Logger.getLogger("run").warn("#    - Keeping last label: " + label);
				}
				String dataSourceName = labelStatements.get(labelStatements.size() - 1).getObject().toString();

				dataSourceName = DataSourceKeeper.uniquify(dataSourceName);
				dataSourceRDFResource.getProperty(RDFS.label).changeObject(dataSourceName);
				dataSourceProvider.setDataSourceName(dataSourceName);

				// RDFS.comment <=> Comments
				if (dataSourceRDFResource.hasProperty(RDFS.comment)) {
					// FIXME: SHOULD A DataSource BE ALLOWED TO HAVE MORE THAN ONE COMMENT?
					dataSourceProvider.setComments(dataSourceRDFResource.getProperty(RDFS.comment).getObject().toString());
				}

				// DCTerms.hasVersion <=> Version
				if (dataSourceRDFResource.hasProperty(DCTerms.hasVersion)) {
					dataSourceProvider.setVersion(dataSourceRDFResource.getProperty(DCTerms.hasVersion).getObject().toString());
				}

				//  ContactPerson Info
				if (dataSourceRDFResource.hasProperty(FEDLCA.hasContactPerson)){
					// FIXME: SHOULD THERE BE MORE THAN ONE CONTACT PERSON ALLOWED?
					Statement statement = dataSourceRDFResource.getProperty(FEDLCA.hasContactPerson);
					RDFNode person = statement.getObject();
					if (person.asResource().hasProperty(FEDLCA.personName)){
						dataSourceMD.setContactName(person.asResource().getProperty(FEDLCA.personName).getObject().toString());
					}
					if (person.asResource().hasProperty(FEDLCA.affiliation)){
						dataSourceMD.setContactAffiliation(person.asResource().getProperty(FEDLCA.affiliation).getObject().toString());
					}
					if (person.asResource().hasProperty(FEDLCA.email)){
						dataSourceMD.setContactEmail(person.asResource().getProperty(FEDLCA.email).getObject().toString());
					}
					if (person.asResource().hasProperty(FEDLCA.voicePhone)){
						dataSourceMD.setContactPhone(person.asResource().getProperty(FEDLCA.voicePhone).getObject().toString());
					}	
				}

				// THE DATASOURCE SHOULD NOT STORE THE CURATOR, EXCEPT IN ANNOTATIONS!!
				
				//  CuratorPerson Info
//				if (dataSourceRDFResource.hasProperty(FEDLCA.curatedBy)){
//					Statement statement = dataSourceRDFResource.getProperty(FEDLCA.curatedBy);
//					RDFNode person = statement.getObject();
//					if (person.asResource().hasProperty(FEDLCA.personName)){
//						dataSourceMD.setContactName(person.asResource().getProperty(FEDLCA.personName).getObject().toString());
//					}
//					if (person.asResource().hasProperty(FEDLCA.affiliation)){
//						dataSourceMD.setContactAffiliation(person.asResource().getProperty(FEDLCA.affiliation).getObject().toString());
//					}
//					if (person.asResource().hasProperty(FEDLCA.email)){
//						dataSourceMD.setContactEmail(person.asResource().getProperty(FEDLCA.email).getObject().toString());
//					}
//					if (person.asResource().hasProperty(FEDLCA.voicePhone)){
//						dataSourceMD.setContactPhone(person.asResource().getProperty(FEDLCA.voicePhone).getObject().toString());
//					}	
//				}		

				dataSourceProvider.setDataSourceMD(dataSourceMD);
//				dataSourceProvider.setCuratorMD(curatorMD);
				DataSourceKeeper.add(dataSourceProvider);
			}
		}

	}

//	public static void syncDataSourceProviderToTDB(DataSourceProvider dataSourceProvider) {
//		// THIS SHOULD NOT BE NECESSARY, AS JAVA OBJECTS SHOULD SYNC ON CHANGE
//
//		DataSourceMD dataSourceMD = dataSourceProvider.getDataSourceMD();
//		CuratorMD curatorMD = dataSourceProvider.getCuratorMD();
//
//		Model model = ActiveTDB.model;
//		Resource tdbResource = dataSourceProvider.getTdbResource();
//		assert tdbResource != null : "tdbResource cannot be null";
//		assert RDFS.label != null : "RDFS.label cannot be null";
//		assert dataSourceMD.getPersonName() != null : "dataSourceMD.getName() cannot be null";
//		System.out.println("tdbResource = " + tdbResource);
//
//		if (tdbResource.hasProperty(RDFS.label)) {
//			tdbResource.removeAll(RDFS.label);
//		}
//		model.addLiteral(tdbResource, RDFS.label, model.createLiteral(dataSourceMD.getPersonName()));
//
//		// tdbResource.removeAll(RDFS.comment);
//		model.addLiteral(tdbResource, RDFS.comment, model.createLiteral(dataSourceMD.getComments()));
//
//		tdbResource.removeAll(DCTerms.hasVersion);
//		model.addLiteral(tdbResource, DCTerms.hasVersion, model.createLiteral(dataSourceMD.getVersion()));
//
//		tdbResource.removeAll(FEDLCA.personName);
//		model.addLiteral(tdbResource, FEDLCA.personName, model.createLiteral(dataSourceMD.getContactName()));
//
//		tdbResource.removeAll(FEDLCA.affiliation);
//		model.addLiteral(tdbResource, FEDLCA.affiliation, model.createLiteral(dataSourceMD.getContactAffiliation()));
//
//		tdbResource.removeAll(FEDLCA.email);
//		model.addLiteral(tdbResource, FEDLCA.email, model.createLiteral(dataSourceMD.getContactEmail()));
//
//		tdbResource.removeAll(FEDLCA.voicePhone);
//		model.addLiteral(tdbResource, FEDLCA.voicePhone, model.createLiteral(dataSourceMD.getContactPhone()));
//
//		tdbResource.removeAll(FEDLCA.personName);
//		model.addLiteral(tdbResource, FEDLCA.personName, model.createLiteral(curatorMD.getName()));
//
//		tdbResource.removeAll(FEDLCA.affiliation);
//		model.addLiteral(tdbResource, FEDLCA.affiliation, model.createLiteral(curatorMD.getAffiliation()));
//
//		tdbResource.removeAll(FEDLCA.email);
//		model.addLiteral(tdbResource, FEDLCA.email, model.createLiteral(curatorMD.getEmail()));
//
//		tdbResource.removeAll(FEDLCA.voicePhone);
//		model.addLiteral(tdbResource, FEDLCA.voicePhone, model.createLiteral(curatorMD.getPhone()));
//
//		if (!dataSourceMD.getComments().matches("^\\s*$")) {
//			// ONLY IF NOT ALL WHITE SPACES
//			model.addLiteral(tdbResource, RDFS.comment, model.createLiteral(dataSourceMD.getComments()));
//		}
//
//		for (FileMD fileMD : dataSourceProvider.getFileMDList()) {
//			if (fileMD.getTdbResource() == null) {
//				fileMD.syncDataToTDB();
//			}
//			model.add(tdbResource, LCAHT.containsFile, fileMD.getTdbResource());
//		}
//	}

	public static int removeAllWithSubject(Resource subject) {
		int count = 0;
		StmtIterator stmtIterator = subject.listProperties();
		Set<Statement> statementSet = stmtIterator.toSet();
		// System.out.println("statementSet.size(): " + statementSet.size());
		for (Statement statement : statementSet) {
			// System.out.println("Statement: " + statement);
			statement.remove();
			count++;
		}
		count++;
		return count;
	}

	public static int removeAllWithObject(RDFNode object) {
		int count = 0;
		List<Property> properties = getAllProperties();
		for (Property predicate : properties) {
			count += removeAllWithPredicateObject(predicate, object);
		}
		return count;
	}

	public static int removeAllWithSubjectPredicate(Resource subject, Property predicate) {
		int count = 0;
		List<Statement> statements = subject.listProperties(predicate).toList();
		for (Statement statement : statements) {
			statement.remove();
			count++;
		}
		return count;
	}

	public static int removeAllWithPredicateObject(Property predicate, RDFNode object) {
		int count = 0;
		ResIterator resIterator = model.listSubjectsWithProperty(predicate, object);
		while (resIterator.hasNext()) {
			Resource subject = resIterator.next();
			model.remove(subject, predicate, object);
			count++;
		}
		return count;
	}

	public static List<Property> getAllProperties() {
		List<Property> results = new ArrayList<Property>();
		QGetAllProperties qGetAllProperties = new QGetAllProperties();
		ResultSet resultSet = qGetAllProperties.getResultSet();
		List<String> resultVars = resultSet.getResultVars();
		String p = resultVars.get(0);
		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.nextSolution();
			Resource predAsResource = querySolution.getResource(p);
			String predAsString = predAsResource.getURI();
			Property predicate = model.createProperty(predAsString);
			results.add(predicate);
		}
		return results;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addSelectedTDBListener(IActiveTDBListener listener) {
		// System.out.println("Added TDBListener = " + listener);
		// activeTDBListeners.add(listener);
	}

	@Override
	public void removeSelectedTDBListener(IActiveTDBListener listener) {
		// TODO Auto-generated method stub

	}

	public static ActiveTDB getInstance() {
		return instance;
	}

	public static void setInstance(ActiveTDB instance) {
		ActiveTDB.instance = instance;
	}

	public static Date getDateFromLiteral(Literal typedLiteralDate) {
		Date resultingDate = null;
		if (!typedLiteralDate.isLiteral()) {
			return null;
		}

		Literal literalDate = typedLiteralDate.asLiteral();
		String formattedDate = literalDate.getString();
		String actualFormattedDate = formattedDate.replaceFirst("\\^\\^.*", "");

		try {
			resultingDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(actualFormattedDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return resultingDate;
	}

}
