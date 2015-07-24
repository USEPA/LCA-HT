package gov.epa.nrmrl.std.lca.ht.flowProperty.mgr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
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
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.Node;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.TreeNode;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
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
	private static int rowNumSelected;
	private static int colNumSelected;
	private static FlowUnit unitToMatch;
	// private static TreeItem currentFlowPropertySelection;
	private static TreeItem currentFlowUnitSelection;

	private class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return new Object[0];
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

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
		GridLayout gl_innerComposite = new GridLayout(2, false);
		gl_innerComposite.horizontalSpacing = 15;
		gl_innerComposite.marginHeight = 0;
		gl_innerComposite.marginLeft = 5;
		gl_innerComposite.marginWidth = 0;
		gl_innerComposite.verticalSpacing = 0;
		innerComposite.setLayout(gl_innerComposite);

		unAssignButton = new Button(innerComposite, SWT.NONE);
		GridData gd_unAssignButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_unAssignButton.heightHint = 20;
		gd_unAssignButton.widthHint = 90;
		unAssignButton.setLayoutData(gd_unAssignButton);
		unAssignButton.setText("Unassign");
		unAssignButton.addSelectionListener(unassignListener);

		nextButton = new Button(innerComposite, SWT.NONE);
		GridData gd_assignButton = new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1);
		gd_assignButton.widthHint = 90;
		nextButton.setLayoutData(gd_assignButton);
		nextButton.setText("Next");
		nextButton.addSelectionListener(nextListener);

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
		int thing = masterTree.getSelectionCount();
		if (thing > 0) {
			TreeItem selectedTreeItem = masterTree.getSelection()[0];
			TreeNode selectedTreeNode = (TreeNode) selectedTreeItem.getData();
			if (selectedTreeNode.nodeClass == FedLCA.FlowUnit) {
				if (currentFlowUnitSelection != null) {
					currentFlowUnitSelection.setBackground(null);

				}
				currentFlowUnitSelection = selectedTreeItem;
				currentFlowUnitSelection.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
				unitToMatch.setMatchingResource(selectedTreeNode.uri);
			}
		}
		masterTree.deselectAll();

		// unitToMatch.setMatchingResource(newMasterFlowProperty);

		// FlowUnit flowUnit = unitToMatch.getUserDataFlowUnit();
		// if (flowUnit == null) {
		// if (newMasterFlowUnit != null) {
		// flowUnit = new FlowUnit(newMasterFlowUnit);
		// unitToMatch.setUserDataFlowUnit(flowUnit);
		// }
		// } else {
		// flowUnit.setMatchingResource(newMasterFlowUnit);
		// }

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
			// expandAll();
			CSVTableView.selectNext(ID);
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
		System.out.println("Item expanded: item.getText() " + item.getText());
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
		// ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		// Model model = ActiveTDB.getModel(null);
		// if (model.contains(FedLCA.FlowUnit, ECO.hasDataSource, FedLCA.UnitGroup)) {
		// System.out.println("Hey!");
		// }
		// ActiveTDB.tdbDataset.end();

		FlowProperty.loadMasterFlowUnits();
		TreeNode masterPropertyTree = new TreeNode(null);

		TreeNode curSuperGroupNode = null;
		TreeNode curGroupNode = null;
		String lastSuperGroupString = "";
		Resource lastUnitGroup = null;
		for (FlowUnit flowUnit : FlowProperty.lcaMasterUnits) {
			String superGroupString = flowUnit.getUnitSuperGroup();
			// for (FlowUnit flowUnit : FlowProperty.lcaMasterUnits) {
			if (!superGroupString.equals(lastSuperGroupString)) {
				lastSuperGroupString = superGroupString;
				curSuperGroupNode = new TreeNode(masterPropertyTree);
				curSuperGroupNode.nodeName = superGroupString;
				curSuperGroupNode.nodeClass = FedLCA.UnitSuperGroup;
			}
			Resource unitGroup = flowUnit.getUnitGroup();
			if (lastUnitGroup == null) {
				lastUnitGroup = unitGroup;
				curGroupNode = new TreeNode(curSuperGroupNode);
				curGroupNode.uri = unitGroup;
				curGroupNode.nodeName = unitGroup.getProperty(RDFS.label).getObject().asLiteral().getString();
				curGroupNode.uuid = unitGroup.getProperty(FedLCA.hasOpenLCAUUID).getObject().asLiteral().getString();

				curGroupNode.nodeClass = FedLCA.UnitGroup;
				// curGroupNode.relatedObject = flowProperty;
			} else if (!lastUnitGroup.equals(unitGroup)) {
				lastUnitGroup = unitGroup;
				curGroupNode = new TreeNode(curSuperGroupNode);
				curGroupNode.uri = unitGroup;
				curGroupNode.nodeName = unitGroup.getProperty(RDFS.label).getObject().asLiteral().getString();
				curGroupNode.uuid = unitGroup.getProperty(FedLCA.hasOpenLCAUUID).getObject().asLiteral().getString();

				curGroupNode.nodeClass = FedLCA.UnitGroup;
			}

			TreeNode curNode = new TreeNode(curGroupNode);

			curNode.nodeName = flowUnit.getOneProperty(FlowUnit.flowUnitString) + " ("
					+ flowUnit.getOneProperty(FlowUnit.flowPropertyUnitDescription) + ")";
			curNode.uri = flowUnit.getTdbResource();
			curNode.nodeClass = FedLCA.FlowUnit;
			curNode.uuid = (String) flowUnit.getOneProperty(FlowUnit.openLCAUUID);
			curNode.referenceDescription = (String) flowUnit.getOneProperty(FlowUnit.flowPropertyUnitDescription);
			curNode.relatedObject = flowUnit;
			// }
		}
		// partialCollapse();

		// TreeNode physicalIndividual = new TreeNode(masterPropertyTree);
		// physicalIndividual.nodeName = "Physical individual";
		//
		// TreeNode physicalCombined = new TreeNode(masterPropertyTree);
		// physicalCombined.nodeName = "Physical hybrid";
		//
		// TreeNode other = new TreeNode(masterPropertyTree);
		// other.nodeName = "Other";

		// // -------- PHYSICAL
		// TreeNode mass = new TreeNode(physicalIndividual);
		// mass.nodeName = "Mass";
		// mass.uri = FedLCA.Mass;
		// mass.uuid = "93a60a57-a4c8-11da-a746-0800200c9a66";
		// mass.referenceDescription = "Kilogram";
		// mass.referenceUnit = "kg";
		// createSubNodes(mass);
		//
		// TreeNode length = new TreeNode(physicalIndividual);
		// length.nodeName = "Length";
		// length.uri = FedLCA.Length;
		// length.uuid = "838aaa22-0117-11db-92e3-0800200c9a66";
		// length.referenceDescription = "Meter";
		// length.referenceUnit = "m";
		// createSubNodes(length);
		//
		// TreeNode area = new TreeNode(physicalIndividual);
		// area.nodeName = "Area";
		// area.uri = FedLCA.Area;
		// area.uuid = "93a60a57-a3c8-18da-a746-0800200c9a66";
		// area.referenceDescription = "Square meter";
		// area.referenceUnit = "m2";
		// createSubNodes(area);
		//
		// TreeNode volume = new TreeNode(physicalIndividual);
		// volume.nodeName = "Volume";
		// volume.uri = FedLCA.Volume;
		// volume.uuid = "93a60a57-a3c8-12da-a746-0800200c9a66";
		// volume.referenceDescription = "Cubic meter";
		// volume.referenceUnit = "m3";
		// createSubNodes(volume);
		//
		// TreeNode duration = new TreeNode(physicalIndividual);
		// duration.nodeName = "Time";
		// // duration.nodeName = "Duration";
		// duration.uri = FedLCA.Duration;
		// duration.uuid = "af638906-3ec7-4314-8de7-f76039f2dd01";
		// duration.referenceDescription = "Day";
		// duration.referenceUnit = "d";
		// createSubNodes(duration);
		//
		// TreeNode energy = new TreeNode(physicalIndividual);
		// energy.nodeName = "Energy";
		// energy.uri = FedLCA.Energy;
		// energy.uuid = "93a60a57-a3c8-11da-a746-0800200c9a66";
		// energy.referenceDescription = "Megajoule";
		// energy.referenceUnit = "MJ";
		// createSubNodes(energy);
		//
		// TreeNode radioactivity = new TreeNode(physicalIndividual);
		// radioactivity.nodeName = "Radioactivity";
		// radioactivity.uri = FedLCA.Radioactivity;
		// radioactivity.uuid = "93a60a57-a3c8-16da-a746-0800200c9a66";
		// radioactivity.referenceDescription = "Kilo-Bequerel, 1000 events per second";
		// radioactivity.referenceUnit = "kBq";
		// createSubNodes(radioactivity);
		//
		// // -------- PHYSICAL HYBRID
		// TreeNode massTime = new TreeNode(physicalCombined);
		// massTime.nodeName = "Mass*time";
		// massTime.uri = FedLCA.MassTime;
		// massTime.uuid = "59f191d6-5dd3-4553-af88-1a32accfe308";
		// massTime.referenceDescription = "Kilogram times year";
		// massTime.referenceUnit = "kg*a";
		// createSubNodes(massTime);
		//
		// TreeNode massPerTime = new TreeNode(physicalCombined);
		// massPerTime.nodeName = "Mass/time";
		// massPerTime.uri = FedLCA.MassPerTime;
		// massPerTime.uuid = "94b84332-8f2d-4592-b2a0-e19da33a69e9";
		// massPerTime.referenceDescription = "Kilogram per year";
		// massPerTime.referenceUnit = "kg/a";
		// createSubNodes(massPerTime);
		//
		// TreeNode massLength = new TreeNode(physicalCombined);
		// massLength.nodeName = "Mass*length";
		// massLength.uri = FedLCA.MassLength;
		// massLength.uuid = "838aaa21-0117-11db-92e3-0800200c9a66";
		// massLength.referenceDescription = "Metric ton-kilometer";
		// massLength.referenceUnit = "t*km";
		// createSubNodes(massLength);
		//
		// TreeNode lengthTime = new TreeNode(physicalCombined);
		// lengthTime.nodeName = "Length*time";
		// lengthTime.uri = FedLCA.LengthTime;
		// lengthTime.uuid = "326eb58b-e5b3-4cea-b45a-2398c25109f8";
		// lengthTime.referenceDescription = "Meter times year";
		// lengthTime.referenceUnit = "m*a";
		// createSubNodes(lengthTime);
		//
		// TreeNode areaTime = new TreeNode(physicalCombined);
		// areaTime.nodeName = "Area*time";
		// areaTime.uri = FedLCA.AreaTime;
		// areaTime.uuid = "93a60a57-a3c8-20da-a746-0800200c9a66";
		// areaTime.referenceDescription = "Square meter times year";
		// areaTime.referenceUnit = "m2*a";
		// createSubNodes(areaTime);
		//
		// TreeNode volumeTime = new TreeNode(physicalCombined);
		// volumeTime.nodeName = "Volume*time";
		// volumeTime.uri = FedLCA.VolumeTime;
		// volumeTime.uuid = "93a60a57-a3c8-23da-a746-0800200c9a66";
		// volumeTime.referenceDescription = "Cubic meter times year";
		// volumeTime.referenceUnit = "m3*a";
		// createSubNodes(volumeTime);
		//
		// TreeNode volumeLength = new TreeNode(physicalCombined);
		// volumeLength.nodeName = "Volume*length";
		// volumeLength.uri = FedLCA.VolumeLength;
		// volumeLength.uuid = "ff8ed45d-bbfb-4531-8c7b-9b95e52bd41d";
		// volumeLength.referenceDescription = "Cubic metre times kilometre";
		// volumeLength.referenceUnit = "m3*km";
		// createSubNodes(volumeLength);
		//
		// TreeNode energyPerMassTime = new TreeNode(physicalCombined);
		// energyPerMassTime.nodeName = "Energy/mass*time";
		// energyPerMassTime.uri = FedLCA.EnergyPerMassTime;
		// energyPerMassTime.uuid = "258d6abd-14f2-4484-956c-c88e8f6fd8ed";
		// energyPerMassTime.referenceDescription = "Megajoule per kilogram times day";
		// energyPerMassTime.referenceUnit = "MJ/kg*d";
		// createSubNodes(energyPerMassTime);
		//
		// TreeNode energyPerAreaTime = new TreeNode(physicalCombined);
		// energyPerAreaTime.nodeName = "Energy/area*time";
		// energyPerAreaTime.uri = FedLCA.EnergyPerAreaTime;
		// energyPerAreaTime.uuid = "876adcd3-29e6-44e2-acdd-11be304ae654";
		// energyPerAreaTime.referenceDescription = "Kilowatthour per square meter times day";
		// energyPerAreaTime.referenceUnit = "kWh/m2*d";
		// createSubNodes(energyPerAreaTime);
		//
		// // -------- OTHER
		// TreeNode itemCount = new TreeNode(other);
		// itemCount.nodeName = "Number of items";
		// itemCount.uri = FedLCA.ItemCount;
		// itemCount.uuid = "5beb6eed-33a9-47b8-9ede-1dfe8f679159";
		// itemCount.referenceDescription = "Number of items";
		// itemCount.referenceUnit = "Item(s)";
		// createSubNodes(itemCount);
		//
		// TreeNode itemsLength = new TreeNode(other);
		// itemsLength.nodeName = "Items*length";
		// itemsLength.uri = FedLCA.ItemsLength;
		// itemsLength.uuid = "5454b231-270e-45e6-89b2-7f4f3e482245";
		// itemsLength.referenceDescription = "Items times kilometre";
		// itemsLength.referenceUnit = "Items*km";
		// createSubNodes(itemsLength);
		//
		// TreeNode goodsTransportMassDistance = new TreeNode(other);
		// goodsTransportMassDistance.nodeName = "Goods transport (mass*distance)";
		// goodsTransportMassDistance.uri = FedLCA.GoodsTransportMassDistance;
		// goodsTransportMassDistance.uuid = "";
		// goodsTransportMassDistance.referenceDescription = "";
		// goodsTransportMassDistance.referenceUnit = "";
		// createSubNodes(goodsTransportMassDistance);
		//
		// TreeNode personTransport = new TreeNode(other);
		// personTransport.nodeName = "Person transport";
		// personTransport.uri = FedLCA.PersonTransport;
		// personTransport.uuid = "11d161f0-37e3-4d49-bf7a-ff4f31a9e5c7";
		// personTransport.referenceDescription = "Person kilometer";
		// personTransport.referenceUnit = "p*km";
		// createSubNodes(personTransport);
		//
		// TreeNode vehicleTransport = new TreeNode(other);
		// vehicleTransport.nodeName = "Vehicle transport";
		// vehicleTransport.uri = FedLCA.VehicleTransport;
		// vehicleTransport.uuid = "af16ae7e-3e04-408a-b8ae-5b3666dbe7f9";
		// vehicleTransport.referenceDescription = "Vehicle-kilometer";
		// vehicleTransport.referenceUnit = "v*km";
		// createSubNodes(vehicleTransport);
		//
		// TreeNode netCalorificValue = new TreeNode(other);
		// netCalorificValue.nodeName = "Net calorific value";
		// netCalorificValue.uri = FedLCA.NetCalorificValue;
		// netCalorificValue.uuid = "";
		// netCalorificValue.referenceDescription = "";
		// netCalorificValue.referenceUnit = "";
		// createSubNodes(netCalorificValue);
		//
		// TreeNode grossCalorificValue = new TreeNode(other);
		// grossCalorificValue.nodeName = "Gross calorific value";
		// grossCalorificValue.uri = FedLCA.GrossCalorificValue;
		// grossCalorificValue.uuid = "";
		// grossCalorificValue.referenceDescription = "";
		// grossCalorificValue.referenceUnit = "";
		// createSubNodes(grossCalorificValue);
		//
		// TreeNode normalVolume = new TreeNode(other);
		// normalVolume.nodeName = "Normal Volume";
		// normalVolume.uri = FedLCA.NormalVolume;
		// normalVolume.uuid = "";
		// normalVolume.referenceDescription = "";
		// normalVolume.referenceUnit = "";
		// createSubNodes(normalVolume);
		//
		// TreeNode valueUS2000BulkPrices = new TreeNode(other);
		// valueUS2000BulkPrices.nodeName = "Market value US 2000, bulk prices";
		// valueUS2000BulkPrices.uri = FedLCA.ValueUS2000BulkPrices;
		// valueUS2000BulkPrices.uuid = "";
		// valueUS2000BulkPrices.referenceDescription = "";
		// valueUS2000BulkPrices.referenceUnit = "";
		// createSubNodes(valueUS2000BulkPrices);

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

	private static TreeNode getTreeNodeByURI(Resource resource) {
		for (TreeItem treeItem1 : masterTree.getItems()) {
			TreeNode treeNode1 = (TreeNode) treeItem1.getData();
			if (treeNode1.getUri() != null) {
				System.out.println("treeNode1 = " + treeNode1);
				if (resource.equals(treeNode1.getUri())) {
					return treeNode1;

				}
			}

			for (TreeItem treeItem2 : treeItem1.getItems()) {
				TreeNode treeNode2 = (TreeNode) treeItem2.getData();
				System.out.println("treeNode2 = " + treeNode2);

				if (treeNode2.getUri() != null) {
					if (resource.equals(treeNode2.getUri())) {
						return treeNode2;
					}
				}
				for (TreeItem treeItem3 : treeItem2.getItems()) {
					TreeNode treeNode3 = (TreeNode) treeItem3.getData();
					System.out.println("treeNode3 = " + treeNode3);

					if (treeNode3.getUri() != null) {
						if (resource.equals(treeNode3.getUri())) {
							return treeNode3;
						}
					}
				}
			}
		}
		return null;
	}

	private static TreeItem getTreeItemByURI(Resource resource) {
		for (TreeItem treeItem1 : masterTree.getItems()) {
			TreeNode treeNode1 = (TreeNode) treeItem1.getData();
			String node1Name = treeNode1.nodeName;

			if (treeNode1.getUri() != null) {
				// System.out.println("treeNode1 = " + treeNode1);
				if (resource.equals(treeNode1.getUri())) {
					return treeItem1;
				}
			}
			for (TreeItem treeItem2 : treeItem1.getItems()) {
				TreeNode treeNode2 = (TreeNode) treeItem2.getData();
				if (treeNode2 == null) {
					System.out.println("Choke at level 2 with" + node1Name);
					return null;
				}
				String node2Name = treeNode2.nodeName;
				// System.out.println("treeNode2 = " + treeNode2);
				if (treeNode2.getUri() != null) {
					if (resource.equals(treeNode2.getUri())) {
						return treeItem2;
					}
				}
				for (TreeItem treeItem3 : treeItem2.getItems()) {
					TreeNode treeNode3 = (TreeNode) treeItem3.getData();
					if (treeNode3 == null) {
						System.out.println("Choke at level 3 with" + node2Name);
						return null;
					}
					String node3Name = treeNode3.nodeName;

					// System.out.println("treeNode3 = " + treeNode3);
					if (treeNode3.getUri() != null) {
						if (resource.equals(treeNode3.getUri())) {
							return treeItem3;
						}
						// for (TreeItem treeItem4 : treeItem3.getItems()) {
						// TreeNode treeNode4 = (TreeNode) treeItem4.getData();
						// // System.out.println("treeNode4 = " + treeNode4);
						// if (treeNode4.getUri() != null) {
						// if (resource.equals(treeNode4.getUri())) {
						// return treeItem4;
						// }
						// }
						// }
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
		for (int i = unitToMatch.getFirstRow();i<data.size();i++){
			FlowUnit flowUnitOfRow = data.get(i).getFlowUnit();
			if (flowUnitOfRow != null){
				if (flowUnitOfRow.equals(unitToMatch)){
					rowCount++;
				}
			}
		}
		if (currentFlowUnitSelection != null) {
			currentFlowUnitSelection.setBackground(null);
		}
		String labelString;
		if (unitToMatch.getUnitGroup() != null) {
			labelString = unitToMatch.getUnitGroup().getProperty(RDFS.label).getString()
					+ System.getProperty("line.separator") + "   "
					+ (String) unitToMatch.getOneProperty(FlowUnit.flowUnitString);
		} else {
			labelString = (String) unitToMatch.getOneProperty(FlowUnit.flowUnitString);
		}
		labelString+=System.getProperty("line.separator") + rowCount + " rows";

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

	// public static void update() {
	// Util.findView(CSVTableView.ID);
	// if (CSVTableView.getTable().getSelectionCount() == 0) {
	// return;
	// }
	// TableItem tableItem = CSVTableView.getTable().getSelection()[0];
	// String rowNumString = tableItem.getText(0);
	// int rowNumber = Integer.parseInt(rowNumString) - 1;
	// DataRow dataRow = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getData().get(rowNumber);
	// unitToMatch = dataRow.getFlowProperty();
	// if (unitToMatch == null) {
	// masterTree.deselectAll();
	// setUserDataLabel("", false);
	// } else {
	//
	// String labelString = null;
	// String propertyString = unitToMatch.getPropertyStr();
	// String unitString = unitToMatch.getUnitStr();
	// if (propertyString == null) {
	// labelString = unitString;
	// } else {
	// // labelString = propertyString + ": " + unitString;
	//
	// labelString = propertyString + System.getProperty("line.separator") + "   " + unitString;
	// }
	//
	// partialCollapse();
	// Resource propertyResource = unitToMatch.getMatchingResource();
	// if (propertyResource != null) {
	// TreeItem treeItem = getTreeItemByURI(propertyResource);
	// if (treeItem != null) {
	// masterTree.setSelection(treeItem);
	// setUserDataLabel(labelString, true);
	//
	// } else {
	// masterTree.deselectAll();
	// setUserDataLabel(labelString, false);
	// }
	// } else {
	// masterTree.deselectAll();
	// setUserDataLabel(labelString, false);
	// }
	// }
	// }

	// public static void update(Integer dataRowNum) {
	// Util.findView(CSVTableView.ID);
	// if (CSVTableView.getTable().getSelectionCount() == 0){
	// return;
	// }
	// TableItem tableItem = CSVTableView.getTable().getSelection()[0];
	// String rowNumString = tableItem.getText(0);
	// int rowNumber = Integer.parseInt(rowNumString) - 1;
	// DataRow dataRow = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getData().get(rowNumber);
	// unitToMatch = dataRow.getFlowProperty();
	// Resource propertyResource = unitToMatch.getMatchingResource();
	// String labelString = null;
	// String propertyString = unitToMatch.getPropertyStr();
	// String unitString = unitToMatch.getUnitStr();
	// if (propertyString == null) {
	// labelString = unitString;
	// } else {
	// labelString = propertyString + ": " + unitString;
	// }
	//
	// partialCollapse();
	// if (propertyResource != null) {
	// TreeItem treeItem = getTreeItemByURI(propertyResource);
	// if (treeItem != null) {
	// masterTree.setSelection(treeItem);
	// // setUserDataLabel(labelString, SWTResourceManager.getColor(SWT.COLOR_GREEN));
	// setUserDataLabel(labelString, true);
	//
	// } else {
	// masterTree.deselectAll();
	// // setUserDataLabel(labelString, SWTResourceManager.getColor(SWT.COLOR_GREEN));
	// setUserDataLabel(labelString, true);
	// }
	// } else {
	// masterTree.deselectAll();
	// // setUserDataLabel(labelString, SWTResourceManager.getColor(SWT.COLOR_YELLOW));
	// setUserDataLabel(labelString, false);
	// }
	// }

	private static void partialCollapse() {
		for (TreeItem treeItem1 : masterTreeViewer.getTree().getItems()) {
			treeItem1.setExpanded(true);
			for (TreeItem treeItem2 : treeItem1.getItems()) {
				treeItem2.setExpanded(false);
			}
		}
	}

	private static void setUserDataLabel(String labelString, Color color) {
		if (labelString != null) {
			userDataLabel.setText(labelString);
		}
		userDataLabel.setBackground(color);
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
