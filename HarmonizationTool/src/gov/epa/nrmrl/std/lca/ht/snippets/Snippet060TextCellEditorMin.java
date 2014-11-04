package gov.epa.nrmrl.std.lca.ht.snippets;

/*******************************************************************************
 * Copyright (c) 2008, 2014 Software Competence Center Hagenberg (SCCH) GmbH
 * Copyright (c) 2008 Mario Winterer
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Lars Vogel (lars.vogel@gmail.com) - Bug 413427
 *******************************************************************************/

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

/**
 * Shows how to attach content assist to a text cell editor.
 * 
 * @author Mario Winterer
 */
public class Snippet060TextCellEditorMin {
	private static class Color {
		public String name;

		public Color(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static class TextCellEditorWithContentProposal extends TextCellEditor {

		public TextCellEditorWithContentProposal(Composite parent, KeyStroke keyStroke, char[] autoActivationCharacters) {
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

	public static class CellEditingSupport extends EditingSupport {
		private TextCellEditorWithContentProposal cellEditor;

		public CellEditingSupport(TableViewer viewer) {
			super(viewer);

			// IContentProposalProvider contentProposalProvider = new SimpleContentProposalProvider(new String[] {
			// "red",
			// "green", "blue" });
			cellEditor = new TextCellEditorWithContentProposal(viewer.getTable(), null, null);
		}

		@Override
		protected boolean canEdit(Object element) {
			return (element instanceof Color);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return cellEditor;
		}

		@Override
		protected Object getValue(Object element) {
			System.out.println("get element = " + element);
			return ((Color) element).name;
		}

		@Override
		protected void setValue(Object element, Object value) {
			System.out.println("set element = " + element);
			System.out.println("set element.getClass() = " + element.getClass());
			((Color) element).name = value.toString();
			getViewer().update(element, null);
		}

	}

	public Snippet060TextCellEditorMin(Shell shell) {
		final TableViewer viewer = new TableViewer(shell, SWT.BORDER | SWT.FULL_SELECTION);
		final Table table = viewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		final TableViewerColumn colorColumn = new TableViewerColumn(viewer, SWT.LEFT);
		colorColumn.getColumn().setText("Color name");
		colorColumn.getColumn().setWidth(200);
		colorColumn.setLabelProvider(new ColumnLabelProvider());
		colorColumn.setEditingSupport(new CellEditingSupport(viewer));

		viewer.setContentProvider(new ArrayContentProvider());

		ColumnViewerEditorActivationStrategy activationSupport = new ColumnViewerEditorActivationStrategy(viewer) {
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
		FocusCellHighlighter focusCellHighlighter = new FocusCellOwnerDrawHighlighter(viewer);
		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(viewer, focusCellHighlighter);

		TableViewerEditor.create(viewer, focusCellManager, activationSupport, ColumnViewerEditor.TABBING_VERTICAL
				| ColumnViewerEditor.KEYBOARD_ACTIVATION);

		viewer.setInput(createModel());
	}

	private Color[] createModel() {
		return new Color[] { new Color("red"), new Color("green") };
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet060TextCellEditorMin(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}
}
