package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.RDFUtil;
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
import com.hp.hpl.jena.vocabulary.RDFS;

public class FlowProperty {
	// CLASS VARIABLES
	public static final String flowPropertyPrimaryIdentifier = "Primary Info";
	public static final String flowPropertyAdditionalIdentifier = "Additional Info";
	public static final Resource rdfClass = FedLCA.FlowProperty;
	// NOTE: EVENTUALLY label AND comment SHOULD COME FROM ONTOLOGY
	public static final String label = "Flow Property";
	public static final String comment = "The Flow Property is the characteristic used to measure the quanitity of the flowable.  Examples include 'volume', 'mass*time', and 'person transport'.  For a given Flow Property, only certain units are valid: e.g. 'm3' for 'volume', 'kg*hr' for 'mass*time', and 'people*km' for 'person transport'.";

	private static Map<String, LCADataPropertyProvider> dataPropertyMap;

	static {
		ActiveTDB.tsReplaceLiteral(rdfClass, RDFS.label, label);
		ActiveTDB.tsAddLiteral(rdfClass, RDFS.comment, comment);
		System.out.println("label assigned to Flow Property");

		dataPropertyMap = new LinkedHashMap<String, LCADataPropertyProvider>();
		LCADataPropertyProvider lcaDataPropertyProvider;

		lcaDataPropertyProvider = new LCADataPropertyProvider(
				flowPropertyPrimaryIdentifier);
		lcaDataPropertyProvider.setPropertyClass(label);
		lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
		lcaDataPropertyProvider.setRequired(true);
		lcaDataPropertyProvider.setUnique(true);
		lcaDataPropertyProvider.setLeftJustified(true);
		lcaDataPropertyProvider.setCheckLists(getPropertyNameCheckList());
		lcaDataPropertyProvider
				.setTDBProperty(FedLCA.flowPropertyPrimaryDescription);
		dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(),
				lcaDataPropertyProvider);

		lcaDataPropertyProvider = new LCADataPropertyProvider(
				flowPropertyAdditionalIdentifier);
		lcaDataPropertyProvider.setPropertyClass(label);
		lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
		lcaDataPropertyProvider.setRequired(false);
		lcaDataPropertyProvider.setUnique(false);
		lcaDataPropertyProvider.setLeftJustified(true);
		lcaDataPropertyProvider.setCheckLists(getPropertyNameCheckList());
		lcaDataPropertyProvider
				.setTDBProperty(FedLCA.flowPropertySupplementalDescription);
		dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(),
				lcaDataPropertyProvider);
	}

	// INSTANCE VARIABLES
	private Resource tdbResource;
	private List<LCADataValue> lcaDataValues;
	private Resource matchingResource;

	// CONSTRUCTORS
	public FlowProperty() {
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
		lcaDataValues = new ArrayList<LCADataValue>();
	}

	public FlowProperty(Resource tdbResource) {
		this.tdbResource = tdbResource;
		lcaDataValues = new ArrayList<LCADataValue>();
		clearSyncDataFromTDB();
	}

	// METHODS
	public Object getOneProperty(String key) {
		for (LCADataValue lcaDataValue : lcaDataValues) {
			if (lcaDataValue.getLcaDataPropertyProvider().getPropertyName()
					.equals(key)) {
				return lcaDataValue.getValue();
			}
		}
		return null;
	}

	public Object[] getAllProperties(String key) {
		List<Object> resultList = new ArrayList<Object>();
		for (LCADataValue lcaDataValue : lcaDataValues) {
			if (lcaDataValue.getLcaDataPropertyProvider().getPropertyName()
					.equals(key)) {
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
				if (lcaDataValue.getLcaDataPropertyProvider().getPropertyName()
						.equals(key)) {
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
		LCADataPropertyProvider lcaDataPropertyProvider = dataPropertyMap
				.get(key);
		RDFDatatype rdfDatatype = lcaDataPropertyProvider.getRdfDatatype();
		Class<?> objectClass = RDFUtil.getJavaClassFromRDFDatatype(rdfDatatype);
		if (!objectClass.equals(object.getClass())) {
			return;
		}
		LCADataValue newLCADataValue = new LCADataValue();
		newLCADataValue.setLcaDataPropertyProvider(lcaDataPropertyProvider);
		newLCADataValue.setValue(object);
		// newLCADataValue.setValueAsString(object.toString()); // SHOULD WE DO
		// THIS AT ALL?

		if (lcaDataPropertyProvider.isUnique()) {
			removeValues(lcaDataPropertyProvider.getPropertyName());
			ActiveTDB.tsReplaceLiteral(tdbResource,
					lcaDataPropertyProvider.getTDBProperty(), rdfDatatype,
					object);
		} else {
			ActiveTDB.tsAddLiteral(tdbResource,
					lcaDataPropertyProvider.getTDBProperty(), rdfDatatype,
					object);
		}
		lcaDataValues.add(newLCADataValue);
	}

	private void removeValues(String key) {
		for (LCADataValue lcaDataValue : lcaDataValues) {
			if (lcaDataValue.getLcaDataPropertyProvider().getPropertyName()
					.equals(key)) {
				lcaDataValues.remove(lcaDataValue);
			}
		}
	}

	public void updateSyncDataFromTDB() {
		if (tdbResource == null) {
			return;
		}
		// LCADataPropertyProvider LIST IS ALL LITERALS
		for (LCADataPropertyProvider lcaDataPropertyProvider : dataPropertyMap
				.values()) {
			if (!tdbResource.hasProperty(lcaDataPropertyProvider
					.getTDBProperty())) {
				continue;
			}
			if (lcaDataPropertyProvider.isUnique()) {
				removeValues(lcaDataPropertyProvider.getPropertyName());
				Object value = tdbResource
						.getProperty(lcaDataPropertyProvider.getTDBProperty())
						.getLiteral().getValue();
				if (value
						.getClass()
						.equals(RDFUtil
								.getJavaClassFromRDFDatatype(lcaDataPropertyProvider
										.getRdfDatatype()))) {
					LCADataValue lcaDataValue = new LCADataValue();
					lcaDataValue
							.setLcaDataPropertyProvider(lcaDataPropertyProvider);
					lcaDataValue.setValue(value);
					lcaDataValues.add(lcaDataValue);
				}
			} else {
				StmtIterator stmtIterator = tdbResource
						.listProperties(lcaDataPropertyProvider
								.getTDBProperty());
				while (stmtIterator.hasNext()) {
					Object value = stmtIterator.next().getLiteral().getValue();
					if (value
							.getClass()
							.equals(RDFUtil
									.getJavaClassFromRDFDatatype(lcaDataPropertyProvider
											.getRdfDatatype()))) {
						LCADataValue lcaDataValue = new LCADataValue();
						lcaDataValue
								.setLcaDataPropertyProvider(lcaDataPropertyProvider);
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

	// public static final CSVColumnInfo[] getHeaderMenuObjects() {
	// CSVColumnInfo[] results = new CSVColumnInfo[2];
	//
	// results[0] = new CSVColumnInfo("Property (primary)");
	// results[0].setRequired(true);
	// results[0].setUnique(true);
	// results[0].setCheckLists(getPropertyNameCheckList());
	// results[0].setLeftJustified(true);
	// results[0].setRDFClass(rdfClass);
	// results[0].setTdbProperty(FedLCA.flowPropertyPrimaryDescription);
	// results[0].setRdfDatatype(XSDDatatype.XSDstring);
	//
	// results[1] = new CSVColumnInfo("Property (additional)");
	// results[1].setRequired(false);
	// results[1].setUnique(false);
	// results[1].setCheckLists(getPropertyNameCheckList());
	// results[1].setLeftJustified(true);
	// results[1].setRDFClass(rdfClass);
	// results[1].setTdbProperty(FedLCA.flowPropertySupplementalDescription);
	// results[1].setRdfDatatype(XSDDatatype.XSDstring);
	// return results;
	// }

	private static List<QACheck> getPropertyNameCheckList() {
		List<QACheck> qaChecks = QACheck.getGeneralQAChecks();

		// String d1 = "Non-allowed characters";
		// String e1 =
		// "Various characters are not considered acceptible in standard chemical names.";
		// String s1 = "Check your data";
		// Pattern p1 = Pattern.compile("^([^\"]+)[\"]([^\"]+)$");
		// String r1 = null;
		//
		// qaChecks.add(new QACheck(d1, e1, s1, p1, r1, false));
		return qaChecks;
	}

	// public String getPrimaryFlowProperty() {
	// return primaryFlowProperty;
	// }
	//
	// public void setPrimaryFlowProperty(String primaryFlowProperty) {
	// this.primaryFlowProperty = primaryFlowProperty;
	// RDFDatatype rdfDatatype = getHeaderMenuObjects()[0].getRdfDatatype();
	// ActiveTDB.tsReplaceLiteral(tdbResource,
	// FedLCA.flowPropertyPrimaryDescription, rdfDatatype, primaryFlowProperty);
	// }
	//
	// public List<String> getsupplementaryFlowProperties() {
	// return supplementaryFlowProperties;
	// }
	//
	// public void setSupplementaryFlowProperties(List<String>
	// supplementaryFlowProperties) {
	// ActiveTDB.tsRemoveAllObjects(tdbResource,
	// FedLCA.flowPropertySupplementalDescription);
	// this.supplementaryFlowProperties = supplementaryFlowProperties;
	// for (String supplementaryFlowProperty : supplementaryFlowProperties) {
	// ActiveTDB.tsAddLiteral(tdbResource,
	// FedLCA.flowPropertySupplementalDescription, supplementaryFlowProperty);
	// }
	// }

	// public void addSupplementaryFlowProperty(String
	// supplementaryFlowProperty) {
	// if (supplementaryFlowProperties == null) {
	// supplementaryFlowProperties = new ArrayList<String>();
	// }
	// supplementaryFlowProperties.add(supplementaryFlowProperty);
	// ActiveTDB.tsAddLiteral(tdbResource,
	// FedLCA.flowPropertySupplementalDescription, supplementaryFlowProperty);
	// }
	//
	// public void removeSupplementaryFlowProperty(String
	// supplementaryFlowProperty) {
	// this.supplementaryFlowProperties.remove(supplementaryFlowProperty);
	// Literal literalToRemove =
	// ActiveTDB.tsCreateTypedLiteral(supplementaryFlowProperty);
	// ActiveTDB.tsRemoveStatement(tdbResource,
	// FedLCA.flowPropertySupplementalDescription, literalToRemove);
	// }

	public Resource getTdbResource() {
		return tdbResource;
	}

	public void setTdbResource(Resource tdbResource) {
		// StmtIterator stmtIterator = this.tdbResource.listProperties();
		// while (stmtIterator.hasNext()){
		// Statement statement = stmtIterator.next();
		// ActiveTDB.tdbModel.remove(statement);
		// }
		// NEXT STATEMENT REPLACES ABOVE
		this.tdbResource.removeProperties();
		this.tdbResource = tdbResource;
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
	}
}
