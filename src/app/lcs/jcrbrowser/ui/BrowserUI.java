package app.lcs.jcrbrowser.ui;

import static app.lcs.jcrbrowser.utility.ApplicationContext.applicationProperties;
import static app.lcs.jcrbrowser.utility.ApplicationContext.frmMain;
import static app.lcs.jcrbrowser.utility.ApplicationContext.session;
import static app.lcs.jcrbrowser.utility.Constants.REPOSITORY_PASSWORD;
import static app.lcs.jcrbrowser.utility.Constants.REPOSITORY_URL;
import static app.lcs.jcrbrowser.utility.Constants.REPOSITORY_USERNAME;
import static app.lcs.jcrbrowser.utility.Constants.ROOT_WORKSPACE_ID;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.jackrabbit.commons.JcrUtils;

import app.lcs.jcrbrowser.Startup;
import app.lcs.jcrbrowser.bo.BOContent;
import app.lcs.jcrbrowser.utility.ApplicationContext;
import app.lcs.jcrbrowser.utility.Constants;
import app.lcs.jcrbrowser.utility.JCRUtil;

/**
 * @author: Aniruddha Dutta Chowdhury (adchowdhury@gmail.com)
 * @since: Mar 3, 2014
 */

public class BrowserUI extends JPanel implements MouseListener, ActionListener {
	private JList				lstObjects		= null;
	private DefaultListModel	listModel		= null;
	private JTextField			txtPath			= null;
	private JButton				browseJCR		= null;
	private JButton				clearAll		= null;
	private JButton				browseNode		= null;

	private JPopupMenu			listContextMenu	= null;
	private JMenuItem			download		= null;
	private JMenuItem			createFolder	= null;
	private JMenuItem			uploadFolder	= null;
	private JMenuItem			uploadFile		= null;
	private JMenuItem			showProperties	= null;
	
	private BOContent			currentContent	= null;

	public BrowserUI() {
		initUI();
	}

	private void initUI() {
		txtPath = new JTextField();
		txtPath.setEditable(false);
		
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		browseJCR = new JButton(renderer.getDefaultOpenIcon());
		browseJCR.setToolTipText("Click this for selecting JCR...");
		
		browseJCR.addActionListener(this);
		
		clearAll = new JButton("X");
		clearAll.setToolTipText("Click this to clear current selection of JCR & all");
		clearAll.addActionListener(this);
		clearAll.setEnabled(false);
		
		browseNode = new JButton(new ImageIcon(Startup.class.getResource("/app/lcs/jcrbrowser/ui/browse.png")));
		browseNode.setToolTipText("Click this to directly goto any valid path");
		browseNode.addActionListener(this);
		browseNode.setEnabled(false);
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(txtPath, BorderLayout.CENTER);
		
		
		JPanel pnlTopButton = new JPanel(new GridLayout(1, 2));
		pnlTopButton.add(browseJCR);
		pnlTopButton.add(browseNode);
		pnlTopButton.add(clearAll);
		
		topPanel.add(pnlTopButton, BorderLayout.EAST);
		
		lstObjects = new JList(listModel = new DefaultListModel());
		lstObjects.setCellRenderer(new CustomListCellRenderer());
		lstObjects.addMouseListener(this);

		lstObjects.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		/*txtPath.setText(JCRUtil.getWorkspacePath());
		List<BOContent> contents = JCRUtil.getContents(null);
		for (BOContent boContent : contents) {
			listModel.addElement(boContent);
		}*/
		setLayout(new BorderLayout());
		add(new JScrollPane(lstObjects), BorderLayout.CENTER);
		add(topPanel, BorderLayout.NORTH);
		
		listContextMenu = new JPopupMenu();
		
		download = new JMenuItem("Download");
		createFolder = new JMenuItem("Create Folder");
		uploadFolder = new JMenuItem("Upload Folder");
		uploadFile = new JMenuItem("Upload File");
		showProperties = new JMenuItem("Show Properties");
		
		download.addActionListener(this);
		createFolder.addActionListener(this);
		uploadFolder.addActionListener(this);
		uploadFile.addActionListener(this);
		showProperties.addActionListener(this);
		
		
		download.setIcon(new ImageIcon(Startup.class.getResource("/app/lcs/jcrbrowser/ui/download.png")));
		uploadFile.setIcon(new ImageIcon(Startup.class.getResource("/app/lcs/jcrbrowser/ui/upload.png")));
		//browseNode.setIcon(new ImageIcon(Startup.class.getResource("/app/lcs/jcrbrowser/ui/browse.png")));
		
	}
	
	private void prepareFolderPopupMenu() {
		listContextMenu.removeAll();
		
		//listContextMenu.add(download);
		listContextMenu.add(createFolder);
		//listContextMenu.add(uploadFolder);
		listContextMenu.add(uploadFile);
		
		listContextMenu.addSeparator();
		listContextMenu.add(showProperties);
	}
	
	private void prepareFilePopupMenu() {
		listContextMenu.removeAll();
		
		listContextMenu.add(download);
		
		listContextMenu.addSeparator();
		listContextMenu.add(showProperties);
	}

	static class CustomListCellRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (comp instanceof JLabel) {
				JLabel lbl = (JLabel) comp;
				DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();

				Icon leafIcon = renderer.getDefaultLeafIcon();
				// Icon openIcon = renderer.getDefaultOpenIcon( );
				Icon closedIcon = renderer.getDefaultClosedIcon();
				if (((BOContent) value).isFolder()) {
					lbl.setIcon(closedIcon);
				} else {
					lbl.setIcon(leafIcon);
				}

			}
			return comp;
		}
	}
	
	private void reloadList(BOContent content) {
		if(DialogUtility.isWaitDialogOn() == false) {
			DialogUtility.showWaitDialog("Please wait, while we load the repository");
		}
		listModel.clear();
		try {
			txtPath.setText(JCRUtil.getWorkspacePath() + content.getNode().getPath());
			//System.out.println("its here " + content.getNode().getPath());
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		List<BOContent> contents = JCRUtil.getContents(content.getNode());
		for (BOContent boContent : contents) {
			listModel.addElement(boContent);
			//System.out.println(boContent);
		}
		lstObjects.updateUI();
		DialogUtility.hideWaitDialog();
	}
	
	private boolean isRightClick(MouseEvent event) {
		if(event == null) {
			return false;
		}else if(event.getButton() == MouseEvent.BUTTON3) {
			return true;
		}else {
			String osName = System.getProperty("os.name");
			if(osName != null && osName.toLowerCase().indexOf("mac") >= 0) {
				if(event.getButton() == MouseEvent.BUTTON1 && event.isControlDown()) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		if (event.getClickCount() != 2 || 
				isRightClick(event) || 
				txtPath.getText().trim().length() <= 0) {
			return;
		}
		int index = lstObjects.getSelectedIndex();
		if (listModel.size() <= index) {
			return;
		}
		BOContent content = (BOContent) listModel.get(index);
		if (content.isFolder() == false) {
			System.out.println("its a file");
			return;
		}
		
		currentContent = content;
		
		reloadList(content);
	}

	@Override
	public void mousePressed(MouseEvent event) {
		
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		if(isRightClick(event) && txtPath.getText().trim().length() > 0) {
			int index = lstObjects.getSelectedIndex();
			//System.out.println(index);
			BOContent content = null;
			if(index < 1) {
				content = currentContent;
			}else {
				content = (BOContent) listModel.get(index);
			}
			try {
				System.out.println(content.getNode().getPath());
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
			if (content.isFolder() == false) {
				prepareFilePopupMenu();
			}else {
				prepareFolderPopupMenu();
			}
			listContextMenu.show(lstObjects, event.getX(), event.getY()); //and show the menu
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}
	
	private void clearData() {
		currentContent = null;
		listModel.clear();
		lstObjects.updateUI();
		applicationProperties.clear();
		txtPath.setText("");
		ApplicationContext.frmMain.setTitle("Please select a repository to browse...");
	}
	
	private void openRepository(String strRepositoryURL, String strWorkspaceID, String a_strUserName, String a_strPassword) {
		applicationProperties.clear();
		applicationProperties.put(REPOSITORY_URL, strRepositoryURL);
		applicationProperties.put(ROOT_WORKSPACE_ID, strWorkspaceID);
		applicationProperties.put(REPOSITORY_USERNAME, strWorkspaceID);
		applicationProperties.put(REPOSITORY_PASSWORD, strWorkspaceID);
		
		JCRUtil.initSession();
		if(ApplicationContext.session != null){
			txtPath.setText(JCRUtil.getWorkspacePath());
			ApplicationContext.frmMain.setTitle("Browsing: " + applicationProperties.getProperty(REPOSITORY_URL) + "/" + applicationProperties.getProperty(ROOT_WORKSPACE_ID));
			try {
				currentContent = new BOContent(session.getRootNode());
				reloadList(currentContent);
				DialogUtility.hideWaitDialog();
			} catch (RepositoryException a_th) {
				System.out.println(a_th.getMessage());
				DialogUtility.hideWaitDialog();
				JOptionPane.showMessageDialog(null, a_th.getMessage(), "Couldn't open webdav to: " + applicationProperties.getProperty(REPOSITORY_URL), JOptionPane.ERROR_MESSAGE);
				clearData();
			}
		}else {
			clearData();
			DialogUtility.hideWaitDialog();
		}
	}
	
	private void browseNode() {
		String str = JOptionPane.showInputDialog(null, "Enter path : ", "Folder path", 1);
		
		if(str == null || str.trim().length() <= 0) {
			return;
		}
		currentContent = new BOContent(JCRUtil.getNodeByAbsolutePath(str));
		
		if(currentContent.getNode() != null) {
			reloadList(currentContent);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		
		if(actionEvent.getSource() == browseNode) {
			browseNode();
			return;
		}else if(actionEvent.getSource() == clearAll) {
			clearData();
			browseNode.setEnabled(false);
			clearAll.setEnabled(false);
			return;
		}else if(actionEvent.getSource() == browseJCR) {
			JTextField repoURL = new JTextField(25);
			JTextField workspaceID = new JTextField();
			JTextField username = new JTextField(25);
			JPasswordField password = new JPasswordField();
			
			repoURL.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					((JTextField)e.getComponent()).selectAll();
				}
			});
			
			workspaceID.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					((JTextField)e.getComponent()).selectAll();
				}
			});
			
			username.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					((JTextField)e.getComponent()).selectAll();
				}
			});
			
			password.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					((JPasswordField)e.getComponent()).selectAll();
				}
			});
			
			
			repoURL.setText("http://10.10.1.240:8180/jackrabbit/server");
			workspaceID.setText("default");
			username.setText("admin");

			JPanel myPanel = new JPanel(new BorderLayout(10, 10));
			JPanel leftPanel = new JPanel(new GridLayout(4, 1));
			JPanel middlePanel = new JPanel(new GridLayout(4, 1));
			leftPanel.add(new JLabel("Reository URL:"));
			leftPanel.add(new JLabel("Workspace:"));
			leftPanel.add(new JLabel("Username:"));
			leftPanel.add(new JLabel("Password:"));
			
			middlePanel.add(repoURL);
			middlePanel.add(workspaceID);
			middlePanel.add(username);
			middlePanel.add(password);
			
			myPanel.add(leftPanel, BorderLayout.WEST);
			myPanel.add(middlePanel, BorderLayout.CENTER);

			int result = JOptionPane.showConfirmDialog(this, myPanel, "Please Enter Repository URL and Workspace ID", JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				final String strRepositoryURL = repoURL.getText();
				final String strWorkspaceID = workspaceID.getText();
				final String strUserName = username.getText();
				final String strPassword = password.getText();
				
				if(strRepositoryURL != null && strRepositoryURL.trim().length() > 0 &&
						strWorkspaceID != null && strWorkspaceID.trim().length() > 0) {
						Thread th = new Thread(new Runnable() {
							@Override
							public void run() {
								openRepository(strRepositoryURL, strWorkspaceID, strUserName, strPassword);
								clearAll.setEnabled(true);
								browseNode.setEnabled(true);
							}
						});
						th.start();
						DialogUtility.showWaitDialog("Please wait, while we load the repository");
				}
			}
		}
		
		int index = lstObjects.getSelectedIndex();
		System.out.println(index);
		BOContent content = null;
		if(index < 1) {
			content = currentContent;
		}else {
			content = (BOContent) listModel.get(index);
		}
		
		if(download == actionEvent.getSource()) {
			if(content.isFolder()) {
				
			}else {
				JFileChooser fc = new JFileChooser();
				try {
					fc.setSelectedFile(new File(content.getNode().getName()));
				} catch (RepositoryException e) {
					e.printStackTrace();
				}
				int returnVal = fc.showSaveDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					DialogUtility.showWaitDialog("Please wait, while we download the file");
					try {
						File file = fc.getSelectedFile();
						
						InputStream contentStrm = content.getNode().getNode("jcr:content").getProperty("jcr:data").getBinary().getStream();
						OutputStream outputStream = new FileOutputStream(file);
						int read = 0;
						byte[] bytes = new byte[1024];
				 
						while ((read = contentStrm.read(bytes)) != -1) {
							outputStream.write(bytes, 0, read);
						}
						
						outputStream.flush();
						outputStream.close();
						contentStrm.close();
						DialogUtility.hideWaitDialog();
						JOptionPane.showMessageDialog(this, "File successfully downloaded");
					} catch (Throwable a_th) {
						a_th.printStackTrace();
						DialogUtility.hideWaitDialog();
						JOptionPane.showMessageDialog(null, a_th.getMessage(), "Couldn't download file", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}else if(createFolder == actionEvent.getSource()) {
			String str = JOptionPane.showInputDialog(null, "Enter folder name : ", "Folder name", 1);
			System.out.println(str);
			
			JCRUtil.createFolder(content.getNode(), str);
			
			reloadList(content);
			
			JOptionPane.showMessageDialog(this, "Folder successfully created");
		}else if(uploadFile == actionEvent.getSource()) {		
			
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fc.getSelectedFile();
	            try {
	            	DialogUtility.showWaitDialog("Please wait, while we upload the file");
	            	FileInputStream fis = new FileInputStream(file);
	    			JcrUtils.putFile(content.getNode(), file.getName(), file.toURL().openConnection().getContentType(), fis);
	    			session.save();
	    			reloadList(content);
	    			DialogUtility.hideWaitDialog();
	    			JOptionPane.showMessageDialog(this, "File successfully uploaded");
	    			fis.close();
	    		} catch (Throwable a_th) {
	    			a_th.printStackTrace();
	    			DialogUtility.hideWaitDialog();
					JOptionPane.showMessageDialog(frmMain, a_th.getMessage(), "Couldn't upload file", JOptionPane.ERROR_MESSAGE);
	    		}
	            //System.out.println(file);
	        }
		}else if(showProperties == actionEvent.getSource()) {
			JOptionPane.showMessageDialog(frmMain, new NodePropertyViewer(content), "Property", JOptionPane.INFORMATION_MESSAGE);
		}
	}
}