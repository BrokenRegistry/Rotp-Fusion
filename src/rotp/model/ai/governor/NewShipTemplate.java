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

import static rotp.model.ships.ShipDesign.maxWeapons;

import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import rotp.model.ai.interfaces.ShipDesigner;
import rotp.model.ai.interfaces.ShipTemplate;
import rotp.model.empires.Empire;
import rotp.model.empires.EmpireView;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.IGameOptions;
import rotp.model.ships.ShipArmor;
import rotp.model.ships.ShipComputer;
import rotp.model.ships.ShipDesign;
import rotp.model.ships.ShipDesignLab;
import rotp.model.ships.ShipECM;
import rotp.model.ships.ShipManeuver;
import rotp.model.ships.ShipShield;
import rotp.model.ships.ShipSpecial;
import rotp.model.ships.ShipWeapon;
import rotp.model.ships.ShipWeaponMissileType;
import rotp.model.tech.Tech;
import rotp.model.tech.TechBiologicalWeapon;
import rotp.model.tech.TechShipInertial;
import rotp.model.tech.TechTree;

public class NewShipTemplate extends ShipTemplate { // For Player auto Design
	// BR: easiest way to share between methods
	private float totalSpace;
	//ail: looking at the stats of our enemies
	private boolean needRange;
	private boolean preventiveNeedRange;
	private boolean boostInertial;
	private boolean needDefenses;
	private boolean useSDModsSpace;
	private float enemyMissilePercentage;
	private float topSpeed;
	private float antiDote;
	private float avgECM;
	private float avgHP;
	private float bestSHD;
	private float longRangePct;
	private float totalCost;
	private float nonMissileTotal;
	private float missileTotal;

	// Design Requirement, size independent
	private boolean hasInertial;
	private float inertialDefense;
	private float moduleSpaceRatio;		
	private float shieldWeight;
	private float ecmWeight;	
	private float maneuverWeight;
	private float armorWeight; 
	private float specialsWeight; 
	private boolean sameSpeedAllowed;
	private boolean reinforcedArmorAllowed;

	private DesignType duty;
	private boolean hasCloaking = false;
	private boolean mustHaveBeam;

	@Override protected ShipDesign bestDesign(ShipDesigner ai, DesignType role)	{
		// create a blank design, one for each size. Add the current design as a 5th entry
		Empire emp = ai.empire();
		ShipDesign[] shipDesigns = new ShipDesign[4];
		for (int i = 0; i<4; i++) 
			shipDesigns[i] = newDesign(ai, role, i);

		SortedMap<Float, ShipDesign> designSorter = new TreeMap<>();
		float costLimit = emp.totalPlanetaryProduction() * max(0.125f, emp.shipMaintCostPerBC()) * 50 / emp.allColonizedSystems().size();
		float biggestShipWeaponSize = 0;
		float biggestBombSize = 0;
		float highestAttackLevel = 0;
		float highestHP = 0;
		float highestShield = 0;
		ShipDesign biggestWeaponDesign = null;
		for (int i = 0; i<4; i++) {
			ShipDesign design = shipDesigns[i];
			for (int j=0; j<maxWeapons(); j++) {
				if (design.weapon(j).groundAttacksOnly()) {
					if (design.weapon(j).space(design) > biggestBombSize)
						biggestBombSize = design.weapon(j).space(design);
				}
				else {
					if (design.weapon(j).space(design) > biggestShipWeaponSize) {
						biggestShipWeaponSize = design.weapon(j).space(design);
						biggestWeaponDesign = design;
					}
				}
			}
			if (design.attackLevel() > highestAttackLevel)
				highestAttackLevel = design.attackLevel();
			if (design.hits() > highestHP)
				highestHP = design.hits();
			if (design.shield().level() > highestShield)
				highestShield = design.shield().level();
		}
		float dmgPerCostLimit = 0;
		if (biggestWeaponDesign != null)
			dmgPerCostLimit = biggestWeaponDesign.firepowerAntiShip(highestShield) * costLimit / biggestWeaponDesign.cost();

		for (int i = 0; i<4; i++) {
			ShipDesign design = shipDesigns[i];
			float cost	= design.cost() * emp.shipDesignModCost(i);
			float score	= design.spaceUsed() / cost;
			float defScore = design.hits() / cost;
			float hitPct = (5 + highestAttackLevel - (design.beamDefense() + design.missileDefense()) / 2) / 10;
			hitPct = max(.05f, hitPct);
			hitPct = min(hitPct, 1.0f);
			float absorbPct = 1.0f;
			if (design.firepowerAntiShip(0) > 0) // more than 95% absorb will be normalized so score doesn't become infinity
				absorbPct = max(biggestWeaponDesign.firepowerAntiShip(design.shieldLevel()) / biggestWeaponDesign.firepowerAntiShip(0), 0.05f);
			float mitigation = Float.MAX_VALUE;
			if (hitPct > 0 && absorbPct > 0)	
				mitigation = (1 / hitPct) * (1 / absorbPct);
			defScore *= mitigation;
			score *= defScore;
			if (cost > costLimit)
				score /= cost / costLimit;
			float spaceWpnSize = 0;
			float bombWpnSize = 0;
			float twoRackSpace = 0;
			float restSpace = 0;
			for (int j=0; j<maxWeapons(); j++) {
				if (design.weapon(j).groundAttacksOnly()) {
					if (design.weapon(j).space(design) > bombWpnSize)
						bombWpnSize = design.weapon(j).space(design);
				}
				else {
					if (design.weapon(j).space(design) > spaceWpnSize)
						spaceWpnSize = design.weapon(j).space(design);
				}
				if (design.weapon(j).isMissileWeapon() && design.weapon(j).shots() <= 2)
					twoRackSpace +=  design.weapon(j).space(design) * design.wpnCount(j);
				else
					restSpace +=  design.weapon(j).space(design) * design.wpnCount(j);
			}

			boolean isMissileBoat = false;
			if (twoRackSpace > restSpace)
				isMissileBoat = true;

			if (dmgPerCostLimit < design.hits() && !isMissileBoat) // missile-ships will retreat after shooting twice anyways
				score *= 2;

			float specialsMod = 1;
			for (int s=0; s<design.maxSpecials(); s++) {
				//Inertial doesn't add to score because it's already taken into account for in the mitigation
				if (!design.special(s).isNone() && !design.special(s).isInertial())
					specialsMod*=1.26;
			}

			score *= specialsMod;
			float weaponSizeMod = 1.0f;
			if (role.isBomber() && biggestBombSize > 0)
				weaponSizeMod *= bombWpnSize / biggestBombSize;
			else if (biggestShipWeaponSize > 0)
				weaponSizeMod *= spaceWpnSize / biggestShipWeaponSize;
			if (ai.empire().shipDesignerAI().wantHybrid())
				if (biggestBombSize > 0)
					weaponSizeMod *= ai.empire().generalAI().defenseRatio() + (1 - ai.empire().generalAI().defenseRatio()) * bombWpnSize / biggestBombSize;
				else
					weaponSizeMod *= ai.empire().generalAI().defenseRatio(); 
			score *= weaponSizeMod;
			designSorter.put(score, design);
			// For bombers we want the smallest that has the best bomb because it's easiest to "dose"
			if (role.isBomber() && weaponSizeMod == 1)
				break;
		}
		// lastKey is design with greatest damage
		return designSorter.get(designSorter.lastKey()); 
	}
	@Override protected ShipDesign newDesign(ShipDesigner ai, DesignType role, int size)	{
		IGameOptions opts = options();
		duty = role;
		Empire empire	= ai.empire();
		TechTree tech	= empire.tech();
		useSDModsSpace	= opts.useShipDesignModsSpace();

		//ail: looking at the stats of our enemies
		analyzeEnemiesStats(ai);

		// Common design requirement for all sizes (weighting)
		designRequirements(ai, duty);

		// if we have a large ship, let's let the AI use more specials; it may have to differentiate designs more
		if (size >= ShipDesign.LARGE)
			specialsWeight += 1; 

		boolean missileShip	 = duty.isMissile();
		mustHaveBeam	 = duty.mustHaveBeam();
		boolean standardShip = duty.isStandard();
		// the sum of shield, ECM, maneuver and armor weights may be not exactly equal to modulesSpace
		// however, unless it isn't 1.0 or close to it of available space after engines and BC, it doesn't matter
		float weightsSum = ecmWeight + maneuverWeight + armorWeight + specialsWeight;
		if (standardShip && useSDModsSpace)
			shieldWeight = weightsSum/4;
			
		shieldWeight *= 1 - empire.generalAI().nebulaRatio();

		weightsSum += shieldWeight;
		if (weightsSum == 0)
			weightsSum = Float.MIN_VALUE; // To avoid /0

		ShipDesign d = ai.lab().newBlankDesign(size);
		// name it first so we can use name for reference in debugging
		// engines are always the priority in MOO1 mechanics
		ai.lab().nameDesign(d);
		setFastestEngine(ai, d);
		// battle computers are always the priority in MOO1 mechanics
		if (!duty.isDestroyer())
			setBestBattleComputer(ai, d); 

		totalSpace = d.availableSpace();
		float modulesSpace = totalSpace * moduleSpaceRatio;
		float weaponsSpace = totalSpace - modulesSpace;

		if (standardShip) {
			ShipWeapon wantedWeapon = setOptimalWeapon(ai, d, weaponsSpace, 4, needRange, true, false, false, needDefenses, topSpeed, avgECM, bestSHD, antiDote, false, avgHP, true); // dry run to see what we'd like
			if (wantedWeapon != null && wantedWeapon.isMissileWeapon() && wantedWeapon.shots() < 5) {
				missileShip = true;
				reinforcedArmorAllowed = false;
			}
		}
		else {
			ShipWeapon wantedWeapon;
			float space = totalSpace;
			int slotNum = 1;
			boolean ranged;
			boolean aShips;
			boolean noMiss;
			boolean missile;
			boolean no2Rack;
			float missSpeed = topSpeed;;
			float ecm = avgECM;
			float shd = bestSHD;
			boolean dwnSize = true;
			boolean dryRun = true;

			switch (duty) {
				case BOMBER:
					ranged	= false;
					aShips	= false;
					noMiss	= false;
					missile	= false;
					no2Rack	= needDefenses;
					wantedWeapon = setOptimalWeapon(ai, d, space, slotNum, ranged, aShips, noMiss, missile, no2Rack, missSpeed, ecm, shd, antiDote, dwnSize, avgHP, dryRun);
					break;
				case DESTROYER:
					ranged	= needRange;
					aShips	= true;
					noMiss	= false;
					missile	= false;
					no2Rack	= needDefenses;
					wantedWeapon = setOptimalWeapon(ai, d, space, slotNum, ranged, aShips, noMiss, missile, no2Rack, missSpeed, ecm, shd, antiDote, dwnSize, avgHP, dryRun);
					break;
				case BEAM:
					ranged	= preventiveNeedRange;
					aShips	= true;
					noMiss	= true;
					missile	= false;
					no2Rack	= true;
					wantedWeapon = setOptimalWeapon(ai, d, space, slotNum, ranged, aShips, noMiss, missile, no2Rack, missSpeed, ecm, shd, antiDote, dwnSize, avgHP, dryRun);
					break;
				case MISSILE:
					ranged	= preventiveNeedRange;
					aShips	= true;
					noMiss	= false;
					missile	= true;
					no2Rack	= false;
					wantedWeapon = setOptimalWeapon(ai, d, space, slotNum, ranged, aShips, noMiss, missile, no2Rack, missSpeed, ecm, shd, antiDote, dwnSize, avgHP, dryRun);
					break;
				case INTERCEPTOR:
				case HYBRID:
					ranged	= needRange;
					aShips	= true;
					noMiss	= false;
					missile	= false;
					no2Rack	= needDefenses;
					wantedWeapon = setOptimalWeapon(ai, d, space, slotNum, ranged, aShips, noMiss, missile, no2Rack, missSpeed, ecm, shd, antiDote, dwnSize, avgHP, dryRun);
					break;
				case FIGHTER:
				default:
					ranged	= needRange;
					aShips	= true;
					noMiss	= false;
					missile	= false;
					no2Rack	= needDefenses;
					wantedWeapon = setOptimalWeapon(ai, d, space, slotNum, ranged, aShips, noMiss, missile, no2Rack, missSpeed, ecm, shd, antiDote, dwnSize, avgHP, dryRun);
					break;
			}
			if (wantedWeapon != null) {
				weaponsSpace = max(weaponsSpace, wantedWeapon.space(d));
				modulesSpace = totalSpace - weaponsSpace;
			}
		}
		float spaceFactor	= modulesSpace / weightsSum;
		float shieldSpace	= spaceFactor * shieldWeight;
		float ecmSpace		= spaceFactor * ecmWeight;
		float maneuverSpace	= spaceFactor * maneuverWeight;
		float armorSpace	= spaceFactor * armorWeight;
		float specialsSpace	= spaceFactor * specialsWeight;

		//System.out.print("\n"+ai.empire().name()+" "+d.name()+" shieldSpace: "+shieldSpace+" nebulaRatio: "+ai.empire().generalAI().nebulaRatio());
		// ail: removed leftovers. The impact isn't minor at all. Even at a weight of 0 they usually crammed in the max-level ECM, leaving much less space for weapons
		// branches made in the ugly way for clarity
		// specials will be skipped for smaller hulls in the early game, bringing a bit more allowance to the second fitting
		//ArrayList<ShipSpecial> specials;
		SortedMap<Float, ShipSpecial> specials;
		//System.out.print("\n"+ai.empire().name()+" "+d.name()+" avgSHD: "+avgSHD+" avgECM: "+avgECM);

		switch (duty) {
			case BEAM:
				specials = buildSpecialsList(d, ai, enemyMissilePercentage, false, preventiveNeedRange, true, longRangePct, false);
				break;
			case BOMBER:
				specials = buildSpecialsList(d, ai, enemyMissilePercentage, true, false, boostInertial, longRangePct, false);
				break;
			case DESTROYER:
				specials = buildSpecialsList(d, ai, enemyMissilePercentage, false, needRange, boostInertial, longRangePct, missileShip);
				break;
			case HYBRID:
				specials = buildSpecialsList(d, ai, enemyMissilePercentage, false, preventiveNeedRange, boostInertial, longRangePct, false);
				break;
			case MISSILE:
				specials = buildSpecialsList(d, ai, enemyMissilePercentage, false, false, false, longRangePct, true);
				break;
			case INTERCEPTOR:
				specials = buildSpecialsList(d, ai, enemyMissilePercentage, false, preventiveNeedRange, true, longRangePct, false);
				break;
			case FIGHTER:
			default:
				specials = buildSpecialsList(d, ai, enemyMissilePercentage, false, needRange, boostInertial, longRangePct, missileShip);
				break;
		}

		boolean haveBHG = tech.hasBlackHoleTech();
		hasCloaking = tech.hasCloakingTech();
		float spaceOfBlackHoleCloakCombo = 0;
		if (hasCloaking)
			spaceOfBlackHoleCloakCombo = ai.lab().specialCloak().space(d);
		if (haveBHG) {
			spaceOfBlackHoleCloakCombo += ai.lab().specialBlackHole().space(d);
			if (spaceOfBlackHoleCloakCombo >= 0.5f * d.totalSpace())
				haveBHG = false;
		}

		//when we can combine cloaking with either BHG or Stasis, we allow a lot more space for specials
		if (hasCloaking && haveBHG)
			specialsSpace = max(specialsSpace, totalSpace * 0.5f);
		
		switch (duty) {
			case BOMBER:
				setFittingSpecial(ai, d, specialsSpace, specials, true);
				setFittingArmor(ai, d, armorSpace, reinforcedArmorAllowed);
				setFittingManeuver(ai, d, maneuverSpace, sameSpeedAllowed);
				setFittingECM(ai, d, ecmSpace);
				break;
			case DESTROYER:
				setFittingArmor(ai, d, armorSpace, reinforcedArmorAllowed);
				setFittingManeuver(ai, d, maneuverSpace, sameSpeedAllowed);
				break;
			case BEAM:
			case HYBRID:
			case MISSILE:
			case INTERCEPTOR:
			case FIGHTER:
			default:
				setFittingSpecial(ai, d, specialsSpace, specials, false);
				setFittingArmor(ai, d, armorSpace, reinforcedArmorAllowed);
				if (!missileShip) {
					setFittingECM(ai, d, ecmSpace);
					setFittingShields(ai, d, shieldSpace);
				}
				setFittingManeuver(ai, d, maneuverSpace, sameSpeedAllowed);
				break;
		}
		
		//if we couldn't determine other's HP, we take our own after putting on armor
		if (avgHP == 0)
			avgHP = d.hits();
		
		for (int j=0;j<d.maxSpecials();j++) {
			if (d.special(j).beamRangeBonus() > 0)
				needRange = false;
			if (d.special(j).allowsCloaking())
				needRange = false;
		}

		float hybridBombRatio = 0;
		switch (duty) {
			case BOMBER:
				hybridBombRatio = 0.8f;
				break;
			case DESTROYER:
				hybridBombRatio = 0.1f;
			case BEAM:
				hybridBombRatio = 0.1f;
				break;
			case HYBRID:
				hybridBombRatio = 1f/3f;
				break;
			case MISSILE:
				hybridBombRatio = 0f;
				break;
			case INTERCEPTOR:
				hybridBombRatio = 0.1f;
				break;
			case FIGHTER:
			default:
				if (ai.wantHybrid())
					hybridBombRatio = 1 - empire.generalAI().defenseRatio();
				break;
		}

		float minBombSpace = 0;
		for(ShipWeapon wpn : ai.lab().weapons())
			if (wpn.groundAttacksOnly() && wpn.space(d) > minBombSpace)
					minBombSpace = wpn.space(d);
		// what's left will be used on non-bombs for bombers, second best weapon for destroyers
		// repeat calls of setOptimalShipCombatWeapon() will result in a weapon from another category (beam, missile, streaming) than already installed
		// fighters will have a single best weapon over all four slots
		ShipWeapon bestNonBomb = null;

		// For a better readability
		float space;
		int slotNum;
		boolean ranged;
		boolean aShips;
		boolean noMiss;
		boolean missile;
		boolean no2Rack;
		float missSpeed = topSpeed;;
		float ecm;
		float shd;
		boolean dwnSize;
		boolean dryRun = false;
		switch (duty) {
			case BOMBER:
				space	= d.availableSpace();
				slotNum	= 3;
				ranged	= false;
				aShips	= false;
				noMiss	= false;
				missile	= false;
				no2Rack	= needDefenses;
				ecm		= avgECM;
				shd		= bestSHD;
				dwnSize	= false;
				setOptimalWeapon(ai, d, space, slotNum, ranged, aShips, noMiss, missile, no2Rack, missSpeed, ecm, shd, antiDote, dwnSize, avgHP, dryRun);
				// even though bombs should use all space, this is run in case of it running into the max-required-bombs per design-limit
				space	= d.availableSpace();
				slotNum	= 4;
				ranged		= needRange;
				aShips	= true;
				noMiss	= false;
				missile	= false;
				no2Rack	= needDefenses;
				ecm		= avgECM;
				shd		= bestSHD;
				dwnSize	= false;
				bestNonBomb = setOptimalWeapon(ai, d, space, slotNum, ranged, aShips, noMiss, missile, no2Rack, missSpeed, ecm, shd, antiDote, dwnSize, avgHP, dryRun);
				break;
			case DESTROYER:
				space	= d.availableSpace();
				slotNum	= 4;
				ranged	= needRange;
				aShips	= true;
				noMiss	= false;
				missile	= false;
				no2Rack	= needDefenses;
				ecm		= avgECM;
				shd		= bestSHD;
				dwnSize	= true;
				bestNonBomb = setOptimalWeapon(ai, d, space, slotNum, ranged, aShips, noMiss, missile, no2Rack, missSpeed, ecm, shd, antiDote, dwnSize, avgHP, dryRun);
				break;
			case BEAM:
				space	= d.availableSpace() * hybridBombRatio;
				slotNum	= 1;
				ranged	= false;
				aShips	= false;
				noMiss	= true;
				missile	= false;
				no2Rack	= true;
				ecm		= avgECM;
				shd		= bestSHD;
				dwnSize	= false;
				setOptimalWeapon(ai, d, space, slotNum, ranged, aShips, noMiss, missile, no2Rack, missSpeed, ecm, shd, antiDote, dwnSize, avgHP, dryRun);
				space	= d.availableSpace();
				slotNum	= 4;
				ranged	= preventiveNeedRange;
				aShips	= true;
				noMiss	= true;
				missile	= false;
				no2Rack	= true;
				ecm		= avgECM;
				shd		= bestSHD;
				dwnSize	= false;
				bestNonBomb = setOptimalWeapon(ai, d, space, slotNum, ranged, aShips, noMiss, missile, no2Rack, missSpeed, ecm, shd, antiDote, dwnSize, avgHP, dryRun);
				break;
			case MISSILE:
				space	= d.availableSpace();
				slotNum	= 4;
				ranged	= preventiveNeedRange;
				aShips	= true;
				noMiss	= false;
				missile	= true;
				no2Rack	= false;
				ecm		= avgECM;
				shd		= bestSHD;
				dwnSize	= true;
				bestNonBomb = setOptimalWeapon(ai, d, space, slotNum, ranged, aShips, noMiss, missile, no2Rack, missSpeed, ecm, shd, antiDote, dwnSize, avgHP, dryRun);
				break;
			case INTERCEPTOR:
			case HYBRID:
				space	= max(minBombSpace, d.availableSpace() * hybridBombRatio);
				slotNum	= 1;
				ranged	= false;
				aShips	= false;
				noMiss	= false;
				missile	= false;
				no2Rack	= needDefenses;
				ecm		= avgECM;
				shd		= bestSHD;
				dwnSize	= false;
				setOptimalWeapon(ai, d, space, slotNum, ranged, aShips, noMiss, missile, no2Rack, missSpeed, ecm, shd, antiDote, dwnSize, avgHP, dryRun);
				space	= d.availableSpace() / 2;
				slotNum	= 2;
				ranged	= needRange;
				aShips	= true;
				noMiss	= true;
				missile	= false;
				no2Rack	= needDefenses;
				ecm		= avgECM;
				shd		= bestSHD;
				dwnSize	= false;
				bestNonBomb = setOptimalWeapon(ai, d, space, slotNum, ranged, aShips, noMiss, missile, no2Rack, missSpeed, ecm, shd, antiDote, dwnSize, avgHP, dryRun);
				space	= d.availableSpace();
				slotNum	= 2;
				ranged	= needRange;
				aShips	= true;
				noMiss	= false;
				missile	= true;
				no2Rack	= needDefenses;
				ecm		= avgECM;
				shd		= bestSHD;
				dwnSize	= false;
				setOptimalWeapon(ai, d, space, slotNum, ranged, aShips, noMiss, missile, no2Rack, missSpeed, ecm, shd, antiDote, dwnSize, avgHP, dryRun);
				break;
			case FIGHTER:
			default:
				space	= max(minBombSpace, d.availableSpace() * hybridBombRatio);
				slotNum	= 1;
				ranged	= false;
				aShips	= false;
				noMiss	= false;
				missile	= false;
				no2Rack	= needDefenses;
				ecm		= avgECM;
				shd		= bestSHD;
				dwnSize	= false;
				setOptimalWeapon(ai, d, space, slotNum, ranged, aShips, noMiss, missile, no2Rack, missSpeed, ecm, shd, antiDote, dwnSize, avgHP, dryRun);
				space	= d.availableSpace();
				slotNum	= 4;
				ranged	= needRange;
				aShips	= true;
				noMiss	= false;
				missile	= false;
				no2Rack	= needDefenses;
				ecm		= avgECM;
				shd		= bestSHD;
				dwnSize	= false;
				bestNonBomb = setOptimalWeapon(ai, d, space, slotNum, ranged, aShips, noMiss, missile, no2Rack, missSpeed, ecm, shd, antiDote, dwnSize, avgHP, dryRun);
				break;
		}

		//Since destroyer is always tiny and we want to make sure we have a weapon, the computer is added afterwards
		if (duty.isDestroyer())
			setBestBattleComputer(ai, d); 
		ai.lab().iconifyDesign(d);
		for (int i = 0; i <= 2; ++i) {
			if (d.special(i) != null)
				d.special(i, ai.lab().noSpecial());
		}
		//if removing beam-bonus gave us some space back, we re-add something else
		boolean skipBeamBonus = false;
		if ((bestNonBomb != null && !bestNonBomb.isBeamWeapon()) || bestNonBomb == null)
			skipBeamBonus = true;
		setFittingSpecial(ai, d, d.availableSpace(), specials, skipBeamBonus);
		//if we still have space cram in whatever weapon still fits
		switch(duty) {
			case BOMBER:
				setOptimalWeapon(ai, d, d.availableSpace(), 1, false, false, false, false, needDefenses, topSpeed, avgECM, bestSHD, antiDote, true, avgHP, false);
				break;
			default:
				setOptimalWeapon(ai, d, d.availableSpace(), 4, needRange, true, false, false, needDefenses, topSpeed, avgECM, bestSHD, antiDote, true, avgHP, false);
		}
		//All slots full - try to add additional stuff in the already used slots (if we have big weapons in slots 2-4 and small ones in 1, we might fit more into slot 1)
		for (int i=0; i<maxWeapons(); i++) {
			ShipWeapon weapon = d.weapon(i);
			if (weapon.noWeapon())
				continue;
			while(d.availableSpace() >= weapon.space(d))
				d.wpnCount(i, d.wpnCount(i) + 1);
		}
		return d;
	}
	////////////////////////////////////////////////////
	private void designRequirements(ShipDesigner ai, DesignType role)	{
		Empire emp	= ai.empire();
		hasInertial = false;
		TechShipInertial topShipInertialTech = ai.empire().tech().topShipInertialTech();
		inertialDefense = 1;
		if (topShipInertialTech != null) {
			inertialDefense = topShipInertialTech.defenseBonus;
			hasInertial = true;
		}
		// initial separation of the free space left onto weapons and non-weapons/specials
		moduleSpaceRatio = emp.shipDesignModModuleSpace();		

		// Initial weighting of what isn't weapons
		shieldWeight	= 4;
		ecmWeight		= 3;	
		maneuverWeight	= 2;
		armorWeight		= 3;
		reinforcedArmorAllowed	= emp.shipDesignModReinforcedArmor(); 
		sameSpeedAllowed		= emp.shipDesignModSpeedMatching(); 
		specialsWeight			= emp.shipDesignModSpecialsWeight();

		ecmWeight = Math.round(ecmWeight * 2 * enemyMissilePercentage);	
		switch (role) {
			case BEAM:
				if (useSDModsSpace) {
					shieldWeight	= emp.shipDesignModShieldWeightFB();
					ecmWeight		= emp.shipDesignModEcmWeightFD();
					maneuverWeight	= emp.shipDesignModManeuverWeightF();
					armorWeight		= emp.shipDesignModArmorWeightFB();
				}
				else
					maneuverWeight += 1;
				break;
			case BOMBER:
				if (useSDModsSpace) {
					shieldWeight	= emp.shipDesignModShieldWeightFB();
					ecmWeight		= emp.shipDesignModEcmWeightB();
					maneuverWeight	= emp.shipDesignModManeuverWeightBD();
					armorWeight		= emp.shipDesignModArmorWeightFB();
				}
				else {
					shieldWeight = 0;
				}
				break;
			case DESTROYER:
				if (useSDModsSpace) {
					shieldWeight	= emp.shipDesignModShieldWeightD();
					ecmWeight		= emp.shipDesignModEcmWeightFD();
					maneuverWeight	= emp.shipDesignModManeuverWeightBD();
					armorWeight		= emp.shipDesignModArmorWeightD();
				}
				break;
			case FIGHTER:
				if (useSDModsSpace) {
					shieldWeight	= emp.shipDesignModShieldWeightFB();
					ecmWeight		= emp.shipDesignModEcmWeightFD();
					maneuverWeight	= emp.shipDesignModManeuverWeightBD();
					armorWeight		= emp.shipDesignModArmorWeightFB();
				}
				break;
			case HYBRID:
				if (useSDModsSpace) {
					shieldWeight	= emp.shipDesignModShieldWeightFB();
					ecmWeight		= emp.shipDesignModEcmWeightFD();
					maneuverWeight	= emp.shipDesignModManeuverWeightBD();
					armorWeight		= emp.shipDesignModArmorWeightFB();
				}
				if (hasInertial) {
					float factor = sqrt(inertialDefense);
					shieldWeight	/= factor;
					ecmWeight		/= factor;
					maneuverWeight	*= factor;
					armorWeight		/= factor;
				}
				break;
			case MISSILE:
				if (useSDModsSpace) {
					shieldWeight	= emp.shipDesignModShieldWeightFB();
					ecmWeight		= emp.shipDesignModEcmWeightFD();
					maneuverWeight	= emp.shipDesignModManeuverWeightBD();
					armorWeight		= emp.shipDesignModArmorWeightFB();
				}
				else {
					shieldWeight	= 0;
					ecmWeight		= 0;
					maneuverWeight	= 0;
					armorWeight		= 0;
					specialsWeight	= 0;
					reinforcedArmorAllowed = false;
				}
				break;
			case INTERCEPTOR:
				if (useSDModsSpace) {
					shieldWeight	= emp.shipDesignModShieldWeightFB();
					ecmWeight		= emp.shipDesignModEcmWeightFD();
					maneuverWeight	= emp.shipDesignModManeuverWeightBD();
					armorWeight		= emp.shipDesignModArmorWeightFB();
				}
				if (hasInertial) {
					float factor = sqrt(inertialDefense);
					shieldWeight	/= factor;
					ecmWeight		/= factor;
					maneuverWeight	*= factor;
					armorWeight		/= factor;
				}
				break;
			default:
				break;
		}
	}
	private void analyzeEnemiesStats(ShipDesigner ai)	{
		enemyMissilePercentage = 0.0f;
		needRange = false;
		preventiveNeedRange = false;
		boostInertial = false;
		needDefenses = false;
		topSpeed = 0;
		antiDote = 0;
		avgECM = 0;
		avgHP = 0;
		bestSHD = 0;
		longRangePct = 0;
		totalCost = 0;
		nonMissileTotal = 0.0f;
		missileTotal = 0.0f;

		for(EmpireView ev : ai.empire().contacts()) {
			// Investigate opponent technology
			if (ev.spies().tech().antidoteLevel() > antiDote)
				antiDote = ev.spies().tech().antidoteLevel();
			if (ev.spies().tech().bestMissileBase().scatterPack() != null || ev.spies().tech().topPlanetaryShieldTech() != null)
			   needDefenses = true; 
			if (ev.spies().tech().hasRepulsorTech())
				preventiveNeedRange = true;
			// Investigate opponent Fleet
			for(ShipDesign enemyDesign : ev.designsUncut()) {
				if (enemyDesign.scrapped())
					continue;
				boolean isLongRange = false;
				if (enemyDesign.repulsorRange() > 0)
					needRange = true;
				for (int j=0;j<enemyDesign.maxSpecials();j++) {
					if (enemyDesign.special(j).createsBlackHole())
						boostInertial = true;
					if (enemyDesign.special(j).beamRangeBonus() > 0)
						isLongRange = true;
					if (enemyDesign.special(j).allowsCloaking())
						isLongRange = true;
				}
				for (int i=0; i<maxWeapons(); i++) {
					ShipWeapon weapon = enemyDesign.weapon(i);
					if (weapon == null)
						continue;
					if (weapon.range() > 1)
						isLongRange = true;
					if (weapon.isMissileWeapon())
						missileTotal += weapon.cost() * enemyDesign.wpnCount(i);
					else
						nonMissileTotal += weapon.cost() * enemyDesign.wpnCount(i);
				}
				if (enemyDesign.combatSpeed() > topSpeed)
					topSpeed = enemyDesign.combatSpeed();
				float count = ev.shipDesignCount(enemyDesign.id());
				avgECM += enemyDesign.ecm().level() * enemyDesign.cost() * count;
				avgHP += enemyDesign.hits() * enemyDesign.cost() * count;
				if (enemyDesign.shieldLevel() > bestSHD)
					bestSHD = enemyDesign.shieldLevel();
				if (isLongRange)
					longRangePct += enemyDesign.cost() * count;
				totalCost += enemyDesign.cost() * count;
			}
			float missileBaseCostPerBC = ev.missileBaseCostPerBC();
			float shipMaintCostPerBC = ev.shipMaintCostPerBC();
			if (shipMaintCostPerBC + missileBaseCostPerBC > 0)
				enemyMissilePercentage = max((enemyMissilePercentage * shipMaintCostPerBC + missileBaseCostPerBC) / (shipMaintCostPerBC + missileBaseCostPerBC), enemyMissilePercentage);
		}
		//if opponent has repulsors we can't fire missiles point blank so they must be faster to compensate
		if (needRange)
			topSpeed += 0.5;
		//when ships move diagonally they can outrun same-speed missiles so missiles must be faster
		topSpeed *= sqrt(2);
		//missile doesn't need to get to center of target, only within 0.7 range of it. Since it has 2 turns time we can subtract 0.35 from the required speed
		topSpeed -= 0.35;
		if (totalCost > 0) {
			longRangePct /= totalCost;
			avgECM /= totalCost;
			avgHP /= totalCost;
		}
		if (missileTotal+nonMissileTotal > 0)
			enemyMissilePercentage = max(enemyMissilePercentage, missileTotal / (missileTotal + nonMissileTotal));
	}

	////////////////////////////////////////////////////

	// ********** MODULE FITTING FUNCTIONS ********** //

	private void setFastestEngine(ShipDesigner ai, ShipDesign d)	{ d.engine(ai.lab().fastestEngine()); }
	private void setBestBattleComputer(ShipDesigner ai, ShipDesign d)	{
		List<ShipComputer> comps = ai.lab().computers();
		for (int i=comps.size()-1; i >=0; i--) {
			d.computer(comps.get(i));
			if (d.availableSpace() >= 0)
				return;
		}
	}
	private float setFittingManeuver(ShipDesigner ai, ShipDesign d, float spaceAllowed, boolean sameSpeedAllowed)	{
		float initialSpace = d.availableSpace();

		for (ShipManeuver manv : ai.lab().maneuvers()) {
			ShipManeuver prevManv = d.maneuver();
			int prevSpeed = d.combatSpeed();
			d.maneuver(manv);

			if ((initialSpace - d.availableSpace()) > spaceAllowed)
				d.maneuver(prevManv);
			else if ((d.combatSpeed() == prevSpeed) && (!sameSpeedAllowed))
				d.maneuver(prevManv);
		}
		return (spaceAllowed - (initialSpace - d.availableSpace()));
	}
	private float setFittingArmor(ShipDesigner ai, ShipDesign d, float spaceAllowed, boolean reinforcedArmorAllowed)	{
		float initialSpace = d.availableSpace();

		boolean foundIt = false;
		List<ShipArmor> armors = ai.lab().armors();
		for (int i=armors.size()-1; (i >=0) && (!foundIt); i--) {
			ShipArmor arm = armors.get(i);

			// as we go backwards from the bestest armor to the worsest,
			// a better armor should always be chosen before the reinforced one if it exists due to smaller size
			// some races will just never use reinforced armor, I believe, the ones that prefer smaller ships
			if (!arm.reinforced() || (reinforcedArmorAllowed)) {
				d.armor(armors.get(i));
				if ((initialSpace - d.availableSpace()) <= spaceAllowed)
					foundIt = true;
			}
		}
		return (spaceAllowed - (initialSpace - d.availableSpace()));
	}
	private float setFittingShields(ShipDesigner ai, ShipDesign d, float spaceAllowed)	{
		float initialSpace = d.availableSpace();

		boolean foundIt = false;
		List<ShipShield> shields = ai.lab().shields();
		for (int i=shields.size()-1; (i >= 0) && (!foundIt); i--) {
			d.shield(shields.get(i));
			//System.out.print("\n"+ai.empire().name()+" "+d.name()+" "+d.name()+" shieldspace: "+spaceAllowed+" "+shields.get(i).name()+" "+shields.get(i).space(d));
			if ((initialSpace - d.availableSpace()) <= spaceAllowed)
				foundIt = true;
		}
		return (spaceAllowed - (initialSpace - d.availableSpace()));
	}
	private float setFittingECM(ShipDesigner ai, ShipDesign d, float spaceAllowed)	{
		float initialSpace = d.availableSpace();

		boolean foundIt = false;
		List<ShipECM> ecm = ai.lab().ecms();
		for (int i=ecm.size()-1; (i >=0) && (!foundIt); i--) {
			d.ecm(ecm.get(i));
			if ((initialSpace - d.availableSpace()) <= spaceAllowed)
				foundIt = true;
		}
		return (spaceAllowed - (initialSpace - d.availableSpace()));
	}
////////////////////////////////////////////////////


// ********** SPECIALS SELECTION AND FITTING FUNCTIONS ********** //

	private SortedMap<Float, ShipSpecial> buildSpecialsList(ShipDesign d, ShipDesigner ai,
			float antiMissile, boolean bomber, boolean needRange, boolean boostInertial,
			float longRangePct, boolean missileShip)	{
		SortedMap<Float, ShipSpecial> specials = new TreeMap<>(Collections.reverseOrder());
		List<ShipSpecial> allSpecials = ai.lab().specials();
		Empire emp = ai.empire();

		int designsWithStasisField = 0;
		for (int slot=0;slot<ShipDesignLab.MAX_DESIGNS;slot++) {
			ShipDesign ourDesign = ai.lab().design(slot);
			for (int j=0; j<ourDesign.maxSpecials(); j++) {
				ShipSpecial spec = ourDesign.special(j);
				if (!spec.isNone() && spec.tech().isType(Tech.STASIS_FIELD) == true)
					designsWithStasisField++;
			}
		}

		for (ShipSpecial spec: allSpecials) {
			if (spec.isNone() || spec.isColonySpecial() || spec.isFuelRange())
				continue;
			Tech tech = spec.tech();
			float currentScore = 0;

			//new approach: The main idea behind our bombers is that they are cheap and that they carry lots of bombs, so no specials beside of cloaking-device help us outside of combat
			if (bomber) {
				switch (tech.techType) {
				case Tech.CLOAKING:
					//ail: we always want it. It's the best!
					currentScore = emp.shipDesignModPrefCloak()? 500 : 0;
					break;
				case Tech.MISSILE_SHIELD:
					if (tech.typeSeq == 0)
						currentScore = 40;
					else if (tech.typeSeq == 1)
						currentScore = 75;
					else if (tech.typeSeq == 2)
						currentScore = 100;
					float missileLevel = 0;
					if (ai.empire().tech().topBaseMissileTech() != null)
						missileLevel = max(missileLevel, ai.empire().tech().topBaseMissileTech().level());
					if (ai.empire().tech().topBaseScatterPackTech() != null)
						missileLevel = max(missileLevel, ai.empire().tech().topBaseScatterPackTech().level());
					currentScore -= missileLevel;
					currentScore = max(0, currentScore);
					currentScore *= 10;
					currentScore *= antiMissile;
					if (!emp.shipDesignModPrefMissShield())
						currentScore = 0;
					break;
				}
				continue;
			}

			if (missileShip && !(tech.isType(Tech.CLOAKING) || tech.isType(Tech.SCANNER) || tech.isType(Tech.SHIP_INERTIAL) || tech.isType(Tech.TELEPORTER)))
				continue;

			switch (tech.techType) {
			case Tech.AUTOMATED_REPAIR:
				if (tech.typeSeq == 0)
					currentScore = 75;
				else if (tech.typeSeq == 1)
					currentScore = 125;
				currentScore += tech.level - ai.empire().tech().avgTechLevel(); //loses usefulness with more miniaturization
				if (d.size() < 2)
					currentScore = 0;
				else if (d.size() > 2)
					currentScore *= 6;
				if (!emp.shipDesignModPrefRepair())
					currentScore = 0;
				break;
			case Tech.SCANNER:
				currentScore = 50;
				if (mustHaveBeam && d.size() > 2)
					currentScore *= 4;
				break;
			case Tech.BLACK_HOLE:
				currentScore = 500;
				currentScore *= (5-d.size());
				if (needRange && !hasCloaking)
					currentScore /= 5;
				break;
			case Tech.BEAM_FOCUS:
				if (spec.beamRangeBonus() > 0) {
					currentScore = 100;
					currentScore *= (d.totalSpace() - spec.space(d)) / d.totalSpace();
					if (mustHaveBeam || (needRange && !hasCloaking))
						currentScore *= 5;
				}
				else if (spec.beamShieldMod() < 1) {
					currentScore = 200;
					currentScore *= (d.totalSpace() - spec.space(d)) / d.totalSpace();
				}
				if (!emp.shipDesignModPrefBeamFocus())
					currentScore = 0;
				break;
			case Tech.CLOAKING:
				//ail: we always want it. It's the best!
				currentScore = 5000;
				if (emp.shipDesignModPrefCloak())
					currentScore *= 2;
				else
					currentScore = 0;
				break;
			case Tech.DISPLACEMENT:
				currentScore = 50;
				break;
			case Tech.ENERGY_PULSAR:
				currentScore = 50 * (tech.typeSeq + 1);
				currentScore *= (5-d.size());
				if (needRange)
					currentScore /= 5;
				if (!emp.shipDesignModPrefPulsars())
					currentScore = 0;
				break;
			case Tech.MISSILE_SHIELD:
				if (tech.typeSeq == 0)
					currentScore = 40;
				else if (tech.typeSeq == 1)
					currentScore = 75;
				else if (tech.typeSeq == 2)
					currentScore = 100;
				float missileLevel = 0;
				if (ai.empire().tech().topBaseMissileTech() != null)
					missileLevel = max(missileLevel, ai.empire().tech().topBaseMissileTech().level());
				if (ai.empire().tech().topBaseScatterPackTech() != null)
					missileLevel = max(missileLevel, ai.empire().tech().topBaseScatterPackTech().level());
				currentScore -= missileLevel;
				currentScore = Math.max(0, currentScore);
				currentScore *= 5;
				currentScore *= antiMissile;
				if (!emp.shipDesignModPrefMissShield())
					currentScore = 0;
				break;
			case Tech.REPULSOR:
				currentScore = 250 * (1 - longRangePct);
				if (needRange && !hasCloaking)
					currentScore *= 2;
				if (!emp.shipDesignModPrefRepulsor())
					currentScore = 0;
				break;
			case Tech.SHIP_INERTIAL:
				currentScore = 100 * (tech.typeSeq + 1);
				if (boostInertial)
					currentScore *= 2;
				if (!emp.shipDesignModPrefInertial())
					currentScore = 0;
				break;
			case Tech.SHIP_NULLIFIER:
				currentScore = 100;
				if (needRange)
					currentScore *= 1.5;
				if (spec.tech().isTechNullifier()) {
					if (!emp.shipDesignModPrefInertial())
						currentScore = 0;
				}
				else if (spec.tech().isWarpDissipator())
					if (!emp.shipDesignModPrefWarpDissip())
						currentScore = 0;
				break;
			case Tech.STASIS_FIELD:
				currentScore = 500;
				if (needRange && !hasCloaking || designsWithStasisField > 1)
					currentScore /= 10;
				if (!emp.shipDesignModPrefStasis())
					currentScore = 0;
				break;
			case Tech.STREAM_PROJECTOR:
				currentScore = 100 * (tech.typeSeq + 1);
				currentScore *= (5-d.size());
				if (needRange)
					currentScore *= 2;
				if (!emp.shipDesignModPrefStreamProj())
					currentScore = 0;
				break;
			case Tech.SUBSPACE_INTERDICTOR:
				currentScore = 200 * (1 - ai.empire().generalAI().defenseRatio());
				break;
			case Tech.TELEPORTER:
				currentScore = 100 * ai.empire().generalAI().defenseRatio();
				break;
			}
			//ail: removed division by size otherwise bigger and better stuff will never be used because other stuff just miniaturizes more
			//currentScore /= spec.space(d);
			//if we put stuff with 0 score, we end up with tinies and auto-repair
			if (currentScore > 0)
				specials.put(currentScore, spec);
		}
		return specials; 
	}
	private float setFittingSpecial(ShipDesigner ai, ShipDesign d, float spaceAllowed, SortedMap<Float, ShipSpecial> specials, boolean skipBeamBonus)	{
		int nextSlot = d.nextEmptySpecialSlot();
		if (nextSlot < 0)
			return spaceAllowed;
		if (specials.isEmpty())
			return spaceAllowed;

		float remainingSpace = spaceAllowed; 

		boolean alreadyInertial = false;
		boolean alreadyAutoRepair = false;
		for(ShipSpecial spec : specials.values()) {
			if (spec.isNone())
				continue;
			if ((spec.beamRangeBonus() > 0 || spec.beamShieldMod() < 1) && skipBeamBonus)
				continue;
			if (spec.isInertial() && alreadyInertial)
				continue;
			if (spec.shipRepairPct()>0 && alreadyAutoRepair)
				continue;
			if (spec.space(d) <= remainingSpace) {
				d.special(nextSlot,spec);
				nextSlot++;
				remainingSpace -= spec.space(d);
				if (spec.isInertial())
					alreadyInertial = true;
				if (spec.shipRepairPct()>0)
					alreadyAutoRepair = true;
				if (nextSlot > 2)
					break;
			}
		}
		return remainingSpace;
	}

// ********* FUNCTIONS SETTING ANTI-SHIP AND ANTI-PLANET WEAPONS ********** //

	private ShipWeapon setOptimalWeapon(ShipDesigner ai, ShipDesign d, float spaceAllowed, int numSlotsToUse, boolean mustBeRanged, boolean mustTargetShips, boolean prohibitMissiles, boolean mustBeMissiles, boolean prohibitTwoRackMissiles, float missileSpeedMinimum, float avgECM, float avgSHD, float antiDote, boolean downSize, float avgHP, boolean dryRun)	{
		List<ShipWeapon> allWeapons = ai.lab().weapons();
		ShipWeapon bestWeapon = null;
		float bestScore = 0.0f;
		float shield = avgSHD;
		if (!mustTargetShips)
			shield = ai.empire().bestEnemyPlanetaryShieldLevel() + ai.empire().bestEnemyShieldLevel();
		float startingShield = shield;
		spaceAllowed = min(spaceAllowed, d.availableSpace());
		while(bestWeapon == null) {
			for (ShipWeapon wpn: allWeapons) {
				if (wpn.canAttackShips() && mustTargetShips || !mustTargetShips) {
					//We don't want missiles: Can be outrun, can run out and strong counters exist
					if (wpn.space(d) > spaceAllowed && downSize)
						continue;
					if (wpn.isMissileWeapon() && prohibitMissiles)
						continue;
					if (!wpn.isMissileWeapon() && mustBeMissiles)
						continue;
					if (wpn.range() < 2 && mustBeRanged)
						continue;
					if (!mustTargetShips && !wpn.groundAttacksOnly())
						continue;
					float missileDamageMod = 1.0f;
					if (wpn.isMissileWeapon()) {
						ShipWeaponMissileType swm = (ShipWeaponMissileType)wpn;
						//System.out.print("\n"+ai.empire().name()+" "+d.name()+" wpn: "+wpn.name()+" speed: "+swm.speed()+" / "+missileSpeedMinimum);
						if (!mustBeMissiles && swm.speed() <= missileSpeedMinimum)
							continue;
						if (prohibitTwoRackMissiles && swm.shots() < 5)
							continue;
						avgECM -= swm.computerLevel();
						missileDamageMod = max(0.0f, 1.0f - 0.1f * avgECM);
						missileDamageMod *= swm.shots() / 5.0f;
					}
					float currentScore = wpn.firepower(shield) * missileDamageMod / wpn.space(d);
					float overKillMod = 1.0f;
					float expectedDamagePerShot = max(0,(wpn.minDamage() + wpn.maxDamage()) / 2.0f - shield);
					if (expectedDamagePerShot > avgHP && mustTargetShips)
						overKillMod = avgHP / expectedDamagePerShot;
					currentScore *= overKillMod;

					if (wpn.isBioWeapon() && ai.empire().shipDesignModBioWeapons())
						currentScore = bioWeaponScoreMod(ai) * TechBiologicalWeapon.avgDamage(wpn.maxDamage(), (int)antiDote) * 200 / wpn.space(d);
					else
						currentScore = 0;
					if (currentScore > bestScore) {
						bestWeapon = wpn;
						bestScore = currentScore;
					}
				}
			}
			if (bestWeapon == null) {
				shield -= 1;
				//if we couldn't find any ranged-weapon that does damage, we allow 
				if (shield < 0 && prohibitMissiles == true && mustBeRanged == true) {
					shield = startingShield;
					prohibitMissiles = false;
					continue;
				}
				if (shield < 0 && prohibitMissiles == false && mustBeRanged == true) {
					shield = startingShield;
					mustBeRanged = false;
					continue;
				}
				if (shield < 0)
					return null;
			}
		}
		int weaponSlotsOccupied = 0;
		for (int i = 0; i < ShipDesign.maxWeapons; i++)
			if (d.wpnCount(i)>0)
				weaponSlotsOccupied++;

		int num = (int)Math.floor(spaceAllowed / bestWeapon.space(d));
		//ail: there is a maximum amount of bombs that make sense, which is exceeded in late game, we need to calculate it and adjust the number
		if (bestWeapon.groundAttacksOnly()) {
			//maximum hit points a colony can have * hit points per point of population
			float highestPopulation = 0;
			for (int id=0;id<ai.empire().sv.count();id++) {
				StarSystem current = galaxy().system(id);
				if (current.planet().maxSize() > highestPopulation)
					highestPopulation = current.planet().maxSize();
			}
			float maxDamageNeeded = highestPopulation * 200;
			float maxBombsNeeded = maxDamageNeeded / (bestWeapon.firepower(ai.empire().bestEnemyPlanetaryShieldLevel()-ai.empire().bestEnemyShieldLevel()) * 10);
			num = (int) Math.ceil(min(num, maxBombsNeeded));
		}
		int maxSlots = weaponSlotsOccupied + numSlotsToUse;
		if (maxSlots > ShipDesign.maxWeapons)
			maxSlots = ShipDesign.maxWeapons;

		if (!dryRun) {
			for (int slot=weaponSlotsOccupied; slot<maxSlots;slot++) {
				int numSlot = (int) Math.ceil((float)num/(maxSlots-slot));
				if (numSlot > 0) {
					d.weapon(slot, bestWeapon);
					d.wpnCount(slot, numSlot);
					num -= numSlot;
				}
			}
		}
		if (num > 0)
			return bestWeapon;
		return null;
	}
	private float bioWeaponScoreMod(ShipDesigner ai)	{
		float scoreMod = 1;
		float totalMissileBaseCost = 0;
		//float totalShipCost = 0;
		float totalPopulationCost = 0;
		for(Empire enemy : ai.empire().contactedEmpires()) {
			totalMissileBaseCost += enemy.totalMissileBaseCost() * 50; //this is maintenance-cost but was supposed to be the actual cost, so times 50 to take that into account!
			//totalShipCost += enemy.shipMaintCostPerBC();
			totalPopulationCost += enemy.totalPlanetaryPopulation() * enemy.tech().populationCost();
		}
		//System.out.print("\n"+ai.empire().name()+" totalPopulationCost"+totalPopulationCost+" totalMissileBaseCost: "+totalMissileBaseCost);
		if (totalMissileBaseCost > 0)
			scoreMod = totalPopulationCost / (totalMissileBaseCost + totalPopulationCost);
		float invasionStrength = ai.empire().tech().troopCombatAdj(false);
		float avgDefenderStrength = 0;
		int empireCount = 0;
		for(Empire emp : ai.empire().contactedEmpires()) {
			avgDefenderStrength += ai.empire().viewForEmpire(emp).spies().tech().troopCombatAdj(true);
			++empireCount;
		}
		if (empireCount > 0)
			avgDefenderStrength /= empireCount;
		float killRatio = 1.0f;
		if (avgDefenderStrength > 0) {
			if (invasionStrength > avgDefenderStrength) {
				float atkAdv = invasionStrength - avgDefenderStrength;
				killRatio = (float) ((Math.pow(100-atkAdv,2)/2) / (Math.pow(100,2) - Math.pow(100-atkAdv,2)/2));
			} else {
				float defAdv = avgDefenderStrength - invasionStrength;
				killRatio = (float) ((Math.pow(100,2) - Math.pow(100-defAdv,2)/2) / (Math.pow(100-defAdv,2)/2));
			}
		}
		if (killRatio < 1)
			scoreMod *= killRatio;
		//System.out.print("\n"+ai.empire().name()+" multiplies bio-weapon score by "+killRatio+" new score-modifier: "+scoreMod);
		return scoreMod;
	}
}

