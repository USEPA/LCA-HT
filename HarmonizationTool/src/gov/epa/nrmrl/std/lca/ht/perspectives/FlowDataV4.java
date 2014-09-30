package gov.epa.nrmrl.std.lca.ht.perspectives;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IFolderLayout;

public class FlowDataV4 implements IPerspectiveFactory {

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.perspectives.FlowDataV4";

	public void createInitialLayout(IPageLayout layout) {
		addFastViews(layout);
		String editorArea = layout.getEditorArea();
		layout.setFixed(false);
		layout.setEditorAreaVisible(false);

		IFolderLayout matchToolLayout = layout.createFolder("Control and Matching", IPageLayout.LEFT, 0.30f, editorArea);
		matchToolLayout.addView(FlowsWorkflow.ID);
		{
			IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.BOTTOM, 0.4f, "Control and Matching");
			folderLayout.addView("gov.epa.nrmrl.std.lca.ht.log.LoggerViewer");
			folderLayout.addView("gov.epa.nrmrl.std.lca.ht.flowContext.mgr.MatchContexts");
			folderLayout.addView("gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.MatchProperties");
			folderLayout.addView("gov.epa.nrmrl.std.lca.ht.flowable.MatchFlowableTableView");
		}

		IFolderLayout dataLayout = layout.createFolder("Data and SPARQL", IPageLayout.RIGHT, 0.01f, editorArea);
		// FLOAT AFTER IPageLayout.RIGHT ABOVE IS THE WIDTH OF THE FIRST Workflow and Logger WINDOWS
		dataLayout.addView(CSVTableView.ID);

		addPerspectiveShortcuts(layout);
	}

	private void addPerspectiveShortcuts(IPageLayout layout) {
		layout.addPerspectiveShortcut(FlowDataV4.ID);
		layout.addPerspectiveShortcut(OriginalPerspective.ID);
	}

	private void addFastViews(IPageLayout layout) {
	}
}
