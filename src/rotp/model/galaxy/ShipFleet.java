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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rotp.model.Sprite;
import rotp.model.ai.FleetOrders;
import rotp.model.ai.FleetStats;
import rotp.model.combat.CombatStackColony;
import rotp.model.combat.CombatStackShip;
import rotp.model.combat.ShipCombatManager;
import rotp.model.empires.Empire;
import rotp.model.empires.ShipView;
import rotp.model.ships.ShipDesign;
import rotp.ui.ScaledInteger;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.map.IMapHandler;
import rotp.ui.sprites.FlightPathSprite;
import rotp.util.Base;

public class ShipFleet extends FleetBase implements ScaledInteger {
	private static final long serialVersionUID = 1L;
	private enum Status { ORBITING, DEPLOYED, IN_TRANSIT, RETREAT_ON_ARRIVAL };
	private final int empId;
	private int sysId = StarSystem.NULL_ID;
	private int destSysId = StarSystem.NULL_ID;
	private int rallySysId = StarSystem.NULL_ID;
	private int[] num = new int[MAX_DESIGNS];
	private Status status = Status.ORBITING;

	private boolean retreating = false;
	private float fromX, fromY, destX, destY;
	private float launchTime = NOT_LAUNCHED;

	private transient FleetOrders orders;
	private transient FlightPathSprite pathSprite;
	private transient int[] bombardCount = new int[MAX_DESIGNS];
	private transient boolean isCopy = false;
	private transient Integer lastDestIdTest; // some options can make this test time consuming

	// =========================================================
	// Constructors
	//
	private ShipFleet(int emp, ShipFleet f) {
		empId = emp;
		sysId(f.sysId());
		fromX(f.x());
		fromY(f.y());
		destSysId(f.destSysId());
		destX(f.destX());
		destY(f.destY());
		status(f.status());
		launchTime(f.launchTime());
		isCopy = true;
		reloadBombs();
	}
	ShipFleet(int emp, int x, int y) {
		empId = emp;
		sysId(StarSystem.NULL_ID);
		fromX(x);
		fromY(y);
		reloadBombs();
	}
	public ShipFleet(int emp, StarSystem s) {
		empId = emp;
		sysId(s.id);
		fromX(s.x());
		fromY(s.y());
		reloadBombs();
	}

	// =========================================================
	// Local variables management
	//
	@Override public int empId()		{ return empId; }
	public int sysId()					{ return sysId; }
	public void sysId(int i)			{ sysId = i; }
	@Override public int destSysId()	{ return destSysId; }
	public void destSysId(int i)		{ 
		destSysId = i;
		if (hasDestination()) {
			StarSystem s = galaxy().system(destSysId());
			destX(s.x());
			destY(s.y());
			setArrivalTimeAdjusted();
		}
	}
	public int rallySysId()				{ return rallySysId; }
	public void rallySysId(int i)		{ rallySysId = i; }
	private Status status()				{ return status; }
	private void status(Status st)		{
		lastDestIdTest = null;
		status = st;
	}
	@Override public float fromX()		{ return fromX; }
	private void fromX(float f)			{ fromX = f; }
	@Override public float fromY()		{ return fromY; }
	private void fromY(float f)			{ fromY = f; }
	@Override public float destX()		{ return destX; }
	private void destX(float f)			{ destX = f; }
	@Override public float destY()		{ return destY; }
	private void destY(float f)			{ destY = f; }
	@Override public float launchTime()	{ return launchTime; }
	private void launchTime(float f)	{ launchTime = f; }

	// =========================================================
	// Other methods
	//
	@Override public int displayPriority()		{ return 8; }
	@Override public boolean hasDisplayPanel()	{ return true; }
	public void toggleRally()				{
		if (rallySysId() == StarSystem.NULL_ID)
			rallySysId(destSysId());
		else
			rallySysId(StarSystem.NULL_ID);
	}
	@Override public Empire empire()		{ return galaxy().empire(empId()); }
	@Override public boolean retreating()	{ return retreating; }
    public void retreating(boolean b)   { retreating = b; }
    public StarSystem system()          { return galaxy().system(sysId()); }
    public void system(StarSystem s)    { sysId(id(s)); }
	@Override public StarSystem destination()	{ return galaxy().system(destSysId()); }
	void makeOrbiting()					{ status(Status.ORBITING); }
	void makeDeployed()					{ status(Status.DEPLOYED); }
	private void makeInTransit()		{ status(Status.IN_TRANSIT); }
	public void makeRetreatOnArrival()	{ status(Status.RETREAT_ON_ARRIVAL); }
	public boolean isOrbiting()				{ return status() == Status.ORBITING; }
	@Override public boolean isDeployed()	{ return status() == Status.DEPLOYED; }
	@Override public boolean inTransit()	{ return status() == Status.IN_TRANSIT || retreatOnArrival(); }
	public boolean retreatOnArrival()		{ return status() == Status.RETREAT_ON_ARRIVAL; }
	public StarSystem ennemyAlienTarget()	{
		if (isOrbiting() && isArmed())
			return empire().couldTargetAlien(sysId()) ? galaxy().system(sysId()) : null;
		if (hasDestination() && !retreatOnArrival() && isArmed())
			return empire().couldTargetAlien(destSysId()) ? galaxy().system(destSysId()) : null;
		return null;
	}
	public StarSystem warEnemyTarget()		{
		if (isOrbiting() && isArmed())
			return empire().isWarEnemySystem(sysId()) ? galaxy().system(sysId()) : null;
		if (hasDestination() && !retreatOnArrival() && isArmed())
			return empire().isWarEnemySystem(destSysId()) ? galaxy().system(destSysId()) : null;
		return null;
	}
	public void toggleRetreatOnArrival()	{
		switch (status()) {
			case IN_TRANSIT:
				status(Status.RETREAT_ON_ARRIVAL);
				return;
			case RETREAT_ON_ARRIVAL:
				status(Status.IN_TRANSIT);
				return;
			default:
				return;
		}
	}
	public void setXY(float x, float y)		{ fromX(x); fromY(y); }
	public void setXY(StarSystem sys)		{ fromX(sys.x()); fromY(sys.y()); }
	@Override public boolean isRallied()	{ return rallySysId() != StarSystem.NULL_ID; }
	public boolean isRalliedThisTurn()		{ return isRallied() && (launchTime() == galaxy().currentTime()); }
	public boolean isRetreatingThisTurn()	{
		return retreating() &&
				(launchTime() == galaxy().currentTime() || launchTime() == NOT_LAUNCHED); 
	}
	public boolean hasDestination()			{ return destSysId() != StarSystem.NULL_ID; }
	public List<StarSystem> allowedRetreatSystems()	{ return empire().allowedRetreatSystems(system()); }
	@Override public boolean visibleTo(int empireId)	{
		if (empireId == empId())
            return true;

        for (int i=0;i<MAX_DESIGNS;i++) {
            ShipDesign d = design(i);
            if ((num(i) > 0) && (d != null) && !d.allowsCloaking())
                return true;
        }
        return false;
    }
	private final int[] bombardCount()	{
		if (bombardCount == null)
			bombardCount = new int[MAX_DESIGNS];
		return bombardCount;
	}
	final void reloadBombs()		{
		int length = bombardCount().length;
		for (int i=0;i<length;i++)
			bombardCount[i]=0;
	}
	private int bombardCount(int i)	{ return bombardCount()[i]; }
	public void bombarded(int i)	{ bombardCount()[i]++; }
    public void clear() {
        for (int i=0;i<MAX_DESIGNS;i++)
            num(i, 0);
    }

    @Override
    public boolean persistOnClick()             { return empire() == player(); }
    public static ShipFleet copy(ShipFleet fl) {
        // returns a new ship fleet with identical stacks & count
		ShipFleet temp = new ShipFleet(fl.empId(), fl);

        System.arraycopy(fl.num, 0, temp.num, 0, MAX_DESIGNS);
        return temp;
    }
	@Override public StarSystem destinationOrRallySystem()	{
		int destId = hasDestination() ? destSysId() : rallySysId();
		return galaxy().system(destId);
	}
    @Override public FlightPathSprite pathSprite() {
        StarSystem dest = destinationOrRallySystem();
        // If we have no destination and no rally system, we have no path and no path sprite.
        if (dest == null)
            return null;
        if (pathSprite == null)
            pathSprite = new FlightPathSprite(this, dest);
        if (pathSprite.destination() != dest)
            // Under what possible circumstances could this ever happen?
            // The FlightPathSprite constructor sets its destination to the second argument. That's all it does.
            pathSprite.destination(dest);
        return pathSprite;
    }
	public ShipDesign design(int i)		{ return empire().shipLab().design(i); }
	protected ShipDesign[] designs()	{ return empire().shipLab().designs(); }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(Integer.toHexString(hashCode()));
        sb.append('(');
        for (int i=0;i<MAX_DESIGNS;i++) {
            ShipDesign d = design(i);
            if ((num(i) > 0) && (d != null)){
                sb.append(d.name());
                sb.append('-');
                sb.append(num(i));
                sb.append('.');
            }
        }
        sb.append(')');
        return sb.toString();
    }
	@Override public float x()	{ return inTransit() ? transitX() : fromX(); }
	@Override public float y()	{ return inTransit() ? transitY() : fromY(); }
	@Override public float transitX()	{
		if (isCopy)
			return fromX();
		else
			return fromX() + travelPct()*(destX() - fromX());
	}
	@Override public float transitY()	{
		if (isCopy)
			return fromY();
		else
			return fromY() + travelPct()*(destY() - fromY());
	}

    public void launch() {
        StarSystem sys = system();
        launch(sys.x(), sys.y());
    }
    public void launch(float x, float y)  {
		fromX(x);
		fromY(y);
	    launchTime(galaxy().currentTime());
        setArrivalTimeAdjusted();
        makeInTransit();
    }
	void arrive(StarSystem sys, boolean scan)	{
		sysId(sys.id);
		fromX(sys.x());
		fromY(sys.y());
		retreating(false);
		
		destSysId(StarSystem.NULL_ID);
		rallySysId(StarSystem.NULL_ID);
		launchTime(NOT_LAUNCHED);
		makeOrbiting();
		if (scan)
			empire().sv.view(sys.id).refreshSystemEntryScan();
	}
	public boolean launched()			{ return launchTime() > NOT_LAUNCHED; }
    public boolean canUndeploy()    { 
        if (isRalliedThisTurn())
            return true;
        return isDeployed() && !retreating(); 
    }
    //public boolean isActive()       { return hasShips(); }

    public int[] numCopy()          { return num.clone(); }
    public int num(int i)           { return max(0, num[i]); } // BR for backward fix
    void validate()                 { // BR: For backward compatibility with monsters
    	if (num == null) {
    		num = new int[MAX_DESIGNS];
    		if (this instanceof OrionGuardianShip)
    			system(galaxy().orionSystem());
    	}
    }
    void num(int i, int count)      { num[i] = max(0, count); }
    public boolean visibleTo(Empire emp) {
        if (emp.canSeeShips(empId()))
            return true;

        for (int i=0;i<MAX_DESIGNS;i++) {
            ShipDesign d = design(i);
            if ((num(i) > 0) && (d != null) && !d.allowsCloaking())
                return true;
        }
        return false;
    }
    public boolean allowsScanning() {
        for (int i=0;i<MAX_DESIGNS;i++) {
            ShipDesign d = design(i);
            if ((num(i) > 0) && (d != null) && d.allowsScanning())
                return true;
        }
        return false;
    }
    public ShipDesign visibleDesign(int emp, int num) {
        int[] visible = visibleShips(emp);
        int cnt = num;
        for (int i=0;i<visible.length;i++) {
            if (visible[i] > 0) {
                if (cnt == 0)
                    return design(i);
                else
                    cnt--;
            }
        }
        return null;
    }
    public int[] visibleShips(int emp) {
        if (empId() == emp)
            return numCopy();

        int[] visible = new int[MAX_DESIGNS];
        for (int i=0;i<MAX_DESIGNS;i++) {
            ShipDesign d = design(i);
            if ((num(i) > 0) && (d != null) && !d.allowsCloaking())
                visible[i] = num(i);
            else
                visible[i] = 0;
        }
        return visible;
    }
    public Map<ShipDesign, Integer> visibleShipDesigns(int emp) {
        Map<ShipDesign, Integer> ret = new HashMap<ShipDesign, Integer>();
        int[] visible = visibleShips(emp);
        for (int i=0; i<visible.length; i++) {
            int cnt = visible[i];
            if (cnt > 0) {
                ShipDesign design = design(i);
                if (design == null)
                    ret.put(null, ret.getOrDefault(null, 0) + cnt);
                else
                    ret.put(design, cnt);
            }
        }
        return ret;
    }
    public float visibleFirepower(int emp, int shieldLevel) {
        // this calculates the visible firepower threat against SystemView sv
        // it only checks visible stacks for sv's owner
        // for unscanned ShipDesigns it asks the sv's owner to estimate threat based on size
        //  (this allows the owner to check any spied tech trees for worst possible weapons)
        final Empire viewingEmpire = galaxy().empire(emp);
        // cannot use mapToDouble().sum() nor Collectors.summingDouble https://rmannibucau.metawerx.net/java-stream-float-widening.html
        return visibleShipDesigns(emp).entrySet().stream().map(entry -> {
            ShipDesign design = entry.getKey();
            int cnt = entry.getValue();
            return cnt * viewingEmpire.estimatedShipFirepower(design, shieldLevel);
        }).reduce(0f, (Float a, Float b) -> a + b); // this .reduce() is literally just .sum() for floats
    }
    public float visibleFirepowerAntiShip(int viewingEmpireId, int shieldLevel) {
        // visibleFirepowerAntiShip() is almost identical with visibleFirepower().
        // It would be great if we could cut down the boilerplate even more, but some seems necessary for readability,
        // like ShipDesign design = entry.getKey().
        final Empire viewingEmpire = galaxy().empire(viewingEmpireId);
        // cannot use mapToDouble().sum() nor Collectors.summingDouble https://rmannibucau.metawerx.net/java-stream-float-widening.html
        return visibleShipDesigns(viewingEmpireId).entrySet().stream().map(entry -> {
            ShipDesign design = entry.getKey();
            int cnt = entry.getValue();
            return cnt * viewingEmpire.estimatedShipFirepowerAntiShip(design, shieldLevel);
        }).reduce(0f, (Float a, Float b) -> a + b); // this .reduce() is literally just .sum() for floats
    }
    boolean aggressiveWith(ShipFleet fl, StarSystem sys) {
        // only possibly aggressive if one of the fleets is armed
        if (isArmed() || fl.isArmed())
            return empire().aggressiveWith(fl.empire(), sys);
        else
            return false;
    }
    void checkColonize() {
        if ((system() != null) && isOrbiting())
        	if (options().isColdWarMode()) {
        		boolean dangerFleets = system().nonAlliedAlienShipsInOrbit(empire());
        		if (!dangerFleets)
        			empire().ai().checkColonize(system(), this);
        	}
        	else
        		empire().ai().checkColonize(system(), this);
    }
    public boolean canColonizeSystem(StarSystem sys) {
        if ((sys == null) || sys.planet().type().isAsteroids())
            return false;
        ShipDesign d = empire().shipDesignerAI().bestDesignToColonize(this, sys);
        return d != null;
    }
    public void colonizeSystem(StarSystem sys) {
        ShipDesign d = empire().shipDesignerAI().bestDesignToColonize(this, sys);
        if (d != null)
            colonizeSystem(sys, d);
    }
    public void colonizeSystem(StarSystem sys, ShipDesign d) {
        String sName = empire().sv.name(sys.id);
        log("Fleet: ", toString(), " colonizing system: ", sName, " with design:", d.name());
        empire().colonize(sName, sys);
        empire().sv.refreshFullScan(sys.id);

        // if fleet will be disbanded, ensure UIs are no longer selecting the fleet
        if (numShips() == 1)
            session().replaceVarValue(this, sys);

        removeShips(d.id(), 1, true);
        empire().shipLab().recordUse(d, 1);
    }
    public boolean canAttackPlanets() {
        for (int i=0;i<MAX_DESIGNS;i++) {
            ShipDesign d = design(i);
            if (num(i)>0 && d != null && d.canAttackPlanets())
                return true;
        }
        return false;
    }
    public boolean isArmed() {
        for (int i=0;i<MAX_DESIGNS;i++) {
            if (num(i) > 0) {
                ShipDesign d = design(i);
                if ((d != null) && d.isArmed())
                    return true;
            }
        }
        return false;
    }
    public boolean isArmedForShipCombat() {
        for (int i=0;i<MAX_DESIGNS;i++) {
            if (num(i) > 0) {
                ShipDesign d = design(i);
                if ((d != null) && d.isArmedForShipCombat())
                    return true;
            }
        }
        return false;
    }
    public boolean isArmed(StarSystem sys) {
        for (int i=0;i<MAX_DESIGNS;i++) {
            ShipDesign d = design(i);
            if ((num(i)>0) && (d != null)) {
                for (int j=0;j<ShipDesign.maxWeapons();j++) {
                    if (!d.weapon(j).isNone()) {
                        if (d.weapon(j).canAttackShips())
                            return true;
                        if (sys.isColonized() && !empire().alliedWith(sys.empire().id) & d.weapon(j).canAttackPlanets())
                            return true;
                    }
                }
                for (int j=0;j<d.maxSpecials();j++) {
                    if (d.special(j).canAttackShips()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    @Override
    public boolean isPotentiallyArmed(Empire e) {
        if (empire() == e)
            return isArmed();
        // only return as definitely unarmed if every
        // ship design in this fleet has been scanned
        // by Empire e and is known to be unarmed
        for (int i=0;i<MAX_DESIGNS;i++) {
            if (num(i) > 0) {
                ShipDesign des = design(i);
                ShipView sv = e.shipViewFor(des);
                if ((sv == null) || sv.isPotentiallyArmed())
                    return true;
            }
        }
        return false;
    }
    public boolean isActive()  { // = has ships
        for (int i=0;i<MAX_DESIGNS;i++) {
            if (num(i)>0)
                return true;
        }
        return false;
    }
    public boolean hasShip(ShipDesign d)  {
        return (d == null) || !d.active() ? false : num(d.id()) > 0;
    }
    @Override
    public float calculateAdjustedArrivalTime() {
        float arrivalTime = galaxy().currentTime();
        if (hasDestination())
            arrivalTime += travelTimeAdjusted(destination());
        if (arrivalTime <= galaxy().currentTime()) 
            log("Error: ship arrivalTime <= currentTime");
        return arrivalTime;
    }
    public FleetOrders newOrders() {
        if (orders == null)
            orders = new FleetOrders(this);
        orders.reset();
        return orders;
    }
    public FleetOrders orders() {
        if (orders == null)
            orders = newOrders();
        return orders;
    }
    public boolean canBeAdjustedBy(Empire c) {
        return (c == empire()) && canAdjust();
    }
    public boolean canBeSentBy(Empire c) {
        return (c == empire()) && canSend();
    }
    private boolean canAdjust() {
        return isActive() && !inTransit();
    }
    public boolean canSend() {
        return isActive() && (!inTransit() || isRetreatingThisTurn() || isRalliedThisTurn() || isOrbiting() || empire().tech().hyperspaceCommunications());
    }
	private boolean allowedToRetreatTo(int id)	{
		if (id == sysId())
			return false;
		if (lastDestIdTest != null) {
			if (id == lastDestIdTest)
				return true;
			if (-id == lastDestIdTest)
				return false;
		}
		if (empire().allowedToRetreatTo(system(), galaxy().system(id))) {
			lastDestIdTest = id;
			return true;
		}
		else {
			lastDestIdTest = -id;
			return false;
		}
	}
	@Override public boolean canSendTo(int id)	{
		if (id == StarSystem.NULL_ID)
			return false;

		if (retreating())
				return isRetreatingThisTurn() && allowedToRetreatTo(id);
		if (!canSend())
            return false;

        //cannot send if already orbiting the sv.system
        if (!inTransit() && (sysId() == id))
            return false;
        if (!empire().sv.withinRange(id, range()))
            return false;

        return true;
    }
    public boolean canSendDesignTo(ShipDesign d, int id) {
        if (id == StarSystem.NULL_ID)
            return false;

		if (retreating())
			return isRetreatingThisTurn() && allowedToRetreatTo(id);
        if (!canSend())
            return false;

        //cannot send if already orbiting the sv.system
        if (!inTransit() && (sysId() == id))
            return false;
        if (!empire().sv.withinRange(id, d.range()))
            return false;

        return true;
    }
    public boolean canMassDeployTo(StarSystem sys) {
        if (sys == null)
            return false;
        //allow if already orbiting the sv.system
        if (isOrbiting() && !inTransit() && (system() == sys))
            return true;
		if (retreating())
			return allowedToRetreatTo(sys.id);
        return true;
    }
    @Override
    public float hullPoints() {
        float pts = 0;
        for (int i=0;i<MAX_DESIGNS;i++) {
            if (num(i) > 0) {
                ShipDesign d = design(i);
                if (d != null)
                    pts += (num(i) * d.hullPoints());
            }
        }
        return pts;
    }
    public float range() {
        if (isEmpty())
            return 0;

        for (int i=0;i<MAX_DESIGNS;i++) {
            if (num(i)>0) {
                ShipDesign des = design(i);
                if (!des.isExtendedRange())
                    return empire().tech().shipRange();
            }
        }
        return empire().tech().scoutRange();
    }
    @Override public float travelSpeed() { return slowestStackSpeed(); }
	@Override public float speedInNebulae()	{ return empire().speedInNebulae(); }
	public String speedInNebulaeStr()	{ return empire().speedInNebulaeStr(); }
	public int slowestStackBaseSpeed()	{
		int maxBaseSpeed = Integer.MAX_VALUE;
		for (int i=0; i<MAX_DESIGNS; i++) {
			if (num(i)>0) {
				ShipDesign des = design(i);
				if (des != null)
					maxBaseSpeed = min(maxBaseSpeed, des.engine().baseWarp());
			}
		}
		return maxBaseSpeed;
	}
    public float slowestStackSpeed() {
        float maxSpeed = Integer.MAX_VALUE;
        for (int i=0;i<MAX_DESIGNS;i++) {
            if (num(i)>0) {
                ShipDesign des = design(i);
                if (des != null)
                    maxSpeed = min(maxSpeed, des.engine().warp());
            }
        }
        return maxSpeed;
    }
    // modnar: add firepowerAntiShip to only count weapons that can attack ships
    public float firepowerAntiShip(float shield) {
        float dmg = 0;
        for (int i=0;i<MAX_DESIGNS;i++) {
            if (num(i)>0) {
                ShipDesign des = design(i);
                if (des != null)
                    dmg += (num(i) * des.firepowerAntiShip(shield));
            }
        }
        return dmg;
    }
    // modnar: add firepowerAntiShip to only count weapons that can attack ships
    public float firepowerAntiShip(float shield, float defense, float missileDefense) {
        float dmg = 0;
        for (int i=0;i<MAX_DESIGNS;i++) {
            if (num(i)>0) {
                ShipDesign des = design(i);
                if (des != null)
                    dmg += (num(i) * des.firepowerAntiShip(shield, defense, missileDefense));
            }
        }
        return dmg;
    }
    public float firepower(float shield) {
        float dmg = 0;
        for (int i=0;i<MAX_DESIGNS;i++) {
            if (num(i)>0) {
                ShipDesign des = design(i);
                if (des != null)
                    dmg += (num(i) * des.firepower(shield));
            }
        }
        return dmg;
    }
	protected int otherSpecialCount()	{ return 0; } // For monster out of design specials
    private FleetStats getFleetStats(ShipFleet fl) {
        FleetStats stats	= new FleetStats();
        float totalShield	= 0;
        float totalDefense	= 0;
        float totalMissileDefense = 0;
        float totalSpecials	= 0;
        float totalHP		= 0;
        int shipDefenseBonus  = fl.empire().shipDefenseBonus();
        int otherSpecialCount = fl.otherSpecialCount();
        for (int i=0;i<MAX_DESIGNS;i++) {
            int num = fl.num(i);
            if (num > 0) {
                ShipDesign des = fl.design(i);
                if(des == null)
                    continue;
                float hits	  = num * des.hits();
                totalHP		 += hits;
                totalShield	 += hits * des.shieldLevel();
                totalDefense += hits * (des.beamDefense() + shipDefenseBonus);
                totalMissileDefense += hits * (des.missileDefense() + otherSpecialCount);
            }
        }
        if(totalHP > 0)
        {
            stats.avgShield	 = totalShield / totalHP;
            stats.avgDefense = totalDefense / totalHP;
            stats.avgMissileDefense = totalMissileDefense / totalHP;
            stats.avgSpecials = totalSpecials / totalHP;
            stats.totalHP	  = totalHP;
        }
        return stats;
    }
    private float combatPower(ShipFleet attacker, FleetStats defender) { //BR: From xilmi.AIFleetCommander
        FleetStats defenderStats = defender;
        FleetStats attackerStats = getFleetStats(attacker);
        float power = attacker.firepowerAntiShip(defenderStats.avgShield, defenderStats.avgDefense, defenderStats.avgMissileDefense);
        power *= Math.pow(1.26, attackerStats.avgSpecials);
        float shipsOfAvgPower = attackerStats.totalHP / attackerStats.avgHP;
        power *= (shipsOfAvgPower + 1) / (2 * shipsOfAvgPower);
        power *= attackerStats.avgArmor;
        return power;
    }
    private float combatPower(ShipFleet attacker, ShipFleet defender) { //BR: From xilmi.AIFleetCommander
        return combatPower(attacker, getFleetStats(defender));
    }
    public boolean monsterStrongerThan(ShipFleet attacker, float securityFactor) {
    	float attackerPower = combatPower(attacker, this);
    	float defenderPower = combatPower(this, attacker);
    	float attackerDefenderRatio = attackerPower/defenderPower;
        return attackerDefenderRatio < securityFactor;
    }
	public boolean canReach(StarSystem dest)	{
		boolean inRange = empire().sv.withinRange(dest.id, range());
		if (inRange && retreating()) {
			return canSendTo(dest.id);
		}
		return inRange;
	}
    public float travelTimeAdjusted(StarSystem to)	{ return travelTimeAdjusted(to, travelSpeed()); }
    public float travelTimeAdjusted(StarSystem dest, float speed) {
        if (isOrbiting() || isDeployed()
        || (inTransit() && (travelPct() == 0 && system() != null))) {
        	StarSystem sys = system();
            if (sys != null && sys.hasStargate(empire()) && dest.hasStargate(empire()))
                return 1;
        }
        return travelTimeAdjusted(this,dest,speed);
    }
    public int travelTurnsAdjusted(StarSystem dest)	{ return ceil(travelTimeAdjusted(dest)); }
    public boolean isEmpty() { return numShips() == 0; }
    public int numShips ()   {
        int count = 0;
        for (int i=0;i<MAX_DESIGNS;i++)
            count += num(i);
        return count;
    }
    public float bcValue() {
        float bc = 0;
        for (int i=0;i<MAX_DESIGNS;i++) {
            if (num(i) > 0) {
                ShipDesign des = design(i);
                if (des != null)
                    bc += (num(i) * des.cost());
            }
        }
        return bc;
    }
    public ShipDesign newestOfType (int missionType) {
        ShipDesign newestDesign = null;
        for (int i=0;i<MAX_DESIGNS;i++) {
            if (num(i) > 0) {
                ShipDesign desn = design(i);
                if ((desn != null) && (desn.mission() == missionType)) {
                    if ((newestDesign == null) || (newestDesign.seq() < desn.seq()))
                        newestDesign = desn;
                }
            }
        }
        return newestDesign;
    }
    public boolean hasColonyShip() {
        for (int i=0;i<MAX_DESIGNS;i++) {
            if (num(i) > 0) {
                ShipDesign des = design(i);
                if ((des != null) && des.hasColonySpecial())
                    return true;
            }
        }
        return false;
    }
	public boolean isAutoScoutOnly()	{
		boolean hasShip = false;
		for (int i=0;i<MAX_DESIGNS;i++) {
			if (num(i) > 0) {
				hasShip = true;
				ShipDesign des = design(i);
				if (des != null && !des.isAutoScout())
					return false;
			}
		}
		return hasShip;
	}
    public void disband()	{ galaxy().ships.deleteFleet(this); }
    public boolean addFleet(ShipFleet fl)	{
    	if (this == fl)
    		return false;
        for (int id=0; id<MAX_DESIGNS; id++)
        	addShips(id, fl.num(id));
        return true;
    }
    public void addShips(int id, int n)		{ num(id, max(0, num(id) + n)); }
    void addFleetAndDisband(ShipFleet fl)	{
    	if (addFleet(fl)) {
    		session().replaceVarValue(fl, this);
            log("disband#3 fleet: ", fl.toString());
            fl.disband();
    	}
    }
    public void removeShips(int designId, int n, boolean disbandIfEmpty)	{
    	addShips(designId, -n);
        if (disbandIfEmpty && !isActive()) {
            log("disband#4 fleet: ", toString());
            disband();
        }
    }
    public int removeShips(int i, int count)  { // For unregistered fleet only
    	count = max(0, count);
    	count = min(count, num(i));
    	addShips(i, -count);
    	return count;
    }
    public void targetBombard(float popLim) { // BR:
        StarSystem sys = system();
        if (!sys.isColonized())
            return;

        Empire victim = sys.empire();
        victim.lastAttacker(empire());
        log(empire().name(), " fleet bombarding ", sys.name());
        ShipCombatManager mgr = galaxy().shipCombat();
        mgr.setupBombardment(system(), this);

        CombatStackColony colonyStack = mgr.results().colonyStack;
        for (int i=0;i<MAX_DESIGNS;i++) {
            if (num(i) > 0) {
                ShipDesign d = design(i);
                if (d != null) {
                    CombatStackShip shipStack = new CombatStackShip(this, i, mgr);
                    for (int j=0;j<ShipDesign.maxWeapons();j++) {
                        int wpnCount = d.wpnCount(j);
                        int attackCount = d.weapon(j).attacksPerRound() * d.weapon(j).scatterAttacks();
                        int bombAtt = d.weapon(j).bombardAttacks() - bombardCount(i);
                        int numAttacks = num(i) * attackCount * wpnCount * bombAtt;
                        for (int k=0; k<numAttacks && system().population()>popLim; k++)
                            d.weapon(j).fireUpon(shipStack, colonyStack, 1, mgr);
                    }
                    for (int j=0;j<d.maxSpecials();j++) {
                        int numAttacks = d.special(j).bombardAttacks() - bombardCount(i);
                        for (int k=0; k<numAttacks && system().population()>popLim; k++)
                            d.special(j).fireUpon(shipStack, colonyStack, 1, mgr);
                    }
                }
            }
        }
        mgr.endOfCombat(true);
        empire().sv.refreshFullScan(sysId());
        victim.sv.refreshFullScan(sysId());
    }
    public void bombard() {
        StarSystem sys = system();
        if (!sys.isColonized())
            return;

        Empire victim = sys.empire();
        victim.lastAttacker(empire());
        log(empire().name(), " fleet bombarding ", sys.name());
        ShipCombatManager mgr = galaxy().shipCombat();
        mgr.setupBombardment(system(), this);

        CombatStackColony colonyStack = mgr.results().colonyStack;
        for (int i=0;i<MAX_DESIGNS;i++) {
            if (num(i) > 0) {
                ShipDesign d = design(i);
                if (d != null) {
                    CombatStackShip shipStack = new CombatStackShip(this, i, mgr);
                    for (int j=0;j<ShipDesign.maxWeapons();j++) {
                        int wpnCount = d.wpnCount(j);
                        int attackCount = d.weapon(j).attacksPerRound() * d.weapon(j).scatterAttacks();
                        int bombAtt = d.weapon(j).bombardAttacks() - bombardCount(i);
                        int numAttacks = num(i) * attackCount * wpnCount * bombAtt;
                        for (int k=0;k<numAttacks && system().isColonized();k++)
                            d.weapon(j).fireUpon(shipStack, colonyStack, 1, mgr);
                    }
                    for (int j=0;j<d.maxSpecials();j++) {
                        int numAttacks = d.special(j).bombardAttacks() - bombardCount(i);
                        for (int k=0;k<numAttacks && system().isColonized();k++)
                            d.special(j).fireUpon(shipStack, colonyStack, 1, mgr);
                    }
                }
            }
        }
        mgr.endOfCombat(true);
        empire().sv.refreshFullScan(sysId());
        victim.sv.refreshFullScan(sysId());
    }
    public float expectedBombardDamage(boolean ignoreBio) {  return expectedBombardDamage(system(), ignoreBio);  }
    public float expectedBombardDamage(StarSystem sys, boolean ignoreBio) {
        if (!sys.isColonized())
            return 0;

        float damage = 0;
        ShipCombatManager mgr = galaxy().shipCombat();
        CombatStackColony planetStack = new CombatStackColony(sys.colony(), mgr);

        for (int i=0;i<MAX_DESIGNS;i++) {
            if (num(i) > 0) {
                ShipDesign d = design(i);
                for (int j=0;j<ShipDesign.maxWeapons();j++) {
                    if(d.weapon(j).isBioWeapon() && ignoreBio || d.weapon(j).bombardAttacks() == 0)
                        continue;
                    damage += (num(i) * d.wpnCount(j) * d.weapon(j).estimatedBombardDamage(d, planetStack) * ((float)(d.weapon(j).bombardAttacks() - bombardCount(i)) / d.weapon(j).bombardAttacks()));
                }
                for (int j=0;j<d.maxSpecials();j++)
                {
                    if(d.special(j).bombardAttacks() == 0)
                        continue;
                    damage += d.special(j).estimatedBombardDamage(d, planetStack) * ((float)(d.special(j).bombardAttacks() - bombardCount(i)) / d.special(j).bombardAttacks());
                }
            }
        }
        return damage;
    }
    //
    // Fleet Sprite behavior is here now
    private final static Comparator<ShipFleet> DEST_Y = (ShipFleet o1, ShipFleet o2) -> Base.compare(o1.destY(), o2.destY());
    public final static Comparator<ShipFleet> COST = (ShipFleet o1, ShipFleet o2) -> Base.compare(o2.bcValue(), o1.bcValue());
    // BR: To fix dHannash resulting pathSprite bug
    public FlightPathSprite pathSpriteTo(StarSystem sys) {
        return new FlightPathSprite(this, sys);
    }
    public void use(Sprite o, IMapHandler ui) {
        if (o instanceof StarSystem) {
            StarSystem sys = (StarSystem) o;
            FlightPathSprite.workingPath(pathSpriteTo(sys));
            ui.repaint();
        }
    }
	public boolean isCloseTo(ShipFleet fl)	{
		Rectangle selectBox = selectBox();
		float centerX = selectBox.x + (selectBox.width/2);
		float centerY = selectBox.y + (selectBox.height/2);
		selectBox = fl.selectBox();
		float fleetX = selectBox.x + (selectBox.width/2);
		float fleetY = selectBox.y + (selectBox.height/2);
		return distance(fleetX-centerX, fleetY-centerY) <= s8;
	}
    @Override
    public boolean isSelectableAt(GalaxyMapPanel map, int mapX, int mapY) {
        return displayed() && selectBox().contains(mapX, mapY);
    }
    @Override
    public float selectDistance(GalaxyMapPanel map, int mapX, int mapY)  {
        float centerX = selectBox().x+(selectBox().width/2);
        float centerY = selectBox().y+(selectBox().height/2);
        return distance(mapX, mapY, centerX, centerY);
    }
    @Override
    public int mapX(GalaxyMapPanel map) {
        int x = map.mapX(x());
        
        if (inTransit())
            return x;
        // if in orbit, offset map position above and to right of the star
        float mult = max(4, min(100,map.scaleX()));
        if (isDeployed() || isRallied())
            x -= scaled((int)(600/mult)); // 20 px to left if leaving system
        else if (isOrbiting())
            x += scaled((int)(400/mult)); // 20 px to right of the star
        return x;
    }
    @Override
    public int mapY(GalaxyMapPanel map) {
        int vSpacing = s14;
        int y = map.mapY(y());
        if (inTransit())
            return y;
        float mult = max(4, min(60,map.scaleX()));
        int shipH = (int) (500/mult);
        // check orbiting fleets
        if (!inTransit()) {
            // 20 px above star, then +10 down for its position in fleet list
            y -= shipH;
            StarSystem sys = system();
            List<ShipFleet> fleets = sys.exitingFleets();
            // check if in exiting fleets first...so that list so that
            // fleets moving upward in galaxy are at the top of the list
            if (fleets.contains(this)) {
                Collections.sort(fleets, DEST_Y);
                int index = fleets.indexOf(this);
                y += (vSpacing*index);
                return y;
            }

            // search for a fleet matching this fleet's civ, not this fleet itself
            // this allows "working" fleets in the process of deployment to be found
            // at the correct index and the flight path line drawn properly
            List<ShipFleet> orbitingFleets = sys.orbitingFleets();
            int index  = 0;
            for (int i=0;i<orbitingFleets.size();i++) {
                ShipFleet fl = orbitingFleets.get(i);
                if (fl.empire() == empire())
                    index = i;
            }
            y += (vSpacing*index);
        }
        return y;
    }
    @Override
    public int centerMapX(GalaxyMapPanel map) { // BR: Only used by drawShipPath
        return mapX(map) + s7;
    }
    @Override
    public int centerMapY(GalaxyMapPanel map) { // BR: Only used by drawShipPath
        return mapY(map) + s5;
    }
    @Override
    public int maxMapScale() {
        float size = hullPoints();
        if (size >= 10000)
            return GalaxyMapPanel.maxFleetHugeScale;
        else if (size >= 100)
            return GalaxyMapPanel.maxFleetLargeScale;
        else
            return GalaxyMapPanel.maxFleetSmallScale;
    }
    @Override
    public boolean decideWhetherDisplayed(GalaxyMapPanel map) {
        if (!map.displays(this))
            return false;
        if (map.scaleX() > maxMapScale())
            return false;
        Sprite clickedSprite = map.parent().clickedSprite();
        boolean clickingOnThisFleet = false;
        if ((clickedSprite == this)
        || ((destination() != null) && (destination() == clickedSprite)))
            clickingOnThisFleet = true;

        // if fleet is unarmed and map is not showing unarmed, then
        // don't draw unless we are clicking on this fleet
        boolean armed = isPotentiallyArmed(player());    
        if (!armed && !map.showUnarmedShips() && !clickingOnThisFleet)
            return false;

        // stop drawing unarmed AI fleets at a certain zoom level
        if (!armed && !empire().isPlayerControlled() && (map.scaleX() > GalaxyMapPanel.maxFleetUnarmedScale))
            return false;

        // because fleets can be disbanded asynchronously to the ui thread,
        // make sure this fleet is still active before drawing
        if (!isActive())
            return false;

        if (!map.parent().shouldDrawSprite(this))
            return false;

        return true;
    }
    @Override
    public void draw(GalaxyMapPanel map, Graphics2D g2) {
        if (!displayed())
            return;
        float size = hullPoints();
        int imgSize = 1;
        if (size >= 100)
            imgSize = 2;
        else if (size >= 10000)
            imgSize = 3;

        if (map.scaleX() > GalaxyMapPanel.maxFleetLargeScale) 
            imgSize--;
        if (map.scaleX() > GalaxyMapPanel.maxFleetSmallScale)
            imgSize--;

        // are we zoomed out too far to show a fleet of this size?
        if (imgSize < 1)
            return;

//        int x1 = centerMapX(map);
//        int y1 = centerMapY(map);
//        System.out.println("drawShipPath x1 / y1 : " + x1 + " / " + y1);
        int x = mapX(map);
        int y = mapY(map);
        BufferedImage img;

        boolean armed = isPotentiallyArmed(player());    
        if (armed) {
            if (imgSize == 1)
                img = empire().shipImage();
            else if (imgSize == 2)
                img = empire().shipImageLarge();
            else
                img = empire().shipImageHuge();
        }
        else
            img = empire().scoutImage();

        int w = img.getWidth();
        int h = img.getHeight();

        if (!hasDestination() || (destX() > x()))
            g2.drawImage(img, x, y, w, h, map);
        else
            g2.drawImage(img, x+w, y, -w, h, map);

        int pad = s8;
        selectBox().setBounds(x-pad,y-pad,w+pad+pad,h+pad+pad);

        int cnr = s10;
        if (map.parent().isClicked(this))
            drawSelection(g2, map, x-s5, y-s5, w+s10, h+s10, cnr);
        else if (map.parent().isHovering(this))
            drawHovering(g2, map, x-s5, y-s5, w+s10, h+s10, cnr);
    }
    protected void drawSelection(Graphics2D g, GalaxyMapPanel map, int x, int y, int w, int h, int cnr) {
        if (empire() == null)
            return;

        g.setColor(empire().selectionColor());
        g.fillRoundRect(x, y, w, h, cnr, cnr);

        Stroke prev = g.getStroke();
        g.setStroke(stroke2);
        g.setColor(empire().color());
        g.drawRoundRect(x,y,w,h, cnr, cnr);
        g.setStroke(prev);
    }
    protected void drawHovering(Graphics2D g, GalaxyMapPanel map, int x, int y, int w, int h, int cnr) {
        if (empire() == null)
            return;

        Stroke prev = g.getStroke();
        g.setStroke(stroke2);
        g.setColor(empire().color());
        g.drawRoundRect(x,y,w,h, cnr, cnr);
        g.setStroke(prev);
    }
    @Override
    public IMappedObject source() { return this; }

    public boolean isOneShip()	{ return numShips() == 1; }
    public void degradePlanet(StarSystem sys)	{  } // BR:
}
