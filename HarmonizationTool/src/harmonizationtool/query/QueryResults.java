package harmonizationtool.query;

import harmonizationtool.model.DataRow;
import harmonizationtool.model.ModelProvider;

public class QueryResults {
	private DataRow columnHeaders = null;
	private ModelProvider modelProvider = null;
	public DataRow getColumnHeaders() {
		return columnHeaders;
	}
	public void setColumnHeaders(DataRow columnHeaders) {
		this.columnHeaders = columnHeaders;
	}
	public ModelProvider getModelProvider() {
		return modelProvider;
	}
	public void setModelProvider(ModelProvider modelProvider) {
		this.modelProvider = modelProvider;
	}

}
