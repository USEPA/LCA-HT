package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
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
	private Integer referenceDataStatus = null;

	// private boolean isMaster = false;

	public DataSourceProvider() {
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
		boolean success = DataSourceKeeper.add(this);
		System.out.println("Success: " + success + " with this.dataSourceName");
		DataSourceKeeper.add(this);
	}

	public DataSourceProvider(Resource tdbResource) {
		if (DataSourceKeeper.getByTdbResource(tdbResource) < 0) {
			if (tdbResource == null) {
				this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
			} else {
				this.tdbResource = tdbResource;
			}
			boolean synced = syncFromTDB(null);
			if (!synced) {
				synced = syncFromTDB(ActiveTDB.importGraphName);
			}
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
		ActiveTDB.tsReplaceObject(tdbResource, FedLCA.hasContactPerson, contactPerson.getTdbResource());
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
		// Model tdbModel = ActiveTDB.getModel();
		// if (!tdbModel.contains(tdbResource, LCAHT.containsFile, fileMD.getTdbResource())) {
		ActiveTDB.tsAddGeneralTriple(tdbResource, LCAHT.containsFile, fileMD.getTdbResource(), null);
		// }
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
		ActiveTDB.tsRemoveGenericTriple(tdbResource, RDF.type, null, null);
		ActiveTDB.tsRemoveGenericTriple(tdbResource, RDFS.label, null, null);
		ActiveTDB.tsRemoveGenericTriple(tdbResource, DCTerms.hasVersion, null, null);
		ActiveTDB.tsRemoveGenericTriple(tdbResource, RDFS.comment, null, null);
		ActiveTDB.tsRemoveGenericTriple(tdbResource, FedLCA.hasContactPerson, null, null);
		ActiveTDB.tsRemoveGenericTriple(tdbResource, LCAHT.containsFile, null, null);
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
		if (version == null) {
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
		if (comments == null) {
			return "";
		}
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
		ActiveTDB.tsReplaceLiteral(tdbResource, RDFS.comment, comments);
	}

	public boolean syncFromTDB(String graphName) {
		RDFNode rdfNode = null;
		if (tdbResource == null) {
			return false;
		}
		// --- BEGIN SAFE -READ- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.getModel(graphName);
		try {
			if (!tdbModel.containsResource(tdbResource)) {
				return false;
			}
			NodeIterator nodeIterator = tdbModel.listObjectsOfProperty(tdbResource, RDFS.label);
			if (nodeIterator.hasNext()) {
				rdfNode = nodeIterator.next();
			}
			if (rdfNode == null) {
				dataSourceName = DataSourceKeeper.uniquify("unknownName");
			} else {
				dataSourceName = rdfNode.asLiteral().getString();
			}
			if (tdbModel.contains(tdbResource, RDF.type, LCAHT.MasterDataset)) {
				referenceDataStatus = 1;
			} else if (tdbModel.contains(tdbResource, RDF.type, LCAHT.SupplementaryReferenceDataset)) {
				referenceDataStatus = 1;
			} else {
				referenceDataStatus = null;
			}
		} catch (Exception e) {
			System.out.println("Syncing of dataset failed with Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -READ- TRANSACTION ---
		
		ActiveTDB.tsReplaceLiteral(tdbResource, RDFS.label, dataSourceName);

		if (tdbResource.hasProperty(RDFS.comment)) {
			rdfNode = tdbResource.getProperty(RDFS.comment).getObject();
			if (rdfNode != null) {
				comments = ActiveTDB.getStringFromLiteral(rdfNode);
			}
		}
		if (tdbResource.hasProperty(FedLCA.hasContactPerson)) {
			rdfNode = tdbResource.getProperty(FedLCA.hasContactPerson).getObject();
			if (rdfNode != null) {
				contactPerson = new Person(rdfNode.asResource());
			}
		}

		StmtIterator stmtIterator = tdbResource.listProperties(LCAHT.containsFile);
		System.out.println("stmtIterator.toList() = " + stmtIterator.toList());

		while (stmtIterator.hasNext()) {
			// ================= ?BUG IN JENA? USING THE LINE BELOW TO KEEP STABILITY
			System.out.println("stmtIterator.toList() = " + stmtIterator.toList());
			// SOMETHING ABOUT THE ABOVE STATEMENT PREVENTS THE TDB AND ITERATOR FROM SCREWING UP

			// Statement statement = stmtIterator.next(); <== SOMETHING WRONG WITH COUNTER IF YOU USE THIS
			Statement statement = stmtIterator.nextStatement();

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
		return true;
	}

	public static Resource getRdfclass() {
		return rdfClass;
	}

	public Integer getReferenceDataStatus() {
		return referenceDataStatus;
	}

	public void setReferenceDataStatus(Integer referenceDataStatus) {
		this.referenceDataStatus = referenceDataStatus;
		ActiveTDB.tsRemoveStatement(tdbResource, RDF.type, LCAHT.MasterDataset);
		ActiveTDB.tsRemoveStatement(tdbResource, RDF.type, LCAHT.SupplementaryReferenceDataset);
		if (referenceDataStatus != null) {
			if (referenceDataStatus == 2) {
				ActiveTDB.tsAddGeneralTriple(tdbResource, RDF.type, LCAHT.SupplementaryReferenceDataset, null);
			} else {
				ActiveTDB.tsAddGeneralTriple(tdbResource, RDF.type, LCAHT.SupplementaryReferenceDataset, null);
			}
		}
	}
}
