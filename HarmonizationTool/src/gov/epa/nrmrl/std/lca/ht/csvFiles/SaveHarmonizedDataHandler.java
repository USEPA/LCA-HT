package gov.epa.nrmrl.std.lca.ht.csvFiles;

import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.dialog.GenericMessageBox;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.MatchContexts;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.MatchProperties;
import gov.epa.nrmrl.std.lca.ht.output.HarmonizedDataSelector;
import gov.epa.nrmrl.std.lca.ht.sparql.GenericUpdate;
import gov.epa.nrmrl.std.lca.ht.sparql.QueryResults;
import gov.epa.nrmrl.std.lca.ht.sparql.ResultsView;
import gov.epa.nrmrl.std.lca.ht.sparql.QueryView.QueryViewContentProvider;
import gov.epa.nrmrl.std.lca.ht.sparql.QueryView.QueryViewLabelProvider;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

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
import java.util.ArrayList;
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
import com.hp.hpl.jena.sparql.util.Utils;

public class SaveHarmonizedDataHandler implements IHandler {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.csvFiles.SaveHarmonizedDataHandler";

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
		Util.findView(MatchContexts.ID);
		Util.findView(MatchProperties.ID);

		System.out.println("Saving Harmonized Data");
		DataRow headerRow = HarmonizedDataSelector.getHarmonizedDataHeader();
		System.out.println("headerRow " + headerRow);

		List<DataRow> dataRows = new ArrayList<DataRow>();
		TableProvider tableProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey());
		for (int i = 0; i < tableProvider.getData().size(); i++) {
			DataRow dataRow = HarmonizedDataSelector.getHarmonizedDataRow(i);
			dataRows.add(dataRow);
		}

		Shell shell = HandlerUtil.getActiveShell(event);
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		String[] filterNames = new String[] { "Text Files", "All Files (*)" };
		String[] filterExtensions = new String[] { "*.txt", "*" };

		String outputDirectory = Util.getPreferenceStore().getString("outputDirectory");
		if (outputDirectory.startsWith("(same as") || outputDirectory.length() == 0) {
			outputDirectory = Util.getPreferenceStore().getString("workingDirectory");
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
		if (saveTo == null) {
			// "dialog was cancelled or an error occurred"
			return null;
		}

		try {
			CSVWriter csvWriter = new CSVWriter();
			CSVConfig csvConfig = new CSVConfig();
			csvConfig.setDelimiter('\t');
			// FIXME - FOR CONSISTENCY, WRITE TRUE CSV (GOOFY, THOUGH IT IS), NOT TSV
			// csvConfig.setIgnoreValueDelimiter(true);
			// csvConfig.setValueDelimiter('"'); //IS THIS RIGHT?

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
