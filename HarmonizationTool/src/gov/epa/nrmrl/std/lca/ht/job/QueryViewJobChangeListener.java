package gov.epa.nrmrl.std.lca.ht.job;

import java.util.Date;

import gov.epa.nrmrl.std.lca.ht.sparql.QueryView;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;

/**
 * @author Tommy Cathey 919-541-1500
 * 
 *         Listens to a QueryView Job
 * 
 */
public class QueryViewJobChangeListener implements IJobChangeListener {
	private String key;
	private Job job = null;
	private Date jobStartDate;
	private Date jobEndDate;

	public QueryViewJobChangeListener(QueryView queryView, String key) {
		this.key = key;
	}

	@Override
	public void aboutToRun(IJobChangeEvent event) {
		Date startDate = new Date();
		Logger.getLogger("run").info("Job: " + key + " started: " + startDate);
		// String message = "Job: =>" + key + "<= started: " + startDate;
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
		Date endDate = new Date();

		// System.out.println("End date / time: " + endDate);
		// String message = "Job: =>" + key + "<= ended: " + endDate;
		// JobStatusView jobStatusView = (JobStatusView) Util
		// .findView(JobStatusView.ID);
		// if (jobStatusView != null) {
		// jobStatusView.add(message);
		// }

		Logger.getLogger("run").info("Job: " + key + " ended: " + endDate);

//		job = event.getJob();
//		JobStatus.textAdd("Job: "+job.getName()+" finished: "+jobEndDate+"\\n");

		if (job instanceof QueryViewJob) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					QueryView queryView = (QueryView) Util.findView(QueryView.ID);
					if (((QueryViewJob) job).getResultSet() == null) {
						return;
					}
					queryView.queryCallback(((QueryViewJob) job).getResultSet(), key);
				}

			});
		} else {
			System.out.println("job = "+job);
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
