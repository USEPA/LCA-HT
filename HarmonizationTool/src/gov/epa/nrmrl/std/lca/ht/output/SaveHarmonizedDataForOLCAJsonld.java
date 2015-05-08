package gov.epa.nrmrl.std.lca.ht.output;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.MatchContexts;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.MatchProperties;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

public class SaveHarmonizedDataForOLCAJsonld implements IHandler {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.csvFiles.SaveHarmonizedDataForOLCAJsonld";

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		Util.findView(MatchContexts.ID);
		Util.findView(MatchProperties.ID);

		Logger runLogger = Logger.getLogger("run");

		System.out.println("Saving Harmonized Data to .jsonld file");
		DataRow headerRow = HarmonizedDataSelector.getHarmonizedDataHeader();
		System.out.println("headerRow " + headerRow);

//		List<DataRow> dataRows = new ArrayList<DataRow>();
//		TableProvider tableProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey());
//		for (int i = 0; i < tableProvider.getData().size(); i++) {
//			DataRow dataRow = HarmonizedDataSelector.getHarmonizedDataRow(i);
//			dataRows.add(dataRow);
//		}

		Shell shell = HandlerUtil.getActiveShell(event);
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		String[] filterNames = new String[] { "Json Files", "Jsonld Files" };
		String[] filterExtensions = new String[] { "*.json", "*.jsonld" };

		String outputDirectory = Util.getPreferenceStore().getString("outputDirectory");
		if (outputDirectory.startsWith("(same as") || outputDirectory.length() == 0) {
			outputDirectory = Util.getPreferenceStore().getString("workingDirectory");
		}
		if (outputDirectory.length() > 0) {
			dialog.setFilterPath(outputDirectory);
		} else {
			String homeDir = System.getProperty("user.home");
			dialog.setFilterPath(homeDir);
		}

		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);
		Util.findView(CSVTableView.ID);
		String key = CSVTableView.getTableProviderKey();
		DataSourceProvider dataSourceProvider = TableKeeper.getTableProvider(key).getDataSourceProvider();
		String currentName = dataSourceProvider.getDataSourceName();
		dialog.setFileName(currentName + "_harmoinzed");

		// GenericStringBox dataSetNameSelector = new GenericStringBox(shell, "(choose dataset)",
		// DataSourceKeeper.getAlphabetizedNames());

		String saveTo = dialog.open();
		System.out.println("Save to: " + saveTo);
		if (saveTo == null) {
			// "dialog was cancelled or an error occurred"
			return null;
		}

		runLogger.info("  # Writing RDF triples to " + saveTo.toString());
		ActiveTDB.copyDatasetContentsToExportGraph(currentName);
		
		/* Once data are copied into the export graph, data can be prepared for openLCA
		 * 1) Determine which Flows have new information
		 * 2) Create new UUIDs for those
		 * 3) Move old info to an appropriate field name
		 * 4) Place new info in the appropriate place
		 * 5) Append to description info about what happened 
		 * */

		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		try {

			StringBuilder b = new StringBuilder();
			b.append(Prefixes.getPrefixesForQuery());
			b.append("insert {graph <" + ActiveTDB.exportGraphName + ">{  \n");
			b.append("  ?flow a olca:Flow . \n");
			b.append("  ?flow olca:name ?name . \n");
			b.append("  ?flow olca:cas ?cas_fmt . \n");
			b.append("  ?flow olca:formula ?formula . \n");
			b.append("}} \n");
			b.append("  where  {graph <" + ActiveTDB.exportGraphName + ">{\n");
			b.append("    ?flow a fedlca:Flow . \n");
			b.append("    ?flow eco:hasFlowable ?flowable . \n");
			b.append("    #-- Flow must have a comparison with 'equivalent' \n");
			b.append("    ?c fedlca:comparedSource ?flowable . \n");
			b.append("    ?c fedlca:comparedMaster ?masterFlowable . \n");
			b.append("    ?c fedlca:comparedEquivalence fedlca:Equivalent . \n");
			b.append("    ?masterFlowable rdfs:label ?masterLabel .  \n");
			b.append("    optional { ?masterFlowable eco:casNumber ?cas . } \n");
			b.append("    optional { ?masterFlowable eco:chemicalFormula ?formula . } \n");

			b.append("    ?flow fedlca:hasFlowContext ?flowContext . \n");
			b.append("    ?flowContext owl:sameAs ?masterFlowContext . \n");
			b.append("    filter (?flowContext != ?masterFlowContext ) \n");
			b.append("    ?masterFlowContext fedlca:flowContextGeneral ?generalContext . \n");
			b.append("    ?masterFlowContext fedlca:flowContextSupplementaryDescription ?specificContext . \n");

			b.append("    ?flow fedlca:hasFlowUnit ?flowUnit . \n");
			b.append("    ?flowUnit owl:sameAs ?masterFlowUnit . \n");
			b.append("    filter (?flowUnit != ?masterFlowUnit ) \n");

			b.append("    ?c fedlca:comparedSource ?flowable . \n");

			b.append("  }}\n");
			String query = b.toString();
			System.out.println("\n" + query + "\n");
			UpdateRequest request = UpdateFactory.create(query);
			UpdateProcessor proc = UpdateExecutionFactory.create(request, ActiveTDB.graphStore);
			proc.execute();
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("01 TDB transaction failed; see Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---

		try {
			FileOutputStream fout = new FileOutputStream(saveTo);
			String outType = ActiveTDB.getRDFTypeFromSuffix(saveTo);

			// --- BEGIN SAFE -READ- TRANSACTION ---
			ActiveTDB.tdbDataset.begin(ReadWrite.READ);
			tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
			tdbModel.write(fout, outType);
			ActiveTDB.tdbDataset.end();
			// ---- END SAFE -WRITE- TRANSACTION ---

		} catch (FileNotFoundException e1) {
			ActiveTDB.tdbDataset.abort();
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		/* To copy a dataset to the export graph */
		// ActiveTDB.copyDatasetContentsToExportGraph(olca);
		return null;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isHandled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

}
