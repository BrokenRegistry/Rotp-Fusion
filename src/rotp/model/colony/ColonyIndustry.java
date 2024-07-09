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
import rotp.model.planet.Planet;
import rotp.model.tech.TechRoboticControls;

public class ColonyIndustry extends ColonySpendingCategory {
    private static final long serialVersionUID = 1L;
    protected float factories = 0;
    protected float previousFactories = 0;
    protected int   robotControls     = 2; // unadjusted, currently implemented, may be <> topRobotControl
    protected float industryReserveBC = 0;
    protected float unallocatedBC     = 0;
    protected float newFactories      = 0;

    @Override
    public void init(Colony c) {
        super.init(c);
        factories = 0;
        robotControls = TechRoboticControls.BASE_ROBOT_CONTROLS;
        industryReserveBC = 0;
        unallocatedBC = 0;
        newFactories = 0;
    }
    @Override
    public int categoryType()              { return Colony.INDUSTRY; }
    public float factories()               { return factories; }
    public void resetFactories(float d)    { factories = max(0,d); previousFactories = factories;}
    public void removeFactories(float d)   { factories = max(0,factories-d); }
    // public void previousFactories(float d) { previousFactories = d; } // BR: not public
    public int deltaFactories()            { return (int)factories - (int)previousFactories; }
    protected int robotControls()          { return robotControls; }
    public float newFactoryCost()          { return tech().newFactoryCost(robotControls()); }
    public int effectiveRobotControls()    {
        if(empire().ignoresFactoryRefit())
            return maxRobotControls();
        return robotControls() + empire().robotControlsAdj();
    }
    public int maxRobotControls()          { return tech().topRobotControls() + empire().robotControlsAdj(); }
    @Override
    public float totalBC()                 { return super.totalBC() * planet().productionAdj(); }
    public float maxFactories()            { return planet().maxSize() * maxRobotControls(); }
    //public float maxFactories(int rc)    { return planet().maxSize() * rc; }
    public int maxBuildableFactories()     { return (int) (planet().currentSize() * maxRobotControls()); }
    public int maxBuildableFactories(int rc) { return (int) (planet().currentSize() * (rc+empire().robotControlsAdj())); }
	/**
	 * Best you can do with current RC and current population if there was enough factories.
	 */
    public int maxUseableFactories()       { return maxUseableFactories(robotControls()); }
	/**
	 * Best you can do with given RC and current population if there was enough factories.
	 */
    public int maxUseableFactories(int rc) { return (int) colony().population() * (rc+empire().robotControlsAdj()); }
    @Override
    public boolean isCompleted()          { return factories >= maxBuildableFactories(); }
    public boolean isCompletedThisTurn()  { return isCompleted() && (newFactories > 0); }
    @Override
    public float orderedValue()           { return max(super.orderedValue(), colony().orderAmount(Colony.Orders.FACTORIES)); }
    @Override
    public void removeSpendingOrders()    { colony().removeColonyOrder(Colony.Orders.FACTORIES); }
    public void addToAlienFactories()     {
    	planet().addAlienFactories(empire().id, (int) factories);
    }
    public void capturedBy(Empire newCiv) {
        if (newCiv == empire())
            return;

        Planet p = planet();
        p.addAlienFactories(empire().id, (int) factories);
        industryReserveBC = 0;
        robotControls = newCiv.tech().topRobotControls();
        factories = p.alienFactories(newCiv.id);
        p.removeAlienFactories(newCiv.id);
        unallocatedBC = 0;
        newFactories = 0;
        previousFactories = 0;
    }
    @Override
    public void nextTurn(float totalProd, float totalReserve) {
        if (factories < 0) // correct possible data issue
            factories = 0;

        // correct for any captured errors in existing saves where a
        // captured colony had higher controls
        robotControls = min(robotControls, tech().topRobotControls());

        previousFactories = factories;
        // prod gets planetary bonus, but not reserve
        float prodBC = pct()* totalProd * planet().productionAdj();
        float rsvBC = pct() * totalReserve;
        float newBC = prodBC+rsvBC+industryReserveBC;
        industryReserveBC = 0;
        newFactories = 0;

        while ((newBC > 0) && (robotControls <= tech().topRobotControls())) {
            // how many total factories can we have at current controls?
            float buildableFactories = maxBuildableFactories(robotControls);
            // if we already have that many factories, then upgrade robotic controls if possible
            if (buildableFactories <= (factories+newFactories)) {
                if (robotControls == tech().topRobotControls())
                    break; // no more robotic control upgrades, so quit
                if (newBC > 0) {
                    float upgradeCost = 0;
                    float factoriesToRefit = buildableFactories;
                    if (!empire().ignoresFactoryRefit())
                        upgradeCost = factoriesToRefit * tech().bestFactoryCost() / 2;
                    // not enough to upgrade? save off BC for next turn and exit
                    if (upgradeCost > newBC) {
                        industryReserveBC = newBC;
                        return;
                    }
                    else {
                        // pay to upgrade all factories to new RC at once
                        newBC -= upgradeCost;
                        robotControls++;
                        buildableFactories = maxBuildableFactories(robotControls);
                    }
                }
            }
            // first, try to convert existing alien factories to our max build limit
            if ((newFactories+factories) < buildableFactories) {
                while (convertableAlienFactories() > 0 && (newBC > factoryConversionCost()) && (newFactories + factories < buildableFactories)) {
                    convertRandomAlienFactory();
                    newBC -= factoryConversionCost();
                }
            }
            // second, try to build new factories at current controls
            if ((newFactories+factories) < buildableFactories) {
                float factoriesToBuild = max(0,buildableFactories-factories-newFactories);
                float costPerFactory = newFactoryCost();
                float buildCost = factoriesToBuild * costPerFactory;
                float bcSpent = min(newBC, buildCost);
                newFactories += (bcSpent/costPerFactory);
                newBC -= bcSpent;
            }
        }

        // send remaining BC to empire reserve
        unallocatedBC = newBC;
    }
    @Override
    public void assessTurn() {
        Colony c = colony();
        float orderAmt = 0;
        if (isCompletedThisTurn()) {
            orderAmt = max(orderAmt, c.orderAmount(Colony.Orders.FACTORIES));
            c.removeColonyOrder(Colony.Orders.FACTORIES);
        }

        c.addFollowUpSpendingOrder(orderAmt);
    }
    public void commitTurn() {
        factories += newFactories;
        if (!empire().divertColonyExcessToResearch())
           empire().addReserve(unallocatedBC);
        unallocatedBC = 0;
    }
    @Override
    public float excessSpending()    {
        if (colony().allocation(categoryType()) == 0)
            return 0;

        float prodBC = pct()* colony().totalProductionIncome() * planet().productionAdj();
        float rsvBC = pct() * colony().maxReserveIncome();
        float totalBC = prodBC+rsvBC+industryReserveBC;

        // deduct cost to convert alien factories
        float convertCost = totalAlienConversionCost();
        if (totalBC <= convertCost)
            return 0;

        totalBC -= convertCost;

        // deduct cost to build remaining factories at current robot controls level
        float maxBuildable = maxBuildableFactories(robotControls);
        float possibleNewFactories = 0;
        if (maxBuildable > factories) {
            possibleNewFactories = maxBuildable-factories;
            float buildCost = possibleNewFactories*newFactoryCost();
            if (totalBC <= buildCost)
                return 0;
            totalBC -= buildCost;
        }

        int colonyControls = robotControls;
        while ((totalBC > 0) && (colonyControls < tech().topRobotControls())) {
            // calculate cost to refit existing factories
            float upgradeCost = 0;
            float factoriesToUpgrade = min(factories+possibleNewFactories, maxBuildableFactories(colonyControls));
            if (!empire().ignoresFactoryRefit())
                upgradeCost = factoriesToUpgrade * tech().bestFactoryCost() / 2;
            // not enough to upgrade? save off BC for next turn and exit
            if (upgradeCost > totalBC)
                return 0;
            // pay to upgrade all factories to new RC at once
            totalBC -= upgradeCost;
            colonyControls++;
            //after refitting, build up to max useable factories at current robot controls level
            float factoriesToBuild = max(0, maxBuildableFactories(colonyControls)-factories-possibleNewFactories);
            if (factoriesToBuild > 0) {
                float costPerFactory = tech().newFactoryCost(colonyControls);
                float buildCost = factoriesToBuild * costPerFactory;
                if (buildCost > totalBC)
                    return 0;
                possibleNewFactories += factoriesToBuild;
                totalBC -= buildCost;
            }
        }
        return max(0,totalBC);
    }
    @Override
    public String upcomingResult()   {
        if (colony().allocation(categoryType()) == 0)
            return text(noneText);

        float prodBC = pct()* colony().totalProductionIncome() * planet().productionAdj();
        float rsvBC = pct() * colony().maxReserveIncome();
        float startBC = prodBC+rsvBC+industryReserveBC;
        float newBC = prodBC+rsvBC+industryReserveBC;
        int colonyControls = min(robotControls, tech().topRobotControls());
        float builtFactories = factories;

        if (newBC <= 0)
            return text(noneText);

        // cost to build up to max useable factories

        float possibleNewFactories = 0;

        int previouslyConvertedFactories = 0;

        while ((newBC > 0) && (colonyControls <= tech().topRobotControls())) {
            // how many total factories can we have at current controls?
            float buildableFactories = maxBuildableFactories(colonyControls);

            // if we already have that many factories, then upgrade robotic controls if possible
            if (buildableFactories <= builtFactories) {
                if (colonyControls == tech().topRobotControls())
                    break; // no more robotic control upgrades, so quit
                if (newBC > 0) {
                    float upgradeCost = 0;
                    float factoriesToRefit = buildableFactories;
                    if (!empire().ignoresFactoryRefit())
                        upgradeCost = factoriesToRefit * tech().bestFactoryCost() / 2;
                    // not enough to upgrade? save off BC for next turn and exit
                    if (upgradeCost > newBC)
                        return text(refitFactoriesText);
                    else {
                        // pay to upgrade all factories to new RC at once
                        newBC -= upgradeCost;
                        colonyControls++;
                        buildableFactories = maxBuildableFactories(colonyControls);
                    }
                }
            }
            // first, try to convert existing alien factories to our max build limit
            if (builtFactories < buildableFactories) {
                int convertableFactories = convertableAlienFactories(colonyControls)-previouslyConvertedFactories;
                if (convertableFactories > 0) {
                    float totalConvertCost = convertableFactories * factoryConversionCost();
                    float convertCost = min(newBC, totalConvertCost);
                    float delta = convertCost/factoryConversionCost();
                    newBC -= convertCost;
                    possibleNewFactories += delta;
                    builtFactories += delta;
                    previouslyConvertedFactories += delta;
                }
            }
            // second, try to build new factories at current controls
            if (builtFactories < buildableFactories) {
                float costPerFactory = tech().newFactoryCost(colonyControls);
                float factoriesToBuild = buildableFactories-builtFactories;
                float totalBuildCost = factoriesToBuild * costPerFactory;
                float buildCost = min(newBC, totalBuildCost);
                float delta = buildCost/costPerFactory;
                possibleNewFactories += delta;
                builtFactories += delta;
                newBC -= buildCost;
            }
        }

        if (newBC > 0)
            return overflowText();
        else
            return buildFactoriesText(possibleNewFactories, startBC);
    }
    public float maxSpendingNeeded() {
        float builtFactories = factories;
        int colonyControls = robotControls;

        float totalCost = 0;
        int previouslyConvertedFactories = 0;

        while (colonyControls <= tech().topRobotControls()) {
            // how many total factories can we have at current controls?
            float buildableFactories = maxBuildableFactories(colonyControls);

            // if we already have that many factories, then upgrade robotic controls if possible
            if (buildableFactories <= builtFactories) {
                if (colonyControls == tech().topRobotControls())
                    break; // no more robotic control upgrades, so quit
                if (!empire().ignoresFactoryRefit()) {
                    float refitCost = buildableFactories * tech().bestFactoryCost() / 2;
                    totalCost += refitCost;
                }
                colonyControls++;
            }
            // first, try to convert existing alien factories to our max build limit
            if (builtFactories < buildableFactories) {
                int convertableFactories = convertableAlienFactories(colonyControls)-previouslyConvertedFactories;
                if (convertableFactories > 0) {
                    float convertCost = convertableFactories * factoryConversionCost();
                    float delta = convertCost/factoryConversionCost();
                    totalCost += convertCost;
                    builtFactories += delta;
                    previouslyConvertedFactories += delta;
                }
            }
            // second, try to build new factories at current controls
            if (builtFactories < buildableFactories) {
                float costPerFactory = tech().newFactoryCost(colonyControls);
                float factoriesToBuild = buildableFactories-builtFactories;
                float buildCost = factoriesToBuild * costPerFactory;
                float delta = buildCost/costPerFactory;
                totalCost += buildCost;
                builtFactories += delta;
            }
        }

        totalCost = max(0, totalCost-industryReserveBC);

        // adjust cost for planetary production
        // assume any amount over current production comes from reserve (no adjustment)
        float totalBC = (colony().totalProductionIncome() * planet().productionAdj()) + colony().maxReserveIncome();
        if (totalCost > totalBC)
            totalCost += colony().totalProductionIncome() * (1 - planet().productionAdj());
        else
            totalCost *= colony().totalIncome() / totalBC;

        return totalCost;
    }
    public int maxAllocationNeeded() {
        float needed = maxSpendingNeeded();
        if (needed <= 0)
            return 0;
        float pctNeeded = min(1, needed / colony().totalIncome());
        int ticks = (int) Math.ceil(pctNeeded * MAX_TICKS);
        return ticks;
    }
    public int minAllocationNeeded() {
        float needed = spendingNeeded();
        if (needed <= 0)
            return 0;
        float pctNeeded = min(1, needed / colony().totalIncome());
        int ticks = (int) Math.ceil(pctNeeded * MAX_TICKS);
        return ticks;
    }
    public Float[] factoryBalance()	 {
    	// Population expectation
    	float expectedPopulation = expectedPopulation();
    	float expectedMissingPopulation = planet().currentSize() - expectedPopulation;
    	// Factories
    	float maxFactories	     = maxBuildableFactories();
    	float upcomingFactories  = upcomingFactories();
    	float maxNeededFactories = maxFactories - factories - upcomingFactories;
    	float neededFactories    = maxNeededFactories - expectedMissingPopulation * robotControls;
    	Float factoryBalance     = -neededFactories;

    	if (robotControls != tech().topRobotControls()
    			|| convertableAlienFactories() != 0) { // check for refit
        	factoryBalance = null;
    	}
    	return new Float[] {factoryBalance, upcomingFactories, neededFactories, factories, maxFactories};
    }
    //
    // NEW REPATRIATED METHODS
    //
    public float maxNewFactories(float bc) { return min(maxUseableFactories()-factories(), bc/newFactoryCost()); }
    //
    // PROTECTED METHODS
    //
    protected String buildFactoriesText(float delta, float newBC) {
        float deltaRounded = delta >= 10 ? (int) delta : (float)Math.floor(delta*10)/10;
        if (deltaRounded == (int) deltaRounded)
            return text(perYearText, (int)deltaRounded);
        else
            return text(perYearText, fmt(deltaRounded,1));
    }
    protected float expectedPopulation() {
    	float curentPopulation	 = colony().population();
       	float upcomingPopGrowth  = colony().ecology().upcomingPopGrowth(); // Next Turn
    	float expectedPopulation = curentPopulation + upcomingPopGrowth;
    	expectedPopulation		 = min(expectedPopulation, colony().maxSize());
    	return expectedPopulation;
    }
    protected float factoryConversionCost()    { return 2; }
    protected float totalAlienConversionCost() {
        return convertableAlienFactories() * factoryConversionCost();
    }
    protected int convertableAlienFactories()  {
        return convertableAlienFactories(robotControls);
    }
    protected int convertableAlienFactories(int rc) {
        float alienFactories = planet().numAlienFactories();
        return (int) max(0, min(alienFactories,maxBuildableFactories(rc)-factories));
    }
    protected void convertRandomAlienFactory() {
        float num = convertableAlienFactories();
        Planet p = planet();
        if (num == 0)
            return;

        // select random race from alienFactories and convert 1 factory
        int randomEmpId = p.randomAlienFactoryEmpire();
        p.addAlienFactories(randomEmpId, -1);
        newFactories++;
    }
    //
    // PRIVATE METHODS
    //
    private float spendingNeeded() {
        float builtFactories = factories;
        int colonyControls = robotControls;
        float expectedMissingPopulation = planet().currentSize() - expectedPopulation();
        float notTobuild = expectedMissingPopulation * maxRobotControls();
        builtFactories += notTobuild;

        float totalCost = 0;
        int previouslyConvertedFactories = 0;

        // Cost of all
        while (colonyControls <= tech().topRobotControls()) {
            // how many total factories can we have at current controls?
            float buildableFactories = maxBuildableFactories(colonyControls);

            if (colonyControls == tech().topRobotControls()) {
            	// no more robotic control upgrades Take account of given limit.
            	buildableFactories -= notTobuild;
            	if (buildableFactories <= builtFactories)
            		break;
            }
            else { // Still in refit mode... build and refit up to max.
            	if (buildableFactories <= builtFactories) {
            		// We already have that many factories, then upgrade robotic controls
            		if (!empire().ignoresFactoryRefit()) {
                        float refitCost = buildableFactories * tech().bestFactoryCost() / 2;
                        totalCost += refitCost;
            		}
            		colonyControls++;
            	}
            }

            // first, try to convert existing alien factories to our max build limit
            if (builtFactories < buildableFactories) {
                int convertableFactories = convertableAlienFactories(colonyControls)-previouslyConvertedFactories;
                if (convertableFactories > 0) {
                    float convertCost = convertableFactories * factoryConversionCost();
                    float delta = convertCost/factoryConversionCost();
                    totalCost += convertCost;
                    builtFactories += delta;
                    previouslyConvertedFactories += delta;
                }
            }
            // second, try to build new factories at current controls
            if (builtFactories < buildableFactories) {
                float costPerFactory = tech().newFactoryCost(colonyControls);
                float factoriesToBuild = buildableFactories-builtFactories;
                float buildCost = factoriesToBuild * costPerFactory;
                float delta = buildCost/costPerFactory;
                totalCost += buildCost;
                builtFactories += delta;
            }
        }
        totalCost = max(0, totalCost-industryReserveBC);

        // adjust cost for planetary production
        // assume any amount over current production comes from reserve (no adjustment)
        float totalBC = (colony().totalProductionIncome() * planet().productionAdj()) + colony().maxReserveIncome();
        if (totalCost > totalBC)
            totalCost += colony().totalProductionIncome() * (1 - planet().productionAdj());
        else
            totalCost *= colony().totalIncome() / totalBC;

        return totalCost;
    }
    public int minAllocationNeeded() {
        float needed = spendingNeeded();
        if (needed <= 0)
            return 0;
        float pctNeeded = min(1, needed / colony().totalIncome());
        int ticks = (int) Math.ceil(pctNeeded * MAX_TICKS);
        return ticks;
    }
    private float expectedPopulation() {
    	float curentPopulation	 = colony().population();
       	float upcomingPopGrowth  = colony().ecology().upcomingPopGrowth(); // Next Turn
    	float expectedPopulation = curentPopulation + upcomingPopGrowth;
    	expectedPopulation		 = min(expectedPopulation, colony().maxSize());
    	return expectedPopulation;
    }
    public Float[] factoryBalance()	 {
    	// Population expectation
    	float expectedPopulation = expectedPopulation();
    	float expectedMissingPopulation = planet().currentSize() - expectedPopulation;
    	// Factories
    	float maxFactories	     = maxBuildableFactories();
    	float upcomingFactories  = upcomingFactories();
    	float maxNeededFactories = maxFactories - factories - upcomingFactories;
	float neededFactories    = maxNeededFactories - expectedMissingPopulation * maxRobotControls();
	float factoryBalance     = -neededFactories;
	Float refitFlag = 0f;

    	if (robotControls != tech().topRobotControls()
    			|| convertableAlienFactories() != 0) { // check for refit
		refitFlag = null;
    	}
	return new Float[] {factoryBalance, refitFlag};
    }
    //
    // PRIVATE METHODS
    //
    private float upcomingFactories() {
        if (colony().allocation(categoryType()) == 0)
            return 0;

        float prodBC = pct()* colony().totalProductionIncome() * planet().productionAdj();
        float rsvBC = pct() * colony().maxReserveIncome();
        float newBC = prodBC+rsvBC+industryReserveBC;
        int colonyControls = min(robotControls, tech().topRobotControls());
        float builtFactories = factories;

        if (newBC <= 0)
            return 0;

        // cost to build up to max usable factories
        float possibleNewFactories = 0;

        int previouslyConvertedFactories = 0;

        while ((newBC > 0) && (colonyControls <= tech().topRobotControls())) {
            // how many total factories can we have at current controls?
            float buildableFactories = maxBuildableFactories(colonyControls);

            // if we already have that many factories, then upgrade robotic controls if possible
            if (buildableFactories <= builtFactories) {
                if (colonyControls == tech().topRobotControls())
                    break; // no more robotic control upgrades, so quit
                if (newBC > 0) {
                    float upgradeCost = 0;
                    float factoriesToRefit = buildableFactories;
                    if (!empire().ignoresFactoryRefit())
                        upgradeCost = factoriesToRefit * tech().bestFactoryCost() / 2;
                    // not enough to upgrade? save off BC for next turn and exit
                    if (upgradeCost > newBC)
                        return possibleNewFactories;
                    else {
                        // pay to upgrade all factories to new RC at once
                        newBC -= upgradeCost;
                        colonyControls++;
                        buildableFactories = maxBuildableFactories(colonyControls);
                    }
                }
            }
            // first, try to convert existing alien factories to our max build limit
            if (builtFactories < buildableFactories) {
                int convertableFactories = convertableAlienFactories(colonyControls)-previouslyConvertedFactories;
                if (convertableFactories > 0) {
                    float totalConvertCost = convertableFactories * factoryConversionCost();
                    float convertCost = min(newBC, totalConvertCost);
                    float delta = convertCost/factoryConversionCost();
                    newBC -= convertCost;
                    possibleNewFactories += delta;
                    builtFactories += delta;
                    previouslyConvertedFactories += delta;
                }
            }
            // second, try to build new factories at current controls
            if (builtFactories < buildableFactories) {
                float costPerFactory = tech().newFactoryCost(colonyControls);
                float factoriesToBuild = buildableFactories-builtFactories;
                float totalBuildCost = factoriesToBuild * costPerFactory;
                float buildCost = min(newBC, totalBuildCost);
                float delta = buildCost/costPerFactory;
                possibleNewFactories += delta;
                builtFactories += delta;
                newBC -= buildCost;
            }
        }
        return possibleNewFactories;
    }
    @Override public int smartAllocationNeeded(MouseEvent e) { //TODO BR: smartAllocationNeeded
    	if (e==null || SwingUtilities.isLeftMouseButton(e))
    		return maxAllocationNeeded();
    	if (SwingUtilities.isRightMouseButton(e))
    		return MAX_TICKS;
    	if (SwingUtilities.isMiddleMouseButton(e))
    		return minAllocationNeeded();
    	return 0;
    }
}
