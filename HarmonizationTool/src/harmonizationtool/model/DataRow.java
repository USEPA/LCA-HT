package harmonizationtool.model;

import java.util.ArrayList;
import java.util.List;

public class DataRow {
	private List<String> columnValues = new ArrayList<String>();

	public DataRow() {
	}
	
	@Override
	public String toString() {
		return "DataRow [columnValues=" + columnValues + "]";
	}

	public void setColumnValues(List<String> columnValues) {
		this.columnValues = columnValues;
	}

	public List<String> getColumnValues() {
		return columnValues;
	}
	
	public void add(String s){
		columnValues.add(s);
	}
}
