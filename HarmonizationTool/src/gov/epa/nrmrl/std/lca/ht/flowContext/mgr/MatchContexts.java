package gov.epa.nrmrl.std.lca.ht.flowContext.mgr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.Flow;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.TreeNode;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Tree;

import com.hp.hpl.jena.rdf.model.Resource;
import org.eclipse.swt.widgets.Label;

public class MatchContexts extends ViewPart {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.flowContext.mgr.MatchContexts";
	private static Tree masterTree;
	// private static List<String> contextsToMatch;
	// private static List<Resource> contextResourcesToMatch;
	private static Composite outerComposite;
	private static Button unAssignButton;
	private static Button nextButton;
	private static Button nextUnmatchedButton;
	private static Text userDataLabel;

	private static TreeItem currentFlowContextSelection;

	// private class ContentProvider implements IStructuredContentProvider {
	// public Object[] getElements(Object inputElement) {
	// return new Object[0];
	// }
	//
	// public void dispose() {
	// }
	//
	// public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	// }
	// }

	public MatchContexts() {
		// MatchContexts = this;
	}

	public static Tree getMasterTree() {
		return masterTree;
	}

	public static void setMasterTree(Tree masterTree) {
		MatchContexts.masterTree = masterTree;
	}

	private static TreeViewer masterTreeViewer;
	// private int rowNumSelected;
	// private int colNumSelected;
	private static FlowContext contextToMatch;

	// private Composite compositeMatches;
	// private Composite compositeMaster;

	@Override
	public void createPartControl(Composite parent) {
		GridLayout gl_parent = new GridLayout(1, false);
		parent.setLayout(gl_parent);

		outerComposite = new Composite(parent, SWT.NONE);
		GridData gd_outerComposite = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_outerComposite.heightHint = 25;
		outerComposite.setLayoutData(gd_outerComposite);
		GridLayout gl_outerComposite = new GridLayout(1, false);
		gl_outerComposite.marginHeight = 0;
		gl_outerComposite.verticalSpacing = 0;
		outerComposite.setLayout(gl_outerComposite);
		// ============ NEW COL =========
		Composite innerComposite = new Composite(outerComposite, SWT.NONE);
		GridData gd_innerComposite = new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1);
		gd_innerComposite.heightHint = 25;
		innerComposite.setLayoutData(gd_innerComposite);
		GridLayout gl_innerComposite = new GridLayout(3, false);
		gl_innerComposite.marginLeft = 5;
		gl_innerComposite.marginHeight = 0;
		gl_innerComposite.verticalSpacing = 0;
		gl_innerComposite.marginWidth = 0;
		gl_innerComposite.horizontalSpacing = 15;
		innerComposite.setLayout(gl_innerComposite);

		unAssignButton = new Button(innerComposite, SWT.NONE);
		GridData gd_unAssignButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_unAssignButton.widthHint = 85;
		unAssignButton.setLayoutData(gd_unAssignButton);
		unAssignButton.setText("Unassign");
		unAssignButton.addSelectionListener(unassignListener);

		nextButton = new Button(innerComposite, SWT.NONE);
		GridData gd_assignButton = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_assignButton.widthHint = 85;
		nextButton.setLayoutData(gd_assignButton);
		nextButton.setText("Next");
		nextButton.addSelectionListener(nextListener);

		nextUnmatchedButton = new Button(innerComposite, SWT.NONE);
		GridData gridNextUnmatched = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gridNextUnmatched.widthHint = 85;
		nextUnmatchedButton.setLayoutData(gridNextUnmatched);
		nextUnmatchedButton.setText("Next Unmatched");
		nextUnmatchedButton.addSelectionListener(nextListener);

		userDataLabel = new Text(parent, SWT.MULTI);
		GridData gd_userDataLabel = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_userDataLabel.heightHint = 40;
		gd_userDataLabel.widthHint = 800;
		userDataLabel.setLayoutData(gd_userDataLabel);
		// userDataLabel.setSize(120, 14);
		userDataLabel.setText("(user data)");
		// ============ NEW COL =========
		masterTreeViewer = new TreeViewer(parent, SWT.BORDER);
		masterTree = masterTreeViewer.getTree();
		masterTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		masterTree.setLinesVisible(true);

		masterTreeViewer.setLabelProvider(new ColumnLabelProvider() {
			// private Color currentColor = null;

			// @Override
			public String getText(Object treeNode) {
				return ((TreeNode) treeNode).nodeName;
			}
		});
		TreeViewerColumn masterTreeColumn = new TreeViewerColumn(masterTreeViewer, SWT.NONE);
		masterTreeColumn.getColumn().setWidth(300);
		masterTreeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object treeNode) {
				return ((TreeNode) treeNode).nodeName;
			}
		});

		masterTreeViewer.setContentProvider(new MyContentProvider());
		masterTreeViewer.setInput(createHarmonizeCompartments());
		masterTreeViewer.getTree().addSelectionListener(new SelectionListener() {
			public void doit(SelectionEvent e) {
				assign();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				doit(e);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				doit(e);
			}
		});

		masterTreeViewer.refresh();
		for (TreeItem item : masterTree.getItems()) {
			expandItem(item);
		}
	}

	private SelectionListener unassignListener = new SelectionListener() {
		private void doit(SelectionEvent e) {
			Util.findView(CSVTableView.ID);
			Util.findView(FlowsWorkflow.ID);
			int csvRowNumSelected = CSVTableView.getRowNumSelected();
			if (csvRowNumSelected < 0) {
				return;
			}
			TableItem tableItem = CSVTableView.getTable().getItem(csvRowNumSelected);
			masterTree.deselectAll();
			if (currentFlowContextSelection != null) {
				currentFlowContextSelection.setBackground(null);
			}
			currentFlowContextSelection = null;
			String rowNumString = tableItem.getText(0);
			int rowNumber = Integer.parseInt(rowNumString) - 1;
			DataRow dataRow = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getData().get(rowNumber);
			dataRow.getFlowContext().setMatchingResource(null);
			FlowsWorkflow.removeMatchContextRowNum(contextToMatch.getFirstRow());
			CSVTableView.colorFlowContextRows();
			userDataLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
			rematchFlows();
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			doit(e);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			doit(e);
		}
	};

	private void assign() {
		Util.findView(CSVTableView.ID);
		Util.findView(FlowsWorkflow.ID);
		if (CSVTableView.getTableProviderKey() == null) {
			masterTree.deselectAll();
			return;
		}
		if (CSVTableView.preCommit) {
			masterTree.deselectAll();
			return;
		}
		int csvRowNumSelected = CSVTableView.getRowNumSelected();
		if (csvRowNumSelected < 0) {
			masterTree.deselectAll();
			return;
		}

		TableItem tableItem = CSVTableView.getTable().getItem(csvRowNumSelected);
		String rowNumString = tableItem.getText(0);
		int rowNumber = Integer.parseInt(rowNumString) - 1;
		if (CSVTableView.getRowsToIgnore().contains(rowNumber)) {
			masterTree.deselectAll();
			return;
		}
		//
		int count = masterTree.getSelectionCount();
		if (count < 1) {
			userDataLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
			currentFlowContextSelection.setBackground(null);
			return;
		}

		TreeItem selectedTreeItem = masterTree.getSelection()[0];
		TreeNode selectedTreeNode = (TreeNode) selectedTreeItem.getData();
		if (currentFlowContextSelection != null) {
			currentFlowContextSelection.setBackground(null);
		}
		currentFlowContextSelection = selectedTreeItem;
		currentFlowContextSelection.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
		contextToMatch.setMatchingResource(selectedTreeNode.uri);

		masterTree.deselectAll();

		Resource newResource = selectedTreeNode.getUri();
		if (newResource == null) {
			userDataLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
			currentFlowContextSelection.setBackground(null);
			return;
		}
		contextToMatch.setMatchingResource(newResource);
		FlowsWorkflow.addMatchContextRowNum(contextToMatch.getFirstRow());
		CSVTableView.colorFlowContextRows();
		userDataLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
		rematchFlows();
	}

	private static void rematchFlows() {
		List<Integer> flowsToReMatchRows = new ArrayList<Integer>();
		try {
			Util.showView(CSVTableView.ID);
			Util.showView(FlowsWorkflow.ID);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TableProvider tableProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey());
		for (DataRow dataRow : tableProvider.getData()) {
			FlowContext flowContext = dataRow.getFlowContext();
			if (flowContext != null) {
				if (flowContext.equals(contextToMatch)) {
					flowsToReMatchRows.add(dataRow.getRowNumber());
				}
			}
		}
		Flow.matchFlows(flowsToReMatchRows);
	}

	private SelectionListener nextListener = new SelectionListener() {
		private void doit(SelectionEvent e) {
			Object source = e.getSource();
			boolean nextUnmatched = false;
			if (source instanceof Button) {
				String buttonText = ((Button) source).getText();
				if (buttonText.matches(".*Unmatched.*")) {
					nextUnmatched = true;
				}
			}
			CSVTableView.selectNext(ID, nextUnmatched);
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			doit(e);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			doit(e);
		}
	};

	private void expandItem(TreeItem item) {
//		System.out.println("Item expanded: item.getText() " + item.getText());
		item.setExpanded(true);
		masterTreeViewer.refresh();
		for (TreeItem child : item.getItems()) {
			expandItem(child);
		}
	}

	// private void confirmModelContanisCompartments(TreeNode treeNode){
	// Model tdbModel = ActiveTDB.tdbModel;
	// if (treeNode.uri != null){
	// if (!tdbModel.containsResource(treeNode.uri)){
	// tdbModel.createResource(treeNode.uri);
	// }
	// }
	// Iterator<Node> iterator = treeNode.getChildIterator();
	// while(iterator.hasNext()){
	// Node child = iterator.next();
	// confirmModelContanisCompartments((TreeNode)child);
	// }
	// }

	// private void confirmResource(Resource uri) {
	// Model tdbModel = ActiveTDB.tdbModel;
	// if (!tdbModel.containsResource(uri)) {
	// tdbModel.createResource(uri);
	// }
	// }

	public static String getNodeNameFromResource(Resource resource) {
		StringBuilder b = new StringBuilder();
		for (TreeItem treeItem : masterTreeViewer.getTree().getItems()) {
			TreeNode treeNode = (TreeNode) treeItem.getData();
			for (TreeNode testNode : TreeNode.getAllChildNodes(treeNode)) {
				Resource nodeResource = testNode.uri;
				if (nodeResource == null) {
					continue;
				}
				if (nodeResource.equals(resource)) {
					b.append(testNode.nodeName);
					TreeNode parentNode = (TreeNode) testNode.getParent();
					while (parentNode instanceof TreeNode) {
						if (!(parentNode.getParent() instanceof TreeNode)) {
							break;
						}
						b.insert(0, "; ");
						b.insert(0, parentNode.nodeName);
						parentNode = (TreeNode) parentNode.getParent();
					}
					return b.toString();
				}
			}
		}
		return ("(flow context name not found)");
	}

	public static String[] getTwoNodeNamesFromResource(Resource resource) {
		String[] result = new String[2];
		result[0] = "";
		result[1] = "";
		if (resource == null) {
			return result;
		}
		for (TreeItem treeItem : masterTreeViewer.getTree().getItems()) {
			TreeNode treeNode = (TreeNode) treeItem.getData();
			for (TreeNode testNode : TreeNode.getAllChildNodes(treeNode)) {
				Resource nodeResource = testNode.uri;
				if (nodeResource == null) {
					continue;
				}
				if (nodeResource.equals(resource)) {
					result[1] = testNode.nodeName;
					TreeNode parentNode = (TreeNode) testNode.getParent();
					result[0] = parentNode.nodeName;
				}
			}
		}
		return result;
	}
	
	//preloaded during app startup but not meant to be cached
	private static TreeNode initialTree = null;
	
	public static void preloadHarmonizedCompartments() {
		TreeNode tree = createHarmonizeCompartments();
		initialTree = tree;
	}

	private static TreeNode createHarmonizeCompartments() {

		FlowContext.reLoadMasterFlowContexts();
		if (initialTree != null) {
			TreeNode tree = initialTree;
			initialTree = null;
			return tree;
		}
		TreeNode masterCompartmentTree = new TreeNode(null);
		String general = "";
		TreeNode tn = null;
		for (FlowContext flowContext : FlowContext.getLcaMasterContexts()) {
			String newGeneral = (String) flowContext.getOneProperty(FlowContext.flowContextGeneral);
			if (!newGeneral.equals(general)) {
				tn = new TreeNode(masterCompartmentTree);
				if (newGeneral.matches("resource")) {
					tn.nodeName = "Resource";
				} else {
					tn.nodeName = "Release to " + newGeneral;
				}
				general = newGeneral;
			}
			TreeNode tn2 = new TreeNode(tn);
			tn2.nodeName = flowContext.getSpecificString();
			tn2.uuid = (String) flowContext.getOneProperty(FlowContext.openLCAUUID);
			tn2.uri = flowContext.getTdbResource();
		}
		return masterCompartmentTree;
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

	@Override
	public void setFocus() {

	}

	// private static TreeNode getTreeNodeByURI(Resource resource) {
	// for (TreeItem treeItem1 : masterTree.getItems()) {
	// TreeNode treeNode1 = (TreeNode) treeItem1.getData();
	// if (treeNode1.getUri() != null) {
	// System.out.println("treeNode1 = " + treeNode1);
	// if (resource.equals(treeNode1.getUri())) {
	// return treeNode1;
	//
	// }
	// }
	//
	// for (TreeItem treeItem2 : treeItem1.getItems()) {
	// TreeNode treeNode2 = (TreeNode) treeItem2.getData();
	// System.out.println("treeNode2 = " + treeNode2);
	//
	// if (treeNode2.getUri() != null) {
	// if (resource.equals(treeNode2.getUri())) {
	// return treeNode2;
	// }
	// }
	// for (TreeItem treeItem3 : treeItem2.getItems()) {
	// TreeNode treeNode3 = (TreeNode) treeItem3.getData();
	// System.out.println("treeNode3 = " + treeNode3);
	//
	// if (treeNode3.getUri() != null) {
	// if (resource.equals(treeNode3.getUri())) {
	// return treeNode3;
	// }
	// }
	// }
	// }
	// }
	// return null;
	// }

	private static TreeItem getTreeItemByURI(Resource resource) {
		for (TreeItem treeItem1 : masterTree.getItems()) {
			TreeNode treeNode1 = (TreeNode) treeItem1.getData();
			if (treeNode1.getUri() != null) {
				// System.out.println("treeNode1 = " + treeNode1);
				if (resource.equals(treeNode1.getUri())) {
					return treeItem1;
				}
			}
			for (TreeItem treeItem2 : treeItem1.getItems()) {
				TreeNode treeNode2 = (TreeNode) treeItem2.getData();
				// System.out.println("treeNode2 = " + treeNode2);
				if (treeNode2.getUri() != null) {
					if (resource.equals(treeNode2.getUri())) {
						return treeItem2;
					}
				}
				for (TreeItem treeItem3 : treeItem2.getItems()) {
					TreeNode treeNode3 = (TreeNode) treeItem3.getData();
					// System.out.println("treeNode3 = " + treeNode3);
					if (treeNode3.getUri() != null) {
						if (resource.equals(treeNode3.getUri())) {
							return treeItem3;
						}
					}
				}
			}
		}
		return null;
	}

	// public List<String> getContextsToMatch() {
	// return contextsToMatch;
	// }
	//
	// public void setContextsToMatch(List<String> contexts) {
	// contextsToMatch = contexts;
	// // update();
	// }

	public class MatchModel {
		private Resource resource = null;
		private String label = "";

		public Resource getResource() {
			return resource;
		}

		public void setResource(Resource resource) {
			this.resource = resource;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}
	}

	// private MouseListener columnMouseListener = new MouseListener() {
	//
	// @Override
	// public void mouseDoubleClick(MouseEvent e) {
	// System.out.println("double click event :e =" + e);
	// }
	//
	// @Override
	// public void mouseDown(MouseEvent e) {
	// System.out.println("mouse down event :e =" + e);
	// // if (e.button == 1) {
	// // leftClick(e);
	// // } else if (e.button == 3) {
	// // // queryTbl.deselectAll();
	// // rightClick(e);
	// // }
	// }
	//
	// @Override
	// public void mouseUp(MouseEvent e) {
	// System.out.println("mouse up event :e =" + e);
	// }
	//
	// private void leftClick(MouseEvent event) {
	// System.out.println("cellSelectionMouseDownListener event " + event);
	// // Point ptLeft = new Point(1, event.y);
	// // Point ptClick = new Point(event.x, event.y);
	// // int clickedRow = 0;
	// // int clickedCol = 0;
	// // // TableItem item = queryTbl.getItem(ptLeft);
	// // // if (item == null) {
	// // // return;
	// // // }
	// // // clickedRow = queryTbl.indexOf(item);
	// // // clickedCol = getTableColumnNumFromPoint(clickedRow, ptClick);
	// // // if (clickedCol > 0) {
	// // // queryTbl.deselectAll();
	// // // return;
	// // // }
	// // // queryTbl.select(clickedRow);
	// // rowNumSelected = clickedRow;
	// // colNumSelected = clickedCol;
	// // System.out.println("rowNumSelected = " + rowNumSelected);
	// // System.out.println("colNumSelected = " + colNumSelected);
	// // rowMenu.setVisible(true);
	// }
	//
	// private void rightClick(MouseEvent event) {
	// System.out.println("cellSelectionMouseDownListener event " + event);
	// // Point ptLeft = new Point(1, event.y);
	// // Point ptClick = new Point(event.x, event.y);
	// // int clickedRow = 0;
	// // int clickedCol = 0;
	// // // TableItem item = queryTbl.getItem(ptLeft);
	// // // if (item == null) {
	// // // return;
	// // // }
	// // // clickedRow = queryTbl.indexOf(item);
	// // // clickedCol = getTableColumnNumFromPoint(clickedRow, ptClick);
	// // // int dataClickedCol = clickedCol - 1;
	// // if (clickedCol < 0) {
	// // return;
	// // }
	// //
	// // rowNumSelected = clickedRow;
	// // colNumSelected = clickedCol;
	// // System.out.println("rowNumSelected = " + rowNumSelected);
	// // System.out.println("colNumSelected = " + colNumSelected);
	// }
	// };

	// private int getTableColumnNumFromPoint(int row, Point pt) {
	// TableItem item = queryTbl.getItem(row);
	// for (int i = 0; i < queryTbl.getColumnCount(); i++) {
	// Rectangle rect = item.getBounds(i);
	// if (rect.contains(pt)) {
	// return i;
	// }
	// }
	// return -1;
	// }

	// public List<Resource> getContextResourcesToMatch() {
	// return contextResourcesToMatch;
	// }

	// public void setContextResourcesToMatch(List<Resource> contextResourcesToMatch) {
	// this.contextResourcesToMatch = contextResourcesToMatch;
	// }

	public static void update(int dataRowNumber) {
		List<DataRow> data = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getData();
		DataRow dataRow = data.get(dataRowNumber);
		contextToMatch = dataRow.getFlowContext();
		if (contextToMatch == null) {
			masterTree.deselectAll();
			if (currentFlowContextSelection != null) {
				currentFlowContextSelection.setBackground(null);
				currentFlowContextSelection = null;
			}
			setUserDataLabel("", false);
			return;
		}

		//

		String generalString = (String) contextToMatch.getOneProperty(FlowContext.flowContextGeneral);
		String specificString = contextToMatch.getSpecificString();

		int rowCount = 0;
		for (int i = contextToMatch.getFirstRow(); i < data.size(); i++) {
			FlowContext flowContextOfRow = data.get(i).getFlowContext();
			if (flowContextOfRow != null) {
				if (flowContextOfRow.equals(contextToMatch)) {
					rowCount++;
				}
			}
		}
		if (currentFlowContextSelection != null) {
			currentFlowContextSelection.setBackground(null);
		}
		String nounVerb = " flows contain";
		if (rowCount == 1) {
			nounVerb = " flow contains";
		}
		String labelString = rowCount + nounVerb + System.getProperty("line.separator");
		if (specificString == null) {
			labelString += generalString;
		} else {
			labelString += generalString + "; " + specificString;
		}

		Resource contextResource = dataRow.getFlowContext().getMatchingResource();
		if (contextResource != null) {
			TreeItem treeItem = getTreeItemByURI(contextResource);
			if (treeItem != null) {
				TreeItem parentItem = treeItem.getParentItem();
				parentItem.setExpanded(true);
				currentFlowContextSelection = treeItem;
				treeItem.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
				// masterTree.deselectAll();
				// masterTree.setSelection(getTreeItemByURI(contextResource));
				setUserDataLabel(labelString, true);

			} else {
				if (masterTree != null)
					masterTree.deselectAll();
				setUserDataLabel(labelString, false);
			}
		} else {
			if (masterTree != null)
				masterTree.deselectAll();
			setUserDataLabel(labelString, false);
		}
	}

	// public static void update() {
	// Util.findView(CSVTableView.ID);
	// if (CSVTableView.getTable().getSelectionCount() == 0) {
	// return;
	// }
	// TableItem tableItem = CSVTableView.getTable().getSelection()[0];
	// String rowNumString = tableItem.getText(0);
	// int rowNumber = Integer.parseInt(rowNumString) - 1;
	// DataRow dataRow = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getData().get(rowNumber);
	// contextToMatch = dataRow.getFlowContext();
	// if (contextToMatch == null) {
	// masterTree.deselectAll();
	// setUserDataLabel("", false);
	// } else {
	//
	// String labelString = null;
	//
	// String generalString = contextToMatch.getGeneralString();
	// String specificString = contextToMatch.getSpecificString();
	// if (specificString == null) {
	// labelString = generalString;
	// } else {
	// labelString = generalString + System.getProperty("line.separator") + "   " + specificString;
	// }
	//
	// Resource contextResource = dataRow.getFlowContext().getMatchingResource();
	// if (contextResource != null) {
	// TreeItem treeItem = getTreeItemByURI(contextResource);
	// if (treeItem != null) {
	// masterTree.setSelection(getTreeItemByURI(contextResource));
	// setUserDataLabel(labelString, true);
	//
	// } else {
	// masterTree.deselectAll();
	// setUserDataLabel(labelString, false);
	//
	// }
	// } else {
	// masterTree.deselectAll();
	// setUserDataLabel(labelString, false);
	//
	// }
	// }
	// }

	private static void setUserDataLabel(String labelString, boolean isMatched) {
		if (userDataLabel == null)
			return;
		if (labelString != null) {
			userDataLabel.setText(labelString);
		}
		if (isMatched) {
			userDataLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
		} else {
			userDataLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
		}
	}
	// public static void update(Integer dataRowNum) {
	// Util.findView(CSVTableView.ID);
	// if (CSVTableView.getTable().getSelectionCount() == 0) {
	// return;
	// }
	// // TableItem tableItem = CSVTableView.getTable().getSelection()[0];
	// // String rowNumString = tableItem.getText(0);
	// // int rowNumber = Integer.parseInt(rowNumString) - 1;
	// DataRow dataRow = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getData().get(dataRowNum);
	// contextToMatch = dataRow.getFlowContext();
	// Resource contextResource = dataRow.getFlowContext().getMatchingResource();
	// if (contextResource != null) {
	// TreeItem treeItem = getTreeItemByURI(contextResource);
	// if (treeItem != null) {
	// masterTree.setSelection(getTreeItemByURI(contextResource));
	// } else {
	// masterTree.deselectAll();
	// }
	// } else {
	// masterTree.deselectAll();
	// }
	// // for (int i = 0, n = masterTree.getColumnCount(); i < n; i++) {
	// // masterTree.getColumn(i).pack();
	// // int width = masterTree.getColumn(i).getWidth();
	// // if (width < 20) {
	// // masterTree.getColumn(i).setWidth(20);
	// // } else if (width > 400 && masterTree.getHorizontalBar().getVisible()) {
	// // masterTree.getColumn(i).setWidth(400);
	// // }
	// // }
	// }
}
