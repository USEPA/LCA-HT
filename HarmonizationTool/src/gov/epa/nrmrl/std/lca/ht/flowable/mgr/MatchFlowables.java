package gov.epa.nrmrl.std.lca.ht.flowable.mgr;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.FillLayout;

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
	private static int searchRow = 1;
	private static boolean editingInProgress = false;
	private static boolean justUpdated = false;
	private static Button addToMaster;

	private static int maxSearchResults = 50;
	public static Text editorText;

	// private static FlowableTableRow[] flowableTableRows;
	private static List<FlowableTableRow> flowableTableRows;

	// private static Text[] searchText = new Text[11];
	// private static TableEditor[] searchEditor = new TableEditor[11];
	// private static TableEditor editor;

	// private static TextCellEditor editor;
	// private static TableEditor editor;

	public MatchFlowables() {
	}

	@Override
	public void createPartControl(Composite parent) {
		outerComposite = new Composite(parent, SWT.V_SCROLL);
		outerComposite.setLayout(new GridLayout(1, false));
		System.out.println("hello, from sunny MatchFlowables!");
		initializeTableViewer(outerComposite);
		initialize();

	}

	private static void initializeTableViewer(Composite composite) {

		Composite innerComposite = new Composite(outerComposite, SWT.NONE);
		innerComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gridData.heightHint = 20;
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

		// TODO - ADD THIS BUTTON (BELOW) AND IMPLEMENT.
		addToMaster = new Button(innerComposite, SWT.NONE);
		addToMaster.setText("Add to Master");
		addToMaster.setVisible(true);
		addToMaster.addSelectionListener(addToMasterListener);

		tableViewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);
		tableViewer.setContentProvider(new ArrayContentProvider());

		ColumnViewerEditorActivationStrategy activationSupport = new ColumnViewerEditorActivationStrategy(tableViewer) {
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				justUpdated = false;
				System.out.println("=================== Caught by isEditorActivationEvent");
				if (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION) {
					System.out.println("======= MOUSE_CLICK_SELECTION ========");
				} else if (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED) {
					System.out.println("======= KEY_PRESSED ========");
				} else if (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION) {
					System.out.println("======= MOUSE_DOUBLE_CLICK_SELECTION ========");
				} else if (event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC) {
					System.out.println("======= PROGRAMMATIC ========");
				} else if (event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL) {
					System.out.println("======= TRAVERSAL ========");
				} else {
					System.out.println("======= something else. event.eventType = " + event.eventType);
				}
				ViewerCell viewerCell = (ViewerCell) event.getSource();
				FlowableTableRow flowableTableRow = (FlowableTableRow) viewerCell.getElement();
				int newRowNumSelected = flowableTableRow.getRowNumber();
				int newColNumSelected = viewerCell.getColumnIndex();
				if (newRowNumSelected == 0) {
					table.deselect(rowNumSelected);
					return false;
				} else if ((newRowNumSelected != searchRow) && (newColNumSelected < 6)) {
					rowNumSelected = newRowNumSelected;
					colNumSelected = newColNumSelected;
					assignMatch();
					table.deselect(rowNumSelected);
					return false;
				} else if ((newRowNumSelected != searchRow) && (newColNumSelected > 5)) {
					table.deselect(rowNumSelected);
					return false;
				} else if (newColNumSelected < 6) {
					table.deselect(rowNumSelected);
					return false;
				} else { // BY NOW WE MUST BE IN AN ACTIVE PART OF SEARCH ROW
					if (editingInProgress) {
						String textInEditor = "[editorText is still null]";
						if (editorText != null) {
							textInEditor = editorText.getText();
						}
						if (textInEditor.equals(flowableTableRows.get(searchRow).get(6))) {
							justUpdated = false;
							rowNumSelected = newRowNumSelected;
							colNumSelected = newColNumSelected;
							return true;
						}
						String textInLastCell = "[no previously selected cell]";
						if (rowNumSelected > 0 && colNumSelected > 0) {
							textInLastCell = flowableTableRows.get(rowNumSelected).get(colNumSelected);
						}

						String textInNewCell = flowableTableRows.get(newRowNumSelected).get(newColNumSelected);

						System.out.println("textInEditor: " + textInEditor);
						System.out.println("textInLastCell: " + textInLastCell);
						System.out.println("textInNewCell: " + textInNewCell);

						// if (editingInProgress) {
						// table.getItem(rowNumSelected).setText(colNumSelected, textInEditor);
						flowableTableRows.get(rowNumSelected).set(colNumSelected, textInEditor);

						// editorText
						// System.out.println("editorText.getText() = " + editorText.getText());
						editorText.setText(textInNewCell);
						justUpdated = true;
					}
					rowNumSelected = newRowNumSelected;
					colNumSelected = newColNumSelected;
					if (newColNumSelected == 6) {
						findMatches();
						justUpdated = false;
						editingInProgress = false;
						System.out.println("Setting editingInProgress to: " + editingInProgress);
						return false;
					} else {
						editingInProgress = true;
						System.out.println("Setting editingInProgress to: " + editingInProgress);

						return true;
					}
				}
			}
		};
		activationSupport.setEnableEditorActivationWithKeyboard(true);

		FocusCellHighlighter focusCellHighlighter = new FocusCellOwnerDrawHighlighter(tableViewer);
		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(tableViewer,
				focusCellHighlighter);

		TableViewerEditor.create(tableViewer, focusCellManager, activationSupport, ColumnViewerEditor.TABBING_VERTICAL
				| ColumnViewerEditor.KEYBOARD_ACTIVATION);

	}

	private static void initializeTable() {
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}

	public static void update(int rowNumber) {
		removeColumns();
		createColumns();
		// initialize();
		TableProvider tableProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey());

		DataRow dataRow = tableProvider.getData().get(rowNumber);
		dataTableRowNum = rowNumber;
		flowableToMatch = dataRow.getFlowable();

		flowableToMatch.clearSearchResults();
		if (flowableToMatch == null) {
			return;
		}
		if (flowableToMatch.getTdbResource().hasProperty(LCAHT.hasQCStatus, LCAHT.QCStatusAdHocMaster)) {
			addToMaster.setText("Remove from Master");
//		} else if (flowableToMatch.getTdbResource().hasProperty(LCAHT.hasQCStatus)) {
//			addToMaster.setText("Remove 2");
//		} else if (flowableToMatch.getTdbResource().hasProperty(RDFS.label)) {
//			addToMaster.setText("Remove 3");
		} else {
			addToMaster.setText("Add to Master");
//			updateMatchCounts();
		}
		searchRow = flowableToMatch.getMatchCandidates().size() + 1;
		// flowableToMatch.clearSyncDataFromTDB(); // NECESSARY? GOOD? TODO: CHECK THIS
		LinkedHashMap<Resource, String> matchCandidateResources = flowableToMatch.getMatchCandidates();
		LinkedHashMap<Resource, String> searchResultResources = flowableToMatch.getSearchResults();
		int rowCount = searchRow + 1 + searchResultResources.size();

		if (searchResultResources.size() > maxSearchResults) {
			rowCount = flowableToMatch.getMatchCandidates().size() + 2 + maxSearchResults;
		}

		flowableTableRows = new ArrayList<FlowableTableRow>();
		FlowableTableRow flowableTableRow0 = new FlowableTableRow();
		flowableTableRow0.setFlowable(flowableToMatch);
		flowableTableRow0.setRowNumber(0);
		flowableTableRow0.setValues();
		flowableTableRows.add(flowableTableRow0);

		int row = 1;
		for (Object dFlowableResource : matchCandidateResources.keySet()) {
			Flowable dFlowable = new Flowable((Resource) dFlowableResource);
			FlowableTableRow flowableTableRow = new FlowableTableRow();
			flowableTableRow.setFlowable(dFlowable);
			flowableTableRow.setRowNumber(row);
			flowableTableRow.matchStatus = MatchStatus.getBySymbol(matchCandidateResources.get(dFlowableResource));
			flowableTableRow.setValues();
			flowableTableRows.add(flowableTableRow);
			row++;
		}
		// NOW CREATE THE ROW WITH THE SEARCH MESSAGE
		FlowableTableRow flowableTableRow = new FlowableTableRow();
		flowableTableRow.setRowNumber(row);
		flowableTableRow.getColumnValues().set(6, "Click to Search -->");
		flowableTableRows.add(flowableTableRow);

		// tableViewer.setInput(flowableTableRows);
		// Table fred = table;
		// table.getItemCount();
		// showSearchResults(50);
		tableViewer.setInput(flowableTableRows);

		// Composite thing = table.getParent();
		// thing.setRedraw(true);
		// thing.redraw();
		// Composite thing2 = thing.getParent();
		// thing2.setRedraw(true);
		// thing2.redraw();
		// tableViewer.refresh();
		Point p = table.getParent().getParent().getSize();
		p.y -= 30;
		table.setSize(p);
		table.getItem(0).setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
		table.getItem(searchRow).setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
		updateMatchCounts();

		// tableViewer.refresh();
	}

	private static void removeColumns() {
		table.setRedraw(false);
		while (table.getColumnCount() > 0) {
			table.getColumns()[0].dispose();
		}
		table.setRedraw(true);
	}

	private static void displayNewSearchResults() {
		clearSearchRows();
		LinkedHashMap<Resource, String> searchResultResources = flowableToMatch.getSearchResults();
		int row = searchRow + 1;
		for (Object dFlowableResource : searchResultResources.keySet()) {
			Flowable dFlowable = new Flowable((Resource) dFlowableResource);
			FlowableTableRow flowableTableRow = new FlowableTableRow();
			flowableTableRow.setFlowable(dFlowable);
			flowableTableRow.setRowNumber(row);
			flowableTableRow.matchStatus = MatchStatus.getBySymbol(searchResultResources.get(dFlowableResource));
			flowableTableRow.setValues();
			flowableTableRows.add(flowableTableRow);
			row++;
			if (row >= maxSearchResults + searchRow) {
				continue;
			}
		}
		updateMatchCounts();
		Point p = table.getParent().getParent().getSize();
		p.y -= 30;
		table.setSize(p);
		table.getItem(0).setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
		table.getItem(searchRow).setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
		// tableViewer.refresh();
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

		tableViewerColumn = createTableViewerColumn("Data Source", 200, 6);
		tableViewerColumn.getColumn().setAlignment(SWT.LEFT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(6));

		tableViewerColumn = createTableViewerColumn(Flowable.flowableNameString, 300, 7);
		tableViewerColumn.getColumn().setAlignment(SWT.LEFT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(7));

		tableViewerColumn = createTableViewerColumn(Flowable.casString, 100, 8);
		tableViewerColumn.getColumn().setAlignment(SWT.RIGHT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(8));

		tableViewerColumn = createTableViewerColumn(Flowable.flowableSynonymString, 300, 9);
		tableViewerColumn.getColumn().setAlignment(SWT.LEFT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(9));

		tableViewerColumn = createTableViewerColumn("Other", 200, 10);
		tableViewerColumn.getColumn().setAlignment(SWT.LEFT);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(10));
	}

	private static void updateMatchCounts() {
		Integer[] matchSummary = new Integer[] { 0, 0, 0, 0, 0, 0 };
		boolean noMatch = true;
		if (table.getItemCount() == 0) {
			return;
		}
		LinkedHashMap<Resource, String> candidateMap = flowableToMatch.getMatchCandidates();
		for (String value : candidateMap.values()) {
			int col = MatchStatus.getNumberBySymbol(value);
			if (col > 0 && col < 4) {
				noMatch = false;
			}
			matchSummary[col]++;
		}

		TableItem tableItem = table.getItem(0);
		for (int colNum = 0; colNum < 6; colNum++) {
			tableItem.setText(colNum, matchSummary[colNum].toString());
		}

		Util.findView(FlowsWorkflow.ID);

		if (flowableToMatch.getTdbResource().hasProperty(LCAHT.hasQCStatus, LCAHT.QCStatusAdHocMaster)) {
			FlowsWorkflow.addMatchFlowableRowNum(flowableToMatch.getFirstRow());
			table.getItem(0).setBackground(SWTResourceManager.getColor(SWT.COLOR_CYAN));
		} else if (noMatch) {
			FlowsWorkflow.removeMatchFlowableRowNum(flowableToMatch.getFirstRow());
			table.getItem(0).setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));

		} else {
			FlowsWorkflow.addMatchFlowableRowNum(flowableToMatch.getFirstRow());
			table.getItem(0).setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
		}
		tableViewer.refresh();
	}

	public static int getCsvTableRowNum() {
		return dataTableRowNum;
	}

	public static void setCsvTableRowNum(int csvTableRowNum) {
		MatchFlowables.dataTableRowNum = csvTableRowNum;
	}

	protected static void assignMatch() {
		LinkedHashMap<Resource, String> candidateMap = flowableToMatch.getMatchCandidates();
		LinkedHashMap<Resource, String> searchMap = flowableToMatch.getSearchResults();
		FlowableTableRow flowableTabRow = (FlowableTableRow) table.getItem(rowNumSelected).getData();
		Flowable matchingFlowable = flowableTabRow.getFlowable();
		if (rowNumSelected < searchRow) {
			String curSymbol = candidateMap.get(flowableTabRow.getFlowable().getTdbResource());
			int curCol = MatchStatus.getNumberBySymbol(curSymbol);

			TableItem tableItem = table.getItem(rowNumSelected);
			FlowableTableRow flowableTableRow = (FlowableTableRow) tableItem.getData();

			tableItem.setText(curCol, "");
			flowableTableRow.set(curCol, "");

			String newString = MatchStatus.getByValue(colNumSelected).getSymbol();
			tableItem.setText(colNumSelected, newString);
			flowableTableRow.set(colNumSelected, newString);
			candidateMap.put(flowableTabRow.getFlowable().getTdbResource(), newString);

		} else {
			String curSymbol = searchMap.get(flowableTabRow.getFlowable().getTdbResource());
			int curCol = MatchStatus.getNumberBySymbol(curSymbol);

			TableItem tableItem = table.getItem(rowNumSelected);
			FlowableTableRow flowableTableRow = (FlowableTableRow) tableItem.getData();

			tableItem.setText(curCol, "");
			flowableTableRow.set(curCol, "");

			String newString = MatchStatus.getByValue(colNumSelected).getSymbol();
			tableItem.setText(colNumSelected, newString);
			flowableTableRow.set(colNumSelected, newString);
			if (colNumSelected > 0) {
				candidateMap.put(flowableTabRow.getFlowable().getTdbResource(), newString);
			} else {
				candidateMap.remove(flowableTabRow.getFlowable().getTdbResource());
			}
			searchMap.put(flowableTabRow.getFlowable().getTdbResource(), newString);
		}
		// CSVTableView.colorOneFlowableRow(flowableToMatch.getFirstRow());
		updateMatchCounts();
	}

	@Override
	public void setFocus() {
		System.out.println("We got focus!");
		editingInProgress = false;
		System.out.println("Setting editingInProgress to: " + editingInProgress);

	}

	private static void clearSearchRows() {
		System.out.println("flowableTableRows is = " + flowableTableRows);
		System.out.println("flowableTableRows.length was = " + flowableTableRows.size());
		int totalRows = flowableTableRows.size();
		for (int i = totalRows - 1; i > searchRow; i--) {
			flowableTableRows.remove(i);
		}
		System.out.println("flowableTableRows.length now = " + flowableTableRows.size());
	}

	private static void findMatches() {
		TableItem tableItem = table.getItem(searchRow);
		FlowableTableRow flowableTableRow = (FlowableTableRow) tableItem.getData();

		String nameSearch = flowableTableRow.get(7);
		String casSearch = flowableTableRow.get(8);
		String synSearch = flowableTableRow.get(9);
		String otherSearch = flowableTableRow.get(10);
		// WORK HERE

		StringBuilder b = new StringBuilder();
		b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
		b.append("PREFIX  fedlca: <http://epa.gov/nrmrl/std/lca/fedlca/1.0#> \n");
		b.append("PREFIX  lcaht: <http://epa.gov/nrmrl/std/lca/ht/1.0#> \n");
		b.append("PREFIX  afn:    <http://jena.hpl.hp.com/ARQ/function#> \n");
		b.append("PREFIX  fn:     <http://www.w3.org/2005/xpath-functions#> \n");
		b.append("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
		b.append("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
		b.append("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
		b.append("PREFIX  xml:    <http://www.w3.org/XML/1998/namespace> \n");
		b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
		b.append("PREFIX  dcterms: <http://purl.org/dc/terms/> \n");
		b.append(" \n");
		b.append("SELECT distinct ?f  \n");
		b.append("WHERE \n");
		b.append("  { \n");
		b.append("    ?f rdfs:label ?name . \n");
		b.append("    optional {?f eco:casNumber ?cas . }\n");
		b.append("    optional {?f skos:altName ?syn . }\n");
		b.append("    optional {?f ?p ?other . }\n");
		b.append("    ?f a eco:Flowable . \n");

		if (!casSearch.matches("^\\s*$")) {
			String casRegex = star2regex(casSearch);
			b.append("    filter regex(str(?cas),\"" + casRegex + "\",\"i\") \n");
		}
		if (!nameSearch.matches("^\\s*$")) {
			String nameRegex = star2regex(nameSearch);
			b.append("    filter regex(str(?name),\"" + nameRegex + "\",\"i\") \n");
		}
		if (!synSearch.matches("^\\s*$")) {
			String synRegex = star2regex(synSearch);
			b.append("    filter regex(str(?syn),\"" + synRegex + "\",\"i\") \n");
		}
		if (!otherSearch.matches("^\\s*$")) {
			String otherRegex = star2regex(otherSearch);
			b.append("    filter regex(str(?other),\"" + otherRegex + "\",\"i\") \n");
		}
		b.append("   } \n");
		String query = b.toString();
		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);
		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		System.out.println("resultSet = " + resultSet);
		flowableToMatch.clearSearchResults();
		// resetTable();
		LinkedHashMap<Resource, String> candidateMap = flowableToMatch.getMatchCandidates();
		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			RDFNode rdfNode = querySolution.get("f");
			if (flowableToMatch.getTdbResource().equals(rdfNode)) {
				continue;
			}
			if (flowableToMatch.getTdbResource().equals(rdfNode)) {
				continue;
			}
			if (candidateMap.containsKey(rdfNode)) {
				continue;
			}
			// Flowable flowable = new Flowable(rdfNode.asResource());
			flowableToMatch.addSearchResult(rdfNode.asResource());
		}
		displayNewSearchResults();
		// appendSearchResults(50);
	}

	private static void resetTable() {
		int i = table.getItemCount() - 1;
		table.remove(flowableToMatch.getMatchCandidates().size() + 2, i);
	}

	private static String star2regex(String typicalWildcards) {
		String part1 = "";
		if (typicalWildcards.substring(0, 1).equals("*")) {
			part1 = typicalWildcards.substring(1);
		} else {
			part1 = "^" + typicalWildcards;
		}

		String part2 = "";
		if (typicalWildcards.substring(typicalWildcards.length() - 1, typicalWildcards.length()).equals("*")) {
			part2 = part1.substring(0, part1.length() - 1);
		} else {
			part2 = part1 + "$";
		}
		// TODO : ESCAPE VARIOUS CHARACTERS THAT MIGHT BE TREATED AS REGEX MATCHES

		return part2;
	}

	private static class MyColumnLabelProvider extends ColumnLabelProvider {
		private int dataColumnNumber;

		public MyColumnLabelProvider(int colNum) {
			this.dataColumnNumber = colNum;
		}

		// public void setText(Object element, String ){
		//
		// }

		@Override
		public String getText(Object element) {
			FlowableTableRow flowableTableRow = null;
			try {
				flowableTableRow = (FlowableTableRow) element;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("element= " + element);
			}
			String s = "";
			try {
				int size = flowableTableRow.getColumnValues().size();
				if (dataColumnNumber < size) {
					s = flowableTableRow.getColumnValues().get(dataColumnNumber);
				}
			} catch (Exception e) {
				System.out.println("dataRow=" + flowableTableRow);
				e.printStackTrace();
			}
			return s;
		}
	}

	// private static class RowIndexColumnLabelProvider extends ColumnLabelProvider {
	//
	// public RowIndexColumnLabelProvider() {
	// }
	//
	// @Override
	// public String getToolTipText(Object element) {
	// DataRow dataRow = null;
	// try {
	// dataRow = (DataRow) element;
	// } catch (Exception e) {
	// e.printStackTrace();
	// System.out.println("element= " + element);
	// }
	// String t = "";
	// try {
	// t = dataRow.getRowToolTip();
	//
	// } catch (Exception e) {
	// System.out.println("dataRow=" + dataRow);
	// e.printStackTrace();
	// }
	// return t;
	// }
	//
	// @Override
	// public Point getToolTipShift(Object object) {
	// return new Point(5, 5);
	// }
	//
	// @Override
	// public int getToolTipDisplayDelayTime(Object object) {
	// return 100; // msec
	// }
	//
	// @Override
	// public int getToolTipTimeDisplayed(Object object) {
	// return 5000; // msec
	// }
	//
	// @Override
	// public String getText(Object element) {
	// DataRow dataRow = null;
	// try {
	// dataRow = (DataRow) element;
	// } catch (Exception e) {
	// e.printStackTrace();
	// System.out.println("element= " + element);
	// }
	// String s = "";
	// try {
	// // s = dataRow.getRowNumber() + 1 + "";
	// int rowNumPlus1 = dataRow.getRowNumber() + 1;
	// s = rowNumPlus1 + "";
	// } catch (Exception e) {
	// System.out.println("dataRow=" + dataRow);
	// e.printStackTrace();
	// }
	// return s;
	// }
	// }

	private static TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {

		final TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE, colNumber);
		final TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText(title);
		tableColumn.setWidth(bound);
		tableColumn.setResizable(true);
		tableColumn.setMoveable(false);
		// tableColumn.addSelectionListener(colSelectionListener);
		// tableColumn.addListener(SWT.MouseDown, (Listener)
		// columnMouseListener);

		// if (colNumber > 0) {
		// tableColumn.setToolTipText(csvColumnDefaultTooltip);
		// }
		tableViewerColumn.setEditingSupport(new CellEditingSupport(tableViewer));

		return tableViewerColumn;
	}

	private static void createTableViewerMatchColumn(MatchStatus matchStatus) {
		int colNumber = matchStatus.getValue();
		TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE, colNumber);
		TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setText(matchStatus.getSymbol());
		tableColumn.setWidth(20);
		tableColumn.setResizable(true);
		tableColumn.setMoveable(false);
		tableColumn.setToolTipText(matchStatus.getName() + " - " + matchStatus.getComment());
		tableColumn.setAlignment(SWT.CENTER);
		tableViewerColumn.setLabelProvider(new MyColumnLabelProvider(matchStatus.getValue()));
		// tableViewerColumn.getColumn().addSelectionListener(nextSelectionListener);
	}

	// //
	// private static SelectionListener colSelectionListener = new SelectionListener() {
	//
	// private void doit(SelectionEvent e) {
	// // System.out.println("SelectionListener event e= " + e);
	// if (e.getSource() instanceof TableColumn) {
	// // TableColumn col = (TableColumn) e.getSource();
	// // colNumSelected = table.indexOf(col);
	// // if (colNumSelected > 0) {
	// // headerMenu.setVisible(true);
	// // }
	// }
	// }
	//
	// @Override
	// public void widgetSelected(SelectionEvent e) {
	// doit(e);
	// };
	//
	// @Override
	// public void widgetDefaultSelected(SelectionEvent e) {
	// doit(e);
	// }
	// };

	private static SelectionListener nextSelectionListener = new SelectionListener() {

		private void doit(SelectionEvent e) {
			CSVTableView.selectNextFlowable();
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
			if (addToMaster.getText().equals("Add to Master")) {
				while (!flowableToMatch.getTdbResource().hasProperty(LCAHT.hasQCStatus, LCAHT.QCStatusAdHocMaster)) {
					ActiveTDB.tsAddTriple(flowableToMatch.getTdbResource(), LCAHT.hasQCStatus,
							LCAHT.QCStatusAdHocMaster);
				}
				// ActiveTDB.tsReplaceLiteral(flowableToMatch.getTdbResource(), LCAHT.hasQCStatus, "Curated master");
				FlowsWorkflow.addMatchFlowableRowNum(flowableToMatch.getFirstRow());
				table.getItem(0).setBackground(SWTResourceManager.getColor(SWT.COLOR_CYAN));
				addToMaster.setText("Remove from Master");
			} else {
				while (flowableToMatch.getTdbResource().hasProperty(LCAHT.hasQCStatus, LCAHT.QCStatusAdHocMaster)) {
					ActiveTDB.tsRemoveStatement(flowableToMatch.getTdbResource(), LCAHT.hasQCStatus,
							LCAHT.QCStatusAdHocMaster);
				}
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

	private static Composite outerComposite;

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

	// =====================================================
	private static class CellEditingSupport extends EditingSupport {
		public TextCellEditorMod1 cellEditor;

		public CellEditingSupport(TableViewer viewer) {
			super(viewer);

			// IContentProposalProvider contentProposalProvider = new SimpleContentProposalProvider(new String[] {
			// "red",
			// "green", "blue" });
			cellEditor = new TextCellEditorMod1(viewer.getTable(), null, null);
		}

		@Override
		protected boolean canEdit(Object element) {
			return (true);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return cellEditor;
		}

		@Override
		protected Object getValue(Object element) {
			FlowableTableRow flowableTableRow = (FlowableTableRow) element;
			System.out.println("== GET ==");
			editingInProgress = true;
			System.out.println("Setting editingInProgress to: " + editingInProgress);
			return flowableTableRow.get(colNumSelected);
		}

		@Override
		protected void setValue(Object element, Object value) {
			FlowableTableRow flowableTableRow = (FlowableTableRow) element;

			if (value == null) {
				System.out.println("NO OVERWRITE!");
				// tableViewer.refresh();
			} else {

				System.out.println("== SET ==");
				if (editingInProgress && !justUpdated) {
					System.out.println("Still editing");
					flowableTableRow.set(colNumSelected, (String) value);
					editingInProgress = false;
					System.out.println("Setting editingInProgress to: " + editingInProgress);
				} else {
					System.out.println("Not editing");
					// flowableTableRow.set(colNumSelected, (String) value);
				}
				table.deselectAll();
				tableViewer.refresh();
				// getViewer().update(element, null);
			}
		}
	}

	public static class TextCellEditorMod1 extends TextCellEditor {
		public TextCellEditorMod1(Composite parent, KeyStroke keyStroke, char[] autoActivationCharacters) {
			super(parent);
			editorText = this.text;
			// System.out.println("keyStroke = " + keyStroke);
			// System.out.println("autoActivationCharacters" + autoActivationCharacters.toString());
		}

		@Override
		protected void focusLost() {
			System.out.println("Losing focus");
			// System.out.println("keyStroke"+keyStroke);
			editorText = this.text;

			System.out.println("this.text.getText() = " + this.text.getText());
		}

		@Override
		protected boolean dependsOnExternalFocusListener() {
			return false;
		}
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
			int matchStatusCol = -1;
			// String matchStatusSymbol = "";
			if (matchStatus != null) {
				matchStatusCol = matchStatus.getValue();
				if (matchStatusCol > -1) {
					getColumnValues().set(matchStatusCol, matchStatus.getSymbol());
				}
			}

			Flowable flowable = this.getFlowable();
			getColumnValues().set(6, flowable.getDataSource());
			getColumnValues().set(7, flowable.getName());
			getColumnValues().set(8, flowable.getCas());
			String[] syns = flowable.getSynonyms();
			if (syns.length == 0) {
				getColumnValues().set(9, "");
			} else if (syns.length == 1) {
				getColumnValues().set(9, syns[0]);
			} else if (syns.length > 1) {
				StringBuilder b = new StringBuilder();
				b.append(syns[0]);
				for (int i = 1; i < syns.length; i++) {
					b.append(" -or- " + syns[i]);
				}
				getColumnValues().set(9, b.toString());
			}
			// this.setColumnValues(columnValues);
		}

		public void setMatchStatus(MatchStatus matchStatus) {
			this.matchStatus = matchStatus;
		}

		private void setBlankData(int columnCount) {
			setColumnValues(new ArrayList<String>());
			for (int i = 0; i < columnCount; i++) {
				getColumnValues().add(i, "");
			}
		}
	}
}
