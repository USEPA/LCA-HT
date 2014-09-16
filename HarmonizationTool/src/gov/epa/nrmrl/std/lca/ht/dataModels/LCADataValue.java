package gov.epa.nrmrl.std.lca.ht.dataModels;

public class LCADataValue {
	private LCADataPropertyProvider lcaDataPropertyProvider;
	private Object value;
	private String valueAsString;

	public LCADataPropertyProvider getLcaDataPropertyProvider() {
		return lcaDataPropertyProvider;
	}

	public void setLcaDataPropertyProvider(LCADataPropertyProvider lcaDataPropertyProvider) {
		this.lcaDataPropertyProvider = lcaDataPropertyProvider;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getValueAsString() {
		return valueAsString;
	}

	public void setValueAsString(String valueAsString) {
		this.valueAsString = valueAsString;
	}
}
