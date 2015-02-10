package gov.epa.nrmrl.std.lca.ht.sparql;

public class GenericUpdate extends HarmonyBaseUpdate {

	public GenericUpdate(String query, String label) {
		queryStr = query;
		graphName = null;
		this.label = label;
		System.out.println("Running a generic update:");
	}

	public GenericUpdate(String query, String label, String graphNameToUse) {
		graphName = graphNameToUse;
		queryStr = query;
		this.label = label;
		System.out.println("Running a generic update:");
	}
}
