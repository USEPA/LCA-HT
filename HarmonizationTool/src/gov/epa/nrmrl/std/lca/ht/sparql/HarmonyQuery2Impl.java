package gov.epa.nrmrl.std.lca.ht.sparql;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;

public class HarmonyQuery2Impl implements HarmonyQuery2 {
	private String query = null;

	public HarmonyQuery2Impl() {
	}

	@Override
	public ResultSet getResultSet() throws IllegalArgumentException {
		if (query == null) {
			throw new IllegalArgumentException("query cannot be null");
		}
		Model tdbModel = ActiveTDB.getModel(null);
		if (tdbModel == null) {
			throw new IllegalArgumentException("ActiveTDB.tdbModel is null");
		}
		QueryExecution qexec = QueryExecutionFactory.create(query, tdbModel);
		ResultSetRewindable resultSetRewindable = ResultSetFactory.copyResults(qexec.execSelect());
		return resultSetRewindable;
	}

	public ResultSet getResultSet(String graphName) throws IllegalArgumentException {
		System.out.println("query=\n" + query);
		if (query == null) {
			throw new IllegalArgumentException("query cannot be null");
		}

		Model model = ActiveTDB.getModel(graphName);

		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSetRewindable resultSetRewindable = ResultSetFactory.copyResults(qexec.execSelect());
		return resultSetRewindable;
	}

	public ResultSet getResultSet(Model model) throws IllegalArgumentException {
		System.out.println("query=\n" + query);
		if (query == null) {
			throw new IllegalArgumentException("query cannot be null");
		}
		if (model == null) {
			model = ActiveTDB.getModel(null);
		}
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSetRewindable resultSetRewindable = ResultSetFactory.copyResults(qexec.execSelect());
		return resultSetRewindable;
	}

	@Override
	public String getQuery() {
		return query;
	}

	@Override
	public void setQuery(String query) {
		this.query = query;
	}

	public void getParamaterFromUser() {
	}

}
