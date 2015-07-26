package gov.epa.nrmrl.std.lca.ht.tdb;

import gov.epa.nrmrl.std.lca.ht.utils.Temporal;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class ImportRDFFileDirectlyToGraph {

	public static final Logger runLogger = Logger.getLogger("run");

	private static class LCAZipInputStream extends ZipInputStream {
		public LCAZipInputStream(InputStream in) {
			super(in);
		}

		public void close() throws IOException {

		}

		public void closeZip() throws IOException {
			super.close();
		}
	}

	public static Object loadToDefaultGraph(String filePath, String graphName) {

		if (ActiveTDB.getModel(graphName) == null) {
			return null;
		}

		File file = null;
		InputStream input = null;

		if (filePath.startsWith("classpath:")) {
			String targetPath = filePath.substring("classpath:".length());
			input = ImportRDFFileDirectlyToGraph.class.getResourceAsStream(targetPath);
		} else
			file = new File(filePath);

		long time0 = System.currentTimeMillis();
		if (file != null)
			loadDataFromRDFFile(file, graphName);
		else
			loadDataFromRDFFile(filePath, null, input, graphName);
		long time1 = System.currentTimeMillis();

		float interval1 = ((time1 - time0) / 1000F);
		runLogger.info("  # Seconds to read file:  " + interval1);

		return null;
	}

	private static void loadDataFromRDFFile(File file, String graphName) {
		List<Resource> beforeList = findFileMDResource(graphName);
		try {
			InputStream in = new FileInputStream(file);
			String fileName = file.getName();
			String path = file.getPath();
			loadDataFromRDFFile(fileName, path, in, graphName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		List<Resource> afterList = findFileMDResource(graphName);
		if (afterList.size() == beforeList.size() + 1) {
			for (Resource resource : afterList) {
				if (!beforeList.contains(resource)) {
					Date date = new Date();
					Literal literal = Temporal.getLiteralFromDate1(date);
					ActiveTDB.tsRemoveAllLikeLiterals(resource, LCAHT.fileReadDate, literal, graphName);
					ActiveTDB.tsAddGeneralTriple(resource, LCAHT.fileReadDate, literal, graphName);
				}
			}
		} else if (afterList.size() == beforeList.size()) {
			// TODO: create new FileMD info and attach to file
		}
	}

	private static List<Resource> findFileMDResource(String graphName) {
		List<Resource> fileMDResources = new ArrayList<Resource>();
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.getModel(graphName);
		Selector selector = new SimpleSelector(null, LCAHT.containsFile, null, null);
		StmtIterator stmtIterator = tdbModel.listStatements(selector);
		while (stmtIterator.hasNext()) {
			fileMDResources.add(stmtIterator.next().getObject().asResource());
		}
		ActiveTDB.tdbDataset.end();
		return fileMDResources;
	}

	private static void loadDataFromRDFFile(String fileName, String path, InputStream in, String graphName) {

		runLogger.info("\nLOAD RDF path " + path + " fileName " + fileName);
		List<Resource> beforeList = findFileMDResource(graphName);

		Map<String, String> fileContents = new HashMap<String, String>();
		if (!fileName.matches(".*\\.zip")) {
			String inputType = ActiveTDB.getRDFTypeFromSuffix(fileName);
			if (inputType == null) {
				return;
			}
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				fileContents.put(bufferToString(br), inputType);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (fileName.matches(".*\\.zip.*")) {
			LCAZipInputStream zs = null;

			try {
				zs = new LCAZipInputStream(in);
				runLogger.info("  # File is a zip file");

				ZipEntry ze = zs.getNextEntry();
				while (ze != null) {
					String inputType = ActiveTDB.getRDFTypeFromSuffix(ze.getName());
					if (inputType == null) {
						continue;
					}

					BufferedReader zipStream = new BufferedReader(new InputStreamReader(zs));
					fileContents.put(bufferToString(zipStream), inputType);
					runLogger.info("  # Zip contents: " + ze.getName());
					ze = zs.getNextEntry();
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (zs != null)
						zs.closeZip();
				} catch (Exception e) {
				}
			}
		}
		readStringsCountNewDataSources(fileContents, graphName);
		List<Resource> afterList = findFileMDResource(graphName);
		if (afterList.size() == beforeList.size() + 1) {
			for (Resource resource : afterList) {
				if (!beforeList.contains(resource)) {
					Date date = new Date();
					Literal literal = Temporal.getLiteralFromDate1(date);
					ActiveTDB.tsRemoveAllLikeLiterals(resource, LCAHT.fileReadDate, literal, graphName);
					ActiveTDB.tsAddGeneralTriple(resource, LCAHT.fileReadDate, literal, graphName);
				}
			}
		} else if (afterList.size() == beforeList.size()) {
			// TODO: create new FileMD info and attach to file
		}

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

	private static void readStringsCountNewDataSources(Map<String, String> fileContentsList, String graphName) {
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model importModel = ActiveTDB.getModel(graphName);

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
//			System.out.println("The failing string was: \n" + failedString);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}
}