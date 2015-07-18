package gov.epa.nrmrl.std.lca.ht.handler;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataFormatCheck.FormatCheck;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.FileMD;
import gov.epa.nrmrl.std.lca.ht.dataModels.Flow;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.dialog.MetaDataDialog;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.FlowContext;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.FlowUnit;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.Flowable;
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Temporal;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.OpenLCA;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.ArrayList;
//import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Literal;
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

	private Display display = null;

	public static class RunData implements Runnable {
		String path = null;
		File file = null;
		FileMD fileMD = null;
		MetaDataDialog dialog = null;
//		Calendar readDate = GregorianCalendar.getInstance();
		Date readDate = new Date();
		ImportUserData importCommand;
		Display display = null;

		public RunData(ImportUserData data) {
			importCommand = data;
		}

		@Override
		public void run() {
			importCommand.finishImport(this);

		}
	}

	private static void updateText(final StyledText target, final String message) {
		try {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					target.setText(message);

				}
			});
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		synchronized (Util.getInitLock()) {
			RunData data = new RunData(this);
			data.display = Display.getCurrent();
			FlowsWorkflow.btnLoadUserData.setEnabled(false);

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

			data.path = fileDialog.open();
			if (data.path == null) {
				FlowsWorkflow.btnLoadUserData.setEnabled(true);
				runLogger.info("# Cancelling CSV file read");
				return null;
			}
			data.file = new File(data.path);
			data.fileMD = new FileMD(true);
			data.fileMD.setFilename(data.file.getName());
			data.fileMD.setPath(data.path);
			data.fileMD.setByteCount(data.file.length());
			Date modifiedDate = new Date(data.file.lastModified());
			data.fileMD.setModifiedDate(modifiedDate);
			Date readDate = new Date();
			data.fileMD.setReadDate(readDate);

//			data.fileMD.setModifiedDate(new Date(data.file.lastModified()));
//			data.readDate = new Date();
//			data.fileMD.setReadDate(data.readDate);
			runLogger.info("# File read at: " + Temporal.getLocalDateFmt(data.readDate));
			long time = data.file.lastModified();
			Date date = new Date(time);
//			calednar.setTimeInMillis(time);
			runLogger.info("# File last modified: " + Temporal.getLocalDateFmt(date));
			runLogger.info("# File size: " + data.file.length());

			System.out.println("All's fine before opening dialog");
			data.dialog = new MetaDataDialog(Display.getCurrent().getActiveShell(), data.fileMD);
			System.out.println("meta initialized");
			data.dialog.create();
			System.out.println("meta created");
			if (data.dialog.open() == MetaDataDialog.CANCEL) {
				System.out.println("cancel!");
				FlowsWorkflow.btnLoadUserData.setEnabled(true);

				data.fileMD.remove();
				return null;
			}
			FlowsWorkflow.textLoadUserData.setText("... loading ...");
			FlowsWorkflow.textLoadUserData.setToolTipText("... loading ...");
			new Thread(data).start();
		}
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

		try {
			CSVParser parser = new CSVParser(fileReader, CSVFormat.EXCEL);
			//CSVParser parser = new CSVParser(fileReader, CSVStrategy.EXCEL_STRATEGY);
			// FIXME - IF THE CSV FILE HAS WINDOWS CARRIAGE RETURNS, THE HT DOESN'T SPLIT ON THEM, SO YOU GET ONE ROW, MANY
			// COLUMNS
			/*String[] values = null;
			try {
				parser.getRecordNumber();
				values = parser.getLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (values == null) { // BLANK FILE STILL HAS values (BUT ZERO LENGTH)
	
			}*/
			
			 for (CSVRecord csvRecord : parser) {		 
					DataRow dataRow = initDataRow(csvRecord);
					tableProvider.addDataRow(dataRow); // SLOW PROCESS: JUNO FIXME
			}
			parser.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		if (tableProvider.getData().size() == 0) {
			runLogger.warn("# No content in CSV file!");
			return;
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

		// runLogger.info("LOAD RDF " + path + " " + new Date());

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
				runLogger.info("LOAD RDF " + fileName + " " + new Date());
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (fileName.matches(".*\\.zip")) {
			try {
				@SuppressWarnings("resource")
				ZipFile zf = new ZipFile(path);
				runLogger.info("LOAD RDF (zip file)" + fileName + " " + new Date());
				int size = zf.size();
				int i = 0;
				int percent = 0;

				Enumeration<?> entries = zf.entries();

				while (entries.hasMoreElements()) {
					int newPercent = ++i * 100 / size;
					if (percent != newPercent) {
						percent = newPercent;
						updateText(FlowsWorkflow.textLoadUserData, "1/4 Loading: " + percent + "%");

					}
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

					// runLogger.info("LOAD RDF " + ze.getName());
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

		// if (haveOLCAData) {
		// int olca_convert = OpenLCA.convertOpenLCAToLCAHT(ActiveTDB.importGraphName);
		// runLogger.info("  # openLCA data items converted: " + olca_convert);
		// }

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
		ActiveTDB.copyImportGraphContentsToDefault();
		ActiveTDB.clearImportGraphContents();

		runLogger.info("Syncing TDB to LCAHT " + new Date());
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
		// b.append(Prefixes.getPrefixesForQuery());
		// b.append("insert {graph <" + ActiveTDB.importGraphName + "> {?s eco:hasDataSource ?ds }} \n");
		// b.append("where { graph <" + ActiveTDB.importGraphName + "> { \n");
		// b.append("  select ?s ?ds \n");
		// // b.append("  from <" + ActiveTDB.importGraphName + "> {\n");
		// /* THE ABOVE LINE WORKS IN A SIMPLE QUERY, BUT NOT IN A SUBQUERY IN AN INSERT */
		// b.append("  where { graph <" + ActiveTDB.importGraphName + "> {\n");
		// b.append("    ?ds a eco:DataSource . \n");
		// b.append("    ?ds rdfs:label ?name . \n");
		// b.append("    filter (str(?name) = \"" + datasetName + "\") \n");
		// b.append("    ?s a ?class .  \n");
		// b.append("    filter(!exists{?s eco:hasDataSource ?x }) \n ");
		// b.append("    filter regex (str(?class),\"^http://openlca\")   \n");
		// b.append("  }}} \n");
		// b.append("} \n");

		b.append(Prefixes.getPrefixesForQuery());
		b.append("select ?s ?class\n");
		b.append("from <" + ActiveTDB.importGraphName + ">\n");
		/* THE ABOVE LINE WORKS IN A SIMPLE QUERY, BUT NOT IN A SUBQUERY IN AN INSERT */
		b.append("where {\n");
		b.append("  ?s a ?class .  \n");
		b.append("  filter(!exists{?s eco:hasDataSource ?x }) \n ");
		// b.append("  filter regex (str(?class),\"^http://openlca\")   \n");
		b.append("} \n");
		String query = b.toString();
		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);
		harmonyQuery2Impl.setGraphName(ActiveTDB.importGraphName);

		updateText(FlowsWorkflow.textLoadUserData, "3/4 Dataset");
		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		List<Resource> itemsToAddToDatasource = new ArrayList<Resource>();
		List<Resource> flowsToTagWithUUID = new ArrayList<Resource>();

		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			Resource item = querySolution.get("s").asResource();
			itemsToAddToDatasource.add(item);
			Resource rdfClass = querySolution.get("class").asResource();
			if (rdfClass.equals(OpenLCA.Flow)) {
				flowsToTagWithUUID.add(item);
			}
		}

		Pattern uuidCheckPattern = FormatCheck.getUUIDCheck().get(0).getPattern();
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(ActiveTDB.importGraphName);
		try {
			for (Resource itemToAdd : itemsToAddToDatasource) {
				tdbModel.add(itemToAdd, ECO.hasDataSource, datasetResource);
			}
			for (Resource flowResource : flowsToTagWithUUID) {
				String uriFull = flowResource.getURI();
				String uuidCandidate = uriFull.substring(uriFull.length() - 36);
				Matcher uuidCheck = uuidCheckPattern.matcher(uuidCandidate);
				if (uuidCheck.find()) {
					Literal uuidLiteral = tdbModel.createTypedLiteral(uuidCandidate);
					tdbModel.add(flowResource, FedLCA.hasOpenLCAUUID, uuidLiteral);
				} else {
					System.out.println("No match for " + uuidCandidate);
				}
			}
			ActiveTDB.tdbDataset.commit();
			// runLogger.info(" Finished adding items to datasource " + new Date());
		} catch (Exception e) {
			System.out.println("Assigning openLCA items to DataSource failed with Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
		runLogger.info("  # Data items added to dataset: " + itemsToAddToDatasource.size());
	}

	public static void buildUserDataTableFromOLCADataViaQuery() {
		buildUserDataTableFromOLCADataViaQuery(null, tableProvider);
	}

	public static void buildUserDataTableFromOLCADataViaQuery(String dataSourceName, TableProvider tblProvider) {
		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		if (dataSourceName != null) {
			b.append("select \n ");
			b.append("  ?flowable_uuid \n");
			b.append("  ?flowable \n");
			b.append("  ?cas \n");
			b.append("  ?formula \n");
			b.append("  ?context_general \n");
			b.append("  ?context_specific \n");
			b.append("  ?reference_unit \n");
			b.append("  ?flow_property \n");
			b.append("  ?lcaflowable \n");
			b.append("  ?flowCtx \n");
			b.append("  ?flowUnit \n");
			b.append("  ?mf \n");
			b.append("  (count (?ahs) as ?adhoc) \n");
			b.append("  (count (?cp) as ?flowableMatch) \n");
			b.append("  (count (?ctxMatch) as ?contextMatch) \n");
			b.append("  (count (?unMatch) as ?unitMatch) \n");
			b.append("where { {");
		}
		b.append("select distinct \n");
		b.append("  (fn:substring(str(?f), ?flowable_length - 35) as ?flowable_uuid) \n");
		b.append("  ?flowable  \n");
		b.append("  ?cas \n");
		b.append("  ?formula \n");
		b.append("  ?context_general \n");
		b.append("  ?context_specific \n");
		// b.append("  (fn:substring(str(?cat), ?cat_length - 35) as?context_specific_uuid) \n");
		b.append("  ?reference_unit \n");
		// b.append("  (fn:substring(str(?ru), ?ru_length - 35) as?reference_unit_uuid) \n");
		b.append("  ?flow_property \n");
		// b.append("  (fn:substring(str(?fp), ?fp_length - 35) as?flow_property_uuid) \n");
		if (dataSourceName != null) {
			b.append("  ?lcaflowable \n");
			b.append("  ?flowCtx \n");
			b.append("  ?flowUnit \n");
			b.append("  ?ahs \n"); 
			b.append("  ?cp \n");
			b.append("  ?mf \n");
			b.append("  ?ctxMatch \n");
			b.append("  ?unMatch \n");
		}
		else
			b.append("from <" + ActiveTDB.importGraphName + ">\n");
		b.append(" \n");
		b.append("where { \n");
		b.append(" \n");
		b.append("  #--- FLOWABLE \n");
		if (dataSourceName != null) {
			b.append("  ?ds rdfs:label \"" + dataSourceName + "\"^^xsd:string .\n");
			b.append("  ?f eco:hasDataSource ?ds . \n");
		}
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
		b.append("  optional { \n");
		b.append("    ?cat olca:parentCategory ?parentCat . \n");
		b.append("    ?parentCat olca:name ?context_general . \n");
		b.append("  } \n");
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
		if ( dataSourceName != null) {
			b.append("  optional { \n");
			//b.append("    ?ds rdfs:label \"" + dataSourceName + "\"^^xsd:string .\n");
			b.append("    ?f eco:hasDataSource ?ds . \n");
			b.append("    ?f lcaht:hasQCStatus ?ahs   . \n");
			b.append("    FILTER( ?ahs = lcaht:QCStatusAdHocMaster) . \n");
			b.append("  } \n");
			b.append("  optional { \n");
			//b.append("    ?ds rdfs:label \"" + dataSourceName + "\"^^xsd:string .\n");
			b.append("    ?lcaflow eco:hasDataSource ?ds . \n");
			b.append("    ?f fedlca:hasOpenLCAUUID ?lcid . \n");
			b.append("    ?lcaflow fedlca:hasOpenLCAUUID ?lcid . \n");
			b.append("    ?lcaflow eco:hasFlowable ?lcaflowable . \n");
			b.append("    optional { \n");
			b.append("      ?cp fedlca:comparedSource ?lcaflowable . \n");
			b.append("      ?cp fedlca:comparedEquivalence fedlca:Equivalent \n");
			b.append("    } \n");
			b.append("    optional { \n");
			b.append("      ?lcaflow fedlca:hasFlowContext ?flowCtx . \n");
			b.append("      optional { \n");
			b.append("        ?flowCtx owl:sameAs ?ctxMatch . \n");
			b.append("      } \n");
			b.append("    } \n");
			b.append("    optional { \n");
			b.append("      ?lcaflow fedlca:hasFlowUnit ?flowUnit . \n");
			b.append("      optional { \n");
			b.append("        ?flowUnit owl:sameAs ?unMatch \n");
			b.append("      } \n");
			b.append("    } \n");
			b.append("    optional { \n");
			b.append("      ?lcaflow owl:sameAs ?mf \n");
			b.append("    } \n");
			b.append("  } \n");
		}
		b.append("} \n");
		if (dataSourceName != null) {
			b.append("} } \n");
			b.append(" group by ?flowable_uuid ?flowable ?cas ?formula ?context_general ?context_specific ?reference_unit ?flow_property ?lcaflowable ?flowCtx ?flowUnit ?mf \n");
		}
		b.append("order by ?flowable \n");

		String query = b.toString();
		System.out.println("Query \n" + query);

		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);
		if (dataSourceName == null)
			harmonyQuery2Impl.setGraphName(ActiveTDB.importGraphName);

		// runLogger.info("querying current user data " + new Date());
		if (dataSourceName == null)
			updateText(FlowsWorkflow.textLoadUserData, "4/4 Building table");
		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		
		
		if (dataSourceName != null)
			tblProvider.setExistingData(dataSourceName != null);

		// runLogger.info("adding user data to table " + new Date());
		tblProvider.createUserData((ResultSetRewindable) resultSet);

		tblProvider.getHeaderRow().add(""); // THIS MAKES THE SIZE OF THE HEADER ROW ONE GREATER TODO: ADD A COLUMN
												// COUNT FIELD TO TABLES

		tblProvider.setLCADataPropertyProvider(1, Flow.getDataPropertyMap().get(Flow.openLCAUUID));
		tblProvider.setLCADataPropertyProvider(2, Flowable.getDataPropertyMap().get(Flowable.flowableNameString));
		tblProvider.setLCADataPropertyProvider(3, Flowable.getDataPropertyMap().get(Flowable.casString));
		tblProvider.setLCADataPropertyProvider(4, Flowable.getDataPropertyMap().get(Flowable.chemicalFormulaString));
		tblProvider.setLCADataPropertyProvider(5, FlowContext.getDataPropertyMap()
				.get(FlowContext.flowContextGeneral));
		tblProvider.setLCADataPropertyProvider(6,
				FlowContext.getDataPropertyMap().get(FlowContext.flowContextSpecific));
		tblProvider.setLCADataPropertyProvider(7, FlowUnit.getDataPropertyMap().get(FlowUnit.flowUnitString));
		tblProvider.setLCADataPropertyProvider(8, FlowUnit.getDataPropertyMap().get(FlowUnit.flowPropertyString));
		
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
		b.append("    ?flb fedlca:hasFormattedCAS ?cas . \n");
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
		b.append("     ?f fedlca:hasFlowUnit ?fu . \n");
		b.append("     ?fu fedlca:flowPropertyUnitString ?unit . \n");
		b.append("     ?fp fedlca:hasFlowUnit ?fu . \n");
		b.append("     ?fp rdfs:label ?flow_property . \n");
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
		tableProvider.setLCADataPropertyProvider(6, FlowUnit.getDataPropertyMap().get(FlowUnit.flowUnitString));
		tableProvider.setLCADataPropertyProvider(7, FlowUnit.getDataPropertyMap().get(FlowUnit.flowPropertyString));
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
		int size = fileContentsList.size();
		int i = 0;
		int percent = 0;
		try {
			for (String fileContents : fileContentsList.keySet()) {
				int newPercent = ++i * 100 / size;
				if (percent != newPercent) {
					percent = newPercent;
					updateText(FlowsWorkflow.textLoadUserData, "2/4 Importing: " + percent + "%");

				}
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

	private static DataRow initDataRow(CSVRecord record) {
		DataRow dataRow = new DataRow();
		for (String s : record) {
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

	public void finishImport(final RunData data) {

		tableProvider.setFileMD(data.fileMD);
		tableProvider.setDataSourceProvider(data.dialog.getCurDataSourceProvider());
		TableKeeper.saveTableProvider(data.path, tableProvider);

		if (data.path.matches(".*\\.csv")) {
			loadUserDataFromCSVFile(data.file);
		} else {
			loadUserDataFromRDFFile(data.file);
		}

		Date readEndDate = new Date();
		System.out.println("readEndDate.getTime() "+readEndDate.getTime());
		Date readDate = data.fileMD.getReadDate();
		System.out.println("data.readDate.getTime() "+readDate.getTime());

		long secondsRead = (readEndDate.getTime() - data.readDate.getTime())/1000 ; 
//				- data.readDate.getTime()) / 1000);
		runLogger.info("# File read time (in seconds): " + secondsRead);
		// display.readAndDispatch();
		displayTableView(data);
	}

	public static void displayTableView(final RunData data) {
		data.display.wake();

		// This has to be done in a UI thread, otherwise we get NPEs when we try to access the active window
		// final Display display = ImportUserData.currentDisplay;
		/*
		 * new Thread() { public void run() { //display.async
		 */
		data.display.syncExec(new Runnable() {
			public void run() {
				try {
					Util.showView(CSVTableView.ID);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
				System.out.println("About to update CSVTableView");
				CSVTableView.update(data.path);

				FlowsWorkflow.btnLoadUserData.setEnabled(true);

				String key = CSVTableView.getTableProviderKey();
				if (key == null) {
					System.out.println("The CSVTableView does not have a table!");
					FlowsWorkflow.textLoadUserData.setText("");
					FlowsWorkflow.textLoadUserData.setToolTipText("");
				} else {
					FlowsWorkflow.btnConcludeFile.setEnabled(true);

					FlowsWorkflow.textLoadUserData.setText(TableKeeper
							.getTableProvider(CSVTableView.getTableProviderKey()).getFileMD().getFilename());
					FlowsWorkflow.textLoadUserData.setToolTipText(TableKeeper
							.getTableProvider(CSVTableView.getTableProviderKey()).getFileMD().getPath());
					FlowsWorkflow.btnCheckData.setEnabled(true);
					System.out.println("About to do setHeaderInfo()");
				}
				FlowContext.loadMasterFlowContexts(); /* THERE MAY BE A BETTER TIME TO DO THIS */
			}
		});
		// }
		// }.start();

	}

}
