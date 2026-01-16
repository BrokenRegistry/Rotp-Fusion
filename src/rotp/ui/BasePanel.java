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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import rotp.Rotp;
import rotp.model.Sprite;
import rotp.model.galaxy.StarType;
import rotp.model.game.IDebugOptions;
import rotp.ui.main.MainUI;
import rotp.ui.main.SystemPanel;
import rotp.ui.util.InterfacePreview;
import rotp.util.Base;
import rotp.util.ThickBevelBorder;

public class BasePanel extends JPanel implements Base, ScaledInteger, InterfacePreview {
    private static final long serialVersionUID = 1L;
    public static final String TEXTURE_GRAY = "TEXTURE_GRAY";
    public static final String TEXTURE_BROWN = "TEXTURE_BROWN";

    protected static GraphicsConfiguration gc;
    public static final Color hoverC = Color.yellow;
    public static final Color depressedC = new Color(208,160,0);

    public static Color buttonLighter = new Color(192,192,192);
    public static Color buttonLight = new Color(156,156,156);
    public static Color buttonColor = new Color(123,123,123);
    public static Color buttonDark = new Color(83,83,83);
    public static Color buttonDarker = new Color(63,63,63);
    private static Border buttonBevelBorder;
    static final Color greenText = Color.green;
    static final Color greenBackground = new Color(0, 128, 0, 128);
    protected static final Color greenGaiaText	  = Color.green;
    protected static final Color greenFertileText = new Color(0, 166, 0);

    static Image textureGray, textureBrown;

    static Color borderLight0 = new Color(169,127,99);
    static Color borderLight1 = new Color(151,112,90);
    static Color borderShade0 = new Color(85,64,47);
    static Color borderShade1 = new Color(61,48,28);
    static Color backShade = new Color(0,0,0,128);
    static Border shadedBorder;

    protected BufferedImage starBackground;
    protected int starScrollX = 0;
    protected Image screenBuffer;

    public static GraphicsConfiguration gc() {
        if (gc == null)
            gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        return gc;
    }
    public void showHotKeys()              { showHelp(); }
    public void showHelp()                 {  }
    public void cancelHelp()               {  }
    public void advanceHelp()              { }

    public boolean hasStarBackground()     { return false; }
    public final boolean hasTexture()      { return textureName() != null; }
    public boolean isAlpha()               { return false; }
    public String textureName()            { return null; }
    public Shape textureClip()             { return null; }
    public Area textureArea()              { return null; }
    public boolean drawMemory()            { return false; }
    public int minStarDist()               { return 50; }
    public int varStarDist()               { return 100; }

    public boolean canEscape()             { return false; }
    public Color starBackgroundC()         { return Color.black; }
    public Border buttonBevelBorder() {
        if (buttonBevelBorder == null)
            buttonBevelBorder =  new ThickBevelBorder(4, 1, buttonLighter, buttonLight, buttonDarker, buttonDark, buttonDark, buttonDarker, buttonLight, buttonLighter);
        return buttonBevelBorder;
    }
    public Border shadedBorder() {
        if (shadedBorder == null)
            shadedBorder = new ThickBevelBorder(5, borderShade0, borderShade1, borderShade0, borderShade1, borderLight0, borderLight1, borderLight0, borderLight1);
        return shadedBorder;
    }
    protected Image screenBuffer() {
        if (screenBuffer == null)
            screenBuffer = newOpaqueImage(getWidth(), getHeight());
        return screenBuffer;
    }
    protected void clearBuffer() {
        screenBuffer = null;
    }

    public boolean useNullClick(int cnt, boolean right) { return false; }
    public boolean useClickedSprite(Sprite o, int count, boolean rightClick)   { return false; }
    public boolean useHoveringSprite(Sprite o)          { return false; }

    public JFrame frame()                  { return (JFrame) SwingUtilities.getRoot(RotPUI.instance()); }
    public void enableGlassPane(BasePanel p) {
        p.setVisible(false);
        frame().setGlassPane(p);
        p.setVisible(true);
    }
    public void disableGlassPane()  { frame().getGlassPane().setVisible(false); }
    public void showError(String s) {
        disableGlassPane();
        ErrorDialogPanel err = new ErrorDialogPanel(s);
        enableGlassPane(err);
    }
    public void cancel()   { }
    public void open()     { }
    public void handleNextTurn()    { }
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (hasTexture())
            drawTexture(g);
        if (drawMemory())
            drawMemory(g);
    }
    protected void jPanelPaintComponent(Graphics g) {
        setRenderingHints(g);
        super.paintComponent(g);
    }
    protected void jPanelPaint(Graphics g) {
        super.paint(g);
    }
    public BasePanel glassPane() {
        if (frame() != null) {
            Component pane = frame().getGlassPane();
            if ((pane instanceof BasePanel) && pane.isVisible())
                return (BasePanel)frame().getGlassPane();
        }
        return null;
    }
    @Override
    public void paintComponent(Graphics g) {
        setRenderingHints(g);

        if (hasStarBackground())
            setBackground(starBackgroundC());

        super.paintComponent(g);

        if (hasStarBackground())
            drawStars(g);
    }
    protected void setFPS(int fps) {
        RotPUI.fps(fps);
    }
    protected void resetFPS() {
        setFPS(10);
    }
    protected void drawStars(Graphics g) {
        drawStars(g, getWidth(), getHeight());
    }
    protected void  drawStars(Graphics g, int w, int h) {
        BufferedImage stars = starBackground(w, h);
        int scroll = starScrollX % w;
        if (scroll == 0)
            g.drawImage(stars, 0, 0, w, h, 0, 0, w, h, null);
        else {
            //g.drawImage(stars, 0, 0, w, h, 0, 0, w, h, null);
            g.drawImage(stars, 0, 0, w-scroll, h, scroll, 0, w, h, null);
            g.drawImage(stars, w-scroll, 0, w, h, 0, 0, scroll, h, null);
        }
    }
    protected BufferedImage newStarBackground() {
        initializeStarBackgroundImage(this,getWidth(),getHeight());
        return starBackground;
    }
    protected BufferedImage starBackground() {
        return starBackground == null ? newStarBackground() : starBackground;
    }
    protected BufferedImage starBackground(int w, int h) {
        if (starBackground == null)
            initializeStarBackgroundImage(this,w,h);
        return starBackground;
    }
    public void actionPerformed(ActionEvent e) { }

    public void animateForLowGraphic() { }
    public void animate() { }
    public void drawStar(Graphics2D g2, StarType sType, int r, int x0, int y0) {
        Composite prev = g2.getComposite();
        g2.setComposite(AlphaComposite.SrcOver);
        BufferedImage img = sType.image(r,0);
        int w = img.getWidth();
        //g2.drawImage(img,x0-(w/2),y0-(w/2),null);
        g2.drawImage(img,x0-r,y0-r,x0+r,y0+r,0,0,w,w,null);
        g2.setComposite(prev);
    }
    public void initializeStarBackgroundImage(JPanel obs, int w, int h) {
        starBackground =  new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        drawBackgroundStars(starBackground, obs, minStarDist(), varStarDist());
    }
    public BufferedImage newStarBackground(JPanel obs, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        drawBackgroundStars(img, obs, minStarDist(), varStarDist());
        return img;
    }
    public void drawSkipText(Graphics g, boolean clickContinue) {
        int y0 = getHeight()-s8;
        drawSkipText(g, y0, clickContinue);
    }
    public void drawSkipText(Graphics g, int y0, boolean clickContinue) {
        String skipText = clickContinue ? text("CLICK_CONTINUE") : text("CLICK_SKIP");
        g.setFont(narrowFont(20));
        int sw = g.getFontMetrics().stringWidth(skipText);
        int x0 = (getWidth()-sw)/2;
        drawBorderedString(g, skipText, x0, y0, Color.black, Color.lightGray);
    }
    public void drawNotice(Graphics g, int fontSize) {
        drawNotice(g, fontSize, 0);
    }
	public void drawNotice(Graphics g, int fontSize, int yAdj)	{ drawNotice(g, fontSize, yAdj, true); }
    public void drawNotice(Graphics g, int fontSize, int yAdj, boolean shading)	{
        int w = getWidth();
        int h = getHeight();
        int bdrW = s7;
        boolean autoRun = IDebugOptions.debugAutoRun();

        g.setColor(backShade);
        if (shading && !autoRun) // BR: No shading when auto-run
        	g.fillRect(0,0,getWidth(), getHeight());

        String title = NoticeMessage.title();
        String step = NoticeMessage.step();
        int fontSize2 = fontSize*4/5;

        g.setFont(narrowFont(fontSize));
        int sw1 = g.getFontMetrics().stringWidth(title);
        g.setFont(narrowFont(fontSize2));
        int sw2 = g.getFontMetrics().stringWidth(step);
        int sw = max(sw1,sw2);
        int noticeW = sw+s60;
        int noticeH = step.isEmpty() ? scaled(6+fontSize)+bdrW+bdrW : scaled(6+(fontSize*7/4))+bdrW+bdrW;

        int x = (w-sw)/2;
        int y = (h+yAdj)/2;
        if (autoRun) { // let us see the map while auto-run
        	y = h - noticeH - bdrW ;
        	// x = bdrW+s10;
        	// y = bdrW+s10;
       }

        g.setColor(MainUI.paneShadeC);
        g.fillRect(x-bdrW, y-bdrW, noticeW+bdrW+bdrW, noticeH+bdrW+bdrW);
        g.setColor(MainUI.paneBackground);
        g.fillRect(x, y, noticeW, noticeH);

        g.setFont(narrowFont(fontSize));
        int y1 = y+scaled(fontSize)+bdrW-s5;
        drawShadowedString(g, title, 2, x+((noticeW-sw1)/2), y1, SystemPanel.textShadowC, SystemPanel.whiteText);
        if (!step.isEmpty()) {
            int y2 = y+noticeH-bdrW-s6;
            g.setFont(narrowFont(fontSize2));
            drawShadowedString(g, step, 2, x+((noticeW-sw2)/2), y2, SystemPanel.textShadowC, SystemPanel.whiteText);
        }
    }
    public void redrawMemory() {
        repaint(getWidth()-s100,getHeight()-s50,s100,s50);
    }
    public boolean displayMemory() {
        return UserPreferences.showMemory();
    }

    public void drawMemory(Graphics g) {
        if (!displayMemory())
            return;
        g.setColor(Color.white);
        g.setFont(narrowFont(14));
        String s = Rotp.getMemoryInfo(true);
        int sw = g.getFontMetrics().stringWidth(s);
        drawString(g, s, getWidth()-sw-s5, getHeight()-s5);
    }
    public void drawTexture(Graphics g0) {
        drawTexture(g0, 0, 0, getWidth(), getHeight());
    }

    public void drawTextureWithExistingClip(Graphics g0, int x, int y, int w, int h) {
       if (!UserPreferences.texturesInterface())
            return;
        float pct = UserPreferences.uiTexturePct();
        if (pct <= 0)
            return;
        if (pct > 1)
            pct = 1;
        Graphics2D g = (Graphics2D) g0;
        Image texture = null;
        switch(textureName()) {
            case TEXTURE_BROWN:
                if (textureBrown == null)
                    textureBrown = image(TEXTURE_BROWN);
                texture = textureBrown;
                break;
            case TEXTURE_GRAY:
                if (textureGray == null)
                    textureGray = image(TEXTURE_GRAY);
                texture = textureGray;
        }
        if (texture == null)
            return;

        int imgW = texture.getWidth(null);
        int imgH = texture.getHeight(null);

        Composite prevComposite = g.getComposite();
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pct);
        g.setComposite(ac);
        g.drawImage(texture, x, y, x+w, y+h, 0, 0, min(x+w,imgW), min(imgH,y+h), this);
        g.setComposite(prevComposite);        
    }
    public void drawTexture(Graphics g0, int x, int y, int w, int h) {
        Shape clip = (textureArea() != null) ? textureArea() : textureClip(); 
        g0.setClip(clip);
        drawTextureWithExistingClip(g0, x,y,w,h);
        g0.setClip(null);
    }
    public void drawTexture(Graphics g0, Shape clip, int x, int y, int w, int h) {
        g0.setClip(clip);
        drawTextureWithExistingClip(g0, x,y,w,h);
        g0.setClip(null);
    }

    // used for keyEvents sent from RotPUI
    public void keyPressed(KeyEvent e) { setModifierKeysState(e); }
    public void keyReleased(KeyEvent e) { setModifierKeysState(e); }
    public void keyTyped(KeyEvent e) { }
    public void playAmbience() {
            playAmbience(ambienceSoundKey());
    }
    public String defaultAmbience(){
        if (galaxy() == null)
            return "IntroAmbience";
        if (galaxy().council().finalWar())
            return "FinalWarAmbience"; 
        if (player().atWar())
            return "WarAmbience"; 
        else if (player().hasAnyContact())
            return "ContactAmbience";
        else
            return "ExploreAmbience";        
    }
    public String ambienceSoundKey() { 
        return defaultAmbience();
    }
    // BR:
    public boolean checkModifierKey(InputEvent e) {
		if (checkForChange(e)) {
			repaintKeyBound();
			return true;
		}
		return false;
	}
    public void repaintKeyBound() { repaint(); }
}
