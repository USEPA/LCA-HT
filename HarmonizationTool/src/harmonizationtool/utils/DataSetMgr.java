package harmonizationtool.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataSetMgr {

    public class DataSetMap<Int, DataSet>{
        private Map<Int, DataSet> keyToValueMap = new ConcurrentHashMap<Int, DataSet>();
        private Map<DataSet, Int> valueToKeyMap = new ConcurrentHashMap<DataSet, Int>();

        synchronized public void put(Int key, DataSet value){
            keyToValueMap.put(key, value);
            valueToKeyMap.put(value, key);
        }

        synchronized public DataSet removeById(Int key){
            DataSet removedValue = keyToValueMap.remove(key);
            valueToKeyMap.remove(removedValue);
            return removedValue;
        }

        synchronized public Int removeByDataSet(DataSet value){
            Int removedKey = valueToKeyMap.remove(value);
            keyToValueMap.remove(removedKey);
            return removedKey;
        }

        public boolean containsId(Int key){
            return keyToValueMap.containsKey(key);
        }

        public boolean containsDataSet(DataSet value){
            return keyToValueMap.containsValue(value);
        }

        public Int getId(DataSet value){
            return valueToKeyMap.get(value);
        }

        public DataSet getDataSet(Int key){
            return keyToValueMap.get(key);
        }
    }
}
