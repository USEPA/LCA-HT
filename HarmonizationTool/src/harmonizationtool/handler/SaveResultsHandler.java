package harmonizationtool.handler;

import harmonizationtool.ResultsView;
import harmonizationtool.QueryView.QueryViewContentProvider;
import harmonizationtool.QueryView.QueryViewLabelProvider;
import harmonizationtool.comands.SelectTDB;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.ModelProvider;
import harmonizationtool.query.GenericUpdate;
import harmonizationtool.query.QueryResults;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.ViewPart;

import com.hp.hpl.jena.rdf.model.Model;

public class SaveResultsHandler implements IHandler {

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
//-------------------------
		System.out.println("Saving Results");
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		ResultsView resultsView = (ResultsView) page
				.findView(ResultsView.ID);
		QueryResults queryResults = resultsView.getQueryResults();
		List<DataRow>  dataRows = queryResults.getModelProvider().getData();
		System.out.println(dataRows.get(0).toString());
		

//		ISelection iSelection = viewer.getSelection();
//		Object obj = ((IStructuredSelection) iSelection).getFirstElement();
//		System.out.println("saving file: " + obj);
//		Shell shell = getViewSite().getShell();
//		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
//		String[] filterNames = new String[] { "Image Files", "All Files (*)" };
//		String[] filterExtensions = new String[] { "*.csv", "*" };
//		String filterPath = "/";
//		String platform = SWT.getPlatform();
//		if (platform.equals("win32") || platform.equals("wpf")) {
//			filterNames = new String[] { "Image Files", "All Files (*.*)" };
//			filterExtensions = new String[] {
//					"*.gif;*.png;*.bmp;*.jpg;*.jpeg;*.tiff", "*.*" };
//			filterPath = "c:\\";
//		}
//		dialog.setFilterNames(filterNames);
//		dialog.setFilterExtensions(filterExtensions);
//		dialog.setFilterPath(filterPath);
//		dialog.setFileName("myfile");
//		String saveTo = dialog.open();
//		System.out.println("Save to: " + saveTo);
//
//		try {
//			File file = new File(saveTo);
//			if (!file.exists()) {
//				file.createNewFile();
//			}
//
//			FileWriter fw = new FileWriter(file.getAbsoluteFile());
//			BufferedWriter bw = new BufferedWriter(fw);
//			bw.write("this is the content");
//			bw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		//-------------------------
		
//		System.out.println("executing Export Triples");
//		Model model = SelectTDB.model;
//		// ModelProvider modelProvider = new ModelProvider();
//		FileDialog fileDialog = new FileDialog(HandlerUtil
//				.getActiveWorkbenchWindow(event).getShell(), SWT.SAVE);
//		fileDialog.setFilterExtensions(new String[] { "*.ttl", "*.n3" });
//		String homeDir = System.getProperty("user.home");
//		fileDialog.setFilterPath(homeDir);
//		String path = fileDialog.open();
//		if (path != null) {
//			try {
//
//				System.out.println(path.toString());
//				FileOutputStream fout = new FileOutputStream(path);
//				// model.write(fout,"TURTLE");
//				// model.write(fout, "TURTLE", null);
//				model.write(fout, "N3", null);
//				// The built-in languages are "RDF/XML"
//				// "RDF/XML-ABBREV"
//				// "N-TRIPLE"
//				// "N3"
//				// "TURTLE"
//				// In addition, for N3 output the language can be specified as:
//				// "N3-PP", "N3-PLAIN" or "N3-TRIPLE", which controls the style
//				// of N3 produced.
//
//				fout.close();
//				//
//
//			} catch (FileNotFoundException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} catch (Exception e) {
//
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			//
//			// StringBuilder b = new StringBuilder();
//			// FileReader fileReader = null;
//			// try {
//			// fileReader = new FileReader(path);
//			// BufferedReader br = new BufferedReader(fileReader);
//			// String s;
//			//
//			// while ((s = br.readLine()) != null) {
//			// // System.out.println(s);
//			// b.append(s+"\n");
//			// }
//			// fileReader.close();
//			// } catch (FileNotFoundException e1) {
//			// // TODO Auto-generated catch block
//			// e1.printStackTrace();
//			// } catch (IOException e) {
//			// // TODO Auto-generated catch block
//			// e.printStackTrace();
//			// }
//
//			// String queryStr = b.toString();
//			// GenericUpdate iGenericInsert = new
//			// GenericUpdate(queryStr,"Ext. File Update");
//
//			// addFilename(path);
//			IWorkbenchPage page = PlatformUI.getWorkbench()
//					.getActiveWorkbenchWindow().getActivePage();
//			ResultsView resultsView = (ResultsView) page
//					.findView(ResultsView.ID);
//			String title = resultsView.getTitle();
//			System.out.println("title= " + title);
//
//			// resultsView.update(iGenericInsert.getData());
//			// resultsView.update(iGenericInsert.getQueryResults());
//			// ViewData.setKey(path);
//			// TableViewer tableViewer = viewData.getViewer();
//			// tableViewer.setInput(new Object[] {""});
//			// resultsView.update(path);

//		}
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
