package harmonizationtool.edit;

import java.util.List;

import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;

import org.eclipse.jface.viewers.CellEditor;
//import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

public class CSVEdittingSupport extends EditingSupport{

	private TableViewer viewer;
	private int colIndex;

	public CSVEdittingSupport(TableViewer viewer, int colIndex) {
		super(viewer);
		this.viewer = viewer;
		this.colIndex=colIndex;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return new TextCellEditor(viewer.getTable());
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		System.out.println(element.getClass().getName());
		DataRow dataRow  = (DataRow)element;
		String value = dataRow.getColumnValues().get(colIndex);
		System.out.println("value="+value+" colIndex= "+colIndex);
		return value;
	}

	@Override
	protected void setValue(Object element, Object value) {
		String valStr = (String)value;
		DataRow dataRow  = (DataRow)element;
		List<String> columnValues = dataRow.getColumnValues();
		columnValues.set(colIndex, valStr);
		viewer.update(element, null);
	}

}
