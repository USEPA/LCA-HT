package gov.epa.nrmrl.std.lca.ht.handler;

import java.util.List;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.FileMD;
import gov.epa.nrmrl.std.lca.ht.dataModels.FileMDKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.dialog.ChooseDataSetDialog;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenDataSet implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ChooseDataSetDialog dlg = new ChooseDataSetDialog(HandlerUtil.getActiveShell(event),
				"Please choose a dataset to re-load:");
		dlg.open();
		if (dlg.getReturnCode() == ChooseDataSetDialog.CANCEL)
			return null;
		FlowsWorkflow.clearStatusText();
		FlowsWorkflow.buttonModePostLoad();
		String dataSet = dlg.getSelection();
		CSVTableView.reset();

		// dataSet = chooseDataSetDialog.getit()
		DataSourceProvider ds = DataSourceKeeper.getByName(dataSet);
		ds.syncFromTDB(null);
		TableProvider provider = new TableProvider();
		provider.setDataSourceProvider(ds);
		String filePath = null;
		String fileName = "";
		// TODO - find out what it means when this has more than one FileMD
		List<FileMD> fileList = ds.getFileMDListNewestFirst();
		if (fileList.size() > 0) {
			FileMD fileMd = fileList.get(0);
			fileName = fileMd.getFilenameString();
			filePath = fileMd.getPath();
			provider.setFileMD(fileList.get(0));
		}
		// TODO - does it matter if there was an old one?
		TableKeeper.saveTableProvider(filePath, provider);
		// TODO - make sure we handle buildUserDataTableFromLCAHTDataViaQuery case also
		long startTime = System.currentTimeMillis();
		if (fileName.endsWith(".csv")) {
			if (!ImportUserData.buildUserDataTableFromCSVData(dataSet, provider)) {
				FlowsWorkflow.switchToWorkflowState(FlowsWorkflow.ST_BEFORE_LOAD);
				return null;
			}
		} else {
			ImportUserData.buildUserDataTableFromOLCADataViaQuery(dataSet, provider);
		}

		ImportUserData.RunData data = new ImportUserData.RunData(null);
		data.path = filePath;
		data.display = Display.getCurrent();
		ImportUserData.displayTableView(data);

		// Run in asyncExec to avoid race condition where 85% message replaces completed message
		ImportUserData.updateText(FlowsWorkflow.statusLoadUserData, fileName);
		System.out.println("Dataset opened in " + (System.currentTimeMillis() - startTime) / 1000 + "s");
		if (CSVTableView.preCommit) {
			if (CSVTableView.matchedFlowableRowNumbers.size() > 0
					|| CSVTableView.matchedFlowContextRowNumbers.size() > 0
					|| CSVTableView.matchedFlowPropertyRowNumbers.size() > 0) {
				CSVTableView.setPostCommit();
				FlowsWorkflow.switchToWorkflowState(FlowsWorkflow.ST_BEFORE_EXPORT);
			} else {
				FlowsWorkflow.switchToWorkflowState(FlowsWorkflow.ST_BEFORE_CHECK);
			}
		} else {
			FlowsWorkflow.switchToWorkflowState(FlowsWorkflow.ST_BEFORE_EXPORT);
		}
		return null;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {

	}

}
