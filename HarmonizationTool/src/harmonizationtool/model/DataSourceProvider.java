package harmonizationtool.model;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FEDLCA;
import harmonizationtool.vocabulary.LCAHT;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
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
		this.tdbResource = ActiveTDB.createResource(rdfClass);
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
		if (contactPerson == null) {
			return;
		}
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(FEDLCA.hasContactPerson);
			tdbResource.addProperty(FEDLCA.hasContactPerson, contactPerson.getTdbResource());
			ActiveTDB.tdbDataset.commit();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public Resource getTdbResource() {
//		if (tdbResource == null) {
//			// --- BEGIN SAFE -WRITE- TRANSACTION ---
//			ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
//			try {
//				tdbResource = ActiveTDB.tdbModel.createResource();
//				ActiveTDB.tdbDataset.commit();
//			} finally {
//				ActiveTDB.tdbDataset.end();
//			}
//			// ---- END SAFE -WRITE- TRANSACTION ---
//		}
		assert tdbResource != null : "tdbResource cannot be null";
		return tdbResource;
	}

	public void setTdbResource(Resource tdbResource) {
		this.tdbResource = tdbResource;
	}

	public void addFileMD(FileMD fileMD) {
		System.out.println("got to line: 1");
		if (fileMD == null) {
			return;
		}
		System.out.println("got to line: 2");

		fileMDList.add(fileMD);
		System.out.println("got to line: 3");

		if (!ActiveTDB.tdbModel.contains(tdbResource, LCAHT.containsFile, fileMD.getTdbResource())) {
			System.out.println("got to line: 4");

			// --- BEGIN SAFE -WRITE- TRANSACTION ---
			ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
			try {
				tdbResource.addProperty(LCAHT.containsFile, fileMD.getTdbResource());
				System.out.println("got to line: 5");

				ActiveTDB.tdbDataset.commit();
				System.out.println("got to line: 6");

			} finally {
				System.out.println("got to line: 7");

				ActiveTDB.tdbDataset.end();
				System.out.println("got to line: 8");

			}
			System.out.println("got to line: 9");

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
		// fileMD.remove(); -- BETTER NOT REMOVE THIS IN CASE SOME OTHER DATASOURCE HAS THIS FILE
		fileMDList.remove(fileMD);
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		try {
			ActiveTDB.tdbModel.remove(tdbResource, LCAHT.containsFile, fileMD.getTdbResource());
			ActiveTDB.tdbDataset.commit();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public void removeFileMDList() {
		// for (FileMD fileMD : fileMDList) { -- BETTER NOT REMOVE THIS IN CASE SOME OTHER DATASOURCE HAS THIS FILE
		// fileMD.remove();
		// }
		fileMDList = null;
	}

	public void remove() {
		removeFileMDList();
		// contactPerson.remove();
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(FEDLCA.hasContactPerson);
			tdbResource.removeAll(RDFS.label);
			tdbResource.removeAll(RDFS.comment);
			tdbResource.removeAll(DCTerms.hasVersion);
			ActiveTDB.tdbModel.remove(tdbResource, RDF.type, ECO.DataSource);
			ActiveTDB.tdbDataset.commit();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
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
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(RDFS.label);
			tdbResource.addProperty(RDFS.label, dataSourceName);
			// ActiveTDB.replaceLiteral(tdbResource, RDFS.label, dataSourceName);
			ActiveTDB.tdbDataset.commit();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(DCTerms.hasVersion);
			tdbResource.addProperty(DCTerms.hasVersion, version);
			// ActiveTDB.replaceLiteral(tdbResource, DCTerms.hasVersion, version);
			ActiveTDB.tdbDataset.commit();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(RDFS.comment);
			tdbResource.addProperty(RDFS.comment, comments);
			// ActiveTDB.replaceLiteral(tdbResource, RDFS.comment, comments);
			ActiveTDB.tdbDataset.commit();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
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

		rdfNode = tdbResource.getProperty(FEDLCA.hasContactPerson).getObject();
		if (rdfNode == null) {
			contactPerson = null;
		} else {
			contactPerson = new Person(rdfNode.asResource());
		}

		StmtIterator stmtIterator = tdbResource.listProperties(LCAHT.containsFile);
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
			rdfNode = statement.getObject();
			int fileMDIndex = FileMDKeeper.getIndexByTdbResource(rdfNode.asResource());
			System.out.println("file Index: " + fileMDIndex);
			if (fileMDIndex > -1) {
				addFileMD(FileMDKeeper.get(fileMDIndex));
			} else {
				FileMD fileMD = new FileMD(rdfNode.asResource());
				addFileMD(fileMD);
			}
		}
	}

	public static Resource getRdfclass() {
		return rdfClass;
	}
}
