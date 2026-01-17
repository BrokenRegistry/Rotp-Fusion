package rotp.ui.components;

import static java.awt.GridBagConstraints.NONE;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;

import rotp.ui.BasePanel;
import rotp.ui.game.GameUI;
import rotp.util.FontManager;

public interface RotPButtons extends RotPComponents	{
	int MINI_BUTTON		= -1;
	int DEFAULT_BUTTON	= 0;
	int BIG_BUTTON		= 1;
	int HUGE_BUTTON		= 2;
	
	int MINI_FONT_SIZE		= 12;
	int DEFAULT_FONT_SIZE	= 15;
	int BIG_FONT_SIZE		= 20;
	int HUGE_FONT_SIZE		= 30;
	default int buttonSize()			{ return DEFAULT_BUTTON; }
	default int buttonFontSize()		{
		switch(buttonSize()) {
			case MINI_BUTTON:	return MINI_FONT_SIZE;
			case BIG_BUTTON:	return BIG_FONT_SIZE;
			case HUGE_BUTTON:	return HUGE_FONT_SIZE;
			default: 			return DEFAULT_FONT_SIZE;
		}
	}
	default Font buttonFont()			{ return buttonFont(buttonFontSize()); }
	default Font buttonFont(int size)	{ return FontManager.current().narrowFont(size); }
	default Insets getButtonMargin()	{
		switch(buttonSize()) {
			case MINI_BUTTON:	return new Insets(s4, s2, 0, s2);
			case BIG_BUTTON:	return new Insets(s2, s20, s1, s20);
			case HUGE_BUTTON:	return new Insets(s3, s40, s3, s40);
			default: 			return new Insets(s4, s10, s1, s10);
		}	
	}
	default boolean isPaintedBackground()	{ return false; }
	default boolean isShadowedString()		{ return false; }
	default Color getShadowColor()			{ return GameUI.borderDarkColor(); }
	default Paint getBackGroundPaint()		{ return buttonBackgroundColor(); }

	public class RButton extends JButton implements RotPButtons	{
		private static final long serialVersionUID = 1L;
		private boolean showBorder;

		public RButton(String text)	{
			super(text);
			setForeground(buttonTextColor());
			setBackground(buttonBackgroundColor());
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
		@Override public JComponent getComponent()	{ return this; }
		@Override protected void paintComponent(Graphics g)	{
			Graphics2D g2 = (Graphics2D) g;
			int w = getWidth();
			int h = getHeight();

			// Paint Background
			if (isPaintedBackground())
				g2.setPaint(getBackGroundPaint());
			else
				g2.setColor(getBackground());
			g2.fillRoundRect(0, s2, w-1, h-1-s2-s2, cnr, cnr);

			// Paint Borders
			if (showBorder) {
				g2.setStroke(BasePanel.stroke1);
				g2.setColor(highlightColor());
				g2.drawRoundRect(0, s2, w-1, h-1-s2-s2, cnr, cnr);
			}

			// Paint Text
			String text = getText();
			int sw	= g2.getFontMetrics().stringWidth(text);
			int x	= (w-sw)/2;
			int y	= (h*75)/100;
			Color c	= showBorder ? Color.yellow : buttonTextColor();
			g2.setColor(c);
			if (isShadowedString())
				drawShadowedString(g2, text, 2, x, y, getShadowColor(), c);
			else
				drawString(g2, text, x, y);
//			super.paintComponent(g);
		}
		// BR: to let Guide UI access to tool tip text
//		@Override public String getToolTipText(MouseEvent e)	{ return null; }
		private class ButtonMouseAdapter extends MouseAdapter	{
			@Override public void mouseEntered(MouseEvent evt)	{
				showBorder = true;
				setForeground(highlightColor());
				popGuide(getToolTipText());
			}
			@Override public void mouseExited(MouseEvent evt)	{
				showBorder = false;
				setForeground(GameUI.borderBrightColor());
				hideGuide();
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
			super(pane, text, x, y, anchor);
		}
		public RBigButton(Container pane, String text, GridBagConstraints gbc)	{
			super(pane, text, gbc);
		}
		@Override public int buttonSize()	{ return BIG_BUTTON; }
		@Override public boolean isPaintedBackground()	{ return true; }
		@Override public boolean isShadowedString()		{ return true; }
		@Override public Color getShadowColor()			{ return GameUI.borderDarkColor(); }
		@Override public Paint getBackGroundPaint()		{ return GameUI.buttonBackground(0, getWidth()); }
	}

	public class RHugeButton extends RButton	{
		private static final long serialVersionUID = 1L;

		public RHugeButton(String text)	{ super(text); }
		public RHugeButton(Container pane, String text, int x, int y, int anchor)	{
			super(pane, text, x, y, anchor);
		}
		public RHugeButton(Container pane, String text, GridBagConstraints gbc)	{
			super(pane, text, gbc);
		}
		@Override public int buttonSize()				{ return HUGE_BUTTON; }
		@Override public boolean isPaintedBackground()	{ return true; }
		@Override public boolean isShadowedString()		{ return true; }
		@Override public Color getShadowColor()			{ return GameUI.borderDarkColor(); }
		@Override public Paint getBackGroundPaint()		{ return GameUI.buttonBackground(0, getWidth()); }
	}

	public class RToggleButton extends JToggleButton implements RotPButtons	{
		private static final long serialVersionUID = 1L;
		private boolean showBorder;
		
		public RToggleButton(String text)	{
			super(text);
			setForeground(buttonTextColor());
			setBackground(buttonBackgroundColor());
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
		@Override public JComponent getComponent()	{ return this; }
		@Override protected void paintComponent(Graphics g)	{
			Graphics2D g2 = (Graphics2D) g.create();
			g.setFont(getFont());
			int w = getWidth();
			int h = getHeight();
			int sw = g.getFontMetrics().stringWidth(getText());
			int x = (w - sw)/2;
			int y = h*2/3;
			if (isSelected()) {
				//setForeground(buttonBackgroundColor());
				g2.setColor(buttonTextColor());
				g2.fillRoundRect(0, s2, w-1, h-1-s2-s2, cnr, cnr);
				g2.setColor(buttonBackgroundColor());
				g2.setStroke(stroke2);
				g2.drawRoundRect(0, s2, w-1, h-1-s2-s2, cnr, cnr);
			}
			else {
				//setForeground(buttonTextColor());
				g2.setColor(buttonBackgroundColor());
				g2.fillRoundRect(0, s2, w-1, h-1-s2-s2, cnr, cnr);
				g2.setColor(buttonTextColor());
			}
			if (showBorder) {
				g2.setStroke(stroke1);
				g2.setColor(highlightColor());
				g2.drawRoundRect(0, s2, w-1, h-1-s2-s2, cnr, cnr);
			}
			// Sometime Java truncate the text
			g2.drawString(getText(), x, y);
			// super.paintComponent(g);
			g2.dispose();
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
