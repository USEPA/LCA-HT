package gov.epa.nrmrl.std.lca.ht.dataCuration;

import gov.epa.nrmrl.std.lca.ht.dataModels.Person;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;

import java.util.Date;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * This class provides access to the RDF Class which serves as a parent to multiple Comparisons.  There will typically
 * be one per LCA-HT session, and curator information, creation time, and modified time will be included along with
 * comments.  From instantiation, the AnnotationProvider must have tdbResource (handle to its instantiation in the TDB)
 *  
 * @author Tom Transue
 *
 */
public class AnnotationProvider {
	private static final Resource rdfClass = FedLCA.Annotation;

	private static AnnotationProvider currentAnnotation = null;
	static {
		if (currentAnnotation == null) {
			currentAnnotation = new AnnotationProvider();
		}
	}

	private Resource tdbResource = null;
	private Date creationDate;
	private Date modifiedDate = null;
	private Person curator = null;
	private String comment = null;

	/**
	 * Because AnnotationProviders are not often created, bundling the transaction safe TDB writes is not
	 * necessary
	 * 
	 * @author = Tom Transue
	 */
	public AnnotationProvider() {
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
		Date now = new Date();
		this.creationDate = now;
		setModifiedDate(now);
	}
	
	public AnnotationProvider(Resource tdbResource){
		this.tdbResource = tdbResource;
		updateSyncDataFromTDB();
		if (this.creationDate == null){
			this.creationDate = new Date();
		}
	}

	public static void updateCurrentAnnotationModifiedDate() {
		currentAnnotation.setCreationDate(new Date());
	}
	
//	DFNode modNode = AnnotationProvider.getCurrentAnnotation().getProperty(DCTerms.modified).getObject();
//	String modString = "";
//	try {
//		Literal modLiteral = modNode.asLiteral();
//		// XSDDateTime modDateTime = (XSDDateTime) modLiteral.getValue();
//		// ABOVE .getValue() METHOD CHOKES ON BAD XSDDateTime Literals
//		// .getString() should work for these purposes
//		modString = modLiteral.getString();
//		// String modLexical = modLiteral.getLexicalForm();
//		// Calendar modCalendar = modDateTime.asCalendar();
//		// System.out.println("modLexical = " + modLexical);
//		// System.out.println("modCalendar = " + modCalendar);
//
//	} catch (Exception e2) {
//		// TODO Auto-generated catch block
//		e2.printStackTrace();
//	}

	
	public void updateSyncDataFromTDB() {
		if (tdbResource == null) {
			return;
		}
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		// Model tdbModel = ActiveTDB.getModel(null);
		StmtIterator stmtIterator = tdbResource.listProperties();
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
//			if (statement.getPredicate().equals(DCTerms.created)) {
//				this.creationDate = statement.getObject().asLiteral().getLexicalForm();
//			} else if (statement.getPredicate().equals(DCTerms.modified)) {
//				this.userDataObject = statement.getObject().asResource();
//			} else if (statement.getPredicate().equals(FedLCA.comparedMaster)) {
//				this.masterDataObject = statement.getObject().asResource();
//			} else if (statement.getPredicate().equals(FedLCA.comparedEquivalence)) {
//				this.equivalence = statement.getObject().asResource();
//			} else if (statement.getPredicate().equals(FedLCA.memberOfCollection)) {
//				this.annotationResource = statement.getObject().asResource();
//			} else if (statement.getPredicate().equals(RDFS.comment)) {
//				this.comment = statement.getObject().asLiteral().getString();
//			}
		}
		ActiveTDB.tdbDataset.end();
	}

	public static Resource getRdfclass() {
		return rdfClass;
	}

	public static AnnotationProvider getCurrentAnnotation() {
		return currentAnnotation;
	}

	public static void setCurrentAnnotation(AnnotationProvider currentAnnotation) {
		AnnotationProvider.currentAnnotation = currentAnnotation;
	}

	public Resource getTdbResource() {
		return tdbResource;
	}

	public void setTdbResource(Resource tdbResource) {
		this.tdbResource = tdbResource;
	}

	public Date getCreationDate() {
		return creationDate;
	}


	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public Person getCurator() {
		return curator;
	}

	public void setCurator(Person curator) {
		this.curator = curator;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setCreationDate(Date creationDate) {
		ActiveTDB.tsReplaceLiteral(currentAnnotation.tdbResource, DCTerms.modified, creationDate);
		this.creationDate = creationDate;
	}
}
