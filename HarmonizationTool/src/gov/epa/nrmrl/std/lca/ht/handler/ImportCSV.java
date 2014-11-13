package gov.epa.nrmrl.std.lca.ht.handler;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.FileMD;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.dialog.MetaDataDialog;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVStrategy;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class ImportCSV implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.handler.ImportCSV";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Logger runLogger = Logger.getLogger("run");
		TableProvider tableProvider = new TableProvider();
		FileMD fileMD = new FileMD();

		FileDialog fileDialog = new FileDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), SWT.OPEN);
		fileDialog.setFilterExtensions(new String[] { "*.csv" });
		String inputDirectory = Util.getPreferenceStore().getString("inputDirectory");
		if (inputDirectory.length() > 0) {
			fileDialog.setFilterPath(inputDirectory);
		} else {
			String homeDir = System.getProperty("user.home");
			fileDialog.setFilterPath(homeDir);
		}

		String path = fileDialog.open();
		if (path == null) {
			runLogger.info("# Cancelling CSV file read");
			return null;
		}
		File file = null;
		if (path != null) {
			file = new File(path);

			runLogger.info("LOAD CSV " + path);

			if (!file.exists()) {
				runLogger.warn("# File not found\n");
				return null;
			}
		}

		FileReader fileReader = null;
		try {
			fileReader = new FileReader(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (fileReader == null) {
			runLogger.error("# File not readable\n");
			return null;
		}

		CSVParser parser = new CSVParser(fileReader, CSVStrategy.EXCEL_STRATEGY);
		// FIXME - IF THE CSV FILE HAS WINDOWS CARRIAGE RETURNS, THE HT DOESN'T SPLIT ON THEM, SO YOU GET ONE ROW, MANY
		// COLUMNS
		String[] values = null;
		try {
			values = parser.getLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (values == null) { // BLANK FILE STILL HAS values (BUT ZERO LENGTH)
			runLogger.warn("# No content in CSV file!");

			// Util.findView(View.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
			// System.out.println(" No content in CSV file!");
			return null;
		}
		fileMD.setFilename(file.getName());
		fileMD.setPath(path);
		fileMD.setByteCount(file.length());
		fileMD.setModifiedDate(new Date(file.lastModified()));
		Date readDate = new Date();
		fileMD.setReadDate(readDate);
		runLogger.info("# File read at: " + Util.getLocalDateFmt(readDate));
		runLogger.info("# File last modified: " + Util.getLocalDateFmt(new Date(file.lastModified())));
		runLogger.info("# File size: " + file.length());

		System.out.println("All's fine before opening dialog");
		MetaDataDialog dialog = new MetaDataDialog(Display.getCurrent().getActiveShell(), fileMD);
		System.out.println("meta initialized");
		dialog.create();
		System.out.println("meta created");
		boolean thing;
		if (thing = dialog.open() == MetaDataDialog.CANCEL) { // FIXME
			System.out.println("cancel!");

			fileMD.remove();
			return null;
		}
		System.out.println("thing = " + thing);
		System.out.println("Got past opening dialog");
		tableProvider.setFileMD(fileMD);
		System.out.println("FileMD set in tableProvider");

		tableProvider.setDataSourceProvider(dialog.getCurDataSourceProvider());
		System.out.println("DataSource set in tableProvider");

		TableKeeper.saveTableProvider(path, tableProvider);
		System.out.println("Save tableProvider in TableKeeper");

		// READ THE FILE NOW
		try {
			while (values != null) {
				DataRow dataRow = initDataRow(values);
				tableProvider.addDataRow(dataRow); // SLOW PROCESS: JUNO FIXME
				values = parser.getLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Date readEndDate = new Date();
		int secondsRead = (int) ((readEndDate.getTime() - readDate.getTime()) / 1000);
		runLogger.info("# File read time (in seconds): " + secondsRead);

		try {
			Util.showView(CSVTableView.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		System.out.println("About to update CSVTableView");
		CSVTableView.update(path);

		return null;
	}

	private DataRow initDataRow(String[] values) {
		DataRow dataRow = new DataRow();
		for (String s : values) {
			dataRow.add(s);
		}
		return dataRow;
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
