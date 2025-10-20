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
package rotp.model.tech;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.util.Comparator;
import java.util.HashMap;

import rotp.model.colony.Colony;
import rotp.model.combat.CombatStack;
import rotp.model.empires.Empire;
import rotp.model.empires.species.Species;
import rotp.model.ships.ShipDesign;
import rotp.ui.BasePanel;
import rotp.ui.combat.ShipBattleUI;
import rotp.util.Base;

public class Tech implements Base {
    public static final float miniSlowRate = .97164f;
    public static final float miniFastRate = .933033f;

    public static final int ARMOR = 1;
    public static final int ATMOSPHERE_ENRICHMENT = 2;
    public static final int AUTOMATED_REPAIR = 3;
    public static final int BATTLE_COMPUTER = 4;
    public static final int BATTLE_SUIT = 5;
    public static final int BEAM_FOCUS = 6;
    public static final int BIOLOGICAL_ANTIDOTE = 7;
    public static final int BIOLOGICAL_WEAPON = 8;
    public static final int BLACK_HOLE = 9;
    public static final int BOMB_WEAPON = 10;
    public static final int CLOAKING = 11;
    public static final int CLONING = 12;
    public static final int COMBAT_TRANSPORTER = 13;
    public static final int CONTROL_ENVIRONMENT = 14;
    public static final int DEFLECTOR_SHIELD = 15;
    public static final int DISPLACEMENT = 16;
    public static final int ECM_JAMMER = 17;
    public static final int ECO_RESTORATION = 18;
    public static final int ENERGY_PULSAR = 19;
    public static final int ENGINE_WARP = 20;
    public static final int FUEL_RANGE = 21;
    public static final int HAND_WEAPON = 22;
    public static final int HYPERSPACE_COMM = 23;
    public static final int IMPROVED_INDUSTRIAL = 24;
    public static final int IMPROVED_TERRAFORMING = 25;
    public static final int INDUSTRIAL_WASTE = 26;
    public static final int MISSILE_SHIELD = 27;
    public static final int MISSILE_WEAPON = 28;
    public static final int PERSONAL_SHIELD = 29;
    public static final int PLANETARY_SHIELD = 30;
    public static final int REPULSOR = 31;
    public static final int RESERVE_FUEL_RANGE = 32;
    public static final int ROBOTIC_CONTROLS = 33;
    public static final int SCANNER = 34;
    public static final int SHIP_INERTIAL = 35;
    public static final int SHIP_NULLIFIER = 36;
    public static final int SHIP_WEAPON = 37;
    public static final int SOIL_ENRICHMENT = 38;
    public static final int STARGATE = 39;
    public static final int STASIS_FIELD = 40;
    public static final int STREAM_PROJECTOR = 41;
    public static final int SUBSPACE_INTERDICTOR = 42;
    public static final int TELEPORTER = 43;
    public static final int TORPEDO_WEAPON = 44;
    public static final int SQUID_INK = 45;
    public static final int RESIST_SPECIAL = 46;
    public static final int EAT_SHIPS = 47;
    public static final int FUTURE_COMPUTER = 90;
    public static final int FUTURE_CONSTRUCTION = 91;
    public static final int FUTURE_FORCE_FIELD = 92;
    public static final int FUTURE_PLANETOLOGY = 93;
    public static final int FUTURE_PROPULSION = 94;
    public static final int FUTURE_WEAPON = 95;
    public static final HashMap<Integer, String> typeMap = new HashMap<>();
    static {
    	typeMap.put(ARMOR, "ARMOR");
    	typeMap.put(ATMOSPHERE_ENRICHMENT, "ATMOSPHERE");
    	typeMap.put(AUTOMATED_REPAIR, "AUTOREPAIR");
    	typeMap.put(BATTLE_COMPUTER, "COMPUTER");
    	typeMap.put(BATTLE_SUIT, "BATTLESUIT");
    	typeMap.put(BEAM_FOCUS, "BEAMFOCUS");
    	typeMap.put(BIOLOGICAL_ANTIDOTE, "BIOANTIDOTE");
    	typeMap.put(BIOLOGICAL_WEAPON, "BIOWEAPON");
    	typeMap.put(BLACK_HOLE, "BLACKHOLE");
    	typeMap.put(BOMB_WEAPON, "BOMB");
    	typeMap.put(CLOAKING, "CLOAK");
    	typeMap.put(CLONING, "CLONE");
    	typeMap.put(COMBAT_TRANSPORTER, "COMBATTRANSPORT");
    	typeMap.put(CONTROL_ENVIRONMENT, "CONTROLENV");
    	typeMap.put(DEFLECTOR_SHIELD, "DEFLECTOR");
    	typeMap.put(DISPLACEMENT, "DISPLACEMENT");
    	typeMap.put(ECM_JAMMER, "ECMJAMMER");
    	typeMap.put(ECO_RESTORATION, "ECORESTORATION");
    	typeMap.put(ENERGY_PULSAR, "PULSAR");
    	typeMap.put(ENGINE_WARP, "ENGINE");
    	typeMap.put(FUEL_RANGE, "FUELRANGE");
    	typeMap.put(HAND_WEAPON, "HANDWEAPON");
    	typeMap.put(HYPERSPACE_COMM, "HYPERSPACE");
    	typeMap.put(IMPROVED_INDUSTRIAL, "IMPRINDUSTRY");
    	typeMap.put(IMPROVED_TERRAFORMING, "IMPRTFORM");
    	typeMap.put(INDUSTRIAL_WASTE, "INDWASTE");
    	typeMap.put(MISSILE_SHIELD, "MISSDEF");
    	typeMap.put(MISSILE_WEAPON, "MISSWPN");
    	typeMap.put(PERSONAL_SHIELD, "PERSSHIELD");
    	typeMap.put(PLANETARY_SHIELD, "PLANETSHIELD");
    	typeMap.put(REPULSOR, "REPULSOR");
    	typeMap.put(RESERVE_FUEL_RANGE, "RSRVFUEL");
    	typeMap.put(ROBOTIC_CONTROLS, "ROBOTCTRL");
    	typeMap.put(SCANNER, "SCANNER");
    	typeMap.put(SHIP_INERTIAL, "SHIPINERTIAL");
    	typeMap.put(SHIP_NULLIFIER, "SHIPNULLIFIER");
    	typeMap.put(SHIP_WEAPON, "SHIPWPN");
    	typeMap.put(SOIL_ENRICHMENT, "SOILENRICH");
    	typeMap.put(STARGATE, "STARGATE");
    	typeMap.put(STASIS_FIELD, "STASIS");
    	typeMap.put(STREAM_PROJECTOR, "STREAMWPN");
    	typeMap.put(SUBSPACE_INTERDICTOR, "SUBSPACEINT");
    	typeMap.put(TELEPORTER, "SUBSPACETEL");
    	typeMap.put(TORPEDO_WEAPON, "TORPEDO");
    	typeMap.put(SQUID_INK, "SQUID_INK");
    	typeMap.put(RESIST_SPECIAL, "RESIST_SPECIAL");
    	typeMap.put(EAT_SHIPS, "EAT_SHIPS");
    	typeMap.put(FUTURE_COMPUTER, "FUTURECOMP");
    	typeMap.put(FUTURE_CONSTRUCTION, "FUTURECONST");
    	typeMap.put(FUTURE_FORCE_FIELD, "FUTUREFORCE");
    	typeMap.put(FUTURE_PLANETOLOGY, "FUTUREPLANET");
    	typeMap.put(FUTURE_PROPULSION, "FUTUREPROP");
    	typeMap.put(FUTURE_WEAPON, "FUTUREWEAPON");
    }
	public static String buildId(String s, int i)	{ return s + ":" + i; }

    public String id;
    public int techType;
    public int typeSeq;
    public int level;
    public TechCategory cat;
    public String iconFilename;
    public String effectKey; // BR: Never used

    // BR: Made private to easily track their access
    private String name = "";
    // private String capsName = ""; // BR: Never Used
    private String detail = "";
    private String trigger = "";
    private String item = null;
    private String shDesc = ""; // for short description = brief()
    private String item2 = null;
    private String shDesc2 = ""; // for short description 2 = brief2()

    public int sequence;
    public int quintile = 0;
    public boolean restricted = false;
    public boolean free = false;
    public float cost = 0;
    public float size = 0;
    public float power = 0;

    public String id()               { return id; }
    public void id(String s, int i)  { id = buildId(s, i); }
    public Tech () {  }

//    public Tech(int lv, int seq, TechCategory c) {
//        typeSeq = seq;
//        level = lv;
//        cat = c;
//        init();
//    }
    public void init() {
        quintile = (level+4)/5;
    }
    @Override
    public String toString() { return concat("Tech: ", name); }

    void setName (String s)		{ name		= s; }
    void setDetail(String s)	{ detail	= s; trigger = s;} // trigger should be loaded after
    void setTrigger(String s)	{ trigger	= s; }
    void setItem(String s)		{ item		= s; }
    void setShDesc(String s)	{ shDesc	= s; }
    void setItem2(String s)		{ item2		= s; }
    void setShDesc2(String s)	{ shDesc2	= s; }
    private String detailKey()	{ return options().techRandomEvents() ? trigger: detail; }

    public	  float	  discoveryPct()		{ return cat.discoveryPct(); }
    public	  String  name()				{ return text(name); }
    public	  Integer level()				{ return level; }
    public	  String  detail()				{ return text(detailKey()); }
    protected String  detail(int i)			{ return text(detailKey(), i); }
    protected String  detail(float f)		{ return text(detailKey(), fmt(f)); }
    protected String  detail(String s)		{ return text(detailKey(), s); }
    public	  String  brief()				{ return text(shDesc); }
    protected String  brief(int i)			{ return text(shDesc, i); }
    protected String  brief(String s)		{ return text(shDesc, s); }
    public	  String  brief2()				{ return text(shDesc2); }
    public	  String  item()				{ return item == null ? name() : text(item); }
    public	  String  item2()				{ return item2 == null ? item() : text(item2); }
    public	  String  imageKey()			{ return ""; }
    public	  String  techTypeKey()			{ return typeMap.get(techType); }
    public	  String  techTypeName()		{ return labels().realLabel(techTypeKey() + "_NAME"); }
    public	  String  techTypeDesc()		{ return labels().realLabel(techTypeKey() + "_DETAIL"); }
    public	  String  infoKey()				{ return name.replace("_NAME", "_INFO"); }
    public	  String  info()				{ return labels().realLabel(infoKey()); }
    
    public	  Image	  image()				{ return iconFilename == null ? null : image(iconFilename); }
    public	  int	  futureTechLevel()		{ return 0; }

    public boolean isWarpDissipator()		{ return false; }
    public boolean isTechNullifier()		{ return false; }
    public boolean isControlEnvironmentTech() { return false; }
    public boolean isMissileWeaponTech()    { return false; }
    public boolean isMissileBaseWeapon()    { return false; }
    public boolean isRoboticControlsTech()  { return false; }
    public boolean isPlanetaryShieldTech()  { return false; }
    public boolean isFuelRangeTech()        { return false; }
    public boolean isFutureTech()           { return false; }
    public boolean isMonsterTech()          { return false; }
    public boolean isObsolete(Empire c)     { return false; }

    public boolean isType(int type)         { return techType == type; }

    public boolean providesShipComponent()  { return false; }

    public float warModeFactor()            { return 1; }
    public float expansionModeFactor()      { return 1; }
    public boolean promptToReallocate()     { return followup() != Colony.Orders.NONE; }
    public Colony.Orders followup()         { return Colony.Orders.NONE; }

    public int quintile()                   { return quintile; }
    public void provideBenefits(Empire c)   {  }
    public boolean canBeResearched(Species s)	{ return true; }

    public float baseReallocateAmount()    { return 0.25f; }
    public float tradeValue(Empire civ)    { return level; }  // BR: Never used
    public float baseValue(Empire civ)     { return level; }
    public float baseCost()                { return cost; }
    public float baseSize()                { return size; }
    public float basePower()               { return power; }

    public float baseCost(ShipDesign d)    { return cost; }
    public float baseSize(ShipDesign d)    { return size; }
    public float basePower(ShipDesign d)   { return power; }

    public boolean reducesEcoSpending()    { return false; }

    // Shielded
    public void drawIneffectiveAttack(CombatStack source, CombatStack target, int wpnNum, int count) {  }
    // Total Miss
    public void drawUnsuccessfulAttack(CombatStack source, CombatStack target, int wpnNum, int count) {  }
    // Some Damage
    public void drawSuccessfulAttack(CombatStack source, CombatStack target, int wpnNum, float dmg, int count) {
    	drawSuccessfulAttack(source, target, wpnNum, dmg, count, dmg);
    }
    public void drawSuccessfulAttack(CombatStack source, CombatStack target, int wpnNum, float dmg, int count, float force) { }

    public float researchCost()             { return cat.costForTech(this); }
    public int maxMiniaturizationLevels()   { return 50; }
    public boolean canBeMiniaturized()      { return false; }

    public float sizeMiniaturization(Empire emp) {
        TechCategory empireCat = emp == null ? cat : emp.tech().category(cat.index());
        int catLevel = (int) empireCat.techLevel();
        if ((level >= catLevel) || !canBeMiniaturized())
            return 1;
        else {
            int minLevel = min(maxMiniaturizationLevels(), catLevel - level);
            if (empireCat.isWeaponTechCategory())
                return pow(miniFastRate, minLevel);
            else
                return pow(miniSlowRate, minLevel);
        }
    }
    public float costMiniaturization(Empire emp) {
        TechCategory empireCat = emp == null ? cat : emp.tech().category(cat.index());
        int catLevel = (int) empireCat.techLevel();
        if ((level >= catLevel) || !canBeMiniaturized())
            return 1;
        else {
            int minLevel = min(maxMiniaturizationLevels(), catLevel - level);
            return pow(miniFastRate, minLevel);
        }
    }
    public static Empire comparatorCiv;
    public static Comparator<String> LEVEL = (String o1, String o2) -> {
        Tech t1 = TechLibrary.current().tech(o1);
        Tech t2 = TechLibrary.current().tech(o2);
        return t1.level().compareTo(t2.level());
    };
    public static Comparator<String> REVERSE_LEVEL = (String o1, String o2) -> {
        Tech t1 = TechLibrary.current().tech(o1);
        Tech t2 = TechLibrary.current().tech(o2);
        return t2.level().compareTo(t1.level());
    };
    public static Comparator<Tech> RESEARCH_PRIORITY = new ResearchPriorityComparator();
    public static Comparator<Tech> RESEARCH_VALUE    = new ResearchValueComparator();
    public static Comparator<Tech> BASE_VALUE        = new BaseValueComparator();
    public static Comparator<Tech> WAR_TRADE_VALUE   = new WarTradeComparator();
    public Comparator<Tech> OBJECT_TRADE_PRIORITY = (Tech o1, Tech o2) -> {
        float pr1 = this.level() - o1.level();
        if(pr1 < 0)
            pr1 = o1.level() - this.level();
        float pr2 = this.level() - o2.level();
        if(pr2 < 0)
            pr2 = o2.level() - this.level();
        return Base.compare(pr2, pr1);
    };
    public static Comparator<Tech> TRADE_PRIORITY = (Tech o1, Tech o2) -> {
        return Base.compare(o2.level, o1.level);
    } // order that we are willing to trade away techs
    // from lowest-level to highest-level
    ;
    public void drawSpecialAttack(CombatStack source, CombatStack target, int wpnNum, float dmg) {
        ShipBattleUI ui = source.mgr.ui;
        if (ui == null)
            return;

        if (!source.mgr.showAnimations())
            return;

        int stW = ui.stackW();
        int stH = ui.stackH();
        int st0X = ui.stackX(source);
        int st0Y = ui.stackY(source);
        int st1X = ui.stackX(target);
        int st1Y = ui.stackY(target);

        int x0 = st0X > st1X ? st0X+(stW/3) :st0X+(stW*2/3);
        int y0 = st0Y+stH/2;
        int x1 = st1X+stW/2;
        int y1 = st1Y+stH/2;

        Graphics2D g = (Graphics2D) ui.getGraphics();
        Stroke prev = g.getStroke();

        g.setColor(Color.yellow);
        g.setStroke(BasePanel.baseStroke(1));

        g.drawLine(x0, y0, x1, y1);

        g.setStroke(prev);

        if (dmg > 0) {
            g.setFont(narrowFont(20));
            String s = "-"+(int)Math.ceil(dmg);
            int sw = g.getFontMetrics().stringWidth(s);
            int x= x1-(sw/2);
            drawBorderedString(g, s, x, y1, Color.white, Color.red);
        }
        sleep(250);
        ui.paintAllImmediately();
        sleep(250);
    }
    private static class ResearchPriorityComparator implements Comparator<Tech> {
    	@Override public int compare(Tech o1, Tech o2) {
            float pr1 = comparatorCiv.ai().scientist().researchPriority(o1);
            float pr2 = comparatorCiv.ai().scientist().researchPriority(o2);
            if (pr1 != pr2)
                return Base.compare(pr2, pr1);
            else
                return Base.compare(o1.level, o2.level);
        }
    }
    private static class ResearchValueComparator implements Comparator<Tech> {
    	@Override
        public int compare(Tech o1, Tech o2) {
            float pr1 = comparatorCiv.ai().scientist().researchValue(o1);
            float pr2 = comparatorCiv.ai().scientist().researchValue(o2);
            if (pr1 != pr2)
                return Base.compare(pr2, pr1);
            else
                return Base.compare(o2.level, o1.level);
        }
    }
    private static class BaseValueComparator implements Comparator<Tech> {
    	@Override public int compare(Tech o1, Tech o2) {
            float pr1 = o1.baseValue(comparatorCiv);
            float pr2 = o2.baseValue(comparatorCiv);
            if (pr1 != pr2)
                return Base.compare(pr2, pr1);
            else
                return Base.compare(o2.level, o1.level);
        }
    }
    private static class WarTradeComparator implements Comparator<Tech> {
    	@Override public int compare(Tech o1, Tech o2) {
            float pr1 = comparatorCiv.ai().scientist().warTradeValue(o1);
            float pr2 = comparatorCiv.ai().scientist().warTradeValue(o2);
            return Base.compare(pr2, pr1);
        }
    }
}
