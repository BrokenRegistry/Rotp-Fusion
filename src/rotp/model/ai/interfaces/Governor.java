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
package rotp.model.ai.interfaces;

import rotp.model.ai.FleetPlan;
import rotp.model.ai.ShipPlan;
import rotp.model.colony.Colony;
import rotp.model.empires.SystemView;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.model.planet.Planet;

public interface Governor {
	void setInitialAllocations(Colony c);
	void setColonyAllocations(Colony c);

	default void lowerExpenses(Colony col)		{
		float totalProd = col.totalIncome();

		// does this colony have a positive income? If not, start canceling some activities
		// 1. start by reducing outgoing transports
		while ((totalProd <= 0) && col.canLowerMaintenance()) {
			col.lowerMaintenance();
			totalProd = col.totalIncome();
		}

		// 2. try reducing shipyard maintenance (stargate)
		while ((totalProd <= 0) && col.shipyard().canLowerMaintenance()) {
			col.shipyard().lowerMaintenance();
			totalProd = col.totalIncome();
		}

		// 3. try reducing defense maintenance (bases)
		while ((totalProd <= 0) && col.defense().canLowerMaintenance()) {
			col.defense().lowerMaintenance();
			totalProd = col.totalIncome();
		}
		col.validate();
	}
	default float maxShipBCPermitted(Colony c)	{
		float maxAllowed = Math.max(0, c.totalIncome() - c.wasteCleanupCost());
		return Math.min(maxAllowed, c.totalIncome() * shipPctForColony(c));
	}
	default boolean readyToBuild(Colony col, ShipPlan sh, int designCost) {
		float pct = col.currentProductionCapacity();
		float estProd = col.industry().factories()*col.planet().productionAdj();
		if (pct > 0.9)  // modnar: change to 90% to build anything
			return true;
		else if (pct > 0.75) // modnar: change to 75%, colonize is the lowest priority we can build
			return sh.plan.priority() >= FleetPlan.COLONIZE;

		return estProd > designCost*5;
	}
	default int suggestedEmpireTaxLevel()		{ return 0; } // this will hopefully be handled at the planet level, so return 0
	default float targetPopPct(SystemView sv)	{
		if (sv.borderSystem()) return .75f;

		Planet p = sv.system().planet();
		if (p.isResourceRich()) return .75f;
		if (p.isResourceUltraRich()) return .75f;
		if (p.isArtifact()) return .75f;
		if (p.isOrionArtifact()) return .75f;
		if (p.currentSize() <= 20) return .75f;

		if (sv.supportSystem()) return .5f;
		if (p.currentSize() <= 40) return .5f;

		return .25f;
	}
	default float shipPctForColony(Colony col)	{
		// 20% or research spending, whichever is greater
		float pct = Math.max(col.pct(Colony.SHIP)+col.pct(Colony.RESEARCH), .2f);
		// adjust upwards are downwards based on planet bonuses
		pct *= col.planet().productionAdj();
		pct /= col.planet().researchAdj();
		return Math.min(pct, 1);
	}

    // specific to Xilmi AI
    default float productionScore(StarSystem sys) { return 0; }
    default float expectedBombardDamageAsIfBasesWereThere(ShipFleet fl, StarSystem sys, int bases) { return 0; }
}
