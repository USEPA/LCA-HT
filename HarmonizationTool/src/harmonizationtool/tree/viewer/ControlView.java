package harmonizationtool.tree.viewer;

import harmonizationtool.ResultsTreeEditor;
import harmonizationtool.utils.Util;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;

public class ControlView extends ViewPart {
	public static final String ID = "harmonizationtool.tree.viewer.ControlView";
	private Text textMatched;
	private Text textUnmatched;
	private Text textTotal;
	private Button btnMatchedExpand;
	private Button btnUnmatchedExpand;
	private Button btnMatchedShow;
	private Button btnUnmatchedShow;

	public ControlView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(4, false));
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		Label lblMatched = new Label(composite, SWT.NONE);
		lblMatched.setText("Matched");

		textMatched = new Text(composite, SWT.BORDER);

		btnMatchedExpand = new Button(composite, SWT.NONE);
		btnMatchedExpand.setText("Expand All");
		btnMatchedExpand.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String s = btnMatchedExpand.getText().equals("Expand All") ? "Collapse All" : "Expand All";
				btnMatchedExpand.setText(s);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnMatchedShow = new Button(composite, SWT.NONE);
		btnMatchedShow.setText("Hide");
		btnMatchedShow.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String s = btnMatchedShow.getText().equals("Hide") ? "Show" : "Hide";
				btnMatchedShow.setText(s);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Label lblUnmatched = new Label(composite, SWT.NONE);
		lblUnmatched.setText("Un Matched");

		textUnmatched = new Text(composite, SWT.BORDER);

		btnUnmatchedExpand = new Button(composite, SWT.NONE);
		btnUnmatchedExpand.setText("Expand All");
		btnUnmatchedExpand.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String s = btnUnmatchedExpand.getText().equals("Expand All") ? "Collapse All" : "Expand All";
				btnUnmatchedExpand.setText(s);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnUnmatchedShow = new Button(composite, SWT.NONE);
		btnUnmatchedShow.setText("Hide");
		btnUnmatchedShow.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String s = btnUnmatchedShow.getText().equals("Hide") ? "Show" : "Hide";
				btnUnmatchedShow.setText(s);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Label lblTotal = new Label(composite, SWT.NONE);
		lblTotal.setText("Total");

		textTotal = new Text(composite, SWT.BORDER);
		// Object layoutDataTextTotal GridData;
		// textTotal.setLayoutData(layoutDataTextTotal GridData);
		GridData textTotalGridData = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		textTotalGridData.widthHint = 60;
		textTotal.setLayoutData(textTotalGridData);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		Button btnCancel = new Button(composite, SWT.NONE);
		btnCancel.setText("Cancel");
		btnCancel.setText("Commit");
		btnCancel.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("Cancel Not Implemented Yet.");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Button btnCommit = new Button(composite, SWT.NONE);
		btnCommit.setText("Commit");
		btnCommit.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("Commit Not Implemented Yet.");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		init();
	}

	private void init() {
		ResultsTreeEditor resultsTreeEditor = (ResultsTreeEditor) Util
				.findView(ResultsTreeEditor.ID);
		if (resultsTreeEditor != null) {
			try {
				int totalRows = resultsTreeEditor.getTotalNumberRows();
				setTotalRows("" + totalRows);
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void setFocus() {
	}

	public void setTotalRows(String totalRows) {
		textTotal.setText(totalRows);
	}

}
