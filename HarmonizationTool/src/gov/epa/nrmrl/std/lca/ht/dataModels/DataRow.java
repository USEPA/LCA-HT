package gov.epa.nrmrl.std.lca.ht.dataModels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;

public class DataRow {
	private List<String> columnValues = new ArrayList<String>();
	// private List<String> toolTipValues = new ArrayList<String>();
	private List<MatchCandidate> matchCandidates = new ArrayList<MatchCandidate>();
	private Set<Resource> matchCandidateFlowables = new HashSet<Resource>();
	private Set<Resource> matchCandidateFlowContexts = new HashSet<Resource>();
	private Set<Resource> matchCandidateFlowProperties = new HashSet<Resource>();

	private int rowNumber;
	private String rowToolTip;

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

	public List<MatchCandidate> getMatchCandidates() {
		return matchCandidates;
	}

	public void setMatchCandidates(List<MatchCandidate> matchCandidates) {
		this.matchCandidates = matchCandidates;
	}

	public int addMatchCandidate(MatchCandidate matchCandidate) {
		if (matchCandidate != null) {
			matchCandidates.add(matchCandidate);
		}
		return matchCandidates.size();
	}

	public Set<Resource> getMatchCandidateFlowables() {
		return matchCandidateFlowables;
	}

	public void setMatchCandidateFlowables(Set<Resource> matchCandidateFlowables) {
		this.matchCandidateFlowables = matchCandidateFlowables;
	}

	public void addMatchCandidateFlowable(Resource resource) {
		matchCandidateFlowables.add(resource);
	}

	public void removeMatchCandidateFlowable(Resource resource) {
		matchCandidateFlowables.remove(resource);
	}

	// --

	public Set<Resource> getMatchCandidateFlowContexts() {
		return matchCandidateFlowContexts;
	}

	public void setMatchCandidateFlowContexts(Set<Resource> matchCandidateFlowContexts) {
		this.matchCandidateFlowContexts = matchCandidateFlowContexts;
	}

	public void addMatchCandidateFlowContext(Resource resource) {
		matchCandidateFlowContexts.add(resource);
	}

	public void removeMatchCandidateFlowContext(Resource resource) {
		matchCandidateFlowContexts.remove(resource);
	}

	// --

	public Set<Resource> getMatchCandidateFlowProperties() {
		return matchCandidateFlowProperties;
	}

	public void setMatchCandidateFlowProperties(Set<Resource> matchCandidateFlowProperties) {
		this.matchCandidateFlowProperties = matchCandidateFlowProperties;
	}

	public void addMatchCandidateFlowProperty(Resource resource) {
		matchCandidateFlowProperties.add(resource);
	}

	public void removeMatchCandidateFlowProperty(Resource resource) {
		matchCandidateFlowProperties.remove(resource);
	}

}
