package gov.epa.nrmrl.std.lca.ht.flowable.mgr;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView.LCACellModifier;
//import gov.epa.nrmrl.std.lca.ht.dataCuration.AnnotationProvider;
import gov.epa.nrmrl.std.lca.ht.dataCuration.ComparisonKeeper;
import gov.epa.nrmrl.std.lca.ht.dataCuration.ComparisonProvider;
import gov.epa.nrmrl.std.lca.ht.dataCuration.CurationMethods;
import gov.epa.nrmrl.std.lca.ht.dataFormatCheck.Issue;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.Flow;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataPropertyProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.FlowUnit;
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;

/**
 * @author Tommy E. Cathey and Tom Transue
 * 
 */
public class MatchFlowables extends ViewPart {

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.flowable.MatchFlowableTableView";

	private static TableViewer tableViewer;
	private static Table table;

	private static int rowNumSelected = -1;
	private static int colNumSelected = -1;
	private static Flowable flowableToMatch;
	private static int dataTableRowNum = -1;
	private static Button addToMaster;

	private static int maxSearchResults = 100;
	private static int nextStartResult = 0;
	private static Text rowCountText;
	public static Text editorText;
	private static Composite outerComposite;
	private static Combo chooseSearchFieldCombo;
	private static Text chooseSearchFieldText;
	private static Button searchButton;

	private static Color orange = new Color(Display.getCurrent(), 255, 128, 0);

	private static List<FlowableTableRow> flowableTableRows;

	public MatchFlowables() {
	}

	@Override
	public void createPartControl(Composite parent) {
		outerComposite = new Composite(parent, SWT.NONE);
		outerComposite.setLayout(new GridLayout(1, false));
		// System.out.println("hello, from sunny MatchFlowables!");
		initializeTableViewer(outerComposite);
		initialize();
	}
	
	public static class ReadOnlyCellEditor extends TextCellEditor {
		
		public static Color currentColor = null;
		
		public ReadOnlyCellEditor(Composite parent) {
			super(parent, SWT.READ_ONLY);
		}
		protected void doSetFocus() {
			text.setBackground(currentColor);
		}
		protected void doSetValue(Object value) {
			if (value != null)
				super.doSetValue(value);
		}
		
	}
	
	public static class MatchCellModifier implements ICellModifier {

		@Override
		public Object getValue(Object element, String property) {
			int tableRow = ((FlowableTableRow)element).getRowNumber();
			int index = Integer.valueOf(property) + 1;

			ReadOnlyCellEditor.currentColor = table.getItem(tableRow).getBackground();
			if (index >= 0)
				return ((DataRow)element).get(index);
			return null;
		}
		
		@Override
		public boolean canModify(Object element, String property) {
			return true;
		}

		@Override
		public void modify(Object element, String property, Object newValue) {		
		}
		
	}

	private static void initializeTableViewer(Composite composite) {

		Composite innerComposite = new Composite(outerComposite, SWT.NONE);
		innerComposite.setLayout(new GridLayout(7, false));
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gridData.heightHint = 30;
		innerComposite.setLayoutData(gridData);

		composite.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				table.setSize(table.getParent().getSize());
			}
			// THIS IS NOT PERFECT
			// WHEN THE WINDOW IS RESIZED SMALLER, THE TABLE OVER RUNS A LITTLE
		});

		Button acceptAdvance = new Button(innerComposite, SWT.NONE);
		acceptAdvance.setText("Next");
		acceptAdvance.addSelectionListener(nextSelectionListener);

		addToMaster = new Button(innerComposite, SWT.NONE);
		addToMaster.setText("Add to Master");
		addToMaster.setVisible(true);
		addToMaster.addSelectionListener(addToMasterListener);

		rowCountText = new Text(innerComposite, SWT.BORDER);
		GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_text.minimumWidth = 70;
		rowCountText.setEditable(false);
		rowCountText.setLayoutData(gd_text);

		searchButton = new Button(innerComposite, SWT.NONE);
		searchButton.setText("Search:");
		searchButton.addSelectionListener(searchListener);

		chooseSearchFieldCombo = new Combo(innerComposite, SWT.NONE);
		chooseSearchFieldCombo.add("Name / synonym");
		chooseSearchFieldCombo.add("CAS RN");
		chooseSearchFieldCombo.add("(other)");
		chooseSearchFieldCombo.select(0);
		chooseSearchFieldCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				resetSearchButton();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				resetSearchButton();
			}
		});

		chooseSearchFieldText = new Text(innerComposite, SWT.BORDER);
		GridData gd_chooseSearchFieldText = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_chooseSearchFieldText.widthHint = 6000;
		chooseSearchFieldText.setLayoutData(gd_chooseSearchFieldText);
		new Label(innerComposite, SWT.NONE);
		chooseSearchFieldText.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resetSearchButton();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				resetSearchButton();
			}
		});

		tableViewer = new TableViewer(composite, SWT.FULL_SELECTION);
		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);
		tableViewer.setContentProvider(new ArrayContentProvider());

		CellEditor[] editors = new CellEditor[11];
		String[] columnProperties = new String[editors.length];
		for (int i = 6; i < 11; ++i) {
			editors[i] = new ReadOnlyCellEditor(tableViewer.getTable());
			columnProperties[i] = String.valueOf(i - 1);
		}
		tableViewer.setCellEditors(editors);
		tableViewer.setCellModifier(new MatchCellModifier());
		tableViewer.setColumnProperties(columnProperties);
	}

	private static void initializeTable() {
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addListener(SWT.MouseUp, tableSelectionListener);
	}

	private static Listener tableSelectionListener = new Listener() {

		@Override
		public void handleEvent(Event event) {
			Point ptLeft = new Point(1, event.y);
			Point ptClick = new Point(event.x, event.y);
			TableItem tableItem = table.getItem(ptLeft);
			if (tableItem == null) {
				return;
			}
			rowNumSelected = table.indexOf(tableItem);
			colNumSelected = getTableColumnNumFromPoint(rowNumSelected, ptClick);

			if ((rowNumSelected > 0) && (colNumSelected < 6) && (colNumSelected > -1)) {
				assignMatch();
			}
			table.deselect(rowNumSelected);
		}
	};

	private static int getTableColumnNumFromPoint(int row, Point pt) {
		TableItem item = table.getItem(row);
		for (int i = 0; i < table.getColumnCount(); i++) {
			Rectangle rect = item.getBounds(i);
			if (rect.contains(pt)) {
				return i;
			}
		}
		return -1;
	}

	public static void update(int rowNumber) {
		try {
			Util.showView(CSVTableView.ID);
			Util.showView(FlowsWorkflow.ID);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		removeColumns();
		createColumns();
		dataTableRowNum = rowNumber;

		TableProvider tableProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey());
		List<DataRow> data = tableProvider.getData();
		DataRow dataRow = data.get(rowNumber);
		flowableToMatch = dataRow.getFlowable();

		if (flowableToMatch == null) {
			return;
		}

		if (!flowableToMatch.wasDoubleChedked) {
			flowableToMatch.setCandidates();
		}

		resetSearchButton();
		chooseSearchFieldCombo.select(0);
		chooseSearchFieldText.setText("");

		// flowableToMatch.transferSearchResults();
		flowableToMatch.clearSearchComparisons();

		boolean adHoc = false;
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		adHoc = ActiveTDB.getModel(null).contains(flowableToMatch.getTdbResource(), LCAHT.hasQCStatus,
				LCAHT.QCStatusAdHocMaster);
		ActiveTDB.tdbDataset.end();
		if (adHoc) {
			addToMaster.setText("Remove from Master");
		} else {
			addToMaster.setText("Add to Master");
		}

		int rowCount = 0;
		for (int i = flowableToMatch.getFirstRow(); i < data.size(); i++) {
			Flowable flowableOfRow = data.get(i).getFlowable();
			if (flowableOfRow != null) {
				if (flowableOfRow.equals(flowableToMatch)) {
					rowCount++;
				}
			}
		}
		rowCountText.setText(rowCount + " rows");
		System.out.println("got rows:" + rowCount);

		// flowableToMatch.clearSyncDataFromTDB(); // NECESSARY? GOOD? TODO: CHECK THIS
		// LinkedHashMap<Resource, String> matchCandidateResources = flowableToMatch.getMatchCandidates();
		List<ComparisonProvider> comparisonProviders = flowableToMatch.getComparisons();

		flowableTableRows = new ArrayList<FlowableTableRow>();
		FlowableTableRow flowableTableRow0 = new FlowableTableRow();
		flowableTableRow0.setFlowable(flowableToMatch);
		flowableTableRow0.setRowNumber(0);
		flowableTableRow0.setValues();
		flowableTableRows.add(flowableTableRow0);

		int row = 1;
		for (ComparisonProvider comparisonProvider : comparisonProviders) {
			Flowable dFlowable = new Flowable(comparisonProvider.getMasterDataObject());
			FlowableTableRow flowableTableRow = new FlowableTableRow();
			flowableTableRow.setFlowable(dFlowable);
			flowableTableRow.setRowNumber(row);
			flowableTableRow.matchStatus = MatchStatus.getByResource(comparisonProvider.getEquivalence());
			flowableTableRow.setValues();
			flowableTableRows.add(flowableTableRow);
			row++;

		}
		// for (Object dFlowableResource : matchCandidateResources.keySet()) {
		// Flowable dFlowable = new Flowable((Resource) dFlowableResource);
		// FlowableTableRow flowableTableRow = new FlowableTableRow();
		// flowableTableRow.setFlowable(dFlowable);
		// flowableTableRow.setRowNumber(row);
		// flowableTableRow.matchStatus = MatchStatus.getBySymbol(matchCandidateResources.get(dFlowableResource));
		// flowableTableRow.setValues();
		// flowableTableRows.add(flowableTableRow);
		// row++;
		// }

		tableViewer.setInput(flowableTableRows);

		Point p = table.getParent().getParent().getSize();
		p.y -= 30;
		table.setSize(p);
		table.getItem(0).setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
		updateMatchCounts();
		autosizeColumns();
	}

	private static void autosizeColumns() {
		for (int i = 0, n = table.getColumnCount(); i < n; i++) {
			table.getColumn(i).pack();
			int width = table.getColumn(i).getWidth();
			if (width < 20) {
				table.getColumn(i).setWidth(20);
			} else if (width > 400 && table.getHorizontalBar().getVisible()) {
				table.getColumn(i).setWidth(400);
			}
		}
	}

	private static void removeColumns() {
		table.setRedraw(false);
		while (table.getColumnCount() > 0) {
			table.getColumns()[0].dispose();
		}
		table.setRedraw(true);
	}

	private static void displayNewSearchResults() {
		// flowableToMatch.transferSearchResults();
		clearSearchRows();
		List<ComparisonProvider> searchResultComparisons = flowableToMatch.getSearchComparisons();
		// LinkedHashMap<Resource, String> searchResultResources = flowableToMatch.getSearchResults();
		int row = flowableToMatch.getComparisons().size() + 1;
		for (ComparisonProvider comparisonProvider : searchResultComparisons) {
			Flowable dFlowable = new Flowable(comparisonProvider.getMasterDataObject());
			FlowableTableRow flowableTableRow = new FlowableTableRow();
			flowableTableRow.setFlowable(dFlowable);
			flowableTableRow.setRowNumber(row);
			flowableTableRow.matchStatus = MatchStatus.getByResource(comparisonProvider.getEquivalence());
			flowableTableRow.setValues();
			flowableTableRows.add(flowableTableRow);
			row++;
		}
		Point p = table.getParent().getParent().getSize();
		p.y -= 30;
		table.setSize(p);
		updateMatchCounts();
		autosizeColumns();
	}

	private static void createColumns() {

		TableViewerColumn tableViewerColumn;
		// MatchStatus matchStatus;

		createTableViewerMatchColumn(MatchStatus.UNKNOWN);
		createTableViewerMatchColumn(MatchStatus.EQUIVALENT);
		createTableViewerMatchColumn(MatchStatus.SUBSET);
		createTableViewerMatchColumn(MatchStatus.SUPERSET);
		createTableViewerMatchColumn(MatchStatus.PROXY);
		createTableViewerMatchColumn(MatchStatus.NONEQUIVALENT);

		tableViewerColumn = createTableViewerColumn("Data Source", SWT.LEFT, 200, 6);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(6));

		tableViewerColumn = createTableViewerColumn(Flowable.flowableNameString, SWT.LEFT, 300, 7);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(7));

		tableViewerColumn = createTableViewerColumn(Flowable.casString, SWT.RIGHT, 100, 8);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(8));

		tableViewerColumn = createTableViewerColumn(Flowable.flowableSynonymString, SWT.LEFT, 300, 9);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(9));

		tableViewerColumn = createTableViewerColumn("Other", SWT.LEFT, 200, 10);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(10));
	}

	private static void updateMatchCounts() {
		Integer[] matchSummary = new Integer[] { 0, 0, 0, 0, 0, 0 };
		int hits = 0;
		if (table.getItemCount() == 0) {
			return;
		}
		List<ComparisonProvider> comparisons = flowableToMatch.getComparisons();
		// LinkedHashMap<Resource, String> candidateMap = flowableToMatch.getMatchCandidates();
		// int tableRow = 1;
		for (ComparisonProvider comparisonProvider : comparisons) {
			int col = MatchStatus.getByResource(comparisonProvider.getEquivalence()).getValue();
			if (col > 0 && col < 5) {
				hits++;
			}
			// TableItem tableItem = table.getItem(tableRow);
			// tableItem.setText(col,value);
			matchSummary[col]++;
		}

		// TableItem tableItem = table.getItem(0);
		FlowableTableRow rowZero = flowableTableRows.get(0);
		for (int colNum = 0; colNum < 6; colNum++) {
			rowZero.set(colNum, matchSummary[colNum].toString());
		}

		Util.findView(FlowsWorkflow.ID);

		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		boolean adHoc = ActiveTDB.getModel(null).contains(flowableToMatch.getTdbResource(), LCAHT.hasQCStatus,
				LCAHT.QCStatusAdHocMaster);
		ActiveTDB.tdbDataset.end();

		if (adHoc) {
			FlowsWorkflow.addMatchFlowableRowNum(flowableToMatch.getFirstRow());
			table.getItem(0).setBackground(SWTResourceManager.getColor(SWT.COLOR_CYAN));
			FlowsWorkflow.addMatchFlowableRowNum(flowableToMatch.getFirstRow());
		} else if (hits == 0 && comparisons.size() == 0) {
			FlowsWorkflow.removeMatchFlowableRowNum(flowableToMatch.getFirstRow());
			table.getItem(0).setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));

		} else if (hits == 1) {
			FlowsWorkflow.addMatchFlowableRowNum(flowableToMatch.getFirstRow());
			table.getItem(0).setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
		} else {
			FlowsWorkflow.removeMatchFlowableRowNum(flowableToMatch.getFirstRow());
			table.getItem(0).setBackground(orange);
		}
		table.redraw();
		tableViewer.refresh();
	}

	public static int getCsvTableRowNum() {
		return dataTableRowNum;
	}

	public static void setCsvTableRowNum(int csvTableRowNum) {
		MatchFlowables.dataTableRowNum = csvTableRowNum;
	}

	protected static void assignMatch() {
		List<ComparisonProvider> comparisons = flowableToMatch.getComparisons();

		TableItem tableItem = table.getItem(rowNumSelected);
		FlowableTableRow flowableTableRow = (FlowableTableRow) tableItem.getData();
		Flowable flowable = flowableTableRow.getFlowable();
		Resource flowableResource = flowable.getTdbResource();
		MatchStatus matchStatus = MatchStatus.getByValue(colNumSelected);
		flowableTableRow.setMatchStatus(matchStatus);
		// LinkedHashMap<Resource, String> candidateMap = flowableToMatch.getMatchCandidates();

		if (rowNumSelected < comparisons.size() + 1) {
			ComparisonProvider comparisonProvider = comparisons.get(rowNumSelected - 1);
			comparisonProvider.setEquivalence(matchStatus.getEquivalence());
			// candidateMap.put(flowableResource, matchStatus.getSymbol());
		} else {
			List<ComparisonProvider> searchComparisons = flowableToMatch.getSearchComparisons();
			// LinkedHashMap<Resource, String> searchMap = flowableToMatch.getSearchResults();
			// if (searchMap != null) {
			if (rowNumSelected < comparisons.size() + searchComparisons.size() + 1) {
				ComparisonProvider comparisonProvider = searchComparisons.get(rowNumSelected - comparisons.size() - 1);
				comparisonProvider.setEquivalence(matchStatus.getEquivalence());
				// searchMap.put(flowableResource, matchStatus.getSymbol());
				if (colNumSelected == 0) {
					flowableToMatch.removeComparison(comparisonProvider);
				} else {
					flowableToMatch.addComparison(comparisonProvider);
				}
			}
		}
		Resource equivalenceResource = matchStatus.getEquivalence();
		ComparisonProvider comparisonProvider = ComparisonKeeper.findComparison(flowableToMatch.getTdbResource(),
				flowableResource);
		if (comparisonProvider == null) {
			comparisonProvider = new ComparisonProvider(flowableToMatch.getTdbResource(), flowableResource,
					equivalenceResource);
		} else {
			comparisonProvider.setEquivalence(equivalenceResource);
		}
		comparisonProvider.appendToComment(" - Udpated by curator");
		comparisonProvider.syncToTDB();
		// AnnotationProvider.updateCurrentAnnotationModifiedDate();
		// CSVTableView.colorOneFlowableRow(flowableToMatch.getFirstRow());
		updateMatchCounts();
		rematchFlows();
	}

	private static void rematchFlows() {
		List<Integer> flowsToReMatchRows = new ArrayList<Integer>();
		try {
			Util.showView(CSVTableView.ID);
			Util.showView(FlowsWorkflow.ID);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TableProvider tableProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey());
		for (DataRow dataRow : tableProvider.getData()) {
			Flowable flowable = dataRow.getFlowable();
			if (flowable != null) {
				if (flowable.equals(flowableToMatch)) {
					flowsToReMatchRows.add(dataRow.getRowNumber());
				}
			}
		}
		Flow.matchFlows(flowsToReMatchRows);
	}

	@Override
	public void setFocus() {
	}

	private static void clearSearchRows() {
		int totalRows = flowableTableRows.size();
		for (int i = totalRows - 1; i > flowableToMatch.getComparisons().size() + 1; i--) {
			flowableTableRows.remove(i);
		}
		// flowableToMatch.clearSearchResults();
	}

	private static SelectionListener searchListener = new SelectionListener() {
		private void doit(SelectionEvent e) {
			findMatches();
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			doit(e);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			doit(e);
		}

	};

	private static void findMatches() {
		String uneditedParam = chooseSearchFieldText.getText();
		boolean casSearch = false;
		String whereParam = uneditedParam;
		if (uneditedParam.matches("[0-9][0-9]+-[0-9][0-9]-[0-9]")) {
			whereParam = Flowable.standardizeCAS(uneditedParam);
			casSearch = true;
		} else {
			String whereParamPre = star2regex(uneditedParam.toLowerCase());
			whereParam = whereParamPre.replaceAll("\"", "\\\\\"");
		}

		if (whereParam.matches("\\s*")) {
			return;
		}
		String whereClause = "";
		if (!casSearch) {
			/* Name / synonym search - Must have at least on alphanumeric */
			if (!uneditedParam.matches(".*[a-zA-Z0-9].*")) {
				return;
			}
			whereClause = "?f skos:altLabel ?syn . \n" + "filter regex(str(?syn),\"" + whereParam + "\") \n";
		} else {
			/* CAS RN search */
			if (!uneditedParam.matches(".*[0-9].*")) {
				return;
			}
			whereClause = "?f fedlca:hasFormattedCAS ?cas . \n" + "filter regex(str(?cas),\"" + whereParam + "\") \n";
		}
		// if (searchAllFields) {
		// // if (!uneditedParam.matches(".*[0-9].*")) {
		// // return;
		// // }
		// whereClause = "    ?f ?p ?other . \n" + "    filter regex(str(?other),\"" + whereParam + "\") \n";
		// }

		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		b.append(" \n");
		b.append("SELECT distinct ?f  \n");
		b.append("WHERE \n");
		b.append("  { \n");
		b.append(whereClause);
		b.append("    ?f a eco:Flowable . \n");
		b.append("    ?f eco:hasDataSource ?ds . \n");
		b.append("    ?ds a ?masterTest . \n");
		b.append("    filter regex (str(?masterTest), \"Dataset\") \n");
		b.append("   } order by afn:localname(?masterTest) \n");
		b.append("   limit " + maxSearchResults + " offset " + nextStartResult + "\n");
		String query = b.toString();
		System.out.println("query = \n" + query);
		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);
		Logger.getLogger("run").info("Searching master list for matching flowables...");

		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		// flowableToMatch.transferSearchResults();
		// flowableToMatch.clearSearchResults();
		flowableToMatch.clearSearchComparisons();

		// resetTable();
		// LinkedHashMap<Resource, String> candidateMap = flowableToMatch.getMatchCandidates();
		int count = 0;
		List<Resource> candidates = new ArrayList<Resource>();
		while (resultSet.hasNext()) {
			count++;
			QuerySolution querySolution = resultSet.next();
			RDFNode rdfNode = querySolution.get("f");
			Resource resource = rdfNode.asResource();
			candidates.add(resource);
		}
		// Expect that resultSet is done, so block manager is happy

		for (Resource resource : candidates) {
			/* Don't match self */
			if (flowableToMatch.getTdbResource().equals(resource)) {
				continue;
			}
			ComparisonProvider comparisonProvider = flowableToMatch.getComparison(resource);
			/* Don't match something already in the list of Comparisons */
			if (comparisonProvider != null) {
				continue;
			}
			comparisonProvider = new ComparisonProvider(flowableToMatch.getTdbResource(), resource,
					FedLCA.EquivalenceCandidate);
			flowableToMatch.addSearchComparison(comparisonProvider);
		}
		// resultSet.remove();
		// for (Resource resource : candidateMap.keySet()) {
		// flowableToMatch.addSearchResult(resource, candidateMap.get(resource));
		// }
		if (count > 99) {
			nextStartResult += maxSearchResults;
			searchButton.setText(nextStartResult + " -");
		} else {
			resetSearchButton();
		}
		Logger.getLogger("run").info("... search complete. " + count + " matching field are shown.");
		displayNewSearchResults();
	}

	private static void resetSearchButton() {
		nextStartResult = 0;
		searchButton.setText("Search:");
	}

	private static String star2regex(String typicalWildcards) {
		StringBuilder regexBuilder = new StringBuilder();
		regexBuilder.append("^\\\\Q");
		regexBuilder.append(typicalWildcards.replaceAll("\\*", "\\\\\\\\E.*\\\\\\\\Q"));
		regexBuilder.append("\\\\E$");
		return regexBuilder.toString();
	}

	private static class MyColumnLabelProvider extends ColumnLabelProvider {
		private int dataColumnNumber;

		public MyColumnLabelProvider(int colNum) {
			this.dataColumnNumber = colNum;
		}

		@Override
		public String getText(Object element) {
			FlowableTableRow flowableTableRow = null;
			try {
				flowableTableRow = (FlowableTableRow) element;
			} catch (Exception e) {
				e.printStackTrace();
			}
			String s = "";
			try {
				int size = flowableTableRow.getColumnValues().size();
				if (dataColumnNumber < size) {
					s = flowableTableRow.getColumnValues().get(dataColumnNumber);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return s;
		}
	}

	private static TableViewerColumn createTableViewerColumn(String title, int style, int bound, final int colNumber) {

		final TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, style, colNumber);
		final TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText(title);
		tableColumn.setWidth(bound);
		tableColumn.setResizable(true);
		tableColumn.setMoveable(false);
		return tableViewerColumn;
	}

	private static void createTableViewerMatchColumn(MatchStatus matchStatus) {
		int colNumber = matchStatus.getValue();
		TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.CENTER, colNumber);
		TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText(matchStatus.getSymbol());
		tableColumn.setWidth(20);
		tableColumn.setResizable(true);
		tableColumn.setMoveable(false);
		tableColumn.setToolTipText(matchStatus.getName() + " - " + matchStatus.getComment());
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(colNumber));
	}

	private static SelectionListener nextSelectionListener = new SelectionListener() {

		private void doit(SelectionEvent e) {
			Object source = e.getSource();
			boolean nextUnmatched = false;
			if (source instanceof Button) {
				String buttonText = ((Button) source).getText();
				if (buttonText.matches(".*Unmatched.*")) {
					nextUnmatched = true;
				}
			}
			CSVTableView.selectNext(ID, nextUnmatched);
			// System.out.println("event e = " + e);
			// CSVTableView.selectNext(ID, false);
			// CSVTableView.selectNextFlowable();
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

	private static SelectionListener addToMasterListener = new SelectionListener() {

		private void doit(SelectionEvent e) {
			// int count = 0;
			if (CSVTableView.getTableProviderKey() == null) {
				return;
			}
			if (CSVTableView.preCommit) {
				return;
			}
			if (addToMaster.getText().equals("Add to Master")) {
				ActiveTDB.tsAddGeneralTriple(flowableToMatch.getTdbResource(), LCAHT.hasQCStatus,
						LCAHT.QCStatusAdHocMaster, null);
				FlowsWorkflow.addMatchFlowableRowNum(flowableToMatch.getFirstRow());
				table.getItem(0).setBackground(SWTResourceManager.getColor(SWT.COLOR_CYAN));
				addToMaster.setText("Remove from Master");
			} else {
				ActiveTDB.tsRemoveStatement(flowableToMatch.getTdbResource(), LCAHT.hasQCStatus,
						LCAHT.QCStatusAdHocMaster);
				updateMatchCounts();
				addToMaster.setText("Add to Master");
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
	}

	public static void colorCell(int rowNumber, int colNumber, Color color) {
		if (rowNumber > -1 && rowNumber < table.getItemCount()) {
			TableItem tableItem = table.getItem(rowNumber);
			tableItem.setBackground(colNumber, color);
		}
	}

	public static void setFlowable(Flowable flowable) {
		// TODO Auto-generated method stub

	}

	public static Flowable getFlowableToMatch() {
		return flowableToMatch;
	}

	public static void setFlowableToMatch(Flowable flowableToMatch) {
		MatchFlowables.flowableToMatch = flowableToMatch;
	}

	public static int getMaxSearchResults() {
		return maxSearchResults;
	}

	public static void setMaxSearchResults(int maxSearchResults) {
		MatchFlowables.maxSearchResults = maxSearchResults;
	}

	public static class FlowableTableRow extends DataRow {
		private MatchStatus matchStatus;

		public FlowableTableRow() {
			setBlankData(11);
		}

		public MatchStatus getMatchStatus() {
			return matchStatus;
		}

		public void setValues() {
			if (matchStatus != null) {
				setMatchStatus(matchStatus);
			}

			Flowable flowable = this.getFlowable();
			getColumnValues().set(6, flowable.getDataSource());
			getColumnValues().set(7, flowable.getName());
			getColumnValues().set(8, flowable.getFormattedCas());
			String[] syns = flowable.getSynonyms();
			if (syns.length == 0) {
				getColumnValues().set(9, "");
			} else if (syns.length == 1) {
				String syn = syns[0];
				if (!syn.equals(flowable.getName().toLowerCase())) {
					getColumnValues().set(9, syn);
				}
			} else if (syns.length > 1) {
				StringBuilder b = new StringBuilder();
				b.append(syns[0]);
				for (int i = 1; i < syns.length; i++) {
					String syn = syns[i];
					if (!syn.equals(flowable.getName().toLowerCase())) {
						b.append(" -or- " + syns[i]);
					}
				}
				getColumnValues().set(9, b.toString());
			}
		}

		public void setMatchStatus(MatchStatus matchStatus) {
			this.matchStatus = matchStatus;
			clearMatchColumns();
			getColumnValues().set(matchStatus.getValue(), matchStatus.getSymbol());
		}

		private void setBlankData(int columnCount) {
			setColumnValues(new ArrayList<String>());
			for (int i = 0; i < columnCount; i++) {
				getColumnValues().add(i, "");
			}
		}

		private void clearMatchColumns() {
			for (int i = 0; i < 6; i++) {
				getColumnValues().set(i, "");
			}
		}
	}
}
