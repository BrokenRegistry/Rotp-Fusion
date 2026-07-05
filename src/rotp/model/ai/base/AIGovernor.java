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
package rotp.model.ai.base;

import rotp.model.ai.interfaces.Governor;
import rotp.model.colony.Colony;
import rotp.model.colony.ColonySpendingCategory;
import rotp.model.empires.Empire;
import rotp.model.galaxy.StarSystem;
import rotp.util.Base;

public final class AIGovernor implements Base, Governor {
    public static final int SHIP = Colony.SHIP;
    public static final int DEFENSE = Colony.DEFENSE;
    public static final int INDUSTRY = Colony.INDUSTRY;
    public static final int ECOLOGY = Colony.ECOLOGY;
    public static final int RESEARCH = Colony.RESEARCH;
    private final Empire empire;

    public AIGovernor (Empire c) {
        empire = c;
    }
    @Override
    public void setInitialAllocations(Colony col) {
        baseSetColonyAllocations(col);
    }
    @Override
    public void setColonyAllocations(Colony col) {
        baseSetColonyAllocations(col);
        col.validate();
    }
    private void baseSetColonyAllocations(Colony col) {                
        int maxAllocation = ColonySpendingCategory.MAX_TICKS;

        // for systems that have a research project, focus research and forget
        // everything else until the project is done
        if (col.research().hasProject()) {
            float totalProd = col.totalIncome();
            float cleanCost = col.minimumCleanupCost();
            col.clearSpending();
            col.pct(ECOLOGY, cleanCost/totalProd);
            col.allocation(RESEARCH, maxAllocation - col.totalAmountAllocated());
            return;
        }

        // for systems that are flagged as rush defense, do that and forget
        // everything else until the project is done
        if (empire.generalAI().rushDefenseSystems().contains(col.starSystem())) {
            if (col.defense().maxSpendingNeeded() > 0) {
                float totalProd = col.totalIncome();
                float cleanCost = col.minimumCleanupCost();
                col.clearSpending();
                col.pct(ECOLOGY, cleanCost/totalProd);
                col.allocation(DEFENSE, maxAllocation - col.totalAmountAllocated());
                return;
            }
        }
        
        // for systems that are flagged as rush ship, do that and forget
        // everything else until the project is done
        if (empire.generalAI().rushShipSystems().contains(col.starSystem())) {
            float totalProd = col.totalIncome();
            float cleanCost = col.minimumCleanupCost();
            col.clearSpending();
            col.pct(ECOLOGY, cleanCost/totalProd);
            col.allocation(SHIP, maxAllocation - col.totalAmountAllocated());
            return;
        }

        // calc this now before spending amts are  reset
        float maxShipBCNeeded = col.shipyard().maxSpendingNeeded();
        float maxShipBC = maxShipBCPermitted(col);
        float shipPctSpending = shipPctForColony(col);
        float currentNet = col.totalIncome() - col.minimumCleanupCost();
        // # of turns we could make ship with 100% ship
        float shipTurns = maxShipBCNeeded/(currentNet*shipPctSpending);
        // pct increase of factories we could make with 100% industry
        float maxNewFactories = min(col.industry().maxUseableFactories()-col.industry().factories(), currentNet/col.industry().newFactoryCost());
        float factoryIncreasePct = maxNewFactories/col.industry().factories();

        suggestMissileBaseCount(col);
        col.clearSpending();

        lowerExpenses(col);
        float totalProd = col.totalIncome();
        float cleanCost = col.minimumCleanupCost();
        float netProd = totalProd - cleanCost;
        float shipCost = 0;
        // calculate minimum eco cleanup pct
        col.pct(ECOLOGY, cleanCost/totalProd);

        if (col.allocation(ECOLOGY) < 0) {
            err("Minimum cleanup cost < 0");
            throw new RuntimeException("Minimum cleanup cost < 0");
        }

        // don't change ship allocation for players else reset to zero
        if (empire.isPlayerControlled()) {
            shipCost = totalProd * col.pct(SHIP);
            if (shipCost > netProd) {
                col.pct(SHIP, netProd/totalProd);
                shipCost = col.pct(SHIP) * totalProd;
            }
        }
        else {
            shipCost = 0;
            col.pct(SHIP, 0);
        }

        // modnar: set 70% research overhead for inner colonies >85% full production
		// or 20% research overhead for non-inner colonies >90% full production (not just border colonies)
		// not applicable to rich/ultra-rich
		// no need to allocate anything here, should be added in automatically to research at the end
		int bases = (int) col.defense().bases();
        int maxBases = col.defense().maxBases();
		float resOverhead = 0.1f*netProd;
		StarSystem sys = col.starSystem();
		float prodPct = col.currentProductionCapacity();
		if (bases >= maxBases) { // only if missile bases are in place
			if ((prodPct > 0.85) && empire.sv.isInnerSystem(sys.id) && !col.planet().isResourceRich() && !col.planet().isResourceUltraRich()) { 
				netProd -= 7*resOverhead;
			}
			if ((prodPct > 0.9) && !empire.sv.isInnerSystem(sys.id) && !col.planet().isResourceRich() && !col.planet().isResourceUltraRich()) { 
				netProd -= 2*resOverhead;
			}
		}
		
        // ship spending, if requested
        if (!col.shipyard().buildingObsoleteDesign()
        && (col.shipyard().desiredShips() > 0)
        && ((1.0/shipTurns) > factoryIncreasePct)){
            shipCost = min(maxShipBC, col.shipyard().maxSpendingNeeded());
            float shipPct = shipCost/totalProd;
            col.pct(SHIP, shipPct);
            shipCost = col.pct(SHIP) * totalProd;
        }
        netProd -= shipCost;

        if (col.totalAmountAllocated() >= maxAllocation)
            return;

        // prod spending gets up to 100% of planet's remaining net prod
        float prodCost = min(netProd, col.industry().maxSpendingNeeded());
        col.pct(INDUSTRY, prodCost/totalProd);
        prodCost = col.pct(INDUSTRY) * totalProd;
        netProd -= prodCost;

        if (col.totalAmountAllocated() >= maxAllocation)
            return;

        // eco spending gets up to 40% of planet's remaining net prod
        float nonCleanEcoCost = col.ecology().maxSpendingNeeded() - cleanCost;
        float ecoCost = min((netProd * .4f), nonCleanEcoCost);
        col.pct(ECOLOGY, (ecoCost + cleanCost)/totalProd);

        if (col.pct(ECOLOGY) < 0) {
            err("Eco pct < 0");
            throw new RuntimeException("Minimum cleanup cost < 0");
        }

        ecoCost = col.pct(ECOLOGY) * totalProd;
        netProd -= (ecoCost - cleanCost);

        if (col.totalAmountAllocated() >= maxAllocation)
            return;

        // modnar: reduce defense spending, "up to 30%" (previous 50%)
        float defCost = min((netProd * .3f), col.defense().maxSpendingNeeded());
        col.pct(DEFENSE, defCost/totalProd);
        defCost = col.pct(DEFENSE) * totalProd;

        if (col.totalAmountAllocated() >= maxAllocation)
            return;

        // research gets the rest
        int totalAlloc = col.allocation(SHIP)+col.allocation(DEFENSE)+col.allocation(INDUSTRY)+col.allocation(ECOLOGY);
        col.allocation(RESEARCH, maxAllocation - totalAlloc);

        // check to allocate reserve
        // modnar: reduce to 0%, since it's taken care of by the AICTreasurer (?)
        if (col.planet().noArtifacts() && (col.pct(RESEARCH) > 0.5) ) {
            int rsvAmt = (int) Math.min(0.0, col.pct(RESEARCH) - 0.5);
            col.addPct(RESEARCH, -rsvAmt);
            col.addPct(INDUSTRY, rsvAmt);
        }

        for (int i=0;i<col.spending.length;i++)
            col.locked(i, false);
    }
    public void suggestMissileBaseCount(Colony col)	{ suggestMissileBaseCount(col, col.production()); }
    public void suggestMissileBaseCount(Colony col, float prod) {
        if (empire.contacts().isEmpty())  {
            col.defense().maxBases(0);
            return;
        }
        StarSystem sys = col.starSystem();
        int currBases = col.defense().missileBases();
        if (sys == null)  // this can happen at startup
            col.defense().maxBases(0);
        else if (empire.sv.isAttackTarget(sys.id))
            col.defense().maxBases(max(currBases, (int)(col.production()/30))); // modnar: reduce base count
        else if (empire.sv.isBorderSystem(sys.id))
            col.defense().maxBases(max(currBases, (int)(col.production()/40))); // modnar: reduce base count
        else
            col.defense().maxBases(max(currBases, (int)(col.production()/50)));
    }
}
