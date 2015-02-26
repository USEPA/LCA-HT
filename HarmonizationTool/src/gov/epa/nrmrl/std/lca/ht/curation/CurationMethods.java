package gov.epa.nrmrl.std.lca.ht.curation;

import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;

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

	public static Resource compareSourceToMasterThing(Resource sourceResource, Resource masterResource) {
		/*
		 * Get attributes of each and compare to each other 1a) Everything the same -> keep source 1b) No differences
		 * (all in source same as master), but source has additional -> return source 2a) No differences (all in source
		 * same as master), but master has additional -> return master 3a) No differences, but each has additional ->
		 * copy source info to special fields, update comment, make new uuid, return hybrid 3b) Differences -> copy
		 * source info to special fields, update comment, make new uuid, return hybrid
		 */
		boolean keepSource = true;
		boolean useMaster = true;
		boolean updateMaster = false;

		List<Statement> sourceStatements = new ArrayList<Statement>();
		List<Statement> masterStatements = new ArrayList<Statement>();
		List<Resource> sourceClasses = new ArrayList<Resource>();
		List<Resource> masterClasses = new ArrayList<Resource>();
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.getModel(null);
		StmtIterator masterStatementIterator = masterResource.listProperties();
		while (masterStatementIterator.hasNext()) {
			masterStatements.add(masterStatementIterator.next());
		}
		StmtIterator sourceStatementIterator = sourceResource.listProperties();
		while (sourceStatementIterator.hasNext()) {
			sourceStatements.add(sourceStatementIterator.next());
		}

		Resource commonClass = null;
		masterStatementIterator = masterResource.listProperties(RDF.type);
		while (masterStatementIterator.hasNext()) {
			masterClasses.add(masterStatementIterator.next().getObject().asResource());
		}
		sourceStatementIterator = sourceResource.listProperties(RDF.type);
		while (sourceStatementIterator.hasNext()) {
			Resource sourceClass = sourceStatementIterator.next().getObject().asResource();
			if (masterClasses.contains(sourceClass)){
				if (commonClass == null){
					commonClass = sourceClass;
				} else {
					// WHAT TO DO IF THERE ARE MULTIPLE SAME CLASSES?
				}
			}
			sourceClasses.add(sourceStatementIterator.next().getObject().asResource());
		}
		
		ActiveTDB.tdbDataset.end();

		HashMap<Property, RDFNode> masterAttributes = new HashMap<Property, RDFNode>();
		HashMap<Property, RDFNode> sourceAttributes = new HashMap<Property, RDFNode>();
		for (Statement statement : sourceStatements) {
			sourceAttributes.put(statement.getPredicate(), statement.getObject());
		}
		for (Statement statement : masterStatements) {
			Property masterProperty = statement.getPredicate();
			RDFNode masterRDFNode = statement.getObject();
			masterAttributes.put(masterProperty, masterRDFNode);
			if (sourceAttributes.containsKey(masterProperty)) {
				RDFNode sourceObject = sourceAttributes.get(masterProperty);
				if (!sourceObject.equals(masterRDFNode)) {
					keepSource = false;
					useMaster = false;
					updateMaster = true;
					break;
				} else {
					sourceAttributes.remove(masterProperty);
				}
			} else {
				keepSource = false;
			}
		}
		if (!sourceAttributes.isEmpty()) {
			useMaster = false;
		}
		if (keepSource && !useMaster && !updateMaster) {
			return sourceResource;
		} else if (!keepSource && useMaster && !updateMaster) {
			return masterResource;
		} else if (!keepSource && !useMaster && updateMaster) {
			Resource newResource = ActiveTDB.tsCreateResource(commonClass);
			// WORK HERE: TODO == FIXME
			return masterResource;
		} else {
			return null;
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
		List<Resource> comparisonsToRemove = new ArrayList<Resource>();
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.getModel(null);
		// Model model = ActiveTDB.getModel(null);
		Selector selector = new SimpleSelector(null, FedLCA.comparedSource, comparedSource);
		StmtIterator stmtIterator = tdbModel.listStatements(selector);
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.nextStatement();
			int count = 0;
			while (tdbModel.contains(statement.getSubject(), FedLCA.comparedMaster, comparedMaster)) {
				comparisonsToRemove.add(statement.getSubject());
				count++;
			}
			System.out.println("count " + count);
			break;
		}
		ActiveTDB.tdbDataset.end();
		for (Resource toRemove : comparisonsToRemove) {
			removeComparison(toRemove);
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
