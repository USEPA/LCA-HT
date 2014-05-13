package harmonizationtool.preferences;

import java.io.File;

import gov.epa.nrmrl.std.lca.ht.log.LoggerManager;
import harmonizationtool.Activator;
import harmonizationtool.utils.Util;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DirectoryPreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private Composite logFileComposite;

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

		addField(new DirectoryFieldEditor("defaultTDB", "Default TDB:", getFieldEditorParent()));

		addField(new DirectoryFieldEditor("workingDirectory", "Working Direcotry:", getFieldEditorParent()));

		addField(new DirectoryFieldEditor("outputDirectory", "Output Direcotry:", getFieldEditorParent()));

		// Composite composite = new Composite();
		Composite composite = getFieldEditorParent();

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

		// addField(resultingRunfile);

	}
}
