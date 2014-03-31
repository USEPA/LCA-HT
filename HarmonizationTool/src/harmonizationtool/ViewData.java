package harmonizationtool;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import harmonizationtool.edit.CSVEdittingSupport;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.TableKeeper;
import harmonizationtool.model.TableProvider;
import harmonizationtool.vocabulary.ECO;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author tec
 * 
 */
public class ViewData extends ViewPart {
	public ViewData() {
	}

	public static final String ID = "HarmonizationTool.viewData";
	private static String key = null;
	private TableViewer viewer;
	private static List<Object> columns = new ArrayList<Object>();
	// the menu that is displayed when column header is right clicked
	private Menu headerMenu;
	private TableColumn columnSelected = null;

	public static final String IMPACT_ASSESSMENT_METHOD_HDR = "Impact Assessment Method";
	// e.g. ReCiPe or TRACI
	
	public static final String IMPACT_CAT_HDR = "Impact_Category";
	// e.g. climate change
	// public static final Resource ImpactCategory = ECO.ImpactCategory;
	
	public static final String IMPACT_CAT_INDICATOR_HDR = "Impact category indicator"; 
	// e.g. infrared radiative forcing

	public static final String IMPACT_CHARACTERIZATION_MODEL_HDR = "Impact Characterization Model"; // e.g.
	// IPCC Global Warming Potential (GWP)
	
	public static final String IMPACT_CAT_REF_UNIT_HDR = "Impact cat ref unit";
	// e.g. kg CO2 eq

	public static final String CAT1_HDR = "Category"; // e.g. air
	public static final String CAT2_HDR = "Subcategory"; // e.g. low population
	public static final String CAT3_HDR = "Sub-subcategory";
	
	public static final String NAME_HDR = "Flowable Name";
	public static final String CASRN_HDR = "CASRN";
	public static final String ALT_NAME_HDR = "Flowable Alt_Name";

	// ECO.ImpactCharacterizationFactor;
		public static final String CHAR_FACTOR_HDR = "Characterization factor";
	// THIS IS THE (float) NUMBER

	public static final String FLOW_UNIT_HDR = "Flow Unit";
	// e.g. kg

	public static final String IGNORE_HDR = "Ignore";

	public TableViewer getViewer() {
		return viewer;
	}

	/**
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */
	class ViewContentProvider implements IStructuredContentProvider {
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

	@Override
	public void dispose() {
		super.dispose();
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.READ_ONLY);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				System.out.println("============row selected=======");
				System.out.println(selection.getClass().getName());
				Object element = selection.getFirstElement();
				System.out.println(element.getClass().getName());
				DataRow dataRow = (DataRow) element;
				TableProvider tableProvider = TableKeeper.getTableProvider(key);
				int index = tableProvider.getIndex(dataRow);
				System.out.println("index=" + index);
			}
		});

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void update(String key) {
		this.key = key;
		// viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
		// | SWT.V_SCROLL);
		viewer.setContentProvider(new ArrayContentProvider());
		final Table table = viewer.getTable();
		removeColumns(table);
		createColumns(viewer);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		viewer.setContentProvider(new ArrayContentProvider());
		TableProvider tableProvider = TableKeeper.getTableProvider(key);
		// System.out.println("tableProvider.getData()="+tableProvider.getData());
		// System.out.println("tableProvider.getData().toString()="+tableProvider.getData().toString());
		viewer.setInput(tableProvider.getData());
		// viewer.refresh();

	}

	public void clearView(String key) {
		if ((key != null) && (key.equals(this.key))) {
			final Table table = viewer.getTable();
			viewer.setInput(null);
			removeColumns(table);
			table.setHeaderVisible(false);
			table.setLinesVisible(false);
		}
	}

	/**
	 * removes columns from the given table
	 * 
	 * @param table
	 */
	private void removeColumns(Table table) {
		System.out.println(this.getClass().getName() + ".removeColumns(table)");
		table.setRedraw(false);
		while (table.getColumnCount() > 0) {
			table.getColumns()[0].dispose();
		}
		table.setRedraw(true);
	}

	private void createColumns(final TableViewer viewer) {
		System.out.println("key=" + key);
		if (key != null) {
			// Define the menu and assign to the table
			headerMenu = new Menu(viewer.getTable());
			viewer.getTable().setMenu(headerMenu);
			initializeColumnHeaderMenu(headerMenu);

			TableProvider tableProvider = TableKeeper.getTableProvider(key);
			List<DataRow> dataRowList = tableProvider.getData();
			DataRow dataRow = dataRowList.get(0);
			int numCol = dataRow.getColumnValues().size();
			System.out.println("numCol = " + numCol);
			ArrayList<String> titles = new ArrayList<String>();
			ArrayList<Integer> bounds = new ArrayList<Integer>();
			List<String> headerNames = tableProvider.getHeaderNamesAsStrings();
			if (headerNames == null || headerNames.isEmpty()) {
				for (int i = 1; i <= numCol; i++) {
					titles.add("Ignore");
					bounds.add(100);
				}
			} else {
				System.out
						.println("headerNames.size() = " + headerNames.size());
				for (int i = 1; i <= numCol; i++) {
					titles.add(headerNames.get(i - 1));
					bounds.add(100);
				}
			}
			String[] titlesArray = new String[titles.size()];
			titles.toArray(titlesArray);
			int[] boundsArray = new int[bounds.size()];
			int indx = 0;
			for (Integer integer : bounds) {
				boundsArray[indx++] = integer;
			}
			for (int i = 0; i < titles.size(); i++) {
				TableViewerColumn col = createTableViewerColumn(titlesArray[i],
						boundsArray[i], i);
				col.setLabelProvider(new MyColumnLabelProvider(i));
				// tableProvider.addHeaderName(titlesArray[i],col.hashCode());
				columns.add(col);
			}
			saveColumnNames();
		}
	}

	/**
	 * class for generating column labels. This class will handle a variable
	 * number of columns
	 * 
	 * @author tec
	 */
	class MyColumnLabelProvider extends ColumnLabelProvider {
		private int myColNum;

		public MyColumnLabelProvider(int colNum) {
			this.myColNum = colNum;
		}

		@Override
		public String getText(Object element) {
			DataRow dataRow = null;
			try {
				dataRow = (DataRow) element;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("element= " + element);
			}
			String s = "";
			try {
				int size = dataRow.getColumnValues().size();
				if (myColNum < size) {
					s = dataRow.getColumnValues().get(myColNum);
				}
			} catch (Exception e) {
				System.out.println("dataRow=" + dataRow);
				e.printStackTrace();
			}
			return s;
		}
	}

	/**
	 * convenience method for creating a TableViewerColumn
	 * 
	 * @param title
	 * @param bound
	 * @param colNumber
	 * @return
	 */
	private TableViewerColumn createTableViewerColumn(String title, int bound,
			final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer,
				SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		viewerColumn
				.setEditingSupport(new CSVEdittingSupport(viewer, colNumber));
		column.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource() instanceof TableColumn) {
					TableColumn col = (TableColumn) e.getSource();
					// set columnSelected; this is used when editing the column
					// headers
					columnSelected = col;
					int index = col.getAlignment();
					System.out.println("index =+index");
							
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (e.getSource() instanceof TableColumn) {
					TableColumn col = (TableColumn) e.getSource();
					// set columnSelected; this is used when editing the column
					// headers
					columnSelected = col;
					int index = col.getAlignment();
					System.out.println("index =+index");
							
				}
//				System.out.println(e.toString());
			}

		});
		return viewerColumn;
	}

	/**
	 * this method initializes the headerMenu with menuItems and a
	 * ColumnSelectionListener
	 * 
	 * @param parent
	 *            headerMenu which allows user to rename the columns
	 */
	private void initializeColumnHeaderMenu(Menu parent) {
		ColumnSelectionListener colListener = new ColumnSelectionListener();
		MenuItem menuItem = new MenuItem(parent, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(CASRN_HDR);

		menuItem = new MenuItem(parent, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(NAME_HDR);

		menuItem = new MenuItem(parent, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(ALT_NAME_HDR);

		menuItem = new MenuItem(parent, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(CAT1_HDR);

		menuItem = new MenuItem(parent, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(CAT2_HDR);

		menuItem = new MenuItem(parent, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(CAT3_HDR);

		menuItem = new MenuItem(parent, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(IMPACT_CAT_HDR);

		menuItem = new MenuItem(parent, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(IMPACT_CAT_REF_UNIT_HDR);

		menuItem = new MenuItem(parent, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(CHAR_FACTOR_HDR);

		menuItem = new MenuItem(parent, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(FLOW_UNIT_HDR);

		menuItem = new MenuItem(parent, SWT.NORMAL);
		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(IGNORE_HDR);
		// menuItem = new MenuItem(parent, SWT.NORMAL);
		// menuItem.addListener(SWT.Selection, colListener);
		// menuItem.setText("Custom...");
	}

	/**
	 * once the user has selected a column header for change this Listener will
	 * set the column header to the value selected by the user. If the user
	 * selects "Custom...", then a dialog is displayed so the user can enter a
	 * custom value for the column header.
	 * 
	 * @author tec 919-541-1500
	 * 
	 */
	private class ColumnSelectionListener implements Listener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets
		 * .Event)
		 * 
		 * this event handler is called when the user right clicks on the column
		 * to change the column header name.
		 * columnSelected.setText(menuItemText) will set the column header to
		 * the value of the menu item selected. If "Custom..." is selected, then
		 * the user can set a custom name for the column header.
		 */
		@Override
		public void handleEvent(Event event) {
			if ((event.widget instanceof MenuItem) && (columnSelected != null)) {
				String menuItemText = ((MenuItem) event.widget).getText();
				if (menuItemText != null) {
					if (menuItemText.equals("Custom...")) {
						// allow the user to define a custom header name
						InputDialog inputDialog = new InputDialog(getViewSite()
								.getShell(), "Column Name Dialog",
								"Enter a custom column label", "", null);
						inputDialog.open();
						int returnCode = inputDialog.getReturnCode();
						if (returnCode == InputDialog.OK) {
							String val = inputDialog.getValue();
							columnSelected.setText(val);
						}
					} else {
						columnSelected.setText(menuItemText);
					}

				}
				// save the column names to the TableProvider in case the data
				// table needs to be
				// re-displayed
				saveColumnNames();
			}
		}

	}

	/**
	 * this method retrieves the column header text values from the column
	 * components and passes them to the TableProvider so they can be retrieved
	 * when the data table is re-displayed
	 */
	private void saveColumnNames() {
		List<String> columnNames = new ArrayList<String>();
		TableColumn[] tableColumns = viewer.getTable().getColumns();
		for (TableColumn tableColumn : tableColumns) {
			columnNames.add(tableColumn.getText());
		}
		TableProvider tableProvider = TableKeeper.getTableProvider(key);
		tableProvider.setHeaderNames(columnNames);
	}
}
