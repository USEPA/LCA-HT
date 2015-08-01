package gov.epa.nrmrl.std.lca.ht.tdb;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


//import gov.epa.nrmrl.std.lca.ht.dataCuration.AnnotationProvider;
import gov.epa.nrmrl.std.lca.ht.dataCuration.CurationMethods;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.FileMDKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.PersonKeeper;
import gov.epa.nrmrl.std.lca.ht.dialog.GenericMessageBox;
import gov.epa.nrmrl.std.lca.ht.dialog.StorageLocationDialog;
import gov.epa.nrmrl.std.lca.ht.log.LoggerManager;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.utils.RDFUtil;
import gov.epa.nrmrl.std.lca.ht.utils.Temporal;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;
import gov.epa.nrmrl.std.lca.ht.vocabulary.OpenLCA;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.IServiceLocator;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * This Class is intended to provide commonly used methods more complex TDB access methods than those offered through
 * the API. It including read and update queries.
 * Methods using transactions as well as those not using transactions are included.  The latter are often called from
 * within a transaction (e.g. in another method) since a transaction within a transaction is not allowed.
 * 
 * @author Tom Transue
 *
 */
public class ActiveTDB implements IHandler, IActiveTDB {
	// public static Model tdbModel = null;
	// DO NOT ATTEMPT TO MANAGE A STATIC COPY OF THE DEFAULT MODEL!!!
	public static Dataset tdbDataset = null;
	// private static String tdbDir = null;
	public static GraphStore graphStore = null;
	private static ActiveTDB instance = null;
	public static MessageDialog creationMessage;

	// private List<IActiveTDBListener> activeTDBListeners = new
	// ArrayList<IActiveTDBListener>();
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB";
	public static final String importGraphName = LCAHT.NS + "importGraph";
	public static final String exportGraphName = LCAHT.NS + "exportGraph";
	public static final String importPlusDefault = "importPlusDefault";

	private static DatasetAccessor datasetAccessor;

	public ActiveTDB() {
		System.out.println("created ActiveTDB");
		setInstance(this);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		/*if (tdbDataset != null) {
			if (tdbDataset.getDefaultModel() != null) {
				System.out.println("Attempting to execute ActiveTDB after the TDB is open!");
				return null;
			}
		}
		openTDB();
		try {
			syncTDBtoLCAHT();
		} catch (Exception e) {
			System.out.println("syncTDBtoLCAHT() failed with Exception: " + e);
			e.printStackTrace();
			Exception e2 = new ExecutionException("***********THE TDB MAY BE BAD*******************");
			e2.printStackTrace();
			System.exit(1);
		}*/
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		shell.setVisible(true);
		shell.getDisplay().update();

		return null;
	}

	public static void syncTDBtoLCAHT() {
		System.out.println("Syncing data sources");
		DataSourceKeeper.syncFromTDB();
		// System.out.println("Syncing people");
		// PersonKeeper.syncFromTDB();
		// System.out.println("Syncing files");
		// FileMDKeeper.syncFromTDB();
		System.out.println("Done syncing");
		// AnnotationProvider.updateCurrentAnnotationModifiedDate();
	}
	
	public static void initPrefs() {
		
	}

	public static void openTDB() {
		if (tdbDataset == null) {
			/*Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			String activeTDB = Util.getPreferenceStore().getString("defaultTDB");
			if (Util.EMPTY_STRING.equals(activeTDB)) {
				StorageLocationDialog dialog = new StorageLocationDialog(shell);
				shell.setVisible(false);
				dialog.open();
				if (dialog.getReturnCode() == StorageLocationDialog.RET_SHOW_PREFS)
					redirectToPreferences();
				String infoMessage = "Initializing TDB Data - please wait...";
				creationMessage = new MessageDialog(shell, "Info", null, infoMessage, MessageDialog.INFORMATION,
						new String[] {}, 0);
				creationMessage.setBlockOnOpen(false);
				creationMessage.open();
				creationMessage.getShell().getDisplay().update();
			}*/

			String defaultTDB = null;
			File defaultTDBFile = null;
			String errMsg = null;
			boolean tdbCreated = false;
			while (!tdbCreated) {
				defaultTDB = Util.getPreferenceStore().getString("defaultTDB");
				defaultTDBFile = new File(defaultTDB);

				if (defaultTDBFile.isDirectory()) {
					/*String infoMessage = "Initializing TDB Data - please wait...";
					creationMessage = new MessageDialog(shell, "Info", null, infoMessage, MessageDialog.INFORMATION,
							new String[] {}, 0);
					creationMessage.setBlockOnOpen(false);
					creationMessage.open();
					creationMessage.getShell().getDisplay().update();*/
					System.out.println("defaultTDBFile.list().length=" + defaultTDBFile.list().length);
					try {
						tdbDataset = TDBFactory.createDataset(defaultTDBFile.getPath());
						assert tdbDataset != null : "tdbDataset cannot be null";
						Model importModel = GraphFactory.makePlainModel();
						Model exportModel = GraphFactory.makePlainModel();

						datasetAccessor = DatasetAccessorFactory.create(tdbDataset);
						datasetAccessor.putModel(importGraphName, importModel);
						datasetAccessor.putModel(exportGraphName, exportModel);
						graphStore = GraphStoreFactory.create(tdbDataset);
						Util.getPreferenceStore().putValue("activeTDB", defaultTDB);
						Util.getPreferenceStore().save();

						tdbCreated = true;
						/*if (creationMessage != null)
							creationMessage.close();*/
						// TODO: Write to the Logger whether the TDB is freshly
						// created or has contents already. Also
						// write
						// to the TDB that the session has started
						// Prefixes.syncPrefixMapToTDBModel();
					} catch (Exception e1) {
						System.out.println("Exception: " + e1);
						if (!prefsCanceled) {
							// TODO: Determine when this message might display
							// and what the user and software shoul do
							// about it.
							StringBuilder b = new StringBuilder();
							b.append("It appears that the HT can not create a TDB in the default directory. ");
							b.append("You may need to create a new TDB.");
							errMsg = b.toString();
						}
					}
				} else {
					StringBuilder b = new StringBuilder();
					b.append("Sorry, but the Default TDB set in your preferences is not an accessible directory. ");
					b.append("Please select an existing TDB directory, or an accessible directory for a new TDB.");
					errMsg = b.toString();
				}
				if (!tdbCreated) {
					// ask user for TDB directory
					// TODO: Determine when this message might display and what
					// the user and software shoul do about it.
					new GenericMessageBox(null, "Error", errMsg);
					// If user has previously canceled with invalid data, quit.
					if (prefsCanceled) {
						errMsg = "The selected directory is not accessible - exiting now.";
						new GenericMessageBox(null, "Error", errMsg);
						System.exit(1);
					}
					redirectToPreferences();

				}
			}

		}
		PrefixMapping prefixMapping = Prefixes.getPrefixmapping();
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = getModel(null);
		try {
			tdbModel.setNsPrefixes(prefixMapping);
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("Prefix mapping sync ActiveTDB default failed with Exception: " + e);
			e.printStackTrace();
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---

		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		tdbModel = getModel(importGraphName);
		try {
			tdbModel.setNsPrefixes(prefixMapping);
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("Prefix mapping sync ActiveTDB import failed with Exception: " + e);
			e.printStackTrace();
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---

		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		tdbModel = getModel(exportGraphName);
		try {
			tdbModel.setNsPrefixes(prefixMapping);
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("Prefix mapping sync ActiveTDB export failed with Exception: " + e);
			e.printStackTrace();
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public static void copyImportGraphContentsToDefault() {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model importModel = getModel(importGraphName);
		try {
			datasetAccessor.add(importModel);
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("01 TDB transaction failed; see Exception: " + e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public static void copyDefaultModelToExportGraph() {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model defaultModel = tdbDataset.getDefaultModel();
		Model exportModel = tdbDataset.getNamedModel(exportGraphName);
		try {
			exportModel.add(defaultModel);
			// Model unionModel = ModelFactory.createUnion(defaultModel, exportModel);

			System.out.println("defaultModel: " + defaultModel.size());
			System.out.println("exportModel: " + exportModel.size());
			// System.out.println("unionModel: " + unionModel.size());
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("copyDatasetContentsToExportGraph(String datasetName) failed; see Exception: " + e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
	}

	public static Set<Resource> getDatasetMemberSubjects(String datasetName, String graphName) {
		if (datasetName == null) {
			return null;
		}
		Set<Resource> datasetMemberSubjects = new HashSet<Resource>();
		Resource datasetTDBResource = null;

		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.getModel(graphName);

		// First - find the dataset's TDBResource. If more than one, ( TODO ) present an intelligent response
		Literal dataSetNameLiteral = tdbModel.createTypedLiteral(datasetName);
		Selector selector0 = new SimpleSelector(null, RDFS.label, dataSetNameLiteral);
		StmtIterator stmtIterator0 = tdbModel.listStatements(selector0);
		while (stmtIterator0.hasNext()) {
			Statement statement = stmtIterator0.next();
			datasetTDBResource = statement.getSubject().asResource();
		}

		if (datasetTDBResource == null) {
			ActiveTDB.tdbDataset.end();
			return null;
		}

		// Next - get all subjects that are members of the dataset
		SimpleSelector selector1 = new SimpleSelector(null, ECO.hasDataSource, datasetTDBResource);
		StmtIterator stmtIterator1 = tdbModel.listStatements(selector1);
		stmtIterator1 = tdbModel.listStatements(selector1);
		while (stmtIterator1.hasNext()) {
			Statement statement = stmtIterator1.next();
			datasetMemberSubjects.add(statement.getSubject().asResource());
		}
		ActiveTDB.tdbDataset.end();
		return datasetMemberSubjects;
	}

	/**
	 * This method follows components of an RDF graph starting with a dataset (eco:DataSource).
	 * Each triple associated with the DataSource is included.
	 * 
	 * Then each subject with eco:hasDataSource [the DataSource] is followed in the following way:
	 * 1) Each triple with the subject is included.
	 * 2) Each triple in which a predicate of the triple from (1) AS Subject is included
	 * 3) Each triple in which a non-literal object of the triple from (1) AS Subject is included
	 * 4) Predicates and Objects found in the triples of (2) and (3) are followed in the same way
	 * 
	 * The goal is to gather all objects necessary for a given graph to be "complete".

	 * @param datasetName - the (unique) String name (rdfs:label) of the data set (eco:DataSource)
	 * @param graphName - String name of the graph from which to pull Statements.  Use null for the default graph.
	 * @param includeComparisons - if set to <i>true</i> Statements of associated Comparisons and ComparedMaster features will be
	 * included
	 * @return A List of Statements which may then be placed into an alternate graph or tested in a particular way.
	 */
	public static List<Statement> collectAllStatementsForDataset(String datasetName, String graphName,
			boolean includeComparisons) {
		if (datasetName == null) {
			return null;
		}
		Set<RDFNode> newNodesToCheck = new HashSet<RDFNode>();
		Resource datasetTDBResource = null;
		RDFNode datasetTDBNode = null;

		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.getModel(graphName);

		// First - find the dataset's TDBResource. If more than one, ( TODO ) present an intelligent response
		Literal dataSetNameLiteral = tdbModel.createTypedLiteral(datasetName);
		Selector selector0 = new SimpleSelector(null, RDFS.label, dataSetNameLiteral);
		StmtIterator stmtIterator0 = tdbModel.listStatements(selector0);
		while (stmtIterator0.hasNext()) {
			Statement statement = stmtIterator0.next();
			if (newNodesToCheck.size() > 0) {
				System.out.println("There should not be more than one dataset with this name: " + datasetName);
			}
			datasetTDBResource = statement.getSubject();
			datasetTDBNode = datasetTDBResource;
			newNodesToCheck.add(datasetTDBNode);
		}

		if (newNodesToCheck.size() == 0) {
			ActiveTDB.tdbDataset.end();
			return null;
		}
		// Next - get all aspects of the dataset itself
		stmtIterator0 = datasetTDBResource.listProperties();
		while (stmtIterator0.hasNext()) {
			Statement statement = stmtIterator0.next();
			newNodesToCheck.add(statement.getSubject());
		}

		// Next - get all subjects that are members of the dataset
		List<Resource> membersOfDataset = new ArrayList<Resource>();
		SimpleSelector selector1 = new SimpleSelector(null, ECO.hasDataSource, datasetTDBResource);
		StmtIterator stmtIterator1 = tdbModel.listStatements(selector1);
		stmtIterator1 = tdbModel.listStatements(selector1);
		while (stmtIterator1.hasNext()) {
			Statement statement = stmtIterator1.next();
			membersOfDataset.add(statement.getSubject());
		}
		newNodesToCheck.addAll(membersOfDataset);
		if (includeComparisons) {
			// Next - get all subjects that are members of the dataset
			for (Resource resource : membersOfDataset) {
				SimpleSelector selector2 = new SimpleSelector(null, FedLCA.comparedSource, resource);
				StmtIterator stmtIterator2 = tdbModel.listStatements(selector2);
				stmtIterator2 = tdbModel.listStatements(selector2);
				while (stmtIterator2.hasNext()) {
					Statement statement = stmtIterator2.next();
					newNodesToCheck.add(statement.getSubject());
				}
			}
		}
		ActiveTDB.tdbDataset.end();
		return collectStatementsTraversingNodeSet(newNodesToCheck, graphName);
	}

	/**
	 * This method "Follows" each RDFNode in the specified newNodesToCheck HashSet, collecting all Statements
	 * in which the RDFNode is a subject.  It then iterates using each Predicate and non-Literal Object in then
	 * next round of newNodesToCheck and continues until new new RDFNodes are found.
	 * <br/>A set of "stopClasses" can be specified in <i>stopAtTheseClasses</i>so that objects in triples which
	 * belong to specified RDF Classes can be explicitly <i>not</i> followed.
	 * <br/>Note that for some data
	 * structures or with a Reasoner in place, this method could take a long time and possibly return the entire
	 * graph.  An alternate version might include a specified maximum number of cycles to try or maximum size of
	 * the newNodesToCheck HashSet.
	 * 
	 * @param newNodesToCheck - a HashSet of RDFNodes to "follow"
	 * @param stopAtTheseClasses - a HashSet of RDFNodes to not follow. A subject with rdf:type [stopAtTheseClasses member]
	 * will not be followed.
	 * @param graphName - the String name of the graph within tdbDataset
	 * @return an ArrayList of Statements containing every triple found while following the RDFNodes.
	 */
	public static List<Statement> collectStatementsTraversingNodeSetWithStops(Set<RDFNode> newNodesToCheck,
			Set<RDFNode> stopAtTheseClasses, String graphName) {
		if (newNodesToCheck == null) {
			return null;
		}
		List<Statement> returnStatements = new ArrayList<Statement>();
		Set<RDFNode> nodesAlreadyFound = new HashSet<RDFNode>();

		int cycle = 0;
		while (newNodesToCheck.size() > 0) {
			cycle++;
			// System.out.println("Beginning cycle " + cycle + " . Starting with " + returnStatements.size()
			// + " statements, and " + newNodesToCheck.size() + " new nodes to check");
			List<Statement> newStatements = collectStatements(newNodesToCheck, graphName);
			nodesAlreadyFound.addAll(newNodesToCheck);
			newNodesToCheck.clear();
			for (Statement statement : newStatements) {
				returnStatements.add(statement);
				RDFNode predicate = statement.getPredicate();
				if (!nodesAlreadyFound.contains(predicate)) {
					nodesAlreadyFound.add(predicate);
					newNodesToCheck.add(predicate);
				}
				RDFNode object = statement.getObject();
				if (!nodesAlreadyFound.contains(object)) {
					boolean stopClass = false;
					if (!object.isLiteral()) {
						StmtIterator stmtIterator = object.asResource().listProperties(RDF.type);
						while (stmtIterator.hasNext()) {
							RDFNode classObject = stmtIterator.next().getObject();
							if (stopAtTheseClasses.contains(classObject)) {
								stopClass = true;
							}
						}
					}
					if (!stopClass) {
						nodesAlreadyFound.add(object);
						newNodesToCheck.add(object);
					}
				}
			}
		}
		// System.out.println("Completed after " + cycle + " + cycle(s). Found " + returnStatements.size()
		// + " statements, after checking a total of " + nodesAlreadyFound.size() + " nodes.");
		return returnStatements;
	}

	/**
	 * This method "Follows" each RDFNode in the specified newNodesToCheck HashSet, collecting all Statements
	 * in which the RDFNode is a subject.  It then iterates using each Predicate and non-Literal Object in then
	 * next round of newNodesToCheck and continues until new new RDFNodes are found.  Note that for some data
	 * structures or with a Reasoner in place, this method could take a long time and possibly return the entire
	 * graph.  An alternate version might include a specified maximum number of cycles to try or maximum size of
	 * the newNodesToCheck HashSet.
	 * 
	 * @param newNodesToCheck - a HashSet of RDFNodes to "follow"
	 * @param graphName - the String name of the graph within tdbDataset
	 * @return an ArrayList of Statements containing every triple found while following the RDFNodes.
	 */
	public static List<Statement> collectStatementsTraversingNodeSet(Set<RDFNode> newNodesToCheck, String graphName) {
		if (newNodesToCheck == null) {
			return null;
		}
		List<Statement> returnStatements = new ArrayList<Statement>();
		/*
		 * The lines below would solve the same problem, but a tiny bit slower, so leaving the full "copy of above
		 * method
		 */
		// Set<RDFNode> stopClassesEmpty = new HashSet<RDFNode>();
		// return collectStatementsTraversingNodeSetWithStops(newNodesToCheck, stopClassesEmpty, graphName);

		Set<RDFNode> nodesAlreadyFound = new HashSet<RDFNode>();

		int cycle = 0;
		while (newNodesToCheck.size() > 0) {
			cycle++;
			System.out.println("Beginning cycle " + cycle + " . Starting with " + returnStatements.size()
					+ " statements, and " + newNodesToCheck.size() + " new nodes to check");
			List<Statement> newStatements = collectStatements(newNodesToCheck, graphName);
			nodesAlreadyFound.addAll(newNodesToCheck);
			newNodesToCheck.clear();
			for (Statement statement : newStatements) {
				returnStatements.add(statement);
				RDFNode predicate = statement.getPredicate();
				if (!nodesAlreadyFound.contains(predicate)) {
					nodesAlreadyFound.add(predicate);
					newNodesToCheck.add(predicate);
				}
				RDFNode object = statement.getObject();
				if (!nodesAlreadyFound.contains(object)) {
					nodesAlreadyFound.add(object);
					newNodesToCheck.add(object);
				}
			}
		}
		System.out.println("Completed after " + cycle + " + cycle(s). Found " + returnStatements.size()
				+ " statements, after checking a total of " + nodesAlreadyFound.size() + " nodes.");
		return returnStatements;
	}

	/**
	 * Like collectStatementsTraversingNodeSet, this method "Follows" each RDFNode in the specified newNodesToCheck
	 * HashSet, returning all RDFNodes found while traversing the graph in a "forward" direction (i.e. not seeking
	 * all subjects and objects associated with predicates found, nor all subjects and predicates associated with all
	 * objects found).  It does iterate using each Predicate and non-Literal Object in then
	 * next round of newNodesToCheck and continues until new new RDFNodes are found.  Note that for some data
	 * structures or with a Reasoner in place, this method could take a long time and possibly return the entire
	 * graph.  An alternate version might include a specified maximum number of cycles to try or maximum size of
	 * the newNodesToCheck HashSet.
	 * 
	 * @param newNodesToCheck - a HashSet of RDFNodes to "follow"
	 * @param graphName - the String name of the graph within tdbDataset
	 * @return a HashSet of RDFNodes found while following the starting set of RDFNodes.
	 */
	public static Set<RDFNode> collectNodesTraversingNodeSet(Set<RDFNode> newNodesToCheck, String graphName) {
		if (newNodesToCheck == null) {
			return null;
		}
		List<Statement> returnStatements = new ArrayList<Statement>();
		Set<RDFNode> nodesAlreadyFound = new HashSet<RDFNode>();

		int cycle = 0;
		while (newNodesToCheck.size() > 0) {
			cycle++;
			System.out.println("Beginning cycle " + cycle + " . Starting with " + returnStatements.size()
					+ " statements, and " + newNodesToCheck.size() + " new nodes to check");
			List<Statement> newStatements = collectStatements(newNodesToCheck, graphName);
			nodesAlreadyFound.addAll(newNodesToCheck);
			newNodesToCheck.clear();
			for (Statement statement : newStatements) {
				returnStatements.add(statement);
				RDFNode predicate = statement.getPredicate();
				if (!nodesAlreadyFound.contains(predicate)) {
					nodesAlreadyFound.add(predicate);
					newNodesToCheck.add(predicate);
				}
				RDFNode object = statement.getObject();
				if (!nodesAlreadyFound.contains(object)) {
					nodesAlreadyFound.add(object);
					newNodesToCheck.add(object);
				}
			}
		}
		System.out.println("Completed after " + cycle + " + cycle(s). Found " + returnStatements.size()
				+ " statements, after checking a total of " + nodesAlreadyFound.size() + " nodes.");
		return nodesAlreadyFound;
	}

	public static List<Statement> collectStatementsStopAtQualifiedURIsWithStops(Set<RDFNode> newNodesToCheck,
			Set<RDFNode> stopAtTheseClasses, String graphName) {
		if (newNodesToCheck == null) {
			return null;
		}
		List<Statement> returnStatements = new ArrayList<Statement>();
		Set<RDFNode> nodesAlreadyFound = new HashSet<RDFNode>();

		int cycle = 0;
		while (newNodesToCheck.size() > 0) {
			cycle++;
			// System.out.println("Beginning cycle " + cycle + " . Starting with " + returnStatements.size()
			// + " statements, and " + newNodesToCheck.size() + " new nodes to check");
			List<Statement> newStatements = collectStatements(newNodesToCheck, graphName);
			nodesAlreadyFound.addAll(newNodesToCheck);
			newNodesToCheck.clear();
			for (Statement statement : newStatements) {
				returnStatements.add(statement);
				RDFNode object = statement.getObject();
				if (!nodesAlreadyFound.contains(object)) {
					boolean stopClass = false;
					if (object.isAnon()) {
						StmtIterator stmtIterator = object.asResource().listProperties(RDF.type);
						while (stmtIterator.hasNext()) {
							RDFNode classObject = stmtIterator.next().getObject();
							if (stopAtTheseClasses.contains(classObject)) {
								stopClass = true;
								break;
							}
						}
						if (!stopClass) {
							nodesAlreadyFound.add(object);
							newNodesToCheck.add(object);
						}
					}
				}
			}
		}
		// System.out.println("Completed after " + cycle + " + cycle(s). Found " + returnStatements.size()
		// + " statements, after checking a total of " + nodesAlreadyFound.size() + " nodes.");
		return returnStatements;
	}

	public static List<Statement> collectStatementsStopAtQualifiedURIs(Set<RDFNode> newNodesToCheck, String graphName) {
		if (newNodesToCheck == null) {
			return null;
		}
		List<Statement> returnStatements = new ArrayList<Statement>();
		Set<RDFNode> nodesAlreadyFound = new HashSet<RDFNode>();

		int cycle = 0;
		while (newNodesToCheck.size() > 0) {
			cycle++;
			// System.out.println("Beginning cycle " + cycle + " . Starting with " + returnStatements.size()
			// + " statements, and " + newNodesToCheck.size() + " new nodes to check");
			List<Statement> newStatements = collectStatements(newNodesToCheck, graphName);
			nodesAlreadyFound.addAll(newNodesToCheck);
			newNodesToCheck.clear();
			for (Statement statement : newStatements) {
				returnStatements.add(statement);

				RDFNode object = statement.getObject();
				if (!nodesAlreadyFound.contains(object)) {
					if (object.isAnon()) {
						nodesAlreadyFound.add(object);
						newNodesToCheck.add(object);
					}
				}
			}
		}
		// System.out.println("Completed after " + cycle + " + cycle(s). Found " + returnStatements.size()
		// + " statements, after checking a total of " + nodesAlreadyFound.size() + " nodes.");
		return returnStatements;
	}

	/**
	 * This returns all Statements within a specified graph in which the specified Nodes are a Subject.
	 * It can be used as part of a recursion to "follow" a set of Subjects, including all necessary components.
	 * 
	 * @param nodesToTest : a Set of nodes to find ?node ?p ?o statements if ?node is not a Literal.
	 * @param graphName is the String associated with the graph in tdbDataset
	 * @return An ArrayList<Statement> containing Statements with the nodesToTest entries as subjects
	 */
	private static List<Statement> collectStatements(Set<RDFNode> nodesToTest, String graphName) {
		if (nodesToTest == null) {
			return null;
		}
		List<Statement> returnList = new ArrayList<Statement>();

		if (nodesToTest.size() == 0) {
			return returnList;
		}
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.getModel(graphName);
		for (RDFNode rdfNode : nodesToTest) {
			if (!rdfNode.isLiteral()) {
				if (rdfNode.isResource()) {
					Resource resource = rdfNode.asResource();
					Selector selector = new SimpleSelector(resource, null, null, null);
					StmtIterator stmtIterator = tdbModel.listStatements(selector);
					returnList.addAll(stmtIterator.toList());
				}
			}
		}
		ActiveTDB.tdbDataset.end();
		return returnList;
	}

	public static void copyDatasetContentsToExportGraph(String datasetName, boolean includeComparisons) {
		List<Statement> statementsToCopy = collectAllStatementsForDataset(datasetName, null, true);

		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = getModel(exportGraphName);
		try {
			tdbModel.add(statementsToCopy);
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("copyDatasetContentsToExportGraph(String datasetName) failed; see Exception: " + e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---

		System.out.println("defaultModel: " + getModel(null).size());
		System.out.println("exportModel: " + getModel(exportGraphName).size());
	}

	public static void copyStatementsToGraph(List<Statement> statementsToCopy, String graphTo) {

		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = getModel(graphTo);
		try {
			tdbModel.add(statementsToCopy);
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("copyStatementsToGraph(" + graphTo + ") failed; see Exception: " + e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public static void clearImportGraphContents() {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model importModel = getModel(importGraphName);
		try {
			importModel.removeAll();
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("clearImportGraphContents() failed; see Exception: " + e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public static void clearExportGraphContents() {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model exportModel = getModel(exportGraphName);
		try {
			exportModel.removeAll();
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("clearExportGraphContents() failed; see Exception: " + e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	private static boolean prefsCanceled = false;

	public static void markPrefsCanceled() {
		prefsCanceled = true;
	}

	private static void redirectToPreferences() {
		prefsCanceled = false;
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

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		System.out.println("added listener: " + handlerListener);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public static void cleanUp() {
		try {
			tdbDataset.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void gets() {
		/* JUST A PLACE HOLDER SO THAT OUTLINE SHOWS A DIVIDER */
	}

	public static String getStringFromLiteral(RDFNode rdfNode) {
		if (!rdfNode.isLiteral()) {
			return null;
		}
		Literal literal = rdfNode.asLiteral();
		Object value = literal.getValue();
		if (value instanceof String) {
			return (String) value;
		}
		return null;
	}

	public static Long getLongFromLiteral(RDFNode rdfNode) {
		if (!rdfNode.isLiteral()) {
			return null;
		}
		Literal literal = rdfNode.asLiteral();
		Object value = literal.getValue();
		if (value instanceof Long) {
			return (Long) value;
		}
		return null;
	}

	public static Integer getIntegerFromLiteral(RDFNode rdfNode) {
		if (!rdfNode.isLiteral()) {
			return null;
		}
		Literal literal = rdfNode.asLiteral();
		Object value = literal.getValue();
		if (value instanceof Integer) {
			return (Integer) value;
		}
		return null;
	}

	private static void tsCreates() {
		/* JUST A PLACE HOLDER SO THAT OUTLINE SHOWS A DIVIDER */
	}

	public static Resource tsCreateResource(Resource rdfclass) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		Resource result = null;
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = getModel(null);
		try {
			result = tdbModel.createResource(rdfclass);
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("tsCreateResource(Resource rdfclass) failed; see Exception: " + e);
			tdbModel.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		return result;
	}

	public static Resource tsCreateResource(String uri) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		Resource result = null;
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = getModel(null);
		try {
			result = tdbModel.createResource(uri);
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("tsCreateResource(String uri) failed; see Exception: " + e);
			tdbModel.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		return result;
	}

	private static void tsRemovals() {
		/* JUST A PLACE HOLDER SO THAT OUTLINE SHOWS A DIVIDER */
	}

	public static void tsRemoveGenericTriple(Resource subject, Property predicate, RDFNode object, String graphName) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = getModel(graphName);
		try {
			tdbModel.removeAll(subject, predicate, object);
			tdbDataset.commit();
		} catch (Exception e) {
			System.out
					.println("tsRemoveGenericTriple(Resource subject, Property predicate, RDFNode object, String graphName) failed; see Exception: "
							+ e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public static int tsRemoveAllNonLiteralObjects(Resource subject, Property predicate, String graphName) {
		int triplesRemoved = -1;
		List<RDFNode> rdfNodesToRemove = new ArrayList<RDFNode>();
		// --- BEGIN SAFE -READ- TRANSACTION ---
		tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = getModel(graphName);
		NodeIterator nodeIterator = tdbModel.listObjectsOfProperty(subject, predicate);
		while (nodeIterator.hasNext()) {
			RDFNode rdfNode = nodeIterator.next();
			if (!rdfNode.isLiteral()) {
				rdfNodesToRemove.add(rdfNode);
			}
		}
		tdbDataset.end();
		// ---- END SAFE -READ- TRANSACTION ---
		triplesRemoved = rdfNodesToRemove.size();
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		tdbModel = getModel(graphName);
		try {
			for (RDFNode rdfNode : rdfNodesToRemove) {
				tdbModel.remove(subject, predicate, rdfNode);
			}
			tdbDataset.commit();
		} catch (Exception e) {
			System.out
					.println("tsRemoveAllNonLiteralObjects(Resource subject, Property predicate, String graphName) failed; see Exception: "
							+ e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		return triplesRemoved;
	}

	public static int tsRemoveAllLikeLiterals(Resource subject, Property predicate, Object thingLiteral,
			String graphName) {
		int countOfRemovedItems = -1;
		RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(thingLiteral);
		List<RDFNode> rdfNodesToRemove = new ArrayList<RDFNode>();
		// --- BEGIN SAFE -READ- TRANSACTION ---
		tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = getModel(graphName);
		NodeIterator nodeIterator = tdbModel.listObjectsOfProperty(subject, predicate);
		while (nodeIterator.hasNext()) {
			RDFNode rdfNode = nodeIterator.next();
			if (rdfNode.isLiteral()) {
				Literal literal = rdfNode.asLiteral();
				RDFDatatype rdfDatatypeOfNode = literal.getDatatype();
				if (rdfDatatypeOfNode == null) {
					System.out.println("yell!");
				} else if (rdfDatatypeOfNode.equals(rdfDatatype)) {
					rdfNodesToRemove.add(rdfNode);
				}
			}
		}
		tdbDataset.end();
		// ---- END SAFE -READ- TRANSACTION ---
		countOfRemovedItems = rdfNodesToRemove.size();
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		tdbModel = getModel(graphName);
		try {
			for (RDFNode rdfNode : rdfNodesToRemove) {
				tdbModel.remove(subject, predicate, rdfNode);
			}
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("tsRemoveAllLikeLiterals failed; see Exception: ");
			e.printStackTrace();
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		return countOfRemovedItems;
	}

	public static void tsRemoveStatement(Resource subject, Property predicate, RDFNode object) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = getModel(null);
		try {
			tdbModel.remove(subject, predicate, object);
			tdbDataset.commit();
			// sync();
		} catch (Exception e) {
			System.out.println("10 TDB transaction failed; see Exception: " + e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	private static void tsAdds() {
		/* JUST A PLACE HOLDER SO THAT OUTLINE SHOWS A DIVIDER */
	}

	public static void tsAddGeneralTriple(Resource subject, Property predicate, Object thingToAdd, String graphName) {
		if (thingToAdd == null) {
			return;
		}
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = getModel(graphName);
		try {
			if (thingToAdd instanceof RDFNode) {
				tdbModel.add(subject, predicate, (RDFNode) thingToAdd);

			} else {
				if (thingToAdd instanceof Calendar || thingToAdd instanceof Date) {
					String formattedDate = null;
					if (thingToAdd instanceof Date) {
						formattedDate = Temporal.dateFormatter.format(thingToAdd);
					} else {
						long milliseconds = ((Calendar) thingToAdd).getTimeInMillis();
						Date dateConvertedFromCalendar = new Date(milliseconds);
						formattedDate = Temporal.dateFormatter.format(dateConvertedFromCalendar);
					}
					Literal literalToAdd = tdbModel.createTypedLiteral(formattedDate, XSDDatatype.XSDdateTime);
					tdbModel.add(subject, predicate, literalToAdd);
				} else {
					RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(thingToAdd);
					Literal newRDFNode = tdbModel.createTypedLiteral(thingToAdd, rdfDatatype);
					tdbModel.add(subject, predicate, newRDFNode);
				}
			}
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("04 TDB transaction failed; see Exception: " + e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	private static void tsReplaces() {
		/* JUST A PLACE HOLDER SO THAT OUTLINE SHOWS A DIVIDER */
	}

	public static void tsReplaceLiteral(Resource subject, Property predicate, Object thingLiteral) {
		if (thingLiteral == null) {
			return;
		}
		tsRemoveAllLikeLiterals(subject, predicate, thingLiteral, null);
		tsAddGeneralTriple(subject, predicate, thingLiteral, null);
	}

	public static void tsReplaceLiteral(Resource subject, Property predicate, Object thingLiteral, String graphName) {
		tsRemoveAllLikeLiterals(subject, predicate, thingLiteral, graphName);
		tsAddGeneralTriple(subject, predicate, thingLiteral, graphName);
	}

	public static void tsReplaceLiteral(Resource subject, Property predicate, RDFDatatype rdfDatatype,
			Object thingLiteral) {
		tsRemoveAllLikeLiterals(subject, predicate, thingLiteral, null);
		tsAddGeneralTriple(subject, predicate, thingLiteral, null);
	}

	public static long countTriples(String graphName) {
		long result = -1;
		// --- BEGIN SAFE -READ- TRANSACTION ---
		tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = getModel(graphName);
		result = tdbModel.size();
		tdbDataset.end();
		// ---- END SAFE -READ- TRANSACTION ---
		return result;
	}

	public static int tsReplaceResourceSameType(Resource subject, Property predicate, Resource object, String graphName) {
		int removedTriples = tsRemoveAllLikeObjects(subject, predicate, object, graphName);
		if (object == null) {
			return removedTriples;
		}
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = getModel(graphName);
		try {
			tdbModel.add(subject, predicate, object);
			tdbDataset.commit();
		} catch (Exception e) {
			System.out
					.println("tsReplaceResourceSameType(Resource subject, Property predicate, Resource object, String graphName) failed; see Exception: "
							+ e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		return removedTriples;
	}

	public static int tsRemoveAllLikeObjects(Resource subject, Property predicate, Resource object, String graphName) {
		if (subject == null) {
			return -1;
		}
		if (object == null) {
			return tsRemoveAllNonLiteralObjects(subject, predicate, graphName);
		}
		if (object.isLiteral()) {
			return -1;
		}
		int countOfRemovedItems = -1;
		List<RDFNode> classesToWhichObjectBelongs = new ArrayList<RDFNode>();
		List<RDFNode> rdfNodesToRemove = new ArrayList<RDFNode>();

		// --- BEGIN SAFE -READ- TRANSACTION ---
		tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = getModel(graphName);
		NodeIterator nodeIteratorOfClasses = tdbModel.listObjectsOfProperty(object, RDF.type);
		while (nodeIteratorOfClasses.hasNext()) {
			RDFNode rdfNodeOfClass = nodeIteratorOfClasses.next();
			classesToWhichObjectBelongs.add(rdfNodeOfClass);
		}

		NodeIterator nodeIterator = tdbModel.listObjectsOfProperty(subject, predicate);
		while (nodeIterator.hasNext()) {
			RDFNode rdfNode = nodeIterator.next();
			if (!rdfNode.isLiteral()) {
				nodeIteratorOfClasses = tdbModel.listObjectsOfProperty(rdfNode.asResource(), RDF.type);
				while (nodeIteratorOfClasses.hasNext()) {
					RDFNode rdfNodeOfClass = nodeIteratorOfClasses.next();
					if (classesToWhichObjectBelongs.contains(rdfNodeOfClass)) {
						rdfNodesToRemove.add(rdfNode);
						break;
					}
				}
			}
		}
		tdbDataset.end();
		// ---- END SAFE -READ- TRANSACTION ---
		countOfRemovedItems = rdfNodesToRemove.size();
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		tdbModel = getModel(graphName);
		try {
			for (RDFNode rdfNode : rdfNodesToRemove) {
				tdbModel.remove(subject, predicate, rdfNode);
			}
			tdbDataset.commit();
		} catch (Exception e) {
			System.out
					.println("tsRemoveAllLikeObjects(Resource subject, Property predicate, Resource object, String graphName) failed; see Exception: "
							+ e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		return countOfRemovedItems;
	}

	public static Literal tsCreateTypedLiteral(Object thingToMakeLiteral, String graphName) {
		RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(thingToMakeLiteral);
		Literal literal = null;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = getModel(graphName);
		try {
			if (thingToMakeLiteral instanceof Calendar || thingToMakeLiteral instanceof Date) {
				String formattedDate = null;
				if (thingToMakeLiteral instanceof Date) {
					formattedDate = Temporal.dateFormatter.format(thingToMakeLiteral);
				} else {
					long milliseconds = ((Calendar) thingToMakeLiteral).getTimeInMillis();
					Date dateConvertedFromCalendar = new Date(milliseconds);
					formattedDate = Temporal.dateFormatter.format(dateConvertedFromCalendar);
				}
				literal = tdbModel.createTypedLiteral(formattedDate, XSDDatatype.XSDdateTime);
				// TODO: QUESTION: Does Jena actually write something when
				// creating Nodes? If not, no need for write-safe
				// transaction....
				// TODO: ... ALSO: Does it matter what graph the Node is created
				// in?
			} else {
				literal = tdbModel.createTypedLiteral(thingToMakeLiteral, rdfDatatype);
			}
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("tsCreateTypedLiteral(Object thingLiteral, String graphName) failed; see Exception: "
					+ e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		return literal;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void addSelectedTDBListener(IActiveTDBListener listener) {
	}

	@Override
	public void removeSelectedTDBListener(IActiveTDBListener listener) {
	}

	public static ActiveTDB getInstance() {
		return instance;
	}

	public static void setInstance(ActiveTDB instance) {
		ActiveTDB.instance = instance;
	}

	private static Model getFreshModel(String graphName) {
		Model model;
		tdbDataset.begin(ReadWrite.READ);
		if (graphName == null) {
			model = tdbDataset.getDefaultModel();
		} else {
			model = tdbDataset.getNamedModel(graphName);
		}
		tdbDataset.end();
		return model;
	}

	public static Model getModel(String graphName) {
		if (tdbDataset.isInTransaction()) {
			if (graphName == null) {
				return tdbDataset.getDefaultModel();
			} else {
				return tdbDataset.getNamedModel(graphName);
			}
		} else {
			return getFreshModel(graphName);
		}
	}

	public static String getTDBVersion() {
		TDB tdb = new TDB();
		String version = tdb.VERSION;
		return version;
	}

	public static boolean refreshTDB() {
		boolean success = false;
		tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = getModel(null);

		try {
			long size = tdbModel.size();
			System.out.println("Model on refresh has size: " + size);
			success = true;
		} catch (Exception e) {
			System.out.println("TDB transaction failed; see Exception: " + e);
			tdbDataset.abort();
			success = false;
		} finally {
			tdbDataset.end();
		}
		return success;
	}

	public static void addTriple(Resource s, Property p, Resource o) {
		s.addProperty(p, o);
	}

	/**
	 * "TURTLE" TURTLE
	 * "TTL" TURTLE
	 * "Turtle" TURTLE
	 * "N-TRIPLES" NTRIPLES
	 * "N-TRIPLE" NTRIPLES
	 * "NT" NTRIPLES
	 * "RDF/XML" RDFXML
	 * "N3" N3
	 * "JSON-LD" JSONLD
	 * "RDF/JSON" RDFJSON
	 *
	 * @param fileName = the file name (including the suffix) 
	 * @return a String representing the RDF type associate with this file type
	 */
	public static String getRDFTypeFromSuffix(String fileName) {
		if (fileName.matches(".*\\.rdf")) {
			return "RDF/XML";
		}
		if (fileName.matches(".*\\.nt")) {
			return "N-TRIPLES";
		}
		if (fileName.matches(".*\\.n3")) {
			return "N3";
		}
		if (fileName.matches(".*\\.ttl")) {
			return "TTL";
		}
		if (fileName.matches(".*\\.jsonld")) {
			return "JSON-LD";
		}
		if (fileName.matches(".*\\.json")) {
			return "JSON-LD";
		}

		return null;
	}

	public static void setModelPrefixMap(String exportgraphnameToUse) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = getModel(exportgraphnameToUse);
		try {
			tdbModel.setNsPrefixes(Prefixes.getPrefixmapping());
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("Prefix mapping sync from ActiveTDB failed with Exception: " + e);
			e.printStackTrace();
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		System.out.println("Mapping = " + tdbModel.getNsPrefixMap());
	}

	public static void tsReplaceObject(Resource subject, Property predicate, Resource newObject) {
		List<RDFNode> nodesToRemove;
		tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = getModel(null);
		NodeIterator nodeIterator = tdbModel.listObjectsOfProperty(subject, predicate);
		nodesToRemove = nodeIterator.toList();
		tdbDataset.end();
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		tdbModel = getModel(null);
		try {
			for (RDFNode node : nodesToRemove) {
				tdbModel.remove(subject, predicate, node);
			}
			tdbModel.add(subject, predicate, newObject);
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("tsReplaceObject from ActiveTDB failed with Exception: " + e);
			e.printStackTrace();
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public static void sync() {
		TDB.sync(tdbDataset);
	}

	/**
	 * This method is intended to return a String containing a properly formatted UUID String given either
	 * a) a URI resource (not blank node) whose URI terminates in a UUID (i.e. last 36 characters form one)
	 * b) a Literal whose string value is a UUID
	 * @param uuidNode - and RDFNode to test
	 * @return a String with a properly formatted UUID / ^[a-f\d]{8}-[a-f\d]{4}-[a-f\d]{4}-[a-f\d]{4}-[a-f\d]{12}$ /
	 */
	public static String getUUIDFromRDFNode(RDFNode uuidNode) {
		if (uuidNode == null) {
			return null;
		}
		if (uuidNode.isAnon()) {
			return null;
		}
		String uuidToReturn = null;
		if (uuidNode.isLiteral()) {
			Literal uuidLiteral = uuidNode.asLiteral();
			if (uuidLiteral.getDatatype().equals(XSDDatatype.XSDstring)) {
				uuidToReturn = uuidLiteral.getString();
			}
		} else {
			String uri = uuidNode.asResource().getURI();
			int start = uri.length() - 36;
			uuidToReturn = uri.substring(start);
		}
		if (uuidToReturn == null) {
			return null;
		}
		String lcUUIDToReturn = uuidToReturn.toLowerCase();
		if (lcUUIDToReturn.matches("[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}")) {
			return lcUUIDToReturn;
		}
		return null;
	}
}
