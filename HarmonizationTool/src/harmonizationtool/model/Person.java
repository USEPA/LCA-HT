package harmonizationtool.model;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FEDLCA;
import harmonizationtool.vocabulary.LCAHT;

import java.util.Date;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Person {
	private String name;
	private String affiliation;
	private String email;
	private String phone;
	private Resource tdbResource;
	protected final static Model model = ActiveTDB.model;

	public Person() {
		this.tdbResource = model.createResource();
		model.add(tdbResource, RDF.type, ECO.Person);
	}

	public Person(Resource tdbResource) {
		this.tdbResource = tdbResource;
		syncDataFromTDB();
	}

	public Person(String name, String affiliation, String email, String phone) {
		super();
		this.name = name;
		this.affiliation = affiliation;
		this.email = email;
		this.phone = phone;
		this.tdbResource = model.createResource();
		model.add(tdbResource, RDF.type, ECO.Person);
		model.add(this.tdbResource, FEDLCA.personName, model.createTypedLiteral(this.name));
		model.add(this.tdbResource, FEDLCA.affiliation, model.createTypedLiteral(this.affiliation));
		model.add(this.tdbResource, FEDLCA.email, model.createTypedLiteral(this.email));
		model.add(this.tdbResource, FEDLCA.voicePhone, model.createTypedLiteral(this.phone));
	}

	public String getName() {
		if (name == null) {
			name = "";
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
		ActiveTDB.replaceLiteral(tdbResource, FEDLCA.personName, name);
	}

	public String getAffiliation() {
		if (affiliation == null) {
			affiliation = "";
		}
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
		ActiveTDB.replaceLiteral(tdbResource, FEDLCA.affiliation, affiliation);
	}

	public String getEmail() {
		if (email == null) {
			email = "";
		}
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
		ActiveTDB.replaceLiteral(tdbResource, FEDLCA.email, email);
	}

	public String getPhone() {
		if (phone == null) {
			phone = "";
		}
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
		ActiveTDB.replaceLiteral(tdbResource, FEDLCA.voicePhone, phone);
	}

	public void syncDataFromTDB() {
		NodeIterator nodeIterator;
		RDFNode object;
		Literal literal;
		if (tdbResource == null) {
			return;
		}

		nodeIterator = model.listObjectsOfProperty(tdbResource, FEDLCA.personName);
		if (nodeIterator.hasNext()) {
			object = nodeIterator.next();
			if (object.isLiteral()) {
				literal = object.asLiteral();
				Object javaObject = literal.getValue();
				if (javaObject.getClass().equals(String.class)) {
					name = (String) literal.getValue();
				}
			}
		}

		nodeIterator = model.listObjectsOfProperty(tdbResource, FEDLCA.affiliation);
		if (nodeIterator.hasNext()) {
			object = nodeIterator.next();
			if (object.isLiteral()) {
				literal = object.asLiteral();
				Object javaObject = literal.getValue();
				if (javaObject.getClass().equals(String.class)) {
					affiliation = (String) literal.getValue();
				}
			}
		}
		nodeIterator = model.listObjectsOfProperty(tdbResource, FEDLCA.email);
		if (nodeIterator.hasNext()) {
			object = nodeIterator.next();
			if (object.isLiteral()) {
				literal = object.asLiteral();
				Object javaObject = literal.getValue();
				if (javaObject.getClass().equals(String.class)) {
					email = (String) literal.getValue();
				}
			}
		}
		nodeIterator = model.listObjectsOfProperty(tdbResource, FEDLCA.voicePhone);
		if (nodeIterator.hasNext()) {
			object = nodeIterator.next();
			if (object.isLiteral()) {
				literal = object.asLiteral();
				Object javaObject = literal.getValue();
				if (javaObject.getClass().equals(String.class)) {
					phone = (String) literal.getValue();
				}
			}
		}
	}

	public void remove() {
		tdbResource.removeAll(FEDLCA.personName);
		tdbResource.removeAll(FEDLCA.affiliation);
		tdbResource.removeAll(FEDLCA.email);
		tdbResource.removeAll(FEDLCA.voicePhone);
	}
}
