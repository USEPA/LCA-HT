package harmonizationtool.handler;

import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceKeeper;
import harmonizationtool.dialog.GenericMessageBox;
import harmonizationtool.dialog.MetaDataDialog;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

public class EditMetadataHandler implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (DataSourceKeeper.size() == 0) {
			new GenericMessageBox(HandlerUtil.getActiveShell(event), "No Data Sets",
					"The HT does not contain any DataSources at this time.  Read a CSV or RDF file to create some.");
			return null;
		}
		MetaDataDialog dialog = new MetaDataDialog(HandlerUtil.getActiveShell(event));
		dialog.create();
		dialog.open();
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
