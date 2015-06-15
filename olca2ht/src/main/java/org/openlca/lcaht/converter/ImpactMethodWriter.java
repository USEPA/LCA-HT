package org.openlca.lcaht.converter;

import java.util.Map;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.ZipStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ImpactMethodWriter implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ZipStore store;
	private Map<String, Map<String, JsonObject>> maps;

	ImpactMethodWriter(Map<String, Map<String, JsonObject>> maps, ZipStore store) {
		this.store = store;
		this.maps = maps;
	}

	@Override
	public void run() {
		Map<String, JsonObject> methods = maps.get(Type.ImpactMethod);
		if(methods == null) {
			log.info("no impact assessment methods found");
			return;
		}
		try {
			for(String id : methods.keySet()) {
				JsonObject method = methods.get(id);
				IO.setRef(method, "category", maps.get(Type.Category));
				addCategories(method);
				log.info("write impact method {}", id);
				store.put(ModelType.IMPACT_METHOD, method);
			}
		} catch (Exception e) {
			log.error("failed to write LCIA methods", e);
		}
	}

	private void addCategories(JsonObject method) {
		Map<String, JsonObject> cats = maps.get(Type.ImpactCategory);
		if(cats == null)
			return;
		JsonArray categories = new JsonArray();
		for(String catId : IO.getIds(method, "impactCategories")){
			JsonObject cat = cats.get(catId);
			if(cat == null)
				continue;
			JsonObject ref = IO.makeRef(cat);
			categories.add(ref);
		}
		method.add("impactCategories", categories);
	}

}
