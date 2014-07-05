package gov.epa.nrmrl.std.lca.ht.csvFiles;

import gov.epa.nrmrl.std.lca.ht.dataModels.QACheck;
import harmonizationtool.model.Issue;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class CSVColumnInfo {

	private String headerString;
	private boolean isRequired;
	private boolean isUnique;
	private boolean leftJustified = true;
	private List<QACheck> checkLists;
	private List<Issue> issues = new ArrayList<Issue>();
	private Resource rdfClass;
	private Property tdbProperty;

	// private LCADataField lcaDataField;

	public CSVColumnInfo() {

	}

	// public CSVColumnInfo(String headerString, boolean isRequired, boolean isUnique, boolean
	// leftJustified,
	// List<QACheck> checkLists, List<Issue> issues, LCADataField lcaDataField) {
	public CSVColumnInfo(String headerString, boolean isRequired, boolean isUnique, boolean leftJustified, List<QACheck> checkLists, List<Issue> issues) {
		super();
		this.headerString = headerString;
		this.isRequired = isRequired;
		this.isUnique = isUnique;
		this.leftJustified = leftJustified;
		this.checkLists = checkLists;
		this.issues = issues;
		// this.setLcaDataField(lcaDataField);
	}

	public CSVColumnInfo(String headerString) {
		this.headerString = headerString;
		this.isRequired = false;
		this.isUnique = false;
		this.checkLists = null;
	}

	public CSVColumnInfo(String headerString, boolean isRequired, boolean isUnique, List<QACheck> checkLists) {
		this.headerString = headerString;
		this.isRequired = isRequired;
		this.isUnique = isUnique;
		this.checkLists = checkLists;
	}

	public String getHeaderString() {
		return headerString;
	}

	public void setHeaderString(String headerString) {
		this.headerString = headerString;
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

	public List<QACheck> getCheckLists() {
		return checkLists;
	}

	public void setCheckLists(List<QACheck> checkLists) {
		this.checkLists = checkLists;
	}

	public void addQACheck(QACheck qaCheck) {
		this.checkLists.add(qaCheck);
	}

	// public Status getStatus() {
	// return status;
	// }
	//
	// public void setStatus(Status status) {
	// this.status = status;
	// }

	public List<Issue> getIssues() {
		return issues;
	}

	public void setIssues(List<Issue> issues) {
		this.issues = issues;
	}

	public void addIssue(Issue issue) {
		this.issues.add(issue);
	}

	public int getIssueCount() {
		return this.issues.size();
	}

	// public int getIndexInTable() {
	// return indexInTable;
	// }
	//
	// public void setIndexInTable(int indexInTable) {
	// this.indexInTable = indexInTable;
	// }

	public void clearIssues() {
		this.issues.clear();
	}

	public boolean isLeftJustified() {
		return leftJustified;
	}

	public void setLeftJustified(boolean leftJustified) {
		this.leftJustified = leftJustified;
	}

	public Resource getRDFClass() {
		return rdfClass;
	}

	public void setRDFClass(Resource rdfClass) {
		this.rdfClass = rdfClass;
	}

	public Property getTdbProperty() {
		return tdbProperty;
	}

	public void setTdbProperty(Property tdbProperty) {
		this.tdbProperty = tdbProperty;
	}

	// public LCADataField getLcaDataField() {
	// return lcaDataField;
	// }
	//
	// public void setLcaDataField(LCADataField lcaDataField) {
	// this.lcaDataField = lcaDataField;
	// }
}
