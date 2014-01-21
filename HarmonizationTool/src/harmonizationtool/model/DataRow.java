package harmonizationtool.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataRow {
	private List<String> columnValues = new ArrayList<String>();

	public DataRow() {
	}
	
	@Override
	public String toString() {
		return "DataRow [columnValues=" + columnValues + "]";
	}

	public Iterator<String> getIterator(){
		return columnValues.iterator();
	}
	
	public int getSize(){
		return columnValues.size();
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
	
	public void set(int index, String string){
		columnValues.set(index, string);		
	}

	public String get(int i) {
		return columnValues.get(i);
	}

	public String join(String delimiter) {
		if (columnValues.size()<0){
			return "";
		}
		String joinedRow = columnValues.get(0);
		for (int i = 1;i<columnValues.size();i++){
			joinedRow+=delimiter+columnValues.get(i);
		}
		return joinedRow;
	}

	public void clear() {
		columnValues.clear();
	}
}
