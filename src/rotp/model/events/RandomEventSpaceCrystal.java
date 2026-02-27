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
package rotp.model.events;

import rotp.model.empires.Empire;
import rotp.model.galaxy.SpaceCrystal;
import rotp.model.galaxy.SpaceMonster;
import rotp.model.game.IGameOptions;
import rotp.ui.util.ParamInteger;

public class RandomEventSpaceCrystal extends RandomEventMonsters {
	private static final long serialVersionUID = 1L;
	// Static parameters for Tech Triggered Events
	public static final String TRIGGER_TECH		= "Stargate:0";
	public static final String TRIGGER_GNN_KEY	= "EVENT_SPACE_CRYSTAL_TRIG";
	public static final String GNN_EVENT		= "GNN_Event_Crystal";
	private int empId; // Not to be set: kept for backward compatibility
	private int sysId; // Not to be set: kept for backward compatibility
	private int turnCount; // Not to be set: kept for backward compatibility
	
	@Override protected SpaceMonster newMonster(Float speed, Float level) {
		return new SpaceCrystal(speed, level);
	}
	@Override protected int nextEmpId(boolean clear)	{ return galaxy().events().nextTargetEmpireForSpaceCrystal(clear); }
	@Override public boolean techDiscovered()	{ return !galaxy().events().spaceCrystalNotTriggered(); }
	@Override protected String name()			{ return "CRYSTAL"; }
	@Override ParamInteger delayTurn()			{ return IGameOptions.crystalDelayTurn; }
	@Override ParamInteger returnTurn()			{ return IGameOptions.crystalReturnTurn; }
	@Override protected Integer lootMonster(boolean lootMode)	{
		if (!lootMode)
			return null;
		int lootBC		= bcLootAmount();
		float lootProb	= bcLootProbability();
		float rProb		= researchLootProbability();
		int rBC			= researchLootAmount();
		boolean completeAllowed = (rProb >= 1) && !repeatable();

		Integer saleAmount = lootBC/5;
		if (random() < lootProb)
			saleAmount = lootBC;
		// Studying Crystal remains help completing the current research
		Empire emp = monster.lastAttacker();
		if (random() < rProb) {
			if (completeAllowed) {
				if (!emp.tech().propulsion().completeResearch())
					saleAmount = saleAmount * 5/2; // if no research then more gold
			} else
				if (!emp.tech().propulsion().contributeToResearch(rBC))
					saleAmount += rBC; // if no research then more gold
		}
		// Selling the Crystal part gives reserve BC, scaling with turn number
		return saleAmount;
	}
	// Don't use! For backward compatibility only, when a monster was already launched
	@Override protected int oldEmpId()			{ return empId; }
	@Override protected int oldSysId()			{ return sysId; }
	@Override protected int oldTurnCount()		{ return turnCount; }
}
