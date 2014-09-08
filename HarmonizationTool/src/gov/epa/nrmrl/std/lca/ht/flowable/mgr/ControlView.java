package gov.epa.nrmrl.std.lca.ht.flowable.mgr;

import gov.epa.nrmrl.std.lca.ht.utils.Util;

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
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.harmonizationtool.tree.viewer.ControlView";
	private Text textMatched;
	private Text textUnmatched;
	private Text textTotal;
	private Button btnMatchedExpand;
	private Button btnUnmatchedExpand;
	private Button btnMatchedShow;
	private Button btnUnmatchedShow;
	private Button btnAllExpand;

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
		// new Label(composite, SWT.NONE);

		btnMatchedExpand = new Button(composite, SWT.NONE);
		btnMatchedExpand.setText("Expand All");
		btnMatchedExpand.addSelectionListener(new SelectionListener() {

			// @Override
			public void widgetSelected(SelectionEvent e) {
				ResultsTreeEditor resultsTreeEditor = (ResultsTreeEditor) Util
						.findView(ResultsTreeEditor.ID);
				boolean expand = true;
				if (btnMatchedExpand.getText().equals("Expand All")) {
					btnMatchedExpand.setText("Collapse All");
				} else {
					btnMatchedExpand.setText("Expand All");
					expand = false;
				}

				if (resultsTreeEditor != null) {
					resultsTreeEditor.expandMatched(expand);
				}
				// String s = btnMatchedExpand.getText().equals("Expand All") ?
				// "Collapse All" :
				// "Expand All";
				// btnMatchedExpand.setText(s);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		btnMatchedShow = new Button(composite, SWT.NONE);
		btnMatchedShow.setText("Hide");
		btnMatchedShow.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				ResultsTreeEditor resultsTreeEditor = (ResultsTreeEditor) Util
						.findView(ResultsTreeEditor.ID);
				boolean hide = true;
				if (btnMatchedShow.getText().equals("Hide")) {
					btnMatchedShow.setText("Show");
				} else {
					btnMatchedShow.setText("Hide");
					hide = false;
				}

				if (resultsTreeEditor != null) {
					resultsTreeEditor.hideMatched(hide);
				}
				// String s = btnMatchedExpand.getText().equals("Expand All") ?
				// "Collapse All" :
				// "Expand All";
				// btnMatchedExpand.setText(s);
			}

			// @Override
			// public void widgetSelected(SelectionEvent e) {
			// String s = btnMatchedShow.getText().equals("Hide") ? "Show" :
			// "Hide";
			// btnMatchedShow.setText(s);
			// }

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

			public void widgetSelected(SelectionEvent e) {
				ResultsTreeEditor resultsTreeEditor = (ResultsTreeEditor) Util
						.findView(ResultsTreeEditor.ID);
				boolean expand = true;
				if (btnUnmatchedExpand.getText().equals("Expand All")) {
					btnUnmatchedExpand.setText("Collapse All");
				} else {
					btnUnmatchedExpand.setText("Expand All");
					expand = false;
				}

				if (resultsTreeEditor != null) {
					resultsTreeEditor.expandUnmatched(expand);
				}
				// String s = btnMatchedExpand.getText().equals("Expand All") ?
				// "Collapse All" :
				// "Expand All";
				// btnMatchedExpand.setText(s);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnUnmatchedShow = new Button(composite, SWT.NONE);
		btnUnmatchedShow.setText("Hide");
		btnUnmatchedShow.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				ResultsTreeEditor resultsTreeEditor = (ResultsTreeEditor) Util
						.findView(ResultsTreeEditor.ID);
				boolean hide = true;
				if (btnUnmatchedShow.getText().equals("Hide")) {
					btnUnmatchedShow.setText("Show");
				} else {
					btnUnmatchedShow.setText("Hide");
					hide = false;
				}

				if (resultsTreeEditor != null) {
					resultsTreeEditor.hideUnmatched(hide);
				}
				// String s = btnMatchedExpand.getText().equals("Expand All") ?
				// "Collapse All" :
				// "Expand All";
				// btnMatchedExpand.setText(s);
			}

			//
			// @Override
			// public void widgetSelected(SelectionEvent e) {
			// String s = btnUnmatchedShow.getText().equals("Hide") ? "Show" :
			// "Hide";
			// btnUnmatchedShow.setText(s);
			// }

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
		// textTotalGridData.widthHint = 60;
		textTotal.setLayoutData(textTotalGridData);

		btnAllExpand = new Button(composite, SWT.NONE);
		btnAllExpand.setText("Expand All");
		btnAllExpand.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ResultsTreeEditor resultsTreeEditor = (ResultsTreeEditor) Util
						.findView(ResultsTreeEditor.ID);
				boolean expand = true;
				if (btnAllExpand.getText().equals("Expand All")) {
					btnAllExpand.setText("Collapse All");
					btnMatchedExpand.setText("Collapse All");
					btnUnmatchedExpand.setText("Collapse All");
				} else {
					btnAllExpand.setText("Expand All");
					btnMatchedExpand.setText("Expand All");
					btnUnmatchedExpand.setText("Expand All");

					expand = false;
				}

				if (resultsTreeEditor != null) {
					resultsTreeEditor.expandAll(expand);
				}
				// String s = btnMatchedExpand.getText().equals("Expand All") ?
				// "Collapse All" :
				// "Expand All";
				// btnMatchedExpand.setText(s);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		new Label(composite, SWT.NONE);

		Button btnCancel = new Button(composite, SWT.NONE);
		btnCancel.setText("Cancel");
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
				// System.out.println("Commit Not Implemented Yet.");
				ResultsTreeEditor resultsTreeEditor = (ResultsTreeEditor) Util
						.findView(ResultsTreeEditor.ID);
				resultsTreeEditor.commitMatches();
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

	public void setMatchedRows(String matchedRows) {
		textMatched.setText(matchedRows);
	}

	public void setUnmatchedRows(String unMatchedRows) {
		textUnmatched.setText(unMatchedRows);
	}
}
