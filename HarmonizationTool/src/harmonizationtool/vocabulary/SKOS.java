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

public class SKOS {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();

    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.w3.org/2004/02/skos/core#";

    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}

    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    /** <p></p> */
    public static final Property altLabel = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#altLabel" );
    
    /** <p>Broader concepts are typically rendered as parents in a concept hierarchy (tree).</p> */
    public static final Property broader = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#broader" );

    /** <p>skos:exactMatch is disjoint with each of the properties skos:broadMatch and skos:relatedMatch.</p> */
    public static final Property exactMatch = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#exactMatch" );

    /** <p>These concept mapping relations mirror semantic relations, and the data model defined below is similar (with the exception of skos:exactMatch) to the data model defined for semantic relations. A distinct vocabulary is provided for concept mapping relations, to provide a convenient way to differentiate links within a concept scheme from links between concept schemes. However, this pattern of usage is not a formal requirement of the SKOS data model, and relies on informal definitions of best practice.</p> */
    public static final Property mappingRelation = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#mappingRelation" );

    /** <p>For any resource, every item in the list given as the value of the skos:memberList property is also a value of the skos:member property.</p> */
    public static final Property memberList = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#memberList" );

    /** <p>Narrower concepts are typically rendered as children in a concept hierarchy (tree).</p> */
    public static final Property narrower = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#narrower" );

    /** <p>skos:related is disjoint with skos:broaderTransitive</p> */
    public static final Property related = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#related" );
    
    /** <p></p> */
    public static final Property broadMatch = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#broadMatch" );

    /** <p></p> */
    public static final Property broaderTransitive = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#broaderTransitive" );

    /** <p></p> */
    public static final Property closeMatch = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#closeMatch" );

    /** <p></p> */
    public static final Property hasTopConcept = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#hasTopConcept" );

    /** <p></p> */
    public static final Property inScheme = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#inScheme" );

    /** <p></p> */
    public static final Property member = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#member" );

    /** <p></p> */
    public static final Property narrowMatch = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#narrowMatch" );

    /** <p></p> */
    public static final Property narrowerTransitive = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#narrowerTransitive" );

    /** <p></p> */
    public static final Property relatedMatch = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#relatedMatch" );

    /** <p></p> */
    public static final Property semanticRelation = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#semanticRelation" );

    /** <p></p> */
    public static final Property topConceptOf = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#topConceptOf" );
}
