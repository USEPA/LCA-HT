package harmonizationtool.handler;

import harmonizationtool.QueryView;
import harmonizationtool.ResultsView;
import harmonizationtool.QueryView.QueryViewContentProvider;
import harmonizationtool.QueryView.QueryViewLabelProvider;
import harmonizationtool.comands.SelectTDB;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.ModelProvider;
import harmonizationtool.query.GenericQuery;
import harmonizationtool.query.GenericUpdate;
import harmonizationtool.query.QGetNextDSIndex;
import harmonizationtool.query.QueryResults;
import harmonizationtool.query.UAssignDSIndex_with_param;
import harmonizationtool.utils.Util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.List;
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

	private QGetNextDSIndex qGetNextDSIndex = new QGetNextDSIndex();
	private UAssignDSIndex_with_param uAssignDSIndex_with_param = new UAssignDSIndex_with_param();

	// qGetNextDSIndex.

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		// public Object execute(ExecutionEvent event) throws ExecutionException
		System.out.println("executing TDB load");
		if(SelectTDB.model== null){
			String msg = "ERROR no TDB open";
			Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
			return null;
		}

		Model model = SelectTDB.model;
		String workingDir = Util.getPreferenceStore().getString("workingDir");
		FileDialog fileDialog = new FileDialog(HandlerUtil
				.getActiveWorkbenchWindow(event).getShell(), SWT.OPEN
				| SWT.MULTI);
		fileDialog.setFilterPath(workingDir);
		// fileDialog
		// .setFilterExtensions(new String[] { "*.zip", "*.n3", "*.rdf" });
		fileDialog.setFilterExtensions(new String[] { "*.zip;*.n3;*.rdf" }); // SHOWS
																				// ALL
																				// TYPES
																				// IN
																				// ONE
																				// WINDOW

//		String homeDir = System.getProperty("user.home");
//		fileDialog.setFilterPath(homeDir);
		System.out.println("Ready to open");
		// ----------------- TOMMY HELP FIX THIS (BELOW) ----------------------
		// String path = fileDialog.open(); // INPUT FROM USER
		fileDialog.open(); // INPUT FROM USER
		String path = fileDialog.getFilterPath();
		String[] fileList = fileDialog.getFileNames();

		String sep = System.getProperty("file.separator");
		System.out.println("path= " + path);
		for (String fileName : fileList) {
			System.out.println("fileName= " + fileName);
			if (!fileName.startsWith(sep)) {
				fileName = path + sep + fileName;
			}
			// }

			long was = model.size();
			long startTime = System.currentTimeMillis();
			if (!fileName.matches(".*\\.zip.*")) {
				try {
					int next = 1;
					String inputType = "RDF/XML";
					if (fileName.matches(".*\\.n3.*")) {
						inputType = "N3";
					}
					InputStream inputStream = new FileInputStream(fileName);
					model.read(inputStream, null, inputType);
					// NEED TO DETERINE WHAT THE NEXT DATA SET LOCAL INDEX
					// NUMBER IS
					GenericQuery iGenericQuery = new GenericQuery(
							qGetNextDSIndex.getQuery(), "Internal Query");
					iGenericQuery.getData();
					QueryResults parts = iGenericQuery.getQueryResults();
					List<DataRow> resultRow = parts.getModelProvider()
							.getData();
					// if(resultRow.size() > 0){
					DataRow row = resultRow.get(0);
					List<String> valueList = row.getColumnValues();
					String indexStr = valueList.get(0);
					next = Integer.parseInt(indexStr);
					// }

					System.out.println("resultRow = " + resultRow.toString());

					// String[] strarray = (String[]) parts.toArray();
					// System.out.println("strarray.toString() = "+strarray.toString()
					// );

					// System.out.println("iGenericQuery.getData().toString() = "+iGenericQuery.getData().toString());

					// int next =
					// Integer.parseInt(parts.toArray()[1].toString());

					// int next = 1*Integer.parseInt(parts.subList(1,
					// 1).toString());
					// int next = Integer.parseInt(parts[1].toString());
					System.out.println("next = " + next);

					
					uAssignDSIndex_with_param.setNext(next);
					uAssignDSIndex_with_param.getQuery();
					
					System.out.println("success setting 'next'");
					uAssignDSIndex_with_param.getData();

					// UAssignDSIndex_with_param.

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
		// System.out.println("qGetNextDSIndex = "+qGetNextDSIndex.toString());

		String msg = "Finished Loading TDB: "+Util.getPreferenceStore().getString("defaultTDB");
		Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);

		
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
