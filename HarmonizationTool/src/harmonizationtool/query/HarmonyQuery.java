package harmonizationtool.query;

import java.util.List;

public interface HarmonyQuery {
	String getLabel();
	String getQuery();
	List<String> getData();
//	List<String> getDataXform();
	QueryResults getQueryResults();
//	QueryResults getQueryResults();
}
