package harmonizationtool.query;

public class QMatchCAS extends HarmonyBaseQuery {
	{
		label = "Match CAS";
	}
	{
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
		b.append(" \n");
		b.append("SELECT (afn:localname(?ds_match) as ?data_set) (str(count (?cas)) as ?same_cas)\n");
		b.append(" \n");
		b.append("WHERE { \n");
		b.append("      ?s1 eco:hasDataSource eco:ds_001 . \n");
		b.append("      ?s2 eco:hasDataSource ?ds_match . \n");
		b.append("      filter(str(afn:localname(?ds_match)) != \"ds_001\") . \n");
		// b.append("      ?s1 a eco:Substance .  \n");
		// b.append("      ?s2 a eco:Substance .  \n");
		b.append("      ?s1 eco:casNumber ?cas .  \n");
		b.append("      ?s2 eco:casNumber ?cas .  \n");
		b.append("} \n");
		b.append("      group by ?ds_match \n");
		queryStr = b.toString();

	}
}
