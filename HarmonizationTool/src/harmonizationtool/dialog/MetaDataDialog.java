package harmonizationtool.dialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.model.DataSourceKeeper;
import harmonizationtool.model.DataSourceProvider;
import harmonizationtool.model.FileMD;
import harmonizationtool.model.Person;
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

	private DataSourceProvider curDataSourceProvider = null;
	private DataSourceProvider newDataSourceProvider = null;
	private FileMD callingFileMD = null;
	private Combo comboDataSourceSelector = null;
	private Combo comboFileSelector = null;
	private List<Text> dialogValues = new ArrayList<Text>();
	// private Color red = new Color(Display.getCurrent(), 255, 0, 0);
	private Color defaultBG = null;

	// private String newFileName = null;
	// Label lbl_01 = null;
	private Logger runLogger = Logger.getLogger("run");

	private ComboFileSelectorListener comboFileSelectorListener;
	protected String combDataSourceSelectorSavedText = "";
	private int comboSelectionIndex = -1;

	// CALL THIS:
	// CASE 1) TO VIEW OR EDIT EXISTING DATA SOURCE INFO
	// CASE 2) WITH A NEW FILE TO
	// 2a) CREATE A NEW DATA SOURCE
	// 2b) ADD TO AN EXISTING DATA SOURCE

	/**
	 * @wbp.parser.constructor
	 */
	public MetaDataDialog(Shell parentShell) {
		super(parentShell);
		// CASE 1) TO VIEW OR EDIT EXISTING DATA SOURCE INFO
		System.out.println("DataSourceKeeper.size() " + DataSourceKeeper.size());
		if (DataSourceKeeper.size() == 0) {
			new GenericMessageBox(parentShell, "No Data Sets",
					"The HT does not contain any DataSources at this time.  Read a CSV or RDF file to create some.");
			cancelPressed();
			return;
		}
		this.callingFileMD = null;
		this.curDataSourceProvider = DataSourceKeeper.get(0);
		runLogger.info("SET META existing DataSource");
	}

	public MetaDataDialog(Shell parentShell, FileMD fileMD) {
		super(parentShell);
		// CASE 2) WITH A NEW FILE TO
		// 2a) CREATE A NEW DATA SOURCE
		// 2b) ADD TO AN EXISTING DATA SOURCE

		assert fileMD != null : "fileMD cannot be null";
		this.callingFileMD = fileMD;
		this.newDataSourceProvider = new DataSourceProvider();
		this.newDataSourceProvider.setDataSourceName(DataSourceKeeper.uniquify(fileMD.getFilename().substring(0,
				fileMD.getFilename().length() - 4)));
		this.newDataSourceProvider.addFileMD(callingFileMD);
		this.newDataSourceProvider.setContactPerson(new Person());
		this.curDataSourceProvider = this.newDataSourceProvider;
		DataSourceKeeper.add(newDataSourceProvider);
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
		Label lbl_section1 = new Label(composite, SWT.LEFT);
		lbl_section1.setFont(SWTResourceManager.getFont("Lucida Grande", 16, SWT.BOLD));
		lbl_section1.setBounds(col1Left, rowIndex * disBtwnRows, col1Width + col2Width, rowHeight);
		lbl_section1.setText("Data Set Information:");

		rowIndex++;
		Label lbl_selectDataSource = new Label(composite, SWT.RIGHT);
		lbl_selectDataSource.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_selectDataSource.setText("Select");

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
		comboDataSourceSelector.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("combo.addSelectionListener.widgetSelectedr=" + e.toString());
				comboSelectionIndex = comboDataSourceSelector.getSelectionIndex();
				populateDataSourceMD();
				runLogger.info("  DATASOURCE SELECTED: " + comboDataSourceSelector.getText());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				System.out.println("combo.addSelectionListener.widgetDefaultSelected=" + e.toString());
				comboSelectionIndex = comboDataSourceSelector.getSelectionIndex();
				populateDataSourceMD();
				runLogger.info("  DATASOURCE SELECTED: " + comboDataSourceSelector.getText());
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
		Label lbl_version = new Label(composite, SWT.RIGHT);
		lbl_version.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_version.setText("Version");
		Text text_version = new Text(composite, SWT.BORDER);
		text_version.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_comments = new Label(composite, SWT.RIGHT);
		lbl_comments.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_comments.setText("Comments");
		Text text_comments = new Text(composite, SWT.BORDER | SWT.WRAP);
		text_comments.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight * 2);

		rowIndex++;
		rowIndex++;
		Label lbl_contactName = new Label(composite, SWT.RIGHT);
		lbl_contactName.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_contactName.setText("Contact Name");
		Text text_contactName = new Text(composite, SWT.BORDER);
		text_contactName.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_contactAffiliation = new Label(composite, SWT.RIGHT);
		lbl_contactAffiliation.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_contactAffiliation.setText("Contact Affiliation");
		Text text_contactAffiliation = new Text(composite, SWT.BORDER);
		text_contactAffiliation.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_contactEmail = new Label(composite, SWT.RIGHT);
		lbl_contactEmail.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_contactEmail.setText("Contact Email");
		Text text_contactEmail = new Text(composite, SWT.BORDER);
		text_contactEmail.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_contactPhone = new Label(composite, SWT.RIGHT);
		lbl_contactPhone.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_contactPhone.setText("Contact Phone");
		Text text_contactPhone = new Text(composite, SWT.BORDER);
		text_contactPhone.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		// NEXT STEP: ADD FileMD DATA
		rowIndex++;
		Label sep_01a = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep_01a.setBounds(50, rowIndex * disBtwnRows - 5, 250, 2);

		Label lbl_section2 = new Label(composite, SWT.LEFT);
		lbl_section2.setFont(SWTResourceManager.getFont("Lucida Grande", 16, SWT.BOLD));
		lbl_section2.setBounds(col1Left, rowIndex * disBtwnRows, col1Width + col2Width, rowHeight);
		lbl_section2.setText("File Information:");

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
		Label lbl_fileSize = new Label(composite, SWT.RIGHT);
		lbl_fileSize.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_fileSize.setText("Size (bytes)");
		Text text_fileSize = new Text(composite, SWT.BORDER);
		text_fileSize.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		text_fileSize.setEditable(false);
		text_fileSize.setBackground(defaultBG);

		rowIndex++;
		Label lbl_lastModified = new Label(composite, SWT.RIGHT);
		lbl_lastModified.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_lastModified.setText("Last Modified");
		Text text_lastModified = new Text(composite, SWT.BORDER);
		text_lastModified.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		text_lastModified.setEditable(false);
		text_lastModified.setBackground(defaultBG);

		rowIndex++;
		Label lbl_fileReadTime = new Label(composite, SWT.RIGHT);
		lbl_fileReadTime.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_fileReadTime.setText("Read Time");
		Text text_fileReadTime = new Text(composite, SWT.BORDER);
		text_fileReadTime.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		text_fileReadTime.setEditable(false);
		text_fileReadTime.setBackground(defaultBG);

		dialogValues.add(text_fileSize);           // 00 File Size (bytes)
		dialogValues.add(text_lastModified);       // 01 File Last Modified
		dialogValues.add(text_fileReadTime);       // 02 File Read Time
		dialogValues.add(text_version);            // 03 Data Set Version
		dialogValues.add(text_comments);           // 04 Data Set Comments
		dialogValues.add(text_contactName);        // 05 Data Set Contact Name
		dialogValues.add(text_contactAffiliation); // 06 Data Set Contact Affiliation
		dialogValues.add(text_contactEmail);       // 07 Data Set Contact Email
		dialogValues.add(text_contactPhone);       // 08 Data Set Contact Phone

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

	public DataSourceProvider getCurDataSourceProvider() {
		return curDataSourceProvider;
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
		Person contactPerson = curDataSourceProvider.getContactPerson();
		// CuratorMD curatorMD = curDataSourceProvider.getCuratorMD();

//		dialogValues.add(text_fileSize);           // 00 File Size (bytes)
//		dialogValues.add(text_lastModified);       // 01 File Last Modified
//		dialogValues.add(text_fileReadTime);       // 02 File Read Time
//		dialogValues.add(text_version);            // 03 Data Set Version
//		dialogValues.add(text_comments);           // 04 Data Set Comments
//		dialogValues.add(text_contactName);        // 05 Data Set Contact Name
//		dialogValues.add(text_contactAffiliation); // 06 Data Set Contact Affiliation
//		dialogValues.add(text_contactEmail);       // 07 Data Set Contact Email
//		dialogValues.add(text_contactPhone);       // 08 Data Set Contact Phone
		
		curDataSourceProvider.setDataSourceName(dataSourceName);
		curDataSourceProvider.setVersion(dialogValues.get(3).getText());
		curDataSourceProvider.setComments(dialogValues.get(4).getText());
		contactPerson.setName(dialogValues.get(5).getText());
		contactPerson.setAffiliation(dialogValues.get(6).getText());
		contactPerson.setEmail(dialogValues.get(7).getText());
		contactPerson.setPhone(dialogValues.get(8).getText());
		runLogger.info("  SET META: name = " + dataSourceName);
		runLogger.info("  SET META: version = " + dialogValues.get(3).getText());
		runLogger.info("  SET META: comments = \"" + Util.escape(dialogValues.get(4).getText()) + "\"");
		runLogger.info("  SET META: contactName = " + contactPerson.getName());
		runLogger.info("  SET META: contactAffiliation = " + contactPerson.getAffiliation());
		runLogger.info("  SET META: contactEmail = " + contactPerson.getEmail());
		runLogger.info("  SET META: contactPhone = " + contactPerson.getPhone());

		if (newDataSourceProvider != null) {
			if (comboSelectionIndex > 0) {
				DataSourceKeeper.remove(newDataSourceProvider);
				curDataSourceProvider.addFileMD(callingFileMD);
				runLogger.info("  SET META: associated file = " + callingFileMD.getPath() + "/"
						+ callingFileMD.getFilename());
			}
		}

		runLogger.info("SET META complete");

		super.okPressed();
	}

	private void renameDataSource() {

		GenericStringBox genericStringBox = new GenericStringBox(getShell(), comboDataSourceSelector.getText(),
				comboDataSourceSelector.getItems());

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
			new GenericMessageBox(getParentShell(), "Duplicate Name",
					"Data Set names must be onePerParentGroup.  Please choose a new name.");
			return;
		}
		curDataSourceProvider.setDataSourceName(newFileName);
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
		List<String> sortedNames = DataSourceKeeper.getNames();
		String[] results = new String[sortedNames.size()];

		if (newDataSourceProvider == null) {
			for (int i = 0; i < sortedNames.size(); i++) {
				results[i] = sortedNames.get(i);
			}
			curDataSourceProvider = DataSourceKeeper.get(DataSourceKeeper.indexOfDataSourceName(results[0]));
		} else {
			results[0] = newDataSourceProvider.getDataSourceName();
			boolean seen = false;
			for (int i = 0; i < sortedNames.size(); i++) {
				if (sortedNames.get(i).equals(results[0])) {
					seen = true;
				} else if (seen) {
					results[i] = sortedNames.get(i);
				} else {
					results[i + 1] = sortedNames.get(i);
				}
			}
			curDataSourceProvider = newDataSourceProvider;
		}
		return results;
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
		dialogValues.get(3).setText(curDataSourceProvider.getVersion());
		dialogValues.get(4).setText(curDataSourceProvider.getComments());
		
		Person contactPerson = curDataSourceProvider.getContactPerson();
		if (contactPerson != null) {
			dialogValues.get(5).setText(contactPerson.getName());
			dialogValues.get(6).setText(contactPerson.getAffiliation());
			dialogValues.get(7).setText(contactPerson.getEmail());
			dialogValues.get(8).setText(contactPerson.getPhone());
		}
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
			dialogValues.get(0).setText(curFileMD.getByteCount() + "");
			if (curFileMD.getModifiedDate() != null) {
				dialogValues.get(1).setText(Util.getLocalDateFmt(curFileMD.getModifiedDate()));
			}
			if (curFileMD.getReadDate() != null) {
				dialogValues.get(2).setText(Util.getLocalDateFmt(curFileMD.getReadDate()));
			}
		}
	}

	// protected void redrawDialogCuratorMD() {
	// CuratorMD curatorMD = curDataSourceProvider.getCuratorMD();
	// System.out.println("curatorMD.getName: = " + curatorMD.getName());
	// dialogValues.get(9).setText(curatorMD.getName());
	// dialogValues.get(10).setText(curatorMD.getAffiliation());
	// dialogValues.get(11).setText(curatorMD.getEmail());
	// dialogValues.get(12).setText(curatorMD.getPhone());
	// }

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

		// redrawDialogCuratorMD();
	}

	protected void createComboFileSelectorList() {
		comboFileSelector.removeAll();
		// if (curDataSourceProvider == null) {
		// if (callingFileMD != null) {
		// comboFileSelector.add(callingFileMD.getFilename());
		// }
		// return;
		// }
		List<FileMD> fileMDList = curDataSourceProvider.getFileMDList();
		int selectionIndex = 0;
		// if (callingFileMD != null) {
		// comboFileSelector.add(callingFileMD.getFilename());
		// }
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
			System.out.println("choice is " + comboFileSelector.getSelectionIndex() + " with value: "
					+ comboFileSelector.getText());
		}

	}
}
