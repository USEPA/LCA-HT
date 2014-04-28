package gov.epa.nrmrl.std.lca.ht.job;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class JobStatusView extends ViewPart {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.job.JobStatusView";
	private static Text text;

	public JobStatusView() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		text = new Text(composite, SWT.READ_ONLY | SWT.MULTI);
		text.setSize(300, 500);
		text.setText("Job log\n");
	}

	@Override
	public void setFocus() {
		text.setFocus();
	}

	public void add(String string) {
		text.append(string);
	}
}
