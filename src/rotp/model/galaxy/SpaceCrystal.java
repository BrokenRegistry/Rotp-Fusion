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
package rotp.model.galaxy;

import static rotp.model.game.IBaseOptsTools.MOD_UI;
import static rotp.model.game.IBaseOptsTools.MOO1_DEFAULT;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

import rotp.model.colony.Colony;
import rotp.model.combat.CombatStackMonster;
import rotp.model.combat.CombatStackSpaceCrystal;
import rotp.model.planet.PlanetType;
import rotp.model.ships.ShipArmor;
import rotp.model.ships.ShipComputer;
import rotp.model.ships.ShipDesign;
import rotp.model.ships.ShipDesignLab;
import rotp.model.ships.ShipECM;
import rotp.model.ships.ShipEngine;
import rotp.model.ships.ShipShield;
import rotp.ui.util.ParamBoolean;
import rotp.ui.util.ParamInteger;

public final class SpaceCrystal extends SpaceMonster {
    private static final long serialVersionUID = 1L;
    private static final Color shieldColor	= Color.cyan;
    private static final String imageKey	= "SPACE_CRYSTAL";
    private static final boolean isFusion	= false;
	private static BufferedImage monsterImage; // no need to have hundred copy of this

	public static final ParamInteger crystalLevelPct = new ParamInteger(MOD_UI, "CRYSTAL_LEVEL_MULT", 100)
			.setLimits(10, 500)
			.setIncrements(1, 5, 20)
			.pctValue(true);
	public static ParamBoolean isMoO1Monster = new ParamBoolean(MOD_UI, "IS_MOO1_SPACE_CRYSTAL", false)
			.setDefaultValue(MOO1_DEFAULT, true)
			.formerName(MOD_UI + "IS_MOO1_MONSTER");

    public SpaceCrystal(Float speed, Float level)	{ super(imageKey, ORIGINAL_ROAMING_EMPIRE, speed, level); }

	@Override public boolean isMoO1Monster()	{ return isMoO1Monster.get(); };
    @Override public void initCombat()	{
    	super.initCombat();
		if (isMoO1Monster.get())
			addCombatStack(new CombatStackMonster(this, imageKey, stackLevel(), 0, isFusion, shieldColor));
		else
			addCombatStack(new CombatStackSpaceCrystal(this, imageKey, stackLevel(), 0, shieldColor));
    }
	@Override protected BufferedImage getMapImage()	{
		if (monsterImage == null)
			monsterImage = super.getMapImage();
		return monsterImage;
	}
	@Override protected Float stackLevel()		{ return super.stackLevel() * crystalLevelPct.get()/100f; }
    @Override public SpaceMonster getCopy()		{ return new SpaceCrystal(null, null); }
    @Override protected int otherSpecialCount()	{ return isMoO1Monster.get() ? 1 : 2; }
    @Override public void degradePlanet(StarSystem sys) {
        Colony col = sys.colony();
        if (col != null) {
            sys.empire().lastAttacker(this);
            col.destroy();  
        }
        if (!isMoO1Monster.get())
        	sys.planet().degradeToType(PlanetType.DEAD);
        float maxWaste = sys.planet().maxWaste();
        sys.planet().addWaste(maxWaste);
        sys.planet().removeExcessWaste();
        sys.abandoned(false);
    }
	@Override protected ShipDesign monsterDesign()	{
		if (isMoO1Monster.get())
			return designMoO1();
		else
			return designRotP();
	}
	private int hullHitPoints()		{ return moO1Level (5000, 1000, 500, 0.5f, 0.25f); }
	private ShipDesign designMoO1()	{
		ShipDesignLab lab = empire().shipLab();
		ShipDesign design = lab.newBlankDesign(4, hullHitPoints());
		design.mission	(ShipDesign.DESTROYER);

		List<ShipEngine> engines = lab.engines();
		design.engine	(engines.get(stackLevel(1, engines.size()-1)));

		List<ShipComputer> computers = lab.computers();
		int attackLevel = stackLevel(10);
		design.computer	(computers.get(min(attackLevel, computers.size()-1)));

		List<ShipArmor> armors = lab.armors();
		design.armor	(armors.get(stackLevel(0, armors.size()-1)));

		List<ShipShield> shields = lab.shields();
		design.shield	(shields.get(stackLevel(5, shields.size()-1)));

		List<ShipECM> ecms = lab.ecms();
		design.ecm		(ecms.get(stackLevel(2, ecms.size()-1)));

		int maneuver = max(2, stackLevel(2));
		design.maneuver(lab.maneuver(maneuver));
		design.monsterManeuver(maneuver);
		design.monsterAttackLevel(attackLevel);
		design.monsterBeamDefense(1);
		design.monsterEcmDefense(1);
		design.monsterInitiative(100);

		int wpnAll = max(1, stackLevel(10));
		for (int i=4; i>0; i--) {
			int count = wpnAll/i;
			if (count != 0) {
				// Crystal ray
				design.weapon(i-1, lab.crystalRay(), count);
				wpnAll -= count;
			}
		}
		design.special(0, lab.specialLightningShield());
		design.special(1, lab.specialAdvDamControl());		// Advanced Damage control
		design.special(2, lab.specialBlackHole());			// Black Hole Generator
		design.special(3, lab.specialResistStasis());		// Immune to Stasis
		return design;
	}
	private ShipDesign designRotP()	{
		ShipDesignLab lab = empire().shipLab();
		int hp = (int) (stackLevel(7000));
		ShipDesign design = lab.newBlankDesign(5, hp);

		design.mission	(ShipDesign.DESTROYER);

		List<ShipEngine> engines = lab.engines();
		design.engine	(engines.get(stackLevel(0, engines.size()-1)));

		List<ShipComputer> computers = lab.computers();
		int computerLevel = stackLevel(10);
		design.computer	(computers.get(min(computerLevel, computers.size()-1)));

		List<ShipArmor> armors = lab.armors();
		design.armor	(armors.get(stackLevel(0, armors.size()-1)));

		List<ShipShield> shields = lab.shields();
		design.shield	(shields.get(stackLevel(5, shields.size()-1)));

		List<ShipECM> ecms = lab.ecms();
		design.ecm		(ecms.get(stackLevel(0, ecms.size()-1)));

		int maneuver = max(2, stackLevel(2));
		design.maneuver(lab.maneuver(maneuver));
		design.monsterManeuver(maneuver);
		design.monsterAttackLevel(stackLevel(20)); // Normal = Always hit
		design.monsterBeamDefense(1);
		design.monsterEcmDefense(1);
		design.monsterInitiative(100);

		design.special(0, lab.specialZyroShield());
		design.special(1, lab.specialTeleporter());
		design.special(2, lab.specialCrystalPulsar());
		design.special(3, lab.specialCrystalNullifier());
		design.special(4, lab.specialResistStasis());
		return design;
	}
}
