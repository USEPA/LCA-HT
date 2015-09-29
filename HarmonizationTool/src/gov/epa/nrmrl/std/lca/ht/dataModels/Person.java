package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class Person {
	private String name;
	private String affiliation;
	private String email;
	private String phone;
	private static final Resource rdfClass = ECO.Person;
	private Resource tdbResource;

	// protected final static Model model = ActiveTDB.tdbModel;

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
		if (name == null) {
			name = "";
		}
		this.name = name;
		ActiveTDB.tsReplaceLiteral(tdbResource, FedLCA.personName, name);
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
		if (affiliation == null) {
			affiliation = "";
		}
		this.affiliation = affiliation;
		ActiveTDB.tsReplaceLiteral(tdbResource, FedLCA.affiliation, affiliation);
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
		if (email == null) {
			email = "";
		}
		this.email = email;
		ActiveTDB.tsReplaceLiteral(tdbResource, FedLCA.email, email);
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
		if (phone == null) {
			phone = "";
		}
		this.phone = phone;
		ActiveTDB.tsReplaceLiteral(tdbResource, FedLCA.voicePhone, phone);
	}

	public void syncDataFromTDB() {
		RDFNode rdfNode;

		if (tdbResource == null) {
			return;
		}

		if (tdbResource.hasProperty(FedLCA.personName)) {
			rdfNode = tdbResource.getProperty(FedLCA.personName).getObject();
			if (rdfNode != null) {
				name = ActiveTDB.getStringFromLiteral(rdfNode);
			}
		}

		if (tdbResource.hasProperty(FedLCA.affiliation)) {
			rdfNode = tdbResource.getProperty(FedLCA.affiliation).getObject();
			if (rdfNode != null) {
				affiliation = ActiveTDB.getStringFromLiteral(rdfNode);
			}
		}

		if (tdbResource.hasProperty(FedLCA.email)) {
			rdfNode = tdbResource.getProperty(FedLCA.email).getObject();
			if (rdfNode != null) {
				email = ActiveTDB.getStringFromLiteral(rdfNode);
			}
		}

		if (tdbResource.hasProperty(FedLCA.voicePhone)) {
			rdfNode = tdbResource.getProperty(FedLCA.voicePhone).getObject();
			if (rdfNode != null) {
				phone = ActiveTDB.getStringFromLiteral(rdfNode);
			}
		}
	}

	public void remove() {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(null);
		try {
			StmtIterator stmtIterator = tdbResource.listProperties();
			while (stmtIterator.hasNext()) {
				tdbModel.remove(stmtIterator.next());
			}
			for (int i = 0; i < DataSourceKeeper.size(); i++) {
				DataSourceProvider dataSourceProvider = DataSourceKeeper.get(i);
				Person person = dataSourceProvider.getContactPerson();
				if (person.equals(this)) {
					tdbModel.remove(dataSourceProvider.getTdbResource(), FedLCA.hasContactPerson, tdbResource);
				}
			}
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
