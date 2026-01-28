package rotp.ui.components;

import static java.awt.GridBagConstraints.NONE;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Function;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;

import rotp.ui.BasePanel;
import rotp.ui.game.GameUI;
import rotp.ui.util.IParam;
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
	default Color getLineBorderColor()		{ return GameUI.borderBrightColor(); }
	default Color getShadowColor()			{ return GameUI.borderDarkColor(); }
	default Paint getBackGroundPaint()		{ return buttonBackgroundColor(); }

	static RButton newMiniButton(String text)	{
		RButton button = new RButton(text, MINI_FONT_SIZE);
		button.enableLinedBorder(false);
		return button;
	}
	static RButton newButton(String text)	{
		RButton button = new RButton(text, DEFAULT_FONT_SIZE);
		return button;
	}
	static RButton newBigButton(String text, boolean painted)	{
		RButton button = new RButton(text, BIG_FONT_SIZE);
		button.enablePaintedBackground(painted);
		button.enableShadowedString(true);
		return button;
	}
	static RButton newHugeButton(String text)	{
		RButton button = new RButton(text, HUGE_FONT_SIZE);
		button.enablePaintedBackground(true);
		button.enableShadowedString(true);
		return button;
	}

	public class RButton extends JButton implements RotPButtons	{
		private static final long serialVersionUID = 1L;
		private boolean highlightBorder	= false;
		private boolean linedBorder	= true;
		private boolean paintedBG	= false;
		private boolean shadowText	= false;
		private int fontSize = DEFAULT_FONT_SIZE;
		private IParam<?> param;
		private BasePanel parent;
		private Function<MouseEvent, String> actionListener;

		public RButton(String text, int fontSize, IParam<?> param, BasePanel parent)	{
			this(text, fontSize);
			this.param	= param;
			this.parent	= parent;
		}
		public RButton(String text, int fontSize)	{
			super(text);
			this.fontSize = fontSize;
			setForeground(buttonTextColor());
			setBackground(buttonBackgroundColor());
			setVerticalTextPosition(AbstractButton.BOTTOM);
			setHorizontalTextPosition(AbstractButton.CENTER);
			setFont(buttonFont());
			setFont(buttonFont(fontSize));
			setMargin(rotpMargin());
			setOpaque(false);
			setFocusPainted(false);
			setBorderPainted(false);
			setContentAreaFilled(false);
			addMouseListener(new ButtonMouseAdapter());
		}
		public int textBaselineOffset()		{ return scaled(fontSize)/10; }
		public int heightPct()				{ return 100; }
		public void setLabelKey()			{ setLabelKey(getText()); }
		public void setLabelKey(String key) {
				setText(text(key));
				setToolTipText(htmlText(key + LABEL_DESCRIPTION));
		}
		public Insets rotpMargin()			{
			int side = scaled(fontSize);
			int top = s2 + side/10;
			int bottom = s2 + side/20;
			return new Insets(top, side, bottom, side);
		}
		public void enableLinedBorder(boolean flag)		{ linedBorder = flag; }
		public void enablePaintedBackground(boolean b)	{ paintedBG = b; }
		public void enableShadowedString(boolean flag)	{ shadowText = flag; }
		public void setParam (IParam<?> param)			{ this.param = param; }
		public void setParent (BasePanel parent)		{ this.parent = parent; }
		public void setListener (Function<MouseEvent, String> listener)	{ actionListener = listener; }
		public Dimension rotpSize()	{
			FontMetrics metrics = new Canvas().getFontMetrics(getFont());
			Insets margin = getMargin();
			int txtH = metrics.getHeight();
			int txtW = metrics.stringWidth(getText());
			int modH = (txtH * 100)/100;
			int h = modH + margin.top + margin.bottom;
			int w = txtW + margin.left + margin.right;
			Dimension size = new Dimension(w, h);
			if(isMinimumSizeSet()) {
				Dimension lim = getMinimumSize();
				size.width	= max(size.width, lim.width);				
				size.height	= max(size.height, lim.height);				
			}
			if(isMaximumSizeSet()) {
				Dimension lim = getMaximumSize();
				size.width	= min(size.width, lim.width);				
				size.height	= min(size.height, lim.height);				
			}
//			System.out.println(fontSize + " --> " + new Dimension(unscaled(size.width), unscaled(size.height)));
			setSize(size);
			return size;
		}
		@Override public IParam<?> getParam()			{ return param; }
		@Override public JComponent getComponent()		{ return this; }
		@Override public boolean isPaintedBackground()	{ return paintedBG; }
		@Override public Paint getBackGroundPaint()		{ return GameUI.buttonBackground(0, getWidth()); }
		@Override public boolean isShadowedString()		{ return shadowText; }
		@Override public Dimension getPreferredSize()	{
			if (isPreferredSizeSet())
				return super.getPreferredSize();
			return rotpSize();
		}
		@Override protected void paintComponent(Graphics g)	{
			Graphics2D g2 = (Graphics2D) g;
			setRenderingHints(g);
			int w = getWidth();
			int h = getHeight();
			
			// Paint Background
			if (isPaintedBackground())
				g2.setPaint(getBackGroundPaint());
			else
				g2.setColor(getBackground());
			g2.fillRoundRect(0, 0, w-1, h-1, cnr, cnr);

			// Paint Borders
			if (linedBorder || highlightBorder) {
				g2.setStroke(BasePanel.stroke1);
				if (highlightBorder)
					g2.setColor(highlightColor());
				else
					g2.setColor(getLineBorderColor());
				g2.drawRoundRect(0, 0, w-1, h-1, cnr, cnr);
			}

			// Paint Text
			g2.setFont(getFont());
			Insets margin = getMargin();
			String text = getText();
			int sw	= g2.getFontMetrics().stringWidth(text);
			int x	= max((w-sw)/2, margin.left);
			int sh	= h - margin.top - margin.bottom;
			int y	= (sh*75)/100 + margin.top;
			Color c	= highlightBorder ? Color.yellow : buttonTextColor();
			g2.setColor(c);
			if (isShadowedString())
				drawShadowedString(g2, text, 2, x, y, getShadowColor(), c);
			else
				drawString(g2, text, x, y);
		}
		private class ButtonMouseAdapter extends MouseAdapter	{
			@Override public void mouseEntered(MouseEvent evt)	{
				highlightBorder = true;
				setForeground(highlightColor());
				setDescription();
				popGuide();
//				if (param != null ) {
//					setDescription(param.getDescription());
//					popGuide(param.getGuide());
//				}
//				else {
//					setDescription(getToolTipText());
//					popGuide(getToolTipText());
//				}
			}
			@Override public void mouseExited(MouseEvent evt)	{
				highlightBorder = false;
				setForeground(GameUI.borderBrightColor());
				hideGuide();
				clearDescription();
			}
			@Override public void mousePressed(MouseEvent evt)	{}
			@Override public void mouseReleased(MouseEvent evt)	{
				if (param != null)
					param.toggle(evt, parent);
				if (actionListener != null)
					actionListener.apply(evt);
			}
		}
	}


	public class RToggleButton extends JToggleButton implements RotPButtons	{
		private static final long serialVersionUID = 1L;
		private boolean showBorder;
		private IParam<?> param;
		private BasePanel parent;
		
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
				setDescription();
				popGuide();
			}
			@Override public void mouseExited(MouseEvent evt)	{
				showBorder = false;
				setForeground(buttonTextColor());
				repaint(); // to remove the border
				hideGuide();
				clearDescription();
			}
			@Override public void mousePressed(MouseEvent evt)	{}
			@Override public void mouseReleased(MouseEvent evt)	{
				if (param != null)
					param.toggle(evt, parent);
			}
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
