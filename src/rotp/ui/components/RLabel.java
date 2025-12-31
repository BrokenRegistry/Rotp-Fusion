package rotp.ui.components;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import rotp.util.FontManager;

public class RLabel extends JLabel implements RotPComponents	{
	private static final long serialVersionUID = 1L;
	private static final EmptyBorder border = new EmptyBorder(s4, s4, 0, s4);
	protected int labelFontSize	= 12;
	protected Font labelFont	= FontManager.current().narrowFont(labelFontSize);
	
	public RLabel(String text)	{
		super(text);
		setForeground(labelTextColor());
		setFont(labelFont);
		setBorder(border);
	}
	public RLabel(Container pane, String text, GridBagConstraints gbc)	{
		this(text);
		pane.add(this, gbc);
	}
	protected Color labelTextColor()	{ return Color.WHITE; }
}
