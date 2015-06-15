package org.openlca.lcaht.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.ZipStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Json2Zip implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Map<String, Map<String, JsonObject>> maps = new HashMap<>();

	private File htFile;
	private File zipFile;

	public Json2Zip(File htFile, File zipFile) {
		this.htFile = htFile;
		this.zipFile = zipFile;
	}

	@Override
	public void run() {
		log.info("convert {} to {}", htFile, zipFile);
		try (ZipStore store = ZipStore.open(zipFile);
		     FileInputStream fis = new FileInputStream(htFile);
		     InputStreamReader isr = new InputStreamReader(fis, "utf-8");
		     JsonReader reader = new JsonReader(isr)) {
			boolean found = findArray(reader);
			if (!found) {
				log.error("Did not found array in json");
				return;
			}
			parseArray(reader);
			writeData(store);
		} catch (Exception e) {
			log.error("failed to convert file", e);
		}
	}

	private void writeData(ZipStore store) {
		writeSimpleType(store, Type.Location, ModelType.LOCATION);
		writeSimpleType(store, Type.Actor, ModelType.ACTOR);
		writeSimpleType(store, Type.Source, ModelType.SOURCE);
		new CategoryWriter(maps, store).run();
		new UnitGroupWriter(maps, store).run();
		new FlowPropertyWriter(maps, store).run();
		new FlowWriter(maps, store).run();
		new ProcessWriter(maps, store).run();
		new ImpactMethodWriter(maps, store).run();
		new ImpactCategoryWriter(maps, store).run();
	}

	private void writeSimpleType(ZipStore store, String type, ModelType model) {
		Map<String, JsonObject> map = maps.get(type);
		Map<String, JsonObject> categories = maps.get(Type.Category);
		if (map == null)
			return;
		for (String id : map.keySet()) {
			JsonObject obj = map.get(id);
			IO.setRef(obj, "category", categories);
			log.info("write {}: {}", model, id);
			store.put(model, obj);
		}
	}

	private void parseArray(JsonReader reader) throws IOException {
		reader.beginArray();
		Gson gson = new Gson();
		while (reader.hasNext()) {
			JsonToken t = reader.peek();
			if (t != JsonToken.BEGIN_OBJECT)
				break;
			cacheObject(reader, gson);
		}
	}

	private void cacheObject(JsonReader reader, Gson gson) {
		JsonObject obj = gson.fromJson(reader, JsonObject.class);
		IO.cutBaseUris(obj);
		String type = IO.getType(obj);
		Map<String, JsonObject> map = getMap(type);
		String id = IO.getId(obj);
		if (map == null || id == null)
			return;
		map.put(id, obj);
	}

	private Map<String, JsonObject> getMap(String type) {
		if (type == null)
			return null;
		Map<String, JsonObject> map = maps.get(type);
		if (map != null)
			return map;
		log.trace("create new cache for type {}", type);
		map = new HashMap<>();
		maps.put(type, map);
		return map;
	}

	/**
	 * Find the array where the triples are stored. A direct input of a json
	 * array ([...]) or an json object with a @graph attribute which holds this
	 * array are supported ({"@graph": [...]}). Returns true if the reader is
	 * positioned to read this array, otherwise it returns false.
	 */
	private boolean findArray(JsonReader reader) throws Exception {
		JsonToken token = reader.peek();
		if (token == JsonToken.BEGIN_ARRAY)
			return true;
		if (token != JsonToken.BEGIN_OBJECT)
			return false;
		reader.beginObject();
		token = reader.peek();
		if (token != JsonToken.NAME)
			return false;
		String name = reader.nextName();
		if (!Objects.equals("@graph", name))
			return false;
		token = reader.peek();
		return token == JsonToken.BEGIN_ARRAY;
	}
}
