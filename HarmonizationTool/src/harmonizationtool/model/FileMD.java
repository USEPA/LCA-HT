package harmonizationtool.model;

//import java.util.Calendar;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.utils.FileEncodingUtil;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FEDLCA;
import harmonizationtool.vocabulary.LCAHT;

import java.util.Calendar;
import java.util.Date;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

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

	// public FileMD(String filename, String path, long size, Date modifiedDate, Date readDate) {
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
		// assert this.tdbResource == null : "Why and how would you change the tdbResource (blank node) for a file?";
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
		rdfNode = tdbResource.getProperty(LCAHT.fileName).getObject();
		if (rdfNode != null) {
			filename = ActiveTDB.getStringFromLiteral(rdfNode);
		}

		rdfNode = tdbResource.getProperty(LCAHT.filePath).getObject();
		if (rdfNode != null) {
			path = ActiveTDB.getStringFromLiteral(rdfNode);
		}

		// rdfNode = tdbResource.getProperty(LCAHT.fileEncoding).getObject();
		// if (rdfNode != null) {
		// encoding = ActiveTDB.getStringFromLiteral(rdfNode);
		// }
		// System.out.println("sync line: 5");

		rdfNode = tdbResource.getProperty(LCAHT.byteCount).getObject();
		byteCount = rdfNode.asLiteral().getLong();

		rdfNode = tdbResource.getProperty(LCAHT.fileModifiedDate).getObject();
		modifiedDate = ActiveTDB.getDateFromLiteral(rdfNode.asLiteral());

		rdfNode = tdbResource.getProperty(LCAHT.fileReadDate).getObject();
		readDate = ActiveTDB.getDateFromLiteral(rdfNode.asLiteral());

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
