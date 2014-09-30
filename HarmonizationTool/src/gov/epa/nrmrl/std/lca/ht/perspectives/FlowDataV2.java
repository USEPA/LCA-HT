package gov.epa.nrmrl.std.lca.ht.perspectives;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.MatchContexts;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.MatchPropertiesOld;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.MatchFlowableTableView;
import gov.epa.nrmrl.std.lca.ht.log.LoggerViewer;
import gov.epa.nrmrl.std.lca.ht.sparql.QueryView;
import gov.epa.nrmrl.std.lca.ht.sparql.ResultsView;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IFolderLayout;

public class FlowDataV2 implements IPerspectiveFactory {

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.perspectives.FlowDataV2";

	public void createInitialLayout(IPageLayout layout) {
		addFastViews(layout);
		String editorArea = layout.getEditorArea();
		layout.setFixed(false);
		layout.setEditorAreaVisible(false);

		IFolderLayout workflowLayout = layout.createFolder("Workflow", IPageLayout.LEFT, 0.1f, editorArea);
		// FLOAT AFTER IPageLayout.LEFT SEEMS TO HAVE NO EFFECT ?!?
		workflowLayout.addView(FlowsWorkflow.ID);
		{
			IFolderLayout matchToolLayout = layout.createFolder("Match Tools", IPageLayout.BOTTOM, 0.6f, "Workflow");
			// FLOAT AFTER IPageLayout.BOTTOM ABOVE IS HEIGHT OF CSVTableView WINDOW (RELATIVE TO MatchTool WINDOWS)

			matchToolLayout.addView(MatchFlowableTableView.ID);

		}

		IFolderLayout dataLayout = layout.createFolder("Data", IPageLayout.RIGHT, 0.25f, "Workflow");
		// FLOAT AFTER IPageLayout.RIGHT ABOVE IS THE WIDTH OF THE FIRST Workflow and Logger WINDOWS
		dataLayout.addView(CSVTableView.ID);
		dataLayout.addView(MatchContexts.ID);
		dataLayout.addView(MatchPropertiesOld.ID);

		addPerspectiveShortcuts(layout);
		layout.addView(LoggerViewer.ID, IPageLayout.BOTTOM, 0.7f, "Workflow");
		// FLOAT AFTER IPageLayout.BOTTOM ABOVE IS THE HEIGHT OF THE Workflow WINDOW (RELATIVE TO Logger)

	}

	private void addPerspectiveShortcuts(IPageLayout layout) {
		layout.addPerspectiveShortcut(FlowDataV2.ID);
		layout.addPerspectiveShortcut(OriginalPerspective.ID);
	}

	private void addFastViews(IPageLayout layout) {
		layout.addFastView(QueryView.ID);
		layout.addFastView(ResultsView.ID);
	}
}
