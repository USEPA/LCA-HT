package harmonizationtool.query;

import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
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
			return;
		}
		long startModelSize = model.size();

		queryResults = new QueryResults();
		GraphStore graphStore = ActiveTDB.graphStore;
		DataRow columnHeaders = new DataRow();
		queryResults.setColumnHeaders(columnHeaders);

		columnHeaders.add("Model");
		columnHeaders.add("Size");

		System.err.printf("Before Update: %s\n", startModelSize);
		// data.add("Before Update");
		// data.add(""+tdbModel.size());

		TableProvider tableProvider = new TableProvider();
		queryResults.setTableProvider(tableProvider);
		DataRow dataRow = new DataRow();
		tableProvider.addDataRow(dataRow);
		dataRow.add("Before Update");
		dataRow.add("" + startModelSize);

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
		long endModelSize = model.size();
		System.err.printf("After Update: %s\n", endModelSize);
		// data.add("After Update");
		// data.add("" + tdbModel.size());
		DataRow dataRow2 = new DataRow();
		tableProvider.addDataRow(dataRow2);
		dataRow2.add("After Update");
		dataRow2.add("" + endModelSize);
		
		DataRow dataRow3 = new DataRow();
		tableProvider.addDataRow(dataRow3);
		
		long modelSizeChange = endModelSize - startModelSize;
		String message = "New Triples:";

		if (modelSizeChange > 0) {
			System.err.printf("Net Increase: %s\n", modelSizeChange);
		} else {
			message = "Triples removed:";
			modelSizeChange = 0 - modelSizeChange;
		}

		dataRow3.add(message);
		dataRow3.add("" + modelSizeChange);

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