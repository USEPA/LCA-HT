package harmonizationtool.utils;

import harmonizationtool.Activator;
import harmonizationtool.ViewData;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class Util {
	private Util() {
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
//			if (chars[i] == ',') {
//				b.append("\\");
//			}
//			if (chars[i] == '\'') {
//				b.append("\\");
//			}
//			if (chars[i] == '`') {
//				b.append("\\");
//			}
			b.append(chars[i]);
		}
		return b.toString();
	}
    public static IViewPart findView(String viewID){
    	IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(viewID);
    	return view;
    }
    public static void showView(String viewID) throws PartInitException{
    	PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewID);
    }
    public static IPreferenceStore getPreferenceStore(){
    	return Activator.getDefault().getPreferenceStore();
    }
//    public static IStatusLineManager getStatusLine(){
//    	PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().
//    	PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getV
//    	getViewSite().getActionBars().getStatusLineManager();
//    }
}
