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
package rotp.ui.main.overlay;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

import rotp.model.Sprite;
import rotp.model.empires.Empire;
import rotp.ui.BasePanel;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.main.MainUI;
import rotp.ui.main.SystemPanel;
import rotp.ui.sprites.AdvisorOKSprite;

public final class MapOverlayAdvice implements IMapOverlay {
	public static final String MAIN_ADVISOR_SCOUT			= "MAIN_ADVISOR_SCOUT";
	public static final String MAIN_ADVISOR_TRANSPORT		= "MAIN_ADVISOR_TRANSPORT";
	public static final String MAIN_ADVISOR_DIPLOMACY		= "MAIN_ADVISOR_DIPLOMACY";
	public static final String MAIN_ADVISOR_SHIP_ENGINE		= "MAIN_ADVISOR_SHIP_ENGINE";
	public static final String MAIN_ADVISOR_RALLY_POINTS	= "MAIN_ADVISOR_RALLY_POINTS";
	public static final String MAIN_ADVISOR_MISSILE_BASES	= "MAIN_ADVISOR_MISSILE_BASES";
	public static final String MAIN_ADVISOR_SHIP_WEAPON		= "MAIN_ADVISOR_SHIP_WEAPON";
	public static final String MAIN_ADVISOR_COUNCIL			= "MAIN_ADVISOR_COUNCIL";
	public static final String MAIN_ADVISOR_REBELLION		= "MAIN_ADVISOR_REBELLION";
	public static final String MAIN_ADVISOR_COUNCIL_RESISTED= "MAIN_ADVISOR_COUNCIL_RESISTED";
	public static final String MAIN_ADVISOR_RESIST_COUNCIL	= "MAIN_ADVISOR_RESIST_COUNCIL";
	private static Color backDarkC = new Color(34,53,102);
	private static Color centerC = new Color(79,102,156);
	private RadialGradientPaint backPaint;
	// private LinearGradientPaint buttonPaint;
	private Rectangle bounds = new Rectangle();
	private MainUI parent;
	private BufferedImage advisorImg;
	private AdvisorOKSprite okButton;
	private String textKey;
	private Empire emp1;
	private String var1, var2, var3;
    public MapOverlayAdvice(MainUI p) {
        parent = p;
        okButton = new AdvisorOKSprite();
    }
    public void releaseObjects() {
    	emp1 = null;
    }
    public void init(String key, Empire e1, String s1, String s2, String s3) {
        textKey = key;
        emp1 = e1;
        var1 = s1;
        var2 = s2;
        var3 = s3;

		Empire pl = player();
		BufferedImage img;
		pl.resetScientist();
		pl.resetSpy();
		pl.resetDiplomat();
		pl.resetSoldier();
		switch(key) {
			case MAIN_ADVISOR_SCOUT:			img = pl.advisorScout();		break;
			case MAIN_ADVISOR_TRANSPORT:		img = pl.advisorTransport();	break;
			case MAIN_ADVISOR_DIPLOMACY:		img = pl.advisorDiplomacy();	break;
			case MAIN_ADVISOR_SHIP_ENGINE:		img = pl.advisorShip();			break;
			case MAIN_ADVISOR_RALLY_POINTS:		img = pl.advisorRally();		break;
			case MAIN_ADVISOR_MISSILE_BASES:	img = pl.advisorMissile();		break;
			case MAIN_ADVISOR_SHIP_WEAPON:		img = pl.advisorWeapon();		break;
			case MAIN_ADVISOR_COUNCIL:			img = pl.advisorCouncil();		break;
			case MAIN_ADVISOR_REBELLION:		img = pl.advisorRebellion();	break;
			case MAIN_ADVISOR_COUNCIL_RESISTED:	img = pl.advisorCouncilResisted();	break;
			case MAIN_ADVISOR_RESIST_COUNCIL:	img = pl.advisorResistCouncil();	break;
			default: img = pl.advisorRebellion();
		}

        advisorImg = newBufferedImage(img);
        okButton.draw(true);
        parent.repaint();
    }
    @Override
    public boolean hideNextTurnNotice()           { return false; }
    @Override
    public boolean canChangeMapScale()            { return true; }
    @Override
    public boolean consumesClicks(Sprite spr)  { return false; }
    @Override
    public boolean masksMouseOver(int x, int y)   { return bounds.contains(x,y); }
    @Override
    public boolean hoveringOverSprite(Sprite o) { return false; }
    @Override
    public void advanceMap() { parent.clearAdvice(); okButton.draw(false);}
    @Override
    public void paintOverMap(MainUI parent, GalaxyMapPanel ui, Graphics2D g) {
        if (advisorImg == null)
            return;

        int h = ui.getHeight();

        int mgn = s3;
        int buttonBarH = s68;

        int txtRightMargin  = s10;
        int txtLeftMargin   = scaled(155);
        int txtNominalWidth = scaled(215);
        int maxLineNumber   = 6;

        // BR: Increase with for long text
        // Title
        String title = text("MAIN_ADVISOR_TITLE", player().civilizationName(), player().leader().name());
        title = player().replaceTokens(title, "player");
        int sw = g.getFontMetrics(narrowFont(28)).stringWidth(title);
        int textWidth = max(txtNominalWidth, sw);
        // Description
        g.setFont(narrowFont(18));
        String desc;
        if (var1 == null)
            desc = text(textKey);
        else if (var2 == null)
            desc = text(textKey, var1);
        else if (var3 == null)
            desc = text(textKey, var1, var2);
        else
            desc = text(textKey, var1, var2, var3);

        desc = player().replaceTokens(desc, "player");

        if (emp1 != null)
            desc = emp1.replaceTokens(desc, "alien");

        List<String> lines = wrappedLines(g, desc, textWidth);
        int stepW = s10;
        while (lines.size() > maxLineNumber) {
        	textWidth += stepW;
        	lines = wrappedLines(g, desc, textWidth);
        }

        int boxW = txtRightMargin + textWidth + txtLeftMargin;
        int boxH = s200;

        if (backPaint == null) {
            float[] dist = {0.0f, 1.0f};
            Color[] colors = {centerC, backDarkC};
            Point2D center = new Point2D.Float(mgn+(boxW/2), h-(boxH/2)-buttonBarH);
            float radius = (float) (Math.sqrt((boxW*boxW)+(boxH*boxH))/2);
            backPaint = new RadialGradientPaint(center, radius, dist, colors);
        }
        g.setPaint(backPaint);
        int boxBottom = h-buttonBarH;
        int boxTop = boxBottom-boxH;
        int boxLeft = mgn;
        int boxRight = boxLeft+boxW;
        g.fillRect(mgn, boxTop, boxW, boxH);
        bounds.setBounds(mgn, boxTop, boxW, boxH);

        if (okButton.hovering()) {
            Stroke prev = g.getStroke();
            g.setStroke(BasePanel.stroke2);
            g.setColor(BasePanel.hoverC);
            g.drawRect(mgn, boxTop, boxW, boxH);
            g.setStroke(prev);
        }

        // draw text title
        int textMgn = boxLeft + txtLeftMargin;
        g.setFont(narrowFont(28));
        drawShadowedString(g, title, 4, textMgn, boxTop+scaled(28), SystemPanel.textShadowC, SystemPanel.whiteText);

        int imgW = advisorImg.getWidth();
        int imgH = advisorImg.getHeight();
        int dispW = imgW*s160/440;
        int dispH = imgH*dispW/imgW;
        int x1 = boxLeft;
        int y1 = boxBottom-dispH;
        g.drawImage(advisorImg, x1, y1, x1+dispW, y1+dispH, 0, 0, imgW, imgH, null);

        // draw text
        g.setFont(narrowFont(18));
        int lineH = s19;
        int y0 = boxTop + s55;
        for (String line: lines) {
            drawString(g,line, textMgn, y0);
            y0 += lineH;
        }
        String okStr = text("MAIN_ADVISOR_BUTTON_OK");
        g.setFont(narrowFont(20));
        int okW = g.getFontMetrics().stringWidth(okStr);
        // draw button
        int buttonW = Math.max(s75, okW+s30);
        int buttonX = boxRight-buttonW-txtRightMargin;
        int buttonY = boxBottom - s40;
        int buttonH = s30;
        parent.addNextTurnControl(okButton);
        okButton.parent(this);
        okButton.setBounds(buttonX, buttonY, buttonW, buttonH);
        okButton.setSelectionBounds(bounds.x, bounds.y, bounds.width, bounds.height);
        okButton.draw(parent.map(), g);
    }
    @Override
    public boolean handleKeyPress(KeyEvent e) {
		if (baseHandleKeyPress(e))
			return true;
        switch(e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                advanceMap();
                return true;
            case KeyEvent.VK_L:
				if (e.isAltDown()) {
					debugReloadLabels(parent);
					return true;
				}
				return false;
            default:
                return false;
        }
    }
}
