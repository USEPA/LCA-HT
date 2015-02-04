package gov.epa.nrmrl.std.lca.ht.handler;

import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceKeeper;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;
import gov.epa.nrmrl.std.lca.ht.vocabulary.OpenLCA;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class ImportMasterRDFHandler implements IHandler {

	private static boolean shouldInferOLCAtriples = false;

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
		DataSourceKeeper.placeOrphanDataInNewOrphanDataset();

		for (String fileName : fileList) {
			System.out.println("fileName= " + fileName);
			if (!fileName.startsWith(sep)) {
				fileName = path + sep + fileName;
				File file = new File(fileName);

				List<String> currentNames = DataSourceKeeper.getDataSourceNamesInTDB();
				int priorDataSetCount = currentNames.size();
				String[] currentNamesArray = new String[priorDataSetCount];
				for (int i = 0; i < priorDataSetCount; i++) {
					currentNamesArray[i] = currentNames.get(i);
				}

				shouldInferOLCAtriples = false;
				loadDataFromRDFFile(file);
				if (shouldInferOLCAtriples) {
					int olcaAdded = OpenLCA.inferOpenLCATriples();
					runLogger.info("  # RDF triples added to openLCA data:  "
							+ NumberFormat.getIntegerInstance().format(olcaAdded));
				}
				List<String> newNames = DataSourceKeeper.getDataSourceNamesInTDB();
				if (newNames.size() == currentNames.size()) {
					String proposedName = fileList[0];
					Resource newDataSetResource = DataSourceKeeper.createNewDataSet(event, proposedName,
							LCAHT.MasterDataset);

					DataSourceKeeper.placeOrphanDataInDataset(newDataSetResource);
				}

			}

			long was = ActiveTDB.getModel().size();
			long startTime = System.currentTimeMillis();

			float elapsedTimeSec = (System.currentTimeMillis() - startTime) / 1000F;
			System.out.println("Time elapsed: " + elapsedTimeSec);
			long now = ActiveTDB.getModel().size();
			long change = now - was;
			System.out.println("Was:" + was + " Added:" + change + " Now:" + now);
			runLogger.info("  # RDF triples before: " + NumberFormat.getIntegerInstance().format(was));
			runLogger.info("  # RDF triples after:  " + NumberFormat.getIntegerInstance().format(now));
			runLogger.info("  # RDF triples added:  " + NumberFormat.getIntegerInstance().format(change));
		}

		return null;
	}

	private static void loadDataFromRDFFile(File file) {

		String fileName = file.getName();
		String path = file.getPath();
		Logger runLogger = Logger.getLogger("run");

		runLogger.info("LOAD RDF " + path);

		Map<String, String> fileContents = new HashMap<String, String>();
		// List<String> fileContents = new ArrayList<String>();

		// long was = ActiveTDB.getModel().size();
		// long startTime = System.currentTimeMillis();
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
		} else if (fileName.matches(".*\\.zip.*")) {
			try {
				@SuppressWarnings("resource")
				ZipFile zf = new ZipFile(path);
				runLogger.info("LOAD RDF (zip file)" + fileName);

				Enumeration<?> entries = zf.entries();

				while (entries.hasMoreElements()) {
					ZipEntry ze = (ZipEntry) entries.nextElement();
					String inputType = ActiveTDB.getRDFTypeFromSuffix(ze.getName());
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
		// int newDataSources = readStringsCountNewDataSources(fileContents);
		readStringsCountNewDataSources(fileContents);
		ActiveTDB.syncTDBtoLCAHT();
	}

	// int olcaAdded = OpenLCA.inferOpenLCATriples();
	// runLogger.info("  # RDF triples added to openLCA data:  " + olcaAdded);

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
				if (!shouldInferOLCAtriples) {
					if (newLine.matches(".*http:\\/\\/openlca\\.org.*")) {
						shouldInferOLCAtriples = true;
					}
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

	private static int readStringsCountNewDataSources(Map<String, String> fileContentsList) {
		int before = DataSourceKeeper.countDataSourcesInTDB();
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.tdbDataset.getDefaultModel();
		String failedString = "";
		try {
			// tdbModel.setNsPrefix("", "http://openlca.org/schema/v1.0/");
			// tdbModel.setNsPrefix("eco", "http://ontology.earthster.org/eco/core#");
			for (String fileContents : fileContentsList.keySet()) {
				failedString = fileContents;
				// if (tableProvider.doesContainUntranslatedOpenLCAData()){
				String inputType = fileContentsList.get(fileContents);
				ByteArrayInputStream stream = new ByteArrayInputStream(fileContents.getBytes());
				tdbModel.read(stream, "http://openlca.org/schema/v1.0/", inputType);
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
		return DataSourceKeeper.countDataSourcesInTDB() - before;
	}

	// private static void readStreamCountNewDataSources(InputStream inputStream, String inputType) {
	// // --- BEGIN SAFE -WRITE- TRANSACTION ---
	// ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
	// Model tdbModel = ActiveTDB.tdbDataset.getDefaultModel();
	// try {
	// tdbModel.setNsPrefix("", "http://openlca.org/schema/v1.0/");
	// // tdbModel.setNsPrefix("eco", "http://ontology.earthster.org/eco/core#");
	// tdbModel.read(inputStream, null, inputType);
	// ActiveTDB.tdbDataset.commit();
	// // TDB.sync(ActiveTDB.tdbDataset);
	// } catch (Exception e) {
	// System.out.println("Import failed with Exception: " + e);
	// ActiveTDB.tdbDataset.abort();
	// } finally {
	// ActiveTDB.tdbDataset.end();
	// }
	// // ---- END SAFE -WRITE- TRANSACTION ---
	// }

	// private static void readBufferCountNewDataSources(BufferedReader zipStream, String inputType) {
	// // --- BEGIN SAFE -WRITE- TRANSACTION ---
	// ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
	// Model tdbModel = ActiveTDB.tdbDataset.getDefaultModel();
	// try {
	// tdbModel.setNsPrefix("", "http://openlca.org/schema/v1.0/");
	// tdbModel.read(zipStream, null, inputType);
	// ActiveTDB.tdbDataset.commit();
	// // TDB.sync(ActiveTDB.tdbDataset);
	// } catch (Exception e) {
	// System.out.println("Import failed; see strack trace!\n" + e);
	// ActiveTDB.tdbDataset.abort();
	// } finally {
	// ActiveTDB.tdbDataset.end();
	// }
	// // ---- END SAFE -WRITE- TRANSACTION ---
	// }

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
