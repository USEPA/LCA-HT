package gov.epa.nrmrl.std.lca.ht.dataModels;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.PartInitException;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.FlowContext;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.FlowProperty;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.Flowable;
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Flow {
	public static final String openLCAUUID = "UUID";
	public static final String label = "Flow";
	public static final String comment = "A flow is a combination of a Flowable, a Flow Context, and a Flow Proprety.  An example is CO2 emitted to unspecified air measured by mass.";

	private Flowable flowable;
	private FlowContext flowContext;
	private FlowProperty flowProperty;
	private Resource tdbResource;
	private static final Resource rdfClass = FedLCA.Flow;
	private static Map<String, LCADataPropertyProvider> dataPropertyMap;

	static {
		ActiveTDB.tsAddGeneralTriple(rdfClass, RDFS.label, label, null);
		ActiveTDB.tsAddGeneralTriple(rdfClass, RDFS.comment, comment, null);
		ActiveTDB.tsAddGeneralTriple(rdfClass, RDF.type, OWL.Class, null);

		dataPropertyMap = new LinkedHashMap<String, LCADataPropertyProvider>();
		LCADataPropertyProvider lcaDataPropertyProvider;

		lcaDataPropertyProvider = new LCADataPropertyProvider(openLCAUUID);
		lcaDataPropertyProvider.setPropertyClass(label);
		lcaDataPropertyProvider.setRDFClass(rdfClass);
		lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
		lcaDataPropertyProvider.setRequired(false);
		lcaDataPropertyProvider.setUnique(true);
		lcaDataPropertyProvider.setLeftJustified(true);
		lcaDataPropertyProvider.setCheckLists(QACheck.getUUIDCheck());
		lcaDataPropertyProvider.setTDBProperty(FedLCA.hasOpenLCAUUID);
		dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);
	}

	// private static final Resource rdfClass = FASC.FlowAggregationCategory;

	public static Resource getRdfclass() {
		return rdfClass;
	}

	public Flow() {
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
	}

	public Flowable getFlowable() {
		return flowable;
	}

	public void setFlowable(Flowable flowable) {
		if (flowable == null) {
			return;
		}
		this.flowable = flowable;
		ActiveTDB.tsReplaceObject(tdbResource, ECO.hasFlowable, flowable.getTdbResource());
	}

	public FlowContext getFlowContext() {
		return flowContext;
	}

	public void setFlowContext(FlowContext flowContext) {
		if (flowContext == null) {
			return;
		}
		this.flowContext = flowContext;
		ActiveTDB.tsReplaceObject(tdbResource, FedLCA.hasFlowContext, flowContext.getTdbResource());
	}

	public FlowProperty getFlowProperty() {
		return flowProperty;
	}

	public void setFlowProperty(FlowProperty flowProperty) {
		if (flowProperty == null) {
			return;
		}
		this.flowProperty = flowProperty;
		ActiveTDB.tsReplaceObject(tdbResource, FedLCA.hasFlowProperty, flowProperty.getTdbResource());
	}

	public Resource getTdbResource() {
		return tdbResource;
	}

	public void setTdbResource(Resource tdbResource) {
		this.tdbResource = tdbResource;
	}

	public void remove() {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(null);
		try {
			StmtIterator stmtIterator = tdbResource.listProperties();
			while (stmtIterator.hasNext()) {
				tdbModel.remove(stmtIterator.next());
			}
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	// public void setThree(Flowable flowable2, FlowContext flowContext2, FlowProperty flowProperty2) {
	// this.flowable = flowable2;
	// this.flowContext = flowContext2;
	// this.flowProperty = flowProperty2;
	// ActiveTDB.tsAddThree(tdbResource, ECO.hasFlowable, flowable2.getTdbResource(), FedLCA.hasFlowContext,
	// flowContext2.getTdbResource(), FedLCA.hasFlowProperty, flowProperty2.getTdbResource());
	// }

	// public static void addFlowData(int rowNumberPlusOne, Flowable flowable2, FlowContext flowContext2,
	// FlowProperty flowProperty2, Resource dataSourceResource) {
	// // Model tdbModel = ActiveTDB.getModel();
	// if (ActiveTDB.tdbDataset.isInTransaction()) {
	// System.out.println("!!!!!!!!!!!!!!Transaction in transaction");
	// System.out.println(new Object() {
	// }.getClass().getEnclosingMethod().getName());
	// }
	//
	// // --- BEGIN SAFE -WRITE- TRANSACTION ---
	// ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
	// Model tdbModel = ActiveTDB.getModel(null);
	// try {
	// Resource tdbResource = tdbModel.createResource(rdfClass);
	// if (rowNumberPlusOne > 0) {
	// Literal rowNumberLiteral = tdbModel.createTypedLiteral(rowNumberPlusOne);
	// tdbModel.add(tdbResource, FedLCA.sourceTableRowNumber, rowNumberLiteral);
	// }
	// if (flowable2 != null) {
	// tdbModel.add(tdbResource, ECO.hasFlowable, flowable2.getTdbResource());
	// }
	// if (flowContext2 != null) {
	// tdbModel.add(tdbResource, FedLCA.hasFlowContext, flowContext2.getTdbResource());
	// }
	// if (flowProperty2 != null) {
	// tdbModel.add(tdbResource, FedLCA.hasFlowProperty, flowProperty2.getTdbResource());
	// }
	// if (dataSourceResource != null) {
	// tdbModel.add(tdbResource, ECO.hasDataSource, dataSourceResource);
	// }
	// ActiveTDB.tdbDataset.commit();
	// } catch (Exception e) {
	// System.out.println("addFlowData failed; see Exception: " + e);
	// ActiveTDB.tdbDataset.abort();
	// } finally {
	// ActiveTDB.tdbDataset.end();
	// }
	// // ---- END SAFE -WRITE- TRANSACTION ---
	// }

	public static void addAllFlowData() {
		// Model tdbModel = ActiveTDB.getModel();
		if (ActiveTDB.tdbDataset.isInTransaction()) {
			System.out.println("!!!!!!!!!!!!!!Transaction in transaction");
			System.out.println(new Object() {
			}.getClass().getEnclosingMethod().getName());
			return;
			// TODO: MAKE THIS AN ASSERT
		}

		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(null);
		try {
			Util.showView(CSVTableView.ID);
			TableProvider tableProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey());
			Resource dataSourceResource = tableProvider.getDataSourceProvider().getTdbResource();
			LCADataPropertyProvider[] lcaDataPropertyProviders = tableProvider.getLcaDataProperties();
			int uuidRow = -1;
			for (int col = 0; col < lcaDataPropertyProviders.length; col++) {
				LCADataPropertyProvider lcaDataPropertyProvider = lcaDataPropertyProviders[col];
				if (lcaDataPropertyProvider != null) {
					if (lcaDataPropertyProvider.getRDFClass() != null) {
						if (lcaDataPropertyProvider.getRDFClass().equals(rdfClass)) {
							if (lcaDataPropertyProvider.getPropertyName().equals(openLCAUUID)) {
								uuidRow = col;
							}
						}
					}
				}
			}

			List<Integer> rowsToIgnore = CSVTableView.getRowsToIgnore();
			for (int i = 0; i < tableProvider.getData().size(); i++) {
				if (rowsToIgnore.contains(i)) {
					continue;
				}
				int rowNumberPlusOne = i + 1;
				DataRow dataRow = tableProvider.getData().get(i);

				Resource tdbResource = tdbModel.createResource(rdfClass);
				if (rowNumberPlusOne > 0) {
					Literal rowNumberLiteral = tdbModel.createTypedLiteral(rowNumberPlusOne);
					tdbModel.add(tdbResource, FedLCA.sourceTableRowNumber, rowNumberLiteral);
				}
				if (dataRow.getFlowable() != null) {
					tdbModel.add(tdbResource, ECO.hasFlowable, dataRow.getFlowable().getTdbResource());
				}
				if (dataRow.getFlowContext() != null) {
					tdbModel.add(tdbResource, FedLCA.hasFlowContext, dataRow.getFlowContext().getTdbResource());
				}
				if (dataRow.getFlowProperty() != null) {
					tdbModel.add(tdbResource, FedLCA.hasFlowProperty, dataRow.getFlowProperty().getTdbResource());
				}
				if (dataSourceResource != null) {
					tdbModel.add(tdbResource, ECO.hasDataSource, dataSourceResource);
				}
				if (uuidRow > -1) {
					String value = dataRow.get(uuidRow-1);
					if (!value.equals("")) {
						Literal valueAsLiteral = tdbModel.createTypedLiteral(value);
						tdbModel.add(tdbResource, FedLCA.hasOpenLCAUUID, valueAsLiteral);
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
		matchFlows();

	}

	public static int matchFlows() {
		try {
			Util.showView(CSVTableView.ID);
			Util.showView(FlowsWorkflow.ID);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TableProvider tableProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey());
		String dataSourceName = tableProvider.getDataSourceProvider().getDataSourceName();
		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		b.append("select distinct \n");
		b.append("  ?rowNumber \n");
		b.append(" \n");
		b.append("where { \n");
		b.append(" \n");
		b.append("  #--- FLOWABLE \n");
		b.append("  ?f a fedlca:Flow . \n");
		b.append("  ?f eco:hasDataSource ?ds . \n");
		b.append("  ?ds rdfs:label \"" + dataSourceName + "\"^^xsd:string . \n");
		b.append("  ?mf a fedlca:Flow . \n");
		b.append("  ?mf eco:hasDataSource ?mds . \n");
		b.append("  ?mds a lcaht:MasterDataset . \n");

		b.append("  ?f eco:hasFlowable ?flowable . \n");
		b.append("  ?mf eco:hasFlowable ?flowable . \n");
		b.append("  ?f fedlca:hasFlowContext ?flowContext . \n");
		b.append("  ?mf fedlca:hasFlowContext ?flowContext . \n");
		b.append("  ?f fedlca:hasFlowProperty ?flowPropertyUnit . \n");
		b.append("  ?flowPropertyUnit fedlca:flowPropertyString ?flowPropertyString . \n");
		b.append("  ?mf fedlca:hasFlowProperty ?flowProperty . \n");
		b.append("  filter (str(?flowPropertyString) = str(afn:localname(?flowProperty)))  . \n");
		b.append("  ?f fedlca:sourceTableRowNumber ?rowNumber . \n");

		b.append("} \n");
		b.append("order by ?rowNumber \n");

		String query = b.toString();
		System.out.println("Flow matching query \n" + query);

		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);
		harmonyQuery2Impl.setGraphName(ActiveTDB.importGraphName);

		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		int hits = 0;
		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			int matchedRowNumber = querySolution.get("rowNumber").asLiteral().getInt();
			FlowsWorkflow.addFlowRowNum(matchedRowNumber);
			hits++;
		}
		return hits;
	}

	public static Map<String, LCADataPropertyProvider> getDataPropertyMap() {
		return dataPropertyMap;
	}

	public static void setDataPropertyMap(Map<String, LCADataPropertyProvider> dataPropertyMap) {
		Flow.dataPropertyMap = dataPropertyMap;
	}
}
