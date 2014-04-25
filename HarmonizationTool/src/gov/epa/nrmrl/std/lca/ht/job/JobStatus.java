package gov.epa.nrmrl.std.lca.ht.job;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class JobStatus extends ViewPart {
	public static final String ID = "harmonizationtool.jobStatus";

	private static Text text;

	public JobStatus() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		text = new Text(parent, SWT.BORDER);
		text.append("Job status:\\n");
	}

	@Override
	public void setFocus() {
		// text.setFocus();
	}

	public static void add(String key) {
		// NEED TO IMPLEMENT ADDING A JOB
	}

	public static void textAdd(String string) {
		text.append(string);
	}
}
