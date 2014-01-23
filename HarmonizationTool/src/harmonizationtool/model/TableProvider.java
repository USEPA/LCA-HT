package harmonizationtool.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class TableProvider {
	private List<String> headerNames = null;
	private List<DataRow> data = new ArrayList<DataRow>();

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

	public void setHeaderNames(List<String> columnNames) {
		if (headerNames == null) {
			headerNames = new ArrayList<String>();
		} else {
			headerNames.clear();
		}
		for (String name : columnNames) {
			headerNames.add(name);
		}
	}

	public static TableProvider create(ResultSetRewindable resultSetRewindable) {
		TableProvider tableProvider = new TableProvider();
		resultSetRewindable.reset();
		tableProvider.setHeaderNames(resultSetRewindable.getResultVars());
		for (; resultSetRewindable.hasNext();) {
			QuerySolution soln = resultSetRewindable.nextSolution();
			DataRow dataRow = new DataRow();
			tableProvider.addDataRow(dataRow);
			for (String header : tableProvider.getHeaderNames()) {
				try {
					RDFNode rdfNode = null;
					rdfNode = soln.get(header);
					if (rdfNode == null) {
						dataRow.add("");

					} else {
						dataRow.add(rdfNode.toString());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return tableProvider;
	}
}
