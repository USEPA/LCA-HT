package harmonizationtool.query;

import harmonizationtool.comands.SelectTDB;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.sun.tools.corba.se.idl.InvalidArgument;

public class HarmonyQuery2Impl implements HarmonyQuery2 {
	private String query;
	private String parameterizedQuery = null;
	private List<String> parameters = new ArrayList<String>();
	private String parameterToken = null;
	private boolean requiresParameters = false;

	public HarmonyQuery2Impl(String query, String parameterToken) {
		this.query = query;
		this.parameterToken  = parameterToken;
		requiresParameters = true;
	}
	public HarmonyQuery2Impl(String query) {
		this.query = query;
		this.parameterizedQuery = query;
		requiresParameters = false;
	}

	@Override
	public String getQuery() {
		return query;
	}

	@Override
	public ResultSet getResultSet() throws InvalidArgument {
		processParameters();
		Model model = SelectTDB.model;
		if(model== null){
			throw new InvalidArgument("SelectTDB.model is null");
		}

		QueryExecution qexec = QueryExecutionFactory.create(parameterizedQuery, model);
		ResultSetRewindable resultSetRewindable = ResultSetFactory.copyResults(qexec.execSelect());

		return resultSetRewindable;
	}
	
	public ResultSet getResultSet(Model model) throws InvalidArgument {
		processParameters();
		if(model== null){
			throw new InvalidArgument("(RDF) model is null");
		}

		QueryExecution qexec = QueryExecutionFactory.create(parameterizedQuery, model);
		ResultSetRewindable resultSetRewindable = ResultSetFactory.copyResults(qexec.execSelect());

		return resultSetRewindable;
	}


	@Override
	public void setParameters(String... parameters) throws InvalidArgument {
		this.parameters.clear();
		for(String parameter : parameters){
			this.parameters.add(parameter);
		}
		String[] splitQuery = query.split(parameterToken);
		if(splitQuery.length != this.parameters.size()+1){
			throw new InvalidArgument("Parameter mismatch");
		}
	}
	private void processParameters() throws InvalidArgument{
		String[] splitQuery = query.split(parameterToken);
		if(splitQuery.length != parameters.size()+1){
			throw new InvalidArgument("Parameter mismatch");
		}
		StringBuilder b = new StringBuilder();
		int parameterIndex = 0;
		for(String s : splitQuery){
			b.append(s);
			if (parameterIndex < parameters.size()) {
				b.append(" " + parameters.get(parameterIndex++));
			}
		}
		parameterizedQuery = b.toString();
	}
	@Override
	public String getParameterizedQuery() {
		return parameterizedQuery;
	}
	@Override
	public boolean requiresParameters() {
		return requiresParameters ;
	}

}
