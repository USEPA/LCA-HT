package harmonizationtool.handler;

import harmonizationtool.ResultsView;
import harmonizationtool.QueryView.QueryViewContentProvider;
import harmonizationtool.QueryView.QueryViewLabelProvider;
import harmonizationtool.comands.SelectTDB;
import harmonizationtool.model.ModelProvider;
import harmonizationtool.query.GenericUpdate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

import com.hp.hpl.jena.*;
import com.hp.hpl.jena.rdf.arp.JenaReader;
import com.hp.hpl.jena.rdf.model.Model;

public class ImportTDBHandler implements IHandler {
	// TODO: REFACTOR TO CALL THIS SOMETHING MORE LIKE: ImportRDFHandler
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
		System.out.println("executing TDB load");
		Model model = SelectTDB.model;
		FileDialog fileDialog = new FileDialog(HandlerUtil
				.getActiveWorkbenchWindow(event).getShell(), SWT.OPEN
				| SWT.MULTI);
//		fileDialog
//				.setFilterExtensions(new String[] { "*.zip", "*.n3", "*.rdf" });
		fileDialog
		.setFilterExtensions(new String[] { "*.zip;*.n3;*.rdf" }); // SHOWS ALL TYPES IN ONE WINDOW

		String homeDir = System.getProperty("user.home");
		fileDialog.setFilterPath(homeDir);
		System.out.println("Ready to open");
		// ----------------- TOMMY HELP FIX THIS (BELOW) ----------------------
		// String path = fileDialog.open(); // INPUT FROM USER
		fileDialog.open(); // INPUT FROM USER
		String path = fileDialog.getFilterPath();
		String[] fileList = fileDialog.getFileNames();
		
		String sep = System.getProperty("file.separator");
		System.out.println("path= "+path);
		for(String fileName : fileList){
			System.out.println("fileName= "+fileName);
			if(!fileName.startsWith(sep)){
				fileName = path + sep + fileName;
			}
//		}

//		String nextFile = null;
//		for (int iterator = 0; iterator < fileList.length; iterator++) {
//			nextFile = fileList[iterator];
//			String fileName = path + "/" + nextFile; // ESPECIALLY WRONG
//			System.out.println("fileName: " + fileName);
//			System.out.println("File: " + nextFile + " from path: " + path);
//			// ----------------- TOMMY HELP FIX THIS (ABOVE)
//			// ----------------------

			long was = model.size();
			long startTime = System.currentTimeMillis();
			if (!fileName.matches(".*\\.zip.*")) {
				try {
					String inputType = "RDF/XML";
					if (fileName.matches(".*\\.n3.*")) {
						inputType = "N3";
					}
					InputStream inputStream = new FileInputStream(fileName);
					model.read(inputStream, null, inputType);
					// JenaReader jenaReader = new JenaReader();
					// jenaReader.setProperty("n3", SA); // TEST THIS SOME
					// DAY
					// MAYBE?
					// jenaReader.read(model, inputStream, null); // DEFAULT
					// IS
					// RDF
					// - XML

				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (Exception e) {

					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (fileName.matches(".*\\.zip.*")) {
				// System.out.println("Got a zip file");
				try {
					ZipFile zf = new ZipFile(fileName);
					Enumeration entries = zf.entries();
					// JenaReader jenaReader = new JenaReader();
					while (entries.hasMoreElements()) {
						ZipEntry ze = (ZipEntry) entries.nextElement();
						String inputType = "SKIP";
						if (ze.getName().matches(".*\\.rdf.*")) {
							inputType = "RDF/XML";
						} else if (ze.getName().matches(".*\\.n3.*")) {
							inputType = "N3";
						}
						if (inputType != "SKIP") {
							System.out.println("Adding data from " + inputType
									+ " zipped file:" + ze.getName());
							BufferedReader zipStream = new BufferedReader(
									new InputStreamReader(zf.getInputStream(ze)));
							model.read(zipStream, null, inputType);
							// jenaReader.read(model, zipStream, null);
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			float elapsedTimeSec = (System.currentTimeMillis() - startTime) / 1000F;
			System.out.println("Time elapsed: " + elapsedTimeSec);

			long now = model.size();
			long change = now - was;
			System.out.println("Was:" + was + " Added:" + change + " Now:"
					+ now);
		}
		// GenericUpdate iGenericInsert = new
		// GenericUpdate(queryStr,"Ext. File Update");

		// addFilename(fileName);
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		ResultsView resultsView = (ResultsView) page.findView(ResultsView.ID);
		String title = resultsView.getTitle();
		System.out.println("title= " + title);

		// resultsView.update(iGenericInsert.getData());
		// resultsView.update(iGenericInsert.getQueryResults());
		// ViewData.setKey(fileName);
		// TableViewer tableViewer = viewData.getViewer();
		// tableViewer.setInput(new Object[] {""});
		// resultsView.update(fileName);

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
