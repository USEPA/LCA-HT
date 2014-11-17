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
	public ResultSet getResultSet() {
		System.out.println("query=\n"+query);
		if(query == null){
			throw new IllegalArgumentException("query cannot be null");
		}
		if(ActiveTDB.getModel() == null){
			throw new IllegalArgumentException("ActiveTDB.tdbModel is null");
		}
		ActiveTDB.sync();
		QueryExecution qexec = QueryExecutionFactory.create(query, ActiveTDB.getModel());
		ResultSetRewindable resultSetRewindable = ResultSetFactory.copyResults(qexec.execSelect());

		return resultSetRewindable;
	}
	
	public ResultSet getResultSet(Model model) throws IllegalArgumentException {
		System.out.println("query=\n"+query);
		if(query == null){
			throw new IllegalArgumentException("query cannot be null");
		}
		if(model== null){
			throw new IllegalArgumentException("(RDF) tdbModel is null");
		}
		
		ActiveTDB.sync();
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
	
	public void getParamaterFromUser() {}


}
