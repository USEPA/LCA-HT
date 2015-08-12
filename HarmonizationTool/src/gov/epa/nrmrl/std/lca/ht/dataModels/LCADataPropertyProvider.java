package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataFormatCheck.FormatCheck;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.Flowable;
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class LCADataPropertyProvider {
	public static Set<LCADataPropertyProvider> allRegisteredProviders = new HashSet<LCADataPropertyProvider>();
	private String propertyClass; // e.g. "Flowable"
	private String propertyName; // e.g. "Name"
	private Resource rdfClass; // e.g. ECO.Flowable;
	private RDFDatatype rdfDatatype; // e.g. XSDDatatype.XSDfloat
	private boolean isRequired = false; // e.g. true
	private boolean isUnique = true; // e.g. false
	private boolean leftJustified = true; // e.g. true
	private List<FormatCheck> qaChecks; // A LIST OF WAYS OF CHECKING THIS COLUMN FOR VALIDITY
	private Property tdbProperty;

	// private List<Issue> issues = new ArrayList<Issue>();
	// private List<String> values = new ArrayList<String>();

	public LCADataPropertyProvider() {

	}

	public LCADataPropertyProvider(String propertyName) {
		this.propertyName = propertyName;
	}

	public static void registerProviders(Map<String, LCADataPropertyProvider> hashToAdd) {
		for (LCADataPropertyProvider lcaDataPropertyProvider : hashToAdd.values()) {
			allRegisteredProviders.add(lcaDataPropertyProvider);
		}
	}

	public LCADataPropertyProvider copyLCADataProperty(LCADataPropertyProvider lcaDataProperty) {
		LCADataPropertyProvider result = new LCADataPropertyProvider(lcaDataProperty.getPropertyName());
		// CSVColumnInfo newCSVColumnInfo = new CSVColumnInfo();
		result.propertyName = lcaDataProperty.getPropertyName();
		result.rdfDatatype = lcaDataProperty.getRdfDatatype();

		result.isRequired = lcaDataProperty.isRequired();
		result.isUnique = lcaDataProperty.isUnique();
		result.leftJustified = lcaDataProperty.isLeftJustified();
		// result.tdbProperty = lcaDataProperty.getTDBProperty();
		result.qaChecks = lcaDataProperty.copyCheckLists();
		// BUT DON'T COPY ISSUES
		// this.issues = menuCSVColumnInfo.copyIssues();
		// INITIALIZE INSTEAD
		// result.issues = new ArrayList<Issue>();
		// propertyValueJavaClass = Integer.getClass();
		return result;
	}

	private static boolean compareLCADataPropertyProviders(LCADataPropertyProvider lcaDataProperty1,
			LCADataPropertyProvider lcaDataProperty2) {
		if (lcaDataProperty1 == null) {
			return false;
		}
		if (lcaDataProperty2 == null) {
			return false;
		}
		if (!lcaDataProperty1.getPropertyClass().equals(lcaDataProperty2.getPropertyClass())) {
			return false;
		}
		if (!lcaDataProperty1.getPropertyName().equals(lcaDataProperty2.getPropertyName())) {
			return false;
		}
		return true;
	}

	public boolean sameAs(LCADataPropertyProvider lcaDataPropertyProvider) {
		return compareLCADataPropertyProviders(this, lcaDataPropertyProvider);
	}

	/*
	 * TODO: TAHOWARD - The following two or three methods should allow us to assign columns at the time of assignment,
	 * and to restore them when restoring a table.
	 */

	public static List<LCADataPropertyProvider> retrieveDatasetLCADataPropertyProviderList(String datasetName) {
		List<LCADataPropertyProvider> currentDatasetLCADataPropertyProviderList = new ArrayList<LCADataPropertyProvider>();
		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		b.append("select distinct ?col ?dataProp ?dataClass \n");
		b.append("where {  \n");
		b.append("  ?dca a fedlca:DatasetColumnAssignment . \n");
		b.append("  ?dca eco:hasDataSource ?ds . \n");
		b.append("  ?ds rdfs:label \"" + datasetName + "\"^^xsd:string . \n");
		b.append("  ?dca fedlca:columnNumber ?col . \n");
		b.append("  ?dca fedlca:dataColumnProperty ?dataProp . \n");
		b.append("  ?dca fedlca:dataColumnClass ?dataClass . \n");
		b.append("} \n");
		b.append("order by ?col \n");
		String query = b.toString();
		System.out.println("Query " + query);
		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);

		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		boolean noHits = true;
		List<Resource> classList = new ArrayList<Resource>();
		List<RDFNode> propertyList = new ArrayList<RDFNode>();
		while (resultSet.hasNext()) {
			noHits = false;
			QuerySolution querySolution = resultSet.next();
			int col = querySolution.get("col").asLiteral().getInt();
			RDFNode rdfNode = querySolution.get("dataProp");
			Resource dataClass = querySolution.get("dataClass").asResource();

			while (col >= propertyList.size()) {
				classList.add(null);
				propertyList.add(null);
			}
			classList.add(col, dataClass);
			propertyList.add(col, rdfNode);
		}
		if (noHits) {
			return null;
		}
		/*
		 * TODO: There may be a better looping order, or a method to retrieve the LCADataPropertyProvider from a
		 * Property is needed
		 */

		for (int i = 1; i < propertyList.size(); i++) {
			RDFNode rdfNode = propertyList.get(i);
			Resource classValue = classList.get(i);
			if (rdfNode == null) {
				continue;
			}
			for (LCADataPropertyProvider lcaDataPropertyProvider : allRegisteredProviders) {
				Property property = lcaDataPropertyProvider.tdbProperty;
				Resource classResource = lcaDataPropertyProvider.getRDFClass();
				if (property.equals(rdfNode) && classResource.equals(classValue)) {
					while (currentDatasetLCADataPropertyProviderList.size() <= i) {
						currentDatasetLCADataPropertyProviderList.add(null);
					}
					currentDatasetLCADataPropertyProviderList.add(i, lcaDataPropertyProvider);
					break;
				}
			}
		}
		return currentDatasetLCADataPropertyProviderList;
	}

	/**
	 * This method gets from the TDB each assigned DatasetColumnAssignment and creates an ArrayList of
	 * LCADataPropertyProviders.  If none are found the method returns null.  The length of the ArrayList is not
	 * guaranteed to be the same as the length of the current Dataset Table column count.  Also, all un-assigned
	 * entries in the List will be null.
	 * @return An ArrayList<LCADataPropertyProvider> or null if none are found
	 */
	public static List<LCADataPropertyProvider> retrieveCurrentDatasetLCADataPropertyProviderList() {
		String curDataSourceProviderName = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey())
				.getDataSourceProvider().getDataSourceName();
		return retrieveDatasetLCADataPropertyProviderList(curDataSourceProviderName);
	}

	/**
	 * This method gets from the TDB the current RDF Resource associated with the specified columnNumber of
	 * the current Dataset Table.  If it is not assigned, it returns null.
	 * 
	 * @param columnNumber the column number to check
	 * @return the RDF Resource associated with this column number.
	 */
	public static Resource retrieveOneCurrentDataColumnProperty(int columnNumber) {
		Resource datasetColumnAssignment = null;
		String curDataSourceProviderName = CSVTableView.getCurrentDatasetName();

		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		b.append("select distinct ?dca \n");
		b.append("where {  \n");
		b.append("  ?dca a fedlca:DatasetColumnAssignment . \n");
		b.append("  ?dca eco:hasDataSource ?ds . \n");
		b.append("  ?ds rdfs:label \"" + curDataSourceProviderName + "\"^^xsd:string . \n");
		b.append("  ?dca fedlca:columnNumber " + columnNumber + " . \n");
		b.append("} \n");
		b.append("limit 1 \n");
		String query = b.toString();

		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);

		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			datasetColumnAssignment = querySolution.get("dca").asResource();
		}
		return datasetColumnAssignment;
	}

	/**
	 * This method assigns or re-assigns in the TDB a fedlca:DatasetColumnAssignment with the specified 
	 * columnNumber.  If the corresponding column in the current Dataset Table is NOT assigned, it will
	 * remove the resource.  If it is assigned, the existing or new fedlca:DatasetColumnAssignment will
	 * have the fedlca:dataColumnProperty equal to the LCADataPropertyProvider's tdbProperty which is a
	 * unique property for each LCADataPropertyProvider.  Note that if an similar LCADataPropertyProvider
	 * was assigned to a different column, this method does NOT attempt to find and re-assign / remove
	 * that column.
	 * 
	 * @param columnNumber the column number to assign.
	 */
	public static void storeOneCurrentColumnAssignment(int columnNumber) {
		Resource currentValue = retrieveOneCurrentDataColumnProperty(columnNumber);
		LCADataPropertyProvider lcaDataPropertyProvider = TableKeeper.getTableProvider(
				CSVTableView.getTableProviderKey()).getLcaDataProperties()[columnNumber];
		if (currentValue == null && lcaDataPropertyProvider == null) {
			return;
		}
		Resource curDataSourceProviderResource = CSVTableView.getCurrentDatasetTDBResource();
		// BEGIN WRITE TRANSACTION
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(null);
		try {
			if (currentValue == null) {
				currentValue = tdbModel.createResource(FedLCA.DatasetColumnAssignment);
			}
			if (lcaDataPropertyProvider == null) {
				tdbModel.removeAll(currentValue, null, null);
			} else {
				tdbModel.removeAll(currentValue, FedLCA.columnNumber, null);
				tdbModel.addLiteral(currentValue, FedLCA.columnNumber, columnNumber);

				Resource dataClass = lcaDataPropertyProvider.getRDFClass();
				tdbModel.removeAll(currentValue, FedLCA.dataColumnClass, null);
				tdbModel.add(currentValue, FedLCA.dataColumnClass, dataClass);

				Property property = lcaDataPropertyProvider.getTDBProperty();
				tdbModel.removeAll(currentValue, FedLCA.dataColumnProperty, null);
				tdbModel.add(currentValue, FedLCA.dataColumnProperty, property);
				// If currentValue did not exist, this is needed
				tdbModel.add(currentValue, ECO.hasDataSource, curDataSourceProviderResource);
			}
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			e.printStackTrace();
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// END WRITE TRANSACTION
	}

	// public String getDataClassName() {
	// if (rdfClass.hasProperty(RDFS.label)) {
	// return rdfClass.getPropertyResourceValue(RDFS.label).asLiteral().getString();
	// }
	// return null;
	// }

	// public LCADataPropertyProvider(LCADataPropertyProvider menuCSVColumnInfo) {
	// // CSVColumnInfo newCSVColumnInfo = new CSVColumnInfo();
	// this.propertyName = menuCSVColumnInfo.getPropertyName();
	// this.isRequired = menuCSVColumnInfo.isRequired();
	// this.isUnique = menuCSVColumnInfo.isUnique();
	// this.leftJustified = menuCSVColumnInfo.isLeftJustified();
	// this.rdfClass = menuCSVColumnInfo.getRDFClass();
	// this.tdbProperty = menuCSVColumnInfo.getTdbProperty();
	// this.rdfDatatype = menuCSVColumnInfo.getRdfDatatype();
	// this.checkLists = menuCSVColumnInfo.copyCheckLists();
	// // BUT DON'T COPY ISSUES
	// // this.issues = menuCSVColumnInfo.copyIssues();
	// // INITIALIZE INSTEAD
	// this.issues = new ArrayList<Issue>();
	// }

	public String getPropertyClass() {
		return propertyClass;
	}

	public void setPropertyClass(String propertyClass) {
		this.propertyClass = propertyClass;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	// public Object getPropertyValueJavaType() {
	// return propertyValueJavaType;
	// }
	//
	// public void setPropertyValueJavaType(Object propertyValueJavaType) {
	// this.propertyValueJavaType = propertyValueJavaType;
	// }

	public Resource getRDFClass() {
		return rdfClass;
	}

	public void setRDFClass(Resource rdfClass) {
		this.rdfClass = rdfClass;
	}

	public RDFDatatype getRdfDatatype() {
		return rdfDatatype;
	}

	public void setRDFDatatype(RDFDatatype rdfDatatype) {
		this.rdfDatatype = rdfDatatype;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public void setRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}

	public boolean isUnique() {
		return isUnique;
	}

	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}

	public boolean isLeftJustified() {
		return leftJustified;
	}

	public void setLeftJustified(boolean leftJustified) {
		this.leftJustified = leftJustified;
	}

	public List<FormatCheck> getCheckLists() {
		return qaChecks;
	}

	public void setCheckLists(List<FormatCheck> checkLists) {
		this.qaChecks = checkLists;
	}

	public List<FormatCheck> copyCheckLists() {
		List<FormatCheck> results = new ArrayList<FormatCheck>();
		for (FormatCheck qaCheck : qaChecks) {
			FormatCheck newQACheck = new FormatCheck(qaCheck);
			results.add(newQACheck);
		}
		return results;
	}

	public void addQACheck(FormatCheck qaCheck) {
		this.qaChecks.add(qaCheck);
	}

	// public List<Issue> getIssues() {
	// return issues;
	// }
	//
	// public void setIssues(List<Issue> issues) {
	// this.issues = issues;
	// }
	//
	// public void addIssue(Issue issue) {
	// this.issues.add(issue);
	// }
	//
	// public int getIssueCount() {
	// return this.issues.size();
	// }
	//
	// public void clearIssues() {
	// this.issues.clear();
	// }

	public Property getTDBProperty() {
		return tdbProperty;
	}

	public void setTDBProperty(Property tdbProperty) {
		this.tdbProperty = tdbProperty;
	}
}
