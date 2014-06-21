package harmonizationtool.model;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FEDLCA;
import harmonizationtool.vocabulary.LCAHT;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class DataSourceProvider {
	private String dataSourceName;
	private String version;
	private String comments;
	private ContactMD dataSourceMD;
	// private CuratorMD curatorMD;
	private List<FileMD> fileMDList = new ArrayList<FileMD>();
	// private List<Annotation> annotationList = new ArrayList<Annotation>();

	private Resource tdbResource;
	private static final Model model = ActiveTDB.model;
	private boolean isMaster = false;

	public DataSourceProvider() {
		tdbResource = model.createResource();
		model.add(tdbResource, RDF.type, ECO.DataSource);
	}

	public ContactMD getDataSourceMD() {
		return dataSourceMD;
	}

	public void setDataSourceMD(ContactMD dataSourceMD) {
		this.dataSourceMD = dataSourceMD;
	}

	// public CuratorMD getCuratorMD() {
	// return curatorMD;
	// }
	//
	// public void setCuratorMD(CuratorMD curatorMD) {
	// this.curatorMD = curatorMD;
	// }

	public Resource getTdbResource() {
		if (tdbResource == null) {
			tdbResource = ActiveTDB.model.createResource();
		}
		assert tdbResource != null : "tdbResource cannot be null";
		return tdbResource;
	}

	public void setTdbResource(Resource tdbResource) {
		this.tdbResource = tdbResource;
	}

	public void addFileMD(FileMD fileMD) {
		fileMDList.add(fileMD);
		tdbResource.addProperty(LCAHT.containsFile, fileMD.getTdbResource());
	}

	public List<FileMD> getFileMDList() {
		return fileMDList;
	}

	public void remove(FileMD fileMD) {
		fileMD.remove();
		fileMDList.remove(fileMD);
		ActiveTDB.model.remove(tdbResource, LCAHT.containsFile, fileMD.getTdbResource());
	}

	public void removeFileMDList() {
		for (FileMD fileMD : fileMDList) {
			fileMD.remove();
		}
		fileMDList = null;
	}

	public void remove() {
		removeFileMDList();

		// dataSourceMD.remove();
		tdbResource.removeAll(ECO.hasDataSource);
		// curatorMD.remove();
		// tdbResource.removeAll(FEDLCA.curatedBy);
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
		 model.add(this.tdbResource, RDFS.label, model.createTypedLiteral(this.dataSourceName));
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
		 model.add(this.tdbResource, DCTerms.hasVersion, model.createTypedLiteral(this.version));
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
		 model.add(this.tdbResource, RDFS.comment, model.createTypedLiteral(this.comments));

	}
}
