package harmonizationtool.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author tec
 * 
 */
public class TreeNode {
	protected TreeNode parent;
	protected List<TreeNode> children = new ArrayList<TreeNode>();

	public TreeNode(TreeNode parent) {
		this.parent = parent;
		// System.out.println("Created Node "+this+ " with parent:"+ parent);
		if (this.parent != null) {
			parent.children.add(this);
		}
	}

	public void addChild(TreeNode child) {
		children.add(child);
	}

	public Iterator<TreeNode> getChildIterator() {
		return children.iterator();
	}

	public int getIndexOfChild(TreeNode child) {
		return children.indexOf(child);
	}

	public void removeChild(TreeNode child) {
		children.remove(child);
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
		for (TreeNode child : node.children) {
			printToString(child, b, indent + "  ");
		}
		return b.toString();
	}

	public TreeNode getParent() {
		return parent;
	}
}
