package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.dialog.GenericStringBox;
import gov.epa.nrmrl.std.lca.ht.sparql.GenericUpdate;
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.handlers.HandlerUtil;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class DataSourceKeeper {
	private static List<DataSourceProvider> dataSourceProviderList = new ArrayList<DataSourceProvider>();
	private static final String orphanDataSourceNameBase = "LCA-HT internal dataset ";

	// protected final static Model tdbModel = ActiveTDB.tdbModel;

	public static String getOrphanDataDourceNameBase() {
		return orphanDataSourceNameBase;
	}

	private DataSourceKeeper() {
	}

	public static boolean add(DataSourceProvider dataSourceProvider) {
		if (dataSourceProviderList.contains(dataSourceProvider)) {
			return false;
		}
		return dataSourceProviderList.add(dataSourceProvider);
	}

	public static boolean remove(DataSourceProvider dataSourceProvider) {
		dataSourceProvider.remove();
		// TODO - NEEDS TO REMOVE ALL DATA
		return dataSourceProviderList.remove(dataSourceProvider);
	}

	public static int getNextOrphanDataSetNumber() {
		int newNumber = 1;
		for (String name : getAlphabetizedNames()) {
			if (name.startsWith(orphanDataSourceNameBase)) {
				newNumber = 1 + Integer.parseInt(name.substring(orphanDataSourceNameBase.length()));
			}
		}
		return newNumber;
	}

	// public static boolean remove(DataSourceProvider dataSourceProvider,
	// boolean removeTDBData) {
	// if (removeTDBData) {
	// Resource tdbResource = dataSourceProvider.getTdbResource();
	// ActiveTDB.removeAllWithSubject(tdbResource);
	// ActiveTDB.removeAllWithObject(tdbResource);
	// }
	// return dataSourceProviderList.remove(dataSourceProvider);
	// }

	public static List<Integer> getIDs() {
		List<Integer> ids = new ArrayList<Integer>();
		Iterator<DataSourceProvider> iterator = dataSourceProviderList.iterator();
		while (iterator.hasNext()) {
			ids.add(dataSourceProviderList.indexOf(iterator.next()));
		}
		return ids;
	}

	public static List<String> getAlphabetizedNameList() {
		List<String> results = new ArrayList<String>();
		Iterator<DataSourceProvider> iterator = dataSourceProviderList.iterator();
		while (iterator.hasNext()) {
			results.add(iterator.next().getDataSourceName());
		}
		Collections.sort(results);
		return results;
	}

	public static String[] getAlphabetizedNames() {
		String[] results = new String[dataSourceProviderList.size()];
		List<String> nameList = getAlphabetizedNameList();
		for (int i = 0; i < dataSourceProviderList.size(); i++) {
			results[i] = nameList.get(i);
		}
		return results;
	}

	/**
	 * THE FOLLOWING METHOD PRODUCES A DataSourceName DISTINCT FROM ANY IN THE TDB
	 * 
	 * @param proposedNewDataSourceName
	 * @return
	 */
	public static String uniquify(String proposedNewDataSourceName) {
		String uniqueName = proposedNewDataSourceName;
		Pattern numberedEnding = Pattern.compile("(.*)_(\\d+)$");

		while (indexOfDataSourceName(uniqueName) > -1) {
			Matcher endingMatcher = numberedEnding.matcher(uniqueName);

			if (endingMatcher.find()) {
				String baseName = endingMatcher.group(1);
				// System.out.println("Match, so baseName: "+baseName);

				int nextVal = Integer.parseInt(endingMatcher.group(2)) + 1;
				uniqueName = baseName + "_" + nextVal;
				// System.out.println("... and uniqueName is now: "+uniqueName);

			} else {
				uniqueName += "_1";
				// System.out.println("No match, so uniqueName is now: "+uniqueName);
			}
		}
		return uniqueName;
	}

	public static DataSourceProvider get(int index) {
		if (index < 0) {
			return null;
		}
		if (index >= dataSourceProviderList.size()) {
			return null;
		}
		return dataSourceProviderList.get(index);
	}

	public static boolean hasIndex(int index) {
		try {
			dataSourceProviderList.get(index);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static Integer indexOf(DataSourceProvider dataSourceProvider) {
		if (dataSourceProvider == null) {
			return -1;
		}
		return dataSourceProviderList.indexOf(dataSourceProvider);
	}

	public static int indexOfDataSourceName(String name) {
		Iterator<DataSourceProvider> iterator = dataSourceProviderList.iterator();
		while (iterator.hasNext()) {
			DataSourceProvider dataSourceProvider = (DataSourceProvider) iterator.next();
			if (dataSourceProvider.getDataSourceNameString().equals(name)) {
				return DataSourceKeeper.indexOf(dataSourceProvider);
			}
		}
		return -1;
	}

	public static int size() {
		return dataSourceProviderList.size();
	}

	public static int getByTdbResource(Resource tdbResource) {
		Iterator<DataSourceProvider> iterator = dataSourceProviderList.iterator();
		while (iterator.hasNext()) {
			DataSourceProvider dataSourceProvider = iterator.next();
			Resource resource = dataSourceProvider.getTdbResource();
			if (resource.equals(tdbResource)) {
				return dataSourceProviderList.indexOf(dataSourceProvider);
			}
		}
		return -1;
	}

	public static DataSourceProvider get(FileMD fileMD) {
		for (DataSourceProvider dataSourceProvider : dataSourceProviderList) {
			if (dataSourceProvider.getFileMDList().contains(fileMD)) {
				return dataSourceProvider;
			}
		}
		return null;
	}

	public static DataSourceProvider getByName(String name) {
		for (DataSourceProvider dataSourceProvider : dataSourceProviderList) {
			if (dataSourceProvider.getDataSourceName().equals(name)) {
				return dataSourceProvider;
			}
		}
		return null;
	}

	public static int countDataSourcesInTDB() {
		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		b.append("select (count(distinct ?ds) as ?count) \n");
		b.append("where {  \n");
		b.append("  ?s eco:hasDataSource ?ds .  \n");
		b.append("} \n");
		String query = b.toString();

		System.out.println("query = " + query);

		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);

		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		QuerySolution querySolution = resultSet.next();

		RDFNode rdfNode = querySolution.get("count");
		return (rdfNode.asLiteral().getInt());
	}

	public static List<String> getDataSourceNamesInTDB() {
		List<String> currentNames = new ArrayList<String>();
		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		b.append("select distinct ?dataSource \n");
		b.append("where {  \n");
		b.append("  ?s eco:hasDataSource ?ds .  \n");
		b.append("  ?ds rdfs:label ?ds_name . \n");
		b.append("  bind (str(?ds_name) as ?dataSource) \n");
		b.append("} \n");
		String query = b.toString();

		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);

		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			RDFNode rdfNode = querySolution.get("dataSource");
			currentNames.add(rdfNode.asLiteral().getString());
		}
		return currentNames;
	}

	public static Resource createNewDataSet(ExecutionEvent event, String proposedName, Resource dataSetType) {
		String newFileName = null;
		List<String> currentNames = getDataSourceNamesInTDB();
		while (newFileName == null && !currentNames.contains(newFileName)) {
			String[] currentNamesArray = new String[currentNames.size()];
			for (int i = 0; i < currentNames.size(); i++) {
				currentNamesArray[i] = currentNames.get(i);
			}
			GenericStringBox genericStringBox = new GenericStringBox(HandlerUtil.getActiveShell(event), proposedName,
					currentNamesArray);
			genericStringBox.create("Provide New Data Set Name",
					"Recently loaded data is not assigned to a data set.  Please provide a name for this new set.");
			genericStringBox.open();
			newFileName = genericStringBox.getResultString();
		}
		Resource newDataSource = ActiveTDB.tsCreateResource(ECO.DataSource);
		ActiveTDB.tsAddTriple(newDataSource, RDF.type, dataSetType);
		ActiveTDB.tsAddLiteral(newDataSource, RDFS.label, newFileName);
		return newDataSource;
	}

	public static int placeOrphanDataInNewOrphanDataset() {
		int nextOrphanNumber = DataSourceKeeper.getNextOrphanDataSetNumber();
		Resource tempDataSource = ActiveTDB.tsCreateResource(LCAHT.NS + "tempDataSource_" + nextOrphanNumber);
		ActiveTDB.tsAddTriple(tempDataSource, RDF.type, ECO.DataSource);
		ActiveTDB.tsAddTriple(tempDataSource, RDF.type, LCAHT.OrphanDataset);
		ActiveTDB.tsAddLiteral(tempDataSource, RDFS.label, DataSourceKeeper.getOrphanDataDourceNameBase()
				+ nextOrphanNumber);
		return placeOrphanDataInDataset(tempDataSource);
	}

	public static String getTDBDataSourceName(Resource dataSourceResource) {
		String name = null;
		// --- BEGIN SAFE -READ- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.tdbDataset.getDefaultModel();
		try {
			NodeIterator nodeIterator = tdbModel.listObjectsOfProperty(dataSourceResource, RDFS.label);
			RDFNode rdfNode = nodeIterator.next();
			name = rdfNode.asLiteral().getString();
		} catch (Exception e) {
			System.out.println("Import failed with Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		return name;
	}

	public static int placeOrphanDataInDataset(Resource dataSetResource) {
		if (dataSetResource == null) {
			return -1;
		}
		if (!ActiveTDB.getModel().contains(dataSetResource, RDF.type, ECO.DataSource)) {
			return -1;
		}

		String dataSourceName = getTDBDataSourceName(dataSetResource);
		if (dataSourceName == null) {
			return -1;
		}

		int count = 0;
//		String dsName = ActiveTDB.getURIString(dataSetResource);

		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		b.append(" \n");
		b.append("insert  \n");
		b.append("{?s eco:hasDataSource ?newds . } \n");
		b.append(" \n");
		b.append("where { \n");
		b.append("  {select ?newds where { \n ");
		b.append("      ?newds a eco:DataSource . \n ");
		b.append("      ?newds rdfs:label \""+dataSourceName+"\"^^xsd:string . \n ");
		b.append("    } \n ");
		b.append("  } \n ");
		b.append("  {select ?s where {  \n ");
		b.append("    ?s ?p ?o .  \n ");
		b.append("    filter   \n ");
		b.append("      (!exists  \n ");
		b.append("        {?s eco:hasDataSource ?ds . }  \n ");
		b.append("      )   \n ");
		b.append("    }   \n ");
		b.append("  }   \n ");
		b.append("} \n ");
		
		String query = b.toString();
		System.out.println("Query: \n" + query);

		GenericUpdate iGenericUpdate = new GenericUpdate(query, "Temp data source");
		iGenericUpdate.getData();
		count = iGenericUpdate.getAddedTriples().intValue();
		System.out.println("Orphan triples: " + count);
		if (count > 0) {
			DataSourceProvider newDataSource = new DataSourceProvider(dataSetResource);
			// newDataSource.syncFromTDB();
			DataSourceKeeper.add(newDataSource);
		} else {
			ActiveTDB.tsRemoveAllObjects(dataSetResource);
		}
		return count;
	}

//	public static boolean anyOrphans() {
//		StringBuilder b = new StringBuilder();
//		b.append(Prefixes.getPrefixesForQuery());
//		b.append(" \n");
//		b.append("ask  \n");
//		b.append("where { \n");
//		b.append("  ?s ?p ?o . \n");
//		b.append("  ?ds a eco:DataSource . \n");
//		b.append("  filter ( \n");
//		b.append("    (!exists \n");
//		b.append("      {?s eco:hasDataSource ?ds . } \n");
//		b.append("    )  \n");
//		b.append("  ) \n");
//		b.append("} \n");
//		String query = b.toString();
//
//		GenericUpdate iGenericUpdate = new GenericUpdate(query, "Temp data source");
//		iGenericUpdate.getData();
//		int count = iGenericUpdate.getAddedTriples().intValue();
//		System.out.println("Orphan triples: " + count);
//		if (count > 0) {
//			return true;
//		}
//		return false;
//	}

	public static void resolveUnsavedDataSources() {
		int dataSourcesInTDB = countDataSourcesInTDB();
		int dataSourcesInKeeper = dataSourceProviderList.size();
		if (dataSourcesInTDB > dataSourcesInKeeper) {
			syncFromTDB();
		} else if (dataSourcesInTDB < dataSourcesInKeeper) {
			for (DataSourceProvider dataSourceProvider : dataSourceProviderList) {
				if (dataSourceProvider.getTdbResource() != null) {
					// WHAT DOES IT MEAN IF YOU'RE HERE?!?
					ActiveTDB.addTriple(dataSourceProvider.getTdbResource(), RDF.type, ECO.DataSource);
				} else {
					String newDSName = getOrphanDataDourceNameBase() + getNextOrphanDataSetNumber();
					Resource newDSResource = ActiveTDB.createResource(ECO.DataSource);
					ActiveTDB.tsAddTriple(newDSResource, RDFS.label, ActiveTDB.tsCreateTypedLiteral(newDSName)
							.asResource());
				}
			}
		}
	}

	public static void syncFromTDB() {
		Model tdbModel = ActiveTDB.getModel();
		ResIterator iterator = tdbModel.listSubjectsWithProperty(RDF.type, ECO.DataSource);
		while (iterator.hasNext()) {
			Resource dataSourceRDFResource = iterator.next();
			int dataSourceIndex = getByTdbResource(dataSourceRDFResource);
			// NOW SEE IF THE DataSource IS IN THE DataSourceKeeper YET
			System.out.println("another DataSource found in TDB");
			if (dataSourceIndex < 0) {
				System.out.println("... new one");
				new DataSourceProvider(dataSourceRDFResource);
			}
		}
	}
}
