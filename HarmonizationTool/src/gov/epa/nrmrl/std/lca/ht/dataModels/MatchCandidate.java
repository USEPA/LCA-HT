package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class MatchCandidate {
	// EACH MatchCandidate HAS AN ItemToMatch AND A MatchCandidate
	private int itemToMatchRow;
	private Resource itemToMatchTDBResource;
	private Resource matchCandidateTDBResource;
	private boolean confirmed;

	public MatchCandidate(int itemToMatchRow, Resource itemToMatchTDBResource, Resource matchCandidateTDBResource) {
		super();
		this.itemToMatchRow = itemToMatchRow;
		this.itemToMatchTDBResource = itemToMatchTDBResource;
		this.matchCandidateTDBResource = matchCandidateTDBResource;
	}

	public boolean confirmRDFtypeMatch() {
		// SHOULD USE REASONER TO EXPLORE SUBCLASSES
		StmtIterator itemStatementIterator = itemToMatchTDBResource.listProperties(RDF.type);
		while (itemStatementIterator.hasNext()) {
			RDFNode type = itemStatementIterator.next().getObject();
			if (ActiveTDB.model.contains(matchCandidateTDBResource, RDF.type, type)) {
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
}
