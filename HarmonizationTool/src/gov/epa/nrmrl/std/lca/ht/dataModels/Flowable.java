package gov.epa.nrmrl.std.lca.ht.dataModels;

import harmonizationtool.model.Issue;
import harmonizationtool.vocabulary.ECO;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.Resource;

public class Flowable {
	private String name = "";
	private String cas = null;
	private boolean isEmission = false;
	private boolean isResource = false;
	private List<String> altNames = null;
	private String formula = null;
	private String SMILES = null;
	private Resource rdfClass = ECO.Flowable;
	
	private Pattern acceptableCASFormat = Pattern.compile("^\\d{2,}-\\d\\d-\\d$|^\\d{5,}$");

	public List<QACheck> getQaChecks() {
		List<QACheck> allChecks = new ArrayList<QACheck>();

		Pattern p1 = acceptableCASFormat;
		Issue i1 = new Issue("Non-standard CAS format", "CAS numbers may only have all digits, or digits with \"-\" signs 4th and 2nd from the end .",
				"Parse digits.  To parse the numeric components, use the auto-clean function.", true);
		allChecks.add(new QACheck(i1.getDescription(), p1, i1));		
		return allChecks;
	}
	
	public List<String> getHeadersList(){
		List<String> headerList = new ArrayList<String>();
		headerList.add("Flowable Name");
		headerList.add("Flowable AltName");
		headerList.add("CAS");
		headerList.add("Chemical formula");
		headerList.add("SMILES");
		return headerList;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCas() {
		return cas;
	}

	public void setCas(String cas) {
		this.cas = cas;
	}

	public boolean isEmission() {
		return isEmission;
	}

	public void setEmission(boolean isEmission) {
		this.isEmission = isEmission;
	}

	public boolean isResource() {
		return isResource;
	}

	public void setResource(boolean isResource) {
		this.isResource = isResource;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public String getSMILES() {
		return SMILES;
	}

	public void setSMILES(String sMILES) {
		SMILES = sMILES;
	}

	public List<String> getAltNames() {
		return altNames;
	}

	public void setAltNames(List<String> altNames) {
		this.altNames = altNames;
	}

	public Resource getRdfClass() {
		return rdfClass;
	}

	public void setRdfClass(Resource rdfClass) {
		this.rdfClass = rdfClass;
	}

	public Pattern getAcceptableCASFormat() {
		return acceptableCASFormat;
	}

	public void setAcceptableCASFormat(Pattern acceptableCASFormat) {
		this.acceptableCASFormat = acceptableCASFormat;
	}
}
