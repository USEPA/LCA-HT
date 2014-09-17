package gov.epa.nrmrl.std.lca.ht.tdb;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;

public class ActiveTDB implements IHandler, IActiveTDB {
	public static Model tdbModel = null;
	public static Dataset tdbDataset = null;
	public static String tdbDir = null;
	public static GraphStore graphStore = null;
	private static ActiveTDB instance = null;

	// private List<IActiveTDBListener> activeTDBListeners = new
	// ArrayList<IActiveTDBListener>();
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB";

	public ActiveTDB() {
		System.out.println("created ActiveTDB");
		setInstance(this);
	}

	// public static ActiveTDB getInstance() {
	// return instance;
	// }

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (tdbModel != null) {
			System.out.println("tdb seems to be open already!");
			return null;
		}
		// System.out.println("about to open TDB. Model right now is: " +
		// tdbModel);
		// IT IS A BAD IDEA TO PRINT THE MODEL [ALL DATA!] TO System.out !!!
		openTDB();
		// System.out.println("Now Model is: " + tdbModel);
		try {
			syncTDBtoLCAHT();
		} catch (Exception e) {
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
	}

	private static void openTDB() {
		if (tdbModel == null) {
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
					tdbDataset = TDBFactory.createDataset(defaultTDBFile.getPath());
					assert tdbDataset != null : "tdbDataset cannot be null";
					tdbModel = tdbDataset.getDefaultModel();
					graphStore = GraphStoreFactory.create(tdbDataset);
					System.out.println("TDB Successfully initiated!");
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
		// try {
		// tdbModel.close();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		try {
			graphStore.close();
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

	private static void replaceLiteral(Resource subject, Property predicate, RDFDatatype rdfDatatype, Object thingLiteral) {
		Literal newRDFNode = tdbModel.createTypedLiteral(thingLiteral, rdfDatatype);
		removeAllLikeLiterals(subject,predicate,thingLiteral);
		tdbModel.add(subject, predicate, newRDFNode);
	}

	public static void tsReplaceLiteral(Resource subject, Property predicate, RDFDatatype rdfDatatype,
			Object thingLiteral) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		try {
			Model tdbModel = tdbDataset.getDefaultModel();
			Literal newRDFNode = tdbModel.createTypedLiteral(thingLiteral, rdfDatatype);
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
			tdbModel.add(subject, predicate, newRDFNode);
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("TDB transaction failed; see strack trace!");
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	private static void replaceLiteral(Resource subject, Property predicate, Object thingLiteral) {
		RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(thingLiteral);
		replaceLiteral(subject, predicate, rdfDatatype, thingLiteral);
	}

	public static void tsReplaceLiteral(Resource subject, Property predicate, Object thingLiteral) {
		RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(thingLiteral);
		tsReplaceLiteral(subject, predicate, rdfDatatype, thingLiteral);
	}

	private static void replaceResource(Resource subject, Property predicate, Resource object) {
		subject.removeAll(predicate);
		subject.addProperty(predicate, object);
	}

	public static void tsReplaceResource(Resource subject, Property predicate, Resource object) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		try {
			Model tdbModel = tdbDataset.getDefaultModel();
			String uri = subject.getURI();
			Resource tr_subject = tdbModel.getResource(uri);
			// Resource tr_subject = tdbModel.createResource(uri);
			// NOTE: Resource subject not valid for changing within a
			// transaction
			subject.removeAll(predicate);
			subject.addProperty(predicate, object);
//			tr_subject.removeAll(predicate);
//			tr_subject.addProperty(predicate, object);
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("TDB transaction failed; see strack trace!");
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	private static Resource createResource(Resource rdfclass) {
		Resource result = tdbModel.createResource(rdfclass);
		return result;
	}

	public static Resource tsCreateResource(Resource rdfclass) {
		// FIXME: resources only valid within a transaction??
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		Resource result = null;
		tdbDataset.begin(ReadWrite.WRITE);
		try {
			Model tdbModel = tdbDataset.getDefaultModel();
			result = tdbModel.createResource(rdfclass);
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("TDB transaction failed; see strack trace!");
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		return result;
	}

	private static void addLiteral(Resource subject, Property predicate, RDFDatatype rdfDatatype, Object thingLiteral) {
		Literal newRDFNode = tdbModel.createTypedLiteral(thingLiteral, rdfDatatype);
		tdbModel.add(subject, predicate, newRDFNode);
	}

	public static void tsAddLiteral(Resource subject, Property predicate, RDFDatatype rdfDatatype, Object thingLiteral) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		try {
			Model tdbModel = tdbDataset.getDefaultModel();
			Literal newRDFNode = tdbModel.createTypedLiteral(thingLiteral, rdfDatatype);
			tdbModel.add(subject, predicate, newRDFNode);
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("TDB transaction failed; see strack trace!");
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	private static void addLiteral(Resource subject, Property predicate, Object thingLiteral) {
		RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(thingLiteral);
		addLiteral(subject, predicate, rdfDatatype, thingLiteral);
	}

	public static void tsAddLiteral(Resource subject, Property predicate, Object thingLiteral) {
		RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(thingLiteral);
		tsAddLiteral(subject, predicate, rdfDatatype, thingLiteral);
	}

	public static void tsAddTriple(Resource subject, Property predicate, Resource object) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		try {
			Model tdbModel = tdbDataset.getDefaultModel();
			tdbModel.add(subject, predicate, object);
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("TDB transaction failed; see strack trace!");
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}
	
	private static Literal createTypedLiteral(Object thingLiteral) {
		RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(thingLiteral);
		return tdbModel.createTypedLiteral(thingLiteral, rdfDatatype);
	}

	public static Literal tsCreateTypedLiteral(Object thingLiteral) {
		RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(thingLiteral);
		Literal literal = null;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		try {
			Model tdbModel = tdbDataset.getDefaultModel();
			literal = tdbModel.createTypedLiteral(thingLiteral, rdfDatatype);
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("TDB transaction failed; see strack trace!");
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		return literal;
	}


	private static void removeAllObjects(Resource subject, Property predicate) {
		subject.removeAll(predicate);
	}
	
	public static void tsRemoveAllObjects(Resource subject, Property predicate) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		try {
			Model tdbModel = tdbDataset.getDefaultModel();
			String uri = subject.getURI();
			Resource tr_subject = tdbModel.getResource(uri);
			subject.removeAll(predicate);
//			tr_subject.removeAll(predicate);

			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("TDB transaction failed; see strack trace!");
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	private static void removeAllLikeLiterals(Resource subject, Property predicate, Object thingLiteral) {
		RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(thingLiteral);
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
		RDFDatatype rdfDatatype = RDFUtil.getRDFDatatypeFromJavaClass(object);
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		try {
			Model tdbModel = tdbDataset.getDefaultModel();
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
		} catch (Exception e) {
			System.out.println("TDB transaction failed; see strack trace!");
			tdbDataset.abort();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}
	
	private static void removeStatement(Resource subject, Property predicate, RDFNode object) {
		tdbModel.remove(subject, predicate, object);
	}

	
	public static void tsRemoveStatement(Resource subject, Property predicate, RDFNode object) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		try {
			Model tdbModel = tdbDataset.getDefaultModel();
			tdbModel.remove(subject, predicate, object);
			tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("TDB transaction failed; see strack trace!");
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

}
