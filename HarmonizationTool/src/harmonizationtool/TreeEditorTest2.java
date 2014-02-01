//package harmonizationtool;
//
///*******************************************************************************
// * Copyright (c) 2006, 2010 Tom Schindl and others.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     Tom Schindl - initial API and implementation
// *******************************************************************************/
//
//import harmonizationtool.comands.SelectTDB;
//import harmonizationtool.vocabulary.ECO;
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//import org.apache.commons.httpclient.methods.GetMethod;
//import org.eclipse.jface.viewers.CellEditor;
//import org.eclipse.jface.viewers.CheckboxCellEditor;
//import org.eclipse.jface.viewers.ColumnLabelProvider;
//import org.eclipse.jface.viewers.ColumnViewer;
//import org.eclipse.jface.viewers.ColumnViewerEditor;
//import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
//import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
//import org.eclipse.jface.viewers.DoubleClickEvent;
//import org.eclipse.jface.viewers.EditingSupport;
//import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
//import org.eclipse.jface.viewers.ITreeContentProvider;
//import org.eclipse.jface.viewers.TextCellEditor;
//import org.eclipse.jface.viewers.TreeViewer;
//import org.eclipse.jface.viewers.TreeViewerColumn;
//import org.eclipse.jface.viewers.TreeViewerEditor;
//import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
//import org.eclipse.jface.viewers.Viewer;
//import org.eclipse.jface.viewers.ViewerCell;
//import org.eclipse.jface.viewers.ViewerColumn;
//import org.eclipse.jface.viewers.ViewerRow;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.MouseEvent;
//import org.eclipse.swt.events.MouseListener;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.events.SelectionListener;
//import org.eclipse.swt.events.TraverseEvent;
//import org.eclipse.swt.events.TraverseListener;
//import org.eclipse.swt.graphics.Color;
//import org.eclipse.swt.graphics.Device;
//import org.eclipse.swt.graphics.Point;
//import org.eclipse.swt.layout.FillLayout;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Event;
//import org.eclipse.swt.widgets.Listener;
//import org.eclipse.swt.widgets.Shell;
//
//import com.hp.hpl.jena.rdf.model.Property;
//import com.hp.hpl.jena.rdf.model.Resource;
//
///**
// * Demonstrate how to work-around 3.3.1 limitation when it comes to
// * TAB-Traversal and checkbox editors. 3.4 will hopefully provide provide an
// * out-of-the-box fix see bug 198502
// * 
// * @author Tom Schindl <tom.schindl@bestsolution.at>
// * 
// */
//<<<<<<< HEAD:HarmonizationTool/src/harmonizationtool/TreeEditorTest2.java
//public class TreeEditorTest2 {
//	private TreeNode selTreeNode = null;
//
//	public TreeEditorTest2(final Shell shell) {
//		final TreeViewer treeViewer = new TreeViewer(shell, SWT.BORDER | SWT.FULL_SELECTION);
//=======
//public class TreeEditorTest {
//	public static enum MatchStatus {
//		EQUIVALENT(1, 0, 255, 0), NONEQUIVALENT(2, 255, 0, 0), UNKNOWN(0, 0, 0,
//				255);
//		private int value;
//		private int r;
//		private int g;
//		private int b;
//
//		private MatchStatus(int value, int r, int g, int b) {
//			this.value = value;
//			this.r = r;
//			this.g = g;
//			this.b = b;
//		}
//
//		public int getValue() {
//			return value;
//		}
//
//		public Color getColor() {
//			Device device = Display.getCurrent();
//			return new Color(device, r, g, b);
//		}
//	};
//
//	TreeViewerColumn column2 = null;
//
//	public TreeEditorTest(final Shell shell) {
//		final TreeViewer treeViewer = new TreeViewer(shell, SWT.BORDER
//				| SWT.FULL_SELECTION);
//>>>>>>> 6477ca74151f550d0475a353b18dba68d43d3e0c:HarmonizationTool/src/harmonizationtool/TreeEditorTest.java
//		treeViewer.getTree().setLinesVisible(true);
//		treeViewer.getTree().setHeaderVisible(true);
//
//		final TreeViewerFocusCellManager mgr = new TreeViewerFocusCellManager(
//				treeViewer, new FocusCellOwnerDrawHighlighter(treeViewer));
//		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(
//				treeViewer) {
//			protected boolean isEditorActivationEvent(
//					ColumnViewerEditorActivationEvent event) {
//				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
//						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
//						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && (event.keyCode == SWT.CR || event.character == ' '))
//						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
//			}
//		};
//
//		TreeViewerEditor.create(treeViewer, mgr, actSupport,
//				ColumnViewerEditor.TABBING_HORIZONTAL
//						| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
//						| ColumnViewerEditor.TABBING_VERTICAL
//						| ColumnViewerEditor.KEYBOARD_ACTIVATION);
//
//		final TextCellEditor textCellEditor = new TextCellEditor(
//				treeViewer.getTree());
//		final CheckboxCellEditor checkboxCellEditor = new CheckboxCellEditor(
//				treeViewer.getTree());
//
//		TreeViewerColumn column1 = new TreeViewerColumn(treeViewer, SWT.NONE);
//		column1.getColumn().setWidth(200);
//		column1.getColumn().setMoveable(true);
//		column1.getColumn().setText("DataSet");
//		column1.setLabelProvider(new ColumnLabelProvider() {
//
//			public String getText(Object treeNode) {
//				return ((TreeNode) treeNode).colLabels.get(0);
//			}
//
//			@Override
//			public void update(ViewerCell cell) {
//				super.update(cell);
//				ViewerRow viewerRow = cell.getViewerRow();
//				if (viewerRow.getElement() instanceof TreeNode) {
//					TreeNode treeNode = (TreeNode) viewerRow.getElement();
//				}
//			}
//
//		});
//
//
//		column2 = new TreeViewerColumn(treeViewer, SWT.NONE);
//		column2.getColumn().setWidth(200);
//		column2.getColumn().setMoveable(true);
//		column2.getColumn().setText("Substance Name");
//		column2.setLabelProvider(new ColumnLabelProvider() {
//
//			public String getText(Object treeNode) {
//				return ((TreeNode) treeNode).colLabels.get(1);
//			}
//
//			@Override
//			public void update(ViewerCell viewerCell) {
//				super.update(viewerCell);
//				int index = viewerCell.getVisualIndex();
//				((TreeNode) viewerCell.getElement()).checkStatusOfChildren();
//				MatchStatus status = ((TreeNode) viewerCell.getElement()).getMatchStatus(index);
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
//			}
//
//		});
//
//		TreeViewerColumn column3 = new TreeViewerColumn(treeViewer, SWT.NONE);
//		column3.getColumn().setWidth(200);
//		column3.getColumn().setMoveable(true);
//		column3.getColumn().setText("CAS");
//
//		column3.setLabelProvider(new ColumnLabelProvider() {
//			private Color currentColor = null;
//
//			@Override
//			public String getText(Object treeNode) {
//				return ((TreeNode) treeNode).colLabels.get(2);
//			}
//
//			@Override
//			protected void initialize(ColumnViewer viewer, ViewerColumn column) {
//
//				super.initialize(viewer, column);
//			}
//
//			@Override
//			public void update(ViewerCell viewerCell) {
//				super.update(viewerCell);
//				int index = viewerCell.getVisualIndex();
//				((TreeNode) viewerCell.getElement()).checkStatusOfChildren();
//				MatchStatus status = ((TreeNode) viewerCell.getElement()).getMatchStatus(index);
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
//			}
//
//		});
//
//		treeViewer.getTree().addSelectionListener(new SelectionListener() {
//
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//			}
//
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				System.out
//						.println("widgetDefaultSelected e.getSource().toString()="
//								+ e.getSource().toString());
//				System.out.println("widgetDefaultSelected e.item=" + e.item);
//
//			}
//		});
//		treeViewer.getTree().addMouseListener(new MouseListener() {
//
//			@Override
//			public void mouseUp(MouseEvent e) {
//				Point point = new Point(e.x, e.y);
//				ViewerCell viewerCell = column2.getViewer().getCell(point);
//				try {
//					int index = viewerCell.getVisualIndex();
//					Object treeNode = viewerCell.getElement();
//					if (treeNode instanceof TreeNodeSubRow) {
//						TreeNodeSubRow subRow = (TreeNodeSubRow) treeNode;
//						MatchStatus status = subRow.matchStatus.get(index);
//
//						if (status == MatchStatus.EQUIVALENT) {
//							subRow.matchStatus.set(index,
//									MatchStatus.NONEQUIVALENT);
//						}
//						if (status == MatchStatus.NONEQUIVALENT) {
//							subRow.matchStatus.set(index, MatchStatus.UNKNOWN);
//						}
//						if (status == MatchStatus.UNKNOWN) {
//							subRow.matchStatus.set(index,
//									MatchStatus.EQUIVALENT);
//						}
//						treeViewer.refresh();
//					}
//				} catch (Exception e1) {
//				}
//				treeViewer.getTree().deselectAll();
//			}
//
//			@Override
//			public void mouseDown(MouseEvent e) {
//<<<<<<< HEAD:HarmonizationTool/src/harmonizationtool/TreeEditorTest2.java
//				System.out.println("MouseDown on selTreeNode.treeRow: " + selTreeNode.treeRow);
//				System.out.println("x = " + e.x + " . And y = " + e.y);
//			}
//
//			@Override
//			public void mouseDoubleClick(MouseEvent e) {
//=======
//>>>>>>> 6477ca74151f550d0475a353b18dba68d43d3e0c:HarmonizationTool/src/harmonizationtool/TreeEditorTest.java
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void mouseDoubleClick(MouseEvent e) {
//				// TODO Auto-generated method stub
//
//			}
//		});
//		treeViewer.setContentProvider(new MyContentProvider());
//		// treeViewer.setInput(createModel());
//		treeViewer.setInput(createModel2());
//
//		treeViewer.getControl().addTraverseListener(new TraverseListener() {
//
//			public void keyTraversed(TraverseEvent e) {
//				if ((e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS)
//						&& mgr.getFocusCell().getColumnIndex() == 2) {
//					ColumnViewerEditor editor = treeViewer
//							.getColumnViewerEditor();
//					ViewerCell cell = mgr.getFocusCell();
//
//					try {
//						Method m = ColumnViewerEditor.class.getDeclaredMethod(
//								"processTraverseEvent", new Class[] {
//										int.class, ViewerRow.class,
//										TraverseEvent.class });
//						m.setAccessible(true);
//						m.invoke(
//								editor,
//								new Object[] {
//										new Integer(cell.getColumnIndex()),
//										cell.getViewerRow(), e });
//					} catch (SecurityException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					} catch (NoSuchMethodException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					} catch (IllegalArgumentException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					} catch (IllegalAccessException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					} catch (InvocationTargetException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//				}
//			}
//
//		});
//	}
//
//	/**
//	 * DUMMY DATA
//	 * @return
//	 */
//	private TreeNode createModel2() {
//		TreeNodeRoot root = new TreeNodeRoot();
//		for (int i = 1; i < 10; i++) {
//<<<<<<< HEAD:HarmonizationTool/src/harmonizationtool/TreeEditorTest2.java
//			List<String> rowNames = new ArrayList<String>();
//			rowNames.add("Master List");
//			rowNames.add("Benzene " + i);
//			rowNames.add("102-32-" + i);
//			row = new TreeNode(root, rowNames);
//			row.treeRow = i;
//			row.treeSubRow = 0;
//			row.sourceRowNum = i;
//			root.child.add(row);
//
//			subRow = new TreeNode(row);
//			subRow.colLabels.add("TRACI");
//			subRow.colLabels.add("Benzene x" + i);
//			subRow.colLabels.add("102-32-" + i);
//			subRow.treeRow = i;
//			subRow.treeSubRow = 1;
//
//			subRow.sourceRowNum = i * 10 + 1;
//			row.child.add(subRow);
//
//			subRow = new TreeNode(row);
//			subRow.colLabels.add("ReCiPe");
//			subRow.colLabels.add("Benzene " + i);
//			subRow.colLabels.add("102-32-" + i);
//			subRow.treeRow = i;
//			subRow.treeSubRow = 2;
//			subRow.sourceRowNum = i * 10 + 2;
//			row.child.add(subRow);
//		}
//		return root;
//	}
//
//	private void colorMatchedRows(TreeViewer treeViewer) {
//		// ViewerRow newVR = new ViewerRow();
//		// newVR.setText(0, "new col 1");
//		// newVR.setText(1, "new col 2");
//		// newVR.setText(1, "new col 3");
//
//		Device device = Display.getCurrent();
//		Color red = new Color(device, 255, 0, 0);
//		Color green = new Color(device, 0, 255, 0);
//		Color blue = new Color(device, 0, 0, 255);
//		Color pink = new Color(device, 255, 150, 150);
//		TreeNode trunk = (TreeNode) treeViewer.getInput();
//		// for (TreeNode row : trunk.child) {
//		for (int i = 0; i < trunk.child.size(); i++) {
//			TreeNode row = trunk.child.get(i);
//			TreeItem treeItem = treeViewer.getTree().getItem(i);
//			treeItem.setExpanded(true);
//
//			for (int j = 0; j < row.child.size(); j++) {
//				TreeNode subRow = row.child.get(j);
//				for (int k = 0; k < subRow.colLabels.size(); k++) {
//					String queryColLabel = row.colLabels.get(k);
//					String refColLabel = subRow.colLabels.get(k);
//					System.out.println("queryColLabel = " + queryColLabel);
//					System.out.println("refColLabel = " + refColLabel);
//					if (refColLabel.equals(queryColLabel)) {
//						// int itemCount = treeItem.getItemCount();
//						// System.out.println("at row: " + i + ", subRow: " + j + ", col: " + k +
//						// ", itemCount = " + itemCount);
//						treeItem.setBackground(blue);
//						TreeItem thing = treeItem.getItem(0);
//						thing.setBackground(pink);
//						// System.out.println("at row: " + i + ", subRow: " + j + ", col: " + k +
//						// ", here's the treeItem.getItem(0): " + treeItem.getItem(0));
//						// System.out.println("at row: " + i + ", subRow: " + j + ", col: " + k +
//						// ", here's the treeItem.getItem(0).getData(): " + thing.getData());
//
//						TreeColumn treeColumnX = treeViewer.getTree().getColumn(k);
//						
////						Point point = new Point(10, 10);
////						TreeItem thing2 = treeViewer.getTree().getItem(point);
////						System.out.println("thing2 = " + thing2);
//
//						// Display thing = treeColumnX.getDisplay();
//						// Object thing = treeColumnX.getData();
//
//						System.out.println("treeColumnX.getText() = " + treeColumnX.getText());
//						// TreeItem[] thing = treeColumnX.getParent().getItems();
//						// Object thing = treeItem.getData();
//						// System.out.println("thing: " + thing);
//=======
//			TreeNodeRow treeNodeRow = new TreeNodeRow(root);
//			treeNodeRow.colLabels.add("Master List");
//			treeNodeRow.colLabels.add("Benzene " + i);
//			treeNodeRow.colLabels.add("102-32-" + i);
//			treeNodeRow.matchStatus.add(MatchStatus.UNKNOWN);
//			treeNodeRow.matchStatus.add(MatchStatus.UNKNOWN);
//			treeNodeRow.matchStatus.add(MatchStatus.UNKNOWN);
//			{// TRACI subrow
//				TreeNodeSubRow treeNodeSubRow = new TreeNodeSubRow(treeNodeRow);
//				treeNodeSubRow.colLabels.add("TRACI");
//				treeNodeSubRow.colLabels.add("Benzene x" + i);
//				treeNodeSubRow.colLabels.add("102-32-" + i);
//				treeNodeSubRow.matchStatus.add(MatchStatus.UNKNOWN);
//				treeNodeSubRow.matchStatus.add(MatchStatus.NONEQUIVALENT);
//				treeNodeSubRow.matchStatus.add(MatchStatus.EQUIVALENT);
//>>>>>>> 6477ca74151f550d0475a353b18dba68d43d3e0c:HarmonizationTool/src/harmonizationtool/TreeEditorTest.java
//
//			}
//			{// ReCiPe subrow
//				TreeNodeSubRow treeNodeSubRow = new TreeNodeSubRow(treeNodeRow);
//				treeNodeSubRow.colLabels.add("ReCiPe");
//				treeNodeSubRow.matchStatus.add(MatchStatus.UNKNOWN);
//				if(i==3){
//				    treeNodeSubRow.colLabels.add("Benzene x" + i);
//				    treeNodeSubRow.matchStatus.add(MatchStatus.NONEQUIVALENT);
//				}else{
//					treeNodeSubRow.colLabels.add("Benzene " + i);
//					treeNodeSubRow.matchStatus.add(MatchStatus.EQUIVALENT);
//				}
//				treeNodeSubRow.colLabels.add("102-32-" + i);
//				treeNodeSubRow.matchStatus.add(MatchStatus.EQUIVALENT);
//			}
//		}
//		for (TreeNode row : root.children) {
//			System.out.println(row);
//			for (TreeNode subRow : row.children) {
//				System.out.println(subRow);
//			}
//		}
//		return root;
//	}
//
//
//	public static void main(String[] args) {
//		Display display = new Display();
//		Shell shell = new Shell(display);
//		shell.setLayout(new FillLayout());
//		new TreeEditorTest2(shell);
//		shell.open();
//
//		while (!shell.isDisposed()) {
//			if (!display.readAndDispatch())
//				display.sleep();
//		}
//
//		display.dispose();
//	}
//
//	private class MyContentProvider implements ITreeContentProvider {
//
//		public Object[] getElements(Object inputElement) {
//			// return ((TreeNode) inputElement).child.toArray();
//			return ((TreeNode) inputElement).children.toArray();
//		}
//
//		public void dispose() {
//		}
//
//		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
//		}
//
//		public Object[] getChildren(Object parentElement) {
//			return getElements(parentElement);
//		}
//
//		public Object getParent(Object treeNode) {
//			if (treeNode == null) {
//				return null;
//			}
//			return ((TreeNode) treeNode).parent;
//		}
//
//		public boolean hasChildren(Object treeNode) {
//			return ((TreeNode) treeNode).children.size() > 0;
//		}
//
//	}
//
//	/**
//	 * @author tsb
//	 * 
//	 *         new TreeNode being developed to replace current TreeNode which is
//	 *         poorly designed
//	 * 
//	 */
//	private  class TreeNode {
//		public TreeNode parent;
//		public ArrayList<TreeNode> children = new ArrayList<TreeNode>();
//		public List<String> colLabels = new ArrayList<String>();
//		protected List<MatchStatus> matchStatus = new ArrayList<MatchStatus>();
//		public TreeNode getParent() {
//			return parent;
//		}
//		public void addMatchStatus(MatchStatus status){
//			matchStatus.add(status);
//		}
//		public MatchStatus getMatchStatus(int index){
//			return matchStatus.get(index);
//		}
//		public void updateMatchStatus(int index , MatchStatus status){
//			matchStatus.set(index,status);
//		}
//		public void checkStatusOfChildren(){
//		}
//	}
//
//	private class TreeNodeRoot extends TreeNode {
//		public TreeNodeRoot() {
//			parent = null;
//		}
//	}
//
//	private class TreeNodeRow extends TreeNode {
//		public TreeNodeRow(TreeNodeRoot treeNodeRoot) {
//			this.parent = treeNodeRoot;
//			this.parent.children.add(this);
//		}
//		public void checkStatusOfChildren(){
//			int index = 1;
//			matchStatus.set(index, MatchStatus.UNKNOWN);
//			for(TreeNode child : children){
//				MatchStatus childStatus = child.getMatchStatus(index);
//				if(childStatus == MatchStatus.EQUIVALENT){
//					matchStatus.set(index, MatchStatus.EQUIVALENT);
//					break;
//				}
//			}
//			index = 2;
//			matchStatus.set(index, MatchStatus.UNKNOWN);
//			for(TreeNode child : children){
//				MatchStatus childStatus = child.getMatchStatus(index);
//				if(childStatus == MatchStatus.EQUIVALENT){
//					matchStatus.set(index, MatchStatus.EQUIVALENT);
//					break;
//				}
//			}
//		}
//
//		@Override
//		public String toString() {
//			return "TreeNodeRow [parent=" + parent + ", colLabels=" + colLabels
//					+ ", matchStatus=" + matchStatus + "]";
//		}
//	}
//
//	private  class TreeNodeSubRow extends TreeNode {
//		public TreeNodeSubRow(TreeNodeRow treeNodeRow) {
//			this.parent = treeNodeRow;
//			this.parent.children.add(this);
//		}
//		public void checkStatusOfChildren(){
//		}
//		@Override
//		public String toString() {
//			return "TreeNodeSubRow [parent=" + parent + ", colLabels="
//					+ colLabels + ", matchStatus=" + matchStatus + "]";
//		}
//	}
//
//}