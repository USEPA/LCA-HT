package gov.epa.nrmrl.std.lca.ht.harmonizationtool;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

public class ProjectDirectoryDialog extends TitleAreaDialog {
	// private String resultString = null;
	// private String currentName = null;
	// private Color red = new Color(Display.getCurrent(), 255, 0, 0);
	// private Color defaultColor = null;

	// private List<String> nameList = new ArrayList<String>();

	public ProjectDirectoryDialog(Shell parentShell) {
		super(parentShell);
	}

	public static void execute(Shell shell) {
		DirectoryDialog dirDialog = new DirectoryDialog(shell);
		dirDialog.setText("Select your home directory");
		String selectedDir = dirDialog.open();
		System.out.println(selectedDir);
	}
}
