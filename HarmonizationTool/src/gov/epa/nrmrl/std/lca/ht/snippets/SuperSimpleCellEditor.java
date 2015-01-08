package gov.epa.nrmrl.std.lca.ht.snippets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * This class demonstrates CellEditors. It allows you to create and edit Person objects
 */
public class SuperSimpleCellEditor extends ApplicationWindow {
	// Table column names/properties
	public static final String NAME = "Name";

	public static final String[] PROPS = { NAME };

	// The data model
	private java.util.List people;

	/**
	 * Constructs a PersonEditor
	 */
	public SuperSimpleCellEditor() {
		super(null);
		people = new ArrayList();
	}

	/**
	 * Runs the application
	 */
	public void run() {
		// Don't return from open() until window closes
		setBlockOnOpen(true);

		// Open the main window
		open();

		// Dispose the display
		Display.getCurrent().dispose();
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Person Editor");
		shell.setSize(400, 400);
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		final TableViewer tv = new TableViewer(composite, SWT.FULL_SELECTION);
		tv.setContentProvider(new PersonContentProvider());
		tv.setLabelProvider(new PersonLabelProvider());
		tv.setInput(people);

		Table table = tv.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		new TableColumn(table, SWT.CENTER).setText(NAME);

		for (int i = 0, n = table.getColumnCount(); i < n; i++) {
			table.getColumn(i).pack();
		}

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		Person p = new Person();
		p.setName("Name");
		people.add(p);

		// Create the cell editors
		CellEditor[] editors = new CellEditor[1];
		editors[0] = new TextCellEditor(table);

		// Set the editors, cell modifier, and column properties
		tv.setColumnProperties(PROPS);
		tv.getTable().getColumn(0).setWidth(500);
		tv.setCellModifier(new PersonCellModifier(tv));
		tv.setCellEditors(editors);
		tv.refresh();

		return composite;
	}

	public static void main(String[] args) {
		new SuperSimpleCellEditor().run();
	}
}

class PersonCellModifier implements ICellModifier {
	private Viewer viewer;

	public PersonCellModifier(Viewer viewer) {
		this.viewer = viewer;
	}

	public boolean canModify(Object element, String property) {
		// Allow editing of all values
		return true;
	}

	public Object getValue(Object element, String property) {
		System.out.println("get element = "+element);
		Person p = (Person) element;
		if (SuperSimpleCellEditor.NAME.equals(property))
			return p.getName();
		else
			return null;
	}

	public void modify(Object element, String property, Object value) {
		System.out.println("mod element = "+element);

		if (element instanceof Item)
			element = ((Item) element).getData();

		Person p = (Person) element;
		if (SuperSimpleCellEditor.NAME.equals(property))
			p.setName((String) value);

		// Force the viewer to refresh
		viewer.refresh();
	}
}

class Person {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

class PersonLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		Person person = (Person) element;
		switch (columnIndex) {
		case 0:
			return person.getName();
		}
		return null;
	}

	public void addListener(ILabelProviderListener listener) {
		// Ignore it
	}

	public void dispose() {
		// Nothing to dispose
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// Ignore
	}
}

class PersonContentProvider implements IStructuredContentProvider {
	/**
	 * Returns the Person objects
	 */
	public Object[] getElements(Object inputElement) {
		return ((List) inputElement).toArray();
	}

	/**
	 * Disposes any created resources
	 */
	public void dispose() {
		// Do nothing
	}

	/**
	 * Called when the input changes
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// Ignore
	}
}
