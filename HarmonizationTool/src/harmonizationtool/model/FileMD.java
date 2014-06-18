package harmonizationtool.model;

//import java.util.Calendar;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.utils.FileEncodingUtil;
import harmonizationtool.vocabulary.LCAHT;

import java.util.Date;

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
	private Date lastModified;
	private Date readTime;
	private Resource tdbResource;
	private static final Model model = ActiveTDB.model;

	public FileMD() {
		this.tdbResource = model.createResource();
//		model.add(tdbResource, RDF.type, LCAHT.dataFile);
	}
	
	public FileMD(Resource tdbResource) {
		this.tdbResource = tdbResource;
	}

	public FileMD(String filename, String path, long size, Date lastModified, Date readTime) {
		super();
		this.filename = filename;
		this.path = path;
		this.byteCount = size;
		this.lastModified = lastModified;
		this.readTime = readTime;
		this.tdbResource = model.createResource();
		model.add(this.tdbResource, LCAHT.fileName, model.createTypedLiteral(this.filename));
		model.add(this.tdbResource, LCAHT.filePath, model.createTypedLiteral(this.path));
		model.add(this.tdbResource, LCAHT.byteCount, model.createTypedLiteral(this.byteCount));
		model.add(this.tdbResource, LCAHT.fileLastModified, model.createTypedLiteral(this.lastModified));
		model.add(this.tdbResource, LCAHT.fileReadDate, model.createTypedLiteral(this.readTime));
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
		ActiveTDB.replaceLiteral(tdbResource, LCAHT.fileName, filename);
//		System.out.println("Filename: "+ filename + " added to TDB");
		// NodeIterator nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.fileName);
		// while (nodeIterator.hasNext()){
		// RDFNode rdfNode = nodeIterator.next();
		// model.removeAll(tdbResource, LCAHT.fileName, rdfNode);
		// }
		// model.add(tdbResource, LCAHT.fileName, model.createTypedLiteral(filename));
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
		ActiveTDB.replaceLiteral(tdbResource, LCAHT.filePath, path);
	}

	public long getByteCount() {
		return byteCount;
	}

	public void setByteCount(long size) {
		this.byteCount = size;
		ActiveTDB.replaceLiteral(tdbResource, LCAHT.byteCount, size);
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
		ActiveTDB.replaceLiteral(tdbResource, LCAHT.fileLastModified, lastModified);
	}

	public Date getReadTime() {
		return readTime;
	}

	public void setReadTime(Date readTime) {
		this.readTime = readTime;
		ActiveTDB.replaceLiteral(tdbResource, LCAHT.fileReadDate, readTime);
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		if (!FileEncodingUtil.containsEncoding(encoding)) {
			// WARN THAT THIS ENCODING HAS NOT BEEN SEEN
		}
		this.encoding = encoding;
		ActiveTDB.replaceLiteral(tdbResource, LCAHT.fileEncoding, encoding);
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
	}

	public void syncDataFromTDB() {
		NodeIterator nodeIterator;
		RDFNode object;

		nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.fileName);
		object = nodeIterator.next();
		this.filename = object.toString();

		nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.filePath);
		object = nodeIterator.next();
		this.path = object.toString();
		
		nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.fileEncoding);
		object = nodeIterator.next();
		this.encoding = object.toString();
		
		nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.byteCount);
		object = nodeIterator.next();
		this.byteCount = (long) Integer.parseInt(object.toString());
		
		nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.byteCount);
		object = nodeIterator.next();
		this.byteCount = (long) Integer.parseInt(object.toString());
		
		nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.fileLastModified);
		object = nodeIterator.next();
		long dateLong = (long) Integer.parseInt(object.toString());
		this.lastModified = new Date(dateLong);
		
		nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.fileReadDate);
		object = nodeIterator.next();
		dateLong = (long) Integer.parseInt(object.toString());
		this.readTime = new Date(dateLong);
	}

//	public static FileMD syncFromTDB(Resource fileMDResource) {
//		FileMD fileMD = new FileMD(fileMDResource);
//		return fileMD;
//	}
}
