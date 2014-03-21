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
	// private boolean newFileMD = false;
	private boolean newDataSet = false;
	// private boolean dataSetEnabled = true;
	private DataSetProvider curDataSetProvider = null;
	private DataSetProvider callingDataSetProvider = null;
	// private FileMD curFileMD = null;
	private FileMD callingFileMD = null;
	// private DataSetMD dataSetMD = null;
	// private CuratorMD curatorMD = null;
	// private Resource tdbResource = null;
	private Combo comboDataSetSelector = null;
	private Combo comboFileSelector = null;
	private List<Text> dialogValues = new ArrayList<Text>();
	// private ComboFileSelectorMgr comboFileSelectorMgr;
	// private FileMDComboModifyListener FileMDComboModifyListener;
	private ComboFileSelectorListener comboFileSelectorListener;
	protected String comboTextSaved = "";
	protected boolean comboKeyHeldDown = false;
	private int comboSelectionIndex = -1;

	// THERE ARE THREE WAYS TO CALL THIS:
	// CASE 1) SIMPLY TO VIEW OR EDIT DATA SET INFO
	// CASE 2) WITH A SELECTED FILE AND ITS DATA SET
	// CASE 3) WITH A NEW FILE TO ADD TO AN EXISTING DATA SET
	// CASE 4) WITH A NEW FILE TO ADD TO A NEW DATA SET (CREATED HERE)

	/**
	 * @wbp.parser.constructor
	 */
	public CSVMetaDialog(Shell parentShell) {
		super(parentShell);
		// CASE 1 - EDIT DATA SET INFO FOR ANY EXISTING DATA SET
		if (DataSetKeeper.size() == 0) {
			new GenericMessageBox(parentShell, "No Data Sets", "The HT does not contain any DataSets at this time.  Read a CSV or RDF file to create some.");
			return;
		}
		if (DataSetKeeper.size() == 0) {
			return;
		}
		this.curDataSetProvider = DataSetKeeper.get(0);
		// if (curDataSetProvider.getFileMDList().size() > 0) {
		// this.curFileMD = curDataSetProvider.getFileMDList().get(0);
		// }
	}

	public CSVMetaDialog(Shell parentShell, FileMD fileMD, DataSetProvider dataSetProvider) {
		// CASE 2 - EDIT DATA SET INFO FOR ONE DATA SET ONLY
		super(parentShell);
		this.callingDataSetProvider = dataSetProvider;
		this.callingFileMD = fileMD;
		this.curDataSetProvider = callingDataSetProvider;
		// this.curFileMD = callingFileMD;
	}

	public CSVMetaDialog(Shell parentShell, FileMD fileMD) {
		super(parentShell);
		// CASE 3 - NEW FILE TO ADD TO EXISTING DATA SET
		// CASE 4 - NEW FILE TO ADD TO NEW DATA SET (CREATED HERE)
		assert fileMD != null : "fileMD cannot be null";
		this.callingFileMD = fileMD;
		curDataSetProvider = new DataSetProvider();
		curDataSetProvider.addFileMD(callingFileMD);
		curDataSetProvider.setDataSetMD(new DataSetMD());
		curDataSetProvider.getDataSetMD().setName("(new data set)");
		curDataSetProvider.addFileMD(callingFileMD);
		curDataSetProvider.setCuratorMD(new CuratorMD());
	}

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
		if ((callingFileMD != null) && (callingDataSetProvider == null)) {
			comboDataSetSelector = new Combo(composite, SWT.DROP_DOWN);
			comboDataSetSelector.setToolTipText("Choose an existing data set or type a name in the first selection to create a new one");
		} else {
			comboDataSetSelector = new Combo(composite, SWT.READ_ONLY);
		}
		comboDataSetSelector.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		comboDataSetSelector.setItems(getDataSetInfo());

		// combo.addMouseListener(new MouseListener() {
		//
		// @Override
		// public void mouseDoubleClick(MouseEvent e) {
		//
		// }
		//
		// @Override
		// public void mouseDown(MouseEvent e) {
		//
		// }
		//
		// @Override
		// public void mouseUp(MouseEvent e) {
		//
		// }
		// });

		comboDataSetSelector.addKeyListener(new KeyListener() {

			private int comboSelectionIndexSaved;

			@Override
			public void keyReleased(KeyEvent e) {
				System.out.println("keyReleased=" + e.toString());
				comboKeyHeldDown = false;
				System.out.println("comboSelectionIndex = " + comboSelectionIndex);
				if (comboSelectionIndex == 0) {
					// allow typing
					comboDataSetSelector.setItem(comboSelectionIndex, curDataSetProvider.getDataSetMD().getName());
				} else {
					// no typing
					comboDataSetSelector.setText(comboTextSaved);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println("keyPressed=" + e.toString());
				System.out.println("comboSelectionIndex = " + comboSelectionIndex);
				if (!comboKeyHeldDown && (comboSelectionIndex != 0)) {
					comboTextSaved = comboDataSetSelector.getText();
					comboKeyHeldDown = true;
				}
			}
		});
		comboDataSetSelector.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("combo.addSelectionListener.widgetSelectedr=" + e.toString());
				comboSelectionIndex = comboDataSetSelector.getSelectionIndex();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				System.out.println("combo.addSelectionListener.widgetDefaultSelected=" + e.toString());
				comboSelectionIndex = comboDataSetSelector.getSelectionIndex();

			}

		});

		comboDataSetSelector.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				// comboSelectionIndex = combo.getSelectionIndex();
				if (comboKeyHeldDown) {
					// do nothing
				} else {
					System.out.println("Modify event: " + e.toString());
					System.out.println("Modify Event: combo.getSelectionIndex() = " + comboDataSetSelector.getSelectionIndex() + " with combo.getText() = "
							+ comboDataSetSelector.getText());
					populateDataSetMD();
				}
			}
		});

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
		comboFileSelector = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		// comboFileSelectorMgr = new ComboFileSelectorMgr();
		createComboFileSelectorList();
		comboFileSelector.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		// NEXT STEP: COLLECT FILE LIST INFO BASED ON WHAT IS PASSED, AND ADD
		// OTHER
		comboFileSelector.setToolTipText("Files associated with this data set." + comboDataSetSelector.getText());
		comboFileSelectorListener = new ComboFileSelectorListener();
		comboFileSelector.addModifyListener(comboFileSelectorListener);

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

		comboDataSetSelector.select(0);
		comboSelectionIndex = comboDataSetSelector.getSelectionIndex();

		redrawDialogRows();

		Control control = super.createDialogArea(parent);
		control.setToolTipText("");
		return control;
	}

	// @Override
	// public int open() {
	// if (callingFileMD != null) {
	// // getButton(IDialogConstants.OK_ID).setEnabled(false);
	// }
	// return super.open();
	// }

	@Override
	protected void cancelPressed() {

		super.cancelPressed();
	}

	@Override
	protected void okPressed() {
		DataSetMD dataSetMD = curDataSetProvider.getDataSetMD();
		CuratorMD curatorMD = curDataSetProvider.getCuratorMD();
		// dataSetMD.setName(dialogValues.get(3).getText()); //FIXME
		dataSetMD.setName(comboDataSetSelector.getText()); // FIXME
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

		if (newDataSet) {
			boolean success = DataSetKeeper.add(curDataSetProvider); // A DataSetProvider IS BORN!!
			System.out.println("Created new DataSetProvider succees: = " + success);
		} else if ((callingFileMD != null) && (callingDataSetProvider == null)) {
			curDataSetProvider.addFileMD(callingFileMD);
		}
		dataSetProviderToTDB(curDataSetProvider);
		super.okPressed();
	}

	private void dataSetProviderToTDB(DataSetProvider dsProvider) {
		// SHOULD BREAK OUT TO ITS OWN CLASS OR ADD TO DataSetProvider or SelectTDB
		DataSetMD dataSetMD = dsProvider.getDataSetMD();

		Model model = SelectTDB.model;
		Resource tdbResource = dsProvider.getTdbResource();
		assert tdbResource != null : "tdbResource cannot be null";
		assert RDFS.label != null : "RDFS.label cannot be null";
		assert dataSetMD.getName() != null : "dataSetMD.getName() cannot be null";
		System.out.println("tdbResource = " + tdbResource);

		if (model.contains(tdbResource, RDFS.label)) {
			// REPLACE OTHER label(s)
			NodeIterator nodeIterator = model.listObjectsOfProperty(tdbResource, RDFS.label);
			while (nodeIterator.hasNext()) {
				RDFNode rdfNode = nodeIterator.next();
				assert rdfNode.isLiteral() : "DataSet RDFS.label value must be literal!";
				model.remove(tdbResource, RDFS.label, rdfNode.asLiteral());
			}
		}

		model.addLiteral(tdbResource, RDFS.label, model.createLiteral(dataSetMD.getName()));

		if (model.contains(tdbResource, RDFS.comment)) {
			// REPLACE OTHER comment(s)
			NodeIterator nodeIterator = model.listObjectsOfProperty(tdbResource, RDFS.comment);
			while (nodeIterator.hasNext()) {
				RDFNode rdfNode = nodeIterator.next();
				System.out.println("Is it literal? -- " + rdfNode.isLiteral());
				model.remove(tdbResource, RDFS.comment, rdfNode.asLiteral());
			}
		}
		if (!dataSetMD.getComments().matches("^\\s*$")) {
			// ONLY IF NOT ALL WHITE SPACES
			model.addLiteral(tdbResource, RDFS.comment, model.createLiteral(dataSetMD.getComments()));
		}

		if (model.contains(tdbResource, DCTerms.hasVersion)) {
			NodeIterator nodeIterator = model.listObjectsOfProperty(tdbResource, DCTerms.hasVersion);
			while (nodeIterator.hasNext()) {
				RDFNode rdfNode = nodeIterator.next();
				System.out.println("Is it literal? -- " + rdfNode.isLiteral());
				model.remove(tdbResource, DCTerms.hasVersion, rdfNode.asLiteral());
			}
		}
		if (!dataSetMD.getVersion().matches("^\\s*$")) {
			model.addLiteral(tdbResource, DCTerms.hasVersion, model.createLiteral(dataSetMD.getVersion()));
		}
	}

	// COLLECT INFO ABOUT DATA SETS FROM THE TDB
	private String[] getDataSetInfo() {
		List<String> toSort = new ArrayList<String>();
		if (callingDataSetProvider != null) {
			String[] results = new String[1];
			results[0] = callingDataSetProvider.getDataSetMD().getName();
			return results;
		} else {
			List<Integer> ids = DataSetKeeper.getIDs();
			Iterator<Integer> iterator = ids.iterator();

			while (iterator.hasNext()) {
				int id = iterator.next();
				toSort.add(DataSetKeeper.get(id).getDataSetMD().getName());
			}
			Collections.sort(toSort);
		}
		if ((callingFileMD != null) && (callingDataSetProvider == null)) {
			String[] results = new String[DataSetKeeper.size() + 1];
			results[0] = "(new data set)";
			for (int i = 0; i < toSort.size(); i++) {
				results[i + 1] = toSort.get(i);
			}
			return results;
		} else {
			String[] results = new String[DataSetKeeper.size()];
			for (int i = 0; i < toSort.size(); i++) {
				results[i] = toSort.get(i);
			}
			return results;
		}
	}

	protected void populateDataSetMD() {
		String selectedDataSetName = comboDataSetSelector.getText();
		int selectedDataSetID = DataSetKeeper.indexOfDataSetName(selectedDataSetName);
		if (selectedDataSetID > -1) {
			curDataSetProvider = DataSetKeeper.get(selectedDataSetID);
		}

		redrawDialogRows();

		// if ((callingFileMD != null) && (comboSelectionIndex == 0)) {
		// newDataSet = true;
		// curDataSetProvider = callingDataSetProvider;
		// curFileMD = callingDataSetProvider.getFileMDList().get(0);
		// System.out.println("... it is new");
		// } else {
		// newDataSet = false;
		// int dsNum = DataSetKeeper.indexOfDataSetName(selectedDataSetName);
		// if (dsNum > -1) {
		// curDataSetProvider = DataSetKeeper.get(dsNum);
		// } else {
		// curDataSetProvider.getDataSetMD().setName(selectedDataSetName);
		// // dataSetProvider = null;
		// // System.out.println("What happened?");
		// }
		// }
		//
		// List<FileMD> fileMDList = curDataSetProvider.getFileMDList();
		// System.out.println("fileMDList has size: " + fileMDList.size());
		// if (callingDataSetProvider != null) {
		// curFileMD = callingDataSetProvider.getFileMDList().get(0);
		// } else if (fileMDList.size() > 0) {
		// curFileMD = fileMDList.get(0);
		// } else {
		// curFileMD = null;
		// }
		// // dataSetMD = curDataSetProvider.getDataSetMD();
		// // curatorMD = curDataSetProvider.getCuratorMD();
		// redrawDialogRows();
	}

	// private void populateFileMD() {
	// int index = comboFileSelector.getSelectionIndex();
	// if (index < 0) {
	// return;
	// }
	// List<FileMD> fileList = curDataSetProvider.getFileMDList();
	// if (fileList.size() <= 0) {
	// return;
	// }
	// if ((index == 0) && ((callingFileMD != null) && (callingDataSetProvider == null))) {
	// curFileMD = callingFileMD;
	// } else {
	// curFileMD = fileList.get(index);
	// }
	// redrawDialogFileMD();
	// }

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
		FileMD curFileMD = callingFileMD; // MAY BE NULL
		int index = comboFileSelector.getSelectionIndex();
		if (index >= 0) {
			if (curDataSetProvider != null) {
				if (callingFileMD != null) {
					if (index > 0) {
						curFileMD = curDataSetProvider.getFileMDList().get(index - 1);
					}
				} else {
					curFileMD = curDataSetProvider.getFileMDList().get(index);
				}
			}
		}
		if (curFileMD == null) {
			dialogValues.get(0).setText("");
			dialogValues.get(1).setText("");
			dialogValues.get(2).setText("");
		} else {
			comboFileSelector.setToolTipText(curFileMD.getPath());
			dialogValues.get(0).setText(curFileMD.getSize() + "");
			dialogValues.get(1).setText(Util.getLocalDateFmt(curFileMD.getLastModified()));
			dialogValues.get(2).setText(Util.getLocalDateFmt(curFileMD.getReadTime()));
		}
	}

	protected void redrawDialogCuratorMD() {
		CuratorMD curatorMD = curDataSetProvider.getCuratorMD();
		System.out.println("curatorMD.getName: = " + curatorMD.getName());
		dialogValues.get(9).setText(curatorMD.getName());
		dialogValues.get(10).setText(curatorMD.getAffiliation());
		dialogValues.get(11).setText(curatorMD.getEmail());
		dialogValues.get(12).setText(curatorMD.getPhone());
	}

	protected void clearDialogRows() {
		Iterator<Text> dialogValueIterator = dialogValues.iterator();
		while (dialogValueIterator.hasNext()) {
			Text dialogValue = dialogValueIterator.next();
			dialogValue.setText("");
		}
	}

	protected void redrawDialogRows() {
		// System.out.println(" in redrawDialogRows()");
		// CLEAR ALL DIALOG BOXES (BECAUSE WE'LL REDRAW)

		// String[] listOfFiles = getFileInfo();
		// int index = 0;
		// List<FileMD> tempFileMDList = new ArrayList<FileMD>();
		// if (!newDataSet && ((callingFileMD != null) && (callingDataSetProvider == null))) {
		// // NOT A NEW DATA SET, BUT A NEW FILE NAME (TO ADD, PRESUMABLY)
		// tempFileMDList.add(callingDataSetProvider.getFileMDList().get(0));
		// }
		// tempFileMDList.addAll(curDataSetProvider.getFileMDList());
		// comboFileSelectorMgr.setItems(tempFileMDList);
		// comboFileSelectorMgr.setText(curFileMD);
		clearDialogRows();

		redrawDialogDataSetMD();

		createComboFileSelectorList();
		redrawDialogFileMD();

		redrawDialogCuratorMD();
	}

	protected void createComboFileSelectorList() {
		comboFileSelector.removeAll();
		if (curDataSetProvider == null) {
			if (callingFileMD != null) {
				comboFileSelector.add(callingFileMD.getFilename());
			}
			return;
		}
		List<FileMD> fileMDList = curDataSetProvider.getFileMDList();
		int selectionIndex = 0;
		if ((callingFileMD != null) && (callingDataSetProvider == null)) {
			comboFileSelector.add(callingFileMD.getFilename());
		}
		for (int i = 0; i < fileMDList.size(); i++) {
			FileMD fileMD = fileMDList.get(i);
			comboFileSelector.add(fileMD.getFilename());
			if (callingFileMD == fileMD) {
				selectionIndex = i;
			}
		}
		comboFileSelector.select(selectionIndex);
		// populateFileMD();
	}

	// private class ComboFileSelectorMgr {
	// List<FileMD> fileMDlist = null;
	//
	// public ComboFileSelectorMgr() {
	//
	// }
	//
	// public void setItems(List<FileMD> fileMDlist) {
	// this.fileMDlist = fileMDlist;
	// String[] temp = new String[fileMDlist.size()];
	// int index = 0;
	// for (FileMD fileMD : fileMDlist) {
	// temp[index++] = fileMD.getFilename();
	// }
	// comboFileSelector.setItems(temp);
	// }
	//
	// public FileMD getFileMD(int index) {
	// return fileMDlist.get(index);
	// }
	//
	// public void setText(FileMD fileMD) {
	// if (fileMD == null) {
	// return;
	// }
	// comboFileSelector.setText(fileMD.getFilename());
	// }
	// }

	public class ComboFileSelectorListener implements ModifyListener {

		@Override
		public void modifyText(ModifyEvent e) {
			System.out.println("ModifyEvent=" + e.toString());
			// redrawDialogRows();
			System.out.println("fileMDCombo index " + comboFileSelector.getSelectionIndex());
			// populateFileMD();
			redrawDialogFileMD();
			System.out.println("choice is " + comboFileSelector.getSelectionIndex() + " with value: " + comboFileSelector.getText());
		}

	}
}
