package harmonizationtool.model;

//import java.util.Calendar;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.utils.FileEncodingUtil;
import harmonizationtool.vocabulary.FEDLCA;
import harmonizationtool.vocabulary.LCAHT;

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
	private String encoding = null;
	private long byteCount;
	private Date modifiedDate;
	private Date readDate;
	private Resource tdbResource;
	private static final Model model = ActiveTDB.model;

	public FileMD() {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			this.tdbResource = model.createResource();
			model.add(tdbResource, RDF.type, LCAHT.dataFile);
		} finally {
			ActiveTDB.TDBDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		FileMDKeeper.add(this);
	}

	public FileMD(Resource tdbResource) {
		this.tdbResource = tdbResource;
		FileMDKeeper.add(this);
		syncDataFromTDB();
	}

	public FileMD(String filename, String path, long size, Date modifiedDate, Date readDate) {
		super();
		setFilename(filename);
		setPath(path);
		setByteCount(size);
		setModifiedDate(modifiedDate);
		setReadDate(readDate);
		FileMDKeeper.add(this);
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			this.filename = filename;
			tdbResource.removeAll(LCAHT.fileName);
			tdbResource.addProperty(LCAHT.fileName, filename);
		} finally {
			ActiveTDB.TDBDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(LCAHT.filePath);
			tdbResource.addProperty(LCAHT.filePath, path);
			ActiveTDB.TDBDataset.commit();
		} finally {
			ActiveTDB.TDBDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public long getByteCount() {
		return byteCount;
	}

	public void setByteCount(long size) {
		this.byteCount = size;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---

		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(LCAHT.byteCount);
			tdbResource.addLiteral(LCAHT.byteCount, byteCount);
			ActiveTDB.TDBDataset.commit();
		} finally {
			ActiveTDB.TDBDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(LCAHT.fileModifiedDate);
			tdbResource.addLiteral(LCAHT.fileModifiedDate, modifiedDate);
			ActiveTDB.TDBDataset.commit();
		} finally {
			ActiveTDB.TDBDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public Date getReadDate() {
		return readDate;
	}

	public void setReadDate(Date readDate) {
		this.readDate = readDate;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(LCAHT.fileReadDate);
			tdbResource.addLiteral(LCAHT.fileReadDate, readDate);
			ActiveTDB.TDBDataset.commit();
		} finally {
			ActiveTDB.TDBDataset.end();
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
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			ActiveTDB.replaceLiteral(tdbResource, LCAHT.fileEncoding, encoding);
			ActiveTDB.TDBDataset.commit();
		} finally {
			ActiveTDB.TDBDataset.end();
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
		assert this.tdbResource == null : "Why and how would you change the tdbResource (blank node) for a file?";
		this.tdbResource = tdbResource;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			model.add(tdbResource, RDF.type, LCAHT.dataFile);
			ActiveTDB.TDBDataset.commit();
		} finally {
			ActiveTDB.TDBDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public void syncDataFromTDB() {
		NodeIterator nodeIterator;
		RDFNode object;
		Literal literal;
		if (tdbResource == null) {
			return;
		}

		nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.fileName);
		if (nodeIterator.hasNext()) {
			object = nodeIterator.next();
			if (object.isLiteral()) {
				literal = object.asLiteral();
				Object javaObject = literal.getValue();
				if (javaObject.getClass().equals(String.class)) {
					filename = (String) literal.getValue();
				}
			}
		}

		nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.filePath);
		if (nodeIterator.hasNext()) {
			object = nodeIterator.next();
			if (object.isLiteral()) {
				literal = object.asLiteral();
				Object javaObject = literal.getValue();
				if (javaObject.getClass().equals(String.class)) {
					path = (String) literal.getValue();
				}
			}
		}

		nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.fileEncoding);
		if (nodeIterator.hasNext()) {
			object = nodeIterator.next();
			if (object.isLiteral()) {
				literal = object.asLiteral();
				Object javaObject = literal.getValue();
				if (javaObject.getClass().equals(String.class)) {
					encoding = (String) literal.getValue();
				}
			}
		}

		nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.byteCount);
		if (nodeIterator.hasNext()) {
			object = nodeIterator.next();
			if (object.isLiteral()) {
				literal = object.asLiteral();
				Object javaObject = literal.getLong();
				if (javaObject.getClass().equals(Long.class)) {
					byteCount = literal.getLong();
				}
			}
		}

		nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.fileModifiedDate);
		if (nodeIterator.hasNext()) {
			object = nodeIterator.next();
			if (object.isLiteral()) {
				literal = object.asLiteral();
				modifiedDate = ActiveTDB.getDateFromLiteral(literal);
			}
		}

		nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.fileReadDate);
		if (nodeIterator.hasNext()) {
			object = nodeIterator.next();
			if (object.isLiteral()) {
				literal = object.asLiteral();
				readDate = ActiveTDB.getDateFromLiteral(literal);
			}
		}
	}

	public void syncDataToTDB() {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			if (tdbResource == null) {
				tdbResource = model.createResource();
			}
			model.add(tdbResource, RDF.type, LCAHT.dataFile);
			model.add(tdbResource, LCAHT.fileName, model.createTypedLiteral(filename));
			model.add(tdbResource, LCAHT.filePath, model.createTypedLiteral(path));
			// Literal byteCountLiteral = model.createTypedLiteral(byteCount);
			// model.add(tdbResource, LCAHT.byteCount, byteCountLiteral);
			// NOTE xsd:long ==> xsd:integer WHICH IS ARBITRARY LENGTH (ANY SEQ.
			// OF
			// DIGITS)
			// NOTE ALSO: xsd:int IS LIKE java.lang.Integer (LIMITED TO
			// -2147483648
			// TO 2147483647)
			model.add(tdbResource, LCAHT.byteCount, model.createTypedLiteral(byteCount));
			model.add(tdbResource, LCAHT.fileModifiedDate, model.createTypedLiteral(modifiedDate));
			model.add(tdbResource, LCAHT.fileReadDate, model.createTypedLiteral(readDate));
			ActiveTDB.TDBDataset.commit();
		} finally {
			ActiveTDB.TDBDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public void remove() {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(LCAHT.fileName);
			tdbResource.removeAll(LCAHT.filePath);
			tdbResource.removeAll(LCAHT.fileEncoding);
			tdbResource.removeAll(LCAHT.byteCount);
			tdbResource.removeAll(LCAHT.fileModifiedDate);
			tdbResource.removeAll(LCAHT.fileReadDate);
		} finally {
			ActiveTDB.TDBDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}
}
