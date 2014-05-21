package harmonizationtool.model;

public class Issue {
	private static String description;
	private static String location;
	private static String details;
	private static String suggestion;
	private static Status status = null;
	private static boolean autofix;
	
	public static String getDescription() {
		return description;
	}

	public static void setDescription(String description) {
		Issue.description = description;
	}

	public static String getDetails() {
		return details;
	}

	public static void setDetails(String details) {
		Issue.details = details;
	}

	public static String getSuggestion() {
		return suggestion;
	}

	public static void setSuggestion(String suggestion) {
		Issue.suggestion = suggestion;
	}

	public static boolean isAutofix() {
		return autofix;
	}

	public static void setAutofix(boolean autofix) {
		Issue.autofix = autofix;
	}

	public static String getLocation() {
		return location;
	}

	public static Status getStatus() {
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
