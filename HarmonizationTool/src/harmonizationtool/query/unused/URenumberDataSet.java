package harmonizationtool.query.unused;

import harmonizationtool.query.HarmonyBaseUpdate;

public class URenumberDataSet extends HarmonyBaseUpdate {

	{
		label = "Rename a Data Set";
	}
	{
		StringBuilder b = new StringBuilder();
		String oldNum = "4";
		String newNum = "8";
		
		b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		b.append("PREFIX  ecogov: <http://epa.gov/nrmrl/std/lca/ecogov#> \n");
		b.append(" \n");
		b.append("DELETE {  \n");
		b.append("  ?ds ecogov:localSerialNumber "+oldNum+" . \n");	
		b.append("} \n");
		b.append("INSERT { \n");
		b.append("  ?ds ecogov:localSerialNumber "+newNum+" . \n");
		b.append("} \n");
		b.append(" \n");
		b.append("where { \n");
		b.append("  ?ds ecogov:localSerialNumber "+oldNum+" . \n");
		b.append("  ?ds a eco:DataSource . \n");
		b.append("} \n");

		queryStr = b.toString();
	}
}
