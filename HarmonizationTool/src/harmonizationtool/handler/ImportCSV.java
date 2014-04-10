package harmonizationtool.handler;

import harmonizationtool.View;
import harmonizationtool.ViewData;
import harmonizationtool.dialog.MetaDataDialog;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.FileMD;
import harmonizationtool.model.TableKeeper;
import harmonizationtool.model.TableProvider;
import harmonizationtool.utils.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVStrategy;
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

	// public String fileNameStr;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("executing Import CSV");
		// ModelProvider modelProvider = new ModelProvider();
		TableProvider tableProvider = new TableProvider();
		FileMD fileMD = new FileMD();

		FileDialog fileDialog = new FileDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), SWT.OPEN);
		fileDialog.setFilterExtensions(new String[] { "*.csv" });
		String workingDir = Util.getPreferenceStore().getString("workingDir");
		if (workingDir.length() > 0) {
			fileDialog.setFilterPath(workingDir);
		} else {
			String homeDir = System.getProperty("user.home");
			fileDialog.setFilterPath(homeDir);
		}

		String path = fileDialog.open();
		File file = null;
		if (path != null) {
			file = new File(path);

			if (!file.exists()) {

				String msg = "File does not exist!";
				Util.findView(View.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
				System.out.println(msg);
				return null;
			}
		}

		// FileReader fileReader = null;
		// FileInputStream fileInputStream = null;
		BufferedReader bufferedReader = null;
		try {
			// fileReader = new FileReader(path);
//			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF8"));
//			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "Cp1252"));
//			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "MacRoman"));
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "ISO8859_1"));
			bufferedReader.mark(50000);

			// System.out.println("fileReader.getEncoding()= " + fileReader.getEncoding());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// if (fileReader == null) {
		if (bufferedReader == null) {
			String msg = "Can not read CSV file!";
			Util.findView(View.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
			System.out.println(msg);
			return null;
		}
		// BufferedReader bufferedReader = new BufferedReader(fileReader);
		try {
			String nonAsciiRegexString = "[^ -~]";
//			String nonAsciiRegexString = "[^\\p{ASCII}]";
			
			Pattern nonAsciiPattern = Pattern.compile(nonAsciiRegexString);
			int lineNumber = 0;
			while (bufferedReader.ready()) {
				String line = bufferedReader.readLine();
				lineNumber++;
//				System.out.println("Screening line number: " + lineNumber+": "+line);
////				if (line.matches(nonAsciiRegexString)) {
//					System.out.println("line: " + line + " contains non-ascii");
//					//
					Matcher matcher = nonAsciiPattern.matcher(line);
//					int group = -1;
					while (matcher.find()) {
//						group++;
						String nonAsciiChar = matcher.group();
						int unicodeCharNumber = nonAsciiChar.codePointAt(0);
						int colNumber = matcher.end();
						System.out.println("On " + lineNumber + ", column "+colNumber+" found character number: " +unicodeCharNumber +" which looks like:" + nonAsciiChar);
					}
//				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			bufferedReader.reset();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		CSVParser parser = new CSVParser(bufferedReader, CSVStrategy.EXCEL_STRATEGY);
		String[] values = null;
		try {
			values = parser.getLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (values == null) { // BLANK FILE STILL HAS values (BUT ZERO LENGTH)
			String msg = "No content in CSV file!";
			Util.findView(View.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
			System.out.println(msg);
			return null;
		}
		fileMD.setFilename(file.getName());
		fileMD.setPath(path);
		fileMD.setSize(file.length());
		fileMD.setLastModified(new Date(file.lastModified()));
		fileMD.setReadTime(new Date());

		// IF WE GOT CONTENT, THEN SAVE THIS FILE (MODEL) AND ADD IT TO THE MENU
		TableKeeper.saveTableProvider(path, tableProvider);

		View view = (View) Util.findView(View.ID);
		view.add(fileMD);

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

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ViewData viewData = (ViewData) page.findView(ViewData.ID);

		String title = viewData.getTitle();
		System.out.println("title= " + title);
		viewData.update(path);

		// BRING UP THE DATA FILE VIEW
		try {
			Util.showView(View.ID);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// ... AND BRING UP THE DATA CONTENTS VIEW

		try {
			Util.showView(ViewData.ID);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String msg = "Finished reading file: " + path;
		Util.findView(View.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);

		// NOW OPEN DIALOG WITH SOME PRE-POPULATE fileMD INFO
		MetaDataDialog dialog = new MetaDataDialog(Display.getCurrent().getActiveShell(), fileMD);

		dialog.create();
		if (dialog.open() == MetaDataDialog.CANCEL) {
			// remove fileMD from Data Files viewer
			view.remove(fileMD);

		}

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
