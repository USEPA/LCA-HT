package harmonizationtool.query;

public class IdsRowQuery extends HarmonyBaseUpdate {
	private final String casrn;
//	private final String dataSourceIRI;
	private final String name;
	private final String altName;
	private final String cat;
	private final String subcat;
	private final String impactCat;
	private final String impactCatRefUnit;
	private final Double charFactor;
	private final String flowUnit;
	private String dataSourceIRI = "ds_999"; 

	private final String rowNumber;

	public String getPrefix() {
		
		StringBuilder b = new StringBuilder();
		b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		b.append("PREFIX  ei:     <http://ontology.earthster.org/eco/ecoinvent#> \n");
		b.append("PREFIX  eu:     <http://ontology.earthster.org/eco/unit#> \n");
		b.append("PREFIX  ecocml: <http://ontology.earthster.org/eco/CML2001#> \n");
		b.append("PREFIX  fedlca: <http://epa.gov/nrmrl/std/lca/fedlca/1.0#> \n");
		b.append("PREFIX  ds:  <http://data.lca.std.nrmrl.epa.gov/" + this.dataSourceIRI + "#> \n");
		b.append(" \n");
		b.append("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
		b.append("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
		b.append("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		b.append("PREFIX  xml:    <http://www.w3.org/XML/1998/namespace> \n");
		b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
		b.append("PREFIX  dcterms: <http://purl.org/dc/terms/> \n");

		b.append(" \n");
		return b.toString();
	}

	public String getInsertTriples() {
		StringBuilder b = new StringBuilder();
		
		b.append("ds:flowable_" + this.rowNumber + " a eco:Flowable , \n"); 
		b.append("                         owl:NamedIndividual ; \n");
		if ((this.casrn != null) && (!this.casrn.isEmpty())) {
			b.append("                eco:casNumber \"" + this.casrn + "\"^^xsd:string ; \n");
		}
		if ((this.name != null) && (!this.name.isEmpty())) {
			b.append("                rdfs:label \"" + this.name + "\"^^xsd:string ;  \n");
		}
		if ((this.altName != null) && (!this.altName.isEmpty())) {
			b.append("                skos:altLabel \"" + this.altName + "\"^^xsd:string ; \n");
		}
		b.append("                fedlca:importRow " + this.rowNumber + " ; \n");
		b.append("                eco:hasDataSource eco:" + this.dataSourceIRI + " .  \n");
		//------------------- CATEGORY INFO
		if ((this.cat != null) && (!this.cat.isEmpty())) {
			b.append("ds:category_" + this.rowNumber + " a ecocml:category1 , \n");
			b.append("                         owl:NamedIndividual ; \n");
			b.append("                rdfs:label \"" + this.cat + "\"^^xsd:string ;  \n");
			b.append("                fedlca:importRow " + this.rowNumber + " ; \n");
			b.append("                eco:hasDataSource eco:" + this.dataSourceIRI + " .  \n");
		}
		//------------------- SUB-CATEGORY INFO
		if ((this.subcat != null) && (!this.subcat.isEmpty())) {
			b.append("ds:subcategory_" + this.rowNumber + " a ecocml:category2 , \n"); 
			b.append("                         owl:NamedIndividual ; \n");
			b.append("                rdfs:label \"" + this.subcat + "\"^^xsd:string ;  \n");
			b.append("                fedlca:importRow " + this.rowNumber + " ; \n");
			b.append("                eco:hasDataSource eco:" + this.dataSourceIRI + " .  \n");
		}
		//------------------- 
		if ((this.impactCat != null) && (!this.impactCat.isEmpty())) {
			b.append("ds:impactResult_" + this.rowNumber + " a eco:ImpactCategoryIndicatorResult , \n"); 
			b.append("                         owl:NamedIndividual ; \n");
			b.append("                eco:hasImpactCategory [ a eco:ImpactCategory; rdfs:label \"" + this.impactCat + "\"^^xsd:string ] ; \n");
			b.append("                eco:hasRefUnit \"" + this.flowUnit + "\"^^xsd:string ; \n");
			b.append("                eco:hasQuantity \n");
			b.append("                [ a   eco:PhysicalQuantity ; \n");
			b.append("                    eco:hasMagnitude "+this.charFactor + "; \n");
			b.append("                    eco:hasUnitOfMeasure [ a eco:UnitOfMeasure; rdfs:label \"" + this.impactCatRefUnit + "\"^^xsd:string ] \n");
//        	b.append("                            eu:kgN_per_kg> \n");
        	b.append("                ] ; \n");
			b.append("                fedlca:importRow " + this.rowNumber + " ; \n");
			b.append("                eco:hasDataSource eco:" + this.dataSourceIRI + " .  \n");
//        	b.append("  }       \n"); 
		}
		
		return b.toString();
	}

	public IdsRowQuery(String casrn, String dataSourceIRI, String name, String altName, String cat, String subcat, String impactCat, String impactCatRefUnit, Double charFactor, String flowUnit, String rowNumber) {
//		IdsRowQuery idsRowQuery = new IdsRowQuery(casrn, dataSourceIRI, name, altName, cat, subcat, impactCat, impactCatRefUnit, charFactor, flowUnit, "" + rowNumber);


		assert dataSourceIRI != null : "dataSourceIRI cannot be null";
		assert rowNumber != null : "rowNumber cannot be null";
		assert (casrn != null) && (name != null) && (altName != null) : "casrn & name & altName cannot all be null";
		this.casrn = casrn;
		this.dataSourceIRI = dataSourceIRI;
		this.name = name;
		this.altName = altName;
		this.cat = cat;
		this.subcat = subcat;
		this.impactCat = impactCat;
		this.impactCatRefUnit = impactCatRefUnit;
		this.charFactor = charFactor;
		this.flowUnit = flowUnit;
		this.rowNumber = rowNumber;

		label = this.casrn;

		StringBuilder b = new StringBuilder();
		
		b.append("ds:flowable_" + this.rowNumber + " a eco:Flowable , \n"); 
		b.append("                         owl:NamedIndividual ; \n");
		if ((this.casrn != null) && (!this.casrn.isEmpty())) {
			b.append("                eco:casNumber \"" + this.casrn + "\"^^xsd:string ; \n");
		}
		if ((this.name != null) && (!this.name.isEmpty())) {
			b.append("                rdfs:label \"" + this.name + "\"^^xsd:string ;  \n");
		}
		if ((this.altName != null) && (!this.altName.isEmpty())) {
			b.append("                skos:altLabel \"" + this.altName + "\"^^xsd:string ; \n");
		}
		b.append("                fedlca:importRow " + this.rowNumber + " ; \n");
		b.append("                eco:hasDataSource eco:" + this.dataSourceIRI + " .  \n");
		//------------------- CATEGORY INFO
		if ((this.cat != null) && (!this.cat.isEmpty())) {
			b.append("ds:category_" + this.rowNumber + " a ecocml:category1 , \n");
			b.append("                         owl:NamedIndividual ; \n");
			b.append("                rdfs:label \"" + this.cat + "\"^^xsd:string ;  \n");
			b.append("                fedlca:importRow " + this.rowNumber + " ; \n");
			b.append("                eco:hasDataSource eco:" + this.dataSourceIRI + " .  \n");
		}
		//------------------- SUB-CATEGORY INFO
		if ((this.subcat != null) && (!this.subcat.isEmpty())) {
			b.append("ds:subcategory_" + this.rowNumber + " a ecocml:category2 , \n"); 
			b.append("                         owl:NamedIndividual ; \n");
			b.append("                rdfs:label \"" + this.subcat + "\"^^xsd:string ;  \n");
			b.append("                fedlca:importRow " + this.rowNumber + " ; \n");
			b.append("                eco:hasDataSource eco:" + this.dataSourceIRI + " .  \n");
		}
		//------------------- 
		if ((this.impactCat != null) && (!this.impactCat.isEmpty())) {
			b.append("ds:impactResult_" + this.rowNumber + " a eco:ImpactCategoryIndicatorResult , \n"); 
			b.append("                         owl:NamedIndividual ; \n");
			b.append("                eco:hasImpactCategory [ a eco:ImpactCategory; rdfs:label \"" + this.impactCat + "\"^^xsd:string ] ; \n");
			b.append("                eco:hasRefUnit \"" + this.flowUnit + "\"^^xsd:string ; \n");
			b.append("                eco:hasQuantity \n");
			b.append("                [ a   eco:PhysicalQuantity ; \n");
			b.append("                    eco:hasMagnitude "+this.charFactor + "; \n");
			b.append("                    eco:hasUnitOfMeasure [ a eco:UnitOfMeasure; rdfs:label \"" + this.impactCatRefUnit + "\"^^xsd:string ] \n");
//        	b.append("                            eu:kgN_per_kg> \n");
        	b.append("                ] ; \n");
			b.append("                fedlca:importRow " + this.rowNumber + " ; \n");
			b.append("                eco:hasDataSource eco:" + this.dataSourceIRI + " .  \n");
//        	b.append("  }       \n"); 
		}
//    		------------------- 
        	

//        System.out.println("Query= "+b.toString());
		queryStr = b.toString();
	}
}
