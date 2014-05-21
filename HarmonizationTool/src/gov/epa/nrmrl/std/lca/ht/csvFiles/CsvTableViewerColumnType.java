package gov.epa.nrmrl.std.lca.ht.csvFiles;

public enum CsvTableViewerColumnType {
	FLOWABLE_NAME("Flowable Name", "Flowable", true, true),
	FLOWABLE_ALT_NAME("Flowable Synonym", "Flowable", true, false), CAS("CAS", "Flowable", false, true),
	// Conventional Alternative Synonym

	CONTEXT("Context (primary)", "Context", true, true), CONTEXT1("Context (additional)", "Context", false, false);

	// IMPACT_ASSESSMENT_METHOD_HDR,
	// IMPACT_CHARACTERIZATION_MODEL_HDR,
	// IMPACT_DIR_HDR,
	// IMPACT_CAT_HDR,
	// IMPACT_CAT_INDICATOR_HDR,
	// IMPACT_CAT_REF_UNIT_HDR,
	//
	// CHAR_FACTOR_HDR,
	// FLOW_UNIT_HDR,
	// FLOW_PROPERTY_HDR,
	//
	// IGNORE_HDR;

	private String displayString;
	public String getDisplayString() {
		return displayString;
	}

//	public void setDisplayString(String displayString) {
//		this.displayString = displayString;
//	}

	public String getParentGroup() {
		return parentGroup;
	}

//	public void setParentGroup(String parentGroup) {
//		this.parentGroup = parentGroup;
//	}

	public boolean isRequired() {
		return required;
	}

//	public void setRequired(boolean required) {
//		this.required = required;
//	}

	public boolean isUnique() {
		return unique;
	}

//	public void setUnique(boolean unique) {
//		this.unique = unique;
//	}

	private String parentGroup;
	private boolean required;
	private boolean unique;

	private CsvTableViewerColumnType(String displayString, String parentGroup, boolean required, boolean unique) {
		this.displayString = displayString;
		this.parentGroup = parentGroup;
		this.required = required;
		this.unique = unique;

	}

	public String[] getParentGroups() {
		String[] result = new String[2];
		result[0] = "Flowable";
		result[1] = "Context";
		return result;
	}

	public static boolean isUnique(String displayString) {
		for (CsvTableViewerColumnType type : CsvTableViewerColumnType.values()) {
			if (type.displayString.equals(displayString)) {
				return type.unique;
			}
		}
		return false;
	}

	public static boolean isRequired(String displayString) {
		for (CsvTableViewerColumnType type : CsvTableViewerColumnType.values()) {
			if (type.displayString.equals(displayString)) {
				return type.required;
			}
		}
		return false;
	}

	public static CsvTableViewerColumnType getTypeFromDisplayString(String displayString) {
		for (CsvTableViewerColumnType type : CsvTableViewerColumnType.values()) {
			if (type.displayString.equals(displayString))
				return type;
		}
		return null;

	}

	// public CsvTableViewerColumnType[] getCsvTableColumnTypes(){
	// return CsvTableViewerColumnType.values();
	// }
}
