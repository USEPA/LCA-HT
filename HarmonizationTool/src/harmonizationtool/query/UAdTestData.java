package harmonizationtool.query;

public class UAdTestData extends HarmonyBaseInsert {

	{
		label = "test insert";
	}
	{
		StringBuilder b = new StringBuilder();
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
		b.append(" \n");
		b.append("INSERT DATA  \n");
		b.append("{ \n");
		b.append(" \n");
		b.append("eco:test_data_set_999 rdf:type eco:DataSource , \n");
		b.append("                   owl:NamedIndividual ; \n");
		b.append(" \n");
		b.append("          rdfs:label \"test_data_999\" ; \n");
		b.append("          rdfs:comment \"User data with user description... from a file called: TRACI2.1.xlsx with size: 1,444,917 bytes and md5: 1821879b5ab014e0ec9192c3d1a2d6d4.\" ; \n");
		b.append(" \n");
		b.append("          eco:hasMajorVersionNumber 0 ; \n");
		b.append("          eco:hasMinorVersionNumber 1 . \n");
		b.append(" \n");
		b.append("td_999:row_0001 rdf:type eco:Substance , \n");
		b.append("                        owl:NamedIndividual ; \n");
		b.append("               eco:hasDataSource eco:qd001 ; \n");
		b.append("               eco:casNumber \"50-00-0\"^^xsd:string ; \n");
		b.append("               rdfs:label \"Formaldehyde\"^^xsd:string ; \n");
		b.append("               skos:altLabel \"Bad\"^^xsd:string . \n");
		b.append(" \n");
		b.append("td_999:row_0002 rdf:type eco:Substance , \n");
		b.append("                        owl:NamedIndividual ; \n");
		b.append("               eco:hasDataSource eco:qd001 ; \n");
		b.append("               eco:casNumber \"25-32-1\"^^xsd:string ; \n");
		b.append("               rdfs:label \"Benzene\"^^xsd:string ; \n");
		b.append("               skos:altLabel \"Poison\"^^xsd:string . \n");
		b.append("# ... \n");
		b.append(" \n");
		b.append("td_999:row_3211 rdf:type eco:Substance , \n");
		b.append("                        owl:NamedIndividual ; \n");
		b.append("               eco:hasDataSource eco:qd001 ; \n");
		b.append("               eco:casNumber \"925-12-4\"^^xsd:string ; \n");
		b.append("               rdfs:label \"Bisphenol A\"^^xsd:string ; \n");
		b.append("               skos:altLabel \"Bad Stuff\"^^xsd:string . \n");
		b.append("} \n");
		queryStr = b.toString();
	}
}
