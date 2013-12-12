package harmonizationtool;

import harmonizationtool.comands.SelectTDB;
import harmonizationtool.dialog.MyDialog;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.ModelKeeper;
import harmonizationtool.model.ModelProvider;
import harmonizationtool.query.GenericQuery;
import harmonizationtool.query.GenericUpdate;
import harmonizationtool.query.IdsInfoQuery;
import harmonizationtool.query.IdsRowQuery;
import harmonizationtool.query.QueryResults;
import harmonizationtool.utils.Util;
import harmonizationtool.query.ZGetNextDSIndex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVStrategy;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
//import com.hp.hpl.jena.vocabulary.NFO; // DOES NOT EXIST!!
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class View extends ViewPart {
	public static final String ID = "HarmonizationTool.view";

	private TableViewer viewer;
	private Action actionImport;
	private Action actionSave;
	private Action actionClose;
	private Action actionExportToTDB;
	private Action actionExportSubsToTDB;

	private ZGetNextDSIndex qGetNextDSIndex = new ZGetNextDSIndex();

	/**
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */
	class ViewContentProvider implements IStructuredContentProvider {
		Viewer v;

		public ViewContentProvider(Viewer v) {
			this.v = v;
		}

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (parent instanceof Object[]) {
				return (Object[]) parent;
			}
			return new Object[0];
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider(viewer));
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());

		makeActions();
		hookContextMenu();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				View.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);

	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(actionImport);
		// manager.add(actionExportToTDB); // NEEDS REPAIR TODO
		manager.add(actionExportSubsToTDB);
		manager.add(actionSave);
		manager.add(actionClose);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private long filesizeLong = 0;
	private int filesizeInt = 0;
	private String filenameStr = ""; // FIXME: SHOULD USE THIS IN THE DATA SET

	private static void printValues(int lineNumber, String[] as) {
		System.out.println("Line " + lineNumber + " has " + as.length
				+ " values:");
		for (String s : as) {
			System.out.println("\t|" + s + "|");
		}
		System.out.println();
	}

	private DataRow initDataRow(String[] values) {
		DataRow dataRow = new DataRow();
		for (String s : values) {
			dataRow.add(s);
		}
		return dataRow;
	}

	private void makeActions() {
		actionImport = new Action() {
			public void run() {
				System.out.println("executing actionImport");
				ModelProvider modelProvider = new ModelProvider();
				FileDialog fileDialog = new FileDialog(
						getViewSite().getShell(), SWT.OPEN);
				fileDialog.setFilterExtensions(new String[] { "*.csv" });
				String workingDir = Util.getPreferenceStore().getString(
						"workingDir");
				fileDialog.setFilterPath(workingDir);
				// String homeDir = System.getProperty("user.home");
				// fileDialog.setFilterPath(homeDir);
				String path = fileDialog.open();
				if (path != null) {
					File file = new File(path);
					if (file.exists()) {
						filesizeLong = file.length();
						filesizeInt = (int) filesizeLong;
						System.out.println("Size long= " + filesizeLong
								+ ". int = " + filesizeInt);
					}
					FileReader fileReader = null;
					try {
						fileReader = new FileReader(path);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					if (fileReader != null) {
						CSVParser parser = new CSVParser(fileReader,
								CSVStrategy.EXCEL_STRATEGY);
						String[] values = null;
						try {
							values = parser.getLine();
						} catch (IOException e) {
							e.printStackTrace();
						}
						while (values != null) {
							// printValues(parser.getLineNumber(),values);
							DataRow dataRow = initDataRow(values);
							modelProvider.addDataRow(dataRow);
							ModelKeeper.saveModelProvider(path, modelProvider);
							// System.out.println(dataRow);
							try {
								values = parser.getLine();
							} catch (IOException e) {
								e.printStackTrace();
							}

						}
					}
					addFilename(path);
					IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					ViewData viewData = (ViewData) page.findView(ViewData.ID);
					String title = viewData.getTitle();
					System.out.println("title= " + title);
					viewData.update(path);
				}
				CSVParser c = null;
			}
		};
		actionImport.setText("Import...");
		actionImport.setToolTipText("Import CSV");
		actionImport.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJ_FILE));

		actionSave = new Action() {
			public void run() {
				System.out.println("executing actionSave");
				ISelection iSelection = viewer.getSelection();
				Object obj = ((IStructuredSelection) iSelection)
						.getFirstElement();
				System.out.println("saving file: " + obj);
				Shell shell = getViewSite().getShell();
				FileDialog dialog = new FileDialog(shell, SWT.SAVE);
				String[] filterNames = new String[] { "Image Files",
						"All Files (*)" };
				String[] filterExtensions = new String[] { "*.csv", "*" };
				String filterPath = "/";
				String platform = SWT.getPlatform();

				if (platform.equals("win32") || platform.equals("wpf")) {
					filterNames = new String[] { "Image Files",
							"All Files (*.*)" };
					filterExtensions = new String[] {
							"*.gif;*.png;*.bmp;*.jpg;*.jpeg;*.tiff", "*.*" };
					filterPath = "c:\\";
				}

				String workingDir = Util.getPreferenceStore().getString(
						"workingDir");
				if (workingDir.length() > 0) {
					dialog.setFilterPath(workingDir);
				} else {
					dialog.setFilterPath(filterPath);
				}

				dialog.setFilterNames(filterNames);
				dialog.setFilterExtensions(filterExtensions);

				dialog.setFileName("myfile");
				String saveTo = dialog.open();
				System.out.println("Save to: " + saveTo);

				try {
					File file = new File(saveTo);
					if (!file.exists()) {
						file.createNewFile();
					}

					FileWriter fw = new FileWriter(file.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write("this is the content");
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		};
		actionSave.setText("Save...");
		actionSave.setToolTipText("Save CSV");
		actionSave.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJ_FILE));

		actionClose = new Action() {
			public void run() {
				System.out.println("executing actionClose");
				ISelection iSelection = viewer.getSelection();
				Object obj = ((IStructuredSelection) iSelection)
						.getFirstElement();
				String key = (String) obj;
				ModelKeeper.remove(key);
				removeFilename(obj);
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				ViewData viewData = (ViewData) page.findView(ViewData.ID);
				viewData.clearView(key);
			}
		};
		actionClose.setText("Close");
		actionClose.setToolTipText("Close CSV");
		actionClose.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJ_FILE));

		actionExportToTDB = new Action() {
			public void run() {
				System.out.println("executing actionExportToTDB");
				ISelection iSelection = viewer.getSelection();
				System.out.println("iSelection=" + iSelection);
				if (!iSelection.isEmpty()) {
					Object obj = ((IStructuredSelection) iSelection)
							.getFirstElement();
					String key = (String) obj;
					IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					ViewData viewData = (ViewData) page.findView(ViewData.ID);
					System.out.println("key=" + key);
					MyDialog dialog = new MyDialog(Display.getCurrent()
							.getActiveShell());
					dialog.create();
					if (dialog.open() == Window.OK) {
						// String dataSourceIRI = dialog.getDataSourceIRI();
						String dataSourceLid = dialog.getDataSourceLid();
						System.out.println(dialog.getDataSourceName());
						System.out.println(dialog.getMajorVersion());
						System.out.println(dialog.getMinorVersion());
						System.out.println(dialog.getComment());
						String dataSourceName = dialog.getDataSourceName();
						String majorNumber = dialog.getMajorVersion();
						String minorNumber = dialog.getMinorVersion();
						String comment = dialog.getComment();

						// int next = 1;
						GenericQuery iGenericQuery = new GenericQuery(
								qGetNextDSIndex.getQuery(), "Internal Query");
						iGenericQuery.getData();
						QueryResults parts = iGenericQuery.getQueryResults();
						List<DataRow> resultRow = parts.getModelProvider()
								.getData();
						// if(resultRow.size() > 0){
						DataRow row = resultRow.get(0);
						List<String> valueList = row.getColumnValues();
						dataSourceLid = valueList.get(0);
						// next = Integer.parseInt(indexStr);

						IdsInfoQuery idsInfoQuery = new IdsInfoQuery(
								dataSourceLid, dataSourceName, majorNumber,
								minorNumber, comment);
						// IdsInfoQuery idsInfoQuery = new
						// IdsInfoQuery(dataSourceName, majorNumber,
						// minorNumber, comment);

						List<String> resultList = idsInfoQuery.getData();
						// System.out.println(resultList.toString());
						// System.out.println(idsInfoQuery.getQuery());
						ModelProvider modelProvider = ModelKeeper
								.getModelProvider(key);
						List<String> headers = modelProvider.getHeaderNames();
						System.out.println(headers.toString());
						List<DataRow> dataRowList = modelProvider.getData();
						int rowNumber = 1;
						System.out.println("dataRowList.size = "
								+ dataRowList.size());

						int N = 50000; // MAYBE SHOULD BE OPTIMIZED FIXME
						StringBuilder b = new StringBuilder();

						for (DataRow dataRow : dataRowList) {
							String casrn = null;
							String name = null;
							String altName = null;
							String cat = null;
							String subcat = null;
							String impactCat = null;
							String impactCatRefUnit = null;
							Double charFactor = null;
							String flowUnit = null;

							// CAT_HDR = "Category";
							// SUBCAT_HDR = "Subcategory";
							// IMPACT_CAT_HDR = "Impact_Category";
							// IMPACT_CAT_REF_UNIT_HDR = "Impact_cat_ref_unit";
							// CHAR_FACTOR_HDR = "Characterization_factor";
							// FLOW_UNIT_HDR = "Flow_Unit";

							{
								int index = headers.indexOf(ViewData.CASRN_HDR);
								if (index > -1) {
									String unescCasrn = dataRow
											.getColumnValues().get(index);
									casrn = Util.escape(unescCasrn);
									casrn = casrn
											.replaceFirst(
													"[^1-9]*(\\d{2,7})-?(\\d\\d)-?(\\d)\\D*$",
													"$1-$2-$3"); // REMOVE
																	// LEADING
																	// STUFF,
									// System.out.println("casrn=" + casrn);
									// casrn.replaceFirst(regex, replacement)
								}
							}
							{
								int index = headers.indexOf(ViewData.NAME_HDR);
								if (index > -1) {
									String unescName = dataRow
											.getColumnValues().get(index);
									name = Util.escape(unescName);
									// System.out.println("name=" + name);
								}
							}
							{
								int index = headers
										.indexOf(ViewData.ALT_NAME_HDR);
								if (index > -1) {
									String unescAltName = dataRow
											.getColumnValues().get(index);
									altName = Util.escape(unescAltName);
									// System.out.println("altName=" + altName);
								}
							}
							{
								int index = headers.indexOf(ViewData.CAT_HDR);
								if (index > -1) {
									String unescCat = dataRow.getColumnValues()
											.get(index);
									cat = Util.escape(unescCat);
									// System.out.println("cat=" + cat);
								}
							}
							{
								int index = headers
										.indexOf(ViewData.SUBCAT_HDR);
								if (index > -1) {
									String unescSubcat = dataRow
											.getColumnValues().get(index);
									subcat = Util.escape(unescSubcat);
									// System.out.println("subcat=" + subcat);
								}
							}
							{
								int index = headers
										.indexOf(ViewData.IMPACT_CAT_HDR);
								if (index > -1) {
									String unescImpactCat = dataRow
											.getColumnValues().get(index);
									impactCat = Util.escape(unescImpactCat);
									// System.out.println("impactCat=" +
									// impactCat);
								}
							}
							{
								int index = headers
										.indexOf(ViewData.IMPACT_CAT_REF_UNIT_HDR);
								if (index > -1) {
									String unescImpactCatRefUnit = dataRow
											.getColumnValues().get(index);
									impactCatRefUnit = Util
											.escape(unescImpactCatRefUnit);
									// System.out.println("impactCatRefUnit=" +
									// impactCatRefUnit);
								}
							}
							{
								int index = headers
										.indexOf(ViewData.CHAR_FACTOR_HDR);
								if (index > -1) {
									try {
										charFactor = Double.valueOf(dataRow
												.getColumnValues().get(index));
									} catch (NumberFormatException e) {
										charFactor = 0.0;
										// e.printStackTrace();
									}
									// System.out.println("charFactor=" +
									// charFactor);
								}
							}
							{
								int index = headers
										.indexOf(ViewData.FLOW_UNIT_HDR);
								if (index > -1) {
									String unescFlowUnit = dataRow
											.getColumnValues().get(index);
									flowUnit = Util.escape(unescFlowUnit);
									// System.out.println("flowUnit=" +
									// flowUnit);
								}
							}
							// String dataSourceIRI = "dude";
							IdsRowQuery idsRowQuery = new IdsRowQuery(casrn,
									dataSourceLid, name, altName, cat, subcat,
									impactCat, impactCatRefUnit, charFactor,
									flowUnit, "" + rowNumber);
							// IdsRowQuery idsRowQuery = new IdsRowQuery(casrn,
							// name, altName, cat, subcat, impactCat,
							// impactCatRefUnit, charFactor, flowUnit, "" +
							// rowNumber);
							//
							String insertTriples = idsRowQuery
									.getInsertTriples();
							b.append(insertTriples);
							// b.append(idsRowQuery.toString());

							// if (Integer.valueOf(rowNumber) == 5) {
							// System.out.println("Triples: "+insertTriples);
							// }
							if ((rowNumber % N == 0)
									|| (rowNumber == dataRowList.size())) {
								// add prefix
								String prefix = idsRowQuery.getPrefix();
								prefix += "\n INSERT DATA \n { \n";
								b.insert(0, prefix);
								b.append(" } \n");
								System.out.println(b.toString().substring(0,
										5000));
								// GenericQuery iGenericQuery = new
								// GenericQuery(b.toString(), "bundled insert");
								GenericUpdate iGenericUpdate = new GenericUpdate(
										b.toString(), "bundled insert");
								List<String> results = iGenericUpdate.getData();
								// System.out.println(results.toString());
								b.setLength(0);
							}
							rowNumber++;
						}

					}

				}
			}
		};
		actionExportToTDB.setText("Export");
		actionExportToTDB.setToolTipText("Export to TDB");
		actionExportToTDB.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJ_FILE));

		actionExportSubsToTDB = new Action() {
			public void run() {
				System.out.println("executing actionExportSubsToTDB");
				ISelection iSelection = viewer.getSelection();
				System.out.println("iSelection=" + iSelection);
				if (!iSelection.isEmpty()) {
					Object obj = ((IStructuredSelection) iSelection)
							.getFirstElement();
					String key = (String) obj;
					IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					ViewData viewData = (ViewData) page.findView(ViewData.ID);
					System.out.println("key=" + key);
					MyDialog dialog = new MyDialog(Display.getCurrent()
							.getActiveShell());
					dialog.create();
					if (dialog.open() == Window.OK) {
						System.out.println(dialog.getDataSourceLid());
						System.out.println(dialog.getDataSourceName());
						System.out.println(dialog.getMajorVersion());
						System.out.println(dialog.getMinorVersion());
						System.out.println(dialog.getComment());

						String dataSourceLid = dialog.getDataSourceLid();
						String dataSourceName = dialog.getDataSourceName();
						String majorNumber = dialog.getMajorVersion();
						String minorNumber = dialog.getMinorVersion();
						String comment = dialog.getComment();
						int dataSourceLidInt = 1; // DEFAULT IF NOT SPECIFIED
													// AND NO DATA SETS ARE
													// THERE

						if (dataSourceLid.matches("^\\d+$")) {
							dataSourceLidInt = Integer.parseInt(dataSourceLid);
						} else {
							GenericQuery iGenericQuery = new GenericQuery(
									qGetNextDSIndex.getQuery(),
									"Internal Query");
							iGenericQuery.getData();
							QueryResults parts = iGenericQuery
									.getQueryResults();
							List<DataRow> resultRow = parts.getModelProvider()
									.getData();
							if (resultRow.size() > 0) {
								DataRow row = resultRow.get(0);
								List<String> valueList = row.getColumnValues();
								dataSourceLidInt = Integer.parseInt(valueList
										.get(0));
							}
						}
						// ---------------------------------
						Model model = SelectTDB.model;
						if (model == null) {
							String msg = "ERROR no TDB open";
							Util.findView(QueryView.ID).getViewSite()
									.getActionBars().getStatusLineManager()
									.setMessage(msg);
							return;
						}
						System.out.println("Running ExportSubsToTDB internals");

						String eco_p = "http://ontology.earthster.org/eco/core#";
						String ethold_p = "http://epa.gov/nrmrl/std/lca/ethold#";
						String afn_p = "http://jena.hpl.hp.com/ARQ/function#";
						String fn_p = "http://www.w3.org/2005/xpath-functions#";
						String nfo_p = "http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#";
						String skos_p = "http://www.w3.org/2004/02/skos/core#";
						String sumo_p = "http://www.ontologyportal.org/SUMO.owl.rdf#";
						String xml_p = "http://www.w3.org/XML/1998/namespace";
						// THE FOLLOWING ARE IN Vocabulary
						// String owl_p = "http://www.w3.org/2002/07/owl#";
						// String rdf_p =
						// "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
						// String rdfs_p =
						// "http://www.w3.org/2000/01/rdf-schema#";
						// String xsd_p = "http://www.w3.org/2001/XMLSchema#";

						Resource ds = model.getResource(eco_p + "DataSource");
						Resource substance = model.getResource(eco_p
								+ "Substance");
						Property altLabel = model.getProperty(skos_p
								+ "altLabel");
						Property majV = model.getProperty(eco_p
								+ "hasMajorVersionNumber");
						Property minV = model.getProperty(eco_p
								+ "hasMinorVersionNumber");
						Property lid = model.getProperty(ethold_p
								+ "localSerialNumber");
						Property foundOnRow = model.getProperty(ethold_p
								+ "foundOnRow");
						Property casNumber = model.getProperty(eco_p
								+ "casNumber");
						Property hasDataSource = model.getProperty(eco_p
								+ "hasDataSource");
						Property fileSize = model.getProperty(nfo_p
								+ "fileSize");
						Literal dsLidLit = model
								.createTypedLiteral(dataSourceLidInt);
						Literal dsNameLit = model
								.createTypedLiteral(dataSourceName);
						Literal dsMajLit = model
								.createTypedLiteral(majorNumber);
						Literal dsMinLit = model
								.createTypedLiteral(minorNumber);
						Literal dsCommLit = model.createTypedLiteral(comment);
						Literal dsFileSizeLit = model
								.createTypedLiteral(filesizeInt);

						// Dataset dataset = SelectTDB.dataset;
						// GraphStore graphStore = SelectTDB.graphStore;
						DataRow columnHeaders = new DataRow();
						// queryResults.setColumnHeaders(columnHeaders);

						long change = model.size();

						columnHeaders.add("Model");
						columnHeaders.add("Size");

						System.err.printf("Before Update: %s\n", model.size());

						ModelProvider resultsViewModel = new ModelProvider();
						// queryResults.setModelProvider(modelProvider);
						DataRow resDataRow = new DataRow();
						resultsViewModel.addDataRow(resDataRow);
						resDataRow.add("Before Update");
						resDataRow.add("" + model.size());

						// model.getResource(arg0);

						// ---------------------------------

						// int next = 1;
						GenericQuery iGenericQuery = new GenericQuery(
								qGetNextDSIndex.getQuery(), "Internal Query");
						iGenericQuery.getData();
						QueryResults parts = iGenericQuery.getQueryResults();
						List<DataRow> resultRow = parts.getModelProvider()
								.getData();
						// if(resultRow.size() > 0){
						DataRow row = resultRow.get(0);
						List<String> valueList = row.getColumnValues();

						// IdsInfoQuery idsInfoQuery = new IdsInfoQuery(
						// dataSourceLid, dataSourceName, majorNumber,
						// minorNumber, comment);
						// IdsInfoQuery idsInfoQuery = new
						// IdsInfoQuery(dataSourceName, majorNumber,
						// minorNumber, comment);

						// List<String> resultList = idsInfoQuery.getData();
						// System.out.println(resultList.toString());
						// System.out.println(idsInfoQuery.getQuery());
						ModelProvider modelProvider = ModelKeeper
								.getModelProvider(key);
						List<String> headers = modelProvider.getHeaderNames();
						System.out.println(headers.toString());
						List<DataRow> dataRowList = modelProvider.getData();
						System.out.println("dataRowList.size = "
								+ dataRowList.size());

						// String eco = model.expandPrefix("eco");
						// -----------------------------------------

						// System.out.println("eco means: " + eco);
						// LOOP ONCE TO GET LARGEST ALREADY PRESENT
						Resource dsResourceHandle = null;
						System.out
								.println("Now to find a list of data sources...");

						// ResIterator dataSetResources = model
						// .listSubjectsWithProperty(RDF.type, ds);
						ResIterator dataSetResources = model
								.listResourcesWithProperty(RDF.type,
										ds.asNode());

						// LOOP THROUGH EACH DATA SET TO SEE IF THE LOCAL ID IS
						// ALREADY THERE
						// System.out.println("it worked this time...");

						while (dataSetResources.hasNext()) {
							Resource dsResource = dataSetResources.next();
							System.out.println("going...");
							if (model.contains(dsResource, lid, dsLidLit)) {
								dsResourceHandle = dsResource;
							}
						}
						// BUT IF WE DIDN'T FIND THE DATA SET THAT ALRAEDY HAS
						// THIS LID, MAKE ONE
						if (dsResourceHandle == null) {
							Resource tempHandle = model.createResource();
							model.add(tempHandle, RDF.type, ds);
							model.add(tempHandle, RDFS.label, dsNameLit); // REQUIRED
							model.add(tempHandle, lid, dsLidLit); // REQUIRED
							if (dsMajLit != null) {
								model.add(tempHandle, majV, dsMajLit); // OPTIONAL
							}
							if (dsMinLit != null) {

								model.add(tempHandle, minV, dsMinLit); // OPTIONAL
							}
							if (dsCommLit != null) {

								model.add(tempHandle, RDFS.comment, dsCommLit); // OPTIONAL
							}
							if (filesizeInt > 0) {
								model.add(tempHandle, fileSize, dsFileSizeLit);
							}
							dsResourceHandle = tempHandle;
						}

						Hashtable<String, Resource> str2res = new Hashtable<String, Resource>();

						System.out.println("Ready to iterate...");
						int csvRow = 0;
						for (DataRow csvDataRow : dataRowList) {
							if (csvRow % 10000 == 0) {
								System.out
										.println("Finished reading data file row: "
												+ csvRow);
							}

							Literal drRowLit = model.createTypedLiteral(csvRow);

							String name = null; // REQUIRED
							String altName = null; // OPTIONAL
							String casrn = null; // OPTIONAL

							Literal drNameLit = null;
							Literal drAltNameLit = null;
							Literal drCasLit = null;

							try {
								int index = headers.indexOf(ViewData.NAME_HDR);
								if (index > -1) {
									String unescName = csvDataRow
											.getColumnValues().get(index);
									name = Util.escape(unescName);
									// System.out.println("name=" + name);
									drNameLit = model.createTypedLiteral(name);
								} else {
									String msg = "Substances must have a \"Name\" field!";
									Util.findView(QueryView.ID).getViewSite()
											.getActionBars()
											.getStatusLineManager()
											.setMessage(msg);
									return; // FIXME -- IS THERE A "RIGHT" WAY
											// TO LEAVE

								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							try {
								int index = headers
										.indexOf(ViewData.ALT_NAME_HDR);
								if (index > -1) {
									String unescAltName = csvDataRow
											.getColumnValues().get(index);
									altName = Util.escape(unescAltName);
									// System.out.println("altName=" +
									// altName);
									drAltNameLit = model
											.createTypedLiteral(altName);
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							try {
								int index = headers.indexOf(ViewData.CASRN_HDR);
								if (index > -1) {
									String unescCasrn = csvDataRow
											.getColumnValues().get(index);
									casrn = Util.escape(unescCasrn);
									casrn = casrn
											.replaceFirst(
													"[^1-9]*(\\d{2,7})-?(\\d\\d)-?(\\d)\\D*$",
													"$1-$2-$3"); // REMOVE
																	// LEADING
																	// STUFF,
									drCasLit = model.createTypedLiteral(casrn);
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							// System.out.println("name, cas, altName: " + name
							// + ", " + casrn + ", " + altName);

							Resource subResourceHandle = null;

							String combined_str = name + altName + casrn;
							combined_str.hashCode();
							if (str2res.containsKey(combined_str)) {
								subResourceHandle = str2res.get(combined_str);
							} else {
								Resource newSub = model.createResource();
								newSub.addProperty(RDF.type, substance);
								newSub.addLiteral(RDFS.label, drNameLit);
								if (altName != null && altName.length() > 0) {
									newSub.addLiteral(altLabel, drAltNameLit);
								}
								if (casrn != null && casrn.length() > 0) {
									newSub.addLiteral(casNumber, drCasLit);
								}
								newSub.addProperty(hasDataSource,
										dsResourceHandle);
								subResourceHandle = newSub;
								str2res.put(combined_str, subResourceHandle);
							}
							subResourceHandle.addLiteral(foundOnRow, drRowLit);
							csvRow++;
						}
						// -----------------------------------------
						DataRow resDataRow2 = new DataRow();
						modelProvider.addDataRow(resDataRow2);
						resDataRow2.add("After Update");
						resDataRow2.add("" + model.size());

						change = model.size() - change;
						System.err.printf("Net Increase: %s\n", change);
						DataRow resDataRow3 = new DataRow();
						modelProvider.addDataRow(resDataRow3);

						String increase = "New Triples:";

						if (change < 0) {
							increase = "Triples removed:";
							change = 0 - change;
						}
						// data.add(increase);
						// data.add("" + change);
						resDataRow3.add(increase);
						resDataRow3.add("" + change);

						long startTime = System.currentTimeMillis();
						float elapsedTimeSec = (System.currentTimeMillis() - startTime) / 1000F;
						System.out.println("Time elapsed: " + elapsedTimeSec);
						System.err.printf("After Update: %s\n", model.size());
						System.out.println("done");

					}

				}

			}

		};
		actionExportSubsToTDB.setText("Export Subs");
		actionExportSubsToTDB.setToolTipText("Export only substances to TDB");
		actionExportSubsToTDB.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJ_FILE));

		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) viewer
						.getSelection();
				if (selection.isEmpty())
					return;
				String key = (String) selection.toList().get(0);
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				ViewData viewData = (ViewData) page.findView(ViewData.ID);
				viewData.update(key);
			}

		});

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void addFilename(final String url) {
		viewer.add(url);
	}

	public void removeFilename(Object element) {
		if (element == null)
			return;

		viewer.remove(element);
	}

}