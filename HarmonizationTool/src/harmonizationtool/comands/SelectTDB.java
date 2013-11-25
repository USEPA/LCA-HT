package harmonizationtool.comands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import harmonizationtool.Activator;
import harmonizationtool.QueryView;
import harmonizationtool.ViewData;
import harmonizationtool.utils.Util;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.Bundle;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
//import com.hp.hpl.jena.query.Query;
//import com.hp.hpl.jena.query.QueryExecution;
//import com.hp.hpl.jena.query.QueryExecutionFactory;
//import com.hp.hpl.jena.query.QueryFactory;
//import com.hp.hpl.jena.query.QuerySolution;
//import com.hp.hpl.jena.query.ResultSet;
//import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
//import com.hp.hpl.jena.rdf.model.RDFNode;
//import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.PrefixMapping.Factory;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;

//import com.hp.hpl.jena.query.Dataset;
//import com.hp.hpl.jena.query.Query;
//import com.hp.hpl.jena.query.QueryExecution;
//import com.hp.hpl.jena.query.QueryExecutionFactory;
//import com.hp.hpl.jena.query.QueryFactory;
//import com.hp.hpl.jena.query.QuerySolution;
//import com.hp.hpl.jena.query.ResultSet;
//import com.hp.hpl.jena.rdf.model.Literal;
//import com.hp.hpl.jena.rdf.model.Model;
//import com.hp.hpl.jena.rdf.model.RDFNode;
//import com.hp.hpl.jena.rdf.model.Resource;
//import com.hp.hpl.jena.tdb.TDBFactory;

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
		String msg = "Opening TDB: "+Util.getPreferenceStore().getString("defaultTDB");
		Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
		String defaultTDB = Util.getPreferenceStore().getString("defaultTDB");
		File defaultTDBFile = new File(defaultTDB);
		if (defaultTDBFile.isDirectory()) {
			System.out.println("defaultTDBFile.list().length=" + defaultTDBFile.list().length);
			try {
				dataset = TDBFactory.createDataset(defaultTDBFile.getPath());
				assert dataset != null : "dataset cannot be null";
				model = dataset.getDefaultModel();
				graphStore = GraphStoreFactory.create(dataset); // FIXME DO WE NEED

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		 msg = "Using TDB: "+Util.getPreferenceStore().getString("defaultTDB");
			Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
		return null;
	}

	// @Override
	public Object executeOLD(ExecutionEvent event) throws ExecutionException {
		String msg = "SelectTDB.execute()";
		Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
		System.out.println(msg);
		// cleanUp();
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		System.out.println("Executing SelectTDB");
		String defaultTDB = Util.getPreferenceStore().getString("defaultTDB");
		File defaultTDBFile = new File(defaultTDB);
		DirectoryDialog directoryDialog = new DirectoryDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		System.out.println("defaultTDBFile.isDirectory() = " + defaultTDBFile.isDirectory());
		if (defaultTDBFile.isDirectory()) {
			System.out.println("calling directoryDialog.setFilterPath(" + defaultTDBFile.getPath() + ")");
			directoryDialog.setFilterPath(defaultTDBFile.getPath());
		}
		tdbDir = directoryDialog.open();
		if (tdbDir != null) {
			cleanUp();
		} else {
			return null;
		}
		System.out.println("tdbDir=" + tdbDir);
		try {
			dataset = TDBFactory.createDataset(tdbDir);
			// TDBFactory.createDataset(tdbDir); // FIXME DO WE NEED THIS?
		} catch (Exception e) {
			e.printStackTrace();
		}

		// new PrefixMapping.Factory();
		// PrefixMapping prefixMapping = Factory.create();
		// prefixMapping.setNsPrefix("eco",
		// "<http://ontology.earthster.org/eco/core#>");
		// prefixMapping.setNsPrefix("ecou",
		// "<http://ontology.earthster.org/eco/unit#>");
		// prefixMapping.setNsPrefix("ethold",
		// "<http://epa.gov/nrmrl/std/lca#>");
		// prefixMapping.setNsPrefix("afn",
		// "<http://jena.hpl.hp.com/ARQ/function#>");
		// prefixMapping.setNsPrefix("fn",
		// "<http://www.w3.org/2005/xpath-functions#>");
		// prefixMapping.setNsPrefix("owl", "<http://www.w3.org/2002/07/owl#>");
		// prefixMapping.setNsPrefix("skos",
		// "<http://www.w3.org/2004/02/skos/core#>");
		// prefixMapping.setNsPrefix("rdf",
		// "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
		// prefixMapping.setNsPrefix("rdfs",
		// "<http://www.w3.org/2000/01/rdf-schema#>");
		// prefixMapping.setNsPrefix("xml",
		// "<http://www.w3.org/XML/1998/namespace>");
		// prefixMapping.setNsPrefix("xsd",
		// "<http://www.w3.org/2001/XMLSchema#>");
		//
		// //
		// // System.out.println(prefixMapping.toString());
		assert dataset != null : "dataset cannot be null";
		model = dataset.getDefaultModel();
		// HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().findView(ViewData.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(
		// msg);
		// model.setNsPrefixes(prefixMapping);

		graphStore = GraphStoreFactory.create(dataset); // FIXME DO WE NEED
														// THIS?

		System.err.printf("Model size is: %s\n", model.size());
		msg = "TDB loading complete. Model size is:" + model.size();

		// boolean doQuery = false;
		// if (doQuery) {
		//
		// String queryString = "SELECT * WHERE { ?s ?p ?o } limit 100";
		// Query query = QueryFactory.create(queryString);
		// QueryExecution qexec = QueryExecutionFactory.create(query, model);
		// try {
		// ResultSet results = qexec.execSelect();
		// for (; results.hasNext();) {
		// QuerySolution soln = results.nextSolution();
		// System.out.println(soln);
		// RDFNode x = soln.get("varName"); // Get a result variable by name.
		// Resource r = soln.getResource("VarR"); // Get a result variable -
		// must be a
		// // resource
		// Literal l = soln.getLiteral("VarL"); // Get a result variable - must
		// be a
		// // literal
		// }
		// } catch (Exception e) {
		// System.err.print("Error:" + e.getMessage());
		// } finally {
		// qexec.close();
		// }
		// }
		for (ISelectedTDBListener listener : selectedTDBListeners) {
			listener.TDBchanged(tdbDir);
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
	public void addSelectedTDBListener(ISelectedTDBListener listener) {
		System.out.println("Added TDBListener = " + listener);
		selectedTDBListeners.add(listener);
	}

	@Override
	public void removeSelectedTDBListener(ISelectedTDBListener listener) {
		// TODO Auto-generated method stub

	}

}
