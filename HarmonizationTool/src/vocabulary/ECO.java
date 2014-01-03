package vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * Vocabulary definitions taken from the Earthster Core Ontologhy (ECO) by the Environmental Modeling and Visualization Lab (EMVL)
 * Supporting work for PIs Troy Hawkins and Wes Ingwersen, US EPA 
 * @author Tom Transue 03 Jan 2014 09:46 
 */

public class ECO {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();

    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://ontology.earthster.org/eco/core#";

    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}

    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    /** <p>A data source is a source of data used in a model.</p> */
    public static final Resource DataSource = m_model.createProperty( "http://ontology.earthster.org/eco/core#DataSource" );
    
    /** <p>allocatedBy relates a quantified effect to an effect allocation that allocates it.</p> */
    public static final Property allocatedBy = m_model.createProperty( "http://ontology.earthster.org/eco/core#allocatedBy" );

    /** <p>allocatedFrom relates an effect allocation to a quantified effect which is allocated.</p> */
    public static final Property allocatedFrom = m_model.createProperty( "http://ontology.earthster.org/eco/core#allocatedFrom" );

    /** <p>allocatedTo relates an effect allocation to a technosphere exchange the allocation is to.</p> */
    public static final Property allocatedTo = m_model.createProperty( "http://ontology.earthster.org/eco/core#allocatedTo" );

    /** <p>assessmentOf relates an impact assessment to the entity it is an assessment of.</p> */
    public static final Property assessmentOf = m_model.createProperty( "http://ontology.earthster.org/eco/core#assessmentOf" );

    /** <p>compliesWith may be used to relate a model to a compliance requirement that it complies with.</p> */
    public static final Property compliesWith = m_model.createProperty( "http://ontology.earthster.org/eco/core#compliesWith" );

    /** <p>computedFrom relates an impact assessment to the LCA model from which it was computed.</p> */
    public static final Property computedFrom = m_model.createProperty( "http://ontology.earthster.org/eco/core#computedFrom" );

    /** <p>consumptionRegion relates a restriction relation to a geographic region and states that the aggregate is restricted to consumption in that geographic region.</p> */
    public static final Property consumptionRegion = m_model.createProperty( "http://ontology.earthster.org/eco/core#consumptionRegion" );

    /** <p>currentAtTime indicates that a technology was current at a given time.</p> */
    public static final Property currentAtTime = m_model.createProperty( "http://ontology.earthster.org/eco/core#currentAtTime" );

    /** <p>duringInterval relates a restriction relation to a time interval and states that the aggregate is restricted to that time interval.</p> */
    public static final Property duringInterval = m_model.createProperty( "http://ontology.earthster.org/eco/core#duringInterval" );

    /** <p>hasAggregate relates a restriction relation to the aggregate that results from the restriction.</p> */
    public static final Property hasAggregate = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasAggregate" );

    /** <p>A subproperty of hasQuantifiedEffect that indicates the quantified effect is allocated.</p> */
    public static final Property hasAllocatedQuantifiedEffect = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasAllocatedQuantifiedEffect" );

    /** <p>hasAssignment is a symmetric property that relates a variable and its assigned quantity.</p> */
    public static final Property hasAssignment = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasAssignment" );

    /** <p>hasAttributeDefinition relates an attribute to its definition.</p> */
    public static final Property hasAttributeDefinition = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasAttributeDefinition" );

    /** <p>hasAttributeValue relates an entity to an attribute value for that entity.</p> */
    public static final Property hasAttributeValue = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasAttributeValue" );

    /** <p>hasBindingInputExchange relates an exchange binding to its input exchange.</p> */
    public static final Property hasBindingInputExchange = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasBindingInputExchange" );

    /** <p>hasBindingOutputExchange relates an exchange binding to its output exchange.</p> */
    public static final Property hasBindingOutputExchange = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasBindingOutputExchange" );

    /** <p>hasCategory relates an entity to a category of that entity.</p> */
    public static final Property hasCategory = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasCategory" );

    /** <p>hasEffect may be used to indicate the effect of a quantified effect.</p> */
    public static final Property hasEffect = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasEffect" );

    /** <p>hasEffectAggregationCategory relates an effect to an an aggregation category.</p> */
    public static final Property hasEffectAggregationCategory = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasEffectAggregationCategory" );

    /** <p>hasEffectAllocation relates an allocation to an effect allocation resulting from that allocation.</p> */
    public static final Property hasEffectAllocation = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasEffectAllocation" );

    /** <p>hasExpression may be used to relate a quantity to an expression.</p> */
    public static final Property hasExpression = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasExpression" );

    /** <p>hasExpressionContext may be used to relate an expression to an expression context for use in its evaluation.</p> */
    public static final Property hasExpressionContext = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasExpressionContext" );

    /** <p>hasFlow relates an ecosphere exchange to its flow.</p> */
    public static final Property hasFlow = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasFlow" );

    /** <p>hasFunction relates a functional unit of measure to the function it is used to measure.</p> */
    public static final Property hasFunction = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasFunction" );

    /** <p>hasFunctionalQuantity relates an entity to a functional quantity and is typically used to relate an exchange to a functional quantity.</p> */
    public static final Property hasFunctionalQuantity = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasFunctionalQuantity" );

    /** <p>hasGeoLocation relates an entity to its geo location.</p> */
    public static final Property hasGeoLocation = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasGeoLocation" );

    /** <p></p> */
    public static final Property hasImpactAssessment = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasImpactAssessment" );

    /** <p>may be used to relate an impact assessment method to a complete list of its impact assessment method category descriptions.</p> */
    public static final Property hasImpactAssessmentCategoryDescriptionList = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasImpactAssessmentCategoryDescriptionList" );

    /** <p>relates an impact assessment to one or more impact assessment methods used to compute its results.</p> */
    public static final Property hasImpactAssessmentMethod = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasImpactAssessmentMethod" );

    /** <p>relates an impact category indicator result to the impact assessment method category description that describes how it was computed.</p> */
    public static final Property hasImpactAssessmentMethodCategoryDescription = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasImpactAssessmentMethodCategoryDescription" );

    /** <p>hasImpactCategory may be used to relate an impact assessment method category description to an impact category.</p> */
    public static final Property hasImpactCategory = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasImpactCategory" );

    /** <p>relates an impact assessment method category description to an impact category indicator.</p> */
    public static final Property hasImpactCategoryIndicator = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasImpactCategoryIndicator" );

    /** <p>relates an impact assessment method category description to a impact characterization factor.</p> */
    public static final Property hasImpactCharacterizationFactor = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasImpactCharacterizationFactor" );

    /** <p>relates an impact assessment method category description to an impact characterization model.</p> */
    public static final Property hasImpactCharacterizationModel = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasImpactCharacterizationModel" );

    /** <p>hasModel relates an entity to a model of that entity.</p> */
    public static final Property hasModel = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasModel" );

    /** <p>hasQuantifiedEffect relates an LCA model to a quantified effect of the entity being modelled.</p> */
    public static final Property hasQuantifiedEffect = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasQuantifiedEffect" );

    /** <p>hasQuantifiedEffect list relates an LCA model to a complete list of the quantified effects in the model.  This can be used to avoid making a closed world assumption using only the property hasQuantifiedEffect.</p> */
    public static final Property hasQuantifiedEffectList = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasQuantifiedEffectList" );

    /** <p>May be used to indicate a quantity of something. May be used to indicated the quantity of a quantified effect.</p> */
    public static final Property hasQuantity = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasQuantity" );

    /** <p></p> */
    public static final Property hasRefUnit = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasRefUnit" );

    /** <p>hasReferenceExchange is used to relate a model to its reference exchange.</p> */
    public static final Property hasReferenceExchange = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasReferenceExchange" );

    /** <p>hasRelated version may be used to relate a model to a different version of that model.</p> */
    public static final Property hasRelatedVersion = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasRelatedVersion" );

    /** <p>hasReport may be used to relate a validation result to a report.</p> */
    public static final Property hasReport = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasReport" );

    /** <p>hasRestrictionRelation relates an aggregate to a relation that defines it as a restriction of another aggregate.</p> */
    public static final Property hasRestrictionRelation = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasRestrictionRelation" );

    /** <p>hasStructuredValue may be used to relate an entity to a structured value.  For example, it may be used to related an impact result to a structured value.</p> */
    public static final Property hasStructuredValue = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasStructuredValue" );

    /** <p>hasTechnosphereTransfer relates a technosphere exchange to a technosphere transfer.</p> */
    public static final Property hasTechnosphereTransfer = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasTechnosphereTransfer" );

    /** <p>hasTransferable relates a technosphere transfer to what is transfered.</p> */
    public static final Property hasTransferable = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasTransferable" );

    /** <p>hasUncertaintyDistribution can relate a quantity to an uncertainty distribution for that quantity.</p> */
    public static final Property hasUncertaintyDistribution = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasUncertaintyDistribution" );

    /** <p>hasUnitOfMeasure relates a physical quantity to its units of measure.</p> */
    public static final Property hasUnitOfMeasure = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasUnitOfMeasure" );

    /** <p>hasUnitQuantity is used to relate a functional unit of measure to a unit quantity.</p> */
    public static final Property hasUnitQuantity = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasUnitQuantity" );

    /** <p>hasValidationResult relates a model to a result from a process of validating that model.</p> */
    public static final Property hasValidationResult = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasValidationResult" );

    /** <p>hasVariable relates an expression context to a variable valid in that context.  The values of the rdfs:label properties of all the variables in a context must be unique.</p> */
    public static final Property hasVariable = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasVariable" );

    /** <p>models relates a model to an entity it is a model of.</p> */
    public static final Property models = m_model.createProperty( "http://ontology.earthster.org/eco/core#models" );

    /** <p>modernAtTime indicate that a technology was modern at a given time.</p> */
    public static final Property modernAtTime = m_model.createProperty( "http://ontology.earthster.org/eco/core#modernAtTime" );

    /** <p>newAtTime indicates that technology was new at a given time.</p> */
    public static final Property newAtTime = m_model.createProperty( "http://ontology.earthster.org/eco/core#newAtTime" );

    /** <p>oldAtTime indicates that a technology was old at a given time.</p> */
    public static final Property oldAtTime = m_model.createProperty( "http://ontology.earthster.org/eco/core#oldAtTime" );

    /** <p>outdatedAtTime indicates that a technology was outdated at a given time.</p> */
    public static final Property outdatedAtTime = m_model.createProperty( "http://ontology.earthster.org/eco/core#outdatedAtTime" );

    /** <p>relates an impact assessment to the quanity of a good or service to which the results are relative.</p> */
    public static final Property quantityAssessed = m_model.createProperty( "http://ontology.earthster.org/eco/core#quantityAssessed" );

    /** <p>responsibleAgent relates an entity to an agent responsible for it.  It may be used to relate a validation result to an agent.</p> */
    public static final Property responsibleAgent = m_model.createProperty( "http://ontology.earthster.org/eco/core#responsibleAgent" );

    /** <p>restrictedBy relates a restriction relation to the value that constrains the members of the restriction.  Subproperties of restrictedBy define what aspect of the restricted entities is restricted by this value.</p> */
    public static final Property restrictedBy = m_model.createProperty( "http://ontology.earthster.org/eco/core#restrictedBy" );

    /** <p>restrictionOf relates a restriction relation to the aggregate that it restricts.</p> */
    public static final Property restrictionOf = m_model.createProperty( "http://ontology.earthster.org/eco/core#restrictionOf" );

    /** <p>sourceFrom is used to relate an ouput exchange to the input exchange that it is sourced from.</p> */
    public static final Property sourcedFrom = m_model.createProperty( "http://ontology.earthster.org/eco/core#sourcedFrom" );

    /** <p>usesTechnology indicates that an entity uses a technology.</p> */
    public static final Property usesTechnology = m_model.createProperty( "http://ontology.earthster.org/eco/core#usesTechnology" );

    /** <p>validInterval relates an entity to a time interval for which it is valid.  validInterval may be used to annotate a model.</p> */
    public static final Property validInterval = m_model.createProperty( "http://ontology.earthster.org/eco/core#validInterval" );

    /** <p></p> */
    public static final Property hasDataSource = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasDataSource" );

    /** <p></p> */
    public static final Property hasFlowable = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasFlowable" );

    /** <p></p> */
    public static final Property hasFootprintModel = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasFootprintModel" );

    /** <p></p> */
    public static final Property hasImpactCategoryIndicatorResult = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasImpactCategoryIndicatorResult" );

    /** <p></p> */
    public static final Property hasRate = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasRate" );

    /** <p></p> */
    public static final Property hasUnallocatedQuantifiedEffect = m_model.createProperty( "http://ontology.earthster.org/eco/core#hasUnallocatedQuantifiedEffect" );
}
