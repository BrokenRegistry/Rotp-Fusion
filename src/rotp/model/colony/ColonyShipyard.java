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
package rotp.model.colony;

import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import rotp.model.empires.Empire;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.IGameOptions;
import rotp.model.ships.Design;
import rotp.model.ships.DesignStargate;
import rotp.model.ships.ShipDesign;
import rotp.model.tech.TechStargate;

public class ColonyShipyard extends ColonySpendingCategory {
    private static final long serialVersionUID = 1L;
    private boolean hasStargate = false;
    private boolean buildingStargate = false;
    private Design design;
    private Design prevDesign;
    private float stargateBC = 0;
    private float shipBC = 0;
    private float shipReserveBC = 0;
    private boolean stargateCompleted = false;
    private int buildLimit = 0;
    private boolean shipLimitReached = false;
	private Integer defaultDesignId  = null; // If null Get the one from the Empire
	private transient ShipFleet orbitFleetCopy;
	private transient ShipFleet rallyFleetCopy; // Transit that join combat
    private transient float maxAllowedShipBCProd;
    private transient int newShips = 0;
    // private transient int rallyDestSysId = StarSystem.NULL_ID; // BR: useless and misleading

    // used by the AI when determining what to build
    private float queuedBC = 0;
    private int desiredShips = 0;

	private ShipFleet rallyFleetCopy()	{
		if (rallyFleetCopy == null)
			rallyFleetCopy = new ShipFleet(empire().id, colony().starSystem());
		return rallyFleetCopy;
	}
	// BR: Public methods related to rally joining combat, ordered by call sequence.
	// 1. preNextTurn (Better safe than sorry) Then 5.b assessTurn
	public void clearFleetsCopies()		{ 
		rallyFleetCopy = null;
		orbitFleetCopy = null;
	}
	// 2. moveShipsInTransit -> arriveFleet: Transit that may join a combat
	public void addToRallyFleetCopy(ShipFleet fl)	{ rallyFleetCopy().addFleet(fl); }
	// 3. nextEmpireTurns 
	@Override public void nextTurn(float totalProd, float totalReserve)	{
		// Build the ships if any and place them in orbit or in rally
		nextTurnPart1(totalProd, totalReserve);

		if (empire().isPlayerControlled()) {
			// Orbiting and transit fleets are now set; save a copy in case of combat
			ShipFleet orbitingFleet = colony().starSystem().orbitingFleetForEmpire(empire());
			if (orbitingFleet != null) {
				orbitFleetCopy = ShipFleet.copy(orbitingFleet);
				// System.out.println("nexTurn orbitFleetCopy = " + orbitFleetCopy.toString()); // TO DO BR: Comment
				IGameOptions opts = options();
				if (opts.rallyTransitJoinCombat() || !opts.rallyLossDefense()) {
					rallyFleetCopy(); // to init if empty
					// System.out.println("nexTurn rallyFleetCopy = " + rallyFleetCopy().toString()); // TO DO BR: Comment
				}
			}
		}
	}
	// 3.a  nextTurn
	private void nextTurnPart1(float totalProd, float totalReserve)	{
		//xilmi: setting those to false before potentially leaving function as otherwise the message will keep coming every turn
		shipLimitReached = false;
		stargateCompleted = false;
		maxAllowedShipBCProd = -1;
		// if we switched designs, send previous ship BC to shipyard reserve
		if (design != prevDesign) {
			if (prevDesign instanceof DesignStargate)
				stargateBC += shipBC;
			else
				shipReserveBC += shipBC;
			shipBC = 0;
		}
		// prod gets planetary bonus, but not reserve
		float prodBC = pct()* totalProd * planet().productionAdj();
		// shipyard reserve will match production BC
		float rsvBC = pct() * totalReserve;
		float newBC = prodBC+rsvBC;
		float cost = design.cost();
		newShips = 0;

		if (colony().allocation(categoryType()) == 0)
			return;
		// should never happen anymore, but hey
		if (buildingObsoleteDesign()) {
			empire().addReserve(newBC);
			return;
		}

		Empire emp = colony().empire();
		if (buildingStargate) {
			stargateBC += newBC;
			if (stargateBC >= cost) {
				hasStargate = true;
				stargateCompleted = true;
				newShips++;
				if (!emp.divertColonyExcessToResearch())
					emp.addReserve(stargateBC - cost);
				if (emp.isPlayerControlled())
					goToDefaultDesign();
				else
					goToNextDesign();
				stargateBC = 0;
			}
			prevDesign = design;
			return;
		}
		else { // Shipbuilding
			float shipRsvBC = min(prodBC, shipReserveBC);
			shipBC += newBC;
			shipBC += shipRsvBC;
			shipReserveBC -= shipRsvBC;
			if (buildLimit == 0) {
				while (shipBC >= cost) {
					newShips++;
					shipBC -= cost;
				}
			}
			else {
				while ((shipBC >= cost) && (buildLimit > 0)) {
					newShips++;
					buildLimit--;
					shipBC -= cost;
				}
				if (buildLimit == 0) {
					shipLimitReached = true;
					if (!emp.divertColonyExcessToResearch())
						emp.addReserve(shipBC);
					shipBC = 0;
				}
			}
			if (newShips > 0) {
				ShipDesign shipDesign = (ShipDesign) design;
				shipDesign.addBuildCount(newShips);
				emp.shipLab().recordConstruction(shipDesign, newShips);
				emp.shipBuildingSystems().add(colony().starSystem());
				placeNewShipsInOrbit(shipDesign, newShips);
				if (emp.isPlayerControlled()) {
					log(colony().name(), " has constructed: ", str(newShips), " ", design.name());
					session().addShipsConstructed(shipDesign,  newShips);
				}
			}
		}
		prevDesign = design;
	}
	// 3.b nextTurnPart1
	private void placeNewShipsInOrbit(ShipDesign d, int count)	{
		Empire emp = colony().empire();
		StarSystem sys = colony().starSystem();
		int sysId = sys.id;
		int designId = d.id();

		// Make ships orbiting
		galaxy().ships.buildShips(emp.id, sysId, designId, count);

		// if we are rallying, note how many of which design we need to deploy later
		if (emp.isPlayerControlled() && emp.sv.hasRallyPoint(sysId) && emp.alliedWith(emp.sv.empId(sysId))) {
			int rallyDestSysId = id(emp.sv.rallySystem(sysId));

			// Join combat?
			if (options().rallyBuiltJoinCombat())
				// Default value, they are already orbiting add them to the copy
				rallyFleetCopy().addShips(designId, count);
			else
				// In that case move them to the non combating fleet now.
				galaxy().ships.rallyOrbitingShips(emp.id, sysId, designId, count, rallyDestSysId);
		}
	}
	// 4. Some combat may occurs, orbiting fleet size may have changed
	// 5. assesTurn
	@Override public void assessTurn()	{
		assessTurnPart1();
		clearFleetsCopies();
	}
	// 5.a assesTurn -> assesTurn
	private void assessTurnPart1()	{
		if (rallyFleetCopy == null)
			return;

		Colony c = colony();
		Empire emp = c.empire();
		int empId = emp.id;
		int sysId = c.starSystem().id;
		if (!(emp.sv.hasRallyPoint(sysId) && emp.alliedWith(emp.sv.empId(sysId))))
			return;

		StarSystem rallySys = emp.sv.rallySystem(sysId);
		if (rallySys == null)
			return;

		int rallyId = rallySys.id;
		galaxy().ships.rallyOrbitingShips(empId, sysId, rallyId, rallyFleetCopy, orbitFleetCopy);
	}
    public boolean hasStargate()              { return hasStargate; }
    public boolean stargateCompleted()        { return stargateCompleted; }
    void removeStargate()  { hasStargate = stargateCompleted = false; }
    public boolean shipLimitReached()         { return shipLimitReached; }
    public Design design()                    { return design; }
    public void design(Design d)              { design = d; }
    public void addQueuedBC(float d)          { queuedBC += d; }
    public int desiredShips()                 { return desiredShips; }
    public void addDesiredShips(int i)        { desiredShips += i; }
    public int buildLimit()                   { return buildLimit; }
    public void buildLimit(int i)             { buildLimit = max(0,i); }
    public String buildLimitStr() { return buildLimit == 0 ? text("MAIN_COLONY_SHIPYARD_LIMIT_NONE") : str(buildLimit); }
    public boolean incrementBuildLimit(int amt)  { buildLimit += amt;  return true; }
    public boolean decrementBuildLimit(int amt)  { 
        if (buildLimit == 0)
            return false;
        
        buildLimit = max(0, buildLimit - amt);
        return true;
    }
    public boolean resetBuildLimit()         {
        if (buildLimit == 0)
            return false;
        buildLimit = 0;
        return true;
    }
    private float maxAllowedShipBCProd() {
        if (maxAllowedShipBCProd < 0)
            maxAllowedShipBCProd = empire().governorAI().maxShipBCPermitted(colony())* planet().productionAdj();
        return maxAllowedShipBCProd;
    }
    @Override
    public void init(Colony c) {
        super.init(c);
        hasStargate = false;
        buildingStargate = false;
        design = null;
        prevDesign = null;
        stargateBC = 0;
        shipBC = 0;
        newShips = 0;
        maxAllowedShipBCProd = -1;
        clearFleetsCopies();
    }
    @Override
    public int categoryType()            { return Colony.SHIP; }
    @Override
    public float totalBC()              { return super.totalBC() * planet().productionAdj(); }
    @Override
    public boolean isCompleted()         { return false; }
    @Override
    public boolean canLowerMaintenance() { return stargateMaintenanceCost() > 0; }
    @Override
    public void lowerMaintenance()       { hasStargate = false; }
    public boolean building()            { return queuedBC > 0; }
    private boolean willingToBuild(Design d) {
        if (design == d)
            return queuedBC < maxAllowedShipBCProd();
        else
            return queuedBC == 0;
    }
    public void resetQueueData() {
        queuedBC = 0;
        desiredShips = 0;
    }
    public boolean buildingObsoleteDesign() {
        return !design.active();
    }
    /* public float queuedBCForDesign(Design d) {
        if (prevDesign != d)
            return 0;
        else if (buildingStargate)
            return stargateBC;
        else
            return shipBC;
    } */
    public float turnsToBuild(Design d) {
        if (!willingToBuild(d))
            return Integer.MAX_VALUE;

        float planetProd = maxAllowedShipBCProd();
        float alreadyDone = 0;

        if (d == prevDesign)
            alreadyDone = shipBC - queuedBC;
        else
            alreadyDone = min(shipBC, planetProd) - queuedBC;

        return (d.cost() - alreadyDone) / planetProd;
    }
    public float stargateMaintenanceCost() {
        return hasStargate ? TechStargate.MAINTENANCE : 0;
    }
	float maintenanceCost()			{ return 0; }
	void capturedBy(Empire newCiv)	{
        if (newCiv == empire())
            return;
        hasStargate = false;
        buildingStargate = false;
        stargateCompleted = false;
        stargateBC = 0;
        shipBC = 0;
        shipReserveBC = 0;
        newShips = 0;
        buildLimit = 0;
        resetQueueData();
        shipLimitReached = false;
        clearFleetsCopies();
        maxAllowedShipBCProd = -1;
        if (newCiv.isPlayer())
        	goToDefaultDesign(); 
    }
    public void goToPrevDesign() {
        design = empire().shipLab().prevDesignFrom(design, hasStargate);
        buildingStargate = design == empire().shipLab().stargateDesign();
    }
    public void goToNextDesign() {
        design = empire().shipLab().nextDesignFrom(design, hasStargate);
        buildingStargate = design == empire().shipLab().stargateDesign();
    }
    public void switchToDesign(Design d) {
        design = d;
        buildingStargate = d instanceof DesignStargate;
    }
	public void goToDefaultDesign()	{
		design = empire().shipLab().defaultDesign(defaultDesignId);
		buildingStargate = design == empire().shipLab().stargateDesign();
	}
	public void defaultDesignId(Integer id)	{ defaultDesignId = id; }
	//public Integer defaultDesignId()  { return defaultDesignId==null? empire().defaultDesignId(): defaultDesignId; }

    public boolean canCycleDesign()   { return design.scrapped() || (empire().shipLab().numDesigns() > 1) || canBuildStargate(); }
    public boolean canBuildStargate() { return tech().canBuildStargate() && !hasStargate; }
    boolean buildingStargate()	{ return buildingStargate; }
	boolean buildStargate()		{
		Design stargate	= empire().shipLab().stargateDesign();
		Design first	= design();
		while (!stargate.equals(design)) {
			goToNextDesign();
			if (design.equals(first)) {
				System.out.println("unable to cycle to Shargate design");
				return false;
			}
		}
		return true;
	}
    public int upcomingShipCount()    {
        if (buildingObsoleteDesign())
            return 0;
        if (colony().allocation(categoryType()) == 0)
            return 0;
    	return upcomingShipCount(pct());
    }
    private int upcomingShipCount(float pct) {
        float tmpShipReserveBC = shipReserveBC;
        float tmpShipBC = shipBC;
//        float tmpStargateBC = stargateBC;
        float accumBC = buildingStargate ? stargateBC : shipBC;
        // if we switched designs, send previous ship BC to shipyard reserve
        if (design != prevDesign) {
            if (!(prevDesign instanceof DesignStargate))
                tmpShipReserveBC += tmpShipBC;
//            if (prevDesign instanceof DesignStargate)
//                tmpShipReserveBC += tmpStargateBC;
//            else
//                tmpShipReserveBC += tmpShipBC;
            accumBC = 0;
        }
        float prodBC = pct * colony().totalProductionIncome() * planet().productionAdj();
        float rsvBC  = pct * colony().maxReserveIncome();
        float newBC  = prodBC + rsvBC;
        float totalBC = max(newBC + accumBC, 0);
        float cost = design.cost();

        // add BC from shipyard reserve if buildings(ship rsv is capped by prod)
        if (!buildingStargate) 
            totalBC = totalBC+min(tmpShipReserveBC,prodBC);

        if (totalBC < cost)
            return 0;
        
        if (buildingStargate)
            return 1;
        if (buildLimit == 0) 
            return (int) (totalBC / cost);

        return min(buildLimit, (int) (totalBC / cost));
    }
    /* public String buildLimitResult() { // BR: Commented Unused
        return text("MAIN_COLONY_SHIPYARD_LIMIT",buildLimit());
    } */
    @Override public float[] excessSpending() {
        if (colony().allocation(categoryType()) == 0)
            return new float[] {0, 0};

        float rawProdBC = pct() * colony().totalProductionIncome();
        float prodBC = rawProdBC * planet().productionAdj();
        float rsvBC = pct() * colony().maxReserveIncome();
        float totalBC = prodBC+rsvBC;
        float researchFactor = (rawProdBC+rsvBC) / totalBC;

        // if building ships with no build limit, then no possible overflow
        if (!buildingStargate && (buildLimit == 0))
            return new float[] {0, 0};

        int numBuild = buildLimit;
        if (buildingStargate) {
            totalBC += stargateBC;
            numBuild = 1;
        }
        else
            totalBC += shipBC;

        float buildCost = numBuild * design.cost();

        if (buildCost > totalBC)
            return new float[] {0, 0};

        float reserveBC  = totalBC - buildCost;
        float researchBC = reserveBC * researchFactor;
        return new float[] {reserveBC, researchBC};
    }
    @Override
    public String upcomingResult() {
        if (colony().allocation(categoryType()) == 0)
            return noneText;

        float tmpShipReserveBC = shipReserveBC;
        float tmpShipBC = shipBC;
//        float tmpStargateBC = stargateBC;
        float accumBC = buildingStargate ? stargateBC : shipBC;
        // if we switched designs, send previous ship BC to shipyard reserve
        if (design != prevDesign) {
            if (!(prevDesign instanceof DesignStargate))
                tmpShipReserveBC += tmpShipBC;
//            if (prevDesign instanceof DesignStargate)
//                tmpShipReserveBC += tmpStargateBC;
//            else
//                tmpShipReserveBC += tmpShipBC;
            accumBC = 0;
        }
        float prodBC = pct()* colony().totalProductionIncome() * planet().productionAdj();
        float rsvBC = pct() * colony().maxReserveIncome();
        float newBC = prodBC+rsvBC;
        float totalBC = max(newBC+accumBC, 0);
        float cost = design.cost();
        float fromReserve = 0;

        // add BC from shipyard reserve if buildling ship (ship rsv is capped by prod)
        if (!buildingStargate) 
        	fromReserve = min(tmpShipReserveBC,prodBC);
        totalBC += fromReserve;

        if (totalBC == 0)
            return text(noneText);

        if (buildingObsoleteDesign())
            return overflowText();

        // returns how many years if we are not spending enough to finish even 1 
        float missingBC = cost-totalBC;
        if (missingBC>0) {
            if (newBC == 0)
                return text(noneText);
            else {
            	int turns = (int) Math.ceil(missingBC/newBC);
            	tmpShipReserveBC-=fromReserve;
            	if(tmpShipReserveBC>0) {
            		int reserveTurns = (int) (tmpShipReserveBC/newBC);
            		turns=(int) Math.ceil(missingBC/(2*newBC));
            		if (turns > reserveTurns) {
            			turns = reserveTurns+1;
            			missingBC-= (tmpShipReserveBC+turns*newBC);
            			if (missingBC>0)
            				turns += (int) Math.ceil(missingBC/newBC);
            		}
            	}
            	turns+=1;
                // int turns = (int) Math.ceil((cost - accumBC) / newBC);
            	// BR: Fixed turns estimation when ship reserve is used
                if (turns == 1)
                    return text(yearText, 1);
                else if (turns > 99)
                    return text(yearsLongText, turns);
                else
                    return text(yearsText, turns);
            }
        }

        // if building stargate, anything after 1 is overflow
        if (buildingStargate)
            return overflowText();

        // if building ships with no limit, specify how many ships
        if (buildLimit == 0)  {
            int  ships = (int) (totalBC / cost);
            if (ships == 1)
                return text(yearText, "1");
            else
                return text(perYearText, ships);            
        }

        float totalCost = buildLimit * cost;

        // not spending enough to hit the build limit, specify how many ships
        if (totalBC <= totalCost) {
            int  ships = (int) (totalBC / cost);
            if (ships == 1)
                return text(yearText, "1");
            else
                return text(perYearText, ships);
        }

        // we are exceeding limit, so result is overflow
        return overflowText();
    }
    public float maxSpendingNeeded() {
        float totalCost = 0;
        if (buildingStargate)
            totalCost = max(0, design.cost() - stargateBC);
        else {
            float shipCost = design.cost() * max(desiredShips, buildLimit);
            if (design == prevDesign)
                shipCost -= shipBC;

            totalCost = max(0, shipCost);
        }

        // adjust cost for planetary production
        // assume any amount over current production comes from reserve (no adjustment)
        float totalBC = (colony().totalProductionIncome() * planet().productionAdj()) + colony().maxReserveIncome();
        if (totalCost > totalBC)
            totalCost += colony().totalProductionIncome() * (1 - planet().productionAdj());
        else
            totalCost *= colony().totalIncome() / totalBC;

        return totalCost;
    }
    int maxAllocationNeeded()	{ return maxAllocationNeeded(colony().totalIncome()); }
    int maxAllocationNeeded(float totalIncome)	{
        float needed = maxSpendingNeeded();
        if (needed <= 0)
            return 0;
        float pctNeeded = min(1, needed / totalIncome);
        int ticks = (int) Math.ceil(pctNeeded * MAX_TICKS);
        return ticks;
    } 
    @Override public int smoothAllocationNeeded(boolean prioritized) {
    	int buildTarget = buildLimit();
    	if (buildingStargate)
    		buildTarget = 1;
    	else if (buildTarget == 0)
    			return prioritized? MAX_TICKS : 0;

    	// Start at 1 tick, as even if 0BC are needed, no tick = no ship
    	for (int tick=1; tick<=MAX_TICKS; tick++) {
    		if (upcomingShipCount((float) tick / MAX_TICKS) >= buildTarget)
    			return tick;
    	}
        return MAX_TICKS;
    }
    @Override public int smartAllocationNeeded(MouseEvent e) {
    	if (e==null || SwingUtilities.isLeftMouseButton(e)) // Target limit
    		return smoothAllocationNeeded(false);
    	if (SwingUtilities.isRightMouseButton(e)) // Max Available
    		return MAX_TICKS;
    	if (SwingUtilities.isMiddleMouseButton(e)) // AI suggestion, but free for future uses
    		return maxAllocationNeeded();
    	return 0;
    }
    @Override public int refreshAllocationNeeded(boolean prioritized, boolean hadShipSpending, float targetPopPercent) {
   		return smoothAllocationNeeded(prioritized || hadShipSpending);
    }
}
