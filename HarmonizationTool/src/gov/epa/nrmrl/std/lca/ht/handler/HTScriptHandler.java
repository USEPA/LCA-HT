package gov.epa.nrmrl.std.lca.ht.handler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import gov.epa.nrmrl.std.lca.ht.sparql.GenericQuery;
import gov.epa.nrmrl.std.lca.ht.sparql.ResultsView;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.handlers.HandlerUtil;

public class HTScriptHandler implements IHandler {
    // THIS CLASS IS A COPY OF ExtQuerHandler BUT HAS NOT BEEN EDITED YET
	// THE GOAL IS TO HAVE ALLOW THE HT TO READ A "SCRIPT" FILE WITH A SEQUENCE OF COMMANDS
	// E.G.: LOAD file1.n3, LOAD file2.n3, LOAD file3.n3, RUN update1.rq AND update2.rq, THEN DISPLAY RESULTS OF query1.rq
	// TODO: DETERMINE WHAT "COMMAND NAMES" ARE, WHAT THE SHOULD SO, AND WHAT PARAMETERS THEY REQUIRE
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
		System.out.println("executing an external query");
//		ModelProvider modelProvider = new ModelProvider();
		FileDialog fileDialog = new FileDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), SWT.OPEN);
		fileDialog.setFilterExtensions(new String[] { "*.txt" });
		String homeDir = System.getProperty("user.home");
		fileDialog.setFilterPath(homeDir);
		String path = fileDialog.open();
		if (path != null) {
			StringBuilder b = new StringBuilder();
			FileReader fileReader = null;
			try {
				fileReader = new FileReader(path);
				BufferedReader br = new BufferedReader(fileReader);
				String s;

				while ((s = br.readLine()) != null) {
					// System.out.println(s);
					b.append(s+"\n");
				}
				fileReader.close();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String queryStr = b.toString();
            GenericQuery iGenericQuery = new GenericQuery(queryStr,"Ext. File Query");

//			addFilename(path);
//			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			ResultsView resultsView = (ResultsView) Util.findView(ResultsView.ID);
			String title = resultsView.getTitle();
			System.out.println("title= " + title);

			resultsView.update(iGenericQuery.getData());
			resultsView.update(iGenericQuery.getQueryResults());
			// CSVTableView.setKey(path);
			// TableViewer tableViewer = csvTableView.getViewer();
			// tableViewer.setInput(new Object[] {""});
//			resultsView.update(path);
		}
//		actionExtQuery.setText("Exec. Query...");
//		actionExtQuery.setToolTipText("SPARQL Query in .ttl file");
//		actionExtQuery.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
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
