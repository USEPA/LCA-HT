package harmonizationtool.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import harmonizationtool.Activator;
import harmonizationtool.ViewData;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class Util {
	private Util() {
	}

	public static String getGMTDateFmt(Date date) {
		// SimpleDateFormat dateFormatGmt = new
		// SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ssZ");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormatGmt.format(date);
	}

	public static String getLocalDateFmt(Date date) {
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ssZ");
		dateFormatLocal.setTimeZone(TimeZone.getDefault());
		return dateFormatLocal.format(date);
	}
	

	public static Date setDateFmt(String string) throws ParseException {
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ssZ");
		dateFormatLocal.setTimeZone(TimeZone.getDefault());
		return dateFormatLocal.parse(string);
	}

	public static String splitCamelCase(String s) {
		String result = s.replaceAll(String.format("%s|%s|%s",
				"(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
				"(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
		String s1 = result.substring(0, 1);
		String s2 = result.substring(1);
		return s1.toUpperCase() + s2;
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
		IViewPart view;
		try {
			view = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().findView(viewID);
		} catch (Exception e) {
			view = null;
		}
		return view;
	}

	public static void showView(String viewID) throws PartInitException {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.showView(viewID);
	}

	public static IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}
	// public static IStatusLineManager getStatusLine(){
	// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().
	// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getV
	// getViewSite().getActionBars().getStatusLineManager();
	// }
}
