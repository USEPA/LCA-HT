package harmonizationtool.preferences;

import harmonizationtool.Activator;
import harmonizationtool.utils.Util;

import org.apache.log4j.LoggerManager;
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

public class DirectoryPreferences extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

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
		addField(new DirectoryFieldEditor("workingDirectory",
				"Working Direcotry:", getFieldEditorParent()));
		{
			Composite composite = getFieldEditorParent();
			addField(new DirectoryFieldEditor("outputDirectory",
					"Output Direcotry:", getFieldEditorParent()));
			StringFieldEditor stringFieldEditor = new StringFieldEditor(
					"runfileRoot", "Runfile Root", composite);
//			stringFieldEditor.getLabelControl(composite).setText(
//					"Output File Root");
			addField(stringFieldEditor);
//			Text outputFileRoot = stringFieldEditor.getTextControl(composite);
//			outputFileRoot.addModifyListener(new ModifyListener() {
//
//				@Override
//				public void modifyText(ModifyEvent e) {
//					Text sourceText = (Text) e.getSource();
//					String logFile = sourceText.getText();
//					logFile += "_";
//					logFile += Util.getPreferenceStore().getString("timestamp")
//							+ ".log";
//					Util.getPreferenceStore().setValue("logFile", logFile);
//				}
//			});
		}

		addField(new DirectoryFieldEditor("defaultTDB", "Default TDB:",
				getFieldEditorParent()));
	}
}
