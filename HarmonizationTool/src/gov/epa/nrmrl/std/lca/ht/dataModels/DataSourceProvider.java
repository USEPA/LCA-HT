package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;
import gov.epa.nrmrl.std.lca.ht.vocabulary.OpenLCA;

import java.util.ArrayList;
//import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class DataSourceProvider {
	private static final Resource rdfClass = ECO.DataSource;

	private String dataSourceName;
	private String version = "";
	private String comments = "";
	private Person contactPerson = null;
	private List<FileMD> fileMDList = new ArrayList<FileMD>();
	// private List<AnnotationProvider> annotationList = new ArrayList<AnnotationProvider>();
	private Resource tdbResource;
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
		String noString = null;
		remove(noString);
	}

	/**
	 * This method is intended to remove a DataSourceProvider including
	 * 1) The DataSourceProvider itself
	 * 2) The TDB objects associated with the ECO.DataSource
	 * 3) Each TDB subject having a ECO.hasDataSource of that ECO.DataSource
	 * 4) Each triple beginning with the subject mentioned in 3)
	 * 
	 * Note: It should be adjusted to consider other triples
	 * 
	 * @param graph String representing the graph from which the ECO.DataSource should be removed
	 */
	public void remove(String graph) {
		removeFileMDList();
		List<Statement> removeStatements = new ArrayList<Statement>();

		// --- BEGIN SAFE -READ- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.getModel(graph);
		Selector selector0 = new SimpleSelector(tdbResource, null, null, null);
		StmtIterator stmtIterator0 = tdbModel.listStatements(selector0);
		while (stmtIterator0.hasNext()) {
			removeStatements.add(stmtIterator0.next());
		}
		Selector selector1 = new SimpleSelector(null, ECO.hasDataSource, tdbResource.asNode());
		StmtIterator stmtIterator1 = tdbModel.listStatements(selector1);
		while (stmtIterator1.hasNext()) {
			Statement statement = stmtIterator1.next();
			removeStatements.add(statement);
			Selector selector2 = new SimpleSelector(statement.getSubject(), null, null, null);
			StmtIterator stmtIterator2 = tdbModel.listStatements(selector2);
			while (stmtIterator2.hasNext()) {
				removeStatements.add(stmtIterator2.next());
			}
		}
		ActiveTDB.tdbDataset.end();
		// --- END SAFE -READ- TRANSACTION ---

		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		tdbModel = ActiveTDB.getModel(graph);
		try {
			for (Statement statement : removeStatements) {
				tdbModel.remove(statement);
			}
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
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

	/**
	 * Items that need to be synced to the Java object from the TDB (using Resource tdbResource) include
	 * String dataSourceName <= RDFS.label
	 * String version <= DCTerms.hasVersion
	 * String comments <= RDFS.comment
	 * Person contactPerson <= FedLCA.hasContactPerson
	 * List<FileMD> fileMDList <= LCAHT.containsFile
	 * Integer referenceDataStatus <= [if belongs to class LCAHT.MasterDataset or LCAHT.SupplementaryReferenceDataset]

	 * @param String indicating which graph is to be used to sync from
	 * @return True if succeeded, False if failed.
	 */
	public boolean syncFromTDB(String graphName) {
		if (tdbResource == null) {
			return false;
		}
		Resource personResource = null;
		List<Resource> fileMDResources = new ArrayList<Resource>();

		// --- BEGIN SAFE -READ- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.getModel(graphName);
		if (!tdbModel.containsResource(tdbResource)) {
			return false;
		}
		Selector selector = new SimpleSelector(tdbResource, null, null, null);
		StmtIterator stmtIterator = tdbModel.listStatements(selector);
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
			Property property = statement.getPredicate();
			if (property.equals(RDFS.label)) {
				dataSourceName = statement.getObject().asLiteral().getString();
			} else if (property.equals(RDFS.comment)) {
				comments = statement.getObject().asLiteral().getString();
			} else if (property.equals(DCTerms.hasVersion)) {
				version = statement.getObject().asLiteral().getString();
			} else if (property.equals(FedLCA.hasContactPerson)) {
				personResource = statement.getObject().asResource();
			} else if (property.equals(LCAHT.containsFile)) {
				fileMDResources.add(statement.getObject().asResource());
			} else if (property.equals(RDF.type)) {
				Resource type = statement.getObject().asResource();
				if (type.equals(LCAHT.MasterDataset) || type.equals(LCAHT.SupplementaryReferenceDataset)) {
					referenceDataStatus = 1;
				}
			}
		}
		ActiveTDB.tdbDataset.end();

		// ---- END SAFE -READ- TRANSACTION ---

		// FIXME - TONY HOWARD - PUT A BREAK POINT JUST BELOW HERE, THEN RUN THE HT
		contactPerson = new Person(personResource);
		for (Resource fileMDResource : fileMDResources) {
			FileMD fileMD = new FileMD(fileMDResource);
			fileMDList.add(fileMD);
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
		if (referenceDataStatus == null) {
			ActiveTDB.tsRemoveStatement(tdbResource, RDF.type, LCAHT.MasterDataset);
			ActiveTDB.tsRemoveStatement(tdbResource, RDF.type, LCAHT.SupplementaryReferenceDataset);
			return;
		}
		if (referenceDataStatus == 1) {
			ActiveTDB.tsRemoveStatement(tdbResource, RDF.type, LCAHT.SupplementaryReferenceDataset);
			ActiveTDB.tsAddGeneralTriple(tdbResource, RDF.type, LCAHT.MasterDataset, null);
			return;
		}
		if (referenceDataStatus == 1) {
			ActiveTDB.tsRemoveStatement(tdbResource, RDF.type, LCAHT.MasterDataset);
			ActiveTDB.tsAddGeneralTriple(tdbResource, RDF.type, LCAHT.SupplementaryReferenceDataset, null);
		}
	}
}
