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
import gov.epa.nrmrl.std.lca.ht.perspectives.LCIWorkflowPerspective;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

//import org.eclipse.swt.events.SelectionAdapter;

public class FlowsWorkflow extends ViewPart {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow";

	private static Text textLoadCSV;
	private static Text textCheckData;
	private static Text textCommit;
	private static Text textMatchFlowables;
	private static Text textMatchFlowContexts;
	private static Text textMatchFlowProperties;

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

	private static Button btnLoadCSV;
	private static Button btnCheckData;
	private static Button btnCommit;
	private static Button btnMatchFlowables;
	private static Button btnMatchFlowContexts;
	private static Button btnMatchFlowProperties;
	private static Button btnConcludeFile;

	private static LinkedHashSet<Integer> uniqueFlowableRowNumbers = new LinkedHashSet<Integer>();
	private static LinkedHashSet<Integer> uniqueFlowContextRowNumbers = new LinkedHashSet<Integer>();
	private static LinkedHashSet<Integer> uniqueFlowPropertyRowNumbers = new LinkedHashSet<Integer>();

	private static LinkedHashSet<Integer> matchedFlowableRowNumbers = new LinkedHashSet<Integer>();
	private static LinkedHashSet<Integer> matchedFlowContextRowNumbers = new LinkedHashSet<Integer>();
	private static LinkedHashSet<Integer> matchedFlowPropertyRowNumbers = new LinkedHashSet<Integer>();

	// private static FileMD fileMD;
	// private static DataSourceProvider dataSourceProvider;

	// private static TableProvider tableProvider;

	public FlowsWorkflow() {
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}

	@Override
	public void createPartControl(Composite parent) {
		initializeOtherViews();
		// Util.findView(LoggerViewer.ID);
		// LoggerViewer.clear(); // INITIALIZES SO THAT LOGGER RECEIVES INPUT

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

		btnLoadCSV = new Button(composite, SWT.NONE);
		GridData gd_btnLoadCSV = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnLoadCSV.widthHint = 120;
		btnLoadCSV.setLayoutData(gd_btnLoadCSV);
		btnLoadCSV.setText("Load CSV Data");

		btnLoadCSV.addSelectionListener(loadCSVListener);

		textLoadCSV = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		textLoadCSV.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));

		GridData gd_textFileInfo = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textFileInfo.widthHint = 150;
		textLoadCSV.setLayoutData(gd_textFileInfo);

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

		textCheckData = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		textCheckData.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
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
		GridData gd_btnCSV2TDB = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnCSV2TDB.widthHint = 120;
		btnCommit.setLayoutData(gd_btnCSV2TDB);
		btnCommit.setText("Commit");
		btnCommit.addSelectionListener(commitListener);

		textCommit = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		textCommit.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		textCommit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		// ======== ROW 4 =======================

		label_04 = new Label(composite, SWT.NONE);
		GridData gd_label_04 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_label_04.widthHint = 20;
		label_04.setLayoutData(gd_label_04);
		label_04.setText("4");

		btnMatchFlowContexts = new Button(composite, SWT.NONE);
		GridData gd_btnMatchFlowContexts = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		btnMatchFlowContexts.addSelectionListener(matchFlowContextsListener);

		// gd_btnMatchFlowContexts.heightHint = 45;
		gd_btnMatchFlowContexts.widthHint = 120;
		btnMatchFlowContexts.setLayoutData(gd_btnMatchFlowContexts);
		btnMatchFlowContexts.setText(matchContextsString);
		btnMatchFlowContexts.setEnabled(false);
		// btnMatchFlowContexts.addSelectionListener(matchFlowContextsListener);
		// TODO - implement above

		textMatchFlowContexts = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		textMatchFlowContexts.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		// textMatchFlowContexts.setText("(0 of 30");
		GridData gd_textMatchFlowContexts = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		// gd_textMatchFlowContexts.heightHint = 45;
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

		// gd_btnMatchFlowProperties.heightHint = 45;
		gd_btnMatchFlowProperties.widthHint = 120;
		btnMatchFlowProperties.setLayoutData(gd_btnMatchFlowProperties);
		btnMatchFlowProperties.setText(matchPropertiesString);
		btnMatchFlowProperties.setEnabled(false);
		// btnMatchFlowProperties.addSelectionListener(matchFlowPropertiesListener);
		// TODO - implement above

		textMatchFlowProperties = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		textMatchFlowProperties.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		// textMatchFlowProperties.setText("(0 of 30");
		GridData gd_textMatchFlowProperties = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		// gd_textMatchFlowProperties.heightHint = 45;
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
		// gd_btnMatchFlowables.heightHint = 45;
		gd_btnMatchFlowables.widthHint = 120;
		btnMatchFlowables.setLayoutData(gd_btnMatchFlowables);
		btnMatchFlowables.setText(matchFlowablesString);

		btnMatchFlowables.setEnabled(false);
		btnMatchFlowables.addSelectionListener(matchFlowablesListener);
		// TODO - implement above

		textMatchFlowables = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		textMatchFlowables.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		// textMatchFlowables.setText("(430 of 600 rows match)");
		GridData gd_textMatchFlowables = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		// gd_textMatchFlowables.heightHint = 45;
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
		btnConcludeFile.setText("Cancel CSV");
		btnConcludeFile.setEnabled(false);
		new Label(composite, SWT.NONE);
		btnConcludeFile.addSelectionListener(concludeFileListener);
	}

	public static String getTextMetaFileInfo() {
		return textLoadCSV.getToolTipText();
	}

	public static void setTextMetaFileInfo(String metaFileInfo) {
		textLoadCSV.setToolTipText(metaFileInfo);
	}

	public static String getTextFileInfo() {
		return textLoadCSV.getText();
	}

	public static void setTextFileInfo(String fileInfo) {
		textLoadCSV.setText(fileInfo);
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
	SelectionListener loadCSVListener = new SelectionListener() {

		private void doit(SelectionEvent e) {

			IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
			try {
				handlerService.executeCommand("gov.epa.nrmrl.std.lca.ht.handler.ImportCSV", null);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			String key = CSVTableView.getTableProviderKey();
			if (key == null) {
				System.out.println("The CSVTableView does not have a table!");
			} else {
				btnLoadCSV.setEnabled(false);
				btnConcludeFile.setEnabled(true);

				textLoadCSV.setText(TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getFileMD()
						.getFilename());
				textLoadCSV.setToolTipText(TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getFileMD()
						.getPath());
				btnCheckData.setEnabled(true);
				System.out.println("About to do setHeaderInfo()");
				// setHeaderInfo();
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
	SelectionListener checkDataListener = new SelectionListener() {
		private void doit(SelectionEvent e) {
			CSVTableView.clearIssues();
			textCheckData.setText(" ... checking data ...");
			// CSVColumnInfo[] csvColumnInfos =
			// TableKeeper.getTableProvider(CSVTableView.getTableProviderKey())
			// .getAssignedCSVColumnInfo();
			// CSVColumnInfo[] csvColumnInfos = null; // <== FIXME COMMENT OUT
			// THIS HACK, THEN FIX RED BELOW
			LCADataPropertyProvider[] lcaDataPropreties = TableKeeper.getTableProvider(
					CSVTableView.getTableProviderKey()).getLcaDataProperties();
			// List<CSVColumnInfo> requiredCSVColumnInfo = new
			// ArrayList<CSVColumnInfo>();
			List<LCADataPropertyProvider> requiredLCADataPropertyProvider = new ArrayList<LCADataPropertyProvider>();
			boolean checkForRequiredFlowableFields = false;
			boolean checkForRequiredFlowContextFields = false;
			boolean checkForRequiredFlowPropertyFields = false;

			int countOfAssignedFields = 0;
			// for (CSVColumnInfo csvColumnInfo : csvColumnInfos) {
			for (LCADataPropertyProvider lcaDataPropertyProvider : lcaDataPropreties) {
				if (lcaDataPropertyProvider == null) {
					continue;
				}
				countOfAssignedFields++;
				if (lcaDataPropertyProvider.getPropertyClass().equals(Flowable.label)) {
					checkForRequiredFlowableFields = true;
				}
				if (lcaDataPropertyProvider.getPropertyClass().equals(FlowContext.label)) {
					checkForRequiredFlowContextFields = true;
				}
				if (lcaDataPropertyProvider.getPropertyClass().equals(FlowProperty.label)) {
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
								continue;
							}
						}
						if (found == false) {
							new GenericMessageBox(e.display.getActiveShell(), "Missing Assignment",
									"For each flowable, the " + requiredLCADataProperty.getPropertyName()
											+ " is required");
						}
					}
				}
			}
			if (checkForRequiredFlowContextFields) {
				for (LCADataPropertyProvider requiredLCADataProperty : FlowContext.getDataPropertyMap().values()) {
					if (requiredLCADataProperty.isRequired()) {
						boolean found = false;
						for (LCADataPropertyProvider gotIt : requiredLCADataPropertyProvider) {
							if (gotIt.sameAs(requiredLCADataProperty)) {
								found = true;
								continue;
							}
						}
						if (found == false) {
							new GenericMessageBox(e.display.getActiveShell(), "Missing Assignment",
									"For each flow context, the " + requiredLCADataProperty.getPropertyName()
											+ " is required");
						}
					}
				}
			}
			if (checkForRequiredFlowPropertyFields) {
				for (LCADataPropertyProvider requiredLCADataProperty : FlowProperty.getDataPropertyMap().values()) {
					if (requiredLCADataProperty.isRequired()) {
						boolean found = false;
						for (LCADataPropertyProvider gotIt : requiredLCADataPropertyProvider) {
							if (gotIt.sameAs(requiredLCADataProperty)) {
								found = true;
								continue;
							}
						}
						if (found == false) {
							new GenericMessageBox(e.display.getActiveShell(), "Missing Assignment",
									"For each flow property, the " + requiredLCADataProperty.getPropertyName()
											+ " is required");
						}
					}
				}
			}

			// int colsChecked = CSVTableView.countAssignedColumns();
			// int colsChecked = 0;

			if (countOfAssignedFields == 0) {
				textCheckData.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
				textCheckData.setText("Assign at least one column first)");
				btnMatchFlowables.setEnabled(false);
				btnCommit.setEnabled(false);
			} else {
				int issueCount = CSVTableView.checkCols();
				// int issueCount = 0;
				textCheckData.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));

				textCheckData.setText(issueCount + " issues. " + countOfAssignedFields + " columns checked");
				if (issueCount == 0) {
					// btnMatchFlowables.setEnabled(true);
					btnCommit.setEnabled(true);
				} else {
					btnMatchFlowables.setEnabled(false);
					btnCommit.setEnabled(false);
				}
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
			btnMatchFlowContexts.setEnabled(true);
			btnMatchFlowProperties.setEnabled(true);
			CSVTableView.preCommit = false;
			CSVTableView.initializeRowMenu();
			String jobKey = "autoMatch_01";
			AutoMatchJob autoMatchJob = new AutoMatchJob("FlowsWorkflow Job");
			autoMatchJob.setPriority(Job.SHORT);
			autoMatchJob.setSystem(false);
			autoMatchJob.addJobChangeListener(new AutoMatchJobChangeListener((FlowsWorkflow) Util
					.findView(FlowsWorkflow.ID), jobKey));
			autoMatchJob.schedule();
			btnConcludeFile.setText("Close CSV");
			// THIS IS AS GOOD A PLACE AS ANY TO SEND THESE 6 REFERENCES:
			CSVTableView.setUniqueFlowableRowNumbers(uniqueFlowableRowNumbers);
			CSVTableView.setUniqueFlowContextRowNumbers(uniqueFlowContextRowNumbers);
			CSVTableView.setUniqueFlowPropertyRowNumbers(uniqueFlowPropertyRowNumbers);
			CSVTableView.setMatchedFlowableRowNumbers(matchedFlowableRowNumbers);
			CSVTableView.setMatchedFlowContextRowNumbers(matchedFlowContextRowNumbers);
			CSVTableView.setMatchedFlowPropertyRowNumbers(matchedFlowPropertyRowNumbers);
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
			CSVTableView.setSelection(0);
			CSVTableView.matchRowContents();
//			Util.setPerspective(FlowDataV4.ID);
//			Util.setPerspective(LCIWorkflowPerspective.ID);
			try {
				Util.showView(MatchContexts.ID);
			} catch (PartInitException e1) {
				// TODO Auto-generated catch block
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
			CSVTableView.setSelection(0);
			CSVTableView.matchRowContents();
//			Util.setPerspective(FlowDataV4.ID);
//			Util.setPerspective(LCIWorkflowPerspective.ID);
			try {
				Util.showView(MatchProperties.ID);
			} catch (PartInitException e1) {
				// TODO Auto-generated catch block
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
				// CSVTableView.colorFlowableRows();
			}
			CSVTableView.setSelection(0);
			CSVTableView.matchRowContents();
//			Util.setPerspective(FlowDataV5.ID);
//			Util.setPerspective(LCIWorkflowPerspective.ID);

			try {
				Util.showView(MatchFlowables.ID);
			} catch (PartInitException e1) {
				// TODO Auto-generated catch block
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
			CSVTableView.clearFilterRowNumbers();
			CSVTableView.reset();
			CSVTableView.initialize();
			CSVTableView.setSelection(0);

			btnLoadCSV.setEnabled(true);
			textLoadCSV.setText("");

			btnCheckData.setEnabled(false);
			textCheckData.setText("");

			btnCommit.setEnabled(false);
			textCommit.setText("");

			btnMatchFlowables.setEnabled(false);
			textMatchFlowables.setText("");

			btnMatchFlowContexts.setEnabled(false);
			textMatchFlowContexts.setText("");

			btnMatchFlowProperties.setEnabled(false);
			textMatchFlowProperties.setText("");

			btnConcludeFile.setEnabled(false);
			
			uniqueFlowContextRowNumbers.clear();
			uniqueFlowPropertyRowNumbers.clear();
			uniqueFlowableRowNumbers.clear();
			matchedFlowContextRowNumbers.clear();
			matchedFlowPropertyRowNumbers.clear();
			matchedFlowableRowNumbers.clear();

			if (btnConcludeFile.getText().equals("Close CSV")) {
				MatchFlowables.initialize();
				// MatchContexts.initialize();
				// MatchProperties.initialize();

				// TODO - CONFIRM WITH USER?
				// TODO - INDICATE THAT FILE CONTENT WAS SAVED
			} else if (btnConcludeFile.getText().equals("Cancel CSV")) {
				// TODO - CONFIRM WITH USER
				// TODO - REMOVE THE FileMD
				// TODO - INDICATE THAT FILE CONTENT WAS REMOVED
			}
//			Util.setPerspective(FlowDataV4.ID);
			Util.setPerspective(LCIWorkflowPerspective.ID);

			try {
				Util.showView(LoggerViewer.ID);
			} catch (PartInitException e1) {
				// TODO Auto-generated catch block
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
		btnMatchFlowables.setEnabled(false);
	}

	public static void disableContextBtn() {
		btnMatchFlowContexts.setEnabled(false);
	}

	public static void disablePropertyBtn() {
		btnMatchFlowProperties.setEnabled(false);
	}

	public static void addContextRowNum(int rowNumToSend) {
		uniqueFlowContextRowNumbers.add(rowNumToSend);
		textMatchFlowContexts.setText(matchedFlowContextRowNumbers.size() + " matched. "
				+ uniqueFlowContextRowNumbers.size() + " found.");
		CSVTableView.colorFlowContextRows();
	}

	public static void addPropertyRowNum(int rowNumToSend) {
		uniqueFlowPropertyRowNumbers.add(rowNumToSend);
		textMatchFlowProperties.setText(matchedFlowPropertyRowNumbers.size() + " matched. "
				+ uniqueFlowPropertyRowNumbers.size() + " found.");
		CSVTableView.colorFlowPropertyRows();
	}

	public static void addFlowableRowNum(int rowNumToSend) {
		uniqueFlowableRowNumbers.add(rowNumToSend);
		textMatchFlowables.setText(matchedFlowableRowNumbers.size() + " matched. " + uniqueFlowableRowNumbers.size()
				+ " found.");
//		CSVTableView.colorFlowableRows();
		CSVTableView.colorOneFlowableRow(rowNumToSend);

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

	public static void addMatchFlowableRowNum(int rowNumToSend) {
//		if (!uniqueFlowableRowNumbers.contains(rowNumToSend)){
//			Flowable flowable = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getData().get(rowNumToSend).getFlowable();
//			int firstRowWithSameFlowable = rowNumToSend;
//			for (int row = rowNumToSend-1;row>=0;row--){
//				Flowable otherFlowable = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getData().get(row).getFlowable();
//				if (otherFlowable.equals(flowable)){
//					firstRowWithSameFlowable = row;
//				}
//			}
//			if (!uniqueFlowableRowNumbers.contains(firstRowWithSameFlowable)){
//				return;
//			}
//			rowNumToSend = firstRowWithSameFlowable;
//		}
		matchedFlowableRowNumbers.add(rowNumToSend);
		textMatchFlowables.setText(matchedFlowableRowNumbers.size() + " matched. " + uniqueFlowableRowNumbers.size()
				+ " found.");
		CSVTableView.colorOneFlowableRow(rowNumToSend);
	}

	public static void removeMatchContextRowNum(int rowNumber) {
//		if (!uniqueFlowableRowNumbers.contains(rowNumber)){
//			Flowable flowable = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getData().get(rowNumber).getFlowable();
//			int firstRowWithSameFlowable = rowNumber;
//			for (int row = rowNumber-1;row>=0;row--){
//				Flowable otherFlowable = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getData().get(row).getFlowable();
//				if (otherFlowable.equals(flowable)){
//					firstRowWithSameFlowable = row;
//				}
//			}
//			if (!uniqueFlowableRowNumbers.contains(firstRowWithSameFlowable)){
//				return;
//			}
//			rowNumber = firstRowWithSameFlowable;
//		}
		matchedFlowContextRowNumbers.remove(rowNumber);
		textMatchFlowContexts.setText(matchedFlowContextRowNumbers.size() + " matched. "
				+ uniqueFlowContextRowNumbers.size() + " found.");
//		CSVTableView.colorFlowContextRows();
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
//		CSVTableView.colorFlowableRows();
		CSVTableView.colorOneFlowableRow(rowNumber);
	}

	public static void updateFlowableCount() {
		textMatchFlowables.setText(matchedFlowableRowNumbers.size() + " matched. " + uniqueFlowableRowNumbers.size()
				+ " found.");
	}
}
