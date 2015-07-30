package gov.epa.nrmrl.std.lca.ht.output;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dialog.ChooseDataSetDialog;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.MatchContexts;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.MatchProperties;
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Temporal;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;
import gov.epa.nrmrl.std.lca.ht.vocabulary.OpenLCA;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class SaveHarmonizedDataForOLCAJsonld implements IHandler {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.output.SaveHarmonizedDataForOLCAJsonld";

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
	}

	private void writeResource(String folder, String filename, String fileContents, ZipOutputStream output)
			throws IOException {
		String separator = "/";

		try {
			separator = System.getProperty("file.separator");
		} catch (Exception e) {
			e.printStackTrace();
		}

		output.putNextEntry(new ZipEntry(folder + separator + filename));
		output.write(fileContents.getBytes());
		output.closeEntry();
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		boolean nestedCall = true;
		Shell shell = null;
		String currentName = event.getParameter("LCA-HT.exportDataSetName");
		
		if (currentName == null) {
			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			ChooseDataSetDialog dlg = new ChooseDataSetDialog(shell, false);
			dlg.open();
			currentName = dlg.getSelection();
		}
		
		String saveTo = event.getParameter("LCA-HT.outputFilename");

		Logger runLogger = Logger.getLogger("run");

		System.out.println("Saving Harmonized Data to .jsonld file");

		if (saveTo == null) {
			Util.findView(MatchContexts.ID);
			Util.findView(MatchProperties.ID);

			nestedCall = false;
			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			FileDialog dialog = new FileDialog(shell, SWT.SAVE);
			String[] filterNames = new String[] { "Json Files", "Jsonld Files", "Turtle Files", "Zip Files" };
			String[] filterExtensions = new String[] { "*.json", "*.jsonld", "*.ttl" };

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
			dialog.setFileName(currentName + "_harmonized");

			saveTo = dialog.open();
		}
		System.out.println("Save to: " + saveTo);
		if (saveTo == null) {
			// "dialog was cancelled or an error occurred"
			return null;
		}

		runLogger.info("  # Writing RDF triples to " + saveTo.toString());
		if (nestedCall) 
			writeData(saveTo, currentName);
		else {
			final String save = saveTo;
			final String ds = currentName;
			new Thread(new Runnable() { public void run() {
				disableButtons();
				writeData(save, ds);
				enableButtons();
			}}).start();
		}
		return null;
	}
	
	private void disableButtons() {
		Display.getDefault().syncExec(new Runnable() { public void run() {
			FlowsWorkflow.disableAllButtons();
		}});
	}
	
	private void enableButtons() {
		Display.getDefault().syncExec(new Runnable() { public void run() {
			FlowsWorkflow.restoreAllButtons();
		}});
	}
	
	public void writeData(String saveTo, String currentName) {
		// ActiveTDB.copyDatasetContentsToExportGraph(currentName);
		// List<Statement> statements = ActiveTDB.collectAllStatementsForDataset(currentName, null);
		/*
		 * The following line collects essentially every item in the TDB default graph that has the predicate, object
		 * pair: eco:hasDataSource [dataset to export]. This Set is then parsed and binned according to what type of
		 * object it is. Then objects are read in batches from simplest (i.e. not containing reference to more complex
		 * types) to more complex, and "followed" to include all attributes. If the attribute is Literal the single
		 * statement is added, if it is another node (URI type or blank = Anonymous) Note that: In the first attempt
		 * (which does not work) nodes are not followed if they belongs to a specified "stop class". When "flows" are
		 * read their UUIDs are compared with those found via harmonization.
		 */
		Set<Resource> datasetMembers = ActiveTDB.getDatasetMemberSubjects(currentName, null);

		Set<RDFNode> stopAtTheseClasses = new HashSet<RDFNode>();
		stopAtTheseClasses.add(OpenLCA.Actor);
		// stopAtTheseClasses.add(OpenLCA.Category);
		// stopAtTheseClasses.add(OpenLCA.FlowProperty);
		stopAtTheseClasses.add(OpenLCA.Flow);
		stopAtTheseClasses.add(OpenLCA.ImpactCategory);
		stopAtTheseClasses.add(OpenLCA.ImpactMethod);
		stopAtTheseClasses.add(OpenLCA.Location);
		stopAtTheseClasses.add(OpenLCA.Process);
		stopAtTheseClasses.add(OpenLCA.Source);
		stopAtTheseClasses.add(OpenLCA.UnitGroup);
		stopAtTheseClasses.add(FedLCA.Person);
		stopAtTheseClasses.add(LCAHT.DataFile);
		stopAtTheseClasses.add(ECO.DataSource);
		stopAtTheseClasses.add(ECO.Flowable);
		stopAtTheseClasses.add(FedLCA.Flow);
		stopAtTheseClasses.add(FedLCA.FlowContext);
		stopAtTheseClasses.add(FedLCA.FlowUnit);

		// if (saveTo.endsWith(".zip")) {
		// /*
		// * FAILED ATTEMPT TO WRITE INDIVIDUAL .json FILES TO SPECIFIC DIRECTORIES /* The order of the items below is
		// * critical since detection of changes in some objects must be propagated to objects that contain them.
		// * During preparation of each .json file, Comparisons will be consulted to see what changes should be made
		// */
		// Map<String, Set<Resource>> resourceMap = new LinkedHashMap<String, Set<Resource>>();
		// resourceMap.put("actors", new HashSet<Resource>());
		// resourceMap.put("categories", new HashSet<Resource>());
		// resourceMap.put("flow_properties", new HashSet<Resource>());
		// resourceMap.put("locations", new HashSet<Resource>());
		// resourceMap.put("sources", new HashSet<Resource>());
		// resourceMap.put("unit_groups", new HashSet<Resource>());
		//
		// resourceMap.put("flows", new HashSet<Resource>());
		//
		// resourceMap.put("processes", new HashSet<Resource>());
		// resourceMap.put("lcia_categories", new HashSet<Resource>());
		// resourceMap.put("lcia_methods", new HashSet<Resource>());
		//
		// // resourceMap.put("unmatched_resources", new HashSet<Resource>());
		//
		// // First, sort members into batches
		// int memberCount = datasetMembers.size();
		// for (Resource itemResource : datasetMembers) {
		// if (itemResource.hasProperty(RDF.type, OpenLCA.Actor)) {
		// resourceMap.get("actors").add(itemResource);
		// } else if (itemResource.hasProperty(RDF.type, OpenLCA.Category)) {
		// resourceMap.get("categories").add(itemResource);
		// } else if (itemResource.hasProperty(RDF.type, OpenLCA.FlowProperty)) {
		// resourceMap.get("flow_properties").add(itemResource);
		// } else if (itemResource.hasProperty(RDF.type, OpenLCA.Flow)) {
		// resourceMap.get("flows").add(itemResource);
		// } else if (itemResource.hasProperty(RDF.type, OpenLCA.ImpactCategory)) {
		// resourceMap.get("lcia_categories").add(itemResource);
		// } else if (itemResource.hasProperty(RDF.type, OpenLCA.ImpactMethod)) {
		// resourceMap.get("lcia_methods").add(itemResource);
		// } else if (itemResource.hasProperty(RDF.type, OpenLCA.Location)) {
		// resourceMap.get("locations").add(itemResource);
		// } else if (itemResource.hasProperty(RDF.type, OpenLCA.Process)) {
		// resourceMap.get("processes").add(itemResource);
		// } else if (itemResource.hasProperty(RDF.type, OpenLCA.Source)) {
		// resourceMap.get("sources").add(itemResource);
		// } else if (itemResource.hasProperty(RDF.type, OpenLCA.UnitGroup)) {
		// resourceMap.get("unit_groups").add(itemResource);
		// } else {
		// memberCount--;
		// // resourceMap.get("unmatched_resources").add(itemResource);
		// }
		// }
		//
		// Set<RDFNode> subClassesNotToPackageSeparately = new HashSet<RDFNode>();
		// subClassesNotToPackageSeparately.add(OpenLCA.Exchange);
		// subClassesNotToPackageSeparately.add(OpenLCA.FlowPropertyFactor);
		// subClassesNotToPackageSeparately.add(OpenLCA.ProcessDocumentation);
		// subClassesNotToPackageSeparately.add(OpenLCA.Uncertainty);
		// subClassesNotToPackageSeparately.add(ECO.Flowable);
		// subClassesNotToPackageSeparately.add(ECO.DataSource);
		// subClassesNotToPackageSeparately.add(ECO.Flowable);
		// subClassesNotToPackageSeparately.add(FedLCA.Flow);
		// subClassesNotToPackageSeparately.add(FedLCA.FlowContext);
		// subClassesNotToPackageSeparately.add(FedLCA.FlowUnit);
		// subClassesNotToPackageSeparately.add(FedLCA.Person);
		// subClassesNotToPackageSeparately.add(LCAHT.DataFile);
		//
		// try {
		// ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(saveTo));
		// int total = 0;
		// Map<String, String> oldNewUUIDMap = new HashMap<String, String>();
		// for (String folderKey : resourceMap.keySet()) {
		// // List<String> uuidsToReplace = new LinkedList<String>();
		// System.out.println("Working on '" + folderKey + "' files");
		// Set<Resource> hashSet = resourceMap.get(folderKey);
		// int lastPercent = -1;
		// for (Resource itemResource : hashSet) {
		// total++;
		// int percent = 100 * total / memberCount;
		//
		// if (percent > lastPercent + 1) {
		// System.out.println(percent + " % complete");
		// lastPercent = percent;
		// }
		//
		// /*
		// * Confirm that this item should be processed (i.e. URI is a UUID)
		// */
		// String itemUUID = ActiveTDB.getUUIDFromRDFNode(itemResource);
		// if (itemUUID == null) {
		// for (Statement statement : itemResource.listProperties(RDF.type).toList()) {
		// if (!subClassesNotToPackageSeparately.contains(statement.getObject())) {
		// System.out.println("Found in '" + folderKey + "' a thing with class: "
		// + statement.getObject());
		// }
		// }
		// continue;
		// }
		//
		// Set<RDFNode> singleSet = new HashSet<RDFNode>();
		// singleSet.add(itemResource);
		//
		// // List<Statement> statements = ActiveTDB.collectStatementsTraversingNodeSetWithStops(singleSet,
		// // stopAtTheseClasses, null);
		// List<Statement> statements = ActiveTDB.collectStatementsStopAtQualifiedURIsWithStops(singleSet,
		// stopAtTheseClasses, null);
		// // List<Statement> statements = ActiveTDB.collectStatementsStopAtQualifiedURIs(singleSet, null);
		//
		// ActiveTDB.clearExportGraphContents();
		// ActiveTDB.copyStatementsToGraph(statements, ActiveTDB.exportGraphName);
		//
		// /*
		// * Here begins the test to manage exactly what changes are made to what types of objects Some
		// * require nothing 1) flows require changing info to the harmonized flow, but creating
		// * "description" and "lastChange" 2) processes require changing the "flow" info and info about
		// * Exchanges
		// */
		// if (folderKey.equals("flows")) {
		// // Only ELEMENTARY_FLOW Flows will have changes (at this point)
		// if (ActiveTDB.getModel(ActiveTDB.exportGraphName).contains(itemResource, OpenLCA.flowType,
		// OpenLCA.ELEMENTARY_FLOW)) {
		//
		// List<Resource> matchingMasters = getOLCAMatchingMasterResources(itemUUID);
		// if (matchingMasters.size() > 1) {
		// System.out.println("Got multiple matches.  Count is :" + matchingMasters.size());
		// } else if (matchingMasters.size() == 1) {
		// Map<String, RDFNode> itemProperties = getFlowFeatureLiterals(itemResource);
		// Map<String, RDFNode> masterProperties = getFlowFeatureLiterals(matchingMasters
		// .get(0));
		// // Update description and lastChange if needed
		// String changes = replaceUserLiterals(itemResource, itemProperties, masterProperties);
		// if (!changes.equals("")) {
		// RDFNode oldDescription = itemProperties.get("description");
		// if (oldDescription == null || oldDescription.asLiteral().getString().equals("")) {
		// updateDescription(itemResource, changes);
		// } else {
		// String newDescription = itemProperties.get("description").asLiteral()
		// .getString()
		// + " -> " + changes;
		// updateDescription(itemResource, newDescription);
		// }
		// }
		//
		// // Now check UUIDs of some things
		// RDFNode newUUIDNode = masterProperties.get("uuid");
		// String newUUID = ActiveTDB.getUUIDFromRDFNode(newUUIDNode);
		// if (newUUID == null) {
		// newUUID = Util.getRandomUUID();
		// // TODO: HANDLE THIS SITUATION BETTER
		// }
		// if (!newUUID.equals(itemUUID)) {
		// oldNewUUIDMap.put(itemUUID, newUUID);
		// }
		//
		// // Handle FlowCategory (context)
		// RDFNode itemCategory = itemProperties.get("category");
		// String itemCategoryUUID = ActiveTDB.getUUIDFromRDFNode(itemCategory);
		// if (!oldNewUUIDMap.containsKey(itemCategoryUUID)) {
		// RDFNode masterCategory = masterProperties.get("category");
		// Statement findUUIDStatement = masterCategory.asResource().getProperty(
		// FedLCA.hasOpenLCAUUID);
		// String masterContextUUID = ActiveTDB.getUUIDFromRDFNode(findUUIDStatement
		// .getObject());
		// if (!itemCategoryUUID.equals(masterContextUUID)) {
		// removeCategories(itemCategory);
		// oldNewUUIDMap.put(itemCategoryUUID, masterContextUUID);
		// }
		// }
		// // Handle FlowProperty and FlowUnit
		// RDFNode itemProperty = itemProperties.get("flow_properties");
		// Statement firstFlowPropertyStatement = itemProperty.asResource().getProperty(
		// OpenLCA.flowProperty);
		// String itemPropertyUUID = ActiveTDB.getUUIDFromRDFNode(firstFlowPropertyStatement
		// .getObject().asResource());
		// if (!oldNewUUIDMap.containsKey(itemPropertyUUID)) {
		// RDFNode masterProperty = masterProperties.get("flow_properties");
		// Statement findUUIDStatement = masterProperty.asResource().getProperty(
		// FedLCA.hasOpenLCAUUID);
		// String masterPropertyUUID = ActiveTDB.getUUIDFromRDFNode(findUUIDStatement
		// .getObject());
		// if (!itemPropertyUUID.equals(masterPropertyUUID)) {
		// removeFlowProperty(itemProperty);
		// oldNewUUIDMap.put(itemPropertyUUID, masterPropertyUUID);
		// }
		// }
		// }
		// }
		// }
		//
		// // Update the lastChange anyway
		// updateLastChange(itemResource);
		// /*
		// * Now copy contents of the graph to a string in .json format
		// */
		// StringWriter stringOut = new StringWriter();
		// ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		// Model tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		// tdbModel.write(stringOut, "JSON-LD");
		//
		// /*
		// * Now some post-processing string manipulation to make openLCA happy
		// */
		// ActiveTDB.tdbDataset.end();
		// String fileContents = stringOut.toString();
		// for (String from : oldNewUUIDMap.keySet()) {
		// String newString = fileContents.replaceAll(from, oldNewUUIDMap.get(from));
		// fileContents = newString;
		// }
		// String cleanedFileContents1 = fileContents.replaceAll("\"olca:", "\"");
		// String cleanedFileContents2 = cleanedFileContents1.replaceAll(
		// " \"@id\" : \"urn:x-arq:DefaultGraphNode\",", "");
		//
		// /*
		// * Now append to the .zip file with the individual .json file
		// */
		// String fileName = itemUUID + ".json";
		// if (oldNewUUIDMap.containsKey(itemUUID)) {
		// fileName = oldNewUUIDMap.get(itemUUID) + ".json";
		// }
		//
		// writeResource(folderKey, fileName, cleanedFileContents2, zipFile);
		// }
		// }
		// zipFile.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// return null;
		// }

		/*
		 * ===========================================================================================================
		 * == Alternate approach - copy things, then zip up ==========================================================
		 * ===========================================================================================================
		 */

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				FlowsWorkflow.setTextConcludeStatus("0/3 steps to complete");
			}
		});
		Map<String, String> userFlow2masterFlow = new HashMap<String, String>();
		Map<String, String> userFlow2masterUnit = new HashMap<String, String>();
		Map<String, Double> masterUnit2convFactor = new HashMap<String, Double>();

		// First - collect info on Flow Units
		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		b.append("select distinct \n ");
		b.append("  ?row \n ");
		b.append("  ?userFlowUUID \n ");
		b.append("  ?masterFlowUUID \n ");
		b.append("  ?masterUnitUUID \n ");
		b.append("  ?fedlcaMasterIsReferenceUnit \n ");
		b.append("  ?olcaMasterIsReferenceUnit \n ");
		b.append("  ?masterConversionFactor \n ");
		b.append("  ?olcaMasterConversionFactor \n ");
		b.append("where { \n ");
		b.append(" \n ");
		b.append("  ?uf a fedlca:Flow . \n ");
		b.append("  ?uf fedlca:hasOpenLCAUUID ?userFlowUUID . \n ");
		b.append("  ?uf eco:hasDataSource ?uds . \n ");
		b.append("  ?uds rdfs:label \"" + currentName + "\"^^xsd:string . \n ");
		b.append("  ?uf fedlca:sourceTableRowNumber ?row . \n ");
		b.append("  ?c fedlca:comparedSource ?uf . \n ");
		b.append("  ?c fedlca:comparedMaster ?mf . \n ");
		b.append("  ?c fedlca:comparedEquivalence fedlca:Equivalent . \n ");
		b.append("  ?mf fedlca:hasOpenLCAUUID ?masterFlowUUID . \n ");
		b.append(" \n ");
		b.append("  ?uf fedlca:hasFlowUnit ?ufu . \n ");
		b.append("  ?ufu a fedlca:FlowUnit . \n ");
		b.append(" \n ");
		b.append("  ?ufu owl:sameAs ?mfu . \n ");
		b.append("  ?mfu fedlca:unitConversionFactor ?masterConversionFactor . \n ");
		b.append("  ?mug a fedlca:UnitGroup . \n ");
		b.append("  ?mug fedlca:hasFlowUnit ?mfu . \n ");
		b.append("  ?mug fedlca:hasReferenceUnit ?mrefU . \n ");
		b.append("  ?mfu fedlca:hasOpenLCAUUID ?uuid_typed . \n ");
		b.append("  bind (if((?mrefU = ?mfu),true,false) as ?fedlcaMasterIsReferenceUnit ) \n ");
		b.append(" \n ");
		b.append("  bind (str(?uuid_typed) as ?masterUnitUUID) \n ");
		b.append("  bind (IRI(concat(\"http://openlca.org/schema/v1.0/\",?masterUnitUUID)) as ?olcaMasterUnitURI ) \n ");
		b.append("  ?olcaMasterUnitURI olca:conversionFactor ?olcaMasterConversionFactor . \n ");
		b.append("  optional { ?olcaMasterUnitURI olca:referenceUnit ?olcaMasterIsReferenceUnit .} \n ");
		b.append("} \n ");
		b.append("order by ?row \n ");
		b.append(" \n ");
		String query = b.toString();

		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);

		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			RDFNode rowNode = querySolution.get("row");
			int row = rowNode.asLiteral().getInt();

			RDFNode rdfNode2 = querySolution.get("userFlowUUID");
			String userFlowUUID = rdfNode2.asLiteral().getString();

			RDFNode rdfNode3 = querySolution.get("masterFlowUUID");
			String masterFlowUUID = rdfNode3.asLiteral().getString();

			RDFNode rdfNode4 = querySolution.get("masterUnitUUID");
			String masterUnitUUID = rdfNode4.asLiteral().getString();

			RDFNode rdfNode5 = querySolution.get("fedlcaMasterIsReferenceUnit");
			boolean fedlcaMasterIsRef = false;
			if (rdfNode5 != null) {
				if (rdfNode5.asLiteral().getBoolean()) {
					fedlcaMasterIsRef = true;
				}
			}
			RDFNode rdfNode6 = querySolution.get("olcaMasterIsReferenceUnit");
			boolean olcaMasterIsRef = false;
			if (rdfNode6 != null) {
				if (rdfNode6.asLiteral().getBoolean()) {
					olcaMasterIsRef = true;
				}
			}
			if ((fedlcaMasterIsRef && !olcaMasterIsRef) || (!fedlcaMasterIsRef && olcaMasterIsRef)) {
				System.out.println("Reference unit mismatch: " + fedlcaMasterIsRef + " in FedLCA and "
						+ olcaMasterIsRef + "in OpenLCA");
			}

			RDFNode rdfNode7 = querySolution.get("masterConversionFactor");
			double masterConversionFactor = rdfNode7.asLiteral().getDouble();
			RDFNode rdfNode8 = querySolution.get("olcaMasterConversionFactor");
			double olcaConversionFactor = rdfNode8.asLiteral().getDouble();
			if (masterConversionFactor != olcaConversionFactor) {
				System.out.println("Conversion factor mismatch: " + masterConversionFactor + " in FedLCA and "
						+ olcaConversionFactor + "in OpenLCA");
			}
			if (masterConversionFactor != 1 && fedlcaMasterIsRef) {
				System.out.println("Reference unit: " + masterUnitUUID + "has conversion factor: "
						+ masterConversionFactor + " -- instead of 1.0");
			}
			/*
			 * TODO: Check the logic here, but only need to make a note for a change if not a reference unit (which
			 * implies conversion factor 1, but cf = 1 does not imply reference)
			 */
			if (!fedlcaMasterIsRef || !olcaMasterIsRef || (olcaConversionFactor != 1.0)
					|| (masterConversionFactor != 1.0)) {
				userFlow2masterFlow.put(userFlowUUID, masterFlowUUID);
				userFlow2masterUnit.put(userFlowUUID, masterUnitUUID);
				masterUnit2convFactor.put(userFlowUUID, olcaConversionFactor);
			}
		}

		/*
		 * The order of the items below is critical since detection of changes in some objects must be propagated to
		 * objects that contain them. During preparation of each .json file, Comparisons will be consulted to see what
		 * changes should be made
		 */
		Map<String, Set<Resource>> resourceMap = new LinkedHashMap<String, Set<Resource>>();
		resourceMap.put("actors", new HashSet<Resource>());
		resourceMap.put("categories", new HashSet<Resource>());
		resourceMap.put("flow_properties", new HashSet<Resource>());
		resourceMap.put("locations", new HashSet<Resource>());
		resourceMap.put("sources", new HashSet<Resource>());
		resourceMap.put("unit_groups", new HashSet<Resource>());

		resourceMap.put("flows", new HashSet<Resource>());

		resourceMap.put("processes", new HashSet<Resource>());
		resourceMap.put("lcia_categories", new HashSet<Resource>());
		resourceMap.put("lcia_methods", new HashSet<Resource>());

		// resourceMap.put("unmatched_resources", new HashSet<Resource>());

		// First, sort members into batches
		int memberCount = datasetMembers.size();
		for (Resource itemResource : datasetMembers) {
			if (itemResource.hasProperty(RDF.type, OpenLCA.Actor)) {
				resourceMap.get("actors").add(itemResource);
			} else if (itemResource.hasProperty(RDF.type, OpenLCA.Category)) {
				resourceMap.get("categories").add(itemResource);
			} else if (itemResource.hasProperty(RDF.type, OpenLCA.FlowProperty)) {
				resourceMap.get("flow_properties").add(itemResource);
			} else if (itemResource.hasProperty(RDF.type, OpenLCA.Flow)) {
				resourceMap.get("flows").add(itemResource);
			} else if (itemResource.hasProperty(RDF.type, OpenLCA.ImpactCategory)) {
				resourceMap.get("lcia_categories").add(itemResource);
			} else if (itemResource.hasProperty(RDF.type, OpenLCA.ImpactMethod)) {
				resourceMap.get("lcia_methods").add(itemResource);
			} else if (itemResource.hasProperty(RDF.type, OpenLCA.Location)) {
				resourceMap.get("locations").add(itemResource);
			} else if (itemResource.hasProperty(RDF.type, OpenLCA.Process)) {
//				System.out.println("Process count: " + resourceMap.get("processes").size());
				resourceMap.get("processes").add(itemResource);
			} else if (itemResource.hasProperty(RDF.type, OpenLCA.Source)) {
				resourceMap.get("sources").add(itemResource);
			} else if (itemResource.hasProperty(RDF.type, OpenLCA.UnitGroup)) {
				resourceMap.get("unit_groups").add(itemResource);
			} else {
				memberCount--;
				// resourceMap.get("unmatched_resources").add(itemResource);
			}
		}

		// Set<RDFNode> stopAtTheseClasses = new HashSet<RDFNode>();
		// stopAtTheseClasses.add(OpenLCA.Actor);
		// // stopAtTheseClasses.add(OpenLCA.Category);
		// // stopAtTheseClasses.add(OpenLCA.FlowProperty);
		// stopAtTheseClasses.add(OpenLCA.Flow);
		// stopAtTheseClasses.add(OpenLCA.ImpactCategory);
		// stopAtTheseClasses.add(OpenLCA.ImpactMethod);
		// stopAtTheseClasses.add(OpenLCA.Location);
		// stopAtTheseClasses.add(OpenLCA.Process);
		// stopAtTheseClasses.add(OpenLCA.Source);
		// stopAtTheseClasses.add(OpenLCA.UnitGroup);
		// stopAtTheseClasses.add(FedLCA.Person);
		// stopAtTheseClasses.add(LCAHT.DataFile);
		// stopAtTheseClasses.add(ECO.DataSource);
		// stopAtTheseClasses.add(ECO.Flowable);
		// stopAtTheseClasses.add(FedLCA.Flow);
		// stopAtTheseClasses.add(FedLCA.FlowContext);
		// stopAtTheseClasses.add(FedLCA.FlowUnit);

		Set<RDFNode> subClassesNotToPackageSeparately = new HashSet<RDFNode>();
		subClassesNotToPackageSeparately.add(OpenLCA.Exchange);
		subClassesNotToPackageSeparately.add(OpenLCA.FlowPropertyFactor);
		subClassesNotToPackageSeparately.add(OpenLCA.ProcessDocumentation);
		subClassesNotToPackageSeparately.add(OpenLCA.Uncertainty);
		subClassesNotToPackageSeparately.add(ECO.Flowable);
		subClassesNotToPackageSeparately.add(ECO.DataSource);
		subClassesNotToPackageSeparately.add(ECO.Flowable);
		subClassesNotToPackageSeparately.add(FedLCA.Flow);
		subClassesNotToPackageSeparately.add(FedLCA.FlowContext);
		subClassesNotToPackageSeparately.add(FedLCA.FlowUnit);
		subClassesNotToPackageSeparately.add(FedLCA.Person);
		subClassesNotToPackageSeparately.add(LCAHT.DataFile);

		// try {
		// ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(saveTo));
		int total = 0;
		Map<String, String> oldNewFlowUUIDMap = new HashMap<String, String>();
		Map<String, String> oldNewOtherUUIDMap = new HashMap<String, String>();

		ActiveTDB.clearExportGraphContents();
		int lastPercent = -1;
		for (String folderKey : resourceMap.keySet()) {
			// List<String> uuidsToReplace = new LinkedList<String>();
//			System.out.println("Working on '" + folderKey + "' files");
			Set<Resource> hashSet = resourceMap.get(folderKey);
			for (Resource itemResource : hashSet) {
				total++;
				int percent = 100 * total / memberCount;

				if (percent >= lastPercent + 1) {
					// Ready for when this routine becomes threaded
					final int percentToWrite = percent;
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							FlowsWorkflow.setTextConcludeStatus("1/3 preparing components " + percentToWrite + "%");
						}
					});
					System.out.println("1/3 " + percent + "% complete");
					lastPercent = percent;
				}

				/*
				 * Confirm that this item should be processed (i.e. URI is a UUID)
				 */
				String itemUUID = ActiveTDB.getUUIDFromRDFNode(itemResource);
				if (itemUUID == null) {
					for (Statement statement : itemResource.listProperties(RDF.type).toList()) {
						if (!subClassesNotToPackageSeparately.contains(statement.getObject())) {
							System.out.println("Found in '" + folderKey + "' a thing with class: "
									+ statement.getObject());
						}
					}
					continue;
				}

				Set<RDFNode> singleSet = new HashSet<RDFNode>();
				singleSet.add(itemResource);

				// List<Statement> statements = ActiveTDB.collectStatementsTraversingNodeSetWithStops(singleSet,
				// stopAtTheseClasses, null);
				List<Statement> statements = ActiveTDB.collectStatementsTraversingNodeSetWithStops(singleSet,
						stopAtTheseClasses, null);
				// List<Statement> statements = ActiveTDB.collectStatementsStopAtQualifiedURIs(singleSet, null);

				// ActiveTDB.clearExportGraphContents();
				ActiveTDB.copyStatementsToGraph(statements, ActiveTDB.exportGraphName);

				/*
				 * Here begins the test to manage exactly what changes are made to what types of objects Some require
				 * nothing 1) flows require changing info to the harmonized flow, but creating "description" and
				 * "lastChange" 2) processes require changing the "flow" info and info about Exchanges
				 */
				if (folderKey.equals("flows")) {
//					if (itemUUID.equals("37236b2f-b18d-35a7-9860-d9149c1763f1")) {
//						System.out.println("pause here");
//					}
					// Only ELEMENTARY_FLOW Flows will have changes (at this point)
					if (ActiveTDB.getModel(ActiveTDB.exportGraphName).contains(itemResource, OpenLCA.flowType,
							OpenLCA.ELEMENTARY_FLOW)) {

						List<Resource> matchingMasters = getOLCAMatchingMasterResources(itemUUID);
						if (matchingMasters.size() > 1) {
							System.out.println("Got multiple matches.  Count is :" + matchingMasters.size());
						} else if (matchingMasters.size() == 1) {
							Map<String, RDFNode> itemProperties = getFlowFeatureLiterals(itemResource);
							Map<String, RDFNode> masterProperties = getFlowFeatureLiterals(matchingMasters.get(0));
							// Update description and lastChange if needed
							String changes = replaceUserLiterals(itemResource, itemProperties, masterProperties);
							if (!changes.equals("")) {
								RDFNode oldDescription = itemProperties.get("description");
								if (oldDescription == null || oldDescription.asLiteral().getString().equals("")) {
									updateDescription(itemResource, changes);
								} else {
									String newDescription = itemProperties.get("description").asLiteral().getString()
											+ " -> " + changes;
									updateDescription(itemResource, newDescription);
								}
							}

							// Now check UUIDs of some things
							RDFNode newUUIDNode = masterProperties.get("uuid");
							String newUUID = null;
							if (newUUIDNode == null) {
								newUUID = Util.getRandomUUID();
								// TODO: HANDLE THIS SITUATION BETTER
							} else {
								newUUID = ActiveTDB.getUUIDFromRDFNode(newUUIDNode);
							}
							if (!newUUID.equals(itemUUID)) {
								oldNewFlowUUIDMap.put(itemUUID, newUUID);
								updateLastChange(itemResource);
							}

							// Handle FlowCategory (context)
							RDFNode itemCategory = itemProperties.get("category");
							String itemCategoryUUID = ActiveTDB.getUUIDFromRDFNode(itemCategory);
							if (!oldNewOtherUUIDMap.containsKey(itemCategoryUUID)) {
								RDFNode masterCategory = masterProperties.get("category");
								Statement findUUIDStatement = masterCategory.asResource().getProperty(
										FedLCA.hasOpenLCAUUID);
								String masterContextUUID = ActiveTDB.getUUIDFromRDFNode(findUUIDStatement.getObject());
								if (!itemCategoryUUID.equals(masterContextUUID)) {
									// removeCategories(itemCategory);
									oldNewOtherUUIDMap.put(itemCategoryUUID, masterContextUUID);
								}
							}
							// Handle FlowProperty and FlowUnit
//							if (userFlow2masterFlow.containsKey(itemUUID)) {
//								System.out.println("UUID and master are: " + itemUUID + " and "
//										+ userFlow2masterFlow.get(itemUUID));
//							}
							RDFNode itemProperty = itemProperties.get("flow_properties");
							Statement firstFlowPropertyStatement = itemProperty.asResource().getProperty(
									OpenLCA.flowProperty);
							String itemPropertyUUID = ActiveTDB.getUUIDFromRDFNode(firstFlowPropertyStatement
									.getObject().asResource());
//							if (!itemPropertyUUID.equals(userFlow2masterUnit.get(itemUUID))) {
								// Careful here. Because the same flow can have different units, we must update the unit
								// in this flow, but not all instances this unit...
								// so, do nothing here, but deal with it in phase 2
//								if (userFlow2masterFlow.containsKey(itemUUID)) {
//									System.out.println("Flow: " + itemUUID + " will get new unit: "
//											+ userFlow2masterUnit.get(itemUUID) + ". Not: " + itemPropertyUUID);
//								}
//							}
							if (!oldNewOtherUUIDMap.containsKey(itemPropertyUUID)) {
								RDFNode masterProperty = masterProperties.get("flow_properties");
								Statement findUUIDStatement = masterProperty.asResource().getProperty(
										FedLCA.hasOpenLCAUUID);
								String masterPropertyUUID = ActiveTDB.getUUIDFromRDFNode(findUUIDStatement.getObject());
								if (!itemPropertyUUID.equals(masterPropertyUUID)) {
									// removeFlowProperty(itemProperty);
									oldNewOtherUUIDMap.put(itemPropertyUUID, masterPropertyUUID);
								}
							}
						}
					}
				}
			}
		}
		// runAllUUIDReplacements(oldNewFlowUUIDMap, oldNewOtherUUIDMap, stopAtTheseClasses);
		// runUnitUUIDReplacements(userFlow2masterFlow, userFlow2masterUnit, masterUnit2convFactor);
		runAllUUIDReplacements2(oldNewFlowUUIDMap, oldNewOtherUUIDMap, userFlow2masterUnit, masterUnit2convFactor);

		try {
			FileOutputStream fout = new FileOutputStream(saveTo);
			String outType = ActiveTDB.getRDFTypeFromSuffix(saveTo);

			// --- BEGIN SAFE -READ- TRANSACTION ---
			ActiveTDB.tdbDataset.begin(ReadWrite.READ);
			Model tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
			tdbModel.write(fout, outType);
			ActiveTDB.tdbDataset.end();
			fout.close();
			removePrefix(saveTo);
			// ---- END SAFE -WRITE- TRANSACTION ---

		} catch (FileNotFoundException e1) {
			ActiveTDB.tdbDataset.abort();
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		ActiveTDB.clearExportGraphContents();

		/* To copy a dataset to the export graph */
		// ActiveTDB.copyDatasetContentsToExportGraph(olca);
	}

	private void removeCategories(RDFNode categoryRDFNode) {
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		try {
			Resource categoryResource = categoryRDFNode.asResource();
			Resource parentResource = categoryResource.getPropertyResourceValue(OpenLCA.parentCategory);
			tdbModel.removeAll(categoryResource, null, null);
			tdbModel.removeAll(parentResource, null, null);
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("Update description failed; see Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
	}

	private void removeFlowProperty(RDFNode propertyRDFNode) {
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		try {
			Resource propertyResource = propertyRDFNode.asResource();
			tdbModel.removeAll(propertyResource, null, null);
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("Update description failed; see Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
	}

	private void runAllUUIDReplacements2(Map<String, String> oldNewFlowUUIDMap, Map<String, String> oldNewOtherUUIDMap,
			Map<String, String> oldNewFlowUnitUUIDMap, Map<String, Double> oldNewConvFactorMap) {
		// while (ActiveTDB.tdbDataset.isInTransaction()){
		// ActiveTDB.tdbDataset.end();
		// }
		int totalToDo = oldNewFlowUUIDMap.size() + oldNewOtherUUIDMap.size();
		int totalDone = 0;
		int lastPercentComplete = -1;
		Set<RDFNode> otherThingsToBringIn = new HashSet<RDFNode>();
		List<Statement> statementsToAdd = new ArrayList<Statement>();
		List<Statement> statementsToRemove = new ArrayList<Statement>();
		// List<RDFNode> objectsToAdd = new ArrayList<RDFNode>();
		// List<RDFNode> objectsToRemove = new ArrayList<RDFNode>();

		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model expModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		for (String oldUUID : oldNewFlowUUIDMap.keySet()) {
			if (oldUUID.equals("37236b2f-b18d-35a7-9860-d9149c1763f1")) {
				System.out.println("pause here");
			}
			if (oldUUID.equals("fc1c42ce-a759-49fa-b987-f1ec5e503db1")) {
				System.out.println("pause here");
			}

			totalDone++;
			int percent = 100 * totalDone / totalToDo;
			if (percent >= lastPercentComplete + 1) {
				final int percentToWrite = percent;
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						FlowsWorkflow.setTextConcludeStatus("2/3 updating components " + percentToWrite + "%");
					}
				});
				System.out.println("2/3 " + percent + "% complete");
				lastPercentComplete = percent;
			}
			String newUUID = oldNewFlowUUIDMap.get(oldUUID);
			Resource oldFlowResource = expModel.createResource(OpenLCA.NS + oldUUID);
			Resource newFlowResource = expModel.createResource(OpenLCA.NS + newUUID);
			// BEWARE: listStatements DOES NOT seem to capture statements with non-literal objects
			// StmtIterator stmtIterator0 = expModel.listStatements(oldFlowResource, null, null, null);
			// Get all the statements with this Flow as subject. Note that the Flow doesn't change even if the FlowUnit
			// does
			Selector selector0 = new SimpleSelector(oldFlowResource, null, null, null);
			StmtIterator stmtIterator0 = expModel.listStatements(selector0);
			while (stmtIterator0.hasNext()) {
				Statement statement = stmtIterator0.next();
				statementsToRemove.add(statement);
				RDFNode predicate = statement.getPredicate();
//				System.out.println("Predicate : " + predicate.asResource().getURI());
				RDFNode object = statement.getObject();
				if (!object.isLiteral() && !object.isAnon()) {
					String uri = object.asResource().getURI();
					if (uri.length() == 36 + OpenLCA.NS.length()) {
						String oldUUIDObject = uri.substring(uri.length() - 36);
						if (oldNewOtherUUIDMap.containsKey(oldUUIDObject)) {
							String newUUIDObject = oldNewOtherUUIDMap.get(oldUUIDObject);
							object = expModel.createResource(OpenLCA.NS + newUUIDObject);
						}
					}
				}
				Statement addStatement = expModel.createStatement(newFlowResource, statement.getPredicate(), object);
				statementsToAdd.add(addStatement);
			}

			// Now get all the statements with this Flow as an object.

			Selector selector1 = new SimpleSelector(null, null, oldFlowResource);
			StmtIterator stmtIterator1 = expModel.listStatements(selector1);
			while (stmtIterator1.hasNext()) {
				Statement statement = stmtIterator1.next();
				statementsToRemove.add(statement);
				Resource subject = statement.getSubject();
				Property predicate = statement.getPredicate();
				Statement addStatement = expModel.createStatement(subject, predicate, newFlowResource);
				statementsToAdd.add(addStatement);
				if (subject.isAnon() && predicate.equals(OpenLCA.flow)) {
					if (oldNewConvFactorMap.containsKey(oldUUID) && oldNewFlowUnitUUIDMap.containsKey(oldUUID)) {
						boolean isExchangeOrImpactFactor = false;
						// Determine what type of thing this is that has the "Flow" in question
						if (expModel.contains(subject, RDF.type, OpenLCA.Exchange)) {
							isExchangeOrImpactFactor = true;
							Selector selector2 = new SimpleSelector(subject, OpenLCA.amount, null, null);
							StmtIterator stmtIterator2 = expModel.listStatements(selector2);
							// TODO : What if there were more than one?!?
							Statement statement2 = stmtIterator2.next();
							RDFNode oldAmountDouble = statement2.getObject();
							Double amountToConvert = oldAmountDouble.asLiteral().getDouble();
							// Now, the great question: "to divide or muliply?"
							// Divide because e.g. the Exchange amount would have to be 2.2 times as much if X
							// number of pounds were applied vs. X number of kg. Conversion factor is 0.45, so
							// divide.
							Double newAmountAfterConversion = amountToConvert / oldNewConvFactorMap.get(oldUUID);
							Literal newAmount = expModel.createTypedLiteral(newAmountAfterConversion);
							Statement addStatement2 = expModel.createStatement(subject.asResource(), OpenLCA.amount,
									newAmount);
							statementsToAdd.add(addStatement2);
							statementsToRemove.add(statement2);
						} else if (expModel.contains(subject, RDF.type, OpenLCA.ImpactFactor)) {
							isExchangeOrImpactFactor = true;
							Selector selector2 = new SimpleSelector(subject, OpenLCA.value, null, null);
							StmtIterator stmtIterator2 = expModel.listStatements(selector2);
							// TODO : What if there were more than one?!?
							Statement statement2 = stmtIterator2.next();
							RDFNode oldAmountDouble = statement2.getObject();
							Double amountToConvert = oldAmountDouble.asLiteral().getDouble();
							// Now, the great question: "to divide or muliply?"
							// Multiply because e.g. the ImpactFactor value would be .45 times as much if X number
							// of pounds were applied vs. X number of kg.
							Double newAmountAfterConversion = amountToConvert * oldNewConvFactorMap.get(oldUUID);
							Literal newAmount = expModel.createTypedLiteral(newAmountAfterConversion);
							Statement addStatement2 = expModel.createStatement(subject.asResource(), OpenLCA.amount,
									newAmount);
							statementsToAdd.add(addStatement2);
							statementsToRemove.add(statement2);
						} else {
							isExchangeOrImpactFactor = false;
							System.out.println("What other thing has the Flow as an object?!?");
						}
						if (isExchangeOrImpactFactor) {
							// Change the "unit" to the new unit
							Selector selector3 = new SimpleSelector(subject, OpenLCA.unit, null, null);
							StmtIterator stmtIterator3 = expModel.listStatements(selector3);
							Statement statement3 = stmtIterator3.next();
							statementsToRemove.add(statement3);
							Resource subject3 = statement3.getSubject();
							Resource newObject = expModel.createResource(OpenLCA.NS
									+ oldNewFlowUnitUUIDMap.get(oldUUID));
							Statement addStatement3 = expModel.createStatement(subject3, OpenLCA.unit, newObject);
							statementsToAdd.add(addStatement3);
						}
					}
				}
			}
		}
		ActiveTDB.tdbDataset.end();

		// It is necessary to do this once because otherwise, the Flow won't be present to have it's objects replaced
		tsRemoveStatementsFromGraph(statementsToRemove, statementsToAdd, ActiveTDB.exportGraphName);
		statementsToAdd.clear();
		statementsToRemove.clear();

		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		for (String oldUUID : oldNewOtherUUIDMap.keySet()) {
			totalDone++;
			int percent = 100 * totalDone / totalToDo;
			if (percent >= lastPercentComplete + 1) {
				final int percentToWrite = percent;
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						FlowsWorkflow.setTextConcludeStatus("2/3 updating components " + percentToWrite + "%");
					}
				});
				System.out.println("2/3 " + percent + "% complete");
				lastPercentComplete = percent;
			}
			String newUUID = oldNewOtherUUIDMap.get(oldUUID);
//			if (newUUID.equals("2d9498c8-6873-45e1-af33-e1a298c119b9")) {
//				System.out.println("pause here");
//			}
			Resource oldOtherResource = expModel.createResource(OpenLCA.NS + oldUUID);
			Resource newOtherResource = expModel.createResource(OpenLCA.NS + newUUID);

			otherThingsToBringIn.add(newOtherResource);
			// BEWARE: listStatements DOES NOT seem to capture statements with non-literal objects
			// AVOID: StmtIterator stmtIterator0 = expModel.listStatements(selector0);

			Selector selector1 = new SimpleSelector(null, null, oldOtherResource);
			StmtIterator stmtIterator1 = expModel.listStatements(selector1);
			while (stmtIterator1.hasNext()) {
				Statement statement = stmtIterator1.next();
				statementsToRemove.add(statement);
				RDFNode subject = statement.getSubject();
				RDFNode predicate = statement.getPredicate();
				RDFNode object = statement.getObject();
				if (!object.isLiteral() && !object.isAnon()) {
					String uri = object.asResource().getURI();
					String oldUUIDObject = uri.substring(uri.length() - 36);
					/*
					 * Logic here is tricky. We're looking for statements in which the object is the old Flow Resource,
					 * but the subject is NOT a Flow Resource. Maybe this is not possible, but the "if" helps confirm
					 * it. The subject should be a.... check this. If an Exchange or an ImpactFactor, the subject is
					 * Anon. These are not root objects (in the resourceMap)
					 */
					if (!oldNewFlowUUIDMap.containsKey(oldUUIDObject)) {
						Statement addStatement = expModel.createStatement(subject.asResource(), (Property) predicate,
								newOtherResource);
						statementsToAdd.add(addStatement);
					}
				}
			}
		}
		ActiveTDB.tdbDataset.end();

		// Now collect the item which is new from the default graph so as to replace the removed one.
		statementsToAdd.addAll(ActiveTDB.collectStatementsTraversingNodeSet(otherThingsToBringIn, null));

		// Now remove and add the new batches of statements (whose objects have changed)
		tsRemoveStatementsFromGraph(statementsToRemove, statementsToAdd, ActiveTDB.exportGraphName);
	}

	private void runAllUUIDReplacements(Map<String, String> oldNewFlowUUIDMap, Map<String, String> oldNewOtherUUIDMap,
			Set<RDFNode> stopAtTheseClasses) {
		// while (ActiveTDB.tdbDataset.isInTransaction()){
		// ActiveTDB.tdbDataset.end();
		// }
		int totalToDo = oldNewFlowUUIDMap.size() + oldNewOtherUUIDMap.size();
		int totalDone = 0;
		int lastPercentComplete = -1;
		Set<RDFNode> otherThingsToBringIn = new HashSet<RDFNode>();
		List<Statement> statementsToAdd = new ArrayList<Statement>();
		List<Statement> statementsToRemove = new ArrayList<Statement>();
		// List<RDFNode> objectsToAdd = new ArrayList<RDFNode>();
		// List<RDFNode> objectsToRemove = new ArrayList<RDFNode>();

		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model expModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		for (String oldUUID : oldNewFlowUUIDMap.keySet()) {
//			if (oldUUID.equals("37236b2f-b18d-35a7-9860-d9149c1763f1")) {
//				System.out.println("pause here");
//			}
//			if (oldUUID.equals("fc1c42ce-a759-49fa-b987-f1ec5e503db1")) {
//				System.out.println("pause here");
//			}

			totalDone++;
			int percent = 100 * totalDone / totalToDo;
			if (percent >= lastPercentComplete + 1) {
				final int percentToWrite = percent;
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						FlowsWorkflow.setTextConcludeStatus("2/3 updating components " + percentToWrite + "%");
					}
				});
				System.out.println("2/3 " + percent + "% complete");
				lastPercentComplete = percent;
			}
			String newUUID = oldNewFlowUUIDMap.get(oldUUID);
			Resource oldFlowResource = expModel.createResource(OpenLCA.NS + oldUUID);
			Resource newFlowResource = expModel.createResource(OpenLCA.NS + newUUID);
			// BEWARE: listStatements DOES NOT seem to capture statements with non-literal objects
			// StmtIterator stmtIterator0 = expModel.listStatements(oldFlowResource, null, null, null);
			Selector selector0 = new SimpleSelector(oldFlowResource, null, null, null);
			StmtIterator stmtIterator0 = expModel.listStatements(selector0);
			while (stmtIterator0.hasNext()) {
				Statement statement = stmtIterator0.next();
				statementsToRemove.add(statement);
				RDFNode object = statement.getObject();
				if (!object.isLiteral() && !object.isAnon()) {
					String uri = object.asResource().getURI();
					if (uri.length() == 36 + OpenLCA.NS.length()) {
						String oldUUIDObject = uri.substring(uri.length() - 36);
						if (oldNewOtherUUIDMap.containsKey(oldUUIDObject)) {
							String newUUIDObject = oldNewOtherUUIDMap.get(oldUUIDObject);
							object = expModel.createResource(OpenLCA.NS + newUUIDObject);
						}
					}
				}
				Statement addStatement = expModel.createStatement(newFlowResource, statement.getPredicate(), object);
				statementsToAdd.add(addStatement);
			}

			Selector selector1 = new SimpleSelector(null, null, oldFlowResource);
			StmtIterator stmtIterator1 = expModel.listStatements(selector1);
			while (stmtIterator1.hasNext()) {
				Statement statement = stmtIterator1.next();
				statementsToRemove.add(statement);
				Statement addStatement = expModel.createStatement(statement.getSubject(), statement.getPredicate(),
						newFlowResource);
				statementsToAdd.add(addStatement);
			}
		}
		ActiveTDB.tdbDataset.end();

		// It is necessary to do this once because otherwise, the Flow won't be present to have it's objects replaced
		tsRemoveStatementsFromGraph(statementsToRemove, statementsToAdd, ActiveTDB.exportGraphName);
		statementsToAdd.clear();
		statementsToRemove.clear();

		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		for (String oldUUID : oldNewOtherUUIDMap.keySet()) {
			totalDone++;
			int percent = 100 * totalDone / totalToDo;
			if (percent >= lastPercentComplete + 1) {
				final int percentToWrite = percent;
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						FlowsWorkflow.setTextConcludeStatus("2/3 updating components " + percentToWrite + "%");
					}
				});
				System.out.println("2/3 " + percent + "% complete");
				lastPercentComplete = percent;
			}
			String newUUID = oldNewOtherUUIDMap.get(oldUUID);
			if (newUUID.equals("2d9498c8-6873-45e1-af33-e1a298c119b9")) {
				System.out.println("pause here");
			}
			Resource oldOtherResource = expModel.createResource(OpenLCA.NS + oldUUID);
			Resource newOtherResource = expModel.createResource(OpenLCA.NS + newUUID);

			otherThingsToBringIn.add(newOtherResource);
			// BEWARE: listStatements DOES NOT seem to capture statements with non-literal objects
			// StmtIterator stmtIterator0 = expModel.listStatements(oldOtherResource, null, null, null);
			// Selector selector0 = new SimpleSelector(oldOtherResource, null, null, null);
			// StmtIterator stmtIterator0 = expModel.listStatements(selector0);
			// while (stmtIterator0.hasNext()) {
			// Statement statement = stmtIterator0.next();
			// statementsToRemove.add(statement);
			// }

			Selector selector1 = new SimpleSelector(null, null, oldOtherResource);
			StmtIterator stmtIterator1 = expModel.listStatements(selector1);
			while (stmtIterator1.hasNext()) {
				Statement statement = stmtIterator1.next();
				statementsToRemove.add(statement);
				RDFNode object = statement.getObject();
				if (!object.isLiteral() && !object.isAnon()) {
					String uri = object.asResource().getURI();
					String oldUUIDObject = uri.substring(uri.length() - 36);
					if (!oldNewFlowUUIDMap.containsKey(oldUUIDObject)) {

						Statement addStatement = expModel.createStatement(statement.getSubject(),
								statement.getPredicate(), newOtherResource);
						statementsToAdd.add(addStatement);
					}
				}
			}
		}
		ActiveTDB.tdbDataset.end();

		// Now collect the item which is new from the default graph so as to replace the removed one.
		statementsToAdd.addAll(ActiveTDB.collectStatementsTraversingNodeSet(otherThingsToBringIn, null));

		// Now remove and add the new batches of statements (whose objects have changed)
		tsRemoveStatementsFromGraph(statementsToRemove, statementsToAdd, ActiveTDB.exportGraphName);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				FlowsWorkflow.setTextConcludeStatus("");
			}
		});
	}

	private static void tsRemoveStatementsFromGraph(List<Statement> statementsToRemove,
			List<Statement> statementsToAdd, String graphName) {
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model expModel = ActiveTDB.getModel(graphName);
		try {
			expModel.remove(statementsToRemove);
			expModel.add(statementsToAdd);
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("Replace URI failed; see Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
	}

	private static void replaceUUIDtypeURI(String oldUUID, String newUUID, String graphName) {
		Resource oldResource = ActiveTDB.tsCreateResource(OpenLCA.NS + oldUUID);
		Set<RDFNode> newNode = new HashSet<RDFNode>();
		newNode.add(oldResource);
		List<Statement> statementsSubjectsToAdd = ActiveTDB.collectStatementsTraversingNodeSet(newNode, graphName);
		List<Statement> statementsSubjectsToRemove = ActiveTDB.collectStatementsTraversingNodeSet(newNode, graphName);
		List<Statement> statementsObjects = new ArrayList<Statement>();

		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.getModel(graphName);
		Selector selector = new SimpleSelector(null, null, oldResource);
		StmtIterator stmtIterator = tdbModel.listStatements(selector);
		statementsObjects.addAll(stmtIterator.toList());
		ActiveTDB.tdbDataset.end();

		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		try {
			Resource newResource = tdbModel.createResource(OpenLCA.NS + newUUID);
			for (Statement statement : statementsSubjectsToRemove) {
				if (statement.getSubject().equals(oldResource)) {
					tdbModel.add(newResource, statement.getPredicate(), statement.getObject());
					tdbModel.remove(statement);
				}
			}
			for (Statement statement : statementsObjects) {
				if (statement.getObject().equals(oldResource)) {
					tdbModel.add(statement.getSubject(), statement.getPredicate(), newResource);
					tdbModel.remove(statement);
				}
			}
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("Replace URI failed; see Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}

	}

	private static void updateDescription(Resource itemResource, String newDescription) {
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		try {
			tdbModel.removeAll(itemResource, OpenLCA.description, null);
			tdbModel.add(itemResource, OpenLCA.description, newDescription);
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("Update description failed; see Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
	}

	private static void updateLastChange(Resource itemResource) {
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		try {
			Date lastChange = new Date();
			tdbModel.removeAll(itemResource, DCTerms.modified, null);
			tdbModel.removeAll(itemResource, OpenLCA.lastChange, null);
			tdbModel.add(itemResource, OpenLCA.lastChange, Temporal.getLiteralFromDate1(lastChange));
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("Update description failed; see Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
	}

	private static List<Resource> getOLCAMatchingMasterResources(String userUUID) {
		List<Resource> matchingResources = new ArrayList<Resource>();
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.getModel(null);
		Literal userUUIDLiteral = tdbModel.createTypedLiteral(userUUID);
		Selector selector0 = new SimpleSelector(null, FedLCA.hasOpenLCAUUID, userUUIDLiteral);
		StmtIterator stmtIterator0 = tdbModel.listStatements(selector0);
		while (stmtIterator0.hasNext()) {
			Statement statement0 = stmtIterator0.next();
			Resource flowResource = statement0.getSubject();
			if (flowResource.hasProperty(RDF.type, FedLCA.Flow)) {
				Selector selector1 = new SimpleSelector(null, FedLCA.comparedSource, flowResource);
				StmtIterator stmtIterator1 = tdbModel.listStatements(selector1);
				while (stmtIterator1.hasNext()) {
					Statement statement1 = stmtIterator1.next();
					Resource comparisonResource = statement1.getSubject();
					Resource masterResource = comparisonResource.getProperty(FedLCA.comparedMaster).getObject()
							.asResource();
					matchingResources.add(masterResource);
				}
			}
		}
		ActiveTDB.tdbDataset.end();
		return matchingResources;
	}

	/**
	 * This method is designed to take the Resource of an openLCA Flow, together with the list of attributes
	 * of the original and of the harmonized master Flow.  It then writes new triples for any Literals that
	 * change and makes a note of the changes in the returned String. 
	 * @param itemResource is the Resource of the openLCA Flow for which data are to be replaced
	 * @param oldProperties is a Map of name (String) / value (RDFNode) pairs for the original
	 * @param newProperties a Map of name (String) / value (RDFNode) pairs for the master Flow
	 * @return a String containing the changes concatenated with semi-colon space.
	 */
	private static String replaceUserLiterals(Resource itemResource, Map<String, RDFNode> oldProperties,
			Map<String, RDFNode> newProperties) {
		String changes = "";
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		Model tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		try {
			for (String key : newProperties.keySet()) {
				RDFNode newValueNode = newProperties.get(key);
				RDFNode oldValueNode = oldProperties.get(key);
				if (newValueNode == null || !newValueNode.isLiteral()) {
					continue;
				}
				if (key.equals("name")) {
					String newValue = newValueNode.asLiteral().getString();
					if (oldValueNode == null) {
						changes += "original name: (not defined); ";
						tdbModel.add(itemResource, OpenLCA.name, newProperties.get(key));
					} else {
						String oldValue = oldValueNode.asLiteral().getString();
						if (!newValue.equals(oldValue)) {
							changes += "original name: '" + oldValue + "'; ";
							tdbModel.removeAll(itemResource, OpenLCA.name, null);
							tdbModel.add(itemResource, OpenLCA.name, newProperties.get(key));
						}
					}
				} else if (key.equals("cas")) {
					String newValue = newValueNode.asLiteral().getString();
					if (oldValueNode == null) {
						changes += "original cas: (not defined); ";
						tdbModel.add(itemResource, OpenLCA.cas, newProperties.get(key));
					} else {
						String oldValue = oldValueNode.asLiteral().getString();
						if (!newValue.equals(oldValue)) {
							changes += "original cas: '" + oldValue + "'; ";
							tdbModel.removeAll(itemResource, OpenLCA.cas, null);
							tdbModel.add(itemResource, OpenLCA.cas, newProperties.get(key));
						}
					}
				} else if (key.equals("formula")) {
					String newValue = newValueNode.asLiteral().getString();
					if (oldValueNode == null) {
						changes += "original formula: (not defined); ";
						tdbModel.add(itemResource, OpenLCA.formula, newProperties.get(key));
					} else {
						String oldValue = oldValueNode.asLiteral().getString();
						if (!newValue.equals(oldValue)) {
							changes += "original formula: '" + oldValue + "'; ";
							tdbModel.removeAll(itemResource, OpenLCA.formula, null);
							tdbModel.add(itemResource, OpenLCA.formula, newProperties.get(key));
						}
					}
				}
			}
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			System.out.println("01 TDB transaction failed; see Exception: " + e);
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		return changes;
	}

	/**
	 * This method is designed to return a map of name, value pairs for specific literal values of a Flow
	 * @param flowResource
	 * @return
	 */
	private static Map<String, RDFNode> getFlowFeatureLiterals(Resource flowResource) {
		Map<String, RDFNode> returnMap = new HashMap<String, RDFNode>();
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		if (flowResource.hasProperty(RDF.type, OpenLCA.Flow)) {
			Map<String, Property> olcaMap = OpenLCA.getOpenLCAMap();
			for (String key : olcaMap.keySet()) {
				Statement statement = flowResource.getProperty(olcaMap.get(key));
				if (statement != null) {
					returnMap.put(key, statement.getObject());
				}
			}
		} else if (flowResource.hasProperty(RDF.type, FedLCA.Flow)) {

			Statement statement = flowResource.getProperty(FedLCA.hasOpenLCAUUID);
			if (statement != null) {
				returnMap.put("uuid", statement.getObject());
			}

			Resource flowable = flowResource.getPropertyResourceValue(ECO.hasFlowable);
			statement = flowable.getProperty(RDFS.label);
			if (statement != null) {
				returnMap.put("name", statement.getObject());
			}

			statement = flowable.getProperty(ECO.casNumber);
			if (statement != null) {
				returnMap.put("cas", statement.getObject());
			}
			statement = flowable.getProperty(ECO.chemicalFormula);
			if (statement != null) {
				returnMap.put("formula", statement.getObject());
			}
			statement = flowable.getProperty(RDFS.comment);
			if (statement != null) {
				returnMap.put("description", statement.getObject());
			}

			Resource dataSetResource = flowResource.getPropertyResourceValue(ECO.hasDataSource);
			Resource dataSetFileResource = dataSetResource.getPropertyResourceValue(LCAHT.containsFile);

			statement = dataSetFileResource.getProperty(DCTerms.hasVersion);
			if (statement != null) {
				returnMap.put("version", statement.getObject());
			}
			statement = dataSetFileResource.getProperty(DCTerms.modified);
			if (statement != null) {
				returnMap.put("lastChange", statement.getObject());
			}
			// Location not implemented yet
			returnMap.put("location", null);
			statement = flowResource.getProperty(FedLCA.hasFlowProperty);
			if (statement != null) {
				returnMap.put("flow_properties", statement.getObject());
			}
			statement = flowResource.getProperty(FedLCA.hasFlowContext);
			if (statement != null) {
				returnMap.put("category", statement.getObject());
			}
		}
		ActiveTDB.tdbDataset.end();
		return returnMap;
	}

	// OpenLCA does not handle the olca: prefix correctly. Resolve by removing and letting the @context declaration
	// handle
	private void removePrefix(String path) {
		String tempPath = path + ".lcaht.tmp";
		File oldFile = new File(path);
		File newFile = new File(tempPath);
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(oldFile));
			String line = bufferedReader.readLine();
			PrintWriter printWriter = new PrintWriter(new PrintWriter(newFile));
			while (line != null) {
				line = line.replaceAll("olca:", "");
				printWriter.println(line);
				line = bufferedReader.readLine();
			}
			printWriter.close();
			bufferedReader.close();
			oldFile.delete();
			newFile.renameTo(oldFile);

		} catch (IOException e) {
			e.printStackTrace();
		}
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
