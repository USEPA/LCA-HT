package harmonizationtool;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import harmonizationtool.model.TableProvider;
import harmonizationtool.query.GenericQuery;
import harmonizationtool.query.GenericUpdate;
import harmonizationtool.query.HSubsSameCas;
import harmonizationtool.query.HarmonyLabeledQuery;
import harmonizationtool.query.HarmonyQuery2Impl;
import harmonizationtool.query.HarmonyUpdate;
import harmonizationtool.query.IParamHarmonize;
import harmonizationtool.query.QDataSetContents;

import harmonizationtool.query.QMatchCAS;
import harmonizationtool.query.QMatchCASandName;
import harmonizationtool.query.UDelDataSet;

import harmonizationtool.query.QCountMatches;
import harmonizationtool.query.IParamQuery;
import harmonizationtool.query.QxCasNotInDB;
import harmonizationtool.query.QDataSources;

import harmonizationtool.query.HarmonyQuery;
import harmonizationtool.utils.Util;

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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.RDFNode;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Table;

public class QueryView extends ViewPart implements ISelectedTDBListener {
	public static final String ID = "HarmonizationTool.QueryViewID";
	
	private List<HarmonyLabeledQuery> labeledQueries = new ArrayList<HarmonyLabeledQuery>();

	private TableViewer viewer;

//	private QDataSources qDataSources = new QDataSources();
//	private QDataSetContents qDataSetContents = new QDataSetContents();
//
//	private QCountMatches qCountMatches = new QCountMatches();
//	private QMatchCAS qMatchCAS = new QMatchCAS();
//	private QMatchCASandName qMatchCASandName = new QMatchCASandName();
//	private HSubsSameCas hSubsSameCas = new HSubsSameCas();
	
	private UDelDataSet uDelDataSet = new UDelDataSet();

	private Map<String, HarmonyQuery> queryMap = new HashMap<String, HarmonyQuery>();
	private Map<String, HarmonyUpdate> updateMap = new HashMap<String, HarmonyUpdate>();
//	private List<String> paramQueries = new ArrayList<String>();
	private List<String> paramUpdates = new ArrayList<String>();

	private Text windowQueryUpdate;

	private void createLabeledQueries(){
		labeledQueries.add(new QDataSources());
		labeledQueries.add(new QDataSetContents());
		labeledQueries.add(new QCountMatches());
		labeledQueries.add(new QMatchCAS());
		labeledQueries.add(new QMatchCASandName());
		labeledQueries.add(new HSubsSameCas());
	}
	
	private HarmonyLabeledQuery queryFromKey(String key){
		try {
			for (HarmonyLabeledQuery harmonyLabeledQuery: labeledQueries){
				if (harmonyLabeledQuery.getLabel().equals(key)){
					return harmonyLabeledQuery;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public QueryView() {
//		paramQueries.add("Show CAS Matches");         // FIXME, SHOULD GET THE KEY FROM THE QUERY FILE
//		paramQueries.add("Show CAS + Name Matches");  // FIXME, SHOULD GET THE KEY FROM THE QUERY FILE
//		paramQueries.add("Count CAS matches");        // FIXME, SHOULD GET THE KEY FROM THE QUERY FILE
//		
//		paramQueries.add("Harmonize Subs Same CAS");  // FIXME, SHOULD GET THE KEY FROM THE QUERY FILE

		// paramQueries.add("Show CAS not in DB");
		paramUpdates.add("Delete data set...");       // FIXME, SHOULD GET THE KEY FROM THE QUERY FILE
	    createLabeledQueries();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(null);

		Device device = Display.getCurrent();

		Button queryButton = new Button(parent, SWT.BORDER);
		// btnNewButton.setBounds(149, 0, 148, 469);
		queryButton.setBounds(20, 150, 100, 30);
		queryButton.setAlignment(SWT.LEFT);
		queryButton.setText("Run Query");
		queryButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// txtTextArea.setText("new text");
				String queryStr = windowQueryUpdate.getText();
				GenericQuery iGenericQuery = new GenericQuery(queryStr,
						"Ext. File Query");

				// addFilename(path);
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				ResultsView resultsView = (ResultsView) page
						.findView(ResultsView.ID);
				String title = resultsView.getTitle();
				System.out.println("title= " + title);

				resultsView.update(iGenericQuery.getData());
				resultsView.update(iGenericQuery.getQueryResults());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		Button updateButton = new Button(parent, SWT.BORDER);
		updateButton.setForeground(SWTResourceManager
				.getColor(SWT.COLOR_DARK_RED));
		updateButton.setBounds(20, 190, 100, 30);
		updateButton.setAlignment(SWT.LEFT);
		// updateButton.setBackground(new Color(device,255,200,200)); // DOES
		// NOT WORK IN WINDOWS
		updateButton.setText("Run Update");
		updateButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// txtTextArea.setText("new text");
				String updateStr = windowQueryUpdate.getText();
				GenericUpdate iGenericUpdate = new GenericUpdate(updateStr, "Update from window");

				// addFilename(path);
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				ResultsView resultsView = (ResultsView) page
						.findView(ResultsView.ID);
				String title = resultsView.getTitle();
				System.out.println("title= " + title);

				resultsView.update(iGenericUpdate.getData());
				resultsView.update(iGenericUpdate.getQueryResults());

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		windowQueryUpdate = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		windowQueryUpdate
				.setToolTipText("Load, type, or cut and paste a query here.  Then hit \"Run Query\"");
		// txtTextArea.setBounds(297, 0, 148, 469);
		windowQueryUpdate.setBounds(150, 0, 600, 500);

		// Color queryWindowColor = new Color(device, 255, 255, 200);
		windowQueryUpdate.setBackground(new Color(device, 255, 255, 200));
		windowQueryUpdate.setText("(query / update editor)");
		// parent.setLayout(null);
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		Table table = viewer.getTable();
		// table.setBounds(445, 0, 149, 469);
		table.setBounds(0, 0, 150, 500);
		viewer.setContentProvider(new QueryViewContentProvider(viewer));

		// queryWindow.append("Query Window");
		viewer.setLabelProvider(new QueryViewLabelProvider());
		viewer.setInput(getViewSite());
		windowQueryUpdate.addKeyListener(new org.eclipse.swt.events.KeyListener() {
			@Override
			public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
			}

			@Override
			public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
			}
		});

		// Cursor cursor = new Cursor(device, 19);
		// Color red = new Color (device, 255, 0, 0);

		makeActions();
		hookContextMenu();
//		addQuery(qDataSources);
//		addQuery(qDataSourcesSubCountB);
//		addQuery(qDataSetContents);

//		addQuery(qMatchCAS);
//		addQuery(qMatchCASandName);
//		addQuery(qCountMatches);
//		addQuery(hSubsSameCas);

		for(HarmonyLabeledQuery harmonyLabeledQuery: labeledQueries){
			addQuery(harmonyLabeledQuery);
		}

		addUpdate(uDelDataSet);
//		addUpdate(uDelDataSet);
		// addQuery(qCasNotInDB);
		
		

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

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) viewer
						.getSelection();
				if (selection.isEmpty())
					return;
				String key = (String) selection.toList().get(0);
				System.out.println("key=" + key);

				HarmonyLabeledQuery q = queryFromKey(key);
				if (q != null){
					if(q.requiresParameters()){
						System.out.println("This is not ready yet.  Stay tuned...");
						System.exit(1);
					}else{
						
						System.out.println(q.getQuery());

						IWorkbenchPage page = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getActivePage();
						ResultsView resultsView = (ResultsView) page
								.findView(ResultsView.ID);
						ResultSet resultSet = q.getResultSet();
						TableProvider tableProvider = TableProvider.create((ResultSetRewindable)resultSet);
						resultsView.update(tableProvider);
					}
				}
//					DialogQueryDataset dialog = new DialogQueryDataset(Display
//							.getCurrent().getActiveShell());
//					
//					
//					dialog.create();
//					if (dialog.open() == Window.OK) {
//						System.out.println("OK");
//						String primaryDataSet = dialog.getPrimaryDataSet();
//						String[] referenceDataSets = dialog.getReferenceDataSets();
//						System.out.println(primaryDataSet);
//						for (String s : referenceDataSets) {
//							System.out.println(s);
//						}
//						// do query
//						
//
//
//						if (q instanceof IParamQuery) {
//							System.out.println(" is instanceof IParamQuery");
//							IParamQuery paramQuery = (IParamQuery) q;
//							paramQuery.setPrimaryDataSet(primaryDataSet);
//							paramQuery.setReferenceDataSets(referenceDataSets);
//							System.out.println(q.getQuery());
//
//							IWorkbenchPage page = PlatformUI.getWorkbench()
//									.getActiveWorkbenchWindow().getActivePage();
//							ResultsView resultsView = (ResultsView) page
//									.findView(ResultsView.ID);
//
////							q.getData();
//							System.out.println("It worked, now take this out and go back to work!");
////							resultsView.update(q.getData());
////							resultsView.update(q.getQueryResults());
//
//							System.out.println("done");
//						} else if (q instanceof IParamHarmonize) {
//							System.out.println(" is instanceof IParamHarmonize");
//							IParamHarmonize iParamHarmonize = (IParamHarmonize) q;
//							iParamHarmonize.setQueryDataSet(primaryDataSet);
//							iParamHarmonize.setReferenceDataSet(referenceDataSets[0]);
//							System.out.println(q.getQuery());
//
//							IWorkbenchPage page = PlatformUI.getWorkbench()
//									.getActiveWorkbenchWindow().getActivePage();
//							ResultsView resultsView = (ResultsView) page
//									.findView(ResultsView.ID);
//							// resultsView.update(q);
//
////							resultsView.iUpdate(q.getDataXform());
////							resultsView.iUpdate(q.getQueryResults());
//
//							System.out.println("done");
//						}
//
//					}
//				} else if (paramUpdates.contains(key)) {
//
//					// GET THE UPDATE STRING (BUT DON'T RUN IT)
//					HarmonyUpdate u = updateMap.get(key);
//					 System.out.println("u is:"+u.toString());
//					String updateStr = u.getQuery();
//					
//					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//					QueryView queryView = (QueryView) page.findView(QueryView.ID);
//					queryView.setTextAreaContent(updateStr);
//					
//					System.out.println("done");
//
//				} else {
//
//					HarmonyQuery q = queryMap.get(key);
//					System.out.println(q.getQuery());
//
//					IWorkbenchPage page = PlatformUI.getWorkbench()
//							.getActiveWorkbenchWindow().getActivePage();
//					ResultsView resultsView = (ResultsView) page
//							.findView(ResultsView.ID);
//					// resultsView.update(q);
//
//					resultsView.update(q.getData());
//					resultsView.update(q.getQueryResults());
//
//					System.out.println("done");
//				}
				
				try {
					Util.showView(ResultsView.ID);
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}

	public void addFilename(final String url) {
		viewer.add(url);
	}

	public void addQuery(HarmonyLabeledQuery query) {
		viewer.add(query.getLabel());
//		queryMap.put(query.getLabel(), query);
	}
	public void addUpdate(HarmonyUpdate update) {
		viewer.add(update.getLabel());
		updateMap.put(update.getLabel(), update);
	}

	public void removeFilename(Object element) {
		if (element == null)
			return;

		viewer.remove(element);
	}

	public void setTextAreaContent(String s) {
		windowQueryUpdate.setText(s);
		return;
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
}
