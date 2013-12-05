package harmonizationtool.query;

public class ZGetNextDSIndex extends HarmonyBaseQuery {
	{
		label = "Assign the next localSerialNumber to an unassigned dataset";
	}
	{
		StringBuilder b = new StringBuilder();
		
		b.append("##\n");
		b.append("prefix ethold: <http://epa.gov/nrmrl/std/lca/ethold#> \n");
		b.append("prefix eco:    <http://ontology.earthster.org/eco/core#> \n");
		b.append("\n");
		b.append("select (str(?biggest+1) as ?next) where {\n");
		b.append("  {\n");
		b.append("    select ?biggest where {\n");
		b.append("      bind (0 as ?biggest)\n");
		b.append("    }\n");
		b.append("  }\n");
		b.append("  union\n");
		b.append("  {\n");
		b.append("    select ?biggest where {\n");
		b.append("      optional{\n");
		b.append("        ?ds a eco:DataSource .\n");
		b.append("        ?ds ethold:localSerialNumber ?biggest .\n");
		b.append("      }\n");
		b.append("    }\n");
		b.append("    order by desc(?biggest)\n");
		b.append("    limit 1\n");
		b.append("  }\n");
		b.append("}\n");
		b.append("order by desc(?biggest)\n");
		b.append("limit 1\n");

		queryStr = b.toString();

	}
}