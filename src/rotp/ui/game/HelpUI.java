/*
 * Copyright 2015-2020 Ray Fowler
 * 
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.gnu.org/licenses/gpl-3.0.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rotp.ui.game;

import static rotp.ui.game.BaseModPanel.guideFontSize;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JTextPane;

import rotp.Rotp;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.util.IParam;
import rotp.ui.util.InterfaceHelp;


public class HelpUI extends BasePanel implements MouseListener, MouseMotionListener {
    private static final long serialVersionUID = 1L;
    private static final Color backgroundHaze = new Color(0,0,0,40);
    private static final int FONT_SIZE		= 16;
    private static final int MIN_FONT_SIZE	= 10;
//    private static final BufferedImage fakeGraphic = new BufferedImage(16, 16, TYPE_INT_ARGB);
    private static int margin = s30;
    private final Color blueBackC  = new Color(78,101,155);
    private final Color brownBackC = new Color(240,240,240);
    private final Color brownTextC = new Color(45,14,5);

    private List<HelpSpec> specs = new ArrayList<>();
	private InterfaceHelp parent; // BR: changed to Interface to allow Dialogs to call it
	private JDialog dialog;
	private boolean asGuide;
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
    public HelpUI() {
        init();
    }
    private void init() {
        setOpaque(false);
        addMouseListener(this);
        addMouseMotionListener(this);
    }
    public void open(BasePanel p) {
        parent = p;
        asGuide = false;
        enableGlassPane(this);
    }
    public void close() {
		clear();
		if (dialog == null)
			disableGlassPane();
		else {
//			dialog.setEnabled(true);
			dialog.getContentPane().setEnabled(true);
			dialog.getGlassPane().setVisible(false);
			dialog = null;
		}
    }
    public void clear() {
        specs.clear();
        clearGuide();
    }
	public void open(InterfaceHelp parent, Rectangle target, String tipText, Rectangle valid)	{
		this.parent = parent;
		asGuide = true;
		guideData.init(target, tipText, valid);
		enableGlassPane(this);
	}
	public void open(InterfaceHelp parent, JComponent target, String tipText, Rectangle valid)	{
		this.parent = parent;
		asGuide = true;
		guideData.init(target, tipText, valid);
		enableGlassPane(this);
	}
	public void open(JDialog frame, InterfaceHelp parent, Rectangle target, String tipText, Rectangle valid)	{
		dialog  = frame;
		this.parent = parent;
		asGuide = true;
		guideData.init(target, tipText, valid);
		this.setVisible(false);
		dialog.setGlassPane(this);
		this.setVisible(true);
	}
	public void open(JDialog frame, InterfaceHelp parent, JComponent target, String tipText, Rectangle valid)	{
		dialog  = frame;
		this.parent = parent;
		asGuide = true;
		guideData.init(target, tipText, valid);
		this.setVisible(false);
		dialog.setGlassPane(this);
		this.setVisible(true);
	}
	// -#-
	// ========================================================================
	// #=== HelpSpec Management
	//
    public HelpSpec addBrownHelpText(int x, int y, int w, int num, String text) {
        HelpSpec sp = addBlueHelpText(x,y,w,num,text);
        sp.backC = brownBackC;
        sp.textC = brownTextC;
        return sp;
    }
    public HelpSpec addBlueHelpText(int x, int y, int w, int num, String text) {
        return addBlueHelpText(x,y,w,num,text,-1,-1,-1,-1,-1,-1);
    }
    public HelpSpec addBlueHelpText(int x, int y, int w, int num, String text, int x1, int y1, int x2, int y2) {
        return addBlueHelpText(x,y,w,num,text,x1,y1,x2,y2,-1,-1);
    }
    public HelpSpec addBlueHelpText(int x, int y, int w, int num, String text, int x1, int y1, int x2, int y2, int x3, int y3) {
        HelpSpec sp = new HelpSpec();
        sp.text = text;
        sp.x = x;
        sp.w = w;
        sp.x1 = x1;
        sp.y1 = y1;
        sp.x2 = x2;
        sp.y2 = y2;
        sp.x3 = x3;
        sp.y3 = y3;
        sp.backC = blueBackC;

        if (num==0) {
        	sp.lines = getLineNumber(text, w);
        	sp.hMax  = sp.height();
        }
        else if (num<0) {
        	sp.lines = -num;
        	sp.hMax  = sp.height();
        	sp.init();
        	//sp.lines = getLineNumber(text, w, sp.hMax);
        }
        else {
        	sp.lines = num;
        	sp.hMax  = sp.height();
        	sp.init();
        }
        sp.hMax = sp.height();

        if (y<0)
        	sp.y = -y - sp.height();
        else
        	sp.y = y;
        	
        specs.add(sp);
        return sp;
    }
	public int getLineNumber(String str, int maxWidth)	{
		List<String> lines = wrappedLines(narrowFont(FONT_SIZE), str, maxWidth - margin);
		return lines.size();
	}
	// -#-
	// ========================================================================
	// #=== Standard Overriders
	//
    @Override public void paintComponent(Graphics g0)	{
        super.paintComponent(g0);
		Graphics2D g = (Graphics2D) g0;
		if (asGuide)	{
			paintGuide(g);
			return;
		}
        int w = getWidth();
        int h = getHeight();
        g.setColor(backgroundHaze);
        g.fillRect(0, 0, w, h);

        for (HelpSpec spec: specs) {
            int maxHeight = spec.hMax();

            // Text formating
            int fontSize = spec.fontSize;
            g.setFont(narrowFont(fontSize));
            List<String> lines = wrappedLines(g, spec.text, spec.w - margin);
            int specH = height(lines.size(), fontSize);
            while ((specH > maxHeight) && (fontSize > MIN_FONT_SIZE)) {
                fontSize--;
                g.setFont(narrowFont(fontSize));
                lines = wrappedLines(g, spec.text, spec.w - margin);
                specH = height(lines.size(), fontSize);
            }
            // draw background box
            Color backC = spec.backC;
            Color bdrC  = new Color(backC.getRed(), backC.getGreen(), backC.getBlue(), 160);
            g.setColor(bdrC);
            g.fillRect(spec.x, spec.y, spec.w, specH);
            g.setColor(backC);
            g.fillRect(spec.x+s5, spec.y+s5, spec.w-s10, specH-s10);

            // draw box text
            g.setColor(spec.textC);
            int lineH = lineH(fontSize);
            int x0 = spec.x + s15;
            int y0 = spec.y + lineH + scaled(fontSize/2 - 1);
            for (String line: lines) {
                drawString(g,line, x0, y0);
                y0 += lineH;
            }
            // draw line to target
            if (spec.x2 >= 0) {
                Stroke prev = g.getStroke();
                g.setStroke(stroke2);
                g.setColor(spec.lineC);
                g.drawLine(spec.x1, spec.y1, spec.x2, spec.y2);
                if (spec.x3 >=0) 
                    g.drawLine(spec.x2, spec.y2, spec.x3, spec.y3);
                g.setStroke(prev);
            }
            // BR: draw lines of target Array
            if (spec.lineArr != null) {
                Stroke prev = g.getStroke();
                g.setStroke(stroke2);
                g.setColor(spec.lineC);
            	int size = spec.lineArr.length/2 - 1;
            	for (int i=0; i<size; i++) {
            		int k = 2*i;
            		g.drawLine(spec.lineArr[k], spec.lineArr[k+1], spec.lineArr[k+2], spec.lineArr[k+3]);
            	}
                g.setStroke(prev);
            }
       }
    }
    @Override public void keyPressed(KeyEvent e)		{
        switch(e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                parent.cancelHelp();
                break;
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_F1:
                parent.advanceHelp();
                break;
            case KeyEvent.VK_E:
				if (e.isAltDown() && e.isControlDown()) {
					debugReloadLabels("en");
					parent.repaint();
				}
				return;
            case KeyEvent.VK_F:
				if (e.isAltDown() && e.isControlDown()) {
					debugReloadLabels("fr");
					parent.repaint();
				}
				return;
			case KeyEvent.VK_L:
				if (e.isAltDown()) {
					debugReloadLabels("");
					parent.repaint();
				}
				break;
        }
    }
    @Override public void mouseClicked(MouseEvent e)	{ }
    @Override public void mousePressed(MouseEvent e)	{ }
    @Override public void mouseReleased(MouseEvent e)	{ parent.advanceHelp(); }
    @Override public void mouseEntered(MouseEvent e)	{ }
    @Override public void mouseExited(MouseEvent e)		{ }
	@Override public void mouseDragged(MouseEvent e)	{ }
	@Override public void mouseMoved(MouseEvent e)		{
		if (asGuide)
			guideMouseMoved(e);
	}

    private static int lineH(int fontSize)				{ return RotPUI.scaledSize(fontSize + 2); }
    private static int height(int lines, int fontSize)	{ return s2 + (lines + 1) * lineH(fontSize) ; }
    static int lineH()									{ return lineH(FONT_SIZE); }
    static int height(int lines)						{ return height(lines, FONT_SIZE); }
	// -#-
	// ========================================================================
	// #=== HelpSpec
	//
    public class HelpSpec {
        private int x, y, w;
        private int lines, hMax;
        private int fontSize = FONT_SIZE;
        private int[] lineArr; // BR: to allow frames
        private int x1 = -1;
        private int y1 = -1;
        private int x2 = -1;
        private int y2 = -1;
        private int x3 = -1;
        private int y3 = -1;
        private Color backC = Color.blue;
        private Color textC = Color.white;
        private Color lineC = Color.white;
        private String text;
        public int lineH()	{ return scaled(fontSize + 2); }        
        public int height()	{ return s2 + (lines+1) * lineH(); }
        public int hMax()	{ return hMax; }
        public int x()	    { return x; }
        public int y()	    { return y; }
        public int xe()		{ return x + w; }
        public int ye()		{ return y + height(); }
        public int xc()		{ return x + w/2; }
        public int yc()		{ return y + height()/2; }
        public int xcb()	{ return x + w/4; }
        public int ycb()	{ return y + height()/4; }
        public int xce()	{ return x + w*3/4; }
        public int yce()	{ return y + height()*3/4; }
        public void setLineColor(Color c)	{ lineC = c; }
        public void setTextColor(Color c)	{ textC = c; }
        public void setBackColor(Color c)	{ backC = c; }
        public void setLineArr(int... arr)	{ lineArr = arr; }
        public int[] rect(int x, int y, int w, int h)		{
        	return new int[] {x, y, x+w, y, x+w, y+h, x, y+h, x, y};
        }
        public void setLine(int x1, int y1, int x2, int y2) { setLine(x1, y1, x2, y2, -1, -1); }
        public void setLine(int x1a, int y1a, int x2a, int y2a, int x3a, int y3a) {
            x1 = x1a;
            y1 = y1a;
            x2 = x2a;
            y2 = y2a;
            x3 = x3a;
            y3 = y3a;
        }
        private void init()	{
            fontSize = FONT_SIZE;
            Font font = narrowFont(fontSize);
            List<String> linesList = wrappedLines(font, text, w - margin);
            lines = linesList.size();
            int specH = height();
            while ((specH > hMax) && (fontSize > MIN_FONT_SIZE)) {
                fontSize--;
                font = narrowFont(fontSize);
                linesList = wrappedLines(font, text, w - margin);
                lines = linesList.size();
                specH = height();
            }
        }
    }
	// -#-
	// ========================================================================
	// #=== Guide
	//
	private Color bgC		= GameUI.setupFrame(); // TODO put elsewhere
	private Color lineColor	= bgC;

	private void clearGuide()					{ guideData.clear(); }
	private void paintGuide(Graphics2D g)		{ guideData.paintGuide(g); }
	private void guideMouseMoved(MouseEvent e)	{
		//System.out.println("guideMouseMoved"); // TODO BR: REMOVE
		if (guideData.isEmpty()) {
			close(); // TODO BR: Maybe not
		}
		else if (!guideData.contains(e.getLocationOnScreen()))
			close();
	}

	class GuideData	{
		private Rectangle targetBox, validBox;
		private JTextPane guideBox;
		private int[] lineArr;
		private int left, top, w, h;

		private void clear()	{
			validBox	= null;
			guideBox	= null;
			targetBox	= null;
			lineArr		= null;
		}
		// Queries
		private boolean isEmpty()			{ return guideBox == null; }
		private boolean contains(Point p)	{ return validBox.contains(p); }
		private void paintGuide(Graphics2D g)	{
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
		private void prepareData()	{
			guideBox = new JTextPane();
			guideBox.setOpaque(true);
			guideBox.setContentType("text/html");
			guideBox.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			guideBox.setBackground(bgC);
			guideFontSize(FONT_SIZE);
		}
		private void init(JComponent target, String tipText, Rectangle valid)	{ init(target.getBounds(), tipText, valid); }
		private void init(Rectangle target, String tipText, Rectangle valid)	{
			prepareData();
			createGuideBox(target, tipText, valid);
		}
		// Creations
		private void createGuideBox(Rectangle target, String tipText, Rectangle valid)	{ createGuideBox(target, tipText, valid, false); }
		private void createGuideBox(Rectangle target, String tipText, Rectangle valid, boolean fullHelp)	{
//			targetBox = target;
			targetBox = valid;
			validBox  = valid;

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
			// TODO
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
			targetBox = target.getBounds();
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
			guideFontSize(FONT_SIZE);
			String txt = param.getGuide();
			if (txt == null || txt.isEmpty())
				return txt;

			txt = cleanHtmlText(txt);
			setSizeAndLocation(); // For position and arrow
			if (guideFontSize() < FONT_SIZE) {
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
			int xShift	= s20;
			int yShift	= s20;
			int xCover	= s10;
			int yCover	= s10;
			int xMargin	= s10;
			int yMargin	= s10;
			int xb, xd, yb, yd;
			int iW, iH;
			if (dialog == null) {
				iW = scaled(Rotp.IMG_W);
				iH = scaled(Rotp.IMG_H);
			}
			else {
				iW = dialog.getWidth();
				iH = dialog.getHeight();
			}
			xCover = min(xCover, targetBox.width/2);
			yCover = min(yCover, targetBox.height/2);

			autoSizeBox(s400);
			// relative position
			// find X location
			if (2*targetBox.x + targetBox.width  > iW) { // put box to the left
				left = targetBox.x - w - xShift;
				if (left < xMargin)
					left = xMargin;
				xb = left + w;
				xd = targetBox.x + xCover;
				if (xd < xb)
					xd = xb + s10;
			}
			else { // put box to the right
				left = targetBox.x + targetBox.width + xShift;
				if (left+w > iW-xMargin)
					left = iW-xMargin - w;
				xb = left;
				xd = targetBox.x + targetBox.width - xCover;
				if (xd > xb)
					xd = xb - s10;
			}
			// find Y location
			if (2*targetBox.y + targetBox.width  > iH) { // put box to the top
				top = targetBox.y - h - yShift;
				if (top < yMargin)
					top = yMargin;
				yb = top + h;
				yd = targetBox.y + yCover;
				if (yd < yb)
					yb = yd + s10;
			}
			else { // put box to the bottom
				top = targetBox.y + targetBox.height + yShift;
				if (top+h > iH-yMargin)
					top = iH-yMargin - h;
				yb = top;
				yd = targetBox.y + targetBox.height - yCover;
				if (yd > yb)
					yb = yd - s10;
			}
			if (targetBox.width>0) // no line for Hotkeys help
				setLineArr(xb, yb, xd, yd);
//			boxLocation.x = left;
//			boxLocation.y = top;
		}
		private void autoSizeBox(int maxWidth)	{
			int iW, iH;
			if (dialog == null) {
				iW = scaled(Rotp.IMG_W - 20);
				iH = scaled(Rotp.IMG_H - 20);
			}
			else {
				iW = dialog.getWidth() - s20;
				iH = dialog.getHeight() - s20;
			}
			int testW, preTest;
			w = Short.MAX_VALUE;
			boolean go = true;

			while (go) {
				guideBox.setFont(plainFont(guideFontSize()));
				h = Short.MAX_VALUE;
				preTest = -1;
				testW = maxWidth - 1; // to prevent rounding errors
				while (h > iH && preTest != testW && testW < iW) {
					preTest = testW;
					guideBox.setSize(new Dimension(testW, Short.MAX_VALUE));
					Dimension paneSize = guideBox.getPreferredSize();
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
			guideBox.setSize(autoSize);
		}
	}
	// -#-
}

