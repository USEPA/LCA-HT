package harmonizationtool.dialog;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MyDialog extends TitleAreaDialog {

	private Text dataSourceLidText;
	private Text dataSourceNameText;
	private Text majorVersionText;
	private Text minorVersionText;
	private Text commentText;
	private String dataSourceLid;
	private String dataSourceName;
	private String majorVersion;
	private String minorVersion;
	private String comment;

	public MyDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();
		// Set the title
		setTitle("Export to TDB");
		// Set the message
		setMessage("Set Metadata", IMessageProvider.INFORMATION);

	}

	@Override
	protected Control createDialogArea(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		// layout.horizontalAlignment = GridData.FILL;
		parent.setLayout(layout);

		// The text fields will grow with the size of the dialog
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;

		Label label0 = new Label(parent, SWT.NONE);
		label0.setText("Data Source Local ID");
		dataSourceLidText = new Text(parent, SWT.BORDER);
		dataSourceLidText.setLayoutData(gridData);
		dataSourceLidText.setText("(new)");
		dataSourceLidText.setEnabled(true);
		label0.setEnabled(true);
		
//		dataSourceLidText.setEnabled(false);

		Label label1 = new Label(parent, SWT.NONE);
		label1.setText("Data Source Name");
		dataSourceNameText = new Text(parent, SWT.BORDER);
		dataSourceNameText.setLayoutData(gridData);

		Label label2 = new Label(parent, SWT.NONE);
		label2.setText("Major Version");
		// You should not re-use GridData
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		majorVersionText = new Text(parent, SWT.BORDER);
		majorVersionText.setLayoutData(gridData);

		Label label3 = new Label(parent, SWT.NONE);
		label3.setText("Minor Version");
		// You should not re-use GridData
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		minorVersionText = new Text(parent, SWT.BORDER);
		minorVersionText.setLayoutData(gridData);

		Label label4 = new Label(parent, SWT.NONE);
		label4.setText("Comment");
		// You should not re-use GridData
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		commentText = new Text(parent, SWT.BORDER);
		commentText.setLayoutData(gridData);
		return parent;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 3;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.CENTER;

		parent.setLayoutData(gridData);
		// Create Add button
		// Own method as we need to overview the SelectionAdapter
		createOkButton(parent, OK, "OK", true);
		// Add a SelectionListener

		// Create Cancel button
		Button cancelButton = createButton(parent, CANCEL, "Cancel", false);
		// Add a SelectionListener
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(CANCEL);
				close();
			}
		});
	}

	protected Button createOkButton(Composite parent, int id, String label, boolean defaultButton) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.setFont(JFaceResources.getDialogFont());
		button.setData(new Integer(id));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (isValidInput()) {
					okPressed();
				}
			}
		});
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
		}
		setButtonLayoutData(button);
		return button;
	}

	private boolean isValidInput() {
		boolean valid = true;
		if (dataSourceLidText.getText().length() == 0) {
			setErrorMessage("Please set data source IRI");
			valid = false;
		}
		if (dataSourceNameText.getText().length() == 0) {
			setErrorMessage("Please set data source name");
			valid = false;
		}
		if (majorVersionText.getText().length() == 0) {
			setErrorMessage("Please set major version #");
			valid = false;
		}
		if (minorVersionText.getText().length() == 0) {
			setErrorMessage("Please set minor version #");
			valid = false;
		}
		if (commentText.getText().length() == 0) {
			setErrorMessage("Please set comment");
			valid = false;
		}
		return valid;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	// Coyy textFields because the UI gets disposed
	// and the Text Fields are not accessible any more.
	private void saveInput() {
		dataSourceLid = dataSourceLidText.getText();
		dataSourceName = dataSourceNameText.getText();
		majorVersion = majorVersionText.getText();
		minorVersion = minorVersionText.getText();
		comment = commentText.getText();
	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

	public String getDataSourceLid() {
		return dataSourceLid;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public String getMajorVersion() {
		return majorVersion;
	}

	public String getMinorVersion() {
		return minorVersion;
	}

	public String getComment() {
		return comment;
	}

}
