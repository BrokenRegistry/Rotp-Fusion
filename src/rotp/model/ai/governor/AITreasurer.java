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
package rotp.model.ai.governor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import rotp.model.ai.interfaces.Treasurer;
import rotp.model.colony.Colony;
import rotp.model.colony.Colony.ColonyBudget;
import rotp.model.colony.Colony.ColonyList;
import rotp.model.empires.Empire;
import rotp.model.empires.Empire.EmpireBudget;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.GovernorOptions;

public final class AITreasurer implements Treasurer {
	private final Empire empire;
	private final EmpireBudget empireBudget;

	public AITreasurer (Empire emp)		{
		empire = emp;
		empireBudget = empire.budget();
	}
	@Override public String toString()	{ return concat("Treasurer: ", empire.raceName()); }
	@Override public void planTheBudget()	{
		GovernorOptions govOptions = session().getGovernorOptions();
		empireBudget.resetEmpireBudget();

		List<ColonyBudget> playerBudgets	= new ArrayList<>();
		List<ColonyBudget> governorBudgets	= new ArrayList<>();
		List<ColonyBudget> otherBudgets		= new ArrayList<>();
		for (StarSystem sys: empire.allColonizedSystems()) {
			if (sys == null)
				continue;
			Colony col = sys.colony();
			if (col == null || col.inRebellion())
				continue;
			ColonyBudget budget = col.budget();
			if (budget.isPlayerBudget()) {
				playerBudgets.add(budget);
				if (col.isGovernor()) // could be both
					governorBudgets.add(budget);
			}
			else if (col.isGovernor())
				governorBudgets.add(budget);
			else
				otherBudgets.add(budget);
		}
		requestSubsidies(playerBudgets, true);
		empireBudget.setUnusedPlayerReserves();
		requestSubsidies(governorBudgets, false);


		// Update Empire reserve status
		float allocatedBC = empire.empireExcessSpendingIncome();
		empireBudget.allocatedBC(allocatedBC);

		float ReserveNextTurn		= govOptions.autospendReserveNextTurn();
		float ReserveNextTurnRatio	= govOptions.autospendReserveNextTurnRatio();
		float totalProd = empire.totalPlanetaryProduction();
		empireBudget.totalProd(totalProd);
		float requestedReservesBC = max(ReserveNextTurn, totalProd * ReserveNextTurnRatio);
		empireBudget.requestedReserves(requestedReservesBC);

		List<ColonyBudget> remainingBudgets	= new ArrayList<>();
		remainingBudgets.addAll(playerBudgets);
		remainingBudgets.addAll(governorBudgets);
		remainingBudgets.addAll(otherBudgets);
		float developedProd = 0;
		float undevelopedProd = 0;

		boolean empireTaxUndeveloped = !empire.empireTaxOnlyDeveloped();
		float empireTaxRate	= empire.empireTaxRate();
		float taxedBC	= 0;

		for (ColonyBudget budget : remainingBudgets) {
			if (budget.colonyIsDeveloped()) {
				developedProd += budget.nextProduction();
				budget.budgetTaxeRate(empireTaxRate);
			}
			else {
				undevelopedProd += budget.nextProduction();
				if (empireTaxUndeveloped)
					budget.budgetTaxeRate(empireTaxRate);
			}
			taxedBC += budget.budgetTaxedBC();
		}
		empireBudget.taxedBC(taxedBC);
		float expectedRevenueBC = taxedBC + allocatedBC;
		empireBudget.expectedRevenueBC(expectedRevenueBC);
		empireBudget.developedProd(developedProd);
		empireBudget.undevelopedProd(undevelopedProd);
		empireBudget.allocatedBC(allocatedBC);


		// Plan Reserves
		boolean planReserveNextTurn	= govOptions.autospendPlanReserveNextTurn();
		boolean excessToResearch	= empire.divertColonyExcessToResearch();
		if (excessToResearch || !planReserveNextTurn)
			return;

		float excessRevenueBC = expectedRevenueBC - requestedReservesBC;
		float allowedExcessRevenue = 10;
		if (excessRevenueBC > allowedExcessRevenue) {
			Collections.sort(remainingBudgets, INC_RESOURCES);
			// reset excess spending
			for (ColonyBudget cBudget : remainingBudgets)
				if (cBudget.colonyIsDeveloped()) {
					excessRevenueBC -= cBudget.resetExess(2*excessRevenueBC, true, true);
					if (excessRevenueBC == 0)
						break;
					if (excessRevenueBC < 0) {
						// add one tick back
						excessRevenueBC += cBudget.tryToContribute(-2*excessRevenueBC, true);
						break;
					}
				}
		}

		if (excessRevenueBC < 0) {
			Collections.sort(remainingBudgets, DEC_RESOURCES);
			for (ColonyBudget cBudget : remainingBudgets)
				if (cBudget.colonyIsDeveloped() && !cBudget.isArtifact()) {
					excessRevenueBC += cBudget.tryToContribute(-2*excessRevenueBC, true);
					if (excessRevenueBC >= 0)
						break;
				}
		}
		expectedRevenueBC = excessRevenueBC + requestedReservesBC;
		empireBudget.expectedRevenueBC(expectedRevenueBC);
	}
	private final Comparator<ColonyBudget> INC_RESOURCES = (ColonyBudget b1, ColonyBudget b2) -> Integer.compare(b1.resourcesSort(), b2.resourcesSort());
	private final Comparator<ColonyBudget> DEC_RESOURCES = (ColonyBudget b1, ColonyBudget b2) -> Integer.compare(b2.resourcesSort(), b1.resourcesSort());
	private void requestSubsidies(List<ColonyBudget> budgets, boolean fromPlayer)	{
		GovernorOptions govOptions = session().getGovernorOptions();
		// first, help systems that are fighting plague or supernova research events
		boolean helpRandomEvent = false; // TODO BR: helpRandomEvent
		boolean newColoniesFirst = govOptions.isAutospendOnNewColoniesFirst();
		boolean spendOnNewColonies = govOptions.isAutospendOnNewColonies();
		boolean spendToBoostArtefact = govOptions.isAutospendOnArtefacts();
		float autospendMaxIndustryRatio = govOptions.autospendMaxIndustryPct();

		if (helpRandomEvent || fromPlayer)
			requestSubsidiesForRandomEvents(budgets, fromPlayer);

		if (!fromPlayer)
			requestSubsidiesForTaggedColonies(budgets);

		if (newColoniesFirst) {
			if (spendOnNewColonies)
				requestSubsidiesForNewColony(budgets, autospendMaxIndustryRatio, fromPlayer);
			if (spendToBoostArtefact)
				requestSubsidiesForArtefactColony (budgets, fromPlayer);
		}
		else {
			if (spendToBoostArtefact)
				requestSubsidiesForArtefactColony (budgets, fromPlayer);
			if (spendOnNewColonies)
				requestSubsidiesForNewColony(budgets, autospendMaxIndustryRatio, fromPlayer);
		}
		if (!fromPlayer)
			return;

		Collections.sort(budgets, Colony.INCREASING_BUDGET);
		for (ColonyBudget budget : budgets)
			empireBudget.budgetizeReserve(budget, fromPlayer);
		budgets.clear();
	}
	private void requestSubsidiesForRandomEvents(List<ColonyBudget> allBudgets, boolean fromPlayer)	{
		List<ColonyBudget> budgets = new ArrayList<>();
		for (Iterator<ColonyBudget> iter = allBudgets.iterator(); iter.hasNext(); ) {
			ColonyBudget budget = iter.next();
			if (budget.hasProject()) {
				budgets.add(budget);
				iter.remove();
			}
		}
		Collections.sort(budgets, Colony.INCREASING_BUDGET);
		for (ColonyBudget budget : budgets)
			empireBudget.budgetizeReserve(budget, fromPlayer);
	}
	private void requestSubsidiesForTaggedColonies(List<ColonyBudget> allBudgets)	{
		// Start with those allowed to pick in the reserve
		List<ColonyBudget> budgets = new ArrayList<>();
		for (Iterator<ColonyBudget> iter = allBudgets.iterator(); iter.hasNext(); ) {
			ColonyBudget budget = iter.next();
			if (budget.noMinumumReserve()) {
				budgets.add(budget);
				iter.remove();
			}
		}
		Collections.sort(budgets, Colony.INCREASING_BUDGET);
		for (ColonyBudget budget : budgets)
			empireBudget.budgetizeReserve(budget, false);

		// Then the others the reserve
		budgets.clear();
		for (Iterator<ColonyBudget> iter = allBudgets.iterator(); iter.hasNext(); ) {
			ColonyBudget budget = iter.next();
			if (budget.isGovernorBudget()) {
				budgets.add(budget);
				iter.remove();
			}
		}
		Collections.sort(budgets, Colony.INCREASING_BUDGET);
		for (ColonyBudget budget : budgets)
			empireBudget.budgetizeReserve(budget, false);
	}
	private void requestSubsidiesForNewColony (List<ColonyBudget> allBudgets, float maxIndustryRatio, boolean fromPlayer)	{
		List<ColonyBudget> budgets = new ArrayList<>();
		for (Iterator<ColonyBudget> iter = allBudgets.iterator(); iter.hasNext(); ) {
			ColonyBudget budget = iter.next();
			if (budget.isNewColony(maxIndustryRatio)) {
				budgets.add(budget);
				iter.remove();
			}
		}
		Collections.sort(budgets, Colony.INCREASING_BUDGET);
		for (ColonyBudget budget : budgets)
			empireBudget.budgetizeReserve(budget, fromPlayer);
	}
	private void requestSubsidiesForArtefactColony (List<ColonyBudget> allBudgets, boolean fromPlayer)	{
		List<ColonyBudget> budgets = new ArrayList<>();

		// Boost fully developed Orion Artefact colonies
		for (Iterator<ColonyBudget> iter = allBudgets.iterator(); iter.hasNext(); ) {
			ColonyBudget budget = iter.next();
			if (budget.isOrionArtifact() && budget.colonyIsDeveloped()) {
				budgets.add(budget);
				iter.remove();
			}
		}
		Collections.sort(budgets, Colony.DECREASING_BUDGET);
		for (ColonyBudget budget : budgets)
			empireBudget.budgetizeReserve(budget, fromPlayer);

		// Boost fully developed Antaran Artefact colonies
		budgets.clear();
		for (Iterator<ColonyBudget> iter = allBudgets.iterator(); iter.hasNext(); ) {
			ColonyBudget budget = iter.next();
			if (budget.isAntaran() && budget.colonyIsDeveloped()) {
				budgets.add(budget);
				iter.remove();
			}
		}
		Collections.sort(budgets, Colony.DECREASING_BUDGET);
		for (ColonyBudget budget : budgets)
			empireBudget.budgetizeReserve(budget, fromPlayer);

		// Boost undeveloped Orion Artefact colonies
		budgets.clear();
		for (Iterator<ColonyBudget> iter = allBudgets.iterator(); iter.hasNext(); ) {
			ColonyBudget budget = iter.next();
			if (budget.isOrionArtifact()) {
				budgets.add(budget);
				iter.remove();
			}
		}
		Collections.sort(budgets, Colony.DECREASING_BUDGET);
		for (ColonyBudget budget : budgets)
			empireBudget.budgetizeReserve(budget, fromPlayer);

		// Boost undeveloped Antaran Artefact colonies
		budgets.clear();
		for (Iterator<ColonyBudget> iter = allBudgets.iterator(); iter.hasNext(); ) {
			ColonyBudget budget = iter.next();
			if (budget.isAntaran()) {
				budgets.add(budget);
				iter.remove();
			}
		}
		Collections.sort(budgets, Colony.DECREASING_BUDGET);
		for (ColonyBudget budget : budgets)
			empireBudget.budgetizeReserve(budget, fromPlayer);
	}

	@Override public void allocateReserve(Empire empire)	{
		GovernorOptions govOptions = session().getGovernorOptions();
		boolean log = false;	// TO DO BR: set to false;

		boolean helpRandomEvent = false; // TODO BR: helpRandomEvent
		if (helpRandomEvent && autospendRandomEventsColony(0, log))
			return;

		// Start with the labeled colonies
		int minReserve = govOptions.getReserve();
		if (autospendTaggedColony(minReserve, true))
			return;

		if ((int)empire.totalReserve() <= minReserve)
			return;
		boolean newColoniesFirst = govOptions.isAutospendOnNewColoniesFirst();
		boolean shieldWithoutBases = govOptions.getShieldWithoutBases();
		boolean spendOnNewColonies = govOptions.isAutospendOnNewColonies();
		boolean spendToBoostArtefact = govOptions.isAutospendOnArtefacts();
		float autospendMaxIndustryPct = govOptions.autospendMaxIndustryPct();

		if (newColoniesFirst) {
			if (spendOnNewColonies && autospendOnNewColony(minReserve, autospendMaxIndustryPct, log))
				return;
			if (spendToBoostArtefact && autoSpendOnArtefact(minReserve, shieldWithoutBases, log))
				return;
		}
		else {
			if (spendToBoostArtefact && autoSpendOnArtefact(minReserve, shieldWithoutBases, log))
				return;
			if (spendOnNewColonies && autospendOnNewColony(minReserve, autospendMaxIndustryPct, log))
				return;
		}
	}
	private boolean autospendRandomEventsColony(int minReserve, boolean log)	{
		// if reserve is low, don't even attempt to spend money
		if ((int)empire.totalReserve() <= minReserve)
			return true;

		ColonyList colonies = new ColonyList();

		for (StarSystem sys : empire.allColonizedSystems())
			if (sys != null) {
				Colony c = sys.colony();
				if (c != null && !c.inRebellion() && c.isGovernor() && c.research().hasProject())
					colonies.add(c);
			}
		colonies.sortbyIncreasingProduction();
		return spendMoney(colonies, minReserve, log, "Tagged Colony");
	}
	/**
	 * Spend reserve automatically (if enabled).
	 *
	 * Spend the tagged Colonies first, even taking into the reserve.
	 * Spend only the amount planet can consume this turn
	 * Start with planet with lowest production, and end when money runs out
	 * or no suitable planets are available.
	 * Spend only on planets with governor on.
	 */
	private boolean autospendTaggedColony(int minReserve, boolean log)	{
		// Start with those allowed to pick in the reserve
		ColonyList colonies = new ColonyList();
		for (StarSystem sys : empire.allColonizedSystems())
			if (sys != null) {
				Colony c = sys.colony();
				if (c != null && !c.inRebellion() && c.isGovernor() && c.govFundColonyUpdated() && c.getFundFromReserve())
					colonies.add(c);
			}
		colonies.sortbyIncreasingProduction();
		if (spendMoney(colonies, 0, log, "Tagged Colony"))
			return true;

		// if reserve is low, don't even attempt to spend money
		if ((int)empire.totalReserve() <= minReserve)
			return true;

		colonies.clear();
		for (StarSystem sys : empire.allColonizedSystems())
			if (sys != null) {
				Colony c = sys.colony();
				if (c != null && !c.inRebellion() && c.isGovernor() && c.govFundColony())
					colonies.add(c);
			}

		colonies.sortbyIncreasingProduction();
		return spendMoney(colonies, minReserve, log, "Tagged Colony");
	}
	/**
	 * Spend reserve automatically (if enabled).
	 *
	 * Spend only on planets with production &lt; maxIndustryPct
	 * Spend only the amount planet can consume this turn
	 * Start with planet with lowest production, and end when money runs out
	 * or no suitable planets are available.
	 * Spend only on planets with governor on.
	 * Spend only if industry and ecology are not complete.
	 *
	 */
	private boolean autospendOnNewColony(int minReserve, float maxIndustryPct, boolean log)	{
		// if reserve is low, don't even attempt to spend money
		if ((int)empire.totalReserve() <= minReserve)
			return true;

		ColonyList colonies = new ColonyList();

		for (StarSystem sys : empire.allColonizedSystems())
			if (sys != null) {
				Colony c = sys.colony();
				if (c != null && !c.inRebellion() && c.isGovernor() && c.industry().completedPct()<=maxIndustryPct)
					colonies.add(c);
			}

		colonies.sortbyIncreasingProduction();
		return spendMoney(colonies, minReserve, log, "new Colony");
	}
	private boolean autoSpendOnArtefact(int minReserve, boolean shieldWithoutBases, boolean log)	{
		// if reserve is low, don't even attempt to spend money
		if ((int)empire.totalReserve() <= minReserve)
			return true;

		// Boost fully developed Artefact Colonies First

		// Boost fully developed Orion Artefact colonies
		ColonyList colonies = new ColonyList();
		for (StarSystem sys : empire.allColonizedSystems())
			if (sys != null && sys.planet().isOrionArtifact()) {
				Colony c = sys.colony();
				if (c != null && !c.inRebellion() && c.isGovernor() && c.isDeveloped(shieldWithoutBases))
					colonies.add(c);
			}
		colonies.sortbyDecreasingProduction();
		if (spendMoney(colonies, minReserve, log, "developped Orion Artefact colony"))
			return true; // Reserve is low, don't even attempt to spend more money

		// Boost fully developed Antaran Artefact colonies
		colonies.clear();
		for (StarSystem sys : empire.allColonizedSystems())
			if (sys != null && sys.planet().isAntaran()) {
				Colony c = sys.colony();
				if (c != null && !c.inRebellion() && c.isGovernor() && c.isDeveloped(shieldWithoutBases))
					colonies.add(c);
			}
		colonies.sortbyDecreasingProduction();
		if (spendMoney(colonies, minReserve, log, "developped Antaran Artefact colony"))
			return true; // Reserve is low, don't even attempt to spend more money

		// Boost undeveloped Orion Artefact colonies
		colonies.clear();
		for (StarSystem sys : empire.allColonizedSystems())
			if (sys != null && sys.planet().isOrionArtifact()) {
				Colony c = sys.colony();
				if (c != null && !c.inRebellion() && c.isGovernor() && !c.isDeveloped(shieldWithoutBases))
					colonies.add(c);
			}
		colonies.sortbyDecreasingProduction();
		if (spendMoney(colonies, minReserve, log, "New Orion Artefact colony"))
			return true; // Reserve is low, don't even attempt to spend more money

		// Boost undeveloped Antaran Artefact colonies
		colonies.clear();
		for (StarSystem sys : empire.allColonizedSystems())
			if (sys != null && sys.planet().isAntaran()) {
				Colony c = sys.colony();
				if (c != null && !c.inRebellion() && c.isGovernor() && !c.isDeveloped(shieldWithoutBases))
					colonies.add(c);
			}
		colonies.sortbyDecreasingProduction();
		return spendMoney(colonies, minReserve, log, "new Antaran Artefact colony");
	}
	private boolean spendMoney(ColonyList colonies, int minReserve, boolean log, String txt)	{
		if (log) {
			String header = "Autospend request on "+ txt + " ";
			for (Colony c: colonies)
				System.out.println(header + c.production() + " " + c.name());
		}

		for (Colony c: colonies) {
			if (c != null) {
				float maxReserveNeeded = c.maxReserveNeeded();
				if (maxReserveNeeded <= 0)
					continue;

				float available = empire.totalReserve() - minReserve;
				if (available <= 1)
					return true;

				// over-allocate by 1 BC to speed up fractional spending
				int bcToSpend = (int)Math.ceil(Math.min(available, maxReserveNeeded));
				empire.allocateReserve(c, bcToSpend);
				if (log)
					System.out.format("Autospend allocated %d BC to %s%n", bcToSpend, c.name());
			}
		}
		return false;
	}
}
