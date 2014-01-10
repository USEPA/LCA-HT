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

public class ETHOLD {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();

    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://epa.gov/nrmrl/std/lca/ethold#";

    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}

    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    /** <p>'has compartment' is a static concept as opposed to either 'to' or 'from' which represent transfer in an elementary flow</p> */
    public static final Property hasCompartment = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ethold#hasCompartment" );

    /** <p>This is being used to associate a "EcosphereExhange" with an "ElementaryFlow"</p> */
    public static final Property hasElementaryFlow = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ethold#hasElementaryFlow" );
    
    /** <p></p> */
    public static final Property atAltitude = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ethold#atAltitude" );

    /** <p></p> */
    public static final Property fromCompartment = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ethold#fromCompartment" );

    /** <p></p> */
    public static final Property nearPopulationDensity = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ethold#nearPopulationDensity" );

    /** <p></p> */
    public static final Property toCompartment = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ethold#toCompartment" );

    
	public static final Property localSerialNumber = m_model.createProperty( "http://epa.gov/nrmrl/std/lca/ethold#localSerialNumber " );;
}

