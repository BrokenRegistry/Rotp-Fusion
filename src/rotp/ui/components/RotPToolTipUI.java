package rotp.ui.components;

import static rotp.ui.game.BaseModPanel.guideFontSize;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.JToolTip;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalToolTipUI;

import rotp.Rotp;
import rotp.ui.ScaledInteger;
import rotp.ui.util.IParam;
import rotp.util.Base;

public class RotPToolTipUI extends MetalToolTipUI implements Base, ScaledInteger {

	public static ComponentUI createUI(JComponent c)	{ return new RotPToolTipUI(); }
	RotPToolTipUI() { super(); }

	@Override public void paint(Graphics g, JComponent c)		{ paintTooltip(g, c); }
	@Override public Dimension getMinimumSize(JComponent c)		{ return getSize(c); }
	@Override public Dimension getPreferredSize(JComponent c)	{ return getSize(c); }
	@Override public Dimension getMaximumSize(JComponent c)		{ return getSize(c); }

	private static final int FONT_SIZE	= 16;

	private JTextPane pane;
	private JTextPane pane() {
		if (pane == null) {
			pane = new JTextPane();
			pane.setOpaque(false);
			pane.setContentType("text/html");
			pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		}
		return pane;
	}
	private void paintHtmlString(Graphics g, int left, int top)	{
		g.translate(left, top);
		pane().paint(g);
		g.translate(-left, -top);
	}
//	private Color bgC	= GameUI.setupFrame();
	private Rectangle dest	= new Rectangle();
//	private String text;
	private int x, y, w, h;
//	private boolean fullHelp;
//	private boolean initialised = false;
//	private Font font; 

	private void paintTooltip(Graphics g, JComponent c)	{
//		c.setOpaque(false);
		//c.setBackground(Color.LIGHT_GRAY);
		Insets insets = c.getInsets();
		c.setLocation(x, y);
		paintHtmlString(g, insets.left, insets.top);
	}
	private Dimension getSize(JComponent c)	{
		pane = initPane(c, false);
		if (pane == null)
			return new Dimension();
		return pane.getSize();
	}
	private void autoSize(JTextPane box, int maxWidth)	{
		int iW = scaled(Rotp.IMG_W - 20);
		int iH = scaled(Rotp.IMG_H - 20);
		int testW, preTest;
		w = Short.MAX_VALUE;
		boolean go = true;

		while (go) {
			box.setFont(plainFont(guideFontSize()));
			h = Short.MAX_VALUE;
			preTest = -1;
			testW = maxWidth - 1; // to prevent rounding errors
			while (h > iH && preTest != testW && testW < iW) {
				preTest = testW;
				box.setSize(new Dimension(testW, Short.MAX_VALUE));
				Dimension paneSize = box.getPreferredSize();
				w = min(testW, paneSize.width);
				h = paneSize.height;
				testW *= (float) h /iH;
			}
			go = (w > iW || h > iH);
			if (go) {
				guideFontSize(max(1, min(guideFontSize()-1, (int)(guideFontSize() * (float)iH/h -1))));
				go = guideFontSize() > 1;
			}
		}
		w += 1;
		Dimension autoSize = new Dimension(w, h);
		box.setSize(autoSize);
//		return autoSize;
	}
	private String cleanText(String text)	{
		text = text.replace("<=", "&lt;=");
		text = text.replace(">=", "&gt;=");
		text = text.replace("<>", "&lt;&gt;");
		return text;
	}
//	private void setFullHelp(boolean full)	{ fullHelp = full; }

	private JTextPane initPane(JComponent c, boolean fullHelp)	{
		guideFontSize(FONT_SIZE);
		// Check for normal tool tip
		JToolTip tip = (JToolTip)c;
		String tipText = tip.getTipText();
		if (tipText == null) {
			pane().setText("");
			pane.setSize(new Dimension());
			return pane;
		}
		// Check for parameter
		JComponent src = tip.getComponent();
		if (src != null && src instanceof IParam) {
			Rectangle rect = src.getBounds();
			String txt = setParam((IParam) src, rect);
			if (txt != null && !txt.isEmpty())
				return pane;
		}
		// TODO
		//return cleanText(tipText);
		pane().setText(cleanText(tipText));
		autoSize(pane(), s400);
		return pane;
	}
	private String setParam(IParam param, Rectangle dest)	{
		if (param == null)
			return null;
		guideFontSize(FONT_SIZE);
		String txt = param.getGuide();
		if (txt == null || txt.isEmpty())
			return txt;

		txt = cleanText(txt);
		setDest(dest); // For position and arrow
		if (guideFontSize() < FONT_SIZE) {
			// Second call to build the guide to adjust html size to current font size
			txt = param.getGuide();
			if (txt == null || txt.isEmpty())
				return txt; // Should never happen
			txt = cleanText(txt);
			setDest(dest); // For position and arrow
		}
		return txt;
	}
	private void setDest(Rectangle dest) {
		this.dest = dest;
		int xShift	= s20;
		int yShift	= s20;
		int xCover	= s10;
		int yCover	= s10;
		int xMargin	= s10;
		int yMargin	= s10;
		int xb, xd, yb, yd;
		int iW = scaled(Rotp.IMG_W);
		int iH = scaled(Rotp.IMG_H);
		xCover = min(xCover, dest.width/2);
		yCover = min(yCover, dest.height/2);
		
		autoSize(pane(), s400);
		// relative position
		// find X location
		if (2*dest.x + dest.width  > iW) { // put box to the left
			x = dest.x - w - xShift;
			if (x < xMargin)
				x = xMargin;
			xb = x + w;
			xd = dest.x + xCover;
			if (xd < xb)
				xd = xb + s10;
		}
		else { // put box to the right
			x = dest.x + dest.width + xShift;
			if (x+w > iW-xMargin)
				x = iW-xMargin - w;
			xb = x;
			xd = dest.x + dest.width - xCover;
			if (xd > xb)
				xd = xb - s10;
		}
		// find Y location
		if (2*dest.y + dest.width  > iH) { // put box to the top
			y = dest.y - h - yShift;
			if (y < yMargin)
				y = yMargin;
			yb = y + h;
			yd = dest.y + yCover;
			if (yd < yb)
				yb = yd + s10;
		}
		else { // put box to the bottom
			y = dest.y + dest.height + yShift;
			if (y+h > iH-yMargin)
				y = iH-yMargin - h;
			yb = y;
			yd = dest.y + dest.height - yCover;
			if (yd > yb)
				yb = yd - s10;
		}
//		if (dest.width>0) // no line for Hotkeys help
//			setLineArr(xb, yb, xd, yd);
	}
//	private void setDest(Rectangle newDest)	{
//		dest = newDest;
//		init(dest);
//	}
//	public  void setDest(Rectangle dest, String text, Graphics g0)	{
//		guideFontSize(FONT_SIZE);
////		lineArr = null;
//		setFullHelp(dest.width == 0);
//		setText(text);
//		setDest(dest);
//	}
}