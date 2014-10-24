package gov.epa.nrmrl.std.lca.ht.flowable.mgr;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataPropertyProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FASC;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.vocabulary.RDF;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.FillLayout;

/**
 * @author Tommy E. Cathey and Tom Transue
 * 
 */
public class MatchFlowableTableView extends ViewPart {

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.flowable.MatchFlowableTableView";

	private static TableViewer tableViewer;
	private static Table table;

	private static int rowNumSelected = -1;
	private static int colNumSelected = -1;
	private static Flowable flowableToMatch;
	private static int dataTableRowNum = -1;

	private static TextCellEditor editor;

	public MatchFlowableTableView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		outerComposite = new Composite(parent, SWT.NONE);
		outerComposite.setLayout(new GridLayout(1, false));
		System.out.println("hello, from sunny MatchFlowableTableView!");
		initializeTableViewer(outerComposite);
		// initializePopup(composite);
		initialize();

		// parent.addListener(SWT.Resize, new Listener() {
		// @Override
		// public void handleEvent(Event event) {
		// table.setSize(table.getParent().getSize());
		// }
		// // THIS IS NOT PERFECT
		// // WHEN THE WINDOW IS RESIZED SMALLER, THE TABLE OVER RUNS A LITTLE
		// });

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}

	private static void initializeTableViewer(Composite composite) {

		Composite innterComposite = new Composite(outerComposite, SWT.NONE);
		innterComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData gd_composite_2 = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_composite_2.heightHint = 20;
		innterComposite.setLayoutData(gd_composite_2);
		// innterComposite.setBounds(0, 0, 64, 64);

		Button acceptAdvance = new Button(innterComposite, SWT.NONE);
		acceptAdvance.setText("Assign");
		acceptAdvance.addSelectionListener(assignSelectionListener);

		Button addToMaster = new Button(innterComposite, SWT.NONE);
		addToMaster.setText("Add to Master");
		addToMaster.setVisible(false);
		tableViewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);
		editor = new TextCellEditor(tableViewer.getTable());
		tableViewer.setContentProvider(new ArrayContentProvider());
		createColumns();
	}

	//
	private static void initializeTable() {
		table = tableViewer.getTable();
		GridData gd_table = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_table.widthHint = 3000;
		gd_table.heightHint = 1500;
		table.setLayoutData(gd_table);
		// table.addListener(SWT.KeyDown, keyListener);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addMouseListener(tableMouseListener);
	}

	public static void update(int rowNumber) {
		initialize();
		TableProvider tableProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey());
		DataRow dataRow = tableProvider.getData().get(rowNumber);
		dataTableRowNum = rowNumber;
		flowableToMatch = dataRow.getFlowable();
		if (flowableToMatch == null) {
			return;
		}

		flowableToMatch.clearSyncDataFromTDB(); // NECESSARY? GOOD? TODO: CHECK THIS
		table.clearAll();
		int rowCount = flowableToMatch.getMatchCandidates().size() + 2;
		table.setItemCount(rowCount);
		setResultRowData(0, flowableToMatch);
		table.getItem(0).setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		LinkedHashMap<Resource, String> matchCandidateResources = flowableToMatch.getMatchCandidates();
		if (matchCandidateResources == null) {
			return;
		}
		int row = 1;
		// Integer[] matchSummary = new Integer[] { 0, 0, 0, 0, 0, 0 };
		for (Object dFlowableResource : matchCandidateResources.keySet()) {
			Flowable dFlowable = new Flowable((Resource) dFlowableResource);
			setResultRowData(row, dFlowable);
			String matchStatus = matchCandidateResources.get(dFlowableResource);
			TableItem tableItem = table.getItem(row);
			for (int i = 0; i < 6; i++) { // CLEAR STRINGS
				tableItem.setText(i, "");
			}
			int num = 0;
			if (matchStatus != null) {
				num = MatchStatus.getNumberBySymbol(matchStatus);
			} else {
				matchStatus = "?";
			}
			if (num < 0) {
				num = 0;
			}

			tableItem.setText(num, matchStatus);
			// matchSummary[num]++;
			row++;
		}

		updateMatchCounts();

		// for (int i = 0; i < 6; i++) {
		// if (matchSummary[i] == null) {
		// table.getItem(0).setText(i, "0");
		// } else {
		// table.getItem(0).setText(i, matchSummary[i].toString());
		// }
		// }
	}

	// private static void updateMatchStatus(int rowNumber) {
	// Integer[] matchSummary = new Integer[7];
	// for (int i = 0; i < 7; i++) {
	// table.getItem(0).setText(i, matchSummary[i] + "");
	// }
	// }

	private static void setResultRowData(int rowNum, Flowable flowable) {
		TableItem qRow = table.getItem(rowNum);
		if (flowable.getDataSource() != null) {
			qRow.setText(6, flowable.getDataSource());
		}
		qRow.setText(7, flowable.getName());
		String casString = flowable.getCas();
		if (casString != null) {
			qRow.setText(8, flowable.getCas());
		}
		String synConcat = "";
		String[] synonyms = flowable.getSynonyms();
		if (synonyms.length > 0) {
			synConcat = synonyms[0];
		}
		for (int i = 0; i < synonyms.length; i++) {
			String synonym = synonyms[i];
			synConcat += " -or- " + synonym;
		}
		qRow.setText(9, synConcat);
	}

	private static void createColumns() {

		TableViewerColumn tableViewerColumn;
		// MatchStatus matchStatus;

		createTableViewerMatchColumn(MatchStatus.UNKNOWN);
		createTableViewerMatchColumn(MatchStatus.EQUIVALENT);
		createTableViewerMatchColumn(MatchStatus.SUBSET);
		createTableViewerMatchColumn(MatchStatus.SUPERSET);
		createTableViewerMatchColumn(MatchStatus.PROXY);
		createTableViewerMatchColumn(MatchStatus.NONEQUIVALENT);

		tableViewerColumn = createTableViewerColumn("Data Source", 200, 6);
		tableViewerColumn.getColumn().setAlignment(SWT.LEFT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(6));

		tableViewerColumn = createTableViewerColumn(Flowable.flowableNameString, 300, 7);
		tableViewerColumn.getColumn().setAlignment(SWT.LEFT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(7));

		tableViewerColumn = createTableViewerColumn(Flowable.casString, 100, 8);
		tableViewerColumn.getColumn().setAlignment(SWT.RIGHT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(8));

		tableViewerColumn = createTableViewerColumn(Flowable.flowableSynonymString, 300, 9);
		tableViewerColumn.getColumn().setAlignment(SWT.LEFT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(9));

		tableViewerColumn = createTableViewerColumn("Other", 200, 10);
		tableViewerColumn.getColumn().setAlignment(SWT.LEFT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(10));
		//
		// matchStatus = MatchStatus.UNKNOWN;
		// tableViewerColumn = createTableViewerColumn(matchStatus.getSymbol(),
		// 20, matchStatus.getValue());
		// tableViewerColumn.getColumn().setToolTipText(matchStatus.getName() +
		// " - " + matchStatus.getComment());
		// tableViewerColumn.getColumn().setAlignment(SWT.CENTER);
		// tableViewerColumn.setLabelProvider(new
		// MyColumnLabelProvider(matchStatus.getValue()));
		// tableViewerColumn.getColumn().addSelectionListener(assignSelectionListener);
		//
		// matchStatus = MatchStatus.EQUIVALENT;
		// tableViewerColumn = createTableViewerColumn(matchStatus.getSymbol(),
		// 20, matchStatus.getValue());
		// tableViewerColumn.getColumn().setToolTipText(matchStatus.getName() +
		// " - " + matchStatus.getComment());
		// tableViewerColumn.getColumn().setAlignment(SWT.CENTER);
		// tableViewerColumn.setLabelProvider(new
		// MyColumnLabelProvider(matchStatus.getValue()));
		// tableViewerColumn.getColumn().addSelectionListener(assignSelectionListener);

		// matchStatus = MatchStatus.SUBSET;
		// tableViewerColumn = createTableViewerColumn(matchStatus.getSymbol(),
		// 20, matchStatus.getValue());
		// tableViewerColumn.getColumn().setToolTipText(matchStatus.getName() +
		// " - " + matchStatus.getComment());
		// tableViewerColumn.getColumn().setAlignment(SWT.CENTER);
		// tableViewerColumn.setLabelProvider(new
		// MyColumnLabelProvider(matchStatus.getValue()));
		// tableViewerColumn.getColumn().addSelectionListener(assignSelectionListener);
		//
		// matchStatus = MatchStatus.SUPERSET;
		// tableViewerColumn = createTableViewerColumn(matchStatus.getSymbol(),
		// 20, matchStatus.getValue());
		// tableViewerColumn.getColumn().setToolTipText(matchStatus.getName() +
		// " - " + matchStatus.getComment());
		// tableViewerColumn.getColumn().setAlignment(SWT.CENTER);
		// tableViewerColumn.setLabelProvider(new
		// MyColumnLabelProvider(matchStatus.getValue()));
		// tableViewerColumn.getColumn().addSelectionListener(assignSelectionListener);
		//
		// matchStatus = MatchStatus.PROXY;
		// tableViewerColumn = createTableViewerColumn(matchStatus.getSymbol(),
		// 20, matchStatus.getValue());
		// tableViewerColumn.getColumn().setToolTipText(matchStatus.getName() +
		// " - " + matchStatus.getComment());
		// tableViewerColumn.getColumn().setAlignment(SWT.CENTER);
		// tableViewerColumn.setLabelProvider(new
		// MyColumnLabelProvider(matchStatus.getValue()));
		// tableViewerColumn.getColumn().addSelectionListener(assignSelectionListener);
		//
		// matchStatus = MatchStatus.NONEQUIVALENT;
		// tableViewerColumn = createTableViewerColumn(matchStatus.getSymbol(),
		// 20, matchStatus.getValue());
		// tableViewerColumn.getColumn().setToolTipText(matchStatus.getName() +
		// " - " + matchStatus.getComment());
		// tableViewerColumn.getColumn().setAlignment(SWT.CENTER);
		// tableViewerColumn.setLabelProvider(new
		// MyColumnLabelProvider(matchStatus.getValue()));
		// tableViewerColumn.getColumn().addSelectionListener(assignSelectionListener);

	}

	// private static Listener keyListener = new Listener() {
	//
	// @Override
	// public void handleEvent(Event event) {
	// System.out.println("Column 1 received event: " + event);
	// System.out.println("event.keyCode: " + event.keyCode);
	// System.out.println("event.character: " + event.character);
	//
	// }
	// };
	private static void updateMatchCounts() {
		Integer[] matchSummary = new Integer[] { 0, 0, 0, 0, 0, 0 };
		boolean noMatch = true;
		for (int rowNum = 1; rowNum < table.getItemCount(); rowNum++) {
			TableItem tableItem = table.getItem(rowNum);
			for (int colNum = 0; colNum < 6; colNum++) {
				if (!tableItem.getText(colNum).equals("")) {
					if (colNum > 0 && colNum < 5) {
						noMatch = false;
					}
					matchSummary[colNum]++;
				}
			}
		}
		TableItem tableItem = table.getItem(0);
		for (int colNum = 0; colNum < 6; colNum++) {
			tableItem.setText(colNum, matchSummary[colNum].toString());
		}

		Util.findView(FlowsWorkflow.ID);
		if (noMatch) {
			FlowsWorkflow.removeMatchFlowableRowNum(dataTableRowNum);
		} else {
			FlowsWorkflow.addMatchFlowableRowNum(dataTableRowNum);
		}

//		Util.findView(CSVTableView.ID);
//		CSVTableView.colorOneFlowableRow(dataTableRowNum);
	}

	public static int getCsvTableRowNum() {
		return dataTableRowNum;
	}

	public static void setCsvTableRowNum(int csvTableRowNum) {
		MatchFlowableTableView.dataTableRowNum = csvTableRowNum;
	}

	private static MouseListener tableMouseListener = new MouseListener() {

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			System.out.println("double click event :e =" + e);
		}

		@Override
		public void mouseDown(MouseEvent e) {
			System.out.println("mouse down event :e =" + e);
			if (e.button == 1) {
				leftClick(e);
			} else if (e.button == 3) {
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
			if (clickedRow < 1) {
				return;
			}
			if (clickedRow > table.getItemCount() - 2) {
				// HANDLE BLANK ROW SEARCH TOOL IF THEY CLICK LAST ROW
				return;
			}
			clickedCol = getTableColumnNumFromPoint(clickedRow, ptClick);
			if (clickedCol < 0) {
				return;
			}
			rowNumSelected = clickedRow;
			colNumSelected = clickedCol;

			LinkedHashMap<Resource, String> candidateMap = flowableToMatch.getMatchCandidates();
			if (clickedCol < 6) {
				Resource flowableCandidateResource = (Resource) candidateMap.keySet().toArray()[rowNumSelected - 1];
				int oldCol = MatchStatus.getNumberBySymbol(candidateMap.get(flowableCandidateResource));
				item.setText(oldCol, "");
				item.setText(clickedCol, MatchStatus.getByValue(clickedCol).getSymbol());
				candidateMap.put(flowableCandidateResource, MatchStatus.getByValue(clickedCol).getSymbol());
				table.deselectAll();
			} else {
				// HANDLE LOOKUPS AND STUFF
			}
			updateMatchCounts();
		}

		private void rightClick(MouseEvent event) {
			System.out.println("cellSelectionMouseDownListener event " + event);
		}
	};

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
				System.out.println("element= " + element);
			}
			String s = "";
			try {
				int size = dataRow.getColumnValues().size();
				if (dataColumnNumber < size) {
					s = dataRow.getColumnValues().get(dataColumnNumber);
				}
			} catch (Exception e) {
				System.out.println("dataRow=" + dataRow);
				e.printStackTrace();
			}
			return s;
		}
	}

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
				// s = dataRow.getRowNumber() + 1 + "";
				int rowNumPlus1 = dataRow.getRowNumber() + 1;
				s = rowNumPlus1 + "";
			} catch (Exception e) {
				System.out.println("dataRow=" + dataRow);
				e.printStackTrace();
			}
			return s;
		}
	}

	private static TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {

		final TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE, colNumber);
		final TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText(title);
		tableColumn.setWidth(bound);
		tableColumn.setResizable(true);
		tableColumn.setMoveable(false);
		// tableColumn.addSelectionListener(colSelectionListener);
		// tableColumn.addListener(SWT.MouseDown, (Listener)
		// columnMouseListener);

		// if (colNumber > 0) {
		// tableColumn.setToolTipText(csvColumnDefaultTooltip);
		// }

		return tableViewerColumn;
	}

	private static void createTableViewerMatchColumn(MatchStatus matchStatus) {
		int colNumber = matchStatus.getValue();
		TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE, colNumber);
		TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText(matchStatus.getSymbol());
		tableColumn.setWidth(20);
		tableColumn.setResizable(true);
		tableColumn.setMoveable(false);
		tableColumn.setToolTipText(matchStatus.getName() + " - " + matchStatus.getComment());
		tableColumn.setAlignment(SWT.CENTER);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(matchStatus.getValue()));
		// tableViewerColumn.getColumn().addSelectionListener(assignSelectionListener);
	}

	//
	private static SelectionListener colSelectionListener = new SelectionListener() {

		private void doit(SelectionEvent e) {
			// System.out.println("SelectionListener event e= " + e);
			if (e.getSource() instanceof TableColumn) {
				// TableColumn col = (TableColumn) e.getSource();
				// colNumSelected = table.indexOf(col);
				// if (colNumSelected > 0) {
				// headerMenu.setVisible(true);
				// }
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

	private static SelectionListener assignSelectionListener = new SelectionListener() {

		private void doit(SelectionEvent e) {
			CSVTableView.selectNextFlowable();

			// TableItem tableItem = table.getItem(0);
			// boolean gotMatch = false;
			// for (int colNum = 1; colNum < 5; colNum++) {
			// if (!tableItem.getText(colNum).equals("")) {
			// gotMatch = true;
			// break;
			// }
			// }
			// Util.findView(CSVTableView.ID);
			// // if (gotMatch) {
			// CSVTableView.colorFlowableRows();
			// FlowsWorkflow.updateFlowableCount();
			// // FIXME
			// // PROBLEM IS THAT ASSIGNMENT HAPPENS BEFORE "ASSIGN" BUTTON
			// // }
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

	private static SelectionListener advanceCSVTableListener = new SelectionListener() {

		private void doit(SelectionEvent e) {
			TableItem tableItem = table.getItem(0);
			boolean gotMatch = false;
			for (int colNum = 1; colNum < 5; colNum++) {
				if (!tableItem.getText(colNum).equals("")) {
					gotMatch = true;
					break;
				}
			}
			Util.findView(CSVTableView.ID);
			if (gotMatch) {
				CSVTableView.colorFlowableRows();
				// FlowsWorkflow.updateFlowableCount();
				// FIXME
				// PROBLEM IS THAT ASSIGNMENT HAPPENS BEFORE "ASSIGN" BUTTON
			}
			CSVTableView.selectNextFlowable();
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

	private static Composite outerComposite;

	public static void initialize() {
		initializeTable();
		// initializeIgnoreRowMenu();
		// initializeFixRowMenu();
		// CONSIDER: headerMenu;
		// CONSIDER: columnActionsMenu;
		// CONSIDER: ignoreRowMenu;
		// CONSIDER: fixCellMenu;
		// rowsToIgnore.clear();
		// rowNumSelected = -1;
		// colNumSelected = -1;

	}

	private static int getColumnNumSelected(Point point) {
		int clickedRow = getRowNumSelected(point);
		int clickedCol = getTableColumnNumFromPoint(clickedRow, point);
		if (clickedCol < 0) {
			return -1;
		}
		return clickedCol;
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

	public static void colorCell(int rowNumber, int colNumber, Color color) {
		if (rowNumber > -1 && rowNumber < table.getItemCount()) {
			TableItem tableItem = table.getItem(rowNumber);
			tableItem.setBackground(colNumber, color);
		}
	}

	public static void setFlowable(Flowable flowable) {
		// TODO Auto-generated method stub

	}

	public static Flowable getFlowableToMatch() {
		return flowableToMatch;
	}

	public static void setFlowableToMatch(Flowable flowableToMatch) {
		MatchFlowableTableView.flowableToMatch = flowableToMatch;
	}
}
