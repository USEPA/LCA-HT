package gov.epa.nrmrl.std.lca.ht.tdb;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TDBTest implements IHandler, IActiveTDB {
	{

	}
	
	private static void openTDB(){
		final Dataset tdbDataset = TDBFactory.createDataset("/Users/transue/tdbplay");
		final Model tdbModel = tdbDataset.getDefaultModel();
		// tdbModel = tdbDataset.getNamedModel("namedGraph");
		final GraphStore graphStore = GraphStoreFactory.create(tdbDataset);
		System.out.println("TDB Successfully initiated!");
		Resource testResource = tdbModel.createResource("testResource");
		testResource.addProperty(RDFS.label, "test resource");
		Resource testClass = tdbModel.createResource(OWL.Class);
		testResource.addProperty(RDF.type, testClass);
		
	}
	
	public static void main(){
		Display display = new Display();
		Shell shell = new Shell(display);
		openTDB();
		String query = "select * where {?s ?p ?o}";
		
	}

	@Override
	public void addSelectedTDBListener(IActiveTDBListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeSelectedTDBListener(IActiveTDBListener listener) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isHandled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}
}
