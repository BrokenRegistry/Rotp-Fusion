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
import rotp.model.galaxy.SpaceAmoeba;
import rotp.model.galaxy.SpaceMonster;
import rotp.model.game.IGameOptions;
import rotp.ui.util.ParamInteger;

public class RandomEventSpaceAmoeba extends RandomEventMonsters {
	private static final long serialVersionUID = 1L;
	// Static parameters for Tech Triggered Events
	public static final String TRIGGER_TECH		= "Cloning:1";
	public static final String TRIGGER_GNN_KEY	= "EVENT_SPACE_AMOEBA_TRIG";
	public static final String GNN_EVENT		= "GNN_Event_Amoeba";
	private int empId; // Not to be set: kept for backward compatibility
	private int sysId; // Not to be set: kept for backward compatibility
	private int turnCount; // Not to be set: kept for backward compatibility

	public RandomEventSpaceAmoeba() {
		//System.out.println(LocalTime.now() + " No Galaxy: " + " New RandomEventSpaceAmoeba was created");
	}
	@Override protected SpaceMonster newMonster(Float speed, Float level) {
//		if (galaxy() != null)
//			System.out.println(galaxy().currentTurn() + " RandomEventSpaceAmoeba: newMonster was created");
//		else
//			System.out.println(LocalTime.now() + " No Galaxy: " + " RandomEventSpaceAmoeba: newMonster was created");
		return new SpaceAmoeba(speed, level);
	}
	@Override protected int nextEmpId(boolean clear)	{ return galaxy().events().nextTargetEmpireForSpaceAmoeba(clear); }
	@Override public boolean techDiscovered()	{ return !galaxy().events().spaceAmoebaNotTriggered(); }
	@Override protected String name()			{ return "AMOEBA"; }
	@Override ParamInteger delayTurn()			{ return IGameOptions.amoebaDelayTurn; }
	@Override ParamInteger returnTurn()			{ return IGameOptions.amoebaReturnTurn; }
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
		// Studying amoeba remains help completing the current research
		Empire emp = monster.lastAttacker();
		if (random() < rProb) {
			if (completeAllowed) {
				if (!emp.tech().planetology().completeResearch())
					saleAmount = saleAmount * 5/2; // if no research then more gold
			} else
				if (!emp.tech().planetology().contributeToResearch(rBC))
					saleAmount += rBC; // if no research then more gold
		}
		// Selling the amoeba flesh gives reserve BC, scaling with turn number
		return saleAmount;
	}
	// Don't use! For backward compatibility only, when a monster was already launched
	@Override protected int oldEmpId()			{ return empId; }
	@Override protected int oldSysId()			{ return sysId; }
	@Override protected int oldTurnCount()		{ return turnCount; }
}
