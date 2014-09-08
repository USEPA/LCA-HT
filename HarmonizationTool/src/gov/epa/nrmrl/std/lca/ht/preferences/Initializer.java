package gov.epa.nrmrl.std.lca.ht.preferences;

import java.util.Date;

import gov.epa.nrmrl.std.lca.ht.utils.Util;
import harmonizationtool.Activator;

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
        System.out.println("Running from this path"+runPath);
        logger.warn("Running from this path"+runPath);
        System.out.println("Platform.getInstallLocation().getURL().getFile() "+Platform.getInstallLocation().getURL().getFile());
        logger.warn("Platform.getInstallLocation().getURL().getFile() "+Platform.getInstallLocation().getURL().getFile());
        
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault("curatorName", "");
        store.setDefault("curatorAffiliation", "");
        store.setDefault("curatorPhone", "");
        store.setDefault("curatorEmail", "");
        store.setDefault("workingDirectory", System.getProperty("user.home"));
        store.setDefault("outputDirectory", store.getString("workingDirectory"));
        store.setDefault("runfileRoot", "LCAHT");
        Date startupDate = new Date();
		store.setDefault("startTimestamp",Util.getLocalDateFmt(startupDate));
        store.setDefault("defaultTDB", "");
	}
}
