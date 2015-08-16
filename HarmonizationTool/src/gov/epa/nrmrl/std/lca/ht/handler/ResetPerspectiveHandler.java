package gov.epa.nrmrl.std.lca.ht.handler;

import java.io.File;
import java.util.prefs.Preferences;

import gov.epa.nrmrl.std.lca.ht.harmonizationtool.Application;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;

public class ResetPerspectiveHandler implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
	}

	//Does not need a restart but uses private api copied from org.eclipse.ui.handlers.ResetPerspectiveHandler
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {	
		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		if (activeWorkbenchWindow != null) {
			WorkbenchPage page = (WorkbenchPage) activeWorkbenchWindow.getActivePage();
			if (page != null) {
				IPerspectiveDescriptor descriptor = page.getPerspective();
				if (descriptor != null) {
					boolean offerRevertToBase = false;
					if (descriptor instanceof PerspectiveDescriptor) {
						PerspectiveDescriptor desc = (PerspectiveDescriptor) descriptor;
						offerRevertToBase = desc.isPredefined() && desc.hasCustomDefinition();
					}

					if (offerRevertToBase) {
						String message = "Do you want to restore the Harmonization Tool to its default layout?";
						boolean toggleState = false;
						MessageDialogWithToggle dialog = MessageDialogWithToggle.open(
								MessageDialog.QUESTION, activeWorkbenchWindow.getShell(),
								WorkbenchMessages.RevertPerspective_title, message,
								WorkbenchMessages.RevertPerspective_option, toggleState, null,
								null, SWT.SHEET);
						if (dialog.getReturnCode() == IDialogConstants.YES_ID) {
							if (dialog.getToggleState()) {
								PerspectiveRegistry reg = (PerspectiveRegistry) PlatformUI
										.getWorkbench().getPerspectiveRegistry();
								reg.revertPerspective(descriptor);
							}
							page.resetPerspective();
						}
					} else {
						String message = "Do you want to restore the Harmonization Tool to its default layout?";
						boolean result = MessageDialog.open(MessageDialog.QUESTION,
								activeWorkbenchWindow.getShell(),
								WorkbenchMessages.ResetPerspective_title, message, SWT.SHEET);
						if (result) {
							page.resetPerspective();
						}
					}
				}
			}
		}

		return null;
	}
	
	//Does not use private api but requires restart
	public Object executePublicWithRestart(ExecutionEvent event) throws ExecutionException {
		MessageDialog messageDialog = new MessageDialog(Display.getDefault().getActiveShell(), "Confirm", null,
				"Are you sure you wish to restart the Harmonization Tool and restore all windows to their default layout?",
				MessageDialog.QUESTION, new String[] { "Ok", "Cancel" }, 0);
		if (messageDialog.open() == 0) {
			Preferences osPrefs = Preferences.userNodeForPackage(Application.class);
			String wsDir = osPrefs.get("lca.wsDir", Util.getInitialWorkspaceLocation());
			String windowPrefs = wsDir + File.separator + ".metadata" + File.separator + ".plugins" + File.separator + "org.eclipse.e4.workbench" + File.separator + "workbench.xmi";
			
			new File(windowPrefs).deleteOnExit();
			PlatformUI.getWorkbench().restart();
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
