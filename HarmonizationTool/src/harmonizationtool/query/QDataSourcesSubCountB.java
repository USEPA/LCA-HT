package harmonizationtool.query;

public class QDataSourcesSubCountB extends HarmonyBaseQuery {
	{
		label = "Show DataSet Flowable Counts";
	}
	{
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
		b.append("SELECT  (str(?label) as ?name) (str(?vs) as ?v) (str(count(distinct ?flble)) as ?Flowables) (str(count(distinct ?sub)) as ?Substances)\n");
//		b.append("SELECT  (str(?label) as ?name) ?data_set (str(count(distinct ?flble)) as ?Flowables) (str(count(distinct ?sub)) as ?Substances)\n");
		b.append("WHERE \n");
//		b.append("  { ?s ?p ?o . \n");
		b.append("  { ?s a eco:DataSource . \n");
		b.append("    ?s rdfs:label ?label \n");
//		b.append("    OPTIONAL { ?s fedlca:localSerialNumber ?lid } \n");
//		b.append("    OPTIONAL { ?s eco:hasMajorVersionNumber ?mj } \n");
//		b.append("    OPTIONAL { ?s eco:hasMinorVersionNumber ?mi } \n");
		b.append("    OPTIONAL { ?s dcterms:hasVersion ?vs } \n");
		b.append("    {{ ?flble eco:hasDataSource ?s . \n");
		b.append("    {?flble a eco:Flowable .} UNION {?flble a eco:Substance .} UNION {?flble a eco:Energy .}} \n");
		b.append("    UNION \n");
		b.append("    {?sub eco:hasDataSource ?s . \n");
		b.append("    ?sub a eco:Substance . }} \n ");
//		b.append("    bind (concat(str(?label),\" \", str(?mj),\".\",str(?mi)) as ?data_set) \n");

//		b.append("    bind (concat(str(?label),\" \", str(?vs)) as ?data_set) \n");
		b.append("  } \n");
		b.append("group by ?label ?vs \n");
		b.append("order by ?label ?vs \n");
		
		queryStr = b.toString();

	}
}