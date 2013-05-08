package harmonizationtool.query;

public class ZunusedDDataQuery extends HarmonyBaseQuery {

	{
		label = "DData";
	}
	{
		StringBuilder b = new StringBuilder();
		b.append("PREFIX  eco:  <http://ontology.earthster.org/eco/core#> \n");
		b.append(" \n");
		b.append("DELETE { \n");
		b.append("  ?s ?p ?o . \n");
		b.append("  ?v ?w ?x . \n");
		b.append("} \n");
		b.append("WHERE \n");
		b.append("  { ?s ?p ?o . \n");
		b.append("    ?s eco:hasDataSource eco:ACToR2012.1 . \n");
		b.append("    eco:ACToR2012.1 ?w ?x \n");
		b.append("  } \n");
		queryStr =  b.toString();		
	}
}
