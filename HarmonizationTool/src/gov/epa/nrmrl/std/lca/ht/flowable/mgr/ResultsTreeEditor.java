package gov.epa.nrmrl.std.lca.ht.flowable.mgr;

import harmonizationtool.ColumnLabelProvider;
import harmonizationtool.comands.SelectTDB;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.TableProvider;
import harmonizationtool.query.QueryResults;
import harmonizationtool.tree.Node;
import harmonizationtool.utils.Util;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.ETHOLD;
import harmonizationtool.vocabulary.LCAHT;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;
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

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;

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
	private TreeNode trunkDisplayed = null;

	protected TreeItem selectedItem;
	public static Listener measureListener = null;
	private boolean expansionEventOccurred = false;
	private List<TreeItem> expandedTrees = new ArrayList<TreeItem>();

	public void createPartControl(Composite parent) {

		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		treeViewer.getTree().setLinesVisible(true);
		treeViewer.getTree().setHeaderVisible(true);

		// treeViewer.getTree().setSize(200, 20);
		// parent.getShell().get
		// treeViewer.setContentProvider(new MyContentProvider());
		treeViewer.addTreeListener(new ITreeViewerListener() {

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				expansionEventOccurred = true;
			}

			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				expansionEventOccurred = true;
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
				selectedItem = (TreeItem) e.item;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		// resize tree row height just one time
		measureListener = new Listener() {
			public void handleEvent(Event event) {
				// height cannot be per row so simply set
				event.height = 18;
				treeViewer.getTree().removeListener(SWT.MeasureItem, measureListener);
			}
		};
		treeViewer.getTree().addListener(SWT.MeasureItem, measureListener);

		treeViewer.getTree().addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
				Point point = new Point(e.x, e.y);
				ViewerCell viewerCell = treeViewer.getCell(point);
				// ViewerCell viewerCell =
				// firstColumn.getViewer().getCell(point);
				try {
					int index = viewerCell.getVisualIndex();
					Object treeNode = viewerCell.getElement();
					if (treeNode instanceof TreeNodeSubRow) {
						TreeNodeSubRow subRow = (TreeNodeSubRow) treeNode;
						MatchStatus status = subRow.getMatchStatus(index);
						// System.out.println("subRow.rowSubURI.isAnon(): "
						// + subRow.rowSubURI.isAnon());
						// System.out.println("subRow.rowSubURI == null: "
						// + subRow.rowSubURI == null);

						if (status == MatchStatus.EQUIVALENT) {
							subRow.updateMatchStatus(index, MatchStatus.NONEQUIVALENT);
						}
						if (status == MatchStatus.NONEQUIVALENT) {
							subRow.updateMatchStatus(index, MatchStatus.UNKNOWN);
						}
						if (status == MatchStatus.UNKNOWN) {
							subRow.updateMatchStatus(index, MatchStatus.EQUIVALENT);
						}
						updateControlViewMatches();

						// int matchCount = 0;
						// Iterator iterator = trunk.getChildIterator();
						// TreeNodeRow statRow = null;
						// while (iterator.hasNext()) {
						// TreeNodeRow treeRow = (TreeNodeRow) iterator.next();
						// if (statRow == null) {
						// statRow = treeRow;
						// }
						// if (treeRow.getMatchStatus(1) ==
						// MatchStatus.EQUIVALENT) {
						// matchCount++;
						// }
						// }
						// ================
						// int matchCount = countMatchedRows();
						// ControlView controlView =
						// (ControlView)Util.findView(ControlView.ID);
						// if(controlView != null){
						// controlView.setMatchedRows("" + matchCount);
						// int unmatched = trunk.size()-matchCount;
						// controlView.setUnmatchedRows("" + unmatched);
						// }
						// ================
						// String was =
						// treeViewer.getTree().getColumn(1).getText();
						// String now = "substance: " + matchCount;
						// treeViewer.getTree().getColumn(1).setText(now);

						// statRow.setColumnLabel(1, "Matched Names: "
						// + matchCount);

					} else if (treeNode instanceof TreeNodeRow) {
						if (selectedItem.getExpanded()) {
							selectedItem.setExpanded(false);
							expandedTrees.remove(treeViewer.getTree().getItem(point));

						} else {
							selectedItem.setExpanded(true);
							if (!expandedTrees.contains(treeViewer.getTree().getItem(point))) {
								expandedTrees.add(treeViewer.getTree().getItem(point));
							}

						}
					}
					treeViewer.refresh();

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
				if (expansionEventOccurred) {
					updateExpandedTrees();
				}
				for (TreeItem treeItem : expandedTrees) {
					int x1 = treeItem.getBounds().x;
					int y1 = treeItem.getBounds().y;
					Rectangle rightRect = treeItem.getBounds(treeViewer.getTree().getColumnCount() - 1);
					int x2 = rightRect.x + rightRect.width;
					Rectangle bottomRect = treeItem.getItem(treeItem.getItemCount() - 1).getBounds();
					int y2 = bottomRect.y + bottomRect.height;
					Rectangle fullRowRect = new Rectangle(x1, y1, x2 - x1 - 1, y2 - y1 - 1);
					int lineWidth = 1;
					drawRect(gc, fullRowRect, lineWidth);
				}
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

	protected void updateExpandedTrees() {
		TreeItem[] treeItems = treeViewer.getTree().getItems();
		expandedTrees.clear();
		for (TreeItem treeItem : treeItems) {
			if (treeItem.getExpanded()) {
				expandedTrees.add(treeItem);
			}
		}
		expansionEventOccurred = false;
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
			expandedTrees.clear();
			treeViewer.setContentProvider(new MyContentProvider());
			removeColumns();
			createColumns(tableProvider);
			trunk = createTrunk(tableProvider);
			trunkDisplayed = trunk;
			treeViewer.setInput(trunkDisplayed);
			// treeViewer.getTree().setHeaderVisible(true);
			// treeViewer.getTree().setLinesVisible(true);

			// set TotalRows on the ControlView
			ControlView controlView = (ControlView) Util.findView(ControlView.ID);
			if (controlView != null) {
				controlView.setTotalRows("" + trunkDisplayed.size());
			}
			updateControlViewMatches();
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
			TreeViewerColumn columnSpecific = createColumn(i, columnHeaders.get(i));
			if (i == 0) {
				firstColumn = columnSpecific;
			}
		}
	}

	// ===============
	private TreeViewerColumn createColumn(final int colNum, String header) {

		TreeViewerColumn newColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
		newColumn.getColumn().setWidth(200);
		newColumn.getColumn().setMoveable(true);
		newColumn.getColumn().setText(header);
		newColumn.setLabelProvider(new ColumnLabelProvider() {
			// private Color currentColor = null;

			@Override
			public String getText(Object treeNode) {
				return ((TreeNode) treeNode).getColumnLabel(colNum);
			}

			@Override
			public void update(ViewerCell viewerCell) {
				super.update(viewerCell);
				int index = viewerCell.getVisualIndex();
				MatchStatus status = ((TreeNode) viewerCell.getElement()).getMatchStatus(index);
				// System.out.println("subRow.rowSubURI.isAnon(): "+((TreeNode)
				// viewerCell.getElement()).rowSubURI.isAnon());
				// System.out.println("subRow.rowSubURI == null: "+((TreeNode)
				// viewerCell.getElement()).rowSubURI == null);

				if (status == MatchStatus.EQUIVALENT) {
					viewerCell.setBackground(MatchStatus.EQUIVALENT.getColor());
				}
				if (status == MatchStatus.NONEQUIVALENT) {
					viewerCell.setBackground(MatchStatus.NONEQUIVALENT.getColor());
				}
				if (status == MatchStatus.UNKNOWN) {
					viewerCell.setBackground(MatchStatus.UNKNOWN.getColor());
				}
				if (viewerCell.getElement() instanceof TreeNodeSubRow) {
					viewerCell.setFont(JFaceResources.getDefaultFont());
				} else if (viewerCell.getElement() instanceof TreeNodeRow) {
					viewerCell.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));

				}
			}
		});

		return newColumn;
	}

	// ===============
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
		// treeViewer.getTree().clearAll(true);
		DataRow header = tableProvider.getHeaderNames();
		List<DataRow> data = tableProvider.getData();
		List<Resource> uriList = tableProvider.getUriList();
		DataRow firstRow = data.get(0);
		String keyDataSet = firstRow.get(0);
		// if (trunk == null) {
		trunk = new TreeNode(null);
		// }
		// new TreeNodeRow(trunk);
		TreeNodeRow treeRow = new TreeNodeRow(trunk);
		int uriCol = -1;
		for (int col = 0; col < firstRow.getSize(); col++) {
			// if (header.get(col).equals(TableProvider.SUBROW_SUB_URI)) {
			// uriCol = col;
			// } else {
			treeRow.addColumnLabel(firstRow.get(col));
			treeRow.addMatchStatus(MatchStatus.UNKNOWN);
			// }
		}
		TreeNodeSubRow treeSubRow;

		for (int i = 1; i < data.size(); i++) {
			DataRow dataRow = data.get(i);
			Resource uri = uriList.get(i);
			if (keyDataSet.equals(dataRow.get(0))) {
				treeRow = new TreeNodeRow(trunk);
				treeRow.uri = uri;
				for (int col = 0; col < dataRow.getSize(); col++) {
					// if (col == uriCol) {
					// // uriCol = col;
					// treeRow.uri = resolveUriFromString(dataRow.get(col));
					// } else {
					treeRow.addColumnLabel(dataRow.get(col));
					treeRow.addMatchStatus(MatchStatus.UNKNOWN);
					// }
				}
			} else {
				treeSubRow = new TreeNodeSubRow(treeRow);
				for (int col = 0; col < dataRow.getSize(); col++) {
					treeSubRow.addColumnLabel(dataRow.get(col));
					treeSubRow.uri = uri;

					// if (col == uriCol) {
					// // uriCol = col;
					// treeSubRow.uri = resolveUriFromString(dataRow.get(col));
					// } else {
					if (treeSubRow.getColumnLabel(col).toUpperCase().equals(treeRow.getColumnLabel(col).toUpperCase())) {
						treeSubRow.addMatchStatus(MatchStatus.EQUIVALENT);
						// treeRow.updateMatchStatus(col,
						// MatchStatus.EQUIVALENT); // WHY DOES THIS GET
						// DONE
						// AUTOMAGICALLY?
					} else {
						treeSubRow.addMatchStatus(MatchStatus.UNKNOWN);
					}
					// }
				}
			}
		}
		int matchCount = 0;
		Iterator iterator = trunk.getChildIterator();
		while (iterator.hasNext()) {
			treeRow = (TreeNodeRow) iterator.next();
			if (treeRow.getMatchStatus(1) == MatchStatus.EQUIVALENT) {
				matchCount++;
			}
		}
		String now = "substance: " + matchCount;
		treeViewer.getTree().getColumn(1).setText(now);
		treeViewer.getTree().getColumn(2).setText("same CAS: " + trunk.size());
		return trunk;

	}

	//
	// private Resource resolveUriFromString(String uriString) {
	// Resource uri = SelectTDB.model.createResource();
	// if (uriString.startsWith("http:") || uriString.startsWith("file:")) {
	// uri = SelectTDB.model.getResource(uriString);
	// } else {
	// ResIterator iterator = (SelectTDB.model.listSubjectsWithProperty(
	// RDF.type, ECO.Substance));
	// while (iterator.hasNext()) {
	// Resource resource = iterator.next();
	// if (resource.isAnon()) {
	// AnonId anonId = (AnonId) resource.getId();
	// if (uriString.equals(anonId.toString())) {
	// uri = resource;
	// }
	// }
	// }
	// }
	// return uri;
	// }

	protected boolean canEdit(Object treeNode) {
		return true;
	}

	protected Object getValue(Object treeNode) {
		return ((TreeNode) treeNode) + "";
		// return ((TreeNode) treeNode).counter + "";
	}

	public int getTotalNumberRows() throws Exception {
		return trunkDisplayed.size();
	}

	private int countMatchedRows() {
		int matchCount = 0;
		Iterator iterator = trunkDisplayed.getChildIterator();
		TreeNodeRow statRow = null;
		while (iterator.hasNext()) {
			TreeNodeRow treeRow = (TreeNodeRow) iterator.next();
			if (statRow == null) {
				statRow = treeRow;
			}
			if (treeRow.getMatchStatus(1) == MatchStatus.EQUIVALENT) {
				matchCount++;
			}
		}
		return matchCount;
	}

	// public void toggleExapandAll(){
	// if (expandedTrees.size()<(trunk.size()/2)){
	// expandAll(true);
	// }
	// else {
	// expandAll(false);
	// }
	// }

	public void expandAll(boolean expand) {
		int itemCount = treeViewer.getTree().getItemCount();
		expandedTrees.clear();
		if (expand) {
			for (int i = 0; i < itemCount; i++) {
				TreeItem treeItem = (TreeItem) treeViewer.getTree().getItem(i);
				expandedTrees.add(treeItem);
				treeItem.setExpanded(expand);
			}
		} else {
			for (int i = itemCount - 1; i >= 0; i--) {
				TreeItem treeItem = (TreeItem) treeViewer.getTree().getItem(i);
				treeItem.setExpanded(expand);
			}
		}
		treeViewer.refresh();
	}

	public void expandUnmatched(boolean expand) {
		int itemCount = treeViewer.getTree().getItemCount();
		if (expand) {
			for (int i = 0; i < itemCount; i++) {
				TreeItem treeItem = (TreeItem) treeViewer.getTree().getItem(i);
				TreeNode treeNode = (TreeNode) trunkDisplayed.get(i);
				if (!treeItem.getExpanded() && (treeNode.getMatchStatus(1) != MatchStatus.EQUIVALENT)) {
					treeItem.setExpanded(expand);
					if (!expandedTrees.contains(treeItem)) {
						expandedTrees.add(treeItem);
					}
				}
			}
		} else {
			for (int i = itemCount - 1; i >= 0; i--) {
				TreeItem treeItem = (TreeItem) treeViewer.getTree().getItem(i);
				TreeNode treeNode = (TreeNode) trunkDisplayed.get(i);
				if (treeItem.getExpanded() && (treeNode.getMatchStatus(1) != MatchStatus.EQUIVALENT)) {
					treeItem.setExpanded(expand);
					if (expandedTrees.contains(treeItem)) {
						expandedTrees.remove(treeItem);
					}
				}
			}
		}
		treeViewer.refresh();
	}

	public void hideMatched(boolean hide) {
		TreeNode tempTrunk = new TreeNode(null);
		List<Integer> expandedTreeIndexes = new ArrayList<Integer>();
		if (hide) {
			for (int i = 0; i < trunkDisplayed.size(); i++) {
				TreeItem treeItem = treeViewer.getTree().getItem(i);
				TreeNode treeNode = (TreeNode) trunkDisplayed.get(i);
				if (treeNode.getMatchStatus(1) != MatchStatus.EQUIVALENT) {
					tempTrunk.addChild(treeNode);
					if (treeItem.getExpanded()) {
						expandedTreeIndexes.add(tempTrunk.size() - 1);
					}
				}
			}
		} else {
			for (int i = 0; i < trunk.size(); i++) {
				TreeNode treeNode = (TreeNode) trunk.get(i);
				if (trunkDisplayed.contains(treeNode)) {
					tempTrunk.addChild(treeNode);
					int dispId = trunkDisplayed.getIndexOfChild(treeNode);
					TreeItem treeItem = treeViewer.getTree().getItem(dispId);
					if (treeItem.getExpanded()) {
						expandedTreeIndexes.add(tempTrunk.size() - 1);
					}
				} else {
					if (treeNode.getMatchStatus(1) == MatchStatus.EQUIVALENT) {
						tempTrunk.addChild(treeNode);
					}
				}
			}
		}
		trunkDisplayed = tempTrunk;
		treeViewer.setInput(trunkDisplayed);
		rebuildExpandedTrees(expandedTreeIndexes);
		treeViewer.refresh();
	}

	public void hideUnmatched(boolean hide) {
		TreeNode tempTrunk = new TreeNode(null);
		List<Integer> expandedTreeIndexes = new ArrayList<Integer>();
		if (hide) {
			for (int i = 0; i < trunkDisplayed.size(); i++) {
				TreeItem treeItem = treeViewer.getTree().getItem(i);
				TreeNode treeNode = (TreeNode) trunkDisplayed.get(i);
				if (treeNode.getMatchStatus(1) == MatchStatus.EQUIVALENT) {
					tempTrunk.addChild(treeNode);
					if (treeItem.getExpanded()) {
						expandedTreeIndexes.add(tempTrunk.size() - 1);
					}
				}
			}
		} else {
			for (int i = 0; i < trunk.size(); i++) {
				TreeNode treeNode = (TreeNode) trunk.get(i);
				if (trunkDisplayed.contains(treeNode)) {
					tempTrunk.addChild(treeNode);
					int dispId = trunkDisplayed.getIndexOfChild(treeNode);
					TreeItem treeItem = treeViewer.getTree().getItem(dispId);
					if (treeItem.getExpanded()) {
						expandedTreeIndexes.add(tempTrunk.size() - 1);
					}
				} else {
					if (treeNode.getMatchStatus(1) != MatchStatus.EQUIVALENT) {
						tempTrunk.addChild(treeNode);
					}
				}
			}
		}
		trunkDisplayed = tempTrunk;
		treeViewer.setInput(trunkDisplayed);
		rebuildExpandedTrees(expandedTreeIndexes);
		treeViewer.refresh();
	}

	private void rebuildExpandedTrees(List<Integer> expandedTreeIndexes) {
		expandedTrees.clear();
		for (int i : expandedTreeIndexes) {
			TreeItem treeItem = treeViewer.getTree().getItem(i);
			expandedTrees.add(treeItem);
			treeItem.setExpanded(true);
		}
	}

	// public void hideUnmatched(boolean hide) {
	// TreeNode tempTrunk = new TreeNode(null);
	// if (hide) {
	// for (int i = 0; i < trunkDisplayed.size(); i++) {
	// TreeNode treeNode = (TreeNode) trunkDisplayed.get(i);
	// if (treeNode.getMatchStatus(1) == MatchStatus.EQUIVALENT) {
	// tempTrunk.addChild(treeNode);
	// } else if (expandedTrees.contains(treeNode)) {
	// expandedTrees.remove(treeNode); // FIXME check here and above
	// }
	// }
	// } else {
	// for (int i = 0; i < trunk.size(); i++) {
	// TreeNode treeNode = (TreeNode) trunk.get(i);
	// if (trunkDisplayed.contains(treeNode)) {
	// tempTrunk.addChild(treeNode);
	// } else {
	// if (treeNode.getMatchStatus(1) != MatchStatus.EQUIVALENT) {
	// tempTrunk.addChild(treeNode);
	// }
	// }
	// }
	// }
	// trunkDisplayed = tempTrunk;
	// treeViewer.setInput(trunkDisplayed);
	// treeViewer.refresh();
	// }
	//
	public void expandMatched(boolean expand) {
		int itemCount = treeViewer.getTree().getItemCount();
		if (expand) {
			for (int i = 0; i < itemCount; i++) {
				TreeItem treeItem = (TreeItem) treeViewer.getTree().getItem(i);
				TreeNode treeNode = (TreeNode) trunkDisplayed.get(i);
				if (!treeItem.getExpanded() && (treeNode.getMatchStatus(1) == MatchStatus.EQUIVALENT)) {
					treeItem.setExpanded(expand);
					if (!expandedTrees.contains(treeItem)) {
						expandedTrees.add(treeItem);
					}
				}
			}
		} else {
			for (int i = itemCount - 1; i >= 0; i--) {
				TreeItem treeItem = (TreeItem) treeViewer.getTree().getItem(i);
				TreeNode treeNode = (TreeNode) trunkDisplayed.get(i);
				if (treeItem.getExpanded() && (treeNode.getMatchStatus(1) == MatchStatus.EQUIVALENT)) {
					treeItem.setExpanded(expand);
					if (expandedTrees.contains(treeItem)) {
						expandedTrees.remove(treeItem);
					}
				}
			}
		}
		treeViewer.refresh();
	}

	private void updateControlViewMatches(int matchCount) {
		ControlView controlView = (ControlView) Util.findView(ControlView.ID);
		if (controlView != null) {
			controlView.setMatchedRows("" + matchCount);
			int unmatched = trunkDisplayed.size() - matchCount;
			controlView.setUnmatchedRows("" + unmatched);
		}
	}

	private void updateControlViewMatches() {
		ControlView controlView = (ControlView) Util.findView(ControlView.ID);
		int matchCount = countMatchedRows();
		if (controlView != null) {
			controlView.setMatchedRows("" + matchCount);
			int unmatched = trunkDisplayed.size() - matchCount;
			controlView.setUnmatchedRows("" + unmatched);
		}
	}

	public void commitMatches() {
		Model model = SelectTDB.model;
		Resource annotationResource = model.createResource();
		model.add(annotationResource, RDF.type, ETHOLD.Annotation);
		// 2) Assign to it a date and creator
		Date calendar = new Date();
		Literal dateLiteral = model.createTypedLiteral(calendar);
		model.add(annotationResource, DCTerms.dateSubmitted, dateLiteral);
		if (Util.getPreferenceStore().getString("userName") != null) {
			Literal userName = model.createLiteral(Util.getPreferenceStore().getString("userName"));
			model.add(annotationResource, DCTerms.creator, userName);
		}

		Iterator<Node> iterator = trunk.getChildIterator();
		while (iterator.hasNext()) {
			TreeNodeRow treeNodeRow = (TreeNodeRow) iterator.next();
			Iterator<Node> childIterator = treeNodeRow.getChildIterator();
			while (childIterator.hasNext()) {
				TreeNodeSubRow treeNodeSubRow = (TreeNodeSubRow) childIterator.next();
				Resource comparison = null;
				System.out.println("got here");
				if (treeNodeSubRow.getMatchStatus(1).equals(MatchStatus.EQUIVALENT)) {
					comparison = addComparison(treeNodeRow.uri, treeNodeSubRow.uri, ETHOLD.equivalent);
					System.out.println("  eq");


				} else if (treeNodeSubRow.getMatchStatus(1).equals(MatchStatus.NONEQUIVALENT)) {
					comparison = addComparison(treeNodeRow.uri, treeNodeSubRow.uri, ETHOLD.nonequivalent);
					System.out.println("  non-eq");

				}
				if (comparison != null) {
					model.add(annotationResource, ETHOLD.hasComparison, comparison);
					System.out.println("  Annotation adding a Comparison");

				}
			}
		}
	}

	private Resource addComparison(Resource querySource, Resource master, Resource equivalence) {
		Model model = SelectTDB.model;
		if (querySource == null || master == null) {
			return null;
		}
		Resource comparisonResource = model.createResource();
		model.add(comparisonResource, RDF.type, ETHOLD.Comparison);
		model.add(comparisonResource, ETHOLD.comparedSource, querySource);
		model.add(comparisonResource, ETHOLD.comparedMaster, master);
		model.add(comparisonResource, ETHOLD.comparedEquivalence, equivalence);
		return comparisonResource;
	}

}
