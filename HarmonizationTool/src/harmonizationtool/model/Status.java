package harmonizationtool.model;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;

public enum Status {
	NOTABLE(0, 0, 0, 255),
	WARNING(1, 255, 255, 0),
	UNRESOLVED(2, 255, 0, 0),
	RESOLVED(3, 0, 255, 0),
	FATAL(4, 255, 150, 150);

	private int value;
	private int r;
	private int g;
	private int b;

	private Status(int value, int r, int g, int b) {
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
}
