package gov.epa.nrmrl.std.lca.ht.sparqlQueries;

import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.LabeledQuery;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;

import com.hp.hpl.jena.query.ResultSet;

public class QContentsWithoutDataset extends HarmonyQuery2Impl implements LabeledQuery {
	public static final String LABEL = "Items NOT IN a dataset";

	public QContentsWithoutDataset() {
		super();
	}

	public ResultSet getResultSet() {
		// CALL THE DIALOG TO GET THE PARAMETERS
		// getDialog();
		// BUILD THE QUERY USING THE PARAMETERS
		buildQuery();
		// READY TO CALL getResultSet() ON THESUPER CLASS
		return super.getResultSet();
	}

	private void buildQuery() {
		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		b.append(" ");
		b.append("SELECT  \n");
		b.append("  (str(count(distinct ?member)) as ?count)  \n");
		b.append("  (str(afn:namespace(?class)) as ?NameSpace)  \n");
		b.append("  (str(afn:localname(?class)) as ?Type)  \n");
		b.append("WHERE  \n");
		b.append("  {  \n");
		b.append("    ?member a ?class . \n");
		b.append("    filter not exists {?member eco:hasDataSource ?ds } \n");
		b.append("  }  \n");
		b.append("group by ?class  \n");
		b.append("order by ?NameSpace ?Type \n");
		setQuery(b.toString());
	}

	@Override
	public String getLabel() {
		return LABEL;
	}
}