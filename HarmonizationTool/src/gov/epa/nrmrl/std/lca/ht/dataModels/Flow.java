package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.FlowContext;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.FlowProperty;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.Flowable;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FASC;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class Flow {
	private Flowable flowable;
	private FlowContext flowContext;
	private FlowProperty flowProperty;
	private Resource tdbResource;
	private static final Resource rdfClass = FASC.FlowAggregationCategory;

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
		ActiveTDB.tsReplaceResource(tdbResource, ECO.hasFlowable, flowable.getTdbResource());
	}

	public FlowContext getFlowContext() {
		return flowContext;
	}

	public void setFlowContext(FlowContext flowContext) {
		if (flowContext == null) {
			return;
		}
		this.flowContext = flowContext;
		ActiveTDB.tsReplaceResource(tdbResource, FASC.hasCompartment, flowContext.getTdbResource());
	}

	public FlowProperty getFlowProperty() {
		return flowProperty;
	}

	public void setFlowProperty(FlowProperty flowProperty) {
		if (flowProperty == null) {
			return;
		}
		this.flowProperty = flowProperty;
		ActiveTDB.tsReplaceResource(tdbResource, FedLCA.hasFlowProperty, flowProperty.getTdbResource());
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
		Model tdbModel = ActiveTDB.tdbDataset.getDefaultModel();
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
		ActiveTDB.tsAddThree(tdbResource, ECO.hasFlowable, flowable2.getTdbResource(), FASC.hasCompartment,
				flowContext2.getTdbResource(), FedLCA.hasFlowProperty, flowProperty2.getTdbResource());
	}
}
