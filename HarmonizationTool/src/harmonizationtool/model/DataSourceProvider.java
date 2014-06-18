package harmonizationtool.model;

import java.util.ArrayList;
import java.util.List;
import com.hp.hpl.jena.rdf.model.Resource;

public class DataSourceProvider {
	private DataSourceMD dataSourceMD;
	private CuratorMD curatorMD;
	private List<FileMD> fileMDList = new ArrayList<FileMD>();
	private Resource tdbResource;
	private boolean isMaster = false;
	
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

//		if (tdbResource == null){
//			tdbResource = ActiveTDB.model.createResource();
//		}
//		assert tdbResource != null : "tdbResource cannot be null";
		return tdbResource;
	}
	public void setTdbResource(Resource tdbResource) {
		this.tdbResource = tdbResource;
	}
	public void addFileMD(FileMD fileMD){
		fileMDList.add(fileMD);
	}
	public List<FileMD> getFileMDList() {
		return fileMDList;
	}
	public void remove(FileMD fileMD) {
		fileMDList.remove(fileMD);
		
	}
	public boolean isMaster() {
		return isMaster;
	}
	public void setMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}
}
