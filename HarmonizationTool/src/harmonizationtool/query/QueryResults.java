package harmonizationtool.query;

import harmonizationtool.model.DataRow;
import harmonizationtool.model.TableProvider;

public class QueryResults {
	private DataRow columnHeaders = null;
	private TableProvider tableProvider = null;
	public DataRow getColumnHeaders() {
		return columnHeaders;
	}
	public void setColumnHeaders(DataRow columnHeaders) {
		this.columnHeaders = columnHeaders;
	}
	public TableProvider getTableProvider() {
		return tableProvider;
	}
	public void setTableProvider(TableProvider tableProvider) {
		this.tableProvider = tableProvider;
	}

}
