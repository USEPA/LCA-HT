package harmonizationtool.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * Vocabulary definitions taken from the Earthster Core Ontologhy (ECO) by the Environmental Modeling and Visualization Lab (EMVL)
 * Supporting work for PIs Troy Hawkins and Wes Ingwersen, US EPA 
 * @author Tom Transue 03 Jan 2014 09:46 
 */

/**
 *  Effect Aggregation Categories ontology.

This is an eco extension ontology that defines the class FlowAggregationCategory to represent the concept of a flow as a pair of substance and compartment.

It is an extension point for defining other kinds of common aggregation categories.

To the extent possible under law, New Earth  has waived all copyright and related or neighboring rights to http://ontology.earthster.org/eco/fasc. This work is published from United States.
 * @author Tom
 *
 */

public class CopyOfFASC {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();

    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://ontology.earthster.org/eco/fasc#";

    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}

    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    /** <p>Compartments are used for classifying effects.  Effects have a hasCompartment property and the type of the value of that property may be used to classify the effect.  Examples of compartments include emissions to urban air and resource consumption from water.</p> */
    public static final Resource Compartment = m_model.createResource( "http://ontology.earthster.org/eco/fasc#Compartment" );

    /** <p>hasCompartment indicates a compartment related to a resource.  It may be used to relate a flow aggregation category to a compartment.</p> */
    public static final Property hasCompartment = m_model.createProperty( "http://ontology.earthster.org/eco/fasc#hasCompartment" );

}