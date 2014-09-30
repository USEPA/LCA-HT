package gov.epa.nrmrl.std.lca.ht.flowable.mgr;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;

public enum MatchStatus {
	UNKNOWN(0,
			255,
			255,
			255,
			"?",
			"No relation",
			"No assignment of sameness or difference has been assigned"
			),
	EQUIVALENT(1,
			215,
			255,
			215,
			"=",
			"Equivalent",
			"The two items are assigned as equivalent for all purposes"
			),
	SUBSET(2,
			220,
			160,
			100,
			"<",
			"Subset",
			"The first item is a subset of the second item"
			),
	SUPERSET(3,
			220,
			140,
			220,
			">",
			"Superset",
			"The first item is a superset containing the second item"
			),
	PROXY(4,
			255,
			255,
			200,
			"~",
			"Proxy",
			"The two items are similar, but a better match may be sought later"
			),
	NONEQUIVALENT(5,
			255,
			215,
			200,
			"X",
			"Explicit difference",
			"The two items are explicitly assigned as different despite some evidence of sameness"),
	ADDITION(6,
			255,
			215,
			200,
			"+",
			"Suggested addition",
			"The item appears to have no match, and so is proposed as a new item");
	private int value;
	private int r;
	private int g;
	private int b;
	private String symbol;
	private String name;
	private String comment;
	

	private MatchStatus(int value, int r, int g, int b, String symbol, String name, String comment) {
		this.value = value;
		this.r = r;
		this.g = g;
		this.b = b;
		this.setSymbol(symbol);
		this.name= name;
		this.comment = comment;
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
};
