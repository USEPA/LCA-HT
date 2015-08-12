package gov.epa.nrmrl.std.lca.ht.flowProperty.mgr;

import gov.epa.nrmrl.std.lca.ht.dataFormatCheck.FormatCheck;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataPropertyProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataValue;
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.tdb.ImportRDFFileDirectlyToGraph;
import gov.epa.nrmrl.std.lca.ht.utils.RDFUtil;
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
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * The FlowProperty class handles all Flow Properties and Flow Property Units
 * 
 * 
 * @author cfowler
 *
 */
public class FlowProperty {
	// CLASS VARIABLES
	// public static final String flowPropertyPrimaryIdentifier = "Primary Info";
	public static final String flowPropertyUnit = "Unit";
	public static final String flowPropertyString = "Property";
	public static final String flowPropertyAdditionalIdentifier = "Additional Info";
	public static final Resource rdfClass = null;
	// public static final Resource rdfClass = FedLCA.FlowProperty;

	// NOTE: EVENTUALLY label AND comment SHOULD COME FROM ONTOLOGY
	public static final String label = "Flow Property";
	// public static final String comment =
	// "The Flow Property is the characteristic used to measure the quanitity of the flowable.  Examples include 'volume', 'mass*time', and 'person transport'.  For a given Flow Property, only certain units are valid: e.g. 'm3' for 'volume', 'kg*hr' for 'mass*time', and 'people*km' for 'person transport'.";
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

//				lcaDataPropertyProvider = new LCADataPropertyProvider(flowPropertyUnit);
//				lcaDataPropertyProvider.setRDFClass(rdfClass);
//				lcaDataPropertyProvider.setPropertyClass(label);
//				lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
//				lcaDataPropertyProvider.setRequired(true);
//				lcaDataPropertyProvider.setUnique(true);
//				lcaDataPropertyProvider.setLeftJustified(true);
//				lcaDataPropertyProvider.setCheckLists(getPropertyNameCheckList());
//				lcaDataPropertyProvider.setTDBProperty(FedLCA.flowPropertyUnitString);
//				dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);
//
//				lcaDataPropertyProvider = new LCADataPropertyProvider(flowPropertyString);
//				lcaDataPropertyProvider.setRDFClass(rdfClass);
//				lcaDataPropertyProvider.setPropertyClass(label);
//				lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
//				lcaDataPropertyProvider.setRequired(false);
//				lcaDataPropertyProvider.setUnique(true);
//				lcaDataPropertyProvider.setLeftJustified(true);
//				lcaDataPropertyProvider.setCheckLists(getPropertyNameCheckList());
//				lcaDataPropertyProvider.setTDBProperty(FedLCA.flowPropertyString);
//				dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);
//
//				lcaDataPropertyProvider = new LCADataPropertyProvider(flowPropertyAdditionalIdentifier);
//				lcaDataPropertyProvider.setRDFClass(rdfClass);
//				lcaDataPropertyProvider.setPropertyClass(label);
//				lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
//				lcaDataPropertyProvider.setRequired(false);
//				lcaDataPropertyProvider.setUnique(false);
//				lcaDataPropertyProvider.setLeftJustified(true);
//				lcaDataPropertyProvider.setCheckLists(getPropertyNameCheckList());
//				lcaDataPropertyProvider.setTDBProperty(FedLCA.flowPropertySupplementalDescription);
//				dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);
//				LCADataPropertyProvider.registerProviders(dataPropertyMap);
			}
		}
	}

	// INSTANCE VARIABLES
	private Resource tdbResource;
	private List<LCADataValue> lcaDataValues;
	private Resource matchingResource;
	private int firstRow;

	// private List<FlowUnit> flowUnits;
	// private FlowUnit userDataFlowUnit;
	// private FlowUnit referenceFlowUnit;
	// public String superGroup;

	// CONSTRUCTORS
	public FlowProperty() {
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
		lcaDataValues = new ArrayList<LCADataValue>();
		matchingResource = null;
	}

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

	public static void reLoadMasterFlowUnits() {
		Logger runLogger = Logger.getLogger("run");
		if (ActiveTDB.getMasterFlowPropertyDatasetResources() == null){
			String masterPropertiesFile = "classpath:/RDFResources/master_properties_v1.4a_lcaht.n3";
			runLogger.info("Need to load data: " + masterPropertiesFile);
			ImportRDFFileDirectlyToGraph.loadToDefaultGraph(masterPropertiesFile, null);
			DataSourceKeeper.syncFromTDB();
		}
		if (lcaMasterUnits.size() > 0){
			lcaMasterUnits.clear();
		}

		List<Resource> flowUnitResources = new ArrayList<Resource>();
		while (flowUnitResources.size() == 0) {
			runLogger.info("Creating Master Property list");
			//
			StringBuilder b = new StringBuilder();
			b.append(Prefixes.getPrefixesForQuery());
			b.append("select  distinct ?mu where { \n");
			b.append("  ?mug a fedlca:UnitGroup . \n");
			b.append("  ?mug fedlca:displaySortIndex ?ug_index . \n");
			b.append("  ?mug eco:hasDataSource ?ds . \n");
			b.append("  ?mug fedlca:hasFlowUnit ?mu . \n");
			b.append("  ?mu a fedlca:FlowUnit . \n");
			b.append("  ?mu fedlca:displaySortIndex ?u_index . \n");
			b.append("  filter(?ug_index > -1) \n");
			b.append("  filter(?u_index > -1) \n");
			b.append("  ?mu eco:hasDataSource ?ds . \n");
			b.append("  ?ds a lcaht:MasterDataset . \n");
			b.append("} \n");
			b.append("order by ?ug_index ?u_index\n");

			String query = b.toString();
//			System.out.println("Query = \n" + query);
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
		}

		for (Resource resource : flowUnitResources) {
			FlowUnit unitToAdd = new FlowUnit(resource);
			lcaMasterUnits.add(unitToAdd);
		}
	}
	
	private static List<FormatCheck> getPropertyNameCheckList() {
		List<FormatCheck> qaChecks = FormatCheck.getGeneralQAChecks();
		return qaChecks;
	}

	public Resource getTdbResource() {
		return tdbResource;
	}

	public void setTdbResource(Resource tdbResource) {
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

	// public String getDataSource() {
	// return "Master List";
	// }

	public String getUnitStr() {
		return (String) getOneProperty(flowPropertyUnit);
	}

	public String getPropertyStr() {
		return (String) getOneProperty(flowPropertyString);
	}
}
