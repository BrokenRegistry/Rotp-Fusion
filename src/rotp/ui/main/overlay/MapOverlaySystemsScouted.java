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
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import rotp.model.Sprite;
import rotp.model.empires.Empire;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.GameSession;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.main.MainUI;
import rotp.ui.main.SystemPanel;
import rotp.ui.sprites.RoundButtonSprite;
import rotp.ui.sprites.SystemFlagSprite;

public final class MapOverlaySystemsScouted implements IMapOverlay {
	private Color maskC  = new Color(40,40,40,160);
	private Area mask;
	private BufferedImage planetImg;
	private MainUI parent;
	private List<StarSystem> scoutSystems = new ArrayList<>();
	private List<StarSystem> allySystems = new ArrayList<>();
	private List<StarSystem> astronomerSystems = new ArrayList<>();
	private List<StarSystem> orderedSystems = new ArrayList<>();
	private int systemIndex = 0;
	private boolean drawSprites = false;
	private PreviousSystemButtonSprite prevSystemButton = new PreviousSystemButtonSprite();
	private NextSystemButtonSprite nextSystemButton = new NextSystemButtonSprite();
	private ContinueButtonSprite continueButton = new ContinueButtonSprite();
	private SystemFlagSprite flagButton = new SystemFlagSprite();

	public MapOverlaySystemsScouted(MainUI p)	{ parent = p; }
    public void releaseObjects() {
		scoutSystems.clear();
		allySystems.clear();
		astronomerSystems.clear();
		orderedSystems.clear();
    }
    public void init(HashMap<String, List<StarSystem>> newSystems) {
        parent.hideDisplayPanel();
        parent.map().setScale(20);
        systemIndex = 0;
        drawSprites = true;
        orderedSystems.clear();
        continueButton.reset();
        prevSystemButton.reset();
        nextSystemButton.reset();
        flagButton.reset();
        if (newSystems.isEmpty() 
        		|| options().isAutoPlay()) // BR: don't show scouting when auto-play
            advanceMap();
        else {
            // create an alphabetized list of systems
            scoutSystems = newSystems.get("Scouts");
            allySystems = newSystems.get("Allies");
            astronomerSystems = newSystems.get("Astronomers");
            orderedSystems.addAll(scoutSystems);
            orderedSystems.addAll(astronomerSystems);
            orderedSystems.addAll(allySystems);
            Collections.sort(orderedSystems, StarSystem.NAME);
            mapSelectIndex(0);
        }
    }
    private void mapSelectIndex(int i) {
        mask = null;
        planetImg = null;
        StarSystem nextSystem = orderedSystems.get(i);
        parent.map().recenterMapOn(nextSystem);
        parent.map().resetRangeAreas();
        parent.mapFocus(nextSystem);
        parent.clickedSprite(nextSystem);
        parent.repaint();
    }
    private void nextSystem() {
        systemIndex++;
        if (systemIndex >= orderedSystems.size())
            systemIndex = 0;
        mapSelectIndex(systemIndex);
    }
    private void previousSystem() {
        systemIndex--;
        if (systemIndex < 0)
            systemIndex = orderedSystems.size()-1;
        mapSelectIndex(systemIndex);
    }
    private void toggleFlagColor(boolean reverse) {
        StarSystem sys = orderedSystems.get(systemIndex);
        player().sv.toggleFlagColor(sys.id, reverse);
        parent.repaint();
    }
    @Override
    public void advanceMap() {
        if (drawSprites) {
            drawSprites = false;
            orderedSystems.clear();
            scoutSystems.clear();
            allySystems.clear();
            astronomerSystems.clear();

            if (GameSession.performingTurn())
                parent.resumeTurn();
            else
                parent.resumeOutsideTurn();
        }        
    }
    @Override
    public boolean drawSprites()   { return drawSprites; }
    @Override
    public boolean masksMouseOver(int x, int y)   { return true; }
    @Override
    public boolean hoveringOverSprite(Sprite o) { return false; }
    @Override
    public void paintOverMap(MainUI p, GalaxyMapPanel ui, Graphics2D g) {
        if (orderedSystems.isEmpty())
            return;
        StarSystem sys = orderedSystems.get(systemIndex);
        Empire pl = player();

        int w = ui.getWidth();
        int h = ui.getHeight();

        int bdrW = s7;
        int boxW = scaled(540);
        int boxH = scaled(240);
		int boxH1 = s68;
        int buttonPaneH = s40;

        int boxX = -s40+(w/2);
        int boxY = s40+(h-boxH)/2;

        // dimensions of the shade pane
        int x0 = boxX-bdrW;
        int y0 = boxY-bdrW;
        int w0 = boxW+bdrW+bdrW;
        int h0 = boxH+bdrW+bdrW+buttonPaneH;

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
		try { // for the case mask is set to null at the wrong time by a key action.
			g.fill(mask);
		}
		catch(Exception e) {
			return;
		}
        // draw border
        g.setColor(MainUI.paneShadeC);
        g.fillRect(x0, y0, w0, h0);

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
                p.drawStar((Graphics2D) imgG, sys.starType(), s60, boxW*4/5, (boxH-boxH1)/3);
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
        int x1 = boxX+((leftW-sw)/2);
        drawBorderedString(g, yearStr, 2, x1, boxY+boxH1-s20, SystemPanel.textShadowC, SystemPanel.orangeText);

        String scoutStr = text("MAIN_SCOUT_TITLE");
        int titleFontSize = scaledFont(g, scoutStr, boxW-leftW-s10, 24, 14);
        g.setFont(narrowFont(titleFontSize));
        drawShadowedString(g, scoutStr, 4, boxX+leftW, boxY+boxH1-s40, SystemPanel.textShadowC, Color.white);

        String detailStr = "";
        if (scoutSystems.contains(sys))
            detailStr = text("MAIN_SCOUT_SUBTITLE_1");
        else if (astronomerSystems.contains(sys))
            detailStr = text("MAIN_SCOUT_SUBTITLE_2");
        else if (allySystems.contains(sys))
            detailStr = text("MAIN_SCOUT_SUBTITLE_3");
            
        if (!detailStr.isEmpty()) {
            g.setColor(Color.darkGray);
            g.setFont(narrowFont(16));
            drawString(g,detailStr, boxX+leftW+s30, boxY+boxH1-s20);
        }

        // draw planet info, from bottom up
        int x2 = boxX+s15;
        int y2 = boxY+boxH-s10;
        int lineH = s20;
        int desiredFont = 18;

        if (pl.sv.isUltraPoor(sys.id)) {
            g.setColor(SystemPanel.redText);
            String s1 = text("MAIN_SCOUT_ULTRA_POOR_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 14);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x2, y2, Color.black, Color.white);
            y2 -= lineH;
        }
        else if (pl.sv.isPoor(sys.id)) {
            g.setColor(SystemPanel.redText);
            String s1 = text("MAIN_SCOUT_POOR_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 14);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x2, y2, Color.black, Color.white);
            y2 -= lineH;
        }
        else if (pl.sv.isRich(sys.id)) {
            g.setColor(SystemPanel.greenText);
            String s1 = text("MAIN_SCOUT_RICH_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 14);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x2, y2, Color.black, Color.white);
            y2 -= lineH;
        }
        else if (pl.sv.isUltraRich(sys.id)) {
            g.setColor(SystemPanel.greenText);
            String s1 = text("MAIN_SCOUT_ULTRA_RICH_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 14);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x2, y2, Color.black, Color.white);
            y2 -= lineH;
        }

        if (pl.sv.isOrionArtifact(sys.id)) {
            g.setColor(SystemPanel.greenText);
            String s1 = text("MAIN_SCOUT_ANCIENTS_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 14);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x2, y2, Color.black, Color.white);
            y2 -= lineH;
        }
        else if (pl.sv.isArtifact(sys.id)) {
            g.setColor(SystemPanel.greenText);
            String s1 = text("MAIN_SCOUT_ARTIFACTS_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 14);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x2, y2, Color.black, Color.white);
            y2 -= lineH;
        }

        if (pl.isEnvironmentHostile(sys)) {
            g.setColor(SystemPanel.redText);
            String s1 = text("MAIN_SCOUT_HOSTILE_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 14);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x2, y2, Color.black, Color.white);
            y2 -= lineH;
        }
        else if (pl.isEnvironmentFertile(sys)) {
            g.setColor(SystemPanel.greenText);
            String s1 = text("MAIN_SCOUT_FERTILE_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 14);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x2, y2, Color.black, Color.white);
            y2 -= lineH;
        }
        else if (pl.isEnvironmentGaia(sys)) {
            g.setColor(SystemPanel.greenText);
            String s1 = text("MAIN_SCOUT_GAIA_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 14);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x2, y2, Color.black, Color.white);
            y2 -= lineH;
        }

        // classification line
        if (sys.planet().type().isAsteroids()) {
            String s1 = text("MAIN_SCOUT_NO_PLANET");
            g.setFont(narrowFont(desiredFont+3));
            drawBorderedString(g, s1, 1, x2, y2, Color.black, Color.white);
            y2 -= lineH;
        }
        else {
            String s1 = text("MAIN_SCOUT_TYPE", text(sys.planet().type().key()), (int)sys.planet().maxSize());
            g.setFont(narrowFont(desiredFont+3));
            drawBorderedString(g, s1, 1, x2, y2, Color.black, Color.white);
            y2 -= lineH;
        }

        if (pl.sv.isColonized(sys.id)) {
            g.setFont(narrowFont(24));
            String s1 = pl.sv.descriptiveName(sys.id);
            int fontSize = scaledFont(g, s1, boxW-x2-s10, 24, 18);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x2, y2, Color.black, Color.white);
            y2 -= lineH;
			y2 -= s5;
        }
        // planet name
        String sysName = pl.sv.knownName(sys.id);
		y2 -= s5;
        g.setColor(SystemPanel.orangeText);
        g.setFont(narrowFont(40));
        drawBorderedString(g, sysName, 1, x2, y2, Color.darkGray, SystemPanel.orangeText);

        // planet flag
        p.addNextTurnControl(flagButton);
		flagButton.init(this, g, sys.id);
        flagButton.mapX(boxX+boxW-flagButton.width()+s10);
        flagButton.mapY(boxY+boxH-flagButton.height()+s10);
        flagButton.draw(p.map(), g);

        // init and draw continue button sprite
        p.addNextTurnControl(continueButton);
        continueButton.init(this, g);
        continueButton.mapX(x0+w0-continueButton.width()-s10);
        continueButton.mapY(y0+h0-continueButton.height()-s10);
        if (orderedSystems.size() < 2)
            continueButton.setSelectionBounds(x0,y0,w0,h0);
        continueButton.draw(p.map(), g);

        if (orderedSystems.size() > 1) {
            p.addNextTurnControl(prevSystemButton);
            prevSystemButton.init(this,g);
            prevSystemButton.mapX(x0+s10);
            prevSystemButton.mapY(continueButton.mapY());
            prevSystemButton.draw(p.map(), g);

            // draw notice number
            String notice2Str = text("MAIN_ALLOCATE_BRIEF_NUMBER", str(systemIndex+1), str(orderedSystems.size()));
            g.setFont(narrowFont(16));
            int sw4 = g.getFontMetrics().stringWidth(notice2Str);
            int x4b = prevSystemButton.mapX()+prevSystemButton.width()+s10;
            int y4b = prevSystemButton.mapY()+prevSystemButton.height()-s10;
            g.setColor(SystemPanel.blackText);
            drawString(g,notice2Str, x4b, y4b);

            p.addNextTurnControl(nextSystemButton);
            nextSystemButton.init(this,g);
            nextSystemButton.mapX(x4b+sw4+s10);
            nextSystemButton.mapY(continueButton.mapY());
            nextSystemButton.draw(p.map(), g);
        }
    }
    @Override
    public boolean handleKeyPress(KeyEvent e) {
		if (baseHandleKeyPress(e))
			return true;
        boolean shift = e.isShiftDown();
        switch(e.getKeyCode()) {
            case KeyEvent.VK_N:
                nextSystem();
                break;
            case KeyEvent.VK_P:
                previousSystem();
                break;
            case KeyEvent.VK_C:
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_ESCAPE:
                advanceMap();
                break;
            case KeyEvent.VK_F:
                toggleFlagColor(shift);
                break;
            case KeyEvent.VK_L:
            	if (e.isAltDown()) {
            		debugReloadLabels(parent);
            		break;
            	}
            	misClick();
            	break;
            default:
            	if (!shift) // BR to avoid noise when changing flag color
            		misClick();
                break;
        }
        return true;
    }
    private final class PreviousSystemButtonSprite extends RoundButtonSprite {
		private final Color edgeC	 = new Color(59,59,59);
		private final Color midC	 = new Color(93,93,93);

		private void init(MapOverlaySystemsScouted p, Graphics2D g)	{ init(p, g, s20, s30, "MAIN_SCOUT_PREV_SYSTEM", 2); }

		@Override protected Color[] colors()	{ return new Color[] {edgeC, midC, edgeC}; }
		@Override public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
			((MapOverlaySystemsScouted)parent).previousSystem();
		}
	}
	private final class NextSystemButtonSprite extends RoundButtonSprite {
		private final Color edgeC	= new Color(44,59,30);
		private final Color midC	= new Color(70,93,48);

		private void init(MapOverlaySystemsScouted p, Graphics2D g)	{ init(p, g, s20, s30, "MAIN_SCOUT_NEXT_SYSTEM", 2); }

		@Override protected Color[] colors()	{ return new Color[] {edgeC, midC, edgeC}; }
		@Override public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
			((MapOverlaySystemsScouted)parent).nextSystem();
		}
	}
	private final class ContinueButtonSprite extends RoundButtonSprite {
		private final Color edgeC	= new Color(59,59,59);
		private final Color midC	= new Color(93,93,93);
	
		private void init(MapOverlaySystemsScouted p, Graphics2D g)	{ init(p, g, s60, s30, "MAIN_SCOUT_CLOSE", 2); }

		@Override protected Color[] colors()	{ return new Color[] {edgeC, midC, edgeC}; }
		@Override public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
			((MapOverlaySystemsScouted) parent).advanceMap();
			((MapOverlaySystemsScouted) parent).parent.map().resetRangeAreas();
		}
	}
}
