package harmonizationtool.query.unused;

import harmonizationtool.query.HarmonyBaseUpdate;
import harmonizationtool.query.IIntQuery;

public class ZAssignDSIndex_with_param extends HarmonyBaseUpdate implements IIntQuery {
	private int next;
//	private String primaryDataset;
//	private String[] refDatasets;
	{
		label = "Assign a localSerialNumber to one unassigned dataset";
	}
	@Override
	public void setNext(int next) {
		this.next = next;
	}
	
//	@Override
//	public void setPrimaryDatset(String primaryDataset) {
//		this.primaryDataset = primaryDataset;
//		
//	}
//	@Override
//	public void setRefDatasets(String[] refDatasets) {
//		this.refDatasets = refDatasets;
//		
//	}
	@Override
	public String getQuery() {
		{
			
			try {
				StringBuilder b = new StringBuilder();
				b.append("##\n");
				b.append("prefix ecogov: <http://epa.gov/nrmrl/std/lca/ecogov#> \n");
				b.append("prefix eco:    <http://ontology.earthster.org/eco/core#> \n");
				b.append("\n");
				b.append("insert {?ds_bn ecogov:localSerialNumber "+this.next+" . }\n");
				b.append("where {\n");
				b.append("  select ?ds_bn where {\n");
				b.append("    ?ds_bn a eco:DataSource .\n");
				b.append("    filter (not exists {?ds_bn ecogov:localSerialNumber ?num . })\n");
				b.append("  }\n");
				b.append("limit 1\n");
				b.append("}\n");

	
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
