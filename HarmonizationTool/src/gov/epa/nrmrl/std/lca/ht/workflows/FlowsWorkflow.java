package gov.epa.nrmrl.std.lca.ht.workflows;

import java.util.List;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.Flowable;
//import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataType;
import gov.epa.nrmrl.std.lca.ht.dataModels.QACheck;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.DataSetProvider;
import harmonizationtool.model.FileMD;
import harmonizationtool.model.TableKeeper;
import harmonizationtool.model.TableProvider;
import harmonizationtool.utils.Util;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Text;

//import org.eclipse.swt.widgets.Canvas;

public class FlowsWorkflow extends ViewPart {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow";

//	private List<LCADataType> lcaDataTypes;

//	private Flowable flowable = null;
	private static Text textFileInfo;
	private static Text textIssues;
	private static Text textCommitToDB;
	private static Text textAutoMatched;
	private static Text textSemiAutoMatched;
	private static Text textManualMatched;

	// private static Button btnDataset;
//	private static Button btnAssignColumns;

	private Label label_01;
	private Label label_02;
	private Label label_03;
	private Label label_04;
	private Label label_05;
	private Label label_06;
	private Label label_07;

	private static Button btnCheckData;
	private static Button btnCSV2TDB;
	private static Button btnAutoMatch;

	private static FileMD fileMD;
	private static DataSetProvider dataSetProvider;
	private static TableProvider tableProvider;

	public FlowsWorkflow() {
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}

	@Override
	public void createPartControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		new Label(composite, SWT.NONE);

		Label lblActions = new Label(composite, SWT.NONE);
		lblActions.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblActions.setText("Actions");

		Label lblStatus = new Label(composite, SWT.NONE);
		lblStatus.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblStatus.setText("Status");

		label_01 = new Label(composite, SWT.NONE);
		label_01.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_01.setText("1");
		// lblNewLabel.setSize(100, 30);

		Button btnLoadCSV = new Button(composite, SWT.NONE);
		btnLoadCSV.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnLoadCSV.setText("Load CSV Data");

		btnLoadCSV.addSelectionListener(new SelectionListener() {

			// @Override
			public void widgetSelected(SelectionEvent e) {

				IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
				try {
					handlerService.executeCommand("harmonizationtool.handler.ImportCSV", null);
				} catch (Exception ex) {
					throw new RuntimeException("command with id \"harmonizationtool.handler.ImportCSV\" not found");
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		textFileInfo = new Text(composite, SWT.BORDER);
		textFileInfo.setEditable(false);

		// textFileInfo.setText("(filename)");
		GridData gd_textFileInfo = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textFileInfo.widthHint = 150;
		textFileInfo.setLayoutData(gd_textFileInfo);

		label_02 = new Label(composite, SWT.NONE);
		label_02.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_02.setText("2");

		btnCheckData = new Button(composite, SWT.NONE);
		btnCheckData.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnCheckData.setText("Check Data");
		btnCheckData.setEnabled(false);

		btnCheckData.addSelectionListener(new SelectionListener() {

			// @Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("clicked...");
				 CSVTableView.checkColumns();
				// FIXME
//				btnCSV2TDB.setEnabled(true);
				 btnAutoMatch.setEnabled(true);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		textIssues = new Text(composite, SWT.BORDER);
		// textIssues.setText("0 issues");
		GridData gd_textIssues = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textIssues.widthHint = 150;
		textIssues.setLayoutData(gd_textIssues);

		label_03 = new Label(composite, SWT.NONE);
		label_03.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_03.setText("3");

		btnCSV2TDB = new Button(composite, SWT.NONE);
		btnCSV2TDB.setEnabled(false);
		btnCSV2TDB.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnCSV2TDB.setText("Commit to DB");

		textCommitToDB = new Text(composite, SWT.BORDER);
		textCommitToDB.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		label_04 = new Label(composite, SWT.NONE);
		label_04.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_04.setText("4");

		btnAutoMatch = new Button(composite, SWT.NONE);
		btnAutoMatch.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnAutoMatch.setText("Auto match");
		btnAutoMatch.setEnabled(false);
		btnAutoMatch.addSelectionListener(new SelectionListener() {

			// @Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("clicked...");
				 CSVTableView.matchFlowables();
				// FIXME
//				btnCSV2TDB.setEnabled(true);
//				 textAutoMatched.setEnabled(true);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});


		textAutoMatched = new Text(composite, SWT.BORDER);
		// textAutoMatched.setText("(430 of 600 rows match)");
		GridData gd_textAutoMatched = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textAutoMatched.widthHint = 150;
		textAutoMatched.setLayoutData(gd_textAutoMatched);

		label_05 = new Label(composite, SWT.NONE);
		label_05.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_05.setText("5");

		Button btnSemiautoMatch = new Button(composite, SWT.NONE);
		btnSemiautoMatch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnSemiautoMatch.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnSemiautoMatch.setText("Semi-auto match");
//		btnSemiautoMatch.setEnabled(false);

		textSemiAutoMatched = new Text(composite, SWT.BORDER);
		// textSemiAutoMatched.setText("(100 of 170 rows confirmed)");
		GridData gd_textSemiAutoMatched = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textSemiAutoMatched.widthHint = 150;
		textSemiAutoMatched.setLayoutData(gd_textSemiAutoMatched);

		label_06 = new Label(composite, SWT.NONE);
		label_06.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_06.setText("6");

		Button btnManualMatch = new Button(composite, SWT.NONE);
		btnManualMatch.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnManualMatch.setText("Manual match");
		btnManualMatch.setEnabled(false);

		textManualMatched = new Text(composite, SWT.BORDER);
		// textManualMatched.setText("(0 of 30");
		GridData gd_textManualMatched = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textManualMatched.widthHint = 150;
		textManualMatched.setLayoutData(gd_textManualMatched);
		// TODO Auto-generated method stub

		label_07 = new Label(composite, SWT.NONE);
		label_07.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_07.setText("7");

		Button btnConcludeFile = new Button(composite, SWT.NONE);
		btnConcludeFile.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnConcludeFile.setText("Cancel CSV");
		btnConcludeFile.setEnabled(true);
		new Label(composite, SWT.NONE);

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

	// public static String getTextAssociatedDataset() {
	// return textAssociatedDataset.getText();
	// }
	//
	// public static void setTextAssociatedDataset(String associatedDataset) {
	// textAssociatedDataset.setText(associatedDataset);
	// }

//	public static String getTextColumnsAssigned() {
//		return textColumnsAssigned.getText();
//	}
//
//	public static void setTextColumnsAssigned(String columnsAssigned) {
//		textColumnsAssigned.setText(columnsAssigned);
//	}

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

	public static String getTextSemiAutoMatched() {
		return textSemiAutoMatched.getText();
	}

	public static void setTextSemiAutoMatched(String semiAutoMatched) {
		textSemiAutoMatched.setText(semiAutoMatched);
	}

	public static String getTextManualMatched() {
		return textManualMatched.getText();
	}

	public static void setTextManualMatched(String manualMatched) {
		textManualMatched.setText(manualMatched);
	}

	public static void setFileMD(FileMD newFileMD) {
		fileMD = newFileMD;
		setTextFileInfo("Filename: " + fileMD.getFilename());
		setTextMetaFileInfo(fileMD.getPath());
	}

	public static void setDataSet(String dataSet) {
		// setTextAssociatedDataset(dataSet);
		// btnDataset.setSelection(true);
	}

	public static void setDataSetProvider(DataSetProvider dsProvider) {
		dataSetProvider = dsProvider;
		setDataSet(dataSetProvider.getDataSetMD().getName());
		if (getTextFileInfo().length() < 1) {
			setFileMD(dataSetProvider.getFileMDList().get(0));
			// HACK: CHOOSE FIRST FILE
		} else {
			setFileMD(dataSetProvider.getFileMDList().get(dataSetProvider.getFileMDList().size() - 1));
		}
		setHeaderInfo();
	}

	private static void setHeaderInfo() {
		CSVTableView csvTableView;
		if (Util.findView(CSVTableView.ID) == null){
			try {
				Util.showView(CSVTableView.ID);
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		csvTableView = (CSVTableView) Util.findView(CSVTableView.ID);
//		csvTableView.appendHeaderMenuDiv();
		csvTableView.appendToHeaderMenu(Flowable.getHeaderMenuObjects());
		csvTableView.appendHeaderMenuDiv();
		csvTableView.appendToHeaderMenu(new CSVTableView.CSVColumnInfo("Context (primary)", true, true, QACheck.getGeneralQAChecks()));
		csvTableView.appendToHeaderMenu(new CSVTableView.CSVColumnInfo("Context (additional)", false, false, QACheck.getGeneralQAChecks()));

	}

//	public static void setAssignedColumnCount(int count, int total) {
////		System.out.println("Made it!");
//		if (count > 0) {
//			btnCheckData.setEnabled(true);
//		} else {
//			btnCheckData.setEnabled(false);
//		}
//		setTextColumnsAssigned(count + " of " + total);
//	}

	// public CSVColCheck checkDataColContents(String colName, Pattern pattern,
	// String message){
	// CSVColCheck results = null;
	// if (tableProvider == null){
	// tableProvider = TableKeeper
	// .getTableProvider(fileMD.getPath());
	// }
	// int index = tableProvider.getHeaderNamesAsStrings().indexOf(colName);
	// for (int i=0;i<tableProvider.getData().size();i++){
	// int iPlusOne = i+1;
	// DataRow row = tableProvider.getData().get(i);
	// // for (DataRow row: tableProvider.getData()){
	// String val = row.get(index);
	// System.out.println("value: "+val);
	// Matcher matcher = pattern.matcher(val);
	// int count = matcher.groupCount();
	// // for (hit: matcher.group)
	// // if(val.substring(0,1).equals(" ")){
	// // // LEADING SPACE
	// // System.out.println("Leading space on line: "+iPlusOne);
	// // }
	// }
	// return results;
	// }

	// public String checkFlowableCASCol(){
	// CSVColCheck results = null;
	// if (tableProvider == null){
	// tableProvider = TableKeeper
	// .getTableProvider(fileMD.getPath());
	// }
	// int index =
	// tableProvider.getHeaderNamesAsStrings().indexOf(ViewData.NAME_HDR);
	// }
	public String checkOneColumn(int colIndex){
		String result = "";
		
		return result;
	}

	public String checkFlowableNameCol() {
		String results = null;
		if (tableProvider == null) {
			tableProvider = TableKeeper.getTableProvider(fileMD.getPath());
		}
//		int index = tableProvider.getHeaderNamesAsStrings().indexOf(CSVTableView.NAME_HDR);
		int index = tableProvider.getHeaderNamesAsStrings().indexOf("Flowable Name");
		for (int i = 0; i < tableProvider.getData().size(); i++) {
			int iPlusOne = i + 1;
			DataRow row = tableProvider.getData().get(i);
			// for (DataRow row: tableProvider.getData()){
			String val = row.get(index);
			System.out.println("value: " + val);

			if (val.substring(0, 1).equals(" ")) {
				// LEADING SPACE
				System.out.println("Leading space on line: " + iPlusOne);
			}
		}
		return results;
	}

	// public List<LCADataFamily> getLCADataParents() {
	// List<LCADataType> lcaDataTypes = new ArrayList<LCADataType>();
	// lcaDataTypes.add(new CAS("", "", false, false, false, null));
	// return lcaDataTypes;
	// }

//	public void setLcaDataTypes(List<LCADataType> lcaDataTypes) {
//		this.lcaDataTypes = lcaDataTypes;
//	}

}
