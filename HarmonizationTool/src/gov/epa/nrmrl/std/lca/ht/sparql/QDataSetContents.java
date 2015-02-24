package gov.epa.nrmrl.std.lca.ht.sparql;

import com.hp.hpl.jena.query.ResultSet;

public class QDataSetContents extends HarmonyQuery2Impl implements LabeledQuery {
	public static final String LABEL = "Show DataSet Contents";

	// private String param1;
	// private String[] referenceDataSets;
	// private String param2;

	public QDataSetContents() {
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
		b.append("SELECT \n");
		b.append("  (strbefore(afn:localname(?referenceClass),\"Dataset\") as ?Reference) \n");

		b.append("  (str(?label) as ?Dataset_name) \n");
		b.append("  (str(count(distinct ?member)) as ?count) \n");
		b.append("  (str(afn:namespace(?class)) as ?NameSpace) \n");
		b.append("  (str(afn:localname(?class)) as ?Type) \n");
		b.append("WHERE \n");
		b.append("  { ?s a eco:DataSource . \n");
		b.append("    ?s rdfs:label ?label  . \n");
		b.append("    ?member eco:hasDataSource ?s . \n");
		b.append("    optional { \n");
		b.append("      ?s a ?referenceClass . \n ");
		b.append("      filter (?referenceClass != eco:DataSource ) \n");
		b.append("      filter regex (str(?referenceClass),\"Dataset$\") \n ");
		b.append("    }");
		b.append("    ?member a ?class . \n");
		b.append("  } \n");
		b.append("#group by ?label ?class_name \n");
		b.append("group by ?referenceClass ?label ?class \n");
		b.append("order by ?referenceClass ?NameSpace ?Type\n");
		setQuery(b.toString());
	}

	@Override
	public String getLabel() {
		return LABEL;
	}
}