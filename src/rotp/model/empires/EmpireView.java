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
package rotp.model.empires;

import static rotp.model.empires.EmpireStatus.POWER;

import java.awt.Image;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rotp.model.ai.interfaces.Diplomat;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.model.galaxy.Transport;
import rotp.model.incidents.DiplomaticIncident;
import rotp.model.ships.ShipDesign;
import rotp.model.ships.ShipDesignLab;
import rotp.model.tech.TechTree;
import rotp.ui.diplomacy.DialogueManager;
import rotp.ui.diplomacy.DiplomaticCounterReply;
import rotp.ui.diplomacy.DiplomaticReply;
import rotp.util.Base;

public final class EmpireView implements Base, Serializable {
    private static final long serialVersionUID = 1L;
    private final Empire empire;
    private final Empire owner;
    private final DiplomaticEmbassy embassy;
    private final SpyNetwork spies;
    private final TradeRoute trade;
    private transient EmpireView otherView;

	// Dangerous unlimited calls that returns more than the view from the owner
	public Empire empireUncut()				{ return empire; }		// Viewed
	public TechTree spiedTech()				{ return spies.tech(); } // Should be used instead of
	public TechTree techUncut()				{ return empire.tech(); }
	public ShipDesignLab shipLabUncut()		{ return empire.shipLab(); }
	public ShipDesign[] designsUncut()		{ return empire.shipLab().designs(); }

	// May be a little bit too much - should check if already spied on once.
	// and to return a list of empireId should be safer...
	// public List<StarSystem> allColonizedSystems()	{ return empire.allColonizedSystems(); }
	public List<Empire> allies()					{ return empire.allies(); }
	// Probably too informative... onWarFooting() is not public info
	public List<Empire> enemies()					{ return empire.enemies(); }
	public List<Empire> warEnemies()				{ return empire.warEnemies(); }
	public List<Transport> transports()				{ return empire.transports(); }
	public int numEnemies()							{ return empire.numEnemies(); }
	public int shipDesignCount(int desId)			{ return empire.shipDesignCount(desId); }
	public float missileBaseCostPerBC()				{ return empire.missileBaseCostPerBC(); }
	public float shipMaintCostPerBC()				{ return empire.shipMaintCostPerBC(); }
	public float totalPlanetaryPopulation()			{ return empire.totalPlanetaryPopulation(); }
	public float totalPlanetaryProductionUncut()	{ return empire.totalPlanetaryProduction(); }
	public float powerLevelUncut()					{ return empire.powerLevel(empire); }
	public float militaryPowerLevelUncut()			{ return empire.militaryPowerLevel(); }
	public boolean inShipRange(int empId)			{ return empire.inShipRange(empId); }
	public boolean alliedWith(int empId)			{ return empire.alliedWith(empId); }

	// To be reviewed
	public int numSystemsForIncidents()				{ return empire.numSystemsForCiv(empire.id); }
	public int numSystemsForCiv()					{ return owner.numSystemsForCiv(empire.id); }

	// These calls are fair
    public Empire owner()                 { return owner; }		// Viewer
    public int empId()                    { return empire.id; }	// Viewed
    public int ownerId()                  { return owner.id; }	// Viewer
    public DiplomaticEmbassy embassy()    { return embassy; }
    public SpyNetwork spies()             { return spies; }
    public TradeRoute trade()             { return trade; }
    public EmpireStatus status()          { return empire.status(); }
    public String raceName()              { return empire.raceName(); }
    public Leader leader()                { return empire.leader(); }
    public boolean extinct()              { return empire.extinct(); }
    public int homeSysId()                { return empire.homeSysId(); }
    public boolean isPlayer()             { return empire.isPlayer(); }
    public boolean isHumanEmpire()        { return empire.isHumanEmpire(); }
    public boolean isPlayerControlled()   { return empire.isPlayerControlled(); }

    void clearDataForExtinctEmpire(int id)		{ empire.clearDataForExtinctEmpire(id); }
	public boolean is(Empire emp)				{ return empire == emp; }
	public boolean wantToBreakTreaty()			{ return embassy.treaty().wantToBreak(empire); }
	public boolean isMember(List<Empire> l)		{ return l != null && l.contains(empire); }
	public boolean withinRange(int i, float r)	{ return empire.sv.withinRange(i, r); }
	public String dialogue(String key)			{ return empire.dialogue(key); }
	public float internalSecurityAdj()			{ return empire.internalSecurityAdj(); }
	public Diplomat diplomatAI()				{ return empire.diplomatAI(); }
	public float tradePctBonus()				{ return empire.tradePctBonus(); }
	public float diplomacyBonus()				{ return empire.diplomacyBonus(); }
	public float militaryPowerLevel()			{ return owner().militaryPowerLevel(empire); }
	public float powerLevel()					{ return owner().powerLevel(empire); }
	public List<ShipFleet> empireFleets()		{ return owner().fleetsForEmpire(empire); }
	public List<StarSystem> empireSystems()		{ return owner().systemsForCiv(empire.id); }
	public int empireNumSystems()				{ return owner().numSystemsForCiv(empire.id); }
	public int lastCouncilVoteEmpId()			{ return empire.lastCouncilVoteEmpId(); }

	EmpireView viewForEmpire(Empire e)	{ return empire.viewForEmpire(e); }

    public EmpireView otherView() {
        if (otherView == null)
            otherView = empire.viewForEmpire(owner);
        return otherView;
    }
	void validateOnLoad() { // For backward compatibility
		embassy.validateOnLoad();
		if (owner().isHumanEmpire()) // Variables not used by AI
			spies.validateOnLoad();
	}
    EmpireView(Empire o, Empire c) {
        empire = c;
        owner = o;
        spies = new SpyNetwork(this);
        trade = new TradeRoute(this);
        embassy = new DiplomaticEmbassy(this);
        setSuggestedAllocations();
    }
    @Override
    public String toString()   { return concat(owner.raceName(), " View of: ", empire.raceName()); }
    
    private Integer listOrder() {
        int rangeMod = inEconomicRange() ? 0 : 100;
        return rangeMod + embassy().treaty().listOrder();
    }
    public boolean diplomats() {
        return !embassy.diplomatGone() && !otherView().embassy.diplomatGone() && !embassy.finalWar();
    }
	public Image flag() {
		if (embassy().anyWar())
			return empire.flagWar();
		else if (embassy().isFriend())
			return empire.flagPact();
		else
			return empire.flagNorm();
	}
	public Image dialogueBox() {
		if (embassy().anyWar())
			return owner().dialogWar();
		else if (embassy().isFriend())
			return owner().dialogPact();
		else
			return owner().dialogNorm();
	}
    public float scaleOfContempt() {
        // returns 0 if equal power
        // returns 1, 2, 3 if we are 2x,3x,4x more powerful
        // returns -1,-2,-3 if we are 1/2x, 1/3x, 1/4x as powerful
        float ourPower = empireWeakness();
        if (ourPower == 1)
            return 0;
        else if (ourPower > 1)
            return ourPower-1;
        else
            return -(1/ourPower)+1;
    }
    private float empireWeakness() { return 1 / empirePower(); }
    public float empirePower() {
        // This returns the estimated strength of the view.civ vs. the view.owner
        // > 1 means empire is STRONGER than view.owner
        // < 1 means empire is WEAKER than view.owner
        float num = empire.status().lastViewValue(owner, POWER);
        float den = owner.powerLevel(owner);
        //xilmi: When we don't know anything about the other empire, we assume it's as strong as we are
        if(num == 0)
            num = den;
        return num / den;
    }
    void refresh() {
        if (!embassy.contact()) {
            float range = owner.shipRange();
            for (int id=0; id<owner.sv.count();id++) {
                if ((owner.sv.empire(id) == empire) && owner.sv.withinRange(id, range))
                    setContact();
            }
        }
        if (!embassy.contact())
            return;
    }
    public boolean inEconomicRange() { return owner().inEconomicRange(empId()); }
    void breakAllTreaties() {
        trade.stopRoute();
        embassy.resetTreaty();
    }
    public List<Integer> nominalTradeLevels() {
        float prod = min(empire.totalPlanetaryProduction(), owner.totalPlanetaryProduction());
        float currTrade = trade.level();

        List<Integer> values = new ArrayList<>();
        int maxOptions = 4;
        float maxPct = 0.25f;
        float lvlPct = maxPct/maxOptions;
        for (int i=1;i<=maxOptions;i++) {
            float pct = lvlPct*i;
            Integer level = 25* ((int) (pct * prod/25));
            if ((level > currTrade) && !values.contains(level))
                values.add(level);
        }
        return values;
    }
    public boolean timerIsActive() {
    	return otherView() != null 
        		&& otherView().embassy() != null
        		&& otherView().embassy().timerIsActive(DiplomaticEmbassy.TIMER_SPY_WARNING);
    }
	// BR: moved Governor to GovernorAI
	void setSuggestedAllocations()	{ spies.setSuggestedAllocations(); }
    void setContact() {
        if (owner.extinct() || empire.extinct())
            return;
        // when civ is within propulsion range
        if (!owner.hasContacted(empire.id))
            embassy.makeFirstContact();
        else
            embassy.setContact();

        trade.setContact();

        if (!otherView().embassy.contact())
            otherView().setContact();

        setSuggestedAllocations();
    }
    void refreshSystemSpyViews() {
        // this method is called once during the SpyNetwork nextTurn method
        // it is responsible for updating the spy views for an empire

        // if in unity, it refreshes views for all of the empire's colonies

        // all other cases, it only refreshes the views for the empire's 
        // known colonies, PLUS one additional previously-unknown colony
        // per spy network. Unknown colonies closest in distance to this
        // empire are learned first

        // get all #empire views AND all views we think belong to #empire
        // this keeps us from sabotaging #empire colonies that have been destroyed
        List<StarSystem> allKnownSystems = owner.systemsForCiv(empire.id);

        // build list of unknown systems  (all minus known)
        List<StarSystem> allUnknownSystems = new ArrayList<>(empire.allColonizedSystems());
        allUnknownSystems.removeAll(allKnownSystems);

        // BR: If trade treaty: systems in range are known from the traders
        if (!allUnknownSystems.isEmpty() && trade().active()) {
        	float scoutRange = owner.scoutRange();
        	for (StarSystem sys : allUnknownSystems)
        		if (owner.distanceTo(sys) <= scoutRange)
        			allKnownSystems.add(sys);
            allUnknownSystems.removeAll(allKnownSystems);
        }
 
        // if there are some unknown, all add if unity else sort by distance
        // and add 1 for each spy network
        if (!allUnknownSystems.isEmpty()) {
        	if (embassy().unity() || embassy().alliance()) 
                allKnownSystems.addAll(allUnknownSystems);
            else {
                int spyNetworks = spies().activeSpies().size();
                int numDiscoveredSystems = min(spyNetworks, allUnknownSystems.size());
                if (numDiscoveredSystems > 0) {
                    StarSystem.TARGET_EMPIRE = owner;
                    Collections.sort(allUnknownSystems, StarSystem.DISTANCE_TO_TARGET_EMPIRE);
                    for (int i=0;i<numDiscoveredSystems;i++)
                        allKnownSystems.add(allUnknownSystems.get(i));
                }
            }
        }

        for (StarSystem sys : allKnownSystems)
            owner.sv.refreshSpyScan(sys.id);
    }
    void nextTurn(float prod, float spyMod) {
        log(this+": nextTurn");
        if (empire.extinct())
            return;

        embassy.nextTurn(prod);
        spies.nextTurn(prod, spyMod);
    }
    void makeDiplomaticOffers() {
        log(this+": makeDiplomaticOffers");
        if (owner.isAIControlled())
            owner.diplomatAI().makeDiplomaticOffers(this);
    }
    public String decode(String s) {
        String s1 = owner.replaceTokens(s, "my");
        s1 = empire.replaceTokens(s1, "your");
        return s1;
    }
    private String decode(String s, Empire other) {
        String s1 = owner.replaceTokens(s, "my");
        s1 = empire.replaceTokens(s1, "your");
        if (other != null)
            s1 = other.replaceTokens(s1, "other");
        return s1;
    }
    private String decodedMessage(String key) {
        return decode(DialogueManager.current().randomMessage(key, this));
    }
    private String decodedMessage(String key, Empire speaker, Empire otherEmp) {
        return decode(DialogueManager.current().randomMessage(key, speaker), otherEmp);
    }
    private String decodedMessage(String key, Empire otherEmp) {
        return decode(DialogueManager.current().randomMessage(key, this), otherEmp);
    }
    private String decodedMessage(String key, DiplomaticIncident inc) {
        return decode(inc.decode(DialogueManager.current().randomMessage(key, this)));
    }
    public DiplomaticReply refuse(String reason) {
        return DiplomaticReply.answer(false, decodedMessage(reason));
    }
    public DiplomaticReply refuse(String reason, Empire otherEmp) {
        return DiplomaticReply.answer(false, decodedMessage(reason, otherEmp));
    }
    public DiplomaticReply accept(String reason) {
        return DiplomaticReply.answer(true, decodedMessage(reason));
    }
    public DiplomaticReply accept(String reason, Empire speaker) {
        return DiplomaticReply.answer(true, decodedMessage(reason, speaker, null));
    }
    public DiplomaticReply accept(String reason, DiplomaticIncident inc) {
        return DiplomaticReply.answer(true, decodedMessage(reason, inc));
    }
    public DiplomaticReply counter(String reason, Empire target, List<String> techs, float bribeAmt) {
        return DiplomaticCounterReply.answer(true, decodedMessage(reason, target), id(target), techs, bribeAmt);
    }
    public static Comparator<EmpireView> PLAYER_LIST_ORDER = (EmpireView o1, EmpireView o2) -> o1.listOrder().compareTo(o2.listOrder());
    public static Comparator<EmpireView> BY_RACENAME       = (EmpireView o1, EmpireView o2) -> o1.empire.raceName().compareTo(o2.empire.raceName());
}
