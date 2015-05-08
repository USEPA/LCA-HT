package gov.epa.nrmrl.std.lca.ht.job;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.Flow;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataPropertyProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.FlowContext;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.FlowProperty;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.FlowUnit;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.Flowable;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.StopWatch;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
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

	/* 1: HIGH EVIDENCE HITS - AutoMatch FINDS THESE THROUGH METHODS: */
	/* -- Flowable.setMasterMatches() */
	/* -- FlowContext.setMatches() */
	/* -- FlowProperty.setMatches() */

	/* 2: DUPLICATE HITS - GET FLAGGED ORANGE DURING AUTO-MATCH */
	/* 3: PARTIAL EVIDENCE ITEMS (PROCESS SHOULD NOT BE IN AUTOMATCH) */

	public AutoMatchJob(String name) {
		super(name);
		this.tableKey = CSVTableView.getTableProviderKey();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		StopWatch stopWatch01 = new StopWatch("Total Time");
//		StopWatch stopWatch02 = new StopWatch("Concat. and hash Flowables");
//		StopWatch stopWatch03 = new StopWatch("Create Flowables");
//		StopWatch stopWatch04 = new StopWatch("Find matches + color rows");
//		StopWatch stopWatch05 = new StopWatch("Flow Context and Properties");
//		StopWatch stopWatch06 = new StopWatch("Flow");

		List<Integer> rowsToIgnore = CSVTableView.getRowsToIgnore();

		TableProvider tableProvider = TableKeeper.getTableProvider(tableKey);
		DataSourceProvider dataSourceProvider = tableProvider.getDataSourceProvider();

		Map<String, Flowable> flowableMap = new HashMap<String, Flowable>();
		Map<String, FlowContext> flowContextMap = new HashMap<String, FlowContext>();
		Map<String, FlowUnit> flowPropertyMap = new HashMap<String, FlowUnit>();

		LCADataPropertyProvider[] lcaDataProperties = tableProvider.getLcaDataProperties();

		Set<Integer> flowableCSVColumnNumbers = new HashSet<Integer>();
		Set<Integer> flowContextCSVColumnNumbers = new HashSet<Integer>();
		Set<Integer> flowPropertyCSVColumnNumbers = new HashSet<Integer>();

		// NOTE: assignedCSVColumns[0] SHOULD BE NULL (NO DATA IN THAT COLUMN)
		for (int i = 1; i < lcaDataProperties.length; i++) {
			LCADataPropertyProvider lcaDataPropertyProvider = lcaDataProperties[i];
			if (lcaDataPropertyProvider == null) {
				continue;
			}
			if (lcaDataPropertyProvider.getPropertyClass().equals(Flowable.label)) {
				flowableCSVColumnNumbers.add(i);
			} else if (lcaDataPropertyProvider.getPropertyClass().equals(FlowContext.label)) {
				flowContextCSVColumnNumbers.add(i);
			} else if (lcaDataPropertyProvider.getPropertyClass().equals(FlowUnit.label)) {
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
		stopWatch01.start();
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
			FlowUnit flowUnit = null;

			final int rowNumToSend = rowNumber;
			DataRow dataRow = tableProvider.getData().get(rowNumber);

			// ========================== FLOWABLE ==========================
//			stopWatch02.start();

			String flowableConcatinated = "";
			for (int i : flowableCSVColumnNumbers) {
				if (lcaDataProperties[i].isRequired() && dataRow.get(i - 1).equals("")) {
					flowableConcatinated = "";
					// REQUIRED FIELDS CAN NOT BE BLANK
					break;
				}
				flowableConcatinated += dataRow.get(i - 1) + "\t";
			}

			flowable = flowableMap.get(flowableConcatinated);
//			stopWatch02.stop();
			if (flowable == null) {
//				stopWatch03.start();
				flowable = new Flowable();
				flowableMap.put(flowableConcatinated, flowable);

				ActiveTDB.tsReplaceObject(flowable.getTdbResource(), ECO.hasDataSource,
						dataSourceProvider.getTdbResource());
				for (int i : flowableCSVColumnNumbers) {
					String dataValue = dataRow.get(i - 1);
					if (dataValue.equals("")) {
						continue;
					}
					LCADataPropertyProvider lcaDataPropertyProvider = lcaDataProperties[i];
					flowable.setProperty(lcaDataPropertyProvider.getPropertyName(), dataValue);
				}
//				stopWatch03.stop();
//				stopWatch04.start();
				final int hitCount = flowable.setMasterMatches(false);
				flowable.setFirstRow(rowNumToSend);
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						FlowsWorkflow.addFlowableRowNum(rowNumToSend);
						if (hitCount > 0) {
							FlowsWorkflow.addMatchFlowableRowNum(rowNumToSend);
						}
					}

				});
//				stopWatch04.stop();
			}
			dataRow.setFlowable(flowable);
			// }
//			stopWatch05.start();

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
					ActiveTDB.tsReplaceObject(flowContext.getTdbResource(), ECO.hasDataSource,
							dataSourceProvider.getTdbResource());
					for (int i : flowContextCSVColumnNumbers) {
						String dataValue = dataRow.get(i - 1);
						if (dataValue.equals("")) {
							continue;
						}
						LCADataPropertyProvider lcaDataPropertyProvider = lcaDataProperties[i];
						flowContext.setProperty(lcaDataPropertyProvider.getPropertyName(), dataValue);
					}
					final boolean hit = flowContext.setMatches(flowContextConcatinated);
					flowContext.setFirstRow(rowNumToSend);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							FlowsWorkflow.addContextRowNum(rowNumToSend);
							if (hit) {
								FlowsWorkflow.addMatchContextRowNum(rowNumToSend);
							}
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
				flowUnit = flowPropertyMap.get(flowPropertyConcatinated);
				if (flowUnit == null) {
					flowUnit = new FlowUnit();
					flowPropertyMap.put(flowPropertyConcatinated, flowUnit);
					ActiveTDB.tsReplaceObject(flowUnit.getTdbResource(), ECO.hasDataSource,
							dataSourceProvider.getTdbResource());
					for (int i : flowPropertyCSVColumnNumbers) {
						String dataValue = dataRow.get(i - 1);
						if (dataValue.equals("")) {
							continue;
						}
						LCADataPropertyProvider lcaDataPropertyProvider = lcaDataProperties[i];
						flowUnit.setProperty(lcaDataPropertyProvider.getPropertyName(), dataValue);
//						flowUnit.name =  dataValue;
					}

					final boolean hit = flowUnit.setMatches();
					flowUnit.setFirstRow(rowNumToSend);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							FlowsWorkflow.addPropertyRowNum(rowNumToSend);
							if (hit) {
								FlowsWorkflow.addMatchPropertyRowNum(rowNumToSend);
							}
						}
					});
				}
				dataRow.setFlowUnit(flowUnit);
				// NOW SEE IF THIS FLOW UNIT IS FOUND IN openLCA FLOWs
				final boolean hit = dataRow.setMatches();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						FlowsWorkflow.addFlowRowNum(rowNumToSend);
						if (hit) {
							FlowsWorkflow.addMatchFlowRowNum(rowNumToSend);
						}
					}
				});
			}
//			stopWatch05.stop();
		}
		// ========================== FLOW ==========================
//		stopWatch06.start();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				FlowsWorkflow.setTextCommit("Adding flow data...");
				Flow.addAllFlowData();
			}
		});
//		stopWatch06.stop();
		stopWatch01.stop();

		System.out.println(stopWatch01);
		// System.out.println(stopWatch02);
		// System.out.println(stopWatch03);
		// System.out.println(stopWatch04);
		// System.out.println(stopWatch05);
		// System.out.println(stopWatch06);

		// ========================== ROW BY ROW LOOP IS COMPLETE ==========================
		return Status.OK_STATUS;
	}

	public Integer[] getHitCounts() {
		return results;
	}

}
