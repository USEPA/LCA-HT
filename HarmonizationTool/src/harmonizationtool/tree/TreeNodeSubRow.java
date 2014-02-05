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
		if(matchStatus.size() <= index){
			return null;
		}
		return matchStatus.get(index);
	}

}
