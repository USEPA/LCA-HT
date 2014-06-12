package gov.epa.nrmrl.std.lca.ht.csvFiles;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import gov.epa.nrmrl.std.lca.ht.dataModels.Flowable;
import gov.epa.nrmrl.std.lca.ht.dataModels.QACheck;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.Issue;
import harmonizationtool.model.Status;
import harmonizationtool.model.TableKeeper;
import harmonizationtool.model.TableProvider;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.ToolTip;
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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

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

	public static List<Integer> rowsToIgnore = new ArrayList<Integer>();

	private static int tableColumnSelectedIndex = -1;

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

	private class RowMenuSelectionListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			if (event.widget instanceof MenuItem) {
				String menuItemText = ((MenuItem) event.widget).getText();
				if (menuItemText.equals("ignore row")) {
					tableViewer.getTable().getItem(rowNumSelected)
							.setForeground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
					if (!rowsToIgnore.contains(rowNumSelected)) {
						rowsToIgnore.add(rowNumSelected);
					}
				} else if (menuItemText.equals("use row")) {
					tableViewer.getTable().getItem(rowNumSelected)
							.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
					if (rowsToIgnore.contains(rowNumSelected)) {
						rowsToIgnore.remove(rowsToIgnore.indexOf(rowNumSelected));
					}
				}
			}
		}

	}

	private static int getTableColumnNumFromPoint(int row, Point pt) {
		TableItem item = table.getItem(row);
		for (int i = 0; i < table.getColumnCount(); i++) {
			Rectangle rect = item.getBounds(i);
			if (rect.contains(pt)) {
				return i;
			}
		}
		return -1;
	}

	private static Listener cellSelectionMouseDownListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			Point ptLeft = new Point(1, event.y);
			Point ptClick = new Point(event.x, event.y);
			int clickedRow = 0;
			int clickedCol = 0;
			TableItem item = table.getItem(ptLeft);
			if (item == null) {
				return;
			}
			clickedRow = table.indexOf(item);
			clickedCol = getTableColumnNumFromPoint(clickedRow, ptClick);
			if (clickedCol < 0) {
				return;
			}

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
			Point ptLeft = new Point(1, event.y);
			Point ptClick = new Point(event.x, event.y);
			int hoverRow = 0;
			int hoverCol = 0;
			TableItem item = table.getItem(ptLeft);
			if (item == null) {
				return;
			}
			hoverRow = table.indexOf(item);

			hoverCol = getTableColumnNumFromPoint(hoverRow, ptClick);
			if (hoverCol < 0) {
				return;
			}

			CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[hoverCol];
			Issue issueOfThisCell = null;
			for (Issue issue : csvColumnInfo.getIssues()) {
				if (issue.getRowNumber() == hoverRow) {
					issueOfThisCell = issue;
				}
			}
			if (issueOfThisCell == null) {
				infoMenu.setVisible(false);
			} else {
				setInfoMenu(issueOfThisCell);
				infoMenu.setVisible(true);
//				tableViewer.getControl().setFocus();
				table.setFocus();
			}
		}

	};
	
	private static Listener cellSelectionMouseExitListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			infoMenu.setVisible(false);
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
					colorCell(rowNumSelected, colNumSelected, SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
				} else if (menuItemText.equals("fix this issue type for this column")) {
					fixIssueInColumn();
					tableViewer.getTable().getItem(rowNumSelected)
							.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
					if (rowsToIgnore.contains(rowNumSelected)) {
						rowsToIgnore.remove(rowsToIgnore.indexOf(rowNumSelected));
					}

				} else if (menuItemText.equals("standardize all CAS in this column")) {
					standardizeAllCAS(colNumSelected);
				}
			}
		}
	}


	@Override
	public void createPartControl(Composite parent) {

		tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL |SWT.READ_ONLY);
		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);
		editor = new TextCellEditor(tableViewer.getTable());

		table = tableViewer.getTable();
		table.addListener(SWT.MouseHover, cellSelectionMouseHoverListener);
		table.addListener(SWT.MouseExit, cellSelectionMouseExitListener);
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


	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		tableViewer.getControl().setFocus();
	}

	public void update(String key) {
		this.key = key;

		tableViewer.setContentProvider(new ArrayContentProvider());
		removeColumns(table);
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
			TableProvider tableProvider = TableKeeper.getTableProvider(key);
			initializeHeaderMenu();
			String defaultHeader = headerMenu.getItem(0).getText();
			DataRow headerRow = tableProvider.getHeaderRow();
			List<DataRow> tableData = tableProvider.getData();
			DataRow dataRow = tableData.get(0);
			System.out.println("Adding headers");
			while (headerRow.getSize() < dataRow.getSize()) {
				headerRow.add(defaultHeader);
			}
			System.out.println("Adding columns");

			TableViewerColumn tableViewerColumn = createTableViewerColumn(defaultHeader, 100, 0);
			tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(0));

			for (int i = 0; i < tableProvider.getColumnCount(); i++) {
				System.out.println("  Column " + i);
				tableViewerColumn = createTableViewerColumn(defaultHeader, 100, i+1);
				tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(i+1));
			}
		}
	}

	/**
	 * class for generating column labels. This class will handle a variable
	 * number of tableViewerColumns
	 * 
	 * @author tec
	 */
	class MyColumnLabelProvider extends ColumnLabelProvider {
		
//		  @Override
//		  public String getText(Object element) {
//		    Person p = (Person) element;
//		    return p.getFirstName();
//		  }

		  @Override
		  public String getToolTipText(Object element) {
					DataRow dataRow = null;
					try {
						dataRow = (DataRow) element;
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("element= " + element);
					}
					String t = "";
					try {
						int size = dataRow.getColumnValues().size();
						if (myColNum < size) {
//							t = dataRow.getToolTipValue(myColNum);
							t = dataRow.getToolTipValues().get(myColNum);
						}
					} catch (Exception e) {
						System.out.println("dataRow=" + dataRow);
						e.printStackTrace();
					}
					return t;
		  }

		  @Override
		  public Point getToolTipShift(Object object) {
		    return new Point(5, 5);
		  }

		  @Override
		  public int getToolTipDisplayDelayTime(Object object) {
		    return 100; // msec
		  }

		  @Override
		  public int getToolTipTimeDisplayed(Object object) {
		    return 5000; // msec
		  }
		  
		  //=======================
		private int myColNum;

		public MyColumnLabelProvider(int colNum) {
			this.myColNum = colNum;
		}

		@Override
		public String getText(Object element) {
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
		tableColumn.setText(title);
		tableColumn.setWidth(bound);
		tableColumn.setResizable(true);
		tableColumn.setMoveable(false);
		tableColumn.setToolTipText(csvColumnDefaultTooltip);
		tableColumn.addSelectionListener(colSelectionListener);
		return tableViewerColumn;
	}

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
		availableCSVColumnInfo.add(csvColumnInfo);

		MenuItem menuItem = new MenuItem(headerMenu, SWT.NORMAL);
		clearMenuItemListeners(menuItem);
		menuItem.addListener(SWT.Selection, new HeaderMenuColumnSelectionListener());
		menuItem.setText(csvColumnInfo.getHeaderString());
	}

	private void clearMenuItemListeners(MenuItem menuItem) {
		for (Listener listener : menuItem.getListeners(SWT.Selection)) {
			menuItem.removeListener(SWT.Selection, listener);
		}
	}

	public void appendHeaderMenuDiv() {
		new MenuItem(headerMenu, SWT.SEPARATOR);
	}

	public void appendToCSVColumnsInfo(CSVColumnInfo[] csvColumnInfos) {
		for (CSVColumnInfo csvColumnInfo : csvColumnInfos) {
			appendToCSVColumnsInfo(csvColumnInfo);
		}
	}

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

		menuItem = new MenuItem(fixCellMenu, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, fixCellMenuSelectionListener);
		menuItem.setText("fix this cell");

		menuItem = new MenuItem(fixCellMenu, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, fixCellMenuSelectionListener);
		menuItem.setText("fix this issue for this column");

		menuItem = new MenuItem(fixCellMenu, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, fixCellMenuSelectionListener);
		menuItem.setText("standardize all CAS in this column");

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
		for (Issue issue : csvColumnInfo.getIssues()) {
			if ((issue.getRowNumber() == rowNumSelected) && (!issue.getStatus().equals(Status.RESOLVED))) {
				QACheck qaCheck = issue.getQaCheck();
				if (qaCheck.getReplacement() != null) {
					Matcher matcher = qaCheck.getPattern().matcher(startingText);
					String fixedValue = matcher.replaceFirst(qaCheck.getReplacement());
					tableItem.setText(colNumSelected, fixedValue);
					TableProvider tableProvider = TableKeeper.getTableProvider(key);
					tableProvider.getData().get(rowNumSelected).set(colNumSelected, fixedValue);
					issue.setStatus(Status.RESOLVED);
					colorCell(issue);
				}
			}
		}
	}

	private void fixOneIssue(Issue issue) {
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

	private void standardizeAllCAS(int columnIndex) {
		CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[columnIndex];
		if (!csvColumnInfo.getHeaderString().equals("CAS")) {
			return;
		}
		for (int i = 0; i < table.getItemCount(); i++) {
			TableItem item = table.getItem(i);
			String value = item.getText(columnIndex);
			String fixedValue = Flowable.standardizeCAS(value);
			if (fixedValue != null) {
				TableProvider tableProvider = TableKeeper.getTableProvider(key);
				List<DataRow> dataRowList = tableProvider.getData();
				DataRow toFix = dataRowList.get(i);
				toFix.set(columnIndex, fixedValue);
				item.setText(columnIndex, fixedValue);
			}
		}
		checkOneColumn(columnIndex);
	}

	private class HeaderMenuColumnSelectionListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			System.out.println("HeaderMenuColumnSelectionListener event = " + event);
			System.out.println("event.widget  = " + event.widget);

			if ((event.widget instanceof MenuItem) && (tableColumnSelectedIndex > -1)) {
				MenuItem menuItem = (MenuItem) event.widget;
				String menuItemText = menuItem.getText();
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
//				selectedCSVColumnInfo.setStatus(Status.UNCHECKED);
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
			}
		}
	}

	private static void clearIssueColors(CSVColumnInfo csvColumnInfo) {
		List<Issue> issueList = csvColumnInfo.getIssues();
		if (issueList != null) {
			for (Issue issue : issueList) {
				colorCell(issue.getRowNumber(), issue.getColNumber(),
						SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
			}
		}
	}

	private static int checkOneColumn(int colIndex) {
		int issueCount = 0;
		CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[colIndex];
		if (!csvColumnInfo.getHeaderString().equals(csvColumnDefaultTooltip)) {
			clearIssueColors(csvColumnInfo);
			csvColumnInfo.clearIssues();
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
						}
					}
				}
			}
			issueCount = csvColumnInfo.getIssueCount();
			table.getColumn(colIndex).setToolTipText(issueCount + " issues");
		} else {
			table.getColumn(colIndex).setToolTipText(csvColumnDefaultTooltip);
		}
		return issueCount;
	}

	public static int checkCols() {
		int totalIssueCount = 0;
		for (int colIndex = 0; colIndex < assignedCSVColumnInfo.length; colIndex++) {
			totalIssueCount += checkOneColumn(colIndex);
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
		TableProvider tableProvider = TableKeeper.getTableProvider(key);
		DataRow dataRow = tableProvider.getData().get(issue.getRowNumber());
		String toolTip = "- "+issue.getQaCheck().getDescription()+"\n - "+issue.getQaCheck().getExplanation();
		dataRow.setToolTipValue(issue.getColNumber(), toolTip);
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

	public static int countAssignedColumns() {
		int colsAssigned = 0;
		for (CSVColumnInfo csvColumnInfo : assignedCSVColumnInfo) {
			if (!csvColumnInfo.getHeaderString().equals(csvColumnDefaultTooltip)) {
				colsAssigned++;
			}
		}
		return colsAssigned;
	}

	public static void matchFlowables() {
		for (int colIndex = 0; colIndex<assignedCSVColumnInfo.length;colIndex++){
			CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[colIndex];
			if (csvColumnInfo.getHeaderString().equals("Flowable Name")){
				matchFlowableNames(colIndex);
			}
		}
	}

	private static void matchFlowableNames(int colIndex) {
		for (int row = 0; row<table.getItemCount();row++){
			TableItem item = table.getItem(row);
			String flowableName = item.getText(colIndex);
			Model model = ActiveTDB.model;
//			String query = "select * where {?s ";
			
//			if (model.)
		}
	}

}
