package harmonizationtool;

import org.eclipse.swt.widgets.Shell;

public class ProjectDirectoryManager {

	public static void Init(Shell shell) throws Exception {
		ProjectDirectoryDialog.execute(shell);
//		throw new Exception("Hey!");
	}
}
