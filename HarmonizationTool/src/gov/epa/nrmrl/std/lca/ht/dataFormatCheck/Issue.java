package gov.epa.nrmrl.std.lca.ht.dataFormatCheck;

@SuppressWarnings("rawtypes")
/**
 * An Issue is created each time a field fails a FormatCheck. The Status of each
 * issue may be changed following user action.
 * 
 * @author Tom Transue
 * 
 */
public class Issue implements Comparable {
	private FormatCheck qaCheck;
	private int rowNumber;
	private int colNumber;
	private int characterPosition;
	private Status status = null;

	public Issue(FormatCheck qaCheck, int rowNumber, int colNumber,
			int characterPosition, Status status) {
		this.qaCheck = qaCheck;
		this.rowNumber = rowNumber;
		this.colNumber = colNumber;
		this.characterPosition = characterPosition;
		this.status = status;
	}

	public FormatCheck getQaCheck() {
		return qaCheck;
	}

	public void setQaCheck(FormatCheck qaCheck) {
		this.qaCheck = qaCheck;
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

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public int getCharacterPosition() {
		return characterPosition;
	}

	public void setCharacterPosition(int characterPosition) {
		this.characterPosition = characterPosition;
	}

	@Override
	public int compareTo(Object o) {
		Issue issue = (Issue) o;
		int result = rowNumber - issue.getRowNumber();
		return result;
	}
}
