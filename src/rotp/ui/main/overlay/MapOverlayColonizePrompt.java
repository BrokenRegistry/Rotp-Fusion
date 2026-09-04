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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.List;

import rotp.model.Sprite;
import rotp.model.empires.Empire;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.model.ships.ShipDesign;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.main.MainUI;
import rotp.ui.main.SystemPanel;
import rotp.ui.sprites.ColonizeNoSprite;
import rotp.ui.sprites.ColonizeYesSprite;
import rotp.ui.sprites.SystemFlagSprite;

public final class MapOverlayColonizePrompt implements IMapOverlay {
	private Color maskC  = new Color(40,40,40,160);
	private Area mask;
	private BufferedImage planetImg;
	private MainUI parent;
	private int sysId;
	private ShipFleet fleet;
	private ShipDesign design;
	private boolean drawSprites = false;
	private ColonizeNoSprite noButton	= new ColonizeNoSprite();
	private ColonizeYesSprite yesButton	= new ColonizeYesSprite();
	private SystemFlagSprite flagButton	= new SystemFlagSprite();

	public MapOverlayColonizePrompt(MainUI p)	{ parent = p; }
    public void releaseObjects() {
    	fleet = null;
    	design = null;
    }
    public void init(int systemId, ShipFleet fl, ShipDesign d) {
        StarSystem sys = galaxy().system(systemId);
        sysId = systemId;
        fleet = fl;
        design = d;
        noButton.reset();
        yesButton.reset();
        flagButton.reset();
        drawSprites = true;
        parent.hideDisplayPanel();
        parent.map().setScale(20);
        parent.map().recenterMapOn(sys);
        parent.map().resetRangeAreas();
        parent.mapFocus(sys);
        parent.clickedSprite(sys);
        parent.repaint();
    }
    private void toggleFlagColor(boolean reverse) {
        player().sv.toggleFlagColor(sysId, reverse);
        parent.repaint();
    }
    @Override
    public boolean drawSprites()   { return drawSprites; }
    public void colonizeYes() {
        if (drawSprites) {
            drawSprites = false;
            mask = null;
            planetImg = null;
            softClick();
            parent.clearOverlay();
            parent.repaintAllImmediately();
            RotPUI.instance().selectColonizationPanel(sysId, fleet, design);
        }
    }
    public void colonizeNo() {
        if (drawSprites) {
            drawSprites = false;
            mask = null;
            planetImg = null;
            advanceMap();
        }
    }
    @Override
    public boolean masksMouseOver(int x, int y)   { return true; }
    @Override
    public boolean hoveringOverSprite(Sprite o) { return false; }
    @Override
    public void advanceMap() {
        parent.resumeTurn();
    }
    @Override
    public void paintOverMap(MainUI parent, GalaxyMapPanel ui, Graphics2D g) {
        StarSystem sys = galaxy().system(sysId);
        boolean hasPlague = sys.hasPlague();
        Empire pl = player();

        int w = ui.getWidth();
        int h = ui.getHeight();

        int bdrW = s7;
        int boxW = scaled(540);
        int boxH = scaled(240);
        int boxH1 = BasePanel.s68;

        int boxX = -s40+(w/2);
        int boxY = s40+(h-boxH)/2;

        // draw map mask
        if (mask == null) {
            int r = s60;
            int centerX = w*2/5;
            int centerY = h*2/5;
            Ellipse2D window = new Ellipse2D.Float();
            window.setFrame(centerX-r, centerY-r, r+r, r+r);
            Area st1 = new Area(window);
            Rectangle blackout  = new Rectangle();
            blackout.setFrame(0,0,w,h);
            mask = new Area(blackout);
            mask.subtract(st1);
        }
        g.setColor(maskC);
        g.fill(mask);
        // draw border
        g.setColor(MainUI.paneShadeC);
        g.fillRect(boxX-bdrW, boxY-bdrW, boxW+bdrW+bdrW, boxH+bdrW+bdrW);

        // draw Box
        g.setColor(MainUI.paneBackground);
        g.fillRect(boxX, boxY, boxW, boxH1);

        // draw planet image
        if (planetImg == null) {
            if (sys.planet().type().isAsteroids()) {
                planetImg = newBufferedImage(boxW, boxH-boxH1);
                Graphics imgG = planetImg.createGraphics();
                imgG.setColor(Color.black);
                imgG.fillRect(0, 0, boxW, boxH-boxH1);
                drawBackgroundStars(imgG, boxW, boxH-boxH1);
                parent.drawStar((Graphics2D) imgG, sys.starType(), s60, boxW*4/5, (boxH-boxH1)/3);
                imgG.dispose();
            }
            else 
                planetImg = sys.planet().type().panoramaImage();
        }
        g.drawImage(planetImg, boxX, boxY+boxH1, boxW, boxH-boxH1, null);

        // draw header info
        int leftW = boxW * 2/5;
        String yearStr = displayYearOrTurn();
        g.setFont(narrowFont(40));
        int sw = g.getFontMetrics().stringWidth(yearStr);
        int x0 = boxX+((leftW-sw)/2);
        drawBorderedString(g, yearStr, 2, x0, boxY+boxH1-s20, SystemPanel.textShadowC, SystemPanel.orangeText);

        String sysName = player().sv.name(sysId);
        String scoutStr = text("MAIN_COLONIZE_TITLE", sysName);
        g.setColor(Color.black);
        g.setFont(narrowFont(14));
        drawString(g,scoutStr, boxX+leftW, boxY+s20);

        // calc width needed for yes/no buttons
        g.setFont(narrowFont(20));
        String yesStr = text("MAIN_COLONIZE_YES");
        String noStr = text("MAIN_COLONIZE_NO");
        int swYes = g.getFontMetrics().stringWidth(yesStr);
        int swNo = g.getFontMetrics().stringWidth(noStr);
        int buttonW = s20+Math.max(swYes, swNo);

        // print prompt string
        String promptStr = text("MAIN_COLONIZE_PROMPT");
        int promptFontSize = scaledFont(g, promptStr, boxW-leftW-buttonW-buttonW-s30, 24, 16);
        g.setFont(narrowFont(promptFontSize));
        int swPrompt = g.getFontMetrics().stringWidth(promptStr);
        drawShadowedString(g, promptStr, 4, boxX+leftW, boxY+s50, SystemPanel.textShadowC, Color.white);

        // draw yes/no buttons
        g.setFont(narrowFont(20));
        int buttonY = boxY + s30;
        int buttonH = s30;
        int x2 = boxX + leftW + swPrompt + s10;
        int x3 = x2 + buttonW + s10;
        // yes button
        parent.addNextTurnControl(yesButton);
        yesButton.parent(this);
        yesButton.setBounds(x2, buttonY, buttonW, buttonH);
        yesButton.draw(parent.map(), g);
        // no button
        parent.addNextTurnControl(noButton);
        noButton.parent(this);
        noButton.setBounds(x3, buttonY, buttonW, buttonH);
        noButton.draw(parent.map(), g);

        // draw planet info, from bottom up
        int x1 = boxX+s15;
        int y1 = boxY+boxH-s10;
        int lineH = s20;
        int desiredFont = 18;

        if (pl.sv.isUltraPoor(sysId)) {
            g.setColor(SystemPanel.redText);
            String s1 = text("MAIN_SCOUT_ULTRA_POOR_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 14);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x1, y1, Color.black, Color.white);
            y1 -= lineH;
        }
        else if (pl.sv.isPoor(sysId)) {
            g.setColor(SystemPanel.redText);
            String s1 = text("MAIN_SCOUT_POOR_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 14);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x1, y1, Color.black, Color.white);
            y1 -= lineH;
        }
        else if (pl.sv.isRich(sysId)) {
            g.setColor(SystemPanel.greenText);
            String s1 = text("MAIN_SCOUT_RICH_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 14);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x1, y1, Color.black, Color.white);
            y1 -= lineH;
        }
        else if (pl.sv.isUltraRich(sysId)) {
            g.setColor(SystemPanel.greenText);
            String s1 = text("MAIN_SCOUT_ULTRA_RICH_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 14);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x1, y1, Color.black, Color.white);
            y1 -= lineH;
        }

        if (pl.sv.isOrionArtifact(sysId)) {
            g.setColor(SystemPanel.greenText);
            String s1 = text("MAIN_SCOUT_ANCIENTS_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 14);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x1, y1, Color.black, Color.white);
            y1 -= lineH;
        }
        else if (pl.sv.isArtifact(sysId)) {
            g.setColor(SystemPanel.greenText);
            String s1 = text("MAIN_SCOUT_ARTIFACTS_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 14);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x1, y1, Color.black, Color.white);
            y1 -= lineH;
        }

        if (player().isEnvironmentHostile(sys)) {
            g.setColor(SystemPanel.redText);
            String s1 = text("MAIN_SCOUT_HOSTILE_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 14);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x1, y1, Color.black, Color.white);
            y1 -= lineH;
        }
        else if (player().isEnvironmentFertile(sys)) {
            g.setColor(SystemPanel.greenText);
            String s1 = text("MAIN_SCOUT_FERTILE_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 14);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x1, y1, Color.black, Color.white);
            y1 -= lineH;
        }
        else if (player().isEnvironmentGaia(sys)) {
            g.setColor(SystemPanel.greenText);
            String s1 = text("MAIN_SCOUT_GAIA_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 14);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x1, y1, Color.black, Color.white);
            y1 -= lineH;
        }

        // classification line
        if (sys.planet().type().isAsteroids()) {
            String s1 = text("MAIN_SCOUT_NO_PLANET");
            g.setFont(narrowFont(desiredFont+3));
            drawBorderedString(g, s1, 1, x1, y1, Color.black, Color.white);
            y1 -= lineH;
        }
        else {
            String s1 = text("MAIN_SCOUT_TYPE", text(sys.planet().type().key()), (int)sys.planet().maxSize());
            g.setFont(narrowFont(desiredFont+3));
            drawBorderedString(g, s1, 1, x1, y1, Color.black, Color.white);
            y1 -= lineH;
        }

        // planet name
        y1 -= scaled(5);
        g.setFont(narrowFont(40));
        drawBorderedString(g, sysName, 1, x1, y1, Color.darkGray, SystemPanel.orangeText);

		// draw plague warning if one
		if (hasPlague) {
			int xP = boxX + s10;
			int yP = boxY + boxH1 + s15;
			g.setColor(SystemPanel.redText);
			String s1 = text("MAIN_COLONIZE_PLAGUE");
			List<String> list = scaledNarrowWrappedLines(g, s1, boxW-s20, 3, desiredFont, 10);
			for (String str : list) {
				drawBorderedString(g, str, 1, xP, yP, Color.white, Color.red);
				yP += lineH;
			}
		}

        // planet flag
        parent.addNextTurnControl(flagButton);
        flagButton.init(this, g, sysId);
        flagButton.mapX(boxX+boxW-flagButton.width()+s10);
        flagButton.mapY(boxY+boxH-flagButton.height()+s10);
        flagButton.draw(parent.map(), g);
    }
    @Override
    public boolean handleKeyPress(KeyEvent e) {
		if (baseHandleKeyPress(e))
			return true;
        boolean shift = e.isShiftDown();
        switch(e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_N:
                colonizeNo();
                break;
            case KeyEvent.VK_Y:
                colonizeYes();
                break;
            case KeyEvent.VK_F:
                toggleFlagColor(shift);
                break;
			case KeyEvent.VK_L:
				if (e.isAltDown()) {
					debugReloadLabels(parent);
					parent.repaint();
					break;
				}
            default:
            	if (!shift) // BR to avoid noise when changing flag color
            		misClick();
                break;
        }
        return true;
    }
}
