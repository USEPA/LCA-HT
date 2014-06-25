package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FASC;
import harmonizationtool.vocabulary.FEDLCA;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class Flow {
	private Flowable flowable;
	private FlowContext flowContext;
	private Resource tdbResource;

	public Flow() {
		this.tdbResource = ActiveTDB.model.createResource();
		this.tdbResource.addProperty(RDF.type, FASC.FlowAggregationCategory);
	}

	public Flow(Flowable flowable, FlowContext flowContext) {
		this.flowable = flowable;
		this.flowContext = flowContext;
		this.tdbResource = ActiveTDB.model.createResource();
		this.tdbResource.addProperty(RDF.type, FASC.FlowAggregationCategory);
		this.tdbResource.addProperty(ECO.hasFlowable, this.flowable.getTdbResource());
		this.tdbResource.addProperty(FEDLCA.hasFlowContext, this.flowContext.getTdbResource());
		this.tdbResource.addProperty(FASC.hasCompartment, this.flowContext.getTdbResource());
	}

	public Flowable getFlowable() {
		return flowable;
	}

	public void setFlowable(Flowable flowable) {
		this.flowable = flowable;
		tdbResource.removeAll(ECO.hasFlowable);
		tdbResource.addProperty(ECO.hasFlowable, flowable.getTdbResource());
	}

	public FlowContext getFlowContext() {
		return flowContext;
	}

	public void setFlowContext(FlowContext flowContext) {
		this.flowContext = flowContext;
		tdbResource.removeAll(FASC.hasCompartment);
		tdbResource.removeAll(FEDLCA.hasFlowContext);
		tdbResource.addProperty(ECO.hasFlowable, flowContext.getTdbResource());
		tdbResource.addProperty(FEDLCA.hasFlowContext, flowContext.getTdbResource());
	}

	public Resource getTdbResource() {
		return tdbResource;
	}

	public void setTdbResource(Resource tdbResource) {
		this.tdbResource = tdbResource;
	}
}
