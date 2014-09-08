package gov.epa.nrmrl.std.lca.ht.sparql;

import gov.epa.nrmrl.std.lca.ht.views.QueryView;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class GenericQuery extends HarmonyBaseQuery {


	public GenericQuery(String query, String label) {
		queryStr = query;
//		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//		QueryView queryView = (QueryView) page.findView(QueryView.ID);
//		queryView.setTextAreaContent(queryStr);
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
