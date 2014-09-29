package gov.epa.nrmrl.std.lca.ht.flowable.mgr;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import com.hp.hpl.jena.rdf.model.Resource;

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
	private static Flowable flowableThing;

	private static TextCellEditor editor;

	public MatchFlowableTableView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(null);
		Label label = new Label(composite, SWT.NONE);
		label.setText("label");
		System.out.println("hello, from sunny MatchFlowableTableView!");
		initializeTableViewer(composite);
		// initializePopup(composite);
		initialize();

		parent.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				table.setSize(table.getParent().getSize());
			}
			// THIS IS NOT PERFECT
			// WHEN THE WINDOW IS RESIZED SMALLER, THE TABLE OVER RUNS A LITTLE
		});

		// table.addListener(SWT.MeasureItem, new Listener() {
		// public void handleEvent(Event event) {
		// System.out.println("MeasureItem Event: " + event);
		// TableItem item = (TableItem) event.item;
		// String text = item.getText(event.index);
		// Point size = event.gc.textExtent(text);
		// event.height = Math.max(event.height, size.y);
		// }
		// });

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private static void initializeTableViewer(Composite composite) {
		tableViewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);
		editor = new TextCellEditor(tableViewer.getTable());
		tableViewer.setContentProvider(new ArrayContentProvider());
		createColumns();
	}

	//
	private static void initializeTable() {
		table = tableViewer.getTable();
		table.addListener(SWT.KeyDown, keyListener);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}

	public static void update(int rowNumber) {
		initialize();
		TableProvider tableProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey());
		DataRow dataRow = tableProvider.getData().get(rowNumber);
		// Resource curDataSet = tableProvider.getDataSourceProvider().getTdbResource();
		// List<Resource> queryPlusCandidates = new ArrayList<Resource>();
		Flowable qFlowable = dataRow.getFlowable();
		if (qFlowable == null) {
			return;
		}
		qFlowable.clearSyncDataFromTDB();
		table.clearAll();
		int rowCount = qFlowable.getMatchCandidates().size() + 2;
		table.setItemCount(rowCount);
		setResultRowData(0, qFlowable);
		table.getItem(0).setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		LinkedHashMap<Resource, String> matchCandidateResources = qFlowable.getMatchCandidates();
		if (matchCandidateResources == null) {
			return;
		}
		int row = 1;
		for (Object dFlowableResource : matchCandidateResources.keySet()) {
			Flowable dFlowable = new Flowable((Resource) dFlowableResource);
			setResultRowData(row, dFlowable);
			String matchStatus = matchCandidateResources.get(dFlowable);
			if (matchStatus.equals("?")) {
				table.getItem(row).setText(0, matchStatus);
			} else if (matchStatus.equals("=")) {
				table.getItem(row).setText(1, matchStatus);
			} else if (matchStatus.equals("<")) {
				table.getItem(row).setText(2, matchStatus);
			} else if (matchStatus.equals(">")) {
				table.getItem(row).setText(3, matchStatus);
			} else if (matchStatus.equals("~")) {
				table.getItem(row).setText(4, matchStatus);
			} else if (matchStatus.equals("X")) {
				table.getItem(row).setText(5, matchStatus);
			} else if (matchStatus.equals("+")) {
				table.getItem(row).setText(6, matchStatus);
			}
			row++;
		}
	}

	private static void setResultRowData(int rowNum, Flowable flowable) {
		TableItem qRow = table.getItem(rowNum);
		if (flowable.getDataSource() != null) {
			qRow.setText(7, flowable.getDataSource());
		}
		qRow.setText(8, flowable.getName());
		qRow.setText(9, flowable.getCas());
		String synConcat = "";
		String[] synonyms = flowable.getSynonyms();
		if (synonyms.length > 0) {
			synConcat = synonyms[0];
		}
		for (int i = 0; i < synonyms.length; i++) {
			String synonym = synonyms[i];
			synConcat += " -or- " + synonym;
		}
		qRow.setText(10, synConcat);
	}

	// private static void update(Resource flowableResource) {
	// TableProvider miniTableProvider = new TableProvider();
	// DataRow miniDataRow = new DataRow();
	//
	// String name = "";
	// if (flowableResource.hasProperty(RDFS.label)) {
	// Statement statement = flowableResource.getProperty(RDFS.label);
	// name = statement.getObject().asLiteral().getString();
	// // name =
	// // resource.getPropertyResourceValue(RDFS.label).getLocalName();
	//
	// }
	// miniDataRow.add(" - ");
	//
	// Resource dataSourceResource = flowableResource.getProperty(ECO.hasDataSource).getObject().asResource();
	// String dataSourceName = dataSourceResource.getProperty(RDFS.label).getObject().asLiteral().getString();
	// miniDataRow.add(dataSourceName);
	//
	// miniDataRow.add(name);
	//
	// String casrn = "";
	// if (flowableResource.hasProperty(ECO.casNumber)) {
	//
	// Statement statement = flowableResource.getProperty(ECO.casNumber);
	// casrn = statement.getObject().asLiteral().getString();
	// // casrn =
	// // resource.getPropertyResourceValue(ECO.casNumber).getLocalName();
	// }
	// miniDataRow.add(casrn);
	//
	// String syns = "";
	// StmtIterator stmtIterator = flowableResource.listProperties(SKOS.altLabel);
	// System.out.println("syns stmtIterator = " + stmtIterator);
	// while (stmtIterator.hasNext()) {
	// String synonym = stmtIterator.next().getObject().asLiteral().getString();
	// syns += synonym + System.getProperty("line.separator");
	// System.out.println("syns = " + syns);
	// }
	// miniDataRow.add(syns);
	//
	// miniDataRow.add("other: N/A");
	// miniTableProvider.addDataRow(miniDataRow);
	//
	// update(miniTableProvider);
	// // TODO - NEED TO ARRANGE A BLANK ROW FOR SEARCH TOOL
	// }
	//
	// private static void update(List<Resource> flowableResources) {
	// TableProvider miniTableProvider = new TableProvider();
	//
	// for (int i = 0; i < flowableResources.size(); i++) {
	// DataRow miniDataRow = new DataRow();
	//
	// Resource resource = flowableResources.get(i);
	// // TableItem tableItem = table.getItem(i);
	//
	// if (i == 0) {
	// miniDataRow.add(" - ");
	// } else {
	// miniDataRow.add(" " + i + " ");
	// }
	//
	// Resource dataSourceResource = resource.getProperty(ECO.hasDataSource).getObject().asResource();
	// String dataSourceName = dataSourceResource.getProperty(RDFS.label).getObject().asLiteral().getString();
	// miniDataRow.add(dataSourceName);
	//
	// String name = "";
	// if (resource.hasProperty(RDFS.label)) {
	// Statement statement = resource.getProperty(RDFS.label);
	// name = statement.getObject().asLiteral().getString();
	// }
	// miniDataRow.add(name);
	//
	// String casrn = "";
	// if (resource.hasProperty(ECO.casNumber)) {
	// Statement statement = resource.getProperty(ECO.casNumber);
	// casrn = statement.getObject().asLiteral().getString();
	// }
	// miniDataRow.add(casrn);
	//
	// String syns = "";
	// StmtIterator stmtIterator = resource.listProperties(SKOS.altLabel);
	// System.out.println("syns stmtIterator = " + stmtIterator);
	// while (stmtIterator.hasNext()) {
	// String synonym = stmtIterator.next().getObject().asLiteral().getString();
	// syns += synonym + System.getProperty("line.separator");
	// System.out.println("syns = " + syns);
	// }
	// miniDataRow.add(syns);
	//
	// miniDataRow.add("other: N/A");
	// miniTableProvider.addDataRow(miniDataRow);
	// }
	// update(miniTableProvider);
	// }

	// private static void update(TableProvider miniTableProvider) {
	// // reset();
	// // createColumns();
	// // TableProvider tableProvider = TableKeeper.getTableProvider(key);
	// // tableViewer.setInput(null);
	// table.removeAll();
	// tableViewer.refresh();
	// tableViewer.setInput(miniTableProvider.getData());
	// table.getItem(0).setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
	//
	// int rowCount = table.getItemCount();
	// // ADD A ROW FOR SEARCHING
	// table.setItemCount(rowCount + 1);
	// TableItem queryRow = table.getItem(rowCount);
	// queryRow.addListener(SWT.Selection, new Listener() {
	//
	// @Override
	// public void handleEvent(Event event) {
	// System.out.println("Event =" + event);
	// }
	// });
	//
	// // table.addMouseListener(mouseListener);
	//
	// // try{
	// // table.getItem(4).getData();
	// // } catch (Exception e) {
	// // e.printStackTrace();
	// // }
	// //
	// System.out.println("table.getItemCount() " + table.getItemCount());
	// System.out.println("miniTableProvider.getColumnCount() " + miniTableProvider.getColumnCount());
	//
	// // miniTableProvider.resetAssignedCSVColumnInfo();
	// // colorRowNumberColumn();
	// // table.setItem
	// table.setSize(table.getParent().getSize());
	// // initializeHeaderMenu();
	// // initializeColumnActionsMenu();
	// }

	//
	// public static void reset() {
	// tableViewer.setInput(null);
	// // removeColumns();
	// }

	// private static void removeColumns() {
	// table.setRedraw(false);
	// while (table.getColumnCount() > 0) {
	// table.getColumns()[0].dispose();
	// }
	// table.setRedraw(true);
	// }

	private static void createColumns() {

		TableViewerColumn tableViewerColumn = createTableViewerColumn("?", 20, 0);
		tableViewerColumn
				.getColumn()
				.setToolTipText(
						"Not assigned.  A check in this column indicates that the Flowable in this row is not explicitly related to that in the top row.");
		tableViewerColumn.getColumn().setAlignment(SWT.CENTER);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(0));
		tableViewerColumn.getColumn().addSelectionListener(assignSelectionListener);

		tableViewerColumn = createTableViewerColumn("=", 20, 1);
		tableViewerColumn
				.getColumn()
				.setToolTipText(
						"Equivalent.  A check in this column indicates that the Flowable in this row is the same as that in the top row.");
		tableViewerColumn.getColumn().setAlignment(SWT.CENTER);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(1));
		tableViewerColumn.getColumn().addSelectionListener(assignSelectionListener);

		tableViewerColumn = createTableViewerColumn("<", 20, 2);
		tableViewerColumn
				.getColumn()
				.setToolTipText(
						"Subset.  A check in this column indicates that the Flowable in this row is a subset of that in the top row.");
		tableViewerColumn.getColumn().setAlignment(SWT.CENTER);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(2));
		tableViewerColumn.getColumn().addSelectionListener(assignSelectionListener);

		tableViewerColumn = createTableViewerColumn(">", 20, 3);
		tableViewerColumn
				.getColumn()
				.setToolTipText(
						"Superset.  A check in this column indicates that the Flowable in this row is a superset of that in the top row.");
		tableViewerColumn.getColumn().setAlignment(SWT.CENTER);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(3));
		tableViewerColumn.getColumn().addSelectionListener(assignSelectionListener);

		tableViewerColumn = createTableViewerColumn("~", 20, 4);
		tableViewerColumn
				.getColumn()
				.setToolTipText(
						"Proxy.  A check in this column indicates that the Flowable in this row is an imperfect match for that in the top row.");
		tableViewerColumn.getColumn().setAlignment(SWT.CENTER);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(4));
		tableViewerColumn.getColumn().addSelectionListener(assignSelectionListener);

		tableViewerColumn = createTableViewerColumn("X", 20, 5);
		tableViewerColumn
				.getColumn()
				.setToolTipText(
						"Explicit difference.  A check in this column indicates that the Flowable in this row is not the same as that in the top row (despite some evidence to of a match).");
		tableViewerColumn.getColumn().setAlignment(SWT.CENTER);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(5));
		tableViewerColumn.getColumn().addSelectionListener(assignSelectionListener);

		tableViewerColumn = createTableViewerColumn("+", 20, 6);
		tableViewerColumn
				.getColumn()
				.setToolTipText(
						"Suggested addition.  A check in this column indicates that the Flowable in this row is not available in the Master List, and should be considered a candidate new Flowable.");
		tableViewerColumn.getColumn().setAlignment(SWT.CENTER);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(6));
		tableViewerColumn.getColumn().addSelectionListener(assignSelectionListener);
		// tableViewerColumn.getColumn().addListener(SWT.KeyDown, keyListener);

		tableViewerColumn = createTableViewerColumn("Data Source", 200, 7);
		tableViewerColumn.getColumn().setAlignment(SWT.LEFT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(7));

		tableViewerColumn = createTableViewerColumn(Flowable.flowableNameString, 300, 8);
		tableViewerColumn.getColumn().setAlignment(SWT.LEFT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(8));

		// String casString = Flowable;
		tableViewerColumn = createTableViewerColumn(Flowable.casString, 100, 9);
		tableViewerColumn.getColumn().setAlignment(SWT.RIGHT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(9));

		tableViewerColumn = createTableViewerColumn(Flowable.flowableSynonymString, 300, 10);
		tableViewerColumn.getColumn().setAlignment(SWT.LEFT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(10));

		tableViewerColumn = createTableViewerColumn("Other", 200, 11);
		tableViewerColumn.getColumn().setAlignment(SWT.LEFT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(11));
	}

	private static Listener keyListener = new Listener() {

		@Override
		public void handleEvent(Event event) {
			System.out.println("Column 1 received event: " + event);
			System.out.println("event.keyCode: " + event.keyCode);
			System.out.println("event.character: " + event.character);

		}
	};

	private static MouseListener mouseListener = new MouseListener() {

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
			clickedCol = getTableColumnNumFromPoint(clickedRow, ptClick);
			if (clickedCol > 0) {
				table.deselectAll();
				return;
			}
			table.select(clickedRow);
			rowNumSelected = clickedRow;
			colNumSelected = clickedCol;
			// rowMenu.setVisible(true);
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
			// int dataClickedCol = clickedCol - 1;
			if (clickedCol < 0) {
				return;
			}

			rowNumSelected = clickedRow;
			colNumSelected = clickedCol;
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
			if (e.getSource() instanceof TableColumn) {
				Point point = new Point(e.x, e.y);
				colNumSelected = getColumnNumSelected(point);
				rowNumSelected = getRowNumSelected(point);
				if (rowNumSelected == 0) {
					return;
				}
				Util.findView(CSVTableView.ID);
				DataRow dataRow = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getData()
						.get(rowNumSelected);
				Flowable flowable = dataRow.getFlowable();
				// Object[] thing = flowable.getMatchCandidates().keySet().toArray()[colNumSelected];

				for (int i = 0; i < 7; i++) {
					colorCell(rowNumSelected, i, SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
				}
				colorCell(rowNumSelected, colNumSelected, SWTResourceManager.getColor(SWT.COLOR_CYAN));

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
}
