package harmonizationtool.dialog;

import java.util.HashMap;
import java.util.Map;

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
	private String inFilename;
	private String inBytes;
	private String inLastModified;
	private Text inDsName;
	private Text inDsAffiliation;
	private Text inDsContactName;
	private Text inDsContactEmail;
	private Text inDsContactPhone;
	private Text inCuratorName;
	private Text inCuratorAffiliation;
	private Text inCuratorEmail;
	private Text inCuratorPhone;
	private Text inCuratorComments;
	
	private Text filename;
	private Text bytes;
	private Text lastModified;
	private Text dsName;
	private Text dsAffiliation;
	private Text dsContactName;
	private Text dsContactEmail;
	private Text dsContactPhone;
	private Text curatorName;
	private Text curatorAffiliation;
	private Text curatorEmail;
	private Text curatorPhone;
	private Text curatorComments;

	public void setFilename(String fn) {
		System.out.println("fn="+fn);
		inFilename = fn;
	}

	public void setBytes(String numBytes) {
		inBytes = numBytes;
	}

	public void setLastModified(String lm) {
		inLastModified = lm;
	}
	
	public Map<String,Object> getMetaMap(){
		Map<String,Object> metaMap = new HashMap<String,Object>();
		metaMap.put("filename", filename);
		metaMap.put("bytes", bytes);
		metaMap.put("lastModified", lastModified);
		metaMap.put("dsName", dsName);
		metaMap.put("dsAffiliation", dsAffiliation);
		metaMap.put("dsContactName", dsContactName);
		metaMap.put("dsContactEmail", dsContactEmail);
		metaMap.put("dsContactPhone", dsContactPhone);
		metaMap.put("curatorName", curatorName);
		metaMap.put("curatorAffiliation", curatorAffiliation);
		metaMap.put("curatorEmail", curatorEmail);
		metaMap.put("curatorPhone", curatorPhone);
		metaMap.put("curatorComments", curatorComments);
		return metaMap;
	}
	public void setMetaMap(Map<String,Object> metaMap){
		inFilename = metaMap.get(filename).toString();
		inBytes = metaMap.get(bytes).toString();
		inLastModified = metaMap.get(lastModified).toString();
		inDsName.setText(metaMap.get(dsName).toString());
		inDsAffiliation.setText(metaMap.get(dsAffiliation).toString());
		inDsContactName.setText(metaMap.get(dsContactName).toString());
		inDsContactEmail.setText(metaMap.get(dsContactEmail).toString());
		inDsContactPhone.setText(metaMap.get(dsContactPhone).toString());
		inCuratorName.setText(metaMap.get(curatorName).toString());
		inCuratorAffiliation.setText(metaMap.get(curatorAffiliation).toString());
		inCuratorEmail.setText(metaMap.get(curatorEmail).toString());
		inCuratorPhone.setText(metaMap.get(curatorPhone).toString());
		inCuratorComments.setText(metaMap.get(curatorComments).toString());
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("CSV file Meta Data");
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBounds(0, 0, 600, 600);
		composite.setLayout(new GridLayout(2, false));

		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Filename:");

		filename = new Text(composite, SWT.BORDER);
		if (inFilename != null) {filename.setText(inFilename);}
		// ImportCSV icsv = new ImportCSV(); // THIS DOESN'T WORK, YOU CAN'T GET A "NEW" ONE
		// System.out.println("filenameStr = " + icsv.fileNameStr);
		// filename.setText(icsv.fileNameStr);

		Label lblNewLabel_1 = new Label(composite, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("Bytes:");

		bytes = new Text(composite, SWT.BORDER);
		bytes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (inBytes != null) {bytes.setText(inBytes);}

		Label lblNewLabel_2 = new Label(composite, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_2.setText("Last Modified:");

		lastModified = new Text(composite, SWT.BORDER);
		lastModified.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (inLastModified != null) {lastModified.setText(inLastModified);}

		Label lblNewLabel_3 = new Label(composite, SWT.NONE);
		lblNewLabel_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_3.setText("Data Set Name:");

		dsName = new Text(composite, SWT.BORDER);
		dsName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (inDsName != null) {dsName = inDsName;}

		Label lblNewLabel_4 = new Label(composite, SWT.NONE);
		lblNewLabel_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_4.setText("Affiliation:");

		dsAffiliation = new Text(composite, SWT.BORDER);
		dsAffiliation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (inDsAffiliation != null) {dsAffiliation = inDsAffiliation;}

		Label lblNewLabel_5 = new Label(composite, SWT.NONE);
		lblNewLabel_5.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_5.setText("Contact Name:");

		dsContactName = new Text(composite, SWT.BORDER);
		dsContactName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (inDsContactName != null) {dsContactName = inDsContactName;}

		Label lblNewLabel_6 = new Label(composite, SWT.NONE);
		lblNewLabel_6.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_6.setText("Contact Email:");

		dsContactEmail = new Text(composite, SWT.BORDER);
		dsContactEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (inDsContactEmail != null) {dsContactEmail = inDsContactEmail;}

		Label lblNewLabel_7 = new Label(composite, SWT.NONE);
		lblNewLabel_7.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_7.setText("Contact Phone:");

		dsContactPhone = new Text(composite, SWT.BORDER);
		dsContactPhone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (inDsContactPhone != null) {dsContactPhone = inDsContactPhone;}

		Label lblNewLabel_8 = new Label(composite, SWT.NONE);
		lblNewLabel_8.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_8.setText("Curator Name:");

		curatorName = new Text(composite, SWT.BORDER);
		curatorName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		String cn = Util.getPreferenceStore().getString("username");
		if (cn.length() > 0){
			curatorName.setText(cn);
		}
		if (inCuratorName != null) {curatorName = inCuratorName;}

		Label lblNewLabel_9 = new Label(composite, SWT.NONE);
		lblNewLabel_9.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_9.setText("Curator Affiliation:");

		curatorAffiliation = new Text(composite, SWT.BORDER);
		curatorAffiliation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		String ca = Util.getPreferenceStore().getString("userAffiliation");
		if (ca.length() > 0){
			curatorAffiliation.setText(ca);
		}
		if (inCuratorAffiliation != null) {curatorAffiliation = inCuratorAffiliation;}

		Label lblNewLabel_10 = new Label(composite, SWT.NONE);
		lblNewLabel_10.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_10.setText("Curator Email:");

		curatorEmail = new Text(composite, SWT.BORDER);
		curatorEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		String ce = Util.getPreferenceStore().getString("userEmail");
		if (ce.length() > 0){
			curatorEmail.setText(ce);
		}
		if (inCuratorEmail != null) {curatorEmail = inCuratorEmail;}

		Label lblNewLabel_11 = new Label(composite, SWT.NONE);
		lblNewLabel_11.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_11.setText("Curator Phone:");

		curatorPhone = new Text(composite, SWT.BORDER);
		curatorPhone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		String cp = Util.getPreferenceStore().getString("userPhone");
		if (cp.length() > 0){
			curatorPhone.setText(cp);
		}
		if (inCuratorPhone != null) {curatorPhone = inCuratorPhone;}

		Label lblNewLabel_12 = new Label(composite, SWT.NONE);
		lblNewLabel_12.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_12.setText("Comments:");

		curatorComments = new Text(composite, SWT.BORDER);
		curatorComments.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (inCuratorComments != null) {curatorComments = inCuratorComments;}

		return super.createDialogArea(parent);
	}

	public CSVImportDialog(Shell parentShell) {
		super(parentShell);
	}
}
