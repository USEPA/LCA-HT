package gov.epa.nrmrl.std.lca.ht.output;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.curation.CurationMethods;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.MatchContexts;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.MatchProperties;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Calendar;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.DCTerms;

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

		// List<DataRow> dataRows = new ArrayList<DataRow>();
		// TableProvider tableProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey());
		// for (int i = 0; i < tableProvider.getData().size(); i++) {
		// DataRow dataRow = HarmonizedDataSelector.getHarmonizedDataRow(i);
		// dataRows.add(dataRow);
		// }

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

		/*
		 * Once data are copied into the export graph, data can be prepared for openLCA 1) Determine which Flows have
		 * new information 2) Create new UUIDs for those 3) Move old info to an appropriate field name 4) Place new info
		 * in the appropriate place 5) Append to description info about what happened
		 */
		// FIXME -- ISSUES ARE BELOW WITH DATE TIME FUNCTIONS
		RDFNode modNode = CurationMethods.getCurrentAnnotation().getProperty(DCTerms.modified).getObject();
		Literal modLiteral = modNode.asLiteral();
		XSDDateTime modObject = (XSDDateTime) modLiteral.getValue();
		Calendar modCalendar = modObject.asCalendar();
		String modString = modCalendar.getTime().toGMTString();
		

		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		// nothing
//		RDFNode modNode = CurationMethods.getCurrentAnnotation().getProperty(DCTerms.modified).getObject();
//		Literal modLiteral = modNode.asLiteral();
//		Calendar cal = ((XSDDateTime) literalDate).;
//		Object thing = literalDate.getValue();
//		System.out.println("it is a :"+modString.getClass());
//		Date annotatationDate = new Date(CurationMethods.getCurrentAnnotation().getProperty(DCTerms.modified)
//				.getObject().asLiteral().getLong());
//		String dateString = Util.getLocalDateFmt(annotatationDate);
		try {

			StringBuilder b = new StringBuilder();
			b.append(Prefixes.getPrefixesForQuery());
			b.append("  delete {graph <" + ActiveTDB.exportGraphName + ">{  \n");
			b.append("    ?of olca:description ?oDescription ; \n");
			b.append("        olca:lastChange ?oLastChange ; \n");
			b.append("        olca:cas ?oCas ; \n");
			b.append("        fedlca:hasOpenLCAUUID ?oUUID . \n");
			b.append("  }} \n");
			b.append("   \n");
			b.append("  insert {graph <" + ActiveTDB.exportGraphName + ">{  \n");
			b.append("    ?of olca:description ?newDescription ; \n");
			b.append("        olca:lastChange ?newLastChange ; \n");
			b.append("        olca:cas ?newCas ; \n");
			b.append("        fedlca:hasOpenLCAUUID ?newUUID . \n");
			b.append("  }} \n");
			b.append("   \n");
			b.append("  where { \n");
			b.append("    #-- olca:Flow \n");
			b.append("    ?of a olca:Flow . \n");
			b.append("    ?of fedlca:hasOpenLCAUUID ?oUUID . \n");
			b.append("    ?pf a fedlca:Flow . \n");
			b.append("   \n");
			b.append("    #-- fedlca:Flow (parsed) \n");
			b.append("    ?pf fedlca:hasOpenLCAUUID ?oUUID . \n");
			b.append("    ?pf fedlca:sourceTableRowNumber ?row . \n");
			b.append("   \n");
			b.append("    ?pf owl:sameAs ?mf . \n");
			b.append("    #-- fedlca:Flow (master) \n");
			b.append("    ?mf a fedlca:Flow . \n");
			b.append("    ?mf eco:hasFlowable ?mflowable . \n");
			b.append("    ?mds a lcaht:MasterDataset . \n");
			b.append("    ?mf eco:hasDataSource ?mds . \n");
			b.append("   \n");
			b.append("    #-- olca:name == rdfs:label -- 1 CONDITION NEEDING ACTION \n");
			b.append("    ?of olca:name ?oName . \n");
			b.append("    ?mflowable rdfs:label ?mName . \n");
			b.append("    bind (IF ((str(?oName) != str(?mName) ) , concat(\"; name: master = \", ?mName) , \"\") as ?cName) \n");
			b.append("   \n");
			b.append("    #-- olca:cas == eco:casNumber -- 3 CONDITIONS NEEDING ACTION \n");
			b.append("    optional { ?of olca:cas ?oCas . } \n");
			b.append("    optional { ?mflowable eco:casNumber ?mCas . } \n");
			b.append("    bind (IF (( bound(?oCas) &&  bound(?mCas) && str(?oCas) != str(?mCas)) , concat(\"; cas: original = \",?oCas),\"\") as ?c1Cas) \n");
			b.append("    bind (IF ((!bound(?oCas) &&  bound(?mCas)) , \"; cas: original not defined\",\"\") as ?c2Cas) \n");
			b.append("    bind (IF (( bound(?oCas) && !bound(?mCas)) , \"; cas: master not defined\",\"\") as ?c3Cas) \n");
			b.append("    bind (concat(?c1Cas,?c2Cas,?c3Cas) as ?cCas) \n");
			b.append("    bind (IF ((?c1Cas != \"\" || ?c2Cas != \"\" || ?c3Cas != \"\"),?mCas, ?oCas) as ?newCas) \n");
			b.append("    #-- ABOVE, THE USE OF ?oCas IS TO ENSURE THAT IT GETS PUT BACK SINCE IT WILL BE DELETED \n");
			b.append("   \n");
			b.append("    #-- olca:formula == eco:chemicalFormula -- 3 CONDITIONS NEEDING ACTION \n");
			b.append("    optional { ?of olca:formula ?oFormula . } \n");
			b.append("    optional { ?mflowable eco:chemicalFormula ?mFormula . } \n");
			b.append("    bind (IF (( bound(?oFormula) &&  bound(?mFormula) && str(?oFormula) != str(?mFormula)) , concat(\"; formula: original = \",?oFormula),\"\") as ?c1Formula) \n");
			b.append("    bind (IF ((!bound(?oFormula) &&  bound(?mFormula)) , \"; formula: original not defined\",\"\") as ?c2Formula) \n");
			b.append("    bind (IF (( bound(?oFormula) && !bound(?mFormula)) , \"; formula: master not defined\",\"\") as ?c3Formula) \n");
			b.append("    bind (concat(?c1Formula,?c2Formula,?c3Formula) as ?cFormula) \n");
			b.append("   \n");
			b.append("    #-- fedlca:hasOpenLCAUUID (for both) -- 4 CONDITIONS NEEDING ACTION \n");
			b.append("    optional { ?mf fedlca:hasOpenLCAUUID ?mUUID . } \n");
			b.append("    bind (IF (( bound(?oUUID) &&  bound(?mUUID) && str(?oUUID) != str(?mUUID)) , concat(\"; UUID: original = \",?oUUID),\"\") as ?c1UUID) \n");
			b.append("    bind (IF ((!bound(?oUUID) &&  bound(?mUUID)) , \"; UUID: original not defined\",\"\") as ?c2UUID) \n");
			b.append("    #-- ABOVE NOT NEEDED UNLESS WE MAKE THE oUUID OPTIONAL \n");
			b.append("    bind (IF (( bound(?oUUID) && !bound(?mUUID)) , \"; UUID: master not defined\",\"\") as ?c3UUID) \n");
			b.append("    bind (IF ((!bound(?oUUID) && !bound(?mUUID)) , \"; UUID: new value created\",\"\") as ?c4UUID) \n");
			b.append("    bind (concat(?c1UUID,?c2UUID,?c3UUID,?c4UUID) as ?cUUID) \n");
			b.append("    bind (IF (( bound(?oUUID) &&  bound(?mUUID)) , str(?mUUID),\"\") as ?new1UUID) \n");
			b.append("    bind (IF (( bound(?oUUID) && !bound(?mUUID)) ,str(?oUUID),\"\") as ?new2UUID) \n");
			b.append("    bind (concat(?new1UUID,?new2UUID) as ?newUUID) \n");
			b.append("   \n");
			b.append("    #-- olca:lastChange -- 1 CONDITION NEEDING ACTION \n");
			b.append("    optional {?of olca:lastChange ?oLastChange } \n");
			b.append("    bind (IF (bound(?oLastChange) , concat(\"; previous lastChange: \",str(?oLastChange)),\"\") as ?cLastChange)  \n");
			b.append("    bind (\""+ modString +"\"^^xsd:dataTime as ?newLastChange) \n");
			b.append("    #--    ^^^^^^^^^^^^^^^^^^^^^^^^^ PLACE ACTUAL VALUE FROM Annotation ABOVE \n");
			b.append("   \n");
			b.append("    #-- olca:description -- 1 CONDITION PLUS CONCATINATION NEEDED \n");
			b.append("    optional {?of olca:description ?oDescription } \n");
			b.append("    bind (IF (!bound(?oDescription) , concat(\"Description created:\",str(now())),?oDescription) as ?cDescription)  \n");
			b.append("    bind (concat(?cDescription , ?cUUID, ?cName, ?cCas, ?cFormula , ?cLastChange) as ?newDescription) \n");
			b.append("  } \n");
			b.append("   \n");
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
