package rotp.ui.components;

import static rotp.ui.game.BaseModPanel.guideFontSize;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.Popup;
import javax.swing.PopupFactory;

import rotp.Rotp;
import rotp.ui.ScaledInteger;
import rotp.ui.game.GameUI;
import rotp.ui.util.IParam;
import rotp.util.Base;

public interface RotPGuideUI {

	GuideUI guideUI = new GuideUI();

	static void pop(JComponent frame, JComponent target, String text)	{ guideUI.show(frame, target, text); }
	static void pop(JComponent frame, JComponent target, IParam<?> param)	{ guideUI.show(frame, target, param); }
	static void pop(JComponent frame, JComponent target, Image image)	{ guideUI.show(frame, target, image); }
	static void hide()	{ guideUI.hide(); }

	class GuideUI implements Base, ScaledInteger {
		private static final String HIDE = "HIDE";
		private static final String SHOW = "SHOW";

		private GuideUI() {}

		private Popup popup;
//		private JLabel popupLabel = new JLabel("Popup_Label");
		private PopupFactory factory	= PopupFactory.getSharedInstance();

		private void hide()	{ popup.hide(); }
		private void show(JComponent frame)	{
			System.out.println("x = " + x + "  y = " + y); // TODO BR: REMOVE
			popup = factory.getPopup(frame, pane(), x, y);
			popup.show();
		}
		private void show(JComponent frame, JComponent target, IParam<?> param)	{
			pane(frame, target, param, false);
			show(frame);
		}
		private void show(JComponent frame, JComponent target, String text) {
//			this.frame = frame;
//			this.target = target;
			pane(frame, target, text, false);
			show(frame);
		}
		private void show(JComponent frame, JComponent target, Image image)	{
//			this.frame = frame;
//			this.target = target;
			pane().insertIcon(new ImageIcon(image));
			show(frame);
		}
//		private void actionPerformed(ActionEvent actionEvent) {
//			String actionCommand = actionEvent.getActionCommand();
//			switch (actionCommand) {
//				case HIDE:
//					popup.hide();
//					break;
//				case SHOW:
//					popup.show();
//					break;
//			}
//		}

//		private JComponent frame, target;
		private static final int FONT_SIZE	= 16;
//		private final int maxWidth      = scaled(400);
//		private final Color helpColor	= new Color(240,240,240);
//		private final JTextPane border	= new JTextPane();
//		private final JTextPane	margin	= new JTextPane();
		private JTextPane pane	= new JTextPane();
//		private Rectangle dest			= new Rectangle(0,0,0,0);
//		private String text;
		private int x, y, w, h;
		private int[] lineArr;
//		private boolean fullHelp;
		private Color bgC		= GameUI.setupFrame();;
//		private Color bdrC		= new Color(bgC.getRed(), bgC.getGreen(), bgC.getBlue(), 160);
//		private Color lineColor	= bgC;
//		private Font font;
//		private boolean initialised = false;

		// ========================================================================
		// #=== Pane Builder
		//
		private JTextPane pane(JComponent frame, JComponent target, String tipText, boolean fullHelp)	{
			pane().removeAll();
			guideFontSize(FONT_SIZE);
			// Check for normal tool tip
			if (tipText == null) {
				pane.setText("");
				pane.setSize(new Dimension());
				return pane;
			}
//			// Check for parameter
//			JComponent src = target.getComponent();
//			if (target != null && target instanceof IParam) {
//				Rectangle rect = src.getBounds();
//				String txt = setParam((IParam) src, rect);
//				if (txt != null && !txt.isEmpty())
//					return pane;
//			}
			// TODO
			//return cleanText(tipText);
			pane().setText(cleanText(tipText));
			setDest(target.getBounds());
			return pane;
		}
		private JTextPane pane(JComponent frame, JComponent target, IParam<?> param, boolean fullHelp)	{
			pane().removeAll();
			guideFontSize(FONT_SIZE);
			// Check for normal tool tip
			if (param == null) {
				pane.setText("");
				pane.setSize(new Dimension());
				return pane;
			}
			// Check for parameter
			Rectangle rect = target.getBounds();
			String txt = setParam(param, rect);
			if (txt != null && !txt.isEmpty())
				return pane;
			// TODO
			//return cleanText(tipText);
			pane.setText("");
			pane.setSize(new Dimension());
			return pane;
		}

		private void setLineArr(int... arr)		{ lineArr = arr; }	private JTextPane pane() {
			if (pane == null) {
				pane = new JTextPane();
				pane.setOpaque(true);
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

		private String cleanText(String text)	{
			text = text.replace("<=", "&lt;=");
			text = text.replace(">=", "&gt;=");
			text = text.replace("<>", "&lt;&gt;");
			return text;
		}
//		private void setFullHelp(boolean full)	{ fullHelp = full; }
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
//			return autoSize;
		}
		private String setParam(IParam<?> param, Rectangle dest)	{
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
			System.out.println("Dest = " + dest.toString()); // TODO BR: REMOVE
//			this.dest = dest;
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
			if (dest.width>0) // no line for Hotkeys help
				setLineArr(xb, yb, xd, yd);
		}
	// -#-
		// ========================================================================
		// #=== Display Management
		//
//		public JFrame frame()			{ return (JFrame) SwingUtilities.getRoot(RotPUI.instance()); }
//		public void disableGlassPane()	{ frame().getGlassPane().setVisible(false); }
//		public void enableGlassPane(BasePanel p) {
//			p.setVisible(false);
//			frame().setGlassPane(p);
//			p.setVisible(true);
//		}
//		public void open(BasePanel p) {
//			parent = p;
//			enableGlassPane(this);
//		}
//		public void close() {
////			specs.clear();
//			disableGlassPane();
//		}

		// -#-
	}

	
}
