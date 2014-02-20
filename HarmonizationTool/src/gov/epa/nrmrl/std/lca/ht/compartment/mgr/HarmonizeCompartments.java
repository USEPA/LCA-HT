package gov.epa.nrmrl.std.lca.ht.compartment.mgr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

//import harmonizationtool.ColumnLabelProvider;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.TableProvider;
import harmonizationtool.tree.Node;
import harmonizationtool.vocabulary.LCAHT;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.jface.viewers.TableViewer;

import com.hp.hpl.jena.rdf.model.Resource;

public class HarmonizeCompartments extends ViewPart {
	private static class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return new Object[0];
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	public HarmonizeCompartments() {
	}

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.compartment.mgr.HarmonizeCompartments";
	private Table queryTbl;
	private TableViewer queryTblViewer;
	private Table matchedTbl;
	private TableViewer matchedTblViewer;
	private Tree masterTree;
	private TreeViewer masterTreeViewer;
	private Label queryLbl;
	private Label matchedLbl;
	private Label masterLbl;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(5, false));
		new Label(parent, SWT.NONE);

		queryLbl = new Label(parent, SWT.NONE);
		queryLbl.setText("Query Compartments");
		queryLbl.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 1, 1));

		matchedLbl = new Label(parent, SWT.NONE);
		matchedLbl.setText("Matched");
		matchedLbl.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 1, 1));

		masterLbl = new Label(parent, SWT.NONE);
		masterLbl.setText("Master Compartments");
		masterLbl.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 1, 1));

		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);

		queryTblViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		queryTbl = queryTblViewer.getTable();
		queryTbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		queryTblViewer.setContentProvider(new ContentProvider());

		matchedTblViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		matchedTbl = matchedTblViewer.getTable();
		matchedTbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		matchedTblViewer.setContentProvider(new ContentProvider());

		masterTreeViewer = new TreeViewer(parent, SWT.BORDER);
		masterTree = masterTreeViewer.getTree();
		masterTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		masterTree.setLinesVisible(true);

		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		masterTreeViewer.setLabelProvider(new ColumnLabelProvider() {
			// private Color currentColor = null;

			// @Override
			public String getText(Object treeNode) {
				return ((TreeNode) treeNode).nodeName;
			}
		});
		masterTreeViewer.setContentProvider(new MyContentProvider());
		masterTreeViewer.setInput(createHarmonizeCompartments());
		masterTreeViewer.getTree().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeNode treeNode = (TreeNode) (e.item.getData());

				if (!treeNode.hasChildern()) {
					String masterLabel = treeNode.getLabel();
					if (queryTblViewer.getTable().getItemCount() > 0) {
						int row = queryTblViewer.getTable().getSelectionIndex();
						if (row > -1) {
//							String queryLabel = queryTblViewer.getTable().getSelection()[0].getText(0);
							MatchModel[] matchedModel = (MatchModel[]) (matchedTblViewer.getInput());
							matchedModel[row].setLabel(masterLabel);
							matchedTblViewer.refresh();
						}
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		for (TreeItem item : masterTree.getItems()) {
			expandItem(item);
		}
		masterTreeViewer.refresh();
	}

	private void expandItem(TreeItem item) {
		System.out.println("Item expanded: item.getText() " + item.getText());
		item.setExpanded(true);
		masterTreeViewer.refresh();
		for (TreeItem child : item.getItems()) {
			expandItem(child);
		}
	}

	private TreeNode createHarmonizeCompartments() {
		TreeNode masterCompartmentTree = new TreeNode(null);

		TreeNode release = new TreeNode(masterCompartmentTree);
		release.nodeName = "Release";
		release.uri = LCAHT.release;

		TreeNode air = new TreeNode(release);
		air.nodeName = "air";
		air.uri = LCAHT.airUnspecified;

		TreeNode airLowPop = new TreeNode(air);
		airLowPop.nodeName = "low population density";
		airLowPop.uri = LCAHT.airLow_population_density;

		TreeNode airUnspec = new TreeNode(air);
		airUnspec.nodeName = "unspecified";
		airUnspec.uri = LCAHT.airUnspecified;

		TreeNode airHighPop = new TreeNode(air);
		airHighPop.nodeName = "high population density";
		airHighPop.uri = LCAHT.airHigh_population_density;

		TreeNode airLowPopLongTerm = new TreeNode(air);
		airLowPopLongTerm.nodeName = "low population density, long-term";
		airLowPopLongTerm.uri = LCAHT.airLow_population_densityLong_term;

		TreeNode airLowerStratPlusUpperTrop = new TreeNode(air);
		airLowerStratPlusUpperTrop.nodeName = "lower stratosphere + upper troposphere";
		airLowerStratPlusUpperTrop.uri = LCAHT.airLower_stratosphere_upper_troposphere;

		TreeNode water = new TreeNode(release);
		water.nodeName = "water";
		water.uri = LCAHT.waterUnspecified;

		TreeNode waterFossil = new TreeNode(water);
		waterFossil.nodeName = "fossil-";
		waterFossil.uri = LCAHT.waterFossil;

		TreeNode waterFresh = new TreeNode(water);
		waterFresh.nodeName = "fresh-";
		waterFresh.uri = LCAHT.waterFresh;

		TreeNode waterFreshLongTerm = new TreeNode(water);
		waterFreshLongTerm.nodeName = "fresh-, long-term";
		waterFreshLongTerm.uri = LCAHT.waterFreshLong_term;

		TreeNode waterGround = new TreeNode(water);
		waterGround.nodeName = "ground-";
		waterGround.uri = LCAHT.waterGround;

		TreeNode waterGroundLongTerm = new TreeNode(water);
		waterGroundLongTerm.nodeName = "ground-, long-term";
		waterGroundLongTerm.uri = LCAHT.waterGroundLong_term;

		TreeNode waterLake = new TreeNode(water);
		waterLake.nodeName = "lake";
		waterLake.uri = LCAHT.waterLake;

		TreeNode waterOcean = new TreeNode(water);
		waterOcean.nodeName = "ocean";
		waterOcean.uri = LCAHT.waterOcean;

		TreeNode waterRiver = new TreeNode(water);
		waterRiver.nodeName = "river";
		waterRiver.uri = LCAHT.waterRiver;

		TreeNode waterRiverLongTerm = new TreeNode(water);
		waterRiverLongTerm.nodeName = "river, long-term";
		waterRiverLongTerm.uri = LCAHT.waterRiverLong_term;

		TreeNode waterSurface = new TreeNode(water);
		waterSurface.nodeName = "surface water";
		waterSurface.uri = LCAHT.waterSurface;

		TreeNode waterUnspec = new TreeNode(water);
		waterUnspec.nodeName = "unspecified";
		waterUnspec.uri = LCAHT.waterUnspecified;

		TreeNode soil = new TreeNode(release);
		soil.nodeName = "soil";
		soil.uri = LCAHT.soilUnspecified;

		TreeNode soilAgricultural = new TreeNode(soil);
		soilAgricultural.nodeName = "agricultural";
		soilAgricultural.uri = LCAHT.soilAgricultural;

		TreeNode soilForestry = new TreeNode(soil);
		soilForestry.nodeName = "forestry";
		soilForestry.uri = LCAHT.soilForestry;

		TreeNode soilIndustrial = new TreeNode(soil);
		soilIndustrial.nodeName = "industrial";
		soilIndustrial.uri = LCAHT.soilIndustrial;

		TreeNode soilUnspec = new TreeNode(soil);
		soilUnspec.nodeName = "unspecified";
		soilUnspec.uri = LCAHT.soilUnspecified;

		TreeNode resource = new TreeNode(masterCompartmentTree);
		resource.nodeName = "Resource";
		resource.uri = LCAHT.resource;

		TreeNode resourceBiotic = new TreeNode(resource);
		resourceBiotic.nodeName = "biotic";
		resourceBiotic.uri = LCAHT.resourceBiotic;

		TreeNode resourceInAir = new TreeNode(resource);
		resourceInAir.nodeName = "in air";
		resourceInAir.uri = LCAHT.resourceIn_air;

		TreeNode resourceInGround = new TreeNode(resource);
		resourceInGround.nodeName = "in ground";
		resourceInGround.uri = LCAHT.resourceIn_ground;

		TreeNode resourceInLand = new TreeNode(resource);
		resourceInLand.nodeName = "in land";
		resourceInLand.uri = LCAHT.resourceIn_land;

		TreeNode resourceInWater = new TreeNode(resource);
		resourceInWater.nodeName = "in water";
		resourceInWater.uri = LCAHT.resourceIn_water;

		TreeNode resourceUnspec = new TreeNode(resource);
		resourceUnspec.nodeName = "unspecified";
		resourceUnspec.uri = LCAHT.resourceUnspecified;
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
		queryTblViewer.getControl().setFocus();

	}

	public void update(TableProvider tableProvider) {
		queryTblViewer.setLabelProvider(new LabelProvider());
		queryTblViewer.setContentProvider(new QueryContentProvider());
		QueryModel[] queryModel = createQueryModel(tableProvider);
		queryTblViewer.setInput(queryModel);
		queryTblViewer.getTable().setLinesVisible(true);
		MatchModel[] matchModel = createMatchModel(queryModel);
		System.out.println("Created matchModel matchModel.length= " + matchModel.length);
		matchedTblViewer.setLabelProvider(new MatchLabelProvider());
		matchedTblViewer.setContentProvider(new MatchContentProvider());
		matchedTblViewer.setInput(matchModel);
		matchedTblViewer.getTable().setLinesVisible(true);
	}

	private class MatchLabelProvider extends LabelProvider {

		@Override
		public String getText(Object element) {
			return element == null ? "" : ((MatchModel) element).getLabel();
		}

	}

	private class QueryContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return (QueryModel[]) inputElement;
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	private class MatchContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return (MatchModel[]) inputElement;
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	private QueryModel[] createQueryModel(TableProvider tableProvider) {
		int rows = tableProvider.getData().size();
		QueryModel[] elements = new QueryModel[rows];
		int index = 0;
		for (DataRow dataRow : tableProvider.getData()) {
			String value = dataRow.get(0);
			elements[index++] = new QueryModel(value);
		}
		return elements;
	}

	public class QueryModel {
		private String label = "";

		public QueryModel(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	private MatchModel[] createMatchModel(QueryModel[] queryModel) {
		int rows = queryModel.length;
		MatchModel[] matchModel = new MatchModel[rows];
		for (int i = 0; i < matchModel.length; i++) {
			// matchModel[i] = null;
			matchModel[i] = new MatchModel();
			matchModel[i].setLabel("");
		}
		return matchModel;
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

}
