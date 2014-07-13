package harmonizationtool.model;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FEDLCA;
import harmonizationtool.vocabulary.LCAHT;

import java.util.Date;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
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
	private static final Resource rdfClass = ECO.Person;
	private Resource tdbResource;
	protected final static Model model = ActiveTDB.model;

	public Person() {
		this.tdbResource = model.createResource();
		model.add(tdbResource, RDF.type, ECO.Person);
		PersonKeeper.add(this);
	}

	public Person(Resource tdbResource) {
		this.tdbResource = tdbResource;
		syncDataFromTDB();
		PersonKeeper.add(this);
	}

	public Person(String name, String affiliation, String email, String phone) {
		super();
		setName(name);
		setAffiliation(affiliation);
		setEmail(email);
		setPhone(phone);
//		this.name = name;
//		this.affiliation = affiliation;
//		this.email = email;
//		this.phone = phone;
//		this.tdbResource = model.createResource();
//		model.add(tdbResource, RDF.type, ECO.Person);
//		model.add(this.tdbResource, FEDLCA.personName, model.createTypedLiteral(this.name));
//		model.add(this.tdbResource, FEDLCA.affiliation, model.createTypedLiteral(this.affiliation));
//		model.add(this.tdbResource, FEDLCA.email, model.createTypedLiteral(this.email));
//		model.add(this.tdbResource, FEDLCA.voicePhone, model.createTypedLiteral(this.phone));
		PersonKeeper.add(this);

	}

	public String getName() {
		if (name == null) {
			name = "";
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
		tdbResource.removeAll(FEDLCA.personName);
		tdbResource.addProperty(FEDLCA.personName, name);
//		ActiveTDB.replaceLiteral(tdbResource, FEDLCA.personName, XSDDatatype.XSDstring, name);
	}

	public String getAffiliation() {
		if (affiliation == null) {
			affiliation = "";
		}
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
		tdbResource.removeAll(FEDLCA.affiliation);
		tdbResource.addProperty(FEDLCA.affiliation, affiliation);
//		ActiveTDB.replaceLiteral(tdbResource, FEDLCA.affiliation, XSDDatatype.XSDstring, affiliation);
	}

	public String getEmail() {
		if (email == null) {
			email = "";
		}
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
		tdbResource.removeAll(FEDLCA.email);
		tdbResource.addProperty(FEDLCA.email, email);
//		ActiveTDB.replaceLiteral(tdbResource, FEDLCA.email, XSDDatatype.XSDstring, email);
	}

	public String getPhone() {
		if (phone == null) {
			phone = "";
		}
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
		tdbResource.removeAll(FEDLCA.voicePhone);
		tdbResource.addProperty(FEDLCA.voicePhone, phone);
//		ActiveTDB.replaceLiteral(tdbResource, FEDLCA.voicePhone, XSDDatatype.XSDstring, phone);
	}

	public void syncDataFromTDB() {
		RDFNode resource;

		if (tdbResource == null) {
			return;
		}

		resource = tdbResource.getProperty(FEDLCA.personName).getObject();
		name = ActiveTDB.getStringFromLiteral(resource);

		resource = tdbResource.getProperty(FEDLCA.affiliation).getObject();
		affiliation = ActiveTDB.getStringFromLiteral(resource);

		resource = tdbResource.getProperty(FEDLCA.email).getObject();
		email = ActiveTDB.getStringFromLiteral(resource);

		resource = tdbResource.getProperty(FEDLCA.voicePhone).getObject();
		phone = ActiveTDB.getStringFromLiteral(resource);
	}

	public void remove() {
		tdbResource.removeAll(FEDLCA.personName);
		tdbResource.removeAll(FEDLCA.affiliation);
		tdbResource.removeAll(FEDLCA.email);
		tdbResource.removeAll(FEDLCA.voicePhone);
	}

	public static Resource getRdfclass() {
		return rdfClass;
	}

	public Resource getTdbResource() {
		return tdbResource;
	}

	public void setTdbResource(Resource tdbResource) {
		this.tdbResource = tdbResource;
	}
}
