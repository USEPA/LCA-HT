package gov.epa.nrmrl.std.lca.ht.tdb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gov.epa.nrmrl.std.lca.ht.views.QueryView;
import harmonizationtool.dialog.GenericMessageBox;
import harmonizationtool.model.CuratorMD;
import harmonizationtool.model.DataSetKeeper;
import harmonizationtool.model.DataSetMD;
import harmonizationtool.model.DataSetProvider;
import harmonizationtool.query.QGetAllProperties;
import harmonizationtool.utils.Util;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FEDLCA;

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
	private List<IActiveTDBListener> activeTDBListeners = new ArrayList<IActiveTDBListener>();

	public ActiveTDB() {
		System.out.println("created ActiveTDB");
		instance = this;
	}

//	public static ActiveTDB getInstance() {
//		return instance;
//	}

	public void finalize() {
		System.out.println("closing TDBDataset and model");
		cleanUp();
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
			syncTDBToDataSetKeeper();
		} catch (Exception e) {
			Exception e2 = new ExecutionException(
					"***********THE TDB MAY BE BAD*******************");
			e2.printStackTrace();
			System.exit(1);
		}

		return null;
	}

	private void updateStatusLine() {
//		String msg;
//		msg = "Using TDB: " + Util.getPreferenceStore().getString("defaultTDB");
//		Util.findView(QueryView.ID).getViewSite().getActionBars()
//				.getStatusLineManager().setMessage(msg);
	}

	private static void openTDB() {
		while (model == null) {
			if ((Util.getPreferenceStore().getString("defaultTDB") == null)
					|| (Util.getPreferenceStore().getString("defaultTDB") == "")) {
				Shell shell = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell();
				StringBuilder b = new StringBuilder();
				b.append("The Harmonization Tool (HT) requires the user to specify a directory for the Triplestore DataBase (TDB). ");
				b.append("Please pick an existing TDB directory, or an empty one where the HT can create a new TDB.");
				new GenericMessageBox(shell, "Welcome!", b.toString());

				redirectToPreferences();
			}
//			String msg = "Opening TDB: "
//					+ Util.getPreferenceStore().getString("defaultTDB");
//			Util.findView(QueryView.ID).getViewSite().getActionBars()
//					.getStatusLineManager().setMessage(msg);
			String defaultTDB = Util.getPreferenceStore().getString(
					"defaultTDB");
			File defaultTDBFile = new File(defaultTDB);
			if (!defaultTDBFile.isDirectory()) {
				// ask user for TDB directory
				Shell shell = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell();
				StringBuilder b = new StringBuilder();
				b.append("Sorry, but the Default TDB set in your preferences is not an accessible directory. ");
				b.append("Please select an existing TDB directory, or an accessible directory for a new TDB.");
				new GenericMessageBox(shell, "Alert", b.toString());

				redirectToPreferences();
			} else {
				System.out.println("defaultTDBFile.list().length="
						+ defaultTDBFile.list().length);
				try {
					TDBDataset = TDBFactory
							.createDataset(defaultTDBFile.getPath());
					assert TDBDataset != null : "TDBDataset cannot be null";
					model = TDBDataset.getDefaultModel();
					graphStore = GraphStoreFactory.create(TDBDataset);

				} catch (Exception e1) {
					Shell shell = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell();
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
			ICommandService commandService = (ICommandService) serviceLocator
					.getService(ICommandService.class);
			Command command = commandService
					.getCommand("org.eclipse.ui.window.preferences");
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
	 * Because the DataSetKeeper does not contain DataSets, but the TDB might,
	 * we need to get DataSet info from the TDB Each TDB subject which is
	 * rdf:type eco:DataSource should have a DataSetProvider NOTE:
	 * eco:DataSource should change to lcaht:DataSet
	 */
	public static void syncTDBToDataSetKeeper() { // THIS IS CURRENTLY LIKE
													// initiateDataSetKeeper
		if (model == null) {
			openTDB();
		}
		assert model != null : "model should not be null";
		ResIterator iterator = model.listSubjectsWithProperty(RDF.type,
				ECO.DataSource);
		while (iterator.hasNext()) {
			Resource subject = iterator.next();

			int dataSetIndex = DataSetKeeper.getByTdbResource(subject);
			if (dataSetIndex < 0) {
				DataSetProvider dataSetProvider = new DataSetProvider();
				dataSetProvider.setTdbResource(subject);
				DataSetMD dataSetMD = new DataSetMD();
				CuratorMD curatorMD = new CuratorMD();

				// RDFS.label <=> Name
				if (subject.listProperties(RDFS.label).toList().size() > 1) {
					System.out.println("!Data set has "
							+ subject.listProperties(RDFS.label).toList()
									.size() + " labels!");
					StmtIterator stmtIterator = subject
							.listProperties(RDFS.label);
					while (stmtIterator.hasNext()) {
						Statement statement = stmtIterator.next();
						statement.remove();
					}
				}
				String name = subject.getProperty(RDFS.label).getObject()
						.toString();

//				while (DataSetKeeper.indexOfDataSetName(name) > -1) {
//					name += "+";
//				}
				String newName = DataSetKeeper.uniquify(name);
				name = DataSetKeeper.uniquify(name);
				subject.getProperty(RDFS.label).changeObject(name);
				dataSetMD.setName(name);

				// RDFS.comment <=> Comments
				if (subject.hasProperty(RDFS.comment)) {
					dataSetMD.setComments(subject.getProperty(RDFS.comment)
							.getObject().toString());
				}

				// DCTerms.hasVersion <=> Version
				if (subject.hasProperty(DCTerms.hasVersion)) {
					dataSetMD.setVersion(subject
							.getProperty(DCTerms.hasVersion).getObject()
							.toString());
				}

				// ECOGOV.dataSetContactName <=> ContactName
				if (subject.hasProperty(FEDLCA.dataSetContactName)) {
					dataSetMD.setContactName(subject
							.getProperty(FEDLCA.dataSetContactName).getObject()
							.toString());
				}
				// ECOGOV.dataSetContactAffiliation <=> ContactAffiliation
				if (subject.hasProperty(FEDLCA.dataSetContactAffiliation)) {
					dataSetMD.setContactAffiliation(subject
							.getProperty(FEDLCA.dataSetContactAffiliation)
							.getObject().toString());
				}
				// ECOGOV.dataSetContactEmail <=> ContactEmail
				if (subject.hasProperty(FEDLCA.dataSetContactEmail)) {
					dataSetMD.setContactEmail(subject
							.getProperty(FEDLCA.dataSetContactEmail)
							.getObject().toString());
				}
				// ECOGOV.dataSetContactPhone <=> ContactPhone
				if (subject.hasProperty(FEDLCA.dataSetContactPhone)) {
					dataSetMD.setContactPhone(subject
							.getProperty(FEDLCA.dataSetContactPhone)
							.getObject().toString());
				}

				// ECOGOV.dataSetCuratorName <=> CuratorName
				if (subject.hasProperty(FEDLCA.dataSetCuratorName)) {
					curatorMD.setName(subject
							.getProperty(FEDLCA.dataSetCuratorName).getObject()
							.toString());
				}
				// ECOGOV.dataSetCuratorAffiliation <=> CuratorAffiliation
				if (subject.hasProperty(FEDLCA.dataSetCuratorAffiliation)) {
					curatorMD.setAffiliation(subject
							.getProperty(FEDLCA.dataSetCuratorAffiliation)
							.getObject().toString());
				}
				// ECOGOV.dataSetCuratorEmail <=> CuratorEmail
				if (subject.hasProperty(FEDLCA.dataSetCuratorEmail)) {
					curatorMD.setEmail(subject
							.getProperty(FEDLCA.dataSetCuratorEmail)
							.getObject().toString());
				}
				// ECOGOV.dataSetCuratorPhone <=> CuratorPhone
				if (subject.hasProperty(FEDLCA.dataSetCuratorPhone)) {
					curatorMD.setPhone(subject
							.getProperty(FEDLCA.dataSetCuratorPhone)
							.getObject().toString());
				}
				// pref.localname <=> CuratorName
				// pref.localname <=> CuratorAffiliation
				// pref.localname <=> CuratorEmail
				// pref.localname <=> CuratorPhone

				dataSetProvider.setDataSetMD(dataSetMD);
				dataSetProvider.setCuratorMD(curatorMD);
				DataSetKeeper.add(dataSetProvider);
			}
		}
	}

	public static void syncDataSetProviderToTDB(DataSetProvider dsProvider) {
		// SHOULD BREAK OUT TO ITS OWN CLASS OR ADD TO DataSetProvider or
		// ActiveTDB
		DataSetMD dataSetMD = dsProvider.getDataSetMD();
		CuratorMD curatorMD = dsProvider.getCuratorMD();

		Model model = ActiveTDB.model;
		Resource tdbResource = dsProvider.getTdbResource();
		assert tdbResource != null : "tdbResource cannot be null";
		assert RDFS.label != null : "RDFS.label cannot be null";
		assert dataSetMD.getName() != null : "dataSetMD.getName() cannot be null";
		System.out.println("tdbResource = " + tdbResource);
		

		if (tdbResource.hasProperty(RDFS.label)) {
			tdbResource.removeAll(RDFS.label);
		}
		model.addLiteral(tdbResource, RDFS.label,
				model.createLiteral(dataSetMD.getName()));

		// tdbResource.removeAll(RDFS.comment);
		model.addLiteral(tdbResource, RDFS.comment,
				model.createLiteral(dataSetMD.getComments()));

		tdbResource.removeAll(DCTerms.hasVersion);
		model.addLiteral(tdbResource, DCTerms.hasVersion,
				model.createLiteral(dataSetMD.getVersion()));

		tdbResource.removeAll(FEDLCA.dataSetContactName);
		model.addLiteral(tdbResource, FEDLCA.dataSetContactName,
				model.createLiteral(dataSetMD.getContactName()));

		tdbResource.removeAll(FEDLCA.dataSetContactAffiliation);
		model.addLiteral(tdbResource, FEDLCA.dataSetContactAffiliation,
				model.createLiteral(dataSetMD.getContactAffiliation()));

		tdbResource.removeAll(FEDLCA.dataSetContactEmail);
		model.addLiteral(tdbResource, FEDLCA.dataSetContactEmail,
				model.createLiteral(dataSetMD.getContactEmail()));

		tdbResource.removeAll(FEDLCA.dataSetContactPhone);
		model.addLiteral(tdbResource, FEDLCA.dataSetContactPhone,
				model.createLiteral(dataSetMD.getContactPhone()));

		tdbResource.removeAll(FEDLCA.dataSetCuratorName);
		model.addLiteral(tdbResource, FEDLCA.dataSetCuratorName,
				model.createLiteral(curatorMD.getName()));

		tdbResource.removeAll(FEDLCA.dataSetCuratorAffiliation);
		model.addLiteral(tdbResource, FEDLCA.dataSetCuratorAffiliation,
				model.createLiteral(curatorMD.getAffiliation()));

		tdbResource.removeAll(FEDLCA.dataSetCuratorEmail);
		model.addLiteral(tdbResource, FEDLCA.dataSetCuratorEmail,
				model.createLiteral(curatorMD.getEmail()));

		tdbResource.removeAll(FEDLCA.dataSetCuratorPhone);
		model.addLiteral(tdbResource, FEDLCA.dataSetCuratorPhone,
				model.createLiteral(curatorMD.getPhone()));

		if (!dataSetMD.getComments().matches("^\\s*$")) {
			// ONLY IF NOT ALL WHITE SPACES
			model.addLiteral(tdbResource, RDFS.comment,
					model.createLiteral(dataSetMD.getComments()));
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

	public static int removeAllWithSubjectPredicate(Resource subject,
			Property predicate) {
		int count = 0;
		List<Statement> statements = subject.listProperties(predicate).toList();
		for (Statement statement : statements) {
			statement.remove();
			count++;
		}
		return count;
	}

	public static int removeAllWithPredicateObject(Property predicate,
			RDFNode object) {
		int count = 0;
		ResIterator resIterator = model.listSubjectsWithProperty(predicate,
				object);
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
//		System.out.println("Added TDBListener = " + listener);
//		activeTDBListeners.add(listener);
	}

	@Override
	public void removeSelectedTDBListener(IActiveTDBListener listener) {
		// TODO Auto-generated method stub

	}

}
