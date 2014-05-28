package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CsvTableViewerColumnType;
import harmonizationtool.model.Issue;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.TableColumn;

public class QACheck {
	private String name;

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

	private Pattern pattern;
	private Issue issue;

	public QACheck(Pattern pattern, Issue issue) {
		this.name = null;
		this.pattern = pattern;
		this.issue = issue;
	}

	public QACheck(String name, Pattern pattern, Issue issue) {
		this.name = name;
		this.pattern = pattern;
		this.issue = issue;
	}

	public Issue getIssue() {
		return issue;
	}

	// public void csvColQACheck(CsvTableViewerColumn column, QACheck qaCheck){
	// // TableColumn tableColumn = (TableColumn) column.getColumn().getData();
	// @SuppressWarnings("unchecked")
	// List<String> items = (List<String>) tableColumn.getData();
	// int rowNum = 0;
	// for (String rowValue: items){
	// Matcher matcher = qaCheck.getPattern().matcher(rowValue);
	// if (matcher.find()){
	// qaCheck.getIssue().setStatus(Status.UNRESOLVED);
	// qaCheck.getIssue().setLocation("Row: "+rowNum);
	// }
	// rowNum++;
	// }
	// // thing = tableColumn.getData()
	// // Matcher matcher =
	//
	// }
	public Pattern getPattern() {
		return pattern;
	}

	public static List<QACheck> getGeneralQAChecks() {
		List<QACheck> qaCheckPack = new ArrayList<QACheck>();

		Pattern p1 = Pattern.compile("^\"([^\"]*)\"$");
		Issue i1 = new Issue("Bookend quotes", "The text is surrounded by apparently superfluous double quote marks.",
				"Remove these quote marks.  You may also use the auto-clean function.", true);
		qaCheckPack.add(new QACheck(i1.getDescription(), p1, i1));

		Pattern p2 = Pattern.compile("^\\s");

		Issue i2 = new Issue(
				"Leading space(s)",
				"Preceeding text, at least one white space character occurs.  This may be a non-printing character.",
				"If you can not see and remove the leading space, search for non-ASCCI characters.  You may also use the auto-clean function.",
				true);
		qaCheckPack.add(new QACheck(i2.getDescription(), p2, i2));

		Pattern p3 = Pattern.compile("\\s$");
		Issue i3 = new Issue(
				"Trailing space(s)",
				"Following text, at least one white space character occurs.  This may be a non-printing character.",
				"If you can not see and remove the leading space, search for non-ASCCI characters.  You may also use the auto-clean function.",
				true);
		qaCheckPack.add(new QACheck(i3.getDescription(), p3, i3));

		for (QACheck qaCheck:qaCheckPack){
			System.out.println("qaCheck.getName()"+qaCheck.getName());
		}
		return qaCheckPack;
	}

	public static void checkColumn(TableColumn column) {
		Object data = column.getData();
		System.out.println("data: " + data);
		System.out.println("data.getClass(): " + data.getClass());
	}

	public static List<QACheck> getQAChecks(CsvTableViewerColumnType type) {
		return getGeneralQAChecks();
	}

}
