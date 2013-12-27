package harmonizationtool.handler;

import harmonizationtool.View;
import harmonizationtool.ViewData;
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

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVStrategy;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
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

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("executing Import CSV");
		ModelProvider modelProvider = new ModelProvider();
		long filesizeLong;
		int filesizeInt;
		Calendar filedate_java;
		String filenameStr; // FIXME: SHOULD USE THIS IN THE DATA SET

		FileDialog fileDialog = new FileDialog(HandlerUtil
				.getActiveWorkbenchWindow(event).getShell(), SWT.OPEN);
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
				filenameStr = file.getName();
				filesizeLong = file.length();
				filesizeInt = (int) filesizeLong;
				System.out.println("Size long= " + filesizeLong + ". int = "
						+ filesizeInt);
				filedate_java = Calendar.getInstance();
				filedate_java.setTime(new Date(file.lastModified()));
				System.out.println("filedate_java = "
						+ filedate_java.toString());
				System.out.println("filedate_java timeZone = "
						+ filedate_java.getTimeZone());
			}
		}

		FileReader fileReader = null;
		try {
			fileReader = new FileReader(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (fileReader != null) {
			CSVParser parser = new CSVParser(fileReader,
					CSVStrategy.EXCEL_STRATEGY);
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
		
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		ViewData viewData = (ViewData) page.findView(ViewData.ID);

		String title = viewData.getTitle();
		System.out.println("title= " + title);
		viewData.update(path);
		try {
			Util.showView(ViewData.ID);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
