package harmonizationtool.model;

import java.util.HashMap;
import java.util.Map;

public class TableKeeper {
	private static Map<String, TableProvider> map = new HashMap<String, TableProvider>();

	public static void saveTableProvider(String key, TableProvider tableProvider) {
		map.put(key, tableProvider);
	}

	public static TableProvider getTableProvider(String key) {
		return map.get(key);
	}
	public static int getTableKeeperSize(){
		int size = map.size();
		return size;
	}
	public static  void remove(String key) {
		if(map.containsKey(key)){
			System.out.println("TableKeeper removing key: "+key);
			 map.remove(key);
		}else{
			System.out.println("TableKeeper does not contain key: "+key);
		}
	}
}
