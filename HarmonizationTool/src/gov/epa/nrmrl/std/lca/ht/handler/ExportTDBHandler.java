package gov.epa.nrmrl.std.lca.ht.handler;

import gov.epa.nrmrl.std.lca.ht.dialog.GenericMessageBox;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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

public class ExportTDBHandler implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		Model tdbModelCopy = ActiveTDB.getModel();
		if (tdbModelCopy == null) {
			return null;
		}

		Logger runLogger = Logger.getLogger("run");

		FileDialog fileDialog = new FileDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), SWT.SAVE);
//		fileDialog.setFilterExtensions(new String[] { "*.n3", "*.rdf", "*.ttl", "*.jsonld" });
		fileDialog.setFilterExtensions(new String[] { "*.n3", "*.rdf", "*.ttl" });
		String outputDirectory = Util.getPreferenceStore().getString("outputDirectory");
		if (outputDirectory.startsWith("(same as") || outputDirectory.length() == 0) {
			outputDirectory = Util.getPreferenceStore().getString("workingDirectory");
		}
		if (outputDirectory.length() > 0) {
			fileDialog.setFilterPath(outputDirectory);
		} else {
			String homeDir = System.getProperty("user.home");
			fileDialog.setFilterPath(homeDir);
		}
		String path = fileDialog.open();
		if (path != null) {
			long startTime = System.currentTimeMillis();

			try {
				String outType = "SKIP";
				if (path.matches(".*\\.rdf.*")) {
					outType = "RDF/XML";
				} else if (path.matches(".*\\.n3.*")) {
					outType = "N3";
				} else if (path.matches(".*\\.ttl.*")) {
					outType = "TTL";
//				} else if (path.matches(".*\\.jsonld.*")) {
//					outType = "JSON-LD";
				}

				if (outType.equals("SKIP")) {
					new GenericMessageBox(HandlerUtil.getActiveShell(event), "Unsupported output format",
							"Supported output formats include .rdf (RDF/XML), .n3, and .ttl");
//				"Supported output formats include .rdf (RDF/XML), .n3, .ttl, and .jsonld");
					return null;
				}
				runLogger.info("  # Writing RDF triples to " + path.toString());
				FileOutputStream fout = new FileOutputStream(path);
				// RDFWriter rdfWriter = tdbModel.getWriter("RDF/XML");
				// RDFWriter rdfWriter = tdbModel.getWriter(outType); // WORKED
				// rdfWriter.write(tdbModel, fout, null); // WORKED
				// tdbModel.write(fout, path, outType); // BAD

				// --- BEGIN SAFE -WRITE- TRANSACTION ---
				ActiveTDB.tdbDataset.begin(ReadWrite.READ);
				Model tdbModel = ActiveTDB.getModel();
				try {
					tdbModel.write(fout, outType); // TESTING
				} catch (Exception e) {
					System.out.println("Export failed; see strack trace!");
					ActiveTDB.tdbDataset.abort();
				} finally {
					ActiveTDB.tdbDataset.end();
				}
				// ---- END SAFE -WRITE- TRANSACTION ---

				// ActiveTDB.tdbModel.write(fout, outType); // TESTING
				fout.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			float elapsedTimeSec = (System.currentTimeMillis() - startTime) / 1000F;
			runLogger.info("  # Time elapsed: " + elapsedTimeSec);
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
