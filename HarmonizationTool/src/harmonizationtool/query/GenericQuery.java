package harmonizationtool.query;

public class GenericQuery extends HarmonyBaseQuery {


	public GenericQuery(String query, String label) {
		queryStr = query;
		this.label = label;
		System.out.println("Running a generic query:");
		if (queryStr.length() >500){
			System.out.println(queryStr.substring(0, 500)+" ....");
		}
		else {
			System.out.println(queryStr);
		}
	}
}
