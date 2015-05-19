package gov.epa.nrmrl.std.lca.ht.tdb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gov.epa.nrmrl.std.lca.ht.curation.CurationMethods;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.FileMDKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.PersonKeeper;
import gov.epa.nrmrl.std.lca.ht.dialog.GenericMessageBox;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.utils.RDFUtil;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;

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

import com.hp.hpl.jena.datatypes.RDFDatatype;
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

public class ActiveTDB implements IHandler, IActiveTDB {
	// public static Model tdbModel = null; // DO NOT ATTEMPT TO MANAGE A STATIC COPY OF THE DEFAULT MODEL!!!
	public static Dataset tdbDataset = null;
	// private static String tdbDir = null;
	public static GraphStore graphStore = null;
	private static ActiveTDB instance = null;

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
		if (tdbDataset != null) {
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
			Exception e2 = new ExecutionException("***********THE TDB MAY BE BAD*******************");
			e2.printStackTrace();
			System.exit(1);
		}

		return null;
	}

	public static void syncTDBtoLCAHT() {
		System.out.println("Syncing people");
		PersonKeeper.syncFromTDB();
		System.out.println("Syncing files");
		FileMDKeeper.syncFromTDB();
		System.out.println("Syncing data sources");
		DataSourceKeeper.syncFromTDB();
		System.out.println("Done syncing");
		CurationMethods.updateAnnotationModifiedDate();
	}

	private static void openTDB() {
		if (tdbDataset == null) {
			String activeTDB = Util.getPreferenceStore().getString("activeTDB");
			if (Util.EMPTY_STRING.equals(activeTDB)) {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				StringBuilder b = new StringBuilder();
				b.append("The Harmonization Tool (HT) requires the user to specify directories for local storage.  ");
				b.append("Please pick existing directories, or an empty ones where the HT can store data.");
				new GenericMessageBox(shell, "Welcome!", b.toString());
				redirectToPreferences();
			}

			String defaultTDB = null;
			File defaultTDBFile = null;
			String errMsg = null;
			boolean tdbCreated = false;
			while (!tdbCreated) {
				defaultTDB = Util.getPreferenceStore().getString("defaultTDB");
				defaultTDBFile = new File(defaultTDB);

				if (defaultTDBFile.isDirectory()) {		
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
						// TODO: Write to the Logger whether the TDB is freshly created or has contents already. Also write
						// to the TDB that the session has started
						// Prefixes.syncPrefixMapToTDBModel();
					} catch (Exception e1) {
						System.out.println("Exception: " + e1);
						if (!prefsCanceled) {
							// TODO: Determine when this message might display and what the user and software shoul do about it.
							StringBuilder b = new StringBuilder();
							b.append("It appears that the HT can not create a TDB in the default directory. ");
							b.append("You may need to create a new TDB.");
							errMsg = b.toString();
						}
					}
				}
				else {
					StringBuilder b = new StringBuilder();
					b.append("Sorry, but the Default TDB set in your preferences is not an accessible directory. ");
					b.append("Please select an existing TDB directory, or an accessible directory for a new TDB.");
					errMsg = b.toString();
				}
				if (!tdbCreated) {
					// ask user for TDB directory
					// TODO: Determine when this message might display and what the user and software shoul do about it.
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					new GenericMessageBox(shell, "Error", errMsg);
					//If user has previously canceled with invalid data, quit.
					if (prefsCanceled) {
						errMsg = "The selected directory is not accessible - exiting now.";
						new GenericMessageBox(shell, "Error", errMsg);
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

	public static void copyDatasetContentsToExportGraph(String datasetName) {

		// --- BEGIN SAFE -READ- TRANSACTION ---
		tdbDataset.begin(ReadWrite.READ);
		Model defaultModel = tdbDataset.getDefaultModel();
		Model exportModel = tdbDataset.getNamedModel(exportGraphName);
		Model unionModel = ModelFactory.createUnion(defaultModel, exportModel);
		
		System.out.println("defaultModel: " + defaultModel.size());
		System.out.println("exportModel: " + exportModel.size());
		System.out.println("unionModel: " + unionModel.size());
		tdbDataset.end();
		// ---- END SAFE -READ- TRANSACTION ---

		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		try {
			StringBuilder b = new StringBuilder();
			b.append(Prefixes.getPrefixesForQuery());
			b.append("insert {graph <" + exportGraphName + "> {?s ?p ?o . ?c ?p1 ?o1 . ?m ?p2 ?o2 .}} \n");
			b.append("  where {\n");
			b.append("    ?s ?p ?o .  \n");
			b.append("    {{  \n");
			b.append("      ?s eco:hasDataSource ?ds .  \n");
			b.append("      ?ds rdfs:label \"" + datasetName + "\"^^xsd:string .  \n");
			b.append("      OPTIONAL {  \n");
			b.append("        ?c fedlca:comparedSource ?s . \n");
			b.append("        ?c fedlca:comparedMaster ?m . \n");
			b.append("        ?c ?p1 ?o1 .  \n");
			b.append("        ?m ?p2 ?o2 .  \n");
			b.append("      }  \n");
			b.append("    } UNION  \n");
			b.append("    {  \n");
			b.append("      ?s a eco:DataSource .  \n");
			b.append("      ?s rdfs:label \"" + datasetName + "\"^^xsd:string .  \n");
			b.append("    }}  \n");
			b.append("  }\n");
			String query = b.toString();
			System.out.println("\n" + query + "\n");
			UpdateRequest request = UpdateFactory.create(query);
			UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore);
			proc.execute();
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("copyDatasetContentsToExportGraph(String datasetName) failed; see Exception: " + e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---

		tdbDataset.begin(ReadWrite.READ);
		defaultModel = getModel(null);
		exportModel = getModel(exportGraphName);
		unionModel = ModelFactory.createUnion(defaultModel, exportModel);
		System.out.println("defaultModel: " + defaultModel.size());
		System.out.println("exportModel: " + exportModel.size());
		System.out.println("unionModel: " + unionModel.size());
		tdbDataset.end();
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
				}
				if (rdfDatatypeOfNode.equals(rdfDatatype)) {
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
			System.out.println("08 TDB transaction failed; see Exception: " + e);
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

	public static void tsAddGeneralTriple(Resource subject, Property predicate, Object thingLiteral, String graphName) {
		if (thingLiteral == null) {
			return;
		}
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = getModel(graphName);
		try {
			if (thingLiteral instanceof RDFNode) {
				tdbModel.add(subject, predicate, (RDFNode) thingLiteral);

			} else {
				RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(thingLiteral);
				Literal newRDFNode = tdbModel.createTypedLiteral(thingLiteral, rdfDatatype);
				tdbModel.add(subject, predicate, newRDFNode);
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
		if (subject == null){
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

	public static Literal tsCreateTypedLiteral(Object thingLiteral, String graphName) {
		RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(thingLiteral);
		Literal literal = null;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = getModel(graphName);
		try {
			literal = tdbModel.createTypedLiteral(thingLiteral, rdfDatatype);
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

	public static String getRDFTypeFromSuffix(String fileName) {
		String inputType = null;
		if (fileName.matches(".*\\.rdf")) {
			inputType = "RDF/XML";
		} else if (fileName.matches(".*\\.nt")) {
			inputType = "N-TRIPLES";
		} else if (fileName.matches(".*\\.n3")) {
			inputType = "N3";
		} else if (fileName.matches(".*\\.ttl")) {
			inputType = "TTL";
		} else if (fileName.matches(".*\\.jsonld")) {
			inputType = "JSON-LD";
		} else if (fileName.matches(".*\\.json")) {
			inputType = "JSON-LD";
		}
		/*
		 * /* Jena reader RIOT Lang
		 */

		/* "TURTLE" TURTLE */
		/* "TTL" TURTLE */
		/* "Turtle" TURTLE */
		/* "N-TRIPLES" NTRIPLES */
		/* "N-TRIPLE" NTRIPLES */
		/* "NT" NTRIPLES */
		/* "RDF/XML" RDFXML */
		/* "N3" N3 */
		/* "JSON-LD" JSONLD */
		/* "RDF/JSON" RDFJSON */

		return inputType;
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
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}
	
	public static void sync(){
		TDB.sync(tdbDataset);
	}
}
