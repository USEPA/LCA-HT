package gov.epa.nrmrl.std.lca.dataModels;

import gov.epa.nrmrl.std.lca.ht.workflows.CSVColCheck;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.Issue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.TableColumn;

public class QACheck {
	public static Pattern pattern;
	public static Issue issue;
	

	public CSVColCheck csvColQACheck(TableColumn column, QACheck qaCheck){
		CSVColCheck results = null;
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
		return results;
	}
	
}
