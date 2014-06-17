package gov.epa.nrmrl.std.lca.ht.dataModels;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class LCADataField {
	private Resource resourceSubject;
	private Property propertyPredicate;
	private String literalObjectType;
	private boolean functional;
	private boolean required;

	public LCADataField(Resource resourceSubject, Property propertyPredicate, String literalObjectType,
			boolean functional, boolean required) {
		super();
		this.resourceSubject = resourceSubject;
		this.propertyPredicate = propertyPredicate;
		this.literalObjectType = literalObjectType;
		this.functional = functional;
		this.required = required;
	}

	public LCADataField() {
		// NEW BLANK LCADataField
	}

	public Resource getResourceSubject() {
		return resourceSubject;
	}

	public void setResourceSubject(Resource resourceSubject) {
		this.resourceSubject = resourceSubject;
	}

	public Property getPropertyPredicate() {
		return propertyPredicate;
	}

	public void setPropertyPredicate(Property propertyPredicate) {
		this.propertyPredicate = propertyPredicate;
	}

	public String getLiteralObjectType() {
		return literalObjectType;
	}

	public void setLiteralObjectType(String literalObjectType) {
		this.literalObjectType = literalObjectType;
	}

	public boolean isFunctional() {
		return functional;
	}

	public void setFunctional(boolean functional) {
		this.functional = functional;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

}
