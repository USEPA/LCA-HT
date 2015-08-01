package gov.epa.nrmrl.std.lca.ht.csvFiles;

import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.FlowContext;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.MatchContexts;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.FlowUnit;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.MatchProperties;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.Flowable;
import gov.epa.nrmrl.std.lca.ht.handler.ImportUserData;
import gov.epa.nrmrl.std.lca.ht.output.HarmonizedDataSelector;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class SaveHarmonizedDataHandler implements IHandler {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.csvFiles.SaveHarmonizedDataHandler";

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
	}
	
	public void writeStoredData(String dataSourceName, String saveLocation) {
		ResultSetRewindable resultSetRewindable = ImportUserData.queryOLCATAbleData(dataSourceName);

		DataRow headerRow = new DataRow();
		
		TableProvider.setHeaderNames(headerRow, resultSetRewindable.getResultVars());
		
		List<DataRow> dataRows = new ArrayList<DataRow>();
				
		while (resultSetRewindable.hasNext()) {
			QuerySolution soln = resultSetRewindable.nextSolution();
			DataRow dataRow = new DataRow();
			dataRows.add(dataRow);
			Iterator<String> iterator = headerRow.getIterator();
			while (iterator.hasNext()) {
				String header = iterator.next();
				try {
					RDFNode rdfNode = null;
					rdfNode = soln.get(header);
					if (rdfNode == null) {
						dataRow.add("");
					} else {
						dataRow.add(rdfNode.toString());

					}
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
		}
		writeTableData(headerRow, dataRows, saveLocation);
	}
	
	public List<DataRow> getOpenTableData() {
		List<DataRow> dataRows = new ArrayList<DataRow>();
		TableProvider tableProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey());
		for (int i = 0; i < tableProvider.getData().size(); i++) {
			DataRow dataRow = HarmonizedDataSelector.getHarmonizedDataRow(i);
			dataRows.add(dataRow);
		}
		return dataRows;
	}


	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		
		Util.findView(MatchContexts.ID);
		Util.findView(MatchProperties.ID);
		
		FlowsWorkflow.disableAllButtons();

		System.out.println("Saving Harmonized Data");
		
		final String dataSetName = event.getParameter("LCA-HT.exportDataSetName");

		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		String[] filterNames = new String[] { "CSV Files", "Text Files", "All Files (*)" };
		String[] filterExtensions = new String[] { "*.csv", "*.txt", "*" };

		String outputDirectory = Util.getPreferenceStore().getString("outputDirectory");
		if (outputDirectory.length() > 0) {
			dialog.setFilterPath(outputDirectory);
		} else {
			String homeDir = System.getProperty("user.home");
			dialog.setFilterPath(homeDir);
		}

		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);
		dialog.setFileName("query_results");

		final String saveTo = dialog.open();
		System.out.println("Save to: " + saveTo);
		
		if (saveTo == null) {
//			FlowsWorkflow.restoreAllButtons();
			FlowsWorkflow.switchToWorkflowState(3);
		}
		
		
		new Thread(new Runnable() { public void run() {
			if (dataSetName != null) {
				writeStoredData(dataSetName, saveTo);
				Display.getDefault().syncExec(new Runnable() { public void run() {
//					FlowsWorkflow.restoreAllButtons();
					FlowsWorkflow.switchToWorkflowState(3);
				}});
			}
			
			DataRow headerRow = HarmonizedDataSelector.getHarmonizedDataHeader();
			System.out.println("headerRow " + headerRow);
	
			List<DataRow> dataRows = getOpenTableData();
			
			writeTableData(headerRow, dataRows, saveTo);
			Display.getDefault().syncExec(new Runnable() { public void run() {
//				FlowsWorkflow.restoreAllButtons();
				FlowsWorkflow.switchToWorkflowState(3);
			}});
			}}).start();
		return null;
	}
	
	public void writeTableData(DataRow headerRow, List<DataRow> dataRows, String saveTo) {

		try {
			// FIXME - FOR CONSISTENCY, WRITE TRUE CSV (GOOFY, THOUGH IT IS), NOT TSV
			// csvConfig.setIgnoreValueDelimiter(true);
			// csvConfig.setValueDelimiter('"'); //IS THIS RIGHT?

			// configure file for output
			File file = new File(saveTo);
			if (!file.exists()) {
				file.createNewFile();
			}
			Writer fileWriter = new FileWriter(file);
			CSVFormat format = CSVFormat.newFormat('\t');

			// configure fields
			String[] headers = new String[headerRow.getColumnValues().size()];
			int i = 0;
			for (String header : headerRow.getColumnValues()) {
				headers[i++] = header;
			}
			format = format.withHeader(headers).withRecordSeparator("\n");
			CSVPrinter csvPrinter = new CSVPrinter(fileWriter, format);

			// prepare and write data
			for (DataRow dataRow : dataRows) {
//				System.out.println("Row: "+row++);
				for (i = 0; i < dataRow.getColumnValues().size(); i++) {
					csvPrinter.printRecord(dataRow.getColumnValues());
				}
			}

			// flush and close writer (closes underlying writer)
			csvPrinter.flush();
			csvPrinter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
	}

}
