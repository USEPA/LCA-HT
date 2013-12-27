package harmonizationtool.dialog;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

public class MyDialog2 extends TitleAreaDialog {
	private Text text;
	private Text text_1;
	private Text text_2;
	private Text text_3;
	private Text text_4;
	private Text text_5;
	private Text text_6;
	private Text text_9;
	private Text text_10;
	private Text text_11;
	private Text text_12;
	private Text text_13;
	private Text text_14;


	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBounds(0, 0, 600, 600);
		composite.setLayout(new GridLayout(2, false));
		
		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Filename:");
		
		text = new Text(composite, SWT.BORDER);
		
		Label lblNewLabel_1 = new Label(composite, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("Bytes:");
		
		text_1 = new Text(composite, SWT.BORDER);
		text_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_2 = new Label(composite, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_2.setText("Last Modified:");
		
		text_2 = new Text(composite, SWT.BORDER);
		text_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_3 = new Label(composite, SWT.NONE);
		lblNewLabel_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_3.setText("Data Set Name:");
		
		text_3 = new Text(composite, SWT.BORDER);
		text_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_4 = new Label(composite, SWT.NONE);
		lblNewLabel_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_4.setText("Affiliation:");
		
		text_4 = new Text(composite, SWT.BORDER);
		text_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_5 = new Label(composite, SWT.NONE);
		lblNewLabel_5.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_5.setText("Contact Name:");
		
		text_5 = new Text(composite, SWT.BORDER);
		text_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_6 = new Label(composite, SWT.NONE);
		lblNewLabel_6.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_6.setText("Contact Email:");
		
		text_6 = new Text(composite, SWT.BORDER);
		text_6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_7 = new Label(composite, SWT.NONE);
		lblNewLabel_7.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_7.setText("Contact Phone:");
		
		text_14 = new Text(composite, SWT.BORDER);
		text_14.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_8 = new Label(composite, SWT.NONE);
		lblNewLabel_8.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_8.setText("Curator Name:");
		
		text_13 = new Text(composite, SWT.BORDER);
		text_13.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_9 = new Label(composite, SWT.NONE);
		lblNewLabel_9.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_9.setText("Curator Affiliation:");
		
		text_12 = new Text(composite, SWT.BORDER);
		text_12.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_10 = new Label(composite, SWT.NONE);
		lblNewLabel_10.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_10.setText("Curator Email:");
		
		text_11 = new Text(composite, SWT.BORDER);
		text_11.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_11 = new Label(composite, SWT.NONE);
		lblNewLabel_11.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_11.setText("Curator Phone:");
		
		text_10 = new Text(composite, SWT.BORDER);
		text_10.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_12 = new Label(composite, SWT.NONE);
		lblNewLabel_12.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_12.setText("Comments:");
		
		text_9 = new Text(composite, SWT.BORDER);
		text_9.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		return super.createDialogArea(parent);
	}

	public MyDialog2(Shell parentShell) {
		super(parentShell);
	}
}
