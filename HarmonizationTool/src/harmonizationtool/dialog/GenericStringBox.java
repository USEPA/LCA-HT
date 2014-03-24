package harmonizationtool.dialog;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class GenericStringBox extends TitleAreaDialog {

private Text text;

//public MyDialog(Shell parentShell) {
//	super(parentShell);
//}

public GenericStringBox(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}

public String create(String title, String message) {
		super.create();
		// Set the title
		Composite composite = new Composite(getParentShell(), SWT.NONE);
		// composite.setBounds(0, 0, 600, 1200);
		composite.setLayout(null);
		MessageBox dialog = 
				  new MessageBox(getShell(), SWT.OK);
				dialog.setText(title);
				dialog.setMessage(message);
				text = new Text(composite, SWT.BORDER);
				text.setBounds(0, 0, 400, 20);
		dialog.open();
		setTitle(title);
		// Set the message
		setMessage(message, IMessageProvider.INFORMATION);
		return text.getText();

	}
}
