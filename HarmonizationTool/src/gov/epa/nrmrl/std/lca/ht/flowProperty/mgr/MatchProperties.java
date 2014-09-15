package gov.epa.nrmrl.std.lca.ht.flowProperty.mgr;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.Node;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.TreeNode;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.jface.viewers.TableViewer;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.TableColumn;

public class MatchProperties extends ViewPart {
	private static Button btnCommitMatches;
	private List<String> propertiesToMatch;
	private List<Resource> propertyResourcesToMatch;

	private static class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return new Object[0];
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	public MatchProperties() {
	}

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.MatchProperties";
	private Table queryTbl;
	private TableViewer queryTblViewer;
	private Table matchedTbl;
	private TableViewer matchedTblViewer;
	private Tree masterTree;
	private TreeViewer masterTreeViewer;
	private Label queryLbl;
	private Label matchedLbl;
	private Label masterLbl;

	// private Composite compositeMatches;
	// private Composite compositeMaster;

	@Override
	public void createPartControl(Composite parent) {
		GridLayout gl_parent = new GridLayout(4, false);
		parent.setLayout(gl_parent);
		// ================== ROW 1 ==========================
		// ============ NEW COL =========
		new Label(parent, SWT.NONE);
		// ============ NEW COL =========
		Composite compositeQuery = new Composite(parent, SWT.NONE);
		compositeQuery.setLayout(new FillLayout(SWT.HORIZONTAL));
		compositeQuery.setSize(300, 30);
		compositeQuery.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		queryLbl = new Label(compositeQuery, SWT.NONE);
		queryLbl.setAlignment(SWT.CENTER);
		queryLbl.setText("Query Contexts");
		// ============ NEW COL =========
		Composite compositeMatches = new Composite(parent, SWT.NONE);
		compositeMatches.setLayout(new GridLayout(4, false));
		// gd_compositeMatches.minimumWidth = 300;
		compositeMatches.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		matchedLbl = new Label(compositeMatches, SWT.NONE);
		matchedLbl.setText("Matched");
		new Label(compositeMatches, SWT.NONE);
		new Label(compositeMatches, SWT.NONE);

		btnCommitMatches = new Button(compositeMatches, SWT.CENTER);
		btnCommitMatches.setText("Commit Matches");
		btnCommitMatches.addSelectionListener(new SelectionListener() {

			// @Override
			public void widgetSelected(SelectionEvent e) {
				QueryModel[] queryModel = (QueryModel[]) queryTblViewer.getInput();
				System.out.println("queryModel.length = " + queryModel.length);
				System.out.println("queryModel[0] = " + queryModel[0]);

				// Resource annotation = FedLCA.Annotation;
				// Property isA = RDF.type;
				// Resource Class = OWL.Class;
				// Property creator = DCTerms.creator;
				// Property dateSubmitted = DCTerms.dateSubmitted;
				// Property hasComparison = FedLCA.hasComparison;
				//
				// Resource comparison = FedLCA.Comparison;
				// Property comparedSource = FedLCA.comparedSource;
				// Property comparedMaster = FedLCA.comparedMaster;
				// Property comparedEquivalence = FedLCA.comparedEquivalence;
				// Resource equivalent = FedLCA.equivalent;

				Model model = ActiveTDB.tdbModel;
				// SHOULD MAKE A CLASS FOR Annotation (WITH AUTOMATIC SYNCING
				// WITH TDB) FIXME
				// NEED TO DO THE FOLLOWING
				// 1) Create a new Annotation (assigning it to the class
				// Annotation)
				// 2) Assign to it a date and creator
				Resource annotationResource = null;
				if (queryModel.length > 0) {
					// NEED TO DO THE FOLLOWING
					// 1) Create a new Annotation (assigning it to the class
					// Annotation)
					annotationResource = model.createResource();
					model.add(annotationResource, RDF.type, FedLCA.Annotation);
					// 2) Assign to it a date and creator
					Date calendar = new Date();
					Literal dateLiteral = model.createTypedLiteral(calendar);
					model.add(annotationResource, DCTerms.dateSubmitted, dateLiteral);
					if (Util.getPreferenceStore().getString("userName") != null) {
						Literal userName = model.createLiteral(Util.getPreferenceStore().getString("userName"));
						model.add(annotationResource, DCTerms.creator, userName);
					}
				}
				// 3) Loop through each match
				MatchModel[] matchModel = (MatchModel[]) matchedTblViewer.getInput();
				System.out.println("matchModel.length= " + matchModel.length);
				for (int i = 0; i < queryModel.length; i++) {
					QueryModel qModel = queryModel[i];
					// String qString = qModel.label;
					MatchModel mModel = matchModel[i];
					if (mModel != null) {
						System.out.println("matchRow[" + i + "].label = " + mModel.label);
						System.out.println("matchRow.getResource() = " + mModel.getResource());
						// System.out.println("matchRow.getResource().getLocalName() = "+matchRow.getResource().getLocalName());
						// System.out.println("matchRow["+i+"].resource.getLocalName() = "+matchRow.resource.getLocalName());
						// A) Find the Source URI
						// B) Find the Master URI
						Resource queryPropertyResource = qModel.getUri();
						Resource masterPropertyResource = mModel.resource;
						// confirmResource(queryPropertyResource);
						// confirmResource(masterPropertyResource);

						if (masterPropertyResource == null) {
							continue;
						}
						System.out.println("index i =  " + i);
						// C) Create a new Comparison (assigning it to the class
						// Comparison)
						// D) Connect the Annotation to the Comparison
						// E) Create 3 triples for that Comparison: Source,
						// Master, Equivalence

						Resource comparisonResource = model.createResource();
						model.add(comparisonResource, RDF.type, FedLCA.Comparison);
						model.add(annotationResource, FedLCA.hasComparison, comparisonResource);
						model.add(comparisonResource, FedLCA.comparedSource, queryPropertyResource);
						model.add(comparisonResource, FedLCA.comparedMaster, masterPropertyResource);
						model.add(comparisonResource, FedLCA.comparedEquivalence, FedLCA.equivalent);

					} else {
						System.out.println("matchModel[" + i + "] is null!");
					}
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		// ============ NEW COL =========
		Composite compositeMaster = new Composite(parent, SWT.NONE);
		compositeMaster.setLayout(new GridLayout(1, false));
		GridData gd_compositeMaster = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_compositeMaster.minimumWidth = 300;
		compositeMaster.setLayoutData(gd_compositeMaster);

		masterLbl = new Label(compositeMaster, SWT.NONE);
		masterLbl.setText("Master Flow Properties");

		Button btnAutoAdvance = new Button(compositeMaster, SWT.CHECK);
		btnAutoAdvance.setText("Auto Advance");
		// ================== ROW 2 ==========================
		// ============ NEW COL =========
		new Label(parent, SWT.NONE);
		// ============ NEW COL =========
		queryTblViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		queryTbl = queryTblViewer.getTable();
		GridData gd_queryTbl = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_queryTbl.widthHint = 300;
		queryTbl.setLayoutData(gd_queryTbl);
		queryTblViewer.setContentProvider(new ContentProvider());
		TableViewerColumn queryColumn = new TableViewerColumn(queryTblViewer, SWT.NONE);
		TableColumn qColumn = queryColumn.getColumn();
		qColumn.setMoveable(true);
		qColumn.setAlignment(SWT.RIGHT);
		qColumn.setWidth(600);
		queryColumn.setLabelProvider(new ColumnLabelProvider() {
			// @Override
			// public String getText(Object treeNode) {
			// return ((TreeNode) treeNode).nodeName;
			// }
		});
		// ============ NEW COL =========
		matchedTblViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		matchedTbl = matchedTblViewer.getTable();
		GridData gd_matchedTbl = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_matchedTbl.widthHint = 300;
		matchedTbl.setLayoutData(gd_matchedTbl);
		matchedTblViewer.setContentProvider(new ContentProvider());
		TableViewerColumn matchColumn = new TableViewerColumn(matchedTblViewer, SWT.NONE);
		TableColumn mColumn = matchColumn.getColumn();
		mColumn.setMoveable(true);
		mColumn.setAlignment(SWT.RIGHT);
		mColumn.setWidth(600);
		// matchColumn.getColumn().setWidth(300);
		matchColumn.setLabelProvider(new ColumnLabelProvider() {
			// @Override
			// public String getText(Object treeNode) {
			// return ((TreeNode) treeNode).nodeName;
			// }
		});
		// ============ NEW COL =========
		masterTreeViewer = new TreeViewer(parent, SWT.BORDER);
		masterTree = masterTreeViewer.getTree();
		GridData gd_masterTree = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_masterTree.widthHint = 300;
		masterTree.setLayoutData(gd_masterTree);
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
		masterTreeViewer.setInput(createHarmonizeProperties());
		masterTreeViewer.getTree().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeNode treeNode = (TreeNode) (e.item.getData());

				if (!treeNode.hasChildern()) {
					String masterLabel = treeNode.getLabel();
					Resource masterResource = treeNode.getUri();
					if (queryTblViewer.getTable().getItemCount() > 0) {
						int row = queryTblViewer.getTable().getSelectionIndex();
						if (row > -1) {
							// String queryLabel =
							// queryTblViewer.getTable().getSelection()[0].getText(0);
							MatchModel[] matchedModel = (MatchModel[]) (matchedTblViewer.getInput());
							matchedModel[row].setLabel(masterLabel);
							matchedModel[row].setResource(masterResource);
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
		masterTreeViewer.refresh();

		for (TreeItem item : masterTree.getItems()) {
			expandItem(item);
		}
	}

	private void expandItem(TreeItem item) {
		System.out.println("Item expanded: item.getText() " + item.getText());
		item.setExpanded(true);
		masterTreeViewer.refresh();
		for (TreeItem child : item.getItems()) {
			expandItem(child);
		}
	}

	private TreeNode createHarmonizeProperties() {
		TreeNode masterPropertyTree = new TreeNode(null);

		// -------- PHYSICAL COMBINED
		TreeNode physicalIndividual = new TreeNode(masterPropertyTree);
		physicalIndividual.nodeName = "Physical individual";

		TreeNode mass = new TreeNode(physicalIndividual);
		mass.nodeName = "Mass";
		mass.uri = FedLCA.Mass;

		TreeNode area = new TreeNode(physicalIndividual);
		area.nodeName = "Area";
		area.uri = FedLCA.Area;

		TreeNode volume = new TreeNode(physicalIndividual);
		volume.nodeName = "Volume";
		volume.uri = FedLCA.Volume;

		TreeNode duration = new TreeNode(physicalIndividual);
		duration.nodeName = "Duration";
		duration.uri = FedLCA.Duration;

		TreeNode energy = new TreeNode(physicalIndividual);
		energy.nodeName = "Energy";
		energy.uri = FedLCA.Energy;

		TreeNode radioactivity = new TreeNode(physicalIndividual);
		radioactivity.nodeName = "Radioactivity";
		radioactivity.uri = FedLCA.Radioactivity;

		// -------- PHYSICAL COMBINED
		TreeNode physicalCombined = new TreeNode(masterPropertyTree);
		physicalCombined.nodeName = "Physical combined";

		TreeNode volumeTime = new TreeNode(physicalCombined);
		volumeTime.nodeName = "Volume*time";
		volumeTime.uri = FedLCA.VolumeTime;

		TreeNode massTime = new TreeNode(physicalCombined);
		massTime.nodeName = "Mass*time";
		massTime.uri = FedLCA.MassTime;

		TreeNode volumeLength = new TreeNode(physicalCombined);
		volumeLength.nodeName = "Volume*Length";
		volumeLength.uri = FedLCA.VolumeLength;

		TreeNode areaTime = new TreeNode(physicalCombined);
		areaTime.nodeName = "Area*time";
		areaTime.uri = FedLCA.AreaTime;

		TreeNode lengthTime = new TreeNode(physicalCombined);
		lengthTime.nodeName = "Length*time";
		lengthTime.uri = FedLCA.LengthTime;

		TreeNode energyPerMassTime = new TreeNode(physicalCombined);
		energyPerMassTime.nodeName = "Energy/mass*time";
		energyPerMassTime.uri = FedLCA.EnergyPerMassTime;

		TreeNode energyPerAreaTime = new TreeNode(physicalCombined);
		energyPerAreaTime.nodeName = "Energy/area*time";
		energyPerAreaTime.uri = FedLCA.EnergyPerAreaTime;

		// -------- OTHER
		TreeNode other = new TreeNode(masterPropertyTree);
		other.nodeName = "Other";

		TreeNode itemCount = new TreeNode(other);
		itemCount.nodeName = "Number of Items";
		itemCount.uri = FedLCA.ItemCount;

		TreeNode itemsLength = new TreeNode(other);
		itemsLength.nodeName = "Items*Length";
		itemsLength.uri = FedLCA.ItemsLength;

		TreeNode goodsTransportMassDistance = new TreeNode(other);
		goodsTransportMassDistance.nodeName = "Goods transport (mass*distance)";
		goodsTransportMassDistance.uri = FedLCA.GoodsTransportMassDistance;

		TreeNode personTransport = new TreeNode(other);
		personTransport.nodeName = "Person transport";
		personTransport.uri = FedLCA.PersonTransport;

		TreeNode vehicleTransport = new TreeNode(other);
		vehicleTransport.nodeName = "Vehicle transport";
		vehicleTransport.uri = FedLCA.VehicleTransport;

		TreeNode netCalorificValue = new TreeNode(other);
		netCalorificValue.nodeName = "Net calorific value";
		netCalorificValue.uri = FedLCA.NetCalorificValue;

		TreeNode grossCalorificValue = new TreeNode(other);
		grossCalorificValue.nodeName = "Gross calorific value";
		grossCalorificValue.uri = FedLCA.GrossCalorificValue;

		TreeNode normalVolume = new TreeNode(other);
		normalVolume.nodeName = "Normal Volume";
		normalVolume.uri = FedLCA.NormalVolume;

		return masterPropertyTree;
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

	public void update() {
		LabelProvider labelProvider = new LabelProvider();
		if (queryTblViewer == null) {
			System.out.println("Why is this null, now?");
		} else {
			System.out.println("queryTblViewer = " + queryTblViewer);
		}
		queryTblViewer.setLabelProvider(labelProvider);
		queryTblViewer.setContentProvider(new QueryContentProvider());
		QueryModel[] queryModel = createQueryModel();
		queryTblViewer.setInput(queryModel);
		queryTblViewer.getTable().setLinesVisible(true);
		MatchModel[] matchModel = createMatchModel(queryModel);
		System.out.println("Created matchModel matchModel.length= " + matchModel.length);
		matchedTblViewer.setLabelProvider(new MatchLabelProvider());
		matchedTblViewer.setContentProvider(new MatchContentProvider());
		matchedTblViewer.setInput(matchModel);
		matchedTblViewer.getTable().setLinesVisible(true);
		System.out.println("masterTreeViewer.getTree().getColumnCount()= "
				+ masterTreeViewer.getTree().getColumnCount());
		System.out.println("masterTreeViewer.getTree().getItems().length= "
				+ masterTreeViewer.getTree().getItems().length);
		System.out.println("masterTreeViewer.getTree().getItemCount()= " + masterTreeViewer.getTree().getItemCount());
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
		System.out.println("masterTreeViewer.getTree().getColumnCount()= "
				+ masterTreeViewer.getTree().getColumnCount());
		System.out.println("masterTreeViewer.getTree().getItems().length= "
				+ masterTreeViewer.getTree().getItems().length);
		System.out.println("masterTreeViewer.getTree().getItemCount()= " + masterTreeViewer.getTree().getItemCount());
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

	private QueryModel[] createQueryModel() {
		int rows = propertiesToMatch.size();
		QueryModel[] elements = new QueryModel[rows];
		int index = 0;
		for (String contextConcat : propertiesToMatch) {
			String value = contextConcat;
			Resource resource = propertyResourcesToMatch.get(index);
			// String value = dataRow.get(0);
			QueryModel queryModel = new QueryModel(value);
			queryModel.uri = resource;
			elements[index++] = queryModel;
			// index++;
		}
		return elements;
	}

	private QueryModel[] createQueryModel(TableProvider tableProvider) {
		int rows = tableProvider.getData().size();
		QueryModel[] elements = new QueryModel[rows];
		int index = 0;
		for (DataRow dataRow : tableProvider.getData()) {
			String value = dataRow.get(0);
			QueryModel queryModel = new QueryModel(value);
			// AnonId uri = new AnonId(dataRow.get(1)); // MIGHT THIS WORK?
			Resource queryPropertyResource = null;
			// Resource fred = (Resource)uri;
			// thing = ActiveTDB.tdbModel.getResource(fred );
			ResIterator iterator = (ActiveTDB.tdbModel.listSubjectsWithProperty(RDF.type, FedLCA.FlowProperty));
			while (iterator.hasNext()) {
				Resource resource = iterator.next();
				if (resource.isAnon()) {
					AnonId anonId = (AnonId) resource.getId();
					if (dataRow.get(1).equals(anonId.toString())) {
						queryPropertyResource = resource;
						System.out.println("index = " + index);
						System.out.println("anonId.toString() =" + anonId.toString());
						System.out.println("anonId.getLabelString() =" + anonId.getLabelString());
						System.out.println("dataRow.get(1) = " + dataRow.get(1));
					}
				}
			}
			queryModel.setUri(queryPropertyResource);
			elements[index++] = queryModel;
		}
		return elements;
	}

	public class QueryModel {
		private String label = "";
		private Resource uri = null;

		public QueryModel(String label) {
			this.label = label;
		}

		public void setUri(Resource uri) {
			this.uri = uri;
		}

		public Resource getUri() {
			return uri;
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

	public List<String> getPropertiesToMatch() {
		return propertiesToMatch;
	}

	public void setPropertiesToMatch(List<String> properties) {
		propertiesToMatch = properties;
		// update();
	}

	public List<Resource> getPropertyResourcesToMatch() {
		return propertyResourcesToMatch;
	}

	public void setPropertyResourcesToMatch(List<Resource> propertyResourcesToMatch) {
		this.propertyResourcesToMatch = propertyResourcesToMatch;
	}

}
