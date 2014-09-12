package gov.epa.nrmrl.std.lca.ht.log;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.varia.LevelRangeFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;

public class LoggerViewer extends ViewPart {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.log.LoggerViewer";

	LoggerWriter loggerWriter;
	Text loggerArea;
	private Text text;

	public LoggerViewer() {
		super();
	}

	public void createPartControl(Composite parent) {

//		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		parent.setLayout(new GridLayout(1, false));

		Button btnClear = new Button(parent, SWT.NONE);
		btnClear.setText("Clear");
		btnClear.addSelectionListener(new SelectionListener() {
			//
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				loggerWriter.getBuffer().setLength(0);
				loggerArea.setText("");
			}
		});

//		text = new Text(parent, SWT.BORDER);
		loggerArea = new Text(parent, SWT.WRAP | SWT.READ_ONLY | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		loggerArea.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		loggerArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		loggerWriter = new LoggerWriter(loggerArea);
		configureLog();
	}

	public void configureLog() {
		setupLogger(Logger.getLogger("run"), "INFO");
	}

	private void setupLogger(Logger logger, String level) {
		logger.setAdditivity(true);
		logger.setLevel(Level.toLevel(level));
		Appender appender = new WriterAppender(new PatternLayout("%p - %m%n"), loggerWriter);
		LevelRangeFilter tempFilter = new LevelRangeFilter();
		tempFilter.setLevelMin(Level.toLevel(level));
		appender.addFilter(tempFilter);
		logger.addAppender(appender);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
}
