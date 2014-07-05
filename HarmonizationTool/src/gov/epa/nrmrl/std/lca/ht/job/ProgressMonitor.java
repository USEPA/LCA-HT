package gov.epa.nrmrl.std.lca.ht.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;

public class ProgressMonitor implements IProgressMonitor {
	private ProgressBar progressBar;

	// @Inject UISynchronize sync;

	// @PostConstruct
	// public void createControls(Composite parent) {
	// progressBar = new ProgressBar(parent, SWT.SMOOTH);
	// progressBar.setBounds(100, 10, 200, 20);
	// }

	@Override
	public void worked(final int work) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				System.out.println("Worked");
				progressBar.setSelection(progressBar.getSelection() + work);
			}
		});
	}

	@Override
	public void subTask(String name) {

	}

	@Override
	public void setTaskName(String name) {

	}

	@Override
	public void setCanceled(boolean value) {

	}

	@Override
	public boolean isCanceled() {
		return false;
	}

	@Override
	public void internalWorked(double work) {
	}

	@Override
	public void done() {
		System.out.println("Done");

	}

	@Override
	public void beginTask(final String name, final int totalWork) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				progressBar.setMaximum(totalWork);
				progressBar.setToolTipText(name);
			}
		});
		System.out.println("Starting");
	}
}
