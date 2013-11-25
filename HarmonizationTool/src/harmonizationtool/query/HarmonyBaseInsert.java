package harmonizationtool.query;

import harmonizationtool.QueryView;
import harmonizationtool.comands.SelectTDB;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.ModelProvider;
import harmonizationtool.utils.Util;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;// DO I NEED THIS?
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

public class HarmonyBaseInsert implements HarmonyQuery {

	protected String label = null;
	protected String queryStr = null;
	private List<String> data = null;
	private QueryResults queryResults = null;

	public HarmonyBaseInsert() {
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
//		Query query = QueryFactory.create(queryStr);
		Model model = SelectTDB.model;
		if(model== null){
			String msg = "ERROR no TDB open";
			Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
			return;
		}
//		String tdbDir = SelectTDB.tdbDir;
		Dataset dataset = SelectTDB.dataset;
		queryResults = new QueryResults();
		GraphStore graphStore = SelectTDB.graphStore;
		DataRow columnHeaders = new DataRow();
		queryResults.setColumnHeaders(columnHeaders);
		
		long change = model.size();
		
		columnHeaders.add("Model");
		columnHeaders.add("Size");
		
		System.err.printf("Before Update: %s\n", model.size());
//		data.add("Before Update");
//		data.add(""+model.size());
		
		ModelProvider modelProvider = new ModelProvider();
		queryResults.setModelProvider(modelProvider);
		DataRow dataRow = new DataRow();
		modelProvider.addDataRow(dataRow);
		dataRow.add("Before Update");
		dataRow.add(""+ model.size());

//		Resource s = model.createResource("<http://I>");
//		Property p = model.createProperty("<http://am>");
//		Resource o = model.createResource("<http://I>");
//		Statement statement = model.createStatement(s, p, o);
//		model.add(statement);
//		model.add(s, p, o);
//		model.add(s,p,"hello");

//		model.getResource(arg0);
		long startTime = System.currentTimeMillis();
		String sparqlUpdateString = queryStr;
		System.out.println("query = " + sparqlUpdateString.toString());
		UpdateRequest request = UpdateFactory.create(sparqlUpdateString);
		UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore);
		try {
			proc.execute();
//			dataset.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		float elapsedTimeSec = (System.currentTimeMillis() - startTime) / 1000F;
		System.out.println("Time elapsed: " + elapsedTimeSec);
		System.err.printf("After Update: %s\n", model.size());
//		data.add("After Update");
//		data.add("" + model.size());
		DataRow dataRow2 = new DataRow();
		modelProvider.addDataRow(dataRow2);
		dataRow2.add("After Update");
		dataRow2.add("" + model.size());
		
		change = model.size() - change;
		System.err.printf("Net Increase: %s\n", change);
		DataRow dataRow3 = new DataRow();
		modelProvider.addDataRow(dataRow3);
		
		String increase = "New Triples:";
		
		if (change < 0){
			increase = "Triples removed:";
			change=0-change;
		}
//		data.add(increase);
//		data.add("" + change);
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

}