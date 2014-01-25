package harmonizationtool.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class TableProvider {
	private DataRow headerNames = new DataRow();
	private List<DataRow> data = new ArrayList<DataRow>();
	public static final String SUBROW_PREFIX = "sr";
	public static final String SUBROW_NAMEHEADER = "srName";

	public void addDataRow(DataRow dataRow) {
		data.add(dataRow);
	}

	public List<DataRow> getData() {
		return data;
	}

	public int getIndex(DataRow dataRow) {
		return data.indexOf(dataRow);
	}

	public DataRow getHeaderNames() {
		return headerNames;
	}

	public List<String> getHeaderNamesAsStrings() {
		List<String> headerNamesAsStrings = new ArrayList<String>();
		Iterator<String> iter = headerNames.getIterator();
		while (iter.hasNext()) {
			headerNamesAsStrings.add(iter.next());
		}
		return headerNamesAsStrings;
	}

	public void setHeaderNames(List<String> columnNames) {
		if (headerNames == null) {
			headerNames = new DataRow();
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
			Iterator<String> iterator = tableProvider.getHeaderNames().getIterator();
			while (iterator.hasNext()) {
				String header = iterator.next();
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

	private class TransformCell {
		public final int rowNum;
		public final String newHeader;

		public TransformCell(int rowNum, String newHeader) {
			this.rowNum = rowNum;
			this.newHeader = newHeader;
		}
	}

	public static TableProvider createTransform(ResultSetRewindable resultSetRewindable) {
		TableProvider tableProvider = new TableProvider();
		resultSetRewindable.reset();
		List<String> origHeaderNames = resultSetRewindable.getResultVars();
		String dataSetName = "DataSet";
		tableProvider.headerNames.add(dataSetName); // THE FIRST COLUMN IS DataSet
		Map<String, TransformCell> headerMap = new HashMap<String, TransformCell>();
		for (String origHeader : origHeaderNames) {
			int headerSplitPoint = origHeader.indexOf("_");
			if (headerSplitPoint == -1) {
				tableProvider.headerNames.add(origHeader); // NOT A subRowHeader. ADD IT
				TransformCell transformCell = tableProvider.new TransformCell(0, origHeader);
				headerMap.put(origHeader, transformCell);
			} else {
				String origHeaderPrefix = origHeader.substring(0, headerSplitPoint);
				String origHeaderField = origHeader.substring(headerSplitPoint + 1);
				if (origHeaderPrefix.matches("^" + SUBROW_PREFIX + "\\d+$")) {
					int subRowNum = Integer.parseInt(origHeaderPrefix.substring(SUBROW_PREFIX.length()));
					if (origHeaderField.equals(SUBROW_NAMEHEADER)) { // DataSet FIELD
						TransformCell transformCell = tableProvider.new TransformCell(subRowNum, dataSetName);
						headerMap.put(origHeader, transformCell);
						// headerMap.put(origHeader, dataSetName);
						// continue; // DON'T ADD IT AGAIN (SEE ABOVE)
					} else {
						TransformCell transformCell = tableProvider.new TransformCell(subRowNum, origHeaderField);
						headerMap.put(origHeader, transformCell);
						if (subRowNum == 1) {
							tableProvider.headerNames.add(origHeaderField);
						}
					}
				} else {
					tableProvider.headerNames.add(origHeader); // NOT A subRowHeader. ADD IT
					TransformCell transformCell = tableProvider.new TransformCell(0, origHeader);
					headerMap.put(origHeader, transformCell);
				}
			}
		}

		System.out.println(headerMap.toString());

		for (; resultSetRewindable.hasNext();) {
			QuerySolution soln = resultSetRewindable.nextSolution();
			// List<List<String>> twoDArray = new ArrayList<List<String>>();
			Map<Integer, DataRow> twoDArray = new HashMap<Integer, DataRow>();
			for (String origHeader : origHeaderNames) {
				TransformCell transformCell = headerMap.get(origHeader);
				int colNum = tableProvider.getHeaderNamesAsStrings().indexOf(transformCell.newHeader);
				int rowNum = transformCell.rowNum;
				DataRow dataRow = twoDArray.get(rowNum);
				if (dataRow == null) {
					twoDArray.put(rowNum, new DataRow());
					dataRow = twoDArray.get(rowNum);
				}
				while (dataRow.getSize() < colNum+1){ // FIXME - WHAT'S WRONG WITH MY LOGIC?
					dataRow.add("");
				}
				try {
					RDFNode rdfNode = soln.get(origHeader);
					if (rdfNode == null) {
						dataRow.set(colNum, "");
					} else {
						dataRow.set(colNum, rdfNode.toString());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// NOW TAKE NON EMPTY ITEMS FROM dataRow ZERO AND FILL THE COLUMN BELOW
			DataRow dataRowFillSource = twoDArray.get(0);
			for (int i=0;i<dataRowFillSource.getSize();i++){
				for (int j=1; j< twoDArray.size(); j++){
					DataRow dataRowJ = twoDArray.get(j);
					while (dataRowJ.getSize() < i+1){
						dataRowJ.add("");
					}
					String valTwoFillCol = twoDArray.get(0).get(i);
					if (!dataRowFillSource.get(i).equals("")){
						dataRowJ.set(i, valTwoFillCol);
						tableProvider.addDataRow(dataRowJ);
					}
				}
			}
		}
		return tableProvider;

		// throw new IllegalArgumentException("implement me");
	}

}
