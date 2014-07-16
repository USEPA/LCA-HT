package harmonizationtool.model;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FEDLCA;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class Person {
	private String name;
	private String affiliation;
	private String email;
	private String phone;
	private static final Resource rdfClass = ECO.Person;
	private Resource tdbResource;
	protected final static Model model = ActiveTDB.tdbModel;

	public Person() {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		try {
			this.tdbResource = model.createResource();
			this.tdbResource.addProperty(RDF.type, rdfClass);
			ActiveTDB.tdbDataset.commit();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		PersonKeeper.add(this);
	}

	public Person(Resource tdbResource) {
		this.tdbResource = tdbResource;
		syncDataFromTDB();
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
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(FEDLCA.personName);
			tdbResource.removeAll(FEDLCA.affiliation);
			tdbResource.removeAll(FEDLCA.email);
			tdbResource.removeAll(FEDLCA.voicePhone);

			ActiveTDB.tdbDataset.commit();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
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
