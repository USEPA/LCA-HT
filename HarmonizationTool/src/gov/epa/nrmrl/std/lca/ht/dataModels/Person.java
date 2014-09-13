package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FEDLCA;

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
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
		PersonKeeper.add(this);
	}

	public Person(Resource tdbResource) {
		this.tdbResource = tdbResource;
		syncDataFromTDB();
		PersonKeeper.add(this);
	}

	public String getName() {
		return name;
	}

	public String getNameString() {
		if (name == null) {
			return "";
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
		ActiveTDB.tsReplaceLiteral(tdbResource, FEDLCA.personName, name);
	}

	public String getAffiliation() {
		return affiliation;
	}

	public String getAffiliationString() {
		if (affiliation == null) {
			return "";
		}
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
		ActiveTDB.tsReplaceLiteral(tdbResource, FEDLCA.affiliation, affiliation);
	}

	public String getEmail() {
		return email;
	}

	public String getEmailString() {
		if (email == null) {
			return "";
		}
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
		ActiveTDB.tsReplaceLiteral(tdbResource, FEDLCA.email, email);
	}

	public String getPhone() {
		return phone;
	}

	public String getPhoneString() {
		if (phone == null) {
			return "";
		}
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
		ActiveTDB.tsReplaceLiteral(tdbResource, FEDLCA.voicePhone, phone);
	}

	public void syncDataFromTDB() {
		RDFNode rdfNode;

		if (tdbResource == null) {
			return;
		}

		if (tdbResource.hasProperty(FEDLCA.personName)) {
			rdfNode = tdbResource.getProperty(FEDLCA.personName).getObject();
			if (rdfNode != null) {
				name = ActiveTDB.getStringFromLiteral(rdfNode);
			}
		}

		if (tdbResource.hasProperty(FEDLCA.affiliation)) {
			rdfNode = tdbResource.getProperty(FEDLCA.affiliation).getObject();
			if (rdfNode != null) {
				affiliation = ActiveTDB.getStringFromLiteral(rdfNode);
			}
		}

		if (tdbResource.hasProperty(FEDLCA.email)) {
			rdfNode = tdbResource.getProperty(FEDLCA.email).getObject();
			if (rdfNode != null) {
				email = ActiveTDB.getStringFromLiteral(rdfNode);
			}
		}

		if (tdbResource.hasProperty(FEDLCA.voicePhone)) {
			rdfNode = tdbResource.getProperty(FEDLCA.voicePhone).getObject();
			if (rdfNode != null) {
				phone = ActiveTDB.getStringFromLiteral(rdfNode);
			}
		}
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
