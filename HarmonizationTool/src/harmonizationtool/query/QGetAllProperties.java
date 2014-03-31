package harmonizationtool.query;

import com.hp.hpl.jena.query.ResultSet;

public class QGetAllProperties extends HarmonyQuery2Impl implements LabeledQuery {
	public static final String LABEL = "Get Properties";

	public QGetAllProperties() {
		super();
	}
	
	public ResultSet getResultSet() {
		// BUILD THE QUERY USING THE PARAMETERS
		buildQuery();
		// READY TO CALL getResultSet() ON THESUPER CLASS
		return super.getResultSet();
	}

	private void buildQuery() {
		StringBuilder b = new StringBuilder();
		b.append("SELECT distinct ?p where {?s ?p ?o .} \n");
		setQuery(b.toString());
	}

	@Override
	public String getLabel() {
		return LABEL;
	}
}