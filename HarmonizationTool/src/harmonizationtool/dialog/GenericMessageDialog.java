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

public class GenericMessageDialog extends TitleAreaDialog {
	private String message;
	private String title;

	public GenericMessageDialog(Shell parentShell, String title, String message) {
		super(parentShell);
		this.title = title;
		this.message = message;
	}

	@Override
	public void create() {
		super.create();
		// Set the title
		setTitle(title);
		// Set the message
		 setMessage(message, IMessageProvider.WARNING);

	}

	@Override
	protected Control createDialogArea(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		// layout.horizontalAlignment = GridData.FILL;
		parent.setLayout(layout);

		// The text fields will grow with the size of the dialog
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;

//		Label label0 = new Label(parent, SWT.NONE);
//		label0.setText("Data Source Local ID");
//		label0.setEnabled(true);
//		// You should not re-use GridData
//		gridData = new GridData();
//		gridData.grabExcessHorizontalSpace = true;
//		gridData.horizontalAlignment = GridData.FILL;
//		// You should not re-use GridData
//		gridData = new GridData();
//		gridData.grabExcessHorizontalSpace = true;
//		gridData.horizontalAlignment = GridData.FILL;
//		// You should not re-use GridData
//		gridData = new GridData();
//		gridData.grabExcessHorizontalSpace = true;
//		gridData.horizontalAlignment = GridData.FILL;
		return parent;
	}

//	@Override
//	protected void createButtonsForButtonBar(Composite parent) {
//		GridData gridData = new GridData();
//		gridData.verticalAlignment = GridData.FILL;
//		gridData.horizontalSpan = 2;
//		gridData.grabExcessHorizontalSpace = true;
//		gridData.grabExcessVerticalSpace = true;
//		gridData.horizontalAlignment = SWT.CENTER;
//
//		parent.setLayoutData(gridData);
//		Button okButton = createButton(parent, OK, "OK", true);
//		okButton.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				setReturnCode(OK);
//				close();
//			}
//		});
//	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void okPressed() {
		// saveInput();
		super.okPressed();
	}

//	public String getDataSourceLid() {
//		return dataSourceLid;
//	}
//
//	public String getDataSourceName() {
//		return dataSourceName;
//	}
//
//	public String getMajorVersion() {
//		return majorVersion;
//	}
//
//	public String getMinorVersion() {
//		return minorVersion;
//	}
//
//	public String getComment() {
//		return comment;
//	}

}
