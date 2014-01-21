package harmonizationtool.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.layout.GridLayout;

//import sun.misc.Regexp;
import swing2swt.layout.BoxLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.widgets.Button;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.jface.viewers.CheckboxTableViewer;

public class ITableProvider {
	private List<String> headerNames = null;
	private List<DataRow> data = new ArrayList<DataRow>();

	// private List<String> headerXformNames = null;
	private DataRow headerXformNames = null;
	private List<DataRow> dataXform = new ArrayList<DataRow>();
	private List<String> subRowNames = null;
	private static String subRowPrefix = "sr";
	private static String subRowNameHeader = "srName";

	private int subRowCount = 0;

	// private Table table_2;
	// private Table table;
	// private Table table_1;

	// THIS CLASS HELPS CONVERT THE RESULTS OF A SPARQL QUERY TO
	// AN INTERLEAVED TABLE
	// IN WHICH COLUMS WITH SIMILAR RESULTS APPEAR AS A GROUPS OF ROWS
	// AN INPUT ROW CONTAINS THINGS LIKE:
	// - sr1_srName => THE NAME OF SUBROW 1 <-- srName DEFINED ABOVE AS
	// subRowNameHeader
	// - sr[n]_srName => THE NAME OF SUBROW n
	// - sr1_substance => A COLUMN thing FOR SUBROW 1
	// - sr[n]_substance => A COLUMN thing FOR SUBROW n
	// - sr[n]_[field] => A COLUMN [field] FOR SUBROW n
	/**
	 * @wbp.parser.entryPoint
	 */
	// public void gridDisplay(Shell shell) {
	// Display display = new Display();
	// // Shell shell = new Shell(display);
	// shell.setBounds(10, 10, 200, 250);
	// Composite composite = new Composite(shell, SWT.NONE);
	// // composite.setLayout(new BoxLayout(BoxLayout.X_AXIS));
	// composite.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
	//
	// table_2 = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
	// table_2.setHeaderVisible(true);
	// table_2.setLinesVisible(true);
	//
	// TableColumn tblclmnCol = new TableColumn(table_2, SWT.NONE);
	// tblclmnCol.setWidth(100);
	// tblclmnCol.setText("Col1");
	//
	// TableColumn tblclmnCol_1 = new TableColumn(table_2, SWT.NONE);
	// tblclmnCol_1.setWidth(100);
	// tblclmnCol_1.setText("Col2");
	//
	// TableItem tableItem = new TableItem(table_2, SWT.NONE);
	// tableItem.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
	// tableItem.setText("New TableItem");
	//
	// TableItem tableItem_1 = new TableItem(table_2, SWT.NONE);
	// tableItem_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_MAGENTA));
	// tableItem_1.setText("New TableItem");
	//
	// CheckboxTableViewer checkboxTableViewer =
	// CheckboxTableViewer.newCheckList(composite, SWT.BORDER |
	// SWT.FULL_SELECTION);
	// table_1 = checkboxTableViewer.getTable();
	//
	// TableViewerColumn tableViewerColumn = new
	// TableViewerColumn(checkboxTableViewer, SWT.NONE);
	// TableColumn tblclmnJface = tableViewerColumn.getColumn();
	// tblclmnJface.setWidth(100);
	// tblclmnJface.setText("jface 1");
	//
	// TableViewer tableViewer = new TableViewer(composite, SWT.BORDER |
	// SWT.FULL_SELECTION);
	// table = tableViewer.getTable();
	//
	// //
	// // table.addListener(SWT.MeasureItem, new Listener() {
	// // public void handleEvent(Event event) {
	// // int clientWidth = table.getClientArea().width;
	// // event.height = event.gc.getFontMetrics().getHeight() * 2;
	// // event.width = clientWidth * 2;
	// // }
	// //
	// // @Override
	// // public void handleEvent1(Event event) {
	// // // TODO Auto-generated method stub
	// //
	// // }
	// // });
	// shell.open();
	// while (!shell.isDisposed()) {
	// if (!display.readAndDispatch())
	// display.sleep();
	// }
	// display.dispose();
	// // super(parentShell);
	// // fine;
	// // newFileMD = true;
	// // newDataSet = true;
	// // dataSetEnabled = true;
	// // assert fileMD != null : "fileMD cannot be null";
	// // this.fileMD = fileMD; // SET LOCAL VERSION
	// // tempDataSetProvider = new DataSetProvider();
	// // this.dataSetMD = new DataSetMD();
	// // tempDataSetProvider.setDataSetMD(dataSetMD);
	// // this.curatorMD = new CuratorMD(true);
	// // tempDataSetProvider.setCuratorMD(curatorMD);
	// // tempDataSetProvider.addFileMD(fileMD); // THIS MEANS WE DON'T HAVE TO
	// // // ADD IT AGAIN
	// // dataSetProvider = tempDataSetProvider;
	// // curatorFromPrefs();
	// }

	public void addDataRow(DataRow dataRow) {
		data.add(dataRow);
	}

	public void addDataXformRows(DataRow dataRow) {

		if (subRowCount < 2) {
			return; // THIS ONLY ADDS ROWS IF THERE ARE 2 OR MORE subRowNames
		}
		if (headerNames.size() != dataRow.getSize()) {
			return; // GOT TO HAVE ALIGNMENT HERE
		}
		for (int i = 0; i < subRowCount; i++) {
			DataRow dataXformRow = new DataRow();
			dataXformRow.add("placeholder"); // WILL ADD NAME WHEN WE FIND IT
			for (int j = 0; j < dataRow.getSize(); j++) {
				String cell = dataRow.get(j);
				String header = headerNames.get(j);
				int headerSplitPoint = header.indexOf("_");
				if (headerSplitPoint == -1) {
					dataXformRow.add(cell);
				} else {
					String headerPrefix = header.substring(0, headerSplitPoint);
					if (headerPrefix.startsWith(subRowPrefix)) {
						String headerPrefixNumString = headerPrefix
								.substring(subRowPrefix.length() + 1);
						if (headerPrefixNumString.matches("^\\d+$")) {
							int headerPrefixNum = Integer
									.parseInt(headerPrefixNumString);
							String headerField = header.substring(headerPrefix
									.length());
							if (headerPrefixNum == i + 1) {
								if (headerField.equals(subRowNameHeader)) {
									dataXformRow.set(0, cell);
								} else {
									dataXformRow.add(cell);
								}
							}
						}
					}
				}
			}
			dataXform.add(dataXformRow);
			System.out.println("Now dataXform has this many rows: "
					+ dataXform.size());
		}
	}

	public List<DataRow> getData() {
		return data;
	}

	public List<DataRow> getDataXform() {

		return dataXform;
	}

	public int getIndex(DataRow dataRow) {
		return data.indexOf(dataRow);
	}

	public List<String> getHeaderNames() {
		return headerNames;
	}

	public void setHeaderNames(DataRow columnHeaders) {
		headerNames = columnHeaders.getColumnValues();
	}

	// public List<String> getHeaderXformNames() {
	// return headerXformNames;
	// }

	// public List<String> getSubRowNames() {
	// return subRowPrefixes;
	// }

	public void setXformNames(List<String> headerNames) {
		this.headerNames = headerNames;
		if (headerXformNames == null) {
			headerXformNames = new DataRow();
		} else {
			headerXformNames.clear();
		}
		headerXformNames.add("DataSet"); // THE FIRST COLUMN IS ALWAYS THE
											// DataSet NAME
		for (String header : headerNames) {
			int headerSplitPoint = header.indexOf("_");
			if (headerSplitPoint == -1) {
				headerXformNames.add(header); // NOT A subRowHeader. ADD IT
			} else {
				String headerPrefix = header.substring(0, headerSplitPoint);
				String headerField = header.substring(headerSplitPoint + 1);
				if (headerField.equals(subRowNameHeader)) { // ALREADY HAVE DataSet FIELD
					continue;
				}
				if (headerPrefix.matches(subRowPrefix + "\\d+$")) {
					if (headerPrefix.equals(subRowPrefix + "1")) {
						headerXformNames.add(headerField);
						System.out.println("Added : " + headerField);
					}
				} else {
					headerXformNames.add(header); // NOT A subRowHeader. ADD IT
				}
			}
		}
	}

	public void setColumnNames(List<String> columnNames) {
		headerNames = columnNames;
		// if (headerNames == null) {
		// headerNames = new ArrayList<String>();
		// } else {
		// headerNames.clear();
		// }
		// for (String name : columnNames) {
		// headerNames.add(name);
		// }
	}

	public static String getSubRowPrefix() {
		return subRowPrefix;
	}

	public static String getsubRowNameHeader() {
		return subRowNameHeader;
	}

	public DataRow getColumnXformHeaders() {
		System.out.println("Size of headerNames" + headerNames.size());
		setXformNames(headerNames);
		return headerXformNames;
	}

	public List<String> getDataXformAsStrings() {
		List<String> results = new ArrayList<String>();
		for (DataRow dataRow : dataXform) {
			String row = dataRow.join("\t");
			results.add(row);
		}
		// TODO Auto-generated method stub
		return results;
	}
}
