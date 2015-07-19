package gov.epa.nrmrl.std.lca.ht.output;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dialog.ChooseDataSetDialog;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.MatchContexts;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.MatchProperties;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Temporal;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;
import gov.epa.nrmrl.std.lca.ht.vocabulary.OpenLCA;

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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
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

	/*
	 * TODO FIXME TODO FIXME TODO ALERT! DANGER! SEE HERE! ATTENTION TOM TRANSUE : Tom, just change writeResource to
	 * generate a correct uuid and convert the resource to JSON instead of calling toString and remove the main method
	 * (I added it just as a sample for how to use the zip api), and I think everything else will just work.
	 */

	private void writeResource(String folder, Resource resource, ZipOutputStream output) throws IOException {
		String uuid = Integer.toString(resource.hashCode());
		output.putNextEntry(new ZipEntry(folder + "/" + uuid + ".json"));
		output.write(resource.toString().getBytes());
		output.closeEntry();
	}

	private void writeResource(String folder, String filename, String fileContents, ZipOutputStream output)
			throws IOException {
		output.putNextEntry(new ZipEntry(folder + "/" + filename));
		output.write(fileContents.getBytes());
		output.closeEntry();
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		Shell shell = HandlerUtil.getActiveShell(event);
		ChooseDataSetDialog dlg = new ChooseDataSetDialog(shell);
		dlg.open();
		String currentName = dlg.getSelection();

		Util.findView(MatchContexts.ID);
		Util.findView(MatchProperties.ID);

		Logger runLogger = Logger.getLogger("run");

		System.out.println("Saving Harmonized Data to .jsonld file");

		String saveTo = event.getParameter("LCA-HT.outputFilename");

		if (saveTo == null) {
			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			FileDialog dialog = new FileDialog(shell, SWT.SAVE);
			String[] filterNames = new String[] { "Json Files", "Jsonld Files", "Turtle Files", "Zip Files" };
			String[] filterExtensions = new String[] { "*.json", "*.jsonld", "*.ttl", "*.zip" };

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
		// ActiveTDB.copyDatasetContentsToExportGraph(currentName);
		// List<Statement> statements = ActiveTDB.collectAllStatementsForDataset(currentName, null);
		Set<Resource> datasetMembers = ActiveTDB.getDatasetMemberSubjects(currentName, null);

		if (saveTo.endsWith(".zip")) {
			/*
			 * The order of the items below is critical since detection of changes in some objects must be propagated to
			 * objects that contain them. During preparation of each .json file, Comparisons will be consulted to see
			 * what changes should be made
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

			Set<RDFNode> stopAtTheseClasses = new HashSet<RDFNode>();
			stopAtTheseClasses.add(OpenLCA.Actor);
			stopAtTheseClasses.add(OpenLCA.Category);
			stopAtTheseClasses.add(OpenLCA.FlowProperty);
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

			try {
				ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(saveTo));
				int total = 0;
				for (String folderKey : resourceMap.keySet()) {
					System.out.println("Working on '" + folderKey + "' files");
					Set<Resource> hashSet = resourceMap.get(folderKey);
					int lastPercent = -1;
					for (Resource itemResource : hashSet) {
						total++;
						int percent = 100 * total / memberCount;

						if ((percent % 5 == 0) && (percent > lastPercent + 1)) {
							System.out.println(percent + " % complete");
							lastPercent = percent;
						}

						/*
						 * Confirm that this item should be processed
						 */
						String itemUUID = null;
						if (!itemResource.isAnon()) {
							String uriName = itemResource.getURI();
							if (uriName.matches(OpenLCA.NS + "[a-f0-9-]{36}")) {
								//
								// NOTE: DO NOT USE .getLocalName() AS IT TRUNCATES LEADING ZEROS!! See line below:
								// String fileName = itemResource.getLocalName() + ".json";
								// Take a substring starting at the end of the NameSpace instead
								//
								itemUUID = uriName.substring(OpenLCA.NS.length());
							} else {
								for (Statement statement : itemResource.listProperties(RDF.type).toList()) {
									if (!subClassesNotToPackageSeparately.contains(statement.getObject())) {
										System.out.println("Found in '" + folderKey + "' a thing with class: "
												+ statement.getObject());
									}
								}
							}
							if (itemUUID == null) {
								continue;
							}

							Set<RDFNode> singleSet = new HashSet<RDFNode>();
							singleSet.add(itemResource);

							List<Statement> statements = ActiveTDB.collectStatementsTraversingNodeSetWithStops(
									singleSet, stopAtTheseClasses, null);
							ActiveTDB.clearExportGraphContents();
							ActiveTDB.copyStatementsToGraph(statements, ActiveTDB.exportGraphName);

							/*
							 * Here begins the test to manage exactly what changes are made to what types of objects
							 * Some require nothing 1) flows require changing info to the harmonized flow, but creating
							 * "description" and "lastChange" 2) processes require changing the "flow" info and info
							 * about Exchanges
							 */
							if (folderKey.equals("flows")) {
								if (ActiveTDB.getModel(ActiveTDB.exportGraphName).contains(itemResource,
										OpenLCA.flowType, OpenLCA.ELEMENTARY_FLOW)) {

									List<Resource> matchingMasters = getOLCAMatchingMasterResources(itemUUID);
									if (matchingMasters.size() > 1) {
										System.out
												.println("Got multiple matches.  Count is :" + matchingMasters.size());
									} else if (matchingMasters.size() == 1) {
										Map<String, RDFNode> itemProperties = getFlowFeatureLiterals(itemResource);
										Map<String, RDFNode> masterProperties = getFlowFeatureLiterals(matchingMasters
												.get(0));

										String changes = replaceUserLiterals(itemResource, itemProperties,
												masterProperties);
										if (!changes.equals("")) {
											String description = itemProperties.get("description") + " -> " + changes;
											itemResource.removeAll(OpenLCA.description);
											itemResource.addProperty(OpenLCA.description, description);

											Date lastChange = new Date();
											itemResource.removeAll(DCTerms.modified);
											itemResource.addLiteral(DCTerms.modified,
													Temporal.getLiteralFromDate1(lastChange));

										}
										for (String itemKey : itemProperties.keySet()) {
											RDFNode itemValueNode = itemProperties.get(itemKey);
											RDFNode masterValueNode = masterProperties.get(itemKey);
											if (itemValueNode != null && masterValueNode != null) {
												if (!itemValueNode.isLiteral() && !masterValueNode.isLiteral()) {
													// Handle the non-literal parts
												}
											}
										}
									}
								}
							}

							/*
							 * Now copy contents of the graph to a string in .json format
							 */
							StringWriter stringOut = new StringWriter();
							ActiveTDB.tdbDataset.begin(ReadWrite.READ);
							Model tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
							tdbModel.write(stringOut, "JSON-LD");

							/*
							 * Now some post-processing string manipulation to make openLCA happy
							 */
							ActiveTDB.tdbDataset.end();
							String fileContents = stringOut.toString();
							String cleanedfileContents1 = fileContents.replaceAll("\"olca:", "\"");
							String cleanedfileContents2 = cleanedfileContents1.replaceAll(
									" \"@id\" : \"urn:x-arq:DefaultGraphNode\",", "");

							/*
							 * Now append to the .zip file with the individual .json file
							 */
							String fileName = itemUUID + ".json";
							writeResource(folderKey, fileName, cleanedfileContents2, zipFile);
						}
					}
				}
				zipFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		/*
		 * =================================================================================
		 * ============================= OLD CODE BELOW =====================================
		 * =================================================================================
		 */
		// /**
		// * Once data are copied into the export graph, data can be prepared for openLCA
		// * 1) Select Flows using Comparisons
		// * 2) Create new UUIDs for those
		// * 3) Move old info to an appropriate field name
		// * 4) Place new info in the appropriate place
		// * 5) Append to description info about what happened
		// */
		//
		// // if (false){
		// Date modifiedDate = AnnotationProvider.getCurrentAnnotation().getModifiedDate();
		// String modString = Temporal.getLocalDateFmt(modifiedDate);
		// /* TODO - WORK HERE to collect the ComparisonProviders associated with the dataset, then go through each
		// * and make appropriate changes to the export dataset
		// */
		//
		// // List<Statement> statementsToFix = new ArrayList<Statement>();
		// ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		// Model tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		// Selector selector0 = new SimpleSelector(null, FedLCA.comparedSource, null, null);
		// StmtIterator stmtIterator0 = tdbModel.listStatements(selector0);
		// while (stmtIterator0.hasNext()) {
		// Statement statement = stmtIterator0.next();
		// statementsToFix.add(statement);
		// }
		// ActiveTDB.tdbDataset.end();
		//
		// ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		// tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		// for (Statement statement : statementsToFix) {
		// String unTypedDateTime = statement.getObject().toString();
		// Literal typedDateTime = tdbModel.createTypedLiteral(unTypedDateTime, XSDDatatype.XSDdateTime);
		// tdbModel.add(statement.getSubject(), OpenLCA.lastChange, typedDateTime);
		// tdbModel.remove(statement);
		// // System.out.println("statement = "+statement);
		// }
		// // nothing
		// // RDFNode modNode = CurationMethods.getCurrentAnnotation().getProperty(DCTerms.modified).getObject();
		// // Literal modLiteral = modNode.asLiteral();
		// // Calendar cal = ((XSDDateTime) literalDate).;
		// // Object thing = literalDate.getValue();
		// // System.out.println("it is a :"+modString.getClass());
		// // Date annotatationDate = new Date(CurationMethods.getCurrentAnnotation().getProperty(DCTerms.modified)
		// // .getObject().asLiteral().getLong());
		// // String dateString = Util.getLocalDateFmt(annotatationDate);
		// try {
		//
		// StringBuilder b = new StringBuilder();
		// b.append(Prefixes.getPrefixesForQuery());
		// b.append("  delete {graph <" + ActiveTDB.exportGraphName + ">{  \n");
		// b.append("    ?of olca:description ?oDescription ; \n");
		// b.append("        olca:lastChange ?oLastChange ; \n");
		// b.append("        olca:cas ?oCas ; \n");
		// b.append("        olca:name ?oName ; \n");
		// b.append("        fedlca:hasOpenLCAUUID ?oUUID . \n");
		// b.append("  }} \n");
		// b.append("   \n");
		// b.append("  insert {graph <" + ActiveTDB.exportGraphName + ">{  \n");
		// b.append("    ?of olca:description ?newDescription ; \n");
		// b.append("        olca:lastChange ?newLastChange ; \n");
		// b.append("        olca:cas ?newCas ; \n");
		// b.append("        olca:name ?mName ; \n");
		// b.append("        fedlca:hasOpenLCAUUID ?newUUID . \n");
		// b.append("  }} \n");
		// b.append("   \n");
		// b.append("  where { \n");
		// b.append("    #-- olca:Flow \n");
		// b.append("    ?of a olca:Flow . \n");
		// b.append("    ?of fedlca:hasOpenLCAUUID ?oUUID . \n");
		// b.append("    ?pf a fedlca:Flow . \n");
		// b.append("   \n");
		// b.append("    #-- fedlca:Flow (parsed) \n");
		// b.append("    ?pf fedlca:hasOpenLCAUUID ?oUUID . \n");
		// b.append("    ?pf fedlca:sourceTableRowNumber ?row . \n");
		// b.append("   \n");
		// b.append("    ?pf owl:sameAs ?mf . \n");
		// // b.append("    #-- fedlca:Comparison (created) \n");
		// // b.append("    ?comp fedlca:comparedSource ?pf . \n");
		// // b.append("    ?comp fedlca:comparedMaster ?mf . \n");
		// // b.append("    ?comp fedlca:comparedEquivalence fedlca:Equivalent . \n");
		// // // TODO: Handle cases other than "Equivalent"
		// b.append("   \n");
		// b.append("    #-- fedlca:Flow (master) \n");
		// b.append("    ?mf a fedlca:Flow . \n");
		// b.append("    ?mf eco:hasFlowable ?mflowable . \n");
		// b.append("    ?mds a lcaht:MasterDataset . \n");
		// b.append("    ?mf eco:hasDataSource ?mds . \n");
		// b.append("   \n");
		// b.append("    #-- olca:name == rdfs:label -- 1 CONDITION NEEDING ACTION \n");
		// b.append("    ?of olca:name ?oName . \n");
		// b.append("    ?mflowable rdfs:label ?mName . \n");
		// b.append("    bind (IF ((str(?oName) != str(?mName) ) , concat(\"; name: original = \", ?oName) , \"\") as ?cName) \n");
		// b.append("   \n");
		// b.append("    #-- olca:cas == fedlca:hasFormattedCAS -- 3 CONDITIONS NEEDING ACTION \n");
		// b.append("    optional { ?of olca:cas ?oCas . } \n");
		// b.append("    optional { ?mflowable fedlca:hasFormattedCAS ?mCas . } \n");
		// b.append("    bind (IF (( bound(?oCas) &&  bound(?mCas) && str(?oCas) != str(?mCas)) , concat(\"; cas: original = \",?oCas),\"\") as ?c1Cas) \n");
		// b.append("    bind (IF ((!bound(?oCas) &&  bound(?mCas)) , \"; cas: original not defined\",\"\") as ?c2Cas) \n");
		// b.append("    bind (IF (( bound(?oCas) && !bound(?mCas)) , \"; cas: master not defined\",\"\") as ?c3Cas) \n");
		// b.append("    bind (concat(?c1Cas,?c2Cas,?c3Cas) as ?cCas) \n");
		// b.append("    bind (IF ((?c1Cas != \"\" || ?c2Cas != \"\"),?mCas, ?oCas) as ?newCas) \n");
		// b.append("    #-- ABOVE, THE USE OF ?oCas IS TO ENSURE THAT IT GETS PUT BACK SINCE IT WILL BE DELETED \n");
		// b.append("   \n");
		// b.append("    #-- olca:formula == eco:chemicalFormula -- 3 CONDITIONS NEEDING ACTION \n");
		// b.append("    optional { ?of olca:formula ?oFormula . } \n");
		// b.append("    optional { ?mflowable eco:chemicalFormula ?mFormula . } \n");
		// b.append("    bind (IF (( bound(?oFormula) &&  bound(?mFormula) && str(?oFormula) != str(?mFormula)) , concat(\"; formula: original = \",?oFormula),\"\") as ?c1Formula) \n");
		// b.append("    bind (IF ((!bound(?oFormula) &&  bound(?mFormula)) , \"; formula: original not defined\",\"\") as ?c2Formula) \n");
		// b.append("    bind (IF (( bound(?oFormula) && !bound(?mFormula)) , \"; formula: master not defined\",\"\") as ?c3Formula) \n");
		// b.append("    bind (concat(?c1Formula,?c2Formula,?c3Formula) as ?cFormula) \n");
		// b.append("   \n");
		// b.append("    #-- fedlca:hasOpenLCAUUID (for both) -- 4 CONDITIONS NEEDING ACTION \n");
		// b.append("    optional { ?mf fedlca:hasOpenLCAUUID ?mUUID . } \n");
		// b.append("    bind (IF (( bound(?oUUID) &&  bound(?mUUID) && str(?oUUID) != str(?mUUID)) , concat(\"; UUID: original = \",?oUUID),\"\") as ?c1UUID) \n");
		// b.append("    bind (IF ((!bound(?oUUID) &&  bound(?mUUID)) , \"; UUID: original not defined\",\"\") as ?c2UUID) \n");
		// b.append("    #-- ABOVE NOT NEEDED UNLESS WE MAKE THE oUUID OPTIONAL \n");
		// b.append("    bind (IF (( bound(?oUUID) && !bound(?mUUID)) , \"; UUID: master not defined\",\"\") as ?c3UUID) \n");
		// b.append("    bind (IF ((!bound(?oUUID) && !bound(?mUUID)) , \"; UUID: new value created\",\"\") as ?c4UUID) \n");
		// b.append("    bind (concat(?c1UUID,?c2UUID,?c3UUID,?c4UUID) as ?cUUID) \n");
		// b.append("    bind (IF (( bound(?oUUID) &&  bound(?mUUID)) , ?mUUID,\"\") as ?new1UUID) \n");
		// b.append("    bind (IF (( bound(?oUUID) && !bound(?mUUID)) , ?oUUID,\"\") as ?new2UUID) \n");
		// b.append("    bind (concat(?new1UUID,?new2UUID) as ?newUUID) \n");
		// b.append("   \n");
		// b.append("    #-- olca:lastChange -- 1 CONDITION NEEDING ACTION \n");
		// b.append("    optional {?of olca:lastChange ?oLastChange } \n");
		// b.append("    bind (IF (bound(?oLastChange) , concat(\"; previous lastChange: \",str(?oLastChange)),\"\") as ?cLastChange)  \n");
		// b.append("    bind (\"" + modString + "\"^^xsd:dateTime as ?newLastChange) \n");
		// // b.append("    bind (\"" + modString + "\" as ?newLastChange) \n");
		// b.append("    #--    ^^^^^^^^^^^^^^^^^^^^^^^^^ PLACE ACTUAL VALUE FROM AnnotationProvider ABOVE \n");
		// b.append("   \n");
		// b.append("    #-- olca:description -- 1 CONDITION PLUS CONCATINATION NEEDED \n");
		// b.append("    optional {?of olca:description ?oDescription } \n");
		// b.append("    bind (IF (!bound(?oDescription) , concat(\"Description created:\",str(now())),?oDescription) as ?cDescription)  \n");
		// b.append("    bind (concat(?cDescription , ?cUUID, ?cName, ?cCas, ?cFormula , ?cLastChange) as ?newDescription) \n");
		// b.append("  } \n");
		// b.append("   \n");
		// String query = b.toString();
		// System.out.println("Big query = \n" + query + "\n");
		// UpdateRequest request = UpdateFactory.create(query);
		// UpdateProcessor proc = UpdateExecutionFactory.create(request, ActiveTDB.graphStore);
		// proc.execute();
		// ActiveTDB.tdbDataset.commit();
		// } catch (Exception e) {
		// System.out.println("01 TDB transaction failed; see Exception: " + e);
		// ActiveTDB.tdbDataset.abort();
		// } finally {
		// ActiveTDB.tdbDataset.end();
		// }
		// // ---- END SAFE -WRITE- TRANSACTION ---
		//
		// String olcaNS = OpenLCA.NS;
		// if (!olcaNS.equals(Prefixes.getNSForPrefix("olca"))) {
		// System.out.println("Aaack!  OpenLCA namespace has changed!");
		// // TODO: Determine a good place to keep track of this since openLCA namespace may change in or out of LCA HT
		// }
		// // int olcaNSLength = olcaNS.length() + 1;
		//
		// List<Resource> originalIRI = new ArrayList<Resource>();
		// List<String> newUUID = new ArrayList<String>();
		//
		// // COLLECT THE OLD IRIs AND THE NEW UUID TO SET UP THE REPLACEMENT
		// // ---- BEGIN SAFE -READ- TRANSACTION ---
		// ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		// tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		// try {
		// StringBuilder b = new StringBuilder();
		// b.append(Prefixes.getPrefixesForQuery());
		// b.append("select distinct ?of ?uuid \n");
		// b.append("  where { \n");
		// b.append("    ?mf a fedlca:Flow . \n");
		// b.append("    ?mf fedlca:hasOpenLCAUUID ?muuid . \n");
		// b.append("    ?mf eco:hasDataSource ?mds . \n");
		// b.append("    ?mds a lcaht:MasterDataset . \n");
		// b.append("    bind (str(?muuid) as ?uuid) \n");
		// b.append("    ?of fedlca:hasOpenLCAUUID ?uuid . \n");
		// b.append("    ?of a olca:Flow . \n");
		// b.append("} \n");
		// String query = b.toString();
		// HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		// harmonyQuery2Impl.setQuery(query);
		// System.out.println("query = " + query);
		// harmonyQuery2Impl.setGraphName(ActiveTDB.exportGraphName);
		//
		// ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		// while (resultSet.hasNext()) {
		// QuerySolution querySolution = resultSet.next();
		// originalIRI.add(querySolution.get("of").asResource());
		// newUUID.add(querySolution.get("uuid").asLiteral().getString());
		// }
		// } catch (Exception e) {
		// System.out.println("Read from TDB failed; see Exception: " + e);
		// ActiveTDB.tdbDataset.abort();
		// } finally {
		// ActiveTDB.tdbDataset.end();
		// }
		// // // ---- END SAFE -READ- TRANSACTION ---
		// //
		// // // ---- BEGIN SAFE -WRITE- TRANSACTION ---
		// ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		// tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		// try {
		// List<Statement> statementsToAdd = new ArrayList<Statement>();
		// List<Statement> statementsToRemove = new ArrayList<Statement>();
		// for (int i = 0; i < originalIRI.size(); i++) {
		// Resource newIRI = tdbModel.createResource(OpenLCA.NS + newUUID.get(i));
		// Resource oldIRI = originalIRI.get(i);
		// StmtIterator stmtIterator = oldIRI.listProperties();
		// while (stmtIterator.hasNext()) {
		// Statement statement = stmtIterator.next();
		// statementsToAdd.add(tdbModel.createStatement(newIRI, statement.getPredicate(),
		// statement.getObject()));
		// statementsToRemove.add(statement);
		// }
		// Selector selector = new SimpleSelector(null, null, oldIRI);
		// StmtIterator stmtIterator2 = tdbModel.listStatements(selector);
		// while (stmtIterator2.hasNext()) {
		// Statement statement = stmtIterator2.next();
		// statementsToAdd.add(tdbModel.createStatement(statement.getSubject(), statement.getPredicate(),
		// newIRI));
		// statementsToRemove.add(statement);
		// }
		// }
		// for (Statement statement : statementsToAdd) {
		// tdbModel.add(statement);
		// }
		// for (Statement statement : statementsToRemove) {
		// tdbModel.remove(statement);
		// }
		// ActiveTDB.tdbDataset.commit();
		// } catch (Exception e) {
		// System.out.println("replace URI failed; see Exception: " + e);
		// ActiveTDB.tdbDataset.abort();
		// } finally {
		// ActiveTDB.tdbDataset.end();
		// }
		// // ---- END SAFE -WRITE- TRANSACTION ---
		//
		// // ---- BEGIN SAFE -WRITE- TRANSACTION ---
		// ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		// tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		// try {
		// // NOW NEED TO REMOVE ALL EXTRANEOUS STUFF
		// StringBuilder b = new StringBuilder();
		// b.append(Prefixes.getPrefixesForQuery());
		// b.append("  delete {graph <" + ActiveTDB.exportGraphName + ">{  \n");
		// b.append("    ?s ?p ?o . \n");
		// b.append("  }} \n");
		// b.append("   \n");
		// b.append("  where { \n");
		// b.append("    ?ds a eco:DataSource . \n");
		// b.append("    ?ds rdfs:label ?dsName . \n");
		// b.append("    filter(str(?dsName) = \"" + currentName + "\" ) . \n");
		// b.append("    ?s ?p ?o . \n");
		// b.append("    ?s eco:hasDataSource ?ads . \n");
		// b.append("    filter (?ds != ?ads) \n");
		// b.append("    ?s ?p ?o . \n");
		// b.append("} \n");
		// String query = b.toString();
		// System.out.println("Replace UUIDs query = \n" + query + "\n");
		// UpdateRequest request = UpdateFactory.create(query);
		// UpdateProcessor proc = UpdateExecutionFactory.create(request, ActiveTDB.graphStore);
		// proc.execute();
		// ActiveTDB.tdbDataset.commit();
		// } catch (Exception e) {
		// System.out.println("Remove non-dataset stuff failed; see Exception: " + e);
		// ActiveTDB.tdbDataset.abort();
		// } finally {
		// ActiveTDB.tdbDataset.end();
		// }
		// // ---- END SAFE -WRITE- TRANSACTION ---

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
		return null;
	}

	private static List<Resource> getOLCAMatchingMasterResources(String userUUID) {
		List<Resource> matchingResources = new ArrayList<Resource>();
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.getModel(null);
		Selector selector0 = new SimpleSelector(null, FedLCA.hasOpenLCAUUID, userUUID);
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
			Resource flowable = flowResource.getPropertyResourceValue(ECO.hasFlowable);
			Statement statement = flowable.getProperty(RDFS.label);
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
				returnMap.put("flowProperties", statement.getObject());
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

	/*
	 * This creates a zip file in /tmp/data.zip. Run main, and look at /tmp/data.zip. The structure matches the
	 * fileNames and fileContents arrays.
	 */

	// public static void main(String[] args) {
	// String outputPath = "C:\\Users\\Tom\\data.zip";
	//
	// String[] fileNames = new String[] { "uuids/first.json", "uuids/second.json", "flows/elements.json",
	// "readme.txt" };
	//
	// String[] fileContentsToZip = new String[] { "this is the first file - not much in it",
	// "this is where all the good stuff happens", "these are not the flows you're looking for",
	// "nothing to see here, move along" };
	//
	// try {
	// ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(outputPath));
	// for (int i = 0; i < fileNames.length; ++i) {
	// zipFile.putNextEntry(new ZipEntry(fileNames[i]));
	// // Treat this just like a FileOutputStream
	// zipFile.write(fileContentsToZip[i].getBytes());
	// zipFile.closeEntry();
	// }
	// zipFile.close();
	//
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

}
