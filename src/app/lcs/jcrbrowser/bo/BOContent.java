package app.lcs.jcrbrowser.bo;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.VersionManager;

import app.lcs.jcrbrowser.utility.Constants;

/**
 * @author: Aniruddha Dutta Chowdhury (adchowdhury@gmail.com)
 * @since: Mar 3, 2014
 */

public class BOContent {

	private Node	node		= null;
	private boolean	isParent	= false;
	private boolean isFolder	= false;

	public BOContent(Node a_node) {
		node = a_node;
		
		if(node != null) {
			try {
				String nodeTypeName = node.getPrimaryNodeType().getName();
				if(Constants.NT_File.equalsIgnoreCase(nodeTypeName)) {
					isFolder = false;
				}else {
					isFolder = true;
				}
			} catch (Throwable a_th) {
				System.out.println(a_th.getMessage());
				//ignore
			}
		}
	}
	
	public boolean isSystem() {
		if(node == null) {
			return false;
		}
		
		try {
			String nodeTypeName = node.getPrimaryNodeType().getName();
			if(Constants.NT_System.equalsIgnoreCase(nodeTypeName)) {
				return true;
			}
		} catch (Throwable a_th) {
			System.out.println(a_th.getMessage());
			//ignore
		}
		return false;
	}
	
	public int getChildCount(){
		int childCount = 0;
		try {
			
		} catch (Throwable a_th) {
			System.out.println(a_th.getMessage());
			//ignore
		}
		return childCount;
	}

	public Node getNode() {
		return node;
	}
	
	public void setParent(boolean isParent) {
		this.isParent = isParent;
	}
	
	public boolean isParent() {
		return isParent;
	}
	
	public boolean isFolder() {
		return isFolder;
	}

	@Override
	public String toString() {
		if(isParent) {
			return "..";
		}
		
		if (node == null) {
			return "null";
		} else {
			try {
				
				return node.getName();
			} catch (RepositoryException e) {
				System.err.println(e.getMessage());
				return "!!! Error !!!";
			}
		}
	}
}
