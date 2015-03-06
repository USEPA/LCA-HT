package gov.epa.nrmrl.std.lca.ht.flowContext.mgr;

import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataPropertyProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataValue;
import gov.epa.nrmrl.std.lca.ht.dataModels.QACheck;
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.tdb.ImportRDFFileDirectlyToGraph;
import gov.epa.nrmrl.std.lca.ht.utils.RDFUtil;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class FlowContext {
	// CLASS VARIABLES
	public static final String flowContextGeneral = "General";
	public static final String flowContextSpecific = "Specific";
	public static final String openLCAUUID = "openLCA UUID";
	private static final Resource rdfClass = FedLCA.FlowContext;
	// NOTE: EVENTUALLY label AND comment SHOULD COME FROM ONTOLOGY
	public static final String label = "Flow Context";
	//
	// public static final String comment =
	// "The Flow Context is a term developed for the LCA Harmonization Tool.  It encompases terms such as 'Category' or 'Compartment' and may have several descriptors including geological feature, population density, or land use.  A Flow has a hasFlowContext property with an object being a FlowContext.  "
	// +
	// "This term is similar to fasc:Compartment.  Examples of Flow Contexts include emissions to urban air and resource consumption from water.";

	// public static final String comment =
	// "Compartments are used for classifying effects.  Effects have a hasCompartment property and the type of the value of that property may be used to classify the effect.  Examples of compartments include emissions to urban air and resource consumption from water.";
	private static Map<String, LCADataPropertyProvider> dataPropertyMap;
	private static List<FlowContext> lcaMasterContexts = new ArrayList<FlowContext>();

	private static List<Pattern> regexGeneralString = new ArrayList<Pattern>();

	static {
		if (dataPropertyMap == null) {
			dataPropertyMap = new LinkedHashMap<String, LCADataPropertyProvider>();
			if (dataPropertyMap.isEmpty()) {
				// ActiveTDB.tsReplaceLiteral(rdfClass, RDFS.label, label);
				// ActiveTDB.tsAddGeneralTriple(rdfClass, RDFS.comment, comment, null);
				// ActiveTDB.tsAddGeneralTriple(rdfClass, RDF.type, OWL.Class, null);

				System.out.println("label assigned to Flow Context");

				LCADataPropertyProvider lcaDataPropertyProvider;

				lcaDataPropertyProvider = new LCADataPropertyProvider(flowContextGeneral);
				lcaDataPropertyProvider.setPropertyClass(label);
				lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
				lcaDataPropertyProvider.setRequired(true);
				lcaDataPropertyProvider.setUnique(true);
				lcaDataPropertyProvider.setLeftJustified(true);
				lcaDataPropertyProvider.setCheckLists(getContextNameCheckList());
				lcaDataPropertyProvider.setTDBProperty(FedLCA.flowContextGeneral);
				dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);

				lcaDataPropertyProvider = new LCADataPropertyProvider(flowContextSpecific);
				lcaDataPropertyProvider.setPropertyClass(label);
				lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
				lcaDataPropertyProvider.setRequired(false);
				lcaDataPropertyProvider.setUnique(false);
				lcaDataPropertyProvider.setLeftJustified(true);
				lcaDataPropertyProvider.setCheckLists(getContextNameCheckList());
				lcaDataPropertyProvider.setTDBProperty(FedLCA.flowContextSpecific);
				dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);

				lcaDataPropertyProvider = new LCADataPropertyProvider(openLCAUUID);
				lcaDataPropertyProvider.setPropertyClass(label);
				lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
				lcaDataPropertyProvider.setRequired(false);
				lcaDataPropertyProvider.setUnique(false);
				lcaDataPropertyProvider.setLeftJustified(true);
				// lcaDataPropertyProvider.setCheckLists(getContextNameCheckList());
				lcaDataPropertyProvider.setTDBProperty(FedLCA.hasOpenLCAUUID);
				dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);

				// Pattern airGeneralRE = Pattern.compile("air", Pattern.CASE_INSENSITIVE);
				// regexGeneralString.add(airGeneralRE);
				// addContext("air", "unspecified", FedLCA.airUnspecified, "5ea0e54a-d88d-4f7c-89a4-54f21c5791e7");
				// addContext("air", "low population density", FedLCA.airLow_population_density,
				// "ebcdff7a-b8c0-405b-8601-98a1ac3f26ef");
				// addContext("air", "high population density", FedLCA.airHigh_population_density,
				// "e6e67f13-0bcb-4113-966b-023c3186b339");
				// addContext("air", "low population density, long-term", FedLCA.airLow_population_densityLong_term,
				// "f9ac762d-1403-4763-9aec-9b11ab79874b");
				// addContext("air", "lower stratosphere + upper troposphere",
				// FedLCA.airLower_stratosphere_upper_troposphere, "885ce78b-9872-4a59-8244-deebeb12caea");
				//
				// Pattern waterGeneralRE = Pattern.compile("water", Pattern.CASE_INSENSITIVE);
				// regexGeneralString.add(waterGeneralRE);
				// addContext("water", "unspecified", FedLCA.waterUnspecified, "a7c280e9-d13a-43cf-9127-d3bbf4d0e256");
				// addContext("water", "fossil-", FedLCA.waterFossil, "d0d05279-8621-404d-9878-218f04427fa6");
				// addContext("water", "fresh-", FedLCA.waterFresh, "1657ede0-aec3-41d1-bf1d-eeada890bdce");
				// addContext("water", "fresh-, long-term", FedLCA.waterFreshLong_term,
				// "ed1e0813-ed99-4897-b20c-13ec90584825");
				// addContext("water", "ground-", FedLCA.waterGround, "4f146a17-ae4a-487b-874b-5d3013b86f44");
				// addContext("water", "ground-, long-term", FedLCA.waterGroundLong_term,
				// "eba77525-9745-4f4a-9182-91a67306ba1c");
				// addContext("water", "lake", FedLCA.waterLake, "c1069072-9923-48f6-821d-8fad6e0ace5b");
				// addContext("water", "ocean", FedLCA.waterOcean, "8b7c395f-60ef-4863-a7e6-3560b5ad1aae");
				// addContext("water", "river", FedLCA.waterRiver, "58ed0153-34aa-4d6f-babf-3cfb201eac1d");
				// addContext("water", "river, long-term", FedLCA.waterRiverLong_term,
				// "1df73ec9-e6b7-4f91-8f62-14b8ee2f7d93");
				// addContext("water", "surface", FedLCA.waterSurface, "782cf5cb-0a6b-44aa-8a87-e5997dd0d1ff");
				//
				// Pattern soilGeneralRE = Pattern.compile("soil", Pattern.CASE_INSENSITIVE);
				// regexGeneralString.add(soilGeneralRE);
				// addContext("soil", "unspecified", FedLCA.soilUnspecified, "e97d11b5-78e4-4a93-9a63-14673f89f709");
				// addContext("soil", "agricultural", FedLCA.soilAgricultural, "34efc703-6409-4acf-8f1d-dec646adca8c");
				// addContext("soil", "forestry", FedLCA.soilForestry, "b50bb945-da42-49d2-a6e1-73544e36aaf2");
				// addContext("soil", "industrial", FedLCA.soilIndustrial, "185a7592-e3ae-4c44-a124-9c700b76d33d");
				//
				// Pattern resourceGeneralRE = Pattern.compile("resource", Pattern.CASE_INSENSITIVE);
				// regexGeneralString.add(resourceGeneralRE);
				// addContext("resource", "unspecified", FedLCA.resourceUnspecified,
				// "0d557bab-d095-4142-912e-398fccb68240");
				// addContext("resource", "biotic", FedLCA.resourceBiotic, "26305d8d-591e-4927-8e19-ca7513edcee9");
				// addContext("resource", "in air", FedLCA.resourceIn_air, "965603be-3e94-42e6-9b2c-95eaf3b998c0");
				// addContext("resource", "in ground", FedLCA.resourceIn_ground,
				// "75c87bc3-468b-4d9f-b2c5-9d521fb4822e");
				// addContext("resource", "in land", FedLCA.resourceIn_land, "54f7604f-c04e-4404-a229-852ede4379dc");
				// addContext("resource", "in water", FedLCA.resourceIn_water, "bcfc6117-3461-4f85-a5c8-fe59a533cc29");
			}
		}
	}

	// INSTANCE VARIABLES
	private Resource tdbResource;
	private List<LCADataValue> lcaDataValues;
	private Resource matchingResource;
	private int firstRow;
	private String fullName = null;
	private String generalString = null;
	private String specificString = null;
	private String openLCAUUIDValue = null;

	/* An object must match all required match patterns for automatic match */
	private List<Pattern> requiredMatchPatterns = new ArrayList<Pattern>();
	/* An object must NOT match any forbidden match patterns to match */
	private List<Pattern> forbiddenMatchPatterns = new ArrayList<Pattern>();
	/* An object must match at least one of the sufficient match patterns to match */
	private List<Pattern> sufficientMatchPatterns = new ArrayList<Pattern>();

	// private Set<Resource> matchCandidates;

	// CONSTRUCTORS
	public FlowContext() {
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
		lcaDataValues = new ArrayList<LCADataValue>();
		matchingResource = null;
	}

	// private static void addContext(String generalString, String specificString, Resource tdbResource, String uuid) {
	// // FlowContext flowContext = new FlowContext();
	// FlowContext flowContext = new FlowContext(tdbResource, false);
	//
	// flowContext.tdbResource = tdbResource;
	// // THE ABOVE MUST BE DONE FIRST, SO THAT TRIPLES ARE ADDED PROPERLY
	// flowContext.generalString = generalString;
	// ActiveTDB.tsAddGeneralTriple(tdbResource, RDF.type, rdfClass, null);
	// ActiveTDB.tsAddGeneralTriple(tdbResource, FedLCA.flowContextGeneral, generalString, null);
	// flowContext.specificString = specificString;
	// ActiveTDB.tsAddGeneralTriple(tdbResource, FedLCA.flowContextSpecific, specificString, null);
	// flowContext.openLCAUUID = uuid;
	// ActiveTDB.tsAddGeneralTriple(tdbResource, FedLCA.hasOpenLCAUUID, uuid, null);
	// lcaMasterContexts.add(flowContext);
	// }

	// public FlowContext(Resource tdbResource) {
	// this.tdbResource = tdbResource;
	// lcaDataValues = new ArrayList<LCADataValue>();
	// // clearSyncDataFromTDB();
	// }
	//
	public FlowContext(Resource tdbResource, boolean sync) {
		this.tdbResource = tdbResource;
		lcaDataValues = new ArrayList<LCADataValue>();
		if (sync) {
			clearSyncDataFromTDB();
		}
	}

	public Object getOneProperty(String key) {
		for (LCADataValue lcaDataValue : lcaDataValues) {
			if (lcaDataValue.getLcaDataPropertyProvider().getPropertyName().equals(key)) {
				return lcaDataValue.getValue();
			}
		}
		return null;
	}

	public Object[] getAllProperties(String key) {
		List<Object> resultList = new ArrayList<Object>();
		for (LCADataValue lcaDataValue : lcaDataValues) {
			if (lcaDataValue.getLcaDataPropertyProvider().getPropertyName().equals(key)) {
				resultList.add(lcaDataValue.getValue());
			}
		}
		Object[] results = new Object[resultList.size()];
		if (resultList.size() == 0) {
			return null;
		}
		for (int i = 0; i < resultList.size(); i++) {
			results[i] = resultList.get(i);
		}
		return results;
	}

	public List<LCADataValue> getPropertyValuesInOrder() {
		List<LCADataValue> results = new ArrayList<LCADataValue>();
		for (String key : dataPropertyMap.keySet()) {
			for (LCADataValue lcaDataValue : lcaDataValues) {
				if (lcaDataValue.getLcaDataPropertyProvider().getPropertyName().equals(key)) {
					results.add(lcaDataValue);
				}
			}
		}
		return results;
	}

	public void setProperty(String key, Object object) {
		if (object == null) {
			return;
		}
		if (!dataPropertyMap.containsKey(key)) {
			return;
		}
		LCADataPropertyProvider lcaDataPropertyProvider = dataPropertyMap.get(key);
		RDFDatatype rdfDatatype = lcaDataPropertyProvider.getRdfDatatype();
		Class<?> objectClass = RDFUtil.getJavaClassFromRDFDatatype(rdfDatatype);
		if (!objectClass.equals(object.getClass())) {
			return;
		}
		LCADataValue newLCADataValue = new LCADataValue();
		newLCADataValue.setLcaDataPropertyProvider(lcaDataPropertyProvider);
		newLCADataValue.setValue(object);
		// newLCADataValue.setValueAsString(object.toString()); // SHOULD WE DO THIS AT ALL?

		if (lcaDataPropertyProvider.isUnique()) {
			removeValues(lcaDataPropertyProvider.getPropertyName());
			ActiveTDB.tsReplaceLiteral(tdbResource, lcaDataPropertyProvider.getTDBProperty(), object);
		} else {
			ActiveTDB.tsAddGeneralTriple(tdbResource, lcaDataPropertyProvider.getTDBProperty(), object, null);
		}
		lcaDataValues.add(newLCADataValue);
	}

	private void removeValues(String key) {
		for (LCADataValue lcaDataValue : lcaDataValues) {
			if (lcaDataValue.getLcaDataPropertyProvider().getPropertyName().equals(key)) {
				lcaDataValues.remove(lcaDataValue);
			}
		}
	}

	public void updateSyncDataFromTDB() {
		if (tdbResource == null) {
			return;
		}
		// LCADataPropertyProvider LIST IS ALL LITERALS
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		for (LCADataPropertyProvider lcaDataPropertyProvider : dataPropertyMap.values()) {
			if (!tdbResource.hasProperty(lcaDataPropertyProvider.getTDBProperty())) {
				continue;
			}
			if (lcaDataPropertyProvider.isUnique()) {
				removeValues(lcaDataPropertyProvider.getPropertyName());
				Object value = tdbResource.getProperty(lcaDataPropertyProvider.getTDBProperty()).getLiteral()
						.getValue();
				if (value.getClass().equals(
						RDFUtil.getJavaClassFromRDFDatatype(lcaDataPropertyProvider.getRdfDatatype()))) {
					LCADataValue lcaDataValue = new LCADataValue();
					lcaDataValue.setLcaDataPropertyProvider(lcaDataPropertyProvider);
					lcaDataValue.setValue(value);
					lcaDataValues.add(lcaDataValue);
				}
			} else {
				StmtIterator stmtIterator = tdbResource.listProperties(lcaDataPropertyProvider.getTDBProperty());
				while (stmtIterator.hasNext()) {
					Object value = stmtIterator.nextStatement().getLiteral().getValue();
					if (value.getClass().equals(
							RDFUtil.getJavaClassFromRDFDatatype(lcaDataPropertyProvider.getRdfDatatype()))) {
						LCADataValue lcaDataValue = new LCADataValue();
						lcaDataValue.setLcaDataPropertyProvider(lcaDataPropertyProvider);
						lcaDataValue.setValue(value);
						lcaDataValues.add(lcaDataValue);
					}
				}
			}
		}
		ActiveTDB.tdbDataset.end();
	}

	public void clearSyncDataFromTDB() {
		lcaDataValues.clear();
		updateSyncDataFromTDB();
	}

	private static List<QACheck> getContextNameCheckList() {
		List<QACheck> qaChecks = QACheck.getGeneralQAChecks();
		return qaChecks;
	}

	public Resource getTdbResource() {
		return tdbResource;
	}

	public static Resource getRdfclass() {
		return rdfClass;
	}

	public static Map<String, LCADataPropertyProvider> getDataPropertyMap() {
		return dataPropertyMap;
	}

	public Resource getMatchingResource() {
		return matchingResource;
	}

	public void setMatchingResource(Resource matchingResource) {
		this.matchingResource = matchingResource;
		ActiveTDB.tsReplaceResourceSameType(tdbResource, OWL.sameAs, matchingResource, null);
	}

	public int getFirstRow() {
		return firstRow;
	}

	public void setFirstRow(int firstRow) {
		this.firstRow = firstRow;
	}

	public String getDataSource() {
		return "Master List";
	}

	public boolean setMatches() {
		String generalString = (String) getOneProperty(flowContextGeneral);
		String specificString = (String) getOneProperty(flowContextSpecific);
		if (generalString == null) {
			return false;
		}
		if (specificString == null) {
			return false;
		}

		String udSpecificString = specificString.replaceAll("[()]", "");

		for (Pattern pattern : regexGeneralString) {
			Matcher matcher1 = pattern.matcher(generalString);
			if (matcher1.find()) {
				for (FlowContext flowContext : lcaMasterContexts) {
					String masterGeneralString = flowContext.generalString;
					Matcher matcher2 = pattern.matcher(masterGeneralString);
					if (matcher2.find()) {
						String masterSpecificString = flowContext.specificString;
						if (udSpecificString.toLowerCase().equals(masterSpecificString)) {
							setMatchingResource(flowContext.tdbResource);
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public String getGeneralString() {
		Object result = getOneProperty(flowContextGeneral);
		if (result == null) {
			return null;
		}
		return (String) result;
	}

	public String getSpecificString() {
		Object[] result = getAllProperties(flowContextSpecific);
		if (result.length == 0) {
			return null;
		}
		if (result.length == 1) {
			return (String) result[0];
		}
		StringBuilder b = new StringBuilder();
		b.append((String) result[0]);
		for (int i = 1; i < result.length; i++) {
			b.append(" -> " + (String) result[i]);
		}
		return b.toString();
	}

	public static void loadMasterFlowContexts() {
		Logger runLogger = Logger.getLogger("run");
		if (lcaMasterContexts.size() > 0) {
			return;
		}
		boolean needToReadMasterFile = true;
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.getModel(null);
		ResIterator resIterator = tdbModel.listSubjectsWithProperty(RDF.type, rdfClass);
		while (resIterator.hasNext()) {
			Resource context = resIterator.next();
			if (tdbModel.contains(context, RDF.type, LCAHT.MasterDataset)) {
				ActiveTDB.tdbDataset.end();
				needToReadMasterFile = false;
				break;
			}
		}
		ActiveTDB.tdbDataset.end();

		if (needToReadMasterFile) {
			String resourceDirectory = Util.getPreferenceStore().getString("resourceDirectory");
			String path = resourceDirectory + File.separator + "mc2_lcaht.n3";
			File file = null;
			try {
				file = new File(path);
				System.out.println("We got the file!  It's at: " + file.getPath());
			} catch (Exception e1) {
				System.out.println("The Master Contexts file: " + path + " was not found.");
				runLogger.warn("The Master Contexts file: " + path + " was not found.");
				return;
			}
			ImportRDFFileDirectlyToGraph.loadToDefaultGraph(path, null);
		}

		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		b.append("select ?fc \n");
		b.append("       ?fc_uuid \n");
		b.append("       ?fc_gen \n");
		b.append("       ?fc_spec \n");
		b.append("       ?fc_nec_regex \n");
		b.append("       ?fc_suf_regex \n");
		b.append("       ?fc_forbid_regex \n");
		b.append("where {\n");
		b.append("  ?fc a fedlca:FlowContext .  \n");
		b.append("  ?fc a lcaht:MasterDataset .  \n");
		b.append("  ?fc fedlca:presentationSortIndex ?sort .  \n");
		b.append("  optional { ?fc fedlca:flowContextNecessaryRegexPattern ?fc_nec_regex } \n");
		b.append("  optional { ?fc fedlca:flowContextSufficientRegexPattern ?fc_suf_regex } \n");
		b.append("  optional { ?fc fedlca:flowContextForbiddenRegexPattern ?fc_forbid_regex } \n");
		b.append("} \n");
		b.append("order by ?sort \n");

		String query = b.toString();
		System.out.println("Query = \n" + query);
		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);
		harmonyQuery2Impl.setGraphName(null);

		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		List<Resource> itemsToAddToDatasource = new ArrayList<Resource>();
		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			Resource fcResource = querySolution.get("fc").asResource();
			FlowContext newFlowContext = new FlowContext(fcResource, false);
			String uuid = querySolution.get("fc_uuid").asLiteral().getString();
			String general = querySolution.get("fc_gen").asLiteral().getString();
			String specific = querySolution.get("fc_spec").asLiteral().getString();
			RDFNode necNode = querySolution.get("fc_nec_regex");
			if (necNode != null) {
				String necessaryRegex = necNode.asLiteral().getString();
				newFlowContext.addRequiredMatchPatterns(Pattern.compile(necessaryRegex));
			}
			RDFNode sufNode = querySolution.get("fc_nec_regex");
			if (sufNode != null) {
				String sufficientRegex = sufNode.asLiteral().getString();
				newFlowContext.addRequiredMatchPatterns(Pattern.compile(sufficientRegex));
			}
			RDFNode forbiddenNode = querySolution.get("fc_nec_regex");
			if (forbiddenNode != null) {
				String forbiddenRegex = forbiddenNode.asLiteral().getString();
				newFlowContext.addRequiredMatchPatterns(Pattern.compile(forbiddenRegex));
			}
			lcaMasterContexts.add(newFlowContext);
		}
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getOpenLCAUUIDValue() {
		return openLCAUUIDValue;
	}

	public void setOpenLCAUUIDValue(String openLCAUUIDValue) {
		this.openLCAUUIDValue = openLCAUUIDValue;
	}

	public List<Pattern> getRequiredMatchPatterns() {
		return requiredMatchPatterns;
	}

	public void setRequiredMatchPatterns(List<Pattern> requiredMatchPatterns) {
		this.requiredMatchPatterns = requiredMatchPatterns;
	}

	public void addRequiredMatchPatterns(Pattern requiredMatchPattern) {
		requiredMatchPatterns.add(requiredMatchPattern);
	}

	public List<Pattern> getForbiddenMatchPatterns() {
		return forbiddenMatchPatterns;
	}

	public void setForbiddenMatchPatterns(List<Pattern> forbiddenMatchPatterns) {
		this.forbiddenMatchPatterns = forbiddenMatchPatterns;
	}

	public void addForbiddenMatchPatterns(Pattern forbiddenMatchPattern) {
		forbiddenMatchPatterns.add(forbiddenMatchPattern);
	}

	public List<Pattern> getSufficientMatchPatterns() {
		return sufficientMatchPatterns;
	}

	public void setSufficientMatchPatterns(List<Pattern> sufficientMatchPatterns) {
		this.sufficientMatchPatterns = sufficientMatchPatterns;
	}

	public void addSufficientMatchPatterns(Pattern sufficientMatchPattern) {
		sufficientMatchPatterns.add(sufficientMatchPattern);
	}
	
	public static List<FlowContext> getLcaMasterContexts() {
		return lcaMasterContexts;
	}

	public static void setLcaMasterContexts(List<FlowContext> lcaMasterContexts) {
		FlowContext.lcaMasterContexts = lcaMasterContexts;
	}
}
