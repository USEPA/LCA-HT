package gov.epa.nrmrl.std.lca.ht.perspectives;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IFolderLayout;

public class OriginalPerspective implements IPerspectiveFactory {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.perspectives.OriginalPerspective";
	public static final String PID = "<Original_Perspective>";

	public void createInitialLayout(IPageLayout layout) {
		addPerspectiveShortcuts(layout);
		String editorArea = layout.getEditorArea();
		layout.setFixed(false);
		layout.setEditorAreaVisible(false);
		{
			IFolderLayout folderLayout = layout.createFolder("folder1", IPageLayout.LEFT, 0.65f, editorArea);

			folderLayout.addView("HarmonizationTool.view");
			folderLayout.addView("HarmonizationTool.QueryViewID");
		}
		{
			IFolderLayout folderLayout = layout.createFolder("folder3", IPageLayout.LEFT, 0.8f, editorArea);
			folderLayout.addView("gov.epa.nrmrl.std.lca.ht.csvFiles.csvTableView");
			folderLayout.addView("HarmonizationTool.ResultsViewID");
			folderLayout.addView("HarmonizationTool.ResultsTreeEditorID");
//			folderLayout.addView("gov.epa.nrmrl.std.lca.ht.job.JobStatusView");

		}
	}

	private void addPerspectiveShortcuts(IPageLayout layout) {
		layout.addPerspectiveShortcut(OriginalPerspective.ID);
		layout.addPerspectiveShortcut(FlowData.ID);
	}
}
