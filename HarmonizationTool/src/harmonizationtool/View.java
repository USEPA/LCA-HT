package harmonizationtool;

import harmonizationtool.dialog.MyDialog;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.ModelKeeper;
import harmonizationtool.model.ModelProvider;
import harmonizationtool.query.GenericQuery;
import harmonizationtool.query.GenericUpdate;
import harmonizationtool.query.IdsInfoQuery;
import harmonizationtool.query.IdsRowQuery;
import harmonizationtool.utils.Util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

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

public class View extends ViewPart {
	public static final String ID = "HarmonizationTool.view";

	private TableViewer viewer;
	private Action actionImport;
	private Action actionSave;
	private Action actionClose;
	private Action actionExportToTDB;

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
		manager.add(actionImport);
		manager.add(actionExportToTDB);
		manager.add(actionSave);
		manager.add(actionClose);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

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
		actionImport = new Action() {
			public void run() {
				System.out.println("executing actionImport");
				ModelProvider modelProvider = new ModelProvider();
				FileDialog fileDialog = new FileDialog(getViewSite().getShell(), SWT.OPEN);
				fileDialog.setFilterExtensions(new String[] { "*.csv" });
				String homeDir = System.getProperty("user.home");
				fileDialog.setFilterPath(homeDir);
				String path = fileDialog.open();
				if (path != null) {
					FileReader fileReader = null;
					try {
						fileReader = new FileReader(path);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					if (fileReader != null) {
						CSVParser parser = new CSVParser(fileReader, CSVStrategy.EXCEL_STRATEGY);
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
//							System.out.println(dataRow);
							try {
								values = parser.getLine();
							} catch (IOException e) {
								e.printStackTrace();
							}

						}
					}
					addFilename(path);
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
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
		actionImport.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));

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
				dialog.setFilterNames(filterNames);
				dialog.setFilterExtensions(filterExtensions);
				dialog.setFilterPath(filterPath);
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
				Object obj = ((IStructuredSelection) iSelection).getFirstElement();
				String key = (String) obj;
				ModelKeeper.remove(key);
				removeFilename(obj);
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				ViewData viewData = (ViewData) page.findView(ViewData.ID);
				viewData.clearView(key);
			}
		};
		actionClose.setText("Close");
		actionClose.setToolTipText("Close CSV");
		actionClose.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));

		actionExportToTDB = new Action() {
			public void run() {
				System.out.println("executing actionExportToTDB");
				ISelection iSelection = viewer.getSelection();
				System.out.println("iSelection=" + iSelection);
				if (!iSelection.isEmpty()) {
					Object obj = ((IStructuredSelection) iSelection).getFirstElement();
					String key = (String) obj;
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					ViewData viewData = (ViewData) page.findView(ViewData.ID);
					System.out.println("key=" + key);
					MyDialog dialog = new MyDialog(Display.getCurrent().getActiveShell());
					dialog.create();
					if (dialog.open() == Window.OK) {
						System.out.println(dialog.getDataSourceName());
						System.out.println(dialog.getMajorVersion());
						System.out.println(dialog.getMinorVersion());
						System.out.println(dialog.getComment());
						String dataSourceIRI = dialog.getDataSourceIRI();
						String dataSourceName = dialog.getDataSourceName();
						String majorNumber = dialog.getMajorVersion();
						String minorNumber = dialog.getMinorVersion();
						String comment = dialog.getComment();

						IdsInfoQuery idsInfoQuery = new IdsInfoQuery(dataSourceIRI, dataSourceName, majorNumber, minorNumber, comment);
						List<String> resultList = idsInfoQuery.getData();
//						System.out.println(resultList.toString());
//						System.out.println(idsInfoQuery.getQuery());
						ModelProvider modelProvider = ModelKeeper.getModelProvider(key);
						List<String> headers = modelProvider.getHeaderNames();
						System.out.println(headers.toString());
						List<DataRow> dataRowList = modelProvider.getData();
						int rowNumber = 1;
						System.out.println("dataRowList.size = " + dataRowList.size());
				
						int N = 50000;  // MAYBE SHOULD BE OPTIMIZED FIXME
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

//							CAT_HDR = "Category";
//							SUBCAT_HDR = "Subcategory";
//							IMPACT_CAT_HDR = "Impact_Category";
//							IMPACT_CAT_REF_UNIT_HDR = "Impact_cat_ref_unit";
//							CHAR_FACTOR_HDR = "Characterization_factor";
//							FLOW_UNIT_HDR = "Flow_Unit";
							
							{
								int index = headers.indexOf(ViewData.CASRN_HDR);
								if (index > -1) {
									String unescCasrn = dataRow.getColumnValues().get(index);
									casrn = Util.escape(unescCasrn);
									casrn = casrn.replaceFirst("[^1-9]*(\\d{2,7})-?(\\d\\d)-?(\\d)\\D*$","$1-$2-$3"); // REMOVE LEADING STUFF, 
									// System.out.println("casrn=" + casrn);
//									casrn.replaceFirst(regex, replacement)
									}
							}
							{
								int index = headers.indexOf(ViewData.NAME_HDR);
								if (index > -1) {
									String unescName = dataRow.getColumnValues().get(index);
									name = Util.escape(unescName);
									// System.out.println("name=" + name);
								}
							}
							{
								int index = headers.indexOf(ViewData.ALT_NAME_HDR);
								if (index > -1) {
									String unescAltName = dataRow.getColumnValues().get(index);
									altName = Util.escape(unescAltName);
									// System.out.println("altName=" + altName);
								}
							}
							{
								int index = headers.indexOf(ViewData.CAT_HDR);
								if (index > -1) {
									String unescCat = dataRow.getColumnValues().get(index);
									cat = Util.escape(unescCat);
									// System.out.println("cat=" + cat);
								}
							}
							{
								int index = headers.indexOf(ViewData.SUBCAT_HDR);
								if (index > -1) {
									String unescSubcat = dataRow.getColumnValues().get(index);
									subcat = Util.escape(unescSubcat);
									// System.out.println("subcat=" + subcat);
								}
							}
							{
								int index = headers.indexOf(ViewData.IMPACT_CAT_HDR);
								if (index > -1) {
									String unescImpactCat = dataRow.getColumnValues().get(index);
									impactCat = Util.escape(unescImpactCat);
									// System.out.println("impactCat=" + impactCat);
								}
							}
							{
								int index = headers.indexOf(ViewData.IMPACT_CAT_REF_UNIT_HDR);
								if (index > -1) {
									String unescImpactCatRefUnit = dataRow.getColumnValues().get(index);
									impactCatRefUnit = Util.escape(unescImpactCatRefUnit);
									// System.out.println("impactCatRefUnit=" + impactCatRefUnit);
								}
							}
							{
								int index = headers.indexOf(ViewData.CHAR_FACTOR_HDR);
								if (index > -1) {
									try {
										charFactor = Double.valueOf(dataRow.getColumnValues().get(index));
									} catch (NumberFormatException e) {
										charFactor = 0.0;
//										e.printStackTrace();
									}
									// System.out.println("charFactor=" + charFactor);
								}
							}
							{
								int index = headers.indexOf(ViewData.FLOW_UNIT_HDR);
								if (index > -1) {
									String unescFlowUnit = dataRow.getColumnValues().get(index);
									flowUnit = Util.escape(unescFlowUnit);
									// System.out.println("flowUnit=" + flowUnit);
								}
							}

							IdsRowQuery idsRowQuery = new IdsRowQuery(casrn, dataSourceIRI, name, altName, cat, subcat, impactCat, impactCatRefUnit, charFactor, flowUnit, "" + rowNumber);
							String insertTriples = idsRowQuery.getInsertTriples();
							b.append(insertTriples);
//							b.append(idsRowQuery.toString());

//							if (Integer.valueOf(rowNumber) == 5) {
//								System.out.println("Triples: "+insertTriples);
//							}
							if ((rowNumber % N == 0) || (rowNumber == dataRowList.size())) {
								// add prefix
								String prefix = idsRowQuery.getPrefix();
								prefix += "\n INSERT DATA \n { \n";
								b.insert(0, prefix);
								b.append(" } \n");
								System.out.println(b.toString().substring(0, 5000));
//								GenericQuery iGenericQuery = new GenericQuery(b.toString(), "bundled insert");
								GenericUpdate iGenericUpdate = new GenericUpdate(b.toString(), "bundled insert");
								List<String> results = iGenericUpdate.getData();
//								System.out.println(results.toString());
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
		actionExportToTDB.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));

		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection.isEmpty())
					return;
				String key = (String) selection.toList().get(0);
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
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