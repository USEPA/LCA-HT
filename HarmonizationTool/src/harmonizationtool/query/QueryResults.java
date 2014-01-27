package harmonizationtool.query;

import harmonizationtool.model.DataRow;
import harmonizationtool.model.TableProvider;

public class QueryResults {
	private DataRow columnHeaders = null;
	private TableProvider tableProvider = null;

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
}
