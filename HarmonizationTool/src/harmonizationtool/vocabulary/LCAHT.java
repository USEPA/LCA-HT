package harmonizationtool.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Vocabulary definitions from being developed by the Environmental Modeling and Visualization Lab (EMVL)
 * Supporting work for PIs Troy Hawkins and Wes Ingwersen, US EPA 
 * @author Tom Transue 03 Jan 2014 09:46 
 */

public class LCAHT {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();

    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://epa.gov/nrmrl/std/lca/ht";

    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}

    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource airHigh_population_density = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#air-high_population_density" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource airLow_population_density = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#air-low_population_density" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource airLow_population_densityLong_term = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#air-low_population_density-_long-term" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource airLower_stratosphere_upper_troposphere = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#air-lower_stratosphere_upper_troposphere" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource airUnspecified = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#air-unspecified" );

    /** <p>implied from Category_Subcategory.xlsx</p> */
    public static final Resource release = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#release" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource resource = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#resource" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource resourceBiotic = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#resource-biotic" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource resourceIn_air = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#resource-in_air" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource resourceIn_ground = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#resource-in_ground" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource resourceIn_land = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#resource-in_land" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource resourceIn_water = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#resource-in_water" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource resourceUnspecified = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#resource-unspecified" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource soilAgricultural = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#soil-agricultural" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource soilForestry = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#soil-forestry" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource soilIndustrial = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#soil-industrial" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource soilUnspecified = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#soil-unspecified" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterFossil = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#water-fossil" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterFresh = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#water-fresh" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterFreshLong_term = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#water-fresh-long_term" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterGround = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#water-ground" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterGroundLong_term = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#water-ground-long_term" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterLake = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#water-lake" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterOcean = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#water-ocean" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterRiver = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#water-river" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterRiverLong_term = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#water-river-long_term" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterSurface = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#water-surface" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterUnspecified = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht#water-unspecified" );
    
 }
