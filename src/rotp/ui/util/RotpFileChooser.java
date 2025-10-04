package rotp.ui.util;


import java.awt.Component;
import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import rotp.ui.game.GameUI;
import rotp.util.Base;
import rotp.util.LabelManager;

public class RotpFileChooser extends JFileChooser implements Base{
	private static final long serialVersionUID = 1L;

	private int posX, posY, width, height; // unscaled values
	boolean hasPosition, hasSize; // unscaled values

	/**
	 * 
	 * @param x unscaled position
	 * @param y unscaled position
	 * @param w unscaled width
	 * @param h unscaled Height
	 */
	public RotpFileChooser(int x, int y, int w, int h)	{
		super();
		setFileChooserFont(getComponents());
		setDLGPosition(x, y);
		setDlgSize(w, h);
	}
	public RotpFileChooser()	{ this(400, 200, 420,470); }

	/**
	 * 
	 * @param x unscaled position
	 * @param y unscaled position
	 */
	public void setDLGPosition(int x, int y)	{
		posX = x;
		posY = y;
		hasPosition = true;
	}
	/**
	 * 
	 * @param w unscaled width
	 * @param h unscaled Height
	 */
	public void setDlgSize(int w, int h)		{
		width = w;
		height = h;
		hasSize = w>0 && h>0;
	}

	@Override protected JDialog createDialog(Component parent) throws HeadlessException	{
		JDialog dlg = super.createDialog(parent);
		if (hasPosition)
			dlg.setLocation(scaled(posX), scaled(posY));
		if (hasSize)
			dlg.setSize(scaled(width), scaled(height));
		dlg.getContentPane().setBackground(GameUI.borderMidColor());
		return dlg;
	}

	@SuppressWarnings("rawtypes")
	private void setFileChooserFont(Component[] comp)	{
		int topInset  = scaled(6);
		int sideInset = scaled(15);
		for(int i=0; i<comp.length; i++) {
			if(comp[i] instanceof JPanel) {
				((JPanel)comp[i]).setBackground(GameUI.borderMidColor());
				if(((JPanel)comp[i]).getComponentCount() !=0)
					setFileChooserFont(((JPanel)comp[i]).getComponents());
			}
			if(comp[i] instanceof JTextField)
				((JTextField)comp[i]).setBackground(GameUI.setupFrame());
			if(comp[i] instanceof JToggleButton)
				((JToggleButton)comp[i]).setBackground(GameUI.setupFrame());
			if(comp[i] instanceof JButton) {
				String txt = ((JButton)comp[i]).getText();
				String cancel = LabelManager.current().label("BUTTON_TEXT_CANCEL");
				String open = LabelManager.current().label("BUTTON_TEXT_OPEN");
				if (txt!=null && (cancel.equals(txt) || open.equals(txt))) {
					((JButton)comp[i]).setMargin(new Insets(topInset, sideInset, 0, sideInset));
					((JButton)comp[i]).setBackground(GameUI.buttonBackgroundColor());
					((JButton)comp[i]).setForeground(GameUI.buttonTextColor());
					((JButton)comp[i]).setVerticalAlignment(SwingConstants.TOP);
				}
			}
			if(comp[i] instanceof JScrollPane)
				((JScrollPane)comp[i]).setBackground(GameUI.borderMidColor());
			if(comp[i] instanceof JList) {
				((JList)comp[i]).setBackground(GameUI.setupFrame());
				((JList)comp[i]).setSelectionBackground(GameUI.borderMidColor());
			}
			if(comp[i] instanceof JComboBox)
				((JComboBox)comp[i]).setBackground(GameUI.setupFrame());
			if(comp[i] instanceof Container)
				setFileChooserFont(((Container)comp[i]).getComponents());
			try {comp[i].setFont(narrowFont(15));}
			catch(Exception e) {} //do nothing
		}
	}
}
