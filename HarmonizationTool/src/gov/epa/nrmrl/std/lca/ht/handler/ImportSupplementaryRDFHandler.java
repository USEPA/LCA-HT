package gov.epa.nrmrl.std.lca.ht.handler;

import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dialog.GenericStringBox;
import gov.epa.nrmrl.std.lca.ht.sparql.GenericUpdate;
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;
import gov.epa.nrmrl.std.lca.ht.vocabulary.OpenLCA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ImportSupplementaryRDFHandler implements IHandler {
	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		Logger runLogger = Logger.getLogger("run");
		// System.out.println("executing TDB load");
		if (ActiveTDB.getModel() == null) {
			return null;
		}
		FileDialog fileDialog = new FileDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), SWT.OPEN
				| SWT.MULTI);
		String inputDirectory = Util.getPreferenceStore().getString("inputDirectory");
		if (inputDirectory.length() > 0) {
			fileDialog.setFilterPath(inputDirectory);
		} else {
			String homeDir = System.getProperty("user.home");
			fileDialog.setFilterPath(homeDir);
		}

		// ------------------------------
		DataSourceKeeper.placeOrphanDataInNewOrphanDataset();
		// Resource tempDataSource = ActiveTDB.tsCreateResource(LCAHT.NS + "tempDataSource");
		// ActiveTDB.tsAddTriple(tempDataSource, RDF.type, ECO.DataSource);
		// ActiveTDB.tsAddLiteral(tempDataSource, RDFS.label, "(LCA-HT default dataset)");
		//
		// StringBuilder b = new StringBuilder();
		// b.append(Prefixes.getPrefixesForQuery());
		//
		// // b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		// // b.append("PREFIX  lcaht:  <http://epa.gov/nrmrl/std/lca/ht/1.0#> \n");
		// // b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		// b.append(" \n");
		// b.append("insert  \n");
		// b.append("{?s eco:hasDataSource lcaht:tempDataSource . } \n");
		// b.append(" \n");
		// b.append("where { \n");
		// b.append("  ?s ?p ?o . \n");
		// b.append("  filter ( \n");
		// b.append("    (!exists \n");
		// b.append("      {?s eco:hasDataSource ?ds . } \n");
		// b.append("    )  \n");
		// b.append("    &&  \n");
		// b.append("    (!isBlank(?s)) \n");
		// b.append("  ) \n");
		// b.append("} \n");
		// String query = b.toString();
		//
		// GenericUpdate iGenericUpdate = new GenericUpdate(query, "Temp data source");
		// iGenericUpdate.getData();
		// iGenericUpdate.getQueryResults();

		// ------------------------------
		List<String> currentNames = new ArrayList<String>();
		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		// b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		// b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#>  \n");
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
		int priorDataSetCount = currentNames.size();
		String[] currentNamesArray = new String[priorDataSetCount];
		for (int i = 0; i < priorDataSetCount; i++) {
			currentNamesArray[i] = currentNames.get(i);
		}
		System.out.println("priorDataSetCount = " + priorDataSetCount);

		// ------------------------------

		fileDialog.setFilterExtensions(new String[] { "*.zip;*.n3;*.ttl;*.rdf;*.jsonld;*.json" });
		// fileDialog.setFilterExtensions(new String[] { "*.zip;*.n3;*.ttl;*.rdf;" });
		// SHOWS ALL TYPES IN ONE WINDOW

		String status = fileDialog.open();
		System.out.println("status = " + status);
		if (status == null) {
			runLogger.info("# Cancelling Supplementary RDF file read");
			return null;
		}
		String path = fileDialog.getFilterPath();
		runLogger.info("# Read Supplementary RDF data from " + path);

		String[] fileList = fileDialog.getFileNames();

		// String sep = System.getProperty("file.separator");
		String sep = File.separator;

		// System.out.println("path= " + path);
		// IRIResolver thing = IRIResolver.create("http://openlca.org/schema/v1.0/");

		for (String fileName : fileList) {
			System.out.println("fileName= " + fileName);
			if (!fileName.startsWith(sep)) {
				fileName = path + sep + fileName;
			}

			long was = ActiveTDB.getModel().size();
			long startTime = System.currentTimeMillis();
			if (!fileName.matches(".*\\.zip")) {
				try {
					String inputType = "SKIP";
					if (fileName.matches(".*\\.rdf")) {
						inputType = "RDF/XML";
					} else if (fileName.matches(".*\\.n3")) {
						inputType = "N3";
					} else if (fileName.matches(".*\\.ttl")) {
						inputType = "TTL";
					} else if (fileName.matches(".*\\.jsonld")) {
						inputType = "JSON-LD";
					} else if (fileName.matches(".*\\.json")) {
						inputType = "JSON-LD";
					}
					InputStream inputStream = new FileInputStream(fileName);
					runLogger.info("LOAD RDF " + fileName);

					// --- BEGIN SAFE -WRITE- TRANSACTION ---
					ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
					Model tdbModel = ActiveTDB.tdbDataset.getDefaultModel();
					try {
						tdbModel.read(inputStream, null, inputType);
						ActiveTDB.tdbDataset.commit();
						TDB.sync(ActiveTDB.tdbDataset);
					} catch (Exception e) {
						System.out.println("Import failed with Exception: " + e);
						ActiveTDB.tdbDataset.abort();
					} finally {
						ActiveTDB.tdbDataset.end();
					}
					// ---- END SAFE -WRITE- TRANSACTION ---
					ActiveTDB.syncTDBtoLCAHT();

				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (fileName.matches(".*\\.zip.*")) {
				try {
					@SuppressWarnings("resource")
					ZipFile zf = new ZipFile(fileName);
					runLogger.info("LOAD RDF (zip file)" + fileName);

					Enumeration<?> entries = zf.entries();
					while (entries.hasMoreElements()) {
						ZipEntry ze = (ZipEntry) entries.nextElement();
						String inputType = "SKIP";
						if (ze.getName().matches(".*\\.rdf")) {
							inputType = "RDF/XML";
						} else if (ze.getName().matches(".*\\.n3")) {
							inputType = "N3";
						} else if (ze.getName().matches(".*\\.ttl")) {
							inputType = "TTL";
						} else if (ze.getName().matches(".*\\.jsonld")) {
							inputType = "JSON-LD";
						} else if (fileName.matches(".*\\.json")) {
							inputType = "JSON-LD";
						}
						if (inputType != "SKIP") {
							// System.out.println("Adding data from " + inputType + " zipped file:" + ze.getName());
							BufferedReader zipStream = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
							runLogger.info("  # zip file contains: " + ze.getName());

							// --- BEGIN SAFE -WRITE- TRANSACTION ---
							ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
							Model tdbModel = ActiveTDB.tdbDataset.getDefaultModel();
							try {
								tdbModel.read(zipStream, null, inputType);
								ActiveTDB.tdbDataset.commit();
								// TDB.sync(ActiveTDB.tdbDataset);
							} catch (Exception e) {
								System.out.println("Import failed; see strack trace!\n" + e);
								ActiveTDB.tdbDataset.abort();
							} finally {
								ActiveTDB.tdbDataset.end();
							}
							// ---- END SAFE -WRITE- TRANSACTION ---

							ActiveTDB.syncTDBtoLCAHT();
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			float elapsedTimeSec = (System.currentTimeMillis() - startTime) / 1000F;
			System.out.println("Time elapsed: " + elapsedTimeSec);
			long now = ActiveTDB.getModel().size();
			long change = now - was;
			System.out.println("Was:" + was + " Added:" + change + " Now:" + now);
			runLogger.info("  # RDF triples before: " + NumberFormat.getIntegerInstance().format(was));
			runLogger.info("  # RDF triples after:  " + NumberFormat.getIntegerInstance().format(now));
			runLogger.info("  # RDF triples added:  " + NumberFormat.getIntegerInstance().format(change));
		}

		StringBuilder b2 = new StringBuilder();
		b2.append(Prefixes.getPrefixesForQuery());
		// b2.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		// b2.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#>  \n");
		b2.append("select (count(distinct ?ds) as ?count) \n");
		b2.append("where {  \n");
		b2.append("  ?s eco:hasDataSource ?ds .  \n");
		b2.append("} \n");
		String query2 = b2.toString();

		System.out.println("query2 = " + query2);

		HarmonyQuery2Impl harmonyQuery2Impl2 = new HarmonyQuery2Impl();
		harmonyQuery2Impl2.setQuery(query2);

		ResultSet resultSet2 = harmonyQuery2Impl2.getResultSet();
		QuerySolution querySolution2 = resultSet2.next();

		RDFNode rdfNode = querySolution2.get("count");
		int postDataSetCount = (int) rdfNode.asLiteral().getInt();
		System.out.println("postDataSetCount = " + postDataSetCount);

		if (postDataSetCount == priorDataSetCount) {
			// NEW DATA DID NOT HAVE A DATA SOURCE
			String newFileName = null;
			while (newFileName == null && !currentNames.contains(newFileName)) {
				GenericStringBox genericStringBox = new GenericStringBox(HandlerUtil.getActiveShell(event),
						"(new data set)", currentNamesArray);
				genericStringBox.create("Name Data Set", "Please type a new data set name for this Supplementary Data Set");
				genericStringBox.open();
				newFileName = genericStringBox.getResultString();
			}
			Resource newDataSource = ActiveTDB.tsCreateResource(ECO.DataSource);
			ActiveTDB.tsAddTriple(newDataSource, RDF.type, LCAHT.SupplementaryReferenceDataset);
			ActiveTDB.tsAddLiteral(newDataSource, RDFS.label, newFileName);

			b = new StringBuilder();
			b.append(Prefixes.getPrefixesForQuery());

			// b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
			// b.append("PREFIX  lcaht:  <http://epa.gov/nrmrl/std/lca/ht/1.0#> \n");
			// b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
			// b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");

			b.append(" \n");
			b.append("insert  \n");
			b.append("{?s eco:hasDataSource ?newds . } \n");
			b.append(" \n");
			b.append("where { \n");
			b.append("  ?newds a eco:DataSource . \n");
			b.append("  ?newds rdfs:label \"" + newFileName + "\"^^xsd:string . \n");
			b.append("  ?s ?p ?o . \n");
			b.append("  filter ( \n");
			b.append("    (!exists \n");
			b.append("      {?s eco:hasDataSource ?ds . } \n");
			b.append("    )  \n");
			// b.append("    &&  \n");
			// b.append("    (!isBlank(?s)) \n");
			b.append("  ) \n");
			b.append("} \n");
			query = b.toString();

			GenericUpdate iGenericUpdate = new GenericUpdate(query, "Temp data source");
			iGenericUpdate.getData();
			int added = iGenericUpdate.getAddedTriples().intValue();
			runLogger.info("# " + added + " triples were added assigning new data to data set " + newFileName);

			int olcaInferrences = OpenLCA.inferOpenLCATriples();
			runLogger.info("# " + olcaInferrences + " triples were added assigning openLCA data types");

			// iGenericUpdate.getQueryResults();
		} else if (postDataSetCount - priorDataSetCount > 1) {
			// NEW DATA HAD MULTIPLE DATA SOURCES
		} else {
			// NEW DATA HAD 1 DATA SOURCE (BECAUSE THERE WILL NOT BE LESS?!?)
		}
		return null;
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
