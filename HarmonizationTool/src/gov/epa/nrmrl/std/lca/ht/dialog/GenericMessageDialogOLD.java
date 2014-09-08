package gov.epa.nrmrl.std.lca.ht.dialog;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

//public class GenericMessageDialog extends TitleAreaDialog {
	public class GenericMessageDialogOLD extends MessageDialog{
	private String message;
	private String title;
	private Text messageText;

	public GenericMessageDialogOLD(Shell parentShell, String title, String message) {
//		super(parentShell);
		super(parentShell, "My title", null, "My question",CANCEL, null, 0);
		this.title = title;
		this.message = message;
//		MessageDialog dg = new MessageDialog(
//				window.getShell(),
//				"My title",
//				null,
//				"My question",
//				MessageDialog.QUESTION_WITH_CANCEL, 
//				new String[]{
//					IDialogConstants.YES_LABEL, 
//					IDialogConstants.NO_LABEL, 
//					IDialogConstants.CANCEL_LABEL},
//				0
//				);
		
	}

	@Override
	public void create() {
		super.create();
		// Set the title
//		setTitle(title);
		// Set the message
//		 setMessage(message, IMessageProvider.WARNING);

	}

	@Override
	protected Control createDialogArea(Composite parent) {
		   Composite area = (Composite) super.createDialogArea(parent);
	        Composite container = new Composite(area, SWT.NONE);
	        container.setLayoutData(new GridData(GridData.FILL_BOTH));
	        
	        
	        Text messageText = new Text(container, SWT.MULTI | SWT.WRAP);
//	        Label lblNewLabel = new Label(container, SWT.NONE);
	        messageText.setBounds(0, 0, 521, 51);
//	        messageText.set
//	        lblNewLabel.setText("My New Label");
	        messageText.setText(message);
//	        messageText.set

	        return area;
		
		
//		GridLayout layout = new GridLayout();
//		layout.numColumns = 1;
//		// layout.horizontalAlignment = GridData.FILL;
//		parent.setLayout(layout);
//
//		// The text fields will grow with the size of the dialog
//		GridData gridData = new GridData();
//		gridData.grabExcessHorizontalSpace = true;
//		gridData.horizontalAlignment = GridData.FILL;
//
////		Label label0 = new Label(parent, SWT.NONE);
////		label0.setText("Data Source Local ID");
////		label0.setEnabled(true);
////		// You should not re-use GridData
////		gridData = new GridData();
////		gridData.grabExcessHorizontalSpace = true;
////		gridData.horizontalAlignment = GridData.FILL;
////		// You should not re-use GridData
////		gridData = new GridData();
////		gridData.grabExcessHorizontalSpace = true;
////		gridData.horizontalAlignment = GridData.FILL;
////		// You should not re-use GridData
////		gridData = new GridData();
////		gridData.grabExcessHorizontalSpace = true;
////		gridData.horizontalAlignment = GridData.FILL;
//		return parent;
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

	@Override
	protected Point getInitialSize() {
		return super.getInitialSize();
	}
}
