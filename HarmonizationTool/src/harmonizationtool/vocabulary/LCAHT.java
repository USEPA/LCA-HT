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
    public static final String NS = "http://epa.gov/nrmrl/std/lca/ht/1.0#";

    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}

    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource airHigh_population_density = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#air-high_population_density" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource Annotation = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#Annotation" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource airLow_population_density = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#air-low_population_density" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource airLow_population_densityLong_term = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#air-low_population_density-_long-term" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource airLower_stratosphere_upper_troposphere = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#air-lower_stratosphere_upper_troposphere" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource airUnspecified = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#air-unspecified" );

    /** <p>implied from Category_Subcategory.xlsx</p> */
    public static final Resource release = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#release" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource resource = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#resource" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource resourceBiotic = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#resource-biotic" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource resourceIn_air = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#resource-in_air" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource resourceIn_ground = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#resource-in_ground" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource resourceIn_land = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#resource-in_land" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource resourceIn_water = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#resource-in_water" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource resourceUnspecified = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#resource-unspecified" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource soilAgricultural = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#soil-agricultural" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource soilForestry = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#soil-forestry" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource soilIndustrial = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#soil-industrial" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource soilUnspecified = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#soil-unspecified" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterFossil = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#water-fossil" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterFresh = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#water-fresh" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterFreshLong_term = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#water-fresh-long_term" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterGround = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#water-ground" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterGroundLong_term = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#water-ground-long_term" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterLake = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#water-lake" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterOcean = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#water-ocean" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterRiver = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#water-river" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterRiverLong_term = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#water-river-long_term" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterSurface = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#water-surface" );

    /** <p>from Category_Subcategory.xlsx</p> */
    public static final Resource waterUnspecified = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#water-unspecified" );
    
    public static final Resource dataFile = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ht/1.0#dataFile" );
    public static final Property byteCount = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ht/1.0#byteCount" );
    public static final Property fileName = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ht/1.0#fileName" );
    public static final Property filePath = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ht/1.0#filePath" );
    public static final Property fileEncoding = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ht/1.0#fileEncoding" );
    public static final Property fileLastModified = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ht/1.0#fileLastModified" );
    public static final Property fileReadDate = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ht/1.0#fileReadDate" );
    
    public static final Property containsFile = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ht/1.0#containsFile" );


    

    
 }
