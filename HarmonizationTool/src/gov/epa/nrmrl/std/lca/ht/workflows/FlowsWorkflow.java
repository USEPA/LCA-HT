package gov.epa.nrmrl.std.lca.ht.workflows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataPropertyProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dialog.GenericMessageBox;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.FlowContext;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.MatchContexts;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.FlowProperty;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.MatchProperties;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.Flowable;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.MatchFlowables;
import gov.epa.nrmrl.std.lca.ht.job.AutoMatchJob;
import gov.epa.nrmrl.std.lca.ht.job.AutoMatchJobChangeListener;
import gov.epa.nrmrl.std.lca.ht.log.LoggerViewer;
import gov.epa.nrmrl.std.lca.ht.output.SaveHarmonizedDataForOLCAJsonld;
import gov.epa.nrmrl.std.lca.ht.output.SaveHarmonizedDataForOLCAJsonldZip;
import gov.epa.nrmrl.std.lca.ht.userInterfacePerspectives.LCIWorkflowPerspective;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.wb.swt.SWTResourceManager;

//import org.eclipse.swt.events.SelectionAdapter;

/**
 * The Flows Workflow is the first example of a workflow in the Harmonization Tool.  In the future, there will be many workflows that users can utilize.  The first operation that
 * the user will perform once the Harmonization Tool is running and they have selected their working directories resides within the window produced in this class. 
 * The window is called the Inventory Workflow (the window name can be found within the plugin.xml file).
 *
 * @author Tom Transue
 */
public class FlowsWorkflow extends ViewPart {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow";

	private Label lblUserData;
	private Label lblFormat;
	private Label lblSave;
	private Label lblContext;
	private Label lblUnit;
	private Label lblFlowable;
	private Label lblFinalize;

	private static String matchContextsString = "Show Unique";
	private static String matchPropertiesString = "Show Unique";
	private static String matchFlowablesString = "Show Unique";

	public static Button btnLoadUserData;
	public static Button btnCheckData;
	public static Button btnCommit;
	public static Button btnMatchFlowables;
	public static Button btnMatchFlowContexts;
	public static Button btnMatchFlowProperties;
	public static Button btnConcludeFile;

	public static StyledText statusLoadUserData;
	public static StyledText statusCheckData;
	public static StyledText statusSaveMatch;
	public static StyledText statusFlowable;
	public static StyledText statusFlowContext;
	public static StyledText statusFlowUnit;
	public static StyledText statusConclude;

	private static Map<Button, Boolean> buttonState = new HashMap<Button, Boolean>();

	// enable / restore all buttons are meant to be called while commands are running to prevent multiple invocations
	public static void disableAllButtons() {
		buttonState.put(btnLoadUserData, btnLoadUserData.isEnabled());
		buttonState.put(btnCheckData, btnCheckData.isEnabled());
		buttonState.put(btnCommit, btnCommit.isEnabled());
		buttonState.put(btnMatchFlowables, btnMatchFlowables.isEnabled());
		buttonState.put(btnMatchFlowContexts, btnMatchFlowContexts.isEnabled());
		buttonState.put(btnMatchFlowProperties, btnMatchFlowProperties.isEnabled());
		buttonState.put(btnConcludeFile, btnConcludeFile.isEnabled());
		for (Map.Entry<Button, Boolean> pair : buttonState.entrySet()) {
			pair.getKey().setEnabled(false);
		}
	}

//	public static void restoreAllButtons() {
//		for (Map.Entry<Button, Boolean> pair : buttonState.entrySet()) {
//			pair.getKey().setEnabled(pair.getValue());
//		}
//		buttonState.clear();
//	}

	private static SortedSet<Integer> uniqueFlowableRowNumbers = new TreeSet<Integer>();
	private static SortedSet<Integer> uniqueFlowContextRowNumbers = new TreeSet<Integer>();
	private static SortedSet<Integer> uniqueFlowPropertyRowNumbers = new TreeSet<Integer>();
	private static SortedSet<Integer> uniqueFlowRowNumbers = new TreeSet<Integer>();

	private static SortedSet<Integer> matchedFlowableRowNumbers = new TreeSet<Integer>();
	private static SortedSet<Integer> matchedFlowContextRowNumbers = new TreeSet<Integer>();
	private static SortedSet<Integer> matchedFlowPropertyRowNumbers = new TreeSet<Integer>();
	private static SortedSet<Integer> matchedFlowRowNumbers = new TreeSet<Integer>();

	// private static FileMD fileMD;
	// private static DataSourceProvider dataSourceProvider;

	// private static TableProvider tableProvider;

	public FlowsWorkflow() {
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void createPartControl(Composite parent) {
		initializeOtherViews();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		GridLayout gl_composite = new GridLayout(3, false);
		gl_composite.verticalSpacing = 2;
		composite.setLayout(gl_composite);

		lblStep = new Label(composite, SWT.NONE);
		lblStep.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblStep.setText("Step");

		Label lblActions = new Label(composite, SWT.NONE);
		lblActions.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblActions.setText("Action");

		Label lblStatus = new Label(composite, SWT.NONE);
		lblStatus.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblStatus.setText("Status");

		// ======== ROW 1 =======================

		lblUserData = new Label(composite, SWT.NONE);
		lblUserData.setText("1. User Data");
		lblUserData.setToolTipText("User data includes any dataset the user wishes to harmonize.");
		// lblNewLabel.setSize(100, 30);

		btnLoadUserData = new Button(composite, SWT.NONE);
		GridData gd_btnLoadUD = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnLoadUD.widthHint = 80;
		btnLoadUserData.setLayoutData(gd_btnLoadUD);
		btnLoadUserData.setText("Load...");

		btnLoadUserData.addSelectionListener(loadUserDataListener);

		statusLoadUserData = new StyledText(composite, SWT.BORDER | SWT.READ_ONLY);
		statusLoadUserData.setEnabled(false);
		statusLoadUserData.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));

		GridData gd_textFileInfo = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textFileInfo.widthHint = 150;
		statusLoadUserData.setLayoutData(gd_textFileInfo);

		// ======== ROW 2 =======================

		lblFormat = new Label(composite, SWT.NONE);
		lblFormat.setText("2. Format");
		lblFormat.setToolTipText("Prior to saving and matching LCA data, data formats are checked for common errors.");

		btnCheckData = new Button(composite, SWT.NONE);
		GridData gd_btnCheckData = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnCheckData.widthHint = 80;
		btnCheckData.setLayoutData(gd_btnCheckData);
		btnCheckData.setText("Check Data");
		btnCheckData.setEnabled(false);
		btnCheckData.addSelectionListener(checkDataListener);

		statusCheckData = new StyledText(composite, SWT.BORDER | SWT.READ_ONLY);
		statusCheckData.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		statusCheckData.setEnabled(false);
		// statusCheckData.setText("0 issues");
		GridData gd_textIssues = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textIssues.widthHint = 150;
		statusCheckData.setLayoutData(gd_textIssues);

		// ======== ROW 3 =======================

		lblSave = new Label(composite, SWT.NONE);
		lblSave.setFont(SWTResourceManager.getFont("Tahoma", 8, SWT.NORMAL));
		// Ascii x26 is an ampersand
		lblSave.setText("3. Save+Match");
		lblSave.setToolTipText("In this step, user data is written to the database and matched against master data.  Canceling prior to the completion of this set means that user data is not saved.");

		btnCommit = new Button(composite, SWT.NONE);
		btnCommit.setEnabled(false);
		GridData gd_btnCommit = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnCommit.widthHint = 80;
		btnCommit.setLayoutData(gd_btnCommit);
		btnCommit.setText("Begin");
		btnCommit.addSelectionListener(commitListener);

		statusSaveMatch = new StyledText(composite, SWT.BORDER | SWT.READ_ONLY);
		statusSaveMatch.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		statusSaveMatch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		statusSaveMatch.setEnabled(false);

		// ======== ROW 4 =======================

		lblContext = new Label(composite, SWT.NONE);
		lblContext.setText("4. Flow Context");
		lblContext
				.setToolTipText("The LCA-Harmonization Tool chooses the term \"Flow Context\" to represent a term often called category or compartment.  The Flow Context must match for a user Flow to match a master Flow.");
		btnMatchFlowContexts = new Button(composite, SWT.NONE);
		GridData gd_btnMatchFlowContexts = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		btnMatchFlowContexts.addSelectionListener(matchFlowContextsListener);

		gd_btnMatchFlowContexts.widthHint = 80;
		btnMatchFlowContexts.setLayoutData(gd_btnMatchFlowContexts);
		btnMatchFlowContexts.setText("Show Unique");
		btnMatchFlowContexts.setEnabled(false);

		statusFlowContext = new StyledText(composite, SWT.BORDER | SWT.READ_ONLY);
		statusFlowContext.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		statusFlowContext.setEnabled(false);
		GridData gd_textMatchFlowContexts = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textMatchFlowContexts.widthHint = 150;
		statusFlowContext.setLayoutData(gd_textMatchFlowContexts);

		// ======== ROW 5 =======================

		lblUnit = new Label(composite, SWT.NONE);
		lblUnit.setText("5. Flow Unit");
		lblUnit.setToolTipText("The Flow Unit and its corresponding Flow Property must be matched for an LCA Flow to match.");

		btnMatchFlowProperties = new Button(composite, SWT.NONE);
		GridData gd_btnMatchFlowProperties = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		btnMatchFlowProperties.addSelectionListener(matchFlowPropertiesListener);

		gd_btnMatchFlowProperties.widthHint = 80;
		btnMatchFlowProperties.setLayoutData(gd_btnMatchFlowProperties);
		btnMatchFlowProperties.setText("Show Unique");
		btnMatchFlowProperties.setEnabled(false);

		statusFlowUnit = new StyledText(composite, SWT.BORDER | SWT.READ_ONLY);
		statusFlowUnit.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		statusFlowUnit.setEnabled(false);
		GridData gd_textMatchFlowProperties = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textMatchFlowProperties.widthHint = 150;
		statusFlowUnit.setLayoutData(gd_textMatchFlowProperties);

		// ======== ROW 6 =======================

		lblFlowable = new Label(composite, SWT.NONE);
		lblFlowable.setText("6. Flowable");
		lblFlowable
				.setToolTipText("In the LCA-Harmonization Tool, Flowables are matched indendently of Flows so that like Flowables can be scrutinized as a group.");

		btnMatchFlowables = new Button(composite, SWT.NONE);
		GridData gd_btnMatchFlowables = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnMatchFlowables.widthHint = 80;
		btnMatchFlowables.setLayoutData(gd_btnMatchFlowables);
		btnMatchFlowables.setText("Show Unique");

		btnMatchFlowables.setEnabled(false);
		btnMatchFlowables.addSelectionListener(matchFlowablesListener);

		statusFlowable = new StyledText(composite, SWT.BORDER | SWT.READ_ONLY);
		statusFlowable.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		statusFlowable.setEnabled(false);
		GridData gd_textMatchFlowables = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textMatchFlowables.widthHint = 120;
		statusFlowable.setLayoutData(gd_textMatchFlowables);
		// ======== ROW 7 =======================

		lblFinalize = new Label(composite, SWT.NONE);
		lblFinalize.setText("7. Finish");
		lblFinalize
				.setToolTipText("In this step, users may export harmonized data or cancel if prior to completing step 3.");

		btnConcludeFile = new Button(composite, SWT.NONE);
		GridData gd_btnConcludeFile = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnConcludeFile.widthHint = 80;
		btnConcludeFile.setLayoutData(gd_btnConcludeFile);
		btnConcludeFile.setEnabled(false);
		statusConclude = new StyledText(composite, SWT.BORDER | SWT.READ_ONLY);
		statusConclude.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		statusConclude.setEnabled(false);
		statusConclude.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnConcludeFile.addSelectionListener(concludeFileListener);
		gd_textMatchFlowables.widthHint = 120;

		// this needs to be done here to be available for reloaded data sets that have never been auto matched
		CSVTableView.setUniqueFlowableRowNumbers(uniqueFlowableRowNumbers);
		CSVTableView.setUniqueFlowContextRowNumbers(uniqueFlowContextRowNumbers);
		CSVTableView.setUniqueFlowPropertyRowNumbers(uniqueFlowPropertyRowNumbers);
		CSVTableView.setUniqueFlowRowNumbers(uniqueFlowRowNumbers);

		CSVTableView.setMatchedFlowableRowNumbers(matchedFlowableRowNumbers);
		CSVTableView.setMatchedFlowContextRowNumbers(matchedFlowContextRowNumbers);
		CSVTableView.setMatchedFlowPropertyRowNumbers(matchedFlowPropertyRowNumbers);
		CSVTableView.setMatchedFlowRowNumbers(matchedFlowRowNumbers);
		switchToWorkflowState(ST_BEFORE_LOAD);
	}

	// Access to 1. User Data
	public static String getStatusUserData() {
		return statusLoadUserData.getText();
	}

	public static void setStatusUserData(String newStatus) {
		statusLoadUserData.setText(newStatus);
	}

	public static String getTooltipStatusUserData() {
		return statusLoadUserData.getToolTipText();
	}

	public static void setTooltipStatusUserData(String tooltip) {
		statusLoadUserData.setToolTipText(tooltip);
	}

	// Access to 2. Format (Check Data)
	public static String setStatusCheckData() {
		return statusCheckData.getText();
	}

	public static void getStatusCheckData(String newStatus) {
		statusCheckData.setText(newStatus);
	}

	public static String getTooltipStatusCheckData() {
		return statusCheckData.getToolTipText();
	}

	public static void setTooltipStatusCheckData(String tooltip) {
		statusCheckData.setToolTipText(tooltip);
	}

	// Access to 3. Save + Match (aka Commit or AutoMatch)
	public static String getStatusSaveMatch() {
		return statusSaveMatch.getText();
	}

	public static void setStatusSaveMatch(String newStatus) {
		statusSaveMatch.setText(newStatus);
	}

	public static String getTooltipStatusSaveMatch() {
		return statusSaveMatch.getToolTipText();
	}

	public static void setTooltipStatusSaveMatch(String tooltip) {
		statusSaveMatch.setToolTipText(tooltip);
	}

	// Access to 4. Flow Context
	public static String getStatusFlowContext() {
		return statusFlowContext.getText();
	}

	public static void setStatusFlowContext(String newStatus) {
		statusFlowContext.setText(newStatus);
	}

	public static String getTooltipStatusFlowContext() {
		return statusFlowContext.getToolTipText();
	}

	public static void setTooltipStatusFlowContext(String tooltip) {
		statusFlowContext.setToolTipText(tooltip);
	}

	// Access to 5. Flow Unit
	public static String getStatusFlowUnit() {
		return statusFlowUnit.getText();
	}

	public static void setStatusFlowUnit(String newStatus) {
		statusFlowUnit.setText(newStatus);
	}

	public static String getTooltipFlowUnit() {
		return statusFlowUnit.getToolTipText();
	}

	public static void setTooltipStatusFlowUnit(String tooltip) {
		statusFlowUnit.setToolTipText(tooltip);
	}

	// Access to 6. Flowable
	public static String getStatusFlowable() {
		return statusFlowable.getText();
	}

	public static void setStatusFlowable(String newStatus) {
		statusFlowable.setText(newStatus);
	}

	public static String getTooltipStatusFlowable() {
		return statusFlowable.getToolTipText();
	}

	public static void setTooltipStatusFlowable(String tooltip) {
		statusFlowable.setToolTipText(tooltip);
	}

	// Access to 7. Final (aka Conclude = Cancel / Export)
	public static String getStatusConclude() {
		return statusConclude.getText();
	}

	public static void setStatusConclude(String newStatus) {
		statusConclude.setText(newStatus);
	}

	public static String getTooltipStatusConclude() {
		return statusConclude.getToolTipText();
	}

	public static void setTooltipStatusConclude(String tooltip) {
		statusConclude.setToolTipText(tooltip);
	}

	private static void initializeOtherViews() {
		Util.findView(LoggerViewer.ID);
		LoggerViewer.clear(); // INITIALIZES SO THAT LOGGER RECEIVES INPUT
	}

	public static void enableCommitButton(boolean enabled) {
		btnCommit.setEnabled(enabled);
	}

	public static void clearStatusText() {
		statusLoadUserData.setText("");
		statusCheckData.setText("");
		statusSaveMatch.setText("");
		statusFlowable.setText("");
		statusFlowContext.setText("");
		statusFlowUnit.setText("");
	}

	public static void buttonModePostLoad() {
//		FlowsWorkflow.restoreAllButtons();
		switchToWorkflowState(FlowsWorkflow.ST_BEFORE_CHECK);
		CSVTableView.preCommit = true;
	}

	// TODO - ensure that all these buttons should be set while auto match is running. If not, set them individually.
	public static void buttonModeWhileCommit() {
		buttonModePostCommit();
		/*
		 * Leaving this enabled for now - it gives the user the ability to "cancel" a job as it's reunning and work on
		 * something else. This may be worth revisiting later - the "canceled" job still runs in the background, and
		 * will re-enable buttons when it's finished, regardless of what's currently happening
		 */
		/*
		 * btnLoadUserData.setEnabled(false); btnLoadUserData.setGrayed(true);
		 */
		switchToWorkflowState(FlowsWorkflow.ST_DURING_COMMIT);
		btnConcludeFile.setEnabled(false);
		btnConcludeFile.setGrayed(true);
	}

	public static void buttonModePostCommit() {
		switchToWorkflowState(ST_BEFORE_EXPORT);
//		FlowsWorkflow.restoreAllButtons();
//		btnCommit.setEnabled(false);
//		btnCheckData.setEnabled(false);
//		btnMatchFlowables.setEnabled(true);
//		btnMatchFlowables.setGrayed(false);
//		btnMatchFlowContexts.setEnabled(true);
//		btnMatchFlowContexts.setGrayed(false);
//		btnMatchFlowProperties.setEnabled(true);
//		btnMatchFlowProperties.setGrayed(false);
		/*
		 * btnLoadUserData.setEnabled(true); btnLoadUserData.setGrayed(false);
		 */
//		btnConcludeFile.setEnabled(true);
//		btnConcludeFile.setGrayed(false);
//		btnConcludeFile.setText("Export");
		CSVTableView.setPostCommit();
	}

	// ------------------- LOAD LISTENER -------------------
	SelectionListener loadUserDataListener = new SelectionListener() {

		private void doit(SelectionEvent e) {
			switchToWorkflowState(FlowsWorkflow.ST_DURING_LOAD);

			IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
			try {
				handlerService.executeCommand("gov.epa.nrmrl.std.lca.ht.handler.ImportUserData", null);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			// btnConcludeFile.setText("Cancel");
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			doit(e);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			doit(e);
		}
	};

	// ------------------- CHECK LISTENER -------------------
	// TODO - move whatever we need into ui threads
	// TODO - disable buttons sensibly
	SelectionListener checkDataListener = new SelectionListener() {
		private void doit(final SelectionEvent e) {
			switchToWorkflowState(ST_DURING_CHECK);

			CSVTableView.clearIssues();
			final LCADataPropertyProvider[] lcaDataPropreties = TableKeeper.getTableProvider(
					CSVTableView.getTableProviderKey()).getLcaDataProperties();
			final List<LCADataPropertyProvider> requiredLCADataPropertyProvider = new ArrayList<LCADataPropertyProvider>();
			// This has to be done in a UI thread, otherwise we get NPEs when we
			// try to access the active window
			final Display display = Display.getDefault();

			new Thread() {
				public void run() {

					boolean checkForRequiredFlowableFields = false;
					boolean checkForRequiredFlowContextFields = false;
					boolean checkForRequiredFlowPropertyFields = false;

					int countOfAssignedFields = 0;
					for (LCADataPropertyProvider lcaDataPropertyProvider : lcaDataPropreties) {
						if (lcaDataPropertyProvider == null) {
							continue;
						}
						countOfAssignedFields++;
						if (lcaDataPropertyProvider.getPropertyClass().equals(Flowable.label)) {
							checkForRequiredFlowableFields = true;
						} else if (lcaDataPropertyProvider.getPropertyClass().equals(FlowContext.label)) {
							checkForRequiredFlowContextFields = true;
						} else if (lcaDataPropertyProvider.getPropertyClass().equals(FlowProperty.label)) {
							checkForRequiredFlowPropertyFields = true;
						}
						if (lcaDataPropertyProvider.isRequired()) {
							requiredLCADataPropertyProvider.add(lcaDataPropertyProvider);
						}
					}

					if (checkForRequiredFlowableFields) {
						for (LCADataPropertyProvider requiredLCADataProperty : Flowable.getDataPropertyMap().values()) {
							if (requiredLCADataProperty.isRequired()) {
								boolean found = false;
								for (LCADataPropertyProvider gotIt : requiredLCADataPropertyProvider) {
									if (gotIt.sameAs(requiredLCADataProperty)) {
										found = true;
										break;
									}
								}
								if (found == false) {
									final String missingProp = requiredLCADataProperty.getPropertyName();
									Display.getDefault().syncExec(new Runnable() {
										public void run() {
											new GenericMessageBox(e.display.getActiveShell(), "Missing Assignment",
													"For each flowable, the " + missingProp + " is required");
										}
									});
								}
							}
						}
					}
					if (checkForRequiredFlowContextFields) {
						for (LCADataPropertyProvider requiredLCADataProperty : FlowContext.getDataPropertyMap()
								.values()) {
							if (requiredLCADataProperty.isRequired()) {
								boolean found = false;
								for (LCADataPropertyProvider gotIt : requiredLCADataPropertyProvider) {
									if (gotIt.sameAs(requiredLCADataProperty)) {
										found = true;
										break;
									}
								}
								if (found == false) {
									final String missingProp = requiredLCADataProperty.getPropertyName();
									Display.getDefault().syncExec(new Runnable() {
										public void run() {
											new GenericMessageBox(e.display.getActiveShell(), "Missing Assignment",
													"For each flow context, the " + missingProp + " is required");
										}
									});
								}
							}
						}
					}
					if (checkForRequiredFlowPropertyFields) {
						for (LCADataPropertyProvider requiredLCADataProperty : FlowProperty.getDataPropertyMap()
								.values()) {
							if (requiredLCADataProperty.isRequired()) {
								boolean found = false;
								for (LCADataPropertyProvider gotIt : requiredLCADataPropertyProvider) {
									if (gotIt.sameAs(requiredLCADataProperty)) {
										found = true;
										break;
									}
								}
								if (found == false) {
									final String missingProp = requiredLCADataProperty.getPropertyName();
									Display.getDefault().syncExec(new Runnable() {
										public void run() {
											new GenericMessageBox(e.display.getActiveShell(), "Missing Assignment",
													"For each flow property, the " + missingProp + " is required");
										}
									});
								}
							}
						}
					}

					final int fieldCount = countOfAssignedFields;
					/* ui start */
					new Thread() {
						public void run() {
							display.syncExec(new Runnable() {
								public void run() {
									if (fieldCount == 0) {
										statusCheckData.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
										statusCheckData.setText("Assign at least one column first)");
										btnMatchFlowables.setEnabled(false);
										btnCommit.setEnabled(false);
									} else {
										int issueCount = CSVTableView.checkCols();
										/* The above function tells CSVTableView to do all the data checking */
										// int issueCount = 0;
										statusCheckData.setBackground(SWTResourceManager
												.getColor(SWT.COLOR_INFO_BACKGROUND));

										statusCheckData.setText(issueCount + " issues. " + fieldCount
												+ " columns checked");
										if (issueCount == 0) {
											// btnMatchFlowables.setEnabled(true);
											switchToWorkflowState(FlowsWorkflow.ST_BEFORE_COMMIT);

											// btnCommit.setEnabled(true);
											// setTooltipStatusSaveMatch("Click to begin saving to database and matching data.");
										} else {
											switchToWorkflowState(FlowsWorkflow.ST_FIX_ISSUES);

											// btnMatchFlowables.setEnabled(false);
											// btnCommit.setEnabled(false);
											// setTooltipStatusSaveMatch("Issues must be resolved prior to Save+Match");
										}
									}
									// btnCheckData.setEnabled(true);
									// btnLoadUserData.setEnabled(false);
								}
							});
						}
					}.start();
				}
			}.start();
			// switchToWorkflowState(4);
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			doit(e);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			doit(e);
		}
	};

	// ------------------- COMMIT LISTENER -------------------
	SelectionListener commitListener = new SelectionListener() {

		private void doit(SelectionEvent e) {
			switchToWorkflowState(FlowsWorkflow.ST_DURING_COMMIT);
			FlowsWorkflow.disableAllButtons();
			// btnCommit.setEnabled(false);
			Util.findView(CSVTableView.ID);
			int colsChecked = CSVTableView.countAssignedColumns();
			if (colsChecked == 0) {
				statusSaveMatch.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
				statusSaveMatch.setText("Assign and check columns first)");
//				FlowsWorkflow.restoreAllButtons();
				switchToWorkflowState(FlowsWorkflow.ST_BEFORE_CHECK);
				return;
			}
			buttonModeWhileCommit();
			// CSVTableView.preCommit = false;
			CSVTableView.initializeRowMenu();
			String jobKey = "autoMatch_01";
			AutoMatchJob autoMatchJob = new AutoMatchJob("FlowsWorkflow Job");
			autoMatchJob.setPriority(Job.SHORT);
			autoMatchJob.setSystem(false);
			autoMatchJob.addJobChangeListener(new AutoMatchJobChangeListener((FlowsWorkflow) Util
					.findView(FlowsWorkflow.ID), jobKey));
			autoMatchJob.schedule();
			switchToWorkflowState(FlowsWorkflow.ST_BEFORE_EXPORT);
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			doit(e);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			doit(e);
		}
	};

	// ------------- FLOW CONTEXT LISTENER ----------------
	private SelectionListener matchFlowContextsListener = new SelectionListener() {
		private void doit(SelectionEvent e) {
			btnCommit.setEnabled(false);
			btnCheckData.setEnabled(false);
			if (btnMatchFlowContexts.getText().equals(matchContextsString)) {
				switchToWorkflowState(FlowsWorkflow.ST_SHOW_CONTEXT);
				CSVTableView.setFilterRowNumbersWCopy(uniqueFlowContextRowNumbers);
				CSVTableView.colorFlowContextRows();
			} else {
				switchToWorkflowState(FlowsWorkflow.ST_BEFORE_EXPORT);
				CSVTableView.clearFilterRowNumbers();
			}
			Util.setPerspective(LCIWorkflowPerspective.ID);
			try {
				Util.showView(MatchContexts.ID);
			} catch (PartInitException e1) {
				e1.printStackTrace();
			}
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			doit(e);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			doit(e);
		}
	};

	// ------------- FLOW PROPERTY LISTENER ----------------

	private SelectionListener matchFlowPropertiesListener = new SelectionListener() {
		private void doit(SelectionEvent e) {
			btnCommit.setEnabled(false);
			btnCheckData.setEnabled(false);
			if (btnMatchFlowProperties.getText().equals(matchPropertiesString)) {
				switchToWorkflowState(FlowsWorkflow.ST_SHOW_UNIT);
				CSVTableView.setFilterRowNumbersWCopy(uniqueFlowPropertyRowNumbers);
				CSVTableView.colorFlowPropertyRows();

			} else {
				switchToWorkflowState(FlowsWorkflow.ST_BEFORE_EXPORT);
				CSVTableView.clearFilterRowNumbers();
			}
			Util.setPerspective(LCIWorkflowPerspective.ID);
			try {
				Util.showView(MatchProperties.ID);
			} catch (PartInitException e1) {
				e1.printStackTrace();
			}
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			doit(e);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			doit(e);
		}
	};

	// ------------- FLOWABLE LISTENER ----------------
	private SelectionListener matchFlowablesListener = new SelectionListener() {
		private void doit(SelectionEvent e) {
			if (btnMatchFlowables.getText().equals(matchFlowablesString)) {
				switchToWorkflowState(FlowsWorkflow.ST_SHOW_FLOWABLE);
				CSVTableView.setFilterRowNumbersWCopy(uniqueFlowableRowNumbers);
				CSVTableView.colorFlowableRows();
			} else {
				switchToWorkflowState(FlowsWorkflow.ST_BEFORE_EXPORT);
				CSVTableView.clearFilterRowNumbers();
			}
//			CSVTableView.selectTableRow(0);
//			CSVTableView.setColNumSelected(1);
//			CSVTableView.selectRowColumn();

			try {
				Util.showView(MatchFlowables.ID);
			} catch (PartInitException e1) {
				e1.printStackTrace();
			}
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			doit(e);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			doit(e);
		}
	};

	// ------------- CONCLUDE FILE LISTENER ----------------
	SelectionListener concludeFileListener = new SelectionListener() {
		private void doit(SelectionEvent e) {
			// CSVTableView.clearFilterRowNumbers();
			// CSVTableView.reset();
			// CSVTableView.initialize();
			// CSVTableView.setSelection(0);

			// btnLoadUserData.setEnabled(true);
			// statusLoadUserData.setText("");

			// btnCheckData.setEnabled(false);
			// statusCheckData.setText("");

			// btnCommit.setEnabled(false);
			// statusSaveMatch.setText("");

			// btnMatchFlowables.setEnabled(false);
			// statusFlowable.setText("");

			// btnMatchFlowContexts.setEnabled(false);
			// statusFlowContext.setText("");

			// btnMatchFlowProperties.setEnabled(false);
			// statusFlowUnit.setText("");

			// btnConcludeFile.setEnabled(false);

			// uniqueFlowContextRowNumbers.clear();
			// uniqueFlowPropertyRowNumbers.clear();
			// uniqueFlowableRowNumbers.clear();
			// uniqueFlowRowNumbers.clear();

			// matchedFlowContextRowNumbers.clear();
			// matchedFlowPropertyRowNumbers.clear();
			// matchedFlowableRowNumbers.clear();
			// matchedFlowRowNumbers.clear();

			if (btnConcludeFile.getText().matches(".*Export.*")) {
				switchToWorkflowState(FlowsWorkflow.ST_DURING_EXPORT);
				IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
				try {
//					handlerService.executeCommand(SaveHarmonizedDataForOLCAJsonld.ID, null);
					handlerService.executeCommand(SaveHarmonizedDataForOLCAJsonldZip.ID, null);

				} catch (Exception ex) {
					ex.printStackTrace();
				}
				// MatchFlowables.initialize();

			} else if (btnConcludeFile.getText().matches(".*Cancel.*")) {
				// TODO - add a dialog to confirm
				CSVTableView.clearFilterRowNumbers();
				CSVTableView.reset();
				CSVTableView.initialize();
				CSVTableView.selectTableRow(0);
				switchToWorkflowState(FlowsWorkflow.ST_BEFORE_LOAD);

				btnLoadUserData.setEnabled(true);
				statusLoadUserData.setText("");

				btnCheckData.setEnabled(false);
				statusCheckData.setText("");

				// TODO - CONFIRM WITH USER
				// TODO - REMOVE THE FileMD
				// TODO - INDICATE THAT FILE CONTENT WAS REMOVED
				MatchFlowables.initialize();
			}
			Util.setPerspective(LCIWorkflowPerspective.ID);

			try {
				Util.showView(LoggerViewer.ID);
			} catch (PartInitException e1) {
				e1.printStackTrace();
			}
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			doit(e);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			doit(e);
		}
	};
	private Label lblStep;

	public void queryCallback(Integer[] results, String key) {

	}

	public static String getMatchContextsString() {
		return matchContextsString;
	}

	public static void setMatchContextsString(String matchContextsString) {
		FlowsWorkflow.matchContextsString = matchContextsString;
	}

	public static String getMatchPropertiesString() {
		return matchPropertiesString;
	}

	public static void setMatchPropertiesString(String matchPropertiesString) {
		FlowsWorkflow.matchPropertiesString = matchPropertiesString;
	}

	public static String getMatchFlowablesString() {
		return matchFlowablesString;
	}

	public static void setMatchFlowablesString(String matchFlowablesString) {
		FlowsWorkflow.matchFlowablesString = matchFlowablesString;
	}

	public static void disableFlowableBtn() {
		btnMatchFlowables.setGrayed(true);
		btnMatchFlowables.setEnabled(false);
	}

	public static void disableContextBtn() {
		btnMatchFlowContexts.setGrayed(true);
		btnMatchFlowContexts.setEnabled(false);
	}

	public static void disablePropertyBtn() {
		btnMatchFlowProperties.setGrayed(true);
		btnMatchFlowProperties.setEnabled(false);
	}

	public static void showFlowContextMatchCount(int matched, int total) {
		statusFlowContext.setText(matched + " matched. " + total + " found.");
	}

	public static void addContextRowNum(int rowNumToSend) {
		uniqueFlowContextRowNumbers.add(rowNumToSend);
		showFlowContextMatchCount(matchedFlowContextRowNumbers.size(), uniqueFlowContextRowNumbers.size());
		CSVTableView.colorOneFlowContextRow(rowNumToSend, matchedFlowContextRowNumbers.contains(rowNumToSend));
	}

	public static void showFlowUnitMatchCount(int matched, int total) {
		statusFlowUnit.setText(matched + " matched. " + total + " found.");
	}

	public static void addPropertyRowNum(int rowNumToSend) {
		uniqueFlowPropertyRowNumbers.add(rowNumToSend);
		showFlowUnitMatchCount(matchedFlowPropertyRowNumbers.size(), uniqueFlowPropertyRowNumbers.size());
		CSVTableView.colorOneFlowPropertyRow(rowNumToSend, matchedFlowPropertyRowNumbers.contains(rowNumToSend));
	}

	public static void showFlowableMatchCount(int matched, int total) {
		statusFlowable.setText(matched + " matched. " + total + " found.");
	}

	public static void addFlowableRowNum(int dataRowNumberToSend) {
		uniqueFlowableRowNumbers.add(dataRowNumberToSend);
		showFlowableMatchCount(matchedFlowableRowNumbers.size(), uniqueFlowableRowNumbers.size());
		CSVTableView.colorOneFlowableRow(dataRowNumberToSend);
	}

	public static void addFlowRowNum(int rowNumToSend) {
		uniqueFlowRowNumbers.add(rowNumToSend);
		CSVTableView.colorOneFlowRow(rowNumToSend, matchedFlowRowNumbers.contains(rowNumToSend), true);
	}

	public static void addFlowRowNum(int rowNumToSend, boolean colorNow) {
		uniqueFlowRowNumbers.add(rowNumToSend);
		if (colorNow) {
			CSVTableView.colorOneFlowRow(rowNumToSend, matchedFlowRowNumbers.contains(rowNumToSend), true);
		}
	}

	public static void addMatchContextRowNum(int rowNumToSend) {
		matchedFlowContextRowNumbers.add(rowNumToSend);
		statusFlowContext.setText(matchedFlowContextRowNumbers.size() + " matched. "
				+ uniqueFlowContextRowNumbers.size() + " found.");
		CSVTableView.colorFlowContextRows();
	}

	public static void addMatchPropertyRowNum(int rowNumToSend) {
		matchedFlowPropertyRowNumbers.add(rowNumToSend);
		statusFlowUnit.setText(matchedFlowPropertyRowNumbers.size() + " matched. "
				+ uniqueFlowPropertyRowNumbers.size() + " found.");
		CSVTableView.colorFlowPropertyRows();

	}

	public static void addMatchFlowRowNum(int rowNumToSend) {
		matchedFlowRowNumbers.add(rowNumToSend);
		// statusFlowUnit.setText(matchedFlowPropertyRowNumbers.size()
		// + " matched. "
		// + uniqueFlowPropertyRowNumbers.size() + " found.");

		// statusSaveMatch.setText(matchedFlowRowNumbers.size() + " of " + uniqueFlowRowNumbers.size() +
		// " flows matched");
		CSVTableView.colorFlowRows();
	}

	public static void addMatchFlowableRowNum(int rowNumToSend) {
		uniqueFlowableRowNumbers.add(rowNumToSend);
		matchedFlowableRowNumbers.add(rowNumToSend);
		statusFlowable.setText(matchedFlowableRowNumbers.size() + " matched. " + uniqueFlowableRowNumbers.size()
				+ " found.");
		CSVTableView.colorOneFlowableRow(rowNumToSend);
	}

	public static void removeMatchContextRowNum(int rowNumber) {
		// if (!uniqueFlowableRowNumbers.contains(rowNumber)){
		// Flowable flowable =
		// TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getData().get(rowNumber).getFlowable();
		// int firstRowWithSameFlowable = rowNumber;
		// for (int row = rowNumber-1;row>=0;row--){
		// Flowable otherFlowable =
		// TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getData().get(row).getFlowable();
		// if (otherFlowable.equals(flowable)){
		// firstRowWithSameFlowable = row;
		// }
		// }
		// if (!uniqueFlowableRowNumbers.contains(firstRowWithSameFlowable)){
		// return;
		// }
		// rowNumber = firstRowWithSameFlowable;
		// }
		matchedFlowContextRowNumbers.remove(rowNumber);
		statusFlowContext.setText(matchedFlowContextRowNumbers.size() + " matched. "
				+ uniqueFlowContextRowNumbers.size() + " found.");
		// CSVTableView.colorFlowContextRows();
		CSVTableView.colorOneFlowableRow(rowNumber);
	}

	public static void removeMatchPropertyRowNum(int rowNumber) {
		matchedFlowPropertyRowNumbers.remove(rowNumber);
		statusFlowUnit.setText(matchedFlowPropertyRowNumbers.size() + " matched. "
				+ uniqueFlowPropertyRowNumbers.size() + " found.");
		CSVTableView.colorFlowPropertyRows();
	}

	public static void removeMatchFlowableRowNum(int rowNumber) {
		matchedFlowableRowNumbers.remove(rowNumber);
		statusFlowable.setText(matchedFlowableRowNumbers.size() + " matched. " + uniqueFlowableRowNumbers.size()
				+ " found.");
		// CSVTableView.colorFlowableRows();
		CSVTableView.colorOneFlowableRow(rowNumber);
	}

	public static void removeMatchFlowRowNum(int rowNumber) {
		matchedFlowRowNumbers.remove(rowNumber);
		// statusSaveMatch.setText(matchedFlowRowNumbers.size()+" of "+uniqueFlowRowNumbers.size()+" flows matched");
		CSVTableView.colorFlowRows();
	}

	public static void updateFlowableCount() {
		statusFlowable.setText(matchedFlowableRowNumbers.size() + " matched. " + uniqueFlowableRowNumbers.size()
				+ " found.");
	}
	
	public static final int ST_BEFORE_LOAD = 1;
	public static final int ST_DURING_LOAD = 2;
	public static final int ST_BEFORE_CHECK = 3;
	public static final int ST_DURING_CHECK = 4;
	public static final int ST_FIX_ISSUES = 5;
	public static final int ST_BEFORE_COMMIT = 6;
	public static final int ST_DURING_COMMIT = 7;
	public static final int ST_BEFORE_EXPORT = 8;
	public static final int ST_SHOW_CONTEXT = 9;
	public static final int ST_SHOW_UNIT = 10;
	public static final int ST_SHOW_FLOWABLE = 11;
	public static final int ST_DURING_EXPORT = 12;

	public static void switchToWorkflowState(int stateNumber) {
		// Opening
		if (stateNumber == ST_BEFORE_LOAD) {
			btnLoadUserData.setText("Load...");
			btnLoadUserData
					.setToolTipText("Click to load user data.  To load reference (master) data, see Advanced -> Load Reference Data List...");
			setButtonState(btnLoadUserData, true);
			setTooltipStatusUserData("");
			btnCheckData.setText("Check Data");
			btnCheckData.setToolTipText("");
			setButtonState(btnCheckData, false);
			setTooltipStatusCheckData("");
			btnCommit.setText("Begin");
			btnCommit.setToolTipText("");
			setButtonState(btnCommit, false);
			setTooltipStatusSaveMatch("");
			btnMatchFlowables.setText("Show Unique");
			btnMatchFlowables.setToolTipText("");
			setButtonState(btnMatchFlowables, false);
			setTooltipStatusFlowContext("");
			btnMatchFlowContexts.setText("Show Unique");
			btnMatchFlowContexts.setToolTipText("");
			setButtonState(btnMatchFlowContexts, false);
			setTooltipStatusFlowUnit("");
			btnMatchFlowProperties.setText("Show Unique");
			btnMatchFlowProperties.setToolTipText("");
			setButtonState(btnMatchFlowProperties, false);
			setTooltipStatusFlowable("");
			btnConcludeFile.setText("");
			btnConcludeFile.setToolTipText("");
			setButtonState(btnConcludeFile, false);
			setTooltipStatusConclude("");
			CSVTableView.preCommit = true;
		}
		// Running step 1
		else if (stateNumber == ST_DURING_LOAD) {
			btnLoadUserData.setText("Load...");
			btnLoadUserData.setToolTipText("Load in progress...");
			setButtonState(btnLoadUserData, false);
			setTooltipStatusUserData("");
			btnCheckData.setText("Check Data");
			btnCheckData.setToolTipText("");
			setButtonState(btnCheckData, false);
			setTooltipStatusCheckData("");
			btnCommit.setText("Begin");
			btnCommit.setToolTipText("");
			setButtonState(btnCommit, false);
			setTooltipStatusSaveMatch("");
			btnMatchFlowables.setText("Show Unique");
			btnMatchFlowables.setToolTipText("");
			setButtonState(btnMatchFlowables, false);
			setTooltipStatusFlowContext("");
			btnMatchFlowContexts.setText("Show Unique");
			btnMatchFlowContexts.setToolTipText("");
			setButtonState(btnMatchFlowContexts, false);
			setTooltipStatusFlowUnit("");
			btnMatchFlowProperties.setText("Show Unique");
			btnMatchFlowProperties.setToolTipText("");
			setButtonState(btnMatchFlowProperties, false);
			setTooltipStatusFlowable("");
			btnConcludeFile.setText("");
			btnConcludeFile.setToolTipText("");
			setButtonState(btnConcludeFile, false);
			setTooltipStatusConclude("");
			CSVTableView.preCommit = true;
		}
		// End of step 1
		else if (stateNumber == ST_BEFORE_CHECK) {
			btnLoadUserData.setText("Load...");
			btnLoadUserData.setToolTipText("");
			setButtonState(btnLoadUserData, false);
//			setTooltipStatusUserData("[File path]");
			btnCheckData.setText("Check Data");
			btnCheckData.setToolTipText("Click to run format checks.");
			setButtonState(btnCheckData, true);
			setTooltipStatusCheckData("");
			btnCommit.setText("Begin");
			btnCommit.setToolTipText("");
			setButtonState(btnCommit, false);
			setTooltipStatusSaveMatch("");
			btnMatchFlowables.setText("Show Unique");
			btnMatchFlowables.setToolTipText("");
			setButtonState(btnMatchFlowables, false);
			setTooltipStatusFlowContext("");
			btnMatchFlowContexts.setText("Show Unique");
			btnMatchFlowContexts.setToolTipText("");
			setButtonState(btnMatchFlowContexts, false);
			setTooltipStatusFlowUnit("");
			btnMatchFlowProperties.setText("Show Unique");
			btnMatchFlowProperties.setToolTipText("");
			setButtonState(btnMatchFlowProperties, false);
			setTooltipStatusFlowable("");
			btnConcludeFile.setText("Cancel...");
			btnConcludeFile
					.setToolTipText("Click to cancel working with this dataset.  Nothing will be saved to the database.");
			setButtonState(btnConcludeFile, true);
			setTooltipStatusConclude("");
			CSVTableView.preCommit = true;
		}
		// During step 2
		else if (stateNumber == ST_DURING_CHECK) {
			btnLoadUserData.setText("Load...");
			btnLoadUserData.setToolTipText("");
			setButtonState(btnLoadUserData, false);
//			setTooltipStatusUserData("[File path]");
			btnCheckData.setText("Check Data");
			btnCheckData.setToolTipText("Checking...");
			setButtonState(btnCheckData, false);
			setTooltipStatusCheckData("");
			btnCommit.setText("Begin");
			btnCommit.setToolTipText("");
			setButtonState(btnCommit, false);
			setTooltipStatusSaveMatch("");
			btnMatchFlowables.setText("Show Unique");
			btnMatchFlowables.setToolTipText("");
			setButtonState(btnMatchFlowables, false);
			setTooltipStatusFlowContext("");
			btnMatchFlowContexts.setText("Show Unique");
			btnMatchFlowContexts.setToolTipText("");
			setButtonState(btnMatchFlowContexts, false);
			setTooltipStatusFlowUnit("");
			btnMatchFlowProperties.setText("Show Unique");
			btnMatchFlowProperties.setToolTipText("");
			setButtonState(btnMatchFlowProperties, false);
			setTooltipStatusFlowable("");
			btnConcludeFile.setText("Cancel...");
			btnConcludeFile.setToolTipText("");
			setButtonState(btnConcludeFile, false);
			setTooltipStatusConclude("");
			CSVTableView.preCommit = true;
		}
		// Following step 2 (if issues)
		else if (stateNumber == ST_FIX_ISSUES) {
			btnLoadUserData.setText("Load...");
			btnLoadUserData.setToolTipText("");
			setButtonState(btnLoadUserData, false);
//			setTooltipStatusUserData("[File path]");
			btnCheckData.setText("Check Data");
			btnCheckData.setToolTipText("Complete");
			setButtonState(btnCheckData, true);
			setTooltipStatusCheckData("To see info about issues [more..]");
			btnCommit.setText("Begin");
			btnCommit.setToolTipText("Issues must be resolved prior to Save+Match");
			setButtonState(btnCommit, false);
			setTooltipStatusSaveMatch("To see info about issues [more..]");
			btnMatchFlowables.setText("Show Unique");
			btnMatchFlowables.setToolTipText("");
			setButtonState(btnMatchFlowables, false);
			setTooltipStatusFlowContext("");
			btnMatchFlowContexts.setText("Show Unique");
			btnMatchFlowContexts.setToolTipText("");
			setButtonState(btnMatchFlowContexts, false);
			setTooltipStatusFlowUnit("");
			btnMatchFlowProperties.setText("Show Unique");
			btnMatchFlowProperties.setToolTipText("");
			setButtonState(btnMatchFlowProperties, false);
			setTooltipStatusFlowable("");
			btnConcludeFile.setText("Cancel...");
			btnConcludeFile
					.setToolTipText("Click to cancel working with this dataset.  Nothing will be saved to the database.");
			setButtonState(btnConcludeFile, true);
			setTooltipStatusConclude("");
			CSVTableView.preCommit = true;
		}
		// Following step 2 (if no issues)
		else if (stateNumber == ST_BEFORE_COMMIT) {
			btnLoadUserData.setText("Load...");
			btnLoadUserData.setToolTipText("");
			setButtonState(btnLoadUserData, false);
//			setTooltipStatusUserData("[File path]");
			btnCheckData.setText("Check Data");
			btnCheckData.setToolTipText("Complete");
			setButtonState(btnCheckData, true);
			setTooltipStatusCheckData("");
			btnCommit.setText("Begin");
			btnCommit.setToolTipText("Click to begin saving to database and matching data");
			setButtonState(btnCommit, true);
			setTooltipStatusSaveMatch("");
			btnMatchFlowables.setText("Show Unique");
			btnMatchFlowables.setToolTipText("");
			setButtonState(btnMatchFlowables, false);
			setTooltipStatusFlowContext("");
			btnMatchFlowContexts.setText("Show Unique");
			btnMatchFlowContexts.setToolTipText("");
			setButtonState(btnMatchFlowContexts, false);
			setTooltipStatusFlowUnit("");
			btnMatchFlowProperties.setText("Show Unique");
			btnMatchFlowProperties.setToolTipText("");
			setButtonState(btnMatchFlowProperties, false);
			setTooltipStatusFlowable("");
			btnConcludeFile.setText("Cancel...");
			btnConcludeFile
					.setToolTipText("Click to cancel working with this dataset.  Nothing will be saved to the database.");
			setButtonState(btnConcludeFile, true);
			setTooltipStatusConclude("");
			CSVTableView.preCommit = true;
		}
		// During step 3
		else if (stateNumber == ST_DURING_COMMIT) {
			btnLoadUserData.setText("Load...");
			btnLoadUserData.setToolTipText("");
			setButtonState(btnLoadUserData, false);
//			setTooltipStatusUserData("[File path]");
			btnCheckData.setText("Check Data");
			btnCheckData.setToolTipText("Complete");
			setButtonState(btnCheckData, false);
			setTooltipStatusCheckData("");
			btnCommit.setText("Cancel...");
			btnCommit.setToolTipText("Click to cancel the Save & Match process.");
			setButtonState(btnCommit, true);
			setTooltipStatusSaveMatch("");
			btnMatchFlowables.setText("Show Unique");
			btnMatchFlowables.setToolTipText("");
			setButtonState(btnMatchFlowables, false);
			setTooltipStatusFlowContext("");
			btnMatchFlowContexts.setText("Show Unique");
			btnMatchFlowContexts.setToolTipText("");
			setButtonState(btnMatchFlowContexts, false);
			setTooltipStatusFlowUnit("");
			btnMatchFlowProperties.setText("Show Unique");
			btnMatchFlowProperties.setToolTipText("");
			setButtonState(btnMatchFlowProperties, false);
			setTooltipStatusFlowable("");
			btnConcludeFile.setText("Cancel...");
			btnConcludeFile.setToolTipText("");
			setButtonState(btnConcludeFile, false);
			setTooltipStatusConclude("");
//			CSVTableView.preCommit = false;
		}
		// Following step 3
		else if (stateNumber == ST_BEFORE_EXPORT) {
			btnLoadUserData.setText("Load new...");
			btnLoadUserData
					.setToolTipText("Click to load a new user dataset.  Current data may be reloaded in the future to continue harmonization.  To re-load a previously laoded dataset, use the menu item \"DataSet -> Reload dataset\". To load reference (master) data, see Advanced -> Load Reference Data List...");
			setButtonState(btnLoadUserData, true);
//			setTooltipStatusUserData("[File path]");
			btnCheckData.setText("");
			btnCheckData.setToolTipText("Complete");
			setButtonState(btnCheckData, false);
			setTooltipStatusCheckData("");
			btnCommit.setText("");
			btnCommit.setToolTipText("");
			setButtonState(btnCommit, false);
			setTooltipStatusSaveMatch("User data has been saved to the database and matched against master data.  Results are shown below.");
			btnMatchFlowables.setText("Show Unique");
			btnMatchFlowables
					.setToolTipText("Click to show only the first row with each of the 8 distinct Flow Contexts in the User Data table.");
			setButtonState(btnMatchFlowables, true);
			setTooltipStatusFlowContext("A total of 8 distinct Flow Contexts were found, and 7 have been matched to master Flow Contexts");
			btnMatchFlowContexts.setText("Show Unique");
			btnMatchFlowContexts
					.setToolTipText("Click to show only the first row with each of the 6 distinct Flow Units in the User Data table.");
			setButtonState(btnMatchFlowContexts, true);
			setTooltipStatusFlowUnit("A total of 6 distinct Flow Units were found, and 6 have been matched to master Flow Units");
			btnMatchFlowProperties.setText("Show Unique");
			btnMatchFlowProperties
					.setToolTipText("Click to show only the first row with each of the 25 distinct Flowables in the User Data table.");
			setButtonState(btnMatchFlowProperties, true);
			setTooltipStatusFlowable("A total of 25 distinct Flowables were found, and 23 have been matched to master Flowables");
			btnConcludeFile.setText("Export...");
			btnConcludeFile.setToolTipText("Click to export this dataset to any of several formats.");
			setButtonState(btnConcludeFile, true);
			setTooltipStatusConclude("");
			CSVTableView.preCommit = false;
		}
		// Following step 3, if 4 is toggled
		else if (stateNumber == ST_SHOW_CONTEXT) {
			btnLoadUserData.setText("Load new...");
			btnLoadUserData
					.setToolTipText("Click to load a new user dataset.  Current data may be reloaded in the future to continue harmonization.  To re-load a previously laoded dataset, use the menu item \"DataSet -> Reload dataset\". To load reference (master) data, see Advanced -> Load Reference Data List...");
			setButtonState(btnLoadUserData, true);
//			setTooltipStatusUserData("[File path]");
			btnCheckData.setText("");
			btnCheckData.setToolTipText("");
			setButtonState(btnCheckData, false);
			setTooltipStatusCheckData("");
			btnCommit.setText("");
			btnCommit.setToolTipText("");
			setButtonState(btnCommit, false);
			setTooltipStatusSaveMatch("User data has been saved to the database and matched against master data.  Results are shown below.");
			btnMatchFlowables.setText("Show All");
			btnMatchFlowables.setToolTipText("Click to show all rows in the User Data table.");
			setButtonState(btnMatchFlowables, true);
			setTooltipStatusFlowContext("A total of 8 distinct Flow Contexts were found, and 7 have been matched to master Flow Contexts");
			btnMatchFlowContexts.setText("Show Unique");
			btnMatchFlowContexts
					.setToolTipText("You must click \"Show All\" next to \"4. Flow Context\" to use this function.");
			setButtonState(btnMatchFlowContexts, false);
			setTooltipStatusFlowUnit("A total of 6 distinct Flow Units were found, and 6 have been matched to master Flow Units");
			btnMatchFlowProperties.setText("Show Unique");
			btnMatchFlowProperties
					.setToolTipText("You must click \"Show All\" next to \"4. Flow Context\" to use this function.");
			setButtonState(btnMatchFlowProperties, false);
			setTooltipStatusFlowable("A total of 25 distinct Flowables were found, and 23 have been matched to master Flowables");
			btnConcludeFile.setText("Export...");
			btnConcludeFile.setToolTipText("Click to export this dataset to any of several formats.");
			setButtonState(btnConcludeFile, true);
			setTooltipStatusConclude("");
			CSVTableView.preCommit = false;
		}
		// Following step 3, if 5 is toggled
		else if (stateNumber == ST_SHOW_UNIT) {
			btnLoadUserData.setText("Load new...");
			btnLoadUserData
					.setToolTipText("Click to load a new user dataset.  Current data may be reloaded in the future to continue harmonization.  To re-load a previously laoded dataset, use the menu item \"DataSet -> Reload dataset\". To load reference (master) data, see Advanced -> Load Reference Data List...");
			setButtonState(btnLoadUserData, true);
//			setTooltipStatusUserData("[File path]");
			btnCheckData.setText("");
			btnCheckData.setToolTipText("");
			setButtonState(btnCheckData, false);
			setTooltipStatusCheckData("");
			btnCommit.setText("");
			btnCommit.setToolTipText("");
			setButtonState(btnCommit, false);
			setTooltipStatusSaveMatch("User data has been saved to the database and matched against master data.  Results are shown below.");
			btnMatchFlowables.setText("Show Unique");
			btnMatchFlowables
					.setToolTipText("You must click \"Show All\" next to \"5. Flow Unit\" to use this function.");
			setButtonState(btnMatchFlowables, false);
			setTooltipStatusFlowContext("A total of 8 distinct Flow Contexts were found, and 7 have been matched to master Flow Contexts");
			btnMatchFlowContexts.setText("Show All");
			btnMatchFlowContexts.setToolTipText("Click to show all rows in the User Data table.");
			setButtonState(btnMatchFlowContexts, true);
			setTooltipStatusFlowUnit("A total of 6 distinct Flow Units were found, and 6 have been matched to master Flow Units");
			btnMatchFlowProperties.setText("Show Unique");
			btnMatchFlowProperties
					.setToolTipText("You must click \"Show All\" next to \"5. Flow Unit\" to use this function.");
			setButtonState(btnMatchFlowProperties, false);
			setTooltipStatusFlowable("A total of 25 distinct Flowables were found, and 23 have been matched to master Flowables");
			btnConcludeFile.setText("Export...");
			btnConcludeFile.setToolTipText("Click to export this dataset to any of several formats.");
			setButtonState(btnConcludeFile, true);
			setTooltipStatusConclude("");
			CSVTableView.preCommit = false;
		}
		// Following step 3, if 6 is toggled
		else if (stateNumber == ST_SHOW_FLOWABLE) {
			btnLoadUserData.setText("Load new...");
			btnLoadUserData
					.setToolTipText("Click to load a new user dataset.  Current data may be reloaded in the future to continue harmonization.  To re-load a previously laoded dataset, use the menu item \"DataSet -> Reload dataset\". To load reference (master) data, see Advanced -> Load Reference Data List...");
			setButtonState(btnLoadUserData, true);
//			setTooltipStatusUserData("[File path]");
			btnCheckData.setText("");
			btnCheckData.setToolTipText("");
			setButtonState(btnCheckData, false);
			setTooltipStatusCheckData("");
			btnCommit.setText("");
			btnCommit.setToolTipText("");
			setButtonState(btnCommit, false);
			setTooltipStatusSaveMatch("User data has been saved to the database and matched against master data.  Results are shown below.");
			btnMatchFlowables.setText("Show Unique");
			btnMatchFlowables
					.setToolTipText("You must click \"Show All\" next to \"6. Flowable\" to use this function.");
			setButtonState(btnMatchFlowables, false);
			setTooltipStatusFlowContext("A total of 8 distinct Flow Contexts were found, and 7 have been matched to master Flow Contexts");
			btnMatchFlowContexts.setText("Show Unique");
			btnMatchFlowContexts
					.setToolTipText("You must click \"Show All\" next to \"6. Flowable\" to use this function.");
			setButtonState(btnMatchFlowContexts, false);
			setTooltipStatusFlowUnit("A total of 6 distinct Flow Units were found, and 6 have been matched to master Flow Units");
			btnMatchFlowProperties.setText("Show All");
			btnMatchFlowProperties.setToolTipText("Click to show all rows in the User Data table.");
			setButtonState(btnMatchFlowProperties, true);
			setTooltipStatusFlowable("A total of 25 distinct Flowables were found, and 23 have been matched to master Flowables");
			btnConcludeFile.setText("Export...");
			btnConcludeFile.setToolTipText("Click to export this dataset to any of several formats.");
			setButtonState(btnConcludeFile, true);
			setTooltipStatusConclude("");
			CSVTableView.preCommit = false;
		}
		// During step 7 (following dialogs)
		else if (stateNumber == ST_DURING_EXPORT) {
			btnLoadUserData.setText("");
			btnLoadUserData.setToolTipText("");
			setButtonState(btnLoadUserData, false);
//			setTooltipStatusUserData("[File path]");
			btnCheckData.setText("");
			btnCheckData.setToolTipText("");
			setButtonState(btnCheckData, false);
			setTooltipStatusCheckData("");
			btnCommit.setText("");
			btnCommit.setToolTipText("");
			setButtonState(btnCommit, false);
			setTooltipStatusSaveMatch("User data has been saved to the database and matched against master data.  Results are shown below.");
			btnMatchFlowables.setText("Show Unique");
			btnMatchFlowables.setToolTipText("");
			setButtonState(btnMatchFlowables, false);
			setTooltipStatusFlowContext("A total of 8 distinct Flow Contexts were found, and 8 have been matched to master Flow Contexts");
			btnMatchFlowContexts.setText("Show Unique");
			btnMatchFlowContexts.setToolTipText("");
			setButtonState(btnMatchFlowContexts, false);
			setTooltipStatusFlowUnit("A total of 6 distinct Flow Units were found, and 6 have been matched to master Flow Units");
			btnMatchFlowProperties.setText("Show Unique");
			btnMatchFlowProperties.setToolTipText("");
			setButtonState(btnMatchFlowProperties, false);
			setTooltipStatusFlowable("A total of 25 distinct Flowables were found, and 25 have been matched to master Flowables");
			btnConcludeFile.setText("Cancel...");
			btnConcludeFile.setToolTipText("Click to cancel the export process.");
			setButtonState(btnConcludeFile, true);
			setTooltipStatusConclude("");
			CSVTableView.preCommit = false;
		}
	}

	private static void setButtonState(Button buttonToChange, boolean turnOn) {
		buttonToChange.setEnabled(turnOn);
		buttonToChange.setGrayed(!turnOn);
	}
}
