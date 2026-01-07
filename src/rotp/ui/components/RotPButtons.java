package rotp.ui.components;

import static java.awt.GridBagConstraints.NONE;

import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JToggleButton;

import rotp.ui.BasePanel;
import rotp.ui.game.GameUI;
import rotp.util.FontManager;

public interface RotPButtons extends RotPComponents	{
	int MINI_BUTTON		= -1;
	int DEFAULT_BUTTON	= 0;
	int BIG_BUTTON		= 1;
	
	int miniFontSize 	= 12;
	int defaultFontSize = 14;
	int bigFontSize 	= 16;
	default int buttonSize()			{ return DEFAULT_BUTTON; }
	default int buttonFontSize()		{
		switch(buttonSize()) {
			case MINI_BUTTON:	return miniFontSize;
			case BIG_BUTTON:	return bigFontSize;
			default: 			return defaultFontSize;
		}
	}
	default Font buttonFont()			{ return buttonFont(buttonFontSize()); }
	default Font buttonFont(int size)	{ return FontManager.current().narrowFont(size); }
	default Insets getButtonMargin()	{
		switch(buttonSize()) {
			case MINI_BUTTON:	return new Insets(s5, s2, 0, s2);
			case BIG_BUTTON:	return new Insets(s6, s20, 0, s20);
			default: 			return new Insets(s5, s10, 0, s10);
		}	
	}

	public class RButton extends JButton implements RotPButtons	{
		private static final long serialVersionUID = 1L;
		private boolean showBorder;

		public RButton(String text)	{
			super(text);
			setForeground(buttonTextColor());
			setMargin(getButtonMargin());
			setVerticalTextPosition(AbstractButton.BOTTOM);
			setHorizontalTextPosition(AbstractButton.CENTER);
			setFont(buttonFont());
			setOpaque(false);
			setFocusPainted(false);
			setBorderPainted(false);
			setContentAreaFilled(false);
			addMouseListener(new ButtonMouseAdapter());
		}
		public RButton(Container pane, String text, int x, int y, int anchor)	{
			this(text);
			pane.add(this, newGbc(x,y, 1,1, 0,0, anchor, NONE, getButtonMargin(), 0,0));
		}
		public RButton(Container pane, String text, GridBagConstraints gbc)	{
			this(text);
			pane.add(this, gbc);
		}
		public void setLabelKey()			{ setLabelKey(getText()); }
		public void setLabelKey(String key) {
				setText(text(key));
				setToolTipText(htmlText(key + LABEL_DESCRIPTION));
		}
		@Override protected void paintComponent(Graphics g)	{
			Graphics2D g2 = (Graphics2D) g;
			Rectangle bounds = getBounds();
			g2.setColor(buttonBackgroundColor());
			g2.fillRoundRect(0, s2, bounds.width-1, bounds.height-1-s2-s2, cnr, cnr);
			if (showBorder) {
				g2.setStroke(BasePanel.stroke1);
				g2.setColor(highlightColor());
				g2.drawRoundRect(0, s2, bounds.width-1, bounds.height-1-s2-s2, cnr, cnr);
			}
			super.paintComponent(g);
		}
		private class ButtonMouseAdapter extends MouseAdapter	{
			@Override public void mouseEntered(MouseEvent evt)	{
				showBorder = true;
				setForeground(highlightColor());
			}
			@Override public void mouseExited(MouseEvent evt)	{
				showBorder = false;
				setForeground(GameUI.borderBrightColor());
			}
			@Override public void mousePressed(MouseEvent evt)	{}
			@Override public void mouseReleased(MouseEvent evt)	{}
		}
	}

	public class RMiniButton extends RButton	{
		private static final long serialVersionUID = 1L;

		public RMiniButton(String text)	{ super(text); }
		public RMiniButton(Container pane, String text, int x, int y, int anchor)	{
			this(text);
			pane.add(this, newGbc(x,y, 1,1, 0,0, anchor, NONE, getButtonMargin(), 0,0));
		}
		public RMiniButton(Container pane, String text, GridBagConstraints gbc)	{
			this(text);
			pane.add(this, gbc);
		}
		@Override public int buttonSize()	{ return MINI_BUTTON; }
	}
	
	public class RBigButton extends RButton	{
		private static final long serialVersionUID = 1L;

		public RBigButton(String text)	{ super(text); }
		public RBigButton(Container pane, String text, int x, int y, int anchor)	{
			this(text);
			pane.add(this, newGbc(x,y, 1,1, 0,0, anchor, NONE, getButtonMargin(), 0,0));
		}
		public RBigButton(Container pane, String text, GridBagConstraints gbc)	{
			this(text);
			pane.add(this, gbc);
		}
		@Override public int buttonSize()	{ return BIG_BUTTON; }
	}
	
	public class RToggleButton extends JToggleButton implements RotPButtons	{
		private static final long serialVersionUID = 1L;
		private boolean showBorder;
		
		public RToggleButton(String text)	{
			super(text);
			setForeground(buttonTextColor());
			setMargin(getButtonMargin());
			setVerticalTextPosition(AbstractButton.BOTTOM);
			setHorizontalTextPosition(AbstractButton.CENTER);
			setFont(buttonFont());
			setFocusPainted(false);
			setBorderPainted(false);
			setContentAreaFilled(false);
			addMouseListener(new ButtonMouseAdapter());
		}
		public RToggleButton(Container pane, String text, int x, int y, int anchor)	{
			this(text);
			pane.add(this, newGbc(x,y, 1,1, 0,0, anchor, NONE, getButtonMargin(), 0,0));
		}
		public RToggleButton(Container pane, String text, GridBagConstraints gbc)	{
			this(text);
			pane.add(this, gbc);
		}
		public void toggle()	{ setEnabled(!isEnabled()); }

		@Override protected void paintComponent(Graphics g)	{
			Graphics2D g2 = (Graphics2D) g;
			Rectangle bounds = getBounds();
			g.setFont(getFont());
			int sw = g.getFontMetrics().stringWidth(getText());
			int x = (bounds.width - sw)/2;
			int y = bounds.height*2/3;
			if (isSelected()) {
				//setForeground(buttonBackgroundColor());
				g2.setColor(buttonTextColor());
				g2.fillRoundRect(0, s2, bounds.width-1, bounds.height-1-s2-s2, cnr, cnr);
				g2.setColor(buttonBackgroundColor());
				g2.setStroke(stroke2);
				g2.drawRoundRect(0, s2, bounds.width-1, bounds.height-1-s2-s2, cnr, cnr);
			}
			else {
				//setForeground(buttonTextColor());
				g2.setColor(buttonBackgroundColor());
				g2.fillRoundRect(0, s2, bounds.width-1, bounds.height-1-s2-s2, cnr, cnr);
				g2.setColor(buttonTextColor());
			}
			if (showBorder) {
				g2.setStroke(stroke1);
				g2.setColor(highlightColor());
				g2.drawRoundRect(0, s2, bounds.width-1, bounds.height-1-s2-s2, cnr, cnr);
			}
			// Sometime Java truncate the text
			g2.drawString(getText(), x, y);
			// super.paintComponent(g);
		}
		private class ButtonMouseAdapter extends MouseAdapter	{
			@Override public void mouseEntered(MouseEvent evt)	{
				showBorder = true;
				setForeground(highlightColor());
			}
			@Override public void mouseExited(MouseEvent evt)	{
				showBorder = false;
				setForeground(buttonTextColor());
				repaint(); // to remove the border
			}
			@Override public void mousePressed(MouseEvent evt)	{}
			@Override public void mouseReleased(MouseEvent evt)	{}
		}
	}

	public class RMiniToggleButton extends RToggleButton	{
		private static final long serialVersionUID = 1L;

		public RMiniToggleButton(String text)	{
			super(text);
		}
		public RMiniToggleButton(String text, String name)	{
			super(text);
			setName(name);
		}
		public RMiniToggleButton(Container pane, String text, int x, int y, int anchor)	{
			this(text);
			pane.add(this, newGbc(x,y, 1,1, 0,0, anchor, NONE, getButtonMargin(), 0,0));
		}
		public RMiniToggleButton(Container pane, String text, GridBagConstraints gbc)	{
			this(text);
			pane.add(this, gbc);
		}
		@Override public int buttonSize()	{ return MINI_BUTTON; }
	}

	public class RBigToggleButton extends RToggleButton	{
		private static final long serialVersionUID = 1L;

		public RBigToggleButton(String text)	{
			super(text);
		}
		public RBigToggleButton(Container pane, String text, int x, int y, int anchor)	{
			this(text);
			pane.add(this, newGbc(x,y, 1,1, 0,0, anchor, NONE, getButtonMargin(), 0,0));
		}
		public RBigToggleButton(Container pane, String text, GridBagConstraints gbc)	{
			this(text);
			pane.add(this, gbc);
		}
		@Override public int buttonSize()	{ return BIG_BUTTON; }
	}
}
