package harmonizationtool;

import java.util.ArrayList;
import java.util.List;

import harmonizationtool.ResultsView.ViewContentProvider;
import harmonizationtool.ResultsView.ViewLabelProvider;
import harmonizationtool.ViewData.MyColumnLabelProvider;
import harmonizationtool.comands.SelectTDB;
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
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
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

public class ResultsTreeEditor extends ViewPart {
	public static final String ID = "HarmonizationTool.ResultsTreeEditorID";

//	private TableViewer viewer;
	private TreeViewer viewer;
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
	
	class MyContentProvider implements ITreeContentProvider {
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}
		
		public Object[] getElements(Object inputElement) {
			return ((MyModel) inputElement).child.toArray();
		}

		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		public Object getParent(Object element) {
			if (element == null) {
				return null;
			}
			return ((MyModel) element).parent;
		}

		public boolean hasChildren(Object element) {
			return ((MyModel) element).child.size() > 0;
		}
	}
	
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
	
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.BORDER
				| SWT.FULL_SELECTION);
		viewer.getTree().setLinesVisible(true);
		viewer.getTree().setHeaderVisible(true);
		

		// viewer.addDoubleClickListener(new IDoubleClickListener(){
		// @Override
		// public void doubleClick(DoubleClickEvent event) {
		// ISelection selection = event.getSelection();
		// System.out.println("selection=" + selection);
		// }});
	}
	
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void update(List<String> data) {
		// viewer.setInput(data.toArray());
	}

//	public void update(TableProvider tableProvider) {
//	try {
//		viewer.setContentProvider(new ArrayContentProvider());
//		final Table table = viewer.getTable();
//		removeColumns(table);
//		createColumns(viewer, tableProvider);
//		table.setHeaderVisible(true);
//		table.setLinesVisible(true);
//		viewer.setInput(tableProvider.getData());
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
//}
//	public void update(QueryResults queryResults) {
//		try {
//			this.queryResults = queryResults;
//			System.err.println("queryResults=" + queryResults);
//			System.err.println("queryResults.getColumnHeaders()="
//					+ queryResults.getColumnHeaders());
//			System.out.println("queryResults.getColumnHeaders().toString()="
//					+ queryResults.getColumnHeaders().toString());
//			viewer.setContentProvider(new ArrayContentProvider());
//			final Table table = viewer.getTable();
//			removeColumns(table);
//			createColumns(viewer, queryResults);
//			table.setHeaderVisible(true);
//			table.setLinesVisible(true);
//			viewer.setContentProvider(new ArrayContentProvider());
//			TableProvider tableProvider = queryResults.getTableProvider();
//			System.out.println("tableProvider.getData().size()="
//					+ tableProvider.getData().size());
//			System.out.println("tableProvider.getData().toString()="
//					+ tableProvider.getData().toString());
//			viewer.setInput(tableProvider.getData());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

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

	
	public class MyModel {
		public MyModel parent;

		public ArrayList child = new ArrayList();

		public int counter;

		public boolean bool;

		public MyModel(int counter, MyModel parent) {
			this.parent = parent;
			this.counter = counter;
		}

		public String toString() {
			String rv = "Item ";
			if (parent != null) {
				rv = parent.toString() + ".";
			}

			rv += counter;

			return rv;
		}
	}
//	class ViewContentProvider implements IStructuredContentProvider {
//		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
//		}
//
//		public void dispose() {
//		}
//
//		public Object[] getElements(Object parent) {
//			if (parent instanceof Object[]) {
//				return (Object[]) parent;
//			}
//			return new Object[0];
//		}
//	}
//
//	class ViewLabelProvider extends LabelProvider implements
//			ITableLabelProvider {
//		public String getColumnText(Object obj, int index) {
//			return getText(obj);
//		}
//
//		public Image getColumnImage(Object obj, int index) {
//			return getImage(obj);
//		}
//
//	}

	
	
	

//	final TreeViewerFocusCellManager mgr = new TreeViewerFocusCellManager(v,new FocusCellOwnerDrawHighlighter(v));
//	ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(v) {
//		protected boolean isEditorActivationEvent(
//				ColumnViewerEditorActivationEvent event) {
//			return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
//					|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
//					|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && ( event.keyCode == SWT.CR || event.character == ' ' ))
//					|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
//		}
//	};
//
//	TreeViewerEditor.create(v, mgr, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
//			| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
//			| ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);
//
//	final TextCellEditor textCellEditor = new TextCellEditor(v.getTree());
//	final CheckboxCellEditor checkboxCellEditor = new CheckboxCellEditor(v.getTree());
//
//	TreeViewerColumn column = new TreeViewerColumn(v, SWT.NONE);
//	column.getColumn().setWidth(200);
//	column.getColumn().setMoveable(true);
//	column.getColumn().setText("Column 1");
//	column.setLabelProvider(new ColumnLabelProvider() {
//
//		public String getText(Object element) {
//			return "Column 1 => " + element.toString();
//		}
//
//	});
//	column.setEditingSupport(new EditingSupport(v) {
//		protected boolean canEdit(Object element) {
//			return false;
//		}
//
//		protected CellEditor getCellEditor(Object element) {
//			return textCellEditor;
//		}
//
//		protected Object getValue(Object element) {
//			return ((MyModel) element).counter + "";
//		}
//
//		protected void setValue(Object element, Object value) {
//			((MyModel) element).counter = Integer
//					.parseInt(value.toString());
//			v.update(element, null);
//		}
//	});

	column = new TreeViewerColumn(v, SWT.NONE);
	column.getColumn().setWidth(200);
	column.getColumn().setMoveable(true);
	column.getColumn().setText("Column 2");
	column.setLabelProvider(new ColumnLabelProvider() {

		public String getText(Object element) {
			return "Column 2 => " + element.toString();
		}

	});
	column.setEditingSupport(new EditingSupport(v) {
		protected boolean canEdit(Object element) {
			return true;
		}

		protected CellEditor getCellEditor(Object element) {
			return textCellEditor;
		}

		protected Object getValue(Object element) {
			return ((MyModel) element).counter + "";
		}

		protected void setValue(Object element, Object value) {
			((MyModel) element).counter = Integer
			.parseInt(value.toString());
			v.update(element, null);
		}
	});

	column = new TreeViewerColumn(v, SWT.NONE);
	column.getColumn().setWidth(200);
	column.getColumn().setMoveable(true);
	column.getColumn().setText("Column 3");
	column.setLabelProvider(new ColumnLabelProvider() {

		public String getText(Object element) {
			return ((MyModel)element).bool + "";
		}

	});
	column.setEditingSupport(new EditingSupport(v) {
		protected boolean canEdit(Object element) {
			return true;
		}

		protected CellEditor getCellEditor(Object element) {
			return checkboxCellEditor;
		}

		protected Object getValue(Object element) {
			return new Boolean(((MyModel) element).bool);
		}

		protected void setValue(Object element, Object value) {
			((MyModel) element).bool = ((Boolean)value).booleanValue();
			v.update(element, null);
		}
	});

	v.setContentProvider(new MyContentProvider());
	v.setInput(createModel());
	v.getControl().addTraverseListener(new TraverseListener() {

		public void keyTraversed(TraverseEvent e) {
			if( (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) && mgr.getFocusCell().getColumnIndex() == 2 ) {
				ColumnViewerEditor editor = v.getColumnViewerEditor();
				ViewerCell cell = mgr.getFocusCell();

				try {
					Method m = ColumnViewerEditor.class.getDeclaredMethod("processTraverseEvent", new Class[] {int.class,ViewerRow.class,TraverseEvent.class});
					m.setAccessible(true);
					m.invoke(editor, new Object[] { new Integer(cell.getColumnIndex()), cell.getViewerRow(), e });
				} catch (SecurityException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (NoSuchMethodException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IllegalArgumentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

	});
}

private MyModel createModel() {

	MyModel root = new MyModel(0, null);
	root.counter = 0;

	MyModel tmp;
	MyModel subItem;
	for (int i = 1; i < 10; i++) {
		tmp = new MyModel(i, root);
		root.child.add(tmp);
		for (int j = 1; j < i; j++) {
			subItem = new MyModel(j, tmp);
			subItem.child.add(new MyModel(j * 100, subItem));
			tmp.child.add(subItem);
		}
	}

	return root;
}

private class MyContentProvider implements ITreeContentProvider {

	public Object[] getElements(Object inputElement) {
		return ((MyModel) inputElement).child.toArray();
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	public Object getParent(Object element) {
		if (element == null) {
			return null;
		}
		return ((MyModel) element).parent;
	}

	public boolean hasChildren(Object element) {
		return ((MyModel) element).child.size() > 0;
	}

}

public class MyModel {
	public MyModel parent;

	public ArrayList child = new ArrayList();

	public int counter;

	public boolean bool;

	public MyModel(int counter, MyModel parent) {
		this.parent = parent;
		this.counter = counter;
	}

	public String toString() {
		String rv = "Item ";
		if (parent != null) {
			rv = parent.toString() + ".";
		}

		rv += counter;

		return rv;
	}
}



	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */

	/**
	 * Passing the focus request to the viewer's control.
	 */


	// public void iUpdate(List<String> data) {
	// // viewer.setInput(data.toArray());
	// }




	private void createColumns(final TableViewer viewer,
			TableProvider tableProvider) {

		DataRow columnHeaders = new DataRow();
		columnHeaders = tableProvider.getHeaderNames();

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


	public QueryResults getQueryResults() {
		return queryResults;
	}

	public void formatForTransform0() {
		try {
			viewer.setContentProvider(new ArrayContentProvider());
			Table table = viewer.getTable();
//			TableColumn[] tableColumn = table.getColumns();
			TableItem[] tableItems = table.getItems();
			String keyDataSet = tableItems[0].getText(0);
			int keyDataRow = 0;
			for (int i = 0; i < tableItems.length; i++) {
				TableItem tableItem = tableItems[i];
				if (keyDataSet.equals(tableItem.getText(0))) {
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



//========================================================================
//========================================================================

/*******************************************************************************
 * Copyright (c) 2006, 2010 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *******************************************************************************/


/**
 * Demonstrate how to work-around 3.3.1 limitation when it comes to TAB-Traversal and
 * checkbox editors. 3.4 will hopefully provide provide an out-of-the-box fix see bug 198502
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */

	public Snippet048TreeViewerTabWithCheckboxFor3_3(final Shell shell) {
		final TreeViewer v = new TreeViewer(shell, SWT.BORDER
				| SWT.FULL_SELECTION);
		v.getTree().setLinesVisible(true);
		v.getTree().setHeaderVisible(true);

		final TreeViewerFocusCellManager mgr = new TreeViewerFocusCellManager(v,new FocusCellOwnerDrawHighlighter(v));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(v) {
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && ( event.keyCode == SWT.CR || event.character == ' ' ))
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};

		TreeViewerEditor.create(v, mgr, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				| ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);

		final TextCellEditor textCellEditor = new TextCellEditor(v.getTree());
		final CheckboxCellEditor checkboxCellEditor = new CheckboxCellEditor(v.getTree());

		TreeViewerColumn column = new TreeViewerColumn(v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("Column 1");
		column.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				return "Column 1 => " + element.toString();
			}

		});
		column.setEditingSupport(new EditingSupport(v) {
			protected boolean canEdit(Object element) {
				return false;
			}

			protected CellEditor getCellEditor(Object element) {
				return textCellEditor;
			}

			protected Object getValue(Object element) {
				return ((MyModel) element).counter + "";
			}

			protected void setValue(Object element, Object value) {
				((MyModel) element).counter = Integer
						.parseInt(value.toString());
				v.update(element, null);
			}
		});

		column = new TreeViewerColumn(v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("Column 2");
		column.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				return "Column 2 => " + element.toString();
			}

		});
		column.setEditingSupport(new EditingSupport(v) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				return textCellEditor;
			}

			protected Object getValue(Object element) {
				return ((MyModel) element).counter + "";
			}

			protected void setValue(Object element, Object value) {
				((MyModel) element).counter = Integer
				.parseInt(value.toString());
				v.update(element, null);
			}
		});

		column = new TreeViewerColumn(v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("Column 3");
		column.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				return ((MyModel)element).bool + "";
			}

		});
		column.setEditingSupport(new EditingSupport(v) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				return checkboxCellEditor;
			}

			protected Object getValue(Object element) {
				return new Boolean(((MyModel) element).bool);
			}

			protected void setValue(Object element, Object value) {
				((MyModel) element).bool = ((Boolean)value).booleanValue();
				v.update(element, null);
			}
		});

		v.setContentProvider(new MyContentProvider());
		v.setInput(createModel());
		v.getControl().addTraverseListener(new TraverseListener() {

			public void keyTraversed(TraverseEvent e) {
				if( (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) && mgr.getFocusCell().getColumnIndex() == 2 ) {
					ColumnViewerEditor editor = v.getColumnViewerEditor();
					ViewerCell cell = mgr.getFocusCell();

					try {
						Method m = ColumnViewerEditor.class.getDeclaredMethod("processTraverseEvent", new Class[] {int.class,ViewerRow.class,TraverseEvent.class});
						m.setAccessible(true);
						m.invoke(editor, new Object[] { new Integer(cell.getColumnIndex()), cell.getViewerRow(), e });
					} catch (SecurityException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (NoSuchMethodException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IllegalArgumentException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (InvocationTargetException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}

		});
	}

	private MyModel createModel() {

		MyModel root = new MyModel(0, null);
		root.counter = 0;

		MyModel tmp;
		MyModel subItem;
		for (int i = 1; i < 10; i++) {
			tmp = new MyModel(i, root);
			root.child.add(tmp);
			for (int j = 1; j < i; j++) {
				subItem = new MyModel(j, tmp);
				subItem.child.add(new MyModel(j * 100, subItem));
				tmp.child.add(subItem);
			}
		}

		return root;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet048TreeViewerTabWithCheckboxFor3_3(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}

	private class MyContentProvider implements ITreeContentProvider {

		public Object[] getElements(Object inputElement) {
			return ((MyModel) inputElement).child.toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		public Object getParent(Object element) {
			if (element == null) {
				return null;
			}
			return ((MyModel) element).parent;
		}

		public boolean hasChildren(Object element) {
			return ((MyModel) element).child.size() > 0;
		}

	}

	public class MyModel {
		public MyModel parent;

		public ArrayList child = new ArrayList();

		public int counter;

		public boolean bool;

		public MyModel(int counter, MyModel parent) {
			this.parent = parent;
			this.counter = counter;
		}

		public String toString() {
			String rv = "Item ";
			if (parent != null) {
				rv = parent.toString() + ".";
			}

			rv += counter;

			return rv;
		}
	}

}


