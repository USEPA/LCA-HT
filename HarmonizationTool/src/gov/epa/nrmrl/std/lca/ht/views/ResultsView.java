package gov.epa.nrmrl.std.lca.ht.views;

import java.util.ArrayList;
import java.util.List;

//import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView.MyColumnLabelProvider;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.edit.CSVEdittingSupport;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.TableProvider;
import harmonizationtool.query.HarmonyQuery;
import harmonizationtool.query.QueryResults;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class ResultsView extends ViewPart {
	public static final String ID = "HarmonizationTool.ResultsViewID";

	private TableViewer viewer;
	private static List<Object> columns = new ArrayList<Object>();
	private TableColumn columnSelected = null;
	private QueryResults queryResults = null;

	/**
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (parent instanceof Object[]) {
				return (Object[]) parent;
			}
			return new Object[0];
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		// viewer.addDoubleClickListener(new IDoubleClickListener(){
		// @Override
		// public void doubleClick(DoubleClickEvent event) {
		// ISelection selection = event.getSelection();
		// System.out.println("selection=" + selection);
		// }});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void update(List<String> data) {
		// viewer.setInput(data.toArray());
	}

	// public void iUpdate(List<String> data) {
	// // viewer.setInput(data.toArray());
	// }

	public void update(TableProvider tableProvider) {
		if (queryResults == null){
			queryResults = new QueryResults();
		}
		queryResults.setColumnHeaders(tableProvider.getHeaderRow());
		queryResults.setTableProvider(tableProvider);

		try {
			viewer.setContentProvider(new ArrayContentProvider());
			final Table table = viewer.getTable();
			removeColumns(table);
			createColumns(viewer, tableProvider);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			viewer.setInput(tableProvider.getData());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void update(QueryResults queryResults) {
		try {
			this.queryResults = queryResults;
			System.err.println("queryResults=" + queryResults);
			System.err.println("queryResults.getColumnHeaders()="
					+ queryResults.getColumnHeaders());
			System.out.println("queryResults.getColumnHeaders().toString()="
					+ queryResults.getColumnHeaders().toString());
			viewer.setContentProvider(new ArrayContentProvider());
			final Table table = viewer.getTable();
			removeColumns(table);
			createColumns(viewer, queryResults);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			viewer.setContentProvider(new ArrayContentProvider());
			TableProvider tableProvider = queryResults.getTableProvider();
			System.out.println("tableProvider.getData().size()="
					+ tableProvider.getData().size());
			System.out.println("tableProvider.getData().toString()="
					+ tableProvider.getData().toString());
			viewer.setInput(tableProvider.getData());
		} catch (Exception e) {
			e.printStackTrace();
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
		try {
			table.setRedraw(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createColumns(final TableViewer viewer,
			TableProvider tableProvider) {

		DataRow columnHeaders = new DataRow();
		columnHeaders = tableProvider.getHeaderRow();

		ArrayList<String> titles = new ArrayList<String>();
		ArrayList<Integer> bounds = new ArrayList<Integer>();
		for (String header : columnHeaders.getColumnValues()) {
			titles.add(header);
			bounds.add(100);
		}
		String[] titlesArray = new String[titles.size()];
		titles.toArray(titlesArray);
		int[] boundsArray = new int[bounds.size()];
		int indx = 0;
		for (Integer integer : bounds) {
			boundsArray[indx++] = integer;
		}
		for (int i = 0; i < titles.size(); i++) {
			TableViewerColumn col = createTableViewerColumn(titlesArray[i],
					boundsArray[i], i);
			col.setLabelProvider(new MyColumnLabelProvider(i));
			columns.add(col);
		}
	}

	private void createColumns(final TableViewer viewer,
			QueryResults queryResults) {
		DataRow columnHeaders = queryResults.getColumnHeaders();

		ArrayList<String> titles = new ArrayList<String>();
		ArrayList<Integer> bounds = new ArrayList<Integer>();
		for (String header : columnHeaders.getColumnValues()) {
			titles.add(header);
			bounds.add(100);
		}
		String[] titlesArray = new String[titles.size()];
		titles.toArray(titlesArray);
		int[] boundsArray = new int[bounds.size()];
		int indx = 0;
		for (Integer integer : bounds) {
			boundsArray[indx++] = integer;
		}
		for (int i = 0; i < titles.size(); i++) {
			TableViewerColumn col = createTableViewerColumn(titlesArray[i],
					boundsArray[i], i);
			col.setLabelProvider(new MyColumnLabelProvider(i));
			columns.add(col);
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
	private TableViewerColumn createTableViewerColumn(String title, int bound,
			final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer,
				SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		viewerColumn
				.setEditingSupport(new CSVEdittingSupport(viewer, colNumber));
		// column.addListener(eventType, new Listener(){});
		column.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println(e.toString());
				System.out.println(e.getSource().getClass());
				if (e.getSource() instanceof TableColumn) {
					TableColumn col = (TableColumn) e.getSource();
					System.out.println("col: " + col.getText());
					columnSelected = col;
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				System.out.println(e.toString());
			}

		});
		// Now add a MenuItem for the colum to the table menu
		// createMenuItem(headerMenu, column);
		return viewerColumn;
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

	public QueryResults getQueryResults() {
		return queryResults;
	}

	public void formatForTransform0() {
		try {
			viewer.setContentProvider(new ArrayContentProvider());
			Table table = viewer.getTable();
//			TableColumn[] tableColumn = table.getColumns();
			TableItem[] tableItems = table.getItems();
			String keyDataSource = tableItems[0].getText(0);
			int keyDataRow = 0;
			for (int i = 0; i < tableItems.length; i++) {
				TableItem tableItem = tableItems[i];
				if (keyDataSource.equals(tableItem.getText(0))) {
					keyDataRow = i;
					tableItem.setBackground(0, SWTResourceManager
							.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
				} else {
					// if (tableItem.getText(0).equals("")) {
					// table.remove(i);
					// // i = i - 1;
					// continue;
					// }
					for (int j = 1; j < table.getColumnCount(); j++) {
						if (tableItem
								.getText(j)
								.toUpperCase()
								.equals(tableItems[keyDataRow].getText(j)
										.toUpperCase())) {
							tableItem.setBackground(j, SWTResourceManager
									.getColor(SWT.COLOR_GREEN));
							tableItems[keyDataRow].setBackground(j,
									SWTResourceManager
											.getColor(SWT.COLOR_GREEN));
						}
						// tableItem.ad
						// tableItem.addListener(SWT.MouseDown, new Listener() {
						//
						// @Override
						// public void handleEvent(Event event) {
						// System.out.println("event.item = " +
						// event.item.toString());
						// TableItem tableItem = (TableItem) event.item;
						// // int j =
						// tableItem.setBackground(0,
						// SWTResourceManager.getColor(SWT.COLOR_BLUE));
						// // tableItems[keyDataRow].setBackground(j,
						// // SWTResourceManager.getColor(SWT.COLOR_GREEN));
						// }
						//
						// });
						// DO GREAT STUFF HERE!!!
						// System.out.println("tableItem.getText(i) = " +
						// tableItem.getText(i));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
