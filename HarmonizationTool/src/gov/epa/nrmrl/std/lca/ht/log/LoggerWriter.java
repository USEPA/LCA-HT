package gov.epa.nrmrl.std.lca.ht.log;

import java.io.StringWriter;
import org.eclipse.swt.widgets.Text;


/**
 * The function of this class is to update the logger window with information as events occur within the user interface and as data is stored, written to, or removed.
 * 
 * 
 * @author cfowler
 * @author ttransue
 */
public class LoggerWriter extends StringWriter {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.log.LoggerWriter";

	private Text loggerArea;
	
	private int lastWrite = 0;

	public LoggerWriter(Text area) {
		super();
		loggerArea = area;
	}

	@Override
	public void write(char[] cbuf, int off, int len) {
		super.write(cbuf, off, len);
		update();
	}

	@Override
	public void write(int c) {
		super.write(c);
		update();
	}

	@Override
	public void write(String str, int off, int len) {
		super.write(str, off, len);
		update();
	}

	@Override
	public void write(String str) {
		super.write(str);
		update();
	}

	private void update() {
		loggerArea.getDisplay().asyncExec(new Runnable() {
			public void run() {
				int newWrite = LoggerWriter.this.getBuffer().length();
				if (lastWrite > newWrite)
					return;
				loggerArea.append(LoggerWriter.this.getBuffer().substring(lastWrite, newWrite));
				loggerArea.setSelection(loggerArea.getText().length());
				lastWrite = newWrite;
			}
		});
	}
	
	public void clear() {
		getBuffer().setLength(0);
		lastWrite = 0;
	}
}
