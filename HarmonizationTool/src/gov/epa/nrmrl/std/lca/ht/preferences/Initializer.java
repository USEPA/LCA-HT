package gov.epa.nrmrl.std.lca.ht.preferences;

import java.io.File;
//import java.util.Calendar;
//import java.util.GregorianCalendar;

import java.util.Date;

import gov.epa.nrmrl.std.lca.ht.harmonizationtool.Activator;
import gov.epa.nrmrl.std.lca.ht.utils.Temporal;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class Initializer extends AbstractPreferenceInitializer {

	public Initializer() {
		System.out.println("gov.epa.nrmrl.std.lca.ht.preferences.Initializer() constructor");
	}

	@Override
	public void initializeDefaultPreferences() {
		Logger logger = Logger.getLogger("run");
		String runPath = System.getProperty("user.dir");
		System.out.println("Running from this path: " + runPath);
		// logger.warn("Running from this path"+runPath);
		System.out.println("Platform.getInstallLocation().getURL().getFile(): "
				+ Platform.getInstallLocation().getURL().getFile());
		// logger.warn("Platform.getInstallLocation().getURL().getFile() "+Platform.getInstallLocation().getURL().getFile());
		// if (!workspaceDir.exists()) {
		// workspaceDir.mkdirs();
		// }
		initializeDefaultPreferences(Util.getInitialStorageLocation(), true);
	}

	public static boolean initializeDefaultPreferences(String defaultPath) {
		return initializeDefaultPreferences(defaultPath, false);
	}

	// On the first run with no input from the user and no active TDB in the prefs, don't create filesystem directories.
	// Wait until explicitly given a path
	public static boolean initializeDefaultPreferences(String defaultPath, boolean disableFolderCreation) {
		String activeTDB = Util.getPreferenceStore().getString("activeTDB");
		if (!Util.EMPTY_STRING.equals(activeTDB)) {
			disableFolderCreation = false;
		}
		String tdbPath = defaultPath + File.separator + "TDB";
		String projectPath = defaultPath + File.separator + "project_01";
		String resourcePath = projectPath + File.separator + "resource_files";
		String inputPath = projectPath + File.separator + "input_data";
		String outputPath = projectPath + File.separator + "output_data";
		String logPath = projectPath + File.separator + "log";

		if (!disableFolderCreation) {
			File tdbDir = new File(tdbPath);
			tdbDir.mkdirs();
			File projectDir = new File(projectPath);
			projectDir.mkdirs();
			File resourceDir = new File(resourcePath);
			resourceDir.mkdirs();
			File inputDir = new File(inputPath);
			inputDir.mkdirs();
			File outputDir = new File(outputPath);
			outputDir.mkdirs();
			File logDir = new File(logPath);
			logDir.mkdirs();

			if (!tdbDir.exists() || !projectDir.exists() || !resourceDir.exists() || !inputDir.exists()
					|| !outputDir.exists() || !logDir.exists())
				return false;
		}

		System.out.println("workspaceDir.getPath(): " + defaultPath);
		// if (!fred.exists()){
		// fred.mkdirs();
		// }
		System.out.println("Platform.getInstallLocation().getURL().getFile() "
				+ Platform.getInstallLocation().getURL().getFile());

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault("curatorName", "");
		store.setDefault("curatorAffiliation", "");
		store.setDefault("curatorPhone", "");
		store.setDefault("curatorEmail", "");

		store.setDefault("defaultTDB", tdbPath);

		// store.setDefault("workingDirectory", System.getProperty("user.home"));
		store.setDefault("workingDirectory", projectPath);
		store.setDefault("resourceDirectory", resourcePath);

		store.setDefault("inputDirectory", inputPath);

		// store.setDefault("outputDirectory", store.getString("workingDirectory"));
		store.setDefault("outputDirectory", outputPath);

		store.setDefault("logDirectory", logPath);

		store.setDefault("runfileRoot", "LCAHT");
		store.setDefault("startTimestamp", Temporal.getLocalDateFmt(new Date()));
		return true;
	}
}
