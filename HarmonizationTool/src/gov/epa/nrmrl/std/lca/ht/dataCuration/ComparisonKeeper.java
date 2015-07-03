package gov.epa.nrmrl.std.lca.ht.dataCuration;

import java.util.ArrayList;
import java.util.List;

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

	public static int commitUncommittedComparisons() {
		for (ComparisonProvider comparisonProvider : uncommittedComparisons) {
			if (comparisonProvider.getTdbResource() != null) {
				comparisonProvider.commitToTDB();
			}
		}
		int done = uncommittedComparisons.size();
		uncommittedComparisons.clear();
		return done;
	}

	public static List<ComparisonProvider> getUncommittedcomparisons() {
		return uncommittedComparisons;
	}

}
