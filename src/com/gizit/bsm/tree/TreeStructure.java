package com.gizit.bsm.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TreeStructure {

	public TreeStructure()
	{
	}
	
	public Node rootNode;
	public void AddRoot(int sid, String name, int status, String citype) {
		// initialize node
		rootNode = new Node();
		rootNode.sid = sid;
		rootNode.level = 1;
		rootNode.name = name;
		rootNode.status = status;
		rootNode.citype = citype;
		rootNode.parent = null;
		rootNode.childNodes = new ArrayList<Node>();
		rootNode.children = new ArrayList<Integer>();
		rootNode.oldChildNodes = new ArrayList<Node>();
		rootNode.nodeChanged = false;
		rootNode.virtualNodeGroups = new ArrayList<NodeGroup>();

		// add path of this node
		rootNode.path = new Path();
		rootNode.path.nodePath = new ArrayList<Node>();
		rootNode.path.nodePath.add(rootNode);
	}
	
	public void AddRoot(int sid, String name, int status, String citype, String source_ci_id) {
		// initialize node
		rootNode = new Node();
		rootNode.sid = sid;
		rootNode.level = 1;
		rootNode.name = name;
		rootNode.status = status;
		rootNode.citype = citype;
		rootNode.source_ci_id = source_ci_id;
		rootNode.parent = null;
		rootNode.childNodes = new ArrayList<Node>();
		rootNode.children = new ArrayList<Integer>();
		rootNode.oldChildNodes = new ArrayList<Node>();
		rootNode.nodeChanged = false;
		rootNode.virtualNodeGroups = new ArrayList<NodeGroup>();
		
		// add path of this node
		rootNode.path = new Path();
		rootNode.path.nodePath = new ArrayList<Node>();
		rootNode.path.nodePath.add(rootNode);
	}
	
	public Node AddChild(Node parentNode, int childSid, String name, int status, String citype) {
		Node node = null;
		if(!parentNode.children.contains(childSid) && parentNode.canHaveChild )
		{
			// initialize node
			node = new Node();
			node.sid = childSid;
			node.name = name;
			node.status = status;
			node.citype = citype;
			node.level = parentNode.level + 1;
			node.parent = parentNode;
			node.childNodes = new ArrayList<Node>();
			node.children = new ArrayList<Integer>();
			node.oldChildNodes = new ArrayList<Node>();
			node.nodeChanged = false;
			node.virtualNodeGroups = new ArrayList<NodeGroup>();
			
			// add path of this node
			node.path = new Path();
			node.path.nodePath = new ArrayList<Node>();
			for(Node n : parentNode.path.nodePath)
			{
				node.path.nodePath.add(n);
			}
			node.path.nodePath.add(node);
			
			// add this node as child to parent node
			parentNode.childNodes.add(node);
			parentNode.children.add(childSid);
			
			if(node.sidCount(childSid) > 1)
				node.canHaveChild = false;
		}
		
		return node;
	}
	
	public Node AddChild(Node parentNode, int childSid, String name, int status, String citype, String source_ci_id) {
		Node node = null;
		if(!parentNode.children.contains(childSid) && parentNode.canHaveChild )
		{
			// initialize node
			node = new Node();
			node.sid = childSid;
			node.name = name;
			node.status = status;
			node.citype = citype;
			node.source_ci_id = source_ci_id;
			node.level = parentNode.level + 1;
			node.parent = parentNode;
			node.childNodes = new ArrayList<Node>();
			node.children = new ArrayList<Integer>();
			node.oldChildNodes = new ArrayList<Node>();
			node.nodeChanged = false;
			node.virtualNodeGroups = new ArrayList<NodeGroup>();
			
			// add path of this node
			node.path = new Path();
			node.path.nodePath = new ArrayList<Node>();
			for(Node n : parentNode.path.nodePath)
			{
				node.path.nodePath.add(n);
			}
			node.path.nodePath.add(node);
			
			// add this node as child to parent node
			parentNode.childNodes.add(node);
			parentNode.children.add(childSid);
			
			if(node.sidCount(childSid) > 1)
				node.canHaveChild = false;
		}
		
		return node;
	}
	
	public void UpdateStatus(Node node) {
		
	}

	public class Node implements java.lang.Cloneable {
		public int level;
		public int sid;
		public String name;
		public int status;
		public String citype;
		public String source_ci_id;
		public List<Integer> children;
		public HashMap<Integer, Integer> childrenWeights;
		public Path path;
		public Node parent;
		public List<Node> childNodes;
		public boolean canHaveChild = true;
		public boolean nodeChanged = true;
		public List<Node> oldChildNodes;
		public List<NodeGroup> virtualNodeGroups;
		public String bad_output_rule;
		public String marginal_output_rule;
		public double badbad;
		public double badmarginal;
		public double marginalbad;
		public double marginalmarginal;
		public String compmodel;
		public int impactweight;
		public int childCount;
		
		public int sidCount(int csid) {
			int occurrence = 0;
			
			for(Node n : path.nodePath) {
				if(n.sid == csid) {
					occurrence++;
				}
			}
			return occurrence;
		}
		
		public Object clone() throws CloneNotSupportedException {
			return super.clone();
		}
	}
	
	public class Path implements java.lang.Cloneable {
		public List<Node> nodePath;

		public Object clone() throws CloneNotSupportedException {
			return super.clone();
		}
	}
	
	// virtual children groups
	public class NodeGroup {
		public List<Node> groupedChildren;
		public double weight;
	}

}
