package org.openlca.lcaht.converter;

import java.util.Map;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.ZipStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessWriter implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ZipStore store;
	private Map<String, JsonObject> processes;
	private Map<String, Map<String, JsonObject>> maps;

	ProcessWriter(Map<String, Map<String, JsonObject>> maps, ZipStore store) {
		this.store = store;
		this.processes = maps.get(Type.Process);
		this.maps = maps;
	}

	@Override
	public void run() {
		if (processes == null) {
			log.info("no processes found");
			return;
		}
		try {
			for (String id : processes.keySet()) {
				JsonObject process = processes.get(id);
				IO.setRef(process, "category", maps.get(Type.Category));
				IO.setRef(process, "location", maps.get(Type.Location));
				addDoc(process);
				addExchanges(process);
				log.info("write process {}", id);
				store.put(ModelType.PROCESS, process);
			}
		} catch (Exception e) {
			log.error("failed to write processes", e);
		}
	}

	private void addDoc(JsonObject process) {
		JsonElement docId = process.get("processDocumentation");
		Map<String, JsonObject> docMap = maps.get(Type.ProcessDocumentation);
		if (docId == null || docMap == null)
			return;
		JsonObject doc = docMap.get(docId.getAsString());
		process.add("processDocumentation", doc);
		if (doc == null)
			return;
		IO.setRef(doc, "reviewer", maps.get(Type.Actor));
		IO.setRef(doc, "dataDocumentor", maps.get(Type.Actor));
		IO.setRef(doc, "dataGenerator", maps.get(Type.Actor));
		IO.setRef(doc, "dataSetOwner", maps.get(Type.Actor));
		IO.setRef(doc, "publication", maps.get(Type.Source));
		addSources(doc);
	}

	private void addSources(JsonObject doc) {
		Map<String, JsonObject> sourceMap = maps.get(Type.Source);
		if (sourceMap == null)
			return;
		JsonArray sources = new JsonArray();
		for (String sourceId : IO.getIds(doc, "sources")) {
			JsonObject source = sourceMap.get(sourceId);
			if (source == null)
				continue;
			JsonObject ref = IO.makeRef(source);
			sources.add(ref);
		}
		doc.add("sources", sources);
	}

	private void addExchanges(JsonObject process) {
		Map<String, JsonObject> exchanges = maps.get(Type.Exchange);
		if (exchanges == null)
			return;
		JsonArray array = new JsonArray();
		for (String id : IO.getIds(process, "exchanges")) {
			JsonObject exchange = exchanges.get(id);
			if (exchange == null)
				continue;
			addExchangeRefs(exchange);
			array.add(exchange);
		}
		process.add("exchanges", array);
	}

	private void addExchangeRefs(JsonObject exchange) {
		IO.setRef(exchange, "flow", maps.get(Type.Flow));
		IO.setRef(exchange, "flowProperty", maps.get(Type.FlowProperty));
		IO.setRef(exchange, "unit", maps.get(Type.Unit));
		JsonElement elem = exchange.get("uncertainty");
		Map<String, JsonObject> uncertainties = maps.get(Type.Uncertainty);
		if (elem == null || uncertainties == null)
			return;
		JsonObject uncertainty = uncertainties.get(elem.getAsString());
		exchange.add("uncertainty", uncertainty);
	}
}
