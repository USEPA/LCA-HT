package gov.epa.nrmrl.std.lca.ht.csvFiles;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/** This is a copy of the TableViewerColumn class from org.eclipse.jface.viewers with the added "type" variable
 * 
 * @author transue
 *
 */
public final class LcaCsvTableColumn extends ViewerColumn {
	private LcaCsvTableColumnType type = null;
	private TableColumn column;

	/**
	 * Creates a new viewer column for the given {@link TableViewer} on a new
	 * {@link TableColumn} with the given style bits. The column is added at the
	 * end of the list of columns.
	 * 
	 * @param viewer
	 *            the table viewer to which this column belongs
	 * @param style
	 *            the style used to create the column, for applicable style bits
	 *            see {@link TableColumn}
	 * @see TableColumn#TableColumn(Table, int)
	 */
	public LcaCsvTableColumn(TableViewer viewer, int style) {
		this(viewer, style, -1);
	}

	/**
	 * Creates a new viewer column for the given {@link TableViewer} on a new
	 * {@link TableColumn} with the given style bits. The column is inserted at
	 * the given index into the list of columns.
	 * 
	 * @param viewer
	 *            the table viewer to which this column belongs
	 * @param style
	 *            the style used to create the column, for applicable style bits
	 *            see {@link TableColumn}
	 * @param index
	 *            the index at which to place the newly created column
	 * @see TableColumn#TableColumn(Table, int, int)
	 */
	public LcaCsvTableColumn(TableViewer viewer, int style, int index) {
		this(viewer, createColumn(viewer.getTable(), style, index));
	}

	/**
	 * Creates a new viewer column for the given {@link TableViewer} on the given
	 * {@link TableColumn}.
	 * 
	 * @param viewer
	 *            the table viewer to which this column belongs
	 * @param column
	 *            the underlying table column
	 */
	public LcaCsvTableColumn(TableViewer viewer, TableColumn column) {
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

	public LcaCsvTableColumnType getType() {
		return type;
	}
	
}
//public final class LcaCsvTableColumn extends ViewerColumn {
//	private TableColumn column;
//	private int colNumber;
//	protected LcaCsvTableColumn(TableViewer viewer, TableColumn column) {
//		super(viewer, column);
//		this.column = column;
//	}
//	protected LcaCsvTableColumn(TableViewer viewer, Widget columnOwner, int colNumber) {
//		super(viewer, columnOwner);
//		this.colNumber = colNumber;
//	}
//
//	public static LcaCsvTableColumnType type;


//}
