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
package rotp.model.galaxy;

import static rotp.model.ships.ShipDesignLab.MAX_DESIGNS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rotp.model.colony.ColonyShipyard;
import rotp.model.empires.Empire;
import rotp.model.game.IGameOptions;
import rotp.model.ships.ShipDesign;
import rotp.model.ships.ShipDesignLab;
import rotp.ui.sprites.ShipRelocationSprite;
import rotp.util.Base;

public class Ships implements Base, Serializable {
    private static final long serialVersionUID = 1L;
    public static boolean rallyTransitJoinCombat = false;
    private final List<ShipFleet> allFleets = new ArrayList<>();
    private List<ShipFleet> allFleetsCopy() { return new ArrayList<>(allFleets); }

    public void rallyOrbitingShips(int empId, int sysId, int rallySysId, ShipFleet rallyCopy, ShipFleet orbitCopy) {
        ShipFleet orbitingFleet = orbitingFleet(empId, sysId);
        if (orbitingFleet == null)
            return;   // no ships in orbit to rally

        ShipFleet rallyingFleet = rallyingFleet(empId, sysId, rallySysId);
        // if none, create one
        boolean newFleet = rallyingFleet == null;
        if (newFleet) {
            StarSystem sys = galaxy().system(sysId);
            rallyingFleet = new ShipFleet(empId, sys);
            rallyingFleet.rallySysId(rallySysId);
        }
        IGameOptions opts = options();
        boolean defenseGetLoss = opts.rallyLossDefense();
        boolean rallyGetLoss   = opts.rallyLossRally();

        // adjust the new rally size
        for (int i=0; i<MAX_DESIGNS; i++) {
            int orbitingCount = orbitingFleet.num(i);
            int rallyCount    = rallyCopy.num(i);
            int oldOrbitCount = orbitCopy.num(i);
            int defenseCount  = oldOrbitCount - rallyCount;
            if (orbitingCount < oldOrbitCount) { // some loss
            	if (defenseGetLoss) {
            		rallyCount   = min (rallyCount, orbitingCount);
            		defenseCount = orbitingCount - rallyCount;
            	} else if (rallyGetLoss) {
            		defenseCount = min (defenseCount, orbitingCount);
            		rallyCount   = orbitingCount - defenseCount;            		
            	} else {
            		rallyCount   = orbitingCount * rallyCount / oldOrbitCount;
            		defenseCount = orbitingCount - rallyCount;
            	}
            }
			if (rallyCount > 0) {
				rallyingFleet.addShips(i, rallyCount);
				orbitingFleet.removeShips(i, rallyCount, true);
			}
        	orbitCopy.num(i, defenseCount);
        }
        // Deploy fleet if new fleet
        if (newFleet && rallyingFleet.numShips() >0) {
            rallyingFleet.makeDeployed();
            allFleets.add(rallyingFleet);
        }
    }
    public void rallyOrbitingShips(int empId, int sysId, int designId, int count, int rallySysId) {
        ShipFleet orbitingFleet = orbitingFleet(empId, sysId);
        if (orbitingFleet == null)
            return;   // no ships in orbit to rally

        int rallyCount = min(count, orbitingFleet.num(designId));

        if (rallyCount == 0)
            return;   // no orbiting ships of this design to rally

        // check for an existing rally fleet
        ShipFleet rallyingFleet = rallyingFleet(empId, sysId, rallySysId);

        // if none, create one
        if (rallyingFleet == null) {
            StarSystem sys = galaxy().system(sysId);
            rallyingFleet = new ShipFleet(empId, sys);
            rallyingFleet.rallySysId(rallySysId);
            rallyingFleet.makeDeployed();
            allFleets.add(rallyingFleet);
        }

        // move rallyCount ships from orbitingFleet to rallyingFleet
        rallyingFleet.addShips(designId, rallyCount);  
        orbitingFleet.removeShips(designId, rallyCount, true);
    }
    private ShipFleet forwardRallyFleet(ShipFleet fl, int empId, int sysId, int rallySysId) {
        ShipFleet existingFleet = rallyingFleet(empId, sysId, rallySysId);

        if (existingFleet == null) {
            existingFleet = fl;
            existingFleet.rallySysId(rallySysId);
            existingFleet.destSysId(rallySysId);
            existingFleet.makeDeployed();
        }
        else {
            existingFleet.addFleet(fl);
            fl.disband();
        }
        return existingFleet;
    }
    public void buildShips(int empId, int sysId, int designId, int count) {
        // are we relocating new ships? If so, do so as long as dest is still allied with us
        ShipFleet existingFleet = orbitingFleet(empId, sysId);

        if (existingFleet == null) {
            StarSystem sys = galaxy().system(sysId);
            existingFleet = new ShipFleet(empId, sys);
            existingFleet.makeOrbiting();
            allFleets.add(existingFleet);
        }
        existingFleet.addShips(designId, count);
    }
 /*   public void deployShips(int empId, int sysId, int[] counts, int destSysId) {    
        // get orbiting fleet to pull ships from
        ShipFleet orbitingFleet = orbitingFleet(empId, sysId);
        if (orbitingFleet == null) {
            err("Attempting to deploy fleet from a system that has no orbiting fleet");
            return;
        }
        
        // adjust ship counts
        int[] actual = new int[counts.length];
        int totalDeployed = 0;
        int totalOrbiting = 0;
        for (int i=0;i<actual.length;i++) {
            actual[i] = min(counts[i], orbitingFleet.num(i));
            totalOrbiting += orbitingFleet.num(i);
            totalDeployed += actual[i];
        }
        if (totalDeployed == 0) {
            // err("Unable to deploy.. actual ships deployed = 0");
            log("Unable to deploy.. actual ships deployed = 0");
            return;
        }

        // if entire orbiting fleet is being deployed just use it
        if (totalOrbiting == totalDeployed) {
            orbitingFleet.destSysId(destSysId);
            orbitingFleet.makeDeployed();
            return;
        }
            
        // else we create a new deployed fleet 
        // else we create a new deployed subfleet from the source 
        StarSystem sys = galaxy().system(sysId);
        StarSystem destSys = galaxy().system(destSysId);
        
        // calculate warp speed of new fleet and travel turns to the destination
        int minSpeed = 9;
        Empire sourceEmp = orbitingFleet.empire();
        for (int i=0; i<actual.length; i++) {
            if (counts[i] > 0) 
                minSpeed = min(minSpeed, sourceEmp.shipLab().design(i).warpSpeed());
        }   
        int turns = sourceEmp.travelTurnsAdjusted(sys, destSys, minSpeed);
        // find any existing deployed fleets to that dest with the same travel turns
        ShipFleet deployedFleet = deployedFleet(empId, sysId, destSysId, turns);    
        
        if (deployedFleet == null) {
            deployedFleet = new ShipFleet(empId, sys);
            deployedFleet.destSysId(destSysId);
            deployedFleet.makeDeployed();
            allFleets.add(deployedFleet);            
            galaxy().empire(empId).addVisibleShip(deployedFleet);
        }
        
        // transfer ships from orbiting to deplooyed fleet
        for (int i=0; i<actual.length; i++) {
            int startCount = orbitingFleet.num(i);
            deployedFleet.num(i, actual[i]);
            orbitingFleet.num(i, startCount-actual[i]);
        }
        deployedFleet.setArrivalTimeAdjusted();
        // if source fleet is gone, remove it and subst session vars
        if (orbitingFleet.isEmpty()) {
            deleteFleet(orbitingFleet);
            session().replaceVarValue(orbitingFleet, deployedFleet);
        }
    } */
    public ShipFleet deployFleet(ShipFleet sourceFleet, int destSysId) {
        if (sourceFleet.inTransit()) {
            if (sourceFleet.canSend())
                return redirectFleet(sourceFleet, destSysId);
            else
                return sourceFleet;
        }

        // returns the deployed fleet
        int sysId = id(sourceFleet.system());
		if (sysId == destSysId) {
			if (sourceFleet.isDeployed())
				undeployFleet(sourceFleet);
			if (sourceFleet.isOrbiting())
				return sourceFleet;
		}

        StarSystem destSys = galaxy().system(destSysId);
        int turns = sourceFleet.travelTurnsAdjusted(destSys);

        // get deployed fleet to add ships to
        ShipFleet deployedFleet = deployedFleet(sourceFleet.empId(), sysId, destSysId, turns);   

        if (deployedFleet == sourceFleet) 
            return sourceFleet;

        // if no deployed fleet, use this one
        if (deployedFleet == null) {
            sourceFleet.destSysId(destSysId);
            sourceFleet.makeDeployed();
            sourceFleet.setArrivalTimeAdjusted();
            return sourceFleet;
        }

        // transfer ships from source to deployed fleet
        for (int i=0; i<MAX_DESIGNS; i++) {
            int a = sourceFleet.num(i);
            int b = deployedFleet.num(i);
            deployedFleet.num(i, a+b);
            sourceFleet.num(i, 0);
        }
        // recalc arrival time (added ships may change this)
        deployedFleet.setArrivalTimeAdjusted();

        // if source fleet is gone, remove it and subst session vars
        if (sourceFleet.isEmpty()) {
            deleteFleet(sourceFleet);
            session().replaceVarValue(sourceFleet, deployedFleet);
        }
        return deployedFleet;
    }
    public boolean deploySubfleet(ShipFleet sourceFleet, List<ShipDesign> designs, int destSysId) {
		if (sourceFleet.sysId() == destSysId) {
			if (sourceFleet.isOrbiting())
				return false;
			if (sourceFleet.isDeployed())
				undeployFleet(sourceFleet, designs);
		}
        int[] counts = new int[MAX_DESIGNS];
        for (ShipDesign d: designs) 
            counts[d.id()] = sourceFleet.num(d.id());

        return deploySubfleet(sourceFleet, counts, destSysId);
    }
    public boolean deploySubfleet(ShipFleet sourceFleet, int[] counts, int destSysId) {
        // returns true if a new subfleet was created
        // adjust ship counts
        int[] actual = new int[MAX_DESIGNS];
        int totalDeployed = 0;
        int totalOrbiting = 0;
        for (int i=0;i<actual.length;i++) {
            actual[i] = min(counts[i], sourceFleet.num(i));
            totalOrbiting += sourceFleet.num(i);
            totalDeployed += actual[i];
        }
        if (totalDeployed == 0) {
            // err("Unable to deploy.. actual ships  deployed = 0");
            log("Unable to deploy.. actual ships  deployed = 0");
            return false;
        }
        if (totalDeployed == sourceFleet.numShips()) {
            ShipFleet deployedFleet = deployFleet(sourceFleet, destSysId);
            return deployedFleet != sourceFleet;
        }

        // cannot redirect a partial fleet, even with HC
        if (sourceFleet.inTransit())
            return false;

        // else we create a new deployed subfleet from the source 
        StarSystem sys = sourceFleet.system();
        StarSystem destSys = galaxy().system(destSysId);

        // calculate warp speed of new fleet and travel turns to the destination
        float minSpeed = Integer.MAX_VALUE;
        Empire sourceEmp = sourceFleet.empire();
        for (int i=0; i<actual.length; i++) {
            if (counts[i] > 0)
                minSpeed = min(minSpeed, sourceEmp.shipLab().design(i).warpSpeed());
        }
        int turns = sourceEmp.travelTurnsAdjusted(sys, destSys, minSpeed);

        // find any existing deployed fleets to that dest with the same travel turns
        int empId = sourceFleet.empId();
        ShipFleet deployedFleet = deployedFleet(empId, sys.id, destSysId, turns);

        if (deployedFleet == null) {
            // if entire source fleet is being deployed just use it
            if (totalOrbiting == totalDeployed) {
                sourceFleet.destSysId(destSysId);
                sourceFleet.makeDeployed();
                sourceFleet.setArrivalTimeAdjusted();
                return false;
            }
            deployedFleet = new ShipFleet(empId, sys);
            deployedFleet.destSysId(destSysId);
            deployedFleet.makeDeployed();
            allFleets.add(deployedFleet);
            galaxy().empire(empId).addVisibleShip(deployedFleet);
        }

        // transfer ships from orbiting to deployed fleet
        for (int i=0; i<actual.length; i++) {
            int srcCount = sourceFleet.num(i);
            int destCount = deployedFleet.num(i);
            deployedFleet.num(i, destCount+actual[i]);
            sourceFleet.num(i, srcCount-actual[i]);
        }
        deployedFleet.setArrivalTimeAdjusted();

        // if source fleet is gone, remove it and subst session vars
        if (sourceFleet.isEmpty()) {
            deleteFleet(sourceFleet);
            session().replaceVarValue(sourceFleet, deployedFleet);
        }
        return true;
    }
    public ShipFleet redirectFleet(ShipFleet sourceFleet, int destSysId) {   
        // else we create a new deployed subfleet from the source 
        float currX = sourceFleet.x();
        float currY = sourceFleet.y();

        sourceFleet.destSysId(destSysId);
        sourceFleet.launch(currX, currY);

        return sourceFleet;
    }
    public ShipFleet retreatFleet(ShipFleet sourceFleet, int destSysId) {
		if (destSysId == StarSystem.NULL_ID) {
			deleteFleet(sourceFleet);
			session().removeVarValue(sourceFleet);
			return null;
		}
        ShipFleet retreatingFleet = retreatingFleet(sourceFleet.empId(), id(sourceFleet.system()), destSysId);

        if (retreatingFleet == null) {
            sourceFleet.destSysId(destSysId);
            sourceFleet.makeDeployed();
            sourceFleet.retreating(true);
            sourceFleet.setArrivalTimeAdjusted();
            return sourceFleet;  
        }

        // transfer ships from orbiting to retreating fleet
        for (int i=0; i<MAX_DESIGNS; i++) {
            int a = sourceFleet.num(i);
            int b = retreatingFleet.num(i);
            retreatingFleet.num(i, a+b);
            sourceFleet.num(i, 0);
        }
        retreatingFleet.setArrivalTimeAdjusted();
        retreatingFleet.retreating(true);
        deleteFleet(sourceFleet);
        session().replaceVarValue(sourceFleet, retreatingFleet);

        return retreatingFleet;
    }
    public ShipFleet retreatSubfleet(ShipFleet sourceFleet, int designId, int destSysId) {
        int retreatCount = sourceFleet.num(designId);
        if (retreatCount == 0) 
            return null;

		if (destSysId == StarSystem.NULL_ID) {
			sourceFleet.num(designId, 0);
			if (sourceFleet.isEmpty()) {
				deleteFleet(sourceFleet);
				session().removeVarValue(sourceFleet);
			}
			return null;
		}

        int allCount = sourceFleet.numShips();

        StarSystem sys = sourceFleet.system();
        int empId = sourceFleet.empId();
        ShipFleet retreatingFleet = retreatingFleet(sourceFleet.empId(), sys.id, destSysId);

        if (sys.id == destSysId) {
            err("Trying to retreat to same system");
            return null;
        }

        if (retreatingFleet == null) {
            // if entire source fleet is retreatuing just use it
            if (retreatCount == allCount) {
                sourceFleet.destSysId(destSysId);
                sourceFleet.makeDeployed();
                sourceFleet.retreating(true);
                sourceFleet.setArrivalTimeAdjusted();
                return sourceFleet;
            }
            retreatingFleet = new ShipFleet(empId, sys);
            retreatingFleet.destSysId(destSysId);
            retreatingFleet.makeDeployed();
            retreatingFleet.retreating(true);
            allFleets.add(retreatingFleet); 
            galaxy().empire(empId).addVisibleShip(retreatingFleet);
        }

        // transfer ships from orbiting to deployed fleet
        int a = sourceFleet.num(designId);
        int b = retreatingFleet.num(designId);
        retreatingFleet.num(designId, a+b);
        sourceFleet.num(designId, 0);
        retreatingFleet.setArrivalTimeAdjusted();

        // if source fleet is gone, remove it and subst session vars
        if (sourceFleet.isEmpty()) {
            deleteFleet(sourceFleet);
            session().replaceVarValue(sourceFleet, retreatingFleet);
        }

        return retreatingFleet;
    }
    public boolean cancelRetreatingFleets(int empId, int sysId) {
        List<ShipFleet> retreatingFleets = retreatingFleets(empId, sysId);
        ShipFleet orbitingFleet = orbitingFleet(empId, sysId);

        boolean cancelled = !retreatingFleets.isEmpty();

        for (ShipFleet fl: retreatingFleets) {
            if (orbitingFleet == null) {
                orbitingFleet = fl;
                orbitingFleet.makeOrbiting();
                orbitingFleet.retreating(false);
                orbitingFleet.rallySysId(StarSystem.NULL_ID);
                orbitingFleet.destSysId(StarSystem.NULL_ID);
            }
            else {
                for (int i=0;i<MAX_DESIGNS;i++) {
                    int a = fl.num(i);
                    int b = orbitingFleet.num(i);
                    orbitingFleet.num(i, a+b);
                    fl.num(i,0);
                }
                deleteFleet(fl);
                session().replaceVarValue(fl, orbitingFleet);
            }
        }
        return cancelled;
    }
    public boolean undeployFleet(ShipFleet sourceFleet) {
        if (!sourceFleet.isDeployed() && !sourceFleet.isRalliedThisTurn())
            return false;
        // returns true if the source fleet was scrapped
        StarSystem sys = sourceFleet.system();
        int empId = sourceFleet.empId();
        ShipFleet orbitingFleet = orbitingFleet(empId, sys.id);
        if (orbitingFleet == null) {
            sourceFleet.makeOrbiting();
            sourceFleet.rallySysId(StarSystem.NULL_ID);
            sourceFleet.destSysId(StarSystem.NULL_ID);
            return false;
        }

        for (int i=0;i<MAX_DESIGNS;i++) {
            int a = sourceFleet.num(i);
            int b = orbitingFleet.num(i);
            orbitingFleet.num(i, a+b);
            sourceFleet.num(i,0);
        }
        deleteFleet(sourceFleet);
        session().replaceVarValue(sourceFleet, orbitingFleet);

        return true;
    }
    public void undeployFleet(ShipFleet sourceFleet, List<ShipDesign> designs) {
        if (!sourceFleet.isDeployed())
            return;
        // returns true if the source fleet was scrapped
        StarSystem sys = sourceFleet.system();
        int empId = sourceFleet.empId();

        int count = 0;
        for (ShipDesign d: designs) 
            count += sourceFleet.num(d.id());

        if (count == 0) {
            err("Undeploying subfleet of zero ships");
            return;
        }    
        if (count == sourceFleet.numShips()) {
            undeployFleet(sourceFleet); // undeploy entire fleet
            return;
        }

        ShipFleet orbitingFleet = orbitingFleet(empId, sys.id);     
        // if none exists, creating orbiting fleet to hold undeploying ships
        if (orbitingFleet == null) {
            orbitingFleet = new ShipFleet(empId, sys);
            orbitingFleet.makeOrbiting();
            allFleets.add(orbitingFleet); 
            galaxy().empire(empId).addVisibleShip(orbitingFleet);
        }

        // move undeploying ships into orbiting fleet
        for (ShipDesign d: designs) {
            int i = d.id();
            int a = orbitingFleet.num(i);
            int b = sourceFleet.num(i);
            orbitingFleet.num(i, a+b);
            sourceFleet.num(i, 0);
        }
    }
    public void deleteFleet(ShipFleet fl) {
        fl.clear();
        allFleets.remove(fl);

        Galaxy g = galaxy();
        for (Empire emp: g.empires())
            emp.visibleShips().remove(fl);
    }
    void launchFleets() { // For session nextTurn
        List<ShipFleet> fleetsAll = allFleetsCopy();
	        for (ShipFleet fl: fleetsAll) {
	            if (fl != null && fl.isDeployed() && !fl.isRallied())
	                fl.launch();
	        }
    }
    public void launchFleets(int sysId) { // For combat retreat
        List<ShipFleet> fleetsAll = allFleetsCopy();

        for (ShipFleet fl: fleetsAll) {
            if (fl != null && fl.isDeployed() && (id(fl.system()) == sysId) && !fl.isRallied()) {
                fl.launch();
            }
        }
    }
    void reloadBombs() {
        List<ShipFleet> fleetsAll = allFleetsCopy();
        for (ShipFleet fl: fleetsAll)
        	if (fl != null)
        		fl.reloadBombs();
    }
    void launchRalliedFleets()	{
        List<ShipFleet> fleetsAll = allFleetsCopy();

        for (ShipFleet fl: fleetsAll) {
            if (fl != null && fl.isDeployed() && fl.isRallied()) {
                fl.destSysId(fl.rallySysId());
                fl.launch();
            }
        }
    }
    boolean arriveFleet(ShipFleet fleet) {
        StarSystem sys = galaxy().system(fleet.destSysId());

        if (fleet.retreatOnArrival()) {
            fleet.arrive(sys, false);
            StarSystem destSys = fleet.empire().retreatSystem(sys);
            retreatFleet(fleet, destSys.id);
            return false;
        }

        // only players can set up rally points
		// Only rally on player's colonies, they may have new owner
		if (fleet.isRallied() && isPlayer(sys.empire())) {
        	ShipRelocationSprite spr = sys.rallySprite();
            if (spr.isActive() && spr.forwardRallies()) {
                if (rallyTransitJoinCombat) {	// Memorize the transit
                	// System.out.println("arriveFleet addToRallyFleetCopy " + fleet.toString()); // TO DO BR: comment
                	sys.colony().shipyard().addToRallyFleetCopy(fleet);
                	// Then continue to make them orbiting
                }
                else { // Rally transit ignore combat
                	fleet.arrive(sys, true);
	            	forwardRallyFleet(fleet, fleet.empId(), sys.id, spr.rallySystem().id);
	            	return false;
                }
            }
        }
        // if an orbiting fleet already exists, merge with it
        ShipFleet orbitingFleet = orbitingFleet(fleet.empId(), sys.id);
        if (orbitingFleet == null) {
            fleet.arrive(sys, true);
            orbitingFleet = fleet;
        }
        else if (fleet != orbitingFleet) {
            for (int i=0;i<MAX_DESIGNS;i++) {
                int a = orbitingFleet.num(i);
                int b = fleet.num(i);
                orbitingFleet.num(i, a+b);
                fleet.num(i,0);
            }
            allFleets.remove(fleet);
        }

        // update ship views
        List<ShipFleet> fleets = orbitingFleets(sys.id);
        if (fleets.size() > 1) {
            for (ShipFleet fl: fleets) {
                if (fl != null && fl != fleet) {
                    fl.empire().encounterFleet(orbitingFleet);
                    fleet.empire().encounterFleet(fl);
                }
            }
        }
        if (sys.isColonized())
            sys.empire().scanFleet(orbitingFleet);
        return false;
    }
    ShipFleet anyFleetAtSystem(int empId, int sysId) {
        List<ShipFleet> fleetsAll = allFleetsCopy();
        
        for (ShipFleet fl: fleetsAll) {
            if (fl != null && (fl.empId() == empId) && (fl.sysId() == sysId) && fl.isOrbiting())
                return fl;
        }
        return null;
    }
    ShipFleet orbitingFleet(int empId, int sysId) {
        List<ShipFleet> fleetsAll = allFleetsCopy();
        
        for (ShipFleet fl: fleetsAll) {
            if (fl != null && (fl.empId() == empId) && (fl.sysId() == sysId) 
            		&& fl.isOrbiting() && !fl.isRallied())
                return fl;
        }
        return null;
    }
    private ShipFleet rallyingFleet(int empId, int sysId, int rallySysId) {
        List<ShipFleet> fleetsAll = allFleetsCopy();
        
        for (ShipFleet fl: fleetsAll) {
            if (fl != null && (fl.empId() == empId) && (fl.sysId() == sysId) 
            		&& fl.isDeployed() && (fl.rallySysId() == rallySysId))
                return fl;
        }
        return null;
    }
    ShipFleet retreatingFleet(int empId, int sysId, int destSysId) {
        List<ShipFleet> fleetsAll = allFleetsCopy();
        
        for (ShipFleet fl: fleetsAll) {
            if (fl != null && (fl.empId() == empId) && (fl.sysId() == sysId) 
            		&& fl.isDeployed() && fl.retreating() && (fl.destSysId() == destSysId))
                return fl;
        }
        return null;
    }
    public List<ShipFleet> fleeingFleets(int empId, int sysId) {
        List<ShipFleet> fleetsAll = allFleetsCopy();
        List<ShipFleet> retreating = new ArrayList<>();
        for (ShipFleet fl: fleetsAll) {
            if (fl != null && (fl.empId() == empId) && (fl.sysId() == sysId) 
            		&& fl.retreating())
                retreating.add(fl);
        }
        return retreating;
    }
    private List<ShipFleet> retreatingFleets(int empId, int sysId) {
        List<ShipFleet> fleetsAll = allFleetsCopy();
        List<ShipFleet> retreating = new ArrayList<>();
        for (ShipFleet fl: fleetsAll) {
            if (fl != null && (fl.empId() == empId) && (fl.sysId() == sysId) 
            		&& fl.isDeployed() && fl.retreating())
                retreating.add(fl);
        }
        return retreating;
    }
    private ShipFleet deployedFleet(int empId, int sysId, int destSysId, int turns) {
        List<ShipFleet> fleetsAll = allFleetsCopy();
        
        for (ShipFleet fl: fleetsAll) {
            if (fl != null && (fl.empId() == empId) && (fl.sysId() == sysId) 
            			&& fl.isDeployed() && (fl.destSysId() == destSysId)) {
                StarSystem destSys = galaxy().system(destSysId);
                if (fl.travelTurnsAdjusted(destSys) == turns)
                    return fl;
            }
        }
        return null;
    }
    List<ShipFleet> incomingFleets(int sysId) {
        List<ShipFleet> fleets = new ArrayList<>();
        List<ShipFleet> fleetsAll = allFleetsCopy();
        
        for (ShipFleet fl: fleetsAll) {
            if (fl != null) {
                if ((fl.destSysId() == sysId) && (fl.inTransit() || fl.isDeployed()))
                    fleets.add(fl);
            }
        }
        return fleets;
    }
    List<ShipFleet> orbitingFleets(int sysId) {
        List<ShipFleet> fleets = new ArrayList<>();
        List<ShipFleet> fleetsAll = allFleetsCopy();
        
        for (ShipFleet fl: fleetsAll) {
            // NPE was found on a map repaint during next turn. 
            // unsure how this is possible since allFleets var is private with no accessor
            // all allFleets.add() calls are in this class and only add new ShipFleet().
            if (fl != null) {
                if ((fl.sysId() == sysId) && fl.isOrbiting())
                    fleets.add(fl);
            }
        }
        return fleets;
    }
    List<ShipFleet> deployedFleets(int sysId) {
        List<ShipFleet> fleets = new ArrayList<>();
        List<ShipFleet> fleetsAll = allFleetsCopy();
        
        for (ShipFleet fl: fleetsAll) {
            if (fl != null) {
                if ((fl.sysId() == sysId) && fl.isDeployed())
                    fleets.add(fl);
            }
        }
        return fleets;
    }
	public List<ShipFleet> deployedFleetsTo(int empId, int sysId) {
		List<ShipFleet> fleets = new ArrayList<>();
		List<ShipFleet> fleetsAll = allFleetsCopy();
		for (ShipFleet fl: fleetsAll)
			if (fl != null
					&& fl.isDeployed()
					&& fl.empId() == empId
					&& fl.destSysId() == sysId)
				fleets.add(fl);
		return fleets;
	}
    public List<ShipFleet> inTransitFleets() {
        List<ShipFleet> fleets = new ArrayList<>();
        List<ShipFleet> fleetsAll = allFleetsCopy();
        
        for (ShipFleet fl: fleetsAll) {
            if (fl != null && fl.inTransit())
                fleets.add(fl);
        }
        return fleets;
    }
    public List<ShipFleet> inTransitNotRetreatingFleets(int empId, int designId) {
        // this specific piece of code is used to find any colony ships still 
        // en route to their dest so the AI doesn't prematurely scrap them
        List<ShipFleet> fleets = new ArrayList<>();
        List<ShipFleet> fleetsAll = allFleetsCopy();
        
        for (ShipFleet fl: fleetsAll) {
            if (fl != null && (fl.empId() == empId) && fl.inTransit() 
            		&& !fl.retreating() && (fl.num(designId) > 0))
            	fleets.add(fl);
        }
        return fleets;
    }
    public List<ShipFleet> notInTransitFleets(int empireId) {
        List<ShipFleet> fleets = new ArrayList<>();
        List<ShipFleet> fleetsAll = allFleetsCopy();

        for (ShipFleet fl: fleetsAll) {
            if (fl != null && (fl.empId() == empireId) && !fl.inTransit())
                fleets.add(fl);
        }
        return fleets;
    }
    public List<ShipFleet> allFleets() {
        return allFleetsCopy();
    }
    public List<ShipFleet> allFleets(int empireId) {
        List<ShipFleet> fleets = new ArrayList<>();
        List<ShipFleet> fleetsAll = allFleetsCopy();

        for (ShipFleet fl: fleetsAll) {
            if (fl != null && fl.empId() == empireId)
                fleets.add(fl);
        }
        return fleets;
    }
    public List<ShipFleet> allOrbitingFleets(int empireId) {
        List<ShipFleet> fleets = new ArrayList<>();
        List<ShipFleet> fleetsAll = allFleetsCopy();

        for (ShipFleet fl: fleetsAll) {
            if (fl != null && fl.empId() == empireId && fl.isOrbiting())
                fleets.add(fl);
        }
        return fleets;
    }

    public int[] scrapDesign(int empireId, int designId) {
        int scrapCount = 0;
        int empCount   = 0;
        int allyCount  = 0;
        List<ShipFleet> emptyFleets = new ArrayList<>();
        List<ShipFleet> fleetsAll = allFleetsCopy();

        for (ShipFleet fl: fleetsAll) {
            if (fl != null && fl.empId() == empireId) {
            	int count = fl.num(designId);
                if (count > 0) {
                	StarSystem sys = fl.system();
                	if (sys != null && fl.isOrbiting()) {
                		if (sys.empId() == empireId)
                			empCount += count;
                		else if (galaxy().empire(empireId).alliedWith(sys.empId()))
                			allyCount += count;
                	}
                    scrapCount += count;
                    fl.num(designId, 0);
                    if (fl.isEmpty())
                        emptyFleets.add(fl);
                }
            }
        }

        for (ShipFleet fl: emptyFleets) 
            this.deleteFleet(fl);

        return new int[] {scrapCount, empCount, allyCount};
    }
    /* public int scrapDesign(int empireId, int designId) {
        int scrapCount = 0;
        List<ShipFleet> emptyFleets = new ArrayList<>();
        List<ShipFleet> fleetsAll = allFleetsCopy();

        for (ShipFleet fl: fleetsAll) {
            if (fl.empId == empireId) {
                int count = fl.num(designId);
                if (count > 0) {
                    scrapCount += count;
                    fl.num(designId, 0);
                    if (fl.isEmpty())
                        emptyFleets.add(fl);
                }
            }
        }

        for (ShipFleet fl: emptyFleets) 
            this.deleteFleet(fl);

        return scrapCount;
    } */
    public int[] shipDesignCounts(int empireId) {
        int[] count = new int[ShipDesignLab.MAX_DESIGNS];
        List<ShipFleet> fleetsAll = allFleetsCopy();

        for (ShipFleet fl: fleetsAll) {
            if (fl != null && fl.empId() == empireId) {
                for (int i=0;i<count.length;i++)
                    count[i] += fl.num(i);
            }
        }
        return count;
    }
    public int[] shipDesignInTransitCounts(int empireId) {
        int[] count = new int[ShipDesignLab.MAX_DESIGNS];
        List<ShipFleet> fleetsAll = allFleetsCopy();
        
        for (ShipFleet fl: fleetsAll) {
            if (fl != null && (fl.empId() == empireId)
            && (fl.inTransit() || fl.isDeployed())) {
                for (int i=0;i<count.length;i++)
                    count[i] += fl.num(i);
            }
        }
        return count;
    }
    public int[] shipDesignConstructionCounts(int empireId) {
        int[] count = new int[ShipDesignLab.MAX_DESIGNS];
        
        List<StarSystem> colonies = galaxy().empire(empireId).allColonizedSystems();
        for (StarSystem s: colonies) {
            ColonyShipyard sy = s.colony().shipyard();            
            if ((sy.allocation() > 0) && sy.design().isShip()) 
                count[sy.design().id()]++;
        }
        return count;
    }
    public int shipDesignCount(int empireId, int designId) {
        int count = 0;
        List<ShipFleet> fleetsAll = allFleetsCopy();
        
        for (ShipFleet fl: fleetsAll) {
            if (fl != null && fl.empId() == empireId) 
                count += fl.num(designId);
        }
        return count;
    }
    public int hullSizeCount(int empireId, int hullSize) {
        int[] count = shipDesignCounts(empireId);

        int hullCount = 0;
        ShipDesignLab lab = galaxy().empire(empireId).shipLab();
        for (int i=0;i<count.length;i++) {
            if (lab.design(i).size() == hullSize)
                hullCount += count[i];
        }
        return hullCount;
    }
}
