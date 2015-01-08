package gov.epa.nrmrl.std.lca.ht.handler;

import gov.epa.nrmrl.std.lca.ht.log.LoggerViewer;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.ui.PartInitException;

public class GenericShowViewHandler implements IHandler {

	public static String ID;
	public static String getID() {
		return ID;
	}

	public static void setID(String iD) {
		ID = iD;
	}

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			String viewID = event.getParameter("HarmonizationTool.commandParameter1");
			System.out.println("viewID = " + viewID);
			viewID = event.getParameter("Name");
			System.out.println("viewID = " + viewID);
			Util.showView(LoggerViewer.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
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
