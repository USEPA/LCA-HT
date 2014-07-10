package gov.epa.nrmrl.std.lca.ht.workflows;

import java.util.ArrayList;
import java.util.List;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVColumnInfo;
import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.Flow;
import gov.epa.nrmrl.std.lca.ht.dataModels.FlowContext;
import gov.epa.nrmrl.std.lca.ht.dataModels.Flowable;
import gov.epa.nrmrl.std.lca.ht.dataModels.MatchCandidate;
import gov.epa.nrmrl.std.lca.ht.job.AutoMatchJob;
import gov.epa.nrmrl.std.lca.ht.job.AutoMatchJobChangeListener;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.DataSourceProvider;
import harmonizationtool.model.TableKeeper;
import harmonizationtool.utils.Util;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FASC;
import harmonizationtool.vocabulary.FEDLCA;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.XSD;

//import org.eclipse.swt.widgets.Canvas;

public class FlowsWorkflow extends ViewPart {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow";

	// private List<LCADataType> lcaDataTypes;

	// private Flowable flowable = null;
	private static Text textFileInfo;
	private static Text textIssues;
	private static Text textCommitToDB;
	private static Text textAutoMatched;
	// private static Text textSemiAutoMatched;
	private static Text textManualMatched;

	private Label label_01;
	private Label label_02;
	private Label label_03;
	private Label label_04;
	private Label label_05;
	private Label label_06;
	private Label label_07;

	private static Button btnCheckData;
	private static Button btnAutoMatch;
	private static Button btnCSV2TDB;
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

		Button btnLoadCSV = new Button(composite, SWT.NONE);
		GridData gd_btnLoadCSV = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnLoadCSV.widthHint = 100;
		btnLoadCSV.setLayoutData(gd_btnLoadCSV);
		btnLoadCSV.setText("Load CSV Data");

		btnLoadCSV.addSelectionListener(loadCSVListener);

		textFileInfo = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		textFileInfo.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));

		GridData gd_textFileInfo = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textFileInfo.widthHint = 150;
		textFileInfo.setLayoutData(gd_textFileInfo);

		// ======== ROW 2 =======================

		label_02 = new Label(composite, SWT.NONE);
		GridData gd_label_02 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_label_02.widthHint = 20;
		label_02.setLayoutData(gd_label_02);
		label_02.setText("2");

		btnCheckData = new Button(composite, SWT.NONE);
		GridData gd_btnCheckData = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnCheckData.widthHint = 100;
		btnCheckData.setLayoutData(gd_btnCheckData);
		btnCheckData.setText("Check Data");
		btnCheckData.setEnabled(false);
		btnCheckData.addSelectionListener(checkDataListener);

		textIssues = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		textIssues.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		// textIssues.setText("0 issues");
		GridData gd_textIssues = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textIssues.widthHint = 150;
		textIssues.setLayoutData(gd_textIssues);

		// ======== ROW 3 =======================

		label_03 = new Label(composite, SWT.NONE);
		GridData gd_label_03 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_label_03.widthHint = 20;
		label_03.setLayoutData(gd_label_03);
		label_03.setText("3");

		btnCSV2TDB = new Button(composite, SWT.NONE);
		btnCSV2TDB.setEnabled(false);
		GridData gd_btnCSV2TDB = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnCSV2TDB.widthHint = 100;
		btnCSV2TDB.setLayoutData(gd_btnCSV2TDB);
		btnCSV2TDB.setText("Commit to DB");
		btnCSV2TDB.addSelectionListener(csv2TDBListener);

		textCommitToDB = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		textCommitToDB.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		textCommitToDB.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		// ======== ROW 4 =======================

		label_04 = new Label(composite, SWT.NONE);
		GridData gd_label_04 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_label_04.widthHint = 20;
		label_04.setLayoutData(gd_label_04);
		label_04.setText("4");

		btnAutoMatch = new Button(composite, SWT.NONE);
		GridData gd_btnAutoMatch = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnAutoMatch.widthHint = 100;
		btnAutoMatch.setLayoutData(gd_btnAutoMatch);
		btnAutoMatch.setText("Auto match");
		btnAutoMatch.setEnabled(false);
		btnAutoMatch.addSelectionListener(autoMatchListener);

		textAutoMatched = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		textAutoMatched.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		// textAutoMatched.setText("(430 of 600 rows match)");
		GridData gd_textAutoMatched = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textAutoMatched.widthHint = 150;
		textAutoMatched.setLayoutData(gd_textAutoMatched);

		// ======== ROW 5 =======================

		// label_05 = new Label(composite, SWT.NONE);
		// GridData gd_label_05 = new GridData(SWT.RIGHT, SWT.CENTER, false,
		// false, 1, 1);
		// gd_label_05.widthHint = 20;
		// label_05.setLayoutData(gd_label_05);
		// label_05.setText("5");
		//
		// Button btnSemiautoMatch = new Button(composite, SWT.NONE);
		// btnSemiautoMatch.addSelectionListener(new SelectionAdapter() {
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// }
		// });
		// GridData gd_btnSemiautoMatch = new GridData(SWT.CENTER, SWT.CENTER,
		// false, false, 1, 1);
		// gd_btnSemiautoMatch.widthHint = 100;
		// btnSemiautoMatch.setLayoutData(gd_btnSemiautoMatch);
		// btnSemiautoMatch.setText("Semi-auto match");
		// // btnSemiautoMatch.setEnabled(false);
		//
		// textSemiAutoMatched = new Text(composite, SWT.BORDER |
		// SWT.READ_ONLY);
		// textSemiAutoMatched.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		// // textSemiAutoMatched.setText("(100 of 170 rows confirmed)");
		// GridData gd_textSemiAutoMatched = new GridData(SWT.FILL, SWT.CENTER,
		// true, false, 1, 1);
		// gd_textSemiAutoMatched.widthHint = 150;
		// textSemiAutoMatched.setLayoutData(gd_textSemiAutoMatched);

		label_05 = new Label(composite, SWT.NONE);
		GridData gd_label_05 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_label_05.widthHint = 20;
		label_05.setLayoutData(gd_label_05);
		label_05.setText("5");

		Button btnManualMatch = new Button(composite, SWT.NONE);
		GridData gd_btnManualMatch = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnManualMatch.widthHint = 100;
		btnManualMatch.setLayoutData(gd_btnManualMatch);
		btnManualMatch.setText("Manual match");
		btnManualMatch.setEnabled(false);

		textManualMatched = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		textManualMatched.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		// textManualMatched.setText("(0 of 30");
		GridData gd_textManualMatched = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textManualMatched.widthHint = 150;
		textManualMatched.setLayoutData(gd_textManualMatched);
		// TODO Auto-generated method stub

		label_06 = new Label(composite, SWT.NONE);
		GridData gd_label_06 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_label_06.widthHint = 20;
		label_06.setLayoutData(gd_label_06);
		label_06.setText("6");

		btnConcludeFile = new Button(composite, SWT.NONE);
		GridData gd_btnConcludeFile = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnConcludeFile.widthHint = 100;
		btnConcludeFile.setLayoutData(gd_btnConcludeFile);
		btnConcludeFile.setText("Cancel CSV");
		btnConcludeFile.setEnabled(true);
		new Label(composite, SWT.NONE);
		btnConcludeFile.addSelectionListener(concludeFileListener);

		// textManualMatched = new Text(composite, SWT.BORDER);
		// // textManualMatched.setText("(0 of 30");
		// GridData gd_textManualMatched = new GridData(SWT.FILL, SWT.CENTER,
		// true, false, 1, 1);
		// gd_textManualMatched.widthHint = 150;
		// textManualMatched.setLayoutData(gd_textManualMatched);
		// TODO Auto-generated method stub
	}

	// public FlowableHeaderObj[] getHeaderObjects(){
	//
	// }

	public static String getTextMetaFileInfo() {
		return textFileInfo.getToolTipText();
	}

	public static void setTextMetaFileInfo(String metaFileInfo) {
		textFileInfo.setToolTipText(metaFileInfo);
	}

	public static String getTextFileInfo() {
		return textFileInfo.getText();
	}

	public static void setTextFileInfo(String fileInfo) {
		textFileInfo.setText(fileInfo);
	}

	// public static String getTextAssociatedDataSource() {
	// return textAssociatedDataSource.getText();
	// }
	//
	// public static void setTextAssociatedDataSource(String associatedDataSource) {
	// textAssociatedDataSource.setText(associatedDataSource);
	// }

	// public static String getTextColumnsAssigned() {
	// return textColumnsAssigned.getText();
	// }
	//
	// public static void setTextColumnsAssigned(String columnsAssigned) {
	// textColumnsAssigned.setText(columnsAssigned);
	// }

	public static String getTextIssues() {
		return textIssues.getText();
	}

	public static void setTextIssues(String issues) {
		textIssues.setText(issues);
	}

	public static String getTextAutoMatched() {
		return textAutoMatched.getText();
	}

	public static void setTextAutoMatched(String autoMatched) {
		textAutoMatched.setText(autoMatched);
	}

	// public static String getTextSemiAutoMatched() {
	// return textSemiAutoMatched.getText();
	// }
	//
	// public static void setTextSemiAutoMatched(String semiAutoMatched) {
	// textSemiAutoMatched.setText(semiAutoMatched);
	// }

	public static String getTextManualMatched() {
		return textManualMatched.getText();
	}

	public static void setTextManualMatched(String manualMatched) {
		textManualMatched.setText(manualMatched);
	}

	// public static void setFileMD(FileMD newFileMD) {
	// fileMD = newFileMD;
	// setTextFileInfo("Filename: " + fileMD.getFilename());
	// setTextMetaFileInfo(fileMD.getPath());
	// }

	// public static void setDataSourceProvider(DataSourceProvider dsProvider) {
	// dataSourceProvider = dsProvider;
	// // setDataSource(dataSourceProvider.getDataSourceMD().getName());
	// if (getTextFileInfo().length() < 1) {
	// setFileMD(dataSourceProvider.getFileMDList().get(0));
	// // HACK: CHOOSE FIRST FILE
	// } else {
	// setFileMD(dataSourceProvider.getFileMDList().get(dataSourceProvider.getFileMDList().size() -
	// 1));
	// }
	// setHeaderInfo();
	// }

	private static void setHeaderInfo() {
		CSVTableView csvTableView;
		if (Util.findView(CSVTableView.ID) == null) {
			try {
				Util.showView(CSVTableView.ID);
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		csvTableView = (CSVTableView) Util.findView(CSVTableView.ID);
		// csvTableView.appendHeaderMenuDiv();
		csvTableView.appendToAvailableCSVColumnInfo("Assign Flowable Fields", Flowable.getHeaderMenuObjects());
		csvTableView.appendToAvailableCSVColumnInfo("Assign Flow Context Fields", FlowContext.getHeaderMenuObjects());
	}

	SelectionListener loadCSVListener = new SelectionListener() {

		@Override
		public void widgetSelected(SelectionEvent e) {
			IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
			try {
				handlerService.executeCommand("harmonizationtool.handler.ImportCSV", null);
			} catch (Exception ex) {
				ex.printStackTrace();
				// throw new
				// RuntimeException("command with id \"harmonizationtool.handler.ImportCSV\" not found");
			}
			textFileInfo.setText(TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getFileMD().getFilename());
			textFileInfo.setToolTipText(TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getFileMD().getPath());

			// FIXME - GET THE RIGHT FILE NAME, NOT JUST THE FIRST
			setHeaderInfo();
			btnCheckData.setEnabled(true);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
			try {
				handlerService.executeCommand("harmonizationtool.handler.ImportCSV", null);
			} catch (Exception ex) {
				throw new RuntimeException("command with id \"harmonizationtool.handler.ImportCSV\" not found");
			}
			setHeaderInfo();
			btnCheckData.setEnabled(true);
		}
	};

	SelectionListener checkDataListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			textIssues.setText(" ... checking data ...");
			int colsChecked = CSVTableView.countAssignedColumns();
			if (colsChecked == 0) {
				textIssues.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
				textIssues.setText("Assign at least one column first)");
				btnAutoMatch.setEnabled(false);
				btnCSV2TDB.setEnabled(false);

			} else {
				int issueCount = CSVTableView.checkCols();
				textIssues.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));

				textIssues.setText(issueCount + " issues. " + colsChecked + " columns checked");
				if (issueCount == 0) {
					// btnAutoMatch.setEnabled(true);
					btnCSV2TDB.setEnabled(true);
				} else {
					btnAutoMatch.setEnabled(false);
					btnCSV2TDB.setEnabled(false);
				}
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			textIssues.setText(" ... checking data ...");
			int issueCount = CSVTableView.checkCols();
			textIssues.setText(issueCount + " issues found");
			btnCSV2TDB.setEnabled(true);
			btnAutoMatch.setEnabled(true);
		}
	};

	SelectionListener csv2TDBListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			int colsChecked = CSVTableView.countAssignedColumns();
			if (colsChecked == 0) {
				textCommitToDB.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
				textCommitToDB.setText("Assign and check columns first)");
				btnAutoMatch.setEnabled(false);
			} else {
				int triples = safeCommitColumns2TDB();
				// triples += safeCommitFlowContexts2TDB();
				textCommitToDB.setText(triples + " triples added");
				if (triples == 0) {
					textCommitToDB.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
				} else {
					textCommitToDB.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
				}
				btnAutoMatch.setEnabled(true);
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			textIssues.setText(" ... checking data ...");
			int issueCount = CSVTableView.checkCols();
			textIssues.setText(issueCount + " issues found");
			btnCSV2TDB.setEnabled(true);
			btnAutoMatch.setEnabled(true);
		}
	};

	SelectionListener autoMatchListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			// CSVTableView.matchFlowables();
			String jobKey = "autoMatch_01";
			// Table table = CSVTableView.getTable();
			AutoMatchJob autoMatchJob = new AutoMatchJob("FlowsWorkflow Job", CSVTableView.getTableProviderKey());
			autoMatchJob.setPriority(Job.SHORT);
			autoMatchJob.setSystem(false);
			autoMatchJob.addJobChangeListener(new AutoMatchJobChangeListener((FlowsWorkflow) Util.findView(FlowsWorkflow.ID), jobKey));
			autoMatchJob.schedule();

			// autoMatch();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// CSVTableView.matchFlowables();
			autoMatch();
		}
	};

	SelectionListener concludeFileListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			// CSVTableView.matchFlowables();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// CSVTableView.matchFlowables();
		}
	};

	private int safeCommitColumns2TDB() {
		// THE SAFE PART MEANS
		// PRIOR TO ADDING TRIPLES, PREVIOUSLY ADDED
		// TRIPLES FROM THIS FILE SHOULD BE REMOVED
		Model model = ActiveTDB.model;

		long triples = model.size();
		Table table = CSVTableView.getTable();

		CSVColumnInfo[] assignedCSVColumns = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getAssignedCSVColumnInfo();
		for (int rowNumber = 0; rowNumber < table.getItemCount(); rowNumber++) {
			int rowNumberPlusOne = rowNumber + 1;
			Literal rowLiteral = ActiveTDB.model.createTypedLiteral(rowNumberPlusOne, XSDDatatype.XSDinteger);

			Item item = table.getItem(rowNumber);
			DataRow dataRow = (DataRow) item.getData();
			Flowable flowable = new Flowable();
			FlowContext flowContext = new FlowContext();
			for (int colNumber = 1; colNumber < assignedCSVColumns.length; colNumber++) {
				CSVColumnInfo csvColumnnInfo = assignedCSVColumns[colNumber];
				if (csvColumnnInfo != null) {
					
					String headerName = csvColumnnInfo.getHeaderString();
					if (headerName.equals("Flowable Name")) {
						if (flowable.getName() != null) {
							Logger.getLogger("run").warn("# Trying to add a second name to this flowable");
						}
						flowable.setName(dataRow.get(colNumber - 1));
					} else if (headerName.equals("Flowable Synonym")) {
						flowable.addSynonym(dataRow.get(colNumber - 1));
					} else if (headerName.equals("CAS")) {
						flowable.setCas(dataRow.get(colNumber - 1));
					} else if (headerName.equals("Chemical formula")) {
						flowable.setFormula(dataRow.get(colNumber - 1));
					} else if (headerName.equals("SMILES")) {
						flowable.setSmiles(dataRow.get(colNumber - 1));
					} else if (headerName.equals("Context (primary)")) {
						flowContext.setPrimaryFlowContext(dataRow.get(colNumber - 1));
					} else if (headerName.equals("Context (additional)")) {
						flowContext.addAdditionalFlowContext(dataRow.get(colNumber - 1));
					}
				}
			}
			Flow flow = new Flow();
			DataSourceProvider dataSourceProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getDataSourceProvider();
			dataSourceProvider.getTdbResource().addProperty(ECO.hasFlow, flow.getTdbResource());
			flow.getTdbResource().addProperty(ECO.hasDataSource, dataSourceProvider.getTdbResource());
			if (flowable.getName() != null) {
				if (!flowable.getName().equals("")) {
					flowable.getTdbResource().addLiteral(FEDLCA.sourceTableRowNumber, rowLiteral);
					flowable.getTdbResource().addProperty(ECO.hasDataSource, dataSourceProvider.getTdbResource());
					flow.getTdbResource().addProperty(ECO.hasFlowable, flowable.getTdbResource());
				}
			}
			if (flowContext.getPrimaryFlowContext() != null) {
				if (!flowContext.getPrimaryFlowContext().equals("")) {
					flowContext.getTdbResource().addLiteral(FEDLCA.sourceTableRowNumber, rowLiteral);
					flowContext.getTdbResource().addProperty(ECO.hasDataSource, dataSourceProvider.getTdbResource());
					flow.getTdbResource().addProperty(FASC.hasCompartment, flowContext.getTdbResource());
				}
			}
		}
		long newTriples = model.size() - triples;
		return (int) newTriples;
	}

	public static Integer[] autoMatch() {
		Integer[] results = new Integer[3];
		List<MatchCandidate> matchCandidates = new ArrayList<MatchCandidate>();
		DataSourceProvider dataSourceProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getDataSourceProvider();

		Model model = ActiveTDB.model;
		Resource dataSourceTDBResource = dataSourceProvider.getTdbResource();
		// ResIterator resourceIterator = model.listResourcesWithProperty(ECO.hasDataSource,
		// dataSourceTDBResource.asNode());
		ResIterator resourceIterator = model.listResourcesWithProperty(ECO.hasDataSource, dataSourceTDBResource);
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

						// NOW FIND OTHER "dataItems" WITH THE SAME PROPERTY AND RDFNode

						if (!dataItemProperty.equals(FEDLCA.sourceTableRowNumber)) {
							ResIterator matchingResourcesIterator = model.listSubjectsWithProperty(dataItemProperty, dataItemRDFNode);
							while (matchingResourcesIterator.hasNext()) {
								System.out.println("Found a match for dataItemStatement: " + dataItemStatement);
								Resource matchingResource = matchingResourcesIterator.next();
								if (!matchingResource.equals(dataItem)) {
									MatchCandidate matchCandidate = new MatchCandidate(rowNumber, dataItem, matchingResource);
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
		results[0] = matchCandidates.size();
		results[1] = 0;
		results[2] = 0;
		return results;
		// while (flowablePropertyStatements.hasNext()) {
		// Statement flowContextStatement = flowablePropertyStatements.next();
		// ResIterator matchIterator =
		// model.listSubjectsWithProperty(flowContextStatement.getPredicate(),
		// flowContextStatement.getObject());
		// while (matchIterator.hasNext()) {
		// Resource matchThing = matchIterator.next();
		// if (!matchThing.equals(flowContextStatement.getResource())) {
		// Statement rowNumberStatement =
		// flowContextTdbResource.getProperty(FEDLCA.sourceTableRowNumber);
		// int row = ActiveTDB.getIntegerFromLiteral(rowNumberStatement.getObject());
		// CSVTableView.getTable().getItem(row - 1).setBackground(0,
		// SWTResourceManager.getColor(SWT.COLOR_GREEN));
		// }
		// }
		// }
		// thing = model.listSubjectsWithProperty(arg0, arg1)
		// }

		// thing = dataSourceTDBResource.getProperty(FASC.hasFl)
	}

	private void autoMatch_01() {
		List<MatchCandidate> matchCandidates = new ArrayList<MatchCandidate>();
		DataSourceProvider dataSourceProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getDataSourceProvider();

		Model model = ActiveTDB.model;
		Resource dataSourceTDBResource = dataSourceProvider.getTdbResource();
		// ResIterator resourceIterator = model.listResourcesWithProperty(ECO.hasDataSource,
		// dataSourceTDBResource.asNode());
		ResIterator resourceIterator = model.listResourcesWithProperty(ECO.hasDataSource, dataSourceTDBResource);
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

						// NOW FIND OTHER "dataItems" WITH THE SAME PROPERTY AND RDFNode

						if (!dataItemProperty.equals(FEDLCA.sourceTableRowNumber)) {
							ResIterator matchingResourcesIterator = model.listSubjectsWithProperty(dataItemProperty, dataItemRDFNode);
							while (matchingResourcesIterator.hasNext()) {
								System.out.println("Found a match for dataItemStatement: " + dataItemStatement);
								Resource matchingResource = matchingResourcesIterator.next();
								if (!matchingResource.equals(dataItem)) {
									MatchCandidate matchCandidate = new MatchCandidate(rowNumber, dataItem, matchingResource);
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
		// while (flowablePropertyStatements.hasNext()) {
		// Statement flowContextStatement = flowablePropertyStatements.next();
		// ResIterator matchIterator =
		// model.listSubjectsWithProperty(flowContextStatement.getPredicate(),
		// flowContextStatement.getObject());
		// while (matchIterator.hasNext()) {
		// Resource matchThing = matchIterator.next();
		// if (!matchThing.equals(flowContextStatement.getResource())) {
		// Statement rowNumberStatement =
		// flowContextTdbResource.getProperty(FEDLCA.sourceTableRowNumber);
		// int row = ActiveTDB.getIntegerFromLiteral(rowNumberStatement.getObject());
		// CSVTableView.getTable().getItem(row - 1).setBackground(0,
		// SWTResourceManager.getColor(SWT.COLOR_GREEN));
		// }
		// }
		// }
		// thing = model.listSubjectsWithProperty(arg0, arg1)
		// }

		// thing = dataSourceTDBResource.getProperty(FASC.hasFl)
	}

	// private int safeCommitFlowContexts2TDB() {
	// // THE SAFE PART MEANS
	// // PRIOR TO ADDING TRIPLES, PREVIOUSLY ADDED
	// // TRIPLES FROM THIS FILE SHOULD BE REMOVED
	// Model model = ActiveTDB.model;
	//
	// long triples = model.size();
	// // CSVColumnInfo[] FlowableCSVColumnInfos =
	// // CSVTableView.getFlowableCSVColumnInfos();
	//
	// long newTriples = model.size() - triples;
	// return (int) newTriples;
	// }

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
		// TableProvider tableProvider = TableProvider.createTransform0((ResultSetRewindable)
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
		// TableProvider tableProvider = TableProvider.create((ResultSetRewindable) resultSet);
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
		// ResultsView resultsView = (ResultsView) Util.findView(ResultsView.ID);
		// TableProvider tableProvider = TableProvider.create((ResultSetRewindable) resultSet);
		// resultsView.update(tableProvider);
		// }

	}
}
