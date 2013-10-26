package harmonizationtool.query;

public class QDataSourcesSubCountB extends HarmonyBaseQuery {
	{
		label = "Show IRI + Counts";
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
		b.append("SELECT (afn:localname(?set) as ?sourceIRI) (str(count(distinct ?sub)) as ?Substances)\n");
		b.append("WHERE \n");
		b.append("  { ?sub eco:hasDataSource ?set . \n");
		b.append("    ?sub a eco:Substance . \n");
		b.append("  } \n");
		b.append("group by ?set \n");
		b.append("order by ?set \n");
		queryStr = b.toString();

	}
}