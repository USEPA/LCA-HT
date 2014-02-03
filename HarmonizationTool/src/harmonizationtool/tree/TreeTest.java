package harmonizationtool.tree;



import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @author tec
 *
 */
public class TreeTest {

	public TreeTest(Shell shell) {
		TreeNode root = createModel();
		System.out.println("ROOT="+TreeNode.printToString(root, new StringBuilder()));
	}

	private TreeNode createModel() {
		TreeNode root = new TreeNode(null);
		for (int i = 1; i < 10; i++) {
			TreeNodeRow treeNodeRow = new TreeNodeRow(root);
			treeNodeRow.addColumnLabel("Master List");
			treeNodeRow.addColumnLabel("Benzene " + i);
			treeNodeRow.addColumnLabel("102-32-" + i);
			treeNodeRow.addMatchStatus(MatchStatus.UNKNOWN);
			treeNodeRow.addMatchStatus(MatchStatus.UNKNOWN);
			treeNodeRow.addMatchStatus(MatchStatus.UNKNOWN);
			{// TRACI subrow
				TreeNodeSubRow treeNodeSubRow = new TreeNodeSubRow(treeNodeRow);
				treeNodeSubRow.addColumnLabel("TRACI");
				treeNodeSubRow.addColumnLabel("Benzene x" + i);
				treeNodeSubRow.addColumnLabel("102-32-" + i);
				treeNodeSubRow.addMatchStatus(MatchStatus.UNKNOWN);
				treeNodeSubRow.addMatchStatus(MatchStatus.NONEQUIVALENT);
				treeNodeSubRow.addMatchStatus(MatchStatus.EQUIVALENT);

			}
			{// ReCiPe subrow
				TreeNodeSubRow treeNodeSubRow = new TreeNodeSubRow(treeNodeRow);
				treeNodeSubRow.addColumnLabel("ReCiPe");
				treeNodeSubRow.addMatchStatus(MatchStatus.UNKNOWN);
				if(i==3){
				    treeNodeSubRow.addColumnLabel("Benzene x" + i);
				    treeNodeSubRow.addMatchStatus(MatchStatus.NONEQUIVALENT);
				}else{
					treeNodeSubRow.addColumnLabel("Benzene " + i);
					treeNodeSubRow.addMatchStatus(MatchStatus.EQUIVALENT);
				}
				treeNodeSubRow.addColumnLabel("102-32-" + i);
				treeNodeSubRow.addMatchStatus(MatchStatus.EQUIVALENT);
			}
			TreeNodeBlankRow treeNodeBlankRow = new TreeNodeBlankRow(root);
			treeNodeBlankRow.addMatchStatus(MatchStatus.BLANK);
			treeNodeBlankRow.addMatchStatus(MatchStatus.BLANK);
			treeNodeBlankRow.addMatchStatus(MatchStatus.BLANK);
		}
		return root;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new TreeTest(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}
