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
package rotp.model.galaxy;

import java.awt.Color;
import java.util.List;

import rotp.model.combat.CombatStackMonster;
import rotp.model.ships.ShipArmor;
import rotp.model.ships.ShipComputer;
import rotp.model.ships.ShipDesign;
import rotp.model.ships.ShipDesignLab;
import rotp.model.ships.ShipECM;
import rotp.model.ships.ShipEngine;
import rotp.model.ships.ShipShield;

public class SpaceJellyfish extends GuardianMonsters {
	private static final long serialVersionUID = 1L;
	private static final Color shieldColor	= Color.blue;
	private static final String imageKey	= "SPACE_JELLYFISH";
	private static final boolean isFusion	= true;

	public SpaceJellyfish(Float speed, Float level) {
		super(imageKey, -2, speed, level);
		num(0, 1); // Number of monsters
	}

	@Override public void initCombat()			{
		super.initCombat();
		addCombatStack(new CombatStackMonster(this, imageKey, stackLevel(), 0, isFusion, shieldColor));
	}
	@Override public SpaceMonster getCopy()		{ return new SpaceJellyfish(null, null); }
	@Override protected int otherSpecialCount() { return 2; }
	@Override protected ShipDesign designRotP()	{
		ShipDesignLab lab = empire().shipLab();
		ShipDesign design = lab.newBlankDesign(Math.max(ShipDesign.MEDIUM,
								stackLevel(ShipDesign.LARGE, ShipDesign.HUGE)));
		design.mission	(ShipDesign.DESTROYER);

		List<ShipEngine> engines = lab.engines();
		design.engine	(engines.get(stackLevel(0, engines.size()-1)));

		List<ShipComputer> computers = lab.computers();
		design.computer	(computers.get(stackLevel(2, computers.size()-1)));

		List<ShipArmor> armors = lab.armors();
		design.armor	(armors.get(stackLevel(2, armors.size()-1)));

		List<ShipShield> shields = lab.shields();
		design.shield	(shields.get(stackLevel(2, shields.size()-1)));

		List<ShipECM> ecms = lab.ecms();
		design.ecm		(ecms.get(stackLevel(2, ecms.size()-1)));

		int maneuver = max(2, stackLevel(2));
		design.maneuver(lab.maneuver(maneuver));
		design.monsterManeuver(maneuver);

		int wpnAll = max(1, stackLevel(10));
		for (int i=4; i>0; i--) {
			int count = wpnAll/i;
			if (count != 0) {
				design.weapon(i-1, lab.jellyfishDart(), count);
				wpnAll -= count;
			}
		}
		design.special(0, lab.specialBattleScanner());
		design.special(1, lab.specialResistStasis());		// Immune to Stasis
		
		design.monsterInitiative(100);
		
		return design;
	}
}
