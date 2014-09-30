package gov.epa.nrmrl.std.lca.ht.flowContext.mgr;

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
import org.eclipse.ui.part.ViewPart;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Tree;
import com.hp.hpl.jena.rdf.model.Resource;

public class MatchContexts extends ViewPart {
	private List<String> contextsToMatch;
	private List<Resource> contextResourcesToMatch;

	private class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return new Object[0];
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	public MatchContexts() {
		// MatchContexts = this;
	}

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.flowContext.mgr.MatchContexts";
	private static Tree masterTree;
	private static TreeViewer masterTreeViewer;
	private static Label masterLbl;
	private int rowNumSelected;
	private int colNumSelected;

	// private Composite compositeMatches;
	// private Composite compositeMaster;

	@Override
	public void createPartControl(Composite parent) {
		GridLayout gl_parent = new GridLayout(1, false);
		parent.setLayout(gl_parent);

		outerComposite = new Composite(parent, SWT.NONE);
		outerComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		outerComposite.setLayout(new GridLayout(2, false));
		// ============ NEW COL =========
		Composite innerComposite = new Composite(outerComposite, SWT.NONE);
		innerComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true,
				false, 1, 1));
		innerComposite.setLayout(new GridLayout(2, false));

		unAssignButton = new Button(innerComposite, SWT.NONE);
		GridData gd_unAssignButton = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		gd_unAssignButton.widthHint = 100;
		unAssignButton.setLayoutData(gd_unAssignButton);
		unAssignButton.setText("Unassign");
		unAssignButton.addSelectionListener(unassignListener);

		assignButton = new Button(innerComposite, SWT.NONE);
		GridData gd_assignButton = new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1);
		gd_assignButton.widthHint = 100;
		assignButton.setLayoutData(gd_assignButton);
		assignButton.setText("Assign");
		assignButton.addSelectionListener(assignListener);

		masterLbl = new Label(outerComposite, SWT.NONE);
		masterLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		masterLbl.setSize(120, 14);
		masterLbl.setText("Master Flow Contexts");
		// ============ NEW COL =========
		masterTreeViewer = new TreeViewer(parent, SWT.BORDER);
		masterTree = masterTreeViewer.getTree();
		masterTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1));
		masterTree.setLinesVisible(true);

		masterTreeViewer.setLabelProvider(new ColumnLabelProvider() {
			// private Color currentColor = null;

			// @Override
			public String getText(Object treeNode) {
				return ((TreeNode) treeNode).nodeName;
			}
		});
		TreeViewerColumn masterTreeColumn = new TreeViewerColumn(
				masterTreeViewer, SWT.NONE);
		masterTreeColumn.getColumn().setWidth(300);
		masterTreeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object treeNode) {
				return ((TreeNode) treeNode).nodeName;
			}
		});

		masterTreeViewer.setContentProvider(new MyContentProvider());
		masterTreeViewer.setInput(createHarmonizeCompartments());
		masterTreeViewer.getTree().addSelectionListener(
				new SelectionListener() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						TreeNode treeNode = (TreeNode) (e.item.getData());

						if (!treeNode.hasChildern()) {
							String masterLabel = treeNode.getLabel();
							Resource masterResource = treeNode.getUri();
						}
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						// TODO Auto-generated method stub

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
			TableItem[] tableItems = CSVTableView.getTable().getSelection();
			TableItem tableItem = tableItems[0];
			String rowNumString = tableItem.getText(0);
			int rowNumber = Integer.parseInt(rowNumString) - 1;
			DataRow dataRow = TableKeeper
					.getTableProvider(CSVTableView.getTableProviderKey())
					.getData().get(rowNumber);
			dataRow.getFlowContext().setMatchingResource(null);
			FlowsWorkflow.removeMatchContextRowNum(rowNumber);
			CSVTableView.colorFlowContextRows();
			// tableItem.setBackground(color);
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

	private SelectionListener assignListener = new SelectionListener() {
		private void doit(SelectionEvent e) {
			Util.findView(CSVTableView.ID);
			Util.findView(FlowsWorkflow.ID);
			TableItem[] tableItems = CSVTableView.getTable().getSelection();
			TableItem tableItem = tableItems[0];
			String rowNumString = tableItem.getText(0);
			int rowNumber = Integer.parseInt(rowNumString) - 1;
			DataRow dataRow = TableKeeper
					.getTableProvider(CSVTableView.getTableProviderKey())
					.getData().get(rowNumber);

			TreeItem treeItem = masterTree.getSelection()[0];
			TreeNode treeNode = (TreeNode) treeItem.getData();
			Resource newResource = treeNode.getUri();
			if (newResource == null) {
				return;
			}
			dataRow.getFlowContext().setMatchingResource(newResource);
			FlowsWorkflow.addMatchContextRowNum(rowNumber);
			CSVTableView.colorFlowContextRows();
			CSVTableView.selectNextContext();
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

	private TreeNode createHarmonizeCompartments() {
		TreeNode masterCompartmentTree = new TreeNode(null);

		TreeNode release = new TreeNode(masterCompartmentTree);
		release.nodeName = "Release";
		// confirmUri(LCAHT.release);

		TreeNode air = new TreeNode(release);
		air.nodeName = "air";
		air.uri = FedLCA.airUnspecified;
		// confirmUri(FedLCA.release);

		TreeNode airLowPop = new TreeNode(air);
		airLowPop.nodeName = "low population density";
		airLowPop.uri = FedLCA.airLow_population_density;
		// confirmUri(FedLCA.release);

		TreeNode airUnspec = new TreeNode(air);
		airUnspec.nodeName = "unspecified";
		airUnspec.uri = FedLCA.airUnspecified;
		// confirmUri(FedLCA.release);

		TreeNode airHighPop = new TreeNode(air);
		airHighPop.nodeName = "high population density";
		airHighPop.uri = FedLCA.airHigh_population_density;
		// confirmUri(FedLCA.release);

		TreeNode airLowPopLongTerm = new TreeNode(air);
		airLowPopLongTerm.nodeName = "low population density, long-term";
		airLowPopLongTerm.uri = FedLCA.airLow_population_densityLong_term;
		// confirmUri(FedLCA.release);

		TreeNode airLowerStratPlusUpperTrop = new TreeNode(air);
		airLowerStratPlusUpperTrop.nodeName = "lower stratosphere + upper troposphere";
		airLowerStratPlusUpperTrop.uri = FedLCA.airLower_stratosphere_upper_troposphere;
		// confirmUri(FedLCA.release);

		TreeNode water = new TreeNode(release);
		water.nodeName = "water";
		water.uri = FedLCA.waterUnspecified;
		// confirmUri(FedLCA.release);

		TreeNode waterFossil = new TreeNode(water);
		waterFossil.nodeName = "fossil-";
		waterFossil.uri = FedLCA.waterFossil;
		// confirmUri(FedLCA.release);

		TreeNode waterFresh = new TreeNode(water);
		waterFresh.nodeName = "fresh-";
		waterFresh.uri = FedLCA.waterFresh;
		// confirmUri(FedLCA.release);

		TreeNode waterFreshLongTerm = new TreeNode(water);
		waterFreshLongTerm.nodeName = "fresh-, long-term";
		waterFreshLongTerm.uri = FedLCA.waterFreshLong_term;
		// confirmUri(FedLCA.release);

		TreeNode waterGround = new TreeNode(water);
		waterGround.nodeName = "ground-";
		waterGround.uri = FedLCA.waterGround;
		// confirmUri(FedLCA.release);

		TreeNode waterGroundLongTerm = new TreeNode(water);
		waterGroundLongTerm.nodeName = "ground-, long-term";
		waterGroundLongTerm.uri = FedLCA.waterGroundLong_term;
		// confirmUri(FedLCA.release);

		TreeNode waterLake = new TreeNode(water);
		waterLake.nodeName = "lake";
		waterLake.uri = FedLCA.waterLake;
		// confirmUri(FedLCA.release);

		TreeNode waterOcean = new TreeNode(water);
		waterOcean.nodeName = "ocean";
		waterOcean.uri = FedLCA.waterOcean;
		// confirmUri(FedLCA.release);

		TreeNode waterRiver = new TreeNode(water);
		waterRiver.nodeName = "river";
		waterRiver.uri = FedLCA.waterRiver;
		// confirmUri(FedLCA.release);

		TreeNode waterRiverLongTerm = new TreeNode(water);
		waterRiverLongTerm.nodeName = "river, long-term";
		waterRiverLongTerm.uri = FedLCA.waterRiverLong_term;
		// confirmUri(FedLCA.release);

		TreeNode waterSurface = new TreeNode(water);
		waterSurface.nodeName = "surface water";
		waterSurface.uri = FedLCA.waterSurface;
		// confirmUri(FedLCA.release);

		TreeNode waterUnspec = new TreeNode(water);
		waterUnspec.nodeName = "unspecified";
		waterUnspec.uri = FedLCA.waterUnspecified;
		// confirmUri(FedLCA.release);

		TreeNode soil = new TreeNode(release);
		soil.nodeName = "soil";
		soil.uri = FedLCA.soilUnspecified;
		// confirmUri(FedLCA.release);

		TreeNode soilAgricultural = new TreeNode(soil);
		soilAgricultural.nodeName = "agricultural";
		soilAgricultural.uri = FedLCA.soilAgricultural;
		// confirmUri(FedLCA.release);

		TreeNode soilForestry = new TreeNode(soil);
		soilForestry.nodeName = "forestry";
		soilForestry.uri = FedLCA.soilForestry;
		// confirmUri(FedLCA.release);

		TreeNode soilIndustrial = new TreeNode(soil);
		soilIndustrial.nodeName = "industrial";
		soilIndustrial.uri = FedLCA.soilIndustrial;
		// confirmUri(FedLCA.release);

		TreeNode soilUnspec = new TreeNode(soil);
		soilUnspec.nodeName = "unspecified";
		soilUnspec.uri = FedLCA.soilUnspecified;
		// confirmUri(FedLCA.release);

		TreeNode resource = new TreeNode(masterCompartmentTree);
		resource.nodeName = "Resource";
		resource.uri = FedLCA.resource;
		// confirmUri(FedLCA.release);

		TreeNode resourceBiotic = new TreeNode(resource);
		resourceBiotic.nodeName = "biotic";
		resourceBiotic.uri = FedLCA.resourceBiotic;
		// confirmUri(FedLCA.release);

		TreeNode resourceInAir = new TreeNode(resource);
		resourceInAir.nodeName = "in air";
		resourceInAir.uri = FedLCA.resourceIn_air;
		// confirmUri(FedLCA.release);

		TreeNode resourceInGround = new TreeNode(resource);
		resourceInGround.nodeName = "in ground";
		resourceInGround.uri = FedLCA.resourceIn_ground;
		// confirmUri(FedLCA.release);

		TreeNode resourceInLand = new TreeNode(resource);
		resourceInLand.nodeName = "in land";
		resourceInLand.uri = FedLCA.resourceIn_land;
		// confirmUri(FedLCA.release);

		TreeNode resourceInWater = new TreeNode(resource);
		resourceInWater.nodeName = "in water";
		resourceInWater.uri = FedLCA.resourceIn_water;
		// confirmUri(FedLCA.release);

		TreeNode resourceUnspec = new TreeNode(resource);
		resourceUnspec.nodeName = "unspecified";
		resourceUnspec.uri = FedLCA.resourceUnspecified;
		// confirmUri(FedLCA.resourceUnspecified);

		// confirmModelContanisCompartments(masterCompartmentTree);
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
			if (treeNode1.getUri() != null) {
				System.out.println("treeNode1 = " + treeNode1);
				if (resource.equals(treeNode1.getUri())) {
					return treeItem1;

				}
			}

			for (TreeItem treeItem2 : treeItem1.getItems()) {
				TreeNode treeNode2 = (TreeNode) treeItem2.getData();
				System.out.println("treeNode2 = " + treeNode2);

				if (treeNode2.getUri() != null) {
					if (resource.equals(treeNode2.getUri())) {
						return treeItem2;
					}
				}
				for (TreeItem treeItem3 : treeItem2.getItems()) {
					TreeNode treeNode3 = (TreeNode) treeItem3.getData();
					System.out.println("treeNode3 = " + treeNode3);

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

	// public void update(TableProvider tableProvider) {
	// queryTblViewer.setLabelProvider(new LabelProvider());
	// queryTblViewer.setContentProvider(new QueryContentProvider());
	// QueryModel[] queryModel = createQueryModel(tableProvider);
	// queryTblViewer.setInput(queryModel);
	// queryTblViewer.getTable().setLinesVisible(true);
	// MatchModel[] matchModel = createMatchModel(queryModel);
	// System.out.println("Created matchModel matchModel.length= " +
	// matchModel.length);
	// matchedTblViewer.setLabelProvider(new MatchLabelProvider());
	// matchedTblViewer.setContentProvider(new MatchContentProvider());
	// matchedTblViewer.setInput(matchModel);
	// matchedTblViewer.getTable().setLinesVisible(true);
	// System.out.println("masterTreeViewer.getTree().getColumnCount()= "
	// + masterTreeViewer.getTree().getColumnCount());
	// System.out.println("masterTreeViewer.getTree().getItems().length= "
	// + masterTreeViewer.getTree().getItems().length);
	// System.out.println("masterTreeViewer.getTree().getItemCount()= " +
	// masterTreeViewer.getTree().getItemCount());
	// }
	//
	// private class MatchLabelProvider extends LabelProvider {
	//
	// @Override
	// public String getText(Object element) {
	// return element == null ? "" : ((MatchModel) element).getLabel();
	// }
	//
	// }

	// private class QueryContentProvider implements IStructuredContentProvider
	// {
	//
	// @Override
	// public Object[] getElements(Object inputElement) {
	// return (QueryModel[]) inputElement;
	// }
	//
	// @Override
	// public void dispose() {
	// }
	//
	// @Override
	// public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	// {
	// }
	//
	// }
	//
	// private class MatchContentProvider implements IStructuredContentProvider
	// {
	//
	// @Override
	// public Object[] getElements(Object inputElement) {
	// return (MatchModel[]) inputElement;
	// }
	//
	// @Override
	// public void dispose() {
	// }
	//
	// @Override
	// public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	// {
	// }
	//
	// }
	//
	// private QueryModel[] createQueryModel() {
	// int rows = contextsToMatch.size();
	// QueryModel[] elements = new QueryModel[rows];
	// int index = 0;
	// for (String contextConcat : contextsToMatch) {
	// String value = contextConcat;
	// Resource resource = contextResourcesToMatch.get(index);
	// // String value = dataRow.get(0);
	// QueryModel queryModel = new QueryModel(value);
	// queryModel.uri = resource;
	// elements[index++] = queryModel;
	// // index++;
	// }
	// return elements;
	// }
	//
	// private QueryModel[] createQueryModel(TableProvider tableProvider) {
	// int rows = tableProvider.getData().size();
	// QueryModel[] elements = new QueryModel[rows];
	// int index = 0;
	// for (DataRow dataRow : tableProvider.getData()) {
	// String value = dataRow.get(0);
	// QueryModel queryModel = new QueryModel(value);
	// // AnonId uri = new AnonId(dataRow.get(1)); // MIGHT THIS WORK?
	// Resource queryCompartmentResource = null;
	// // Resource fred = (Resource)uri;
	// // thing = ActiveTDB.tdbModel.getResource(fred );
	// ResIterator iterator =
	// (ActiveTDB.tdbModel.listSubjectsWithProperty(RDF.type,
	// FASC.Compartment));
	// while (iterator.hasNext()) {
	// Resource resource = iterator.next();
	// if (resource.isAnon()) {
	// AnonId anonId = (AnonId) resource.getId();
	// if (dataRow.get(1).equals(anonId.toString())) {
	// queryCompartmentResource = resource;
	// System.out.println("index = " + index);
	// System.out.println("anonId.toString() =" + anonId.toString());
	// System.out.println("anonId.getLabelString() =" +
	// anonId.getLabelString());
	// System.out.println("dataRow.get(1) = " + dataRow.get(1));
	// }
	// }
	// }
	// queryModel.setUri(queryCompartmentResource);
	// elements[index++] = queryModel;
	// }
	// return elements;
	// }

	// public class QueryModel {
	// private String label = "";
	// private Resource uri = null;
	//
	// public QueryModel(String label) {
	// this.label = label;
	// }
	//
	// public void setUri(Resource uri) {
	// this.uri = uri;
	// }
	//
	// public Resource getUri() {
	// return uri;
	// }
	//
	// @Override
	// public String toString() {
	// return label;
	// }
	// }

	// private MatchModel[] createMatchModel(QueryModel[] queryModel) {
	// int rows = queryModel.length;
	// MatchModel[] matchModel = new MatchModel[rows];
	// for (int i = 0; i < matchModel.length; i++) {
	// // matchModel[i] = null;
	// matchModel[i] = new MatchModel();
	// matchModel[i].setLabel("");
	// }
	// return matchModel;
	// }

	public List<String> getContextsToMatch() {
		return contextsToMatch;
	}

	public void setContextsToMatch(List<String> contexts) {
		contextsToMatch = contexts;
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

	private MouseListener columnMouseListener = new MouseListener() {

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			System.out.println("double click event :e =" + e);
		}

		@Override
		public void mouseDown(MouseEvent e) {
			System.out.println("mouse down event :e =" + e);
			if (e.button == 1) {
				leftClick(e);
			} else if (e.button == 3) {
				// queryTbl.deselectAll();
				rightClick(e);
			}
		}

		@Override
		public void mouseUp(MouseEvent e) {
			System.out.println("mouse up event :e =" + e);
		}

		private void leftClick(MouseEvent event) {
			System.out.println("cellSelectionMouseDownListener event " + event);
			Point ptLeft = new Point(1, event.y);
			Point ptClick = new Point(event.x, event.y);
			int clickedRow = 0;
			int clickedCol = 0;
			// TableItem item = queryTbl.getItem(ptLeft);
			// if (item == null) {
			// return;
			// }
			// clickedRow = queryTbl.indexOf(item);
			// clickedCol = getTableColumnNumFromPoint(clickedRow, ptClick);
			// if (clickedCol > 0) {
			// queryTbl.deselectAll();
			// return;
			// }
			// queryTbl.select(clickedRow);
			rowNumSelected = clickedRow;
			colNumSelected = clickedCol;
			System.out.println("rowNumSelected = " + rowNumSelected);
			System.out.println("colNumSelected = " + colNumSelected);
			// rowMenu.setVisible(true);
		}

		private void rightClick(MouseEvent event) {
			System.out.println("cellSelectionMouseDownListener event " + event);
			Point ptLeft = new Point(1, event.y);
			Point ptClick = new Point(event.x, event.y);
			int clickedRow = 0;
			int clickedCol = 0;
			// TableItem item = queryTbl.getItem(ptLeft);
			// if (item == null) {
			// return;
			// }
			// clickedRow = queryTbl.indexOf(item);
			// clickedCol = getTableColumnNumFromPoint(clickedRow, ptClick);
			// int dataClickedCol = clickedCol - 1;
			if (clickedCol < 0) {
				return;
			}

			rowNumSelected = clickedRow;
			colNumSelected = clickedCol;
			System.out.println("rowNumSelected = " + rowNumSelected);
			System.out.println("colNumSelected = " + colNumSelected);
		}
	};
	private Composite outerComposite;
	private Button unAssignButton;
	private Button assignButton;

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

	public List<Resource> getContextResourcesToMatch() {
		return contextResourcesToMatch;
	}

	public void setContextResourcesToMatch(
			List<Resource> contextResourcesToMatch) {
		this.contextResourcesToMatch = contextResourcesToMatch;
	}

	public static void update(Integer dataRowNum) {
		TableItem tableItem = CSVTableView.getTable().getSelection()[0];
		String rowNumString = tableItem.getText(0);
		int rowNumber = Integer.parseInt(rowNumString) - 1;
		DataRow dataRow = TableKeeper
				.getTableProvider(CSVTableView.getTableProviderKey()).getData()
				.get(rowNumber);
		Resource contextResource = dataRow.getFlowContext().getMatchingResource();
		if (contextResource != null) {
			// masterTree.setSelection(getTreeNodeByURI(contextResource));
			TreeItem treeItem = getTreeItemByURI(contextResource);
			if (treeItem != null) {
				masterTree.setSelection(getTreeItemByURI(contextResource));
			} else {
				masterTree.deselectAll();
			}
		} else {
			masterTree.deselectAll();
		}
		// matchFlowContextResource

	}
}
