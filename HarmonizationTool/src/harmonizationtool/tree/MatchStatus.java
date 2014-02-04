package harmonizationtool.tree;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;

public enum MatchStatus {
	EQUIVALENT(1, 0, 255, 0), NONEQUIVALENT(2, 255, 0, 0), UNKNOWN(0, 0, 0, 255), BLANK(3,0,0,0);
	private int value;
	private int r;
	private int g;
	private int b;

	private MatchStatus(int value, int r, int g, int b) {
		this.value = value;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public int getValue() {
		return value;
	}

	public Color getColor() {
		Device device = Display.getCurrent();
		return new Color(device, r, g, b);
	}
};
