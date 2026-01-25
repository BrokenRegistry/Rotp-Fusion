package rotp.ui.components;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JPanel;

import rotp.ui.BasePanel;
import rotp.ui.game.GameUI;

public interface RotPPanels {

	public class RPanel extends JPanel implements RotPComponents {
		private static final long serialVersionUID = 1L;
		@Override public JComponent getComponent()				{ return null; }
		@Override public String getToolTipText(MouseEvent e)	{ return null; }
	}

	public class RContentPanel extends BasePanel implements RotPComponents {
		private static final long serialVersionUID = 1L;
		// private static final String NAME = "ContentPanel";
		protected Color titleColorBack	= Color.BLACK;
		protected Color titleColorFore	= Color.WHITE;
		protected String title			= "No Title";
		protected int titleFontSizeMax	= 30;
		protected int titleFontSizeMin	= 15;
		protected int titleTopMargin	= s5;
		protected int titleBottomMargin	= s15;
		protected int bgAlpha = 200;
		private int currentTitleFontSize;

		private BufferedImage backImg;
		private int width, height;

		protected RContentPanel(int backGroundAlpha)	{ bgAlpha = backGroundAlpha; }

		protected Color titleColorBack()	{ return titleColorBack; }
		protected Color titleColorFore()	{ return titleColorFore; }
		protected String title()			{ return title; }
		protected int titleFontSizeMax()	{ return titleFontSizeMax; }
		protected int titleFontSizeMin()	{ return titleFontSizeMin; }
		protected int titleTopMargin()		{ return titleTopMargin; }
		protected int titleBottomMargin()	{ return titleBottomMargin; }
		protected int topContentPosition()	{ return titleTopMargin() + scaled(currentTitleFontSize()) + titleBottomMargin(); }
		private int currentTitleFontSize()	{ return currentTitleFontSize == 0? titleFontSizeMax : currentTitleFontSize; };

		protected BufferedImage backImg()	{
			int w = getWidth();
			int h = getHeight();
			if (backImg == null || w != width || h != height)
				initBackImg(w, h, bgAlpha);
			return backImg;
		}
		public void initBackImg()	{
			backImg = null;
			initBackImg(getWidth(), getHeight(), bgAlpha);
		}
		public void initBackImg(int backGroundAlpha)	{
			backImg = null;
			bgAlpha = backGroundAlpha;
			initBackImg(getWidth(), getHeight(), bgAlpha);
		}
		protected void initBackImg(int w, int h, int alpha)	{
			width = w;
			height = h;
			backImg = new BufferedImage(w, h, TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) backImg.getGraphics();
			setHiRenderingHints(g);
			g.setPaint(GameUI.modBackground(0, w, alpha));
			g.fillRect(0, 0, w, h);

			// Title
			String title = title();
			if (title != null && !title.isEmpty()) {
				currentTitleFontSize = scaledFont(g, title(), w-s20, titleFontSizeMax(), titleFontSizeMin());
				int sw = g.getFontMetrics().stringWidth(title);
				int xTitle = (w-sw)/2;
				int yTitle = titleTopMargin() + scaled(currentTitleFontSize);
				drawBorderedString(g, title, 1, xTitle, yTitle, titleColorBack(), titleColorFore());
			}
			g.dispose();
		}
		@Override public JComponent getComponent()	{ return this; }
		@Override public void paintComponent(Graphics g) {
			super.paintComponent(g);
			int w = getWidth();
			int h = getHeight();
			setHiRenderingHints(g);
			g.drawImage(backImg(), 0, 0, w, h, this);
		}
	}
}
