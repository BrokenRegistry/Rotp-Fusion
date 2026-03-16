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
import rotp.model.tech.TechAtmosphereEnrichment;
import rotp.model.tech.TechTree;

public class ColonyEcology extends ColonySpendingCategory {
    private static final long serialVersionUID = 1L;
    private static final int SOIL_UPGRADE_BC = 150;
    private float hostileBC = 0;
    private float soilEnrichBC = 0;
    private float wasteCleaned = 0;
    private float unallocatedBC = 0;

    private float newGrownPopulation = 0;
    private float newPurchasedPopulation = 0;
    private float newBiosphereIncrease = 0;

    private boolean atmosphereCompleted = false;
    private boolean soilEnrichCompleted = false;
    private boolean terraformCompleted = false;
    private boolean populationGrowthCompleted = false;
    private int expectedPopGrowth = 0;
    private float floatPopGrowth = 0;

    public boolean atmosphereCompletedThisTurn()        { return atmosphereCompleted; }
    public boolean soilEnrichCompletedThisTurn()        { return soilEnrichCompleted; }
    public boolean terraformCompletedThisTurn()         { return terraformCompleted; }
    public boolean populationGrowthCompletedThisTurn()  { return populationGrowthCompleted && populationGrowthCompleted(); }
    public boolean populationGrowthCompleted()          { return colony().population() >= colony().maxSize(); }
    public boolean terraformCompleted()                 { return planet().currentSize() >= colony().maxSize(); }

    public void resetBiosphere() {
        hostileBC = 0;
        soilEnrichBC = 0;
    }
    public void init() {
        hostileBC = 0;
        soilEnrichBC = 0;
        planet().resetWaste();
        unallocatedBC = 0;

        wasteCleaned = 0;
        newGrownPopulation = 0;
        newPurchasedPopulation = 0;
        newBiosphereIncrease = 0;
    }
    public boolean isTerraformed() {
    	switch (options().selectedAutoTerraformEnding()) {
    	case "Cleaned":
    		return empire().ignoresPlanetEnvironment() || waste() == 0;
    	case "Terraformed":
    		return colony().planet().currentSize() >= colony().planet().maxSize()
        		&& (empire().ignoresPlanetEnvironment() || waste() == 0);
    	case "Populated":
    	default:
    		return colony().population() >= colony().planet().maxSize()
            		&& (empire().ignoresPlanetEnvironment() || waste() == 0);
    	}
    }
    void checkPlanetImprovement(GovWorksheet gws)	{
    	planet().potentialImprovement(tech(), gws);
    	if (gws.canTerraformAtmosphere)
            gws.atmosphereCost = max(0, atmosphereTerraformCost() - hostileBC);

    	if (gws.canEnrichSoil) {
    		//gws.enrichSoilCost = enrichSoilCost() - soilEnrichBC;
    		gws.nextEnrichSoilCost = SOIL_UPGRADE_BC - soilEnrichBC;
    	}

    	if (gws.canTerraform)
    		gws.terraformCost = tech().popIncreaseCost() * gws.terraformIncrease;
    }
    @Override
    public int categoryType()    { return Colony.ECOLOGY; }
    @Override
    public boolean isCompleted() {
		return colony().population() >= colony().planet().maxSize()
        		&& (empire().ignoresPlanetEnvironment() || waste() == 0);
    }
	@Override public boolean isCompleted(int maxMissingPop) {
    	boolean noWaste = empire().ignoresPlanetEnvironment() || waste() == 0;
    	Colony col = colony();
    	if (noWaste) {
    		float missingPop = col.planet().maxSize() - col.population() + col.inTransport();
    		return missingPop <= maxMissingPop;
    	}
    	return false;
    }
    @Override
    public float orderedValue() {
        return max(super.orderedValue(),
                    colony().orderAmount(Colony.Orders.SOIL),
                    colony().orderAmount(Colony.Orders.ATMOSPHERE),
                    colony().orderAmount(Colony.Orders.TERRAFORM));
    }
    @Override
    public void removeSpendingOrders()   {
        colony().removeColonyOrder(Colony.Orders.SOIL);
        colony().removeColonyOrder(Colony.Orders.ATMOSPHERE);
        colony().removeColonyOrder(Colony.Orders.TERRAFORM);
    }
    public void capturedBy(Empire newCiv) {
        if (newCiv == empire())
            return;
        hostileBC = 0;
        soilEnrichBC = 0;
        unallocatedBC = 0;
        wasteCleaned = 0;
        newGrownPopulation = 0;
        newPurchasedPopulation = 0;
        newBiosphereIncrease = 0;
        atmosphereCompleted = false;
        soilEnrichCompleted = false;
        terraformCompleted = false;
        populationGrowthCompleted = false;
        expectedPopGrowth = 0;
    }
    public float waste()           { return planet().waste(); }
    public void addWaste(float w)  { planet().addWaste(w); }
    public float atmosphereTerraformCost() {
        return TechAtmosphereEnrichment.hostileTech.cost;
    }
    public float enrichSoilCost() {
        if (!tech().enrichSoil())
            return 0;

        int envDiff = tech().topSoilEnrichmentTech().environment - planet().environment();
        return Math.max(0, envDiff * SOIL_UPGRADE_BC);
    }
    public float terraformCost() {
        float roomToGrow = Math.max(0, colony().maxSize() - planet().currentSize());
        if (roomToGrow <= 0)
            return 0;

        return roomToGrow * tech().popIncreaseCost();
    }
    public float wasteWillClean(float availableBC, float wasteToClean) {
        Empire emp = colony().empire();
        if (emp.ignoresPlanetEnvironment())
            return 0;
        else
            return max(0, min((availableBC * emp.tech().wasteElimination()), wasteToClean));
    }
    public float wasteWillClean(float availableBC) {
        return wasteWillClean(availableBC, waste());
    }
    @Override
    public void nextTurn(float totalProd, float totalReserve) {
        Colony c = colony();
        Planet p = c.planet();
        Empire emp = c.empire();
        TechTree tr = emp.tech();
        newGrownPopulation = c.normalPopGrowth();
        wasteCleaned = 0;

        float prodBC = pct()* totalProd;
        float rsvBC = pct() * totalReserve;
        float newBC = totalAvailableBCthisCategory(totalProd, totalReserve);

        // add new waste created from this turn & clean it up
        addWaste(c.newWaste());
        wasteCleaned = wasteWillClean(newBC);
        newBC -= (wasteCleaned / tr.wasteElimination());

        // try to convert hostile atmosphere
        atmosphereCompleted = false;
        if (p.canTerraformAtmosphere(emp))  { 
            float hostileCost = min((atmosphereTerraformCost() - hostileBC), newBC);
            hostileCost = max(hostileCost,0);
            hostileBC += hostileCost;
            newBC -= hostileCost;
            atmosphereCompleted = hostileBC >= atmosphereTerraformCost();
            if (atmosphereCompleted) {
                hostileBC = 0;
                p.terraformAtmosphere();
            }
        }

        //if not Hostile & civ has SoilEnrichment that will improvement this environment,
        // then try to pay for soil enrichment...
        soilEnrichCompleted = false;
        if (!p.isEnvironmentHostile() && tr.enrichSoil() && (tr.topSoilEnrichmentTech().environment > p.environment()))  {
            float enrichCost = min(newBC,enrichSoilCost() - soilEnrichBC);
            enrichCost = max(enrichCost,0);
            soilEnrichBC += enrichCost;
            newBC -= enrichCost;
            while (soilEnrichBC >= SOIL_UPGRADE_BC) {
                soilEnrichBC -= SOIL_UPGRADE_BC;
                p.enrichSoil();
            }
            soilEnrichCompleted = p.environment() >= tr.topSoilEnrichmentTech().environment;
        }

        // try to terraform planet to maxSize
        float terraformCost = terraformCost();
        newBiosphereIncrease = 0;

        terraformCompleted = false;
        if ((newBC > 0) && (terraformCost > 0)) {
            terraformCost = min(newBC, terraformCost);
            newBiosphereIncrease = terraformCost / tr.topTerraformingTech().costPerMillion;
            newBC -= terraformCost;
            p.terraformBiosphere(newBiosphereIncrease);
            terraformCompleted = (newBiosphereIncrease > 0) && (p.currentSize() >= c.maxSize());
        }

        // try to buy new population
        populationGrowthCompleted = false;
        newPurchasedPopulation = 0;
        if (newBC > 0) {
            newPurchasedPopulation = newBC / tr.populationCost();
            newPurchasedPopulation = min(newPurchasedPopulation,(p.currentSize() - c.population() + c.inTransport() - newGrownPopulation));
            newPurchasedPopulation = max(newPurchasedPopulation,0);
            newBC -= (newPurchasedPopulation* tr.populationCost());
        }

        // for poor planets, we want to assume that as much
        // of the remaining BC left (ecoProd) is from reserve
        // this minimizes loss when sending back to the reserve
        float planetAdj = p.productionAdj();
        if (planetAdj < 1) {
            float unadjustedBC = min(rsvBC, newBC);
            float adjustedBC = (newBC - unadjustedBC) * planetAdj;
            unallocatedBC += (unadjustedBC + adjustedBC);
        }
        // for normal/rich planets, we want to assume that as much
        // of the remaining BC left (ecoProd) is from planetary
        // production.. to maximum BC send to the reserve
        else {
            float unadjustedBC = max(0, newBC-prodBC);
            float adjustedBC = (newBC - unadjustedBC) * planetAdj;
            unallocatedBC += (unadjustedBC + adjustedBC);
        }
    }
    @Override
    public void assessTurn() {
        Colony c = colony();
        float orderAmt = 0;
        if (atmosphereCompletedThisTurn()) {
            orderAmt = max(orderAmt, c.orderAmount(Colony.Orders.ATMOSPHERE));
            c.removeColonyOrder(Colony.Orders.ATMOSPHERE);
        }
        if (soilEnrichCompletedThisTurn()) {
            orderAmt = max(orderAmt, c.orderAmount(Colony.Orders.SOIL));
            c.removeColonyOrder(Colony.Orders.SOIL);
        }
        if (terraformCompletedThisTurn()) {
            orderAmt = max(orderAmt, c.orderAmount(Colony.Orders.TERRAFORM));
            c.removeColonyOrder(Colony.Orders.TERRAFORM);
        }
        if (populationGrowthCompletedThisTurn()) {
            orderAmt = max(orderAmt, c.orderAmount(Colony.Orders.POPULATION));
            c.removeColonyOrder(Colony.Orders.POPULATION);
        }
        
        c.addFollowUpSpendingOrder(orderAmt);
    }
    public void commitTurn() {
        Colony c = colony();
        addWaste(-wasteCleaned);
        c.setPopulation(c.population() + newGrownPopulation + newPurchasedPopulation);
        populationGrowthCompleted = ((newGrownPopulation+newPurchasedPopulation) > 0) && (c.population() >= c.maxSize());

        // if affected by waste, deduct population due to decreased planet size
        if (!empire().ignoresPlanetEnvironment()) {
            float pop = colony().population();
            float size = planet().sizeAfterWaste();
            if (pop > size) {
                float over = pop - size;
                float loss= over/pop * .1f * over;
                loss=min(loss,over);
                c.setPopulation(c.population()-loss);
            }
        }
        if (colony().population() < 0)
        {
            err("ERROR: bad pop for ", colony().name(), " pop:"+colony().population(), " newGrown:", str(newGrownPopulation), " newPurchased:", str(newPurchasedPopulation));
        }

        if (!empire().divertColonyExcessToResearch())
            empire().addReserve(unallocatedBC);
        unallocatedBC = 0;
    }
    public int upcomingPopGrowth() {
        upcomingResult();
        return expectedPopGrowth;
    }
    public float upcomingPopGrowthFloat() {
        upcomingResult();
        return floatPopGrowth;
    }
    @Override
    public boolean warning() {
        if (empire().ignoresPlanetEnvironment())
            return false;
        
        float newBC = totalAvailableBCthisCategory(colony().totalProductionIncome(), colony().maxReserveIncome());
        newBC = max(0, newBC);
        return (newBC < colony().wasteCleanupCost());
    }
    @Override
    public String upcomingResult(){
        Colony c = colony();

        float newBC = totalAvailableBCthisCategory(c.totalProductionIncome(), c.maxReserveIncome());
        newBC = max(0, newBC);
        float cost;

        // new population
        float currentPop = c.population();
        // Currently, this assumes that all incoming transports will not be shot down.
        float workingPop = c.populationAfterNextTurnTransports();
        float expGrowth  = c.normalPopGrowth();
        float expPop = min(workingPop+expGrowth, planet().currentSize());
        floatPopGrowth = expPop - currentPop;  // BR: To allow fine tuning
        expectedPopGrowth = (int) (expPop) - (int) currentPop; // BR: ?!

        // check for waste cleanup
        cost = c.wasteCleanupCost();
        if (newBC < cost) 
            return text(wasteText);

        if (c.allocation(categoryType()) == 0)
            return text(noneText);
		// BR: Moved at the end to allow the return of the real pop growth
		//if (allocation() == cleanupAllocationNeeded())
		if (newBC == cost)
			return text(cleanupText);

        newBC -= cost;
        // check for atmospheric terraforming
        Empire emp = c.empire();
        Planet p = c.planet();
        boolean canTerraformAtmosphere = p.canTerraformAtmosphere(emp);
        if (canTerraformAtmosphere) {
            cost = atmosphereTerraformCost() - hostileBC;
            if (newBC < cost)
                return text(atmosphereText);
            newBC -= cost;
        }

        // check for soil enrichment
        TechTree tr = emp.tech();
        if ((! p.isEnvironmentHostile()) || canTerraformAtmosphere) {
            if (tr.enrichSoil()) {
                int envUpgrade = tr.topSoilEnrichmentTech().environment - p.environment();
                if (envUpgrade > 0) {
                    cost = ((tr.topSoilEnrichmentTech().environment - p.environment()) * SOIL_UPGRADE_BC) - soilEnrichBC;
                    if (newBC < cost)
                        return text(enrichSoilText);
                    newBC -= cost;
                }
            }
        }

        // check for terraforming
        float maxPopSize = c.maxSize();
        float roomToGrow = maxPopSize - p.currentSize();
        if (roomToGrow > 0) {
            cost = roomToGrow * tr.topTerraformingTech().costPerMillion;
            if (newBC < cost) 
                 return text(terraformText);
            newBC -= cost;
        }

        // check for purchasing new pop
        float newPopPurchaseable = getNewPopPurchasableShortTerm();
        if (newPopPurchaseable > 0) {
            float newPopCost = tr.populationCost();
            cost = newPopPurchaseable * newPopCost;
            float pop = workingPop + expGrowth + min(newPopPurchaseable, newBC/newPopCost);
            floatPopGrowth = pop - currentPop;
            expectedPopGrowth = (int) pop - (int) currentPop;
            if (newBC < cost)
                return text(growthText);
            newBC -= cost;
        }
        else {
        	floatPopGrowth = maxPopSize - currentPop;
        	expectedPopGrowth = (int) maxPopSize - (int) currentPop;
        }

		if (allocation() == cleanupAllocationNeeded())
			return text(cleanupText);

        // if less <1% of income, show "Clean", else show "Reserve"
        if (newBC <= (c.totalIncome()/100))
            return text(growthText);
        else
            return overflowText();
    }
    @Override public float[] excessSpending() {
        Colony c = colony();
        if (c.allocation(categoryType()) == 0)
            return new float[] {0, 0};

        float totalBC = totalAvailableBCthisCategory(colony().totalProductionIncome(), colony().maxReserveIncome());
        
        // deduct cost to clean industrial waste
        float cleanCost = c.wasteCleanupCost();
        if (totalBC <= cleanCost)
            return new float[] {0, 0};

        totalBC -= cleanCost;

        Planet p = c.planet();
        Empire emp = c.empire();
        boolean canTerraformAtmosphere = p.canTerraformAtmosphere(emp);
        // deduct cost for atmospheric terraforming
        if (canTerraformAtmosphere) {
            float atmoCost = atmosphereTerraformCost() - hostileBC;
            if (totalBC <= atmoCost)
                return new float[] {0, 0};
            totalBC -= atmoCost;
        }

        // deduct cost for soil enrichment
        TechTree tr = emp.tech();
        if (!emp.ignoresPlanetEnvironment()) {
            if ((! p.isEnvironmentHostile()) || canTerraformAtmosphere) {
                if (tr.enrichSoil()) {
                    int envUpgrade = tr.topSoilEnrichmentTech().environment - p.environment();
                    if (envUpgrade > 0) {
                        float enrichCost = (envUpgrade * SOIL_UPGRADE_BC) - soilEnrichBC;
                        if (totalBC < enrichCost)
                            return new float[] {0, 0};
                        totalBC -= enrichCost;
                    }
                }
            }
        }

        // deduct cost for size terraforming
        float maxPopSize = c.maxSize();
        float roomToGrow = maxPopSize - p.currentSize();
        if (roomToGrow > 0) {
            float tformCost = roomToGrow * tr.topTerraformingTech().costPerMillion;
            if (totalBC < tformCost) 
                return new float[] {0, 0};
            totalBC -= tformCost;
        }

        // deduct cost for purchasing new pop
        float newPopPurchaseable = getNewPopPurchasableShortTerm();
        if (newPopPurchaseable > 0) {
            float growthCost = newPopPurchaseable * tr.populationCost();
            if (totalBC < growthCost)
                return new float[] {0, 0};
            totalBC -= growthCost;
        } 

        float reserveBC  = max(0,totalBC);
        return new float[] {reserveBC, reserveBC};
    }

    // get how many pops purchasable
    private float getNewPopPurchasableShortTerm() {
        float maxPopSize = colony().maxSize();
        float newPopPurchaseable = maxPopSize - colony().workingPopulation() - colony().normalPopGrowth();
        switch (options().selectedPopGrowthFactor()) {
            case "Reduced":
                newPopPurchaseable = min(newPopPurchaseable, maxPopSize/tech().populationCost());
        }
        if (newPopPurchaseable < 0) {
            return 0;
        }
        return newPopPurchaseable;
    }
    private float getNewPopPurchasableLongTerm(float targetPopRatio) {
        float maxPopSize = targetPopRatio * colony().maxSize();
        float newPopPurchaseable = maxPopSize - colony().expectedPopulationLongTerm();
        switch (options().selectedPopGrowthFactor()) {
            case "Reduced":
                newPopPurchaseable = min(newPopPurchaseable, maxPopSize/tech().populationCost());
        }
        if (newPopPurchaseable < 0)
            return 0;
        return newPopPurchaseable;
    }
    private float targetSpendingNeeded(float targetPopPct) {
        // cost to terraform planet
        float tform = terraformSpendingNeeded();
        // try to buy new population
        float newPopCost = getNewPopPurchasableLongTerm(targetPopPct) * tech().populationCost();
        newPopCost = max(0,newPopCost);
        return tform + newPopCost;
    }
    public float maxSpendingNeeded() { return targetSpendingNeeded(1.0f); }
    public float[] planetBoostCost() {
    	float[] planetBoostCost = new float[5];
        Empire emp = empire();
        TechTree tech = emp.tech();
        Planet planet = planet();
        float soilEnrichBCCopy = soilEnrichBC;
        if (planet.isEnvironmentHostile()) {
            if (planet.canTerraformAtmosphere(emp)) {
            	planetBoostCost[0] = max(0, atmosphereTerraformCost() - hostileBC);

            	if (tech.enrichSoil()) {
            		int deltaEnv = tech.topSoilEnrichmentTech().environment - planet.environment();
            		if (deltaEnv > 1) {
            			planetBoostCost[1] = max(0, SOIL_UPGRADE_BC - soilEnrichBCCopy);
            			soilEnrichBCCopy -= SOIL_UPGRADE_BC;
            			if (deltaEnv > 2)
            				planetBoostCost[2] = max(0, SOIL_UPGRADE_BC - soilEnrichBCCopy);
            		}
            	}
            }
        }
        else if (planet.isEnvironmentNormal()) {
        	if (tech.enrichSoil()) {
        		int deltaEnv = tech.topSoilEnrichmentTech().environment - planet.environment();
        		if (deltaEnv > 0) {
        			planetBoostCost[1] = max(0, SOIL_UPGRADE_BC - soilEnrichBCCopy);
        			soilEnrichBCCopy -= SOIL_UPGRADE_BC;
        			if (deltaEnv > 1)
        				planetBoostCost[2] = max(0, SOIL_UPGRADE_BC - soilEnrichBCCopy);
        		}
        	}
        }
        else if (planet.isEnvironmentFertile()) {
        	if (tech.enrichSoil()) {
        		int deltaEnv = tech.topSoilEnrichmentTech().environment - planet.environment();
        		if (deltaEnv > 0)
        			planetBoostCost[2] = max(0, SOIL_UPGRADE_BC - soilEnrichBCCopy);
        	}
        }

        float roomToGrow = colony().maxSize() - planet.currentSize();
        if (roomToGrow > 0)
        	planetBoostCost[3] = max(0, roomToGrow * tech.popIncreaseCost());

        for (int i=0; i<4; i++)
        	planetBoostCost[4] += planetBoostCost[i];

        return planetBoostCost;
    }
    
    public float terraformSpendingNeeded() {
        float cleanCost = colony().minimumCleanupCost();
        Empire emp = empire();
        TechTree tech = emp.tech();
        Planet planet = planet();
        // try to convert hostile atmosphere
        float hostileCost = 0;

        if (planet.canTerraformAtmosphere(emp))
            hostileCost = max(0, atmosphereTerraformCost() - hostileBC);

        // don't count enrichSoil cost unless not hostile or civ can terraform hostile
        float enrichCost = 0;
        // if (!emp.ignoresPlanetEnvironment()) { // BR: Still useful for size improvement
            if (!planet.isEnvironmentHostile() || planet.canTerraformAtmosphere(emp)) {
                if (tech.enrichSoil() && (tech.topSoilEnrichmentTech().environment > planet.environment())) {
                    enrichCost = ((tech.topSoilEnrichmentTech().environment - planet.environment()) * SOIL_UPGRADE_BC) - soilEnrichBC;
                    enrichCost = max(0,enrichCost);
                }
            }
        // }
        // try to terraform planet to maxSize (currently not counting incr from previous terraforms)
        float roomToGrow = colony().maxSize() - planet.currentSize();
        float terraformCost = 0;

        if (roomToGrow > 0) {
            terraformCost = roomToGrow * tech.popIncreaseCost();
            terraformCost = max(0,terraformCost);
        }
        return cleanCost + hostileCost + enrichCost + terraformCost;
    }
    public int terraformAllocationNeeded() {
        float needed = terraformSpendingNeeded();
        if (needed == 0)
            return 0;
        float prod = colony().totalProductionIncome() + colony().maxReserveIncome();
        float pctNeeded = min(1, needed / prod);
        int ticks = (int) Math.ceil(pctNeeded * MAX_TICKS);
        return ticks;
    }
    public int cleanupAllocationNeeded() {
        float needed = colony().minimumCleanupCost();
        if (needed == 0)
            return 0;
        float prod = colony().totalIncome();
        float pctNeeded = min(1, needed / prod);
        int ticks = (int) Math.ceil(pctNeeded * MAX_TICKS);
        return ticks;
    }
    public int maxAllocationNeeded(GovWorksheet gws)	{
    	return targetAllocationNeeded(gws.targetPopPercent, gws.totalIncome); }
    public int maxAllocationNeeded()	{ return targetAllocationNeeded(1.0f, colony().totalIncome()); }
    private int targetAllocationNeeded(float targetPopPct, float totalIncome)	{
        float needed = targetSpendingNeeded(targetPopPct);
        if (needed <= 0)
            return 0;
        float pctNeeded = min(1, needed / totalIncome);
        int ticks = (int) Math.ceil(pctNeeded * MAX_TICKS);
        return ticks;
    }
    @Override public int refreshAllocationNeeded(boolean prioritized, boolean hadShipSpending, float targetPopPct) {
    	return targetAllocationNeeded(targetPopPct, colony().totalIncome());
    }
    @Override public int smoothAllocationNeeded(boolean prioritized) { return maxAllocationNeeded(); }
    @Override public int smartAllocationNeeded(MouseEvent e) {
    	if (e==null || SwingUtilities.isLeftMouseButton(e))
    		return maxAllocationNeeded();
    	if (SwingUtilities.isRightMouseButton(e))
    		return terraformAllocationNeeded();
    	if (SwingUtilities.isMiddleMouseButton(e))
    		return cleanupAllocationNeeded();
    	return 0;
    }
    @Override public int govAllocationNeeded(boolean prioritized, GovWorksheet gws) {
        if (prioritized || gws.promoteWorkers || colony().govUrgeBuildUp())
        	return targetAllocationNeeded(gws.targetPopPercent, gws.totalIncome);
    	// cost to terraform planet
        float needed = terraformSpendingNeeded();
        float popToBuy = getNewPopPurchasableLongTerm(gws.targetPopPctToBuy);
//        if (!gws.promotePopGrowth) {
//        	popToBuy = min(popToBuy, gws.minPopGrowth - colony().normalPopGrowth());
//        	popToBuy = max(0, popToBuy);
//        }
        if (popToBuy>0)
        	needed += popToBuy * tech().populationCost();
        float pctNeeded = min(1, needed / gws.totalIncome);
        int ticks = ceil(pctNeeded * MAX_TICKS);
        return ticks;
    }
}
