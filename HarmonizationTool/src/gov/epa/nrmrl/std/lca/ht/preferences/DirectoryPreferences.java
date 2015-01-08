package gov.epa.nrmrl.std.lca.ht.preferences;

import java.io.File;

import gov.epa.nrmrl.std.lca.ht.harmonizationtool.Activator;
import gov.epa.nrmrl.std.lca.ht.log.LoggerManager;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DirectoryPreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	// private Composite logFileComposite;

	// private StringFieldEditor logFileName;

	public DirectoryPreferences() {
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("sets preferences");
	}

	@Override
	protected void createFieldEditors() {
		String runFile = "[Output Directory]" + File.separator + "runfiles" + File.separator
				+ Util.getPreferenceStore().getString("runfileRoot") + "_"
				+ Util.getPreferenceStore().getString("startTimestamp") + ".txt";

		Composite composite = getFieldEditorParent();

		addField(new DirectoryFieldEditor("defaultTDB", "Triples DB Directory:", composite));
		addField(new DirectoryFieldEditor("workingDirectory", "Project Direcotry:", composite));
		addField(new DirectoryFieldEditor("inputDirectory", "Input Direcotry:", composite));
		addField(new DirectoryFieldEditor("outputDirectory", "Output Direcotry:", composite));
		addField(new DirectoryFieldEditor("logDirectory", "Log Direcotry:", composite));

		composite = getFieldEditorParent();
		
		StringFieldEditor runfileRootEditor = new StringFieldEditor("runfileRoot", "Runfile Root", composite);
		Text runfileRoot = runfileRootEditor.getTextControl(composite);

		final StringFieldEditor resultingRunfile = new StringFieldEditor("id", "Resulting Runfile", -1,
				StringFieldEditor.VALIDATE_ON_KEY_STROKE, composite);
		resultingRunfile.getTextControl(composite).setEditable(false);
		resultingRunfile.getTextControl(composite).setText(runFile);

		runfileRoot.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {

				Text sourceText = (Text) e.getSource();
				String logFile = sourceText.getText();
				logFile += "_";
				logFile += LoggerManager.getTimeStampValidFmt() + ".txt";
				resultingRunfile.setStringValue("[Output Directory]" + File.separator + "runfiles" + File.separator
						+ logFile);
				// Util.getPreferenceStore().setValue("logFile", logFile);
			}
		});

		addField(runfileRootEditor);
	}
}
