package gov.epa.nrmrl.std.lca.ht.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Vocabulary definitions from being developed by the Environmental Modeling and Visualization Lab (EMVL)
 * Supporting work for PIs Troy Hawkins and Wes Ingwersen, US EPA 
 * @author Tom Transue 03 Jan 2014 09:46 
 */

public class SUMO {
    /** <p>The RDF tdbModel that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();



    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.ontologyportal.org/SUMO.owl#";

    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}

    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
 
    public static final Resource AngleMeasure = m_model.createResource( NS + "AngleMeasure" );
    public static final Resource AreaMeasure = m_model.createResource( NS + "AreaMeasure" );
    public static final Resource LengthMeasure = m_model.createResource( NS + "LengthMeasure" );

    public static final Resource MassMeasure = m_model.createResource( NS + "MassMeasure" );
    public static final Resource VolumeMeasure = m_model.createResource( NS + "VolumeMeasure" );
    
    

 }
