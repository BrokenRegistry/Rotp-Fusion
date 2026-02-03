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
package rotp.model.ai.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import rotp.model.ai.EnemyColonyTarget;
import rotp.model.ai.EnemyShipTarget;
import rotp.model.ai.interfaces.ShipDesigner;
import rotp.model.ai.interfaces.ShipTemplate;
import rotp.model.empires.Empire;
import rotp.model.galaxy.StarSystem;
import rotp.model.ships.ShipArmor;
import rotp.model.ships.ShipComputer;
import rotp.model.ships.ShipDesign;
import rotp.model.ships.ShipECM;
import rotp.model.ships.ShipManeuver;
import rotp.model.ships.ShipShield;
import rotp.model.ships.ShipSpecial;
import rotp.model.ships.ShipWeapon;
import rotp.model.tech.Tech;
import rotp.model.tech.TechTree;

public class NewShipTemplate extends ShipTemplate {
    public  static final NewShipTemplate newShipTemplate = new NewShipTemplate();

    @Override protected ShipDesign bestDesign(ShipDesigner ai, DesignType role) {
        // up to three empires with worst relations, may include allies and ourselves
        List<TechTree> rivalsTech = assessRivalsTech(ai.empire(), 3);
        List<EnemyShipTarget> shipTargets = buildShipTargetList(rivalsTech);
        List<EnemyColonyTarget> colonyTargets = buildColonyTargetList(rivalsTech);

        Empire emp = ai.empire();

        // get the current design that we are considering replace
        ShipDesign currentDesign = null;
        switch (role) {
            case BOMBER:    currentDesign = ai.lab().bomberDesign(); break;
            case DESTROYER: currentDesign = ai.lab().destroyerDesign(); break;
            case FIGHTER:
            default:
            	currentDesign = ai.lab().fighterDesign(); break;
        }

        // create a blank design, one for each size. Add the current design as a 5th entry
        ShipDesign[] shipDesigns = new ShipDesign[5];
        for (int i = 0; i<4; i++) 
            shipDesigns[i] = newDesign(ai, role, i, shipTargets, colonyTargets); 
        shipDesigns[4] = currentDesign;

        // race's design cost multiplier for each hull size (set in definition.txt file)
        float[] costMultiplier = new float[5];
        costMultiplier[0] = emp.shipDesignModCostMultSmall();
        costMultiplier[1] = emp.shipDesignModCostMultMedium();
        costMultiplier[2] = emp.shipDesignModCostMultLarge();
        costMultiplier[3] = emp.shipDesignModCostMultHuge();  
        // add another entry for the current design, using the cost multiplier for its size
        costMultiplier[4] = costMultiplier[currentDesign.size()];

        // how many ships of each design can we build for virtual tests?
        // use top 5 colonies, with 50% production for ships
        float shipBudgetBC = shipProductionBudget(ai, 5, 0.5f); 

        SortedMap<Float, ShipDesign> designSorter = new TreeMap<>();

        for (int i = 0; i<costMultiplier.length; i++) {
            ShipDesign design = shipDesigns[i];
            // number of whole designs we can build within our budget
            int count = (int) (shipBudgetBC / (design.cost() * costMultiplier[i]));
            // total damage output for this design
            float designDamage = count * design.perTurnDamage(); 
            designSorter.put(designDamage, design);
        }     
        // lastKey is design with greatest damage
        return designSorter.get(designSorter.lastKey()); 
    }

    private ShipDesign newDesign(ShipDesigner ai, DesignType role, int size, List<EnemyShipTarget> shipTargets, List<EnemyColonyTarget> colonyTargets) {
        ShipDesign d = ai.lab().newBlankDesign(size);
        // engines are always the priority in MOO1 mechanics
        setFastestEngine(ai, d);
        // battle computers are always the priority in MOO1 mechanics
        setBestBattleComputer(ai, d); 
        
        float totalSpace = d.availableSpace();
        Empire emp = ai.empire();

        // initial separation of the free space left onto weapons and non-weapons/specials
        float moduleSpaceRatio = emp.shipDesignModModuleSpace();
        float modulesSpace = totalSpace * moduleSpaceRatio;

        // arbitrary initial weighting of what isn't weapons
        // Shield Weight: default 2, bombers/humans 4
        int shieldWeight = role.isDestroyer() ? emp.shipDesignModShieldWeightD() : emp.shipDesignModShieldWeightFB();
        // ECM Weight: default 1, bombers 3
        int ecmWeight = role.isBomber() ? emp.shipDesignModEcmWeightB() : emp.shipDesignModEcmWeightFD();
        // Maneuver Weight: default 2, fighters/alkari/mrrshan 4
        int maneuverWeight = role.isFighter() ? emp.shipDesignModManeuverWeightF() : emp.shipDesignModManeuverWeightBD();
        // Armor Weight: default 2, destroyers/bulrathi/silicoid 3
        int armorWeight = role.isDestroyer() ? emp.shipDesignModArmorWeightD() : emp.shipDesignModArmorWeightFB();
        // Specials Weight: default 1, adjust elsewhere for ship size
        int specialsWeight = emp.shipDesignModSpecialsWeight();
        // Same Speed Allowed Flag: default false, alkari/mrrshan true
        boolean sameSpeedAllowed = emp.shipDesignModSpeedMatching();
        // Reinforced Armor Allowed Flag: default true, alkari/klackon false
        boolean reinforcedArmorAllowed = emp.shipDesignModReinforcedArmor();
        // Allow Bio Weapons: default false, silicoid true  (adjusted elsewhere for leader type)
        boolean allowBioWeapons = emp.shipDesignModBioWeapons();

        // if we have a large ship, let's let the AI use more specials; it may have to differentiate designs more
        if (size >= ShipDesign.LARGE)
            specialsWeight += 1;

        // xenophobes will bio-bomb regardless of racial preferences
        if (role == DesignType.BOMBER) {
            if (ai.empire().leader().isRuthless())
                allowBioWeapons = true;
        }

        // the sum of shield, ECM, maneuver and armor weights may be not exactly equal to modulesSpace
        // however, unless it isn't 1.0 or close to it of available space after engines and BC, it doesn't matter
        int weightsSum = shieldWeight + ecmWeight + maneuverWeight + armorWeight + specialsWeight;

        float shieldSpace = modulesSpace * shieldWeight / weightsSum;
        float ecmSpace = modulesSpace * ecmWeight / weightsSum;
        float maneuverSpace = modulesSpace * maneuverWeight / weightsSum;
        float armorSpace = modulesSpace * armorWeight / weightsSum;
        float specialsSpace = modulesSpace * specialsWeight / weightsSum;

        // after installing a system we'll inevitably have leftovers
        // so the order of placing the systems will have a minor impact on the ship's design
        // the systems that come first will be most tightly constrained, the systems that come in the end will be more free
        float leftovers = 0f;

        // branches made in the ugly way for clarity
        // specials will be skipped for smaller hulls in the early game, bringing a bit more allowance to the second fitting
        ArrayList<ShipSpecial> raceSpecials = buildRacialSpecialsList(ai);

        switch (role) {
            case BOMBER:
                leftovers += setFittingSpecial(ai, d, specialsSpace, raceSpecials);
                leftovers += setFittingShields(ai, d, shieldSpace + leftovers);
                leftovers += setFittingArmor(ai, d, armorSpace + leftovers, reinforcedArmorAllowed);
                leftovers += setFittingManeuver(ai, d, maneuverSpace + leftovers, sameSpeedAllowed);
                setFittingECM(ai, d, ecmSpace + leftovers);
                break;
            case DESTROYER: 
                leftovers += setFittingSpecial(ai, d, specialsSpace, raceSpecials);
                leftovers += setFittingECM(ai, d, ecmSpace + leftovers);
                leftovers += setFittingManeuver(ai, d, maneuverSpace + leftovers, sameSpeedAllowed);
                leftovers += setFittingShields(ai, d, shieldSpace + leftovers);
                setFittingArmor(ai, d, armorSpace + leftovers, reinforcedArmorAllowed);
                break;
            case FIGHTER:
            default:
                leftovers += setFittingSpecial(ai, d, specialsSpace, raceSpecials);
                leftovers += setFittingArmor(ai, d, armorSpace + leftovers, reinforcedArmorAllowed);
                leftovers += setFittingECM(ai, d, ecmSpace + leftovers);
                leftovers += setFittingShields(ai, d, shieldSpace + leftovers);
                setFittingManeuver(ai, d, maneuverSpace + leftovers, sameSpeedAllowed);
                break;
        }

        leftovers = 0f; // whatever is left goes onto weapons

        float firstWeaponSpaceRatio = 0.8f; // bombs for bombers, best weapon for destroyers
        // what's left will be used on non-bombs for bombers, second best weapon for destroyers
        // repeat calls of setOptimalShipCombatWeapon() will result in a weapon from another category (beam, missile, streaming) than already installed
        // fighters will have a single best weapon over all four slots

        switch (role) {
            case BOMBER:
                setOptimalBombardmentWeapon(ai, d, colonyTargets, firstWeaponSpaceRatio * d.availableSpace(), allowBioWeapons); // uses slot 0
                setOptimalShipCombatWeapon(ai, d, shipTargets, d.availableSpace(), 1); // uses slot 1
                setPerTurnBombDamage(d, ai.empire());
                break;
            case DESTROYER:
                setOptimalShipCombatWeapon(ai, d, shipTargets, firstWeaponSpaceRatio * d.availableSpace(), 2); // uses slots 0-1
                setOptimalShipCombatWeapon(ai, d, shipTargets, d.availableSpace(), 2); // uses slots 2-3
                upgradeBeamRangeSpecial(ai, d);
                setPerTurnShipDamage(d, ai.empire());
                break;
            case FIGHTER:
            default:
                setOptimalShipCombatWeapon(ai, d, shipTargets, d.availableSpace(), 4); // uses slots 0-3
                upgradeBeamRangeSpecial(ai, d);
                setPerTurnShipDamage(d, ai.empire());
                break;
        }
        
        ai.lab().nameDesign(d);
        ai.lab().iconifyDesign(d);
        return d;
    }
////////////////////////////////////////////////////


// ********** MODULE FITTING FUNCTIONS ********** //

    private void setFastestEngine(ShipDesigner ai, ShipDesign d) {
        d.engine(ai.lab().fastestEngine());
    }

    private void setBestBattleComputer(ShipDesigner ai, ShipDesign d) {
        List<ShipComputer> comps = ai.lab().computers();
        for (int i=comps.size()-1; i >=0; i--) {
            d.computer(comps.get(i));
            if (d.availableSpace() >= 0)
                return;
        }
    }

    private float setFittingManeuver(ShipDesigner ai, ShipDesign d, float spaceAllowed, boolean sameSpeedAllowed) {
        float initialSpace = d.availableSpace();

        for (ShipManeuver manv : ai.lab().maneuvers()) {
            ShipManeuver prevManv = d.maneuver();
            int prevSpeed = d.combatSpeed();
            
            d.maneuver(manv);

            if ((initialSpace - d.availableSpace()) > spaceAllowed) {
                d.maneuver(prevManv);
            }
            else if ((d.combatSpeed() == prevSpeed) && (!sameSpeedAllowed)) {
                d.maneuver(prevManv);
            }
        }
        return (spaceAllowed - (initialSpace - d.availableSpace()));
    }

    private float setFittingArmor(ShipDesigner ai, ShipDesign d, float spaceAllowed, boolean reinforcedArmorAllowed){
        float initialSpace = d.availableSpace();

        boolean foundIt = false;
        List<ShipArmor> armors = ai.lab().armors();
        for (int i=armors.size()-1; (i >=0) && (!foundIt); i--) {
            ShipArmor arm = armors.get(i);

            // as we go backwards from the best armor to the worst,
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

    private float setFittingShields(ShipDesigner ai, ShipDesign d, float spaceAllowed) {
        float initialSpace = d.availableSpace();

        boolean foundIt = false;
        List<ShipShield> shields = ai.lab().shields();
        for (int i=shields.size()-1; (i >= 0) && (!foundIt); i--) {
            d.shield(shields.get(i));
            if ((initialSpace - d.availableSpace()) <= spaceAllowed)
                foundIt = true;
        }
        return (spaceAllowed - (initialSpace - d.availableSpace()));
    }

    private float setFittingECM(ShipDesigner ai, ShipDesign d, float spaceAllowed) {
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

    private ArrayList<ShipSpecial> buildRacialSpecialsList(ShipDesigner ai) {
        Empire emp = ai.empire();
        ArrayList<ShipSpecial> specials = new ArrayList<>();
        List<ShipSpecial> allSpecials = ai.lab().specials();

        // the intended order of the resulting list is:
        // 0. no special special to make sure AI doesn't end up with battle scanners on all vehicles
        // 1. battle scanner (always there)
        // 2. up to three latest non-colony, non-HEF specials the race has for added spice (and to account for races which do not have preferences)
        // 3. whatever special types the race prefers in ascending level order, well the order they're stored in in ai.lab().specials()
        // 4. black hole or teleporters (always good, HEF is only good for beam ships and is added separately)
        // then it would be fitted on the design in backwards order, so it's ok if the list has some repeat entries

        // 0
        specials.add(new ShipSpecial());

        // 1 - Battle Scanner
        for (ShipSpecial spec: allSpecials) {
            if (spec.allowsScanning()) {
                specials.add(spec);
            }
        }

        // create list of specials that we will not consider in the racials special list
        // colony modules, reserve fuel tanks, and High Energy Focus (set elsewhere)
        List<ShipSpecial> exclusionList1 = new ArrayList<>();
        for (ShipSpecial spec: allSpecials) {
            if (spec.isColonySpecial()
            || spec.isFuelRange()
            || (spec.beamRangeBonus() > 0))
                exclusionList1.add(spec);
        }
        // 2 - three best specials not in the exclusion list
        for (int i=allSpecials.size()-1; (i >=0) && (i>allSpecials.size()-4); i--) {
            ShipSpecial spec = allSpecials.get(i);
            if (!exclusionList1.contains(spec))
                specials.add(allSpecials.get(i));
        }

        // Alkari and Psilon
        boolean preferPulsars = emp.shipDesignModPrefPulsars();
        // Darlok and Psilon
        boolean preferCloak = emp.shipDesignModPrefCloak();
        // Meklar and Psilon
        boolean preferRepair = emp.shipDesignModPrefRepair();
        // Mrrshan and Psilon
        boolean preferInertial = emp.shipDesignModPrefInertial();
        // Sakkra, Bulrathi and Psilon
        boolean preferMissileShield = emp.shipDesignModPrefMissShield();
        // Psilon
        boolean preferRepulsor = emp.shipDesignModPrefRepulsor();
        // Psilon
        boolean preferStasisField = emp.shipDesignModPrefStasis();
        // Psilon
        boolean preferStreamProjector = emp.shipDesignModPrefStreamProj();
        // Psilon
        boolean preferWarpDissipator = emp.shipDesignModPrefWarpDissip();
        // Psilon
        boolean preferTechNullifier = emp.shipDesignModPrefTechNullif();
        // Psilon
        boolean preferBeamFocus = emp.shipDesignModPrefBeamFocus();

        // 3 - Racially preferred specials
        for (ShipSpecial spec: allSpecials) {
            Tech tech = spec.tech();
            if ((tech != null) && !spec.isColonySpecial() && !spec.isFuelRange() ) {
                if (preferPulsars && tech.isType(Tech.ENERGY_PULSAR))
                    specials.add(spec);
                if (preferCloak && tech.isType(Tech.CLOAKING))
                    specials.add(spec);
                if (preferRepair  && tech.isType(Tech.AUTOMATED_REPAIR))
                    specials.add(spec);
                if (preferInertial && tech.isType(Tech.SHIP_INERTIAL))
                    specials.add(spec);
                if (preferMissileShield && tech.isType(Tech.MISSILE_SHIELD))
                    specials.add(spec);
                if (preferRepulsor && tech.isType(Tech.REPULSOR))
                    specials.add(spec);
                if (preferStasisField && tech.isType(Tech.STASIS_FIELD))
                    specials.add(spec);
                if (preferStreamProjector && tech.isType(Tech.STREAM_PROJECTOR))
                    specials.add(spec);
                if (preferWarpDissipator && tech.isWarpDissipator())
                    specials.add(spec);
                if (preferTechNullifier && tech.isTechNullifier())
                    specials.add(spec);
                if (preferBeamFocus && tech.isType(Tech.BEAM_FOCUS))
                    specials.add(spec);
            }
        }

        // 4 - Subspace Teleporter & Black Hole Generator
        for (ShipSpecial spec: allSpecials) {
            if (spec.allowsTeleporting() || spec.createsBlackHole())
                specials.add(spec);
        }
        return specials;
    }

    private float setFittingSpecial(ShipDesigner ai, ShipDesign d, float spaceAllowed, ArrayList<ShipSpecial> specials) {
        int nextSlot = d.nextEmptySpecialSlot();
        if (nextSlot < 0)
            return spaceAllowed;

        float initialSpace = d.availableSpace();
        boolean foundIt = false;

        for (int i=specials.size()-1; (i >=0) && (!foundIt); i--) {
            d.special(nextSlot,specials.get(i));
            if ((initialSpace - d.availableSpace()) <= spaceAllowed)
                foundIt = true;
        }
        return (spaceAllowed - (initialSpace - d.availableSpace()));
    }

    private void upgradeBeamRangeSpecial(ShipDesigner ai, ShipDesign d) {
        // if not using a beam weapon, then skip
        if (!d.weapon(0).isBeamWeapon())
            return;
        // if teleporters or max combat speed, skip
        if (d.allowsTeleporting() || (d.combatSpeed() >= 9))
            return;
        // if we don't have room for more specials, we can skip
        int slot1 = d.nextEmptySpecialSlot();
        if (slot1 < 0)
            return;

        // go through specials that improve compat speed (inertials)
        int addlRange = 0;
        float wpnRangeFactor = 0.95f;

        List<ShipSpecial> specials = ai.lab().specials();
        for (ShipSpecial spec: specials) {

            boolean notInstalledAlready = true;
            for (int i = 0; i<ShipDesign.maxSpecials; i++) {
                if (d.special(i).name().equals(spec.name())) // ugly as hell
                    notInstalledAlready = false;
            }

            if (notInstalledAlready) {
                int rangeBonus = spec.beamRangeBonus();
                if (rangeBonus > 0) {
                    int rangeDiff = rangeBonus - addlRange;
                    int wpnCount = d.wpnCount(0);
                    int minNewWpnCount = (int) Math.ceil(wpnCount*Math.pow(wpnRangeFactor, rangeDiff));
                    // calc reduction in space and how many weapons need to be removed
                    float spaceLost = d.availableSpace() + d.special(slot1).space(d) - spec.space(d);
                    int wpnRemoved = (int) Math.floor(spaceLost/ d.weapon(0).space(d));
                    int newWpnCount = wpnCount+wpnRemoved;
                    if (newWpnCount >= minNewWpnCount) {
                        addlRange = rangeBonus;
                        d.special(slot1,spec);
                        d.wpnCount(0,newWpnCount);
                    }
                }
            }
        }
    }
////////////////////////////////////////////////////


// ********** HELPER FUNCTIONS ASSESSING ENEMIES AND OWN PRODUCTION ********** //

    private float shipProductionBudget(ShipDesigner ai, int topSystems, float shipRatio) {
        // how many BCs can topSystems number of top producing colonies
        // crank into ship production with shipRatio ratio per turn

        List<StarSystem> systems = ai.empire().allColonizedSystems();
        List<Float> systemsProduction = new ArrayList<>();

        for (StarSystem sys: systems)
            systemsProduction.add(sys.colony().production());
        
        Collections.sort(systemsProduction, Collections.reverseOrder());
        
        float totalShipProduction = 0f;
        for (int i = 0; (i<topSystems) && (i<systemsProduction.size()); i++)
            totalShipProduction += systemsProduction.get(i);

        return totalShipProduction;
    }

////////////////////////////////////////////////////

    
// ********* FUNCTIONS SETTING ANTI-SHIP AND ANTI-PLANET WEAPONS ********** //

    private void setOptimalShipCombatWeapon(ShipDesigner ai, ShipDesign d, List<EnemyShipTarget> targets, float spaceAllowed, int numSlotsToUse) {
        List<ShipWeapon> allWeapons = ai.lab().weapons();
        List<ShipSpecial> allSpecials = ai.lab().specials();

        List<ShipSpecial> rangeSpecials = new ArrayList<>();
        for (ShipSpecial sp: allSpecials) {
            if (sp.allowsCloaking()
            ||  sp.allowsTeleporting()
            || (sp.beamRangeBonus() >= 2))
                rangeSpecials.add(sp);
        }

        boolean haveBeam = false;
        boolean haveMissiles = false;
        boolean haveStreaming = false;
        int weaponSlotsOccupied = 0;

        for (int i = 0; i < ShipDesign.maxWeapons; i++) {
            if (d.wpnCount(i)>0) {
                // I assume slots are always occupied consequtively for the ease of implementation (and they should be in the current code)
                // otherwise some sorting will be needed at the start of this function
                weaponSlotsOccupied++;

                if (d.weapon(i).isStreamingWeapon()) { // not sure if streaming and beam definitions overlap, so doing it this way
                    haveStreaming = true;
                }
                else if (d.weapon(i).isBeamWeapon()) {
                    haveBeam = true;
                }
                else if (d.weapon(i).isMissileWeapon()) {
                    haveMissiles = true;
                }
            }
        }

        DesignDamageSpec maxDmgSpec = newDamageSpec();
        for (ShipWeapon wpn: allWeapons) {
            if (wpn.canAttackShips()) {
                if ((wpn.isBeamWeapon() && haveBeam) || (wpn.isStreamingWeapon() && haveStreaming) || (wpn.isMissileWeapon() && haveMissiles)) {
                    // a fairly ugly way to ensure that designs have weapons of different kinds
                    // as long as we keep up to two different weapon types, we're ok, as any AI has both lasers and missiles from the start
                    // with three, a check would be needed to ensure weapon space doesn't go unused on null weapon
                }
                else { // yeah, we don't have a weapon of this kind already installed
                    DesignDamageSpec minDmgSpec = newDamageSpec();
                    minDmgSpec.damage = Float.MAX_VALUE;
                    for (EnemyShipTarget tgt: targets) {
                        DesignDamageSpec spec = simulateDamage(d, wpn, rangeSpecials, tgt, spaceAllowed);
                        if (minDmgSpec.damage > spec.damage)
                            minDmgSpec.set(spec);
                    }
                    if (maxDmgSpec.damage < minDmgSpec.damage)
                        maxDmgSpec.set(minDmgSpec);
                }
            }
        }

        // at this point, maxDmgSpec is the optimum
        // spread out the weapon count across all 4 weapon slots 
        // ** well, now across numSlotsToUse starting with weaponSlotsOccupied ** //
        // using (int) Math.ceil((float)num/(maxSlots-slot)) ensures
        // equal distribution with highest first.. i.e 22 = 6 6 5 5
        if (maxDmgSpec.weapon != null) {
            int num = maxDmgSpec.numWeapons;
            int maxSlots = weaponSlotsOccupied + numSlotsToUse;
            if (maxSlots > ShipDesign.maxWeapons)
                maxSlots = ShipDesign.maxWeapons;
            
            for (int slot=weaponSlotsOccupied; slot<maxSlots;slot++) {
                int numSlot = (int) Math.ceil((float)num/(maxSlots-slot));
                if (numSlot > 0) {
                    d.weapon(slot, maxDmgSpec.weapon);
                    d.wpnCount(slot, numSlot);
                    num -= numSlot;
                }
            }
        }
        if (maxDmgSpec.special != null) {
            int spSlot = d.nextEmptySpecialSlot();
            d.special(spSlot, maxDmgSpec.special);
        }
        d.perTurnDamage(maxDmgSpec.damage);
        maxDmgSpec.reclaim();
    }
    private void setOptimalBombardmentWeapon(ShipDesigner ai, ShipDesign d, List<EnemyColonyTarget> targets, float spaceAllowed, boolean allowBioWeapons) {
        List<ShipWeapon> allWeapons = ai.lab().weapons();
        List<ShipSpecial> allSpecials = ai.lab().specials();

        List<ShipSpecial> rangeSpecials = new ArrayList<>();
        for (ShipSpecial sp: allSpecials) {
            if (sp.allowsCloaking()
            ||  sp.allowsTeleporting())
                rangeSpecials.add(sp);
        }

        DesignDamageSpec maxDmgSpec = newDamageSpec();
        for (ShipWeapon wpn: allWeapons) {
            if (allowBioWeapons || !wpn.isBioWeapon()) {
                DesignDamageSpec minDmgSpec = newDamageSpec();
                minDmgSpec.damage = Float.MAX_VALUE;
                for (EnemyColonyTarget tgt: targets) {
                    DesignDamageSpec spec = simulateBombDamage(d, wpn, rangeSpecials, tgt, spaceAllowed);
                    if (minDmgSpec.damage > spec.damage)
                        minDmgSpec.set(spec);
                }
                if (maxDmgSpec.damage < minDmgSpec.damage)
                    maxDmgSpec.set(minDmgSpec);
            }
        }

        // bombardment weapons go in slot 0
        if (maxDmgSpec.weapon != null) {
            d.weapon(0, maxDmgSpec.weapon);
            d.wpnCount(0, maxDmgSpec.numWeapons);
        }
        if (maxDmgSpec.special != null) {
            int spSlot = d.nextEmptySpecialSlot();
            d.special(spSlot, maxDmgSpec.special);
        }
        d.perTurnDamage(maxDmgSpec.damage);
        maxDmgSpec.reclaim();
    }
////////////////////////////////////////////////////


// ********** DAMAGE SIMULATION FUNCTIONS ********** //
// some are called from outside

    private DesignDamageSpec simulateDamage(ShipDesign d, ShipWeapon wpn, List<ShipSpecial> specials, EnemyShipTarget target, float spaceAllowed) {
        DesignDamageSpec spec = newDamageSpec();
        spec.weapon = wpn;
        spec.damage = 0;

        mockDesign.copyFrom(d);
        int wpnSlot = mockDesign.nextEmptyWeaponSlot();
        int specSlot = mockDesign.nextEmptySpecialSlot();
        int numWeapons = (int) (spaceAllowed/wpn.space(d));

        mockDesign.wpnCount(wpnSlot, numWeapons);
        mockDesign.weapon(wpnSlot, wpn);

        float wpnDamage = estimatedShipDamage(mockDesign, target);
        if (wpnDamage > spec.damage) {
            spec.special = null;
            spec.weapon = wpn;
            spec.numWeapons = numWeapons;
            spec.damage = wpnDamage;
        }

        for (ShipSpecial sp: specials) {
            mockDesign.special(specSlot, sp);
            wpnDamage = estimatedShipDamage(mockDesign, target);
            if (wpnDamage > spec.damage) {
                spec.special = null;
                spec.weapon = wpn;
                spec.numWeapons = numWeapons;
                spec.damage = wpnDamage;
            }
        }
        return spec;
    }
    private DesignDamageSpec simulateBombDamage(ShipDesign d, ShipWeapon wpn, List<ShipSpecial> specials, EnemyColonyTarget target, float spaceAllowed) {
        DesignDamageSpec spec = newDamageSpec();
        spec.weapon = wpn;
        spec.damage = 0;

        mockDesign.copyFrom(d);
        int wpnSlot = mockDesign.nextEmptyWeaponSlot();
        int specSlot = mockDesign.nextEmptySpecialSlot();
        float spaceForBombs = spaceAllowed;
        int numWeapons = (int) (spaceForBombs/wpn.space(d));

        mockDesign.wpnCount(wpnSlot, numWeapons);
        mockDesign.weapon(wpnSlot, wpn);

        float wpnDamage = estimatedBombDamage(mockDesign, target);
        if (wpnDamage > spec.damage) {
            spec.special = null;
            spec.weapon = wpn;
            spec.numWeapons = numWeapons;
            spec.damage = wpnDamage;
        }

        for (ShipSpecial sp: specials) {
            mockDesign.special(specSlot, sp);
            wpnDamage = estimatedBombDamage(mockDesign, target);
            if (wpnDamage > spec.damage) {
                spec.special = sp;
                spec.weapon = wpn;
                spec.numWeapons = numWeapons;
                spec.damage = wpnDamage;
            }
        }
        return spec;
    }

    void setPerTurnShipDamage(ShipDesign d, Empire emp) {
        List<EnemyShipTarget> targets = buildShipTargetList(assessRivalsTech(emp, 3)); // may prove a source of bugs, needs access to a built target list
        float minDamage = Float.MAX_VALUE;
        for (EnemyShipTarget tgt: targets) {
            float targetDmg = estimatedShipDamage(d, tgt);
            minDamage = Math.min(minDamage, targetDmg);
        }
        d.perTurnDamage(minDamage);
    }
    private void setPerTurnBombDamage(ShipDesign d, Empire emp) {
        List<EnemyColonyTarget> targets = buildColonyTargetList(assessRivalsTech(emp,3));  // may prove a source of bugs, needs access to a built target list
        float minDamage = Float.MAX_VALUE;
        for (EnemyColonyTarget tgt: targets) {
            float targetDmg = estimatedBombDamage(d, tgt);
            minDamage = Math.min(minDamage, targetDmg);
        }
        d.perTurnDamage(minDamage);
    }

    private float estimatedShipDamage(ShipDesign d, EnemyShipTarget target) {
        List<ShipSpecial> rangeSpecials = new ArrayList<>();
        for (int i=0;i<d.maxSpecials();i++) {
            ShipSpecial sp = d.special(i);
            if (sp.allowsCloaking()
            || (sp.allowsTeleporting() && !target.hasInterdictors)
            || (sp.beamRangeBonus() >= 2))
                rangeSpecials.add(sp);
        }
        float totalDamage = 0;
        for (int i=0;i<ShipDesign.maxWeapons();i++) {
            float wpnDamage;
            ShipWeapon wpn = d.weapon(i);
            if (wpn.noWeapon() || wpn.groundAttacksOnly())
                wpnDamage = 0;
            else if (target.hasRepulsors && (wpn.range() < 2) && rangeSpecials.isEmpty())
                wpnDamage = 0;
            else {
                wpnDamage = d.wpnCount(i) * wpn.firepower(target.shieldLevel);
                if (wpn.isLimitedShotWeapon())
                        wpnDamage = wpnDamage * wpn.shots() / 10;
                // divide by # of turns to fire
                wpnDamage /= wpn.turnsToFire();
                // +15% damage for each weapon computer level
                // this estimates increased dmg from +hit
                wpnDamage *= (1+ (.15*wpn.computerLevel()));
            }
            totalDamage += wpnDamage;
        }
        return totalDamage;
    }
}
