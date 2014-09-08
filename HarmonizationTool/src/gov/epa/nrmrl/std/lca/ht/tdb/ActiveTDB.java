package gov.epa.nrmrl.std.lca.ht.tdb;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.FileMDKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.PersonKeeper;
import gov.epa.nrmrl.std.lca.ht.dialog.GenericMessageBox;
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
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
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
//		System.out.println("about to open TDB. Model right now is: " + tdbModel);
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
		while (tdbModel == null) {
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

	// public static int removeAllWithSubject(Resource subject) {
	// int count = 0;
	// StmtIterator stmtIterator = subject.listProperties();
	// Set<Statement> statementSet = stmtIterator.toSet();
	// // System.out.println("statementSet.size(): " + statementSet.size());
	// for (Statement statement : statementSet) {
	// // System.out.println("Statement: " + statement);
	// statement.remove();
	// count++;
	// }
	// count++;
	// return count;
	// }

	// public static int removeAllWithObject(RDFNode object) {
	// int count = 0;
	// List<Property> properties = getAllProperties();
	// for (Property predicate : properties) {
	// count += removeAllWithPredicateObject(predicate, object);
	// }
	// return count;
	// }

	// public static int removeAllWithSubjectPredicate(Resource subject,
	// Property predicate) {
	// int count = 0;
	// List<Statement> statements = subject.listProperties(predicate).toList();
	// for (Statement statement : statements) {
	// statement.remove();
	// count++;
	// }
	// return count;
	// }

	// public static int removeAllWithPredicateObject(Property predicate,
	// RDFNode object) {
	// int count = 0;
	// ResIterator resIterator = tdbModel.listSubjectsWithProperty(predicate,
	// object);
	// while (resIterator.hasNext()) {
	// Resource subject = resIterator.next();
	// tdbModel.remove(subject, predicate, object);
	// count++;
	// }
	// return count;
	// }

	// public static List<Property> getAllProperties() {
	// List<Property> results = new ArrayList<Property>();
	// QGetAllProperties qGetAllProperties = new QGetAllProperties();
	// ResultSet resultSet = qGetAllProperties.getResultSet();
	// List<String> resultVars = resultSet.getResultVars();
	// String p = resultVars.get(0);
	// while (resultSet.hasNext()) {
	// QuerySolution querySolution = resultSet.nextSolution();
	// Resource predAsResource = querySolution.getResource(p);
	// String predAsString = predAsResource.getURI();
	// Property predicate = tdbModel.createProperty(predAsString);
	// results.add(predicate);
	// }
	// return results;
	// }

	public static void replaceLiteral(Resource subject, Property predicate, RDFDatatype rdfDatatype, Object thingLiteral) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		try {
			Literal newRDFNode = tdbModel.createTypedLiteral(thingLiteral, rdfDatatype);
			NodeIterator nodeIterator = tdbModel.listObjectsOfProperty(subject, predicate);
			while (nodeIterator.hasNext()) {
				RDFNode rdfNode = nodeIterator.next();
				if (rdfNode.isLiteral()) {
					if (rdfNode.asLiteral().getDatatype().equals(rdfDatatype)) {
						tdbModel.removeAll(subject, predicate, rdfNode);
					}
				}
			}
			tdbModel.add(subject, predicate, newRDFNode);
			tdbDataset.commit();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public static void replaceLiteral(Resource subject, Property predicate, Object thingLiteral) {
		removeAllObjects(subject, predicate);
		if (thingLiteral == null) {
			return;
		}
		RDFDatatype rdfDatatype = getRDFDatatypeFromJavaClass(thingLiteral);
		System.out.println("rdfDatatype = "+rdfDatatype);
		if (rdfDatatype.equals(XSDDatatype.XSDstring) && thingLiteral.equals("")) {
			return;
		}
		replaceLiteral(subject, predicate, rdfDatatype, thingLiteral);
	}

	public static void replaceResource(Resource subject, Property predicate, Resource object) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		try {
			subject.removeAll(predicate);
			subject.addProperty(predicate, object);
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public static Resource createResource(Resource rdfclass) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		Resource result = null;
		tdbDataset.begin(ReadWrite.WRITE);
		try {
			result = tdbModel.createResource(rdfclass);
			tdbDataset.commit();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		return result;
	}

	public static void addLiteral(Resource subject, Property predicate, RDFDatatype rdfDatatype, Object thingLiteral) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		try {
			Literal newRDFNode = tdbModel.createTypedLiteral(thingLiteral, rdfDatatype);
			tdbModel.add(subject, predicate, newRDFNode);
			tdbDataset.commit();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public static void addLiteral(Resource subject, Property predicate, Object thingLiteral) {
		RDFDatatype rdfDatatype = getRDFDatatypeFromJavaClass(thingLiteral);
		addLiteral(subject, predicate, rdfDatatype, thingLiteral);
	}

	public static Literal createTypedLiteral(Object thingLiteral) {
		RDFDatatype rdfDatatype = getRDFDatatypeFromJavaClass(thingLiteral);
		return tdbModel.createTypedLiteral(thingLiteral, rdfDatatype);
	}

	public static void removeAllObjects(Resource subject, Property predicate) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		try {
			subject.removeAll(predicate);
			tdbDataset.commit();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public static void removeAllLikeObjects(Resource subject, Property predicate, RDFNode object) {
		// FIXME
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		try {
			subject.removeAll(predicate);
			tdbDataset.commit();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public static void removeStatement(Resource subject, Property predicate, RDFNode object) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		tdbDataset.begin(ReadWrite.WRITE);
		try {
			tdbModel.remove(subject, predicate, object);
			tdbDataset.commit();
		} finally {
			tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public static RDFDatatype getRDFDatatypeFromJavaClass(Object object) {
		if (object instanceof Float) {
			return XSDDatatype.XSDfloat;
		}
		if (object instanceof Double) {
			return XSDDatatype.XSDdouble;
		}
		if (object instanceof Integer) {
			return XSDDatatype.XSDint;
		}
		if (object instanceof Long) {
			return XSDDatatype.XSDlong;
		}
		if (object instanceof Short) {
			return XSDDatatype.XSDshort;
		}
		if (object instanceof Byte) {
			return XSDDatatype.XSDbyte;
		}
		if (object instanceof BigInteger) {
			return XSDDatatype.XSDinteger;
		}
		if (object instanceof BigDecimal) {
			return XSDDatatype.XSDdecimal;
		}
		if (object instanceof Boolean) {
			return XSDDatatype.XSDboolean;
		}
		if (object instanceof String) {
			return XSDDatatype.XSDstring;
		}
		if (object instanceof Date) {
			return XSDDatatype.XSDdateTime;
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public static Class getJavaClassFromRDFDatatype(RDFDatatype rdfDatatype) {
		if (rdfDatatype.equals(XSDDatatype.XSDfloat)) {
			try {
				return Class.forName("Float");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (rdfDatatype.equals(XSDDatatype.XSDdouble)) {
			try {
				return Class.forName("Double");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (rdfDatatype.equals(XSDDatatype.XSDint)) {
			try {
				return Class.forName("Integer");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (rdfDatatype.equals(XSDDatatype.XSDlong)) {
			try {
				return Class.forName("Long");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (rdfDatatype.equals(XSDDatatype.XSDshort)) {
			try {
				return Class.forName("Short");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (rdfDatatype.equals(XSDDatatype.XSDbyte)) {
			try {
				return Class.forName("Byte");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (rdfDatatype.equals(XSDDatatype.XSDinteger)) {
			try {
				return Class.forName("BigInteger");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (rdfDatatype.equals(XSDDatatype.XSDdecimal)) {
			try {
				return Class.forName("BigDecimal");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (rdfDatatype.equals(XSDDatatype.XSDboolean)) {
			try {
				return Class.forName("Boolean");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (rdfDatatype.equals(XSDDatatype.XSDstring)) {
			try {
				return Class.forName("String");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (rdfDatatype.equals(XSDDatatype.XSDdateTime)) {
			try {
				return Class.forName("Date");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return null;
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
