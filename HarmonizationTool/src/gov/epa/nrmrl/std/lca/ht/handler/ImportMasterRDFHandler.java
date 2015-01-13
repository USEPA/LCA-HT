package gov.epa.nrmrl.std.lca.ht.handler;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.jena.riot.system.IRIResolver;
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
import com.hp.hpl.jena.tdb.TDB;

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
		System.out.println("executing TDB load");
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
//		fileDialog.setFilterExtensions(new String[] { "*.zip;*.n3;*.ttl;*.rdf;" });
		// SHOWS ALL TYPES IN ONE WINDOW

		fileDialog.open();
		String path = fileDialog.getFilterPath();
		String[] fileList = fileDialog.getFileNames();

		// String sep = System.getProperty("file.separator");
		String sep = File.separator;

		System.out.println("path= " + path);
		IRIResolver thing = IRIResolver.create("http://openlca.org/schema/v1.0#");

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
						System.out.println("Import failed with Exception: "+e);
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
//								TDB.sync(ActiveTDB.tdbDataset);
							} catch (Exception e) {
								System.out.println("Import failed; see strack trace!\n"+e);
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
