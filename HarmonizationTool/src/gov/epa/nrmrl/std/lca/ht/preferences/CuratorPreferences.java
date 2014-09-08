package gov.epa.nrmrl.std.lca.ht.preferences;

import harmonizationtool.Activator;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class CuratorPreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public CuratorPreferences() {
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("sets preferences");

	}

	@Override
	protected void createFieldEditors() {
		addField(new StringFieldEditor("curatorName", "Name:", getFieldEditorParent()));
		addField(new StringFieldEditor("curatorAffiliation", "Affiliation:", getFieldEditorParent()));
		addField(new StringFieldEditor("curatorEmail", "Email:", getFieldEditorParent()));
		addField(new StringFieldEditor("curatorPhone", "Phone:", getFieldEditorParent()));
	}

}
