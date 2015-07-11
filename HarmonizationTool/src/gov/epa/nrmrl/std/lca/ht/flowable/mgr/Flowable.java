package gov.epa.nrmrl.std.lca.ht.flowable.mgr;

import gov.epa.nrmrl.std.lca.ht.dataCuration.AnnotationProvider;
import gov.epa.nrmrl.std.lca.ht.dataCuration.ComparisonKeeper;
import gov.epa.nrmrl.std.lca.ht.dataCuration.ComparisonProvider;
import gov.epa.nrmrl.std.lca.ht.dataFormatCheck.FormatCheck;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataPropertyProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataValue;
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.RDFUtil;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.SKOS;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Flowable {
	// CLASS VARIABLES
	private static final Resource rdfClass = ECO.Flowable;
	public static final String label = "Flowable";
	public static final String flowableNameString = "Name";
	public static final String flowableSynonymString = "Synonym";
	public static final String casString = "CAS";
	// public static final String stdCASString = "Standard CAS";
	public static final String chemicalFormulaString = "Chemical formula";
	public static final String smilesString = "SMILES";
	// NOTE: EVENTUALLY label AND comment SHOULD COME FROM ONTOLOGY
	public static final String comment = "A flowable is the entity that flows in an elementary flow.  It can be a substance or energy.  Examples of flowables include CO2 and waste heat. No identity conditions are specified for flowables.";
	private static Map<String, LCADataPropertyProvider> dataPropertyMap;

	static {
		ActiveTDB.tsReplaceLiteral(rdfClass, RDFS.label, label);// <-- THIS
																// FAILS TO DO
																// THE
																// ASSIGNMENT
		ActiveTDB.tsAddGeneralTriple(rdfClass, RDFS.label, label, null);
		ActiveTDB.tsAddGeneralTriple(rdfClass, RDFS.comment, comment, null);
		ActiveTDB.tsAddGeneralTriple(rdfClass, RDF.type, OWL.Class, null);

		// ActiveTDB.replaceLiteral(rdfClass, RDFS.label, label);// <-- THIS
		// FAILS TO DO THE ASSIGNMENT
		// ActiveTDB.addLiteral(rdfClass, RDFS.comment, comment);

		// JUNO: UNCOMMENTING THE LINES BELOW SUGGEST SOMETHING VERY ODD ABOUT
		// THE TDB:
		//
		// rdfClass.addProperty(RDFS.label, label); // <-- THIS SUCCEEDS IN THE
		// ASSIGNMENT
		// Literal literal = ActiveTDB.tdbModel.createLiteral(label);

		// ActiveTDB.tdbModel.add(rdfClass, RDFS.label, literal); // WHAT ABOUT
		// THIS?
		Model tdbModel = ActiveTDB.getModel(null);
		StmtIterator stmtIterator = tdbModel.listStatements();
		// System.out.println("rdfClass = " + rdfClass);

		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.nextStatement();
			if (!statement.getSubject().isAnon()) {
				if (statement.getSubject().getLocalName().equals(Flowable.getRdfclass().getLocalName())) {
					// Resource thing = statement.getSubject();
					// Resource thing2 = statement.getResource();
					// System.out.println("Flowable.getRdfclass() = "
					// +Flowable.getRdfclass());
					// System.out.println("statement.getSubject().getLocalName() = "
					// + statement.getSubject().getLocalName());
					//
					// System.out.println("Statement: " + statement.getSubject()
					// + " -- " + statement.getPredicate()
					// + " -- " + statement.getObject());
				}
			}
		}

		if (rdfClass.hasProperty(RDFS.label)) { // <-- THIS IS SUPPOSED TO CHECK
												// THE ASSIGNMENT
			// System.out.println(rdfClass.getProperty(RDFS.label).getString());
		} else {
			// System.out.println("wtf");
		}
		//

		// System.out.println("label assigned to Flowable");

		dataPropertyMap = new LinkedHashMap<String, LCADataPropertyProvider>();
		LCADataPropertyProvider lcaDataPropertyProvider;

		lcaDataPropertyProvider = new LCADataPropertyProvider(flowableNameString);
		lcaDataPropertyProvider.setPropertyClass(label);
		lcaDataPropertyProvider.setRDFClass(rdfClass);
		lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
		lcaDataPropertyProvider.setRequired(true);
		lcaDataPropertyProvider.setUnique(true);
		lcaDataPropertyProvider.setLeftJustified(true);
		lcaDataPropertyProvider.setCheckLists(getFlowablesNameCheckList());
		lcaDataPropertyProvider.setTDBProperty(RDFS.label);

		dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);

		lcaDataPropertyProvider = new LCADataPropertyProvider(flowableSynonymString);
		lcaDataPropertyProvider.setPropertyClass(label);
		lcaDataPropertyProvider.setRDFClass(rdfClass);
		lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
		lcaDataPropertyProvider.setRequired(false);
		lcaDataPropertyProvider.setUnique(false);
		lcaDataPropertyProvider.setLeftJustified(true);
		lcaDataPropertyProvider.setCheckLists(getFlowablesNameCheckList());
		lcaDataPropertyProvider.setTDBProperty(SKOS.altLabel);
		dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);

		lcaDataPropertyProvider = new LCADataPropertyProvider(casString);
		lcaDataPropertyProvider.setPropertyClass(label);
		lcaDataPropertyProvider.setRDFClass(rdfClass);
		lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
		lcaDataPropertyProvider.setRequired(false);
		lcaDataPropertyProvider.setUnique(true);
		lcaDataPropertyProvider.setLeftJustified(false);
		lcaDataPropertyProvider.setCheckLists(getCASCheckList());
		lcaDataPropertyProvider.setTDBProperty(ECO.casNumber);
		dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);

		lcaDataPropertyProvider = new LCADataPropertyProvider(chemicalFormulaString);
		lcaDataPropertyProvider.setPropertyClass(label);
		lcaDataPropertyProvider.setRDFClass(rdfClass);
		lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
		lcaDataPropertyProvider.setRequired(false);
		lcaDataPropertyProvider.setUnique(false);
		lcaDataPropertyProvider.setLeftJustified(true);
		lcaDataPropertyProvider.setCheckLists(getFormulaCheckList());
		lcaDataPropertyProvider.setTDBProperty(ECO.chemicalFormula);
		dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);

		lcaDataPropertyProvider = new LCADataPropertyProvider(smilesString);
		lcaDataPropertyProvider.setPropertyClass(label);
		lcaDataPropertyProvider.setRDFClass(rdfClass);
		lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
		lcaDataPropertyProvider.setRequired(false);
		lcaDataPropertyProvider.setUnique(false);
		lcaDataPropertyProvider.setLeftJustified(true);
		lcaDataPropertyProvider.setCheckLists(getSmilesCheckList());
		lcaDataPropertyProvider.setTDBProperty(FedLCA.hasSmilesString);
		dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);
	}

	// INSTANCE VARIABLES
	private Resource tdbResource;
	private List<LCADataValue> lcaDataValues;
	// private LinkedHashMap<Resource, String> matchCandidates;
	// private LinkedHashMap<Resource, String> searchResults;
	private List<ComparisonProvider> comparisons = new ArrayList<ComparisonProvider>();
	private List<ComparisonProvider> searchComparisons = new ArrayList<ComparisonProvider>();
	public boolean wasDoubleChedked = false;

	private int firstRow;

	// CONSTRUCTORS
	public Flowable() {
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
		lcaDataValues = new ArrayList<LCADataValue>();
		// matchCandidates = new LinkedHashMap<Resource, String>();
		// searchResults = new LinkedHashMap<Resource, String>();
	}

	public Flowable(Resource tdbResource) {
		this.tdbResource = tdbResource;
		lcaDataValues = new ArrayList<LCADataValue>();
		// matchCandidates = new LinkedHashMap<Resource, String>();
		// searchResults = new LinkedHashMap<Resource, String>();
		clearSyncDataFromTDB();
	}

	// public Flowable(boolean transactionSafe) {
	// if (transactionSafe) {
	// this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
	// lcaDataValues = new ArrayList<LCADataValue>();
	// matchCandidates = new LinkedHashMap<Resource, String>();
	// searchResults = new LinkedHashMap<Resource, String>();
	// } else {
	// this.tdbResource = ActiveTDB.createResource(rdfClass);
	// lcaDataValues = new ArrayList<LCADataValue>();
	// matchCandidates = new LinkedHashMap<Resource, String>();
	// searchResults = new LinkedHashMap<Resource, String>();
	// }
	// }

	// METHODS
	public Object getOneProperty(String key) {
		for (LCADataValue lcaDataValue : lcaDataValues) {
			if (lcaDataValue.getLcaDataPropertyProvider().getPropertyName().equals(key)) {
				return lcaDataValue.getValue();
			}
		}
		return null;
	}

	public Object getOneProperty(Property predicate) {
		if (tdbResource.hasProperty(predicate)) {
			try {
				return tdbResource.getProperty(predicate).getObject().asLiteral().getValue();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

	public void setProperty(String key, String valueAsString) {
		if (valueAsString == null) {
			return;
		}
		if (!dataPropertyMap.containsKey(key)) {
			return;
		}
		LCADataPropertyProvider lcaDataPropertyProvider = dataPropertyMap.get(key);
		// RDFDatatype rdfDatatype = lcaDataPropertyProvider.getRdfDatatype();

		boolean found = false;
		if (lcaDataPropertyProvider.isUnique()) {
			for (LCADataValue lcaDataValue : lcaDataValues) {
				if (lcaDataValue.getLcaDataPropertyProvider().equals(lcaDataPropertyProvider)) {
					lcaDataValue.setValueAsString(valueAsString);
					found = true;
					Object object = lcaDataValue.getValue();
					ActiveTDB.tsReplaceLiteral(tdbResource, lcaDataPropertyProvider.getTDBProperty(), object);
					if (!valueAsString.equals(valueAsString.toLowerCase())) {
						// SPECIAL CASE: NAME GETS ADDED TO SYNONYMS IN LOWER
						// CASE FORM
						if (key.equals(flowableNameString)) {
							ActiveTDB.tsAddGeneralTriple(tdbResource, SKOS.altLabel, valueAsString.toLowerCase(), null);
						}
						// SPECIAL CASE: NAME GETS ADDED TO SYNONYMS IN LOWER
						// CASE FORM
						if (key.equals(flowableSynonymString)) {
							ActiveTDB.tsAddGeneralTriple(tdbResource, SKOS.altLabel, valueAsString.toLowerCase(), null);
						}
					}
					continue;
				}
			}
		}
		if (!found) {
			LCADataValue lcaDataValue = new LCADataValue();
			lcaDataValue.setLcaDataPropertyProvider(lcaDataPropertyProvider);
			lcaDataValue.setValueAsString(valueAsString);
			Object object = lcaDataValue.getValue();
			lcaDataValues.add(lcaDataValue);
			ActiveTDB.tsAddGeneralTriple(tdbResource, lcaDataPropertyProvider.getTDBProperty(), object, null);
			if (!valueAsString.equals(valueAsString.toLowerCase())) {
				// SPECIAL CASE: NAME GETS ADDED TO SYNONYMS IN LOWER CASE FORM
				if (key.equals(flowableNameString)) {
					ActiveTDB.tsAddGeneralTriple(tdbResource, SKOS.altLabel, valueAsString.toLowerCase(), null);
				}
				// SPECIAL CASE: NAME GETS ADDED TO SYNONYMS IN LOWER CASE FORM
				if (key.equals(flowableSynonymString)) {
					ActiveTDB.tsAddGeneralTriple(tdbResource, SKOS.altLabel, valueAsString.toLowerCase(), null);
				}
			}
		}
		// ActiveTDB.tsAddLiteral(tdbResource,
		// lcaDataPropertyProvider.getTDBProperty(), rdfDatatype,
		// valueAsString);
	}

	public String getName() {
		return (String) getOneProperty(flowableNameString);
	}

	public String[] getSynonyms() {
		// String[] results;
		Object[] resultObjects = getAllProperties(flowableSynonymString);
		if (resultObjects == null) {
			return new String[0];
		}
		String[] results = new String[resultObjects.length];
		for (int i = 0; i < resultObjects.length; i++) {
			results[i] = (String) resultObjects[i];
		}
		return results;
	}

	// public String getCas() {
	// return (String) getOneProperty(casString);
	// }

	public String getFormattedCas() {
		return (String) getOneProperty(FedLCA.hasFormattedCAS);
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
		// Resource dataSourceResource =
		// tdbResource.getProperty(ECO.hasDataSource).getResource();
	}

	public void clearSyncDataFromTDB() {
		lcaDataValues.clear();
		updateSyncDataFromTDB();
	}

	public boolean notMatched() {
		// int count = 0;
		// for (String hit : matchCandidates.values()) {
		// int hitNumber = MatchStatus.getNumberBySymbol(hit);
		// if (hitNumber > 0 && hitNumber < 6) {
		// count++;
		// }
		// }
		if (countAssignedComparisons() == 1) {
			return false;
		}
		// if (count == 1) {
		// return false;
		// }
		return true;
	}

	private static List<FormatCheck> getFormulaCheckList() {
		List<FormatCheck> qaChecks = FormatCheck.getGeneralQAChecks();
		// Pattern p1 = Pattern.compile("?([A-Z][a-z]?\\d*)+");
		// Issue i1 = new Issue("Double quote",
		// "Chemical names may have a prime (single quote), but two or three primes should be represented by multiple single quote characters.",
		// "Replace the double quote with two single quotes.  You may also use the auto-clean function.",
		// true);
		// qaChecks.add(new FormatCheck(p1, i1));
		return qaChecks;
	}

	private static List<FormatCheck> getCASCheckList() {
		List<FormatCheck> qaChecks = FormatCheck.getGeneralQAChecks();
		String d2 = "Non-standard CAS format";
		String e2 = "CAS fields must be either blank or formatted propertly.  Data will be ignored.";
		String s2 = "Standardize CAS";
		Pattern acceptableCASFormat = Pattern.compile("^$|^\\d{2,7}-\\d\\d-\\d$|^\\d{5,10}$");
		qaChecks.add(new FormatCheck(d2, e2, s2, acceptableCASFormat, null, true));
		return qaChecks;
	}

	private static List<FormatCheck> getSmilesCheckList() {
		List<FormatCheck> qaChecks = FormatCheck.getGeneralQAChecks();

		// String d1 = "Invalid SMILES";
		// String e1 = "Characters disallowed in SMILES include ...";
		// String s1 = "Check the SMILES source.";
		// Pattern p1 = Pattern.compile(" GET FROM TOMMY");
		// String r2 = null;
		// qaChecks.add(new FormatCheck(d1, e1, s1, p1, null, true));
		return qaChecks;
	}

	private static List<FormatCheck> getFlowablesNameCheckList() {
		List<FormatCheck> qaChecks = FormatCheck.getGeneralQAChecks();

		String d1 = "Non-allowed characters";
		String e1 = "Various characters are not considered acceptible in standard chemical names.";
		String s1 = "Check your data";
		Pattern p1 = Pattern.compile("^([^\"]+)[\"]([^\"]+)$");
		String r1 = null;

		qaChecks.add(new FormatCheck(d1, e1, s1, p1, r1, false));
		return qaChecks;
	}

	/**
	 * Removes all leading zeroes and dashes in the CASRN if possible.
	 * 
	 * @param candidate	candidate is a CASRN of type string.  
	 * @return a sequence of 5 to 10 digits NOT starting with zero
	 */
	public static String stripCASdigits(String candidate) {
		String strippedCas = "";
		strippedCas = candidate.replaceAll("\\D", "");
		strippedCas = strippedCas.replace("^0+", "");
		if (strippedCas.equals("")) {
			return null;
		}
		if (Integer.parseInt(strippedCas) < 10000) {
			return null;
		}
		return strippedCas;
	}

	/**
	 * 
	 * @param candidate	candidate is a CASRN of type string.
	 * @return a standard CAS with 2 to 7 digits (not starting with a zero), a dash, two digits, a dash, and one digit
	 */
	public static String standardizeCAS(String candidate) {
		String standardizedCas = "";
		String digitsOnly = stripCASdigits(candidate);
		if (digitsOnly == null || digitsOnly.equals("")) {
			return digitsOnly;
		}
		String noLeadingZeros = digitsOnly.replaceAll("^0+", "");
		if (Integer.parseInt(noLeadingZeros) < 50000) {
			return null;
		}
		standardizedCas = noLeadingZeros.substring(0, noLeadingZeros.length() - 3);
		standardizedCas += "-";
		standardizedCas += noLeadingZeros.substring(noLeadingZeros.length() - 3, noLeadingZeros.length() - 1);
		standardizedCas += "-";
		standardizedCas += noLeadingZeros.substring(noLeadingZeros.length() - 1, noLeadingZeros.length());
		return standardizedCas;
	}

	/**
	 * This function is a simple CAS checksum checker.  Enter a CASRN (with or without dashes) and either the test will pass or fail. 
	 * Recall that a CASRN must be 5-7 digits - 2 digits - 1 digit and that the last digit is the checksum value.
	 * 
	 * @param casNumber	Enter a CASRN with or without dashes.  The CASRN must be of type string.
	 * @return	True if checksum passes, False if checksum fails.
	 */
	public static boolean correctCASCheckSum(String casNumber) {

		// Stripping CAS down to digits only
		String strippedCas = stripCASdigits(casNumber);
		if (strippedCas == null) {
			return false;
		}

		// Checksum checker code
		int multiplier = strippedCas.length() - 1;
		int checksum = 0;
		for (int i = 0; i < strippedCas.length() - 1; i++) {
			checksum += multiplier * Integer.parseInt(strippedCas.substring(i, i + 1));
			multiplier--;
		}
		checksum = checksum % 10;

		if (checksum == Integer.parseInt(strippedCas.substring(strippedCas.length() - 1, strippedCas.length()))) {
			return true;
		}
		return false;
	}

	public static FormatCheck createBadCheckSumQACheck() {

		String d = "Invalid CAS";
		String e = "A bad checksum has been detected.  Data will be used but flagged in TDB for bad checksum.";
		String s = "Select ignore row or change CAS in data file.";
		Pattern p = null;
		String r = null;
		return new FormatCheck(d, e, s, p, r, false);

		// TODO CFowler: Change the color of the issues

		// Make sure that the workflow accepts notable issues

	}

	public Resource getTdbResource() {
		return tdbResource;
	}

	public void setTdbResource(Resource tdbResource) {
		this.tdbResource = tdbResource;
	}

	public List<LCADataValue> getLcaDataValues() {
		return lcaDataValues;
	}

	public void setLcaDataValues(List<LCADataValue> lcaDataValues) {
		this.lcaDataValues = lcaDataValues;
	}

	public static Resource getRdfclass() {
		return rdfClass;
	}

	public static Map<String, LCADataPropertyProvider> getDataPropertyMap() {
		return dataPropertyMap;
	}

	// public LinkedHashMap<Resource, String> getMatchCandidates() {
	// if (matchCandidates == null) {
	// return new LinkedHashMap<Resource, String>();
	// }
	// return matchCandidates;
	// }

	// public void setMatchCandidates(LinkedHashMap<Resource, String> matchCandidates) {
	// this.matchCandidates = matchCandidates;
	// }

	public void addMatchCandidate(Resource resource) {
		// matchCandidates.put(resource, "?");
		comparisons.add(new ComparisonProvider(tdbResource, resource, FedLCA.EquivalenceCandidate));
	}

	public void addSearchResult(Resource masterResource, String equivalenceSymbol) {
		// searchResults.put(resource, "?");
		// CurationMethods.createNewComparison(tdbResource, resource, FedLCA.EquivalenceCandidate);
		ComparisonProvider comparisonProvider = getComparison(masterResource);
		if (comparisonProvider == null) {
			Resource comparisonEquivalenceValue = MatchStatus.getBySymbol(equivalenceSymbol).getEquivalence();
			comparisonProvider = new ComparisonProvider(tdbResource, masterResource, comparisonEquivalenceValue);
			searchComparisons.add(comparisonProvider);
		}
	}

	public void removeMatchCandidate(Resource resource) {
		// matchCandidates.remove(resource);
		ComparisonProvider comparison = new ComparisonProvider(resource);
		comparison.remove();

	}

	public void setMatchCandidateStatus(int matchCandidateIndex, int statusCol) {
		ComparisonProvider comparisonProvider = comparisons.get(matchCandidateIndex);
		// Resource dFlowableResource = (Resource) matchCandidates.keySet().toArray()[matchCandidateIndex];
		MatchStatus matchStatus = MatchStatus.getByValue(statusCol);
		Resource equivalence = matchStatus.getEquivalence();
		comparisonProvider.setEquivalence(equivalence);
		// matchCandidates.put(dFlowableResource, matchStatus.getSymbol());
	}

	public int setMasterMatches(boolean doubleCheck) {
		String qName = getName();
		String lcQName = qName.toLowerCase();
		lcQName = lcQName.replaceAll("\"", "\\\\\"");
		List<String> namesToMatch = new ArrayList<String>();
		namesToMatch.add(lcQName);
		if (lcQName.matches(".*;.*")) {
			namesToMatch.add(lcQName.split(";")[0]);
		}
		for (String syn : getSynonyms()) {
			String lcSyn = syn.toLowerCase();
			lcSyn = lcSyn.replaceAll("\"", "\\\\\"");
			namesToMatch.add(lcSyn);
			if (lcSyn.matches(".*;.*")) {
				namesToMatch.add(lcSyn.split(";")[0]);
			}
		}

		// Literal qNameLiteral = ActiveTDB.tsCreateTypedLiteral(lcQName);
		// Model tdbModel = ActiveTDB.getModel();

		boolean checkCas = false;
		String qCAS = getFormattedCas();
		if (qCAS != null) {
			if (!qCAS.equals("")) {
				checkCas = true;
			}
		}

		// =========================================
		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		b.append(" \n");
		b.append("SELECT distinct ?f ?masterTest \n");
		b.append("WHERE \n");
		b.append("  { \n");
		b.append("    { \n");

		b.append("      { ?f skos:altLabel \"" + namesToMatch.get(0) + "\"^^xsd:string  . } \n");
		for (int i = 1; i < namesToMatch.size(); i++) {
			b.append("   UNION { ?f skos:altLabel \"" + namesToMatch.get(i) + "\"^^xsd:string . } \n");
		}
		b.append("    } \n");

		if (checkCas) {
			b.append("    optional {?f fedlca:hasFormattedCAS \"" + qCAS + "\"^^xsd:string . }\n");
			// b.append("    filter (str(?cas) = \"" + qCAS + "\")\n");
		}
		b.append("    ?f eco:hasDataSource ?ds . \n");
		b.append("    ?ds a lcaht:MasterDataset . \n");
		b.append("    ?f a eco:Flowable . \n");
		b.append("   } \n");
		String query = b.toString();
		// System.out.println("Query = \n" + query);

		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);

		ResultSet resultSet = harmonyQuery2Impl.getResultSet();

		int count = 0;
		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			RDFNode rdfNode = querySolution.get("f");
			count++;
			// matchCandidates.put(rdfNode.asResource(), "=");
			ComparisonProvider comparisonProvider = new ComparisonProvider(tdbResource, rdfNode.asResource(),
					FedLCA.Equivalent);
			comparisonProvider.setComment("Created in setMasterMatches");
			comparisonProvider.setLastUpdate(new Date());
			ComparisonKeeper.addUncommittedComparison(comparisonProvider);

			comparisons.add(comparisonProvider);
		}
		if (count > 0) {
			AnnotationProvider.updateCurrentAnnotationModifiedDate();
			return count;
		}

		if (!doubleCheck) {
			return count;
		}
		count += setCandidates();
		return count;
	}

	public int setCandidates() {
		wasDoubleChedked = true;
		String qName = getName();
		String lcQName = qName.toLowerCase();
		lcQName = lcQName.replaceAll("\"", "\\\\\"");
		List<String> namesToMatch = new ArrayList<String>();
		namesToMatch.add(lcQName);
		if (lcQName.matches(".*;.*")) {
			namesToMatch.add(lcQName.split(";")[0]);
		}

		if (lcQName.matches(".*, in water")) {
			String firstPart = lcQName.split(", in water")[0];
			System.out.println("First part = " + firstPart);
			namesToMatch.add(firstPart);
		} else if (lcQName.matches(".* in water")) {
			String firstPart = lcQName.split(" in water")[0];
			System.out.println("First part = " + firstPart);
			namesToMatch.add(firstPart);
		} else if (lcQName.matches(".*, in soil")) {
			String firstPart = lcQName.split(", in soil")[0];
			System.out.println("First part = " + firstPart);
			namesToMatch.add(firstPart);
		} else if (lcQName.matches(".* in soil")) {
			String firstPart = lcQName.split(" in soil")[0];
			System.out.println("First part = " + firstPart);
			namesToMatch.add(firstPart);
		} else if (lcQName.matches(".*, in air")) {
			String firstPart = lcQName.split(", in air")[0];
			System.out.println("First part = " + firstPart);
			namesToMatch.add(firstPart);
		} else if (lcQName.matches(".* in air")) {
			String firstPart = lcQName.split(" in air")[0];
			System.out.println("First part = " + firstPart);
			namesToMatch.add(firstPart);
		} else if (lcQName.matches(".*, in biomass")) {
			String firstPart = lcQName.split(", in biomass")[0];
			System.out.println("First part = " + firstPart);
			namesToMatch.add(firstPart);
		} else if (lcQName.matches(".* in biomass")) {
			String firstPart = lcQName.split(" in biomass")[0];
			System.out.println("First part = " + firstPart);
			namesToMatch.add(firstPart);
		} else if (lcQName.matches(".*, in ground")) {
			String firstPart = lcQName.split(", in ground")[0];
			System.out.println("First part = " + firstPart);
			namesToMatch.add(firstPart);
		} else if (lcQName.matches(".* in ground")) {
			String firstPart = lcQName.split(" in ground")[0];
			System.out.println("First part = " + firstPart);
			namesToMatch.add(firstPart);
		}

		for (String syn : getSynonyms()) {
			String lcSyn = syn.toLowerCase();
			lcSyn = lcSyn.replaceAll("\"", "\\\\\"");
			namesToMatch.add(lcSyn);
			/*
			 * Special rule to attempt to match first part of a semi-colon delimited list
			 */
			if (lcSyn.matches(".*;.*")) {
				namesToMatch.add(lcSyn.split(";")[0]);
			}
			if (lcSyn.matches(".*, in water")) {
				String firstPart = lcSyn.split(", in water")[0];
				System.out.println("First part = " + firstPart);
				namesToMatch.add(firstPart);
			} else if (lcSyn.matches(".* in water")) {
				String firstPart = lcSyn.split(" in water")[0];
				System.out.println("First part = " + firstPart);
				namesToMatch.add(firstPart);
			} else if (lcSyn.matches(".*, in soil")) {
				String firstPart = lcSyn.split(", in soil")[0];
				System.out.println("First part = " + firstPart);
				namesToMatch.add(firstPart);
			} else if (lcSyn.matches(".* in soil")) {
				String firstPart = lcSyn.split(" in soil")[0];
				System.out.println("First part = " + firstPart);
				namesToMatch.add(firstPart);
			} else if (lcSyn.matches(".*, in air")) {
				String firstPart = lcSyn.split(", in air")[0];
				System.out.println("First part = " + firstPart);
				namesToMatch.add(firstPart);
			} else if (lcSyn.matches(".* in air")) {
				String firstPart = lcSyn.split(" in air")[0];
				System.out.println("First part = " + firstPart);
				namesToMatch.add(firstPart);
			} else if (lcSyn.matches(".*, in biomass")) {
				String firstPart = lcSyn.split(", in biomass")[0];
				System.out.println("First part = " + firstPart);
				namesToMatch.add(firstPart);
			} else if (lcSyn.matches(".* in biomass")) {
				String firstPart = lcSyn.split(" in biomass")[0];
				System.out.println("First part = " + firstPart);
				namesToMatch.add(firstPart);
			} else if (lcSyn.matches(".*, in ground")) {
				String firstPart = lcSyn.split(", in ground")[0];
				System.out.println("First part = " + firstPart);
				namesToMatch.add(firstPart);
			} else if (lcSyn.matches(".* in ground")) {
				String firstPart = lcSyn.split(" in ground")[0];
				System.out.println("First part = " + firstPart);
				namesToMatch.add(firstPart);
			}

			System.out.println(lcSyn);

			/* Special rule to attempt to match text prior to ", in ground" */
			if (lcSyn.matches(".*, in ground")) {
				String firstPart = lcSyn.split(", in ground")[0];
				System.out.println("First part = " + firstPart);
				namesToMatch.add(firstPart);
			}
		}

		boolean checkCas = false;
		String qCAS = getFormattedCas();
		if (qCAS != null) {
			if (!qCAS.equals("")) {
				checkCas = true;
			}
		}
		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());

		b.append(" \n");
		b.append("SELECT distinct ?f \n");
		b.append("WHERE \n");
		b.append("  { \n");
		b.append("    { \n");
		if (checkCas) {
			b.append("      {?f fedlca:hasFormattedCAS \"" + qCAS + "\"^^xsd:string . } UNION \n");
		}
		b.append("      { ?f skos:altLabel \"" + namesToMatch.get(0) + "\"^^xsd:string  . } \n");
		for (int i = 1; i < namesToMatch.size(); i++) {
			b.append("   UNION { ?f skos:altLabel \"" + namesToMatch.get(i) + "\"^^xsd:string . } \n");
		}
		b.append("    } \n");
		b.append("    ?f eco:hasDataSource ?ds . \n");
		b.append("    ?f a eco:Flowable . \n");
		b.append("    ?ds a ?masterTest . \n");
		b.append("    filter regex (str(?masterTest), \".*Dataset\") \n");
		b.append("   } order by ?masterTest \n");

		String query = b.toString();
		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);

		ResultSet resultSet = harmonyQuery2Impl.getResultSet();

		int count = 0;
		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			RDFNode rdfNode = querySolution.get("f");
			Resource masterResource = rdfNode.asResource();
			count++;
			ComparisonProvider comparisonProvider = getComparison(masterResource);
			if (comparisonProvider == null) {
				comparisonProvider = new ComparisonProvider(tdbResource, masterResource, FedLCA.EquivalenceCandidate);
				comparisons.add(comparisonProvider);
			}
		}
		return count;
	}

	public String getDataSource() {
		if (tdbResource.hasProperty(ECO.hasDataSource)) {
			Resource qDataSource = tdbResource.getPropertyResourceValue(ECO.hasDataSource);
			if (qDataSource.hasProperty(RDFS.label)) {
				String value = qDataSource.getProperty(RDFS.label).getString();
				return value;
			} else {
				return "[no source]";
			}
		}
		return null;
	}

	public int getFirstRow() {
		return firstRow;
	}

	public void setFirstRow(int firstRow) {
		this.firstRow = firstRow;
	}

	// public void clearSearchResults() {
	// searchResults.clear();
	// }

	public void clearSearchComparisons() {
		searchComparisons.clear();
	}

	public void transferSearchResults() {
		for (ComparisonProvider comparisonProvider : searchComparisons) {
			Resource equivalence = comparisonProvider.getEquivalence();
			if (equivalence != null) {
				if (!equivalence.equals(FedLCA.EquivalenceCandidate)) {
					comparisons.add(comparisonProvider);
				}
			}
		}
	}

	// public LinkedHashMap<Resource, String> getSearchResults() {
	// if (searchResults == null) {
	// return new LinkedHashMap<Resource, String>();
	// }
	// return searchResults;
	// }

	public List<ComparisonProvider> getComparisons() {
		if (comparisons == null) {
			comparisons = new ArrayList<ComparisonProvider>();
		}
		return comparisons;
	}

	public void setComparisons(List<ComparisonProvider> comparisons) {
		this.comparisons = comparisons;
	}

	public void addComparison(ComparisonProvider comparisonProvider) {
		comparisonProvider.commitToTDB();
		comparisons.add(comparisonProvider);
	}

	public void addComparison(Resource masterResource, Resource equivalence) {
		ComparisonProvider comparisonProvider = getComparison(masterResource);
		if (comparisonProvider == null) {
			comparisonProvider = new ComparisonProvider(tdbResource, masterResource, equivalence);
			addComparison(comparisonProvider);
		} else {
			comparisonProvider.setEquivalence(equivalence);
		}
	}

	public void removeComparison(ComparisonProvider comparisonProvider) {
		comparisons.remove(comparisonProvider);
	}

	public void removeComparison(Resource masterResource) {
		removeComparison(getComparison(masterResource));
	}

	public int countTotalComparisons() {
		return comparisons.size();
	}

	public int countAssignedComparisons() {
		int count = 0;
		for (ComparisonProvider comparisonProvider : comparisons) {
			Resource equivalence = comparisonProvider.getEquivalence();
			if (equivalence == null) {
				continue;
			}
			if (equivalence.equals(FedLCA.Equivalent) || equivalence.equals(FedLCA.EquivalenceSubset)
					|| equivalence.equals(FedLCA.EquivalenceSuperset) || equivalence.equals(FedLCA.EquivalenceProxy)) {
				count++;
			}
		}
		return count;
	}

	public ComparisonProvider getComparison(Resource masterResource) {
		if (masterResource == null) {
			return null;
		}
		for (ComparisonProvider comparisonProvider : comparisons) {
			Resource comparisonMasterResource = comparisonProvider.getMasterDataObject();
			if (comparisonMasterResource != null) {
				if (comparisonMasterResource.equals(masterResource)) {
					return comparisonProvider;
				}
			}
		}
		return null;
	}

	public ComparisonProvider getSearchComparison(Resource masterResource) {
		if (masterResource == null) {
			return null;
		}
		for (ComparisonProvider comparisonProvider : searchComparisons) {
			Resource comparisonMasterResource = comparisonProvider.getMasterDataObject();
			if (comparisonMasterResource != null) {
				if (comparisonMasterResource.equals(masterResource)) {
					return comparisonProvider;
				}
			}
		}
		return null;
	}

	public List<ComparisonProvider> getSearchComparisons() {
		if (searchComparisons == null) {
			searchComparisons = new ArrayList<ComparisonProvider>();
		}
		return searchComparisons;
	}

	public void setSearchComparisons(List<ComparisonProvider> searchComparisons) {
		this.searchComparisons = searchComparisons;
	}

	public void removeSearchComparisons(ComparisonProvider comparisonProvidertoRemove) {
		searchComparisons.remove(comparisonProvidertoRemove);
	}

	public void addSearchComparison(ComparisonProvider comparisonProvider) {
		searchComparisons.add(comparisonProvider);
	}
}
