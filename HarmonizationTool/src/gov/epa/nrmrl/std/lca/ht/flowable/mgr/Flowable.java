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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

	// CONSTRUCTORS
	public Flowable() {
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
		lcaDataValues = new ArrayList<LCADataValue>();
	}

	public Flowable(Resource tdbResource) {
		this.tdbResource = tdbResource;
		lcaDataValues = new ArrayList<LCADataValue>();
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

//		boolean found = false;
//		if (lcaDataPropertyProvider.isUnique()) {
//			for (LCADataValue lcaDataValue : lcaDataValues) {
//				if (lcaDataValue.getLcaDataPropertyProvider().equals(lcaDataPropertyProvider)) {
//					lcaDataValue.setValueAsString(valueAsString);
//					found = true;
//					Object object = lcaDataValue.getValue();
//					ActiveTDB.tsReplaceLiteral(tdbResource, lcaDataPropertyProvider.getTDBProperty(), rdfDatatype,
//							object);
//					continue;
//				}
//			}
//		}
//		if (!found) {
//			LCADataValue lcaDataValue = new LCADataValue();
//			lcaDataValue.setLcaDataPropertyProvider(lcaDataPropertyProvider);
//			lcaDataValue.setValueAsString(valueAsString);
//			Object object = lcaDataValue.getValue();
//			lcaDataValues.add(lcaDataValue);
//			ActiveTDB.tsAddLiteral(tdbResource, lcaDataPropertyProvider.getTDBProperty(), rdfDatatype, object);
//		}
		ActiveTDB.tsAddLiteral(tdbResource, lcaDataPropertyProvider.getTDBProperty(), rdfDatatype, valueAsString);
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
		return (String[]) getAllProperties(flowableSynonymString);
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

	public static Set<MatchCandidate> findMatches(Flowable flowable) {
		Set<MatchCandidate> results = new HashSet<MatchCandidate>();
		Resource qDataSource = flowable.getTdbResource().getPropertyResourceValue(ECO.hasDataSource);

		// FIND MATCHES FOR THIS FLOWABLE
		// FIND MATCHES INVOLVING NAMES AND SYNONYMS:
		// Q-NAME = DB-NAME
		// return results;
		Statement statement = flowable.getTdbResource().getProperty(RDFS.label);
		if (statement == null) {
//			System.out.println("Nothing here");
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

	// public static String compareFlowables(Resource queryFlowableResource, Resource referenceFlowableResource) {
	// Flowable queryFlowable = new Flowable(queryFlowableResource); // TEMP
	// queryFlowable.syncDataFromTDB();
	// Flowable referenceFlowable = new Flowable(referenceFlowableResource);
	// referenceFlowable.syncDataFromTDB();
	// return compareFlowables(queryFlowable, referenceFlowable);
	// }

	// public List<Flowable> findMatchingFlowables(Flowable flowable) {
	// List<Flowable> hits = new ArrayList<Flowable>();
	// return hits;
	// }

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

	// CSVColumnInfo(String headerString, boolean isRequired, boolean isUnique,
	// List<QACheck>
	// checkLists)
	// FIXME - THIS QUICK HACK CREATES THESE NAMES ONCE, A OBJECT WITH A FACTORY AND ACCESSOR SHOULD BE USED

	// public static final CSVColumnInfo[] getHeaderMenuObjects() {
	// CSVColumnInfo[] results = new CSVColumnInfo[5];
	//
	// results[0] = new CSVColumnInfo(flowableNameString);
	// results[0].setRequired(true);
	// results[0].setUnique(true);
	// results[0].setCheckLists(getFlowablesNameCheckList());
	// results[0].setRDFClass(rdfClass);
	// results[0].setTdbProperty(RDFS.label);
	// results[0].setRdfDatatype(XSDDatatype.XSDstring);
	//
	// results[1] = new CSVColumnInfo(flowableSynonymString);
	// results[1].setRequired(false);
	// results[1].setUnique(false);
	// results[1].setCheckLists(getFlowablesNameCheckList());
	// results[1].setRDFClass(rdfClass);
	// results[1].setTdbProperty(SKOS.altLabel);
	// results[1].setRdfDatatype(XSDDatatype.XSDstring);
	//
	// results[2] = new CSVColumnInfo(casString);
	// results[2].setRequired(false);
	// results[2].setUnique(true);
	// results[2].setCheckLists(getCASCheckList());
	// results[2].setLeftJustified(false);
	// results[2].setRDFClass(rdfClass);
	// results[2].setTdbProperty(ECO.casNumber);
	// results[2].setRdfDatatype(XSDDatatype.XSDstring);
	//
	// results[3] = new CSVColumnInfo(chemicalFormulaString);
	// results[3].setRequired(false);
	// results[3].setUnique(false);
	// results[3].setCheckLists(getFormulaCheckList());
	// results[3].setLeftJustified(false);
	// results[3].setRDFClass(rdfClass);
	// results[3].setTdbProperty(ECO.chemicalFormula);
	// results[3].setRdfDatatype(XSDDatatype.XSDstring);
	//
	// results[4] = new CSVColumnInfo(smilesString);
	// results[4].setRequired(false);
	// results[4].setUnique(false);
	// results[4].setCheckLists(getSmilesCheckList());
	// results[4].setLeftJustified(false);
	// results[4].setRDFClass(rdfClass);
	// results[4].setTdbProperty(FedLCA.hasSmilesString);
	// results[4].setRdfDatatype(XSDDatatype.XSDstring);
	//
	// return results;
	// }

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
		String e2 = "CAS numbers must be a) blank, b) 5+ digits, or c) digits with \"-\" signs 4th and 2nd from the end.";
		String s2 = "Parse digits into a formatted CAS";
		Pattern acceptableCASFormat = Pattern.compile("^$|^0*(\\d{2,})-?(\\d\\d)-?(\\d)$");
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

	//
	// public void setName(String name) {
	// this.name = name;
	// ActiveTDB.tsReplaceLiteral(tdbResource, RDFS.label, name);
	// }
	//
	// public List<String> getSynonyms() {
	// return synonyms;
	// }
	//
	// public void setSynonyms(List<String> synonyms) {
	// ActiveTDB.tsRemoveAllObjects(tdbResource, SKOS.altLabel);
	// this.synonyms = synonyms;
	// for (String synonym : synonyms) {
	// ActiveTDB.tsAddLiteral(tdbResource, SKOS.altLabel, synonym);
	// }
	// }
	//
	// public void addSynonym(String synonym) {
	// if (synonyms == null) {
	// synonyms = new ArrayList<String>();
	// }
	// synonyms.add(synonym);
	// ActiveTDB.tsAddLiteral(tdbResource, SKOS.altLabel, synonym);
	// }
	//
	// public void removeSynonym(String synonym) {
	// this.synonyms.remove(synonym);
	// Literal literalToRemove = ActiveTDB.tsCreateTypedLiteral(synonym);
	// ActiveTDB.tsRemoveStatement(tdbResource, SKOS.altLabel, literalToRemove);
	// }
	//
	// public String getCas() {
	// return cas;
	// }
	//
	// public void setCas(String cas) {
	// this.cas = cas;
	// ActiveTDB.tsReplaceLiteral(tdbResource, ECO.casNumber, cas);
	// }

	// public boolean isEmission() {
	// return isEmission;
	// }
	//
	// public void setEmission(boolean isEmission) {
	// this.isEmission = isEmission;
	// if (isEmission) {
	// ActiveTDB.addLiteral(tdbResource, RDF.type, FASC.EmissionCompartment);
	// } else {
	// ActiveTDB.removeStatement(tdbResource, RDF.type, FASC.EmissionCompartment);
	// }
	// }

	// public boolean isResource() {
	// return isResource;
	// }
	//
	// public void setResource(boolean isResource) {
	// this.isResource = isResource;
	// if (isResource) {
	// ActiveTDB.addLiteral(tdbResource, RDF.type, FASC.ResourceConsumptionCompartment);
	// } else {
	// ActiveTDB.removeStatement(tdbResource, RDF.type, FASC.ResourceConsumptionCompartment);
	// }
	// }

	// public String getFormula() {
	// return formula;
	// }
	//
	// public void setFormula(String formula) {
	// this.formula = formula;
	// ActiveTDB.tsReplaceLiteral(tdbResource, ECO.chemicalFormula, formula);
	// }
	//
	// public String getSmiles() {
	// return smiles;
	// }
	//
	// public void setSmiles(String smiles) {
	// this.smiles = smiles;
	// ActiveTDB.tsReplaceLiteral(tdbResource, FedLCA.hasSmilesString, smiles);
	//
	// }

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

	// private static Pattern acceptableCASFormat =
	// Pattern.compile("^0*(\\d{2,})-(\\d\\d)-(\\d)$|^0*(\\d{5,})$|^$");

	// public List<QACheck> getQaChecks() {
	// List<QACheck> allChecks = new ArrayList<QACheck>();
	//
	// Pattern p1 = acceptableCASFormat;
	// Issue i1 = new Issue("Non-standard CAS format",
	// "CAS numbers may only have all digits, or digits with \"-\" signs 4th and 2nd from the end .",
	// "Parse digits.  To parse the numeric components, use the auto-clean function.",
	// true);
	// allChecks.add(new QACheck(i1.getDescription(), p1, true, i1));
	// return allChecks;
	// }

	// public List<String> getHeadersList() {
	// List<String> headerList = new ArrayList<String>();
	// headerList.add("Flowable Name");
	// headerList.add("Flowable AltName");
	// headerList.add("CAS");
	// headerList.add("Chemical formula");
	// headerList.add("SMILES");
	// return headerList;
	// }

	// public static Issue fullyCheckCAS(String cas){
	// QACheck casQACheck = makeCASQACheck();
	// Issue issue = new Issue();
	// return issue;
	// }
	//
	// private QACheck makeCASQACheck(){
	// QACheck casQACheck = new QACheck("Full CAS check",
	// "Includes format and checksum", null,
	// null, null, false);
	// casQACheck.setHandlerMethod(fullyCheckCAS(new Issue()));
	// return casQACheck;
	// }
	public static Resource getRdfclass() {
		return rdfClass;
	}

	public static Map<String, LCADataPropertyProvider> getDataPropertyMap() {
		return dataPropertyMap;
	}

	// public static String getFlowableNameString() {
	// return flowableNameString;
	// }
	//
	// public static String getFlowableSynonymString() {
	// return flowableSynonymString;
	// }
	//
	// public static String getCASString() {
	// return casString;
	// }
	//
	// public static String getChemicalformulastring() {
	// return chemicalFormulaString;
	// }
	//
	// public static String getSMILESString() {
	// return smilesString;
	// }
}
