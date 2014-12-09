package gov.epa.nrmrl.std.lca.ht.tdb;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import gov.epa.nrmrl.std.lca.ht.dataModels.CurationMethods;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.FileMDKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.PersonKeeper;
import gov.epa.nrmrl.std.lca.ht.dialog.GenericMessageBox;
import gov.epa.nrmrl.std.lca.ht.utils.RDFUtil;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
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
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;

public class ActiveTDB implements IHandler, IActiveTDB {
	// public static Model tdbModel = null; // DO NOT ATTEMPT TO MANAGE A STATIC COPY OF THE DEFAULT MODEL!!!
	public static Dataset tdbDataset = null;
	// private static String tdbDir = null;
	public static GraphStore graphStore = null;
	private static ActiveTDB instance = null;

	// private List<IActiveTDBListener> activeTDBListeners = new
	// ArrayList<IActiveTDBListener>();
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB";

	private static final boolean noReadWrite = false;

	public ActiveTDB() {
		System.out.println("created ActiveTDB");
		setInstance(this);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (tdbDataset != null) {
			if (tdbDataset.getDefaultModel() != null) {
				// if (tdbDataset.getNamedModel("namedGraph") != null) {
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
		CurationMethods.createNewAnnotation();
	}

	private static void openTDB() {
		if (tdbDataset == null) {
			if ((Util.getPreferenceStore().getString("defaultTDB") == null)
					|| (Util.getPreferenceStore().getString("defaultTDB") == "")) {
				// TODO: Determine when this message might display and what the user and software shoul do about it.
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
				// TODO: Determine when this message might display and what the user and software shoul do about it.
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				StringBuilder b = new StringBuilder();
				b.append("Sorry, but the Default TDB set in your preferences is not an accessible directory. ");
				b.append("Please select an existing TDB directory, or an accessible directory for a new TDB.");
				new GenericMessageBox(shell, "Alert", b.toString());

				redirectToPreferences();
			} else {
				System.out.println("defaultTDBFile.list().length=" + defaultTDBFile.list().length);
				try {
					tdbDataset = TDBFactory.createDataset(defaultTDBFile.getPath());
					assert tdbDataset != null : "tdbDataset cannot be null";
					// tdbModel = ModelFactory.createDefaultModel();
					// tdbDataset.setDefaultModel(tdbModel);
					// tdbModel = tdbDataset.getDefaultModel();
					// tdbModel = tdbDataset.getNamedModel("namedGraph");
					graphStore = GraphStoreFactory.create(tdbDataset);
					System.out.println("TDB Successfully initiated!");
					// TODO: Write to the Logger whether the TDB is freshly created or has contents already. Also write
					// to the TDB that the session has started

				} catch (Exception e1) {
					System.out.println("Exception: " + e1);
					// TODO: Determine when this message might display and what the user and software shoul do about it.
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
			// graphStore.close();
			// TODO: FIGURE OUT WHY THE ABOVE CAUSES PROBLEMS OR IF IT WILL ALWAYS BE CLOSED (SO CAN LEAVE IT OUT)
			// MAYBE graphStore MUST BE CLOSED BEFORE tdbDataset
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void finalize() {
		System.out.println("closing tdbDataset and tdbModel");
		cleanUp();
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

	private static void replaceLiteral(Resource subject, Property predicate, RDFDatatype rdfDatatype,
			Object thingLiteral) {
		if (noReadWrite) {
			return;
		}
		Model tdbModel = null;
		if (tdbDataset.isInTransaction()) {
			tdbModel = tdbDataset.getDefaultModel();
		} else {
			tdbModel = getFreshModel();
		}
		Literal newRDFNode = tdbModel.createTypedLiteral(thingLiteral, rdfDatatype);
		removeAllLikeLiterals(subject, predicate, thingLiteral);
		tdbModel.add(subject, predicate, newRDFNode);
	}

	public static void tsReplaceLiteral(Resource subject, Property predicate, RDFDatatype rdfDatatype,
			Object thingLiteral) {
		if (tdbDataset.isInTransaction()) {
			System.out.println("!!!!!!!!!!!!!!Transaction in transaction");
			System.out.println(new Object() {
			}.getClass().getEnclosingMethod().getName());
		}
		if (rdfDatatype == null) {
			System.out.println("====> Something wrong: trying to write with undefined rdfDatatype");
			return;
		}
		if (noReadWrite) {
			return;
		}
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = tdbDataset.getDefaultModel();
		try {
			// Model tdbModel = tdbDataset.getDefaultModel();
			Literal newRDFNode = tdbModel.createTypedLiteral(thingLiteral, rdfDatatype);
			NodeIterator nodeIterator = tdbModel.listObjectsOfProperty(subject, predicate);
			while (nodeIterator.hasNext()) {
				RDFNode rdfNode = nodeIterator.next();
				if (rdfNode.isLiteral()) {
					// if (rdfNode.asLiteral().getDatatype().equals(rdfDatatype)) {
					// ONLY REMOVING SAME TYPE
					tdbModel.removeAll(subject, predicate, rdfNode);
					// }
				}
			}
			tdbModel.add(subject, predicate, newRDFNode);
			tdbDataset.commit();
			// sync();
		} catch (Exception e) {
			System.out.println("01 TDB transaction failed; see Exception: " + e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public static void replaceLiteral(Resource subject, Property predicate, Object thingLiteral) {
		if (noReadWrite) {
			return;
		}
		RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(thingLiteral);
		replaceLiteral(subject, predicate, rdfDatatype, thingLiteral);
	}

	public static void tsReplaceLiteral(Resource subject, Property predicate, Object thingLiteral) {
		if (tdbDataset.isInTransaction()) {
			System.out.println("!!!!!!!!!!!!!!Transaction in transaction");
			System.out.println(new Object() {
			}.getClass().getEnclosingMethod().getName());
		}
		if (noReadWrite) {
			return;
		}
		RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(thingLiteral);
		tsReplaceLiteral(subject, predicate, rdfDatatype, thingLiteral);
	}

	private static void replaceResource(Resource subject, Property predicate, Resource object) {
		if (noReadWrite) {
			return;
		}
		subject.removeAll(predicate);
		subject.addProperty(predicate, object);
	}

	public static void tsReplaceResource(Resource subject, Property predicate, Resource object) {
		if (tdbDataset.isInTransaction()) {
			System.out.println("!!!!!!!!!!!!!!Transaction in transaction");
			System.out.println(new Object() {
			}.getClass().getEnclosingMethod().getName());
		}
		if (noReadWrite) {
			return;
		}
		if (object == null) {
			tsRemoveAllObjects(subject, predicate);
			return;
		}
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = tdbDataset.getDefaultModel();
		try {
			// Model tdbModel = tdbDataset.getDefaultModel();
			NodeIterator nodeIterator = tdbModel.listObjectsOfProperty(subject, predicate);
			while (nodeIterator.hasNext()) {
				RDFNode rdfNode = nodeIterator.next();
				if (object.isLiteral() && rdfNode.isLiteral()) {
					RDFDatatype rdfNodeType = rdfNode.asLiteral().getDatatype();
					RDFDatatype objectType = object.asLiteral().getDatatype();
					if (rdfNodeType.equals(objectType)) {
						tdbModel.remove(subject, predicate, rdfNode);
					}
				} else {
					removeAllObjects(subject, predicate);
				}
			}
			tdbModel.add(subject, predicate, object);

			// tdbModel.add(subject, predicate, object);
			// Resource tr_subject;
			// if (subject.isAnon()){
			// AnonId anonId = subject.getId();
			// tr_subject = tdbModel.createResource(anonId);
			// } else {
			// String uri = subject.getURI();
			// tr_subject = tdbModel.getResource(uri);
			// }
			// // Resource tr_subject = tdbModel.createResource(uri);
			// // NOTE: Resource subject not valid for changing within a
			// // transaction
			// // subject.removeAll(predicate);
			// // subject.addProperty(predicate, object);
			// tr_subject.removeAll(predicate);
			// tr_subject.addProperty(predicate, object);
			tdbDataset.commit();
			// sync();
		} catch (Exception e) {
			System.out.println("02 TDB transaction failed; see Exception: " + e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	private static Resource createResource(Resource rdfclass) {
		Model tdbModel = null;
		if (tdbDataset.isInTransaction()) {
			tdbModel = tdbDataset.getDefaultModel();
		} else {
			tdbModel = getFreshModel();
		}
		Resource result = tdbModel.createResource(rdfclass);
		return result;
	}

	// public static void flush() {
	// garbageIn();
	// garbageOut();
	// }

	// public static void garbageIn() {
	// for (int i = 0; i < 1000; i++) {
	// tsAddLiteral(FedLCA.deleteMe, FedLCA.hasValue, i);
	// }
	// }
	//
	// public static void garbageOut() {
	// int count = 0;
	// Model tdbModel = tdbDataset.getDefaultModel();
	// while (tdbModel.contains(FedLCA.deleteMe, FedLCA.hasValue)) {
	// tsRemoveAllObjects(FedLCA.deleteMe, FedLCA.hasValue);
	// count++;
	// if (count > 100) {
	// System.out.println("count: " + count);
	// System.out.println("Delete garbage failed.");
	// return;
	// }
	// }
	// System.out.println("Delete garbage required " + count + " tries.");
	// }

	public static Resource tsCreateResource(Resource rdfclass) {
		if (tdbDataset.isInTransaction()) {
			System.out.println("!!!!!!!!!!!!!!Transaction in transaction");
			System.out.println(new Object() {
			}.getClass().getEnclosingMethod().getName());
		}
		// FIXME: resources only valid within a transaction??
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		Resource result = null;
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = tdbDataset.getDefaultModel();
		try {
			// Model tdbModel = tdbDataset.getDefaultModel();
			result = tdbModel.createResource(rdfclass);
			tdbDataset.commit();
			// sync();
		} catch (Exception e) {
			System.out.println("03 TDB transaction failed; see Exception: " + e);
			tdbModel.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		return result;
	}

	private static void addLiteral(Resource subject, Property predicate, RDFDatatype rdfDatatype, Object thingLiteral) {
		if (noReadWrite) {
			return;
		}
		Model tdbModel = null;
		if (tdbDataset.isInTransaction()) {
			tdbModel = tdbDataset.getDefaultModel();
		} else {
			tdbModel = getFreshModel();
		}
		Literal newRDFNode = tdbModel.createTypedLiteral(thingLiteral, rdfDatatype);
		tdbModel.add(subject, predicate, newRDFNode);
	}

	public static void tsAddLiteral(Resource subject, Property predicate, RDFDatatype rdfDatatype, Object thingLiteral) {
		if (tdbDataset.isInTransaction()) {
			System.out.println("!!!!!!!!!!!!!!Transaction in transaction");
			System.out.println(new Object() {
			}.getClass().getEnclosingMethod().getName());
		}
		if (noReadWrite) {
			return;
		}
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = tdbDataset.getDefaultModel();
		try {
			// Model tdbModel = tdbDataset.getDefaultModel();
			Literal newRDFNode = tdbModel.createTypedLiteral(thingLiteral, rdfDatatype);
			tdbModel.add(subject, predicate, newRDFNode);
			tdbDataset.commit();
			// sync();
		} catch (Exception e) {
			System.out.println("04 TDB transaction failed; see Exception: " + e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public static void addLiteral(Resource subject, Property predicate, Object thingLiteral) {
		if (noReadWrite) {
			return;
		}
		RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(thingLiteral);
		addLiteral(subject, predicate, rdfDatatype, thingLiteral);
	}

	public static void tsAddLiteral(Resource subject, Property predicate, Object thingLiteral) {
		if (tdbDataset.isInTransaction()) {
			System.out.println("!!!!!!!!!!!!!!Transaction in transaction");
			System.out.println(new Object() {
			}.getClass().getEnclosingMethod().getName());
		}
		if (noReadWrite) {
			return;
		}
		RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(thingLiteral);
		tsAddLiteral(subject, predicate, rdfDatatype, thingLiteral);
	}

	public static void tsAddTriple(Resource subject, Property predicate, Resource object) {
		if (tdbDataset.isInTransaction()) {
			System.out.println("!!!!!!!!!!!!!!Transaction in transaction");
			System.out.println(new Object() {
			}.getClass().getEnclosingMethod().getName());
		}
		if (noReadWrite) {
			return;
		}
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = tdbDataset.getDefaultModel();
		try {
			// Model tdbModel = tdbDataset.getDefaultModel();
			tdbModel.add(subject, predicate, object);
			tdbDataset.commit();
			// sync();
		} catch (Exception e) {
			System.out.println("05 TDB transaction failed; see Exception: " + e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	private static Literal createTypedLiteral(Object thingLiteral) {
		RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(thingLiteral);
		Model tdbModel = null;
		if (tdbDataset.isInTransaction()) {
			tdbModel = tdbDataset.getDefaultModel();
		} else {
			tdbModel = getFreshModel();
		}
		return tdbModel.createTypedLiteral(thingLiteral, rdfDatatype);
	}

	public static Literal tsCreateTypedLiteral(Object thingLiteral) {
		if (tdbDataset.isInTransaction()) {
			System.out.println("!!!!!!!!!!!!!!Transaction in transaction");
			System.out.println(new Object() {
			}.getClass().getEnclosingMethod().getName());
		}
		RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(thingLiteral);
		Literal literal = null;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = tdbDataset.getDefaultModel();
		try {
			// Model tdbModel = tdbDataset.getDefaultModel();
			literal = tdbModel.createTypedLiteral(thingLiteral, rdfDatatype);
			tdbDataset.commit();
			// sync();
		} catch (Exception e) {
			System.out.println("06 TDB transaction failed; see Exception: " + e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		return literal;
	}

	private static void removeAllObjects(Resource subject, Property predicate) {
		if (noReadWrite) {
			return;
		}
		subject.removeAll(predicate);
	}

	public static void tsRemoveAllObjects(Resource subject, Property predicate) {
		if (tdbDataset.isInTransaction()) {
			System.out.println("!!!!!!!!!!!!!!Transaction in transaction");
			System.out.println(new Object() {
			}.getClass().getEnclosingMethod().getName());
		}
		if (noReadWrite) {
			return;
		}
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = tdbDataset.getDefaultModel();
		try {
			// Model tdbModel = tdbDataset.getDefaultModel();
			NodeIterator nodeIterator = tdbModel.listObjectsOfProperty(subject, predicate);
			while (nodeIterator.hasNext()) {
				RDFNode rdfNode = nodeIterator.next();
				tdbModel.remove(subject, predicate, rdfNode);
			}
			// Resource tr_subject;
			// if (subject.isAnon()){
			// AnonId anonId = subject.getId();
			// tr_subject = tdbModel.createResource(anonId);
			// } else {
			// String uri = subject.getURI();
			// tr_subject = tdbModel.getResource(uri);
			// }
			// // subject.removeAll(predicate);
			// tr_subject.removeAll(predicate);
			tdbDataset.commit();
			// sync();
		} catch (Exception e) {
			System.out.println("07 TDB transaction failed; see Exception: " + e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public static void tsRemoveAllObjects(Resource comparison) {
		if (tdbDataset.isInTransaction()) {
			System.out.println("!!!!!!!!!!!!!!Transaction in transaction");
			System.out.println(new Object() {
			}.getClass().getEnclosingMethod().getName());
		}
		if (noReadWrite) {
			return;
		}
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = tdbDataset.getDefaultModel();
		try {
			// Model tdbModel = tdbDataset.getDefaultModel();
			StmtIterator stmtIterator = comparison.listProperties();
			while (stmtIterator.hasNext()) {
				Statement statement = stmtIterator.nextStatement();
				tdbModel.remove(statement);
			}
			tdbDataset.commit();
			// sync();
		} catch (Exception e) {
			System.out.println("08 TDB transaction failed; see Exception: " + e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---

	}

	private static void removeAllLikeLiterals(Resource subject, Property predicate, Object thingLiteral) {
		if (noReadWrite) {
			return;
		}
		RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(thingLiteral);
		Model tdbModel = null;
		if (tdbDataset.isInTransaction()) {
			tdbModel = tdbDataset.getDefaultModel();
		} else {
			tdbModel = getFreshModel();
		}
		NodeIterator nodeIterator = tdbModel.listObjectsOfProperty(subject, predicate);
		while (nodeIterator.hasNext()) {
			RDFNode rdfNode = nodeIterator.next();
			if (rdfNode.isLiteral()) {
				if (rdfNode.asLiteral().getDatatype().equals(rdfDatatype)) {
					// ONLY REMOVING SAME TYPE
					tdbModel.removeAll(subject, predicate, rdfNode);
				}
			}
		}
	}

	public static void tsRemoveAllLikeLiterals(Resource subject, Property predicate, Object object) {
		if (tdbDataset.isInTransaction()) {
			System.out.println("!!!!!!!!!!!!!!Transaction in transaction");
			System.out.println(new Object() {
			}.getClass().getEnclosingMethod().getName());
		}
		if (noReadWrite) {
			return;
		}
		RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(object);
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = tdbDataset.getDefaultModel();
		try {
			// Model tdbModel = tdbDataset.getDefaultModel();
			NodeIterator nodeIterator = tdbModel.listObjectsOfProperty(subject, predicate);
			while (nodeIterator.hasNext()) {
				RDFNode rdfNode = nodeIterator.next();
				if (rdfNode.isLiteral()) {
					if (rdfNode.asLiteral().getDatatype().equals(rdfDatatype)) {
						// ONLY REMOVING SAME TYPE
						tdbModel.removeAll(subject, predicate, rdfNode);
					}
				}
			}
			tdbDataset.commit();
			// sync();
		} catch (Exception e) {
			System.out.println("09 TDB transaction failed; see Exception: " + e);
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	private static void removeStatement(Resource subject, Property predicate, RDFNode object) {
		if (noReadWrite) {
			return;
		}
		Model tdbModel = null;
		if (tdbDataset.isInTransaction()) {
			tdbModel = tdbDataset.getDefaultModel();
		} else {
			tdbModel = getFreshModel();
		}
		tdbModel.remove(subject, predicate, object);
	}

	public static void tsRemoveStatement(Resource subject, Property predicate, RDFNode object) {
		if (tdbDataset.isInTransaction()) {
			System.out.println("!!!!!!!!!!!!!!Transaction in transaction");
			System.out.println(new Object() {
			}.getClass().getEnclosingMethod().getName());
		}
		if (noReadWrite) {
			return;
		}
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = tdbDataset.getDefaultModel();
		try {
			// Model tdbModel = tdbDataset.getDefaultModel();
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
			e.printStackTrace();
			return null;
		}
		return resultingDate;
	}

	public static Model getFreshModel() {
		tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = tdbDataset.getDefaultModel();
		tdbDataset.end();
		return tdbModel;
		// return tdbDataset.getNamedModel("namedGraph");
	}

	public static Model getModel() {
		if (tdbDataset.isInTransaction()) {
			return tdbDataset.getDefaultModel();
		} else {
			return getFreshModel();
		}
		// sync();
	}

	public static int countAllData() {
		int result = -1;
		// // --- BEGIN SAFE -WRITE- TRANSACTION ---
		// tdbDataset.begin(ReadWrite.READ);
		// try {
		// Model union = tdbDataset.getNamedModel("urn:x-arq:UnionGraph");
		// StmtIterator stmtIterator = union.listStatements();
		// while (stmtIterator.hasNext()){
		// result++;
		// }
		// // result = union.listStatements().toList().size();
		// // tdbDataset.commit();
		// } catch (Exception e) {
		// System.out.println("TDB transaction failed; see Exception: " + e);
		// tdbDataset.abort();
		// } finally {
		// tdbDataset.end();
		// }
		return result;

	}

	public static void sync() {
		TDB.sync(tdbDataset);
	}

	public static String getTDBVersion() {
		TDB thing = new TDB();
		String version = thing.VERSION;
		return version;
	}

	public static boolean refreshTDB() {
		boolean success = false;
		tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = tdbDataset.getDefaultModel();

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
}
