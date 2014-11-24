package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;

import java.util.Date;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;

public class CurationMethods {
	private static Resource currentAnnotation;

	public static Resource getLatestAnnotation() {
		StringBuilder b = new StringBuilder();
		b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		b.append("PREFIX  fedlca: <http://epa.gov/nrmrl/std/lca/fedlca/1.0#> \n");
		b.append("PREFIX  lcaht:  <http://epa.gov/nrmrl/std/lca/ht/1.0#> \n");
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
		b.append("SELECT distinct ?a  \n");
		b.append("WHERE \n");
		b.append("  { \n");
		b.append("    ?a rdf:type fedlca:Annotation . \n");
		b.append("    ?a dcterms:dateSubmitted ?date . \n");
		b.append("   } \n");
		b.append("order by ?date DESC \n");
		b.append("limit 1 \n");
		String query = b.toString();
		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);
		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		if (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			RDFNode rdfNode = querySolution.get("a");
			return rdfNode.asResource();
		}
		return null;
	}

	public static Resource createNewAnnotation() {
		Resource annotationResource = ActiveTDB.tsCreateResource(FedLCA.Annotation);
		Date calendar = new Date();
		Literal dateLiteral = ActiveTDB.tsCreateTypedLiteral(calendar);
		ActiveTDB.tsAddLiteral(annotationResource, DCTerms.dateSubmitted, dateLiteral);
		if (Util.getPreferenceStore().getString("userName") != null) {
			String userName = Util.getPreferenceStore().getString("userName");
			ActiveTDB.tsAddLiteral(annotationResource, DCTerms.creator, userName);
		}
		currentAnnotation = annotationResource;
		return annotationResource;
	}

	public static Resource addComparison(Resource querySource, Resource master, Resource equivalence) {
		if (querySource == null || master == null) {
			System.out.println("querySource = " + querySource + " and master = " + master);
			return null;
		}
		Resource comparison = ActiveTDB.tsCreateResource(FedLCA.Comparison);

		ActiveTDB.tsAddLiteral(comparison, FedLCA.comparedSource, querySource);
		ActiveTDB.tsAddLiteral(comparison, FedLCA.comparedMaster, master);
		ActiveTDB.tsAddLiteral(comparison, FedLCA.comparedEquivalence, equivalence);
		return comparison;

	}

	public static Resource getCurrentAnnotation() {
		return currentAnnotation;
	}

	public static void setCurrentAnnotation(Resource currentAnnotation) {
		CurationMethods.currentAnnotation = currentAnnotation;
	}
}
