package gov.epa.nrmrl.std.lca.ht.job;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVColumnInfo;
import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.Flow;
import gov.epa.nrmrl.std.lca.ht.dataModels.FlowContext;
import gov.epa.nrmrl.std.lca.ht.dataModels.FlowProperty;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataPropertyProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.Flowable;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

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

		List<Integer> rowsToIgnore = CSVTableView.getRowsToIgnore();

		TableProvider tableProvider = TableKeeper.getTableProvider(tableKey);
		DataSourceProvider dataSourceProvider = tableProvider.getDataSourceProvider();

		Map<String, Flowable> flowableMap = new LinkedHashMap<String, Flowable>();
		Map<String, FlowContext> flowContextMap = new LinkedHashMap<String, FlowContext>();
		Map<String, FlowProperty> flowPropertyMap = new LinkedHashMap<String, FlowProperty>();

		// List<MatchCandidate[]> matchRows = new ArrayList<MatchCandidate[]>();

		List<Flow> flows = new ArrayList<Flow>();

		// long triples = ActiveTDB.tdbModel.size();
		// Table table = CSVTableView.getTable();

		// CSVColumnInfo[] assignedCSVColumns =
		// tableProvider.getAssignedCSVColumnInfo();
		// CSVColumnInfo[] assignedCSVColumns = null; // <== FIXME COMMENT OUT
		// THIS HACK, THEN FIX RED BELOW

		LCADataPropertyProvider[] lcaDataProperties = tableProvider.getLcaDataProperties();

		Set<Integer> flowableCSVColumnNumbers = new HashSet<Integer>();
		Set<Integer> flowContextCSVColumnNumbers = new HashSet<Integer>();
		Set<Integer> flowPropertyCSVColumnNumbers = new HashSet<Integer>();

		// assignedCSVColumns[0] SHOULD BE NULL (NO DATA IN THAT COLUMN)
		for (int i = 1; i < lcaDataProperties.length; i++) {
			// CSVColumnInfo csvColumnInfo = assignedCSVColumns[i];
			LCADataPropertyProvider lcaDataPropertyProvider = lcaDataProperties[i];
			if (lcaDataPropertyProvider == null) {
				continue;
			}
			if (lcaDataPropertyProvider.getPropertyClass().equals(Flowable.label)) {
				flowableCSVColumnNumbers.add(i);
			} else if (lcaDataPropertyProvider.getPropertyClass().equals(FlowContext.label)) {
				flowContextCSVColumnNumbers.add(i);
			} else if (lcaDataPropertyProvider.getPropertyClass().equals(FlowProperty.label)) {
				flowPropertyCSVColumnNumbers.add(i);
			}
		}
		if (flowableCSVColumnNumbers.size() == 0) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					FlowsWorkflow.setTextMatchFlowables("N/A");
					FlowsWorkflow.disableFlowableBtn();
				}
			});
		}

		if (flowContextCSVColumnNumbers.size() == 0) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					FlowsWorkflow.setTextFlowContexts("N/A");
					FlowsWorkflow.disableContextBtn();

				}
			});
		}

		if (flowPropertyCSVColumnNumbers.size() == 0) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					FlowsWorkflow.setTextFlowProperties("N/A");
					FlowsWorkflow.disablePropertyBtn();
				}
			});
		}

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				FlowsWorkflow.setTextCommit("0%");
			}
		});
		int percentComplete = 0;
		// TableProvider flowContextTableProvider = new TableProvider();
		// TableProvider flowPropertyTableProvider = new TableProvider();

		// ========================== BEGIN ROW BY ROW ==========================
		for (int rowNumber = 0; rowNumber < tableProvider.getData().size(); rowNumber++) {
			if (rowsToIgnore.contains(rowNumber)) {
				continue;
			}

			if (100 * rowNumber / tableProvider.getData().size() >= percentComplete) {
				final String state = percentComplete + "%";

				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						FlowsWorkflow.setTextCommit(state);
					}
				});
				percentComplete += 1;
			}
			Flowable flowable = null;
			FlowContext flowContext = null;
			FlowProperty flowProperty = null;

			final int rowNumberPlusOne = rowNumber + 1;
			final int rowNumToSend = rowNumber;

			// Literal rowLiteral =
			// ActiveTDB.createTypedLiteral(rowNumberPlusOne);

			// Item item = tableProvider.getItem(rowNumber);
			DataRow dataRow = tableProvider.getData().get(rowNumber);

			// ========================== FLOWABLE ==========================
			String flowableConcatinated = "";
			for (int i : flowableCSVColumnNumbers) {
				if (lcaDataProperties[i].isRequired() && dataRow.get(i - 1).equals("")) {
					flowableConcatinated = "";
					// REQUIRED FIELDS CAN NOT BE BLANK
					break;
				}
				flowableConcatinated += dataRow.get(i - 1) + "\t";
			}
			if (!flowableConcatinated.matches("^\\s*$")) {
				flowable = flowableMap.get(flowableConcatinated);
				if (flowable == null) {
					flowable = new Flowable();
					flowableMap.put(flowableConcatinated, flowable);
					ActiveTDB.tsReplaceResource(flowable.getTdbResource(), ECO.hasDataSource,
							dataSourceProvider.getTdbResource());
					for (int i : flowableCSVColumnNumbers) {
						String dataValue = dataRow.get(i - 1);
						if (dataValue.equals("")) {
							continue;
						}
						LCADataPropertyProvider lcaDataPropertyProvider = lcaDataProperties[i];
						flowable.setProperty(lcaDataPropertyProvider.getPropertyName(), dataValue);
					}
					final boolean hit = flowable.setMatches();
					flowable.setFirstRow(rowNumToSend);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							FlowsWorkflow.addFlowableRowNum(rowNumToSend);
							if (hit){
								FlowsWorkflow.addMatchFlowableRowNum(rowNumToSend);
							}
						}
			
					});
				}
				dataRow.setFlowable(flowable);
			}

			// ========================== FLOW CONTEXT ==========================
			String flowContextConcatinated = "";
			for (int i : flowContextCSVColumnNumbers) {
				if (lcaDataProperties[i].isRequired() && dataRow.get(i - 1).equals("")) {
					flowContextConcatinated = "";
					// REQUIRED FIELDS CAN NOT BE BLANK
					break;
				}
				flowContextConcatinated += dataRow.get(i - 1) + "\t";
			}
			if (!flowContextConcatinated.matches("^\\s*$")) {
				flowContext = flowContextMap.get(flowContextConcatinated);
				if (flowContext == null) {
					flowContext = new FlowContext();
					flowContextMap.put(flowContextConcatinated, flowContext);
					// DataRow flowContextDataRow = new DataRow();
					// flowContextDataRow.add(flowContextConcatinated);
					ActiveTDB.tsReplaceResource(flowContext.getTdbResource(), ECO.hasDataSource,
							dataSourceProvider.getTdbResource());
					for (int i : flowContextCSVColumnNumbers) {
						LCADataPropertyProvider lcaDataPropertyProvider = lcaDataProperties[i];
						if (lcaDataPropertyProvider.isUnique()) {
							ActiveTDB.tsReplaceLiteral(flowContext.getTdbResource(),
									lcaDataPropertyProvider.getTDBProperty(), dataRow.get(i - 1));
						} else {
							ActiveTDB.tsAddLiteral(flowContext.getTdbResource(),
									lcaDataPropertyProvider.getTDBProperty(), dataRow.get(i - 1));
						}
					}
					flowContext.setFirstRow(rowNumToSend);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							FlowsWorkflow.addContextRowNum(rowNumToSend);
						}
					});
				}
				dataRow.setFlowContext(flowContext);
			}

			// ========================== FLOW PROPERTY ==========================
			String flowPropertyConcatinated = "";
			for (int i : flowPropertyCSVColumnNumbers) {
				if (lcaDataProperties[i].isRequired() && dataRow.get(i - 1).equals("")) {
					flowPropertyConcatinated = "";
					// REQUIRED FIELDS CAN NOT BE BLANK
					break;
				}
				flowPropertyConcatinated += dataRow.get(i - 1) + "\t";
			}
			if (!flowPropertyConcatinated.matches("^\\s*$")) {
				flowProperty = flowPropertyMap.get(flowPropertyConcatinated);
				if (flowProperty == null) {
					flowProperty = new FlowProperty();
					flowPropertyMap.put(flowPropertyConcatinated, flowProperty);
					ActiveTDB.tsReplaceResource(flowProperty.getTdbResource(), ECO.hasDataSource,
							dataSourceProvider.getTdbResource());
					for (int i : flowPropertyCSVColumnNumbers) {
						LCADataPropertyProvider lcaDataPropertyProvider = lcaDataProperties[i];
						if (lcaDataPropertyProvider.isUnique()) {
							ActiveTDB.tsReplaceLiteral(flowProperty.getTdbResource(),
									lcaDataPropertyProvider.getTDBProperty(), dataRow.get(i - 1));
						} else {
							ActiveTDB.tsAddLiteral(flowProperty.getTdbResource(),
									lcaDataPropertyProvider.getTDBProperty(), dataRow.get(i - 1));
						}
					}
					flowProperty.setFirstRow(rowNumToSend);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							FlowsWorkflow.addPropertyRowNum(rowNumToSend);
						}
					});
				}
				dataRow.setFlowProperty(flowProperty);
			}
			// ========================== FLOW ==========================
			Flow tempFlow = new Flow();
			tempFlow.setFlowable(flowable);
			tempFlow.setFlowContext(flowContext);
			tempFlow.setFlowProperty(flowProperty);

			ActiveTDB.tsReplaceResource(tempFlow.getTdbResource(), ECO.hasDataSource,
					dataSourceProvider.getTdbResource());
			ActiveTDB.tsAddLiteral(tempFlow.getTdbResource(), FedLCA.sourceTableRowNumber, rowNumberPlusOne);
			flows.add(tempFlow);

		}
		// ========================== ROW BY ROW LOOP IS COMPLETE ==========================
		return Status.OK_STATUS;
	}

	public Integer[] getHitCounts() {
		return results;
	}

}
