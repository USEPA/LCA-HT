package harmonizationtool.model;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.vocabulary.ECO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
			Model model = ActiveTDB.model;
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
			ActiveTDB.removeAllWithSubject(tdbResource);
			ActiveTDB.removeAllWithObject(tdbResource);
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
		Pattern numberedEnding = Pattern.compile("(.*)_(\\d+)$");
		 
		while (indexOfDataSetName(uniqueName) >-1){
			
//			System.out.println("checking uniqueName: "+uniqueName);
//			Pattern numberedEnding = Pattern.compile("_\\d\\d$");
			Matcher endingMatcher = numberedEnding.matcher(uniqueName);
			
			if (endingMatcher.find()){
				String baseName = endingMatcher.group(1);
//				System.out.println("Match, so baseName: "+baseName);

				int nextVal=Integer.parseInt(endingMatcher.group(2))+1;
				uniqueName=baseName+"_"+nextVal;
//				System.out.println("... and uniqueName is now: "+uniqueName);

			}
			else {
				uniqueName+="_1";
//				System.out.println("No match, so uniqueName is now: "+uniqueName);
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
