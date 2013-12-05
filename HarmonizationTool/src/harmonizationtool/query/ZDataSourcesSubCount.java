package harmonizationtool.query;

public class ZDataSourcesSubCount extends HarmonyBaseQuery {
	{
		label = "Show Sources + Counts";
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
		b.append("SELECT (afn:localname(?s) as ?sourceIRI) (str(?label) as ?name) (concat(str(?mj),\".\",str(?mi)) as ?version) (str(?com) as ?comment) (str(count(distinct ?sub)) as ?Substances)\n");
		b.append("WHERE \n");
		b.append("  { ?s ?p ?o . \n");
		b.append("    ?s a eco:DataSource . \n");
		b.append("    OPTIONAL {?s rdfs:label ?label} \n");
		b.append("    OPTIONAL {?s rdfs:comment ?com} \n");
		b.append("    OPTIONAL { ?s eco:hasMajorVersionNumber ?mj } \n");
		b.append("    OPTIONAL { ?s eco:hasMinorVersionNumber ?mi } \n");
		b.append("    ?sub eco:hasDataSource ?s . \n");
		b.append("    ?sub a eco:Substance . \n");
		b.append("  } \n");
		b.append("group by ?s ?label ?mj ?mi ?com \n");
		b.append("order by ?s \n");
		queryStr = b.toString();

	}
}