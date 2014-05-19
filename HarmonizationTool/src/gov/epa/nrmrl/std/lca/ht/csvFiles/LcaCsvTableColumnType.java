package gov.epa.nrmrl.std.lca.ht.csvFiles;

public enum LcaCsvTableColumnType {
	FLOWABLE_NAME ("Flowable Name","Flowable",true, true),
	FLOWABLE_ALT_NAME ("Alt Flowable Name","Flowable",true, false),
	CAS("CAS#","Flowable",false, true),

	CONTEXT("Context (primary)","Context",true, true),
	CONTEXT1("Context (additional)","Context",false, false);

//	IMPACT_ASSESSMENT_METHOD_HDR,
//	IMPACT_CHARACTERIZATION_MODEL_HDR,
//	IMPACT_DIR_HDR,
//	IMPACT_CAT_HDR,
//	IMPACT_CAT_INDICATOR_HDR,
//	IMPACT_CAT_REF_UNIT_HDR,
//
//	CHAR_FACTOR_HDR,
//	FLOW_UNIT_HDR,
//	FLOW_PROPERTY_HDR,
//
//	IGNORE_HDR;
	
	public String displayString;
	public String parentGroup;
	public boolean required;
	public boolean unique;

	private LcaCsvTableColumnType(String displayString, String parentGroup, boolean required, boolean unique) {
		this.displayString = displayString;
		this.parentGroup = parentGroup;
		this.required = required;
		this.unique = unique;

	}
	
	public String[] getParentGroups(){
		String[] result = new String[2];
		result[0] = "Flowable";
		result[1] = "Context";
		return result;
	}
	public static boolean isUnique(String displayString){
		for (LcaCsvTableColumnType type: LcaCsvTableColumnType.values()){
			if (type.displayString.equals(displayString)){
				return type.unique;
			}
		}
		return false;
	}
//	public LcaCsvTableColumnType[] getCsvTableColumnTypes(){
//		return LcaCsvTableColumnType.values();
//	}
}
