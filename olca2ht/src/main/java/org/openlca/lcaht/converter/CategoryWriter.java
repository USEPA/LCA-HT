package org.openlca.lcaht.converter;

import java.util.Map;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.ZipStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CategoryWriter implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Map<String, JsonObject> categories;
	private ZipStore store;

	CategoryWriter(Map<String, Map<String, JsonObject>> maps, ZipStore store) {
		this.categories = maps.get(Type.Category);
		this.store = store;
	}

	@Override
	public void run() {
		if (categories == null) {
			log.info("no categories found");
			return;
		}
		try {
			for (String categoryId : categories.keySet()) {
				JsonObject cat = categories.get(categoryId);
				IO.setRef(cat, "parentCategory", categories);
				log.info("write category {}", categoryId);
				store.put(ModelType.CATEGORY, cat);
			}
		} catch (Exception e) {
			log.error("Failed to write categories", e);
		}
	}
}
