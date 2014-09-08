package gov.epa.nrmrl.std.lca.ht.job;

import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.hp.hpl.jena.query.ResultSet;

/**
 * @author tsb Tommy Cathey 919-541-1500
 * 
 *         Job for executing a QueryView query. It takes a HarmonyQuery2Impl and
 *         provides a method getResultSet to retrieve the ResultSet
 * 
 */
public class QueryViewJob extends Job {
	private HarmonyQuery2Impl harmonyQuery2Impl = null;
	private ResultSet resultSet = null;

	public QueryViewJob(String name, HarmonyQuery2Impl harmonyQuery2Impl) {
		super(name);
		this.harmonyQuery2Impl = harmonyQuery2Impl;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		resultSet = harmonyQuery2Impl.getResultSet();
		return Status.OK_STATUS;
	}

	public ResultSet getResultSet() {
		return resultSet;
	}

}
