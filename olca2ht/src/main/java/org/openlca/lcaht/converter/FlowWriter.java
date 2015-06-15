package org.openlca.lcaht.converter;

import java.util.List;
import java.util.Map;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.ZipStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowWriter implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ZipStore store;
	private Map<String, JsonObject> flows;
	private Map<String, JsonObject> categories;
	private Map<String, JsonObject> factors;
	private Map<String, JsonObject> properties;

	FlowWriter(Map<String, Map<String, JsonObject>> maps, ZipStore store) {
		this.store = store;
		this.flows = maps.get(Type.Flow);
		this.categories = maps.get(Type.Category);
		this.factors = maps.get(Type.FlowPropertyFactor);
		this.properties = maps.get(Type.FlowProperty);
	}

	@Override
	public void run() {
		if (flows == null) {
			log.info("no flows found");
			return;
		}
		try {
			for (String flowId : flows.keySet()) {
				JsonObject flow = flows.get(flowId);
				IO.setRef(flow, "category", categories);
				addFactors(flow);
				log.info("write flow {}", flowId);
				store.put(ModelType.FLOW, flow);
			}
		} catch (Exception e) {
			log.error("failed to write flows", e);
		}
	}

	private void addFactors(JsonObject flow) {
		List<String> ids = IO.getIds(flow, "flowProperties");
		if (ids.isEmpty())
			return;
		JsonArray array = new JsonArray();
		for (String id : ids) {
			JsonObject factor = factors.get(id);
			if (factor == null) {
				log.warn("Could not get flow property factor {}", id);
				continue;
			}
			IO.setRef(factor, "flowProperty", properties);
			array.add(factor);
		}
		flow.add("flowProperties", array);
	}
}
