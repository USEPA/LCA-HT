package harmonizationtool.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.model.CuratorMD;
import harmonizationtool.model.DataSetKeeper;
import harmonizationtool.model.DataSetMD;
import harmonizationtool.model.DataSetProvider;
import harmonizationtool.model.FileMD;
import harmonizationtool.model.ModelProvider;
import harmonizationtool.utils.Util;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.DCTerms;
import org.eclipse.wb.swt.SWTResourceManager;

public class ChooseDataSetDialog extends TitleAreaDialog {

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
	// YOU CAN GET HERE WITH A DataSetProvider
	// WITH DataSetMD , CuratorMD , fileMDList , tdbResource

	public ChooseDataSetDialog(Shell parentShell) {
		super(parentShell);
		newFileMD = false;
		newDataSet = false;
		if (DataSetKeeper.size() == 0) {
			new GenericMessageBox(
					parentShell,
					"No Data Sets",
					"The HT does not contain any DataSets at this time.  Read a CSV or RDF file to create some.");
			return;
		}
		this.dataSetProvider = DataSetKeeper.get(0);
		dataSetMD = dataSetProvider.getDataSetMD();
		curatorMD = dataSetProvider.getCuratorMD();
		tdbResource = dataSetProvider.getTdbResource();
	}

	// MAKE THE WHOLE DIALOG BOX
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Select a DataSet to delete");

		// CREATE THE COMPOSITE
		Composite composite = new Composite(parent, SWT.NONE);
		// composite.setBounds(0, 0, 600, 1200);
		composite.setLayout(null);
		int col1Left = 5;
		int col1LeftIndent = 25;
		int col1Width = 100;
		int col2Left = 125;
		int col2Width = 250;
		int rowHeight = 20;
		int disBtwnRows = 30;
		int rowIndex = 0;

		rowIndex = 0;
		Label lbl_5b = new Label(composite, SWT.LEFT);
		lbl_5b.setFont(SWTResourceManager
				.getFont("Lucida Grande", 16, SWT.BOLD));
		lbl_5b.setBounds(col1Left, rowIndex * disBtwnRows, col1Width
				+ col2Width, rowHeight);
		lbl_5b.setText("Data Set Information:");

		rowIndex++;
		Label lbl_01 = new Label(composite, SWT.RIGHT);
		lbl_01.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
				rowHeight);
		// lblAssociatedDataSet.setBounds(0, 0, 400, 14);
		lbl_01.setText("Name");

		// ADD THE DATA SET CHOOSER PULL DOWN
		combo = new Combo(composite, SWT.DROP_DOWN);
		combo.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		// COLLECT DATA SET LIST
		String[] dsInfo = getDataSetInfo();
//		if (newFileMD) {
//			combo.setToolTipText("Please choose an existing data set or select: "
//					+ dsInfo[0]);
//			combo.setItems(getDataSetInfo());
//			combo.setText(getDataSetInfo()[0]);
//
//		} else {
			combo.setItems(getDataSetInfo());
			combo.setText(getDataSetInfo()[0]);
//		}
		combo.setEnabled(true);

		combo.addMouseListener(new MouseListener() {

			private String savedComboText = "";

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseDown(MouseEvent e) {
				savedComboText = combo.getText();

			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (savedComboText.equals(combo.getText())){
					return; 
				}
				int selectionIndex = combo.getSelectionIndex();
				System.out.println("selectionIndex = " + selectionIndex);
				populateMeta(combo.getText());
				getButton(IDialogConstants.OK_ID).setEnabled(true);
				System.out.println("choice is " + combo.getSelectionIndex()
						+ " with value: " + combo.getText());
			}
		});
		// combo.addModifyListener(new ModifyListener() {
		// public void modifyText(ModifyEvent e) {
		// int selectionIndex = combo.getSelectionIndex();
		// System.out.println("selectionIndex = " + selectionIndex);
		// populateMeta(combo.getText());
		// getButton(IDialogConstants.OK_ID).setEnabled(true);
		// System.out.println("choice is " + combo.getSelectionIndex() +
		// " with value: " + combo.getText());
		// }
		// });

		rowIndex++;
		Label lbl_07 = new Label(composite, SWT.RIGHT);
		lbl_07.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
				rowHeight);
		lbl_07.setText("Version");
		Text text_07 = new Text(composite, SWT.BORDER);
		text_07.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
				rowHeight);
		text_07.setEnabled(false);

		rowIndex++;
		Label lbl_08 = new Label(composite, SWT.RIGHT);
		lbl_08.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
				rowHeight);
		lbl_08.setText("Comments");
		Text text_08 = new Text(composite, SWT.BORDER);
		text_08.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
				rowHeight);
		text_08.setEnabled(false);


		rowIndex++;
		Label lbl_09 = new Label(composite, SWT.RIGHT);
		lbl_09.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
				rowHeight);
		lbl_09.setText("Contact Name");
		Text text_09 = new Text(composite, SWT.BORDER);
		text_09.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
				rowHeight);
		text_09.setEnabled(false);

		rowIndex++;
		Label lbl_10 = new Label(composite, SWT.RIGHT);
		lbl_10.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
				rowHeight);
		lbl_10.setText("Contact Affiliation");
		Text text_10 = new Text(composite, SWT.BORDER);
		text_10.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
				rowHeight);
		text_10.setEnabled(false);

		rowIndex++;
		Label lbl_11 = new Label(composite, SWT.RIGHT);
		lbl_11.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
				rowHeight);
		lbl_11.setText("Contact Email");
		Text text_11 = new Text(composite, SWT.BORDER);
		text_11.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
				rowHeight);
		text_11.setEnabled(false);

		rowIndex++;
		Label lbl_12 = new Label(composite, SWT.RIGHT);
		lbl_12.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
				rowHeight);
		lbl_12.setText("Contact Phone");
		Text text_12 = new Text(composite, SWT.BORDER);
		text_12.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
				rowHeight);
		text_12.setEnabled(false);

		// NEXT STEP: ADD FileMD DATA
		rowIndex = 8;
		Label sep_01a = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep_01a.setBounds(50, rowIndex * disBtwnRows - 5, 250, 2);

		Label lbl_1b = new Label(composite, SWT.LEFT);
		lbl_1b.setFont(SWTResourceManager
				.getFont("Lucida Grande", 16, SWT.BOLD));
		lbl_1b.setBounds(col1Left, rowIndex * disBtwnRows, col1Width
				+ col2Width, rowHeight);
		lbl_1b.setText("File Information:");

		rowIndex++;
		Label lbl_02 = new Label(composite, SWT.RIGHT);
		lbl_02.setText("Name");
		lbl_02.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
				rowHeight);
		fileMDCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		fileMDComboMgr = new FileMDComboMgr();
		fileMDCombo.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
				rowHeight);
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

		rowIndex++;
		Label lbl_03 = new Label(composite, SWT.RIGHT);
		lbl_03.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
				rowHeight);
		lbl_03.setText("Size (bytes)");
		Text text_03 = new Text(composite, SWT.BORDER);
		text_03.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
				rowHeight);
		text_03.setEnabled(false);

		rowIndex++;
		Label lbl_04 = new Label(composite, SWT.RIGHT);
		lbl_04.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
				rowHeight);
		lbl_04.setText("Last Modified");
		Text text_04 = new Text(composite, SWT.BORDER);
		text_04.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
				rowHeight);
		text_04.setEnabled(false);

		rowIndex++;
		Label lbl_05 = new Label(composite, SWT.RIGHT);
		lbl_05.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
				rowHeight);
		lbl_05.setText("Read Time");
		Text text_05 = new Text(composite, SWT.BORDER);
		text_05.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
				rowHeight);
		text_05.setEnabled(false);

		// rowIndex++;
		// Label sep_05a = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		// sep_05a.setBounds(50, rowIndex * disBtwnRows - 5, 250, 2);

		// rowIndex++;
		// Label lbl_06 = new Label(composite, SWT.RIGHT);
		// lbl_06.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
		// rowHeight);
		// lbl_06.setText("Name");
		// Text text_06 = new Text(composite, SWT.BORDER);
		// text_06.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
		// rowHeight);

		rowIndex = 13;
		Label sep_12a = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep_12a.setBounds(60, rowIndex * disBtwnRows - 5, 250, 2);

		Label lbl_12b = new Label(composite, SWT.LEFT);
		lbl_12b.setFont(SWTResourceManager.getFont("Lucida Grande", 16,
				SWT.BOLD));
		lbl_12b.setBounds(5, rowIndex * disBtwnRows, col1Width + col2Width, 20);
		lbl_12b.setText("Curator Information:");

		rowIndex++;
//		Button copyUserInfo = new Button(composite, SWT.BORDER);
//		copyUserInfo
//				.setToolTipText("Values for the Curator will be copied from user info set in the preferences.");
//		copyUserInfo.setBounds(col2Left, rowIndex * disBtwnRows, 250, 25);
//		copyUserInfo.setText("Copy Info from Preferences");
//		copyUserInfo.addListener(SWT.Selection, new Listener() {
//			@Override
//			public void handleEvent(Event event) {
//				dialogValues.get(9).setText(
//						Util.getPreferenceStore().getString("curatorName"));
//				dialogValues.get(10).setText(
//						Util.getPreferenceStore().getString("curatorAffiliation"));
//				dialogValues.get(11).setText(
//						Util.getPreferenceStore().getString("curatorEmail"));
//				dialogValues.get(12).setText(
//						Util.getPreferenceStore().getString("curatorPhone"));
//			}
//		});

//		rowIndex++;
		Label lbl_13 = new Label(composite, SWT.RIGHT);
		lbl_13.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
				rowHeight);
		lbl_13.setText("Name");
		Text text_13 = new Text(composite, SWT.BORDER);
		text_13.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
				rowHeight);

		rowIndex++;
		Label lbl_14 = new Label(composite, SWT.RIGHT);
		lbl_14.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, 20);
		lbl_14.setText("Affiliation");
		Text text_14 = new Text(composite, SWT.BORDER);
		text_14.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
				rowHeight);

		rowIndex++;
		Label lbl_15 = new Label(composite, SWT.RIGHT);
		lbl_15.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, 20);
		lbl_15.setText("Email");
		Text text_15 = new Text(composite, SWT.BORDER);
		text_15.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
				rowHeight);

		rowIndex++;
		Label lbl_16 = new Label(composite, SWT.RIGHT);
		lbl_16.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
				rowHeight);
		lbl_16.setText("Phone");
		Text text_16 = new Text(composite, SWT.BORDER);
		text_16.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
				rowHeight);

		// dialogValues.add(text_02); // 00 File Name
		dialogValues.add(text_03); // 00 File Size (bytes)
		dialogValues.add(text_04); // 01 File Last Modified
		dialogValues.add(text_05); // 02 File Read Time
		// dialogValues.add(text_06); // 03 Data Set Name
		dialogValues.add(text_07); // 03 Data Set Version
		dialogValues.add(text_08); // 04 Data Set Comments
		dialogValues.add(text_09); // 05 Data Set Contact Name
		dialogValues.add(text_10); // 06 Data Set Contact Affiliation
		dialogValues.add(text_11); // 07 Data Set Contact Email
		dialogValues.add(text_12); // 08 Data Set Contact Phone
		dialogValues.add(text_13); // 9 Curator Name
		dialogValues.add(text_14); // 10 Curator Affiliation
		dialogValues.add(text_15); // 11 Curator Email
		dialogValues.add(text_16); // 12 Curator Phone

		redrawDialogRows();

		Control control = super.createDialogArea(parent);
		control.setToolTipText("");
		return control;
	}

	@Override
	public int open() {
//		if (newFileMD) {
////			getButton(IDialogConstants.OK_ID).setEnabled(false);
//		}
		return super.open();
	}

	@Override
	protected void cancelPressed() {

		super.cancelPressed();
	}

	@Override
	protected void okPressed() {

		// dataSetMD.setName(dialogValues.get(3).getText()); //FIXME
		dataSetMD.setName(combo.getText()); // FIXME
		dataSetMD.setVersion(dialogValues.get(3).getText());
		dataSetMD.setComments(dialogValues.get(4).getText());
		dataSetMD.setContactName(dialogValues.get(5).getText());
		dataSetMD.setContactAffiliation(dialogValues.get(6).getText());
		dataSetMD.setContactEmail(dialogValues.get(7).getText());
		dataSetMD.setContactPhone(dialogValues.get(8).getText());

		// curatorMD META DATA
		curatorMD.setName(dialogValues.get(9).getText());
		curatorMD.setAffiliation(dialogValues.get(10).getText());
		curatorMD.setEmail(dialogValues.get(11).getText());
		curatorMD.setPhone(dialogValues.get(12).getText());

		dataSetProvider.setDataSetMD(dataSetMD);
		dataSetProvider.setCuratorMD(curatorMD);
		// Model model = ActiveTDB.model;
//		if (newDataSet) {
//			DataSetKeeper.add(dataSetProvider); // A DataSetProvider IS BORN!!
//												// (NEW)
//			dataSetProviderToTDB(dataSetProvider);
//			// int dataSetIdPlusOne = DataSetKeeper.indexOf(dataSetProvider) +
//			// 1;
//			// Resource newTDBResource = dataSetProvider.getTdbResource();
//			// model.addLiteral(newTDBResource, ECOGOV.localSerialNumber,
//			// model.createTypedLiteral(dataSetIdPlusOne));
//			// model.addLiteral(newTDBResource, RDFS.label,
//			// model.createLiteral(dataSetMD.getName()));
//			// model.addLiteral(newTDBResource, RDFS.comment,
//			// model.createLiteral(dataSetMD.getComments()));
//			// model.addLiteral(newTDBResource, DCTerms.hasVersion,
//			// model.createLiteral(dataSetMD.getVersion()));
//		} else if (newFileMD) {
//			dataSetProvider.addFileMD(tempDataSetProvider.getFileMDList()
//					.get(0));
//			dataSetProviderToTDB(dataSetProvider);
//		} else {
			dataSetProviderToTDB(dataSetProvider);
//		}

		super.okPressed();
	}

	private void dataSetProviderToTDB(DataSetProvider dsProvider) {
		Model model = ActiveTDB.model;
		// int dataSetIdPlusOne = DataSetKeeper.indexOf(dsProvider) + 1;
		Resource tdbResource = dsProvider.getTdbResource();
		// if (model.contains(tdbResource, ECOGOV.localSerialNumber)) {
		// NodeIterator nodeIterator = model.listObjectsOfProperty(
		// tdbResource, ECOGOV.localSerialNumber);
		// while (nodeIterator.hasNext()) {
		// RDFNode rdfNode = nodeIterator.next();
		// System.out.println("Is it literal? -- " + rdfNode.isLiteral());
		// model.remove(tdbResource, ECOGOV.localSerialNumber,
		// rdfNode.asLiteral());
		// // model.
		// }
		// }
		// model.addLiteral(tdbResource, ECOGOV.localSerialNumber,
		// model.createTypedLiteral(dataSetIdPlusOne));

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
		Model model = ActiveTDB.model;
//		if (!dataSetEnabled) {
//			Integer id = DataSetKeeper.indexOf(dataSetProvider);
//			String name = dataSetProvider.getDataSetMD().getName();
//			// String version = dataSetProvider.getDataSetMD().getVersion();
//			// int id_plus_one = id + 1;
//			String[] results = new String[1];
//			// results[0] = id_plus_one + ": " + name + " " + version;
//			results[0] = name;
//			return results;
//		} else if (newFileMD) {
//			String[] results = new String[DataSetKeeper.size() + 1];
//			List<String> toSort = new ArrayList<String>();
//			// results[0] = ""; // RESERVING THIS FOR THE FIRST ENTRY (DEFAULT =
//			// // NEW)
//			List<Integer> ids = DataSetKeeper.getIDs();
//			Iterator<Integer> iterator = ids.iterator();
//
//			Integer id = 0;
//			// int id_plus_one = id + 1;
//			int counter = 0;
//			while (iterator.hasNext()) {
//				counter++;
//				id = iterator.next();
//				// id_plus_one = id + 1;
//				DataSetProvider dsProvider = DataSetKeeper.get(id);
//				Resource tdbResource = dsProvider.getTdbResource();
//				if (model.contains(tdbResource, RDFS.label)) {
//					String name = model
//							.listObjectsOfProperty(tdbResource, RDFS.label)
//							.next().asLiteral().getString();
//					toSort.add(name);
//				}
//
//				// if (model.contains(tdbResource, DCTerms.hasVersion)) {
//				// version = model.listObjectsOfProperty(tdbResource,
//				// DCTerms.hasVersion).next().asLiteral().getString();
//				// } else if (model.contains(tdbResource,
//				// ECO.hasMajorVersionNumber)) {
//				// version = model.listObjectsOfProperty(tdbResource,
//				// ECO.hasMajorVersionNumber).next().asLiteral().getString();
//				// if (model.contains(tdbResource, ECO.hasMinorVersionNumber)) {
//				// version += "." + model.listObjectsOfProperty(tdbResource,
//				// ECO.hasMinorVersionNumber).next().asLiteral().getString();
//				// }
//				// }
//				// results[counter] = id_plus_one + ":" + name + " " + version;
//				// results[counter] = name;
//			}
//			Collections.sort(toSort);
//			// Integer next = id_plus_one + 1;
//			results[0] = "(new data set)";
//			for (int i = 0; i < toSort.size(); i++) {
//				results[i + 1] = toSort.get(i);
//			}
//			return results;
//		} else {
			// if (DataSetKeeper.size() == 0){return null;}
			String[] results = new String[DataSetKeeper.size()];
			List<String> toSort = new ArrayList<String>();

			// results[0] = ""; // RESERVING THIS FOR THE FIRST ENTRY (DEFAULT =
			// // NEW)
			List<Integer> ids = DataSetKeeper.getIDs();
			Iterator<Integer> iterator = ids.iterator();

			Integer id = 0;
			// int id_plus_one = id + 1;
			// int counter = -1;
			while (iterator.hasNext()) {
				// counter++;
				id = iterator.next();
				// id_plus_one = id + 1;
				DataSetProvider dsProvider = DataSetKeeper.get(id);
				Resource tdbResource = dsProvider.getTdbResource();
				// String name = "";
				// String version = "";
				if (model.contains(tdbResource, RDFS.label)) {
					String name = model
							.listObjectsOfProperty(tdbResource, RDFS.label)
							.next().asLiteral().getString();
					toSort.add(name);
				}
			}
			Collections.sort(toSort);

			// Integer next = id_plus_one + 1;
			// results[0] = next + ": (new data set)";
			for (int i = 0; i < toSort.size(); i++) {
				results[i] = toSort.get(i);
			}
			return results;
		}
//	}

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
		// int dsNum = Integer.parseInt(dataSetChosen.split(":")[0]) - 1; //
		// SUBTRACT
		// 1 !!
		System.out.println("Got data set: " + dataSetChosen);
		// if ()
		if (dataSetChosen.endsWith("new data set)")) {
			newDataSet = true;
			dataSetProvider = tempDataSetProvider;
			fileMD = tempDataSetProvider.getFileMDList().get(0);
			System.out.println("... it is new");
		} else {
			newDataSet = false;
			int dsNum = DataSetKeeper.indexOfDataSetName(dataSetChosen);
			if (dsNum > -1) {
				dataSetProvider = DataSetKeeper.get(dsNum);
			} else {
				dataSetProvider.getDataSetMD().setName(dataSetChosen);
//				dataSetProvider = null;
//				System.out.println("What happened?");
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
//		if (!newDataSet && newFileMD) { // NOT A NEW DATA SET, BUT A NEW FILE
//										// NAME (TO ADD, PRESUMABLY)
//			tempFileMDList.add(tempDataSetProvider.getFileMDList().get(0));
//		}
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
			// dialogValues.get(3).setText(dataSetMD.getName()); //FIXME
			combo.setText(dataSetMD.getName());
			dialogValues.get(3).setText(dataSetMD.getVersion());
			dialogValues.get(4).setText(dataSetMD.getComments());
			dialogValues.get(5).setText(dataSetMD.getContactName());
			dialogValues.get(6).setText(dataSetMD.getContactAffiliation());
			dialogValues.get(7).setText(dataSetMD.getContactEmail());
			dialogValues.get(8).setText(dataSetMD.getContactPhone());

		}
		if (curatorMD != null) {
			System.out.println("curatorMD.getName: = " + curatorMD.getName());
			dialogValues.get(9).setText(curatorMD.getName());
			dialogValues.get(10).setText(curatorMD.getAffiliation());
			dialogValues.get(11).setText(curatorMD.getEmail());
			dialogValues.get(12).setText(curatorMD.getPhone());
			dialogValues.get(9).setEnabled(false);
			dialogValues.get(10).setEnabled(false);
			dialogValues.get(11).setEnabled(false);
			dialogValues.get(12).setEnabled(false);

			
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
