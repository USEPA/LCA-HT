package gov.epa.nrmrl.std.lca.ht.csvFiles;

import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.dialog.GenericMessageBox;
import gov.epa.nrmrl.std.lca.ht.dialog.GenericStringBox;
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
import org.apache.log4j.Logger;
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

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.util.Utils;

public class SaveHarmonizedDataJsonld implements IHandler {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.csvFiles.SaveHarmonizedDataJsonld";

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

		Logger runLogger = Logger.getLogger("run");

		System.out.println("Saving Harmonized Data to .jsonld file");
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
		String[] filterNames = new String[] { "Json Files", "Jsonld Files" };
		String[] filterExtensions = new String[] { "*.json", "*.jsonld" };

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
		Util.findView(CSVTableView.ID);
		String key = CSVTableView.getTableProviderKey();
		DataSourceProvider dataSourceProvider = TableKeeper.getTableProvider(key).getDataSourceProvider();
		String currentName = dataSourceProvider.getDataSourceName();
		dialog.setFileName(currentName + "_harmoinzed");

		// GenericStringBox dataSetNameSelector = new GenericStringBox(shell, "(choose dataset)",
		// DataSourceKeeper.getAlphabetizedNames());

		String saveTo = dialog.open();
		System.out.println("Save to: " + saveTo);
		if (saveTo == null) {
			// "dialog was cancelled or an error occurred"
			return null;
		}

		runLogger.info("  # Writing RDF triples to " + saveTo.toString());
		try {
			FileOutputStream fout = new FileOutputStream(saveTo);
			String outType = ActiveTDB.getRDFTypeFromSuffix(saveTo);
			ActiveTDB.copyDatasetContentsToExportGraph(currentName);

			// --- BEGIN SAFE -READ- TRANSACTION ---
			ActiveTDB.tdbDataset.begin(ReadWrite.READ);
			Model tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
			tdbModel.write(fout, outType);
			ActiveTDB.tdbDataset.end();
			// ---- END SAFE -WRITE- TRANSACTION ---

		} catch (FileNotFoundException e1) {
			ActiveTDB.tdbDataset.abort();
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		/* To copy a dataset to the export graph */
		// ActiveTDB.copyDatasetContentsToExportGraph(olca);
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
