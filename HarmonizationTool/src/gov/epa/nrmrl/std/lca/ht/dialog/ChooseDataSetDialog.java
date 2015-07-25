package gov.epa.nrmrl.std.lca.ht.dialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

public class ChooseDataSetDialog extends Dialog {
	
	boolean filterMasters = false;
	
	static String defaultPrompt = "Please choose a data set to export:";
	
	String prompt;
	
	private static Set<String> masterNames = new HashSet<String>();
	
	static {
		masterNames.add("Master Contexts ");
		masterNames.add("Master Properties ");
		masterNames.add("Master Flowables ");
	}
	
	public ChooseDataSetDialog(Shell parentShell) {
		this(parentShell, false, defaultPrompt);
	}

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public ChooseDataSetDialog(Shell parentShell, boolean filterMasterDataSets, String message) {
		super(parentShell);
		prompt = message;
		filterMasters = filterMasterDataSets;
	}
	
	private Combo combo;
	String storageLocation;
	Label dialogLabel;
	boolean showPrefs = false;
	String selection = null;
	boolean noDataSets = false;
	
	protected Control createDialogArea(Composite parent) {
		
		//setTitle("Choose Storage Location");
		//setMessage("The Harmonization Tool (HT) requires the user to specify directories for local storage.  Please choose a location to store its data.");
		
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(gd);

		/*Web*/
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		
		dialogLabel = new Label(container, SWT.CHECK);

		dialogLabel.setText(prompt);
		dialogLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		
		new Label(container, SWT.NONE);
		combo = new Combo(container, SWT.READ_ONLY);
		

		gd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd.grabExcessHorizontalSpace = true;
		combo.setLayoutData(gd);
				
		List<String> names = DataSourceKeeper.getAlphabetizedNameList();
		if (filterMasters) {
			List<String> filteredNames = new ArrayList<String>();
			for (String name: names) {
				boolean masterFound = false;
				for (String master:masterNames) {
					if (name.startsWith(master)) {
						masterFound = true;
						break;
					}
						
				}
				if (!masterFound)
					filteredNames.add(name); 
			}
			names = filteredNames;
		}
		
		if (names.isEmpty()) {
			dialogLabel.setText("No data sets found");
			noDataSets = true;
			//combo.setEnabled(false);
		}
		
		combo.setItems(names.toArray(new String[0]));
		
		String key = CSVTableView.getTableProviderKey();
		if (key != null) {
			String curDataSourceProviderName = TableKeeper.getTableProvider(key).getDataSourceProvider().getDataSourceName();
			int index = combo.indexOf(curDataSourceProviderName);
			String text = combo.getItem(index) + " (Current)";
			combo.setItem(index, text);
			combo.select(index);
		}
		else {
			List<String> dataSources = DataSourceKeeper.getDataSourceNamesInTDB();
			String lastDs = dataSources.get(dataSources.size() - 1);
			String[] items = combo.getItems();
			for (int i = 0; i < items.length; ++i) {
				if (items[i].equals(lastDs)) {
					combo.select(i);
					break;
				}
			}
		}

		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		return area;
	}
	
	protected Control createContents(Composite parent) {
		Control ret = super.createContents(parent);
		//OK Button doesn't exist until after super.createContents returns
		if (noDataSets)
			this.getButton(IDialogConstants.OK_ID).setEnabled(false);
		return ret;
	}
	
	protected void okPressed() {
		selection = combo.getText();
		if (selection.endsWith(" (Current)"))
			selection = selection.substring(0,  selection.length() - " (Current)".length());
		System.out.println("Setting selection = " + selection);
		super.okPressed();
	}
	
	public String getSelection() {
		return selection;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 200);
	}

}
