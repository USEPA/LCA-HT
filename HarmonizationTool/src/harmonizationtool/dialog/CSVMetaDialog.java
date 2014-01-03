package harmonizationtool.dialog;

//import java.awt.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import harmonizationtool.ViewData;
import harmonizationtool.comands.SelectTDB;
import harmonizationtool.handler.ImportCSV;
import harmonizationtool.model.CuratorMD;
import harmonizationtool.model.DataSetKeeper;
import harmonizationtool.model.DataSetMD;
import harmonizationtool.model.DataSetProvider;
import harmonizationtool.model.FileMD;
import harmonizationtool.model.ModelProvider;
import harmonizationtool.utils.ResourceIdMgr;
import harmonizationtool.utils.Util;
import harmonizationtool.vocabulary.ECO;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.DCTerms;

public class CSVMetaDialog extends TitleAreaDialog {

	public ModelProvider modelProvider = null;
	private DataSetProvider dataSetProvider = null;
	private FileMD fileMD = null;
	private	DataSetMD dataSetMD = null;
	private	CuratorMD curatorMD = null;
	private	Resource tdbResource = null;
	private Combo combo = null;

	public Map<String, String> metaData = new LinkedHashMap<String, String>();

	public void setMetaData(Map<String, String> map) {
		metaData = map;
	}

	@SuppressWarnings("null")
	private String[] getDataSetInfo() {
		Model model = SelectTDB.model;
		if (dataSetProvider != null) {
			String[] results = new String[1];
			Integer id = DataSetKeeper.indexOf(dataSetProvider);
			Resource tdbResource = dataSetProvider.getTdbResource();
			String name = "";
			String version = "";
			if (model.contains(tdbResource, RDFS.label)) {
				name = model.listObjectsOfProperty(tdbResource, RDFS.label)
						.next().asLiteral().getString();
			}
			if (model.contains(tdbResource, DCTerms.hasVersion)) {
				version = model
						.listObjectsOfProperty(tdbResource, DCTerms.hasVersion)
						.next().asLiteral().getString();
			} else if (model.contains(tdbResource,ECO.hasMajorVersionNumber)) {
				version = model.listObjectsOfProperty(tdbResource, ECO.hasMajorVersionNumber).next()
						.asLiteral().getString();
				if (model.contains(tdbResource, ECO.hasMinorVersionNumber)) {
					version += "."+model.listObjectsOfProperty(tdbResource, ECO.hasMinorVersionNumber)
							.next().asLiteral().getString();
				}
			}
			results[0] = id + ": " + name + " " + version;
			return results;
		} else {
			String[] results = new String[DataSetKeeper.size()+1];
//			results[0] = ""; // RESERVING THIS FOR THE FIRST ENTRY (DEFAULT =
//								// NEW)
			List<Integer> ids = DataSetKeeper.getIDs();
			Iterator<Integer> iterator = ids.iterator();

			Integer id = 0;
			int counter = 0;
			while (iterator.hasNext()) {
				counter++;
				id = iterator.next();
				DataSetProvider dsProvider = DataSetKeeper.get(id);
				Resource tdbResource = dsProvider.getTdbResource();
				String name = "";
				String version = "";
				if (model.contains(tdbResource, RDFS.label)) {
					name = model.listObjectsOfProperty(tdbResource, RDFS.label)
							.next().asLiteral().getString();
				}
				if (model.contains(tdbResource, DCTerms.hasVersion)) {
					version = model
							.listObjectsOfProperty(tdbResource,
									DCTerms.hasVersion).next().asLiteral()
							.getString();
				} else if (model.contains(tdbResource,ECO.hasMajorVersionNumber)) {
					version = model.listObjectsOfProperty(tdbResource, ECO.hasMajorVersionNumber).next()
							.asLiteral().getString();
					if (model.contains(tdbResource, ECO.hasMinorVersionNumber)) {
						version += "."+model.listObjectsOfProperty(tdbResource, ECO.hasMinorVersionNumber)
								.next().asLiteral().getString();					}
				}
				results[counter] = id + ":" + name + " " + version;
			}
			Integer next = id + 1;
			results[0] = next + ": (new data set)";
			return results;
		}
	}

	private List<Text> dialogValues = new ArrayList<Text>();
//	private boolean initialCreate;
	private Text text;

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("CSV file Meta Data");
		if (fileMD == null) {
			if (dataSetProvider == null) {
				return null; // HOW DID WE GET HERE WITH NEITHER?
			}
			fileMD = dataSetProvider.getFileMDList().get(0); // FIRST FILE...
																// BUT THIS IS
																// NOT RIGHT, SO
																// FIXME
			dataSetMD = dataSetProvider.getDataSetMD();
			curatorMD = dataSetProvider.getCuratorMD();
			tdbResource = dataSetProvider.getTdbResource();
		} else {
			dataSetMD = new DataSetMD();
			curatorMD = new CuratorMD();
			curatorMD.setName(Util.getPreferenceStore().getString("userName"));
			curatorMD.setAffiliation(Util.getPreferenceStore().getString(
					"userAffiliation"));
			curatorMD
					.setEmail(Util.getPreferenceStore().getString("userEmail"));
			curatorMD
					.setPhone(Util.getPreferenceStore().getString("userPhone"));
		}

		// // addMetaFile();
		// addMetaDataSet();
		// addMetaCurator();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBounds(0, 0, 600, 600);
		composite.setLayout(new GridLayout(1, false));

		combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd_combo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1,
				1);
		gd_combo.widthHint = 185;
		gd_combo.heightHint = 28;
		combo.setLayoutData(gd_combo);

		Label lblAssociatedDataSet = new Label(composite, SWT.NONE);
		GridData gd_lblAssociatedDataSet = new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1);
		gd_lblAssociatedDataSet.heightHint = 36;
		lblAssociatedDataSet.setLayoutData(gd_lblAssociatedDataSet);
		// lblAssociatedDataSet.setBounds(0, 0, 400, 14);
		lblAssociatedDataSet.setText("Data Set");

		Label lblNewLabel = new Label(composite, SWT.NONE);
		GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		gd_lblNewLabel.heightHint = 41;
		lblNewLabel.setLayoutData(gd_lblNewLabel);
		lblNewLabel.setText("New Label");

		text = new Text(composite, SWT.BORDER);
		GridData gd_text = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1,
				1);
		gd_text.widthHint = 149;
		gd_text.heightHint = 34;
		text.setLayoutData(gd_text);
		// combo.add("New", 1);
		// combo.setItems(getDataSetInfo());
		if (dataSetProvider == null) {
			combo.setItems(getDataSetInfo());
			combo.setText("Choose...");
			// combo.setBounds(0, 15, 400, 50);
		} else {
			combo.setItems(getDataSetInfo());
			combo.setEnabled(false);
		}

		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				populateMeta(combo.getText());
				getButton(IDialogConstants.OK_ID).setEnabled(true);
				System.out.println("choice is " + combo.getSelectionIndex()
						+ " with value: " + combo.getText());
			}
		});
		//
		// composite.setLayout(new GridLayout(2, false));
		//
		// Set<String> keySet = modelProvider.getKeys();
		// Iterator<String> keySeq = keySet.iterator();
		// while (keySeq.hasNext()) {
		// String key = keySeq.next();
		// Label lblNewLabel = new Label(composite, SWT.NONE);
		// lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
		// false, false, 1, 1));
		// String readableKey = Util.splitCamelCase(key);
		// System.out.println("key: " + key + " and readableKey: "
		// + readableKey);
		// lblNewLabel.setText(readableKey);
		// dialogValues.add(new Text(composite, SWT.BORDER));
		// dialogValues.get(dialogValues.size() - 1).setLayoutData(
		// new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		// dialogValues.get(dialogValues.size() - 1).setText(
		// modelProvider.getMetaValue(key));
		// }
		return super.createDialogArea(parent);
	}

	// FOR NEW DIALOGS, ONLY NEED FILE INFO
	/**
	 * @wbp.parser.constructor
	 */
	public CSVMetaDialog(Shell parentShell, FileMD fileMD) {
		super(parentShell);
		assert fileMD != null : "fileMD cannot be null";
		this.fileMD = fileMD;
	}

	// FOR EDIT, NEED THE DataSetProvider
	public CSVMetaDialog(Shell parentShell, DataSetProvider dataSetProvider) {
		super(parentShell);
		this.dataSetProvider = dataSetProvider;
	}

	// public CSVMetaDialog(Shell parentShell, ModelProvider modelProvider,
	// boolean initialCreate) {
	// super(parentShell);
	// this.modelProvider = modelProvider;
	// this.initialCreate = initialCreate;
	// }

	@Override
	protected void cancelPressed() {
		super.cancelPressed();
	}

	@Override
	protected void okPressed() {
		Iterator<Text> dialogSeq = dialogValues.iterator();
		String comboText = combo.getText();

		for (String key : modelProvider.getKeys()) {
			Text nextText = dialogSeq.next();
			modelProvider.setMetaKeyValue(key, nextText.getText());
			System.out.println("dialogSeq text value: " + nextText.getText());
		}
		super.okPressed();
	}

	protected void populateMeta(String data_choice) {
		System.out.println("The person chose a new data set...");

		if (data_choice.startsWith("x")) {
			System.out.println("... it is new");
			Set<String> keySet = modelProvider.getKeys();
			int index = -1;
			Iterator<String> keySeq = keySet.iterator();
			while (keySeq.hasNext()) {
				String key = keySeq.next();
				index++;
				// dialogValues.get(index).setText(
				// modelProvider.getMetaValue(key));
				dialogValues.get(index).setText("blank");
			}
		}

	}

	@Override
	public int open() {
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		return super.open();
	}
}
