package harmonizationtool.query;

public class IdsRowQuery extends HarmonyBaseInsert {
	private final String casrn;
	private final String dataSourceIRI;
	private final String name;
	private final String altName;
	private final String rowNumber;

	public String getPrefix() {
		StringBuilder b = new StringBuilder();
		b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		b.append("PREFIX  ethold: <http://epa.gov/nrmrl/std/lca/ethold#> \n");
		b.append("PREFIX  ds:  <http://data.lca.std.nrmrl.epa.gov/" + this.dataSourceIRI + "#> \n");
		b.append(" \n");
		b.append("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
		b.append("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
		b.append("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		b.append("PREFIX  xml:    <http://www.w3.org/XML/1998/namespace> \n");
		b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
		b.append(" \n");
		return b.toString();
	}

	public String getInsertTriples() {
		StringBuilder b = new StringBuilder();
		b.append("ds:substance_" + this.rowNumber + " a eco:Substance , \n");
		b.append("                         owl:NamedIndividual ;\n");
		if ((this.casrn != null) && (!this.casrn.isEmpty())) {
			b.append("                eco:casNumber \"" + this.casrn + "\"^^xsd:string ;\n");
		}
		if ((this.name != null) && (!this.name.isEmpty())) {
			b.append("                rdfs:label \"" + this.name + "\"^^xsd:string ;\n");
		}
		if ((this.altName != null) && (!this.altName.isEmpty())) {
			b.append("                skos:altLabel \"" + this.altName + "\"^^xsd:string ;\n");
		}
		b.append("                eco:hasDataSource eco:" + this.dataSourceIRI + " .\n");
		return b.toString();
	}

	public IdsRowQuery(String casrn, String dataSourceIRI, String name, String altName, String rowNumber) {
		assert dataSourceIRI != null : "dataSourceIRI cannot be null";
		assert rowNumber != null : "rowNumber cannot be null";
		assert (casrn != null) && (name != null) && (altName != null) : "casrn & name & altName cannot all be null";
		this.casrn = casrn;
		this.dataSourceIRI = dataSourceIRI;
		this.name = name;
		this.altName = altName;
		this.rowNumber = rowNumber;

		label = this.casrn;

		StringBuilder b = new StringBuilder();
		b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		b.append("PREFIX  ethold: <http://epa.gov/nrmrl/std/lca/ethold#> \n");
		b.append("PREFIX  ds:  <http://data.lca.std.nrmrl.epa.gov/" + this.dataSourceIRI + "#> \n");
		b.append(" \n");
		b.append("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
		b.append("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
		b.append("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
		b.append(" \n");
		b.append("INSERT DATA  \n");
		b.append("{ \n");
		b.append("ds:" + this.rowNumber + " a eco:Substance , \n");
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
		b.append("                eco:hasDataSource eco:" + this.dataSourceIRI + " .  \n");
		b.append("} \n");
		b.append(" \n");
		queryStr = b.toString();
	}
}
