package gov.epa.nrmrl.std.lca.ht.csvFiles;

import gov.epa.nrmrl.std.lca.ht.tdb.SelectTDB;
import gov.epa.nrmrl.std.lca.ht.views.ResultsView;
import gov.epa.nrmrl.std.lca.ht.views.QueryView.QueryViewContentProvider;
import gov.epa.nrmrl.std.lca.ht.views.QueryView.QueryViewLabelProvider;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.TableProvider;
import harmonizationtool.query.GenericUpdate;
import harmonizationtool.query.QueryResults;
import harmonizationtool.utils.Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.writer.CSVConfig;
import org.apache.commons.csv.writer.CSVWriter;
import org.apache.commons.csv.writer.CSVField;
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
		// -------------------------
		System.out.println("Saving Results");
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		ResultsView resultsView = (ResultsView) page.findView(ResultsView.ID);
		QueryResults queryResults = resultsView.getQueryResults();
		DataRow headerRow = queryResults.getColumnHeaders();
		List<DataRow> dataRows = queryResults.getTableProvider().getData();
		System.out.println(dataRows.get(0).toString());

		// ISelection iSelection = viewer.getSelection();
		// Object obj = ((IStructuredSelection) iSelection).getFirstElement();
		// System.out.println("saving file: " + obj);
		// Shell shell = getViewSite().getShell();
		Shell shell = HandlerUtil.getActiveShell(event);
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		String[] filterNames = new String[] { "Text Files", "All Files (*)" };
		String[] filterExtensions = new String[] { "*.txt", "*" };
//		String filterPath = "/";
//		String platform = SWT.getPlatform();
//		if (platform.equals("win32") || platform.equals("wpf")) {
//			filterNames = new String[] { "Text Files", "All Files (*.*)" };
//			filterExtensions = new String[] {
//					"*.txt", "*.*" };
//			filterPath = "c:\\";
//		}
//		else if (platform.equals("macosx")) {
//			filterNames = new String[] { "Text Files", "All Files (*.*)" };
//			filterExtensions = new String[] {
//					"*.txt", "*.*" };
//			filterPath = "~/";
//		}
		
		String outputDirectory = Util.getPreferenceStore().getString(
				"outputDirectory");
		if (outputDirectory.startsWith("(same as") || outputDirectory.length() == 0) {
			outputDirectory = Util.getPreferenceStore().getString(
					"workingDirectory");
		}
		if (outputDirectory.length() > 0) {
			dialog.setFilterPath(outputDirectory);
		} else {
			String homeDir = System.getProperty("user.home");
			dialog.setFilterPath(homeDir);
		}
		
		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);
		dialog.setFileName("query_results");
		
		String saveTo = dialog.open();
		System.out.println("Save to: " + saveTo);

		try {
			CSVWriter csvWriter = new CSVWriter();
			CSVConfig csvConfig = new CSVConfig();
			csvConfig.setDelimiter('\t');
//			csvConfig.setIgnoreValueDelimiter(true);
//			csvConfig.setValueDelimiter('"'); //IS THIS RIGHT?

			// configure file for output
			File file = new File(saveTo);
			if (!file.exists()) {
				file.createNewFile();
			}
			Writer fileWriter = new FileWriter(file);
			csvWriter.setWriter(fileWriter);

			// configure fields
			for (String header : headerRow.getColumnValues()) {
				csvConfig.addField(new CSVField(header.trim()));
			}
			csvWriter.setConfig(csvConfig);

			// prepare and write headers
			Map<String, String> map = new HashMap<String, String>();
			for (String header : headerRow.getColumnValues()) {
				map.put(header.trim(), header.trim());
			}
			csvWriter.writeRecord(map);

			// prepare and write data
			for (DataRow dataRow : dataRows) {
				map.clear();
				for (int i = 0; i < dataRow.getColumnValues().size(); i++) {
					String fieldName = headerRow.getColumnValues().get(i).trim();
					String value = dataRow.getColumnValues().get(i);
					map.put(fieldName, value);
				}
				csvWriter.writeRecord(map);
			}

			// flush and close writer
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

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
