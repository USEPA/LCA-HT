package gov.epa.nrmrl.std.lca.ht.output;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.csvFiles.SaveHarmonizedDataHandler;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dialog.ChooseDataSetDialog;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.MatchContexts;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.MatchProperties;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

//import java.util.Calendar;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openlca.lcaht.converter.Json2Zip;

public class SaveHarmonizedDataForOLCAJsonldZip implements IHandler {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.output.SaveHarmonizedDataForOLCAJsonldZip";

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				FlowsWorkflow.switchToWorkflowState(FlowsWorkflow.ST_DURING_EXPORT);
				FlowsWorkflow.setStatusConclude("Starting export...");
			}
		});
		Shell shell = HandlerUtil.getActiveShell(event);
		ChooseDataSetDialog dlg = new ChooseDataSetDialog(shell);
		dlg.open();
		final String currentName = dlg.getSelection();
		String saveTo = null;
		final Logger runLogger = Logger.getLogger("run");

		if (currentName == null) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					FlowsWorkflow.setStatusConclude("Export canceled");
					FlowsWorkflow.switchToWorkflowState(FlowsWorkflow.ST_BEFORE_EXPORT);
				}
			});
			return null;
		}
		if (dlg.getFormat() == 1) {
			Util.findView(MatchContexts.ID);
			Util.findView(MatchProperties.ID);

			System.out.println("Saving Harmonized Data to .zip file");

			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			FileDialog dialog = new FileDialog(shell, SWT.SAVE);
			String[] filterNames = new String[] { "Zip Files" };
			String[] filterExtensions = new String[] { "*.zip" };

			String outputDirectory = Util.getPreferenceStore().getString("outputDirectory");
			if (outputDirectory.length() > 0) {
				dialog.setFilterPath(outputDirectory);
			} else {
				String homeDir = System.getProperty("user.home");
				dialog.setFilterPath(homeDir);
			}

			dialog.setFilterNames(filterNames);
			dialog.setFilterExtensions(filterExtensions);
			Util.findView(CSVTableView.ID);

			dialog.setFileName(currentName + "_harmonized");

			saveTo = dialog.open();
			System.out.println("Save to: " + saveTo);

			if (saveTo == null) {
				// "dialog was cancelled or an error occurred"
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						FlowsWorkflow.setStatusConclude("Export canceled");
						FlowsWorkflow.switchToWorkflowState(FlowsWorkflow.ST_BEFORE_EXPORT);
					}
				});
				return null;
			}
		}
		if (dlg.getFormat() == 0) {
			HashMap<String, String> newParams = new HashMap<String, String>(event.getParameters());
			newParams.put("LCA-HT.exportDataSetName", currentName);
			ExecutionEvent newEvent = new ExecutionEvent(event.getCommand(), newParams, event.getTrigger(),
					event.getApplicationContext());

			IHandler innerHandler = new SaveHarmonizedDataHandler();
			innerHandler.execute(newEvent);
			return null;
		} else if (dlg.getFormat() == 2) {
			HashMap<String, String> newParams = new HashMap<String, String>(event.getParameters());
			newParams.put("LCA-HT.exportDataSetName", currentName);
			ExecutionEvent newEvent = new ExecutionEvent(event.getCommand(), newParams, event.getTrigger(),
					event.getApplicationContext());

			IHandler innerHandler = new SaveHarmonizedDataForOLCAJsonld();
			innerHandler.execute(newEvent);
			return null;
		} else if (dlg.getFormat() == 1) {
			// TODO = this temp filename helps trigger the next routine to report when
			// it is finished
			final String tempOutputName = saveTo + ".tmp.json";
			final String originalName = saveTo;
			FlowsWorkflow.disableAllButtons();
			new Thread(new Runnable() {
				public void run() {
					try {
						HashMap<String, String> newParams = new HashMap<String, String>(event.getParameters());
						newParams.put("LCA-HT.outputFilename", tempOutputName);
						newParams.put("LCA-HT.exportDataSetName", currentName);
						ExecutionEvent newEvent = new ExecutionEvent(event.getCommand(), newParams, event.getTrigger(),
								event.getApplicationContext());

						runLogger.debug("Converting to OpenLCA Zip");
						SaveHarmonizedDataForOLCAJsonld innerHandler = new SaveHarmonizedDataForOLCAJsonld();
						innerHandler.execute(newEvent);

						File input = new File(tempOutputName);
						File output = new File(originalName);
						new Json2Zip(input, output).run();
						input.delete();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
		// Display.getDefault().syncExec(new Runnable() {
		// public void run() {
		// FlowsWorkflow.setStatusConclude("Export complete");
		// FlowsWorkflow.switchToWorkflowState(8);
		// }
		// });
		return null;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isHandled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

}
