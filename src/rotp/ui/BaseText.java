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
package rotp.ui;

import static rotp.ui.BasePanel.s5;
import static rotp.ui.BasePanel.s50;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import rotp.util.Base;
import rotp.util.LanguageManager;


public class BaseText implements Base {
    static int ST_NORMAL = 0;
    static int ST_LOCKED = 1;
    static int ST_HOVER = 1;
    static int ST_PRESSED = 1;

    private final BasePanel panel;
    protected Color enabledC, disabledC, hoverC, depressedC, shadeC; // BR not final
	private final int topLBdr, btmRBdr; // , bdrStep;
    private final Rectangle bounds = new Rectangle();
    private String text, hoverText;
    private int x,y;
    private boolean disabled;
    private boolean visible = true;
    private boolean depressed = false;
    private boolean hovered = false;
    private BufferedImage textShadow;
    private int bufferedLanguage = -1;
    private boolean logoFont = false;
    private boolean bordered = false;
    private int fontSize = 10;
    private int xOrig;
    private int yOrig;
    BaseText preceder;
    // BR: For boundaries only
    // Fixed Width for easy scrolling even if the text is short
    // No need to put the mouse on the text
    // Margins to include some icons
    private int width = 200;
    private int rightMargin	= 0;
    private int leftMargin	= 0;
    private boolean fixedWidth = false;
    public BasePanel panel() { return panel; } // BR:
    public void setRightMargin(int margin) 		{ rightMargin = margin; }
    public void setLeftMargin(int margin) 		{ leftMargin  = margin; }
    public void setFixedWidth(boolean b, int w)	{
    	fixedWidth = b;
    	width = w;
    }

    /**
     * @param p		BasePanel
     * @param logo	logoFont
     * @param fSize	fontSize
     * @param x1	xOrig
     * @param y1	yOrig
     * @param c1	enabledC
     * @param c2	disabledC
     * @param c3	hoverC
     * @param c4	depressedC
     * @param c5	shadeC
     * @param i1	bdrStep (never used)
     * @param i2	topLBdr
     * @param i3	btmRBdr
     */
    public BaseText(BasePanel p, boolean logo, int fSize, int x1, int y1, Color c1, Color c2, Color c3, Color c4, Color c5, int i1, int i2, int i3) {
        panel = p;
        
        logoFont = logo;
        fontSize = fSize;
        xOrig = x1;
        yOrig = y1;
        x = scaled(x1);
        y = scaled(y1);
        enabledC = c1;
        disabledC = c2;
        hoverC = c3;
        depressedC = c4;
        shadeC = c5;
        //bdrStep = i1;
        topLBdr = i2;
        btmRBdr = i3;
    }
    public void enabledC(Color newColor) { // BR: for triple-color management
    	enabledC = newColor;
    }
    public void setScaledXY(int x1, int y1) {
        xOrig = unscaled(x1);
        yOrig = unscaled(y1);
        x = x1;
        y = y1;
    }
    public void setY(int y1) {
        yOrig = y1;
        y = scaled(y1);
    }
    protected void newFontSize(int fSize) { fontSize = fSize; }
    public boolean decrFontSize(int minSize) {
    	fontSize -= 1;
    	if (fontSize < minSize) {
    		fontSize = minSize;
    		return false;
    	}
    	return true;
    }
    private Font font() {
        return logoFont ? logoFont(fontSize) : narrowFont(fontSize);
    }
    @SuppressWarnings("unused")
	private boolean centered() {  return (xOrig == 0) && (preceder == null); }
    @Override
    public String toString()  { return concat("Text:", text, "  at:", bounds.toString()); }
    public void displayText(String s) { text = s; }
    public void hoverText(String s)   { hoverText = s; }
    public int x()                    { return bounds.x; }
    public int y()                    { return bounds.y; }
    public int w()                    { return bounds.width; }
    public int h()                    { return bounds.height; }
    public int bottomY()              { return bounds.y + bounds.height; }
    public Rectangle bounds()         { return bounds; }
    public void disabled(boolean b)   { disabled = b; }
    public void visible(boolean b)    { visible = b; }
    public void bordered(boolean b)   { bordered = b; }
    public void preceder(BaseText t)  { preceder = t; }
    public boolean isHovered()        { return hovered; }
    public boolean isEmpty()          { return text.isEmpty(); }
    public void setBounds(int x, int y, int w, int h) {
        bounds.setBounds(x,y,w,h);
    }
    public void shiftBounds(int dx, int dy) {
    	int newX = bounds.x + dx;
    	int newY = bounds.y + dy;
    	bounds.setLocation(newX, newY);
    }
    public void reset() { 
        bounds.setBounds(0,0,0,0); 
    }
    public void rescale() {
        x = scaled(xOrig);
        y = scaled(yOrig);
    }
    public boolean contains(int x, int y) {
        return visible && bounds.contains(x, y);
    }
    public void repaint(String s) {
        Graphics g = panel.getGraphics();
        int oldW = stringWidth(g);
        displayText(s);
        int newW = stringWidth(g);
        g.dispose();
        if (!fixedWidth) // BR: fixed Width for scrolling
        	bounds.width = max(oldW, newW) + s5;
        repaint();
    }
    public void repaint(String s1, String s2) {
        Graphics g = panel.getGraphics();
        int oldW = stringWidth(g);
        displayText(s1);
        hoverText(s2);
        int newW = stringWidth(g);
        g.dispose();
        if (!fixedWidth) // BR: fixed Width for scrolling
        	bounds.width = max(oldW, newW) + s5;
        repaint();
    }
    public void repaint() {
        panel.repaint(bounds.x, bounds.y, bounds.width+s50, bounds.height);
    }
    public void mousePressed() {
        depressed = true;
    }
    public void mouseReleased() {
        depressed = false;
    }
    public void mouseEnter() {
        hovered = true;
    }
    public void mouseExit() {
        hovered = false;
        depressed = false;
    }
    public int stringWidth(Graphics g) {
        int sw1 = text == null ? 0 : g.getFontMetrics(font()).stringWidth(text);
        int sw2 = hoverText == null ? sw1 : g.getFontMetrics(font()).stringWidth(hoverText);
        return max(sw1,sw2);
    }
    protected Color textColor() {
        if (disabled)
            return disabledC;
        else if (depressed)
            return depressedC;
        else if (hovered)
            return hoverC;
        else
            return enabledC;
    }
    public String displayText() {
        if (hovered && (hoverText != null))
            return hoverText;
        else
            return text;
    }
    public int draw() {
        return draw(panel.getGraphics());
    }
    public int drawCentered() {
        return drawCentered(panel.getGraphics());
    }
    public void updateBounds(Graphics g) { update(g, false); } // BR:
    public int draw(Graphics g) { return update(g, true); }
    private int update(Graphics g, boolean draw) { // BR:
        if (!visible)
            return 0;
        int x1 = x >= 0 ? x : panel.getWidth()+x;
        int y1 = y >= 0 ? y : panel.getHeight()+y;
        
        if ((preceder != null) && (x>= 0)) {
            x1 = preceder.x() + preceder.w() + x;
        }
        g.setFont(font());
        g.setColor(textColor());
        int sw = stringWidth(g);
        int fontH = g.getFontMetrics().getHeight();
        // BR: fixed Width for scrolling
        if (!fixedWidth)
        	setBounds(x1-leftMargin, y1-fontH, sw+leftMargin+rightMargin+s5, fontH+(fontH/5));
        else {
        	if (width > 0)
        		setBounds(x1-leftMargin, y1-fontH-1, width, fontH+(fontH/5)+2);
        	else // offset
        		setBounds(x1+width, y1-fontH, sw+s5-width, fontH+(fontH/5));
        }
        if (draw) {
            if (bordered)
                drawBorderedString(g,displayText(), x1, y1, Color.black,textColor());
            else 
                drawString(g,displayText(), x1,y1);
        }
        return x1+sw;
    }
    public int drawCentered(Graphics g) {
        if (!visible)
            return 0;
        g.setFont(font());
        int w = panel.getWidth();
        int sw = stringWidth(g);
        int x1 = (w-sw)/2;
        int y1 = y > 0 ? y : panel.getHeight()+y;
        int fontH = g.getFontMetrics().getHeight();
        int sp = fontH/4;
        int hPad = sw/20;
        int vPad = fontH/5;
        setBounds(x1,y1+sp-fontH,sw+s5,fontH-sp/2);
        
        int shadowImgW = sw+hPad+hPad;
        if ((textShadow == null)                                      // first time through?
        || (bufferedLanguage != LanguageManager.selectedLanguage())   // language changed?
        || (shadowImgW != textShadow.getWidth())) {                   // font resized?
            bufferedLanguage = LanguageManager.selectedLanguage();
            textShadow = newBufferedImage(shadowImgW, fontH+vPad);
            Graphics g0 = textShadow.getGraphics();
            g0.setFont(font());
            g0.setColor(shadeC);
            int topThick = scaled(topLBdr);
            int thick = btmRBdr;
            for (int x0=(0-topThick);x0<=thick;x0++) {
                int x0s = scaled(x0);
                for (int y0=(0-topThick);y0<=thick;y0++) {
                    int y0s = scaled(y0);
                    drawString(g0,text, hPad+x0s, fontH+y0s);
                }
            }
            g0.dispose();
        }
        Color c0 = textColor();
        g.setColor(c0);
        g.drawImage(textShadow, x1-hPad, y1-fontH, null);
        drawString(g,displayText(), x1, y1);
        return x1+sw;
    }
}
