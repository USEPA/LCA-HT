package gov.epa.nrmrl.std.lca.ht.sparql;

import java.util.List;

public interface HarmonyUpdate {
	String getLabel();
	String getQuery();
	List<String> getData();
	QueryResults getQueryResults();
}
