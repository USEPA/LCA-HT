package gov.epa.nrmrl.std.lca.ht.sparql;

import gov.epa.nrmrl.std.lca.ht.dialog.DialogQueryDataSource;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import com.hp.hpl.jena.query.ResultSet;

public class QMatchCAS extends HarmonyQuery2Impl implements LabeledQuery {
	public static final String LABEL = "Show CAS Matches";

	private String param1;
	private String[] referenceDataSets;
	private String param2;

	public QMatchCAS() {
		super();
	}

	public ResultSet getResultSet() {
		// CALL THE DIALOG TO GET THE PARAMETERS
		getDialog();
		// BUILD THE QUERY USING THE PARAMETERS
		buildQuery();
		// READY TO CALL getResultSet() ON THESUPER CLASS
		return super.getResultSet();
	}

	public ResultSet getResultSet(String param1, String[] referenceDataSets) {
		// BRING IN THE PARAMETERS
		this.param1 = param1;
		this.referenceDataSets = referenceDataSets;
		// BUILD THE QUERY USING THE PARAMETERS
		buildQuery();
		// READY TO CALL getResultSet() ON THESUPER CLASS
		return super.getResultSet();
	}

	private void getDialog() {
		DialogQueryDataSource dialog = new DialogQueryDataSource(Display.getCurrent().getActiveShell());
		dialog.create();
		if (dialog.open() == Window.OK) {
			System.out.println("OK");
			param1 = dialog.getPrimaryDataSource();
			referenceDataSets = dialog.getReferenceDataSources();
		}
	}

	private void buildQuery() {
		param2 = "?match_label = \"" + referenceDataSets[0] + "\"";
		for (int i = 1; i < referenceDataSets.length; i++) {
			param2 += " || ?match_label = \"" + referenceDataSets[i] + "\"";
		}

		StringBuilder b = new StringBuilder();
		b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		b.append("PREFIX  fedlca: <http://epa.gov/nrmrl/std/lca/fedlca/1.0#> \n");
		b.append("PREFIX  lcaht:  <http://epa.gov/nrmrl/std/lca/ht/1.0#> \n");
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
		b.append("SELECT DISTINCT \n");
		b.append("  (afn:localname(?s1) as ?q_sub) \n");
		b.append("  (str(?name1) as ?q_name) \n");
		b.append("  (str(?cas) as ?same_cas) \n");
		b.append("  (str(?name2) as ?db_name) \n");
		b.append("  (str(?match_label) as ?matching_set) \n");
		b.append(" \n");
		b.append("WHERE { \n");
		b.append("      ?s1 eco:hasDataSource ?ds_prim . \n");
		b.append("      ?ds_prim rdfs:label ?ds_label . \n");
		b.append("      filter regex(str(?ds_label), \"" + param1 + "\")  \n");
		// param1 EXAMPLE: TRACI 2.1 [IT IS A DATA SET NAME]
		b.append("      ?s2 eco:hasDataSource ?ds_match . \n");
		b.append("      ?ds_match rdfs:label ?match_label . \n");
		b.append("      filter (?ds_prim != ?ds_match) \n");
		b.append("      ?s1 eco:casNumber ?cas .  \n");
		b.append("      ?s2 eco:casNumber ?cas .   \n");
		b.append("      ?s1 rdfs:label ?name1 . \n");
		b.append("      ?s2 rdfs:label ?name2 .  \n");
		b.append("      {{?s1 a eco:Flowable .  } UNION {?s1 a eco:Substance . }} \n");
		b.append("      {{?s2 a eco:Flowable .  } UNION {?s2 a eco:Substance . }} \n");
		b.append("      filter(" + param2 + ") \n"); 
		// param2 EXAMPLES
		// str(?match_label) = "ReCiPe" || str(?match_label) = "MOVES" ||
		// str(?match_label) = "TRACI 2.1"
		// OR IT COULD HAVE A TRAILING '|| false' LIKE THIS:
		// str(?match_label) = "ReCiPe" || str(?match_label) = "MOVES" ||
		// str(?match_label) = "TRACI 2.1" || false

		b.append("} \n");
		b.append("order by ?s1 ?ds_match \n");
		setQuery(b.toString());
	}

	@Override
	public String getLabel() {
		return LABEL;
	}
}
