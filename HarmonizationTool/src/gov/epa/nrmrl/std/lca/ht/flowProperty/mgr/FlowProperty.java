package gov.epa.nrmrl.std.lca.ht.flowProperty.mgr;

import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataPropertyProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataValue;
import gov.epa.nrmrl.std.lca.ht.dataModels.QACheck;
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.tdb.ImportRDFFileDirectlyToGraph;
import gov.epa.nrmrl.std.lca.ht.utils.RDFUtil;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class FlowProperty {
	// CLASS VARIABLES
	// public static final String flowPropertyPrimaryIdentifier = "Primary Info";
	public static final String flowPropertyUnit = "Unit";
	public static final String flowPropertyString = "Property";
	public static final String flowPropertyAdditionalIdentifier = "Additional Info";
	public static final Resource rdfClass = null;
//	public static final Resource rdfClass = FedLCA.FlowProperty;

	// NOTE: EVENTUALLY label AND comment SHOULD COME FROM ONTOLOGY
	public static final String label = "Flow Property";
//	public static final String comment = "The Flow Property is the characteristic used to measure the quanitity of the flowable.  Examples include 'volume', 'mass*time', and 'person transport'.  For a given Flow Property, only certain units are valid: e.g. 'm3' for 'volume', 'kg*hr' for 'mass*time', and 'people*km' for 'person transport'.";
	public static List<FlowUnit> lcaMasterUnits = new ArrayList<FlowUnit>();
	// public static List<FlowProperty> lcaMasterProperties = new ArrayList<FlowProperty>();
	private static Map<String, LCADataPropertyProvider> dataPropertyMap;
	// private static List<FlowProperty> lcaMasterProperties = new ArrayList<FlowProperty>();

	static {
		if (dataPropertyMap == null) {
			dataPropertyMap = new LinkedHashMap<String, LCADataPropertyProvider>();
			if (dataPropertyMap.isEmpty()) {

				dataPropertyMap = new LinkedHashMap<String, LCADataPropertyProvider>();
				LCADataPropertyProvider lcaDataPropertyProvider;

				lcaDataPropertyProvider = new LCADataPropertyProvider(flowPropertyUnit);
				lcaDataPropertyProvider.setRDFClass(rdfClass);
				lcaDataPropertyProvider.setPropertyClass(label);
				lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
				lcaDataPropertyProvider.setRequired(true);
				lcaDataPropertyProvider.setUnique(true);
				lcaDataPropertyProvider.setLeftJustified(true);
				lcaDataPropertyProvider.setCheckLists(getPropertyNameCheckList());
				lcaDataPropertyProvider.setTDBProperty(FedLCA.flowPropertyUnitString);
				dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);

				lcaDataPropertyProvider = new LCADataPropertyProvider(flowPropertyString);
				lcaDataPropertyProvider.setRDFClass(rdfClass);
				lcaDataPropertyProvider.setPropertyClass(label);
				lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
				lcaDataPropertyProvider.setRequired(false);
				lcaDataPropertyProvider.setUnique(true);
				lcaDataPropertyProvider.setLeftJustified(true);
				lcaDataPropertyProvider.setCheckLists(getPropertyNameCheckList());
				lcaDataPropertyProvider.setTDBProperty(FedLCA.flowPropertyString);
				dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);

				lcaDataPropertyProvider = new LCADataPropertyProvider(flowPropertyAdditionalIdentifier);
				lcaDataPropertyProvider.setRDFClass(rdfClass);
				lcaDataPropertyProvider.setPropertyClass(label);
				lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
				lcaDataPropertyProvider.setRequired(false);
				lcaDataPropertyProvider.setUnique(false);
				lcaDataPropertyProvider.setLeftJustified(true);
				lcaDataPropertyProvider.setCheckLists(getPropertyNameCheckList());
				lcaDataPropertyProvider.setTDBProperty(FedLCA.flowPropertySupplementalDescription);
				dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);
			}
		}
	}

	// INSTANCE VARIABLES
	private Resource tdbResource;
	private List<LCADataValue> lcaDataValues;
	private Resource matchingResource;
	private int firstRow;
//	private List<FlowUnit> flowUnits;
//	private FlowUnit userDataFlowUnit;
//	private FlowUnit referenceFlowUnit;
//	public String superGroup;

	// CONSTRUCTORS
	public FlowProperty() {
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
		lcaDataValues = new ArrayList<LCADataValue>();
		matchingResource = null;
	}

	// private static void addUnit(String unitGroup, String uuid, double conversionFactor, String description,
	// String name, String synonyms, String referenceUnit) {
	// FlowUnit lcaUnit = new FlowUnit();
	// lcaUnit.conversionFactor = conversionFactor;
	// lcaUnit.description = description;
	// lcaUnit.name = name;
	// lcaUnit.referenceUnit = referenceUnit;
	// lcaUnit.synonyms = synonyms;
	// lcaUnit.unit_group = unitGroup;
	// lcaUnit.uuid = uuid;
	// lcaUnit.tdbResource = null;
	// lcaMasterUnits.add(lcaUnit);
	// }

	// private static void addUnit(String unitGroup, String uuid, double conversionFactor, String description,
	// String name, String synonyms, String referenceUnit, Resource tdbResource) {
	// FlowUnit lcaUnit = new FlowUnit();
	// lcaUnit.conversionFactor = conversionFactor;
	// lcaUnit.description = description;
	// lcaUnit.name = name;
	// lcaUnit.referenceUnit = referenceUnit;
	// lcaUnit.synonyms = synonyms;
	// lcaUnit.unit_group = unitGroup;
	// lcaUnit.uuid = uuid;
	// lcaUnit.tdbResource = tdbResource;
	//
	// ActiveTDB.tsAddGeneralTriple(tdbResource, RDF.type, rdfClass, null);
	// ActiveTDB.tsAddGeneralTriple(tdbResource, DCTerms.description, description, null);
	// ActiveTDB.tsAddGeneralTriple(tdbResource, FedLCA.unitConversionFactor, lcaUnit.conversionFactor, null);
	// // ActiveTDB.tsAddGeneralTriple(tdbResource, OpenLCA.description, description, null);
	//
	// ActiveTDB.tsAddGeneralTriple(tdbResource, FedLCA.hasOpenLCAUUID, uuid, null);
	// lcaMasterUnits.add(lcaUnit);
	// }

	public FlowProperty(Resource tdbResource) {
		this.tdbResource = tdbResource;
		lcaDataValues = new ArrayList<LCADataValue>();
		clearSyncDataFromTDB();
	}

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
					Object value = stmtIterator.nextStatement().getLiteral().getValue();
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
		// --- BEGIN SAFE -READ- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		ResIterator resIterator = ActiveTDB.getModel(null).listSubjectsWithProperty(FedLCA.comparedSource, tdbResource);
		// ActiveTDB.tdbDataset.end();
		if (resIterator.hasNext()) {
			matchingResource = resIterator.next();
			ActiveTDB.tdbDataset.end();
			return;
		}
		ActiveTDB.tdbDataset.end();
	}

	public void clearSyncDataFromTDB() {
		lcaDataValues.clear();
		updateSyncDataFromTDB();
	}

	public static void loadMasterFlowUnits() {
		Logger runLogger = Logger.getLogger("run");
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model model = ActiveTDB.getModel(null);
		if (model.contains(FedLCA.FlowUnit, ECO.hasDataSource, FedLCA.UnitGroup)){
			System.out.println("Hey!");
		}
		ActiveTDB.tdbDataset.end();
		List<Resource> flowUnitResources = new ArrayList<Resource>();
		while (flowUnitResources.size() == 0) {
			runLogger.info("Creating Master Property list");
			//
			StringBuilder b = new StringBuilder();
			b.append(Prefixes.getPrefixesForQuery());
			b.append("select  ?mu where { \n");
			b.append("  ?mu a fedlca:FlowUnit . \n");
			b.append("  ?mu fedlca:displaySortIndex ?u_index . \n");
			b.append("  ?mu eco:hasDataSource ?ds . \n");
			b.append("  ?ds a lcaht:MasterDataset . \n");

			b.append("} \n");
			b.append("order by ?u_index  \n");

			String query = b.toString();
			System.out.println("Query = \n" + query);
			ActiveTDB.tdbDataset.begin(ReadWrite.READ);
			HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
			harmonyQuery2Impl.setQuery(query);
			harmonyQuery2Impl.setGraphName(null);

			ResultSet resultSet = harmonyQuery2Impl.getResultSet();

			while (resultSet.hasNext()) {
				QuerySolution querySolution = resultSet.next();
				Resource refUnit = querySolution.get("mu").asResource();
				flowUnitResources.add(refUnit);
			}
			ActiveTDB.tdbDataset.end();


			if (flowUnitResources.size() == 0) {
				String masterPropertiesFile = "classpath:/RDFResources/master_properties_v1.4a_lcaht.n3";
				runLogger.info("Need to load data: " + masterPropertiesFile);
				ImportRDFFileDirectlyToGraph.loadToDefaultGraph(masterPropertiesFile, null);
				DataSourceKeeper.syncFromTDB();
			}
		}
		
//		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
//		model = ActiveTDB.getModel(null);
//		if (model.contains(FedLCA.FlowUnit, ECO.hasDataSource, FedLCA.UnitGroup)){
//			System.out.println("Hey!");
//		}
//		ActiveTDB.tdbDataset.end();
		
		for (Resource resource : flowUnitResources) {
			FlowUnit unitToAdd = new FlowUnit(resource);
			lcaMasterUnits.add(unitToAdd);
		}
	}

	// public static void loadMasterFlowProperties() {
	// Logger runLogger = Logger.getLogger("run");
	// while (lcaMasterProperties.size() == 0) {
	// runLogger.info("Creating Master Property list");
	// //
	// StringBuilder b = new StringBuilder();
	// b.append(Prefixes.getPrefixesForQuery());
	// b.append("select  ?mu ?mug ?ug_name ?u_name ?ug_index ?ug_uuid ?u_uuid ?desc ?altLabel ?convFactor ?ru ?specialClassName where { \n");
	// b.append("  ?mug a fedlca:UnitGroup . \n");
	// b.append("  ?mug fedlca:hasUnit ?mu . \n");
	// b.append("  ?mug fedlca:displaySortIndex ?ug_index . \n");
	// b.append("  ?mug rdfs:label ?ug_name . \n");
	// b.append("  ?mug fedlca:hasReferenceUnit ?ru . \n");
	// b.append("  ?mug fedlca:hasOpenLCAUUID ?ug_uuid . \n");
	// // b.append("  ?ru fedlca:hasOpenLCAUUID ?ru_uuid . \n");
	// b.append("  ?mu a fedlca:FlowUnit . \n");
	// b.append("  ?mu dcterms:description ?desc . \n");
	// b.append("  ?mu rdfs:label ?u_name . \n");
	// b.append("  ?mu fedlca:hasOpenLCAUUID ?u_uuid . \n");
	// b.append("  ?mu fedlca:displaySortIndex ?u_index . \n");
	// b.append("  optional {?mug a ?specialClass . \n");
	// b.append("            ?specialClass rdfs:label ?specialClassName . \n");
	// b.append("            filter ((?specialClass != fedlca:UnitGroup ) && (?specialClass != owl:NamedIndividual )) \n");
	// b.append("  } \n");
	// b.append("  optional {?mu fedlca:unitConversionFactor ?convFactor } \n");
	// b.append("  optional {?mu skos:altLabel  ?altLabel } \n");
	// b.append("} \n");
	// b.append("order by ?ug_index ?u_index  \n");
	//
	// String query = b.toString();
	// System.out.println("Query = \n" + query);
	// HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
	// harmonyQuery2Impl.setQuery(query);
	// harmonyQuery2Impl.setGraphName(null);
	//
	// ResultSet resultSet = harmonyQuery2Impl.getResultSet();
	// // // List<Resource> itemsToAddToDatasource = new ArrayList<Resource>();
	// // Map<Resource, FlowContext> masterMapTemp = new HashMap<Resource, FlowContext>();
	//
	// FlowProperty previousUnitGroup = null;
	// FlowUnit previousFlowUnit = null;
	// FlowProperty currentUnitGroup = null;
	// FlowUnit currentFlowUnit = null;
	//
	// while (resultSet.hasNext()) {
	// QuerySolution querySolution = resultSet.next();
	//
	// Resource masterUnitGroupResource = querySolution.get("mug").asResource();
	// String unitGroupName = querySolution.get("ug_name").asLiteral().getString();
	// int unitGroupIndex = querySolution.get("ug_index").asLiteral().getInt();
	// String unitGroupUUID = querySolution.get("ug_uuid").asLiteral().getString();
	// RDFNode specialClassCandidate = querySolution.get("specialClassName");
	// String specialClassLabel = "(other)";
	// if (specialClassCandidate != null) {
	// specialClassLabel = specialClassCandidate.asLiteral().getString();
	// }
	// Resource refUnit = querySolution.get("ru").asResource();
	//
	// Resource masterFlowUnitResource = querySolution.get("mu").asResource();
	// String unitName = querySolution.get("u_name").asLiteral().getString();
	// String unitUUID = querySolution.get("u_uuid").asLiteral().getString();
	// String unitDescription = querySolution.get("desc").asLiteral().getString();
	// RDFNode altLabelCandidate = querySolution.get("altLabel");
	// String unitAltLabel = null;
	// if (altLabelCandidate != null) {
	// unitAltLabel = altLabelCandidate.asLiteral().getString();
	// }
	// RDFNode conversionFactorCandidate = querySolution.get("convFactor");
	// double unitConversionFactor = 0;
	// if (conversionFactorCandidate != null) {
	// unitConversionFactor = conversionFactorCandidate.asLiteral().getDouble();
	// }
	//
	// if (previousFlowUnit == null) {
	// currentFlowUnit = new FlowUnit();
	// } else {
	// if (previousFlowUnit.getTdbResource().equals(masterFlowUnitResource)) {
	// currentFlowUnit = previousFlowUnit;
	// } else {
	// currentFlowUnit = new FlowUnit();
	// }
	// }
	// currentFlowUnit.setTdbResource(masterFlowUnitResource);
	//
	// currentFlowUnit.conversionFactor = unitConversionFactor;
	// currentFlowUnit.description = unitDescription;
	// currentFlowUnit.name = unitName;
	// currentFlowUnit.referenceUnit = refUnit;
	// currentFlowUnit.unit_group = masterUnitGroupResource;
	// currentFlowUnit.uuid = unitUUID;
	// if (unitAltLabel != null) {
	// currentFlowUnit.synonyms.add(unitAltLabel);
	// }
	//
	// if (previousUnitGroup == null) {
	// currentUnitGroup = new FlowProperty();
	// lcaMasterProperties.add(currentUnitGroup);
	// } else {
	// if (previousUnitGroup.tdbResource.equals(masterUnitGroupResource)) {
	// currentUnitGroup = previousUnitGroup;
	// } else {
	// currentUnitGroup = new FlowProperty();
	// lcaMasterProperties.add(currentUnitGroup);
	// }
	// }
	// previousUnitGroup = currentUnitGroup;
	//
	// currentUnitGroup.setTdbResource(masterUnitGroupResource);
	// // currentUnitGroup.setProperty("Property", unitGroupName);
	// // currentUnitGroup.setProperty("UUID", unitGroupUUID);
	// // currentUnitGroup.setProperty("index", unitGroupIndex);
	// currentUnitGroup.superGroup = specialClassLabel;
	// currentUnitGroup.addFlowUnit(currentFlowUnit);
	// // lcaMasterProperties.add(currentUnitGroup);
	// previousUnitGroup = currentUnitGroup;
	// previousFlowUnit = currentFlowUnit;
	// }
	//
	// if (lcaMasterProperties.size() == 0) {
	// String masterPropertiesFile = "classpath:/RDFResources/master_properties_v1.4a_lcaht.n3";
	// runLogger.info("Need to load data: " + masterPropertiesFile);
	// ImportRDFFileDirectlyToGraph.loadToDefaultGraph(masterPropertiesFile, null);
	// DataSourceKeeper.syncFromTDB();
	// }
	// }
	// }

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
		// Statement statement = stmtIterator.nextStatement();
		// ActiveTDB.tdbModel.remove(statement);
		// }
		// NEXT STATEMENT REPLACES ABOVE
		// this.tdbResource.removeProperties();
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
		if (matchingResource == null) {
			ActiveTDB.tsRemoveAllLikeObjects(matchingResource, OWL.sameAs, null, null);
			this.matchingResource = null;
			return;
		}
		this.matchingResource = matchingResource;
		ActiveTDB.tsReplaceResourceSameType(tdbResource, OWL.sameAs, matchingResource, null);
	}

	public int getFirstRow() {
		return firstRow;
	}

	public void setFirstRow(int firstRow) {
		this.firstRow = firstRow;
	}

	public String getDataSource() {
		return "Master List";
	}

//	public boolean setMatches() {
//		String unitStr = (String) getOneProperty(flowUnitString);
//
//		if (unitStr == null) {
//			return false;
//		}
//		for (FlowUnit flowUnit : lcaMasterUnits) {
//			if (flowUnit.name.equals(unitStr)) {
//				setMatchingResource(flowUnit.getTdbResource());
//				return true;
//			}
//		}
//		return false;
//	}

	public String getUnitStr() {
		return (String) getOneProperty(flowPropertyUnit);
	}

	public String getPropertyStr() {
		return (String) getOneProperty(flowPropertyString);
	}

//	public List<FlowUnit> getflowUnits() {
//		if (flowUnits == null) {
//			flowUnits = new ArrayList<FlowUnit>();
//		}
//		return flowUnits;
//	}
//
//	public void setflowUnits(List<FlowUnit> flowUnits) {
//		this.flowUnits = flowUnits;
//	}
//
//	public void addFlowUnit(FlowUnit flowUnit) {
//		if (flowUnits == null) {
//			flowUnits = new ArrayList<FlowUnit>();
//		}
//		flowUnits.add(flowUnit);
//	}
//
//	public FlowUnit getUserDataFlowUnit() {
//		return userDataFlowUnit;
//	}
//
//	public void setUserDataFlowUnit(FlowUnit userDataFlowUnit) {
//		this.userDataFlowUnit = userDataFlowUnit;
//	}
//
//	public FlowUnit getReferenceFlowUnit() {
//		return referenceFlowUnit;
//	}
//
//	public void setReferenceFlowUnit(FlowUnit referenceFlowUnit) {
//		this.referenceFlowUnit = referenceFlowUnit;
//	}

	// public static List<FlowProperty> getLcaMasterProperties() {
	// return lcaMasterProperties;
	// }
	//
	// public static void setLcaMasterProperties(List<FlowProperty> lcaMasterProperties) {
	// FlowProperty.lcaMasterProperties = lcaMasterProperties;
	// }
}
