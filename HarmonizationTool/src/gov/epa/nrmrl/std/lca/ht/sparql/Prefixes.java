package gov.epa.nrmrl.std.lca.ht.sparql;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FASC;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;
import gov.epa.nrmrl.std.lca.ht.vocabulary.OpenLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.SKOS;

import java.util.LinkedHashMap;
import java.util.Map;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class Prefixes {
	private static final Map<String, String> prefixMap = new LinkedHashMap<String, String>();

	// private static PrefixMapping prefixMapping = null;

	public static void createMap() {
		prefixMap.put("fedlca", FedLCA.NS); /* http://epa.gov/nrmrl/std/lca/fedlca/1.0# */
		prefixMap.put("lcaht", LCAHT.NS); /* http://epa.gov/nrmrl/std/lca/ht/1.0# */
		prefixMap.put("olca", OpenLCA.NS); /* http://openlca.org/schema/v1.0/ */
		prefixMap.put("eco", ECO.NS); /* http://ontology.earthster.org/eco/core# */
		prefixMap.put("fasc", FASC.NS); /* http://ontology.earthster.org/eco/fasc# */
		prefixMap.put("skos", SKOS.NS); /* http://www.w3.org/2004/02/skos/core# */

		prefixMap.put("afn", "http://jena.hpl.hp.com/ARQ/function#");
		prefixMap.put("fn", "http://www.w3.org/2005/xpath-functions#");
		prefixMap.put("xml", "http://www.w3.org/XML/1998/namespace");

		prefixMap.put("rdf", RDF.getURI()); /* http://www.w3.org/1999/02/22-rdf-syntax-ns# */
		prefixMap.put("rdfs", RDFS.getURI()); /* http://www.w3.org/2000/01/rdf-schema# */
		prefixMap.put("xsd", XSD.getURI()); /* http://www.w3.org/2001/XMLSchema# */

		prefixMap.put("owl", OWL.NS); /* http://www.w3.org/2002/07/owl# */
		prefixMap.put("dcterms", DCTerms.NS); /* http://purl.org/dc/terms/ */
		syncPrefixMapToTDBModel();
	}

	public static void syncPrefixMapToTDBModel() {
		if (prefixMap == null) {
			createMap();
		}
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.tdbDataset.getDefaultModel();
		try {
			for (String key : prefixMap.keySet()) {
				String value = prefixMap.get(key);
				tdbModel.setNsPrefix(key, value);
			}
		} catch (Exception e) {
			System.out.println("Import failed with Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public static Map<String, String> getPrefixmap() {
		if (prefixMap == null) {
			createMap();
		}
		return prefixMap;
	}

	public static String getPrefixesForQuery() {
		if (prefixMap.size() < 2) {
			createMap();
		}
		StringBuilder b = new StringBuilder();
		for (String key : prefixMap.keySet()) {
			b.append("PREFIX " + key + ": <" + prefixMap.get(key) + ">\n");
		}
		return b.toString();
	}
}
