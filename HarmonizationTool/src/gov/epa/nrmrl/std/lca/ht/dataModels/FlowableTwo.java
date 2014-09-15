package gov.epa.nrmrl.std.lca.ht.dataModels;

import java.util.ArrayList;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.vocabulary.RDFS;

import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataPropertyProvider;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.SKOS;

public class FlowableTwo extends LCADataObjectProvider {
	public FlowableTwo(LCADataObjectProvider parent) {
		super();
		thingName = "Flowable";
		rdfClass = ECO.Flowable;
		lcaDataProperties = new ArrayList<LCADataPropertyProvider>();
	}

	public void getHeaderMenuObjects() {
		LCADataPropertyProvider lcaDataProperty;

		lcaDataProperty = new LCADataPropertyProvider("Name");
		lcaDataProperty.setRequired(true);
		lcaDataProperty.setUnique(true);
		// lcaDataProperty.setCheckLists(getFlowablesNameCheckList());
		lcaDataProperty.setTDBProperty(RDFS.label);
		lcaDataProperty.setRDFDatatype(XSDDatatype.XSDstring);
		// lcaDataProperties.add(lcaDataProperty);

		lcaDataProperty = new LCADataPropertyProvider("Synonym");
		lcaDataProperty.setRequired(false);
		lcaDataProperty.setUnique(false);
		lcaDataProperty.setTDBProperty(SKOS.altLabel);
		lcaDataProperty.setRDFDatatype(XSDDatatype.XSDstring);

		lcaDataProperty = new LCADataPropertyProvider("CAS RN");
		lcaDataProperty.setRequired(false);
		lcaDataProperty.setUnique(true);
		lcaDataProperty.setLeftJustified(false);
		lcaDataProperty.setTDBProperty(ECO.casNumber);
		lcaDataProperty.setRDFDatatype(XSDDatatype.XSDstring);

		lcaDataProperty = new LCADataPropertyProvider("Formula");
		lcaDataProperty.setRequired(false);
		lcaDataProperty.setUnique(false);
		lcaDataProperty.setLeftJustified(false);
		lcaDataProperty.setTDBProperty(ECO.chemicalFormula);
		lcaDataProperty.setRDFDatatype(XSDDatatype.XSDstring);

		lcaDataProperty = new LCADataPropertyProvider("SMILES");
		lcaDataProperty.setRequired(false);
		lcaDataProperty.setUnique(false);
		lcaDataProperty.setLeftJustified(false);
		lcaDataProperty.setTDBProperty(FedLCA.hasSmilesString);
		lcaDataProperty.setRDFDatatype(XSDDatatype.XSDstring);
	}
}
