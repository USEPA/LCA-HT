package gov.epa.nrmrl.std.lca.ht.output;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.MatchContexts;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.MatchProperties;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;



//import java.util.Calendar;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openlca.lcaht.converter.Json2Zip;


public class SaveHarmonizedDataForOLCAJsonldZip implements IHandler {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.output.SaveHarmonizedDataForOLCAJsonldZip";

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		

			
		Util.findView(MatchContexts.ID);
		Util.findView(MatchProperties.ID);

		Logger runLogger = Logger.getLogger("run");

		System.out.println("Saving Harmonized Data to .zip file");

		Shell shell = HandlerUtil.getActiveShell(event);
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		String[] filterNames = new String[] { "Zip Files" };
		String[] filterExtensions = new String[] { "*.zip" };

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
		dialog.setFileName(currentName + "_harmonized");

		String saveTo = dialog.open();
		System.out.println("Save to: " + saveTo);
		if (saveTo == null) {
			// "dialog was cancelled or an error occurred"
			return null;
		}
		
		String tempOutputName = saveTo + ".tmp.json";
		HashMap newParams = new HashMap(event.getParameters());
		newParams.put("LCA-HT.outputFilename", tempOutputName);
		ExecutionEvent newEvent = new ExecutionEvent(event.getCommand(), newParams, event.getTrigger(),event.getApplicationContext());
		
		runLogger.debug("Converting to OpenLCA Zip");
		SaveHarmonizedDataForOLCAJsonld innerHandler = new SaveHarmonizedDataForOLCAJsonld();
		innerHandler.execute(newEvent);
		
		File input = new File(tempOutputName);
		File output = new File(saveTo);
		new Json2Zip(input, output).run();
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
