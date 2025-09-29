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
package rotp.model.ai.governor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import rotp.model.ai.interfaces.SpyMaster;
import rotp.model.empires.DiplomaticEmbassy;
import rotp.model.empires.Empire;
import rotp.model.empires.EmpireView;
import rotp.model.empires.SpyNetwork;
import rotp.model.empires.SpyNetwork.Sabotage;
import rotp.model.empires.SpyNetwork.SabotageTargets;
import rotp.model.empires.SystemInfo;
import rotp.model.galaxy.Location;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.GovernorOptions;
import rotp.util.Base;

public class AISpyMaster implements Base, SpyMaster {
	// Copied from Xilmi's AISpyMaster and adapted for the governor
	private final Empire empire;
	public AISpyMaster (Empire c)	{ empire = c; }
	// Copied from Xilmi's AISpyMaster No changes
	@Override public int suggestedInternalSecurityLevel()	{
		// invoked after nextTurn() processing is complete on each civ's turn
		// also invoked when contact is made in mid-turn
		// return from 0 to 40, which translates to 0% to 20% of total prod
		//
		// modnar: is it not 0% to 10% of total prod, for a max of +20% security bonus, with 0 to 10 ticks/clicks?
		// MAX_SECURITY_TICKS = 10 in model/empires/Empire.java
		if(options().forbidTechStealing())
			return 0;
		float paranoia = 0;
		float avgOpponentTechLevel = 0;
		float opponentCount = 0;
		for (EmpireView cv : empire.empireViews()) {
			if ((cv != null) && cv.embassy().contact() && cv.inEconomicRange()) {
				if(cv.isMember(empire.allies()))
					continue;
				avgOpponentTechLevel += cv.techUncut().maxTechLevel();
				opponentCount++;
			}
		}
		if(opponentCount > 0)
			avgOpponentTechLevel /= opponentCount;
		paranoia = empire.tech().avgTechLevel() - avgOpponentTechLevel;
		if(avgOpponentTechLevel == 0)
			paranoia = 0;
		if (paranoia < 0)
			paranoia = 0;
		//System.out.println(empire.galaxy().currentTurn()+" "+ empire.name()+" counter-espionage: "+paranoia+" mt: "+empire.tech().avgTechLevel()+" ot: "+avgOpponentTechLevel);
		return min(10, (int)Math.round(paranoia)); // modnar: change max to 10, MAX_SECURITY_TICKS = 10
	}
	// Copied from Xilmi's AISpyMaster Then moved/added governor code
	@Override public void setSpyingAllocation(EmpireView v)	{
		// invoked after nextTurn() processing is complete on each civ's turn
		// also invoked when contact is made in mid-turn
		// how much allocation for the spyNetwork?
		// each pt of allocation represents .005 of total civ production
		// max allocation is 25, or 10% of total civ production
		GovernorOptions governor = govOptions();
		if (v.owner().spendingNotYetMade() && !governor.trainSpiesASAP())
			return;

		DiplomaticEmbassy emb = v.embassy();
		SpyNetwork spies = v.spies();

		// situations where no spies are ever needed
		if (!emb.contact() || v.extinct() || !v.inEconomicRange() || emb.unity()) {
			spies.allocation(0);
			return;
		}
		if (governor.respectPromises()
				&& !spies.govIgnoreThreat()
				&& v.timerIsActive()) {
			// Then respect your promise
			// Cut allocation for hide and ShutDown
			if(spies.allocation() > 0)
				spies.allocation(0);
			// Remove the spies if ShutDown
			if(spies.maxSpies() > 0 && spies.govShutdownSpy())
				spies.maxSpies(0);
			return;
		}
		if(governor.isAutoSpy()) {
			setAutoSpyAllocation(v);
			return;
		}
		if(governor.isAutoInfiltrate()) {
			if(spies.allocation() < 1)
				spies.allocation(1);
			if(spies.maxSpies() < 1)
				spies.maxSpies(1);
			return;
		}
	}
	private void setAutoSpyAllocation(EmpireView v) { // Copied from Xilmi's AISpyMaster
		DiplomaticEmbassy emb = v.embassy();
		SpyNetwork spies = v.spies();

		int previousSpiesAllocation = spies.allocation();
		int maxSpiesNeeded = 0;

		if (emb.finalWar())
			maxSpiesNeeded = 3;
		else if (emb.war())
			maxSpiesNeeded = 2;
		else if (emb.noTreaty()) 
			maxSpiesNeeded = 1;
		else if (emb.pact())
			maxSpiesNeeded = 1;
		else if (emb.atPeace())
			maxSpiesNeeded = 1;
		else if (emb.alliance()) 
			maxSpiesNeeded = 0;
		
		if (spies.numActiveSpies() >= spies.maxSpies())
			spies.allocation(0);
		else {
			maxSpiesNeeded *= 2;
			//ail: avoid inefficient overspending by adjusting to spy-costs
			float bcPerTick = empire.totalPlanetaryProduction() * empire.spySpendingModifier() / 200.0f;
			float maxTicksNeeded = spies.maxSpies() * empire.baseSpyCost() / bcPerTick;
			maxSpiesNeeded = min(maxSpiesNeeded, (int)Math.ceil(maxTicksNeeded));
			if(spies.lastSpyDate() == -1)
				maxSpiesNeeded = (int)Math.ceil(max(maxSpiesNeeded, spies.realCostForNextSpy() / bcPerTick));
			spies.allocation(maxSpiesNeeded);
		}
		if (spies.allocation() > previousSpiesAllocation
				&& v.owner().spendingNotYetMade()
				&& govOptions().contactUpdateSpending()) {
			v.owner().redoGovTurnDecisions();
		}
	}
	// Copied from Xilmi's AISpyMaster, adapted for Governor
	@Override public void setSpyingMission(EmpireView v)	{
		// invoked for each CivView for each civ after nextTurn() processing is complete on each civ's turn
		// also invoked when contact is made in mid-turn
		// 0 = hide; 1 = sabotage; 2 = espionage
		GovernorOptions governor = govOptions();
		if (!governor.isAutoSpy())
			return;
		if (v.owner().spendingNotYetMade() && !governor.trainSpiesASAP())
			return;

		DiplomaticEmbassy emb = v.embassy();
		SpyNetwork spies = v.spies();

		// extinct or no contact = hide
		if (v.extinct() || !emb.contact()) {
			spies.beginHide();
			spies.maxSpies(0);
			return;
		}

		// they are our allies
		if (emb.alliance() || emb.unity()) {
			spies.beginHide();
			spies.maxSpies(0);
			return;
		}
		if (governor.respectPromises() && !emb.anyWar() && v.timerIsActive()) {
			if (spies.govShutdownSpy()) {
				spies.beginHide();
				spies.maxSpies(0);
				return;
			}
			if (spies.govHideSpy()) {
				spies.beginHide();
				return;
			}
		}
		// we've been warned and they are not our enemy (i.e. no war preparations)
		boolean canEspionage = !spies.possibleTechs().isEmpty();
		Sabotage sabMission = bestSabotageChoice(v);
		boolean canSabotage = spies.canSabotage() && (sabMission != null);

		// we are in a pact or at peace
		// ail: according to official strategy-guide two spies is supposedly the ideal number for tech-stealing etc, so always setting it to two except for hiding
		// let's see what happens, if we just nonchalantly spy on everyone regardless of anything considering they won't declare war unless they would do so anyways
		if (emb.pact() || emb.atPeace() || emb.noTreaty()) {
			if(canEspionage)
			{
				spies.beginEspionage();
				spies.maxSpies(2);
			}
			else
			{
				spies.beginHide();
				spies.maxSpies(1);
			}
			return;
		}
		if (emb.anyWar()) {
			if (canEspionage)
			{
				spies.beginEspionage();
				spies.maxSpies(2);
			}
			else if (canSabotage)
			{
				spies.beginSabotage();
				spies.maxSpies(2);
			}
			else
			{
				spies.beginHide();
				spies.maxSpies(1);
			}
			return;
		}
		
		// default for any other treaty state (??) is to hide
	   spies.beginHide();
	}
	// Copied from Xilmi's AISpyMaster No changes
	// TODO BR: Add option for user preference
	@Override public Sabotage bestSabotageChoice(EmpireView v)	{
		// invoked when a Sabotage attempt is successful
		// unfinished - AI needs to choose best sabotage type
		SabotageTargets targets = v.spies().sabotageTargets();
		if (!targets.rebellionTargets.isEmpty())
			return Sabotage.REBELS;
		else if (!targets.baseTargets.isEmpty())
			return Sabotage.MISSILES;
		else if (!targets.factoryTargets.isEmpty())
			return Sabotage.FACTORIES;
		else
			return null;
	}
	// Based on Xilmi's AISpyMaster
	// TODO BR: Add option for user preferences
	@Override public StarSystem bestSystemForSabotage(EmpireView v, Sabotage choice)	{
		// invoked when a Sabotage attempt is successful
		// choice: 1 - factories, 2 - missiles, 3 - rebellion
		// BR: replaced by the call that will follow the Dark Galaxy rules (if any)

		// if there are unexplored systems, we'll prefer those and start with the closest
		SabotageTargets targets = v.spies().sabotageTargets();
		Set<StarSystem> allTargets = new HashSet<>();
		allTargets.addAll(targets.rebellionTargets);
		allTargets.addAll(targets.factoryTargets);
		allTargets.addAll(targets.baseTargets);

		SystemInfo sv = empire.sv;
		List<StarSystem> unscouteds = allTargets.stream().filter(
				sys -> !sv.isScouted(sys.id)).collect(Collectors.toList());
		if (unscouteds.isEmpty())
			return SpyMaster.super.bestSystemForSabotage(v, choice);

		float transportRange = empire.shipRange();
		List<StarSystem> inTransportRange = unscouteds.stream().filter(
				sys -> sv.distance(sys.id)<=transportRange).collect(Collectors.toList());

		// if none are in range for massive attack, choose the closest to our borders
		// as it's probably the next to be reachable
		if (inTransportRange.isEmpty()) {
			StarSystem.VIEWING_EMPIRE = empire;
			Collections.sort(unscouteds, StarSystem.VDISTANCE);
			return unscouteds.get(0);
		}

		// Some are reachable, choose the closest to our center for a more massive attack
		StarSystem best = null;
		Location colonyCenter = empire.generalAI().colonyCenter(empire);
		float lowestDistance = Float.MAX_VALUE;
		for (StarSystem tgt: inTransportRange) {
			float distance = colonyCenter.distanceTo(tgt);
			if (distance < lowestDistance) {
				lowestDistance = distance;
				best = tgt;
			}
		}
		return best;
	}
	// Copied from Xilmi's AISpyMaster No changes
	@Override public Empire suggestToFrame(List<Empire> empires)	{
		if (empires.size() < 2)
			return null;
		Empire e1 = empires.get(0);
		Empire e2 = empires.get(1);
		EmpireView v1 = empire.viewForEmpire(e1);
		EmpireView v2 = empire.viewForEmpire(e2);

		// throw enemies under the bus first
		if (v1.embassy().anyWar() && !v2.embassy().anyWar())
			return e1;
		if (!v1.embassy().anyWar() && v2.embassy().anyWar())
			return e2;

		// throw allies under the bus last
		if (v1.embassy().alliance() && !v2.embassy().alliance())
			return e2;
		if (!v1.embassy().alliance() && v2.embassy().alliance())
			return e1;

		// throw the stronger guy under the bus
		//if (v1.empireUncut().powerLevel(v1.empireUncut()) > v2.empireUncut().powerLevel(v2.empireUncut()))
		if (v1.powerLevelUncut() > v2.powerLevelUncut())
			return e1;
		else
			return e2;
	}
}
