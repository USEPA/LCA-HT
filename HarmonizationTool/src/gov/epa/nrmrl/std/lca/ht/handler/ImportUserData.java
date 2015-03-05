package gov.epa.nrmrl.std.lca.ht.handler;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.FileMD;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.dialog.MetaDataDialog;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.FlowContext;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.FlowProperty;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.Flowable;
import gov.epa.nrmrl.std.lca.ht.sparql.GenericUpdate;
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.OpenLCA;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVStrategy;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ImportUserData implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.handler.ImportUserData";
	private static TableProvider tableProvider;
	private static Logger runLogger = Logger.getLogger("run");

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		tableProvider = new TableProvider();

		FileDialog fileDialog = new FileDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), SWT.OPEN);
		fileDialog.setFilterExtensions(new String[] { "*.csv;*.zip;*.n3;*.ttl;*.rdf;*.jsonld;*.json" });
		String inputDirectory = Util.getPreferenceStore().getString("inputDirectory");
		if (inputDirectory.length() > 0) {
			fileDialog.setFilterPath(inputDirectory);
		} else {
			String homeDir = System.getProperty("user.home");
			fileDialog.setFilterPath(homeDir);
		}

		String path = fileDialog.open();
		if (path == null) {
			runLogger.info("# Cancelling CSV file read");
			return null;
		}
		File file = new File(path);
		FileMD fileMD = new FileMD();
		fileMD.setFilename(file.getName());
		fileMD.setPath(path);
		fileMD.setByteCount(file.length());
		fileMD.setModifiedDate(new Date(file.lastModified()));
		Date readDate = new Date();
		fileMD.setReadDate(readDate);
		runLogger.info("# File read at: " + Util.getLocalDateFmt(readDate));
		runLogger.info("# File last modified: " + Util.getLocalDateFmt(new Date(file.lastModified())));
		runLogger.info("# File size: " + file.length());

		System.out.println("All's fine before opening dialog");
		MetaDataDialog dialog = new MetaDataDialog(Display.getCurrent().getActiveShell(), fileMD);
		System.out.println("meta initialized");
		dialog.create();
		System.out.println("meta created");
		boolean thing;
		if (thing = dialog.open() == MetaDataDialog.CANCEL) { // FIXME
			System.out.println("cancel!");

			fileMD.remove();
			return null;
		}
		System.out.println("thing = " + thing);
		System.out.println("Got past opening dialog");
		tableProvider.setFileMD(fileMD);
		System.out.println("FileMD set in tableProvider");

		tableProvider.setDataSourceProvider(dialog.getCurDataSourceProvider());
		System.out.println("DataSource set in tableProvider");

		TableKeeper.saveTableProvider(path, tableProvider);
		System.out.println("Save tableProvider in TableKeeper");

		if (path.matches(".*\\.csv")) {
			loadUserDataFromCSVFile(file);
		} else {
			loadUserDataFromRDFFile(file);
		}

		Date readEndDate = new Date();
		int secondsRead = (int) ((readEndDate.getTime() - readDate.getTime()) / 1000);
		runLogger.info("# File read time (in seconds): " + secondsRead);

		try {
			Util.showView(CSVTableView.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		System.out.println("About to update CSVTableView");
		CSVTableView.update(path);

		return null;
	}

	private static void loadUserDataFromCSVFile(File file) {
		runLogger.info("LOAD CSV " + file.getPath());

		if (!file.exists()) {
			runLogger.warn("# File not found\n");
			return;
		}

		FileReader fileReader = null;
		try {
			fileReader = new FileReader(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (fileReader == null) {
			runLogger.error("# File not readable\n");
			return;
		}

		CSVParser parser = new CSVParser(fileReader, CSVStrategy.EXCEL_STRATEGY);
		// FIXME - IF THE CSV FILE HAS WINDOWS CARRIAGE RETURNS, THE HT DOESN'T SPLIT ON THEM, SO YOU GET ONE ROW, MANY
		// COLUMNS
		String[] values = null;
		try {
			values = parser.getLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (values == null) { // BLANK FILE STILL HAS values (BUT ZERO LENGTH)
			runLogger.warn("# No content in CSV file!");
			return;
		}

		// READ THE FILE NOW
		try {
			while (values != null) {
				DataRow dataRow = initDataRow(values);
				tableProvider.addDataRow(dataRow); // SLOW PROCESS: JUNO FIXME
				values = parser.getLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

	private static void loadUserDataFromRDFFile(File file) {
		// DataSourceKeeper.placeOrphanDataInNewOrphanDataset();
		// List<String> currentNames = DataSourceKeeper.getDataSourceNamesInTDB();
		// int priorDataSetCount = DataSourceKeeper.countDataSourcesInTDB();

		String fileName = file.getName();
		String path = file.getPath();
		Logger runLogger = Logger.getLogger("run");

		runLogger.info("LOAD RDF " + path);

		Map<String, String> fileContents = new HashMap<String, String>();
		// List<String> fileContents = new ArrayList<String>();

		long was = ActiveTDB.getModel(null).size();
		long startTime = System.currentTimeMillis();
		if (!fileName.matches(".*\\.zip")) {
			String inputType = ActiveTDB.getRDFTypeFromSuffix(fileName);
			if (inputType == null) {
				return;
			}
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				boolean fixIDs = false;
				if (inputType.equals("JSON-LD")) {
					fixIDs = true;
				}
				fileContents.put(bufferToString(br, fixIDs), inputType);
				runLogger.info("LOAD RDF " + fileName);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (fileName.matches(".*\\.zip")) {
			try {
				@SuppressWarnings("resource")
				ZipFile zf = new ZipFile(path);
				runLogger.info("LOAD RDF (zip file)" + fileName);

				Enumeration<?> entries = zf.entries();

				while (entries.hasMoreElements()) {
					ZipEntry ze = (ZipEntry) entries.nextElement();
					String zipFileName = ze.getName();
					String inputType = ActiveTDB.getRDFTypeFromSuffix(zipFileName);
					if (inputType == null) {
						continue;
					}
					BufferedReader zipStream = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
					boolean fixIDs = false;
					if (inputType.equals("JSON-LD")) {
						fixIDs = true;
					}
					// fileContents.add(bufferToString(zipStream, fixIDs));
					fileContents.put(bufferToString(zipStream, fixIDs), inputType);

					runLogger.info("LOAD RDF " + ze.getName());
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		readStringsCountNewDataSources(fileContents);
		Resource existingDataSource = null;
		boolean haveOLCAData = false;

		new OpenLCA(); /* This initiates and runs static methods */
		// --- BEGIN SAFE -READ- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model importModel = ActiveTDB.getModel(ActiveTDB.importGraphName);

		if (importModel.contains(null, RDF.type, OpenLCA.Flow)) {
			haveOLCAData = true;
		}

		if (importModel.contains(null, RDF.type, ECO.DataSource)) {
			ResIterator resIterator = importModel.listSubjectsWithProperty(RDF.type, ECO.DataSource);
			while (resIterator.hasNext()) {
				existingDataSource = resIterator.next();
				if (importModel.contains(null, ECO.hasDataSource, existingDataSource)) {
					break;
				}
			}
		}
		ActiveTDB.tdbDataset.end();
		// ---- END SAFE -READ- TRANSACTION ---

		if (haveOLCAData) {
			int olca_convert = OpenLCA.convertOpenLCAToLCAHT(ActiveTDB.importGraphName);
			runLogger.info("  # openLCA data items converted: " + olca_convert);
		}

		if (existingDataSource != null) {
			DataSourceProvider dataSourceProvider = tableProvider.getDataSourceProvider();
			ActiveTDB.tsReplaceLiteral(existingDataSource, RDFS.label, dataSourceProvider.getDataSourceName(),
					ActiveTDB.importGraphName);
			Resource toRemove = dataSourceProvider.getTdbResource();
			dataSourceProvider.setTdbResource(existingDataSource);
			ActiveTDB.tsRemoveGenericTriple(toRemove, null, null, ActiveTDB.importGraphName);
		}

		ActiveTDB.syncTDBtoLCAHT();

		placeContentsInDataset();

		if (haveOLCAData) {
			buildUserDataTableFromOLCADataViaQuery();
		} else {
			buildUserDataTableFromLCAHTDataViaQuery();
		}

		/* TRANSFER DATA TO DEFAULT GRAPH */
//		ActiveTDB.copyImportGraphContentsToDefault();
//		ActiveTDB.clearImportGraphContents();

		ActiveTDB.syncTDBtoLCAHT();

		float elapsedTimeSec = (System.currentTimeMillis() - startTime) / 1000F;
		long now = ActiveTDB.getModel(null).size();
		long change = now - was;
		runLogger.info(fileContents.size() + " files imported in " + elapsedTimeSec + "seconds.");
		runLogger.info("  # RDF triples before: " + NumberFormat.getIntegerInstance().format(was));
		runLogger.info("  # RDF triples after:  " + NumberFormat.getIntegerInstance().format(now));
		runLogger.info("  # RDF triples added:  " + NumberFormat.getIntegerInstance().format(change));

	}

	private static void placeContentsInDataset() {
		DataSourceProvider dataSourceProvider = tableProvider.getDataSourceProvider();
		Resource datasetResource = dataSourceProvider.getTdbResource();

		StringBuilder b = new StringBuilder();
//		b.append(Prefixes.getPrefixesForQuery());
//		b.append("insert {graph <" + ActiveTDB.importGraphName + "> {?s eco:hasDataSource ?ds }} \n");
//		b.append("where { graph <" + ActiveTDB.importGraphName + "> { \n");
//		b.append("  select ?s ?ds \n");
////		b.append("  from <" + ActiveTDB.importGraphName + "> {\n");
//		/* THE ABOVE LINE WORKS IN A SIMPLE QUERY, BUT NOT IN A SUBQUERY IN AN INSERT */
//		b.append("  where { graph <" + ActiveTDB.importGraphName + "> {\n");
//		b.append("    ?ds a eco:DataSource . \n");
//		b.append("    ?ds rdfs:label ?name . \n");
//		b.append("    filter (str(?name) = \"" + datasetName + "\") \n");
//		b.append("    ?s a ?class .  \n");
//		b.append("    filter(!exists{?s eco:hasDataSource ?x }) \n ");
//		b.append("    filter regex (str(?class),\"^http://openlca\")   \n");
//		b.append("  }}} \n");
//		b.append("} \n");

		b.append(Prefixes.getPrefixesForQuery());
		b.append("select ?s ?ds \n");
		b.append("from <" + ActiveTDB.importGraphName + ">\n");
		/* THE ABOVE LINE WORKS IN A SIMPLE QUERY, BUT NOT IN A SUBQUERY IN AN INSERT */
		b.append("where {\n");
		b.append("  ?s a ?class .  \n");
		b.append("  filter(!exists{?s eco:hasDataSource ?x }) \n ");
//		b.append("  filter regex (str(?class),\"^http://openlca\")   \n");
		b.append("} \n");
		String query = b.toString();
		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);
		harmonyQuery2Impl.setGraphName(ActiveTDB.importGraphName);

		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		List<Resource> itemsToAddToDatasource = new ArrayList<Resource>();
		while (resultSet.hasNext()){
			 QuerySolution querySolution = resultSet.next();
			 itemsToAddToDatasource.add(querySolution.get("s").asResource());
		}
		
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(ActiveTDB.importGraphName);
		try {
			for (Resource itemToAdd : itemsToAddToDatasource) {
				tdbModel.add(itemToAdd, ECO.hasDataSource, datasetResource);
			}
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("Assigning openLCA items to DataSource failed with Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		runLogger.info("  # Data items added to dataset: " + itemsToAddToDatasource.size());
	}

	private static void buildUserDataTableFromOLCADataViaQuery() {
		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		b.append("select distinct \n");
		b.append("  ?flowable  \n");
		// b.append("  (fn:substring(str(?f), ?flowable_length - 35) as ?flowable_uuid) \n");
		b.append("  ?cas \n");
		b.append("  ?formula \n");
		b.append("  ?context_general \n");
		b.append("  ?context_specific \n");
		// b.append("  (fn:substring(str(?cat), ?cat_length - 35) as?context_specific_uuid) \n");
		b.append("  ?reference_unit \n");
		// b.append("  (fn:substring(str(?ru), ?ru_length - 35) as?reference_unit_uuid) \n");
		b.append("  ?flow_property \n");
		// b.append("  (fn:substring(str(?fp), ?fp_length - 35) as?flow_property_uuid) \n");
		b.append("from <" + ActiveTDB.importGraphName + ">\n");
		b.append(" \n");
		b.append("where { \n");
		b.append(" \n");
		b.append("  #--- FLOWABLE \n");
		b.append("  ?f olca:flowType olca:ELEMENTARY_FLOW . \n");
		b.append("  ?f olca:name ?flowable . \n");
		b.append("  optional { \n");
		b.append("    ?f olca:cas ?cas . \n");
		b.append("  } \n");
		b.append("  optional { \n");
		b.append("    ?f olca:formula ?formula . \n");
		b.append("  } \n");
		b.append("bind (fn:string-length(str(?f)) as ?flowable_length) \n");
		b.append(" \n");
		b.append("  #--- FLOW CONTEXT \n");
		b.append("  ?f olca:category ?cat . \n");
		b.append("  ?cat olca:name ?context_specific . \n");
		b.append("  ?cat olca:parentCategory ?parentCat . \n");
		b.append("  ?parentCat olca:name ?context_general . \n");
		b.append("  bind (fn:string-length(str(?cat)) as ?cat_length) \n");
		b.append(" \n");
		b.append("  #--- FLOW PROPERTY \n");
		b.append("  {{?f olca:flowPropertyFactors ?fpf } UNION \n"); // OLD SCHEMA
		b.append("  {?f olca:flowProperties ?fpf }} \n"); // SCHEMA AS OF 2015-02-12
		b.append("  ?fpf olca:flowProperty ?fp . \n");
		b.append("  ?fp olca:name ?flow_property . \n");
		b.append("  bind (fn:string-length(str(?fp)) as ?fp_length) \n");
		b.append(" \n");
		b.append("  ?fp olca:unitGroup ?ug . \n");
		b.append("  {{ ?ug olca:referenceUnit ?ru . } UNION \n"); // OLD SCHEMA
		b.append("  { ?ug olca:units ?ru . \n"); // SCHEMA AS OF 2015-02-12
		b.append("  ?ru olca:referenceUnit \"true\"^^xsd:boolean . }} \n");
		b.append("  ?ru olca:name ?reference_unit . \n");
		b.append("  bind (fn:string-length(str(?ru)) as ?ru_length) \n");
		b.append(" \n");
		b.append("} \n");
		b.append("order by ?flowable  \n");

		String query = b.toString();
		System.out.println("Query \n"+query);

		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);
		harmonyQuery2Impl.setGraphName(ActiveTDB.importGraphName);

		ResultSet resultSet = harmonyQuery2Impl.getResultSet();

		tableProvider.createUserData((ResultSetRewindable) resultSet);

		tableProvider.getHeaderRow().add(""); // THIS MAKES THE SIZE OF THE HEADER ROW ONE GREATER TODO: ADD A COLUMN
												// COUNT FIELD TO TABLES

		tableProvider.setLCADataPropertyProvider(1, Flowable.getDataPropertyMap().get(Flowable.flowableNameString));
		tableProvider.setLCADataPropertyProvider(2, Flowable.getDataPropertyMap().get(Flowable.casString));
		tableProvider.setLCADataPropertyProvider(3, Flowable.getDataPropertyMap().get(Flowable.chemicalFormulaString));
		tableProvider.setLCADataPropertyProvider(4, FlowContext.getDataPropertyMap()
				.get(FlowContext.flowContextGeneral));
		tableProvider.setLCADataPropertyProvider(5,
				FlowContext.getDataPropertyMap().get(FlowContext.flowContextSpecific));
		tableProvider.setLCADataPropertyProvider(6, FlowProperty.getDataPropertyMap()
				.get(FlowProperty.flowPropertyUnit));
		tableProvider.setLCADataPropertyProvider(7,
				FlowProperty.getDataPropertyMap().get(FlowProperty.flowPropertyString));
		return;
	}

	private static void buildUserDataTableFromLCAHTDataViaQuery() {
		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		b.append("select distinct \n");
		b.append("  ?flowable  \n");
		b.append("  ?cas \n");
		b.append("  ?formula \n");
		b.append("  ?context_general \n");
		b.append("  ?context_specific \n");
		b.append("  ?unit \n");
		b.append("  ?flow_property \n");
		b.append("from <" + ActiveTDB.importGraphName + ">\n");
		b.append(" \n");
		b.append("where { \n");
		b.append(" \n");
		b.append("  #--- FLOW \n");
		b.append("  ?f a fedlca:Flow . \n");
		b.append("  ?f fedlca:sourceTableRowNumber ?rowNumber . \n");
		b.append("  #--- FLOWABLE \n");
		b.append("  ?f eco:hasFlowable ?flb . \n");
		b.append("  ?flb a eco:Flowable . \n");
		b.append("  ?flb rdfs:label ?flowable . \n");
		b.append("  optional { \n");
		b.append("    ?flb eco:casNumber ?cas . \n");
		b.append("  } \n");
		b.append("  optional { \n");
		b.append("    ?flb eco:chemicalFormula ?formula . \n");
		b.append("  } \n");
		b.append(" \n");
		b.append("  #--- FLOW CONTEXT \n");
		b.append("  optional { \n");
		b.append("    ?f fedlca:hasFlowContext ?cat . \n");
		b.append("    ?cat fedlca:flowContextGeneral ?context_general . \n");
		b.append("    ?cat fedlca:flowContextSpecific ?context_specific . \n");
		b.append("  } \n");
		b.append(" \n");
		b.append("  #--- FLOW PROPERTY \n");
		b.append("  optional { \n");
		b.append("     ?f fedlca:hasFlowProperty ?fp . \n");
		b.append("     ?fp fedlca:flowPropertyUnitString ?unit . \n");
		b.append("  } \n");
		// b.append("  optional { \n");
		// b.append("     ?fp rdfs:label ?flow_property . \n");
		// b.append("  } \n");
		b.append("} \n");
		b.append("order by ?rowNumber  \n");

		String query = b.toString();

		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);
		harmonyQuery2Impl.setGraphName(ActiveTDB.importGraphName);

		ResultSet resultSet = harmonyQuery2Impl.getResultSet();

		tableProvider.createUserData((ResultSetRewindable) resultSet);

		tableProvider.getHeaderRow().add(""); // THIS MAKES THE SIZE OF THE HEADER ROW ONE GREATER TODO: ADD A COLUMN
												// COUNT FIELD TO TABLES

		tableProvider.setLCADataPropertyProvider(1, Flowable.getDataPropertyMap().get(Flowable.flowableNameString));
		tableProvider.setLCADataPropertyProvider(2, Flowable.getDataPropertyMap().get(Flowable.casString));
		tableProvider.setLCADataPropertyProvider(3, Flowable.getDataPropertyMap().get(Flowable.chemicalFormulaString));
		tableProvider.setLCADataPropertyProvider(4, FlowContext.getDataPropertyMap()
				.get(FlowContext.flowContextGeneral));
		tableProvider.setLCADataPropertyProvider(5,
				FlowContext.getDataPropertyMap().get(FlowContext.flowContextSpecific));
		tableProvider.setLCADataPropertyProvider(6, FlowProperty.getDataPropertyMap()
				.get(FlowProperty.flowPropertyUnit));
		tableProvider.setLCADataPropertyProvider(7,
				FlowProperty.getDataPropertyMap().get(FlowProperty.flowPropertyString));
		return;
	}

	private static String bufferToString(BufferedReader bufferedReader, boolean fixIDs) {
		String pattern = "(\\@id\":)(\\d+)";
		StringBuilder stringBuilder = new StringBuilder();
		String line;
		try {
			line = bufferedReader.readLine();
			while (line != null) {
				String newLine = line;
				if (fixIDs) {
					newLine = line.replaceAll(pattern, "$1" + "\"" + "$2" + "\"");
				}
				stringBuilder.append(newLine);
				stringBuilder.append(System.lineSeparator());
				line = bufferedReader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stringBuilder.toString();
	}

	private static void readStringsCountNewDataSources(Map<String, String> fileContentsList) {
		// int before = DataSourceKeeper.countDataSourcesInTDB();
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(ActiveTDB.importGraphName);
		String failedString = "";
		try {
			for (String fileContents : fileContentsList.keySet()) {
				failedString = fileContents;
				String inputType = fileContentsList.get(fileContents);
				ByteArrayInputStream stream = new ByteArrayInputStream(fileContents.getBytes());
				tdbModel.read(stream, "http://openlca.org/schema/v1.0/", inputType);
				/*
				 * The central argument is the @base. TODO: check if other values may become necessary if data without
				 * fully qualified URIs is to be read in
				 */
			}
			// tdbModel.read(inputStream, null, inputType);
			ActiveTDB.tdbDataset.commit();
			// TDB.sync(ActiveTDB.tdbDataset);
		} catch (Exception e) {
			System.out.println("Import failed with Exception: " + e);
			System.out.println("The failing string was: \n" + failedString);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	private static DataRow initDataRow(String[] values) {
		DataRow dataRow = new DataRow();
		for (String s : values) {
			dataRow.add(s);
		}
		return dataRow;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
	}

}
