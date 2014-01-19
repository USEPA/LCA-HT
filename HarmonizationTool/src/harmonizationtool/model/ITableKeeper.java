package harmonizationtool.model;

import java.util.HashMap;
import java.util.Map;

public class ITableKeeper {
	private static Map<String, ITableProvider> map = new HashMap<String, ITableProvider>();

	public static void saveITableProvider(String key, ITableProvider iTableProvider) {
		map.put(key, iTableProvider);
	}

	public static ITableProvider getITableProvider(String key) {
		return map.get(key);
	}
	public static int getITableKeeperSize(){
		int size = map.size();
		return size;
	}
	public static  void remove(String key) {
		if(map.containsKey(key)){
			System.out.println("ITableKeeper removing key: "+key);
			 map.remove(key);
		}else{
			System.out.println("ITableKeeper does not contain key: "+key);
		}
	}
}
