package gov.epa.nrmrl.std.lca.ht.harmonizationtool;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.IServiceLocator;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(1400, 850));
		// SIZE OF START SCREEN
		configurer.setShowCoolBar(false);
		configurer.setShowStatusLine(true);
	}

	@Override
	public void postWindowCreate() {
		System.out.println("ApplicationWorkbenchWindowAdvisor.postWindowCreate()");
		super.postWindowCreate();
	}

	@Override
	public void postWindowOpen() {
		System.out.println("ApplicationWorkbenchWindowAdvisor.postWindowOpen()");
		try {
			IServiceLocator serviceLocator = PlatformUI.getWorkbench();
			ICommandService commandService = (ICommandService) serviceLocator.getService(ICommandService.class);
			Command command = commandService.getCommand(ActiveTDB.ID);
			command.executeWithChecks(new ExecutionEvent());
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (NotDefinedException e) {
			e.printStackTrace();
		} catch (NotEnabledException e) {
			e.printStackTrace();
		} catch (NotHandledException e) {
			e.printStackTrace();
		}
		super.postWindowOpen();
	}

	@Override
	public boolean preWindowShellClose() {
		System.out.println("ApplicationWorkbenchWindowAdvisor.preWindowShellClose()");
		try {
			IServiceLocator serviceLocator = PlatformUI.getWorkbench();
			ICommandService commandService = (ICommandService) serviceLocator.getService(ICommandService.class);
			Command command = commandService.getCommand("gov.epa.nrmrl.std.lca.ht.handler.CloseTDB");
			command.executeWithChecks(new ExecutionEvent());
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (NotDefinedException e) {
			e.printStackTrace();
		} catch (NotEnabledException e) {
			e.printStackTrace();
		} catch (NotHandledException e) {
			e.printStackTrace();
		}
		
		return super.preWindowShellClose();
	}

	@Override
	public void postWindowClose() {
		System.out.println("ApplicationWorkbenchWindowAdvisor.postWindowClose()");
//		try {
//			IServiceLocator serviceLocator = PlatformUI.getWorkbench();
//			ICommandService commandService = (ICommandService) serviceLocator.getService(ICommandService.class);
//			Command command = commandService.getCommand("gov.epa.nrmrl.std.lca.ht.handler.CloseTDB");
//			command.executeWithChecks(new ExecutionEvent());
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		} catch (NotDefinedException e) {
//			e.printStackTrace();
//		} catch (NotEnabledException e) {
//			e.printStackTrace();
//		} catch (NotHandledException e) {
//			e.printStackTrace();
//		}
		super.postWindowClose();
	}
}
