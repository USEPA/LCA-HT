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
 * Vocabulary definitions from being developed by the Environmental Modeling and
 * Visualization Lab (EMVL) Supporting work for PIs Troy Hawkins and Wes
 * Ingwersen, US EPA
 * 
 * @author Tom Transue 03 Jan 2014 09:46
 */

public class FEDLCA {
	/**
	 * <p>
	 * The RDF tdbModel that holds the vocabulary terms
	 * </p>
	 */
	private static Model m_model = ModelFactory.createDefaultModel();

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
	public static final Resource CASRN = m_model.createResource("http://epa.gov/nrmrl/std/lca/fedlca/1.0#CASRN");

	public static final Resource equivalent = m_model
			.createResource("http://epa.gov/nrmrl/std/lca/fedlca/1.0#equivalent");
	public static final Resource nonEquivalent = m_model
			.createResource("http://epa.gov/nrmrl/std/lca/fedlca/1.0#nonEquivalent");

	public static final Resource Equivalence = m_model
			.createResource("http://epa.gov/nrmrl/std/lca/fedlca/1.0#Equivalence");
	public static final Resource Annotation = m_model
			.createResource("http://epa.gov/nrmrl/std/lca/fedlca/1.0#Annotation");
	public static final Resource Comparison = m_model
			.createResource("http://epa.gov/nrmrl/std/lca/fedlca/1.0#Comparison");




	public static final Property hasComparison = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#hasComparison");
	public static final Property comparedSource = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#comparedSource");
	public static final Property comparedMaster = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#comparedMaster");
	public static final Property comparedEquivalence = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#comparedEquivalence");

	/**
	 * <p>
	 * 'has compartment' is a static concept as opposed to either 'to' or 'from'
	 * which represent transfer in an elementary flow
	 * </p>
	 */
	public static final Property hasCompartment = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#hasCompartment");

	/**
	 * <p>
	 * Predicate pointing to CAS Class
	 * </p>
	 */
	public static final Property hasCAS = m_model.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#hasCAS");

	/**
	 * <p>
	 * This is being used to associate a "EcosphereExhange" with an
	 * "ElementaryFlow"
	 * </p>
	 */
	public static final Property hasElementaryFlow = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#hasElementaryFlow");

	public static final Resource ImpactCharacterization = m_model
			.createResource("http://epa.gov/nrmrl/std/lca/fedlca/1.0#ImpactCharacterization");

	public static final Resource FlowContext = m_model
			.createResource("http://epa.gov/nrmrl/std/lca/fedlca/1.0#FlowContext");

	public static final Property flowContextPrimaryDescription = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#flowContextPrimaryDescription");

	public static final Property flowContextSupplementalDescription = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#flowContextSupplementalDescription");

	public static final Property hasFlowContext = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#hasFlowContext");

	/**
	 * <p>
	 * </p>
	 */
	// public static final Property atAltitude = m_model.createProperty(
	// "http://epa.gov/nrmrl/std/lca/fedlca/1.0#atAltitude" );

	/**
	 * <p>
	 * </p>
	 */
	public static final Property hasFlowUnit = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#hasFlowUnit");

	/**
	 * <p>
	 * </p>
	 */
	public static final Property fromCompartment = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#fromCompartment");

	/**
	 * <p>
	 * </p>
	 */
	// public static final Property nearPopulationDensity =
	// m_model.createProperty(
	// "http://epa.gov/nrmrl/std/lca/fedlca/1.0#nearPopulationDensity" );

	/**
	 * <p>
	 * </p>
	 */
	public static final Property toCompartment = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#toCompartment");

	public static final Property localSerialNumber = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#localSerialNumber");

//	public static final Property foundOnRow = m_model
//			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#foundOnRow");

	public static final Property sourceTableRowNumber = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#sourceTableRowNumber");

	public static final Property hasCategory1 = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#hasCategory1");

	public static final Property hasCategory2 = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#hasCategory2");

	public static final Property hasCategory3 = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#hasCategory3");

	public static final Property hasSmilesString = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#hasSmilesString");

	
	public static final Property curatedBy = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#curatedBy");

	public static final Property hasContactPerson = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#hasContactPerson");
	
	public static final Resource Person = m_model
			.createResource("http://epa.gov/nrmrl/std/lca/fedlca/1.0#Person");
	
	public static final Resource ContactPerson = m_model
			.createResource("http://epa.gov/nrmrl/std/lca/fedlca/1.0#ContactPerson");
	
	public static final Resource Curator = m_model
			.createResource("http://epa.gov/nrmrl/std/lca/fedlca/1.0#Curator");


	public static final Property personName = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#personName");
	public static final Property affiliation = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#affiliation");
	public static final Property email = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#email");
	public static final Property voicePhone = m_model
			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#voicePhone");

//	public static final Property dataSourceContactName = m_model
//			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#dataSourceContactName");
//	public static final Property dataSourceContactAffiliation = m_model
//			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#dataSourceContactAffiliation");
//	public static final Property dataSourceContactEmail = m_model
//			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#dataSourceContactEmail");
//	public static final Property dataSourceContactPhone = m_model
//			.createProperty("http://epa.gov/nrmrl/std/lca/fedlca/1.0#dataSourceContactPhone");
}
