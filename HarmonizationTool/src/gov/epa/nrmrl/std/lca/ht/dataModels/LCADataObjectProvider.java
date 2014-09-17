package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import java.util.ArrayList;
import java.util.List;
import com.hp.hpl.jena.rdf.model.Resource;

public class LCADataObjectProvider {
	protected List<LCADataPropertyProvider> lcaDataProperties= new ArrayList<LCADataPropertyProvider>();
	protected String thingName;
	protected Resource rdfClass;
	// private static final Model tdbModel = ActiveTDB.tdbModel;
	
	private Resource tdbResource;

	public LCADataObjectProvider() {
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
	}

	// private static final Model tdbModel = ActiveTDB.tdbModel;
	
	public LCADataObjectProvider(Resource tdbResource) {
		this.tdbResource = tdbResource;
		// syncDataFromTDB();
	}

	public List<LCADataPropertyProvider> getLcaDataProperties() {
		return lcaDataProperties;
	}

	public void setLCADataProperties(List<LCADataPropertyProvider> lcaDataProperties) {
		this.lcaDataProperties = lcaDataProperties;
	}

	public String getThingName() {
		return thingName;
	}

	public void setThingName(String thingName) {
		this.thingName = thingName;
	}

	public Resource getRDFClass() {
		return rdfClass;
	}

	public void setRDFClass(Resource rdfClass) {
		this.rdfClass = rdfClass;
	}

	public Resource getTDBResource() {
		return tdbResource;
	}

	public void setTDBResource(Resource tdbResource) {
		this.tdbResource = tdbResource;
	}

	// private static final Model tdbModel = ActiveTDB.tdbModel;


	// public void remove() {
	// // --- BEGIN SAFE -WRITE- TRANSACTION ---
	// ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
	// try {
	// tdbResource.removeAll(FedLCA.personName);
	// tdbResource.removeAll(FedLCA.affiliation);
	// tdbResource.removeAll(FedLCA.email);
	// tdbResource.removeAll(FedLCA.voicePhone);
	//
	// ActiveTDB.tdbDataset.commit();
	// } finally {
	// ActiveTDB.tdbDataset.end();
	// }
	// // ---- END SAFE -WRITE- TRANSACTION ---
	// }

	
}
