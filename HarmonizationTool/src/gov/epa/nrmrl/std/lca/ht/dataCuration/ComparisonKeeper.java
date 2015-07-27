package gov.epa.nrmrl.std.lca.ht.dataCuration;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Temporal;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDFS;

/** This class manages ComparisonProvider objects that are not held in the TDB or are extracted from the TDB into memory
 * 
 * @author Tom Transue
 *
 */
public class ComparisonKeeper {
	private static final List<ComparisonProvider> uncommittedComparisons = new ArrayList<ComparisonProvider>();

	public static void addUncommittedComparison(ComparisonProvider comparisonProvider) {
		uncommittedComparisons.add(comparisonProvider);
	}

	public static void removeUncommittedComparison(ComparisonProvider comparisonProvider) {
		uncommittedComparisons.remove(comparisonProvider);
	}

	public static int commitUncommittedComparisons(String commentToAdd) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(null);
		try {
			for (ComparisonProvider comparisonProvider : uncommittedComparisons) {
				if (comparisonProvider.getTdbResource() == null) {
					Resource tdbResource = tdbModel.createResource(ComparisonProvider.getRDFClass());
					tdbModel.add(tdbResource, FedLCA.comparedSource, comparisonProvider.getUserDataObject());
					tdbModel.add(tdbResource, FedLCA.comparedMaster, comparisonProvider.getMasterDataObject());
					tdbModel.add(tdbResource, FedLCA.comparedEquivalence, comparisonProvider.getEquivalence());
					Literal literal = tdbModel.createLiteral(comparisonProvider.getComment()+commentToAdd);
					tdbModel.add(tdbResource, RDFS.comment, literal);
					literal = Temporal.getLiteralFromDate1(comparisonProvider.getLastUpdate());
					tdbModel.add(tdbResource, DCTerms.modified, literal);
//					AnnotationProvider annotationProvider = comparisonProvider.getAnnotationProvider();
//					if (annotationProvider != null){
//						Resource annotationResource = annotationProvider.getTdbResource();
//						if (annotationResource != null){
//							tdbModel.add(tdbResource, FedLCA.memberOfCollection, annotationResource);
//						}
//					}
					comparisonProvider.setTdbResource(tdbResource, false);
				}
			}
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("Creating new ComparisonProvider failed with Exception: " + e);
			e.printStackTrace();
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ----
//		AnnotationProvider.updateCurrentAnnotationModifiedDate();
		int done = uncommittedComparisons.size();
		uncommittedComparisons.clear();
		return done;
	}

	public static List<ComparisonProvider> getUncommittedcomparisons() {
		return uncommittedComparisons;
	}
	
	public static List<ComparisonProvider> getComparisons(Resource userObject) {
		Resource comparisonResource = null;
		List<ComparisonProvider> comparisons = new ArrayList<ComparisonProvider>();
		Model tdbModel = ActiveTDB.getModel(null);
		ResIterator resIterator = tdbModel.listResourcesWithProperty(FedLCA.comparedSource, userObject);
		while (resIterator.hasNext()) {
			comparisonResource = resIterator.next();
			ComparisonProvider comparisonProvider = new ComparisonProvider(comparisonResource);
			comparisons.add(comparisonProvider);

		}
		return comparisons;

	}

	public static ComparisonProvider findComparison(Resource userObject, Resource masterObject) {
		for (ComparisonProvider comparisonProvider : uncommittedComparisons) {
			if (comparisonProvider.getUserDataObject().equals(userObject)
					&& comparisonProvider.getMasterDataObject().equals(masterObject)) {
				return comparisonProvider;
			}
		}
		Resource comparisonResource = null;
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.getModel(null);
		ResIterator resIterator = tdbModel.listResourcesWithProperty(FedLCA.comparedSource, userObject);
		while (resIterator.hasNext()) {
			comparisonResource = resIterator.next();
			if (tdbModel.contains(comparisonResource, FedLCA.comparedMaster, masterObject)) {
				break;
			}
		}
		ActiveTDB.tdbDataset.end();
		if (comparisonResource == null) {
			ComparisonProvider comparisonProvider = new ComparisonProvider(userObject, masterObject,
					FedLCA.EquivalenceCandidate);
			uncommittedComparisons.add(comparisonProvider);
			return comparisonProvider;
		}
		return new ComparisonProvider(comparisonResource);
	}
}
