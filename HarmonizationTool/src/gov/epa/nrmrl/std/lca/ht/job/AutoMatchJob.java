package gov.epa.nrmrl.std.lca.ht.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.epa.nrmrl.std.lca.ht.compartment.mgr.HarmonizeContexts;
import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVColumnInfo;
import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.Flow;
import gov.epa.nrmrl.std.lca.ht.dataModels.FlowContext;
import gov.epa.nrmrl.std.lca.ht.dataModels.FlowProperty;
import gov.epa.nrmrl.std.lca.ht.dataModels.Flowable;
import gov.epa.nrmrl.std.lca.ht.dataModels.MatchCandidate;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FEDLCA;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

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
		Map<String, FlowProperty> flowPropertyMap = new HashMap<String, FlowProperty>();

		// List<MatchCandidate[]> matchRows = new ArrayList<MatchCandidate[]>();

		List<Flow> flows = new ArrayList<Flow>();

		// long triples = ActiveTDB.tdbModel.size();
		// Table table = CSVTableView.getTable();

		CSVColumnInfo[] assignedCSVColumns = tableProvider.getAssignedCSVColumnInfo();
		List<Integer> flowableCSVColumnNumbers = new ArrayList<Integer>();
		List<Integer> flowContextCSVColumnNumbers = new ArrayList<Integer>();
		List<Integer> flowPropertyCSVColumnNumbers = new ArrayList<Integer>();

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
			} else if (csvColumnInfo.getRDFClass().equals(FlowProperty.getRdfclass())) {
				flowPropertyCSVColumnNumbers.add(i);
			}
		}

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				FlowsWorkflow.setTextCommit("0%");
			}
		});
		int percentComplete = 0;
		TableProvider flowContextTableProvider = new TableProvider();
		TableProvider flowPropertyTableProvider = new TableProvider();

		for (int rowNumber = 0; rowNumber < tableProvider.getData().size(); rowNumber++) {
			if (rowsToIgnore.contains(rowNumber)) {
				continue;
			}
			// List<MatchCandidate> matchCandidatesThisRow = new
			// ArrayList<MatchCandidate>();
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
			FlowProperty flowProperty = null;

			final int rowNumberPlusOne = rowNumber + 1;
			// Literal rowLiteral =
			// ActiveTDB.createTypedLiteral(rowNumberPlusOne);

			// Item item = tableProvider.getItem(rowNumber);
			DataRow dataRow = tableProvider.getData().get(rowNumber);

			String flowableConcatinated = "";
			for (int i : flowableCSVColumnNumbers) {
				if (assignedCSVColumns[i].isRequired() && dataRow.get(i - 1).equals("")) {
					flowableConcatinated = "";
					// REQUIRED FIELDS CAN NOT BE BLANK
					break;
				}
				flowableConcatinated += dataRow.get(i - 1) + "\t";
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
						String dataValue = dataRow.get(i - 1);
						if (dataValue.equals("")) {
							continue;
						}
						CSVColumnInfo csvColumnInfo = assignedCSVColumns[i];
						if (csvColumnInfo.isUnique()) {
							ActiveTDB.replaceLiteral(flowable.getTdbResource(), csvColumnInfo.getTdbProperty(),
									dataValue);
						} else {
							ActiveTDB.addLiteral(flowable.getTdbResource(), csvColumnInfo.getTdbProperty(), dataValue);
						}
					}
					final int flowableCount = flowableMap.size();
					System.out
							.println("flowableCount ----> " + flowableCount + " after adding " + flowableConcatinated);

					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							FlowsWorkflow.setTextMatchFlowables("0 / " + flowableCount);
						}
					});
				}
				// FIND MATCHES FOR THIS FLOWABLE
				// FIND MATCHES INVOLVING NAMES AND SYNONYMS:
				// Q-NAME = DB-NAME

				Set<MatchCandidate> matches = Flowable.findMatches(flowable);
				final int numHits = matches.size();
				// MatchCandidate[] matchCandidatesThisRow = new MatchCandidate[numHits];
				// int counter = 0;
				for (MatchCandidate mc : matches) {
					dataRow.addMatchCandidate(mc);
					// matchCandidatesThisRow[counter] = mc;
					// counter++;
				}
				// matchRows.add(matchCandidatesThisRow);
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (numHits == 0) {
							CSVTableView.colorCell(rowNumberPlusOne - 1, 0,
									SWTResourceManager.getColor(SWT.COLOR_YELLOW));
						} else if (numHits == 1) {
							CSVTableView.colorCell(rowNumberPlusOne - 1, 0,
									SWTResourceManager.getColor(SWT.COLOR_GREEN));
						} else {
							CSVTableView.colorCell(rowNumberPlusOne - 1, 0, SWTResourceManager.getColor(SWT.COLOR_CYAN));
						}
					}
				});

				ActiveTDB.addLiteral(flowable.getTdbResource(), FEDLCA.sourceTableRowNumber, rowNumberPlusOne);
			}

			// NOW DO flowContext
			String flowContextConcatinated = "";
			for (int i : flowContextCSVColumnNumbers) {
				if (assignedCSVColumns[i].isRequired() && dataRow.get(i - 1).equals("")) {
					flowContextConcatinated = "";
					// REQUIRED FIELDS CAN NOT BE BLANK
					break;
				}
				flowContextConcatinated += dataRow.get(i - 1) + "\t";
			}
			if (!flowContextConcatinated.equals("")) {
				if (flowContextMap.containsKey(flowContextConcatinated)) {
					flowContext = flowContextMap.get(flowContextConcatinated);
				} else {
					flowContext = new FlowContext();
					flowContextMap.put(flowContextConcatinated, flowContext);
					DataRow flowContextDataRow = new DataRow();
					flowContextDataRow.add(flowContextConcatinated);
					flowContextTableProvider.addDataRow(flowContextDataRow);
					ActiveTDB.replaceResource(flowContext.getTdbResource(), ECO.hasDataSource,
							dataSourceProvider.getTdbResource());
					for (int i : flowContextCSVColumnNumbers) {
						CSVColumnInfo csvColumnInfo = assignedCSVColumns[i];
						if (csvColumnInfo.isUnique()) {
							ActiveTDB.replaceLiteral(flowContext.getTdbResource(), csvColumnInfo.getTdbProperty(),
									dataRow.get(i - 1));
						} else {
							ActiveTDB.addLiteral(flowContext.getTdbResource(), csvColumnInfo.getTdbProperty(),
									dataRow.get(i - 1));
						}
					}
					final int flowContextCount = flowContextMap.size();
					System.out.println("flowContextCount----> " + flowContextCount + "after adding "
							+ flowContextConcatinated);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							FlowsWorkflow.setTextFlowContexts("0 / " + flowContextCount);
						}
					});
				}
				ActiveTDB.addLiteral(flowContext.getTdbResource(), FEDLCA.sourceTableRowNumber, rowNumberPlusOne);

			}

			// NOW DO flowProperty
			String flowPropertyConcatinated = "";
			for (int i : flowPropertyCSVColumnNumbers) {
				if (assignedCSVColumns[i].isRequired() && dataRow.get(i - 1).equals("")) {
					flowPropertyConcatinated = "";
					// REQUIRED FIELDS CAN NOT BE BLANK
					break;
				}
				flowPropertyConcatinated += dataRow.get(i - 1) + "\t";
			}
			if (!flowPropertyConcatinated.equals("")) {
				if (flowPropertyMap.containsKey(flowPropertyConcatinated)) {
					flowProperty = flowPropertyMap.get(flowPropertyConcatinated);
				} else {
					flowProperty = new FlowProperty();
					flowPropertyMap.put(flowPropertyConcatinated, flowProperty);
					ActiveTDB.replaceResource(flowProperty.getTdbResource(), ECO.hasDataSource,
							dataSourceProvider.getTdbResource());
					DataRow flowPropertyDataRow = new DataRow();
					flowPropertyDataRow.add(flowPropertyConcatinated);
					flowPropertyTableProvider.addDataRow(flowPropertyDataRow);
					for (int i : flowPropertyCSVColumnNumbers) {
						CSVColumnInfo csvColumnInfo = assignedCSVColumns[i];
						if (csvColumnInfo.isUnique()) {
							ActiveTDB.replaceLiteral(flowProperty.getTdbResource(), csvColumnInfo.getTdbProperty(),
									dataRow.get(i - 1));
						} else {
							ActiveTDB.addLiteral(flowProperty.getTdbResource(), csvColumnInfo.getTdbProperty(),
									dataRow.get(i - 1));
						}
					}
					final int flowPropertyCount = flowPropertyMap.size();
					System.out.println("flowPropertyCount----> " + flowPropertyCount + "after adding "
							+ flowPropertyConcatinated);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							FlowsWorkflow.setTextFlowProperties("0 / " + flowPropertyCount);
						}
					});
				}
				ActiveTDB.addLiteral(flowProperty.getTdbResource(), FEDLCA.sourceTableRowNumber, rowNumberPlusOne);

			}

			if (flowable != null && flowContext != null && flowProperty != null) {
				Flow tempFlow = new Flow();
				tempFlow.setFlowable(flowable);
				tempFlow.setFlowContext(flowContext);
				tempFlow.setFlowProperty(flowProperty);

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
//		HarmonizeContexts.update(flowContextTableProvider);
//		HarmonizeFlowProperties.update(flowPropertyTableProvider);
		// TODO - WORK OUT THE ABOVE METHODS IN THEIR CLASSES
		
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
