package harmonizationtool.query;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.TableProvider;
import harmonizationtool.vocabulary.ECO;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.RDF;

public class HarmonyBaseUpdate implements HarmonyQuery {

	protected String label = null;
	protected String queryStr = null;
	private List<String> data = null;
	private QueryResults queryResults = null;

	public HarmonyBaseUpdate() {
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
		// Query query = QueryFactory.create(queryStr);
		Model model = ActiveTDB.tdbModel;
		if (model == null) {
			// String msg = "ERROR no TDB open";
			// Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
			return;
		}

		queryResults = new QueryResults();
		GraphStore graphStore = ActiveTDB.graphStore;
		DataRow columnHeaders = new DataRow();
		queryResults.setColumnHeaders(columnHeaders);

		long change = model.size();

		columnHeaders.add("Model");
		columnHeaders.add("Size");

		System.err.printf("Before Update: %s\n", model.size());
		// data.add("Before Update");
		// data.add(""+tdbModel.size());

		TableProvider tableProvider = new TableProvider();
		queryResults.setTableProvider(tableProvider);
		DataRow dataRow = new DataRow();
		tableProvider.addDataRow(dataRow);
		dataRow.add("Before Update");
		dataRow.add("" + model.size());

		// Resource s = tdbModel.createResource("<http://I>");
		// Property p = tdbModel.createProperty("<http://am>");
		// Resource o = tdbModel.createResource("<http://I>");
		// Statement statement = tdbModel.createStatement(s, p, o);
		// tdbModel.add(statement);
		// tdbModel.add(s, p, o);
		// tdbModel.add(s,p,"hello");

		// tdbModel.getResource(arg0);
		long startTime = System.currentTimeMillis();
		String sparqlUpdateString = queryStr;
		System.out.println("query = " + sparqlUpdateString.toString());

		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		try {
			UpdateRequest request = UpdateFactory.create(sparqlUpdateString);
			UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore);
			proc.execute();
			ActiveTDB.tdbDataset.commit();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---

		float elapsedTimeSec = (System.currentTimeMillis() - startTime) / 1000F;
		System.out.println("Time elapsed: " + elapsedTimeSec);
		System.err.printf("After Update: %s\n", model.size());
		// data.add("After Update");
		// data.add("" + tdbModel.size());
		DataRow dataRow2 = new DataRow();
		tableProvider.addDataRow(dataRow2);
		dataRow2.add("After Update");
		dataRow2.add("" + model.size());

		change = model.size() - change;
		System.err.printf("Net Increase: %s\n", change);
		DataRow dataRow3 = new DataRow();
		tableProvider.addDataRow(dataRow3);

		String increase = "New Triples:";

		if (change < 0) {
			increase = "Triples removed:";
			change = 0 - change;
		}
		// data.add(increase);
		// data.add("" + change);
		dataRow3.add(increase);
		dataRow3.add("" + change);

		System.out.println("done");
	}

	@Override
	public List<String> getData() {
		data = new ArrayList<String>();
		executeQuery();
		return data;
	}

	@Override
	public QueryResults getQueryResults() {
		return queryResults;
	}
	//
	// @Override
	// public List<String> getDataXform() {
	// // TODO Auto-generated method stub
	// return null;
	// }

}