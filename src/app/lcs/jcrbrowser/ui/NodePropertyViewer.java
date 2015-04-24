package app.lcs.jcrbrowser.ui;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import app.lcs.jcrbrowser.bo.BOContent;
import app.lcs.jcrbrowser.utility.JCRUtil;

/**
 * @author: Aniruddha Dutta Chowdhury (adchowdhury@gmail.com)
 * @since: Mar 20, 2014
 */

public class NodePropertyViewer extends JPanel {
	private JTable			propertyTable	= null;
	private BOContent		content			= null;
	protected Properties	properties		= null;
	protected List			keys			= null;
	private static String[]	columns			= { "Property Name", "Property Value" };

	public NodePropertyViewer(BOContent a_content) {
		content = a_content;
		initUI();
	}

	private void initUI() {
		setLayout(new BorderLayout());

		try {
			properties = JCRUtil.getProperties(content);
			keys = Collections.list(properties.keys());
			
			PropertyClassModel propertyModel = new PropertyClassModel();
			propertyTable = new JTable(propertyModel);
			add(new JScrollPane(propertyTable), BorderLayout.CENTER);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private class PropertyClassModel extends DefaultTableModel {

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public String getColumnName(int column) {
			return columns[column];
		}

		@Override
		public int getRowCount() {
			if(keys == null) {
				
			}
			return keys.size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			switch (column) {
			case 0:
				return keys.get(row);
			case 1:
				return properties.get(keys.get(row));
			}
			return "N/A";
		}
	}
}