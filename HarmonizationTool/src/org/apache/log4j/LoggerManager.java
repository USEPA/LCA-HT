package org.apache.log4j;

import harmonizationtool.utils.Util;

import java.io.IOException;
import java.util.Date;

public class LoggerManager {
	private static String timestampValidFmt = Util.getPreferenceStore().getString("startTimestamp").replace(":", "-")
			.substring(0, Util.getPreferenceStore().getString("startTimestamp").length() - 5);

	public static void Init() {
		System.out.println("The logger init is executing");
		setUpRootLogger();
		setUpScriptLogger();

		System.out.println("Wrote to root log!");
	}

	private static void setUpRootLogger() {
		Logger rootLogger = Logger.getRootLogger();
		BasicConfigurator.configure();
		rootLogger.setLevel(Level.DEBUG);

		PatternLayout layout = new PatternLayout("[%t] %-5p %c %x - %m%n");
		// "%d{ISO8601} [%t] %-5p %c %x - %m%n");

		rootLogger.addAppender(new ConsoleAppender(layout));
		try {
			// Define file appender with layout and output log file name
			RollingFileAppender fileAppender = new RollingFileAppender(layout, Util.getPreferenceStore().getString("outputDirectory") + "/"
					+ Util.getPreferenceStore().getString("outputFileRoot") + "_debug_" + timestampValidFmt + ".txt");
			// Add the appender to root logger
			rootLogger.addAppender(fileAppender);
		} catch (IOException e) {
			System.out.println("Failed to add appender !!");
			System.out.println("e =" + e);
		}
		rootLogger.info("Started LCAHT at: " + Util.getLocalDateFmt(new Date()));
	}

	private static void setUpScriptLogger() {
		Logger scriptLogger = Logger.getLogger("script");

		PatternLayout layout = new PatternLayout("%m%n");
		try {
			// Define file appender with layout and output log file name
			RollingFileAppender fileAppender = new RollingFileAppender(layout, Util.getPreferenceStore().getString("outputDirectory") + "/"
					+ Util.getPreferenceStore().getString("outputFileRoot") + "_script_" + timestampValidFmt + ".txt");

			// Add the appender to root logger
			scriptLogger.addAppender(fileAppender);
		} catch (IOException e) {
			System.out.println("Failed to add appender !!");
			System.out.println("e =" + e);
		}
		scriptLogger.setLevel(Level.INFO);
		scriptLogger.info("# Started LCAHT at: " + Util.getLocalDateFmt(new Date()));
	}
}
