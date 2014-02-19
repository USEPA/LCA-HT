package gov.epa.nrmrl.std.lca.ht.compartment.mgr;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;


//import gov.epa.nrmrl.std.lca.ht.flowable.mgr.TreeNode;
import harmonizationtool.ColumnLabelProvider;
import harmonizationtool.tree.Node;

public class CopyOfHarmonizeCompartments extends ViewPart {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.compartment.mgr.HarmonizeCompartmentsID";

	public TreeNode masterCompartmentTree = new TreeNode(null);
	private TreeViewer treeViewer;

	public void createPartControl(Composite parent) {

		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		treeViewer.getTree().setLinesVisible(true);
		treeViewer.getTree().setHeaderVisible(true);

		// treeViewer.getTree().setSize(200, 20);
		// parent.getShell().get
		treeViewer.setContentProvider(new MyContentProvider());
		treeViewer.addTreeListener(new ITreeViewerListener() {

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
//				expansionEventOccurred = true;
			}

			@Override
			public void treeExpanded(TreeExpansionEvent event) {
//				expansionEventOccurred = true;
			}

		});
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

		treeViewer.getTree().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
//				selectedItem = (TreeItem) e.item;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		// resize tree row height just one time
//		measureListener = new Listener() {
//			public void handleEvent(Event event) {
//				// height cannot be per row so simply set
//				event.height = 18;
//				treeViewer.getTree().removeListener(SWT.MeasureItem, measureListener);
//			}
//		};
//		treeViewer.getTree().addListener(SWT.MeasureItem, measureListener);

		treeViewer.getTree().addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
				Point point = new Point(e.x, e.y);
				ViewerCell viewerCell = treeViewer.getCell(point);
				// ViewerCell viewerCell = firstColumn.getViewer().getCell(point);
				try {
					int index = viewerCell.getVisualIndex();
					Object treeNode = viewerCell.getElement();
//					if (treeNode instanceof TreeNodeSubRow) {
//						TreeNodeSubRow subRow = (TreeNodeSubRow) treeNode;
//						MatchStatus status = subRow.getMatchStatus(index);
//						// System.out.println("subRow.rowSubURI.isAnon(): "
//						// + subRow.rowSubURI.isAnon());
//						// System.out.println("subRow.rowSubURI == null: "
//						// + subRow.rowSubURI == null);
//
//						if (status == MatchStatus.EQUIVALENT) {
//							subRow.updateMatchStatus(index, MatchStatus.NONEQUIVALENT);
//						}
//						if (status == MatchStatus.NONEQUIVALENT) {
//							subRow.updateMatchStatus(index, MatchStatus.UNKNOWN);
//						}
//						if (status == MatchStatus.UNKNOWN) {
//							subRow.updateMatchStatus(index, MatchStatus.EQUIVALENT);
//						}
//						updateControlViewMatches();
//
//						// int matchCount = 0;
//						// Iterator iterator = trunk.getChildIterator();
//						// TreeNodeRow statRow = null;
//						// while (iterator.hasNext()) {
//						// TreeNodeRow treeRow = (TreeNodeRow) iterator.next();
//						// if (statRow == null) {
//						// statRow = treeRow;
//						// }
//						// if (treeRow.getMatchStatus(1) == MatchStatus.EQUIVALENT) {
//						// matchCount++;
//						// }
//						// }
//						// ================
//						// int matchCount = countMatchedRows();
//						// ControlView controlView = (ControlView)Util.findView(ControlView.ID);
//						// if(controlView != null){
//						// controlView.setMatchedRows("" + matchCount);
//						// int unmatched = trunk.size()-matchCount;
//						// controlView.setUnmatchedRows("" + unmatched);
//						// }
//						// ================
//						// String was = treeViewer.getTree().getColumn(1).getText();
//						// String now = "substance: " + matchCount;
//						// treeViewer.getTree().getColumn(1).setText(now);
//
//						// statRow.setColumnLabel(1, "Matched Names: "
//						// + matchCount);
//
//					} else if (treeNode instanceof TreeNodeRow) {
//						if (selectedItem.getExpanded()) {
//							selectedItem.setExpanded(false);
//							expandedTrees.remove(treeViewer.getTree().getItem(point));
//
//						} else {
//							selectedItem.setExpanded(true);
//							if (!expandedTrees.contains(treeViewer.getTree().getItem(point))) {
//								expandedTrees.add(treeViewer.getTree().getItem(point));
//							}
//
//						}
//					}
//					treeViewer.refresh();

					//
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

		// treeViewer.setLabelProvider(new TreeLabelProvider());
		treeViewer.getTree().addListener(SWT.PaintItem, new Listener() {

			@Override
			public void handleEvent(Event event) {
				GC gc = event.gc;
//				if (expansionEventOccurred) {
//					updateExpandedTrees();
//				}
//				for (TreeItem treeItem : expandedTrees) {
//					int x1 = treeItem.getBounds().x;
//					int y1 = treeItem.getBounds().y;
//					Rectangle rightRect = treeItem.getBounds(treeViewer.getTree().getColumnCount() - 1);
//					int x2 = rightRect.x + rightRect.width;
//					Rectangle bottomRect = treeItem.getItem(treeItem.getItemCount() - 1).getBounds();
//					int y2 = bottomRect.y + bottomRect.height;
//					Rectangle fullRowRect = new Rectangle(x1, y1, x2 - x1 - 1, y2 - y1 - 1);
//					int lineWidth = 1;
//					drawRect(gc, fullRowRect, lineWidth);
//				}
			}

			// private void drawRect(GC gc, int x1, int y1, int x2, int y2,
			// int lineWidth) {
			// int lineWidthSave = gc.getLineWidth();
			// gc.setLineWidth(lineWidth);
			// gc.drawRectangle(x1, y1, x2 - x1, y2 - y1);
			// gc.setLineWidth(lineWidthSave);
			// }
			private void drawRect(GC gc, Rectangle rectangle, int lineWidth) {
				int lineWidthSave = gc.getLineWidth();
				gc.setLineWidth(lineWidth);
				gc.drawRectangle(rectangle);
				gc.setLineWidth(lineWidthSave);
			}

		});
	}
	
	private class MyContentProvider implements ITreeContentProvider {

		public Object[] getElements(Object inputElement) {
			Iterator<Node> iter = ((TreeNode) inputElement).getChildIterator();
			List<Node> l = new ArrayList<Node>();
			while (iter.hasNext()) {
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

//	public HarmonizeCompartments(){
//		TreeNode release = new TreeNode(masterCompartmentTree);
//		release.nodeName = "Release";
//
//		TreeNode air = new TreeNode(release);
//		air.nodeName = "air";
//		TreeNode lowPop = new TreeNode(air);
//		lowPop.nodeName = "low population density";
//		TreeNode airUnspec = new TreeNode(air);
//		airUnspec.nodeName = "unspecified";
//		TreeNode airHighPop = new TreeNode(air);
//		airHighPop.nodeName = "high population density";
//		TreeNode airLowPopLongTerm = new TreeNode(air);
//		airLowPopLongTerm.nodeName = "low population density, long-term";
//		TreeNode airLowerStratPlusUpperTrop = new TreeNode(air);
//		airLowerStratPlusUpperTrop.nodeName = "lower stratosphere + upper troposphere";
//		
//		TreeNode water = new TreeNode(release);
//		water.nodeName = "water";
//		TreeNode waterFossil = new TreeNode(water);
//		waterFossil.nodeName = "fossil-";
//		TreeNode waterFresh = new TreeNode(water);
//		waterFresh.nodeName = "fresh-";
//		TreeNode waterFreshLongTerm = new TreeNode(water);
//		waterFreshLongTerm.nodeName = "fresh-, long-term";
//		TreeNode waterGround = new TreeNode(water);
//		waterGround.nodeName = "ground-";
//		TreeNode waterGroundLongTerm = new TreeNode(water);
//		waterGroundLongTerm.nodeName = "ground-, long-term";
//		TreeNode waterLake = new TreeNode(water);
//		waterLake.nodeName = "lake";
//		TreeNode waterOcean = new TreeNode(water);
//		waterOcean.nodeName = "ocean";
//		TreeNode waterRiver = new TreeNode(water);
//		waterRiver.nodeName = "river";		
//		TreeNode waterRiverLongTerm = new TreeNode(water);
//		waterRiverLongTerm.nodeName = "river, long-term";
//		TreeNode waterSurface = new TreeNode(water);
//		waterSurface.nodeName = "surface water";
//		TreeNode waterUnspec = new TreeNode(water);
//		waterUnspec.nodeName = "unspecified";
//
//		TreeNode soil = new TreeNode(release);
//		soil.nodeName = "soil";
//		TreeNode soilAgricultural = new TreeNode(soil);
//		soilAgricultural.nodeName = "agricultural";
//		TreeNode soilForestry = new TreeNode(soil);
//		soilForestry.nodeName = "forestry";
//		TreeNode soilIndustrial = new TreeNode(soil);
//		soilIndustrial.nodeName = "industrial";
//		TreeNode soilUnspec = new TreeNode(soil);
//		soilUnspec.nodeName = "unspecified";
//		
//		TreeNode resource = new TreeNode(masterCompartmentTree);
//		resource.nodeName = "Resource";
//		
//		TreeNode resourceBiotic = new TreeNode(resource);
//		resourceBiotic.nodeName = "biotic";
//		TreeNode resourceInAir = new TreeNode(resource);
//		resourceInAir.nodeName = "in air";	
//		TreeNode resourceInGround = new TreeNode(resource);
//		resourceInGround.nodeName = "in ground";	
//		TreeNode resourceInLand = new TreeNode(resource);
//		resourceInLand.nodeName = "in land";	
//		TreeNode resourceInWater = new TreeNode(resource);
//		resourceInWater.nodeName = "in water";	
//		TreeNode resourceUnspec = new TreeNode(resource);
//		resourceUnspec.nodeName = "unspecified";
//	}
//
//	private TreeViewerColumn createColumn() {
//
//		TreeViewerColumn newColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
//		newColumn.getColumn().setWidth(300);
//		newColumn.getColumn().setMoveable(true);
//		newColumn.getColumn().setText("Compartments");
//		newColumn.setLabelProvider(new ColumnLabelProvider() {
//			// private Color currentColor = null;
//
////			@Override
//			public String getText(Object treeNode) {
//				return ((TreeNode) treeNode).nodeName;
//			}
//
////			@Override
////			public void update(ViewerCell viewerCell) {
////				super.update(viewerCell);
////				int index = viewerCell.getVisualIndex();
////				MatchStatus status = ((TreeNode) viewerCell.getElement()).getMatchStatus(index);
////				// System.out.println("subRow.rowSubURI.isAnon(): "+((TreeNode)
////				// viewerCell.getElement()).rowSubURI.isAnon());
////				// System.out.println("subRow.rowSubURI == null: "+((TreeNode)
////				// viewerCell.getElement()).rowSubURI == null);
////
////				if (status == MatchStatus.EQUIVALENT) {
////					viewerCell.setBackground(MatchStatus.EQUIVALENT.getColor());
////				}
////				if (status == MatchStatus.NONEQUIVALENT) {
////					viewerCell.setBackground(MatchStatus.NONEQUIVALENT.getColor());
////				}
////				if (status == MatchStatus.UNKNOWN) {
////					viewerCell.setBackground(MatchStatus.UNKNOWN.getColor());
////				}
////				if (viewerCell.getElement() instanceof TreeNodeSubRow) {
////					viewerCell.setFont(JFaceResources.getDefaultFont());
////				} else if (viewerCell.getElement() instanceof TreeNodeRow) {
////					viewerCell.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
////
////				}
////			}
//		});
//
//		return newColumn;
//	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
}
