package gov.epa.nrmrl.std.lca.ht.dataModels;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVColCheck;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.model.Issue;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class LCADataType {
	protected String header;
	protected String parentGroup;
	protected boolean requiredByParentGroup;
	protected boolean onePerParentGroup;
	protected List<QACheck> qaChecks;
	protected int csvColNumber = -1;
	protected CSVColCheck csvColCheck;

	public int getCsvColNumber() {
		return csvColNumber;
	}

	public void setCsvColNumber(int csvColNumber) {
		this.csvColNumber = csvColNumber;
	}

	public CSVColCheck getCsvColCheck() {
		return csvColCheck;
	}

	public void setCsvColCheck(CSVColCheck csvColCheck) {
		this.csvColCheck = csvColCheck;
	}

	protected LCADataType(String displayString, String parentGroup, boolean requiredByParentGroup, boolean onePerParentGroup) {
		super();
		this.header = displayString;
		this.parentGroup = parentGroup;
		this.requiredByParentGroup = requiredByParentGroup;
		this.onePerParentGroup = onePerParentGroup;
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

	public boolean isRequiredByParentGroup() {
		return requiredByParentGroup;
	}

	public void setRequiredByParentGroup(boolean required) {
		this.requiredByParentGroup = required;
	}

	public boolean isOnePerParentGroup() {
		return onePerParentGroup;
	}

	public void setOnePerParentGroup(boolean unique) {
		this.onePerParentGroup = unique;
	}

//	public static List<QACheck> initializeQAChecks() {
//		List<QACheck> qaChecks = new ArrayList<QACheck>();
//
//		Pattern p1 = Pattern.compile("^\"([^\"]*)\"$");
//		Issue i1 = new Issue("Bookend quotes", "The text is surrounded by apparently superfluous double quote marks.",
//				"Remove these quote marks.  You may also use the auto-clean function.", true);
//		qaChecks.add(new QACheck(i1.getDescription(), p1, i1));
//
//		Pattern p2 = Pattern.compile("^\\s");
//
//		Issue i2 = new Issue("Leading space(s)", "Preceeding text, at least one white space character occurs.  This may be a non-printing character.",
//				"If you can not see and remove the leading space, search for non-ASCCI characters.  You may also use the auto-clean function.", true);
//		qaChecks.add(new QACheck(i2.getDescription(), p2, i2));
//
//		Pattern p3 = Pattern.compile("\\s$");
//		Issue i3 = new Issue("Trailing space(s)", "Following text, at least one white space character occurs.  This may be a non-printing character.",
//				"If you can not see and remove the leading space, search for non-ASCCI characters.  You may also use the auto-clean function.", true);
//		qaChecks.add(new QACheck(i3.getDescription(), p3, i3));
//
//		for (QACheck qaCheck : qaChecks) {
//			System.out.println("qaCheck.getName()" + qaCheck.getName());
//		}
//		return qaChecks;
//	}

//	public List<QACheck> getQaChecks() {
//		return initializeQAChecks();
//	}

	public void setQaChecks(List<QACheck> qaChecks) {
		this.qaChecks = qaChecks;
	}
}
