package gov.epa.nrmrl.std.lca.ht.perspectives;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.MatchContexts;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.MatchFlowables;
import gov.epa.nrmrl.std.lca.ht.log.LoggerViewer;
import gov.epa.nrmrl.std.lca.ht.sparql.QueryView;
import gov.epa.nrmrl.std.lca.ht.sparql.ResultsView;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IFolderLayout;

public class FlowDataV3 implements IPerspectiveFactory {

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.perspectives.FlowDataV3";

	public void createInitialLayout(IPageLayout layout) {
		addFastViews(layout);
		String editorArea = layout.getEditorArea();
		layout.setFixed(false);
		layout.setEditorAreaVisible(false);

		IFolderLayout matchToolLayout = layout.createFolder("Control and Matching", IPageLayout.LEFT, 0.4f, editorArea);
		matchToolLayout.addView(FlowsWorkflow.ID);
		matchToolLayout.addView(MatchFlowables.ID);
		matchToolLayout.addView(MatchContexts.ID);
		matchToolLayout.addView(LoggerViewer.ID);

		IFolderLayout dataLayout = layout.createFolder("Data and SPARQL", IPageLayout.RIGHT, 0.01f, editorArea);
		// FLOAT AFTER IPageLayout.RIGHT ABOVE IS THE WIDTH OF THE FIRST Workflow and Logger WINDOWS
		dataLayout.addView(CSVTableView.ID);
		dataLayout.addView(QueryView.ID);
		dataLayout.addView(ResultsView.ID);

		addPerspectiveShortcuts(layout);
	}

	private void addPerspectiveShortcuts(IPageLayout layout) {
		layout.addPerspectiveShortcut(FlowDataV3.ID);
		layout.addPerspectiveShortcut(OriginalPerspective.ID);
	}

	private void addFastViews(IPageLayout layout) {
	}
}
