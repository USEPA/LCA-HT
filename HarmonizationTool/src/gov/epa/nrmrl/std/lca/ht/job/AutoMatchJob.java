package gov.epa.nrmrl.std.lca.ht.job;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author tsb Tommy Cathey 919-541-1500
 * @author Tom Transue 919-541-0494
 * 
 *         Job for executing AutoMatching rows in a CSVTableView. 
 * 
 */
public class AutoMatchJob extends Job {
//	private FlowsWorkflow flowsWorkflow;
	private CSVTableView csvTableView;
	private Integer[] results = new Integer[3];
	// 1: HIGH EVIDENCE HITS
	// 2: LOWER EVIDENCE HITS
	// 3: NO EVIDENCE ITEMS (UNMATCHED)

	public AutoMatchJob(String name, CSVTableView csvTableView) {
		super(name);
		this.csvTableView = csvTableView;
//		this.harmonyQuery2Impl = harmonyQuery2Impl;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		results = FlowsWorkflow.autoMatch_02(csvTableView);
		return Status.OK_STATUS;
	}

	public Integer[] getHitCounts() {
		return results;
	}

}
