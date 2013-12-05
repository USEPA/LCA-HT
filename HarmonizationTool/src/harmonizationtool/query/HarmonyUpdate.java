package harmonizationtool.query;

import java.util.List;

public interface HarmonyUpdate {
	String getLabel();
	String getQuery();
	List<String> getData();
	QueryResults getQueryResults();
}
