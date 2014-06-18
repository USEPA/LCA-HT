package harmonizationtool.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;
import harmonizationtool.model.CuratorMD;
import harmonizationtool.model.DataSourceKeeper;
import harmonizationtool.model.DataSourceMD;
import harmonizationtool.model.DataSourceProvider;
import harmonizationtool.model.FileMD;
import harmonizationtool.model.ModelProvider;
import harmonizationtool.utils.Util;

import org.apache.log4j.Logger;
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
//import org.eclipse.swt.events.KeyEvent;
//import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.eclipse.swt.widgets.Button;
import org.eclipse.wb.swt.SWTResourceManager;

public class MetaDataDialog extends TitleAreaDialog {

	public ModelProvider modelProvider = null;
	private DataSourceProvider curDataSourceProvider = null;
	private DataSourceProvider callingDataSourceProvider = null;
	private DataSourceProvider newDataSourceProvider = null;
	private FileMD callingFileMD = null;
	private Combo comboDataSourceSelector = null;
	private Combo comboFileSelector = null;
	private List<Text> dialogValues = new ArrayList<Text>();
	// private Color red = new Color(Display.getCurrent(), 255, 0, 0);
	private Color defaultBG = null;
	private String newDataSourceTempName = "";

	// private String newFileName = null;
	// Label lbl_01 = null;
	private Logger runLogger = Logger.getLogger("run");

	private ComboFileSelectorListener comboFileSelectorListener;
	protected String combDataSourceSelectorSavedText = "";
	// protected boolean comboKeyHeldDown = false;
	private int comboSelectionIndex = -1;

	// THERE ARE THREE WAYS TO CALL THIS:
	// CASE 1) SIMPLY TO VIEW OR EDIT DATA SET INFO
	// CASE 2) WITH A SELECTED FILE AND ITS DATA SET
	// CASE 3) WITH A NEW FILE TO ADD TO AN EXISTING DATA SET
	// CASE 4) WITH A NEW FILE TO ADD TO A NEW DATA SET (CREATED HERE)

	/**
	 * @wbp.parser.constructor
	 */
	public MetaDataDialog(Shell parentShell) {
		super(parentShell);
		// CASE 1 - EDIT DATA SET INFO FOR ANY EXISTING DATA SET
		if (DataSourceKeeper.size() == 0) {
			new GenericMessageBox(parentShell, "No Data Sets", "The HT does not contain any DataSources at this time.  Read a CSV or RDF file to create some.");
			return;
		}
		if (DataSourceKeeper.size() == 0) {
			return;
		}
		this.curDataSourceProvider = DataSourceKeeper.get(0);
		runLogger.info("SET META existing DataSource");
	}

	public MetaDataDialog(Shell parentShell, FileMD fileMD, DataSourceProvider dataSourceProvider) {
		// CASE 2 - EDIT DATA SET INFO FOR ONE DATA SET ONLY (WITH A FILE
		// SELECTED)
		super(parentShell);
		assert fileMD != null : "fileMD cannot be null";
		this.callingDataSourceProvider = dataSourceProvider;
		this.callingFileMD = fileMD;
		this.curDataSourceProvider = callingDataSourceProvider;
		newDataSourceTempName = DataSourceKeeper.uniquify(fileMD.getFilename().substring(0, fileMD.getFilename().length() - 4));
		// this.curFileMD = callingFileMD;
		runLogger.info("SET META start - existing DataSource");
		runLogger.info("  start name = " + dataSourceProvider.getDataSourceMD().getName());
	}

	public MetaDataDialog(Shell parentShell, FileMD fileMD) {
		super(parentShell);
		// CASE 3 - NEW FILE TO ADD TO EXISTING DATA SET
		// CASE 4 - NEW FILE TO ADD TO NEW DATA SET (CREATED HERE)

		assert fileMD != null : "fileMD cannot be null";
		this.callingFileMD = fileMD;
		this.newDataSourceProvider = new DataSourceProvider();
		this.newDataSourceProvider.addFileMD(callingFileMD);
		this.newDataSourceProvider.setDataSourceMD(new DataSourceMD());
		this.newDataSourceProvider.getDataSourceMD().setName(fileMD.getFilename());
		this.newDataSourceProvider.setCuratorMD(new CuratorMD());
		this.curDataSourceProvider = this.newDataSourceProvider;
		newDataSourceTempName = DataSourceKeeper.uniquify(fileMD.getFilename().substring(0, fileMD.getFilename().length() - 4));

		if (DataSourceKeeper.size() == 0) {
			DataSourceKeeper.add(newDataSourceProvider);
		}
		runLogger.info("SET META start - new file");

	}

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
		lbl_01.setText("Select");

		// ADD THE DATA SET CHOOSER PULL DOWN
		// if (newDataSourceProvider != null) {
		// comboDataSourceSelector = new Combo(composite, SWT.DROP_DOWN);
		// comboDataSourceSelector
		// .setToolTipText("Choose an existing data set or type a name in the first selection to create a new one");
		// } else {
		comboDataSourceSelector = new Combo(composite, SWT.READ_ONLY);
		// }
		comboDataSourceSelector.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		comboDataSourceSelector.setItems(getDataSourceInfo());
		comboDataSourceSelector.select(0);

		// comboDataSourceSelector.addKeyListener(new KeyListener() {
		//
		// private int comboSelectionIndexSaved;
		//
		// @Override
		// public void keyReleased(KeyEvent e) {
		// System.out.println("combDataSourceSelectorSavedText :"
		// + combDataSourceSelectorSavedText);
		// System.out.println("comboDataSourceSelector.getText() :"
		// + comboDataSourceSelector.getText());
		// System.out.println("comboDataSourceSelector.getItem(0) :"
		// + comboDataSourceSelector.getItem(0));
		//
		// System.out.println("keyReleased=" + e.toString());
		// comboKeyHeldDown = false;
		// System.out.println("comboSelectionIndex = "
		// + comboSelectionIndex);
		// if (comboSelectionIndex != 0) {
		// comboDataSourceSelector.setText(combDataSourceSelectorSavedText);
		// } else {
		// comboDataSourceSelector.setText(comboDataSourceSelector.getText());
		// }
		// }
		//
		// @Override
		// public void keyPressed(KeyEvent e) {
		// System.out.println("keyPressed=" + e.toString());
		// System.out.println("comboSelectionIndex = "
		// + comboSelectionIndex);
		// // comboDataSourceSelector.setBackground(defaultBG);
		// if ((!comboKeyHeldDown) && (comboSelectionIndex != 0)) {
		// combDataSourceSelectorSavedText = comboDataSourceSelector
		// .getText();
		// }
		// comboKeyHeldDown = true;
		// }
		// });
		comboDataSourceSelector.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("combo.addSelectionListener.widgetSelectedr=" + e.toString());
				comboSelectionIndex = comboDataSourceSelector.getSelectionIndex();
				populateDataSourceMD();
				runLogger.info("  DATASOURCE SELECTED: " + comboDataSourceSelector.getText());

				// if (comboSelectionIndex == 0){
				// comboDataSourceSelector.setItem(0,
				// combDataSourceSelectorSavedText);
				// }
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				System.out.println("combo.addSelectionListener.widgetDefaultSelected=" + e.toString());
				comboSelectionIndex = comboDataSourceSelector.getSelectionIndex();
				populateDataSourceMD();
				runLogger.info("  DATASOURCE SELECTED: " + comboDataSourceSelector.getText());

				// if (comboSelectionIndex == 0){
				// comboDataSourceSelector.setItem(0,
				// combDataSourceSelectorSavedText);
				// }
			}

		});

		Button dataSourceRename = new Button(composite, SWT.BORDER);
		dataSourceRename.setToolTipText("Click to rename this data set.");
		dataSourceRename.setBounds(col2Left + 250, rowIndex * disBtwnRows - 1, 80, 25);
		dataSourceRename.setText("Rename");
		dataSourceRename.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				renameDataSource();
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
		Text text_08 = new Text(composite, SWT.BORDER | SWT.WRAP);
		text_08.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight * 2);

		rowIndex++;
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
		rowIndex++;
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
		comboFileSelector.setToolTipText("Files associated with this data set." + comboDataSourceSelector.getText());
		comboFileSelectorListener = new ComboFileSelectorListener();
		comboFileSelector.addModifyListener(comboFileSelectorListener);

		rowIndex++;
		Label lbl_03 = new Label(composite, SWT.RIGHT);
		lbl_03.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_03.setText("Size (bytes)");
		Text text_03 = new Text(composite, SWT.BORDER);
		text_03.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		text_03.setEditable(false);
		text_03.setBackground(defaultBG);

		rowIndex++;
		Label lbl_04 = new Label(composite, SWT.RIGHT);
		lbl_04.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_04.setText("Last Modified");
		Text text_04 = new Text(composite, SWT.BORDER);
		text_04.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		text_04.setEditable(false);
		text_04.setBackground(defaultBG);

		rowIndex++;
		Label lbl_05 = new Label(composite, SWT.RIGHT);
		lbl_05.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_05.setText("Read Time");
		Text text_05 = new Text(composite, SWT.BORDER);
		text_05.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		text_05.setEditable(false);
		text_05.setBackground(defaultBG);

		rowIndex++;
		Label sep_12a = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep_12a.setBounds(60, rowIndex * disBtwnRows - 5, 250, 2);

		Label lbl_12b = new Label(composite, SWT.LEFT);
		lbl_12b.setFont(SWTResourceManager.getFont("Lucida Grande", 16, SWT.BOLD));
		lbl_12b.setBounds(5, rowIndex * disBtwnRows, col1Width + col2Width, 20);
		lbl_12b.setText("Curator Information:");

		rowIndex++;
		Button copyCuratorInfo = new Button(composite, SWT.BORDER);
		copyCuratorInfo.setToolTipText("Values for the Curator will be copied from curator info set in the preferences.");
		copyCuratorInfo.setBounds(col2Left, rowIndex * disBtwnRows, 250, 25);
		copyCuratorInfo.setText("Copy Info from Preferences");
		copyCuratorInfo.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				dialogValues.get(9).setText(Util.getPreferenceStore().getString("curatorName"));
				dialogValues.get(10).setText(Util.getPreferenceStore().getString("curatorAffiliation"));
				dialogValues.get(11).setText(Util.getPreferenceStore().getString("curatorEmail"));
				dialogValues.get(12).setText(Util.getPreferenceStore().getString("curatorPhone"));
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

		// if (newDataSourceProvider != null) {
		// lbl_01.setText("Type new name (or select existing)");
		// }
		// comboSelectionIndex = comboDataSourceSelector.getSelectionIndex();
		// defaultBG = comboDataSourceSelector.getBackground();

		redrawDialogRows();

		Control control = super.createDialogArea(parent);
		control.setToolTipText("");
		return control;
	}

	@Override
	protected void cancelPressed() {

		super.cancelPressed();
		runLogger.info("SET META cancel");
	}

	@Override
	protected void okPressed() {
		String dataSourceName = comboDataSourceSelector.getText();
		System.out.println("comboDataSourceSelector.getText() " + comboDataSourceSelector.getText());
		DataSourceMD dataSourceMD = curDataSourceProvider.getDataSourceMD();
		CuratorMD curatorMD = curDataSourceProvider.getCuratorMD();

		dataSourceMD.setName(dataSourceName);
		dataSourceMD.setVersion(dialogValues.get(3).getText());
		dataSourceMD.setComments(dialogValues.get(4).getText());
		dataSourceMD.setContactName(dialogValues.get(5).getText());
		dataSourceMD.setContactAffiliation(dialogValues.get(6).getText());
		dataSourceMD.setContactEmail(dialogValues.get(7).getText());
		dataSourceMD.setContactPhone(dialogValues.get(8).getText());
		runLogger.info("  SET META: name = " + dataSourceName);
		runLogger.info("  SET META: version = " + dialogValues.get(3).getText());
		runLogger.info("  SET META: comments = \"" + Util.escape(dialogValues.get(4).getText()) + "\"");
		runLogger.info("  SET META: contactName = " + dialogValues.get(5).getText());
		runLogger.info("  SET META: contactAffiliation = " + dialogValues.get(6).getText());
		runLogger.info("  SET META: contactEmail = " + dialogValues.get(7).getText());
		runLogger.info("  SET META: contactPhone = " + dialogValues.get(8).getText());

		// curatorMD META DATA
		curatorMD.setName(dialogValues.get(9).getText());
		curatorMD.setAffiliation(dialogValues.get(10).getText());
		curatorMD.setEmail(dialogValues.get(11).getText());
		curatorMD.setPhone(dialogValues.get(12).getText());
		runLogger.info("  SET META: curatorName = " + dialogValues.get(9).getText());
		runLogger.info("  SET META: curatorAffiliation = " + dialogValues.get(10).getText());
		runLogger.info("  SET META: curatorEmail = " + dialogValues.get(11).getText());
		runLogger.info("  SET META: curatorPhone = " + dialogValues.get(12).getText());

		if (newDataSourceProvider != null) {
			if (comboSelectionIndex < 1) {

				// comboSelectionIndex = -1 if no change issued
				boolean success = DataSourceKeeper.add(curDataSourceProvider); // A
																			// DataSourceProvider
																			// IS
																			// BORN!!
				System.out.println("Created new DataSourceProvider succees: = " + success);
			}
		}
		if (callingFileMD != null) {
			curDataSourceProvider.addFileMD(callingFileMD);
			runLogger.info("  SET META: associated file = " + callingFileMD.getPath() + "/" + callingFileMD.getFilename());
		}

		System.out.println("newDataSourceProvider " + newDataSourceProvider);
		System.out.println("comboSelectionIndex " + comboSelectionIndex);

		ActiveTDB.syncDataSourceProviderToTDB(curDataSourceProvider);
		if (newDataSourceProvider != null) {
			FlowsWorkflow.setDataSourceProvider(curDataSourceProvider);
		}
		runLogger.info("SET META complete");

		super.okPressed();
	}

	private void renameDataSource() {
		// GenericStringBox genericStringBox = new GenericStringBox(getShell(),
		// comboDataSourceSelector.getText());
		boolean sendToFlowsWorkflow = false;
		// if (FlowsWorkflow.getTextFileInfo().endsWith(comboDataSourceSelector.getText())){
		// sendToFlowsWorkflow = true;
		// }
		GenericStringBox genericStringBox = new GenericStringBox(getShell(), comboDataSourceSelector.getText(), comboDataSourceSelector.getItems());

		genericStringBox.create("Name Data Set", "Please type a new data set name");
		genericStringBox.open();

		String newFileName = genericStringBox.getResultString();
		if (newFileName == null) {
			// cancel PRESSED
			return;
		}

		if (comboDataSourceSelector.getText().equals(newFileName)) {
			// SAME NAME, DO NOTHING
			return;
		}

		if (DataSourceKeeper.indexOfDataSourceName(newFileName) > -1) {
			new GenericMessageBox(getParentShell(), "Duplicate Name", "Data Set names must be onePerParentGroup.  Please choose a new name.");
			return;
		}
		curDataSourceProvider.getDataSourceMD().setName(newFileName);
		if (curDataSourceProvider.getTdbResource() != null) {
			// Literal oldNameLit = ActiveTDB.model
			// .createLiteral(comboDataSourceSelector.getText());
			Literal newNameLit = ActiveTDB.model.createLiteral(newFileName);
			ActiveTDB.removeAllWithSubjectPredicate(curDataSourceProvider.getTdbResource(), RDFS.label);
			ActiveTDB.model.add(curDataSourceProvider.getTdbResource(), RDFS.label, newNameLit);
		}

		comboDataSourceSelector.setItem(comboDataSourceSelector.getSelectionIndex(), newFileName);

	}

	// COLLECT INFO ABOUT DATA SETS FROM THE TDB
	private String[] getDataSourceInfo() {
		List<String> toSort = DataSourceKeeper.getNames();
		if (callingDataSourceProvider != null) {
			String[] results = new String[1];
			results[0] = callingDataSourceProvider.getDataSourceMD().getName();
			return results;
		} else if (newDataSourceProvider != null) {
			String[] results = new String[toSort.size() + 1];
			// results[0] = newDataSourceTempName;
			for (int i = 0; i < toSort.size(); i++) {
				results[i + 1] = toSort.get(i);
			}
			results[0] = newDataSourceTempName;
			curDataSourceProvider = newDataSourceProvider;
			return results;
		} else {
			String[] results = new String[toSort.size()];
			for (int i = 0; i < toSort.size(); i++) {
				results[i] = toSort.get(i);
			}
			curDataSourceProvider = DataSourceKeeper.get(DataSourceKeeper.indexOfDataSourceName(results[0]));
			return results;
		}
	}

	protected void populateDataSourceMD() {
		String selectedDataSourceName = comboDataSourceSelector.getText();
		int selectedDataSourceID = DataSourceKeeper.indexOfDataSourceName(selectedDataSourceName);

		if ((0 <= selectedDataSourceID) && (selectedDataSourceID < DataSourceKeeper.size())) {
			curDataSourceProvider = DataSourceKeeper.get(selectedDataSourceID);
		} else {
			curDataSourceProvider = newDataSourceProvider;
		}

		redrawDialogRows();
	}

	protected void redrawDialogDataSourceMD() {
		DataSourceMD dataSourceMD = curDataSourceProvider.getDataSourceMD();
		System.out.println("dataSourceMD.getName: = " + dataSourceMD.getName());
		dialogValues.get(3).setText(dataSourceMD.getVersion());
		dialogValues.get(4).setText(dataSourceMD.getComments());
		dialogValues.get(5).setText(dataSourceMD.getContactName());
		dialogValues.get(6).setText(dataSourceMD.getContactAffiliation());
		dialogValues.get(7).setText(dataSourceMD.getContactEmail());
		dialogValues.get(8).setText(dataSourceMD.getContactPhone());
	}

	protected void redrawDialogFileMD() {
		FileMD curFileMD = callingFileMD; // MAY BE NULL
		int index = comboFileSelector.getSelectionIndex();
		if (index >= 0) {
			if (callingFileMD != null) {
				if (index > 0) {
					curFileMD = curDataSourceProvider.getFileMDList().get(index - 1);
				}
			} else {
				curFileMD = curDataSourceProvider.getFileMDList().get(index);
			}
			// }
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
		CuratorMD curatorMD = curDataSourceProvider.getCuratorMD();
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
		clearDialogRows();

		redrawDialogDataSourceMD();

		createComboFileSelectorList();
		redrawDialogFileMD();

		redrawDialogCuratorMD();
	}

	protected void createComboFileSelectorList() {
		comboFileSelector.removeAll();
		if (curDataSourceProvider == null) {
			if (callingFileMD != null) {
				comboFileSelector.add(callingFileMD.getFilename());
			}
			return;
		}
		List<FileMD> fileMDList = curDataSourceProvider.getFileMDList();
		int selectionIndex = 0;
		if ((callingFileMD != null) && (callingDataSourceProvider == null)) {
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
	}

	public class ComboFileSelectorListener implements ModifyListener {

		@Override
		public void modifyText(ModifyEvent e) {
			System.out.println("ModifyEvent=" + e.toString());
			System.out.println("fileMDCombo index " + comboFileSelector.getSelectionIndex());
			redrawDialogFileMD();
			System.out.println("choice is " + comboFileSelector.getSelectionIndex() + " with value: " + comboFileSelector.getText());
		}

	}
}
