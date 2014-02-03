package harmonizationtool.tree;


import java.util.ArrayList;
import java.util.List;

/**
 * @author tec
 *
 */
public class TreeNodeRow extends TreeNode {
	protected List<String> colLabels = new ArrayList<String>();
	protected List<MatchStatus> matchStatus = new ArrayList<MatchStatus>();

	public TreeNodeRow(TreeNode parent) {
		super(parent);
	}
	@Override
	public String toString() {
		String temp = this.getClass().getName();
		String name = temp.substring(temp.lastIndexOf('.')+1);
		return name+"@" + Integer.toHexString(this.hashCode())+"[colLabels=" + colLabels + ", matchStatus="
				+ matchStatus + ", parent=" + "@" + Integer.toHexString(parent.hashCode()) + ", children.size()=" + children.size()+"]";
	}
	public void addMatchStatus(MatchStatus status){
		matchStatus.add(status);
	}
	public MatchStatus getMatchStatus(int index){
		matchStatus.set(index, MatchStatus.UNKNOWN);
		for(TreeNode child : children){
			MatchStatus childStatus = ((TreeNodeSubRow)child).getMatchStatus(index);
			if(childStatus == MatchStatus.EQUIVALENT){
				matchStatus.set(index, MatchStatus.EQUIVALENT);
				break;
			}
		}
		return matchStatus.get(index);
	}
	public void updateMatchStatus(int index , MatchStatus status){
		matchStatus.set(index,status);
	}
    public void addColumnLabel(String label){
    	colLabels.add(label);
    }
    public String getColumnLabel(int index){
    	return colLabels.get(index);
    }
    public void setColumnLabel(int index, String label){
    	colLabels.set(index,label);
    }
}
