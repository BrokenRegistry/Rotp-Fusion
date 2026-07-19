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
	public static final String noneText = "MAIN_COLONY_SPENDING_NONE";
	public static final String reserveText = "MAIN_COLONY_SPENDING_RESERVE";
	public static final String techText = "MAIN_COLONY_SPENDING_TECH";
	public static final String convertAlienFactoriesText = "MAIN_COLONY_SPENDING_CONVERT";
	public static final String refitFactoriesText = "MAIN_COLONY_SPENDING_REFIT";
	public static final String maximumFactoriesText = "MAIN_COLONY_SPENDING_MAX_FACT";
	public static final String wasteText = "MAIN_COLONY_SPENDING_WASTE";
	public static final String atmosphereText = "MAIN_COLONY_SPENDING_ATMOSPHERE";
	public static final String enrichSoilText = "MAIN_COLONY_SPENDING_ENRICH_SOIL";
	public static final String terraformText = "MAIN_COLONY_SPENDING_TERRAFORM";
	public static final String cleanupText = "MAIN_COLONY_SPENDING_CLEANUP";
	public static final String growthText = "MAIN_COLONY_SPENDING_GROWTH";
	public static final String shieldText = "MAIN_COLONY_SPENDING_SHIELD";
	public static final String upgradeBasesText = "MAIN_COLONY_SPENDING_UPG_BASES";
	public static final String researchPointsText = "MAIN_COLONY_SPENDING_RP";
	public static final String yearsLongText = "MAIN_COLONY_COMPLETION_CENTURY";
	public static final String yearsText = "MAIN_COLONY_COMPLETION_YEARS";
	public static final String yearText = "MAIN_COLONY_COMPLETION_YEAR";
	public static final String perYearText = "MAIN_COLONY_COMPLETION_PER_YEAR";
    public static final int MAX_TICKS = 50;

    private Colony colony;

    /**
     * upcomingResult() is the text displayed to the player in the EmpireColonySpendingPane.
     * upcomingResult() is the *expected* result, based on information *known* to the player at the time.
     * nextTurn(), by contrast, will determine the *actual* result, which will sometimes be different.
     * In particular, GameSession.nextTurnProcess() checks for random events before resolving colony spending.
     * A ship that was expected to be built next year might not be built if an unexpected earthquake strikes the colony during the year.
    */
    public abstract String upcomingResult();

    public abstract int categoryType();
    public abstract boolean isCompleted();
    public abstract void nextTurn(float prod, float rsv);
    public abstract void assessTurn();

    public boolean isCompleted(int maxMissing) { return isCompleted(); }
    public ColonySpendingCategory () {  }
    @Override
    public String toString()            { return str(allocation()); }
    public float totalBC()              { return pct() * colony().totalIncome(); }
    public float totalBCForEmpire()     { return totalBC(); }
    public int allocation()             { return colony.allocation(categoryType()); }
    public float pct()                  { return (float)allocation()/ MAX_TICKS; }
    public float totalAvailableBCthisCategory(float totalProd, float totalReserve) {
        float prodBC = pct() * totalProd;
        float rsvBC = pct() * totalReserve;
        return prodBC + rsvBC;
    }
    public boolean warning()            { return false; }
    public String overflowText()        { 
        if (!empire().divertColonyExcessToResearch())
            return text(reserveText);
        else if (empire().tech().researchCompleted())
            return text(reserveText);
        else
            return text(techText);
    }
    public void init(Colony c)        { colony = c; }
    public Colony colony()            { return colony; }
    public Planet planet()            { return colony().planet(); }
    public Empire empire()            { return colony().empire(); }
    public TechTree tech()            { return empire().tech(); }
    public String name()              { return ""; }
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
    public int smoothAllocationNeeded(boolean prioritized)	{ return 0; }
    public int smartAllocationNeeded(MouseEvent e)			{ return 0; }
    public int refreshAllocationNeeded(boolean prioritized, boolean hadShipSpending, float targetPopPct) {
    	return smoothAllocationNeeded(prioritized);
    }
    public int govAllocationNeeded(boolean prioritized, GovWorksheet gws) {
    	return refreshAllocationNeeded(prioritized, gws.keepDirectShipAlloc, gws.targetPopPercent);
    }
}
