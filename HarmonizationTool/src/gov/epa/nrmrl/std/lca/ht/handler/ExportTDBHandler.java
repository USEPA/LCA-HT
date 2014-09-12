package gov.epa.nrmrl.std.lca.ht.handler;

import gov.epa.nrmrl.std.lca.ht.sparql.ResultsView;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class ExportTDBHandler implements IHandler {

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
		// public Object execute(ExecutionEvent event) throws ExecutionException
		// {
		if (ActiveTDB.tdbModel == null) {
			// String msg = "ERROR no TDB open";
			// Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
			return null;
		}

		System.out.println("executing TDB Dump");
		// Model model = ActiveTDB.tdbModel;
		// ModelProvider modelProvider = new ModelProvider();
		FileDialog fileDialog = new FileDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), SWT.SAVE);
		fileDialog.setFilterExtensions(new String[] { "*.n3", "*.rdf" });
		String outputDirectory = Util.getPreferenceStore().getString("outputDirectory");
		if (outputDirectory.startsWith("(same as") || outputDirectory.length() == 0) {
			outputDirectory = Util.getPreferenceStore().getString("workingDirectory");
		}
		if (outputDirectory.length() > 0) {
			fileDialog.setFilterPath(outputDirectory);
		} else {
			String homeDir = System.getProperty("user.home");
			fileDialog.setFilterPath(homeDir);
		}
		String path = fileDialog.open();
		if (path != null) {
			long startTime = System.currentTimeMillis();

			try {
				String outType = "RDF/XML"; // DEFAULT
				if (path.matches(".*\\.n3.*")) {
					outType = "N3";
				}
				System.out.println(path.toString());
				FileOutputStream fout = new FileOutputStream(path);
				// RDFWriter rdfWriter = tdbModel.getWriter("RDF/XML");
				// RDFWriter rdfWriter = tdbModel.getWriter(outType); // WORKED
				// rdfWriter.write(tdbModel, fout, null); // WORKED
				// tdbModel.write(fout, path, outType); // BAD
				ActiveTDB.tdbModel.write(fout, outType); // TESTING
				fout.close();
				//

			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (Exception e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// String queryStr = b.toString();
			// GenericUpdate iGenericInsert = new
			// GenericUpdate(queryStr,"Ext. File Update");

			// addFilename(path);
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			ResultsView resultsView = (ResultsView) page.findView(ResultsView.ID);
			String title = resultsView.getTitle();
			System.out.println("title= " + title);

			// resultsView.update(iGenericInsert.getData());
			// resultsView.update(iGenericInsert.getQueryResults());
			// CSVTableView.setKey(path);
			// TableViewer tableViewer = csvTableView.getViewer();
			// tableViewer.setInput(new Object[] {""});
			// resultsView.update(path);
			float elapsedTimeSec = (System.currentTimeMillis() - startTime) / 1000F;
			System.out.println("Time elapsed: " + elapsedTimeSec);
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
