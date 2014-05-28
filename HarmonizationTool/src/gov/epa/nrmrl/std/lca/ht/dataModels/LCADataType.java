package gov.epa.nrmrl.std.lca.ht.dataModels;

import com.hp.hpl.jena.rdf.model.Resource;

public class LCADataType {
	protected String displayString;
	protected String parentGroup;
	protected boolean required;
	protected boolean unique;
	protected Resource rdfClass;
	
	protected LCADataType(String displayString, String parentGroup, boolean required, boolean unique) {
		super();
		this.displayString = displayString;
		this.parentGroup = parentGroup;
		this.required = required;
		this.unique = unique;
//		if ()
	}
	public String getDisplayString() {
		return displayString;
	}
	public void setDisplayString(String displayString) {
		this.displayString = displayString;
	}
	public String getParentGroup() {
		return parentGroup;
	}
	public void setParentGroup(String parentGroup) {
		this.parentGroup = parentGroup;
	}
	public boolean isRequired() {
		return required;
	}
	public void setRequired(boolean required) {
		this.required = required;
	}
	public boolean isUnique() {
		return unique;
	}
	public void setUnique(boolean unique) {
		this.unique = unique;
	}
}
