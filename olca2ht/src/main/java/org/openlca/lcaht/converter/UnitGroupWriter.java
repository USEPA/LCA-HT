package org.openlca.lcaht.converter;

import java.util.List;
import java.util.Map;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.ZipStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UnitGroupWriter implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Map<String, JsonObject> groups;
	private Map<String, JsonObject> units;
	private Map<String, JsonObject> categories;
	private Map<String, JsonObject> properties;
	private ZipStore store;

	UnitGroupWriter(Map<String, Map<String, JsonObject>> maps, ZipStore store) {
		this.groups = maps.get(Type.UnitGroup);
		this.units = maps.get(Type.Unit);
		this.categories = maps.get(Type.Category);
		this.properties = maps.get(Type.FlowProperty);
		this.store = store;
	}

	@Override
	public void run() {
		if (groups == null) {
			log.info("no unit groups found");
			return;
		}
		try {
			for (String groupId : groups.keySet()) {
				JsonObject group = groups.get(groupId);
				IO.setRef(group, "category", categories);
				IO.setRef(group, "defaultFlowProperty", properties);
				addUnits(group);
				log.info("write unit group {}", groupId);
				store.put(ModelType.UNIT_GROUP, group);
			}
		} catch (Exception e) {
			log.error("Failed to write unit groups", e);
		}
	}

	private void addUnits(JsonObject group) {
		List<String> ids = IO.getIds(group, "units");
		if (ids.isEmpty())
			return;
		JsonArray units = new JsonArray();
		for (String id : ids) {
			JsonObject unit = getUnit(id);
			if (unit != null)
				units.add(unit);
			else
				log.warn("could not get unit {}", id);
		}
		group.add("units", units);
	}

	private JsonObject getUnit(String id) {
		if (id == null || units == null)
			return null;
		return units.get(id);
	}
}
