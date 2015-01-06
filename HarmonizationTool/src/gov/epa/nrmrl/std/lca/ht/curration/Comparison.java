package gov.epa.nrmrl.std.lca.ht.curration;

import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;

import com.hp.hpl.jena.rdf.model.Resource;

public class Comparison {
	private static Resource tdbClass = FedLCA.Comparison;

	private Resource userDataObject = null;
	private Resource masterDataObject = null;
	private Resource equivalence = null;
	private Resource annotationResource = null;
	private String explanation = null;
	private Resource createdBy = null;
	
}
