package gov.epa.nrmrl.std.lca.ht.output;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataCuration.ComparisonProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataPropertyProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataValue;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.FlowContext;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.MatchContexts;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.FlowProperty;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.FlowUnit;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.MatchProperties;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.Flowable;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.MatchStatus;
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.widgets.TreeItem;

public class HarmonizedDataSelector extends ViewPart {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.output.HarmonizedDataSelector";
	private static Combo comboDataSource;

	private static CheckboxTreeViewer checkboxTreeViewer;
	private static TreeItem treeItemFlowable;
	private static TreeItem treeItemContext;
	private static TreeItem treeItemProperty;
	private static boolean expansionEventOccurred = false;
	private static List<TreeItem> expandedTrees = new ArrayList<TreeItem>();
	private static TreeItem selectedItem;

	private static DataSourceProvider curDataSourceProvider;
	private static Logger runLogger = Logger.getLogger("run");

	public HarmonizedDataSelector() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		Button btnReset = new Button(parent, SWT.NONE);
		btnReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getHarmonizedDataRow(1);
				// HACK ABOVE
				update();
			}
		});
		btnReset.setText("Reset");
		new Label(parent, SWT.NONE);

		Label lblDatasetToOutput = new Label(parent, SWT.NONE);
		lblDatasetToOutput.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDatasetToOutput.setText("Choose Dataset");

		comboDataSource = new Combo(parent, SWT.NONE);
		comboDataSource.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(parent, SWT.NONE);

		checkboxTreeViewer = new CheckboxTreeViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		Tree tree = checkboxTreeViewer.getTree();
		GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_tree.heightHint = 400;
		tree.setLayoutData(gd_tree);

		checkboxTreeViewer.addTreeListener(new ITreeViewerListener() {

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				expansionEventOccurred = true;
			}

			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				expansionEventOccurred = true;
			}

		});

		final TreeViewerFocusCellManager mgr = new TreeViewerFocusCellManager(checkboxTreeViewer,
				new FocusCellOwnerDrawHighlighter(checkboxTreeViewer));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(checkboxTreeViewer) {
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && (event.keyCode == SWT.CR || event.character == ' '))
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};

		TreeViewerEditor.create(checkboxTreeViewer, mgr, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | ColumnViewerEditor.TABBING_VERTICAL
				| ColumnViewerEditor.KEYBOARD_ACTIVATION);

		checkboxTreeViewer.getTree().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedItem = (TreeItem) e.item;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		checkboxTreeViewer.getControl().addTraverseListener(new TraverseListener() {

			public void keyTraversed(TraverseEvent e) {
				if ((e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS)
						&& mgr.getFocusCell().getColumnIndex() == 2) {
					ColumnViewerEditor editor = checkboxTreeViewer.getColumnViewerEditor();
					ViewerCell cell = mgr.getFocusCell();

					try {
						Method m = ColumnViewerEditor.class.getDeclaredMethod("processTraverseEvent", new Class[] {
								int.class, ViewerRow.class, TraverseEvent.class });
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

		checkboxTreeViewer.getTree().addListener(SWT.PaintItem, new Listener() {

			@Override
			public void handleEvent(Event event) {
				GC gc = event.gc;
				if (expansionEventOccurred) {
					updateExpandedTrees();
				}
				for (TreeItem treeItem : expandedTrees) {
					int x1 = treeItem.getBounds().x;
					int y1 = treeItem.getBounds().y;
					Rectangle rightRect = treeItem.getBounds(checkboxTreeViewer.getTree().getColumnCount() - 1);
					int x2 = rightRect.x + rightRect.width;
					Rectangle bottomRect = treeItem.getItem(treeItem.getItemCount() - 1).getBounds();
					int y2 = bottomRect.y + bottomRect.height;
					Rectangle fullRowRect = new Rectangle(x1, y1, x2 - x1 - 1, y2 - y1 - 1);
					int lineWidth = 1;
					drawRect(gc, fullRowRect, lineWidth);
				}
			}

			private void drawRect(GC gc, Rectangle rectangle, int lineWidth) {
				int lineWidthSave = gc.getLineWidth();
				gc.setLineWidth(lineWidth);
				gc.drawRectangle(rectangle);
				gc.setLineWidth(lineWidthSave);
			}

		});

		treeItemFlowable = new TreeItem(tree, SWT.NONE);
		treeItemFlowable.setText("Flowable");

		treeItemContext = new TreeItem(tree, SWT.NONE);
		treeItemContext.setText("Flow Context");

		treeItemProperty = new TreeItem(tree, SWT.NONE);
		treeItemProperty.setText("Flow Property");

		for (String propertyName : Flowable.getDataPropertyMap().keySet()) {
			TreeItem treeItem = new TreeItem(treeItemFlowable, SWT.NONE);
			treeItem.setText(propertyName);
			treeItemFlowable.setExpanded(true);
		}

		for (String propertyName : FlowContext.getDataPropertyMap().keySet()) {
			TreeItem treeItem = new TreeItem(treeItemContext, SWT.NONE);
			treeItem.setText(propertyName);
			treeItemContext.setExpanded(true);
		}

		for (String propertyName : FlowProperty.getDataPropertyMap().keySet()) {
			TreeItem treeItem = new TreeItem(treeItemProperty, SWT.NONE);
			treeItem.setText(propertyName);
			treeItemProperty.setExpanded(true);
		}
		update();
	}

	public static void update() {
		comboDataSource.removeAll();
		// textFlowableInfo.setText("");
		// textContextInfo.setText("");
		// textPropertyInfo.setText("");
		String[] names = DataSourceKeeper.getAlphabetizedNames();
		if (names != null) {
			comboDataSource.setItems(DataSourceKeeper.getAlphabetizedNames());
			comboDataSource.addSelectionListener(new ComboDataSourceListener());
		}
	}

	private static final class ComboDataSourceListener implements SelectionListener {
		private void doit(SelectionEvent e) {
			String newSelectionName = comboDataSource.getText();
			// if (curDataSourceProvider == null) {
			// return;
			// }
			// if (curDataSourceProvider.getDataSourceName().equals(newSelectionName)) {
			// return;
			// }
			curDataSourceProvider = DataSourceKeeper.getByName(newSelectionName);
			// redrawDialogRows();
			// createComboSelectorFileMD();
			runLogger.info("  OUTPUT DATASOURCE SELECTED: " + comboDataSource.getText());
			updateFlowables();
			updateContexts();
			updateProperties();
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			doit(e);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			doit(e);
		}
	}

	private static void updateFlowables() {
		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());

		// b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		// b.append("PREFIX  fasc:   <http://ontology.earthster.org/eco/fasc#> \n");
		// b.append("PREFIX  fedlca: <http://epa.gov/nrmrl/std/lca/fedlca/1.0#> \n");
		// b.append("PREFIX  lcaht: <http://epa.gov/nrmrl/std/lca/ht/1.0#> \n");
		// b.append("PREFIX  afn:    <http://jena.hpl.hp.com/ARQ/function#> \n");
		// b.append("PREFIX  fn:     <http://www.w3.org/2005/xpath-functions#> \n");
		// b.append("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
		// b.append("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
		// b.append("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		// b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		// b.append("PREFIX  xml:    <http://www.w3.org/XML/1998/namespace> \n");
		// b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
		// b.append("PREFIX  dcterms: <http://purl.org/dc/terms/> \n");
		b.append(" \n");
		b.append("SELECT (count(distinct ?flowable) as ?count)  \n");
		b.append("WHERE \n");
		b.append("  { \n");
		b.append("    ?flowable rdf:type eco:Flowable . \n");
		b.append("    ?flowable eco:hasDataSource ?dataSource . \n");
		b.append("    ?dataSource rdfs:label ?dataSourceName . \n");
		b.append("    filter (str(?dataSourceName) = \"" + comboDataSource.getText() + "\") \n");
		b.append("   } \n");
		String query = b.toString();
		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);
		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		if (resultSet == null) {
			return;
		}
		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			RDFNode rdfNode = querySolution.get("count");
			String countStr = rdfNode.asLiteral().getValue().toString();
			treeItemFlowable.setText("Flowable (" + countStr + ")");
			// textFlowableInfo.setText(rdfNode.asLiteral().getValue().toString());
		}

		treeItemFlowable.removeAll();

		// Resource resource = rdfNode.asResource();
		for (String propertyName : Flowable.getDataPropertyMap().keySet()) {
			LCADataPropertyProvider lcaDataPropertyProvider = Flowable.getDataPropertyMap().get(propertyName);
			Property property = lcaDataPropertyProvider.getTDBProperty();
			Resource resource = lcaDataPropertyProvider.getRDFClass();
			String propertyString = null;
			if (property.isAnon()) {
				AnonId anonId = (AnonId) property.getId();
				propertyString = anonId.getLabelString();
			} else {
				propertyString = property.getURI();
			}

			b = new StringBuilder();
			b.append(Prefixes.getPrefixesForQuery());

			// b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
			// b.append("PREFIX  fasc:   <http://ontology.earthster.org/eco/fasc#> \n");
			// b.append("PREFIX  fedlca: <http://epa.gov/nrmrl/std/lca/fedlca/1.0#> \n");
			// b.append("PREFIX  lcaht: <http://epa.gov/nrmrl/std/lca/ht/1.0#> \n");
			// b.append("PREFIX  afn:    <http://jena.hpl.hp.com/ARQ/function#> \n");
			// b.append("PREFIX  fn:     <http://www.w3.org/2005/xpath-functions#> \n");
			// b.append("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
			// b.append("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
			// b.append("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
			// b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
			// b.append("PREFIX  xml:    <http://www.w3.org/XML/1998/namespace> \n");
			// b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
			// b.append("PREFIX  dcterms: <http://purl.org/dc/terms/> \n");
			b.append(" \n");
			b.append("SELECT ?flowable \n");
			b.append("WHERE \n");
			b.append("  { \n");
			b.append("    ?flowable rdf:type eco:Flowable . \n");
			b.append("    ?flowable eco:hasDataSource ?dataSource . \n");
			b.append("    ?dataSource rdfs:label ?dataSourceName . \n");
			b.append("    filter (str(?dataSourceName) = \"" + comboDataSource.getText() + "\") \n");
			b.append("    ?comparison a fedlca:Comparison .\n");
			b.append("    ?comparison fedlca:comparedSource ?flowable .\n");
			b.append("    ?comparison fedlca:comparedMaster ?masterFlowable .\n");
			b.append("    ?masterFlowable <" + propertyString + "> ?value .\n");
			b.append("   } \n");
			b.append("   limit 1 \n");
			query = b.toString();

			harmonyQuery2Impl = new HarmonyQuery2Impl();
			harmonyQuery2Impl.setQuery(query);
			resultSet = harmonyQuery2Impl.getResultSet();
			TreeItem treeItem = new TreeItem(treeItemFlowable, SWT.NONE);
			treeItem.setText(propertyName);
			treeItemFlowable.setExpanded(true);
			if (resultSet.hasNext()) {
				treeItem.setChecked(true);
				treeItem.setGrayed(false);
			} else {
				treeItem.setChecked(false);
				treeItem.setGrayed(true);
			}

			//
			// propertyNames.add(propertyName);
			// }
			// }
		}
		// Collections.sort(propertyNames);
		// String[] addEm = new String[propertyNames.size()];
		// for (int i = 0; i < addEm.length; i++) {
		// addEm[i] = propertyNames.get(i);
		// }
		// comboFlowableFields.setItems(addEm);
	}

	private static void updateContexts() {
		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());

		// b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		// b.append("PREFIX  fasc:   <http://ontology.earthster.org/eco/fasc#> \n");
		// b.append("PREFIX  fedlca: <http://epa.gov/nrmrl/std/lca/fedlca/1.0#> \n");
		// b.append("PREFIX  lcaht: <http://epa.gov/nrmrl/std/lca/ht/1.0#> \n");
		// b.append("PREFIX  afn:    <http://jena.hpl.hp.com/ARQ/function#> \n");
		// b.append("PREFIX  fn:     <http://www.w3.org/2005/xpath-functions#> \n");
		// b.append("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
		// b.append("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
		// b.append("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		// b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		// b.append("PREFIX  xml:    <http://www.w3.org/XML/1998/namespace> \n");
		// b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
		// b.append("PREFIX  dcterms: <http://purl.org/dc/terms/> \n");
		b.append(" \n");
		b.append("SELECT (count(distinct ?context) as ?count) \n");
		b.append("WHERE \n");
		b.append("  { \n");
		b.append("    ?context rdf:type fasc:Compartment . \n");
		b.append("    ?context eco:hasDataSource ?dataSource . \n");
		b.append("    ?dataSource rdfs:label ?dataSourceName . \n");
		b.append("    filter (str(?dataSourceName) = \"" + comboDataSource.getText() + "\") \n");
		b.append("   } \n");
		String query = b.toString();
		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);
		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			RDFNode rdfNode = querySolution.get("count");
			// textContextInfo.setText(rdfNode.asLiteral().getValue().toString());
		}

		b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());

		// b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		// b.append("PREFIX  fasc:   <http://ontology.earthster.org/eco/fasc#> \n");
		// b.append("PREFIX  fedlca: <http://epa.gov/nrmrl/std/lca/fedlca/1.0#> \n");
		// b.append("PREFIX  lcaht: <http://epa.gov/nrmrl/std/lca/ht/1.0#> \n");
		// b.append("PREFIX  afn:    <http://jena.hpl.hp.com/ARQ/function#> \n");
		// b.append("PREFIX  fn:     <http://www.w3.org/2005/xpath-functions#> \n");
		// b.append("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
		// b.append("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
		// b.append("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		// b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		// b.append("PREFIX  xml:    <http://www.w3.org/XML/1998/namespace> \n");
		// b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
		// b.append("PREFIX  dcterms: <http://purl.org/dc/terms/> \n");
		b.append(" \n");
		b.append("SELECT distinct ?prop  \n");
		b.append("WHERE \n");
		b.append("  { \n");
		b.append("    ?context rdf:type fasc:Compartment . \n");
		b.append("    ?context eco:hasDataSource ?dataSource . \n");
		b.append("    optional {?context ?prop ?n .} \n");
		b.append("    ?dataSource rdfs:label ?dataSourceName . \n");
		b.append("    filter (str(?dataSourceName) = \"" + comboDataSource.getText() + "\") \n");
		b.append("   } \n");
		query = b.toString();
		harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);
		resultSet = harmonyQuery2Impl.getResultSet();
		List<String> propertyNames = new ArrayList<String>();
		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			RDFNode rdfNode = querySolution.get("prop");
			Resource resource = rdfNode.asResource();
			for (String propertyName : FlowContext.getDataPropertyMap().keySet()) {
				LCADataPropertyProvider lcaDataPropertyProvider = FlowContext.getDataPropertyMap().get(propertyName);
				if (lcaDataPropertyProvider.getTDBProperty().asResource().equals(resource)) {
					propertyNames.add(propertyName);
				}
			}
		}
		Collections.sort(propertyNames);
		String[] addEm = new String[propertyNames.size()];
		for (int i = 0; i < addEm.length; i++) {
			addEm[i] = propertyNames.get(i);
		}
	}

	private static void updateProperties() {
		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		// b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		// b.append("PREFIX  fasc:   <http://ontology.earthster.org/eco/fasc#> \n");
		// b.append("PREFIX  fedlca: <http://epa.gov/nrmrl/std/lca/fedlca/1.0#> \n");
		// b.append("PREFIX  lcaht: <http://epa.gov/nrmrl/std/lca/ht/1.0#> \n");
		// b.append("PREFIX  afn:    <http://jena.hpl.hp.com/ARQ/function#> \n");
		// b.append("PREFIX  fn:     <http://www.w3.org/2005/xpath-functions#> \n");
		// b.append("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
		// b.append("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
		// b.append("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		// b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		// b.append("PREFIX  xml:    <http://www.w3.org/XML/1998/namespace> \n");
		// b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
		// b.append("PREFIX  dcterms: <http://purl.org/dc/terms/> \n");
		b.append(" \n");
		b.append("SELECT (count(distinct ?property) as ?count) \n");
		b.append("WHERE \n");
		b.append("  { \n");
		b.append("    ?property rdf:type fedlca:FlowProperty . \n");
		b.append("    ?property eco:hasDataSource ?dataSource . \n");
		b.append("    ?dataSource rdfs:label ?dataSourceName . \n");
		b.append("    filter (str(?dataSourceName) = \"" + comboDataSource.getText() + "\") \n");
		b.append("   } \n");
		String query = b.toString();
		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);
		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			RDFNode rdfNode = querySolution.get("count");
			// textPropertyInfo.setText(rdfNode.asLiteral().getValue().toString());
		}

		b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());

		// b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		// b.append("PREFIX  fasc:   <http://ontology.earthster.org/eco/fasc#> \n");
		// b.append("PREFIX  fedlca: <http://epa.gov/nrmrl/std/lca/fedlca/1.0#> \n");
		// b.append("PREFIX  lcaht: <http://epa.gov/nrmrl/std/lca/ht/1.0#> \n");
		// b.append("PREFIX  afn:    <http://jena.hpl.hp.com/ARQ/function#> \n");
		// b.append("PREFIX  fn:     <http://www.w3.org/2005/xpath-functions#> \n");
		// b.append("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
		// b.append("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
		// b.append("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		// b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		// b.append("PREFIX  xml:    <http://www.w3.org/XML/1998/namespace> \n");
		// b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
		// b.append("PREFIX  dcterms: <http://purl.org/dc/terms/> \n");
		b.append(" \n");
		b.append("SELECT distinct ?prop  \n");
		b.append("WHERE \n");
		b.append("  { \n");
		b.append("    ?property rdf:type fedlca:FlowProperty . \n");
		b.append("    ?property eco:hasDataSource ?dataSource . \n");
		b.append("    optional {?property ?prop ?n .} \n");
		b.append("    ?dataSource rdfs:label ?dataSourceName . \n");
		b.append("    filter (str(?dataSourceName) = \"" + comboDataSource.getText() + "\") \n");
		b.append("   } \n");
		query = b.toString();
		harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);
		resultSet = harmonyQuery2Impl.getResultSet();
		List<String> propertyNames = new ArrayList<String>();
		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			RDFNode rdfNode = querySolution.get("prop");
			Resource resource = rdfNode.asResource();
			for (String propertyName : FlowProperty.getDataPropertyMap().keySet()) {
				LCADataPropertyProvider lcaDataPropertyProvider = FlowProperty.getDataPropertyMap().get(propertyName);
				if (lcaDataPropertyProvider.getTDBProperty().asResource().equals(resource)) {
					propertyNames.add(propertyName);
				}
			}
		}
		Collections.sort(propertyNames);
		String[] addEm = new String[propertyNames.size()];
		for (int i = 0; i < addEm.length; i++) {
			addEm[i] = propertyNames.get(i);
		}
	}

	@Override
	public void setFocus() {
		checkboxTreeViewer.getControl().setFocus();

	}

	protected void updateExpandedTrees() {
		TreeItem[] treeItems = checkboxTreeViewer.getTree().getItems();
		expandedTrees.clear();
		for (TreeItem treeItem : treeItems) {
			if (treeItem.getExpanded()) {
				expandedTrees.add(treeItem);
			}
		}
		expansionEventOccurred = false;
	}

	public static DataRow getHarmonizedDataHeader() {
		TableProvider tableProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey());
		if (tableProvider == null) {
			return null;
		}
		// String csvDataSetName = tableProvider.getDataSourceProvider().getDataSourceName();
		DataRow inputHeader = tableProvider.getHeaderRow();
		if (inputHeader == null) {
			return null;
		}

		DataRow outputHeader = new DataRow();
		List<String> seen = new ArrayList<String>();
		for (int i = 1; i < inputHeader.getSize(); i++) {
			LCADataPropertyProvider lcaDataPropertyProvider = tableProvider.getLCADataPropertyProvider(i);
			if (lcaDataPropertyProvider == null) {
				outputHeader.add("(Column: " + i + " - not assigned)");
			} else {
				String thisColName = lcaDataPropertyProvider.getPropertyClass() + ": "
						+ lcaDataPropertyProvider.getPropertyName();
				int next = 2;
				String stringToTest = thisColName;
				while (seen.contains(stringToTest)) {
					stringToTest = thisColName + "(" + next + ")";
				}
				seen.add(stringToTest);
				outputHeader.add(stringToTest);
			}
		}
		outputHeader.add("Master List Flowable Match Condition");
		for (LCADataPropertyProvider lcaDataPropertyProvider : Flowable.getDataPropertyMap().values()) {
			outputHeader.add("Master List " + lcaDataPropertyProvider.getPropertyClass() + ": "
					+ lcaDataPropertyProvider.getPropertyName());
		}
		outputHeader.add("Master List Flow Context General");
		outputHeader.add("Master List Flow Context Specific");
		outputHeader.add("Master List Flow Property");
		outputHeader.add("Master List Flow Property Unit");
		outputHeader.add("Master List Flow Property Conversion Factor ");
		outputHeader.add("Master List Flow Property Reference Unit");
		return outputHeader;
	}

	public static DataRow getHarmonizedDataRow(int rowNumber) {
		DataRow outputRow = new DataRow();

		TableProvider tableProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey());
		// String csvDataSetName = tableProvider.getDataSourceProvider().getDataSourceName();
		DataRow inputRow = tableProvider.getData().get(rowNumber);
		// DataRow inputHeader = tableProvider.getHeaderRow();
		for (int col = 0; col < inputRow.getSize(); col++) {
			// System.out.println("Column " + col);
			if (col == 0) {
				// System.out.println("Is " + rowNumber + " equal to " + inputRow.getRowNumber() + " ?");
			}
			outputRow.add(inputRow.get(col));
		}

		// PREPARE MASTER LIST OUTPUT FOR -- FLOWABLES --
		Flowable flowable = inputRow.getFlowable();
		if (flowable == null) {
			outputRow.add("");
			// outputRow.add(""); // NOT SURE WHY WE NEED TWO OF THESE? WHY DOESN'T THE LOOP BELOW COUNT SAME AS IN NEXT
			// SECTION?
			for (LCADataPropertyProvider lcaDataPropertyProvider : Flowable.getDataPropertyMap().values()) {
				outputRow.add("");
			}
		} else {
			// LinkedHashMap<Resource, String> matchCandidates = flowable.getMatchCandidates();
			boolean hit = false;
			// for (Entry<Resource, String> matchCandidate : matchCandidates.entrySet()) {

			for (ComparisonProvider comparisonProvider : flowable.getComparisons()) {
				MatchStatus matchStatus = MatchStatus.getByResource(comparisonProvider.getEquivalence());
				String matchCondition = matchStatus.getSymbol();
				outputRow.add(matchCondition);
				int matchNumber = matchStatus.getValue();
				if (matchNumber > 0 && matchNumber < 5) {
					hit = true;
					Flowable mFlowable = new Flowable(comparisonProvider.getUserDataObject());
					// String dataSourceName = mFlowable.getDataSource();
					List<LCADataValue> lcaDataValues = mFlowable.getPropertyValuesInOrder();

					for (LCADataPropertyProvider lcaDataPropertyProvider : Flowable.getDataPropertyMap().values()) {
						StringBuilder resultForField = new StringBuilder();
						resultForField.append("");
						for (LCADataValue lcaDataValue : lcaDataValues) {
							if (lcaDataPropertyProvider.equals(lcaDataValue.getLcaDataPropertyProvider())) {
								if (resultForField.length() < 2) {
									resultForField.append(lcaDataValue.getValueAsString());
								} else {
									resultForField.append("; " + lcaDataValue.getValueAsString());
								}
							}
						}
						outputRow.add(resultForField.toString());
					}
				}
				break;
				// FIXME : THIS MEANS THAT ONLY THE FIRST FLOWABLE THAT IS EQUAL, SUBSET, SUPERSET, OR PROXY WILL BE
				// LISTED
				// FIXME : WHAT SHOULD BE DONE IF MORE THAN ONE HIT? OBVIOUSLY THE EQUAL SHOULD TAKE PRECEDENCE, BUT...
				// FIXME : WHAT DOES IT MEAN IF THERE ARE MULTIPLE HITS AND SOME ARE SUBSET OR PROXY?
			}
			if (hit == false) {
				// outputRow.add("");
				for (LCADataPropertyProvider lcaDataPropertyProvider : Flowable.getDataPropertyMap().values()) {
					outputRow.add("");
				}
			}
		}

		// PREPARE MASTER LIST OUTPUT FOR -- FLOW CONTEXTS --
		FlowContext flowContext = inputRow.getFlowContext();
		if (flowContext == null) {
			outputRow.add("");
			outputRow.add("");
		} else {
			Resource matchingResource = flowContext.getMatchingResource();
			if (matchingResource == null) {
				outputRow.add("");
				outputRow.add("");
			} else {
				// String flowContextString = MatchContexts.getConcatinatedNodeNameFromResource(matchingResource);
				String[] flowContextStrings = MatchContexts.getTwoNodeNamesFromResource(matchingResource);

				outputRow.add(flowContextStrings[0]);
				outputRow.add(flowContextStrings[1]);
			}
		}

		// PREPARE MASTER LIST OUTPUT FOR -- FLOW PROPERTIES --
		FlowUnit flowUnit = inputRow.getFlowUnit();
		if (flowUnit == null) {
			outputRow.add("");
			outputRow.add("");
			outputRow.add("");
			outputRow.add("");
		} else {
			Resource matchingResource = flowUnit.getMatchingResource();
			if (matchingResource == null) {
				outputRow.add("");
				outputRow.add("");
				outputRow.add("");
				outputRow.add("");
			} else {
				String[] flowPropertyStrings = MatchProperties.getFourPropertyStringsFromResource(matchingResource);
				outputRow.add(flowPropertyStrings[0]);
				outputRow.add(flowPropertyStrings[1]);
				outputRow.add(flowPropertyStrings[2]);
				outputRow.add(flowPropertyStrings[3]);
			}
		}

		return outputRow;
	}
}
