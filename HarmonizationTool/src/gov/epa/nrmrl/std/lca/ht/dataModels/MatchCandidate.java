package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class MatchCandidate {
	// EACH MatchCandidate HAS AN ItemToMatch AND A MatchCandidate
	private int itemToMatchRow;
	private Resource itemToMatchTDBResource;
	private Resource matchCandidateTDBResource;
	private int matchedFeatureCount = 0;
	private boolean confirmed;

	public MatchCandidate(Resource  itemToMatchTDBResource, Resource matchCandidateTDBResource) {
		super();
		this.itemToMatchTDBResource = itemToMatchTDBResource;
		this.matchCandidateTDBResource = matchCandidateTDBResource;

	}
	public MatchCandidate(int itemToMatchRow, Resource itemToMatchTDBResource, Resource matchCandidateTDBResource) {
		super();
		this.itemToMatchRow = itemToMatchRow;
		this.itemToMatchTDBResource = itemToMatchTDBResource;
		this.matchCandidateTDBResource = matchCandidateTDBResource;
	}

	public boolean confirmRDFtypeMatch() {
		// SHOULD USE REASONER TO EXPLORE SUBCLASSES - FIXME
		if (itemToMatchTDBResource == null){
			return false;
		}
		if (matchCandidateTDBResource == null){
			return false;
		}
		StmtIterator itemStatementIterator = itemToMatchTDBResource.listProperties(RDF.type);
		while (itemStatementIterator.hasNext()) {	
//			RDFNode type = itemStatementIterator.next().getObject();
			RDFNode type = itemStatementIterator.nextStatement().getObject();
			Model tdbModel = ActiveTDB.getModel(null);
			if (tdbModel.contains(matchCandidateTDBResource, RDF.type, type)) {
				return true;
			}
		}
		return false;
	}

	public int getItemToMatchRow() {
		return itemToMatchRow;
	}

	public void setItemToMatchRow(int itemToMatchRow) {
		this.itemToMatchRow = itemToMatchRow;
	}

	public Resource getItemToMatchTDBResource() {
		return itemToMatchTDBResource;
	}

	public void setItemToMatchTDBResource(Resource itemToMatchTDBResource) {
		this.itemToMatchTDBResource = itemToMatchTDBResource;
	}

	public Resource getMatchCandidateTDBResource() {
		return matchCandidateTDBResource;
	}

	public void setMatchCandidateTDBResource(Resource matchCandidateTDBResource) {
		this.matchCandidateTDBResource = matchCandidateTDBResource;
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	public int getMatchedFeatureCount() {
		return matchedFeatureCount;
	}

	public void setMatchedFeatureCount(int matchedFeatureCount) {
		this.matchedFeatureCount = matchedFeatureCount;
	}

	public void incrementMatchFeatureCount() {
		this.matchedFeatureCount++;
	}
}
