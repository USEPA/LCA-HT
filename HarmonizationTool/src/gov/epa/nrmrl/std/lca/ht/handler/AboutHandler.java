package gov.epa.nrmrl.std.lca.ht.handler;

import gov.epa.nrmrl.std.lca.ht.dialog.GenericMessageBox;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class AboutHandler implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.handler.AboutHandler";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
//		Logger runLogger = Logger.getLogger("run");
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		String version = Util.getProductVersion();
		new GenericMessageBox(shell, "Version", "You are using HT version "+version+". Its database uses the Jena TDB version "+ActiveTDB.getTDBVersion());
		return null;
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
