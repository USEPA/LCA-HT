package harmonizationtool.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QMatchCAS extends HarmonyBaseQuery implements IParamQuery {
//	private String primaryDataset;
	private int primaryID;
//	private String[] refDatasets;
	private int[] refIds;
	private String regex = "^(\\d+)\\s";
//	private String regex = "(\\d+)";

	private Pattern firstInt = Pattern.compile(regex);

	// String s ="xyz: 123a-45";
	// String patternStr="xyz:[ \\t]*([\\S ]+)";
	// Pattern p = Pattern.compile(patternStr);
	// Matcher m = p.matcher(s);
	// //System.err.println(s);
	// if(m.find()){
	// int count = m.groupCount();
	// System.out.println("group count is "+count);
	// for(int i=0;i<count;i++){
	// System.out.println(m.group(i));
	// }
	// }

	{
		label = "Show CAS Matches";
	}

	@Override
	public void setPrimaryDatset(String primaryDataset) {
//		this.primaryDataset = primaryDataset ;
		Matcher m = firstInt.matcher(primaryDataset);
		m.find();
//		System.out.println("Trying to match " + primaryDataset + " to " + m.toString());
		this.primaryID = Integer.parseInt(m.group(0).trim());
//		System.out.println("primaryDataset = " + primaryDataset);
	}

	@Override
	public void setRefDatasets(String[] refDatasets) {
//		this.refDatasets = refDatasets;
		this.refIds = new int[refDatasets.length];
		for (int i = 0; i < refDatasets.length; i++) {
			Matcher mr = firstInt.matcher(refDatasets[i]);
			mr.find();
			System.out.println("refDs of " + i + " = " + mr.group(0));
			this.refIds[i] = Integer.parseInt(mr.group(0).trim());
		}
	}

	@Override
	public String getQuery() {
		{
			try {
				StringBuilder b = new StringBuilder();
				b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
				b.append("PREFIX  ethold: <http://epa.gov/nrmrl/std/lca/ethold#> \n");
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
				b.append("SELECT (afn:localname(?s1) as ?q_sub) (str(?name) as ?q_name) (str(?cas) as ?same_cas) (str(?name2) as ?db_name) (str(?match_lid) as ?local_id) \n");
				b.append(" \n");
				b.append("WHERE { \n");
				b.append("      ?s1 eco:hasDataSource ?ds_prim . \n");
				b.append("      ?ds_prim ethold:localSerialNumber " + primaryID + " . \n");
				b.append("      ?s2 eco:hasDataSource ?ds_match . \n");
				b.append("      ?ds_match ethold:localSerialNumber ?match_lid . \n");
				b.append("      filter (?ds_prim != ?ds_match) \n");
				b.append("      ?s1 eco:casNumber ?cas .  \n");
				b.append("      ?s2 eco:casNumber ?cas .   \n");
				b.append("      ?s1 rdfs:label ?name . \n");
				b.append("      ?s2 rdfs:label ?name2 .  \n");
				b.append("      {{?s1 a eco:Flowable .  } UNION {?s1 a eco:Substance . }} \n");
				b.append("      {{?s2 a eco:Flowable .  } UNION {?s2 a eco:Substance . }} \n");
				b.append(" \n");
				b.append("      filter( \n");
//				System.out.println("refDatasets size = "+refDatasets.length);
				for (int i : refIds) {
					b.append(" ?match_lid  = " + i
							+ " || \n");
				}
				b.append("false) \n"); // THE false ALLOWS THE TRAILING OR (||) TO BE VALID
				b.append("} \n");
				b.append("order by ?s1 ?ds_match \n");
				queryStr = b.toString();
				System.out.println("queryStr = \n" + queryStr);
				return queryStr;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("queryStr = \n" + queryStr);

				queryStr = null;
				return queryStr;
			}

		}
	}
}
