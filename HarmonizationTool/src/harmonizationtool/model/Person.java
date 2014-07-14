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
	protected final static Model model = ActiveTDB.model;

	public Person() {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			this.tdbResource = model.createResource();
			this.tdbResource.addProperty(RDF.type, ECO.Person);
			ActiveTDB.TDBDataset.commit();
		} finally {
			ActiveTDB.TDBDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		PersonKeeper.add(this);
	}

	public Person(Resource tdbResource) {
		this.tdbResource = tdbResource;
		syncDataFromTDB();
		PersonKeeper.add(this);
	}

//	public Person(String name, String affiliation, String email, String phone) {
////		super();
//		// --- BEGIN SAFE -WRITE- TRANSACTION ---
//		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
//		try {
//			this.tdbResource = model.createResource();
//			this.tdbResource.addProperty(RDF.type, ECO.Person);
//			ActiveTDB.TDBDataset.commit();
//		} finally {
//			ActiveTDB.TDBDataset.end();
//		}
//		// ---- END SAFE -WRITE- TRANSACTION ---
//		setName(name);
//		setAffiliation(affiliation);
//		setEmail(email);
//		setPhone(phone);
//		PersonKeeper.add(this);
//	}

	public String getName() {
		if (name == null) {
			name = "";
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(FEDLCA.personName);
			tdbResource.addProperty(FEDLCA.personName, name);
			ActiveTDB.TDBDataset.commit();
		} finally {
			ActiveTDB.TDBDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---

		// ActiveTDB.replaceLiteral(tdbResource, FEDLCA.personName,
		// XSDDatatype.XSDstring, name);
	}

	public String getAffiliation() {
		if (affiliation == null) {
			affiliation = "";
		}
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(FEDLCA.affiliation);
			tdbResource.addProperty(FEDLCA.affiliation, affiliation);
			ActiveTDB.TDBDataset.commit();
		} finally {
			ActiveTDB.TDBDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		// ActiveTDB.replaceLiteral(tdbResource, FEDLCA.affiliation,
		// XSDDatatype.XSDstring, affiliation);
	}

	public String getEmail() {
		if (email == null) {
			email = "";
		}
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(FEDLCA.email);
			tdbResource.addProperty(FEDLCA.email, email);
			ActiveTDB.TDBDataset.commit();
		} finally {
			ActiveTDB.TDBDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		// ActiveTDB.replaceLiteral(tdbResource, FEDLCA.email,
		// XSDDatatype.XSDstring, email);
	}

	public String getPhone() {
		if (phone == null) {
			phone = "";
		}
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(FEDLCA.voicePhone);
			tdbResource.addProperty(FEDLCA.voicePhone, phone);
			ActiveTDB.TDBDataset.commit();
		} finally {
			ActiveTDB.TDBDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		// ActiveTDB.replaceLiteral(tdbResource, FEDLCA.voicePhone,
		// XSDDatatype.XSDstring, phone);
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
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(FEDLCA.personName);
			tdbResource.removeAll(FEDLCA.affiliation);
			tdbResource.removeAll(FEDLCA.email);
			tdbResource.removeAll(FEDLCA.voicePhone);

			ActiveTDB.TDBDataset.commit();
		} finally {
			ActiveTDB.TDBDataset.end();
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
