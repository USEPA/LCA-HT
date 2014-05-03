package harmonizationtool.model;

import harmonizationtool.comands.SelectTDB;
import harmonizationtool.vocabulary.ECO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class DataSetKeeper {

	private static List<DataSetProvider> dataSetProviderList = new ArrayList<DataSetProvider>();

	private DataSetKeeper() {
	}

	public static boolean add(DataSetProvider dataSetProvider) {
		if (dataSetProvider.getTdbResource() == null) {
			Model model = SelectTDB.model;
			Resource tdbResource = model.createResource();
			model.add(tdbResource, RDF.type, ECO.DataSource);
			model.add(tdbResource, RDFS.label, model.createLiteral(dataSetProvider.getDataSetMD()
					.getName()));
			model.add(tdbResource, RDFS.comment, model.createLiteral(dataSetProvider.getDataSetMD()
					.getComments()));
			model.add(tdbResource, DCTerms.hasVersion, model.createLiteral(dataSetProvider
					.getDataSetMD().getVersion()));
			dataSetProvider.setTdbResource(tdbResource);
		}
		return dataSetProviderList.add(dataSetProvider);
	}
	
	public static boolean remove(DataSetProvider dataSetProvider) {
		return dataSetProviderList.remove(dataSetProvider);
	}
	
	public static boolean remove(DataSetProvider dataSetProvider, boolean removeTDBData) {
		if (removeTDBData){
			Resource tdbResource = dataSetProvider.getTdbResource();
			SelectTDB.removeAllWithSubject(tdbResource);
			SelectTDB.removeAllWithObject(tdbResource);
		}
		return dataSetProviderList.remove(dataSetProvider);
	}

	public static List<Integer> getIDs() {
		List<Integer> ids = new ArrayList<Integer>();
		Iterator<DataSetProvider> iterator = dataSetProviderList.iterator();
		while (iterator.hasNext()) {
			ids.add(dataSetProviderList.indexOf(iterator.next()));
		}
		return ids;
	}
	
	public static List<String> getNames() {
		List<String> results = new ArrayList<String>();
		Iterator<DataSetProvider> iterator = dataSetProviderList.iterator();
		while (iterator.hasNext()) {
			results.add(iterator.next().getDataSetMD().getName());
		}
		Collections.sort(results);
		return results;
	}
	
	public static String uniquify(String proposedNewDatasetName){
		String uniqueName = proposedNewDatasetName;
		while (indexOfDataSetName(uniqueName) >-1){
			if (uniqueName.matches("/_\\d\\d$")){
				String baseName = uniqueName.substring(0,uniqueName.length()-2);
				int nextVal=Integer.parseInt(uniqueName.substring(baseName.length(), 2))+1;
				uniqueName=baseName+nextVal;
			}
			else {
				uniqueName+="_02";
			}
		}
		return uniqueName;
	}

	public static DataSetProvider get(int index) {
		if (index < 0){
			return null;
		}
		if (index >= dataSetProviderList.size()){
			return null;
		}
		return dataSetProviderList.get(index);
	}

	public static boolean hasIndex(int index) {
		try {
			dataSetProviderList.get(index);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static Integer indexOf(DataSetProvider dataSetProvider) {
		return dataSetProviderList.indexOf(dataSetProvider);
	}
	
	public static int indexOfDataSetName(String name) {
		Iterator<DataSetProvider> iterator = dataSetProviderList.iterator();
		while(iterator.hasNext()){
			DataSetProvider dataSetProvider = (DataSetProvider) iterator.next();
			if (dataSetProvider.getDataSetMD().getName().equals(name)){
				return DataSetKeeper.indexOf(dataSetProvider);
			}
		}
		return -1;
	}

	public static int size() {
		return dataSetProviderList.size();
	}

	public static int getByTdbResource(Resource tdbResource) {
		Iterator<DataSetProvider> iterator = dataSetProviderList.iterator();
		while (iterator.hasNext()) {
			DataSetProvider dataSetProvider = iterator.next();
			Resource resource = dataSetProvider.getTdbResource();
			if (resource.equals(tdbResource)) {
				return dataSetProviderList.indexOf(dataSetProvider);
			}
		}
		return -1;
	}

	public static DataSetProvider get(FileMD fileMD) {
		for(DataSetProvider dataSetProvider : dataSetProviderList){
			if(dataSetProvider.getFileMDList().contains(fileMD)){
				return dataSetProvider;
			}
		}
		return null;
	}

}
