package gov.epa.nrmrl.std.lca.ht.utils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.hp.hpl.jena.rdf.model.Resource;

public class DataSourceMap {
	private Map<Integer, Resource> idToResource = new ConcurrentHashMap<Integer, Resource>();
	private Map<Resource, Integer> resourceToId = new ConcurrentHashMap<Resource, Integer>();
	private static final DataSourceMap instance = new DataSourceMap();

	private DataSourceMap() {
	}

	public static DataSourceMap getInstance() {
		return instance;
	}

	synchronized public void put(Integer id, Resource resource) {
		idToResource.put(id, resource);
		resourceToId.put(resource, id);
	}

	synchronized public Resource removeById(Integer id) {
		Resource removedResource = idToResource.remove(id);
		resourceToId.remove(removedResource);
		return removedResource;
	}

	synchronized public Integer removeByDataSource(Resource resource) {
		Integer removedId = resourceToId.remove(resource);
		idToResource.remove(removedId);
		return removedId;
	}

	public boolean containsId(Integer id) {
		return idToResource.containsKey(id);
	}

	public boolean containsDataSource(Resource resource) {
		return idToResource.containsValue(resource);
	}

	public Integer getId(Resource resource) {
		if (resourceToId.containsKey(resource)) {
			return resourceToId.get(resource);
		} else {
			return putNew(resource);
		}
	}

	public Resource getDataSource(Integer id) {
		return idToResource.get(id);
	}

	public int size() {
		return idToResource.size();
	}

	public boolean renumber(int from, int to) {
		if (idToResource.containsKey(to)) {
			return false;
		} else {
			idToResource.put(to, idToResource.get(from));
			idToResource.remove(from);
			resourceToId.put(idToResource.get(to), to);
			return true;
		}
	}

	public int getNext() {
		Set<Integer> indexes = idToResource.keySet();
		Integer max = -1;
		while (indexes.iterator().hasNext()) {
			Integer id = indexes.iterator().next();
			max = Math.max(max, id);
		}
		return max + 1;
	}

	synchronized public Integer putNew(Resource resource) {
		Integer id = getNext();
		idToResource.put(id, resource);
		resourceToId.put(resource, id);
		return id;
	}
}
