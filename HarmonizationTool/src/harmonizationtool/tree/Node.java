package harmonizationtool.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class Node {

	protected Node parent;
	protected List<Node> children = new ArrayList<Node>();

	public Node(Node parent) {
		super();
		this.parent = parent;
		// System.out.println("Created Node "+this+ " with parent:"+ parent);
		if (this.parent != null) {
			parent.children.add(this);
		}
	}

	public void addChild(Node child) {
		children.add(child);
	}

	public Iterator<Node> getChildIterator() {
		return children.iterator();
	}

	public int getIndexOfChild(Node child) {
		return children.indexOf(child);
	}

	public void removeChild(Node child) {
		children.remove(child);
	}

	public Node getParent() {
		return parent;
	}

}