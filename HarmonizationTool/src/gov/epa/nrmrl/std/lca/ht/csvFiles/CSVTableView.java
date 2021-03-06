package gov.epa.nrmrl.std.lca.ht.csvFiles;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.nrmrl.std.lca.ht.dataFormatCheck.Issue;
import gov.epa.nrmrl.std.lca.ht.dataFormatCheck.FormatCheck;
import gov.epa.nrmrl.std.lca.ht.dataFormatCheck.Status;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.Flow;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataPropertyProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.dialog.GenericMessageBox;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.FlowContext;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.MatchContexts;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.FlowUnit;
//import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.FlowProperty;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.MatchProperties;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.Flowable;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.MatchFlowables;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.MatchStatus;
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
//import java.util.GregorianCalendar;
//import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * @author Tommy E. Cathey and Tom Transue
 * 
 */
public class CSVTableView extends ViewPart {

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView";

	private static String tableProviderKey = null;

	private static TableViewer tableViewer;
	private static Table table;

	private static TextCellEditor editor;

	private static List<LCADataPropertyProvider> lcaDataPropertyProviders = new ArrayList<LCADataPropertyProvider>();
	private static List<Issue> issueList = new ArrayList<Issue>();
	// REMEMBER THE OFFSET:

	// private static final String csvColumnDefaultColumnHeader = "   -   ";

	private static final String csvColumnDefaultTooltip = "Ignore Column";
	private static final String deassignText = "Remove assignment";
	private static Menu headerMenu;
	private static Menu columnActionsMenu;
	private static Menu rowMenu;
	private static Menu fixCellMenu;
	// private static Menu infoMenu;
	private static Text popup;

	private static int rowNumSelected = -1;
	private static int colNumSelected = -1;
	private static Point clickPoint;

	private static List<Integer> rowsToIgnore = new ArrayList<Integer>();

	private static ViewerSorter sorter;

	private static TableRowFilter rowFilter = new TableRowFilter();
	public static Color orange = new Color(Display.getCurrent(), 255, 128, 0);

	public static boolean preCommit = true;

	private static SortedSet<Integer> flowableColumns = new TreeSet<Integer>();
	private static SortedSet<Integer> propertyColumns = new TreeSet<Integer>();
	private static SortedSet<Integer> contextColumns = new TreeSet<Integer>();
	private static SortedSet<Integer> flowColumns = new TreeSet<Integer>();

	private static List<String> masterFlowUUIDs = null;

	public static List<Integer> getRowsToIgnore() {
		return rowsToIgnore;
	}

	private static Font defaultFont = null;
	private static Font boldFont = null;

	// private static Font boldFont = new Font(Display.getCurrent(), new FontData("Lucida Grande", 11, SWT.BOLD));

	// THESE 6 ARE MANAGED IN FlowsWorkflow, BUT BROUGHT OVER FOR CONVENIENCE
	public static SortedSet<Integer> uniqueFlowableRowNumbers;
	public static SortedSet<Integer> uniqueFlowContextRowNumbers;
	public static SortedSet<Integer> uniqueFlowPropertyRowNumbers;
	public static SortedSet<Integer> uniqueFlowRowNumbers;

	public static SortedSet<Integer> matchedFlowableRowNumbers;
	public static SortedSet<Integer> matchedFlowContextRowNumbers;
	public static SortedSet<Integer> matchedFlowPropertyRowNumbers;
	public static SortedSet<Integer> matchedFlowRowNumbers;

	private static class LCAArrayContentProvider extends ArrayContentProvider {
		public Object[] getElements(Object inputElement) {
			Object[] ret = super.getElements(inputElement);
			return ret;
		}
	}

	public CSVTableView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(null);
		// System.out.println("hello, from sunny CSVTableView!");
		initializeTableViewer(composite);
		initialize();

		parent.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				table.setSize(table.getParent().getSize());
			}
			// THIS IS NOT PERFECT
			// WHEN THE WINDOW IS RESIZED SMALLER, THE TABLE OVER RUNS A LITTLE
		});
		initializePopup(composite);
	}

	private static void initializeTableViewer(Composite composite) {
		tableViewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);

		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);
		// editor = new TextCellEditor(tableViewer.getTable());
		tableViewer.setContentProvider(new CSVTableView.LCAArrayContentProvider());
		tableViewer.setSorter(sorter);
		tableViewer.addFilter(rowFilter);
	}

	private static void initializeTable() {
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addMouseListener(tableMousePointListener);
		table.addSelectionListener(tableSelectionListener);
		table.addListener(SWT.MouseHover, cellSelectionMouseHoverListener);
		table.addListener(SWT.MouseExit, cellSelectionMouseExitListener);

		// table.setSize(10, 10);
	}

	private static SelectionListener tableSelectionListener = new SelectionListener() {
		private void doit(SelectionEvent e) {
			TableItem newTableItem = (TableItem) e.item;
			if (newTableItem == null) {
				return;
			}
			TableItem lastTableItem = null;
			if (rowNumSelected > -1 && rowNumSelected < table.getItemCount()) {
				lastTableItem = table.getItem(rowNumSelected);
			}
			int newRow = table.indexOf(newTableItem);
			if (newRow == rowNumSelected) {
				return;
			}
			rowNumSelected = newRow;
			colNumSelected = getTableColumnNumFromPoint(rowNumSelected, clickPoint);

			if (preCommit && (colNumSelected == 0)) {
				table.select(rowNumSelected);
				rowMenu.setVisible(true);
				return;
			}

			table.deselectAll();

			if (preCommit) {
				return;
			}
			String dataRowNumString = newTableItem.getText(0);
			Integer dataRowNum = Integer.parseInt(dataRowNumString) - 1;
			if (rowsToIgnore.contains(dataRowNum)) {
				return;
			}

			if (defaultFont == null) {
				createFonts(newTableItem);
			}
			if (lastTableItem != null) {
				lastTableItem.setFont(defaultFont);
			}
			newTableItem.setFont(boldFont);
			selectRowColumn();
			return;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			doit(e);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			doit(e);
		}
	};

	private static Listener cellSelectionMouseHoverListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			if (!preCommit) {
				return;
			}
			// System.out.println("cellSelectionMouseHoverListener event = " + event);

			Point ptClick = new Point(event.x, event.y);
			int scrollPos = table.getHorizontalBar().getSelection();
			Point ptLeft = new Point(scrollPos + 1, event.y);

			int hoverRow = 0;
			int hoverCol = 0;
			TableItem item = table.getItem(ptLeft);
			if (item == null) {
				return;
			}
			hoverRow = table.indexOf(item);

			hoverCol = getTableColumnNumFromPoint(hoverRow, ptClick);
			// int dataHoverCol = hoverCol - 1;
			if (hoverCol < 1) {
				return;
			}
			if (hoverCol > 0) {
				TableProvider tableProvider = TableKeeper.getTableProvider(tableProviderKey);
				LCADataPropertyProvider lcaDataPropertyProvider = tableProvider.getLCADataPropertyProvider(hoverCol);
				// CSVColumnInfo csvColumnInfo =
				// tableProvider.getAssignedCSVColumnInfo()[hoverCol];

				if (lcaDataPropertyProvider == null) {
					return;
				}
				List<Issue> issuesOfThisCell = new ArrayList<Issue>();

				for (Issue issue : getIssuesByColumn(hoverCol)) {
					if (issue.getRowNumber() == hoverRow) {
						issuesOfThisCell.add(issue);
					}
				}
				if (issuesOfThisCell.size() > 0) {
					setPopup(issuesOfThisCell);
					// int thing = table.getTopIndex();
					popup.setBounds(event.x + 40, event.y + 10 - (table.getTopIndex() * 15), 300,
							60 * issuesOfThisCell.size());

					// popup.setLocation(event.x + 40, event.y + 10);
					// popup.setBounds(90, 90, 300, 60);
					popup.moveAbove(table);
					popup.setVisible(true);
					System.out.println("popup should be visible now!");

				} else {
					popup.setVisible(false);
					rowMenu.setVisible(false);
				}
			}
		}
	};

	private static Listener cellSelectionMouseExitListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			popup.setVisible(false);
		}

	};

	public static SortedSet<Integer> getFilterRowNumbers() {
		return rowFilter.getFilterRowNumbers();
	}

	public static void setFilterRowNumbersWCopy(SortedSet<Integer> filterRowNumbers) {
		SortedSet<Integer> copyOfUnique = new TreeSet<Integer>();
		for (int rowNumber : filterRowNumbers) {
			copyOfUnique.add(rowNumber);
		}
		rowFilter.setFilterRowNumbers(copyOfUnique);
		reColor();
	}

	public static void clearFilterRowNumbers() {
		rowFilter.clearFilterRowNumbers();
		reColor();
	}

	public static void reColor() {
		if (preCommit) {
			colorByIssues();
		} else {
			colorFlowableRows();
			colorFlowContextRows();
			colorFlowPropertyRows();
			colorFlowRows();
		}
	}

	private static void colorByIssues() {
		TableProvider tableProvider = TableKeeper.getTableProvider(tableProviderKey);
		for (int i = 0; i < table.getColumnCount(); i++) {
			if (tableProvider.getLcaDataProperties()[i] != null) {
				colorRowNumberColumn(i);
			}
		}
	}

	private static class TableRowFilter extends ViewerFilter {
		// private List<Integer> filterRowNumbers = new ArrayList<Integer>();
		private static SortedSet<Integer> filterRowNumbers = new TreeSet<Integer>();

		public SortedSet<Integer> getFilterRowNumbers() {
			if (filterRowNumbers == null) {
				filterRowNumbers = new TreeSet<Integer>();
			}
			return filterRowNumbers;
		}

		public void setFilterRowNumbers(SortedSet<Integer> newFilterRowNumbers) {
			filterRowNumbers = newFilterRowNumbers;
			tableViewer.refresh();
		}

		public void clearFilterRowNumbers() {
			filterRowNumbers.clear();
			tableViewer.refresh();
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			DataRow row = (DataRow) element;
			int dataRowNum = row.getRowNumber();
			return (filterRowNumbers.isEmpty() || filterRowNumbers.contains(dataRowNum));
		}
	}

	private static MouseListener tableMousePointListener = new MouseListener() {
		@Override
		public void mouseDoubleClick(MouseEvent e) {

		}

		@Override
		public void mouseDown(MouseEvent e) {
			if (e.button == 1) {
				clickPoint = new Point(e.x, e.y);
			}
		}

		@Override
		public void mouseUp(MouseEvent e) {
		}
	};

	// private static MouseListener tableMouseListener = new MouseListener() {
	// @Override
	// public void mouseDoubleClick(MouseEvent e) {
	// Point ptClick = new Point(e.x, e.y);
	// TableColumn tableColumn = table.getColumn(getColumnNumSelected(ptClick));
	// if (tableColumn.getWidth() > 30) {
	// tableColumn.setWidth(25);
	// } else {
	// tableColumn.setWidth(100);
	// }
	// // tableViewer.refresh();
	// }
	//
	// @Override
	// public void mouseDown(MouseEvent e) {
	// // System.out.println("mouse down event :e =" + e);
	// // if (e.button == 1) {
	// // leftClick(e);
	// // } else if (e.button == 3) {
	// // table.deselectAll();
	// // rightClick(e);
	// // }
	// }
	//
	// @Override
	// public void mouseUp(MouseEvent e) {
	// if (e.button == 1) {
	// leftClick(e);
	// } else if (e.button == 3) {
	// table.deselectAll();
	// rightClick(e);
	// }
	// // System.out.println("mouse up event :e =" + e);
	// }
	//
	// private void leftClick(MouseEvent event) {
	// // System.out.println("cellSelectionMouseDownListener event " + event);
	// int scrollPos = table.getHorizontalBar().getSelection();
	// int index = table.getSelectionIndex();
	// Point ptLeft = new Point(1 - scrollPos, event.y);
	// Point ptClick = new Point(event.x, event.y);
	//
	// TableItem newTableItem = table.getItem(ptLeft);
	//
	//
	// if (newTableItem == null) {
	// return;
	// }
	// TableItem lastTableItem = null;
	// if (rowNumSelected > -1 && rowNumSelected < table.getItemCount()) {
	// lastTableItem = table.getItem(rowNumSelected);
	// }
	// int newRow = table.indexOf(newTableItem);
	// if (newRow == rowNumSelected) {
	// return;
	// }
	// rowNumSelected = newRow;
	// colNumSelected = getTableColumnNumFromPoint(rowNumSelected, ptClick);
	//
	// if (preCommit && (colNumSelected == 0)) {
	// table.select(rowNumSelected);
	// rowMenu.setVisible(true);
	// return;
	// }
	//
	// table.deselectAll();
	//
	// if (preCommit) {
	// return;
	// }
	// String dataRowNumString = newTableItem.getText(0);
	// Integer dataRowNum = Integer.parseInt(dataRowNumString) - 1;
	// if (rowsToIgnore.contains(dataRowNum)) {
	// return;
	// }
	//
	// if (defaultFont == null) {
	// createFonts(newTableItem);
	// }
	// if (lastTableItem != null) {
	// lastTableItem.setFont(defaultFont);
	// }
	// newTableItem.setFont(boldFont);
	//
	// matchRowContents();
	// return;
	// // }
	//
	// }
	//
	// private void rightClick(MouseEvent event) {
	// // System.out.println("cellSelectionMouseDownListener event " + event);
	// // Point ptLeft = new Point(1, event.y);
	// Point ptClick = new Point(event.x, event.y);
	// int clickedRow = 0;
	// int clickedCol = 0;
	// TableItem item = table.getItem(ptClick);
	// if (item == null) {
	// return;
	// }
	// clickedRow = table.indexOf(item);
	// clickedCol = getTableColumnNumFromPoint(clickedRow, ptClick);
	// if (clickedCol < 0) {
	// return;
	// }
	//
	// rowNumSelected = clickedRow;
	// colNumSelected = clickedCol;
	// if (colNumSelected == 0) {
	// rowMenu.setVisible(true);
	// } else {
	// Issue issueOfThisCell = null;
	// // boolean firstResolvable = false;
	// initializeFixCellMenu(false);
	//
	// for (Issue issue : getIssuesByColumn(clickedCol)) {
	// if (issue.getRowNumber() == rowNumSelected) {
	// issueOfThisCell = issue;
	// if (issue.getQaCheck().getReplacement() != null) {
	// // firstResolvable = true;
	// initializeFixCellMenu(true);
	// } else {
	// // firstResolvable = false;
	// }
	// break;
	// }
	// }
	// if (issueOfThisCell != null) {
	// fixCellMenu.setVisible(true);
	// } else {
	// rowMenu.setVisible(false);
	// fixCellMenu.setVisible(false);
	// }
	// }
	// }
	// };

	private static void createFonts(TableItem tableItem) {
		defaultFont = tableItem.getFont();
		FontData boldFontData = defaultFont.getFontData()[0];
		boldFontData.setStyle(SWT.BOLD);
		boldFont = new Font(Display.getCurrent(), boldFontData);

	}

	// private static int getColumnNumSelected(Point point) {
	// int clickedRow = getRowNumSelected(point);
	// int clickedCol = getTableColumnNumFromPoint(clickedRow, point);
	// if (clickedCol < 0) {
	// return -1;
	// }
	// return clickedCol;
	// }

	public static void selectRowColumn() {
		if (colNumSelected < 0)
			return;
		TableProvider tableProvider = TableKeeper.getTableProvider(tableProviderKey);
		LCADataPropertyProvider lcaDataPropertyProvider = tableProvider.getLcaDataProperties()[colNumSelected];

		int dataRowNum = getDataRowNumberFromTableRowNumber(rowNumSelected);
		MatchFlowables.update(dataRowNum);
		MatchContexts.update(dataRowNum);
		MatchProperties.update(dataRowNum);
		if (lcaDataPropertyProvider == null) {
			return;
		}
		Resource columnType = lcaDataPropertyProvider.getRDFClass();
		if (columnType == null) {
			return;
		}
		if (columnType.equals(FlowUnit.rdfClass)) {
			try {
				Util.showView(MatchProperties.ID);
			} catch (PartInitException e1) {
				e1.printStackTrace();
			}
		} else if (columnType.equals(FlowContext.rdfClass)) {
			try {
				Util.showView(MatchContexts.ID);
			} catch (PartInitException e1) {
				e1.printStackTrace();
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

	private static int getRowNumSelected(Point point) {
		TableItem item = table.getItem(point);
		if (item == null) {
			return -1;
		}
		return table.indexOf(item);
	}

	private static void initializePopup(Composite composite) {
		// popup.addListener(SWT.Modify, popupResizeListener);
		popup = new Text(composite, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		popup.setEditable(false);
		popup.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		popup.moveAbove(composite);
		popup.setText("User Data Table");
		popup.setVisible(true);
		popup.setBounds(0, 0, 300, 60);
	}

	public static void initializeRowMenu() {
		rowMenu = new Menu(table);
		RowMenuSelectionListener rowMenuSelectionListener = new RowMenuSelectionListener();

		MenuItem menuItem;

		if (preCommit) {
			menuItem = new MenuItem(rowMenu, SWT.NORMAL);
			menuItem.addListener(SWT.Selection, rowMenuSelectionListener);
			menuItem.setText("ignore row");

			menuItem = new MenuItem(rowMenu, SWT.NORMAL);
			menuItem.addListener(SWT.Selection, rowMenuSelectionListener);
			menuItem.setText("use row");
		}
	}

	private static class RowMenuSelectionListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			if (event.widget instanceof MenuItem) {
				String menuItemText = ((MenuItem) event.widget).getText();
				if (menuItemText.equals("ignore row")) {
					for (TableItem tableItem : table.getSelection()) {
						tableItem.setForeground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
						int dataRowNumber = getDataRowNumberFromTableRowNumber(table.indexOf(tableItem));
						if (!rowsToIgnore.contains(dataRowNumber)) {
							rowsToIgnore.add(dataRowNumber);
						}
					}
				} else if (menuItemText.equals("use row")) {
					for (TableItem tableItem : table.getSelection()) {
						tableItem.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
						int dataRowNumber = getDataRowNumberFromTableRowNumber(table.indexOf(tableItem));
						if (rowsToIgnore.contains(dataRowNumber)) {
							rowsToIgnore.remove(rowsToIgnore.indexOf(dataRowNumber));
						}
					}
				}
			}
		}
	}

	private static void initializeFixCellMenu(boolean auto) {
		if (fixCellMenu != null) {
			fixCellMenu.dispose();
		}
		fixCellMenu = new Menu(table);

		MenuItem menuItem = new MenuItem(fixCellMenu, SWT.NORMAL);
		if (auto) {
			menuItem.addListener(SWT.Selection, new FixCellMenuSelectionListener());
			menuItem.setText("Auto-resolve issue");
			// } else {
			// menuItem.addListener(SWT.Selection, new EditCellMenuSelectionListener());
			// menuItem.setText("Edit this cell");
			// menuItem.setText("Ignore this cell");

		}
	}

	private static class FixCellMenuSelectionListener implements Listener {
		@Override
		public void handleEvent(Event event) {
			if (!(event.widget instanceof MenuItem)) {
				return;
			}
			// ignoreCurrentlySelectedCell();
		}
	}

	private static class EditCellMenuSelectionListener implements Listener {
		@Override
		public void handleEvent(Event event) {
			if (!(event.widget instanceof MenuItem)) {
				return;
			}
			fixCurrentlySelectedCell();
			// TableColumn tableColumn = table.getColumn(colNumSelected);
			// TableItem tableItem = table.getItem(rowNumSelected);
			// tableItem.get

		}
	}

	private static void fixCurrentlySelectedCell() {
		// int dataColNumSelected = colNumSelected - 1;
		if (colNumSelected == 0) {
			return;
		}
		TableProvider tableProvider = TableKeeper.getTableProvider(tableProviderKey);
		LCADataPropertyProvider lcaDataPropertyProvider = tableProvider.getLCADataPropertyProvider(colNumSelected);

		// CSVColumnInfo csvColumnInfo =
		// tableProvider.getAssignedCSVColumnInfo()[colNumSelected];
		// CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[colNumSelected];
		if (lcaDataPropertyProvider == null) {
			return;
		}
		TableItem tableItem = table.getItem(rowNumSelected);
		String startingText = tableItem.getText(colNumSelected);
		for (Issue issue : getIssuesByColumn(colNumSelected)) {
			if ((issue.getRowNumber() == rowNumSelected) && (!issue.getStatus().equals(Status.RESOLVED))) {
				FormatCheck qaCheck = issue.getQaCheck();
				if (qaCheck.getReplacement() != null) {
					Matcher matcher = qaCheck.getPattern().matcher(startingText);
					String fixedValue = matcher.replaceFirst(qaCheck.getReplacement());
					tableItem.setText(colNumSelected, fixedValue);
					tableProvider.getData().get(rowNumSelected).set(colNumSelected - 1, fixedValue);
					issue.setStatus(Status.RESOLVED);
					colorCell(issue);
				}
			}
		}
	}

	// WHAT IS THIS THING BELOW?
	protected CellEditor getCellEditor(Object element) {
		return editor;
	}

	// WHAT IS THIS THING BELOW?
	protected boolean canEdit(Object element) {
		return true;
	}

	public static class LCACellModifier implements ICellModifier {

		@Override
		public Object getValue(Object element, String property) {
			int index = Integer.valueOf(property);
			if (index >= 0)
				return ((DataRow) element).get(index);
			return null;
		}

		// Allows modification of any cell with an issue
		@Override
		public boolean canModify(Object element, String property) {
			int index = Integer.valueOf(property);
			DataRow row = (DataRow) element;
			List<Issue> issues = getIssuesByColumn(index + 1);

			for (Issue issue : issues)
				if (issue.getRowNumber() == row.getRowNumber()) {
					return true;
				}

			return false;
		}

		@Override
		public void modify(Object element, String property, Object newValue) {
			DataRow row = (DataRow) ((TableItem) element).getData();
			int index = Integer.valueOf(property);

			Object oldValue = row.get(index);
			if (!oldValue.equals(newValue)) {
				TableItem item = (TableItem) element;
				// tableViewer.update() resets background colors. Not sure how to tell it not to, so save and restore
				// instead
				Color[] c = new Color[row.getSize() + 1];
				for (int i = 0; i < c.length; ++i)
					c[i] = item.getBackground(i);
				row.set(index, newValue.toString());
				tableViewer.update(row, new String[] { property });
				for (int i = 0; i < c.length; ++i)
					item.setBackground(i, c[i]);

				// TODO - Tom, store newValue in the TDB. provider has info on what property is being edited, and row
				// has the TDB object to modify.
				LCADataPropertyProvider lcaDataPropertyProvider = lcaDataPropertyProviders.get(index);
			}

		}

	}

	// ===========================================
	public static void update(String key) {
		table.removeAll();
		// Don't do this (below), because it screws up re-load....
		// clearItemCounts();
		removeColumns();
		flowableColumns.clear();
		propertyColumns.clear();
		contextColumns.clear();
		flowColumns.clear();

		tableProviderKey = key;
		createColumns();
		resetFields();
		// initializeHeaderMenu();
		setHeaderMenu(1);

		TableProvider tableProvider = TableKeeper.getTableProvider(key);
		Date loadStartDate = new Date();

		tableViewer.setInput(tableProvider.getData());
		CellEditor[] editors = new CellEditor[lcaDataPropertyProviders.size()];
		String[] columnProperties = new String[editors.length];
		for (int i = 0; i < editors.length; ++i) {
			editors[i] = new TextCellEditor(getTable());
			columnProperties[i] = String.valueOf(i - 1);
		}
		tableViewer.setCellEditors(editors);
		tableViewer.setCellModifier(new LCACellModifier());
		tableViewer.setColumnProperties(columnProperties);

		Date loadEndDate = new Date();

		long secondsRead = ((loadEndDate.getTime() - loadStartDate.getTime()) / 1000);

		// System.out.println("# CSVTableView load time (in seconds): " + secondsRead);

		table.setSize(table.getParent().getSize());
		for (int i = 0, n = table.getColumnCount(); i < n; i++) {
			table.getColumn(i).pack();
			int width = table.getColumn(i).getWidth();
			if (width < 20) {
				table.getColumn(i).setWidth(20);
			} else if (width > 400 && table.getHorizontalBar().getVisible()) {
				table.getColumn(i).setWidth(400);
			}
		}
		initializeColumnActionsMenu();
		initializeOtherViews();

		tableProvider.colorExistingRows();
	}

	// private static void tinyRoutine(List<DataRow> data) {
	// tableViewer.setInput(data);
	// }

	private static void createColumns() {
		// System.out.println("key=" + tableProviderKey);
		if (tableProviderKey != null) {
			TableProvider tableProvider = TableKeeper.getTableProvider(tableProviderKey);
			if (tableProvider.getData() == null) {
				return;
			}
			if (tableProvider.getData().size() == 0) {
				return;
			}
			// String defaultHeader = headerMenu.getItem(0).getText();
			DataRow headerRow = tableProvider.getHeaderRow();
			int columnCount = tableProvider.getData().get(0).getSize();
			while (headerRow.getSize() <= columnCount) {
				headerRow.add("");
			}

			// headerRow.add("");
			// headerRow.set(0, "");
			for (int i = 1; i <= columnCount; i++) {
				LCADataPropertyProvider lcaDataPropertyProvider = tableProvider.getLCADataPropertyProvider(i);
				if (lcaDataPropertyProvider == null) {
					headerRow.set(i, "- " + i + " -");
				} else {
					headerRow.set(
							i,
							lcaDataPropertyProvider.getPropertyClass() + ": "
									+ lcaDataPropertyProvider.getPropertyName());
				}
			}
			// System.out.println("Adding columns");

			// COLUMN ZERO
			TableViewerColumn tableViewerColumn = createTableViewerColumn("", 200, 0);
			tableViewerColumn.getColumn().setAlignment(SWT.RIGHT); // DOESN'T
																	// WORK ON
																	// COL
																	// ZERO?!?
			tableViewerColumn.setLabelProvider(new RowIndexColumnLabelProvider());

			for (int i = 0; i < tableProvider.getColumnCount(); i++) {
				int iplus1 = i + 1;
				// System.out.println("Populating column " + iplus1 + " with data from tableProvider column " + i);
				// tableViewerColumn =
				// createTableViewerColumn(csvColumnDefaultColumnHeader, 100,
				// iplus1);
				tableViewerColumn = createTableViewerColumn(headerRow.get(iplus1), 100, iplus1);
				tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(iplus1));
			}
		}
	}

	private static void colorRowNumberColumn(int colNumber) {
		for (int i = 0; i < table.getItemCount(); i++) {
			TableItem item = table.getItem(i);
			item.setBackground(colNumber, null);
		}
		List<Integer> origRowsToYellow = new ArrayList<Integer>();
		for (Issue issue : getIssuesByColumn(colNumber)) {
			if (!issue.getStatus().equals(Status.RESOLVED)) {
				origRowsToYellow.add(issue.getRowNumber());
			}
		}
		SortedSet<Integer> filterRowNumbers = getFilterRowNumbers();
		int curRow = 0;

		// if (getFilterRowNumbers() != null) {
		if (!filterRowNumbers.isEmpty()) {
			for (int integer : filterRowNumbers) {
				if (origRowsToYellow.contains(integer)) {
					table.getItem(curRow).setBackground(colNumber, SWTResourceManager.getColor(SWT.COLOR_YELLOW));
				}
				curRow++;
			}
			// }
		}
		if (curRow == 0) {
			for (TableItem tableItem : table.getItems()) {
				if (origRowsToYellow.contains(curRow)) {
					tableItem.setBackground(colNumber, SWTResourceManager.getColor(SWT.COLOR_YELLOW));
				}
				curRow++;
			}
		}
	}

	private static void setHeaderMenu(int option) {
		if (headerMenu != null) {
			headerMenu.dispose();
		}

		headerMenu = new Menu(table);

		MenuItem menuItem;
		if (option == 1) { // NOT ASSIGNED YET
			addLCADataPropertiesToHeaderMenu();
		} else if (option == 2) { // IS ASSIGNED
			if (preCommit) {
				String colTypeGeneral = TableKeeper.getTableProvider(tableProviderKey)
						.getLCADataPropertyProvider(colNumSelected).getPropertyClass();
				String colTypeSpecific = TableKeeper.getTableProvider(tableProviderKey)
						.getLCADataPropertyProvider(colNumSelected).getPropertyName();

				final TableColumn tableColumn = table.getColumn(colNumSelected);
				String toolTip = tableColumn.getToolTipText();

				Pattern pattern = Pattern.compile("^([1-9]\\d*) issues");
				Matcher matcher = pattern.matcher(toolTip);
				int issueCount = 0;
				if (matcher.find()) {
					String count = matcher.group(1);

					issueCount = Integer.parseInt(count);

					menuItem = new MenuItem(headerMenu, SWT.NORMAL);
					menuItem.setText("Show only " + count + " issues");
					menuItem.addListener(SWT.Selection, new FilterByIssuesListener());

				} else {
					SortedSet<Integer> filterRowNumbers = getFilterRowNumbers();
					if (!filterRowNumbers.isEmpty()) {
						final int newIssueCount = getIssuesByColumn(colNumSelected).size();

						menuItem = new MenuItem(headerMenu, SWT.NORMAL);
						menuItem.setText("Show all rows");
						menuItem.addListener(SWT.Selection, new Listener() {
							@Override
							public void handleEvent(Event event) {
								tableColumn.setToolTipText(newIssueCount + " issues");
								clearFilterRowNumbers();
							}
						});

						/*
						 * TAHOWARD TODO = The three lines below will be the new ones to allow editing. See below for
						 * more details.
						 */
						// menuItem = new MenuItem(headerMenu, SWT.NORMAL);
						// menuItem.setText("Edit content of fields");
						// menuItem.addListener(SWT.Selection, new EditFieldsListener());
						/*
						 * TAHOWARD TODO = Create an EditFieldsListener() so that people can edit the fields. Make sure
						 * that there is some place in the code for me to save the old value and put it in the TDB as a
						 * "originalValue" type field value.
						 */

						menuItem = new MenuItem(headerMenu, SWT.NORMAL);
						menuItem.setText("Change issue fields to blank");
						menuItem.addListener(SWT.Selection, new BlankFieldsListener());

					}
				}

				if (colTypeGeneral.equals(Flowable.label) && colTypeSpecific.equals(Flowable.casString)) {
					if (issueCount > 0) {
						menuItem = new MenuItem(headerMenu, SWT.NORMAL);
						menuItem.setText("Auto-resolve " + issueCount + " Issues");
						menuItem.addListener(SWT.Selection, new AutoResolveColumnListener());
					}

					// DONE CFowler: Standardize CAS functionality disabled with the following three comments.
					// menuItem = new MenuItem(headerMenu, SWT.NORMAL);
					// menuItem.setText("Standardize CAS");
					// menuItem.addListener(SWT.Selection, new StandardizeAllCASListener());

				} else {

					if (issueCount > 0) {
						// System.out.println("We gots issues: " + toolTip);
						String count = matcher.group(1);

						menuItem = new MenuItem(headerMenu, SWT.NORMAL);
						menuItem.setText("Auto-resolve " + count + " Issues");
						menuItem.addListener(SWT.Selection, new AutoResolveColumnListener());

						menuItem = new MenuItem(headerMenu, SWT.NORMAL);
						menuItem.setText("Change issue fields to blank");
						menuItem.addListener(SWT.Selection, new BlankFieldsListener());
					}
				}

				new MenuItem(headerMenu, SWT.SEPARATOR);

				menuItem = new MenuItem(headerMenu, SWT.NORMAL);
				menuItem.setText(deassignText);
				menuItem.addListener(SWT.Selection, new HeaderMenuColumnAssignmentListener());
			} else {
				// WHAT MENU OPTIONS MAKE SENSE ONCE COMMIT IS COMPLETE?
				new MenuItem(headerMenu, SWT.SEPARATOR);

			}
		}
	}

	private static void initializeColumnActionsMenu() {
		if (columnActionsMenu != null) {
			columnActionsMenu.dispose();
		}
		columnActionsMenu = new Menu(headerMenu);
		for (MenuItem menuItem : headerMenu.getItems()) {
			if (menuItem.getText().equals("Column Actions")) {
				menuItem.setMenu(columnActionsMenu);
			}
		}
		MenuItem menuItem;

		menuItem = new MenuItem(columnActionsMenu, SWT.NORMAL);
		menuItem.setText("Auto-resolve Column");
		menuItem.addListener(SWT.Selection, new AutoResolveColumnListener());

		menuItem = new MenuItem(columnActionsMenu, SWT.NORMAL);
		menuItem.setText("Standardize CAS");
		menuItem.addListener(SWT.Selection, new StandardizeAllCASListener());

		new MenuItem(headerMenu, SWT.SEPARATOR);

		menuItem = new MenuItem(headerMenu, SWT.NORMAL);
		menuItem.setText(deassignText);
		menuItem.addListener(SWT.Selection, new HeaderMenuColumnAssignmentListener());

	}

	private static void deAssignColumn() {
		TableProvider tableProvider = TableKeeper.getTableProvider(tableProviderKey);
		tableProvider.setLCADataPropertyProvider(colNumSelected, null);
		TableColumn tableColumn = table.getColumn(colNumSelected);
		tableColumn.setText("- " + colNumSelected + " -");
		tableColumn.setToolTipText(csvColumnDefaultTooltip);
		LCADataPropertyProvider.storeOneCurrentColumnAssignment(colNumSelected);
	}

	private static class StandardizeAllCASListener implements Listener {
		@Override
		public void handleEvent(Event event) {
			// int dataColNumSelected = colNumSelected - 1;
			if (colNumSelected == 0) {
				return;
			}
			TableProvider tableProvider = TableKeeper.getTableProvider(tableProviderKey);
			LCADataPropertyProvider lcaDataPropertyProvider = tableProvider.getLCADataPropertyProvider(colNumSelected);

			// CSVColumnInfo csvColumnInfo =
			// tableProvider.getAssignedCSVColumnInfo()[colNumSelected];
			if (lcaDataPropertyProvider == null) {
				return;
			}
			if (!lcaDataPropertyProvider.getPropertyClass().equals(Flowable.label)) {
				return;
			}
			if (!lcaDataPropertyProvider.getPropertyName().equals("CAS")) {
				return;
			}
			clearIssues(colNumSelected);
			Map<Integer, Issue> issuesThisCol = new HashMap<Integer, Issue>();
			for (Issue issue : issueList) {
				if (issue.getColNumber() == colNumSelected) {
					issuesThisCol.put(issue.getRowNumber(), issue);
				}
			}
			for (int i = 0; i < table.getItemCount(); i++) {
				TableItem item = table.getItem(i);
				int rowNum = getDataRowNumberFromTableRowNumber(i);
				String value = item.getText(colNumSelected);
				String fixedValue = Flowable.standardizeCAS(value);
				if (fixedValue != null) {
					if (issuesThisCol.containsKey(rowNum)) {
						issuesThisCol.get(rowNum).setStatus(Status.RESOLVED);
						colorCell(issuesThisCol.get(rowNum));
						colorCell(i, colNumSelected, null);
					}

					// TableProvider tableProvider =
					// TableKeeper.getTableProvider(tableProviderKey);

					List<DataRow> dataRowList = tableProvider.getData();
					DataRow toFix = dataRowList.get(i);
					toFix.set(colNumSelected - 1, fixedValue);
					item.setText(colNumSelected, fixedValue);
				}
			}

			checkOneColumn(colNumSelected);
			colorRowNumberColumn(colNumSelected);
		}
	}

	private static class AutoResolveColumnListener implements Listener {
		@Override
		public void handleEvent(Event event) {
			if (colNumSelected == 0) {
				return;
			}
			int resolvedCount = 0;
			for (Issue issue : getIssuesByColumn(colNumSelected)) {
				if (fixOneIssue(issue)) {
					resolvedCount++;
				}
			}
			TableColumn tableColumn = table.getColumn(colNumSelected);
			String toolTip = tableColumn.getToolTipText();
			Pattern pattern = Pattern.compile("^(.*?)(\\d+)(.*?)$");
			Matcher matcher = pattern.matcher(toolTip);
			if (matcher.find()) {
				String preString = matcher.group(1);
				String countString = matcher.group(2);
				String postString = matcher.group(3);

				Integer previousCount = Integer.parseInt(countString);
				int newCount = previousCount - resolvedCount;
				tableColumn.setToolTipText(preString + newCount + postString);
			}
		}
	}

	private static class BlankFieldsListener implements Listener {
		@Override
		public void handleEvent(Event event) {
			if (colNumSelected == 0) {
				return;
			}
			// System.out.println("rowNumSelected = " + rowNumSelected);
			// Point point = new Point(event.x, event.y);
			// int rowNum = getRowNumSelected(point);
			// System.out.println("rowNum = " + rowNum);

			// String[] options = new String[2];
			// options[0] = "Cancel";
			// options[1] = "Confirm Deletion";
			MessageDialog messageDialog = new MessageDialog(event.display.getActiveShell(), "Confirm", null,
					"Are you sure you wish to change identified fields in this column to blank?",
					MessageDialog.QUESTION, new String[] { "Cancel", "Confirm Deletion" }, 0);
			// messageDialog.create();
			if (messageDialog.open() == 1) {

				TableProvider tableProvider = TableKeeper.getTableProvider(tableProviderKey);

				for (Issue issue : getIssuesByColumn(colNumSelected)) {
					issue.setStatus(Status.RESOLVED);
					DataRow dataRow = tableProvider.getData().get(issue.getRowNumber());
					dataRow.set(issue.getColNumber() - 1, "");
					SortedSet<Integer> filterRowNumbers = getFilterRowNumbers();
					if (filterRowNumbers != null) {
						if (filterRowNumbers.isEmpty()) {
							table.getItem(rowNumSelected).setText(issue.getColNumber(), "");
							colorCell(rowNumSelected, colNumSelected, issue.getStatus().getColor());

						} else {
							Iterator<Integer> iterator = filterRowNumbers.iterator();
							for (int i = 0; i < filterRowNumbers.size(); i++) {
								if (iterator.next() == issue.getRowNumber()) {
									table.getItem(i).setText(issue.getColNumber(), "");
									colorCell(rowNumSelected, colNumSelected, issue.getStatus().getColor());

								}
							}
						}
					}
				}
				TableColumn tableColumn = table.getColumn(colNumSelected);
				tableColumn.setToolTipText("0 issues");
			}
		}
	}

	// public static void selectNext() {
	// if (rowNumSelected < (table.getItemCount() - 1)) {
	// TableItem lastTableItem = table.getItem(rowNumSelected);
	// rowNumSelected++;
	// TableItem tableItem = table.getItem(rowNumSelected);
	// table.deselectAll();
	// if (defaultFont == null) {
	// createFonts(tableItem);
	// }
	// lastTableItem.setFont(defaultFont);
	// tableItem.setFont(boldFont);
	// selectRowColumn();
	// }
	// }

	/**
	 * This method advances the highlighted (selected) row in the User Data table.  If <strong>nextUnmatched</strong>
	 * is <i>false</i> the next visible row in the table will become selected.  If <strong>nextUnmatched</strong>
	 * is <i>true</i>, the next unmatched item of the type specified by the calling Class becomes the new highlighted
	 * row.
	 * @param String <strong>viewCallerID</strong> is the Class ID string associated with the calling Class.
	 * @param boolean <strong>nextUnmatched</strong> tells the method to advance to the next unmatched item (if true)
	 * or the next item (if false).
	 */
	public static void selectNext(String viewCallerID, boolean nextUnmatched) {
		selectNext(viewCallerID, nextUnmatched, false);
	}

	private static void selectNext(String viewCallerID, boolean nextUnmatched, boolean searchFromStart) {
		int origSelection = rowNumSelected;
		if (rowNumSelected < 1 || rowNumSelected >= table.getItemCount() || searchFromStart) {
			rowNumSelected = 0;
		}

		int nextTableRowNum = rowNumSelected;
		int nextDataRowNum = -1;
		int curDataRowNum = getDataRowNumberFromTableRowNumber(rowNumSelected);
		if (searchFromStart)
			--curDataRowNum; // Needed to allow selection of row 0
		if (viewCallerID.equals(MatchContexts.ID)) {
			for (int i : uniqueFlowContextRowNumbers) {
				if (i > curDataRowNum) {
					if (!nextUnmatched) {
						nextDataRowNum = i;
						break;
					} else if (!matchedFlowContextRowNumbers.contains(i)) {
						nextDataRowNum = i;
						break;
					}
				}
			}
			if (nextDataRowNum == -1) {
				if (searchFromStart) {
					if (!CSVTableView.preCommit)
						new GenericMessageBox(Display.getDefault().getActiveShell(), "Alert", "No results found.");
					return;
				}

				selectNext(viewCallerID, nextUnmatched, true);
				return;
			}
			for (int i = rowNumSelected; i < table.getItems().length; i++) {
				int nextRowNum = getDataRowNumberFromTableRowNumber(i);
				if (nextRowNum == nextDataRowNum) {
					nextTableRowNum = i;
					break;
				}
			}
			for (int j = contextColumns.size() - 1; j >= 0; j--) {
				int colNum = (int) contextColumns.toArray()[j];
				TableColumn tableColumn = table.getColumn(colNum);
				table.showColumn(tableColumn);
			}
			MatchContexts.update(nextDataRowNum);
		} else if (viewCallerID.equals(MatchProperties.ID)) {
			for (int i : uniqueFlowPropertyRowNumbers) {
				if (i > curDataRowNum) {
					if (!nextUnmatched) {
						nextDataRowNum = i;
						break;
					} else if (!matchedFlowPropertyRowNumbers.contains(i)) {
						nextDataRowNum = i;
						break;
					}
				}
			}
			if (nextDataRowNum == -1) {
				if (searchFromStart) {
					if (!CSVTableView.preCommit)
						new GenericMessageBox(Display.getDefault().getActiveShell(), "Alert", "No results found.");
					return;
				}

				selectNext(viewCallerID, nextUnmatched, true);
				return;
			}
			for (int i = rowNumSelected; i < table.getItems().length; i++) {
				int nextRowNum = getDataRowNumberFromTableRowNumber(i);
				if (nextRowNum == nextDataRowNum) {
					nextTableRowNum = i;
					break;
				}
			}
			for (int j = propertyColumns.size() - 1; j >= 0; j--) {
				int colNum = (int) propertyColumns.toArray()[j];
				TableColumn tableColumn = table.getColumn(colNum);
				table.showColumn(tableColumn);
			}
			MatchProperties.update(nextDataRowNum);
		} else if (viewCallerID.equals(MatchFlowables.ID)) {
			for (int i : uniqueFlowableRowNumbers) {
				if (i > curDataRowNum) {
					if (!nextUnmatched) {
						nextDataRowNum = i;
						break;
					} else if (!matchedFlowableRowNumbers.contains(i)) {
						nextDataRowNum = i;
						break;
					}
				}
			}
			if (nextDataRowNum == -1) {
				if (searchFromStart) {
					if (!CSVTableView.preCommit)
						new GenericMessageBox(Display.getDefault().getActiveShell(), "Alert", "No results found.");
					return;
				}

				selectNext(viewCallerID, nextUnmatched, true);
				return;
			}
			for (int i = rowNumSelected; i < table.getItems().length; i++) {
				int nextRowNum = getDataRowNumberFromTableRowNumber(i);
				if (nextRowNum == nextDataRowNum) {
					nextTableRowNum = i;
					break;
				}
			}
			for (int j = flowableColumns.size() - 1; j >= 0; j--) {
				int colNum = (int) flowableColumns.toArray()[j];
				TableColumn tableColumn = table.getColumn(colNum);
				table.showColumn(tableColumn);
			}
			MatchFlowables.update(nextDataRowNum);
		}
		if (searchFromStart) {
			new GenericMessageBox(Display.getDefault().getActiveShell(), "Alert",
					"No more results, returning to the top of the list.");
			rowNumSelected = origSelection;
		}
		selectTableRow(nextTableRowNum);
		// matchRowContents();

		try {
			Util.showView(viewCallerID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	public static void selectTableRow(int tableRowNum) {
		if (tableRowNum < 0 || tableRowNum > table.getItemCount()) {
			return;
		}
		TableItem tableItem = table.getItem(rowNumSelected);
		tableItem.setFont(defaultFont);
		rowNumSelected = tableRowNum;
		TableItem newTableItem = table.getItem(rowNumSelected);
		table.select(rowNumSelected);
		table.deselectAll();
		table.setTopIndex(rowNumSelected);
		if (defaultFont == null) {
			createFonts(tableItem);
		}
		newTableItem.setFont(boldFont);
	}

	// public static void selectNext(String viewCallerID) {
	// // System.out.println("RowNumSelected = " + rowNumSelected);
	// // System.out.println("table.getSelectionIndex() = " + table.getSelectionIndex());
	// if (rowNumSelected < (table.getItemCount() - 1)) {
	// TableItem lastTableItem = table.getItem(rowNumSelected);
	// rowNumSelected++;
	// TableItem tableItem = table.getItem(rowNumSelected);
	// table.deselectAll();
	// lastTableItem.setFont(defaultFont);
	// tableItem.setFont(boldFont);
	// selectRowColumn();
	// }
	// try {
	// Util.showView(viewCallerID);
	// } catch (PartInitException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	// public static void selectNextFlowable() {
	// // System.out.println("RowNumSelected = " + rowNumSelected);
	// // System.out.println("table.getSelectionIndex() = " + table.getSelectionIndex());
	// if (rowNumSelected < (table.getItemCount() - 1)) {
	// TableItem lastTableItem = table.getItem(rowNumSelected);
	// int dataRowNumber = getDataRowNumberFromTableRowNumber(rowNumSelected);
	// int nextUnmatchedFlowableNum = -1;
	// for (Integer i : uniqueFlowableRowNumbers) {
	// if (i > dataRowNumber) {
	// if (!matchedFlowableRowNumbers.contains(i)) {
	// nextUnmatchedFlowableNum = i;
	// break;
	// }
	// }
	// }
	// for (int i = rowNumSelected; i < table.getItems().length; i++) {
	// TableItem tableItem = table.getItem(i);
	// String nextRowNumString = tableItem.getText(0);
	// int nextRowNum = Integer.parseInt(nextRowNumString) - 1;
	// if (nextRowNum == nextUnmatchedFlowableNum) {
	// lastTableItem = table.getItem(rowNumSelected);
	// lastTableItem.setFont(defaultFont);
	// rowNumSelected = table.indexOf(tableItem);
	// tableItem.setFont(boldFont);
	// table.setTopIndex(rowNumSelected);
	// break;
	// }
	// }
	// selectRowColumn();
	// }
	// }

	@SuppressWarnings("unchecked")
	private static List<Issue> getIssuesByColumn(int columnNumber) {
		List<Issue> results = new ArrayList<Issue>();
		for (Issue issue : issueList) {
			if (issue.getColNumber() == columnNumber) {
				results.add(issue);
			}
		}
		Collections.sort(results);
		// System.out.println("results = "+results);
		return results;
	}

	private static class FilterByIssuesListener implements Listener {
		@Override
		public void handleEvent(Event event) {
			if (colNumSelected == 0) {
				return;
			}
			SortedSet<Integer> issueSet = new TreeSet<Integer>();
			for (Issue issue : issueList) {
				if (issue.getColNumber() == colNumSelected) {
					if (!issue.getStatus().equals(Status.RESOLVED)) {
						issueSet.add(issue.getRowNumber());
					}
				}
			}
			TableColumn tableColumn = table.getColumn(colNumSelected);
			tableColumn.setToolTipText("Only showing " + issueSet.size() + " issues");
			setFilterRowNumbersWCopy(issueSet);
		}
	}

	private static class HeaderMenuColumnAssignmentListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			// SEVERAL POSSIBILITIES
			// a) DE-ASSIGN (RE-ACTIVATE THE MENU ITEM IF NEED BE)
			// b) ASSIGN TO UNASSIGNED (DE-ACTIVATE THE MENU ITEM IF NEED BE)
			// c) RE-ASSIGN (RE-ACTIVATE ONE MENU ITEM, AND MAYBE DE-ACTIVATE
			// ANOTHER)
			// d) NO CHANGE

			// System.out.println("HeaderMenuColumnAssignmentListener event.widget  = " + event.widget);
			if (!(event.widget instanceof MenuItem)) {
				// System.out.println("What's going on here?");
				return;
			}
			// int dataColNumSelected = colNumSelected - 1;
			if (colNumSelected == 0) {
				// MAY HANDLE THIS AT SOME POINT
				// System.out.println("Clicked on col. zero.  How did this fire?");
				return;
			}
			MenuItem menuItem = (MenuItem) event.widget;
			String menuItemName = menuItem.getText();
			// DE-ASSIGNING A COLUMN
			if (menuItemName.equals(deassignText)) {
				deAssignColumn();
			} else {
				// ASSIGNING A COLUMN
				String menuItemClass = menuItem.getParent().getParentItem().getText(); // WOULD FAIL IF MENU DOESN'T
																						// HAVE PARENT
				LCADataPropertyProvider lcaDataPropertyProvider = null;
				if (menuItemClass.equals(Flowable.label)) {
					lcaDataPropertyProvider = Flowable.getDataPropertyMap().get(menuItemName);
				} else if (menuItemClass.equals(FlowContext.label)) {
					lcaDataPropertyProvider = FlowContext.getDataPropertyMap().get(menuItemName);
				} else if (menuItemClass.equals(FlowUnit.label)) {
					lcaDataPropertyProvider = FlowUnit.getDataPropertyMap().get(menuItemName);
				} else if (menuItemClass.equals(Flow.label)) {
					lcaDataPropertyProvider = Flow.getDataPropertyMap().get(menuItemName);
				}

				if (lcaDataPropertyProvider == null) {
					// System.out.println("Hmm, didn't find anything matching!");
				}

				TableProvider tableProvider = TableKeeper.getTableProvider(tableProviderKey);
				LCADataPropertyProvider oldLCADataPropertyProvider = tableProvider
						.getLCADataPropertyProvider(colNumSelected);
				if (oldLCADataPropertyProvider != null) {
					// System.out.println("No longer allowed to re-assign a column.  Must de-assign first!");
				}
				tableProvider.setLCADataPropertyProvider(colNumSelected, lcaDataPropertyProvider);
				TableColumn tableColumn = table.getColumn(colNumSelected);
				// tableColumn.setText(menuItemClass + ":" +
				// System.getProperty("line.separator") + menuItemName); //
				// <-- IF THE HEADER WERE TALLER
				tableColumn.setText(menuItemClass + ": " + menuItemName);
				tableColumn.setToolTipText("");
				if (lcaDataPropertyProvider.isLeftJustified()) {
					tableColumn.setAlignment(SWT.LEFT);
				} else {
					tableColumn.setAlignment(SWT.RIGHT);
				}
				LCADataPropertyProvider.storeOneCurrentColumnAssignment(colNumSelected);
			}
		}
	}

	// private class RowIndexColumnLabelProvider extends ColumnLabelProvider {
	private static class RowIndexColumnLabelProvider extends ColumnLabelProvider {

		public RowIndexColumnLabelProvider() {
		}

		@Override
		public String getToolTipText(Object element) {
			DataRow dataRow = null;
			try {
				dataRow = (DataRow) element;
			} catch (Exception e) {
				e.printStackTrace();
				// System.out.println("element= " + element);
			}
			String t = "";
			try {
				t = dataRow.getRowToolTip();

			} catch (Exception e) {
				// System.out.println("dataRow=" + dataRow);
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
				// System.out.println("element= " + element);
			}
			String s = "";
			try {
				// s = dataRow.getRowNumber() + 1 + "";
				int rowNumPlus1 = dataRow.getRowNumber() + 1;
				s = rowNumPlus1 + "";
			} catch (Exception e) {
				// System.out.println("dataRow=" + dataRow);
				e.printStackTrace();
			}
			return s;
		}
	}

	private static class MyColumnLabelProvider extends ColumnLabelProvider {
		private int dataColumnNumber;

		public MyColumnLabelProvider(int colNum) {
			this.dataColumnNumber = colNum - 1;
		}

		@Override
		public String getText(Object element) {
			DataRow dataRow = null;
			try {
				dataRow = (DataRow) element;
			} catch (Exception e) {
				e.printStackTrace();
				// System.out.println("element= " + element);
			}
			String s = "";
			try {
				int size = dataRow.getColumnValues().size();
				if (dataColumnNumber < size) {
					s = dataRow.getColumnValues().get(dataColumnNumber);
				}
			} catch (Exception e) {
				// System.out.println("dataRow=" + dataRow);
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
	 * @return a TableViewerColumn
	 */
	private static TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {

		final TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE, colNumber);
		final TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText(title);
		tableColumn.setWidth(bound);
		tableColumn.setResizable(true);
		tableColumn.setMoveable(false);
		tableColumn.addSelectionListener(colSelectionListener);
		// if (colNumber > 0) {
		// tableColumn.addListener(SWT.MouseDoubleClick, columnMouseListener3);
		// }

		if (colNumber > 0) {
			tableColumn.setToolTipText(csvColumnDefaultTooltip);
		}

		return tableViewerColumn;
	}

	private static SelectionListener colSelectionListener = new SelectionListener() {

		private void doit(SelectionEvent e) {
			// System.out.println("SelectionListener event e= " + e);
			if (e.getSource() instanceof TableColumn) {
				TableColumn col = (TableColumn) e.getSource();
				colNumSelected = table.indexOf(col);
				if (colNumSelected > 0) {
					// FIRST CHECK TO SEE IF THIS COLUMN HAS BEEN ASSIGNED OR NOT
					if (table.getColumn(colNumSelected).getText().equals("- " + colNumSelected + " -")) {
						// initializeHeaderMenu();
						setHeaderMenu(1);
					} else {
						setHeaderMenu(2);
					}
					headerMenu.setVisible(true);
				}
			}
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			doit(e);
		};

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			doit(e);
		}

	};

	private static MenuItem getHeaderMenuItemFromName(String headerName) {
		for (MenuItem item : headerMenu.getItems()) {
			if (item.getMenu() != null) {
				Menu childMenu = item.getMenu();
				for (MenuItem childItem : childMenu.getItems()) {
					if (childItem.getText().equals(headerName)) {
						return childItem;
					}
				}
			}
		}
		return null;
	}

	private static void setPopup(List<Issue> issuesOfThisCell) {
		if (issuesOfThisCell.size() == 0) {
			return; // THIS SHOULDN'T HAPPEN
		}
		String popupText = "";

		if (issuesOfThisCell.size() == 1) {
			if (issuesOfThisCell.get(0).getStatus().equals(Status.RESOLVED)) {
				popupText = "- RESOLVED: " + issuesOfThisCell.get(0).getQaCheck().getDescription();

			} else {
				popupText = "- " + issuesOfThisCell.get(0).getQaCheck().getDescription();
			}
			popupText += System.lineSeparator() + "       - " + issuesOfThisCell.get(0).getQaCheck().getExplanation();
			popup.setText(popupText);
			return;
		}
		int count = 1;
		for (Issue issue : issuesOfThisCell) {
			if (!issue.getStatus().equals(Status.RESOLVED)) {
				popupText = count + " - " + issue.getQaCheck().getDescription();
				popupText += System.lineSeparator() + "       - " + issue.getQaCheck().getExplanation()
						+ System.lineSeparator();
				count++;
			}
		}
		for (Issue issue : issuesOfThisCell) {
			if (issue.getStatus().equals(Status.RESOLVED)) {
				popupText = count + " - RESOLVED: " + issue.getQaCheck().getDescription();
				popupText += System.lineSeparator() + "       - " + issue.getQaCheck().getExplanation()
						+ System.lineSeparator();
				count++;
			}
		}
		popup.setText(popupText);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		tableViewer.getControl().setFocus();
	}

	public static void reset() {
		table.removeAll();
		clearItemCounts();
		flowableColumns.clear();
		propertyColumns.clear();
		contextColumns.clear();
		flowColumns.clear();

		tableViewer.setInput(null);
		rowsToIgnore.clear();
		initializeRowMenu();
		removeColumns();
		preCommit = true;
	}

	public static void initialize() {
		initializeTable();
		initializeRowMenu();
		initializeFixCellMenu(true);
		// THIS IS SPECIFIC TO FlowsWorkflow
		// TODO: MAKE THIS VARY WITH THE WORK FLOW
		// initializeHeaderMenu();
		// CONSIDER: columnActionsMenu;
		// CONSIDER: rowMenu;
		// CONSIDER: fixCellMenu;
		rowsToIgnore.clear();
		rowNumSelected = -1;
		colNumSelected = -1;
	}

	private static void initializeOtherViews() {

		Util.findView(MatchFlowables.ID);
		MatchFlowables.initialize();

		// Util.findView(MatchContexts.ID);
		// MatchContexts.initialize();

		Util.findView(MatchProperties.ID);
		// MatchProperties.initialize();
	}

	private static void resetFields() {
		lcaDataPropertyProviders.clear();
		// System.out.println("Workflow telling CSVTableView to add header menu stuff 1");
		addFields(Flowable.getDataPropertyMap());
		// System.out.println("Workflow telling CSVTableView to add header menu stuff 2");
		addFields(FlowContext.getDataPropertyMap());
		// System.out.println("Workflow telling CSVTableView to add header menu stuff 3");
		addFields(FlowUnit.getDataPropertyMap());
		// System.out.println("lcaDataPropertyProviders.size() = " + lcaDataPropertyProviders.size());
		addFields(Flow.getDataPropertyMap());
	}

	private static void removeColumns() {
		table.setRedraw(false);
		while (table.getColumnCount() > 0) {
			table.getColumns()[0].dispose();
		}
		table.setRedraw(true);
	}

	private static void addLCADataPropertiesToHeaderMenu() {
		String curMenuName = "";
		Menu curMenu = null;
		for (LCADataPropertyProvider lcaDataPropertyProvider : lcaDataPropertyProviders) {
			if (!lcaDataPropertyProvider.isIncludeInList()) {
				continue;
			}
			String newMenuName = lcaDataPropertyProvider.getPropertyClass();
			// System.out.println("newMenuName = " + newMenuName);
			MenuItem menuParent = null;
			if (!newMenuName.equals(curMenuName)) {
				curMenuName = newMenuName;
				menuParent = new MenuItem(headerMenu, SWT.CASCADE);
				menuParent.setText(newMenuName);
				curMenu = new Menu(headerMenu);
				menuParent.setMenu(curMenu);
			}
			MenuItem menuItem = new MenuItem(curMenu, SWT.NORMAL);
			clearMenuItemListeners(menuItem);
			if (lcaDataPropertyProvider.isUnique()) {
				TableProvider tableProvider = TableKeeper.getTableProvider(tableProviderKey);
				for (LCADataPropertyProvider checkLCADataPropertyProvider : tableProvider.getLcaDataProperties()) {
					if (lcaDataPropertyProvider.sameAs(checkLCADataPropertyProvider)) {
						menuItem.setEnabled(false);
						continue;
					}
				}
			}
			menuItem.addListener(SWT.Selection, new HeaderMenuColumnAssignmentListener());
			menuItem.setText(lcaDataPropertyProvider.getPropertyName());
			// System.out.println("menuItem.getText() = " + menuItem.getText());

		}
	}

	private static void clearMenuItemListeners(MenuItem menuItem) {
		for (Listener listener : menuItem.getListeners(SWT.Selection)) {
			menuItem.removeListener(SWT.Selection, listener);
		}
	}

	private static boolean fixOneIssue(Issue issue) {
		int colNumber = issue.getColNumber();
		// int colNumber = dataColNumber + 1;
		TableItem tableItem = table.getItem(issue.getRowNumber());
		String startingText = tableItem.getText(colNumber);
		// System.out.println("trying to fix: " + startingText);
		FormatCheck qaCheck = issue.getQaCheck();
		if (qaCheck.getReplacement() != null) {
			Matcher matcher = qaCheck.getPattern().matcher(startingText);
			String fixedText = matcher.replaceFirst(qaCheck.getReplacement());
			if (!fixedText.equals(startingText)) {
				// System.out.println("The value is now ->" + fixedText + "<-");
				tableItem.setText(colNumber, fixedText);
				// System.out.println("TableItem fixed, but not (source) TableProvider data");
				DataRow dataRow = TableKeeper.getTableProvider(tableProviderKey).getData().get(issue.getRowNumber());
				// System.out.println("Underlying value is now: " +
				// dataRow.get(issue.getColNumber() - 1));
				dataRow.set(issue.getColNumber() - 1, fixedText);
				// System.out.println("Underlying value is now: " +
				// dataRow.get(issue.getColNumber() - 1));
				issue.setStatus(Status.RESOLVED);
				colorCell(issue);
				return true;
			}
		}
		return false;
	}

	private static void clearIssues(int colNumber) {
		if (issueList == null) {
			return;
		}
		if (issueList.size() < 1) {
			return;
		}
		for (Issue issue : getIssuesByColumn(colNumber)) {
			issueList.remove(issueList.indexOf(issue));
			colorCell(issue.getRowNumber(), colNumber, SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		}
	}

	public static void clearIssues() {
		if (issueList == null) {
			return;
		}
		if (issueList.size() < 1) {
			return;
		}
		issueList = new ArrayList<Issue>();
		for (TableItem tableItem : table.getItems()) {
			tableItem.setBackground(null);
		}
	}

	private static int checkOneColumn(int colIndex) {
		if (colIndex == 0) {
			return 0;
		}
		int issueCount = 0;
		TableProvider tableProvider = TableKeeper.getTableProvider(tableProviderKey);
		LCADataPropertyProvider lcaDataPropertyProvider = tableProvider.getLCADataPropertyProvider(colIndex);

		// CSVColumnInfo csvColumnInfo =
		// tableProvider.getAssignedCSVColumnInfo()[colIndex];
		// CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[colIndex];
		if (lcaDataPropertyProvider == null) {
			table.getColumn(colIndex).setToolTipText(csvColumnDefaultTooltip);
			return 0;
		}
		// clearIssueColors(colIndex);
		clearIssues(colIndex);
		// clearIssues
		// csvColumnInfo.clearIssues();
		List<String> columnValues = getColumnValues(colIndex);

		for (FormatCheck qaCheck : lcaDataPropertyProvider.getCheckLists()) {
			for (int i = 0; i < columnValues.size(); i++) {
				if (rowsToIgnore.contains(i)) {
					continue;
				}
				String val = columnValues.get(i);
				Matcher matcher = qaCheck.getPattern().matcher(val);

				if (qaCheck.isPatternMustMatch()) {
					if (!matcher.find()) {
						Issue issue = new Issue(qaCheck, i, colIndex, 0, Status.WARNING);
						Logger.getLogger("run").warn(qaCheck.getDescription());
						Logger.getLogger("run").warn("  ->Row " + issue.getRowNumber());
						Logger.getLogger("run").warn("  ->Column " + colIndex);
						Logger.getLogger("run").warn("  ->Required pattern not found");
						assignIssue(issue);
						issueCount++;
						// csvColumnInfo.addIssue(issue);
					} else if (lcaDataPropertyProvider.getPropertyClass().equals(Flowable.label)
							&& lcaDataPropertyProvider.getPropertyName().equals(Flowable.casString) && val != null
							&& val.length() != 0) {
						// TODO - Java question, does each boolean in a list joined with && get evaluated, or does it
						// quit when one is false? See above and below.
						if (Flowable.correctCASCheckSum(val) == false) {
							Issue issue = new Issue(Flowable.createBadCheckSumQACheck(), i, colIndex, 0, Status.WARNING);
							Logger.getLogger("run").warn(qaCheck.getDescription());
							Logger.getLogger("run").warn("  ->Row " + issue.getRowNumber());
							Logger.getLogger("run").warn("  ->Column " + colIndex);
							Logger.getLogger("run").warn("  ->Checksum failed");
							assignIssue(issue);
							issueCount++;
						}
					}
				} else {
					while (matcher.find()) {
						Issue issue = new Issue(qaCheck, i, colIndex, matcher.end(), Status.WARNING);
						Logger.getLogger("run").warn(qaCheck.getDescription());
						Logger.getLogger("run").warn("  ->Row" + issue.getRowNumber());
						Logger.getLogger("run").warn("  ->Column" + colIndex);
						Logger.getLogger("run").warn("  ->Character position" + issue.getCharacterPosition());
						assignIssue(issue);
						issueCount++;
						// csvColumnInfo.addIssue(issue);
					}
				}
			}
		}
		table.getColumn(colIndex).setToolTipText(issueCount + " issues");
		return issueCount;
	}

	public static int checkCols() {
		int totalIssueCount = 0;
		int colCount = TableKeeper.getTableProvider(tableProviderKey).getColumnCount();
		for (int colIndex = 1; colIndex <= colCount; colIndex++) {
			totalIssueCount += checkOneColumn(colIndex);
			colorRowNumberColumn(colIndex);
		}
		return totalIssueCount;
	}

	public static void colorCell(int tableRowNumber, int colNumber, Color color) {
		if (tableRowNumber > -1 && tableRowNumber < table.getItemCount()) {
			TableItem tableItem = table.getItem(tableRowNumber);
			tableItem.setBackground(colNumber, color);
		}
	}

	private static void colorCell(Issue issue) {
		SortedSet<Integer> filterRows = getFilterRowNumbers();
		int colIndex = issue.getColNumber();
		if ((filterRows == null) || (filterRows.size() == 0)) {
			TableItem tableItem = table.getItem(issue.getRowNumber());
			int rowNumberByLabel = Integer.parseInt(tableItem.getText(0));
			if (rowNumberByLabel - 1 == issue.getRowNumber()) {
				tableItem.setBackground(colIndex, issue.getStatus().getColor());
			}
		} else {
			if (filterRows.contains((Integer) issue.getRowNumber())) {
				int row = 0;
				Iterator<Integer> iterator = filterRows.iterator();
				while (iterator.hasNext()) {
					int origRowNum = iterator.next();
					if (origRowNum == issue.getRowNumber()) {
						colorCell(row, colIndex, issue.getStatus().getColor());
						continue;
					}
					row++;
				}
			}
		}
	}

	private static List<String> getColumnValues(int colIndex) {
		List<String> results = new ArrayList<String>();
		for (TableItem item : table.getItems()) {
			results.add(item.getText(colIndex));
		}
		return results;
	}

	private static void assignIssue(Issue issue) {
		if (issue.getRowNumber() >= table.getItemCount()) {
			return;
		}
		issueList.add(issue);

		colorCell(issue);
		return;
	}

	private static LCADataPropertyProvider getLCADataPropertyProviderByMenuString(String classLabel, String propertyName) {
		for (LCADataPropertyProvider lcaDataPropertyProvider : lcaDataPropertyProviders) {
			if (!classLabel.equals(lcaDataPropertyProvider.getPropertyClass())) {
				continue;
			}
			if (propertyName.equals(lcaDataPropertyProvider.getPropertyName())) {
				return lcaDataPropertyProvider;
			}
		}
		return null;
	}

	public static int countAssignedColumns() {
		int colsAssigned = 0;
		for (LCADataPropertyProvider lcaDataPropertyProvider : lcaDataPropertyProviders) {
			if (lcaDataPropertyProvider != null) {
				colsAssigned++;
			}
		}
		return colsAssigned;
	}

	public static Table getTable() {
		return table;
	}

	public static String getTableProviderKey() {
		return tableProviderKey;
	}

	public void setTableProviderKey(String tableProviderKey) {
		CSVTableView.tableProviderKey = tableProviderKey;
	}

	public static void addFields(Map<String, LCADataPropertyProvider> dataPropertyMap) {
		for (LCADataPropertyProvider lcaDataPropertyProvider : dataPropertyMap.values()) {
			// System.out.println("Adding lcaDataPropertyProvider.getPropertyName(): "
			// + lcaDataPropertyProvider.getPropertyName());
			lcaDataPropertyProviders.add(lcaDataPropertyProvider);
			// System.out.println("lcaDataPropertyProviders.size() = " + lcaDataPropertyProviders.size());
		}
	}

	public static void colorOneFlowableRow(int dataRowNumber) {
		if (!uniqueFlowableRowNumbers.contains(dataRowNumber)) {
			return;
		}
		int tableRowNumberToColor = getTableRowNumberFromDataRowNumber(dataRowNumber);
		if (tableRowNumberToColor < 0) {
			return;
		}

		// WHAT COLOR ARE WE GOING TO COLOR IT?
		Color color;

		List<DataRow> data = TableKeeper.getTableProvider(tableProviderKey).getData();
		DataRow dataRow = data.get(dataRowNumber);
		Flowable flowable = dataRow.getFlowable();
		if (flowable == null) {
			return;
		}
		Resource resource = flowable.getTdbResource();
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		boolean adHoc = ActiveTDB.getModel(null).contains(resource, LCAHT.hasQCStatus, LCAHT.QCStatusAdHocMaster);
		ActiveTDB.tdbDataset.end();
		if (adHoc) {
			color = SWTResourceManager.getColor(SWT.COLOR_CYAN);
		} else {
			int assignedMatches = flowable.countAssignedComparisons();
			if (assignedMatches == 1) {
				color = SWTResourceManager.getColor(SWT.COLOR_GREEN);
			} else if (assignedMatches > 1) {
				color = orange;
			} else {
				color = SWTResourceManager.getColor(SWT.COLOR_YELLOW);
			}
		}
		TableItem tableItem = table.getItem(tableRowNumberToColor);
		for (int i : flowableColumns) {
			tableItem.setBackground(i, color);
		}
	}

	public static void colorFlowableRows() {
		for (int tableRowNumber = 0; tableRowNumber < table.getItemCount(); ++tableRowNumber) {
			int dataRowNumber = getDataRowNumberFromTableRowNumber(tableRowNumber);
			if (uniqueFlowableRowNumbers.contains(dataRowNumber))
				colorOneFlowableRow(dataRowNumber);
		}
	}

	public static void colorOneFlowContextRow(int rowToColor, boolean matched) {
		Color color;
		if (matched)
			color = SWTResourceManager.getColor(SWT.COLOR_GREEN);
		else
			color = SWTResourceManager.getColor(SWT.COLOR_YELLOW);
		for (int j : contextColumns) {
			colorCell(rowToColor, j, color);
		}
	}

	public static void colorFlowContextRows() {
		Set<Integer> filterRowNumbers = getFilterRowNumbers();

		if (filterRowNumbers.size() > 0) {
			int visibleRowNum = 0;
			for (int i : filterRowNumbers) {
				if (uniqueFlowContextRowNumbers.contains(i)) {

					Color color;
					if (matchedFlowContextRowNumbers.contains(i)) {
						color = SWTResourceManager.getColor(SWT.COLOR_GREEN);
					} else {
						color = SWTResourceManager.getColor(SWT.COLOR_YELLOW);
					}
					for (int j : contextColumns) {
						colorCell(visibleRowNum, j, color);
					}
				}
				visibleRowNum++;
			}
		} else {
			for (int i = 0; i < table.getItemCount(); ++i) {
				if (!uniqueFlowContextRowNumbers.contains(i))
					continue;
				Color color;
				if (matchedFlowContextRowNumbers.contains(i)) {
					color = SWTResourceManager.getColor(SWT.COLOR_GREEN);
				} else {
					color = SWTResourceManager.getColor(SWT.COLOR_YELLOW);
				}
				for (int j : contextColumns) {
					colorCell(i, j, color);
				}
			}
		}
	}

	public static void colorOneFlowPropertyRow(int rowToColor, boolean matched) {
		Color color;
		if (matched)
			color = SWTResourceManager.getColor(SWT.COLOR_GREEN);
		else
			color = SWTResourceManager.getColor(SWT.COLOR_YELLOW);
		for (int j : propertyColumns) {
			colorCell(rowToColor, j, color);
		}
	}

	/**
	 * This method returns the unique dataset name associated with the current Dataset Table
	 * @return a String containing the name of the current dataset.
	 */
	public static String getCurrentDatasetName() {
		TableProvider tableProvider = TableKeeper.getTableProvider(tableProviderKey);
		return tableProvider.getDataSourceProvider().getDataSourceName();
	}

	/**
	 * This method returns the RDF Resource associated with the current Dataset Table
	 * @return an RDF Resource of RDF Class ECO:DataSource containing the current dataset
	 */
	public static Resource getCurrentDatasetTDBResource() {
		TableProvider tableProvider = TableKeeper.getTableProvider(tableProviderKey);
		return tableProvider.getDataSourceProvider().getTdbResource();
	}

	public static void colorFlowPropertyRows() {
		Set<Integer> filterRowNumbers = getFilterRowNumbers();

		if (filterRowNumbers.size() > 0) {
			int visibleRowNum = 0;
			for (int i : filterRowNumbers) {
				if (uniqueFlowPropertyRowNumbers.contains(i)) {

					Color color;
					// Resource resource =
					// TableKeeper.getTableProvider(tableProviderKey).getData().get(i).getFlowable()
					// .getTdbResource();
					// ActiveTDB.tdbDataset.begin(ReadWrite.READ);
					// boolean adHoc =
					// ActiveTDB.tdbDataset.getDefaultModel().contains(resource,
					// LCAHT.hasQCStatus,
					// LCAHT.QCStatusAdHocMaster);
					// ActiveTDB.tdbDataset.end();
					// if (adHoc) {
					// color = SWTResourceManager.getColor(SWT.COLOR_CYAN);
					// } else
					if (matchedFlowPropertyRowNumbers.contains(i)) {
						color = SWTResourceManager.getColor(SWT.COLOR_GREEN);
					} else {
						color = SWTResourceManager.getColor(SWT.COLOR_YELLOW);
					}
					for (int j : propertyColumns) {
						colorCell(visibleRowNum, j, color);
					}
				}
				visibleRowNum++;
			}
		} else {
			for (int i = 0; i < table.getItemCount(); ++i) {
				if (!uniqueFlowPropertyRowNumbers.contains(i))
					continue;
				Color color;
				// Resource resource =
				// TableKeeper.getTableProvider(tableProviderKey).getData().get(i).getFlowable()
				// .getTdbResource();
				// ActiveTDB.tdbDataset.begin(ReadWrite.READ);
				// boolean adHoc =
				// ActiveTDB.tdbDataset.getDefaultModel().contains(resource,
				// LCAHT.hasQCStatus,
				// LCAHT.QCStatusAdHocMaster);
				// ActiveTDB.tdbDataset.end();
				// if (adHoc) {
				// color = SWTResourceManager.getColor(SWT.COLOR_CYAN);
				// } else
				if (matchedFlowPropertyRowNumbers.contains(i)) {
					color = SWTResourceManager.getColor(SWT.COLOR_GREEN);
				} else {
					color = SWTResourceManager.getColor(SWT.COLOR_YELLOW);
				}
				for (int j : propertyColumns) {
					colorCell(i, j, color);
				}
			}
		}
	}

	public static void colorOneFlowRow(int dataRowNumber, boolean matched) {
		colorOneFlowRow(dataRowNumber, matched, false);
	}

	public static void colorOneFlowRow(int dataRowNumber, boolean matched, boolean updateHeader) {
		Color color;
		int tableRowNumber = getTableRowNumberFromDataRowNumber(dataRowNumber);
		if (matched)
			color = SWTResourceManager.getColor(SWT.COLOR_GREEN);
		else
			color = SWTResourceManager.getColor(SWT.COLOR_YELLOW);
		for (int j : flowColumns) {
			colorCell(tableRowNumber, j, color);
		}
		colorCell(tableRowNumber, 0, color);

		if (updateHeader) {
			updateFlowHeaderCount(matchedFlowRowNumbers.size(), uniqueFlowRowNumbers.size());
		}
	}

	public static void updateFlowHeaderCount(int matched, int total) {
		TableColumn tc = table.getColumn(0);
		tc.setText(matched + "/" + total);
		double digits = Math.floor(Math.log(matched) / Math.log(10));
		digits += Math.floor(Math.log(total) / Math.log(10));
		digits += 1;
		int digitWidth = (int) (digits * 7);
		// table.getColumn(0).pack();
		table.getColumn(0).setWidth(digitWidth);
		String matchedMessage = matched + " of " + total + " flows matched.";
		table.getColumn(0).setToolTipText(matchedMessage);
		Logger.getLogger("run").info(matchedMessage);

	}

	public static void colorFlowRows() {
		Set<Integer> filterRowNumbers = getFilterRowNumbers();
		if (filterRowNumbers.size() > 0) {
			for (int dataRowNumber : filterRowNumbers) {
				if (uniqueFlowRowNumbers.contains(dataRowNumber)) {
					colorOneFlowRow(dataRowNumber, matchedFlowRowNumbers.contains(dataRowNumber));
				}
			}
		} else {
			for (int i = 0; i < table.getItemCount(); ++i) {
				if (uniqueFlowRowNumbers.contains(i))
					colorOneFlowRow(i, matchedFlowRowNumbers.contains(i));
			}
		}
		resizeFlowColumns();
	}

	public static void resizeFlowColumns() {
		TableColumn tc = table.getColumn(0);
		tc.setText(matchedFlowRowNumbers.size() + "/" + uniqueFlowRowNumbers.size());
		double digits = Math.floor(Math.log(matchedFlowRowNumbers.size()) * Math.log(10));
		digits += Math.floor(Math.log(uniqueFlowRowNumbers.size()) * Math.log(10));
		digits += 1;
		int digitWidth = (int) (digits * 5);
		// table.getColumn(0).pack();
		table.getColumn(0).setWidth(digitWidth);

	}

	public static void clearItemCounts() {
		uniqueFlowableRowNumbers.clear();
		uniqueFlowContextRowNumbers.clear();
		uniqueFlowPropertyRowNumbers.clear();
		uniqueFlowRowNumbers.clear();

		matchedFlowableRowNumbers.clear();
		matchedFlowContextRowNumbers.clear();
		matchedFlowPropertyRowNumbers.clear();
		matchedFlowRowNumbers.clear();
	}

	// public static Set<Integer> getUniqueFlowableRowNumbers() {
	// return uniqueFlowableRowNumbers;
	// }

	public static void setUniqueFlowableRowNumbers(SortedSet<Integer> uniqueFlowableRowNumbers) {
		CSVTableView.uniqueFlowableRowNumbers = uniqueFlowableRowNumbers;
	}

	// public static Set<Integer> getUniqueFlowContextRowNumbers() {
	// return uniqueFlowContextRowNumbers;
	// }

	public static void setUniqueFlowContextRowNumbers(SortedSet<Integer> uniqueFlowContextRowNumbers) {
		CSVTableView.uniqueFlowContextRowNumbers = uniqueFlowContextRowNumbers;
	}

	// public static Set<Integer> getUniqueFlowPropertyRowNumbers() {
	// return uniqueFlowPropertyRowNumbers;
	// }

	public static void setUniqueFlowPropertyRowNumbers(SortedSet<Integer> uniqueFlowPropertyRowNumbers) {
		CSVTableView.uniqueFlowPropertyRowNumbers = uniqueFlowPropertyRowNumbers;
	}

	// public static Set<Integer> getUniqueFlowRowNumbers() {
	// return uniqueFlowRowNumbers;
	// }

	public static void setUniqueFlowRowNumbers(SortedSet<Integer> uniqueFlowRowNumbers) {
		CSVTableView.uniqueFlowRowNumbers = uniqueFlowRowNumbers;
	}

	// --------

	// public static Set<Integer> getMatchedFlowableRowNumbers() {
	// return matchedFlowableRowNumbers;
	// }

	public static void setMatchedFlowableRowNumbers(SortedSet<Integer> matchedFlowableRowNumbers) {
		CSVTableView.matchedFlowableRowNumbers = matchedFlowableRowNumbers;
	}

	// public static Set<Integer> getMatchedFlowContextRowNumbers() {
	// return matchedFlowContextRowNumbers;
	// }

	public static void setMatchedFlowContextRowNumbers(SortedSet<Integer> matchedFlowContextRowNumbers) {
		CSVTableView.matchedFlowContextRowNumbers = matchedFlowContextRowNumbers;
	}

	// public static Set<Integer> getMatchedFlowPropertyRowNumbers() {
	// return matchedFlowPropertyRowNumbers;
	// }

	public static void setMatchedFlowPropertyRowNumbers(SortedSet<Integer> matchedFlowPropertyRowNumbers) {
		CSVTableView.matchedFlowPropertyRowNumbers = matchedFlowPropertyRowNumbers;
	}

	// public static Set<Integer> getMatchedFlowPropertyRowNumbers() {
	// return matchedFlowPropertyRowNumbers;
	// }

	public static void setMatchedFlowRowNumbers(SortedSet<Integer> matchedFlowRowNumbers) {
		CSVTableView.matchedFlowRowNumbers = matchedFlowRowNumbers;
	}

	// public static void setSelection(int i) {
	// if (i < 0 || i >= table.getItemCount()) {
	// return;
	// }
	// String rowNumString = table.getItem(i).getText(0);
	// int dataRowNumber = Integer.parseInt(rowNumString) - 1;
	// while (rowsToIgnore.contains(dataRowNumber)) {
	// i++;
	// rowNumString = table.getItem(i).getText(0);
	// dataRowNumber = Integer.parseInt(rowNumString) - 1;
	// }
	// if (i >= table.getItemCount()) {
	// table.deselectAll();
	// return;
	// }
	// rowNumSelected = i;
	// table.setSelection(i);
	// matchRowContents();
	// }
	//
	// public static void setRowNumSelected(int i) {
	// int oldRowNumSelected = rowNumSelected;
	// if (i < 0 || i >= table.getItemCount()) {
	// rowNumSelected = 0;
	// } else {
	// rowNumSelected = i;
	// }
	// TableItem tableItem = table.getItem(rowNumSelected);
	// if (defaultFont == null) {
	// createFonts(tableItem);
	// }
	// if (oldRowNumSelected >= 0 & oldRowNumSelected < table.getItemCount()) {
	// table.getItem(oldRowNumSelected).setFont(defaultFont);
	// }
	// tableItem.setFont(boldFont);
	// }

	public static List<String> getMasterFlowUUIDs() {
		if (masterFlowUUIDs == null) {
			/* Collect master Flow UUIDs (if any) */
			masterFlowUUIDs = new ArrayList<String>();
			StringBuilder b = new StringBuilder();
			b.append(Prefixes.getPrefixesForQuery());
			b.append("select distinct ?uuid \n");
			b.append("where {  \n");
			b.append("  ?f fedlca:hasOpenLCAUUID ?uuid .  \n");
			b.append("  ?f a fedlca:Flow . \n");
			b.append("  ?f eco:hasDataSource ?mds . \n");
			b.append("  ?mds a lcaht:MasterDataset . \n");
			b.append("} \n");
			String query = b.toString();

			HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
			harmonyQuery2Impl.setQuery(query);

			ResultSet resultSet = harmonyQuery2Impl.getResultSet();
			while (resultSet.hasNext()) {
				QuerySolution querySolution = resultSet.next();
				RDFNode rdfNode = querySolution.get("uuid");
				masterFlowUUIDs.add(rdfNode.asLiteral().getString());
			}
		}
		return masterFlowUUIDs;
	}

	public static int getRowNumSelected() {
		return rowNumSelected;
	}

	/**
	 * This method provides the tableProvider (data) index number for a given row in the User Data table based
	 * on the integer in the 0th column of the specified displayed row in the User Data table.
	 * It returns -1 if<br/><ul>
	 * <li>the parameter is out of range of the current User Data table view</li>
	 * <li>the 0th column of the specified row does not contain a parsable integer</li>
	 * <li>the resulting integer would be outside the range of the current tableProvider
	 * @param int <strong> rowNumber</strong> the index of the User Data table as currently displayed
	 * @return The int index in the tableProvider represented by the specified row in the User Data table
	 */
	public static int getDataRowNumberFromTableRowNumber(int tableRowNumber) {
		if (tableRowNumber < 0 || tableRowNumber >= table.getItemCount()) {
			return -1;
		}
		String rowNumString = table.getItem(tableRowNumber).getText(0);
		int dataRowIndex;
		try {
			dataRowIndex = Integer.valueOf(rowNumString) - 1;
		} catch (NumberFormatException e) {
			return -1;
		}
		if (dataRowIndex >= 0 && dataRowIndex < TableKeeper.getTableProvider(tableProviderKey).getData().size()) {
			return dataRowIndex;
		}
		return -1;
	}

	private static int getTableRowNumberFromDataRowNumber(int dataRowNumber) {
		Set<Integer> filterRowNumbers = getFilterRowNumbers();
		if (filterRowNumbers != null) {
			if (filterRowNumbers.size() == 0) {
				return dataRowNumber;
			}
		}
		for (int tableRowNumberCandidate = 0; tableRowNumberCandidate < table.getItemCount(); tableRowNumberCandidate++) {
			int dataRowNumberCandidate = getDataRowNumberFromTableRowNumber(tableRowNumberCandidate);
			if (dataRowNumberCandidate == dataRowNumber) {
				return tableRowNumberCandidate;
			}
		}
		return -1;
	}

	public static void setPostCommit() {
		preCommit = false;
		// tableViewer.

		LCADataPropertyProvider[] lcaDataProperties = TableKeeper.getTableProvider(tableProviderKey)
				.getLcaDataProperties();
		for (int i = 0; i < lcaDataProperties.length; i++) {
			LCADataPropertyProvider lcaDataPropertyProvider = lcaDataProperties[i];
			if (lcaDataPropertyProvider != null) {
				if (lcaDataPropertyProvider.getPropertyClass().equals(Flowable.label)) {
					flowableColumns.add(i);
				} else if (lcaDataPropertyProvider.getPropertyClass().equals(FlowUnit.label)) {
					propertyColumns.add(i);
				} else if (lcaDataPropertyProvider.getPropertyClass().equals(FlowContext.label)) {
					contextColumns.add(i);
				} else if (lcaDataPropertyProvider.getPropertyClass().equals(Flow.label)) {
					flowColumns.add(i);
				}
			}
		}
	}

	public static void setColNumSelected(int col) {
		colNumSelected = col;
	}
}
