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
package rotp.model.ai.player;

import rotp.model.ai.interfaces.Governor;
import rotp.model.colony.Colony;
import rotp.model.empires.Empire;
import rotp.model.galaxy.StarSystem;
import rotp.util.Base;

public final class AIGovernor implements Base, Governor {
	private final Empire empire;

	public AIGovernor (Empire c)	{ empire = c; }
	@Override public void setInitialAllocations(Colony col)	{ baseSetAutoPilotAllocations(col); }
	@Override public void setColonyAllocations(Colony col)	{
		//System.out.println(galaxy().currentTurn()+" "+empire.name()+" "+col.name()+" called setColonyAllocations.");
		final StarSystem sys = col.starSystem();
		final String name = empire.sv.name(sys.id);
		final boolean cleanupOK = ensureMinimumCleanup(col);
		final int bases = col.defense().activeBases();
		final int maxBases = col.defense().maxBases();
		if (col.shipyard().design().scrapped()) {
			if (col.shipyard().building() || (col.shipyard().allocation() > 0))
				session().addSystemToAllocate(sys, text("MAIN_ALLOCATE_DESIGN_SCRAPPED", name));
			else
				col.shipyard().goToNextDesign();
		}
		if (col.shipyard().stargateCompleted())
			session().addSystemToAllocate(sys, text("MAIN_ALLOCATE_STARGATE_COMPLETE", name));
		if (col.shipyard().shipLimitReached())
			session().addSystemToAllocate(sys, text("MAIN_ALLOCATE_SHIPS_COMPLETE", name, col.shipyard().design().name()));
		if (col.defense().shieldCompleted())
			session().addSystemToAllocate(sys, text("MAIN_ALLOCATE_SHIELD_COMPLETE", name, col.empire().tech().topPlanetaryShieldTech().name()));
		if ((bases > 0) && (bases >= maxBases) && col.defense().missileBasesUpgraded())
			session().addSystemToAllocate(sys, text("MAIN_ALLOCATE_BASES_UPGRADED", name, col.empire().tech().topBaseMissileTech().name()));
		if ((bases > 0) && col.defense().missileBasesCompletedThisTurn())
			session().addSystemToAllocate(sys, text("MAIN_ALLOCATE_BASES_COMPLETE", name, col.defense().maxBases()));
		if (col.industry().isCompletedThisTurn())
			session().addSystemToAllocate(sys, text("MAIN_ALLOCATE_MAX_FACTORIES", name, (int)col.industry().maxBuildableFactories()));
		if (!cleanupOK)
			session().addSystemToAllocate(sys, text("MAIN_ALLOCATE_ECO_LOCKED_WASTE", name));
		if (col.ecology().populationGrowthCompletedThisTurn())
			session().addSystemToAllocate(sys, text("MAIN_ALLOCATE_MAX_POPULATION", name, (int)col.maxSize()));
		if (col.ecology().atmosphereCompletedThisTurn())
			session().addSystemToAllocate(sys, text("MAIN_ALLOCATE_ATMOSPHERE_COMPLETE", name));
		if (col.ecology().soilEnrichCompletedThisTurn()) {
			if (col.planet().isEnvironmentGaia())
				session().addSystemToAllocate(sys, text("MAIN_ALLOCATE_GAIA_COMPLETE", name));
			else
				session().addSystemToAllocate(sys, text("MAIN_ALLOCATE_FERTILE_COMPLETE", name));
		}
		if (col.ecology().terraformCompletedThisTurn()) 
			session().addSystemToAllocate(sys, text("MAIN_ALLOCATE_TERRAFORM_COMPLETE", name));
		if (col.research().hasCompletedProject()) 
			session().addSystemToAllocate(sys, text("MAIN_ALLOCATE_PROJECT_ENDED", name, col.research().completedProject().projectKey()));
		if (col.hasNewOrders() || (col.allocationRemaining() != 0) || session().awaitingAllocation(sys)) {
			baseSetAutoPilotAllocations(col);
			col.validate();
		}
	}
	private boolean ensureMinimumCleanup(Colony col)	{
		// return true if eco spending is set to enough for waste
		final float totalProd = col.totalIncome();
		final float minEco = col.minimumCleanupCost();
		final float minPct = min(1f, minEco/totalProd);

		// if locked and insufficient ECO spending, return false
		if (col.locked(ECOLOGY))
			return col.pct(ECOLOGY) >= minPct;

		if (minPct < 0)
			err("Minimum cleanup pct: ", str(minPct), "  totalProd:",str(totalProd), "   minEco:", str(minEco));

		if (col.pct(ECOLOGY) < minPct)
			col.setCleanupPct(minPct);
		return true;
	}
	private void baseSetAutoPilotAllocations(Colony col) {
		final int prevShip	= col.shipyard().allocation();
		final int prevDef	= col.defense().allocation();
		final int prevInd	= col.industry().allocation();
		final int prevEco	= col.ecology().allocation();
		final int prevRes	= col.research().allocation();

		final int cleanEco	= col.ecology().cleanupAllocationNeeded();
		final int maxInd	= col.industry().maxAllocationNeeded();
		final int maxEco	= col.ecology().terraformAllocationNeeded();
		final int maxEco2	= col.ecology().maxAllocationNeeded();
		final int maxDef	= col.defense().maxAllocationNeeded();
		final int orderInd	= col.industry().orderedAllocation();
		final int orderEco	= col.ecology().orderedAllocation();
		final int orderDef	= col.defense().orderedAllocation();

		// reset all unlocked allocations to zero
		col.clearUnlockedSpending();
		col.hasNewOrders(false);

		// 1. spend ECO for cleaning before anything else
		if (!col.locked(ECOLOGY))
			col.addAllocation(ECOLOGY, min(col.allocationRemaining(), cleanEco-col.allocation(ECOLOGY)));

		// 2. NOW ENSURE ORDERED AMOUNTS ARE MET (orders set when techs are learned)
		// priority of orders is: industry, ecology, defense, ship, research
		// but do not exceed the max needed to finish the project
		if (!col.locked(INDUSTRY))
			col.setAllocation(INDUSTRY,	min(orderInd, maxInd));
		if (!col.locked(ECOLOGY))
			col.setAllocation(ECOLOGY,	min(orderEco, maxEco));
		if (!col.locked(DEFENSE))
			col.setAllocation(DEFENSE,	min(orderDef, maxDef));

		// 3. Unless we have just completed building a stargate or reached a ship
		// limit, ensure that SHIP spending 
		// is maintained. Ship spending is never allocated for player colonies by the AI 
		//Governor so any spending here must be treated similarly to a player order
		if (!col.locked(SHIP) 
				&& !col.shipyard().stargateCompleted()
				&& !col.shipyard().shipLimitReached())
			col.setAllocation(SHIP, prevShip);

		// 4. now fill any remaining build requirements for ind/eco/def
		// being careful not to exceed previous spending in that category
		// i.e. if we raise spending in a category, it is only because
		// it was ordered by a new technology
		if (!col.locked(INDUSTRY))
			col.setAllocation(INDUSTRY,	min(prevInd, maxInd));
		if (!col.locked(ECOLOGY))
			col.setAllocation(ECOLOGY,	min(prevEco, maxEco));
		if (!col.locked(DEFENSE))
			col.setAllocation(DEFENSE,	min(prevDef, maxDef));

		// if this is a ship-building-colony that is not researching put rest in ships
		if(!col.locked(SHIP)
				&& prevShip > 0
				&& prevRes == 0
				&& !col.shipyard().shipLimitReached()
				&& !col.shipyard().stargateCompleted())
			col.addAllocation(SHIP, col.allocationRemaining());

		// SPEND THE EXCESS
		// if there is industry left to build, go there first
		if (!col.locked(INDUSTRY))
			col.setAllocation(INDUSTRY,	maxInd);
		// if there is terraforming left to build, go there first
		if (!col.locked(ECOLOGY))
			col.setAllocation(ECOLOGY,	maxEco);
		// if there is defense left to build, go there next
		if (!col.locked(DEFENSE))
			col.setAllocation(DEFENSE,	maxDef);
		// if there is population to grow, go there
		if (!col.locked(ECOLOGY))
			col.setAllocation(ECOLOGY,	maxEco2);

		// if research not locked go there
		if (!col.locked(RESEARCH))
			col.addAllocation(RESEARCH,	col.allocationRemaining());
		else if (!col.locked(INDUSTRY))
			col.addAllocation(INDUSTRY,	col.allocationRemaining());
		else if (!col.locked(ECOLOGY))
			col.addAllocation(ECOLOGY,	col.allocationRemaining());
		else if (!col.locked(DEFENSE))
			col.addAllocation(DEFENSE,	col.allocationRemaining());
		else if (!col.locked(SHIP))
			col.addAllocation(SHIP, col.allocationRemaining());
	}
}
