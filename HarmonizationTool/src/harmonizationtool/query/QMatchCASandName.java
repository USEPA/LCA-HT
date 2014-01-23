package harmonizationtool.query;

public class QMatchCASandName extends HarmonyLabeledQuery {
	private static String query = null;

	{ // init block
		StringBuilder b = new StringBuilder();
		b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		b.append("PREFIX  ethold: <http://epa.gov/nrmrl/std/lca/ethold#> \n");
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
		b.append("SELECT (afn:localname(?s1) as ?q_sub) (str(?name) as ?q_name) (str(?cas) as ?same_cas) (str(?match_label) as ?matching_set) \n");
		b.append(" \n");
		b.append("WHERE { \n");
		b.append("      ?s1 eco:hasDataSource ?ds_prim . \n");
		b.append("      ?ds_prim rdfs:label ?ds_label . \n");
		b.append("      filter regex(str(?ds_label), \"%%%\")  \n");
		b.append("      ?s2 eco:hasDataSource ?ds_match . \n");
		b.append("      ?ds_match rdfs:label ?match_label . \n");
		b.append("      filter (?ds_prim != ?ds_match) \n");
		b.append("      ?s1 eco:casNumber ?cas .  \n");
		b.append("      ?s2 eco:casNumber ?cas .   \n");
		b.append("      ?s1 rdfs:label ?name . \n");
		b.append("      ?s2 rdfs:label ?name .  \n");
		b.append("      {{?s1 a eco:Flowable .  } UNION {?s1 a eco:Substance . }} \n");
		b.append("      {{?s2 a eco:Flowable .  } UNION {?s2 a eco:Substance . }} \n");
		b.append("      filter(%%%) \n"); // THIS WILL CONTAIN THTE LIST OF
											// MATCHING DATA SETS

		// FOR EXAMPLE, %%% MIGHT BE
		// str(?match_label) = "ReCiPe" || str(?match_label) = "MOVES" ||
		// str(?match_label) = "TRACI 2.1"
		// OR IT COULD HAVE A TRAILING || false LIKE THIS:
		// str(?match_label) = "ReCiPe" || str(?match_label) = "MOVES" ||
		// str(?match_label) = "TRACI 2.1" || false

		b.append("  } \n");
		b.append("order by ?s1 ?ds_match \n");
		query = b.toString();
	}

	public QMatchCASandName() {
		super(query, "%%%", "Show CAS and Name Matches");
	}
}
