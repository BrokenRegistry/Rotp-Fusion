package rotp.ui.components;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;

import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import rotp.util.FontManager;

public class RTextPane extends JTextPane implements RotPComponents	{
	private static final long serialVersionUID = 1L;
	private static final EmptyBorder border = new EmptyBorder(s4, s4, 0, s4);
	protected int labelFontSize	= 12;
	protected Font labelFont()	{ return FontManager.current().narrowFont(labelFontSize); }
	
	public RTextPane()	{
		super();
		setForeground(labelTextColor());
		setFont(labelFont());
		setBorder(border);
	}
	public RTextPane(int fontSize, Color fontColor)	{
		super();
		setForeground(fontColor);
		labelFontSize = fontSize;
		setFont(labelFont());
		setBorder(border);
//		addMouseListener(new LabelMouseAdapter());
	}
	public RTextPane(Container pane, GridBagConstraints gbc)	{
		this();
		pane.add(this, gbc);
	}
	protected Color labelTextColor()	{ return Color.WHITE; }

	@Override public JComponent getComponent()	{ return this; }
//	@Override public String getToolTipText(MouseEvent e)	{ return null; }
//	private class LabelMouseAdapter extends MouseAdapter	{
//		@Override public void mouseEntered(MouseEvent evt)	{
//			//showBorder = true;
//			setForeground(highlightColor());
//		}
//		@Override public void mouseExited(MouseEvent evt)	{
//			//showBorder = false;
//			setForeground(GameUI.borderBrightColor());
//		}
//		@Override public void mousePressed(MouseEvent evt)	{}
//		@Override public void mouseReleased(MouseEvent evt)	{}
//	}

}
