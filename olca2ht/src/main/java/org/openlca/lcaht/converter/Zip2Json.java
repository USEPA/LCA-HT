package org.openlca.lcaht.converter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.ZipStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts an openLCA JSON-LD zip package to a single JSON file.
 */
class Zip2Json implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private File zipFile;
	private File jsonFile;

	public Zip2Json(File zipFile, File jsonFile) {
		this.zipFile = zipFile;
		this.jsonFile = jsonFile;
	}

	@Override
	public void run() {
		log.info("Convert {} to {}", zipFile, jsonFile);
		try (ZipStore store = ZipStore.open(zipFile);
		     FileOutputStream fos = new FileOutputStream(jsonFile);
		     OutputStreamWriter w = new OutputStreamWriter(fos, "utf-8");
		     JsonWriter writer = new JsonWriter(w)) {
			writer.beginObject();
			writer.name("@graph");
			writer.beginArray();
			writeObjects(store, writer);
			writer.endArray();
			writer.endObject();
			log.info("file {} created", jsonFile);
		} catch (Exception e) {
			log.error("Conversion failed", e);
		}
	}

	private void writeObjects(ZipStore store, JsonWriter writer) {
		ModelType[] types = {ModelType.LOCATION, ModelType.CATEGORY,
				ModelType.ACTOR, ModelType.SOURCE, ModelType.UNIT_GROUP,
				ModelType.FLOW_PROPERTY, ModelType.FLOW, ModelType.IMPACT_METHOD,
				ModelType.IMPACT_CATEGORY, ModelType.PROCESS};
		Gson gson = new Gson();
		for (ModelType type : types) {
			for(String refId : store.getRefIds(type)) {
				JsonObject object = store.get(type, refId);
				gson.toJson(object, writer);
			}
		}
	}
}
