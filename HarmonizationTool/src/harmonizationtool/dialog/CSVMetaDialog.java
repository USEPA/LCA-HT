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
import harmonizationtool.model.ModelProvider;
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

public class CSVMetaDialog extends TitleAreaDialog {

	public ModelProvider modelProvider = null;

	public Map<String, String> metaData = new LinkedHashMap<String, String>();

	public void setMetaData(Map<String, String> map) {
		metaData = map;
	}

	private void addMetaDataSet() {
		if (!modelProvider.hasMetaKey("dataSetName")) {
			modelProvider.setMetaKeyValue("dataSetName", "");
		}
		if (!modelProvider.hasMetaKey("dataSetVersion")) {
			modelProvider.setMetaKeyValue("dataSetVersion", "");
		}
		if (!modelProvider.hasMetaKey("dataSetContactName")) {
			modelProvider.setMetaKeyValue("dataSetContactName", "");
		}
		if (!modelProvider.hasMetaKey("dataSetContactAffiliation")) {
			modelProvider.setMetaKeyValue("dataSetContactAffiliation", "");
		}
		if (!modelProvider.hasMetaKey("dataSetContactEmail")) {
			modelProvider.setMetaKeyValue("dataSetContactEmail", "");
		}
		if (!modelProvider.hasMetaKey("dataSetContactPhone")) {
			modelProvider.setMetaKeyValue("dataSetContactPhone", "");
		}
		if (!modelProvider.hasMetaKey("dataSetComments")) {
			modelProvider.setMetaKeyValue("dataSetComments", "");
		}
	}

	private void addMetaCurator() {
		if (!modelProvider.hasMetaKey("curatorName")) {
			String cn = Util.getPreferenceStore().getString("userName");
			modelProvider.setMetaKeyValue("curatorName", cn);
		}
		if (!modelProvider.hasMetaKey("curatorAffiliation")) {
			String cn = Util.getPreferenceStore().getString("userAffiliation");
			modelProvider.setMetaKeyValue("curatorAffiliation", cn);
		}
		if (!modelProvider.hasMetaKey("curatorEmail")) {
			String cn = Util.getPreferenceStore().getString("userEmail");
			modelProvider.setMetaKeyValue("curatorEmail", cn);
		}
		if (!modelProvider.hasMetaKey("curatorPhone")) {
			String cn = Util.getPreferenceStore().getString("userPhone");
			modelProvider.setMetaKeyValue("curatorPhone", cn);
		}
	}

	private List<Text> dialogValues = new ArrayList<Text>();

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("CSV file Meta Data");
		// addMetaFile();
		addMetaDataSet();
		addMetaCurator();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBounds(0, 0, 600, 600);
		composite.setLayout(new GridLayout(2, false));

		Set<String> keySet = modelProvider.getKeys();
		Iterator<String> keySeq = keySet.iterator();
		while (keySeq.hasNext()) {
			String key = keySeq.next();
			Label lblNewLabel = new Label(composite, SWT.NONE);
			lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
					false, false, 1, 1));
			String readableKey = Util.splitCamelCase(key);
			System.out.println("key: " + key + " and readableKey: "
					+ readableKey);
			lblNewLabel.setText(readableKey);
			dialogValues.add(new Text(composite, SWT.BORDER));
			dialogValues.get(dialogValues.size() - 1).setLayoutData(
					new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			dialogValues.get(dialogValues.size() - 1).setText(
					modelProvider.getMetaValue(key));
		}

		return super.createDialogArea(parent);
	}

	public CSVMetaDialog(Shell parentShell, ModelProvider modelProvider) {
		super(parentShell);
		this.modelProvider = modelProvider;
	}

	@Override
	protected void cancelPressed() {
		super.cancelPressed();
	}

	@Override
	protected void okPressed() {
		Iterator<Text> dialogSeq = dialogValues.iterator();
		for (String key : modelProvider.getKeys()) {
			Text nextText = dialogSeq.next();
			modelProvider.setMetaKeyValue(key, nextText.getText());
			System.out.println("dialogSeq text value: " + nextText.getText());
		}
		super.okPressed();
	}

}
