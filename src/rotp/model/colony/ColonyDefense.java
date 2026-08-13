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
import java.util.Map;

import javax.swing.SwingUtilities;

import rotp.model.empires.Empire;

public final class ColonyDefense extends ColonySpendingCategory {
    private static final long serialVersionUID = 1L;
    public static final int MAX_BASES = 9999; // BR: != Default Max bases
    private MissileBase missileBase;	// Missiles characteristics
    private float bases = 0;			// number of bases and partially completed bases
    private float previousBases = 0;
    private int maxBases = 1;
    private float shield = 0;
    private float newBases = 0;
    private float newShield = 0;
    private float baseUpgradeBC = 0;
    private float unallocatedBC = 0;
    private float newBaseUpgradeCost = 0;
    private boolean shieldCompleted = false;
    private boolean missileBasesUpgraded = false;

	public MissileBase missileBase()		{ return missileBase; }	// Missiles bases characteristics
	public int activeBases()				{ return (int) bases; }	// Number of working bases
	public float rawBases()					{ return bases; }	// Raw number of bases
	public void bases(float d)				{ bases = d; }
	public void destroyBases(int i)			{ bases(rawBases() - i); }
	public int maxBases()					{ return maxBases; }
	public void maxBases(int i)				{ maxBases = i; }
	public float missingBases()				{ return maxBases - bases; }
	public int deltaBases()					{ return (int) bases - (int) previousBases; }
	float shield()							{ return shield; }

    public boolean shieldCompleted()       { return shieldCompleted && shieldAtMaxLevel(); }
    public boolean missileBasesUpgraded()  { return missileBasesUpgraded && (missileBase == tech().bestMissileBase()); }
	void updateMissileBase()				{ missileBase = colony().tech().bestMissileBase(); }
    @Override public boolean isCompleted() {
		final boolean missilesDone = (maxBases() == 0)
				|| ((missileBase == colony().tech().bestMissileBase()) &&  missileBasesCompleted());
        return missilesDone && shieldAtMaxLevel();
    }
	public float completedPct(boolean shieldWithoutBases)	{
		float totcalCost;
		float currentValue;
		// Shield
		if (maxBases() == 0 && !shieldWithoutBases) {
			totcalCost = 0;
			currentValue = 0;
		}
		else {
			totcalCost = maxShieldLevel() * 100;
			currentValue = shield * 100;
		}

		// missile upgrade
		totcalCost += missileUpgradeCost();
		currentValue += baseUpgradeBC;

		// New Missile
		float newMissileBaseCost = tech().newMissileBaseCost();
		totcalCost += maxBases() * newMissileBaseCost;
		currentValue += rawBases() * newMissileBaseCost;

		if (totcalCost == 0)
			return 1;
		return bounds(0, currentValue/totcalCost, 1);
	}
	public boolean isCompleted(int maxMissingBase, boolean shieldWithoutBases)	{
		if (maxBases() == 0 && !shieldWithoutBases)
			return true;
		if (!shieldAtMaxLevel())
			return false;
		return missingBases() <= maxMissingBase;
	}
	@Override public boolean isCompleted(int maxMissingBase) { return isCompleted(maxMissingBase, govOptions().getShieldWithoutBases()); }
    public boolean shieldAtMaxLevel()      {
        return colony().starSystem().inNebula() || (shield >= maxShieldLevel());
    }
	boolean missileBasesCompleted()	{
        return (rawBases() >= maxBases())
            && (missileBase == empire().tech().bestMissileBase());
    }
    public boolean missileBasesCompletedThisTurn() {
        return (deltaBases() > 0) && missileBasesCompleted();
    }
    @Override
    public int categoryType()          { return Colony.DEFENSE; }
    @Override
    public float totalBC()             { return super.totalBC() * planet().productionAdj(); }
    public void incrMaxBases(int inc, boolean shiftDown, boolean ctrlDown)  {
       	if (shiftDown)
       		inc *= 5;
       	if (ctrlDown)
       		inc *= 20;
       	maxBases += inc;
        if (maxBases > MAX_BASES)
        	maxBases = 0;
        else if (maxBases < 0) 
        	maxBases = MAX_BASES;
    }
    public String armorDesc()        { return tech().topArmorTech().shortName(); }
    public String battleSuitDesc()   { return tech().topBattleSuitTech().name(); }
    public String weaponDesc()       { return tech().topHandWeaponTech().name(); }
    public String personalShieldDesc() { return tech().topPersonalShieldTech().name(); }
    public int troops()              { return (int) Math.ceil(colony().population()); }

    private float orderedBasesValue() {
        // if no bases needed for this colony, ignore the order for minimum base spending
        return maxBases() == 0 ? 0 : colony().orderAmount(Colony.Orders.BASES);
    }
    private float orderedShieldValue() {
        return shieldAtMaxLevel() ? 0 : colony().orderAmount(Colony.Orders.SHIELD);
    }
    @Override
    public float orderedValue() {
        return max(super.orderedValue(),
                        orderedBasesValue(),
                        orderedShieldValue());
    }
    @Override
    public void removeSpendingOrders()   {
        colony().removeColonyOrder(Colony.Orders.BASES);
        colony().removeColonyOrder(Colony.Orders.SHIELD);
    }
	void capturedBy(Empire newCiv)	{
        if (newCiv == empire())
            return;
        bases(0);
        shield = 0;
        newBases = 0;
        newShield = 0;
        baseUpgradeBC = 0;
        unallocatedBC = 0;
        maxBases(1);
        missileBase = newCiv.tech().bestMissileBase();
        shieldCompleted = false;
        missileBasesUpgraded = false;
    }
    @Override
    public void nextTurn(float totalProd, float totalReserve) {
        previousBases = bases;
        newBases = 0;
        newShield = 0;
        // prod gets planetary bonus, but not reserve
		final float prodBC = pct()* totalProd * planet().productionAdj();
		final float rsvBC = pct() * totalReserve;
        float newBC = prodBC+rsvBC;

		final float baseCost = missileBase.cost(empire());
        shieldCompleted = false;

        // build shield strength (100 BC per level)
        if (!shieldAtMaxLevel()) {
            newShield = min((newBC/100), (maxShieldLevel() - shield));
            newShield = max(0, newShield);
            newBC -= (newShield * 100);
        }

        newBaseUpgradeCost = 0;
		final float newBaseCost = tech().newMissileBaseCost();

        if (bases > 0 && missileBase != tech().bestMissileBase()) {
            newBaseUpgradeCost = (bases * max(0,newBaseCost-baseCost)) - baseUpgradeBC;
            newBaseUpgradeCost = min(newBC, newBaseUpgradeCost);
            newBaseUpgradeCost = max(0, newBaseUpgradeCost);
            newBC -= newBaseUpgradeCost;
        }

        if (bases < maxBases()) {
            newBases = min((newBC / baseCost), maxBases() - bases);
            newBases = max(0, newBases);
            newBC -= (newBases * baseCost);
        }
        else if (bases > maxBases()) {
            newBases = 0;
            float scrappedBases = bases - maxBases();
            bases = maxBases();
            newBC += (scrappedBases * tech().bestMissileBase().cost(empire()));
        }
        // send remaining BC to reserve
        unallocatedBC = newBC;
    }
    @Override
    public void assessTurn() {
        final Colony c = colony();
        if (shieldAtMaxLevel()) {
            float orderAmt = c.orderAmount(Colony.Orders.SHIELD);
            if (orderAmt > 0) {
                c.removeColonyOrder(Colony.Orders.SHIELD);
                if (!missileBasesCompleted()) 
                    c.addColonyOrder(Colony.Orders.BASES, orderAmt*2/5);
            }
        }
        if (missileBasesCompleted()) 
            c.removeColonyOrder(Colony.Orders.BASES);
    }
	void commitTurn()	{
        // upgrade shield
        shield += newShield;
        shieldCompleted = (newShield > 0) && shieldAtMaxLevel();

        // upgrade existing missile bases
        baseUpgradeBC += newBaseUpgradeCost;

        missileBasesUpgraded = false;
        if (baseUpgradeBC >= missileUpgradeCost()) {
            missileBasesUpgraded = baseUpgradeBC > 0;
            baseUpgradeBC = 0;
            missileBase = tech().bestMissileBase();
        }

        // add new missile bases to limit
        if (newBases > 0) {
            bases(rawBases() + newBases);
            baseUpgradeBC = 0;
        }

        // remainder goes into reserve
        if (!empire().divertColonyExcessToResearch())
            empire().addReserve(unallocatedBC);
        unallocatedBC = 0;
    }
    public float maxShieldLevel()      { return colony().starSystem().inNebula() ? 0 : tech().maxPlanetaryShieldLevel(); }
    public float missileBaseMaintenanceCost(Map<MissileBase, Float> knownBaseCosts) { 
        float baseCost = 0;
        if (knownBaseCosts.containsKey(missileBase))
            baseCost = knownBaseCosts.get(missileBase);
        else {
            baseCost = missileBase.cost(player());
            knownBaseCosts.put(missileBase, baseCost);
        }
        return (activeBases() * baseCost * .02f); 
    }
    private float missileUpgradeCost()  { 
        //for some unknown reason sometimes there's a slight discrepancy between cost of what should be the same thing already
        if(tech().bestMissileBase() == missileBase)
            return 0;
        return rawBases() * (tech().newMissileBaseCost() - missileBase.cost(empire())); 
    }
    public boolean isArmed()             { return activeBases() >= 1; }
    public int shieldLevel()             { return (int) (shield / 5) * 5; }
    public int shieldLevelComp()         { return planet().starSystem().inNebula()? -1 : shieldLevel(); }
    @Override  public boolean canLowerMaintenance()	{ return rawBases() > 0; }
    @Override public void lowerMaintenance()		{ bases (Math.max(0, rawBases()-1)); }
    public float firepower(float shield) {
        return activeBases() * missileBase.firepower(shield);
    }
    public int missileShieldLevel() {
        return (colony().starSystem().inNebula() || empire() == null) ? 0 : shieldLevel() + (int) tech().maxDeflectorShieldLevel();
    }
    @Override public float[] excessSpending() {
        if (colony().allocation(categoryType()) == 0)
            return new float[] {0, 0};

		final float rawProdBC = pct() * colony().totalProductionIncome();
        float prodBC = rawProdBC * planet().productionAdj();
		final float rsvBC = pct() * colony().maxReserveIncome();
		float totalBC = prodBC+rsvBC;
		final float researchFactor = (rawProdBC+rsvBC) / totalBC;

        // deduct cost to finish shield
		final float shieldCost = (maxShieldLevel() - shield) * 100;
        if (shieldCost >= totalBC)
            return new float[] {0, 0};

        totalBC -= shieldCost;
        if (maxBases() == 0)
            return new float[] {totalBC, researchFactor * totalBC};

        // deduct cost to upgrade existing bases
        float bestBaseCost = 0;
        if ((rawBases() > 0) && (missileBase != tech().bestMissileBase())) {
            final float baseCost = missileBase.cost(empire());
            bestBaseCost = tech().bestMissileBase().cost(empire());
            if (bestBaseCost > baseCost) {
                float upgradeCost = (rawBases() * (bestBaseCost-baseCost))-baseUpgradeBC;
                if (upgradeCost > totalBC)
                    return new float[] {0, 0};
                totalBC -= upgradeCost;
            }
        }

        // deduct cost to build remaining bases
        if (rawBases() < maxBases()) {
            if (bestBaseCost == 0)
                bestBaseCost = tech().bestMissileBase().cost(empire());
            float buildCost = missingBases() * bestBaseCost;
            if (buildCost > totalBC)
                return new float[] {0, 0};
            totalBC -= buildCost;
        }

		final float reserveBC  = max(0,totalBC);
		final float researchBC = reserveBC * researchFactor;
        return new float[] {reserveBC, researchBC};
    }
    @Override
    public String upcomingResult() {
        if (colony().allocation(categoryType()) == 0)
            return text(noneText);

		final float prodBC = pct()* colony().totalProductionIncome() * planet().productionAdj();
		final float rsvBC = pct() * colony().maxReserveIncome();
        float newBC = max(0, prodBC+rsvBC);
        float shieldCost = 0;

        if (!shieldAtMaxLevel())
            shieldCost = (maxShieldLevel() - shield) * 100;

        if (newBC < shieldCost)
            return text(shieldText);

        newBC -= shieldCost;

        float upgradeCost = 0;
		final float baseCost = missileBase.cost(empire());
		final float newBaseCost = tech().bestMissileBase().cost(empire());

        if (missileBase != tech().bestMissileBase()) {
            newBC += baseUpgradeBC;
            upgradeCost = rawBases() * Math.max(0,newBaseCost-baseCost);
            if (newBC < upgradeCost)
                return text(upgradeBasesText);
        }

        newBC -= upgradeCost;

		final float maxCost = (maxBases() - rawBases()) * newBaseCost;
		final float newBases = rawBases() + (newBC/newBaseCost);
		final int delta = (int) newBases - activeBases();

        if (newBC <= maxCost) {
            if (delta < 1) {
                if (newBC == 0)
                    return text(noneText);
                else {
                    int turns = (int) Math.ceil( (1- (rawBases() % 1)) * newBaseCost/newBC);
                    if (turns > 99)
                        return text(yearsLongText, turns);
                    else
                        return text(yearsText, turns);
                }
            }
            else if (delta == 1)
                return text(yearText, 1);
            else
                return text(perYearText, delta);
        }
        return overflowText();
    }
    public float maxSpendingNeeded() {
		final float buildShieldCost = Math.max(0, (maxShieldLevel() - shield) * 100);
		final float upgradeMissileBasesCost = Math.max(0,  missileUpgradeCost() - baseUpgradeBC);
		final float newMissileBasesCost = Math.max(0, missingBases() * tech().newMissileBaseCost());
        float totalCost = buildShieldCost + upgradeMissileBasesCost + newMissileBasesCost;

        // adjust cost for planetary production
        // assume any amount over current production comes from reserve (no adjustment)
        float totalBC = (colony().totalProductionIncome() * planet().productionAdj()) + colony().maxReserveIncome();
        if (totalCost > totalBC)
            totalCost += colony().totalProductionIncome() * (1 - planet().productionAdj());
        else
            totalCost *= colony().totalIncome() / totalBC;

        return totalCost;
    }
    public int maxAllocationNeeded() { return maxAllocationNeeded(colony().totalIncome()); }
	int maxAllocationNeeded(float totalIncome)		{
		final float needed = maxSpendingNeeded();
        if (needed <= 0)
            return 0;
		final float pctNeeded = min(1, needed / totalIncome);
		return ceil(pctNeeded * MAX_TICKS);
    }
	private int shieldAllocationNeeded()			{ return shieldAllocationNeeded(colony().totalIncome()); }
	int shieldAllocationNeeded(float totalIncome)	{
        final float needed = (maxShieldLevel() - shield) * 100;
        if (needed <= 0)
            return 0;
        float pctNeeded = min(1, needed / totalIncome);
		return ceil(pctNeeded * MAX_TICKS);
    }
    @Override public int smoothAllocationNeeded(boolean prioritized) { return maxAllocationNeeded(); }
    @Override public int smartAllocationNeeded(MouseEvent e) {
    	if (e==null || SwingUtilities.isLeftMouseButton(e)) // Upgrade And go to Target limit
    		return maxAllocationNeeded();
    	if (SwingUtilities.isRightMouseButton(e)) // Max Available
    		return MAX_TICKS;
    	if (SwingUtilities.isMiddleMouseButton(e)) // Shield only
    		return shieldAllocationNeeded();
    	return 0;
    }
}
