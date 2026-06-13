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

import static rotp.model.game.IBaseOptsTools.MOD_UI;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

import rotp.model.combat.CombatStackTestRepulsor;
import rotp.model.ships.ShipArmor;
import rotp.model.ships.ShipComputer;
import rotp.model.ships.ShipDesign;
import rotp.model.ships.ShipDesignLab;
import rotp.model.ships.ShipECM;
import rotp.model.ships.ShipEngine;
import rotp.model.ships.ShipShield;
import rotp.ui.util.ParamInteger;

// modnar: add Space Pirates random event
public final class SpaceTestRepulsor extends SpaceMonster {
	private static final long serialVersionUID = 1L;
    private static final Color shieldColor	= Color.magenta;
    private static final String imageKey	= "SPACE_PIRATES";
    private static final boolean isFusion	= true;
	private static BufferedImage monsterImage; // no need to have hundred copy of this
	private static int numStack = 4;

	public static final ParamInteger piratesLevelPct = new ParamInteger(MOD_UI, "PIRATES_LEVEL_MULT", 100)
			.setLimits(10, 500)
			.setIncrements(1, 5, 20)
			.pctValue(true);

	public SpaceTestRepulsor(Float speed, Float level)	{ super(imageKey, ORIGINAL_ROAMING_EMPIRE, speed, level); }
	@Override public void initCombat() {
		super.initCombat();
		for (int id=0; id<ShipDesignLab.MAX_DESIGNS; id++)
			if (id >= numStack)
				return;
			else
				addCombatStack(new CombatStackTestRepulsor(this, imageKey, stackLevel(), id, isFusion, shieldColor));
	}
	protected BufferedImage getMapImage()		{
		if (monsterImage == null)
			monsterImage = super.getMapImage();
		return monsterImage;
	}
	@Override protected Float stackLevel()	{ return 1f; }
	@Override protected void initDesigns()	{
		super.initDesigns();
		for (int id=0; id<ShipDesignLab.MAX_DESIGNS; id++) {
			if (id >= numStack)
				return;

			num(id, 1);
			ShipDesign des = monsterDesign();
			des.setImage(image());
			des.name(text(nameKey));
			des.id(id);
			designs[id] = des;
		}
	}
	@Override protected ShipDesign monsterDesign() {
		ShipDesignLab lab = empire().shipLab();
		ShipDesign design = lab.newBlankDesign(ShipDesign.MEDIUM);
		design.mission	(ShipDesign.DESTROYER);

		List<ShipEngine> engines = lab.engines();
		design.engine	(engines.get(0));

		List<ShipComputer> computers = lab.computers();
		design.computer	(computers.get(0));

		List<ShipArmor> armors = lab.armors();
		design.armor	(armors.get(0));

		List<ShipShield> shields = lab.shields();
		design.shield	(shields.get(0));

		List<ShipECM> ecms = lab.ecms();
		design.ecm		(ecms.get(0));

		int maneuver = 0;
		design.maneuver(lab.maneuver(0));
		design.monsterManeuver(maneuver);

		design.special(0, lab.specialRepulsor());

		design.monsterInitiative(1);

		return design;
	}
}
