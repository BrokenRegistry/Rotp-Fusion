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

import static rotp.ui.game.GuideUI.cleanHtmlText;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JEditorPane;

import rotp.Rotp;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.main.SystemPanel;


public class HelpUI extends BasePanel implements MouseListener {
    private static final long serialVersionUID = 1L;
    private static final Color backgroundHaze = new Color(0,0,0,40);
	private static final int FONT_SIZE	= 16;
	//private static final int MIN_FONT_SIZE	= 10;
    private static int margin = s30;
    private final Color blueBackC  = new Color(78,101,155);
    private final Color brownBackC = new Color(240,240,240);
    private final Color brownTextC = new Color(45,14,5);

    private List<HelpSpec> specs = new ArrayList<>();
	private BasePanel parent;

    public HelpUI() {
        init();
    }
    private void init() {
        setOpaque(false);
        addMouseListener(this);
    }
    public void open(BasePanel p) {
        parent = p;
        enableGlassPane(this);
    }
    public void close() {
    	clear();
    	disableGlassPane();
    }
    public void clear() { specs.clear(); }

    public HelpSpec addBrownHelpText(int x, int y, int w, int num, String text) {
        HelpSpec sp = addBlueHelpText(x,y,w,num,text);
        sp.backC = brownBackC;
        sp.textC = brownTextC;
        return sp;
    }
	public HelpSpec addBlueHelpText(int x, int y, int w, int num, String text)	{
		if (text == null)
			text = "null";
		HelpSpec sp = new HelpSpec();
		sp.guideBox.setText(cleanHtmlText(text));
		sp.x = x;
		sp.w = w;

		if (num==0)
			sp.hMax = sp.autoSizeBox(w-margin, 0);
		else if (num<0) {
			sp.lines = -num;
			sp.hMax  = sp.hInit();
			sp.hMax = sp.autoSizeBox(w-margin, sp.hMax);
		}
		else {
			sp.lines = num;
			sp.hMax  = sp.hInit();
			sp.hMax = sp.autoSizeBox(w-margin, sp.hMax);
		}

		if (y<0) // position of the bottom of the box
			sp.y = -y - sp.hMax;
		else
			sp.y = y;

		specs.add(sp);
		return sp;
	}
	@Override public void paintComponent(Graphics g0)	{
		super.paintComponent(g0);
		Graphics2D g = (Graphics2D) g0;
		paintComponent(g, true);
	}
	public void paintComponent(Graphics2D g, boolean withHaze)	{
		if (withHaze) {
			g.setColor(backgroundHaze);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		for (HelpSpec spec: specs)
			spec.paintComponent(g);
	}
    @Override public void keyPressed(KeyEvent e)	{
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

    private static int lineH(int fontSize)				{ return RotPUI.scaledSize(fontSize + 2); }
    private static int height(int lines, int fontSize)	{ return s2 + (lines + 1) * lineH(fontSize) ; }
    static int lineH()									{ return lineH(FONT_SIZE); }
    static int height(int lines)						{ return height(lines, FONT_SIZE); }

    public class HelpSpec {
		private JEditorPane guideBox = new JEditorPane();
		private int x, y, w, tw, th;
        private int lines, hMax;
        private int fontSize = FONT_SIZE;
        private int[] lineArr; // BR: to allow frames
        private int x1 = -1;
        private int y1 = -1;
        private int x2 = -1;
        private int y2 = -1;
        private int x3 = -1;
        private int y3 = -1;
        private Color backC = blueBackC;
        private Color textC = Color.white;
        private Color lineC = Color.white;
		HelpSpec()	{
			guideBox.setText("");
			guideBox.setOpaque(false);
			guideBox.setContentType("text/html");
			guideBox.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			guideBox.setBackground(new Color(0, 0, 0, 0));
			guideBox.setForeground(SystemPanel.whiteText);
		}
		private int hInit()	{ return s2 + (lines+1) * lineH(); }
		public int height()	{ return hMax; }
		public int lineH()	{ return scaled(fontSize + 2); }
        public int x()		{ return x; }
        public int y()		{ return y; }
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
		public int[] rect(int x, int y, int w, int h)		{ return new int[] {x, y, x+w, y, x+w, y+h, x, y+h, x, y}; }
		public void setLine(int x1, int y1, int x2, int y2)	{ setLine(x1, y1, x2, y2, -1, -1); }
		public void setLine(int x1a, int y1a, int x2a, int y2a, int x3a, int y3a)	{
            x1 = x1a;
            y1 = y1a;
            x2 = x2a;
            y2 = y2a;
            x3 = x3a;
            y3 = y3a;
        }
		private void paintComponent(Graphics2D g)	{
			// draw background box
			Color bdrC  = new Color(backC.getRed(), backC.getGreen(), backC.getBlue(), 160);
			g.setColor(bdrC);
			g.fillRect(x, y, w, hMax);
			g.setColor(backC);
			int xx = x+s5;
			int yy = y+s5;
			g.fillRect(xx, yy, w-s10, hMax-s10);

			// draw box text
			guideBox.setForeground(textC);
			xx += s10;
			g.translate(xx, yy);
			guideBox.paint(g);
			g.translate(-xx, -yy);

			int xe = x2;
			int ye = y2;
			// BR: draw lines of target Array
			if (lineArr != null) {
				Stroke prev = g.getStroke();
				g.setStroke(stroke2);
				g.setColor(lineC);
				int size = lineArr.length/2 - 1;
				for (int i=0; i<size; i++) {
					int k = 2*i;
					xe = lineArr[k+2];
					ye = lineArr[k+3];
					g.drawLine(lineArr[k], lineArr[k+1], xe, ye);
				}
				g.setStroke(prev);
			}
			// draw line to target
			if (x2 >= 0) {
				xe = x2;
				ye = y2;
				Stroke prev = g.getStroke();
				g.setStroke(stroke2);
				g.setColor(lineC);
				g.drawLine(x1, y1, x2, y2);
				if (x3 >=0) {
					g.drawLine(x2, y2, x3, y3);
					xe = x3;
					ye = y3;
				}
				g.setStroke(prev);
			}
			int r = s3;
			g.fillOval(xe-r, ye-r, r+r, r+r);
		}
		private int autoSizeBox(int maxW, int maxH)	{
			int iW = maxW == 0? scaled(Rotp.IMG_W) : maxW;
			int iH = maxH == 0? scaled(Rotp.IMG_H) : maxH;
			int testW, preTest;
			tw = Short.MAX_VALUE;
			boolean go = true;
			int guideFontSize = FONT_SIZE;

			while (go) {
				guideBox.setFont(narrowFont(guideFontSize));
				th = Short.MAX_VALUE;
				preTest = -1;
				testW = maxW - 1; // to prevent rounding errors
				while (th > iH && preTest != testW && testW < iW) {
					preTest = testW;
					guideBox.setSize(new Dimension(testW, Short.MAX_VALUE));
					Dimension paneSize = guideBox.getPreferredSize();
					tw = min(testW, paneSize.width);
					th = paneSize.height;
					testW *= (float) th /iH;
				}
				go = tw > iW || th > iH;
				if (go) {
					guideFontSize = max(1, min(guideFontSize-1, (int)(guideFontSize * (float)iH/th -1)));
					go = guideFontSize > 1;
				}
			}
			tw += 1;
			Dimension autoSize = new Dimension(tw, th);
			guideBox.setSize(autoSize);
			return th+s12;
		}
	}
}

