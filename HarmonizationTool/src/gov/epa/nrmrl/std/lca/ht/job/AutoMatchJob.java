package gov.epa.nrmrl.std.lca.ht.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataCuration.ComparisonKeeper;
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
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.StopWatch;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * 
 * This class is run in a separate thread as it may take several minutes, and the user may wish to 
 * make assignments while it runs.
 * 
 * The purpose is to go through a table of LCI user data row by row first matching Flowables, Flow Contexts,
 * and Flow Properties.  At the end, the user Flows in which each of the three components are matched are
 * checked against a list of master Flows.
 * 
 * @author Tommy Cathey 919-541-1500
 * @author Tom Transue 919-541-0494
 * 
 * 
 */
public class AutoMatchJob extends Job {
	private String tableKey;
	private Integer[] results = new Integer[3];
	private static Color purple = new Color(Display.getCurrent(), 200, 0, 200);
	private static Color orange = new Color(Display.getCurrent(), 255, 128, 0);

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
		// StopWatch stopWatch02 = new StopWatch("Concat. and hash Flowables");
		// StopWatch stopWatch03 = new StopWatch("Create Flowables");
		// StopWatch stopWatch04 = new StopWatch("Find matches + color rows");
		// StopWatch stopWatch05 = new StopWatch("Flow Context and Properties");
		// StopWatch stopWatch06 = new StopWatch("Flow");

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
		int flowCSVColumnNumberForUUID = -1;

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
			} else if (lcaDataPropertyProvider.getPropertyClass().equals(Flow.label)) {
				if (lcaDataPropertyProvider.getPropertyName().equals(Flow.openLCAUUID)) {
					flowCSVColumnNumberForUUID = i;
				}
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
				FlowsWorkflow.setTextCommit("0/3 steps");
			}
		});
		int percentComplete = 0;
		stopWatch01.start();

		/* Collect master Flow UUIDs (if any) */
		List<String> masterFlowUUIDs = new ArrayList<String>();
		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		b.append("select distinct ?uuid \n");
		b.append("where {  \n");
		b.append("  ?f fedlca:hasOpenLCAUUID ?uuid .  \n");
		b.append("  ?f a fedlca:Flow . \n");
		b.append("  ?f eco:hasDataSource ?mds . \n");
		b.append("  ?mds a lcaht:MasterDataset . \n");
		b.append("} \n");
		String query = b.toString();

		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);

		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			RDFNode rdfNode = querySolution.get("uuid");
			masterFlowUUIDs.add(rdfNode.asLiteral().getString());
		}

		// ========================== BEGIN ROW BY ROW ==========================
		for (int rowNumber = 0; rowNumber < tableProvider.getData().size(); rowNumber++) {
			if (rowsToIgnore.contains(rowNumber)) {
				continue;
			}

			if (100 * rowNumber / tableProvider.getData().size() >= percentComplete) {
				final String state = "1/3: " + percentComplete + "%";

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
			// stopWatch02.start();

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
			// stopWatch02.stop();
			if (flowable == null) {
				// stopWatch03.start();
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

					if (lcaDataPropertyProvider.getPropertyName().equals(Flowable.casString)
							&& lcaDataPropertyProvider.getPropertyClass().equals(Flowable.label) && dataValue != null
							&& dataValue.length() != 0) {
						String standardCas = Flowable.standardizeCAS(dataValue);
						if (!(standardCas == null) && (!standardCas.equals(""))) {
							ActiveTDB.tsAddGeneralTriple(flowable.getTdbResource(), FedLCA.hasFormattedCAS,
									standardCas, null);
						}
					}
				}
				// stopWatch03.stop();
				// stopWatch04.start();
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
				// stopWatch04.stop();
			}
			dataRow.setFlowable(flowable);
			// }
			// stopWatch05.start();

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
						// flowUnit.name = dataValue;
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
			}

			// ========================== FLOW (UUIDs ONLY) ==========================
			if (flowCSVColumnNumberForUUID > -1) {
				String uuid = dataRow.get(flowCSVColumnNumberForUUID - 1);
				if (masterFlowUUIDs.contains(uuid)) {
					final int masterUUIDRowNum = rowNumToSend;
					final int rowNum = flowCSVColumnNumberForUUID;
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							CSVTableView.colorCell(masterUUIDRowNum, 0, orange);
							CSVTableView.colorCell(masterUUIDRowNum, rowNum, orange);
						}
					});

				}
				// String dataValue =

			}
		}
		// ========================== FLOW ==========================
		// stopWatch06.start();

		ComparisonKeeper.commitUncommittedComparisons();
		Resource dataSourceResource = tableProvider.getDataSourceProvider().getTdbResource();
		List<Integer> rowsToCheck = new ArrayList<Integer>();
		percentComplete = 0;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(null);
		try {
			// ========================== BEGIN ROW BY ROW ==========================
			for (int rowNumber = 0; rowNumber < tableProvider.getData().size(); rowNumber++) {
				if (rowsToIgnore.contains(rowNumber)) {
					continue;
				}
				if (100 * rowNumber / tableProvider.getData().size() >= percentComplete) {
					final String state = "2/3: " + percentComplete + "%";
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							FlowsWorkflow.setTextCommit(state);
							// Flow.addAllFlowData();
						}
					});
					percentComplete += 1;
				}

				int rowNumberPlusOne = rowNumber + 1;
				DataRow dataRow = tableProvider.getData().get(rowNumber);
				boolean findMatchingFlow = true;
				Resource tdbResource = tdbModel.createResource(Flow.getRdfclass());
				if (rowNumberPlusOne > 0) {
					Literal rowNumberLiteral = tdbModel.createTypedLiteral(rowNumberPlusOne);
					tdbModel.add(tdbResource, FedLCA.sourceTableRowNumber, rowNumberLiteral);

					if (dataRow.getFlowable() != null) {
						tdbModel.add(tdbResource, ECO.hasFlowable, dataRow.getFlowable().getTdbResource());
					} else {
						findMatchingFlow = false;
					}
					if (dataRow.getFlowContext() != null) {
						tdbModel.add(tdbResource, FedLCA.hasFlowContext, dataRow.getFlowContext().getTdbResource());
					} else {
						findMatchingFlow = false;
					}
					if (dataRow.getFlowUnit() != null) {
						// tdbModel.add(tdbResource, FedLCA.hasFlowProperty, dataRow.getFlowUnit().getTdbResource());
						tdbModel.add(tdbResource, FedLCA.hasFlowUnit, dataRow.getFlowUnit().getTdbResource());
					} else {
						findMatchingFlow = false;
					}
					if (dataSourceResource != null) {
						tdbModel.add(tdbResource, ECO.hasDataSource, dataSourceResource);
					} else {
						findMatchingFlow = false;
					}
					if (flowCSVColumnNumberForUUID > -1) {
						String value = dataRow.get(flowCSVColumnNumberForUUID - 1);
						if (!value.equals("")) {
							Literal valueAsLiteral = tdbModel.createTypedLiteral(value);
							tdbModel.add(tdbResource, FedLCA.hasOpenLCAUUID, valueAsLiteral);
						}
					}
					if (findMatchingFlow) {
						rowsToCheck.add(rowNumber);
					}
				}
			}
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("addFlowData failed; see Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---

		Map<Resource, Resource> flowMap = new HashMap<Resource, Resource>();
		// TableProvider tableProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey());
		String dataSourceName = tableProvider.getDataSourceProvider().getDataSourceName();
		percentComplete = 0;
		int counter = 0;
		for (int i : rowsToCheck) {
			counter++;
			if (100 * counter / rowsToCheck.size() >= percentComplete) {
				final String state = "3/3: " + percentComplete + "%";
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						FlowsWorkflow.setTextCommit(state);
						// Flow.addAllFlowData();
					}
				});
				percentComplete += 1;
			}
			FlowsWorkflow.addFlowRowNum(i, false);
			int iPlusOne = i + 1;
			b = new StringBuilder();
			b.append(Prefixes.getPrefixesForQuery());
			b.append("select distinct ?f ?mf \n");
			b.append(" \n");
			b.append("where { \n");
			b.append(" \n");
			b.append("  ?f fedlca:sourceTableRowNumber " + iPlusOne + " . \n");
			b.append("  ?f eco:hasFlowable ?flowable . \n");
			b.append("  optional {?f fedlca:hasOpenLCAUUID ?uuid } \n");
			b.append("  ?c fedlca:comparedSource ?flowable . \n");
			b.append("  ?c fedlca:comparedMaster ?mflowable . \n");
			b.append("  ?c fedlca:comparedEquivalence fedlca:Equivalent . \n");
			b.append("  ?mf eco:hasFlowable ?mflowable . \n");

			b.append("  ?f fedlca:hasFlowUnit ?flowUnit . \n");
			b.append("  ?flowUnit owl:sameAs ?mflowUnit . \n");
			b.append("  ?mug fedlca:hasFlowUnit ?mflowUnit . \n");
			b.append("  ?mFlowProperty fedlca:belongsToUnitGroup ?mug . \n");
			b.append("  ?mf fedlca:hasFlowProperty ?mFlowProperty . \n");

			b.append("  ?f fedlca:hasFlowContext ?flowContext . \n");
			b.append("  ?flowContext owl:sameAs ?mflowContext . \n");
			b.append("  ?mf fedlca:hasFlowContext ?mflowContext . \n");

			b.append("  ?f a fedlca:Flow . \n");
			b.append("  ?f eco:hasDataSource ?ds . \n");
			b.append("  ?ds rdfs:label \"" + dataSourceName + "\"^^xsd:string . \n");
			b.append("  ?c a fedlca:Comparison . \n");
			b.append("  ?mf a fedlca:Flow . \n");
			b.append("  ?mf eco:hasDataSource ?mds . \n");
			b.append("  ?mds a lcaht:MasterDataset . \n");

			b.append("} \n");

			query = b.toString();
			// System.out.println("Flow matching query \n" + query);

			harmonyQuery2Impl = new HarmonyQuery2Impl();
			harmonyQuery2Impl.setQuery(query);
			harmonyQuery2Impl.setGraphName(null);

			resultSet = harmonyQuery2Impl.getResultSet();
			final int colorIt = i;

			if (resultSet.hasNext()) {
				QuerySolution querySolution = resultSet.next();
				Resource userFlowResource = querySolution.get("f").asResource();
				Resource masterFlowResource = querySolution.get("mf").asResource();
				// RDFNode uuidNode = querySolution.get("uuid");
				// if (uuidNode != null){
				// String uuidString = uuidNode.asLiteral().getString();
				// ActiveTDB.getModel(null).createResource(OpenLCA.NS+uuidString);
				// }

				flowMap.put(userFlowResource, masterFlowResource);

				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						FlowsWorkflow.addMatchFlowRowNum(colorIt);
					}
				});
			} else {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						FlowsWorkflow.removeMatchFlowRowNum(colorIt);
					}
				});
			}
		}

		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		tdbModel = ActiveTDB.getModel(null);
		try {
			for (Resource key : flowMap.keySet()) {
				tdbModel.add(key, OWL2.sameAs, flowMap.get(key));
			}
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("addFlowData failed; see Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---

		// return flowMap.size();
		// }

		// matchFlows(rowsToCheck);

		// }
		// stopWatch06.stop();
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
