package harmonizationtool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;

import harmonizationtool.View.ViewContentProvider;
import harmonizationtool.View.ViewLabelProvider;
import harmonizationtool.comands.ISelectedTDBListener;
import harmonizationtool.comands.SelectTDB;
import harmonizationtool.dialog.DialogQueryDataset;
import harmonizationtool.dialog.MyDialog;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.ModelKeeper;
import harmonizationtool.model.ModelProvider;
import harmonizationtool.query.QDataSourcesSubCountB;
import harmonizationtool.query.QMatchCAS;
import harmonizationtool.query.QCountMatches;
//import harmonizationtool.query.QMatchNameNotCAS;
import harmonizationtool.query.IParamQuery;
import harmonizationtool.query.QCasNotInDB;
import harmonizationtool.query.UDelTestData;
import harmonizationtool.query.QDataSources;
import harmonizationtool.query.GenericUpdate;
import harmonizationtool.query.GenericQuery;
import harmonizationtool.query.QDataSourcesSubCount;
import harmonizationtool.query.ZunusedGCasNameSourceQuery;
import harmonizationtool.query.HarmonyQuery;
import harmonizationtool.query.UAdTestData;
import harmonizationtool.query.ZunusedNonSubstanceQuery;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVStrategy;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
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
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class QueryView extends ViewPart implements ISelectedTDBListener {
	public static final String ID = "HarmonizationTool.QueryViewID";

	private TableViewer viewer;
	private Action actionImport;
	private Action actionSave;
	private Action actionClose;
	// private Action actionExtQuery;
	// private Action actionExtUpdate;

	private QDataSources qDataSources = new QDataSources();
	private QDataSourcesSubCount qDataSourcesSubCount = new QDataSourcesSubCount();
	private QDataSourcesSubCountB qDataSourcesSubCountB = new QDataSourcesSubCountB();
	private UAdTestData uAdTestData = new UAdTestData();
	private UDelTestData uDelTestData = new UDelTestData();

	private QCountMatches qCountMatches = new QCountMatches();
	private QMatchCAS qMatchCAS = new QMatchCAS();
	private QCasNotInDB qCasNotInDB = new QCasNotInDB();
	// private QMatchNameNotCAS qMatchNameNotCAS = new QMatchNameNotCAS();

	private Map<String, HarmonyQuery> queryMap = new HashMap<String, HarmonyQuery>();
	private List<String> paramQueries = new ArrayList<String>();

	public QueryView() {
		paramQueries.add("Show CAS Matches");
		paramQueries.add("Count CAS matches");
		paramQueries.add("Show CAS not in DB");
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		viewer.setContentProvider(new QueryViewContentProvider(viewer));
		viewer.setLabelProvider(new QueryViewLabelProvider());
		viewer.setInput(getViewSite());

		makeActions();
		hookContextMenu();
		addQuery(qDataSources);
		// addQuery(qDataSourcesSubCount);
		addQuery(qDataSourcesSubCountB);
		// addQuery(uAdTestData);
		// addQuery(uDelTestData);
		addQuery(qMatchCAS);
		addQuery(qCountMatches);
		addQuery(qCasNotInDB);

		// ICommandService commandService = (ICommandService) PlatformUI
		// .getWorkbench().getActiveWorkbenchWindow()
		// .getService(ICommandService.class);
		// Command command = commandService
		// .getCommand("harmonizationtool.tdb.select.id");
		// IHandler handler = command.getHandler();
		// System.out.println(handler.getClass().getName());
		// handler.addHandlerListener(this);
		SelectTDB.getInstance().addSelectedTDBListener(this);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */
	public class QueryViewContentProvider implements IStructuredContentProvider {
		Viewer v;

		public QueryViewContentProvider(Viewer v) {
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

	public class QueryViewLabelProvider extends LabelProvider implements
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

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				QueryView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);

	}

	private void fillContextMenu(IMenuManager manager) {
		// manager.add(actionImport);
		// manager.add(actionSave);
		// manager.add(actionClose);
		// manager.add(actionExtQuery);
		// manager.add(actionExtUpdate);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

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
					// ViewData.setKey(path);
					// TableViewer tableViewer = viewData.getViewer();
					// tableViewer.setInput(new Object[] {""});
					viewData.update(path);
				}
				CSVParser c = null;
				// addFilename("filename");
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

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				System.out.println("Double click action");
				IStructuredSelection selection = (IStructuredSelection) viewer
						.getSelection();
				if (selection.isEmpty())
					return;
				String key = (String) selection.toList().get(0);
				System.out.println("key=" + key);

				if (paramQueries.contains(key)) {

					DialogQueryDataset dialog = new DialogQueryDataset(Display
							.getCurrent().getActiveShell());
					dialog.create();
					if (dialog.open() == Window.OK) {
						System.out.println("OK");
						String primaryDataset = dialog.getPrimaryDataset();
						String[] refDatasets = dialog.getReferenceDatasets();
						System.out.println(primaryDataset);
						for (String s : refDatasets) {
							System.out.println(s);
						}
						// do query
						HarmonyQuery q = queryMap.get(key);
						if (q instanceof IParamQuery) {
							System.out.println(" is instanceof IParamQuery");
							IParamQuery paramQuery = (IParamQuery) q;
							paramQuery.setPrimaryDatset(primaryDataset);
							paramQuery.setRefDatasets(refDatasets);
							System.out.println(q.getQuery());

							IWorkbenchPage page = PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getActivePage();
							ResultsView resultsView = (ResultsView) page
									.findView(ResultsView.ID);
							// resultsView.update(q);

							resultsView.update(q.getData());
							resultsView.update(q.getQueryResults());

							System.out.println("done");
						}
					}

				} else {

					HarmonyQuery q = queryMap.get(key);
					System.out.println(q.getQuery());

					IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					ResultsView resultsView = (ResultsView) page
							.findView(ResultsView.ID);
					// resultsView.update(q);

					resultsView.update(q.getData());
					resultsView.update(q.getQueryResults());

					System.out.println("done");
				}
			}

		});

	}

	public void addFilename(final String url) {
		viewer.add(url);
	}

	public void addQuery(HarmonyQuery query) {
		viewer.add(query.getLabel());
		queryMap.put(query.getLabel(), query);
	}

	public void removeFilename(Object element) {
		if (element == null)
			return;

		viewer.remove(element);
	}

	@Override
	public void TDBchanged(String tdb) {
		System.out.println("new TDB = " + tdb);
		// String key = (String) selection.toList().get(0);
		String key = "Show Data Sources";
		System.out.println("key=" + key);
		HarmonyQuery q = queryMap.get(key);
		System.out.println(q.getQuery());

		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		ResultsView resultsView = (ResultsView) page.findView(ResultsView.ID);
		// resultsView.update(q);

		resultsView.update(q.getData());
		resultsView.update(q.getQueryResults());
		System.out.println("done");

	}

	// @Override
	// public void handlerChanged(HandlerEvent handlerEvent) {
	// System.out.println("query event handled");
	// IHandler handler = handlerEvent.getHandler();
	// if( handler instanceof SelectTDB){
	// System.out.println("tdbDir+"+((SelectTDB)handler).tdbDir);
	// }
	// }

}
