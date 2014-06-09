package gov.epa.nrmrl.std.lca.ht.csvFiles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import gov.epa.nrmrl.std.lca.ht.dataModels.QACheck;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.Issue;
import harmonizationtool.model.Status;
import harmonizationtool.model.TableKeeper;
import harmonizationtool.model.TableProvider;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * @author tec and Tom Transue
 * 
 */
public class CSVTableView extends ViewPart {
	private TextCellEditor editor;

	public CSVTableView() {
	}

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.csvFiles.csvTableView";

	private static String key = null;
	private static TableViewer tableViewer;

	private static Table table;

	private static List<CSVColumnInfo> availableCSVColumnInfo = new ArrayList<CSVColumnInfo>();
	private static CSVColumnInfo[] assignedCSVColumnInfo;
	private static String csvColumnDefaultTooltip = "Ignore Column";
	private static Menu headerMenu;
	private static Menu ignoreRowMenu;
	private static Menu fixCellMenu;
	private static Menu infoMenu;

	private static int rowNumSelected = -1;
	private static int colNumSelected = -1;

	// private static List<Integer> rowsSelected = new ArrayList<Integer>();
	public static List<Integer> rowsToIgnore = new ArrayList<Integer>();

	private static int tableColumnSelectedIndex = -1;

	// private static Color white = new Color(Display.getCurrent(), 255, 255,
	// 255);

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

	private static SelectionListener colSelectionListener = new SelectionListener() {

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource() instanceof TableColumn) {
				TableColumn col = (TableColumn) e.getSource();
				tableColumnSelectedIndex = table.indexOf(col);
				System.out.println("(widgetSelected) tableColumnSelectedIndex set to " + tableColumnSelectedIndex);
				headerMenu.setVisible(true);
			}
		};

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			if (e.getSource() instanceof TableColumn) {
				TableColumn col = (TableColumn) e.getSource();
				System.out.println("(widgetDefaultSelected) tableColumnSelectedIndex set to "
						+ tableColumnSelectedIndex);
				tableColumnSelectedIndex = table.indexOf(col);
				headerMenu.setVisible(true);
			}
		}

	};

	// private static ISelectionChangedListener rowsSelectedChangedListener =
	// new ISelectionChangedListener() {
	// public void selectionChanged(final SelectionChangedEvent event) {
	// IStructuredSelection selection = (IStructuredSelection)
	// event.getSelection();
	// Iterator iterator = selection.iterator();
	// rowsSelected.clear();
	// while (iterator.hasNext()) {
	// int index = TableKeeper.getTableProvider(key).getIndex((DataRow)
	// iterator.next());
	// rowsSelected.add(index);
	// }
	// ignoreRowMenu.setVisible(true);
	// }
	// };

	private class RowMenuSelectionListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			// System.out.println("RowMenuSelectionListener event = " + event);
			if (event.widget instanceof MenuItem) {
				String menuItemText = ((MenuItem) event.widget).getText();
				if (menuItemText.equals("ignore row")) {
					// for (int tableIndex : rowsSelected) {

					tableViewer.getTable().getItem(rowNumSelected)
							.setForeground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
					if (!rowsToIgnore.contains(rowNumSelected)) {
						rowsToIgnore.add(rowNumSelected);
					}
					// }
				} else if (menuItemText.equals("use row")) {
					// for (int tableIndex : rowsSelected) {
					tableViewer.getTable().getItem(rowNumSelected)
							.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
					if (rowsToIgnore.contains(rowNumSelected)) {
						// int indexToRemove =
						// rowsToIgnore.indexOf(rowNumSelected);
						rowsToIgnore.remove(rowsToIgnore.indexOf(rowNumSelected));
					}
					// }
				}
				// saveColumnNames();
			}
		}

	}

	private static int binSortPoint2CellRow(Point pt, int col) {
		int fullRange = table.getItemCount();
		System.out.println("fullRange " + fullRange);
		int depth = 2;
		int granularityToStep = 20;
		int toTest = 0;
		TableItem item = table.getItem(toTest);
		Rectangle rect = item.getBounds(col);
		System.out.println("rect: "+rect);
		while (rect.width == 0){
			toTest+=granularityToStep;
			if (toTest > fullRange){
				granularityToStep/= 2;
				toTest=0;
			}
			item = table.getItem(toTest);
			rect = item.getBounds(col);
		}
		int lastMove = 1;
		while (!rect.contains(pt)) {
			System.out.println("depth: " + depth + " . toTest: " + toTest);
			System.out.println("rect.y: " + rect.y + " - pt.y: " + pt.y);

			depth *= 2;
			if (fullRange / depth < 1) {
				depth = fullRange;
			}
			if (rect.y > pt.y) {
				toTest -= fullRange / depth;
				lastMove = -1;
			} else {
				toTest += fullRange / depth;
				lastMove = 1;
			}
			if (toTest > fullRange) {
				toTest = fullRange;
			}
			else if(toTest < 0){
				toTest=0;
			}
			item = table.getItem(toTest);
			rect = item.getBounds(col);
			while (rect.width == 0){
				// FIXME
			}
		}
		return toTest;
	}

	private static Listener cellSelectionMouseDownListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			Point pt = new Point(event.x, event.y);
			int clickedRow = 0;
			int clickedCol = 0;
			
			TableItem item = table.getItem(clickedRow);
			Rectangle rect = item.getBounds(clickedCol);
			while (rect.width == 0){
				clickedRow++;
				item = table.getItem(clickedRow);
				rect = item.getBounds(clickedCol);
				if (clickedRow>=table.getItemCount()){
					clickedRow = -1;
					clickedCol++;
				}
			}
			while  (rect.x + rect.width < pt.x) {
				System.out.println("column checking... item: "+item+"   rect: "+ rect+ "    pt: "+pt);
				clickedCol ++;
				rect = item.getBounds(clickedCol);
			}

			clickedRow = binSortPoint2CellRow(pt, clickedCol);
			//
			// System.out.println("(click) Col: "+col);
			// binSortPoint2CellRow(col)
			//
			// for (int row = 0; row < table.getItemCount(); row++) {
			// if (row%1000 == 0){
			// System.out.println("(click) Row: "+row);
			// }
			// else if (rect.x > pt.x) {
			// col = table.getColumnCount();
			// row = table.getItemCount();
			// } else if (rect.contains(pt)) {
			// clickedRow = row;
			// clickedCol = col;
			// System.out.println("Item " + clickedRow + "-" + clickedCol);
			// col = table.getColumnCount();
			// row = table.getItemCount();
			// }
			// }
			// }

			rowNumSelected = clickedRow;
			colNumSelected = clickedCol;
			CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[clickedCol];
			Issue issueOfThisCell = null;
			for (Issue issue : csvColumnInfo.getIssues()) {
				if (issue.getRowNumber() == clickedRow) {
					issueOfThisCell = issue;
				}
			}
			if (issueOfThisCell == null) {
				ignoreRowMenu.setVisible(true);
			} else {
				fixCellMenu.setVisible(true);
			}
		}
	};

	private static Listener cellSelectionMouseHoverListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			Point pt = new Point(event.x, event.y);
			int clickedRow = 0;
			int clickedCol = 0;
			TableItem item = table.getItem(0);
			Rectangle rect = item.getBounds(clickedCol);
			while  (rect.x + rect.width < pt.x) {
				System.out.println("column checking... item: "+item+"   rect: "+ rect+ "    pt: "+pt);
				clickedCol ++;
				rect = item.getBounds(clickedCol);
			}

			clickedRow = binSortPoint2CellRow(pt, clickedCol);
			//
			// for (int col = 0; col < table.getColumnCount(); col++) {
			// System.out.println("(hover) Col: "+col);
			// for (int row = 0; row < table.getItemCount(); row++) {
			// if (row%1000 == 0){
			// System.out.println("(click) Row: "+row);
			// }
			// TableItem item = table.getItem(row);
			// Rectangle rect = item.getBounds(col);
			// if (rect.x + rect.width < pt.x) {
			// col++;
			// } else if (rect.x > pt.x) {
			// col = table.getColumnCount();
			// row = table.getItemCount();
			// } else if (rect.contains(pt)) {
			// clickedRow = row;
			// clickedCol = col;
			// System.out.println("Item " + clickedRow + "-" + clickedCol);
			// col = table.getColumnCount();
			// row = table.getItemCount();
			// }
			// }
			// }
			rowNumSelected = clickedRow;
			colNumSelected = clickedCol;
			CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[clickedCol];
			Issue issueOfThisCell = null;
			for (Issue issue : csvColumnInfo.getIssues()) {
				if (issue.getRowNumber() == clickedRow) {
					issueOfThisCell = issue;
				}
			}
			if (issueOfThisCell == null) {
				infoMenu.setVisible(false);
			} else {
				setInfoMenu(issueOfThisCell);
				infoMenu.setVisible(true);
			}
			// System.out.println("Still fine at 239");
		}

	};

	private static void setInfoMenu(Issue issueOfThisCell) {
		while (infoMenu.getItemCount() > 0) {
			infoMenu.getItem(infoMenu.getItemCount() - 1).dispose();
		}
		MenuItem menuItem = new MenuItem(infoMenu, SWT.NORMAL);
		menuItem.setText(issueOfThisCell.getQaCheck().getDescription());

		menuItem = new MenuItem(infoMenu, SWT.NORMAL);
		menuItem.setText(issueOfThisCell.getQaCheck().getExplanation());

		menuItem = new MenuItem(infoMenu, SWT.NORMAL);
		menuItem.setText(issueOfThisCell.getQaCheck().getSuggestion());
	}

	private class FixCellMenuSelectionListener implements Listener {

		@Override
		public void handleEvent(Event event) {

			System.out.println("Clicked on a cell with an issue");
			if (event.widget instanceof MenuItem) {
				String menuItemText = ((MenuItem) event.widget).getText();
				if (menuItemText.equals("fix this cell")) {
					fixCurrentlySelectedCell();
					colorCell(rowNumSelected, colNumSelected, SWTResourceManager.getColor(SWT.COLOR_WHITE));
				} else if (menuItemText.equals("fix this issue type for this column")) {
					fixIssueInColumn();
					// for (int tableIndex : rowsSelected) {
					tableViewer.getTable().getItem(rowNumSelected)
							.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
					if (rowsToIgnore.contains(rowNumSelected)) {
						// int indexToRemove =
						// rowsToIgnore.indexOf(rowNumSelected);
						rowsToIgnore.remove(rowsToIgnore.indexOf(rowNumSelected));
					}
					// }
				}
				// saveColumnNames();
			}
		}

	}

	// private static Listener cellSelectionMouseHoverListener = new Listener()
	// {
	// @Override
	// public void handleEvent(Event event) {
	// System.out.println("event = " + event);
	// Rectangle clientArea = table.getClientArea();
	// Point pt = new Point(event.x, event.y);
	// int index = table.getTopIndex();
	// while (index < table.getItemCount()) {
	// boolean visible = false;
	// TableItem item = table.getItem(index);
	// for (int i = 0; i < table.getColumnCount(); i++) {
	// Rectangle rect = item.getBounds(i);
	// if (rect.contains(pt)) {
	// System.out.println("Item " + index + "-" + i);
	// continue;
	// }
	// if (!visible && rect.intersects(clientArea)) {
	// visible = true;
	// }
	// }
	// if (!visible)
	// return;
	// index++;
	// }
	// }
	// };

	@Override
	public void createPartControl(Composite parent) {
		// parent.setLayout(null);

		tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		// tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL |
		// SWT.V_SCROLL | SWT.READ_ONLY);
		editor = new TextCellEditor(tableViewer.getTable());

		table = tableViewer.getTable();

		// tableViewer.addSelectionChangedListener(rowsSelectedChangedListener);
		table.addListener(SWT.MouseHover, cellSelectionMouseHoverListener);
		table.addListener(SWT.MouseDown, cellSelectionMouseDownListener);

		initializeIgnoreRowMenu();
		initializeFixRowMenu();
		initializeInfoMenu();
	}

	protected CellEditor getCellEditor(Object element) {
		return editor;
	}

	protected boolean canEdit(Object element) {
		return true;
	}

	// protected Object getValue(Object element) {
	// return ((Person) element).getFirstName();
	// }
	//
	//
	// protected void setValue(Object element, Object userInputValue) {
	// ((Person) element).setFirstName(String.valueOf(value));
	// viewer.update(element, null);
	// }
	//

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
		System.out.println("Setting content provider");
		tableViewer.setContentProvider(new ArrayContentProvider());
		TableProvider tableProvider = TableKeeper.getTableProvider(key);
		System.out.println("Setting input data");
		tableViewer.setInput(tableProvider.getData());
		System.out.println("Setting (blank) CSVColumnInfo");
		assignedCSVColumnInfo = new CSVColumnInfo[table.getColumns().length];
		for (int i = 0; i < table.getColumns().length; i++) {
			assignedCSVColumnInfo[i] = new CSVColumnInfo(csvColumnDefaultTooltip);
		}
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
			System.out.println("Adding headers");
			while (headerRow.getSize() < dataRow.getSize()) {
				headerRow.add(defaultHeader);
			}
			// int numCol = headerRow.getSize();
			// System.out.println("numCol = " + numCol);

			System.out.println("Adding columns");

			for (int i = 0; i < tableProvider.getColumnCount(); i++) {
				System.out.println("  Column " + i);

				// if (headerRow.get(i) == null) {
				// headerRow.set(i, IGNORE_HDR);
				// }
				TableViewerColumn tableViewerColumn = createTableViewerColumn(defaultHeader, 100, i);
				tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(i));
				// TableColumn tableColumn = tableViewerColumn.getColumn();
				// tableColumn.addListener(eventType, listener)
				// tableProvider.addHeaderName(titlesArray[i],col.hashCode());
				// tableViewerColumns.add(tableViewerColumn);
			}
			// saveColumnNames();
		}
	}

	/**
	 * class for generating column labels. This class will handle a variable
	 * number of tableViewerColumns
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
	 * this method initializes the headerMenu with menuItems and a
	 * HeaderMenuColumnSelectionListener
	 * 
	 * @param menu
	 *            headerMenu which allows user to rename the tableViewerColumns
	 */

	public void initializeHeaderMenu() {
		if (headerMenu != null) {
			headerMenu.dispose();
		}
		CSVColumnInfo ignoreColumnInfo = new CSVColumnInfo(csvColumnDefaultTooltip);
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
		clearMenuItemListeners(menuItem);
		menuItem.addListener(SWT.Selection, new HeaderMenuColumnSelectionListener());
		menuItem.setText(csvColumnInfo.getHeaderString());
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

	// private void resetSelectionListener(MenuItem menuItem) {
	// for (Listener listener : menuItem.getListeners(SWT.Selection)) {
	// menuItem.removeListener(SWT.Selection, listener);
	// }
	// HeaderMenuColumnSelectionListener columnSelectionListener = new
	// HeaderMenuColumnSelectionListener();
	// menuItem.addListener(SWT.Selection, columnSelectionListener);
	// }

	private void clearMenuItemListeners(MenuItem menuItem) {
		for (Listener listener : menuItem.getListeners(SWT.Selection)) {
			menuItem.removeListener(SWT.Selection, listener);
		}
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
	// // while (ignoreRowMenu.getItemCount() >0){
	// ignoreRowMenu.setData(null);
	// // }
	// }

	private void initializeIgnoreRowMenu() {
		ignoreRowMenu = new Menu(table);
		RowMenuSelectionListener rowMenuSelectionListener = new RowMenuSelectionListener();

		MenuItem menuItem;

		menuItem = new MenuItem(ignoreRowMenu, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, rowMenuSelectionListener);
		menuItem.setText("ignore row");

		menuItem = new MenuItem(ignoreRowMenu, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, rowMenuSelectionListener);
		menuItem.setText("use row");

	}

	private void initializeFixRowMenu() {
		fixCellMenu = new Menu(table);
		FixCellMenuSelectionListener fixCellMenuSelectionListener = new FixCellMenuSelectionListener();

		MenuItem menuItem;
		//
		// menuItem = new MenuItem(fixCellMenu, SWT.NORMAL);
		// menuItem.addListener(SWT.Selection, fixCellMenuSelectionListener);
		// menuItem.setText("(menu not updated)");

		menuItem = new MenuItem(fixCellMenu, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, fixCellMenuSelectionListener);
		menuItem.setText("fix this cell");

		menuItem = new MenuItem(fixCellMenu, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, fixCellMenuSelectionListener);
		menuItem.setText("fix this issue for this column");

		menuItem = new MenuItem(fixCellMenu, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, fixCellMenuSelectionListener);
		menuItem.setText("run character-encoding tool");

	}

	private void initializeInfoMenu() {
		infoMenu = new Menu(table);
	}

	private void fixCurrentlySelectedCell() {
		CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[colNumSelected];
		TableItem tableItem = table.getItem(rowNumSelected);
		String startingText = tableItem.getText(colNumSelected);
		System.out.println("trying to fix: " + startingText);

		for (Issue issue : csvColumnInfo.getIssues()) {
			if ((issue.getRowNumber() == rowNumSelected) && (!issue.getStatus().equals(Status.RESOLVED))) {
				QACheck qaCheck = issue.getQaCheck();
				if (qaCheck.getReplacement() != null) {
					Matcher matcher = qaCheck.getPattern().matcher(startingText);
					String thing = matcher.replaceFirst(qaCheck.getReplacement());
					System.out.println("The value is now ->" + thing + "<-");
					tableItem.setText(colNumSelected, thing);
					issue.setStatus(Status.RESOLVED);
					colorCell(issue);
				}
			}
		}
	}

	private void fixOneIssue(Issue issue) {
		// CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[colNumSelected];
		TableItem tableItem = table.getItem(issue.getRowNumber());
		String startingText = tableItem.getText(issue.getColNumber());
		System.out.println("trying to fix: " + startingText);
		QACheck qaCheck = issue.getQaCheck();
		if (qaCheck.getReplacement() != null) {
			Matcher matcher = qaCheck.getPattern().matcher(startingText);
			String thing = matcher.replaceFirst(qaCheck.getReplacement());
			System.out.println("The value is now ->" + thing + "<-");
			tableItem.setText(colNumSelected, thing);
			issue.setStatus(Status.RESOLVED);
			colorCell(issue);
		}

	}

	private void fixIssueInColumn() {
		CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[colNumSelected];
		TableItem tableItem = table.getItem(rowNumSelected);
		String startingText = tableItem.getText(colNumSelected);
		System.out.println("trying to fix: " + startingText);
		QACheck qaCheckToFix = null;
		// FIRST, FIND THE ISSUE IN QUESTION
		for (Issue issue : csvColumnInfo.getIssues()) {
			if ((issue.getRowNumber() == rowNumSelected) && (!issue.getStatus().equals(Status.RESOLVED))) {
				qaCheckToFix = issue.getQaCheck();
			}
		}
		for (Issue issue : csvColumnInfo.getIssues()) {
			if (issue.getQaCheck().equals(qaCheckToFix)) {
				fixOneIssue(issue);
			}
		}
	}

	// private void setFixCellRowMenu() {
	// // while (ignoreRowMenu.getItemCount() >0){
	// // ignoreRowMenu.getItem(0).dispose();
	// // }
	// ignoreRowMenu.setData(null);
	//
	// // RowMenuSelectionListener iSelectionChangedListener = new
	// // RowMenuSelectionListener();
	//
	// MenuItem menuItem;
	//
	// menuItem = new MenuItem(ignoreRowMenu, SWT.NORMAL);
	// // menuItem.addListener(SWT.Selection, iSelectionChangedListener);
	// menuItem.setText("ignore rows");
	//
	// // new MenuItem(ignoreRowMenu, SWT.SEPARATOR); // ----------
	//
	// menuItem = new MenuItem(ignoreRowMenu, SWT.NORMAL);
	// // menuItem.addListener(SWT.Selection, iSelectionChangedListener);
	// menuItem.setText("use rows");
	//
	// }

	// private void setIssueRowMenu(){
	// while (ignoreRowMenu.getItemCount() >0){
	// ignoreRowMenu.getItem(0).dispose();
	// }
	// MenuItem menuItem;
	//
	// // menuItem = new MenuItem(ignoreRowMenu, SWT.NORMAL);
	// // menuItem.addListener(SWT.Selection, iSelectionChangedListener);
	// // menuItem.setText("resolve issue");
	// //
	// // // new MenuItem(ignoreRowMenu, SWT.SEPARATOR); // ----------
	// //
	// // menuItem = new MenuItem(ignoreRowMenu, SWT.NORMAL);
	// // menuItem.addListener(SWT.Selection, iSelectionChangedListener);
	// // menuItem.setText("flag cell");
	//
	// }

	/**
	 * once the user has selected a column header for change this Listener will
	 * set the column header to the value selected by the user. If the user
	 * selects "Custom...", then a dialog is displayed so the user can enter a
	 * custom value for the column header.
	 * 
	 * @author tec 919-541-1500
	 * 
	 */
	private class HeaderMenuColumnSelectionListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			System.out.println("HeaderMenuColumnSelectionListener event = " + event);
			System.out.println("event.widget  = " + event.widget);

			if ((event.widget instanceof MenuItem) && (tableColumnSelectedIndex > -1)) {
				MenuItem menuItem = (MenuItem) event.widget;
				String menuItemText = menuItem.getText();
				// CSVColumnInfo csvColumnInfo =
				// getCSVColumnInfoByHeaderString(menuItemText);
				CSVColumnInfo menuCSVColumnInfo = getCSVColumnInfoByHeaderString(menuItemText);
				CSVColumnInfo selectedCSVColumnInfo = assignedCSVColumnInfo[tableColumnSelectedIndex];

				if (menuCSVColumnInfo.getHeaderString().equals(selectedCSVColumnInfo.getHeaderString())) {
					// NO ACTION REQUIRED, NOTHING CHANGED
					return;
				}

				// RESET PREVIOUS HEADER STRING FOR THE COLUMN SELECTED
				getMenuItemByHeaderString(selectedCSVColumnInfo.getHeaderString()).setEnabled(true);

				selectedCSVColumnInfo.setHeaderString(menuCSVColumnInfo.getHeaderString());
				selectedCSVColumnInfo.setRequired(menuCSVColumnInfo.isRequired());
				selectedCSVColumnInfo.setUnique(menuCSVColumnInfo.isUnique());
				selectedCSVColumnInfo.setCheckLists(menuCSVColumnInfo.getCheckLists());
				selectedCSVColumnInfo.setStatus(Status.UNCHECKED);
				selectedCSVColumnInfo.setIssues(new ArrayList<Issue>());

				if (menuCSVColumnInfo.isUnique()) {
					menuItem.setEnabled(false);
				}
				table.getColumn(tableColumnSelectedIndex).setText(menuItemText);
				if (headerMenu.indexOf(menuItem) == 0) {
					table.getColumn(tableColumnSelectedIndex).setToolTipText(csvColumnDefaultTooltip);
				} else {
					table.getColumn(tableColumnSelectedIndex).setToolTipText("Assigned");
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
				// System.out.println("ColumnSelected text is now: " +
				// menuItemText);

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

	/**
	 * this method retrieves the column header text values from the column
	 * components and passes them to the TableProvider so they can be retrieved
	 * when the data table is re-displayed
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
			if (!csvColumnInfo.getHeaderString().equals(csvColumnDefaultTooltip)) {
				List<Issue> issueList = csvColumnInfo.getIssues();
				if (issueList != null) {
					for (int i = issueList.size() - 1; i >= 0; i--) {
						Issue issue = issueList.get(i);
						colorCell(issue.getRowNumber(), colIndex,
								SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
						csvColumnInfo.getIssues().remove(issue);
					}
				}

				csvColumnInfo.setIssues(new ArrayList<Issue>());
				List<String> columnValues = getColumnValues(colIndex);
				for (QACheck qaCheck : csvColumnInfo.getCheckLists()) {
					for (int i = 0; i < columnValues.size(); i++) {
						if (rowsToIgnore.contains(i)) {
							continue;
						}
						String val = columnValues.get(i);
						Matcher matcher = qaCheck.getPattern().matcher(val);

						if (qaCheck.isPatternMustMatch()) {
							if (!matcher.find()) {
								Issue issue = new Issue(qaCheck, i, colIndex, 0, Status.UNRESOLVED);
								Logger.getLogger("run").warn(qaCheck.getDescription());
								Logger.getLogger("run").warn("  ->Row " + issue.getRowNumber());
								Logger.getLogger("run").warn("  ->Column " + colIndex);
								Logger.getLogger("run").warn("  ->Required pattern not found");
								assignIssue(issue);
								csvColumnInfo.addIssue(issue);
							}
						} else {
							while (matcher.find()) {
								Issue issue = new Issue(qaCheck, i, colIndex, matcher.end(), Status.UNRESOLVED);
								Logger.getLogger("run").warn(qaCheck.getDescription());
								Logger.getLogger("run").warn("  ->Row" + issue.getRowNumber());
								Logger.getLogger("run").warn("  ->Column" + colIndex);
								Logger.getLogger("run").warn("  ->Character position" + issue.getCharacterPosition());
								assignIssue(issue);
								csvColumnInfo.addIssue(issue);
								// table.getColumn(csvColumnInfo.getIndexInTable()).setToolTipText(csvColumnInfo.getIssueCount()
								// + " issues below");
							}
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
					// while (matcher.find()) {
					// issuesRemaining++;
					// // System.out.println("check.getIssue() " +
					// // qaCheck.getIssue());
					// Issue issue = qaCheck.getIssue();
					// issue.setRowNumber(i);
					// issue.setColNumber(colIndex);
					// issue.setRowNumber(i);
					// issue.setCharacterPosition(matcher.end());
					// issue.setStatus(Status.UNRESOLVED);
					//
					// Logger.getLogger("run").warn(issue.getDescription());
					// Logger.getLogger("run").warn("  ->Row" +
					// issue.getRowNumber());
					// Logger.getLogger("run").warn("  ->Column" +
					// issue.getColNumber());
					// Logger.getLogger("run").warn("  ->Character position" +
					// issue.getCharacterPosition());
					// assignIssue(issue);
					// csvColumnInfo.addIssue(issue);
					// table.getColumn(colIndex).setToolTipText(csvColumnInfo.getIssueCount()
					// +
					// " issues below");
					// }
				}
				// }
			}
		}
		return issuesRemaining;
	}

	private static void colorCell(int rowNumber, int colNumber, Color color) {
		if (rowNumber > -1 && rowNumber < table.getItemCount()) {
			TableItem tableItem = table.getItem(rowNumber);
			tableItem.setBackground(colNumber, color);
		}
	}

	private static void colorCell(Issue issue) {
		TableItem tableItem = table.getItem(issue.getRowNumber());
		tableItem.setBackground(issue.getColNumber(), issue.getStatus().getColor());
	}

	private static List<String> getColumnValues(int colIndex) {
		List<String> results = new ArrayList<String>();
		TableProvider tableProvider = TableKeeper.getTableProvider(key);
		List<DataRow> dataRowList = tableProvider.getData();
		for (DataRow dataRow : dataRowList) {
			results.add(dataRow.get(colIndex));
		}
		return results;
	}

	private static void assignIssue(Issue issue) {
		if (issue.getRowNumber() >= table.getItemCount()) {
			return;
		}
		colorCell(issue);
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
