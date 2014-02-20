package gov.epa.nrmrl.std.lca.ht.compartment.mgr;

import harmonizationtool.tree.Node;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;


/**
 * @author tec
 * 
 */
public class TreeNode extends Node {
	protected String nodeName = new String();
	protected Resource uri = null;

	public TreeNode(TreeNode parent) {
		super(parent);
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

}
