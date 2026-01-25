package rotp.ui.components;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import rotp.ui.game.GameUI;
import rotp.ui.util.IParam;
import rotp.util.FontManager;

public class RLabel extends JLabel implements RotPComponents	{
	private static final long serialVersionUID = 1L;
	private static final EmptyBorder border = new EmptyBorder(s4, s4, 0, s4);
	protected int labelFontSize	= 12;
	protected Font labelFont()	{ return FontManager.current().narrowFont(labelFontSize); }
	protected IParam<?> param;

	public RLabel(String text)	{
		super(text);
		setForeground(labelTextColor());
		setFont(labelFont());
		setBorder(border);
		addMouseListener(new LabelMouseAdapter());
	}
	public RLabel(String text, int fontSize, Color fontColor)	{
		super(text);
		setForeground(fontColor);
		labelFontSize = fontSize;
		setFont(labelFont());
		setBorder(border);
		addMouseListener(new LabelMouseAdapter());
	}
	public RLabel(Container pane, String text, GridBagConstraints gbc)	{
		this(text);
		pane.add(this, gbc);
	}
	protected Color labelTextColor()	{ return Color.WHITE; }

	public void setParam(IParam<?> param)		{ this.param = param; }
	@Override public IParam<?> getParam()		{ return param; }
	@Override public JComponent getComponent()	{ return this; }
	private class LabelMouseAdapter extends MouseAdapter	{
		@Override public void mouseEntered(MouseEvent evt)	{
			//showBorder = true;
			if (showGuide()) {
				setForeground(highlightColor());
				popGuide();
			}
		}
		@Override public void mouseExited(MouseEvent evt)	{
			//showBorder = false;
			setForeground(GameUI.borderBrightColor());
			hideGuide();
		}
		@Override public void mousePressed(MouseEvent evt)	{}
		@Override public void mouseReleased(MouseEvent evt)	{}
	}

}
