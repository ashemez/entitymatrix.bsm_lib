package com.gizit.bsm.tree;

import java.util.ArrayList;
import java.util.List;

public class BSNode {
	
	public int level;

	public int sid;
	public String name;
	public int status;
	public String citype;
	public String source_ci_id;
	
	public List<Integer> children;
	public BSPath path;
	public BSNode parent;
	
	public List<BSNode> childNodes;
	public boolean canHaveChild = true;
	public boolean nodeChanged = true;
	public List<BSNode> oldChildNodes;
	
	public String bad_output_rule;
	public String marginal_output_rule;

	public BSNode AddChild(int childSid, String name, int status, String citype, String source_ci_id) {
		BSNode node = null;
		if(!this.children.contains(childSid) && this.canHaveChild )
		{
			// initialize node
			node = new BSNode();
			node.sid = childSid;
			node.name = name;
			node.status = status;
			node.citype = citype;
			node.source_ci_id = source_ci_id;
			node.level = this.level + 1;
			node.parent = this;
			node.childNodes = new ArrayList<BSNode>();
			node.children = new ArrayList<Integer>();
			node.oldChildNodes = new ArrayList<BSNode>();
			node.nodeChanged = false;
			
			// add path of this node
			node.path = new BSPath();
			for(BSNode n : this.path)
			{
				node.path.add(n);
			}
			node.path.add(node);
			
			// add this node as child to parent node
			this.childNodes.add(node);
			this.children.add(childSid);
			
			//if(node.sidCount(childSid) > 1)
			//	node.canHaveChild = false;
		}
		
		return node;
	}

}
