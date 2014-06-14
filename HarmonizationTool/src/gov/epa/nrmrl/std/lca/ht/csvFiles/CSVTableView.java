package gov.epa.nrmrl.std.lca.ht.csvFiles;

import gov.epa.nrmrl.std.lca.ht.dataModels.Flowable;
import gov.epa.nrmrl.std.lca.ht.dataModels.QACheck;
import gov.epa.nrmrl.std.lca.ht.tdb.*;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.*;
//import harmonizationtool.model.Status;
import harmonizationtool.model.TableKeeper;
import harmonizationtool.model.TableProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author Tommy E. Cathey and Tom Transue
 * 
 */
public class CSVTableView extends ViewPart {

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.csvFiles.csvTableView";

	private static String key = null;
	private static TableViewer tableViewer;
	private static Table table;
	private TextCellEditor editor;

	private static List<CSVColumnInfo> availableCSVColumnInfo = new ArrayList<CSVColumnInfo>();
	private static CSVColumnInfo[] assignedCSVColumnInfo;
	private static String csvColumnDefaultColumnHeader = "   -   ";
	private static String csvColumnDefaultTooltip = "Ignore Column";
	private static Menu headerMenu;
	private static Menu columnActionsMenu;
	private static Menu ignoreRowMenu;
	private static Menu fixCellMenu;
	// private static Menu infoMenu;
	private static Text popup;

	private static int rowNumSelected = -1;
	private static int colNumSelected = -1;

	public static List<Integer> rowsToIgnore = new ArrayList<Integer>();

	public CSVTableView() {
	}

	// //=
	// @Override
	// public void createPartControl(final Composite parent) {
	//
	//
	// }
	// //=
	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(null);
		popup = new Text(composite, SWT.BORDER | SWT.WRAP);
		popup.setEditable(false);
		popup.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		popup.setText("");
		popup.setVisible(false);
		popup.setLocation(90, 90);
		popup.setBounds(90, 90, 300, 60);
		// popup.addListener(SWT.Modify, popupResizeListener);

		tableViewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);
		editor = new TextCellEditor(tableViewer.getTable());

		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addListener(SWT.MouseHover, cellSelectionMouseHoverListener);
		table.addListener(SWT.MouseExit, cellSelectionMouseExitListener);
		// table.addListener(SWT.MouseDown, cellSelectionMouseDownListener);
		table.addMouseListener(columnMouseListener);
		// table.addMouseListener(columnMouseListener);
		// table.setSize(composite.getBounds().width-20,
		// composite.getBounds().height-20);
		table.setSize(0, 0);

		initializeIgnoreRowMenu();
		initializeFixRowMenu();
		// popup = new Text(parent,SWT.NONE);
		// popup.setText("");
		// popup.setVisible(false);
		// popup.setLocation(90, 90);
		// initializeInfoMenu();

		parent.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				table.setSize(table.getParent().getSize());
			}
		});
	}

	// private static Listener popupResizeListener = new Listener() {
	// public void handleEvent(Event e) {
	// popup.setVisible(true);
	// popup.setSize(300, 500);
	// System.out.println("popup.getVerticalBar().getVisible()"+popup.getVerticalBar().getVisible());
	//
	// int height = 5;
	// while ((popup.getVerticalBar().getVisible()) && (height < 200)) {
	// System.out.println("popup.getVerticalBar().getVisible()"+popup.getVerticalBar().getVisible());
	// height += 5;
	// popup.setSize(250, height);
	// // popup.getShell().pack(true);
	// }
	// }
	// };

	private static SelectionListener colSelectionListener = new SelectionListener() {

		@Override
		public void widgetSelected(SelectionEvent e) {
			System.out.println("SelectionListener event e= " + e);
			if (e.getSource() instanceof TableColumn) {
				TableColumn col = (TableColumn) e.getSource();
				colNumSelected = table.indexOf(col);
				if (colNumSelected > 0) {
					headerMenu.setVisible(true);
				}
			}
		};

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			if (e.getSource() instanceof TableColumn) {
				System.out.println("SelectionListener event e= " + e);
				TableColumn col = (TableColumn) e.getSource();
				System.out.println("(widgetDefaultSelected) colNumSelected set to " + colNumSelected);
				colNumSelected = table.indexOf(col);
				if (colNumSelected > 0) {
					headerMenu.setVisible(true);
				}
			}
		}

	};

	private class HeaderMenuColumnSelectionListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			// System.out.println("HeaderMenuColumnSelectionListener event = " +
			// event);
			System.out.println("HeaderMenuColumnSelectionListener event.widget  = " + event.widget);

			if ((event.widget instanceof MenuItem) && (colNumSelected > 0)) {
				MenuItem menuItem = (MenuItem) event.widget;
				String menuItemText = menuItem.getText();
				CSVColumnInfo menuCSVColumnInfo = getCSVColumnInfoByHeaderString(menuItemText);
				if (menuCSVColumnInfo == null){
					System.out.println("Not a CSVColumnInfo assignment");
					return;
				}
				CSVColumnInfo selectedCSVColumnInfo = assignedCSVColumnInfo[colNumSelected];

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
				selectedCSVColumnInfo.setLeftJustified(menuCSVColumnInfo.isLeftJustified());
				// selectedCSVColumnInfo.setStatus(Status.UNCHECKED);
				selectedCSVColumnInfo.setIssues(new ArrayList<Issue>());

				if (menuCSVColumnInfo.isUnique()) {
					menuItem.setEnabled(false);
				}
				TableColumn tableColumn = table.getColumn(colNumSelected);
				if (selectedCSVColumnInfo.isLeftJustified()) {
					tableColumn.setAlignment(SWT.LEFT);
				} else {
					tableColumn.setAlignment(SWT.RIGHT);
				}
				tableColumn.setText(menuItemText);
				if (headerMenu.indexOf(menuItem) == 0) {
					tableColumn.setToolTipText(csvColumnDefaultTooltip);
				} else {
					tableColumn.setToolTipText("Assigned");
				}
			}
		}
	}

	private static Listener cellSelectionMouseDownListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			System.out.println("cellSelectionMouseDownListener event " + event);
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
			if (colNumSelected == 0) {
				ignoreRowMenu.setVisible(true);
				// item.setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
			} else {
				CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[clickedCol];
				Issue issueOfThisCell = null;
				for (Issue issue : csvColumnInfo.getIssues()) {
					if (issue.getRowNumber() == clickedRow) {
						issueOfThisCell = issue;
						break;
					}
				}
				if (issueOfThisCell != null) {
					fixCellMenu.setVisible(true);
				} else {
					ignoreRowMenu.setVisible(false);
					fixCellMenu.setVisible(false);
				}
			}
		}
	};

	private static MouseListener columnMouseListener = new MouseListener() {
		@Override
		public void mouseDoubleClick(MouseEvent e) {
			System.out.println("double click event :e =" + e);
		}

		@Override
		public void mouseDown(MouseEvent e) {
			System.out.println("mouse down event :e =" + e);
			if (e.button == 1) {
				leftClick(e);
			}
			else if (e.button == 3) {
				table.deselectAll();
				rightClick(e);
			}
		}

		@Override
		public void mouseUp(MouseEvent e) {
			System.out.println("mouse up event :e =" + e);
		}
		
		private void leftClick(MouseEvent event) {
			System.out.println("cellSelectionMouseDownListener event " + event);
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
			if (clickedCol > 0) {
				table.deselectAll();
				return;
			}

			rowNumSelected = clickedRow;
			colNumSelected = clickedCol;
			ignoreRowMenu.setVisible(true);
		}

		private void rightClick(MouseEvent event) {
			System.out.println("cellSelectionMouseDownListener event " + event);
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
			if (colNumSelected == 0) {
				ignoreRowMenu.setVisible(true);
				// item.setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
			} else {
				CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[clickedCol];
				Issue issueOfThisCell = null;
				for (Issue issue : csvColumnInfo.getIssues()) {
					if (issue.getRowNumber() == clickedRow) {
						issueOfThisCell = issue;
						break;
					}
				}
				if (issueOfThisCell != null) {
					fixCellMenu.setVisible(true);
				} else {
					ignoreRowMenu.setVisible(false);
					fixCellMenu.setVisible(false);
				}
			}
		}
	};

	private static Listener cellSelectionMouseHoverListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			System.out.println("cellSelectionMouseHoverListener event = " + event);
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
			if (hoverCol > 0) {
				CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[hoverCol];
				List<Issue> issuesOfThisCell = new ArrayList<Issue>();
				for (Issue issue : csvColumnInfo.getIssues()) {
					if (issue.getRowNumber() == hoverRow) {
						issuesOfThisCell.add(issue);
					}
				}
				if (issuesOfThisCell.size() > 0) {
					// setInfoMenu(issuesOfThisCell);
					setPopup(issuesOfThisCell);
					popup.setLocation(event.x + 20, event.y + 50);
					popup.setVisible(true);
					System.out.println("popup be visible now!");
					System.out.println("table.getParent().getSize() = " + table.getParent().getSize());

				} else {
					// infoMenu.setVisible(false);
					popup.setVisible(false);
					ignoreRowMenu.setVisible(false);
				}
			}
		}

	};

	private static Listener cellSelectionMouseExitListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			System.out.println("cellSelectionMouseExitListener event = " + event);
			// infoMenu.setVisible(false);
			popup.setVisible(false);
		}

	};
	private Text text;

	private class RowMenuSelectionListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			if (event.widget instanceof MenuItem) {
				System.out.println("RowMenuSelectionListener event = " + event);
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

	private class FixCellMenuSelectionListener implements Listener {

		@Override
		public void handleEvent(Event event) {

			System.out.println("Clicked on a cell with an issue");
			if (event.widget instanceof MenuItem) {
				System.out.println("FixCellMenuSelectionListener event = " + event);
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

	/**
	 * class for generating column labels. This class will handle a variable
	 * number of tableViewerColumns
	 * 
	 * @author tec
	 */
	class MyColumnLabelProvider extends ColumnLabelProvider {
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
				if (myColNum <= size) {
					s = dataRow.getColumnValues().get(myColNum - 1);
				}
			} catch (Exception e) {
				System.out.println("dataRow=" + dataRow);
				e.printStackTrace();
			}
			return s;
		}
	}

	class RowIndexColumnLabelProvider extends ColumnLabelProvider {

		public RowIndexColumnLabelProvider() {
		}

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
				t = dataRow.getRowToolTip();

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
				s = dataRow.getRowNumber() + 1 + "";
			} catch (Exception e) {
				System.out.println("dataRow=" + dataRow);
				e.printStackTrace();
			}
			return s;
		}
	}

	protected CellEditor getCellEditor(Object element) {
		return editor;
	}

	protected boolean canEdit(Object element) {
		return true;
	}

	/**
	 * this method initializes the headerMenu with menuItems and a
	 * HeaderMenuColumnSelectionListener
	 * 
	 * @param menu
	 *            headerMenu which allows user to rename the tableViewerColumns
	 */

	private void initializeHeaderMenu() {
		if (headerMenu != null) {
			headerMenu.dispose();
		}
		CSVColumnInfo ignoreColumnInfo = new CSVColumnInfo(csvColumnDefaultColumnHeader);
		availableCSVColumnInfo.add(ignoreColumnInfo);

		headerMenu = new Menu(table);
		
		MenuItem menuItem;
		menuItem = new MenuItem(headerMenu, SWT.CASCADE);
		menuItem.setText("Column Actions");
		menuItem.setMenu(columnActionsMenu);
//		menuItem.addListener(SWT.Selection, new HeaderMenuColumnSelectionListener());
		
		new MenuItem(headerMenu, SWT.SEPARATOR);
		
		menuItem = new MenuItem(headerMenu, SWT.NORMAL);
		menuItem.setText(ignoreColumnInfo.getHeaderString());
		menuItem.addListener(SWT.Selection, new HeaderMenuColumnSelectionListener());

	}
	
	private void initializeColumnActionsMenu() {
		if (columnActionsMenu != null) {
			columnActionsMenu.dispose();
		}

		columnActionsMenu = new Menu(table);
		
		MenuItem menuItem;
		
		menuItem = new MenuItem(columnActionsMenu, SWT.NORMAL);
		menuItem.setText("Standardize CAS");
		menuItem.addListener(SWT.Selection, new HeaderMenuColumnSelectionListener());

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

	// private void initializeInfoMenu() {
	// infoMenu = new Menu(table);
	// }

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

	public void appendHeaderMenuDiv() {
		new MenuItem(headerMenu, SWT.SEPARATOR);
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

	// private static void setInfoMenu(Issue issueOfThisCell) {
	// while (infoMenu.getItemCount() > 0) {
	// infoMenu.getItem(infoMenu.getItemCount() - 1).dispose();
	// }
	// MenuItem menuItem = new MenuItem(infoMenu, SWT.NORMAL);
	// menuItem.setText("- " + issueOfThisCell.getQaCheck().getDescription());
	//
	// menuItem = new MenuItem(infoMenu, SWT.NORMAL);
	// menuItem.setText("- " + issueOfThisCell.getQaCheck().getExplanation());
	//
	// // menuItem = new MenuItem(infoMenu, SWT.NORMAL);
	// // menuItem.setText(issueOfThisCell.getQaCheck().getSuggestion());
	// }

	// private static void setInfoMenu(List<Issue> issuesOfThisCell) {
	// while (infoMenu.getItemCount() > 0) {
	// infoMenu.getItem(infoMenu.getItemCount() - 1).dispose();
	// }
	// if (issuesOfThisCell.size() == 0) {
	// return; // THIS SHOULDN'T HAPPEN
	// }
	// if (issuesOfThisCell.size() == 1) {
	// setInfoMenu(issuesOfThisCell.get(0));
	// return;
	// }
	//
	// for (int i = 0; i < issuesOfThisCell.size(); i++) {
	// MenuItem menuItem = new MenuItem(infoMenu, SWT.NORMAL);
	// menuItem.setText(i + "- " +
	// issuesOfThisCell.get(i).getQaCheck().getDescription());
	//
	// menuItem = new MenuItem(infoMenu, SWT.NORMAL);
	// menuItem.setText("       - " +
	// issuesOfThisCell.get(i).getQaCheck().getExplanation());
	// }
	// }

	private static void setPopup(List<Issue> issuesOfThisCell) {
		if (issuesOfThisCell.size() == 0) {
			return; // THIS SHOULDN'T HAPPEN
		}
		if (issuesOfThisCell.size() == 1) {
			String popupText = "- " + issuesOfThisCell.get(0).getQaCheck().getDescription();
			popupText += "\n       - " + issuesOfThisCell.get(0).getQaCheck().getExplanation();
			popup.setText(popupText);
			return;
		}
		String popupText = "0 - " + issuesOfThisCell.get(0).getQaCheck().getDescription();
		popupText += "\n       - " + issuesOfThisCell.get(0).getQaCheck().getExplanation();
		for (int i = 1; i < issuesOfThisCell.size(); i++) {

			popupText += "\n" + i + " - " + issuesOfThisCell.get(i).getQaCheck().getDescription();
			popupText += "\n       - " + issuesOfThisCell.get(i).getQaCheck().getExplanation();
		}
		popup.setText(popupText);
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
		assignedCSVColumnInfo[0] = null;
		for (int i = 1; i < table.getColumns().length; i++) {
			assignedCSVColumnInfo[i] = new CSVColumnInfo(csvColumnDefaultColumnHeader);
		}
		colorRowNumberColumn();

		table.setSize(table.getParent().getSize());
		// LISTENER IS NOT PERFECT
	}

	private void colorRowNumberColumn() {
		for (int i = 0; i < table.getItemCount(); i++) {
			TableItem item = table.getItem(i);
			item.setBackground(0, SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
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
			System.out.println("Adding headers");
			List<DataRow> tableData = tableProvider.getData();
			DataRow dataRow = tableData.get(0);
			// while (headerRow.getSize() < dataRow.getSize()) {
			// headerRow.add(defaultHeader);
			// }
			headerRow.add("");
			for (int i = 1; i <= dataRow.getSize(); i++) {
				headerRow.add(defaultHeader);
			}
			System.out.println("Adding columns");

			TableViewerColumn tableViewerColumn = createTableViewerColumn("", 50, 0);
			tableViewerColumn.getColumn().setAlignment(SWT.RIGHT);
			tableViewerColumn.setLabelProvider(new RowIndexColumnLabelProvider());

			for (int i = 0; i < tableProvider.getColumnCount(); i++) {
				System.out.println("  Populating column " + i + 1 + "with data from tableProvider column " + i);
				tableViewerColumn = createTableViewerColumn(defaultHeader, 100, i + 1);
				tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(i + 1));
				// tableViewerColumn.getColumn()._addListener(SWT.MouseDown,
				// columnMouseListener);
			}
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
		tableColumn.addSelectionListener(colSelectionListener);
		// tableColumn.addListener(SWT.MouseDown, (Listener)
		// columnMouseListener);

		if (colNumber > 0) {
			tableColumn.setToolTipText(csvColumnDefaultTooltip);
		}

		return tableViewerColumn;
	}

	public void appendToAvailableCSVColumnInfo(CSVColumnInfo csvColumnInfo) {
		availableCSVColumnInfo.add(csvColumnInfo);

		MenuItem menuItem = new MenuItem(headerMenu, SWT.NORMAL);
		clearMenuItemListeners(menuItem);
		menuItem.addListener(SWT.Selection, new HeaderMenuColumnSelectionListener());
		menuItem.setText(csvColumnInfo.getHeaderString());
	}
	
	public void appendToAvailableCSVColumnInfo(Menu menuParent, CSVColumnInfo csvColumnInfo) {
		availableCSVColumnInfo.add(csvColumnInfo);
		MenuItem menuItem = new MenuItem(menuParent, SWT.NORMAL);
		clearMenuItemListeners(menuItem);
		menuItem.addListener(SWT.Selection, new HeaderMenuColumnSelectionListener());
		menuItem.setText(csvColumnInfo.getHeaderString());
	}

	public void appendToAvailableCSVColumnInfo(CSVColumnInfo[] csvColumnInfos) {
		for (CSVColumnInfo csvColumnInfo : csvColumnInfos) {
			appendToAvailableCSVColumnInfo(csvColumnInfo);
		}
	}
	

	public void appendToAvailableCSVColumnInfo(String fieldType, CSVColumnInfo[] csvColumnInfos) {
		MenuItem menuParent = new MenuItem(headerMenu, SWT.CASCADE);
		menuParent.setText(fieldType);
	    Menu newMenu = new Menu(headerMenu);
	    menuParent.setMenu(newMenu);
		for (CSVColumnInfo csvColumnInfo : csvColumnInfos) {
			appendToAvailableCSVColumnInfo(newMenu , csvColumnInfo);
		}
	}

	private void clearMenuItemListeners(MenuItem menuItem) {
		for (Listener listener : menuItem.getListeners(SWT.Selection)) {
			menuItem.removeListener(SWT.Selection, listener);
		}
	}

	private void fixCurrentlySelectedCell() {
		if (colNumSelected == 0) {
			return;
		}
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
		if (colNumSelected == 0) {
			return;
		}
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
		if (columnIndex == 0) {
			return;
		}
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
				toFix.set(columnIndex - 1, fixedValue);
				item.setText(columnIndex, fixedValue);
			}
		}
		checkOneColumn(columnIndex);
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
		if (colIndex == 0) {
			return 0;
		}
		int issueCount = 0;
		CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[colIndex];
		if (!csvColumnInfo.getHeaderString().equals(csvColumnDefaultColumnHeader)) {
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
		for (int colIndex = 1; colIndex < assignedCSVColumnInfo.length; colIndex++) {
			totalIssueCount += checkOneColumn(colIndex);
		}
		return totalIssueCount;
	}

	public static int autoFixColumn(int colIndex) {
		if (colIndex == 0) {
			return 0;
		}
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
		for (TableItem item : table.getItems()) {
			results.add(item.getText(colIndex));
		}

		// TableProvider tableProvider = TableKeeper.getTableProvider(key);
		// List<DataRow> dataRowList = tableProvider.getData();
		// for (DataRow dataRow : dataRowList) {
		// results.add(dataRow.get(colIndex));
		// }
		return results;
	}

	private static void assignIssue(Issue issue) {
		if (issue.getRowNumber() >= table.getItemCount()) {
			return;
		}
		// TableProvider tableProvider = TableKeeper.getTableProvider(key);
		// DataRow dataRow = tableProvider.getData().get(issue.getRowNumber());
		// String toolTip =
		// "- "+issue.getQaCheck().getDescription()+"\n - "+issue.getQaCheck().getExplanation();
		// dataRow.setToolTipValue(issue.getColNumber(), toolTip);
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
		// for (int i =1;i<assignedCSVColumnInfo.length;i++) {
		// CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[i];
		for (CSVColumnInfo csvColumnInfo : assignedCSVColumnInfo) {
			if (csvColumnInfo != null) {
				if (!csvColumnInfo.getHeaderString().equals(csvColumnDefaultColumnHeader)) {
					colsAssigned++;
				}
			}
		}
		return colsAssigned;
	}

	public static void matchFlowables() {
		for (int colIndex = 1; colIndex < assignedCSVColumnInfo.length; colIndex++) {
			CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[colIndex];
			if (csvColumnInfo.getHeaderString().equals("Flowable Name")) {
				matchFlowableNames(colIndex);
			}
		}
	}

	private static void matchFlowableNames(int colIndex) {
		for (int row = 0; row < table.getItemCount(); row++) {
			TableItem item = table.getItem(row);
			String flowableName = item.getText(colIndex);
			Model model = ActiveTDB.model;
			// String query = "select * where {?s ";

			// if (model.)
		}
	}

}
