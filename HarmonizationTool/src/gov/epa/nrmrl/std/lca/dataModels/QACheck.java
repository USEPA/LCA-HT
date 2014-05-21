package gov.epa.nrmrl.std.lca.dataModels;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CsvTableViewerColumn;
import gov.epa.nrmrl.std.lca.ht.workflows.CSVColCheck;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.Issue;
import harmonizationtool.model.Status;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.TableColumn;

public class QACheck {
	private static String name;
	private static Pattern pattern;
	private static Issue issue;
	
	public QACheck(Pattern pattern, Issue issue){
		this.name = null;
		this.pattern = pattern;
		this.issue = issue;
	}
	public QACheck(String name, Pattern pattern, Issue issue){
		this.name = name;
		this.pattern = pattern;
		this.issue = issue;
	}
	
	public Issue getIssue(){
		return issue;
	}
	public void csvColQACheck(CsvTableViewerColumn column, QACheck qaCheck){
		TableColumn tableColumn = (TableColumn) column.getColumn().getData();
		@SuppressWarnings("unchecked")
		List<String> items = (List<String>) tableColumn.getData();
		int rowNum = 0;
		for (String rowValue: items){
			Matcher matcher = qaCheck.getPattern().matcher(rowValue);
			if (matcher.find()){
				qaCheck.getIssue().setStatus(Status.UNRESOLVED);
				qaCheck.getIssue().setLocation("Row: "+rowNum);
			}
			rowNum++;
		}
//		thing = tableColumn.getData()
//		Matcher matcher = 

	}
	private Pattern getPattern() {
		return pattern;
	}
	
//	public CSVColCheck csvColQACheck(TableColumn column, QACheck qaCheck){
//		CSVColCheck results = null;
//		if (tableProvider == null){
//		tableProvider = TableKeeper
//				.getTableProvider(fileMD.getPath());
//		}
//		int index = tableProvider.getHeaderNamesAsStrings().indexOf(colName);
//		 Object thingy = column.getData();
//		for (int i=0;i<column.;i++){
//			int iPlusOne = i+1;
//			DataRow row = tableProvider.getData().get(i);
////		for (DataRow row: tableProvider.getData()){
//			String val = row.get(index);
//			System.out.println("value: "+val);
//			Matcher matcher = qaCheck.pattern.matcher(val);
//			int count = matcher.groupCount();
////			for (hit: matcher.group)
////					if(val.substring(0,1).equals(" ")){
//				// LEADING SPACE
//				System.out.println("Leading space on line: "+iPlusOne);
//			}
//		}
//		return results;
//	}
	
}
