package gov.epa.nrmrl.std.lca.ht.dataModels;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.rdf.model.Property;

public class LCADataPropertyProvider {
	private String propertyName;               // e.g. "Flowable Name"
	private RDFDatatype rdfDatatype;           // e.g. XSDDatatype.XSDfloat
	private boolean isRequired = false;        // e.g. true
	private boolean isUnique = true;           // e.g. false
	private boolean leftJustified = true;      // e.g. true
	private List<QACheck> checkLists;          // A LIST OF WAYS OF CHECKING THIS COLUMN FOR VALIDITY
//	private List<Issue> issues = new ArrayList<Issue>();
	private Property tdbProperty;
//	private List<String> values = new ArrayList<String>();

	public LCADataPropertyProvider() {

	}

	public LCADataPropertyProvider(String propertyName) {
		this.propertyName = propertyName;
	}

	public LCADataPropertyProvider copyLCADataProperty(LCADataPropertyProvider lcaDataProperty) {
		LCADataPropertyProvider result = new LCADataPropertyProvider(lcaDataProperty.getPropertyName());
		// CSVColumnInfo newCSVColumnInfo = new CSVColumnInfo();
		result.propertyName = lcaDataProperty.getPropertyName();
		result.rdfDatatype = lcaDataProperty.getRdfDatatype();

		result.isRequired = lcaDataProperty.isRequired();
		result.isUnique = lcaDataProperty.isUnique();
		result.leftJustified = lcaDataProperty.isLeftJustified();
		result.tdbProperty = lcaDataProperty.getTDBProperty();
		result.checkLists = lcaDataProperty.copyCheckLists();
		// BUT DON'T COPY ISSUES
		// this.issues = menuCSVColumnInfo.copyIssues();
		// INITIALIZE INSTEAD
//		result.issues = new ArrayList<Issue>();
//		propertyValueJavaClass = Integer.getClass();
		return result;
	}

	// public LCADataPropertyProvider(LCADataPropertyProvider menuCSVColumnInfo) {
	// // CSVColumnInfo newCSVColumnInfo = new CSVColumnInfo();
	// this.propertyName = menuCSVColumnInfo.getPropertyName();
	// this.isRequired = menuCSVColumnInfo.isRequired();
	// this.isUnique = menuCSVColumnInfo.isUnique();
	// this.leftJustified = menuCSVColumnInfo.isLeftJustified();
	// this.rdfClass = menuCSVColumnInfo.getRDFClass();
	// this.tdbProperty = menuCSVColumnInfo.getTdbProperty();
	// this.rdfDatatype = menuCSVColumnInfo.getRdfDatatype();
	// this.checkLists = menuCSVColumnInfo.copyCheckLists();
	// // BUT DON'T COPY ISSUES
	// // this.issues = menuCSVColumnInfo.copyIssues();
	// // INITIALIZE INSTEAD
	// this.issues = new ArrayList<Issue>();
	// }

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

//	public Object getPropertyValueJavaType() {
//		return propertyValueJavaType;
//	}
//
//	public void setPropertyValueJavaType(Object propertyValueJavaType) {
//		this.propertyValueJavaType = propertyValueJavaType;
//	}

	public RDFDatatype getRdfDatatype() {
		return rdfDatatype;
	}

	public void setRDFDatatype(RDFDatatype rdfDatatype) {
		this.rdfDatatype = rdfDatatype;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public void setRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}

	public boolean isUnique() {
		return isUnique;
	}

	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}

	public boolean isLeftJustified() {
		return leftJustified;
	}

	public void setLeftJustified(boolean leftJustified) {
		this.leftJustified = leftJustified;
	}

	public List<QACheck> getCheckLists() {
		return checkLists;
	}

	public void setCheckLists(List<QACheck> checkLists) {
		this.checkLists = checkLists;
	}

	public List<QACheck> copyCheckLists() {
		List<QACheck> results = new ArrayList<QACheck>();
		for (QACheck qaCheck : checkLists) {
			QACheck newQACheck = new QACheck(qaCheck);
			results.add(newQACheck);
		}
		return results;
	}

	public void addQACheck(QACheck qaCheck) {
		this.checkLists.add(qaCheck);
	}

//	public List<Issue> getIssues() {
//		return issues;
//	}
//
//	public void setIssues(List<Issue> issues) {
//		this.issues = issues;
//	}
//
//	public void addIssue(Issue issue) {
//		this.issues.add(issue);
//	}
//
//	public int getIssueCount() {
//		return this.issues.size();
//	}
//
//	public void clearIssues() {
//		this.issues.clear();
//	}

	public Property getTDBProperty() {
		return tdbProperty;
	}

	public void setTDBProperty(Property tdbProperty) {
		this.tdbProperty = tdbProperty;
	}
}
