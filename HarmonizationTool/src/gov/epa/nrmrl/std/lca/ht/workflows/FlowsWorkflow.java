package gov.epa.nrmrl.std.lca.ht.workflows;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

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
 *
 * @author ttransue
 */
public class FlowsWorkflow extends ViewPart {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow";

	public static StyledText textLoadUserData;
	public static StyledText textCheckData;
	public static StyledText textCommit;
	public static StyledText textMatchFlowables;
	public static StyledText textMatchFlowContexts;
	public static StyledText textMatchFlowProperties;

	private Label label_01;
	private Label label_02;
	private Label label_03;
	private Label label_04;
	private Label label_05;
	private Label label_06;
	private Label label_07;

	private static String matchContextsString = "Match Contexts";
	private static String matchPropertiesString = "Match Properties";
	private static String matchFlowablesString = "Match Flowables";

	public static Button btnLoadUserData;
	public static Button btnCheckData;
	public static Button btnCommit;
	public static Button btnMatchFlowables;
	public static Button btnMatchFlowContexts;
	public static Button btnMatchFlowProperties;
	public static Button btnConcludeFile;

	private static LinkedHashSet<Integer> uniqueFlowableRowNumbers = new LinkedHashSet<Integer>();
	private static LinkedHashSet<Integer> uniqueFlowContextRowNumbers = new LinkedHashSet<Integer>();
	private static LinkedHashSet<Integer> uniqueFlowPropertyRowNumbers = new LinkedHashSet<Integer>();
	private static LinkedHashSet<Integer> uniqueFlowRowNumbers = new LinkedHashSet<Integer>();

	private static LinkedHashSet<Integer> matchedFlowableRowNumbers = new LinkedHashSet<Integer>();
	private static LinkedHashSet<Integer> matchedFlowContextRowNumbers = new LinkedHashSet<Integer>();
	private static LinkedHashSet<Integer> matchedFlowPropertyRowNumbers = new LinkedHashSet<Integer>();
	private static LinkedHashSet<Integer> matchedFlowRowNumbers = new LinkedHashSet<Integer>();

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
		new Label(composite, SWT.NONE);

		Label lblActions = new Label(composite, SWT.NONE);
		lblActions.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblActions.setText("Actions");

		Label lblStatus = new Label(composite, SWT.NONE);
		lblStatus.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblStatus.setText("Status");

		// ======== ROW 1 =======================

		label_01 = new Label(composite, SWT.NONE);
		GridData gd_label_01 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_label_01.widthHint = 20;
		label_01.setLayoutData(gd_label_01);
		label_01.setText("1");
		// lblNewLabel.setSize(100, 30);

		btnLoadUserData = new Button(composite, SWT.NONE);
		GridData gd_btnLoadUD = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnLoadUD.widthHint = 120;
		btnLoadUserData.setLayoutData(gd_btnLoadUD);
		btnLoadUserData.setText("Load User Data");

		btnLoadUserData.addSelectionListener(loadUserDataListener);

		textLoadUserData = new StyledText(composite, SWT.BORDER | SWT.READ_ONLY);
		textLoadUserData.setEnabled(false);
		textLoadUserData.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));

		GridData gd_textFileInfo = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textFileInfo.widthHint = 150;
		textLoadUserData.setLayoutData(gd_textFileInfo);

		// ======== ROW 2 =======================

		label_02 = new Label(composite, SWT.NONE);
		GridData gd_label_02 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_label_02.widthHint = 20;
		label_02.setLayoutData(gd_label_02);
		label_02.setText("2");

		btnCheckData = new Button(composite, SWT.NONE);
		GridData gd_btnCheckData = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnCheckData.widthHint = 120;
		btnCheckData.setLayoutData(gd_btnCheckData);
		btnCheckData.setText("Check Data");
		btnCheckData.setEnabled(false);
		btnCheckData.addSelectionListener(checkDataListener);

		textCheckData = new StyledText(composite, SWT.BORDER | SWT.READ_ONLY);
		textCheckData.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		textCheckData.setEnabled(false);
		// textCheckData.setText("0 issues");
		GridData gd_textIssues = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textIssues.widthHint = 150;
		textCheckData.setLayoutData(gd_textIssues);

		// ======== ROW 3 =======================

		label_03 = new Label(composite, SWT.NONE);
		GridData gd_label_03 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_label_03.widthHint = 20;
		label_03.setLayoutData(gd_label_03);
		label_03.setText("3");

		btnCommit = new Button(composite, SWT.NONE);
		btnCommit.setEnabled(false);
		GridData gd_btnCommit = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnCommit.widthHint = 120;
		btnCommit.setLayoutData(gd_btnCommit);
		btnCommit.setText("Commit");
		btnCommit.addSelectionListener(commitListener);

		textCommit = new StyledText(composite, SWT.BORDER | SWT.READ_ONLY);
		textCommit.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		textCommit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textCommit.setEnabled(false);

		// ======== ROW 4 =======================

		label_04 = new Label(composite, SWT.NONE);
		GridData gd_label_04 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_label_04.widthHint = 20;
		label_04.setLayoutData(gd_label_04);
		label_04.setText("4");

		btnMatchFlowContexts = new Button(composite, SWT.NONE);
		GridData gd_btnMatchFlowContexts = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		btnMatchFlowContexts.addSelectionListener(matchFlowContextsListener);

		gd_btnMatchFlowContexts.widthHint = 120;
		btnMatchFlowContexts.setLayoutData(gd_btnMatchFlowContexts);
		btnMatchFlowContexts.setText(matchContextsString);
		btnMatchFlowContexts.setEnabled(false);

		textMatchFlowContexts = new StyledText(composite, SWT.BORDER | SWT.READ_ONLY);
		textMatchFlowContexts.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		textMatchFlowContexts.setEnabled(false);
		GridData gd_textMatchFlowContexts = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textMatchFlowContexts.widthHint = 150;
		textMatchFlowContexts.setLayoutData(gd_textMatchFlowContexts);

		// ======== ROW 5 =======================

		label_05 = new Label(composite, SWT.NONE);
		GridData gd_label_05 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_label_05.widthHint = 20;
		label_05.setLayoutData(gd_label_05);
		label_05.setText("5");

		btnMatchFlowProperties = new Button(composite, SWT.NONE);
		GridData gd_btnMatchFlowProperties = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		btnMatchFlowProperties.addSelectionListener(matchFlowPropertiesListener);

		gd_btnMatchFlowProperties.widthHint = 120;
		btnMatchFlowProperties.setLayoutData(gd_btnMatchFlowProperties);
		btnMatchFlowProperties.setText(matchPropertiesString);
		btnMatchFlowProperties.setEnabled(false);

		textMatchFlowProperties = new StyledText(composite, SWT.BORDER | SWT.READ_ONLY);
		textMatchFlowProperties.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		textMatchFlowProperties.setEnabled(false);
		GridData gd_textMatchFlowProperties = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textMatchFlowProperties.widthHint = 150;
		textMatchFlowProperties.setLayoutData(gd_textMatchFlowProperties);

		// ======== ROW 6 =======================

		label_06 = new Label(composite, SWT.NONE);
		GridData gd_label_06 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_label_06.widthHint = 20;
		label_06.setLayoutData(gd_label_06);
		label_06.setText("6");

		btnMatchFlowables = new Button(composite, SWT.NONE);
		GridData gd_btnMatchFlowables = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnMatchFlowables.widthHint = 120;
		btnMatchFlowables.setLayoutData(gd_btnMatchFlowables);
		btnMatchFlowables.setText(matchFlowablesString);

		btnMatchFlowables.setEnabled(false);
		btnMatchFlowables.addSelectionListener(matchFlowablesListener);

		textMatchFlowables = new StyledText(composite, SWT.BORDER | SWT.READ_ONLY);
		textMatchFlowables.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		textMatchFlowables.setEnabled(false);
		GridData gd_textMatchFlowables = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textMatchFlowables.widthHint = 120;
		textMatchFlowables.setLayoutData(gd_textMatchFlowables);
		// ======== ROW 7 =======================

		label_07 = new Label(composite, SWT.NONE);
		GridData gd_label_07 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_label_07.widthHint = 20;
		label_07.setLayoutData(gd_label_07);
		label_07.setText("7");

		btnConcludeFile = new Button(composite, SWT.NONE);
		GridData gd_btnConcludeFile = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnConcludeFile.widthHint = 120;
		btnConcludeFile.setLayoutData(gd_btnConcludeFile);
		btnConcludeFile.setText("Cancel Harmonization");
		btnConcludeFile.setEnabled(false);
		new Label(composite, SWT.NONE);
		btnConcludeFile.addSelectionListener(concludeFileListener);
	}

	public static String getTextMetaFileInfo() {
		return textLoadUserData.getToolTipText();
	}

	public static void setTextMetaFileInfo(String metaFileInfo) {
		textLoadUserData.setToolTipText(metaFileInfo);
	}

	public static String getTextFileInfo() {
		return textLoadUserData.getText();
	}

	public static void setTextFileInfo(String fileInfo) {
		textLoadUserData.setText(fileInfo);
	}

	public static String getTextIssues() {
		return textCheckData.getText();
	}

	public static void setTextIssues(String issues) {
		textCheckData.setText(issues);
	}

	public static String getTextCommit() {
		return textCommit.getText();
	}

	public static void setTextCommit(String msg) {
		textCommit.setText(msg);
	}

	public static String getTextMatchFlowables() {
		return textMatchFlowables.getText();
	}

	public static void setTextMatchFlowables(String matchFlowables) {
		textMatchFlowables.setText(matchFlowables);
	}

	public static String getTextFlowContexts() {
		return textMatchFlowContexts.getText();
	}

	public static void setTextFlowContexts(String flowContexts) {
		textMatchFlowContexts.setText(flowContexts);
	}

	public static String getTextFlowProperties() {
		return textMatchFlowProperties.getText();
	}

	public static void setTextFlowProperties(String flowProperties) {
		textMatchFlowProperties.setText(flowProperties);
	}

	private static void initializeOtherViews() {
		Util.findView(LoggerViewer.ID);
		LoggerViewer.clear(); // INITIALIZES SO THAT LOGGER RECEIVES INPUT
	}

	// ------------------- LOAD LISTENER -------------------
	SelectionListener loadUserDataListener = new SelectionListener() {

		private void doit(SelectionEvent e) {

			IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
			try {
				handlerService.executeCommand("gov.epa.nrmrl.std.lca.ht.handler.ImportUserData", null);
			} catch (Exception ex) {
				ex.printStackTrace();
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

	// ------------------- CHECK LISTENER -------------------
	// TODO - move whatever we need into ui threads
	// TODO - disable buttons sensibly
	SelectionListener checkDataListener = new SelectionListener() {
		private void doit(final SelectionEvent e) {
			CSVTableView.clearIssues();
			textCheckData.setText(" ... checking data ...");
			btnCheckData.setEnabled(false);
			btnLoadUserData.setEnabled(false);

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
						}
						else if (lcaDataPropertyProvider.getPropertyClass().equals(FlowContext.label)) {
							checkForRequiredFlowContextFields = true;
						}
						else if (lcaDataPropertyProvider.getPropertyClass().equals(FlowProperty.label)) {
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
													"For each flowable, the " + missingProp
															+ " is required");
										}});
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
													"For each flow context, the " + missingProp
															+ " is required");
										}});
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
													"For each flow property, the " + missingProp
															+ " is required");
										}});
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
										textCheckData.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
										textCheckData.setText("Assign at least one column first)");
										btnMatchFlowables.setEnabled(false);
										btnCommit.setEnabled(false);
									} else {
										int issueCount = CSVTableView.checkCols();
										/* The above function tells CSVTableView to do all the data checking */
										// int issueCount = 0;
										textCheckData.setBackground(SWTResourceManager
												.getColor(SWT.COLOR_INFO_BACKGROUND));

										textCheckData.setText(issueCount + " issues. " + fieldCount
												+ " columns checked");
										if (issueCount == 0) {
											// btnMatchFlowables.setEnabled(true);
											btnCommit.setEnabled(true);
										} else {
											btnMatchFlowables.setEnabled(false);
											btnCommit.setEnabled(false);
										}
									}
									btnCheckData.setEnabled(true);
									btnLoadUserData.setEnabled(true);
								}
							});
						}
					}.start();
				}
			}.start();
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
			btnCommit.setEnabled(false);
			Util.findView(CSVTableView.ID);
			int colsChecked = CSVTableView.countAssignedColumns();
			if (colsChecked == 0) {
				textCommit.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
				textCommit.setText("Assign and check columns first)");
				btnCheckData.setEnabled(true);
				btnMatchFlowables.setEnabled(false);
				return;
			}
			btnCheckData.setEnabled(false);
			btnMatchFlowables.setEnabled(true);
			btnMatchFlowables.setGrayed(false);
			btnMatchFlowContexts.setEnabled(true);
			btnMatchFlowContexts.setGrayed(false);
			btnMatchFlowProperties.setEnabled(true);
			btnMatchFlowProperties.setGrayed(false);
			// CSVTableView.preCommit = false;
			CSVTableView.setPostCommit();
			CSVTableView.initializeRowMenu();
			String jobKey = "autoMatch_01";
			AutoMatchJob autoMatchJob = new AutoMatchJob("FlowsWorkflow Job");
			autoMatchJob.setPriority(Job.SHORT);
			autoMatchJob.setSystem(false);
			autoMatchJob.addJobChangeListener(new AutoMatchJobChangeListener((FlowsWorkflow) Util
					.findView(FlowsWorkflow.ID), jobKey));
			autoMatchJob.schedule();
			btnConcludeFile.setText("Export Harmonized Data");
			// THIS IS AS GOOD A PLACE AS ANY TO SEND THESE 6 REFERENCES:
			CSVTableView.setUniqueFlowableRowNumbers(uniqueFlowableRowNumbers);
			CSVTableView.setUniqueFlowContextRowNumbers(uniqueFlowContextRowNumbers);
			CSVTableView.setUniqueFlowPropertyRowNumbers(uniqueFlowPropertyRowNumbers);
			CSVTableView.setUniqueFlowRowNumbers(uniqueFlowRowNumbers);

			CSVTableView.setMatchedFlowableRowNumbers(matchedFlowableRowNumbers);
			CSVTableView.setMatchedFlowContextRowNumbers(matchedFlowContextRowNumbers);
			CSVTableView.setMatchedFlowPropertyRowNumbers(matchedFlowPropertyRowNumbers);
			CSVTableView.setMatchedFlowRowNumbers(matchedFlowRowNumbers);

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
				btnMatchFlowContexts.setText("Show All Rows");
				btnMatchFlowProperties.setEnabled(false);
				btnMatchFlowables.setEnabled(false);
				CSVTableView.setFilterRowNumbersWCopy(uniqueFlowContextRowNumbers);
				CSVTableView.colorFlowContextRows();
			} else {
				btnMatchFlowContexts.setText(matchContextsString);
				btnMatchFlowProperties.setEnabled(true);
				btnMatchFlowables.setEnabled(true);
				CSVTableView.clearFilterRowNumbers();
				// CSVTableView.colorFlowContextRows();
			}
			// CSVTableView.setSelection(0);
			CSVTableView.setRowNumSelected(0);
			CSVTableView.matchRowContents();
			// Util.setPerspective(FlowDataV4.ID);
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
				btnMatchFlowProperties.setText("Show All Rows");
				btnMatchFlowContexts.setEnabled(false);
				btnMatchFlowables.setEnabled(false);
				CSVTableView.setFilterRowNumbersWCopy(uniqueFlowPropertyRowNumbers);
				CSVTableView.colorFlowPropertyRows();

			} else {
				btnMatchFlowProperties.setText(matchPropertiesString);
				btnMatchFlowContexts.setEnabled(true);
				btnMatchFlowables.setEnabled(true);
				CSVTableView.clearFilterRowNumbers();
			}
			CSVTableView.setRowNumSelected(0);
			CSVTableView.matchRowContents();
			// Util.setPerspective(FlowDataV4.ID);
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
			btnCommit.setEnabled(false);
			btnCheckData.setEnabled(false);
			if (btnMatchFlowables.getText().equals(matchFlowablesString)) {
				btnMatchFlowables.setText("Show All Rows");
				btnMatchFlowContexts.setEnabled(false);
				btnMatchFlowProperties.setEnabled(false);
				CSVTableView.setFilterRowNumbersWCopy(uniqueFlowableRowNumbers);
				CSVTableView.colorFlowableRows();

			} else {
				btnMatchFlowables.setText(matchFlowablesString);
				btnMatchFlowContexts.setEnabled(true);
				btnMatchFlowProperties.setEnabled(true);
				CSVTableView.clearFilterRowNumbers();
			}
			CSVTableView.setRowNumSelected(0);
			CSVTableView.setColNumSelected(1);
			CSVTableView.matchRowContents();

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
//			CSVTableView.clearFilterRowNumbers();
//			CSVTableView.reset();
//			CSVTableView.initialize();
//			CSVTableView.setSelection(0);

//			btnLoadUserData.setEnabled(true);
//			textLoadUserData.setText("");

//			btnCheckData.setEnabled(false);
//			textCheckData.setText("");

//			btnCommit.setEnabled(false);
//			textCommit.setText("");

//			btnMatchFlowables.setEnabled(false);
//			textMatchFlowables.setText("");

//			btnMatchFlowContexts.setEnabled(false);
//			textMatchFlowContexts.setText("");

//			btnMatchFlowProperties.setEnabled(false);
//			textMatchFlowProperties.setText("");

//			btnConcludeFile.setEnabled(false);

//			uniqueFlowContextRowNumbers.clear();
//			uniqueFlowPropertyRowNumbers.clear();
//			uniqueFlowableRowNumbers.clear();
//			uniqueFlowRowNumbers.clear();

//			matchedFlowContextRowNumbers.clear();
//			matchedFlowPropertyRowNumbers.clear();
//			matchedFlowableRowNumbers.clear();
//			matchedFlowRowNumbers.clear();

			if (btnConcludeFile.getText().equals("Export Harmonized Data")) {
				IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
				try {
					handlerService.executeCommand(SaveHarmonizedDataForOLCAJsonld.ID, null);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				// MatchFlowables.initialize();

			} else if (btnConcludeFile.getText().equals("Cancel Harmonization")) {
				CSVTableView.clearFilterRowNumbers();
				CSVTableView.reset();
				CSVTableView.initialize();
				CSVTableView.setSelection(0);

				btnLoadUserData.setEnabled(true);
				textLoadUserData.setText("");

				btnCheckData.setEnabled(false);
				textCheckData.setText("");

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

	public static void addContextRowNum(int rowNumToSend) {
		uniqueFlowContextRowNumbers.add(rowNumToSend);
		textMatchFlowContexts.setText(matchedFlowContextRowNumbers.size() + " matched. "
				+ uniqueFlowContextRowNumbers.size() + " found.");
		CSVTableView.colorOneFlowContextRow(rowNumToSend, matchedFlowContextRowNumbers.contains(rowNumToSend));
	}

	public static void addPropertyRowNum(int rowNumToSend) {
		uniqueFlowPropertyRowNumbers.add(rowNumToSend);
		textMatchFlowProperties.setText(matchedFlowPropertyRowNumbers.size() + " matched. "
				+ uniqueFlowPropertyRowNumbers.size() + " found.");
		CSVTableView.colorOneFlowPropertyRow(rowNumToSend, matchedFlowPropertyRowNumbers.contains(rowNumToSend));
	}

	public static void addFlowableRowNum(int rowNumToSend) {
		uniqueFlowableRowNumbers.add(rowNumToSend);
		textMatchFlowables.setText(matchedFlowableRowNumbers.size() + " matched. " + uniqueFlowableRowNumbers.size()
				+ " found.");
		CSVTableView.colorOneFlowableRow(rowNumToSend);
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
		textMatchFlowContexts.setText(matchedFlowContextRowNumbers.size() + " matched. "
				+ uniqueFlowContextRowNumbers.size() + " found.");
		CSVTableView.colorFlowContextRows();
	}

	public static void addMatchPropertyRowNum(int rowNumToSend) {
		matchedFlowPropertyRowNumbers.add(rowNumToSend);
		textMatchFlowProperties.setText(matchedFlowPropertyRowNumbers.size() + " matched. "
				+ uniqueFlowPropertyRowNumbers.size() + " found.");
		CSVTableView.colorFlowPropertyRows();
		
	}

	public static void addMatchFlowRowNum(int rowNumToSend) {
		matchedFlowRowNumbers.add(rowNumToSend);
		// textMatchFlowProperties.setText(matchedFlowPropertyRowNumbers.size()
		// + " matched. "
		// + uniqueFlowPropertyRowNumbers.size() + " found.");

//		textCommit.setText(matchedFlowRowNumbers.size() + " of " + uniqueFlowRowNumbers.size() + " flows matched");
		CSVTableView.colorFlowRows();
	}

	public static void addMatchFlowableRowNum(int rowNumToSend) {
		// if (!uniqueFlowableRowNumbers.contains(rowNumToSend)){
		// Flowable flowable =
		// TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getData().get(rowNumToSend).getFlowable();
		// int firstRowWithSameFlowable = rowNumToSend;
		// for (int row = rowNumToSend-1;row>=0;row--){
		// Flowable otherFlowable =
		// TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getData().get(row).getFlowable();
		// if (otherFlowable.equals(flowable)){
		// firstRowWithSameFlowable = row;
		// }
		// }
		// if (!uniqueFlowableRowNumbers.contains(firstRowWithSameFlowable)){
		// return;
		// }
		// rowNumToSend = firstRowWithSameFlowable;
		// }
		matchedFlowableRowNumbers.add(rowNumToSend);
		textMatchFlowables.setText(matchedFlowableRowNumbers.size() + " matched. " + uniqueFlowableRowNumbers.size()
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
		textMatchFlowContexts.setText(matchedFlowContextRowNumbers.size() + " matched. "
				+ uniqueFlowContextRowNumbers.size() + " found.");
		// CSVTableView.colorFlowContextRows();
		CSVTableView.colorOneFlowableRow(rowNumber);
	}

	public static void removeMatchPropertyRowNum(int rowNumber) {
		matchedFlowPropertyRowNumbers.remove(rowNumber);
		textMatchFlowProperties.setText(matchedFlowPropertyRowNumbers.size() + " matched. "
				+ uniqueFlowPropertyRowNumbers.size() + " found.");
		CSVTableView.colorFlowPropertyRows();
	}

	public static void removeMatchFlowableRowNum(int rowNumber) {
		matchedFlowableRowNumbers.remove(rowNumber);
		textMatchFlowables.setText(matchedFlowableRowNumbers.size() + " matched. " + uniqueFlowableRowNumbers.size()
				+ " found.");
		// CSVTableView.colorFlowableRows();
		CSVTableView.colorOneFlowableRow(rowNumber);
	}

	public static void removeMatchFlowRowNum(int rowNumber) {
		matchedFlowRowNumbers.remove(rowNumber);
		// textCommit.setText(matchedFlowRowNumbers.size()+" of "+uniqueFlowRowNumbers.size()+" flows matched");
		CSVTableView.colorFlowRows();
	}

	public static void updateFlowableCount() {
		textMatchFlowables.setText(matchedFlowableRowNumbers.size() + " matched. " + uniqueFlowableRowNumbers.size()
				+ " found.");
	}
}
