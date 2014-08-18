package gov.epa.nrmrl.std.lca.ht.perspectives;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.MatchFlowableTableView;
import gov.epa.nrmrl.std.lca.ht.log.LoggerViewer;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IFolderLayout;

public class FlowDataWMatchFlowables implements IPerspectiveFactory {

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.perspectives.FlowDataWMatchFlowables";
//	public static final String PID = "<FlowData>";

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setFixed(false);
		layout.setEditorAreaVisible(false);

		IFolderLayout controlFolder = layout.createFolder("Control and Status", IPageLayout.LEFT, 0.25f, editorArea);
		controlFolder.addView(FlowsWorkflow.ID);
		layout.addView(LoggerViewer.ID, IPageLayout.BOTTOM, 0.75f, "Control and Status");

		IFolderLayout dataFolder = layout.createFolder("CSV File", IPageLayout.RIGHT, 0.95f, editorArea);
		dataFolder.addView(CSVTableView.ID);
		layout.addView(MatchFlowableTableView.ID, IPageLayout.BOTTOM, 0.75f, "CSV File");

		addPerspectiveShortcuts(layout);

	}

	private void addPerspectiveShortcuts(IPageLayout layout) {
		layout.addPerspectiveShortcut(FlowDataWMatchFlowables.ID);
		layout.addPerspectiveShortcut(OriginalPerspective.ID);
	}
}
