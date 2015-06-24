package gov.epa.nrmrl.std.lca.ht.userInterfacePerspectives;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.MatchContexts;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.MatchProperties;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.MatchFlowables;
import gov.epa.nrmrl.std.lca.ht.log.LoggerViewer;
import gov.epa.nrmrl.std.lca.ht.output.HarmonizedDataSelector;
import gov.epa.nrmrl.std.lca.ht.sparql.QueryView;
import gov.epa.nrmrl.std.lca.ht.sparql.ResultsView;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IFolderLayout;

public class LCIWorkflowPerspective implements IPerspectiveFactory {

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.userInterfacePerspectives.LCIWorkflowPerspective";

	public void createInitialLayout(IPageLayout layout) {
//		addFastViews(layout);
		String editorArea = layout.getEditorArea();
		layout.setFixed(false);
		layout.setEditorAreaVisible(false);

		IFolderLayout workflowLayout = layout.createFolder("Workflow", IPageLayout.LEFT, 0.1f, editorArea);
		// FLOAT AFTER IPageLayout.LEFT SEEMS TO HAVE NO EFFECT ?!?
		workflowLayout.addView(FlowsWorkflow.ID);

		IFolderLayout dataLayout = layout.createFolder("Data", IPageLayout.RIGHT, 0.25f, "Workflow");
		// FLOAT AFTER IPageLayout.RIGHT ABOVE IS THE WIDTH OF THE FIRST Workflow and Logger WINDOWS
		dataLayout.addView(CSVTableView.ID);
		dataLayout.addView(QueryView.ID);
		dataLayout.addView(HarmonizedDataSelector.ID);

//		addPerspectiveShortcuts(layout);

		IFolderLayout dataMatchFlowables = layout.createFolder("Flowables", IPageLayout.BOTTOM, 0.70f, "Data");
		dataMatchFlowables.addView(MatchFlowables.ID);
		dataMatchFlowables.addView(ResultsView.ID);

		IFolderLayout matchToolLayout = layout.createFolder("Match Tools", IPageLayout.BOTTOM, 0.35f, "Workflow");
		matchToolLayout.addView(LoggerViewer.ID);
		matchToolLayout.addView(MatchContexts.ID);
		matchToolLayout.addView(MatchProperties.ID);

		// FLOAT AFTER IPageLayout.BOTTOM ABOVE IS THE HEIGHT OF THE Workflow WINDOW (RELATIVE TO Logger)

	}

//	private void addPerspectiveShortcuts(IPageLayout layout) {
//		layout.addPerspectiveShortcut(LCIWorkflowPerspective.ID);
//	}
//
//	private void addFastViews(IPageLayout layout) {
//		layout.addFastView(QueryView.ID);
//		layout.addFastView(ResultsView.ID);
//	}
}
