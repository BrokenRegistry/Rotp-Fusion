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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints; // modnar: needed for adding RenderingHints
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ConcurrentModificationException;
import java.util.List;

import rotp.model.Sprite;
import rotp.model.empires.Empire;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.SpaceMonster;
import rotp.model.galaxy.StarSystem;
import rotp.model.ships.ShipDesign;
import rotp.model.ships.ShipDesignLab;
import rotp.model.ships.ShipLibrary;
import rotp.ui.BasePanel;
import rotp.ui.map.IMapHandler;
import rotp.ui.sprites.FlightPathSprite;

public final class FleetPanel extends BasePanel implements MapSpriteViewer {
    private static final long serialVersionUID = 1L;
    private final SpriteDisplayPanel parent;
    protected BasePanel topPane;
    protected FleetDetailPane detailPane;
    protected BasePanel bottomPane;
    String nebulaText;
    private final int[] stackAdjustment = new int[ShipDesignLab.MAX_DESIGNS];
    public void toggleShowFleetInfo() {
    	parent.parent.showFleetInfo(!parent.parent.showFleetInfo());
    }
    public void clearShowFleetInfo() { parent.parent.showFleetInfo(false); }

    //session vars
    private StarSystem selectedDest()         { return (StarSystem) sessionVar("FLEETDEPLOY_SELECTED_DEST"); }
    private void selectedDest(StarSystem s)   { sessionVar("FLEETDEPLOY_SELECTED_DEST", s); }
    private StarSystem tentativeDest()        { return (StarSystem) sessionVar("FLEETDEPLOY_TENTATIVE_DEST"); }
    private void tentativeDest(StarSystem s)  { sessionVar("FLEETDEPLOY_TENTATIVE_DEST", s); }
    private ShipFleet selectedFleet()         {
        Object obj = sessionVar("SELECTED_FLEET");
        if (obj instanceof ShipFleet)
            return (ShipFleet) obj;

        selectedFleet(null);
        return null;
    }
    @Override
    public void cancel()                            { removeSessionVar("ADJUSTED_FLEET"); clearStackAdjustments(); }
    @Override
    public boolean canEscape()                      { return true; }
    private void selectedFleet(ShipFleet s)         { sessionVar("SELECTED_FLEET", s); }
    private ShipFleet adjustedFleet() {
        Object adjFleetObj = sessionVar("ADJUSTED_FLEET");
        if ((adjFleetObj != null) && (adjFleetObj instanceof ShipFleet))
            return (ShipFleet) adjFleetObj;

        //log("creating adjusted fleet");
        ShipFleet adjFleet = newAdjustedFleet();
        adjustedFleet(adjFleet);
        if (adjFleet == null)
            return null;
        if (tentativeDest() != null)
            FlightPathSprite.workingPath(adjFleet.pathSpriteTo(tentativeDest()));
        return adjFleet;
    }
    private void  adjustedFleet(ShipFleet fl) {
        FlightPathSprite.workingPath(null);
        StarSystem sys = selectedDest();
        if ((fl != null) && (sys != null))
            FlightPathSprite.workingPath(fl.pathSpriteTo(sys));

        sessionVar("ADJUSTED_FLEET", fl);
    }
    private ShipFleet displayedFleet()  {
        ShipFleet fl = parent.shipFleetToDisplay();
        return (fl == null) ? adjustedFleet() : fl;
    }
    private void  displayedFleet(ShipFleet s)       { sessionVar("DISPLAYED_FLEET", s); }
    public FleetPanel(SpriteDisplayPanel p) {
        parent = p;
        selectNewFleet(null);
        initModel();
    }
    public void releaseObjects() { }

    @Override
    public void handleNextTurn()             {  clearStackAdjustments(); }
    public ShipFleet fleetToDisplay()        { return parent.shipFleetToDisplay(); }
    @Override
    public boolean hoverOverFleets()         { return (selectedFleet() == null) || (selectedFleet().empire() != player()); }
    @Override
    public boolean hoverOverFlightPaths()    { return selectedFleet() == null; }
    public StarSystem displayedDestination() {
        if (tentativeDest() != null)
            return tentativeDest();
        else if (selectedDest() != null)
            return selectedDest();
        else
            return null;
    }
    private void clearStackAdjustments() {
        adjustedFleet(null);
        for (int i=0;i<stackAdjustment.length;i++)
            stackAdjustment[i] = 0;
    }
    private boolean haveClickedOnCurrentFleet() {
        // is the current clicked sprite a Fleet?
        return parent.parent.isClicked(displayedFleet());
    }
    private boolean canConsume(Sprite s) {
        // to "consume" a hovered sprite means that this FleetPanel may
        // (or may not) use it but we don't want to relinquish focus
        // to whatever panel is used to display that sprite
        if (s == null)
            return true;
        if (s instanceof ShipFleet)
            return true;
        
        // relinquish focus to star system if the displayed fleet
        // cannot be sent to it
        if (s instanceof StarSystem) {
            ShipFleet fleet = adjustedFleet();
            if (fleet == null)
                return false;
            if (!fleet.canBeSentBy(player()))
                return false;
            // BR: Removed this exception
            // This was preventing the selected fleet from being notified of its deselection.
            // if ((fleet.system() == s) && !fleet.isInTransit()) {
            //     tentativeDest(null);
            //     FlightPathSprite.clearWorkingPaths();
            //     return false;                
            // }
            return true;
        }
        
        return false;
    }
    private boolean canSendFleet() {
        if (selectedDest() == null)
            return false;

        ShipFleet newFleet = adjustedFleet();
        if (newFleet.isEmpty())
            return false;

        return newFleet.canReach(selectedDest());
    }
    /*
     * Player's resized fleet (Right Panel)
     */
    private ShipFleet newAdjustedFleet() {
        ShipFleet selectedFleet = selectedFleet();
        if (selectedFleet == null) {
            selectedFleet(null);
            return null;
        }
        if (!selectedFleet.isActive())
            return null;

        if (selectedFleet.isDeployed())
            return selectedFleet;

        ShipFleet newFleet = ShipFleet.copy(selectedFleet);
        for (int i=0;i<stackAdjustment.length;i++)
            newFleet.addShips(i, stackAdjustment[i]);
        newFleet.rallySysId(selectedFleet.rallySysId());
        newFleet.retreating(selectedFleet.retreating());
        return newFleet;
    }
    public void sendFleet() {
        // attempts to send fleet (OK button) if that selected
        // vars are valid
        if (!canSendFleet()) 
            return;

        ShipFleet newFleet = adjustedFleet();
        ShipFleet displayedFleet = selectedFleet();

        if (displayedFleet.inTransit()) {
            galaxy().ships.redirectFleet(displayedFleet, selectedDest().id);
            cancelFleet();
        }
        else {
            boolean newFleetCreated = galaxy().ships.deploySubfleet(displayedFleet, newFleet.numCopy(), selectedDest().id);
            // newFleet isEmpty if it was the entire fleet selected
            if (newFleetCreated) {
                if (displayedFleet.isEmpty()) 
                    cancelFleet();
                else {
                    selectNewFleet(displayedFleet);
                    adjustStacksToMatchFleet(displayedFleet, newFleet);
                }
            }
            else {
                cancelFleet();
            }
        }
        FlightPathSprite.clearWorkingPaths();
        parent.parent.map().repaint();
    }
    public void undeployFleet() {
        galaxy().ships.undeployFleet(selectedFleet());
        selectNewFleet(null);
        parent.parent.reselectCurrentSystem();
        parent.parent.map().repaint();
    }
    public void cancelFleet() {
        selectNewFleet(null);
        parent.parent.reselectCurrentSystem();
    }
    private void selectNewFleet(ShipFleet fl) {
        clearStackAdjustments();
        adjustedFleet(null);
        tentativeDest(null);
        selectedDest(null);
        displayedFleet(fl);
        selectedFleet(fl);
        FlightPathSprite.clearWorkingPaths();
    }
    private void adjustStacksToMatchFleet(ShipFleet selected, ShipFleet deployed) {
        int selectedCount = 0;
        for (int i=0;i<stackAdjustment.length;i++) {
            if (selected.num(i) <= deployed.num(i)) 
                stackAdjustment[i] = 0;
            else
                stackAdjustment[i] = deployed.num(i) - selected.num(i);
            selectedCount += (selected.num(i)+stackAdjustment[i]);
        }
        // if what remains is a selected fleet adjusted down to 0, then
        // clear the adjustments
        if (selectedCount == 0) 
            clearStackAdjustments();
    }
    @Override
    public boolean useHoveringSprite(Sprite o) {
        if (!canConsume(o))
            return false;

        // no selected fleet, so skip. This happens from when we have selected
        // a system, hover over a fleet (displaying this UI), and then
        // hover over a system (which can be used by this UI so it gets this far)
        if (selectedFleet() == null)
            return false;

        // SHOULD NEVER OCCUR as these sprites fail the prior check
        // any hovered fleets are consumed with no action
        if ((o instanceof ShipFleet)
        || (o instanceof FlightPathSprite)) {
            tentativeDest(null);
            FlightPathSprite.clearWorkingPaths();
            return haveClickedOnCurrentFleet();
        }

        if (o == null) {
            // if we aren't currently hovering over a target system,
            // then we aren't using this null to clear it out
            if (tentativeDest() == null)
                return false;
            if (haveClickedOnCurrentFleet()) {
                if (selectedDest() == null) 
                    FlightPathSprite.clearWorkingPaths();
                else
                    adjustedFleet().use(selectedDest(), parent.parent);
            }
            else {
                // we were hovering over fleet, so default back to selected fleet
                displayedFleet(null);
            }
            tentativeDest(null);
            adjustedFleet(null);
            parent.parent.repaint();
            return haveClickedOnCurrentFleet();
        }

        // if we are not a System, quit now
        if (!(o instanceof StarSystem))
            return false;

        if (adjustedFleet() == null)
            return false;

        if (adjustedFleet().empire() != player())
            return false;

        if (isAltDown())
        	return false;

        adjustedFleet().use(o, parent.parent);
        tentativeDest((StarSystem) o);
        return true;
    }
    @Override
    public boolean useNullClick(int cnt, boolean right) {
        if (right) {
            cancelFleet();
            return true;
        }
        return false;
    }
    @Override
    public boolean useClickedSprite(Sprite o, int count, boolean rightClick) {
        // we have clicked on a system view at this point
        if (rightClick) {
            cancelFleet();
            return true;
        }

        // use clicked Fleets that can be sent... just reset vars
        if (o instanceof ShipFleet) {
            ShipFleet clickedFleet = (ShipFleet) o;
            if (clickedFleet.empire() != player())
                return false;
            if (clickedFleet != selectedFleet())
                selectNewFleet(clickedFleet);
            return false;
        }

        if (o instanceof FlightPathSprite)  
            return true;    

        // clicking on anything but a systemview
        // will leave this screen
        if (!(o instanceof StarSystem)) 
            return false;

        // special case check:
        // on Cancel, then selected fleet is null and we get
        // here when the last selected system is reselected
        if (selectedFleet() == null) 
            return false;

        if (selectedFleet().empire() != player())
            return false;

        StarSystem sys = (StarSystem) o;

        if (selectedFleet().destSysId() == sys.id) 
            return false;

        tentativeDest(sys);
        // don't accept clicks for out of range systems
        // but consume the click (to stay on this view)
        ShipFleet adjustedFleet = adjustedFleet();
        if (adjustedFleet == null) 
            return false;
        if (!adjustedFleet.canReach(sys)) { 
            misClick();
            return true;
        }
        // BR: Allow the selection of the originating system
        // The fleet selection may have been the result
        // of missing the system on the previous click
        if (sys == adjustedFleet.system()) {
        	//System.out.println("sys == adjustedFleet.system()");
        	selectedFleet(null);
        	cancel();
        	return false;
        }
        if (!adjustedFleet.canSendTo(id(sys))) {
            misClick();
            return true;
        }

        softClick();
        selectedDest(sys);
        adjustedFleet.use(o, parent.parent);
        //if (count == 2)
        sendFleet();
        return true;
    }
    @Override
    public void keyPressed(KeyEvent e) {
    	setModifierKeysState(e); // BR: For the Flag color selection
        int k = e.getKeyCode();
        switch (k) {
            case KeyEvent.VK_ESCAPE:
                buttonClick();
                cancelFleet();
                return;
            case KeyEvent.VK_SPACE:
                buttonClick();
                sendFleet();
                return;
            case KeyEvent.VK_TAB:
                // tab-targeting for transports
                ShipFleet fl = adjustedFleet();
                if (fl == null)
                    break;
                StarSystem currSys;
                if (tentativeDest() != null)
                    currSys = tentativeDest();
                else {
                    int currSysId = fl.inTransit() ? fl.destSysId() : fl.sysId();
                    currSys = galaxy().system(currSysId);
                }
                List<StarSystem> systems = player().orderedFleetTargetSystems(fl);
                if (systems.size() > 1)
                    softClick();
                else
                	if (!e.isShiftDown()) // BR to avoid noise when changing flag color
                		misClick();
                // find next index (exploit that missing element returns -1, so set to 0)
                int index = 0;
                switch(e.getModifiersEx()) {
                    case 0:
                        index = systems.indexOf(currSys)+1;
                        if (index == systems.size())
                            index = 0;
                        break;
                    case 1:
                        index = systems.indexOf(currSys)-1;
                        if (index < 0)
                            index = systems.size()-1;
                        break;
                }
                useClickedSprite(systems.get(index), 1, false);
                //parent.parent.hoveringOverSprite(systems.get(index).sprite());
                parent.repaint();
                return;
            case KeyEvent.VK_H:
            	toggleShowFleetInfo();
            	parent.repaint();
                return;
			case KeyEvent.VK_1:
				detailPane().selectSpeed(1);
				return;
			case KeyEvent.VK_2:
				detailPane().selectSpeed(2);
				return;
			case KeyEvent.VK_3:
				detailPane().selectSpeed(3);
				return;
			case KeyEvent.VK_4:
				detailPane().selectSpeed(4);
				return;
			case KeyEvent.VK_5:
				detailPane().selectSpeed(5);
				return;
			case KeyEvent.VK_6:
				detailPane().selectSpeed(6);
				return;
			case KeyEvent.VK_7:
				detailPane().selectSpeed(7);
				return;
			case KeyEvent.VK_8:
				detailPane().selectSpeed(8);
				return;
			case KeyEvent.VK_9:
				detailPane().selectSpeed(9);
				return;
        }
    }
    private void initModel() {
        setBackground(MainUI.paneBackground());

        topPane = topPane();
        detailPane = detailPane();
        bottomPane = bottomPane();

        setLayout(new BorderLayout());
        if (topPane != null) {
            topPane.setPreferredSize(new Dimension(getWidth(),scaled(145)));
            add(topPane, BorderLayout.NORTH);
        }
        add(detailPane, BorderLayout.CENTER);
        if (bottomPane != null) {
            bottomPane.setPreferredSize(new Dimension(getWidth(),s40));
            add(bottomPane, BorderLayout.SOUTH);
        }
    }
    protected BasePanel topPane() {
        if (topPane == null)
            topPane = new FleetGraphicPane(this);
        return topPane;
    }
    protected FleetDetailPane detailPane() {
        if (detailPane == null)
            detailPane = new FleetDetailPane(this);
        return detailPane;
    }
    protected BasePanel bottomPane() {
        if (bottomPane == null)
            bottomPane = new FleetButtonPane(this);
        bottomPane.setBackground(MainUI.shadeBorderC());
        return bottomPane;
    }
    public final class FleetGraphicPane extends BasePanel implements MouseWheelListener {
        private static final long serialVersionUID = 1L;
        private final FleetPanel parent;
        public FleetGraphicPane(FleetPanel p){
            parent = p;
            init();
        }
        private void init() {
            setBackground(Color.black);
            addMouseWheelListener(this);
        }
		@Override public void paintComponent(Graphics g0) {
			try { paintFleetGraphicPane(g0); }
			catch (NullPointerException | ConcurrentModificationException e) {
				if (e instanceof ConcurrentModificationException)
					System.err.println("Concurrent Modification Exception while painting the Fleet Graphic Pane");
				else if (e instanceof NullPointerException)
					System.err.println("Null Pointer Exception while painting the Fleet Graphic Pane");
			}
		}
		private void paintFleetGraphicPane(Graphics g0) {
			// modnar: paint top of "Fleet Deployment" panel on main map screen
            Graphics2D g = (Graphics2D) g0;
            super.paintComponent(g);
            int w = getWidth();
            int h = getHeight();

            Empire pl = player();
            ShipFleet fl = parent.fleetToDisplay();

            // this can happen if the fleet is selected and then
            // all designs are scrapped before return to main ui
            if (fl.isEmpty()) {
                cancelFleet();
                return;
            }
            
            StarSystem sys = fl.isOrbiting() ? fl.system() : null;

            if (sys == null) {
                if (fl.hasDestination())
                    g.drawImage(pl.sv.starBackground(this), 0, 0, null);
            }
            else {
                g.drawImage(pl.sv.starBackground(this), 0, 0, null);
				//modnar: increase planet size, move star
                drawStar(g, sys.starType(), s80, w*3/4, s60);
                sys.planet().draw(g, w, h, s5, s70, s80*2, 45);
            }
            boolean contact = fl.empire().isPlayer() || pl.hasContacted(fl.empId());
            SpaceMonster monster = null;
            boolean isMonster = fl.empire().isMonster();
            // draw ship image
            Image shipImg;
            if (isMonster) {
            	monster = (SpaceMonster) fl;
            	shipImg = monster.image();
            }
            else if (contact)
            	shipImg = fl.empire().transport();
            else
            	shipImg = pl.transport();

            int imgW = shipImg.getWidth(null);
            int imgH = shipImg.getHeight(null);
            float scale = (float) s80 / Math.max(imgW, imgH);
            int shipW = (int) (scale*imgW);
            int shipH = (int) (scale*imgH);
            int shipX = s70;
            int shipY = h-shipH-s10;
			// modnar: one-step progressive image downscaling, slightly better
			// there should be better methods
			if (scale < 0.5) {
				BufferedImage tmp = new BufferedImage(imgW/2, imgH/2, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2D = tmp.createGraphics();
				g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2D.drawImage(shipImg, 0, 0, imgW/2, imgH/2, 0, 0, imgW, imgH, this);
				g2D.dispose();
				shipImg = tmp;
				imgW = shipImg.getWidth(null);
				imgH = shipImg.getHeight(null);
				scale = scale*2;
			}
			// modnar: use (slightly) better downsampling
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(shipImg, shipX,shipY,shipX+shipW,shipY+shipH, 0,0,imgW,imgH, null);

            // draw title
            // g.setFont(narrowFont(36));
            String str1;
            if (isMonster)
            	str1 = monster.name();
            else if (contact) {
                str1 = text("MAIN_FLEET_TITLE");
                str1 = fl.empire().replaceTokens(str1, "fleet");
            }
            else 
                str1 = text("MAIN_FLEET_TITLE_UNKNOWN");

            scaledFont(g, str1, w-s25, 36, 20);
            drawBorderedString(g, str1, 2, s15, s42, Color.black, SystemPanel.orangeText);
            // draw orbiting data, bottom up
            int y0 = h-s12;
            g.setColor(SystemPanel.whiteText);
            g.setFont(narrowFont(20));
            if (fl.launched() || ( fl.isDeployed() && !pl.knowETA(fl) )) {
                if (pl.knowETA(fl) && (fl.hasDestination())) {
                    String dest =  pl.sv.name(fl.destSysId());
                    String str2 = dest.isEmpty() ? text("MAIN_FLEET_DEST_UNSCOUTED") : text("MAIN_FLEET_DESTINATION", dest);
                    int sw2 = g.getFontMetrics().stringWidth(str2);
                    drawString(g,str2, w-sw2-s10, y0);
                    y0 -= s25;
                }
                String str3 = fl.retreating() ? text("MAIN_FLEET_RETREATING") : text("MAIN_FLEET_IN_TRANSIT");
                int sw3 = g.getFontMetrics().stringWidth(str3);
                drawString(g,str3, w-sw3-s10, y0);
                y0 -= s25;
                if (!fl.empire().isPlayer()) {
                    if (pl.alliedWith(fl.empId())) {
                        g.setColor(SystemPanel.greenText);
                        String str4 = text("MAIN_FLEET_ALLY");
                        int sw4 = g.getFontMetrics().stringWidth(str4);
                        drawString(g,str4, w-sw4-s10, y0);
                    } else if (pl.atWarWith(fl.empId())) {
                        g.setColor(SystemPanel.redText);
                        String str4 = text("MAIN_FLEET_ENEMY");
                        int sw4 = g.getFontMetrics().stringWidth(str4);
                        drawString(g,str4, w-sw4-s10, y0);
                    }
                }
            }
            else if (fl.isDeployed()) {
                String dest =  pl.sv.name(fl.destSysId());
                String str2 = dest.isEmpty() ? text("MAIN_FLEET_DEST_UNSCOUTED") : text("MAIN_FLEET_DESTINATION", dest);
                int sw2 = g.getFontMetrics().stringWidth(str2);
                drawString(g,str2, w-sw2-s10, y0);
                y0 -= s25;
                StarSystem sys1 = fl.system();
                String str3 = text("MAIN_FLEET_ORIGIN", pl.sv.name(sys1.id));
                int sw3 = g.getFontMetrics().stringWidth(str3);
                drawString(g,str3, w-sw3-s10, y0);
                y0 -= s25;
                String str4 = fl.retreating() && fl.empire().isPlayer() ? text("MAIN_FLEET_RETREATING") :text("MAIN_FLEET_DEPLOYED");
                int sw4 = g.getFontMetrics().stringWidth(str4);
                drawString(g,str4, w-sw4-s10, y0);
            }
            else {
                StarSystem sys1 = fl.system();
                String str2 = sys1 == null ? "" : text("MAIN_FLEET_LOCATION", pl.sv.name(sys1.id));
                if (str2.isEmpty()) 
                    log("ERROR: No system assigned to fleet ");             
                int sw2 = g.getFontMetrics().stringWidth(str2);
                drawString(g,str2, w-sw2-s10, y0);
                y0 -= s25;
                String str3 = text("MAIN_FLEET_IN_ORBIT");
                int sw3 = g.getFontMetrics().stringWidth(str3);
                drawString(g,str3, w-sw3-s10, y0);
            }
            g.setColor(MainUI.shadeBorderC());
            g.fillRect(0, h-s5, w, s5);
        }
        public void scrollToNextFleet(boolean forward) {
            ShipFleet fl = parent.fleetToDisplay();
            if (fl == null)
                return;

            List<ShipFleet> fleets = fl.empire().orderedFleets();

            int index = fleets.indexOf(fl);
            if (forward) 
                index = (index == (fleets.size()-1)) ? 0 : index + 1;
            else 
                index = (index == 0) ? fleets.size()-1 : index -1;

            IMapHandler topPanel = parent.parent.parent;
            topPanel.clickingOnSprite(fleets.get(index), 1, false, true, false, null);
            topPanel.map().recenterMapOn(fleets.get(index));
            topPanel.repaint();
        }
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            boolean up = e.getWheelRotation() > 0;
            scrollToNextFleet(up);
        }
    }
    public final class FleetDetailPane extends BasePanel implements MouseListener, MouseMotionListener, MouseWheelListener {
        private static final long serialVersionUID = 1L;
        private final Color fleetBackC = new Color(255,255,255,40);
        private BufferedImage starImg;
        private final FleetPanel parent;
        private final Color buttonBackC = new Color(30,30,30);
        private int hoverStackNum = -1;
        private Shape hoverBox, hoverBox2;
        private final Rectangle rallyBox	= new Rectangle();
        private final Rectangle retreatBox	= new Rectangle();
        private final Polygon	minAllBox	= new Polygon();
        private final Polygon	maxAllBox	= new Polygon();
        private final Rectangle	minAllBoxH	= new Rectangle();
        private final Rectangle	maxAllBoxH	= new Rectangle();
        private final Rectangle shipBox[]	= new Rectangle[ShipDesignLab.MAX_DESIGNS];
        private final Polygon	minBox[]	= new Polygon[ShipDesignLab.MAX_DESIGNS];
        private final Polygon	maxBox[]	= new Polygon[ShipDesignLab.MAX_DESIGNS];
        private final Polygon	downBox[]	= new Polygon[ShipDesignLab.MAX_DESIGNS];
        private final Polygon	upBox[]		= new Polygon[ShipDesignLab.MAX_DESIGNS];
        private final Rectangle minBoxH[]	= new Rectangle[ShipDesignLab.MAX_DESIGNS];
        private final Rectangle maxBoxH[]	= new Rectangle[ShipDesignLab.MAX_DESIGNS];
        private final Rectangle downBoxH[]	= new Rectangle[ShipDesignLab.MAX_DESIGNS];
        private final Rectangle upBoxH[]	= new Rectangle[ShipDesignLab.MAX_DESIGNS];
        protected Shape textureClip;
		private boolean showAdjust;

        public FleetDetailPane(FleetPanel p) {
            parent = p;
            init();
        }
        private void init() {
            for (int i=0;i<ShipDesignLab.MAX_DESIGNS;i++) {
                shipBox[i] = new Rectangle();
                minBox[i] = new Polygon();
                maxBox[i] = new Polygon();
                downBox[i] = new Polygon();
                upBox[i] = new Polygon();
                minBoxH[i] = new Rectangle();
                maxBoxH[i] = new Rectangle();
                downBoxH[i] = new Rectangle();
                upBoxH[i] = new Rectangle();
            }
            addMouseListener(this);
            addMouseMotionListener(this);
            addMouseWheelListener(this);
        }
		private void selectSpeed(int speed) {
			if (!showAdjust) {
				misClick();
				return;
			}
			int playerId = player().id;
			ShipFleet fl = selectedFleet();
			if (fl == null)
				return;
			for (int i=0; i<ShipDesignLab.MAX_DESIGNS; i++) {
				ShipDesign d = fl.visibleDesign(playerId, i);
				if(d!=null) {
					int index = d.id();
					if (d.warpSpeed() >= speed)
						stackAdjustment[index] = 0;
					else
						stackAdjustment[index] = 0-fl.num(index);
				}
			}
			adjustedFleet(newAdjustedFleet());
			softClick();
			repaint();
		}
        private void selectAll() {
        	int playerId = player().id;
        	ShipFleet fl = selectedFleet();
			if (fl == null)
				return;
        	for (int i=0; i<ShipDesignLab.MAX_DESIGNS; i++) {
        		ShipDesign d = fl.visibleDesign(playerId, i);
        		if(d!=null) {
        			int index = d.id();
                    stackAdjustment[index] = 0;
        		}
        	}
            adjustedFleet(newAdjustedFleet());
            softClick();
            repaint();
        }
        private void selectNone() {
        	int playerId = player().id;
        	ShipFleet fl = selectedFleet();
			if (fl == null)
				return;
        	for (int i=0; i<ShipDesignLab.MAX_DESIGNS; i++) {
        		ShipDesign d = fl.visibleDesign(playerId, i);
        		if(d!=null) {
        			int index = d.id();
                    stackAdjustment[index] = 0-fl.num(index);
        		}
        	}
            adjustedFleet(newAdjustedFleet());
            softClick();
            repaint();    	
        }
        @Override
        public String textureName()            { return TEXTURE_GRAY; }
        @Override
        public Shape textureClip()     { return textureClip; }
		@Override public void paintComponent(Graphics g0) {
			try { paintDetailPane(g0); }
			catch (NullPointerException | ConcurrentModificationException e) {
				if (e instanceof ConcurrentModificationException)
					System.err.println("Concurrent Modification Exception while painting the Fleet Detail Pane");
				else if (e instanceof NullPointerException)
					System.err.println("Null Pointer Exception while painting the Fleet Detail Pane");
			}
		}
        public void paintDetailPane(Graphics g0) {
            Graphics2D g = (Graphics2D) g0;
            super.paintComponent(g0);
            int w = getWidth();
            int h = getHeight();
            int h1 = s90;

            Empire pl = player();
            ShipFleet origFleet = parent.fleetToDisplay();
            if (origFleet == null)
                return;
            ShipFleet displayFleet = origFleet;
            // do we want to display an adjustable fleet based on selected fleet?
            boolean canAdjust = origFleet.canBeAdjustedBy(pl);
            if (canAdjust)
                displayFleet = adjustedFleet();

            if (displayFleet == null)
                displayFleet = origFleet;

            clearButtons();

            boolean sameFleet = (origFleet.empId() == displayFleet.empId())
			            		&& (origFleet.sysId() == displayFleet.sysId())
			            		&& (origFleet.destSysId() == displayFleet.destSysId());
            showAdjust = canAdjust && sameFleet;

            if (showAdjust)
                drawInfo(g,displayFleet, showAdjust, 0,0,w,h1);
            else
                drawInfo(g,origFleet, showAdjust, 0,0,w,h1);
            drawFleet(g,origFleet,displayFleet, showAdjust,0,h1,w,h-h1);
        }
        private void clearButtons() {
            for (int i=0;i<shipBox.length;i++) {
                shipBox[i].setBounds(0,0,0,0);
                minBox[i].reset();
                maxBox[i].reset();
                upBox[i].reset();
                downBox[i].reset();
                minBoxH[i].setBounds(0,0,0,0);
                maxBoxH[i].setBounds(0,0,0,0);
                upBoxH[i].setBounds(0,0,0,0);
                downBoxH[i].setBounds(0,0,0,0);
            }
            minAllBox.reset();
            maxAllBox.reset();
            minAllBoxH.setBounds(0,0,0,0);
            maxAllBoxH.setBounds(0,0,0,0);
        }
        private void drawInfo(Graphics2D g, ShipFleet displayFl, boolean showAdjust, int x, int y, int w, int h) {
            textureClip = new Rectangle2D.Float(x,y,w,h);
            g.setColor(MainUI.paneBackground());
            g.fillRect(x, y, w, h);
            // if (displayFl.empire().isMonster()) {
            // 	System.out.println("draw Monster Info");
            // }

            int x0 =s10;
            int y0 = s20;
            int lineH = s16;
            String title = displayFl.canBeSentBy(player()) ? text("MAIN_FLEET_DEPLOYMENT") : text("MAIN_FLEET_DISPLAY");
            // g.setFont(narrowFont(22));
            scaledFont(g, title, w-s20, 22, 15);
            drawShadowedString(g, title, 4, x0, y0, SystemPanel.textShadowC, Color.white);

            if (showAdjust) {
                int a[] = new int[3];
                int b[] = new int[3];
            	int ya = y0-s5;
            	int xa = x0+w-s40;

            	g.setFont(narrowFont(16));
                g.setColor(SystemPanel.blackText);
                String selectAll = text("MAIN_FLEET_SELECT_ALL");
                int wsa = s24;
                scaledFont(g, selectAll, wsa, 22, 15);
                int sw = g.getFontMetrics().stringWidth(selectAll);
                int dx = s3+(wsa-sw)/2;

                g.drawString(selectAll, xa+dx, ya+s15);
                scaledFont(g, title, w-s20, 22, 15);
                b[0]=ya-s6;  b[1]=ya-s12; b[2]=ya;
                // draw min all box
                Color c1 = hoverBox == minAllBoxH ? SystemPanel.yellowText : SystemPanel.blackText;
                g.setColor(c1);
                a[0]=xa+s5; a[1]=xa+s15; a[2]=xa+s15;
                minAllBox.addPoint(a[0], b[0]);
                minAllBox.addPoint(a[1], b[1]);
                minAllBox.addPoint(a[2], b[2]);
                g.fill(minAllBox);
                g.fillRect(a[0], b[1], s2, b[2]-b[1]);
                minAllBoxH.setBounds(xa+s5,ya-s20,s12,s23);
                // draw max all box
            	xa += s13;
                b[0]=ya-s6;  b[1]=ya-s12; b[2]=ya;
                c1 = hoverBox == maxAllBoxH ? SystemPanel.yellowText : SystemPanel.blackText;
                g.setColor(c1);
                a[0]=xa+s15; a[1]=xa+s5; a[2]=xa+s5;
                maxAllBox.addPoint(a[0], b[0]);
                maxAllBox.addPoint(a[1], b[1]);
                maxAllBox.addPoint(a[2], b[2]);
                g.fill(maxAllBox);
                g.fillRect(a[0]-s2, b[1], s2, b[2]-b[1]);
                maxAllBoxH.setBounds(xa+s5,ya-s20,s12,s23);
            }
            y0 += s6;
            y0 += lineH;

            StarSystem dest = parent.displayedDestination();
            g.setColor(SystemPanel.blackText);
            String text = null;
            nebulaText = null;
            String retreatText = null;
            String rallyText = null;
            if (displayFl.canBeSentBy(player())) {
                if (!displayFl.canSendTo(id(dest))) {
                    if (dest == null) {
                        StarSystem currDest = displayFl.destination();
                        if (currDest == null)
                            text = "";
                        else {
                            if (displayFl.empire().isPlayer()) {
                                retreatText = text("MAIN_FLEET_AUTO_RETREAT");
                                rallyText = text("MAIN_FLEET_SET_RALLY");
                            }
                            int dist = displayFl.travelTurnsAdjusted(currDest);
                            String destName = player().sv.name(currDest.id);
                            if (destName.isEmpty())
                                text = text("MAIN_FLEET_ETA_UNNAMED", dist);
                            else
                                text = text("MAIN_FLEET_ETA_NAMED", destName, dist);  
                        }
                    }
                    else {
                        String name = player().sv.name(dest.id);
                        g.setColor(SystemPanel.redText);
                        if (name.isEmpty())
                            text = text("MAIN_FLEET_INVALID_DESTINATION2");
                        else 
                            text = text("MAIN_FLEET_INVALID_DESTINATION", name);
                    }
                }
                else if (displayFl.isDeployed() || displayFl.inTransit()) {
                    if (displayFl.empire().isPlayer()) {
                        retreatText = text("MAIN_FLEET_AUTO_RETREAT");
                        rallyText = text("MAIN_FLEET_SET_RALLY");
                    }
                    dest = dest == null ? displayFl.destination() : dest;
                    int dist = displayFl.travelTurnsAdjusted(dest);
                    String destName = player().sv.name(dest.id);
                    if (destName.isEmpty())
                        text = text("MAIN_FLEET_ETA_UNNAMED", dist);
                    else
                        text = text("MAIN_FLEET_ETA_NAMED", destName, dist);
                    if ((dist > 1) && displayFl.passesThroughNebula(dest))
                        nebulaText = text("MAIN_FLEET_THROUGH_NEBULA", displayFl.speedInNebulaeStr());
                }
                else if (displayFl.canSendTo(id(dest))) {
                    int dist = 0;
                    if (displayFl.canReach(dest)) {
                        dist = displayFl.travelTurnsAdjusted(dest);
                        String destName = player().sv.name(dest.id);
                        if (destName.isEmpty())
                            text = text("MAIN_FLEET_ETA_UNNAMED", dist);
                        else
                            text = text("MAIN_FLEET_ETA_NAMED", destName, dist);
                    }
                    else {
                        dist = player().rangeTo(dest);
                        text = text("MAIN_FLEET_OUT_OF_RANGE_DESC", dist);
                    }
                    if ((dist > 1) && displayFl.passesThroughNebula(dest))
                        nebulaText = text("MAIN_FLEET_THROUGH_NEBULA", displayFl.speedInNebulaeStr());
                }
                else if (displayFl.isOrbiting()) {
                    text = text("MAIN_FLEET_CHOOSE_DEST");
                }
            }
            else if (displayFl.inTransit() || displayFl.isDeployed()) {
                if (displayFl.empire().isPlayer()) {
                    retreatText = text("MAIN_FLEET_AUTO_RETREAT");
                    rallyText = text("MAIN_FLEET_SET_RALLY");
                }
                if (player().knowETA(displayFl)) {
                    int dist = displayFl.travelTurnsRemainingAdjusted();
                    if (displayFl.hasDestination()) {
                        String destName = player().sv.name(displayFl.destSysId());
                        if (destName.isEmpty())
                            text = text("MAIN_FLEET_ETA_UNNAMED", dist);
                        else
                            text = text("MAIN_FLEET_ETA_NAMED", destName, dist);
                    }
                }
                else {
                    g.setColor(SystemPanel.redText);
                    text = text("MAIN_FLEET_ETA_UNKNOWN");
                }
            }
            if (text != null) {
            	scaledFont(g, text, w-s30, 16, 10);
            	drawString(g ,text, x0, y0);
                y0 += lineH;
            }
            g.setFont(narrowFont(16));
//            if (text != null) {
//                List<String> lines = wrappedLines(g, text, w-s30);
//                for (String line: lines) {
//                    drawString(g,line, x0, y0);
//                    y0 += lineH;
//                }
//            }

            if (rallyText != null) {
                y0 += lineH/2;
                int checkW = s12;
                int checkX = x0;
                rallyBox.setBounds(checkX, y0-checkW, checkW, checkW);
                Stroke prev = g.getStroke();
                g.setStroke(stroke2);
                g.setColor(MainUI.shadeBorderC());
                g.fill(rallyBox);
                if (hoverBox == rallyBox) {
                    g.setColor(Color.yellow);
                    g.draw(rallyBox);
                }
                if (displayFl.isRallied()) {
                    g.setColor(SystemPanel.whiteText);
                    g.drawLine(checkX-s1, y0-s6, checkX+s3, y0-s3);
                    g.drawLine(checkX+s3, y0-s3, checkX+checkW, y0-s12);
                }
                g.setStroke(prev);
                g.setColor(SystemPanel.blackText);
                int indent = checkW+s6;
            	scaledFont(g, rallyText, w-s30-indent, 16, 10);
                drawString(g, rallyText, x0+indent, y0);
            }
            if (retreatText != null) {
                y0 += lineH;
                int checkW = s12;
                int checkX = x0;
                retreatBox.setBounds(checkX, y0-checkW, checkW, checkW);
                Stroke prev = g.getStroke();
                g.setStroke(stroke2);
                g.setColor(MainUI.shadeBorderC());
                g.fill(retreatBox);
                if (hoverBox == retreatBox) {
                    g.setColor(Color.yellow);
                    g.draw(retreatBox);
                }
                if (displayFl.retreatOnArrival()) {
                    g.setColor(SystemPanel.whiteText);
                    g.drawLine(checkX-s1, y0-s6, checkX+s3, y0-s3);
                    g.drawLine(checkX+s3, y0-s3, checkX+checkW, y0-s12);
                }
               g.setStroke(prev);
                g.setColor(SystemPanel.blackText);
                int indent = checkW+s6;
                // BR: Line in excess are not managed!
            	scaledFont(g, retreatText, w-s30-indent, 16, 10);
            	drawString(g ,retreatText, x0+indent, y0);
            	y0 += lineH;
//                List<String> lines = wrappedLines(g, retreatText, w-s30, indent);
//                for (String line: lines) {
//                    drawString(g,line, x0+indent, y0);
//                    indent = 0;
//                    y0 += lineH;
//                }
            }
        }
        private void drawFleet(Graphics2D g, ShipFleet origFl, ShipFleet displayFl, boolean showAdjust, int x, int y, int w, int h) {
            // draw star background
            g.setColor(Color.black);
            g.fillRect(x, y, w, h);
            if (starImg == null) {
                starImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                drawBackgroundStars(starImg, null);
            }
            g.drawImage(starImg,x,y,null);
            
            int spacing = s15;
            // figure out size of ships
            int shipW = w/2;
            int shipH = (h/3)-spacing-spacing; // give room for text above/below ship
            if  ((shipH *3/2) <  shipW)
                shipW = shipH * 3/2;
            else
                shipH = shipW * 2/3;

            int xAdj = (w-(shipW*2))/3;
            int yAdj = (h-shipH*2)/3;
            int midX = x+(w/2);
            int midY = y+(h/2);
            int leftX = x+(shipW/2);
            int rightX = x+w-(shipW/2);

            int topY = y+(shipH/2)+spacing;
            int botY = y+h-(shipH/2)-spacing;

            // get count of all stacks based on design visibility
            int[] visible = origFl.visibleShips(player().id);
            // count how many of those visible designs have ships
            int num = 0;
            for (int cnt: visible) {
                if (cnt > 0)
                    num++;
            }
            
            boolean contact = origFl.empire().isPlayer() 
            					|| origFl.empire().isMonster()
            					|| player().hasContacted(origFl.empId());
            switch(num) {
                case 0:
                    break;
                case 1:
                    drawShip(g, origFl, displayFl, showAdjust, contact, 0, midX, midY, shipW, shipH);
                    break;
                case 2:
                    drawShip(g, origFl, displayFl, showAdjust, contact, 0, leftX+xAdj, topY+yAdj, shipW, shipH);
                    drawShip(g, origFl, displayFl, showAdjust, contact, 1, rightX-xAdj, botY-yAdj, shipW, shipH);
                    break;
                case 3:
                    drawShip(g, origFl, displayFl, showAdjust, contact, 0, leftX, topY, shipW, shipH);
                    drawShip(g, origFl, displayFl, showAdjust, contact, 1, midX, midY, shipW, shipH);
                    drawShip(g, origFl, displayFl, showAdjust, contact, 2, rightX, botY, shipW, shipH);
                    break;
                case 4:
                    drawShip(g, origFl, displayFl, showAdjust, contact, 0, leftX+xAdj, topY+yAdj, shipW, shipH);
                    drawShip(g, origFl, displayFl, showAdjust, contact, 1, rightX-xAdj, topY+yAdj, shipW, shipH);
                    drawShip(g, origFl, displayFl, showAdjust, contact, 2, leftX+xAdj, botY-yAdj, shipW, shipH);
                    drawShip(g, origFl, displayFl, showAdjust, contact, 3, rightX-xAdj, botY-yAdj, shipW, shipH);
                    break;
                case 5:
                    drawShip(g, origFl, displayFl, showAdjust, contact, 0, leftX, topY, shipW, shipH);
                    drawShip(g, origFl, displayFl, showAdjust, contact, 1, rightX, topY, shipW, shipH);
                    drawShip(g, origFl, displayFl, showAdjust, contact, 2, midX, midY, shipW, shipH);
                    drawShip(g, origFl, displayFl, showAdjust, contact, 3, leftX, botY, shipW, shipH);
                    drawShip(g, origFl, displayFl, showAdjust, contact, 4, rightX, botY, shipW, shipH);
                    break;
                case 6:
                default:
                    drawShip(g, origFl, displayFl, showAdjust, contact, 0, leftX+xAdj, topY, shipW, shipH);
                    drawShip(g, origFl, displayFl, showAdjust, contact, 1, rightX-xAdj, topY, shipW, shipH);
                    drawShip(g, origFl, displayFl, showAdjust, contact, 2, leftX+xAdj, midY, shipW, shipH);
                    drawShip(g, origFl, displayFl, showAdjust, contact, 3, rightX-xAdj, midY, shipW, shipH);
                    drawShip(g, origFl, displayFl, showAdjust, contact, 4, leftX+xAdj, botY, shipW, shipH);
                    drawShip(g, origFl, displayFl, showAdjust, contact, 5, rightX-xAdj, botY, shipW, shipH);
                    break;
            }
            int y0= y+s15;
            g.setFont(narrowFont(15));
            if (nebulaText != null) {
                g.setColor(SystemPanel.redText);
                List<String> lines = wrappedLines(g, nebulaText, w-s30);
                for (String line: lines) {
                    drawString(g,line, s15, y0);
                    y0 += s14;
                }
            }
        }
        private void drawShip(Graphics2D g, ShipFleet origFl, ShipFleet displayFl, boolean canAdjust, boolean contact, int i, int x0, int y0, int w, int h) {
			// modnar: draw ship design icons in "Fleet Deployment" panel on main map screen
            int x = x0-w/2;
            int y = y0-h/2;
            g.setColor(fleetBackC);
            Stroke prev = g.getStroke();
            g.setStroke(stroke2);
            g.drawRoundRect(x,y-s10,w,h+s20,s20,s20);
            g.setStroke(prev);

            Empire pl = player();
            ShipDesign d = origFl.visibleDesign(pl.id,i);
            Image img = d.image();
            if (!contact) {
                String iconKey = ShipLibrary.current().shipKey(pl.shipLab().shipStyleIndex(), d.size(), 0);
                img = icon(iconKey).getImage();
            }
            int imgW = img.getWidth(null);
            int imgH = img.getHeight(null);
            float scale = min((float)w/imgW, (float)h/imgH);

            int w1 = (int)(scale*imgW);
            int h1 = (int)(scale*imgH);

            int x1 = x+((w-w1)/2);
            int y1 = y+((h-h1)/2);
			// modnar: one-step progressive image downscaling, slightly better
			// there should be better methods
			if (scale < 0.5) {
				BufferedImage tmp = new BufferedImage(imgW/2, imgH/2, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2D = tmp.createGraphics();
				g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2D.drawImage(img, 0, 0, imgW/2, imgH/2, 0, 0, imgW, imgH, this);
				g2D.dispose();
				img = tmp;
				imgW = img.getWidth(null);
				imgH = img.getHeight(null);
				scale = scale*2;
			}
			// modnar: use (slightly) better downsampling
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(img, x1, y1, x1+w1, y1+h1, 0, 0, imgW, imgH, parent);

            // draw ship name
            if (contact) {
                scaledFont(g, d.name(), w-s5, 18, 9);
                //g.setFont(narrowFont(18));
                int sw = g.getFontMetrics().stringWidth(d.name());
                int x2 = x+((w-sw)/2);
                g.setColor(SystemPanel.grayText);
                drawString(g,d.name(), x2, y+s5);
            }

            int y3 = y+h+s7;

            Color c0 = shipBox[i] == hoverBox ? SystemPanel.yellowText : SystemPanel.grayText;
            Color c1;

            int a[] = new int[3];
            int b[] = new int[3];

            // draw adjustment arrows
            if (canAdjust) {
                g.setColor(buttonBackC);
                g.fillRoundRect(x, y3-s15, w, s18, s20, s20);
                g.setColor(c0);
                b[0]=y3-s6;  b[1]=y3-s12; b[2]=y3;
                // draw min box
                c1 = hoverBox2 == minBoxH[i] ? SystemPanel.yellowText : SystemPanel.grayText;
                g.setColor(c1);
                a[0]=x+s5; a[1]=x+s15; a[2]=x+s15;
                minBox[i].addPoint(a[0], b[0]);
                minBox[i].addPoint(a[1], b[1]);
                minBox[i].addPoint(a[2], b[2]);
                g.fill(minBox[i]); g.fillRect(a[0], b[1], s2, b[2]-b[1]);
                minBoxH[i].setBounds(x+s5,y3-s20,s12,s23);
                // draw left box
                c1 = hoverBox2 == downBoxH[i] ? SystemPanel.yellowText : SystemPanel.grayText;
                g.setColor(c1);
                a[0]=x+s17; a[1]=x+s27; a[2]=x+s27;
                downBox[i].addPoint(a[0], b[0]);
                downBox[i].addPoint(a[1], b[1]);
                downBox[i].addPoint(a[2], b[2]);
                g.fill(downBox[i]);
                downBoxH[i].setBounds(x+s17,y3-s20,s12,s23);
                // draw max box
                c1 = hoverBox2 == maxBoxH[i] ? SystemPanel.yellowText : SystemPanel.grayText;
                g.setColor(c1);
                a[0]=x+w-s5; a[1]=x+w-s15; a[2]=x+w-s15;
                maxBox[i].addPoint(a[0], b[0]);
                maxBox[i].addPoint(a[1], b[1]);
                maxBox[i].addPoint(a[2], b[2]);
                g.fill(maxBox[i]); g.fillRect(a[0]-s2, b[1], s2, b[2]-b[1]);
                maxBoxH[i].setBounds(x+w-s15,y3-s20,s12,s23);
                // draw up box
                c1 = hoverBox2 == upBoxH[i] ? SystemPanel.yellowText : SystemPanel.grayText;
                g.setColor(c1);
                a[0]=x+w-s17; a[1]=x+w-s27; a[2]=x+w-s27;
                upBox[i].addPoint(a[0], b[0]);
                upBox[i].addPoint(a[1], b[1]);
                upBox[i].addPoint(a[2], b[2]);
                g.fill(upBox[i]);
                upBoxH[i].setBounds(x+w-s27,y3-s20,s12,s23);
            }

            // draw ship count
            g.setColor(c0);
            // format ship count
            int count2 = origFl.num(d.id());
            int count1 = canAdjust ? displayFl.num(d.id()) : count2;
            String s = count1 == count2 ? str(count1) : text("MAIN_FLEET_SHIP_COUNT", count1,count2);
            this.scaledFont(g, s, w-s60, 18, 12);
            int sw3 = g.getFontMetrics().stringWidth(s);
            int x3 = x+((w-sw3)/2);
            drawString(g,s, x3, y3);

            // if hovering, draw highlight frame
            if (hoverBox == shipBox[i]) {
                prev = g.getStroke();
                g.setStroke(stroke1);
                g.setColor(SystemPanel.yellowText);
                g.drawRoundRect(x,y-s10,w,h+s20,s20,s20);
                g.setStroke(prev);
            }
            shipBox[i].setBounds(x,y-s10,w,h+s20);
        }
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int count = e.getUnitsToScroll();
            if (count == 0)
                return;
            if (hoverStackNum < 0)
                return;

            ShipFleet fl = selectedFleet();
            if (fl == null)
                return;

            ShipDesign d = fl.visibleDesign(player().id, hoverStackNum);
            if (d == null)
                return;
            int index = d.id();
            int stackNum = fl.num(index);
            int currAdj = stackAdjustment[index];
            int n = stackNum+currAdj;
            int delta = n>30000 ? 10000 : (n>3000 ? 1000 : (n>300 ? 100 : (n>30 ? 10:1)));
            if (count > 0)
                stackAdjustment[index] = max(0-stackNum, currAdj-delta);
            else if (count < 0)
                stackAdjustment[index] = min(0, currAdj+delta);

            adjustedFleet(newAdjustedFleet());
            if (((stackNum + currAdj) == 0)
            || (stackNum + stackAdjustment[index]) == 0)
                repaint();
            else if (currAdj != stackAdjustment[index])
                repaint();
        }
        @Override
        public void mouseDragged(MouseEvent e) { }
        @Override
        public void mouseMoved(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            Shape prevHover = hoverBox;
            Shape prevHover2 = hoverBox2;
            hoverBox = null;
            hoverBox2 = null;
            
            if (retreatBox.contains(x,y)) 
                hoverBox = retreatBox;
            else if (rallyBox.contains(x,y)) 
                hoverBox = rallyBox;
            else if (minAllBoxH.contains(x,y)) 
                hoverBox = minAllBoxH;
            else if (maxAllBoxH.contains(x,y)) 
                hoverBox = maxAllBoxH;
           
            hoverStackNum = -1;
            for (int i=0;i<shipBox.length;i++) {
                if (shipBox[i].contains(x,y)) {
                    hoverBox = shipBox[i];
                    hoverStackNum = i;
                }
                if (minBoxH[i].contains(x,y))
                    hoverBox2 = minBoxH[i];
                if (downBoxH[i].contains(x,y))
                    hoverBox2 = downBoxH[i];
                if (upBoxH[i].contains(x,y))
                    hoverBox2 = upBoxH[i];
                if (maxBoxH[i].contains(x,y))
                    hoverBox2 = maxBoxH[i];
            }
            if ((hoverBox != prevHover)
            || (hoverBox2 != prevHover2))
                repaint();
        }
        @Override
        public void mouseClicked(MouseEvent e) { }
        @Override
        public void mouseEntered(MouseEvent e) { }
        @Override
        public void mouseExited(MouseEvent e) {
            if ((hoverBox != null) || (hoverBox2 != null)){
                hoverBox = null;
                hoverBox2 = null;
                repaint();
            }
        }
        @Override
        public void mousePressed(MouseEvent e) { }
        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() > 3)
                return;
            int x = e.getX();
            int y = e.getY();
            ShipFleet fl = selectedFleet();
            // selectedFleet can be null if hovering with mouse
            if (fl == null)
                return;
            
            if (retreatBox.contains(x,y)) {
                fl.toggleRetreatOnArrival();
                softClick();
                repaint();
                return;
            }
            if (rallyBox.contains(x,y)) {
                fl.toggleRally();
                softClick();
                repaint();
                return;
            }
            if (minAllBoxH.contains(x,y)) {
            	selectNone();
            	return;
            }
            if (maxAllBoxH.contains(x,y)) {
            	selectAll();
            	return;
            }

            if (hoverStackNum < 0)
                return;
            
            ShipDesign d = fl.visibleDesign(player().id, hoverStackNum);
            int index = d.id();
            int stackNum = fl.num(index);
            int currAdj = stackAdjustment[index];
            int newAdj = 1;
            boolean shiftPressed = e.isShiftDown();
            boolean ctrlPressed = e.isControlDown();
            
            int adjAmt = 1;
            if (shiftPressed)
                adjAmt = 5;
            else if (ctrlPressed)
                adjAmt = 20;
 
            for (int i=0;i<shipBox.length;i++) {
                if (minBoxH[i].contains(x,y))
                    newAdj = 0-stackNum;
                else if (downBox[i].contains(x,y))
                    newAdj = max(currAdj-adjAmt, 0-stackNum);
                else if (upBox[i].contains(x,y))
                    newAdj = min(currAdj+adjAmt, 0);
                else if (maxBox[i].contains(x,y))
                            newAdj = 0;
            }

            // nothing in click range
            if (newAdj > 0)
                return;

            if (newAdj == stackAdjustment[index])
                misClick();
            else {
                stackAdjustment[index] = newAdj;
                adjustedFleet(newAdjustedFleet());
                softClick();
                repaint();
            }
        }
    }

    public String sendFleet(StarSystem sys) {
        // special case check:
        // on Cancel, then selected fleet is null and we get
        // here when the last selected system is reselected
        if (selectedFleet() == null)
            return "Error selected Fleet is null";
        if (selectedFleet().empire() != player())
            return "Error selected Fleet is not owned by the player";
        if (selectedFleet().destSysId() == sys.id) 
            return "Error Fleet already at destination";;

        tentativeDest(sys);
        // don't accept clicks for out of range systems
        // but consume the click (to stay on this view)
        ShipFleet adjustedFleet = adjustedFleet();
        if (adjustedFleet == null) 
            return "Error adjusted Fleet is null";
        if (!adjustedFleet.canReach(sys))
            return "Error Fleet can not reach destination system";
        if (!adjustedFleet.canSendTo(id(sys)))
            return "Error Fleet can not be sent";

        selectedDest(sys);
        adjustedFleet.use(sys, parent.parent);
        sendFleet();
        return "Fleet sent successfully";
    }
    public boolean newAdjustedFleet(List<Integer> counts) { // For VIP Console
    	ShipFleet selectedFleet = selectedFleet();
    	if (selectedFleet == null)
    		return false;
    	for (int i=0; i<ShipDesignLab.MAX_DESIGNS; i++) {
    		int stackNum = selectedFleet.num(i);
            stackAdjustment[i] = 0-stackNum;
    	}
    	// Update the stackAdjustment
    	for (int i=0; i<counts.size(); i++) {
    		ShipDesign d = selectedFleet.visibleDesign(player().id, i);
    		if (d == null)
    			break;
            int index    = d.id();
            int stackNum = selectedFleet.num(index);
            int toSend   = counts.get(i);
            int toKeep   = max(0, stackNum - toSend);
            int newAdj   = 0-toKeep;
            stackAdjustment[index] = newAdj;
    	}
    	// Build the temporary fleet
    	adjustedFleet(newAdjustedFleet());
    	return true;
    }
    public final class FleetButtonPane extends BasePanel implements MouseListener, MouseMotionListener {
        private static final long serialVersionUID = 1L;
        private final FleetPanel parent;
        private final Color buttonShadowC = new Color(33,33,33);
        int leftM, midM1, midM2, rightM;
        private LinearGradientPaint fullGrayBackC;
        private LinearGradientPaint largeGreenBackC;
        private LinearGradientPaint largeRedBackC;
        private LinearGradientPaint smallGrayBackC;
        private boolean initted = false;

        private Shape hoverBox;
        private final Rectangle cancelBox = new Rectangle();
        private final Rectangle deployBox = new Rectangle();
        private final Rectangle undeployBox = new Rectangle();
        public FleetButtonPane(FleetPanel p) {
            parent = p;
            init();
        }
        @Override
        public String textureName()            { return TEXTURE_GRAY; }

        private void init() {
            addMouseListener(this);
            addMouseMotionListener(this);
        }
        private void initGradients() {
            initted = true;
            int w = getWidth();
            leftM = s2;
            midM1 = (w*3/5)-s2;
            midM2 = midM1+s4;
            rightM = w-s2;
            Point2D start = new Point2D.Float(leftM, 0);
            Point2D mid1 = new Point2D.Float(midM1, 0);
            Point2D mid2 = new Point2D.Float(midM2, 0);
            Point2D end = new Point2D.Float(rightM, 0);
            float[] dist = {0.0f, 0.5f, 1.0f};

            Color grayEdgeC = new Color(59,59,59);
            Color grayMidC = new Color(92,92,92);
            Color[] grayColors = {grayEdgeC, grayMidC, grayEdgeC };

            Color greenEdgeC = new Color(44,59,30);
            Color greenMidC = new Color(71,93,48);
            Color[] greenColors = {greenEdgeC, greenMidC, greenEdgeC };

            Color redEdgeC = new Color(92,20,20);
            Color redMidC = new Color(117,42,42);
            Color[] redColors = {redEdgeC, redMidC, redEdgeC };

            fullGrayBackC = new LinearGradientPaint(start, end, dist, grayColors);
            smallGrayBackC = new LinearGradientPaint(mid2, end, dist, grayColors);
            largeGreenBackC = new LinearGradientPaint(start, mid1, dist, greenColors);
            largeRedBackC = new LinearGradientPaint(start, mid1, dist, redColors);
        }
		@Override public void paintComponent(Graphics g0) {
			try { paintButtonPane(g0); }
			catch (NullPointerException | ConcurrentModificationException e) {
				if (e instanceof ConcurrentModificationException)
					System.err.println("Concurrent Modification Exception while painting the Fleet Button Pane");
				else if (e instanceof NullPointerException)
					System.err.println("Null Pointer Exception while painting the Fleet Button Pane");
			}
		}
		public void paintButtonPane(Graphics g0) {
            Graphics2D g = (Graphics2D) g0;
            super.paintComponent(g);

            if (!initted)
                initGradients();

            ShipFleet fleet = parent.adjustedFleet();
            if (fleet == null)
                return;

            clearButtons();
            if (!fleet.empire().isPlayer()) {
                drawFullCancelButton(g);
                return;
            }

            StarSystem dest = parent.displayedDestination();
            if (dest != null) {
                if (!fleet.canReach(dest))  {
                    drawFullOutOfRangeButton(g);
                    return;
                }
                else if (!fleet.canSendTo(id(dest))) {
                    if (fleet.retreating()) 
                        drawFullInvalidRetreatButton(g);
                    else
                        drawFullCancelButton(g);
                    return;
                }
                else {
                    drawLargeDeployButton(g);
                    drawSmallCancelButton(g);
                    return;
                }
            }
            if (fleet.canUndeploy()) {
                drawLargeUndeployButton(g);
                drawSmallCancelButton(g);
                return;
            }
            drawFullCancelButton(g);
        }
        private void clearButtons() {
            cancelBox.setBounds(0,0,0,0);
            deployBox.setBounds(0,0,0,0);
            undeployBox.setBounds(0,0,0,0);
        }
        private void drawFullOutOfRangeButton(Graphics2D g) {
            drawButton(g,fullGrayBackC,text("MAIN_FLEET_OUT_OF_RANGE"), cancelBox, leftM, rightM);
        }
        private void drawFullInvalidRetreatButton(Graphics2D g) {
            drawButton(g,fullGrayBackC,text("MAIN_FLEET_INVALID_RETREAT"), cancelBox, leftM, rightM);
        }
        private void drawFullCancelButton(Graphics2D g) {
            drawButton(g,fullGrayBackC,text("MAIN_FLEET_CANCEL"), cancelBox, leftM, rightM);
        }
        private void drawLargeUndeployButton(Graphics2D g) {
            drawButton(g,largeRedBackC,text("MAIN_FLEET_UNDEPLOY_FLEET"), undeployBox, leftM, midM1);
        }
        private void drawLargeDeployButton(Graphics2D g) {
            drawButton(g,largeGreenBackC,text("MAIN_FLEET_DEPLOY_FLEET"), deployBox, leftM, midM1);
        }
        private void drawSmallCancelButton(Graphics2D g) {
            drawButton(g,smallGrayBackC,text("MAIN_FLEET_CANCEL"), cancelBox, midM2, rightM);
        }
        private void drawButton(Graphics2D g, LinearGradientPaint gradient, String label, Rectangle actionBox, int x1, int x2) {
            int y = s4;
            int h = getHeight()-s7;
            int w = x2 - x1;
            if (actionBox != null)
                actionBox.setBounds(x1,y,w,h);
            g.setColor(buttonShadowC);
            Stroke prev = g.getStroke();
            g.setStroke(stroke2);
            g.drawRoundRect(x1+s3,y+s2,w-s2,h,s10,s10);
            g.setStroke(prev);

            g.setPaint(gradient);
            g.fillRoundRect(x1,y,w,h,s10,s10);

            boolean hovering = (actionBox != null) && (actionBox == hoverBox);
            Color c0 = hovering ? SystemPanel.yellowText : SystemPanel.whiteText;

            //g.setFont(narrowFont(22));
            scaledFont(g, label, w-s10, 22, 14);
            int sw = g.getFontMetrics().stringWidth(label);
            int x0 = x1+((w-sw)/2);
            drawShadowedString(g, label, 3, x0, y+h-s11, SystemPanel.textShadowC, c0);

            g.setColor(c0);
            Stroke prev2 = g.getStroke();
            g.setStroke(stroke2);
            g.drawRoundRect(x1+s1,y,w-s2,h,s10,s10);
            g.setStroke(prev2);
        }
        @Override
        public void mouseDragged(MouseEvent e) { }
        @Override
        public void mouseMoved(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            Shape prevHover = hoverBox;
            hoverBox = null;
            if (cancelBox.contains(x,y))
                hoverBox = cancelBox;
            else if (deployBox.contains(x,y))
                hoverBox = deployBox;
            else if (undeployBox.contains(x,y))
                hoverBox = undeployBox;

            if (hoverBox != prevHover)
                repaint();
        }
        @Override
        public void mouseClicked(MouseEvent e) { }
        @Override
        public void mouseEntered(MouseEvent e) { }
        @Override
        public void mouseExited(MouseEvent e) {
            if (hoverBox != null) {
                hoverBox = null;
                repaint();
            }
        }
        @Override
        public void mousePressed(MouseEvent e) { }
        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() > 3)
                return;
            int x = e.getX();
            int y = e.getY();

            if (cancelBox.contains(x,y)) {
                softClick();
                parent.cancelFleet();
            }
            else if (deployBox.contains(x,y)) {
                softClick();
                parent.sendFleet();
            }
            else if (undeployBox.contains(x,y)) {
                softClick();
                parent.undeployFleet();
            }
        }
    }
}
