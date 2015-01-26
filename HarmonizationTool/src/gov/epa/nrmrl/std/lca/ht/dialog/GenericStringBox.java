package gov.epa.nrmrl.std.lca.ht.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class GenericStringBox extends TitleAreaDialog {

	// private Combo combo;
	private Text text;
	private String resultString = null;
	private String currentName = null;
//	private Color red = new Color(Display.getCurrent(), 255, 0, 0);
	private Color defaultColor = null;

	private List<String> forbiddenList = new ArrayList<String>();
	private Button okButton;

	// private String[] referenceDataSources = null;

	public GenericStringBox(Shell parentShell) {
		super(parentShell);
	}

	public GenericStringBox(Shell parentShell, String currentName) {
		super(parentShell);
		this.currentName = currentName;
	}

	public GenericStringBox(Shell parentShell, String currentName, String[] items) {
		super(parentShell);
		this.currentName = currentName;
		for (String item : items) {
			if (!item.equals(this.currentName)) {
				this.forbiddenList.add(item);
			}
		}
	}

	public void create(String title, String message) {
		super.create();
		// Set the title
		setTitle(title);
		// Set the message
		setMessage(message, IMessageProvider.INFORMATION);

	}

	public String getResultString() {
		return resultString;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(null);

		text = new Text(composite, SWT.BORDER);
		defaultColor = text.getForeground();
		text.setBounds(0, 0, 200, 20);
		if (currentName != null) {
			text.setText(currentName);
		}
		text.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				System.out.println("e = " + e);
				System.out.println("text.getText() = " + text.getText());
				if (forbiddenList.contains(text.getText())) {
					text.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
					okButton.setEnabled(false);
				} else {
					text.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
					okButton.setEnabled(true);
				}
				resultString = text.getText();

			}
		});
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
		okButton = createOkButton(parent, OK, "OK", true);
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
		return valid;
	}

	// @Override
	// protected boolean isResizable() {
	// return true;
	// }

	@Override
	protected void okPressed() {
		// saveInput();
		super.okPressed();
	}

}
