package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FEDLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class DataSourceProvider {
	private String dataSourceName;
	private String version = "";
	private String comments = "";
	private Person contactPerson = null;
	private List<FileMD> fileMDList = new ArrayList<FileMD>();
	// private List<Annotation> annotationList = new ArrayList<Annotation>();
	private Resource tdbResource;
	private static final Resource rdfClass = ECO.DataSource;

	private boolean isMaster = false;

	public DataSourceProvider() {
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
		boolean success = DataSourceKeeper.add(this);
		System.out.println("Success: " + success + " with this.dataSourceName");
		DataSourceKeeper.add(this);
	}

	public DataSourceProvider(Resource tdbResource) {
		if (DataSourceKeeper.getByTdbResource(tdbResource) < 0) {
			this.tdbResource = tdbResource;
			syncFromTDB();
			DataSourceKeeper.add(this);
		}
	}

	public Person getContactPerson() {
		return contactPerson;
	}

	public void setContactPerson(Person contactPerson) {
		this.contactPerson = contactPerson;
		if (contactPerson == null) {
			return;
		}
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		try {
			Resource tdbResource = ActiveTDB.tdbDataset.getDefaultModel().createResource(rdfClass);
			tdbResource.removeAll(FEDLCA.hasContactPerson);
			tdbResource.addProperty(FEDLCA.hasContactPerson, contactPerson.getTdbResource());
			ActiveTDB.tdbDataset.commit();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public Resource getTdbResource() {

		return tdbResource;
	}

	public void setTdbResource(Resource tdbResource) {
		this.tdbResource = tdbResource;
	}

	public void addFileMD(FileMD fileMD) {
		if (fileMD == null) {
			return;
		}

		fileMDList.add(fileMD);

		if (!ActiveTDB.tdbModel.contains(tdbResource, LCAHT.containsFile, fileMD.getTdbResource())) {
			// ActiveTDB.addAnything(tdbResource, LCAHT.containsFile,
			// fileMD.getTdbResource());
			// THE ABOVE SHOULD BE ADDED: TO SAFELY ADD A TRIPLE WHETHER THE
			// OBJECT IS AN RDFNode, A Literal, OR AN
			// OBJECT (TO BE TYPED)

			// --- BEGIN SAFE -WRITE- TRANSACTION ---
			ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
			try {
				Resource tdbResource = ActiveTDB.tdbDataset.getDefaultModel().createResource(rdfClass);
				tdbResource.addProperty(LCAHT.containsFile, fileMD.getTdbResource());
				ActiveTDB.tdbDataset.commit();
			} finally {
				ActiveTDB.tdbDataset.end();
			}
			// ---- END SAFE -WRITE- TRANSACTION ---
		}
	}

	public List<FileMD> getFileMDList() {
		return fileMDList;
	}

	public List<FileMD> getFileMDListNewestFirst() {
		List<FileMD> results = new ArrayList<FileMD>();
		for (FileMD fileMD : fileMDList) {
			Date readDate = fileMD.getReadDate();
			int index = results.size();
			for (FileMD newFileMD : results) {
				Date newReadDate = newFileMD.getReadDate();
				if (readDate.after(newReadDate)) {
					index = results.indexOf(newFileMD);
					break;
				}
			}
			results.add(index, fileMD);
		}
		return results;
	}

	public List<FileMD> getFileMDListOldestFirst() {
		List<FileMD> results = new ArrayList<FileMD>();
		for (FileMD fileMD : fileMDList) {
			Date readDate = fileMD.getReadDate();
			int index = results.size();
			for (FileMD newFileMD : results) {
				Date newReadDate = newFileMD.getReadDate();
				if (readDate.before(newReadDate)) {
					index = results.indexOf(newFileMD);
					break;
				}
			}
			results.add(index, fileMD);
		}
		return results;
	}

	public void remove(FileMD fileMD) {
		// fileMD.remove(); -- BETTER NOT REMOVE THIS IN CASE SOME OTHER
		// DATASOURCE HAS THIS FILE
		fileMDList.remove(fileMD);
		ActiveTDB.tsRemoveStatement(tdbResource, LCAHT.containsFile, fileMD.getTdbResource());
	}

	public void removeFileMDList() {
		// for (FileMD fileMD : fileMDList) { -- BETTER NOT REMOVE THIS IN CASE
		// SOME OTHER DATASOURCE HAS THIS FILE
		// fileMD.remove();
		// }
		fileMDList = null;
	}

	public void remove() {
		removeFileMDList();
		ActiveTDB.tsRemoveAllObjects(tdbResource, RDF.type);
		ActiveTDB.tsRemoveAllObjects(tdbResource, RDFS.label);
		ActiveTDB.tsRemoveAllObjects(tdbResource, DCTerms.hasVersion);
		ActiveTDB.tsRemoveAllObjects(tdbResource, RDFS.comment);
		ActiveTDB.tsRemoveAllObjects(tdbResource, FEDLCA.hasContactPerson);
		ActiveTDB.tsRemoveAllObjects(tdbResource, LCAHT.containsFile);
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
	
	public String getDataSourceNameString() {
		if (dataSourceName == null) {
			return "<NO ASSIGNED NAME>";
		}
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
		ActiveTDB.tsReplaceLiteral(tdbResource, RDFS.label, dataSourceName);
	}

	public String getVersion() {
		return version;
	}
	
	public String getVersionString() {
		if (version == null){
			return "";
		}
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
		ActiveTDB.tsReplaceLiteral(tdbResource, DCTerms.hasVersion, version);
	}

	public String getComments() {
		return comments;
	}
	
	public String getCommentsString() {
		if (comments == null){
			return "";
		}
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
		ActiveTDB.tsReplaceLiteral(tdbResource, RDFS.comment, comments);
	}

	public void syncFromTDB() {
		RDFNode rdfNode;

		if (tdbResource.hasProperty(RDFS.label)) {
			rdfNode = tdbResource.getProperty(RDFS.label).getObject();
			if (rdfNode == null) {
				dataSourceName = DataSourceKeeper.uniquify("unkownName");
			} else {
				dataSourceName = ActiveTDB.getStringFromLiteral(rdfNode);
			}

		} else {
			dataSourceName = DataSourceKeeper.uniquify("unkownName");
			ActiveTDB.tsReplaceLiteral(tdbResource, RDFS.label, dataSourceName);
		}

		if (tdbResource.hasProperty(DCTerms.hasVersion)) {
			rdfNode = tdbResource.getProperty(DCTerms.hasVersion).getObject();
			if (rdfNode != null) {
				version = ActiveTDB.getStringFromLiteral(rdfNode);
			}
		}

		if (tdbResource.hasProperty(RDFS.comment)) {
			rdfNode = tdbResource.getProperty(RDFS.comment).getObject();
			if (rdfNode != null) {
				comments = ActiveTDB.getStringFromLiteral(rdfNode);
			}
		}
		if (tdbResource.hasProperty(FEDLCA.hasContactPerson)) {
			rdfNode = tdbResource.getProperty(FEDLCA.hasContactPerson).getObject();
			if (rdfNode != null) {
				contactPerson = new Person(rdfNode.asResource());
			}
		}

		StmtIterator stmtIterator = tdbResource.listProperties(LCAHT.containsFile);
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
			rdfNode = statement.getObject();
			int fileMDIndex = FileMDKeeper.getIndexByTdbResource(rdfNode.asResource());
			System.out.println("file Index: " + fileMDIndex);
			FileMD fileMD;
			if (fileMDIndex > -1) {
				fileMD = FileMDKeeper.get(fileMDIndex);
			} else {
				fileMD = new FileMD(rdfNode.asResource());
			}
			addFileMD(fileMD);
		}
	}

	public static Resource getRdfclass() {
		return rdfClass;
	}
}
