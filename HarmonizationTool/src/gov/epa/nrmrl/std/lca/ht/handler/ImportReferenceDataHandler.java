package gov.epa.nrmrl.std.lca.ht.handler;

import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.FileMD;
import gov.epa.nrmrl.std.lca.ht.dialog.MetaDataDialog;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.tdb.ImportRDFFileDirectlyToGraph;
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
import java.text.NumberFormat;
import java.io.InputStreamReader;
import java.util.Calendar;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ImportReferenceDataHandler implements IHandler {

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
			if (fileRoot.matches(".*lcaht.*")) {
				ImportRDFFileDirectlyToGraph.loadToDefaultGraph(fullName, null);
				DataSourceKeeper.syncFromTDB();
			} else {

				long time0 = System.currentTimeMillis();

				shouldInferOLCAtriples = false;
				loadDataFromRDFFile(file);
				long time1 = System.currentTimeMillis();

				boolean alreadyEcoDataSource = false;
				if (importGraphContainsEcoDataSource()) {
					shouldInferOLCAtriples = false;
					alreadyEcoDataSource = true;
				}

				long time2;
				if (shouldInferOLCAtriples) {
					int olcaAdded = OpenLCA.convertOpenLCAToLCAHT(ActiveTDB.importGraphName);
					runLogger.info("  # RDF triples added to openLCA data:  "
							+ NumberFormat.getIntegerInstance().format(olcaAdded));
					time2 = System.currentTimeMillis();

				} else {
					time2 = 0;
				}

				long added = 0;
				Integer referenceStatus = 1;
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
						if (importModel.contains(dataSourceResource, RDF.type, LCAHT.MasterDataset)) {
							referenceStatus = 1;
						} else if (importModel.contains(dataSourceResource, RDF.type,
								LCAHT.SupplementaryReferenceDataset)) {
							referenceStatus = 2;
						}
					}

				} catch (Exception e) {
					System.out.println("Import failed with Exception: " + e);
					ActiveTDB.tdbDataset.abort();
				} finally {
					ActiveTDB.tdbDataset.end();
				}
				// ---- END SAFE -READ- TRANSACTION ---

				runLogger.info("  # File read complete");
				runLogger.info("  # Added triples:" + NumberFormat.getIntegerInstance().format(added));
				int dataSetCount = datasetNames.size();

				Resource newDatasetResource = null;
				String proposedNewDatasetName = DataSourceKeeper.uniquify(fileRoot);
				if (dataSetCount == 0) {
					dataSourceProvider = new DataSourceProvider();
					newDatasetResource = dataSourceProvider.getTdbResource();
					runLogger.info("  # Creating new data set for file using name: " + proposedNewDatasetName);
					// dataSourceProvider.syncFromTDB(ActiveTDB.importGraphName);
					dataSourceProvider.setDataSourceName(proposedNewDatasetName);
					dataSourceProvider.setReferenceDataStatus(1);
				} else if (dataSetCount == 1) {
					proposedNewDatasetName = DataSourceKeeper.uniquify((String) datasetNames.keySet().toArray()[0]);
					newDatasetResource = datasetNames.get(proposedNewDatasetName);
					runLogger.info("  # File contains data set: " + proposedNewDatasetName);
					dataSourceProvider = new DataSourceProvider(newDatasetResource);
					// dataSourceProvider.syncFromTDB(ActiveTDB.importGraphName);
					dataSourceProvider.setDataSourceName(proposedNewDatasetName);
					dataSourceProvider.setReferenceDataStatus(referenceStatus);
				} else {
					proposedNewDatasetName = DataSourceKeeper.uniquify((String) datasetNames.keySet().toArray()[0]);
					newDatasetResource = datasetNames.get(proposedNewDatasetName);
					runLogger.warn("  # File contains multiple data sets.  Using first of: " + datasetNames.keySet());
					dataSourceProvider = new DataSourceProvider(newDatasetResource);
					// dataSourceProvider.syncFromTDB(ActiveTDB.importGraphName);
					dataSourceProvider.setDataSourceName(proposedNewDatasetName);
					dataSourceProvider.setReferenceDataStatus(referenceStatus);
				}
				FileMD fileMD = new FileMD();
				fileMD.setFilename(fileName);
				fileMD.setPath(path);
				fileMD.setByteCount(file.length());
				
				Calendar modifiedDate = Calendar.getInstance();
				modifiedDate.setTimeInMillis(file.lastModified());
				fileMD.setModifiedDate(modifiedDate);
				Calendar readDate = Calendar.getInstance();
				fileMD.setReadDate(readDate);
				dataSourceProvider.addFileMD(fileMD);
				long time3 = System.currentTimeMillis();

				MetaDataDialog dialog = new MetaDataDialog(Display.getCurrent().getActiveShell(), dataSourceProvider);
				System.out.println("meta initialized");
				dialog.create();
				System.out.println("meta created");
				int metaStatus = dialog.open();
				if (metaStatus == MetaDataDialog.CANCEL) {
					ActiveTDB.clearImportGraphContents();
				}
				boolean isMaster = true;
				if (dataSourceProvider.getReferenceDataStatus() == 2) {
					isMaster = false;
				}
				/* CHECK FOR AND PROVIDE DATASOURCE FOR ORPHANS */
				long time4 = System.currentTimeMillis();

				if (!alreadyEcoDataSource) {
					List<Resource> orphans = DataSourceKeeper.getOrphanResources(ActiveTDB.importGraphName);
					if (!orphans.isEmpty()) {
						// --- BEGIN SAFE -WRITE- TRANSACTION ---
						ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
						importModel = ActiveTDB.getModel(ActiveTDB.importGraphName);
						try {
							if (isMaster) {
								importModel.add(newDatasetResource, RDF.type, LCAHT.MasterDataset);
							} else {
								importModel.add(newDatasetResource, RDF.type, LCAHT.SupplementaryReferenceDataset);
							}
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
				} else {
					ActiveTDB.tsReplaceLiteral(newDatasetResource, RDFS.label, dataSourceProvider.getDataSourceName(),
							ActiveTDB.importGraphName);
				}
				long time5 = System.currentTimeMillis();

				/* TRANSFER DATA TO DEFAULT GRAPH */
				ActiveTDB.copyImportGraphContentsToDefault();
				long time6 = System.currentTimeMillis();
				ActiveTDB.clearImportGraphContents();
				long time7 = System.currentTimeMillis();

				float interval1 = ((time1 - time0) / 1000F);
				runLogger.info("  # Seconds to read file:  " + interval1);
				float interval3;
				if (time2 > 0) {
					float interval2 = ((time2 - time1) / 1000F);
					interval3 = ((time3 - time2) / 1000F);
					runLogger.info("  # Seconds to infer openLCA triples: " + interval2);
				} else {
					interval3 = ((time3 - time1) / 1000F);
				}
				runLogger.info("  # Seconds to prepare dataset info: " + interval3);
				float interval4 = ((time5 - time4) / 1000F);
				if (!alreadyEcoDataSource) {
					runLogger.info("  # Seconds to assign data to dataset: " + interval4);
				}
				float interval5 = ((time6 - time5) / 1000F);
				runLogger.info("  # Seconds to transfer data to main graph: " + interval5);
				float interval6 = ((time7 - time6) / 1000F);
				runLogger.info("  # Seconds to empty import graph: " + interval6);
			}
		}
		return null;
	}

	private static boolean importGraphContainsEcoDataSource() {
		boolean result = false;
		// --- BEGIN SAFE -READ- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model importModel = ActiveTDB.getModel(ActiveTDB.importGraphName);
		try {
			result = importModel.contains(null, RDF.type, ECO.DataSource);

		} catch (Exception e) {
			System.out.println("Import failed with Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		return result;
		// ---- END SAFE -READ- TRANSACTION ---
	}

	private static void renameDataSet(Resource datasetResource, String oldName, String newName) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model importModel = ActiveTDB.getModel(ActiveTDB.importGraphName);
		try {
			Literal oldNameLiteral = importModel.createTypedLiteral(oldName);
			Literal newNameLiteral = importModel.createTypedLiteral(newName);
			importModel.remove(datasetResource, RDFS.label, oldNameLiteral);
			importModel.add(datasetResource, RDFS.label, newNameLiteral);
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("Problem adding imported items to its dataset; see Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	// private static String getConfirmedNewDatasetName(ExecutionEvent event, String proposedName) {
	// if (DataSourceKeeper.getByName(proposedName) == null) {
	// return proposedName;
	//
	// }
	// String confirmedName = null;
	// int result = Window.CANCEL;
	// while (result == Window.CANCEL) {
	// GenericStringBox genericStringBox = new GenericStringBox(HandlerUtil.getActiveShell(event),
	// DataSourceKeeper.uniquify(proposedName), DataSourceKeeper.getAlphabetizedNames());
	// genericStringBox
	// .create("Provide Alternative Data Set Name",
	// "Recently loaded data has a dataset name that collides with an existing name.  Please provide a different name for this new set.");
	// result = genericStringBox.open();
	// confirmedName = genericStringBox.getResultString();
	// }
	// return confirmedName;
	// }

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

		runLogger.info("\nLOAD RDF " + path);

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
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (fileName.matches(".*\\.zip.*")) {
			try {
				@SuppressWarnings("resource")
				ZipFile zf = new ZipFile(path);
				runLogger.info("  # File is a zip file");

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
					runLogger.info("  # Zip contents: " + ze.getName());

				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		readStringsCountNewDataSources(fileContents);
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

	private static void readStringsCountNewDataSources(Map<String, String> fileContentsList) {
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
