package gov.epa.nrmrl.std.lca.ht.csvFiles;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

import harmonizationtool.model.DataRow;
import harmonizationtool.model.TableProvider;

public class CSVTableProvider extends TableProvider {
	private DataRow statusRow;

	public void addStatusRow(DataRow dataRow) {
		if (statusRow == null) {
			setStatusRow(dataRow);
			getData().add(0, dataRow);
		} else {
			statusRow = dataRow;
		}
	}

	public int getIndex(DataRow dataRow) {
		return getData().indexOf(dataRow) - 1;
	}

	public int getUriIndex(Resource uri) {
		return getUriList().indexOf(uri) - 1;
	}

	public DataRow getStatusRow() {
		return statusRow;
	}

	public void setStatusRow(DataRow statusRow) {
		if (statusRow == null) {
			addStatusRow(statusRow);
		} else {
			this.statusRow = statusRow;
		}
	}

	public DataRow getBlankStatusRow() {
		DataRow result = new DataRow();
		for (int i = 0; i < getHeaderRow().getSize(); i++) {
			result.add("");
		}
		return result;
	}

	public void addBlankStatusRow() {
		if (statusRow != null) {
			statusRow = getBlankStatusRow();
			return;
		}
		addStatusRow(getBlankStatusRow());
	}
}
