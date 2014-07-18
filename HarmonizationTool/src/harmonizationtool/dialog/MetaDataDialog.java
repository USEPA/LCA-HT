package harmonizationtool.dialog;

import java.util.List;

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
import org.eclipse.swt.widgets.Button;
import org.eclipse.wb.swt.SWTResourceManager;

public class MetaDataDialog extends TitleAreaDialog {

	private DataSourceProvider curDataSourceProvider = null;
	private DataSourceProvider newDataSourceProvider = null;
	private FileMD callingFileMD = null;
	private Combo comboSelectorDataSource;
//	private Button dataSourceRename;
	private Combo comboSelectorFileMD;
	// private List<Text> dialogValues = new ArrayList<Text>();
	private Text[] dialogValues = new Text[9];

	// private Color red = new Color(Display.getCurrent(), 255, 0, 0);
	private Color defaultBG = null;

	private Logger runLogger = Logger.getLogger("run");

	// private ComboFileSelectorListener comboFileSelectorListener;
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
		this.curDataSourceProvider = this.newDataSourceProvider;
		DataSourceKeeper.add(newDataSourceProvider);
		runLogger.info("SET META start - new file");
	}

	// MAKE AND "WIRE" THE WHOLE DIALOG BOX
	@Override
	protected Control createDialogArea(Composite parent) {
		System.out.println("createDialogArea called");

		layoutDialog(parent); // BUILD "BASIC STRUCTURE"

		comboSelectorDataSource.setItems(DataSourceKeeper.getAlphabetizedNames());
		int index = DataSourceKeeper.indexOf(newDataSourceProvider);
		if (index >= 0) {
			comboSelectorDataSource.select(index);
		} else {
			comboSelectorDataSource.select(comboSelectorDataSource.indexOf(curDataSourceProvider.getDataSourceName()));
		}
		comboSelectorDataSource.addSelectionListener(new ComboSelectorDataSourceListener());
		comboSelectorFileMD.setToolTipText("Files associated with this data set." + comboSelectorDataSource.getText());
		comboSelectorFileMD.addModifyListener(new ComboFileSelectorListener());
		redrawDialogRows();

		Control control = super.createDialogArea(parent);
		control.setToolTipText("");
		return control;
	}

	public DataSourceProvider getCurDataSourceProvider() {
		return curDataSourceProvider;
	}

	private void layoutDialog(Composite parent) {
		setTitle("CSV file Meta Data");

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
		labelSelectDataSource.setText("Select");

		comboSelectorDataSource = new Combo(composite, SWT.READ_ONLY);
		comboSelectorDataSource.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		Button dataSourceRename = new Button(composite, SWT.BORDER);
		dataSourceRename.setToolTipText("Click to rename this data set.");
		dataSourceRename.setBounds(col2Left + 250, rowIndex * disBtwnRows - 1, 80, 25);
		dataSourceRename.setText("Rename");
		dataSourceRename.addListener(SWT.Selection, new RenameButtonClickListener());

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

	protected void redrawDialogRows() {
		clearDialogRows();
		redrawDialogDataSourceMD();

		createComboSelectorFileMD();
	}

	protected void clearDialogRows() {
		for (Text dialogValue : dialogValues) {
			dialogValue.setText("");
		}
	}

	protected void redrawDialogDataSourceMD() {
		dialogValues[0].setText(curDataSourceProvider.getVersion());
		dialogValues[1].setText(curDataSourceProvider.getComments());

		Person contactPerson = curDataSourceProvider.getContactPerson();
		if (contactPerson == null) {
			contactPerson = new Person();
			curDataSourceProvider.setContactPerson(contactPerson);
			dialogValues[2].setText("");
			dialogValues[3].setText("");
			dialogValues[4].setText("");
			dialogValues[5].setText("");
		} else {
			dialogValues[2].setText(contactPerson.getName());
			dialogValues[3].setText(contactPerson.getAffiliation());
			dialogValues[4].setText(contactPerson.getEmail());
			dialogValues[5].setText(contactPerson.getPhone());
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
		int selectionIndex = 0;
		for (int i = 0; i < fileMDList.size(); i++) {
			FileMD fileMD = fileMDList.get(i);
			comboSelectorFileMD.add(fileMD.getFilename());
			if (callingFileMD == fileMD) {
				selectionIndex = i;
			}
		}
		comboSelectorFileMD.select(selectionIndex);
		redrawDialogFileMD();
	}

	protected void redrawDialogFileMD() {
		FileMD curFileMD = curDataSourceProvider.getFileMDListNewestFirst()
				.get(comboSelectorFileMD.getSelectionIndex());
		dialogValues[6].setText("" + curFileMD.getByteCount());
		dialogValues[7].setText(Util.getLocalDateFmt(curFileMD.getModifiedDate()));
		dialogValues[8].setText(Util.getLocalDateFmt(curFileMD.getReadDate()));
	}

	@Override
	protected void cancelPressed() {

		super.cancelPressed();
		runLogger.info("SET META cancel");
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
		runLogger.info("  SET META: version = " + curDataSourceProvider.getVersion());
		runLogger.info("  SET META: comments = \"" + Util.escape(curDataSourceProvider.getComments()) + "\"");
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

	private final class ComboSelectorDataSourceListener implements SelectionListener {
		private void doit(SelectionEvent e) {
			System.out.println("combo.addSelectionListener.widgetSelectedr=" + e.toString());
			comboSelectionIndex = comboSelectorDataSource.getSelectionIndex();
			curDataSourceProvider = DataSourceKeeper.getByName(comboSelectorDataSource.getText());
			redrawDialogRows();
			// populateDataSourceMD();
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

	public class ComboFileSelectorListener implements ModifyListener {

		@Override
		public void modifyText(ModifyEvent e) {
			System.out.println("ModifyEvent=" + e.toString());
			System.out.println("fileMDCombo index " + comboSelectorFileMD.getSelectionIndex());
			redrawDialogFileMD();
			System.out.println("choice is " + comboSelectorFileMD.getSelectionIndex() + " with value: "
					+ comboSelectorFileMD.getText());
		}
	}
}
