package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.FileEncodingUtil;
import gov.epa.nrmrl.std.lca.ht.utils.RDFUtil;
import gov.epa.nrmrl.std.lca.ht.utils.Temporal;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;

import java.util.Date;
import java.util.List;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;

public class FileMD {
	private String filename;
	private String path;
	private String encoding;
	private long byteCount = 0;
	private Date modifiedDate;
	private Date readDate;
	private Resource tdbResource;
	private static final Resource rdfClass = LCAHT.DataFile;

	public FileMD(){
	
	}
	public FileMD(boolean createTDBResource) {
		if (createTDBResource){
		  this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
		}
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
		if (tdbResource != null)
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
		if (tdbResource != null)
			ActiveTDB.tsReplaceLiteral(tdbResource, LCAHT.filePath, path);
	}

	public long getByteCount() {
		return byteCount;
	}

	public void setByteCount(long size) {
		this.byteCount = size;
		if (tdbResource != null)
			ActiveTDB.tsReplaceLiteral(tdbResource, LCAHT.byteCount, size);
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
		if (tdbResource != null)
			ActiveTDB.tsReplaceLiteral(tdbResource, DCTerms.modified, modifiedDate);
	}

	public Date getReadDate() {
		return readDate;
	}

	public void setReadDate(Date readDate) {
		this.readDate = readDate;
		if (tdbResource != null)
			ActiveTDB.tsReplaceLiteral(tdbResource, LCAHT.fileReadDate, readDate);
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		if (!FileEncodingUtil.containsEncoding(encoding)) {
			// WARN THAT THIS ENCODING HAS NOT BEEN SEEN
		}
		this.encoding = encoding;
		if (tdbResource != null)
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

		if (tdbResource.hasProperty(DCTerms.modified)) {
			rdfNode = tdbResource.getProperty(DCTerms.modified).getObject();
			if (rdfNode.isLiteral()) {
				modifiedDate = Temporal.getDateObject(rdfNode.asLiteral());
			}
		}

		if (tdbResource.hasProperty(LCAHT.fileReadDate)) {
			rdfNode = tdbResource.getProperty(LCAHT.fileReadDate).getObject();
			if (rdfNode.isLiteral()) {
				readDate = Temporal.getDateObject(rdfNode.asLiteral());
			}
		}

		System.out.println("sync line: 8");
	}
	
	public void createTDBResource() {
		if (tdbResource == null)
			return;
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
		if (filename != null)
			setFilename(filename);
		if (path != null)
			setPath(path);
		if (encoding != null)
			setEncoding(encoding);
		if (byteCount != 0)
			setByteCount(byteCount);
		if (modifiedDate != null)
			setModifiedDate(modifiedDate);
		if (readDate != null)
		setReadDate(readDate);
	}

	public void remove() {
		if (tdbResource == null)
			return;
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(null);
		try {
			StmtIterator stmtIterator = tdbResource.listProperties();
			while (stmtIterator.hasNext()) {
				tdbModel.remove(stmtIterator.next());
			}
			for (int i = 0; i < DataSourceKeeper.size(); i++) {
				DataSourceProvider dataSourceProvider = DataSourceKeeper.get(i);
				List<FileMD> fileMDs = dataSourceProvider.getFileMDList();
				if (fileMDs != null) {
					for (FileMD fileMD : fileMDs) {
						if (fileMD.equals(this)) {
							tdbModel.remove(dataSourceProvider.getTdbResource(), LCAHT.containsFile, tdbResource);
						}
					}
				}
			}
			ActiveTDB.tdbDataset.commit();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}
}
