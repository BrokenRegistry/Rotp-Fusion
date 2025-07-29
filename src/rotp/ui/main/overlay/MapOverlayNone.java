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

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;

import rotp.model.Sprite;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.game.BaseCompactOptionsUI;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.main.MainUI;
import rotp.ui.main.TransportDeploymentPanel;
import rotp.ui.sprites.ShipRelocationSprite;
import rotp.ui.sprites.SystemTransportSprite;

public class MapOverlayNone extends MapOverlay {
    MainUI parent;
    public MapOverlayNone(MainUI p) {
        parent = p;
    }
    @Override public boolean hideNextTurnNotice()         { return false; }
    @Override public boolean canChangeMapScale()          { return true; }
    @Override public boolean consumesClicks(Sprite spr)   { return false; }
    @Override public boolean hoveringOverSprite(Sprite o) { return false; }
    @Override public void advanceMap()                    { parent.resumeTurn(); }
    @Override public void paintOverMap(MainUI parent, GalaxyMapPanel ui, Graphics2D g2) { }
	@Override public boolean handleKeyReleased(KeyEvent e) {
		parent.displayPanel().keyReleased(e);
		return true;
	}
    @Override public boolean handleKeyPress(KeyEvent e) {
    	// BR: For the Flag color selection
    	// setModifierKeysState(e); // Already done in MainUI
    	if(session().autoRunning() && (e.getKeyCode() == KeyEvent.VK_ESCAPE)) {
    		session().pauseAutoRun();
    		return true;
    	}
        if (session().performingTurn()) {
            // allocate systems overlay should pass keystrokes
            if (parent.displayPanel().isVisible())
                parent.displayPanel().keyPressed(e);
            return true;
        }
        boolean shift = e.isShiftDown();
        boolean ctrl  = e.isControlDown();
        int s40 = BasePanel.s40;

        if (e.getKeyChar() == '?') {
            parent.showHelp();
            return true;
        }
        switch(e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
            	if (parent.displayPanel().canEscape())
                    parent.displayPanel().keyPressed(e);
                else
                	parent.selectGamePanel();
                break;
			case KeyEvent.VK_NUMBER_SIGN:
			case KeyEvent.VK_DEAD_TILDE:
//				player().autoscout();
//				player().govAI().fleetCommander().nextTurn();
				player().fleetCommanderAI().nextTurn();
				break;
            case KeyEvent.VK_EQUALS:
                if (e.isShiftDown())  
                    parent.map().adjustZoom(-1);
                break;
            case KeyEvent.VK_MINUS:
            	if (e.isAltDown())
            		parent.map().maxZoomOut(1f, 1f);
            	else
            		parent.map().adjustZoom(1);
                break;
            case KeyEvent.VK_UP:
                parent.map().dragMap(0, s40);
                break;
            case KeyEvent.VK_DOWN:
                parent.map().dragMap(0, -s40);
                break;
            case KeyEvent.VK_LEFT:
                parent.map().dragMap(s40, 0);
                break;
            case KeyEvent.VK_RIGHT:
                parent.map().dragMap(-s40, 0);
                break;
            case KeyEvent.VK_HOME:
                Sprite sp = parent.displayPanel().spriteToDisplay();
                if (sp instanceof StarSystem) {
                    StarSystem sys = (StarSystem) sp;
                    player().sv.forceAutoFlagColor(sys.id, e.isAltDown());
                }
                break;
            case KeyEvent.VK_G:
                buttonClick();
                parent.selectGamePanel();
                break;
            case KeyEvent.VK_D:
                buttonClick();
                RotPUI.instance().selectDesignPanel();
                break;
            case KeyEvent.VK_F:
                if (e.isControlDown()) {
                    Sprite spr = parent.displayPanel().spriteToDisplay();
                    if (spr instanceof StarSystem) {
                        StarSystem sys = (StarSystem) spr;
                        player().sv.toggleFlagColor(sys.id,shift);
                        break;
                    }
                }
                buttonClick();
                RotPUI.instance().selectFleetPanel();
                break;
            case KeyEvent.VK_S:
                if (e.isShiftDown())
                    RotPUI.instance().showSpyReport();
                else {
                    buttonClick();
                    RotPUI.instance().selectSystemsPanel();
                }
                break;
            case KeyEvent.VK_E:
            case KeyEvent.VK_R:
                if (e.isAltDown()) {
                    if (parent.clickedSprite() instanceof StarSystem) {
                        StarSystem sys = (StarSystem) parent.clickedSprite();
                        if (player().canRallyFleetsTo(id(sys))) {
                            softClick();
                            player().changeAllExistingRallies(sys);
                            parent.displayPanel().repaint();
                            break;
                        }
                    }
                }
                else if (e.isControlDown()) {
                    if (parent.clickedSprite() instanceof StarSystem) {
                        StarSystem sys = (StarSystem) parent.clickedSprite();
                        if (player().canRallyFleetsFrom(id(sys))) {
                            softClick();
                            parent.clickedSprite(sys.rallySprite());
                            parent.displayPanel().repaint();
                            break;
                        }
                    }
                }
                else {
                    buttonClick();
                    RotPUI.instance().selectRacesPanel();
                    break;
                }
                break;
            case KeyEvent.VK_C:
                buttonClick();
                RotPUI.instance().selectPlanetsPanel();
                break;
            case KeyEvent.VK_T:
                if  (e.getModifiersEx() == 0) { // No modifier!
                    buttonClick();
                    RotPUI.instance().selectTechPanel();
                    break;
                }
                else if (e.isControlDown()) {
                    if (parent.clickedSprite() instanceof StarSystem) {
                        StarSystem sys = (StarSystem) parent.clickedSprite();
                        if (player().canSendTransportsFrom(sys)) {
                            softClick();
                            TransportDeploymentPanel.enableAbandon = false; 
                            parent.hoveringOverSprite(null);
                            parent.clickedSprite(sys.transportSprite());
                            parent.displayPanel().repaint();
                            break;
                        }
                    }
                }
           		misClick();
                break;
            case KeyEvent.VK_A:
                if (e.isControlDown()) {
                    if (parent.clickedSprite() instanceof StarSystem) {
                        StarSystem sys = (StarSystem) parent.clickedSprite();
                        if (player().canSendTransportsFrom(sys)) {
                            softClick();
                            TransportDeploymentPanel.enableAbandon = true; 
                            parent.hoveringOverSprite(null);
                            parent.clickedSprite(sys.transportSprite());
                            parent.displayPanel().repaint();
                            break;
                        }
                    }
                }
                misClick();
                break;
            case KeyEvent.VK_L:
                if (parent.clickedSprite() instanceof StarSystem) {
                    StarSystem sys = (StarSystem) parent.clickedSprite();
                    if (player().canRallyFleetsFrom(id(sys))) {
                        softClick();
                        parent.clickedSprite(sys.rallySprite());
                        parent.displayPanel().repaint();
                        break;
                    }
                }
                misClick();
                break;
            case KeyEvent.VK_N:
                //softClick();
            	if (options().ironmanLocked() || options().autoRunAILocked()) {
            		misClick();
            		break;
            	}
                parent.handleNextTurn();
                session().nextTurn();
                break;
            case KeyEvent.VK_O: // BR:
            	if (options().isGameOptionsAllowed()) {
            		BaseCompactOptionsUI optionsUI = RotPUI.rulesUI();
        			optionsUI.start("", null);
            	}
            	break;
            case KeyEvent.VK_Y: // BR:
            	options().toggleYearDisplay();
                break;
            case KeyEvent.VK_W: // BR:
            	if (e.isAltDown()) {
            		options().toggleSquareGridDisplay();
                    parent.repaint();
                    break;
            	}
            	GalaxyMapPanel.toggleWarView();
            	// Refresh to force activation
            	StarSystem currSys = null;
            	// are we transporting?
                if (parent.clickedSprite() instanceof SystemTransportSprite)
                    currSys = ((SystemTransportSprite) parent.clickedSprite()).homeSystem();
                    // are we ship relocating?
                else if (parent.clickedSprite() instanceof ShipRelocationSprite)
                    currSys = ((ShipRelocationSprite) parent.clickedSprite()).homeSystemView();
                else if (parent.clickedSprite() instanceof StarSystem)
                    currSys = (StarSystem) parent.clickedSprite();
                
                if (currSys != null) {
                	parent.clickedSprite(currSys);
                	parent.map().recenterMapOn(currSys);
                }
                parent.repaint();
                break;
            case KeyEvent.VK_F1:
            	if (shift)
            		parent.showHotKeys();
            	else
            		parent.showHelp();
                break;
            case KeyEvent.VK_F2:
            	nextSystem(player().orderedColonies());
                break;
            case KeyEvent.VK_F3:
            	prevSystem(player().orderedColonies());
                break;
            case KeyEvent.VK_F5:
            	nextSystem(player().orderedShipConstructingColonies());
                break;
            case KeyEvent.VK_F6:
            	prevSystem(player().orderedShipConstructingColonies());
                break;
            case KeyEvent.VK_F7:
            	nextSystem(player().orderedUnderAttackSystems(
            			parent.map().showUnarmedShips(), !parent.map().showFleetsOnly()));
                break;
            case KeyEvent.VK_F8:
            	prevSystem(player().orderedUnderAttackSystems(
            			parent.map().showUnarmedShips(), !parent.map().showFleetsOnly()));
                break;
            case KeyEvent.VK_F9:
            	nextFleet(player().orderedFleets());
                break;
            case KeyEvent.VK_F10:
            	prevFleet(player().orderedFleets());
                break;
            case KeyEvent.VK_F11:
            	nextFleet(player().orderedEnemyFleets());
                break;
            case KeyEvent.VK_F12:
            	prevFleet(player().orderedEnemyFleets());
                break;
            case KeyEvent.VK_PAGE_DOWN:
            case KeyEvent.VK_SPACE:
            	if (e.isShiftDown())
            		nextFleet(player().orderedAutoButIdleFleets());
            	else
            		nextFleet(player().orderedIdleFleets());
                break;
            case KeyEvent.VK_PAGE_UP:
            	if (e.isShiftDown())
            		prevFleet(player().orderedAutoButIdleFleets());
            	else
            		prevFleet(player().orderedIdleFleets());
                break;
            case KeyEvent.VK_MULTIPLY:
            case KeyEvent.VK_7:
            	nextFlag(1, shift, ctrl);
            	break;
            case KeyEvent.VK_SUBTRACT:
            case KeyEvent.VK_8:
        	   nextFlag(2, shift, ctrl);
            	break;
            case KeyEvent.VK_NUMPAD9:
            case KeyEvent.VK_U:
            case KeyEvent.VK_DIVIDE:
            	nextFlag(4, shift, ctrl);
            	break;
            case KeyEvent.VK_ADD:
            case KeyEvent.VK_I:
            	nextFlag(3, shift, ctrl);
            	break;
            default:
                parent.displayPanel().keyPressed(e);
                break;
        }
        return true;
    }
    private void nextFlag(int flagId, boolean shift, boolean ctrl) {
    	StarSystem currSys = parent.lastSystemSelected();
        currSys = player().nextFlaggedSystems(currSys, flagId, ctrl, shift);
        if(currSys != null) {
        	parent.clickingOnSprite(currSys, 1, false, true, false, null);
        	parent.clickedSprite(currSys);
            parent.map().recenterMapOn(currSys);
            parent.repaint();
        }
    }
    private StarSystem currentSystem() {
    	StarSystem currentSystem = null;
    	Sprite clickedSprite = parent.clickedSprite();
    	// are we transporting?
        if (clickedSprite instanceof SystemTransportSprite) {
            SystemTransportSprite trSpr = (SystemTransportSprite) parent.clickedSprite();
            currentSystem = trSpr.starSystem() == null ? trSpr.homeSystem() : trSpr.starSystem();
        }
        // are we ship relocating?
        else if (clickedSprite instanceof ShipRelocationSprite) {
            ShipRelocationSprite spr = (ShipRelocationSprite) parent.clickedSprite();
            currentSystem = spr.starSystem() == null ? spr.homeSystemView() : spr.starSystem();
        }
        else if (clickedSprite instanceof StarSystem)
            currentSystem = (StarSystem) clickedSprite;
    	return currentSystem;
    }
    private void goToSystem(StarSystem system) {
        if (parent.clickedSprite() instanceof SystemTransportSprite) {
			parent.hoveringOverSprite(system, true);
            parent.clickingOnSprite(system, 1, false, false, false, null);
        }
        else if (parent.clickedSprite() instanceof ShipRelocationSprite) {
			parent.hoveringOverSprite(system, true);
        }
        else {
			parent.hoveringOverSprite(system, true);
            parent.clickedSprite(system);
            parent.map().recenterMapOn(system);
        }
    }
    private void nextSystem(List<StarSystem> systems) {
    	if (systems.isEmpty()) {
            misClick();
            return;
    	}
    	StarSystem currSys = currentSystem();
    	// find next index (exploit that missing element returns -1, so set to 0)
        int index = systems.indexOf(currSys)+1;
        if (index == systems.size())
            index = 0;
        goToSystem(systems.get(index));
        parent.repaint();
    }
    private void prevSystem(List<StarSystem> systems) {
    	if (systems.isEmpty()) {
            misClick();
            return;
    	}
    	StarSystem currSys = currentSystem();
    	int index = systems.indexOf(currSys)-1;
        if (index < 0)
            index = systems.size()-1;
        goToSystem(systems.get(index));
        parent.repaint();
    }
    private void nextFleet(List<ShipFleet> fleets) {
    	ShipFleet currFleet = null;
        if (fleets.isEmpty())
            misClick();
        else {
            if (parent.clickedSprite() instanceof ShipFleet)
                currFleet = (ShipFleet)parent.clickedSprite();
            int index = fleets.indexOf(currFleet)+1;
            if (index == fleets.size())
                index = 0;
            parent.clickingOnSprite(fleets.get(index), 1, false, true, false, null);
            parent.map().recenterMapOn(fleets.get(index));
            parent.repaint();
        }
    }
    private void prevFleet(List<ShipFleet> fleets) {
    	ShipFleet currFleet = null;
        if (fleets.isEmpty())
            misClick();
        else {
        	if (parent.clickedSprite() instanceof ShipFleet)
                currFleet = (ShipFleet)parent.clickedSprite();
            int index = fleets.indexOf(currFleet)-1;
            if (index < 0)
                index = fleets.size()-1;
            parent.clickingOnSprite(fleets.get(index), 1, false, true, false, null);
            parent.map().recenterMapOn(fleets.get(index));
            parent.repaint();
        }
    }
}
