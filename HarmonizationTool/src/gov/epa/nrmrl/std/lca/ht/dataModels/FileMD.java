package gov.epa.nrmrl.std.lca.ht.dataModels;

//import java.util.Calendar;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.utils.FileEncodingUtil;
import harmonizationtool.vocabulary.LCAHT;

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

	// private static final Model model = ActiveTDB.tdbModel;

	public FileMD() {
		this.tdbResource = ActiveTDB.createResource(rdfClass);
		FileMDKeeper.add(this);
	}

	public FileMD(Resource tdbResource) {
		this.tdbResource = tdbResource;
		FileMDKeeper.add(this);
		syncDataFromTDB();
	}

	// public FileMD(String filename, String path, long size, Date modifiedDate,
	// Date readDate) {
	// super();
	// setFilename(filename);
	// setPath(path);
	// setByteCount(size);
	// setModifiedDate(modifiedDate);
	// setReadDate(readDate);
	// FileMDKeeper.add(this);
	// }

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
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		try {
			this.filename = filename;
			tdbResource.removeAll(LCAHT.fileName);
			tdbResource.addProperty(LCAHT.fileName, filename);
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
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
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(LCAHT.filePath);
			tdbResource.addProperty(LCAHT.filePath, path);
			ActiveTDB.tdbDataset.commit();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public long getByteCount() {
		return byteCount;
	}

	public void setByteCount(long size) {
		this.byteCount = size;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---

		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(LCAHT.byteCount);
			tdbResource.addLiteral(LCAHT.byteCount, byteCount);
			ActiveTDB.tdbDataset.commit();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(LCAHT.fileModifiedDate);
			tdbResource.addLiteral(LCAHT.fileModifiedDate, modifiedDate);
			ActiveTDB.tdbDataset.commit();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public Date getReadDate() {
		return readDate;
	}

	public void setReadDate(Date readDate) {
		this.readDate = readDate;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(LCAHT.fileReadDate);
			tdbResource.addLiteral(LCAHT.fileReadDate, readDate);
			ActiveTDB.tdbDataset.commit();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		if (!FileEncodingUtil.containsEncoding(encoding)) {
			// WARN THAT THIS ENCODING HAS NOT BEEN SEEN
		}
		this.encoding = encoding;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		try {
			ActiveTDB.replaceLiteral(tdbResource, LCAHT.fileEncoding, encoding);
			ActiveTDB.tdbDataset.commit();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
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
		// // --- BEGIN SAFE -WRITE- TRANSACTION ---
		// ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		// try {
		// model.add(tdbResource, RDF.type, LCAHT.dataFile);
		// ActiveTDB.tdbDataset.commit();
		// } finally {
		// ActiveTDB.tdbDataset.end();
		// }
		// // ---- END SAFE -WRITE- TRANSACTION ---
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
			tdbResource.removeAll(LCAHT.fileName);
			tdbResource.removeAll(LCAHT.filePath);
			tdbResource.removeAll(LCAHT.fileEncoding);
			tdbResource.removeAll(LCAHT.byteCount);
			tdbResource.removeAll(LCAHT.fileModifiedDate);
			tdbResource.removeAll(LCAHT.fileReadDate);
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}
}
