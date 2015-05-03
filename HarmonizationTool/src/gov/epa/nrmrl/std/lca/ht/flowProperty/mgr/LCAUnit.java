package gov.epa.nrmrl.std.lca.ht.flowProperty.mgr;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

public class LCAUnit {
	public String name;
	public String description;
	public List<String> synonyms = new ArrayList<String>();
	public String uuid;
	public String superGroup;
	public Resource unit_group;
	public Resource referenceUnit;
	private Resource tdbResource;
	public double conversionFactor;
	public FlowProperty flowProperty;
	private Resource matchingResource = null;

	public LCAUnit() {

	}

	public void setMatchingResource(Resource matchingResource) {
		if (matchingResource == null){
			ActiveTDB.tsRemoveAllLikeObjects(matchingResource, OWL.sameAs, null, null);
			this.matchingResource = null;
			return;
		}
		this.matchingResource = matchingResource;
		ActiveTDB.tsReplaceResourceSameType(tdbResource, OWL.sameAs, matchingResource, null);
	}

	public Resource getMatchingResource() {
		return matchingResource;
	}

	public String getUnitGroupName() {
		return unit_group.listProperties(RDFS.label).toList().get(0).getString();
	}

	public String getUnitGroupUUID() {
		return unit_group.listProperties(FedLCA.hasOpenLCAUUID).toList().get(0).getString();
	}

	public String getReferenceUnitName() {
		return referenceUnit.listProperties(RDFS.label).toList().get(0).getString();
	}

	public Resource getTdbResource() {
		return tdbResource;
	}

	public void setTdbResource(Resource tdbResource) {
		this.tdbResource = tdbResource;
	}
}
