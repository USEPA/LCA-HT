package gov.epa.nrmrl.std.lca.ht.handler;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.views.ResultsView;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.handlers.HandlerUtil;

public class ExportTriplesHandler implements IHandler {

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

		System.out.println("executing Export Triples");
		if (ActiveTDB.tdbModel == null) {
			// String msg = "ERROR no TDB open";
			// Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
			return null;
		}
//		Model model = ActiveTDB.tdbModel;
		// ModelProvider modelProvider = new ModelProvider();
		FileDialog fileDialog = new FileDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), SWT.SAVE);
		fileDialog.setFilterExtensions(new String[] { "*.ttl", "*.n3" });
		String homeDir = System.getProperty("user.home");
		String workingDirectory = Util.getPreferenceStore().getString("workingDirectory");
		if (workingDirectory.length() > 0) {
			fileDialog.setFilterPath(workingDirectory);
		} else {
			fileDialog.setFilterPath(homeDir);
		}
		String path = fileDialog.open();
		if (path != null) {
			try {

				System.out.println(path.toString());
				FileOutputStream fout = new FileOutputStream(path);
				// tdbModel.write(fout,"TURTLE");
				// tdbModel.write(fout, "TURTLE", null);
				ActiveTDB.tdbModel.write(fout, "N3");
				
				// CHOICES
				// RDF/XML
				// NULL (DEFAULTS TO RDF/XML)
				// RDF/XML-ABBREV
				// N-TRIPLE
				// TURTLE
				// "TTL"
				// "N3"
				
				// In addition, for N3 output the language can be specified as: "N3-PP", "N3-PLAIN" or "N3-TRIPLE",
				// which controls the style of N3 produced.

				fout.close();
				//

			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (Exception e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//
			// StringBuilder b = new StringBuilder();
			// FileReader fileReader = null;
			// try {
			// fileReader = new FileReader(path);
			// BufferedReader br = new BufferedReader(fileReader);
			// String s;
			//
			// while ((s = br.readLine()) != null) {
			// // System.out.println(s);
			// b.append(s+"\n");
			// }
			// fileReader.close();
			// } catch (FileNotFoundException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

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

		}
		// }
		// };
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
