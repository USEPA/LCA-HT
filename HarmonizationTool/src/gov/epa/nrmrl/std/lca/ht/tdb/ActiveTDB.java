package gov.epa.nrmrl.std.lca.ht.tdb;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import harmonizationtool.dialog.GenericMessageBox;
import harmonizationtool.model.CuratorMD;
import harmonizationtool.model.DataSourceKeeper;
import harmonizationtool.model.DataSourceMD;
import harmonizationtool.model.DataSourceProvider;
import harmonizationtool.model.FileMD;
import harmonizationtool.query.QGetAllProperties;
import harmonizationtool.utils.Util;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FEDLCA;
import harmonizationtool.vocabulary.LCAHT;

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

	// private List<IActiveTDBListener> activeTDBListeners = new ArrayList<IActiveTDBListener>();

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
			model.removeAll(subject, predicate, rdfNode);
		}
		System.out.println("Filename: "+ stringLiteral + " added to TDB");
		model.add(subject, predicate, model.createTypedLiteral(stringLiteral));
	}

	public static void replaceLiteral(Resource subject, Property predicate, Long longLiteral) {
		NodeIterator nodeIterator = model.listObjectsOfProperty(subject, predicate);
		while (nodeIterator.hasNext()) {
			RDFNode rdfNode = nodeIterator.next();
			model.removeAll(subject, predicate, rdfNode);
		}
		model.add(subject, predicate, model.createTypedLiteral(longLiteral));
	}

	public static void replaceLiteral(Resource subject, Property predicate, Date dateLiteral) {
		NodeIterator nodeIterator = model.listObjectsOfProperty(subject, predicate);
		while (nodeIterator.hasNext()) {
			RDFNode rdfNode = nodeIterator.next();
			model.removeAll(subject, predicate, rdfNode);
		}
		model.add(subject, predicate, model.createTypedLiteral(dateLiteral));
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
			if ((Util.getPreferenceStore().getString("defaultTDB") == null)
					|| (Util.getPreferenceStore().getString("defaultTDB") == "")) {
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
	 * Because the DataSourceKeeper does not contain DataSources, but the TDB might, we need to get DataSources info
	 * from the TDB. Each TDB subject which is rdf:type eco:DataSource should have a DataSourceProvider
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
				DataSourceMD dataSourceMD = new DataSourceMD();
				CuratorMD curatorMD = new CuratorMD();

				// RDFS.label <=> Name
				if (dataSourceRDFResource.listProperties(RDFS.label).toList().size() > 1) {
					System.out.println("!Data set has "
							+ dataSourceRDFResource.listProperties(RDFS.label).toList().size() + " labels!");
					StmtIterator stmtIterator = dataSourceRDFResource.listProperties(RDFS.label);
					while (stmtIterator.hasNext()) {
						Statement statement = stmtIterator.next();
						statement.remove();
					}
				}
				String name = dataSourceRDFResource.getProperty(RDFS.label).getObject().toString();

				name = DataSourceKeeper.uniquify(name);
				dataSourceRDFResource.getProperty(RDFS.label).changeObject(name);
				dataSourceMD.setName(name);

				// RDFS.comment <=> Comments
				if (dataSourceRDFResource.hasProperty(RDFS.comment)) {
					dataSourceMD.setComments(dataSourceRDFResource.getProperty(RDFS.comment).getObject().toString());
				}

				// DCTerms.hasVersion <=> Version
				if (dataSourceRDFResource.hasProperty(DCTerms.hasVersion)) {
					dataSourceMD.setVersion(dataSourceRDFResource.getProperty(DCTerms.hasVersion).getObject()
							.toString());
				}

				// FEDLCA.dataSourceContactName <=> ContactName
				if (dataSourceRDFResource.hasProperty(FEDLCA.dataSourceContactName)) {
					dataSourceMD.setContactName(dataSourceRDFResource.getProperty(FEDLCA.dataSourceContactName)
							.getObject().toString());
				}
				// FEDLCA.dataSourceContactAffiliation <=> ContactAffiliation
				if (dataSourceRDFResource.hasProperty(FEDLCA.dataSourceContactAffiliation)) {
					dataSourceMD.setContactAffiliation(dataSourceRDFResource
							.getProperty(FEDLCA.dataSourceContactAffiliation).getObject().toString());
				}
				// FEDLCA.dataSourceContactEmail <=> ContactEmail
				if (dataSourceRDFResource.hasProperty(FEDLCA.dataSourceContactEmail)) {
					dataSourceMD.setContactEmail(dataSourceRDFResource.getProperty(FEDLCA.dataSourceContactEmail)
							.getObject().toString());
				}
				// FEDLCA.dataSourceContactPhone <=> ContactPhone
				if (dataSourceRDFResource.hasProperty(FEDLCA.dataSourceContactPhone)) {
					dataSourceMD.setContactPhone(dataSourceRDFResource.getProperty(FEDLCA.dataSourceContactPhone)
							.getObject().toString());
				}

				// FEDLCA.dataSourceCuratorName <=> CuratorName
				if (dataSourceRDFResource.hasProperty(FEDLCA.dataSourceCuratorName)) {
					curatorMD.setName(dataSourceRDFResource.getProperty(FEDLCA.dataSourceCuratorName).getObject()
							.toString());
				}
				// FEDLCA.dataSourceCuratorAffiliation <=> CuratorAffiliation
				if (dataSourceRDFResource.hasProperty(FEDLCA.dataSourceCuratorAffiliation)) {
					curatorMD.setAffiliation(dataSourceRDFResource.getProperty(FEDLCA.dataSourceCuratorAffiliation)
							.getObject().toString());
				}
				// FEDLCA.dataSourceCuratorEmail <=> CuratorEmail
				if (dataSourceRDFResource.hasProperty(FEDLCA.dataSourceCuratorEmail)) {
					curatorMD.setEmail(dataSourceRDFResource.getProperty(FEDLCA.dataSourceCuratorEmail).getObject()
							.toString());
				}
				// FEDLCA.dataSourceCuratorPhone <=> CuratorPhone
				if (dataSourceRDFResource.hasProperty(FEDLCA.dataSourceCuratorPhone)) {
					curatorMD.setPhone(dataSourceRDFResource.getProperty(FEDLCA.dataSourceCuratorPhone).getObject()
							.toString());
				}
				// pref.localname <=> CuratorName
				// pref.localname <=> CuratorAffiliation
				// pref.localname <=> CuratorEmail
				// pref.localname <=> CuratorPhone

				dataSourceProvider.setDataSourceMD(dataSourceMD);
				dataSourceProvider.setCuratorMD(curatorMD);
				DataSourceKeeper.add(dataSourceProvider);

				NodeIterator nodeIterator = model.listObjectsOfProperty(dataSourceRDFResource, LCAHT.containsFile);
				while (nodeIterator.hasNext()) {
					RDFNode fileMDResource = nodeIterator.next();
					if (!fileMDResource.isLiteral()) {
						FileMD fileMD = new FileMD((Resource) fileMDResource);
						fileMD.syncDataFromTDB();
						dataSourceProvider.addFileMD(fileMD);
					}
				}
			}
		}

	}

	public static void syncDataSourceProviderToTDB(DataSourceProvider dsProvider) {
		// SHOULD BREAK OUT TO ITS OWN CLASS OR ADD TO DataSourceProvider or
		// ActiveTDB
		DataSourceMD dataSourceMD = dsProvider.getDataSourceMD();
		CuratorMD curatorMD = dsProvider.getCuratorMD();

		Model model = ActiveTDB.model;
		Resource tdbResource = dsProvider.getTdbResource();
		assert tdbResource != null : "tdbResource cannot be null";
		assert RDFS.label != null : "RDFS.label cannot be null";
		assert dataSourceMD.getName() != null : "dataSourceMD.getName() cannot be null";
		System.out.println("tdbResource = " + tdbResource);

		if (tdbResource.hasProperty(RDFS.label)) {
			tdbResource.removeAll(RDFS.label);
		}
		model.addLiteral(tdbResource, RDFS.label, model.createLiteral(dataSourceMD.getName()));

		// tdbResource.removeAll(RDFS.comment);
		model.addLiteral(tdbResource, RDFS.comment, model.createLiteral(dataSourceMD.getComments()));

		tdbResource.removeAll(DCTerms.hasVersion);
		model.addLiteral(tdbResource, DCTerms.hasVersion, model.createLiteral(dataSourceMD.getVersion()));

		tdbResource.removeAll(FEDLCA.dataSourceContactName);
		model.addLiteral(tdbResource, FEDLCA.dataSourceContactName, model.createLiteral(dataSourceMD.getContactName()));

		tdbResource.removeAll(FEDLCA.dataSourceContactAffiliation);
		model.addLiteral(tdbResource, FEDLCA.dataSourceContactAffiliation,
				model.createLiteral(dataSourceMD.getContactAffiliation()));

		tdbResource.removeAll(FEDLCA.dataSourceContactEmail);
		model.addLiteral(tdbResource, FEDLCA.dataSourceContactEmail,
				model.createLiteral(dataSourceMD.getContactEmail()));

		tdbResource.removeAll(FEDLCA.dataSourceContactPhone);
		model.addLiteral(tdbResource, FEDLCA.dataSourceContactPhone,
				model.createLiteral(dataSourceMD.getContactPhone()));

		tdbResource.removeAll(FEDLCA.dataSourceCuratorName);
		model.addLiteral(tdbResource, FEDLCA.dataSourceCuratorName, model.createLiteral(curatorMD.getName()));

		tdbResource.removeAll(FEDLCA.dataSourceCuratorAffiliation);
		model.addLiteral(tdbResource, FEDLCA.dataSourceCuratorAffiliation,
				model.createLiteral(curatorMD.getAffiliation()));

		tdbResource.removeAll(FEDLCA.dataSourceCuratorEmail);
		model.addLiteral(tdbResource, FEDLCA.dataSourceCuratorEmail, model.createLiteral(curatorMD.getEmail()));

		tdbResource.removeAll(FEDLCA.dataSourceCuratorPhone);
		model.addLiteral(tdbResource, FEDLCA.dataSourceCuratorPhone, model.createLiteral(curatorMD.getPhone()));

		if (!dataSourceMD.getComments().matches("^\\s*$")) {
			// ONLY IF NOT ALL WHITE SPACES
			model.addLiteral(tdbResource, RDFS.comment, model.createLiteral(dataSourceMD.getComments()));
		}
	}

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

}
