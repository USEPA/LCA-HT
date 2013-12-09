package harmonizationtool.handler;

import harmonizationtool.ResultsView;
import harmonizationtool.QueryView.QueryViewContentProvider;
import harmonizationtool.QueryView.QueryViewLabelProvider;
import harmonizationtool.model.ModelProvider;
import harmonizationtool.query.GenericUpdate;
import harmonizationtool.utils.Util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.ViewPart;

public class ExtUpdateHandler implements IHandler {

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
//	public Object execute(ExecutionEvent event) throws ExecutionException {
				System.out.println("executing an external update");
//				ModelProvider modelProvider = new ModelProvider();
				FileDialog fileDialog = new FileDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), SWT.OPEN);
				fileDialog.setFilterExtensions(new String[] { "*.rq" });
				String homeDir = System.getProperty("user.home");
				String workingDir = Util.getPreferenceStore().getString(
						"workingDir");
				if (workingDir.length() > 0) {
					fileDialog.setFilterPath(workingDir);
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

					String queryStr = b.toString();
					GenericUpdate iGenericInsert = new GenericUpdate(queryStr,"Ext. File Update");

//					addFilename(path);
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					ResultsView resultsView = (ResultsView) page.findView(ResultsView.ID);
					String title = resultsView.getTitle();
					System.out.println("title= " + title);

					resultsView.update(iGenericInsert.getData());
					resultsView.update(iGenericInsert.getQueryResults());
					// ViewData.setKey(path);
					// TableViewer tableViewer = viewData.getViewer();
					// tableViewer.setInput(new Object[] {""});
//					resultsView.update(path);

				}

//		actionExtUpdate.setText("Exec. Update...");
//		actionExtUpdate.setToolTipText("SPARQL Update in .ttl file");
//		actionExtUpdate.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
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
