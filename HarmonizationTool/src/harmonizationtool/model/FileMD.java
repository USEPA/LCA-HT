package harmonizationtool.model;

//import java.util.Calendar;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.utils.FileEncodingUtil;
import harmonizationtool.vocabulary.LCAHT;

import java.util.Date;

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
		this.tdbResource = model.createResource();
		model.add(tdbResource, RDF.type, LCAHT.dataFile);
	}

	public FileMD(Resource tdbResource) {
		this.tdbResource = tdbResource;
		model.add(tdbResource, RDF.type, LCAHT.dataFile);
	}

	public FileMD(String filename, String path, long size, Date modifiedDate, Date readDate) {
		super();
		this.filename = filename;
		this.path = path;
		this.byteCount = size;
		this.modifiedDate = modifiedDate;
		this.readDate = readDate;
		this.tdbResource = model.createResource();
		model.add(tdbResource, RDF.type, LCAHT.dataFile);
		model.add(this.tdbResource, LCAHT.fileName, model.createTypedLiteral(this.filename));
		model.add(this.tdbResource, LCAHT.filePath, model.createTypedLiteral(this.path));
		model.add(this.tdbResource, LCAHT.byteCount, model.createTypedLiteral(this.byteCount));
		model.add(this.tdbResource, LCAHT.fileModifiedDate, model.createTypedLiteral(this.modifiedDate));
		model.add(this.tdbResource, LCAHT.fileReadDate, model.createTypedLiteral(this.readDate));
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
		if (tdbResource == null){
			tdbResource = model.createResource();
		}
		ActiveTDB.replaceLiteral(tdbResource, LCAHT.fileName, filename);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
		if (tdbResource == null){
			tdbResource = model.createResource();
		}
		ActiveTDB.replaceLiteral(tdbResource, LCAHT.filePath, path);
	}

	public long getByteCount() {
		return byteCount;
	}

	public void setByteCount(long size) {
		this.byteCount = size;
		if (tdbResource == null){
			tdbResource = model.createResource();
		}
		ActiveTDB.replaceLiteral(tdbResource, LCAHT.byteCount, size);
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
		if (tdbResource == null){
			tdbResource = model.createResource();
		}
		ActiveTDB.replaceLiteral(tdbResource, LCAHT.fileModifiedDate, modifiedDate);
	}

	public Date getReadDate() {
		return readDate;
	}

	public void setReadDate(Date readDate) {
		this.readDate = readDate;
		if (tdbResource == null){
			tdbResource = model.createResource();
		}
		ActiveTDB.replaceLiteral(tdbResource, LCAHT.fileReadDate, readDate);
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		if (!FileEncodingUtil.containsEncoding(encoding)) {
			// WARN THAT THIS ENCODING HAS NOT BEEN SEEN
		}
		this.encoding = encoding;
		if (tdbResource == null){
			tdbResource = model.createResource();
		}
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
		model.add(tdbResource, RDF.type, LCAHT.dataFile);
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
			if (object.isLiteral()){
				literal = (Literal) object;
				Object javaObject = literal.getValue();
				if (javaObject.getClass().equals(String.class)){
					filename = (String) literal.getValue();
				}
			}
		}

		nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.filePath);
		if (nodeIterator.hasNext()) {
			object = nodeIterator.next();
			if (object.isLiteral()){
				literal = (Literal) object;
				Object javaObject = literal.getValue();
				if (javaObject.getClass().equals(String.class)){
					path = (String) literal.getValue();
				}
			}
		}

		nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.fileEncoding);
		if (nodeIterator.hasNext()) {
			object = nodeIterator.next();
			if (object.isLiteral()){
				literal = (Literal) object;
				Object javaObject = literal.getValue();
				if (javaObject.getClass().equals(String.class)){
					encoding = (String) literal.getValue();
				}
			}
		}

		nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.byteCount);
		if (nodeIterator.hasNext()) {
			object = nodeIterator.next();
			if (object.isLiteral()){
				literal = (Literal) object;
				Object javaObject = literal.getLong();
				if (javaObject.getClass().equals(Long.class)){
					byteCount = (Long) literal.getLong();
				}
				else {
					System.out.println("javaObject.getClass(): "+ javaObject.getClass());
				}
			}
		}
		

		nodeIterator = model.listObjectsOfProperty(tdbResource, LCAHT.fileModifiedDate);
		if (nodeIterator.hasNext()) {
			object = nodeIterator.next();
			if (object.isLiteral()){
				literal = (Literal) object;
				Object javaObject = literal.getValue();
//				FIXME FIXME FIXME
				if (javaObject.getClass().equals(Long.class)){
					System.out.println("javaObject= "+javaObject);

//					modifiedDate = new Date((Long) javaObject);
				}
				else {
					System.out.println("fileModifiedDate javaObject.getClass(): "+ javaObject.getClass());
					System.out.println("literal.getValue()= "+literal.getValue());
//					System.out.println("theDate= "+theDate);
//					Date theDate = (Date) javaObject;

				}
			}
		}
		
//		 nodeIterator = model.listObjectsOfProperty(tdbResource,
//		 LCAHT.fileModifiedDate);
//		 object = nodeIterator.next();
//		 long dateLong = (long) Integer.parseInt(object.toString());
//		 this.modifiedDate = new Date(dateLong);
//		
		// nodeIterator = model.listObjectsOfProperty(tdbResource,
		// LCAHT.fileReadDate);
		// object = nodeIterator.next();
		// dateLong = (long) Integer.parseInt(object.toString());
		// this.readDate = new Date(dateLong);
	}

	public void syncDataToTDB() {
		if (tdbResource == null) {
			tdbResource = model.createResource();
		}
		model.add(tdbResource, RDF.type, LCAHT.dataFile);
		model.add(tdbResource, LCAHT.fileName, model.createTypedLiteral(filename));
		model.add(tdbResource, LCAHT.filePath, model.createTypedLiteral(path));
//		Literal byteCountLiteral = model.createTypedLiteral(byteCount);
//		model.add(tdbResource, LCAHT.byteCount, byteCountLiteral);
		// 	NOTE xsd:long ==> xsd:integer WHICH IS ARBITRARY LENGTH (ANY SEQ. OF DIGITS)
		//  NOTE ALSO: xsd:int IS LIKE java.lang.Integer (LIMITED TO -2147483648 TO 2147483647)
		model.add(tdbResource, LCAHT.byteCount, model.createTypedLiteral(byteCount));
		model.add(tdbResource, LCAHT.fileModifiedDate, model.createTypedLiteral(modifiedDate));
		model.add(tdbResource, LCAHT.fileReadDate, model.createTypedLiteral(readDate));
	}

	// public static FileMD syncFromTDB(Resource fileMDResource) {
	// FileMD fileMD = new FileMD(fileMDResource);
	// return fileMD;
	// }
}
