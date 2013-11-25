package harmonizationtool.query;

import com.sun.jmx.snmp.Timestamp;

public class IdsInfoQuery extends HarmonyBaseInsert {
	private final String dataSourceLid;
	private final String dataSourceName;
	private final String majorNumber;
	private final String minorNumber;
	private final String comment;

	// public IdsInfoQuery(String dataSourceIRI, String dataSourceName, String
	// majorNumber, String minorNumber, String comment){
	public IdsInfoQuery(String dataSourceLid, String dataSourceName, String majorNumber,
			String minorNumber, String comment) {

		this.dataSourceLid = dataSourceLid;
		this.dataSourceName = dataSourceName;
		this.majorNumber = majorNumber;
		this.minorNumber = minorNumber;
		this.comment = comment;
//		Timestamp timestamp = new Timestamp();
//		String ts = timestamp.toString();
//		System.out.println(ts.toString());

		label = this.dataSourceName;

		StringBuilder b = new StringBuilder();
		b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		b.append("PREFIX  ethold: <http://epa.gov/nrmrl/std/lca/ethold#> \n");
//		b.append("PREFIX  " + this.dataSourceIRI.toString()
//				+ ":  <http://data.lca.std.nrmrl.epa.gov/"
//				+ this.dataSourceIRI.toString() + "#> \n");
//		b.append("PREFIX  " + this.dataSourceIRI.toString()
//				+ ":  <http://data.lca.std.nrmrl.epa.gov/"
//				+ this.dataSourceIRI.toString() + "#> \n");
		b.append(" \n");
		b.append("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
		b.append("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
		b.append("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
		b.append(" \n");
		b.append("INSERT DATA  \n");
		b.append("{ \n");
		b.append("_:b0 rdf:type eco:DataSource ;  \n");
		b.append("          ethold:localSerialNumber " + this.dataSourceLid.toString() +" ; \n");
		b.append("          rdfs:label \"" + this.dataSourceName.toString()
				+ "\"^^xsd:string ;  \n");
		b.append("          rdfs:comment \"" + this.comment.toString()
				+ "\"^^xsd:string ;  \n");
		b.append("          eco:hasMajorVersionNumber \""
				+ this.majorNumber.toString() + "\"^^xsd:string ; \n"); // TEXT
																		// FOR
																		// NOW,
																		// BUT
																		// SHOULD
																		// BE
																		// FLOAT?
		b.append("          eco:hasMinorVersionNumber \""
				+ this.minorNumber.toString() + "\"^^xsd:string . \n"); // TEXT
																		// FOR
																		// NOW,
																		// BUT
																		// SHOULD
																		// BE
																		// FLOAT?
		b.append("} \n");
		queryStr = b.toString();
	}
}
