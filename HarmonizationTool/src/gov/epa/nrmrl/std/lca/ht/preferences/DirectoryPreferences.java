package gov.epa.nrmrl.std.lca.ht.preferences;

import java.io.File;
import java.util.prefs.Preferences;

import gov.epa.nrmrl.std.lca.ht.dialog.GenericMessageBox;
import gov.epa.nrmrl.std.lca.ht.harmonizationtool.Activator;
import gov.epa.nrmrl.std.lca.ht.harmonizationtool.Application;
import gov.epa.nrmrl.std.lca.ht.log.LoggerManager;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class DirectoryPreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	// private Composite logFileComposite;

	// private StringFieldEditor logFileName;
	
	String workspaceDirectory;
	
	LCADirectoryFieldEditor workspaceEditor;
	
	Button prefsButton;
	
	Preferences osPrefs = null;
	
	class LCADirectoryFieldEditor extends DirectoryFieldEditor {
		public LCADirectoryFieldEditor(String name, String labelText, Composite parent) {
			super(name,labelText,parent);
		}
		
	    protected boolean doCheckState() {
	        return true;
	    }
	}

	public DirectoryPreferences() {
		osPrefs = Preferences.userNodeForPackage(Application.class);

	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("The Harmonization Tool (HT) requires the user to specify directories for local storage.  Please choose a location to store its data.");
	}

	@Override
	protected void createFieldEditors() {
		String runFile = "[Output Directory]" + File.separator + "runfiles" + File.separator
				+ Util.getPreferenceStore().getString("runfileRoot") + "_"
				+ Util.getPreferenceStore().getString("startTimestamp") + ".txt";

		Composite composite = getFieldEditorParent();
		
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		
		workspaceDirectory = osPrefs.get("workspaceDir", null);

		Label label = new Label(composite, SWT.NONE);
		label.setText("Harmonization Tool Workspace:");
		new Label(composite, SWT.NONE);

		workspaceEditor = new LCADirectoryFieldEditor("workspaceDir", "", composite);
		addField(workspaceEditor);
		
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
			
		prefsButton = new Button(composite, SWT.CHECK);
		prefsButton.setSelection(!osPrefs.getBoolean("lca.chooseWorkspace", false));
		prefsButton.setText("Use this as the default and do not ask agiain");
		prefsButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		new Label(composite, SWT.NONE);
	}
	
	@Override
	public boolean performOk() {
		boolean valid = super.performOk();
		String wsDir = null;
		try {
			if (valid) {
				Util.getPreferenceStore().save();

				wsDir = Util.getPreferenceStore().getString("workspaceDir");
				File file = new File(wsDir);
				if (!file.isDirectory())
					valid = file.mkdirs() && file.isDirectory();
				if (!valid) {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					String error = "Could not access the workspace directory - please check the location and try again.";
					new GenericMessageBox(shell, "Error", error);
					return valid;
				}
				boolean dir = file.isDirectory();
				System.out.println(file + " is dir " + dir);
					
				
			}
			System.out.println("Stored directory preferences");
		}
		catch (Exception e) {
			valid = false;
			e.printStackTrace();
		}
		if (valid) {
			osPrefs.put("lca.wsDir", wsDir);
			osPrefs.putBoolean("lca.chooseWorkspace", !prefsButton.getSelection());
		}
		return valid;
	}
	
	@Override
	public boolean performCancel() {
		return true;
	}
}
