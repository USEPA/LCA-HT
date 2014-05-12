package harmonizationtool.console;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;


public class LoggerViewer extends ViewPart {
	public static final String ID = "harmonizationtool.console.LoggerViewer";

	public static Text loggerArea;

	LoggerWriter loggerWriter;
	
	public LoggerViewer() {
		super();
		loggerWriter = new LoggerWriter(loggerArea);
	}

	public void createPartControl(Composite parent) {
		loggerArea = new Text(parent, SWT.READ_ONLY | SWT.MULTI);
		loggerArea.setEditable(false);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		loggerArea.setLayoutData(gd);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
	
}
