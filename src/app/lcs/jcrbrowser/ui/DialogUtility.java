package app.lcs.jcrbrowser.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import app.lcs.jcrbrowser.utility.ApplicationContext;

public class DialogUtility {
	private static final Dimension	standardDimention	= new Dimension(600, 400);
	private static JDialog			waitDialog			= null;
	private static boolean			isWaitDialogOn		= false;
	private static JLabel			waitDialogLabel		= null;
	private static JProgressBar		waitProgressBar		= null;

	public static void showDialog(JComponent child, String strCaption) {
		showDialog(child, strCaption, standardDimention);
	}

	public static JDialog showDialog(JComponent child, String strCaption, Dimension size) {
		return showDialog(child, strCaption, size, true);
	}

	public static JDialog showDialog(final JComponent child, String strCaption, Dimension size, boolean isResizable) {
		final JDialog dialog = new JDialog(ApplicationContext.frmMain, strCaption) {
			@Override
			public void dispose() {
				super.dispose();
			}
		};
		dialog.setLayout(new BorderLayout());
		
		dialog.add(child, BorderLayout.CENTER);
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setSize(size);
		if(!size.equals(standardDimention)) {
			dialog.setMinimumSize(size);
		}
		dialog.setLocationRelativeTo(ApplicationContext.frmMain);
		dialog.setResizable(isResizable);
		if(waitDialog != null) {
			hideWaitDialog();
		}
		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				dialog.setVisible(true);
			}
		});
		th.start();
		
		return dialog;
	}

	public static JDialog getRoot(JComponent pchildComponent) {
		Container objParent = pchildComponent.getParent();
		while (objParent != null) {
			objParent = objParent.getParent();
			if (objParent instanceof JDialog) {
				return (JDialog) objParent;
			}
		}
		return null;
	}

	public static void closeParent(JComponent childComponent) {
		JDialog parent = getRoot(childComponent);
		if (parent != null) {
			parent.dispose();
		}
	}

	public static void showWaitDialog(String waitingText) {
		if (waitDialog == null) {
			initWaitDialog();
		}
		isWaitDialogOn = true;
		waitDialogLabel.setText(waitingText);
		waitDialog.setSize(new Dimension(350, 100));
		waitDialog.setLocationRelativeTo(null);
		waitProgressBar.setIndeterminate(true);
		
		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				waitDialog.setVisible(true);
			}
		});
		th.start();
	}

	private static void initWaitDialog() {
		waitDialog = new JDialog(ApplicationContext.frmMain, "Please Wait");
		waitDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		waitDialog.setSize(new Dimension(350, 100));
		waitDialog.setLayout(new BorderLayout());
		waitDialogLabel = new JLabel();
		waitProgressBar = new JProgressBar();
		waitDialog.setModal(true);
		waitDialogLabel.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
		waitDialog.add(waitDialogLabel, BorderLayout.CENTER);
		waitDialog.add(waitProgressBar, BorderLayout.SOUTH);
	}

	public static void hideWaitDialog() {
		waitProgressBar.setIndeterminate(false);
		waitDialog.setVisible(false);
		isWaitDialogOn = false;
	}

	public static boolean isWaitDialogOn() {
		return isWaitDialogOn;
	}
}