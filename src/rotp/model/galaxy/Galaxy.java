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

import static rotp.util.ObjectCloner.deepCopy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import rotp.Rotp;
import rotp.model.colony.Colony;
import rotp.model.combat.ShipCombatManager;
import rotp.model.empires.Empire;
import rotp.model.empires.Empire.EmpireBaseData;
import rotp.model.empires.GalacticCouncil;
import rotp.model.empires.Race;
import rotp.model.events.RandomEvents;
import rotp.model.galaxy.StarSystem.SystemBaseData;
import rotp.model.game.DynOptions;
import rotp.model.game.GameSession;
import rotp.model.game.IDebugOptions;
import rotp.model.game.IGameOptions;
import rotp.ui.NoticeMessage;
import rotp.ui.UserPreferences;
import rotp.ui.notifications.AdviceNotification;
import rotp.ui.notifications.BombardSystemNotification;
import rotp.ui.vipconsole.VIPConsole;
import rotp.util.Base;
import rotp.util.Rand;

public final class Galaxy implements Base, Serializable {
    private static final long serialVersionUID = 1L;
    public static Galaxy current()   { return GameSession.instance().galaxy(); }
    public static final float TIME_PER_TURN = 1;
    public static final String EMPIRES_KEY	= "SETUP_EMPIRE_LIST";
    public static final String SYSTEMS_KEY	= "SETUP_SYSTEM_LIST";
    
    private float currentTime = 0;
    private final GalacticCouncil council = new GalacticCouncil();
    private final RandomEvents events = new RandomEvents();
    public final Ships ships = new Ships();
    private final StarSystem[] starSystems;
    private List<Nebula> nebulas;
    private final Empire[] empires;
    private final List<String> adviceGiven = new ArrayList<>();
    private final List<Transport> transports = new ArrayList<>();
    private final List<StarSystem> abandonedSystems = new ArrayList<>();

    private Empire playerEmpire;
	private Empire orionEmpire;
    private final int widthLY;
    private final int heightLY;
    private float maxScaleAdj = 1.0f;
    public int systemCount = 0;

    private DynOptions dynamicOptions = new DynOptions();// BR: Dynamic options
    private Integer lastHashCodeDiplomaticIncident	= 0;
    private Integer lastHashCodeShipDesign			= 0;
    private Integer lastHashCodeDesign				= 0;
    private Integer lastHashCodeShip				= 0;
    private Random	permRandom; // BR: for backward compatibility
    private Rand	galRandom = rng(); // BR: to memorize RNG state
    Boolean restartedGame	= false; // BR: To help debug
    Boolean swappedPositions		= false; // BR: To help debug
    private boolean	hadLivePlayerSwap; // BR: History in case of strange bugs.
    private boolean	requestToSwapPlayer;
    private int		requestedSwapPlayer;
    boolean	ironmanLockedOptions;

    public	Integer nextHashCodeDiplomaticIncident() {
    	if (lastHashCodeDiplomaticIncident!=null)
    		lastHashCodeDiplomaticIncident++;
    	return lastHashCodeDiplomaticIncident;
    }
    public	Integer nextHashCodeShipDesign() {
    	if (lastHashCodeShipDesign!=null)
    		lastHashCodeShipDesign++;
    	return lastHashCodeShipDesign;
    }
    public	Integer nextHashCodeDesign() {
    	if (lastHashCodeDesign!=null)
    		lastHashCodeDesign++;
    	return lastHashCodeDesign;
    }
    public	Integer nextHashCodeShip() {
    	if (lastHashCodeShip!=null)
    		lastHashCodeShip++;
    	return lastHashCodeShip;
    }

    private transient ShipCombatManager shipCombat = new ShipCombatManager();
    private transient Map<String, List<String>> raceSystemNames = new HashMap<>();
    private transient Map<String, Integer> raceSystemCtr = new HashMap<>();
    private transient List<SpaceMonster> spaceMonsters;
    
    public Empire orionEmpire()				 {
    	if (orionEmpire == null)
    		orionEmpire = new Empire(this, -2, orionId(), 0, "Orion");
    	return orionEmpire;
    }
    public void backupStarSystem()			 {
    	dynamicOptions.setObject(SYSTEMS_KEY, (Serializable) deepCopy(starSystems));
    }
    public StarSystem[] originalStarSystem() {
    	StarSystem[] originalStarSystem = (StarSystem[]) dynamicOptions.getObject(SYSTEMS_KEY);
    	if (originalStarSystem == null)
    		return starSystems();
    	return originalStarSystem;
    }
	public DynOptions dynamicOptions()			{ return dynamicOptions; } // BR:
	public int beginningYear()					{ return player().startingYear(); }
    public float currentTime()               { return currentTime; }
    public GalacticCouncil council()         { return council; }
    public RandomEvents events()       		 { return events; }
    public List<Nebula> nebulas()            { return nebulas; }
    public Empire[] empires()                { return empires; }
    public int maxNumStarSystems()           { return starSystems.length; }
    public int numStarSystems()              { return systemCount; }
    public StarSystem[] starSystems()        { return starSystems; }
    public List<StarSystem> abandonedSystems() { return abandonedSystems; }
    public StarSystem orionSystem() 		 { return system(orionId()); }
    public int orionId() 				     {
    	IGameOptions opts = options();
    	int numCompWorlds = opts.selectedCompanionWorlds();
    	int numNearbySys  = opts.secondRingSystemNumber();
    	int orionId = numEmpires() * (1 + numCompWorlds + numNearbySys);
    	return orionId;
    }
    public void addStarSystem(StarSystem s)  {
        starSystems[systemCount] = s;
        // Init Orion Guardian, and maybe future little guardian monsters
        if (s.hasMonster())
        	s.monster().setEmpireSystem(systemCount, s);
        systemCount++;
        
        Nebula neb = nebulaContaining(s);
        s.inNebula(neb != null);
        if (neb != null)
            neb.noteStarSystem(s);
    }
    public StarSystem system(int i)          { return (i < 0) || (i >= numStarSystems()) ? null : starSystems[i]; }
	// BR: for debug purpose
	public StarSystem system(String name)	 {
		for (StarSystem sys : starSystems)
			if (sys.name().equals(name))
				return sys;
		return null;
	}
	// BR: for debug purpose
	public int systemId(String name)		 {
		for (StarSystem sys : starSystems)
			if (sys.name().equals(name))
				return sys.id;
		return -1;
	}

    public int width()                       { return widthLY; }
    public int height()                      { return heightLY; }
    public float maxScaleAdj()               { return maxScaleAdj; }

	// For debug only
	public void playerSwapRequest(int id)	 {
		requestToSwapPlayer = true;
		requestedSwapPlayer = id;
	}
	public boolean playerSwapRequest()	 	 { return requestToSwapPlayer; }
	public boolean swapPlayerEmpire()		 { return swapPlayerEmpire(requestedSwapPlayer); }
	public boolean swapPlayerEmpire(int id)	 {
		if (ironmanLockedOptions) {
			misClick();
			return false;
		}
		Empire newPlayerEmpire = empire(id);
		if (newPlayerEmpire.extinct()) {
			System.err.println("!!!!!! Selected Empire is extinct !!!!!!");
			requestToSwapPlayer = false;
			return false;
		}
		// update current options (may have been asked out of the game)
		options().debugPlayerEmpire(id);
		requestToSwapPlayer = false;
		hadLivePlayerSwap   = true;

		Empire oldPlayerEmpire = playerEmpire;
		int oldPlayerAI = playerEmpire.selectedAI;
		Empire.PLAYER_ID = id;
		playerEmpire = newPlayerEmpire;
		oldPlayerEmpire.resetAI();
		oldPlayerEmpire.selectedAI = playerEmpire.selectedAI;
		playerEmpire.resetAI();
		playerEmpire.selectedAI = oldPlayerAI;
		playerEmpire.resetDivertColonyExcessToResearch();
		return true;
	}
	public String getEmpireList()	{
		if (ironmanLockedOptions)
			return text("SETTINGS_MOD_DEBUG_PLAYER_EMPIRE_IRONMAN");

		String str = " (" + text("SETTINGS_MOD_DEBUG_PLAYER_EMPIRE", playerEmpire.id)+")";
		for (Empire emp : empires()) {
			str += NEWLINE;
			str += "(" + emp.id + ") " + emp.name();
			if (emp.extinct())
				str +=  " -> " + text("HISTORY_EXTINCT");
		}
		return str;
	}

    public void player(Empire d)             { playerEmpire = d; }
    @Override
    public Empire player()                   { return playerEmpire; }
    @Override
    public boolean isPlayer(Empire d)        { return playerEmpire == d; }
    public void initNebulas(int size)        { nebulas = new ArrayList<>(size); }
    //public void initNebulas(List<Nebula> nebulas) { this.nebulas = nebulas; } // BR: For Restart with new options
    public ShipCombatManager shipCombat() {
        if (shipCombat == null)
            shipCombat = new ShipCombatManager();
        return shipCombat;
    }
    private Map<String, List<String>> raceSystemNames() {
        if (raceSystemNames == null)
            raceSystemNames = new HashMap<>();
        return raceSystemNames;
    }
    private Map<String, Integer> raceSystemCtr() {
        if (raceSystemCtr == null)
            raceSystemCtr = new HashMap<>();
        return raceSystemCtr;
    }
    public Galaxy(GalaxyBaseData src) { // BR: For Restart
        widthLY		= src.width;
        heightLY	= src.height;
        maxScaleAdj	= src.maxScaleAdj;
        starSystems	= new StarSystem[src.numStarSystems];
        empires		= new Empire[src.numEmpires];
    }
    public Galaxy(GalaxyShape sh) {
        widthLY = sh.width();
        heightLY = sh.height();
        maxScaleAdj = sh.maxScaleAdj();
        starSystems = new StarSystem[sh.totalStarSystems()];
        empires = new Empire[sh.numEmpires()];
    }
    public void advanceTime() { currentTime += TIME_PER_TURN; }
    public void resetAllAI() { // BR: needed when updating fuelRange
    	for (Empire emp : empires)
    		emp.resetAI();
    }
    // BR: For Restart with new options
    public void addNebula(Nebula nebula, float nebSize) {
    	Nebula neb = nebula.copy();
        neb.setXY(nebula.x(), nebula.y());
        nebulas.add(neb);    	
    }
    public List<StarSystem> systemsNamed(String name) {
        List<StarSystem> systems = new ArrayList<>();
        for (StarSystem sys: starSystems) {
            if (sys != null && sys.name().equals(name))
                systems.add(sys);
        }
        return systems;
    }
    private void addAdviceGiven(String key) {
        if (!adviceGiven.contains(key))
            adviceGiven.add(key);
    }
    private boolean adviceAlreadyGiven(String key) {
        return adviceGiven.contains(key) || options().isAutoPlay();
    }
    public void giveAdvice(String key) {
        if (!adviceAlreadyGiven(key)) {
            addAdviceGiven(key);
            if(!UserPreferences.disableAdvisor())
                AdviceNotification.create(key);
        }
    }
    public void giveAdvice(String key, Empire e1, String s1) {
        if (!adviceAlreadyGiven(key)) {
            addAdviceGiven(key);
            AdviceNotification.create(key, e1, s1);
        }
    }
    public void giveAdvice(String key, String s1) {
        if (!adviceAlreadyGiven(key)) {
            addAdviceGiven(key);
            AdviceNotification.create(key, s1);
        }
    }
    /*public void giveAdvice(String key, String s1, String s2) {
        if (!adviceAlreadyGiven(key)) {
            addAdviceGiven(key);
            AdviceNotification.create(key, s1, s2);
        }
    }*/
    public void giveAdvice(String key, String s1, String s2, String s3) {
        if (!adviceAlreadyGiven(key)) {
            addAdviceGiven(key);
            AdviceNotification.create(key, s1, s2, s3);
        }
    }
    private Nebula nebulaContaining(IMappedObject obj) {
        float x = obj.x();
        float y = obj.y();
        for (Nebula neb: nebulas) {
            if (neb.contains(x,y))
                return neb;
        }        
        return null;
    }
    public Empire empireMatching(int color, int shape) {
        for (Empire e: empires) {
            if ((e.colorId() == color) && (e.shape() == shape))
                return e;
        }
        return null;
    }
    public void preNextTurn() {
        NoticeMessage.resetSubstatus(text("TURN_LAUNCHING_FLEETS"));
        for (StarSystem sys: starSystems)
        	if(sys != null)
        		sys.launchTransports();
        ships.launchFleets(); // Launch deployed fleets tagged to be launched. (But not the rallied ones)
        ships.reloadBombs();
        for (StarSystem sys: starSystems)
        	if(sys != null && sys.colony() != null)
        		sys.colony().shipyard().clearFleetsCopy();
    }
    public void postNextTurn1() {
        // check ship combat & invasions at each system
        NoticeMessage.resetSubstatus(text("TURN_SHIP_COMBAT"));
        Galaxy gal = galaxy();
        for (int i=0; i<gal.numStarSystems(); i++) {
            gal.system(i).resolveAnyShipConflict();
        }
    }
    public void refreshAllEmpireViews() {
        NoticeMessage.setSubstatus(text("TURN_REFRESHING"));
        for (Empire e: empires)
        {
            e.refreshViews(false);
            e.setVisibleShips();
        }
    }
    public void postNextTurn2() {
        // check bombardment
        NoticeMessage.resetSubstatus(text("TURN_BOMBARDMENT"));
        checkForPlanetaryBombardment();

        // land transports
        Galaxy gal = galaxy();
        NoticeMessage.resetSubstatus(text("TURN_TRANSPORTS"));
        for (int i=0; i<gal.numStarSystems(); i++)
            gal.system(i).resolvePendingTransports();

        NoticeMessage.resetSubstatus(text("TURN_COLONIES"));
        // after bombardments, check for any possible colonizations
        checkForColonization();
        
        // BR: Removed refreshViews from addColonizedSystem and removeColonizedSystem
        // the recalcDistances takes time and don't need to be called at each events
        // Grouped here. Only Removed refreshViews will be executed.
//        for (Empire e: empires)
//            e.refreshViews(true);

        NoticeMessage.resetSubstatus(text("TURN_SPIES"));
        for (Empire e: empires)
            e.postNextTurn();

        NoticeMessage.resetSubstatus(text("TURN_REBELLION"));
        for (Empire e: empires)
            e.checkForRebellionSpread();

        NoticeMessage.resetSubstatus(text("TURN_COUNCIL"));
        council().checkIfDisband();
    }
    public void postNextTurn3() {
        // BR: Removed refreshViews from addColonizedSystem and removeColonizedSystem
        // the recalcDistances takes time and don't need to be called at each events
        // Grouped here. Only Removed refreshViews will be executed.
        for (Empire e: empires)
            e.refreshViews(true);  	
    }
    private void checkForPlanetaryBombardment() {
        for (StarSystem sys: starSystems) {
        	if(sys != null) {
	            Empire home = sys.empire();
	            List<ShipFleet> fleets = sys.orbitingFleets();
	            if ((home != null) && !fleets.isEmpty()){
	                for (ShipFleet fl: fleets) {
	                    if (fl != null && fl.isOrbiting() && !fl.retreating()) {
	                        Empire fleetEmp = fl.empire();
	                        // cannot bombard if alliance or unity
	                        if (fleetEmp.aggressiveWith(home.id)) {
	                            int promtResult = fleetEmp.ai().promptForBombardment(sys, fl);
	                            if (promtResult == 1)
	                            {
	                                BombardSystemNotification.create(sys.id, fl, true, 0);
	                            }
	                            else if(promtResult > 1)
	                            {
	                                BombardSystemNotification.create(sys.id, fl, true, 1);
	                            }
	                        }
	                    }
	                }
	            }
        	}
        }
    }
    private void checkForColonization() {
        for (StarSystem sys: starSystems) {
            Empire home = sys.empire();
            List<ShipFleet> fleets = sys.orbitingFleetsNoMonster();
            if ((home == null) && !fleets.isEmpty()){
                for (ShipFleet fl: fleets) 
                    fl.checkColonize();
            }
        }
    }
    public void assessTurn() {
        NoticeMessage.resetSubstatus(text("TURN_RESEARCH"));
        for (Empire e: empires) {
        	e.completeResearch();
        }
        // everything that can have happened in the turn has happened
        // now it's time for empires to decide what to do about it
        // warn, praise, break treaties or declare war, generally
        NoticeMessage.resetSubstatus(text("TURN_ASSESS"));
        for (Empire e: empires)
            e.assessTurn();
        ships.disembarkRalliedFleets();
        NoticeMessage.resetSubstatus(text("TURN_DIPLOMACY"));
        for (Empire e: empires)
            e.makeDiplomaticOffers();
        NoticeMessage.resetSubstatus(text("TURN_ACQUIRE_TECHS"));
        for (Empire e: empires)
            e.acquireTradedTechs();
    }
    public void refreshEmpireStatus()	{ // BR: was not up to date at the beginning of turns
    	for (Empire e: empires)
            e.status().assessTurn();
    }
    public void makeNextTurnDecisions() {
        int num = empires.length;
        for (int i=0;i<num;i++) {
            NoticeMessage.setSubstatus(text("TURN_MAKE_DECISIONS"), (i+1), num);
            Empire emp = empires[i];
            if(!emp.extinct())
                emp.makeNextTurnDecisions();
        }
    }
	public void startAlwaysAlly()	{
		for (Empire emp: empires())
			if (!emp.extinct())
				emp.startAlwaysAlly();
	}
	public void startAlwaysAtWar()	{
		for (Empire emp: empires())
			if (!emp.extinct())
				emp.startAlwaysAtWar();
	}
	public void validateOnLoad()	{
    	if (dynamicOptions == null)
    		dynamicOptions = new DynOptions();
    	if (galRandom == null) // For backward compatibility
    		if (permRandom == null)
    			galRandom = rng();
    		else
    			galRandom = new Rand(permRandom.nextLong());
    	if (restartedGame == null) {
    		restartedGame = false;
    		swappedPositions = false;
    	}
    	else if (restartedGame) {
        	System.out.println("!!! Restarted Game");
    		if (swappedPositions)
        		System.out.println("!!! Swapped Positions");
    	}
    	if (options().persistentRNG())
    		Rotp.rand(galRandom);
    	orionEmpire = new Empire(this, -2, orionId(), 0, "Orion"); // to update tech
        for (Empire emp: empires())
             emp.validateOnLoad();
        events.validateOnLoad();
        player().setVisibleMonsters();
        // Gives Guardian monster their systems
    	for (StarSystem sys : starSystems())
    		if (sys != null && sys.hasMonster()) {
    			SpaceMonster monster = sys.monster();
    			if (monster.isOrionGuardian()) {
    				monster.sysId(sys.id);
    				monster.setXY(sys);
    			}
    		}
		// Transport don't have negative size on surrender
		for (Transport tr : transports)
			tr.validateOnLoad();

		ironmanLockedOptions = !options().isGameOptionsAllowed();
		Empire.updatePlayerId(player().id);

		// Console Info to help debug
		if (hadLivePlayerSwap)
			System.err.println("Warning: This save once had the player swapped");
		if (player().id != Empire.DEFAULT_PLAYER_ID)
			System.out.println("Player Empire Id: " + player().id);
	}
    public void validate() {
        for (Empire emp: empires())
            emp.validate();
    }
    public int currentYear() {
        return beginningYear() + (int) currentTime;
    }
    public int numberTurns() { return (int) currentTime; }
    public int currentTurn() { return (int) currentTime+1; }

    public Empire empire(int i)     {
        return (i < 0) || (i >= empires.length) ? null : empires[i];
    }
    public void addEmpire(Empire e) {
        empires[e.id] = e;
    }
    public Empire empireForRace(Race r) {
        for (Empire e: empires) {
            if (e.isRace(r))
                return e;
        }
        return null;
    }
    public Empire empireNamed(String s) {
        for (Empire e: empires) {
            if (e.raceName().equals(s))
                return e;
        }
        return null;
    }
    public void startGame() {
        if (IDebugOptions.debugShowMoreMemory()) {
            memLog();
            // RotPUI.instance().mainUI().showMemoryLowPrompt(); // TO DO BR: Comment
        }
        if (IDebugOptions.selectedShowVIPPanel())
        	VIPConsole.updateConsole();
        giveAdvice("MAIN_ADVISOR_SCOUT");
        session().processNotifications();
    }
    public void moveShipsInTransit() {
        // move transports
        List<Transport> arrivingTransports = new ArrayList<>();
        List<Transport> incoming = new ArrayList<>(transports);
        Collections.sort(incoming, Ship.ARRIVAL_TIME);
        for (Transport sh: incoming) {
	       	if (sh != null) {
	            if (sh.arrivalTimeAdjusted() > currentTime)
	                break;
	            arrivingTransports.add(sh);
	            sh.arrive();
	       	}
        }
        transports.removeAll(arrivingTransports);
        
        //move fleets
        List<ShipFleet> incomingFleets = ships.inTransitFleets();
        Collections.sort(incomingFleets, Ship.ARRIVAL_TIME);
        for (ShipFleet sh: incomingFleets) {
        	if (sh != null) {
	            if (sh.arrivalTimeAdjusted() > currentTime)
	                break;
	            ships.arriveFleet(sh);
        	}
        }

        // BR: Merge and Memorize copy of fleets in case of combat
        IGameOptions opts = options();
        if (opts.rallyPassByCombat() || !opts.rallyLossDefense()) {
        	for (StarSystem sys : player().allColonizedSystems()) {
        		ships.mergeRallyAndOrbitFleets(sys);
        	}
        }
    }
    public void clearSpaceMonsters()			{ spaceMonsters = null; }
    public List<SpaceMonster> spaceMonsters()	{
    	// spaceMonsters = null;
    	if (spaceMonsters == null) {
        	spaceMonsters = events.monsters();
        	for (StarSystem sys : starSystems())
        		if (sys != null && sys.hasMonster())
        			if (sys.monster().isOrionGuardian())
        				spaceMonsters.add(sys.monster());
    	}
    	return spaceMonsters;
    }
    public List<Transport> transports()       { return transports; }
    public void removeTransport(Transport sh) { transports.remove(sh); }
    public void addTransport(Transport sh)    { transports.add(sh); }
    public void removeAllTransports(int empId) {
        List<Transport> allTransports = new ArrayList<>(transports);
        for (Transport tr: allTransports) {
            if (tr != null && tr.empId() == empId)
                transports.remove(tr);
        }
    }
    public void nextEmpireTurns() {
        for (Empire e: empires) {
            if (!e.extinct())
                e.nextTurn();
        }
    }
    public int numColonizedSystems() {
        int num = 0;
        for (Empire e: empires) 
            num += e.numColonizedSystems();       
        return num;
    }
    public int friendlyPopApproachingSystem(StarSystem sys) {
        int pop = 0;
        Galaxy gal = galaxy();

        for (Transport tr: gal.transports()) {
            if (tr != null && tr.empId() == sys.empire().id) {
                if (tr.destSysId() == sys.id)
                    pop += tr.size();
            }
        }
        for (int i=0; i<gal.numStarSystems(); i++) {
            StarSystem system = gal.system(i);
            if (system.planet().isColonized()) {
                Colony col = system.planet().colony();
                if ((col.empire() == sys.empire()) && col.transporting() && (col.transport().destSysId() == sys.id))
                    pop += col.inTransport();
            }
        }
        return pop;
    }
    public int playerPopApproachingSystem(StarSystem sys) {
        int pop = 0;
        Galaxy gal = galaxy();

        for (Transport tr: gal.transports()) {
            if (tr != null && tr.empId() == player().id) {
                if (tr.destSysId() == sys.id)
                    pop += tr.size();
            }
        }
        for (StarSystem system : player().allColonizedSystems()) {
        	if (system != null) {
        		Colony col = system.planet().colony();
        		pop += col.plannedTransport(sys.id);
        	}
        }
        return pop;
    }
    public int friendlyPopApproachingSystemNextTurn(StarSystem sys) {
        int pop = 0;
        Galaxy gal = galaxy();

        for (Transport tr: gal.transports()) {
            if (tr != null && tr.empId() == sys.empire().id) {
                if (tr.destSysId() == sys.id && tr.travelTurnsRemainingAdjusted() <= 1)
                    pop += tr.size();
            }
        }
        for (int i=0; i<gal.numStarSystems(); i++) {
            StarSystem system = gal.system(i);
            if (system.planet().isColonized()) {
                Colony col = system.planet().colony();
                if ((col.empire() == sys.empire()) && col.transporting() && (col.transport().destSysId() == sys.id)) {
                    if (col.transport().travelTurnsRemainingAdjusted() <= 1) {
                        pop += col.inTransport();
                    }
                }
            }
        }
        return pop;
    }
    public int enemyPopApproachingPlayerSystem(StarSystem sys) {
        int pop = 0;
        Empire pl = player();
        for (Transport sh: transports) {
            if ( sh != null && (sh.destSysId() == sys.id)
            		&& (sh.empId() != pl.id)
            		&& pl.knowETA(sh))
                pop += sh.size();
        }
        return pop;
    }
    public int enemyPopApproachingSystem(StarSystem sys) {
        int pop = 0;
        for (Transport sh: transports) {
            if ( (sh != null) &&  (sh.destSysId() == sys.id)
            		&& (sh.empId() != sys.empire().id))
                pop += sh.size();
        }
        for (int i=0; i<numStarSystems(); i++) {
            StarSystem system = system(i);
            if (system.planet().isColonized()) {
                Colony col = system.planet().colony();
                if ((col.empire() != sys.empire()) && col.transporting() && (col.transport().destSysId() == sys.id))
                    pop += col.inTransport();
            }
        }
        return pop;
    }
    public int popApproachingSystem(StarSystem sys, Empire emp) {
        int pop = 0;

        for (Transport tr: transports()) {
            if (tr != null && (tr.empire() == emp) && (tr.destSysId() == sys.id))
               pop += tr.size();
        }
        for (StarSystem system : emp.allColonizedSystems()) {
        	if (system != null) {
	            Colony col = system.colony();
	            if (col != null) {
	                if (col.transporting() && (col.transport().destSysId() == sys.id))
	                    pop += col.inTransport();
	            }
        	}
        }
        return pop;
    }
    public int numEmpires()       { return empires.length; }
    public int numOpponents()     { return empires.length-1; }
    public int numActiveEmpires() {
        int emps = 0;
        for (Empire e : empires) {
            if (!e.extinct())
                emps++;
        }
        return emps;
    }
    public List<Empire> activeEmpires() {
        List<Empire> emps = new ArrayList<>();
        for (Empire e : empires) {
            if (!e.extinct())
                emps.add(e);
        }
        return emps;
    }
    public boolean allAlliedWithPlayer() {
        List<Empire> activeEmpires = activeEmpires();
        int playerId = player().id;
        for (Empire emp: activeEmpires) {
            if (!emp.alliedWith(playerId))
                return false;
        }
        return true;
    }
    public List<StarSystem> systemsInRange(IMappedObject xyz, float radius) {
        List<StarSystem> systems = new ArrayList<>();
        for (int i=0;i<numStarSystems();i++) {
            StarSystem s = system(i);
            if (s.distanceTo(xyz) <= radius)
                systems.add(s);
        }
        return systems;
    }
    public String nextSystemName(String rId) {
        if (!raceSystemNames().containsKey(rId))
            loadRaceNames(rId, 0);

        List<String> remainingNames = raceSystemNames().get(rId);
        if (remainingNames.isEmpty()) {
            int nextSeq = raceSystemCtr().get(rId) + 1;
            loadRaceNames(rId, nextSeq);
            remainingNames = raceSystemNames().get(rId);
        }

        String nextName = remainingNames.remove(0);
        int seq = raceSystemCtr().get(rId);
        if (seq > 1)
            nextName = nextName + " " + Base.letter[seq];

        return nextName;
    }
    private void loadRaceNames(String rId, int i) {
        Race r = Race.keyed(rId);
        List<String> names = new ArrayList<>(r.systemNames());
        shuffle(names);
        raceSystemNames().put(rId, names);
        raceSystemCtr().put(rId, i);
    }
    public boolean tooCloseToHomeWorld(StarSystem s, float lim) {
    	for (Empire e : empires) {
    		if (e == null)
    			return false;
    		if (s.distanceTo(system(e.homeSysId())) < lim)
    			return true;
    	}
    	return false;
    }
    public int techDiscoveryEmpireId(String id) {
    	for (Empire e: empires)
    		if(e.tech().knows(id))
    			return e.id;
    	return Empire.NULL_ID;
    }
    public HashMap<String, Integer> eventGNNState()	{ return events().eventGNNState(); }

    // ==================== GalaxyBaseData ====================
    //
	public static class GalaxyBaseData {
		private int width;
		private int height;
		List<Nebula> nebulas;
		private float maxScaleAdj;
		int numCompWorlds;
		int numStarSystems;
		SystemBaseData[] starSystems;
		private int numEmpires;
		EmpireBaseData[] empires;
		
		GalaxyBaseData(Galaxy src) {
			width		= src.width();
			height		= src.height();
			nebulas		= src.nebulas();
			maxScaleAdj	= src.maxScaleAdj();
			numCompWorlds	= src.empire(0).getCompanionWorldsNumber();

			numStarSystems = src.numStarSystems();
			starSystems	   = new SystemBaseData[numStarSystems];
			StarSystem[] originalSystems = src.originalStarSystem();
			for (int i=0; i<src.systemCount; i++ )
				starSystems[i] = new SystemBaseData(originalSystems[i]);

			numEmpires = src.numEmpires();
			empires	   = new EmpireBaseData[numEmpires];
			for (int i=0; i<numEmpires; i++ )
				empires[i] = new EmpireBaseData(src.empire(i), starSystems);
		}
		void swapPlayer(int id) {
			// Swap empires, but keeps AI
			EmpireBaseData player = empires[id];
			EmpireBaseData alien  = empires[0];
			empires[id]	= alien;
			empires[0]	= player;
			int aiSwap	= alien.raceAI();
			alien.raceAI(player.raceAI());
			player.raceAI(aiSwap);

			// Switch back races if asked
			IGameOptions opts = GameSession.instance().options();
			String restartChangesPlayerRace = opts.selectedRestartChangesPlayerRace();
			if (restartChangesPlayerRace.equalsIgnoreCase("GuiLast")
					|| restartChangesPlayerRace.equalsIgnoreCase("Last")) {
				String playerRaceKey		 = alien.raceKey;
				String alienRaceKey			 = player.raceKey;
				String playerDataRaceKey	 = alien.dataRaceKey;
				String alienDataRaceKey		 = player.dataRaceKey;
				DynOptions playerRaceOptions = alien.raceOptions;
				DynOptions alienRaceOptions  = player.raceOptions;
				alien.raceKey		= alienRaceKey;
				player.raceKey		= playerRaceKey;
				alien.dataRaceKey	= alienDataRaceKey;
				player.dataRaceKey	= playerDataRaceKey;
				alien.raceOptions	= alienRaceOptions;
				player.raceOptions	= playerRaceOptions;
			}
		}
		void playerRace(String r, String dr, boolean isCR,
				DynOptions options, int ai) {
			empires[0].setRace(r, dr, isCR, options, ai);
		}
		LinkedList<String> alienRaces() {
			LinkedList<String> list = new LinkedList<>();
			for (int i=1; i<numEmpires; i++)
				list.add(empires[i].raceKey);
			return list;
		}
    }
}
