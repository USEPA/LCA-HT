package gov.epa.nrmrl.std.lca.ht.sparql;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;

public class HarmonyQuery2Impl implements HarmonyQuery2 {
	private String graphName = null;
	private String query = null;

	public HarmonyQuery2Impl() {

	}

	@Override
	public ResultSet getResultSet() throws IllegalArgumentException {
		if (query == null) {
			throw new IllegalArgumentException("query cannot be null");
		}

		Model model = ActiveTDB.getModel(graphName);

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

	public void setGraphName(String graphNameToUse) {
		graphName = graphNameToUse;
	}

}
