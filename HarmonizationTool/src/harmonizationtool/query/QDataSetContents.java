package harmonizationtool.query;

public class QDataSetContents  extends HarmonyLabeledQuery {
	private static String query = null;
	{// init block
		StringBuilder b = new StringBuilder();
		b.append ("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		b.append ("PREFIX  ethold: <http://epa.gov/nrmrl/std/lca/ethold#> \n");
		b.append ("PREFIX  afn:    <http://jena.hpl.hp.com/ARQ/function#> \n");
		b.append ("PREFIX  fn:     <http://www.w3.org/2005/xpath-functions#> \n");
		b.append ("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
		b.append ("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
		b.append ("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		b.append ("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		b.append ("PREFIX  xml:    <http://www.w3.org/XML/1998/namespace> \n");
		b.append ("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
		b.append ("PREFIX  dcterms: <http://purl.org/dc/terms/> \n");
		b.append (" ");
		b.append ("SELECT  (str(?label) as ?data_set_name) (str(count(distinct ?member)) as ?count) (str(afn:localname(?class)) as ?Type) \n");
		b.append ("WHERE \n");
		b.append ("  { ?s a eco:DataSource . \n");
		b.append ("    ?s rdfs:label ?label  . \n");
		b.append ("    ?member eco:hasDataSource ?s . \n");
		b.append ("    ?member a ?class . \n");
		b.append ("#    ?class rdfs:label ?class_name . \n");
		b.append ("  } \n");
		b.append ("#group by ?label ?class_name \n");
		b.append ("group by ?label ?class \n");
		b.append ("order by ?label \n");
		
		query = b.toString();

	}
	public QDataSetContents() {
		super(query, "Show DataSet Contents");
	}
	
}