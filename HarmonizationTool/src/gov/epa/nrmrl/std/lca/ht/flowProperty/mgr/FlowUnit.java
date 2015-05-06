package gov.epa.nrmrl.std.lca.ht.flowProperty.mgr;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;
import gov.epa.nrmrl.std.lca.ht.vocabulary.SKOS;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class FlowUnit {
	public String name;
	public String description;
	public List<String> synonyms = new ArrayList<String>();
	public String uuid;
	// public String superGroup;
	public Resource unitGroup;
	public String superGroupName = "(other)";
	public Resource referenceUnit;
	private Resource tdbResource;
	public double conversionFactor;
//	public FlowProperty flowProperty;
	private Resource matchingResource = null;
	private int displayOrder = -1;
	private int firstRow = -1;

	public FlowUnit() {

	}

	public FlowUnit(Resource resource) {
		this.tdbResource = resource;
		syncFromTDB();
	}

	public void syncFromTDB() {
		if (tdbResource == null) {
			return;
		}
		/* START OF TRANSACTION */
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		this.name = tdbResource.getProperty(RDFS.label).getObject().asLiteral().getString();
		this.description = tdbResource.getProperty(DCTerms.description).getObject().asLiteral().getString();
		synonyms = new ArrayList<String>();
		StmtIterator stmtIterator = tdbResource.listProperties(SKOS.altLabel);
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
			synonyms.add(statement.getObject().asLiteral().getString());
		}
		this.uuid = tdbResource.getProperty(FedLCA.hasOpenLCAUUID).getObject().asLiteral().getString();

		Model tdbModel = ActiveTDB.getModel(null);
		ResIterator resIterator = tdbModel.listSubjectsWithProperty(FedLCA.hasFlowUnit, tdbResource);
		if (resIterator.hasNext()){
			this.unitGroup = resIterator.next();
		}
		stmtIterator = this.unitGroup.listProperties(RDF.type);
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
			Resource unitGroupClass = statement.getObject().asResource();
			if ((!unitGroupClass.equals(LCAHT.MasterDataset)) && (!unitGroupClass.equals(FedLCA.UnitGroup))){
				this.superGroupName = unitGroupClass.getProperty(RDFS.label).getObject().asLiteral().getString();
			}
		}
		
		ActiveTDB.tdbDataset.end();
		/* END OF TRANSACTION */
		this.conversionFactor = tdbResource.getProperty(FedLCA.unitConversionFactor).getObject().asLiteral()
				.getDouble();
		this.displayOrder = tdbResource.getProperty(FedLCA.displaySortIndex).getObject().asLiteral().getInt();
	}

	public void setMatchingResource(Resource matchingResource) {
		if (matchingResource == null) {
			ActiveTDB.tsRemoveAllLikeObjects(matchingResource, OWL.sameAs, null, null);
			this.matchingResource = null;
			return;
		}
		this.matchingResource = matchingResource;
		ActiveTDB.tsReplaceResourceSameType(tdbResource, OWL.sameAs, matchingResource, null);
	}

	public Resource getMatchingResource() {
		return matchingResource;
	}
	
	public boolean setMatches() {
//		String unitStr = (String) getOneProperty(flowPropertyUnit);

		if (name == null) {
			return false;
		}
		for (FlowUnit flowUnit : FlowProperty.lcaMasterUnits) {
			if (flowUnit.name.equals(name)) {
				setMatchingResource(flowUnit.getTdbResource());
				return true;
			}
		}
		return false;
	}

	public String getUnitGroupName() {
		return unitGroup.listProperties(RDFS.label).toList().get(0).getString();
	}

	public String getUnitGroupUUID() {
		return unitGroup.listProperties(FedLCA.hasOpenLCAUUID).toList().get(0).getString();
	}

	public String getReferenceUnitName() {
		return referenceUnit.listProperties(RDFS.label).toList().get(0).getString();
	}

	public Resource getTdbResource() {
		return tdbResource;
	}

	public void setTdbResource(Resource tdbResource) {
		this.tdbResource = tdbResource;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public int getFirstRow() {
		return firstRow;
	}

	public void setFirstRow(int firstRow) {
		this.firstRow = firstRow;
	}
}
