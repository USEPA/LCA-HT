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
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault("curatorName", "");
        store.setDefault("curatorAffiliation", "");
        store.setDefault("curatorPhone", "");
        store.setDefault("curatorEmail", "");
        store.setDefault("workingDirectory", "");
        store.setDefault("outputDirectory", "");
        store.setDefault("logFileRoot", "LCAHT");
        Date startupDate = new Date();
        store.setDefault("timestamp",Util.getLocalDateFmt(startupDate));
        store.setValue("logFile", store.getString("logFileRoot")+"_"+store.getString("timestamp")+".log");
        store.setDefault("defaultTDB", "");
	}
}
