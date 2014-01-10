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
import harmonizationtool.vocabulary.ETHOLD;

import org.apache.jena.atlas.lib.ArrayUtils;
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
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.DCTerms;

public class CSVMetaDialog extends TitleAreaDialog {

	public ModelProvider modelProvider = null;
	private boolean newFileMD = false;
	private boolean newDataSet = false;
	private DataSetProvider dataSetProvider = null;
	private DataSetProvider tempDataSetProvider = null;
	private FileMD fileMD = null;
	private DataSetMD dataSetMD = null;
	private CuratorMD curatorMD = null;
	private Resource tdbResource = null;
	private Combo combo = null;
	private Combo fileMDCombo = null;
	private List<Text> dialogValues = new ArrayList<Text>();
	private FileMDComboMgr fileMDComboMgr;

	// YOU CAN GET HERE WITH A NEW FILE (FOR A NEW OR EXISTING DATA SET)
	/**
	 * @wbp.parser.constructor
	 */
	public CSVMetaDialog(Shell parentShell, FileMD fileMD) {
		super(parentShell);
		newFileMD = true;
		newDataSet = true;
		assert fileMD != null : "fileMD cannot be null";
		this.fileMD = fileMD; // SET LOCAL VERSION
		tempDataSetProvider = new DataSetProvider();
		this.dataSetMD = new DataSetMD();
		tempDataSetProvider.setDataSetMD(dataSetMD);
		this.curatorMD = new CuratorMD(true);
		tempDataSetProvider.setCuratorMD(curatorMD);
		tempDataSetProvider.addFileMD(fileMD); // THIS MEANS WE DON'T HAVE TO
												// ADD IT AGAIN
		dataSetProvider = tempDataSetProvider;
		// curatorFromPrefs();
	}

	// YOU CAN GET HERE WITH A DataSetProvider
	// WITH DataSetMD , CuratorMD , fileMDList , tdbResource
	public CSVMetaDialog(Shell parentShell, DataSetProvider dataSetProvider) {
		super(parentShell);
		newFileMD = false;
		newDataSet = false;
		this.dataSetProvider = dataSetProvider;
		dataSetMD = dataSetProvider.getDataSetMD();
		curatorMD = dataSetProvider.getCuratorMD();
		tdbResource = dataSetProvider.getTdbResource();
		fileMD = dataSetProvider.getFileMDList().get(0);
	}

	// YOU CAN GET HERE WITH A FileMD AND A DataSetProvider
	// WITH DataSetMD , CuratorMD , fileMDList , tdbResource
	public CSVMetaDialog(Shell parentShell, FileMD fileMD,
			DataSetProvider dataSetProvider) {
		super(parentShell);
		newFileMD = false;
		newDataSet = false;
		this.dataSetProvider = dataSetProvider;
		dataSetMD = dataSetProvider.getDataSetMD();
		curatorMD = dataSetProvider.getCuratorMD();
		tdbResource = dataSetProvider.getTdbResource();
		this.fileMD = fileMD;
	}

	// MAKE THE WHOLE DIALOG BOX
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("CSV file Meta Data");

		// CREATE THE COMPOSITE
		Composite composite = new Composite(parent, SWT.NONE);
		// composite.setBounds(0, 0, 600, 1200);
		composite.setLayout(null);
		int col1Left = 5;
		int col1Width = 190;
		int col2Left = 200;
		int col2Width = 250;
		int rowHeight = 20;
		int disBtwnRows = 30;

		Label lbl_01 = new Label(composite, SWT.NONE);
		lbl_01.setBounds(col1Left, 0 * disBtwnRows, col1Width, rowHeight);
		// lblAssociatedDataSet.setBounds(0, 0, 400, 14);
		lbl_01.setText("Data Set");

		// ADD THE DATA SET CHOOSER PULL DOWN
		combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setBounds(col2Left, 0 * disBtwnRows, col2Width, rowHeight);

		// COLLECT DATA SET LIST
		String[] dsInfo = getDataSetInfo();
		if (newFileMD) {
			combo.setToolTipText("Please choose an existing data set or select: "
					+ dsInfo[0]);
			combo.setItems(getDataSetInfo());
			combo.setText(getDataSetInfo()[0]);

		} else {
			combo.setItems(getDataSetInfo());
			combo.setText(getDataSetInfo()[0]);
			combo.setEnabled(false);
		}

		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				int selectionIndex = combo.getSelectionIndex();
				System.out.println("selectionIndex = " + selectionIndex);
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
		fileMDCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		fileMDComboMgr = new FileMDComboMgr();
		fileMDCombo.setBounds(col2Left, 1 * disBtwnRows, col2Width, rowHeight);
		// NEXT STEP: COLLECT FILE LIST INFO BASED ON WHAT IS PASSED, AND ADD
		// OTHER
		fileMDCombo.setToolTipText("Files associated with this data set."
				+ dsInfo[0]);
		fileMDCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// redrawDialogRows();
				System.out.println("fileMDCombo index "
						+ fileMDCombo.getSelectionIndex());
				populateFileMeta(fileMDCombo.getSelectionIndex());
				System.out.println("choice is "
						+ fileMDCombo.getSelectionIndex() + " with value: "
						+ fileMDCombo.getText());
			}
		});
		// combo2.setItems(getFileInfo());
		// combo2.setText(getFileInfo()[0]);

		// String[] dsInfo = getDataSetInfo();
		// Text text_02 = new Text(composite, SWT.BORDER);
		// text_02.setBounds(col2Left, 1 * disBtwnRows, col2Width, rowHeight);

		Label lbl_03 = new Label(composite, SWT.NONE);
		lbl_03.setBounds(col1Left, 2 * disBtwnRows, col1Width, rowHeight);
		lbl_03.setText("File Size (bytes)");
		Text text_03 = new Text(composite, SWT.BORDER);
		text_03.setBounds(col2Left, 2 * disBtwnRows, col2Width, rowHeight);
		text_03.setEnabled(false);

		Label lbl_04 = new Label(composite, SWT.NONE);
		lbl_04.setBounds(col1Left, 3 * disBtwnRows, col1Width, rowHeight);
		lbl_04.setText("File Last Modified");
		Text text_04 = new Text(composite, SWT.BORDER);
		text_04.setBounds(col2Left, 3 * disBtwnRows, col2Width, rowHeight);
		text_04.setEnabled(false);

		Label lbl_05 = new Label(composite, SWT.NONE);
		lbl_05.setBounds(col1Left, 4 * disBtwnRows, col1Width, rowHeight);
		lbl_05.setText("File Read Time");
		Text text_05 = new Text(composite, SWT.BORDER);
		text_05.setBounds(col2Left, 4 * disBtwnRows, col2Width, rowHeight);
		text_05.setEnabled(false);

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

		// dialogValues.add(text_02); // 00 File Name
		dialogValues.add(text_03); // 00 File Size (bytes)
		dialogValues.add(text_04); // 01 File Last Modified
		dialogValues.add(text_05); // 02 File Read Time
		dialogValues.add(text_06); // 03 Data Set Name
		dialogValues.add(text_07); // 04 Data Set Version
		dialogValues.add(text_08); // 05 Data Set Comments
		dialogValues.add(text_09); // 06 Data Set Contact Name
		dialogValues.add(text_10); // 07 Data Set Contact Affiliation
		dialogValues.add(text_11); // 08 Data Set Contact Email
		dialogValues.add(text_12); // 09 Data Set Contact Phone
		dialogValues.add(text_13); // 10 Curator Name
		dialogValues.add(text_14); // 11 Curator Affiliation
		dialogValues.add(text_15); // 12 Curator Email
		dialogValues.add(text_16); // 13 Curator Phone

		redrawDialogRows();

		return super.createDialogArea(parent);
	}

	@Override
	public int open() {
		if (newFileMD) {
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
		return super.open();
	}

	@Override
	protected void cancelPressed() {

		super.cancelPressed();
	}

	@Override
	protected void okPressed() {

		dataSetMD.setName(dialogValues.get(3).getText());
		dataSetMD.setVersion(dialogValues.get(4).getText());
		dataSetMD.setComments(dialogValues.get(5).getText());
		dataSetMD.setContactName(dialogValues.get(6).getText());
		dataSetMD.setContactAffiliation(dialogValues.get(7).getText());
		dataSetMD.setContactEmail(dialogValues.get(8).getText());
		dataSetMD.setContactPhone(dialogValues.get(9).getText());

		// curatorMD META DATA
		curatorMD.setName(dialogValues.get(10).getText());
		curatorMD.setAffiliation(dialogValues.get(11).getText());
		curatorMD.setEmail(dialogValues.get(12).getText());
		curatorMD.setPhone(dialogValues.get(13).getText());

		dataSetProvider.setDataSetMD(dataSetMD);
		dataSetProvider.setCuratorMD(curatorMD);
//		Model model = SelectTDB.model;
		if (newDataSet) {
			DataSetKeeper.add(dataSetProvider); // A DataSetProvider IS BORN!!
												// (NEW)
			dataSetProviderToTDB(dataSetProvider);
//			int dataSetIdPlusOne = DataSetKeeper.indexOf(dataSetProvider) + 1;
//			Resource newTDBResource = dataSetProvider.getTdbResource();
//			model.addLiteral(newTDBResource, ETHOLD.localSerialNumber,
//					model.createTypedLiteral(dataSetIdPlusOne));
//			model.addLiteral(newTDBResource, RDFS.label,
//					model.createLiteral(dataSetMD.getName()));
//			model.addLiteral(newTDBResource, RDFS.comment,
//					model.createLiteral(dataSetMD.getComments()));
//			model.addLiteral(newTDBResource, DCTerms.hasVersion,
//					model.createLiteral(dataSetMD.getVersion()));
		} else if (newFileMD) {
			dataSetProvider.addFileMD(tempDataSetProvider.getFileMDList()
					.get(0));
			dataSetProviderToTDB(dataSetProvider);
		} else {
			dataSetProviderToTDB(dataSetProvider);
		}

		super.okPressed();
	}

	private void dataSetProviderToTDB(DataSetProvider dsProvider) {
		Model model = SelectTDB.model;
		int dataSetIdPlusOne = DataSetKeeper.indexOf(dsProvider) + 1;
		Resource tdbResource = dsProvider.getTdbResource();
		if (model.contains(tdbResource, ETHOLD.localSerialNumber)) {
			NodeIterator nodeIterator = model.listObjectsOfProperty(
					tdbResource, ETHOLD.localSerialNumber);
			while (nodeIterator.hasNext()) {
				RDFNode rdfNode = nodeIterator.next();
				System.out.println("Is it literal? -- " + rdfNode.isLiteral());
				model.remove(tdbResource, ETHOLD.localSerialNumber,
						rdfNode.asLiteral());
				// model.
			}
		}
		model.addLiteral(tdbResource, ETHOLD.localSerialNumber,
				model.createTypedLiteral(dataSetIdPlusOne));

		if (model.contains(tdbResource, RDFS.label)) {
			NodeIterator nodeIterator = model.listObjectsOfProperty(
					tdbResource, RDFS.label);
			while (nodeIterator.hasNext()) {
				RDFNode rdfNode = nodeIterator.next();
				System.out.println("Is it literal? -- " + rdfNode.isLiteral());
				model.remove(tdbResource, RDFS.label, rdfNode.asLiteral());
			}
		}
		model.addLiteral(tdbResource, RDFS.label,
				model.createLiteral(dataSetMD.getName()));

		if (model.contains(tdbResource, RDFS.comment)) {
			NodeIterator nodeIterator = model.listObjectsOfProperty(
					tdbResource, RDFS.comment);
			while (nodeIterator.hasNext()) {
				RDFNode rdfNode = nodeIterator.next();
				System.out.println("Is it literal? -- " + rdfNode.isLiteral());
				model.remove(tdbResource, RDFS.comment, rdfNode.asLiteral());
			}
		}
		model.addLiteral(tdbResource, RDFS.comment,
				model.createLiteral(dataSetMD.getComments()));

		if (model.contains(tdbResource, DCTerms.hasVersion)) {
			NodeIterator nodeIterator = model.listObjectsOfProperty(
					tdbResource, DCTerms.hasVersion);
			while (nodeIterator.hasNext()) {
				RDFNode rdfNode = nodeIterator.next();
				System.out.println("Is it literal? -- " + rdfNode.isLiteral());
				model.remove(tdbResource, DCTerms.hasVersion,
						rdfNode.asLiteral());
			}
		}
		model.addLiteral(tdbResource, DCTerms.hasVersion,
				model.createLiteral(dataSetMD.getVersion()));

	}

	// private String[] getFileInfo() {
	// List<String> filenameList = new ArrayList<String>();
	// List<FileMD> fileList = dataSetProvider.getFileMDList();
	// for (FileMD fileMD : fileList) {
	// int id = fileList.indexOf(fileMD);
	// int idPlusOne = id + 1;
	// String name = fileMD.getFilename();
	// filenameList.add(idPlusOne + ":" + name);
	// }
	// return filenameList.toArray(new String[filenameList.size()]);
	// }

	// COLLECT INFO ABOUT DATA SETS FROM THE TDB
	private String[] getDataSetInfo() {
		Model model = SelectTDB.model;
		if (!newFileMD) {
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

	private void populateFileMeta(int index) {
		System.out.println("index = " + index);
		List<FileMD> fileList = dataSetProvider.getFileMDList();
		if (index < 0) {
			return;
		}
		if (fileList.size() <= 0) {
			return;
		}
		if (fileList.size() <= index) {
			fileMD = fileList.get(index - 1);
		} else {
			fileMD = fileList.get(index);
		}

		// dialogValues.get(0).setText(fileMD.getFilename());
		fileMDCombo.setToolTipText(fileMD.getPath());
		dialogValues.get(0).setText(fileMD.getSize() + "");
		dialogValues.get(1).setText(
				Util.getLocalDateFmt(fileMD.getLastModified()));
		dialogValues.get(2).setText(Util.getLocalDateFmt(fileMD.getReadTime()));
	}

	protected void populateMeta(String dataSetChosen) {
		System.out.println("The person chose a new data set: " + dataSetChosen);
		int dsNum = Integer.parseInt(dataSetChosen.split(":")[0]) - 1; // SUBTRACT
																		// 1 !!
		System.out.println("Got data set number: " + dsNum);
		// if ()
		if (dataSetChosen.endsWith("new data set)")) {
			newDataSet = true;
			dataSetProvider = tempDataSetProvider;
			fileMD = tempDataSetProvider.getFileMDList().get(0);
			System.out.println("... it is new");
		} else {
			newDataSet = false;
			if (!DataSetKeeper.hasIndex(dsNum)) {
				return;
			}
			try {
				dataSetProvider = DataSetKeeper.get(dsNum);
			} catch (Exception e) {
				System.out.println("What happened?");
			}
		}

		List<FileMD> fileMDList = dataSetProvider.getFileMDList();
		System.out.println("fileMDList has size: " + fileMDList.size());
		if (tempDataSetProvider != null) {
			fileMD = tempDataSetProvider.getFileMDList().get(0);
		} else if (fileMDList.size() > 0) {
			fileMD = fileMDList.get(0);
		} else {
			fileMD = null;
		}
		dataSetMD = dataSetProvider.getDataSetMD();
		curatorMD = dataSetProvider.getCuratorMD();
		redrawDialogRows();
	}

	protected void redrawDialogRows() {
		System.out.println(" in redrawDialogRows()");
		// CLEAR ALL DIALOG BOXES (BECAUSE WE'LL REDRAW)
		Iterator<Text> dialogValueIterator = dialogValues.iterator();
		while (dialogValueIterator.hasNext()) {
			Text dialogValue = dialogValueIterator.next();
			dialogValue.setText("");
		}

		// String[] listOfFiles = getFileInfo();
		// int index = 0;
		List<FileMD> tempFileMDList = new ArrayList<FileMD>();
		if (!newDataSet && newFileMD) { // NOT A NEW DATA SET, BUT A NEW FILE
										// NAME (TO ADD, PRESUMABLY)
			tempFileMDList.add(tempDataSetProvider.getFileMDList().get(0));
		}
		tempFileMDList.addAll(dataSetProvider.getFileMDList());
		fileMDComboMgr.setItems(tempFileMDList);
		fileMDComboMgr.setText(fileMD);

		if (fileMD != null) {
			// dialogValues.get(0).setText(fileMD.getFilename());
			fileMDCombo.setToolTipText(fileMD.getPath());
			dialogValues.get(0).setText(fileMD.getSize() + "");
			dialogValues.get(1).setText(
					Util.getLocalDateFmt(fileMD.getLastModified()));
			dialogValues.get(2).setText(
					Util.getLocalDateFmt(fileMD.getReadTime()));
		}
		if (dataSetMD != null) {
			System.out.println("dataSetMD.getName: = " + dataSetMD.getName());
			dialogValues.get(3).setText(dataSetMD.getName());
			dialogValues.get(4).setText(dataSetMD.getVersion());
			dialogValues.get(5).setText(dataSetMD.getComments());
			dialogValues.get(6).setText(dataSetMD.getContactName());
			dialogValues.get(7).setText(dataSetMD.getContactAffiliation());
			dialogValues.get(8).setText(dataSetMD.getContactEmail());
			dialogValues.get(9).setText(dataSetMD.getContactPhone());

		}
		if (curatorMD != null) {
			System.out.println("curatorMD.getName: = " + curatorMD.getName());
			dialogValues.get(10).setText(curatorMD.getName());
			dialogValues.get(11).setText(curatorMD.getAffiliation());
			dialogValues.get(12).setText(curatorMD.getEmail());
			dialogValues.get(13).setText(curatorMD.getPhone());
		}
	}

	private class FileMDComboMgr {
		List<FileMD> fileMDlist = null;

		public FileMDComboMgr() {

		}

		public void setItems(List<FileMD> fileMDlist) {
			this.fileMDlist = fileMDlist;
			String[] temp = new String[fileMDlist.size()];
			int index = 0;
			for (FileMD fileMD : fileMDlist) {
				temp[index++] = fileMD.getFilename();
			}
			fileMDCombo.setItems(temp);
		}

		public FileMD getFileMD(int index) {
			return fileMDlist.get(index);
		}

		public void setText(FileMD fileMD) {
			if (fileMD == null) {
				return;
			}
			fileMDCombo.setText(fileMD.getFilename());
		}
	}
}
