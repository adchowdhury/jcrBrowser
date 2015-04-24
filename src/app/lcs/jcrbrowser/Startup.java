package app.lcs.jcrbrowser;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import app.lcs.jcrbrowser.ui.BrowserUI;
import app.lcs.jcrbrowser.utility.ApplicationContext;

/**
 * @author: Aniruddha Dutta Chowdhury (adchowdhury@gmail.com)
 * @since: Mar 3, 2014
 */

public class Startup {
	public static void main(String[] args) {
		// take the menu bar off the jframe
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		// set the name of the application menu item
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "JCR Browser");

		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		final JFrame frmMain = new JFrame();
		ApplicationContext.frmMain = frmMain;

		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			private boolean	isAboutOn	= false;
			JEditorPane		editorPane	= null;

			public void eventDispatched(AWTEvent event) {
				if (event instanceof KeyEvent && event.getID() == KeyEvent.KEY_RELEASED) {
					if (((KeyEvent) event).getKeyCode() == KeyEvent.VK_F1) {
						if (isAboutOn) {
							return;
						}
						if (editorPane == null) {
							try {
								editorPane = new JEditorPane(Startup.class.getClassLoader().getResource("app/lcs/jcrbrowser/resources/about.html"));
								editorPane.setPreferredSize(new Dimension(300, 70));
								editorPane.setEditable(false);
								editorPane.setContentType("text/html");
							} catch (IOException e) {
								e.printStackTrace();
								return;
							}
						}

						isAboutOn = true;
						JOptionPane.showMessageDialog(frmMain, new JScrollPane(editorPane), "About JCR Browser", JOptionPane.INFORMATION_MESSAGE);
						isAboutOn = false;
					}
				}
			}
		}, AWTEvent.KEY_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK);

		URL iconURL = Startup.class.getResource("/app/lcs/jcrbrowser/ui/favicon.png");
		// iconURL is null when not found
		ImageIcon icon = new ImageIcon(iconURL);
		frmMain.setIconImage(icon.getImage());

		frmMain.setSize(750, 550);
		frmMain.setLayout(new BorderLayout());
		frmMain.add(new BrowserUI(), BorderLayout.CENTER);
		frmMain.setTitle("Please select a repository to browse...");
		// frmMain.setTitle("Browsing: " +
		// applicationProperties.getProperty(REPOSITORY_URL) + "/" +
		// applicationProperties.getProperty(ROOT_WORKSPACE_ID));
		frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmMain.setLocationRelativeTo(null);
		frmMain.setVisible(true);

	}
}