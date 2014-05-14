package gov.epa.nrmrl.std.lca.ht.workflows;

import harmonizationtool.model.FileMD;
import org.eclipse.swt.widgets.Composite;
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

	private static Text textFileInfo;
	private static Text textAssociatedDataset;
	private static Text textColumnsAssigned;
	private static Text textIssues;
	private static Text textAutoMatched;
	private static Text textSemiAutoMatched;
	private static Text textManualMatched;
	
	private static Button btnDataset;
	private static Button btnAssignColumns;
	public FlowsWorkflow() {
	}

	@Override
	public void createPartControl(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		
		Label lblActions = new Label(composite, SWT.NONE);
		lblActions.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblActions.setText("Actions");
		
		Label lblStatus = new Label(composite, SWT.NONE);
		lblStatus.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblStatus.setText("Status");
		
		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("% Complete");
//		lblNewLabel.setSize(100, 30);
		
		Button btnLoadCSV = new Button(composite, SWT.NONE);
		btnLoadCSV.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnLoadCSV.setText("Load CSV Data");

		btnLoadCSV.addSelectionListener(new SelectionListener() {

			// @Override
			public void widgetSelected(SelectionEvent e) {
				
		        IHandlerService handlerService = (IHandlerService) getSite()
		                .getService(IHandlerService.class);
		        try {
		            handlerService.executeCommand("HarmonizationTool.ImportCSV", null);
		        } catch (Exception ex) {
		            throw new RuntimeException("command with id \"HarmonizationTool.ImportCSV\" not found");
		        }
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});	
		
		textFileInfo = new Text(composite, SWT.BORDER);
//		textFileInfo.setText("(filename)");
		GridData gd_textFileInfo = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textFileInfo.widthHint = 150;
		textFileInfo.setLayoutData(gd_textFileInfo);
		new Label(composite, SWT.NONE);
		
		btnDataset = new Button(composite, SWT.CHECK);
		btnDataset.setText("Dataset");
		btnDataset.setEnabled(false);
		
		textAssociatedDataset = new Text(composite, SWT.BORDER);
		textAssociatedDataset.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite, SWT.NONE);
		
		btnAssignColumns = new Button(composite, SWT.CHECK);
		btnAssignColumns.setText("Assign Columns");
		btnAssignColumns.setEnabled(false);
		
		textColumnsAssigned = new Text(composite, SWT.BORDER);
//		textColumnsAssigned.setText("(0 of 5)");
		GridData gd_textColumnsAssigned = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textColumnsAssigned.widthHint = 150;
		textColumnsAssigned.setLayoutData(gd_textColumnsAssigned);
		new Label(composite, SWT.NONE);
		
		Button btnCheckData = new Button(composite, SWT.NONE);
		btnCheckData.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnCheckData.setText("Check Data");
		
		textIssues = new Text(composite, SWT.BORDER);
//		textIssues.setText("0 issues");
		GridData gd_textIssues = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textIssues.widthHint = 150;
		textIssues.setLayoutData(gd_textIssues);
		new Label(composite, SWT.NONE);
		
		Button btnAutoMatch = new Button(composite, SWT.NONE);
		btnAutoMatch.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnAutoMatch.setText("Auto match");
		
		textAutoMatched = new Text(composite, SWT.BORDER);
//		textAutoMatched.setText("(430 of 600 rows match)");
		GridData gd_textAutoMatched = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textAutoMatched.widthHint = 150;
		textAutoMatched.setLayoutData(gd_textAutoMatched);
		new Label(composite, SWT.NONE);
		
		Button btnSemiautoMatch = new Button(composite, SWT.NONE);
		btnSemiautoMatch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnSemiautoMatch.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnSemiautoMatch.setText("Semi-auto match");
		
		textSemiAutoMatched = new Text(composite, SWT.BORDER);
//		textSemiAutoMatched.setText("(100 of 170 rows confirmed)");
		GridData gd_textSemiAutoMatched = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textSemiAutoMatched.widthHint = 150;
		textSemiAutoMatched.setLayoutData(gd_textSemiAutoMatched);
		new Label(composite, SWT.NONE);
		
		Button btnManualMatch = new Button(composite, SWT.NONE);
		btnManualMatch.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnManualMatch.setText("Manual match");
		
		textManualMatched = new Text(composite, SWT.BORDER);
//		textManualMatched.setText("(0 of 30");
		GridData gd_textManualMatched = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textManualMatched.widthHint = 150;
		textManualMatched.setLayoutData(gd_textManualMatched);
		new Label(composite, SWT.NONE);
		// TODO Auto-generated method stub
		
	}


	public String getTextMetaFileInfo() {
		return textFileInfo.getToolTipText();
	}

	public static void setTextMetaFileInfo(String metaFileInfo) {
		textFileInfo.setToolTipText(metaFileInfo);
	}
	
	public String getTextFileInfo() {
		return textFileInfo.getText();
	}

	public static void setTextFileInfo(String fileInfo) {
		textFileInfo.setText(fileInfo);
	}

	public String getTextAssociatedDataset() {
		return textAssociatedDataset.getText();
	}

	public static void setTextAssociatedDataset(String associatedDataset) {
		textAssociatedDataset.setText(associatedDataset);
	}

	public String getTextColumnsAssigned() {
		return textColumnsAssigned.getText();
	}

	public static void setTextColumnsAssigned(String columnsAssigned) {
		textColumnsAssigned.setText(columnsAssigned);
	}

	public String getTextIssues() {
		return textIssues.getText();
	}

	public static void setTextIssues(String issues) {
		textIssues.setText(issues);
	}

	public String getTextAutoMatched() {
		return textAutoMatched.getText();
	}

	public static void setTextAutoMatched(String autoMatched) {
		textAutoMatched.setText(autoMatched);
	}

	public String getTextSemiAutoMatched() {
		return textSemiAutoMatched.getText();
	}

	public static void setTextSemiAutoMatched(String semiAutoMatched) {
		textSemiAutoMatched.setText(semiAutoMatched);
	}

	public String getTextManualMatched() {
		return textManualMatched.getText();
	}

	public static void setTextManualMatched(String manualMatched) {
		textManualMatched.setText(manualMatched);
	}

	public static void setFileMD(FileMD fileMD) {
		setTextFileInfo(fileMD.getFilename());
		setTextMetaFileInfo(fileMD.getPath());
	}
	
	public static void setDataSet(String dataSet){
		setTextAssociatedDataset(dataSet);
		btnDataset.setSelection(true);
	}
	
	public static void setAssignedColumnCount(int count, int total){
		if (count > 0){
			btnAssignColumns.setSelection(true);
		}
		else {
			btnAssignColumns.setSelection(false);
		}
		setTextColumnsAssigned(count+" of "+total);
	}
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}

}
