package harmonizationtool.tree;

/**
 * @author tec
 *
 */
public class TreeNodeBlankRow extends TreeNodeRow {

	public TreeNodeBlankRow(TreeNode parent) {
		super(parent);
	}
	public MatchStatus getMatchStatus(int index){
		return matchStatus.get(index);
	}

}
