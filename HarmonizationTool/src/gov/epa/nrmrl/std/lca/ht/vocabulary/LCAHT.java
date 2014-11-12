package gov.epa.nrmrl.std.lca.ht.vocabulary;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Vocabulary definitions from being developed by the Environmental Modeling and Visualization Lab (EMVL)
 * Supporting work for PIs Troy Hawkins and Wes Ingwersen, US EPA 
 * @author Tom Transue 03 Jan 2014 09:46 
 */

public class LCAHT {
    /** <p>The RDF tdbModel that holds the vocabulary terms</p> */
//    private static Model m_model = ModelFactory.createDefaultModel();
	private static Model m_model = ActiveTDB.tdbModel;



    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://epa.gov/nrmrl/std/lca/ht/1.0#";

    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}

    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    public static final Resource Annotation = m_model.createResource( NS + "Annotation" );
    public static final Resource dataFile = m_model.createResource( NS + "dataFile" );
    public static final Property byteCount = m_model.createProperty( NS + "byteCount" );
    public static final Property fileName = m_model.createProperty( NS + "fileName" );
    public static final Property filePath = m_model.createProperty( NS + "filePath" );
    public static final Property fileEncoding = m_model.createProperty( NS + "fileEncoding" );
    public static final Property fileModifiedDate = m_model.createProperty( NS + "fileModifiedDate" );
    public static final Property fileReadDate = m_model.createProperty( NS + "fileReadDate" );
    
    public static final Property containsFile = m_model.createProperty( NS + "containsFile" );
    
    public static final Property hasQCStatus = m_model.createProperty( NS + "hasQCStatus" );
    public static final Resource QCStatusCuratedMaster = m_model.createResource( NS + "QCStatusCuratedMaster" );
    public static final Resource QCStatusAdHocMaster = m_model.createResource( NS + "QCStatusAdHocMaster" );
 }
