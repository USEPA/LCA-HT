package harmonizationtool.model;

import java.util.ArrayList;
import java.util.List;

public class DataSetKeeper {
	
	private static List<DataSetProvider> dsList = new ArrayList<DataSetProvider>();

	private DataSetKeeper() {
	}
	
	public boolean add(DataSetProvider dataSetProvider){
		return dsList.add(dataSetProvider);
	}
	
	public DataSetProvider get(int index){
		return dsList.get(index);
	}
	
}
