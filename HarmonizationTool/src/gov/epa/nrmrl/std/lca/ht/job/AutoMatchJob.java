package gov.epa.nrmrl.std.lca.ht.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

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

		// ===========================
		// System.out.println("Flowable.getRdfclass() = " + Flowable.getRdfclass());
		//
		// StmtIterator stmtIterator = ActiveTDB.tdbModel.listStatements();
		// while (stmtIterator.hasNext()) {
		// Statement statement = stmtIterator.next();
		// if (!statement.getSubject().isAnon()) {
		// if (statement.getSubject().getLocalName().equals(Flowable.getRdfclass().getLocalName())) {
		// if (statement.getSubject().equals(Flowable.getRdfclass())) {
		// System.out.println("! Equals operator works !");
		// }
		//
		// System.out.println("Statement: " + statement.getSubject() + " -- " + statement.getPredicate()
		// + " -- " + statement.getObject());
		//
		// StmtIterator stmtIterator2 = Flowable.getRdfclass().listProperties();
		// while (stmtIterator2.hasNext()) {
		// Statement statement2 = stmtIterator2.next();
		// System.out.println("statement2.getPredicate() = " + statement2.getPredicate());
		// }
		// }
		// }
		// }
		// if (Flowable.getRdfclass().hasProperty(RDFS.label)) { // <-- THIS IS SUPPOSED TO CHECK THE ASSIGNMENT
		// System.out.println("got it!" + Flowable.getRdfclass().getProperty(RDFS.label).getString());
		// } else {
		// stmtIterator = Flowable.getRdfclass().listProperties();
		// while (stmtIterator.hasNext()) {
		// Statement statement = stmtIterator.next();
		// System.out.println("statement.getPredicate() = " + statement.getPredicate());
		// }
		// System.out.println("wtf");
		// }
		// ===========================

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

		// CSVColumnInfo[] assignedCSVColumns = tableProvider.getAssignedCSVColumnInfo();
		// CSVColumnInfo[] assignedCSVColumns = null; // <== FIXME COMMENT OUT THIS HACK, THEN FIX RED BELOW

		LCADataPropertyProvider[] lcaDataProperties = tableProvider.getLcaDataProperties();

		List<Integer> flowableCSVColumnNumbers = new ArrayList<Integer>();
		List<Integer> flowContextCSVColumnNumbers = new ArrayList<Integer>();
		List<Integer> flowPropertyCSVColumnNumbers = new ArrayList<Integer>();

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
					// uniqueFlowableRowNumbers.add(rowNumber);

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

					Set<Resource> matches = Flowable.findMatchingFlowableResources(flowable);
					for (Resource flowableResource : matches) {
						dataRow.addMatchCandidateFlowable(flowableResource);
					}

					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							FlowsWorkflow.addFlowableRowNum(rowNumToSend);
						}
					});
				}
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
				// if ((flowContext = flowContextMap.get(flowContextConcatinated)) == null) {
				if (flowContext == null) {
					// uniqueFlowContextRowNumbers.add(rowNumber);

					flowContext = new FlowContext();
					flowContextMap.put(flowContextConcatinated, flowContext);
					DataRow flowContextDataRow = new DataRow();
					flowContextDataRow.add(flowContextConcatinated);
					// flowContextTableProvider.addDataRow(flowContextDataRow);
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

					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							FlowsWorkflow.addContextRowNum(rowNumToSend);
						}
					});
				}
				// ActiveTDB.tsAddLiteral(flowContext.getTdbResource(), FedLCA.sourceTableRowNumber, rowNumberPlusOne);

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

				// if (!flowPropertyConcatinated.equals("")) {
				flowProperty = flowPropertyMap.get(flowPropertyConcatinated);
				if (flowProperty == null) {
					// uniqueFlowPropertyRowNumbers.add(rowNumber);

					flowProperty = new FlowProperty();
					flowPropertyMap.put(flowPropertyConcatinated, flowProperty);
					ActiveTDB.tsReplaceResource(flowProperty.getTdbResource(), ECO.hasDataSource,
							dataSourceProvider.getTdbResource());
					DataRow flowPropertyDataRow = new DataRow();
					flowPropertyDataRow.add(flowPropertyConcatinated);
					// flowPropertyTableProvider.addDataRow(flowPropertyDataRow);
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

					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							FlowsWorkflow.addPropertyRowNum(rowNumToSend);
						}
					});
				}
				// ActiveTDB.tsAddLiteral(flowProperty.getTdbResource(), FedLCA.sourceTableRowNumber, rowNumberPlusOne);

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

//		final List<String> contexts = new ArrayList<String>();
//		final List<Resource> contextResources = new ArrayList<Resource>();
//		for (String flowContextConcat : flowContextMap.keySet()) {
//			contexts.add(flowContextConcat);
//		}
//		Collections.sort(contexts);
//		for (String flowContextConcat : contexts) {
//			contextResources.add(flowContextMap.get(flowContextConcat).getTdbResource());
//		}
//		Display.getDefault().asyncExec(new Runnable() {
//			public void run() {
//				MatchContexts matchContexts = (MatchContexts) Util.findView(MatchContexts.ID);
//				matchContexts.setContextsToMatch(contexts);
//				matchContexts.setContextResourcesToMatch(contextResources);
//				matchContexts.update();
//			}
//		});
//
//		final List<String> properties = new ArrayList<String>();
//		final List<Resource> propertyResources = new ArrayList<Resource>();
//		for (String flowPropertyConcat : flowPropertyMap.keySet()) {
//			properties.add(flowPropertyConcat);
//		}
//		Collections.sort(properties);
//		for (String flowPropertyConcat : properties) {
//			propertyResources.add(flowPropertyMap.get(flowPropertyConcat).getTdbResource());
//		}
//		Display.getDefault().asyncExec(new Runnable() {
//			public void run() {
//				MatchProperties matchProperties = (MatchProperties) Util.findView(MatchProperties.ID);
//				matchProperties.setPropertiesToMatch(properties);
//				matchProperties.setPropertyResourcesToMatch(propertyResources);
//
//				matchProperties.update();
//			}
//		});

		return Status.OK_STATUS;
	}

	public Integer[] getHitCounts() {
		return results;
	}

}
