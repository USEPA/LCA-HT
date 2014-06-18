package harmonizationtool.query.unused;

import harmonizationtool.query.HarmonyBaseQuery;

public class ZAssignDSIndex extends HarmonyBaseQuery {
	{
		label = "Assign the next localSerialNumber to an unassigned TDBDataset";
	}
	{
		StringBuilder b = new StringBuilder();
		
		b.append("prefix :       <http://epa.gov/nrmrl/std/lca/ecogov#> \n");
		b.append("prefix ecogov: <http://epa.gov/nrmrl/std/lca/ecogov#> \n");
		b.append("prefix eco:    <http://ontology.earthster.org/eco/core#> \n");
		b.append("prefix skos:   <http://www.w3.org/2004/02/skos/core#> \n");
		b.append("prefix owl:    <http://www.w3.org/2002/07/owl#> \n");
		b.append("prefix xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
		b.append("prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		b.append("prefix rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		b.append("\n");
		b.append("\n");
		b.append("insert {?ds2 ecogov:localSerialNumber ?next . }\n");
		b.append("where {\n");
		b.append("  select ?ds2 ?next where {\n");
		b.append("    {\n");
		b.append("      select ?ds2 where {\n");
		b.append("        ?ds2 a eco:DataSource .\n");
		b.append("        filter (not exists {?ds2 ecogov:localSerialNumber ?num . })\n");
		b.append("      }\n");
		b.append("      limit 1\n");
		b.append("    }\n");
		b.append("    {\n");
		b.append("      select ((?biggest+1) as ?next) where {\n");
		b.append("        {\n");
		b.append("          select ?biggest where {\n");
		b.append("            bind (0 as ?biggest)\n");
		b.append("          }\n");
		b.append("        }\n");
		b.append("        union\n");
		b.append("        {\n");
		b.append("          select ?biggest where {\n");
		b.append("            optional{\n");
		b.append("              ?ds a eco:DataSource .\n");
		b.append("              ?ds ecogov:localSerialNumber ?biggest .\n");
		b.append("            }\n");
		b.append("          }\n");
		b.append("          order by desc(?biggest)\n");
		b.append("          limit 1\n");
		b.append("        }\n");
		b.append("      }\n");
		b.append("      order by desc(?biggest)\n");
		b.append("      limit 1\n");
		b.append("    }\n");
		b.append("  }\n");
		b.append("}\n");
		queryStr = b.toString();

	}
}