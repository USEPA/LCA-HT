package gov.epa.nrmrl.std.lca.ht.curration;

import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;

import java.util.Date;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
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
		ActiveTDB.tsAddLiteral(annotationResource, DCTerms.created, dateLiteral);
		ActiveTDB.tsAddLiteral(annotationResource, DCTerms.modified, dateLiteral);

		if (Util.getPreferenceStore().getString("userName") != null) {
			String userName = Util.getPreferenceStore().getString("userName");
			// int count = 0;
			// while (!ActiveTDB.tdbModel.contains(annotationResource, DCTerms.creator, userName) || true) {
			ActiveTDB.tsAddLiteral(annotationResource, DCTerms.creator, userName);
		}
		currentAnnotation = annotationResource;
		return annotationResource;
	}

	public static void updateAnnotationModifiedDate() {
		if (currentAnnotation == null) {
			createNewAnnotation();
		} else {
			Date calendar = new Date();
			// Literal dateLiteral = ActiveTDB.tsCreateTypedLiteral(calendar);
			ActiveTDB.tsReplaceLiteral(currentAnnotation, DCTerms.modified, calendar);
		}
	}

	public static void nonTSUpdateAnnotationModifiedDate() {
		if (currentAnnotation == null) {
			createNewAnnotation();
		} else {
			Date calendar = new Date();
			// Literal dateLiteral = ActiveTDB.tsCreateTypedLiteral(calendar);
			ActiveTDB.replaceLiteral(currentAnnotation, DCTerms.modified, calendar);
		}
	}

	public static Resource createNewComparison(Resource querySource, Resource master, Resource equivalence) {
		if (querySource == null || master == null) {
			System.out.println("querySource = " + querySource + " and master = " + master);
			return null;
		}
		if (querySource.equals(master)) {
			return null;
		}
		Resource comparison = ActiveTDB.tsCreateResource(FedLCA.Comparison);

		ActiveTDB.tsAddTriple(comparison, FedLCA.comparedSource, querySource);
		ActiveTDB.tsAddTriple(comparison, FedLCA.comparedMaster, master);
		ActiveTDB.tsAddTriple(comparison, FedLCA.comparedEquivalence, equivalence);
		ActiveTDB.tsAddTriple(currentAnnotation, FedLCA.hasComparison, comparison);
		updateAnnotationModifiedDate();
		return comparison;
	}

	public static Resource nonTSCreateNewComparison(Resource querySource, Resource master, Resource equivalence) {
		if (querySource == null || master == null) {
			System.out.println("querySource = " + querySource + " and master = " + master);
			return null;
		}
		if (querySource.equals(master)) {
			return null;
		}
		Resource comparison = ActiveTDB.createResource(FedLCA.Comparison);

		ActiveTDB.addTriple(comparison, FedLCA.comparedSource, querySource);
		ActiveTDB.addTriple(comparison, FedLCA.comparedMaster, master);
		ActiveTDB.addTriple(comparison, FedLCA.comparedEquivalence, equivalence);
		ActiveTDB.addTriple(currentAnnotation, FedLCA.hasComparison, comparison);
		nonTSUpdateAnnotationModifiedDate();
		return comparison;
	}

	public static void updateComparison(Resource comparison, Resource equivalence) {
		if (comparison == null || equivalence == null) {
			System.out.println("comparison = " + comparison + " and equivalence = " + equivalence);
			return;
		}
		updateAnnotationModifiedDate();
		ActiveTDB.tsReplaceResource(comparison, FedLCA.comparedEquivalence, equivalence);
	}

	public static Resource getCurrentAnnotation() {
		return currentAnnotation;
	}

	public static void setCurrentAnnotation(Resource newAnnotation) {
		currentAnnotation = newAnnotation;
	}

	public static void removeComparison(Resource comparison) {
		ActiveTDB.tsRemoveStatement(currentAnnotation, FedLCA.comparedEquivalence, comparison);
		ActiveTDB.tsRemoveAllObjects(comparison);
		updateAnnotationModifiedDate();
	}

	public static void removeComparison(Resource comparedSource, Resource comparedMaster) {
		Model model = ActiveTDB.getFreshModel();
		Selector selector = new SimpleSelector(null, FedLCA.comparedSource, comparedSource);
		StmtIterator stmtIterator = model.listStatements(selector);
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.nextStatement();
			int count = 0;
			while (model.contains(statement.getSubject(), FedLCA.comparedMaster, comparedMaster)) {
				removeComparison(statement.getSubject());
				count++;
			}
			System.out.println("count " + count);
			break;
		}
	}

	public static Resource getComparison(Resource tdbResource, Resource matchResource) {
		Resource comparison = findCurrentComparison(tdbResource, matchResource);
		if (comparison != null) {
			// System.out.println("updating existing comparison");
			return comparison;
		}
		comparison = createNewComparison(tdbResource, matchResource, FedLCA.equivalenceCandidate);
		// System.out.println("creating new comparison");
		return comparison;
	}

	public static Resource findCurrentComparison(Resource tdbResource, Resource matchResource) {
		Model tdbModel = ActiveTDB.getModel();
		Selector selector = new SimpleSelector(null, FedLCA.comparedSource, tdbResource);
		StmtIterator stmtIterator = tdbModel.listStatements(selector);
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.nextStatement();
			// System.out.println("statement " + statement);
			Resource comparisonCandidate = statement.getSubject();
			if (tdbModel.contains(comparisonCandidate, FedLCA.comparedMaster, matchResource)
					&& tdbModel.contains(currentAnnotation, FedLCA.hasComparison, comparisonCandidate)) {
				removeComparison(comparisonCandidate);
				return comparisonCandidate;
			}
		}
		return null;
	}
}
