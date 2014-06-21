package harmonizationtool.model;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FEDLCA;

import java.util.Date;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class DataSourceMD {
	private String name;
	private String version;
	private String comments;
	private String contactName;
	private String contactAffiliation;
	private String contactEmail;
	private String contactPhone;
//	private Resource tdbResource;
	private static final Model model = ActiveTDB.model;

	public DataSourceMD() {
//		this.tdbResource = model.createResource();
//		model.add(tdbResource, RDF.type, ECO.DataSource);
	}

	public DataSourceMD(Resource tdbResource) {
//		this.tdbResource = tdbResource;
//		model.add(tdbResource, RDF.type, ECO.DataSource);
	}

	public DataSourceMD(String name, String version, String comments, String contactName, String contactAffiliation,
			String contactEmail, String contactPhone) {
		super();
		this.name = name;
		this.version = version;
		this.comments = comments;
		this.contactName = contactName;
		this.contactAffiliation = contactAffiliation;
		this.contactEmail = contactEmail;
		this.contactPhone = contactPhone;
//		this.tdbResource = model.createResource();
//		model.add(tdbResource, RDF.type, ECO.DataSource);
//		model.add(this.tdbResource, RDFS.label, model.createTypedLiteral(this.name));
//		model.add(this.tdbResource, DCTerms.hasVersion, model.createTypedLiteral(this.version));
//		model.add(this.tdbResource, RDFS.comment, model.createTypedLiteral(this.comments));
//		model.add(this.tdbResource, FEDLCA.dataSourceContactName, model.createTypedLiteral(this.contactName));
//		model.add(this.tdbResource, FEDLCA.dataSourceContactAffiliation, model.createTypedLiteral(this.contactAffiliation));
//		model.add(this.tdbResource, FEDLCA.dataSourceContactEmail, model.createTypedLiteral(this.contactEmail));
//		model.add(this.tdbResource, FEDLCA.dataSourceContactPhone, model.createTypedLiteral(this.contactPhone));
	}

	public String getName() {
		if (name == null) {
			name = "";
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
//		model.add(this.tdbResource, RDFS.label, model.createTypedLiteral(this.name));
	}

	public String getVersion() {
		if (version == null) {
			version = "";
		}
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
//		model.add(this.tdbResource, DCTerms.hasVersion, model.createTypedLiteral(this.version));
	}

	public String getComments() {
		if (comments == null) {
			comments = "";
		}
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
//		model.add(this.tdbResource, RDFS.comment, model.createTypedLiteral(this.comments));
	}

	public String getContactName() {
		if (contactName == null) {
			contactName = "";
		}
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
//		model.add(this.tdbResource, FEDLCA.dataSourceContactName, model.createTypedLiteral(this.contactName));
	}

	public String getContactAffiliation() {
		if (contactAffiliation == null) {
			contactAffiliation = "";
		}
		return contactAffiliation;
	}

	public void setContactAffiliation(String contactAffiliation) {
		this.contactAffiliation = contactAffiliation;
//		model.add(this.tdbResource, FEDLCA.dataSourceContactAffiliation, model.createTypedLiteral(this.contactAffiliation));
	}

	public String getContactEmail() {
		if (contactEmail == null) {
			contactEmail = "";
		}
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
//		model.add(this.tdbResource, FEDLCA.dataSourceContactEmail, model.createTypedLiteral(this.contactEmail));
	}

	public String getContactPhone() {
		if (contactPhone == null) {
			contactPhone = "";
		}
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
//		model.add(this.tdbResource, FEDLCA.dataSourceContactPhone, model.createTypedLiteral(this.contactPhone));
	}

//	public void remove() {
//		tdbResource.removeAll(RDFS.label);
//		tdbResource.removeAll(DCTerms.hasVersion);
//		tdbResource.removeAll(RDFS.comment);
//		tdbResource.removeAll(FEDLCA.dataSourceContactName);
//		tdbResource.removeAll(FEDLCA.dataSourceContactAffiliation);
//		tdbResource.removeAll(FEDLCA.dataSourceContactEmail);
//		tdbResource.removeAll(FEDLCA.dataSourceContactPhone);
//	}
}
