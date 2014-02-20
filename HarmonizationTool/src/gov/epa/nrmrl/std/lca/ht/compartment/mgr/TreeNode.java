package gov.epa.nrmrl.std.lca.ht.compartment.mgr;

import harmonizationtool.tree.Node;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * @author tec
 * 
 */
public class TreeNode extends Node {
	protected String nodeName = null;
	protected Resource uri = null;
	static int count = 0;

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

	public String getLabel(){
		if(parent == null){
			return nodeName;
		}else{
			String parentsLabel = ((TreeNode)parent).getLabel();
			return (parentsLabel != null) ? (parentsLabel +": "+nodeName) : nodeName;
		}
	}
}
