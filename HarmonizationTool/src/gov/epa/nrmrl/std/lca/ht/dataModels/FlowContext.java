package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.RDFUtil;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FASC;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

public class FlowContext {
	// CLASS VARIABLES
	public static final String flowContextPrimaryIdentifier = "Primary Info";
	public static final String flowContextAdditionalIdentifier = "Additional Info";
	private static final Resource rdfClass = FASC.Compartment;
	// NOTE: EVENTUALLY label AND comment SHOULD COME FROM ONTOLOGY
	public static final String label = "Flow Context";
	public static final String comment = "Compartments are used for classifying effects.  Effects have a hasCompartment property and the type of the value of that property may be used to classify the effect.  Examples of compartments include emissions to urban air and resource consumption from water.";
	private static Map<String, LCADataPropertyProvider> dataPropertyMap;

	static {
		ActiveTDB.tsReplaceLiteral(rdfClass, RDFS.label, label);
		ActiveTDB.tsAddLiteral(rdfClass, RDFS.comment, comment);
		System.out.println("label assigned to Flow Context");

//		System.out.println("Flow Context has just assigned label and comment");
//		if (rdfClass.hasProperty(RDFS.label)) { // <-- THIS IS SUPPOSED TO CHECK THE ASSIGNMENT
//			System.out.println(rdfClass.getProperty(RDFS.label).getString());
//		} else {
//			System.out.println("wtf");
//		}
		// JUNO: THE ABOVE GIVES THE "wtf" RESULT, BUT A QUERY SHOWS THE TRIPLES ARE ADDED. WTF?

		dataPropertyMap = new LinkedHashMap<String, LCADataPropertyProvider>();
		LCADataPropertyProvider lcaDataPropertyProvider;

		lcaDataPropertyProvider = new LCADataPropertyProvider(flowContextPrimaryIdentifier);
		lcaDataPropertyProvider.setPropertyClass(label);
		lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
		lcaDataPropertyProvider.setRequired(true);
		lcaDataPropertyProvider.setUnique(true);
		lcaDataPropertyProvider.setLeftJustified(true);
		lcaDataPropertyProvider.setCheckLists(getContextNameCheckList());
		lcaDataPropertyProvider.setTDBProperty(FASC.hasCompartment);
		dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);

		lcaDataPropertyProvider = new LCADataPropertyProvider(flowContextAdditionalIdentifier);
		lcaDataPropertyProvider.setPropertyClass(label);
		lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
		lcaDataPropertyProvider.setRequired(false);
		lcaDataPropertyProvider.setUnique(false);
		lcaDataPropertyProvider.setLeftJustified(true);
		lcaDataPropertyProvider.setCheckLists(getContextNameCheckList());
		lcaDataPropertyProvider.setTDBProperty(FedLCA.flowContextSupplementalDescription);
		dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);
	}

	// INSTANCE VARIABLES
	private Resource tdbResource;
	private List<LCADataValue> lcaDataValues;
	private Resource matchingResource;
//	private Set<Resource> matchCandidates;

	// CONSTRUCTORS
	public FlowContext() {
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
		lcaDataValues = new ArrayList<LCADataValue>();
		matchingResource = null;
	}

	public FlowContext(Resource tdbResource) {
		this.tdbResource = tdbResource;
		lcaDataValues = new ArrayList<LCADataValue>();
		clearSyncDataFromTDB();
		// TODO? SYNC THE MATCHING RESOURCE?
	}

	// METHODS
	public Object getOneProperty(String key) {
		for (LCADataValue lcaDataValue : lcaDataValues) {
			if (lcaDataValue.getLcaDataPropertyProvider().getPropertyName().equals(key)) {
				return lcaDataValue.getValue();
			}
		}
		return null;
	}

	public Object[] getAllProperties(String key) {
		List<Object> resultList = new ArrayList<Object>();
		for (LCADataValue lcaDataValue : lcaDataValues) {
			if (lcaDataValue.getLcaDataPropertyProvider().getPropertyName().equals(key)) {
				resultList.add(lcaDataValue.getValue());
			}
		}
		Object[] results = new Object[resultList.size()];
		if (resultList.size() == 0) {
			return null;
		}
		for (int i = 0; i < resultList.size(); i++) {
			results[i] = resultList.get(i);
		}
		return results;
	}

	public List<LCADataValue> getPropertyValuesInOrder() {
		List<LCADataValue> results = new ArrayList<LCADataValue>();
		for (String key : dataPropertyMap.keySet()) {
			for (LCADataValue lcaDataValue : lcaDataValues) {
				if (lcaDataValue.getLcaDataPropertyProvider().getPropertyName().equals(key)) {
					results.add(lcaDataValue);
				}
			}
		}
		return results;
	}

	public void setProperty(String key, Object object) {
		if (object == null) {
			return;
		}
		if (!dataPropertyMap.containsKey(key)) {
			return;
		}
		LCADataPropertyProvider lcaDataPropertyProvider = dataPropertyMap.get(key);
		RDFDatatype rdfDatatype = lcaDataPropertyProvider.getRdfDatatype();
		Class<?> objectClass = RDFUtil.getJavaClassFromRDFDatatype(rdfDatatype);
		if (!objectClass.equals(object.getClass())) {
			return;
		}
		LCADataValue newLCADataValue = new LCADataValue();
		newLCADataValue.setLcaDataPropertyProvider(lcaDataPropertyProvider);
		newLCADataValue.setValue(object);
		// newLCADataValue.setValueAsString(object.toString()); // SHOULD WE DO THIS AT ALL?

		if (lcaDataPropertyProvider.isUnique()) {
			removeValues(lcaDataPropertyProvider.getPropertyName());
			ActiveTDB.tsReplaceLiteral(tdbResource, lcaDataPropertyProvider.getTDBProperty(), rdfDatatype, object);
		} else {
			ActiveTDB.tsAddLiteral(tdbResource, lcaDataPropertyProvider.getTDBProperty(), rdfDatatype, object);
		}
		lcaDataValues.add(newLCADataValue);
	}

	private void removeValues(String key) {
		for (LCADataValue lcaDataValue : lcaDataValues) {
			if (lcaDataValue.getLcaDataPropertyProvider().getPropertyName().equals(key)) {
				lcaDataValues.remove(lcaDataValue);
			}
		}
	}

	public void updateSyncDataFromTDB() {
		if (tdbResource == null) {
			return;
		}
		// LCADataPropertyProvider LIST IS ALL LITERALS
		for (LCADataPropertyProvider lcaDataPropertyProvider : dataPropertyMap.values()) {
			if (!tdbResource.hasProperty(lcaDataPropertyProvider.getTDBProperty())) {
				continue;
			}
			if (lcaDataPropertyProvider.isUnique()) {
				removeValues(lcaDataPropertyProvider.getPropertyName());
				Object value = tdbResource.getProperty(lcaDataPropertyProvider.getTDBProperty()).getLiteral()
						.getValue();
				if (value.getClass().equals(
						RDFUtil.getJavaClassFromRDFDatatype(lcaDataPropertyProvider.getRdfDatatype()))) {
					LCADataValue lcaDataValue = new LCADataValue();
					lcaDataValue.setLcaDataPropertyProvider(lcaDataPropertyProvider);
					lcaDataValue.setValue(value);
					lcaDataValues.add(lcaDataValue);
				}
			} else {
				StmtIterator stmtIterator = tdbResource.listProperties(lcaDataPropertyProvider.getTDBProperty());
				while (stmtIterator.hasNext()) {
					Object value = stmtIterator.next().getLiteral().getValue();
					if (value.getClass().equals(
							RDFUtil.getJavaClassFromRDFDatatype(lcaDataPropertyProvider.getRdfDatatype()))) {
						LCADataValue lcaDataValue = new LCADataValue();
						lcaDataValue.setLcaDataPropertyProvider(lcaDataPropertyProvider);
						lcaDataValue.setValue(value);
						lcaDataValues.add(lcaDataValue);
					}
				}
			}
		}
	}

	public void clearSyncDataFromTDB() {
		lcaDataValues.clear();
		updateSyncDataFromTDB();
	}

	private static List<QACheck> getContextNameCheckList() {
		List<QACheck> qaChecks = QACheck.getGeneralQAChecks();
		return qaChecks;
	}

	public Resource getTdbResource() {
		return tdbResource;
	}

	public static Resource getRdfclass() {
		return rdfClass;
	}

	public static Map<String, LCADataPropertyProvider> getDataPropertyMap() {
		return dataPropertyMap;
	}

	public Resource getMatchingResource() {
		return matchingResource;
	}

	public void setMatchingResource(Resource matchingResource) {
		this.matchingResource = matchingResource;
		ActiveTDB.tsReplaceResource(tdbResource, OWL.sameAs, matchingResource);
	}

}
