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

import harmonizationtool.ViewData;
import harmonizationtool.handler.ImportCSV;
import harmonizationtool.model.ModelProvider;
import harmonizationtool.utils.ResourceIdMgr;
import harmonizationtool.utils.Util;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class CSVMetaDialog extends TitleAreaDialog {

	public ModelProvider modelProvider = null;
	private  Combo combo = null;

	public Map<String, String> metaData = new LinkedHashMap<String, String>();

	public void setMetaData(Map<String, String> map) {
		metaData = map;
	}

	private String[] getDataSetInfo(){
		String[] list = null;
		ResourceIdMgr.getResource(0);
		return list;
	}
	private void addMetaDataSet() {
		if (!modelProvider.hasMetaKey(ModelProvider.DATA_SET_NAME )) {
			modelProvider.setMetaKeyValue(ModelProvider.DATA_SET_NAME, "");
		}
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
	private boolean initialCreate;

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("CSV file Meta Data");
		// addMetaFile();
		addMetaDataSet();
		addMetaCurator();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBounds(0, 0, 600, 600);

		Label lblAssociatedDataSet = new Label(composite, SWT.NONE);
		// lblAssociatedDataSet.setBounds(0, 0, 400, 14);
		lblAssociatedDataSet.setText("Data Set");
		
		combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
//		combo.add("New", 1);
		combo.setItems(new String[] { "4: (new data set)", "1: ReCiPe 108e",
				"2: TRACI 2.1", "3: GaBi 1.1" });
		if(initialCreate){
			combo.setText("Choose...");
			// combo.setBounds(0, 15, 400, 50);
		}else{
			modelProvider.getMetaValue("");
			combo.setText("1: ReCiPe 108e");
			combo.setEnabled(false);
			
		}

		combo.addModifyListener(new ModifyListener() {
		      public void modifyText(ModifyEvent e) {
		    	  populateMeta(combo.getText());
		    	  getButton(IDialogConstants.OK_ID).setEnabled(true);
		    	  System.out.println("choice is "+combo.getSelectionIndex()+" with value: "+combo.getText());
		        }
		      });


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

	public CSVMetaDialog(Shell parentShell, ModelProvider modelProvider, boolean initialCreate) {
		super(parentShell);
		this.modelProvider = modelProvider;
		this.initialCreate = initialCreate;
	}

	@Override
	protected void cancelPressed() {
		super.cancelPressed();
	}

	@Override
	protected void okPressed() {
		Iterator<Text> dialogSeq = dialogValues.iterator();
		String comboText = combo.getText();
		
		for (String key : modelProvider.getKeys()) {
			Text nextText = dialogSeq.next();
			modelProvider.setMetaKeyValue(key, nextText.getText());
			System.out.println("dialogSeq text value: " + nextText.getText());
		}
		super.okPressed();
	}
	protected void populateMeta(String data_choice) {
		System.out.println("The person chose a new data set...");

		if (data_choice.startsWith("x")){
			System.out.println("... it is new");
			Set<String> keySet = modelProvider.getKeys();
			int index = -1;
			Iterator<String> keySeq = keySet.iterator();
			while (keySeq.hasNext()) {
				String key = keySeq.next();
				index++;
//				dialogValues.get(index).setText(
//						modelProvider.getMetaValue(key));
				dialogValues.get(index).setText("blank");
			}
		}
		
	}

	@Override
	public int open() {
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		return super.open();
	}

}
