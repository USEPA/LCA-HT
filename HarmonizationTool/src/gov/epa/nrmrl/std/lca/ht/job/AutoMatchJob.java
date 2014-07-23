package gov.epa.nrmrl.std.lca.ht.job;

import java.util.ArrayList;
import java.util.List;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVColumnInfo;
import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.FlowContext;
import gov.epa.nrmrl.std.lca.ht.dataModels.Flowable;
import gov.epa.nrmrl.std.lca.ht.dataModels.MatchCandidate;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.TableKeeper;
import harmonizationtool.model.TableProvider;
import harmonizationtool.vocabulary.ECO;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author tsb Tommy Cathey 919-541-1500
 * @author Tom Transue 919-541-0494
 * 
 *         Job for executing AutoMatching rows in a CSVTableView.
 * 
 */
public class AutoMatchJob extends Job {
	private String tableKey;
	private Integer[] results = new Integer[3];

	// 1: HIGH EVIDENCE HITS
	// 2: LOWER EVIDENCE HITS
	// 3: NO EVIDENCE ITEMS (UNMATCHED)
	public AutoMatchJob(String name) {
		super(name);
		this.tableKey = CSVTableView.getTableProviderKey();
		// this.harmonyQuery2Impl = harmonyQuery2Impl;
	}

	// public AutoMatchJob(String name, String tableKey) {
	// super(name);
	// this.tableKey = tableKey;
	// // this.harmonyQuery2Impl = harmonyQuery2Impl;
	// }

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		TableProvider tableProvider = TableKeeper.getTableProvider(tableKey);
		Resource tableDataSource = tableProvider.getDataSourceProvider().getTdbResource();
		System.out.println("Table :" + tableProvider);

		int rowCount = tableProvider.getData().size();
		System.out.println("Need to process this many rows: " + rowCount);
		CSVColumnInfo[] assignedCSVColumnInfo = tableProvider.getAssignedCSVColumnInfo();

		// CHECK WHICH Flowable and FlowContext COLUMNS ARE ASSIGNED
		List<Integer> flowableColumnNumbers = new ArrayList<Integer>();
		List<Integer> flowContextColumnNumbers = new ArrayList<Integer>();

		for (int colNumber = 0; colNumber < assignedCSVColumnInfo.length; colNumber++) {
			CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[colNumber];
			if (csvColumnInfo != null) {
				if (csvColumnInfo.getRDFClass().equals(Flowable.getRdfclass())) {
					if (csvColumnInfo.isRequired()) {
						// PREPEND THE IMPORTANT ONES
						flowableColumnNumbers.add(0, colNumber);
					} else {
						// APPEND THE OTHERS
						flowableColumnNumbers.add(colNumber);
					}
				} else if (csvColumnInfo.getRDFClass().equals(FlowContext.getRdfclass())) {
					if (csvColumnInfo.isRequired()) {
						// PREPEND THE IMPORTANT ONES
						flowContextColumnNumbers.add(0, colNumber);
					} else {
						// APPEND THE OTHERS
						flowContextColumnNumbers.add(colNumber);
					}
				}
			}
		}

		List<MatchCandidate> matchCandidates = new ArrayList<MatchCandidate>();
		// NOW ITERATE THROUGH EACH ROW, LOOKING FOR MATCHES
		for (int rowNumber = 0; rowNumber < rowCount; rowNumber++) {
			List<MatchCandidate> rowMatchCandidates = new ArrayList<MatchCandidate>();

			// System.out.println("About to check row: "+rowNumber);
			DataRow dataRow = (DataRow) tableProvider.getData().get(rowNumber);

			final int rowNumberPlusOne = rowNumber + 1;

			// FIRST DO Flowable
			Resource rdfClass = Flowable.getRdfclass();

			for (int colNumber : flowableColumnNumbers) {
				int dataColNumber = colNumber - 1;
				CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[colNumber];
				Property property = csvColumnInfo.getTdbProperty();
				String dataRowValue = dataRow.get(dataColNumber);
				Literal dataRowLiteral = ActiveTDB.createTypedLiteral(dataRowValue);
				ResIterator resIterator = ActiveTDB.tdbModel.listResourcesWithProperty(property, dataRowLiteral);
				Resource itemToMatchTDBResource = null;
				while (resIterator.hasNext()) {
					Resource candidateResource = resIterator.next();
					if (ActiveTDB.tdbModel.contains(candidateResource, RDF.type, rdfClass)) {
						if (ActiveTDB.tdbModel.contains(candidateResource, ECO.hasDataSource, tableDataSource)) {
							itemToMatchTDBResource = candidateResource;
						} else {

							boolean isNew = true;
							for (MatchCandidate matchCandidate : rowMatchCandidates) {
								if (matchCandidate.getItemToMatchTDBResource().equals(itemToMatchTDBResource)
										&& matchCandidate.getMatchCandidateTDBResource().equals(candidateResource)) {
									matchCandidate.incrementMatchFeatureCount();
									isNew = false;
								}
							}
							if (isNew) {
								MatchCandidate matchCandidate = new MatchCandidate(dataColNumber,
										itemToMatchTDBResource, candidateResource);
								if (matchCandidate.confirmRDFtypeMatch()) {
									matchCandidate.setMatchedFeatureCount(1);
									rowMatchCandidates.add(matchCandidate);
								}
							}
						}
					}
				}
			}
			for (MatchCandidate matchCandidate : rowMatchCandidates) {
				matchCandidates.add(matchCandidate);
				System.out.println("Num: " + matchCandidate.getMatchedFeatureCount() + ". type: "
						+ matchCandidate.getItemToMatchTDBResource().getProperty(RDF.type) + ".");
			}
			tableProvider.setLastChecked(rowNumber);
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					CSVTableView.updateCheckedData();
					FlowsWorkflow.setTextAutoMatched("Row: " + rowNumberPlusOne);
				}
			});

		}
		// System.out.println("matchCandidates.size()=" + matchCandidates.size());

		return Status.OK_STATUS;
	}

	public Integer[] getHitCounts() {
		return results;
	}

}
