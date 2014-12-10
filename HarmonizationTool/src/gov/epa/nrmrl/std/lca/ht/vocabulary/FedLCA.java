package gov.epa.nrmrl.std.lca.ht.vocabulary;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Vocabulary definitions from being developed by the Environmental Modeling and Visualization Lab (EMVL) Supporting
 * work for PIs Troy Hawkins and Wes Ingwersen, US EPA
 * 
 * @author Tom Transue
 */

public class FedLCA {
	/**
	 * <p>
	 * The RDF tdbModel that holds the vocabulary terms
	 * </p>
	 */
//	private static Model m_model = ModelFactory.createDefaultModel();
	private static Model m_model = ActiveTDB.getModel();

	/**
	 * <p>
	 * The namespace of the vocabulary as a string
	 * </p>
	 */
	public static final String NS = "http://epa.gov/nrmrl/std/lca/fedlca/1.0#";

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
	public static final Resource CASRN = m_model.createResource(NS + "CASRN");
	
	public static final Resource Annotation = m_model.createResource(NS + "Annotation");
	public static final Property hasComparison = m_model.createProperty(NS + "hasComparison");
	
	public static final Resource Comparison = m_model.createResource(NS + "Comparison");
	public static final Property comparedSource = m_model.createProperty(NS + "comparedSource");
	public static final Property comparedMaster = m_model.createProperty(NS + "comparedMaster");
	public static final Property comparedEquivalence = m_model.createProperty(NS + "comparedEquivalence");
	public static final Resource Equivalence = m_model.createResource(NS + "Equivalence");
	
	public static final Resource equivalenceCandidate = m_model.createResource(NS + "equivalenceCandidate");
	public static final Resource equivalent = m_model.createResource(NS + "equivalent");
	public static final Resource equivalenceSubset = m_model.createResource(NS + "equivalenceSubset");
	public static final Resource equivalenceSuperset = m_model.createResource(NS + "equivalenceSuperset");
	public static final Resource equivalenceProxy = m_model.createResource(NS + "equivalenceProxy");
	public static final Resource nonEquivalent = m_model.createResource(NS + "nonEquivalent");

	/**
	 * <p>
	 * 'has compartment' is a static concept as opposed to either 'to' or 'from' which represent transfer in an
	 * elementary flow
	 * </p>
	 */
	public static final Property hasCompartment = m_model.createProperty(NS + "hasCompartment");

	/**
	 * <p>
	 * Predicate pointing to CAS Class
	 * </p>
	 */
	public static final Property hasCAS = m_model.createProperty(NS + "hasCAS");
	public static final Property hasFormattedCAS = m_model.createProperty(NS + "hasFormattedCAS");

	/**
	 * <p>
	 * This is being used to associate a "EcosphereExhange" with an "ElementaryFlow"
	 * </p>
	 */
	public static final Property hasElementaryFlow = m_model.createProperty(NS + "hasElementaryFlow");

	public static final Resource ImpactCharacterization = m_model.createResource(NS + "ImpactCharacterization");

	public static final Resource FlowContext = m_model.createResource(NS + "FlowContext");

	public static final Property flowContextPrimaryDescription = m_model.createProperty(NS
			+ "flowContextPrimaryDescription");

	public static final Property flowContextSupplementalDescription = m_model.createProperty(NS
			+ "flowContextSupplementalDescription");

	public static final Property hasFlowContext = m_model.createProperty(NS + "hasFlowContext");

	public static final Property flowPropertyPrimaryDescription = m_model.createProperty(NS
			+ "flowPropertyPrimaryDescription");

	public static final Property flowPropertySupplementalDescription = m_model.createProperty(NS
			+ "flowPropertySupplementalDescription");

	public static final Property hasFlowProperty = m_model.createProperty(NS + "hasFlowProperty");
	/**
	 * <p>
	 * </p>
	 */
	// public static final Property atAltitude = m_model.createProperty(
	// NS + "atAltitude" );

	/**
	 * <p>
	 * </p>
	 */
	public static final Property hasFlowUnit = m_model.createProperty(NS + "hasFlowUnit");

	/**
	 * <p>
	 * </p>
	 */
	public static final Property fromCompartment = m_model.createProperty(NS + "fromCompartment");

	/**
	 * <p>
	 * </p>
	 */
	// public static final Property nearPopulationDensity =
	// m_model.createProperty(
	// NS + "nearPopulationDensity" );

	/**
	 * <p>
	 * </p>
	 */
	public static final Property toCompartment = m_model.createProperty(NS + "toCompartment");

	public static final Property localSerialNumber = m_model.createProperty(NS + "localSerialNumber");

	// public static final Property foundOnRow = m_model
	// .createProperty(NS + "foundOnRow");

	public static final Property sourceTableRowNumber = m_model.createProperty(NS + "sourceTableRowNumber");

	public static final Property hasCategory1 = m_model.createProperty(NS + "hasCategory1");

	public static final Property hasCategory2 = m_model.createProperty(NS + "hasCategory2");

	public static final Property hasCategory3 = m_model.createProperty(NS + "hasCategory3");

	public static final Property hasSmilesString = m_model.createProperty(NS + "hasSmilesString");

	public static final Property curatedBy = m_model.createProperty(NS + "curatedBy");

	public static final Property hasContactPerson = m_model.createProperty(NS + "hasContactPerson");

	public static final Resource Person = m_model.createResource(NS + "Person");

	public static final Resource ContactPerson = m_model.createResource(NS + "ContactPerson");

	public static final Resource FlowProperty = m_model.createResource(NS + "FlowProperty");

	// public static final Resource Curator = m_model
	// .createResource(NS + "Curator");

	public static final Property personName = m_model.createProperty(NS + "personName");
	public static final Property affiliation = m_model.createProperty(NS + "affiliation");
	public static final Property email = m_model.createProperty(NS + "email");
	public static final Property voicePhone = m_model.createProperty(NS + "voicePhone");

	// public static final Property dataSourceContactName = m_model
	// .createProperty(NS + "dataSourceContactName");
	// public static final Property dataSourceContactAffiliation = m_model
	// .createProperty(NS + "dataSourceContactAffiliation");
	// public static final Property dataSourceContactEmail = m_model
	// .createProperty(NS + "dataSourceContactEmail");
	// public static final Property dataSourceContactPhone = m_model
	// .createProperty(NS + "dataSourceContactPhone");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource airHigh_population_density = m_model
			.createResource(NS + "air-high_population_density");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource airLow_population_density = m_model.createResource(NS + "air-low_population_density");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource airLow_population_densityLong_term = m_model.createResource(NS
			+ "air-low_population_density-_long-term");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource airLower_stratosphere_upper_troposphere = m_model.createResource(NS
			+ "air-lower_stratosphere_upper_troposphere");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource airUnspecified = m_model.createResource(NS + "air-unspecified");

	/**
	 * <p>
	 * implied from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource release = m_model.createResource(NS + "release");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource resource = m_model.createResource(NS + "resource");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource resourceBiotic = m_model.createResource(NS + "resource-biotic");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource resourceIn_air = m_model.createResource(NS + "resource-in_air");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource resourceIn_ground = m_model.createResource(NS + "resource-in_ground");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource resourceIn_land = m_model.createResource(NS + "resource-in_land");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource resourceIn_water = m_model.createResource(NS + "resource-in_water");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource resourceUnspecified = m_model.createResource(NS + "resource-unspecified");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource soilAgricultural = m_model.createResource(NS + "soil-agricultural");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource soilForestry = m_model.createResource(NS + "soil-forestry");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource soilIndustrial = m_model.createResource(NS + "soil-industrial");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource soilUnspecified = m_model.createResource(NS + "soil-unspecified");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource waterFossil = m_model.createResource(NS + "water-fossil");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource waterFresh = m_model.createResource(NS + "water-fresh");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource waterFreshLong_term = m_model.createResource(NS + "water-fresh-long_term");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource waterGround = m_model.createResource(NS + "water-ground");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource waterGroundLong_term = m_model.createResource(NS + "water-ground-long_term");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource waterLake = m_model.createResource(NS + "water-lake");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource waterOcean = m_model.createResource(NS + "water-ocean");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource waterRiver = m_model.createResource(NS + "water-river");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource waterRiverLong_term = m_model.createResource(NS + "water-river-long_term");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource waterSurface = m_model.createResource(NS + "water-surface");

	/**
	 * <p>
	 * from Category_Subcategory.xlsx
	 * </p>
	 */
	public static final Resource waterUnspecified = m_model.createResource(NS + "water-unspecified");

	// FROM OpenLCA:
	// Number of items
	public static final Resource ItemCount = m_model.createResource(NS + "ItemCount");
	// Items*Length
	public static final Resource ItemsLength = m_model.createResource(NS + "ItemsLength");
	// Volume*time
	public static final Resource VolumeTime = m_model.createResource(NS + "VolumeTime");
	// Mass*time
	public static final Resource MassTime = m_model.createResource(NS + "MassTime");
	// Volume*Length
	public static final Resource VolumeLength = m_model.createResource(NS + "VolumeLength");
	// Goods transport (mass*distance)
	public static final Resource GoodsTransportMassDistance = m_model.createResource(NS + "GoodsTransportMassDistance");
	// Length
	public static final Resource Length = m_model.createResource(NS + "Length");
	// Person transport
	public static final Resource PersonTransport = m_model.createResource(NS + "PersonTransport");
	// Mass
	public static final Resource Mass = m_model.createResource(NS + "Mass");
	// Net calorific value
	public static final Resource NetCalorificValue = m_model.createResource(NS + "NetCalorificValue");
	// Normal Volume
	public static final Resource NormalVolume = m_model.createResource(NS + "NormalVolume");
	// Gross calorific value
	public static final Resource GrossCalorificValue = m_model.createResource(NS + "GrossCalorificValue");
	// Radioactivity
	public static final Resource Radioactivity = m_model.createResource(NS + "Radioactivity");
	// Area
	public static final Resource Area = m_model.createResource(NS + "Area");
	// Area*time
	public static final Resource AreaTime = m_model.createResource(NS + "AreaTime");
	// Volume
	public static final Resource Volume = m_model.createResource(NS + "Volume");
	// Length*time
	public static final Resource LengthTime = m_model.createResource(NS + "LengthTime");
	// Duration
	public static final Resource Duration = m_model.createResource(NS + "Duration");
	// Energy/mass*time
	public static final Resource EnergyPerMassTime = m_model.createResource(NS + "EnergyPerMassTime");
	// Energy/area*time
	public static final Resource EnergyPerAreaTime = m_model.createResource(NS + "EnergyPerAreaTime");
	// Vehicle transport
	public static final Resource VehicleTransport = m_model.createResource(NS + "VehicleTransport");
	// Energy
	public static final Resource Energy = m_model.createResource(NS + "Energy");

	public static final Resource deleteMe = m_model.createResource(NS + "deleteMe");
	public static final Property hasValue = m_model.createProperty(NS + "hasValue");

	public static final Resource BioticProductionOcc = m_model.createResource(NS + "BioticProductionOcc");;
	public static final Resource BioticProductionTransf = m_model.createResource(NS + "BioticProductionTransf");;
	public static final Resource ErosionResistanceOcc = m_model.createResource(NS + "ErosionResistanceOcc");;
	public static final Resource ErosionResistanceTransf = m_model.createResource(NS + "ErosionResistanceTransf");;
	public static final Resource GroundwaterReplenishmentOcc = m_model.createResource(NS + "GroundwaterReplenishmentOcc");;
	public static final Resource GroundwaterReplenishmentTransf = m_model.createResource(NS + "GroundwaterReplenishmentTransf");;
	public static final Resource MechanicalFiltrationOcc = m_model.createResource(NS + "MechanicalFiltrationOcc");;
	public static final Resource MechanicalFiltrationTransf = m_model.createResource(NS + "MechanicalFiltrationTransf");;
	public static final Resource PhysicochemicalFiltrationOcc = m_model.createResource(NS + "PhysicochemicalFiltrationOcc");;
	public static final Resource PhysicochemicalFiltrationTransf = m_model.createResource(NS + "PhysicochemicalFiltrationTransf");

	public static final Resource ValueUS2000BulkPrices = m_model.createResource(NS + "ValueUS2000BulkPrices");
}
