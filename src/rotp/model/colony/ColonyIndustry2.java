/*
 * Copyright 2015-2020 Ray Fowler
 * 
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *	 https://www.gnu.org/licenses/gpl-3.0.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rotp.model.colony;

import static rotp.model.tech.TechRoboticControls.BASE_ROBOT_CONTROLS;
import static rotp.model.tech.TechRoboticControls.MAX_ROBOT_CONTROLS;

import rotp.model.empires.Empire;
import rotp.model.planet.Planet;

public class ColonyIndustry2 extends ColonyIndustry {
	private static final long serialVersionUID = 1L;
//	protected float factories = 0;
//	protected float previousFactories = 0;	// To be displayed on colonies panel
//	protected int   robotControls	  = 2; // currently implemented may be <> topRobotControl
//	protected float industryReserveBC = 0;
//	protected float unallocatedBC	  = 0;
//	protected float newFactories	  = 0;
	private Factories plants;

	@Override public void init(Colony c) {
		super.init(c);
		plants = new Factories(c.empire().robotControlsAdj());
		resetFactories(0);

		// TODO BR: will evolve
		robotControls = BASE_ROBOT_CONTROLS;
		industryReserveBC = 0;
		unallocatedBC = 0;
		newFactories = 0;
	}
	// ########## Validated Overriders ##########
	//
	@Override public void resetFactories(float newNumber)	{ previousFactories = plants.reset(newNumber); }
	@Override public void removeFactories(float toRemove)	{ plants.cut(toRemove); }
    @Override public void addToAlienFactories() 	{ planet().addAlienFactories(empire().id, plants); }
	@Override public void capturedBy(Empire newCiv) {
		if (newCiv == empire())
			return;

		Planet p = planet();
		p.addAlienFactories(empire().id, plants);
		industryReserveBC = 0;
		robotControls = newCiv.tech().topRobotControls();
		plants = p.alienPlants(newCiv.id);
		p.removeAlienFactories(newCiv.id);
		unallocatedBC = 0;
		newFactories = 0;
		previousFactories = 0;
	}
    @Override protected int convertableAlienFactories()  { 
        return convertableAlienFactories(tech().topRobotControls());
    }
	@Override public void nextTurn(float totalProd, float totalReserve) {
		if (factories < 0) // correct possible data issue
			factories = 0;
		robotControls = tech().topRobotControls();

		previousFactories = factories;
		// prod gets planetary bonus, but not reserve
		float prodBC = pct()* totalProd * planet().productionAdj();
		float rsvBC = pct() * totalReserve;
		float newBC = prodBC+rsvBC+industryReserveBC;
		industryReserveBC = 0;
		newFactories = 0;
		
		// First try to maximize expected population work
		float expectedPopulation = expectedPopulation();
		newBC = buildMissingFactories(newBC, expectedPopulation, plants, true);
		if (newBC == 0)
			return;

		// Then continue to spend allowed BC
		newBC = buildMissingFactories(newBC, planet().maxSize(), plants, true);

		// send remaining BC to empire reserve
		unallocatedBC = newBC;
	}
	
	// ########## To be validated later ##########
	//
	@Override public float newFactoryCost()			{ return tech().newFactoryCost(robotControls()); }
	@Override public int effectiveRobotControls()	{ 
		if(empire().ignoresFactoryRefit())
			return maxRobotControls();
		return robotControls() + empire().robotControlsAdj(); 
	}

	@Override public float maxNewFactories(float bc) {
		return min(maxUseableFactories()-factories(), bc/newFactoryCost());
	}
	// ########## To be Validated ##########
	//

	@Override public float excessSpending()	{
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
	@Override public String upcomingResult()   {
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
	@Override public float maxSpendingNeeded() {
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
	@Override public int maxAllocationNeeded() {
		float needed = maxSpendingNeeded();
		if (needed <= 0)
			return 0;
		float pctNeeded = min(1, needed / colony().totalIncome());
		int ticks = (int) Math.ceil(pctNeeded * MAX_TICKS);
		return ticks;
	}
	@Override public int minAllocationNeeded() {
		float needed = spendingNeeded();
		if (needed <= 0)
			return 0;
		float pctNeeded = min(1, needed / colony().totalIncome());
		int ticks = (int) Math.ceil(pctNeeded * MAX_TICKS);
		return ticks;
	}
	@Override public Float[] factoryBalance()	 {
		// Population expectation
		float expectedPopulation = expectedPopulation();
		float expectedMissingPopulation = planet().currentSize() - expectedPopulation;
		// Factories
		float maxFactories		 = maxBuildableFactories();
		float upcomingFactories  = upcomingFactories();
		float maxNeededFactories = maxFactories - factories - upcomingFactories;
		float neededFactories	= maxNeededFactories - expectedMissingPopulation * robotControls;
		Float factoryBalance	 = -neededFactories;
 
		if (robotControls != tech().topRobotControls()
				|| convertableAlienFactories() != 0) { // check for refit
			factoryBalance = null;
		}
		return new Float[] {factoryBalance, upcomingFactories, neededFactories, factories, maxFactories};
	}
	//
	// PRIVATE METHODS
	//
	private float spendingNeeded() {
		float builtFactories = factories;
		int colonyControls = robotControls;
		float expectedMissingPopulation = planet().currentSize() - expectedPopulation();
		float notTobuild = expectedMissingPopulation * tech().topRobotControls();
		builtFactories += notTobuild;

		float totalCost = 0;
		int previouslyConvertedFactories = 0;
		
		// Cost of all
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

	private float buildMissingFactories(float newBC, float population, Factories plants, boolean nt) {
		float[] missingFactories = plants.missingFactories(population, robotControls, empire().robotControlsAdj());
		float alienFactories = planet().numAlienPlants();
		int idx = 0;
		while ((newBC > 0) && (idx <= missingFactories.length)) {
			if (missingFactories[idx] <= 0) {
				idx++;
				continue;
			}
			if (idx == 0) { // just build some. // TODO BR Check for alien factories.
				float unitCost = tech().newFactoryCost(idx + BASE_ROBOT_CONTROLS);
				float cost = missingFactories[idx] * unitCost;
				float builtFactories = 0;
				if (cost > newBC) {
					builtFactories = newBC/unitCost;
					plants.add(idx, builtFactories);
					if (nt) // for Next Turn only
						newFactories += builtFactories;
					return 0; // no more BC... Exit.
				}
				else {
					builtFactories = missingFactories[idx];
					plants.add(idx, builtFactories);
					if (nt) // for Next Turn only
						newFactories += builtFactories;
					newBC -= cost;
					idx++;
					continue;
				}
			}
			else { // Refit and Build simultaneously 
				float dualCost = tech().newFactoryCost(idx + BASE_ROBOT_CONTROLS);
				if (!empire().ignoresFactoryRefit())
					dualCost += tech().bestFactoryCost() / 2;
				float unitCost = dualCost/2;
				float cost = missingFactories[idx] * unitCost;
				float builtFactories = 0;
				if (cost > newBC) {
					builtFactories = newBC/unitCost;
					plants.sub(idx-1, builtFactories/2); // refit
					plants.add(idx, builtFactories);	 // Refit + new built
					if (nt) // for Next Turn only
						newFactories += builtFactories/2;	 // new built
					return 0; // no more BC... Exit.
				}
				else {
					builtFactories = missingFactories[idx];
					plants.sub(idx-1, builtFactories/2); // refit
					plants.add(idx, builtFactories);	 // Refit + new built
					if (nt) // for Next Turn only
						newFactories += builtFactories/2;	 // new built
					newBC -= cost;
					idx++;
					continue;
				}
			}
		}
		return newBC;
	}

	// ########## Sub classes
	//
	public final class Factories {
		private final float[] factories; // idx 0 = BASE_ROBOT_CONTROLS
		private Float totalFactories;

		Factories(int adj) {
			factories = new float[1 + MAX_ROBOT_CONTROLS - BASE_ROBOT_CONTROLS];
			totalFactories = 0f;
		}
		// ###### Setters ######
		//
		float reset (float startingValue)	{
			for (int idx=0; idx<factories.length; idx++)
				factories[idx] = 0;
			totalFactories = Math.max(0, startingValue);
			factories[0] = totalFactories;
			return totalFactories;
		}
//		void set (int rc, float plants)		{
//			factories[rc-BASE_ROBOT_CONTROLS]  = plants;
//			totalFactories = null;
//		}
		// only allowed call from industry2
		private void add (int idx, float plants)		{
			factories[idx] += plants;
			totalFactories = null;
		}
		// only allowed call from industry2
		private void sub (int idx, float plants)		{
			factories[idx] -= plants;
			totalFactories = null;
		}
		void cut (int rc, float plants)		{
			factories[rc-BASE_ROBOT_CONTROLS] -= plants;
			totalFactories = null;
		}
		public void cut (float plants) 		{
			for (int idx=factories.length-1; idx>=0; idx--) {
				if (factories[idx] >= plants) {
					factories[idx] -= plants;
					return;
				}
				else {
					plants -= factories[idx];
					factories[idx] = 0;
				}
			}
			totalFactories = null;
		}
		// ###### getters ######
		//
		public float factories()			{
			if (totalFactories == null) {
				totalFactories = 0f;
				for (float f : factories)
					totalFactories += f;
			}
			return totalFactories;
		}
		float factories(int rc)				{ return factories[rc-BASE_ROBOT_CONTROLS]; }
		float usableFactories(float population, int rcAdj)	{
			float num = 0;
			for (int idx=factories.length-1; idx>=0; idx--) {
				int controls = idx + BASE_ROBOT_CONTROLS + rcAdj;
				float maxFact = population * controls;
				if (factories[idx] >= maxFact) { // enough factories of this level
					num += maxFact;
					return num;
				}
				else { 
					num += factories[idx];
					population -= factories[idx]/controls;
				}
			}
			return num;
		}
		int bestBuiltRoboticControl ()		{
			for (int idx=factories.length-1; idx>=0; idx--)
				if (factories[idx] > 0)
					return idx+BASE_ROBOT_CONTROLS;
			return BASE_ROBOT_CONTROLS;
		}
		float[] missingFactories(float population, int rc, int rcAdj)	{ // TODO
			float[] missingFactories = new float[rc];
			for (int idx=rc-1; idx>=0; idx--) {
				int controls = idx + BASE_ROBOT_CONTROLS + rcAdj;
				float maxFact = population * controls;
				if (factories[idx] >= maxFact) // enough factories of this level
					return missingFactories;
				else { 
					missingFactories[idx] = maxFact-factories[idx];
					population -= factories[idx]/controls;
				}
			}
			return missingFactories;
		}
	}
}
