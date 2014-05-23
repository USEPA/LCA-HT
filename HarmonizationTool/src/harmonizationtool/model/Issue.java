package harmonizationtool.model;

public class Issue {
	private static String description;
	private static String location;
	private static int rowNumber;
	public int getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(int rowNumber) {
		Issue.rowNumber = rowNumber;
	}

	public int getColNumber() {
		return colNumber;
	}

	public void setColNumber(int colNumber) {
		Issue.colNumber = colNumber;
	}

	private static int colNumber;
	private static String details;
	private static String suggestion;
	private static Status status = null;
	private static boolean autofix;
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		Issue.description = description;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		Issue.details = details;
	}

	public String getSuggestion() {
		return suggestion;
	}

	public void setSuggestion(String suggestion) {
		Issue.suggestion = suggestion;
	}

	public boolean isAutofix() {
		return autofix;
	}

	public void setAutofix(boolean autofix) {
		Issue.autofix = autofix;
	}

	public String getLocation() {
		return location;
	}

	public Status getStatus() {
		return status;
	}


	public void resolveIssue() {
		if (status == Status.UNRESOLVED) {
			status = Status.RESOLVED;
		}
	}

	public Issue(String issue, String details, String suggestion) {
		this.description = issue;
		this.details = details;
		this.suggestion = suggestion;
	}

	public Issue(String issue, String details, String suggestion, boolean autofix) {
		this.description = issue;
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
