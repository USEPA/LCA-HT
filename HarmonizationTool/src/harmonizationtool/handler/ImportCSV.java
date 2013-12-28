package harmonizationtool.handler;

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
		// int filesizeInt;
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
				// filesizeInt = (int) filesizeLong;
				System.out.println("Size long= " + filesizeLong);
				filedateJava = Calendar.getInstance();
				filedateJava.setTime(new Date(file.lastModified()));
				System.out.println("filedateJava = " + filedateJava.toString());
				System.out.println("filedateJava timeZone = " + filedateJava.getTimeZone());
				System.out.println("filedataJava UTC?? = "+filedateJava.getTime());
			}
		}

		FileReader fileReader = null;
		try {
			fileReader = new FileReader(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (fileReader != null) {
			CSVParser parser = new CSVParser(fileReader, CSVStrategy.EXCEL_STRATEGY);
			String[] values = null;
			try {
				values = parser.getLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			while (values != null) {
				// printValues(parser.getLineNumber(),values);
				DataRow dataRow = initDataRow(values);
				modelProvider.addDataRow(dataRow);
				ModelKeeper.saveModelProvider(path, modelProvider);
				// System.out.println(dataRow);
				try {
					values = parser.getLine();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		View view = (View) Util.findView(View.ID);
		view.addFilename(path);

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
		// NOW OPEN DIALOG AND PRE-POPULATE SOME
		CSVImportDialog dialog = new CSVImportDialog(Display.getCurrent().getActiveShell());
		// dialog.setBytes(filesizeInt.)
		dialog.setFilename(fileNameStr);
		dialog.setBytes("" + filesizeLong);
		dialog.setLastModified(filedateJava.getTime()+"");

		dialog.create();

		if (dialog.open() == Window.OK) {
			Map<String, Object> metaMap = dialog.getMetaMap();
			modelProvider.setMetaMap(metaMap);
			System.out.println("yeah");
		}

//		 String dataSourceLid = dialog.getDataSourceLid();
		// String dataSourceName = dialog.getDataSourceName();
		// String majorNumber = dialog.getMajorVersion();
		// String minorNumber = dialog.getMinorVersion();
		// String comment = dialog.getComment();
		//
		// System.out.println(dataSourceLid);
		// System.out.println(dataSourceName);
		// System.out.println(majorNumber);
		// System.out.println(minorNumber);
		// System.out.println(comment);
		// }
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
