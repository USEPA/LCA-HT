package gov.epa.nrmrl.std.lca.ht.sparql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.HMatchCategories;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.MatchContexts;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.HSubsSameCas;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.ResultsTreeEditor;
import gov.epa.nrmrl.std.lca.ht.job.QueryViewJob;
import gov.epa.nrmrl.std.lca.ht.job.QueryViewJobChangeListener;
//import gov.epa.nrmrl.std.lca.ht.tdb.IActiveTDBListener;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetRewindable;

import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;

//public class QueryView extends ViewPart implements IActiveTDBListener {
public class QueryView extends ViewPart {

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.sparql.QueryView";

	private List<LabeledQuery> labeledQueries = new ArrayList<LabeledQuery>();

	private TableViewer viewer;

	private UDelDataSource uDelDataSource = new UDelDataSource();

	private Map<String, HarmonyUpdate> updateMap = new HashMap<String, HarmonyUpdate>();
	private List<String> paramUpdates = new ArrayList<String>();

	private Text windowQueryUpdate;

	private void createLabeledQueries() {
		labeledQueries.add(new QDataSources());
		labeledQueries.add(new QDataSetContents());
		labeledQueries.add(new QCountMatches());
		labeledQueries.add(new QMatchCAS());
		labeledQueries.add(new QMatchCASandName());
		labeledQueries.add(new HSubsSameCas());
		labeledQueries.add(new HMatchCategories());
	}

	private LabeledQuery queryFromKey(String key) {
		try {
			for (LabeledQuery labeledQuery : labeledQueries) {
				if (labeledQuery.getLabel().equals(key)) {
					return labeledQuery;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public QueryView() {
		paramUpdates.add("Delete data set..."); // FIXME, SHOULD GET THE KEY
												// FROM THE QUERY FILE
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
				// IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				ResultsView resultsView = (ResultsView) Util.findView(ResultsView.ID);
				String title = resultsView.getTitle();
				System.out.println("title= " + title);

				HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
				harmonyQuery2Impl.setQuery(windowQueryUpdate.getText());
				ResultSet resultSet = ((HarmonyQuery2Impl) harmonyQuery2Impl).getResultSet();

				TableProvider tableProvider = TableProvider.create((ResultSetRewindable) resultSet);
				resultsView.update(tableProvider);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		Button updateButton = new Button(parent, SWT.BORDER);
		updateButton.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
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
				// IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				ResultsView resultsView = (ResultsView) Util.findView(ResultsView.ID);
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

		windowQueryUpdate = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL
				| SWT.MULTI);
		windowQueryUpdate.setToolTipText("Load, type, or cut and paste a query here.  Then hit \"Run Query\"");
		// txtTextArea.setBounds(297, 0, 148, 469);
		windowQueryUpdate.setBounds(150, 0, 600, 500);

		// Color queryWindowColor = new Color(device, 255, 255, 200);
		windowQueryUpdate.setBackground(new Color(device, 255, 255, 200));
		windowQueryUpdate.setText("select * where {?s ?p ?o }");
		// parent.setLayout(null);
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
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

		for (LabeledQuery labeledQuery : labeledQueries) {
			addQuery(labeledQuery);
		}

		addUpdate(uDelDataSource);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * The content provider class is responsible for providing objects to the view. It can wrap existing objects in
	 * adapters or simply return objects as-is. These objects may be sensitive to the current input of the view, or
	 * ignore it and always show the same content (like Task List, for example).
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

	public class QueryViewLabelProvider extends LabelProvider implements ITableLabelProvider {
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

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection.isEmpty()) {
					return;
				}

				// IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

				String key = (String) selection.toList().get(0);
				System.out.println("key=" + key);

				LabeledQuery labeledQuery = queryFromKey(key);
				String showResultsInWindow = ResultsView.ID;
				if (labeledQuery != null) {
					if (labeledQuery instanceof HarmonyQuery2Impl) {

						// Code to initiate query execution Job
						// once the job is done queryCallback(ResultSet
						// resultSet, String key) will be called to display
						// results
						QueryViewJob harmonyQuery2ImplJob = new QueryViewJob("Query", (HarmonyQuery2Impl) labeledQuery);
						((HarmonyQuery2Impl) labeledQuery).getParamaterFromUser();
						harmonyQuery2ImplJob.setPriority(Job.SHORT);
						harmonyQuery2ImplJob.setSystem(false);
						harmonyQuery2ImplJob.addJobChangeListener(new QueryViewJobChangeListener((QueryView) Util
								.findView(QueryView.ID), key));
						harmonyQuery2ImplJob.schedule();

					}

				}
				// DialogQueryDataset dialog = new DialogQueryDataset(Display
				// .getCurrent().getActiveShell());
				//
				//
				// dialog.create();
				// if (dialog.open() == Window.OK) {
				// System.out.println("OK");
				// String primaryDataSet = dialog.getPrimaryDataSet();
				// String[] referenceDataSets = dialog.getReferenceDataSets();
				// System.out.println(primaryDataSet);
				// for (String s : referenceDataSets) {
				// System.out.println(s);
				// }
				// // do query
				//
				//
				//
				// if (q instanceof IParamQuery) {
				// System.out.println(" is instanceof IParamQuery");
				// IParamQuery paramQuery = (IParamQuery) q;
				// paramQuery.setPrimaryDataSet(primaryDataSet);
				// paramQuery.setReferenceDataSets(referenceDataSets);
				// System.out.println(q.getQuery());
				//
				// IWorkbenchPage page = PlatformUI.getWorkbench()
				// .getActiveWorkbenchWindow().getActivePage();
				// ResultsView resultsView = (ResultsView) page
				// .findView(ResultsView.ID);
				//
				// // q.getData();
				// System.out.println("It worked, now take this out and go back to work!");
				// // resultsView.update(q.getData());
				// // resultsView.update(q.getQueryResults());
				//
				// System.out.println("done");
				// } else if (q instanceof IParamHarmonize) {
				// System.out.println(" is instanceof IParamHarmonize");
				// IParamHarmonize iParamHarmonize = (IParamHarmonize) q;
				// iParamHarmonize.setQueryDataSet(primaryDataSet);
				// iParamHarmonize.setReferenceDataSet(referenceDataSets[0]);
				// System.out.println(q.getQuery());
				//
				// IWorkbenchPage page = PlatformUI.getWorkbench()
				// .getActiveWorkbenchWindow().getActivePage();
				// ResultsView resultsView = (ResultsView) page
				// .findView(ResultsView.ID);
				// // resultsView.update(q);
				//
				// // resultsView.iUpdate(q.getDataXform());
				// // resultsView.iUpdate(q.getQueryResults());
				//
				// System.out.println("done");
				// }
				//
				// }
				// } else if (paramUpdates.contains(key)) {
				//
				// // GET THE UPDATE STRING (BUT DON'T RUN IT)
				// HarmonyUpdate u = updateMap.get(key);
				// System.out.println("u is:"+u.toString());
				// String updateStr = u.getQuery();
				//
				// IWorkbenchPage page =
				// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				// QueryView queryView = (QueryView)
				// page.findView(QueryView.ID);
				// queryView.setTextAreaContent(updateStr);
				//
				// System.out.println("done");
				//
				// }

				try {
					Util.showView(showResultsInWindow);
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}

	/**
	 * this is method is called by QueryViewJobChangeListener once the QueryView Job is complete
	 * 
	 * @param resultSet
	 *            results of query
	 * @param key
	 *            type of query requested
	 */
	public void queryCallback(ResultSet resultSet, String key) {
		// IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		LabeledQuery labeledQuery = queryFromKey(key);
		String showResultsInWindow = ResultsView.ID;

		resultSet = ((HarmonyQuery2Impl) labeledQuery).getResultSet();
		// ResultSet resultSet = q.getResultSet();
		// TableProvider tableProvider = TableProvider
		// .create((ResultSetRewindable) resultSet);
		setTextAreaContent(((HarmonyQuery2Impl) labeledQuery).getQuery());
		if (key.startsWith("Harmonize CAS")) { // HACK!!
			ResultsTreeEditor resultsTreeEditor = (ResultsTreeEditor) Util.findView(ResultsTreeEditor.ID);
			// FIXME , BECAUSE WHICH ResultsSet CAN / SHOULD
			// USE
			// WHICH createTransform
			// AND WHICH formatForTransfor()
			// SHOULD BE KNOWN BY THE LabledQuery
			// BUT CHOSEN BY THE CALLER
			showResultsInWindow = ResultsTreeEditor.ID;

			// TableProvider tableProvider = TableProvider.createTransform0((ResultSetRewindable) resultSet);
			// THE LINE BELOW TOSSES OUT THE IDEA OF createTransform0, BUT WORKS FOR STANDARD QUERIES
			TableProvider tableProvider = TableProvider.create((ResultSetRewindable) resultSet);

			// resultsView.update(tableProvider);
			try {
				resultsTreeEditor.update(tableProvider);
			} catch (Exception e) {
				System.out.println("resultsTreeEditor=" + resultsTreeEditor);
				e.printStackTrace();
			}

			// resultsView.formatForTransform0();
		} else if (key.startsWith("Harmonize Compart")) { // HACK!!
			MatchContexts matchContexts = (MatchContexts) Util
					.findView(MatchContexts.ID);
			// FIXME , BECAUSE WHICH ResultsSet CAN / SHOULD
			// USE
			// WHICH createTransform
			// AND WHICH formatForTransfor()
			// SHOULD BE KNOWN BY THE LabledQuery
			// BUT CHOSEN BY THE CALLER
			showResultsInWindow = MatchContexts.ID;

			TableProvider tableProvider = TableProvider.create((ResultSetRewindable) resultSet);
			// resultsView.update(tableProvider);
//			try {
//				matchContexts.update(tableProvider);
//			} catch (Exception e) {
//				System.out.println("resultsTreeEditor=" + matchContexts);
//				e.printStackTrace();
//			}

			// resultsView.formatForTransform0();
		} else {
			ResultsView resultsView = (ResultsView) Util.findView(ResultsView.ID);
			TableProvider tableProvider = TableProvider.create((ResultSetRewindable) resultSet);
			resultsView.update(tableProvider);
		}

	}

	public void addFilename(final String url) {
		viewer.add(url);
	}

	public void addQuery(LabeledQuery query) {
		viewer.add(query.getLabel());
		// queryMap.put(query.getLabel(), query);
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

	// @Override
	// public void TDBchanged(String tdb) {
	// // System.out.println("new TDB = " + tdb);
	// // String key = (String) selection.toList().get(0);
	// // String key = "Show Data Sources";
	// // System.out.println("key=" + key);
	// // HarmonyQuery q = queryMap.get(key);
	// // System.out.println(q.getQuery());
	//
	// // IWorkbenchPage page = PlatformUI.getWorkbench()
	// // .getActiveWorkbenchWindow().getActivePage();
	// // ResultsView resultsView = (ResultsView)
	// // page.findView(ResultsView.ID);
	// // resultsView.update(q);
	//
	// // resultsView.update(q.getData());
	// // resultsView.update(q.getQueryResults());
	// // System.out.println("done");
	//
	// }
}
