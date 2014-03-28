package harmonizationtool.comands;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import harmonizationtool.Activator;
import harmonizationtool.QueryView;
import harmonizationtool.ViewData;
import harmonizationtool.dialog.GenericMessageBox;
import harmonizationtool.model.CuratorMD;
import harmonizationtool.model.DataSetKeeper;
import harmonizationtool.model.DataSetMD;
import harmonizationtool.model.DataSetProvider;
import harmonizationtool.query.QGetAllProperties;
import harmonizationtool.utils.Util;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.ETHOLD;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.Bundle;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
//import com.hp.hpl.jena.query.Query;
//import com.hp.hpl.jena.query.QueryExecution;
//import com.hp.hpl.jena.query.QueryExecutionFactory;
//import com.hp.hpl.jena.query.QueryFactory;
//import com.hp.hpl.jena.query.QuerySolution;
//import com.hp.hpl.jena.query.ResultSet;
//import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
//import com.hp.hpl.jena.rdf.model.RDFNode;
//import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.PrefixMapping.Factory;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class SelectTDB implements IHandler, ISelectedTDB {
	public static Model model = null;
	public static Dataset dataset = null;
	public static String tdbDir = null;
	public static GraphStore graphStore = null;
	private static SelectTDB instance = null;
	private List<ISelectedTDBListener> selectedTDBListeners = new ArrayList<ISelectedTDBListener>();

	public SelectTDB() {
		System.out.println("created SelectTDB");
		instance = this;
	}

	public static SelectTDB getInstance() {
		return instance;
	}

	public void finalize() {
		System.out.println("closing dataset and model");
		cleanUp();
	}

	private void cleanUp() {
		try {
			dataset.close();
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
			syncToDataSetKeeper();
		} catch (Exception e) {
			Exception e2 = new ExecutionException(
					"***********THE TDB MAY BE BAD*******************");
			e2.printStackTrace();
			System.exit(1);
		}

		return null;
	}

	private void updateStatusLine() {
		String msg;
		msg = "Using TDB: " + Util.getPreferenceStore().getString("defaultTDB");
		Util.findView(QueryView.ID).getViewSite().getActionBars()
				.getStatusLineManager().setMessage(msg);
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
			String msg = "Opening TDB: "
					+ Util.getPreferenceStore().getString("defaultTDB");
			Util.findView(QueryView.ID).getViewSite().getActionBars()
					.getStatusLineManager().setMessage(msg);
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
					dataset = TDBFactory
							.createDataset(defaultTDBFile.getPath());
					assert dataset != null : "dataset cannot be null";
					model = dataset.getDefaultModel();
					graphStore = GraphStoreFactory.create(dataset);

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
	public static void syncToDataSetKeeper() { // THIS IS CURRENTLY LIKE
												// initiateDataSetKeeper
		if (model == null) {
			openTDB();
		}
		assert model != null : "model should not be null";
		ResIterator iterator = null;
		try {
			iterator = model.listSubjectsWithProperty(RDF.type, ECO.DataSource);
		} catch (Exception e) {
			Exception e2 = new ExecutionException(
					"***********THE TDB MAY BE BAD*******************");
			e2.printStackTrace();
			System.exit(1);
		}

		while (iterator.hasNext()) {
			System.out.println("got another...");
			Resource subject = (Resource) iterator.next();
			// int dataSetIndexPlusOne = DataSetKeeper.getByTdbResource(subject)
			// + 1;
			int dataSetIndex = DataSetKeeper.getByTdbResource(subject);
			if (dataSetIndex < 0) {
				DataSetProvider dataSetProvider = new DataSetProvider();
				dataSetProvider.setTdbResource(subject);
				DataSetMD dataSetMD = new DataSetMD();
				CuratorMD curatorMD = new CuratorMD();
				if (model.contains(subject, RDFS.label)) {
					NodeIterator nodeIterator = model.listObjectsOfProperty(
							subject, RDFS.label);
					RDFNode node = nodeIterator.next(); // TAKES FIRST ONE (WHAT
														// IF THERE ARE MORE
														// THAN ONE?)
					dataSetMD.setName(node.asLiteral().getString());
					System.out.println("Adding name: "
							+ node.asLiteral().getString() + " for subject: "
							+ subject.getURI());
				}
				if (model.contains(subject, RDFS.comment)) {
					NodeIterator nodeIterator = model.listObjectsOfProperty(
							subject, RDFS.comment);
					RDFNode node = nodeIterator.next(); // TAKES FIRST ONE (WHAT
														// IF THERE ARE MORE
														// THAN ONE?)
					dataSetMD.setComments(node.asLiteral().getString());
					System.out.println("Adding comment: "
							+ node.asLiteral().getString() + " for subject: "
							+ subject.getURI());
				}
				String version = "";
				if (model.contains(subject, DCTerms.hasVersion)) {
					version = model
							.listObjectsOfProperty(subject, DCTerms.hasVersion)
							.next().asLiteral().getString(); // TAKES
																// FIRST
																// ONE
																// (WHAT
																// IF
																// THERE
																// ARE
																// MORE
																// THAN
																// ONE?)
				} else if (model.contains(subject, ECO.hasMajorVersionNumber)) {
					version = model
							.listObjectsOfProperty(subject,
									ECO.hasMajorVersionNumber).next() // TAKES
																		// FIRST
																		// ONE
																		// (WHAT
																		// IF
																		// THERE
																		// ARE
																		// MORE
																		// THAN
																		// ONE?)
							.asLiteral().getString();
					if (model.contains(subject, ECO.hasMinorVersionNumber)) {
						version += "."
								+ model.listObjectsOfProperty(subject,
										ECO.hasMinorVersionNumber).next() // TAKES
																			// FIRST
																			// ONE
																			// (WHAT
																			// IF
																			// THERE
																			// ARE
																			// MORE
																			// THAN
																			// ONE?)
										.asLiteral().getString();
					}
					model.addLiteral(subject, DCTerms.hasVersion,
							model.createTypedLiteral(version)); // ADDING
					// VERSION
					// INFO
				}
				dataSetMD.setVersion(version);
				System.out.println("Adding version: " + version
						+ " for subject: " + subject.getURI());

				dataSetProvider.setDataSetMD(dataSetMD);
				dataSetProvider.setCuratorMD(curatorMD);
				DataSetKeeper.add(dataSetProvider);
				// int newIndexPlusOne = DataSetKeeper.indexOf(dataSetProvider)
				// + 1;
				// if (model.contains(subject, ETHOLD.localSerialNumber)) {
				// NodeIterator nodeIterator = model.listObjectsOfProperty(
				// subject, ETHOLD.localSerialNumber);
				// while (nodeIterator.hasNext()) {
				// RDFNode rdfNode = nodeIterator.next();
				// System.out.println("Is it literal? -- "
				// + rdfNode.isLiteral());
				// model.remove(subject, ETHOLD.localSerialNumber,
				// rdfNode.asLiteral());
				// }
				// }
				// model.addLiteral(subject, ETHOLD.localSerialNumber,
				// model.createTypedLiteral(newIndexPlusOne));
			} else {
				String dsName = null;
				NodeIterator dataSetNameIterator = model.listObjectsOfProperty(
						subject, RDFS.label);
				while (dataSetNameIterator.hasNext()) {
					if (dsName != null) {
						System.out
								.println("HEY!  Data Set has more than one name:"
										+ dsName);
					}
					dsName = dataSetNameIterator.next().asLiteral().getString();
				}
				if (dsName == null) {
					dsName = "Temp Data Set Name #" + dataSetIndex;
				}

				System.out.println("Id for " + dsName + " with URI: "
						+ subject.getURI() + " = "
						+ DataSetKeeper.getByTdbResource(subject));
				// DESTROY ALL CURRENT ethold:localSerialNumber -- I THINK THIS
				// IS THE RIGHT THING
				// if (model.contains(subject, ETHOLD.localSerialNumber)) {
				// NodeIterator nodeIterator = model.listObjectsOfProperty(
				// subject, ETHOLD.localSerialNumber);
				// while (nodeIterator.hasNext()) {
				// RDFNode rdfNode = nodeIterator.next();
				// System.out.println("Is it literal? -- "
				// + rdfNode.isLiteral());
				// model.remove(subject, ETHOLD.localSerialNumber,
				// rdfNode.asLiteral());
				// }
				// }
				// model.addLiteral(subject, ETHOLD.localSerialNumber,
				// model.createTypedLiteral(dataSetIndexPlusOne));
			}
		}
	}

	public static int removeAllWithSubject(Resource subject) {
		int count = 0;
		StmtIterator stmtIterator = subject.listProperties();
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
			model.remove(statement);
			count++;
		}
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
		StmtIterator stmtIterator = subject.listProperties(predicate);
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
			model.remove(statement);
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
			Property predicate = (Property) predAsResource;
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
	public void addSelectedTDBListener(ISelectedTDBListener listener) {
		System.out.println("Added TDBListener = " + listener);
		selectedTDBListeners.add(listener);
	}

	@Override
	public void removeSelectedTDBListener(ISelectedTDBListener listener) {
		// TODO Auto-generated method stub

	}

}
