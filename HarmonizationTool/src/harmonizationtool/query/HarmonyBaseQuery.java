package harmonizationtool.query;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.views.QueryView;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.TableProvider;
import harmonizationtool.utils.Util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.shared.PrefixMapping;

public abstract class HarmonyBaseQuery implements HarmonyQuery {

	protected String label = null;
	protected String queryStr = null;
	private List<String> data = null;
//	private List<String> dataXform = null;
	private QueryResults queryResults = null;

	public QueryResults getQueryResults() {
		return queryResults;
	}

	public List<String> getData() {
		// if(data == null){
		executeQuery();
		// }
		return data;
	}

//	public List<String> getDataXform() {
//		// if(data == null){
//		executeQuery(true);
//		// }
//		return dataXform;
//	}

	public HarmonyBaseQuery() {
		super();
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return label;
	}

	@Override
	public String getQuery() {
		return queryStr;
	}

	private void executeQuery() {
		Query query = QueryFactory.create(queryStr);

		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		QueryView queryView = (QueryView) page
				.findView("HarmonizationTool.QueryViewID");
		queryView.setTextAreaContent(queryStr);

		// create QueryResults to hold the query results
		queryResults = new QueryResults();
		data = null;
		if (ActiveTDB.tdbModel == null) {
//			String msg = "ERROR no TDB open";
//			Util.findView(QueryView.ID).getViewSite().getActionBars()
//					.getStatusLineManager().setMessage(msg);
			return;
		}

		QueryExecution qexec = QueryExecutionFactory.create(query,
				ActiveTDB.tdbModel);
		// System.out.println("tdbModel.getNsPrefixMap()"+ActiveTDB.tdbModel.getNsPrefixMap().toString());

		try {
			long startTime = System.currentTimeMillis();
			// ResultSet results = qexec.execSelect();
			ResultSetRewindable resultSetRewindable = ResultSetFactory
					.copyResults(qexec.execSelect());
			System.out
					.println("OK, here comes your CSV contents... I hope it doesn't swamp your system...");
			// ResultSetRewindable resultSetRewindable =
			// ResultSetFactory.copyResults(results);
			ResultSetFormatter.outputAsCSV(resultSetRewindable);
			resultSetRewindable.reset();

			List<String> newData = new ArrayList<String>();
			List<String> strList = resultSetRewindable.getResultVars();
			// create DataRow to hold column headers
			DataRow columnHeaders = new DataRow();
			// add the columnHeaders to the queryResults structure
			queryResults.setColumnHeaders(columnHeaders);
			String row = "";
			for (String header : strList) {
				header = header + "\t";
				row = row + header;
				// add a header to the columnHeaders dataRow
				columnHeaders.add(header);
			}
			newData.add(row);
			TableProvider tableProvider = new TableProvider();
			queryResults.setTableProvider(tableProvider);
			for (; resultSetRewindable.hasNext();) {
				QuerySolution soln = resultSetRewindable.nextSolution();
				DataRow dataRow = new DataRow();
				tableProvider.addDataRow(dataRow);
				row = "";
				for (String header : strList) {
					try {
						RDFNode rdfNode = null;
						rdfNode = soln.get(header);
						if (rdfNode == null) {
							row = row + "\t";
							// System.out.println("row="+row);
							dataRow.add("");

						} else {
							row = row + rdfNode.toString() + "\t";
							// System.out.println("row="+row);
							dataRow.add(rdfNode.toString());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				newData.add(row);
			}
			data = newData;
			float elapsedTimeSec = (System.currentTimeMillis() - startTime) / 1000F;
			System.out.println("Time elapsed: " + elapsedTimeSec);
		} catch (Exception e) {
			System.err.print("Error:" + e.getMessage());
		} finally {
			qexec.close();
		}
		if (data != null) {
			System.out.println("data.size()=" + data.size());
			// System.out.println("data.toString()=" + data.toString());
		} else {
			System.out.println("data is null");
		}

		System.out.println("done");
	}
}