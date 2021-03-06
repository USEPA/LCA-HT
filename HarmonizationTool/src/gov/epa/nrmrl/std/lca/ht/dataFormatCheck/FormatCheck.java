package gov.epa.nrmrl.std.lca.ht.dataFormatCheck;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * An instance of a FormatCheck is used to check the format of a string to
 * determine if it can be parsed to the data type appropriate for the field for
 * which it is designed. Each data field may have zero or more FormatCheck
 * values defined. The result of running a FormatCheck test against a field is
 * an Issue whose Status is intended to be checked by a user before proceeding.
 * 
 * @author Tom Transue
 * 
 */
public class FormatCheck {
	private String description;
	private String explanation;
	private String suggestion;
	private Pattern pattern;
	private String replacement;
	private boolean patternMustMatch;
	private Object handlerMethod = null;

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	public FormatCheck(String description, String explanation,
			String suggestion, Pattern pattern, String replacement,
			boolean patternMustMatch) {
		this.description = description;
		this.explanation = explanation;
		this.suggestion = suggestion;
		this.pattern = pattern;
		this.replacement = replacement;
		this.patternMustMatch = patternMustMatch;
	}

	public FormatCheck(FormatCheck qaCheck) {
		this.description = qaCheck.getDescription();
		this.explanation = qaCheck.getExplanation();
		this.suggestion = qaCheck.getSuggestion();
		this.pattern = qaCheck.getPattern();
		this.replacement = qaCheck.getReplacement();
		this.patternMustMatch = qaCheck.isPatternMustMatch();
	}

	public Pattern getPattern() {
		return pattern;
	}

	public static List<FormatCheck> getGeneralQAChecks() {
		List<FormatCheck> qaCheckPack = new ArrayList<FormatCheck>();
		String d1 = "Bookend quotes";
		String e1 = "The text is surrounded by apparently superfluous double quote marks.";
		String s1 = "Remove these quote marks";
		Pattern p1 = Pattern.compile("^\"([^\"]*)\"$");
		String r1 = "$1";
		qaCheckPack.add(new FormatCheck(d1, e1, s1, p1, r1, false));

		String d2 = "Leading and trailing space(s)";
		String e2 = "At least one white space character occurs both before and after text.";
		String s2 = "Remove leading and trailing space(s)";
		Pattern p2 = Pattern.compile("^\\s+(.*?)\\s+$");
		String r2 = "$1";
		qaCheckPack.add(new FormatCheck(d2, e2, s2, p2, r2, false));

		String d3 = "Leading space(s)";
		String e3 = "At least one white space character occurs before text.";
		String s3 = "Remove leading space(s)";
		Pattern p3 = Pattern.compile("^\\s+(.*)$");
		String r3 = "$1";
		qaCheckPack.add(new FormatCheck(d3, e3, s3, p3, r3, false));

		String d4 = "Trailing space(s)";
		String e4 = "At least one white space character occurs after text.";
		String s4 = "Remove leading space(s)";
		Pattern p4 = Pattern.compile("^(.*?)\\s+$");
		String r4 = "$1";
		qaCheckPack.add(new FormatCheck(d4, e4, s4, p4, r4, false));

		String d5 = "Non-ASCII character";
		String e5 = "A character outside the standard ASCII printable range was detected.  This can not be auto-resolved.";
		String s5 = "Run the character-encoding tool";
		Pattern p5 = Pattern.compile("[^ -~]");
		String r5 = null;
		qaCheckPack.add(new FormatCheck(d5, e5, s5, p5, r5, false));
		return qaCheckPack;
	}

	public static List<FormatCheck> getUUIDCheck() {
		List<FormatCheck> qaCheckPack = new ArrayList<FormatCheck>();
		String d1 = "UUID format";
		String e1 = "The text does not match a properly formatted UUID: hex digits separated by dashes: 8-4-4-4-12.";
		String s1 = "Check format.  For example, sometimes leading zeros get removed. Note that a blank string is acceptable, but space characters are not.";
		Pattern p1 = Pattern
				.compile("^$||^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$");
		String r1 = null;
		qaCheckPack.add(new FormatCheck(d1, e1, s1, p1, r1, true));

		return qaCheckPack;
	}

	public boolean isPatternMustMatch() {
		return patternMustMatch;
	}

	public void setPatternMustMatch(boolean patternMustMatch) {
		this.patternMustMatch = patternMustMatch;
	}

	public String getReplacement() {
		return replacement;
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExplanation() {
		return explanation;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

	public String getSuggestion() {
		return suggestion;
	}

	public void setSuggestion(String suggestion) {
		this.suggestion = suggestion;
	}

	public Object getHandlerMethod() {
		return handlerMethod;
	}

	public void setHandlerMethod(Object handlerMethod) {
		this.handlerMethod = handlerMethod;
	}

	public static List<FormatCheck> getFloatCheck() {
		List<FormatCheck> qaCheckPack = new ArrayList<FormatCheck>();
		String d1 = "Float format";
		String e1 = "The text does not match a properly formatted floating point number.";
		String s1 = "Check format.";
		Pattern p1 = Pattern
				.compile("^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$");
		String r1 = null;
		qaCheckPack.add(new FormatCheck(d1, e1, s1, p1, r1, true));

		return qaCheckPack;
	}

}
