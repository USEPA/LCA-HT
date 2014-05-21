package gov.epa.nrmrl.std.lca.ht.csvFiles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import gov.epa.nrmrl.std.lca.dataModels.QACheck;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.Issue;
import harmonizationtool.model.TableKeeper;
import harmonizationtool.model.TableProvider;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.ui.part.ViewPart;

/**
 * @author tec
 * 
 */
public class CSVTableView extends ViewPart {
	public CSVTableView() {
	}

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.csvFiles.csvTableView";
//	public static final String ID = "HarmonizationTool.viewData";
	private static String key = null;
	private TableViewer tableViewer;
	private Table table;
	private static List<CsvTableViewerColumn> columns = new ArrayList<CsvTableViewerColumn>();
	// the menu that is displayed when column header is right clicked
	private static Menu headerMenu;
	private Menu rowMenu;
	private String formerlySelectedHeaderMenuItem;

	private List<Integer> rowsSelected = new ArrayList<Integer>();
	public static List<Integer> rowsToIgnore = new ArrayList<Integer>();

	private TableColumn columnSelected = null;
	private Color gray = new Color(Display.getCurrent(), 128, 128, 128);
	private Color black = new Color(Display.getCurrent(), 0, 0, 0);
	// private Color defaultTextColor;

	public static final String IMPACT_ASSESSMENT_METHOD_HDR = "Impact Assessment Method";
	// e.g. ReCiPe or TRACI

	public static final String IMPACT_CAT_HDR = "Impact Category";
	// e.g. climate change
	// public static final Resource ImpactCategory = ECO.ImpactCategory;

	public static final String IMPACT_CAT_INDICATOR_HDR = "Impact category indicator";
	// e.g. infrared radiative forcing

	public static final String IMPACT_CHARACTERIZATION_MODEL_HDR = "Impact Characterization Model"; // e.g.
	// IPCC Global Warming Potential (GWP)

	public static final String IMPACT_DIR_HDR = "Impact Direction";
	// e.g. Resource , From , Emission , Uptake, etc.

	public static final String IMPACT_CAT_REF_UNIT_HDR = "Impact cat ref unit";
	// e.g. kg CO2 eq
	//
	public static final String CAT1_HDR = "Category"; // e.g. air
	public static final String CAT2_HDR = "Subcategory"; // e.g. low population
	public static final String CAT3_HDR = "Sub-subcategory";
	//
	public static final String NAME_HDR = "Flowable Name";
	public static final String CASRN_HDR = "CASRN";
	public static final String ALT_NAME_HDR = "Flowable Alt_Name";
	//
	// // ECO.ImpactCharacterizationFactor;
	// public static final String CHAR_FACTOR_HDR = "Characterization factor";
	// // THIS IS THE (float) NUMBER
	//
	// public static final String FLOW_UNIT_HDR = "Flow Unit";
	// // e.g. kg
	//
	// public static final String FLOW_PROPERTY_HDR = "Flow Property";
	// // e.g. mass
	//
	public static final String IGNORE_HDR = "Ignore";

	// @Override
	// public void dispose() {
	// super.dispose();
	// }

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		// parent.setLayout(null);

		tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		table = tableViewer.getTable();
		// table.setBounds(0, 0, 650, 650);

		// headerMenu = new Menu(table);
		// initializeHeaderMenu();

		rowMenu = new Menu(table);
		initializeRowMenu();

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				// TableItem item =(TableItem)event.item;
				// event.item;
				System.out.println("============row selected=======");

				System.out.println(selection.getClass().getName());
				Iterator iterator = selection.iterator();
				rowsSelected.clear();
				while (iterator.hasNext()) {
					// Object element = iterator.next();
					// DataRow dataRow = (DataRow) iterator.next();

					int index = TableKeeper.getTableProvider(key).getIndex((DataRow) iterator.next());
					rowsSelected.add(index);
				}
				System.out.println(rowsSelected);
				rowMenu.setVisible(true);
			}
		});

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
		// viewer.refresh();

	}

	public void clearView(String key) {
		if ((key != null) && (key.equals(this.key))) {
			// final Table table = tableViewer.getTable();
			tableViewer.setInput(null);
			removeColumns(table);
			table.setHeaderVisible(false);
			table.setLinesVisible(false);
		}
	}

	/**
	 * removes columns from the given table
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

	private void createColumns() {
		System.out.println("key=" + key);
		if (key != null) {
			// Define the menu and assign to the table

			TableProvider tableProvider = TableKeeper.getTableProvider(key);
			headerMenu = tableProvider.getMenu();
			if (headerMenu == null) {
				headerMenu = new Menu(table);
				initializeHeaderMenu();
			}
			DataRow header = tableProvider.getHeaderNames();
			List<DataRow> tableData = tableProvider.getData();
			DataRow dataRow = tableData.get(0);
			while (header.getSize() < dataRow.getSize()) {
				header.add(IGNORE_HDR);
			}
			int numCol = header.getSize();
			System.out.println("numCol = " + numCol);

			for (int i = 0; i < numCol; i++) {
				if (header.get(i) == null) {
					header.set(i, IGNORE_HDR);
				}
				CsvTableViewerColumn col = createTableViewerColumn(header.get(i), 100, i);
				col.setLabelProvider(new MyColumnLabelProvider(i));
				// tableProvider.addHeaderName(titlesArray[i],col.hashCode());
				columns.add(col);
			}
			saveColumnNames();
		}
	}

	/**
	 * class for generating column labels. This class will handle a variable
	 * number of columns
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
	private CsvTableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {

		final CsvTableViewerColumn csvTableViewerColumn = new CsvTableViewerColumn(tableViewer, SWT.NONE, colNumber);
		final TableColumn tableColumn = csvTableViewerColumn.getColumn();
		tableColumn.setText(title);
		tableColumn.setWidth(bound);
		tableColumn.setResizable(true);
		tableColumn.setMoveable(false);
		tableColumn.setToolTipText("Column " + colNumber);

		tableColumn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource() instanceof TableColumn) {
					TableColumn col = (TableColumn) e.getSource();
					columnSelected = col;
					int colSelectionIndex = Integer.parseInt(col.getToolTipText().substring(7));
					System.out.println("colSelectionIndex =" + colSelectionIndex);
					formerlySelectedHeaderMenuItem = columnSelected.getText();
					headerMenu.setVisible(true);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (e.getSource() instanceof TableColumn) {
					TableColumn col = (TableColumn) e.getSource();
					columnSelected = col;
					int colSelectionIndex = Integer.parseInt(col.getToolTipText().substring(7));
					System.out.println("colSelectionIndex =" + colSelectionIndex);
					formerlySelectedHeaderMenuItem = columnSelected.getText();
					headerMenu.setVisible(true);
				}
			}

		});

		return csvTableViewerColumn;
	}
//	
//	public String getTitle(){
//		return "hello";
//	}

	/**
	 * this method initializes the headerMenu with menuItems and a
	 * ColumnSelectionListener
	 * 
	 * @param menu
	 *            headerMenu which allows user to rename the columns
	 */

	private void initializeHeaderMenu() {
		ColumnSelectionListener columnSelectionListener = new ColumnSelectionListener();

		MenuItem menuItem;
		menuItem = new MenuItem(headerMenu, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, columnSelectionListener);
		menuItem.setText("Ignore");
		String lastParentGroup = "";
		for (CsvTableViewerColumnType type : CsvTableViewerColumnType.values()) {
			String parentGroup = type.parentGroup;
			if (!parentGroup.equals(lastParentGroup)) {
				new MenuItem(headerMenu, SWT.SEPARATOR);
				lastParentGroup = parentGroup;
			}
			menuItem = new MenuItem(headerMenu, SWT.NORMAL);

			if (type.required) {
				// DO SOMETHING TO HIGHLIGHT THIS
			} else {
				// DEFAULT ENTRY
			}
			menuItem.addListener(SWT.Selection, columnSelectionListener);
			menuItem.setText(type.displayString);
		}
	}

	private void initializeRowMenu() {
		RowSelectionListener rowSelectionListener = new RowSelectionListener();

		MenuItem menuItem;

		menuItem = new MenuItem(rowMenu, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, rowSelectionListener);
		menuItem.setText("ignore rows");

		// new MenuItem(rowMenu, SWT.SEPARATOR); // ----------

		menuItem = new MenuItem(rowMenu, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, rowSelectionListener);
		menuItem.setText("use rows");

	}

	/**
	 * once the user has selected a column header for change this Listener will
	 * set the column header to the value selected by the user. If the user
	 * selects "Custom...", then a dialog is displayed so the user can enter a
	 * custom value for the column header.
	 * 
	 * @author tec 919-541-1500
	 * 
	 */
	private class ColumnSelectionListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			System.out.println("event = " + event);
			if ((event.widget instanceof MenuItem) && (columnSelected != null)) {
				String menuItemText = ((MenuItem) event.widget).getText();
				MenuItem[] menuItems = headerMenu.getItems();
				// int assigned = 0;
				// int total = menuItems.length;
				for (MenuItem mi : menuItems) {
					if (formerlySelectedHeaderMenuItem.equals(mi.getText())) {
						mi.setEnabled(true);
						break;
					}
				}

				for (MenuItem mi : menuItems) {
					boolean unique = CsvTableViewerColumnType.isUnique(mi.getText());
					if ((unique && (event.widget.equals(mi)))) {
						mi.setEnabled(false);
						break;
					}
				}
				// System.out.println("got here");
				// FlowsWorkflow.setAssignedColumnCount(assigned, total);
				if (menuItemText != null) {
					if (menuItemText.equals("Custom...")) {
						// allow the user to define a custom header name
						InputDialog inputDialog = new InputDialog(getViewSite().getShell(), "Column Name Dialog",
								"Enter a custom column label", "", null);
						inputDialog.open();
						int returnCode = inputDialog.getReturnCode();
						if (returnCode == InputDialog.OK) {
							String val = inputDialog.getValue();
							columnSelected.setText(val);
						}
					} else {
						columnSelected.setText(menuItemText);
					}
					//
				}
				// save the column names to the TableProvider in case the data
				// table needs to be
				// re-displayed
				saveColumnNames();
				TableKeeper.getTableProvider(key).setMenu(headerMenu);
				exportColumnStatus();
			}
		}
	}

	private void exportColumnStatus() {
		int assigned = 0;
		for (Object col : columns) {
			if (!((CsvTableViewerColumn) col).getColumn().getText().equals(IGNORE_HDR)) {
				assigned++;
			}
		}
		FlowsWorkflow.setAssignedColumnCount(assigned, columns.size());
	}

	private class RowSelectionListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			System.out.println("event = " + event);
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
	 * this method retrieves the column header text values from the column
	 * components and passes them to the TableProvider so they can be retrieved
	 * when the data table is re-displayed
	 */
	private void saveColumnNames() {
		List<String> columnNames = new ArrayList<String>();
		// TableColumn[] tableColumns = tableViewer.getTable().getColumns();
		TableColumn[] tableColumns = table.getColumns();
		for (TableColumn tableColumn : tableColumns) {
			columnNames.add(tableColumn.getText());
		}
		TableProvider tableProvider = TableKeeper.getTableProvider(key);
		tableProvider.setHeaderNames(columnNames);
	}

	public static void checkColumns() {
		List<QACheck> checks = createQAChecks();
		for (CsvTableViewerColumn col : columns) {
			CsvTableViewerColumnType type = col.getType();
			if (type.equals(CsvTableViewerColumnType.FLOWABLE_NAME)) {
				for(QACheck check:checks){
					
				}
			}
		}
	}

	public static List<QACheck> createQAChecks() {
		List<QACheck> qaCheckPack = new ArrayList<QACheck>();
		Pattern p = Pattern.compile("^\\s+(.*?)$");
		Issue i = new Issue(
				"Leading space(s)",
				"Preceeding text, at least one white space character occurs.  This may be a non-printing character.",
				"If you can not see and remove the leading space, search for non-ASCCI characters.  You may also use the auto-clean function.",
				true);
		qaCheckPack.add(new QACheck(p, i));
		
		p = Pattern.compile("^(.*?)\\s+$");
		i = new Issue(
				"Trailing space(s)",
				"Following text, at least one white space character occurs.  This may be a non-printing character.",
				"If you can not see and remove the leading space, search for non-ASCCI characters.  You may also use the auto-clean function.",
				true);
		qaCheckPack.add(new QACheck(p, i));
		
		p = Pattern.compile("^\"([^\"]*)\"$");
		i = new Issue(
				"Bookend quotes",
				"The text is surrounded by apparently superfluous double quote marks.",
				"Remove these quote marks.  You may also use the auto-clean function.",
				true);
		qaCheckPack.add(new QACheck(p, i));
		
		return qaCheckPack;
	}
}
