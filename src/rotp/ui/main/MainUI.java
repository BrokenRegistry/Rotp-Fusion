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
package rotp.ui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JLayeredPane;
import javax.swing.border.Border;

import rotp.Rotp;
import rotp.model.Sprite;
import rotp.model.combat.ShipCombatManager;
import rotp.model.empires.Empire;
import rotp.model.empires.EspionageMission;
import rotp.model.empires.ShipView;
import rotp.model.empires.SpyNetwork;
import rotp.model.empires.SystemView;
import rotp.model.galaxy.IMappedObject;
import rotp.model.galaxy.Location;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.SpaceMonster;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.IConvenienceOptions;
import rotp.model.game.IDebugOptions;
import rotp.model.game.IGameOptions;
import rotp.model.ships.ShipDesign;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.UserPreferences;
import rotp.ui.game.HelpUI;
import rotp.ui.game.HelpUI.HelpSpec;
import rotp.ui.main.overlay.MapOverlay;
import rotp.ui.main.overlay.MapOverlayAdvice;
import rotp.ui.main.overlay.MapOverlayAllocateSystems;
import rotp.ui.main.overlay.MapOverlayAutosaveFailed;
import rotp.ui.main.overlay.MapOverlayBombardPrompt;
import rotp.ui.main.overlay.MapOverlayBombardedNotice;
import rotp.ui.main.overlay.MapOverlayColonizePrompt;
import rotp.ui.main.overlay.MapOverlayEspionageMission;
import rotp.ui.main.overlay.MapOverlayJava32Bit;
import rotp.ui.main.overlay.MapOverlayMemoryLow;
import rotp.ui.main.overlay.MapOverlayNone;
import rotp.ui.main.overlay.MapOverlayShipCombatPrompt;
import rotp.ui.main.overlay.MapOverlayShipsConstructed;
import rotp.ui.main.overlay.MapOverlaySpies;
import rotp.ui.main.overlay.MapOverlaySystemsScouted;
import rotp.ui.map.IMapHandler;
import rotp.ui.notifications.GameAlert;
import rotp.ui.races.RacesMilitaryUI;
import rotp.ui.races.RacesUI;
import rotp.ui.sprites.AlertDismissSprite;
import rotp.ui.sprites.FlightPathSprite;
import rotp.ui.sprites.HelpSprite;
import rotp.ui.sprites.SystemTransportSprite;
import rotp.ui.sprites.YearDisplaySprite;
import rotp.ui.vipconsole.VIPConsole;

public class MainUI extends BasePanel implements IMapHandler {
    private static final long serialVersionUID = 1L;
    public static Color paneBackground = new Color(123,123,123);
    public static Color paneBackgroundDk = new Color(100,100,100);
    public static Color paneShadeC = new Color(123,123,123,128);
    public static Color paneShadeC2 = new Color(100,100,100,192);
    private static final Color shadeBorderC = new Color(80,80,80);
    public static Color darkShadowC = new Color(30,30,30);
    private static final Color namePaneBackgroundHighlight =  new Color(64,64,96);
    private static final Color paneBackgroundHighlight = new Color(96,96,128);

    public static Color textBoxShade0 = new Color(150,150,175);
    public static Color textBoxShade1 = new Color(165,165,202);
    public static Color textBoxShade2 = new Color(112,110,158);
    public static Color textBoxTextColor = new Color(208,208,208);
    public static Color textBoxBackground = new Color(47,46,89);
    public static final Color transC = new Color(0,0,0,0);

    public static final Color greenAlertC  = new Color(0,255,0,192);
    public static final Color redAlertC    = new Color(255,0,0,192);
    public static final Color yellowAlertC = new Color(255,255,0,192);
    // modnar: add additional alert colors
    public static final Color blueAlertC   = new Color(0,120,255,192);
    public static final Color limeAlertC   = new Color(160,255,0,192);
    public static final Color orangeAlertC = new Color(255,160,0,192);
    public static final Color purpleAlertC = new Color(180,0,255,192);

    public static int panelWidth, panelHeight;
    static LinearGradientPaint alertBack;
    static Location center = new Location();

    JLayeredPane layers = new JLayeredPane();

    MapOverlayNone overlayNone;
    MapOverlayMemoryLow overlayMemoryLow;
    MapOverlayJava32Bit overlayJava32Bit;
    MapOverlayAutosaveFailed overlayAutosaveFailed;
    MapOverlayShipsConstructed overlayShipsConstructed;
    MapOverlaySpies overlaySpies;
    MapOverlayAllocateSystems overlayAllocateSystems;
    MapOverlaySystemsScouted overlaySystemsScouted;
    MapOverlayEspionageMission overlayEspionageMission;
    MapOverlayColonizePrompt overlayColonizePrompt;
    MapOverlayBombardPrompt overlayBombardPrompt;
    MapOverlayBombardedNotice overlayBombardedNotice;
    MapOverlayShipCombatPrompt overlayShipCombatPrompt;
    MapOverlayAdvice overlayAdvice;
    AlertDismissSprite alertDismissSprite;
    HelpSprite helpSprite;
    MapOverlay overlay;

    private final List<Sprite> nextTurnControls = new ArrayList<>();
    private final List<Sprite> baseControls = new ArrayList<>();

    protected SpriteDisplayPanel displayPanel;
    protected GalaxyMapPanel map;
    protected MainButtonPanel buttonPanel;

    // pre-post next turn state
    private float saveScale;
    private float saveX;
    private float saveY;
    private boolean showAdvice = false;
    private int helpFrame = 0;
    private int numHelpFrames = 0;
    private boolean showFleetInfo = false;
    private boolean lastHoverAltDown = false;
    
    @Override public boolean showFleetInfo()          { return showFleetInfo; }
    @Override public void showFleetInfo(boolean show) { showFleetInfo = show; }

    public Border paneBorder()               { return null; }
    public static Color shadeBorderC()       { return shadeBorderC; }
    public static Color paneBackground()     { return paneBackground; }
    public static Color paneHighlight()      { return paneBackgroundHighlight; }
    public static Color namePaneHighlight()  { return namePaneBackgroundHighlight; }

    public SpriteDisplayPanel displayPanel() { return displayPanel; }
    public void hideDisplayPanel()           {
        displayPanel.setVisible(false); 
    }
    public void showDisplayPanel()           { displayPanel.setVisible(true); }
    public void clearOverlay()               { overlay = showAdvice ? overlayAdvice : overlayNone; }

    private boolean displayPanelMasks(int x, int y) {
        if (!displayPanel.isVisible())
            return false;
        return displayPanel.getBounds().contains(x,y);
    }
    @Override
    public boolean drawMemory()              { return true; }
    @Override
    public GalaxyMapPanel map()              { return map; }

    public MainUI() {
        panelWidth = scaled(250);
        panelHeight = scaled(590);
        initModel();
        addMapControls();
        overlayNone = new MapOverlayNone(this);
        overlayMemoryLow = new MapOverlayMemoryLow(this);
        overlayJava32Bit = new MapOverlayJava32Bit(this);
        overlayAutosaveFailed = new MapOverlayAutosaveFailed(this);
        overlayShipsConstructed = new MapOverlayShipsConstructed(this);
        overlaySpies = new MapOverlaySpies(this);
        overlayAllocateSystems = new MapOverlayAllocateSystems(this);
        overlaySystemsScouted = new MapOverlaySystemsScouted(this);
        overlayEspionageMission = new MapOverlayEspionageMission(this);
        overlayColonizePrompt = new MapOverlayColonizePrompt(this);
        overlayBombardPrompt = new MapOverlayBombardPrompt(this);
        overlayBombardedNotice = new MapOverlayBombardedNotice(this);
        overlayShipCombatPrompt = new MapOverlayShipCombatPrompt(this);
        overlayAdvice = new MapOverlayAdvice(this);
        overlay = overlayNone;
    }
    public void init(boolean pauseNextTurn) {
        map.init();
        if (pauseNextTurn)
            buttonPanel.init();
        
        // if we are being opened not during the next turn process
        // but we have scouted systems (from forming an alliance)
        // bring up the overlay
        if (!session().performingTurn() && session().haveScoutedSystems()) {
            showSystemsScouted(session().systemsScouted());
        }
    }
    @Override
    public void cancel() {
        displayPanel.cancel();
    }
    public void saveMapState() {
        saveScale = map.scaleY();
        saveX = map.centerX();
        saveY = map.centerY();
        sessionVar("MAINUI_SAVE_CLICKED", clickedSprite());
    }
    public void restoreMapState() {
        showDisplayPanel();
        map.setScale(saveScale);
        map.centerX(saveX);
        map.centerY(saveY);
        map.resetRangeAreas();
        map.clearHoverSprite();
        clickedSprite((Sprite) sessionVar("MAINUI_SAVE_CLICKED"));
        showDisplayPanel();
    }
    public void repaintAllImmediately() {
        paintImmediately(0,0,getWidth(),getHeight());
    }
    public void addNextTurnControl(Sprite ms) { nextTurnControls.add(ms); }
    final protected void addMapControls() {
        helpSprite = new HelpSprite(this);
        alertDismissSprite = new AlertDismissSprite(this);
        baseControls.add(new YearDisplaySprite(this));
        baseControls.add(alertDismissSprite);
        baseControls.add(helpSprite);
    }
    @Override
    public boolean showAlerts() {
        return (session().currentAlert() != null) && displayPanel.isVisible();
    }
    @Override
    public boolean showTreasuryResearchBar()       { return overlay != overlayAdvice; }
    @Override
    public boolean showSpyReportIcon()             { return (overlay != overlayAdvice) && session().spyActivity(); }
    @Override
    public boolean showAllCurrentResearch()        { return (overlay == overlayEspionageMission); }
    
    public void setOverlay(MapOverlay lay) { overlay = lay; }
    public MapOverlay overlay()   { return overlay; }
    public void clearAdvice() {
        if (overlay == overlayAdvice) {
            showAdvice = false;
            overlay = overlayNone;
        }
    }
    public void showAdvice(String key, Empire emp1, String var1, String var2, String var3) {
        overlay = overlayAdvice;
        overlayAdvice.init(key, emp1, var1, var2, var3);
        showAdvice = true;
        repaint();
    }
    @Override
    public void cancelHelp() {
        helpFrame = 0;
        numHelpFrames = 0;
        RotPUI.helpUI().close();
    }
    @Override public void showHotKeys() {
        helpFrame = 1;        
        numHelpFrames = 1;
        loadHotKeysUI();
        repaint();   
    }
    @Override
    public void showHelp() {
        helpFrame = 1;
        
        numHelpFrames = 1;
        Sprite spr = clickedSprite();
        if (spr instanceof StarSystem) {
            StarSystem sys = (StarSystem) spr;
            if (sys.empire() == player())
                numHelpFrames = 5;
        }
        
        loadHelpUI();
        repaint();   
    }
    @Override 
    public void advanceHelp() {
        if (helpFrame == 0)
            return;
        helpFrame++; // TO DO BR: UNCOMMENT
        if (helpFrame > numHelpFrames)
        	cancelHelp();
        else
        	loadHelpUI();
        repaint();
    }
    public void showMemoryLowPrompt() {
        overlay = overlayMemoryLow;
        overlayMemoryLow.init();
        repaint();
    }
    public void showJava32BitPrompt() {
        overlay = overlayJava32Bit;
        overlayJava32Bit.init();
        repaint();
    }
    public void showAutosaveFailedPrompt(String err) {
        overlay = overlayAutosaveFailed;
        overlayAutosaveFailed.init(err);
        repaint();
    }
    public void showBombardmentPrompt(int sysId, ShipFleet fl) {
        overlay = overlayBombardPrompt;
        overlayBombardPrompt.init(sysId, fl);
        repaint();
    }
    public void showBombardmentNotice(int sysId, ShipFleet fl) {
        overlayBombardedNotice.init(sysId, fl);
        repaint();
    }
    public void showShipCombatPrompt(ShipCombatManager mgr) {
        overlay = overlayShipCombatPrompt;
        overlayShipCombatPrompt.init(mgr);
        repaint();
    }
    public void showColonizationPrompt(int sysId, ShipFleet fl, ShipDesign d) {
    	if (IDebugOptions.selectedShowVIPPanel()) {
    		VIPConsole.colonizeMenu.openColonyPrompt(sysId, fl);
    		return;
    	}
        overlay = overlayColonizePrompt;
        overlayColonizePrompt.init(sysId, fl, d);
        repaint();
    }
    public void showSpyReport() {
        overlay = overlaySpies;
        overlaySpies.init();
        repaint();
    }
    public void showEspionageMission(EspionageMission esp, int empId) {
        overlay = overlayEspionageMission;
        overlayEspionageMission.init(esp, empId);
        repaint();
    }
    public void showShipsConstructed(HashMap<ShipDesign, Integer> ships) {
        overlay = overlayShipsConstructed;
        overlayShipsConstructed.init();
        if (ships.isEmpty())
            resumeTurn();
        else
            repaint();
    }
    public void showSystemsScouted(HashMap<String, List<StarSystem>> newSystems) {
    	if (IDebugOptions.selectedShowVIPPanel()) {
    		VIPConsole.reportMenu.openScoutReport(newSystems);
    		return;
    	}
		overlay = overlaySystemsScouted;
        overlaySystemsScouted.init(newSystems);
    }
    public void allocateSystems(HashMap<StarSystem,List<String>> newSystems) {
    	if (IDebugOptions.selectedShowVIPPanel()) {
    		session().resumeNextTurnProcessing();
    		return;
    	}
        overlay = overlayAllocateSystems;
        overlayAllocateSystems.init(newSystems);
    }
    @Override
    public void handleNextTurn()    { displayPanel.handleNextTurn(); }
    private void initModel() {
        int w, h;
        if (!UserPreferences.windowed()) {
            Dimension size = Rotp.getSize();
            w = size.width;
            h = size.height;
        }
        else {
            w = scaled(Rotp.IMG_W);
            h = scaled(Rotp.IMG_H);
        }
 
        map = new GalaxyMapPanel(this);
        map.setBounds(0,0,w,h);

        int displayW = panelWidth;
        int displayH = panelHeight;
        displayPanel = new SpriteDisplayPanel(this);
        displayPanel.setBorder(newLineBorder(shadeBorderC,5));
        displayPanel.setBounds(w-displayW-s5,s5,displayW,displayH);

        int buttonH = s60;
        buttonPanel = new MainButtonPanel(this);
        buttonPanel.setBounds(s5,h-s5-buttonH,w-s10,buttonH);

        setLayout(new BorderLayout());
        add(layers, BorderLayout.CENTER);

        layers.add(buttonPanel, JLayeredPane.PALETTE_LAYER);
        layers.add(displayPanel, JLayeredPane.PALETTE_LAYER);
        layers.add(map, JLayeredPane.DEFAULT_LAYER);
        setOpaque(false);
    }
    public boolean enableButtons()   { return !session().performingTurn(); }
    public void selectSprite(Sprite o, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
        // if not in normal mode, then NextTurnControls are
        // the only sprites clickable
        if (overlay.consumesClicks(o)) {
            if (nextTurnControls.contains(o)) {
                o.click(map, count, rightClick, click, middleClick, e);
                map.repaint();
            }
            return;
        }
        boolean used = (displayPanel != null) && displayPanel.useClickedSprite(o, count, rightClick);
        hoveringOverSprite(null);
        if (!used)  {
            o.click(map, count, rightClick, click, middleClick, e);
//            if (o.persistOnClick()) { // BR: For console, to validate displayed info
                hoveringSprite(null);
                if(rightClick == true)
                    handleRightClick(o);
                else
                    clickedSprite(o);
//            }
            o.repaint(map);
        }
    }
    public void selectSystem(StarSystem sys) {
        // main goal here is to trigger sprite click behavior with no click sound
        sys.click(map, 1, false, false, false, null);
        hoveringSprite(null);
        clickedSprite(sys);
        Empire emp = player();
        map.centerX(avg(emp.minX(), emp.maxX()));
        map.centerY(avg(emp.minY(), emp.maxY()));
        map.setBounds(emp.minX()-3, emp.maxX()+3, emp.minY()-3, emp.maxY()+3);
        repaint();
    }
    private void selectPlayerHomeSystem() {
        Empire pl = player();
        StarSystem sys = galaxy().system(pl.capitalSysId());

        // main goal here is to trigger sprite click behavior with no click sound
        sys.click(map, 1, false, false, false, null);
        hoveringSprite(null);
        clickedSprite(sys);

        Empire emp = player();
        map.centerX(avg(emp.minX(), emp.maxX()));
        map.centerY(avg(emp.minY(), emp.maxY()));
        map.setBounds(emp.minX()-3, emp.maxX()+3, emp.minY()-3, emp.maxY()+3);
        repaint();
    }
    private void loadHotKeysUI() {
        if (helpFrame == 0)
            return;
    	helpFrame = numHelpFrames+2;
        
        Sprite spr = this.clickedSprite();
        if (spr instanceof SystemTransportSprite) {
        	helpFrame = numHelpFrames+2;
        	loadHelpFrameTP();
        	RotPUI.helpUI().open(this);
        	return;
        }
        if (spr instanceof StarSystem) {
            StarSystem sys = (StarSystem) spr;
            if (sys.empire() == player()) {
            	loadHotKeysFrame();
            	RotPUI.helpUI().open(this);
            	return;
            }
        }
        if (spr instanceof ShipFleet) {
            loadHotKeysFrame();
            addHotKeysShipFleet();
            RotPUI.helpUI().open(this);
            return;
        }
    	loadHotKeysFrame();
    	RotPUI.helpUI().open(this);        
    }
    private void loadHelpUI() {
        HelpUI helpUI = RotPUI.helpUI();
        if (helpFrame == 0)
            return;
        
        Sprite spr = this.clickedSprite();
        if (spr instanceof SystemTransportSprite) {
        	helpFrame = numHelpFrames+2;
        	loadHelpFrameTP();
        	helpUI.open(this);
        	return;
        }
        if (spr instanceof StarSystem) {
            StarSystem sys = (StarSystem) spr;
            if (sys.empire() == player()) {
                switch(helpFrame) {
                    case 1: loadEmpireColonyHelpFrame1(); break;
                    case 2: loadEmpireColonyHelpFrame2(); break;
                    case 3: loadEmpireColonyHelpFrame3(); break;
                    case 4: loadEmpireColonyHelpFrame4(); break;
                    default: loadButtonBarHelpFrame(); break;
                }
            }
            else {
                loadButtonBarHelpFrame();
            }
        }
        else {
            loadButtonBarHelpFrame();          
        }
        helpUI.open(this);
    }
    @Override
    public Color shadeC()                          { return Color.darkGray; }
    @Override
    public Color backC()                           { return Color.gray; }
    @Override
    public Color lightC()                          { return Color.lightGray; }
    @Override
    public boolean hoverOverFleets()               { return displayPanel.hoverOverFleets(); }
    @Override
    public boolean hoverOverSystems()              { return displayPanel.hoverOverSystems(); }
    @Override
    public boolean hoverOverFlightPaths()          { return displayPanel.hoverOverFlightPaths(); }
    @Override
    public boolean masksMouseOver(int x, int y)    { return displayPanelMasks(x, y) || overlay.masksMouseOver(x,y); }
    @Override
    public Color alertColor(SystemView sv)         { 
        if (sv.isAlert())
            return redAlertC;
        return null; 
    }
    @Override
    public boolean displayNextTurnNotice() {
        // don't display notice when updating things
        return (session().performingTurn()
                && !overlay.hideNextTurnNotice());
    }
    @Override
    public List<Sprite> nextTurnSprites()  { return nextTurnControls; }
    @Override
    public void checkMapInitialized() {
        Boolean inited = (Boolean) sessionVar("MAINUI_MAP_INITIALIZED");
        if (inited == null) {
            map.initializeMapData();
            selectPlayerHomeSystem();
            sessionVar("MAINUI_MAP_INITIALIZED", true);
        }
        displayPanel.releaseObjects(); // BR: To help garbage collector
    }
    @Override
    public void clickingNull(int cnt, boolean right) {
        displayPanel.useNullClick(cnt, right);
    };
    @Override
    public void clickingOnSprite(Sprite o, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
        // if not in normal mode, then NextTurnControls are
        // the only sprites clickable
        if (overlay.consumesClicks(o)) {
            if (nextTurnControls.contains(o)) {
                o.click(map, count, rightClick, click, middleClick, e);
                map.repaint();
            }
            return;
        }
        boolean used = (displayPanel != null) && displayPanel.useClickedSprite(o, count, rightClick);
        hoveringOverSprite(null);
        if (!used)  {
            o.click(map, count, rightClick, click, middleClick, e);
            if (o.persistOnClick()) {
                hoveringSprite(null);
                if(rightClick == true)
                    handleRightClick(o);
                else
                    clickedSprite(o);
            }
            o.repaint(map);
        }
    }
    public void handleRightClick(Sprite s) {
        if (s instanceof StarSystem && clickedSprite() instanceof StarSystem) {
            player().changeRalliesFromAToB((StarSystem)clickedSprite(), (StarSystem)s);
        }
    }
	@Override public void hoveringOverSprite(Sprite o)	{ hoveringOverSprite(o, false); }
	@Override public void hoveringOverSprite(Sprite o, boolean bypassMouse)	{
		boolean altDown	= isAltDown();
		boolean sameAlt	= altDown==lastHoverAltDown;
		lastHoverAltDown = altDown;

		boolean validChange = bypassMouse || map.getMousePosition() != null;
		if (!validChange)
			return;

		if (o == lastHoveringSprite()) {
			if (sameAlt)
				return;
		}
		else if (lastHoveringSprite() != null)
			lastHoveringSprite().mouseExit(map);

		if ((o instanceof StarSystem) 
				&& (lastHoveringSprite() instanceof StarSystem)
				&& (clickedSprite() instanceof ShipFleet)) {
            lastHoveringSprite().mouseExit(map);
            map.clearHoverSprite();
            lastHoveringSprite(null);
            hoveringSprite(null);
            ((StarSystem)o).repaint(map);
            displayPanel.useClickedSprite(clickedSprite(), 1, false);
        }
        lastHoveringSprite(o);
        if (overlay.hoveringOverSprite(o))
            return;

        boolean used = (displayPanel != null) && displayPanel.useHoveringSprite(o);
        if (!used) {
            if (hoveringSprite() != null)
                hoveringSprite().mouseExit(map);
            hoveringSprite(o);
            if (hoveringSprite() != null)
                hoveringSprite().mouseEnter(map);
        }
        repaint();
    }
    @Override
    public boolean shouldDrawSprite(Sprite s) {
        if (s == null)
            return false;
        if (s instanceof FlightPathSprite) {
            FlightPathSprite fp = (FlightPathSprite) s;
            Sprite fpShip = (Sprite) fp.ship();
            if (isClicked(fpShip) || isHovering(fpShip))
                return true;
			StarSystem dest = fp.destination();
			if (isClicked(dest) || isHovering(dest))
				return true;
            if (FlightPathSprite.workingPaths().contains(fp))
                return true;
            if (map.showAllFlightPaths())
                return true;
            if (map.showImportantFlightPaths())
                return fp.isPlayer() || fp.aggressiveToPlayer();
            return false;
        }
		if (s instanceof SystemTransportSprite) {
			SystemTransportSprite sts = (SystemTransportSprite) s;
			StarSystem dest = sts.starSystem();
			if (dest == null)
				return false;
			FlightPathSprite path = sts.pathSpriteTo(dest);
			if (path == null)
				return false;
			if (map.showAllFlightPaths())
				return true;
			if (isClicked(dest) || isHovering(dest))
				return true;
			StarSystem from = sts.homeSystem();
			if (isClicked(from) || isHovering(from))
				return true;
			if (map.showImportantFlightPaths())
				return path.isPlayer() || path.aggressiveToPlayer();
			return false;
		}
        return true;
    }
    @Override
    public Location mapFocus() {
        Location loc = (Location) sessionVar("MAINUI_MAP_FOCUS");
        if (loc == null) {
            loc = new Location();
            sessionVar("MAINUI_MAP_FOCUS", loc);
        }
        return loc;
    }

    public StarSystem lastSystemSelected()   { return (StarSystem) sessionVar("MAINUI_SELECTED_SYSTEM"); }
    public void lastSystemSelected(Sprite s) { sessionVar("MAINUI_SELECTED_SYSTEM", s); }
    @Override
    public Sprite clickedSprite()            { return (Sprite) sessionVar("MAINUI_CLICKED_SPRITE"); }
    @Override
    public void clickedSprite(Sprite s)      { 
        sessionVar("MAINUI_CLICKED_SPRITE", s); 
        if (s instanceof StarSystem)
            lastSystemSelected(s);
    }
    @Override
    public Sprite hoveringSprite()           { return (Sprite) sessionVar("MAINUI_HOVERING_SPRITE"); }
    public void hoveringSprite(Sprite s)     { 
        sessionVar("MAINUI_HOVERING_SPRITE", s); 
        if (s == null)
           return; 
        if (s.hasDisplayPanel() && !session().performingTurn()) 
            showDisplayPanel(); 
    }
    public Sprite lastHoveringSprite()       { return (Sprite) sessionVar("MAINUI_LAST_HOVERING_SPRITE"); }
    public void lastHoveringSprite(Sprite s) { sessionVar("MAINUI_LAST_HOVERING_SPRITE", s); }
    @Override
    public Border mapBorder()                { return null; 	}
    @Override
    public boolean canChangeMapScales()      { return overlay.canChangeMapScale(); }
    @Override
    public float startingScalePct()          { return 12.0f / map().sizeX(); }
    @Override
    public List<Sprite> controlSprites()     { return baseControls; }
    @Override
    public void reselectCurrentSystem()      {
        clickingOnSprite(lastSystemSelected(), 1, false, true, false, null);
        repaint();
    }
    @Override
    public IMappedObject gridOrigin()        {
        if (!map.showGridCircular())
            return null;
        Sprite spr = clickedSprite();
        if (spr instanceof IMappedObject) 
            return (IMappedObject) spr;
        return null;        
    }
    @Override
    public void animate() {
        // stop animating while number-crunching during next turn
        if (!displayNextTurnNotice()) {
            map.animate();
            displayPanel.animate();
        }
    }
    @Override
    public void paintOverMap(GalaxyMapPanel ui, Graphics2D g) {
        nextTurnControls.clear();
        overlay.paintOverMap(this, ui, g);
        if (showFleetInfo)
        	drawFleetsInfo(g);
    }
    private void drawFleetsInfo(Graphics2D g) {
    	if (!showFleetInfo)
    		return;

   		ShipFleet fleet = displayPanel.shipFleetToDisplay();
   		if (fleet == null) {
   			showFleetInfo = false;
   			return;
   		}

   		Empire empire = fleet.empire();
   		
    	if (empire == null) {
   			showFleetInfo = false;
    		return;
    	}
    	Empire player = player();
    	if (!player.hasContact(empire) && !empire.isMonster() && empire != player) {
   			showFleetInfo = false;
   			misClick();
    		return;
    	}

    	RacesMilitaryUI milPane = RacesUI.instance.militaryPanel;
    	
    	int w = scaled(947);
    	int h = BasePanel.s80;
       	int dh = h+BasePanel.s2;
		int x = BasePanel.s46;;
		int yi = BasePanel.s10;
		int y = scaled(166);
		Map<ShipDesign, Integer> visShip = fleet.visibleShipDesigns(player.id);

		if (empire.isPlayer()) {
			for( Entry<ShipDesign, Integer> entry : visShip.entrySet()) {
              	ShipView view = player.shipViewFor(entry.getKey());
              	milPane.drawShipDesign(g, view, entry.getValue(), x, y, w, h, paneBackground);
                y += dh;
			}
			milPane.paintPlayerData(g, x, yi, paneBackground);
		}
		else if (empire.isMonster()) {
			SpaceMonster monster = (SpaceMonster) fleet;
			//ShipDesign design = fleet.design(0);
			ShipDesign design = monster.design(0);
			for( Entry<ShipDesign, Integer> entry : visShip.entrySet()) {
				int num = entry.getValue();
              	ShipView view = empire.shipViewFor(design);
              	h = max(h, BasePanel.s20 * design.maxSpecials());
              	milPane.drawShipDesign(g, view, num, x, y, w, h, paneBackground);
                y += dh;
                milPane.paintMonsterData(g, empire, x, yi, paneBackground);
			}
		}
		else {
			SpyNetwork spies = player.viewForEmpire(empire).spies();
			for( Entry<ShipDesign, Integer> entry : visShip.entrySet()) {
              	ShipView view = spies.shipViewFor(entry.getKey());
              	milPane.drawShipDesign(g, view, entry.getValue(), x, y, w, h, paneBackground);
                  y += dh;
			}
			milPane.paintAlienData(g, empire, x, yi, paneBackground);
		}
    }
    public void advanceMap() {
        log("Advancing Main UI Map");
        overlay.advanceMap();
        map.hoverSprite = clickedSprite();
    }
    public void resumeTurn() {
        clearOverlay();        
        session().resumeNextTurnProcessing();
        repaint();
    }
    public void resumeOutsideTurn() {
        clearOverlay();        
        showDisplayPanel();
        repaint();
    }
    @Override
    public void drawAlerts(Graphics2D g) {
        if (!showAlerts())
            return;
        GameAlert alert = session().currentAlert();
        if (alert == null)
        	return;

        int x = getWidth() - scaled(255);
        int y = getHeight() - scaled(168);
        int w = scaled(250);
        int h = s100;

        if (alertBack == null) {
            float[] dist = {0.0f, 1.0f};
            Color topC = new Color(219,135,8);
            Color botC = new Color(254,174,45);
            Point2D start = new Point2D.Float(0, y);
            Point2D end = new Point2D.Float(0, y+h);
            Color[] colors = {topC, botC };
            alertBack = new LinearGradientPaint(start, end, dist, colors);
        }
        g.setPaint(alertBack);
        g.fillRoundRect(x, y, w, h, s5, s5);
        alertDismissSprite.setBounds(x, y, w, h);

        if (alertDismissSprite.hovering()) {
            Stroke prev = g.getStroke();
            g.setColor(Color.yellow);
            g.setStroke(stroke2);
            g.drawRoundRect(x, y, w, h, s5, s5);
            g.setStroke(prev);
        }

        int num = session().numAlerts();
        int count = session().viewedAlerts()+1;
        String title = num == 1 ? text("MAIN_ALERT_TITLE") : text("MAIN_ALERT_TITLE_COUNT", count, num);
        int x1 = x+scaled(10);
        int y1 = y+scaled(20);

        g.setColor(Color.black);
        g.setFont(narrowFont(18));
        drawString(g,title, x1, y1);
        
        String yearStr = displayYearOrTurn();
		if (IConvenienceOptions.showNextCouncil.get() 
				&& !options().selectedCouncilWinOption().equals(IGameOptions.COUNCIL_NONE)) {
        	int nextC = galaxy().council().nextCouncil();
        	 if (nextC > 0)
        		 yearStr += " (" + nextC + ")";
        }
        g.setFont(narrowFont(16));
        int yearW = g.getFontMetrics().stringWidth(yearStr);
        drawString(g,yearStr, x+w-s5-yearW, y+h-s5);

        g.setFont(narrowFont(16));
        List<String> descLines = wrappedLines(g, alert.description(), scaled(230));
        y1 += scaled(17);
        for (String line: descLines) {
            drawString(g,line, x1, y1);
            y1 += scaled(16);
        }
    }
    private void addHotKeysFrame() {
        HelpUI helpUI = RotPUI.helpUI();
        int xHK = scaled(25);
        int yHK = scaled(15);
        int wHK = scaled(500);
        helpUI.addBrownHelpText(xHK, yHK, wHK, -41, text("MAIN_HELP_HK"));
    }
    private void addHotKeysShipFleet() {
        HelpUI helpUI = RotPUI.helpUI();
        int xHK = scaled(540);
        int yHK = scaled(15);
        int wHK = scaled(400);
        helpUI.addBrownHelpText(xHK, yHK, wHK, 0, text("MAIN_HELP_SHIPFLEET_HK"));
    }
    private void loadHotKeysFrame() {
        HelpUI helpUI = RotPUI.helpUI();
        helpUI.clear();
        addHotKeysFrame();
    }
    private void loadHelpFrameTP() {
        HelpUI helpUI = RotPUI.helpUI();
        int w = getWidth();
        
        helpUI.clear();
        int x0 = scaled(75);
        int w0 = scaled(400);
        int y0 = scaled(300);
        helpUI.addBrownHelpText(x0, y0, w0, 5, text("FLEETS_HELP_3MAIN"));

        int w3 = scaled(350);
        int x3 = w-w3-scaled(280);
//        int y3 = y0- scaled(120);
//        int x3a = w-scaled(50);
//        int y3a = scaled(185);

//        HelpUI.HelpSpec sp3d = helpUI.addBrownHelpText(x3, y3, w3, 4, text("FLEETS_HELP_3D"));
//        sp3d.setLine(x3+w3, y3+sp3d.height()/2, x3a, y3a);

//        y3 += (sp3d.height()+s30);
        int y3	= y0;
        int x3a = w-scaled(120);
        int y3a = scaled(250);
        HelpUI.HelpSpec sp3 = helpUI.addBrownHelpText(x3, y3, w3, 7, text("FLEETS_HELP_3C"));
        sp3.setLine(x3+w3, y3+sp3.height()/2, x3a, y3a);

        
    }
    private void loadEmpireColonyHelpFrame1() {
        HelpUI helpUI = RotPUI.helpUI();

        int w = getWidth();
        int h = getHeight();

        helpUI.clear();
        HelpSpec s0 = helpUI.addBlueHelpText(s100, s10, scaled(350), 0, text("MAIN_HELP_ALL"));
        s0.setLine(s100, s25, s30, s20);
        // BR: Main Mod Help
        helpUI.addBrownHelpText(w-scaled(334), s10, scaled(330), 0, text("MAIN_HELP_MOD"));
        
        int x1 = w-scaled(779);
        int w1 = scaled(430);
        int y1 = scaled(100);
        
        HelpSpec sp1 = helpUI.addBlueHelpText(x1, y1, w1, 3, text("MAIN_HELP_1A"));
        y1 += (sp1.height()+s10);
        // BR: Moved this to make place for governor help
        HelpSpec sp7 = helpUI.addBlueHelpText(x1, y1, w1, 4, text("MAIN_HELP_1G"));
        sp7.setLine(x1+w1, y1+(sp7.height()/2), w-scaled(154), scaled(310));
        y1 += (sp7.height()+s10);

        HelpSpec sp2 = helpUI.addBlueHelpText(x1, y1, w1, 2, text("MAIN_HELP_1B"));
        sp2.setLine(x1+w1, y1+(sp2.height()/2), w-scaled(244), scaled(312));
        y1 += (sp2.height()+s5);
        HelpSpec sp3 = helpUI.addBlueHelpText(x1, y1, w1, 2, text("MAIN_HELP_1C"));
        sp3.setLine(x1+w1, y1+(sp3.height()/2), w-scaled(244), scaled(342));
        y1 += (sp3.height()+s5);
        HelpSpec sp4 = helpUI.addBlueHelpText(x1, y1, w1, 2, text("MAIN_HELP_1D"));
        sp4.setLine(x1+w1, y1+(sp4.height()/2), w-scaled(244), scaled(372));
        y1 += (sp4.height()+s5);
        HelpSpec sp5 = helpUI.addBlueHelpText(x1, y1, w1, 2, text("MAIN_HELP_1E"));
        sp5.setLine(x1+w1, y1+(sp5.height()/2), w-scaled(244), scaled(402));
        y1 += (sp5.height()+s5);
        HelpSpec sp6 = helpUI.addBlueHelpText(x1, y1, w1, 2, text("MAIN_HELP_1F"));
        sp6.setLine(x1+w1, y1+(sp6.height()/2), w-scaled(244), scaled(432));
        y1 += (sp6.height()+s5);
        HelpSpec spM6 = helpUI.addBrownHelpText(x1, y1, w1, 2, text("MAIN_HELP_MOD_1F"));
        spM6.setLine(x1+w1, y1+(spM6.height()/2), w-scaled(244), scaled(662));

        // BR: Moved this to make place for governor help
		// int x2 = w-scaled(299);
		// int y2 = scaled(150);
		// int w2 = scaled(280);
		// HelpSpec sp7 = helpUI.addBlueHelpText(x2,y2,w2, 6, text("MAIN_HELP_1G"));
		// sp7.setLine(x2+(w2/2), y2+sp7.height(), w-scaled(154), scaled(310));
        
        // BR:
		int x2 = w-scaled(325);
		int y2 = scaled(120);
		int w2 = scaled(160);
        HelpSpec spM1 = helpUI.addBrownHelpText(x2,y2,w2, 0, text("MAIN_HELP_MOD_1A"));
        spM1.setLine(x2+(w2*3/4), y2+spM1.height(), w-scaled(180), scaled(275));
		x2 += w2 +s10;
		w2 = scaled(150);
        HelpSpec spM2 = helpUI.addBrownHelpText(x2,y2,w2, 0, text("MAIN_HELP_MOD_1B"));
        spM2.setLine(x2+(w2*2/3), y2+spM2.height(), w-s40, scaled(275));

        int x3 = w-scaled(254); // BR: was 304
        int y3 = scaled(470); // BR: was 490
        int w3 = scaled(250); // BR: was 300
        HelpSpec sp8 = helpUI.addBlueHelpText(x3,y3,w3, 0, text("MAIN_HELP_1H"));
        sp8.setLine(x3+(w3*3/4), y3, w-scaled(54), scaled(430));        

        // BR: Added options panel
        // Setting panel
        boolean moreIcon = session().aFewMoreTurns();
        int yArrowOffset = moreIcon? 0 : s17;
        int yArrowSep = s35;
        int xBoxSep = moreIcon? s10 : s20;
        int xAL = s40;
        // Settings panel
        int x10 = scaled(115);
        int y10 = moreIcon? scaled(180) : scaled(200);
        int w10 = scaled(250);
        int y10a = scaled(300);
        HelpSpec sp10 = helpUI.addBrownHelpText(x10, y10, w10, 0, text("MAIN_HELP_MOD_1I")); // Settings panel
        sp10.setLine(x10, sp10.yce(), xAL, y10a);

        // Rules panel
        int x20 = x10;
        int w20 = w10;
        int y20 = sp10.ye() + xBoxSep;
        int y20a = y10a + yArrowSep;
        HelpSpec sp20 = helpUI.addBrownHelpText(x20, y20, w20, 0, text("MAIN_HELP_MOD_1J")); // Rules panel
        sp20.setLine(x20, sp20.yc(), xAL, y20a);

        // BR: Added Eco report icon
        int x14 = x10;
        int w14 = w10;
        int y14 = sp20.ye() + xBoxSep;
        int y14a = y20a + yArrowSep;
        HelpSpec sp14 = helpUI.addBrownHelpText(x14, y14, w14, 0, text("MAIN_HELP_MOD_1K")); // ECO
        sp14.setLine(x14, sp14.yc(), xAL, y14a+yArrowOffset);

        // BR: Added "a Few More Turns" icon
        int x15 = x10;
        int w15 = w10;
        int y15 = sp14.ye() + xBoxSep;
        int y15a = y14a + yArrowSep;
        HelpSpec sp15 = sp14;
        if (moreIcon) {
            sp15 = helpUI.addBrownHelpText(x15, y15, w15, 0, text("MAIN_HELP_MOD_1L")); // a Few More Turns
            sp15.setLine(x15, sp15.yc(), xAL, y15a);        	
        }

        // BR: Added Spy reports
        int x9 = x10;
        int w9 = w10;
        int y9 = sp15.ye() + xBoxSep;
        int y9a = y15a + yArrowSep;
        HelpSpec sp9 = helpUI.addBlueHelpText(x9, y9, w9, 0, text("MAIN_HELP_1I")); // Spy reports
        sp9.setLine(x9, sp9.ycb(), xAL, y9a-yArrowOffset);
        
        if (showTreasuryResearchBar()) {
            int x12 = x10;
            int y12 = sp9.ye() + xBoxSep;
            int w12 = w20;
            HelpSpec sp12 = helpUI.addBlueHelpText(x12, y12, w12, 0, text("MAIN_HELP_2L")); // Treasury
            sp12.setLine(x12, sp12.ycb(), xAL, h-scaled(290));

            int x13 = x10;
            int y13 = sp12.ye() + xBoxSep;
            int w13 = w20;
            HelpSpec sp13 = helpUI.addBlueHelpText(x13, y13, w13, 0, text("MAIN_HELP_2M")); // Research State
            sp13.setLine(x13, sp13.yc(), xAL, h-scaled(173));
        }
    }
    private void loadEmpireColonyHelpFrame2() {
        HelpUI helpUI = RotPUI.helpUI();
        
        int w = getWidth();

        helpUI.clear();
        HelpSpec s0 = helpUI.addBlueHelpText(s100, s10, scaled(350), 0, text("MAIN_HELP_ALL"));
        s0.setLine(s100, s25, s30, s25);

        // Hot Key frame
		int xHK = s5;
		int yHK = s15;
		int wHK = scaled(480);
		helpUI.addBrownHelpText(xHK, yHK, wHK, -41, text("MAIN_HELP_HK"));

		int wBox = scaled(460);
		int xBox = w - scaled(735);
		int xTar = w - scaled(254);
		int sep  = s5;

		// Factories
		int w10 = wBox;
		int x10 = xBox;
		int y10 = s15;
		HelpSpec sp10 = helpUI.addBlueHelpText(x10, y10, w10, 0, text("MAIN_HELP_2J"));
		sp10.setLine(sp10.xe(), sp10.ye()-s5, xTar-s5, scaled(155), w-scaled(49), scaled(205));

		// Population
		int x9 = xBox;
		int y9 = sp10.ye() + sep;
		int w9 = wBox;
		HelpSpec sp9 = helpUI.addBlueHelpText(x9, y9, w9, 0, text("MAIN_HELP_2I"));
		sp9.setLine(sp9.xe(), sp9.ye()-s5, xTar-s10, scaled(175), xTar+s80, scaled(205));

		// Shields
		int w1 = wBox;
		int x1 = xBox;
		int y1 = sp9.ye() + sep;
		HelpSpec sp1 = helpUI.addBlueHelpText(x1, y1, w1, 0, text("MAIN_HELP_2A"));
		sp1.setLine(sp1.xe(), sp1.ye()-s5, xTar+s95, scaled(230));

        // Bases
        int x2 = xBox;
        int y2 = sp1.ye() + sep;
        int w2 = w10;
        HelpSpec sp2 = helpUI.addBlueHelpText(x2, y2, w2, 0, text("MAIN_HELP_2B"));
        sp2.setLine(sp2.xe(), sp2.ye()-s5, w-scaled(49), scaled(240));

        // Production
        int x4 = xBox;
        int y4 = sp2.ye() + sep;
        int w4 = w10;
        HelpSpec sp4 = helpUI.addBlueHelpText(x4, y4, w4, 0, text("MAIN_HELP_2D"));
        sp4.setLine(sp4.xe(), sp4.ycb(), w-scaled(69), scaled(265));

        int x3 = xBox;
        int y3 = sp4.ye() + sep;
        int w3 = w10;
        HelpSpec sp3 = helpUI.addBlueHelpText(x3, y3, w3, 0, text("MAIN_HELP_2C"));
        sp3.setLine(sp3.xe(), sp3.ycb(), w-scaled(44), scaled(265));

        // Fleet
        int x5 = xBox;
        int y5 = sp3.ye() + sep;
        int w5 = w10;
        HelpSpec sp5 = helpUI.addBlueHelpText(x5, y5, w5, 0, text("MAIN_HELP_2E"));
        sp5.setLine(sp5.xe(), sp5.yce(), w-scaled(144), scaled(502));

        // Rally
        int x6 = xBox;
        int y6 = sp5.ye() + sep;
        //int y6a = scaled(555);
        int w6 = w5;
        HelpSpec sp6 = helpUI.addBlueHelpText(x6, y6, w6, 0, text("MAIN_HELP_2F"));
        sp6.setLine(sp6.xe(), sp6.yc(), w-scaled(144), scaled(535));

        // Rally Stargate
        int w6sg = scaled(250);
        int x6sg = w-w6sg-s4;
        int y6sg = y5 - s30;
        HelpSpec sp6sg = helpUI.addBrownHelpText(x6sg, y6sg, w6sg, 0, text("MAIN_HELP_2FSG"));
        sp6sg.setLine(x6sg+w6sg*3/4, y6sg+sp6sg.height(), w-scaled(30), scaled(520));

        // Transport
        int x7 = xBox;
        int y7 = sp6.ye() + sep;
        int w7 = w5;
        HelpSpec sp7 = helpUI.addBlueHelpText(x7,y7,w7, 0, text("MAIN_HELP_2G"));
        sp7.setLine(x7+w7, y7+s10, w-scaled(200), scaled(585));

        // Abandon
        int x8 = w-scaled(259);
        int y8 = scaled(630);
        int w8 = scaled(250);
        HelpSpec sp8 = helpUI.addBlueHelpText(x8,y8,w8, 0, text("MAIN_HELP_2H"));
        sp8.setLine(w-scaled(64), y8, w-scaled(64), scaled(582));    

        int w11;
        int y11;
        int x11;
        String txt;
        if (options().selectedFlagColorCount() == 1) {
        	txt   = text("MAIN_HELP_2K");
        	y11   = s14;
            if (has3Buttons()) {
            	w11 = scaled(250);
            	txt += " " + text("MAIN_HELP_2K_M3");
                x11 = w-w11-s4;
            } else {
            	w11 = scaled(225);
            	txt += " " + text("MAIN_HELP_2K_M2");
                x11 = w-w11-s14;
            }
        } else {
        	txt   = text("MAIN_HELP_2K_2F");
        	y11   = s4;
            if (has3Buttons()) {
            	txt += " " + text("MAIN_HELP_2K_M3");
            	w11 = scaled(250);
                x11 = w-w11-s4;
            } else {
            	txt += " " + text("MAIN_HELP_2K_M2");
            	w11 = scaled(225);
                x11 = w-w11-s14;
           }
            txt += " " + text("MAIN_HELP_2K_2F_HOLD");
        }

        HelpSpec sp11 = helpUI.addBrownHelpText(x11, y11, w11, 0, txt);
        sp11.setLine(w-scaled(40), y11+sp11.height(), w-scaled(25), scaled(170));
    }
    private void loadEmpireColonyHelpFrame3() { // BR: Smart max help
        HelpUI helpUI = RotPUI.helpUI();
        helpUI.clear();
        HelpSpec s0 = helpUI.addBlueHelpText(s100, s10, scaled(350), 0, text("MAIN_HELP_ALL"));
        s0.setLine(s100, s25, s30, s25);

        Sprite spr = this.clickedSprite();
        if (!(spr instanceof StarSystem))
        	return;
        if (((StarSystem) spr).empire() != player())
        	return;

        int w = getWidth();
        
        int sep = s5;
        int border = s10;

        int w1 = scaled(450);
        int x1 = w - w1 - scaled(280);
        int y1 = border;
        int xe = w-scaled(73);
        int ye = scaled(310);
        int dye = s29;

        // Top of the window
        HelpSpec sp2s = helpUI.addBrownHelpText(x1, y1, w1, 0, text("MAIN_HELP_4B"));
        sp2s.setLine(x1+w1, y1+(sp2s.height()*3/4), xe, ye);

        y1 = sp2s.ye() + sep;
        ye += dye;
        HelpSpec sp3s = helpUI.addBrownHelpText(x1, y1, w1, 0, text("MAIN_HELP_4C"));
        sp3s.setLine(x1+w1, y1+(sp3s.height()*3/4), xe, ye);

        y1 = sp3s.ye() + sep;
        ye += dye;
        HelpSpec sp4s = helpUI.addBrownHelpText(x1, y1, w1, 0, text("MAIN_HELP_4D"));
        sp4s.setLine(x1+w1, y1+(sp4s.height()/2), xe, ye);

        // Intermediate boxes
        int w0 = w1 + scaled(150);
        int x0 = x1 - scaled(150);
        int xBox = w-scaled(247);
        int yBox = scaled(297);
        int hBox = scaled(140);
        int wBox = s42;

        // Colony Order Help
        y1 = sp4s.ye() + sep;
        HelpSpec sp7s = helpUI.addBlueHelpText(x0, y1, w0, 0, text("MAIN_HELP_4G"));
        sp7s.setLine(x1+w1, y1+(sp7s.height()/3), xBox, yBox + s20);
        sp7s.setLineArr(sp7s.rect(xBox, yBox, wBox, hBox));
        
        // Global Help
        y1 = sp7s.ye() + sep;
        xBox = w-scaled(78);
        wBox = s65;
        HelpSpec sp8s = helpUI.addBlueHelpText(x0, y1, w0, 0, text("MAIN_HELP_4A"));
        sp8s.setLine(x1+w1, y1+(sp8s.height()*4/5), xBox, yBox + (hBox + dye)/2);
        sp8s.setLineArr(sp7s.rect(xBox, yBox, wBox, hBox));
     
        // Bottom of the window
        y1 = sp8s.ye() + sep;
        ye += dye;
        HelpSpec sp5s = helpUI.addBrownHelpText(x1, y1, w1, 0, text("MAIN_HELP_4E"));
        sp5s.setLine(x1+w1, y1+(sp5s.height()/2), xe, ye);

        y1 = sp5s.ye() + sep;
        ye += dye;
        HelpSpec sp6s = helpUI.addBrownHelpText(x1, y1, w1, 0, text("MAIN_HELP_4F"));
        sp6s.setLine(x1+w1, y1+(sp6s.height()/2), xe, ye);
    }
    private void loadEmpireColonyHelpFrame4() { // BR: Obedient Governor max help
        HelpUI helpUI = RotPUI.helpUI();
        helpUI.clear();
        int x0 = s100;
        int y0 = s10;
        int w0 = scaled(350);
        HelpSpec s0 = helpUI.addBlueHelpText(x0, y0, w0, 0, text("MAIN_HELP_ALL"));
        s0.setLine(s100, s25, s30, s25);

        Sprite spr = this.clickedSprite();
        if (!(spr instanceof StarSystem))
        	return;
        if (((StarSystem) spr).empire() != player())
        	return;

        int w = getWidth();
        
        int sep = s5;
        int border = s10;

        int w1 = scaled(400);
        int x1 = w - w1 - scaled(290);
        int y1 = border;
        int xe = w-scaled(73);
        int ye = scaled(310);

        // Intermediate boxes
        int wBox = scaled(240);
        int hBox = scaled(70);
        int xBox = w-wBox-s10;
        int yBox = scaled(200);

        // Top of the window
        y0 = s0.ye() + s100;
        helpUI.addBrownHelpText(x0, y0, w0, 0, text("MAIN_HELP_5A"));

        // Box Global Help (5 fields)
        HelpSpec spB = helpUI.addBrownHelpText(xBox, y1, wBox, 0, text("MAIN_HELP_5B"));
        spB.setLine(spB.xc(), spB.ye(), xBox+wBox*3/4, yBox);
        spB.setLineArr(spB.rect(xBox, yBox, wBox, hBox));

        // Factories
        xe = xBox + wBox/2 + s4;
        ye = yBox + s4;
        HelpSpec spFact = helpUI.addBrownHelpText(x1, y1, w1-s50, 0, text("MAIN_HELP_5D"));
        spFact.setLine(spFact.xe(), spFact.yc(), xe, ye);

        // Bases
        y1 = spFact.ye() + sep;
        xe = xBox + wBox/2 + s3;
        ye = yBox + s35;
        HelpSpec spBases = helpUI.addBrownHelpText(x1, y1, w1-s25, 0, text("MAIN_HELP_5F"));
        spBases.setLine(spBases.xe(), spBases.yc(), xe, ye);
     
        // Population
        y1 = spBases.ye() + sep;
        xe = xBox + s4;
        ye = yBox + s4;
        HelpSpec spPop = helpUI.addBrownHelpText(x1, y1, w1, 0, text("MAIN_HELP_5C"));
        spPop.setLine(spPop.xe(), spPop.yc(), xe, ye);

        // Shield
        y1 = spPop.ye() + sep;
        xe = xBox + s3;
        ye = yBox + s35;
        HelpSpec spShield = helpUI.addBrownHelpText(x1, y1, w1, 0, text("MAIN_HELP_5E"));
        spShield.setLine(spShield.xe(), spShield.yc(), xe, ye);

        // Build and Grow
        y1 = spShield.ye() + sep;
        xe = xBox + s4;
        ye = yBox + hBox - s4;
        HelpSpec spBuild = helpUI.addBrownHelpText(x1, y1, w1, 0, text("MAIN_HELP_5G"));
        spBuild.setLine(spBuild.xe(), spBuild.yc(), xe, ye);

        // Fleet
        y1 = spBuild.ye() + sep;
        xe = xBox + s10;
        ye = scaled(315);
        HelpSpec spFleet = helpUI.addBrownHelpText(x1, y1, w1, 0, text("MAIN_HELP_5H"));
        spFleet.setLine(spFleet.xe(), spFleet.yc(), xe, ye);

        // Tech
        y1 = spFleet.ye() + sep;
        xe = xBox + s10;
        ye = scaled(425);
        HelpSpec spTech = helpUI.addBrownHelpText(x1, y1, w1, 0, text("MAIN_HELP_5I"));
        spTech.setLine(spTech.xe(), spTech.yc(), xe, ye);

        // Ships
        y1 = spTech.ye() + sep;
        xe = xBox + wBox/2 - s5;
        ye = scaled(477);
        HelpSpec spShips = helpUI.addBrownHelpText(x1, y1, w1, 0, text("MAIN_HELP_5J"));
        spShips.setLine(spShips.xe(), spShips.yc(), xe, ye);

        // Transports
        y1 = spShips.ye() + sep;
        xe = xBox + s3;
        ye = scaled(580);
        HelpSpec spTrans = helpUI.addBrownHelpText(x1, y1, w1, 0, text("MAIN_HELP_5K"));
        spTrans.setLine(spTrans.xe(), spTrans.yc(), xe, ye);
    }
    private void loadButtonBarHelpFrame() {
        HelpUI helpUI = RotPUI.helpUI();
        helpUI.clear();
        HelpSpec s0 = helpUI.addBlueHelpText(s100, s10, scaled(350), 0, text("MAIN_HELP_ALL"));
        s0.setLine(s100, s25, s30, s25);

        int h = getHeight();
        int w = getWidth();
        
        int buttonW = buttonPanel.buttonW();
        
        int x1 = scaled(25);
        int y1 = scaled(470);
        int w1 = scaled(210);
        int y1a = h - scaled(65);
        int x1a = x1+(w1/4);
        HelpSpec sp1 = helpUI.addBlueHelpText(x1, y1, w1, 4, text("MAIN_HELP_3A"));
        sp1.setLine(x1a, y1+sp1.height(), x1a, y1a);

        int x2 = x1+(buttonW*2/3);
        int y2 = scaled(580);
        int x2a = x2+(w1/2);
        HelpSpec sp2 = helpUI.addBlueHelpText(x2, y2, w1, 4, text("MAIN_HELP_3B"));
        sp2.setLine(x2a, y2+sp2.height(), x2a, y1a);

        int x3 = x2+buttonW;
        HelpSpec sp3 = helpUI.addBlueHelpText(x3, y1, w1, 4, text("MAIN_HELP_3C"));
        sp3.setLine(x3+(w1/2), y1+sp3.height(), x3+(w1/2), y1a);

        int x4 = x3+buttonW;
        HelpSpec sp4 = helpUI.addBlueHelpText(x4, y2, w1, 0, text("MAIN_HELP_3D"));
        sp4.setLine(x4+(w1/2), y2+sp4.height(), x4+(w1/2), y1a);

        int x5 = x4+buttonW;
        HelpSpec sp5 = helpUI.addBlueHelpText(x5, y1, w1, 4, text("MAIN_HELP_3E"));
        sp5.setLine(x5+(w1/2), y1+sp5.height(), x5+(w1/2), y1a);

        int x6 = x5+buttonW;
        HelpSpec sp6 = helpUI.addBlueHelpText(x6, y2, w1, 4, text("MAIN_HELP_3F"));
        sp6.setLine(x6+(w1/2), y2+sp6.height(), x6+(w1/2), y1a);

        int x7 = x6+buttonW;
        HelpSpec sp7 = helpUI.addBlueHelpText(x7, y1, w1, 4, text("MAIN_HELP_3G"));
        sp7.setLine(x7+(w1/2), y1+sp7.height(), x7+(w1/2), y1a);

        int x8 = w-scaled(264);
        int w8 = scaled(150);
        HelpSpec sp8 = helpUI.addBlueHelpText(x8, y2, w8, 3, text("MAIN_HELP_3H"));
        sp8.setLine(x8+(w8/2), y2+sp8.height(), x8+(w8/2), y1a);

        int x9 = w-scaled(220);
        int w9 = scaled(200);
        HelpSpec sp9 = helpUI.addBlueHelpText(x9, y1, w9, 4, text("MAIN_HELP_3I"));
        sp9.setLine(w-scaled(79), y1+sp9.height(), w-scaled(79), h-scaled(100));

        int x10 = scaled(115);
        int y10 = scaled(125);
        int w10 = scaled(250);
        HelpSpec sp10 = helpUI.addBlueHelpText(x10, y10, w10, 3, text("MAIN_HELP_3J"));
        sp10.setLine(x10, y10+(sp10.height()/2), s45, y10+(sp10.height()/2));
    }
    @Override public void keyPressed(KeyEvent e)  {
    	setModifierKeysState(e); // BR: For the Flag color selection
    	if (e.getKeyCode() == KeyEvent.VK_ALT && !lastHoverAltDown) {
            map.altToggled(true);
        }
        if (!overlay.handleKeyPress(e))
            overlayNone.handleKeyPress(e);
    }
    @Override public void keyReleased(KeyEvent e) {
    	setModifierKeysState(e); // BR: For the Flag color selection
    	if (e.getKeyCode() == KeyEvent.VK_ALT && lastHoverAltDown) {
            map.altToggled(false);
        }
        if (!overlay.handleKeyReleased(e))
            overlayNone.handleKeyReleased(e);
    }

    public void selectGamePanel()	{
    	// BR: release element to help garbage collection on new game.
    	// May be more objects should be released.
    	displayPanel.releaseObjects();

        overlayMemoryLow.releaseObjects();
        overlayJava32Bit.releaseObjects();
        overlayAutosaveFailed.releaseObjects();
        overlayShipsConstructed.releaseObjects();
        overlaySpies.releaseObjects();
        overlayAllocateSystems.releaseObjects();
        overlaySystemsScouted.releaseObjects();
        overlayEspionageMission.releaseObjects();
        overlayColonizePrompt.releaseObjects();
        overlayBombardPrompt.releaseObjects();
        overlayBombardedNotice.releaseObjects();
        overlayShipCombatPrompt.releaseObjects();
        overlayAdvice.releaseObjects();

    	RotPUI.instance().selectGamePanel();
    }
}
