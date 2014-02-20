package harmonizationtool.query;


public class QListDataSets extends HarmonyBaseQuery {
	{
		label = "Data Sources: name, version";
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
		b.append("PREFIX  dcterms: <http://purl.org/dc/terms/> \n");
		b.append(" \n");
//		b.append("SELECT DISTINCT (str(?id) as ?lid) (\" \" as ?ws) (str(?label) as ?lab) (\" \" as ?ws2) (str(?vs) as ?version) (str(?mj) as ?maj) (\".\" as ?dot)  (str(?mi) as ?min) \n");
		b.append("SELECT DISTINCT (str(?label) as ?lab)\n");
		b.append("WHERE \n");
		b.append("  { ?s a eco:DataSource . \n");
//		b.append("    ?s ethold:localSerialNumber ?id . \n");
		b.append("    ?s rdfs:label ?label \n");
		b.append("  } \n");
		b.append("order by ?label \n");
		queryStr = b.toString();

	}
}