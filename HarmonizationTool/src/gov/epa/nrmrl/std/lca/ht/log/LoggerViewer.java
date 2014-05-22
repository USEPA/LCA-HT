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

public class LoggerViewer extends ViewPart {
	public static final String ID = "harmonizationtool.console.LoggerViewer";

	LoggerWriter loggerWriter;
	Text loggerArea;

	public LoggerViewer() {
		super();
	}

	public void createPartControl(Composite parent) {
//		Logger.getLogger("run").info("createPartControl done in LoggerViewer");
		// VERY DANGEROUS TO TRY TO DO THIS HERE (THE WIDGET DOESN'T EXIST YET)
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setSize(600,200);
		
		Button btnClear = new Button(composite, SWT.NONE);
		btnClear.setBounds(5, 5, 90, 25);
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
		
		loggerArea = new Text(composite, SWT.READ_ONLY | SWT.MULTI | SWT.H_SCROLL |SWT.V_SCROLL);
		loggerArea.setLocation(0, 35);
		loggerArea.setSize(600, 165);

		loggerArea.setEditable(false);


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
