package gov.epa.nrmrl.std.lca.ht.log;

import gov.epa.nrmrl.std.lca.ht.utils.Util;

import java.io.File;
import java.io.IOException;
//import java.util.Date;

//import java.util.Calendar;
//import java.util.GregorianCalendar;

import java.util.Date;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

public class LoggerManager {
	private static String timestampValidFmt = Util.getPreferenceStore().getString("startTimestamp").replace(":", "-")
			.substring(0, Util.getPreferenceStore().getString("startTimestamp").length() - 5);
	

	/**
	 * Initializes two loggers: Root and Run loggers. Both loggers print to std.out the time that they have started.  The logs can be seen upon startup of the Harmonization Tool. 
	 */
	public static void Init() {
		System.out.println("The logger init is executing");
//		setUpRootLogger();
		setUpRunLogger();
	}

	
	
	
	public static String getTimeStampValidFmt() {
		return timestampValidFmt;
	}

	
	
	/**
	 * This method initializes the Java-based logger (called log4j) named rootLogger and utilizes several log4j functions.  The root logger is set to INFO level.  
	 * A time pattern is generated.  This method is not used.

	 */
//	private static void setUpRootLogger() {
//		Logger rootLogger = Logger.getRootLogger();
//		BasicConfigurator.configure();
//		rootLogger.setLevel(Level.INFO);
//
//		PatternLayout layout = new PatternLayout("%d{HH:mm:ss.SSS} [%t] %-5p %c %x - %m%n");
//		// "%d{ISO8601} [%t] %-5p %c %x - %m%n");
//
//		rootLogger.addAppender(new ConsoleAppender(layout));
//		
//		/**
//		 * Notification that LCAHT has started at a particular time. 
//		 */
//		rootLogger.info("Started LCAHT at: " + Util.getLocalDateFmt(new Date()));
//	}

	
	
	/**
	 * This method sets up a logger of type "run" with a particular pattern and is named runLogger.  The logger level is set to INFO.  
	 */
	private static void setUpRunLogger() {
		Logger runLogger = Logger.getLogger("run");

		PatternLayout layout = new PatternLayout("%m%n");
		try {
			// Define file appender with layout and output log file name
			RollingFileAppender fileAppender = new RollingFileAppender(layout, Util.getPreferenceStore().getString(
					"logDirectory")
					+ File.separator
					+ Util.getPreferenceStore().getString("runfileRoot")
					+ "_"
					+ timestampValidFmt
					+ ".txt");

			// Add the appender to root logger
			runLogger.addAppender(fileAppender);
		} catch (IOException e) {
			System.out.println("Failed to add appender !!");
			System.out.println("e =" + e);
		}
		runLogger.setLevel(Level.INFO);
		// JUNO : TODO: TRY TO SET UP THE Logger.getLogger("run") TO SEND MORE VERBOSE OUTPUT TO THE FILE
		// runLogger.setLevel(Level.DEBUG);
		runLogger.info("# Started LCAHT at: " + Util.getLocalDateFmt(new Date()));
	}

	
	
	/**
	 * This method initializes a Java-based logger (called log4j) with name tdbLogger.  The logger type is from com.hp.hpl.jena.tdb.base.file.BlockAccessMapped.  The logger level is set to TRACE.
	 */
//	private static void setUpTDBLogger() {
//		Logger tdbLogger = Logger.getLogger("com.hp.hpl.jena.tdb.base.file.BlockAccessMapped");
//		PatternLayout layout = new PatternLayout("%d{HH:mm:ss.SSS} [%t] %-5p %c %x - %m%n");
//		tdbLogger.setLevel(Level.TRACE);
//
//	}
}
