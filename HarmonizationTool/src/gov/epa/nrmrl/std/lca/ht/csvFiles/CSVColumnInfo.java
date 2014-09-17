package gov.epa.nrmrl.std.lca.ht.csvFiles;

import gov.epa.nrmrl.std.lca.ht.dataModels.QACheck;
import gov.epa.nrmrl.std.lca.ht.jenaTDB.Issue;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class CSVColumnInfo {

	private String headerString;
	private boolean isRequired;
	private boolean isUnique;
	private boolean leftJustified = true;
	private List<QACheck> checkLists;
	private List<Issue> issues = new ArrayList<Issue>();
	protected Resource rdfClass;
	private Property tdbProperty;
	private RDFDatatype rdfDatatype;
//	private int csvTableViewColumnNumber;

	// THE ASSUMPTION HERE IS:
	// 1) EACH CSVColumnInfo REPRESENTS AN ATTRIBUTE OF A THING WHOSE CLASS IS
	// rdfClass;
	// 2) THAT THING HAS AN ATTRIBUTE DEFINED BY THE FIELD VALUE
	// SO THE FOLLOW TWO TRIPES ARE TRUE:
	// a) ?thing a rdfClass .
	// b) ?thing tdbProperty [fieldValue (CONVERTED TO A TYPED LITERAL OF TYPE
	// rdfDatatype)]

	public CSVColumnInfo() {

	}

	// public CSVColumnInfo(String headerString, boolean isRequired, boolean
	// isUnique, boolean
	// leftJustified,
	// List<QACheck> checkLists, List<Issue> issues, LCADataField_DEPRECATED lcaDataField)
	// {
	// public CSVColumnInfo(String headerString, boolean isRequired, boolean
	// isUnique, boolean leftJustified, List<QACheck> checkLists, List<Issue>
	// issues) {
	// super();
	// this.headerString = headerString;
	// this.isRequired = isRequired;
	// this.isUnique = isUnique;
	// this.leftJustified = leftJustified;
	// this.checkLists = checkLists;
	// this.issues = issues;
	// // this.setLcaDataField(lcaDataField);
	// }

	public CSVColumnInfo(String headerString) {
		this.headerString = headerString;
		// this.isRequired = false;
		// this.isUnique = false;
		// this.checkLists = null;
	}

	// public CSVColumnInfo(String headerString, boolean isRequired, boolean
	// isUnique, List<QACheck> checkLists) {
	// this.headerString = headerString;
	// this.isRequired = isRequired;
	// this.isUnique = isUnique;
	// this.checkLists = checkLists;
	// }

	public CSVColumnInfo(CSVColumnInfo menuCSVColumnInfo) {
		// CSVColumnInfo newCSVColumnInfo = new CSVColumnInfo();
		this.headerString = menuCSVColumnInfo.getHeaderString();
		this.isRequired = menuCSVColumnInfo.isRequired();
		this.isUnique = menuCSVColumnInfo.isUnique();
		this.leftJustified = menuCSVColumnInfo.isLeftJustified();
		this.rdfClass = menuCSVColumnInfo.getRDFClass();
		this.tdbProperty = menuCSVColumnInfo.getTdbProperty();
		this.rdfDatatype = menuCSVColumnInfo.getRdfDatatype();
		this.checkLists = menuCSVColumnInfo.copyCheckLists();
		// BUT DON'T COPY ISSUES
		// this.issues = menuCSVColumnInfo.copyIssues();
		// INITIALIZE INSTEAD
		this.issues = new ArrayList<Issue>();
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

	public List<QACheck> copyCheckLists() {
		List<QACheck> results = new ArrayList<QACheck>();
		for (QACheck qaCheck : checkLists) {
			QACheck newQACheck = new QACheck(qaCheck);
			results.add(newQACheck);
		}
		return results;
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

	public RDFDatatype getRdfDatatype() {
		return rdfDatatype;
	}

	public void setRdfDatatype(RDFDatatype rdfDatatype) {
		this.rdfDatatype = rdfDatatype;
	}

//	public int getCsvTableViewColumnNumber() {
//		return csvTableViewColumnNumber;
//	}
//
//	public void setCsvTableViewColumnNumber(int csvTableViewColumnNumber) {
//		this.csvTableViewColumnNumber = csvTableViewColumnNumber;
//	}

	public boolean sameRDFClassAs(CSVColumnInfo toCompare) {
		if (toCompare == null) {
			return false;
		}
		if (toCompare.getRDFClass().equals(rdfClass)) {
			return true;
		}
		return false;
	}

	public boolean sameRDFClassAs(Resource rdfClass2) {
		if (rdfClass2 == null) {
			return false;
		}
		if (rdfClass2.equals(rdfClass)) {
			return true;
		}
		return false;
	}

	// public LCADataField_DEPRECATED getLcaDataField() {
	// return lcaDataField;
	// }
	//
	// public void setLcaDataField(LCADataField_DEPRECATED lcaDataField) {
	// this.lcaDataField = lcaDataField;
	// }
}
