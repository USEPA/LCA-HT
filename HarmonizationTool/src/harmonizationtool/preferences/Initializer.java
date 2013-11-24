package harmonizationtool.preferences;

import harmonizationtool.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class Initializer extends AbstractPreferenceInitializer {

	public Initializer() {
		System.out.println("harmonizationtool.preferences.Initializer() constructor");
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault("username", "");
        store.setDefault("userAffiliation", "");
        store.setDefault("userPhone", "");
        store.setDefault("userEmail", "");
        store.setDefault("workingDirectory", "");
        store.setDefault("defaultTDB", "");
	}

}
