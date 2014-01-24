package harmonizationtool.query;

import harmonizationtool.dialog.DialogQueryDataset;
import harmonizationtool.model.TableProvider;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import com.hp.hpl.jena.query.ResultSet;

public class HSubsSameCas extends HarmonyQuery2Impl implements LabeledQuery {
	public static final String LABEL = "Harmonize CAS Matches";

	private String param1;
	private String[] referenceDataSets;

	public HSubsSameCas() {
		super();
	}

	public ResultSet getResultSet() {
		// CALL THE DIALOG TO GET THE PARAMETERS
		getDialog();
		// BUILD THE QUERY USING THE PARAMETERS
		buildQuery();
		// READY TO CALL getResultSet() ON THESUPER CLASS
		return super.getResultSet();
	}

	public ResultSet getResultSet(String param1, String[] referenceDataSets) {
		// BRING IN THE PARAMETERS
		this.param1 = param1;
		this.referenceDataSets = referenceDataSets;
		// BUILD THE QUERY USING THE PARAMETERS
		buildQuery();
		// READY TO CALL getResultSet() ON THESUPER CLASS
		return super.getResultSet();
	}

	private void getDialog() {
		DialogQueryDataset dialog = new DialogQueryDataset(Display.getCurrent()
				.getActiveShell());
		dialog.create();
		if (dialog.open() == Window.OK) {
			System.out.println("OK");
			param1 = dialog.getPrimaryDataSet();
			referenceDataSets = dialog.getReferenceDataSets();
		}
	}

	private void buildQuery() {
		for (int i = 0; i < referenceDataSets.length; i++) {
			if (referenceDataSets[i] == param1) {
				// REMOVE IT
			}
		}

		StringBuilder b = new StringBuilder();
		b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		b.append("PREFIX  ethold: <http://epa.gov/nrmrl/std/lca/ethold#> \n");
		b.append("PREFIX  afn:    <http://jena.hpl.hp.com/ARQ/function#> \n");
		b.append("PREFIX  fn:     <http://www.w3.org/2005/xpath-functions#> \n");
		b.append("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
		b.append("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
		b.append("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		b.append("PREFIX  xml:    <http://www.w3.org/XML/1998/namespace> \n");
		b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
		b.append("PREFIX  dcterms: <http://purl.org/dc/terms/> \n");
		b.append(" \n");
		b.append("SELECT  \n");
		b.append("   (str(?ds1_name) as ?" + TableProvider.SUBROW_PREFIX + "1_"
				+ TableProvider.SUBROW_NAMEHEADER + ") \n");
		b.append("   (str(?name1) as ?" + TableProvider.SUBROW_PREFIX + "1_substance_name) \n");

		for (int i = 0; i < referenceDataSets.length; i++) {
			int iPlusTwo = i + 2;
			b.append("   (str(?ds" + iPlusTwo + "_name) as ?" + TableProvider.SUBROW_PREFIX
					+ iPlusTwo + "_" + TableProvider.SUBROW_NAMEHEADER + ") \n");
			b.append("   (str(?name" + iPlusTwo + ") as ?" + TableProvider.SUBROW_PREFIX
					+ iPlusTwo + "_substance_name) \n");
		}
		b.append("   (str(?cas) as ?same_cas) \n");
		b.append(" \n");
		b.append("WHERE { \n");
		b.append("      ?sub1 eco:hasDataSource ?ds1 . \n");
		b.append("      ?ds1 rdfs:label ?ds1_name . \n");
		b.append("      filter regex(str(?ds1_name),\"" + param1 + "\") \n");
		b.append("      ?sub1 eco:casNumber ?cas .  \n");
		b.append("      ?sub1 rdfs:label ?name1 .  \n");

		for (int i = 0; i < referenceDataSets.length; i++) {
			int iPlusTwo = i + 2;
			String refDataSet = referenceDataSets[i];
			b.append("OPTIONAL {");
			b.append("      ?sub" + iPlusTwo + " eco:hasDataSource ?ds"
					+ iPlusTwo + " . \n");
			b.append("      ?ds" + iPlusTwo + " rdfs:label ?ds" + iPlusTwo
					+ "_name . \n");
			b.append("      filter regex(str(?ds" + iPlusTwo + "_name),\""
					+ refDataSet + "\") \n");
			b.append("      ?sub" + iPlusTwo + " eco:casNumber ?cas . \n");
			b.append("      ?sub" + iPlusTwo + " rdfs:label ?name" + iPlusTwo
					+ " .  \n");

			b.append("} \n");
		}

		b.append("      filter ( \n");
		b.append("     bound(?sub2) \n");
		for (int i = 1; i < referenceDataSets.length; i++) {
			int iPlusTwo = i + 2;
			b.append("  || bound(?sub" + iPlusTwo + ") \n");
		}
		b.append("       ) \n");
		b.append("} \n");
		b.append("order by ?cas \n");
		setQuery(b.toString());
	}

	@Override
	public String getLabel() {
		return LABEL;
	}
}