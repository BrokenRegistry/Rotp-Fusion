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
import java.util.List;

import rotp.model.combat.CombatStack;
import rotp.model.combat.CombatStackColony;
import rotp.model.empires.Empire;
import rotp.model.empires.EmpireView;
import rotp.model.game.IGameOptions;

public final class ShipCaptainAdvisor extends rotp.model.ai.xilmi.AIShipCaptain {
	private final Empire alien;
	private CombatStackColony col;
	private EmpireView colView;
	private IGameOptions options;

	private boolean playerCanRetreat;
	private boolean playerCanDeclareWar;
	// Auto Resolve
	private boolean inPact, notAnEnemyColony;
	// Smart Resolve
	private boolean playerShouldRetreat;
	private boolean facingOverwhelmingForce;
	private boolean isCivilFleet;
	private boolean fleetWantToFight;

	public ShipCaptainAdvisor (Empire empire)	{
		super (empire);
		options	= options();
		alien	= mgr.results().aiEmpire();
		col		= mgr.results().colonyStack;
		colView	= (col == null) ? null : empire.viewForEmpire(col.empire());

		performRetreatAnalysis();
	}
	public boolean isCivilFleet()				{ return isCivilFleet; }
	public boolean facingOverwhelmingForce()	{ return facingOverwhelmingForce; }
	public boolean playerShouldRetreat()		{ return playerShouldRetreat; }
	public boolean notAnEnemyColony()			{ return notAnEnemyColony; }
	public boolean fleetWantToFight()			{ return fleetWantToFight; }
	public boolean playerCanDeclareWar()		{ return playerCanDeclareWar; }

	public void performRetreatAnalysis()		{
		// backup the activeStacks as we do not want the changes to be permanent
		List<CombatStack> activeStacks = mgr.activeStacks();
		List<CombatStack> backupStacks = new ArrayList<>(activeStacks);

		// Basic info
		isCivilFleet		= civilFleetAnalysis();
		playerCanDeclareWar	= options.canStartWar(empire, alien);
		playerCanRetreat	= options.playerCanRetreat();
		inPact				= (colView != null) && colView.embassy().pact();
		notAnEnemyColony	= (colView != null) && !colView.isMember(empire.enemies());

		// Fleet strength info
		List<CombatStack> retreatingFleets = new ArrayList<>();
		boolean retreating = true;
		while (retreating) {
			retreatingFleets.clear();
			for (CombatStack cst : activeStacks)
				if (cst.isPlayerControlled() && playerCanRetreat && wouldRetreat(cst))
					retreatingFleets.add(cst);
			activeStacks.removeAll(retreatingFleets);
			retreating = !retreatingFleets.isEmpty();
		}

		// Check if player is still active
		fleetWantToFight = false; 
		playerShouldRetreat = playerCanRetreat;
		for (CombatStack st: activeStacks)
			if (st.empire() == empire && (st.isArmed() || !st.isColony())) {
				fleetWantToFight = true;	// Do not retreat if one of the stack want to fight
				break;
			}
		boolean wantToLeave = !fleetWantToFight || notAnEnemyColony;
		playerShouldRetreat = playerCanRetreat && wantToLeave;

		// Restore the activeStacks
		activeStacks.clear();
		activeStacks.addAll(backupStacks);
	}
	private boolean civilFleetAnalysis()	{
		for (CombatStack cst: mgr.activeStacks())
			if(cst.empire() == empire && cst.isArmed())
				return false;
		return true;
	}
	private boolean wouldRetreat(CombatStack currStack)	{
		if (currStack == col)
			return false;

		// when auto-resolving, retreat player stacks ONLY when not
		// retreating would violate a pact, or when the stack is unarmed
		// armed stacks will otherwise fight to the death, per player expectations
		if (!mgr.allowRetreat) {
			boolean atLeastOneStackStillArmed = false;
			for(CombatStack cst : mgr.allStacks())
				if(cst.empire() == empire && cst.isArmed()) {
					atLeastOneStackStillArmed = true;
					break;
				}
			return inPact || !atLeastOneStackStillArmed;
		}

		// if stack is pacted with colony and doesn't want war, then retreat
		// ail: Whether I want a war or not depends on whether the other faction is an enemy, not on relation!
		if ((colView != null) && !colView.isMember(empire.enemies()))
			return true;

		List<CombatStack> activeStacks = new ArrayList<>(mgr.activeStacks());
		// don't retreat if all enemies can only target planets
		boolean canBeTargeted = false;
		boolean canTarget = false;
		for (CombatStack st: activeStacks) {
			if (st.canPotentiallyAttack(currStack))
				canBeTargeted = true;
			if(currStack.canPotentiallyAttack(st))
				canTarget = true;
		}
		if (!canBeTargeted)
			return false;
		if(!canTarget)
			return true;

		if (facingOverwhelmingForce(currStack, false)) {
			return true;
		}
		return false;
	}
	
}
