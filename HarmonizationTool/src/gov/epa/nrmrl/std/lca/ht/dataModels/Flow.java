package gov.epa.nrmrl.std.lca.ht.dataModels;

import java.util.List;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.FlowContext;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.FlowProperty;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.Flowable;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FASC;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class Flow {
	private Flowable flowable;
	private FlowContext flowContext;
	private FlowProperty flowProperty;
	private Resource tdbResource;
	private static final Resource rdfClass = FedLCA.Flow;
//	private static final Resource rdfClass = FASC.FlowAggregationCategory;

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

	public void setThree(Flowable flowable2, FlowContext flowContext2, FlowProperty flowProperty2) {
		this.flowable = flowable2;
		this.flowContext = flowContext2;
		this.flowProperty = flowProperty2;
		ActiveTDB.tsAddThree(tdbResource, ECO.hasFlowable, flowable2.getTdbResource(), FedLCA.hasFlowContext,
				flowContext2.getTdbResource(), FedLCA.hasFlowProperty, flowProperty2.getTdbResource());
	}

	public static void addFlowData(int rowNumberPlusOne, Flowable flowable2, FlowContext flowContext2,
			FlowProperty flowProperty2, Resource dataSourceResource) {
		// Model tdbModel = ActiveTDB.getModel();
		if (ActiveTDB.tdbDataset.isInTransaction()) {
			System.out.println("!!!!!!!!!!!!!!Transaction in transaction");
			System.out.println(new Object() {
			}.getClass().getEnclosingMethod().getName());
		}

		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(null);
		try {
			Resource tdbResource = tdbModel.createResource(rdfClass);
			if (rowNumberPlusOne > 0) {
				Literal rowNumberLiteral = tdbModel.createTypedLiteral(rowNumberPlusOne);
				tdbModel.add(tdbResource, FedLCA.sourceTableRowNumber, rowNumberLiteral);
			}
			if (flowable2 != null) {
				tdbModel.add(tdbResource, ECO.hasFlowable, flowable2.getTdbResource());
			}
			if (flowContext2 != null) {
				tdbModel.add(tdbResource, FedLCA.hasFlowContext, flowContext2.getTdbResource());
			}
			if (flowProperty2 != null) {
				tdbModel.add(tdbResource, FedLCA.hasFlowProperty, flowProperty2.getTdbResource());
			}
			if (dataSourceResource != null) {
				tdbModel.add(tdbResource, ECO.hasDataSource, dataSourceResource);
			}
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("addFlowData failed; see Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

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
			}
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("addFlowData failed; see Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}
}
