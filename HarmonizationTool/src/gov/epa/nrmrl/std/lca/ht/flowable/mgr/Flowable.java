package gov.epa.nrmrl.std.lca.ht.flowable.mgr;

import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataPropertyProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataValue;
import gov.epa.nrmrl.std.lca.ht.dataModels.MatchCandidate;
import gov.epa.nrmrl.std.lca.ht.dataModels.QACheck;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.RDFUtil;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.SKOS;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Flowable {
	// CLASS VARIABLES
	public static final String flowableNameString = "Name";
	public static final String flowableSynonymString = "Synonym";
	public static final String casString = "CAS";
	public static final String chemicalFormulaString = "Chemical formula";
	public static final String smilesString = "SMILES";
	private static final Resource rdfClass = ECO.Flowable;
	// NOTE: EVENTUALLY label AND comment SHOULD COME FROM ONTOLOGY
	public static final String label = "Flowable";
	public static final String comment = "A flowable is the entity that flows in an elementary flow.  It can be a substance or energy.  Examples of flowables include CO2 and waste heat. No identity conditions are specified for flowables.";
	private static Map<String, LCADataPropertyProvider> dataPropertyMap;

	static {
		ActiveTDB.tsReplaceLiteral(rdfClass, RDFS.label, label);// <-- THIS FAILS TO DO THE ASSIGNMENT
		ActiveTDB.tsAddLiteral(rdfClass, RDFS.comment, comment);

		// ActiveTDB.replaceLiteral(rdfClass, RDFS.label, label);// <-- THIS FAILS TO DO THE ASSIGNMENT
		// ActiveTDB.addLiteral(rdfClass, RDFS.comment, comment);

		// JUNO: UNCOMMENTING THE LINES BELOW SUGGEST SOMETHING VERY ODD ABOUT THE TDB:
		//
		// rdfClass.addProperty(RDFS.label, label); // <-- THIS SUCCEEDS IN THE ASSIGNMENT
		// Literal literal = ActiveTDB.tdbModel.createLiteral(label);

		// ActiveTDB.tdbModel.add(rdfClass, RDFS.label, literal); // WHAT ABOUT THIS?

		StmtIterator stmtIterator = ActiveTDB.tdbModel.listStatements();
		System.out.println("rdfClass = " + rdfClass);

		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
			if (!statement.getSubject().isAnon()) {
				if (statement.getSubject().getLocalName().equals(Flowable.getRdfclass().getLocalName())) {
					// Resource thing = statement.getSubject();
					// Resource thing2 = statement.getResource();
					// System.out.println("Flowable.getRdfclass() = " +Flowable.getRdfclass());
					System.out.println("statement.getSubject().getLocalName() = "
							+ statement.getSubject().getLocalName());

					System.out.println("Statement: " + statement.getSubject() + " -- " + statement.getPredicate()
							+ " -- " + statement.getObject());
				}
			}
		}

		if (rdfClass.hasProperty(RDFS.label)) { // <-- THIS IS SUPPOSED TO CHECK THE ASSIGNMENT
			System.out.println(rdfClass.getProperty(RDFS.label).getString());
		} else {
			System.out.println("wtf");
		}
		//

		System.out.println("label assigned to Flowable");

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
	private LinkedHashMap<Resource, String> matchCandidates;
	private int firstRow;

	// CONSTRUCTORS
	public Flowable() {
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
		lcaDataValues = new ArrayList<LCADataValue>();
		matchCandidates = new LinkedHashMap<Resource, String>();
	}

	public Flowable(Resource tdbResource) {
		this.tdbResource = tdbResource;
		lcaDataValues = new ArrayList<LCADataValue>();
		matchCandidates = new LinkedHashMap<Resource, String>();
		clearSyncDataFromTDB();
	}

	// METHODS
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

	public void setProperty(String key, String valueAsString) {
		if (valueAsString == null) {
			return;
		}
		if (!dataPropertyMap.containsKey(key)) {
			return;
		}
		LCADataPropertyProvider lcaDataPropertyProvider = dataPropertyMap.get(key);
		RDFDatatype rdfDatatype = lcaDataPropertyProvider.getRdfDatatype();

		boolean found = false;
		if (lcaDataPropertyProvider.isUnique()) {
			for (LCADataValue lcaDataValue : lcaDataValues) {
				if (lcaDataValue.getLcaDataPropertyProvider().equals(lcaDataPropertyProvider)) {
					lcaDataValue.setValueAsString(valueAsString);
					found = true;
					Object object = lcaDataValue.getValue();
					ActiveTDB.tsReplaceLiteral(tdbResource, lcaDataPropertyProvider.getTDBProperty(), rdfDatatype,
							object);
					if (!valueAsString.equals(valueAsString.toLowerCase())) {
						// SPECIAL CASE: NAME GETS ADDED TO SYNONYMS IN LOWER CASE FORM
						if (key.equals(flowableNameString)) {
							ActiveTDB.tsAddLiteral(tdbResource, SKOS.altLabel, XSDDatatype.XSDstring,
									valueAsString.toLowerCase());
						}
						// SPECIAL CASE: NAME GETS ADDED TO SYNONYMS IN LOWER CASE FORM
						if (key.equals(flowableSynonymString)) {
							ActiveTDB.tsAddLiteral(tdbResource, SKOS.altLabel, XSDDatatype.XSDstring,
									valueAsString.toLowerCase());
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
			ActiveTDB.tsAddLiteral(tdbResource, lcaDataPropertyProvider.getTDBProperty(), rdfDatatype, object);
			if (!valueAsString.equals(valueAsString.toLowerCase())) {
				// SPECIAL CASE: NAME GETS ADDED TO SYNONYMS IN LOWER CASE FORM
				if (key.equals(flowableNameString)) {
					ActiveTDB.tsAddLiteral(tdbResource, SKOS.altLabel, XSDDatatype.XSDstring,
							valueAsString.toLowerCase());
				}
				// SPECIAL CASE: NAME GETS ADDED TO SYNONYMS IN LOWER CASE FORM
				if (key.equals(flowableSynonymString)) {
					ActiveTDB.tsAddLiteral(tdbResource, SKOS.altLabel, XSDDatatype.XSDstring,
							valueAsString.toLowerCase());
				}
			}
		}
		// ActiveTDB.tsAddLiteral(tdbResource, lcaDataPropertyProvider.getTDBProperty(), rdfDatatype, valueAsString);
	}

	// public void setProperty(String key, Object object) {
	// if (object == null) {
	// return;
	// }
	// if (!dataPropertyMap.containsKey(key)) {
	// return;
	// }
	// LCADataPropertyProvider lcaDataPropertyProvider = dataPropertyMap.get(key);
	// RDFDatatype rdfDatatype = lcaDataPropertyProvider.getRdfDatatype();
	// Class<?> objectClass = RDFUtil.getJavaClassFromRDFDatatype(rdfDatatype);
	// if (!objectClass.equals(object.getClass())) {
	// return;
	// }
	// LCADataValue newLCADataValue = new LCADataValue();
	// newLCADataValue.setLcaDataPropertyProvider(lcaDataPropertyProvider);
	// newLCADataValue.setValue(object);
	// // newLCADataValue.setValueAsString(object.toString()); // SHOULD WE DO THIS AT ALL?
	//
	// if (lcaDataPropertyProvider.isUnique()) {
	// removeValues(lcaDataPropertyProvider.getPropertyName());
	// ActiveTDB.tsReplaceLiteral(tdbResource, lcaDataPropertyProvider.getTDBProperty(), rdfDatatype, object);
	// } else {
	// ActiveTDB.tsAddLiteral(tdbResource, lcaDataPropertyProvider.getTDBProperty(), rdfDatatype, object);
	// }
	// lcaDataValues.add(newLCADataValue);
	// }

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

	public String getCas() {
		return (String) getOneProperty(casString);
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
					Object value = stmtIterator.next().getLiteral().getValue();
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
		// Resource dataSourceResource = tdbResource.getProperty(ECO.hasDataSource).getResource();
	}

	public void clearSyncDataFromTDB() {
		lcaDataValues.clear();
		updateSyncDataFromTDB();
	}

	// public void remove() {
	// // --- BEGIN SAFE -WRITE- TRANSACTION ---
	// ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
	// try {
	// tdbResource.removeAll(FedLCA.personName);
	// tdbResource.removeAll(FedLCA.affiliation);
	// tdbResource.removeAll(FedLCA.email);
	// tdbResource.removeAll(FedLCA.voicePhone);
	//
	// ActiveTDB.tdbDataset.commit();
	// } finally {
	// ActiveTDB.tdbDataset.end();
	// }
	// // ---- END SAFE -WRITE- TRANSACTION ---
	// }

	public static Set<Resource> findMatchingFlowableResources(Flowable flowable) {
		Set<Resource> results = new HashSet<Resource>();
		Resource qResource = flowable.getTdbResource();
		String qName = flowable.getName();
		Literal qNameLiteral = ActiveTDB.tsCreateTypedLiteral(qName);
		ResIterator resIterator = ActiveTDB.tdbModel.listSubjectsWithProperty(RDFS.label, qNameLiteral);
		while (resIterator.hasNext()) {
			Resource flowableMatchCandidate = resIterator.next();

			if (flowableMatchCandidate.hasProperty(RDF.type, rdfClass)) {
				results.add(flowableMatchCandidate);
			}
		}

		resIterator = ActiveTDB.tdbModel.listSubjectsWithProperty(SKOS.altLabel, qNameLiteral);
		while (resIterator.hasNext()) {
			Resource flowableMatchCandidate = resIterator.next();

			if (flowableMatchCandidate.hasProperty(RDF.type, rdfClass)) {
				results.add(flowableMatchCandidate);
			}
		}

		for (String qSyn : flowable.getSynonyms()) {
			Literal qSynLiteral = ActiveTDB.tsCreateTypedLiteral(qSyn);
			resIterator = ActiveTDB.tdbModel.listSubjectsWithProperty(RDFS.label, qSynLiteral);
			while (resIterator.hasNext()) {
				Resource flowableMatchCandidate = resIterator.next();
				if (flowableMatchCandidate.hasProperty(RDF.type, rdfClass)) {
					results.add(flowableMatchCandidate);
				}
			}

			resIterator = ActiveTDB.tdbModel.listSubjectsWithProperty(SKOS.altLabel, qSynLiteral);
			while (resIterator.hasNext()) {
				Resource flowableMatchCandidate = resIterator.next();
				if (flowableMatchCandidate.hasProperty(RDF.type, rdfClass)) {
					results.add(flowableMatchCandidate);
				}
			}
		}

		// CAS MATCHING
		if (qResource.hasProperty(FedLCA.hasFormattedCAS)) {
			String cas = flowable.getCas();
			Literal qCASLiteral = ActiveTDB.tsCreateTypedLiteral(cas);

			resIterator = ActiveTDB.tdbModel.listSubjectsWithProperty(FedLCA.hasFormattedCAS, qCASLiteral);
			while (resIterator.hasNext()) {
				Resource flowableMatchCandidate = resIterator.next();
				if (flowableMatchCandidate.hasProperty(RDF.type, rdfClass)) {
					results.add(flowableMatchCandidate);
				}
			}
		}
		return results;
	}

	public static Set<MatchCandidate> findMatches(Flowable flowable) {
		Set<MatchCandidate> results = new HashSet<MatchCandidate>();
		Resource qDataSource = flowable.getTdbResource().getPropertyResourceValue(ECO.hasDataSource);

		// FIND MATCHES FOR THIS FLOWABLE
		// FIND MATCHES INVOLVING NAMES AND SYNONYMS:
		// Q-NAME = DB-NAME
		// return results;
		Statement statement = flowable.getTdbResource().getProperty(RDFS.label);
		if (statement == null) {
			// System.out.println("Nothing here");
			return results;
		}
		RDFNode objectName = flowable.getTdbResource().getProperty(RDFS.label).getObject();
		ResIterator resIterator = ActiveTDB.tdbModel.listSubjectsWithProperty(RDFS.label, objectName);
		while (resIterator.hasNext()) {
			Resource flowableMatchCandidate = resIterator.next();
			if (ActiveTDB.tdbModel.contains(flowableMatchCandidate, ECO.hasDataSource, qDataSource)) {
				continue; // DON'T MATCH YOURSELF
			}
			if (!flowableMatchCandidate.hasProperty(RDF.type, rdfClass)) {
				continue; // NOT A FLOWABLE
			}
			// THIS IS A name-name MATCH
			MatchCandidate matchCandidate = new MatchCandidate(flowable.getTdbResource(), flowableMatchCandidate);
			results.add(matchCandidate);
		}

		// Q-NAME = DB-SYN
		resIterator = ActiveTDB.tdbModel.listSubjectsWithProperty(SKOS.altLabel, objectName);
		while (resIterator.hasNext()) {
			Resource flowableMatchCandidate = resIterator.next();
			if (ActiveTDB.tdbModel.contains(flowableMatchCandidate, ECO.hasDataSource, qDataSource)) {
				continue; // DON'T MATCH YOURSELF
			}
			if (!flowableMatchCandidate.hasProperty(RDF.type, rdfClass)) {
				continue; // NOT A FLOWABLE
			}
			// THIS IS A name-synonym MATCH
			MatchCandidate matchCandidate = new MatchCandidate(flowable.getTdbResource(), flowableMatchCandidate);
			results.add(matchCandidate);
		}

		//
		StmtIterator stmtIterator = flowable.getTdbResource().listProperties(SKOS.altLabel);
		while (stmtIterator.hasNext()) {
			RDFNode objectAltName = stmtIterator.next().getObject();
			resIterator = ActiveTDB.tdbModel.listSubjectsWithProperty(RDFS.label, objectAltName);
			// Q-SYN = DB-NAME
			while (resIterator.hasNext()) {
				Resource flowableMatchCandidate = resIterator.next();
				if (ActiveTDB.tdbModel.contains(flowableMatchCandidate, ECO.hasDataSource, qDataSource)) {
					continue; // DON'T MATCH YOURSELF
				}
				if (!flowableMatchCandidate.hasProperty(RDF.type, rdfClass)) {
					continue; // NOT A FLOWABLE
				}
				// THIS IS A synonym-name MATCH
				MatchCandidate matchCandidate = new MatchCandidate(flowable.getTdbResource(), flowableMatchCandidate);
				results.add(matchCandidate);
			}

			// Q-SYN = DB-SYN
			resIterator = ActiveTDB.tdbModel.listSubjectsWithProperty(SKOS.altLabel, objectName);
			while (resIterator.hasNext()) {
				Resource flowableMatchCandidate = resIterator.next();
				if (ActiveTDB.tdbModel.contains(flowableMatchCandidate, ECO.hasDataSource, qDataSource)) {
					continue; // DON'T MATCH YOURSELF
				}
				if (!flowableMatchCandidate.hasProperty(RDF.type, rdfClass)) {
					continue; // NOT A FLOWABLE
				}
				// THIS IS A synonym-synonym MATCH
				MatchCandidate matchCandidate = new MatchCandidate(flowable.getTdbResource(), flowableMatchCandidate);
				results.add(matchCandidate);
			}
		}

		// CAS MATCHING
		if (flowable.getTdbResource().hasProperty(FedLCA.hasFormattedCAS)) {
			objectName = flowable.getTdbResource().getProperty(FedLCA.hasFormattedCAS).getObject();
			resIterator = ActiveTDB.tdbModel.listSubjectsWithProperty(FedLCA.hasFormattedCAS, objectName);
			while (resIterator.hasNext()) {
				Resource flowableMatchCandidate = resIterator.next();
				if (ActiveTDB.tdbModel.contains(flowableMatchCandidate, ECO.hasDataSource, qDataSource)) {
					continue; // DON'T MATCH YOURSELF
				}
				if (!flowableMatchCandidate.hasProperty(RDF.type, rdfClass)) {
					continue; // NOT A FLOWABLE
				}
				// THIS IS AN fCAS-fCAS MATCH
				MatchCandidate matchCandidate = new MatchCandidate(flowable.getTdbResource(), flowableMatchCandidate);
				results.add(matchCandidate);
			}
		}

		// OTHER MATCHES ?!?

		return results;
	}

	public static String compareFlowables(Flowable queryFlowable, Flowable referenceFlowable) {
		// INFO TO SHARE FOR JUST NAME AND CAS:
		// ++++.+ (BOTH MATCH, BEST)
		// ----.+ (NAME DOESN'T MATCH, ASSUME ITS A SYNONYM), CAS MATCHES
		// ++++.0 (NAME MATCHES, CAS NOT PRESENT FOR ONE OR BOTH)
		// ++++.- (NAME MATCHES, CAS DOES NOT - RARE AND NEEDS INSPECTION)

		// NAME MATCH SCORES:
		// "+   "; IF NAMES MATCH OR "-   " IF THEY DON'T
		// " +  "; IF qSyn = rName OR " -  " IF THEY DON'T OR " 0  " IF NOT PRESENT FOR ONE
		// "  + "; IF qName = rSyn OR "  - " IF THEY DON'T OR "  0 " IF NOT PRESENT FOR ONE
		// "   +"; IF qSyn = rSyn OR "   -" IF THEY DON'T OR "   0" IF NOT PRESENT FOR ONE

		String nameFlag = "-";
		String qName = queryFlowable.getName();
		String rName = referenceFlowable.getName();
		if (qName == null || rName == null) { // NOT SUPPOSED TO HAPPEN WITH REQUIRED "name"
			nameFlag = "0";
		} else if (qName.equals("") || rName.equals("")) { // NOT SUPPOSED TO HAPPEN WITH REQUIRED "name"
			nameFlag = "0";
		} else if (qName.equals(rName)) {
			nameFlag = "+";
		}

		String synName = "0";
		String nameSyn = "0";
		String synSyn = "0";
		for (String qSynonym : queryFlowable.getSynonyms()) {
			if (synName.equals("0")) {
				synName = "-";
			}
			if (qSynonym.equals(rName)) {
				synName = "+";
			}
			for (String rSynonym : referenceFlowable.getSynonyms()) {
				if (nameSyn.equals("0")) {
					nameSyn = "-";
				}
				if (synSyn.equals("0")) {
					synSyn = "-";
				}
				if (qName.equals(rSynonym)) {
					nameSyn = "+";
				}
				if (qSynonym.equals(rSynonym)) {
					synSyn = "+";
				}
			}
		}

		for (String rSynonym : referenceFlowable.getSynonyms()) {
			if (nameSyn.equals("0")) {
				nameSyn = "-";
			}
			if (qName.equals(rSynonym)) {
				nameSyn = "+";
			}
		}

		String casFlag = "-";
		String qCas = queryFlowable.getCas();
		String rCas = referenceFlowable.getCas();
		if (qCas == null || rCas == null) {
			casFlag = "0";
		} else if (qCas.equals("") || rCas.equals("")) {
			casFlag = "0";
		} else if (qCas.equals(rCas)) {
			casFlag = "+";
		}
		return nameFlag + synName + nameSyn + synSyn + "." + casFlag;
	}

	private static List<QACheck> getFormulaCheckList() {
		List<QACheck> qaChecks = QACheck.getGeneralQAChecks();
		// Pattern p1 = Pattern.compile("?([A-Z][a-z]?\\d*)+");
		// Issue i1 = new Issue("Double quote",
		// "Chemical names may have a prime (single quote), but two or three primes should be represented by multiple single quote characters.",
		// "Replace the double quote with two single quotes.  You may also use the auto-clean function.",
		// true);
		// qaChecks.add(new QACheck(p1, i1));
		return qaChecks;
	}

	private static List<QACheck> getCASCheckList() {
		List<QACheck> qaChecks = QACheck.getGeneralQAChecks();

		// String d1 = "Non-standard CAS format";
		// String e1 =
		// "CAS numbers may only have either a) all digits, or b) digits with \"-\" signs 4th and 2nd from the end.";
		// String s1 = "Parse digits into a formatted CAS";
		// Pattern p1 = Pattern.compile("^\\s*$");
		// String r1 = "";
		// qaChecks.add(new QACheck(d1, e1, s1, p1, r1, false));

		String d2 = "Non-standard CAS format";
		String e2 = "CAS numbers must be either blank or formatted propertly";

		// String e2 =
		// "CAS numbers must be a) blank, b) 5+ digits, or c) digits with \"-\" signs 4th and 2nd from the end.";
		String s2 = "Standardize CAS";
		// Pattern acceptableCASFormat = Pattern.compile("^$|^0*(\\d{2,})-?(\\d\\d)-?(\\d)$");
		Pattern acceptableCASFormat = Pattern.compile("^$|^[1-9]\\d{1,}-\\d\\d-\\d$");

		// String r2 = "$1-$2-$3";
		qaChecks.add(new QACheck(d2, e2, s2, acceptableCASFormat, null, true));
		return qaChecks;
	}

	private static List<QACheck> getSmilesCheckList() {
		List<QACheck> qaChecks = QACheck.getGeneralQAChecks();

		// String d1 = "Invalid SMILES";
		// String e1 = "Characters disallowed in SMILES include ...";
		// String s1 = "Check the SMILES source.";
		// Pattern p1 = Pattern.compile(" GET FROM TOMMY");
		// String r2 = null;
		// qaChecks.add(new QACheck(d1, e1, s1, p1, null, true));
		return qaChecks;
	}

	private static List<QACheck> getFlowablesNameCheckList() {
		List<QACheck> qaChecks = QACheck.getGeneralQAChecks();

		String d1 = "Non-allowed characters";
		String e1 = "Various characters are not considered acceptible in standard chemical names.";
		String s1 = "Check your data";
		Pattern p1 = Pattern.compile("^([^\"]+)[\"]([^\"]+)$");
		String r1 = null;

		qaChecks.add(new QACheck(d1, e1, s1, p1, r1, false));
		return qaChecks;
	}

	// private class FlowableHeaderObj {
	// private String headerString;
	// private boolean isRequired;
	// private boolean isUnique;
	// public FlowableHeaderObj(String headerString, boolean isRequired, boolean
	// isUnique){
	// this.headerString=headerString;
	// this.isRequired = isRequired;
	// this.isUnique = isUnique;
	// }
	// }

	// public void setQaChecks(List<QACheck> qaChecks) {
	// this.qaChecks = qaChecks;
	// }

	// public static boolean validStandardFormat(String candidate) {
	// Matcher matcher = acceptableCASFormat.matcher(candidate);
	// if (matcher.find()) {
	// return true;
	// }
	// return false;
	// }

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

	public static boolean correctCASCheckSum(String casNumber) {
		String strippedCas = stripCASdigits(casNumber);
		if (strippedCas == null) {
			return false;
		}
		int multiplier = 0;
		int checksum = -Integer.parseInt(strippedCas.substring(strippedCas.length() - 1, 1));
		for (int i = strippedCas.length() - 2; i >= 0; i--) {
			multiplier++;
			checksum += multiplier * Integer.parseInt(strippedCas.substring(i, 1));
		}
		if (checksum % 10 == 0) {
			return true;
		}
		return false;
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

	public LinkedHashMap<Resource, String> getMatchCandidates() {
		return matchCandidates;
	}

	public void setMatchCandidates(LinkedHashMap<Resource, String> matchCandidates) {
		this.matchCandidates = matchCandidates;
	}

	public void addMatchCandidate(Resource resource) {
		matchCandidates.put(resource, "?");
	}

	public void removeMatchCandidate(Resource resource) {
		matchCandidates.remove(resource);
	}

	public void setMatchCandidateStatus(int matchCandidateIndex, int statusCol) {
		Resource dFlowableResource = (Resource) matchCandidates.keySet().toArray()[matchCandidateIndex];
		MatchStatus matchStatus = MatchStatus.getByValue(statusCol);
		matchCandidates.put(dFlowableResource, matchStatus.getSymbol());
	}

	public boolean setMatches() {
		String qName = getName();
		String lcQName = qName.toLowerCase();
		Literal qNameLiteral = ActiveTDB.tsCreateTypedLiteral(lcQName);
		ResIterator resIterator = ActiveTDB.tdbModel.listSubjectsWithProperty(RDFS.label, qNameLiteral);
		while (resIterator.hasNext()) {
			Resource flowableMatchCandidate = resIterator.next();
			if (flowableMatchCandidate.hasProperty(RDF.type, rdfClass)) {
				matchCandidates.put(flowableMatchCandidate, "?");
				// THIS IS A name-name MATCH
			}
		}

		resIterator = ActiveTDB.tdbModel.listSubjectsWithProperty(SKOS.altLabel, qNameLiteral);
		while (resIterator.hasNext()) {
			Resource flowableMatchCandidate = resIterator.next();

			if (flowableMatchCandidate.hasProperty(RDF.type, rdfClass)) {
				matchCandidates.put(flowableMatchCandidate, "?");
				// THIS IS A name-synonym MATCH
			}
		}

		for (String altName : getSynonyms()) {
			String lcAltName = altName.toLowerCase();
			Literal qAltNameLiteral = ActiveTDB.tsCreateTypedLiteral(lcAltName);

			resIterator = ActiveTDB.tdbModel.listSubjectsWithProperty(RDFS.label, qAltNameLiteral);
			// Q-SYN = DB-NAME
			while (resIterator.hasNext()) {
				Resource flowableMatchCandidate = resIterator.next();

				if (flowableMatchCandidate.hasProperty(RDF.type, rdfClass)) {
					matchCandidates.put(flowableMatchCandidate, "?");
					// THIS IS A synonym-name MATCH
				}
			}

			resIterator = ActiveTDB.tdbModel.listSubjectsWithProperty(SKOS.altLabel, qAltNameLiteral);
			while (resIterator.hasNext()) {
				Resource flowableMatchCandidate = resIterator.next();
				if (flowableMatchCandidate.hasProperty(RDF.type, rdfClass)) {
					matchCandidates.put(flowableMatchCandidate, "?");
					// THIS IS A synonym-synonym MATCH

				}
			}
		}

		// CAS MATCHING
		String qCAS = getCas();
		Literal qCASLiteral = ActiveTDB.tsCreateTypedLiteral(qCAS);
		resIterator = ActiveTDB.tdbModel.listSubjectsWithProperty(FedLCA.hasFormattedCAS, qCASLiteral);
		while (resIterator.hasNext()) {
			Resource flowableMatchCandidate = resIterator.next();
			if (flowableMatchCandidate.hasProperty(RDF.type, rdfClass)) {
				matchCandidates.put(flowableMatchCandidate, "?");
				// THIS IS AN fCAS-fCAS MATCH
			}
		}
		matchCandidates.remove(tdbResource); // JUST IN CASE YOU TRIED TO MATCH YOURSELF!!
		return autosetStatus();
	}

	private boolean autosetStatus() {
		boolean hit = false;
		String qCAS = getCas();
		Literal qCASLiteral = null;
		if (qCAS != null) {
			qCASLiteral = ActiveTDB.tsCreateTypedLiteral(qCAS);
		}
		// Literal nameLiteral = tdbResource.getProperty(RDFS.label).getObject().asLiteral();
		String qName = getName();
		Literal qNameLiteral = ActiveTDB.tsCreateTypedLiteral(qName);
		// Literal casLiteral = (Literal) tdbResource.getProperty(ECO.casNumber).getObject().asLiteral();

		for (Resource candidateFlowableTDBResource : matchCandidates.keySet()) {
			System.out.println("Resource: candidateFlowableTDBResource" + candidateFlowableTDBResource);
			StmtIterator thing = candidateFlowableTDBResource.listProperties();
			while (thing.hasNext()) {
				Statement fred = thing.next();
				System.out.println("fred.getPredicate() = " + fred.getPredicate());
			}

			// CRITERION 1: QUERY HAS NO CAS AND NAME MATCHES A NAME OR SYNONYM
			if (qCASLiteral == null) {
				if (candidateFlowableTDBResource.hasProperty(RDFS.label, qNameLiteral)
						|| candidateFlowableTDBResource.hasProperty(SKOS.altLabel, qNameLiteral)) {
					matchCandidates.put(candidateFlowableTDBResource, "=");
					hit = true;
					continue;
					// FlowsWorkflow.addFlowableRowNum(firstRow);
				}
				// CRITERION 2: QUERY HAS NO CAS AND SYNONYM MATCHES A NAME OR SYNONYM
				for (String synonym : getSynonyms()) {
					Literal qSynLiteral = qCASLiteral = ActiveTDB.tsCreateTypedLiteral(synonym.toLowerCase());
					if (candidateFlowableTDBResource.hasProperty(RDFS.label, qSynLiteral)
							|| candidateFlowableTDBResource.hasProperty(SKOS.altLabel, qSynLiteral)) {
						matchCandidates.put(candidateFlowableTDBResource, "=");
						hit = true;
						continue;
						// FlowsWorkflow.addFlowableRowNum(firstRow);
					}

				}

			} else {
				if (candidateFlowableTDBResource.hasProperty(ECO.casNumber, qCASLiteral)) {
					// CRITERION 3: CAS MATCHES AND NAME MATCHES A NAME OR SYNONYM

					if (candidateFlowableTDBResource.hasProperty(RDFS.label, qNameLiteral)
							|| candidateFlowableTDBResource.hasProperty(SKOS.altLabel, qNameLiteral)) {
						matchCandidates.put(candidateFlowableTDBResource, "=");
						hit = true;
						continue;
						// FlowsWorkflow.addFlowableRowNum(firstRow);
					}
					// CRITERION 4: CAS MATCHES AND SYNONYM MATCHES A NAME OR SYNONYM
					for (String synonym : getSynonyms()) {
						Literal qSynLiteral = qCASLiteral = ActiveTDB.tsCreateTypedLiteral(synonym.toLowerCase());
						if (candidateFlowableTDBResource.hasProperty(RDFS.label, qSynLiteral)
								|| candidateFlowableTDBResource.hasProperty(SKOS.altLabel, qSynLiteral)) {
							matchCandidates.put(candidateFlowableTDBResource, "=");
							hit = true;
							continue;
							// FlowsWorkflow.addFlowableRowNum(firstRow);
						}

					}
				}
			}
		}
		return hit;
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

}
