package org.openlca.lcaht.converter;

import java.util.Map;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.ZipStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowPropertyWriter implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Map<String, JsonObject> unitGroups;
	private Map<String, JsonObject> categories;
	private Map<String, JsonObject> properties;
	private ZipStore store;

	FlowPropertyWriter(Map<String, Map<String, JsonObject>> maps, ZipStore store) {
		this.unitGroups = maps.get(Type.UnitGroup);
		this.categories = maps.get(Type.Category);
		this.properties = maps.get(Type.FlowProperty);
		this.store = store;
	}

	@Override
	public void run() {
		if(properties == null) {
			log.info("no flow properties found");
			return;
		}
		try {
			for(String propId : properties.keySet()) {
				JsonObject obj = properties.get(propId);
				IO.setRef(obj, "category", categories);
				IO.setRef(obj, "unitGroup", unitGroups);
				log.info("write flow property {}", propId);
				store.put(ModelType.FLOW_PROPERTY, obj);
			}
		} catch (Exception e) {
			log.error("failed to write flow properties", e);
		}
	}
}
