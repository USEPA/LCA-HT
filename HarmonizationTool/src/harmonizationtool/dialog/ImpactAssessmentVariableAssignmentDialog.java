package harmonizationtool.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.model.CuratorMD;
import harmonizationtool.model.DataSourceKeeper;
import harmonizationtool.model.DataSourceMD;
import harmonizationtool.model.DataSourceProvider;
import harmonizationtool.model.FileMD;
import harmonizationtool.model.ModelProvider;
import harmonizationtool.utils.Util;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.wb.swt.SWTResourceManager;

public class ImpactAssessmentVariableAssignmentDialog extends TitleAreaDialog {

	public ModelProvider modelProvider = null;
	private boolean newFileMD = false;
	private boolean newDataSet = false;
	private boolean dataSetEnabled = true;
	private DataSourceProvider dataSourceProvider = null;
	private DataSourceProvider tempDataSetProvider = null;
	private FileMD fileMD = null;
	private DataSourceMD dataSourceMD = null;
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
	public ImpactAssessmentVariableAssignmentDialog(Shell parentShell, FileMD fileMD) {
		super(parentShell);
		newFileMD = true;
		newDataSet = true;
		dataSetEnabled = true;
		assert fileMD != null : "fileMD cannot be null";
		this.fileMD = fileMD; // SET LOCAL VERSION
		tempDataSetProvider = new DataSourceProvider();
		this.dataSourceMD = new DataSourceMD();
		tempDataSetProvider.setDataSourceMD(dataSourceMD);
		this.curatorMD = new CuratorMD(true);
		tempDataSetProvider.setCuratorMD(curatorMD);
		tempDataSetProvider.addFileMD(fileMD); // THIS MEANS WE DON'T HAVE TO
												// ADD IT AGAIN
		dataSourceProvider = tempDataSetProvider;
		// curatorFromPrefs();
	}

	// YOU CAN GET HERE WITH A DataSourceProvider
	// WITH DataSourceMD , CuratorMD , fileMDList , tdbResource
	public ImpactAssessmentVariableAssignmentDialog(Shell parentShell, DataSourceProvider dataSourceProvider) {
		super(parentShell);
		newFileMD = false;
		newDataSet = false;
		dataSetEnabled = false;
		this.dataSourceProvider = dataSourceProvider;
		dataSourceMD = dataSourceProvider.getDataSourceMD();
		curatorMD = dataSourceProvider.getCuratorMD();
		tdbResource = dataSourceProvider.getTdbResource();
		fileMD = dataSourceProvider.getFileMDList().get(0);
	}

	// YOU CAN GET HERE WITH A FileMD AND A DataSourceProvider
	// WITH DataSourceMD , CuratorMD , fileMDList , tdbResource
	public ImpactAssessmentVariableAssignmentDialog(Shell parentShell, FileMD fileMD,
			DataSourceProvider dataSourceProvider) {
		super(parentShell);
		newFileMD = false;
		newDataSet = false;
		dataSetEnabled = true;
		this.dataSourceProvider = dataSourceProvider;
		dataSourceMD = dataSourceProvider.getDataSourceMD();
		curatorMD = dataSourceProvider.getCuratorMD();
		tdbResource = dataSourceProvider.getTdbResource();
		this.fileMD = fileMD;
	}

	public ImpactAssessmentVariableAssignmentDialog(Shell parentShell) {
		super(parentShell);
		newFileMD = false;
		newDataSet = false;
		if (DataSourceKeeper.size() == 0) {
			new GenericMessageBox(
					parentShell,
					"No Data Sets",
					"The HT does not contain any DataSets at this time.  Read a CSV or RDF file to create some.");
			return;
		}
		this.dataSourceProvider = DataSourceKeeper.get(0);
		dataSourceMD = dataSourceProvider.getDataSourceMD();
		curatorMD = dataSourceProvider.getCuratorMD();
		tdbResource = dataSourceProvider.getTdbResource();
	}

	// MAKE THE WHOLE DIALOG BOX
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Impact Assessment Variable Assignment");

		// CREATE THE COMPOSITE
		Composite composite = new Composite(parent, SWT.NONE);
		// composite.setBounds(0, 0, 600, 1200);
		composite.setLayout(null);
		int col1Left = 5;
		int col1LeftIndent = 25;
		int col1Width = 200;
		int col2Left = col1LeftIndent+col1Width;
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
		lbl_5b.setText("General Variables:");

		rowIndex++;
		Label lbl_01 = new Label(composite, SWT.RIGHT);
		lbl_01.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
				rowHeight);
		// lblAssociatedDataSet.setBounds(0, 0, 400, 14);
		lbl_01.setText("Impact Assessment (IA) Method");

		// ADD THE DATA SET CHOOSER PULL DOWN
		combo = new Combo(composite, SWT.DROP_DOWN);
		combo.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

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
		}
		combo.setEnabled(dataSetEnabled);

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
		lbl_07.setText("IA Category");
		Text text_07 = new Text(composite, SWT.BORDER);
		text_07.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
				rowHeight);

		rowIndex++;
		Label lbl_08 = new Label(composite, SWT.RIGHT);
		lbl_08.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
				rowHeight);
		lbl_08.setText("IA Category Indicator");
		Text text_08 = new Text(composite, SWT.BORDER);
		text_08.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
				rowHeight);

		rowIndex++;
		Label lbl_09 = new Label(composite, SWT.RIGHT);
		lbl_09.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
				rowHeight);
		lbl_09.setText("IA Category Reference Unit");
		Text text_09 = new Text(composite, SWT.BORDER);
		text_09.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
				rowHeight);

		rowIndex++;
		Label lbl_10 = new Label(composite, SWT.RIGHT);
		lbl_10.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
				rowHeight);
		lbl_10.setText("IA Characterization Model");
		Text text_10 = new Text(composite, SWT.BORDER);
		text_10.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
				rowHeight);

		rowIndex++;
		Label lbl_11 = new Label(composite, SWT.RIGHT);
		lbl_11.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
				rowHeight);
		lbl_11.setText("Contact Email");
		Text text_11 = new Text(composite, SWT.BORDER);
		text_11.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
				rowHeight);

//		rowIndex++;
//		Label lbl_12 = new Label(composite, SWT.RIGHT);
//		lbl_12.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width,
//				rowHeight);
//		lbl_12.setText("Contact Phone");
//		Text text_12 = new Text(composite, SWT.BORDER);
//		text_12.setBounds(col2Left, rowIndex * disBtwnRows, col2Width,
//				rowHeight);

		// NEXT STEP: ADD FileMD DATA
		rowIndex++;
		Label sep_01a = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep_01a.setBounds(50, rowIndex * disBtwnRows - 5, 250, 2);

		Label lbl_1b = new Label(composite, SWT.LEFT);
		lbl_1b.setFont(SWTResourceManager
				.getFont("Lucida Grande", 16, SWT.BOLD));
		lbl_1b.setBounds(col1Left, rowIndex * disBtwnRows, col1Width
				+ col2Width, rowHeight);
		lbl_1b.setText("Flow Category / Compartment Information:");

		rowIndex++;
		Label lbl_02 = new Label(composite, SWT.RIGHT);
		lbl_02.setText("Category (primary)");
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
		Button copyCuratorInfo = new Button(composite, SWT.BORDER);
		copyCuratorInfo
				.setToolTipText("Values for the Curator will be copied from curator info set in the preferences.");
		copyCuratorInfo.setBounds(col2Left, rowIndex * disBtwnRows, 250, 25);
		copyCuratorInfo.setText("Copy Info from Preferences");
		copyCuratorInfo.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				dialogValues.get(9).setText(
						Util.getPreferenceStore().getString("curatorName"));
				dialogValues.get(10).setText(
						Util.getPreferenceStore().getString("curatorAffiliation"));
				dialogValues.get(11).setText(
						Util.getPreferenceStore().getString("curatorEmail"));
				dialogValues.get(12).setText(
						Util.getPreferenceStore().getString("curatorPhone"));
			}
		});

		rowIndex++;
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
//		dialogValues.add(text_12); // 08 Data Set Contact Phone
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
		if (newFileMD) {
//			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
		return super.open();
	}

	@Override
	protected void cancelPressed() {

		super.cancelPressed();
	}

	@Override
	protected void okPressed() {

		// dataSourceMD.setName(dialogValues.get(3).getText()); //FIXME
		dataSourceMD.setName(combo.getText()); // FIXME
		dataSourceMD.setVersion(dialogValues.get(3).getText());
		dataSourceMD.setComments(dialogValues.get(4).getText());
		dataSourceMD.setContactName(dialogValues.get(5).getText());
		dataSourceMD.setContactAffiliation(dialogValues.get(6).getText());
		dataSourceMD.setContactEmail(dialogValues.get(7).getText());
		dataSourceMD.setContactPhone(dialogValues.get(8).getText());

		// curatorMD META DATA
		curatorMD.setName(dialogValues.get(9).getText());
		curatorMD.setAffiliation(dialogValues.get(10).getText());
		curatorMD.setEmail(dialogValues.get(11).getText());
		curatorMD.setPhone(dialogValues.get(12).getText());

		dataSourceProvider.setDataSourceMD(dataSourceMD);
		dataSourceProvider.setCuratorMD(curatorMD);
		// Model model = ActiveTDB.model;
		if (newDataSet) {
			DataSourceKeeper.add(dataSourceProvider); // A DataSourceProvider IS BORN!!
												// (NEW)
			dataSetProviderToTDB(dataSourceProvider);
			// int dataSetIdPlusOne = DataSourceKeeper.indexOf(dataSourceProvider) +
			// 1;
			// Resource newTDBResource = dataSourceProvider.getTdbResource();
			// model.addLiteral(newTDBResource, FEDLCA.localSerialNumber,
			// model.createTypedLiteral(dataSetIdPlusOne));
			// model.addLiteral(newTDBResource, RDFS.label,
			// model.createLiteral(dataSourceMD.getName()));
			// model.addLiteral(newTDBResource, RDFS.comment,
			// model.createLiteral(dataSourceMD.getComments()));
			// model.addLiteral(newTDBResource, DCTerms.hasVersion,
			// model.createLiteral(dataSourceMD.getVersion()));
		} else if (newFileMD) {
			dataSourceProvider.addFileMD(tempDataSetProvider.getFileMDList()
					.get(0));
			dataSetProviderToTDB(dataSourceProvider);
		} else {
			dataSetProviderToTDB(dataSourceProvider);
		}

		super.okPressed();
	}

	private void dataSetProviderToTDB(DataSourceProvider dsProvider) {
		Model model = ActiveTDB.model;
		// int dataSetIdPlusOne = DataSourceKeeper.indexOf(dsProvider) + 1;
		Resource tdbResource = dsProvider.getTdbResource();
		// if (model.contains(tdbResource, FEDLCA.localSerialNumber)) {
		// NodeIterator nodeIterator = model.listObjectsOfProperty(
		// tdbResource, FEDLCA.localSerialNumber);
		// while (nodeIterator.hasNext()) {
		// RDFNode rdfNode = nodeIterator.next();
		// System.out.println("Is it literal? -- " + rdfNode.isLiteral());
		// model.remove(tdbResource, FEDLCA.localSerialNumber,
		// rdfNode.asLiteral());
		// // model.
		// }
		// }
		// model.addLiteral(tdbResource, FEDLCA.localSerialNumber,
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
				model.createLiteral(dataSourceMD.getName()));

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
				model.createLiteral(dataSourceMD.getComments()));

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
				model.createLiteral(dataSourceMD.getVersion()));

	}

	// private String[] getFileInfo() {
	// List<String> filenameList = new ArrayList<String>();
	// List<FileMD> fileList = dataSourceProvider.getFileMDList();
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
		if (!dataSetEnabled) {
			Integer id = DataSourceKeeper.indexOf(dataSourceProvider);
			String name = dataSourceProvider.getDataSourceMD().getName();
			// String version = dataSourceProvider.getDataSetMD().getVersion();
			// int id_plus_one = id + 1;
			String[] results = new String[1];
			// results[0] = id_plus_one + ": " + name + " " + version;
			results[0] = name;
			return results;
		} else if (newFileMD) {
			String[] results = new String[DataSourceKeeper.size() + 1];
			List<String> toSort = new ArrayList<String>();
			// results[0] = ""; // RESERVING THIS FOR THE FIRST ENTRY (DEFAULT =
			// // NEW)
			List<Integer> ids = DataSourceKeeper.getIDs();
			Iterator<Integer> iterator = ids.iterator();

			Integer id = 0;
			// int id_plus_one = id + 1;
			int counter = 0;
			while (iterator.hasNext()) {
				counter++;
				id = iterator.next();
				// id_plus_one = id + 1;
				DataSourceProvider dsProvider = DataSourceKeeper.get(id);
				Resource tdbResource = dsProvider.getTdbResource();
				if (model.contains(tdbResource, RDFS.label)) {
					String name = model
							.listObjectsOfProperty(tdbResource, RDFS.label)
							.next().asLiteral().getString();
					toSort.add(name);
				}

				// if (model.contains(tdbResource, DCTerms.hasVersion)) {
				// version = model.listObjectsOfProperty(tdbResource,
				// DCTerms.hasVersion).next().asLiteral().getString();
				// } else if (model.contains(tdbResource,
				// ECO.hasMajorVersionNumber)) {
				// version = model.listObjectsOfProperty(tdbResource,
				// ECO.hasMajorVersionNumber).next().asLiteral().getString();
				// if (model.contains(tdbResource, ECO.hasMinorVersionNumber)) {
				// version += "." + model.listObjectsOfProperty(tdbResource,
				// ECO.hasMinorVersionNumber).next().asLiteral().getString();
				// }
				// }
				// results[counter] = id_plus_one + ":" + name + " " + version;
				// results[counter] = name;
			}
			Collections.sort(toSort);
			// Integer next = id_plus_one + 1;
			results[0] = "(new data set)";
			for (int i = 0; i < toSort.size(); i++) {
				results[i + 1] = toSort.get(i);
			}
			return results;
		} else {
			// if (DataSourceKeeper.size() == 0){return null;}
			String[] results = new String[DataSourceKeeper.size()];
			List<String> toSort = new ArrayList<String>();

			// results[0] = ""; // RESERVING THIS FOR THE FIRST ENTRY (DEFAULT =
			// // NEW)
			List<Integer> ids = DataSourceKeeper.getIDs();
			Iterator<Integer> iterator = ids.iterator();

			Integer id = 0;
			// int id_plus_one = id + 1;
			// int counter = -1;
			while (iterator.hasNext()) {
				// counter++;
				id = iterator.next();
				// id_plus_one = id + 1;
				DataSourceProvider dsProvider = DataSourceKeeper.get(id);
				Resource tdbResource = dsProvider.getTdbResource();
				// String name = "";
				// String version = "";
				if (model.contains(tdbResource, RDFS.label)) {
					String name = model
							.listObjectsOfProperty(tdbResource, RDFS.label)
							.next().asLiteral().getString();
					toSort.add(name);
				}
				// if (model.contains(tdbResource, RDFS.label)) {
				// name = model.listObjectsOfProperty(tdbResource,
				// RDFS.label).next().asLiteral().getString();
				// }
				// if (model.contains(tdbResource, DCTerms.hasVersion)) {
				// version = model.listObjectsOfProperty(tdbResource,
				// DCTerms.hasVersion).next().asLiteral().getString();
				// } else if (model.contains(tdbResource,
				// ECO.hasMajorVersionNumber)) {
				// version = model.listObjectsOfProperty(tdbResource,
				// ECO.hasMajorVersionNumber).next().asLiteral().getString();
				// if (model.contains(tdbResource, ECO.hasMinorVersionNumber)) {
				// version += "." + model.listObjectsOfProperty(tdbResource,
				// ECO.hasMinorVersionNumber).next().asLiteral().getString();
				// }
				// }
				// // results[counter] = id_plus_one + ":" + name + " " +
				// version;
				// results[counter] = name;

			}
			Collections.sort(toSort);

			// Integer next = id_plus_one + 1;
			// results[0] = next + ": (new data set)";
			for (int i = 0; i < toSort.size(); i++) {
				results[i] = toSort.get(i);
			}
			return results;
		}
	}

	private void populateFileMeta(int index) {
		System.out.println("index = " + index);
		List<FileMD> fileList = dataSourceProvider.getFileMDList();
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
		dialogValues.get(0).setText(fileMD.getByteCount() + "");
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
			dataSourceProvider = tempDataSetProvider;
			fileMD = tempDataSetProvider.getFileMDList().get(0);
			System.out.println("... it is new");
		} else {
			newDataSet = false;
			int dsNum = DataSourceKeeper.indexOfDataSourceName(dataSetChosen);
			if (dsNum > -1) {
				dataSourceProvider = DataSourceKeeper.get(dsNum);
			} else {
				dataSourceProvider.getDataSourceMD().setName(dataSetChosen);
//				dataSourceProvider = null;
//				System.out.println("What happened?");
			}
		}

		List<FileMD> fileMDList = dataSourceProvider.getFileMDList();
		System.out.println("fileMDList has size: " + fileMDList.size());
		if (tempDataSetProvider != null) {
			fileMD = tempDataSetProvider.getFileMDList().get(0);
		} else if (fileMDList.size() > 0) {
			fileMD = fileMDList.get(0);
		} else {
			fileMD = null;
		}
		dataSourceMD = dataSourceProvider.getDataSourceMD();
		curatorMD = dataSourceProvider.getCuratorMD();
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
		tempFileMDList.addAll(dataSourceProvider.getFileMDList());
		fileMDComboMgr.setItems(tempFileMDList);
		fileMDComboMgr.setText(fileMD);

		if (fileMD != null) {
			// dialogValues.get(0).setText(fileMD.getFilename());
			fileMDCombo.setToolTipText(fileMD.getPath());
			dialogValues.get(0).setText(fileMD.getByteCount() + "");
			dialogValues.get(1).setText(
					Util.getLocalDateFmt(fileMD.getLastModified()));
			dialogValues.get(2).setText(
					Util.getLocalDateFmt(fileMD.getReadTime()));
		}
		if (dataSourceMD != null) {
			System.out.println("dataSourceMD.getName: = " + dataSourceMD.getName());
			// dialogValues.get(3).setText(dataSourceMD.getName()); //FIXME
			combo.setText(dataSourceMD.getName());
			dialogValues.get(3).setText(dataSourceMD.getVersion());
			dialogValues.get(4).setText(dataSourceMD.getComments());
			dialogValues.get(5).setText(dataSourceMD.getContactName());
			dialogValues.get(6).setText(dataSourceMD.getContactAffiliation());
			dialogValues.get(7).setText(dataSourceMD.getContactEmail());
			dialogValues.get(8).setText(dataSourceMD.getContactPhone());

		}
		if (curatorMD != null) {
			System.out.println("curatorMD.getName: = " + curatorMD.getName());
			dialogValues.get(9).setText(curatorMD.getName());
			dialogValues.get(10).setText(curatorMD.getAffiliation());
			dialogValues.get(11).setText(curatorMD.getEmail());
			dialogValues.get(12).setText(curatorMD.getPhone());
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
