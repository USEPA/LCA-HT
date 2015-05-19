package gov.epa.nrmrl.std.lca.ht.preferences;

import java.io.File;

import gov.epa.nrmrl.std.lca.ht.dialog.GenericMessageBox;
import gov.epa.nrmrl.std.lca.ht.harmonizationtool.Activator;
import gov.epa.nrmrl.std.lca.ht.log.LoggerManager;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class DirectoryPreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	// private Composite logFileComposite;

	// private StringFieldEditor logFileName;
	
	String defaultTDB;
	String workingDirectory;
	String inputDirectory;
	String outputDirectory;
	String logDirectory;
	
	class LCADirectoryFieldEditor extends DirectoryFieldEditor {
		public LCADirectoryFieldEditor(String name, String labelText, Composite parent) {
			super(name,labelText,parent);
		}
		
	    protected boolean doCheckState() {
	        return true;
	    }
	}

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
		
		defaultTDB = Util.getPreferenceStore().getDefaultString("defaultTDB");
		workingDirectory = Util.getPreferenceStore().getDefaultString("workingDirectory");
		inputDirectory = Util.getPreferenceStore().getDefaultString("inputDirectory");
		outputDirectory = Util.getPreferenceStore().getDefaultString("outputDirectory");
		logDirectory = Util.getPreferenceStore().getDefaultString("logDirectory");

		addField(new LCADirectoryFieldEditor("defaultTDB", "Triples DB Directory:", composite));
		addField(new LCADirectoryFieldEditor("workingDirectory", "Project Direcotry:", composite));
		addField(new LCADirectoryFieldEditor("inputDirectory", "Input Direcotry:", composite));
		addField(new LCADirectoryFieldEditor("outputDirectory", "Output Direcotry:", composite));
		addField(new LCADirectoryFieldEditor("logDirectory", "Log Direcotry:", composite));

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
	
	@Override
	public boolean performOk() {
		boolean valid = super.performOk();
		try {
			if (valid) {
				Util.getPreferenceStore().save();
				String[] newDirs = new String[] { "defaultTDB", "workingDirectory", "inputDirectory", "outputDirectory", "logDirectory" };
				for (int i = 0; valid && i < newDirs.length; ++i) {
					File file = new File(Util.getPreferenceStore().getString(newDirs[i]));
					if (!file.isDirectory())
						valid = file.mkdirs() && file.isDirectory();
					if (!valid) {
						Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
						String error = "Could not access " + newDirs[i] + " - please check the location and try again.";
						new GenericMessageBox(shell, "Error", error);
						return valid;
					}
					boolean dir = file.isDirectory();
					System.out.println(file + " is dir " + dir);
					
				}
			}
			System.out.println("Stored directory preferences");
		}
		catch (Exception e) {
			valid = false;
			e.printStackTrace();
		}
		return valid;
	}
	
	@Override
	public boolean performCancel() {
		Util.getPreferenceStore().setDefault("defaultTDB", defaultTDB);
		Util.getPreferenceStore().setDefault("workingDirectory", workingDirectory);
		Util.getPreferenceStore().setDefault("inputDirectory", inputDirectory);
		Util.getPreferenceStore().setDefault("outputDirectory", outputDirectory);
		Util.getPreferenceStore().setDefault("logDirectory", logDirectory);
		ActiveTDB.markPrefsCanceled();
		return true;
	}
}
