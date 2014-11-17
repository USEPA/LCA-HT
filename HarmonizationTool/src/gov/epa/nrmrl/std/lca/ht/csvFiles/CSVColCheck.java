package gov.epa.nrmrl.std.lca.ht.csvFiles;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.widgets.TableColumn;

public class CSVColCheck {
	private List<Issue> issues = new ArrayList<Issue>();
	// private TableColumn column;
	private Date lastChecked;

	public CSVColCheck() {
		this.lastChecked = new Date();
		System.out.println("new CSVColCheck, and it got its date");
	}

	public List<Issue> getIssues() {
		return issues;
	}

	public void setIssues(List<Issue> issues) {
		this.issues = issues;
	}
	public void addIssue(Issue issue){
		issues.add(issue);
	}

	// public TableColumn getColumn() {
	// return column;
	// }
	//
	// public void setColumn(TableColumn column) {
	// this.column = column;
	// }

	public Date getLastChecked() {
		return lastChecked;
	}

	public void setLastChecked(Date lastChecked) {
		this.lastChecked = lastChecked;
	}
}
