package com.gizit.bsm.tree;

import java.util.ArrayList;

public class BSTree extends BSNode {

	public BSTree(int root_id, String root_name, String source_ci_id) {
		this.sid = root_id;
		this.name = root_name;
		this.source_ci_id = source_ci_id;
		this.level = 1;
	}
	
	// get unique sid list of tree
	public ArrayList<Integer> GetUniqueSidList(){
		return null;
	}
	
	// update status of a node through sid
	

}

