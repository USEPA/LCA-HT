package gov.epa.nrmrl.std.lca.ht.csvFiles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import gov.epa.nrmrl.std.lca.ht.dataModels.QACheck;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
//import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.DataSetKeeper;
import harmonizationtool.model.Issue;
import harmonizationtool.model.Status;
import harmonizationtool.model.TableKeeper;
import harmonizationtool.model.TableProvider;
import harmonizationtool.vocabulary.ECO;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author tec
 * 
 */
public class CSVTableView extends ViewPart {
	public CSVTableView() {
	}

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.csvFiles.csvTableView";

	private static String key = null;
	private TableViewer tableViewer;

	private static CSVColumnInfo[] assignedCSVColumnInfo;
	private static Table table;

	private static List<TableViewerColumn> tableViewerColumns = new ArrayList<TableViewerColumn>();
	private static List<CSVColumnInfo> availableCSVColumnInfo = new ArrayList<CSVColumnInfo>();
	private static String csvColumnDefaultTooltip = "Column ignored";
	private static Menu headerMenu;
	private static Menu rowMenu;
	// private static String formerlySelectedHeaderMenuItem;

	private static List<Integer> rowsSelected = new ArrayList<Integer>();
	public static List<Integer> rowsToIgnore = new ArrayList<Integer>();

	// private static TableViewerColumn tableViewerColumnSelected = null;
	private static TableColumn tableColumnSelected = null;

	private static Color gray = new Color(Display.getCurrent(), 128, 128, 128);
	private static Color black = new Color(Display.getCurrent(), 0, 0, 0);
	private static Color green = new Color(Display.getCurrent(), 128, 255, 255);

	// public static final String IMPACT_ASSESSMENT_METHOD_HDR =
	// "Impact Assessment Method";
	// // e.g. ReCiPe or TRACI
	//
	// public static final String IMPACT_CAT_HDR = "Impact Category";
	// // e.g. climate change
	// // public static final Resource ImpactCategory = ECO.ImpactCategory;
	//
	// public static final String IMPACT_CAT_INDICATOR_HDR =
	// "Impact category indicator";
	// // e.g. infrared radiative forcing
	//
	// public static final String IMPACT_CHARACTERIZATION_MODEL_HDR =
	// "Impact Characterization Model"; // e.g.
	// // IPCC Global Warming Potential (GWP)
	//
	// public static final String IMPACT_DIR_HDR = "Impact Direction";
	// // e.g. Resource , From , Emission , Uptake, etc.
	//
	// public static final String IMPACT_CAT_REF_UNIT_HDR =
	// "Impact cat ref unit";
	// // e.g. kg CO2 eq
	// //
	// public static final String CAT1_HDR = "Category"; // e.g. air
	// public static final String CAT2_HDR = "Subcategory"; // e.g. low
	// population
	// public static final String CAT3_HDR = "Sub-subcategory";
	// //
	// public static final String NAME_HDR = "Flowable Name";
	// public static final String CASRN_HDR = "CASRN";
	// public static final String ALT_NAME_HDR = "Flowable Alt_Name";
	// //
	// // // ECO.ImpactCharacterizationFactor;
	// // public static final String CHAR_FACTOR_HDR =
	// "Characterization factor";
	// // // THIS IS THE (float) NUMBER
	// //
	// // public static final String FLOW_UNIT_HDR = "Flow Unit";
	// // // e.g. kg
	// //
	// public static final String FLOW_PROPERTY_HDR = "Flow Property";
	// // e.g. mass
	//
	// public static final String IGNORE_HDR = "Ignore";

	// @Override
	// public void dispose() {
	// super.dispose();
	// }

	// private static TableViewerColumn getTableViewerColumnFromTableColumn(TableColumn
	// tableColumn){
	// for (TableViewerColumn tableViewerColumn: tableViewerColumns){
	// TableColumn candidateTableColumn = tableViewerColumn.getColumn();
	// if (candidateTableColumn.equals(tableColumn)){
	// return tableViewerColumn;
	// }
	// }
	// return null;
	// }

	private static SelectionListener colSelectionListener = new SelectionListener() {

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource() instanceof TableColumn) {
				TableColumn col = (TableColumn) e.getSource();
				tableColumnSelected = col;
				System.out.println("(widgetSelected) tableColumnSelected set to " + col);
				headerMenu.setVisible(true);
			}
		};

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			if (e.getSource() instanceof TableColumn) {
				TableColumn col = (TableColumn) e.getSource();
				System.out.println("(widgetDefaultSelected) tableColumnSelected set to " + col);
				tableColumnSelected = col;
				headerMenu.setVisible(true);
			}
		}

	};

	//
	// private static Listener mouseListener = new Listener() {
	//
	// @Override
	// public void handleEvent(Event event) {
	// System.out.println("The person clicked on this cell with event " + event);
	// }
	//
	// };

	private static ISelectionChangedListener rowSelectionListener = new ISelectionChangedListener() {
		public void selectionChanged(final SelectionChangedEvent event) {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			Iterator iterator = selection.iterator();
			rowsSelected.clear();
			while (iterator.hasNext()) {
				int index = TableKeeper.getTableProvider(key).getIndex((DataRow) iterator.next());
				rowsSelected.add(index);
			}
			rowMenu.setVisible(true);
		}
	};

	// private static ISelectionChangedListener rowSelectionListener = new
	// ISelectionChangedListener() {
	// public void selectionChanged(final SelectionChangedEvent event) {
	// System.out.println("event = " + event);
	// IStructuredSelection selection = (IStructuredSelection)
	// event.getSelection();
	// Iterator iterator = selection.iterator();
	// rowsSelected.clear();
	// while (iterator.hasNext()) {
	// int index = TableKeeper.getTableProvider(key).getIndex((DataRow)
	// iterator.next());
	// rowsSelected.add(index);
	// }
	// rowMenu.setVisible(true);
	// }
	// };
	//
	// private static MouseListener cellSelectionListener = new MouseListener()
	// {
	//
	// @Override
	// public void mouseUp(MouseEvent e) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void mouseDown(MouseEvent e) {
	// System.out.println("Event e = " + e);
	// }
	//
	// @Override
	// public void mouseDoubleClick(MouseEvent e) {
	// // TODO Auto-generated method stub
	//
	// }
	// };

	@Override
	public void createPartControl(Composite parent) {
		// parent.setLayout(null);

		tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		table = tableViewer.getTable();
		// table.setBounds(0, 0, 650, 650);

		// headerMenu = new Menu(table);
		// initializeHeaderMenu();

		rowMenu = new Menu(table);
		setIgnoreRowMenu();

		tableViewer.addSelectionChangedListener(rowSelectionListener);

		// table.addListener(SWT.MouseDown, tableListener);
		// table.addMouseListener(mouseListener);

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		tableViewer.getControl().setFocus();
	}

	public void update(String key) {
		this.key = key;

		tableViewer.setContentProvider(new ArrayContentProvider());
		// final Table table = tableViewer.getTable();
		removeColumns(table);
		// createColumns(tableViewer);
		createColumns();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		tableViewer.setContentProvider(new ArrayContentProvider());
		TableProvider tableProvider = TableKeeper.getTableProvider(key);
		// System.out.println("tableProvider.getData()="+tableProvider.getData());
		// System.out.println("tableProvider.getData().toString()="+tableProvider.getData().toString());

		tableViewer.setInput(tableProvider.getData());
		assignedCSVColumnInfo = new CSVColumnInfo[table.getColumns().length];
		for (int i = 0; i < table.getColumns().length; i++) {
			assignedCSVColumnInfo[i] = null;
		}
		// viewer.refresh();

	}

	public void clearView(String key) {
		if ((key != null) && (key.equals(this.key))) {
			// final Table table = tableViewer.getTable();
			tableViewer.setInput(null);
			removeColumns();
			table.setHeaderVisible(false);
			table.setLinesVisible(false);
		}
	}

	/**
	 * removes tableViewerColumns from the given table
	 * 
	 * @param table
	 */
	private void removeColumns(Table table) {
		System.out.println(this.getClass().getName() + ".removeColumns(table)");
		table.setRedraw(false);
		while (table.getColumnCount() > 0) {
			table.getColumns()[0].dispose();
		}
		table.setRedraw(true);
	}

	private void removeColumns() {
		System.out.println(this.getClass().getName() + ".removeColumns(table)");
		table.setRedraw(false);
		while (table.getColumnCount() > 0) {
			table.getColumns()[0].dispose();
		}
		table.setRedraw(true);
	}

	private void createColumns() {
		System.out.println("key=" + key);
		if (key != null) {
			// Define the menu and assign to the table

			TableProvider tableProvider = TableKeeper.getTableProvider(key);
			// headerMenu = tableProvider.getMenu();
			// if (headerMenu == null) {
			// headerMenu = new Menu(table);
			initializeHeaderMenu();
			String defaultHeader = headerMenu.getItem(0).getText();
			// }
			// THE headerRow OF A TableProvider MUST BE POPULATED SO THAT...
			DataRow headerRow = tableProvider.getHeaderRow();
			List<DataRow> tableData = tableProvider.getData();
			DataRow dataRow = tableData.get(0);
			while (headerRow.getSize() < dataRow.getSize()) {
				headerRow.add(defaultHeader);
			}
			// int numCol = headerRow.getSize();
			// System.out.println("numCol = " + numCol);

			for (int i = 0; i < tableProvider.getColumnCount(); i++) {
				// if (headerRow.get(i) == null) {
				// headerRow.set(i, IGNORE_HDR);
				// }
				TableViewerColumn tableViewerColumn = createTableViewerColumn(defaultHeader, 100, i);
				tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(i));
				// tableProvider.addHeaderName(titlesArray[i],col.hashCode());
				tableViewerColumns.add(tableViewerColumn);
			}
			// saveColumnNames();
		}
	}

	/**
	 * class for generating column labels. This class will handle a variable number of
	 * tableViewerColumns
	 * 
	 * @author tec
	 */
	class MyColumnLabelProvider extends ColumnLabelProvider {
		private int myColNum;

		public MyColumnLabelProvider(int colNum) {
			// System.out.println("column was "+colNum);
			this.myColNum = colNum;
			// System.out.println("column now "+colNum);

		}

		// private int getTableViewerColumnIndex(){
		// return myColNum;
		// }

		@Override
		public String getText(Object element) {
			// System.out.println("getText from column: "+myColNum);
			DataRow dataRow = null;
			try {
				dataRow = (DataRow) element;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("element= " + element);
			}
			String s = "";
			try {
				int size = dataRow.getColumnValues().size();
				if (myColNum < size) {
					s = dataRow.getColumnValues().get(myColNum);
				}
			} catch (Exception e) {
				System.out.println("dataRow=" + dataRow);
				e.printStackTrace();
			}
			return s;
		}
	}

	/**
	 * convenience method for creating a TableViewerColumn
	 * 
	 * @param title
	 * @param bound
	 * @param colNumber
	 * @return
	 */
	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {

		final TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE, colNumber);
		final TableColumn tableColumn = tableViewerColumn.getColumn();
		// tableViewerColumn.setColumnNumber(colNumber);
		// tableViewerColumn.setColumn(tableColumn);
		tableColumn.setText(title);
		tableColumn.setWidth(bound);
		tableColumn.setResizable(true);
		tableColumn.setMoveable(false);
		tableColumn.setToolTipText(csvColumnDefaultTooltip);

		tableColumn.addSelectionListener(colSelectionListener);

		return tableViewerColumn;
	}

	//
	// public String getTitle(){
	// return "hello";
	// }

	/**
	 * this method initializes the headerMenu with menuItems and a HeaderMenuColumnSelectionListener
	 * 
	 * @param menu
	 *            headerMenu which allows user to rename the tableViewerColumns
	 */

	public void initializeHeaderMenu() {
		if (headerMenu != null) {
			headerMenu.dispose();
		}
		CSVColumnInfo ignoreColumnInfo = new CSVColumnInfo("Ignore", false, false, null);
		availableCSVColumnInfo.add(ignoreColumnInfo);

		headerMenu = new Menu(table);
		MenuItem menuItem;
		menuItem = new MenuItem(headerMenu, SWT.NORMAL);
		menuItem.setText(ignoreColumnInfo.getHeaderString());
		menuItem.addListener(SWT.Selection, new HeaderMenuColumnSelectionListener());
		new MenuItem(headerMenu, SWT.SEPARATOR);
	}

	public void appendToCSVColumnsInfo(CSVColumnInfo csvColumnInfo) {
		// csvColumnInfo.setIndexInTable(-1);
		// csvColumnInfo.setIssues(new ArrayList<Issue>());
		// csvColumnInfo.setStatus(Status.UNCHECKED);

		availableCSVColumnInfo.add(csvColumnInfo);

		MenuItem menuItem = new MenuItem(headerMenu, SWT.NORMAL);
		// System.out.println("added CSVColumnInfo.headerString = " +
		// csvColumnInfo.getHeaderString());
		menuItem.addListener(SWT.Selection, new HeaderMenuColumnSelectionListener());
		menuItem.setText(csvColumnInfo.getHeaderString());
		resetSelectionListener(menuItem);
	}

	// private void resetSelectionListener() {
	// for (MenuItem menuItem : headerMenu.getItems()) {
	// for (Listener listener : menuItem.getListeners(SWT.Selection)) {
	// menuItem.removeListener(SWT.Selection, listener);
	// }
	// }
	//
	// HeaderMenuColumnSelectionListener columnSelectionListener = new
	// HeaderMenuColumnSelectionListener();
	// for (MenuItem item : headerMenu.getItems()) {
	// item.addListener(SWT.Selection, columnSelectionListener);
	// }
	// }

	private void resetSelectionListener(MenuItem menuItem) {
		for (Listener listener : menuItem.getListeners(SWT.Selection)) {
			menuItem.removeListener(SWT.Selection, listener);
		}
		HeaderMenuColumnSelectionListener columnSelectionListener = new HeaderMenuColumnSelectionListener();
		menuItem.addListener(SWT.Selection, columnSelectionListener);
	}

	public void appendHeaderMenuDiv() {
		new MenuItem(headerMenu, SWT.SEPARATOR);
	}

	// public void appendToHeaderInfo(CSVColumnInfo csvColumnInfo) {
	// csvColumnsInfo.add(csvColumnInfo);
	// }
	//
	public void appendToCSVColumnsInfo(CSVColumnInfo[] csvColumnInfos) {
		for (CSVColumnInfo csvColumnInfo : csvColumnInfos) {
			appendToCSVColumnsInfo(csvColumnInfo);
		}
	}

	// private static void clearRowMenu() {
	// // while (rowMenu.getItemCount() >0){
	// rowMenu.setData(null);
	// // }
	// }

	private void setIgnoreRowMenu() {
		rowMenu.setData(null);

		RowSelectionListener tinyRowSelectionListener = new RowSelectionListener();

		MenuItem menuItem;

		menuItem = new MenuItem(rowMenu, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, tinyRowSelectionListener);
		// menuItem.addListener(SWT.Selection, (MouseListener)
		// cellSelectionListener);

		menuItem.setText("ignore rows");

		// new MenuItem(rowMenu, SWT.SEPARATOR); // ----------

		menuItem = new MenuItem(rowMenu, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, tinyRowSelectionListener);
		menuItem.setText("use rows");

	}

	// private void setFixCellRowMenu() {
	// // while (rowMenu.getItemCount() >0){
	// // rowMenu.getItem(0).dispose();
	// // }
	// rowMenu.setData(null);
	//
	// // RowSelectionListener rowSelectionListener = new
	// // RowSelectionListener();
	//
	// MenuItem menuItem;
	//
	// menuItem = new MenuItem(rowMenu, SWT.NORMAL);
	// // menuItem.addListener(SWT.Selection, rowSelectionListener);
	// menuItem.setText("ignore rows");
	//
	// // new MenuItem(rowMenu, SWT.SEPARATOR); // ----------
	//
	// menuItem = new MenuItem(rowMenu, SWT.NORMAL);
	// // menuItem.addListener(SWT.Selection, rowSelectionListener);
	// menuItem.setText("use rows");
	//
	// }

	// private void setIssueRowMenu(){
	// while (rowMenu.getItemCount() >0){
	// rowMenu.getItem(0).dispose();
	// }
	// MenuItem menuItem;
	//
	// // menuItem = new MenuItem(rowMenu, SWT.NORMAL);
	// // menuItem.addListener(SWT.Selection, rowSelectionListener);
	// // menuItem.setText("resolve issue");
	// //
	// // // new MenuItem(rowMenu, SWT.SEPARATOR); // ----------
	// //
	// // menuItem = new MenuItem(rowMenu, SWT.NORMAL);
	// // menuItem.addListener(SWT.Selection, rowSelectionListener);
	// // menuItem.setText("flag cell");
	//
	// }

	/**
	 * once the user has selected a column header for change this Listener will set the column
	 * header to the value selected by the user. If the user selects "Custom...", then a dialog is
	 * displayed so the user can enter a custom value for the column header.
	 * 
	 * @author tec 919-541-1500
	 * 
	 */
	private class HeaderMenuColumnSelectionListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			System.out.println("HeaderMenuColumnSelectionListener event = " + event);
			System.out.println("event.widget  = " + event.widget);

			if ((event.widget instanceof MenuItem) && (tableColumnSelected != null)) {
				MenuItem menuItem = (MenuItem) event.widget;
				if (menuItem.getText().equals(tableColumnSelected.getText())) {
					// NO ACTION REQUIRED, NOTHING CHANGED
					return;
				}

				// RESET PREVIOUS HEADER STRING FOR THE COLUMN SELECTED
				getMenuItemByHeaderString(tableColumnSelected.getText()).setEnabled(true);

				int columnIndex = table.indexOf(tableColumnSelected);
				String menuItemText = menuItem.getText();
				CSVColumnInfo csvColumnInfo = getCSVColumnInfoByHeaderString(menuItemText);

				if (csvColumnInfo == null) {
					// NOT POSSIBLE?!?
					return;
				}

				CSVColumnInfo csvColumnInfoClone = csvColumnInfo.duplicate();
				assignedCSVColumnInfo[columnIndex] = csvColumnInfoClone;

				if (csvColumnInfoClone.isUnique()) {
					menuItem.setEnabled(false);
				}

				tableColumnSelected.setText(menuItemText);
				if (headerMenu.indexOf(menuItem) == 0) {
					tableColumnSelected.setToolTipText(csvColumnDefaultTooltip);
				} else {
					tableColumnSelected.setToolTipText("Assigned");
				}

				// MenuItem[] menuItems = headerMenu.getItems();

				// for (MenuItem mi : menuItems) {
				// if (formerlySelectedHeaderMenuItem.equals(mi.getText())) {
				// mi.setEnabled(true);
				// break;
				// }
				// }

				// for (MenuItem mi : menuItems) {
				// String headerText = mi.getText();
				// boolean unique =
				// CsvTableViewerColumnType.isUnique(mi.getText());
				// if ((unique && (event.widget.equals(mi)))) {
				// mi.setEnabled(false);
				// break;
				// }
				// }

				// if (menuItemText != null) {
				// if (menuItemText.equals("Custom...")) {
				// // allow the user to define a custom header name
				// InputDialog inputDialog = new
				// InputDialog(getViewSite().getShell(), "Column Name Dialog",
				// "Enter a custom column label", "", null);
				// inputDialog.open();
				// int returnCode = inputDialog.getReturnCode();
				// if (returnCode == InputDialog.OK) {
				// String val = inputDialog.getValue();
				// columnSelected.setText(val);
				// }
				// } else {

				// System.out.println(tableColumnSelected ) // FIXME
				// }
				// System.out.println("Setting columnSelected text...");
				// columnSelected.setText(menuItemText);
				// System.out.println("ColumnSelected text is now: " + menuItemText);

				//
				// }
				// save the column names to the TableProvider in case the data
				// table needs to be
				// re-displayed
				// saveColumnNames();
				// TableKeeper.getTableProvider(key).setMenu(headerMenu);
				// exportColumnStatus();
			}
		}
	}

	private class RowSelectionListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			System.out.println("RowSelectionListener event = " + event);
			if (event.widget instanceof MenuItem) {
				String menuItemText = ((MenuItem) event.widget).getText();
				// MenuItem[] menuItems = rowMenu.getItems();

				if (menuItemText.equals("ignore rows")) {
					for (int tableIndex : rowsSelected) {
						tableViewer.getTable().getItem(tableIndex).setForeground(gray);
						if (!rowsToIgnore.contains(tableIndex)) {
							rowsToIgnore.add(tableIndex);
						}
					}
				} else if (menuItemText.equals("use rows")) {
					for (int tableIndex : rowsSelected) {
						tableViewer.getTable().getItem(tableIndex).setForeground(black);
						if (rowsToIgnore.contains(tableIndex)) {
							int indexToRemove = rowsToIgnore.indexOf(tableIndex);
							rowsToIgnore.remove(indexToRemove);
						}
					}
				}
				// saveColumnNames();
			}
		}

	}

	/**
	 * this method retrieves the column header text values from the column components and passes
	 * them to the TableProvider so they can be retrieved when the data table is re-displayed
	 */
	// private void saveColumnNames() {
	// List<String> columnNames = new ArrayList<String>();
	// // TableColumn[] tableColumns = tableViewer.getTable().getColumns();
	// TableColumn[] tableColumns = table.getColumns();
	// for (TableColumn tableColumn : tableColumns) {
	// columnNames.add(tableColumn.getText());
	// }
	// TableProvider tableProvider = TableKeeper.getTableProvider(key);
	// tableProvider.setHeaderNames(columnNames);
	// }

	public static int checkCols() {
		int totalIssueCount = 0;
		for (int colIndex = 0; colIndex < assignedCSVColumnInfo.length; colIndex++) {
			CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[colIndex];

			// for (CSVColumnInfo csvColumnInfo : columnAssignedCSVColInfo) {
			if (csvColumnInfo != null) {
				List<String> columnValues = getColumnValues(colIndex);
				for (QACheck qaCheck : csvColumnInfo.getCheckLists()) {
					for (int i = 0; i < columnValues.size(); i++) {
						String val = columnValues.get(i);

						Matcher matcher = qaCheck.getPattern().matcher(val);
						while (matcher.find()) {
							System.out.println("check.getIssue() " + qaCheck.getIssue());
							Issue issue = qaCheck.getIssue();
							issue.setRowNumber(i);
							issue.setColNumber(colIndex);
							issue.setRowNumber(i);
							issue.setCharacterPosition(matcher.end());
							issue.setStatus(Status.UNRESOLVED);

							Logger.getLogger("run").warn(issue.getDescription());
							Logger.getLogger("run").warn("  ->Row" + issue.getRowNumber());
							Logger.getLogger("run").warn("  ->Column" + issue.getColNumber());
							Logger.getLogger("run").warn("  ->Character position" + issue.getCharacterPosition());
							assignIssue(issue);
							csvColumnInfo.addIssue(issue);
							// table.getColumn(csvColumnInfo.getIndexInTable()).setToolTipText(csvColumnInfo.getIssueCount()
							// + " issues below");
						}
					}
				}
				int issuesInCol = csvColumnInfo.getIssueCount();
				table.getColumn(colIndex).setToolTipText(issuesInCol + " issues");
				totalIssueCount += issuesInCol;
			} else {
				table.getColumn(colIndex).setToolTipText(csvColumnDefaultTooltip);

			}
		}
		return totalIssueCount;
	}

	public static int autoFixColumn(int colIndex) {
		CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[colIndex];
		int issuesRemaining = 0;
		if (csvColumnInfo != null) {
			csvColumnInfo.clearIssues();
			List<String> columnValues = getColumnValues(colIndex);
			for (QACheck qaCheck : csvColumnInfo.getCheckLists()) {
				for (int i = 0; i < columnValues.size(); i++) {
					String val = columnValues.get(i);

					Matcher matcher = qaCheck.getPattern().matcher(val);
					if (qaCheck.getReplacement() != null) {
						matcher.replaceAll(qaCheck.getReplacement());
					}
					while (matcher.find()) {
						issuesRemaining++;
						// System.out.println("check.getIssue() " + qaCheck.getIssue());
						Issue issue = qaCheck.getIssue();
						issue.setRowNumber(i);
						issue.setColNumber(colIndex);
						issue.setRowNumber(i);
						issue.setCharacterPosition(matcher.end());
						issue.setStatus(Status.UNRESOLVED);

						Logger.getLogger("run").warn(issue.getDescription());
						Logger.getLogger("run").warn("  ->Row" + issue.getRowNumber());
						Logger.getLogger("run").warn("  ->Column" + issue.getColNumber());
						Logger.getLogger("run").warn("  ->Character position" + issue.getCharacterPosition());
						assignIssue(issue);
						csvColumnInfo.addIssue(issue);
						table.getColumn(colIndex).setToolTipText(csvColumnInfo.getIssueCount() + " issues below");
					}
				}
				// }
			}
		}
		return issuesRemaining;
	}

	public static int checkColumns() {
		int issuesFound = 0;
		int colIndex = -1;
		for (TableViewerColumn col : tableViewerColumns) {
			colIndex++;
			if (!col.getColumn().getText().equals(headerMenu.getItem(0).getText())) {

				// if (col.getType() != null) {
				CSVColCheck csvColCheck = new CSVColCheck();
				List<String> columnValues = getColumnValues(colIndex);
				// List<String> columnValues = col.getColumn().getData();
				// System.out.println("columnValues.size() " +
				// columnValues.size());
				// LCADataType colType=col.getAssignedLCADataType();
				for (QACheck check : QACheck.getGeneralQAChecks()) {
					for (int i = 0; i < columnValues.size(); i++) {
						String val = columnValues.get(i);
						// System.out.println("testing column # " + colIndex +
						// " with val: " + val);
						// System.out.println("check.getPattern() " +
						// check.getPattern());
						// System.out.println("check.getIssue() " +
						// check.getIssue());

						Matcher matcher = check.getPattern().matcher(val);
						while (matcher.find()) {
							issuesFound++;
							System.out.println("check.getIssue() " + check.getIssue());
							Issue issue = check.getIssue();
							issue.setRowNumber(i);
							issue.setColNumber(colIndex);
							issue.setRowNumber(i);
							issue.setCharacterPosition(matcher.end());
							issue.setStatus(Status.UNRESOLVED);

							Logger.getLogger("run").warn(issue.getDescription());
							Logger.getLogger("run").warn("  ->Row" + issue.getRowNumber());
							Logger.getLogger("run").warn("  ->Column" + issue.getColNumber());
							Logger.getLogger("run").warn("  ->Character position" + issue.getCharacterPosition());
							assignIssue(issue);
							csvColCheck.addIssue(issue);
						}
					}
				}
			}
		}
		return issuesFound;
		// FlowsWorkflow.setTextIssues(issuesFound + " issues found");
	}

	public static void matchFlowables() {
		Model model = ActiveTDB.model;
		Resource masterFlowableDSResource = DataSetKeeper.getByName("Master_Flowables").getTdbResource(); // HACK
		if (masterFlowableDSResource == null) {
			System.out.println("Awww.  Why didn't we find it!");
		}
		// int issuesFound = 0;
		int colIndex = -1;
		for (TableViewerColumn col : tableViewerColumns) {
			colIndex++;
			Logger.getLogger("run").info("Checking column # " + colIndex);
			if (col.getColumn().getText().equals("Flowable Name")) {
				Logger.getLogger("run").info("Checking column # " + colIndex);
				List<String> columnValues = getColumnValues(colIndex);
				for (int i = 0; i < columnValues.size(); i++) {
					if (i % 100 == 0) {
						Logger.getLogger("run").info("  Completed " + i + "rows.");

					}
					String val = columnValues.get(i);
					if (val != null) {
						// Statement statement =
						// StmtIterator stmtIterator =
						// model.listStatements(masterFlowableDSResource,
						// RDFS.label, val);
						// ResIterator resIterator =
						ResIterator resIterator = model.listResourcesWithProperty(RDFS.label, val);
						while (resIterator.hasNext()) {
							Resource resource = resIterator.next();
							if (model.contains(resource, ECO.hasDataSource, masterFlowableDSResource)) {
								colorCell(i, colIndex, green);

							}
						}
					}
				}
			}
			Logger.getLogger("run").info("  Finished checking.");

		}
		// FlowsWorkflow.setTextIssues(issuesFound + " issues found");
	}

	private static void colorCell(int rowNumber, int colNumber, Color color) {
		TableItem tableItem = table.getItem(rowNumber);
		tableItem.setBackground(colNumber, color);
		// tableItem.addListener(SWT.MouseDown, mouseListener);

	}

	private static List<String> getColumnValues(int colIndex) {
		List<String> results = new ArrayList<String>();
		TableProvider tableProvider = TableKeeper.getTableProvider(key);
		List<DataRow> dataRowList = tableProvider.getData();
		// System.out.println("dataRowList.size() " + dataRowList.size());
		for (DataRow dataRow : dataRowList) {
			results.add(dataRow.get(colIndex));
		}
		return results;
	}

	private static void assignIssue(Issue issue) {
		TableProvider tableProvider = TableKeeper.getTableProvider(key);
		List<DataRow> dataRowList = tableProvider.getData();
		if (issue.getRowNumber() >= dataRowList.size()) {
			return;
		}
		TableItem tableItem = table.getItem(issue.getRowNumber());
		tableItem.setBackground(issue.getColNumber(), issue.getStatus().getColor());
		// tableItem.set
		return;
	}

	private static CSVColumnInfo getCSVColumnInfoByHeaderString(String headerString) {
		for (CSVColumnInfo csvColumnInfo : availableCSVColumnInfo) {
			if (csvColumnInfo.getHeaderString().equals(headerString)) {
				return csvColumnInfo;
			}
		}
		return null;
	}

	private static MenuItem getMenuItemByHeaderString(String headerString) {
		for (MenuItem menuItem : headerMenu.getItems()) {
			if (menuItem.getText().equals(headerString)) {
				return menuItem;
			}
		}
		return null;
	}

}
