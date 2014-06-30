package harmonizationtool.handler;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;
import harmonizationtool.dialog.MetaDataDialog;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.FileMD;
import harmonizationtool.model.TableKeeper;
import harmonizationtool.model.TableProvider;
import harmonizationtool.utils.Util;

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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class ImportCSV implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public static final String ID = "harmonizationtool.handler.ImportCSV";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Logger runLogger = Logger.getLogger("run");

		System.out.println("executing Import CSV");
		// ModelProvider modelProvider = new ModelProvider();
		TableProvider tableProvider = new TableProvider();
		// FileMD(String filename, String path, long size, Date modifiedDate, Date readDate)
		FileMD fileMD = new FileMD();

		FileDialog fileDialog = new FileDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), SWT.OPEN);
		fileDialog.setFilterExtensions(new String[] { "*.csv" });
		String workingDirectory = Util.getPreferenceStore().getString("workingDirectory");
		if (workingDirectory.length() > 0) {
			fileDialog.setFilterPath(workingDirectory);
		} else {
			String homeDir = System.getProperty("user.home");
			fileDialog.setFilterPath(homeDir);
		}

		String path = fileDialog.open();
		File file = null;
		if (path != null) {
			file = new File(path);

			runLogger.info("LOAD CSV " + path);

			if (!file.exists()) {
				runLogger.warn("# File not found\n");

				// String msg = "File does not exist!";
				// Util.findView(View.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
				// System.out.println(msg);
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
			// String msg = "Can not read CSV file!";
			runLogger.error("# File not readable\n");

			// Util.findView(View.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
			// System.out.println(msg);
			return null;
		}
		// FileEncodingUtil checkFileEncoding = new FileEncodingUtil();
		// List<NonAsciiChar> nonAsciiCandidates = FileEncodingUtil.getFirstNonAsciiChar(path, 0);
		// String cr = System.getProperty("line.separator");
		// for (int i = 0; i < nonAsciiCandidates.size(); i++) {
		// NonAsciiChar nonAsciiChar = nonAsciiCandidates.get(i);
		// String message = "# Character Encoding info:" + cr;
		// message += "#    File: " + path + cr;
		// message += "#    Encoding: " + nonAsciiChar.encoding + cr;
		// message += "#    First non-ASCII character: ->" + nonAsciiChar.character + "<-" + cr;
		// message += "#    Occuring on line: " + nonAsciiChar.lineNumber + cr;
		// message += "#    Characters to the left: " + nonAsciiChar.colNumber + cr;
		// runLogger.info(message);
		// }

		// checkFileEncoding.firstNonAsciiChars("basic");
		// String issues = checkFileEncoding.showFileIssues(path);
		// if (issues != null) {
		// runLogger.warn("# Non-ASCII issues found:\n" + issues);
		// System.out.println("Issues found with file: " + path + ":\n" +
		// issues);
		// }
		CSVParser parser = new CSVParser(fileReader, CSVStrategy.EXCEL_STRATEGY);
		String[] values = null;
		try {
			values = parser.getLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (values == null) { // BLANK FILE STILL HAS values (BUT ZERO LENGTH)
		// String msg = "No content in CSV file!";
			runLogger.warn("# No content in CSV file!");

			// Util.findView(View.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
			// System.out.println(msg);
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

		// IF WE GOT CONTENT, THEN SAVE THIS FILE (MODEL) AND ADD IT TO THE MENU
		TableKeeper.saveTableProvider(path, tableProvider);

		// READ FILE, PARSING INTO tableProvider
		// FIXME: SHOULD WE / CAN WE : PARSE DIRECTLY INTO CSVTableView: TableViewer?
		while (values != null) {
			// printValues(parser.getLineNumber(),values);
			DataRow dataRow = initDataRow(values);
			tableProvider.addDataRow(dataRow);
			// System.out.println(dataRow);
			try {
				values = parser.getLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Date readEndDate = new Date();
		int secondsRead = (int) ((readEndDate.getTime() - readDate.getTime()) / 1000);
		runLogger.info("# File read time (in seconds): " + secondsRead);

		// NOW OPEN DIALOG WITH SOME PRE-POPULATE fileMD INFO
		MetaDataDialog dialog = new MetaDataDialog(Display.getCurrent().getActiveShell(), fileMD);
//		CSVTableView.setDataSourceProvider(dialog.getCurDataSourceProvider());
		dialog.create();
		dialog.open();
		// FIXME - GET THE DataSourceProvider TO THE CSVTableView
		if (dialog.open() == MetaDataDialog.CANCEL) { // FIXME
			 fileMD.remove();
			 TableKeeper.remove(path);
		}

		try {
			Util.showView(CSVTableView.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		CSVTableView csvTableView = (CSVTableView) Util.findView(CSVTableView.ID);

		assert csvTableView != null : "csvTableView cannot be null";
		csvTableView.update(path);
//		String title = csvTableView.getTitle();
//		System.out.println("title= " + title);
		
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
