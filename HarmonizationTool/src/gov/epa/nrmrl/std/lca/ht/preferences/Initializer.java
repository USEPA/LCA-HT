package gov.epa.nrmrl.std.lca.ht.preferences;

import java.io.File;
import java.util.Date;

import gov.epa.nrmrl.std.lca.ht.dataModels.CurationMethods;
import gov.epa.nrmrl.std.lca.ht.harmonizationtool.Activator;
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
		File workspaceDir = Platform.getLocation().toFile();
//		if (!workspaceDir.exists()) {
//			workspaceDir.mkdirs();
//		}
		String tdbPath = workspaceDir.getPath() + File.separator + "TDB";
		File tdbDir = new File(tdbPath);
		tdbDir.mkdirs();
		
		String projectPath = workspaceDir.getPath() + File.separator + "project_01";
		File projectDir = new File(projectPath);
		projectDir.mkdirs();
		
		String inputPath = projectDir + File.separator + "input_data";
		File inputDir = new File(inputPath);
		inputDir.mkdirs();
		
		String outputPath = projectDir + File.separator + "output_data";
		File outputDir = new File(outputPath);
		outputDir.mkdirs();
		
		String logPath = projectDir + File.separator + "log";
		File logDir = new File(logPath);
		logDir.mkdirs();
              
		System.out.println("workspaceDir.getPath(): " + workspaceDir.getPath());
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
		
//		store.setDefault("workingDirectory", System.getProperty("user.home"));
		store.setDefault("workingDirectory", projectPath);
		
		store.setDefault("inputDirectory", inputPath);

//		store.setDefault("outputDirectory", store.getString("workingDirectory"));
		store.setDefault("outputDirectory", outputPath);

		store.setDefault("logDirectory", logPath);

		store.setDefault("runfileRoot", "LCAHT");
		Date startupDate = new Date();
		store.setDefault("startTimestamp", Util.getLocalDateFmt(startupDate));
	}
}
