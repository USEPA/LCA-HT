package harmonizationtool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.epa.nrmrl.std.lca.ht.compartment.mgr.HMatchCategories;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.HSubsSameCas;
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyUpdate;
import gov.epa.nrmrl.std.lca.ht.sparql.LabeledQuery;
import gov.epa.nrmrl.std.lca.ht.sparql.QCountMatches;
import gov.epa.nrmrl.std.lca.ht.sparql.QDataSetContents;
import gov.epa.nrmrl.std.lca.ht.sparql.QDataSources;
import gov.epa.nrmrl.std.lca.ht.sparql.QMatchCAS;
import gov.epa.nrmrl.std.lca.ht.sparql.QMatchCASandName;
import gov.epa.nrmrl.std.lca.ht.sparql.UDelDataSource;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.views.ResultsView;

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

public class WorkflowCommands extends ViewPart {

	public static final String ID = "HarmonizationTool.WorkflowCommandsID";

	private List<LabeledQuery> labeledQueries = new ArrayList<LabeledQuery>();

	private TableViewer viewer;

	private UDelDataSource uDelDataSource = new UDelDataSource();

	private Map<String, HarmonyUpdate> updateMap = new HashMap<String, HarmonyUpdate>();
	private List<String> paramUpdates = new ArrayList<String>();

//	private Text windowQueryUpdate;

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

	public WorkflowCommands() {
		paramUpdates.add("Delete data set..."); // FIXME, SHOULD GET THE KEY
												// FROM THE QUERY FILE
		createLabeledQueries();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(null);

		Device device = Display.getCurrent();

//		Button queryButton = new Button(parent, SWT.BORDER);
//		// btnNewButton.setBounds(149, 0, 148, 469);
//		queryButton.setBounds(20, 150, 100, 30);
//		queryButton.setAlignment(SWT.LEFT);
//		queryButton.setText("Run Query");
//		queryButton.addSelectionListener(new SelectionListener() {
//
//			@Override
//			public void widgetSelected(SelectionEvent e) {
////				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//				ResultsView resultsView = (ResultsView) Util.findView(ResultsView.ID);
//				String title = resultsView.getTitle();
//				System.out.println("title= " + title);
//
//				HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
////				harmonyQuery2Impl.setQuery(windowQueryUpdate.getText());
//				ResultSet resultSet = ((HarmonyQuery2Impl) harmonyQuery2Impl).getResultSet();
//
//				TableProvider tableProvider = TableProvider.create((ResultSetRewindable) resultSet);
//				resultsView.update(tableProvider);
//			}
//
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				// TODO Auto-generated method stub
//
//			}
//		});
//		Button updateButton = new Button(parent, SWT.BORDER);
//		updateButton.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
//		updateButton.setBounds(20, 190, 100, 30);
//		updateButton.setAlignment(SWT.LEFT);
//		// updateButton.setBackground(new Color(device,255,200,200)); // DOES
//		// NOT WORK IN WINDOWS
//		updateButton.setText("Run Update");
//		updateButton.addSelectionListener(new SelectionListener() {
//
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				// THE BUTTON WAS PUSHED
//			}
//
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				// TODO Auto-generated method stub
//
//			}
//		});


		makeActions();
		hookContextMenu();

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
				WorkflowCommands.this.fillContextMenu(manager);
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

//	private static void printValues(int lineNumber, String[] as) {
//		System.out.println("Line " + lineNumber + " has " + as.length + " values:");
//		for (String s : as) {
//			System.out.println("\t|" + s + "|");
//		}
//		System.out.println();
//	}
//
//	private DataRow initDataRow(String[] values) {
//		DataRow dataRow = new DataRow();
//		for (String s : values) {
//			dataRow.add(s);
//		}
//		return dataRow;
//	}

	private void makeActions() {

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection.isEmpty()) {
					return;
				}

//				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

				String key = (String) selection.toList().get(0);
				System.out.println("key=" + key);

				LabeledQuery labeledQuery = queryFromKey(key);
				String showResultsInWindow = ResultsView.ID;


				try {
					Util.showView(showResultsInWindow);
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}
}
