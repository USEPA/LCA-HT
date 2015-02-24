package gov.epa.nrmrl.std.lca.ht.dialog;

import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.sparql.GenericQuery;
import gov.epa.nrmrl.std.lca.ht.sparql.QueryResults;
import gov.epa.nrmrl.std.lca.ht.unused.QListDataSources;

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


public class DialogQueryDataSource extends TitleAreaDialog {
	private Combo combo;
	private List list;
	private String primaryDataSource = null;
	private String[] referenceDataSources = null;

	public DialogQueryDataSource(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();
		// Set the title
		setTitle("Select a DataSource");
		// Set the message
		setMessage("Metadata", IMessageProvider.INFORMATION);

	}
	public String getPrimaryDataSource(){
		return primaryDataSource;
	}
	public String[] getReferenceDataSources(){
		return referenceDataSources;
	}

	private QListDataSources qDataSources = new QListDataSources();
	
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
//		combo.setText("ds_001");
		
		list = new List(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.MULTI);
		list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("List selected");
			}
		});
		list.setBounds(174, 70, 133, 100);
		Label lblPrimaryDataSource = new Label(composite, SWT.RIGHT);
		lblPrimaryDataSource.setBounds(29, 25, 122, 14);
		lblPrimaryDataSource.setText("Primary Data Set:");
		
		GenericQuery iGenericQuery = new GenericQuery(
				qDataSources.getQuery(), "Internal Query");
		iGenericQuery.getData();
		QueryResults parts = iGenericQuery.getQueryResults();
		java.util.List<DataRow> resultRow = parts.getTableProvider().getData();
		for (int i=0;i < resultRow.size();i++){
			DataRow row = resultRow.get(i);
			java.util.List<String> valueList = row.getColumnValues();
		    String dataSource="";
			for(int j = 0; j<valueList.size();j++){
				dataSource = dataSource + valueList.get(j);
			}
			combo.add(dataSource);
			list.add(dataSource);
		}

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
		primaryDataSource = combo.getText();
		referenceDataSources = list.getSelection();
	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}


}
