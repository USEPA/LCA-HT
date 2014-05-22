package harmonizationtool.handler;

import gov.epa.nrmrl.std.lca.ht.perspectives.FlowData;
import gov.epa.nrmrl.std.lca.ht.perspectives.OriginalPerspective;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class ShowPerspectiveHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbench wb = PlatformUI.getWorkbench();

		IPerspectiveRegistry perspectiveRegistry = wb.getPerspectiveRegistry();
		IPerspectiveDescriptor[] perspectiveDescriptors = perspectiveRegistry.getPerspectives();
		System.out.println("hello!");
		for (IPerspectiveDescriptor ipd : perspectiveDescriptors) {
			System.out.println("============");
			System.out.println("ipd.getId() " + ipd.getId());
			System.out.println("ipd.getLabel() " + ipd.getLabel());
			System.out.println("ipd.toString() " + ipd.toString());
		}

		// try {
		IWorkbenchPage iWorkbenchPage = wb.getActiveWorkbenchWindow().getActivePage();
		IPerspectiveDescriptor iPerspectiveDescriptor = iWorkbenchPage.getPerspective();
		String id = iPerspectiveDescriptor.getId();

		System.out.println("id " + id);
		if (id.equals(FlowData.PID)) {
			System.out.println("Matched FlowData!");
			iWorkbenchPage.setPerspective(perspectiveRegistry.findPerspectiveWithId(OriginalPerspective.PID));
		} else if (id.equals(OriginalPerspective.PID)) {
			System.out.println("Matched Original Perspective!");
			iWorkbenchPage.setPerspective(perspectiveRegistry.findPerspectiveWithId(FlowData.PID));
		} else {
			System.out.println("No match, dude.  Sorry!");

		}

		System.out.println("now id is " + iPerspectiveDescriptor.getId());
		// }
		// } catch (WorkbenchException e) {
		// e.printStackTrace();
		// }

		// System.out.println(ipd);
		// }
		return null;
	}

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

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
