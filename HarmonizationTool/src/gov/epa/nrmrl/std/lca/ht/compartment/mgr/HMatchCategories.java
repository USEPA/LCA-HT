package gov.epa.nrmrl.std.lca.ht.compartment.mgr;

import harmonizationtool.query.HarmonyQuery2Impl;
import harmonizationtool.query.LabeledQuery;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import com.hp.hpl.jena.query.ResultSet;

public class HMatchCategories extends HarmonyQuery2Impl implements LabeledQuery {
	public static final String LABEL = "Harmonize Compartments (Cats)";

	private String param1;
//	private String[] referenceDataSets;

	public HMatchCategories() {
		super();
	}

	public ResultSet getResultSet() {
		return super.getResultSet();
	}

	public ResultSet getResultSet(String param1, String[] referenceDataSets) {
		// BRING IN THE PARAMETERS
		this.param1 = param1;
//		this.referenceDataSets = referenceDataSets;
		// BUILD THE QUERY USING THE PARAMETERS
		buildQuery();
		// READY TO CALL getResultSet() ON THESUPER CLASS
		return super.getResultSet();
	}

	private void getDialog() {
		DialogPickOneDataset dialog = new DialogPickOneDataset(Display.getCurrent()
				.getActiveShell());
		dialog.create();
		if (dialog.open() == Window.OK) {
			System.out.println("OK");
			param1 = dialog.getPrimaryDataSet();
//			referenceDataSets = dialog.getReferenceDataSets();
		}
	}

	private void buildQuery() {
//		for (int i = 0; i < referenceDataSets.length; i++) {
//			if (referenceDataSets[i] == param1) {
//				// REMOVE IT
//			}
//		}

		StringBuilder b = new StringBuilder();
		
		b.append ("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		b.append ("PREFIX  fasc:    <http://ontology.earthster.org/eco/fasc#>  \n");
		b.append ("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n");
		b.append ("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#>  \n");
		b.append ("  \n");
		b.append ("SELECT DISTINCT  (str(?label) as ?comp) ?c \n");
		b.append ("WHERE  \n");
		b.append ("  { \n");
		b.append ("    ?s a eco:DataSource .  \n");
		b.append ("    ?s rdfs:label ?ds_label . \n");
		b.append ("    filter regex(str(?ds_label),\"^"+param1+"$\") \n");
		b.append ("    ?c eco:hasDataSource ?s . \n");
		b.append ("    ?c a fasc:Compartment . \n");
		b.append ("    ?c rdfs:label ?label . \n");
		b.append ("  }  \n");
		b.append ("  order by ?label  \n");

		
//		b.append ("PREFIX  eco:    <http://ontology.earthster.org/eco/core#>  \n");
//		b.append ("PREFIX  fasc:    <http://ontology.earthster.org/eco/fasc#>  \n");
//		b.append ("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n");
//		b.append ("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#>  \n");
//		b.append ("\n");
//		b.append ("select ?ds ?comp \n");
//		b.append ("where \n");
//		b.append ("{ \n");
//		b.append ("  { \n");
//		b.append ("    { \n");
//		b.append ("      SELECT DISTINCT   \n");
//		b.append ("        (str(?label) as ?ds)  \n");
//		b.append ("        (str(?cat) as ?comp)  \n");
//		b.append ("      WHERE  \n");
//		b.append ("      { ?c a fasc:Compartment . \n");
//		b.append ("        ?s a eco:DataSource .  \n");
//		b.append ("        ?s rdfs:label ?label  . \n");
//		b.append ("        ?c eco:hasDataSource ?s . \n");
//		b.append ("        ?c rdfs:label ?cat . \n");
//		b.append ("      } \n");
//		b.append ("    } \n");
//		b.append ("  filter regex(?ds, \"^"+ param1 +"$\") \n");
//		b.append ("  } UNION  \n");
//		b.append ("  { \n");
//		b.append ("    { \n");
//		b.append ("      SELECT DISTINCT   \n");
//		b.append ("        (str(?label) as ?ds)  \n");
//		b.append ("        (str(?cat) as ?comp)  \n");
//		b.append ("      WHERE  \n");
//		b.append ("      { ?c a fasc:Compartment . \n");
//		b.append ("        ?s a eco:DataSource .  \n");
//		b.append ("        ?s rdfs:label ?label  . \n");
//		b.append ("        ?c eco:hasDataSource ?s . \n");
//		b.append ("        ?c rdfs:label ?cat . \n");
//		b.append ("      } \n");
//		b.append ("    } \n");
//		b.append ("  filter ( \n");
//		String refDataSet = referenceDataSets[0];
//		b.append ("    regex(?ds, \"^"+refDataSet+"$\") \n");
//		for (int i = 1; i < referenceDataSets.length; i++) {
//			refDataSet = referenceDataSets[i];
//			b.append ("    || regex(?ds, \""+refDataSet+"\") \n");
//		}
//		b.append ("  ) \n ");
//		b.append ("  } \n");
//		b.append ("} \n");

		
//		b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
//		b.append("PREFIX  fasc:   <http://ontology.earthster.org/eco/fasc#> \n");
//		b.append("PREFIX  ecogov: <http://epa.gov/nrmrl/std/lca/ecogov#> \n");
//		b.append("PREFIX  afn:    <http://jena.hpl.hp.com/ARQ/function#> \n");
//		b.append("PREFIX  fn:     <http://www.w3.org/2005/xpath-functions#> \n");
//		b.append("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
//		b.append("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
//		b.append("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
//		b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
//		b.append("PREFIX  xml:    <http://www.w3.org/XML/1998/namespace> \n");
//		b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
//		b.append("PREFIX  dcterms: <http://purl.org/dc/terms/> \n");
//		b.append(" \n");
//		b.append("SELECT  DISTINCT\n");
//		b.append("   (str(?ds1_name) as ?" + TableProvider.SUBROW_PREFIX + "1_"
//				+ TableProvider.SUBROW_NAMEHEADER + ") \n");
//		b.append("   (str(?comp1) as ?" + TableProvider.SUBROW_PREFIX + "1_compartment) \n");
//
//
//		for (int i = 0; i < referenceDataSets.length; i++) {
//			int iPlusTwo = i + 2;
//			b.append("   (str(?ds" + iPlusTwo + "_name) as ?" + TableProvider.SUBROW_PREFIX
//					+ iPlusTwo + "_" + TableProvider.SUBROW_NAMEHEADER + ") \n");
//			b.append("   (str(?comp" + iPlusTwo + ") as ?" + TableProvider.SUBROW_PREFIX
//					+ iPlusTwo + "_compartment) \n");
//
//		}
//		b.append(" \n");
//		b.append("WHERE { \n");
//		b.append("      ?c1 eco:hasDataSource ?ds1 . \n");
//		b.append("      OPTIONAL {?c1 ecogov:foundOnRow ?qRow . } \n");
//		b.append("      ?ds1 rdfs:label ?ds1_name . \n");
//		b.append("      filter regex(str(?ds1_name),\"" + param1 + "\") \n");
//		b.append("      ?c1 rdf:type fasc:Compartment .  \n");
//		b.append("      ?c1 rdfs:label ?comp1 .  \n");
//
//		for (int i = 0; i < referenceDataSets.length; i++) {
//			int iPlusTwo = i + 2;
//			String refDataSet = referenceDataSets[i];
//			b.append("OPTIONAL {");
//			b.append("      ?c" + iPlusTwo + " eco:hasDataSource ?ds"
//					+ iPlusTwo + " . \n");
//			b.append("      ?ds" + iPlusTwo + " rdfs:label ?ds" + iPlusTwo
//					+ "_name . \n");
//			b.append("      filter regex(str(?ds" + iPlusTwo + "_name),\""
//					+ refDataSet + "\") \n");
//			b.append("      ?c" + iPlusTwo + " rdf:type fasc:Compartment . \n");
//			b.append("      ?c" + iPlusTwo + " rdfs:label ?comp" + iPlusTwo
//					+ " .  \n");
//
//			b.append("} \n");
//		}
//
//		b.append("      filter ( \n");
//		b.append("     bound(?c2) \n");
//		for (int i = 1; i < referenceDataSets.length; i++) {
//			int iPlusTwo = i + 2;
//			b.append("  || bound(?c" + iPlusTwo + ") \n");
//		}
//		b.append("       ) \n");
//		b.append("} \n");
////		b.append("group by ?ds1_name \n");
//		b.append("order by ?comp1 \n");
////		b.append("limit 500 \n");
		setQuery(b.toString());
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public void getParamaterFromUser() {
		// CALL THE DIALOG TO GET THE PARAMETERS
		getDialog();
		// BUILD THE QUERY USING THE PARAMETERS
		buildQuery();
	}
}