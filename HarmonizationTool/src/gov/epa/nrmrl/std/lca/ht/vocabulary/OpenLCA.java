package gov.epa.nrmrl.std.lca.ht.vocabulary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.FlowContext;
import gov.epa.nrmrl.std.lca.ht.sparql.GenericUpdate;
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Vocabulary definitions from being developed by the Environmental Modeling and Visualization Lab (EMVL) Supporting
 * work for PIs Troy Hawkins and Wes Ingwersen, US EPA
 * 
 * @author Tom Transue
 */

public class OpenLCA {
	/**
	 * <p>
	 * The RDF tdbModel that holds the vocabulary terms
	 * </p>
	 */
	private static Model m_model = ActiveTDB.getModel(null);

	/**
	 * <p>
	 * The namespace of the vocabulary as a string
	 * </p>
	 */
	public static final String NS = "http://openlca.org/schema/v1.0/";

	/**
	 * <p>
	 * The namespace of the vocabulary as a string
	 * </p>
	 * 
	 * @see #NS
	 */
	public static String getURI() {
		return NS;
	}

	/**
	 * <p>
	 * The namespace of the vocabulary as a resource
	 * </p>
	 */
	public static final Resource NAMESPACE = m_model.createResource(NS);

	/** The most generic type of the openLCA model. */
	public static final Resource Entity = m_model.createResource(NS + "Entity");

	/** The super-class of all enumeration types. */
	public static final Resource Enumeration = m_model.createResource(NS + "Enumeration");

	/** A root entity which can have a category. */
	public static final Resource CategorizedEntity = m_model.createResource(NS + "CategorizedEntity");

	/** Everything that can be an input or output of a process (e.g. a substance, a product, a waste, a service * etc.) */
	public static final Resource Flow = m_model.createResource(NS + "Flow");

	/**
	 * flowType - The type of the flow. Note that this type is more a descriptor of how the flow is handled in
	 * calculations.
	 */
	public static final Property flowType = m_model.createProperty(NS + "flowType");

	/** FlowType - The basic flow types. The object of the flowType Property */
	public static final Resource FlowType = m_model.createResource(NS + "FlowType");

	/** Three types of FlowType: ELEMENTARY_FLOW, PRODUCT_FLOW, and WASTE_FLOW */
	public static final Resource ELEMENTARY_FLOW = m_model.createResource(NS + "ELEMENTARY_FLOW");
	public static final Resource PRODUCT_FLOW = m_model.createResource(NS + "PRODUCT_FLOW");
	public static final Resource WASTE_FLOW = m_model.createResource(NS + "WASTE_FLOW");

	/** A CAS number of the flow. */
	public static final Property cas = m_model.createProperty(NS + "cas");

	/** A chemical formula of the flow. */
	public static final Property formula = m_model.createProperty(NS + "formula");

	/**
	 * Conversion factors between flow properties that can be used to express amounts of the flow. Deprecated as of
	 * 2015-02-12
	 */
	public static final Property flowPropertyFactors = m_model.createProperty(NS + "flowPropertyFactors");

	/** Conversion factors between flow properties that can be used to express amounts of the flow. */
	public static final Property flowProperties = m_model.createProperty(NS + "flowProperties");

	/** 
	 * A FlowPropertyFactor is a conversion factor between flow properties (quantities) of a flow. As an example the
	 * amount of the flow 'water' in a process could be expressed in 'kg' mass or 'm3' volume. In this case the flow
	 * water would have two flow property factors: one for the flow property 'mass' and one for 'volume'. Each of these
	 * flow properties has a reference to a unit group which again has a reference unit. In the example the flow
	 * property 'mass' could reference the unit group 'units of mass' with 'kg' as reference unit and volume could
	 * reference the unit group 'units of volume' with 'm3' as reference unit. The flow property factor is now the
	 * conversion factor between these two reference units where the factor of the reference flow property of the flow
	 * is 1. If the reference flow property of 'water' in the example would be 'mass' the respective flow property
	 * factor would be 1 and the factor for 'volume' would be 0.001 (as 1 kg water is 0.001 m3). The amount of water in
	 * a process can now be also given in liter, tons, grams etc. For this, the unit conversion factor of the respective
	 * unit group can be used to convert into the reference unit (which then can be used to convert to the reference
	 * unit of another flow property). Another thing to note is that different flow properties can refer to the same
	 * unit group (e.g. MJ upper calorific value and MJ lower calorific value.)
	 */
	public static final Resource FlowPropertyFactor = m_model.createResource(NS + "FlowPropertyFactor");

	/**
	 * referenceFlowProperty - Takes a boolean value - Indicates whether the flow property of the factor is the
	 * reference flow property of the flow. The reference flow property must have a conversion factor of 1.0 and there
	 * should be only one reference flow property. As of 2015-02-12 applies to a FlowPropertyFactor, not a Flow
	 */
	public static final Property referenceFlowProperty = m_model.createProperty(NS + "referenceFlowProperty");

	/** The flow property (quantity) of the factor. As of 2015-02-12, applies to an Exchange or an Impact Factor */
	public static final Property flowProperty = m_model.createProperty(NS + "flowProperty");

	/** A LCIA category of a LCIA method (see ImpactMethod) which groups a set of characterisation factors */
	public static final Resource ImpactCategory = m_model.createResource(NS + "ImpactCategory");
	
	/**
	 * A flow property is a quantity that can be used to express amounts of a flow. The flow property (quantity) of the
	 * factor.
	 */
	public static final Resource FlowProperty = m_model.createResource(NS + "FlowProperty");

	/** The type of the flow property */
	public static final Property flowPropertyType = m_model.createProperty(NS + "flowPropertyType");

	/** FlowPropertyType - An enumeration of flow property types. The object of a flowPropertyType Property */
	public static final Resource FlowPropertyType = m_model.createResource(NS + "FlowPropertyType");

	/** Three types of FlowType: ECONOMIC_QUANTITY, and PHYSICAL_QUANTITY */
	public static final Resource ECONOMIC_QUANTITY = m_model.createResource(NS + "ECONOMIC_QUANTITY");
	public static final Resource PHYSICAL_QUANTITY = m_model.createResource(NS + "PHYSICAL_QUANTITY");

	/** unitGroup - The units of measure that can be used to express quantities of the flow property. */
	public static final Property unitGroup = m_model.createProperty(NS + "unitGroup");

	/** UnitGroup - A group of units that can be converted into each other. */
	public static final Resource UnitGroup = m_model.createResource(NS + "UnitGroup");

	/**
	 * referenceUnit - The reference unit of the group with the conversion factor 1.0. As of 2015-02-12, this applies to
	 * things of class 'Unit', not 'UnitGroup'
	 */
	public static final Property referenceUnit = m_model.createProperty(NS + "referenceUnit");

	/**
	 * referenceUnit - takes a String value - The name of the reference unit of the LCIA category (e.g. kg CO2-eq.).'
	 */
	public static final Property referenceUnitName = m_model.createProperty(NS + "referenceUnitName");

	/** Unit - A unit of measure */
	public static final Resource Unit = m_model.createResource(NS + "Unit");

	/**
	 * conversionFactor - The conversion factor to the reference unit of the unit group to which this unit belongs. As
	 * of 2015-02-12 applies to a FlowPropertyFactor
	 */
	public static final Property conversionFactor = m_model.createProperty(NS + "conversionFactor");

	/** synonyms - A list of synonyms for the unit. */
	public static final Property synonyms = m_model.createProperty(NS + "synonyms");

	/** units - All units of the unit group. */
	public static final Property units = m_model.createProperty(NS + "units");

	/** category - The category of the entity. */
	public static final Property category = m_model.createProperty(NS + "category");

	/** Category - A category for the categorisation of types like processes, flows, etc. */
	public static final Resource Category = m_model.createResource(NS + "Category");

	/** childCategories - A list of categories that are sub-categories of the category. Deprecated as of 2015-02-12 */
	public static final Property childCategories = m_model.createProperty(NS + "childCategories");

	/**
	 *  parentCategory - A list of categories that are sub-categories of the category. 
	 */
	public static final Property parentCategory = m_model.createProperty(NS + "parentCategory");

	/**
	 *  modelType - The type of models that can be linked to the category. 
	 */  
	public static final Property modelType = m_model.createProperty(NS + "modelType");

	/**
	 *  ModelType - An enumeration of the root entity types.
	 */
	public static final Resource ModelType = m_model.createResource(NS + "ModelType");

	/* Fourteen types of ModelType */
	/*
	 * The following are modelTypes for the root level: Category
	 */

	public static final Resource ACTOR = m_model.createResource(NS + "ACTOR");
	public static final Resource CATEGORY = m_model.createResource(NS + "CATEGORY");
	public static final Resource FLOW = m_model.createResource(NS + "FLOW");
	public static final Resource FLOW_PROPERTY = m_model.createResource(NS + "FLOW_PROPERTY");
	public static final Resource IMPACT_CATEGORY = m_model.createResource(NS + "IMPACT_CATEGORY");
	public static final Resource IMPACT_METHOD = m_model.createResource(NS + "IMPACT_METHOD");
	public static final Resource LOCATION = m_model.createResource(NS + "LOCATION");
	public static final Resource NW_SET = m_model.createResource(NS + "NW_SET");

	public static final Resource PROCESS = m_model.createResource(NS + "PROCESS");
	public static final Resource PRODUCT_SYSTEM = m_model.createResource(NS + "PRODUCT_SYSTEM");
	public static final Resource PROJECT = m_model.createResource(NS + "PROJECT");
	public static final Resource SOURCE = m_model.createResource(NS + "SOURCE");
	public static final Resource UNIT = m_model.createResource(NS + "UNIT");
	public static final Resource UNIT_GROUP = m_model.createResource(NS + "UNIT_GROUP");
	// public static final Resource UNKNOWN = m_model.createResource(NS + "UNKNOWN");

	/** name - The name of the entity. */
	public static final Property name = m_model.createProperty(NS + "name");
	/** description - The description of the entity. */
	public static final Property description = m_model.createProperty(NS + "description");
	/** version - A version number in ILCD format (e.g. 1.0 or 1.0.1). */
	public static final Property version = m_model.createProperty(NS + "version");
	/**
	 * lastChange - The date when the entity was changed the last time. Together with the UUID and version this
	 * identifies an entity unambiguously. The format is a Literal dateTime
	 */
	public static final Property lastChange = m_model.createProperty(NS + "lastChange");

	private static final Map<Property, Property> propertyMap = new HashMap<Property, Property>();
	private static final Map<Resource, Resource> resourceMap = new HashMap<Resource, Resource>();

	static {
		if (propertyMap.isEmpty()) {
			propertyMap.put(cas, FedLCA.hasFormattedCAS);
			propertyMap.put(formula, ECO.chemicalFormula);
			propertyMap.put(name, RDFS.label);
			propertyMap.put(description, RDFS.comment);
			propertyMap.put(version, DCTerms.hasVersion);
			propertyMap.put(synonyms, SKOS.altLabel);
			propertyMap.put(flowProperty, FedLCA.hasFlowUnit);
			propertyMap.put(flowProperties, FedLCA.hasFlowUnit);
			propertyMap.put(category, FedLCA.hasFlowContext);

			resourceMap.put(Category, FlowContext.getRdfclass());
			resourceMap.put(FlowProperty, gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.FlowProperty.getRdfclass());
			resourceMap.put(Flow, gov.epa.nrmrl.std.lca.ht.dataModels.Flow.getRdfclass());
		}
	}

	public static int convertOpenLCAToLCAHT(String graphName) {
		int count = 0;
		// ADD INFERENCES IN WHICH AN
		for (Property propertyFrom : propertyMap.keySet()) {
			Property propertyTo = propertyMap.get(propertyFrom);
			StringBuilder b = new StringBuilder();
			b.append(Prefixes.getPrefixesForQuery());
			b.append("INSERT {graph <" + graphName + "> { ?s <" + propertyTo.getURI() + "> ?to . }} \n");
			b.append("WHERE {graph <" + graphName + "> { ?s <" + propertyFrom.getURI() + "> ?to . }} \n");
			String query = b.toString();
			GenericUpdate iGenericUpdate = new GenericUpdate(query, "Temp data source", graphName);
			iGenericUpdate.getData();
			Long added = iGenericUpdate.getAddedTriples();
			if (added != null) {
				count += added.intValue();
			}
		}
		for (Resource resourceFrom : resourceMap.keySet()) {
			Resource resourceTo = resourceMap.get(resourceFrom);
			StringBuilder b = new StringBuilder();
			b.append(Prefixes.getPrefixesForQuery());
			b.append("INSERT {graph <" + graphName + "> { ?s ?p <" + resourceTo.getURI() + "> . }} \n");
			b.append("WHERE {graph <" + graphName + "> { ?s ?p <" + resourceFrom.getURI() + "> . }} \n");
			String query = b.toString();
			GenericUpdate iGenericUpdate = new GenericUpdate(query, "Temp data source", graphName);
			iGenericUpdate.getData();
			Long added = iGenericUpdate.getAddedTriples();
			if (added != null) {
				count += added.intValue();
			}
		}
		/* OpenLCA does not have Flowables, so we must create Flowables for distinct Flow name, cas, formula, dataset */
		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		b.append("select distinct  ?flow ?name ?cas ?formula ?dataSource where { \n");
		b.append("  ?flow a olca:Flow . \n");
		b.append("  ?flow olca:name ?name . \n");
		b.append("  optional { \n");
		b.append("    ?flow olca:cas ?cas . \n");
		b.append("  } \n");
		b.append("  optional { \n");
		b.append("    ?flow olca:formula ?formula . \n");
		b.append("  } \n");
		b.append("  optional { \n");
		b.append("    ?flow eco:hasDataSource ?dataSource . \n");
		b.append("  } \n");
		b.append("} \n");
		String query = b.toString();

		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);
		harmonyQuery2Impl.setGraphName(graphName);
		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		List<RDFNode> flows = new ArrayList<RDFNode>();
		List<RDFNode> names = new ArrayList<RDFNode>();
		List<RDFNode> cass = new ArrayList<RDFNode>();
		List<RDFNode> formulae = new ArrayList<RDFNode>();
		List<RDFNode> datasets = new ArrayList<RDFNode>();
		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			flows.add(querySolution.get("flow"));
			names.add(querySolution.get("name"));
			formulae.add(querySolution.get("formula"));
			cass.add(querySolution.get("cas"));
			datasets.add(querySolution.get("dataSource"));
		}
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(graphName);
		try {
			count += names.size();
			for (int i = 0; i < names.size(); i++) {
				Resource newFlowable = tdbModel.createResource(ECO.Flowable);
				Resource flowResource = flows.get(i).asResource();
				String nameString = names.get(i).asLiteral().getString();
				Literal nameLiteral = tdbModel.createTypedLiteral(nameString);
				Literal nameLiteralLC = tdbModel.createTypedLiteral(nameString.toLowerCase());
				tdbModel.addLiteral(newFlowable, RDFS.label, nameLiteral);
				tdbModel.addLiteral(newFlowable, SKOS.altLabel, nameLiteralLC);

				if (cass.get(i) != null) {
					String casString = cass.get(i).asLiteral().getString();
					Literal casLiteral = tdbModel.createTypedLiteral(casString);
					tdbModel.addLiteral(newFlowable, FedLCA.hasFormattedCAS, casLiteral);
				}
				if (formulae.get(i) != null) {
					String formulaString = formulae.get(i).asLiteral().getString();
					Literal formulaLiteral = tdbModel.createTypedLiteral(formulaString);
					tdbModel.addLiteral(newFlowable, ECO.chemicalFormula, formulaLiteral);
				}
				if (datasets.get(i) != null) {
					Resource dataset = datasets.get(i).asResource();
					tdbModel.add(newFlowable, ECO.hasDataSource, dataset);
					tdbModel.add(newFlowable, ECO.hasDataSource, dataset);
					tdbModel.add(flowResource, ECO.hasDataSource, dataset);
				} else {

				}
			}
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---

		return count;
	}

	public static int convertLCAHTToOpenLCA(String graphName) {
		int count = 0;
		// ADD INFERENCES IN WHICH AN
		for (Property propertyTo : propertyMap.keySet()) {
			Property propertyFrom = propertyMap.get(propertyTo);
			StringBuilder b = new StringBuilder();
			b.append(Prefixes.getPrefixesForQuery());
			b.append("INSERT  {graph <" + graphName + "> { ?s <" + propertyTo.getURI() + "> ?to . }} \n");
			b.append("WHERE {graph <" + graphName + "> { ?s <" + propertyFrom.getURI() + "> ?to . }} \n");
			String query = b.toString();
			GenericUpdate iGenericUpdate = new GenericUpdate(query, "Temp data source", graphName);
			iGenericUpdate.getData();
			Long added = iGenericUpdate.getAddedTriples();
			if (added != null) {
				count += added.intValue();
			}
		}
		for (Resource resourceTo : resourceMap.keySet()) {
			Resource resourceFrom = resourceMap.get(resourceTo);
			StringBuilder b = new StringBuilder();
			b.append(Prefixes.getPrefixesForQuery());
			b.append("INSERT  {graph <" + graphName + "> { ?s ?p <" + resourceTo.getURI() + "> . }} \n");
			b.append("WHERE {graph <" + graphName + "> { ?s ?p <" + resourceFrom.getURI() + "> . }} \n");
			String query = b.toString();
			GenericUpdate iGenericUpdate = new GenericUpdate(query, "Temp data source", graphName);
			iGenericUpdate.getData();
			Long added = iGenericUpdate.getAddedTriples();
			if (added != null) {
				count += added.intValue();
			}
		}
		/* To convert LCAHT Flow data, determine how to create an openLCA Flow name */
		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		b.append("select distinct  ?flow ?name ?cas ?formula ?dataSource where { \n");
		b.append("  ?flow a olca:Flow . \n");
		b.append("  ?flow olca:name ?name . \n");
		b.append("  optional { \n");
		b.append("    ?flow olca:cas ?cas . \n");
		b.append("  } \n");
		b.append("  optional { \n");
		b.append("    ?flow olca:formula ?formula . \n");
		b.append("  } \n");
		b.append("  optional { \n");
		b.append("    ?flow eco:hasDataSource ?dataSource . \n");
		b.append("  } \n");
		b.append("} \n");
		String query = b.toString();

		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);
		harmonyQuery2Impl.setGraphName(graphName);
		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		List<RDFNode> flows = new ArrayList<RDFNode>();
		List<RDFNode> names = new ArrayList<RDFNode>();
		List<RDFNode> cass = new ArrayList<RDFNode>();
		List<RDFNode> formulae = new ArrayList<RDFNode>();
		List<RDFNode> datasets = new ArrayList<RDFNode>();
		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			flows.add(querySolution.get("flow"));
			names.add(querySolution.get("name"));
			formulae.add(querySolution.get("formula"));
			cass.add(querySolution.get("cas"));
			datasets.add(querySolution.get("dataSource"));
		}
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(graphName);
		try {
			for (int i = 0; i < names.size(); i++) {
				Resource newFlowable = tdbModel.createResource(ECO.Flowable);
				String nameString = names.get(i).asLiteral().getString();
				Literal nameLiteral = tdbModel.createTypedLiteral(nameString);
				Literal nameLiteralLC = tdbModel.createTypedLiteral(nameString.toLowerCase());
				tdbModel.addLiteral(newFlowable, RDFS.label, nameLiteral);
				tdbModel.addLiteral(newFlowable, SKOS.altLabel, nameLiteralLC);
				tdbModel.add(flows.get(i).asResource(), ECO.hasFlowable, newFlowable);

				if (cass.get(i) != null) {
					String casString = cass.get(i).asLiteral().getString();
					Literal casLiteral = tdbModel.createTypedLiteral(casString);
					tdbModel.addLiteral(newFlowable, FedLCA.hasFormattedCAS, casLiteral);
				}
				if (formulae.get(i) != null) {
					String formulaString = formulae.get(i).asLiteral().getString();
					Literal formulaLiteral = tdbModel.createTypedLiteral(formulaString);
					tdbModel.addLiteral(newFlowable, ECO.chemicalFormula, formulaLiteral);
				}
				if (datasets.get(i) != null) {
					tdbModel.add(newFlowable, ECO.hasDataSource, datasets.get(i).asResource());
				} else {

				}
			}
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---

		return count;
	}
}
