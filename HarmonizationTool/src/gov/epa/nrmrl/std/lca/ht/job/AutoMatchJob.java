package gov.epa.nrmrl.std.lca.ht.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVColumnInfo;
import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.Flow;
import gov.epa.nrmrl.std.lca.ht.dataModels.FlowContext;
import gov.epa.nrmrl.std.lca.ht.dataModels.FlowProperty;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataPropertyProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.MatchCandidate;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.MatchContexts;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.MatchProperties;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.Flowable;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

import com.hp.hpl.jena.rdf.model.Resource;

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

//		CSVColumnInfo[] assignedCSVColumns = tableProvider.getAssignedCSVColumnInfo();
		CSVColumnInfo[] assignedCSVColumns = null; // <== FIXME COMMENT OUT THIS HACK, THEN FIX RED BELOW

		LCADataPropertyProvider[] lcaDataProperties = tableProvider.getLcaDataProperties();
		
		List<Integer> flowableCSVColumnNumbers = new ArrayList<Integer>();
		List<Integer> flowContextCSVColumnNumbers = new ArrayList<Integer>();
		List<Integer> flowPropertyCSVColumnNumbers = new ArrayList<Integer>();

		// assignedCSVColumns[0] SHOULD BE NULL (NO DATA IN THAT COLUMN)
		for (int i = 1; i < lcaDataProperties.length; i++) {
			CSVColumnInfo csvColumnInfo = assignedCSVColumns[i];
			LCADataPropertyProvider lcaDataPropertyProvider = lcaDataProperties[i];
			if (lcaDataPropertyProvider == null) {
				continue;
			}
			if (lcaDataPropertyProvider.getRDFClass().equals(Flowable.getRdfclass())) {
				flowableCSVColumnNumbers.add(i);
			} else if (lcaDataPropertyProvider.getRDFClass().equals(FlowContext.getRdfclass())) {
				flowContextCSVColumnNumbers.add(i);
			} else if (lcaDataPropertyProvider.getRDFClass().equals(FlowProperty.getRdfclass())) {
				flowPropertyCSVColumnNumbers.add(i);
			}
		}

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				FlowsWorkflow.setTextCommit("0%");
			}
		});
		int percentComplete = 0;
		// TableProvider flowContextTableProvider = new TableProvider();
		// TableProvider flowPropertyTableProvider = new TableProvider();

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
					ActiveTDB.tsReplaceResource(flowable.getTdbResource(), ECO.hasDataSource,
							dataSourceProvider.getTdbResource());
					for (int i : flowableCSVColumnNumbers) {
						String dataValue = dataRow.get(i - 1);
						if (dataValue.equals("")) {
							continue;
						}
						CSVColumnInfo csvColumnInfo = assignedCSVColumns[i];
						if (csvColumnInfo.isUnique()) {
							ActiveTDB.tsReplaceLiteral(flowable.getTdbResource(), csvColumnInfo.getTdbProperty(),
									dataValue);
//							flowable.setProperty(csvColumnInfo.getLCADataPropertyProvider.setPropertyName(dataValue));
//							flowable.setName(dataValue);
						} else {
							ActiveTDB.tsAddLiteral(flowable.getTdbResource(), csvColumnInfo.getTdbProperty(), dataValue);
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

				ActiveTDB.tsAddLiteral(flowable.getTdbResource(), FedLCA.sourceTableRowNumber, rowNumberPlusOne);
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
					// flowContextTableProvider.addDataRow(flowContextDataRow);
					ActiveTDB.tsReplaceResource(flowContext.getTdbResource(), ECO.hasDataSource,
							dataSourceProvider.getTdbResource());
					for (int i : flowContextCSVColumnNumbers) {
						CSVColumnInfo csvColumnInfo = assignedCSVColumns[i];
						if (csvColumnInfo.isUnique()) {
							ActiveTDB.tsReplaceLiteral(flowContext.getTdbResource(), csvColumnInfo.getTdbProperty(),
									dataRow.get(i - 1));
						} else {
							ActiveTDB.tsAddLiteral(flowContext.getTdbResource(), csvColumnInfo.getTdbProperty(),
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
				ActiveTDB.tsAddLiteral(flowContext.getTdbResource(), FedLCA.sourceTableRowNumber, rowNumberPlusOne);

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
					ActiveTDB.tsReplaceResource(flowProperty.getTdbResource(), ECO.hasDataSource,
							dataSourceProvider.getTdbResource());
					DataRow flowPropertyDataRow = new DataRow();
					flowPropertyDataRow.add(flowPropertyConcatinated);
					// flowPropertyTableProvider.addDataRow(flowPropertyDataRow);
					for (int i : flowPropertyCSVColumnNumbers) {
						CSVColumnInfo csvColumnInfo = assignedCSVColumns[i];
						if (csvColumnInfo.isUnique()) {
							ActiveTDB.tsReplaceLiteral(flowProperty.getTdbResource(), csvColumnInfo.getTdbProperty(),
									dataRow.get(i - 1));
						} else {
							ActiveTDB.tsAddLiteral(flowProperty.getTdbResource(), csvColumnInfo.getTdbProperty(),
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
				ActiveTDB.tsAddLiteral(flowProperty.getTdbResource(), FedLCA.sourceTableRowNumber, rowNumberPlusOne);

			}

			if (flowable != null && flowContext != null && flowProperty != null) {
				Flow tempFlow = new Flow();
				tempFlow.setFlowable(flowable);
				tempFlow.setFlowContext(flowContext);
				tempFlow.setFlowProperty(flowProperty);

				if (flows.contains(tempFlow)) {
					tempFlow.remove();
				} else {
					ActiveTDB.tsReplaceResource(tempFlow.getTdbResource(), ECO.hasDataSource,
							dataSourceProvider.getTdbResource());
					ActiveTDB.tsAddLiteral(tempFlow.getTdbResource(), FedLCA.sourceTableRowNumber, rowNumberPlusOne);
					flows.add(tempFlow);
				}
			}
		}
		final List<String> contexts = new ArrayList<String>();
		final List<Resource> contextResources = new ArrayList<Resource>();
		for (String flowContextConcat : flowContextMap.keySet()) {
			contexts.add(flowContextConcat);
		}
		Collections.sort(contexts);
		for (String flowContextConcat : contexts) {
			contextResources.add(flowContextMap.get(flowContextConcat).getTdbResource());
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MatchContexts matchContexts = (MatchContexts) Util.findView(MatchContexts.ID);
				matchContexts.setContextsToMatch(contexts);
				matchContexts.setContextResourcesToMatch(contextResources);
				matchContexts.update();
			}
		});
		
		final List<String> properties = new ArrayList<String>();
		final List<Resource> propertyResources = new ArrayList<Resource>();
		for (String flowPropertyConcat : flowPropertyMap.keySet()) {
			properties.add(flowPropertyConcat);
		}
		Collections.sort(properties);
		for (String flowPropertyConcat : properties) {
			propertyResources.add(flowPropertyMap.get(flowPropertyConcat).getTdbResource());
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MatchProperties matchProperties = (MatchProperties) Util.findView(MatchProperties.ID);
				matchProperties.setPropertiesToMatch(properties);
				matchProperties.setPropertyResourcesToMatch(propertyResources);

				matchProperties.update();
			}
		});

		return Status.OK_STATUS;
	}

	public Integer[] getHitCounts() {
		return results;
	}

}
