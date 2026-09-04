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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import rotp.model.Sprite;
import rotp.model.ai.player.ShipCaptainAdvisor;
import rotp.model.combat.CombatStack;
import rotp.model.combat.ShipCombatManager;
import rotp.model.empires.DiplomaticEmbassy;
import rotp.model.empires.Empire;
import rotp.model.empires.EmpireStatus;
import rotp.model.empires.EmpireView;
import rotp.model.empires.ShipView;
import rotp.model.empires.SpyNetwork;
import rotp.model.galaxy.SpaceMonster;
import rotp.model.galaxy.StarSystem;
import rotp.ui.combat.ShipBattleUI;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.main.MainUI;
import rotp.ui.main.SystemPanel;
import rotp.ui.races.RacesMilitaryUI;
import rotp.ui.races.RacesUI;
import rotp.ui.sprites.MapSprite;
import rotp.ui.sprites.RoundButtonSprite;
import rotp.ui.sprites.SystemFlagSprite;
import rotp.ui.util.StringList;
import rotp.ui.vipconsole.IVIPListener;
import rotp.ui.vipconsole.VIPConsole;
import rotp.util.LanguageManager;

public final class MapOverlayShipCombatPrompt implements IMapOverlay, IVIPListener {
//    static final Color destroyedTextC = new Color(255,32,32,192);
//    static final Color destroyedMaskC = new Color(0,0,0,160);
	private Color maskC		= new Color(40,40,40,160);
	private Color popupBgC	= new Color(40,40,40);
	private Color popupTxtC	= SystemPanel.darkOrangeText;
	private Area mask;
	private BufferedImage planetImg;
	private MainUI parent;
	private int sysId;
	private int boxX, boxY, boxW, boxH;
	private boolean drawSprites = false;
	private int showInfo = 0;
	private ShipCombatManager mgr;
	private AutoResolveBattleSprite resolveButton = new AutoResolveBattleSprite();
	private SmartResolveBattleSprite smartResolveButton = new SmartResolveBattleSprite();
	private RetreatAllBattleSprite retreatButton = new RetreatAllBattleSprite();
	private EnterBattleSprite battleButton = new EnterBattleSprite();
	private SystemFlagSprite flagButton = new SystemFlagSprite();
	private StartWarBattleSprite warButton = new StartWarBattleSprite();
	private ShipCaptainAdvisor advisor;
	private Empire pl;

    public MapOverlayShipCombatPrompt(MainUI p) {
        parent = p;
    }
    public void releaseObjects() { }
    public void init(ShipCombatManager m) {
    	planetImg = null;
        mgr = m;
        sysId = mgr.system().id;
		pl = player();
        advisor = new ShipCaptainAdvisor(pl);
        flagButton.reset();
        StarSystem sys = galaxy().system(sysId);
        showInfo = 0;
        drawSprites = true;
        parent.hideDisplayPanel();
        parent.map().setScale(20);
        parent.map().recenterMapOn(sys);
        parent.mapFocus(sys);
        parent.clickedSprite(sys);
        parent.repaint();
        initConsoleSelection("Ship Combat", false);
    }
	private void startCombat(int combatFlag)		{
        drawSprites = false;
        parent.clearOverlay();
        parent.repaintAllImmediately();
		mgr.playerSelection(combatFlag);
		session().resumeNextTurnProcessing();
    }
	private void toggleFlagColor(boolean reverse)	{
        player().sv.toggleFlagColor(sysId, reverse);
        parent.repaint();
    }
	private boolean startWar()	{
		Empire alien = mgr.results().aiEmpire();
		if (alien == null)
			return false;
		Empire player = mgr.player();
		if (options().canStartWar(player, alien)) {
			DiplomaticEmbassy embassy = player.viewForEmpire(alien).embassy();
			embassy.declareWar();
			advisor.performRetreatAnalysis();
			return embassy.treaty().isWar();
		}
		return false;
	}
    private void drawFleetsInfo(Graphics2D g) {
		if (showInfo == 0)
			return;
		Empire player = player();
		RacesMilitaryUI milPane = RacesUI.instance.militaryPanel;
		
		int ws = parent.getWidth();
		int w = scaled(947);
		int h = s80;
		int dh = h+s2;
		int x = (ws-w)/4;
		int yi = s10;
		int y = scaled(166);

    	if (showInfo == 1) {
    		if (mgr.results().isMonsterAttack()) {
    			for(CombatStack st : mgr.activeStacks()) {
                    if(st.isMonster()) {
                    	Empire emp = st.empire();
                    	ShipView view = emp.shipViewFor(st.design());
                    	h = max(h, s20 * st.design().maxSpecials());
                    	milPane.drawShipDesign(g, view, st.num, x, y, w, h, MainUI.paneBackground);
                        y += dh;
                        milPane.paintMonsterData(g, emp, x, yi, MainUI.paneBackground);
                    }
				}
				return;
			}
			else {
            	Empire alien  = mgr.results().attacker();
            	if (alien == player)
            		alien = mgr.results().defender();
            	SpyNetwork spies = player.viewForEmpire(alien).spies();
        		for(CombatStack st : mgr.activeStacks()) {
                    if(st.isShip()) {
                        if(st.empire() == alien) {
                        	ShipView view = spies.shipViewFor(st.design());
							if (view != null) {
								milPane.drawShipDesign(g, view, st.num, x, y, w, h, MainUI.paneBackground);
								y += dh;
							}
                        }
                    }
				}
				milPane.paintAlienData(g, alien, x, yi, MainUI.paneBackground);
			}
		}
		else if (showInfo == 2) {
			for(CombatStack st : mgr.activeStacks()) {
                if(st.isShip()) {
                    if(st.empire() == player) {
                    	ShipView view = player.shipViewFor(st.design());
                    	milPane.drawShipDesign(g, view, st.num, x, y, w, h, MainUI.paneBackground);
                        y += dh;
                    }
                }
    		}
    		milPane.paintPlayerData(g, x, yi, MainUI.paneBackground);
    	}
    }
    @Override
    public boolean drawSprites()   { return drawSprites; }
    @Override
    public boolean masksMouseOver(int x, int y)   { return true; }
    @Override
    public boolean hoveringOverSprite(Sprite o) { return false; }
    @Override
    public void advanceMap() {
        startCombat(ShipBattleUI.ENTER_COMBAT);
    }
    @Override
    public void paintOverMap(MainUI parent, GalaxyMapPanel ui, Graphics2D g) {
        StarSystem sys = galaxy().system(sysId);
        Empire pl = player();

        int w = ui.getWidth();
        int h = ui.getHeight();

        int bdrW = s7;
        boxW = scaled(540);
        int boxH1 = s68;
        int boxH2 = scaled(172);
		int buttonPaneH = s35;
        boxH = boxH1 + boxH2 + buttonPaneH;

        boxX = -s40+(w/2);
        boxY = -s40+(h-boxH)/2;

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

        boolean scouted = player().sv.isScouted(sys.id);
        // draw planet image
        if (planetImg == null) {
            if (!scouted || sys.planet().type().isAsteroids()) {
                planetImg = newBufferedImage(boxW, boxH2);
                Graphics imgG = planetImg.createGraphics();
                imgG.setColor(Color.black);
                imgG.fillRect(0, 0, boxW, boxH2);
                drawBackgroundStars(imgG, boxW, boxH2);
                parent.drawStar((Graphics2D) imgG, sys.starType(), s60, boxW*4/5, boxH2/3);
                imgG.dispose();
            }
            else {
                planetImg = sys.planet().type().panoramaImage();
                int planetW = planetImg.getWidth();
                int planetH = planetImg.getHeight();
                Empire emp = sys.empire();
                if (emp != null) {
                    BufferedImage fortImg = emp.fortress(sys.colony().fortressNum());
                    int fortW = scaled(fortImg.getWidth());
                    int fortH = scaled(fortImg.getHeight());
                    int fortScaleW = fortW*planetW/w;
                    int fortScaleH = fortH*planetW/w;
                    int fortX = planetImg.getWidth()-fortScaleW;
                    int fortY = planetImg.getHeight()-fortScaleH+(planetH/5);
                	Graphics imgG = planetImg.createGraphics();
                    imgG.drawImage(fortImg, fortX, fortY, fortX+fortScaleW, fortY+fortScaleH, 0, 0, fortImg.getWidth(), fortImg.getHeight(), null);
                    imgG.dispose();
                }
            }
        }
        g.drawImage(planetImg, boxX, boxY+boxH1, boxW, boxH2, null);

        // draw header info
        int leftW = boxW * 2/5;
        String yearStr = displayYearOrTurn();
        g.setFont(narrowFont(40));
        int sw = g.getFontMetrics().stringWidth(yearStr);
        int x0 = boxX+((leftW-sw)/2);
        drawBorderedString(g, yearStr, 2, x0, boxY+boxH1-s20, SystemPanel.textShadowC, SystemPanel.orangeText);

        Empire aiEmpire = mgr.results().aiEmpire();
        String titleStr;
        if (aiEmpire == null)
            titleStr = text("SHIP_COMBAT_TITLE_MONSTER_DESC", mgr.results().aiRaceName());
        else {
            titleStr = text("SHIP_COMBAT_TITLE_DESC");
            titleStr = aiEmpire.replaceTokens(titleStr, "alien");
        }
        g.setColor(Color.black);
        int titleFontSize = scaledFont(g, titleStr, boxW-leftW, 20, 14);
        g.setFont(narrowFont(titleFontSize));
        drawString(g,titleStr, boxX+leftW, boxY+s20);

        // print prompt string
        String sysName = player().sv.name(sys.id);
        String promptStr = scouted ? text("SHIP_COMBAT_TITLE_SYSTEM", sysName) : text("SHIP_COMBAT_TITLE_UNSCOUTED");
        int promptFontSize = scaledFont(g, promptStr, boxW-leftW-s30, 24, 20);
        g.setFont(narrowFont(promptFontSize));
        drawShadowedString(g, promptStr, 4, boxX+leftW, boxY+s50, SystemPanel.textShadowC, Color.white);

        // init and draw battle and resolve buttons
        parent.addNextTurnControl(battleButton);
        battleButton.init(this, g);
        battleButton.mapX(boxX+boxW-battleButton.width());
        battleButton.mapY(boxY+boxH-battleButton.height());
        battleButton.draw(parent.map(), g);

        parent.addNextTurnControl(resolveButton);
        resolveButton.init(this, g);
        resolveButton.mapX(boxX);
        resolveButton.mapY(battleButton.mapY());
        resolveButton.draw(parent.map(), g);

        parent.addNextTurnControl(smartResolveButton);
        smartResolveButton.init(this, g);
       	smartResolveButton.mapX(resolveButton.mapX()+resolveButton.width()+s7);
        smartResolveButton.mapY(battleButton.mapY());
        smartResolveButton.draw(parent.map(), g);

        if(mgr.playerCanRetreat()) {
            parent.addNextTurnControl(retreatButton);
            retreatButton.init(this, g);
            retreatButton.mapX(smartResolveButton.mapX()+smartResolveButton.width()+s7);
            retreatButton.mapY(battleButton.mapY());
            retreatButton.draw(parent.map(), g);
        }

        // draw planet info, from bottom up
        int x1 = boxX+s15;
        int x2 = boxX+boxW/2+s15;
        int y1 = boxY+boxH1+s15;
        int y2 = y1;
        int lineH = s20;
        int desiredFont = 18;

        HashMap<String, Integer> mySizes = new HashMap<>();
        HashMap<String, Integer> aiSizes = new HashMap<>();
        for(CombatStack st : mgr.activeStacks())
        {
            int putVal = st.num;
            if(st.isShip())
            {
                if(st.empire() == pl)
                {
                    if(mySizes.containsKey(st.design().sizeDesc()))
                        putVal += mySizes.get(st.design().sizeDesc());
                    mySizes.put(st.design().sizeDesc(), putVal);
                }
                else
                {
                    if(aiSizes.containsKey(st.design().sizeDesc()))
                        putVal += aiSizes.get(st.design().sizeDesc());
                    aiSizes.put(st.design().sizeDesc(), putVal);
                }
            }
            else if(st.isColony() && st.isArmed())
            {
                if(st.empire() == pl)
                    mySizes.put(text("MAIN_COLONY_BASES"), putVal);
                else
                    aiSizes.put(text("MAIN_COLONY_BASES"), putVal);
            }
        }
        for(Entry<String, Integer> entry : mySizes.entrySet())
        {
            drawBorderedString(g, entry.getValue() + " " + entry.getKey(), 1, x1, y1, Color.black, pl.color());
            y1 += lineH;
        }
        for(Entry<String, Integer> entry : aiSizes.entrySet())
        {
        	Color txtColor = aiEmpire==null? Color.RED : aiEmpire.color();
            drawBorderedString(g, entry.getValue() + " " + entry.getKey(), 1, x2, y2, Color.black, txtColor);
            y2 += lineH;
        }

        // if unscouted, no planet info
        if (!scouted) {
        	drawFleetsInfo(g);
        	return;
        }

        x1 = boxX+s15;
        y1 = boxY+boxH1+boxH2-s10;

        if (pl.sv.isUltraPoor(sys.id)) {
            g.setColor(SystemPanel.redText);
            String s1 = text("MAIN_SCOUT_ULTRA_POOR_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 15);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x1, y1, Color.black, Color.white);
            y1 -= lineH;
        }
        else if (pl.sv.isPoor(sys.id)) {
            g.setColor(SystemPanel.redText);
            String s1 = text("MAIN_SCOUT_POOR_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 15);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x1, y1, Color.black, Color.white);
            y1 -= lineH;
        }
        else if (pl.sv.isRich(sys.id)) {
            g.setColor(SystemPanel.greenText);
            String s1 = text("MAIN_SCOUT_RICH_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 15);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x1, y1, Color.black, Color.white);
            y1 -= lineH;
        }
        else if (pl.sv.isUltraRich(sys.id)) {
            g.setColor(SystemPanel.greenText);
            String s1 = text("MAIN_SCOUT_ULTRA_RICH_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 15);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x1, y1, Color.black, Color.white);
            y1 -= lineH;
        }

        if (pl.sv.isOrionArtifact(sys.id)) {
            g.setColor(SystemPanel.greenText);
            String s1 = text("MAIN_SCOUT_ANCIENTS_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 15);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x1, y1, Color.black, Color.white);
            y1 -= lineH;
        }
        else if (pl.sv.isArtifact(sys.id)) {
            g.setColor(SystemPanel.greenText);
            String s1 = text("MAIN_SCOUT_ARTIFACTS_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 15);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x1, y1, Color.black, Color.white);
            y1 -= lineH;
        }

        if (pl.isEnvironmentHostile(sys)) {
            g.setColor(SystemPanel.redText);
            String s1 = text("MAIN_SCOUT_HOSTILE_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 15);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x1, y1, Color.black, Color.white);
            y1 -= lineH;
        }
        else if (pl.isEnvironmentFertile(sys)) {
            g.setColor(SystemPanel.greenText);
            String s1 = text("MAIN_SCOUT_FERTILE_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 15);
            g.setFont(narrowFont(fontSize));
            drawBorderedString(g, s1, 1, x1, y1, Color.black, Color.white);
            y1 -= lineH;
        }
        else if (pl.isEnvironmentGaia(sys)) {
            g.setColor(SystemPanel.greenText);
            String s1 = text("MAIN_SCOUT_GAIA_DESC");
            int fontSize = scaledFont(g, s1, boxW-s25, desiredFont, 15);
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

        // planet flag
        parent.addNextTurnControl(flagButton);
        flagButton.init(this, g, sysId);
        flagButton.mapX(boxX+boxW-flagButton.width()+s10);
        flagButton.mapY(boxY+boxH-buttonPaneH-flagButton.height()+s10);
        flagButton.draw(parent.map(), g);

        // Empire flag
        int margin = s4;
    	parent.addNextTurnControl(warButton);
        warButton.init(this, g);
        warButton.mapX(boxX+boxW - warButton.width()-margin);
        warButton.mapY(boxY + margin);            	           	
        warButton.draw(parent.map(), g);

        drawFleetsInfo(g);
    }
    @Override
    public boolean handleKeyPress(KeyEvent e) {
		if (baseHandleKeyPress(e))
			return true;
        boolean shift = e.isShiftDown();
        Empire aiEmpire = mgr.results().aiEmpire();
        switch(e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
            	if (showInfo!=0) { // break info before entering battle
            		showInfo = 0;
            		parent.repaint();
            		break;
            	}
            case KeyEvent.VK_E:
				Empire attacker = mgr.results().attacker();
				if (attacker != null && attacker.isPlayer())
					if (e.isShiftDown())
						mgr.dontTargetHarmlessColony(true);
					else if (e.isControlDown())
						mgr.dontTargetHarmlessColony(false);
                startCombat(ShipBattleUI.ENTER_COMBAT);
                break;
            case KeyEvent.VK_A:
				if (aiEmpire != null) {
					if (mgr.results().attacker().isPlayer())
						if (e.isShiftDown())
							mgr.dontTargetHarmlessColony(true);
						else if (e.isControlDown())
							mgr.dontTargetHarmlessColony(false);
					startCombat(ShipBattleUI.AUTO_RESOLVE);
				}
                break;
            case KeyEvent.VK_S:
				if (aiEmpire != null)
					if (advisor.playerShouldRetreat())
						startCombat(ShipBattleUI.RETREAT_ALL); // Immediate retreat
					else {
						if (mgr.results().attacker().isPlayer())
							if (e.isShiftDown())
								mgr.dontTargetHarmlessColony(true);
							else if (e.isControlDown())
								mgr.dontTargetHarmlessColony(false);
						startCombat(ShipBattleUI.SMART_RESOLVE);
					}
                break;
            case KeyEvent.VK_R:
                if (aiEmpire != null)
                    startCombat(ShipBattleUI.RETREAT_ALL);
                break;
            case KeyEvent.VK_F:
                toggleFlagColor(shift);
                break;
			case KeyEvent.VK_W:
				if (startWar()) {
					buttonClick();
					parent.repaint();
				}
				else
					misClick();
				break;
            case KeyEvent.VK_H:
            case KeyEvent.VK_I:
            	switch (showInfo) {
	            	case 0: showInfo = 1; break;
	            	case 1: showInfo = 2; break;
	            	case 2: showInfo = 0; break;
            	}
            	buttonClick();
            	parent.repaint();
                break;
            case KeyEvent.VK_L:
            	if (e.isAltDown()) {
            		debugReloadLabels(parent);
            		break;
            	}
            	misClick();
            	break;
            default:
            	if (!shift) // BR: to avoid noise when changing flag color
            		misClick();
                break;
        }
        return true;
    }
	private final class AutoResolveBattleSprite extends RoundButtonSprite {
		private final Color edgeC	= new Color(59,59,59);
		private final Color midC	= new Color(93,93,93);

		@Override protected Font font()	{
			String language = LanguageManager.current().selectedLanguageName();
			if (language.equals("Português"))
				return narrowFont(16);
			return narrowFont(18);
		}
		private void init(MapOverlayShipCombatPrompt p, Graphics2D g) {
			parent = p;
			box.setLabelKey("SHIP_COMBAT_AUTO_RESOLVE");
			String language = LanguageManager.current().selectedLanguageName();
			if (language.equals("Português"))
				box.setSize(s20 + g.getFontMetrics(font()).stringWidth(label()), s30);
			else
				box.setSize(s40 + g.getFontMetrics(font()).stringWidth(label()), s30);
			box.setForcedLocation(2);
		}
		@Override protected Color[] colors()	{ return new Color[] {edgeC, midC, edgeC}; }
		@Override public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
			Empire attacker = mgr.results().attacker();
			if (attacker != null && attacker.isPlayer())
				if (e.isShiftDown())
					mgr.dontTargetHarmlessColony(true);
				else if (e.isControlDown())
					mgr.dontTargetHarmlessColony(false);
			startCombat(ShipBattleUI.AUTO_RESOLVE);
		};
	}
	private final class SmartResolveBattleSprite extends RoundButtonSprite {
		private final Color edgeC	= new Color(59,59,59);
		private final Color midC	= new Color(93,93,93);

		private void init(MapOverlayShipCombatPrompt p, Graphics2D g) {
			parent = p;
			box.setLabelKey("SHIP_COMBAT_SMART_RESOLVE");
			String language = LanguageManager.current().selectedLanguageName();
			if (language.equals("Português"))
				box.setSize(s20 + g.getFontMetrics(font()).stringWidth(label()), s30);
			else
				box.setSize(s40 + g.getFontMetrics(font()).stringWidth(label()), s30);
			box.setForcedLocation(2);
		}
		@Override public void draw(GalaxyMapPanel map, Graphics2D g) {
			if (!parent.drawSprites())
				return;
			directDraw(map, g);
			if (hovering && advisor.playerShouldRetreat())
				drawComments(map, g);
		}
		@Override protected Font font()			{
			String language = LanguageManager.current().selectedLanguageName();
			if (language.equals("Português"))
				return narrowFont(16);
			return narrowFont(18);
		}
		@Override protected Color[] colors()	{ return new Color[] {edgeC, midC, edgeC}; }
		@Override public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
			if (advisor.playerShouldRetreat())
				startCombat(ShipBattleUI.RETREAT_ALL); // Immediate retreat
			else {
				Empire attacker = mgr.results().attacker();
				if (attacker != null && attacker.isPlayer())
					if (e.isShiftDown())
						mgr.dontTargetHarmlessColony(true);
					else if (e.isControlDown())
						mgr.dontTargetHarmlessColony(false);
				startCombat(ShipBattleUI.SMART_RESOLVE);
			}
		};
		private void drawComments (GalaxyMapPanel map, Graphics2D g) {
			// advisor.performRetreatAnalysis();
			StringList list = new StringList();
			if (advisor.notAnEnemyColony())
				list.add(pl.text("SHIP_COMBAT_NOT_ENEMY_COLONY"));
			if (advisor.isCivilFleet())
				list.add(pl.text("SHIP_COMBAT_DEFENSELESS_FLEET"));
			else if (advisor.facingOverwhelmingForce())
				list.add(pl.text("SHIP_COMBAT_FLEET_WILL_LOOSE"));
			else if (advisor.fleetWantToFight() && advisor.playerCanDeclareWar()) {
				list.add(pl.text("SHIP_COMBAT_WE_CAN_WIN"));
				list.add(pl.text("SHIP_COMBAT_HOW_TO_START_WAR"));
			}

			int lineH = s20;
			int boxX = box.x - s50;
			int boxY = box.ye() + s6;
			int boxW = box.width + s100;
			int boxH = (list.size() + 1) * lineH + s10;
			g.setPaint(popupTxtC);
			g.fillRoundRect(boxX-s3, boxY-s3, boxW+s3+s3, boxH+s3+s3, s10, s10);
			g.setColor(popupBgC);
			g.fillRoundRect(boxX, boxY, boxW, boxH, cnr, cnr);

			g.setPaint(popupTxtC);
			g.setFont(narrowFont(18));
			String str = pl.text("SHIP_COMBAT_SMART_RETREAT");
			int sw = g.getFontMetrics().stringWidth(str);
			int txtX = boxX + (boxW - sw) / 2;
			int txtY = boxY + lineH;
			drawString(g, str, txtX, txtY);

			g.setFont(narrowFont(14));
			for (String s : list) {
				sw = g.getFontMetrics().stringWidth(s);
				txtX = boxX + (boxW - sw) / 2;
				txtY += lineH;
				drawString(g, s, txtX, txtY);
			}
		}
	}
	private final class RetreatAllBattleSprite extends RoundButtonSprite {
		private final Color edgeC	= new Color(59,59,59);
		private final Color midC	= new Color(93,93,93);

		public void init(MapOverlayShipCombatPrompt p, Graphics2D g) {
			parent = p;
			box.setLabelKey("SHIP_COMBAT_RETREAT_ALL");
			String language = LanguageManager.current().selectedLanguageName();
			if (language.equals("Português"))
				box.setSize(s20 + g.getFontMetrics(font()).stringWidth(label()), s30);
			else
				box.setSize(s40 + g.getFontMetrics(font()).stringWidth(label()), s30);
			box.setForcedLocation(2);
		}
		@Override protected Font font()	{
			String language = LanguageManager.current().selectedLanguageName();
			if (language.equals("Português"))
				return narrowFont(16);
			return narrowFont(18);
		}
		@Override protected Color[] colors()	{ return new Color[] {edgeC, midC, edgeC}; }
		@Override public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
			startCombat(ShipBattleUI.RETREAT_ALL);
		};
	}
	private final class StartWarBattleSprite extends MapSprite {
		private MapOverlayShipCombatPrompt parent;

		private String label()		{ return text("SHIP_COMBAT_START_WAR"); }
		private Font font()			{ return narrowFont(16); }

		private void init(MapOverlayShipCombatPrompt p, Graphics2D g)  {
			parent = p;
			box.setLabelKey("SHIP_COMBAT_START_WAR");
			String language = LanguageManager.current().selectedLanguageName();
			if (language.equals("Português"))
				box.setSize(s20 + g.getFontMetrics(font()).stringWidth(label()), s60);
			else
				box.setSize(s40 + g.getFontMetrics(font()).stringWidth(label()), s60);
			box.setOffset(box.width-s40, 0);
			box.setForcedLocation(6);
		}
        @Override
        public void draw(GalaxyMapPanel map, Graphics2D g) {
            if (!parent.drawSprites())
                return;
    		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
    		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        	Empire player = player();
        	Empire alien  = mgr.results().attacker();
        	if (alien == player)
        		alien = mgr.results().defender();

        	Image flag;
            Image flagWar;
            boolean anyWar;
            EmpireView view = null;
            DiplomaticEmbassy embassy = null;
			int flagH = box.height;
			int flagW = flagH;
			int flagX = box.xe() - flagW;

            if (mgr.results().isMonsterAttack()) {
            	SpaceMonster monster = mgr.results().monster();
            	flagWar = monster.image();
        		flag    = flagWar;
        		anyWar  = true;
            	int sx2 = flagWar.getWidth(null);
            	int sy2 = flagWar.getHeight(null);
            	flagW = flagH * sx2 / sy2;
				int y = box.y + s15;
        		g.drawImage(flagWar, flagX, y, flagX+flagW, y+flagH, 0, 0, sx2, sy2, null);
        		return;
            }
            else if (alien == null) { // Should never happen...
				flagWar = player.flagWar();
				flag    = player.flagPact();
				anyWar  = true;
        	}
        	else {
        		view    = player.viewForEmpire(alien);
        		embassy = view.embassy();
        		flag    = view.flag();
        		flagWar = alien.flagWar();
        		anyWar  = embassy.anyWar();
        	}
            if (hovering) {
            	int sx2 = flagWar.getWidth(null);
            	int sy2 = flagWar.getHeight(null);
            	flagW = flagH * sx2 / sy2;
				g.drawImage(flagWar, flagX, box.y, flagX+flagW, box.y+flagH, 0, 0, sx2, sy2, null);

            	// draw pop-up
            	g.setFont(font());
            	int lineH = s18;
            	int bd = s3;
            	int cnr = s10;
            	int w3 = s200;
            	int h3 = s30;
            	int x3 = boxX + boxW - w3;
				int y3 = box.y - h3 - s15;
            	int ws = w3 - s20;
            	int xs = x3+((w3-ws)/2);
            	int ys = y3 + h3 - s9;
            	if (anyWar) {
            		if (embassy != null) { // Thus not a monster
            			g.setColor(MainUI.paneShadeC);
                		g.fillRoundRect(x3-bd, y3-bd, w3+bd+bd, h3+bd+bd, cnr, cnr);
            			g.setColor(MainUI.textBoxShade0);
                		g.fillRoundRect(x3, y3, w3, h3, cnr, cnr);
                		String str = embassy.treatyStatus();
                		int sw = g.getFontMetrics().stringWidth(str);
                		int xv = x3+((w3-sw)/2);
                		g.setColor(Color.RED);
                		g.drawString(str, xv, ys);
            		}
            	}
            	else if (embassy != null && view != null && alien != null) { // should never be...
            		int xe = x3 + w3 - s10;
            		h3 = scaled(130);
					y3 = box.y - h3 - s15;
            		ys = y3 + lineH;
            		g.setColor(MainUI.paneShadeC);
            		g.fillRoundRect(x3-bd, y3-bd, w3+bd+bd, h3+bd+bd, cnr, cnr);
            		g.setColor(MainUI.textBoxShade0);
            		g.fillRoundRect(x3, y3, w3, h3, cnr, cnr);
            		String str = embassy.treatyStatus();
            		g.setColor(SystemPanel.blackText);
            		g.drawString(str, xs, ys);

            		ys += lineH;
            		str = text("RACES_DIPLOMACY_TRADE_TREATY");
            		g.drawString(str, xs, ys);
            		str = text("RACES_DIPLOMACY_TRADE_AMT", view.trade().level());
            		int sw = g.getFontMetrics().stringWidth(str);
            		int xv = xe - sw;
            		g.drawString(str, xv, ys);

            		ys += lineH;
            		str = text("RACES_DIPLOMACY_CURRENT_TRADE");
            		g.drawString(str, xs, ys);
            		int amt = (int) view.trade().profit();
            		str = text("RACES_DIPLOMACY_TRADE_AMT", str(amt));
            		sw = g.getFontMetrics().stringWidth(str);
            		xv = xe - sw;
            		g.drawString(str, xv, ys);

            		// Fleets power
            		ys += lineH;
            		str = text("RACES_DIPLOMACY_FLEETS_POWER_RATIO");
            		g.drawString(str, xs, ys);
            		int age =  alien.status().age(player());
            		float alienPower  = alien.status().lastViewValue(player, EmpireStatus.FLEET);
            		float playerPower = player.status().ageViewValue(player, EmpireStatus.FLEET, age);
            		float ratio = playerPower/alienPower;
            		str = df1.format(ratio);
            		if (age>1) // Current turn has not been computed
					str += text("RACES_DIPLOMACY_AGE", (age-1));
            		sw = g.getFontMetrics().stringWidth(str);
            		xv = xe - sw;
            		g.drawString(str, xv, ys);

            		// Empires power
            		ys += lineH;
            		str = text("RACES_DIPLOMACY_EMPIRES_POWER_RATIO");
            		g.drawString(str, xs, ys);
            		age =  alien.status().age(player());
            		alienPower  = alien.status().lastViewValue(player, EmpireStatus.POWER);
            		playerPower = player.status().ageViewValue(player, EmpireStatus.POWER, age);
            		ratio = playerPower/alienPower;
            		str = df1.format(ratio);
            		if (age>1) // Current turn has not been computed
					str += text("RACES_DIPLOMACY_AGE", (age-1));
            		sw = g.getFontMetrics().stringWidth(str);
            		xv = xe - sw;
            		g.drawString(str, xv, ys);

					// declare war?
					if (options().canStartWar(player, alien)) {
						ys += lineH + s10;
						str = label();
						sw = g.getFontMetrics().stringWidth(str);
						xv = x3+((w3-sw)/2);
						g.setColor(Color.RED);
						drawBorderedString(g, str, xv, ys, SystemPanel.grayText, Color.RED);
					}
            	}
            }
            else {
            	int sx2 = flag.getWidth(null);
            	int sy2 = flag.getHeight(null);
            	flagW = flagH * sx2 / sy2;
				g.drawImage(flag, flagX, box.y, flagX+flagW, box.y+flagH, 0, 0, sx2, sy2, null);
            }
        }
        @Override
        public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
        	startWar();
        };
    }
	private final class EnterBattleSprite extends RoundButtonSprite {
		private final Color edgeC	= new Color(44,59,30);
		private final Color midC	= new Color(70,93,48);

		private void init(MapOverlayShipCombatPrompt p, Graphics2D g)  {
			parent = p;
			box.setLabelKey("SHIP_COMBAT_ENTER_BATTLE");
			String language = LanguageManager.current().selectedLanguageName();
			if (language.equals("Português")) {
				box.setSize(s20 + g.getFontMetrics(font()).stringWidth(label()), s30);
			}
			else
				box.setSize(s40 + g.getFontMetrics(font()).stringWidth(label()), s30);
			box.setForcedLocation(2);
		}
		@Override protected Font font()	{
			String language = LanguageManager.current().selectedLanguageName();
			if (language.equals("Português"))
				return narrowFont(16);
			return narrowFont(18);
		}
		@Override protected Color[] colors()	{ return new Color[] {edgeC, midC, edgeC}; }
		@Override public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
			Empire attacker = mgr.results().attacker();
			if (attacker != null && attacker.isPlayer())
				if (e.isShiftDown())
					mgr.dontTargetHarmlessColony(true);
				else if (e.isControlDown())
					mgr.dontTargetHarmlessColony(false);
			startCombat(ShipBattleUI.ENTER_COMBAT);
		};
	}
    // ##### Console Tools
	@Override public List<ConsoleOptions> getOptions() {
		List<ConsoleOptions> options = new ArrayList<>();
		options.add(new ConsoleOptions(KeyEvent.VK_A, "A", "Auto Resolve combat."));
		options.add(new ConsoleOptions(KeyEvent.VK_S, "S", "Smart Resolve combat, retreat if overwhelmed."));
		if(options().playerCanRetreat())
			options.add(new ConsoleOptions(KeyEvent.VK_R, "R", "Retreat Fleet."));
		return options;
	}
	@Override public String getMessage() {
    	Empire aiEmpire	= mgr.results().aiEmpire();
    	Empire player	= player();
        StarSystem sys	= galaxy().system(sysId);
    	String message	= displayYearOrTurn();
    	String titleStr;
        if (aiEmpire == null)
            titleStr = text("SHIP_COMBAT_TITLE_MONSTER_DESC", mgr.results().aiRaceName());
        else {
            titleStr = text("SHIP_COMBAT_TITLE_DESC");
            titleStr = aiEmpire.replaceTokens(titleStr, "alien");
        }
        message += ", " + titleStr;
 
        HashMap<String, Integer> mySizes = new HashMap<>();
        HashMap<String, Integer> aiSizes = new HashMap<>();
        for(CombatStack st : mgr.activeStacks()) {
            int putVal = st.num;
            if (st.isShip()) {
                if (st.empire() == player) {
                    if (mySizes.containsKey(st.design().sizeDesc()))
                        putVal += mySizes.get(st.design().sizeDesc());
                    mySizes.put(st.design().sizeDesc(), putVal);
                }
                else {
                    if (aiSizes.containsKey(st.design().sizeDesc()))
                        putVal += aiSizes.get(st.design().sizeDesc());
                    aiSizes.put(st.design().sizeDesc(), putVal);
                }
            }
            else if (st.isColony() && st.isArmed()) {
                if (st.empire() == player)
                    mySizes.put(text("MAIN_COLONY_BASES"), putVal);
                else
                    aiSizes.put(text("MAIN_COLONY_BASES"), putVal);
            }
        }
        message += NEWLINE + "My fleet consist of:";
        for (Entry<String, Integer> entry : mySizes.entrySet())
        	message += NEWLINE + entry.getValue() + " " + entry.getKey();
        message += NEWLINE + "Opponent fleet consist of:";
        for (Entry<String, Integer> entry : aiSizes.entrySet())
        	message += NEWLINE + entry.getValue() + " " + entry.getKey();

        // if unscouted, no planet info
        message += NEWLINE + "System Info:";
        boolean scouted = player.sv.isScouted(sys.id);
        if (scouted) {
        	//message += lineSplit + CommandConsole.cc().viewSystemInfo(sys, false);
        	message += NEWLINE + VIPConsole.systemInfo(sys);
        }
        else
        	message += NEWLINE + text("SHIP_COMBAT_TITLE_UNSCOUTED");

        message += NEWLINE + getMessageOption();
		return message;
	}
}
