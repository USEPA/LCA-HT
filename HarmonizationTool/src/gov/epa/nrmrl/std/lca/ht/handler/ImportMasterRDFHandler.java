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

public class ImportMasterRDFHandler implements IHandler {
	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		Logger runLogger = Logger.getLogger("run");
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
		DataSourceKeeper.placeOrphanDataInNewOrphanDataset();

		List<String> currentNames = DataSourceKeeper.getDataSourceNamesInTDB();
		int priorDataSetCount = currentNames.size();
		String[] currentNamesArray = new String[priorDataSetCount];
		for (int i = 0; i < priorDataSetCount; i++) {
			currentNamesArray[i] = currentNames.get(i);
		}
		System.out.println("priorDataSetCount = " + priorDataSetCount);

		fileDialog.setFilterExtensions(new String[] { "*.zip;*.n3;*.ttl;*.rdf;*.jsonld;*.json" });

		String status = fileDialog.open();
		System.out.println("status = " + status);
		if (status == null) {
			runLogger.info("# Cancelling Master RDF file read");
			return null;
		}
		String path = fileDialog.getFilterPath();
		runLogger.info("# Read Master RDF data from " + path);

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
					readStreamCountNewDataSources(inputStream, inputType);
					// ActiveTDB.syncTDBtoLCAHT();

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
						int suffixLength = 0;
						if (ze.getName().matches(".*\\.rdf")) {
							inputType = "RDF/XML";
							suffixLength = 4;
						} else if (ze.getName().matches(".*\\.n3")) {
							inputType = "N3";
							suffixLength = 3;
						} else if (ze.getName().matches(".*\\.ttl")) {
							inputType = "TTL";
							suffixLength = 4;
						} else if (ze.getName().matches(".*\\.jsonld")) {
							inputType = "JSON-LD";
							suffixLength = 7;
						} else if (fileName.matches(".*\\.json")) {
							inputType = "JSON-LD";
							suffixLength = 5;
						}
						if (inputType != "SKIP") {
							BufferedReader zipStream = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
							runLogger.info("  # zip file contains: " + ze.getName());
							readBufferCountNewDataSources(zipStream, inputType);
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
			int postDataSetCount = DataSourceKeeper.countDataSourcesInTDB();
			System.out.println("postDataSetCount = " + postDataSetCount);
			if (postDataSetCount == priorDataSetCount) {
				String proposedName = fileList[0];
				Resource newDataSetResource = DataSourceKeeper.createNewDataSet(event, proposedName,
						LCAHT.MasterDataset);
				
				DataSourceKeeper.placeOrphanDataInDataset(newDataSetResource);
			}
			int olcaAdded = OpenLCA.inferOpenLCATriples();
			runLogger.info("  # RDF triples added to openLCA data:  " + olcaAdded);

		}

		return null;
	}

	public static void readStreamCountNewDataSources(InputStream inputStream, String inputType) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.tdbDataset.getDefaultModel();
		try {
			tdbModel.read(inputStream, null, inputType);
			ActiveTDB.tdbDataset.commit();
			// TDB.sync(ActiveTDB.tdbDataset);
		} catch (Exception e) {
			System.out.println("Import failed with Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public static void readBufferCountNewDataSources(BufferedReader zipStream, String inputType) {
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
