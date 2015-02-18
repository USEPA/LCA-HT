package gov.epa.nrmrl.std.lca.ht.curation;

import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import java.util.Date;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
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
		b.append(Prefixes.getPrefixesForQuery());
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
		Literal dateLiteral = ActiveTDB.tsCreateTypedLiteral(calendar, null);
		ActiveTDB.tsAddGeneralTriple(annotationResource, DCTerms.created, dateLiteral, null);
		ActiveTDB.tsAddGeneralTriple(annotationResource, DCTerms.modified, dateLiteral, null);

		if (Util.getPreferenceStore().getString("userName") != null) {
			String userName = Util.getPreferenceStore().getString("userName");
			ActiveTDB.tsAddGeneralTriple(annotationResource, DCTerms.creator, userName, null);
		}
		currentAnnotation = annotationResource;
		return annotationResource;
	}

	public static void updateAnnotationModifiedDate() {
		if (currentAnnotation == null) {
			createNewAnnotation();
		} else {
			Date calendar = new Date();
			ActiveTDB.tsReplaceLiteral(currentAnnotation, DCTerms.modified, calendar);
		}
	}


	public static Resource findComparison(Resource querySource, Resource master) {
		Resource comparisonResource = null;
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.getModel(null);
		ResIterator resIterator = tdbModel.listResourcesWithProperty(FedLCA.comparedSource, querySource);
		while (resIterator.hasNext()) {
			comparisonResource = resIterator.next();
			if (tdbModel.contains(comparisonResource, FedLCA.comparedMaster, master)) {
				break;
			}
		}
		ActiveTDB.tdbDataset.end();
		return comparisonResource;
	}

	public static Resource setComparison(Resource querySource, Resource master, Resource equivalence) {
		if (querySource == null || master == null) {
			System.out.println("querySource = " + querySource + " and master = " + master);
			return null;
		}
		if (querySource.equals(master)) {
			return null;
		}

		Resource comparisonResource = findComparison(querySource, master);
		if (comparisonResource == null) {
			comparisonResource = createNewComparison(querySource, master, equivalence);
			return comparisonResource;
		}
		
		ActiveTDB.tsReplaceObject(comparisonResource, FedLCA.comparedEquivalence, equivalence);
		ActiveTDB.tsAddGeneralTriple(currentAnnotation, FedLCA.hasComparison, comparisonResource, null);
		updateAnnotationModifiedDate();
		return comparisonResource;
	}

	public static Resource createNewComparison(Resource querySource, Resource master, Resource equivalence) {
		Resource newComparison = null;
		if (querySource == null || master == null) {
			System.out.println("querySource = " + querySource + " and master = " + master);
			return null;
		}
		if (querySource.equals(master)) {
			return null;
		}
		
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(null);
		try {
			newComparison = tdbModel.createResource(FedLCA.Comparison);
			tdbModel.add(newComparison, FedLCA.comparedSource, querySource);
			tdbModel.add(newComparison, FedLCA.comparedMaster, master);
			tdbModel.add(newComparison, FedLCA.comparedEquivalence, equivalence);
			tdbModel.add(currentAnnotation, FedLCA.hasComparison, newComparison);
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("Creating new Comparison failed with Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---

		updateAnnotationModifiedDate();
		return newComparison;
	}

	public static void updateComparison(Resource comparison, Resource equivalence) {
		if (comparison == null || equivalence == null) {
			System.out.println("comparison = " + comparison + " and equivalence = " + equivalence);
			return;
		}
		updateAnnotationModifiedDate();
		ActiveTDB.tsReplaceObject(comparison, FedLCA.comparedEquivalence, equivalence);
	}

	public static Resource getCurrentAnnotation() {
		return currentAnnotation;
	}

	public static void setCurrentAnnotation(Resource newAnnotation) {
		currentAnnotation = newAnnotation;
	}

	public static void removeComparison(Resource comparison) {
		ActiveTDB.tsRemoveStatement(currentAnnotation, FedLCA.comparedEquivalence, comparison);
		ActiveTDB.tsRemoveGenericTriple(comparison, null, null, null);
		updateAnnotationModifiedDate();
	}

	public static void removeComparison(Resource comparedSource, Resource comparedMaster) {
		Model model = ActiveTDB.getModel(null);
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
			return comparison;
		}
		comparison = createNewComparison(tdbResource, matchResource, FedLCA.EquivalenceCandidate);
		return comparison;
	}

	public static Resource findCurrentComparison(Resource tdbResource, Resource matchResource) {
		Model tdbModel = ActiveTDB.getModel(null);
		Selector selector = new SimpleSelector(null, FedLCA.comparedSource, tdbResource);
		StmtIterator stmtIterator = tdbModel.listStatements(selector);
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.nextStatement();
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
