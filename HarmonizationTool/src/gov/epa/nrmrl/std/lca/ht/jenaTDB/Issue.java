package gov.epa.nrmrl.std.lca.ht.jenaTDB;

import gov.epa.nrmrl.std.lca.ht.dataModels.QACheck;

public class Issue {
	private QACheck qaCheck;
	private int rowNumber;
	private int colNumber;
	private int characterPosition;
	private Status status = null;

	public Issue(QACheck qaCheck, int rowNumber, int colNumber, int characterPosition, Status status){
		this.qaCheck = qaCheck;
		this.rowNumber = rowNumber;
		this.colNumber = colNumber;
		this.characterPosition = characterPosition;
		this.status = status;
	}

	public QACheck getQaCheck() {
		return qaCheck;
	}

	public void setQaCheck(QACheck qaCheck) {
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
}
