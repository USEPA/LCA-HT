package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.FlowContext;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.FlowProperty;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.FlowUnit;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.Flowable;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;

public class DataRow {
	private List<String> columnValues = new ArrayList<String>();

	private Flowable flowable = null;
	private FlowContext flowContext = null;
//	private FlowProperty flowProperty = null;
	private FlowUnit flowUnit = null;

	private Resource masterFlowResource = null;

	private int rowNumber;
	private String rowToolTip;
	private Resource openLCASourceURI = null;

	public DataRow() {
	}

	@Override
	public String toString() {
		return "DataRow [columnValues=" + columnValues + "]";
	}

	public Resource getMatchingMasterContext() {
		if (flowContext == null) {
			return null;
		}
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.getModel(null);
		Statement statement = tdbModel.getProperty(flowContext.getTdbResource(), OWL.sameAs);
		ActiveTDB.tdbDataset.end();
		if (statement != null) {
			return statement.getObject().asResource();
		}
		return null;
	}

//	public Resource getMatchingMasterProperty() {
//		if (flowProperty == null) {
//			return null;
//		}
//		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
//		Model tdbModel = ActiveTDB.getModel(null);
//		Statement statement = tdbModel.getProperty(flowProperty.getTdbResource(), OWL.sameAs);
//		ActiveTDB.tdbDataset.end();
//		if (statement != null) {
//			return statement.getObject().asResource();
//		}
//		return null;
//	}

	public Resource getMatchingMasterFlowUnit() {
//		if (flowProperty == null) {
//			return null;
//		}
//		FlowUnit flowUnit = flowProperty.getUserDataFlowUnit();
		if (flowUnit == null) {
			return null;
		}
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.getModel(null);
		Statement statement = tdbModel.getProperty(flowUnit.getTdbResource(), OWL.sameAs);
		ActiveTDB.tdbDataset.end();
		if (statement != null) {
			return statement.getObject().asResource();
		}
		return null;
	}

	public Iterator<String> getIterator() {
		return columnValues.iterator();
	}

	public int getSize() {
		if (columnValues == null) {
			return -1;
		}
		return columnValues.size();
	}

	public void setColumnValues(List<String> columnValues) {
		this.columnValues = columnValues;
	}

	public List<String> getColumnValues() {
		return columnValues;
	}

	public void add(String s) {
		columnValues.add(s);
	}

	public void set(int index, String string) {
		columnValues.set(index, string);
	}

	// public void setToolTipValue(int index, String string){
	// while (toolTipValues.size() <= index){
	// toolTipValues.add("");
	// }
	// toolTipValues.set(index, string);
	// }

	public String get(int index) {
		if (index < 0 || index >= columnValues.size()) {
			return null;
		}
		return columnValues.get(index);
	}

	public String getCSVTableIndex(int i) {
		if (i < 1 || i > columnValues.size()) {
			return null;
		}
		return columnValues.get(i - 1);
	}

	// public String getToolTipValue(int index) {
	// while (toolTipValues.size() <= index){
	// toolTipValues.add("");
	// }
	// return toolTipValues.get(index);
	// }

	public String join(String delimiter) {
		if (columnValues.isEmpty()) {
			return "";
		}
		String joinedRow = columnValues.get(0);
		for (int i = 1; i < columnValues.size(); i++) {
			joinedRow += delimiter + columnValues.get(i);
		}
		return joinedRow;
	}

	public void clear() {
		columnValues.clear();
		// toolTipValues.clear();
	}

	// public List<String> getToolTipValues() {
	// while (toolTipValues.size() < columnValues.size()){
	// toolTipValues.add("");
	// }
	// return toolTipValues;
	// }

	// public void setToolTipValues(List<String> toolTipValues) {
	// while (toolTipValues.size() < columnValues.size()){
	// toolTipValues.add("");
	// }
	// this.toolTipValues = toolTipValues;
	// }

	public String getRowToolTip() {
		return rowToolTip;
	}

	public void setRowToolTip(String rowToolTip) {
		this.rowToolTip = rowToolTip;
	}

	public int getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(int rowNumber) {
		this.rowNumber = rowNumber;
	}

	public Flowable getFlowable() {
		return flowable;
	}

	public void setFlowable(Flowable flowable) {
		this.flowable = flowable;
	}

	public FlowContext getFlowContext() {
		return flowContext;
	}

	public void setFlowContext(FlowContext flowContext) {
		this.flowContext = flowContext;
	}

//	public FlowProperty getFlowProperty() {
//		return flowProperty;
//	}
//
//	public void setFlowProperty(FlowProperty flowProperty) {
//		this.flowProperty = flowProperty;
//	}

	public Resource getSourceFlowTDBResource() {
		return openLCASourceURI;
	}

	public void setSourceFlowTDBResource(Resource sourceFlowTDBResource) {
		this.openLCASourceURI = sourceFlowTDBResource;
	}

	public boolean setMatches() {
		// StringBuilder b = new StringBuilder();
		// b.append(Prefixes.getPrefixesForQuery());
		// b.append(" \n");
		// b.append("SELECT distinct ?f ?masterTest \n");
		// b.append("WHERE \n");
		// b.append("  { \n");
		// b.append("    { \n");
		//
		// b.append("      { ?f skos:altLabel \"" + namesToMatch.get(0) + "\"^^xsd:string  . } \n");
		// for (int i = 1; i < namesToMatch.size(); i++) {
		// b.append("   UNION { ?f skos:altLabel \"" + namesToMatch.get(i) + "\"^^xsd:string . } \n");
		// }
		// b.append("    } \n");
		//
		// if (checkCas) {
		// b.append("    optional {?f eco:casNumber ?cas . }\n");
		// b.append("    filter (str(?cas) = \"" + qCAS + "\")\n");
		// }
		// b.append("    ?f eco:hasDataSource ?ds . \n");
		// b.append("    ?ds a lcaht:MasterDataset . \n");
		// b.append("    ?f a eco:Flowable . \n");
		// b.append("   } \n");
		// String query = b.toString();
		// System.out.println("Query = \n" + query);
		//
		// HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		// harmonyQuery2Impl.setQuery(query);
		//
		// ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		//
		// int count = 0;
		// while (resultSet.hasNext()) {
		// QuerySolution querySolution = resultSet.next();
		// RDFNode rdfNode = querySolution.get("f");
		// count++;
		// matchCandidates.put(rdfNode.asResource(), "=");
		// CurationMethods.setComparison(tdbResource, rdfNode.asResource(), FedLCA.Equivalent);
		// }
		//
		// Resource currentMasterFlowResource = masterFlowResource;
		// Flowable flowable = getFlowable();
		// FlowContext flowContext = getFlowContext();
		// FlowProperty flowProperty = getFlowUnit();
		// if (flowable == null || flowContext == null || flowProperty == null) {
		// return false;
		// }
		// Resource flowableResource = flowable.getTdbResource();
		// Resource flowContextResource = flowContext.getTdbResource();
		// Resource flowPropertyResource = flowProperty.getTdbResource();
		// Model tdbModel = ActiveTDB.getModel(null);
		// ResIterator resIterator = tdbModel.listResourcesWithProperty(FedLCA.comparedSource, flowableResource);
		//
		// while (resIterator.hasNext()) {
		// Resource comparisonResource = resIterator.next();
		// if (!tdbModel.contains(comparisonResource, FedLCA.comparedEquivalence, FedLCA.Equivalent)) {
		// continue;
		// }
		// RDFNode masterNode = tdbModel.listObjectsOfProperty(comparisonResource, FedLCA.comparedMaster).next();
		// Resource masterFlowableResource = masterNode.asResource();
		//
		// ResIterator resIterator2 = tdbModel.listResourcesWithProperty(ECO.hasFlowable, masterFlowableResource);
		// while (resIterator2.hasNext()) {
		// Resource flowCandidateResource = resIterator2.next();
		// if (!tdbModel.contains(flowCandidateResource, FedLCA.hasFlowContext, flowContextResource)) {
		// continue;
		// }
		// if (!tdbModel.contains(flowCandidateResource, FedLCA.hasFlowProperty, flowPropertyResource)) {
		// continue;
		// }
		// if (!tdbModel.contains(flowCandidateResource, RDF.type, FedLCA.Flow)) {
		// continue;
		// }
		// RDFNode dataSourceResource = tdbModel.getProperty(flowCandidateResource, ECO.hasDataSource).getObject();
		// if (dataSourceResource != null) {
		// if (!tdbModel.contains(dataSourceResource.asResource(), RDF.type, LCAHT.MasterDataset)) {
		// continue;
		// }
		// }
		// if (masterFlowResource == null) {
		// // NEW
		// masterFlowResource = flowCandidateResource;
		// return true;
		// }
		// if (flowCandidateResource.equals(masterFlowResource)) {
		// // NO CHANGE
		// return true;
		// }
		// // CHANGE
		// masterFlowResource = flowCandidateResource;
		// return true;
		// }
		// }
		return false;
	}

	public Resource getFlowResource() {
		return masterFlowResource;
	}

	public void setFlowResource(Resource flowResource) {
		this.masterFlowResource = flowResource;
	}

	public FlowUnit getFlowUnit() {
		return flowUnit;
	}

	public void setFlowUnit(FlowUnit flowUnit) {
		this.flowUnit = flowUnit;
	}
}
