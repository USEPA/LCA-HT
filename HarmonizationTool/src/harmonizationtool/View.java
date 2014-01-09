package harmonizationtool;

import harmonizationtool.comands.SelectTDB;

import harmonizationtool.dialog.CSVMetaDialog;
import harmonizationtool.dialog.MyDialog;
import harmonizationtool.dialog.PlayArea;
import harmonizationtool.handler.ShowDataViewHandler;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.DataSetKeeper;
import harmonizationtool.model.DataSetProvider;
import harmonizationtool.model.ModelKeeper;
import harmonizationtool.model.ModelProvider;
import harmonizationtool.query.GenericQuery;
import harmonizationtool.query.GenericUpdate;
import harmonizationtool.query.IdsInfoQuery;
import harmonizationtool.query.IdsRowQuery;
import harmonizationtool.query.QueryResults;
import harmonizationtool.utils.DataSetMap;
import harmonizationtool.utils.ResourceIdMgr;
import harmonizationtool.utils.Util;
import harmonizationtool.query.ZGetNextDSIndex;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.SKOS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.function.library.date;
import com.hp.hpl.jena.vocabulary.DCTerms;
//import com.hp.hpl.jena.vocabulary.NFO; // DOES NOT EXIST!!
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class View extends ViewPart {
	public static final String ID = "HarmonizationTool.view";

	private TableViewer viewer;
	// private Action actionImport;
	private Action actionSave;
	private Action actionClose;
	private Action editMeta;

	// private Action actionExportToTDB;
	private Action actionParseFlowablesToTDB;
	private Action actionParseCategoriesToTDB;

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
		// manager.add(actionImport);
		// manager.add(actionExportToTDB); // NEEDS REPAIR TODO
		manager.add(actionParseFlowablesToTDB);
		manager.add(actionParseCategoriesToTDB);
		manager.add(actionSave);
		manager.add(editMeta);
		manager.add(actionClose);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private long filesizeLong;
	private int filesizeInt;
	// private date filedate_rdf = null;
	// private Date filedate_java = null;
	private Calendar filedate_java;
	private String filenameStr; // FIXME: SHOULD USE THIS IN THE DATA SET
	private List<Resource> dsList = new ArrayList<Resource>();

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
		// actionImport = new Action() {
		// public void run() {
		// System.out.println("executing actionImport");
		// ModelProvider modelProvider = new ModelProvider();
		// FileDialog fileDialog = new FileDialog(
		// getViewSite().getShell(), SWT.OPEN);
		// fileDialog.setFilterExtensions(new String[] { "*.csv" });
		// String workingDir = Util.getPreferenceStore().getString(
		// "workingDir");
		// fileDialog.setFilterPath(workingDir);
		// // String homeDir = System.getProperty("user.home");
		// // fileDialog.setFilterPath(homeDir);
		// String path = fileDialog.open();
		// if (path != null) {
		// File file = new File(path);
		// if (file.exists()) {
		// filenameStr = file.getName();
		// filesizeLong = file.length();
		// filesizeInt = (int) filesizeLong;
		// System.out.println("Size long= " + filesizeLong
		// + ". int = " + filesizeInt);
		// filedate_java = Calendar.getInstance();
		// filedate_java.setTime(new Date(file.lastModified()));
		// System.out.println("filedate_java = "
		// + filedate_java.toString());
		// System.out.println("filedate_java timeZone = "
		// + filedate_java.getTimeZone());
		// }
		// FileReader fileReader = null;
		// try {
		// fileReader = new FileReader(path);
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// }
		// if (fileReader != null) {
		// CSVParser parser = new CSVParser(fileReader,
		// CSVStrategy.EXCEL_STRATEGY);
		// String[] values = null;
		// try {
		// values = parser.getLine();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// while (values != null) {
		// // printValues(parser.getLineNumber(),values);
		// DataRow dataRow = initDataRow(values);
		// modelProvider.addDataRow(dataRow);
		// ModelKeeper.saveModelProvider(path, modelProvider);
		// // System.out.println(dataRow);
		// try {
		// values = parser.getLine();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		//
		// }
		// }
		// addFilename(path);
		// IWorkbenchPage page = PlatformUI.getWorkbench()
		// .getActiveWorkbenchWindow().getActivePage();
		// ViewData viewData = (ViewData) page.findView(ViewData.ID);
		//
		// String title = viewData.getTitle();
		// System.out.println("title= " + title);
		// viewData.update(path);
		// try {
		// Util.showView(ViewData.ID);
		// } catch (PartInitException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// CSVParser c = null;
		// }
		// };
		// actionImport.setText("Import...");
		// actionImport.setToolTipText("Import CSV");
		// actionImport.setImageDescriptor(PlatformUI.getWorkbench()
		// .getSharedImages()
		// .getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
		//

		editMeta = new Action() {
			public void run() {
				System.out.println("edit Meta Data");

				IStructuredSelection selection = (IStructuredSelection) viewer
						.getSelection();
				if (selection.isEmpty()) {
					return;
				}
				System.out.println(selection.getFirstElement().toString());
				String key = (String) selection.toList().get(0);
				DataSetProvider dataSetProvider = DataSetKeeper.get(0); // FIXME
				// Map<String, String> metaData = csvFile.metaData;
				CSVMetaDialog dialog = new CSVMetaDialog(Display.getCurrent()
						.getActiveShell(), dataSetProvider);
				dialog.create();
				dialog.open();
			}
		};
		editMeta.setText("Edit Meta Data");
		editMeta.setToolTipText("See / Change data for this file");
		editMeta.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
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

		// actionExportToTDB = new Action() {
		// public void run() {
		// System.out.println("executing actionExportToTDB");
		// ISelection iSelection = viewer.getSelection();
		// System.out.println("iSelection=" + iSelection);
		// if (!iSelection.isEmpty()) {
		// Object obj = ((IStructuredSelection) iSelection)
		// .getFirstElement();
		// String key = (String) obj;
		// IWorkbenchPage page = PlatformUI.getWorkbench()
		// .getActiveWorkbenchWindow().getActivePage();
		// ViewData viewData = (ViewData) page.findView(ViewData.ID);
		// System.out.println("key=" + key);
		// MyDialog dialog = new MyDialog(Display.getCurrent()
		// .getActiveShell());
		// dialog.create();
		// if (dialog.open() == Window.OK) {
		// // String dataSourceIRI = dialog.getDataSourceIRI();
		// String dataSourceLid = dialog.getDataSourceLid();
		// System.out.println(dialog.getDataSourceName());
		// System.out.println(dialog.getMajorVersion());
		// System.out.println(dialog.getMinorVersion());
		// System.out.println(dialog.getComment());
		// String dataSourceName = dialog.getDataSourceName();
		// String majorNumber = dialog.getMajorVersion();
		// String minorNumber = dialog.getMinorVersion();
		// String comment = dialog.getComment();
		//
		// // int next = 1;
		// GenericQuery iGenericQuery = new GenericQuery(
		// qGetNextDSIndex.getQuery(), "Internal Query");
		// iGenericQuery.getData();
		// QueryResults parts = iGenericQuery.getQueryResults();
		// List<DataRow> resultRow = parts.getModelProvider()
		// .getData();
		// // if(resultRow.size() > 0){
		// DataRow row = resultRow.get(0);
		// List<String> valueList = row.getColumnValues();
		// dataSourceLid = valueList.get(0);
		// // next = Integer.parseInt(indexStr);
		//
		// IdsInfoQuery idsInfoQuery = new IdsInfoQuery(
		// dataSourceLid, dataSourceName, majorNumber,
		// minorNumber, comment);
		// // IdsInfoQuery idsInfoQuery = new
		// // IdsInfoQuery(dataSourceName, majorNumber,
		// // minorNumber, comment);
		//
		// List<String> resultList = idsInfoQuery.getData();
		// // System.out.println(resultList.toString());
		// // System.out.println(idsInfoQuery.getQuery());
		// ModelProvider modelProvider = ModelKeeper
		// .getModelProvider(key);
		// List<String> headers = modelProvider.getHeaderNames();
		// System.out.println(headers.toString());
		// List<DataRow> dataRowList = modelProvider.getData();
		// int rowNumber = 1;
		// System.out.println("dataRowList.size = "
		// + dataRowList.size());
		//
		// int N = 50000; // MAYBE SHOULD BE OPTIMIZED FIXME
		// StringBuilder b = new StringBuilder();
		//
		// for (DataRow dataRow : dataRowList) {
		// String casrn = null;
		// String name = null;
		// String altName = null;
		// String cat = null;
		// String subcat = null;
		// String impactCat = null;
		// String impactCatRefUnit = null;
		// Double charFactor = null;
		// String flowUnit = null;
		//
		// // CAT_HDR = "Category";
		// // SUBCAT_HDR = "Subcategory";
		// // SUBSUBCAT_HDR = "Sub-subcategory";
		// // IMPACT_CAT_HDR = "Impact_Category";
		// // IMPACT_CAT_REF_UNIT_HDR = "Impact_cat_ref_unit";
		// // CHAR_FACTOR_HDR = "Characterization_factor";
		// // FLOW_UNIT_HDR = "Flow_Unit";
		//
		// {
		// int index = headers.indexOf(ViewData.CASRN_HDR);
		// if (index > -1) {
		// String unescCasrn = dataRow
		// .getColumnValues().get(index);
		// casrn = Util.escape(unescCasrn);
		// casrn = casrn
		// .replaceFirst(
		// "[^1-9]*(\\d{2,7})-?(\\d\\d)-?(\\d)\\D*$",
		// "$1-$2-$3"); // REMOVE
		// // LEADING
		// // STUFF,
		// // System.out.println("casrn=" + casrn);
		// // casrn.replaceFirst(regex, replacement)
		// }
		// }
		// {
		// int index = headers.indexOf(ViewData.NAME_HDR);
		// if (index > -1) {
		// String unescName = dataRow
		// .getColumnValues().get(index);
		// name = Util.escape(unescName);
		// // System.out.println("name=" + name);
		// }
		// }
		// {
		// int index = headers
		// .indexOf(ViewData.ALT_NAME_HDR);
		// if (index > -1) {
		// String unescAltName = dataRow
		// .getColumnValues().get(index);
		// altName = Util.escape(unescAltName);
		// // System.out.println("altName=" + altName);
		// }
		// }
		// {
		// int index = headers.indexOf(ViewData.CAT1_HDR);
		// if (index > -1) {
		// String unescCat = dataRow.getColumnValues()
		// .get(index);
		// cat = Util.escape(unescCat);
		// // System.out.println("cat=" + cat);
		// }
		// }
		// {
		// int index = headers
		// .indexOf(ViewData.CAT2_HDR);
		// if (index > -1) {
		// String unescSubcat = dataRow
		// .getColumnValues().get(index);
		// subcat = Util.escape(unescSubcat);
		// // System.out.println("subcat=" + subcat);
		// }
		// }
		// {
		// int index = headers
		// .indexOf(ViewData.IMPACT_CAT_HDR);
		// if (index > -1) {
		// String unescImpactCat = dataRow
		// .getColumnValues().get(index);
		// impactCat = Util.escape(unescImpactCat);
		// // System.out.println("impactCat=" +
		// // impactCat);
		// }
		// }
		// {
		// int index = headers
		// .indexOf(ViewData.IMPACT_CAT_REF_UNIT_HDR);
		// if (index > -1) {
		// String unescImpactCatRefUnit = dataRow
		// .getColumnValues().get(index);
		// impactCatRefUnit = Util
		// .escape(unescImpactCatRefUnit);
		// // System.out.println("impactCatRefUnit=" +
		// // impactCatRefUnit);
		// }
		// }
		// {
		// int index = headers
		// .indexOf(ViewData.CHAR_FACTOR_HDR);
		// if (index > -1) {
		// try {
		// charFactor = Double.valueOf(dataRow
		// .getColumnValues().get(index));
		// } catch (NumberFormatException e) {
		// charFactor = 0.0;
		// // e.printStackTrace();
		// }
		// // System.out.println("charFactor=" +
		// // charFactor);
		// }
		// }
		// {
		// int index = headers
		// .indexOf(ViewData.FLOW_UNIT_HDR);
		// if (index > -1) {
		// String unescFlowUnit = dataRow
		// .getColumnValues().get(index);
		// flowUnit = Util.escape(unescFlowUnit);
		// // System.out.println("flowUnit=" +
		// // flowUnit);
		// }
		// }
		// // String dataSourceIRI = "dude";
		// IdsRowQuery idsRowQuery = new IdsRowQuery(casrn,
		// dataSourceLid, name, altName, cat, subcat,
		// impactCat, impactCatRefUnit, charFactor,
		// flowUnit, "" + rowNumber);
		// // IdsRowQuery idsRowQuery = new IdsRowQuery(casrn,
		// // name, altName, cat, subcat, impactCat,
		// // impactCatRefUnit, charFactor, flowUnit, "" +
		// // rowNumber);
		// //
		// String insertTriples = idsRowQuery
		// .getInsertTriples();
		// b.append(insertTriples);
		// // b.append(idsRowQuery.toString());
		//
		// // if (Integer.valueOf(rowNumber) == 5) {
		// // System.out.println("Triples: "+insertTriples);
		// // }
		// if ((rowNumber % N == 0)
		// || (rowNumber == dataRowList.size())) {
		// // add prefix
		// String prefix = idsRowQuery.getPrefix();
		// prefix += "\n INSERT DATA \n { \n";
		// b.insert(0, prefix);
		// b.append(" } \n");
		// System.out.println(b.toString().substring(0,
		// 5000));
		// // GenericQuery iGenericQuery = new
		// // GenericQuery(b.toString(), "bundled insert");
		// GenericUpdate iGenericUpdate = new GenericUpdate(
		// b.toString(), "bundled insert");
		// List<String> results = iGenericUpdate.getData();
		// // System.out.println(results.toString());
		// b.setLength(0);
		// }
		// rowNumber++;
		// }
		//
		// }
		//
		// }
		// }
		// };
		// actionExportToTDB.setText("Export");
		// actionExportToTDB.setToolTipText("Export to TDB");
		// actionExportToTDB.setImageDescriptor(PlatformUI.getWorkbench()
		// .getSharedImages()
		// .getImageDescriptor(ISharedImages.IMG_OBJ_FILE));

		actionParseFlowablesToTDB = new Action() {
			public void run() {
				System.out.println("executing actionParseSubsToTDB");
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
					// PlayArea dialog = new
					// PlayArea(Display.getCurrent().getActiveShell(), null);

					dialog.create();
					if (dialog.open() == Window.OK) {
						// DataSetMap dataSetMap = DataSetMap.getInstance();

						String dataSourceLid = dialog.getDataSourceLid();
						String dataSourceName = dialog.getDataSourceName();
						String majorNumber = dialog.getMajorVersion();
						String minorNumber = dialog.getMinorVersion();
						String comment = dialog.getComment();
						// String dataSourceLid = "lid";
						// String dataSourceName = "dsName";
						// String majorNumber = "major";
						// String minorNumber = "minor";
						// String comment = "comment";

						System.out.println(dataSourceLid);
						System.out.println(dataSourceName);
						System.out.println(majorNumber);
						System.out.println(minorNumber);
						System.out.println(comment);

						Model model = SelectTDB.model;
						if (model == null) {
							String msg = "ERROR no TDB open";
							Util.findView(QueryView.ID).getViewSite()
									.getActionBars().getStatusLineManager()
									.setMessage(msg);
							return;
						}

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

						System.out.println("Running ExportSubsToTDB internals");

						// String eco_p =
						// "http://ontology.earthster.org/eco/core#";
						String ethold_p = "http://epa.gov/nrmrl/std/lca/ethold#";
						String afn_p = "http://jena.hpl.hp.com/ARQ/function#";
						String fn_p = "http://www.w3.org/2005/xpath-functions#";
						String nfo_p = "http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#";
						// String skos_p =
						// "http://www.w3.org/2004/02/skos/core#";
						String sumo_p = "http://www.ontologyportal.org/SUMO.owl.rdf#";
						String xml_p = "http://www.w3.org/XML/1998/namespace";
						// THE FOLLOWING ARE IN Vocabulary
						// String owl_p = "http://www.w3.org/2002/07/owl#";
						// String rdf_p =
						// "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
						// String rdfs_p =
						// "http://www.w3.org/2000/01/rdf-schema#";
						// String xsd_p = "http://www.w3.org/2001/XMLSchema#";

						// Resource ds = model.getResource(eco_p +
						// "DataSource");
						// Resource flowable = model.getResource(eco_p
						// + "Flowable");
						// Property altLabel = model.getProperty(skos_p
						// + "altLabel");
						// Property majV = model.getProperty(eco_p
						// + "hasMajorVersionNumber");
						// Property minV = model.getProperty(eco_p
						// + "hasMinorVersionNumber");
						Property lid = model.getProperty(ethold_p
								+ "localSerialNumber");
						Property foundOnRow = model.getProperty(ethold_p
								+ "foundOnRow");
						Property HTuserName = model.getProperty(ethold_p
								+ "HTuserName");
						Property HTuserAffiliation = model.getProperty(ethold_p
								+ "HTuserAffiliation");
						Property HTuserPhone = model.getProperty(ethold_p
								+ "HTuserPhone");
						Property HTuserEmail = model.getProperty(ethold_p
								+ "HTuserEmail");
						Property dataParseTimeStamp = model
								.getProperty(ethold_p + "dataParseTimeStamp");
						// Property casNumber = model.getProperty(eco_p
						// + "casNumber");
						// Property hasDataSource = model.getProperty(eco_p
						// + "hasDataSource");
						Property fileName = model.getProperty(nfo_p
								+ "fileName");
						Property fileSize = model.getProperty(nfo_p
								+ "fileSize");
						Property fileLastModified = model.getProperty(nfo_p
								+ "fileLastModified");
						Literal dsLidLit = model
								.createTypedLiteral(dataSourceLidInt);
						Literal dsNameLit = model
								.createTypedLiteral(dataSourceName);
						Literal dsMajLit = model
								.createTypedLiteral(majorNumber);
						Literal dsMinLit = model
								.createTypedLiteral(minorNumber);
						Literal dsCommLit = model.createTypedLiteral(comment);
						Literal dsFileNameLit = model
								.createTypedLiteral(filenameStr);
						Literal dsFileSizeLit = model
								.createTypedLiteral(filesizeInt);
						Literal dsFileDateLit = model
								.createTypedLiteral(filedate_java);

						Literal dsHTuserName = model.createTypedLiteral(Util
								.getPreferenceStore().getString("userName"));
						Literal dsHTuserAffiliation = model
								.createTypedLiteral(Util.getPreferenceStore()
										.getString("userAffiliation"));
						Literal dsHTuserPhone = model.createTypedLiteral(Util
								.getPreferenceStore().getString("userPhone"));
						Literal dsHTuserEmail = model.createTypedLiteral(Util
								.getPreferenceStore().getString("userEmail"));

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
						// List<Resource> dataSetHandles = null;

						// String eco = model.expandPrefix("eco");
						// -----------------------------------------

						// System.out.println("eco means: " + eco);
						// LOOP ONCE TO GET LARGEST ALREADY PRESENT
						Resource dsResourceHandle = null;
						if (ResourceIdMgr.contains(dataSourceLidInt)) {
							dsResourceHandle = ResourceIdMgr
									.getResource(dataSourceLidInt);
						} else {
							dsResourceHandle = model.createResource();
							model.add(dsResourceHandle, RDF.type,
									ECO.DataSource);
							model.add(dsResourceHandle, RDFS.label, dsNameLit); // REQUIRED
							model.add(dsResourceHandle, lid, dsLidLit); // TO
																		// REMOVE
							if (dsMajLit != null) {
								model.add(dsResourceHandle,
										ECO.hasMajorVersionNumber, dsMajLit); // OPTIONAL
							}
							if (dsMinLit != null) {

								model.add(dsResourceHandle,
										ECO.hasMinorVersionNumber, dsMinLit); // OPTIONAL
							}
							if (dsCommLit != null) {

								model.add(dsResourceHandle, RDFS.comment,
										dsCommLit); // OPTIONAL
							}
							if (filenameStr != null) {
								model.add(dsResourceHandle, fileName,
										dsFileNameLit);
							}
							if (filesizeInt > 0) {
								model.add(dsResourceHandle, fileSize,
										dsFileSizeLit);
							}
							if (filedate_java != null) {
								model.add(dsResourceHandle, fileLastModified,
										dsFileDateLit);
							}
							if (Util.getPreferenceStore().getString("userName")
									.length() > 0) {
								model.add(dsResourceHandle, HTuserName,
										dsHTuserName);
							}
							if (Util.getPreferenceStore()
									.getString("userAffiliation").length() > 0) {
								model.add(dsResourceHandle, HTuserAffiliation,
										dsHTuserAffiliation);
							}
							if (Util.getPreferenceStore()
									.getString("userPhone").length() > 0) {
								model.add(dsResourceHandle, HTuserPhone,
										dsHTuserPhone);
							}
							if (Util.getPreferenceStore()
									.getString("userEmail").length() > 0) {
								model.add(dsResourceHandle, HTuserEmail,
										dsHTuserEmail);
							}
							model.add(dsResourceHandle, dataParseTimeStamp,
									model.createTypedLiteral(Calendar
											.getInstance()));

							ResourceIdMgr.add(dsResourceHandle);
						}
						// Resource
						// ResourceIdMgr.add(resource);
						// Resource dsResourceHandle = null;
						System.out
								.println("Now to find a list of data sources...");

						// ResIterator dataSetResources = model
						// .listSubjectsWithProperty(RDF.type, ds);

						// LOOP THROUGH EACH DATA SET TO SEE IF THE LOCAL ID IS
						// ALREADY THERE
						// System.out.println("it worked this time...");
						// HashSet<Resource> dsHashLids = new
						// HashSet<Resource>();
						// List<Resource> dsList = null;
						// dsList.

						// dsList = new List<Resource>();
						// ResIterator dataSetResources = model
						// .listSubjectsWithProperty(RDF.type, ds);
						// while (dataSetResources.hasNext()) {
						// Resource dsResource = dataSetResources.next();
						// // dataSetHandles.add(dsResource);
						// StmtIterator lidIterator = dsResource
						// .listProperties(lid);
						// if (lidIterator.hasNext()) {
						// Statement stmt = lidIterator.next();
						// System.out.println("getLiteral().getInt = "
						// + stmt.getLiteral().getInt());
						//
						// System.out.println("getInt = " + stmt.getInt());
						// // dsList.add(dsResource);
						// while (dsList.size() < stmt.getInt()) {
						// dsList.add(null);
						// }
						// dsList.add(stmt.getLiteral().getInt(),
						// dsResource);
						// System.out.println("got lid: "
						// + dsList.indexOf(dsResource));
						// } else {
						// // THIS RESOURCE HAS NO LID
						// System.out.println("This resource had no LID");
						// }
						// if (lidIterator.hasNext()) {
						// System.out
						// .println("This resource had more than one LID");
						// // THIS RESOURCE HAS MORE THAN ONE LID
						// }
						// if (model.contains(dsResource, lid, dsLidLit)) {
						// dsResourceHandle = dsResource;
						// }
						// }
						// BUT IF WE DIDN'T FIND THE DATA SET THAT ALRAEDY HAS
						// THIS LID, MAKE ONE
						if (dsResourceHandle == null) {
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
									String msg = "Flowables must have a \"Name\" field!";
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
								model.add(newSub, RDF.type, ECO.Flowable);
								model.addLiteral(newSub, RDFS.label, drNameLit);
								// newSub.addProperty(RDF.type, flowable);
								// newSub.addLiteral(RDFS.label, drNameLit);
								if (altName != null && altName.length() > 0) {
									// newSub.addLiteral(altLabel,
									// drAltNameLit);
									model.addLiteral(newSub, SKOS.altLabel,
											drAltNameLit);
								}
								if (casrn != null && casrn.length() > 0) {
									model.addLiteral(newSub, ECO.casNumber,
											drCasLit);
									// newSub.addLiteral(casNumber, drCasLit);
								}
								model.add(newSub, ECO.hasDataSource,
										dsResourceHandle);
								// newSub.addProperty(hasDataSource,
								// dsResourceHandle);
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
						try {
							Util.showView(ResultsView.ID);
						} catch (PartInitException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

				}

			}

		};
		actionParseFlowablesToTDB.setText("Parse Flowables");
		actionParseFlowablesToTDB.setToolTipText("Parse flowables to TDB");
		actionParseFlowablesToTDB.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJ_FILE));

		actionParseCategoriesToTDB = new Action() {
			public void run() {
				System.out.println("executing actionParseCatsToTDB");
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
						String dataSourceLid = dialog.getDataSourceLid();
						String dataSourceName = dialog.getDataSourceName();
						String majorNumber = dialog.getMajorVersion();
						String minorNumber = dialog.getMinorVersion();
						String comment = dialog.getComment();

						System.out.println(dataSourceLid);
						System.out.println(dataSourceName);
						System.out.println(majorNumber);
						System.out.println(minorNumber);
						System.out.println(comment);

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
						Resource category = model.getResource(ethold_p
								+ "Category");
						Property majV = model.getProperty(eco_p
								+ "hasMajorVersionNumber");
						Property minV = model.getProperty(eco_p
								+ "hasMinorVersionNumber");
						Property lid = model.getProperty(ethold_p
								+ "localSerialNumber");
						Property foundOnRow = model.getProperty(ethold_p
								+ "foundOnRow");
						Property HTuserName = model.getProperty(ethold_p
								+ "HTuserName");
						Property HTuserAffiliation = model.getProperty(ethold_p
								+ "HTuserAffiliation");
						Property HTuserPhone = model.getProperty(ethold_p
								+ "HTuserPhone");
						Property HTuserEmail = model.getProperty(ethold_p
								+ "HTuserEmail");
						Property dataParseTimeStamp = model
								.getProperty(ethold_p + "dataParseTimeStamp");
						Property cat1Prop = model.getProperty(ethold_p
								+ "cat1Prop");
						Property cat2Prop = model.getProperty(ethold_p
								+ "cat2Prop");
						Property cat3Prop = model.getProperty(ethold_p
								+ "cat3Prop");
						Property hasDataSource = model.getProperty(eco_p
								+ "hasDataSource");
						Property fileName = model.getProperty(nfo_p
								+ "fileName");
						Property fileSize = model.getProperty(nfo_p
								+ "fileSize");
						Property fileLastModified = model.getProperty(nfo_p
								+ "fileLastModified");
						Literal dsLidLit = model
								.createTypedLiteral(dataSourceLidInt);
						Literal dsNameLit = model
								.createTypedLiteral(dataSourceName);
						Literal dsMajLit = model
								.createTypedLiteral(majorNumber);
						Literal dsMinLit = model
								.createTypedLiteral(minorNumber);
						Literal dsCommLit = model.createTypedLiteral(comment);
						Literal dsFileNameLit = model
								.createTypedLiteral(filenameStr);
						Literal dsFileSizeLit = model
								.createTypedLiteral(filesizeInt);
						Literal dsFileDateLit = model
								.createTypedLiteral(filedate_java);

						Literal dsHTuserName = model.createTypedLiteral(Util
								.getPreferenceStore().getString("userName"));
						Literal dsHTuserAffiliation = model
								.createTypedLiteral(Util.getPreferenceStore()
										.getString("userAffiliation"));
						Literal dsHTuserPhone = model.createTypedLiteral(Util
								.getPreferenceStore().getString("userPhone"));
						Literal dsHTuserEmail = model.createTypedLiteral(Util
								.getPreferenceStore().getString("userEmail"));

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

						ModelProvider modelProvider = ModelKeeper
								.getModelProvider(key);
						List<String> headers = modelProvider.getHeaderNames();
						System.out.println(headers.toString());
						List<DataRow> dataRowList = modelProvider.getData();
						System.out.println("dataRowList.size = "
								+ dataRowList.size());

						Resource dsResourceHandle = null;
						System.out
								.println("Now to find a list of data sources...");

						ResIterator dataSetResources = model
								.listSubjectsWithProperty(RDF.type, ds);
						while (dataSetResources.hasNext()) {
							Resource dsResource = dataSetResources.next();
							// dataSetHandles.add(dsResource);
							StmtIterator lidIterator = dsResource
									.listProperties(lid);
							if (lidIterator.hasNext()) {
								Statement stmt = lidIterator.next();
								System.out.println("getLiteral().getInt = "
										+ stmt.getLiteral().getInt());

								System.out.println("getInt = " + stmt.getInt());
								// dsList.add(dsResource);
								while (dsList.size() < stmt.getInt()) {
									dsList.add(null);
								}
								dsList.add(stmt.getLiteral().getInt(),
										dsResource);
								System.out.println("got lid: "
										+ dsList.indexOf(dsResource));
							} else {
								// THIS RESOURCE HAS NO LID
								System.out.println("This resource had no LID");
							}
							if (lidIterator.hasNext()) {
								System.out
										.println("This resource had more than one LID");
								// THIS RESOURCE HAS MORE THAN ONE LID
							}
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
							if (filenameStr != null) {
								model.add(tempHandle, fileName, dsFileNameLit);
							}
							if (filesizeInt > 0) {
								model.add(tempHandle, fileSize, dsFileSizeLit);
							}
							if (filedate_java != null) {
								model.add(tempHandle, fileLastModified,
										dsFileDateLit);
							}
							if (Util.getPreferenceStore().getString("userName")
									.length() > 0) {
								model.add(tempHandle, HTuserName, dsHTuserName);
							}
							if (Util.getPreferenceStore()
									.getString("userAffiliation").length() > 0) {
								model.add(tempHandle, HTuserAffiliation,
										dsHTuserAffiliation);
							}
							if (Util.getPreferenceStore()
									.getString("userPhone").length() > 0) {
								model.add(tempHandle, HTuserPhone,
										dsHTuserPhone);
							}
							if (Util.getPreferenceStore()
									.getString("userEmail").length() > 0) {
								model.add(tempHandle, HTuserEmail,
										dsHTuserEmail);
							}
							model.add(tempHandle, dataParseTimeStamp, model
									.createTypedLiteral(Calendar.getInstance()));
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

							String cat1 = null; // REQUIRED
							String cat2 = null; // OPTIONAL
							String cat3 = null; // OPTIONAL

							Literal drCat1Lit = null;
							Literal drCat2Lit = null;
							Literal drCat3Lit = null;

							try {
								int index = headers.indexOf(ViewData.CAT1_HDR);
								if (index > -1) {
									String unescCat1 = csvDataRow
											.getColumnValues().get(index);
									cat1 = Util.escape(unescCat1);
									// System.out.println("cat=" + cat);
									drCat1Lit = model.createTypedLiteral(cat1);
								}

								else {
									String msg = "Categories must have a \"Cat1\" field!";
									Util.findView(QueryView.ID).getViewSite()
											.getActionBars()
											.getStatusLineManager()
											.setMessage(msg);
									return; // FIXME -- IS THERE A "RIGHT"
											// WAY
											// TO LEAVE

								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							try {
								{
									int index = headers
											.indexOf(ViewData.CAT2_HDR);
									if (index > -1) {
										String unescCat2 = csvDataRow
												.getColumnValues().get(index);
										cat2 = Util.escape(unescCat2);
										drCat2Lit = model
												.createTypedLiteral(cat2);
										// System.out.println("subcat=" +
										// subcat);
									}
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							try {
								{
									int index = headers
											.indexOf(ViewData.CAT3_HDR);
									if (index > -1) {
										String unescCat3 = csvDataRow
												.getColumnValues().get(index);
										cat3 = Util.escape(unescCat3);
										drCat3Lit = model
												.createTypedLiteral(cat3);
										// System.out.println("subcat=" +
										// subcat);
									}
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							// System.out.println("name, cas, altName: " + name
							// + ", " + casrn + ", " + altName);

							Resource catResourceHandle = null;

							String combined_str = cat1 + cat2 + cat3;
							combined_str.hashCode();
							if (str2res.containsKey(combined_str)) {
								catResourceHandle = str2res.get(combined_str);
							} else {
								Resource newCat = model.createResource();
								model.add(newCat, RDF.type, category);
								model.addLiteral(newCat, cat1Prop, drCat1Lit);
								// newSub.addProperty(RDF.type, flowable);
								// newSub.addLiteral(RDFS.label, drNameLit);
								if (cat2 != null && cat2.length() > 0) {
									// newSub.addLiteral(altLabel,
									// drAltNameLit);
									model.addLiteral(newCat, cat2Prop,
											drCat2Lit);
								}
								if (cat3 != null && cat3.length() > 0) {
									model.addLiteral(newCat, cat3Prop,
											drCat3Lit);
									// newSub.addLiteral(casNumber, drCasLit);
								}
								model.add(newCat, hasDataSource,
										dsResourceHandle);
								// newSub.addProperty(hasDataSource,
								// dsResourceHandle);
								catResourceHandle = newCat;
								str2res.put(combined_str, catResourceHandle);
							}
							catResourceHandle.addLiteral(foundOnRow, drRowLit);
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
						try {
							Util.showView(ResultsView.ID);
						} catch (PartInitException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

				}

			}

		};
		actionParseCategoriesToTDB.setText("Parse Categories");
		actionParseCategoriesToTDB.setToolTipText("Parse Categories to TDB");
		actionParseCategoriesToTDB.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJ_FILE));

		// viewer.addDoubleClickListener(new IDoubleClickListener() {
		// // viewer.add
		//
		// @Override
		// public void doubleClick(DoubleClickEvent event) {
		// IStructuredSelection selection = (IStructuredSelection) viewer
		// .getSelection();
		// if (selection.isEmpty())
		// return;
		// String key = (String) selection.toList().get(0);
		// IWorkbenchPage page = PlatformUI.getWorkbench()
		// .getActiveWorkbenchWindow().getActivePage();
		// ViewData viewData = (ViewData) page.findView(ViewData.ID);
		// viewData.update(key);
		// }
		//
		// });
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
				// ... AND BRING UP THE DATA CONTENTS VIEW

				try {
					Util.showView(ViewData.ID);
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		});

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void addFilename(final String path) {
		viewer.add(path);
	}

	public void removeFilename(Object element) {
		if (element == null)
			return;

		viewer.remove(element);
	}

}