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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class DialogQueryDataset extends TitleAreaDialog {

	private Combo combo;
	private List list;
	private String primaryDataset = null;
	private String[] refDatasets = null;


	public DialogQueryDataset(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();
		// Set the title
		setTitle("Select datasets to analyze");
		// Set the message
		setMessage("Metadata", IMessageProvider.INFORMATION);

	}
	public String getPrimaryDataset(){
		return primaryDataset;
	}
	public String[] getReferenceDatasets(){
		return refDatasets;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(null);

		combo = new Combo(composite, SWT.NONE);
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("e.item=" + e.item);
			}
		});
		combo.setBounds(174, 25, 133, 22);

		combo.add("ds_001");
		combo.add("ds_002");
		combo.add("ds_003");
		combo.add("ds_004");
		combo.add("ds_005");
		combo.add("ds_006");
		combo.add("ds_007");
		combo.add("ds_008");
		combo.add("ds_009");
		combo.add("ds_010");
		combo.setText("ds_001");

		Label lblPrimaryDataSet = new Label(composite, SWT.RIGHT);
		lblPrimaryDataSet.setBounds(29, 25, 122, 14);
		lblPrimaryDataSet.setText("Primary Data Set:");

		list = new List(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.MULTI);
		list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("List selected");
			}
		});
		list.setBounds(174, 70, 133, 100);
		list.add("ds_001");
		list.add("ds_002");
		list.add("ds_003");
		list.add("ds_004");
		list.add("ds_005");
		list.add("ds_006");
		list.add("ds_007");
		list.add("ds_008");
		list.add("ds_009");
		list.add("ds_010");


		Label lblCompareTo = new Label(composite, SWT.RIGHT);
		lblCompareTo.setBounds(8, 70, 143, 14);
		lblCompareTo.setText("Refernce Data Sets:");

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

	protected Button createOkButton(Composite parent, int id, String label,
			boolean defaultButton) {
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

	@Override
	protected boolean isResizable() {
		return true;
	}

	// Coyy textFields because the UI gets disposed
	// and the Text Fields are not accessible any more.
	private void saveInput() {
		primaryDataset = combo.getText();
		refDatasets = list.getSelection();
	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}


}
