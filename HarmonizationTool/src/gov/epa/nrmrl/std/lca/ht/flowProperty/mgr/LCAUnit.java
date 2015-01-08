package gov.epa.nrmrl.std.lca.ht.flowProperty.mgr;

import com.hp.hpl.jena.rdf.model.Resource;

public class LCAUnit {
	public String name;
	public String description;
	public String synonyms;
	public String uuid;
	public String unit_group;
	public String referenceUnit;
	public Resource tdbResource;
	public double conversionFactor;
}
