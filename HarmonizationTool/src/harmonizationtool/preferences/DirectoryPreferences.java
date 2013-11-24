package harmonizationtool.preferences;

import harmonizationtool.Activator;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DirectoryPreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public DirectoryPreferences() {
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("sets preferences");
	}

	@Override
	protected void createFieldEditors() {
		addField(new DirectoryFieldEditor("workingDir", "Working Direcotry:", getFieldEditorParent()));
		addField(new DirectoryFieldEditor("defaultTDB", "Default TDB::", getFieldEditorParent()));
	}

}
