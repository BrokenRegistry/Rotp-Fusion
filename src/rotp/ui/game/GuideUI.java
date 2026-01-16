/*
 * Copyright 2015-2020 Ray Fowler
 * 
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *	 https://www.gnu.org/licenses/gpl-3.0.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rotp.ui.game;

import static rotp.model.game.IMainOptions.showGuide;
import static rotp.ui.game.BaseModPanel.guideFontSize;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JTextPane;

import rotp.Rotp;
import rotp.ui.BasePanel;
import rotp.ui.util.IParam;
import rotp.util.Base;

public class GuideUI extends BasePanel {
	public interface IGuide {
		JComponent getComponent();
		default IParam getParam()			{ return null; }
		default boolean showGuide()			{ return showGuide.get(); }
		default void popGuide(String tip)	{
			if (showGuide()) {
				JComponent c = getComponent();
				if (c != null)
					GuideUI.open(c, tip);
			}
		}
		default void popGuide()	{
			if (showGuide()) {
				JComponent c = getComponent();
				if (c != null) {
					IParam p = getParam();
					if (p != null)
						GuideUI.open(c, p.getGuide());
				}
			}
		}
		default void hideGuide()	{
			if (showGuide())
				GuideUI.close();
		}
		default Rectangle getBoundsOnScreen()	{
			JComponent c = getComponent();
			if (c != null)
				return new Rectangle(c.getLocationOnScreen(), c.getSize());
			return new Rectangle();
		}
		default void paintImmediately()	{
			JComponent c = getComponent();
			if (c != null)
				c.paintImmediately(0, 0, c.getWidth(), c.getHeight());
		}
	}
	private static final long serialVersionUID = 1L;
	public static GuideUI instance;
	private BasePanel parent;
	private GuideData guideData = new GuideData();

	// ========================================================================
	// === Panel Global Tools
	//
	public static String cleanHtmlText(String text)	{
		text = text.replace("<=", "&lt;=");
		text = text.replace(">=", "&gt;=");
		text = text.replace("<>", "&lt;&gt;");
		return text;
	}
	// ========================================================================
	// #=== Panel Management
	//
	public GuideUI()	{
		instance = this;
		setOpaque(false);
	}
	public static void clear()	{ instance.guideData.clear(); }
	public static void close()	{
		clear();
		instance.disableGlassPane();
	}
//	public void open(BasePanel parent, Rectangle target, String tipText, Rectangle valid)	{
//		this.parent = parent;
//		guideData.init(target, tipText);
//		enableGlassPane(this);
//	}
//	public void open(BasePanel parent, JComponent target, String tipText, Rectangle valid)	{
//		this.parent = parent;
//		guideData.init(target, tipText);
//		enableGlassPane(this);
//	}
	public static void open(JComponent target, String tipText)	{
		instance.guideData.init(target, tipText);
		instance.enableGlassPane(instance);
	}
	private Rectangle getLocationOnScreen(JComponent c)	{
		return new Rectangle(c.getLocationOnScreen(), c.getSize());
	}
	// -#-
	// ========================================================================
	// #=== Standard Overriders
	//
	@Override public void paintComponent(Graphics g0)	{
		super.paintComponent(g0);
		guideData.paintGuide((Graphics2D) g0);
	}
	// -#-
	// ========================================================================
	// #=== Guide
	//
	class GuideData	{
		private static final int GUIDE_FONT_SIZE = 14;
		private static final Color backColor	= GameUI.setupFrame(); // TODO put elsewhere
		private static final Color lineColor	= backColor;
		private static final Color borderColor	= Base.setAlpha(backColor, 160);
		private static final JTextPane guideBox = new JTextPane();
		private Rectangle sourceBox;
		private int[] lineArr;
		private int left, top, width, height;

		GuideData()	{
			guideBox.setOpaque(true);
			guideBox.setContentType("text/html");
			guideBox.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			guideBox.setBackground(backColor);
		}
		private void clear()	{
			sourceBox = null;
			lineArr	  = null;
			guideBox.setText(null);
		}
		// Queries
		private boolean isEmpty()			{ return sourceBox == null; }
		private boolean contains(Point p)	{ return sourceBox==null? false : sourceBox.contains(p); }
		private void paintGuide(Graphics2D g)	{
			g.setColor(borderColor);
			g.fillRect(left-s8, top-s8, width+s8+s8, height+s8+s8);
			g.setColor(backColor);
			g.fillRect(left-s3, top-s3, width+s3+s3, height+s3+s3);
			g.translate(left, top);
			guideBox.paint(g);
			g.translate(-left, -top);
			drawLines(g);
		}
		private void drawLines(Graphics2D g)	{
			if (lineArr != null) {
				Stroke prev = g.getStroke();
				g.setStroke(stroke2);
				g.setColor(lineColor);
				int size = lineArr.length/2 - 1;
				for (int i=0; i<size; i++) {
					int k = 2*i;
					g.drawLine(lineArr[k], lineArr[k+1], lineArr[k+2], lineArr[k+3]);
				}
				g.setStroke(prev);
			}			
		}
		// Initializations
		private void setLineArr(int... arr)	{ lineArr = arr; }
		private void init(JComponent target, String tipText)	{ createGuideBox(getLocationOnScreen(target), tipText, true); }
		private void init(Rectangle target, String tipText)		{ createGuideBox(target, tipText, false); }
		// Creations
		private void createGuideBox(Rectangle target, String tipText)	{ createGuideBox(target, tipText, false); }
		private void createGuideBox(Rectangle target, String tipText, boolean fullHelp)	{
			guideFontSize(GUIDE_FONT_SIZE);
			sourceBox = target;

			// Check for normal tool tip
			if (tipText == null) {
				guideBox.setText("");
				guideBox.setSize(new Dimension());
				return;
			}
//			// Check for parameter
//			JComponent src = target.getComponent();
//			if (target != null && target instanceof IParam) {
//				Rectangle rect = src.getBounds();
//				String txt = setParam((IParam) src, rect);
//				if (txt != null && !txt.isEmpty())
//					return pane;
//			}
			// TODO BR:
			//return cleanHtmlText(tipText);
			guideBox.setText(cleanHtmlText(tipText));
			setSizeAndLocation();
		}
		private void createGuideBox(JComponent target, IParam param, boolean fullHelp)	{ // TODO BR: add validBox
			// Check for normal tool tip
			if (param == null) {
				guideBox.setText("");
				guideBox.setSize(new Dimension());
				return;
			}
			// Check for parameter
			sourceBox = target.getBounds();
			String txt = setSizeAndLocation(param);
			if (txt != null && !txt.isEmpty())
				return;
			// TODO
			//return cleanText(tipText);
			guideBox.setText("");
			guideBox.setSize(new Dimension());
		}
		// Size and location
		private String setSizeAndLocation(IParam param)	{
			if (param == null)
				return null;
			guideFontSize(GUIDE_FONT_SIZE);
			String txt = param.getGuide();
			if (txt == null || txt.isEmpty())
				return txt;

			txt = cleanHtmlText(txt);
			setSizeAndLocation(); // For position and arrow
			if (guideFontSize() < GUIDE_FONT_SIZE) {
				// Second call to build the guide to adjust html size to current font size
				txt = param.getGuide();
				if (txt == null || txt.isEmpty())
					return txt; // Should never happen
				txt = cleanHtmlText(txt);
				setSizeAndLocation(); // For position and arrow
			}
			return txt;
		}
		private void setSizeAndLocation() {
//			System.out.println("Target = " + targetBox.toString()); // TODO BR: REMOVE
			int cover	= sourceBox.height>s25? s5 : s5;
			int xBoxShift	= s20;
			int yBoxShift	= s20;
			int xTarCover	= cover;
			int yTarCover	= cover;
			int xBoxMargin	= s10;
			int yBoxMargin	= s10;
			int xLineCover	= cover;
			int yLineCover	= cover;
			int xb, xd, yb, yd;
			int iW, iH;
			iW = scaled(Rotp.IMG_W);
			iH = scaled(Rotp.IMG_H);
			xTarCover = min(xTarCover, sourceBox.width/2);
			yTarCover = min(yTarCover, sourceBox.height/2);

			autoSizeBox(s400);
			// relative position
			// find X location
			if (2*sourceBox.x + sourceBox.width  > iW) { // put box to the left
				left = sourceBox.x - width - xBoxShift;
				if (left < xBoxMargin)
					left = xBoxMargin;
				xb = left + width;
				xd = sourceBox.x + xTarCover;
				if (xd < xb)
					xd = xb + xLineCover;
			}
			else { // put box to the right
				left = sourceBox.x + sourceBox.width + xBoxShift;
				if (left+width > iW-xBoxMargin)
					left = iW-xBoxMargin - width;
				xb = left;
				xd = sourceBox.x + sourceBox.width - xTarCover;
				if (xd > xb)
					xd = xb - xLineCover;
			}
			// find Y location
			if (2*sourceBox.y + sourceBox.width  > iH) { // put box to the top
				top = sourceBox.y - height - yBoxShift;
				if (top < yBoxMargin)
					top = yBoxMargin;
				yb = top + height;
				yd = sourceBox.y + yTarCover;
				if (yd < yb)
					yb = yd + yLineCover;
			}
			else { // put box to the bottom
				top = sourceBox.y + sourceBox.height + yBoxShift;
				if (top+height > iH-yBoxMargin)
					top = iH-yBoxMargin - height;
				yb = top;
				yd = sourceBox.y + sourceBox.height - yTarCover;
				if (yd > yb)
					yb = yd - yLineCover;
			}
			if (sourceBox.height>s25)
				setLineArr(xb, yb, xd-s6, yd-s13);
			else
				setLineArr(xb, yb, xd-s6, yd-s19);
		}
		private void autoSizeBox(int maxWidth)	{
			int iW, iH;
			iW = scaled(Rotp.IMG_W);
			iH = scaled(Rotp.IMG_H);
			int testW, preTest;
			width = Short.MAX_VALUE;
			boolean go = true;

			while (go) {
				guideBox.setFont(plainFont(guideFontSize()));
				height = Short.MAX_VALUE;
				preTest = -1;
				testW = maxWidth - 1; // to prevent rounding errors
				while (height > iH && preTest != testW && testW < iW) {
					preTest = testW;
					guideBox.setSize(new Dimension(testW, Short.MAX_VALUE));
					Dimension paneSize = guideBox.getPreferredSize();
					width = min(testW, paneSize.width);
					height = paneSize.height;
					testW *= (float) height /iH;
				}
				go = (width > iW || height > iH);
				if (go) {
					guideFontSize(max(1, min(guideFontSize()-1, (int)(guideFontSize() * (float)iH/height -1))));
					go = guideFontSize() > 1;
				}
			}
			width += 1;
			Dimension autoSize = new Dimension(width, height);
			guideBox.setSize(autoSize);
		}
	}
	// -#-
}

