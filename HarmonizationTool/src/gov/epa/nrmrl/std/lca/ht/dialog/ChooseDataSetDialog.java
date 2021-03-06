package gov.epa.nrmrl.std.lca.ht.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;

import org.apache.jena.riot.out.OutputPolicy;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

	static String defaultPrompt = "Please choose a dataset to export:";

	String prompt;

	int height = 200;

	public static final int OUTPUT_FORMAT_ZIP = 0;
	public static final int OUTPUT_FORMAT_JSON = 1;
	public static final int OUTPUT_FORMAT_CSV = 2;

	private static boolean DEFAULT_FILTER_MASTER_DATASETS = true;

	private static Set<String> masterNames = new HashSet<String>();

	static {
		masterNames.add("Master Contexts ");
		masterNames.add("Master Properties ");
		masterNames.add("Master Flowables ");
	}

	public ChooseDataSetDialog(Shell parentShell) {
		this(parentShell, DEFAULT_FILTER_MASTER_DATASETS, defaultPrompt, true);
	}

	public ChooseDataSetDialog(Shell parentShell, boolean askFormat) {
		this(parentShell, DEFAULT_FILTER_MASTER_DATASETS, defaultPrompt, askFormat);
	}

	public ChooseDataSetDialog(Shell parentShell, String message) {
		this(parentShell, DEFAULT_FILTER_MASTER_DATASETS, message, false);
	}

	/**
	 * Create the dialog.
	 * @param parentShell
	 * @wbp.parser.constructor
	 */
	public ChooseDataSetDialog(Shell parentShell, boolean filterMasterDataSets, String message, boolean askFormat) {
		super(parentShell);
		prompt = message;
		filterMasters = filterMasterDataSets;
		askFileFormat = askFormat;
		if (askFileFormat)
			height = 250;
	}

	private Combo dataSetCombo;
	private Map<Integer, String> dsPositionMap = new HashMap<Integer, String>();
	private Combo outputFormatCombo;
	private Map<Integer, Integer> outputFormatMap = new HashMap<Integer, Integer>();
	String storageLocation;
	Label dialogLabel;
	boolean showPrefs = false;
	String selection = null;
	int format = 1;
	boolean noDataSets = false;
	boolean askFileFormat = false;
	boolean zippedJson = false;

	protected Control createDialogArea(Composite parent) {

		// setTitle("Choose Storage Location");
		// setMessage("The Harmonization Tool (HT) requires the user to specify directories for local storage.  Please choose a location to store its data.");

		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(gd);

		/* Web */
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;

		dialogLabel = new Label(container, SWT.CHECK);

		dialogLabel.setText(prompt);
		dialogLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		new Label(container, SWT.NONE);
		dataSetCombo = new Combo(container, SWT.READ_ONLY);

		gd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd.grabExcessHorizontalSpace = true;
		dataSetCombo.setLayoutData(gd);

		List<String> names = DataSourceKeeper.getAlphabetizedNameList();
		if (filterMasters) {
			List<String> filteredNames = new ArrayList<String>();
			for (String name : names) {
				boolean masterFound = false;
				for (String master : masterNames) {
					if (name.startsWith(master)) {
						masterFound = true;
						break;
					}

				}
				if (!masterFound)
					filteredNames.add(name);
			}
			names = filteredNames;
			for (int i = 0; i < names.size(); ++i)
				dsPositionMap.put(i, names.get(i));
		}

		if (names.isEmpty()) {
			dialogLabel.setText("No data sets found");
			noDataSets = true;
			// combo.setEnabled(false);
		}

		dataSetCombo.setItems(names.toArray(new String[0]));

		String key = CSVTableView.getTableProviderKey();
		if (key != null) {
			String curDataSourceProviderName = TableKeeper.getTableProvider(key).getDataSourceProvider()
					.getDataSourceName();
			DataSourceProvider dataSourceProvider = DataSourceKeeper.getByName(curDataSourceProviderName);
			if (dataSourceProvider.containsOLCAData()) {
				zippedJson = true;
			} else {
				zippedJson = false;
			}
			int index = dataSetCombo.indexOf(curDataSourceProviderName);
			String text = dataSetCombo.getItem(index) + " (Current)";
			dataSetCombo.setItem(index, text);
			dataSetCombo.select(index);
		} else {
			List<String> dataSources = DataSourceKeeper.getDataSourceNamesInTDB();
			String lastDs = dataSources.get(dataSources.size() - 1);
			String[] items = dataSetCombo.getItems();
			for (int i = 0; i < items.length; ++i) {
				if (items[i].equals(lastDs)) {
					dataSetCombo.select(i);
					break;
				}
			}
			DataSourceProvider dataSourceProvider = DataSourceKeeper.getByName(dataSetCombo.getSelection().toString());
			if (dataSourceProvider != null && dataSourceProvider.containsOLCAData()) {
				zippedJson = true;
			} else {
				zippedJson = false;
			}
		}
		dataSetCombo.addSelectionListener(new SelectionListener() {
			private void doit(SelectionEvent e) {
				String datasetName = dsPositionMap.get(dataSetCombo.getSelectionIndex());
				DataSourceProvider dataSourceProvider = DataSourceKeeper.getByName(datasetName);
				if (dataSourceProvider.containsOLCAData()) {
					zippedJson = true;
					updateOutputFormatCombo();
				} else {
					zippedJson = false;
					updateOutputFormatCombo();
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
		});

		new Label(container, SWT.NONE);
		// new Label(container, SWT.NONE);
		// new Label(container, SWT.NONE);

		if (askFileFormat) {
			Label outputFormatLabel = new Label(container, SWT.CHECK);

			outputFormatLabel.setText("Please choose an output format:");
			outputFormatLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

			new Label(container, SWT.NONE);
			outputFormatCombo = new Combo(container, SWT.READ_ONLY);

			gd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd.grabExcessHorizontalSpace = true;
			dataSetCombo.setLayoutData(gd);

			updateOutputFormatCombo();

			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);

		}

		return area;
	}

	private void updateOutputFormatCombo() {
		if (!askFileFormat) {
			return;
		}
		List<String> formats = new ArrayList<String>();
		outputFormatMap.clear();
		// TODO - TAHOWARD - I changed this so that the right options are presented, but the index will be wrong on the
		// other end
		if (zippedJson) {
			formats.add("Zipped .json for OpenLCA (.zip)");
			formats.add("Structured data in a single file (.json, .jsonld, .ttl)");
			outputFormatMap.put(0, OUTPUT_FORMAT_ZIP);
			outputFormatMap.put(1, OUTPUT_FORMAT_JSON);
			outputFormatCombo.setItems(formats.toArray(new String[0]));
		} else {
			formats.add("Tab-delimited text file (.csv)");
			outputFormatMap.put(0, OUTPUT_FORMAT_CSV);
			outputFormatCombo.setItems(formats.toArray(new String[0]));
		}
		outputFormatCombo.select(0);
		outputFormatCombo.redraw();
		outputFormatCombo.pack();
	}

	protected Control createContents(Composite parent) {
		Control ret = super.createContents(parent);
		// OK Button doesn't exist until after super.createContents returns
		if (noDataSets)
			this.getButton(IDialogConstants.OK_ID).setEnabled(false);
		return ret;
	}

	protected void okPressed() {
		selection = dsPositionMap.get(dataSetCombo.getSelectionIndex());
		if (askFileFormat) {
			format = outputFormatMap.get(outputFormatCombo.getSelectionIndex());
		}
		System.out.println("Setting selection = " + selection);
		super.okPressed();
	}

	public String getSelection() {
		return selection;
	}

	public int getFormat() {
		return format;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, height);
	}

}
