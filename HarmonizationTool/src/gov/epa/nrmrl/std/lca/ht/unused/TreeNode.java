package gov.epa.nrmrl.std.lca.ht.unused;

import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.Node;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.MatchStatus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author tec
 * 
 */
public class TreeNode extends Node {
	protected List<String> colLabels = new ArrayList<String>();
	protected List<MatchStatus> matchStatus = new ArrayList<MatchStatus>();

	public TreeNode(TreeNode parent) {
		super(parent);
	}

	public static String printToString(TreeNode node, StringBuilder b) {
		printToString(node, b, "");
		return b.toString();
	}

	public static String printToString(TreeNode node, StringBuilder b,
			String indent) {
		// print yourself
		b.append(indent + node.toString() + "\n");
		// ask your children to print themselves
		for (Node child : node.children) {
			printToString((TreeNode)child, b, indent + "  ");
		}
		return b.toString();
	}

	public void addMatchStatus(MatchStatus status) {
		matchStatus.add(status);
	}

	public MatchStatus getMatchStatus(int index) {
		return matchStatus.get(index);
	}

	public void updateMatchStatus(int index, MatchStatus status) {
		matchStatus.set(index,status);
	}

	public void addColumnLabel(String label) {
		colLabels.add(label);
	}

	public String getColumnLabel(int index) {
		return colLabels.get(index);
	}

	public void setColumnLabel(int index, String label) {
		colLabels.set(index,label);
	}

	public int size() {
		return children.size();
	}

	public Object get(int i) {
		return children.get(i);
	}

	public boolean contains(TreeNode treeNode) {
		return children.contains(treeNode);
	}
	
	public static List<TreeNode> getAllChildNodes(TreeNode treeNode){
		List<TreeNode> resultsList = new ArrayList<TreeNode>();
		if (treeNode.hasChildren()){
			Iterator<Node> iterator = treeNode.getChildIterator();
			while (iterator.hasNext()){
				TreeNode newNode = (TreeNode) iterator.next();
				resultsList.add(newNode);
				List<TreeNode> moreResults = getAllChildNodes(newNode);
				resultsList.addAll(moreResults);
			}
		}
		return resultsList;
	}

//	public void remove(TreeNode treeNode) {
//		children.remove(treeNode);
//	}
}
