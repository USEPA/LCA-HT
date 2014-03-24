package harmonizationtool.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import harmonizationtool.comands.SelectTDB;
import harmonizationtool.model.CuratorMD;
import harmonizationtool.model.DataSetKeeper;
import harmonizationtool.model.DataSetMD;
import harmonizationtool.model.DataSetProvider;
import harmonizationtool.model.FileMD;
import harmonizationtool.model.ModelProvider;
import harmonizationtool.utils.Util;
import harmonizationtool.vocabulary.ECO;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.DCTerms;
import org.eclipse.swt.widgets.Button;
import org.eclipse.wb.swt.SWTResourceManager;

public class DataSetDeleteDialog extends TitleAreaDialog {

	public ModelProvider modelProvider = null;
	private DataSetProvider curDataSetProvider = null;
	private DataSetProvider callingDataSetProvider = null;
	private FileMD callingFileMD = null;
	private Combo comboDataSetSelector = null;
	private Combo comboFileSelector = null;
	private List<Text> dialogValues = new ArrayList<Text>();
	private ComboFileSelectorListener comboFileSelectorListener;
	protected String comboTextSaved = "";
	protected boolean comboKeyHeldDown = false;
	private int comboSelectionIndex = -1;

	// THERE ARE THREE WAYS TO CALL THIS:
	// CASE 1) CHOOSE A DATA SET TO DELETE
	// CASE 2) DELETE A SPECIFIC DATA SET

	/**
	 * @wbp.parser.constructor
	 */
	public DataSetDeleteDialog(Shell parentShell) {
		super(parentShell);
		// CASE 1 - CHOOSE A DATA SET TO DELETE
		if (DataSetKeeper.size() == 0) {
			new GenericMessageBox(parentShell, "No Data Sets", "The HT does not contain any DataSets at this time.  Read a CSV or RDF file to create some.");
			return;
		}
		if (DataSetKeeper.size() == 0) {
			return;
		}
		this.curDataSetProvider = DataSetKeeper.get(0);
	}

	public DataSetDeleteDialog(Shell parentShell, DataSetProvider dataSetProvider) {
		// CASE 2 - DELETE A SPECIFIC DATA SET
		super(parentShell);
		this.callingDataSetProvider = dataSetProvider;
		this.curDataSetProvider = callingDataSetProvider;
		// this.curFileMD = callingFileMD;
	}

	// MAKE THE WHOLE DIALOG BOX
	@Override
	protected Control createDialogArea(Composite parent) {
		System.out.println("createDialogArea called");
		setTitle("CSV file Meta Data");

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
		lbl_5b.setFont(SWTResourceManager.getFont("Lucida Grande", 16, SWT.BOLD));
		lbl_5b.setBounds(col1Left, rowIndex * disBtwnRows, col1Width + col2Width, rowHeight);
		lbl_5b.setText("Data Set Information:");

		rowIndex++;
		Label lbl_01 = new Label(composite, SWT.RIGHT);
		lbl_01.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		// lblAssociatedDataSet.setBounds(0, 0, 400, 14);
		lbl_01.setText("Name");

		// ADD THE DATA SET CHOOSER PULL DOWN
		comboDataSetSelector = new Combo(composite, SWT.READ_ONLY);

		comboDataSetSelector.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		comboDataSetSelector.setItems(getDataSetInfo());

		rowIndex++;
		Label lbl_07 = new Label(composite, SWT.RIGHT);
		lbl_07.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_07.setText("Version");
		Text text_07 = new Text(composite, SWT.BORDER);
		text_07.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_08 = new Label(composite, SWT.RIGHT);
		lbl_08.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_08.setText("Comments");
		Text text_08 = new Text(composite, SWT.BORDER);
		text_08.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_09 = new Label(composite, SWT.RIGHT);
		lbl_09.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_09.setText("Contact Name");
		Text text_09 = new Text(composite, SWT.BORDER);
		text_09.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_10 = new Label(composite, SWT.RIGHT);
		lbl_10.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_10.setText("Contact Affiliation");
		Text text_10 = new Text(composite, SWT.BORDER);
		text_10.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_11 = new Label(composite, SWT.RIGHT);
		lbl_11.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_11.setText("Contact Email");
		Text text_11 = new Text(composite, SWT.BORDER);
		text_11.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_12 = new Label(composite, SWT.RIGHT);
		lbl_12.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_12.setText("Contact Phone");
		Text text_12 = new Text(composite, SWT.BORDER);
		text_12.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		// NEXT STEP: ADD FileMD DATA
		rowIndex = 8;
		Label sep_01a = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep_01a.setBounds(50, rowIndex * disBtwnRows - 5, 250, 2);

		Label lbl_1b = new Label(composite, SWT.LEFT);
		lbl_1b.setFont(SWTResourceManager.getFont("Lucida Grande", 16, SWT.BOLD));
		lbl_1b.setBounds(col1Left, rowIndex * disBtwnRows, col1Width + col2Width, rowHeight);
		lbl_1b.setText("File Information:");

		rowIndex++;
		Label lbl_02 = new Label(composite, SWT.RIGHT);
		lbl_02.setText("Name");
		lbl_02.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		comboFileSelector = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		// comboFileSelectorMgr = new ComboFileSelectorMgr();
		createComboFileSelectorList();
		comboFileSelector.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		// NEXT STEP: COLLECT FILE LIST INFO BASED ON WHAT IS PASSED, AND ADD
		// OTHER
		comboFileSelector.setToolTipText("Files associated with this data set." + comboDataSetSelector.getText());
		comboFileSelectorListener = new ComboFileSelectorListener();
		comboFileSelector.addModifyListener(comboFileSelectorListener);

		rowIndex++;
		Label lbl_03 = new Label(composite, SWT.RIGHT);
		lbl_03.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_03.setText("Size (bytes)");
		Text text_03 = new Text(composite, SWT.BORDER);
		text_03.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		text_03.setEditable(false);
		text_03.setBackground(new Color(Display.getCurrent(), 255, 255, 255));

		rowIndex++;
		Label lbl_04 = new Label(composite, SWT.RIGHT);
		lbl_04.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_04.setText("Last Modified");
		Text text_04 = new Text(composite, SWT.BORDER);
		text_04.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		text_04.setEditable(false);
		text_04.setBackground(new Color(Display.getCurrent(), 255, 255, 255));

		rowIndex++;
		Label lbl_05 = new Label(composite, SWT.RIGHT);
		lbl_05.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_05.setText("Read Time");
		Text text_05 = new Text(composite, SWT.BORDER);
		text_05.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);
		text_05.setEditable(false);
		text_05.setBackground(new Color(Display.getCurrent(), 255, 255, 255));

		rowIndex++;
		Label sep_12a = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep_12a.setBounds(60, rowIndex * disBtwnRows - 5, 250, 2);

		Label lbl_12b = new Label(composite, SWT.LEFT);
		lbl_12b.setFont(SWTResourceManager.getFont("Lucida Grande", 16, SWT.BOLD));
		lbl_12b.setBounds(5, rowIndex * disBtwnRows, col1Width + col2Width, 20);
		lbl_12b.setText("Curator Information:");

		rowIndex++;
		Label lbl_13 = new Label(composite, SWT.RIGHT);
		lbl_13.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_13.setText("Name");
		Text text_13 = new Text(composite, SWT.BORDER);
		text_13.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_14 = new Label(composite, SWT.RIGHT);
		lbl_14.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, 20);
		lbl_14.setText("Affiliation");
		Text text_14 = new Text(composite, SWT.BORDER);
		text_14.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_15 = new Label(composite, SWT.RIGHT);
		lbl_15.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, 20);
		lbl_15.setText("Email");
		Text text_15 = new Text(composite, SWT.BORDER);
		text_15.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

		rowIndex++;
		Label lbl_16 = new Label(composite, SWT.RIGHT);
		lbl_16.setBounds(col1LeftIndent, rowIndex * disBtwnRows, col1Width, rowHeight);
		lbl_16.setText("Phone");
		Text text_16 = new Text(composite, SWT.BORDER);
		text_16.setBounds(col2Left, rowIndex * disBtwnRows, col2Width, rowHeight);

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

		comboDataSetSelector.select(0);
		comboSelectionIndex = comboDataSetSelector.getSelectionIndex();

		redrawDialogRows();

		Control control = super.createDialogArea(parent);
		control.setToolTipText("");
		return control;
	}

	// @Override
	// public int open() {
	// if (callingFileMD != null) {
	// // getButton(IDialogConstants.OK_ID).setEnabled(false);
	// }
	// return super.open();
	// }

	@Override
	protected void cancelPressed() {

		super.cancelPressed();
	}

	@Override
	protected void okPressed() {
		int index = DataSetKeeper.indexOfDataSetName(comboDataSetSelector.getText());
		// CONFIRM DELETION OF DATASET
		DataSetProvider dataSetProvider = DataSetKeeper.get(index);
		Resource tdbResource = dataSetProvider.getTdbResource();
		DataSetKeeper.remove(dataSetProvider);
		Model model = SelectTDB.model;
//		for (StmtIterator k = tdbResource.listProperties(); k.hasNext(); )
		StmtIterator stmtIterator = tdbResource.listProperties();
		List<Property> toKill = new ArrayList<Property>();
		while (stmtIterator.hasNext()){
			Statement statement = stmtIterator.next();
//			System.out.println("statement.getSubject() = " +statement.getSubject());
			System.out.println("statement.getPredicate() = " +statement.getPredicate());
			toKill.add(statement.getPredicate());
		}
		for (Property prop: toKill){
			NodeIterator nodeIterator = model.listObjectsOfProperty(tdbResource, prop);
			while (nodeIterator.hasNext()){
				RDFNode node = nodeIterator.next();
				model.removeAll(tdbResource, prop, node);
			}
		}
		toKill.clear();
		ResIterator iterator = model.listSubjectsWithProperty(ECO.hasDataSource, tdbResource);
		while (iterator.hasNext()){
			Resource resource = iterator.next();
			StmtIterator stmtIterator2 = resource.listProperties();
			while (stmtIterator2.hasNext()){
				Statement statement = stmtIterator2.next();
				toKill.add(statement.getPredicate());
//				model.remove(statement);
			}
			for (Property prop: toKill){
				NodeIterator nodeIterator = model.listObjectsOfProperty(resource, prop);
				while (nodeIterator.hasNext()){
					RDFNode node = nodeIterator.next();
					model.removeAll(resource, prop, node);
				}
			}
		}


		super.okPressed();
	}

	// COLLECT INFO ABOUT DATA SETS FROM THE TDB
	private String[] getDataSetInfo() {
		List<String> toSort = new ArrayList<String>();
		if (callingDataSetProvider != null) {
			String[] results = new String[1];
			results[0] = callingDataSetProvider.getDataSetMD().getName();
			return results;
		} else {
			List<Integer> ids = DataSetKeeper.getIDs();
			Iterator<Integer> iterator = ids.iterator();

			while (iterator.hasNext()) {
				int id = iterator.next();
				toSort.add(DataSetKeeper.get(id).getDataSetMD().getName());
			}
			Collections.sort(toSort);
		}
		
		if ((callingFileMD != null) && (callingDataSetProvider == null)) {
			String[] results = new String[DataSetKeeper.size() + 1];
			results[0] = "(new data set)";
			for (int i = 0; i < toSort.size(); i++) {
				results[i + 1] = toSort.get(i);
			}
			return results;
		} else {
			String[] results = new String[DataSetKeeper.size()];
			for (int i = 0; i < toSort.size(); i++) {
				results[i] = toSort.get(i);
			}
			curDataSetProvider = DataSetKeeper.get(DataSetKeeper.indexOfDataSetName(results[0]));
			return results;
		}
	}

	protected void populateDataSetMD() {
		String selectedDataSetName = comboDataSetSelector.getText();
		int selectedDataSetID = DataSetKeeper.indexOfDataSetName(selectedDataSetName);
		if (selectedDataSetID > -1) {
			curDataSetProvider = DataSetKeeper.get(selectedDataSetID);
		}

		redrawDialogRows();
	}

	protected void redrawDialogDataSetMD() {
		DataSetMD dataSetMD = curDataSetProvider.getDataSetMD();
		System.out.println("dataSetMD.getName: = " + dataSetMD.getName());
		dialogValues.get(3).setText(dataSetMD.getVersion());
		dialogValues.get(4).setText(dataSetMD.getComments());
		dialogValues.get(6).setText(dataSetMD.getContactAffiliation());
		dialogValues.get(7).setText(dataSetMD.getContactEmail());
		dialogValues.get(8).setText(dataSetMD.getContactPhone());
	}

	protected void redrawDialogFileMD() {
		FileMD curFileMD = callingFileMD; // MAY BE NULL
		int index = comboFileSelector.getSelectionIndex();
		if (index >= 0) {
//			if (curDataSetProvider != null) {
				if (callingFileMD != null) {
					if (index > 0) {
						curFileMD = curDataSetProvider.getFileMDList().get(index - 1);
					}
				} else {
					curFileMD = curDataSetProvider.getFileMDList().get(index);
				}
//			}
		}
		if (curFileMD == null) {
			dialogValues.get(0).setText("");
			dialogValues.get(1).setText("");
			dialogValues.get(2).setText("");
		} else {
			comboFileSelector.setToolTipText(curFileMD.getPath());
			dialogValues.get(0).setText(curFileMD.getSize() + "");
			dialogValues.get(1).setText(Util.getLocalDateFmt(curFileMD.getLastModified()));
			dialogValues.get(2).setText(Util.getLocalDateFmt(curFileMD.getReadTime()));
		}
	}

	protected void redrawDialogCuratorMD() {
		CuratorMD curatorMD = curDataSetProvider.getCuratorMD();
		System.out.println("curatorMD.getName: = " + curatorMD.getName());
		dialogValues.get(9).setText(curatorMD.getName());
		dialogValues.get(10).setText(curatorMD.getAffiliation());
		dialogValues.get(11).setText(curatorMD.getEmail());
		dialogValues.get(12).setText(curatorMD.getPhone());
	}

	protected void clearDialogRows() {
		Iterator<Text> dialogValueIterator = dialogValues.iterator();
		while (dialogValueIterator.hasNext()) {
			Text dialogValue = dialogValueIterator.next();
			dialogValue.setText("");
		}
	}

	protected void redrawDialogRows() {
		clearDialogRows();

		redrawDialogDataSetMD();

		createComboFileSelectorList();
		redrawDialogFileMD();

		redrawDialogCuratorMD();
	}

	protected void createComboFileSelectorList() {
		comboFileSelector.removeAll();
		if (curDataSetProvider == null) {
			if (callingFileMD != null) {
				comboFileSelector.add(callingFileMD.getFilename());
			}
			return;
		}
		List<FileMD> fileMDList = curDataSetProvider.getFileMDList();
		int selectionIndex = 0;
		if ((callingFileMD != null) && (callingDataSetProvider == null)) {
			comboFileSelector.add(callingFileMD.getFilename());
		}
		for (int i = 0; i < fileMDList.size(); i++) {
			FileMD fileMD = fileMDList.get(i);
			comboFileSelector.add(fileMD.getFilename());
			if (callingFileMD == fileMD) {
				selectionIndex = i;
			}
		}
		comboFileSelector.select(selectionIndex);
	}

	public class ComboFileSelectorListener implements ModifyListener {

		@Override
		public void modifyText(ModifyEvent e) {
			System.out.println("ModifyEvent=" + e.toString());
			System.out.println("fileMDCombo index " + comboFileSelector.getSelectionIndex());
			redrawDialogFileMD();
			System.out.println("choice is " + comboFileSelector.getSelectionIndex() + " with value: " + comboFileSelector.getText());
		}

	}
}