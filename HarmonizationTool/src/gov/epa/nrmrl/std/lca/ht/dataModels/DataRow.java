package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.FlowContext;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.FlowProperty;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.Flowable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;

public class DataRow {
	private List<String> columnValues = new ArrayList<String>();

	private Flowable flowable = null;
	private FlowContext flowContext = null;
	private FlowProperty flowUnit = null;

	private int rowNumber;
	private String rowToolTip;
	private Resource openLCASourceURI = null;

	public DataRow() {
	}

	@Override
	public String toString() {
		return "DataRow [columnValues=" + columnValues + "]";
	}

	public Iterator<String> getIterator() {
		return columnValues.iterator();
	}

	public int getSize() {
		if (columnValues == null) {
			return -1;
		}
		return columnValues.size();
	}

	public void setColumnValues(List<String> columnValues) {
		this.columnValues = columnValues;
	}

	public List<String> getColumnValues() {
		return columnValues;
	}

	public void add(String s) {
		columnValues.add(s);
	}

	public void set(int index, String string) {
		columnValues.set(index, string);
	}

	// public void setToolTipValue(int index, String string){
	// while (toolTipValues.size() <= index){
	// toolTipValues.add("");
	// }
	// toolTipValues.set(index, string);
	// }

	public String get(int index) {
		if (index < 0 || index >= columnValues.size()) {
			return null;
		}
		return columnValues.get(index);
	}

	public String getCSVTableIndex(int i) {
		if (i < 1 || i > columnValues.size()) {
			return null;
		}
		return columnValues.get(i - 1);
	}

	// public String getToolTipValue(int index) {
	// while (toolTipValues.size() <= index){
	// toolTipValues.add("");
	// }
	// return toolTipValues.get(index);
	// }

	public String join(String delimiter) {
		if (columnValues.isEmpty()) {
			return "";
		}
		String joinedRow = columnValues.get(0);
		for (int i = 1; i < columnValues.size(); i++) {
			joinedRow += delimiter + columnValues.get(i);
		}
		return joinedRow;
	}

	public void clear() {
		columnValues.clear();
		// toolTipValues.clear();
	}

	// public List<String> getToolTipValues() {
	// while (toolTipValues.size() < columnValues.size()){
	// toolTipValues.add("");
	// }
	// return toolTipValues;
	// }

	// public void setToolTipValues(List<String> toolTipValues) {
	// while (toolTipValues.size() < columnValues.size()){
	// toolTipValues.add("");
	// }
	// this.toolTipValues = toolTipValues;
	// }

	public String getRowToolTip() {
		return rowToolTip;
	}

	public void setRowToolTip(String rowToolTip) {
		this.rowToolTip = rowToolTip;
	}

	public int getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(int rowNumber) {
		this.rowNumber = rowNumber;
	}

	public Flowable getFlowable() {
		return flowable;
	}

	public void setFlowable(Flowable flowable) {
		this.flowable = flowable;
	}

	public FlowContext getFlowContext() {
		return flowContext;
	}

	public void setFlowContext(FlowContext flowContext) {
		this.flowContext = flowContext;
	}

	public FlowProperty getFlowUnit() {
		return flowUnit;
	}

	public void setFlowUnit(FlowProperty flowUnit) {
		this.flowUnit = flowUnit;
	}

	public Resource getSourceFlowTDBResource() {
		return openLCASourceURI;
	}

	public void setSourceFlowTDBResource(Resource sourceFlowTDBResource) {
		this.openLCASourceURI = sourceFlowTDBResource;
	}
}
