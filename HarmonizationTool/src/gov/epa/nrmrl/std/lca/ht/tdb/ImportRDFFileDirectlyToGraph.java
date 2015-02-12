package gov.epa.nrmrl.std.lca.ht.tdb;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;

public class ImportRDFFileDirectlyToGraph {

	public Object loadToDefaultGraph(String filePath, String graphName) {

		Logger runLogger = Logger.getLogger("run");
		if (ActiveTDB.getModel(graphName) == null) {
			return null;
		}
//
//		String sep = File.separator;
//		int filenameStart = filePath.lastIndexOf(sep);
//		String fileName = filePath.substring(filenameStart);
//		int dotPos = fileName.lastIndexOf('.');
//		String fileRoot = filePath.substring(0, dotPos);
		File file = new File(filePath);

		long time0 = System.currentTimeMillis();
		loadDataFromRDFFile(file, graphName);
		long time1 = System.currentTimeMillis();

		float interval1 = ((time1 - time0) / 1000F);
		runLogger.info("  # Seconds to read file:  " + interval1);

		return null;
	}

	
	private static void loadDataFromRDFFile(File file, String graphName) {

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
				fileContents.put(bufferToString(br), inputType);
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
					fileContents.put(bufferToString(zipStream), inputType);
					runLogger.info("  # Zip contents: " + ze.getName());

				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		readStringsCountNewDataSources(fileContents);
	}


	private static String bufferToString(BufferedReader bufferedReader) {
		StringBuilder stringBuilder = new StringBuilder();
		String line;
		try {
			line = bufferedReader.readLine();
			while (line != null) {
				stringBuilder.append(line);
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
}
