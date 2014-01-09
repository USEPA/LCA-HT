package harmonizationtool.query;

public class URenameDataSet extends HarmonyBaseUpdate {

	{
		label = "Rename a Data Set";
	}
	{
		StringBuilder b = new StringBuilder();
		String oldName = "ds_004";
		String newName = "ds_014";
		
		b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		b.append("PREFIX  ethold: <http://epa.gov/nrmrl/std/lca/ethold#> \n");
		b.append("PREFIX  td_999:  <http://data.lca.std.nrmrl.epa.gov/test_data_999#> \n");
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
		b.append("DELETE {  \n");
		b.append("  ?s ?p ?o . \n");
		b.append("  eco:"+oldName+" ?p1 ?o1 . \n");
		b.append("} \n");
		b.append("INSERT { \n");
		b.append("  ?s eco:hasDataSource eco:"+newName+" .  \n");
		b.append("  eco:"+newName+" ?p1 ?o1 . \n");
		b.append("} \n");
		b.append(" \n");
		b.append("where { \n");
		b.append("  ?s eco:hasDataSource eco:"+oldName+" . \n");
		b.append("  ?s ?p ?o . \n");
		b.append("  eco:"+oldName+" ?p1 ?o1 . \n");
		b.append("} \n");

		queryStr = b.toString();
	}
}
