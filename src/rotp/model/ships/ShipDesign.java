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
package rotp.model.ships;


import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import rotp.model.combat.CombatStack;
import rotp.model.galaxy.Galaxy;
import rotp.model.galaxy.StarSystem;
import rotp.model.planet.PlanetType;
import rotp.util.Base;

public final class ShipDesign extends Design {
    private static final long serialVersionUID = 1L;
    public static final int maxWeapons = 4;
    public static final int maxSpecials = 3;
    public static final int[] shipColors = { 0,1,3,4,5,6,8,9,10,11};
    public static final int maxWeapons()                 { return maxWeapons; }
//    public static final int maxSpecials()                { return maxSpecials; }

    public static final int COLONY = 1;
    public static final int SCOUT = 2;
    public static final int BOMBER = 3;
    public static final int FIGHTER = 4;
    public static final int DESTROYER = 5;

    public static final int MAX_SIZE = 3;
    public static final int SMALL = 0;
    public static final int MEDIUM = 1;
    public static final int LARGE = 2;
    public static final int HUGE = 3;

	public static final int DEFAULT_SHIP_COUNT = 0; // => Follow the default governor value

    public int maxSpecials()                { return special.length; }

    private ShipComputer computer;
    private ShipShield shield;
    private ShipECM ecm;
    private ShipArmor armor;
    private ShipEngine engine;
    private ShipManeuver maneuver;
    private final ShipWeapon[] weapon = new ShipWeapon[maxWeapons];
    private final int[] wpnCount = new int[maxWeapons];
    private final ShipSpecial[] special; // BR: to be initialized later to accommodate monsters
    private int size = SMALL;
    private int mission = SCOUT;
    private int unusedTurns = 0;     // # turns while built but unused by FleetCommander
    public int remainingLife = 999; // once obsolete, this is minimum num turns to survive before scrapping
    private int maxUnusedTurns = 12; // max number of turns to survive while unused
    private int usedCount = 0;       // # ships used by FleetCommander... updated each turn
    private float perTurnDmg = 0;
    private boolean autoScout = false;
    private boolean autoColonize = false;
    private boolean autoAttack = false;
	private int autoScoutShipCount	  = DEFAULT_SHIP_COUNT;
	private int autoColonizeShipCount = DEFAULT_SHIP_COUNT;
	private int autoAttackShipCount	  = DEFAULT_SHIP_COUNT;
    private String iconKey;
    private int shipColor;
    private final Integer hashCode;
    private Integer monsterHitPoints;	// BR: for monsters only
    private Integer monsterBeamDefense;	// BR: for monsters only
    private Integer monsterEcmDefense;	// BR: for monsters only
    private Integer monsterAttackLevel;	// BR: for monsters only
    private Integer monsterInitiative;	// BR: for monsters only
    private Integer monsterManeuver;	// BR: for monsters only
    private transient ImageIcon icon;
    private transient Image image;
    private transient float costBC;

    @Override public int hashCode() {
    	if (hashCode == null)
    		return super.hashCode(); // for backward compatibility
    	return hashCode;
    }
    @Override public boolean equals(Object shipDesign) {
    	if (hashCode == null)
    		return this==shipDesign; // for backward compatibility
    	if (shipDesign == null)
    		return false;
    	if (this==shipDesign)
    		return true;
    	if (shipDesign instanceof ShipDesign)
    		return ((ShipDesign) shipDesign).hashCode() == this.hashCode();
    	return false;
    }

    public static float hullPoints(int size)   { return Galaxy.current().pow(6, size); }
    @Override
    public boolean isShip()                 { return true; }

    //public int specialSize()                { return special.length; }
    public void monsterHitPoints(int hit)   { monsterHitPoints = hit; }
    public void monsterBeamDefense(int def) { monsterBeamDefense = def; }
    public void monsterEcmDefense(int ecm)  { monsterEcmDefense = ecm; }
    public void monsterAttackLevel(int lev) { monsterAttackLevel = lev; }
    public void monsterInitiative(int lev)  { monsterInitiative = lev; }
    public void monsterManeuver(int speed)  { monsterManeuver = speed; }
    public ShipComputer computer()          { return computer; }
    public void computer(ShipComputer c)    { computer = c; }
    public ShipShield shield()              { return shield; }
    public void shield(ShipShield c)        { shield = c; }
    public ShipECM ecm()                    { return ecm; }
    public void ecm(ShipECM c)              { ecm = c; }
    public ShipArmor armor()                { return armor; }
    public void armor(ShipArmor c)          { armor = c; }
    public ShipEngine engine()              { return engine; }
    public void engine(ShipEngine c)        { engine = c; }
    public ShipManeuver maneuver()          { return maneuver; }
    public void maneuver(ShipManeuver c)    { maneuver = c; }
    public ShipWeapon weapon(int i)           { return weapon[i]; }
    public ShipSpecial special(int i)         { return special[i]; }
    public int wpnCount(int i)                { return wpnCount[i]; }
    public void weapon(int i, ShipWeapon c)   { weapon[i] = c; }
    public void special(int i, ShipSpecial c) { special[i] = c; }
    public void wpnCount(int i, int n)        { wpnCount[i] = n; }
    public void weapon(int i, ShipWeapon c, int n) {
        weapon[i] = c;
        wpnCount[i] = n;
    }
    public int size()                       { return size; }
    public void size(int i)                 { size = i; }
    public int mission()                    { return mission; }
    public void mission(int i)              { mission = i; }
    public int remainingLife()              { return remainingLife; }
    public int unusedTurns()                { return unusedTurns; }
    public int maxUnusedTurns()             { return maxUnusedTurns; }
    public void maxUnusedTurns(int i)       { maxUnusedTurns = i; }
    public void addUsedCount(int i)         { usedCount += i; }
    public float perTurnDamage()            { return perTurnDmg; }
    public void perTurnDamage(float d)      { perTurnDmg = d; }
    public String iconKey()                 { return iconKey; }
    public void iconKey(String s)           { icon = null; iconKey = s; }
    public void seq(int i)                  { seq = i%6; setIconKey(); }
 //   public float scrapValue(int n)          { return cost() * n / 4.0f; }
    public float scrapValue(int n)          { return cost() * n * options().selectedScrapRefundFactor(); }
    public void setIconKey() {
        iconKey(ShipLibrary.current().shipKey(lab().shipStyleIndex(), size(), seq()));
    }
    public int shipColor()                  { return shipColor; }
    public void shipColor(int i)            { shipColor = i; }
    public void resetImage()                { image = null; }
    public void setImage(Image img)         { image = img; } // BR: for Monsters
    @Override
    public Image image() {
        if (image == null) {
            ShipImage shipImage = shipImage();
            image = icon(shipImage.nextIcon()).getImage();
            if (shipColor > 0)
                image = Base.colorizer.makeColor(shipColor, image);
        }
        return image;
    }  
    public String sizeDesc()		{ return sizeDesc(size()); }
    public String sizeDesc(int idx)	{
        switch (idx) {
            case ShipDesign.SMALL:  return text("SHIP_DESIGN_SIZE_SMALL");
            case ShipDesign.MEDIUM: return  text("SHIP_DESIGN_SIZE_MEDIUM");
            case ShipDesign.LARGE:  return  text("SHIP_DESIGN_SIZE_LARGE");
            case ShipDesign.HUGE:   return  text("SHIP_DESIGN_SIZE_HUGE");
        }
        return "";
    }
    @Override
    public ImageIcon icon()         {
        if (icon == null)
            icon = icon(iconKey);
        return icon;
    }
    public ShipDesign()             { this(SMALL); }
    private int setMonsterHitPointsAndGetSize(int hitPts) {
    	monsterHitPoints = hitPts;
		if (monsterHitPoints < 25)
			return SMALL;
		else if (monsterHitPoints < 150)
			return MEDIUM;
		else if (monsterHitPoints < 750)
			return LARGE;
		else
			return HUGE;
    }
    public ShipDesign(int numSpecials, int hullHitPoints) {
    	special  = new ShipSpecial[numSpecials];
        hashCode = galaxy().nextHashCodeShipDesign();
		monsterHitPoints = hullHitPoints;
    	int sz = setMonsterHitPointsAndGetSize(hullHitPoints);
    	init(sz);
    }
    public ShipDesign(int sz)      {
    	special  = new ShipSpecial[maxSpecials];
        hashCode = galaxy().nextHashCodeShipDesign();
    	if (sz<0)	// BR: for custom Hit points design
    		sz = setMonsterHitPointsAndGetSize(-sz);
    	init(sz);
    }
    private void init(int sz)      {
    	size(sz);
        active = false;
        for (int i=0; i<maxWeapons(); i++)
        	wpnCount(i,0);
    }
    public boolean isScout()       { return (mission() == SCOUT); }
    public boolean isFighter()     { return (mission() == FIGHTER); }
    public boolean isColonyShip()  { return (mission() == COLONY); }
    public boolean isBomber()      { return (mission() == BOMBER); }
    public boolean isDestroyer()   { return (mission() == DESTROYER); }

    public void clearEmptyWeapons() {
        for (int i=0;i<wpnCount.length;i++) {
            if (wpnCount[i] == 0)
                weapon[i] = lab().noWeapon();
        }
    }

    public ShipImage shipImage() {
        return ShipLibrary.current().shipImage(lab().shipStyleIndex(), size(), seq());
    }
    public void nextImage() {
        seq++;
        if (seq > 5)
            seq = 0;
        setIconKey();
    }
    public void prevImage() {
        seq--;
        if (seq < 0)
            seq = 5;
        setIconKey();
    }
    public void recalculateCost() {
        costBC = -1;
    }
    public void copyFrom(ShipDesign d) {
        seq = d.seq();
        lab(d.lab());
        iconKey(d.iconKey());
        shipColor(d.shipColor());
        size(d.size());
        //name(d.name());
        computer(d.computer());
        shield(d.shield());
        ecm(d.ecm());
        armor(d.armor());
        engine(d.engine());
        maneuver(d.maneuver());
        mission(d.mission());
        for (int i=0;i<maxWeapons();i++) {
            weapon(i, d.weapon(i));
            wpnCount(i, d.wpnCount(i));
        }
        for (int i=0;i<maxSpecials();i++) {
            special(i, d.special(i));
        }
    }
    public boolean validConfiguration() {
        return availableSpace() >= 0;
    }
    public int nextEmptyWeaponSlot() {
        for (int i=0;i<maxWeapons;i++) {
            if (weapon(i).isNone())
                return i;
        }
        return -1;
    }
    public int nextEmptySpecialSlot() {
        for (int i=0;i<maxSpecials;i++) {
            if (special(i).isNone())
                return i;
        }
        return -1;
    }
    protected int adjustedPixel(int pixel, int amt) {
        float adj = (float) amt / 12;
        if (adj > 0)
            return min(255, pixel+(int) (adj*(255-pixel)));
        if (adj < 0)
            return max(0, (int)((1+adj)*pixel));
        return pixel;
    }
    public boolean matchesDesign(ShipDesign d) {
        return matchesDesign(d, false);
    }
    public boolean matchesDesign(ShipDesign d, boolean ignoreWeapons) {
        if (scrapped() != d.scrapped())
            return false;
        if (size() != d.size())
            return false;
        if (armor() != d.armor())
            return false;
        if (shield() != d.shield())
            return false;
        if (computer() != d.computer())
            return false;
        if (ecm() != d.ecm())
            return false;
        if (engine() != d.engine())
            return false;
        if (maneuver() != d.maneuver())
            return false;
        if(!ignoreWeapons) {
            for (int i=0;i<ShipDesign.maxWeapons();i++) {
                if (weapon(i) != d.weapon(i) )
                    return false;
                if (wpnCount(i) != d.wpnCount(i))
                    return false;
            }
        }
        for (int i=0;i<d.maxSpecials();i++) {
            if (special(i) != d.special(i))
                return false;
        }
        return true;
    }
    public boolean validMission(int destId) {
        if (mission != ShipDesign.COLONY)
            return true;
        ShipSpecialColony colonySpecial = colonySpecial();
        if (colonySpecial == null)
            return true;
        if (destId == StarSystem.NULL_ID)
            return false;
        StarSystem dest = galaxy().system(destId);
        PlanetType pt = dest.planet().type();
        // return if ordersStack can colonize the destination planet
        return (empire().ignoresPlanetEnvironment() && empire().acceptedPlanetEnvironment(pt)) || (empire().canColonize(pt) && colonySpecial.canColonize(pt));
    }
    @Override
    public int cost() {
        if (costBC <= 0) {
            float cost = baseCost();
            cost += computer().cost(this);
            cost += shield().cost(this);
            cost += ecm().cost(this);
            cost += armor().cost(this);
            cost += (enginesRequired() * engine().cost(this));

            for (int i=0; i<maxWeapons(); i++)
                cost += (wpnCount(i) * weapon(i).cost(this));
            for (int i=0; i<maxSpecials(); i++)
                cost += special(i).cost(this);
			float availableSpace = availableSpace();
			if (availableSpace < 0)
				cost -= availableSpace;
            costBC = cost;
        }
        return (int) Math.ceil(costBC);
    }
    public float hullPoints() { return hullPoints(size()); }
    public float totalSpace() { return totalSpace(size()); }
    public float totalSpace(int s) {
        float techBonus = 1 + (.02f * empire().tech().construction().techLevel());
        // modnar: change total ship space for new races // modnar: NeoHumans, +40% space
        // BR: Made dataRace call
        float raceSpaceBonus = empire().shipSpaceFactor();
        float optionSpaceBonus = options().selectedShipSpaceFactor();
        int baseSpace = 0;
        switch(s) {
	        case SMALL  : baseSpace = 40; break;
	        case MEDIUM : baseSpace = 200; break;
	        case LARGE  : baseSpace = 1000; break;
	        case HUGE   : baseSpace = 5000; break;
	        default     : baseSpace = 0; break;
        }
        // modnar: change total ship space for new races
        float finalSpace = baseSpace * techBonus * raceSpaceBonus * optionSpaceBonus;
        return finalSpace;
    }
    public void becomeObsolete(int turns) {
        if (!obsolete()) {
            obsolete(true);
            remainingLife = turns;
        }
    }
    public float spaceUsed() { return spaceUsed(size()); }
    private float spaceUsed(int s) {
        int tempSize = size();
        size(s);

        float space = 0;
        space += computer().space(this);
        space += shield().space(this);
        space += ecm().space(this);
        space += armor().space(this);
        space += maneuver().space(this);
        for (int i=0; i<maxWeapons(); i++)
            space += (wpnCount(i) * weapon(i).space(this));
        for (int i=0; i<maxSpecials(); i++)
            space += special(i).space(this);
        size(tempSize);
        return space;
    }
    public int enginesUsed() { return (int) Math.ceil(enginesRequired()); }
    public float enginesRequired() {
        float engines = 0;
        engines += computer().enginesRequired(this);
        engines += shield().enginesRequired(this);
        engines += ecm().enginesRequired(this);
        engines += armor().enginesRequired(this);
        engines += maneuver().enginesRequired(this);
        for (int i=0; i<maxWeapons(); i++)
            engines += (wpnCount(i) * weapon(i).enginesRequired(this));
        for (int i=0; i<maxSpecials(); i++)
            engines += special(i).enginesRequired(this);
        return engines;
    }
    public void addWeapon(ShipWeapon wpn, int count) {
        for (int i=0; i<maxWeapons(); i++) {
            if (weapon(i).noWeapon()) {
                weapon(i, wpn);
                wpnCount(i, count);
                return;
            }
            if (weapon(i).tech() == wpn.tech()) {
                wpnCount(i, wpnCount(i) + count);
                return;
            }
        }
    }
    public boolean canAttackPlanets() {
        for (int i=0;i<maxWeapons();i++) {
            if (weapon(i).canAttackPlanets())
                return true;
        }
        for (int i=0;i<maxSpecials();i++) {
            if (special(i).canAttackPlanets())
                return true;
        }
        return false;
    }
    public boolean isArmed() {
        for (int i=0;i<maxWeapons();i++) {
            if (!weapon(i).noWeapon() && (wpnCount(i)>0))
                return true;
        }
        for (int i=0;i<maxSpecials();i++) {
            if (special(i)!=null && special(i).isWeapon())
                return true;
        }
        return false;
    }
    public boolean isArmedForShipCombat() {
        for (int i=0;i<maxWeapons();i++) {
            if (weapon(i).canAttackShips() && (wpnCount(i)>0))
                return true;
        }
        for (int i=0;i<maxSpecials();i++) {
            if (special(i)!=null && special(i).canAttackShips())
                return true;
        }
        return false;
    }
    public boolean isExtendedRange() {
        for (int i=0;i<maxSpecials();i++) {
            if (special(i)!=null && special(i).isFuelRange())
                return true;
        }
        return false;
    }
    // modnar: add firepowerAntiShip to only count weapons that can attack ships
    public float firepowerAntiShip(float shield) {
        float dmg = 0;
        for (int i=0;i<maxWeapons();i++)
			if (weapon(i).canAttackShips()) {
				dmg += (wpnCount(i) * weapon(i).firepower(shield));
            }
        return dmg;
    }
    public float firepowerAntiShip(float shield, float defense, float missileDefense) {
        float dmg = 0;
        for (int i=0;i<maxWeapons();i++)
            if (weapon(i).canAttackShips()) {
                float attack = attackLevel() + weapon(i).computerLevel() + empire().shipAttackBonus();
                float hitPct = 1;
                if(weapon(i).isBeamWeapon())
                    hitPct = (5 + attack - defense) / 10;
                if(weapon(i).isMissileWeapon())
                    hitPct = (5 + attack - missileDefense) / 10;
                hitPct = max(.05f, hitPct);
                hitPct = min(hitPct, 1.0f);
                dmg += (wpnCount(i) * weapon(i).firepower(shield) * hitPct * weapon(i).bombardAttacks());
            }
        return dmg;
    }
    // BR: To avoid monster being attacked too early
    public float firepowerAntiMonster(float shield, float defense, float missileDefense, int speed, int beamRange) {
    	int speedBonus = combatSpeed() - speed;
        float dmg = 0;
		for (int i=0; i<maxWeapons(); i++) {
			ShipWeapon wpn = weapon(i);
            if (wpn.canAttackShips()) {
            	Integer level  = wpn.tech().level();
            	int rangeMalus = beamRange - wpn.range();
                float attack   = attackLevel() + wpn.computerLevel() + empire().shipAttackBonus();
                float hitPct   = 1;
                if(wpn.isBeamWeapon()) {
                	hitPct = (5 + attack - defense) / 10;
                	if (speedBonus < 0)
                		hitPct /= (0.5f - speedBonus);
                	else if (speedBonus > 0)
                		hitPct *= (0.5f + speedBonus);
                	if (speedBonus < rangeMalus)
                		if (level < 7)
                			hitPct = 0;
                		else
                			hitPct /= 2*(1 + rangeMalus - speedBonus );
                	else if (speedBonus > rangeMalus)
                		hitPct *= (0.5f + speedBonus - rangeMalus);
                }
                if(wpn.isMissileWeapon())
                    hitPct = (5 + attack - missileDefense) / 10;
                hitPct = max(.0f, hitPct);
                hitPct = min(hitPct, 1.0f);
                dmg += (wpnCount(i) * wpn.firepower(shield) * hitPct);
            }
		}
        return dmg;
    }
    public float firepower(float shield) {
        float dmg = 0;
        for (int i=0;i<maxWeapons();i++)
            dmg += (wpnCount(i) * weapon(i).firepower(shield));
        return dmg;
    }
    public float firepower(CombatStack target, int wpn) {
        float dmg = wpnCount(wpn) * weapon(wpn).firepower(target.shieldLevel());
        if (weapon(wpn).isStreamingWeapon() && (dmg > target.maxStackHits()))
            dmg *= (dmg/target.maxStackHits());
        return dmg;
    }
    public float estimatedKills(CombatStack source, CombatStack target) {
        float kills = 0;
        for (int i=0;i<maxWeapons();i++)
            kills += weapon(i).estimatedKills(source, target, wpnCount(i));
        return kills;
    }
    public float availableSpace()                { return totalSpace() - spaceUsed(); }
    public float availableSpace(int newSize)     { return totalSpace(newSize) - spaceUsed(newSize); }
    public float availableSpaceForComputerSlot() { return availableSpace() + computer().space(this); }
    public float availableSpaceForShieldSlot()   { return availableSpace() + shield().space(this); }
    public float availableSpaceForECMSlot()      { return availableSpace() + ecm().space(this); }
    public float availableSpaceForArmorSlot()    { return availableSpace() + armor().space(this); }
    public float availableSpaceForManeuverSlot() { return availableSpace() + maneuver().space(this); }
    public float availableSpaceForWeaponSlot(int i)  { return availableSpace() + (wpnCount(i) * weapon(i).space(this)); }
    public float availableSpaceForSpecialSlot(int i) { return availableSpace() + special(i).space(this); }
    public List<ShipSpecial> availableSpecialsForSlot(int slot) {
        List<ShipSpecial> knownSpecials = lab().specials();
        List<ShipSpecial> allowedSpecials = new ArrayList<>();
        allowedSpecials.addAll(knownSpecials);
        
        for (int i=0;i<maxSpecials();i++) {
            ShipSpecial slotSpecial = special(i);
            if ((i != slot) && !slotSpecial.isNone()) {
                // remove any specials of the same type that are already in other slots
                for (ShipSpecial sp: knownSpecials) {
                    if (sp.designGroup().equals(slotSpecial.designGroup()))
                        allowedSpecials.remove(sp);
                }
            }
        }
        return allowedSpecials;
    }
    public List<ShipManeuver> availableManeuvers() {
        int maxLevel = engine().tech().level();
        List<ShipManeuver> maneuvers = new ArrayList<>();
        for (ShipManeuver manv: lab().maneuvers()) {
            if (manv.tech().level() <= maxLevel)
                maneuvers.add(manv);
        }
        return maneuvers;
    }
    public void changeIcon()  { iconKey(lab().nextAvailableIconKey(size(), iconKey));  }
    public void changeSize(int newSize) {
        size(newSize);
        iconKey(lab().nextAvailableIconKey(size(), null));
    }
    public void setSmallestSize() {
        for (int i=SMALL;i<=HUGE;i++) {
            changeSize(i);
            if (availableSpace() >= 0)
                return;
        }
    }
    public int weaponMax(int i)     { return (int)Math.max(0, weapon(i).max(this, i));  }
    public int baseHits()           { return  baseHits(size()); }
    public int baseHits(int sizeId) {
    	if (monsterHitPoints != null) // BR: for Monsters
    		return monsterHitPoints;
    	// modnar: change base ship HP for new races
    	// modnar: NeoHumans, 66.6% HP
    	// BR: put the method in Race
        float raceHPBonus = empire().hPFactor();
        switch(sizeId) {
            case SMALL  : return (int) Math.round(3 * raceHPBonus);
            case MEDIUM : return (int) Math.round(18 * raceHPBonus);
            case LARGE  : return (int) Math.round(100 * raceHPBonus);
            case HUGE   : return (int) Math.round(600 * raceHPBonus);
            default     : return 0;
        }
    }
    private int baseCost()          { return  baseCost(size()); }
    public int baseCost(int sizeId) {
        switch(sizeId) {
            case SMALL  : return 6;
            case MEDIUM : return 36;
            case LARGE  : return 200;
            case HUGE   : return 1200;
            default     : return 0;
        }
    }
    public int baseMissileDefense() {
        switch(size()) {
            case SMALL  : return 2;
            case MEDIUM : return 1;
            case LARGE  : return 0;
            case HUGE   : return -1;
            default     : return 0;
        }
    }
    public float hits()        {
    	if (monsterHitPoints != null)
    		return monsterHitPoints;
    	else
    		return armor().hits(this);
    		
    }
    public float initiative()  {
    	if (monsterInitiative != null)
    		return monsterInitiative;
        float lvl = computer().level() + maneuverability();
        for (ShipSpecial spec: special)
            lvl += spec.initiativeBonus();
        return lvl;
    }
    public float attackLevel() {
    	if (monsterAttackLevel != null)
    		return monsterAttackLevel;
        int lvl = computer().level();
        for (ShipSpecial spec: special)
            lvl += spec.attackBonus();
        return lvl;
    }
    public float shieldLevel() { return shield().level(); }
    public int combatSpeed()   {
        int speed = maneuver().combatSpeed();
        for (int i=0;i<maxSpecials();i++)
            speed += special(i).speedBonus();
        return max(speed,1);
    }
    public int maneuverability() {
    	if (monsterManeuver != null)
    		return monsterManeuver;
        int speed = baseMissileDefense() + maneuver().level();
        for (int i=0;i<maxSpecials();i++)
            speed += special(i).speedBonus();
        // always guarantee a minimum design speed of 1
        return max(1, speed);
    }
    public float weaponRange(ShipComponent c) {
        if (!c.isBeamWeapon())
            return c.range();
        float rng = c.range();
        for (int j=0;j<maxSpecials();j++)
            rng += special(j).beamRangeBonus();
        return rng;
    }
    public float targetShieldMod(ShipComponent c) {
        float shieldMod = 1.0f;
        if (c.isBeamWeapon()) {
            for (int i = 0; i < maxSpecials(); i++)
                shieldMod *= special(i).beamShieldMod();
        }
        return shieldMod;
    }
    public int moveRange() { return max(1, combatSpeed()); }
    public int repulsorRange() {
        int r = 0;
        for (int i=0;i<maxSpecials();i++)
            r = max(r, special(i).repulsorRange());
        return r;
    }
    public float missileInterceptPct(ShipWeaponMissileType wpn) {
        float maxIntercept = 0;
        for (int i=0;i<maxSpecials();i++)
            maxIntercept = max(maxIntercept, special(i).missileIntercept(wpn));
        return maxIntercept;
    }
    public int missileDefense() {
    	if (monsterEcmDefense != null)
    		return monsterEcmDefense;
        int defense = baseMissileDefense() + ecm().level() + maneuver().level();
        for (int i=0;i<maxSpecials();i++)
            defense += special(i).defenseBonus();
        return defense;
    }
    public int beamDefense() {
    	if (monsterBeamDefense != null)
    		return monsterBeamDefense;
        int defense = baseMissileDefense() + maneuver().level();
        for (int i=0;i<maxSpecials();i++)
            defense += special(i).defenseBonus();
        return defense;
    }
    public boolean allowsCloaking() {
        for (int i=0;i<maxSpecials();i++) {
            if (special(i).allowsCloaking())
                return true;
        }
        return false;
    }
    public boolean allowsTeleporting() {
        for (int i=0;i<maxSpecials();i++) {
            if (special(i).allowsTeleporting())
                return true;
        }
        return false;
    }
    public boolean ignoreRepulsors() {
        for (ShipSpecial spec: special)
            if(spec.resistRepulsors())
                return true;
        return false;
    }
    public boolean immuneToStasis() {
    	for (ShipSpecial spec: special)
            if (spec.isImmuneToStasis())
                return true;
        return false;
    }
    public float blackHoleDef() {
        float def = 0;
        for (int i=0;i<maxSpecials();i++)
            def = max(def,special(i).blackHoleDef());
        return def;
    }
    public boolean allowsScanning() {
        for (int i=0;i<maxSpecials();i++) {
            if (special(i).allowsScanning())
                return true;
        }
        return false;
    }
    public float range() {
        if (isExtendedRange())
            return empire().tech().scoutRange();
        else
            return empire().tech().shipRange();
    }
	public int baseWarpSpeed()			{ return engine().baseWarp(); }
	public float warpSpeed()			{ return engine().warp(); }
	public boolean hasColonySpecial()	{ return colonySpecial() != null; }
	public boolean hasWarpDissipator()	{
        for (ShipSpecial spec: special)
            if(!spec.isNone() && spec.tech().isWarpDissipator())
                return true;
        return false;
    }
    public boolean hasBlackHoleGenerator() {
        for (ShipSpecial spec: special)
            if(!spec.isNone() && spec.createsBlackHole())
                return true;
        return false;
    }
    public boolean hasStasisFieldGenerator() {
        for (ShipSpecial spec: special)
            if(!spec.isNone() && spec.createsStasisField())
                return true;
        return false;
    }
    public float missPct() {
        float pct = 0.0f;
        for (ShipSpecial spec: special)
            pct = max(pct, spec.missPct());
        return pct;
    }
    public ShipSpecialColony colonySpecial() {
        for (int i=0; i<maxSpecials(); i++) {
            if (special(i).isColonySpecial())
                return (ShipSpecialColony) special(i);
        }
        return null;
    }

    public boolean canUpgradeBattleComputer() {
        return empire().tech().topBattleComputerTech().level > computer().tech().level();
    }
    public boolean canUpgradeDeflectorShield() {
        return empire().tech().topDeflectorShieldTech().level > shield().tech().level();
    }
    public boolean canUpgradeEcmJammer() {
        return empire().tech().topECMJammerTech().level > ecm().tech().level();
    }
    public boolean canUpgradeHullArmor() {
        return !armor().reinforced() || empire().tech().topArmorTech().level > armor().tech().level();
    }
    public boolean canUpgradeEngine() {
        return empire().tech().topEngineWarpTech().level > engine().tech().level();
    }
    public boolean canUpgradeManeuver() {
        return empire().tech().topEngineWarpTech().level > maneuver().tech().level();
    }
    public boolean unused(int shipCount, float minUsePct)       {
        // design is considered "unused" if there are some constructed
        // but less than 20% are being allocated by the FleetCommander
        return (shipCount > 0) && (usedCount < (shipCount * minUsePct));
    }
    public void checkForUse(int shipCount, float minUsePct) {
            //unused method
    }
    public void preNextTurn() {
        resetBuildCount();
        usedCount = 0;
    }
	private boolean weaponSlotsFilled()	{
		for (int count : wpnCount)
			if (count == 0)
				return false;
		return true;
	}
	public int autoShipCount(int id)	{
		switch (id) {
			case SCOUT:	 return autoScoutShipCount();
			case COLONY: return autoColonizeShipCount();
			default:	 return autoAttackShipCount();
		}
	}
	public void checkForAutoTag()		{
		if (options().autoTagAutoColon()) {
			if (hasColonySpecial() && !weaponSlotsFilled()) {
				setAutoColonize(true);
				return;
			}
		}
		if (options().autoTagAutoScout()) {
			if (isExtendedRange() && size()<=MEDIUM) {
				setAutoScout(true);
				return;
			}
		}
	}

	public boolean isAutoScout()					{ return autoScout; }
	public void setAutoScout(boolean auto)			{ autoScout = auto; }
	public void autoScoutShipCount(int shipCount)	{ autoScoutShipCount = shipCount; }
	public void setDefaultAutoScoutShipCount()		{ autoScoutShipCount(DEFAULT_SHIP_COUNT); }	
	public boolean isDefaultAutoScoutShipCount()	{ return autoScoutShipCount == 0; }	
	public void autoScoutCountIncr(int incr)		{ autoScoutShipCount(max(1, autoScoutShipCount()+incr)); }	
	public int autoScoutShipCount()					{
		if (autoScout)
			if (autoScoutShipCount==0)
				return govOptions().getAutoScoutShipCount();
			else
				return autoScoutShipCount;
		else
			return 0;
	}

	public boolean isAutoColonize()					{ return autoColonize; }
	public void setAutoColonize(boolean auto)		{ autoColonize = auto; }
	public void autoColonizeShipCount(int shipCount){ autoColonizeShipCount = shipCount; }
	public void setDefaultAutoColonizeShipCount()	{ autoColonizeShipCount(DEFAULT_SHIP_COUNT); }	
	public boolean isDefaultAutoColonizeShipCount()	{ return autoColonizeShipCount == 0; }	
	public void autoColonizeCountIncr(int incr)		{ autoColonizeShipCount(max(1, autoColonizeShipCount()+incr)); }	
	public int autoColonizeShipCount()				{
		if (autoColonize)
			if (autoColonizeShipCount==0)
				return govOptions().getAutoColonyShipCount();
			else
				return autoColonizeShipCount;
		else
			return 0;
	}

	public boolean isAutoAttack()					{ return autoAttack; }
	public void setAutoAttack(boolean auto)			{ autoAttack = auto; }
	public void autoAttackShipCount(int shipCount)	{ autoAttackShipCount = shipCount; }
	public void setDefaultAutoAttackShipCount()		{ autoAttackShipCount(DEFAULT_SHIP_COUNT); }	
	public boolean isDefaultAutoAttackShipCount()	{ return autoAttackShipCount == 0; }	
	public void autoAttackCountIncr(int incr)		{ autoAttackShipCount(max(1, autoAttackShipCount()+incr)); }	
	public int autoAttackShipCount()				{
		if (autoAttack)
			if (autoAttackShipCount==0)
				return govOptions().getAutoAttackShipCount();
			else
				return autoAttackShipCount;
		else
			return 0;
	}

    public int getSpecialCount(boolean ignoreNonCombatAndStats) {
        int specialCount = 0;
        for(ShipSpecial spec : special) {
            if(spec.isNone())
                continue;
            if(ignoreNonCombatAndStats && (spec.isFuelRange() || spec.isColonySpecial() || spec.allowsScanning() || spec.isInertial()))
                continue;
            ++specialCount;
        }
        return specialCount;
    }
    public int bestBeamWeaponRange(int minRange) {
    	int rng = minRange;
    	for ( ShipWeapon wpn : this.weapon)
    		if (wpn.isBeamWeapon())
    			rng = max(rng, wpn.range());
        return rng;
    }
    public boolean hasBeamWeapon() {
    	for ( ShipWeapon wpn : this.weapon)
    		if (wpn.isBeamWeapon())
    			return true;
        return false;
    }
}
