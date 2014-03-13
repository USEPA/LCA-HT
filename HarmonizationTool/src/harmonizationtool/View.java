package harmonizationtool;

import harmonizationtool.comands.SelectTDB;

import harmonizationtool.dialog.CSVMetaDialog;
import harmonizationtool.dialog.MyDialog;
import harmonizationtool.dialog.PlayArea;
import harmonizationtool.handler.ShowDataViewHandler;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.DataSetKeeper;
import harmonizationtool.model.DataSetProvider;
import harmonizationtool.model.FileMD;
//import harmonizationtool.model.ModelKeeper;
//import harmonizationtool.model.ModelProvider;
import harmonizationtool.model.TableKeeper;
import harmonizationtool.model.TableProvider;
import harmonizationtool.query.GenericQuery;
import harmonizationtool.query.GenericUpdate;
import harmonizationtool.query.IdsInfoQuery;
import harmonizationtool.query.IdsRowQuery;
import harmonizationtool.query.QueryResults;
import harmonizationtool.utils.DataSetMap;
import harmonizationtool.utils.ResourceIdMgr;
import harmonizationtool.utils.Util;
import harmonizationtool.query.unused.ZGetNextDSIndex;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.ETHOLD;
import harmonizationtool.vocabulary.FASC;
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
	private Action actionParseImpactAssessmentModelInfoToTDB;

	private ZGetNextDSIndex qGetNextDSIndex = new ZGetNextDSIndex();

	/**
	 * The content provider class is responsible for providing objects to the view. It can wrap
	 * existing objects in adapters or simply return objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore it and always show the same content (like Task
	 * List, for example).
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

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
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
		manager.add(actionParseImpactAssessmentModelInfoToTDB);
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
		System.out.println("Line " + lineNumber + " has " + as.length + " values:");
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

		editMeta = new Action() {
			public void run() {
				System.out.println("edit Meta Data");

				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();

				if (selection.isEmpty()) {
					return;
				}
				System.out.println(selection.getFirstElement().toString());
				FileMD fileMD = (FileMD) selection.toList().get(0);
				DataSetProvider dataSetProvider = DataSetKeeper.get(fileMD);
				// String key = (String) selection.toList().get(0);
				// DataSetProvider dataSetProvider = DataSetKeeper.get(0); //
				// FIXME
				// Map<String, String> metaData = csvFile.metaData;
				CSVMetaDialog dialog = new CSVMetaDialog(Display.getCurrent().getActiveShell(), fileMD, dataSetProvider);
				dialog.create();
				dialog.open();
			}
		};
		editMeta.setText("Edit Meta Data");
		editMeta.setToolTipText("See / Change data for this file");
		editMeta.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
		actionSave = new Action() {
			public void run() {
				System.out.println("executing actionSave");
				ISelection iSelection = viewer.getSelection();
				Object obj = ((IStructuredSelection) iSelection).getFirstElement();
				System.out.println("saving file: " + obj);
				Shell shell = getViewSite().getShell();
				FileDialog dialog = new FileDialog(shell, SWT.SAVE);
				String[] filterNames = new String[] { "Image Files", "All Files (*)" };
				String[] filterExtensions = new String[] { "*.csv", "*" };
				String filterPath = "/";
				String platform = SWT.getPlatform();

				if (platform.equals("win32") || platform.equals("wpf")) {
					filterNames = new String[] { "Image Files", "All Files (*.*)" };
					filterExtensions = new String[] { "*.gif;*.png;*.bmp;*.jpg;*.jpeg;*.tiff", "*.*" };
					filterPath = "c:\\";
				}

				String workingDir = Util.getPreferenceStore().getString("workingDir");
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
		actionSave.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));

		actionClose = new Action() {
			public void run() {
				System.out.println("executing actionClose");
				ISelection iSelection = viewer.getSelection();
				if (iSelection.isEmpty()) {
					return;
				}
				FileMD fileMD = (FileMD) ((IStructuredSelection) iSelection).getFirstElement();
				remove(fileMD);
				DataSetProvider dataSetProvider = DataSetKeeper.get(fileMD);
				dataSetProvider.remove(fileMD);

				// clear data from data view
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				ViewData viewData = (ViewData) page.findView(ViewData.ID);
				viewData.clearView(fileMD.getPath());
			}
		};
		actionClose.setText("Close");
		actionClose.setToolTipText("Close CSV");
		actionClose.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));

		//	NOTES:
		//  LCIA must have the following:
		//  Usually 1 Impact Assessment Method
		//  Usually 1 Impact Characterization Model
		//  Usually 1 to 10 or so Impact Categories
		//          each with an Impact Category Indicator
		//      and each with a Reference Unit (which should have Flow Context)
		//      and each Impact Category will have multiple
		//          multiple Impact Characterizations each with:
		//          one Characterization factor (a magnitude and unit)
		//          one Flowable
		//          one Flow Context which may have:
		//          one or more Compartments or Categories
		
		actionParseFlowablesToTDB = new Action() {
			public void run() {
				System.out.println("executing actionParseSubsToTDB");
				ISelection iSelection = viewer.getSelection();
				System.out.println("iSelection=" + iSelection);
				if (!iSelection.isEmpty()) {

					FileMD fileMD = (FileMD) ((IStructuredSelection) iSelection).getFirstElement();
					String key = fileMD.getPath();
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					ViewData viewData = (ViewData) page.findView(ViewData.ID);
					System.out.println("key=" + key);

					Model model = SelectTDB.model;
					if (model == null) {
						String msg = "ERROR no TDB open";
						Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
						return;
					}

					System.out.println("Running ExportSubsToTDB internals");

					String afn_p = "http://jena.hpl.hp.com/ARQ/function#";
					String fn_p = "http://www.w3.org/2005/xpath-functions#";
					String nfo_p = "http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#";
					String sumo_p = "http://www.ontologyportal.org/SUMO.owl.rdf#";
					String xml_p = "http://www.w3.org/XML/1998/namespace";

					// THE FOLLOWING ARE IN Vocabulary
					// skos
					// owl
					// rdf
					// rdfs
					// xsd
					// ethold
					// eco

					// Stuff we may want to add:
					// Property HTuserName = model.getProperty(ethold_p
					// + "HTuserName");
					// Property HTuserAffiliation = model.getProperty(ethold_p
					// + "HTuserAffiliation");
					// Property HTuserPhone = model.getProperty(ethold_p
					// + "HTuserPhone");
					// Property HTuserEmail = model.getProperty(ethold_p
					// + "HTuserEmail");
					// Property dataParseTimeStamp = model
					// .getProperty(ethold_p + "dataParseTimeStamp");

					// Dataset dataset = SelectTDB.dataset;
					// GraphStore graphStore = SelectTDB.graphStore;
					DataRow columnHeaders = new DataRow();
					// queryResults.setColumnHeaders(columnHeaders);

					long change = model.size();

					columnHeaders.add("Model");
					columnHeaders.add("Size");

					System.err.printf("Before Update: %s\n", model.size());

					TableProvider resultsViewModel = new TableProvider();
					// queryResults.setModelProvider(modelProvider);
					DataRow resDataRow = new DataRow();
					resultsViewModel.addDataRow(resDataRow);
					resDataRow.add("Before Update");
					resDataRow.add("" + model.size());

					// ModelProvider modelProvider = ModelKeeper
					// .getModelProvider(key);
					TableProvider tableProvider = TableKeeper.getTableProvider(key);
					List<String> headers = tableProvider.getHeaderNamesAsStrings();
					System.out.println(headers.toString());
					List<DataRow> dataRowList = tableProvider.getData();
					System.out.println("dataRowList.size = " + dataRowList.size());

					Resource tdbResource = DataSetKeeper.get(fileMD).getTdbResource();

					Hashtable<String, Resource> str2res = new Hashtable<String, Resource>();

					System.out.println("Ready to iterate...");
					int csvRow = 0;
					for (DataRow csvDataRow : dataRowList) {
						if (csvRow % 10000 == 0) {
							System.out.println("Finished reading data file row: " + csvRow);
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
								String unescName = csvDataRow.getColumnValues().get(index);
								name = Util.escape(unescName);
								// System.out.println("name=" + name);
								drNameLit = model.createTypedLiteral(name);
							} else {
								String msg = "Flowables must have a \"Name\" field!";
								Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
								return; // FIXME -- IS THERE A "RIGHT" WAY
										// TO LEAVE

							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						try {
							int index = headers.indexOf(ViewData.ALT_NAME_HDR);
							if (index > -1) {
								String unescAltName = csvDataRow.getColumnValues().get(index);
								altName = Util.escape(unescAltName);
								// System.out.println("altName=" +
								// altName);
								drAltNameLit = model.createTypedLiteral(altName);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						try {
							int index = headers.indexOf(ViewData.CASRN_HDR);
							if (index > -1) {
								String unescCasrn = csvDataRow.getColumnValues().get(index);
								casrn = Util.escape(unescCasrn);
								casrn = casrn.replaceFirst("[^1-9]*(\\d{2,7})-?(\\d\\d)-?(\\d)\\D*$", "$1-$2-$3"); // REMOVE
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
								model.addLiteral(newSub, SKOS.altLabel, drAltNameLit);
							}
							if (casrn != null && casrn.length() > 0) {
								model.addLiteral(newSub, ECO.casNumber, drCasLit);
								// newSub.addLiteral(casNumber, drCasLit);
							}
							model.add(newSub, ECO.hasDataSource, tdbResource);
							// newSub.addProperty(hasDataSource,
							// tdbResource);
							subResourceHandle = newSub;
							str2res.put(combined_str, subResourceHandle);
						}
						subResourceHandle.addLiteral(ETHOLD.foundOnRow, drRowLit);
						subResourceHandle.addLiteral(ETHOLD.sourceTableRowNumber, drRowLit);

						csvRow++;
					}
					// -----------------------------------------
					DataRow resDataRow2 = new DataRow();
					resultsViewModel.addDataRow(resDataRow2);
					resDataRow2.add("After Update");
					resDataRow2.add("" + model.size());

					change = model.size() - change;
					System.err.printf("Net Increase: %s\n", change);
					DataRow resDataRow3 = new DataRow();
					resultsViewModel.addDataRow(resDataRow3);

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

		};
		actionParseFlowablesToTDB.setText("Parse Flowables");
		actionParseFlowablesToTDB.setToolTipText("Parse flowables to TDB");
		actionParseFlowablesToTDB.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));

		actionParseCategoriesToTDB = new Action() {
			public void run() {
				System.out.println("executing actionParseCategoriesToTDB");
				ISelection iSelection = viewer.getSelection();
				System.out.println("iSelection=" + iSelection);
				if (!iSelection.isEmpty()) {

					FileMD fileMD = (FileMD) ((IStructuredSelection) iSelection).getFirstElement();
					String key = fileMD.getPath();
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					ViewData viewData = (ViewData) page.findView(ViewData.ID);
					System.out.println("key=" + key);

					Model model = SelectTDB.model;
					if (model == null) {
						String msg = "ERROR no TDB open";
						Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
						return;
					}

					System.out.println("Running ParseCategoriesToTDB internals");

					String afn_p = "http://jena.hpl.hp.com/ARQ/function#";
					String fn_p = "http://www.w3.org/2005/xpath-functions#";
					String nfo_p = "http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#";
					String sumo_p = "http://www.ontologyportal.org/SUMO.owl.rdf#";
					String xml_p = "http://www.w3.org/XML/1998/namespace";

					// THE FOLLOWING ARE IN Vocabulary
					// skos
					// owl
					// rdf
					// rdfs
					// xsd
					// ethold
					// eco
					// fasc

					// Stuff we may want to add:
					// Property HTuserName = model.getProperty(ethold_p
					// + "HTuserName");
					// Property HTuserAffiliation = model.getProperty(ethold_p
					// + "HTuserAffiliation");
					// Property HTuserPhone = model.getProperty(ethold_p
					// + "HTuserPhone");
					// Property HTuserEmail = model.getProperty(ethold_p
					// + "HTuserEmail");
					// Property dataParseTimeStamp = model
					// .getProperty(ethold_p + "dataParseTimeStamp");

					// Dataset dataset = SelectTDB.dataset;
					// GraphStore graphStore = SelectTDB.graphStore;
					DataRow columnHeaders = new DataRow();
					// queryResults.setColumnHeaders(columnHeaders);

					long change = model.size();

					columnHeaders.add("Model");
					columnHeaders.add("Size");

					System.err.printf("Before Update: %s\n", model.size());

					TableProvider resultsViewModel = new TableProvider();
					// queryResults.setModelProvider(modelProvider);
					DataRow resDataRow = new DataRow();
					resultsViewModel.addDataRow(resDataRow);
					resDataRow.add("Before Update");
					resDataRow.add("" + model.size());

					// ModelProvider modelProvider = ModelKeeper
					// .getModelProvider(key);
					TableProvider tableProvider = TableKeeper.getTableProvider(key);
					List<String> headers = tableProvider.getHeaderNamesAsStrings();
					System.out.println(headers.toString());
					List<DataRow> dataRowList = tableProvider.getData();
					System.out.println("dataRowList.size = " + dataRowList.size());

					Resource tdbResource = DataSetKeeper.get(fileMD).getTdbResource();

					Hashtable<String, Resource> str2res = new Hashtable<String, Resource>();

					System.out.println("Ready to iterate...");
					int csvRow = 0;
					for (DataRow csvDataRow : dataRowList) {
						if (csvRow % 10000 == 0) {
							System.out.println("Finished reading data file row: " + csvRow);
						}

						Literal drRowLit = model.createTypedLiteral(csvRow);

						String cat1 = null; // REQUIRED
						String cat2 = null; // OPTIONAL
						String cat3 = null; // OPTIONAL

						try {
							int index = headers.indexOf(ViewData.CAT1_HDR);
							if (index > -1) {
								String unescCat1 = csvDataRow.getColumnValues().get(index);
								cat1 = Util.escape(unescCat1);
							}

							else {
								String msg = "Categories must have a \"Cat1\" field!";
								Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
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
								int index = headers.indexOf(ViewData.CAT2_HDR);
								if (index > -1) {
									String unescCat2 = csvDataRow.getColumnValues().get(index);
									cat2 = Util.escape(unescCat2);
								}
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							{
								int index = headers.indexOf(ViewData.CAT3_HDR);
								if (index > -1) {
									String unescCat3 = csvDataRow.getColumnValues().get(index);
									cat3 = Util.escape(unescCat3);
								}
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						Resource catResourceHandle = null;

						String combined_str = "";
						if (cat3 != null) {
							combined_str = cat1 + "; " + cat2 + "; " + cat3;
						} else if (cat2 != null) {
							combined_str = cat1 + "; " + cat2;
						} else {
							combined_str = cat1;
						}

						Literal drCatLit = model.createTypedLiteral(combined_str);

						combined_str.hashCode();
						if (str2res.containsKey(combined_str)) {
							catResourceHandle = str2res.get(combined_str);
						} else {
							Resource newCat = model.createResource();
							model.add(newCat, RDF.type, FASC.Compartment);
							model.addLiteral(newCat, RDFS.label, drCatLit);
							model.add(newCat, ECO.hasDataSource, tdbResource);

							catResourceHandle = newCat;
							str2res.put(combined_str, catResourceHandle);
						}
						catResourceHandle.addLiteral(ETHOLD.foundOnRow, drRowLit);
						catResourceHandle.addLiteral(ETHOLD.sourceTableRowNumber, drRowLit);						
						csvRow++;
					}
					// -----------------------------------------
					DataRow resDataRow2 = new DataRow();
					resultsViewModel.addDataRow(resDataRow2);
					resDataRow2.add("After Update");
					resDataRow2.add("" + model.size());

					change = model.size() - change;
					System.err.printf("Net Increase: %s\n", change);
					DataRow resDataRow3 = new DataRow();
					resultsViewModel.addDataRow(resDataRow3);

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

		};
		actionParseCategoriesToTDB.setText("Parse Categories");
		actionParseCategoriesToTDB.setToolTipText("Parse Categories to TDB");
		actionParseCategoriesToTDB.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));

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
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection.isEmpty())
					return;
				FileMD fileMD = (FileMD) selection.toList().get(0);
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				ViewData viewData = (ViewData) page.findView(ViewData.ID);
				viewData.update(fileMD.getPath());
				// ... AND BRING UP THE DATA CONTENTS VIEW

				try {
					Util.showView(ViewData.ID);
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		});

	

	actionParseImpactAssessmentModelInfoToTDB = new Action() {
		public void run() {
			System.out.println("executing actionParseImpactAssessmentModelInfoToTDB");
			ISelection iSelection = viewer.getSelection();
			System.out.println("iSelection=" + iSelection);
			if (!iSelection.isEmpty()) {

				FileMD fileMD = (FileMD) ((IStructuredSelection) iSelection).getFirstElement();
				String key = fileMD.getPath();
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				ViewData viewData = (ViewData) page.findView(ViewData.ID);
				System.out.println("key=" + key);

				Model model = SelectTDB.model;
				if (model == null) {
					String msg = "ERROR no TDB open";
					Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
					return;
				}

				System.out.println("Running ParseImpactAssessmentModelInfoToTDB internals");

				String afn_p = "http://jena.hpl.hp.com/ARQ/function#";
				String fn_p = "http://www.w3.org/2005/xpath-functions#";
				String nfo_p = "http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#";
				String sumo_p = "http://www.ontologyportal.org/SUMO.owl.rdf#";
				String xml_p = "http://www.w3.org/XML/1998/namespace";

				// THE FOLLOWING ARE IN Vocabulary
				// skos
				// owl
				// rdf
				// rdfs
				// xsd
				// ethold
				// eco
				// fasc

				// Stuff we may want to add:
				// Property HTuserName = model.getProperty(ethold_p
				// + "HTuserName");
				// Property HTuserAffiliation = model.getProperty(ethold_p
				// + "HTuserAffiliation");
				// Property HTuserPhone = model.getProperty(ethold_p
				// + "HTuserPhone");
				// Property HTuserEmail = model.getProperty(ethold_p
				// + "HTuserEmail");
				// Property dataParseTimeStamp = model
				// .getProperty(ethold_p + "dataParseTimeStamp");

				// Dataset dataset = SelectTDB.dataset;
				// GraphStore graphStore = SelectTDB.graphStore;
				DataRow columnHeaders = new DataRow();
				// queryResults.setColumnHeaders(columnHeaders);

				long change = model.size();

				columnHeaders.add("Model");
				columnHeaders.add("Size");

				System.err.printf("Before Update: %s\n", model.size());

				TableProvider resultsViewModel = new TableProvider();
				// queryResults.setModelProvider(modelProvider);
				DataRow resDataRow = new DataRow();
				resultsViewModel.addDataRow(resDataRow);
				resDataRow.add("Before Update");
				resDataRow.add("" + model.size());

				// ModelProvider modelProvider = ModelKeeper
				// .getModelProvider(key);
				TableProvider tableProvider = TableKeeper.getTableProvider(key);
				List<String> headers = tableProvider.getHeaderNamesAsStrings();
				System.out.println(headers.toString());
				List<DataRow> dataRowList = tableProvider.getData();
				System.out.println("dataRowList.size = " + dataRowList.size());

				Resource tdbResource = DataSetKeeper.get(fileMD).getTdbResource();

				Hashtable<String, Resource> str2res = new Hashtable<String, Resource>();

				System.out.println("Ready to iterate...");
				int csvRow = 0;
				for (DataRow csvDataRow : dataRowList) {
					if (csvRow % 10000 == 0) {
						System.out.println("Finished reading data file row: " + csvRow);
					}

					Literal drRowLit = model.createTypedLiteral(csvRow);

					String impactAssessmentMethod = null;  // REQUIRED
					String impactCharModel = null;         // REQUIRED
					String impactCategory = null;          // REQUIRED
					String impactCategoryIndicator = null; // REQUIRED (?!?)
					String referenceUnit = null;           // REQUIRED

				
					System.out.println("Ready to find header info 0");
					try {
						int index = headers.indexOf(ViewData.IMPACT_ASSESSMENT_METHOD_HDR);
						if (index > -1) {
							String unescImpactAssessmentMethod = csvDataRow.getColumnValues().get(index);
							impactAssessmentMethod = Util.escape(unescImpactAssessmentMethod);
						}

						else {
							String msg = "An impact assessment method must be assigned";
							Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
							return; // FIXME -- IS THERE A "RIGHT"
									// WAY
									// TO LEAVE

						}
					} catch (Exception e) {
						System.out.println("Failed...");
						e.printStackTrace();
					}

					System.out.println("Ready to find header info 1");

					try {
						int index = headers.indexOf(ViewData.IMPACT_CHARACTERIZATION_MODEL_HDR);
						if (index > -1) {
							String unescImpactCharModel = csvDataRow.getColumnValues().get(index);
							impactCharModel = Util.escape(unescImpactCharModel);
						}

						else {
							String msg = "An impact characterization model must be assigned";
							Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
							return; // FIXME -- IS THERE A "RIGHT"
									// WAY
									// TO LEAVE

						}
					} catch (Exception e) {
						System.out.println("Failed...");
						e.printStackTrace();
					}

					System.out.println("Ready to find header info 2");

					try {
						int index = headers.indexOf(ViewData.IMPACT_CAT_HDR);
						if (index > -1) {
							String unescImpactCategory = csvDataRow.getColumnValues().get(index);
							impactCategory = Util.escape(unescImpactCategory);
						}

						else {
							String msg = "An impact category must be assigned";
							Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
							return; // FIXME -- IS THERE A "RIGHT"
									// WAY
									// TO LEAVE

						}
					} catch (Exception e) {
						System.out.println("Failed...");
						e.printStackTrace();
					}
					
					System.out.println("Ready to find header info 3");
					
					try {
						int index = headers.indexOf(ViewData.IMPACT_CAT_INDICATOR_HDR);
						if (index > -1) {
							String unescImpactCategoryIndicator = csvDataRow.getColumnValues().get(index);
							impactCategoryIndicator = Util.escape(unescImpactCategoryIndicator);
						}

						else {
							String msg = "An impact category indicator must be assigned";
							Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
							return; // FIXME -- IS THERE A "RIGHT"
									// WAY
									// TO LEAVE

						}
					} catch (Exception e) {
						System.out.println("Failed...");
						e.printStackTrace();
					}
					
					System.out.println("Ready to find header info 4");

					try {
						int index = headers.indexOf(ViewData.IMPACT_CAT_REF_UNIT_HDR);
						if (index > -1) {
							String unescReferenceUnit = csvDataRow.getColumnValues().get(index);
							referenceUnit = Util.escape(unescReferenceUnit);
						}

						else {
							String msg = "An reference unit must be assigned";
							Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
							return; // FIXME -- IS THERE A "RIGHT"
									// WAY
									// TO LEAVE

						}
					} catch (Exception e) {
						System.out.println("Failed...");
						e.printStackTrace();
					}

					System.out.println("Now ready to use header info");

					Resource catResourceHandle = null;

//					String combined_str = "";
//					if (cat3 != null) {
//						combined_str = cat1 + "; " + cat2 + "; " + cat3;
//					} else if (cat2 != null) {
//						combined_str = cat1 + "; " + cat2;
//					} else {
//						combined_str = cat1;
//					}
//
//					Literal drCatLit = model.createTypedLiteral(combined_str);
//
//					combined_str.hashCode();
//					if (str2res.containsKey(combined_str)) {
//						catResourceHandle = str2res.get(combined_str);
//					} else {
//						Resource newCat = model.createResource();
//						model.add(newCat, RDF.type, FASC.Compartment);
//						model.addLiteral(newCat, RDFS.label, drCatLit);
//						model.add(newCat, ECO.hasDataSource, tdbResource);
//
//						catResourceHandle = newCat;
//						str2res.put(combined_str, catResourceHandle);
//					}
//					catResourceHandle.addLiteral(ETHOLD.foundOnRow, drRowLit);
//					catResourceHandle.addLiteral(ETHOLD.sourceTableRowNumber, drRowLit);						
//					csvRow++;
//				}
//				// -----------------------------------------
//				DataRow resDataRow2 = new DataRow();
//				resultsViewModel.addDataRow(resDataRow2);
//				resDataRow2.add("After Update");
//				resDataRow2.add("" + model.size());
//
//				change = model.size() - change;
//				System.err.printf("Net Increase: %s\n", change);
//				DataRow resDataRow3 = new DataRow();
//				resultsViewModel.addDataRow(resDataRow3);
//
//				String increase = "New Triples:";
//
//				if (change < 0) {
//					increase = "Triples removed:";
//					change = 0 - change;
//				}
//				// data.add(increase);
//				// data.add("" + change);
//				resDataRow3.add(increase);
//				resDataRow3.add("" + change);
//
//				long startTime = System.currentTimeMillis();
//				float elapsedTimeSec = (System.currentTimeMillis() - startTime) / 1000F;
//				System.out.println("Time elapsed: " + elapsedTimeSec);
//				System.err.printf("After Update: %s\n", model.size());
//				System.out.println("done");
//				try {
//					Util.showView(ResultsView.ID);
//				} catch (PartInitException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
				}

			}

		}

	};
	actionParseImpactAssessmentModelInfoToTDB.setText("Parse Other Model Info");
	actionParseImpactAssessmentModelInfoToTDB.setToolTipText("Set Impact Characterization Model, Impact Category, Impact Category Indicator, and Flow Uni).");
	actionParseImpactAssessmentModelInfoToTDB.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));

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
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			if (selection.isEmpty())
				return;
			FileMD fileMD = (FileMD) selection.toList().get(0);
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			ViewData viewData = (ViewData) page.findView(ViewData.ID);
			viewData.update(fileMD.getPath());
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

	public void add(final FileMD fileMD) {
		viewer.add(fileMD);
	}

	public void remove(final FileMD fileMD) {
		assert fileMD != null : "fileMD cannot be null";
		viewer.remove(fileMD);
	}

	// public void removeFilename(Object element) {
	// if (element == null)
	// return;
	//
	// viewer.remove(element);
	// }

}