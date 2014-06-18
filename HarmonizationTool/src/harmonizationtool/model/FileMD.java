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
import com.hp.hpl.jena.rdf.model.Statement;

public class FileMD {
	private String filename;
	private String path;
	private String encoding = null;
	private long size;
	private Date lastModified;
	private Date readTime;
	private Resource tdbResource;
	private static final Model model = ActiveTDB.model;

	public FileMD(){
		this.tdbResource = model.createResource();
	}
	
	public FileMD(String filename, String path, long size, Date lastModified, Date readTime) {
		super();
		this.filename = filename;
		this.path = path;
		this.size = size;
		this.lastModified = lastModified;
		this.readTime = readTime;
		this.tdbResource = model.createResource();
		model.add(this.tdbResource, LCAHT.fileName, model.createTypedLiteral(this.filename));
		model.add(this.tdbResource, LCAHT.filePath, model.createTypedLiteral(this.path));
		model.add(this.tdbResource, LCAHT.byteCount, model.createTypedLiteral(this.size));
		model.add(this.tdbResource, LCAHT.fileLastModified, model.createTypedLiteral(this.lastModified));
		model.add(this.tdbResource, LCAHT.fileReadDate, model.createTypedLiteral(this.readTime));
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
		NodeIterator nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.fileName);
		while (nodeIterator.hasNext()){
			RDFNode rdfNode = nodeIterator.next();
			model.removeAll(tdbResource, LCAHT.fileName, rdfNode);
		}
		model.add(tdbResource, LCAHT.fileName, model.createTypedLiteral(filename));
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
		NodeIterator nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.filePath);
		while (nodeIterator.hasNext()){
			RDFNode rdfNode = nodeIterator.next();
			model.removeAll(tdbResource, LCAHT.filePath, rdfNode);
		}
		model.add(tdbResource, LCAHT.filePath, model.createTypedLiteral(path));
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
		NodeIterator nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.byteCount);
		while (nodeIterator.hasNext()){
			RDFNode rdfNode = nodeIterator.next();
			model.removeAll(tdbResource, LCAHT.byteCount, rdfNode);
		}
		model.add(tdbResource, LCAHT.byteCount, model.createTypedLiteral(size));
	}
	public Date getLastModified() {
		return lastModified;
	}
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
		NodeIterator nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.fileLastModified);
		while (nodeIterator.hasNext()){
			RDFNode rdfNode = nodeIterator.next();
			model.removeAll(tdbResource, LCAHT.fileLastModified, rdfNode);
		}
		model.add(tdbResource, LCAHT.fileLastModified, model.createTypedLiteral(lastModified));
	}
	public Date getReadTime() {
		return readTime;
	}
	public void setReadTime(Date readTime) {
		this.readTime = readTime;
		NodeIterator nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.fileReadDate);
		while (nodeIterator.hasNext()){
			RDFNode rdfNode = nodeIterator.next();
			model.removeAll(tdbResource, LCAHT.fileReadDate, rdfNode);
		}
		model.add(tdbResource, LCAHT.fileReadDate, model.createTypedLiteral(readTime));
	}
	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		if (!FileEncodingUtil.containsEncoding(encoding)){
			// WARN THAT THIS ENCODING HAS NOT BEEN SEEN
		}
		this.encoding = encoding;
		NodeIterator nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.fileEncoding);
		while (nodeIterator.hasNext()){
			RDFNode rdfNode = nodeIterator.next();
			model.removeAll(tdbResource, LCAHT.fileEncoding, rdfNode);
		}
		model.add(tdbResource, LCAHT.fileEncoding, model.createTypedLiteral(encoding));
	}
	@Override
	public String toString() {
		return path;
	}
	public Resource getTdbResource() {
		return tdbResource;
	}
	public void setTdbResource(Resource tdbResource) {
		if (this.tdbResource != null){
			System.out.println("How / Why would you change the tdbResource (blank node) for a file?");
			// FIXME THIS SHOULD BE AN ASSERT
		}
		this.tdbResource = tdbResource;
	}

}
