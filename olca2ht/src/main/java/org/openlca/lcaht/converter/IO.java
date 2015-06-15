package org.openlca.lcaht.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class IO {

	static final String BASE_URI = "http://openlca.org/schema/v1.0/";

	static List<String> getIds(JsonObject obj, String idProperty) {
		if (obj == null || idProperty == null)
			return Collections.emptyList();
		JsonElement e = obj.get(idProperty);
		if (e == null)
			return Collections.emptyList();
		if (e.isJsonPrimitive())
			return Arrays.asList(cutBaseUri(e.getAsString()));
		if (!e.isJsonArray())
			return Collections.emptyList();
		List<String> ids = new ArrayList<>();
		for (JsonElement item : e.getAsJsonArray()) {
			if (!item.isJsonPrimitive())
				continue;
			ids.add(cutBaseUri(item.getAsString()));
		}
		return ids;
	}

	static String getId(JsonObject obj) {
		return getBaseValue("@id", obj);
	}

	static String getType(JsonObject obj) {
		return getBaseValue("@type", obj);
	}

	private static String getBaseValue(String key, JsonObject obj) {
		if (obj == null)
			return null;
		JsonElement elem = obj.get(key);
		if (elem == null || !elem.isJsonPrimitive())
			return null;
		String typeStr = elem.getAsString();
		return cutBaseUri(typeStr);
	}

	static void cutBaseUris(JsonObject obj) {
		if (obj == null)
			return;
		ArrayList<String> baseKeys = new ArrayList<>();
		for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
			if (e.getKey().startsWith(BASE_URI))
				baseKeys.add(e.getKey());
			JsonElement elem = e.getValue();
			if (elem.isJsonObject())
				cutBaseUris(elem.getAsJsonObject());
			else if (elem.isJsonPrimitive()) {
				JsonPrimitive prim = elem.getAsJsonPrimitive();
				if (prim.isString()) {
					String val = cutBaseUri(prim.getAsString());
					e.setValue(new JsonPrimitive(val));
				}
			}
		}
		cutKeys(baseKeys, obj);
	}

	private static void cutKeys(ArrayList<String> baseKeys, JsonObject obj) {
		for (String key : baseKeys) {
			JsonElement elem = obj.get(key);
			obj.remove(key);
			String newKey = cutBaseUri(key);
			obj.add(newKey, elem);
		}
	}

	static String cutBaseUri(String s) {
		if (s == null)
			return null;
		String t = s.trim();
		if (t.startsWith(BASE_URI))
			return t.substring(BASE_URI.length());
		else
			return t;
	}

	static JsonObject makeRef(JsonElement e) {
		if (e == null || !e.isJsonObject())
			return null;
		JsonObject entity = e.getAsJsonObject();
		JsonObject ref = new JsonObject();
		ref.add("@id", entity.get("@id"));
		ref.add("@type", entity.get("@type"));
		ref.add("name", entity.get("name"));
		return ref;
	}

	static String getString(JsonObject obj, String property) {
		if (obj == null || property == null)
			return null;
		JsonElement elem = obj.get(property);
		if (elem == null || !elem.isJsonPrimitive())
			return null;
		else
			return elem.getAsString();
	}

	static void setRef(JsonObject obj, String idProperty, Map<String,
			JsonObject> map) {
		if (obj == null || idProperty == null || map == null)
			return;
		String id = IO.getString(obj, idProperty);
		if (id == null)
			return;
		obj.remove(idProperty);
		JsonObject ref = IO.makeRef(map.get(id));
		obj.add(idProperty, ref);
	}

}
