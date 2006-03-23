package pcgen.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Table column manager
 */
public class TableColumnManager implements MouseListener {

	private JPopupMenu tablePopup;
	private List checkBoxList;
	private JTable table;
	private JComponent tableButton;
	private TableColumnManagerModel model;
	
	/**
	 * Constructor
	 * @param table
	 * @param tableButton
	 * @param model
	 */
	public TableColumnManager(JTable table, JComponent tableButton, TableColumnManagerModel model) {
		this.table = table;
		this.tableButton = tableButton;
		this.model = model;
		initContents();
	}
	
	private void initContents() {
		tablePopup = new JPopupMenu();
		checkBoxList = new ArrayList();
		
		tablePopup = new javax.swing.JPopupMenu();
		for(int i = 0; i < model.getMColumnList().size(); i++) {
			String name = (String)model.getMColumnList().get(i);
			boolean selected = model.isMColumnDisplayed(i + model.getMColumnOffset());
			JCheckBoxMenuItem popupCb = new JCheckBoxMenuItem();
			tablePopup.add(popupCb);
			popupCb.setText(name);
			popupCb.setSelected(selected);
			popupCb.addActionListener(new PopupActionListener(popupCb, i + model.getMColumnOffset()));
			checkBoxList.add(popupCb);
		}
		tableButton.addMouseListener(this);
		TablePopupActionPerformed();
	}

	/**
	 * Display the table
	 * @param evt
	 */
	public void tableDisplay(java.awt.event.MouseEvent evt) {
		tablePopup.show(evt.getComponent(), evt.getX(), evt.getY());
	}
	
	private void TablePopupActionPerformed() {
		TableColumnModel colModel = table.getColumnModel();
		while(colModel.getColumnCount() > 1) {
			TableColumn col = colModel.getColumn(1);
			colModel.removeColumn(col);
		}
		for(int i = 0; i < checkBoxList.size(); i++) {
			JCheckBoxMenuItem cb = (JCheckBoxMenuItem)checkBoxList.get(i);
			model.setMColumnDisplayed(i + model.getMColumnOffset(), cb.isSelected());
			if(cb.isSelected()) {
				TableColumn col = new TableColumn(i + model.getMColumnOffset());
				col.setHeaderValue(cb.getText());
				col.setWidth(model.getMColumnDefaultWidth(i + model.getMColumnOffset()));
				col.setPreferredWidth(model.getMColumnDefaultWidth(i + model.getMColumnOffset()));
				col.addPropertyChangeListener(new ColumnChangeListener(i + model.getMColumnOffset())); 
				colModel.addColumn(col);
			}
		}
		for(int i = 0; i < model.getMColumnOffset(); i++) {
			TableColumn col = new TableColumn(i);
			col.setWidth(model.getMColumnDefaultWidth(i));
			col.setPreferredWidth(model.getMColumnDefaultWidth(i));
			col.addPropertyChangeListener(new ColumnChangeListener(i)); 
		}
	}
	
	private class PopupActionListener implements ActionListener {
		JCheckBoxMenuItem popupCb;
		int colNo = 0;
		
		/**
		 * Constructor
		 * @param popupCb
		 * @param colNo
		 */
		public PopupActionListener(JCheckBoxMenuItem popupCb, int colNo) {
			this.popupCb = popupCb;
			this.colNo = colNo;
		}

		public void actionPerformed(ActionEvent e) {
			TablePopupActionPerformed();
		}

	}

	public void mouseClicked(MouseEvent e) {
		tableDisplay(e);
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Do nothing?
	}

	public void mouseExited(MouseEvent e) {
		// TODO Do nothing?
	}

	public void mousePressed(MouseEvent e) {
		tableDisplay(e);
	}

	public void mouseReleased(MouseEvent e) {
		tableDisplay(e);
	}

	private class ColumnChangeListener implements PropertyChangeListener {
		int col = 0;
		
		/**
		 * Constructor
		 * @param col
		 */
		public ColumnChangeListener(int col) {
			this.col = col;
		}
		
		public void propertyChange(PropertyChangeEvent evt) {
			TableColumn tcol = (TableColumn)evt.getSource();
			model.setMColumnDefaultWidth(col, tcol.getWidth());
		}
	}

}
