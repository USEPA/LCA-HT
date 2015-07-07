package gov.epa.nrmrl.std.lca.ht.dialog;

import java.util.List;

import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.FileMD;
import gov.epa.nrmrl.std.lca.ht.dataModels.Person;
import gov.epa.nrmrl.std.lca.ht.utils.Temporal;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.wb.swt.SWTResourceManager;

import com.hp.hpl.jena.rdf.model.Resource;

public class MetaDataDialog extends TitleAreaDialog {

	private DataSourceProvider curDataSourceProvider = null;
	private DataSourceProvider newDataSourceProvider = null;
	private FileMD callingFileMD = null;
	private Combo comboSelectorDataSource;
	private Combo comboSelectorFileMD;
	private Text[] dialogValues = new Text[9];
	// private Integer referenceDataStatus = null;

	// private Color red = new Color(Display.getCurrent(), 255, 0, 0);
	private Color defaultBG = SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND);
	private Logger runLogger = Logger.getLogger("run");
	private Button masterButton;
	private Button supplementaryButton;

	// protected String combDataSourceSelectorSavedText = "";

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
			// THIS IS A PROBLEM. FIXME - HOW TO EXIT GRACEFULLY?
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
		this.curDataSourceProvider = new DataSourceProvider();
		this.curDataSourceProvider.setDataSourceName(DataSourceKeeper.uniquify(fileMD.getFilenameString().substring(0,
				fileMD.getFilenameString().length() - 4)));
		this.curDataSourceProvider.addFileMD(this.callingFileMD);
		this.newDataSourceProvider = curDataSourceProvider;
		runLogger.info("SET META start - new file");
	}

	public MetaDataDialog(Shell parentShell, FileMD fileMD, String newProposedName) {
		super(parentShell);
		// CASE 2) WITH A NEW FILE TO
		// 2a) CREATE A NEW DATA SOURCE
		// 2b) ADD TO AN EXISTING DATA SOURCE

		assert fileMD != null : "fileMD cannot be null";
		this.callingFileMD = fileMD;
		this.curDataSourceProvider = new DataSourceProvider();
		this.curDataSourceProvider.setDataSourceName(DataSourceKeeper.uniquify(newProposedName));
		this.curDataSourceProvider.addFileMD(this.callingFileMD);
		this.newDataSourceProvider = curDataSourceProvider;
		runLogger.info("SET META start - new file");
	}

	public MetaDataDialog(Shell parentShell, DataSourceProvider dataSourceProvider) {
		super(parentShell);
		assert dataSourceProvider != null : "dataSourceProvider cannot be null";
		this.curDataSourceProvider = dataSourceProvider;
		this.newDataSourceProvider = curDataSourceProvider;
		runLogger.info("SET META start - new file");
	}

	// MAKE AND "WIRE" THE WHOLE DIALOG BOX
	@Override
	protected Control createDialogArea(Composite parent) {
		System.out.println("createDialogArea called");

		layoutDialog(parent); // BUILD "BASIC STRUCTURE"

		comboSelectorDataSource.setItems(DataSourceKeeper.getAlphabetizedNames());
		String curDataSourceProviderName = curDataSourceProvider.getDataSourceName();
		int index = comboSelectorDataSource.indexOf(curDataSourceProviderName);
		comboSelectorDataSource.select(index);
		comboSelectorDataSource.addSelectionListener(new ComboSelectorDataSourceListener());
		comboSelectorFileMD.setToolTipText("Files associated with this data set.");
		comboSelectorFileMD.addModifyListener(new ComboSelectorFileMDListener());
		redrawDialogRows();
		createComboSelectorFileMD();

		Control control = super.createDialogArea(parent);
		control.setToolTipText("");
		return control;
	}

	public DataSourceProvider getCurDataSourceProvider() {
		return curDataSourceProvider;
	}

	private void layoutDialog(Composite parent) {
		setTitle("View / Edit Dataset Meta Data");

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
		Label label_section1 = new Label(composite, SWT.LEFT);
		label_section1.setFont(SWTResourceManager.getFont("Lucida Grande", 16, SWT.BOLD));
		label_section1.setBounds(col1Left, rowIndex * disBtwnRows, col1Width + col2Width, rowHeight);
		label_section1.setText("Data Set Information:");

		rowIndex++;
		Label labelSelectDataSource = new Label(composite, SWT.RIGHT);
		labelSelectDataSource.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		labelSelectDataSource.setText("Data set name");

		comboSelectorDataSource = new Combo(composite, SWT.READ_ONLY);
		comboSelectorDataSource.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		Button dataSourceRename = new Button(composite, SWT.BORDER);
		dataSourceRename.setToolTipText("Click to rename this data set.");
		dataSourceRename.setBounds(col2Left + 250, rowIndex * disBtwnRows - 2, 70, 25);
		dataSourceRename.setText("Rename");
		dataSourceRename.addListener(SWT.Selection, new RenameButtonClickListener());

		if (callingFileMD == null) {
			Button deleteDataSource = new Button(composite, SWT.NONE);
			deleteDataSource.setToolTipText("Click to delete this data set.");
			deleteDataSource.setBounds(col2Left + 320, rowIndex * disBtwnRows - 4, 32, 28);
			deleteDataSource.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
			deleteDataSource.setText("X");
			deleteDataSource.addSelectionListener(deleteDatasourceListener);
			// deleteDataSource.addListener(SWT.Selection, new DeleteButtonClickListener());
		}

		rowIndex++;
		Label labelReferenceDataStatus = new Label(composite, SWT.RIGHT);
		labelReferenceDataStatus.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		labelReferenceDataStatus.setText("Reference Data");
		masterButton = new Button(composite, SWT.RADIO);
		masterButton.setBounds(col2Left, rowIndex * disBtwnRows, col1Width - 10, rowHeight);
		masterButton.setText("Master List");
		masterButton.addSelectionListener(radioListener);
		supplementaryButton = new Button(composite, SWT.RADIO);
		supplementaryButton.setBounds(col2Left + 110, rowIndex * disBtwnRows, col1Width + 40, rowHeight);
		supplementaryButton.setText("Supplementary List");
		supplementaryButton.addSelectionListener(radioListener);
		if (curDataSourceProvider.getReferenceDataStatus() == null) {
			masterButton.setEnabled(true);
			supplementaryButton.setEnabled(true);
			masterButton.setSelection(false);
			supplementaryButton.setSelection(false);
			masterButton.setEnabled(false);
			supplementaryButton.setEnabled(false);
		} else {
			masterButton.setEnabled(true);
			supplementaryButton.setEnabled(true);
			if (curDataSourceProvider.getReferenceDataStatus() == 2) {
				supplementaryButton.setSelection(true);
				masterButton.setSelection(false);

			} else if (curDataSourceProvider.getReferenceDataStatus() == 1) {
				masterButton.setSelection(true);
				supplementaryButton.setSelection(false);
			}
		}

		rowIndex++;
		Label labelVersion = new Label(composite, SWT.RIGHT);
		labelVersion.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		labelVersion.setText("Version");
		Text textVersion = new Text(composite, SWT.BORDER);
		textVersion.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label labelComments = new Label(composite, SWT.RIGHT);
		labelComments.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		labelComments.setText("Comments");
		Text textComments = new Text(composite, SWT.BORDER | SWT.WRAP);
		textComments.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight * 2);

		rowIndex++;
		rowIndex++;
		Label labelContactName = new Label(composite, SWT.RIGHT);
		labelContactName.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		labelContactName.setText("Contact Name");
		Text textContactName = new Text(composite, SWT.BORDER);
		textContactName.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label labelContactAffiliation = new Label(composite, SWT.RIGHT);
		labelContactAffiliation.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		labelContactAffiliation.setText("Contact Affiliation");
		Text textContactAffiliation = new Text(composite, SWT.BORDER);
		textContactAffiliation.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label labelContactEmail = new Label(composite, SWT.RIGHT);
		labelContactEmail.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		labelContactEmail.setText("Contact Email");
		Text textContactEmail = new Text(composite, SWT.BORDER);
		textContactEmail.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label labelContactPhone = new Label(composite, SWT.RIGHT);
		labelContactPhone.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		labelContactPhone.setText("Contact Phone");
		Text textContactPhone = new Text(composite, SWT.BORDER);
		textContactPhone.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		// NEXT STEP: ADD FileMD DATA
		rowIndex++;
		Label sep_01a = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep_01a.setBounds(50, rowIndex * disBtwnRows - 5, 250, 2);

		Label labelSection2 = new Label(composite, SWT.LEFT);
		labelSection2.setFont(SWTResourceManager.getFont("Lucida Grande", 16, SWT.BOLD));
		labelSection2.setBounds(col1Left, rowIndex * disBtwnRows, col1Width + col2Width, rowHeight);
		labelSection2.setText("File Information:");

		rowIndex++;
		Label labelFileName = new Label(composite, SWT.RIGHT);
		labelFileName.setText("Name");
		labelFileName.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		comboSelectorFileMD = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboSelectorFileMD.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label labelFileSize = new Label(composite, SWT.RIGHT);
		labelFileSize.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		labelFileSize.setText("Size (bytes)");
		Text textFileSize = new Text(composite, SWT.BORDER);
		textFileSize.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		textFileSize.setEditable(false);
		textFileSize.setBackground(defaultBG);

		rowIndex++;
		Label labelLastModified = new Label(composite, SWT.RIGHT);
		labelLastModified.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		labelLastModified.setText("Last Modified");
		Text textLastModified = new Text(composite, SWT.BORDER);
		textLastModified.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		textLastModified.setEditable(false);
		textLastModified.setBackground(defaultBG);

		rowIndex++;
		Label labelFileReadTime = new Label(composite, SWT.RIGHT);
		labelFileReadTime.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		labelFileReadTime.setText("Read Time");
		Text textFileReadTime = new Text(composite, SWT.BORDER);
		textFileReadTime.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		textFileReadTime.setEditable(false);
		textFileReadTime.setBackground(defaultBG);

		dialogValues[0] = textVersion; // ------------ 00 Data Set Version
		dialogValues[1] = textComments; // ----------- 01 Data Set Comments

		dialogValues[2] = textContactName; // -------- 02 Data Set Contact Name
		dialogValues[3] = textContactAffiliation; // - 03 Data Set Contact Affiliation
		dialogValues[4] = textContactEmail; // ------- 04 Data Set Contact Email
		dialogValues[5] = textContactPhone; // ------- 05 Data Set Contact Phone

		dialogValues[6] = textFileSize; // ----------- 06 File Size (bytes)
		dialogValues[7] = textLastModified; // ------- 07 File Last Modified
		dialogValues[8] = textFileReadTime; // ------- 08 File Read Time

	}

	SelectionListener radioListener = new SelectionListener() {
		private void doit(SelectionEvent e) {
			System.out.println(e.getSource());
			Button thing = (Button) e.getSource();
			String thing2 = thing.getText();
			if (thing2.matches(".*Master.*")) {
				curDataSourceProvider.setReferenceDataStatus(1);
			} else if (thing2.matches(".*Supplementary.*")) {
				curDataSourceProvider.setReferenceDataStatus(2);
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

	private SelectionListener deleteDatasourceListener = new SelectionListener() {

		private void doit(SelectionEvent e) {
			String dataSourceToDelete = comboSelectorDataSource.getText();

			MessageDialog messageDialog = new MessageDialog(getShell(), "Confirm", null,
					"Are you sure you wish to remove the label and contents of the data set with the label: "
							+ dataSourceToDelete + "?", MessageDialog.QUESTION, new String[] { "Cancel",
							"Confirm Deletion" }, 0);
			// messageDialog.create();
			if (messageDialog.open() == 1) {
				DataSourceKeeper.remove(DataSourceKeeper.getByName(dataSourceToDelete));
				System.out.println("Deleting " + dataSourceToDelete);
				comboSelectorDataSource.remove(dataSourceToDelete);
				comboSelectorDataSource.select(0);
				runLogger.info("User deleted data set: " + dataSourceToDelete);
			}
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			doit(e);
		};

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			doit(e);
		}
	};

	protected void redrawDialogRows() {
		clearDialogRows();
		redrawDialogDataSourceMD();
		// createComboSelectorFileMD();
	}

	protected void clearDialogRows() {
		for (Text dialogValue : dialogValues) {
			dialogValue.setText("");
		}
	}

	protected void redrawDialogDataSourceMD() {
		dialogValues[0].setText(curDataSourceProvider.getVersionString());
		dialogValues[1].setText(curDataSourceProvider.getCommentsString());

		Person contactPerson = curDataSourceProvider.getContactPerson();
		if (contactPerson == null) {
			contactPerson = new Person();
			curDataSourceProvider.setContactPerson(contactPerson);
			dialogValues[2].setText("");
			dialogValues[3].setText("");
			dialogValues[4].setText("");
			dialogValues[5].setText("");
		} else {
			dialogValues[2].setText(contactPerson.getNameString());
			dialogValues[3].setText(contactPerson.getAffiliationString());
			dialogValues[4].setText(contactPerson.getEmailString());
			dialogValues[5].setText(contactPerson.getPhoneString());
		}
		if (curDataSourceProvider.getReferenceDataStatus() == null) {
			masterButton.setEnabled(false);
			supplementaryButton.setEnabled(false);
			masterButton.setSelection(false);
			supplementaryButton.setSelection(false);
		} else {
			masterButton.setEnabled(true);
			supplementaryButton.setEnabled(true);
			if (curDataSourceProvider.getReferenceDataStatus() == 1) {
				masterButton.setSelection(true);
				supplementaryButton.setSelection(false);
			} else if (curDataSourceProvider.getReferenceDataStatus() == 2){
				supplementaryButton.setSelection(true);
				masterButton.setSelection(false);
			}
		}
	}

	protected void createComboSelectorFileMD() {
		comboSelectorFileMD.removeAll();

		List<FileMD> fileMDList = curDataSourceProvider.getFileMDListNewestFirst();
		if (fileMDList.size() == 0) {
			dialogValues[6].setText("");
			dialogValues[7].setText("");
			dialogValues[8].setText("");
			return;
		}
		for (int i = 0; i < fileMDList.size(); i++) {
			FileMD fileMD = fileMDList.get(i);
			comboSelectorFileMD.add(fileMD.getFilename());
		}
		comboSelectorFileMD.select(0);
		redrawDialogFileMD(0);
	}

	protected void redrawDialogFileMD(int index) {
		List<FileMD> fileMDs = curDataSourceProvider.getFileMDListNewestFirst();
		if (fileMDs.size() == 0) {
			dialogValues[6].setText("");
			dialogValues[7].setText("");
			dialogValues[8].setText("");
			comboSelectorFileMD.select(0);
			return;
		}
		FileMD curFileMD = fileMDs.get(index);
		dialogValues[6].setText("" + curFileMD.getByteCount());
		dialogValues[7].setText(Temporal.getLocalDateFmt(curFileMD.getModifiedDate()));
		dialogValues[8].setText(Temporal.getLocalDateFmt(curFileMD.getReadDate()));
	}

	@Override
	protected void cancelPressed() {
		// super.cancelPressed();
		if (callingFileMD != null) {
			// NEED TO REMOVE THE NEW DATASOURCE
			newDataSourceProvider.remove();
			// curDataSourceProvider = null;
		}
		runLogger.info("SET META cancel");
		super.cancelPressed();
	}

	@Override
	protected void okPressed() {
		String dataSourceName = comboSelectorDataSource.getText();
		System.out.println("comboSelectorDataSource.getText() " + comboSelectorDataSource.getText());
		Person contactPerson = curDataSourceProvider.getContactPerson();
		if (contactPerson == null) {
			contactPerson = new Person();
		}

		curDataSourceProvider.setDataSourceName(dataSourceName);
		curDataSourceProvider.setVersion(dialogValues[0].getText());
		curDataSourceProvider.setComments(dialogValues[1].getText());
		contactPerson.setName(dialogValues[2].getText());
		contactPerson.setAffiliation(dialogValues[3].getText());
		contactPerson.setEmail(dialogValues[4].getText());
		contactPerson.setPhone(dialogValues[5].getText());
		runLogger.info("  SET META: name = " + dataSourceName);
		runLogger.info("  SET META: version = " + curDataSourceProvider.getVersionString());
		runLogger.info("  SET META: comments = \"" + Util.escape(curDataSourceProvider.getCommentsString()) + "\"");
		runLogger.info("  SET META: contactName = " + contactPerson.getNameString());
		runLogger.info("  SET META: contactAffiliation = " + contactPerson.getAffiliationString());
		runLogger.info("  SET META: contactEmail = " + contactPerson.getEmailString());
		runLogger.info("  SET META: contactPhone = " + contactPerson.getPhoneString());
		runLogger.info("SET META complete");

		super.okPressed();
	}

	private final class ComboSelectorDataSourceListener implements SelectionListener {
		private void doit(SelectionEvent e) {
			String newSelectionName = comboSelectorDataSource.getText();
			if (curDataSourceProvider.getDataSourceName().equals(newSelectionName)) {
				return;
			}
			curDataSourceProvider = DataSourceKeeper.getByName(newSelectionName);
			redrawDialogRows();
			createComboSelectorFileMD();
			runLogger.info("  DATASOURCE SELECTED: " + comboSelectorDataSource.getText());
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			doit(e);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			doit(e);
		}
	}

	private final class RenameButtonClickListener implements Listener {
		@Override
		public void handleEvent(Event event) {
			System.out.println("About to new box...");
			GenericStringBox genericStringBox = new GenericStringBox(getShell(), comboSelectorDataSource.getText(),
					comboSelectorDataSource.getItems());
			System.out.println("About to create box...");

			genericStringBox.create("Name Data Set", "Please type a new data set name");
			System.out.println("About to open box...");

			genericStringBox.open();

			System.out.println("Box is open...");

			String newFileName = genericStringBox.getResultString();
			if (newFileName == null) {
				// cancel PRESSED
				return;
			}

			if (comboSelectorDataSource.getText().equals(newFileName)) {
				if (comboSelectorDataSource.getText().equals(newFileName)) {
					// SAME NAME, DO NOTHING
					return;
				}

				if (DataSourceKeeper.indexOfDataSourceName(newFileName) > -1) {
					new GenericMessageBox(getParentShell(), "Duplicate Name",
							"Data Set names must be onePerParentGroup.  Please choose a new name.");
					return;
				}
				curDataSourceProvider.setDataSourceName(newFileName);
				comboSelectorDataSource.setItem(comboSelectorDataSource.getSelectionIndex(), newFileName);
			}
		}
	}

	public class ComboSelectorFileMDListener implements ModifyListener {

		@Override
		public void modifyText(ModifyEvent e) {
			int newIndex = comboSelectorFileMD.getSelectionIndex();
			if (newIndex < 0) {
				newIndex = 0;
			}
			redrawDialogFileMD(newIndex);
		}
	}
}
