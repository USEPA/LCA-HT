package gov.epa.nrmrl.std.lca.ht.job;

import java.util.ArrayList;
import java.util.List;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVColumnInfo;
import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.FlowContext;
import gov.epa.nrmrl.std.lca.ht.dataModels.Flowable;
import gov.epa.nrmrl.std.lca.ht.dataModels.MatchCandidate;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.TableKeeper;
import harmonizationtool.model.TableProvider;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FEDLCA;
import harmonizationtool.vocabulary.LCAHT;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * @author tsb Tommy Cathey 919-541-1500
 * @author Tom Transue 919-541-0494
 * 
 *         Job for executing AutoMatching rows in a CSVTableView.
 * 
 */
public class AutoMatchJob extends Job {
	private String tableKey;
	private Integer[] results = new Integer[3];

	// 1: HIGH EVIDENCE HITS
	// 2: LOWER EVIDENCE HITS
	// 3: NO EVIDENCE ITEMS (UNMATCHED)

	public AutoMatchJob(String name, String tableKey) {
		super(name);
		this.tableKey = tableKey;
		// this.harmonyQuery2Impl = harmonyQuery2Impl;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		TableProvider tableProvider = TableKeeper.getTableProvider(tableKey);
		Model model = ActiveTDB.model;
		// Table table = CSVTableView.getTable();
		System.out.println("Table :" + tableProvider);

		int rowCount = tableProvider.getData().size();
		System.out.println("Need to process this many rows: " + rowCount);
		CSVColumnInfo[] assignedCSVColumnInfo = tableProvider.getAssignedCSVColumnInfo();

		// CHECK WHICH Flowable and FlowContext COLUMNS ARE ASSIGNED
		List<Integer> flowableColumnNumbers = new ArrayList<Integer>();
		List<Integer> flowContextColumnNumbers = new ArrayList<Integer>();

		for (int colNumber = 0; colNumber < assignedCSVColumnInfo.length; colNumber++) {
			CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[colNumber];
			if (csvColumnInfo != null) {
				if (csvColumnInfo.getRDFClass().equals(Flowable.getRdfclass())) {
					if (csvColumnInfo.isRequired()) {
						flowableColumnNumbers.add(0, colNumber);
					} else {
						flowableColumnNumbers.add(colNumber);
					}
				} else if (csvColumnInfo.getRDFClass().equals(FlowContext.getRdfclass())) {
					if (csvColumnInfo.isRequired()) {
						flowContextColumnNumbers.add(0, colNumber);
					} else {
						flowContextColumnNumbers.add(colNumber);
					}
				}
			}
		}

		List<MatchCandidate> matchCandidates = new ArrayList<MatchCandidate>();
		// NOW ITERATE THROUGH EACH ROW, LOOKING FOR MATCHES
		for (int rowNumber = 0; rowNumber < rowCount; rowNumber++) {
			DataRow dataRow = (DataRow) tableProvider.getData().get(rowNumber);

			// FIRST DO Flowable
			Resource rdfClass = Flowable.getRdfclass();
			// FIXME - THIS DOESN'T WORK, BUT WITH INTERNET, I CAN FIGURE IT OUT
//			RDFDatatype integerType = (RDFDatatype) XSD.integer;
//			Literal rowLiteral = model.createTypedLiteral(rowNumber, integerType);
			Literal rowLiteral = model.createTypedLiteral(rowNumber);
			System.out.println("rowLiteral.getDatatype() = "+rowLiteral.getDatatype());
			

			ResIterator resIterator = model.listSubjectsWithProperty(FEDLCA.sourceTableRowNumber, rowLiteral);
			Resource itemToMatchTDBResource = null;
			while (resIterator.hasNext()) {
				itemToMatchTDBResource = resIterator.next();
				if (model.contains(itemToMatchTDBResource, RDF.type, rdfClass)) {
					break;
				}
			}
			ResIterator resourceIterator = model.listSubjectsWithProperty(RDF.type, rdfClass);
			while (resourceIterator.hasNext()) {
				Resource candidate = resourceIterator.next();
				if (!model.contains(candidate, ECO.hasDataSource, tableProvider.getDataSourceProvider().getTdbResource())) {
					for (int colNumber : flowableColumnNumbers) {
						Property property = assignedCSVColumnInfo[colNumber].getTdbProperty();
						Literal literalToMatch = itemToMatchTDBResource.getPropertyResourceValue(property).asLiteral();
						if (model.contains(candidate, property, literalToMatch)) {
							MatchCandidate matchCandidate = new MatchCandidate(colNumber, itemToMatchTDBResource, candidate);
							matchCandidates.add(matchCandidate);
							System.out.println("Row: " + rowNumber + ". Col: " + colNumber);
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									CSVTableView.updateCheckedData();
									FlowsWorkflow.setTextFileInfo("Going...");
								}
							});
						}
					}
				}
			}
			// FIND MATCHING Flowables FOR THIS ROW

			// for (int flowableCol : flowableColumnNumbers) {
			// CSVColumnInfo csvColumnInfo = assignedCSVColumnInfo[flowableCol];
			// // rdfClass = csvColumnInfo.getRDFClass();
			// // String field = dataRow.get(flowableCol - 1);
			// // RDFNode rdfNode = ActiveTDB.model.createLit
			// // thing = ActiveTDB.model.listSubjectsWithProperty(arg0, arg1)
			//
			// }
			//
			// for (int colNumber = 0; colNumber < dataRow.getSize(); colNumber++) {
			// if (assignedCSVColumnInfo[colNumber + 1] != null) {
			// System.out.println("Row: " + rowNumber + ". Col: " + colNumber);
			// Display.getDefault().asyncExec(new Runnable() {
			// public void run() {
			// CSVTableView.updateCheckedData();
			// FlowsWorkflow.setTextFileInfo("Going...");
			// }
			// });
			// }
			// }
			tableProvider.setLastChecked(rowNumber);
		}
		// List<MatchCandidate> matchCandidates = new ArrayList<MatchCandidate>();
		// DataSourceProvider dataSourceProvider = CSVTableView.getDataSourceProvider();
		//
		// Model model = ActiveTDB.model;
		// Resource dataSourceTDBResource = dataSourceProvider.getTdbResource();
		// // ResIterator resourceIterator = model.listResourcesWithProperty(ECO.hasDataSource,
		// // dataSourceTDBResource.asNode());
		// ResIterator resourceIterator = model.listResourcesWithProperty(ECO.hasDataSource,
		// dataSourceTDBResource);
		// int count = 0;
		// while (resourceIterator.hasNext()) {
		// count++;
		// System.out.println("count: " + count);
		// Resource dataItem = resourceIterator.next();
		// int rowNumber = -1;
		// Statement rowNumberStatement = dataItem.getProperty(FEDLCA.sourceTableRowNumber);
		// if (rowNumberStatement != null) {
		// if (rowNumberStatement.getObject().isLiteral()) {
		// rowNumber = rowNumberStatement.getObject().asLiteral().getInt();
		// // System.out.println("rowNumber = " + rowNumber);
		// StmtIterator dataItemProperties = dataItem.listProperties();
		// while (dataItemProperties.hasNext()) {
		// Statement dataItemStatement = dataItemProperties.next();
		// Property dataItemProperty = dataItemStatement.getPredicate();
		// RDFNode dataItemRDFNode = dataItemStatement.getObject();
		// // System.out.println("dataItemStatement: "+dataItemStatement);
		//
		// // NOW FIND OTHER "dataItems" WITH THE SAME PROPERTY AND RDFNode
		//
		// if (!dataItemProperty.equals(FEDLCA.sourceTableRowNumber)) {
		// ResIterator matchingResourcesIterator = model.listSubjectsWithProperty(dataItemProperty,
		// dataItemRDFNode);
		// while (matchingResourcesIterator.hasNext()) {
		// System.out.println("Found a match for dataItemStatement: " + dataItemStatement);
		// Resource matchingResource = matchingResourcesIterator.next();
		// if (!matchingResource.equals(dataItem)) {
		// MatchCandidate matchCandidate = new MatchCandidate(rowNumber, dataItem,
		// matchingResource);
		// if (matchCandidate.confirmRDFtypeMatch()) {
		// matchCandidates.add(matchCandidate);
		// }
		// }
		// }
		// }
		// }
		// }
		// }
		// }
		// System.out.println("matchCandidates.size()=" + matchCandidates.size());

		return Status.OK_STATUS;
	}

	public Integer[] getHitCounts() {
		return results;
	}

}
