package harmonizationtool.dialog;

//import java.awt.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import harmonizationtool.handler.ImportCSV;
import harmonizationtool.utils.Util;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

public class CSVImportDialog extends TitleAreaDialog {

	public Map<String, String> metaData = new LinkedHashMap<String, String>();

	public void setMetaData(Map<String, String> map) {
		metaData = map;
	}

	private void addMetaFile() {
		if (!metaData.keySet().contains("fileName")) {
			metaData.put("fileName", "");
		}
		if (!metaData.keySet().contains("fileSize")) {
			metaData.put("fileSize", "");
		}
		if (!metaData.keySet().contains("fileLastModified")) {
			metaData.put("fileLastModified", "");
		}
	}

	private void addMetaDataSet() {
		metaData.put("dataSetName", "");
		metaData.put("dataSetContactName", "");
		metaData.put("dataSetContactAffiliation", "");
		metaData.put("dataSetContactEmail", "");
		metaData.put("dataSetContactPhone", "");
		metaData.put("dataSetComments", "");
	}

	private void addMetaCurator() {
		if (!metaData.keySet().contains("curatorName")) {
			String cn = Util.getPreferenceStore().getString("userName");
			metaData.put("curatorName", cn);
		}
		if (!metaData.keySet().contains("curatorAffiliation")) {
			String ca = Util.getPreferenceStore().getString("userAffiliation");
			metaData.put("curatorAffiliation", ca);
		}
		if (!metaData.keySet().contains("curatorEmail")) {
			String ce = Util.getPreferenceStore().getString("userEmail");
			metaData.put("curatorEmail", ce);
		}
		if (!metaData.keySet().contains("curatorPhone")) {
			String cp = Util.getPreferenceStore().getString("userPhone");
			metaData.put("curatorPhone", cp);
		}
	}

	public List<Text> dialogValues = new ArrayList<Text>();

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("CSV file Meta Data");
		addMetaFile();
		addMetaDataSet();
		addMetaCurator();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBounds(0, 0, 600, 600);
		composite.setLayout(new GridLayout(2, false));

		Iterator<String> keySeq = metaData.keySet().iterator();
		while (keySeq.hasNext()) {
			String key = keySeq.next();
			Label lblNewLabel = new Label(composite, SWT.NONE);
			lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			String readableKey = Util.splitCamelCase(key);
			System.out.println("key: " + key + " and readableKey: " + readableKey);
			lblNewLabel.setText(readableKey);
			dialogValues.add(new Text(composite, SWT.BORDER));
			dialogValues.get(dialogValues.size() - 1).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			dialogValues.get(dialogValues.size() - 1).setText(metaData.get(key));
		}

		return super.createDialogArea(parent);
	}

	public CSVImportDialog(Shell parentShell) {
		super(parentShell);
	}
}
