package gov.epa.nrmrl.std.lca.ht.dataModels;

import harmonizationtool.vocabulary.ECO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.Property;

public final class CAS extends LCADataType{
//	private static String cas;
	private static Pattern acceptableFormat = Pattern.compile("^\\d{2,}-\\d\\d-\\d$|^\\d{5,}$");
	// 	NOTE THE "FIRST" VALID CAS IS 50-00-0, I.E. FIRST COMPONENT >= 50
	//  VALUES BEGINNING WITH 999, 977, OR 888 ARE FDA NUMBERS.  ONLY THE 888 MATCH CHECKSUMS

	
	public CAS(String displayString, String parentGroup, boolean required, boolean unique, boolean isLiteral, Property rdfProperty) {
//		this.displayString = "CAS";
		
		super("CAS", "Flowable", false, true, true, ECO.casNumber);
	}
	
	public static boolean validStandardFormat(String candidate) {
		Matcher matcher = acceptableFormat.matcher(candidate);
		if (matcher.find()) {
			return true;
		}
		return false;
	}

	public static String standardize(String candidate) {
		if (validStandardFormat(candidate)) {
			String standarizedCas = "";
			while (candidate.startsWith("0")) {
				candidate = candidate.substring(1);
			}
			Matcher digitMatcher = Pattern.compile("\\d").matcher(candidate);
			int digitsCount = digitMatcher.groupCount();
			for (int i = 0; i < digitsCount; i++) {
				if (i == digitsCount - 3 || i == digitsCount - 1) {
					standarizedCas += "-";
				}
				standarizedCas += digitMatcher.group(i);
			}
			return standarizedCas;
		}
		return null;
	}
	
	public static String strip(String candidate) {
		if (validStandardFormat(candidate)) {
			String strippedCas = "";
			while (candidate.startsWith("0")) {
				candidate = candidate.substring(1);
			}
			Matcher digitMatcher = Pattern.compile("\\d").matcher(candidate);
			int digitsCount = digitMatcher.groupCount();
			for (int i = 0; i < digitsCount; i++) {
//				if (i == digitsCount - 3 || i == digitsCount - 1) {
//					standarizedCas += "-";
//				}
				strippedCas += digitMatcher.group(i);
			}
			return strippedCas;
		}
		return null;
	}

	public static boolean correctCheckSum(String candidate) {
		String strippedCas = strip(candidate);
		if (strippedCas == null){
			return false;
		}
		int multiplier=0;
		int checksum=-Integer.parseInt(strippedCas.substring(strippedCas.length(),1));
		for (int i = strippedCas.length()-2;i>=0;i--){
			multiplier++;
			checksum+=multiplier*Integer.parseInt(strippedCas.substring(i,1));
		}
		if (checksum%10 == 0){
			return true;
		}
		return false;
	}
}
