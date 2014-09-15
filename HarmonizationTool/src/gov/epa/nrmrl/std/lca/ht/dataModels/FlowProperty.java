package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVColumnInfo;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

public class FlowProperty {

	private String primaryFlowProperty;
	private List<String> supplementaryFlowProperties;
	private static final Resource rdfClass = FedLCA.FlowProperty;
	private Resource tdbResource;

	public FlowProperty() {
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
	}

	public static final CSVColumnInfo[] getHeaderMenuObjects() {
		CSVColumnInfo[] results = new CSVColumnInfo[2];

		results[0] = new CSVColumnInfo("Property (primary)");
		results[0].setRequired(true);
		results[0].setUnique(true);
		results[0].setCheckLists(getPropertyNameCheckList());
		results[0].setLeftJustified(true);
		results[0].setRDFClass(rdfClass);
		results[0].setTdbProperty(FedLCA.flowPropertyPrimaryDescription);
		results[0].setRdfDatatype(XSDDatatype.XSDstring);

		results[1] = new CSVColumnInfo("Property (additional)");
		results[1].setRequired(false);
		results[1].setUnique(false);
		results[1].setCheckLists(getPropertyNameCheckList());
		results[1].setLeftJustified(true);
		results[1].setRDFClass(rdfClass);
		results[1].setTdbProperty(FedLCA.flowPropertySupplementalDescription);
		results[1].setRdfDatatype(XSDDatatype.XSDstring);
		return results;
	}

	private static List<QACheck> getPropertyNameCheckList() {
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

	public String getPrimaryFlowProperty() {
		return primaryFlowProperty;
	}

	public void setPrimaryFlowProperty(String primaryFlowProperty) {
		this.primaryFlowProperty = primaryFlowProperty;
		RDFDatatype rdfDatatype = getHeaderMenuObjects()[0].getRdfDatatype();
		ActiveTDB.tsReplaceLiteral(tdbResource, FedLCA.flowPropertyPrimaryDescription, rdfDatatype, primaryFlowProperty);
	}

	public List<String> getsupplementaryFlowProperties() {
		return supplementaryFlowProperties;
	}

	public void setSupplementaryFlowProperties(List<String> supplementaryFlowProperties) {
		ActiveTDB.tsRemoveAllObjects(tdbResource, FedLCA.flowPropertySupplementalDescription);
		this.supplementaryFlowProperties = supplementaryFlowProperties;
		for (String supplementaryFlowProperty : supplementaryFlowProperties) {
			ActiveTDB.tsAddLiteral(tdbResource, FedLCA.flowPropertySupplementalDescription, supplementaryFlowProperty);
		}
	}

	public void addSupplementaryFlowProperty(String supplementaryFlowProperty) {
		if (supplementaryFlowProperties == null) {
			supplementaryFlowProperties = new ArrayList<String>();
		}
		supplementaryFlowProperties.add(supplementaryFlowProperty);
		ActiveTDB.tsAddLiteral(tdbResource, FedLCA.flowPropertySupplementalDescription, supplementaryFlowProperty);
	}

	public void removeSupplementaryFlowProperty(String supplementaryFlowProperty) {
		this.supplementaryFlowProperties.remove(supplementaryFlowProperty);
		Literal literalToRemove = ActiveTDB.tsCreateTypedLiteral(supplementaryFlowProperty);
		ActiveTDB.tsRemoveStatement(tdbResource, FedLCA.flowPropertySupplementalDescription, literalToRemove);
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
