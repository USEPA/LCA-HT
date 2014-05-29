package harmonizationtool.model;

public class Issue {
	private String description;
	private String explanation;
	private String suggestion;
	private int rowNumber;
	private int colNumber;
	private int characterPosition;
	private Status status = null;
	private boolean autofix;

	public Issue(String description, String explanation, String suggestion) {
		this.setDescription(description);
		this.setExplanation(explanation);
		this.setSuggestion(suggestion);
	}

	public Issue(String description, String explanation, String suggestion, boolean autofix) {
		this.setDescription(description);
		this.setExplanation(explanation);
		this.setSuggestion(suggestion);
		this.setAutofix(autofix);
	}

	public void resolveIssue() {
		if (status == Status.UNRESOLVED) {
			status = Status.RESOLVED;
		}
	}

	public int getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(int rowNumber) {
		this.rowNumber = rowNumber;
	}

	public int getColNumber() {
		return colNumber;
	}

	public void setColNumber(int colNumber) {
		this.colNumber = colNumber;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSuggestion() {
		return suggestion;
	}

	public void setSuggestion(String suggestion) {
		this.suggestion = suggestion;
	}

	public boolean isAutofix() {
		return autofix;
	}

	public void setAutofix(boolean autofix) {
		this.autofix = autofix;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getExplanation() {
		return explanation;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

	public int getCharacterPosition() {
		return characterPosition;
	}

	public void setCharacterPosition(int characterPosition) {
		this.characterPosition = characterPosition;
	}
}
