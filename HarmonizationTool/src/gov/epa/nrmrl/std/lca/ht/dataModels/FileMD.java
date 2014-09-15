package gov.epa.nrmrl.std.lca.ht.dataModels;


import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.FileEncodingUtil;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;

import java.util.Date;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class FileMD {
	private String filename;
	private String path;
	private String encoding;
	private long byteCount;
	private Date modifiedDate;
	private Date readDate;
	private Resource tdbResource;
	private static final Resource rdfClass = LCAHT.dataFile;

	public FileMD() {
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
		FileMDKeeper.add(this);
	}

	public FileMD(Resource tdbResource) {
		this.tdbResource = tdbResource;
		FileMDKeeper.add(this);
		syncDataFromTDB();
	}

	public String getFilename() {
		return filename;
	}

	public String getFilenameString() {
		if (filename == null) {
			return "";
		}
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
		ActiveTDB.tsReplaceLiteral(tdbResource, LCAHT.fileName, filename);
	}

	public String getPath() {
		return path;
	}

	public String getPathString() {
		if (path == null) {
			return "";
		}
		return path;
	}

	public void setPath(String path) {
		this.path = path;
		ActiveTDB.tsReplaceLiteral(tdbResource, LCAHT.filePath, path);
	}

	public long getByteCount() {
		return byteCount;
	}

	public void setByteCount(long size) {
		this.byteCount = size;
		ActiveTDB.tsReplaceLiteral(tdbResource, LCAHT.byteCount, size);
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
		ActiveTDB.tsReplaceLiteral(tdbResource, LCAHT.fileModifiedDate, modifiedDate);
	}

	public Date getReadDate() {
		return readDate;
	}

	public void setReadDate(Date readDate) {
		this.readDate = readDate;
		ActiveTDB.tsAddLiteral(tdbResource, LCAHT.fileReadDate, readDate);
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		if (!FileEncodingUtil.containsEncoding(encoding)) {
			// WARN THAT THIS ENCODING HAS NOT BEEN SEEN
		}
		this.encoding = encoding;
		ActiveTDB.tsReplaceLiteral(tdbResource, LCAHT.fileEncoding, encoding);
	}

	@Override
	public String toString() {
		return path;
	}

	public Resource getTdbResource() {
		return tdbResource;
	}

	public void setTdbResource(Resource tdbResource) {
		// assert this.tdbResource == null :
		// "Why and how would you change the tdbResource (blank node) for a file?";
		this.tdbResource = tdbResource;
	}

	public void syncDataFromTDB() {
		RDFNode rdfNode;
		if (tdbResource == null) {
			return;
		}

		if (tdbResource.hasProperty(LCAHT.fileName)) {
			rdfNode = tdbResource.getProperty(LCAHT.fileName).getObject();
			if (rdfNode != null) {
				filename = ActiveTDB.getStringFromLiteral(rdfNode);
			}
		}

		if (tdbResource.hasProperty(LCAHT.filePath)) {
			rdfNode = tdbResource.getProperty(LCAHT.filePath).getObject();
			if (rdfNode != null) {
				path = ActiveTDB.getStringFromLiteral(rdfNode);
			}
		}
		if (tdbResource.hasProperty(LCAHT.byteCount)) {
			rdfNode = tdbResource.getProperty(LCAHT.byteCount).getObject();
			if (rdfNode != null) {
				byteCount = rdfNode.asLiteral().getLong();
			}
		}

		if (tdbResource.hasProperty(LCAHT.fileModifiedDate)) {
			rdfNode = tdbResource.getProperty(LCAHT.fileModifiedDate).getObject();
			if (rdfNode != null) {
				modifiedDate = ActiveTDB.getDateFromLiteral(rdfNode.asLiteral());
			}
		}

		if (tdbResource.hasProperty(LCAHT.fileModifiedDate)) {
			rdfNode = tdbResource.getProperty(LCAHT.fileReadDate).getObject();
			if (rdfNode != null) {
				readDate = ActiveTDB.getDateFromLiteral(rdfNode.asLiteral());
			}
		}

		System.out.println("sync line: 8");
	}

	public void remove() {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		try {
			Resource tdbResource = ActiveTDB.tdbDataset.getDefaultModel().createResource(rdfClass);
			tdbResource.removeAll(LCAHT.fileName);
			tdbResource.removeAll(LCAHT.filePath);
			tdbResource.removeAll(LCAHT.fileEncoding);
			tdbResource.removeAll(LCAHT.byteCount);
			tdbResource.removeAll(LCAHT.fileModifiedDate);
			tdbResource.removeAll(LCAHT.fileReadDate);
			ActiveTDB.tdbDataset.commit();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}
}
