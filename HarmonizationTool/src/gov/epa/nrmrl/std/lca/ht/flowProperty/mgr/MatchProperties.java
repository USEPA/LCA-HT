package gov.epa.nrmrl.std.lca.ht.flowProperty.mgr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
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
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.Node;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.TreeNode;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Tree;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

public class MatchProperties extends ViewPart {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.MatchProperties";
	private List<String> propertiesToMatch;
	private List<Resource> propertyResourcesToMatch;
	private static Tree masterTree;
	private static TreeViewer masterTreeViewer;
	private static Text userDataLabel;
	// private static int rowNumSelected;
	// private static int colNumSelected;
	private static FlowUnit unitToMatch;
	// private static TreeItem currentFlowPropertySelection;
	private static TreeItem currentFlowUnitSelection;
	private static Font defaultFont = null;
	private static Font boldFont = null;

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

	public MatchProperties() {
		// MatchContexts = this;
	}

	// private Composite compositeMatches;
	// private Composite compositeMaster;

	@Override
	public void createPartControl(Composite parent) {
		GridLayout gl_parent = new GridLayout(1, false);
		parent.setLayout(gl_parent);

		outerComposite = new Composite(parent, SWT.NONE);
		GridData gd_outerComposite = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gd_outerComposite.heightHint = 25;
		outerComposite.setLayoutData(gd_outerComposite);
		GridLayout gl_outerComposite = new GridLayout(1, false);
		gl_outerComposite.verticalSpacing = 0;
		gl_outerComposite.marginHeight = 0;
		outerComposite.setLayout(gl_outerComposite);
		// ============ NEW COL =========
		Composite innerComposite = new Composite(outerComposite, SWT.NONE);
		GridData gd_innerComposite = new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1);
		gd_innerComposite.heightHint = 25;
		innerComposite.setLayoutData(gd_innerComposite);
		GridLayout gl_innerComposite = new GridLayout(3, false);
		gl_innerComposite.horizontalSpacing = 15;
		gl_innerComposite.marginHeight = 0;
		gl_innerComposite.marginLeft = 5;
		gl_innerComposite.marginWidth = 0;
		gl_innerComposite.verticalSpacing = 0;
		innerComposite.setLayout(gl_innerComposite);

		unAssignButton = new Button(innerComposite, SWT.NONE);
		GridData gd_unAssignButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_unAssignButton.heightHint = 20;
		gd_unAssignButton.widthHint = 85;
		unAssignButton.setLayoutData(gd_unAssignButton);
		unAssignButton.setText("Unassign");
		unAssignButton.addSelectionListener(unassignListener);

		nextButton = new Button(innerComposite, SWT.NONE);
		GridData gd_assignButton = new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1);
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
		gd_userDataLabel.widthHint = 600;
		userDataLabel.setLayoutData(gd_userDataLabel);
		// userDataLabel.setSize(120, 14);
		userDataLabel.setText("(user data)");
		// ============ NEW COL =========
		masterTreeViewer = new TreeViewer(parent, SWT.BORDER);
		masterTree = masterTreeViewer.getTree();
		masterTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		masterTree.setLinesVisible(true);
		ColumnViewerToolTipSupport.enableFor(masterTreeViewer);

		// masterTreeViewer.setLabelProvider(new ColumnLabelProvider() {
		// // private Color currentColor = null;
		//
		// // @Override
		// public String getText(Object treeNode) {
		// return ((TreeNode) treeNode).nodeName;
		// }
		//
		// // @Override
		// public Font getFont(Object treeNode) {
		// TreeNode castAsTreeNode = (TreeNode) treeNode;
		// if (defaultFont == null){
		// createFonts(masterTree.getItem(0));
		// }
		// if (castAsTreeNode.isReference()){
		// return boldFont;
		// }
		// return defaultFont;
		// }
		//
		// });
		// TODO - thinking about something like this below so that reference tree nodes can be bold and have a tooltip
		// masterTreeViewer.setLabelProvider(new CellLabelProvider() {
		// // private Color currentColor = null;
		//
		// // @Override
		// public String getText(Object treeNode) {
		// return ((TreeNode) treeNode).nodeName;
		// }
		//
		// // @Override
		// public Font getFont(Object treeNode) {
		// TreeNode castAsTreeNode = (TreeNode) treeNode;
		// if (defaultFont == null){
		// createFonts(masterTree.getItem(0));
		// }
		// if (castAsTreeNode.isReference()){
		// return boldFont;
		// }
		// return defaultFont;
		// }
		//
		// @Override
		// public void update(ViewerCell cell) {
		// TreeItem treeItem = (TreeItem) cell.getElement();
		// TreeNode treeNode = (TreeNode) treeItem.getData();
		// System.out.println("TreeItem: "+treeNode);
		//
		//
		// }
		//
		// });

		TreeViewerColumn masterTreeColumn = new TreeViewerColumn(masterTreeViewer, SWT.NONE);
		masterTreeColumn.getColumn().setWidth(300);
		// masterTreeColumn.setLabelProvider(new ColumnLabelProvider() {
		// @Override
		// public String getText(Object treeNode) {
		// return ((TreeNode) treeNode).nodeName;
		// }
		// });
		masterTreeColumn.setLabelProvider(new CellLabelProvider() {

			@Override
			public String getToolTipText(Object element) {
				TreeNode treeNode = (TreeNode) element;
				String referenceUnitStatus = "";
				if (!treeNode.isReference) {
					referenceUnitStatus = treeNode.conversionFactor + " conversion factor meaning 1 "
							+ treeNode.nodeName + " = " + treeNode.conversionFactor + " "
							+ treeNode.referenceUnit + " (Reference Unit)";
				} else {
					referenceUnitStatus = "Reference Unit";
				}
				if (treeNode.referenceDescription != null) {
					return treeNode.referenceDescription + System.getProperty("line.separator") + referenceUnitStatus;
				}
				return referenceUnitStatus;
			}

			@Override
			public Point getToolTipShift(Object object) {
				return new Point(15, 15);
			}

			@Override
			public int getToolTipDisplayDelayTime(Object object) {
				return 100;
			}

			@Override
			public int getToolTipTimeDisplayed(Object object) {
				return 15000;
			}

			// @Override
			// public void update(ViewerCell cell) {
			// cell.setText(cell.getElement().toString());
			//
			// }

			// private Color currentColor = null;

			// @Override
			// public String getText(Object treeNode) {
			// return ((TreeNode) treeNode).nodeName;
			// }
			//
			// // @Override
			// public Font getFont(Object treeNode) {
			// TreeNode castAsTreeNode = (TreeNode) treeNode;
			// if (defaultFont == null){
			// createFonts(masterTree.getItem(0));
			// }
			// if (castAsTreeNode.isReference()){
			// return boldFont;
			// }
			// return defaultFont;
			// }

			@Override
			public void update(ViewerCell cell) {
				if (cell.getText().equals("")) {
					TreeNode treeNode = (TreeNode) cell.getElement();
					cell.setText(treeNode.nodeName);
					if (defaultFont == null) {
						createFonts(masterTree.getItem(0));
					}
					if (treeNode.isReference) {
						cell.setFont(boldFont);
					}
				}
				// TreeNode castAsTreeNode = (TreeNode) treeNode;
				// if (defaultFont == null){
				// createFonts(masterTree.getItem(0));
				// }
				// if (castAsTreeNode.isReference()){
				// return boldFont;
				// }
				// return defaultFont;

			}

		});

		masterTreeViewer.setContentProvider(new MyContentProvider());
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model model = ActiveTDB.getModel(null);
		if (model.contains(FedLCA.FlowUnit, ECO.hasDataSource, FedLCA.UnitGroup)) {
			System.out.println("Hey!");
		}
		ActiveTDB.tdbDataset.end();

		masterTreeViewer.setInput(createPropertyTree());
		masterTree = masterTreeViewer.getTree();

		masterTree.addSelectionListener(new SelectionListener() {

			private void doit(SelectionEvent e) {
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
		// expandAll();
		for (TreeItem item : masterTree.getItems()) {
			expandItem(item);
			// for (TreeItem subItem : item.getItems()) {
			// subItem.setExpanded(true);
			// }
		}
		partialCollapse();
	}

	private static void createFonts(TreeItem treeItem) {
		defaultFont = treeItem.getFont();
		FontData boldFontData = defaultFont.getFontData()[0];
		boldFontData.setStyle(SWT.BOLD);
		boldFont = new Font(Display.getCurrent(), boldFontData);
	}

	private SelectionListener unassignListener = new SelectionListener() {

		private void doit(SelectionEvent e) {
			// collapseAll();
			Util.findView(CSVTableView.ID);
			Util.findView(FlowsWorkflow.ID);
			int csvRowNumSelected = CSVTableView.getRowNumSelected();
			if (csvRowNumSelected < 0) {
				return;
			}
			TableItem tableItem = CSVTableView.getTable().getItem(csvRowNumSelected);
			masterTree.deselectAll();
			if (currentFlowUnitSelection != null) {
				currentFlowUnitSelection.setBackground(null);
			}
			currentFlowUnitSelection = null;

			String rowNumString = tableItem.getText(0);
			int rowNumber = Integer.parseInt(rowNumString) - 1;
			DataRow dataRow = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getData().get(rowNumber);
			// dataRow.getFlowProperty().setMatchingResource(null);
			dataRow.getFlowUnit().setMatchingResource(null);
			FlowsWorkflow.removeMatchPropertyRowNum(unitToMatch.getFirstRow());
			CSVTableView.colorFlowPropertyRows();
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

	private static void assign() {
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
		if (unitToMatch == null) {
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

		/*
		 * The Tree will be always left with zero selections, so that when a user does select, that is the "new" choice.
		 * If that choice is identical to the current choice, there will simply be a deselectAll() If the choice is a
		 * SuperGroup there will be no effect except deselectAll() If that choice is a FlowProperty (only), the FlowUnit
		 * will be set to "none" and all other fields changed to white If that choice is a FlowUnit, the corresponding
		 * FlowProperty will be selected as well.
		 */
		int count = masterTree.getSelectionCount();
		if (count < 1) {
			userDataLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
			currentFlowUnitSelection.setBackground(null);
			return;
		}

		TreeItem selectedTreeItem = masterTree.getSelection()[0];
		TreeNode selectedTreeNode = (TreeNode) selectedTreeItem.getData();

		if (selectedTreeNode.isReference) {
			if (boldFont == null) {
				createFonts(selectedTreeItem);
			}
			selectedTreeItem.setFont(boldFont);
		}

		if (selectedTreeNode.nodeClass == FedLCA.FlowUnit) {
			if (currentFlowUnitSelection != null) {
				currentFlowUnitSelection.setBackground(null);
			}
			currentFlowUnitSelection = selectedTreeItem;
			currentFlowUnitSelection.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
			unitToMatch.setMatchingResource(selectedTreeNode.uri);
		}

		masterTree.deselectAll();
		FlowsWorkflow.addMatchPropertyRowNum(unitToMatch.getFirstRow());
		CSVTableView.colorFlowPropertyRows();
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
			FlowUnit flowUnit = dataRow.getFlowUnit();
			if (flowUnit != null) {
				if (flowUnit.equals(unitToMatch)) {
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
		// System.out.println("Item expanded: item.getText() " + item.getText());
		item.setExpanded(true);
		masterTreeViewer.refresh();
		for (TreeItem child : item.getItems()) {
			expandItem(child);
		}
	}

	// private static void expandAll() {
	// for (TreeItem treeItem1 : masterTreeViewer.getTree().getItems()) {
	// treeItem1.setExpanded(true);
	// for (TreeItem treeItem2 : treeItem1.getItems()) {
	// treeItem2.setExpanded(true);
	// }
	// }
	// }
	//
	// private static void collapseAll() {
	// for (TreeItem treeItem1 : masterTreeViewer.getTree().getItems()) {
	// for (TreeItem treeItem2 : treeItem1.getItems()) {
	// treeItem2.setExpanded(false);
	// }
	// treeItem1.setExpanded(false);
	// }
	// }

	public static String getNodeNameFromResource(Resource resource) {
		for (TreeItem treeItem : masterTreeViewer.getTree().getItems()) {
			TreeNode treeNode = (TreeNode) treeItem.getData();
			for (TreeNode testNode : TreeNode.getAllChildNodes(treeNode)) {
				Resource nodeResource = testNode.uri;
				if (nodeResource == null) {
					continue;
				}
				if (nodeResource.equals(resource)) {
					return testNode.nodeName;
				}
			}
		}
		return ("(flow property name not found)");
	}

	public static String[] getThreePropertyStringsFromResource(Resource resource) {
		String[] resultStrings = new String[3];
		resultStrings[0] = ""; // Master List Flow Property
		resultStrings[1] = ""; // Master List Flow Property Reference Unit
		resultStrings[2] = ""; // Master List Flow Property Conversion Factor

		// for (FlowProperty flowProperty : FlowProperty.lcaMasterProperties) {
		for (FlowUnit flowUnit : FlowProperty.lcaMasterUnits) {
			if (flowUnit.getTdbResource().equals(resource)) {
				resultStrings[0] = (String) flowUnit.getOneProperty(FlowUnit.flowPropertyString);
				resultStrings[1] = flowUnit.getReferenceFlowUnit().getProperty(RDFS.label).getString();
				resultStrings[2] = "" + (Double) flowUnit.getOneProperty(FlowUnit.conversionFactor);
			}
			// }
		}
		return resultStrings;
	}

	public static String[] getFourPropertyStringsFromResource(Resource resource) {
		String[] resultStrings = new String[4];
		resultStrings[0] = ""; // Master List Flow Property
		resultStrings[1] = ""; // Master List Flow Property Unit (name)
		resultStrings[2] = ""; // Master List Flow Property Conversion Factor
		resultStrings[3] = ""; // Master List Flow Property Reference Factor

		// for (FlowProperty flowProperty : FlowProperty.lcaMasterProperties) {
		for (FlowUnit flowUnit : FlowProperty.lcaMasterUnits) {
			if (flowUnit.getTdbResource().equals(resource)) {

				resultStrings[0] = (String) flowUnit.getOneProperty(FlowUnit.flowPropertyString);
				resultStrings[1] = (String) flowUnit.getOneProperty(FlowUnit.flowUnitString);
				resultStrings[2] = "" + (Double) flowUnit.getOneProperty(FlowUnit.conversionFactor);
				resultStrings[3] = flowUnit.getReferenceFlowUnit().getProperty(RDFS.label).getString();
				// resultStrings[0] = flowUnit.getUnitGroupName();
				// resultStrings[1] = flowUnit.name;
				// resultStrings[2] = "" + flowUnit.conversionFactor;
				// resultStrings[3] = flowUnit.getReferenceUnitName();
			}
			// }
		}
		return resultStrings;
	}

	private TreeNode createPropertyTree() {
		FlowProperty.reLoadMasterFlowUnits();
		TreeNode masterPropertyTree = new TreeNode(null);

		TreeNode curSuperGroupNode = null;
		TreeNode curGroupNode = null;
		String lastSuperGroupString = "";
		Resource lastUnitGroup = null;
		boolean isReferenceUnit = false;
		for (FlowUnit flowUnit : FlowProperty.lcaMasterUnits) {
			String superGroupString = flowUnit.getUnitSuperGroup();
			// boolean isUnitNode = true;
			if (!superGroupString.equals(lastSuperGroupString)) {
				lastSuperGroupString = superGroupString;
				curSuperGroupNode = new TreeNode(masterPropertyTree);
				curSuperGroupNode.nodeName = superGroupString;
				curSuperGroupNode.nodeClass = FedLCA.UnitSuperGroup;
				// isUnitNode = false;
			}
			Resource unitGroup = flowUnit.getUnitGroup();
			if (lastUnitGroup == null || !lastUnitGroup.equals(unitGroup)) {
				isReferenceUnit = true;
				lastUnitGroup = unitGroup;
				curGroupNode = new TreeNode(curSuperGroupNode);
				curGroupNode.uri = unitGroup;
				curGroupNode.nodeName = unitGroup.getProperty(RDFS.label).getObject().asLiteral().getString();
				curGroupNode.uuid = unitGroup.getProperty(FedLCA.hasOpenLCAUUID).getObject().asLiteral().getString();
				curGroupNode.nodeClass = FedLCA.UnitGroup;
				// curGroupNode.setReference(true);
				/*
				 * TODO: consider whether the user should be able to choose a property (UnitGroup) without having a Unit
				 */
				// curGroupNode.relatedObject = flowProperty;
			}

			TreeNode curNode = new TreeNode(curGroupNode);

			curNode.nodeName = flowUnit.getOneProperty(FlowUnit.flowUnitString).toString();
			curNode.uri = flowUnit.getTdbResource();
			curNode.nodeClass = FedLCA.FlowUnit;
			curNode.uuid = (String) flowUnit.getOneProperty(FlowUnit.openLCAUUID);
			curNode.referenceDescription = (String) flowUnit.getOneProperty(FlowUnit.flowPropertyUnitDescription);
			if (flowUnit.getOneProperty(FlowUnit.conversionFactor) != null) {
				curNode.conversionFactor = (Double) flowUnit.getOneProperty(FlowUnit.conversionFactor);
			}
			curNode.relatedObject = flowUnit;
			if (isReferenceUnit) {
				curNode.isReference = true;
				isReferenceUnit = false;
			} else {
				Resource resourceReferenceUnit = flowUnit.getReferenceFlowUnit();
				String refUnitString = resourceReferenceUnit.getProperty(RDFS.label).getString();
				curNode.setReferenceUnit(refUnitString);
			}
		}
		return masterPropertyTree;
	}

	// private void createSubNodes(TreeNode flowPropertyNode) {
	// String propertyName = flowPropertyNode.getNodeName();
	// String propertyReferenceUnit = flowPropertyNode.getReferenceUnit();
	// List<FlowUnit> units = new ArrayList<FlowUnit>();
	// for (FlowUnit lcaUnit : FlowProperty.lcaMasterUnits) {
	// if (lcaUnit.unit_group.equals(propertyName)) {
	// if (lcaUnit.name.equals(propertyReferenceUnit)) {
	// units.add(0, lcaUnit);
	// } else {
	// units.add(lcaUnit);
	// }
	// }
	// }
	// for (FlowUnit lcaUnit : units) {
	// System.out.println(lcaUnit.name);
	// System.out.println("// " + lcaUnit.description);
	//
	// TreeNode subNode = new TreeNode(flowPropertyNode);
	// subNode.nodeName = lcaUnit.description + "(" + lcaUnit.name + ")";
	// // subNode.uri = FedLCA.[pssh];
	// subNode.uuid = lcaUnit.uuid;
	// subNode.uri = lcaUnit.tdbResource;
	// subNode.referenceDescription = lcaUnit.description;
	// subNode.referenceUnit = flowPropertyNode.referenceUnit;
	// }
	// }

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
	// // System.out.println("treeNode1 = " + treeNode1);
	// if (resource.equals(treeNode1.getUri())) {
	// return treeNode1;
	//
	// }
	// }
	//
	// for (TreeItem treeItem2 : treeItem1.getItems()) {
	// TreeNode treeNode2 = (TreeNode) treeItem2.getData();
	// // System.out.println("treeNode2 = " + treeNode2);
	//
	// if (treeNode2.getUri() != null) {
	// if (resource.equals(treeNode2.getUri())) {
	// return treeNode2;
	// }
	// }
	// for (TreeItem treeItem3 : treeItem2.getItems()) {
	// TreeNode treeNode3 = (TreeNode) treeItem3.getData();
	// // System.out.println("treeNode3 = " + treeNode3);
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
			if (treeNode1.uri != null) {
				if (resource.equals(treeNode1.uri)) {
					return treeItem1;
				}
			}
			for (TreeItem treeItem2 : treeItem1.getItems()) {
				TreeNode treeNode2 = (TreeNode) treeItem2.getData();
				if (treeNode2 == null) {
					return null;
				}
				if (treeNode2.uri != null) {
					if (resource.equals(treeNode2.uri)) {
						return treeItem2;
					}
				}
				for (TreeItem treeItem3 : treeItem2.getItems()) {
					TreeNode treeNode3 = (TreeNode) treeItem3.getData();
					if (treeNode3 == null) {
						return null;
					}
					if (treeNode3.uri != null) {
						if (resource.equals(treeNode3.uri)) {
							return treeItem3;
						}
					}
				}
			}
		}
		return null;
	}

	public List<String> getPropertiesToMatch() {
		return propertiesToMatch;
	}

	public void setPropertiesToMatch(List<String> contexts) {
		propertiesToMatch = contexts;
		// update();
	}

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
	// if (e.button == 1) {
	// leftClick(e);
	// } else if (e.button == 3) {
	// // queryTbl.deselectAll();
	// rightClick(e);
	// }
	// }
	//
	// @Override
	// public void mouseUp(MouseEvent e) {
	// System.out.println("mouse up event :e =" + e);
	// }
	//
	// private void leftClick(MouseEvent event) {
	// System.out.println("cellSelectionMouseDownListener event " + event);
	// Point ptLeft = new Point(1, event.y);
	// Point ptClick = new Point(event.x, event.y);
	// int clickedRow = 0;
	// int clickedCol = 0;
	// // TableItem item = queryTbl.getItem(ptLeft);
	// // if (item == null) {
	// // return;
	// // }
	// // clickedRow = queryTbl.indexOf(item);
	// // clickedCol = getTableColumnNumFromPoint(clickedRow, ptClick);
	// // if (clickedCol > 0) {
	// // queryTbl.deselectAll();
	// // return;
	// // }
	// // queryTbl.select(clickedRow);
	// rowNumSelected = clickedRow;
	// colNumSelected = clickedCol;
	// System.out.println("rowNumSelected = " + rowNumSelected);
	// System.out.println("colNumSelected = " + colNumSelected);
	// // rowMenu.setVisible(true);
	// }
	//
	// private void rightClick(MouseEvent event) {
	// System.out.println("cellSelectionMouseDownListener event " + event);
	// Point ptLeft = new Point(1, event.y);
	// Point ptClick = new Point(event.x, event.y);
	// int clickedRow = 0;
	// int clickedCol = 0;
	// // TableItem item = queryTbl.getItem(ptLeft);
	// // if (item == null) {
	// // return;
	// // }
	// // clickedRow = queryTbl.indexOf(item);
	// // clickedCol = getTableColumnNumFromPoint(clickedRow, ptClick);
	// // int dataClickedCol = clickedCol - 1;
	// if (clickedCol < 0) {
	// return;
	// }
	//
	// rowNumSelected = clickedRow;
	// colNumSelected = clickedCol;
	// System.out.println("rowNumSelected = " + rowNumSelected);
	// System.out.println("colNumSelected = " + colNumSelected);
	// }
	// };
	private Composite outerComposite;
	private Button unAssignButton;
	private Button nextButton;
	private Button nextUnmatchedButton;

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

	public List<Resource> getPropertyResourcesToMatch() {
		return propertyResourcesToMatch;
	}

	public void setPropertyResourcesToMatch(List<Resource> contextResourcesToMatch) {
		this.propertyResourcesToMatch = contextResourcesToMatch;
	}

	public static void update(int rowNumber) {
		List<DataRow> data = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getData();
		DataRow dataRow = data.get(rowNumber);
		unitToMatch = dataRow.getFlowUnit();
		if (unitToMatch == null) {
			masterTree.deselectAll();
			if (currentFlowUnitSelection != null) {
				currentFlowUnitSelection.setBackground(null);
				currentFlowUnitSelection = null;
			}
			setUserDataLabel("", false);
			return;
		}
		int rowCount = 0;
		for (int i = unitToMatch.getFirstRow(); i < data.size(); i++) {
			FlowUnit flowUnitOfRow = data.get(i).getFlowUnit();
			if (flowUnitOfRow != null) {
				if (flowUnitOfRow.equals(unitToMatch)) {
					rowCount++;
				}
			}
		}
		if (currentFlowUnitSelection != null) {
			currentFlowUnitSelection.setBackground(null);
		}
		String nounVerb = " flows contain";
		if (rowCount == 1) {
			nounVerb = " flow contains";
		}
		String labelString = rowCount + nounVerb + System.getProperty("line.separator");

		if (unitToMatch.getUnitGroup() != null) {
			labelString += unitToMatch.getUnitGroup().getProperty(RDFS.label).getString()
					+ System.getProperty("line.separator") + "   "
					+ (String) unitToMatch.getOneProperty(FlowUnit.flowUnitString);
		} else {
			labelString += (String) unitToMatch.getOneProperty(FlowUnit.flowUnitString);
		}

		partialCollapse();
		Resource propertyResource = unitToMatch.getMatchingResource();
		if (propertyResource != null) {
			TreeItem treeItem = getTreeItemByURI(propertyResource);
			if (treeItem != null) {
				TreeNode treeNode = (TreeNode) treeItem.getData();
				if (treeNode.nodeClass.equals(FedLCA.FlowUnit)) {
					TreeItem parentItem = treeItem.getParentItem();
					parentItem.setExpanded(true);
					currentFlowUnitSelection = treeItem;
					treeItem.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
				}
				setUserDataLabel(labelString, true);

			} else {
				masterTree.deselectAll();
				setUserDataLabel(labelString, false);
			}
		} else {
			masterTree.deselectAll();
			setUserDataLabel(labelString, false);
		}
	}

	private static void partialCollapse() {
		for (TreeItem treeItem1 : masterTreeViewer.getTree().getItems()) {
			treeItem1.setExpanded(true);
			for (TreeItem treeItem2 : treeItem1.getItems()) {
				treeItem2.setExpanded(false);
			}
		}
	}

	private static void setUserDataLabel(String labelString, boolean isMatched) {
		if (labelString != null) {
			userDataLabel.setText(labelString);
		}
		if (isMatched) {
			userDataLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
		} else {
			userDataLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
		}
	}
}
