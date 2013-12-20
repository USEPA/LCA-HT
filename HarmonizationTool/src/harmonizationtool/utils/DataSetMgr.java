package harmonizationtool.utils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DataSetMgr {

//	private Integer DSid = null;
    public class DataSetMap<DSid, DataSet>{
        private Map<DSid, DataSet> keyToValueMap = new ConcurrentHashMap<DSid, DataSet>();
        private Map<DataSet, DSid> valueToKeyMap = new ConcurrentHashMap<DataSet, DSid>();

        synchronized public void put(DSid key, DataSet value){
            keyToValueMap.put(key, value);
            valueToKeyMap.put(value, key);
        }

        synchronized public DataSet removeById(DSid key){
            DataSet removedValue = keyToValueMap.remove(key);
            valueToKeyMap.remove(removedValue);
            return removedValue;
        }

        synchronized public DSid removeByDataSet(DataSet value){
            DSid removedKey = valueToKeyMap.remove(value);
            keyToValueMap.remove(removedKey);
            return removedKey;
        }

        public boolean containsId(DSid key){
            return keyToValueMap.containsKey(key);
        }

        public boolean containsDataSet(DataSet value){
            return keyToValueMap.containsValue(value);
        }

        public DSid getId(DataSet value){
            return valueToKeyMap.get(value);
        }

        public DataSet getDataSet(DSid key){
            return keyToValueMap.get(key);
        }
        public int getNext(){
        	Set<DSid> indexes = keyToValueMap.keySet();
        	int max = -1;
        	while(indexes.iterator().hasNext()){
        		DSid id = indexes.iterator().next();
//        		int id = DSid.int();
//        		max = Math.max(max,id); // FIXME need to type Int as integer
        	}
			return max;
        }
    }
}
