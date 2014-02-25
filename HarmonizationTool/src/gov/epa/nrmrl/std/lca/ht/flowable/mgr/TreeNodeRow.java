package gov.epa.nrmrl.std.lca.ht.flowable.mgr;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

import harmonizationtool.tree.Node;

/**
 * @author tec
 * 
 */
public class TreeNodeRow extends TreeNode {
	protected Resource uri = null;

	public TreeNodeRow(TreeNode parent) {
		super(parent);
	}

	@Override
	public MatchStatus getMatchStatus(int index) {
		if (matchStatus.size() <= index) {
			return null;
		}
		matchStatus.set(index, MatchStatus.UNKNOWN);
		for (Node child : children) {
			MatchStatus childStatus = ((TreeNodeSubRow) child)
					.getMatchStatus(index);
			if (childStatus == MatchStatus.EQUIVALENT) {
				matchStatus.set(index, MatchStatus.EQUIVALENT);
				break;
			}
		}
		return matchStatus.get(index);
	}

	@Override
	public String toString() {
		String temp = this.getClass().getName();
		String name = temp.substring(temp.lastIndexOf('.') + 1);
		return name + "@" + Integer.toHexString(this.hashCode())
				+ "[colLabels=" + colLabels + ", matchStatus=" + matchStatus
				+ ", parent=" + "@" + Integer.toHexString(parent.hashCode())
				+ ", children.size()=" + children.size() + "]";
	}
}
