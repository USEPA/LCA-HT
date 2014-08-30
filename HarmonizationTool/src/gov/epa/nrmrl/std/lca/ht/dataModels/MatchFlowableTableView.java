package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FEDLCA;
import harmonizationtool.vocabulary.SKOS;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author Tommy E. Cathey and Tom Transue
 * 
 */
public class MatchFlowableTableView extends ViewPart {

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.dataModels.MatchFlowableTableView";

	private static TableViewer tableViewer;
	private static Table table;
	//
	private static TextCellEditor editor;

	public MatchFlowableTableView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(null);
		Label label = new Label(composite, SWT.NONE);
		label.setText("label");
		System.out.println("hello, from sunny MatchFlowableTableView!");
		initializeTableViewer(composite);
		// initializePopup(composite);
		initialize();

		parent.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				table.setSize(table.getParent().getSize());
			}
			// THIS IS NOT PERFECT
			// WHEN THE WINDOW IS RESIZED SMALLER, THE TABLE OVER RUNS A LITTLE
		});
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private static void initializeTableViewer(Composite composite) {
		tableViewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);
		editor = new TextCellEditor(tableViewer.getTable());
		tableViewer.setContentProvider(new ArrayContentProvider());
	}

	//
	private static void initializeTable() {
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}

	public static void update(int rowNumber) {
		TableProvider tableProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey());
		DataRow dataRow = tableProvider.getData().get(rowNumber);
		Resource curDataSet = tableProvider.getDataSourceProvider().getTdbResource();
		List<Resource> queryPlusCandidates = new ArrayList<Resource>();
		if (dataRow.getMatchCandidates().isEmpty()) {
			// FIXME - OUGHT TO HAVE A BETTER HANDLE ON THE FLOWABLE RESOURCE
			// FOR THIS ROW, BUT WILL FIND BY RDF
			Resource flowableResource = null;
			ResIterator resIterator = ActiveTDB.tdbModel.listResourcesWithProperty(FEDLCA.sourceTableRowNumber,
					rowNumber + 1);
			while (resIterator.hasNext()) {
				Resource flowableResourceCandidate = resIterator.next();
				if (!flowableResourceCandidate.hasProperty(RDF.type, ECO.Flowable)) {
					continue;
				}
				if (!flowableResourceCandidate.hasProperty(ECO.hasDataSource, curDataSet)) {
					continue;
				}
				flowableResource = flowableResourceCandidate;
			}
			update(flowableResource);

			// StringBuilder b = new StringBuilder("");
			// b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
			// b.append("PREFIX  fedlca: <http://epa.gov/nrmrl/std/lca/fedlca/1.0#> \n");
			// b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
			// b.append("SELECT ?s \n");
			// b.append("WHERE \n");
			// b.append("?s fedlca:sourceTableRowNumber 2 . \n");
			// b.append("?s a eco:Flowable . \n");
			// b.append("?s eco:hasDataSource ?ds . \n");
			// b.append("?ds rdfs:label ?label . \n");
			// b.append("filter regex (str(?label), \"recipe108m_short\",\"i\") \n");
			// b.append("} \n");
		} else {
			queryPlusCandidates.add(dataRow.getMatchCandidates().get(0).getItemToMatchTDBResource());
			for (MatchCandidate matchCandidate : dataRow.getMatchCandidates()) {
				queryPlusCandidates.add(matchCandidate.getMatchCandidateTDBResource());
			}
			update(queryPlusCandidates);
		}
	}

	private static void update(Resource flowableResource) {
		TableProvider miniTableProvider = new TableProvider();
		DataRow miniDataRow = new DataRow();

		String name = "";
		if (flowableResource.hasProperty(RDFS.label)) {
			Statement statement = flowableResource.getProperty(RDFS.label);
			name = statement.getObject().asLiteral().getString();
			// name =
			// resource.getPropertyResourceValue(RDFS.label).getLocalName();

		}
		miniDataRow.add(" - ");

		Resource dataSourceResource = flowableResource.getProperty(ECO.hasDataSource).getObject().asResource();
		String dataSourceName = dataSourceResource.getProperty(RDFS.label).getObject().asLiteral().getString();
		miniDataRow.add(dataSourceName);

		miniDataRow.add(name);

		String casrn = "";
		if (flowableResource.hasProperty(ECO.casNumber)) {

			Statement statement = flowableResource.getProperty(ECO.casNumber);
			casrn = statement.getObject().asLiteral().getString();
			// casrn =
			// resource.getPropertyResourceValue(ECO.casNumber).getLocalName();
		}
		miniDataRow.add(casrn);

		String syns = "";
		StmtIterator stmtIterator = flowableResource.listProperties(SKOS.altLabel);
		while (stmtIterator.hasNext()) {
			String synonym = stmtIterator.next().getObject().asLiteral().getString();
			syns += synonym + System.getProperty("line.separator");
		}
		miniDataRow.add(syns);

		miniDataRow.add("other: N/A");
		miniTableProvider.addDataRow(miniDataRow);

		update(miniTableProvider);
		// TODO - NEED TO ARRANGE A BLANK ROW FOR SEARCH TOOL
	}

	private static void update(List<Resource> flowableResources) {
		TableProvider miniTableProvider = new TableProvider();

		for (int i = 0; i < flowableResources.size(); i++) {
			DataRow miniDataRow = new DataRow();

			Resource resource = flowableResources.get(i);
			// TableItem tableItem = table.getItem(i);

			if (i == 0) {
				miniDataRow.add(" - ");
			} else {
				miniDataRow.add(" " + i + " ");
			}
			
			Resource dataSourceResource = resource.getProperty(ECO.hasDataSource).getObject().asResource();
			String dataSourceName = dataSourceResource.getProperty(RDFS.label).getObject().asLiteral().getString();
			miniDataRow.add(dataSourceName);

			String name = "";
			if (resource.hasProperty(RDFS.label)) {
				Statement statement = resource.getProperty(RDFS.label);
				name = statement.getObject().asLiteral().getString();
				// name =
				// resource.getPropertyResourceValue(RDFS.label).getLocalName();

			}
			miniDataRow.add(name);

			String casrn = "";
			if (resource.hasProperty(ECO.casNumber)) {

				Statement statement = resource.getProperty(ECO.casNumber);
				casrn = statement.getObject().asLiteral().getString();
				// casrn =
				// resource.getPropertyResourceValue(ECO.casNumber).getLocalName();
			}
			miniDataRow.add(casrn);

			String syns = "";
			StmtIterator stmtIterator = resource.listProperties(SKOS.altLabel);
			while (stmtIterator.hasNext()) {
				String synonym = stmtIterator.next().getObject().asLiteral().getString();
				syns += synonym + System.getProperty("line.separator");
			}
			miniDataRow.add(syns);

			miniDataRow.add("other: N/A");
			miniTableProvider.addDataRow(miniDataRow);
		}
		update(miniTableProvider);
	}

	private static void update(TableProvider miniTableProvider) {
		reset();
		// createColumns();
		// TableProvider tableProvider = TableKeeper.getTableProvider(key);
		tableViewer.setInput(miniTableProvider.getData());
		miniTableProvider.resetAssignedCSVColumnInfo();
		// colorRowNumberColumn();
		table.setSize(table.getParent().getSize());
		// initializeHeaderMenu();
		// initializeColumnActionsMenu();
	}

	//
	public static void reset() {
		tableViewer.setInput(null);
		// removeColumns();
	}

	// private static void removeColumns() {
	// table.setRedraw(false);
	// while (table.getColumnCount() > 0) {
	// table.getColumns()[0].dispose();
	// }
	// table.setRedraw(true);
	// }

	private static void createColumns() {

		TableViewerColumn tableViewerColumn = createTableViewerColumn("Rank", 30, 0);
		tableViewerColumn.getColumn().setAlignment(SWT.LEFT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(1));

		tableViewerColumn = createTableViewerColumn("Data Source", 200, 1);
		tableViewerColumn.getColumn().setAlignment(SWT.LEFT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(2));

		tableViewerColumn = createTableViewerColumn("Name", 300, 2);
		tableViewerColumn.getColumn().setAlignment(SWT.LEFT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(3));

		tableViewerColumn = createTableViewerColumn("CASRN", 100, 3);
		tableViewerColumn.getColumn().setAlignment(SWT.RIGHT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(4));

		tableViewerColumn = createTableViewerColumn("Synonyms", 300, 4);
		tableViewerColumn.getColumn().setAlignment(SWT.LEFT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(5));

		tableViewerColumn = createTableViewerColumn("Other", 200, 5);
		tableViewerColumn.getColumn().setAlignment(SWT.LEFT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(6));
	}

	private static class MyColumnLabelProvider extends ColumnLabelProvider {
		private int dataColumnNumber;

		public MyColumnLabelProvider(int colNum) {
			this.dataColumnNumber = colNum - 1;
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
				if (dataColumnNumber < size) {
					s = dataRow.getColumnValues().get(dataColumnNumber);
				}
			} catch (Exception e) {
				System.out.println("dataRow=" + dataRow);
				e.printStackTrace();
			}
			return s;
		}
	}

	private static class RowIndexColumnLabelProvider extends ColumnLabelProvider {

		public RowIndexColumnLabelProvider() {
		}

		@Override
		public String getToolTipText(Object element) {
			DataRow dataRow = null;
			try {
				dataRow = (DataRow) element;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("element= " + element);
			}
			String t = "";
			try {
				t = dataRow.getRowToolTip();

			} catch (Exception e) {
				System.out.println("dataRow=" + dataRow);
				e.printStackTrace();
			}
			return t;
		}

		@Override
		public Point getToolTipShift(Object object) {
			return new Point(5, 5);
		}

		@Override
		public int getToolTipDisplayDelayTime(Object object) {
			return 100; // msec
		}

		@Override
		public int getToolTipTimeDisplayed(Object object) {
			return 5000; // msec
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
				// s = dataRow.getRowNumber() + 1 + "";
				int rowNumPlus1 = dataRow.getRowNumber() + 1;
				s = rowNumPlus1 + "";
			} catch (Exception e) {
				System.out.println("dataRow=" + dataRow);
				e.printStackTrace();
			}
			return s;
		}
	}

	private static TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {

		final TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE, colNumber);
		final TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText(title);
		tableColumn.setWidth(bound);
		tableColumn.setResizable(true);
		tableColumn.setMoveable(false);
		tableColumn.addSelectionListener(colSelectionListener);
		// tableColumn.addListener(SWT.MouseDown, (Listener)
		// columnMouseListener);

		// if (colNumber > 0) {
		// tableColumn.setToolTipText(csvColumnDefaultTooltip);
		// }

		return tableViewerColumn;
	}

	//
	private static SelectionListener colSelectionListener = new SelectionListener() {

		private void doit(SelectionEvent e) {
			// System.out.println("SelectionListener event e= " + e);
			if (e.getSource() instanceof TableColumn) {
				// TableColumn col = (TableColumn) e.getSource();
				// colNumSelected = table.indexOf(col);
				// if (colNumSelected > 0) {
				// headerMenu.setVisible(true);
				// }
			}
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			doit(e);
		};

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			doit(e);
		}
	};

	public static void initialize() {
		initializeTable();
		createColumns();
		// initializeIgnoreRowMenu();
		// initializeFixRowMenu();
		// CONSIDER: headerMenu;
		// CONSIDER: columnActionsMenu;
		// CONSIDER: ignoreRowMenu;
		// CONSIDER: fixCellMenu;
		// rowsToIgnore.clear();
		// rowNumSelected = -1;
		// colNumSelected = -1;

	}
}
