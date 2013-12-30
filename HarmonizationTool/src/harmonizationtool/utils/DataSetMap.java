package harmonizationtool.utils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.hp.hpl.jena.rdf.model.Resource;

//public class DataSetMgr {

//	private Integer DSid = null;
    public class DataSetMap{
        private Map<Integer, Resource> idToResource = new ConcurrentHashMap<Integer, Resource>();
        private Map<Resource, Integer> resourceToId = new ConcurrentHashMap<Resource, Integer>();
        private static final DataSetMap instance = new DataSetMap();
        
        private DataSetMap(){       	
        }
        public static DataSetMap getInstance(){
        	return instance;
        }

        synchronized public void put(Integer key, Resource value){
            idToResource.put(key, value);
            resourceToId.put(value, key);
        }

        synchronized public Resource removeById(Integer key){
            Resource removedValue = idToResource.remove(key);
            resourceToId.remove(removedValue);
            return removedValue;
        }

        synchronized public Integer removeByDataSet(Resource value){
            Integer removedKey = resourceToId.remove(value);
            idToResource.remove(removedKey);
            return removedKey;
        }

        public boolean containsId(Integer key){
            return idToResource.containsKey(key);
        }

        public boolean containsDataSet(Resource value){
            return idToResource.containsValue(value);
        }

        public Integer getId(Resource value){
            return resourceToId.get(value);
        }

        public Resource getDataSet(Integer key){
            return idToResource.get(key);
        }
        public int getNext(){
        	Set<Integer> indexes = idToResource.keySet();
        	int max = -1;
        	while(indexes.iterator().hasNext()){
        		Integer id = indexes.iterator().next();
//        		int id = DSid.int();
//        		max = Math.max(max,id); // FIXME need to type Int as integer
        	}
			return max;
        }
    }
//}
