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
import java.io.Serializable;

import rotp.model.empires.Empire;
import rotp.model.planet.Planet;
import rotp.model.tech.TechTree;
import rotp.util.Base;

public abstract class ColonySpendingCategory implements Base, Serializable {
    private static final long serialVersionUID = 1L;
	static final String noneText = "MAIN_COLONY_SPENDING_NONE";
	static final String reserveText = "MAIN_COLONY_SPENDING_RESERVE";
	private static final String techText = "MAIN_COLONY_SPENDING_TECH";
	// static final String convertAlienFactoriesText = "MAIN_COLONY_SPENDING_CONVERT";
	static final String refitFactoriesText = "MAIN_COLONY_SPENDING_REFIT";
	// static final String maximumFactoriesText = "MAIN_COLONY_SPENDING_MAX_FACT";
	static final String wasteText = "MAIN_COLONY_SPENDING_WASTE";
	static final String atmosphereText = "MAIN_COLONY_SPENDING_ATMOSPHERE";
	static final String enrichSoilText = "MAIN_COLONY_SPENDING_ENRICH_SOIL";
	static final String terraformText = "MAIN_COLONY_SPENDING_TERRAFORM";
	static final String cleanupText = "MAIN_COLONY_SPENDING_CLEANUP";
	static final String growthText = "MAIN_COLONY_SPENDING_GROWTH";
	static final String shieldText = "MAIN_COLONY_SPENDING_SHIELD";
	static final String upgradeBasesText = "MAIN_COLONY_SPENDING_UPG_BASES";
	static final String researchPointsText = "MAIN_COLONY_SPENDING_RP";
	static final String yearsLongText = "MAIN_COLONY_COMPLETION_CENTURY";
	static final String yearsText = "MAIN_COLONY_COMPLETION_YEARS";
	static final String yearText = "MAIN_COLONY_COMPLETION_YEAR";
	static final String perYearText = "MAIN_COLONY_COMPLETION_PER_YEAR";
	public static final int MAX_TICKS = 50;

	protected final Colony colony;
	ColonySpendingCategory (Colony c)	{ colony = c; }

    /**
     * upcomingResult() is the text displayed to the player in the EmpireColonySpendingPane.
     * upcomingResult() is the *expected* result, based on information *known* to the player at the time.
     * nextTurn(), by contrast, will determine the *actual* result, which will sometimes be different.
     * In particular, GameSession.nextTurnProcess() checks for random events before resolving colony spending.
     * A ship that was expected to be built next year might not be built if an unexpected earthquake strikes the colony during the year.
    */
	public abstract String[] upcomingResult();

    public abstract int categoryType();
    public abstract boolean isCompleted();
    public abstract void nextTurn(float prod, float rsv);
    public abstract void assessTurn();

    public boolean isCompleted(int maxMissing) { return isCompleted(); }
    @Override
    public String toString()            { return str(allocation()); }
    public float totalBC()              { return pct() * colony.totalIncome(); }
    public float totalBCForEmpire()     { return totalBC(); }
    public int allocation()             { return colony.allocation(categoryType()); }
    float pct()                  { return (float)allocation()/ MAX_TICKS; }
    float totalAvailableBCthisCategory(float totalProd, float totalReserve) {
        float prodBC = pct() * totalProd;
        float rsvBC = pct() * totalReserve;
        return prodBC + rsvBC;
    }
    public boolean warning()            { return false; }
    String overflowText()        { 
        if (!empire().divertColonyExcessToResearch())
            return text(reserveText);
        else if (empire().tech().researchCompleted())
            return text(reserveText);
        else
            return text(techText);
    }
	Colony colony()				{ return colony; }
	Planet planet()				{ return colony.planet(); }
	Empire empire()				{ return colony.empire(); }
	TechTree tech()				{ return empire().tech(); }
    public float orderedValue()       { return colony.locked(categoryType()) ? pct() : 0; }
    public void removeSpendingOrders() { }
    public boolean canLowerMaintenance() { return false; }
    public void lowerMaintenance()       { }
    public int orderedAllocation()       { return (int) Math.ceil(orderedValue() * MAX_TICKS);  }  
    public int adjustValue(int amt)      {
        // attempt to adjust current value by amt
        // return the actual amount adjusted
        int oldValue = allocation();
        colony.allocation(categoryType(), bounds(0,oldValue+amt,MAX_TICKS));
        return allocation() - oldValue;
    }
    public float[] excessSpending()        { return new float[] {0, 0}; }
	public int smoothAllocationNeeded(boolean prioritized, float income)	{ return 0; }
	public int smartAllocationNeeded(MouseEvent e, float income)			{ return 0; }
	public int refreshAllocationNeeded(boolean prioritized, boolean hadShipSpending, float targetPopPct, float income)	{
		return smoothAllocationNeeded(prioritized, income);
	}
	public int govAllocationNeeded(boolean prioritized, GovWorksheet gws)	{
		return refreshAllocationNeeded(prioritized, gws.keepDirectShipAlloc, gws.targetPopPercent, gws.totalIncome);
	}
	float incomeAdjust()	{ return planet().productionAdj(); }
	class UpcomingResult	{
		// Population
		final float currentSize	= planet().currentSize();
		final float currentPop	= colony.population();
		final float sentPop		= colony.inTransport();
		final float nextTurnTr	= galaxy().friendlyPopApproachingSystemNextTurn(colony.starSystem());
		final float longTermTr	= galaxy().friendlyPopApproachingSystem(colony.starSystem());
		// Production
		final float workingPop	= currentPop - sentPop + nextTurnTr;
		final float missingPop	= currentSize - currentPop + sentPop + longTermTr;
		final float workerProd	= workingPop * empire().workerProductivity();
		final float usedFact	= (int) min(colony.industry().factories(), workingPop * colony.industry().effectiveRobotControls());
		final float nextProd	= workerProd + usedFact;
		final float nextRsv		= min(nextProd, colony.reserveIncome());
		final float raw2Net		= colony.totalProductionIncome() / colony.production();
		final float nextIncome	= nextProd * raw2Net * incomeAdjust(); // We expect the same charge ratio
		final float nextTotalBC	= nextIncome + nextRsv;
		final float nextBC		= max(0, pct() * nextTotalBC);
		String adviceHeader()	{
			String adv = text("MAIN_COLONY_HEADER_ADVISOR");
			adv += text("MAIN_COLONY_TOTAL_FUNDS_HELP", fmt(nextIncome, 1), fmt(nextRsv, 1), fmt(nextTotalBC, 1));
			if (nextBC == 0)
				adv += NEWLINE + text("MAIN_COLONY_NO_LOCAL_FUNDS_HELP");
			else
				adv += NEWLINE + text("MAIN_COLONY_LOCAL_FUNDS_HELP", fmt(pct()*100, 0), fmt(nextBC, 1));
			return adv;
		}
		String adviceSurplus(float bc)	{
			if (!empire().divertColonyExcessToResearch())
				return text("MAIN_COLONY_TO_TRESOR_HELP", fmt(bc/2));
			else if (empire().tech().researchCompleted())
				return text("MAIN_COLONY_TO_TRESOR_HELP", fmt(bc/2));
			else
				return text("MAIN_COLONY_TO_RESEARCH_HELP", fmt(bc));
		}
	}
}
