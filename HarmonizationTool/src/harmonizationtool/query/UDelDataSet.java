package harmonizationtool.query;

public class UDelDataSet extends HarmonyBaseInsert implements HarmonyUpdate {

	{
		label = "Delete data set...";
	}
	{
		StringBuilder b = new StringBuilder();
		
		b.append("##\n");
		b.append("prefix :       <http://epa.gov/nrmrl/std/lca/ethold#>\n");
		b.append("prefix ethold: <http://epa.gov/nrmrl/std/lca/ethold#>\n");
		b.append("prefix eco:    <http://ontology.earthster.org/eco/core#>\n");
		b.append("prefix skos:   <http://www.w3.org/2004/02/skos/core#>\n");
		b.append("prefix owl:    <http://www.w3.org/2002/07/owl#>\n");
		b.append("prefix xsd:    <http://www.w3.org/2001/XMLSchema#>\n");
		b.append("prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
		b.append("prefix rdfs:   <http://www.w3.org/2000/01/rdf-schema#>\n");
		b.append("\n");
		b.append("delete {\n");
		b.append("  ?ds ?p1 ?o1 .\n");
		b.append("  ?s2 ?p2 ?o2 .\n");
		b.append("}\n");
		b.append("where {\n");
		b.append("  ?ds ?p1 ?o1 .\n");
		b.append("  ?s2 ?p2 ?o2 .\n");
		b.append("  ?ds a eco:DataSource .\n");
		b.append("  ?ds ethold:localSerialNumber <<local_id>> .\n");
		b.append("  ?s2 eco:hasDataSource ?ds .\n");
		b.append("}\n");
		b.append("\n");
		queryStr = b.toString();
	}
}
