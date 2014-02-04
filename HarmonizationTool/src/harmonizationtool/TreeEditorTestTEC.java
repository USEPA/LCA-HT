package harmonizationtool;

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

import harmonizationtool.comands.SelectTDB;
import harmonizationtool.tree.MatchStatus;
import harmonizationtool.tree.Node;
import harmonizationtool.tree.TreeNode;
import harmonizationtool.tree.TreeNodeRow;
import harmonizationtool.tree.TreeNodeSubRow;
import harmonizationtool.vocabulary.ECO;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Demonstrate how to work-around 3.3.1 limitation when it comes to
 * TAB-Traversal and checkbox editors. 3.4 will hopefully provide provide an
 * out-of-the-box fix see bug 198502
 * 
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 * 
 */
public class TreeEditorTestTEC {

	TreeViewerColumn column2 = null;

	public TreeEditorTestTEC(final Shell shell) {
		final TreeViewer treeViewer = new TreeViewer(shell, SWT.BORDER
				| SWT.FULL_SELECTION);
		treeViewer.getTree().setLinesVisible(true);
		treeViewer.getTree().setHeaderVisible(true);
//		treeViewer.getTree().add
		Tree tree = treeViewer.getTree();
//		tree.addListener(SWT.PaintItem, paintListener);
		treeViewer.getTree().setLinesVisible(true);

		treeViewer.getTree().addListener(SWT.PaintItem, new Listener(){

			@Override
			public void handleEvent(Event event) {
				GC gc = event.gc;
				System.out.println("Paint Event: event.type="+ event.type);
				System.out.println("treeViewer.getTree().getBounds()="+treeViewer.getTree().getBounds());
				int totalWidth = treeViewer.getTree().getBounds().width;
				TreeItem[] treeItems = treeViewer.getTree().getItems();
				System.out.println("treeItems.length="+ treeItems.length);
				for(TreeItem treeItem : treeItems){
					Rectangle rectangle = treeItem.getBounds();
					
					if(treeItem.getExpanded()){
						int lineWidth = 1;
						//draw header line
						drawLine(gc, rectangle.x, rectangle.y + rectangle.height+lineWidth, totalWidth,rectangle.y + rectangle.height+lineWidth, lineWidth);
					}
					{
						//draw separator
						int lineWidth = 2;
						drawLine(gc, rectangle.x, rectangle.y +lineWidth, totalWidth,rectangle.y + lineWidth, lineWidth);
					}
					
					System.out.println("treeItem.getExpanded()="+treeItem.getExpanded());
					int numSubRows = treeItem.getItems().length;
					System.out.println("numSubRows="+numSubRows);
					for(TreeItem subItem : treeItem.getItems()){
						System.out.println("subItem.getExpanded()="+subItem.getExpanded());
						
					}
				}
			}
			
			private void drawLine(GC gc,int x1, int y1, int x2,int y2,int lineWidth){
				int lineWidthSave = gc.getLineWidth();
				gc.setLineWidth(lineWidth);
				gc.drawLine(x1, y1, x2, y2);
				gc.setLineWidth(lineWidthSave);
			}

			
		});

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

		TreeViewerColumn column1 = new TreeViewerColumn(treeViewer, SWT.NONE);
		column1.getColumn().setWidth(200);
		column1.getColumn().setMoveable(true);
		column1.getColumn().setText("DataSet");
		column1.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object treeNode) {
				return ((TreeNodeRow) treeNode).getColumnLabel(0);
			}

			@Override
			public void update(ViewerCell viewerCell) {
				super.update(viewerCell);
				ViewerRow viewerRow = viewerCell.getViewerRow();
				if (viewerRow.getElement() instanceof TreeNode) {
					Node treeNode = (Node) viewerRow.getElement();
				}
				int index = viewerCell.getVisualIndex();
				MatchStatus status = ((TreeNodeRow) viewerCell.getElement())
						.getMatchStatus(index);
//				if (status == MatchStatus.EQUIVALENT) {
//					viewerCell.setBackground(MatchStatus.EQUIVALENT.getColor());
//				}
//				if (status == MatchStatus.NONEQUIVALENT) {
//					viewerCell.setBackground(MatchStatus.NONEQUIVALENT
//							.getColor());
//				}
//				if (status == MatchStatus.UNKNOWN) {
//					viewerCell.setBackground(MatchStatus.UNKNOWN.getColor());
//				}
//				if (status == MatchStatus.BLANK) {
//					viewerCell.setBackground(MatchStatus.BLANK.getColor());
//				}
			}

		});

		column2 = new TreeViewerColumn(treeViewer, SWT.NONE);
		column2.getColumn().setWidth(200);
		column2.getColumn().setMoveable(true);
		column2.getColumn().setText("Substance Name");
		column2.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object treeNode) {
				return ((TreeNodeRow) treeNode).getColumnLabel(1);
			}

			@Override
			public void update(ViewerCell viewerCell) {
				super.update(viewerCell);
				int index = viewerCell.getVisualIndex();
				MatchStatus status = ((TreeNodeRow) viewerCell.getElement())
						.getMatchStatus(index);
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
//				if (status == MatchStatus.BLANK) {
//					viewerCell.setBackground(MatchStatus.BLANK.getColor());
//				}
			}

		});

		TreeViewerColumn column3 = new TreeViewerColumn(treeViewer, SWT.NONE);
		column3.getColumn().setWidth(200);
		column3.getColumn().setMoveable(true);
		column3.getColumn().setText("CAS");

		column3.setLabelProvider(new ColumnLabelProvider() {
			private Color currentColor = null;

			@Override
			public String getText(Object treeNode) {
				return ((TreeNodeRow) treeNode).getColumnLabel(2);
			}

			@Override
			protected void initialize(ColumnViewer viewer, ViewerColumn column) {

				super.initialize(viewer, column);
			}

			@Override
			public void update(ViewerCell viewerCell) {
				super.update(viewerCell);
				int index = viewerCell.getVisualIndex();
				MatchStatus status = ((TreeNodeRow) viewerCell.getElement())
						.getMatchStatus(index);
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
//				if (status == MatchStatus.BLANK) {
//					viewerCell.setBackground(MatchStatus.BLANK.getColor());
//				}
				
			}

		});

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
				ViewerCell viewerCell = column2.getViewer().getCell(point);
				try {
					int index = viewerCell.getVisualIndex();
					Object treeNode = viewerCell.getElement();
					if (treeNode instanceof TreeNodeSubRow) {
						TreeNodeSubRow subRow = (TreeNodeSubRow) treeNode;
						MatchStatus status = subRow.getMatchStatus(index);

						if (status == MatchStatus.EQUIVALENT) {
							subRow.updateMatchStatus(index, MatchStatus.NONEQUIVALENT);
						}
						if (status == MatchStatus.NONEQUIVALENT) {
							subRow.updateMatchStatus(index, MatchStatus.UNKNOWN);
						}
						if (status == MatchStatus.UNKNOWN) {
							subRow.updateMatchStatus(index, MatchStatus.EQUIVALENT);
						}
						treeViewer.refresh();
					}
				} catch (Exception e1) {
				}
				treeViewer.getTree().deselectAll();
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		treeViewer.setContentProvider(new MyContentProvider());
		// treeViewer.setInput(createModel());
		treeViewer.setInput(createModel());

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
						e1.printStackTrace();
					} catch (NoSuchMethodException e1) {
						e1.printStackTrace();
					} catch (IllegalArgumentException e1) {
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {
						e1.printStackTrace();
					} catch (InvocationTargetException e1) {
						e1.printStackTrace();
					}
				}
			}

		});
	}

	/**
	 * DUMMY DATA
	 * 
	 * @return
	 */
	private Node createModel() {
		TreeNode root = new TreeNode(null);
		for (int i = 1; i < 10; i++) {
			TreeNodeRow treeNodeRow = new TreeNodeRow(root);
			treeNodeRow.addColumnLabel("Master List");
			treeNodeRow.addColumnLabel("Benzene " + i);
			treeNodeRow.addColumnLabel("102-32-" + i);
			treeNodeRow.addMatchStatus(MatchStatus.UNKNOWN);
			treeNodeRow.addMatchStatus(MatchStatus.UNKNOWN);
			treeNodeRow.addMatchStatus(MatchStatus.UNKNOWN);
			if(i!=8)
			{// TRACI subrow
				TreeNodeSubRow treeNodeSubRow = new TreeNodeSubRow(treeNodeRow);
				treeNodeSubRow.addColumnLabel("TRACI");
				treeNodeSubRow.addColumnLabel("Benzene x" + i);
				treeNodeSubRow.addColumnLabel("102-32-" + i);
				treeNodeSubRow.addMatchStatus(MatchStatus.UNKNOWN);
				treeNodeSubRow.addMatchStatus(MatchStatus.NONEQUIVALENT);
				treeNodeSubRow.addMatchStatus(MatchStatus.EQUIVALENT);

			}
//			if(i!=8)
			{// ReCiPe subrow
				TreeNodeSubRow treeNodeSubRow = new TreeNodeSubRow(treeNodeRow);
				treeNodeSubRow.addColumnLabel("ReCiPe");
				treeNodeSubRow.addMatchStatus(MatchStatus.UNKNOWN);
				if(i==3){
				    treeNodeSubRow.addColumnLabel("Benzene x" + i);
				    treeNodeSubRow.addMatchStatus(MatchStatus.NONEQUIVALENT);
				}else{
					treeNodeSubRow.addColumnLabel("Benzene " + i);
					treeNodeSubRow.addMatchStatus(MatchStatus.EQUIVALENT);
				}
				treeNodeSubRow.addColumnLabel("102-32-" + i);
				treeNodeSubRow.addMatchStatus(MatchStatus.EQUIVALENT);
			}
//			TreeNodeBlankRow treeNodeBlankRow = new TreeNodeBlankRow(root);
//			treeNodeBlankRow.addMatchStatus(MatchStatus.BLANK);
//			treeNodeBlankRow.addMatchStatus(MatchStatus.BLANK);
//			treeNodeBlankRow.addMatchStatus(MatchStatus.BLANK);
//			treeNodeBlankRow.addColumnLabel("");
//			treeNodeBlankRow.addColumnLabel("");
//			treeNodeBlankRow.addColumnLabel("");
		}
		return root;
	}



	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new TreeEditorTestTEC(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}

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
			return ((Node) treeNode).getParent();
		}

		public boolean hasChildren(Object treeNode) {
			return ((Node) treeNode).getChildIterator().hasNext();
		}

	}



}