package harmonizationtool;

//import harmonizationtool.ResultsView.MyColumnLabelProvider;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.TableProvider;
import harmonizationtool.query.QueryResults;

//import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.eclipse.swt.widgets.TreeColumn;
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
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.jface.viewers.ViewerRow;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ResultsTreeEditor extends ViewPart {
	public static final String ID = "HarmonizationTool.ResultsTreeEditorID";
	private TreeViewer treeViewer;
	private static List<Object> columns = new ArrayList<Object>();
	// private TableColumn columnSelected = null;
	private TreeColumn columnSelected = null;

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
			System.out.println("trunk.child.size() = " +trunk.child.size());
			treeViewer.setInput(trunk); // THIS DOES NOTHING
															// !!
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
		DataRow columnHeaders = tableProvider.getHeaderNames();
		for (int i = 0; i < columnHeaders.getSize(); i++) {
			TreeViewerColumn column = new TreeViewerColumn(treeViewer, SWT.NONE);
			column.getColumn().setWidth(100);
			column.getColumn().setMoveable(true);
			column.getColumn().setText(columnHeaders.get(i));
			column.setLabelProvider(new MyColumnLabelProvider(i));
		}
	}

	public class TreeNode {
		public TreeNode parent;
		public ArrayList child = new ArrayList();

		public TreeNode(TreeNode parent) {
			this.parent = parent;
		}
	}
	public class TreeRowNode extends TreeNode{
//		public RDFNode rdfNode;
		public List<String> columnValues = null; // SHORTCUT FOR NOW

		public TreeRowNode(TreeNode parent, List<String> columnValues) {
			super(parent);
//			this.rdfNode = rdfNode;
			this.columnValues = columnValues;
		}
//		public String getLabel(){
//			Model tdbModel = rdfNode.getModel();
//			Iterator<RDFNode> iter = tdbModel.listObjectsOfProperty((Resource) rdfNode,
//                    RDFS.label);
//			RDFNode labelNode = iter.next();
//			String label = labelNode.toString();
//			return label;
//		}
	}

	@SuppressWarnings("unchecked")
	// ASK TOMMY WHAT THIS IS
	private TreeRowNode createTrunk(TableProvider tableProvider) {
		if (tableProvider == null) {
			return null;
		}

		List<DataRow> data = tableProvider.getData();
		DataRow firstRow = data.get(0);
		String keyDataSet = firstRow.get(0);
		TreeRowNode trunk = new TreeRowNode(null,null);
		TreeRowNode treeRow = new TreeRowNode(trunk, firstRow.getColumnValues());
		trunk.child.add(treeRow);
		TreeRowNode treeSubRow;
		
		for (int i = 1; i < data.size(); i++) {
			DataRow dataRow = data.get(i);
			if (keyDataSet.equals(dataRow.get(0))) {
				treeRow = new TreeRowNode(trunk, dataRow.getColumnValues());
				trunk.child.add(treeRow);
			} else {
				treeSubRow = new TreeRowNode(treeRow, dataRow.getColumnValues());
				treeRow.child.add(treeSubRow);
			}
		}
		return trunk;
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
			System.out.println("calling MyColumnLabelProvider.getText(Object element)");
			return "Test";
		}
	}
	//

	// ==========================================
	// TreeViewerFocusCellManager mgr = new
	// TreeViewerFocusCellManager(treeViewer,new
	// FocusCellOwnerDrawHighlighter(treeViewer));
	//
	// ColumnViewerEditorActivationStrategy actSupport = new
	// ColumnViewerEditorActivationStrategy(treeViewer)
	// {
	// protected boolean isEditorActivationEvent(
	// ColumnViewerEditorActivationEvent event) {
	// return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
	// || event.eventType ==
	// ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
	// || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && (
	// event.keyCode ==
	// SWT.CR || event.character == ' ' ))
	// || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
	// }
	// };
	//
	// // TreeViewerEditor.create(treeViewer, mgr, actSupport,
	// // ColumnViewerEditor.TABBING_HORIZONTAL
	// // | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
	// // | ColumnViewerEditor.TABBING_VERTICAL |
	// // ColumnViewerEditor.KEYBOARD_ACTIVATION);
	//
	// // TreeViewerEditor;
	// // thing = new ColumnLabelProvider();
	//
	// final TextCellEditor textCellEditor = new
	// TextCellEditor(treeViewer.getTree());
	// final CheckboxCellEditor checkboxCellEditor = new
	// CheckboxCellEditor(treeViewer.getTree());
	//
	// // TreeViewerColumn column = new TreeViewerColumn(treeViewer, SWT.NONE);
	// // column.getColumn().setWidth(200);
	// // column.getColumn().setMoveable(true);
	// // column.getColumn().setText("Column 1");
	// // column.setLabelProvider(new ColumnLabelProvider() {
	// // public String getText(Object element) {
	// // return "Column 1 => " + element.toString();
	// // }
	// // });
	// // column.setEditingSupport(new EditingSupport(treeViewer) {
	// // protected boolean canEdit(Object element) {
	// // return false;
	// // }
	//
	// protected CellEditor getCellEditor(Object element) {
	// return textCellEditor;
	// }
	//
	// // protected Object getValue(Object element) {
	// // return ((TreeNode) element).counter + "";
	// // }
	//
	// protected void setValue(Object element, Object value) {
	// ((TreeNode) element).counter = Integer
	// .parseInt(value.toString());
	// treeViewer.update(element, null);
	// }
	// // });
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
		return ((TreeNode) element) + "";
//		return ((TreeNode) element).counter + "";

	}

}
//
