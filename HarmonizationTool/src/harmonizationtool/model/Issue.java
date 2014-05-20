package harmonizationtool.model;

public class Issue {
	private static String issue;
	private static String location;
	private static String details;
	private static String suggestion;
	private static Status status = null;
	private static boolean autofix;

	public void resolveIssue() {
		if (status == Status.UNRESOLVED) {
			status = Status.RESOLVED;
		}
	}

	public Issue(String issue, String details, String suggestion) {
		this.issue = issue;
		this.details = details;
		this.suggestion = suggestion;
	}

	public Issue(String issue, String details, String suggestion, boolean autofix) {
		this.issue = issue;
		this.details = details;
		this.suggestion = suggestion;
		this.autofix = autofix;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
