package gov.epa.nrmrl.std.lca.ht.flowable.mgr;

import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;

import com.hp.hpl.jena.rdf.model.Resource;

public enum MatchStatus {
	UNKNOWN(0, 255, 255, 255, "?", "No relation", "No assignment of sameness or difference has been assigned", FedLCA.equivalenceCandidate),
	EQUIVALENT(1, 215, 255, 215, "=", "Equivalent", "The two items are assigned as equivalent for all purposes", FedLCA.equivalent),
	SUBSET(2, 220, 160, 100, "<", "Subset", "The first item is a subset of the second item", FedLCA.equivalenceSubset),
	SUPERSET(3, 220, 140, 220, ">", "Superset", "The first item is a superset containing the second item", FedLCA.equivalenceSuperset),
	PROXY(4, 255, 255, 200,	"~", "Proxy", "The two items are similar, but a better match may be sought later", FedLCA.equivalenceProxy),
	NONEQUIVALENT(5, 255, 215, 200, "X", "Explicit difference",	"The two items are explicitly assigned as different despite some evidence of sameness", FedLCA.nonEquivalent);
//	ADDITION(6, 255, 215, 200, "+", "Suggested addition", "The item appears to have no match, and so is proposed as a new item", FedLCA.equivalent);
	private int value;
	private int r;
	private int g;
	private int b;
	private String symbol;
	private String name;
	private String comment;
	private Resource equivalence;

	private MatchStatus(int value, int r, int g, int b, String symbol, String name, String comment, Resource equivalence) {
		this.value = value;
		this.r = r;
		this.g = g;
		this.b = b;
		this.setSymbol(symbol);
		this.name = name;
		this.comment = comment;
		this.setEquivalence(equivalence);
	}

	public int getValue() {
		return value;
	}

	public Color getColor() {
		Device device = Display.getCurrent();
		return new Color(device, r, g, b);
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public static int getNumberBySymbol(String matchStatusSymbol) {
		if (matchStatusSymbol.equals(UNKNOWN.getSymbol())) {
			return UNKNOWN.getValue();
		}
		if (matchStatusSymbol.equals(EQUIVALENT.getSymbol())) {
			return EQUIVALENT.getValue();
		}
		if (matchStatusSymbol.equals(SUBSET.getSymbol())) {
			return SUBSET.getValue();
		}
		if (matchStatusSymbol.equals(SUPERSET.getSymbol())) {
			return SUPERSET.getValue();
		}
		if (matchStatusSymbol.equals(PROXY.getSymbol())) {
			return PROXY.getValue();
		}
		if (matchStatusSymbol.equals(NONEQUIVALENT.getSymbol())) {
			return NONEQUIVALENT.getValue();
		}
//		if (matchStatusSymbol.equals(ADDITION.getSymbol())) {
//			return ADDITION.getValue();
//		}
		return -1;
	}

	public static MatchStatus getBySymbol(String symbol) {
		if (symbol == UNKNOWN.getSymbol()) {
			return UNKNOWN;
		}
		if (symbol == EQUIVALENT.getSymbol()) {
			return EQUIVALENT;
		}
		if (symbol == SUBSET.getSymbol()) {
			return SUBSET;
		}
		if (symbol == SUPERSET.getSymbol()) {
			return SUPERSET;
		}
		if (symbol == PROXY.getSymbol()) {
			return PROXY;
		}
		if (symbol == NONEQUIVALENT.getSymbol()) {
			return NONEQUIVALENT;
		}
//		if (symbol == ADDITION.getSymbol()) {
//			return ADDITION;
//		}
		return null;
	}

	public static MatchStatus getByValue(int statusCol) {
		if (statusCol == UNKNOWN.getValue()) {
			return UNKNOWN;
		}
		if (statusCol == EQUIVALENT.getValue()) {
			return EQUIVALENT;
		}
		if (statusCol == SUBSET.getValue()) {
			return SUBSET;
		}
		if (statusCol == SUPERSET.getValue()) {
			return SUPERSET;
		}
		if (statusCol == PROXY.getValue()) {
			return PROXY;
		}
		if (statusCol == NONEQUIVALENT.getValue()) {
			return NONEQUIVALENT;
		}
//		if (statusCol == ADDITION.getValue()) {
//			return ADDITION;
//		}
		return null;
	}

	public Resource getEquivalence() {
		return equivalence;
	}

	public void setEquivalence(Resource equivalence) {
		this.equivalence = equivalence;
	}
};
