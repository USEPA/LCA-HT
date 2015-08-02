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
		System.out.println("Item expanded: item.getText() " + item.getText());
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

	private static TreeNode createHarmonizeCompartments() {
		FlowContext.loadMasterFlowContexts();
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

		// // TreeNode release = new TreeNode(masterCompartmentTree);
		// // release.nodeName = "Release";
		// // confirmUri(LCAHT.release);
		//
		// TreeNode air = new TreeNode(masterCompartmentTree);
		// air.nodeName = "Release to air";
		// // air.uri = FedLCA.airUnspecified;
		// // air.uuid = "5ea0e54a-d88d-4f7c-89a4-54f21c5791e7";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode airUnspecified = new TreeNode(air);
		// airUnspecified.nodeName = "unspecified";
		// airUnspecified.uri = FedLCA.airUnspecified;
		// // airUnspecified.uuid = "5ea0e54a-d88d-4f7c-89a4-54f21c5791e7";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode airLowPop = new TreeNode(air);
		// airLowPop.nodeName = "low population density";
		// airLowPop.uri = FedLCA.airLow_population_density;
		// // airLowPop.uuid = "ebcdff7a-b8c0-405b-8601-98a1ac3f26ef";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode airHighPop = new TreeNode(air);
		// airHighPop.nodeName = "high population density";
		// airHighPop.uri = FedLCA.airHigh_population_density;
		// // confirmUri(FedLCA.release);
		// // airHighPop.uuid = "e6e67f13-0bcb-4113-966b-023c3186b339";
		//
		// TreeNode airLowPopLongTerm = new TreeNode(air);
		// airLowPopLongTerm.nodeName = "low population density, long-term";
		// airLowPopLongTerm.uri = FedLCA.airLow_population_densityLong_term;
		// // airLowPopLongTerm.uuid = "f9ac762d-1403-4763-9aec-9b11ab79874b";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode airLowerStratPlusUpperTrop = new TreeNode(air);
		// airLowerStratPlusUpperTrop.nodeName = "lower stratosphere + upper troposphere";
		// airLowerStratPlusUpperTrop.uri = FedLCA.airLower_stratosphere_upper_troposphere;
		// // airLowerStratPlusUpperTrop.uuid = "885ce78b-9872-4a59-8244-deebeb12caea";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode water = new TreeNode(masterCompartmentTree);
		// water.nodeName = "Release to water";
		// // water.uri = FedLCA.waterUnspecified;
		// // water.uuid = "a7c280e9-d13a-43cf-9127-d3bbf4d0e256";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode waterUnspecified = new TreeNode(water);
		// waterUnspecified.nodeName = "unspecified";
		// waterUnspecified.uri = FedLCA.waterUnspecified;
		// // waterUnspecified.uuid = "a7c280e9-d13a-43cf-9127-d3bbf4d0e256";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode waterFossil = new TreeNode(water);
		// waterFossil.nodeName = "fossil-";
		// waterFossil.uri = FedLCA.waterFossil;
		// // waterFossil.uuid = "d0d05279-8621-404d-9878-218f04427fa6";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode waterFresh = new TreeNode(water);
		// waterFresh.nodeName = "fresh-";
		// waterFresh.uri = FedLCA.waterFresh;
		// // waterFresh.uuid = "1657ede0-aec3-41d1-bf1d-eeada890bdce";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode waterFreshLongTerm = new TreeNode(water);
		// waterFreshLongTerm.nodeName = "fresh-, long-term";
		// waterFreshLongTerm.uri = FedLCA.waterFreshLong_term;
		// // waterFreshLongTerm.uuid = "ed1e0813-ed99-4897-b20c-13ec90584825";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode waterGround = new TreeNode(water);
		// waterGround.nodeName = "ground-";
		// waterGround.uri = FedLCA.waterGround;
		// // waterGround.uuid = "4f146a17-ae4a-487b-874b-5d3013b86f44";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode waterGroundLongTerm = new TreeNode(water);
		// waterGroundLongTerm.nodeName = "ground-, long-term";
		// waterGroundLongTerm.uri = FedLCA.waterGroundLong_term;
		// // waterGroundLongTerm.uuid = "eba77525-9745-4f4a-9182-91a67306ba1c";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode waterLake = new TreeNode(water);
		// waterLake.nodeName = "lake";
		// waterLake.uri = FedLCA.waterLake;
		// // waterLake.uuid = "c1069072-9923-48f6-821d-8fad6e0ace5b";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode waterOcean = new TreeNode(water);
		// waterOcean.nodeName = "ocean";
		// waterOcean.uri = FedLCA.waterOcean;
		// // waterOcean.uuid = "8b7c395f-60ef-4863-a7e6-3560b5ad1aae";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode waterRiver = new TreeNode(water);
		// waterRiver.nodeName = "river";
		// waterRiver.uri = FedLCA.waterRiver;
		// // waterRiver.uuid = "58ed0153-34aa-4d6f-babf-3cfb201eac1d";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode waterRiverLongTerm = new TreeNode(water);
		// waterRiverLongTerm.nodeName = "river, long-term";
		// waterRiverLongTerm.uri = FedLCA.waterRiverLong_term;
		// // waterRiverLongTerm.uuid = "1df73ec9-e6b7-4f91-8f62-14b8ee2f7d93";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode waterSurface = new TreeNode(water);
		// waterSurface.nodeName = "surface water";
		// waterSurface.uri = FedLCA.waterSurface;
		// // waterSurface.uuid = "782cf5cb-0a6b-44aa-8a87-e5997dd0d1ff";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode soil = new TreeNode(masterCompartmentTree);
		// soil.nodeName = "Release to soil";
		// // soil.uri = FedLCA.soilUnspecified;
		// // soil.uuid = "e97d11b5-78e4-4a93-9a63-14673f89f709";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode soilUnspecified = new TreeNode(soil);
		// soilUnspecified.nodeName = "unspecified";
		// soilUnspecified.uri = FedLCA.soilUnspecified;
		// // soilUnspecified.uuid = "e97d11b5-78e4-4a93-9a63-14673f89f709";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode soilAgricultural = new TreeNode(soil);
		// soilAgricultural.nodeName = "agricultural";
		// soilAgricultural.uri = FedLCA.soilAgricultural;
		// // soilAgricultural.uuid = "34efc703-6409-4acf-8f1d-dec646adca8c";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode soilForestry = new TreeNode(soil);
		// soilForestry.nodeName = "forestry";
		// soilForestry.uri = FedLCA.soilForestry;
		// // soilForestry.uuid = "b50bb945-da42-49d2-a6e1-73544e36aaf2";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode soilIndustrial = new TreeNode(soil);
		// soilIndustrial.nodeName = "industrial";
		// soilIndustrial.uri = FedLCA.soilIndustrial;
		// // soilIndustrial.uuid = "185a7592-e3ae-4c44-a124-9c700b76d33d";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode resource = new TreeNode(masterCompartmentTree);
		// resource.nodeName = "Resource";
		// // resource.uri = FedLCA.resourceUnspecified;
		// // resource.uuid = "0d557bab-d095-4142-912e-398fccb68240";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode resourceUnspecified = new TreeNode(resource);
		// resourceUnspecified.nodeName = "unspecified";
		// resourceUnspecified.uri = FedLCA.resourceUnspecified;
		// // resourceUnspecified.uuid = "0d557bab-d095-4142-912e-398fccb68240";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode resourceBiotic = new TreeNode(resource);
		// resourceBiotic.nodeName = "biotic";
		// resourceBiotic.uri = FedLCA.resourceBiotic;
		// // resourceBiotic.uuid = "26305d8d-591e-4927-8e19-ca7513edcee9";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode resourceInAir = new TreeNode(resource);
		// resourceInAir.nodeName = "in air";
		// resourceInAir.uri = FedLCA.resourceIn_air;
		// // resourceInAir.uuid = "965603be-3e94-42e6-9b2c-95eaf3b998c0";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode resourceInGround = new TreeNode(resource);
		// resourceInGround.nodeName = "in ground";
		// resourceInGround.uri = FedLCA.resourceIn_ground;
		// // resourceInGround.uuid = "75c87bc3-468b-4d9f-b2c5-9d521fb4822e";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode resourceInLand = new TreeNode(resource);
		// resourceInLand.nodeName = "in land";
		// resourceInLand.uri = FedLCA.resourceIn_land;
		// // resourceInLand.uuid = "54f7604f-c04e-4404-a229-852ede4379dc";
		// // confirmUri(FedLCA.release);
		//
		// TreeNode resourceInWater = new TreeNode(resource);
		// resourceInWater.nodeName = "in water";
		// resourceInWater.uri = FedLCA.resourceIn_water;
		// // resourceInWater.uuid = "bcfc6117-3461-4f85-a5c8-fe59a533cc29";
		// // confirmUri(FedLCA.release);

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

	public static void update(int rowNumber) {
		List<DataRow> data = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey()).getData();
		DataRow dataRow = data.get(rowNumber);
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
