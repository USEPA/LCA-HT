package gov.epa.nrmrl.std.lca.ht.csvFiles;

import java.util.Date;

import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataType;
import gov.epa.nrmrl.std.lca.ht.dataModels.QACheck;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/** This is a copy of the TableViewerColumn class from org.eclipse.jface.viewers with the added "type" variable
 * 
 * @author transue
 *
 */

public final class CsvTableViewerColumn extends ViewerColumn {
	private CsvTableViewerColumnType type = null;
	private CSVColCheck csvColCheck = null;
	private TableColumn column;
	private int columnNumber;
	private LCADataType assignedLCADataType = null;

	public CsvTableViewerColumn(TableViewer viewer, int style) {
		this(viewer, style, -1);
	}

	public CsvTableViewerColumn(TableViewer viewer, int style, int index) {
		this(viewer, createColumn(viewer.getTable(), style, index));
	}

	public CSVColCheck getCsvColCheck() {
		return csvColCheck;
	}

	public void setCsvColCheck(CSVColCheck csvColCheck) {
		this.csvColCheck = csvColCheck;
	}

	public void setType(CsvTableViewerColumnType type) {
		this.type = type;
	}

	public void setColumn(TableColumn column) {
		this.column = column;
	}

	public CsvTableViewerColumn(TableViewer viewer, TableColumn column) {
		super(viewer, column);
		this.column = column;
	}
	
	private static TableColumn createColumn(Table table, int style, int index) {
		if (index >= 0) {
			return new TableColumn(table, style, index);
		}

		return new TableColumn(table, style);
	}

	/**
	 * @return the underlying SWT table column
	 */
	public TableColumn getColumn() {
		return column;
	}

	public CsvTableViewerColumnType getType() {
		return type;
	}

	public void performStandaredChecks() {
		if (csvColCheck == null){
			csvColCheck = new CSVColCheck();
			csvColCheck.setLastChecked(new Date());
//			csvColCheck.setColumn(column);
			csvColCheck.setIssues(null);
		}
		System.out.println("csvColCheck: "+csvColCheck);

		System.out.println("getColumn().getData(): "+getColumn().getData());
//		QACheck.checkColumn();
	}

	public int getColumnNumber() {
		return columnNumber;
	}

	public void setColumnNumber(int columnNumber) {
		this.columnNumber = columnNumber;
	}

	public LCADataType getAssignedLCADataType() {
		return assignedLCADataType;
	}

	public void setAssignedLCADataType(LCADataType assignedLCADataType) {
		this.assignedLCADataType = assignedLCADataType;
	}
	
}
//public final class CsvTableViewerColumn extends ViewerColumn {
//	private TableColumn column;
//	private int colNumber;
//	protected CsvTableViewerColumn(TableViewer viewer, TableColumn column) {
//		super(viewer, column);
//		this.column = column;
//	}
//	protected CsvTableViewerColumn(TableViewer viewer, Widget columnOwner, int colNumber) {
//		super(viewer, columnOwner);
//		this.colNumber = colNumber;
//	}
//
//	public static CsvTableViewerColumnType type;


//}
