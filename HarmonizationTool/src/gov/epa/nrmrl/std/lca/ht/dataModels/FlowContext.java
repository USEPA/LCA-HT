package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVColumnInfo;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FASC;
import harmonizationtool.vocabulary.FEDLCA;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

public class FlowContext {

	private String primaryFlowContext;
//	private String additionalFlowContext;
	private List<String> additionalFlowContexts = new ArrayList<String>();
	private Resource tdbResource;

	private static final Resource rdfClass = FASC.Compartment;

	public FlowContext() {
		this.tdbResource = ActiveTDB.model.createResource();
	}

	public static CSVColumnInfo[] getHeaderMenuObjects() {
		CSVColumnInfo[] results = new CSVColumnInfo[2];

		results[0] = new CSVColumnInfo("Context (primary)");
		results[0].setRequired(true);
		results[0].setUnique(true);
		results[0].setCheckLists(getContextNameCheckList());
		results[0].setLcaDataField(new LCADataField());
		results[0].getLcaDataField().setResourceSubject(rdfClass);
		results[0].getLcaDataField().setPropertyPredicate(FEDLCA.flowContextPrimaryDescription);
		results[0].getLcaDataField().setLiteralObjectType("String");
		results[0].getLcaDataField().setRequired(true);
		results[0].getLcaDataField().setFunctional(true);

		results[1] = new CSVColumnInfo("Context (additional)");
		results[1].setRequired(false);
		results[1].setUnique(false);
		results[1].setCheckLists(getContextNameCheckList());
		results[1].setLcaDataField(new LCADataField());
		results[1].getLcaDataField().setResourceSubject(rdfClass);
		results[1].getLcaDataField().setPropertyPredicate(FEDLCA.flowContextSupplementalDescription);
		results[1].getLcaDataField().setLiteralObjectType("String");
		results[1].getLcaDataField().setRequired(false);
		results[1].getLcaDataField().setFunctional(false);
		return results;
	}

	private static List<QACheck> getContextNameCheckList() {
		List<QACheck> qaChecks = QACheck.getGeneralQAChecks();

		// String d1 = "Non-allowed characters";
		// String e1 =
		// "Various characters are not considered acceptible in standard chemical names.";
		// String s1 = "Check your data";
		// Pattern p1 = Pattern.compile("^([^\"]+)[\"]([^\"]+)$");
		// String r1 = null;
		//
		// qaChecks.add(new QACheck(d1, e1, s1, p1, r1, false));
		return qaChecks;
	}

	public String getPrimaryFlowContext() {
		return primaryFlowContext;
	}

	public void setPrimaryFlowContext(String primaryFlowContext) {
		this.primaryFlowContext = primaryFlowContext;
		ActiveTDB.replaceLiteral(tdbResource, FEDLCA.flowContextPrimaryDescription, primaryFlowContext);
	}

	public List<String> getAdditionalFlowContexts() {
		return additionalFlowContexts;
	}

	public void setAdditionalFlowContexts(List<String> additionalFlowContexts) {
		this.additionalFlowContexts = additionalFlowContexts;
		tdbResource.removeAll(FEDLCA.flowContextSupplementalDescription);
		for(String additionalFlowContext:additionalFlowContexts){
			ActiveTDB.replaceLiteral(tdbResource, FEDLCA.flowContextSupplementalDescription, additionalFlowContext);			
		}
	}

	public void addAdditionalFlowContext(String additionalFlowContext) {
		this.additionalFlowContexts.add(additionalFlowContext);
		tdbResource.addProperty(FEDLCA.flowContextSupplementalDescription, additionalFlowContext);
	}


	public void removeAdditionalFlowContext(String additionalFlowContext) {
		this.additionalFlowContexts.remove(additionalFlowContext);
		Literal literal = ActiveTDB.model.createLiteral(additionalFlowContext);
		ActiveTDB.model.remove(this.tdbResource, FEDLCA.flowContextSupplementalDescription,literal);
	}
	
	public Resource getTdbResource() {
		return tdbResource;
	}

	public void setTdbResource(Resource tdbResource) {
//		StmtIterator stmtIterator = this.tdbResource.listProperties();
//		while (stmtIterator.hasNext()){
//			Statement statement = stmtIterator.next();
//			ActiveTDB.model.remove(statement);
//		}
		// NEXT STATEMENT REPLACES ABOVE
		this.tdbResource.removeProperties();
		this.tdbResource = tdbResource;
	}

}
