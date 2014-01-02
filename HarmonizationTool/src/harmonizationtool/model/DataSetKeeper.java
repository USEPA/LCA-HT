package harmonizationtool.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

public class DataSetKeeper {
	
	private static List<DataSetProvider> dsList = new ArrayList<DataSetProvider>();

	private DataSetKeeper() {
	}
	
	public static boolean add(DataSetProvider dataSetProvider){
		return dsList.add(dataSetProvider);
	}
	public static List<Integer> getIDs(){
		List<Integer> ids = new ArrayList<Integer>();
		Iterator<DataSetProvider> iterator = dsList.iterator();
		while(iterator.hasNext()){
			ids.add(dsList.indexOf(iterator.next()));
		}
		return ids;
	}
	
	public static DataSetProvider get(int index){
		return dsList.get(index);
	}

	public static Integer indexOf(DataSetProvider dataSetProvider) {
		return dsList.indexOf(dataSetProvider);
	}

	public static int size() {
		return dsList.size();
	}
	public static int getByTdbResource(Resource tdbResource){
		Iterator<DataSetProvider> iterator = dsList.iterator();
		while (iterator.hasNext()){
			DataSetProvider dataSetProvider = iterator.next();
			Resource resource = dataSetProvider.getTdbResource();
			if (resource == tdbResource){
				return dsList.indexOf(dataSetProvider);
			}
		}
		return -1;		
	}
	
}
