package org.openlca.lcaht.converter;

import java.util.Map;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.ZipStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ImpactCategoryWriter implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ZipStore store;
	private Map<String, Map<String, JsonObject>> maps;

	ImpactCategoryWriter(Map<String, Map<String, JsonObject>> maps, ZipStore store) {
		this.store = store;
		this.maps = maps;
	}

	@Override
	public void run() {
		Map<String, JsonObject> cats = maps.get(Type.ImpactCategory);
		if(cats == null){
			log.info("no impact categories found");
			return;
		}
		try {
			for(String id : cats.keySet()) {
				JsonObject cat = cats.get(id);
				addFactors(cat);
				log.info("write impact category {}", id);
				store.put(ModelType.IMPACT_CATEGORY, cat);
			}
		} catch (Exception e) {
			log.error("failed to write LCIA categories", e);
		}
	}

	private void addFactors(JsonObject cat) {
		Map<String, JsonObject> facs = maps.get(Type.ImpactFactor);
		if(facs == null)
			return;
		JsonArray factors = new JsonArray();
		for(String facId : IO.getIds(cat, "impactFactors")) {
			JsonObject factor = facs.get(facId);
			if(factor == null)
				continue;
			addFactorRefs(factor);
			factors.add(factor);
		}
		cat.add("impactFactors", factors);
	}

	private void addFactorRefs(JsonObject factor) {
		IO.setRef(factor, "flow", maps.get(Type.Flow));
		IO.setRef(factor, "flowProperty", maps.get(Type.FlowProperty));
		IO.setRef(factor, "unit", maps.get(Type.Unit));
		JsonElement elem = factor.get("uncertainty");
		Map<String, JsonObject> uncertainties = maps.get(Type.Uncertainty);
		if (elem == null || uncertainties == null)
			return;
		JsonObject uncertainty = uncertainties.get(elem.getAsString());
		factor.add("uncertainty", uncertainty);
	}
}
