package harmonizationtool.query;

import harmonizationtool.comands.SelectTDB;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.ModelProvider;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.shared.PrefixMapping;

public class HarmonyBaseQuery implements HarmonyQuery {

	protected String label = null;
	protected String queryStr = null;
	private List<String> data = null;
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
		// create QueryResults to hold the query results
		queryResults = new QueryResults();
		data = null;

		QueryExecution qexec = QueryExecutionFactory.create(query, SelectTDB.model);
//		System.out.println("model.getNsPrefixMap()"+SelectTDB.model.getNsPrefixMap().toString());
		
		try {
			long startTime = System.currentTimeMillis();
			ResultSet results = qexec.execSelect();
			List<String> newData = new ArrayList<String>();
			List<String> strList = results.getResultVars();
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
			ModelProvider modelProvider = new ModelProvider();
			queryResults.setModelProvider(modelProvider);
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				DataRow dataRow = new DataRow();
				modelProvider.addDataRow(dataRow);
				row = "";
				for (String header : strList) {
					try {
						RDFNode rdfNode = null;
						rdfNode = soln.get(header);
						if(rdfNode == null){
							row = row  + "\t";
							System.out.println("row="+row);
							dataRow.add("");
							
						}else{
							row = row + rdfNode.toString() + "\t";
							System.out.println("row="+row);
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
			System.out.println("data.toString()=" + data.toString());
		}else{
			System.out.println("data is null");
		}

		System.out.println("done");
	}

}