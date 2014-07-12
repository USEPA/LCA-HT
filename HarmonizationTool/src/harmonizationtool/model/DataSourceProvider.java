package harmonizationtool.model;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FEDLCA;
import harmonizationtool.vocabulary.LCAHT;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class DataSourceProvider {
	private String dataSourceName;
	private String version = "";
	private String comments = "";
	private Person contactPerson;
	private List<FileMD> fileMDList = new ArrayList<FileMD>();
	// private List<Annotation> annotationList = new ArrayList<Annotation>();

	private Resource tdbResource;
	protected final static Model model = ActiveTDB.model;
	private boolean isMaster = false;

	public DataSourceProvider() {
		tdbResource = model.createResource();
		model.add(tdbResource, RDF.type, ECO.DataSource);
	}

	public DataSourceProvider(Resource tdbResource) {
		this.tdbResource = tdbResource;
		syncFromTDB();
		if (DataSourceKeeper.getByTdbResource(tdbResource) < 0) {
			DataSourceKeeper.add(this);
		}
	}

	public Person getContactPerson() {
		return contactPerson;
	}

	public void setContactPerson(Person contactPerson) {
		this.contactPerson = contactPerson;
		tdbResource.removeAll(FEDLCA.hasContactPerson);
		tdbResource.addProperty(FEDLCA.hasContactPerson, contactPerson.getTdbResource());
	}

	public Resource getTdbResource() {
		if (tdbResource == null) {
			tdbResource = ActiveTDB.model.createResource();
		}
		assert tdbResource != null : "tdbResource cannot be null";
		return tdbResource;
	}

	public void setTdbResource(Resource tdbResource) {
		this.tdbResource = tdbResource;
	}

	public void addFileMD(FileMD fileMD) {
		fileMDList.add(fileMD);
		tdbResource.addProperty(LCAHT.containsFile, fileMD.getTdbResource());
	}

	public List<FileMD> getFileMDList() {
		return fileMDList;
	}

	public void remove(FileMD fileMD) {
		fileMD.remove();
		fileMDList.remove(fileMD);
		ActiveTDB.model.remove(tdbResource, LCAHT.containsFile, fileMD.getTdbResource());
	}

	public void removeFileMDList() {
		for (FileMD fileMD : fileMDList) {
			fileMD.remove();
		}
		fileMDList = null;
	}

	public void remove() {
		removeFileMDList();
		contactPerson.remove();
		tdbResource.removeAll(FEDLCA.hasContactPerson);
		tdbResource.removeAll(RDFS.label);
		tdbResource.removeAll(RDFS.comment);
		tdbResource.removeAll(DCTerms.hasVersion);
		model.remove(tdbResource, RDF.type, ECO.DataSource);
	}

	public boolean isMaster() {
		return isMaster;
	}

	public void setMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
		ActiveTDB.replaceLiteral(tdbResource, RDFS.label, dataSourceName);
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
		ActiveTDB.replaceLiteral(tdbResource, DCTerms.hasVersion, version);
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
		ActiveTDB.replaceLiteral(tdbResource, RDFS.comment, comments);
	}

	public void syncFromTDB() {
		List<Statement> labelStatements = tdbResource.listProperties(RDFS.label).toList();
		if (labelStatements.size() > 0) {
			dataSourceName = ActiveTDB.getStringFromLiteral(labelStatements.get(0).getObject());
			if (dataSourceName == null) {
				return;
			}
			// RDFNode rdfNode = labelStatements.get(0).getObject();
			// if (rdfNode.isLiteral()) {
			// Object value = rdfNode.asLiteral().getValue();
			// if (value instanceof String) {
			// dataSourceName = (String) value;
			// }
			// }
		}

		labelStatements = tdbResource.listProperties(RDFS.comment).toList();
		if (labelStatements.size() > 0) {
			comments = ActiveTDB.getStringFromLiteral(labelStatements.get(0).getObject());
		}
		if (comments == null) {
			comments = "";
		}

		labelStatements = tdbResource.listProperties(DCTerms.hasVersion).toList();
		if (labelStatements.size() > 0) {
			version = ActiveTDB.getStringFromLiteral(labelStatements.get(0).getObject());
//			version = labelStatements.get(0).getObject().toString();
		}
		if (version == null) {
			version = "";
		}

		NodeIterator nodeIterator = model.listObjectsOfProperty(tdbResource, FEDLCA.hasContactPerson);
		while (nodeIterator.hasNext()) {
			RDFNode contactPersonNode = nodeIterator.next();
			if (!contactPersonNode.isLiteral()) {
				Person person = new Person(contactPersonNode.asResource());
				contactPerson = person;
			}
		}

		nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.containsFile);
		while (nodeIterator.hasNext()) {
			RDFNode fileMDResource = nodeIterator.next();
			if (!fileMDResource.isLiteral()) {
				FileMD fileMD = new FileMD(fileMDResource.asResource());
				addFileMD(fileMD);
			}
		}
	}
}
