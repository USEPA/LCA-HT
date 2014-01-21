package harmonizationtool.query;

import harmonizationtool.model.DataRow;
import harmonizationtool.model.ITableProvider;
import harmonizationtool.model.TableProvider;

public class QueryResults {
	private DataRow columnHeaders = null;
	private TableProvider tableProvider = null;
	private ITableProvider iTableProvider = null;

	// private DataRow columnXformHeaders = null;

	public void setColumnHeaders(DataRow columnHeaders) {
		this.columnHeaders = columnHeaders;
	}

	public DataRow getColumnHeaders() {
		return columnHeaders;
	}

	public void setTableProvider(TableProvider tableProvider) {
		this.tableProvider = tableProvider;
	}

	public TableProvider getTableProvider() {
		return tableProvider;
	}

	public void setITableProvider(ITableProvider iTableProvider) {
		this.iTableProvider = iTableProvider;
	}

	public ITableProvider getITableProvider() {
		return iTableProvider;
	}

	// public void setColumnXformHeaders(DataRow columnXformHeaders) {
	// this.columnXformHeaders = columnXformHeaders;
	// }
	// public DataRow getColumnXformHeaders() {
	// return columnXformHeaders;
	// }

}
