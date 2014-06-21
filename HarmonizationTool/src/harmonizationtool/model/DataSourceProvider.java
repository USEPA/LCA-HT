package harmonizationtool.model;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FEDLCA;
import harmonizationtool.vocabulary.LCAHT;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class DataSourceProvider {
	private DataSourceMD dataSourceMD;
	private CuratorMD curatorMD;
	private List<FileMD> fileMDList = new ArrayList<FileMD>();
	private Resource tdbResource;
	private static final Model model = ActiveTDB.model;
	private boolean isMaster = false;

	public DataSourceProvider(){
//		tdbResource = model.createResource();
//		model.add(tdbResource, RDF.type, ECO.DataSource);	
	}
	
	public DataSourceMD getDataSourceMD() {
		return dataSourceMD;
	}

	public void setDataSourceMD(DataSourceMD dataSourceMD) {
		this.dataSourceMD = dataSourceMD;
	}

	public CuratorMD getCuratorMD() {
		return curatorMD;
	}

	public void setCuratorMD(CuratorMD curatorMD) {
		this.curatorMD = curatorMD;
	}

	public Resource getTdbResource() {

		// if (tdbResource == null){
		// tdbResource = ActiveTDB.model.createResource();
		// }
		// assert tdbResource != null : "tdbResource cannot be null";
		return tdbResource;
	}

	public void setTdbResource(Resource tdbResource) {
		this.tdbResource = tdbResource;
	}

	public void addFileMD(FileMD fileMD) {
		fileMDList.add(fileMD);
	}

	public List<FileMD> getFileMDList() {
		return fileMDList;
	}

	public void remove(FileMD fileMD) {
		fileMD.remove();
		fileMDList.remove(fileMD);
	}

	public void removeFileMDList() {
//		for (FileMD fileMD : fileMDList) {
//			fileMD.remove();
//			tdbResource.removeAll(LCAHT.containsFile);
//			fileMDList.remove(fileMD);
//		}
		fileMDList = null;
	}
	
	public void remove(){
//		removeFileMDList();
//		dataSourceMD.remove();
//		tdbResource.removeAll(ECO.hasDataSource);
//		curatorMD.remove();
//		tdbResource.removeAll(FEDLCA.curatedBy);
	}

	public boolean isMaster() {
		return isMaster;
	}

	public void setMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}
}
