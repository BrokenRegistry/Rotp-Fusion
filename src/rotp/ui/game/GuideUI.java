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

import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.NONE;
import static rotp.model.game.IMainOptions.showGuide;
import static rotp.ui.game.BaseModPanel.guideFontSize;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JTextPane;

import rotp.Rotp;
import rotp.ui.BasePanel;
import rotp.ui.components.RotPButtons;
import rotp.ui.components.RotPButtons.RButton;
import rotp.ui.components.RotPPanels.RPanel;
import rotp.ui.util.IParam;
import rotp.util.Base;
import rotp.util.sound.SoundManager;

public class GuideUI extends BasePanel {
	public interface IGuide {
		DescriptionPane descriptionPane = new DescriptionPane(); // not cap, because not really a constant!

		default void setGuideColors(Color backC, Color textC)	{ GuideData.setCustomColors(backC, textC); }
		default void leaveGuide()	{
			restoreGuideColors();
			hideGuide();
			GuideUI.parent = null;
		}
		default void enterGuide(BasePanel parent)	{ GuideUI.parent = parent; }

		default RButton newGuideButton(boolean big)	{
			RButton button = big? RotPButtons.newBigButton("SETTINGS_GUIDE", false): RotPButtons.newButton("SETTINGS_GUIDE");
			button.setLabelKey();
			button.addActionListener(e -> buttonGuideAction(e));
			return button;
		}
		static void buttonGuideAction(ActionEvent e)	{
			SoundManager.current().playAudioClip("ButtonClick");
			if (showGuide.get()) {
				GuideUI.close();
				showGuide.toggle();
			}
			else {
				showGuide.toggle();
				if (e!=null) {
					RButton button = (RButton) e.getSource();
					button.popGuide(button.getToolTipText());
				}
			}
		}
		default void setDescription(String txt)		{ descriptionPane.setText(txt); }
		default void setDescription(JComponent c)	{
			if (c != null)
				descriptionPane.setText(c.getToolTipText());
		}
		default void setDescription()	{
			if (showDescription()) {
				JComponent c = getComponent();
				if (c != null) {
					IParam<?> p = getParam();
					if (p != null)
						descriptionPane.setText(p.getDescription());
					else
						descriptionPane.setText(c.getToolTipText());
				}
			}
		}
		default void clearDescription()		{ descriptionPane.setText(""); }

		JComponent getComponent();
		default IParam<?> getParam()		{ return null; }
		default boolean showGuide()			{ return showGuide.get(); }
		default boolean showDescription()	{ return descriptionPane.isActive(); }
		default void popGuide(String tip)	{
			if (showGuide()) {
				JComponent c = getComponent();
				if (c != null)
					GuideUI.open(c, tip);
			}
		}
		default void popGuide(String tip, int dx, int dy)	{
			if (showGuide()) {
				JComponent c = getComponent();
				if (c != null)
					GuideUI.open(c, tip, dx, dy);
			}
		}

		default void popGuide(JComponent c)	{ GuideUI.open(c); }
		default void popGuide()	{
			if (showGuide()) {
				JComponent c = getComponent();
				if (c != null) {
					IParam<?> p = getParam();
					if (p != null)
						GuideUI.open(c, p.getGuide());
					else
						GuideUI.open(c);
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
		final class DescriptionPane extends RPanel	{
			private static final long serialVersionUID = 1L;
			public final JTextPane descriptionBox = new JTextPane();
			public final JLabel minHeightLabel	= new JLabel();
			public final JLabel minWidthLabel	= new JLabel();
			private boolean active = false;
			private DescriptionPane()	{
				setOpaque(false);
				setLayout(new GridBagLayout());

				add(minHeightLabel, newGbc(0,0, 1,1, 0,0, CENTER, NONE, ZERO_INSETS, 0,0));
				add(minWidthLabel, newGbc(0,0, 1,1, 0,0, CENTER, NONE, ZERO_INSETS, 0,0));
				setMinHeight(s41); // two lines
				setMinWidth(s20);

//				descriptionBox.setBackground(new Color(78,101,155));
				descriptionBox.setForeground(Color.BLACK);
				descriptionBox.setOpaque(false);
				descriptionBox.setContentType("text/html");
				descriptionBox.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			}
			public void init(Color bgC, Color fgC, Font font, String tip, int minW, int minH)	{
				setBackground(bgC);
				setForeground(fgC);
				descriptionBox.setText(tip);
				setTipFont(font);
				setMinWidth(minW);
				setMinHeight(minH);
			}
			public void setTipFont(Font font)	{ descriptionBox.setFont(font); }
			public void setMinHeight(int h)		{ minHeightLabel.setPreferredSize(new Dimension(1, h)); }
			public void setMinWidth(int w)		{ minWidthLabel.setPreferredSize(new Dimension(w, 1)); }
			public boolean isActive()			{ return active; }
			public void setActive(boolean flag)	{ active = flag; }
			public void setText(String text)	{
				if (active) {
					descriptionBox.setText(text);
					repaint();
				}
			}

			@Override protected void paintComponent(Graphics g)	{
				super.paintComponent(g);
				int w = getWidth();
				int h = getHeight();
				g.setColor(getBackground());
				g.fillRect(0, 0, w-1, h-1);

				Dimension dim =  descriptionBox.getPreferredSize();
				descriptionBox.setSize(dim);
				g.translate(s10, 0);
				descriptionBox.paint(g);
				g.translate(-s10, 0);
			}
		}
	}
	private static final long serialVersionUID = 1L;
	public static GuideUI instance;
	private static BasePanel parent;
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
	public static void open(JComponent target, String tipText, int dx, int dy)	{
		if (showGuide.get() && target != null && tipText!= null && !tipText.isEmpty()) {
			instance.guideData.init(target, tipText, dx, dy);
			instance.enableGlassPane(instance);
		}
	}
	public static void open(JComponent target, String tipText)	{
		if (showGuide.get() && target != null && tipText!= null && !tipText.isEmpty()) {
			instance.guideData.init(target, tipText);
			instance.enableGlassPane(instance);
		}
	}
	public static void open(JComponent target)	{
		if (showGuide.get() && target != null) {
			open(target, target.getToolTipText());
		}
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
	@Override public void keyReleased(KeyEvent e)	{
		if (parent == null)
			switch(e.getKeyCode()) {
				case KeyEvent.VK_ESCAPE:
					setModifierKeysState(e);
					close();
					return;
				case KeyEvent.VK_G:
					IGuide.buttonGuideAction(null);
					return;
				default:
					super.keyReleased(e);
			}
		else
			parent.keyReleased(e);
	}

	// -#-
	// ========================================================================
	// #=== Guide
	//
	static void restoreGuideColors()						{ GuideData.setDefaultColors(); }
	static void setGuideColors(Color backC, Color textC)	{ GuideData.setCustomColors(backC, textC); }
	class GuideData	{
		private static final int GUIDE_FONT_SIZE = 14;
		private static final Color borderLineColor = new Color(128, 128, 128, 128);
		private static Color textColor;
		private static Color backColor;
		private static Color lineColor;
		private static Color borderColor;
		private static final JTextPane guideBox = new JTextPane();
		private Rectangle sourceBox;
		private int[] lineArr;
		private int left, top, width, height;
		private int dx, dy;

		GuideData()	{
			guideBox.setOpaque(true);
			guideBox.setContentType("text/html");
			guideBox.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			setDefaultColors();
			guideBox.setBackground(backColor);
			guideBox.setForeground(textColor);
		}
		private static void setCustomColors(Color backC, Color textC)	{
			textColor	= textC;
			backColor	= backC;
			lineColor	= backC;
			borderColor	= Base.setAlpha(backC, 160);
			setGuideColors();
		}
		private static void setDefaultColors()	{
			textColor	= Color.BLACK;
			backColor	= GameUI.loadHoverBackground();
			lineColor	= backColor;
			borderColor	= Base.setAlpha(backColor, 160);
			setGuideColors();
		}
		private static void setGuideColors()	{
			guideBox.setBackground(backColor);
			guideBox.setForeground(textColor);
		}
		private void clear()	{
			sourceBox = null;
			lineArr	  = null;
			guideBox.setText("");
		}
		// Queries
		private void paintGuide(Graphics2D g)	{
			g.setColor(borderColor);
			g.fillRoundRect(left-s6, top-s6, width+s6+s6, height+s6+s6, cnr+s3, cnr+s3);
			g.setColor(backColor);
			g.fillRoundRect(left-s3, top-s3, width+s3+s3, height+s3+s3, cnr, cnr);
			g.translate(left, top);
			guideBox.paint(g);
			g.translate(-left, -top);
			drawLines(g);
			g.setColor(borderLineColor);
			Stroke prev = g.getStroke();
			g.setStroke(stroke1);
			g.drawRoundRect(left-s3, top-s3, width+s3+s3, height+s3+s3, cnr, cnr);
			g.setStroke(prev);
		}
		private void drawLines(Graphics2D g)	{
			if (lineArr != null) {
				Stroke prev = g.getStroke();
				g.setStroke(stroke2);
				g.setColor(lineColor);
				int size = lineArr.length/2 - 1;
				for (int i=0; i<size; i++) {
					int k = 2*i;
					g.drawLine(lineArr[k], lineArr[k+1], lineArr[k+2]+dx, lineArr[k+3]+dy);
				}
				g.setStroke(prev);
			}			
		}
		// Initializations
		private void setLineArr(int... arr)	{ lineArr = arr; }
		private void init(JComponent target, String tipText, int dx, int dy)	{ createGuideBox(getLocationOnScreen(target), tipText, true, dx, dy); }
		private void init(JComponent target, String tipText)	{ createGuideBox(getLocationOnScreen(target), tipText, true, 0, 0); }
//		private void init(Rectangle target, String tipText)		{ createGuideBox(target, tipText, false, 0, 0); }
		// Creations
//		private void createGuideBox(Rectangle target, String tipText, int dx, int dy)	{ createGuideBox(target, tipText, false, dx, dy); }
//		private void createGuideBox(Rectangle target, String tipText, boolean fullHelp)	{
//			createGuideBox(target, tipText, fullHelp, 0, 0);
//		}
		private void createGuideBox(Rectangle target, String tipText, boolean fullHelp, int dx, int dy)	{
			guideFontSize(GUIDE_FONT_SIZE);
			sourceBox = target;
			this.dx = dx;
			this.dy = dy;

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
		private void createGuideBox(JComponent target, IParam<?> param, boolean fullHelp)	{ // TODO BR: add validBox
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
		private String setSizeAndLocation(IParam<?> param)	{
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
			int cover		= s5;
			int xBoxShift	= s20;
			int yBoxShift	= s20;
			int xTarCover	= s2;
			int yTarCover	= sourceBox.height>s25? s10 : s5;
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
				left += dx;
				xb = left + width;
				xd = sourceBox.x + xTarCover;
				if (xd < xb)
					xd = xb + xLineCover;
			}
			else { // put box to the right
				left = sourceBox.x + sourceBox.width + xBoxShift;
				if (left+width > iW-xBoxMargin)
					left = iW-xBoxMargin - width;
				left += dx;
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
				top += dy;
				yb = top + height;
				yd = sourceBox.y + yTarCover;
				if (yd < yb)
					yb = yd + yLineCover;
			}
			else { // put box to the bottom
				top = sourceBox.y + sourceBox.height + yBoxShift;
				if (top+height > iH-yBoxMargin)
					top = iH-yBoxMargin - height;
				top += dy;
				yb = top;
				yd = sourceBox.y + sourceBox.height - yTarCover;
				if (yd > yb)
					yb = yd - yLineCover;
			}
			setLineArr(xb, yb, xd, yd);
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

