package gov.epa.nrmrl.std.lca.ht.vocabulary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.epa.nrmrl.std.lca.ht.dataModels.Flow;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataValue;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.Flowable;
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
	private static Model m_model = ActiveTDB.getModel();

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

	/**
	 * <p>
	 * A class for CAS Numbers
	 * </p>
	 */
	// public static final Resource CASRN = m_model.createResource(NS + "CASRN");
	/*
	 * Flow - Everything that can be an input or output of a process (e.g. a substance, a product, a waste, a service
	 * etc.)
	 */
	public static final Resource Flow = m_model.createResource(NS + "Flow");
	/*
	 * flowType - The type of the flow. Note that this type is more a descriptor of how the flow is handled in
	 * calculations.
	 */
	public static final Property flowType = m_model.createProperty(NS + "flowType");
	/* FlowType - The basic flow types. The object of the flowType Property */
	public static final Resource FlowType = m_model.createResource(NS + "FlowType");
	/* Three types of FlowType: ELEMENTARY_FLOW, PRODUCT_FLOW, and WASTE_FLOW */
	public static final Resource ELEMENTARY_FLOW = m_model.createResource(NS + "ELEMENTARY_FLOW");
	public static final Resource PRODUCT_FLOW = m_model.createResource(NS + "PRODUCT_FLOW");
	public static final Resource WASTE_FLOW = m_model.createResource(NS + "WASTE_FLOW");

	/* A CAS number of the flow. */
	public static final Property cas = m_model.createProperty(NS + "cas");
	/* A chemical formula of the flow. */
	public static final Property formula = m_model.createProperty(NS + "formula");

	/* Conversion factors between flow properties that can be used to express amounts of the flow. */
	public static final Property flowPropertyFactors = m_model.createProperty(NS + "flowPropertyFactors");

	/*
	 * FlowPropertyFactor - the object of flowPropertyFactors Property A FlowPropertyFactor is a conversion factor
	 * between flow properties (quantities) of a flow. As an example the amount of the flow 'water' in a process could
	 * be expressed in 'kg' mass or 'm3' volume. In this case the flow water would have two flow property factors: one
	 * for the flow property 'mass' and one for 'volume'. Each of these flow properties has a reference to a unit group
	 * which again has a reference unit. In the example the flow property 'mass' could reference the unit group 'units
	 * of mass' with 'kg' as reference unit and volume could reference the unit group 'units of volume' with 'm3' as
	 * reference unit. The flow property factor is now the conversion factor between these two reference units where the
	 * factor of the reference flow property of the flow is 1. If the reference flow property of 'water' in the example
	 * would be 'mass' the respective flow property factor would be 1 and the factor for 'volume' would be 0.001 (as 1
	 * kg water is 0.001 m3). The amount of water in a process can now be also given in liter, tons, grams etc. For
	 * this, the unit conversion factor of the respective unit group can be used to convert into the reference unit
	 * (which then can be used to convert to the reference unit of another flow property). Another thing to note is that
	 * different flow properties can refer to the same unit group (e.g. MJ upper calorific value and MJ lower calorific
	 * value.)
	 */
	public static final Resource FlowPropertyFactor = m_model.createResource(NS + "FlowPropertyFactor");

	/* The flow property (quantity) of the factor. */
	public static final Property flowProperty = m_model.createProperty(NS + "flowProperty");

	/* A flow property is a quantity that can be used to express amounts of a flow. */
	public static final Resource FlowProperty = m_model.createResource(NS + "FlowProperty");
	/* The type of the flow property */
	public static final Property flowPropertyType = m_model.createProperty(NS + "flowPropertyType");
	/* FlowPropertyType - An enumeration of flow property types. The object of a flowPropertyType Property */
	public static final Resource FlowPropertyType = m_model.createResource(NS + "FlowPropertyType");
	/* Three types of FlowType: ECONOMIC_QUANTITY, and PHYSICAL_QUANTITY */
	public static final Resource ECONOMIC_QUANTITY = m_model.createResource(NS + "ECONOMIC_QUANTITY");
	public static final Resource PHYSICAL_QUANTITY = m_model.createResource(NS + "PHYSICAL_QUANTITY");

	/* unitGroup - The units of measure that can be used to express quantities of the flow property. */
	public static final Property unitGroup = m_model.createProperty(NS + "unitGroup");
	/* UnitGroup - A group of units that can be converted into each other. */
	public static final Resource UnitGroup = m_model.createResource(NS + "UnitGroup");
	/* referenceUnit - The reference unit of the group with the conversion factor 1.0. */
	public static final Property referenceUnit = m_model.createProperty(NS + "referenceUnit");
	/* Unit - A unit of measure */
	public static final Resource Unit = m_model.createResource(NS + "Unit");
	/* conversionFactor - The conversion factor to the reference unit of the unit group to which this unit belongs. */
	public static final Property conversionFactor = m_model.createProperty(NS + "conversionFactor");
	/* synonyms - A list of synonyms for the unit. */
	public static final Property synonyms = m_model.createProperty(NS + "synonyms");
	/* units - All units of the unit group. */
	public static final Property units = m_model.createProperty(NS + "units");
	/* category - The category of the entity. */
	public static final Property category = m_model.createProperty(NS + "category");
	/* Category - A category for the categorisation of types like processes, flows, etc. */
	public static final Resource Category = m_model.createResource(NS + "Category");
	/* childCategories - A list of categories that are sub-categories of the category. */
	public static final Property childCategories = m_model.createProperty(NS + "childCategories");
	/* parentCategory - A list of categories that are sub-categories of the category. */
	public static final Property parentCategory = m_model.createProperty(NS + "parentCategory");
	/* modelType - The type of models that can be linked to the category. */
	public static final Property modelType = m_model.createProperty(NS + "modelType");

	/* ModelType - An enumeration of the root entity types. */
	public static final Resource ModelType = m_model.createResource(NS + "ModelType");
	/* Fourteen types of ModelType */
	public static final Resource PROJECT = m_model.createResource(NS + "PROJECT");
	public static final Resource IMPACT_METHOD = m_model.createResource(NS + "IMPACT_METHOD");
	public static final Resource IMPACT_CATEGORY = m_model.createResource(NS + "IMPACT_CATEGORY");
	public static final Resource PRODUCT_SYSTEM = m_model.createResource(NS + "PRODUCT_SYSTEM");
	public static final Resource PROCESS = m_model.createResource(NS + "PROCESS");
	public static final Resource FLOW = m_model.createResource(NS + "FLOW");
	public static final Resource FLOW_PROPERTY = m_model.createResource(NS + "FLOW_PROPERTY");
	public static final Resource UNIT_GROUP = m_model.createResource(NS + "UNIT_GROUP");
	public static final Resource UNIT = m_model.createResource(NS + "UNIT");
	public static final Resource ACTOR = m_model.createResource(NS + "ACTOR");
	public static final Resource SOURCE = m_model.createResource(NS + "SOURCE");
	public static final Resource CATEGORY = m_model.createResource(NS + "CATEGORY");
	public static final Resource LOCATION = m_model.createResource(NS + "LOCATION");
	public static final Resource NW_SET = m_model.createResource(NS + "NW_SET");

	/* name - The name of the entity. */
	public static final Property name = m_model.createProperty(NS + "name");
	/* description - The description of the entity. */
	public static final Property description = m_model.createProperty(NS + "description");
	/* version - A version number in ILCD format (e.g. 1.0 or 1.0.1). */
	public static final Property version = m_model.createProperty(NS + "version");
	/*
	 * lastChange - The date when the entity was changed the last time. Together with the UUID and version this
	 * identifies an entity unambiguously. The format is a Literal dateTime
	 */
	public static final Property lastChange = m_model.createProperty(NS + "lastChange");

	private static final Map<Property, Property> propertyMap = new HashMap<Property, Property>();
	private static final Map<Resource, Resource> resourceMap = new HashMap<Resource, Resource>();

	static {
		propertyMap.put(cas, ECO.casNumber);
		propertyMap.put(formula, ECO.chemicalFormula);
		propertyMap.put(name, RDFS.label);
		propertyMap.put(description, RDFS.comment);
		propertyMap.put(version, DCTerms.hasVersion);
		propertyMap.put(synonyms, SKOS.altLabel);
		propertyMap.put(flowProperty, FedLCA.hasFlowProperty);
		propertyMap.put(category, FASC.hasCompartment);

		resourceMap.put(Category, FASC.Compartment);
		resourceMap.put(FlowProperty, FedLCA.FlowProperty);
		resourceMap.put(Flow, gov.epa.nrmrl.std.lca.ht.dataModels.Flow.getRdfclass());

	}

	public static int inferOpenLCATriples() {
		int count = 0;
		// ADD INFERENCES IN WHICH AN
		for (Property propertyFrom : propertyMap.keySet()) {
			Property propertyTo = propertyMap.get(propertyFrom);
			StringBuilder b = new StringBuilder();
			b.append(Prefixes.getPrefixesForQuery());
			b.append("INSERT { ?s <" + propertyTo.getURI() + "> ?to . } \n");
			b.append("WHERE { ?s <" + propertyFrom.getURI() + "> ?to . } \n");
			String query = b.toString();
			GenericUpdate iGenericUpdate = new GenericUpdate(query, "Temp data source");
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
			b.append("INSERT { ?s ?p <" + resourceTo.getURI() + "> . } \n");
			b.append("WHERE { ?s ?p <" + resourceFrom.getURI() + "> . } \n");
			String query = b.toString();
			GenericUpdate iGenericUpdate = new GenericUpdate(query, "Temp data source");
			iGenericUpdate.getData();
			Long added = iGenericUpdate.getAddedTriples();
			if (added != null) {
				count += added.intValue();
			}
		}
		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		b.append("select distinct  ?name ?cas ?formula ?dataset_name where { \n");
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
		b.append("    ?dataSource rdfs:label ?dataset_name . \n");
		b.append("  } \n");
		b.append("} \n");
		String query = b.toString();

		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);

		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		List<RDFNode> names = new ArrayList<RDFNode>();
		List<RDFNode> cass = new ArrayList<RDFNode>();
		List<RDFNode> formulae = new ArrayList<RDFNode>();
		List<RDFNode> datasets = new ArrayList<RDFNode>();
		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			names.add(querySolution.get("name"));
			formulae.add(querySolution.get("formula"));
			cass.add(querySolution.get("cas"));
			datasets.add(querySolution.get("dataSource"));
		}
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.tdbDataset.getDefaultModel();
		try {
			for (int i = 0; i < names.size(); i++) {
				Resource newFlowable = tdbModel.createResource(ECO.Flowable);
				tdbModel.addLiteral(newFlowable, RDFS.label, names.get(i).asLiteral());
				if (cass.get(i) != null) {
					tdbModel.addLiteral(newFlowable, ECO.casNumber, cass.get(i).asLiteral());
				}
				if (formulae.get(i) != null) {
					tdbModel.addLiteral(newFlowable, ECO.chemicalFormula, formulae.get(i).asLiteral());
				}
				if (datasets.get(i) != null) {
					tdbModel.add(newFlowable, ECO.hasDataSource, datasets.get(i).asResource());
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
