package gov.epa.nrmrl.std.lca.ht.workflows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVColumnInfo;
import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.Flow;
import gov.epa.nrmrl.std.lca.ht.dataModels.FlowContext;
import gov.epa.nrmrl.std.lca.ht.dataModels.FlowProperty;
import gov.epa.nrmrl.std.lca.ht.dataModels.Flowable;
import gov.epa.nrmrl.std.lca.ht.dataModels.MatchCandidate;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.job.AutoMatchJob;
import gov.epa.nrmrl.std.lca.ht.job.AutoMatchJobChangeListener;
import gov.epa.nrmrl.std.lca.ht.perspectives.FlowData;
import gov.epa.nrmrl.std.lca.ht.perspectives.FlowDataWMatchFlowables;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.dialog.GenericMessageBox;
import harmonizationtool.utils.Util;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FASC;
import harmonizationtool.vocabulary.FEDLCA;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import org.eclipse.swt.events.SelectionAdapter;

//import org.eclipse.swt.widgets.Canvas;

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

	private static Button btnLoadCSV;
	private static Button btnCheckData;
	private static Button btnCommit;
	private static Button btnMatchFlowables;
	private static Button btnMatchFlowContexts;
	private static Button btnMatchFlowProperties;
	private static Button btnConcludeFile;

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

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		composite.setLayout(new GridLayout(3, false));
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

		btnMatchFlowables = new Button(composite, SWT.NONE);
		btnMatchFlowables.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		GridData gd_btnMatchFlowables = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnMatchFlowables.heightHint = 45;
		gd_btnMatchFlowables.widthHint = 120;
		btnMatchFlowables.setLayoutData(gd_btnMatchFlowables);
		btnMatchFlowables.setText("Match"+System.getProperty("line.separator")+"Flowables");
		btnMatchFlowables.setEnabled(false);
		// btnMatchFlowables.addSelectionListener(matchFlowablesListener);
		// TODO - implement above

		textMatchFlowables = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		textMatchFlowables.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		// textMatchFlowables.setText("(430 of 600 rows match)");
		GridData gd_textMatchFlowables = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textMatchFlowables.heightHint = 45;
		gd_textMatchFlowables.widthHint = 120;
		textMatchFlowables.setLayoutData(gd_textMatchFlowables);

		// ======== ROW 5 =======================

		label_05 = new Label(composite, SWT.NONE);
		GridData gd_label_05 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_label_05.widthHint = 20;
		label_05.setLayoutData(gd_label_05);
		label_05.setText("5");

		btnMatchFlowContexts = new Button(composite, SWT.NONE);
		GridData gd_btnMatchFlowContexts = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnMatchFlowContexts.heightHint = 45;
		gd_btnMatchFlowContexts.widthHint = 120;
		btnMatchFlowContexts.setLayoutData(gd_btnMatchFlowContexts);
		btnMatchFlowContexts.setText("Match"+System.getProperty("line.separator")+"Flow Contexts");
		btnMatchFlowContexts.setEnabled(false);
		// btnMatchFlowContexts.addSelectionListener(matchFlowContextsListener);
		// TODO - implement above

		textMatchFlowContexts = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		textMatchFlowContexts.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		// textMatchFlowContexts.setText("(0 of 30");
		GridData gd_textMatchFlowContexts = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textMatchFlowContexts.heightHint = 45;
		gd_textMatchFlowContexts.widthHint = 150;
		textMatchFlowContexts.setLayoutData(gd_textMatchFlowContexts);

		// ======== ROW 6 =======================

		label_06 = new Label(composite, SWT.NONE);
		GridData gd_label_06 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_label_06.widthHint = 20;
		label_06.setLayoutData(gd_label_06);
		label_06.setText("6");

		btnMatchFlowProperties = new Button(composite, SWT.NONE);
		GridData gd_btnMatchFlowProperties = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnMatchFlowProperties.heightHint = 45;
		gd_btnMatchFlowProperties.widthHint = 120;
		btnMatchFlowProperties.setLayoutData(gd_btnMatchFlowProperties);
		btnMatchFlowProperties.setText("Match"+System.getProperty("line.separator")+"Flow Properties");
		btnMatchFlowProperties.setEnabled(false);
		// btnMatchFlowProperties.addSelectionListener(matchFlowPropertiesListener);
		// TODO - implement above
		
		textMatchFlowProperties = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		textMatchFlowProperties.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		// textMatchFlowProperties.setText("(0 of 30");
		GridData gd_textMatchFlowProperties = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textMatchFlowProperties.heightHint = 45;
		gd_textMatchFlowProperties.widthHint = 150;
		textMatchFlowProperties.setLayoutData(gd_textMatchFlowProperties);

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

	private static void setHeaderInfo() {
		if (Util.findView(CSVTableView.ID) == null) {
			try {
				Util.showView(CSVTableView.ID);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
		if (CSVTableView.getTableProviderKey() == null) {
			return;
		}

		CSVTableView.appendToAvailableCSVColumnInfo("Assign Flowable Fields", Flowable.getHeaderMenuObjects());
		CSVTableView.appendToAvailableCSVColumnInfo("Assign Flow Context Fields", FlowContext.getHeaderMenuObjects());
		CSVTableView.appendToAvailableCSVColumnInfo("Assign Flow Property Fields", FlowProperty.getHeaderMenuObjects());

	}

	SelectionListener loadCSVListener = new SelectionListener() {

		private void doit(SelectionEvent e) {
			IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
			try {
				handlerService.executeCommand("harmonizationtool.handler.ImportCSV", null);
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
				setHeaderInfo();
				btnCheckData.setEnabled(true);
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

	SelectionListener checkDataListener = new SelectionListener() {
		private void doit(SelectionEvent e) {
			textCheckData.setText(" ... checking data ...");
			CSVColumnInfo[] csvColumnInfos = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey())
					.getAssignedCSVColumnInfo();
			List<CSVColumnInfo> requiredCSVColumnInfo = new ArrayList<CSVColumnInfo>();
			boolean checkForRequiredFlowableFields = false;
			boolean checkForRequiredFlowContextFields = false;
			boolean checkForRequiredFlowPropertyFields = false;

			int countOfAssignedFields = 0;
			for (CSVColumnInfo csvColumnInfo : csvColumnInfos) {
				if (csvColumnInfo == null) {
					continue;
				}
				countOfAssignedFields++;
				if (csvColumnInfo.sameRDFClassAs(Flowable.getRdfclass())) {
					checkForRequiredFlowableFields = true;
				}
				if (csvColumnInfo.sameRDFClassAs(FlowContext.getRdfclass())) {
					checkForRequiredFlowContextFields = true;
				}
				if (csvColumnInfo.sameRDFClassAs(FlowProperty.getRdfclass())) {
					checkForRequiredFlowPropertyFields = true;
				}
				if (csvColumnInfo.isRequired()) {
					requiredCSVColumnInfo.add(csvColumnInfo);
				}
			}
			if (checkForRequiredFlowableFields) {
				for (CSVColumnInfo requiredFlowableCSVColumnInfo : Flowable.getHeaderMenuObjects()) {
					if (requiredFlowableCSVColumnInfo.isRequired()) {
						boolean found = false;
						for (CSVColumnInfo gotIt : requiredCSVColumnInfo) {
							if (gotIt != null) {
								if (gotIt.sameRDFClassAs(requiredFlowableCSVColumnInfo)) {
									found = true;
								}
							}
						}
						if (found == false) {
							new GenericMessageBox(e.display.getActiveShell(), "Missing Assignment",
									"For each flowable, the " + requiredFlowableCSVColumnInfo.getHeaderString()
											+ " is required");
						}
					}
				}
			}
			if (checkForRequiredFlowContextFields) {
				for (CSVColumnInfo requiredFlowContextCSVColumnInfo : FlowContext.getHeaderMenuObjects()) {
					if (requiredFlowContextCSVColumnInfo.isRequired()) {
						boolean found = false;
						for (CSVColumnInfo gotIt : requiredCSVColumnInfo) {
							if (gotIt != null) {
								if (gotIt.sameRDFClassAs(requiredFlowContextCSVColumnInfo)) {
									found = true;
								}
							}
						}
						if (found == false) {
							new GenericMessageBox(e.display.getActiveShell(), "Missing Assignment",
									"For each flow context, the " + requiredFlowContextCSVColumnInfo.getHeaderString()
											+ " is required");
						}
					}
				}
			}
			if (checkForRequiredFlowPropertyFields) {
				for (CSVColumnInfo requiredFlowPropertyCSVColumnInfo : FlowProperty.getHeaderMenuObjects()) {
					if (requiredFlowPropertyCSVColumnInfo.isRequired()) {
						boolean found = false;
						for (CSVColumnInfo gotIt : requiredCSVColumnInfo) {
							if (gotIt != null) {
								if (gotIt.sameRDFClassAs(requiredFlowPropertyCSVColumnInfo)) {
									found = true;
								}
							}
						}
						if (found == false) {
							new GenericMessageBox(e.display.getActiveShell(), "Missing Assignment",
									"For each flow property, the " + requiredFlowPropertyCSVColumnInfo.getHeaderString()
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

	SelectionListener commitListener = new SelectionListener() {

		private void doit(SelectionEvent e) {
			btnCommit.setEnabled(false);
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

			CSVTableView.initializeRowMenu(2);
			String jobKey = "autoMatch_01";
			AutoMatchJob autoMatchJob = new AutoMatchJob("FlowsWorkflow Job");
			autoMatchJob.setPriority(Job.SHORT);
			autoMatchJob.setSystem(false);
			autoMatchJob.addJobChangeListener(new AutoMatchJobChangeListener((FlowsWorkflow) Util
					.findView(FlowsWorkflow.ID), jobKey));
			autoMatchJob.schedule();


			btnConcludeFile.setText("Close CSV");
//			try {
//				Util.showView(FlowDataWMatchFlowables.ID);
//			} catch (PartInitException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			Util.setPerspective(FlowDataWMatchFlowables.ID);

			// return;

			// int colsChecked = CSVTableView.countAssignedColumns();
			// else {
			// int triples = safeCommitColumns2TDB();
			// // triples += safeCommitFlowContexts2TDB();
			// textCommit.setText(triples + " triples added");
			// if (triples == 0) {
			// textCommit.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
			// } else {
			// textCommit.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
			// }
			// btnMatchFlowables.setEnabled(true);
			// btnConcludeFile.setText("Close CSV");
			// }
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

//	SelectionListener autoMatchListener = new SelectionListener() {
//		private void doit(SelectionEvent e) {
//			String jobKey = "autoMatch_01";
//			AutoMatchJob autoMatchJob = new AutoMatchJob("FlowsWorkflow Job");
//			autoMatchJob.setPriority(Job.SHORT);
//			autoMatchJob.setSystem(false);
//			autoMatchJob.addJobChangeListener(new AutoMatchJobChangeListener((FlowsWorkflow) Util
//					.findView(FlowsWorkflow.ID), jobKey));
//			autoMatchJob.schedule();
//		}
//
//		@Override
//		public void widgetSelected(SelectionEvent e) {
//			doit(e);
//		}
//
//		@Override
//		public void widgetDefaultSelected(SelectionEvent e) {
//			doit(e);
//		}
//	};

	SelectionListener concludeFileListener = new SelectionListener() {
		private void doit(SelectionEvent e) {
			CSVTableView.reset();
			CSVTableView.initialize();
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

			if (btnConcludeFile.getText().equals("Close CSV")) {
				// TODO - CONFIRM WITH USER?
				// TODO - INDICATE THAT FILE CONTENT WAS SAVED
			} else if (btnConcludeFile.getText().equals("Cancel CSV")) {
				// TODO - CONFIRM WITH USER
				// TODO - REMOVE THE FileMD
				// TODO - INDICATE THAT FILE CONTENT WAS REMOVED
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

//	private int safeCommitColumns2TDB() {
//		// TODO - IMPLEMENT THE "SAFE" PART WHICH MEANS
//		// PRIOR TO ADDING TRIPLES, PREVIOUSLY ADDED
//		// TRIPLES FROM THIS FILE SHOULD BE REMOVED -- OR...
//		// BETTER YET, A THOUGHTFUL PROCESS AVOIDS DUPLICATE TRIPLES
//		// Model model = ActiveTDB.tdbModel;
//
//		List<Integer> rowsToIgnore = CSVTableView.getRowsToIgnore();
//
//		DataSourceProvider dataSourceProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey())
//				.getDataSourceProvider();
//
//		Map<String, Flowable> flowableMap = new HashMap<String, Flowable>();
//		Map<String, FlowContext> flowContextMap = new HashMap<String, FlowContext>();
//		List<Flow> flows = new ArrayList<Flow>();
//
//		long triples = ActiveTDB.tdbModel.size();
//		Table table = CSVTableView.getTable();
//
//		CSVColumnInfo[] assignedCSVColumns = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey())
//				.getAssignedCSVColumnInfo();
//		List<Integer> flowableCSVColumnNumbers = new ArrayList<Integer>();
//		List<Integer> flowContextCSVColumnNumbers = new ArrayList<Integer>();
//
//		for (int i = 0; i < assignedCSVColumns.length; i++) {
//			CSVColumnInfo csvColumnInfo = assignedCSVColumns[i];
//			if (csvColumnInfo == null) {
//				continue;
//			}
//			if (csvColumnInfo.getRDFClass().equals(Flowable.getRdfclass())) {
//				flowableCSVColumnNumbers.add(i);
//			} else if (csvColumnInfo.getRDFClass().equals(FlowContext.getRdfclass())) {
//				flowContextCSVColumnNumbers.add(i);
//			}
//		}
//		for (int rowNumber = 0; rowNumber < table.getItemCount(); rowNumber++) {
//			if (rowsToIgnore.contains(rowNumber)) {
//				continue;
//			}
//			Flowable flowable = null;
//			FlowContext flowContext = null;
//			int rowNumberPlusOne = rowNumber + 1;
//			// Literal rowLiteral =
//			// ActiveTDB.createTypedLiteral(rowNumberPlusOne);
//
//			Item item = table.getItem(rowNumber);
//			DataRow dataRow = (DataRow) item.getData();
//
//			String flowableConcatinated = "";
//			for (int i : flowableCSVColumnNumbers) {
//				flowableConcatinated += dataRow.get(i) + "\t";
//			}
//			if (!flowableConcatinated.equals("")) {
//				if (flowableMap.containsKey(flowableConcatinated)) {
//					flowable = flowableMap.get(flowableConcatinated);
//				} else {
//					flowable = new Flowable();
//					flowableMap.put(flowableConcatinated, flowable);
//					ActiveTDB.replaceResource(flowable.getTdbResource(), ECO.hasDataSource,
//							dataSourceProvider.getTdbResource());
//					for (int i : flowableCSVColumnNumbers) {
//						CSVColumnInfo csvColumnInfo = assignedCSVColumns[i];
//						if (csvColumnInfo.isUnique()) {
//							ActiveTDB.replaceLiteral(flowable.getTdbResource(), csvColumnInfo.getTdbProperty(),
//							// dataRow.getCSVTableIndex(i));
//									dataRow.get(i));
//
//						} else {
//							ActiveTDB.addLiteral(flowable.getTdbResource(), csvColumnInfo.getTdbProperty(),
//							// dataRow.getCSVTableIndex(i));
//									dataRow.get(i));
//						}
//					}
//				}
//				ActiveTDB.addLiteral(flowable.getTdbResource(), FEDLCA.sourceTableRowNumber, rowNumberPlusOne);
//			}
//
//			String flowContextConcatinated = "";
//			for (int i : flowContextCSVColumnNumbers) {
//				flowContextConcatinated += dataRow.get(i) + "\t";
//			}
//			if (!flowContextConcatinated.equals("")) {
//				if (flowContextMap.containsKey(flowContextConcatinated)) {
//					flowContext = flowContextMap.get(flowContextConcatinated);
//				} else {
//					flowContext = new FlowContext();
//					flowContextMap.put(flowContextConcatinated, flowContext);
//					ActiveTDB.replaceResource(flowContext.getTdbResource(), ECO.hasDataSource,
//							dataSourceProvider.getTdbResource());
//					for (int i : flowContextCSVColumnNumbers) {
//						CSVColumnInfo csvColumnInfo = assignedCSVColumns[i];
//						if (csvColumnInfo.isUnique()) {
//							ActiveTDB.replaceLiteral(flowContext.getTdbResource(), csvColumnInfo.getTdbProperty(),
//									dataRow.get(i));
//						} else {
//							ActiveTDB.addLiteral(flowContext.getTdbResource(), csvColumnInfo.getTdbProperty(),
//									dataRow.get(i));
//						}
//					}
//				}
//				ActiveTDB.addLiteral(flowContext.getTdbResource(), FEDLCA.sourceTableRowNumber, rowNumberPlusOne);
//
//			}
//			if (flowable != null && flowContext != null) {
//				Flow tempFlow = new Flow();
//				tempFlow.setFlowable(flowable);
//				tempFlow.setFlowContext(flowContext);
//				if (flows.contains(tempFlow)) {
//					tempFlow.remove();
//				} else {
//					ActiveTDB.replaceResource(tempFlow.getTdbResource(), ECO.hasDataSource,
//							dataSourceProvider.getTdbResource());
//					ActiveTDB.addLiteral(tempFlow.getTdbResource(), FEDLCA.sourceTableRowNumber, rowNumberPlusOne);
//					flows.add(tempFlow);
//				}
//			}
//		}
//		long newTriples = ActiveTDB.tdbModel.size() - triples;
//		return (int) newTriples;
//	}

	private void autoMatch_01() {
		List<MatchCandidate> matchCandidates = new ArrayList<MatchCandidate>();
		DataSourceProvider dataSourceProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey())
				.getDataSourceProvider();

		// Model model = ActiveTDB.tdbModel;
		Resource dataSourceTDBResource = dataSourceProvider.getTdbResource();
		// ResIterator resourceIterator =
		// tdbModel.listResourcesWithProperty(ECO.hasDataSource,
		// dataSourceTDBResource.asNode());
		ResIterator resourceIterator = ActiveTDB.tdbModel.listResourcesWithProperty(ECO.hasDataSource,
				dataSourceTDBResource);
		int count = 0;
		while (resourceIterator.hasNext()) {
			count++;
			System.out.println("count: " + count);
			Resource dataItem = resourceIterator.next();
			int rowNumber = -1;
			Statement rowNumberStatement = dataItem.getProperty(FEDLCA.sourceTableRowNumber);
			if (rowNumberStatement != null) {
				if (rowNumberStatement.getObject().isLiteral()) {
					rowNumber = rowNumberStatement.getObject().asLiteral().getInt();
					// System.out.println("rowNumber = " + rowNumber);
					StmtIterator dataItemProperties = dataItem.listProperties();
					while (dataItemProperties.hasNext()) {
						Statement dataItemStatement = dataItemProperties.next();
						Property dataItemProperty = dataItemStatement.getPredicate();
						RDFNode dataItemRDFNode = dataItemStatement.getObject();
						// System.out.println("dataItemStatement: "+dataItemStatement);

						// NOW FIND OTHER "dataItems" WITH THE SAME PROPERTY AND
						// RDFNode

						if (!dataItemProperty.equals(FEDLCA.sourceTableRowNumber)) {
							ResIterator matchingResourcesIterator = ActiveTDB.tdbModel.listSubjectsWithProperty(
									dataItemProperty, dataItemRDFNode);
							while (matchingResourcesIterator.hasNext()) {
								System.out.println("Found a match for dataItemStatement: " + dataItemStatement);
								Resource matchingResource = matchingResourcesIterator.next();
								if (!matchingResource.equals(dataItem)) {
									MatchCandidate matchCandidate = new MatchCandidate(rowNumber, dataItem,
											matchingResource);
									if (matchCandidate.confirmRDFtypeMatch()) {
										matchCandidates.add(matchCandidate);
									}
								}
							}
						}
					}
				}
			}
		}
		System.out.println("matchCandidates.size()=" + matchCandidates.size());
	}

	public void queryCallback(Integer[] results, String key) {
		// IWorkbenchPage page =
		// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		// FIXME FIXME -- REDO ALL BELOW
		// LabeledQuery labeledQuery = queryFromKey(key);
		// String showResultsInWindow = ResultsView.ID;
		//
		// resultSet = ((HarmonyQuery2Impl) labeledQuery).getResultSet();
		// // ResultSet resultSet = q.getResultSet();
		// // TableProvider tableProvider = TableProvider
		// // .create((ResultSetRewindable) resultSet);
		// setTextAreaContent(((HarmonyQuery2Impl) labeledQuery).getQuery());
		// if (key.startsWith("Harmonize CAS")) { // HACK!!
		// ResultsTreeEditor resultsTreeEditor = (ResultsTreeEditor)
		// Util.findView(ResultsTreeEditor.ID);
		// // FIXME , BECAUSE WHICH ResultsSet CAN / SHOULD
		// // USE
		// // WHICH createTransform
		// // AND WHICH formatForTransfor()
		// // SHOULD BE KNOWN BY THE LabledQuery
		// // BUT CHOSEN BY THE CALLER
		// showResultsInWindow = ResultsTreeEditor.ID;
		//
		// TableProvider tableProvider =
		// TableProvider.createTransform0((ResultSetRewindable)
		// resultSet);
		// // resultsView.update(tableProvider);
		// try {
		// resultsTreeEditor.update(tableProvider);
		// } catch (Exception e) {
		// System.out.println("resultsTreeEditor=" + resultsTreeEditor);
		// e.printStackTrace();
		// }
		//
		// // resultsView.formatForTransform0();
		// } else if (key.startsWith("Harmonize Compart")) { // HACK!!
		// HarmonizeCompartments harmonizeCompartments = (HarmonizeCompartments)
		// Util.findView(HarmonizeCompartments.ID);
		// // FIXME , BECAUSE WHICH ResultsSet CAN / SHOULD
		// // USE
		// // WHICH createTransform
		// // AND WHICH formatForTransfor()
		// // SHOULD BE KNOWN BY THE LabledQuery
		// // BUT CHOSEN BY THE CALLER
		// showResultsInWindow = HarmonizeCompartments.ID;
		//
		// TableProvider tableProvider =
		// TableProvider.create((ResultSetRewindable) resultSet);
		// // resultsView.update(tableProvider);
		// try {
		// harmonizeCompartments.update(tableProvider);
		// } catch (Exception e) {
		// System.out.println("resultsTreeEditor=" + harmonizeCompartments);
		// e.printStackTrace();
		// }
		//
		// // resultsView.formatForTransform0();
		// } else {
		// ResultsView resultsView = (ResultsView)
		// Util.findView(ResultsView.ID);
		// TableProvider tableProvider =
		// TableProvider.create((ResultSetRewindable) resultSet);
		// resultsView.update(tableProvider);
		// }

	}
}
