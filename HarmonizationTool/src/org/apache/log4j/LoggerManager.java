package org.apache.log4j;

import harmonizationtool.utils.Util;

import java.io.IOException;
import java.util.Date;

public class LoggerManager {

	public static void Init() {
		System.out.println("The logger init is executing");
		setUpRootLogger();
		setUpScriptLogger();

		System.out.println("Wrote to root log!");
	}

	private static void setUpRootLogger() {
		Logger rootLogger = Logger.getRootLogger();
		// LoggerManager.setLogFile(Util.getPreferenceStore().getString(
		// "outputDirectory")
		// + "/debug_" + Util.getPreferenceStore().getString("logFile"));

		BasicConfigurator.configure();

		rootLogger.setLevel(Level.DEBUG);

		PatternLayout layout = new PatternLayout("[%t] %-5p %c %x - %m%n");
		// "%d{ISO8601} [%t] %-5p %c %x - %m%n");

		rootLogger.addAppender(new ConsoleAppender(layout));
		try {
			// Define file appender with layout and output log file name
			RollingFileAppender fileAppender = new RollingFileAppender(layout,
					Util.getPreferenceStore().getString("outputDirectory")
							+ "/"
							+ Util.getPreferenceStore().getString(
									"outputFileRoot")
							+ "_log_"
							+ Util.getPreferenceStore().getString(
									"startTimestamp") + ".txt");
			// Add the appender to root logger
			rootLogger.addAppender(fileAppender);
		} catch (IOException e) {
			System.out.println("Failed to add appender !!");
		}
		rootLogger
				.info("Started LCAHT at: " + Util.getLocalDateFmt(new Date()));
	}

	private static void setUpScriptLogger() {
		Logger scriptLogger = Logger.getLogger("script");

		PatternLayout layout = new PatternLayout("%m%n");
		try {
			// Define file appender with layout and output log file name
			RollingFileAppender fileAppender = new RollingFileAppender(layout,
					Util.getPreferenceStore().getString("outputDirectory")
							+ "/"
							+ Util.getPreferenceStore().getString(
									"outputFileRoot")
							+ "_script_"
							+ Util.getPreferenceStore().getString(
									"startTimestamp") + ".txt");

			// Add the appender to root logger
			scriptLogger.addAppender(fileAppender);
		} catch (IOException e) {
			System.out.println("Failed to add appender !!");
		}
		scriptLogger.setLevel(Level.INFO);
		scriptLogger.info("# Started LCAHT at: "
				+ Util.getLocalDateFmt(new Date()));
	}

	// public static void setLogFile(String filename) {
	// Logger rootLogger = Logger.getRootLogger();
	//
	// PatternLayout layout = new PatternLayout(
	// "%d{ISO8601} [%t] %-5p %c %x - %m%n");
	// rootLogger.addAppender(new ConsoleAppender(layout));
	// try {
	// // Define file appender with layout and output log file name
	// RollingFileAppender fileAppender = new RollingFileAppender(layout,
	// filename);
	// // Add the appender to root logger
	// rootLogger.addAppender(fileAppender);
	// } catch (IOException e) {
	// System.out.println("Failed to add appender !!");
	// }
	// }
}
