package gov.epa.nrmrl.std.lca.ht.output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.FlowContext;
import gov.epa.nrmrl.std.lca.ht.dataModels.FlowProperty;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataPropertyProvider;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.Flowable;
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FASC;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.widgets.TreeItem;

public class HarmonizedDataSelector extends ViewPart {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.output.HarmonizedDataSelector";
	private static Combo comboDataSource;
	private static Combo comboFlowableFields;
	private static Combo comboContextFields;
	private static Combo comboPropertyFields;

	private static TreeItem treeItemFlowable;
	private static TreeItem treeItemContext;
	private static TreeItem treeItemProperty;

	private static DataSourceProvider curDataSourceProvider;
	private static Logger runLogger = Logger.getLogger("run");

	public HarmonizedDataSelector() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(3, false));
		new Label(parent, SWT.NONE);

		Button btnReset = new Button(parent, SWT.NONE);
		btnReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		btnReset.setText("Reset");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);

		Label lblDatasetToOutput = new Label(parent, SWT.NONE);
		lblDatasetToOutput.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDatasetToOutput.setText("Choose Dataset");

		comboDataSource = new Combo(parent, SWT.NONE);
		comboDataSource.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(parent, SWT.NONE);
		
				CheckboxTreeViewer checkboxTreeViewer = new CheckboxTreeViewer(parent, SWT.BORDER);
				Tree tree = checkboxTreeViewer.getTree();
				tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				
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
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
				new Label(parent, SWT.NONE);
		
				comboFlowableFields = new Combo(parent, SWT.NONE);
				comboFlowableFields.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(parent, SWT.NONE);
				new Label(parent, SWT.NONE);
		
				comboContextFields = new Combo(parent, SWT.NONE);
				comboContextFields.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(parent, SWT.NONE);
				new Label(parent, SWT.NONE);
		
				comboPropertyFields = new Combo(parent, SWT.NONE);
				comboPropertyFields.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(parent, SWT.NONE);
		// update();
	}

	public static void update() {
		comboDataSource.removeAll();
//		textFlowableInfo.setText("");
//		textContextInfo.setText("");
//		textPropertyInfo.setText("");
		comboDataSource.setItems(DataSourceKeeper.getAlphabetizedNames());
		comboDataSource.addSelectionListener(new ComboDataSourceListener());

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
		b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		b.append("PREFIX  fasc:   <http://ontology.earthster.org/eco/fasc#> \n");
		b.append("PREFIX  fedlca: <http://epa.gov/nrmrl/std/lca/fedlca/1.0#> \n");
		b.append("PREFIX  lcaht: <http://epa.gov/nrmrl/std/lca/ht/1.0#> \n");
		b.append("PREFIX  afn:    <http://jena.hpl.hp.com/ARQ/function#> \n");
		b.append("PREFIX  fn:     <http://www.w3.org/2005/xpath-functions#> \n");
		b.append("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
		b.append("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
		b.append("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		b.append("PREFIX  xml:    <http://www.w3.org/XML/1998/namespace> \n");
		b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
		b.append("PREFIX  dcterms: <http://purl.org/dc/terms/> \n");
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
			b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
			b.append("PREFIX  fasc:   <http://ontology.earthster.org/eco/fasc#> \n");
			b.append("PREFIX  fedlca: <http://epa.gov/nrmrl/std/lca/fedlca/1.0#> \n");
			b.append("PREFIX  lcaht: <http://epa.gov/nrmrl/std/lca/ht/1.0#> \n");
			b.append("PREFIX  afn:    <http://jena.hpl.hp.com/ARQ/function#> \n");
			b.append("PREFIX  fn:     <http://www.w3.org/2005/xpath-functions#> \n");
			b.append("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
			b.append("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
			b.append("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
			b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
			b.append("PREFIX  xml:    <http://www.w3.org/XML/1998/namespace> \n");
			b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
			b.append("PREFIX  dcterms: <http://purl.org/dc/terms/> \n");
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
		b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		b.append("PREFIX  fasc:   <http://ontology.earthster.org/eco/fasc#> \n");
		b.append("PREFIX  fedlca: <http://epa.gov/nrmrl/std/lca/fedlca/1.0#> \n");
		b.append("PREFIX  lcaht: <http://epa.gov/nrmrl/std/lca/ht/1.0#> \n");
		b.append("PREFIX  afn:    <http://jena.hpl.hp.com/ARQ/function#> \n");
		b.append("PREFIX  fn:     <http://www.w3.org/2005/xpath-functions#> \n");
		b.append("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
		b.append("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
		b.append("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		b.append("PREFIX  xml:    <http://www.w3.org/XML/1998/namespace> \n");
		b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
		b.append("PREFIX  dcterms: <http://purl.org/dc/terms/> \n");
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
//			textContextInfo.setText(rdfNode.asLiteral().getValue().toString());
		}

		b = new StringBuilder();
		b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		b.append("PREFIX  fasc:   <http://ontology.earthster.org/eco/fasc#> \n");
		b.append("PREFIX  fedlca: <http://epa.gov/nrmrl/std/lca/fedlca/1.0#> \n");
		b.append("PREFIX  lcaht: <http://epa.gov/nrmrl/std/lca/ht/1.0#> \n");
		b.append("PREFIX  afn:    <http://jena.hpl.hp.com/ARQ/function#> \n");
		b.append("PREFIX  fn:     <http://www.w3.org/2005/xpath-functions#> \n");
		b.append("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
		b.append("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
		b.append("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		b.append("PREFIX  xml:    <http://www.w3.org/XML/1998/namespace> \n");
		b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
		b.append("PREFIX  dcterms: <http://purl.org/dc/terms/> \n");
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
		comboContextFields.setItems(addEm);
	}

	private static void updateProperties() {
		StringBuilder b = new StringBuilder();
		b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		b.append("PREFIX  fasc:   <http://ontology.earthster.org/eco/fasc#> \n");
		b.append("PREFIX  fedlca: <http://epa.gov/nrmrl/std/lca/fedlca/1.0#> \n");
		b.append("PREFIX  lcaht: <http://epa.gov/nrmrl/std/lca/ht/1.0#> \n");
		b.append("PREFIX  afn:    <http://jena.hpl.hp.com/ARQ/function#> \n");
		b.append("PREFIX  fn:     <http://www.w3.org/2005/xpath-functions#> \n");
		b.append("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
		b.append("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
		b.append("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		b.append("PREFIX  xml:    <http://www.w3.org/XML/1998/namespace> \n");
		b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
		b.append("PREFIX  dcterms: <http://purl.org/dc/terms/> \n");
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
//			textPropertyInfo.setText(rdfNode.asLiteral().getValue().toString());
		}

		b = new StringBuilder();
		b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		b.append("PREFIX  fasc:   <http://ontology.earthster.org/eco/fasc#> \n");
		b.append("PREFIX  fedlca: <http://epa.gov/nrmrl/std/lca/fedlca/1.0#> \n");
		b.append("PREFIX  lcaht: <http://epa.gov/nrmrl/std/lca/ht/1.0#> \n");
		b.append("PREFIX  afn:    <http://jena.hpl.hp.com/ARQ/function#> \n");
		b.append("PREFIX  fn:     <http://www.w3.org/2005/xpath-functions#> \n");
		b.append("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
		b.append("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
		b.append("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		b.append("PREFIX  xml:    <http://www.w3.org/XML/1998/namespace> \n");
		b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
		b.append("PREFIX  dcterms: <http://purl.org/dc/terms/> \n");
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
		comboPropertyFields.setItems(addEm);
	}

	@Override
	public void setFocus() {
	}

}
