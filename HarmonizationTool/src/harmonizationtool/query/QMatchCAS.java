package harmonizationtool.query;

public class QMatchCAS extends HarmonyBaseQuery implements IParamQuery {
	private String primaryDataset;
	private String[] refDatasets;
	{
		label = "Show CAS Matches";
	}
	@Override
	public void setPrimaryDatset(String primaryDataset) {
		this.primaryDataset = primaryDataset;
		
	}
	@Override
	public void setRefDatasets(String[] refDatasets) {
		this.refDatasets = refDatasets;
		
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
				b.append(" \n");
				b.append("SELECT (afn:localname(?s1) as ?q_sub) (str(?name) as ?q_name) (str(?cas) as ?same_cas) (str(?name2) as ?db_name) (afn:localname(?s2) as ?db_sub) (afn:localname(?ds_match) as ?from_data_set) \n");
				b.append(" \n");
				b.append("WHERE { \n");
				b.append("      ?s1 eco:hasDataSource eco:"+primaryDataset+" . \n");
				b.append("      ?s2 eco:hasDataSource ?ds_match . \n");
				b.append("      ?s1 eco:casNumber ?cas .  \n");
				b.append("      ?s2 eco:casNumber ?cas .   \n");
				b.append("      ?s1 rdfs:label ?name . \n");
				b.append("      ?s2 rdfs:label ?name2 .  \n");
				b.append("      ?s1 a eco:Substance .  \n");
				b.append("      ?s2 a eco:Substance .  \n");
				b.append(" \n");
				b.append("      filter( \n");
				for (String s:refDatasets){
					b.append("(str(afn:localname(?ds_match)) = \""+s+"\") || \n");
				}
				b.append("(false)) \n"); // THE false ALLOWS THE TRAILING OR (||) TO BE VALID
				b.append("      filter(str(afn:localname(?ds_match)) != \""+primaryDataset+"\") . \n");
				b.append("} \n");
				b.append("order by ?s1 ?ds_match \n");
				queryStr = b.toString();
				System.out.println("queryStr = \n"+queryStr);
				return queryStr;
			} catch (Exception e) {
				e.printStackTrace();
				queryStr = null;
				return queryStr;
			}

		}
	}
}
