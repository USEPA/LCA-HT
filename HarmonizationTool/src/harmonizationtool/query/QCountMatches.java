package harmonizationtool.query;

//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

public class QCountMatches extends HarmonyBaseQuery implements IParamQuery {
	{
		label = "Count CAS matches";
	}

	private String primaryID;
	private String[] refIds;
//	private String regex = "^(\\d+)\\s";
//	private Pattern firstInt = Pattern.compile(regex);
	
	@Override
	public void setPrimaryDataSet(String primaryDataSet) {
//		this.primaryDataset = primaryDataset ;
//		Matcher m = firstInt.matcher(primaryDataset);
//		m.find();
//		System.out.println("Trying to match " + primaryDataset + " to " + m.toString());
//		primaryID = Integer.parseInt(m.group(0).trim());
//		System.out.println("primaryDataset = " + primaryDataset);
		primaryID = primaryDataSet;
	}

	@Override
	public void setReferenceDataSets(String[] refDataSets) {
//		this.refDatasets = refDatasets;
//		this.refIds = new int[refDatasets.length];
//		for (int i = 0; i < refDatasets.length; i++) {
//			Matcher mr = firstInt.matcher(refDatasets[i]);
//			mr.find();
//			System.out.println("refDs of " + i + " = " + mr.group(0));
//			this.refIds[i] = Integer.parseInt(mr.group(0).trim());
//		}
		refIds = refDataSets ;
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
				b.append("select  (str(?match_label) as ?matching_set) ?cas_plus_name ?cas_minus_name \n");
				b.append("WHERE { \n");
				b.append(" \n");
				b.append("{SELECT (str(count(distinct(?s1))) as ?cas_plus_name) ?match_label \n");
				b.append(" \n");
				b.append("  WHERE { \n");
				b.append("  ?s1 eco:hasDataSource ?ds_prim . \n");
				b.append("  ?ds_prim rdfs:label \"" + primaryID + "\"^^xsd:string . \n");
				b.append("  ?s2 eco:hasDataSource ?ds_match . \n");
				b.append("  ?ds_match rdfs:label ?match_label . \n");
				b.append("  filter (?ds_prim != ?ds_match) \n");
				b.append("  ?s1 eco:casNumber ?cas .  \n");
				b.append("  ?s2 eco:casNumber ?cas .   \n");
				b.append("  ?s1 rdfs:label ?name . \n");
				b.append("  ?s2 rdfs:label ?name2 .  \n");
				b.append("  filter (fn:upper-case(?name) = fn:upper-case(?name2)) \n");
				b.append("  {{?s1 a eco:Flowable .  } UNION {?s1 a eco:Substance . }} \n");
				b.append("  {{?s2 a eco:Flowable .  } UNION {?s2 a eco:Substance . }} \n");
				b.append("      filter( \n");
				for (String refDS : refIds) {
					b.append(" str(?match_label)  = \"" + refDS	+ "\" || \n");
				}
				b.append("false) \n"); // THE false ALLOWS THE TRAILING OR (||) TO BE VALID
				b.append("  } \n");
				b.append("  group by ?match_label \n");
				b.append("  order by ?match_label \n");
				b.append("} \n");
				b.append(" \n");
				b.append("{SELECT (str(count(distinct(?s1))) as ?cas_minus_name) ?match_label \n");
				b.append(" \n");
				b.append("  WHERE { \n");
				b.append("  ?s1 eco:hasDataSource ?ds_prim . \n");
//				b.append("  ?ds_prim ethold:localSerialNumber " + primaryID + " . \n");
				b.append("  ?ds_prim rdfs:label \"" + primaryID + "\"^^xsd:string . \n");
				b.append("  ?s2 eco:hasDataSource ?ds_match . \n");
				b.append("  ?ds_match rdfs:label ?match_label . \n");
				b.append("  filter (?ds_prim != ?ds_match) \n");
				b.append("  ?s1 eco:casNumber ?cas .  \n");
				b.append("  ?s2 eco:casNumber ?cas .   \n");
				b.append("  ?s1 rdfs:label ?name . \n");
				b.append("  ?s2 rdfs:label ?name2 .  \n");
				b.append("  filter (fn:upper-case(?name) != fn:upper-case(?name2)) \n");
				b.append("  ?s1 a eco:Flowable .  \n");
				b.append("  ?s2 a eco:Flowable .  \n");
				b.append("      filter( \n");
//				for (String i : refIds) {
				for (String refDS : refIds) {
					b.append("str(?match_label)  = \"" + refDS	+ "\" || \n");
				}
				b.append("false) \n"); // THE false ALLOWS THE TRAILING OR (||) TO BE VALID
				b.append("  } \n");
				b.append("group by ?match_label \n");
				b.append("order by ?match_label \n");
				b.append("} \n");
				b.append("} \n");
				queryStr = b.toString();
				return queryStr;
			} catch (Exception e) {
				e.printStackTrace();
				queryStr = null;
				return queryStr;
			}
		}
	}
}
