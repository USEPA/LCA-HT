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
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			this.tdbResource = model.createResource();
			model.add(tdbResource, RDF.type, LCAHT.dataFile);
		} finally {
			ActiveTDB.TDBDataset.end();
		}
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

		// this.filename = filename;
		// this.path = path;
		// this.byteCount = size;
		// this.modifiedDate = modifiedDate;
		// this.readDate = readDate;
		// this.tdbResource = model.createResource();
		// model.add(tdbResource, RDF.type, LCAHT.dataFile);
		// model.add(this.tdbResource, LCAHT.fileName,
		// model.createTypedLiteral(this.filename));
		// model.add(this.tdbResource, LCAHT.filePath,
		// model.createTypedLiteral(this.path));
		// model.add(this.tdbResource, LCAHT.byteCount,
		// model.createTypedLiteral(this.byteCount));
		// model.add(this.tdbResource, LCAHT.fileModifiedDate,
		// model.createTypedLiteral(this.modifiedDate));
		// model.add(this.tdbResource, LCAHT.fileReadDate,
		// model.createTypedLiteral(this.readDate));
		FileMDKeeper.add(this);

	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			this.filename = filename;
			tdbResource.removeAll(LCAHT.fileName);
			tdbResource.addProperty(LCAHT.fileName, filename);
		} finally {
			ActiveTDB.TDBDataset.end();
		}
		//
		// if (tdbResource == null) {
		// tdbResource = model.createResource();
		// }
		// ActiveTDB.replaceLiteral(tdbResource, LCAHT.fileName, filename);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(LCAHT.filePath);
			tdbResource.addProperty(LCAHT.filePath, path);
			ActiveTDB.TDBDataset.commit();
		} finally {
			ActiveTDB.TDBDataset.end();
		}
		// if (tdbResource == null) {
		// tdbResource = model.createResource();
		// }
		// ActiveTDB.replaceLiteral(tdbResource, LCAHT.filePath, path);
	}

	public long getByteCount() {
		return byteCount;
	}

	public void setByteCount(long size) {
		this.byteCount = size;

		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(LCAHT.byteCount);
			tdbResource.addLiteral(LCAHT.byteCount, byteCount);
			// tdbResource.removeAll(LCAHT.filePath);
			// tdbResource.addProperty(LCAHT.filePath, path);
			ActiveTDB.TDBDataset.commit();
		} finally {
			ActiveTDB.TDBDataset.end();
		}
		// tdbResource.removeAll(LCAHT.byteCount);
		// tdbResource.addLiteral(LCAHT.byteCount, byteCount);
		// tdbResource.addProperty(LCAHT.byteCount, size);
		//
		// if (tdbResource == null) {
		// tdbResource = model.createResource();
		// }
		// ActiveTDB.replaceLiteral(tdbResource, LCAHT.byteCount, size);
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(LCAHT.fileModifiedDate);
			tdbResource.addLiteral(LCAHT.fileModifiedDate, modifiedDate);
			ActiveTDB.TDBDataset.commit();
		} finally {
			ActiveTDB.TDBDataset.end();
		}

		//
		// if (tdbResource == null) {
		// tdbResource = model.createResource();
		// }
		// ActiveTDB.replaceLiteral(tdbResource, LCAHT.fileModifiedDate,
		// modifiedDate);
	}

	public Date getReadDate() {
		return readDate;
	}

	public void setReadDate(Date readDate) {
		this.readDate = readDate;
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(LCAHT.fileReadDate);
			tdbResource.addLiteral(LCAHT.fileReadDate, readDate);
			ActiveTDB.TDBDataset.commit();
		} finally {
			ActiveTDB.TDBDataset.end();
		}

		//
		// if (tdbResource == null) {
		// tdbResource = model.createResource();
		// }
		// ActiveTDB.replaceLiteral(tdbResource, LCAHT.fileReadDate, readDate);
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		if (!FileEncodingUtil.containsEncoding(encoding)) {
			// WARN THAT THIS ENCODING HAS NOT BEEN SEEN
		}
		this.encoding = encoding;
		
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			ActiveTDB.replaceLiteral(tdbResource, LCAHT.fileEncoding, encoding);
			ActiveTDB.TDBDataset.commit();
		} finally {
			ActiveTDB.TDBDataset.end();
		}

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
		ActiveTDB.TDBDataset.begin(ReadWrite.WRITE);
		try {
			model.add(tdbResource, RDF.type, LCAHT.dataFile);
			ActiveTDB.TDBDataset.commit();
		} finally {
			ActiveTDB.TDBDataset.end();
		}

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
	}

	public void remove() {
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

	}
	// public static FileMD syncFromTDB(Resource fileMDResource) {
	// FileMD fileMD = new FileMD(fileMDResource);
	// return fileMD;
	// }
}
