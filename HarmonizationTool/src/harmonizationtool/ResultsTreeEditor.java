package harmonizationtool;

//import harmonizationtool.ResultsView.MyColumnLabelProvider;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.TableProvider;
import harmonizationtool.query.QueryResults;

//import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
//import harmonizationtool.ResultsTreeEditor.TreeNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
//import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
//import org.eclipse.jface.viewers.TableViewer;
//import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.jface.viewers.ViewerRow;

public class ResultsTreeEditor extends ViewPart {
	public static final String ID = "HarmonizationTool.ResultsTreeEditorID";
	private TreeViewer treeViewer;
	private static List<Object> columns = new ArrayList<Object>();
	private TableColumn columnSelected = null;

	// private QueryResults queryResults = null;

	/**
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	class TreeContentProvider implements ITreeContentProvider {
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

		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		public Object getParent(Object element) {
			if (element == null) {
				return null;
			}
			return ((TreeNode) element).parent;
		}

		public boolean hasChildren(Object element) {
			return ((TreeNode) element).child.size() > 0;
		}

	}

	class TreeLabelProvider extends LabelProvider implements
			ITreePathLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		public void updateLabel(ViewerLabel label, TreePath elementPath) {
			// TODO Auto-generated method stub
			// treePathProvider.updateLabel(label, elementPath);

		}

	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		treeViewer.setContentProvider(new TreeContentProvider());
		treeViewer.setLabelProvider(new TreeLabelProvider());
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	public void update(List<String> data) {
		// INCLUDING THIS SINCE IT IS IN ResultsView (though not implemented
		// there, either
	}

	public void update(TableProvider tableProvider) {
		try {
			treeViewer.setContentProvider(new TreeContentProvider());
			final Tree tree = treeViewer.getTree();
			tree.removeAll();
			createColumns(tableProvider);
			TreeNode trunk = createTrunk(tableProvider); // TODO: WORK HERE
			tree.setHeaderVisible(true);
			tree.setLinesVisible(true);
			treeViewer.setInput(tableProvider.getData()); // THIS DOES NOTHING !!
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void update(QueryResults queryResults) {
		// INCLUDING THIS SINCE IT IS IN ResultsView (not sure if it is needed)
	}
	
	private void removeColumns(Tree tree) {
		// INCLUDING THIS SINCE IT IS IN ResultsView (not sure if it is needed)
	}
	
	private void createColumns(TableProvider tableProvider) {
		DataRow columnHeaders = new DataRow();
		columnHeaders = tableProvider.getHeaderNames();
		for (int i = 0; i < columnHeaders.getSize(); i++) {
			TreeViewerColumn column = new TreeViewerColumn(treeViewer, SWT.NONE);
			column.getColumn().setWidth(100);
			column.getColumn().setMoveable(true);
			column.getColumn().setText(columnHeaders.get(i));
		}
	}

	public class TreeNode {
		public TreeNode parent;
		public ArrayList child = new ArrayList();
		public int counter;

		// public boolean bool;

		public TreeNode(int counter, TreeNode parent) {
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

	@SuppressWarnings("unchecked") // ASK TOMMY WHAT THIS IS
	private TreeNode createTrunk(TableProvider tableProvider) {
		if (tableProvider == null) {
			return null;
		}

		List<DataRow> data = tableProvider.getData();
		DataRow firstRow = data.get(0);
		String keyDataSet = firstRow.get(0);
		TreeNode trunk = new TreeNode(0, null);
		TreeNode treeRow = new TreeNode(1, trunk);
		TreeNode treeSubRow = new TreeNode(2, treeRow);
		trunk.child.add(treeRow);
		treeRow.child.add(treeSubRow);

		for (int j = 0; j < firstRow.getSize(); j++) {
			treeSubRow.child.add(firstRow.get(j));
		}

		for (int i = 1; i < data.size(); i++) {
			DataRow dataRow = data.get(i);
			treeSubRow = new TreeNode(2, treeRow);
			if (keyDataSet.equals(dataRow.get(0))) {
				treeRow = new TreeNode(1, trunk);
				trunk.child.add(treeRow);
			}
			treeRow.child.add(treeSubRow);
			for (int j = 0; j < dataRow.getSize(); j++) {
				treeSubRow.child.add(dataRow.get(j));
			}
		}
		return trunk;
	}

	// class MyColumnLabelProvider extends ColumnLabelProvider {
	// private int myColNum;
	//
	// public MyColumnLabelProvider(int colNum) {
	// this.myColNum = colNum;
	// }
	//
	// @Override
	// public String getText(Object element) {
	// DataRow dataRow = null;
	// try {
	// dataRow = (DataRow) element;
	// } catch (Exception e) {
	// e.printStackTrace();
	// System.out.println("element= " + element);
	// }
	// String s = "";
	// try {
	// int size = dataRow.getColumnValues().size();
	// if (myColNum < size) {
	// s = dataRow.getColumnValues().get(myColNum);
	// }
	// } catch (Exception e) {
	// System.out.println("dataRow=" + dataRow);
	// e.printStackTrace();
	// }
	// return s;
	// }
	// }
	//



	// ==========================================
//	 TreeViewerFocusCellManager mgr = new
//	 TreeViewerFocusCellManager(treeViewer,new
//	 FocusCellOwnerDrawHighlighter(treeViewer));
//	
//	 ColumnViewerEditorActivationStrategy actSupport = new
//	 ColumnViewerEditorActivationStrategy(treeViewer)
//	 {
//	 protected boolean isEditorActivationEvent(
//	 ColumnViewerEditorActivationEvent event) {
//	 return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
//	 || event.eventType ==
//	 ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
//	 || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && (
//	 event.keyCode ==
//	 SWT.CR || event.character == ' ' ))
//	 || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
//	 }
//	 };
//	
//	// TreeViewerEditor.create(treeViewer, mgr, actSupport,
//	// ColumnViewerEditor.TABBING_HORIZONTAL
//	// | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
//	// | ColumnViewerEditor.TABBING_VERTICAL |
//	// ColumnViewerEditor.KEYBOARD_ACTIVATION);
//	
//	 // TreeViewerEditor;
//	 // thing = new ColumnLabelProvider();
//	
//	 final TextCellEditor textCellEditor = new
//	 TextCellEditor(treeViewer.getTree());
//	 final CheckboxCellEditor checkboxCellEditor = new
//	 CheckboxCellEditor(treeViewer.getTree());
//	
//	 // TreeViewerColumn column = new TreeViewerColumn(treeViewer, SWT.NONE);
//	 // column.getColumn().setWidth(200);
//	 // column.getColumn().setMoveable(true);
//	 // column.getColumn().setText("Column 1");
//	 // column.setLabelProvider(new ColumnLabelProvider() {
//	 // public String getText(Object element) {
//	 // return "Column 1 => " + element.toString();
//	 // }
//	 // });
//	 // column.setEditingSupport(new EditingSupport(treeViewer) {
//	 // protected boolean canEdit(Object element) {
//	 // return false;
//	 // }
//	
//	 protected CellEditor getCellEditor(Object element) {
//	 return textCellEditor;
//	 }
//	
//	 // protected Object getValue(Object element) {
//	 // return ((TreeNode) element).counter + "";
//	 // }
//	
//	 protected void setValue(Object element, Object value) {
//	 ((TreeNode) element).counter = Integer
//	 .parseInt(value.toString());
//	 treeViewer.update(element, null);
//	 }
//	 // });
	// ==========================================

	// EditingSupport editingSupport = new EditingSupport(ColumnViewer viewer3)
	// {
	//
	// @Override
	// protected void setValue(Object element, Object value) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// protected Object getValue(Object element) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// protected CellEditor getCellEditor(Object element) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// protected boolean canEdit(Object element) {
	// // TODO Auto-generated method stub
	// return false;
	// }
	// };
	// column.setEditingSupport(editingSupport);
	// column.setEditingSupport(new EditingSupport(viewer) {
	protected boolean canEdit(Object element) {
		return true;
	}

	// protected CellEditor getCellEditor(Object element) {
	// return textCellEditor;
	// }

	protected Object getValue(Object element) {
		return ((TreeNode) element).counter + "";
	}

	// column.setLabelProvider(new ColumnLabelProvider() {
	//
	// public String getText(Object element) {
	// return "Column 2 => " + element.toString();
	// }
	// TreeViewerColumn col = createTreeColumn(titlesArray[i],
	// boundsArray[i], i);
	//
	// col.setLabelProvider(new MyColumnLabelProvider(i));
	// columns.add(col);
	// }
	// }

	//
	// @Override
	// public void setFocus() {
	// // TODO Auto-generated method stub
	//
	//
	// }
	//
	// public void MyModel(int counter, MyModel parent) {
	// this.parent = parent;
	// this.counter = counter;
	// }
	//
	// public class MyModel {
	// public MyModel parent;
	//
	// public ArrayList child = new ArrayList();
	//
	// public int counter;
	//
	// public boolean bool;
	//
	// public MyModel(int counter, MyModel parent) {
	// this.parent = parent;
	// this.counter = counter;
	// }
	//
	// public String toString() {
	// String rv = "Item ";
	// if (parent != null) {
	// rv = parent.toString() + ".";
	// }
	//
	// rv += counter;
	//
	// return rv;
	// }
	// }

	// }

	// final TreeViewerFocusCellManager mgr = new
	// TreeViewerFocusCellManager(v,new
	// FocusCellOwnerDrawHighlighter(v));

	// final TreeViewerFocusCellManager mgr = new
	// TreeViewerFocusCellManager(treeViewer, new
	// FocusCellOwnerDrawHighlighter(treeViewer));
	// ColumnViewerEditorActivationStrategy actSupport = new
	// ColumnViewerEditorActivationStrategy(treeViewer) {
	// protected boolean
	// isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
	// return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL ||
	// event.eventType ==
	// ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
	// || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED &&
	// (event.keyCode ==
	// SWT.CR || event.character == ' '))
	// || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
	// }
	// };

	// TreeViewerEditor.create(viewer, mgr, actSupport,
	// ColumnViewerEditor.TABBING_HORIZONTAL
	// | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
	// | ColumnViewerEditor.TABBING_VERTICAL |
	// ColumnViewerEditor.KEYBOARD_ACTIVATION);

	// TreeViewerEditor;

	// final TextCellEditor textCellEditor = new
	// TextCellEditor(treeViewer.getTree());
	// final CheckboxCellEditor checkboxCellEditor = new
	// CheckboxCellEditor(treeViewer.getTree());

	// public void keyTraversed(TraverseEvent e) {
	// if ((e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail ==
	// SWT.TRAVERSE_TAB_PREVIOUS) &&
	// mgr.getFocusCell().getColumnIndex() == 2) {
	// ColumnViewerEditor editor = treeViewer.getColumnViewerEditor();
	// ViewerCell cell = mgr.getFocusCell();
	//
	// }
	// }
}

// }

// try {
// Method m = ColumnViewerEditor.class.getDeclaredMethod("processTraverseEvent",
// new Class[]
// {int.class,ViewerRow.class,TraverseEvent.class});
// m.setAccessible(true);
// m.invoke(editor, new Object[] { new Integer(cell.getColumnIndex()),
// cell.getViewerRow(), e });
// } catch (SecurityException e1) {
// // TODO Auto-generated catch block
// e1.printStackTrace();
// } catch (NoSuchMethodException e1) {
// // TODO Auto-generated catch block
// e1.printStackTrace();
// } catch (IllegalArgumentException e1) {
// // TODO Auto-generated catch block
// e1.printStackTrace();
// } catch (IllegalAccessException e1) {
// // TODO Auto-generated catch block
// e1.printStackTrace();
// } catch (InvocationTargetException e1) {
// // TODO Auto-generated catch block
// e1.printStackTrace();
// }
// }
// }
//
//
//
// // viewer.addDoubleClickListener(new IDoubleClickListener(){
// // @Override
// // public void doubleClick(DoubleClickEvent event) {
// // ISelection selection = event.getSelection();
// // System.out.println("selection=" + selection);
// // }});
// }
//
// public void setFocus() {
// viewer.getControl().setFocus();
// }
//
// public void update(List<String> data) {
// // viewer.setInput(data.toArray());
// }
//
//
// /**
// * removes columns from the given table
// *
// * @param table
// */
// private void removeColumns(Table table) {
// System.out.println(this.getClass().getName() + ".removeColumns(table)");
// table.setRedraw(false);
// while (table.getColumnCount() > 0) {
// table.getColumns()[0].dispose();
// }
// try {
// table.setRedraw(true);
// } catch (Exception e) {
// e.printStackTrace();
// }
// }
//
//
//
// column = new TreeViewerColumn(viewer, SWT.NONE);
// column.getColumn().setWidth(200);
// column.getColumn().setMoveable(true);
// column.getColumn().setText("Column 2");
// column.setLabelProvider(new ColumnLabelProvider() {
//
// public String getText(Object element) {
// return "Column 2 => " + element.toString();
// }
//
// });
// column.setEditingSupport(new EditingSupport(viewer) {
// protected boolean canEdit(Object element) {
// return true;
// }
//
// protected CellEditor getCellEditor(Object element) {
// return textCellEditor;
// }
//
// protected Object getValue(Object element) {
// return ((MyModel) element).counter + "";
// }
//
// protected void setValue(Object element, Object value) {
// ((MyModel) element).counter = Integer
// .parseInt(value.toString());
// viewer.update(element, null);
// }
// });
//
//
// viewer.setContentProvider(new MyContentProvider());
// viewer.setInput(createModel());
// viewer.getControl().addTraverseListener(new TraverseListener() {
//
// // public void keyTraversed(TraverseEvent e) {
// // if( (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail ==
// SWT.TRAVERSE_TAB_PREVIOUS) &&
// mgr.getFocusCell().getColumnIndex() == 2 ) {
// // ColumnViewerEditor editor = v.getColumnViewerEditor();
// // ViewerCell cell = mgr.getFocusCell();
// //
// // try {
// // Method m =
// ColumnViewerEditor.class.getDeclaredMethod("processTraverseEvent", new
// Class[]
// {int.class,ViewerRow.class,TraverseEvent.class});
// // m.setAccessible(true);
// // m.invoke(editor, new Object[] { new Integer(cell.getColumnIndex()),
// cell.getViewerRow(), e });
// // } catch (SecurityException e1) {
// // // TODO Auto-generated catch block
// // e1.printStackTrace();
// // } catch (NoSuchMethodException e1) {
// // // TODO Auto-generated catch block
// // e1.printStackTrace();
// // } catch (IllegalArgumentException e1) {
// // // TODO Auto-generated catch block
// // e1.printStackTrace();
// // } catch (IllegalAccessException e1) {
// // // TODO Auto-generated catch block
// // e1.printStackTrace();
// // } catch (InvocationTargetException e1) {
// // // TODO Auto-generated catch block
// // e1.printStackTrace();
// // }
// // }
// // }
// //
// });
// }
//
// private MyModel createModel() {
//
// MyModel root = new MyModel(0, null);
// root.counter = 0;
//
// MyModel tmp;
// MyModel subItem;
// for (int i = 1; i < 10; i++) {
// tmp = new MyModel(i, root);
// root.child.add(tmp);
// for (int j = 1; j < i; j++) {
// subItem = new MyModel(j, tmp);
// subItem.child.add(new MyModel(j * 100, subItem));
// tmp.child.add(subItem);
// }
// }
// return root;
// }
//
// private class MyContentProvider implements ITreeContentProvider {
//
// public Object[] getElements(Object inputElement) {
// return ((MyModel) inputElement).child.toArray();
// }
//
// public void dispose() {
// }
//
// public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
// }
//
// public Object[] getChildren(Object parentElement) {
// return getElements(parentElement);
// }
//
// public Object getParent(Object element) {
// if (element == null) {
// return null;
// }
// return ((MyModel) element).parent;
// }
//
// public boolean hasChildren(Object element) {
// return ((MyModel) element).child.size() > 0;
// }
//
// }
//
// public class MyModel {
// public MyModel parent;
//
// public ArrayList child = new ArrayList();
//
// public int counter;
//
// public boolean bool;
//
// public MyModel(int counter, MyModel parent) {
// this.parent = parent;
// this.counter = counter;
// }
//
// public String toString() {
// String rv = "Item ";
// if (parent != null) {
// rv = parent.toString() + ".";
// }
//
// rv += counter;
//
// return rv;
// }
// }
//
//
//
// /**
// * This is a callback that will allow us to create the viewer and initialize
// * it.
// */
//
// /**
// * Passing the focus request to the viewer's control.
// */
//
//
// // public void iUpdate(List<String> data) {
// // // viewer.setInput(data.toArray());
// // }
//
//
//
//
// private void createColumns(final TableViewer viewer,
// TableProvider tableProvider) {
//
// DataRow columnHeaders = new DataRow();
// columnHeaders = tableProvider.getHeaderNames();
//
// ArrayList<String> titles = new ArrayList<String>();
// ArrayList<Integer> bounds = new ArrayList<Integer>();
// for (String header : columnHeaders.getColumnValues()) {
// titles.add(header);
// bounds.add(100);
// }
// String[] titlesArray = new String[titles.size()];
// titles.toArray(titlesArray);
// int[] boundsArray = new int[bounds.size()];
// int indx = 0;
// for (Integer integer : bounds) {
// boundsArray[indx++] = integer;
// }
// for (int i = 0; i < titles.size(); i++) {
// TableViewerColumn col = createTableViewerColumn(titlesArray[i],
// boundsArray[i], i);
// col.setLabelProvider(new MyColumnLabelProvider(i));
// columns.add(col);
// }
// }
//
// private void createColumns(final TableViewer viewer,
// QueryResults queryResults) {
// DataRow columnHeaders = queryResults.getColumnHeaders();
//
// ArrayList<String> titles = new ArrayList<String>();
// ArrayList<Integer> bounds = new ArrayList<Integer>();
// for (String header : columnHeaders.getColumnValues()) {
// titles.add(header);
// bounds.add(100);
// }
// String[] titlesArray = new String[titles.size()];
// titles.toArray(titlesArray);
// int[] boundsArray = new int[bounds.size()];
// int indx = 0;
// for (Integer integer : bounds) {
// boundsArray[indx++] = integer;
// }
// for (int i = 0; i < titles.size(); i++) {
// TableViewerColumn col = createTableViewerColumn(titlesArray[i],
// boundsArray[i], i);
// col.setLabelProvider(new MyColumnLabelProvider(i));
// columns.add(col);
// }
// }
//
// /**
// * convenience method for creating a TableViewerColumn
// *
// * @param title
// * @param bound
// * @param colNumber
// * @return
// */
// private TableViewerColumn createTableViewerColumn(String title, int bound,
// final int colNumber) {
// final TableViewerColumn viewerColumn = new TableViewerColumn(viewer,
// SWT.NONE);
// final TableColumn column = viewerColumn.getColumn();
// column.setText(title);
// column.setWidth(bound);
// column.setResizable(true);
// column.setMoveable(true);
// viewerColumn
// .setEditingSupport(new CSVEdittingSupport(viewer, colNumber));
// // column.addListener(eventType, new Listener(){});
// column.addSelectionListener(new SelectionListener() {
//
// @Override
// public void widgetSelected(SelectionEvent e) {
// System.out.println(e.toString());
// System.out.println(e.getSource().getClass());
// if (e.getSource() instanceof TableColumn) {
// TableColumn col = (TableColumn) e.getSource();
// System.out.println("col: " + col.getText());
// columnSelected = col;
// }
// }
//
// @Override
// public void widgetDefaultSelected(SelectionEvent e) {
// System.out.println(e.toString());
// }
//
// });
// // Now add a MenuItem for the colum to the table menu
// // createMenuItem(headerMenu, column);
// return viewerColumn;
// }
//
// /**
// * class for generating column labels. This class will handle a variable
// * number of columns
// *
// * @author tec
// */
//
//
// public QueryResults getQueryResults() {
// return queryResults;
// }
//
// public void formatForTransform0() {
// try {
// viewer.setContentProvider(new ArrayContentProvider());
// Table table = viewer.getTable();
// // TableColumn[] tableColumn = table.getColumns();
// TableItem[] tableItems = table.getItems();
// String keyDataSet = tableItems[0].getText(0);
// int keyDataRow = 0;
// for (int i = 0; i < tableItems.length; i++) {
// TableItem tableItem = tableItems[i];
// if (keyDataSet.equals(tableItem.getText(0))) {
// keyDataRow = i;
// tableItem.setBackground(0, SWTResourceManager
// .getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
// } else {
// // if (tableItem.getText(0).equals("")) {
// // table.remove(i);
// // // i = i - 1;
// // continue;
// // }
// for (int j = 1; j < table.getColumnCount(); j++) {
// if (tableItem
// .getText(j)
// .toUpperCase()
// .equals(tableItems[keyDataRow].getText(j)
// .toUpperCase())) {
// tableItem.setBackground(j, SWTResourceManager
// .getColor(SWT.COLOR_GREEN));
// tableItems[keyDataRow].setBackground(j,
// SWTResourceManager
// .getColor(SWT.COLOR_GREEN));
// }
// // tableItem.ad
// // tableItem.addListener(SWT.MouseDown, new Listener() {
// //
// // @Override
// // public void handleEvent(Event event) {
// // System.out.println("event.item = " +
// // event.item.toString());
// // TableItem tableItem = (TableItem) event.item;
// // // int j =
// // tableItem.setBackground(0,
// // SWTResourceManager.getColor(SWT.COLOR_BLUE));
// // // tableItems[keyDataRow].setBackground(j,
// // // SWTResourceManager.getColor(SWT.COLOR_GREEN));
// // }
// //
// // });
// // DO GREAT STUFF HERE!!!
// // System.out.println("tableItem.getText(i) = " +
// // tableItem.getText(i));
// }
// }
// }
// } catch (Exception e) {
// e.printStackTrace();
// }
// }
// }
//
// }
//
// //========================================================================
// //========================================================================
//
// /*******************************************************************************
// * Copyright (c) 2006, 2010 Tom Schindl and others.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// * Tom Schindl - initial API and implementation
// *******************************************************************************/
//
//
// /**
// * Demonstrate how to work-around 3.3.1 limitation when it comes to
// TAB-Traversal and
// * checkbox editors. 3.4 will hopefully provide provide an out-of-the-box fix
// see bug 198502
// *
// * @author Tom Schindl <tom.schindl@bestsolution.at>
// *
// */
//
// // public Snippet048TreeViewerTabWithCheckboxFor3_3(final Shell shell) {
// // final TreeViewer v = new TreeViewer(shell, SWT.BORDER
// // | SWT.FULL_SELECTION);
// // v.getTree().setLinesVisible(true);
// // v.getTree().setHeaderVisible(true);
//
// // final TreeViewerFocusCellManager mgr = new
// TreeViewerFocusCellManager(v,new
// FocusCellOwnerDrawHighlighter(v));
// // ColumnViewerEditorActivationStrategy actSupport = new
// ColumnViewerEditorActivationStrategy(v)
// {
// // protected boolean isEditorActivationEvent(
// // ColumnViewerEditorActivationEvent event) {
// // return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
// // || event.eventType ==
// ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
// // || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && (
// event.keyCode ==
// SWT.CR || event.character == ' ' ))
// // || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
// // }
// // };
// //
// // TreeViewerEditor.create(v, mgr, actSupport,
// ColumnViewerEditor.TABBING_HORIZONTAL
// // | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
// // | ColumnViewerEditor.TABBING_VERTICAL |
// ColumnViewerEditor.KEYBOARD_ACTIVATION);
// //
// // final TextCellEditor textCellEditor = new TextCellEditor(v.getTree());
// // final CheckboxCellEditor checkboxCellEditor = new
// CheckboxCellEditor(v.getTree());
//
// // TreeViewerColumn column = new TreeViewerColumn(v, SWT.NONE);
// // column.getColumn().setWidth(200);
// // column.getColumn().setMoveable(true);
// // column.getColumn().setText("Column 1");
// // column.setLabelProvider(new ColumnLabelProvider() {
// //
// // public String getText(Object element) {
// // return "Column 1 => " + element.toString();
// // }
// //
// // });
// // column.setEditingSupport(new EditingSupport(v) {
// // protected boolean canEdit(Object element) {
// // return false;
// // }
// //
// // protected CellEditor getCellEditor(Object element) {
// // return textCellEditor;
// // }
// //
// // protected Object getValue(Object element) {
// // return ((MyModel) element).counter + "";
// // }
// //
// // protected void setValue(Object element, Object value) {
// // ((MyModel) element).counter = Integer
// // .parseInt(value.toString());
// // v.update(element, null);
// // }
// // });
// //
// // column = new TreeViewerColumn(v, SWT.NONE);
// // column.getColumn().setWidth(200);
// // column.getColumn().setMoveable(true);
// // column.getColumn().setText("Column 2");
// // column.setLabelProvider(new ColumnLabelProvider() {
// //
// // public String getText(Object element) {
// // return "Column 2 => " + element.toString();
// // }
// //
// // });
// // column.setEditingSupport(new EditingSupport(v) {
// // protected boolean canEdit(Object element) {
// // return true;
// // }
// //
// // protected CellEditor getCellEditor(Object element) {
// // return textCellEditor;
// // }
// //
// // protected Object getValue(Object element) {
// // return ((MyModel) element).counter + "";
// // }
// //
// // protected void setValue(Object element, Object value) {
// // ((MyModel) element).counter = Integer
// // .parseInt(value.toString());
// // v.update(element, null);
// // }
// // });
// //
// // column = new TreeViewerColumn(v, SWT.NONE);
// // column.getColumn().setWidth(200);
// // column.getColumn().setMoveable(true);
// // column.getColumn().setText("Column 3");
// // column.setLabelProvider(new ColumnLabelProvider() {
// //
// // public String getText(Object element) {
// // return ((MyModel)element).bool + "";
// // }
// //
// // });
// // column.setEditingSupport(new EditingSupport(v) {
// // protected boolean canEdit(Object element) {
// // return true;
// // }
// //
// // protected CellEditor getCellEditor(Object element) {
// // return checkboxCellEditor;
// // }
// //
// // protected Object getValue(Object element) {
// // return new Boolean(((MyModel) element).bool);
// // }
// //
// // protected void setValue(Object element, Object value) {
// // ((MyModel) element).bool = ((Boolean)value).booleanValue();
// // v.update(element, null);
// // }
// // });
// //
// // v.setContentProvider(new MyContentProvider());
// // v.setInput(createModel());
// // v.getControl().addTraverseListener(new TraverseListener() {
// //
// // public void keyTraversed(TraverseEvent e) {
// // if( (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail ==
// SWT.TRAVERSE_TAB_PREVIOUS) &&
// mgr.getFocusCell().getColumnIndex() == 2 ) {
// // ColumnViewerEditor editor = v.getColumnViewerEditor();
// // ViewerCell cell = mgr.getFocusCell();
// //
// // try {
// // Method m =
// ColumnViewerEditor.class.getDeclaredMethod("processTraverseEvent", new
// Class[]
// {int.class,ViewerRow.class,TraverseEvent.class});
// // m.setAccessible(true);
// // m.invoke(editor, new Object[] { new Integer(cell.getColumnIndex()),
// cell.getViewerRow(), e });
// // } catch (SecurityException e1) {
// // // TODO Auto-generated catch block
// // e1.printStackTrace();
// // } catch (NoSuchMethodException e1) {
// // // TODO Auto-generated catch block
// // e1.printStackTrace();
// // } catch (IllegalArgumentException e1) {
// // // TODO Auto-generated catch block
// // e1.printStackTrace();
// // } catch (IllegalAccessException e1) {
// // // TODO Auto-generated catch block
// // e1.printStackTrace();
// // } catch (InvocationTargetException e1) {
// // // TODO Auto-generated catch block
// // e1.printStackTrace();
// // }
// // }
// // }
// //
// // });
// // }
//
// private MyModel createModel() {
//
// MyModel root = new MyModel(0, null);
// root.counter = 0;
//
// MyModel tmp;
// MyModel subItem;
// for (int i = 1; i < 10; i++) {
// tmp = new MyModel(i, root);
// root.child.add(tmp);
// for (int j = 1; j < i; j++) {
// subItem = new MyModel(j, tmp);
// subItem.child.add(new MyModel(j * 100, subItem));
// tmp.child.add(subItem);
// }
// }
//
// return root;
// }
//
// // public static void fred(String[] args) {
// // Display display = new Display();
// // Shell shell = new Shell(display);
// // shell.setLayout(new FillLayout());
// // new Snippet048TreeViewerTabWithCheckboxFor3_3(shell);
// // shell.open();
// //
// // while (!shell.isDisposed()) {
// // if (!display.readAndDispatch())
// // display.sleep();
// // }
// //
// // display.dispose();
// // }
//
// private class MyContentProvider implements ITreeContentProvider {
//
// public Object[] getElements(Object inputElement) {
// return ((MyModel) inputElement).child.toArray();
// }
//
// public void dispose() {
// }
//
// public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
// }
//
// public Object[] getChildren(Object parentElement) {
// return getElements(parentElement);
// }
//
// public Object getParent(Object element) {
// if (element == null) {
// return null;
// }
// return ((MyModel) element).parent;
// }
//
// public boolean hasChildren(Object element) {
// return ((MyModel) element).child.size() > 0;
// }
//
// }
//
// public class MyModel {
// public MyModel parent;
//
// public ArrayList child = new ArrayList();
//
// public int counter;
//
// public boolean bool;
//
// public MyModel(int counter, MyModel parent) {
// this.parent = parent;
// this.counter = counter;
// }
//
// public String toString() {
// String rv = "Item ";
// if (parent != null) {
// rv = parent.toString() + ".";
// }
//
// rv += counter;
//
// return rv;
// }
//
//
//
