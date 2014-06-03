package gov.epa.nrmrl.std.lca.ht.dataModels;

//import gov.epa.nrmrl.std.lca.ht.csvFiles.CsvTableViewerColumnType;
import harmonizationtool.model.Issue;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

//import org.eclipse.swt.widgets.TableColumn;

public class QACheck {
	private String name;

	private Pattern pattern;
	private boolean patternMustMatch;
	private String replacement = null;
	private Issue issue;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	public QACheck(Pattern pattern, boolean patternMustMatch, Issue issue) {
		this.name = null;
		this.pattern = pattern;
		this.patternMustMatch = patternMustMatch;
		this.issue = issue;
	}

	public QACheck(String name, Pattern pattern, boolean patternMustMatch, Issue issue) {
		this.name = name;
		this.pattern = pattern;
		this.patternMustMatch = patternMustMatch;
		this.issue = issue;
	}

	public QACheck(Pattern pattern, boolean patternMustMatch, String replacement, Issue issue) {
		this.name = null;
		this.pattern = pattern;
		this.patternMustMatch = patternMustMatch;
		this.replacement = replacement;
		this.issue = issue;
	}

	public QACheck(String name, Pattern pattern, boolean patternMustMatch, String replacement, Issue issue) {
		this.name = name;
		this.pattern = pattern;
		this.patternMustMatch = patternMustMatch;
		this.replacement = replacement;
		this.issue = issue;
	}

	public Issue getIssue() {
		return issue;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public static List<QACheck> getGeneralQAChecks() {
		List<QACheck> qaCheckPack = new ArrayList<QACheck>();

		Pattern p1 = Pattern.compile("^\"([^\"]*)\"$");
		String r1 = "$1";
		Issue i1 = new Issue("Bookend quotes",
				"The text is surrounded by apparently superfluous double quote marks.",
				"Remove these quote marks.");
		qaCheckPack.add(new QACheck(i1.getDescription(), p1, false, r1, i1));

		Pattern p2 = Pattern.compile("^\\s+(.*?)\\s+$");
		String r2 = "$1";
		Issue i2 = new Issue(
				"Leading and trailing space(s)",
				"At least one white space character occurs both before and after text.  These may be non-printing characters.",
				"Remove leading and trailing space(s).");
		qaCheckPack.add(new QACheck(i2.getDescription(), p2, false, r2, i2));

		Pattern p3 = Pattern.compile("^(.*?)\\s+$");
		String r3 = "$1";

		Issue i3 = new Issue("Trailing space(s)", "Following text, at least one white space character occurs.  This may be a non-printing character.",
				"Remove trailing space(s)");
		qaCheckPack.add(new QACheck(i3.getDescription(), p3, false, r3, i3));

		Pattern p4 = Pattern.compile("^\\s+(.*)");
		String r4 = "$1";
		Issue i4 = new Issue("Leading space(s)", "Preceeding text, at least one white space character occurs.  This may be a non-printing character.",
				"Remove leading space(s)");
		qaCheckPack.add(new QACheck(i2.getDescription(), p4, false, r4, i4));

		for (QACheck qaCheck : qaCheckPack) {
			System.out.println("qaCheck.getName()" + qaCheck.getName());
		}
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

	// public static void checkColumn(TableColumn column) {
	// Object data = column.getData();
	// System.out.println("data: " + data);
	// System.out.println("data.getClass(): " + data.getClass());
	// }

	// public static List<QACheck> getQAChecks(CsvTableViewerColumnType type) {
	// return getGeneralQAChecks();
	// }

}
