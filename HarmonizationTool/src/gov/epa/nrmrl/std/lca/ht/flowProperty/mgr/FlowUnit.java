package gov.epa.nrmrl.std.lca.ht.flowProperty.mgr;

import gov.epa.nrmrl.std.lca.ht.dataCuration.ComparisonKeeper;
import gov.epa.nrmrl.std.lca.ht.dataCuration.ComparisonProvider;
import gov.epa.nrmrl.std.lca.ht.dataFormatCheck.FormatCheck;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataPropertyProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataValue;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.RDFUtil;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;
import gov.epa.nrmrl.std.lca.ht.vocabulary.SKOS;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class FlowUnit {
	private static Map<String, LCADataPropertyProvider> dataPropertyMap;
	public static final Resource rdfClass = FedLCA.FlowUnit;
	public static final String label = "Flow Unit";
	public static final String flowUnitString = "Unit";
	public static final String flowPropertyUnitDescription = "Description";

	public static final String flowPropertyString = "Property";
	public static final String openLCAUUID = "UUID";
	public static final String conversionFactor = "Conversion Factor";

	// - name
	// - description
	// synonyms

	// - uuid
	// referenceUnit
	// - conversionFactor
	// displayOrder
	// superGroup
	// unitGroup (== FlowProperty)

	static {
		if (dataPropertyMap == null) {
			dataPropertyMap = new LinkedHashMap<String, LCADataPropertyProvider>();
			if (dataPropertyMap.isEmpty()) {

				dataPropertyMap = new LinkedHashMap<String, LCADataPropertyProvider>();
				LCADataPropertyProvider lcaDataPropertyProvider;

				lcaDataPropertyProvider = new LCADataPropertyProvider(flowUnitString);
				lcaDataPropertyProvider.setRDFClass(rdfClass);
				lcaDataPropertyProvider.setPropertyClass(label);
				lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
				lcaDataPropertyProvider.setRequired(true);
				lcaDataPropertyProvider.setUnique(true);
				lcaDataPropertyProvider.setCheckLists(getPropertyNameCheckList());
				lcaDataPropertyProvider.setTDBProperty(RDFS.label);
				dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);

				lcaDataPropertyProvider = new LCADataPropertyProvider(flowPropertyString);
				lcaDataPropertyProvider.setRDFClass(rdfClass);
				lcaDataPropertyProvider.setPropertyClass(label);
				lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
				lcaDataPropertyProvider.setRequired(false);
				lcaDataPropertyProvider.setUnique(true);
				lcaDataPropertyProvider.setCheckLists(getPropertyNameCheckList());
				lcaDataPropertyProvider.setTDBProperty(FedLCA.flowPropertyString);
				dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);

				lcaDataPropertyProvider = new LCADataPropertyProvider(flowPropertyUnitDescription);
				lcaDataPropertyProvider.setRDFClass(rdfClass);
				lcaDataPropertyProvider.setPropertyClass(label);
				lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
				lcaDataPropertyProvider.setRequired(false);
				lcaDataPropertyProvider.setUnique(true);
				lcaDataPropertyProvider.setCheckLists(getPropertyNameCheckList());
				lcaDataPropertyProvider.setTDBProperty(DCTerms.description);
				lcaDataPropertyProvider.setIncludeInList(false);
				dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);

				// lcaDataPropertyProvider = new LCADataPropertyProvider(openLCAUUID);
				// lcaDataPropertyProvider.setRDFClass(rdfClass);
				// lcaDataPropertyProvider.setPropertyClass(label);
				// lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
				// lcaDataPropertyProvider.setRequired(false);
				// lcaDataPropertyProvider.setUnique(true);
				// lcaDataPropertyProvider.setCheckLists(FormatCheck.getUUIDCheck());
				// lcaDataPropertyProvider.setTDBProperty(FedLCA.hasOpenLCAUUID);
				// dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);

				lcaDataPropertyProvider = new LCADataPropertyProvider(conversionFactor);
				lcaDataPropertyProvider.setRDFClass(rdfClass);
				lcaDataPropertyProvider.setPropertyClass(label);
				lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDdouble);
				lcaDataPropertyProvider.setRequired(false);
				lcaDataPropertyProvider.setUnique(true);
				lcaDataPropertyProvider.setLeftJustified(false);
				lcaDataPropertyProvider.setCheckLists(FormatCheck.getFloatCheck());
				lcaDataPropertyProvider.setTDBProperty(FedLCA.unitConversionFactor);
				dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);
				LCADataPropertyProvider.registerProviders(dataPropertyMap);
			}
		}
	}

	// public String name;
	// public String description;
	// public List<String> synonyms = new ArrayList<String>();
	// public String uuid;
	// public String superGroup;
	// public Resource unitGroup;
	// public String superGroupName = "(other)";
	// public Resource referenceUnit;
	private Resource tdbResource;
	// public double conversionFactor;
	// public FlowProperty flowProperty;
	private Resource matchingResource;
	private Resource referenceFlowUnit;
	private Resource unitGroup;

	// private int displayOrder = -1;
	private int firstRow = -1;
	private List<LCADataValue> lcaDataValues;

	public FlowUnit() {
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
		lcaDataValues = new ArrayList<LCADataValue>();
		matchingResource = null;
	}

	public FlowUnit(Resource res) {
		this(res, true);
	}

	public FlowUnit(Resource resource, boolean sync) {
		this.tdbResource = resource;
		lcaDataValues = new ArrayList<LCADataValue>();
		if (sync) {
			clearSyncDataFromTDB();
			// updateSyncFromTDB();
		}
	}

	// public void updateSyncFromTDB() {
	// if (tdbResource == null) {
	// return;
	// }
	// this.name = tdbResource.getProperty(RDFS.label).getObject().asLiteral().getString();
	// this.description = tdbResource.getProperty(DCTerms.description).getObject().asLiteral().getString();
	// synonyms = new ArrayList<String>();
	// StmtIterator stmtIterator = tdbResource.listProperties(SKOS.altLabel);
	// while (stmtIterator.hasNext()) {
	// Statement statement = stmtIterator.next();
	// synonyms.add(statement.getObject().asLiteral().getString());
	// }
	// this.uuid = tdbResource.getProperty(FedLCA.hasOpenLCAUUID).getObject().asLiteral().getString();
	//
	// /* START OF TRANSACTION */
	// // ActiveTDB.tdbDataset.begin(ReadWrite.READ);
	//
	// Model tdbModel = ActiveTDB.getModel(null);
	// ResIterator resIterator = tdbModel.listSubjectsWithProperty(FedLCA.hasFlowUnit, tdbResource);
	// if (resIterator.hasNext()) {
	// this.unitGroup = resIterator.next();
	// }
	// stmtIterator = this.unitGroup.listProperties(RDF.type);
	// while (stmtIterator.hasNext()) {
	// Statement statement = stmtIterator.next();
	// Resource unitGroupClass = statement.getObject().asResource();
	// if ((!unitGroupClass.equals(LCAHT.MasterDataset)) && (!unitGroupClass.equals(FedLCA.UnitGroup))
	// && (!unitGroupClass.equals(OWL2.NamedIndividual))) {
	// this.superGroupName = unitGroupClass.getProperty(RDFS.label).getObject().asLiteral().getString();
	// continue;
	// }
	// }
	//
	// // ActiveTDB.tdbDataset.end();
	// /* END OF TRANSACTION */
	// this.conversionFactor = tdbResource.getProperty(FedLCA.unitConversionFactor).getObject().asLiteral()
	// .getDouble();
	// this.displayOrder = tdbResource.getProperty(FedLCA.displaySortIndex).getObject().asLiteral().getInt();
	// }

	public void clearSyncDataFromTDB() {
		lcaDataValues.clear();
		updateSyncDataFromTDB();
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
		// newLCADataValue.setValueAsString(object.toString()); // SHOULD WE DO
		// THIS AT ALL?

		if (lcaDataPropertyProvider.isUnique()) {
			removeValues(lcaDataPropertyProvider.getPropertyName());
			ActiveTDB.tsReplaceLiteral(tdbResource, lcaDataPropertyProvider.getTDBProperty(), object);
		} else {
			ActiveTDB.tsAddGeneralTriple(tdbResource, lcaDataPropertyProvider.getTDBProperty(), object, null);
		}
		lcaDataValues.add(newLCADataValue);
	}

	public void updateSyncDataFromTDB() {
		if (tdbResource == null) {
			return;
		}
		// --- BEGIN SAFE -READ- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.getModel(null);

		// LCADataPropertyProvider LIST IS ALL LITERALS
		for (LCADataPropertyProvider lcaDataPropertyProvider : dataPropertyMap.values()) {
			Property property = lcaDataPropertyProvider.getTDBProperty();
			Selector selector = new SimpleSelector(tdbResource, property, null, null);
			StmtIterator stmtIterator = tdbModel.listStatements(selector);
			int objectCount = 0;
			while (stmtIterator.hasNext()) {
				objectCount++;
				Statement statement = stmtIterator.next();
				Object value = statement.getObject().asLiteral().getValue();
				if (!value.getClass().equals(
						RDFUtil.getJavaClassFromRDFDatatype(lcaDataPropertyProvider.getRdfDatatype()))) {
					continue;
				}
				if (lcaDataPropertyProvider.isUnique()) {
					if (objectCount == 1) {
						removeValues(lcaDataPropertyProvider.getPropertyName());
						LCADataValue lcaDataValue = new LCADataValue();
						lcaDataValue.setLcaDataPropertyProvider(lcaDataPropertyProvider);
						lcaDataValue.setValue(value);
						lcaDataValues.add(lcaDataValue);
					}
				} else {
					LCADataValue lcaDataValue = new LCADataValue();
					lcaDataValue.setLcaDataPropertyProvider(lcaDataPropertyProvider);
					lcaDataValue.setValue(value);
					lcaDataValues.add(lcaDataValue);
				}
			}
		}

		// Now get the matchingResource
		Selector selector = new SimpleSelector(null, FedLCA.comparedSource, tdbResource);
		StmtIterator stmtIterator = tdbModel.listStatements(selector);
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
			Resource comparison = statement.getSubject();
			if (!tdbModel.contains(comparison, FedLCA.comparedEquivalence, FedLCA.Equivalent)) {
				continue;
			}
			Selector selector1 = new SimpleSelector(comparison, FedLCA.comparedMaster, null, null);
			StmtIterator stmtIterator1 = tdbModel.listStatements(selector1);
			while (stmtIterator1.hasNext()) {
				Statement statement1 = stmtIterator1.next();
				matchingResource = statement1.getObject().asResource();
			}
		}

		Selector selector1 = new SimpleSelector(null, FedLCA.hasFlowUnit, tdbResource);
		StmtIterator stmtIterator1 = tdbModel.listStatements(selector1);
		while (stmtIterator1.hasNext()) {
			Statement statement1 = stmtIterator1.next();
			unitGroup = statement1.getSubject();
		}
		if (unitGroup != null) {
			Selector selector2 = new SimpleSelector(unitGroup, FedLCA.hasReferenceUnit, null, null);
			StmtIterator stmtIterator2 = tdbModel.listStatements(selector2);
			while (stmtIterator2.hasNext()) {
				Statement statement2 = stmtIterator2.next();
				referenceFlowUnit = statement2.getObject().asResource();
			}
		}
		ActiveTDB.tdbDataset.end();
		return;
	}

	private void removeValues(String key) {
		for (LCADataValue lcaDataValue : lcaDataValues) {
			if (lcaDataValue.getLcaDataPropertyProvider().getPropertyName().equals(key)) {
				lcaDataValues.remove(lcaDataValue);
			}
		}
	}

	private static List<FormatCheck> getPropertyNameCheckList() {
		List<FormatCheck> qaChecks = FormatCheck.getGeneralQAChecks();
		return qaChecks;
	}

	public void setMatchingResource(Resource matchingResource) {
		this.matchingResource = matchingResource;
		// First line here cleans up old matching assignments
		ActiveTDB.tsRemoveAllLikeObjects(tdbResource, OWL.sameAs, null, null);

		Resource comparisonResource = ComparisonProvider.findComparisonResource(tdbResource, matchingResource);
		if (matchingResource == null && comparisonResource == null) {
			return;
		}
		if (comparisonResource == null) {
			new ComparisonProvider(tdbResource, matchingResource, FedLCA.Equivalent);
			ComparisonKeeper.commitUncommittedComparisons("Set in setMatchingResource");
			return;
		}
		if (matchingResource == null) {
			ActiveTDB.tsRemoveAllLikeObjects(comparisonResource, null, null, null);
			return;
		}
		ComparisonProvider comparisonProvider = new ComparisonProvider(comparisonResource);
		comparisonProvider.updateNow();
		comparisonProvider.setMasterDataObject(matchingResource);
	}

	public Resource getMatchingResource() {
		return matchingResource;
	}

	public boolean setMatches() {
		String unitStr = (String) getOneProperty(flowUnitString);

		if (unitStr == null) {
			return false;
		}
		for (FlowUnit masterFlowUnit : FlowProperty.lcaMasterUnits) {
			if (masterFlowUnit.getOneProperty(flowUnitString).equals(unitStr)) {
				setMatchingResource(masterFlowUnit.getTdbResource());
				return true;
			}
		}
		return false;
	}

	public Object getOneProperty(String key) {
		for (LCADataValue lcaDataValue : lcaDataValues) {
			if (lcaDataValue.getLcaDataPropertyProvider().getPropertyName().equals(key)) {
				return lcaDataValue.getValue();
			}
		}
		// // If you didn't find anything, try syncing...
		// clearSyncDataFromTDB();
		// for (LCADataValue lcaDataValue : lcaDataValues) {
		// if (lcaDataValue.getLcaDataPropertyProvider().getPropertyName().equals(key)) {
		// return lcaDataValue.getValue();
		// }
		// }
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

	// public String getUnitGroupName() {
	// return unitGroup.listProperties(RDFS.label).toList().get(0).getString();
	// }
	//
	// public String getUnitGroupUUID() {
	// return unitGroup.listProperties(FedLCA.hasOpenLCAUUID).toList().get(0).getString();
	// }
	//
	// public String getReferenceUnitName() {
	// return referenceUnit.listProperties(RDFS.label).toList().get(0).getString();
	// }

	public Resource getTdbResource() {
		return tdbResource;
	}

	public void setTdbResource(Resource tdbResource) {
		this.tdbResource = tdbResource;
	}

	// public int getDisplayOrder() {
	// return displayOrder;
	// }
	//
	// public void setDisplayOrder(int displayOrder) {
	// this.displayOrder = displayOrder;
	// }

	public int getFirstRow() {
		return firstRow;
	}

	public void setFirstRow(int firstRow) {
		this.firstRow = firstRow;
	}

	public static Map<String, LCADataPropertyProvider> getDataPropertyMap() {
		return dataPropertyMap;
	}

	public Resource getReferenceFlowUnit() {
		return referenceFlowUnit;
	}

	public void setReferenceFlowUnit(Resource referenceFlowUnit) {
		this.referenceFlowUnit = referenceFlowUnit;
	}

	public Resource getUnitGroup() {
		return unitGroup;
	}

	public void setUnitGroup(Resource unitGroup) {
		this.unitGroup = unitGroup;
		setProperty(flowPropertyString, unitGroup.getPropertyResourceValue(RDFS.label));
	}

	public String getUnitSuperGroup() {
		if (unitGroup == null) {
			return null;
		}
		StmtIterator stmtIterator = unitGroup.listProperties(RDF.type);
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
			Resource classFound = statement.getObject().asResource();
			if ((!classFound.equals(LCAHT.MasterDataset)) && (!classFound.equals(FedLCA.UnitGroup))
					&& (!classFound.equals(OWL2.NamedIndividual))) {
				return classFound.getProperty(RDFS.label).getString();
			}
		}
		return "(other)";
	}
}
