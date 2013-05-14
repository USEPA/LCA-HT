package harmonizationtool.handler;

import harmonizationtool.ResultsView;
import harmonizationtool.QueryView.QueryViewContentProvider;
import harmonizationtool.QueryView.QueryViewLabelProvider;
import harmonizationtool.comands.SelectTDB;
import harmonizationtool.model.ModelProvider;
import harmonizationtool.query.GenericUpdate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.ViewPart;

import com.hp.hpl.jena.rdf.arp.JenaReader;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;

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

		System.out.println("executing TDB Dump");
		Model model = SelectTDB.model;
		// ModelProvider modelProvider = new ModelProvider();
		FileDialog fileDialog = new FileDialog(HandlerUtil
				.getActiveWorkbenchWindow(event).getShell(), SWT.SAVE);
		fileDialog.setFilterExtensions(new String[] {"*.n3","*.rdf"});
		String homeDir = System.getProperty("user.home");
		fileDialog.setFilterPath(homeDir);
		String path = fileDialog.open();
		if (path != null) {
			long startTime = System.currentTimeMillis();

			try {
				String outType = "RDF/XML"; // DEFAULT
				if (path.matches(".*\\.n3.*")){outType = "N3";}
				System.out.println(path.toString());
				FileOutputStream fout = new FileOutputStream(path);
//				RDFWriter rdfWriter = model.getWriter("RDF/XML");
//				RDFWriter rdfWriter = model.getWriter(outType); // WORKED
//				rdfWriter.write(model, fout, null);             // WORKED
//				model.write(fout, path, outType);               // BAD
				model.write(fout, outType); // TESTING
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
			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			ResultsView resultsView = (ResultsView) page
					.findView(ResultsView.ID);
			String title = resultsView.getTitle();
			System.out.println("title= " + title);

			// resultsView.update(iGenericInsert.getData());
			// resultsView.update(iGenericInsert.getQueryResults());
			// ViewData.setKey(path);
			// TableViewer tableViewer = viewData.getViewer();
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
