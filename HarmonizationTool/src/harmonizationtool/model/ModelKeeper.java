package harmonizationtool.model;

import java.util.HashMap;
import java.util.Map;

public class ModelKeeper {
	private static Map<String, ModelProvider> map = new HashMap<String, ModelProvider>();

	public static void saveModelProvider(String key, ModelProvider modelProvider) {
		map.put(key, modelProvider);
	}

	public static ModelProvider getModelProvider(String key) {
		return map.get(key);
	}
	public static int getModelKeeperSize(){
		int size = map.size();
		return size;
	}
	public static  void remove(String key) {
		if(map.containsKey(key)){
			System.out.println("ModelKeeper removing key: "+key);
			 map.remove(key);
		}else{
			System.out.println("ModelKeeper does not contain key: "+key);
		}
	}
}
