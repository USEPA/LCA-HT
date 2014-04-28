package harmonizationtool;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IFolderLayout;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
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
			folderLayout.addView("HarmonizationTool.viewData");
			folderLayout.addView("HarmonizationTool.ResultsViewID");
			folderLayout.addView("HarmonizationTool.ResultsTreeEditorID");
//			folderLayout.addView("gov.epa.nrmrl.std.lca.ht.job.JobStatusView");

		}
	}

}
