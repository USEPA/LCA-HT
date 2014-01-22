package harmonizationtool.query.unused;

import harmonizationtool.query.HarmonyBaseQuery;

public class ZunusedNonSubstanceQuery extends HarmonyBaseQuery {

	{
		label = "NonSubstance";
	}
	{
		StringBuilder b = new StringBuilder();
		b.append("prefix eco: <http://ontology.earthster.org/eco/core#> \n");
		b.append(" \n");
		b.append("select  ?s ?p ?o  where { \n");
		b.append("?s ?p ?o . \n");
		b.append("(exists {?s eco:hasDataSource ?q} . )\n");
//		b.append("!exists {?s eco:casNumber ?r} . \n");
		b.append("filter !regex(str(?s),\"http://data.earthster.org/refdata\") \n");
		b.append("} \n");
		queryStr = b.toString();
	}
}
