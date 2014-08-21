package gov.epa.nrmrl.std.lca.ht.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVColumnInfo;
import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.Flow;
import gov.epa.nrmrl.std.lca.ht.dataModels.FlowContext;
import gov.epa.nrmrl.std.lca.ht.dataModels.Flowable;
import gov.epa.nrmrl.std.lca.ht.dataModels.MatchCandidate;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FEDLCA;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

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
		//
		// private int safeCommitColumns2TDB() {
		// TODO - IMPLEMENT THE "SAFE" PART WHICH MEANS
		// PRIOR TO ADDING TRIPLES, PREVIOUSLY ADDED
		// TRIPLES FROM THIS FILE SHOULD BE REMOVED -- OR...
		// BETTER YET, A THOUGHTFUL PROCESS AVOIDS DUPLICATE TRIPLES
		// Model model = ActiveTDB.tdbModel;

		List<Integer> rowsToIgnore = CSVTableView.getRowsToIgnore();

		TableProvider tableProvider = TableKeeper.getTableProvider(tableKey);
		DataSourceProvider dataSourceProvider = tableProvider.getDataSourceProvider();

		Map<String, Flowable> flowableMap = new HashMap<String, Flowable>();
		Map<String, FlowContext> flowContextMap = new HashMap<String, FlowContext>();
		List<MatchCandidate> matchCandidates = new ArrayList<MatchCandidate>();

		List<Flow> flows = new ArrayList<Flow>();

		// long triples = ActiveTDB.tdbModel.size();
		// Table table = CSVTableView.getTable();

		CSVColumnInfo[] assignedCSVColumns = tableProvider.getAssignedCSVColumnInfo();
		List<Integer> flowableCSVColumnNumbers = new ArrayList<Integer>();
		List<Integer> flowContextCSVColumnNumbers = new ArrayList<Integer>();

		// assignedCSVColumns[0] SHOULD BE NULL (NO DATA IN THAT COLUMN)
		for (int i = 1; i < assignedCSVColumns.length; i++) {
			CSVColumnInfo csvColumnInfo = assignedCSVColumns[i];
			if (csvColumnInfo == null) {
				continue;
			}
			if (csvColumnInfo.getRDFClass().equals(Flowable.getRdfclass())) {
				flowableCSVColumnNumbers.add(i);
			} else if (csvColumnInfo.getRDFClass().equals(FlowContext.getRdfclass())) {
				flowContextCSVColumnNumbers.add(i);
			}
		}

		int percentComplete = 0;
		for (int rowNumber = 0; rowNumber < tableProvider.getData().size(); rowNumber++) {
			if (rowsToIgnore.contains(rowNumber)) {
				continue;
			}
			if (100 * rowNumber / tableProvider.getData().size() > percentComplete) {
				final int state = percentComplete;
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						FlowsWorkflow.setTextCommit(state + "%");
					}
				});
				percentComplete += 10;
			}
			Flowable flowable = null;
			FlowContext flowContext = null;
			int rowNumberPlusOne = rowNumber + 1;
			// Literal rowLiteral =
			// ActiveTDB.createTypedLiteral(rowNumberPlusOne);

			// Item item = tableProvider.getItem(rowNumber);
			DataRow dataRow = tableProvider.getData().get(rowNumber);

			String flowableConcatinated = "";
			for (int i : flowableCSVColumnNumbers) {
				if (assignedCSVColumns[i].isRequired() && dataRow.get(i-1).equals("")) {
					flowableConcatinated = "";
					// REQUIRED FIELDS CAN NOT BE BLANK
					break;
				}
				flowableConcatinated += dataRow.get(i-1) + "\t";
			}
			if (!flowableConcatinated.equals("")) {
				if (flowableMap.containsKey(flowableConcatinated)) {
					flowable = flowableMap.get(flowableConcatinated);
				} else {
					flowable = new Flowable();
					flowableMap.put(flowableConcatinated, flowable);
					ActiveTDB.replaceResource(flowable.getTdbResource(), ECO.hasDataSource,
							dataSourceProvider.getTdbResource());
					for (int i : flowableCSVColumnNumbers) {
						CSVColumnInfo csvColumnInfo = assignedCSVColumns[i];
						if (csvColumnInfo.isUnique()) {
							ActiveTDB.replaceLiteral(flowable.getTdbResource(), csvColumnInfo.getTdbProperty(),
							// dataRow.getCSVTableIndex(i));
									dataRow.get(i-1));

						} else {
							ActiveTDB.addLiteral(flowable.getTdbResource(), csvColumnInfo.getTdbProperty(),
							// dataRow.getCSVTableIndex(i));
									dataRow.get(i-1));
						}
					}
					final int flowableCount = flowableMap.size();
					System.out.println("flowableCount ----> "+flowableCount + "after adding "+flowableConcatinated);

					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							FlowsWorkflow.setTextMatchFlowables("0 / " + flowableCount);
						}
					});

				}
				// FIND MATCHES FOR THIS FLOWABLE
				// FIND MATCHES INVOLVING NAMES AND SYNONYMS:
				// Q-NAME = DB-NAME

				List<MatchCandidate> matches = Flowable.findMatches(flowable);
				for (MatchCandidate matchCandidate : matches) {
					System.out.println("Line "
							+ rowNumberPlusOne
							+ " - "
							+ Flowable.compareFlowables(matchCandidate.getItemToMatchTDBResource(),
									matchCandidate.getMatchCandidateTDBResource()));
				}

				// RDFNode objectName = flowable.getTdbResource().getProperty(RDFS.label).getObject();
				// ResIterator resIterator = ActiveTDB.tdbModel.listSubjectsWithProperty(RDFS.label, objectName);
				// while (resIterator.hasNext()) {
				// Resource flowableMatchCandidate = resIterator.next();
				// if (ActiveTDB.tdbModel.contains(flowableMatchCandidate, ECO.hasDataSource,
				// dataSourceProvider.getTdbResource())) {
				// continue; // DON'T MATCH YOURSELF
				// }
				// if (!flowableMatchCandidate.hasProperty(RDF.type, ECO.Flowable)){
				// continue; // NOT A FLOWABLE
				// }
				// // THIS IS A name-name MATCH
				// System.out.println("name-name on line: "+rowNumberPlusOne);
				// MatchCandidate matchCandidate = new MatchCandidate(rowNumberPlusOne, flowable.getTdbResource(),
				// flowableMatchCandidate);
				// matchCandidates.add(matchCandidate);
				// }
				//
				// // Q-NAME = DB-SYN
				// resIterator = ActiveTDB.tdbModel.listSubjectsWithProperty(SKOS.altLabel, objectName);
				// while (resIterator.hasNext()) {
				// Resource flowableMatchCandidate = resIterator.next();
				// if (ActiveTDB.tdbModel.contains(flowableMatchCandidate, ECO.hasDataSource,
				// dataSourceProvider.getTdbResource())) {
				// continue; // DON'T MATCH YOURSELF
				// }
				// if (!flowableMatchCandidate.hasProperty(RDF.type, ECO.Flowable)){
				// continue; // NOT A FLOWABLE
				// }
				// System.out.println("name-synonym on line: "+rowNumberPlusOne);
				// // THIS IS A name-synonym MATCH
				// MatchCandidate matchCandidate = new MatchCandidate(rowNumberPlusOne, flowable.getTdbResource(),
				// flowableMatchCandidate);
				// matchCandidates.add(matchCandidate);
				// }
				//
				// //
				// StmtIterator stmtIterator = flowable.getTdbResource().listProperties(SKOS.altLabel);
				// while (stmtIterator.hasNext()) {
				// RDFNode objectAltName = stmtIterator.next().getObject();
				// resIterator = ActiveTDB.tdbModel.listSubjectsWithProperty(RDFS.label, objectAltName);
				// // Q-SYN = DB-NAME
				// while (resIterator.hasNext()) {
				// Resource flowableMatchCandidate = resIterator.next();
				// if (ActiveTDB.tdbModel.contains(flowableMatchCandidate, ECO.hasDataSource,
				// dataSourceProvider.getTdbResource())) {
				// continue; // DON'T MATCH YOURSELF
				// }
				// if (!flowableMatchCandidate.hasProperty(RDF.type, ECO.Flowable)){
				// continue; // NOT A FLOWABLE
				// }
				// System.out.println("synonym-name on line: "+rowNumberPlusOne);
				// // THIS IS A synonym-name MATCH
				// MatchCandidate matchCandidate = new MatchCandidate(rowNumberPlusOne, flowable.getTdbResource(),
				// flowableMatchCandidate);
				// matchCandidates.add(matchCandidate);
				// }
				//
				// // Q-SYN = DB-SYN
				// resIterator = ActiveTDB.tdbModel.listSubjectsWithProperty(SKOS.altLabel, objectName);
				// while (resIterator.hasNext()) {
				// Resource flowableMatchCandidate = resIterator.next();
				// if (ActiveTDB.tdbModel.contains(flowableMatchCandidate, ECO.hasDataSource,
				// dataSourceProvider.getTdbResource())) {
				// continue; // DON'T MATCH YOURSELF
				// }
				// if (!flowableMatchCandidate.hasProperty(RDF.type, ECO.Flowable)){
				// continue; // NOT A FLOWABLE
				// }
				// System.out.println("synonym-synonym on line: "+rowNumberPlusOne);
				// // THIS IS A synonym-synonym MATCH
				// MatchCandidate matchCandidate = new MatchCandidate(rowNumberPlusOne, flowable.getTdbResource(),
				// flowableMatchCandidate);
				// matchCandidates.add(matchCandidate);
				// }
				// }
				ActiveTDB.addLiteral(flowable.getTdbResource(), FEDLCA.sourceTableRowNumber, rowNumberPlusOne);
			}

			// NOW DO flowContext
			String flowContextConcatinated = "";
			for (int i : flowContextCSVColumnNumbers) {
				if (assignedCSVColumns[i].isRequired() && dataRow.get(i-1).equals("")) {
					flowContextConcatinated = "";
					// REQUIRED FIELDS CAN NOT BE BLANK
					break;
				}
				flowContextConcatinated += dataRow.get(i-1) + "\t";
			}
			if (!flowContextConcatinated.equals("")) {
				if (flowContextMap.containsKey(flowContextConcatinated)) {
					flowContext = flowContextMap.get(flowContextConcatinated);
				} else {
					flowContext = new FlowContext();
					flowContextMap.put(flowContextConcatinated, flowContext);
					ActiveTDB.replaceResource(flowContext.getTdbResource(), ECO.hasDataSource,
							dataSourceProvider.getTdbResource());
					for (int i : flowContextCSVColumnNumbers) {
						CSVColumnInfo csvColumnInfo = assignedCSVColumns[i];
						if (csvColumnInfo.isUnique()) {
							ActiveTDB.replaceLiteral(flowContext.getTdbResource(), csvColumnInfo.getTdbProperty(),
									dataRow.get(i-1));
						} else {
							ActiveTDB.addLiteral(flowContext.getTdbResource(), csvColumnInfo.getTdbProperty(),
									dataRow.get(i-1));
						}
					}
					final int flowContextCount = flowContextMap.size();
					System.out.println("flowContextCount----> "+flowContextCount + "after adding "+flowContextConcatinated);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							FlowsWorkflow.setTextFlowContexts("0 / " + flowContextCount);
						}
					});
				}
				ActiveTDB.addLiteral(flowContext.getTdbResource(), FEDLCA.sourceTableRowNumber, rowNumberPlusOne);

			}
			if (flowable != null && flowContext != null) {
				Flow tempFlow = new Flow();
				tempFlow.setFlowable(flowable);
				tempFlow.setFlowContext(flowContext);
				if (flows.contains(tempFlow)) {
					tempFlow.remove();
				} else {
					ActiveTDB.replaceResource(tempFlow.getTdbResource(), ECO.hasDataSource,
							dataSourceProvider.getTdbResource());
					ActiveTDB.addLiteral(tempFlow.getTdbResource(), FEDLCA.sourceTableRowNumber, rowNumberPlusOne);
					flows.add(tempFlow);
				}
			}
		}
		// long newTriples = ActiveTDB.tdbModel.size() - triples;
		// return (int) newTriples;

		//
		// TableProvider tableProvider = TableKeeper.getTableProvider(tableKey);
		// Resource tableDataSource =
		// tableProvider.getDataSourceProvider().getTdbResource();
		// System.out.println("Table :" + tableProvider);
		//
		// int rowCount = tableProvider.getData().size();
		// System.out.println("Need to process this many rows: " + rowCount);
		// CSVColumnInfo[] assignedCSVColumnInfo =
		// tableProvider.getAssignedCSVColumnInfo();
		//
		// // CHECK WHICH Flowable and FlowContext COLUMNS ARE ASSIGNED
		// List<Integer> flowableColumnNumbers = new ArrayList<Integer>();
		// List<Integer> flowContextColumnNumbers = new ArrayList<Integer>();
		//
		// for (int colNumber = 0; colNumber < assignedCSVColumnInfo.length;
		// colNumber++) {
		// CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[colNumber];
		// if (csvColumnInfo != null) {
		// if (csvColumnInfo.getRDFClass().equals(Flowable.getRdfclass())) {
		// if (csvColumnInfo.isRequired()) {
		// // PREPEND THE IMPORTANT ONES
		// flowableColumnNumbers.add(0, colNumber);
		// } else {
		// // APPEND THE OTHERS
		// flowableColumnNumbers.add(colNumber);
		// }
		// } else if
		// (csvColumnInfo.getRDFClass().equals(FlowContext.getRdfclass())) {
		// if (csvColumnInfo.isRequired()) {
		// // PREPEND THE IMPORTANT ONES
		// flowContextColumnNumbers.add(0, colNumber);
		// } else {
		// // APPEND THE OTHERS
		// flowContextColumnNumbers.add(colNumber);
		// }
		// }
		// }
		// }
		//
		// List<MatchCandidate> matchCandidates = new
		// ArrayList<MatchCandidate>();
		// // NOW ITERATE THROUGH EACH ROW, LOOKING FOR MATCHES
		// for (int rowNumber = 0; rowNumber < rowCount; rowNumber++) {
		// List<Integer> rowsToIgnore = CSVTableView.getRowsToIgnore();
		// if (rowsToIgnore.contains(rowNumber)){
		// continue;
		// }
		// List<MatchCandidate> rowMatchCandidates = new
		// ArrayList<MatchCandidate>();
		//
		// // System.out.println("About to check row: "+rowNumber);
		// DataRow dataRow = (DataRow) tableProvider.getData().get(rowNumber);
		//
		// final int rowNumberPlusOne = rowNumber + 1;
		//
		// // FIRST DO Flowable
		// // FIND NAME MATCH:
		// // Q-NAME = DB-NAME
		// // Q-SYN = DB-NAME
		// // Q-NAME = DB-SYN
		// // Q-SYN = DB-SYN
		// //------
		// // Q-OTHER = DB-OTHER
		// Resource rdfClass = Flowable.getRdfclass();
		//
		// for (int colNumber : flowableColumnNumbers) {
		// // int dataColNumber = colNumber - 1;
		// CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[colNumber];
		// Property property = csvColumnInfo.getTdbProperty();
		// String dataRowValue = dataRow.get(colNumber);
		// Literal dataRowLiteral = ActiveTDB.createTypedLiteral(dataRowValue);
		// ResIterator resIterator =
		// ActiveTDB.tdbModel.listResourcesWithProperty(property,
		// dataRowLiteral);
		// Resource itemToMatchTDBResource = null;
		// while (resIterator.hasNext()) {
		// Resource candidateResource = resIterator.next();
		// if (ActiveTDB.tdbModel.contains(candidateResource, RDF.type,
		// rdfClass)) {
		// if (ActiveTDB.tdbModel.contains(candidateResource, ECO.hasDataSource,
		// tableDataSource)) {
		// itemToMatchTDBResource = candidateResource;
		// } else {
		//
		// boolean isNew = true;
		// for (MatchCandidate matchCandidate : rowMatchCandidates) {
		// if
		// (matchCandidate.getMatchCandidateTDBResource().equals(candidateResource))
		// {
		// matchCandidate.incrementMatchFeatureCount();
		// isNew = false;
		// }
		// }
		// if (isNew) {
		// MatchCandidate matchCandidate = new MatchCandidate(rowNumber,
		// itemToMatchTDBResource, candidateResource);
		// if (matchCandidate.confirmRDFtypeMatch()) {
		// matchCandidate.setMatchedFeatureCount(1);
		// rowMatchCandidates.add(matchCandidate);
		// }
		// }
		// }
		// }
		// }
		// }
		// for (MatchCandidate matchCandidate : rowMatchCandidates) {
		// Resource qResource = matchCandidate.getItemToMatchTDBResource();
		// Resource rResource = matchCandidate.getMatchCandidateTDBResource();
		// int rowMatch = matchCandidate.getItemToMatchRow();
		// String result = Flowable.compareFlowables(qResource, rResource);
		// if (result.equals("+0")){
		// System.out.println("Hit on row: "+rowMatch);
		// }
		// //
		// System.out.println("On row: "+rowMatch+": "+Flowable.compareFlowables(qResource,
		// rResource));
		// matchCandidates.add(matchCandidate);
		// System.out.println("Num: " + matchCandidate.getMatchedFeatureCount()
		// + ". type: "
		// + matchCandidate.getItemToMatchTDBResource().getProperty(RDF.type) +
		// ".");
		// }
		// tableProvider.setLastChecked(rowNumber);
		// Display.getDefault().asyncExec(new Runnable() {
		// public void run() {
		// CSVTableView.updateCheckedData();
		// FlowsWorkflow.setTextAutoMatched("Row: " + rowNumberPlusOne);
		// }
		// });
		//
		// }
		// // System.out.println("matchCandidates.size()=" +
		// matchCandidates.size());

		return Status.OK_STATUS;
	}

	public Integer[] getHitCounts() {
		return results;
	}

}
