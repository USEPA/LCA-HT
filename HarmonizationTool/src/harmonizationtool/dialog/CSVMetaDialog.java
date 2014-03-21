package harmonizationtool.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import harmonizationtool.comands.SelectTDB;
import harmonizationtool.model.CuratorMD;
import harmonizationtool.model.DataSetKeeper;
import harmonizationtool.model.DataSetMD;
import harmonizationtool.model.DataSetProvider;
import harmonizationtool.model.FileMD;
import harmonizationtool.model.ModelProvider;
import harmonizationtool.utils.Util;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.DCTerms;
import org.eclipse.swt.widgets.Button;
import org.eclipse.wb.swt.SWTResourceManager;

public class CSVMetaDialog extends TitleAreaDialog {

	public ModelProvider modelProvider = null;
//	private boolean newFileMD = false;
	private boolean newDataSet = false;
//	private boolean dataSetEnabled = true;
	private DataSetProvider curDataSetProvider = null;
	private DataSetProvider callingDataSetProvider = null;
	private FileMD curFileMD = null;
	private FileMD callingFileMD = null;
//	private DataSetMD dataSetMD = null;
//	private CuratorMD curatorMD = null;
//	private Resource tdbResource = null;
	private Combo combo = null;
	private Combo fileMDCombo = null;
	private List<Text> dialogValues = new ArrayList<Text>();
	private FileMDComboMgr fileMDComboMgr;
//	private FileMDComboModifyListener FileMDComboModifyListener;
	private FileMDComboModifyListener fileMDComboModifyListener;
	protected String comboTextSaved = "";
	protected boolean comboKeyHeldDown = false;
	private int comboSelectionIndex = -1;

	// THERE ARE THREE WAYS TO CALL THIS:
	// CASE 1) SIMPLY TO VIEW OR EDIT DATA SET INFO
	// CASE 2) WITH A SELECTED FILE AND ITS DATA SET
	// CASE 3) WITH A NEW FILE TO ADD TO AN EXISTING DATA SET
	// CASE 4) WITH A NEW FILE TO ADD TO A NEW DATA SET (CREATED HERE)
	// ? CASE 5) WITH A NEW DATA SET TO ADD 
	
	// YOU CAN GET HERE WITH A NEW FILE (FOR A NEW OR EXISTING DATA SET)
	/**
	 * @wbp.parser.constructor

	 */
	public CSVMetaDialog(Shell parentShell) {
		super(parentShell);
		// CASE 1 - EDIT DATA SET INFO FOR ANY EXISTING DATA SET
//		newFileMD = false;
//		newDataSet = false;
		if (DataSetKeeper.size() == 0) {
			new GenericMessageBox(parentShell, "No Data Sets", "The HT does not contain any DataSets at this time.  Read a CSV or RDF file to create some.");
			return;
		}
		this.curDataSetProvider = DataSetKeeper.get(0);
		this.curFileMD = curDataSetProvider.getFileMDList().get(0);
//		dataSetMD = curDataSetProvider.getDataSetMD();
//		curatorMD = curDataSetProvider.getCuratorMD();
//		tdbResource = curDataSetProvider.getTdbResource();
	}
	
	public CSVMetaDialog(Shell parentShell, FileMD fileMD, DataSetProvider dataSetProvider) {
		// CASE 2 - EDIT DATA SET INFO FOR ONE DATA SET ONLY
		super(parentShell);
//		newFileMD = false;
//		newDataSet = false;
//		dataSetEnabled = true;
		this.callingDataSetProvider = dataSetProvider;
		this.callingFileMD = fileMD;
		this.curDataSetProvider = callingDataSetProvider;
		this.curFileMD = callingFileMD;
//		dataSetMD = dataSetProvider.getDataSetMD();
//		curatorMD = dataSetProvider.getCuratorMD();
//		tdbResource = dataSetProvider.getTdbResource();
//		this.curFileMD = fileMD;
	}

	public CSVMetaDialog(Shell parentShell, FileMD fileMD) {
		super(parentShell);
		// CASE 3 - NEW FILE TO ADD TO EXISTING DATA SET
		// CASE 4 - NEW FILE TO ADD TO NEW DATA SET (CREATED HERE)
//		newFileMD = true;
//		newDataSet = true;
//		dataSetEnabled = true;
//		assert fileMD != null : "fileMD cannot be null";
		this.callingFileMD = fileMD;
		this.curFileMD = callingFileMD;
//		callingDataSetProvider = new DataSetProvider();
//		this.dataSetMD = new DataSetMD();
//		callingDataSetProvider.setDataSetMD(dataSetMD);
//		this.curatorMD = new CuratorMD(true);
//		callingDataSetProvider.setCuratorMD(curatorMD);
//		callingDataSetProvider.addFileMD(fileMD); // THIS MEANS WE DON'T HAVE TO
												// ADD IT AGAIN
//		curDataSetProvider = callingDataSetProvider;
		// curatorFromPrefs();
	}

	// YOU CAN GET HERE WITH A DataSetProvider
	// WITH DataSetMD , CuratorMD , fileMDList , tdbResource
//	public CSVMetaDialog(Shell parentShell, DataSetProvider dataSetProvider) {
//		super(parentShell);
	    // CASE 5 - NEW DATA SET TO ADD WITHOUT NEW FILE INFO (SO WHERE IS IT FROM?)
//		newFileMD = false;
//		newDataSet = false;
//		dataSetEnabled = false;
//		this.dataSetProvider = dataSetProvider;
//		dataSetMD = dataSetProvider.getDataSetMD();
//		curatorMD = dataSetProvider.getCuratorMD();
//		tdbResource = dataSetProvider.getTdbResource();
//		fileMD = dataSetProvider.getFileMDList().get(0);
//	}

	// YOU CAN GET HERE WITH A FileMD AND A DataSetProvider
	// WITH DataSetMD , CuratorMD , fileMDList , tdbResource


	// MAKE THE WHOLE DIALOG BOX
	@Override
	protected Control createDialogArea(Composite parent) {
		System.out.println("createDialogArea called");
		setTitle("CSV file Meta Data");

		// CREATE THE COMPOSITE
		Composite composite = new Composite(parent, SWT.NONE);
		// composite.setBounds(0, 0, 600, 1200);
		composite.setLayout(null);
		int col1Left = 5;
		int col1LeftIndent = 25;
		int col1Width = 100;
		int col2Left = 125;
		int col2Width = 250;
		int rowHeight = 20;
		int disBtwnRows = 30;
		int rowIndex = 0;

		rowIndex = 0;
		Label lbl_5b = new Label(composite, SWT.LEFT);
		lbl_5b.setFont(SWTResourceManager.getFont("Lucida Grande", 16, SWT.BOLD));
		lbl_5b.setBounds(col1Left, rowIndex * disBtwnRows, col1Width + col2Width, rowHeight);
		lbl_5b.setText("Data Set Information:");

		rowIndex++;
		Label lbl_01 = new Label(composite, SWT.RIGHT);
		lbl_01.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		// lblAssociatedDataSet.setBounds(0, 0, 400, 14);
		lbl_01.setText("Name");

		// ADD THE DATA SET CHOOSER PULL DOWN
		if (callingFileMD != null) {
			combo = new Combo(composite, SWT.DROP_DOWN);
			combo.setToolTipText("Choose an existing data set or type a name in the first selection to create a new one");
		} else {
			combo = new Combo(composite, SWT.READ_ONLY);
		}
		combo.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		combo.setItems(getDataSetInfo());
		combo.setText(getDataSetInfo()[0]);

		// COLLECT DATA SET LIST
		String[] dsInfo = getDataSetInfo();


		// comboSelectionIndex = 0;

//		combo.setEnabled(dataSetEnabled);

		// System.out.println("combo.getSelectionIndex()" +
		// combo.getSelectionIndex());

		// combo.addMouseListener(new MouseListener() {
		//
		// private String savedComboText = "";
		//
		// @Override
		// public void mouseDoubleClick(MouseEvent e) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public void mouseDown(MouseEvent e) {
		// // savedComboText = combo.getText();
		//
		// }
		//
		// @Override
		// public void mouseUp(MouseEvent e) {
		// // if (savedComboText.equals(combo.getText())){
		// // return;
		// // }
		// // int selectionIndex = combo.getSelectionIndex();
		// // System.out.println("selectionIndex = " + selectionIndex);
		// // populateMeta(combo.getText());
		// // getButton(IDialogConstants.OK_ID).setEnabled(true);
		// // System.out.println("choice is " + combo.getSelectionIndex()
		// // + " with value: " + combo.getText());
		// }
		// });

		combo.addKeyListener(new KeyListener() {

			private int comboSelectionIndexSaved;

			@Override
			public void keyReleased(KeyEvent e) {
				System.out.println("keyReleased=" + e.toString());
				comboKeyHeldDown = false;
				System.out.println("comboSelectionIndex = " + comboSelectionIndex);

				// if (comboSelectionIndexSaved == 0) {
				if (comboSelectionIndex == 0) {
					// allow typing
//					System.out.println("trying to edit row zero");
//					combo.setText(comboTextSaved + e.character);
//					String[] comboItems = combo.getItems();
//					comboItems[comboSelectionIndex]=dataSetMD.getName();
					combo.setItem(comboSelectionIndex, curDataSetProvider.getDataSetMD().getName());
				} else {
					// no typing
					combo.setText(comboTextSaved);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println("keyPressed=" + e.toString());
				// comboSelectionIndexSaved = comboSelectionIndex;
				if (!comboKeyHeldDown && (comboSelectionIndex != 0)) {
					comboTextSaved = combo.getText();
					comboKeyHeldDown = true;
				}

			}
		});
		combo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("combo.addSelectionListener.widgetSelectedr=" + e.toString());
				comboSelectionIndex = combo.getSelectionIndex();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				System.out.println("combo.addSelectionListener.widgetDefaultSelected=" + e.toString());
				comboSelectionIndex = combo.getSelectionIndex();

			}

		});

		combo.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				// comboSelectionIndex = combo.getSelectionIndex();
				if (comboKeyHeldDown) {
					// do nothing
				} else {
					System.out.println("Modify event: " + e.toString());
					System.out.println("Modify Event: combo.getSelectionIndex() = " + combo.getSelectionIndex() + " with combo.getText() = " + combo.getText());
					populateMeta();
				}

				// getButton(IDialogConstants.OK_ID).setEnabled(true);
			}
		});
		
		combo.select(0);
		comboSelectionIndex = combo.getSelectionIndex();

		rowIndex++;
		Label lbl_07 = new Label(composite, SWT.RIGHT);
		lbl_07.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_07.setText("Version");
		Text text_07 = new Text(composite, SWT.BORDER);
		text_07.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_08 = new Label(composite, SWT.RIGHT);
		lbl_08.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_08.setText("Comments");
		Text text_08 = new Text(composite, SWT.BORDER);
		text_08.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_09 = new Label(composite, SWT.RIGHT);
		lbl_09.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_09.setText("Contact Name");
		Text text_09 = new Text(composite, SWT.BORDER);
		text_09.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_10 = new Label(composite, SWT.RIGHT);
		lbl_10.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_10.setText("Contact Affiliation");
		Text text_10 = new Text(composite, SWT.BORDER);
		text_10.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_11 = new Label(composite, SWT.RIGHT);
		lbl_11.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_11.setText("Contact Email");
		Text text_11 = new Text(composite, SWT.BORDER);
		text_11.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_12 = new Label(composite, SWT.RIGHT);
		lbl_12.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_12.setText("Contact Phone");
		Text text_12 = new Text(composite, SWT.BORDER);
		text_12.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		// NEXT STEP: ADD FileMD DATA
		rowIndex = 8;
		Label sep_01a = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep_01a.setBounds(50, rowIndex * disBtwnRows - 5, 250, 2);

		Label lbl_1b = new Label(composite, SWT.LEFT);
		lbl_1b.setFont(SWTResourceManager.getFont("Lucida Grande", 16, SWT.BOLD));
		lbl_1b.setBounds(col1Left, rowIndex * disBtwnRows, col1Width + col2Width, rowHeight);
		lbl_1b.setText("File Information:");

		rowIndex++;
		Label lbl_02 = new Label(composite, SWT.RIGHT);
		lbl_02.setText("Name");
		lbl_02.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		fileMDCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		fileMDComboMgr = new FileMDComboMgr();
		fileMDCombo.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		// NEXT STEP: COLLECT FILE LIST INFO BASED ON WHAT IS PASSED, AND ADD
		// OTHER
		fileMDCombo.setToolTipText("Files associated with this data set." + dsInfo[0]);
		fileMDComboModifyListener = new FileMDComboModifyListener();
		// fileMDCombo.addModifyListener(new ModifyListener() {
		// public void modifyText(ModifyEvent e) {
		// // redrawDialogRows();
		// System.out.println("fileMDCombo index "
		// + fileMDCombo.getSelectionIndex());
		// populateFileMeta();
		// System.out.println("choice is "
		// + fileMDCombo.getSelectionIndex() + " with value: "
		// + fileMDCombo.getText());
		// }
		// });
		fileMDCombo.addModifyListener(fileMDComboModifyListener);

		// combo2.setItems(getFileInfo());
		// combo2.setText(getFileInfo()[0]);

		// String[] dsInfo = getDataSetInfo();
		// Text text_02 = new Text(composite, SWT.BORDER);
		// text_02.setBounds(col2Left, 1 * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_03 = new Label(composite, SWT.RIGHT);
		lbl_03.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_03.setText("Size (bytes)");
		Text text_03 = new Text(composite, SWT.BORDER);
		text_03.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		text_03.setEnabled(false);

		rowIndex++;
		Label lbl_04 = new Label(composite, SWT.RIGHT);
		lbl_04.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_04.setText("Last Modified");
		Text text_04 = new Text(composite, SWT.BORDER);
		text_04.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		text_04.setEnabled(false);

		rowIndex++;
		Label lbl_05 = new Label(composite, SWT.RIGHT);
		lbl_05.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_05.setText("Read Time");
		Text text_05 = new Text(composite, SWT.BORDER);
		text_05.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		text_05.setEnabled(false);

		// rowIndex++;
		// Label sep_05a = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		// sep_05a.setBounds(50, rowIndex * disBtwnRows - 5, 250, 2);

		// rowIndex++;
		// Label lbl_06 = new Label(composite, SWT.RIGHT);
		// lbl_06.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
		// rowHeight);
		// lbl_06.setText("Name");
		// Text text_06 = new Text(composite, SWT.BORDER);
		// text_06.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
		// rowHeight);

		rowIndex = 13;
		Label sep_12a = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep_12a.setBounds(60, rowIndex * disBtwnRows - 5, 250, 2);

		Label lbl_12b = new Label(composite, SWT.LEFT);
		lbl_12b.setFont(SWTResourceManager.getFont("Lucida Grande", 16, SWT.BOLD));
		lbl_12b.setBounds(5, rowIndex * disBtwnRows, col1Width + col2Width, 20);
		lbl_12b.setText("Curator Information:");

		rowIndex++;
		Button copyUserInfo = new Button(composite, SWT.BORDER);
		copyUserInfo.setToolTipText("Values for the Curator will be copied from user info set in the preferences.");
		copyUserInfo.setBounds(col2Left, rowIndex * disBtwnRows, 250, 25);
		copyUserInfo.setText("Copy Info from Preferences");
		copyUserInfo.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				dialogValues.get(9).setText(Util.getPreferenceStore().getString("userName"));
				dialogValues.get(10).setText(Util.getPreferenceStore().getString("userAffiliation"));
				dialogValues.get(11).setText(Util.getPreferenceStore().getString("userEmail"));
				dialogValues.get(12).setText(Util.getPreferenceStore().getString("userPhone"));
			}
		});

		rowIndex++;
		Label lbl_13 = new Label(composite, SWT.RIGHT);
		lbl_13.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_13.setText("Name");
		Text text_13 = new Text(composite, SWT.BORDER);
		text_13.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_14 = new Label(composite, SWT.RIGHT);
		lbl_14.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, 20);
		lbl_14.setText("Affiliation");
		Text text_14 = new Text(composite, SWT.BORDER);
		text_14.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_15 = new Label(composite, SWT.RIGHT);
		lbl_15.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, 20);
		lbl_15.setText("Email");
		Text text_15 = new Text(composite, SWT.BORDER);
		text_15.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_16 = new Label(composite, SWT.RIGHT);
		lbl_16.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_16.setText("Phone");
		Text text_16 = new Text(composite, SWT.BORDER);
		text_16.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		// dialogValues.add(text_02); // 00 File Name
		dialogValues.add(text_03); // 00 File Size (bytes)
		dialogValues.add(text_04); // 01 File Last Modified
		dialogValues.add(text_05); // 02 File Read Time
		// dialogValues.add(text_06); // 03 Data Set Name
		dialogValues.add(text_07); // 03 Data Set Version
		dialogValues.add(text_08); // 04 Data Set Comments
		dialogValues.add(text_09); // 05 Data Set Contact Name
		dialogValues.add(text_10); // 06 Data Set Contact Affiliation
		dialogValues.add(text_11); // 07 Data Set Contact Email
		dialogValues.add(text_12); // 08 Data Set Contact Phone
		dialogValues.add(text_13); // 9 Curator Name
		dialogValues.add(text_14); // 10 Curator Affiliation
		dialogValues.add(text_15); // 11 Curator Email
		dialogValues.add(text_16); // 12 Curator Phone

		redrawDialogRows();

		Control control = super.createDialogArea(parent);
		control.setToolTipText("");
		return control;
	}

	@Override
	public int open() {
		if (callingFileMD != null) {
			// getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
		return super.open();
	}

	@Override
	protected void cancelPressed() {

		super.cancelPressed();
	}

	@Override
	protected void okPressed() {
		DataSetMD dataSetMD = curDataSetProvider.getDataSetMD();
		CuratorMD curatorMD = curDataSetProvider.getCuratorMD();
		// dataSetMD.setName(dialogValues.get(3).getText()); //FIXME
		dataSetMD.setName(combo.getText()); // FIXME
		dataSetMD.setVersion(dialogValues.get(3).getText());
		dataSetMD.setComments(dialogValues.get(4).getText());
		dataSetMD.setContactName(dialogValues.get(5).getText());
		dataSetMD.setContactAffiliation(dialogValues.get(6).getText());
		dataSetMD.setContactEmail(dialogValues.get(7).getText());
		dataSetMD.setContactPhone(dialogValues.get(8).getText());

		// curatorMD META DATA
		curatorMD.setName(dialogValues.get(9).getText());
		curatorMD.setAffiliation(dialogValues.get(10).getText());
		curatorMD.setEmail(dialogValues.get(11).getText());
		curatorMD.setPhone(dialogValues.get(12).getText());

//		curDataSetProvider.setDataSetMD(dataSetMD);
//		curDataSetProvider.setCuratorMD(curatorMD);
		// Model model = SelectTDB.model;
//		System.out.println("newDataSet = " + newDataSet);
//		System.out.println("comboSelectionIndex = " + comboSelectionIndex);

		if (newDataSet) {
			boolean test = DataSetKeeper.add(curDataSetProvider); // A DataSetProvider IS BORN!!
			System.out.println("test = " + test);

			dataSetProviderToTDB(curDataSetProvider);
			// int dataSetIdPlusOne = DataSetKeeper.indexOf(dataSetProvider) +
			// 1;
			// Resource newTDBResource = dataSetProvider.getTdbResource();
			// model.addLiteral(newTDBResource, ETHOLD.localSerialNumber,
			// model.createTypedLiteral(dataSetIdPlusOne));
			// model.addLiteral(newTDBResource, RDFS.label,
			// model.createLiteral(dataSetMD.getName()));
			// model.addLiteral(newTDBResource, RDFS.comment,
			// model.createLiteral(dataSetMD.getComments()));
			// model.addLiteral(newTDBResource, DCTerms.hasVersion,
			// model.createLiteral(dataSetMD.getVersion()));
		} else if (callingFileMD != null) {
			curDataSetProvider.addFileMD(callingDataSetProvider.getFileMDList().get(0));
			dataSetProviderToTDB(curDataSetProvider);
		} else {
			dataSetProviderToTDB(curDataSetProvider);
		}

		super.okPressed();
	}

	private void dataSetProviderToTDB(DataSetProvider dsProvider) {
		DataSetMD dataSetMD = curDataSetProvider.getDataSetMD();

		Model model = SelectTDB.model;
		// int dataSetIdPlusOne = DataSetKeeper.indexOf(dsProvider) + 1;
		Resource tdbResource = dsProvider.getTdbResource();
		System.out.println("tdbResource = " + tdbResource);
		// if (model.contains(tdbResource, ETHOLD.localSerialNumber)) {
		// NodeIterator nodeIterator = model.listObjectsOfProperty(
		// tdbResource, ETHOLD.localSerialNumber);
		// while (nodeIterator.hasNext()) {
		// RDFNode rdfNode = nodeIterator.next();
		// System.out.println("Is it literal? -- " + rdfNode.isLiteral());
		// model.remove(tdbResource, ETHOLD.localSerialNumber,
		// rdfNode.asLiteral());
		// // model.
		// }
		// }
		// model.addLiteral(tdbResource, ETHOLD.localSerialNumber,
		// model.createTypedLiteral(dataSetIdPlusOne));

		if (model.contains(tdbResource, RDFS.label)) {
			NodeIterator nodeIterator = model.listObjectsOfProperty(tdbResource, RDFS.label);
			while (nodeIterator.hasNext()) {
				RDFNode rdfNode = nodeIterator.next();
				System.out.println("Is it literal? -- " + rdfNode.isLiteral());
				model.remove(tdbResource, RDFS.label, rdfNode.asLiteral());
			}
		}
		assert tdbResource != null : "tdbResource cannot be null";
		assert RDFS.label != null : "RDFS.label cannot be null";
		assert dataSetMD.getName() != null : "dataSetMD.getName() cannot be null";
		model.addLiteral(tdbResource, RDFS.label, model.createLiteral(dataSetMD.getName()));

		if (model.contains(tdbResource, RDFS.comment)) {
			NodeIterator nodeIterator = model.listObjectsOfProperty(tdbResource, RDFS.comment);
			while (nodeIterator.hasNext()) {
				RDFNode rdfNode = nodeIterator.next();
				System.out.println("Is it literal? -- " + rdfNode.isLiteral());
				model.remove(tdbResource, RDFS.comment, rdfNode.asLiteral());
			}
		}
		model.addLiteral(tdbResource, RDFS.comment, model.createLiteral(dataSetMD.getComments()));

		if (model.contains(tdbResource, DCTerms.hasVersion)) {
			NodeIterator nodeIterator = model.listObjectsOfProperty(tdbResource, DCTerms.hasVersion);
			while (nodeIterator.hasNext()) {
				RDFNode rdfNode = nodeIterator.next();
				System.out.println("Is it literal? -- " + rdfNode.isLiteral());
				model.remove(tdbResource, DCTerms.hasVersion, rdfNode.asLiteral());
			}
		}
		model.addLiteral(tdbResource, DCTerms.hasVersion, model.createLiteral(dataSetMD.getVersion()));

	}

	// private String[] getFileInfo() {
	// List<String> filenameList = new ArrayList<String>();
	// List<FileMD> fileList = dataSetProvider.getFileMDList();
	// for (FileMD fileMD : fileList) {
	// int id = fileList.indexOf(fileMD);
	// int idPlusOne = id + 1;
	// String name = fileMD.getFilename();
	// filenameList.add(idPlusOne + ":" + name);
	// }
	// return filenameList.toArray(new String[filenameList.size()]);
	// }

	// COLLECT INFO ABOUT DATA SETS FROM THE TDB
	private String[] getDataSetInfo() {
		Model model = SelectTDB.model;
		if (curDataSetProvider != null) {
			Integer id = DataSetKeeper.indexOf(curDataSetProvider);
			String name = curDataSetProvider.getDataSetMD().getName();
			// String version = dataSetProvider.getDataSetMD().getVersion();
			// int id_plus_one = id + 1;
			String[] results = new String[1];
			// results[0] = id_plus_one + ": " + name + " " + version;
			results[0] = name;
			return results;
		} else if (callingFileMD != null) {
			String[] results = new String[DataSetKeeper.size() + 1];
			List<String> toSort = new ArrayList<String>();
			// results[0] = ""; // RESERVING THIS FOR THE FIRST ENTRY (DEFAULT =
			// // NEW)
			List<Integer> ids = DataSetKeeper.getIDs();
			Iterator<Integer> iterator = ids.iterator();

			Integer id = 0;
			// int id_plus_one = id + 1;
			int counter = 0;
			while (iterator.hasNext()) {
				counter++;
				id = iterator.next();
				// id_plus_one = id + 1;
				DataSetProvider dsProvider = DataSetKeeper.get(id);
				Resource tdbResource = dsProvider.getTdbResource();
				if (model.contains(tdbResource, RDFS.label)) {
					String name = model.listObjectsOfProperty(tdbResource, RDFS.label).next().asLiteral().getString();
					toSort.add(name);
				}

				// if (model.contains(tdbResource, DCTerms.hasVersion)) {
				// version = model.listObjectsOfProperty(tdbResource,
				// DCTerms.hasVersion).next().asLiteral().getString();
				// } else if (model.contains(tdbResource,
				// ECO.hasMajorVersionNumber)) {
				// version = model.listObjectsOfProperty(tdbResource,
				// ECO.hasMajorVersionNumber).next().asLiteral().getString();
				// if (model.contains(tdbResource, ECO.hasMinorVersionNumber)) {
				// version += "." + model.listObjectsOfProperty(tdbResource,
				// ECO.hasMinorVersionNumber).next().asLiteral().getString();
				// }
				// }
				// results[counter] = id_plus_one + ":" + name + " " + version;
				// results[counter] = name;
			}
			Collections.sort(toSort);
			// Integer next = id_plus_one + 1;
			results[0] = "(new data set)";
			for (int i = 0; i < toSort.size(); i++) {
				results[i + 1] = toSort.get(i);
			}
			return results;
		} else {
			// if (DataSetKeeper.size() == 0){return null;}
			String[] results = new String[DataSetKeeper.size()];
			List<String> toSort = new ArrayList<String>();

			// results[0] = ""; // RESERVING THIS FOR THE FIRST ENTRY (DEFAULT =
			// // NEW)
			List<Integer> ids = DataSetKeeper.getIDs();
			Iterator<Integer> iterator = ids.iterator();

			Integer id = 0;
			// int id_plus_one = id + 1;
			// int counter = -1;
			while (iterator.hasNext()) {
				// counter++;
				id = iterator.next();
				// id_plus_one = id + 1;
				DataSetProvider dsProvider = DataSetKeeper.get(id);
				Resource tdbResource = dsProvider.getTdbResource();
				// String name = "";
				// String version = "";
				if (model.contains(tdbResource, RDFS.label)) {
					String name = model.listObjectsOfProperty(tdbResource, RDFS.label).next().asLiteral().getString();
					toSort.add(name);
				}
				// if (model.contains(tdbResource, RDFS.label)) {
				// name = model.listObjectsOfProperty(tdbResource,
				// RDFS.label).next().asLiteral().getString();
				// }
				// if (model.contains(tdbResource, DCTerms.hasVersion)) {
				// version = model.listObjectsOfProperty(tdbResource,
				// DCTerms.hasVersion).next().asLiteral().getString();
				// } else if (model.contains(tdbResource,
				// ECO.hasMajorVersionNumber)) {
				// version = model.listObjectsOfProperty(tdbResource,
				// ECO.hasMajorVersionNumber).next().asLiteral().getString();
				// if (model.contains(tdbResource, ECO.hasMinorVersionNumber)) {
				// version += "." + model.listObjectsOfProperty(tdbResource,
				// ECO.hasMinorVersionNumber).next().asLiteral().getString();
				// }
				// }
				// // results[counter] = id_plus_one + ":" + name + " " +
				// version;
				// results[counter] = name;

			}
			Collections.sort(toSort);

			// Integer next = id_plus_one + 1;
			// results[0] = next + ": (new data set)";
			for (int i = 0; i < toSort.size(); i++) {
				results[i] = toSort.get(i);
			}
			return results;
		}
	}

	private void populateFileMeta() {
		int index = fileMDCombo.getSelectionIndex();
		System.out.println("index = " + index);
		List<FileMD> fileList = curDataSetProvider.getFileMDList();
		if (index < 0) {
			return;
		}
		if (fileList.size() <= 0) {
			return;
		}
		if (index == 0) {
			curFileMD = callingDataSetProvider.getFileMDList().get(0);
		} else if (fileList.size() <= index) {
			curFileMD = fileList.get(index - 1);
		} else {
			curFileMD = fileList.get(index);
		}

		// dialogValues.get(0).setText(fileMD.getFilename());
		// fileMDCombo.setToolTipText(fileMD.getPath()); // THIS CAN'T BE
		// UPDATED WITHOUT FIRING THE MODIFY LISTENER! HOW TO DO THIS?
		// fileMDCombo.removeModifyListener(fileMDComboModifyListener);
		// fileMDCombo.setToolTipText(fileMD.getPath());
		// fileMDCombo.addModifyListener(fileMDComboModifyListener);
		dialogValues.get(0).setText(curFileMD.getSize() + "");
		dialogValues.get(1).setText(Util.getLocalDateFmt(curFileMD.getLastModified()));
		dialogValues.get(2).setText(Util.getLocalDateFmt(curFileMD.getReadTime()));
	}

	protected void populateMeta() {
		String dataSetChosen = combo.getText();
		// if (dataSetChosen.endsWith("new data set)")) {
		if ((callingFileMD != null) && (comboSelectionIndex == 0)) {
			newDataSet = true;
			curDataSetProvider = callingDataSetProvider;
			curFileMD = callingDataSetProvider.getFileMDList().get(0);
			System.out.println("... it is new");
		} else {
			newDataSet = false;
			int dsNum = DataSetKeeper.indexOfDataSetName(dataSetChosen);
			if (dsNum > -1) {
				curDataSetProvider = DataSetKeeper.get(dsNum);
			} else {
				curDataSetProvider.getDataSetMD().setName(dataSetChosen);
				// dataSetProvider = null;
				// System.out.println("What happened?");
			}
		}

		List<FileMD> fileMDList = curDataSetProvider.getFileMDList();
		System.out.println("fileMDList has size: " + fileMDList.size());
		if (callingDataSetProvider != null) {
			curFileMD = callingDataSetProvider.getFileMDList().get(0);
		} else if (fileMDList.size() > 0) {
			curFileMD = fileMDList.get(0);
		} else {
			curFileMD = null;
		}
//		dataSetMD = curDataSetProvider.getDataSetMD();
//		curatorMD = curDataSetProvider.getCuratorMD();
		redrawDialogRows();
	}

	protected void redrawDialogDataSetMD() {
		DataSetMD dataSetMD = curDataSetProvider.getDataSetMD();
		System.out.println("dataSetMD.getName: = " + dataSetMD.getName());
		// dialogValues.get(3).setText(dataSetMD.getName()); //FIXME
		// combo.setText(dataSetMD.getName());
		dialogValues.get(3).setText(dataSetMD.getVersion());
		dialogValues.get(4).setText(dataSetMD.getComments());
		dialogValues.get(5).setText(dataSetMD.getContactName());
		dialogValues.get(6).setText(dataSetMD.getContactAffiliation());
		dialogValues.get(7).setText(dataSetMD.getContactEmail());
		dialogValues.get(8).setText(dataSetMD.getContactPhone());
	}

	protected void redrawDialogFileMD() {
		fileMDCombo.setToolTipText(curFileMD.getPath());
		dialogValues.get(0).setText(curFileMD.getSize() + "");
		dialogValues.get(1).setText(Util.getLocalDateFmt(curFileMD.getLastModified()));
		dialogValues.get(2).setText(Util.getLocalDateFmt(curFileMD.getReadTime()));
	}

	protected void redrawDialogCuratorMD() {
		CuratorMD curatorMD = curDataSetProvider.getCuratorMD();
		System.out.println("curatorMD.getName: = " + curatorMD.getName());
		dialogValues.get(9).setText(curatorMD.getName());
		dialogValues.get(10).setText(curatorMD.getAffiliation());
		dialogValues.get(11).setText(curatorMD.getEmail());
		dialogValues.get(12).setText(curatorMD.getPhone());
	}

	protected void redrawDialogRows() {
		System.out.println(" in redrawDialogRows()");
		// CLEAR ALL DIALOG BOXES (BECAUSE WE'LL REDRAW)
		Iterator<Text> dialogValueIterator = dialogValues.iterator();
		while (dialogValueIterator.hasNext()) {
			Text dialogValue = dialogValueIterator.next();
			dialogValue.setText("");
		}

		// String[] listOfFiles = getFileInfo();
		// int index = 0;
		List<FileMD> tempFileMDList = new ArrayList<FileMD>();
		if (!newDataSet && (callingFileMD != null)) {
			// NOT A NEW DATA SET, BUT A NEW FILE NAME (TO ADD, PRESUMABLY)
			tempFileMDList.add(callingDataSetProvider.getFileMDList().get(0));
		}
		tempFileMDList.addAll(curDataSetProvider.getFileMDList());
		fileMDComboMgr.setItems(tempFileMDList);
		fileMDComboMgr.setText(curFileMD);

		redrawDialogDataSetMD();
		redrawDialogFileMD();
		redrawDialogCuratorMD();
	}

	private class FileMDComboMgr {
		List<FileMD> fileMDlist = null;

		public FileMDComboMgr() {

		}

		public void setItems(List<FileMD> fileMDlist) {
			this.fileMDlist = fileMDlist;
			String[] temp = new String[fileMDlist.size()];
			int index = 0;
			for (FileMD fileMD : fileMDlist) {
				temp[index++] = fileMD.getFilename();
			}
			fileMDCombo.setItems(temp);
		}

		public FileMD getFileMD(int index) {
			return fileMDlist.get(index);
		}

		public void setText(FileMD fileMD) {
			if (fileMD == null) {
				return;
			}
			fileMDCombo.setText(fileMD.getFilename());
		}
	}

	public class FileMDComboModifyListener implements ModifyListener {

		@Override
		public void modifyText(ModifyEvent e) {
			System.out.println("ModifyEvent=" + e.toString());
			// redrawDialogRows();
			System.out.println("fileMDCombo index " + fileMDCombo.getSelectionIndex());
			populateFileMeta();
			System.out.println("choice is " + fileMDCombo.getSelectionIndex() + " with value: " + fileMDCombo.getText());
		}

	}
}
