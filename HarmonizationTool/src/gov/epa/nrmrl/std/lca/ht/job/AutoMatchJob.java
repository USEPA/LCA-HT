package gov.epa.nrmrl.std.lca.ht.job;

import java.util.ArrayList;
import java.util.List;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.MatchCandidate;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;
import harmonizationtool.model.DataSourceProvider;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FEDLCA;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Table;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * @author tsb Tommy Cathey 919-541-1500
 * @author Tom Transue 919-541-0494
 * 
 *         Job for executing AutoMatching rows in a CSVTableView.
 * 
 */
public class AutoMatchJob extends Job {
	private Integer[] results = new Integer[3];
	private Table table;

	// 1: HIGH EVIDENCE HITS
	// 2: LOWER EVIDENCE HITS
	// 3: NO EVIDENCE ITEMS (UNMATCHED)

	public AutoMatchJob(String name) {
		super(name);
		// this.harmonyQuery2Impl = harmonyQuery2Impl;
	}

	public AutoMatchJob(String name, Table table) {
		super(name);
		this.table = table;
		// this.harmonyQuery2Impl = harmonyQuery2Impl;
	}
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		// results = FlowsWorkflow.autoMatch_02();

//		Table table = CSVTableView.getTable();
		int count2 = table.getItemCount();
		System.out.println("count2: "+count2);
		List<MatchCandidate> matchCandidates = new ArrayList<MatchCandidate>();
		DataSourceProvider dataSourceProvider = CSVTableView.getDataSourceProvider();

		Model model = ActiveTDB.model;
		Resource dataSourceTDBResource = dataSourceProvider.getTdbResource();
		// ResIterator resourceIterator = model.listResourcesWithProperty(ECO.hasDataSource,
		// dataSourceTDBResource.asNode());
		ResIterator resourceIterator = model.listResourcesWithProperty(ECO.hasDataSource, dataSourceTDBResource);
		int count = 0;
		while (resourceIterator.hasNext()) {
			count++;
			System.out.println("count: " + count);
			Resource dataItem = resourceIterator.next();
			int rowNumber = -1;
			Statement rowNumberStatement = dataItem.getProperty(FEDLCA.sourceTableRowNumber);
			if (rowNumberStatement != null) {
				if (rowNumberStatement.getObject().isLiteral()) {
					rowNumber = rowNumberStatement.getObject().asLiteral().getInt();
					// System.out.println("rowNumber = " + rowNumber);
					StmtIterator dataItemProperties = dataItem.listProperties();
					while (dataItemProperties.hasNext()) {
						Statement dataItemStatement = dataItemProperties.next();
						Property dataItemProperty = dataItemStatement.getPredicate();
						RDFNode dataItemRDFNode = dataItemStatement.getObject();
						// System.out.println("dataItemStatement: "+dataItemStatement);

						// NOW FIND OTHER "dataItems" WITH THE SAME PROPERTY AND RDFNode

						if (!dataItemProperty.equals(FEDLCA.sourceTableRowNumber)) {
							ResIterator matchingResourcesIterator = model.listSubjectsWithProperty(dataItemProperty, dataItemRDFNode);
							while (matchingResourcesIterator.hasNext()) {
								System.out.println("Found a match for dataItemStatement: " + dataItemStatement);
								Resource matchingResource = matchingResourcesIterator.next();
								if (!matchingResource.equals(dataItem)) {
									MatchCandidate matchCandidate = new MatchCandidate(rowNumber, dataItem, matchingResource);
									if (matchCandidate.confirmRDFtypeMatch()) {
										matchCandidates.add(matchCandidate);
									}
								}
							}
						}
					}
				}
			}
		}
		System.out.println("matchCandidates.size()=" + matchCandidates.size());

		return Status.OK_STATUS;
	}

	public Integer[] getHitCounts() {
		return results;
	}

}
