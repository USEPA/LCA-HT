package harmonizationtool.preferences;

import java.util.Date;

import harmonizationtool.Activator;
import harmonizationtool.utils.Util;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class Initializer extends AbstractPreferenceInitializer {

	public Initializer() {
		System.out.println("harmonizationtool.preferences.Initializer() constructor");
	}

	@Override
	public void initializeDefaultPreferences() {
        String runPath = System.getProperty("user.dir");
        System.out.println("Running from this path"+runPath);
        
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
