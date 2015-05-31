package gov.epa.nrmrl.std.lca.ht.log;

import gov.epa.nrmrl.std.lca.ht.utils.Util;

import java.io.File;
import java.io.IOException;
//import java.util.Date;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

public class LoggerManager {
	private static String timestampValidFmt = Util.getPreferenceStore().getString("startTimestamp").replace(":", "-")
			.substring(0, Util.getPreferenceStore().getString("startTimestamp").length() - 5);

	public static void Init() {
		System.out.println("The logger init is executing");
		setUpRootLogger();
		setUpRunLogger();
		// setUpTDBLogger();

		// System.out.println("Wrote to root log!");
	}

	public static String getTimeStampValidFmt() {
		return timestampValidFmt;
	}

	private static void setUpRootLogger() {
		Logger rootLogger = Logger.getRootLogger();
		BasicConfigurator.configure();
		rootLogger.setLevel(Level.INFO);

		PatternLayout layout = new PatternLayout("%d{HH:mm:ss.SSS} [%t] %-5p %c %x - %m%n");
		// "%d{ISO8601} [%t] %-5p %c %x - %m%n");

		rootLogger.addAppender(new ConsoleAppender(layout));
		rootLogger.info("Started LCAHT at: " + Util.getLocalDateFmt(GregorianCalendar.getInstance()));
	}

	private static void setUpRunLogger() {
		Logger runLogger = Logger.getLogger("run");

		PatternLayout layout = new PatternLayout("%m%n");
		try {
			// Define file appender with layout and output log file name
			RollingFileAppender fileAppender = new RollingFileAppender(layout, Util.getPreferenceStore().getString(
					"logDirectory")
					+ File.separator
					+ Util.getPreferenceStore().getString("runfileRoot")
					+ "_" + timestampValidFmt + ".txt");

			// Add the appender to root logger
			runLogger.addAppender(fileAppender);
		} catch (IOException e) {
			System.out.println("Failed to add appender !!");
			System.out.println("e =" + e);
		}
		runLogger.setLevel(Level.INFO);
		// JUNO : TODO: TRY TO SET UP THE Logger.getLogger("run") TO SEND MORE VERBOSE OUTPUT TO THE FILE
//		runLogger.setLevel(Level.DEBUG);		
		runLogger.info("# Started LCAHT at: " + Util.getLocalDateFmt(Calendar.getInstance()));
	}

	private static void setUpTDBLogger() {
		Logger tdbLogger = Logger.getLogger("com.hp.hpl.jena.tdb.base.file.BlockAccessMapped");

		// PatternLayout layout = new PatternLayout("%m%n");
		PatternLayout layout = new PatternLayout("%d{HH:mm:ss.SSS} [%t] %-5p %c %x - %m%n");

		// try {
		// // Define file appender with layout and output log file name
		// RollingFileAppender fileAppender = new RollingFileAppender(layout,
		// Util.getPreferenceStore().getString("outputDirectory")
		// + "/"
		// + Util.getPreferenceStore().getString(
		// "runfileRootRoot") + "_tdb_"
		// + timestampValidFmt + ".txt");
		//
		// // Add the appender to root logger
		// tdbLogger.addAppender(fileAppender);
		// } catch (IOException e) {
		// System.out.println("Failed to add appender !!");
		// System.out.println("e =" + e);
		// }
		tdbLogger.setLevel(Level.TRACE);
		// runLogger.info("# Started LCAHT at: " + Util.getLocalDateFmt(new
		// Date()));
	}
}
