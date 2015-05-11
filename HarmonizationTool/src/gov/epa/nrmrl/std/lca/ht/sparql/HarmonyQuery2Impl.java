package gov.epa.nrmrl.std.lca.ht.sparql;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

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
		Model model = null;
		if (graphName == null) {
			model = ActiveTDB.getModel(graphName);
		} else {
			if (graphName.startsWith(LCAHT.NS)) {
				model = ActiveTDB.getModel(graphName);
			} else if (graphName.equals(ActiveTDB.importPlusDefault)) {
				model = ModelFactory.createUnion(ActiveTDB.getModel(ActiveTDB.importGraphName),
						ActiveTDB.getModel(null));
			}
		}
		if (model == null) {
			return null;
		}
		long timeStart = System.currentTimeMillis();
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSetRewindable resultSetRewindable = ResultSetFactory.copyResults(qexec.execSelect());
		long timeStop = System.currentTimeMillis();
		long time = timeStop - timeStart;
		System.out.println("Time elapsed: " + time);
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
