package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import harmonizationtool.model.Issue;
import harmonizationtool.vocabulary.ECO;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
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

	private static Pattern acceptableCASFormat = Pattern.compile("^\\d{2,}-\\d\\d-\\d$|^\\d{5,}$");
	
	public List<QACheck> getQaChecks() {
		List<QACheck> allChecks = new ArrayList<QACheck>();

		Pattern p1 = acceptableCASFormat;
		Issue i1 = new Issue("Non-standard CAS format", "CAS numbers may only have all digits, or digits with \"-\" signs 4th and 2nd from the end .",
				"Parse digits.  To parse the numeric components, use the auto-clean function.", true);
		allChecks.add(new QACheck(i1.getDescription(), p1, i1));
		return allChecks;
	}

	// public List<String> getHeadersList() {
	// List<String> headerList = new ArrayList<String>();
	// headerList.add("Flowable Name");
	// headerList.add("Flowable AltName");
	// headerList.add("CAS");
	// headerList.add("Chemical formula");
	// headerList.add("SMILES");
	// return headerList;
	// }

	public static CSVTableView.CSVColumnInfo[] getHeaderMenuObjects() {
		CSVTableView.CSVColumnInfo[] results = new CSVTableView.CSVColumnInfo[5];
		results[0] = new CSVTableView.CSVColumnInfo("Flowable Name", true, true, getFlowablesNameCheckList());
		results[1] = new CSVTableView.CSVColumnInfo("Flowable Synonym", false, false, getFlowablesNameCheckList());
		results[2] = new CSVTableView.CSVColumnInfo("CAS", false, true, getCASCheckList());
		results[3] = new CSVTableView.CSVColumnInfo("Chemical formula", false, false, getFormulaCheckList());
		results[4] = new CSVTableView.CSVColumnInfo("SMILES", false, false, getSMILESCheckList());
		return results;
	}

	private static List<QACheck> getSMILESCheckList() {
		List<QACheck> qaChecks = QACheck.getGeneralQAChecks();
		Issue i1 = new Issue("Double quote", "Chemical names may have a prime (single quote), but two or three primes should be represented by multiple single quote characters.",
				"Replace the double quote with two single quotes.  You may also use the auto-clean function.", true);
		qaChecks.add(new QACheck(acceptableCASFormat , i1));
		return qaChecks;
	}

	private static List<QACheck> getFormulaCheckList() {
		List<QACheck> qaChecks = QACheck.getGeneralQAChecks();
//		Pattern p1 = Pattern.compile("?([A-Z][a-z]?\\d*)+");
//		Issue i1 = new Issue("Double quote", "Chemical names may have a prime (single quote), but two or three primes should be represented by multiple single quote characters.",
//				"Replace the double quote with two single quotes.  You may also use the auto-clean function.", true);
//		qaChecks.add(new QACheck(p1, i1));
		return qaChecks;
	}

	private static List<QACheck> getCASCheckList() {
		List<QACheck> qaChecks = QACheck.getGeneralQAChecks();
		return qaChecks;
	}

	private static List<QACheck> getFlowablesNameCheckList() {
		List<QACheck> qaChecks = QACheck.getGeneralQAChecks();
		Pattern p1 = Pattern.compile("\"");
		Issue i1 = new Issue("Double quote", "Chemical names may have a prime (single quote), but two or three primes should be represented by multiple single quote characters.",
				"Replace the double quote with two single quotes.  You may also use the auto-clean function.", true);
		qaChecks.add(new QACheck(p1, i1));
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

	public static boolean validStandardFormat(String candidate) {
		Matcher matcher = acceptableCASFormat.matcher(candidate);
		if (matcher.find()) {
			return true;
		}
		return false;
	}

	public static String standardizeCAS(String candidate) {
		String standardizedCas = "";
		String digitsOnly = stripCASdigits(candidate);
		if (digitsOnly == null) {
			return digitsOnly;
		}

		Matcher digitMatcher = Pattern.compile("\\d").matcher(digitsOnly);
		int digitsCount = digitMatcher.groupCount();
		for (int i = 0; i < digitsCount; i++) {
			if (i == digitsCount - 3 || i == digitsCount - 1) {
				standardizedCas += "-";
			}
			standardizedCas += digitMatcher.group(i);
		}
		return standardizedCas;
	}

	public static String stripCASdigits(String candidate) {
		String strippedCas = "";
		Matcher digitMatcher = Pattern.compile("\\d").matcher(candidate);
		int digitsCount = digitMatcher.groupCount();
		for (int i = 0; i < digitsCount; i++) {
			strippedCas += digitMatcher.group(i);
		}
		while (candidate.startsWith("0")) {
			candidate = candidate.substring(1);
		}
		if (validStandardFormat(strippedCas)) {
			return strippedCas;
		}
		return null;
	}

	public static boolean correctCASCheckSum(String casNumber) {
		String strippedCas = stripCASdigits(casNumber);
		if (strippedCas == null) {
			return false;
		}
		int multiplier = 0;
		int checksum = -Integer.parseInt(strippedCas.substring(strippedCas.length(), 1));
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

	public Pattern getAcceptableCASFormat() {
		return acceptableCASFormat;
	}

	public void setAcceptableCASFormat(Pattern acceptableCASFormat) {
		this.acceptableCASFormat = acceptableCASFormat;
	}
}
