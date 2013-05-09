package harmonizationtool.comands;

import harmonizationtool.Activator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.hp.hpl.jena.query.Dataset;
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

public class SelectTDB implements IHandler {
	public static Model model = null;
	public static Dataset dataset = null;
	public static String tdbDir = null;
	public static GraphStore graphStore = null;

	public void finalize() {
		System.out.println("closing dataset and model");
		dataset.close();
		model.close();
		graphStore.close();
	}

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);

		System.out.println("Executing SelectTDB");
		DirectoryDialog directoryDialog = new DirectoryDialog(PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getShell());
		tdbDir = directoryDialog.open();
		System.out.println("tdbDir=" + tdbDir);
		try {
			dataset = TDBFactory.createDataset(tdbDir);
//			TDBFactory.createDataset(tdbDir); // FIXME DO WE NEED THIS?
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		new PrefixMapping.Factory();
		PrefixMapping prefixMapping = Factory.create();
//		prefixMapping.setNsPrefix("eco", "<http://ontology.earthster.org/eco/core#>");
//		prefixMapping.setNsPrefix("ecou", "<http://ontology.earthster.org/eco/unit#>");
//		prefixMapping.setNsPrefix("ethold", "<http://epa.gov/nrmrl/std/lca#>");
//		prefixMapping.setNsPrefix("afn", "<http://jena.hpl.hp.com/ARQ/function#>");
//		prefixMapping.setNsPrefix("fn", "<http://www.w3.org/2005/xpath-functions#>");
//		prefixMapping.setNsPrefix("owl", "<http://www.w3.org/2002/07/owl#>");
//		prefixMapping.setNsPrefix("skos", "<http://www.w3.org/2004/02/skos/core#>");
//		prefixMapping.setNsPrefix("rdf", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
//		prefixMapping.setNsPrefix("rdfs", "<http://www.w3.org/2000/01/rdf-schema#>");
//		prefixMapping.setNsPrefix("xml", "<http://www.w3.org/XML/1998/namespace>");
//		prefixMapping.setNsPrefix("xsd", "<http://www.w3.org/2001/XMLSchema#>");
//		
//		//
//		// System.out.println(prefixMapping.toString());
		model = dataset.getDefaultModel();
		model.setNsPrefixes(prefixMapping);



		graphStore = GraphStoreFactory.create(dataset); // FIXME DO WE NEED THIS?

		System.err.printf("Model size is: %s\n", model.size());

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

}
