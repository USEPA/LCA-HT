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
	private DataSetMD dataSetMD = null;
	private CuratorMD curatorMD = null;
	private Resource tdbResource = null;
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
				name = model.listObjectsOfProperty(tdbResource, RDFS.label).next().asLiteral().getString();
			}
			if (model.contains(tdbResource, DCTerms.hasVersion)) {
				version = model.listObjectsOfProperty(tdbResource, DCTerms.hasVersion).next().asLiteral().getString();
			} else if (model.contains(tdbResource, ECO.hasMajorVersionNumber)) {
				version = model.listObjectsOfProperty(tdbResource, ECO.hasMajorVersionNumber).next().asLiteral().getString();
				if (model.contains(tdbResource, ECO.hasMinorVersionNumber)) {
					version += "." + model.listObjectsOfProperty(tdbResource, ECO.hasMinorVersionNumber).next().asLiteral().getString();
				}
			}
			results[0] = id + ": " + name + " " + version;
			return results;
		} else {
			String[] results = new String[DataSetKeeper.size() + 1];
			// results[0] = ""; // RESERVING THIS FOR THE FIRST ENTRY (DEFAULT =
			// // NEW)
			List<Integer> ids = DataSetKeeper.getIDs();
			Iterator<Integer> iterator = ids.iterator();

			Integer id = 0;
			int id_plus_one = id + 1;
			int counter = 0;
			while (iterator.hasNext()) {
				counter++;
				id = iterator.next();
				DataSetProvider dsProvider = DataSetKeeper.get(id);
				Resource tdbResource = dsProvider.getTdbResource();
				String name = "";
				String version = "";
				if (model.contains(tdbResource, RDFS.label)) {
					name = model.listObjectsOfProperty(tdbResource, RDFS.label).next().asLiteral().getString();
				}
				if (model.contains(tdbResource, DCTerms.hasVersion)) {
					version = model.listObjectsOfProperty(tdbResource, DCTerms.hasVersion).next().asLiteral().getString();
				} else if (model.contains(tdbResource, ECO.hasMajorVersionNumber)) {
					version = model.listObjectsOfProperty(tdbResource, ECO.hasMajorVersionNumber).next().asLiteral().getString();
					if (model.contains(tdbResource, ECO.hasMinorVersionNumber)) {
						version += "." + model.listObjectsOfProperty(tdbResource, ECO.hasMinorVersionNumber).next().asLiteral().getString();
					}
				}
				results[counter] = id_plus_one + ":" + name + " " + version;
			}
			Integer next = id + 1;
			results[0] = next + ": (new data set)";
			return results;
		}
	}

	private List<Text> dialogValues = new ArrayList<Text>();

	// private boolean initialCreate;
	// private Text text_02;

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("CSV file Meta Data");
		// FIRST STEP: COLLECT MD INFO BASED ON WHAT IS PASSED, AND ADD OTHER THINGS
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
			curatorMD.setAffiliation(Util.getPreferenceStore().getString("userAffiliation"));
			curatorMD.setEmail(Util.getPreferenceStore().getString("userEmail"));
			curatorMD.setPhone(Util.getPreferenceStore().getString("userPhone"));
		}

		// // addMetaFile();
		// addMetaDataSet();
		// addMetaCurator();
		// NEXT STEP: CREATE THE COMPOSITE

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBounds(0, 0, 600, 1200);
		composite.setLayout(null);
		int col1Left = 5;
		int col1Width = 190;
		int col2Left = 200;
		int col2Width = 200;
		int rowHeight = 20;
		int disBtwnRows = 30;

		Label lbl_01 = new Label(composite, SWT.NONE);
		lbl_01.setBounds(col1Left, 0 * disBtwnRows, col1Width, rowHeight);
		// lblAssociatedDataSet.setBounds(0, 0, 400, 14);
		lbl_01.setText("Data Set");

		// NEXT STEP: ADD THE DATA SET CHOOSER PULL DOWN

		combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setBounds(col2Left, 0 * disBtwnRows, col2Width, rowHeight);

		// combo.add("New", 1);
		// combo.setItems(getDataSetInfo());
		String[] dsInfo = getDataSetInfo();
		// if ((dataSetProvider == null) && (dsInfo.length > 1)) {
		if (dataSetProvider == null) {
			// combo.setText("Choose...");
			combo.setToolTipText("Please choose an existing data set or select: " + dsInfo[0]);
			combo.setItems(getDataSetInfo());
			// combo.setBounds(0, 15, 400, 50);
		} else {
			combo.setItems(getDataSetInfo());
			combo.setEnabled(false);
		}

		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				populateMeta(combo.getText());
				getButton(IDialogConstants.OK_ID).setEnabled(true);
				System.out.println("choice is " + combo.getSelectionIndex() + " with value: " + combo.getText());
			}
		});

		// NEXT STEP: ADD FileMD DATA

		Label lbl_02 = new Label(composite, SWT.NONE);
		lbl_02.setText("File Name");
		lbl_02.setBounds(col1Left, 1 * disBtwnRows, col1Width, rowHeight);
		Text text_02 = new Text(composite, SWT.BORDER);
		text_02.setBounds(col2Left, 1 * disBtwnRows, col2Width, rowHeight);

		Label lbl_03 = new Label(composite, SWT.NONE);
		lbl_03.setBounds(col1Left, 2 * disBtwnRows, col1Width, rowHeight);
		lbl_03.setText("File Size (bytes)");
		Text text_03 = new Text(composite, SWT.BORDER);
		text_03.setBounds(col2Left, 2 * disBtwnRows, col2Width, rowHeight);

		Label lbl_04 = new Label(composite, SWT.NONE);
		lbl_04.setBounds(col1Left, 3 * disBtwnRows, col1Width, rowHeight);
		lbl_04.setText("File Last Modified");
		Text text_04 = new Text(composite, SWT.BORDER);
		text_04.setBounds(col2Left, 3 * disBtwnRows, col2Width, rowHeight);

		Label lbl_05 = new Label(composite, SWT.NONE);
		lbl_05.setBounds(col1Left, 4 * disBtwnRows, col1Width, rowHeight);
		lbl_05.setText("File Read Time");
		Text text_05 = new Text(composite, SWT.BORDER);
		text_05.setBounds(col2Left, 4 * disBtwnRows, col2Width, rowHeight);

		if (fileMD != null) {
			text_02.setText(fileMD.getFilename());
			text_03.setText(fileMD.getSize() + "");
			text_04.setText(harmonizationtool.utils.Util.getLocalDateFmt(fileMD.getLastModified()));
			text_05.setText(harmonizationtool.utils.Util.getLocalDateFmt(fileMD.getReadTime()));
		}

		Label sep_05a = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep_05a.setBounds(50, 5 * disBtwnRows - 5, 250, 2);

		Label lbl_06 = new Label(composite, SWT.NONE);
		lbl_06.setBounds(col1Left, 5 * disBtwnRows, col1Width, rowHeight);
		lbl_06.setText("Data Set Name");
		Text text_06 = new Text(composite, SWT.BORDER);
		text_06.setBounds(col2Left, 5 * disBtwnRows, col2Width, rowHeight);

		Label lbl_07 = new Label(composite, SWT.NONE);
		lbl_07.setBounds(col1Left, 6 * disBtwnRows, col1Width, rowHeight);
		lbl_07.setText("Data Set Version");
		Text text_07 = new Text(composite, SWT.BORDER);
		text_07.setBounds(col2Left, 6 * disBtwnRows, col2Width, rowHeight);

		Label lbl_08 = new Label(composite, SWT.NONE);
		lbl_08.setBounds(col1Left, 7 * disBtwnRows, col1Width, rowHeight);
		lbl_08.setText("Data Set Comments");
		Text text_08 = new Text(composite, SWT.BORDER);
		text_08.setBounds(col2Left, 7 * disBtwnRows, col2Width, rowHeight);

		Label lbl_09 = new Label(composite, SWT.NONE);
		lbl_09.setBounds(col1Left, 8 * disBtwnRows, col1Width, rowHeight);
		lbl_09.setText("Data Set Contact Name");
		Text text_09 = new Text(composite, SWT.BORDER);
		text_09.setBounds(col2Left, 8 * disBtwnRows, col2Width, rowHeight);

		Label lbl_10 = new Label(composite, SWT.NONE);
		lbl_10.setBounds(col1Left, 9 * disBtwnRows, col1Width, rowHeight);
		lbl_10.setText("Data Set Contact Affiliation");
		Text text_10 = new Text(composite, SWT.BORDER);
		text_10.setBounds(col2Left, 9 * disBtwnRows, col2Width, rowHeight);

		Label lbl_11 = new Label(composite, SWT.NONE);
		lbl_11.setBounds(col1Left, 10 * disBtwnRows, col1Width, rowHeight);
		lbl_11.setText("Data Set Contact Email");
		Text text_11 = new Text(composite, SWT.BORDER);
		text_11.setBounds(col2Left, 10 * disBtwnRows, col2Width, rowHeight);

		Label lbl_12 = new Label(composite, SWT.NONE);
		lbl_12.setBounds(col1Left, 11 * disBtwnRows, col1Width, rowHeight);
		lbl_12.setText("Data Set Contact Phone");
		Text text_12 = new Text(composite, SWT.BORDER);
		text_12.setBounds(col2Left, 11 * disBtwnRows, col2Width, rowHeight);

		if (dataSetMD != null) {
			text_06.setText(dataSetMD.getName());
			text_07.setText(dataSetMD.getVersion());
			text_08.setText(dataSetMD.getComments());
			text_09.setText(dataSetMD.getContactName());
			text_10.setText(dataSetMD.getContactAffiliation());
			text_11.setText(dataSetMD.getContactEmail());
			text_12.setText(dataSetMD.getContactPhone());
		}

		Label sep_12a = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep_12a.setBounds(50, 12 * disBtwnRows - 5, 250, 2);

		Label lbl_13 = new Label(composite, SWT.NONE);
		lbl_13.setBounds(col1Left, 12 * disBtwnRows, col1Width, rowHeight);
		lbl_13.setText("Curator Name");
		Text text_13 = new Text(composite, SWT.BORDER);
		text_13.setBounds(col2Left, 12 * disBtwnRows, col2Width, rowHeight);

		Label lbl_14 = new Label(composite, SWT.NONE);
		lbl_14.setBounds(col1Left, 13 * disBtwnRows, col1Width, rowHeight);
		lbl_14.setText("Curator Affiliation");
		Text text_14 = new Text(composite, SWT.BORDER);
		text_14.setBounds(col2Left, 13 * disBtwnRows, col2Width, rowHeight);

		Label lbl_15 = new Label(composite, SWT.NONE);
		lbl_15.setBounds(col1Left, 14 * disBtwnRows, col1Width, rowHeight);
		lbl_15.setText("Curator Email");
		Text text_15 = new Text(composite, SWT.BORDER);
		text_15.setBounds(col2Left, 14 * disBtwnRows, col2Width, rowHeight);

		Label lbl_16 = new Label(composite, SWT.NONE);
		lbl_16.setBounds(col1Left, 15 * disBtwnRows, col1Width, rowHeight);
		lbl_16.setText("Curator Phone");
		Text text_16 = new Text(composite, SWT.BORDER);
		text_16.setBounds(col2Left, 15 * disBtwnRows, col2Width, rowHeight);

		if (curatorMD != null) {
			text_13.setText(curatorMD.getName());
			text_14.setText(curatorMD.getAffiliation());
			text_15.setText(curatorMD.getEmail());
			text_16.setText(curatorMD.getPhone());
		}

		dialogValues.add(text_02);
		dialogValues.add(text_03);
		dialogValues.add(text_04);
		dialogValues.add(text_05);
		dialogValues.add(text_06);
		dialogValues.add(text_07);
		dialogValues.add(text_08);
		dialogValues.add(text_09);
		dialogValues.add(text_10);
		dialogValues.add(text_11);
		dialogValues.add(text_12);
		dialogValues.add(text_13);
		dialogValues.add(text_14);
		dialogValues.add(text_15);
		dialogValues.add(text_16);

		// Label lbl_17 = new Label(composite, SWT.NONE);
		// lbl_17.setBounds(col1Left, 16*disBtwnRows, col1Width, rowHeight);
		// lbl_17.setText("File Size (bytes)");
		// Text text_17 = new Text(composite, SWT.BORDER);
		// text_17.setBounds(col2Left, 16*disBtwnRows, col2Width, rowHeight);
		// text_17.setText(fileMD.getSize() + "");
		// dialogValues.add(text_17);

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
