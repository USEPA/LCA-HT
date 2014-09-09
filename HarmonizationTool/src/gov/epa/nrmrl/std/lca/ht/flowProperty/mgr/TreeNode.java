package gov.epa.nrmrl.std.lca.ht.flowProperty.mgr;

import com.hp.hpl.jena.rdf.model.Resource;


/**
 * @author tec
 * 
 */
public class TreeNode extends Node {
	protected String nodeName = null;
	protected Resource uri = null;
	static int count = 0;

	public Resource getUri() {
		return uri;
	}

	public void setUri(Resource uri) {
		this.uri = uri;
	}

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

//	public Object getChildList() {
//		List<TreeNode> childList = new ArrayList<TreeNode>();
//		Iterator<Node> iterator = getChildIterator();
//		while (iterator.hasNext()){
//			Node child = iterator.next();
//			childList.add((TreeNode) child);
//		}
//		return childList;
//	}
}
