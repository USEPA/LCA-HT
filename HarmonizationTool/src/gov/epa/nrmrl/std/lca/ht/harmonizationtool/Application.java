package gov.epa.nrmrl.std.lca.ht.harmonizationtool;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.prefs.Preferences;

import gov.epa.nrmrl.std.lca.ht.dialog.ChooseDataSetDialog;
import gov.epa.nrmrl.std.lca.ht.dialog.GenericMessageBox;
import gov.epa.nrmrl.std.lca.ht.dialog.StorageLocationDialog;
import gov.epa.nrmrl.std.lca.ht.log.LoggerManager;
import gov.epa.nrmrl.std.lca.ht.preferences.Initializer;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.service.runnable.StartupMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.osgi.util.tracker.ServiceTracker;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) {
		Display display = PlatformUI.createDisplay();
		Preferences osPrefs = Preferences.userNodeForPackage(this.getClass());
		boolean askLocation = osPrefs.getBoolean("lca.chooseWorkspace", true);
		String workspaceDir = osPrefs.get("lca.wsDir", Util.getInitialWorkspaceLocation());
		



		
		if (!askLocation)
			askLocation = !Initializer.initializeDefaultPreferences(workspaceDir);

		while (askLocation) {
			StorageLocationDialog dlg = new StorageLocationDialog(display.getActiveShell(), osPrefs);
			dlg.open();
			if (dlg.getReturnCode() == StorageLocationDialog.RET_CANCEL)
				System.exit(0);
			workspaceDir = osPrefs.get("lca.wsDir", null);
			if (!Initializer.initializeDefaultPreferences(workspaceDir)) {
				StringBuilder b = new StringBuilder();
				b.append("Sorry, but the selected directory is not accessible.  Please ensure the selected directory is available and writeable.");
				String errMsg = b.toString();
				System.out.println("Displaying shell " + null);
				new MessageDialog(null, "Error", null, errMsg, MessageDialog.ERROR, new String[] { "Ok" }, 0).open();
			}
			else {
				askLocation = false;
				
				//Needed to repaint the splash screen after the startup dialog closes.  Don't ask me what it does.
				ServiceTracker<StartupMonitor, StartupMonitor> monitorTracker = new ServiceTracker<StartupMonitor, StartupMonitor>(Activator.ctx, StartupMonitor.class.getName(), null);
				monitorTracker.open();
				try {
						StartupMonitor monitor = monitorTracker.getService();
						if (monitor != null) {
							try {
								monitor.update();
							} catch (Throwable e) {
								// ignore exceptions thrown by the monitor
							}
						}			
				} finally {
					monitorTracker.close();
				}
			}
		}		

		Location instanceLoc = Platform.getInstanceLocation();
		try {
			instanceLoc.set(new URL("file", null, workspaceDir), false);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}  
		
		new LoggerManager();
		LoggerManager.Init();
		ActiveTDB.openTDB();
		ActiveTDB.syncTDBtoLCAHT();
		try {
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
			return IApplication.EXIT_OK;
		} finally {
			display.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		if (!PlatformUI.isWorkbenchRunning())
			return;
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
}
