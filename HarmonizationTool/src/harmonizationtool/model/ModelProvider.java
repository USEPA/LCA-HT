package harmonizationtool.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelProvider {
	private List<DataRow> data = new ArrayList<DataRow>();
	private List<String> headerNames = null;

	public void addDataRow(DataRow dataRow) {
		data.add(dataRow);
	}

	public List<DataRow> getData() {
		return data;
	}

	public int getIndex(DataRow dataRow) {
		return data.indexOf(dataRow);
	}

	public List<String> getHeaderNames() {
		return headerNames;
	}

	public void setColumnNames(List<String> columnNames) {
		if (headerNames == null) {
			headerNames = new ArrayList<String>();
		} else {
			headerNames.clear();
		}
		for (String name : columnNames) {
			headerNames.add(name);
		}
	}

}
