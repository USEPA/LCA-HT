package gov.epa.nrmrl.std.lca.ht.handler;

import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.FileMD;
import gov.epa.nrmrl.std.lca.ht.dialog.GenericStringBox;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

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
		if (ActiveTDB.getModel(ActiveTDB.importGraphName) == null) {
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
		// DataSourceKeeper.placeOrphanDataInNewOrphanDataset();

		for (String fileName : fileList) {
			System.out.println("fileName= " + fileName);
			if (fileName.startsWith(sep)) {
				continue;
			}
			DataSourceProvider dataSourceProvider = null;
			int dotPos = fileName.lastIndexOf('.');
			String fileRoot = fileName.substring(0, dotPos);
			String fullName = path + sep + fileName;
			File file = new File(fullName);

			long startTime = System.currentTimeMillis();

			shouldInferOLCAtriples = false;
			loadDataFromRDFFile(file);
			if (shouldInferOLCAtriples) {
				int olcaAdded = OpenLCA.inferOpenLCATriples();
				runLogger.info("  # RDF triples added to openLCA data:  "
						+ NumberFormat.getIntegerInstance().format(olcaAdded));
			}

			long added = 0;
			HashMap<String, Resource> datasetNames = new HashMap<String, Resource>();
			// --- BEGIN SAFE -READ- TRANSACTION ---
			ActiveTDB.tdbDataset.begin(ReadWrite.READ);
			Model importModel = ActiveTDB.getModel(ActiveTDB.importGraphName);
			try {
				added = importModel.size();
				ResIterator resIterator = importModel.listSubjectsWithProperty(RDF.type, ECO.DataSource);
				while (resIterator.hasNext()) {
					Resource dataSourceResource = resIterator.next();
					NodeIterator nodeIterator = importModel.listObjectsOfProperty(dataSourceResource, RDFS.label);
					datasetNames.put(nodeIterator.next().asLiteral().getString(), dataSourceResource);
				}

			} catch (Exception e) {
				System.out.println("Import failed with Exception: " + e);
				ActiveTDB.tdbDataset.abort();
			} finally {
				ActiveTDB.tdbDataset.end();
			}
			// ---- END SAFE -READ- TRANSACTION ---

			runLogger.info("READ file: " + fullName + "\n");
			runLogger.info("  # Added triples:" + NumberFormat.getIntegerInstance().format(added));
			int dataSetCount = datasetNames.size();

			Resource newDatasetResource = null;
			if (dataSetCount == 0) {
				newDatasetResource = DataSourceKeeper.createNewDataSet(event, fileRoot, LCAHT.MasterDataset);
				dataSourceProvider = DataSourceKeeper.get(DataSourceKeeper.getByTdbResource(newDatasetResource));
				dataSourceProvider.syncFromTDB(ActiveTDB.importGraphName);
			} else if (dataSetCount == 1) {
				String datasetName = (String) datasetNames.keySet().toArray()[0];
				String fixedName = getConfirmedNewDatasetName(event, datasetName);
				newDatasetResource = datasetNames.get(datasetName);
				if (!datasetName.equals(fixedName)) {
					// --- BEGIN SAFE -WRITE- TRANSACTION ---
					ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
					importModel = ActiveTDB.getModel(ActiveTDB.importGraphName);
					try {
						Literal oldNameLiteral = importModel.createTypedLiteral(datasetName);
						Literal newNameLiteral = importModel.createTypedLiteral(fixedName);
						importModel.remove(newDatasetResource, RDFS.label, oldNameLiteral);
						importModel.add(newDatasetResource, RDFS.label, newNameLiteral);
						ActiveTDB.tdbDataset.commit();
					} catch (Exception e) {
						System.out.println("Problem adding imported items to its dataset; see Exception: " + e);
						ActiveTDB.tdbDataset.abort();
					} finally {
						ActiveTDB.tdbDataset.end();
					}
					// ---- END SAFE -WRITE- TRANSACTION ---
					runLogger
							.warn("  # File contains a data set whose name collides with an existing data set.  User selected: "
									+ fixedName + "\n");
				} else {
					runLogger.info("  # File contains data set: " + fixedName + "\n");
				}
				dataSourceProvider = new DataSourceProvider(newDatasetResource);
				dataSourceProvider.syncFromTDB(ActiveTDB.importGraphName);
			} else {
				String datasetName = (String) datasetNames.keySet().toArray()[0];
				String fixedName = getConfirmedNewDatasetName(event, datasetName);
				newDatasetResource = datasetNames.get(datasetName);
				if (!datasetName.equals(fixedName)) {
					// --- BEGIN SAFE -WRITE- TRANSACTION ---
					ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
					importModel = ActiveTDB.getModel(ActiveTDB.importGraphName);
					try {
						Literal oldNameLiteral = importModel.createTypedLiteral(datasetName);
						Literal newNameLiteral = importModel.createTypedLiteral(fixedName);
						importModel.remove(newDatasetResource, RDFS.label, oldNameLiteral);
						importModel.add(newDatasetResource, RDFS.label, newNameLiteral);
						ActiveTDB.tdbDataset.commit();
					} catch (Exception e) {
						System.out.println("Problem adding imported items to its dataset; see Exception: " + e);
						ActiveTDB.tdbDataset.abort();
					} finally {
						ActiveTDB.tdbDataset.end();
					}
					// ---- END SAFE -WRITE- TRANSACTION ---
					runLogger.warn("  # File contains multiple data sets.  User selected: " + fixedName + "\n");
				} else {
					runLogger.warn("  # File contains multiple data sets.  Using first of: " + datasetNames.keySet()
							+ "\n");
				}
				dataSourceProvider = new DataSourceProvider(newDatasetResource);
				dataSourceProvider.syncFromTDB(ActiveTDB.importGraphName);
			}

			/* CHECK FOR AND PROVIDE DATASOURCE FOR ORPHANS */
			List<Resource> orphans = DataSourceKeeper.getOrphanResources(ActiveTDB.getModel(ActiveTDB.importGraphName));
			if (!orphans.isEmpty()) {
				// --- BEGIN SAFE -WRITE- TRANSACTION ---
				ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
				importModel = ActiveTDB.getModel(ActiveTDB.importGraphName);
				try {
					for (Resource orphan : orphans) {
						importModel.add(orphan, ECO.hasDataSource, newDatasetResource);
					}
					ActiveTDB.tdbDataset.commit();
				} catch (Exception e) {
					System.out.println("Problem adding imported items to its dataset; see Exception: " + e);
					ActiveTDB.tdbDataset.abort();
				} finally {
					ActiveTDB.tdbDataset.end();
				}
				// ---- END SAFE -WRITE- TRANSACTION ---
			}
			FileMD fileMD = new FileMD();
			fileMD.setFilename(fileName);
			fileMD.setPath(path);
			fileMD.setByteCount(file.length());
			fileMD.setModifiedDate(new Date(file.lastModified()));
			Date readDate = new Date();
			fileMD.setReadDate(readDate);
			dataSourceProvider.addFileMD(fileMD);

			/* TRANSFER DATA TO DEFAULT GRAPH */
			float startGraphCopy = System.currentTimeMillis();
			ActiveTDB.copyImportGraphContentsToDefault();
			ActiveTDB.clearImportGraphContents();
			float endGraphCopy = (System.currentTimeMillis() - startGraphCopy) / 1000F;
			System.out.println("Time elapsed: " + endGraphCopy);
		}
		return null;
	}

	private static String getConfirmedNewDatasetName(ExecutionEvent event, String proposedName) {
		if (DataSourceKeeper.getByName(proposedName) == null) {
			return proposedName;

		}
		String confirmedName = null;
		int result = Window.CANCEL;
		while (result == Window.CANCEL) {
			GenericStringBox genericStringBox = new GenericStringBox(HandlerUtil.getActiveShell(event),
					DataSourceKeeper.uniquify(proposedName), DataSourceKeeper.getAlphabetizedNames());
			genericStringBox
					.create("Provide Alternative Data Set Name",
							"Recently loaded data has a dataset name that collides with an existing name.  Please provide a different name for this new set.");
			result = genericStringBox.open();
			confirmedName = genericStringBox.getResultString();
		}
		return confirmedName;
	}

	// private static void replaceDatasetName(String datasetName, String fixedName) {
	// // --- BEGIN SAFE -WRITE- TRANSACTION ---
	// ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
	// Model importModel = ActiveTDB.getModel(ActiveTDB.importGraphName);
	// try {
	// for (Resource orphan : DataSourceKeeper.getOrphanResources(ActiveTDB
	// .getModel(ActiveTDB.importGraphName))) {
	// orphan.addProperty(ECO.hasDataSource, newDatasetResource);
	// }
	// ActiveTDB.tdbDataset.commit();
	// } catch (Exception e) {
	// System.out.println("Problem adding imported items to its dataset; see Exception: " + e);
	// ActiveTDB.tdbDataset.abort();
	// } finally {
	// ActiveTDB.tdbDataset.end();
	// }
	// // ---- END SAFE -WRITE- TRANSACTION ---
	// }

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
		// ActiveTDB.syncTDBtoLCAHT();
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
		Model importModel = ActiveTDB.getModel(ActiveTDB.importGraphName);

		String failedString = "";
		try {
			for (String fileContents : fileContentsList.keySet()) {
				failedString = fileContents;
				String inputType = fileContentsList.get(fileContents);
				ByteArrayInputStream stream = new ByteArrayInputStream(fileContents.getBytes());
				importModel.read(stream, "http://openlca.org/schema/v1.0/", inputType);
			}
			ActiveTDB.tdbDataset.commit();
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
