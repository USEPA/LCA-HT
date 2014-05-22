package gov.epa.nrmrl.std.lca.ht.perspectives;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IFolderLayout;

public class FlowData implements IPerspectiveFactory {

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.perspectives.FlowData";
	public static final String PID = "<FlowData>";

	public void createInitialLayout(IPageLayout layout) {
		addPerspectiveShortcuts(layout);
		String editorArea = layout.getEditorArea();
		layout.setFixed(false);
		layout.setEditorAreaVisible(false);

		IFolderLayout controlFolder = layout.createFolder("Control and Status", IPageLayout.LEFT, 0.25f, editorArea);
		controlFolder.addView("gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow");
		layout.addView("harmonizationtool.console.LoggerViewer", IPageLayout.BOTTOM, 0.75f, "Control and Status");

		IFolderLayout dataFolder = layout.createFolder("CSV File", IPageLayout.LEFT, 0.71f, editorArea);
		dataFolder.addView("gov.epa.nrmrl.std.lca.ht.csvFiles.csvTableView");
		// folderLayout.addView("gov.epa.nrmrl.std.lca.ht.job.JobStatusView");

	}

	private void addPerspectiveShortcuts(IPageLayout layout) {
		layout.addPerspectiveShortcut(FlowData.ID);
		layout.addPerspectiveShortcut(OriginalPerspective.ID);
	}
}
