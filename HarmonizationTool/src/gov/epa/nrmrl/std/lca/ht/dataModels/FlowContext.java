package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVColumnInfo;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FASC;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FEDLCA;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

public class FlowContext {

	private String primaryFlowContext;
	private List<String> supplementaryFlowContexts;
	private static Resource rdfClass = FASC.Compartment;
	// TODO ADD A GETTER AND SETTER FOR THE rdfClass
	private Resource tdbResource;

	public FlowContext() {
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
	}

	public static final CSVColumnInfo[] getHeaderMenuObjects() {
		CSVColumnInfo[] results = new CSVColumnInfo[2];

		results[0] = new CSVColumnInfo("Context (primary)");
		results[0].setRequired(true);
		results[0].setUnique(true);
		results[0].setCheckLists(getContextNameCheckList());
		results[0].setLeftJustified(true);
		results[0].setRDFClass(rdfClass);
		results[0].setTdbProperty(FASC.hasCompartment);
		results[0].setRdfDatatype(XSDDatatype.XSDstring);

		results[1] = new CSVColumnInfo("Context (additional)");
		results[1].setRequired(false);
		results[1].setUnique(false);
		results[1].setCheckLists(getContextNameCheckList());
		results[1].setLeftJustified(true);
		results[1].setRDFClass(rdfClass);
		results[1].setTdbProperty(FEDLCA.flowContextSupplementalDescription);
		results[1].setRdfDatatype(XSDDatatype.XSDstring);
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
		RDFDatatype rdfDatatype = getHeaderMenuObjects()[0].getRdfDatatype();
		ActiveTDB.tsReplaceLiteral(tdbResource, FEDLCA.flowContextPrimaryDescription, rdfDatatype, primaryFlowContext);
	}

	public List<String> getsupplementaryFlowContexts() {
		return supplementaryFlowContexts;
	}

	public void setSupplementaryFlowContexts(List<String> supplementaryFlowContexts) {
		ActiveTDB.tsRemoveAllObjects(tdbResource, FEDLCA.flowContextSupplementalDescription);
		this.supplementaryFlowContexts = supplementaryFlowContexts;
		for (String supplementaryFlowContext : supplementaryFlowContexts) {
			ActiveTDB.tsAddLiteral(tdbResource, FEDLCA.flowContextSupplementalDescription, supplementaryFlowContext);
		}
	}

	public void addSupplementaryFlowContext(String supplementaryFlowContext) {
		if (supplementaryFlowContexts == null) {
			supplementaryFlowContexts = new ArrayList<String>();
		}
		supplementaryFlowContexts.add(supplementaryFlowContext);
		ActiveTDB.tsAddLiteral(tdbResource, FEDLCA.flowContextSupplementalDescription, supplementaryFlowContext);
	}

	public void removeSupplementaryFlowContext(String supplementaryFlowContext) {
		this.supplementaryFlowContexts.remove(supplementaryFlowContext);
		Literal literalToRemove = ActiveTDB.tsCreateTypedLiteral(supplementaryFlowContext);
		ActiveTDB.tsRemoveStatement(tdbResource, FEDLCA.flowContextSupplementalDescription, literalToRemove);
	}

	public Resource getTdbResource() {
		return tdbResource;
	}

	public void setTdbResource(Resource tdbResource) {
		// StmtIterator stmtIterator = this.tdbResource.listProperties();
		// while (stmtIterator.hasNext()){
		// Statement statement = stmtIterator.next();
		// ActiveTDB.tdbModel.remove(statement);
		// }
		// NEXT STATEMENT REPLACES ABOVE
		this.tdbResource.removeProperties();
		this.tdbResource = tdbResource;
	}

	public static Resource getRdfclass() {
		return rdfClass;
	}
}
