package harmonizationtool;

import harmonizationtool.comands.SelectTDB;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.TableProvider;
import harmonizationtool.query.QueryResults;
import harmonizationtool.tree.MatchStatus;
import harmonizationtool.tree.Node;
import harmonizationtool.tree.TreeNode;
import harmonizationtool.tree.TreeNodeRow;
import harmonizationtool.tree.TreeNodeSubRow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.ViewerRow;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class ResultsTreeEditor extends ViewPart {
	public static final String ID = "HarmonizationTool.ResultsTreeEditorID";
	private TreeViewer treeViewer;

	// private static List<Object> columns = new ArrayList<Object>();
	// private TableColumn columnSelected = null;
	// private TreeColumn columnSelected = null;

	// private QueryResults queryResults = null;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */

	TreeViewerColumn firstColumn = null;
	private TreeNode trunk = null;

	public void createPartControl(Composite parent) {
		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		treeViewer.getTree().setLinesVisible(true);
		treeViewer.getTree().setHeaderVisible(true);
//		parent.getShell().get
		// treeViewer.setContentProvider(new MyContentProvider());

		final TreeViewerFocusCellManager mgr = new TreeViewerFocusCellManager(
				treeViewer, new FocusCellOwnerDrawHighlighter(treeViewer));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(
				treeViewer) {
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && (event.keyCode == SWT.CR || event.character == ' '))
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};

		TreeViewerEditor.create(treeViewer, mgr, actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL
						| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
						| ColumnViewerEditor.TABBING_VERTICAL
						| ColumnViewerEditor.KEYBOARD_ACTIVATION);

		final TextCellEditor textCellEditor = new TextCellEditor(
				treeViewer.getTree());
		final CheckboxCellEditor checkboxCellEditor = new CheckboxCellEditor(
				treeViewer.getTree());

		treeViewer.getTree().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				System.out
						.println("widgetDefaultSelected e.getSource().toString()="
								+ e.getSource().toString());
				System.out.println("widgetDefaultSelected e.item=" + e.item);

			}
		});
		treeViewer.getTree().addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
				Point point = new Point(e.x, e.y);
				ViewerCell viewerCell = firstColumn.getViewer().getCell(point);
				try {
					int index = viewerCell.getVisualIndex();
					Object treeNode = viewerCell.getElement();
					if (treeNode instanceof TreeNodeSubRow) {
						TreeNodeSubRow subRow = (TreeNodeSubRow) treeNode;
						MatchStatus status = subRow.getMatchStatus(index);
//						System.out.println("subRow.rowSubURI.isAnon(): "
//								+ subRow.rowSubURI.isAnon());
//						System.out.println("subRow.rowSubURI == null: "
//								+ subRow.rowSubURI == null);

						if (status == MatchStatus.EQUIVALENT) {
							subRow.updateMatchStatus(index,
									MatchStatus.NONEQUIVALENT);
						}
						if (status == MatchStatus.NONEQUIVALENT) {
							subRow.updateMatchStatus(index, MatchStatus.UNKNOWN);
						}
						if (status == MatchStatus.UNKNOWN) {
							subRow.updateMatchStatus(index,
									MatchStatus.EQUIVALENT);
						}
						treeViewer.refresh();
					}
				} catch (Exception e1) {
				}
				treeViewer.getTree().deselectAll();
			}

			@Override
			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});
		treeViewer.setContentProvider(new MyContentProvider());
		// treeViewer.setInput(createModel());
		// treeViewer.setInput(createModel2());

		treeViewer.getControl().addTraverseListener(new TraverseListener() {

			public void keyTraversed(TraverseEvent e) {
				if ((e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS)
						&& mgr.getFocusCell().getColumnIndex() == 2) {
					ColumnViewerEditor editor = treeViewer
							.getColumnViewerEditor();
					ViewerCell cell = mgr.getFocusCell();

					try {
						Method m = ColumnViewerEditor.class.getDeclaredMethod(
								"processTraverseEvent", new Class[] {
										int.class, ViewerRow.class,
										TraverseEvent.class });
						m.setAccessible(true);
						m.invoke(
								editor,
								new Object[] {
										new Integer(cell.getColumnIndex()),
										cell.getViewerRow(), e });
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

		// treeViewer.setLabelProvider(new TreeLabelProvider());
		treeViewer.getTree().addListener(SWT.PaintItem, new Listener(){

			@Override
			public void handleEvent(Event event) {
				GC gc = event.gc;
//				System.out.println("Paint Event: event.type="+ event.type);
//				System.out.println("treeViewer.getTree().getBounds()="+treeViewer.getTree().getBounds());
				int totalWidth = treeViewer.getTree().getBounds().width;		
//				ScrollBar scrollBar = treeViewer.getTree().getVerticalBar();
//				System.out.println("scrollBar.getThumb() "+scrollBar.getThumb());
//				System.out.println("scrollBar.getMinimum() "+scrollBar.getMinimum());
//				System.out.println("scrollBar.getMaximum() "+scrollBar.getMaximum());
				
//				System.out.println("treeViewer.getTree().getBounds().height "+treeViewer.getTree().getBounds().height);
//				System.out.println("scrollBar.getThumbBounds().y "+scrollBar.getThumbBounds().y);
				
//				System.out.println("scrollBar.getSelection() "+scrollBar.getSelection());

				// show = if (rectangle.y > (sel/(max-min) - margin) AND
				//            rectangle.y < (sel+thumb)/(max-min) + margin
				// 
//				int margin = 20;
//				int topOfScroll = scrollBar.getThumbBounds().y;
//				int viewerHeight = treeViewer.getTree().getBounds().height;		

//				height = 1000;
//				System.out.println("height = "+ topOfScroll);
//				float minCompare = topOfScroll*(scrollBar.getSelection()/(scrollBar.getMaximum() - scrollBar.getMinimum())) - margin;
//				float maxCompare = topOfScroll*((scrollBar.getSelection()+scrollBar.getThumb())/(scrollBar.getMaximum() - scrollBar.getMinimum())) + margin;
				
//				System.out.println("minCompare = "+minCompare);
//				System.out.println("maxCompare = "+maxCompare);

//				System.out.println("treeViewer.getTree().getBounds().height = "+treeViewer.getTree().getBounds().height);
				TreeItem[] treeItems = treeViewer.getTree().getItems();
//				System.out.println("treeItems.length="+ treeItems.length);
				for(TreeItem treeItem : treeItems){
					Rectangle rectangle = treeItem.getBounds();

					if(treeItem.getExpanded()){
//					if((topOfScroll-30) < rectangle.y && rectangle.y < (topOfScroll+viewerHeight+30)){

						int lineWidth = 1;
						//draw header line
						drawLine(gc, rectangle.x, rectangle.y + rectangle.height+lineWidth, totalWidth,rectangle.y + rectangle.height+lineWidth, lineWidth);
//						System.out.println("y for this box: "+rectangle.y);
						//draw separator
						lineWidth = 2;
						drawLine(gc, rectangle.x, rectangle.y +lineWidth, totalWidth,rectangle.y + lineWidth, lineWidth);
						try {
							// draw bottom separator
							int numSubRows = treeItem.getItems().length;
							TreeItem subItem = treeItem.getItems()[numSubRows-1];
							Rectangle subRectangle = subItem.getBounds();
							drawLine(gc, subRectangle.x, subRectangle.y +subRectangle.height - lineWidth, totalWidth,subRectangle.y +subRectangle.height - lineWidth, lineWidth);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
//					System.out.println("treeItem.getExpanded()="+treeItem.getExpanded());
//					int numSubRows = treeItem.getItems().length;
//					System.out.println("numSubRows="+numSubRows);
//					for(TreeItem subItem : treeItem.getItems()){
//						System.out.println("subItem.getExpanded()="+subItem.getExpanded());
//						
//					}
				}
			}
			
			private void drawLine(GC gc,int x1, int y1, int x2,int y2,int lineWidth){
				int lineWidthSave = gc.getLineWidth();
				gc.setLineWidth(lineWidth);
				gc.drawLine(x1, y1, x2, y2);
				gc.setLineWidth(lineWidthSave);
			}

			
		});

		
	}
	

	private void removeColumns() {
		treeViewer.getTree().setRedraw(false);
		while (treeViewer.getTree().getColumns().length > 0) {
			treeViewer.getTree().getColumns()[0].dispose();
		}
		try {
			treeViewer.getTree().setRedraw(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			treeViewer.setContentProvider(new MyContentProvider());
			// final Tree tree = treeViewer.getTree();
			// tree.removeAll();
			removeColumns();
			createColumns(tableProvider);
//			if (trunk != null) {
////				treeViewer.remove(trunk);
//				treeViewer.getTree().dispose();
//			}
			trunk  = createTrunk(tableProvider); // TODO: WORK HERE
			treeViewer.setInput(trunk);
			treeViewer.getTree().setHeaderVisible(true);
			treeViewer.getTree().setLinesVisible(true);
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
			TreeViewerColumn columnSpecific = createColumn(i,
					columnHeaders.get(i));
			if (i == 0) {
				firstColumn = columnSpecific;
			}
		}
	}

	// ===============
	private TreeViewerColumn createColumn(final int colNum, String header) {

		TreeViewerColumn newColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
		newColumn.getColumn().setWidth(100);
		newColumn.getColumn().setMoveable(true);
		newColumn.getColumn().setText(header);
		newColumn.setLabelProvider(new ColumnLabelProvider() {
			// private Color currentColor = null;

			@Override
			public String getText(Object treeNode) {
				return ((TreeNode) treeNode).getColumnLabel(colNum);
			}

			@Override
			protected void initialize(ColumnViewer viewer, ViewerColumn column) {

				super.initialize(viewer, column);
			}

			@Override
			public void update(ViewerCell viewerCell) {
				super.update(viewerCell);
				int index = viewerCell.getVisualIndex();
				MatchStatus status = ((TreeNode) viewerCell.getElement())
						.getMatchStatus(index);
				// System.out.println("subRow.rowSubURI.isAnon(): "+((TreeNode)
				// viewerCell.getElement()).rowSubURI.isAnon());
				// System.out.println("subRow.rowSubURI == null: "+((TreeNode)
				// viewerCell.getElement()).rowSubURI == null);

				if (status == MatchStatus.EQUIVALENT) {
					viewerCell.setBackground(MatchStatus.EQUIVALENT.getColor());
				}
				if (status == MatchStatus.NONEQUIVALENT) {
					viewerCell.setBackground(MatchStatus.NONEQUIVALENT
							.getColor());
				}
				if (status == MatchStatus.UNKNOWN) {
					viewerCell.setBackground(MatchStatus.UNKNOWN.getColor());
				}
			}

		});

		return newColumn;
	}

	// ===============
	private class MyContentProvider implements ITreeContentProvider {

		public Object[] getElements(Object inputElement) {
			Iterator<Node>  iter = ((TreeNode) inputElement).getChildIterator();
			List<Node> l = new ArrayList<Node>();
			while(iter.hasNext()){
				l.add(iter.next());
			}
			return l.toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		public Object getParent(Object treeNode) {
			if (treeNode == null) {
				return null;
			}
			return ((TreeNode) treeNode).getParent();
		}

		public boolean hasChildren(Object treeNode) {
			return ((Node) treeNode).getChildIterator().hasNext();
		}

	}

	
	// private TreeNodeRoot createTrunk(ResultSetRewindable resultSetRewindable)
	// {
	// if (resultSetRewindable == null) {
	// return null;
	// }
	// TableProvider tableProvider =
	// TableProvider.createTransform0(resultSetRewindable);
	// resultSetRewindable.reset();
	//
	// List<DataRow> data = tableProvider.getData();
	// DataRow firstRow = data.get(0);
	// QuerySolution resultSetRow = resultSetRewindable.next();
	// String keyDataSet = firstRow.get(0);
	// // THE ROW BELOW WOULD DO THE SAME THING
	// // String keyDataSet =
	// //
	// resultSetRow.get(TableProvider.SUBROW_PREFIX+"1_"+TableProvider.SUBROW_NAMEHEADER).asLiteral().getString();
	//
	// TreeNodeRoot trunk = new TreeNodeRoot();
	// TreeNodeRow treeRow = new TreeNodeRow(trunk);
	// int subRowIndex = 1;
	// treeRow.rowSubURI = resultSetRow.get(TableProvider.SUBROW_PREFIX +
	// subRowIndex + "_" + TableProvider.SUBROW_SUB_URI).asResource();
	//
	// int sourceTableRow = -1;
	// if (resultSetRow.get(TableProvider.SUBROW_SOURCE_TAB_ROW) != null) {
	// sourceTableRow = resultSetRow.get(TableProvider.SUBROW_PREFIX +
	// subRowIndex + "_" +
	// TableProvider.SUBROW_SOURCE_TAB_ROW).asLiteral().getInt();
	// }
	// treeRow.sourceTableRow = sourceTableRow;
	//
	// for (int col = 0; col < firstRow.getSize(); col++) {
	// treeRow.colLabels.add(firstRow.get(col));
	// treeRow.addMatchStatus(MatchStatus.UNKNOWN);
	// }
	// // trunk.children.add(treeRow);
	// // trunk.child.add(treeRow);
	// TreeNodeSubRow treeSubRow;
	//
	// for (int i = 1; i < data.size(); i++) {
	// DataRow dataRow = data.get(i);
	// resultSetRow = resultSetRewindable.next();
	// Resource rowURI =
	// resultSetRow.get(TableProvider.SUBROW_SUB_URI).asResource();
	// sourceTableRow = -1;
	// if (resultSetRow.get(TableProvider.SUBROW_SOURCE_TAB_ROW) != null) {
	// sourceTableRow =
	// resultSetRow.get(TableProvider.SUBROW_SOURCE_TAB_ROW).asLiteral().getInt();
	// }
	//
	// if (keyDataSet.equals(dataRow.get(0))) {
	// subRowIndex = 1;
	// treeRow = new TreeNodeRow(trunk);
	// treeRow.rowSubURI = resultSetRow.get(TableProvider.SUBROW_PREFIX +
	// subRowIndex + "_" + TableProvider.SUBROW_SUB_URI).asResource();
	// treeRow.sourceTableRow = sourceTableRow;
	// for (int col = 0; col < dataRow.getSize(); col++) {
	// treeRow.colLabels.add(dataRow.get(col));
	// treeRow.addMatchStatus(MatchStatus.UNKNOWN);
	// }
	// } else {
	// subRowIndex++;
	// treeSubRow = new TreeNodeSubRow(treeRow);
	// treeSubRow.rowSubURI = resultSetRow.get(TableProvider.SUBROW_PREFIX +
	// subRowIndex + "_" + TableProvider.SUBROW_SUB_URI).asResource();
	// treeSubRow.sourceTableRow = sourceTableRow;
	//
	// for (int col = 0; col < dataRow.getSize(); col++) {
	// treeSubRow.colLabels.add(dataRow.get(col));
	// if
	// (treeSubRow.colLabels.get(col).toUpperCase().equals(treeRow.colLabels.get(col).toUpperCase()))
	// {
	// treeSubRow.addMatchStatus(MatchStatus.EQUIVALENT);
	// treeRow.addMatchStatus(MatchStatus.EQUIVALENT);
	// } else {
	// treeSubRow.addMatchStatus(MatchStatus.UNKNOWN);
	// }
	// }
	// }
	// }
	// return trunk;
	// }

	private TreeNode createTrunk(TableProvider tableProvider) {
		if (tableProvider == null) {
			return null;
		}
//		treeViewer.getTree().clearAll(true);

		List<DataRow> data = tableProvider.getData();
		DataRow firstRow = data.get(0);
		String keyDataSet = firstRow.get(0);
//		 if (trunk == null) {
			trunk = new TreeNode(null);
//		}
		TreeNodeRow treeRow = new TreeNodeRow(trunk);
		for (int col = 0; col < firstRow.getSize(); col++) {
			treeRow.addColumnLabel(firstRow.get(col));
			treeRow.addMatchStatus(MatchStatus.UNKNOWN);
		}
		TreeNodeSubRow treeSubRow;

		for (int i = 1; i < data.size(); i++) {
			DataRow dataRow = data.get(i);
			if (keyDataSet.equals(dataRow.get(0))) {
				treeRow = new TreeNodeRow(trunk);
				for (int col = 0; col < dataRow.getSize(); col++) {
					treeRow.addColumnLabel(dataRow.get(col));
					treeRow.addMatchStatus(MatchStatus.UNKNOWN);
				}
			} else {
				treeSubRow = new TreeNodeSubRow(treeRow);
				for (int col = 0; col < dataRow.getSize(); col++) {
					treeSubRow.addColumnLabel(dataRow.get(col));
					if (treeSubRow.getColumnLabel(col).toUpperCase()
							.equals(treeRow.getColumnLabel(col).toUpperCase())) {
						treeSubRow.addMatchStatus(MatchStatus.EQUIVALENT);
						treeRow.addMatchStatus(MatchStatus.EQUIVALENT);
					} else {
						treeSubRow.addMatchStatus(MatchStatus.UNKNOWN);
					}
				}
			}
		}
		return trunk;

	}

	protected boolean canEdit(Object treeNode) {
		return true;
	}

	protected Object getValue(Object treeNode) {
		return ((TreeNode) treeNode) + "";
		// return ((TreeNode) treeNode).counter + "";
	}
	

}
