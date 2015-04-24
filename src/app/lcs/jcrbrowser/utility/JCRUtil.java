package app.lcs.jcrbrowser.utility;

import static app.lcs.jcrbrowser.utility.ApplicationContext.applicationProperties;
import static app.lcs.jcrbrowser.utility.ApplicationContext.session;
import static app.lcs.jcrbrowser.utility.Constants.REPOSITORY_PASSWORD;
import static app.lcs.jcrbrowser.utility.Constants.REPOSITORY_URL;
import static app.lcs.jcrbrowser.utility.Constants.REPOSITORY_USERNAME;
import static app.lcs.jcrbrowser.utility.Constants.ROOT_WORKSPACE_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.SimpleCredentials;
import javax.swing.JOptionPane;

import org.apache.jackrabbit.commons.JcrUtils;

import app.lcs.jcrbrowser.bo.BOContent;

/**
 * @author: Aniruddha Dutta Chowdhury (adchowdhury@gmail.com)
 * @since: Mar 3, 2014
 */

public final class JCRUtil {

	public static void initSession() {
		Repository repository = null;
		char[] password = applicationProperties.getProperty(REPOSITORY_PASSWORD).toCharArray();
		try {
			repository = JcrUtils.getRepository(applicationProperties.getProperty(REPOSITORY_URL));
			session = repository.login(new SimpleCredentials(applicationProperties.getProperty(REPOSITORY_USERNAME), password), applicationProperties.getProperty(ROOT_WORKSPACE_ID));
		} catch (Exception a_excp) {
			a_excp.printStackTrace();
			System.out.println("Not able to get Source webdav connection");
			JOptionPane.showMessageDialog(null, a_excp.getMessage(), "Couldn't open webdav to: " + applicationProperties.getProperty(REPOSITORY_URL), JOptionPane.ERROR_MESSAGE);
			session = null;
			applicationProperties.clear();
		}
	}

	public static void createRecursivePath(String a_strAbsolutePath) {
		String strPaths[] = a_strAbsolutePath.split("/");
		String runningPath = "";
		for (String strPath : strPaths) {
			try {
				String itemPath = runningPath + (runningPath.isEmpty() ? "/" : "") + strPath;
				if (session.itemExists(itemPath) == false) {
					if(runningPath == "") {
						createFolder(session.getRootNode(), strPath);
					}else {
						createFolder(getNodeByAbsolutePath(runningPath), strPath);
					}
				}
				runningPath += (runningPath.endsWith("/") ? "" : "/") + strPath;
			} catch (Throwable a_th) {
				a_th.printStackTrace();
			}
		}
	}
	
	public static Properties getProperties(BOContent a_content) throws Throwable{
		Properties properties = new Properties();
		Node node = a_content.getNode();
		PropertyIterator props = node.getProperties();
		while(props.hasNext()) {
			Property prop = props.nextProperty();
			properties.put(prop.getName(), prop.getString());
		}
		return properties;
	}

	public static Node getNodeByAbsolutePath(String a_strPath) {
		Node node = null;

		if (a_strPath == null || a_strPath.trim().length() <= 0) {
			JOptionPane.showMessageDialog(ApplicationContext.frmMain, "Invalid path", "Couldn't open webdav to: " + a_strPath, JOptionPane.ERROR_MESSAGE);
			return node;
		}

		try {
			if (session.itemExists(a_strPath) == false) {
				int result = JOptionPane.showConfirmDialog(ApplicationContext.frmMain, "The target folder does not exists, do you want to create?", "Path do not exists", JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					createRecursivePath(a_strPath);
				} else {
					return node;
				}
			}

			node = session.getNode(a_strPath);
		} catch (Throwable a_th) {
			a_th.printStackTrace();
			JOptionPane.showMessageDialog(null, a_th.getMessage(), "Couldn't open webdav to: " + applicationProperties.getProperty(REPOSITORY_URL) + a_strPath, JOptionPane.ERROR_MESSAGE);
		}
		return node;
	}

	public static String getWorkspacePath() {
		return applicationProperties.getProperty(REPOSITORY_URL) + "/" + applicationProperties.getProperty(ROOT_WORKSPACE_ID);
	}

	public static void createFolder(Node a_parentNode, String a_folderName) {
		if (a_parentNode == null || a_folderName == null || a_folderName.trim().length() <= 0) {
			return;
		}

		try {
			JcrUtils.getOrAddFolder(a_parentNode, a_folderName);
			session.save();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	public static List<BOContent> getContents(Node a_parentNode) {

		List<BOContent> contents = new ArrayList<BOContent>();

		try {
			Node parentNode = null;

			if (a_parentNode == null) {
				parentNode = session.getRootNode();

				BOContent c = new BOContent(parentNode);
				c.setParent(true);
				contents.add(c);
			} else {
				parentNode = a_parentNode;
				try {
					BOContent c = null;
					String nodeTypeName = parentNode.getPrimaryNodeType().getName();
					if (Constants.NT_Root.equalsIgnoreCase(nodeTypeName)) {
						c = new BOContent(parentNode);
					} else {
						c = new BOContent(parentNode.getParent());
					}
					c.setParent(true);
					contents.add(c);
				} catch (Throwable a_th) {
					System.out.println(a_th.getMessage());
				}
			}

			NodeIterator nodes = parentNode.getNodes();
			while (nodes.hasNext()) {
				Node node = (Node) nodes.next();
				BOContent c = new BOContent(node);
				if (c.isSystem()) {
					continue;
				}
				// System.out.println(node.getPrimaryNodeType().getName());
				contents.add(c);
			}

			// session.logout();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return contents;
	}
}