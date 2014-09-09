package gov.epa.nrmrl.std.lca.ht.flowable.mgr;

import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataType;
import gov.epa.nrmrl.std.lca.ht.jenaTDB.Issue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.Property;

// DEPRECATED

public final class CAS extends LCADataType {
	// private static String cas;
//	private static Pattern acceptableFormat = Pattern.compile("^\\d{2,}-\\d\\d-\\d$|^\\d{5,}$");
//
//	// NOTE THE "FIRST" VALID CAS IS 50-00-0, I.E. FIRST COMPONENT >= 50
//	// VALUES BEGINNING WITH 999, 977, OR 888 ARE FDA NUMBERS. ONLY THE 888 MATCH CHECKSUMS
//
	public CAS(String displayString, String parentGroup, boolean required, boolean unique, boolean isLiteral, Property rdfProperty) {
		// this.displayString = "CAS";

		super("CAS", "Flowable", false, true);
	}
//
//	@Override
//	public List<QACheck> getQaChecks() {
//		List<QACheck> allChecks = super.initializeQAChecks();
//
//		Pattern p1 = acceptableFormat;
//		Issue i1 = new Issue("Non-standard CAS format", "CAS numbers may only have all digits, or digits with \"-\" signs 4th and 2nd from the end .",
//				"Parse digits.  To parse the numeric components, use the auto-clean function.", true);
//		allChecks.add(new QACheck(i1.getDescription(), p1, i1));		
//		return allChecks;
//	}
//
//	@Override
//	public void setQaChecks(List<QACheck> qaChecks) {
//		this.qaChecks = qaChecks;
//	}
//
//
//	public static boolean validStandardFormat(String candidate) {
//		Matcher matcher = acceptableFormat.matcher(candidate);
//		if (matcher.find()) {
//			return true;
//		}
//		return false;
//	}
//
//	public static String standardize(String candidate) {
//		String standardizedCas = "";
//		String digitsOnly = strip(candidate);
//		if (digitsOnly == null) {
//			return digitsOnly;
//		}
//
//		Matcher digitMatcher = Pattern.compile("\\d").matcher(digitsOnly);
//		int digitsCount = digitMatcher.groupCount();
//		for (int i = 0; i < digitsCount; i++) {
//			if (i == digitsCount - 3 || i == digitsCount - 1) {
//				standardizedCas += "-";
//			}
//			standardizedCas += digitMatcher.group(i);
//		}
//		return standardizedCas;
//	}
//
//	public static String strip(String candidate) {
//		String strippedCas = "";
//		Matcher digitMatcher = Pattern.compile("\\d").matcher(candidate);
//		int digitsCount = digitMatcher.groupCount();
//		for (int i = 0; i < digitsCount; i++) {
//			strippedCas += digitMatcher.group(i);
//		}
//		while (candidate.startsWith("0")) {
//			candidate = candidate.substring(1);
//		}
//		if (validStandardFormat(strippedCas)) {
//			return strippedCas;
//		}
//		return null;
//	}
//
//	public static boolean correctCheckSum(String candidate) {
//		String strippedCas = strip(candidate);
//		if (strippedCas == null) {
//			return false;
//		}
//		int multiplier = 0;
//		int checksum = -Integer.parseInt(strippedCas.substring(strippedCas.length(), 1));
//		for (int i = strippedCas.length() - 2; i >= 0; i--) {
//			multiplier++;
//			checksum += multiplier * Integer.parseInt(strippedCas.substring(i, 1));
//		}
//		if (checksum % 10 == 0) {
//			return true;
//		}
//		return false;
//	}
}
