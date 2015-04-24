package app.lcs.jcrbrowser.utility;

import java.util.Properties;

import javax.jcr.Session;
import javax.swing.JFrame;

/**
 * @author: Aniruddha Dutta Chowdhury (adchowdhury@gmail.com)
 * @since: Mar 3, 2014
 */

public final class ApplicationContext {
	public static Properties	applicationProperties	= new Properties();
	public static Session		session					= null;
	public static JFrame		frmMain					= null;
}
