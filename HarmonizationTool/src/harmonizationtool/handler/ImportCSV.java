package harmonizationtool.handler;

import harmonizationtool.QueryView;
import harmonizationtool.View;
import harmonizationtool.ViewData;
import harmonizationtool.dialog.CSVImportDialog;
import harmonizationtool.dialog.MyDialog;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.ModelKeeper;
import harmonizationtool.model.ModelProvider;
import harmonizationtool.utils.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVStrategy;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
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
		ModelProvider modelProvider = new ModelProvider();
		long filesizeLong = 0;
		int filesizeInt = 0;
		Calendar filedateJava = null;
		String fileNameStr = null; // FIXME: SHOULD USE THIS IN THE DATA SET

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
		if (path != null) {
			File file = new File(path);
			if (file.exists()) {
				fileNameStr = file.getName();
				System.out.println("parsed filename as:" + fileNameStr);
				filesizeLong = file.length();
				filesizeInt = (int) filesizeLong;
				System.out.println("Size long= " + filesizeLong);
				filedateJava = Calendar.getInstance();
				filedateJava.setTime(new Date(file.lastModified()));
				System.out.println("filedateJava = " + filedateJava.toString());
				System.out.println("filedateJava timeZone = " + filedateJava.getTimeZone());
				System.out.println("filedataJava UTC?? = " + filedateJava.getTime());
			}
		}

		FileReader fileReader = null;
		try {
			fileReader = new FileReader(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (fileReader == null) {
			String msg = "Can not read CSV file!";
			Util.findView(View.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
			System.out.println(msg);
			return null;
		}
		CSVParser parser = new CSVParser(fileReader, CSVStrategy.EXCEL_STRATEGY);
		String[] values = null;
		try {
			values = parser.getLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (values == null) {
			String msg = "No content in CSV file!";
			Util.findView(View.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
			System.out.println(msg);
			return null;
		}
		// IF WE GOT CONTENT, THEN SAVE THIS FILE (MODEL) AND ADD IT TO THE MENU
		ModelKeeper.saveModelProvider(path, modelProvider); // SUPPOSED TO SAVE ONLY IF
															// CONTENT, BUT DOESN'T WORK

		View view = (View) Util.findView(View.ID);
		view.addFilename(path);

		while (values != null) {
			// printValues(parser.getLineNumber(),values);
			DataRow dataRow = initDataRow(values);
			modelProvider.addDataRow(dataRow);
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
		String msg = "Read file: " + path;
		Util.findView(View.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);

		// NOW OPEN DIALOG AND PRE-POPULATE SOME
		CSVImportDialog dialog = new CSVImportDialog(Display.getCurrent().getActiveShell());
		dialog.metaData.put("fileName", fileNameStr);
		dialog.metaData.put("fileSize", "" + filesizeLong);
		dialog.metaData.put("fileLastModified", filedateJava.getTime().toString());
		// dialog.setFilename(fileNameStr);
		// dialog.setBytes("" + filesizeLong);
		// dialog.setLastModified(filedateJava.getTime() + "");

		dialog.create();

		if (dialog.open() == Window.OK) {
			Iterator<String> keySeq = dialog.metaData.keySet().iterator();
			Iterator<Text> valueSeq = dialog.dialogValues.iterator();
			while (valueSeq.hasNext()) {
				Text textBox = valueSeq.next();
				String key = keySeq.next();
				// String metaValue = dialog.metaData.get(key);
				dialog.metaData.put(key, textBox.toString());
			}
			modelProvider.metaData = dialog.metaData;
			// Map<String, Object> metaMap = dialog.getMetaMap();
			// System.out.println("metaMap: "+ metaMap.toString());
			// System.out.println("filename: "+ metaMap.get("filename").toString());
			//
			// modelProvider.setMetaMap(metaMap);
			System.out.println("yeah");
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
