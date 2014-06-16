package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVColumnInfo;
import harmonizationtool.vocabulary.ECO;

import java.util.List;
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.Resource;

public class FlowContext {

	public static CSVColumnInfo[] getHeaderMenuObjects() {
		CSVColumnInfo[] results = new CSVColumnInfo[2];
		results[0] = new CSVColumnInfo("Context (primary)", true, true, getContextNameCheckList());
		results[1] = new CSVColumnInfo("Context (additional)", false, false, getContextNameCheckList());
		return results;
	}

	private static List<QACheck> getContextNameCheckList() {
		List<QACheck> qaChecks = QACheck.getGeneralQAChecks();

		String d1 = "Non-allowed characters";
		String e1 = "Various characters are not considered acceptible in standard chemical names.";
		String s1 = "Check your data";
		Pattern p1 = Pattern.compile("^([^\"]+)[\"]([^\"]+)$");
		String r1 = null;

		qaChecks.add(new QACheck(d1, e1, s1, p1, r1, false));
		return qaChecks;
	}
}
