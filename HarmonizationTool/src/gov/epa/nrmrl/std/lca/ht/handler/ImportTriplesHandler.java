package gov.epa.nrmrl.std.lca.ht.handler;

import gov.epa.nrmrl.std.lca.ht.sparql.ResultsView;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileInputStream;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.hp.hpl.jena.rdf.model.Model;

public class ImportTriplesHandler implements IHandler {
	// TODO: REMOVE THIS OR DETERMINE HOW IT IS DIFFERENT FROM ImportTDBHandler

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		// public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("executing Import Triples");
		if (ActiveTDB.tdbModel == null) {
			// String msg = "ERROR no TDB open";
			// Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
			return null;
		}
		Model model = ActiveTDB.tdbModel;
		// ModelProvider modelProvider = new ModelProvider();
		FileDialog fileDialog = new FileDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), SWT.OPEN);
		fileDialog.setFilterExtensions(new String[] { "*.n3" });
		String homeDir = System.getProperty("user.home");
		String inputDirectory = Util.getPreferenceStore().getString("inputDirectory");
		if (inputDirectory.length() > 0) {
			fileDialog.setFilterPath(inputDirectory);
		} else {
			fileDialog.setFilterPath(homeDir);
		}

		String path = fileDialog.open();
		// JenaReader jenaReader = new JenaReader();// maybe here FIXME
		// jenaReader.this;
		if (path != null) {
			StringBuilder b = new StringBuilder();
			// FileReader fileReader = null;
			try {
				// fileReader = new FileReader(path);
				// BufferedReader br = new BufferedReader(fileReader);
				// String s;
				// while ((s = br.readLine()) != null) {
				// // System.out.println(s);
				// if (s.toLowerCase().matches("prefix")){
				// System.out.println("PREFIX LINE");
				// }
				// b.append(s+"\n");
				// }
				// fileReader.close();

				InputStream inputStream = new FileInputStream(path);
				// JenaReader jenaReader = new JenaReader();
				// jenaReader.
				// jenaReader.setProperty("n3", SA);
				model.read(inputStream, "N3");
				// OPTIONS: "RDF/XML" "N-TRIPLE" "TURTLE" = "TTL" "N3"
				// jenaReader.read(tdbModel, inputStream, null); // DEFAULT IS RDF / XML

			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (Exception e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// String queryStr = b.toString();
		// GenericUpdate iGenericInsert = new GenericUpdate(queryStr,"Ext. File Update");

		// addFilename(path);
		// IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ResultsView resultsView = (ResultsView) Util.findView(ResultsView.ID);
		String title = resultsView.getTitle();
		System.out.println("title= " + title);

		// resultsView.update(iGenericInsert.getData());
		// resultsView.update(iGenericInsert.getQueryResults());
		// CSVTableView.setKey(path);
		// TableViewer tableViewer = csvTableView.getViewer();
		// tableViewer.setInput(new Object[] {""});
		// resultsView.update(path);

		// }

		// actionExtUpdate.setText("Exec. Update...");
		// actionExtUpdate.setToolTipText("SPARQL Update in .ttl file");
		// actionExtUpdate.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
		return null;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
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
