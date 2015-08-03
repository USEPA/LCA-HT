package gov.epa.nrmrl.std.lca.ht.dialog;

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import gov.epa.nrmrl.std.lca.ht.preferences.Initializer;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.ui.PlatformUI;

public class StorageLocationDialog extends TitleAreaDialog {

	public static final int RET_SUCCESS = 0;
	public static final int RET_NO_DIRECTORY = 1;
	public static final int RET_SHOW_PREFS = 2;
	public static final int RET_CANCEL = 3;

	private Text text;

	Button prefsButton = null;

	String storageLocation = null;

	Preferences osPrefs = null;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public StorageLocationDialog(Shell parentShell, Preferences prefs) {
		super(parentShell);
		this.setBlockOnOpen(true);
		osPrefs = prefs;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		setTitle("Choose Harmonization Tool Workspace");
		setMessage("The Harmonization Tool uses a workspace directory to store its database, input files, output files, and log files in subdirectories.  Please specify a location for this directory.");

		storageLocation = osPrefs.get("lca.wsDir", Util.getInitialWorkspaceLocation());

		Composite area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		Composite container = new Composite(area, SWT.NONE);

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.verticalAlignment = SWT.TOP;
		gd.minimumHeight = -1;
		gd.grabExcessVerticalSpace = false;
		GridLayout gl_container = new GridLayout(3, false);
		gl_container.marginHeight = 0;
		gl_container.verticalSpacing = 1;
		container.setLayout(gl_container);
		container.setLayoutData(gd);
		
				Label label = new Label(container, SWT.NONE);
				label.setText("Directory");
				label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		
				text = new Text(container, SWT.SINGLE | SWT.BORDER);
				
						text.setText(storageLocation);
						gd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
						gd.grabExcessHorizontalSpace = true;
						text.setLayoutData(gd);
		
				final Button button = new Button(container, SWT.NONE);
				button.setText("Browse");
				button.addSelectionListener(new SelectionListener() {

					public void widgetDefaultSelected(SelectionEvent e) {
					}

					public void widgetSelected(SelectionEvent e) {
						DirectoryDialog dlg = new DirectoryDialog(button.getShell(), SWT.OPEN);
						dlg.setFilterPath(storageLocation);
						dlg.setText("Open");
						String path = dlg.open();
						if (path == null)
							return;
						text.setText(path);
					}
				});
				button.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));

		/* Web */
		gd.grabExcessHorizontalSpace = true;
		new Label(container, SWT.NONE);
		
				prefsButton = new Button(container, SWT.CHECK);
				prefsButton.setSelection(!osPrefs.getBoolean("lca.chooseWorkspace", false));
				prefsButton.setText("Use this directory as the default and do not ask again");
		new Label(container, SWT.NONE);

		return area;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(635, 300);
	}

	@Override
	protected void okPressed() {
		setReturnCode(RET_SUCCESS);
		osPrefs.put("lca.wsDir", text.getText());
		osPrefs.putBoolean("lca.chooseWorkspace", !prefsButton.getSelection());
		try {
			osPrefs.sync();
			osPrefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		close();
	}

	@Override
	protected void cancelPressed() {
		setReturnCode(RET_CANCEL);
		close();
		return;
	}

}
