package gov.epa.nrmrl.std.lca.ht.dataModels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class TableProvider {
	private DataSourceProvider dataSourceProvider = null;
	private FileMD fileMD = null;

	private DataRow headerRow = new DataRow();
	private List<DataRow> data = new ArrayList<DataRow>();
	// TODO: JUNO CHECK ABOVE TO CONSIDER A BETTER WAY TO STORE -ALL- CSV DATA
	// CONFIRM COMPATIBILITY WITH TableViewer.setInput
	private int lastChecked;
	private int lastUpdated;
	private List<Integer> uniqueFlowableRowNumbers = new ArrayList<Integer>();
	private List<Integer> uniqueFlowContexRowNumbers  = new ArrayList<Integer>();
	private List<Integer> uniqueFlowPropertyRowNumbers  = new ArrayList<Integer>();
	// JUNO -- THE ABOVE ARE THE THREE THINGS YOU NEED TO POPULATE WITH THE (FIRST) ROW WITH A NEW THING


//	private CSVColumnInfo[] assignedCSVColumnInfo = null;  // <== FIXME COMMT OUT THIS LINE THEN FIX BROKEN CODE

	private LCADataPropertyProvider[] lcaDataProperties = null;

	// public TableProvider() {
	// // PSSH (PUT SOMETHING SMART HERE)
	// }

//	public CSVColumnInfo[] getAssignedCSVColumnInfo() {  // <== FIXME COMMT OUT THIS LINE THEN FIX BROKEN CODE
//		return assignedCSVColumnInfo;
//	}
//
//	public void setAssignedCSVColumnInfo(CSVColumnInfo[] assignedCSVColumnInfo) {  // <== FIXME COMMT OUT THIS LINE THEN FIX BROKEN CODE
//		this.assignedCSVColumnInfo = assignedCSVColumnInfo;
//	}

	public LCADataPropertyProvider getLCADataPropertyProvider(int colNumber) {
		if (lcaDataProperties == null){
			lcaDataProperties = new LCADataPropertyProvider[headerRow.getSize()];
		}
		return lcaDataProperties[colNumber];
	}
	
	public void setLCADataPropertyProvider(int colNumber, LCADataPropertyProvider lcaDataPropertyProvider) {
		if (lcaDataProperties == null){
			lcaDataProperties = new LCADataPropertyProvider[headerRow.getSize()];
		}
		lcaDataProperties[colNumber] = lcaDataPropertyProvider;
	}
	
//	public void resetAssignedCSVColumnInfo() {
//		int columnCount = headerRow.getSize();
//		if (columnCount > 0) {
//			this.assignedCSVColumnInfo = new CSVColumnInfo[columnCount];
//		}
//	}

	public void addDataRow(DataRow dataRow) {
		data.add(dataRow);
		dataRow.setRowNumber(data.indexOf(dataRow));
	}

	public int getColumnCount() {
		return data.get(0).getSize();
	}

	public String checkColumnCountConsistency() {
		String issues = "";
		int shorterRows = 0;
		int longerRows = 0;
		int colCount = getColumnCount();
		if (headerRow.getSize() < colCount) {
			issues = "Header row shorter than first data row.\\n";
		} else if (headerRow.getSize() > colCount) {
			issues = "Header row longer than first data row.\\n";
		}
		for (DataRow dataRow : data) {
			if (dataRow.getSize() > colCount) {
				longerRows++;
			} else if (dataRow.getSize() < colCount) {
				shorterRows++;
			}
		}
		if (shorterRows > 0) {
			issues += "Found " + shorterRows
					+ "rows shorter than the first row.\\n";
		}
		if (longerRows > 0) {
			issues += "Found " + longerRows
					+ "rows longer than the first row.\\n";
		}
		if (issues.equals("")) {
			return null;
		}
		return issues;
	}

	public List<DataRow> getData() {
		return data;
	}

	public int getIndex(DataRow dataRow) {
		return data.indexOf(dataRow);
	}

	public DataRow getHeaderRow() {
		return headerRow;
	}

	public List<String> getHeaderNamesAsStrings() {
		List<String> headerNamesAsStrings = new ArrayList<String>();
		// System.out.println("headerNames.getSize()=" + headerNames.getSize());
		Iterator<String> iter = headerRow.getIterator();
		while (iter.hasNext()) {
			headerNamesAsStrings.add(iter.next());
		}
		// System.out.println("returning headerNames.getSize()=" +
		// headerNames.getSize());
		return headerNamesAsStrings;
	}

	public void setHeaderNames(List<String> columnNames) {
		assert columnNames != null : "columnNames cannot be null";
		assert columnNames.size() != 0 : "columnNames cannot be empty";
		if (headerRow == null) {
			headerRow = new DataRow();
		} else {
			headerRow.clear();
		}
		for (String name : columnNames) {
			headerRow.add(name);
			headerRow.setRowNumber(-1);
		}
	}

	public static TableProvider create(ResultSetRewindable resultSetRewindable) {
		TableProvider tableProvider = new TableProvider();
		resultSetRewindable.reset();
		tableProvider.setHeaderNames(resultSetRewindable.getResultVars());
		while (resultSetRewindable.hasNext()) {
			QuerySolution soln = resultSetRewindable.nextSolution();
			DataRow dataRow = new DataRow();
			tableProvider.addDataRow(dataRow);
			Iterator<String> iterator = tableProvider.getHeaderRow()
					.getIterator();
			while (iterator.hasNext()) {
				String header = iterator.next();
				try {
					RDFNode rdfNode = null;
					rdfNode = soln.get(header);
					if (rdfNode == null) {
						dataRow.add("");

					} else {
						dataRow.add(rdfNode.toString());
						System.out.println("Resource string is "
								+ rdfNode.toString());
						System.out.println("Type of RDFNode = "
								+ RDFNode.class.getName());
						// System.out.println("  soln.getResource(header) =" +
						// soln.getResource(header));
						System.out.println("  soln.get(header)  = " + rdfNode);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return tableProvider;
	}

	public int getLastChecked() {
		return lastChecked;
	}

	public void setLastChecked(int lastChecked) {
		this.lastChecked = lastChecked;
	}

	public int getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(int lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public DataSourceProvider getDataSourceProvider() {
		return dataSourceProvider;
	}

	public void setDataSourceProvider(DataSourceProvider dataSourceProvider) {
		this.dataSourceProvider = dataSourceProvider;
	}

	public FileMD getFileMD() {
		return fileMD;
	}

	public void setFileMD(FileMD fileMD) {
		this.fileMD = fileMD;
	}

	public LCADataPropertyProvider[] getLcaDataProperties() {
		if (lcaDataProperties == null){
			lcaDataProperties = new LCADataPropertyProvider[headerRow.getSize()];
		}
		return lcaDataProperties;
	}

	public void setLcaDataProperties(LCADataPropertyProvider[] lcaDataProperties) {
		this.lcaDataProperties = lcaDataProperties;
	}

	public List<Integer> getUniqueFlowableRowNumbers() {
		return uniqueFlowableRowNumbers;
	}

	public void setUniqueFlowableRowNumbers(List<Integer> uniqueFlowableRowNumbers) {
		this.uniqueFlowableRowNumbers = uniqueFlowableRowNumbers;
	}

	public List<Integer> getUniqueFlowContexRowNumbers() {
		return uniqueFlowContexRowNumbers;
	}

	public void setUniqueFlowContexRowNumbers(
			List<Integer> uniqueFlowContexRowNumbers) {
		this.uniqueFlowContexRowNumbers = uniqueFlowContexRowNumbers;
	}

	public List<Integer> getUniqueFlowPropertyRowNumbers() {
		return uniqueFlowPropertyRowNumbers;
	}

	public void setUniqueFlowPropertyRowNumbers(
			List<Integer> uniqueFlowPropertyRowNumbers) {
		this.uniqueFlowPropertyRowNumbers = uniqueFlowPropertyRowNumbers;
	}

	// private class TransformCell {
	// public final int rowNum;
	// public final String newHeader;
	//
	// public TransformCell(int rowNum, String newHeader) {
	// this.rowNum = rowNum;
	// this.newHeader = newHeader;
	// }
	// }

}
