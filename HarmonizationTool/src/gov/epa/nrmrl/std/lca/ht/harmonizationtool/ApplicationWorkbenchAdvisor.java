package gov.epa.nrmrl.std.lca.ht.harmonizationtool;

import gov.epa.nrmrl.std.lca.ht.perspectives.LCIWorkflowPerspective;

import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	@Override
	public void postStartup() {
	    PreferenceManager pm = PlatformUI.getWorkbench().getPreferenceManager( );
	    pm.remove( "org.eclipse.m2e.core.preferences.Maven2PreferencePage" );
	    pm.remove( "org.eclipse.ui.preferencePages.Workbench" );
	}
	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		System.out.println("configurer.setSaveAndRestore(true)");
		configurer.setSaveAndRestore(true);
		super.initialize(configurer);
	}

	public String getInitialWindowPerspectiveId() {
//		return FlowDataV1.ID;
//		return FlowDataV4.ID;
		return LCIWorkflowPerspective.ID;
//		return OriginalPerspective.ID;
	}

}
