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
import harmonizationtool.vocabulary.ECO;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Demonstrate how to work-around 3.3.1 limitation when it comes to TAB-Traversal and checkbox
 * editors. 3.4 will hopefully provide provide an out-of-the-box fix see bug 198502
 * 
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 * 
 */
public class TreeEditorTest2 {
	private TreeNode selTreeNode = null;

	public TreeEditorTest2(final Shell shell) {
		final TreeViewer treeViewer = new TreeViewer(shell, SWT.BORDER | SWT.FULL_SELECTION);
		treeViewer.getTree().setLinesVisible(true);
		treeViewer.getTree().setHeaderVisible(true);

		final TreeViewerFocusCellManager mgr = new TreeViewerFocusCellManager(treeViewer, new FocusCellOwnerDrawHighlighter(treeViewer));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(treeViewer) {
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL || event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && (event.keyCode == SWT.CR || event.character == ' '))
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};

		TreeViewerEditor.create(treeViewer, mgr, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				| ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);

		final TextCellEditor textCellEditor = new TextCellEditor(treeViewer.getTree());
		final CheckboxCellEditor checkboxCellEditor = new CheckboxCellEditor(treeViewer.getTree());
		// ===================================================================================================
		TreeViewerColumn column = new TreeViewerColumn(treeViewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("DataSet");
		column.setLabelProvider(new ColumnLabelProvider() {

			// public String getText(Object treeNode) {
			// return "Col 1: " + treeNode.toString();
			// }

			public String getText(Object treeNode) {
				return ((TreeNode) treeNode).colLabels.get(0);
			}

		});

		column.setEditingSupport(new EditingSupport(treeViewer) {
			protected boolean canEdit(Object treeNode) {
				return false;
			}

			protected CellEditor getCellEditor(Object treeNode) {
				return textCellEditor;
			}

			protected Object getValue(Object treeNode) {
				return ((TreeNode) treeNode).sourceRowNum + "";
			}

			protected void setValue(Object treeNode, Object value) {
				((TreeNode) treeNode).sourceRowNum = Integer.parseInt(value.toString());
				treeViewer.update(treeNode, null);
			}
		});

		// ===================================================================================================

		column = new TreeViewerColumn(treeViewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("Substance Name");
		column.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object treeNode) {
				return ((TreeNode) treeNode).colLabels.get(1);
			}

		});
		column.setEditingSupport(new EditingSupport(treeViewer) {
			protected boolean canEdit(Object treeNode) {
				return true;
			}

			protected CellEditor getCellEditor(Object treeNode) {
				return textCellEditor;
			}

			protected Object getValue(Object treeNode) {
				return ((TreeNode) treeNode).sourceRowNum + "";
			}

			protected void setValue(Object treeNode, Object value) {
				((TreeNode) treeNode).sourceRowNum = Integer.parseInt(value.toString());
				treeViewer.update(treeNode, null);
			}
		});
		// ===================================================================================================

		column = new TreeViewerColumn(treeViewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("CAS");
		column.setLabelProvider(new ColumnLabelProvider() {
			private Color currentColor = null;

			@Override
			public String getText(Object treeNode) {
				return ((TreeNode) treeNode).colLabels.get(2);
			}

			@Override
			public void update(ViewerCell cell) {
				super.update(cell);

				// System.out.println("cell update --------------------");
				ViewerRow viewerRow = cell.getViewerRow();
				;
				// System.out.println("((TreeNode)viewerRow.getElement()).treeRow = "+((TreeNode)viewerRow.getElement()).treeRow);
				// System.out.println("((TreeNode)viewerRow.getElement()).treeSubRow = "+((TreeNode)viewerRow.getElement()).treeSubRow);
				// System.out.println("viewerRow.toString()="+viewerRow.toString());
				// System.out.println("cell.getVisualIndex()="+cell.getVisualIndex());
				// System.out.println("cell.getBounds()="+cell.getBounds().toString());
				// System.out.println("viewerRow.getBounds()="+viewerRow.getBounds().toString());
				// System.out.println("viewerRow.getItem()="+viewerRow.getItem());

				Device device = Display.getCurrent();
				Color red = new Color(device, 255, 0, 0);
				Color green = new Color(device, 0, 255, 0);
				Color blue = new Color(device, 0, 0, 255);
				if (currentColor == null) {
					currentColor = blue;
				}
				// System.out.println("currentColor="+currentColor);

				if (currentColor.equals(red)) {
					System.out.println("setting color to green");
					cell.setBackground(green);
					currentColor = green;
				} else if (currentColor.equals(green)) {
					System.out.println("setting color to blue");
					cell.setBackground(blue);
					currentColor = blue;

				} else if (currentColor.equals(blue)) {
					System.out.println("setting color to red");
					cell.setBackground(red);
					currentColor = red;
				} else {
					System.out.println("setting color to blue");
					cell.setBackground(blue);
					currentColor = blue;
				}
				treeViewer.getTree().deselectAll();
			}

		});
		column.getColumn().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("COLUMN widgetSelected e.toString()=" + e.toString());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				System.out.println("COLUMN widgetDefaultSelected e.toString()=" + e.toString());
			}
		});
		column.getColumn().addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// System.out.println("event=" + event);
				// System.out.println("event.widget=" + event.widget);
				// System.out.println("event.widget.getData()=" + event.widget.getData());
				// System.out.println("event=" + event);
				// System.out.println("event=" + event);
				// System.out.println("event=" + event);
				// System.out.println("event=" + event);
				// System.out.println("event=" + event);

			}
		});
		column.setEditingSupport(new EditingSupport(treeViewer) {

			protected boolean canEdit(Object treeNode) {
				return false;
			}

			protected CellEditor getCellEditor(Object treeNode) {
				return checkboxCellEditor;
			}

			protected Object getValue(Object treeNode) {
				return new Boolean(((TreeNode) treeNode).bool);
			}

			protected void setValue(Object treeNode, Object value) {
				((TreeNode) treeNode).bool = ((Boolean) value).booleanValue();
				treeViewer.update(treeNode, null);
			}
		});

		// ===================================================================================================

		treeViewer.getTree().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// System.out.println("widgetSelected e.getSource().toString()="
				// + e.getSource().toString());
				// System.out.println("widgetSelected e.item=" + e.item);
				// System.out.println("((TreeNode)e.item.getData()).treeRow=" +
				// ((TreeNode)e.item.getData()).treeRow);
				selTreeNode = (TreeNode) e.item.getData();
				// System.out.println("widgetSelected e.toString=" + e.toString());

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// System.out
				// .println("widgetDefaultSelected e.getSource().toString()="
				// + e.getSource().toString());
				// System.out.println("widgetDefaultSelected e.item=" + e.item);

			}
		});
		treeViewer.getTree().addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
				// System.out.println("mouse up");
				// System.out.println("e.getSource().toString()="
				// + e.getSource().toString());
			}

			@Override
			public void mouseDown(MouseEvent e) {
				System.out.println("MouseDown on selTreeNode.treeRow: " + selTreeNode.treeRow);
				System.out.println("x = " + e.x + " . And y = " + e.y);
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});
		treeViewer.getTree().addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
				System.out.println("e.character = " + e.character);
				System.out.println("e.character = " + e.character);
				colorMatchedRows(treeViewer);

				System.out.println("e.toString() = " + e.toString());
				if (e.toString().equals("s")) {
					System.out.println("selTreeNode.treeSubRow = " + selTreeNode.treeSubRow);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub

			}
		});
		treeViewer.setContentProvider(new MyContentProvider());
		treeViewer.setInput(createModel());
		treeViewer.getControl().addTraverseListener(new TraverseListener() {

			public void keyTraversed(TraverseEvent e) {
				if ((e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) && mgr.getFocusCell().getColumnIndex() == 2) {
					ColumnViewerEditor editor = treeViewer.getColumnViewerEditor();
					ViewerCell cell = mgr.getFocusCell();

					try {
						Method m = ColumnViewerEditor.class.getDeclaredMethod("processTraverseEvent", new Class[] { int.class, ViewerRow.class, TraverseEvent.class });
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

	private TreeNode createModel() {
		// List<String> rootRowNames = new ArrayList();
		// rootRowNames.add("Data Set");
		// rootRowNames.add("Substance Name");
		// rootRowNames.add("CAS");

		TreeNode root = new TreeNode(null);
		// root.colAssocResource.add((Resource) ECO.DataSource);
		// root.colAssocProperty.add((Resource) ECO.hasDataSource);
		// root.colAssocResource.add((Resource) ECO.Substance);
		// root.colAssocProperty.add((Resource) ECO.hasSubstance);
		// root.colAssocResource.add((Resource) ECO.);

		root.sourceRowNum = 0;

		TreeNode row;
		TreeNode subRow;
		for (int i = 1; i < 10; i++) {
			List<String> rowNames = new ArrayList<String>();
			rowNames.add("Master List");
			rowNames.add("Benzene " + i);
			rowNames.add("102-32-" + i);
			row = new TreeNode(root, rowNames);
			row.treeRow = i;
			row.treeSubRow = 0;
			row.sourceRowNum = i;
			root.child.add(row);

			subRow = new TreeNode(row);
			subRow.colLabels.add("TRACI");
			subRow.colLabels.add("Benzene x" + i);
			subRow.colLabels.add("102-32-" + i);
			subRow.treeRow = i;
			subRow.treeSubRow = 1;

			subRow.sourceRowNum = i * 10 + 1;
			row.child.add(subRow);

			subRow = new TreeNode(row);
			subRow.colLabels.add("ReCiPe");
			subRow.colLabels.add("Benzene " + i);
			subRow.colLabels.add("102-32-" + i);
			subRow.treeRow = i;
			subRow.treeSubRow = 2;
			subRow.sourceRowNum = i * 10 + 2;
			row.child.add(subRow);
		}
		return root;
	}

	private void colorMatchedRows(TreeViewer treeViewer) {
		// ViewerRow newVR = new ViewerRow();
		// newVR.setText(0, "new col 1");
		// newVR.setText(1, "new col 2");
		// newVR.setText(1, "new col 3");

		Device device = Display.getCurrent();
		Color red = new Color(device, 255, 0, 0);
		Color green = new Color(device, 0, 255, 0);
		Color blue = new Color(device, 0, 0, 255);
		Color pink = new Color(device, 255, 150, 150);
		TreeNode trunk = (TreeNode) treeViewer.getInput();
		// for (TreeNode row : trunk.child) {
		for (int i = 0; i < trunk.child.size(); i++) {
			TreeNode row = trunk.child.get(i);
			TreeItem treeItem = treeViewer.getTree().getItem(i);
			treeItem.setExpanded(true);

			for (int j = 0; j < row.child.size(); j++) {
				TreeNode subRow = row.child.get(j);
				for (int k = 0; k < subRow.colLabels.size(); k++) {
					String queryColLabel = row.colLabels.get(k);
					String refColLabel = subRow.colLabels.get(k);
					System.out.println("queryColLabel = " + queryColLabel);
					System.out.println("refColLabel = " + refColLabel);
					if (refColLabel.equals(queryColLabel)) {
						// int itemCount = treeItem.getItemCount();
						// System.out.println("at row: " + i + ", subRow: " + j + ", col: " + k +
						// ", itemCount = " + itemCount);
						treeItem.setBackground(blue);
						TreeItem thing = treeItem.getItem(0);
						thing.setBackground(pink);
						// System.out.println("at row: " + i + ", subRow: " + j + ", col: " + k +
						// ", here's the treeItem.getItem(0): " + treeItem.getItem(0));
						// System.out.println("at row: " + i + ", subRow: " + j + ", col: " + k +
						// ", here's the treeItem.getItem(0).getData(): " + thing.getData());

						TreeColumn treeColumnX = treeViewer.getTree().getColumn(k);
						
//						Point point = new Point(10, 10);
//						TreeItem thing2 = treeViewer.getTree().getItem(point);
//						System.out.println("thing2 = " + thing2);

						// Display thing = treeColumnX.getDisplay();
						// Object thing = treeColumnX.getData();

						System.out.println("treeColumnX.getText() = " + treeColumnX.getText());
						// TreeItem[] thing = treeColumnX.getParent().getItems();
						// Object thing = treeItem.getData();
						// System.out.println("thing: " + thing);

					}
				}
			}
		}

		for (TreeColumn column : treeViewer.getTree().getColumns()) {
			System.out.println("column.getData = " + column.getData());

		}
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new TreeEditorTest2(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}

	private class MyContentProvider implements ITreeContentProvider {

		public Object[] getElements(Object inputElement) {
			return ((TreeNode) inputElement).child.toArray();
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
			return ((TreeNode) treeNode).parent;
		}

		public boolean hasChildren(Object treeNode) {
			return ((TreeNode) treeNode).child.size() > 0;
		}

	}

	public class TreeNode {
		public TreeNode parent;

		public ArrayList<TreeNode> child = new ArrayList<TreeNode>();
		// public ArrayList<Resource> colAssocResource = new
		// ArrayList<Resource>();
		// public ArrayList<Property> colAssocProperty = new
		// ArrayList<Property>();
		// public ArrayList<Object> colContent = new ArrayList<Object>();
		public List<String> colLabels = new ArrayList<String>();
		public int treeRow;
		public int treeSubRow;

		public int sourceRowNum;

		public boolean bool;

		public TreeNode(TreeNode parent) {
			this.parent = parent;
		}

		public TreeNode(TreeNode parent, List<String> colLabels) {
			this.parent = parent;
			this.colLabels = colLabels;
		}

		// public String toString() {
		// return colLabels.get(0);
		// }

		// DON'T NEED SETTERS AND GETTERS, ALL THIS IS PUBLIC
		// public int getSourceRowNum() {
		// return sourceRowNum;
		// }
		//
		// public void setSourceRowNum(int sourceRowNum) {
		// this.sourceRowNum = sourceRowNum;
		// }
	}

}