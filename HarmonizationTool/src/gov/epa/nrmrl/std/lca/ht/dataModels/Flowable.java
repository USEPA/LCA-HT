package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVColumnInfo;
import harmonizationtool.vocabulary.ECO;

import java.util.List;
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.Resource;

public class Flowable {
	private String name = "";
	private String cas = null;
	private boolean isEmission = false;
	private boolean isResource = false;
	private List<String> altNames = null;
	private String formula = null;
	private String SMILES = null;
	private Resource rdfClass = ECO.Flowable;
//	private static Pattern acceptableCASFormat = Pattern.compile("^0*(\\d{2,})-(\\d\\d)-(\\d)$|^0*(\\d{5,})$|^$");

	// public List<QACheck> getQaChecks() {
	// List<QACheck> allChecks = new ArrayList<QACheck>();
	//
	// Pattern p1 = acceptableCASFormat;
	// Issue i1 = new Issue("Non-standard CAS format",
	// "CAS numbers may only have all digits, or digits with \"-\" signs 4th and 2nd from the end .",
	// "Parse digits.  To parse the numeric components, use the auto-clean function.", true);
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
	
//	public static Issue fullyCheckCAS(String cas){
//		QACheck casQACheck = makeCASQACheck();
//		Issue issue = new Issue();
//		return issue;
//	}
//	
//	private QACheck makeCASQACheck(){
//		QACheck casQACheck = new QACheck("Full CAS check", "Includes format and checksum", null, null, null, false);
//		casQACheck.setHandlerMethod(fullyCheckCAS(new Issue()));
//		return casQACheck;
//	}

	public static CSVColumnInfo[] getHeaderMenuObjects() {
		CSVColumnInfo[] results = new CSVColumnInfo[5];
		results[0] = new CSVColumnInfo("Flowable Name", true, true, getFlowablesNameCheckList());
		results[1] = new CSVColumnInfo("Flowable Synonym", false, false, getFlowablesNameCheckList());
		results[2] = new CSVColumnInfo("CAS", false, true, getCASCheckList());
		results[2].setLeftJustified(false);
		results[3] = new CSVColumnInfo("Chemical formula", false, false, getFormulaCheckList());
		results[4] = new CSVColumnInfo("SMILES", false, false, getSMILESCheckList());
		return results;
	}

	private static List<QACheck> getSMILESCheckList() {
		List<QACheck> qaChecks = QACheck.getGeneralQAChecks();
		// Issue i1 = new Issue("Double quote",
		// "Chemical names may have a prime (single quote), but two or three primes should be represented by multiple single quote characters.",
		// "Replace the double quote with two single quotes.  You may also use the auto-clean function.",
		// true);
		// qaChecks.add(new QACheck(acceptableCASFormat, i1));
		return qaChecks;
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

//		String d1 = "Non-standard CAS format";
//		String e1 = "CAS numbers may only have either a) all digits, or b) digits with \"-\" signs 4th and 2nd from the end.";
//		String s1 = "Parse digits into a formatted CAS";
//		Pattern p1 = Pattern.compile("^\\s*$");
//		String r1 = "";
//		qaChecks.add(new QACheck(d1, e1, s1, p1, r1, false));
		
		String d2 = "Non-standard CAS format";
		String e2 = "CAS numbers must be a) blank, b) 5+ digits, or c) digits with \"-\" signs 4th and 2nd from the end.";
		String s2 = "Parse digits into a formatted CAS";
		Pattern acceptableCASFormat = Pattern.compile("^$|^0*(\\d{2,})-?(\\d\\d)-?(\\d)$");
//		String r2 = "$1-$2-$3";
		qaChecks.add(new QACheck(d2, e2, s2, acceptableCASFormat, null, true));
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
	// public FlowableHeaderObj(String headerString, boolean isRequired, boolean isUnique){
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
		if (strippedCas.equals("")){
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
		digitsOnly.replaceAll("^0+", "");
		if (Integer.parseInt(digitsOnly)<50000){
			return null;
		}
		standardizedCas = digitsOnly.substring(0, digitsOnly.length() - 3);
		standardizedCas += "-";
		standardizedCas += digitsOnly.substring(digitsOnly.length() - 3, digitsOnly.length() - 1);
		standardizedCas += "-";
		standardizedCas += digitsOnly.substring(digitsOnly.length() - 1, digitsOnly.length());
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCas() {
		return cas;
	}

	public void setCas(String cas) {
		this.cas = cas;
	}

	public boolean isEmission() {
		return isEmission;
	}

	public void setEmission(boolean isEmission) {
		this.isEmission = isEmission;
	}

	public boolean isResource() {
		return isResource;
	}

	public void setResource(boolean isResource) {
		this.isResource = isResource;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public String getSMILES() {
		return SMILES;
	}

	public void setSMILES(String sMILES) {
		SMILES = sMILES;
	}

	public List<String> getAltNames() {
		return altNames;
	}

	public void setAltNames(List<String> altNames) {
		this.altNames = altNames;
	}

	public Resource getRdfClass() {
		return rdfClass;
	}

	public void setRdfClass(Resource rdfClass) {
		this.rdfClass = rdfClass;
	}
}
