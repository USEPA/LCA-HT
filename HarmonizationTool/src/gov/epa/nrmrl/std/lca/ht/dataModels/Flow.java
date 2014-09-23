package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.flowable.mgr.Flowable;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FASC;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Resource;

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
		try {
			tdbResource.removeProperties();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}
}
