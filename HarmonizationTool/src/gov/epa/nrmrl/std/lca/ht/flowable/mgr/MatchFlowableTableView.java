package gov.epa.nrmrl.std.lca.ht.flowable.mgr;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
//import gov.epa.nrmrl.std.lca.ht.snippets.Snippet060TextCellEditorMin.CellEditingSupport;
//import gov.epa.nrmrl.std.lca.ht.snippets.Snippet060TextCellEditorMin.TextCellEditorWithContentProposal;
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import java.util.Iterator;
import java.util.LinkedHashMap;

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
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.FillLayout;

/**
 * @author Tommy E. Cathey and Tom Transue
 * 
 */
public class MatchFlowableTableView extends ViewPart {

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.flowable.MatchFlowableTableView";

	private static TableViewer tableViewer;
	private static Table table;

	private static int rowNumSelected = -1;
	private static int colNumSelected = -1;
	private static Flowable flowableToMatch;
	private static int dataTableRowNum = -1;
	// private static Text[] searchText = new Text[11];
	// private static TableEditor[] searchEditor = new TableEditor[11];
	private static TableEditor editor;

	// private static TextCellEditor editor;
	// private static TableEditor editor;

	public MatchFlowableTableView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		outerComposite = new Composite(parent, SWT.NONE);
		outerComposite.setLayout(new GridLayout(1, false));
		System.out.println("hello, from sunny MatchFlowableTableView!");
		initializeTableViewer(outerComposite);
		// initializePopup(composite);
		initialize();

		// parent.addListener(SWT.Resize, new Listener() {
		// @Override
		// public void handleEvent(Event event) {
		// table.setSize(table.getParent().getSize());
		// }
		// // THIS IS NOT PERFECT
		// // WHEN THE WINDOW IS RESIZED SMALLER, THE TABLE OVER RUNS A LITTLE
		// });

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}

	private static void initializeTableViewer(Composite composite) {

		Composite innterComposite = new Composite(outerComposite, SWT.NONE);
		innterComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData gd_composite_2 = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_composite_2.heightHint = 20;
		innterComposite.setLayoutData(gd_composite_2);
		// innterComposite.setBounds(0, 0, 64, 64);

		Button acceptAdvance = new Button(innterComposite, SWT.NONE);
		acceptAdvance.setText("Next");
		acceptAdvance.addSelectionListener(nextSelectionListener);

		Button addToMaster = new Button(innterComposite, SWT.NONE);
		addToMaster.setText("Add to Master");
		addToMaster.setVisible(false);
		tableViewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);
		// editor = new TableEditor(tableViewer.getTable());
		// editor.horizontalAlignment = SWT.LEFT;
		// editor.grabHorizontal = true;
		// editor.minimumWidth = 50;
		// editor = new TextCellEditor(tableViewer.getTable());
		// editor = new TableEditor(tableViewer.getTable());
		ColumnViewerEditorActivationStrategy activationSupport = new ColumnViewerEditorActivationStrategy(tableViewer) {
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				if (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION) {
					return true;
				}
				return false;
			}
		};
		activationSupport.setEnableEditorActivationWithKeyboard(true);

		/*
		 * Snippet060TextCellEditorWithContentProposal.java Without focus highlighter, keyboard events will not be
		 * delivered to ColumnViewerEditorActivationStragety#isEditorActivationEvent(...) (see above)
		 */
		FocusCellHighlighter focusCellHighlighter = new FocusCellOwnerDrawHighlighter(tableViewer);
		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(tableViewer,
				focusCellHighlighter);

		TableViewerEditor.create(tableViewer, focusCellManager, activationSupport, ColumnViewerEditor.TABBING_VERTICAL
				| ColumnViewerEditor.KEYBOARD_ACTIVATION);

		tableViewer.setContentProvider(new ArrayContentProvider());
		createColumns();
	}

	private static void initializeTable() {
		table = tableViewer.getTable();
		GridData gd_table = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_table.widthHint = 3000;
		gd_table.heightHint = 1500;
		table.setLayoutData(gd_table);
		// table.addListener(SWT.KeyDown, keyListener);
		// table.addSelectionListener(selectionAdapter);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		// table.addSelectionListener(selectionAdapter);
		// table.addListener(SWT.MouseDown, tableMouseListener);
		tableViewer.setInput(table.getItems());
	}

	public static void update(int rowNumber) {
		// initialize();
		TableProvider tableProvider = TableKeeper.getTableProvider(CSVTableView.getTableProviderKey());
		DataRow dataRow = tableProvider.getData().get(rowNumber);
		dataTableRowNum = rowNumber;
		flowableToMatch = dataRow.getFlowable();
		flowableToMatch.clearSearchResults();
		if (flowableToMatch == null) {
			return;
		}

		// flowableToMatch.clearSyncDataFromTDB(); // NECESSARY? GOOD? TODO: CHECK THIS
		// table.removeAll();
		int rowCount = flowableToMatch.getMatchCandidates().size() + 2;
		table.setItemCount(rowCount);
		setResultRowData(0, flowableToMatch);
		table.getItem(0).setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		LinkedHashMap<Resource, String> matchCandidateResources = flowableToMatch.getMatchCandidates();
		if (matchCandidateResources == null) {
			return;
		}
		int row = 1;
		// Integer[] matchSummary = new Integer[] { 0, 0, 0, 0, 0, 0 };
		for (Object dFlowableResource : matchCandidateResources.keySet()) {
			Flowable dFlowable = new Flowable((Resource) dFlowableResource);
			setResultRowData(row, dFlowable);
			String matchStatus = matchCandidateResources.get(dFlowableResource);
			TableItem tableItem = table.getItem(row);
			for (int i = 0; i < 6; i++) { // CLEAR STRINGS
				tableItem.setText(i, "");
			}
			int num = 0;
			if (matchStatus != null) {
				num = MatchStatus.getNumberBySymbol(matchStatus);
			} else {
				matchStatus = "?";
			}
			if (num < 0) {
				num = 0;
			}

			tableItem.setText(num, matchStatus);
			// matchSummary[num]++;
			row++;
		}

		// THE SEARCH ROW (AFTER AUTOMATIC MATCHES)
		TableItem searchRow = table.getItem(matchCandidateResources.size() + 1);
		searchRow.setForeground((SWTResourceManager.getColor(SWT.COLOR_BLUE)));
		searchRow.setText(6, "Click to Search -->");

		// Control oldEditor = editorName.getEditor();
		// if (oldEditor != null) {
		// oldEditor.dispose();
		// }

		// for (int i = 7; i < 11; i++) {
		//
		// searchEditor[i] = new TableEditor(table);
		// Control oldEditor = searchEditor[i].getEditor();
		// if (oldEditor != null) {
		// oldEditor.dispose();
		// }
		// searchEditor[i].horizontalAlignment = SWT.LEFT;
		// searchEditor[i].grabHorizontal = true;
		// searchEditor[i].minimumWidth = 50;
		// searchText[i] = new Text(table, SWT.NONE);
		// searchText[i].setEditable(true);
		// searchText[i].setVisible(true);
		// searchEditor[i].setEditor(searchText[i], searchRow, i);
		// }
		// textName.addModifyListener(new ModifyListener() {
		// public void modifyText(ModifyEvent me) {
		// System.out.println("event: " + me);
		// System.out.println("widget: " + me.widget);
		// // Text text = (Text) editor.getControl();
		// Text text = (Text) editor.getEditor();
		// // if (colNumSelected > 6) {
		// searchRow.setText(colNumSelected, text.getText());
		// // }
		// }
		// });
		// searchText[7].selectAll();
		// searchText[7].setFocus();
		// editorName.setEditor(searchText[7], searchRow, 7);

		// Text nameEditor = new Text(table, SWT.NONE);
		// nameEditor.setText(searchRow.getText(7));
		// nameEditor.addModifyListener(new ModifyListener() {
		// public void modifyText(ModifyEvent me) {
		// Text text = (Text) editor.getEditor();
		// editor.getItem().setText(colNumSelected, text.getText());
		// }
		// });
		// editor.setEditor(nameEditor, searchRow, 7);

		updateMatchCounts();
		showSearchResults(50);
	}

	private static void showSearchResults(int number) {
		LinkedHashMap<Resource, String> searchResultResources = flowableToMatch.getSearchResults();
		if (searchResultResources == null) {
			return;
		}
		int toAdd = number;
		if (searchResultResources.size() < number) {
			toAdd = searchResultResources.size();
		}
		int rowCount = flowableToMatch.getMatchCandidates().size() + 2 + toAdd;
		table.setItemCount(rowCount);

		int row = flowableToMatch.getMatchCandidates().size() + 2;
		for (Object dFlowableResource : searchResultResources.keySet()) {
			Flowable dFlowable = new Flowable((Resource) dFlowableResource);
			setResultRowData(row, dFlowable);
			String matchStatus = searchResultResources.get(dFlowableResource);
			TableItem tableItem = table.getItem(row);
			for (int i = 0; i < 6; i++) { // CLEAR STRINGS
				tableItem.setText(i, "");
			}
			int num = 0;
			if (matchStatus != null) {
				num = MatchStatus.getNumberBySymbol(matchStatus);
			} else {
				matchStatus = "?";
			}
			if (num < 0) {
				num = 0;
			}

			tableItem.setText(num, matchStatus);
			// matchSummary[num]++;
			row++;
			if (row >= rowCount) {
				continue;
			}
		}
		// tableViewer.refresh();
	}

	private static void setResultRowData(int rowNum, Flowable flowable) {
		TableItem qRow = table.getItem(rowNum);
		if (flowable.getDataSource() != null) {
			qRow.setText(6, flowable.getDataSource());
		}
		qRow.setText(7, flowable.getName());
		String casString = flowable.getCas();
		if (casString != null) {
			qRow.setText(8, flowable.getCas());
		}
		String synConcat = "";
		String[] synonyms = flowable.getSynonyms();
		if (synonyms.length > 0) {
			synConcat = synonyms[0];
		}
		for (int i = 0; i < synonyms.length; i++) {
			String synonym = synonyms[i];
			synConcat += " -or- " + synonym;
		}
		qRow.setText(9, synConcat);
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

	/*******************************************************************************
	 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved. This program and the accompanying
	 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
	 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
	 * 
	 * Contributors: IBM Corporation - initial API and implementation
	 *******************************************************************************/

	/*
	 * TableEditor example snippet: edit the text of a table item (in place)
	 * 
	 * For a list of all SWT example snippets see http://www.eclipse.org/swt/snippets/
	 */
	private static SelectionAdapter selectionAdapter = new SelectionAdapter() {
		//
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// Point ptLeft = new Point(1, e.y);
		// Point ptClick = new Point(e.x, e.y);
		// int clickedRow = 0;
		// int clickedCol = 0;
		// TableItem item = table.getItem(ptLeft);
		// if (item == null) {
		// return;
		// }
		// clickedRow = table.indexOf(item);
		// if (clickedRow < 1) {
		// return;
		// }
		// if (clickedRow > table.getItemCount() - 2) {
		// // HANDLE BLANK ROW SEARCH TOOL IF THEY CLICK LAST ROW
		// return;
		// }
		// clickedCol = getTableColumnNumFromPoint(clickedRow, ptClick);
		// if (clickedCol < 0) {
		// return;
		// }
		// rowNumSelected = clickedRow;
		// colNumSelected = clickedCol;
		// System.out.println("Click is rowNum: " + rowNumSelected + ". colNum: " + colNumSelected);
		//
		// // Clean up any previous editor control
		// final TableEditor editor = new TableEditor(table);
		// // The editor must have the same size as the cell and must
		// // not be any smaller than 50 pixels.
		// editor.horizontalAlignment = SWT.LEFT;
		// editor.grabHorizontal = true;
		// editor.minimumWidth = 50;
		// Control oldEditor = editor.getEditor();
		// if (oldEditor != null) {
		// oldEditor.dispose();
		// }
		// // Identify the selected row
		// item = (TableItem) e.item;
		// if (item == null) {
		// return;
		// }
		// // The control that will be the editor must be a child of the Table
		// Text newEditor = new Text(table, SWT.NONE);
		// newEditor.setText(item.getText(colNumSelected));
		//
		// newEditor.addModifyListener(new ModifyListener() {
		// public void modifyText(ModifyEvent me) {
		// Text text = (Text) editor.getEditor();
		// editor.getItem().setText(colNumSelected, text.getText());
		// }
		// });
		// newEditor.selectAll();
		// newEditor.setFocus();
		// editor.setEditor(newEditor, item, colNumSelected);
		// }
		@Override
		public void widgetSelected(SelectionEvent e) {
			// Clean up any previous editor control
			Control oldEditor = editor.getEditor();
			if (oldEditor != null)
				oldEditor.dispose();

			// Identify the selected row
			TableItem item = (TableItem) e.item;
			if (item == null)
				return;

			// The control that will be the editor must be a child of the Table
			Text newEditor = new Text(table, SWT.NONE);
			newEditor.setText(item.getText(7));
			newEditor.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent me) {
					Text text = (Text) editor.getEditor();
					editor.getItem().setText(7, text.getText());
				}
			});
			newEditor.selectAll();
			newEditor.setFocus();
			editor.setEditor(newEditor, item, 7);
		}
	};

	// private static Listener keyListener = new Listener() {
	//
	// @Override
	// public void handleEvent(Event event) {
	// Point ptLeft = new Point(1, event.y);
	// Point ptClick = new Point(event.x, event.y);
	// int clickedRow = 0;
	// int clickedCol = 0;
	// TableItem item = table.getItem(ptLeft);
	// if (item == null) {
	// return;
	// }
	// clickedRow = table.indexOf(item);
	// if (clickedRow < 1) {
	// return;
	// }
	// if (clickedRow > table.getItemCount() - 2) {
	// // HANDLE BLANK ROW SEARCH TOOL IF THEY CLICK LAST ROW
	// return;
	// }
	// clickedCol = getTableColumnNumFromPoint(clickedRow, ptClick);
	// if (clickedCol < 0) {
	// return;
	// }
	// rowNumSelected = clickedRow;
	// colNumSelected = clickedCol;
	//
	// System.out.println("Click is rowNum: " + rowNumSelected + ". colNum: " + colNumSelected);
	// System.out.println("event.keyCode: " + event.keyCode);
	// System.out.println("event.character: " + event.character);
	// if (rowNumSelected == flowableToMatch.getMatchCandidates().size() + 1) {
	// if (colNumSelected > 6) {
	// String text = table.getItem(rowNumSelected).getText(colNumSelected);
	// }
	// }
	//
	// }
	// };

	private static void updateMatchCounts() {
		Integer[] matchSummary = new Integer[] { 0, 0, 0, 0, 0, 0 };
		boolean noMatch = true;
		for (int rowNum = 1; rowNum < table.getItemCount(); rowNum++) {
			TableItem tableItem = table.getItem(rowNum);
			for (int colNum = 0; colNum < 6; colNum++) {
				if (!tableItem.getText(colNum).equals("")) {
					if (colNum > 0 && colNum < 5) {
						noMatch = false;
					}
					matchSummary[colNum]++;
				}
			}
		}
		TableItem tableItem = table.getItem(0);
		for (int colNum = 0; colNum < 6; colNum++) {
			tableItem.setText(colNum, matchSummary[colNum].toString());
		}

		Util.findView(FlowsWorkflow.ID);
		if (noMatch) {
			// FlowsWorkflow.removeMatchFlowableRowNum(dataTableRowNum);
			FlowsWorkflow.removeMatchFlowableRowNum(flowableToMatch.getFirstRow());

		} else {
			FlowsWorkflow.addMatchFlowableRowNum(flowableToMatch.getFirstRow());
			// FlowsWorkflow.addMatchFlowableRowNum(dataTableRowNum);
		}
		// tableViewer.refresh();
		// Util.findView(CSVTableView.ID);
		// CSVTableView.colorOneFlowableRow(dataTableRowNum);
	}

	public static int getCsvTableRowNum() {
		return dataTableRowNum;
	}

	public static void setCsvTableRowNum(int csvTableRowNum) {
		MatchFlowableTableView.dataTableRowNum = csvTableRowNum;
	}

	// private static MouseListener tableMouseListener = new MouseListener() {
	private static Listener tableMouseListener = new Listener() {

		@Override
		public void handleEvent(Event event) {
			System.out.println("cellSelectionMouseDownListener event " + event);
			Point ptLeft = new Point(1, event.y);
			Point ptClick = new Point(event.x, event.y);
			int clickedRow = 0;
			int clickedCol = 0;
			final TableItem item = table.getItem(ptLeft);
			if (item == null) {
				return;
			}
			clickedRow = table.indexOf(item);
			if (clickedRow < 1) {
				return;
			}
			clickedCol = getTableColumnNumFromPoint(clickedRow, ptClick);
			if (clickedCol < 0) {
				return;
			}
			rowNumSelected = clickedRow;
			colNumSelected = clickedCol;

			LinkedHashMap<Resource, String> candidateMap = flowableToMatch.getMatchCandidates();

			if (colNumSelected < 6 && rowNumSelected < candidateMap.size() + 1) {
				for (Resource resource : candidateMap.keySet()) {
					Flowable tempFlowable = new Flowable(resource);
					String source = tempFlowable.getDataSource();
					String name = tempFlowable.getName();
					if (name.equals(item.getText(7)) && source.equals(item.getText(6))) {
						String symbol = candidateMap.get(resource);
						int oldCol = MatchStatus.getNumberBySymbol(symbol);
						item.setText(oldCol, "");
						item.setText(colNumSelected, MatchStatus.getByValue(colNumSelected).getSymbol());
						candidateMap.put(resource, MatchStatus.getByValue(colNumSelected).getSymbol());
						break;
					}
				}
				// Resource flowableCandidateResource = (Resource) candidateMap.keySet().toArray()[rowNumSelected - 1];
				// int oldCol = MatchStatus.getNumberBySymbol(candidateMap.get(flowableCandidateResource));
				// item.setText(oldCol, "");
				// item.setText(colNumSelected, MatchStatus.getByValue(colNumSelected).getSymbol());
				// candidateMap.put(flowableCandidateResource, MatchStatus.getByValue(colNumSelected).getSymbol());
				// Flowable tempFlowable = new Flowable(flowableCandidateResource);
				// // searchText[7].setText(tempFlowable.getName());
				// // searchText[8].setText(tempFlowable.getCas());
				// // searchText[9].setText("");
				// // searchText[10].setText("");
				table.deselectAll();
			} else if (colNumSelected < 6 && rowNumSelected > candidateMap.size() + 1) {
				LinkedHashMap<Resource, String> searchMap = flowableToMatch.getSearchResults();
				for (Resource resource : searchMap.keySet()) {
					Flowable tempFlowable = new Flowable(resource);
					String source = tempFlowable.getDataSource();
					String name = tempFlowable.getName();
					if (name.equals(item.getText(7)) && source.equals(item.getText(6))) {
						String symbol = searchMap.get(resource);
						int oldCol = MatchStatus.getNumberBySymbol(symbol);
						item.setText(oldCol, "");
						item.setText(colNumSelected, MatchStatus.getByValue(colNumSelected).getSymbol());
						candidateMap.put(resource, MatchStatus.getByValue(colNumSelected).getSymbol());
						break;
					}
				}
				table.deselectAll();
			} else if (colNumSelected > 6 && rowNumSelected == candidateMap.size() + 1) {

				// editor = new TableEditor(table);
				// editor.horizontalAlignment = SWT.LEFT;
				// editor.grabHorizontal = true;
				// editor.minimumWidth = 50;
				// Control oldEditor = searchEditor[colNumSelected].getEditor();
				// if (oldEditor != null) {
				// oldEditor.dispose();
				// }

				// searchText[colNumSelected].selectAll();
				// searchText[colNumSelected].setFocus();

				// searchEditor[colNumSelected].setEditor(searchText[colNumSelected], item, colNumSelected);

				// Text newEditor = new Text(table, SWT.NONE);
				// newEditor.setText(item.getText(colNumSelected));
				//
				// newEditor.addModifyListener(new ModifyListener() {
				// public void modifyText(ModifyEvent me) {
				// System.out.println("event: " + me);
				// System.out.println("widget: " + me.widget);
				// // Text text = (Text) editor.getControl();
				// Text text = (Text) editor.getEditor();
				// // if (colNumSelected > 6) {
				// item.setText(colNumSelected, text.getText());
				// // }
				// }
				// });
				// editor.setEditor(newEditor, item, colNumSelected);
			} else if (colNumSelected == 6 && rowNumSelected == candidateMap.size() + 1) {
				System.out.println("Starting search...");
				findMatches();
			}
			// table.getItem(candidateMap.size() + 1).setText(6, "Click to Search -->");
			updateMatchCounts();
			System.out.println("event.widget = " + event.widget);
		}
	};

	private static void findMatches() {
		// for (int i=7;i<11;i++){
		// searchText[i].setVisible(false);
		// }
		// String nameSearch = searchText[7].getText();
		// String casSearch = searchText[8].getText();
		// String synSearch = searchText[9].getText();
		// String otherSearch = searchText[10].getText();

		// String altNameSearch = table.getItem(rowNumSelected).getText(7);
		// String altcasSearch = table.getItem(rowNumSelected).getText(8);
		// String altsynSearch = table.getItem(rowNumSelected).getText(9);
		// String altotherSearch = table.getItem(rowNumSelected).getText(10);

		String nameSearch = table.getItem(rowNumSelected).getText(7);
		String casSearch = table.getItem(rowNumSelected).getText(8);
		String synSearch = table.getItem(rowNumSelected).getText(9);
		String otherSearch = table.getItem(rowNumSelected).getText(10);

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
		resetTable();
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
		showSearchResults(50);
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

	// private static SelectionListener advanceCSVTableListener = new SelectionListener() {
	//
	// private void doit(SelectionEvent e) {
	// TableItem tableItem = table.getItem(0);
	// boolean gotMatch = false;
	// for (int colNum = 1; colNum < 5; colNum++) {
	// if (!tableItem.getText(colNum).equals("")) {
	// gotMatch = true;
	// break;
	// }
	// }
	// Util.findView(CSVTableView.ID);
	// if (gotMatch) {
	// CSVTableView.colorFlowableRows();
	// // FlowsWorkflow.updateFlowableCount();
	// // FIXME
	// // PROBLEM IS THAT ASSIGNMENT HAPPENS BEFORE "ASSIGN" BUTTON
	// }
	// CSVTableView.selectNextFlowable();
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

	private static Composite outerComposite;

	public static void initialize() {
		initializeTable();
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

	private static int getColumnNumSelected(Point point) {
		int clickedRow = getRowNumSelected(point);
		int clickedCol = getTableColumnNumFromPoint(clickedRow, point);
		if (clickedCol < 0) {
			return -1;
		}
		return clickedCol;
	}

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

	private static int getRowNumSelected(Point point) {
		TableItem item = table.getItem(point);
		if (item == null) {
			return -1;
		}
		return table.indexOf(item);
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
		MatchFlowableTableView.flowableToMatch = flowableToMatch;
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
			System.out.println("== GET ==");
			System.out.println("element = " + element);
			System.out.println("cellEditor.getValue() = " + cellEditor.getValue());
			System.out.println("cellEditor.getControl() = " + cellEditor.getControl());

			System.out.println("getViewer = " + getViewer());
			// System.out.println("getViewer = "+value.toString());
			if (element == null) {
				return "hi";
			}
			// return ((Color) element).name;
			return cellEditor.getValue();
		}

		@Override
		protected void setValue(Object element, Object value) {
			System.out.println("== SET ==");
			System.out.println("element = " + element);
			System.out.println("cellEditor.getValue() = " + cellEditor.getValue());
			System.out.println("cellEditor.getControl() = " + cellEditor.getControl());
			System.out.println("value.getClass() = " + value.getClass());

			Control thing = cellEditor.getControl();
			element = value.toString();
			getViewer().update(element, null);
		}

	}

	public static class TextCellEditorMod1 extends TextCellEditor {

		public TextCellEditorMod1(Composite parent, KeyStroke keyStroke, char[] autoActivationCharacters) {
			super(parent);
		}

		@Override
		protected void focusLost() {
		}

		@Override
		protected boolean dependsOnExternalFocusListener() {
			return false;
		}
	}
}
