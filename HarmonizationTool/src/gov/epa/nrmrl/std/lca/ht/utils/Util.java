package gov.epa.nrmrl.std.lca.ht.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import gov.epa.nrmrl.std.lca.ht.harmonizationtool.Activator;
import gov.epa.nrmrl.std.lca.ht.perspectives.FlowDataV2;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class Util {
	private Util() {
	}

	public static String getGMTDateFmt(Date date) {
		if (date == null) {
			return null;
		}
		// SimpleDateFormat dateFormatGmt = new
		// SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormatGmt.format(date);
	}

	public static String getLocalDateFmt(Date date) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		dateFormatLocal.setTimeZone(TimeZone.getDefault());
		return dateFormatLocal.format(date);
	}

	public static Date setDateFmt(String string) throws ParseException {
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		dateFormatLocal.setTimeZone(TimeZone.getDefault());
		return dateFormatLocal.parse(string);
	}

	public static String splitCamelCase(String s) {
		String pattern = String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
				"(?<=[A-Za-z])(?=[^A-Za-z])");
		String result = s.replaceAll(pattern, " ");
		String s1 = result.substring(0, 1);
		String s2 = result.substring(1);
		return s1.toUpperCase() + s2;
	}

	public static String getProductVersion() {
		final IProduct product = Platform.getProduct();
		final Bundle bundle = product.getDefiningBundle();
		final Version v = bundle.getVersion();
		return v.toString();
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
			// if (chars[i] == ',') {
			// b.append("\\");
			// }
			// if (chars[i] == '\'') {
			// b.append("\\");
			// }
			// if (chars[i] == '`') {
			// b.append("\\");
			// }
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

	public static IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
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
}
