package gov.epa.nrmrl.std.lca.ht.compartment.mgr;

import java.util.ArrayList;
import java.util.Calendar;
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

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.TableProvider;
import harmonizationtool.tree.Node;
import harmonizationtool.utils.Util;
import harmonizationtool.vocabulary.FEDLCA;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FASC;
import harmonizationtool.vocabulary.LCAHT;
import com.hp.hpl.jena.vocabulary.OWL;

//import harmonizationtool.vocabulary.;

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
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.TableColumn;

public class HarmonizeCompartments extends ViewPart {
	private Button btnCommitMatches;

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
		compositeQuery.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 1, 1));

		queryLbl = new Label(compositeQuery, SWT.NONE);
		queryLbl.setAlignment(SWT.CENTER);
		queryLbl.setText("Query Compartments");
		// ============ NEW COL =========
		Composite compositeMatches = new Composite(parent, SWT.NONE);
		compositeMatches.setLayout(new GridLayout(4, false));
		// gd_compositeMatches.minimumWidth = 300;
		compositeMatches.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 1, 1));

		matchedLbl = new Label(compositeMatches, SWT.NONE);
		matchedLbl.setText("Matched");
		new Label(compositeMatches, SWT.NONE);
		new Label(compositeMatches, SWT.NONE);

		btnCommitMatches = new Button(compositeMatches, SWT.CENTER);
		btnCommitMatches.setText("Commit Matches");
		btnCommitMatches.addSelectionListener(new SelectionListener() {

			// @Override
			public void widgetSelected(SelectionEvent e) {
				QueryModel[] queryModel = (QueryModel[]) queryTblViewer
						.getInput();
				System.out.println("queryModel.length = " + queryModel.length);
				System.out.println("queryModel[0] = " + queryModel[0]);

				// Resource annotation = FEDLCA.Annotation;
				// Property isA = RDF.type;
				// Resource Class = OWL.Class;
				// Property creator = DCTerms.creator;
				// Property dateSubmitted = DCTerms.dateSubmitted;
				// Property hasComparison = FEDLCA.hasComparison;
				//
				// Resource comparison = FEDLCA.Comparison;
				// Property comparedSource = FEDLCA.comparedSource;
				// Property comparedMaster = FEDLCA.comparedMaster;
				// Property comparedEquivalence = FEDLCA.comparedEquivalence;
				// Resource equivalent = FEDLCA.equivalent;

				Model model = ActiveTDB.model;
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
					model.add(annotationResource, RDF.type, FEDLCA.Annotation);
					// 2) Assign to it a date and creator
					Date calendar = new Date();
					Literal dateLiteral = model.createTypedLiteral(calendar);
					model.add(annotationResource, DCTerms.dateSubmitted,
							dateLiteral);
					if (Util.getPreferenceStore().getString("userName") != null) {
						Literal userName = model.createLiteral(Util.getPreferenceStore().getString("userName"));
						model.add(annotationResource, DCTerms.creator, userName);
					}
				}
				// 3) Loop through each match
				MatchModel[] matchModel = (MatchModel[]) matchedTblViewer
						.getInput();
				System.out.println("matchModel.length= " + matchModel.length);
				for (int i = 0; i < queryModel.length; i++) {
					QueryModel qModel = queryModel[i];
					// String qString = qModel.label;
					MatchModel matchRow = matchModel[i];
					if (matchRow != null) {
						System.out.println("matchRow[" + i + "].label = "
								+ matchRow.label);
						System.out.println("matchRow.getResource() = "
								+ matchRow.getResource());
						// System.out.println("matchRow.getResource().getLocalName() = "+matchRow.getResource().getLocalName());
						// System.out.println("matchRow["+i+"].resource.getLocalName() = "+matchRow.resource.getLocalName());
						// A) Find the Source URI
						// B) Find the Master URI
						Resource queryCompartmentResource = qModel.getUri();
						Resource masterCompartmentResource = matchRow.resource;
						// confirmResource(queryCompartmentResource);
						// confirmResource(masterCompartmentResource);

						if (masterCompartmentResource == null) {
							continue;
						}
						System.out.println("index i =  " + i);
						// C) Create a new Comparison (assigning it to the class
						// Comparison)
						// D) Connect the Annotation to the Comparison
						// E) Create 3 triples for that Comparison: Source,
						// Master, Equivalence

						Resource comparisonResource = model.createResource();
						model.add(comparisonResource, RDF.type,
								FEDLCA.Comparison);
						model.add(annotationResource, FEDLCA.hasComparison,
								comparisonResource);
						model.add(comparisonResource, FEDLCA.comparedSource,
								queryCompartmentResource);
						model.add(comparisonResource, FEDLCA.comparedMaster,
								masterCompartmentResource);
						model.add(comparisonResource,
								FEDLCA.comparedEquivalence, FEDLCA.equivalent);

						// Literal compartmentName =
						// model.createLiteral(qString);
						// ResIterator resIterator =
						// model.listResourcesWithProperty(RDFS.label,
						// compartmentName);
						// while (resIterator.hasNext()) {
						// Resource candidateCompartment = resIterator.next();
						// if (!model.contains(candidateCompartment, RDF.type,
						// FASC.Compartment)) {
						// continue;
						// }
						// if (model.contains(candidateCompartment,
						// ECO.hasDataSource)) {
						// NodeIterator nodeIterator =
						// model.listObjectsOfProperty(candidateCompartment,
						// ECO.hasDataSource);
						//
						// }
						// }
						// Statement statement = model.createStatement(arg0,
						// arg1, arg2);
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
		GridData gd_compositeMaster = new GridData(SWT.FILL, SWT.FILL, false,
				false, 1, 1);
		gd_compositeMaster.minimumWidth = 300;
		compositeMaster.setLayoutData(gd_compositeMaster);

		masterLbl = new Label(compositeMaster, SWT.NONE);
		masterLbl.setText("Master Compartments");

		Button btnAutoAdvance = new Button(compositeMaster, SWT.CHECK);
		btnAutoAdvance.setText("Auto Advance");
		// ================== ROW 2 ==========================
		// ============ NEW COL =========
		new Label(parent, SWT.NONE);
		// ============ NEW COL =========
		queryTblViewer = new TableViewer(parent, SWT.BORDER
				| SWT.FULL_SELECTION);
		queryTbl = queryTblViewer.getTable();
		GridData gd_queryTbl = new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1);
		gd_queryTbl.widthHint = 300;
		queryTbl.setLayoutData(gd_queryTbl);
		queryTblViewer.setContentProvider(new ContentProvider());
		TableViewerColumn queryColumn = new TableViewerColumn(queryTblViewer,
				SWT.NONE);
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
		matchedTblViewer = new TableViewer(parent, SWT.BORDER
				| SWT.FULL_SELECTION);
		matchedTbl = matchedTblViewer.getTable();
		GridData gd_matchedTbl = new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1);
		gd_matchedTbl.widthHint = 300;
		matchedTbl.setLayoutData(gd_matchedTbl);
		matchedTblViewer.setContentProvider(new ContentProvider());
		TableViewerColumn matchColumn = new TableViewerColumn(matchedTblViewer,
				SWT.NONE);
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
		GridData gd_masterTree = new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1);
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
							if (queryTblViewer.getTable().getItemCount() > 0) {
								int row = queryTblViewer.getTable()
										.getSelectionIndex();
								if (row > -1) {
									// String queryLabel =
									// queryTblViewer.getTable().getSelection()[0].getText(0);
									MatchModel[] matchedModel = (MatchModel[]) (matchedTblViewer
											.getInput());
									matchedModel[row].setLabel(masterLabel);
									matchedModel[row]
											.setResource(masterResource);
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

	// private void confirmModelContanisCompartments(TreeNode treeNode){
	// Model model = ActiveTDB.model;
	// if (treeNode.uri != null){
	// if (!model.containsResource(treeNode.uri)){
	// model.createResource(treeNode.uri);
	// }
	// }
	// Iterator<Node> iterator = treeNode.getChildIterator();
	// while(iterator.hasNext()){
	// Node child = iterator.next();
	// confirmModelContanisCompartments((TreeNode)child);
	// }
	// }

	// private void confirmResource(Resource uri) {
	// Model model = ActiveTDB.model;
	// if (!model.containsResource(uri)) {
	// model.createResource(uri);
	// }
	// }

	private TreeNode createHarmonizeCompartments() {
		TreeNode masterCompartmentTree = new TreeNode(null);

		TreeNode release = new TreeNode(masterCompartmentTree);
		release.nodeName = "Release";
		// confirmUri(LCAHT.release);

		TreeNode air = new TreeNode(release);
		air.nodeName = "air";
		air.uri = LCAHT.airUnspecified;
		// confirmUri(LCAHT.release);

		TreeNode airLowPop = new TreeNode(air);
		airLowPop.nodeName = "low population density";
		airLowPop.uri = LCAHT.airLow_population_density;
		// confirmUri(LCAHT.release);

		TreeNode airUnspec = new TreeNode(air);
		airUnspec.nodeName = "unspecified";
		airUnspec.uri = LCAHT.airUnspecified;
		// confirmUri(LCAHT.release);

		TreeNode airHighPop = new TreeNode(air);
		airHighPop.nodeName = "high population density";
		airHighPop.uri = LCAHT.airHigh_population_density;
		// confirmUri(LCAHT.release);

		TreeNode airLowPopLongTerm = new TreeNode(air);
		airLowPopLongTerm.nodeName = "low population density, long-term";
		airLowPopLongTerm.uri = LCAHT.airLow_population_densityLong_term;
		// confirmUri(LCAHT.release);

		TreeNode airLowerStratPlusUpperTrop = new TreeNode(air);
		airLowerStratPlusUpperTrop.nodeName = "lower stratosphere + upper troposphere";
		airLowerStratPlusUpperTrop.uri = LCAHT.airLower_stratosphere_upper_troposphere;
		// confirmUri(LCAHT.release);

		TreeNode water = new TreeNode(release);
		water.nodeName = "water";
		water.uri = LCAHT.waterUnspecified;
		// confirmUri(LCAHT.release);

		TreeNode waterFossil = new TreeNode(water);
		waterFossil.nodeName = "fossil-";
		waterFossil.uri = LCAHT.waterFossil;
		// confirmUri(LCAHT.release);

		TreeNode waterFresh = new TreeNode(water);
		waterFresh.nodeName = "fresh-";
		waterFresh.uri = LCAHT.waterFresh;
		// confirmUri(LCAHT.release);

		TreeNode waterFreshLongTerm = new TreeNode(water);
		waterFreshLongTerm.nodeName = "fresh-, long-term";
		waterFreshLongTerm.uri = LCAHT.waterFreshLong_term;
		// confirmUri(LCAHT.release);

		TreeNode waterGround = new TreeNode(water);
		waterGround.nodeName = "ground-";
		waterGround.uri = LCAHT.waterGround;
		// confirmUri(LCAHT.release);

		TreeNode waterGroundLongTerm = new TreeNode(water);
		waterGroundLongTerm.nodeName = "ground-, long-term";
		waterGroundLongTerm.uri = LCAHT.waterGroundLong_term;
		// confirmUri(LCAHT.release);

		TreeNode waterLake = new TreeNode(water);
		waterLake.nodeName = "lake";
		waterLake.uri = LCAHT.waterLake;
		// confirmUri(LCAHT.release);

		TreeNode waterOcean = new TreeNode(water);
		waterOcean.nodeName = "ocean";
		waterOcean.uri = LCAHT.waterOcean;
		// confirmUri(LCAHT.release);

		TreeNode waterRiver = new TreeNode(water);
		waterRiver.nodeName = "river";
		waterRiver.uri = LCAHT.waterRiver;
		// confirmUri(LCAHT.release);

		TreeNode waterRiverLongTerm = new TreeNode(water);
		waterRiverLongTerm.nodeName = "river, long-term";
		waterRiverLongTerm.uri = LCAHT.waterRiverLong_term;
		// confirmUri(LCAHT.release);

		TreeNode waterSurface = new TreeNode(water);
		waterSurface.nodeName = "surface water";
		waterSurface.uri = LCAHT.waterSurface;
		// confirmUri(LCAHT.release);

		TreeNode waterUnspec = new TreeNode(water);
		waterUnspec.nodeName = "unspecified";
		waterUnspec.uri = LCAHT.waterUnspecified;
		// confirmUri(LCAHT.release);

		TreeNode soil = new TreeNode(release);
		soil.nodeName = "soil";
		soil.uri = LCAHT.soilUnspecified;
		// confirmUri(LCAHT.release);

		TreeNode soilAgricultural = new TreeNode(soil);
		soilAgricultural.nodeName = "agricultural";
		soilAgricultural.uri = LCAHT.soilAgricultural;
		// confirmUri(LCAHT.release);

		TreeNode soilForestry = new TreeNode(soil);
		soilForestry.nodeName = "forestry";
		soilForestry.uri = LCAHT.soilForestry;
		// confirmUri(LCAHT.release);

		TreeNode soilIndustrial = new TreeNode(soil);
		soilIndustrial.nodeName = "industrial";
		soilIndustrial.uri = LCAHT.soilIndustrial;
		// confirmUri(LCAHT.release);

		TreeNode soilUnspec = new TreeNode(soil);
		soilUnspec.nodeName = "unspecified";
		soilUnspec.uri = LCAHT.soilUnspecified;
		// confirmUri(LCAHT.release);

		TreeNode resource = new TreeNode(masterCompartmentTree);
		resource.nodeName = "Resource";
		resource.uri = LCAHT.resource;
		// confirmUri(LCAHT.release);

		TreeNode resourceBiotic = new TreeNode(resource);
		resourceBiotic.nodeName = "biotic";
		resourceBiotic.uri = LCAHT.resourceBiotic;
		// confirmUri(LCAHT.release);

		TreeNode resourceInAir = new TreeNode(resource);
		resourceInAir.nodeName = "in air";
		resourceInAir.uri = LCAHT.resourceIn_air;
		// confirmUri(LCAHT.release);

		TreeNode resourceInGround = new TreeNode(resource);
		resourceInGround.nodeName = "in ground";
		resourceInGround.uri = LCAHT.resourceIn_ground;
		// confirmUri(LCAHT.release);

		TreeNode resourceInLand = new TreeNode(resource);
		resourceInLand.nodeName = "in land";
		resourceInLand.uri = LCAHT.resourceIn_land;
		// confirmUri(LCAHT.release);

		TreeNode resourceInWater = new TreeNode(resource);
		resourceInWater.nodeName = "in water";
		resourceInWater.uri = LCAHT.resourceIn_water;
		// confirmUri(LCAHT.release);

		TreeNode resourceUnspec = new TreeNode(resource);
		resourceUnspec.nodeName = "unspecified";
		resourceUnspec.uri = LCAHT.resourceUnspecified;
		// confirmUri(LCAHT.resourceUnspecified);

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
		queryTblViewer.getControl().setFocus();

	}

	public void update(TableProvider tableProvider) {
		queryTblViewer.setLabelProvider(new LabelProvider());
		queryTblViewer.setContentProvider(new QueryContentProvider());
		QueryModel[] queryModel = createQueryModel(tableProvider);
		queryTblViewer.setInput(queryModel);
		queryTblViewer.getTable().setLinesVisible(true);
		MatchModel[] matchModel = createMatchModel(queryModel);
		System.out.println("Created matchModel matchModel.length= "
				+ matchModel.length);
		matchedTblViewer.setLabelProvider(new MatchLabelProvider());
		matchedTblViewer.setContentProvider(new MatchContentProvider());
		matchedTblViewer.setInput(matchModel);
		matchedTblViewer.getTable().setLinesVisible(true);
		System.out.println("masterTreeViewer.getTree().getColumnCount()= "
				+ masterTreeViewer.getTree().getColumnCount());
		System.out.println("masterTreeViewer.getTree().getItems().length= "
				+ masterTreeViewer.getTree().getItems().length);
		System.out.println("masterTreeViewer.getTree().getItemCount()= "
				+ masterTreeViewer.getTree().getItemCount());
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
			QueryModel queryModel = new QueryModel(value);
			// AnonId uri = new AnonId(dataRow.get(1)); // MIGHT THIS WORK?
			Resource queryCompartmentResource = null;
			// Resource fred = (Resource)uri;
			// thing = ActiveTDB.model.getResource(fred );
			ResIterator iterator = (ActiveTDB.model.listSubjectsWithProperty(
					RDF.type, FASC.Compartment));
			while (iterator.hasNext()) {
				Resource resource = iterator.next();
				if (resource.isAnon()) {
					AnonId anonId = (AnonId) resource.getId();
					if (dataRow.get(1).equals(anonId.toString())) {
						queryCompartmentResource = resource;
						System.out.println("index = " + index);
						System.out.println("anonId.toString() ="
								+ anonId.toString());
						System.out.println("anonId.getLabelString() ="
								+ anonId.getLabelString());
						System.out
								.println("dataRow.get(1) = " + dataRow.get(1));
					}
				}
			}
			queryModel.setUri(queryCompartmentResource);
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
}
