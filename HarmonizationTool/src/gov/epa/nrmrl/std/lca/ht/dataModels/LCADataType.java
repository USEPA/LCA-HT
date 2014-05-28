package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class LCADataType {
	protected String header;
	protected String value;
	protected String parentGroup;
	protected boolean requiredByParentGroup;
	protected boolean onePerParentGroup;
	protected boolean isLiteral;
	protected Resource rdfClass;
	protected Property rdfProperty;


	protected LCADataType(String displayString, String parentGroup, boolean requiredByParentGroup, boolean onePerParentGroup) {
		this.header = displayString;
		this.parentGroup = parentGroup;
		this.requiredByParentGroup = requiredByParentGroup;
		this.onePerParentGroup = onePerParentGroup;
	}

	protected LCADataType(String displayString, String parentGroup, boolean requiredByParentGroup, boolean onePerParentGroup, Resource rdfClass) {
		super();
		this.header = displayString;
		this.parentGroup = parentGroup;
		this.requiredByParentGroup = requiredByParentGroup;
		this.onePerParentGroup = onePerParentGroup;
		if (ActiveTDB.model != null){
			if (!ActiveTDB.model.containsResource(rdfClass)){
				ActiveTDB.model.createResource(rdfClass);
//				ActiveTDB.model.add(rdfClass, arg1, arg2)
			}
		}
	}

	protected LCADataType(String displayString, String parentGroup, boolean requiredByParentGroup, boolean onePerParentGroup, boolean isLiteral, Property rdfProperty) {
		super();
		this.header = displayString;
		this.parentGroup = parentGroup;
		this.requiredByParentGroup = requiredByParentGroup;
		this.onePerParentGroup = onePerParentGroup;
		this.isLiteral = isLiteral;
		if (ActiveTDB.model != null){
			if (!ActiveTDB.model.containsResource(rdfProperty)){
				ActiveTDB.model.createResource(rdfProperty);
				// SLIGHT CONFUSION: IT LOOKS LIKE A Property CAN BE A Resource
			}
		}
	}

	
	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getParentGroup() {
		return parentGroup;
	}

	public void setParentGroup(String parentGroup) {
		this.parentGroup = parentGroup;
	}

	public boolean isRequired() {
		return requiredByParentGroup;
	}

	public void setRequired(boolean required) {
		this.requiredByParentGroup = required;
	}

	public boolean isUnique() {
		return onePerParentGroup;
	}

	public void setUnique(boolean unique) {
		this.onePerParentGroup = unique;
	}
}
