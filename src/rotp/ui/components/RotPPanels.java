package rotp.ui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import rotp.ui.game.GameUI;

public interface RotPPanels {

	public class RContentPanel extends JPanel implements RotPComponents {
		private static final long serialVersionUID = 1L;
		private static final String NAME = "ContentPanel";
		protected Color titleColorBack	= Color.BLACK;
		protected Color titleColorFore	= Color.WHITE;
		protected String title			= "No Title";
		protected int titleFontSizeMax	= 30;
		protected int titleFontSizeMin	= 15;
		protected int titleTopMargin	= s5;
		protected int titleBottomMargin	= s15;
		private int currentTitleFontSize;

		private BufferedImage backImg;
		private int width, height;
		
		protected RContentPanel()	{
			
		}
		protected Color titleColorBack()	{ return titleColorBack; }
		protected Color titleColorFore()	{ return titleColorFore; }
		protected String title()			{ return title; }
		protected int titleFontSizeMax()	{ return titleFontSizeMax; }
		protected int titleFontSizeMin()	{ return titleFontSizeMin; }
		protected int titleTopMargin()		{ return titleTopMargin; }
		protected int titleBottomMargin()	{ return titleBottomMargin; }
		protected int topContentPosition()	{ return titleTopMargin() + scaled(currentTitleFontSize()) + titleBottomMargin(); }
		private int currentTitleFontSize()	{ return currentTitleFontSize == 0? titleFontSizeMax : currentTitleFontSize; };
			

		protected void initBackImg()		{ initBackImg(getWidth(), getHeight()); }
		protected BufferedImage backImg()	{
			int w = getWidth();
			int h = getHeight();
			if (backImg == null || w != width || h != height)
				initBackImg(w, h);
			return backImg;
		}
		protected void initBackImg(int w, int h)	{
			width = w;
			height = h;
			backImg = newOpaqueImage(w, h);
			Graphics2D g = (Graphics2D) backImg.getGraphics();
			setHiRenderingHints(g);
			g.setPaint(GameUI.modBackground(0, w));
			g.fillRect(0, 0, w, h);

			// Title
			String title = title();
			currentTitleFontSize = scaledFont(g, title(), w-s20, titleFontSizeMax(), titleFontSizeMin());
			int sw = g.getFontMetrics().stringWidth(title);
			int xTitle = (w-sw)/2;
			int yTitle = titleTopMargin() + scaled(currentTitleFontSize);
			drawBorderedString(g, title, 1, xTitle, yTitle, titleColorBack(), titleColorFore());
			g.dispose();
		}
		@Override public void paintComponent(Graphics g) {
			int w = getWidth();
			int h = getHeight();
			setHiRenderingHints(g);
			g.drawImage(backImg(), 0, 0, w, h, this);
		}
	}

}
