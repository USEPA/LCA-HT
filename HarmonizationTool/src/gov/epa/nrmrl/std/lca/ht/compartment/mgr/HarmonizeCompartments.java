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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.jface.viewers.TableViewer;

public class HarmonizeCompartments extends ViewPart {
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
		GridData gd_queryTbl = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_queryTbl.minimumWidth = 300;
		queryTbl.setLayoutData(gd_queryTbl);
		
		matchedTblViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		matchedTbl = matchedTblViewer.getTable();
		GridData gd_matchedTbl = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_matchedTbl.minimumWidth = 300;
		matchedTbl.setLayoutData(gd_matchedTbl);
		
		masterTreeViewer = new TreeViewer(parent, SWT.BORDER);
		masterTree = masterTreeViewer.getTree();
		GridData gd_masterTree = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_masterTree.minimumWidth = 400;
		masterTree.setLayoutData(gd_masterTree);
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

		for(TreeItem item:masterTree.getItems()){
			expandItem(item);
		}
		masterTreeViewer.refresh();
	}
	
	private void expandItem(TreeItem item){
		System.out.println("Item expanded: item.getText() "+item.getText());
		item.setExpanded(true);
		masterTreeViewer.refresh();
		for(TreeItem child:item.getItems()){
			expandItem(child);
		}
	}	
	private TreeNode createHarmonizeCompartments() {
		TreeNode masterCompartmentTree = new TreeNode(null);

		TreeNode release = new TreeNode(masterCompartmentTree);
		release.nodeName = "Release";

		TreeNode air = new TreeNode(release);
		air.nodeName = "air";
		TreeNode lowPop = new TreeNode(air);
		lowPop.nodeName = "low population density";
		TreeNode airUnspec = new TreeNode(air);
		airUnspec.nodeName = "unspecified";
		TreeNode airHighPop = new TreeNode(air);
		airHighPop.nodeName = "high population density";
		TreeNode airLowPopLongTerm = new TreeNode(air);
		airLowPopLongTerm.nodeName = "low population density, long-term";
		TreeNode airLowerStratPlusUpperTrop = new TreeNode(air);
		airLowerStratPlusUpperTrop.nodeName = "lower stratosphere + upper troposphere";

		TreeNode water = new TreeNode(release);
		water.nodeName = "water";
		TreeNode waterFossil = new TreeNode(water);
		waterFossil.nodeName = "fossil-";
		TreeNode waterFresh = new TreeNode(water);
		waterFresh.nodeName = "fresh-";
		TreeNode waterFreshLongTerm = new TreeNode(water);
		waterFreshLongTerm.nodeName = "fresh-, long-term";
		TreeNode waterGround = new TreeNode(water);
		waterGround.nodeName = "ground-";
		TreeNode waterGroundLongTerm = new TreeNode(water);
		waterGroundLongTerm.nodeName = "ground-, long-term";
		TreeNode waterLake = new TreeNode(water);
		waterLake.nodeName = "lake";
		TreeNode waterOcean = new TreeNode(water);
		waterOcean.nodeName = "ocean";
		TreeNode waterRiver = new TreeNode(water);
		waterRiver.nodeName = "river";
		TreeNode waterRiverLongTerm = new TreeNode(water);
		waterRiverLongTerm.nodeName = "river, long-term";
		TreeNode waterSurface = new TreeNode(water);
		waterSurface.nodeName = "surface water";
		TreeNode waterUnspec = new TreeNode(water);
		waterUnspec.nodeName = "unspecified";

		TreeNode soil = new TreeNode(release);
		soil.nodeName = "soil";
		TreeNode soilAgricultural = new TreeNode(soil);
		soilAgricultural.nodeName = "agricultural";
		TreeNode soilForestry = new TreeNode(soil);
		soilForestry.nodeName = "forestry";
		TreeNode soilIndustrial = new TreeNode(soil);
		soilIndustrial.nodeName = "industrial";
		TreeNode soilUnspec = new TreeNode(soil);
		soilUnspec.nodeName = "unspecified";

		TreeNode resource = new TreeNode(masterCompartmentTree);
		resource.nodeName = "Resource";

		TreeNode resourceBiotic = new TreeNode(resource);
		resourceBiotic.nodeName = "biotic";
		TreeNode resourceInAir = new TreeNode(resource);
		resourceInAir.nodeName = "in air";
		TreeNode resourceInGround = new TreeNode(resource);
		resourceInGround.nodeName = "in ground";
		TreeNode resourceInLand = new TreeNode(resource);
		resourceInLand.nodeName = "in land";
		TreeNode resourceInWater = new TreeNode(resource);
		resourceInWater.nodeName = "in water";
		TreeNode resourceUnspec = new TreeNode(resource);
		resourceUnspec.nodeName = "unspecified";
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
	private QueryModel[] createQueryModel(TableProvider tableProvider) {
		int rows =tableProvider.getData().size();
		QueryModel[] elements = new QueryModel[rows];
		int index = 0;
		for(DataRow dataRow : tableProvider.getData()){
			String value = dataRow.get(0);
			elements[index++] = new QueryModel(value);
		}
		return elements;
	}
	public class QueryModel {
		private String label = "";
		
		public QueryModel(String label){
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}

}

