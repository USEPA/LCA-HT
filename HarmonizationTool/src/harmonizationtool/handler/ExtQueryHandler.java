package harmonizationtool.handler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.views.QueryView;
import gov.epa.nrmrl.std.lca.ht.views.ResultsView;
import harmonizationtool.model.ModelProvider;
import harmonizationtool.query.GenericQuery;
import harmonizationtool.query.HarmonyQuery2Impl;
import harmonizationtool.utils.Util;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetRewindable;

public class ExtQueryHandler implements IHandler {

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
		fileDialog.setFilterExtensions(new String[] { "*.rq" });
		String homeDir = System.getProperty("user.home");
		String workingDirectory = Util.getPreferenceStore().getString(
				"workingDirectory");
		if (workingDirectory.length() > 0) {
			fileDialog.setFilterPath(workingDirectory);
		} else {
			fileDialog.setFilterPath(homeDir);
		}
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

			//--------------------------- NOW PUT QUERY INTO QUERY WINDOW ------
			String queryStr = b.toString();
//			{
//			THIS IS NOW IN THE GenericQuery CLASS
//				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//				QueryView queryView = (QueryView) page.findView(QueryView.ID);
//				queryView.setTextAreaContent(queryStr);
////				System.out.println("Contents of window: = "+ queryView.toString());
//			}
			
			//--------------------------- NOW RUN THE THING ------
			
			

			
//            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			ResultsView resultsView = (ResultsView) Util.findView(ResultsView.ID);
			String title = resultsView.getTitle();
			System.out.println("title= " + title);

			HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
			harmonyQuery2Impl.setQuery(queryStr);
			ResultSet resultSet = ((HarmonyQuery2Impl) harmonyQuery2Impl)
					.getResultSet();

			TableProvider tableProvider = TableProvider
					.create((ResultSetRewindable) resultSet);
			resultsView.update(tableProvider);
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
