package harmonizationtool.query;

public class GenericQuery extends HarmonyBaseQuery {


	public GenericQuery(String query, String label) {
		queryStr = query;
		this.label = label;
		System.out.println("Running a generic query:");
		if (queryStr.length() >5000){
			System.out.println(queryStr.substring(0, 5000)+" ....");
		}
		else {
			System.out.println(queryStr);
		}
	}
}
