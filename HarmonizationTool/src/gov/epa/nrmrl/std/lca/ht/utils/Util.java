package gov.epa.nrmrl.std.lca.ht.utils;

import java.util.UUID;

import gov.epa.nrmrl.std.lca.ht.harmonizationtool.Activator;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class Util {

	private static Object INIT_LOCK = new Object();

	public static final String EMPTY_STRING = "";

	private Util() {
	}

	/**
	 * This method will parse a string that has camel-casing into many words with the first word capitalized.
	 * @param s		Pass in a string and it will be reformatted with spaces and the first word capitalized.
	 * @return String
	 */
	public static String splitCamelCase(String s) {
		String pattern = String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
				"(?<=[A-Za-z])(?=[^A-Za-z])");
		String result = s.replaceAll(pattern, " ");
		String s1 = result.substring(0, 1);
		String s2 = result.substring(1);
		return s1.toUpperCase() + s2;
	}

	/** 
	 * Get the version number of the Harmonization Tool from the platform product bundle version.
	 * @return String
	 */
	public static String getProductVersion() {
		final IProduct product = Platform.getProduct();
		final Bundle bundle = product.getDefiningBundle();
		final Version v = bundle.getVersion();
		return v.toString();
	}

	public static String getRandomUUID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

	public static String escape(String s) {
		char[] chars = s.toCharArray();
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '\\') {
				b.append("\\");
			}
			if (chars[i] == '"') {
				b.append("\\");
			}
			b.append(chars[i]);
		}
		return b.toString();
	}

	public static IViewPart findView(String viewID) {
		// FIRST TRY TO FIND IT
		// IF YOU CAN'T, THEN SHOW IT AND FIND IT
		IViewPart view;
		try {
			view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(viewID);
		} catch (Exception e) {
			try {
				Util.showView(viewID);
				view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(viewID);
			} catch (PartInitException e1) {
				e1.printStackTrace();
				view = null;
			}
		}
		return view;
	}

	public static void showView(String viewID) throws PartInitException {
		findView(viewID);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewID);
	}

	public static ScopedPreferenceStore getPreferenceStore() {
		return (ScopedPreferenceStore) Activator.getDefault().getPreferenceStore();
	}

	public static Resource getResourceFromString(String uriString) {
		if (uriString.startsWith("http:") || uriString.startsWith("file:")) {
			return ActiveTDB.getModel(null).getResource(uriString);
		} else {
			ResIterator iterator = (ActiveTDB.getModel(null).listSubjectsWithProperty(RDF.type, ECO.Substance));
			while (iterator.hasNext()) {
				Resource resource = iterator.next();
				if (resource.isAnon()) {
					AnonId anonId = (AnonId) resource.getId();
					if (uriString.equals(anonId.toString())) {
						return resource;
					}
				}
			}
		}
		return null;
	}

	private static String initialStorageLocation = Platform.getLocation().toFile().getPath();

	public static String getInitialStorageLocation() {
		return initialStorageLocation;
	}

	public static void setInitialStorageLocation(String location) {
		initialStorageLocation = location;
	}

	public static void setPerspective(String perspectiveID) {
		IWorkbench iWorkBench = PlatformUI.getWorkbench();
		IPerspectiveRegistry perspectiveRegistry = iWorkBench.getPerspectiveRegistry();
		iWorkBench.getActiveWorkbenchWindow().getActivePage()
				.setPerspective(perspectiveRegistry.findPerspectiveWithId(perspectiveID));
	}

	// public static IStatusLineManager getStatusLine(){
	// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().
	// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getV
	// getViewSite().getActionBars().getStatusLineManager();
	// }

	public static Object getInitLock() {
		return INIT_LOCK;
	}
}
