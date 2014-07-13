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
		RDFNode rdfNode;
		rdfNode = tdbResource.getProperty(RDFS.label).getObject();

		if (rdfNode == null) {
			dataSourceName = DataSourceKeeper.uniquify("unkownName");
		} else {
			dataSourceName = ActiveTDB.getStringFromLiteral(rdfNode);
		}
		
		rdfNode = tdbResource.getProperty(DCTerms.hasVersion).getObject();
		if (rdfNode == null) {
			version = "";
		} else {
			version = ActiveTDB.getStringFromLiteral(rdfNode);
		}

		rdfNode = tdbResource.getProperty(RDFS.comment).getObject();
		if (rdfNode == null) {
			comments = "";
		} else {
			comments = ActiveTDB.getStringFromLiteral(rdfNode);
		}

		if (version == null) {
			version = "";
		}

//		rdfNode = tdbResource.getProperty(FEDLCA.hasContactPerson).getObject();
//		if (rdfNode != null){
//			Person person = PersonKeeper.getPersonByTdbResource(rdfNode.asResource());
//			if (person != null){
//				person.syncDataFromTDB();
//				this.contactPerson = person;
//			}
//		}

//		NodeIterator nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.containsFile);
//		while (nodeIterator.hasNext()) {
//			RDFNode fileMDResource = nodeIterator.next();
//			if (!fileMDResource.isLiteral()) {
//				FileMD fileMD = new FileMD(fileMDResource.asResource());
//				addFileMD(fileMD);
//			}
//		}
	}
}
