package harmonizationtool.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.DB;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.VCARD;
import com.hp.hpl.jena.vocabulary.DC_11;




/**
 * Vocabulary definitions from being developed by the Environmental Modeling and Visualization Lab (EMVL)
 * Supporting work for PIs Troy Hawkins and Wes Ingwersen, US EPA 
 * @author Tom Transue 03 Jan 2014 09:46 
 */

public class ECOGOV {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();



    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://epa.gov/nrmrl/std/lca/ecogov#";

    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}

    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>A class for CAS Numbers</p> */
    public static final Resource CASRN = m_model.createResource( "http://epa.gov/nrmrl/std/lca/ecogov#CASRN" );
    
    public static final Resource equivalent = m_model.createResource("http://epa.gov/nrmrl/std/lca/ecogov#equivalent");
    public static final Resource nonEquivalent = m_model.createResource("http://epa.gov/nrmrl/std/lca/ecogov#nonEquivalent");

    public static final Resource Equivalence = m_model.createResource("http://epa.gov/nrmrl/std/lca/ecogov#Equivalence");
    public static final Resource Annotation = m_model.createResource("http://epa.gov/nrmrl/std/lca/ecogov#Annotation");
    public static final Resource Comparison = m_model.createResource("http://epa.gov/nrmrl/std/lca/ecogov#Comparison");

    public static final Property dataSetContactName = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#dataSetContactName" );
    public static final Property dataSetContactAffiliation = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#dataSetContactAffiliation" );
    public static final Property dataSetContactEmail = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#dataSetContactEmail" );
    public static final Property dataSetContactPhone = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#dataSetContactPhone" );

    public static final Property dataSetCuratorName = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#dataSetCuratorName" );
    public static final Property dataSetCuratorAffiliation = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#dataSetCuratorAffiliation" );
    public static final Property dataSetCuratorEmail = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#dataSetCuratorEmail" );
    public static final Property dataSetCuratorPhone = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#dataSetCuratorPhone" );

    public static final Property hasComparison = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#hasComparison" );
    public static final Property comparedSource = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#comparedSource" );
    public static final Property comparedMaster = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#comparedMaster" );
    public static final Property comparedEquivalence = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#comparedEquivalence" );
    
    /** <p>'has compartment' is a static concept as opposed to either 'to' or 'from' which represent transfer in an elementary flow</p> */
    public static final Property hasCompartment = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#hasCompartment" );
        
    /** <p>Predicate pointing to CAS Class</p> */
    public static final Property hasCAS = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#hasCAS" );

    /** <p>This is being used to associate a "EcosphereExhange" with an "ElementaryFlow"</p> */
    public static final Property hasElementaryFlow = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#hasElementaryFlow" );
    
    public static final Resource ImpactCharacterization = m_model.createResource("http://epa.gov/nrmrl/std/lca/ecogov#ImpactCharacterization");
    public static final Resource FlowContext = m_model.createResource("http://epa.gov/nrmrl/std/lca/ecogov#FlowContext");
    
    public static final Property hasFlowContext = m_model.createProperty("http://epa.gov/nrmrl/std/lca/ecogov#hasFlowContext");

    /** <p></p> */
//    public static final Property atAltitude = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#atAltitude" );

    /** <p></p> */
    public static final Property hasFlowUnit = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#hasFlowUnit" );

    /** <p></p> */
    public static final Property fromCompartment = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#fromCompartment" );

    /** <p></p> */
//    public static final Property nearPopulationDensity = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#nearPopulationDensity" );

    /** <p></p> */
    public static final Property toCompartment = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#toCompartment" );

    
	public static final Property localSerialNumber = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ecogov#localSerialNumber" );

	public static final Property foundOnRow = m_model.createProperty("http://epa.gov/nrmrl/std/lca/ecogov#foundOnRow");
	
	public static final Property sourceTableRowNumber = m_model.createProperty("http://epa.gov/nrmrl/std/lca/ecogov#sourceTableRowNumber");

	public static final Property hasCategory1 = m_model.createProperty("http://epa.gov/nrmrl/std/lca/ecogov#hasCategory1");
	
	public static final Property hasCategory2 = m_model.createProperty("http://epa.gov/nrmrl/std/lca/ecogov#hasCategory2");

	public static final Property hasCategory3 = m_model.createProperty("http://epa.gov/nrmrl/std/lca/ecogov#hasCategory3");

	
	
}

