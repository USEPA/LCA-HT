package harmonizationtool.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class GenericMessageBox {
	public GenericMessageBox(Shell parentShell,String title, String message){
		MessageBox dialog = 
				  new MessageBox(parentShell, SWT.OK);
				dialog.setText(title);
				dialog.setMessage(message);
		dialog.open();
	}
}
