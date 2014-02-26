package gov.epa.nrmrl.std.lca.ht.job;

import harmonizationtool.QueryView;
import harmonizationtool.utils.Util;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

/**
 * @author Tommy Cathey 919-541-1500
 * 
 *         Listens to a QueryView Job
 * 
 */
public class QueryViewJobChangeListener implements IJobChangeListener {
	private String key;
	private Job job = null;

	public QueryViewJobChangeListener(QueryView queryView, String key) {
		this.key = key;
	}

	@Override
	public void aboutToRun(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void awake(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void done(IJobChangeEvent event) {
		// when the job is done check that is of the correct instance
		// if so then sync up with the default display. find the queryView and
		// call its queryCallback method
		job = event.getJob();
		if (job instanceof QueryViewJob) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					QueryView queryView = (QueryView) Util.findView(QueryView.ID);
					queryView.queryCallback(((QueryViewJob) job).getResultSet(), key);
				}

			});
		}

	}

	@Override
	public void running(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scheduled(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sleeping(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

}
