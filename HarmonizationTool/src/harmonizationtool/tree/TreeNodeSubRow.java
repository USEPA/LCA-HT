package harmonizationtool.tree;

/**
 * @author tec
 *
 */
public class TreeNodeSubRow extends TreeNodeRow {

	public TreeNodeSubRow(TreeNodeRow parent) {
		super(parent);
	}
	@Override
	public MatchStatus getMatchStatus(int index){
		return matchStatus.get(index);
	}

}
