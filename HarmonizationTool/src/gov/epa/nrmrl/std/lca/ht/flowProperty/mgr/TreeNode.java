package gov.epa.nrmrl.std.lca.ht.flowProperty.mgr;

import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.TreeNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author tec
 * 
 */
public class TreeNode extends Node {
	protected String nodeName = null;
	protected Resource uri = null;
	protected String uuid = null;
	protected String referenceDescription = null;
	protected String referenceUnit = null;
	protected Resource nodeClass = null;
	
	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getReferenceDescription() {
		return referenceDescription;
	}

	public void setReferenceDescription(String referenceDescription) {
		this.referenceDescription = referenceDescription;
	}

	public String getReferenceUnit() {
		return referenceUnit;
	}

	public void setReferenceUnit(String referenceUnit) {
		this.referenceUnit = referenceUnit;
	}

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

	public String getLabel() {
		if (parent == null) {
			return nodeName;
		} else {
			String parentsLabel = ((TreeNode) parent).getLabel();
			return (parentsLabel != null) ? (parentsLabel + ": " + nodeName) : nodeName;
		}
	}

	public static List<TreeNode> getAllChildNodes(TreeNode treeNode) {
		List<TreeNode> resultsList = new ArrayList<TreeNode>();
		if (treeNode.hasChildren()) {
			Iterator<Node> iterator = treeNode.getChildIterator();
			while (iterator.hasNext()) {
				TreeNode newNode = (TreeNode) iterator.next();
				resultsList.add(newNode);
				List<TreeNode> moreResults = getAllChildNodes(newNode);
				resultsList.addAll(moreResults);
			}
		}
		return resultsList;
	}
	// public Object getChildList() {
	// List<TreeNode> childList = new ArrayList<TreeNode>();
	// Iterator<Node> iterator = getChildIterator();
	// while (iterator.hasNext()){
	// Node child = iterator.next();
	// childList.add((TreeNode) child);
	// }
	// return childList;
	// }
}
