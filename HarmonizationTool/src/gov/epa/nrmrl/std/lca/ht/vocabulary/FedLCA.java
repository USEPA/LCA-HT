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
	private static Model m_model = ActiveTDB.getModel(null);



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
	public static final Resource Flow = m_model.createResource(NS + "Flow");
	public static final Resource ElementaryFlow = m_model.createResource(NS + "ElementaryFlow");
	public static final Resource TechnosphereFlow = m_model.createResource(NS + "TechnosphereFlow");
	
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
//	public static final Property hasCAS = m_model.createProperty(NS + "hasCAS");
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


	public static final Property flowPropertyUnitString = m_model.createProperty(NS
			+ "flowPropertyUnitString");
	
	public static final Property flowPropertyString = m_model.createProperty(NS
			+ "flowPropertyString");
	
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

	public static final Property hasIUPACName = m_model.createProperty(NS + "hasIUPACName");

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
	// Mass*length
	public static final Resource MassLength = m_model.createResource(NS + "MassLength");
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
	
	public static final Resource kg = m_model.createResource(NS+"kg");
	// Kilogram
	public static final Resource lb = m_model.createResource(NS+"lb");
	// British pound (avoirdupois)
	public static final Resource kt = m_model.createResource(NS+"kt");
	// Carat (metric)
	public static final Resource carat = m_model.createResource(NS+"carat");
	// Carat (metric)
	public static final Resource g = m_model.createResource(NS+"g");
	// Gram
	public static final Resource kg_SWU = m_model.createResource(NS+"kg_SWU");
	// Kilogram SWU
	public static final Resource long_ton = m_model.createResource(NS+"long_ton");
	// Long ton
	public static final Resource Mg = m_model.createResource(NS+"Mg");
	// Megagram = 1 metric ton
	public static final Resource ug = m_model.createResource(NS+"ug");
	// Microgram
	public static final Resource mg = m_model.createResource(NS+"mg");
	// Milligram
	public static final Resource ng = m_model.createResource(NS+"ng");
	// Nanogram
	public static final Resource oz = m_model.createResource(NS+"oz");
	// Ounce (avoirdupois); commonly used, but NOT for gold, platinum etc. (see "Ounce (troy)")
	public static final Resource oz_troy = m_model.createResource(NS+"oz_troy");
	// Ounce (troy)
	public static final Resource pg = m_model.createResource(NS+"pg");
	// Picogram; 10^-12 g
	public static final Resource sh_ton = m_model.createResource(NS+"sh_ton");
	// Short ton
	public static final Resource t = m_model.createResource(NS+"t");
	// Ton
	public static final Resource m = m_model.createResource(NS+"m");
	// Meter
	public static final Resource cm = m_model.createResource(NS+"cm");
	// Centimeter
	public static final Resource ft = m_model.createResource(NS+"ft");
	// Foot (international)
	public static final Resource in = m_model.createResource(NS+"in");
	// Inch
	public static final Resource mi = m_model.createResource(NS+"mi");
	// International mile
	public static final Resource km = m_model.createResource(NS+"km");
	// Kilometer
	public static final Resource u = m_model.createResource(NS+"u");
	// Micron
	public static final Resource mm = m_model.createResource(NS+"mm");
	// Millimetre
	public static final Resource nmi = m_model.createResource(NS+"nmi");
	// Nautical mile
	public static final Resource yd = m_model.createResource(NS+"yd");
	// Yard (international)
	public static final Resource m2 = m_model.createResource(NS+"m2");
	// Square meter
	public static final Resource ac = m_model.createResource(NS+"ac");
	// Acre (US survey)
	public static final Resource a = m_model.createResource(NS+"a");
	// Are
	public static final Resource ft2 = m_model.createResource(NS+"ft2");
	// British square feet
	public static final Resource mi2 = m_model.createResource(NS+"mi2");
	// British square mile
	public static final Resource ha = m_model.createResource(NS+"ha");
	// Hectare
	public static final Resource cm2 = m_model.createResource(NS+"cm2");
	// Square centimetre
	public static final Resource km2 = m_model.createResource(NS+"km2");
	// Square kilometer
	public static final Resource nmi2 = m_model.createResource(NS+"nmi2");
	// Square nautical mile
	public static final Resource yd2 = m_model.createResource(NS+"yd2");
	// Square yard (imperial/US)
	public static final Resource m3 = m_model.createResource(NS+"m3");
	// Cubic meter
	public static final Resource bl_Imp = m_model.createResource(NS+"bl_Imp");
	// Barrel (Imperial)
	public static final Resource bl_US_beer = m_model.createResource(NS+"bl_US_beer");
	// Barrel (US beer)
	public static final Resource bl_US_dry = m_model.createResource(NS+"bl_US_dry");
	// Barrel (US dry)
	public static final Resource bl_US_fl = m_model.createResource(NS+"bl_US_fl");
	// Barrel (US non-beer fluid)
	public static final Resource bbl = m_model.createResource(NS+"bbl");
	// Barrel (petroleum)
	public static final Resource ft3 = m_model.createResource(NS+"ft3");
	// Cubic feet
	public static final Resource fl_oz_Imp = m_model.createResource(NS+"fl_oz_Imp");
	// Fluid ounce (Imperial)
	public static final Resource gal_Imp = m_model.createResource(NS+"gal_Imp");
	// Gallon (Imperial); used in UK, United Arab Emirates for fuels
	public static final Resource gal_US_dry = m_model.createResource(NS+"gal_US_dry");
	// Gallon (US dry)
	public static final Resource gal_US_fl = m_model.createResource(NS+"gal_US_fl");
	// Gallon (US fluid); used in US e.g. for fuel
	public static final Resource gal_US_liq = m_model.createResource(NS+"gal_US_liq");
	// Gallon (US liquid)
	public static final Resource bushel_Imp = m_model.createResource(NS+"bushel_Imp");
	// Imperial bushel
	public static final Resource l = m_model.createResource(NS+"l");
	// Liter
	public static final Resource micro_l = m_model.createResource(NS+"micro_l");
	// Microlitre
	public static final Resource ml = m_model.createResource(NS+"ml");
	// Milliliter
	public static final Resource normal_m3 = m_model.createResource(NS+"normal_m3");
	// Normal cubic meters
	public static final Resource pt_Imp = m_model.createResource(NS+"pt_Imp");
	// Pint (Imperial)
	public static final Resource pt_US_dry = m_model.createResource(NS+"pt_US_dry");
	// Pint (US dry)
	public static final Resource pt_US_fl = m_model.createResource(NS+"pt_US_fl");
	// Pint (US fluid)
	public static final Resource bsh_US = m_model.createResource(NS+"bsh_US");
	// US bushel
	public static final Resource US_fl_oz = m_model.createResource(NS+"US_fl_oz");
	// US customary fluid ounce
	public static final Resource day = m_model.createResource(NS+"day");
	// Day
	public static final Resource hour = m_model.createResource(NS+"hour");
	// Hour
	public static final Resource min = m_model.createResource(NS+"min");
	// Minute
	public static final Resource sec = m_model.createResource(NS+"sec");
	// Second
	public static final Resource year = m_model.createResource(NS+"year");
	// Year (rounded)
	public static final Resource MJ = m_model.createResource(NS+"MJ");
	// Megajoule
	public static final Resource btu = m_model.createResource(NS+"btu");
	// British thermal unit (International table)
	public static final Resource GJ = m_model.createResource(NS+"GJ");
	// Gigajoule
	public static final Resource J = m_model.createResource(NS+"J");
	// Joule
	public static final Resource kcal = m_model.createResource(NS+"kcal");
	// Kilocalorie (International table)
	public static final Resource kJ = m_model.createResource(NS+"kJ");
	// Kilojoule
	public static final Resource kWh = m_model.createResource(NS+"kWh");
	// Kilowatt times hour
	public static final Resource MWh = m_model.createResource(NS+"MWh");
	// Megawatt times hour
	public static final Resource TJ = m_model.createResource(NS+"TJ");
	// Terajoule
	public static final Resource TCE = m_model.createResource(NS+"TCE");
	// Ton coal equivalent
	public static final Resource TOE = m_model.createResource(NS+"TOE");
	// Ton oil equivalent
	public static final Resource Wh = m_model.createResource(NS+"Wh");
	// Watt times hour
	public static final Resource kBq = m_model.createResource(NS+"kBq");
	// Kilo-Bequerel, 1000 events per second
	public static final Resource Bq = m_model.createResource(NS+"Bq");
	// Bequerel, 1 event per second
	public static final Resource Ci = m_model.createResource(NS+"Ci");
	// Curie
	public static final Resource Rutherford = m_model.createResource(NS+"Rutherford");
	// Rutherford
	public static final Resource kg_year = m_model.createResource(NS+"kg_year");
	// Kilogram times year
	public static final Resource g_year = m_model.createResource(NS+"g_year");
	// Gram times year
	public static final Resource kg_day = m_model.createResource(NS+"kg_day");
	// Kilogram times day (1 year = 365 days)
	public static final Resource ton_day = m_model.createResource(NS+"ton_day");
	// Metric ton times day (1 year = 365 days)
	public static final Resource ton_year = m_model.createResource(NS+"ton_year");
	// Metric tonnes times year
	public static final Resource ton_km = m_model.createResource(NS+"ton_km");
	// Metric ton-kilometer
	public static final Resource lb_mi = m_model.createResource(NS+"lb_mi");
	// British pound (avoirdupois) times international mile
	public static final Resource lb_nautical_mi = m_model.createResource(NS+"lb_nautical_mi");
	// British pound (avoirdupois) times nautical mile
	public static final Resource kg_km = m_model.createResource(NS+"kg_km");
	// Kilogram-kilometer
	public static final Resource ton_mi = m_model.createResource(NS+"ton_mi");
	// Metric ton times international mile
	public static final Resource ton_nautical_mi = m_model.createResource(NS+"ton_nautical_mi");
	// Metric ton times nautical mile
	public static final Resource m_year = m_model.createResource(NS+"m_year");
	// Meter times year
	public static final Resource m2_year = m_model.createResource(NS+"m2_year");
	// Square meter times year
	public static final Resource ft2_year = m_model.createResource(NS+"ft2_year");
	// British square feet times year
	public static final Resource mi2_year = m_model.createResource(NS+"mi2_year");
	// British square mile times year
	public static final Resource ha_year = m_model.createResource(NS+"ha_year");
	// Hectare times year
	public static final Resource km2_year = m_model.createResource(NS+"km2_year");
	// Square kilometer times year
	public static final Resource m2_day = m_model.createResource(NS+"m2_day");
	// Square metre times day
	public static final Resource m3_year = m_model.createResource(NS+"m3_year");
	// Cubic meter times year
	public static final Resource m3_day = m_model.createResource(NS+"m3_day");
	// Cubic meter times day
	public static final Resource l_day = m_model.createResource(NS+"l_day");
	// Liter times day
	public static final Resource l_year = m_model.createResource(NS+"l_year");
	// Liter times year
	public static final Resource MJ_per_kg_day = m_model.createResource(NS+"MJ_per_kg_day");
	// Megajoule per kilogram times day
	public static final Resource kWh_per_m2_day = m_model.createResource(NS+"kWh_per_m2_day");
	// Kilowatthour per square meter times day
	public static final Resource Item = m_model.createResource(NS+"Item");
	// Number of items
	public static final Resource DozenItems = m_model.createResource(NS+"DozenItems");
	// Dozen(s) of items
	public static final Resource person_km = m_model.createResource(NS+"person_km");
	// Person kilometer
	public static final Resource vehicle_km = m_model.createResource(NS+"vehicle_km");
	// Vehicle-kilometer

	public static final Resource m3_km = m_model.createResource(NS+"m3_km");
	// Cubic metre times kilometre
	public static final Resource m3_mi = m_model.createResource(NS+"m3_mi");
	// Cubic metre times international mile
	public static final Resource m3_nautical_mi = m_model.createResource(NS+"m3_nautical_mi");
	// Cubic metre times nautical mile
	public static final Resource l_km = m_model.createResource(NS+"l_km");
	// Litre times kilometre
	public static final Resource l_mi = m_model.createResource(NS+"l_mi");
	// Litre times international mile
	public static final Resource l_nautical_mi = m_model.createResource(NS+"l_nautical_mi");
	// Litre times nautical mile
	
	public static final Resource items_km = m_model.createResource(NS+"items_km");
	// Items times kilometre
	public static final Resource items_mi = m_model.createResource(NS+"items_mi");
	// Items times international mile
	public static final Resource items_nautical_mi = m_model.createResource(NS+"items_nautical_mi");
	// Items times nautical mile
}
