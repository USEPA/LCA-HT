package harmonizationtool.dialog;

//import java.awt.List;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private Combo combo2 = null;
	private List<Text> dialogValues = new ArrayList<Text>();

	// YOU CAN GET HERE WITH A NEW FILE (FOR A NEW OR EXISTING DATA SET)
	/**
	 * @wbp.parser.constructor
	 */
	public CSVMetaDialog(Shell parentShell, FileMD fileMD) {
		super(parentShell);
		assert fileMD != null : "fileMD cannot be null";
		this.fileMD = fileMD; // SET LOCAL VERSION
		dataSetMD = new DataSetMD();
		curatorMD = new CuratorMD();
		curatorFromPrefs();
	}

	// YOU CAN GET HERE WITH A DataSetProvider WITH DataSetMD , CuratorMD ,
	// fileMDList , tdbResource
	public CSVMetaDialog(Shell parentShell, DataSetProvider dataSetProvider) {
		super(parentShell);
		this.dataSetProvider = dataSetProvider;
		dataSetMD = dataSetProvider.getDataSetMD();
		curatorMD = dataSetProvider.getCuratorMD();
		tdbResource = dataSetProvider.getTdbResource();
	}

	// MAKE THE WHOLE DIALOG BOX
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("CSV file Meta Data");
		// FIRST STEP: COLLECT MD INFO BASED ON WHAT IS PASSED, AND ADD OTHER
		// THINGS
		// if (fileMD == null) {
		// if (dataSetProvider == null) {
		// return null; // HOW DID WE GET HERE WITH NEITHER?
		// }
		// fileMD = dataSetProvider.getFileMDList().get(0); // FIRST FILE...
		// // BUT THIS IS
		// // NOT RIGHT, SO
		// // FIXME
		//
		// } else {
		//
		//
		// }

		// // addMetaFile();
		// addMetaDataSet();
		// addMetaCurator();
		// NEXT STEP: CREATE THE COMPOSITE

		Composite composite = new Composite(parent, SWT.NONE);
		// composite.setBounds(0, 0, 600, 1200);
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

		String[] dsInfo = getDataSetInfo();
		// if ((dataSetProvider == null) && (dsInfo.length > 1)) {
		if (dataSetProvider == null) {
			// combo.setText("Choose..."); // THIS DOES NOT WORK... :-(
			combo.setToolTipText("Please choose an existing data set or select: "
					+ dsInfo[0]);
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
				System.out.println("choice is " + combo.getSelectionIndex()
						+ " with value: " + combo.getText());
			}
		});

		// NEXT STEP: ADD FileMD DATA

		Label lbl_02 = new Label(composite, SWT.NONE);
		lbl_02.setText("File Name");
		lbl_02.setBounds(col1Left, 1 * disBtwnRows, col1Width, rowHeight);
		combo2 = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo2.setBounds(col2Left, 1 * disBtwnRows, col2Width, rowHeight);
		combo2.setToolTipText("Files associated with this data set."
				+ dsInfo[0]);
		// combo2.setItems(getFileInfo());

		// String[] dsInfo = getDataSetInfo();
		// Text text_02 = new Text(composite, SWT.BORDER);
		// text_02.setBounds(col2Left, 1 * disBtwnRows, col2Width, rowHeight);

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
			// text_02.setText(fileMD.getFilename());
			text_03.setText(fileMD.getSize() + "");
			text_04.setText(Util.getLocalDateFmt(fileMD.getLastModified()));
			text_05.setText(Util.getLocalDateFmt(fileMD.getReadTime()));
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

		// dialogValues.add(text_02); // 00 File Name
		dialogValues.add(text_03); // 01 File Size (bytes)
		dialogValues.add(text_04); // 02 File Last Modified
		dialogValues.add(text_05); // 03 File Read Time
		dialogValues.add(text_06); // 04 Data Set Name
		dialogValues.add(text_07); // 05 Data Set Version
		dialogValues.add(text_08); // 06 Data Set Comments
		dialogValues.add(text_09); // 07 Data Set Contact Name
		dialogValues.add(text_10); // 08 Data Set Contact Affiliation
		dialogValues.add(text_11); // 09 Data Set Contact Email
		dialogValues.add(text_12); // 10 Data Set Contact Phone
		dialogValues.add(text_13); // 11 Curator Name
		dialogValues.add(text_14); // 12 Curator Affiliation
		dialogValues.add(text_15); // 13 Curator Email
		dialogValues.add(text_16); // 14 Curator Phone

		// Label lbl_17 = new Label(composite, SWT.NONE);
		// lbl_17.setBounds(col1Left, 16*disBtwnRows, col1Width, rowHeight);
		// lbl_17.setText("File Size (bytes)");
		// Text text_17 = new Text(composite, SWT.BORDER);
		// text_17.setBounds(col2Left, 16*disBtwnRows, col2Width, rowHeight);
		// text_17.setText(fileMD.getSize() + "");
		// dialogValues.add(text_17);

		return super.createDialogArea(parent);
	}

	@Override
	public int open() {
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		return super.open();
	}

	@Override
	protected void cancelPressed() {
		super.cancelPressed();
	}

	@Override
	protected void okPressed() {
		// FIRST POPULATE THE THREE BLOCKS OF META DATA:

		// fileMD META DATA
		fileMD.setFilename(dialogValues.get(0).getText());
		fileMD.setSize(Integer.parseInt(dialogValues.get(1).getText()));
		try {
			fileMD.setLastModified(Util.setDateFmt(dialogValues.get(2)
					.getText()));
		} catch (ParseException e) {
			fileMD.setLastModified(null);
		}
		try {
			fileMD.setReadTime(Util.setDateFmt(dialogValues.get(3).getText()));
		} catch (ParseException e) {
			fileMD.setReadTime(null);
		}

		// dataSetMD META DATA
		dataSetMD.setName(dialogValues.get(4).getText());
		dataSetMD.setVersion(dialogValues.get(5).getText());
		dataSetMD.setComments(dialogValues.get(6).getText());
		dataSetMD.setContactName(dialogValues.get(7).getText());
		dataSetMD.setContactAffiliation(dialogValues.get(8).getText());
		dataSetMD.setContactEmail(dialogValues.get(9).getText());
		dataSetMD.setContactPhone(dialogValues.get(10).getText());

		// curatorMD META DATA
		curatorMD.setName(dialogValues.get(11).getText());
		curatorMD.setAffiliation(dialogValues.get(12).getText());
		curatorMD.setEmail(dialogValues.get(13).getText());
		curatorMD.setPhone(dialogValues.get(14).getText());

		dataSetProvider.setDataSetMD(dataSetMD);
		dataSetProvider.setCuratorMD(curatorMD);

		// ARE ADDING THESE TO AN EXISTING DataSetProvider OR A NEW ONE
		Iterator<Text> dialogSeq = dialogValues.iterator();
		String comboText = combo.getText();
		String sPattern = "^(\\d+)";
		Pattern pattern = Pattern.compile(sPattern);
		Matcher matcher = pattern.matcher(comboText);
		matcher.find();
		int dsNum = Integer.parseInt(matcher.group(0)) - 1; // SUBTRACT 1 !!

		if (DataSetKeeper.hasIndex(dsNum)) {
			DataSetProvider dataSetOrig = DataSetKeeper.get(dsNum);
			dataSetProvider.setTdbResource(dataSetOrig.getTdbResource());

			List<FileMD> fileMDList = dataSetProvider.getFileMDList();
			fileMDList.add(fileMD); // QUICK HACK, THAT DOES NOT PREDICATE THE
									// DUPLICATION OF FILE INFO
		} else {
			DataSetKeeper.add(dataSetProvider);
			dataSetProvider.setCuratorMD(curatorMD);
		}

		for (String key : modelProvider.getKeys()) {
			Text nextText = dialogSeq.next();
			modelProvider.setMetaKeyValue(key, nextText.getText());
			System.out.println("dialogSeq text value: " + nextText.getText());
		}
		super.okPressed();
	}

	// POPULATE CuratorMD FROM PREFERENCES
	private void curatorFromPrefs() {
		curatorMD.setName(Util.getPreferenceStore().getString("userName"));
		curatorMD.setAffiliation(Util.getPreferenceStore().getString(
				"userAffiliation"));
		curatorMD.setEmail(Util.getPreferenceStore().getString("userEmail"));
		curatorMD.setPhone(Util.getPreferenceStore().getString("userPhone"));
	}

	private String[] getFileInfo() {
		List<String> filenameList = new ArrayList<String>();
		List<FileMD> fileList = dataSetProvider.getFileMDList();
		for (FileMD fileMD : fileList) {
			int id = fileList.indexOf(fileMD);
			int idPlusOne = id + 1;
			String name = fileMD.getFilename();
			filenameList.add(idPlusOne + ":" + name);
		}
		return filenameList.toArray(new String[filenameList.size()]);
	}

	// COLLECT INFO ABOUT DATA SETS FROM THE TDB
	private String[] getDataSetInfo() {
		Model model = SelectTDB.model;
		if (dataSetProvider != null) {
			Integer id = DataSetKeeper.indexOf(dataSetProvider);
			String name = dataSetProvider.getDataSetMD().getName();
			String version = dataSetProvider.getDataSetMD().getVersion();
			int id_plus_one = id + 1;
			String[] results = new String[1];
			results[0] = id_plus_one + ": " + name + " " + version;
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
				id_plus_one = id + 1;
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
				} else if (model.contains(tdbResource,
						ECO.hasMajorVersionNumber)) {
					version = model
							.listObjectsOfProperty(tdbResource,
									ECO.hasMajorVersionNumber).next()
							.asLiteral().getString();
					if (model.contains(tdbResource, ECO.hasMinorVersionNumber)) {
						version += "."
								+ model.listObjectsOfProperty(tdbResource,
										ECO.hasMinorVersionNumber).next()
										.asLiteral().getString();
					}
				}
				results[counter] = id_plus_one + ":" + name + " " + version;
			}
			Integer next = id_plus_one + 1;
			results[0] = next + ": (new data set)";
			return results;
		}
	}

	protected void populateMeta(String dataSetChosen) {
		System.out.println("The person chose a new data set: " + dataSetChosen);
		int dsNum = Integer.parseInt(dataSetChosen.split(":")[0]) - 1;
		// String sPattern = "^(\\d+)";
		// Pattern pattern = Pattern.compile(sPattern);
		// Matcher matcher = pattern.matcher(dataSetChosen);
		// matcher.find();
		// int = Integer.parseInt(matcher.group(0)) - 1; // SUBTRACT 1 !!
		System.out.println("Got data set number: " + dsNum);

		if (dataSetChosen.endsWith("new data set)")) {
			System.out.println("... it is new");
			fileMD = fileMD;
			dataSetMD = new DataSetMD();
			curatorMD = new CuratorMD();
			curatorFromPrefs();
			redrawDialogRows();
		} else {
			if (!DataSetKeeper.hasIndex(dsNum)) {
				return;
			}
			try {
				dataSetProvider = DataSetKeeper.get(dsNum);
				List<FileMD> fileMDList = dataSetProvider.getFileMDList();

				System.out.println("fileMDList has size: " + fileMDList.size());
				if (fileMDList.size() > 0) {
					combo2.setItems(getFileInfo());
				}
				System.out.println("Got past fileMD");
				dataSetMD = dataSetProvider.getDataSetMD();
				System.out.println("Got past dataSetMD");
				curatorMD = dataSetProvider.getCuratorMD();
				System.out.println("Got past curatorMD");
				redrawDialogRows();
			} catch (Exception e) {
				System.out.println("What happened?");
			}
		}
	}

	protected void redrawDialogRows() {
		// CLEAR ALL DIALOG BOXES (BECAUSE WE'LL REDRAW)
		Iterator<Text> iter = dialogValues.iterator();
		while (iter.hasNext()) {
			Text thing = iter.next();
			thing.setText("");
		}
		if (fileMD != null) {
			combo2.setItems(getFileInfo());
			// dialogValues.get(0).setText(fileMD.getFilename());
			dialogValues.get(1).setText(fileMD.getSize() + "");
			dialogValues.get(2).setText(
					Util.getLocalDateFmt(fileMD.getLastModified()));
			dialogValues.get(3).setText(
					Util.getLocalDateFmt(fileMD.getReadTime()));
		}
		if (dataSetMD != null) {
			System.out.println("dataSetMD.getName: = " + dataSetMD.getName());
			dialogValues.get(4).setText(dataSetMD.getName());
			dialogValues.get(5).setText(dataSetMD.getVersion());
			dialogValues.get(6).setText(dataSetMD.getComments());
			dialogValues.get(7).setText(dataSetMD.getContactName());
			dialogValues.get(8).setText(dataSetMD.getContactAffiliation());
			dialogValues.get(9).setText(dataSetMD.getContactEmail());
			dialogValues.get(10).setText(dataSetMD.getContactPhone());

		}
		if (curatorMD != null) {
			System.out.println("curatorMD.getName: = " + curatorMD.getName());
			dialogValues.get(11).setText(curatorMD.getName());
			dialogValues.get(12).setText(curatorMD.getAffiliation());
			dialogValues.get(13).setText(curatorMD.getEmail());
			dialogValues.get(14).setText(curatorMD.getPhone());
		}
	}
}
