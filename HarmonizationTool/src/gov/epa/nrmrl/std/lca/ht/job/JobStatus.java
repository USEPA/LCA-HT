package gov.epa.nrmrl.std.lca.ht.job;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class JobStatus extends ViewPart {
	public static final String ID = "Job Status ID";
	private Text text;
	
	public JobStatus(){
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		text = new Text(parent, SWT.READ_ONLY | SWT.MULTI);
	}

	@Override
	public void setFocus() {
		text.setFocus();
	}
}


