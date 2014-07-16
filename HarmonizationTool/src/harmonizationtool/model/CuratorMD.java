package harmonizationtool.model;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.utils.Util;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FEDLCA;

public class CuratorMD {
	private String curatorName;
	private String curatorAffiliation;
	private String curatorEmail;
	private String curatorPhone;
	private Resource tdbResource;
	private static final Model model = ActiveTDB.tdbModel;

	public CuratorMD(Resource tdbResource) {
		this.tdbResource = tdbResource;
		model.add(tdbResource, RDF.type, FEDLCA.Curator);
	}

	public CuratorMD(String name, String affiliation, String email, String phone) {
		super();
		this.curatorName = name;
		this.curatorAffiliation = affiliation;
		this.curatorEmail = email;
		this.curatorPhone = phone;
		this.tdbResource = model.createResource();
		model.add(tdbResource, RDF.type, FEDLCA.Person);
		model.add(tdbResource, RDF.type, FEDLCA.Curator);
		model.add(this.tdbResource, FEDLCA.personName, model.createTypedLiteral(this.curatorName));
		model.add(this.tdbResource, FEDLCA.affiliation, model.createTypedLiteral(this.curatorAffiliation));
		model.add(this.tdbResource, FEDLCA.email, model.createTypedLiteral(this.curatorEmail));
		model.add(this.tdbResource, FEDLCA.voicePhone, model.createTypedLiteral(this.curatorPhone));
	}

	public CuratorMD() {
		tdbResource = model.createResource();
		model.add(tdbResource, RDF.type, FEDLCA.Person);
		model.add(tdbResource, RDF.type, FEDLCA.Curator);
	}

	public CuratorMD(boolean preferences) {
		if (preferences) {
			setName(Util.getPreferenceStore().getString("curatorName"));
			setAffiliation(Util.getPreferenceStore().getString("curatorAffiliation"));
			setEmail(Util.getPreferenceStore().getString("curatorEmail"));
			setPhone(Util.getPreferenceStore().getString("curatorPhone"));
		}
	}

	public String getName() {
		if (curatorName == null) {
			curatorName = "";
		}
		return curatorName;
	}

	public void setName(String name) {
		this.curatorName = name;
		model.add(this.tdbResource, FEDLCA.personName, model.createTypedLiteral(this.curatorName));
	}

	public String getAffiliation() {
		if (curatorAffiliation == null) {
			curatorAffiliation = "";
		}
		return curatorAffiliation;
	}

	public void setAffiliation(String affiliation) {
		this.curatorAffiliation = affiliation;
		model.add(this.tdbResource, FEDLCA.affiliation, model.createTypedLiteral(this.curatorAffiliation));
	}

	public String getEmail() {
		if (curatorEmail == null) {
			curatorEmail = "";
		}
		return curatorEmail;
	}

	public void setEmail(String email) {
		this.curatorEmail = email;
		model.add(this.tdbResource, FEDLCA.email, model.createTypedLiteral(this.curatorEmail));
	}

	public String getPhone() {
		if (curatorPhone == null) {
			curatorPhone = "";
		}
		return curatorPhone;
	}

	public void setPhone(String phone) {
		this.curatorPhone = phone;
		model.add(this.tdbResource, FEDLCA.voicePhone, model.createTypedLiteral(this.curatorPhone));
	}

	public void remove() {
		tdbResource.removeAll(FEDLCA.personName);
		tdbResource.removeAll(FEDLCA.affiliation);
		tdbResource.removeAll(FEDLCA.email);
		tdbResource.removeAll(FEDLCA.voicePhone);
	}
}
