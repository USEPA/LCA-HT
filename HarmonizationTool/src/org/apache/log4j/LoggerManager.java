package org.apache.log4j;

import harmonizationtool.utils.Util;

import java.io.File;
import java.io.IOException;
import java.util.Date;

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
		// try {
		// // Define file appender with layout and output log file name
		// RollingFileAppender fileAppender = new RollingFileAppender(layout,
		// Util.getPreferenceStore().getString("outputDirectory")
		// + "/"
		// + Util.getPreferenceStore().getString(
		// "runfileRootRoot") + "_debug_"
		// + timestampValidFmt + ".txt");
		// // Add the appender to root logger
		// rootLogger.addAppender(fileAppender);
		// } catch (IOException e) {
		// System.out.println("Failed to add appender !!");
		// System.out.println("e =" + e);
		// }
		rootLogger.info("Started LCAHT at: " + Util.getLocalDateFmt(new Date()));
	}

	private static void setUpRunLogger() {
		Logger runLogger = Logger.getLogger("run");

		// Establish the "runfiles" subfolder of runfiles
		// String outputDirectory =
		// Util.getPreferenceStore().getString("outputDirectory");
		// if (outputDirectory.length() > 0) {
		// fileDialog.setFilterPath(outputDirectory);
		// } else {
		// String homeDir = System.getProperty("user.home");
		// fileDialog.setFilterPath(homeDir);
		// }
		//
		// String path = fileDialog.open();
		// File file = null;
		// if (path != null) {
		// file = new File(path);
		//
		// runLogger.info("LOAD CSV " + path);

		PatternLayout layout = new PatternLayout("%m%n");
		try {
			// Define file appender with layout and output log file name
			RollingFileAppender fileAppender = new RollingFileAppender(layout, Util.getPreferenceStore().getString(
					"outputDirectory")
					+ File.separator
					+ "runfiles"
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
		runLogger.info("# Started LCAHT at: " + Util.getLocalDateFmt(new Date()));
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
