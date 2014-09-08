package gov.epa.nrmrl.std.lca.ht.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileEncodingUtil {

	private final String cr = System.getProperty("line.separator");

	private static List<String> commonEncodingSets = new ArrayList<String>(Arrays.asList("UTF8", "Cp1252", "ISO8859_1", "MacRoman"));

	private void init() {
		final Map<Integer, String> characterNames = new LinkedHashMap<Integer, String>();
		characterNames.put(0, "name");
	}

	// ENCODINGS FOUND AT:
	// http://docs.oracle.com/javase/7/docs/technotes/guides/intl/encoding.doc.html

	private static List<String> basicEncodingSets = new ArrayList<String>(Arrays.asList("Cp858", "Cp437", "Cp775", "Cp850", "Cp852", "Cp855", "Cp857", "Cp862", "Cp866",
			"ISO8859_1", "ISO8859_2", "ISO8859_4", "ISO8859_5", "ISO8859_7", "ISO8859_9", "ISO8859_13", "ISO8859_15", "KOI8_R", "KOI8_U", "ASCII", "UTF8", "UTF-16",
			"UnicodeBigUnmarked", "UnicodeLittleUnmarked", "UTF_32", "UTF_32BE", "UTF_32LE", "UTF_32BE_BOM", "UTF_32LE_BOM", "Cp1250", "Cp1251", "Cp1252", "Cp1253", "Cp1254",
			"Cp1257", "UnicodeBig", "Cp737", "Cp874", "UnicodeLittle"));

	private static List<String> extendedEncodingSet = new ArrayList<String>(Arrays.asList("Big5", "Big5_HKSCS", "EUC_JP", "EUC_KR", "GB18030", "EUC_CN", "GBK", "Cp838", "Cp1140",
			"Cp1141", "Cp1142", "Cp1143", "Cp1144", "Cp1145", "Cp1146", "Cp1147", "Cp1148", "Cp1149", "Cp037", "Cp1026", "Cp1047", "Cp273", "Cp277", "Cp278", "Cp280", "Cp284",
			"Cp285", "Cp297", "Cp420", "Cp424", "Cp500", "Cp860", "Cp861", "Cp863", "Cp864", "Cp865", "Cp868", "Cp869", "Cp870", "Cp871", "Cp918", "ISO2022CN", "ISO2022JP",
			"ISO2022KR", "ISO8859_3", "ISO8859_6", "ISO8859_8", "JIS_X0201", "JIS_X0212-1990", "SJIS", "TIS620", "Cp1255", "Cp1256", "Cp1258", "MS932", "Big5_Solaris",
			"EUC_JP_LINUX", "EUC_TW", "EUC_JP_Solaris", "Cp1006", "Cp1025", "Cp1046", "Cp1097", "Cp1098", "Cp1112", "Cp1122", "Cp1123", "Cp1124", "Cp1381", "Cp1383", "Cp33722",
			"Cp834", "Cp856", "Cp875", "Cp921", "Cp922", "Cp930", "Cp933", "Cp935", "Cp937", "Cp939", "Cp942", "Cp942C", "Cp943", "Cp943C", "Cp948", "Cp949", "Cp949C", "Cp950",
			"Cp964", "Cp970", "ISCII91", "ISO2022_CN_CNS", "ISO2022_CN_GB", "x-iso-8859-11", "x-JIS0208", "JISAutoDetect", "x-Johab", "MacArabic", "MacCentralEurope",
			"MacCroatian", "MacCyrillic", "MacDingbat", "MacGreek", "MacHebrew", "MacIceland", "MacRoman", "MacRomania", "MacSymbol", "MacThai", "MacTurkish", "MacUkraine",
			"MS950_HKSCS", "MS936", "PCK", "x-SJIS_0213", "Cp50220", "Cp50221", "MS874", "MS949", "MS950", "x-windows-iso2022jp"));

	public String showFileIssues(String path, String encoding) {
		// String encoding = "ISO8859_1";
		// String encoding = "UTF8";
		String issues = null;
		if (path == null) {
			issues = "(Filepath is null)";
			return issues;
		}

		File file = new File(path);
		if (!file.exists()) {
			issues = "File: " + path + " does not exist!";
			return issues;
		}

		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), encoding));
			// bufferedReader = new BufferedReader(new InputStreamReader(new
			// FileInputStream(path),
			// "UTF8"));
			// bufferedReader = new BufferedReader(new InputStreamReader(new
			// FileInputStream(path),
			// "Cp1252"));
			// bufferedReader = new BufferedReader(new InputStreamReader(new
			// FileInputStream(path),
			// "MacRoman"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (bufferedReader == null) {
			issues = "Can not read file: " + path + "!";
			return issues;
		}

		try {
			String nonAsciiRegexString = "[^ -~]";
			// String nonAsciiRegexString = "[^\\p{ASCII}]";

			Pattern nonAsciiPattern = Pattern.compile(nonAsciiRegexString);
			int lineNumber = 0;
			while (bufferedReader.ready()) {
				String line = bufferedReader.readLine();
				lineNumber++;
				Matcher matcher = nonAsciiPattern.matcher(line);
				while (matcher.find()) {
					String nonAsciiChar = matcher.group();
					int unicodeCharNumber = nonAsciiChar.codePointAt(0);
					int colNumber = matcher.end();
					if (issues == null) {
						issues = "";
					}
					issues += "#     Line: " + lineNumber + ", column: " + colNumber + ". found character number: " + unicodeCharNumber + ". It looks like:->" + nonAsciiChar
							+ "<-" + System.getProperty("line.separator");
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return issues;
	}

	public String showFileIssues(String path) {
		String allIssues = "";
		for (String encoding : commonEncodingSets) {
			System.out.println("Checking encoding: " + encoding);
			String issue0 = showFileIssues(path, encoding);
			allIssues += "#   Basic Encoding: " + encoding + ". Issues: " + cr + issue0 + cr;
		}
		// for (String encoding: basicEncodingSets){
		// System.out.println("Checking encoding: " + encoding);
		// String issue0 = showFileIssues(path, encoding);
		// allIssues+= "Basic Encoding: "+encoding+". Issues: \n"+issue0+"\n";
		// }
		// for (String encoding: extendedEncodingSet){
		// System.out.println("Checking encoding: " + encoding);
		// String issue0 = showFileIssues(path, encoding);
		// allIssues+=
		// "Extended Encoding: "+encoding+". Issues: \n"+issue0+"\n";
		// }
		return allIssues;
	}

	public static boolean containsEncoding(String encoding) {
		if (basicEncodingSets.contains(encoding)) {
			return true;
		}
		if (extendedEncodingSet.contains(encoding)) {
			return true;
		}
		return false;
	}

	public static List<NonAsciiChar> getFirstNonAsciiChar(String path, int encodingDepth) {
		List<NonAsciiChar> nonAsciiChars = new ArrayList<NonAsciiChar>();
		String nonAsciiRegexString = "[^ -~]";
		// String nonAsciiRegexString = "[^\\p{ASCII}]";
		Pattern nonAsciiPattern = Pattern.compile(nonAsciiRegexString);

		List<String> encodingList = commonEncodingSets;
		if (encodingDepth == 1) {
			encodingList = basicEncodingSets;
		} else if (encodingDepth == 2) {
			encodingList = extendedEncodingSet;
		}

		if (path == null) {
			return null;
		}

		File file = new File(path);
		if (!file.exists()) {
			return null;
		}

		for (String encoding : encodingList) {
			NonAsciiChar nonAsciiChar = new NonAsciiChar();
			System.out.println("encoding: "+encoding);
			BufferedReader bufferedReader = null;
			try {
				bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), encoding));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (bufferedReader == null) {
				continue;
			}

			try {
				int lineNumber = 0;
				while (bufferedReader.ready()) {
					String line = bufferedReader.readLine();
					lineNumber++;
//					System.out.print("linenumber: "+ lineNumber);
					Matcher matcher = nonAsciiPattern.matcher(line);
					if (matcher.find()) {
						System.out.print("Got one: "+ matcher);

						nonAsciiChar.lineNumber = lineNumber;
						nonAsciiChar.character = matcher.group();
						nonAsciiChar.unicodeCodepoint = nonAsciiChar.character.codePointAt(0);
						nonAsciiChar.colNumber = matcher.end();
//						continue;
					}
				}
			} catch (IOException e1) {
//				continue;
				// IF AN ENCODING IS NOT COMPATIBLE, DON'T INCLUDE IT
				// e1.printStackTrace();
			}
			if (nonAsciiChar.character != null) {
				nonAsciiChars.add(nonAsciiChar);
			}
		}
		// --
		return nonAsciiChars;
	}
}
