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

import static rotp.model.colony.Colony.DIRTY_TYPES_NUM;
import static rotp.model.tech.Tech.miniFastRate;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import rotp.Rotp;
import rotp.model.ai.AI;
import rotp.model.ai.interfaces.Diplomat;
import rotp.model.ai.interfaces.FleetCommander;
import rotp.model.ai.interfaces.General;
import rotp.model.ai.interfaces.Governor;
import rotp.model.ai.interfaces.Scientist;
import rotp.model.ai.interfaces.ShipCaptain;
import rotp.model.ai.interfaces.ShipDesigner;
import rotp.model.ai.interfaces.SpyMaster;
import rotp.model.colony.Colony;
import rotp.model.colony.ColonyShipyard;
import rotp.model.colony.MissileBase;
import rotp.model.empires.Leader.Objective;
import rotp.model.empires.Leader.Personality;
import rotp.model.empires.SpyNetwork.FleetView;
import rotp.model.empires.species.Species;
import rotp.model.events.SystemColonizedEvent;
import rotp.model.events.SystemHomeworldEvent;
import rotp.model.galaxy.Galaxy;
import rotp.model.galaxy.GalaxyFactory;
import rotp.model.galaxy.IMappedObject;
import rotp.model.galaxy.Location;
import rotp.model.galaxy.NamedObject;
import rotp.model.galaxy.Ship;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.Ships;
import rotp.model.galaxy.SpaceMonster;
import rotp.model.galaxy.StarSystem;
import rotp.model.galaxy.StarSystem.SystemBaseData;
import rotp.model.galaxy.Transport;
import rotp.model.game.DynOptions;
import rotp.model.game.GovernorOptions;
import rotp.model.game.IGameOptions;
import rotp.model.incidents.DiplomaticIncident;
import rotp.model.incidents.GenocideIncident;
import rotp.model.planet.PlanetType;
import rotp.model.ships.Design;
import rotp.model.ships.ShipDesign;
import rotp.model.ships.ShipDesignLab;
import rotp.model.ships.ShipLibrary;
import rotp.model.tech.Tech;
import rotp.model.tech.TechRoboticControls;
import rotp.model.tech.TechTree;
import rotp.ui.NoticeMessage;
import rotp.ui.diplomacy.DialogueManager;
import rotp.ui.diplomacy.DiplomaticReply;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.notifications.DiscoverTechNotification;
import rotp.ui.notifications.GNNGenocideNotice;
import rotp.ui.notifications.GNNNotification;
import rotp.ui.notifications.PlunderShipTechNotification;
import rotp.ui.notifications.PlunderTechNotification;
import rotp.util.Base;
import rotp.util.Rand;

public final class Empire extends Species implements NamedObject {
    private static final long serialVersionUID = 1L;
    private static final float SHIP_MAINTENANCE_PCT = .02f;
    private static final float SECURITY_COST_RATIO = 2f;
    private static final int MAX_DEFAULT_MAX_BASES = 999; // BR:
    public static final int DEFAULT_PLAYER_ID = 0;
    public static int PLAYER_ID = DEFAULT_PLAYER_ID;
    public static final int NULL_ID = -1;
    public static final int ABSTAIN_ID = -2;

    public static final int SHAPE_CIRCLE = 0;
    public static final int SHAPE_SQUARE = 1;
    public static final int SHAPE_DIAMOND = 2;
    public static final int SHAPE_TRIANGLE1 = 3;
    public static final int SHAPE_TRIANGLE2 = 4;

    public static Empire thePlayer() { return Galaxy.current().player(); }
    public static long[] times = new long[6]; // BR: Never Used
	// Dynamic difficulty
	private static Double turnMod; // for dynamic difficulty
	private static long turnModCounter = 1;
	private transient float dynaMod;
	private transient long dynaModCounter = 0;
	public static void updateDynValues()	{ valueUpdated("turnMod"); }
	public static void valueUpdated(String id)	{
		if (id.equals("turnMod")) {
			turnMod = null;
			turnModCounter += 1;
		}
	}
	private Double turnMod()	{
		if (turnMod == null) {
			turnMod = options().dynamicDifficultyTurnMod(galaxy().currentTurn());
			//System.out.println("-------------------------- turnMod = " + turnMod); // TO DO BR: REMOVE
		}
		return turnMod;
	}
	public float dynaMod()	{
		if (turnModCounter > dynaModCounter) {
			double empIndPowerLevel = nonDynaIndPowerLevel();
			double playerIndPowerLevel = player().nonDynaIndPowerLevel();
			double r_empInd = empIndPowerLevel / playerIndPowerLevel;
			dynaMod = (float) (1.0 + (options().getDynamicDifficultyScale(r_empInd) - 1.0) * turnMod());
			dynaModCounter = turnModCounter;
			//System.out.println("dynaMod = " + dynaMod); // TO DO BR: REMOVE
		}
		return dynaMod;
	}

    public int selectedAI = -1;
    public final int id;
    private Leader leader;
    private final String raceKey;
    private final int raceNameIndex;
    private TechTree tech = new TechTree();
    private final ShipDesignLab shipLab;
    private final int homeSysId;
    private final int[] compSysId; // modnar: add option to start game with additional colonies
    private int capitalSysId;
    public final SystemInfo sv;
    private final EmpireView[] empireViews;
    private final List<Ship> visibleShips = new ArrayList<>();
    private Map<Ship, StarSystem> suspectedDestinationsOfVisibleShips = new HashMap<>();
    private final List<StarSystem> shipBuildingSystems = new ArrayList<>();
    private final List<StarSystem> colonizedSystems = new ArrayList<>();
    private boolean extinct = false;
    private boolean galacticAlliance = false; // BR: never used!
    private int lastCouncilVoteEmpId = Empire.NULL_ID;
    private Colony.Orders priorityOrders = Colony.Orders.NONE;
    private int bannerColor;
    private final List<StarSystem> newSystems = new ArrayList<>();
    private final EmpireStatus status;

    //bounds
    private float minX, maxX, minY, maxY;
    private float planetScanningRange = 0;
    private float shipScanningRange = 0;
    private boolean knowShipETA = false;
    private boolean scanPlanets = false;
    private boolean recalcDistances = true;
    private float combatTransportPct = 0;
    private int securityAllocation = 0; // Value in %
    private int empireTaxLevel = 0;
    private boolean empireTaxOnlyDeveloped = true;
    private boolean divertColonyExcessToResearch = false;
    private float totalReserve = 0;
    private float tradePiracyRate = 0;
    private NamedObject lastAttacker;
    private int defaultMaxBases = options().defaultMaxBases();
	private String dataRaceKey;	// BR: removed the final key to allow Interface methods to set it

    // BR: Dynamic options
    private DynOptions dynamicOptions = new DynOptions();
    private final DynOptions raceOptions;

    private Long randomSource; // for more repeatable restart
    private Random techRandom; // Do not use: for backward compatibility
    private Rand   memRandom; // for backward compatibility

    private transient float avgX, avgY, nameX1, nameX2; // Names position
    private transient float avgXd, avgYd, nameX1d, nameX2d; // Names position for dark Galaxies

    private transient AI ai;
    private transient boolean[] canSeeShips;
    private transient BufferedImage shipImage;
    private transient BufferedImage shipImageLarge;
    private transient BufferedImage shipImageHuge;
    private transient BufferedImage scoutImage;
    private transient BufferedImage transportImage;
    private transient Color nameColor;
    private transient Color ownershipColor;
    private transient Color selectionColor;
    private transient Color reachColor;
    private transient Color shipBorderColor;
    private transient Color scoutBorderColor;
    private transient Color empireRangeColor;
    private transient double empireNonDynaPower; // BR: create unscaled Power for forced turn end
    private transient double empireNonDynaTechnoIndPower; // BR: create unscaled techno-industrial Power for forced turn end
    private transient float totalEmpireProduction;
    private transient float totalEmpireNonDynaProduction; // modnar: create unscaled production, to avoid infinite recursion
    private transient float totalEmpireShipMaintenanceCost;
    private transient float totalEmpireStargateCost;
    private transient float totalEmpireMissileBaseCost;
    private transient float benchmark;
    private transient int inRange;
    public  transient int numColoniesHistory;
    private transient String empireName;
    private transient List<SpaceMonster> visibleMonsters = new ArrayList<>();
    private transient UfoTracker ufoTracker;
    private transient boolean spendingNotYetMade;

	public static void resetPlayerId()			{ PLAYER_ID = DEFAULT_PLAYER_ID; }
	public static void updatePlayerId(int id)	{ PLAYER_ID = id; }
	public static boolean isPlayer(int id)		{ return id == PLAYER_ID; };	// BR: Never Used

	public void startingNextTurnProcess()		{ spendingNotYetMade = true; }
	public boolean spendingNotYetMade()			{ return spendingNotYetMade; }
	public Integer defaultDesignId()			{ return shipLab.defaultDesignId(); }	// BR: Never Used
	public void defaultDesignId(Integer id)		{ shipLab.defaultDesignId(id); }	// BR: Never Used
	public void clearColoniesDefaultDesignId()	{	// BR: Never Used
		for (StarSystem sys : allColonizedSystems())
			sys.colony().shipyard().defaultDesignId(null);
	}
    public float benchmark() { return benchmark; }
        public void setBenchmark() {
    	if (extinct()) {
    		benchmark = status.lastTurnAlive() - galaxy().currentTurn();
    	}
    	else {
    		benchmark = status.currentPowerValue();
    	}
    }
	private UfoTracker ufoTracker() {
		if (ufoTracker == null)
			ufoTracker = new UfoTracker(this);
		return ufoTracker;
	}
    public Rand techRandom() { // for more repeatable restart
    	if (options().researchMoo1() || galaxy().numberTurns() < 1) {
        	if (memRandom == null) {
        		if (randomSource == null)
        			if (techRandom == null)
        				randomSource = rng().nextLong();
        			else
        				randomSource = techRandom.nextLong();
        		memRandom = new Rand(randomSource);
        	}
        	return memRandom;
    	}
    	return rng();
    }
    public void resetAI() { ai = null; } // BR:
    public void changePlayerAI(String newAI) { // BR:
    	selectedAI = IGameOptions.autoPlayAIset().id(newAI);
    	ai = null;
    }
    public void changeOpponentAI(String newAI) { // BR:
    	selectedAI = IGameOptions.globalAIset().id(newAI);
    	ai = null;
    	ai();
    }
    public AI ai() {
        if (ai == null) {
            if(selectedAI < 0)
                ai = new AI(this, options().selectedAI(this));
            else
                ai = new AI(this, selectedAI);
        }
        return ai;
    }
    public String getAiKey()					  { return IGameOptions.allAIset().aliensKey(selectedAI); } // BR:
    public String getAiName() 					  { return text(getAiKey()); } // BR:
    public Diplomat diplomatAI()                  { return ai().diplomat(); }
    public FleetCommander fleetCommanderAI()      { return ai().fleetCommander(); }
    public ShipCaptain shipCaptainAI()            { return ai().shipCaptain(); }
    public General generalAI()                    { return ai().general(); }
    public Governor governorAI()                  { return ai().governor(); }
    public SpyMaster spyMasterAI()                { return ai().spyMaster(); }
    public Scientist scientistAI()                { return ai().scientist(); }
    public ShipDesigner shipDesignerAI()          { return ai().shipDesigner(); }

    public ShipDesignLab shipLab()                { return shipLab; }
    public EmpireStatus status()                  { return status; }
    public int homeSysId()                        { return homeSysId; }
    public int compSysId(int i)                   { return compSysId[i]; } // modnar: add option to start game with additional colonies

	@Override public int capitalSysId()				{ return capitalSysId; }
	@Override public Leader leader()				{ return leader; }
	@Override public String getLeaderName()			{ return leader.name(); }
	@Override public boolean masksDiplomacy()		{ return super.masksDiplomacy() || ai().diplomat().masksDiplomacy(); }
	@Override public SystemInfo sv()				{ return sv; }

    public List<StarSystem> shipBuildingSystems() { return shipBuildingSystems; }
    public boolean inGalacticAlliance()           { return galacticAlliance; }	// BR: Never Used
    void joinGalacticAlliance()						{ galacticAlliance = true; }
    public float planetScanningRange()            { return max(3, planetScanningRange); }  // max() to correct old saves
    public void planetScanningRange(float d)      { planetScanningRange = d; }
    public float shipScanningRange()              { return shipScanningRange; }
    public void shipScanningRange(float d)        { shipScanningRange = d; }
    public float combatTransportPct()             { return combatTransportPct; }
    public void combatTransportPct(float d)       { combatTransportPct = d; }
    public float tradePiracyRate()                { return tradePiracyRate; }
    public void tradePiracyRate(float f)          { tradePiracyRate = f; }
    public boolean extinct()                      { return extinct; }
    public TechTree tech()                        { return tech; }
    public float totalReserve()                   { return totalReserve; }
    public NamedObject lastAttacker()             { return lastAttacker; }
    public void lastAttacker(NamedObject e)       { lastAttacker = e; }
    public List<ShipFleet> assignableFleets()     {
        if (tech().hyperspaceCommunications())
            return galaxy().ships.allFleets(id);
        else
            return galaxy().ships.notInTransitFleets(id);
    }
    public List<Ship> visibleShips()              { return visibleShips; }
    public List<SpaceMonster> visibleMonsters()   {
    	// visibleMonsters = null;
    	if (visibleMonsters == null)
    		setVisibleMonsters();
    	return visibleMonsters;
    }
    public Map<Ship, StarSystem> suspectedDestinationsOfVisibleShips()  {
    	if (suspectedDestinationsOfVisibleShips == null) // BR: To keep ascendent game compatibility
    		suspectedDestinationsOfVisibleShips = new HashMap<>();
    	return suspectedDestinationsOfVisibleShips;
    }
    public StarSystem suspectedDestinationOfVisibleShip(Ship sh)  { return suspectedDestinationsOfVisibleShips().get(sh); }
    public EmpireView[] empireViews()             { return empireViews; }
    public List<StarSystem> newSystems()          { return newSystems; }
    public int lastCouncilVoteEmpId()             { return lastCouncilVoteEmpId; }
    public void lastCouncilVoteEmpId(int e)       { lastCouncilVoteEmpId = e; }
    public boolean knowShipETA()                  { return knowShipETA; }
	public void knowShipETA(boolean b)            { 
		knowShipETA = (knowShipETA || b);
		if (knowShipETA)
			suspectedDestinationsOfVisibleShips.clear();
	}
    public boolean scanPlanets()                  { return scanPlanets; }
    public void scanPlanets(boolean b)            { scanPlanets = (scanPlanets || b); }
    public void setRecalcDistances()              { recalcDistances = true; }
    public int defaultMaxBases()                  { return defaultMaxBases; }
    public int defaultMaxBases(int newMax)        {
    	defaultMaxBases = bounds(0, newMax, MAX_DEFAULT_MAX_BASES);
    	return defaultMaxBases;
    }
    // BR: made it more global with more control
    public void incrDefaultMaxBases(int inc, boolean shiftDown, boolean ctrlDown)  {
       	if (shiftDown)
       		inc *= 5;
       	if (ctrlDown)
       		inc *= 20;
       	defaultMaxBases += inc;
        if (defaultMaxBases > MAX_DEFAULT_MAX_BASES)
        	defaultMaxBases = 0;
        else if (defaultMaxBases < 0) 
        	defaultMaxBases = MAX_DEFAULT_MAX_BASES;
    }

    public Colony.Orders priorityOrders()         { return priorityOrders; }
    public void priorityOrders(Colony.Orders o)   { priorityOrders = o; }	// BR: Never Used
    @Override public int colorId()                { return bannerColor; }
    private void colorId(int i)                   { bannerColor = i; resetColors(); }
    public int shape()                            { return id / options().numColors(); }
    public float minX()                           { return minX; }
    public float maxX()                           { return maxX; }
    public float minY()                           { return minY; }
    public float maxY()                           { return maxY; }
	public boolean divertColonyExcessToResearch() {
		if (isPlayer())
			return options().divertColonyExcessToResearch();
		else
			return divertColonyExcessToResearch;
	}
	public void toggleColonyExcessToResearch()    {
		if (isPlayer()) {
			divertColonyExcessToResearch = options().toggleDivertColonyExcessToResearch();
			redoGovTurnDecisionsRich();
		}
		else
			divertColonyExcessToResearch = !divertColonyExcessToResearch;
	}
    public void changeColorId(int newColor) {
        int oldColor = colorId();
        
        Empire emp = galaxy().empireMatching(newColor, shape());
        if (emp != null)
            emp.colorId(oldColor);
        
        colorId(newColor);
    }
    public int defaultShipTint() {
        int maxRaces = 10;
        return id < maxRaces ? 0 : id % (ShipDesign.shipColors.length-1)+1;
    }
    private void resetColors() {
        nameColor = null;
        ownershipColor = null;
        selectionColor = null;
        reachColor = null;
        shipBorderColor = null;
        scoutBorderColor = null;
        empireRangeColor = null;
        shipImage = null;
        shipImageLarge = null;
        shipImageHuge = null;
        scoutImage = null;
        transportImage = null;
    }
    public boolean canSeeShips(int empId) {
    	if (isMonster(empId))
    		return true;
        if (canSeeShips == null) {
            canSeeShips = new boolean[galaxy().numEmpires()];
            for (int i=0;i<canSeeShips.length;i++) 
                canSeeShips[i] = (i == id) || viewForEmpire(i).embassy().unity(); 
        }
        return canSeeShips[empId];
    }

	@Override public DynOptions speciesOptions()	{ return raceOptions; }
	@Override public int civilizationNameIndex()	{ return raceNameIndex; }
    public BufferedImage scoutImage() {
        if (scoutImage == null)
            scoutImage = ShipLibrary.current().scoutImage(shipColorId());
        return scoutImage;
    }
    public BufferedImage shipImage() {
        if (shipImage == null)
            shipImage = ShipLibrary.current().shipImage(shipColorId());
        return shipImage;
    }
    public BufferedImage shipImageLarge() {
        if (shipImageLarge == null)
            shipImageLarge = ShipLibrary.current().shipImageLarge(shipColorId());
        return shipImageLarge;
    }
    public BufferedImage shipImageHuge() {
        if (shipImageHuge == null)
            shipImageHuge = ShipLibrary.current().shipImageHuge(shipColorId());
        return shipImageHuge;
    }
    public BufferedImage transportImage() {
        if (transportImage == null)
            transportImage = ShipLibrary.current().transportImage(shipColorId());
        return transportImage;
    }
    public Color nameColor() {
        if (nameColor == null) {
            Color c = color();
            nameColor = newColor(c.getRed(), c.getGreen(), c.getBlue(), 160);
        }
        return nameColor;
    }
    public Color ownershipColor() {
        if (ownershipColor == null) {
            Color c = color();
            ownershipColor = newColor(c.getRed(), c.getGreen(), c.getBlue(), 80);
        }
        return ownershipColor;
    }
    public Color selectionColor() {
        if (selectionColor == null) {
            Color c = color();
            selectionColor = newColor(c.getRed(), c.getGreen(), c.getBlue(), 160);
        }
        return selectionColor;
    }
    public Color shipBorderColor() {
        if (shipBorderColor == null) {
            Color c = color();
            int cR = c.getRed();
            int cG = c.getGreen();
            int cB = c.getBlue();
            shipBorderColor = newColor(cR*6/8,cG*6/8,cB*6/8);
        }
        return shipBorderColor;
    }
    public Color scoutBorderColor() {
        if (scoutBorderColor == null) {
            Color c = color();
            int cR = c.getRed();
            int cG = c.getGreen();
            int cB = c.getBlue();
            scoutBorderColor = newColor(cR*4/8,cG*4/8,cB*4/8);
        }
        return scoutBorderColor;
    }
    public Color empireRangeColor() {
        if (empireRangeColor == null) {
            Color c = color();
            int cR = c.getRed();
            int cG = c.getGreen();
            int cB = c.getBlue();
            empireRangeColor = newColor(cR*3/12,cG*3/12,cB*3/12);
        }
        return empireRangeColor;
    }
    public Color reachColor() {
        if (reachColor == null) {
            Color c = color();
            reachColor = newColor(c.getRed(), c.getGreen(), c.getBlue(), 48);
        }
        return reachColor;
    }
	public void resetDivertColonyExcessToResearch()	{
		divertColonyExcessToResearch = options().divertColonyExcessToResearch();
	}
	// modnar: add option to start game with additional colonies
	// modnar: compId is the System ID array for these additional colonies
	// BR: For Restart with new options and random races
	public Empire(Galaxy g, int empId, Species species, StarSystem s, int[] compId, EmpireBaseData empSrc) {
		super(species);
		IGameOptions opts = options();
		log("creating empire for ", species.id());
		if (empSrc == null)
			randomSource = rng().nextLong();
		else
			randomSource = empSrc.randomSource();
		id = empId;

		// Init empire species parameters
		raceKey		= animKey();
		dataRaceKey	= skillKey();
		raceOptions	= super.speciesOptions(); // BR: for custom species

		// Init Home world
		homeSysId = capitalSysId = s.id;
		compSysId = compId; // modnar: add option to start game with additional colonies
		if (empSrc != null							// Restart
				&& empId != Empire.PLAYER_ID		// Is Alien
				&& !opts.selectedRestartChangesAliensAI())	// Don't changes AI
			selectedAI = empSrc.raceAI();

		empireViews = new EmpireView[opts.selectedNumberOpponents()+1];
		status = new EmpireStatus(this);
		sv = new SystemInfo(this);
		// many things need to know if this is the player civ, so set it early
		if (empId == Empire.PLAYER_ID) {
			resetDivertColonyExcessToResearch();
			g.player(this);
		}

		colorId(super.colorId());
		raceNameIndex	= super.civilizationIndex(); // also initialize species Name
		// Init Leaders
		String leaderName = super.getLeaderName();
		leader = new Leader(this, leaderName);
		if (empSrc != null && empId != Empire.PLAYER_ID
				&& !opts.selectedRestartAppliesSettings()) { // BR: For Restart with new options 
			leader.personality = empSrc.personality;
			leader.objective   = empSrc.objective;
		}
		shipLab = new ShipDesignLab();
	}

	// modnar: add option to start game with additional colonies
	// modnar: compId is the System ID array for these additional colonies
	// BR: For Restart with new options and random races
	public Empire(Galaxy g, int empId, Species species, StarSystem s, int[] compId, Integer cId, String name, EmpireBaseData empSrc) {
		super(species);
		log("creating empire for ", species.id());
		if (empSrc == null)
			randomSource = rng().nextLong();
		else
			randomSource = empSrc.randomSource();
		id = empId;

		// Init empire species parameters
		raceKey		= animKey();
		dataRaceKey	= skillKey();
		raceOptions	= super.speciesOptions(); // BR: for custom species

		// Init Home world
		usedHomeNames().add(s.name()); // To prevent same homeworlds, as custom species may have doubles
		homeSysId = capitalSysId = s.id;
        compSysId = compId; // modnar: add option to start game with additional colonies
        if (empSrc != null							// Restart
        		&& empId != Empire.PLAYER_ID		// Is Alien
        		&& !options().selectedRestartChangesAliensAI())	// Don't changes AI
        	selectedAI = empSrc.raceAI();

        empireViews = new EmpireView[options().selectedNumberOpponents()+1];
        status = new EmpireStatus(this);
        sv = new SystemInfo(this);
        // many things need to know if this is the player civ, so set it early
        if (empId == Empire.PLAYER_ID) {
			resetDivertColonyExcessToResearch();
            g.player(this);
        }

		colorId(cId);
		raceNameIndex	= civilizationIndex(); // also initialize species Name
		// Init Leaders
		String leaderName = name;
		if (leaderName == null)
			leaderName = nextAvailableLeaderExt();
		leader = new Leader(this, leaderName);
		usedLeaderNames().add(leaderName);
        if (empSrc != null && empId != Empire.PLAYER_ID
        		&& !options().selectedRestartAppliesSettings()) { // BR: For Restart with new options 
        	leader.personality = empSrc.personality;
        	leader.objective   = empSrc.objective;
        }
        shipLab = new ShipDesignLab();
    }
    public Empire(Galaxy g, int empId, int sysId, int cId, String name)	{
		super(ORION_KEY);
        log("creating Monster empire for ",  name);
        id			= empId;
        selectedAI	= IGameOptions.BASE;
        raceKey		= "RACE_PSILON";
        dataRaceKey	= "RACE_PSILON";
        raceOptions	= super.speciesOptions();
        homeSysId	= capitalSysId = sysId;
		compSysId	= new int[0];
        empireViews	= new EmpireView[0];;
        status		= null;
        sv			= null;
        bannerColor	= cId;
        leader		= new Leader(this, name);
        shipLab		= new ShipDesignLab();
        loadStartingTechs();
        loadStartingShipDesigns();
        raceNameIndex = 0;
        tech().learnAll();
    }

    public void setBounds(float x1, float x2, float y1, float y2) {
        minX = x1;
        maxX = x2;
        minY = y1;
        maxY = y2;
    }
    public void loadStartingTechs() {
        tech.recalc(this);
    }
    public void loadStartingShipDesigns() {
        shipLab.init(this);
    }
	private boolean isMonster(int id)	{ return id < NULL_ID; };
	private boolean isNull(int id)		{ return id == NULL_ID; };
	private boolean isEmpire(int id)	{ return id >= DEFAULT_PLAYER_ID; };
	private boolean isNotEmpire(int id)	{ return id < DEFAULT_PLAYER_ID; };
    public boolean isMonster()           { return id < NULL_ID; };
    public boolean isEmpire()            { return id >= DEFAULT_PLAYER_ID; };
    public boolean isNull()              { return id == NULL_ID; };
    public boolean isNotEmpire()         { return id < DEFAULT_PLAYER_ID; };
	@Override public boolean isPlayer()	 { return id == PLAYER_ID; };
    public boolean isAI()                { return id != PLAYER_ID; }; // !!! FOR UI ONLY
    public boolean isPlayerControlled()  { return !isAIControlled(); }
    public boolean isAIControlled()      { return isAI() || options().isAutoPlay(); }
    //public boolean isAIControlled()      { return true; } //for quick switch to test how the AI would have fared in a game
    public Color color()                 { return options().color(bannerColor); }
    int shipColorId()					{ return colorId(); }
	@Override public String name()		{
		if (empireName == null)
			empireName = empireTitle();
		return empireName;
	}
    public DiplomaticReply respond(String reason, Empire listener) {
        return respond(reason,listener,null);
    }
    private DiplomaticReply respond(String reason, Empire listener, Empire other) {
        return respond(reason,listener,other,"other");
    }
    DiplomaticReply respond(String reason, Empire listener, Empire other, String otherName) {
        String message = DialogueManager.current().randomMessage(reason, this);
        message = replaceTokens(message, "my");
        message = listener.replaceTokens(message, "your");
        if (other != null)
            message = other.replaceTokens(message, otherName);
        return DiplomaticReply.answer(true, message);
    }
    public DiplomaticReply respond(String reason, DiplomaticIncident inc, Empire listener) {
        String message = DialogueManager.current().randomMessage(reason, this);
        message = replaceTokens(message, "my");
        message = listener.replaceTokens(message, "your");
        message = inc.decode(message);
        return DiplomaticReply.answer(true, message);
    }
    public void chooseNewCapital() {
        // make list of every colony that is not the current capital
        StarSystem currentCapital = galaxy().system(capitalSysId);
        List<StarSystem> allExceptCapital = new ArrayList<>(allColonizedSystems());
        allExceptCapital.remove(currentCapital);

        // from that, make a list of non-rebelling colonies
        List<StarSystem> possible = new ArrayList<>();
        for (StarSystem sys: allExceptCapital) {
            if (sys != null && !sys.colony().inRebellion())
                possible.add(sys);
        }
        // if EVERY colony is rebelling, then choose from any of them
        if (possible.isEmpty())
            possible.addAll(allExceptCapital);

        // if still no other colonies, give it up (we are losing our capital and last colony)
        if (possible.isEmpty())
            return;

        // sort based on production, choose highest(last), then end any rebellions on it
        Collections.sort(possible, StarSystem.BASE_PRODUCTION);
        StarSystem newHome = possible.get(possible.size()-1);
        capitalSysId = newHome.id;
        newHome.colony().clearAllRebellion();        
    }
    public int shipCount(int hullSize) {
        return galaxy().ships.hullSizeCount(id, hullSize);
    }
    @Override
    public String toString()   { return concat("Empire: ", raceName()); }

    public boolean canSendTransportsFrom(StarSystem sys) {
        if (sys == null)
            return false;
        if (sys.empire() != this)
            return false;
        if (sv.maxTransportsToSend(sys.id) == 0)
            return false;
        if (sys.colony().inRebellion())
            return false;
        if (sys.colony().quarantined())
            return false;

        for (StarSystem abSys: galaxy().abandonedSystems()) {
            if (abSys != null && sv.inShipRange(abSys.id) && canColonize(abSys))
                return true;
        }

        return true;
    }
    public boolean canAbandonTo(StarSystem sys) {
        if (sys == null)
            return false;
        if (sys.empId() == id)
            return true;
        return false;
    }
    public boolean canSendTransportsTo(StarSystem sys) {
        if (sys == null)
            return false;
        if (!sv.isScouted(sys.id))
            return false;
        if (!sv.isColonized(sys.id) && !sv.isAbandoned(sys.id))
            return false;
        if (!sv.inShipRange(sys.id))
            return false;
        if ((sys.empire() == this) && sys.colony().inRebellion())
            return true;
        return canColonize(sys.planet().type());
    }
    public boolean canRallyFleetsFrom(int sysId) {
        return (sysId != StarSystem.NULL_ID) && (sv.empire(sysId) == this) && (allColonizedSystems().size() > 1);
    }
    public boolean canRallyFleetsTo(int sysId) {
        return (sysId != StarSystem.NULL_ID) && (sv.empire(sysId) == this) && (allColonizedSystems().size() > 1);
    }
    public boolean canRallyFleets() {
        return allColonizedSystems().size() > 1;
    }
    public boolean canSendTransports() {
        return allColonizedSystems().size() > 1;
    }
    public int maxTransportsAllowed(StarSystem sys) {
        if (!canSendTransportsTo(sys))
            return 0;
        else if (sys.empire() == this)
            return (int)(sv.currentSize(sys.id) - (int) sv.population(sys.id))+sys.transportSprite().amt();
        else
            return (int) sv.currentSize(sys.id);
    }
    public void changeAllExistingRallies(StarSystem dest) {
        if (canRallyFleetsTo(id(dest))) {
            for (StarSystem sys: allColonizedSystems()) {
                if (sys != null && sv.hasRallyPoint(sys.id))
                    sv.rallySystem(sys.id, dest);
            }
        }
    }
    public void changeRalliesFromAToB(StarSystem source, StarSystem dest) {
        if (canRallyFleetsFrom(id(source)) && canRallyFleetsTo(id(dest))) {
        	if (!isCtrlDown())
	            for (StarSystem sys: allColonizedSystems()) {
	                if (sys != null && sv.rallySystem(sys.id) == source)
	                    sv.rallySystem(sys.id, dest);
	            }
            boolean ChainRally = options().defaultChainRally();
            if (isShiftDown())
            	ChainRally = !ChainRally;
            if (ChainRally) {
            	ColonyShipyard shipyard = source.colony().shipyard();
        		Float speed = null;
        		Design design = shipyard.design();
        		if (design instanceof ShipDesign) {
        			ShipDesign shipDesign = (ShipDesign) design;
        			speed = options().chainRallySpeed(this, shipDesign);
        		}
            	chainRally(source, dest, SystemView.SET_RALLY, speed);
            }
            else
            	sv.rallySystem(source.id, dest);
        }
    }
    public void processChainRally(List<StarSystem> chainList, int action) {
    	switch (chainList.size()) {
    	case 2:
    		sv.rallySys(chainList.get(0).id, chainList.get(1), action);
    		return;
    	case 3:
    		sv.rallySys(chainList.get(0).id, chainList.get(1), action);
    		sv.rallySys(chainList.get(1).id, chainList.get(2), action);
    		if (action == SystemView.SET_RALLY)
        		sv.setForwardRallies(chainList.get(1).id, true);
    		return;
    	case 4:
    		sv.rallySys(chainList.get(0).id, chainList.get(1), action);
       		sv.rallySys(chainList.get(1).id, chainList.get(2), action);
       		sv.rallySys(chainList.get(2).id, chainList.get(3), action);
       	    if (action == SystemView.SET_RALLY) {
       	    	sv.setForwardRallies(chainList.get(1).id, true);
       	    	sv.setForwardRallies(chainList.get(2).id, true);
       	    }
    		return;
    	}
    }
    /**
     * Try to find a shortcut through any stargates
     * 
     * @param from Origin StarSystem (Mandatory)
     * @param dest Destination StarSystem (Mandatory)
     * @param action SystemView SET_RALLY | PREVIEW_RALLY | CLEAR_PREVIEW  (Mandatory)
     * @param speed speed to use (optional, can be null)
     */
    public List<StarSystem> chainRally(StarSystem from, StarSystem dest, int action, Float speed) {
    	return chainRallies(from, systemsWithStargate(), dest, action, speed);
    }
    /**
     * Try to find a shortcut through any stargates
     * 
     * @param from Origin StarSystem (Mandatory)
     * @param gates allowed stargates to be used (Mandatory)
     * @param dest Destination StarSystem (Mandatory)
     * @param action SystemView SET_RALLY | PREVIEW_RALLY | CLEAR_PREVIEW  (Mandatory)
     * @param speed speed to use (optional, can be null)
     */
    public List<StarSystem> chainRallies(StarSystem from,
    		List<StarSystem> gates, StarSystem dest, int action, Float speed) {
        List<StarSystem> chainList = new ArrayList<>();
        List<StarSystem> rallyList = getRallyChain(from, gates, dest, speed);
        chainList.add(from);
        if (rallyList != null)
        	chainList.addAll(rallyList);
        chainList.add(dest);
        processChainRally(chainList, action);
        return chainList;
    }
    /**
     * Try to find a shortcut through any stargates
     * 
     * @param from Origin StarSystem (Mandatory)
     * @param dest Destination StarSystem (Mandatory)
     * @param speed speed to use (optional, can be null)
     */
    public List<StarSystem> getRallyChain(StarSystem from, StarSystem dest, Float speed) {
    	return getRallyChain(from, systemsWithStargate(), dest, speed);
    }
    /**
     * Try to find a shortcut through any stargates
     * 
     * @param from Origin StarSystem (Mandatory)
     * @param gates allowed stargates to be used (Mandatory)
     * @param dest Destination StarSystem (Mandatory)
     * @param speed speed to use (optional, can be null)
     */
    private List<StarSystem> getRallyChain(StarSystem from, List<StarSystem> gates, StarSystem dest, Float speed) {
    	boolean fromHasSG = from.hasStargate(this);
        boolean destHasSG = dest.hasStargate(this);
    	int numEndSG = 0;
    	if (fromHasSG) numEndSG ++;
    	if (destHasSG) numEndSG ++;
        if (gates == null || gates.size() < 3-numEndSG || numEndSG == 2)
        	return null;
        if (speed == null)
        	speed = options().chainRallySpeed(this);
        int directTravelTime = (int) Math.ceil(from.travelTimeTo(dest, speed));
        int allowedTime = directTravelTime-(2-numEndSG);
        if (allowedTime <=0)
        	return null;

        // check for link from to stargate
        int[] fromSG = new int[] {-1, 0};
        if (!fromHasSG) {
            fromSG = nearestStargate(from, gates, speed, allowedTime);
            if (fromSG == null)
            	return null;
            allowedTime = directTravelTime-fromSG[1]-1;
        }

        List<StarSystem> relays = new ArrayList<>();
        
        // check for link dest to stargate
        int[] destSG = new int[] {-1, 0};
        if (!destHasSG) {
	        destSG = nearestStargate(dest, gates, speed, allowedTime);
	        if (destSG == null)
	        	return null;
        }
        else { // no-SG to SG to SG
        	relays.add(gates.get(fromSG[0]));
        	return relays;
        }

        if (fromHasSG) { // SG to SG to no-SG
        	relays.add(gates.get(destSG[0]));
        	return relays;
        }
        // no-SG to SG to SG to no-SG
        relays.add(gates.get(fromSG[0]));
        relays.add(gates.get(destSG[0]));
        return relays;
    }
    private int[] nearestStargate(StarSystem sys, List<StarSystem> sgList, float speed, int baseTime) {
    	if (sgList == null || sgList.isEmpty())
    		return null;
    	float minDist = Float.MAX_VALUE;
        int minTime   = baseTime;
    	int minIdx    = -1;
    	int gatesSize = sgList.size();
    	for (int idx=0; idx<gatesSize; idx++) {
    		int travelTime = (int) Math.ceil(sys.travelTimeTo(sgList.get(idx), speed));
    		float distance = sys.distanceTo(sgList.get(idx));
       		if (travelTime < minTime) {
    			minTime = travelTime;
    			minDist = distance;
    			minIdx  = idx;
        	}
       		// Check for min distance for older non TopSpeed units
       		else if (travelTime == minTime && distance < minDist) {
    			minDist = distance;
    			minIdx  = idx;	       			
       		}
    	}
    	if (minTime == baseTime)
    		return null;
    	return new int[] {minIdx, minTime};
    }
    public float minActiveDesignSpeed() {
    	float minSpeed = tech().topSpeed();
		for (ShipDesign design : shipLab.designs()) {
			if (design != null && design.active()) {
				float designSpeed =  (float) design.warpSpeed();
				if (designSpeed < minSpeed)
					minSpeed = designSpeed;
			}
		}
		return minSpeed;
    }
    public void startRallies(List<StarSystem> fromSystems, StarSystem dest) {
        if (canRallyFleetsTo(id(dest))) {
            for (StarSystem sys: fromSystems)
            	if(sys != null)
            		sv.rallySystem(sys.id, dest);
        }
    }
    public void stopRallies(List<StarSystem> fromSystems) {
        for (StarSystem sys: fromSystems)
           	if(sys != null)
           		sv.stopRally(sys.id);
    }
    public void cancelTransport(StarSystem from) {	// BR: Never used
        from.transportSprite().clear();
    }
    public void deployTransport(StarSystem from) {
        from.transportSprite().accept();
    }
    public void deployTransports(List<StarSystem> fromSystems, StarSystem dest, boolean synch) {
        if (synch) {
            float maxTime = 0;
            for (StarSystem from: fromSystems)
            	if (from != null)
            		maxTime = max(maxTime, from.colony().transport().travelTimeAdjusted(dest));
            for (StarSystem from: fromSystems)
            	if (from != null)
            		from.transportSprite().accept(maxTime);
        }
        else {
            for (StarSystem from: fromSystems)
	           	if (from != null)
	                from.transportSprite().accept();
        }
    }
    public int travelTurnsAdjusted(StarSystem from, StarSystem dest, float speed) {
        if (from.hasStargate(this) && dest.hasStargate(this))
            return 1;
        return (int) Math.ceil(from.travelTimeAdjusted(from,dest,speed));
    }
    public void stopRalliesWithSystem(StarSystem dest) {
        List<StarSystem> systems = allColonizedSystems();
        sv.stopRally(dest.id);
        for (StarSystem sys: systems) {
            if (sys != null && sv.rallySystem(sys.id) == dest) 
                sv.stopRally(sys.id);
        }
    }
    public void validate() {
        recalcPlanetaryProduction();
        validateColonizedSystems();
        validateFleets();
        for (StarSystem sys: colonizedSystems)
        	if (sys != null)
        		sys.colony().validate();
    }
    private void validateColonizedSystems() {
        List<StarSystem> good = new ArrayList<>();
        for (StarSystem sys: colonizedSystems) {
            if (sys != null && sys.isColonized() && (sys.empire() == this)) {
                if (!good.contains(sys))
                    good.add(sys);
            }
        }
        colonizedSystems.clear();
        colonizedSystems.addAll(good);
    }
    private void validateFleets() {
        for(ShipFleet fl : galaxy().ships.allFleets(id)) {
            if(fl.bcValue() == 0) {
                fl.clear();
                galaxy().ships.deleteFleet(fl);
            }
        }
    }
	public void startAlwaysAlly()	{
		for(EmpireView view : empireViews)
			if (view != null && !view.extinct())
				view.embassy().startAlwaysAlly();
	}
	public void startAlwaysAtWar()	{
		for(EmpireView view : empireViews)
			if (view != null && !view.extinct())
				view.embassy().startAlwaysAtWar();
	}
	public void validateOnLoad()	{
		if (dynamicOptions == null)
			dynamicOptions = new DynOptions();

		setSpecies(new Species(raceKey, dataRaceKey, raceOptions));
		setOldSpeciesIndex(raceNameIndex);

		tech().validateOnLoad();
        for(EmpireView view : empireViews)
            if(view != null)
                view.validateOnLoad();
    }
    public void cancelTransports(List<StarSystem> fromSystems) {
        for (StarSystem from: fromSystems)
        	if (from != null)
        		from.transportSprite().clear();
    }
    public void addColonyOrder(Colony.Orders order, float amt) {
        for (StarSystem sys: allColonizedSystems()) {
        	if (sys != null) {
	            Colony col = sys.colony();
	            col.addColonyOrder(order, amt);
	            if (col.reallocationRequired)
	                governorAI().setColonyAllocations(col);     
        	}
        }
    }
    public void addColonizedSystem(StarSystem s) {
        if (!colonizedSystems.contains(s)) {
            colonizedSystems.add(s);
            setRecalcDistances();
//            refreshViews(); // BR: Optimization
            for (Empire ally: allies()) {
            	if (ally != null)
            		ally.setRecalcDistances();       
//                 ally.refreshViews(); // BR: Optimization
            }
        }
    }
    public void removeColonizedSystem(StarSystem s) {
        colonizedSystems.remove(s);
        setRecalcDistances();
//        refreshViews(); // BR: Optimization
        for (Empire ally: allies()) {
            if (ally != null)
            	ally.setRecalcDistances();
//            ally.refreshViews(); // BR: Optimization
        }

        if (colonizedSystems.isEmpty())
            goExtinct();
    }
    public void takeAbandonedSystem(StarSystem sys, Transport tr) {
		if (tr.size() == 0)
			return;
        sys.addEvent(new SystemColonizedEvent(id));
        newSystems.add(sys);
        addColonizedSystem(sys);
        sys.becomeColonized(sys.name(), this);
        sys.colony().setPopulation(min(sys.planet().currentSize(),tr.size()));
        tr.size(0);
    }
    public Colony colonize(String sysName, StarSystem sys) {
        StarSystem home = galaxy().system(capitalSysId);
        sys.addEvent(new SystemColonizedEvent(id));
        newSystems.add(sys);
        Colony c = sys.becomeColonized(sysName, this);
        addColonizedSystem(sys);
        governorAI().setInitialAllocations(c);
        if (isPlayerControlled()) {
            int maxTransportPop =(int)(sys.planet().maxSize()-sys.colony().population());
            galaxy().giveAdvice("MAIN_ADVISOR_TRANSPORT", sysName, str(maxTransportPop), home.name());
            session().addSystemToAllocate(sys, text("MAIN_ALLOCATE_COLONIZED", sysName));
        }
        return c;
    }
    public Colony colonizeHomeworld() {
        StarSystem home = galaxy().system(homeSysId);
        home.addEvent(new SystemHomeworldEvent(id));
        newSystems.add(home);
        colonizedSystems.add(home);
        Colony c = home.becomeColonized(home.name(), this);
        c.setHomeworldValues();
        governorAI().setInitialAllocations(c);
        sv.refreshFullScan(homeSysId);
        return c;
    }
    // modnar: add option to start game with additional colonies
    public Colony colonizeCompanionWorld(int sysId) {
        StarSystem sys1 = galaxy().system(sysId);
        sys1.addEvent(new SystemColonizedEvent(id));
        newSystems.add(sys1);
        colonizedSystems.add(sys1);
        Colony c1 = sys1.becomeColonized(sys1.name(), this);
        c1.setCompanionWorldValues();
        governorAI().setInitialAllocations(c1);
        sv.refreshFullScan(sys1.id);
        return c1;
    }
    public boolean isHomeworld(StarSystem sys) {
        return sys.id == homeSysId;
    }
    public boolean isCapital(StarSystem sys) {
        return sys.id == capitalSysId;
    }
    public boolean isColony(StarSystem sys) {
        return (sv.empire(sys.id) == this) && isEnvironmentHostile(sys);
    }
	public boolean isEnvironmentHostile(StarSystem  sys) {
		return !ignoresPlanetEnvironment() && sys.planet().isEnvironmentHostile();
	}
    public boolean isEnvironmentFertile(StarSystem  sys) {
        return sys.planet().isEnvironmentFertile();
    }
    public boolean isEnvironmentGaia(StarSystem  sys) {
        return sys.planet().isEnvironmentGaia();
    }
    public void setBeginningColonyAllocations() {
        // try to maximum industry at start of game (for players)
        Colony c = galaxy().system(homeSysId).colony();
        c.clearSpending();
        governorAI().setInitialAllocations(c);
    }
    private boolean colonyCanScan(StarSystem sys) {
        return scanPlanets && sv.withinRange(sys.id, planetScanningRange());
    }
    private boolean fleetCanScan(StarSystem sys) {
        if (!scanPlanets)
            return false;
        List<ShipFleet> fleets = galaxy().ships.allFleets(id);
        for (ShipFleet fl: fleets) {
            if (fl != null && shipScanningRange() >= fl.distanceTo(sys))
                return true;
        }
        return false;
    }
    public float darkRange()              { return max(tech().scoutRange(), planetScanningRange); }
    public float shipRange()              { return tech().shipRange(); }
    public float scoutRange()             { return tech().scoutRange(); }
    
    public float colonyShipRange() {
        // return max range of design with colony special
        float range = shipRange();
        for (int slot=0;slot<ShipDesignLab.MAX_DESIGNS;slot++) {
            ShipDesign d = shipLab().design(slot);
            if(d.active() && d.hasColonySpecial() && (d.range() > range))
                range = d.range();
        }
        return range;
    }
    public float researchingShipRange()   { return tech().researchingShipRange(); }
    public float researchingScoutRange()  { return tech().researchingScoutRange(); }
    public float learnableShipRange()     { return tech().learnableShipRange(); }	// BR: Never used
    public float learnableScoutRange()    { return tech().learnableScoutRange(); }	// BR: Never used
    public float shipReach(int turns)   { return min(shipRange(), turns*tech().topSpeed()); }	// BR: Never used
    public float scoutReach(int turns)  { return min(scoutRange(), turns*tech().topSpeed()); }
	public float speedInNebulae()		{ return options().selectedWarpSpeedFactor(); }
	public String speedInNebulaeStr()	{
		float speed = speedInNebulae();
		if (speed %1 == 0)
			return "" + (int)speed;
		return fmt(speed);
	}
    public String rangeTechNeededToScout(int sysId) {
        float dist = sv.distance(sysId);
        return tech().rangeTechNeededToScoutDistance(dist);
    }
    public String rangeTechNeededToReach(int sysId) {
        float dist = sv.distance(sysId);
        return tech().rangeTechNeededToReachDistance(dist);
    }
    public String environmentTechNeededToColonize(int sysId) {
        if (canColonize(sysId))
            return null;
        int hostility = sv.planetType(sysId).hostility();
        return tech().environmentTechNeededToColonize(hostility);
    }
    public boolean canColonize(int sysId) {
        StarSystem sys = galaxy().system(sysId);
        return canColonize(sys.planet().type());
    }
    public boolean canColonize(StarSystem sys) {
        return canColonize(sys.planet().type());
    }
    public boolean canColonize(PlanetType pt) {
        if (pt == null)  // hasn't been scouted yet
            return false;
        if (pt.isAsteroids())
            return false;
        if (ignoresPlanetEnvironment() && acceptedPlanetEnvironment(pt))
            return true;
        return tech().canColonize(pt);
    }
    private boolean canColonize(PlanetType pt, int newHostilityLevel) {
        if (pt == null)  // hasn't been scouted yet
            return false;
        if (pt.isAsteroids())
            return false;
        if (ignoresPlanetEnvironment() && acceptedPlanetEnvironment(pt))
            return true;
        return tech().canColonize(pt, newHostilityLevel);
    }
    public boolean isLearningToColonize(PlanetType pt) {
        if (pt == null)  // hasn't been scouted yet
            return false;
        if (pt.isAsteroids())
            return false;
        if (ignoresPlanetEnvironment() && acceptedPlanetEnvironment(pt))
            return true;
        return tech().isLearningToColonize(pt);
    }
    public boolean canLearnToColonize(PlanetType pt) {
        if (pt == null)  // hasn't been scouted yet
            return false;
        if (pt.isAsteroids())
            return false;
        if (ignoresPlanetEnvironment() && acceptedPlanetEnvironment(pt))
            return true;
        return tech().canLearnToColonize(pt);
    }
    public boolean knowETA(Ship sh) {
    	if (isMonster(sh.empId())) {
    		SpaceMonster monster = (SpaceMonster) sh;
    		if (monster.event == null)
    			return false;
    		return monster.event.notified();
    	}
        return knowShipETA || canSeeShips(sh.empId());
    }
    public StarSystem defaultSystem() {
        StarSystem home = galaxy().system(capitalSysId);
        if (home.empire() == this)
            return home;
        else if (!extinct)
        	return allColonizedSystems().get(0);
        else
        	return galaxy().system(0);
    }
    public void addViewFor(Empire emp) {
        if ((emp != null) && (emp != this))
            empireViews[emp.id] = new EmpireView(this, emp);
    }
    public float tradeIncomePerBC() {
        float empireBC = totalPlanetaryProduction();
        float income = netTradeIncome();
        return income / empireBC;
    }
    public void nextTurn() {
        log(this + ": NextTurn");
        shipBuildingSystems.clear();
        newSystems.clear();
        recalcPlanetaryProduction();
        tech().preNextTurn();
        for (ShipDesign d : shipLab.designs()) {
            if (d != null)
                d.preNextTurn();
        }
        
        // assign funds/costs for diplomatic activities
        for (EmpireView v : empireViews()) {
          if ((v!= null) && v.embassy().contact())
                v.spies().report().clear();
        }

        List<StarSystem> allColonies = allColonizedSystems();
        List<Transport> transports = transports();

        if (!extinct) {
            if (allColonies.isEmpty() && transports.isEmpty()) {
                goExtinct();
                return;
            }
        }

        // assign planetary funds/costs & enact development
        for (StarSystem s: allColonies) {
        	if (s != null) {
	            Colony col = s.planet().colony();
	            addReserve(col.production() * col.colonyTaxPct());
	            col.nextTurn();
        	}
        }
        // modnar: newRace GearHead gets 0.25 BC directly from 1.0 POP
        // BR: direct call to Race
        addToTreasury(bCBonus() * totalPlanetaryPopulation());
        recalcPlanetaryProduction();
        spendingNotYetMade = false;
    }
    public void postNextTurn() {
        log(this + ": postNextTurn");
        float civProd = totalPlanetaryProduction();
        float spyMod = spySpendingModifier();

        // assign funds/costs for diplomatic activities
        for (EmpireView v : empireViews()) {
          if ((v!= null) && v.embassy().contact())
                v.nextTurn(civProd, spyMod);
        }
    }
    public void assessTurn() {
         log(this + ": AssessTurn"); 
        // have to assess trade & security views
        // before colonies since taxes may change  
        empireViewsAssessTurn();
        recalcPlanetaryProduction();

        if (status() != null)
            status().assessTurn();

        for (int i=0;i<sv.count();i++) {
            if ((sv.empire(i) == this) && sv.isColonized(i))
                sv.colony(i).assessTurn();
        }
    }
    public int[] needCleaning() {
    	int[] needCleaning = new int[DIRTY_TYPES_NUM];
        List<StarSystem> systems = new ArrayList<>(colonizedSystems);
        for (StarSystem sys: systems)
            if (sys != null && sys.isColonized()) {
            	int[] intArr = sys.colony().needCleaning();
            	for (int i=0; i<DIRTY_TYPES_NUM; i++)
            		needCleaning[i] = needCleaning[i] + intArr[i];
            	}
        return needCleaning;
    }
    public void checkEcoAtClean(MouseEvent e) {
        List<StarSystem> systems = new ArrayList<>(colonizedSystems);
        for (StarSystem sys: systems)
            if (sys != null && sys.isColonized())
                sys.colony().checkEcoAtClean(e);
    }
    public void lowerECOToCleanIfEcoComplete() {
        List<StarSystem> systems = new ArrayList<>(colonizedSystems);
        for (StarSystem sys: systems) {
            if (sys != null && sys.isColonized())
                sys.colony().lowerECOToCleanIfEcoComplete();
        }
    }
    private void empireViewsAssessTurn() {
        for (EmpireView v : empireViews()) {
            if ((v!= null) && v.embassy().contact()) {
                v.embassy().assessTurn();
                v.trade().assessTurn();
            }
        }
    }
    public void makeDiplomaticOffers() {
        for (EmpireView v : empireViews()) {
            if ((v!= null) && v.embassy().contact())
                v.makeDiplomaticOffers();
        }
    }
    public void threatReplyAgainst(int empId, int reply) {
        EmpireView v = viewForEmpire(empId);
        if (v != null)
            v.spies().lastSpyThreatReply(reply);
    }
    public void hideSpiesAgainst(int empId) {
        EmpireView v = viewForEmpire(empId);
        if (v != null)
            v.spies().beginHide();
    }
    public void shutdownSpyNetworksAgainst(int empId) {
        EmpireView v = viewForEmpire(empId);
        if (v != null) 
            v.spies().shutdownSpyNetworks();
    }
    public StarSystem retreatSystem(StarSystem from) {
        return shipCaptainAI().retreatSystem(from);
    }
	private boolean enemyInOrbit(StarSystem sys, List<Integer> enemies)	{
		List<ShipFleet> fleets = sys.orbitingFleetsNoMonster();
		if (fleets.isEmpty())
			return false;
		for (ShipFleet fleet: fleets)
			if (enemies.contains(fleet.empId()))
				return true;
		return false;
	}
	public boolean allowedToRetreatTo(StarSystem fromSys, StarSystem toSys)	{
		IGameOptions opts	= options();
		boolean closestOnly	= opts.retreatClosestOnly();
		if (closestOnly)
			return allowedRetreatSystems(fromSys).contains(toSys);

		int fromSysId	= fromSys.id;
		int toSysId		= toSys.id;
		boolean hyperComExt	= opts.hyperComRetreatExtended() && tech().hyperspaceCommunications();
		boolean anyColony	= hyperComExt || opts.retreatToAnyPlanet();
		boolean allowAlly	= anyColony || opts.retreatOnlyToAlly();
		boolean noEnemy		= opts.noEnemyOnRetreatDestination();

		if (noEnemy) {
			List<Integer> enemies = enemiesId();
			if (toSysId == fromSysId || toSys.hasMonster() || enemies.contains(toSys.empId()))
				return false;
			if (!sv.inShipRange(toSysId))
				return false;
			if (anyColony || toSys.empId() == id || (allowAlly && alliedWith(sv.empId(toSysId))))
				return !enemyInOrbit(toSys, enemies);
		}
		else {
			if (toSysId != fromSysId && sv.inShipRange(toSysId))
				return (anyColony|| toSys.empId() == id || (allowAlly && alliedWith(sv.empId(toSysId))));
		}
		return false;
	}
	public List<StarSystem> allowedRetreatSystems(StarSystem fromSys)	{
		Galaxy gal		= galaxy();
		int fromSysId	= fromSys.id;
		IGameOptions opts	= options();
		boolean hyperComExt	= opts.hyperComRetreatExtended() && tech().hyperspaceCommunications();
		boolean anyPlanet	= hyperComExt || opts.retreatToAnyPlanet();
		boolean allowAlly	= anyPlanet || opts.retreatOnlyToAlly();
		boolean closestOnly	= opts.retreatClosestOnly();
		boolean noEnemy		= opts.noEnemyOnRetreatDestination();

		List<StarSystem> systems = new ArrayList<>();
		if (noEnemy) {
			List<Integer> enemies = enemiesId();
			for (StarSystem sys : gal.starSystems()) {
				int sysId = sys.id;
				if (sysId == fromSysId || sys.hasMonster() || enemies.contains(sys.empId()))
					continue;
				if (!sv.inShipRange(sysId))
					continue;
				if (anyPlanet || sys.empId() == id || (allowAlly && alliedWith(sv.empId(sysId))))
					if (!enemyInOrbit(sys, enemies))
						systems.add(sys);
			}
		}
		else {
			for (StarSystem sys : gal.starSystems()) {
				int sysId = sys.id;
				if (sysId != fromSysId && sv.inShipRange(sysId)) {
					if (anyPlanet|| sys.empId() == id || (allowAlly && alliedWith(sv.empId(sysId)))) {
						systems.add(sys);
					}
				}
			}
		}
		if (systems.isEmpty() || hyperComExt|| !closestOnly)
			return systems;

		float speed = tech().topSpeed();
		StarSystem system = fromSys.minTimeTo(systems, speed);
		systems.clear();
		systems.add(system);
		return systems;
	}
	public List<StarSystem> onlyAlliedSystems(List<StarSystem> systems) {
		List<StarSystem> alliedSystems = new ArrayList<>();
		for (StarSystem sys : systems)
			if (alliedWith(sys.id))
				alliedSystems.add(sys);
		return alliedSystems;
	}
	public StarSystem withDrawFromSystem(StarSystem sys) {
		float speed = tech().topSpeed();
		List<StarSystem> allySystems = allySystems();
		if(allySystems.isEmpty())
			return null;
		if(allySystems.size() == 1)
			return allySystems.get(0);
		//ail: first try to use the staging-point for the system we are currently retreating from so we don't retreat to system with enemies
		int sysId = optimalStagingPoint(sys, 1, allySystems);
		//ail: only if that fails take overall closest system
		if(sysId == StarSystem.NULL_ID)
			//sysId = empire.alliedColonyNearestToSystem(sys, speed);
			return sys.minTimeTo(allySystems, speed);
		else
			return galaxy().system(sysId);
	}
    public void retreatShipsFrom(int empId) {
        List<Transport> transports = transports();
        for (Transport tr: transports) {
            if (tr != null && tr.destination().empId() == empId)
                tr.orderToSurrenderOnArrival();
        }
        ShipCaptain shipCaptain = shipCaptainAI();
        Ships shipMgr = galaxy().ships;
        List<ShipFleet> fleets = shipMgr.allFleets(id);
        for (ShipFleet fl: fleets) {
	        if (fl != null) {
	            // if orbiting a system colonized by empId, then retreat it
	            if (fl.isOrbiting()) {
	                StarSystem orbitSys = fl.system();
	                if (orbitSys.empId() == empId) {
	                    StarSystem dest = shipCaptain.retreatSystem(orbitSys); 
	                    if (dest != null)
	                        shipMgr.retreatFleet(fl, dest.id);
	                }
	            }
	            // if in transit to a system colonized by empId, then
	            // set it to retreat on arrival
	            else if (fl.inTransit()) {
	                StarSystem dest = fl.destination();
	                if (dest.empId() == empId)
	                    fl.makeRetreatOnArrival();
	            }
        	}
        }
    }
    public void completeResearch() {
        tech.allocateResearch();
    }
    public void acquireTradedTechs() {
        tech.acquireTradedTechs();
    }
    public void makeNextTurnDecisions() {
        recalcPlanetaryProduction();

        log(this + ": make NextTurnDecisions");
        NoticeMessage.setSubstatus(text("TURN_SCRAP_SHIPS"));
        shipLab.nextTurn();

        // empire settings
        if (isAIControlled()) {
            scientistAI().setTechTreeAllocations();
            securityAllocation = spyMasterAI().suggestedInternalSecurityLevel();
            empireTaxLevel = governorAI().suggestedEmpireTaxLevel();
            //ail: calling this before fleetCommanderAI avoids a possible case where a fleet is slower than it could be due to scraping ships after the fleet was launched
            NoticeMessage.setSubstatus(text("TURN_DESIGN_SHIPS"));
            shipDesignerAI().nextTurn();
//            long startMs = timeMs(); // BR: fleetCommanderAI timing
            fleetCommanderAI().nextTurn();
//            long spentMs = timeMs() - startMs;
//            if (options().debugAutoRun()) {
//            	String s = "fleetCommanderAI (id=" + id + ") duration = " + msToHMS(spentMs);
//            	turnLog(AUTORUN_OTHERFILE, s);
//            }
            ai().sendTransports();
        }

        if (isAIControlled()) {
            ai().treasurer().allocateReserve();
            // diplomatic activities
            for (EmpireView ev : empireViews()) {
                if ((ev != null) && ev.embassy().contact())
                    ev.setSuggestedAllocations();
            }
        }
        else {
            autospend();
            autotransport();
            // colonize first, then attack, then scout
			// BR: moved to Governor AI
			fleetCommanderAI().nextTurn();
			for (EmpireView ev : empireViews()) {
				if ((ev != null) && ev.embassy().contact())
					ev.setSuggestedAllocations();
			}
            // If planets are governed, redo allocations now
            // BR: I don't know why!? But done at this time, this gives the wrong result!!!
            // For "isFollowingColonyRequests" Governor, the valid allocations will be redone later
            for (int i = 0; i < this.sv.count(); ++i) {
                if (this.sv.empire(i) == this && this.sv.isColonized(i)) {
                    this.sv.colony(i).governIfNeeded();
                }
            }
        }

        // colony development (sometimes done for player if auto-pilot)
        NoticeMessage.setSubstatus(text("TURN_COLONY_SPENDING"));
        for (int n=0; n<sv.count(); n++) {
        	if (sv.empId(n) == id)
                if(!sv.colony(n).isGovernor() || isAIControlled() || sv.colony(n).shipyard().shipLimitReached() || sv.colony(n).shipyard().stargateCompleted()) //do not overrule the governor if it is enabled, except it's for ship-limit-reached
                    governorAI().setColonyAllocations(sv.colony(n));
        }
    }
    public void redoGovTurnDecisions() { // Only for player
    	for (int i = 0; i < sv.count(); ++i)
            if (sv.empire(i) == this && sv.isColonized(i))
                sv.colony(i).governIfPlayerHasRequest();
    }
	public void redoGovTurnDecisionsRich() { // Only for player
		for (int i = 0; i < sv.count(); ++i)
			if (sv.empire(i) == this && sv.isColonized(i))
				if (sv.isRich(i) || sv.isUltraRich(i))
					sv.colony(i).governIfNeeded();
	}

    /**
     * Spend reserve automatically (if enabled).
     *
     * Spend only on planets with production &lt; 30% average
     * Spend only the amount planet can consume this turn
     * Start with planet with lowest production, and end when money runs out
     * or no suitable planets are available.
     * Spend only on planets with governor on.
     * Spend only if industry and ecology are not complete.
     *
     */
    private void autospend() {
        GovernorOptions options = session().getGovernorOptions();
        if (isAIControlled() || !options.isAutospend()) {
            return;
        }
        // if reserve is low, don't even attempt to spend money
        if ((int)this.totalReserve() <= options.getReserve()) {
            return;
        }

        List<Colony> colonies = new LinkedList<>();

        for (StarSystem ss: this.colonizedSystems) {
        	if (ss != null) {
	            Colony c = ss.colony();
	            if (c.isGovernor() && c.maxReserveNeeded() >= 1) {
	                if (!c.industry().isCompleted() || !c.ecology().isCompleted()) {
	                    colonies.add(c);
	                }
	            }
        	}
        }
        Collections.sort(colonies,
                (Colony o1, Colony o2) -> (int)Math.signum(o1.production() - o2.production()));
//        for (Colony c: colonies) {
//            System.out.println("Autospend "+c.production()+" "+c.name());
//        }
        // spend money
        for (Colony c: colonies) {
        	if (c != null) {
	            if (c.maxReserveNeeded() <= 0) {
	                continue;
	            }
	            float available = totalReserve() - options.getReserve();
	            if (available <= 1) {
	                break;
	            }
	            // overallocate by 1 BC to speed up fractional spending
	            int bcToSpend = (int)Math.ceil(Math.min(available, c.maxReserveNeeded()));
	            allocateReserve(c, bcToSpend);
//                System.out.format("Autospend allocated %d bs to %s%n", bcToSpend, c.name());
        	}
        }
    }
    // New autotransport. Start with targets first.
    private void autotransport() {
        GovernorOptions options = govOptions();
        if (isAIControlled()) {
            return;
        }
        if(options.isAutotransportAI())
            ai.sendTransports();
        if(!options.isAutotransportFull())
            return;

        List<Colony> colonies = new LinkedList<>();
        for (int i = 0; i < this.sv.count(); ++i) {
            if (this.sv.empire(i) == this && this.sv.isColonized(i)) {
                Colony c = this.sv.colony(i);
                // don't transport to populations that are missing less than 4 population
                if (c.planet().currentSize() - c.expectedPopulationLongTerm() > 4 ) {
                    colonies.add(c);
                }
            }
        }
        // all colonies at full population- nothing to do
        if (colonies.isEmpty()) {
            return;
        }

        Collections.sort(colonies,
                (Colony o1, Colony o2) -> (int)Math.signum(o1.expectedPopPct() - o2.expectedPopPct()));
        /*for (Colony c: colonies) {
            System.out.println("Transport Recipient "+c.expectedPopPct()+" "+c.expectedPopulation()+"/"+c.planet().currentSize()+" "+c.name());
        }*/

        List<Colony> donors = new LinkedList<>();
        for (int i = 0; i < this.sv.count(); ++i) {
            if (this.sv.empire(i) == this && this.sv.isColonized(i)) {
                Colony c = this.sv.colony(i);
                // don't send population from planets with governor off
                if (!c.isGovernor() || c.noGovAutoTransport()) {
                    continue;
                }
                if (c.transporting() || !c.canTransport() || c.maxTransportsAllowed() < 1) {
                    continue;
                }
                // don't ship population from planets with 20 or less population
                if (c.planet().currentSize() <= 20) {
                    continue;
                }
                // we don't have excess population. Allow transporting 1 pop to stabilize planet ant max-1
                if (c.expectedPopulationLongTerm() < (c.planet().currentSize() - 2)) {
                    continue;
                }
                // if this option is checked, don't send out population out of planets which are Rich or Artefacts
                if (options.isTransportRichDisabled() &&
                        (c.planet().isResourceRich() || c.planet().isResourceUltraRich() ||
                                c.planet().isArtifact() || c.planet().isOrionArtifact())) {
                    continue;
                }
                // ship population out earlier, don't check if ecology is all done.
                // ship population out earlier, don't check if industry is all done.
                donors.add(c);
            }
        }
        /*for (Colony c: donors) {
            System.out.println("Transport Donor "+c.expectedPopulation()+"/"+c.planet().currentSize()+" "+c.name());
        }*/
        // no potential donors- no reason to do any of this.
        if (donors.isEmpty()) {
            return;
        }
        // for each underpopulated colony, find a suitable donor and ship some population
        // More expensive approach- after sending out some population, resort the target list, so that
        // population is more evenly distributed when there are many colonies that need population transported
        while (!colonies.isEmpty() && !donors.isEmpty()) {
            Colony c = colonies.get(0);
            float neededPopulation = (int) (c.planet().currentSize() - c.expectedPopulationLongTerm());
            // Sort donors by distance
            Colony donor = Collections.min(donors, (Colony o1, Colony o2) -> (int) Math.signum(
                    o1.travelTimeAdjusted(o1, c, this.tech().transportTravelSpeed()) -
                            o2.travelTimeAdjusted(o2, c, this.tech().transportTravelSpeed())));
            if (neededPopulation > 1) {
                float transportTime = donor.travelTimeAdjusted(donor, c, this.tech().transportTravelSpeed());
                // limit max transport time
                double maxTime = c.starSystem().inNebula() ? options.getTransportMaxTurns() * 1.5 : options.getTransportMaxTurns();
                if (transportTime > maxTime) {
                    // if first donor doesn't match max travel time, others surely won't.
                    // so no way we're going to transport population to this colony, remove it from the list
                    colonies.remove(c);
                    continue;
                }
                // TODO: Take into account proper governing, simulate X turns
                // That's close to impossible, Colony has multiple side effects on Planet and on Empire and
                // produces ships
                // if population will grow large enough naturally before transports arrive, don't transport.
                double expectedPopAtTransportTime = c.population() +
                        Math.pow(1+c.unrestrictedPopGrowth() / c.population(), transportTime);
                if (expectedPopAtTransportTime >= c.planet().currentSize()) {
                    // colony will be full by the time population arrives from the closest donor, skip this colony
                    colonies.remove(c);
                    continue;
                }

//                System.out.println("Will transport from "+donor.name()+" to "+c.name());
//                System.out.println("Before transport expectedPopulation= "+c.expectedPopulation());
                // if we expect transports in excess of maximum population, ship that away too.
                int expectedOverPopulation = Math.round(donor.expectedPopulationLongTerm() - donor.planet().currentSize());
                int growth = Math.max(1, Math.round(donor.unrestrictedPopGrowth()));
                //System.out.println("Donor "+donor.name()+" overPopulation="+expectedOverPopulation+" growth="+growth);
                int populationToTransport = Math.max(expectedOverPopulation, growth);
                // if this option is set, transport 2x population from poor planets
                if (options.isTransportPoorDouble() && (donor.planet().isResourcePoor() || donor.planet().isResourceUltraPoor()) ) {
                    populationToTransport *= 2;
                }
				// BR: to prevent abandon...
				donor.scheduleTransportsToSystem(c.starSystem(), populationToTransport, true);
//                System.out.println("After transport expectedPopulation="+c.expectedPopulation());
                donors.remove(donor);
            } else {
                colonies.remove(c);
            }
            Collections.sort(colonies,
                    (Colony o1, Colony o2) -> (int)Math.signum(o1.expectedPopPct() - o2.expectedPopPct()));
        }
    }

    public String decode(String s, Empire listener) {
        String s1 = this.replaceTokens(s, "my");
        s1 = listener.replaceTokens(s1, "your");
        return s1;
    }
    public String decode(String s, Empire listener, Empire other) {
        String s1 = this.replaceTokens(s, "my");
        s1 = listener.replaceTokens(s1, "your");
        s1 = other.replaceTokens(s1, "other");
        return s1;
    }
    public List<Transport> transports() {
        Galaxy gal = galaxy();
        List<Transport> transports = new ArrayList<>();
        for (Transport tr: gal.transports()) {
            if (tr != null && tr.empId() == id)
                transports.add(tr);
        }
        return transports;
    }
    public List<Transport> opponentsTransports() {
    	List<Transport> transports = new ArrayList<>();
        for (Ship sh: visibleShips)
            if (sh != null && sh.isTransport() && sh.empId()!=id)
            	transports.add((Transport)sh);
        return transports;
    }
    public int transportsInTransit(StarSystem s) {
        Galaxy gal = galaxy();
        int transports = s.orbitingTransports(id);
        
        for (Transport tr: gal.transports()) {
            if (tr != null && (tr.empId() == id) && (tr.destSysId() == s.id))
                transports += tr.size();
        }
        return transports;
    }
    public int enemyTransportsInTransit(StarSystem s) {	// BR: Never used
        int transports = s.orbitingTransports(id);
        
        boolean[] enemyMap = enemyMap();
        for (Ship sh: visibleShips) {
            if (sh != null && sh.isTransport()) {
                if (enemyMap[sh.empId()] && (sh.destSysId() == s.id))
                if (aggressiveWith(sh.empId()) && sh.destSysId() == s.id)
                    transports += ((Transport)sh).size();
            }
        }
        return transports;
    }
    public int unfriendlyTransportsInTransit(StarSystem s) {
        int transports = s.orbitingTransports(id);
        
        for (Ship sh: visibleShips) {
            if (sh != null && sh.isTransport()) {
                if (aggressiveWith(sh.empId()) && sh.destSysId() == s.id)
                    transports += ((Transport)sh).size();
            }
        }
        return transports;
    }
    public float transportTravelSpeed(IMappedObject fr, IMappedObject to) {
        if (!fr.passesThroughNebula(fr, to))
            return tech().transportTravelSpeed();

        float dist = fr.distanceTo(to);
        float time = fr.travelTimeAdjusted(fr, to, tech().transportTravelSpeed());
        return dist/time;
    }
    public void checkForRebellionSpread() {
        if (extinct)
            return;

        // for each rebelling colony, check the nearest 10 systems to see
        // if rebellion spreads to a nearby colony. Chance of spread is 5%
        // divided by the distance in l-y... i.e a colony 5 light-years away
        // has a 1% chance each turn to rebel
        //
        // this is a different and less extreme mechanic than the one described 
        // in the OSG (which was never implemented in actual MOO1 code anyway) 
        // and will scale better for really large maps. 
        
        // build a list of rebelling systems
        List<StarSystem> allSystems = allColonizedSystems();
        List<StarSystem> rebellingSystems = new ArrayList<>();
        for (StarSystem sys: allSystems) {
            if (sys != null && sys.colony().inRebellion()) {
                rebellingSystems.add(sys);
            }
        }

        // for each rebelling system, build a list of nearby systems that
        // are colonized by the same empire, are not the capital and not currently
        // rebelling. Check each of them for potential rebellion
        for (StarSystem sys: rebellingSystems) {
        	if (sys != null) {
	            for (int nearbyId: sys.nearbySystems()) {
	                StarSystem near = galaxy().system(nearbyId);
	                if ((near.empire() == this) && !near.colony().isCapital() && !near.colony().inRebellion()) {
	                    float pct = 0.05f / (sys.distanceTo(near));
	                    if (random() < pct)
	                        near.colony().spreadRebellion();
	                }
	            }
            }
        }
    }
    public boolean inRevolt() {
        float rebellingPop = 0;
        float loyalPop = 0;
        for (StarSystem sys: allColonizedSystems()) {
            if (sys != null && sys.colony().inRebellion())
                rebellingPop += sys.colony().population();
            else
                loyalPop += sys.colony().population();
        }
        return rebellingPop > loyalPop;
    }
    public void overthrowLeader() {
        if (isPlayer()) {
            //session().status().loseOverthrown();
            return;
        }

        leader = new Leader(this);
        for (EmpireView view: empireViews()) {
            if (view != null)
                view.breakAllTreaties();
        }
        
        // end all rebellions
        for (StarSystem sys: allColonizedSystems())
        	if (sys != null)
        		sys.colony().clearAllRebellion();

        if (viewForEmpire(player()).embassy().contact()) {
            String leaderDesc = text("LEADER_PERSONALITY_FORMAT", leader.personality(),leader.objective());
            String message = text("GNN_OVERTHROW", leaderDesc);
            message = replaceTokens(message, "alien");
            GNNNotification.notifyRebellion(message, false);
        }
    }
    public boolean inEconomicRange(int empId) {
        Empire e = galaxy().empire(empId);
        float range = max(e.scoutRange(), scoutRange());
        for (StarSystem sys: e.allColonizedSystems()) {
            if (sys != null && sv.distance(sys.id) <= range)
                return true;
        }
        return false;
    }
    public boolean hiddenSystem (StarSystem sys) {
    	if (!options().selectedDarkGalaxy())
        	return false;
   		return !inVisibleRange(sys);
    }
    private boolean inVisibleRange(StarSystem sys) {
    	// Test if inside empire range
    	float darkRange	= darkRange();
    	float empDist	= sv.distance(sys.id);
    	if (empDist <= darkRange )
    		return true;
    	// Could a ship extend the range?
    	float scanRange = shipScanningRange();
    	if (scanRange <= 0)
    		return false;
    	// Check if scanned by ship
    	List<ShipFleet> fleets = galaxy().ships.allFleets(id);
    	for (ShipFleet fl: fleets) {
    		if (fl != null && fl.distanceTo(sys) <= scanRange)
    			return true;
    	}
    	return false;
    }
    public boolean inShipRange(int empId) {
        Empire e = galaxy().empire(empId);
        for (StarSystem sys: e.allColonizedSystems()) {
            if (sys != null && sv.distance(sys.id) <= shipRange())
                return true;
        }
        return false;
    }
    // only used as a test method to accelerate testing
    // of the galactic council
    public void makeFullContact() {
        for (EmpireView v : empireViews()) {
            if ((v!= null) && !v.embassy().contact())
                v.embassy().makeFirstContact();
        }
    }
    public float orionCouncilBonus() {
        if (galaxy().orionSystem().empire() == this)
            return .2f;
        return 0;
    }
    public void addVisibleShip(Ship sh) {
        if (sh.visibleTo(id) && !visibleShips.contains(sh))
            visibleShips.add(sh);
    }
    private void addVisibleShips(List<? extends Ship> ships) {
        if (ships != null) {
            for (Ship sh: ships)
            	if (sh != null)
            		addVisibleShip(sh);
        }
    }
    private void addVisibleMonster(SpaceMonster sh) {
        if (sh.visibleTo(id) && !visibleMonsters.contains(sh))
            visibleMonsters.add(sh);
    }
    public List<ShipFleet> enemyFleets() {
        List<ShipFleet> list = new ArrayList<>();
        for (Ship sh: visibleShips()) {
            if (sh != null && sh instanceof ShipFleet) {
                ShipFleet fl = (ShipFleet) sh;
                if ((fl.empId() != id) && !fl.isEmpty())
                    list.add((ShipFleet) sh);
            }
        }
        return list;
    }
    public void refreshViews(boolean afterColony) { // BR: optimize recalcDistances
        // BR: Removed refreshViews from addColonizedSystem and removeColonizedSystem
        // the recalcDistances takes time and don't need to be called at each events
        // Grouped call later on with afterColony=true
        log(this + ": refresh views");
        if (recalcDistances) {
            NoticeMessage.setSubstatus(text("TURN_RECALC_DISTANCES"));
            sv.calculateSystemDistances();
            recalcDistances = false;
        }
        else if(afterColony) {
        	// BR: recalcDistances grouping
        	// to avoid this call after every colony gain / lost
        	// the end should be executed only when recalcDistances is required
        	return;
        }
        
        Galaxy gal = galaxy();
        for (int i=0;i<sv.count();i++) {
            StarSystem sys = gal.system(i);
            if (sys.empire() == this)
                sv.refreshFullScan(i);
            else if ((sys.orbitingFleetForEmpire(this) != null)
            && !sys.orbitingShipsInConflict())
                sv.refreshFullScan(i);
            else if (colonyCanScan(sys))
                sv.refreshLongRangePlanetScan(i);
            else if (fleetCanScan(sys))
                sv.refreshLongRangeShipScan(i);
            else if (sv.isScouted(i)) // don't keep stale fleet info
                sv.view(i).clearFleetInfo();
        }

        for (EmpireView v : empireViews()) {
            if (v!= null)
                v.refresh();
        }
        // Redetermines border/support/inner status for colonies
        for (int n=0;n<sv.count();n++)
            sv.resetSystemData(n);
    }
    public StarSystem closestSystem(IMappedObject mapObj) {
    	float minDistance = Float.MAX_VALUE;
    	StarSystem closestSystem = null;
    	for (StarSystem sys : colonizedSystems) {
    		if (sys != null) {
	    		float dist = sys.distanceTo(mapObj);
	    		if (dist < minDistance) {
	    			minDistance	  = dist;
	    			closestSystem = sys;
	    		}
    		}
    	}
    	return closestSystem;
    }
    public void setVisibleShips(int sysId) {
        addVisibleShips(sv.orbitingFleets(sysId));
        addVisibleShips(sv.exitingFleets(sysId));
    }
    public void updateScoutMessages() { // For player only
    	 List<ShipFleet> myOrbitingShips = galaxy().ships.allOrbitingFleets(id);
    	 for (ShipFleet sh : myOrbitingShips) {
    		 sv.refreshFullScan(sh.sysId());
    	 }
    }
	public void setVisibleShips()		{ setVisibleShips(false); }
	public void setVisibleShips(boolean trackUfo)	{
        Galaxy gal = galaxy();
        // This takes advantage of the fact that setVisibleShips() is called exactly once per turn.
        // BR: was not! added "boolean trackUfo" to make it true!
        // To strictly firewall off privileged information, the right thing to do would be to store the coordinates of ships seen last turn.
        // The *identity* of the ship, as represented by the Ship object, is not necessarily known to the empire.
        // But it's cheaper to just store the ships that were seen and ask them after-the-fact where they were last turn.
        final Set<Ship> shipsVisibleLastTurn = new HashSet<>(visibleShips);
        visibleShips.clear();

        List<ShipFleet> myShips = galaxy().ships.allFleets(id);
        List<StarSystem> mySystems = this.allColonizedSystems();

        // get transports in transit
        for (Transport tr : gal.transports()) {
            if (tr != null && (canSeeShips(tr.empId())
            			|| (tr.visibleTo(id) && canScanTo(tr, mySystems, myShips) )))
                addVisibleShip(tr);
        }

        // get fleets in transit
        for (ShipFleet sh : gal.ships.allFleets()) {
            if (sh != null && (canSeeShips(sh.empId())
            			|| (sh.visibleTo(id) && canScanTo(sh, mySystems, myShips) )))
                addVisibleShip(sh);
        }

		if (trackUfo &&				// Track only once a turn.
				!knowShipETA() &&	// Track only when we don't have ETA tech.
				options().selectedTrackUFOsAcrossTurns()) { // don't run the background tracking if it won't be displayed
			final Set<Ship> shipsKnowLastTurnLocationOf = ufoTracker()
					.matchShipsSeenThisTurnToShipsSeenLastTurn(visibleShips, shipsVisibleLastTurn).keySet();
			suspectedDestinationsOfVisibleShips = ufoTracker()
					.suspectedDestinationsOfShipsSeenLastTurn(shipsKnowLastTurnLocationOf);        	
		}

        // inform our spies!
        for (Ship fl : visibleShips) {
            if (fl != null && fl instanceof ShipFleet)
                detectFleet((ShipFleet)fl);
        }
    }
    public void setVisibleMonsters() {
        Galaxy gal = galaxy();
        // visibleMonsters = null; // BR: Remove
        if (visibleMonsters == null)
        	visibleMonsters = new ArrayList<>();
        else
        	visibleMonsters.clear();

        List<ShipFleet> myShips = gal.ships.allFleets(id);
        List<StarSystem> mySystems = this.allColonizedSystems();

        // get monsters in transit
        List<SpaceMonster> spaceMonsters = gal.spaceMonsters();
        for (SpaceMonster sh : spaceMonsters) {
            if (sh != null && sh.visibleTo(id) && canScanTo(sh, mySystems, myShips) )
            	addVisibleMonster(sh);
        }
    }
    public List<ShipFleet> getVisibleFleets() {
    	Galaxy gal = galaxy();
        List<ShipFleet> myFleets = galaxy().ships.allFleets(id);
        List<StarSystem> mySystems = this.allColonizedSystems();
        List<ShipFleet> fleets = new ArrayList<>();
        for (ShipFleet fl : gal.ships.allFleets())
        	if (fl != null && fl.isActive() && fl.visibleTo(id)
        		&& (canSeeShips(fl.empId()) || canScanTo(fl, mySystems, myFleets)))
        		fleets.add(fl);
        return fleets;
    }
    public List<ShipFleet> getEtaFleets() {
        List<ShipFleet> fleets = new ArrayList<>();
        for (ShipFleet fl : getVisibleFleets())
        	if (fl != null && !fl.isOrbiting() && knowETA(fl))
        		fleets.add(fl);
        return fleets;
    }
    public List<ShipFleet> getEtaFleets(StarSystem sys) {
        List<ShipFleet> fleets = new ArrayList<>();
        for (ShipFleet fl : getEtaFleets())
        	if (fl != null && sys == fl.destination())
        		fleets.add(fl);
        return fleets;
    }
    public boolean canScanTo(IMappedObject loc) {
        return planetsCanScanTo(loc) || shipsCanScanTo(loc);
    }
    private boolean planetsCanScanTo(IMappedObject loc) {
        if (planetScanningRange() == 0)
            return false;

        // could we use this.distanceTo() here?
        Galaxy gal = galaxy();
        for (int i=0; i<sv.count(); i++) {
            if ((sv.empire(i) == this) && (gal.system(i).distanceTo(loc) <= planetScanningRange()))
                return true;
        }
        return false;
    }
    private boolean canScanTo(IMappedObject loc, List<StarSystem> systems, List<ShipFleet> ships) {
        float planetRange = planetScanningRange();
        for (StarSystem sys: systems) {
            if (sys != null && sys.distanceTo(loc) <= planetRange)
                return true;
        }

        float shipRange = shipScanningRange();
        for (Ship sh: ships) {
            if (sh != null && sh.distanceTo(loc) <= shipRange)
                return true;
        }
        return false;
    }
    private boolean shipsCanScanTo(IMappedObject loc) {
        List<ShipFleet> fleets = galaxy().ships.allFleets(id);
        for (ShipFleet fl : fleets) {
            if (fl != null && fl.distanceTo(loc) <= shipScanningRange)
                return true;
        }
        return false;
    }
    float estimatedShipFirepower(Empire emp, int shipSize, int shieldLevel) {
        return 0;
    }
    public float estimatedShipFirepower(ShipDesign design, int shieldLevel) {
        if (design == null)
            return 0;
        ShipView shipView = shipViewFor(design);
        if (shipView == null)
            return estimatedShipFirepower(design.empire(), design.size(), shieldLevel);
        else
            return shipView.visibleFirepower(shieldLevel);
    }
    public float estimatedShipFirepowerAntiShip(ShipDesign design, int shieldLevel) {
        if (design == null)
            return 0;
        ShipView shipView = shipViewFor(design);
        if (shipView == null)
            return estimatedShipFirepower(design.empire(), design.size(), shieldLevel);
        else
            return shipView.visibleFirepowerAntiShip(shieldLevel);
    }
    public float estimatedFleetDamagePerRoundToArrivingTransports(List<ShipFleet> fleets) {
        float defenderDmg = 0;
            // modnar: use firepowerAntiShip to only count ship weapons that can hit ships
            // to prevent ground bombs from being able to damage transports
        for (ShipFleet fl : fleets) {
            if (fl != null && fl.empire().aggressiveWith(id))
                defenderDmg += fl.visibleFirepowerAntiShip(id, 0);
        }
        return defenderDmg;
    }
    public boolean canScoutTo(Location xyz) {	// BR: Never used
        Galaxy gal = galaxy();
        for (int i=0; i<gal.numStarSystems(); i++) {
           StarSystem s = gal.system(i);
            if ((s.empire() == this) && (s.distanceTo(xyz) <= tech.scoutRange())  )
                return true;
        }
        return false;
    }
    float distanceToSystem(StarSystem sys, List<StarSystem> froms) {
        float distance = Float.MAX_VALUE;
        for (StarSystem from: froms)
            distance = min(from.distanceTo(sys), distance);
        return distance;
    }
    public float distanceToEmpire(Empire emp) {
        float distance = Float.MAX_VALUE;
        for (StarSystem from: emp.colonizedSystems)
        	if (from != null)
        		distance = min(distanceToSystem(from, colonizedSystems), distance);
        return distance;
    }
    public float distanceTo(IMappedObject xyz) {
        float distance = Float.MAX_VALUE;

        Galaxy gal = galaxy();
        List<Empire> allies = this.allies();
        if (allies.isEmpty()) {
            // could we use allColonizedSystems() here?
            for (int i=0; i<gal.numStarSystems(); i++) {
                StarSystem s = gal.system(i);
                if (s.empire() == this)
                    distance = min(s.distanceTo(xyz), distance);
                if (distance == 0)
                    return distance;
            }
        }
        else {
            for (int i=0; i<gal.numStarSystems(); i++) {
                StarSystem s = gal.system(i);
                if (s.empire() != null && alliedWith(s.empire().id))
                    distance = min(s.distanceTo(xyz), distance);
                if (distance == 0)
                    return distance;
            }
        }
        return distance;
    }
    // If calculating square roots is a big enough problem that we have to cache them and have a special "Recalculating system distances"
    // popup when recalculating them, should we just...not take any square roots, and compare squared distances?
    public int rangeTo(StarSystem sys) {
        return (int) Math.ceil(sv.distance(sys.id));
    }
    public StarSystem mostPopulousSystemForCiv(Empire c) {
        StarSystem bestSystem, biggestSystem;

        bestSystem = null;
        biggestSystem = null;
        float maxPop1 = 0;
        float maxPop2 = 0;

        for (StarSystem sys: systemsForCiv(id(c))) {
        	if (sys != null) {
        		float sysPop = sv.population(sys.id);
	            Colony col = sv.colony(sys.id);
	            if (sysPop > maxPop1) {
	                maxPop1 = sysPop;
	                biggestSystem = sys;
	            }
	            if (col != null) {
	                if ((sysPop > maxPop2) && (! col.inRebellion()) ){
	                    maxPop2 = sysPop;
	                    bestSystem = sys;
	                }
	            }
        	}
        }

        if (bestSystem != null)
            return bestSystem;
        else
            return biggestSystem;
    }
/*    public boolean isAnyColonyConstructing(ShipDesign d) {
        for (int n=0;n<sv.count();n++) {
            if ((sv.empire(n) == this) && (sv.colony(n).shipyard().queuedBCForDesign(d) > 0))
                return true;
        }
        return false;
    } */
/*    public List<StarSystem> coloniesConstructing(ShipDesign d) {
        Galaxy gal = galaxy();
        List<StarSystem> colonies = new ArrayList<>();

        for (int i=0;i<sv.count();i++) {
            if ((sv.empire(i) == this) && (sv.colony(i).shipyard().queuedBCForDesign(d) > 0))
                colonies.add(gal.system(i));
        }
        return colonies;
    } */
    int shipDesignCount(int designId) {
        return galaxy().ships.shipDesignCount(id, designId);
    }
    public void swapShipConstruction(ShipDesign oldDesign) {
        swapShipConstruction(oldDesign, null);
    }
    public void swapShipConstruction(ShipDesign oldDesign, ShipDesign newDesign) {
        for (StarSystem sys: allColonizedSystems()) {
        	if (sys != null) {
	            ColonyShipyard shipyard = sys.colony().shipyard();
	            if (shipyard.design() == oldDesign) {
	                if ((newDesign != null) && newDesign.active())
	                    shipyard.switchToDesign(newDesign);
	                else if (isPlayer())
	                	shipyard.goToDefaultDesign();
	                else
	                    shipyard.goToNextDesign();
	            }
        	}
        }
    }
    public DiplomaticTreaty treatyWithEmpire(int empId) {
        if ((isNotEmpire(empId)) || (empId >= empireViews.length) || (empId == id))
            return null;
        
        return empireViews[empId].embassy().treaty();
    }
    public EmpireView viewForEmpire(Empire emp) {
        if ((emp != null) && (emp != this) && (emp.isEmpire()))
            return empireViews[emp.id];
        return null;
    }
    public EmpireView viewForEmpire(int empId) {
        if (isNotEmpire(empId) || (empireViews == null) || (empId >= empireViews.length) || (empId == id))
            return null;
        return empireViews[empId];
    }
	private boolean hasContact(EmpireView ev) {
		return (ev != null) && (ev.embassy().contact() && !ev.extinct());
	}
    public boolean hasContact(Empire c) {
        EmpireView v = viewForEmpire(c);
        return (v != null) && (v.embassy().contact() && !v.extinct());
    }
    void makeContact(Empire c) {
        EmpireView v = viewForEmpire(c);
        if (v != null)
            v.setContact();
    }
    public boolean atWar() {
        for (EmpireView v: empireViews()) {
            if ((v != null) && !v.extinct() && v.embassy().anyWar())
                return true;
        }
        return false;
    }
    public float powerLevel(Empire e) {
        return militaryPowerLevel(e) + industrialPowerLevel(e);
    }
    public float militaryPowerLevel(Empire e) {
        // TechTree t0 = e == this ? tech() : viewForEmpire(e).spies().tech();
        float fleet = totalFleetSize(e);
        float techLvl = (float)Math.pow(1 / miniFastRate, tech().avgTechLevel());
        return fleet*techLvl;
    }
    public float militaryPowerLevel() {
        float fleet = totalArmedFleetSize();
        float techLvl = (float)Math.pow(1 / miniFastRate, tech().avgTechLevel());
        return fleet*techLvl;
    }
    public float industrialPowerLevel(Empire e) {
        // TechTree t0 = e == this ? tech() : viewForEmpire(e).spies().tech();
        float prod = totalPlanetaryProduction(e);
        float transportModifier = 1;
        if(totalPlanetaryPopulation() > 0)
        {
            transportModifier = totalEmpirePopulation() / totalPlanetaryPopulation();
            prod *= transportModifier;
        }
        float techLvl = (float)Math.pow(1 / miniFastRate, tech().avgTechLevel());
        return prod*techLvl;
    }
    // modnar: add dynamic difficulty option, change AI colony production
    // create unscaled production power level, to avoid infinite recursion
    private float nonDynaIndPowerLevel() {
        float prod = nonDynaTotalProd();
        float techLvl = (float)Math.pow(1 / miniFastRate, tech().avgTechLevel());
        return prod*techLvl;
    }
    void clearDataForExtinctEmpire(int empId) {
        EmpireView view = viewForEmpire(empId);
        view.spies().shutdownSpyNetworks();
        
        // clear and re-add should be faster than removing ships
        // since each remove would recopy the list
        List<Ship> oldShips = new ArrayList<>(visibleShips());
        visibleShips.clear();
        for (Ship sh: oldShips) {
            if (sh != null && sh.empId() != empId)
               visibleShips.add(sh);
        }
        
        // clear out system view data. Inefficient on large maps
        int n = sv.count();
        for (int i=0;i<n;i++) {
            if (sv.empId(i) == empId)
                sv.view(i).goExtinct();
        }
    }
    public boolean hasAnyContact() {  return !contactedEmpires().isEmpty(); }

    public boolean inRangeOfAnyEmpire() {
        if (inRange < 0) {
            inRange = 0;
            for (EmpireView v: empireViews()) {
                if ((v!= null) && v.embassy().contact() && v.inEconomicRange())
                    inRange = 1;
            }
        }
        return inRange == 1;        
    }
    public List<Empire> contactedEmpires() {
        List<Empire> r = new ArrayList<>();

        for (EmpireView v: empireViews()) {
            if ((v!= null) && v.embassy().contact() && !v.extinct())
                r.add(v.empireUncut());
        }
        return r;
    }
/*    public List<EmpireView> contactedCivsThatKnow(Tech t) {
        List<EmpireView> r = new ArrayList<>();

        for (EmpireView cv : empireViews()) {
            if ((cv!= null) && cv.embassy().contact() && cv.spies().tech().knows(t))
                r.add(cv);
        }
        return r;
    } */
    public DiplomaticTreaty treaty(EmpireView v) {
        if (v == null)
            return null;
        else
            return v.embassy().treaty();
    }
    public DiplomaticTreaty treaty(Empire e) {
        EmpireView v = viewForEmpire(e.id);
        if (v == null)
            return null;
        else
            return v.embassy().treaty();
    }
    public int numColonies() {
    	if (isNotEmpire())
    		return 2;
        int count = 0;
        for (int n=0; n<sv.count(); n++) {
            if (sv.empire(n) == this)
                count++;
        }
        return count;
    }
    private float requestedTotalSpyCostPct() {
        float sum = 0;
        for (EmpireView ev : empireViews()) {
            if (ev != null)
                sum += ev.spies().allocationCostPct();
        }
        return sum;
    }
    public float totalSpyCostPct() {
        float requested = requestedTotalSpyCostPct();
        return min(0.5f, requested);
    }
    public float spySpendingModifier() {
        float requested = requestedTotalSpyCostPct();
        if (requested > 0.5f)
            return 0.5f / requested;
        return 1.0f;
    }
    public int totalActiveSpies() {
        int sum = 0;
        for (EmpireView ev : empireViews()) {
            if (ev != null)
                sum += ev.spies().numActiveSpies();
        }
        return sum;
    }
    private int securityAllocation()            {
    	if (securityAllocation > maxSecurityPct()) {
    		securityAllocation = maxSecurityPct();
    		flagColoniesToRecalcSpending();
    	}
    	return securityAllocation;
    }
    public int internalSecurity()            { return securityAllocation(); }
    public void internalSecurity(int i)      {
        securityAllocation = bounds(0,i,maxSecurityPct());
        flagColoniesToRecalcSpending();
    }
    public float internalSecurityPct()          { return (float) securityAllocation()/maxSecurityPct(); }
    public void increaseInternalSecurity(int i) { internalSecurity(securityAllocation+i); }
    public void decreaseInternalSecurity(int i) { internalSecurity(securityAllocation-i); }
    public void securityAllocation(float d) {
        // d assumed to be between 0 & 1, representing pct of slider clicked
        float incr = 1.0f/(maxSecurityPct()+1);
        float sum = 0;
        for (int i=0;i<maxSecurityPct()+1;i++) {
            sum += incr;
            if (d <= sum) {
                internalSecurity(i);
                return;
            }
        }
        internalSecurity(maxSecurityPct());
    }
    // BR: this was maxSecurityPct(), no more a constant
    private int maxSecurityPct()			{ return options().selectedMaxSecurityPct(); }
    public float internalSecurityCostPct()	{ return (totalInternalSecurityPct()/SECURITY_COST_RATIO); }
    public float totalSecurityCostPct()		{ return totalSpyCostPct() + internalSecurityCostPct(); }
    public float totalInternalSecurityPct()	{
        return inRangeOfAnyEmpire() ? SECURITY_COST_RATIO*securityAllocation()/100 : 0;
    }
    public float baseSpyCost() {
        return (25 + (tech.computer().techLevel()*2)) * spyCostMod();
    }
/*    public float troopKillRatio(StarSystem s) {
		// modnar: this old estimate gives completely wrong results for ground combat
        //float killRatio = (50 + tech.troopCombatAdj(false)) / (50 + sv.defenderCombatAdj(s.id));
		
		// modnar: correct ground combat ratio estimates
		float killRatio = 1.0f;
		if (sv.defenderCombatAdj(s.id) >= tech.troopCombatAdj(false)) {
			float defAdv = sv.defenderCombatAdj(s.id) - tech.troopCombatAdj(false);
			// killRatio = attackerCasualties / defenderCasualties
			killRatio = (float) ((Math.pow(100,2) - Math.pow(100-defAdv,2)/2) / (Math.pow(100-defAdv,2)/2));
		}
		else {
			float atkAdv = tech.troopCombatAdj(false) - sv.defenderCombatAdj(s.id);
			// killRatio = attackerCasualties / defenderCasualties
			killRatio = (float) ((Math.pow(100-atkAdv,2)/2) / (Math.pow(100,2) - Math.pow(100-atkAdv,2)/2));
		}
        return killRatio;
    } */
    public List<EmpireView> contacts() {
        List<EmpireView> r = new ArrayList<>();
        for (EmpireView v : empireViews()) {
            if ((v!= null) && !v.extinct() && v.embassy().contact())
                r.add(v);
        }
        return r;
    }
    public int numContacts() {
        int n = 0;
        for (EmpireView v : empireViews()) {
        	if ((v!= null) && !v.extinct() && v.embassy().contact())
                n++;
        }
        return n;
    }
/*    public List<EmpireView> commonContacts(Empire emp2) {
        List<EmpireView> r = new ArrayList<>();
        if (emp2.extinct)
            return r;
        for (EmpireView v : empireViews()) {
            if ((v!= null) && !v.extinct() && v.embassy().contact()) {
                if (v.is(emp2))
                    r.add(v);
                else {
                    EmpireView v2 = v.viewForEmpire(emp2);
                    if (v2.embassy().contact())
                        r.add(v);
                }
            }
        }
        return r;
    } */
    public int numEnemies() {
        int n = 0;
        for (EmpireView v : empireViews()) {
            if ((v!= null) && !v.extinct() && v.embassy().isEnemy())
//            && (v.embassy().anyWar() || v.embassy().onWarFooting()))
                n++;
        }
        return n;
    }
	public List<EmpireView> warEnemiesView() {	// BR: Never used
		List<EmpireView> r = new ArrayList<>();
		for (EmpireView v : empireViews())
			if ((v!= null) && !v.extinct() && v.embassy().anyWar())
				r.add(v);
		return r;
	}
    public List<Empire> warEnemies() {
        List<Empire> r = new ArrayList<>();
        for (EmpireView v : empireViews()) {
            if ((v!= null) && !v.extinct()
            && v.embassy().anyWar())
                r.add(v.empireUncut());
        }
        return r;
    }
	public List<EmpireView> enemiesView() {	// BR: Never used
		List<EmpireView> r = new ArrayList<>();
		for (EmpireView v : empireViews())
			if ((v!= null) && !v.extinct() && v.embassy().isEnemy())
//					&& (v.embassy().anyWar() || v.embassy().onWarFooting()))
				r.add(v);
		return r;
	}
    public List<Empire> enemies() {
        List<Empire> r = new ArrayList<>();
        for (EmpireView v : empireViews()) {
            if ((v!= null) && !v.extinct() && v.embassy().isEnemy())
                r.add(v.empireUncut());
        }
        return r;
    }
	private List<Integer> enemiesId() {
		List<Integer> r = new ArrayList<>();
		for (EmpireView v : empireViews())
			if ((v!= null) && !v.extinct() && v.embassy().isEnemy())
				r.add(v.empId());
		return r;
	}
    private boolean[] enemyMap() {
        // returns a boolean array where the index is an empire id and 
        // the array value is true if that empire is an "enemy"
        EmpireView[] empViews = empireViews();
        boolean[] map = new boolean[empViews.length];
        for (int i=0;i<map.length;i++) {
            EmpireView v = empViews[i];
            map[i] = (v != null) && !v.extinct() && v.embassy().isEnemy();
//                        && (v.embassy().anyWar() || v.embassy().onWarFooting());
        }
        return map;
    }
    public List<EmpireView> enemyViews() {
        List<EmpireView> r = new ArrayList<>();
        for (EmpireView v : empireViews()) {
            if ((v!= null) && !v.extinct() && v.embassy().isEnemy())
//            && (v.embassy().anyWar() || v.embassy().onWarFooting()))
                r.add(v);
        }
        return r;
    }
    public List<EmpireView> hostiles() {
        List<EmpireView> r = new ArrayList<>();
        for (EmpireView v : empireViews()) {
            if ((v!= null) && !v.extinct() && !v.embassy().isFriend())
                r.add(v);
        }
        return r;
    }
    public boolean hasNonEnemiesKnownBy(Empire e) {	// BR: Never used
        return !nonEnemiesKnownBy(e).isEmpty();
    }
    public List<Empire> nonEnemiesKnownBy(Empire empOther) { // TODO BR: to be replaced by list of EmpireView
        List<Empire> enemies = new ArrayList<>();
        // return any empires we are both in economic range of 
        // and this empire not already at war with
        List<Empire> contacts = contactedEmpires();
        contacts.remove(empOther);
        for (Empire emp: contacts) {
            if (emp != null
            		&& !atWarWith(emp.id)
            		&& inEconomicRange(emp.id)
            		&& empOther.inEconomicRange(emp.id)
            		&& !unityWith(emp.id))
                enemies.add(emp);
        }
        return enemies;
    }
    public List<Empire> allies() { // TODO BR: to be replaced by list of EmpireView
        List<Empire> r = new ArrayList<>();
        for (EmpireView v : empireViews()) {
            if ((v!= null) && !v.extinct() && v.embassy().isAlly())
                r.add(v.empireUncut());
        }
        return r;
    }
    public boolean hasAlliesKnownBy(Empire emp1) {	// BR: Never used
        for (EmpireView v : empireViews()) {
            if ((v!= null) && !v.extinct() && (!v.is(emp1)) && v.embassy().isAlly() && emp1.hasContact(v))
                return true;
        }
        return false;
    }
    public List<Empire> alliesKnownBy(Empire emp1) { 	// BR: Never used
        List<Empire> allies = new ArrayList<>();
        for (EmpireView v : empireViews()) {
            if ((v!= null) && !v.extinct() && (!v.is(emp1)) && v.embassy().isAlly() && emp1.hasContact(v))
                allies.add(v.empireUncut());
        }
        return allies;
    }
    public boolean friendlyWith(int empId) {
        if (empId == id) return true;
        if (isNotEmpire() || isNotEmpire(empId)) return false;

        EmpireView v = viewForEmpire(empId);
        return v == null ? false : v.embassy().isFriend();
    }
    public boolean pactWith(int empId) {
        if (empId == id) return true;
        if (isNotEmpire() || isNotEmpire(empId)) return false;

        EmpireView v = viewForEmpire(empId);
        return v == null ? false : v.embassy().pact();
    }
    /**
     * @param empId
     * @return true if is empire, is allied or is unity
     */
    public boolean alliedWith(int empId) {
        if (empId == id) return true;
        if (isNotEmpire() || isNotEmpire(empId)) return false;

        EmpireView v = viewForEmpire(empId);
        return v == null ? false : v.embassy().alliance() || v.embassy().unity();
    }
    private boolean unityWith(int empId) {
        if (empId == id) return true;
        if (isNotEmpire() || isNotEmpire(empId)) return false;

        EmpireView v = viewForEmpire(empId);
        return v == null ? false : v.embassy().unity();
    }
    public boolean tradingWith(Empire c) {
        if (c == this) return true;
        if (c == null) return false;
        if (isNotEmpire() || c.isNotEmpire()) return false;
        if (c.extinct) return false;

        EmpireView v = viewForEmpire(c);
        return v == null ? false : v.trade().active();
    }
    public boolean aggressiveWith(int empId) {
        if (empId == id)
        	return false;
        if (isNull(empId))
        	return false;
        if (isMonster())
        	return isEmpire(empId);
        if (isMonster(empId))
        	return isEmpire();

        EmpireView v = viewForEmpire(empId);
        if (v == null)
            return false;

        if (v.embassy().peaceTreatyInEffect())
            return false;
        return v.embassy().canAttackWithoutPenalty();
    }
    public boolean aggressiveWith(Empire c, StarSystem s) {
        if (c == this) return false;
        if (c == null) return false;
        if (isMonster())
        	return c.isEmpire();
        if (c.isMonster())
        	return isEmpire();
        if (c.extinct) return true;

        EmpireView v = viewForEmpire(c);
        if (v == null)
            return true;
        return v.embassy().canAttackWithoutPenalty(s);
    }
    public boolean atWarWith(int empId) {
        if (empId == id) return false;
        if (isNull(empId)) return false;
        if (isMonster())
        	return isEmpire(empId);
        if (isMonster(empId))
        	return isEmpire();

        EmpireView v = viewForEmpire(empId);
        if (v == null)
            return false;
        return v.embassy().anyWar();
    }
    public boolean atPeaceWith(int empId) {
        if (empId == id) return false;
        if (isNull(empId)) return false;
        if (isMonster())
        	return isEmpire(empId);
        if (isMonster(empId))
        	return isEmpire();

        EmpireView v = viewForEmpire(empId);
        if (v == null)
            return false;
        return v.embassy().peaceTreatyInEffect();
    }
    public boolean noTreatyWith(int empId) {
        if (empId == id) return false;
        if (isNull(empId)) return false;
        if (isMonster())
        	return isEmpire(empId);
        if (isMonster(empId))
        	return isEmpire();

        EmpireView v = viewForEmpire(empId);
        if (v == null)
            return false;
        return v.embassy().noTreaty();
    }
    public boolean hasTradeWith(Empire c) {
        if (c == this) return false;
        if (c == null) return false;
        if (isMonster() || c.isMonster()) return false;
        if (c.extinct) return false;

        EmpireView v = viewForEmpire(c);
        if (v == null)
            return false;
        return v.trade().active();
    }
    public int contactAge(Empire c) {	// BR: Never used
        if (c == this) return 0;
        if (c == null) return 0;
        if (isMonster() || c.isMonster()) return 0;
        if (c.extinct) return 0;

        EmpireView v = viewForEmpire(c);
        if (v == null)
            return 0;
        return v.embassy().contactAge();
    }
    void shareSystemInfoWithAlly(Empire c) {
        sv.shareAllyData(c.sv);
    }
    void shareSystemInfoWithAllies(SystemView v) {
        for (Empire ally: allies()) {
        	if (ally != null)
        		ally.sv.refreshAllySharingScan(v.sysId);
        }
    }
    /*
    public List<StarSystem> systemsNeedingTransports(int minTransport) {
        List<StarSystem> systems = new ArrayList<>();
        for (StarSystem sys: colonizedSystems) {
            if (sys != null &&
             		(sys.colony().inRebellion() || (sv.popNeeded(sys.id) >= minTransport)) )
                systems.add(sys);
        }
        return systems;
    }
    */
	public List<StarSystem> systemsInShipRange(EmpireView ev) {
		// returns list of systems in ship range
		// if ev provided, restricts list to that owner
		Galaxy gal = galaxy();
		List<StarSystem> systems = new ArrayList<>();
		for (int n=0; n<sv.count(); n++) {
			StarSystem sys = gal.system(n);
			if (sv.inShipRange(sys.id))
				if (ev == null || ev.is(sv.empire(sys.id)))
					systems.add(sys);
		}
		return systems;
	}
    public List<StarSystem> systemsInShipRange(Empire c) {
        // returns list of systems in ship range
        // if c provided, restricts list to that owner
        Galaxy gal = galaxy();
        List<StarSystem> systems = new ArrayList<>();
        for (int n=0;n<sv.count();n++) {
            StarSystem sys = gal.system(n);
            if (sv.inShipRange(sys.id)) {
                if ((c == null) || (sv.empire(sys.id) == c))
                    systems.add(sys);
            }
        }
        return systems;
    }
    /*
    public List<StarSystem> systemsSparingTransports(int minTransport) {
        List<StarSystem> systems = new ArrayList<>();
        for (StarSystem sys: colonizedSystems) {
            if (sys != null && !sys.colony().inRebellion() && sv.maxPopToGive(sys.id) >= minTransport )
                systems.add(sys);
        }
        return systems;
    }
    */
    public List<StarSystem> allColonizedSystems() {
        return colonizedSystems;
    }
    public List<StarSystem> allySystems() {
        Galaxy gal = galaxy();
        List<StarSystem> systems = new ArrayList<>();
        for (int i=0;i<sv.count();i++) {
            if (alliedWith(sv.empId(i)))
                systems.add(gal.system(i));
        }
        return systems;
    }
    public StarSystem colonyNearestToSystem(StarSystem sys) {	// BR: Never used
        List<StarSystem> colonies = new ArrayList<>(allColonizedSystems());
        colonies.remove(sys);
        if (colonies.isEmpty())
            return null;

        StarSystem.TARGET_SYSTEM = sys;
        Collections.sort(colonies, StarSystem.DISTANCE_TO_TARGET_SYSTEM);
        return colonies.get(0);
    }
    public int alliedColonyNearestToSystem(StarSystem sys, float speed) {
        List<StarSystem> colonies = allySystems();
        colonies.remove(sys);

        // build list of allied systems closest to sys, in travel turns (not distance)
        List<StarSystem> closestSystems = new ArrayList<>();
        int minTurns = Integer.MAX_VALUE;
        for (StarSystem colony: colonies) {
        	if (colony != null) {
	            int turns = (int) Math.ceil(colony.travelTimeTo(sys, speed));
	            if (turns < minTurns) {
	                closestSystems.clear();
	                closestSystems.add(colony);
	                minTurns = turns;
	            }
	            else if (turns == minTurns)
	                closestSystems.add(colony);
        	}
        }
        if (closestSystems.isEmpty())
            return StarSystem.NULL_ID;
        if (closestSystems.size() == 1)
            return closestSystems.get(0).id;
       
        // if there is more than one system within the minimum travel turns, 
        // choose the one closest, by distance
        StarSystem.TARGET_SYSTEM = sys;
        Collections.sort(closestSystems, StarSystem.DISTANCE_TO_TARGET_SYSTEM);
        return closestSystems.get(0).id;
    }
	public int optimalStagingPoint(StarSystem target, float speed) {
		IGameOptions opts	= options();
		// BR: Extends the list if more Retreat options are allowed,
		// but does not limit the list when Retreating fleets can not join.
		boolean hyperComExt	= opts.hyperComRetreatExtended() && tech().hyperspaceCommunications();
		boolean anyPlanet	= hyperComExt || opts.retreatToAnyPlanet();
		List<StarSystem> colonies = anyPlanet? systemsInShipRange((Empire)null) : allySystems();
		colonies.remove(target);
		if (colonies.isEmpty())
			return StarSystem.NULL_ID;
		if (colonies.size() == 1) {
			StarSystem col = colonies.get(0);
			return col == null? StarSystem.NULL_ID : col.id;
		}
		return optimalStagingPoint(target, speed, colonies);
	}
	public int optimalStagingPoint(StarSystem target, float speed, List<StarSystem> colonies) {
		//List<StarSystem> colonies = allySystems();
		//colonies.remove(target);

        // build a list of allied systems from sys that take the fewer travel turns
        // from that list, return the one with the greatest range
        // the idea is to try and keep the staging point outside of enemy sensor range
        // without hurting the travel time to the target
        List<StarSystem> closestSystems = new ArrayList<>();
        int minTurns = Integer.MAX_VALUE;
        for (StarSystem stagingPoint: colonies) {
            // modnar: don't allow colonies with enemy fleet in orbit be considered for stagingPoint
            if (stagingPoint != null && !stagingPoint.enemyShipsInOrbit(this)) {
                int turns = (int) Math.ceil(stagingPoint.travelTimeTo(target, speed));
                if (turns < minTurns) {
                    closestSystems.clear();
                    closestSystems.add(stagingPoint);
                    minTurns = turns;
                }
                else if (turns == minTurns)
                    closestSystems.add(stagingPoint);
            }
        }
        if (closestSystems.isEmpty())
            return StarSystem.NULL_ID;

        if (closestSystems.size() == 1)
            return closestSystems.get(0).id;

        Empire targetEmpire = target.empire();
        if (targetEmpire == null) 
            return closestSystems.get(0).id;

        float maxDistance = Float.MIN_VALUE;
        StarSystem bestStagingPoint = null;
        for (StarSystem stage: closestSystems) {
        	if (stage != null) {
	            float dist = targetEmpire.sv.distance(stage.id);
	            if (dist > maxDistance) {
	                maxDistance = dist;
	                bestStagingPoint = stage;
	            }
        	}
        }
        return bestStagingPoint == null ? StarSystem.NULL_ID : bestStagingPoint.id;
    }
	//public List<StarSystem> systemsForCiv(Empire e)		{ return systemsForCiv(id(e)); }
	public List<StarSystem> systemsForCiv(int empId)	{
		if (options().selectedDarkGalaxy())
			return systemsForCivDark(empId);
		else
			return systemsForCivClear(empId);
	}
	public List<StarSystem> systemsForCivNoSpy(int empId)	{
		IGameOptions opts = options();
		if (opts.selectedDarkGalaxy() && !opts.darkGalaxySpy())
			return systemsForCivDark(empId);
		else
			return systemsForCivClear(empId);
	}
	private List<StarSystem> systemsForCivClear(int empId)	{
		Galaxy gal = galaxy();
		List<StarSystem> systems = new ArrayList<>();
		for (int i=0; i<sv.count(); i++)
			if (sv.empId(i) == empId) 
				systems.add(gal.system(i));
		return systems;
	}
	private List<StarSystem> systemsForCivDark(int eId)	{
		Galaxy gal = galaxy();
		float range = darkRange();
		List<StarSystem> systems = new ArrayList<>();
		for (int i=0; i<sv.count(); i++)
			if (sv.empId(i) == eId && distanceTo(gal.system(i)) <= range)
				systems.add(gal.system(i));
		return systems;
	}
	//public List<StarSystem> systemsForCivDark(Empire e)	{ return systemsForCivDark(id(e)); }
	int numSystemsForCiv(int empId)	{
		if (options().selectedDarkGalaxy())
			return numSystemsForCivDark(empId);
		else
			return numSystemsForCivClear(empId);
	}
	private int numSystemsForCivClear(int empId)	{
		int num = 0;
		for (int n=0; n<sv.count(); n++) {
			if (sv.empId(n) == empId)
				num++;
		}
		return num;
	}
	//public int numSystemsForCiv(Empire e)		{ return numSystemsForCiv(id(e)); }
	private int numSystemsForCivDark(int empId)	{
		Galaxy gal = galaxy();
		float range = darkRange();
		int num = 0;
		for (int i=0; i<sv.count(); i++)
			if (sv.empId(i) == empId && distanceTo(gal.system(i)) <= range)
				num++;
		return num;
	}
    public int numColonizedSystems() {
        return colonizedSystems.size();
    }
    List<ShipFleet> fleetsForEmpire(Empire c) {
        List<ShipFleet> fleets2 = new ArrayList<>();
        for (Ship sh: visibleShips()) {
            if (sh != null && (sh.empId() == c.id) && sh instanceof ShipFleet)
                fleets2.add((ShipFleet) sh);
        }
        return fleets2;
    }
    public boolean anyUnexploredSystems() {	// BR: Never used
        for (int n=0;n<sv.count(); n++) {
            if (!sv.isScouted(n))
                return true;
        }
        return false;
    }
    public List<StarSystem> unexploredSystems() {
        Galaxy gal = galaxy();
        List<StarSystem> systems = new ArrayList<>();
        for (int n=0;n<sv.count(); n++) {
            if (!sv.isScouted(n))
                systems.add(gal.system(n));
        }
        return systems;
    }
    public List<StarSystem> uncolonizedPlanetsInShipRange(int newType) {
        Galaxy gal = galaxy();
        List<StarSystem> systems = new ArrayList<>();
        for (int i=0;i<sv.count();i++) {
            StarSystem sys = gal.system(i);
            if (sv.isScouted(i) && sv.inShipRange(i) && canColonize(sys.planet().type(), newType) && !sv.isColonized(i))
                systems.add(sys);
        }
        return systems;
    }
    public List<StarSystem> uncolonizedPlanetsInExtendedShipRange(int newType) {
        Galaxy gal = galaxy();
        List<StarSystem> systems = new ArrayList<>();
        for (int i=0;i<sv.count();i++) {
            StarSystem sys = gal.system(i);
            if (sv.isScouted(i) && sv.inScoutRange(i) && canColonize(sys.planet().type(), newType) && !sv.isColonized(i))
                systems.add(sys);
        }
        return systems;
    }
    public List<StarSystem> uncolonizedPlanetsInRange(float range) {
        Galaxy gal = galaxy();
        List<StarSystem> systems = new ArrayList<>();
        for (int i=0;i<sv.count();i++) {
            StarSystem sys = gal.system(i);
            if (sv.isScouted(i) && (sv.distance(i) <= range) && canColonize(sys.planet().type()) && !sv.isColonized(i))
                systems.add(sys);
        }
        return systems;
    }
    public PlanetType minUncolonizedPlanetTypeInShipRange(boolean checkHabitable) {	// BR: Never used
        // of all uncolonized planets in range that we can colonize
        // find the most hostile type... this guides the colony ship design
        PlanetType minType = PlanetType.keyed(PlanetType.TERRAN);
        for (int n=0;n<sv.count();n++) {
            if (sv.isScouted(n) && sv.inShipRange(n) && !sv.isColonized(n)) {
                PlanetType pType = sv.planetType(n);
                if (!checkHabitable || canColonize(pType)) {
                    if (pType.hostility() > minType.hostility())
                        minType = pType;
                }
            }
        }
        return minType;
    }
    public boolean knowsAllActiveEmpires() {	// BR: Never used
        for (Empire e: galaxy().activeEmpires()) {
            if (this != e) {
                if (!knowsOf(e))
                    return false;
            }
        }
        return true;
    }
    /**
     * !!! this is different than hasContacted() !!! 
     * Do you or any contacted empire has contact with Empire e?
     * The empire must be in economical Range
     * 
     * @param e Empire to have heard of
     * @return Do any of any contact has contact with this empire
     */
    public boolean knowsOf(Empire e) {
        if (e == null)
            return false;
        if (e == this)
            return true;
        if (hasContacted(e.id))
            return true;
        for (Empire emp : contactedEmpires()) {
        	if (emp != null) {
	            EmpireView v = this.viewForEmpire(emp.id);
	            if (v.inEconomicRange() && emp.hasContacted(e.id)) 
	                return true; // BR: Why different from knowsOf(int empId)
        	}
        }
        return false;
    }
    public boolean hasContacted(int empId) {
        EmpireView ev = this.viewForEmpire(empId);
        return (ev != null) && ev.embassy().contact();
    }
    /**
     * !!! this is different than hasContacted() !!! 
     * Do you or any contacted empire has contact with Empire e?
     * The empire is not required to be in economical range
     * 
     * @param empId id of Empire to have heard of
     * @return Do any of my contact has contact with this empire
     */
    public boolean knowsOf(int empId) {
        if (empId == Empire.NULL_ID)
            return false;
        if (empId == id)
            return true;
        if (hasContacted(empId))
            return true;
        for (Empire emp : contactedEmpires()) {
            if (emp != null && emp.hasContacted(empId))
                return true; // BR: Why different from knowsOf(Empire e)
        }
        return false;
    }
    public List<ShipFleet> ownFleetsTargetingSystem(StarSystem target) {
        List<ShipFleet> fleets1 = new ArrayList<>();
        for (ShipFleet fl : galaxy().ships.inTransitFleets()) {
            if (fl != null && fl.empId() == this.id) {
                if (fl.inTransit() && (fl.destSysId() == target.id))
                    fleets1.add(fl);
                else if (!fl.inTransit() && (fl.sysId() == target.id))
                    fleets1.add(fl);
            }
        }
        return fleets1;
    }
	public List<ShipFleet> ownFleetsDeployedToSystem(StarSystem target) {
		return galaxy().ships.deployedFleetsTo(id, target.id);
	}
    public void scrapExcessBases(StarSystem sys, int max) {	// BR: Never used
        if (sv.empire(sys.id) == this) {
            Colony col = sys.colony();
            if (col.defense().bases() > max) {
                log("civScrapBases  bases:", str(col.defense().bases()), " max: ", str(max), " cost: ", str(tech.newMissileBaseCost()));
                totalReserve += ((col.defense().bases() - max) * tech.newMissileBaseCost() / 4);
                col.defense().bases(max);
                //ai().setColonyAllocations(col);
                sv.refreshFullScan(sys.id);
            }
        }
    }
    public float bestEnemyShieldLevel() {
        float best = 0;
        for (EmpireView v : empireViews()) {
            if (v != null)
                best = max(best, v.spies().tech().maxDeflectorShieldLevel());
        }
        return best;
    }
    public float bestEnemyPlanetaryShieldLevel() {
        float best = 0;
        for (EmpireView v : empireViews()) {
            if (v != null) {
                float shieldLevel = v.spies().tech().maxDeflectorShieldLevel() + v.spies().tech().maxPlanetaryShieldLevel();
                best = max(best, shieldLevel);
            }
        }
        return best;
    }
    public void addToTreasury(float amt) {
        totalReserve += amt;
    }
    public void addReserve(float amt) {
        addToTreasury(amt/2);
    }
    void stealTech(String id) {
        tech().learnTech(id);
        log("Tech: "+tech(id).name(), " stolen");
    }
    public void learnTech(String techId) {
        boolean newTech = tech().learnTech(techId);
        if (newTech && isPlayerControlled()) {
            log("Tech: ", techId, " researched");
            DiscoverTechNotification.create(techId);
        }
        // share techs with New Republic allies
        for (EmpireView v: empireViews) {
            if ((v != null) && v.embassy().unity())
                v.techUncut().acquireTechThroughTrade(techId, id);
        }
    }
    /*
    public void acquireTechThroughTrade(String techId, int empId) {
        tech().acquireTechThroughTrade(techId, empId);
    }
*/
    public void plunderTech(Tech t, StarSystem s, Empire emp) {
        boolean newTech = tech().learnTech(t.id);
        if (newTech && isPlayerControlled()) {
            log("Tech: ", t.name(), " plundered from: ", s.name());
            PlunderTechNotification.create(t.id, s.id, emp.id);
        }
    }
    public void plunderShipTech(Tech t, int empId) {
        boolean newTech = tech().learnTech(t.id);
        if (newTech && isPlayerControlled()) {
            log("Ship tech: ", t.name(), " plundered ");
            PlunderShipTechNotification.create(t.id, empId);
        }
    }
    void plunderAncientTech(StarSystem s) {
        boolean isOrion = s.planet().isOrionArtifact();
        int numTechs = s.planet().bonusTechs();
        int levelDiff = isOrion ? 25: 10;
        int minLevel = isOrion ? 20: 1;
        s.planet().plunderBonusTech();
        long seed;
        if (options().isPersistentArtifact())
        	seed = s.seed() + s.id;
        else
        	seed = (long) rng().nextInt(); // To allow seed multiplication
        for (int i=0;i<numTechs;i++) {
        	long seed1 = seed * (i+1);
        	long seed2 = (seed+9) * (i+1);
            Tech t = tech().randomUnknownTech(minLevel, levelDiff, isPlayerControlled(), seed1, seed2);
            if (t == null) // if none found, then break out of loop
                break;
            boolean newTech = tech().learnTech(t.id);
            if (newTech && isPlayerControlled()) {
                log("Tech: ", t.name(), " discovered on: ", s.name());
                PlunderTechNotification.create(t.id, s.id, -1);
            }
        }
    }



    public int maxRobotControls() {
        return tech.topRobotControls() + robotControlsAdj();
    }
    public int baseRobotControls() {	// BR: Never used
        return TechRoboticControls.BASE_ROBOT_CONTROLS + robotControlsAdj();
    }
    public float workerProductivity() {
        float bookFormula = ((tech.planetology().techLevel() * 3) + 50) / 100;
        return bookFormula * workerProductivityMod();
    }
    public float totalIncome()                { return netTradeIncome() + totalPlanetaryIncome(); }
    public float netIncome()                  { return totalIncome() - totalShipMaintenanceCost() - totalStargateCost() - totalMissileBaseCost(); }
    public float empireTaxRevenue() { // Player only 
        if (empireTaxOnlyDeveloped()) // Player only
            return totalTaxableDevelopedPlanetaryProduction() * empireTaxPct() / 2; 
        else
            return totalTaxablePlanetaryProduction() * empireTaxPct() / 2; 
    }
    public float empireInternalSecurityCost() {
        return inRangeOfAnyEmpire() ? totalTaxablePlanetaryProduction() * internalSecurityCostPct() : 0f;
    }
    public float empireExternalSpyingCost()   { return totalTaxablePlanetaryProduction() * totalSpyCostPct(); }

    public boolean incrementEmpireTaxLevel()  { return empireTaxLevel(empireTaxLevel+1); }
    public boolean decrementEmpireTaxLevel()  { return empireTaxLevel(empireTaxLevel-1); }
    public float empireTaxPct()               { return (float) empireTaxLevel / 100; }
    public float maxEmpireTaxPct()            { return (float) maxEmpireTaxLevel()/100; }	// BR: Never used
    public int empireTaxLevel()               { return empireTaxLevel; }
    public boolean empireTaxOnlyDeveloped()   { return empireTaxOnlyDeveloped; }
    public void toggleEmpireTaxOnlyDeveloped(){ // Player only
        empireTaxOnlyDeveloped = !empireTaxOnlyDeveloped;
        if (empireTaxLevel > 0)
            flagColoniesToRecalcSpending();
    }
    public int maxEmpireTaxLevel()            { return 20; }
    public boolean empireTaxLevel(int i)      {
        int prevLevel = empireTaxLevel;
        empireTaxLevel = bounds(0,i,maxEmpireTaxLevel());
        
        if (empireTaxLevel != prevLevel)
            flagColoniesToRecalcSpending();
        return empireTaxLevel != prevLevel;
    }
    //ail: needs to be public for when spending in spy-network is adjusted
    public void flagColoniesToRecalcSpending() {
        // tax rate has changed in some way... flag colonies so they
        // recalc properly
        List<StarSystem> allSystems = allColonizedSystems();
        for (StarSystem sys: allSystems) {
        	if (sys != null) {
	            if(sys.colony() == null)
	                continue;
	            sys.colony().toggleRecalcSpending();
        	}
        }
    }
    public boolean hasTrade() {
        for (EmpireView v : empireViews()) {
            if ((v != null) && (v.trade().level() > 0))
                return true;
        }
        return false;
    }
    public float netTradeIncome() {
        float trade = totalTradeIncome();
        return trade <= 0 ? trade : trade * (1-tradePiracyRate);
    }
    public float totalTradeIncome() {
        float sum = 0;
        for (EmpireView v : empireViews()) {
            if (v != null)
                sum += v.trade().profit();
        }
        return sum;
    }
    public int totalTradeTreaties() {
        int sum = 0;
        for (EmpireView v : empireViews()) {
            if (v != null)
                sum += v.trade().level();
        }
        return sum;
    }
    public float totalFleetSize(Empire emp) {
        if (this == emp)
            return totalFleetSize();

        float spyPts = 0;
        FleetView fv = viewForEmpire(emp).spies().fleetView();
        if (!fv.noReport()) {
            spyPts += (fv.small()*ShipDesign.hullPoints(ShipDesign.SMALL));
            spyPts += (fv.medium()*ShipDesign.hullPoints(ShipDesign.MEDIUM));
            spyPts += (fv.large()*ShipDesign.hullPoints(ShipDesign.LARGE));
            spyPts += (fv.huge()*ShipDesign.hullPoints(ShipDesign.HUGE));
        }

        float visiblePts = 0;
        for (Ship sh : visibleShips()) {
            if ((sh.empId() == emp.id) && sh instanceof ShipFleet) {
                ShipFleet sh1 = (ShipFleet) sh;
                visiblePts += sh1.hullPoints();
            }
        }
        return max(spyPts, visiblePts);
    }
    public Float totalFleetSize() {
        float pts = 0;
        List<ShipFleet> fleets = galaxy().ships.allFleets(id);
        for (ShipFleet fl: fleets) 
            pts += fl.hullPoints();
        return pts;
    }
    public Float totalArmedFleetSize() {
        float pts = 0;
        int[] counts = galaxy().ships.shipDesignCounts(id);
        for (int i=0;i<ShipDesignLab.MAX_DESIGNS; i++) {
            ShipDesign d = shipLab().design(i);
            if (d.active() && d.isArmed() && !d.hasColonySpecial())
                pts += (counts[i] *d.hullPoints());
        }
        return pts;
    }
    public float totalFleetCost() {
        float pts = 0;
        List<ShipFleet> fleets = galaxy().ships.allFleets(id);
        for (ShipFleet fl: fleets)
            pts += fl.bcValue();
        return pts;
    }
    public Float totalEmpirePopulation() {
        float totalPop = 0;
        List<StarSystem> systems = new ArrayList<>(allColonizedSystems());
        for (StarSystem sys: systems)
        	if (sys != null)
        		totalPop += sys.colony().population();
        List<Transport> allTransports = transports();
        for (Transport tr: allTransports)
        	if (tr != null)
        		totalPop += tr.size();

        return totalPop;
    }
    public Float totalPlanetaryPopulation() {
        float totalPop = 0;
        List<StarSystem> systems = new ArrayList<>(allColonizedSystems());
        for (StarSystem sys: systems)
        	if (sys != null)
        		totalPop += sys.colony().population();
        return totalPop;
    }
    public float totalPlanetaryPopulation(Empire emp) {	// BR: Never used
        float totalPop = 0;
        if (emp == this) {
            List<StarSystem> systems = new ArrayList<>(allColonizedSystems());
            for (StarSystem sys: systems)
            	if (sys != null)
            		totalPop += sys.colony().population();
        }
        else {
            for (int n=0; n<sv.count(); n++) {
                if ((sv.empire(n) == emp))
                    totalPop += sv.population(n);
            }
        }
        return totalPop;
    }
    public float totalPlanetaryIncome() {
        float totalProductionBC = 0;
        List<StarSystem> systems = new ArrayList<>(allColonizedSystems());
        for (StarSystem sys: systems)
        	if (sys != null)
        		totalProductionBC += sys.colony().totalIncome();

        return totalProductionBC;
    }
    public float totalTaxablePlanetaryProduction() {
        float totalProductionBC = 0;
        List<StarSystem> systems = new ArrayList<>(allColonizedSystems());
        for (StarSystem sys: systems) {
        	if (sys != null) {
	            Colony col = sys.colony();
	            if (!col.embargoed())
	                totalProductionBC += col.production();
        	}
        }
        return totalProductionBC;
    }
    private float totalTaxableDevelopedPlanetaryProduction() { // Player only
        float totalProductionBC = 0;
        List<StarSystem> systems = new ArrayList<>(allColonizedSystems());
        for (StarSystem sys: systems) {
        	if (sys != null) {
	            Colony col = sys.colony();
	            if (!col.embargoed() && col.isDeveloped())
	                totalProductionBC += col.production();
        	}
        }
        return totalProductionBC;
    }
    public void recalcPlanetaryProduction() {
        totalEmpireProduction = -999;
        totalEmpireNonDynaProduction = -999;
        totalEmpireShipMaintenanceCost = -999;
        totalEmpireStargateCost = -999;
        totalEmpireMissileBaseCost = -999;
        empireNonDynaTechnoIndPower = -999;
        empireNonDynaPower = -999;
        inRange = -1;
    }
    public Float totalPlanetaryProduction() {
        if (totalEmpireProduction <= 0) {
            float totalProductionBC = 0;
            List<StarSystem> systems = new ArrayList<>(allColonizedSystems());
            for (StarSystem sys: systems)
            	if (sys != null)
            		totalProductionBC += sys.colony().production();
            totalEmpireProduction = totalProductionBC;
        }
        return totalEmpireProduction;
    }
    private float totalPlanetaryProduction(Empire emp) {
        if (emp == this)
            return totalPlanetaryProduction();

        float totalProductionBC = 0;
        for (int i=0; i<sv.count(); i++) {
            if ((sv.empire(i) == emp) && (sv.colony(i) != null))
                totalProductionBC += sv.colony(i).production();
        }
        return totalProductionBC;
    }
    // modnar: add dynamic difficulty option, change AI colony production
    // create unscaled production, nonDynaTotalProd, to avoid infinite recursion
    private Float nonDynaTotalProd() {
        if (totalEmpireNonDynaProduction <= 0) {
            float totalProductionBC = 0;
            List<StarSystem> systems = new ArrayList<>(allColonizedSystems());
            for (StarSystem sys: systems)
            	if (sys != null)
            		totalProductionBC += sys.colony().nonDynaProd();
            totalEmpireNonDynaProduction = totalProductionBC;
        }
        return totalEmpireNonDynaProduction;
    }
    public float nonDynaTotalProd(Empire emp) {	// BR: Never used
        if (emp == this)
            return nonDynaTotalProd();

        float totalProductionBC = 0;
        for (int i=0; i<sv.count(); i++) {
            if ((sv.empire(i) == emp) && (sv.colony(i) != null))
                totalProductionBC += sv.colony(i).nonDynaProd();
        }
        return totalProductionBC;
    }
	double empireNonDynaTechnoIndPower() {
		if (empireNonDynaTechnoIndPower <= 0)
			empireNonDynaTechnoIndPower = nonDynaIndPowerLevel();
		return empireNonDynaTechnoIndPower;
	}
	double empireNonDynaPower() {
		if (empireNonDynaPower <= 0) {
			double tech = (float)Math.pow(1 / miniFastRate, tech().avgTechLevel());
			double industrialPower = tech * nonDynaTotalProd();
			double militaryPower   = tech * totalFleetSize();
			empireNonDynaPower =  industrialPower + militaryPower;
		}
		return empireNonDynaPower;
	}
    private float totalShipMaintenanceCost() {
        if (totalEmpireShipMaintenanceCost < 0) {
            int[] counts = galaxy().ships.shipDesignCounts(id);
            float cost = 0;
            // modnar: newRace GearHead gets 50% ship maintenance cost
            // BR: Made dataRace call
            float raceBonus = maintenanceFactor();
            for (int i=0;i<counts.length;i++) 
                cost += (counts[i] * shipLab.design(i).cost());      
            totalEmpireShipMaintenanceCost = cost * SHIP_MAINTENANCE_PCT * raceBonus;
        }
        
        return totalEmpireShipMaintenanceCost;
    }
    private float totalStargateCost() {
        if (totalEmpireStargateCost < 0) {
            float totalCostBC = 0;
            // modnar: newRace GearHead gets 50% stargate maintenance cost
            // BR: Made dataRace call
            float raceBonus = maintenanceFactor();
            List<StarSystem> allSystems = new ArrayList<>(allColonizedSystems());
            for (StarSystem sys: allSystems)
            	if (sys != null)
            		totalCostBC += sys.colony().shipyard().stargateMaintenanceCost();
            totalEmpireStargateCost = totalCostBC * raceBonus;
        }
        return totalEmpireStargateCost;
    }
    public float totalMissileBaseCost() {
        if (totalEmpireMissileBaseCost < 0) {
            float totalCostBC = 0;
            // modnar: newRace GearHead gets 50% base maintenance cost
            // BR: Made dataRace call
            float raceBonus = maintenanceFactor();
            List<StarSystem> allSystems = new ArrayList<>(allColonizedSystems());
            Map<MissileBase, Float> baseCosts = new HashMap<>();
            for (StarSystem sys: allSystems)
            	if (sys != null)
            		totalCostBC += sys.colony().defense().missileBaseMaintenanceCost(baseCosts);
            totalEmpireMissileBaseCost = totalCostBC * raceBonus;
        }
        return totalEmpireMissileBaseCost;
    }
    public float shipMaintCostPerBC() {
        float empireBC = totalPlanetaryProduction();
        return totalShipMaintenanceCost() / empireBC;
    }
    public float stargateCostPerBC() {
        float empireBC = totalPlanetaryProduction();
        return totalStargateCost() / empireBC;
    }
    public float missileBaseCostPerBC() {
        float empireBC = totalPlanetaryProduction();
        return totalMissileBaseCost() / empireBC;
    }
    public float totalPlanetaryIndustrialSpending() {	// BR: Never used
        float totalIndustrialSpendingBC = 0;
        List<StarSystem> systems = new ArrayList<>(allColonizedSystems());
        for (StarSystem sys: systems)
        	if (sys != null)
        		totalIndustrialSpendingBC += (sys.colony().pct(Colony.INDUSTRY) * sys.colony().totalIncome());
        return totalIndustrialSpendingBC;
    }
    public float totalPlanetaryResearch() {
        if (tech().researchCompleted())
            return 0;
        float totalResearchBC = 0;
        List<StarSystem> systems = new ArrayList<>(allColonizedSystems());
        for (StarSystem sys: systems)
        	if (sys != null)
        		totalResearchBC += sys.colony().totalPlanetaryResearch(); // some research BC may stay with colony
        return totalResearchBC;
    }
    public float totalEmpireResearch(float totalRp) {	// BR: Never used
        TechTree t = tech();
        if (t.researchCompleted())
            return 0;
        float total = 0.0f;
        total += t.computer().currentResearch(totalRp);
        total += t.construction().currentResearch(totalRp);
        total += t.forceField().currentResearch(totalRp);
        total += t.planetology().currentResearch(totalRp);
        total += t.propulsion().currentResearch(totalRp);
        total += t.weapon().currentResearch(totalRp);
        return total;
    }
    public float totalPlanetaryResearchSpending() {
        if (tech().researchCompleted())
            return 0;
        float totalResearchBC = 0;
        List<StarSystem> systems = new ArrayList<>(allColonizedSystems());
        for (StarSystem sys: systems)
        	if (sys != null)
        		totalResearchBC += sys.colony().totalPlanetaryResearchSpending();
        return totalResearchBC;
    }
    Float totalPlanetaryFactories() {
        float factories = 0;
        List<StarSystem> systems = new ArrayList<>(allColonizedSystems());
        for (StarSystem sys: systems)
        	if (sys != null)
        		factories += sys.colony().industry().factories();
        return factories;
    }
    public float allocateReserve(Colony col, int amount) {
        float amt = min(totalReserve, amount);
        totalReserve -= amt;
        col.adjustReserveIncome(amt);
        return amt;
    }
    public void goExtinct() {
        // prevent double notifications
        if (extinct)
            return;

        if (lastAttacker instanceof Empire)
            GenocideIncident.create(this, (Empire) lastAttacker);
        GNNGenocideNotice.create(this, lastAttacker);

        extinct = true;
        // iterate over list copy to avoid comodification
        List<ShipFleet> fleets = galaxy().ships.allFleets(id);
        for (ShipFleet fl: fleets) {
	        if (fl != null) {
	            log("disband#1 fleet: ", fl.toString());
	            fl.disband();
        	}
        }

        galaxy().removeAllTransports(id);

        for (EmpireView v : empireViews()) {
            if (v != null)
            {
                v.embassy().removeContact();
                v.clearDataForExtinctEmpire(id);
            }
        }

        // BR: Clear lists to allow memory collection
        visibleShips.clear();
        suspectedDestinationsOfVisibleShips.clear();
        shipBuildingSystems.clear();
        colonizedSystems.clear();
        
        Galaxy g = galaxy();
        if (g.council().finalWar()) {
            g.council().removeEmpire(this);
        }
        else { 
            List<Empire> activeEmpires = galaxy().activeEmpires();
            // Player has gone extinct. Determine loss condition
            if (isPlayer()) {
                // no one killed us... abandonment suicide
                if (lastAttacker == null)
                    session().status().loseNoColonies();
                else
                    session().status().loseMilitary();
            }
            // an AI empire has gone extinct.. see if player win 
            // if only one empire is left then player must have won
            else if (activeEmpires.size() == 1)
                session().status().winMilitary();
            // multiple empires, all allied with player.. that's a win
            else if (galaxy().allAlliedWithPlayer() && !options().noAllianceCouncil()) 
                session().status().winMilitaryAlliance();
        }            
        status.assessTurn();
    }
    public ShipView shipViewFor(ShipDesign d ) {
        if (d == null)
            return null;

        Empire shEmp = d.empire();
        if (shEmp.isMonster())
            return new ShipView(d);
        if (shEmp == this)
            return shipLab.shipViewFor(d);

        EmpireView cv = viewForEmpire(d.empire());
        if (cv != null)
            return cv.spies().shipViewFor(d);

        return null;
    }
    private void detectFleet(ShipFleet fl) {
        EmpireView cv = viewForEmpire(fl.empire());
        if (cv == null)
            return;

        int[] visible = fl.visibleShips(id);
        for (int i=0;i<visible.length;i++) {
            if (visible[i] > 0)
                cv.spies().detectShip(fl.design(i));
        }
    }
    public Shape drawShape(Graphics2D g, int x, int y, int w, int h) {
        return drawShape(g,x,y,w,h,color());
    }
    public Shape drawShape(Graphics2D g, int x, int y, int w, int h, Color c) {
        // Color c1 = new Color(c.getRed(),c.getGreen(),c.getBlue(),192);
        g.setColor(c);
        int m = w/10;
        switch(shape()) {
            case Empire.SHAPE_SQUARE:
                Rectangle2D rect = new Rectangle2D.Float(x+m,y+m,w-m-m,h-m-m);
                g.fill(rect); 
                return rect;
            case Empire.SHAPE_DIAMOND:
                Polygon p = new Polygon();
                p.addPoint(x, y+h/2);
                p.addPoint(x+w/2, y);
                p.addPoint(x+w, y+h/2);
                p.addPoint(x+w/2, y+h);
                g.fill(p); 
                return p;
            case Empire.SHAPE_TRIANGLE1:
                Polygon p1 = new Polygon();
                p1.addPoint(x+w/2, y);
                p1.addPoint(x, y+h);
                p1.addPoint(x+w,y+h);
                g.fill(p1);
                return p1;
            case Empire.SHAPE_TRIANGLE2:
                Polygon p2 = new Polygon();
                p2.addPoint(x+w/2, y+h);
                p2.addPoint(x, y);
                p2.addPoint(x+w,y);
                g.fill(p2);
                return p2;
            case Empire.SHAPE_CIRCLE:
            default:
                Ellipse2D ell = new Ellipse2D.Float(x,y,w,h);
                g.fill(ell); 
                return ell;
        }
    }
    public void encounterFleet(ShipFleet fl) {
        if (fl == null)
            return;
        EmpireView cv = viewForEmpire(fl.empire().id);
        if (cv == null)
            return;

        int[] visible = fl.visibleShips(id);
        for (int i=0;i<visible.length;i++) {
            if (visible[i] > 0)
                cv.spies().encounterShip(fl.design(i));
        }
    }
    public void scanFleet(ShipFleet fl) {
        EmpireView cv = viewForEmpire(fl.empire());
        if (cv == null)
            return;

        int[] visible = fl.visibleShips(id);
        for (int i=0;i<visible.length;i++) {
            if (visible[i] > 0)
                cv.spies().scanShip(fl.design(i));
        }
    }
    public void scanDesign(ShipDesign st, Empire emp) {
        EmpireView cv = viewForEmpire(emp);
        if (cv != null)
            cv.spies().scanShip(st);
    }
    public StarSystem nextFlaggedSystems(StarSystem currSys, int flagId, boolean sameColor, boolean reverse) {
    	if (currSys == null)
    		return currSys;
    	LinkedList<StarSystem> list = new LinkedList<>();
        Galaxy gal = galaxy();
        int lim = sv.count();
        // Build the list
        if (sameColor) {
        	int colorId = sv.view(currSys.id).getFlagColor(flagId);
            for (int n=0; n<lim; n++)
                if (sv.view(n).hasFlagColor(flagId, colorId))
                	list.add(gal.system(n));
        }
        else
            for (int n=0; n<lim; n++)
                if (sv.view(n).hasFlag(flagId))
                	list.add(gal.system(n));
        if (list.size() <= 1)
            return currSys;

        // Sort the List
        Collections.sort(list, IMappedObject.MAP_ORDER);
        
        // Get Next index
        int index = list.indexOf(currSys);
        if (reverse) {
        	index--;
        	if (index < 0)
        		index = list.size()-1;
        }
        else {
        	index++;
        	if (index >= list.size())
        		index = 0;
        }
        return list.get(index);
    }
    public List<StarSystem> systemsWithStargate() {
        List<StarSystem> systemsWithStargate = new ArrayList<>();
        List<StarSystem> allSystems = new ArrayList<>(allColonizedSystems());
        for (StarSystem sys: allSystems)
        	if (sys != null && sys.hasStargate(this))
        		systemsWithStargate.add(sys);
        return systemsWithStargate;
    }
    public List<StarSystem> orderedColonies() {
        List<StarSystem> list = new ArrayList<>(allColonizedSystems());
        Collections.sort(list, IMappedObject.MAP_ORDER);
        return list;
    }
    public List<StarSystem> orderedTransportTargetSystems() {
        // we can only send transports to scouted, colonized
        // systems in range, with planets that we have the
        // technology to colonize
        Galaxy gal = galaxy();
        List<StarSystem> list = new ArrayList<>();
        for (int i=0; i<sv.count();i++) {
            StarSystem sys = gal.system(i);
            if (sv.inShipRange(i)
            && sv.isScouted(i)
            && sv.isColonized(i)
            && tech().canColonize(sys.planet().type()))
                list.add(sys);
        }
        Collections.sort(list, IMappedObject.MAP_ORDER);
        return list;
    }
    public List<StarSystem> orderedFleetTargetSystems(ShipFleet fl) {
        float range = fl.range();
        Galaxy gal = galaxy();
        List<StarSystem> list = new ArrayList<>();
        for (int n=0; n<sv.count();n++) {
            if (sv.withinRange(n, range))
                list.add(gal.system(n));
        }
        Collections.sort(list, IMappedObject.MAP_ORDER);
        return list;
    }
    public List<StarSystem> orderedShipConstructingColonies() {
        List<StarSystem> list = new ArrayList<>(shipBuildingSystems);
        Collections.sort(list, IMappedObject.MAP_ORDER);
        return list;
    }
    public List<ShipFleet> allFleets() {
        List<ShipFleet> list = new ArrayList<>();
        List<ShipFleet> fleets = galaxy().ships.allFleets(id);
        for (ShipFleet fl: fleets) {
            if (fl != null && !fl.isEmpty())
                list.add(fl);
        }
        return list;
    }
    public List<ShipFleet> orderedFleets() {
        List<ShipFleet> list = new ArrayList<>();
        List<ShipFleet> fleets = galaxy().ships.allFleets(id);
        for (ShipFleet fl: fleets) {
            if (fl != null && !fl.isEmpty())
                list.add(fl);
        }
        Collections.sort(list, IMappedObject.MAP_ORDER);
        return list;
    }
    public List<ShipFleet> orderedIdleFleets() {
        List<ShipFleet> list = new ArrayList<>();
        List<ShipFleet> fleets = galaxy().ships.allFleets(id);
        for (ShipFleet fl: fleets) {
            if (fl != null && !fl.isEmpty())
            {
                if(fl.isDeployed() || fl.inTransit())
                    continue;
                if(fl.system() != null && warEnemies().contains(fl.system().empire()))
                    continue;
                boolean allDesignsAuto = true;
                for(int i = 0; i < ShipDesignLab.MAX_DESIGNS; ++i)
                {
                    if(fl.num(i) > 0)
                    {
                        ShipDesign d = shipLab.design(i);
                        if(!d.isAutoScout() && !d.isAutoAttack() && !d.isAutoColonize())
                        {
                            allDesignsAuto = false;
                            break;
                        }
                    }
                }
                if(allDesignsAuto)
                    continue;
                list.add(fl);                
            }
        }
        Collections.sort(list, ShipFleet.COST);
        return list;
    }
    public List<ShipFleet> orderedAutoButIdleFleets() {
        List<ShipFleet> list = new ArrayList<>();
        List<ShipFleet> fleets = galaxy().ships.allFleets(id);
        for (ShipFleet fl: fleets) {
            if (fl != null && !fl.isEmpty())
            {
                if(fl.isDeployed() || fl.inTransit())
                    continue;
                if(fl.system() != null && warEnemies().contains(fl.system().empire()))
                    continue;
                boolean allDesignsAuto = true;
                for(int i = 0; i < ShipDesignLab.MAX_DESIGNS; ++i)
                {
                    if(fl.num(i) > 0)
                    {
                        ShipDesign d = shipLab.design(i);
                        if(!d.isAutoScout() && !d.isAutoAttack() && !d.isAutoColonize())
                        {
                            allDesignsAuto = false;
                            break;
                        }
                    }
                }
                if(allDesignsAuto)
                	list.add(fl);
            }
        }
        Collections.sort(list, ShipFleet.COST);
        return list;
    }
    public List<ShipFleet> orderedEnemyFleets() {
        List<ShipFleet> list = new ArrayList<>(enemyFleets());
        Collections.sort(list, IMappedObject.MAP_ORDER);
        return list;
    }
    public List<Ship> incomingKnownETAFleets(int sysId) {
    	List<Ship> list = new ArrayList<>();
    	List<Ship> vShips = visibleShips();
    	for (Ship sh: vShips) {
    		if (sh != null && sh.destSysId() == sysId) {
    			if (sh.inTransit()) {
	    			if (knowShipETA || sh.empId() == id)
	    				list.add(sh);
    			}
    			else if (sh.isDeployed() && (knowShipETA || sh.empId() == id))
    				list.add(sh);
    		}
    	}
    	return list;
    }
    public List<ShipFleet> visibleOrbitingFleet(StarSystem sys) {
    	List<ShipFleet> list = new ArrayList<>();
    	List<ShipFleet> vShips = sys.orbitingFleetsNoMonster();
    	for (ShipFleet fl: vShips) {
    		if (fl != null && visibleShips.contains(fl))
   				list.add(fl);
    	}
    	return list;
    }
    public List<StarSystem> orderedUnderAttackSystems(boolean showUnarmed, boolean showTransports) {
        List<StarSystem> list = new ArrayList<>();
        Galaxy g = galaxy();
        Empire pl = player();
        for (StarSystem sys: pl.allColonizedSystems()) {
            if (sys != null &&
            		(sys.enemyShipsInOrbit(pl) || sys.hasEvent())) // BR: to track incoming monsters
                list.add(sys);
        }
        if (knowShipETA) {
            List<Ship> vShips = player().visibleShips();
            for (Ship sh: vShips) {
                if (sh != null && sh.empId() != pl.id) {
                    StarSystem sys = g.system(sh.destSysId());
                    if (sys != null) {
                        // don't care about ships going to already-added systems or AI systems
                        if (!list.contains(sys) && (sys.empire() == pl)) {
                            Empire emp = g.empire(sh.empId());
                            // add if incoming fleet is hostile to player
                            if (emp.aggressiveWith(pl.id)) {
                                boolean showShip = showUnarmed
                                        || (showTransports && (sh instanceof Transport)) || sh.isPotentiallyArmed(pl);
                                if (showShip)
                                    list.add(sys);
                            }
                        }
                    }
                }
            }
        }
        Collections.sort(list, IMappedObject.MAP_ORDER);
        return list;
    }
    public static Set<Empire> allContacts(Empire e1, Empire e2) {
        Set<Empire> contacts = new HashSet<>();
        contacts.addAll(e1.contactedEmpires());
        contacts.addAll(e2.contactedEmpires());
        contacts.remove(e1);
        contacts.remove(e2);
        return contacts;
    }
    public void setEmpireMapAvgCoordinates() {
        Empire[] emps = galaxy().empires();
        float[] xAvg = new float[emps.length];
        float[] yAvg = new float[emps.length];
        float[] xMin = new float[emps.length];
        float[] xMax = new float[emps.length];
        int[] num = new int[emps.length];
        float[] xAvgD = new float[emps.length];
        float[] yAvgD = new float[emps.length];
        float[] xMinD = new float[emps.length];
        float[] xMaxD = new float[emps.length];
        int[] numD = new int[emps.length];
        
        for (int i=0;i<emps.length;i++) {
        	xMin[i]  = Float.MAX_VALUE;
        	xMinD[i] = Float.MAX_VALUE;
        }
        
        int n = galaxy().numStarSystems();
        float darkRange = darkRange();
        for (int i=0; i<n; i++) {
            int empId = sv.empId(i);
            if (empId >= 0) {
                if (!sv.name(i).isEmpty()) {
                    StarSystem sys = sv.system(i);
                    xAvg[empId] += sys.x();
                    yAvg[empId] += sys.y();
                    xMin[empId] = min(xMin[empId], sys.x());
                    xMax[empId] = max(xMax[empId], sys.x());
                    num[empId]++;
                    if (this.distanceTo(sys) <= darkRange) {
                        xAvgD[empId] += sys.x();
                        yAvgD[empId] += sys.y();
                        xMinD[empId] = min(xMinD[empId], sys.x());
                        xMaxD[empId] = max(xMaxD[empId], sys.x());
                        numD[empId]++;                    	
                    }
                }
            }
        }
        
        for (Empire emp: emps) {
            int id = emp.id;
            emp.avgX	= xAvg[id]/num[id];
            emp.avgY	= yAvg[id]/num[id];
            emp.nameX1	= xMin[id];
            emp.nameX2	= xMax[id];

            emp.avgXd	= xAvgD[id]/numD[id];
            emp.avgYd	= yAvgD[id]/numD[id];
            emp.nameX1d	= xMinD[id];
            emp.nameX2d	= xMaxD[id];
        }  
    }
    public void draw(GalaxyMapPanel map, Graphics2D g2, boolean darkMode) {
    	if (darkMode)
            draw(map, g2, nameX1d, nameX2d, avgXd, avgYd);
    	else
    		draw(map, g2, nameX1, nameX2, avgX, avgY);
    }
    public void draw(GalaxyMapPanel map, Graphics2D g2, float xMin, float xMax, float xAvg, float yAvg) {
        if (map.hideSystemNames())
            return;
        
        // old save: new var hasn't been calculated yet
        if (avgX == 0)
            return;
        
        float empW = (xMax-xMin)*2/3;
        float adj = max(0,3-empW);
        int x0 = map.mapX(xMin-adj);
        int x1 = map.mapX(xMax+adj);
        
        int mapX = map.mapX(xAvg);
        int mapY = map.mapY(yAvg);
        String longName = "XXXXXXXXXX";
        String name = raceName();
        int fontSize = scaledFont(g2,longName,x1-x0,60,12);
        //int fontSize = max(12, min(40, (int) (50*(x1-x0)/scale)));
        if (fontSize >= 12) {
            if (!name.isEmpty()) {
                g2.setFont(narrowFont(fontSize));
                g2.setColor(nameColor());
                int sw = g2.getFontMetrics().stringWidth(name);
                int x = mapX - (sw/2);
                int y = mapY - (fontSize/2);
                drawString(g2,name, x, y);
            }
        }
    }
    // BR:
    /**
	 * @return the current number of companion worlds
	 */
	public int getCompanionWorldsNumber() {
        if (compSysId != null) {
        	return compSysId.length;
        }
        return 0;
	} // \BR
    /**
	 * @return the current Name of Home World
	 */
	@Override public String getHomeWorldName() { return galaxy().system(homeSysId).name(); }
    // BR:
    /**
     * Change Home World and Companions Name
	 * @param NewName the new HomeWorld Name
	 */
	public void setHomeWorldName(String newName) {
		sv.name(homeSysId, newName);
        int numCompWorlds = getCompanionWorldsNumber();
        if (numCompWorlds > 0) { 
            for (int id = 0; id < numCompWorlds; id++) {
               	String name = GalaxyFactory.compSysName[id] + " " + newName;
               	sv.name(compSysId[id], name);
             }
        }
	} // \BR
    public static Comparator<Empire> TOTAL_POPULATION = (Empire o1, Empire o2) -> o2.totalPlanetaryPopulation().compareTo(o1.totalPlanetaryPopulation());
    public static Comparator<Empire> TOTAL_PRODUCTION = (Empire o1, Empire o2) -> o2.totalPlanetaryProduction().compareTo(o1.totalPlanetaryProduction());
    public static Comparator<Empire> AVG_TECH_LEVEL   = (Empire o1, Empire o2) -> o2.tech.avgTechLevel().compareTo(o1.tech.avgTechLevel());
    public static Comparator<Empire> TOTAL_FLEET_SIZE = (Empire o1, Empire o2) -> o2.totalFleetSize().compareTo(o1.totalFleetSize());
    public static Comparator<Empire> RACE_NAME        = (Empire o1, Empire o2) -> o1.raceName().compareTo(o2.raceName());
    public static Comparator<Empire> HISTORICAL_SIZE  = (Empire o1, Empire o2) -> Base.compare(o2.numColoniesHistory, o1.numColoniesHistory);
    public static Comparator<Empire> BENCHMARK        = (Empire o1, Empire o2) -> Base.compare(o2.benchmark, o1.benchmark);
	static Comparator<Empire> NON_DYNA_POWER   = (Empire o1, Empire o2) -> Double.compare(o2.empireNonDynaPower(), o1.empireNonDynaPower());
	static Comparator<Empire> NON_DYNA_TEC_IND = (Empire o1, Empire o2) -> Double.compare(o2.empireNonDynaTechnoIndPower(), o1.empireNonDynaTechnoIndPower());
	// ==================== EmpireBaseData ====================
	//
	public static final class EmpireBaseData {
		public String raceKey;
		public String dataRaceKey;
		public String empireName;
		public String dataName; // BR: To validate for custom Races
		public String raceName; // BR: To validate for custom Races
		public String leaderName;
		public boolean isCustomRace;
		public DynOptions raceOptions;
		private int raceAI;
		private Personality personality;
		private Objective objective;
		public CivilizationId civilizationId;

		public SystemBaseData homeSys;
		private int[] compSysId;
		public SystemBaseData[] companions;
		private Long randomSource;

		public EmpireBaseData(Empire src, SystemBaseData[] systems) {
			src.validateOnLoad();
			randomSource = src.randomSource;
			raceKey		 = src.raceKey;
			dataRaceKey	 = src.dataRaceKey;
			empireName	 = src.name();
			raceName	 = src.raceName();
			leaderName	 = src.leader().name();
			isCustomRace = src.isCustomSpecies();
			if (isCustomRace)
				dataName = src.setupName();
			else 
				dataName = src.animSetupName();
			raceOptions	 = src.speciesOptions();
			raceAI		 = src.selectedAI;
			personality	 = src.leader().personality;
			objective	 = src.leader().objective;

			homeSys 	= systems[src.homeSysId()];
			compSysId	= src.compSysId;
			int compNum = src.getCompanionWorldsNumber();
			if(compSysId != null) {
				companions = new SystemBaseData[compNum];
				for (int i=0; i<compNum; i++)
					companions[i] = systems[compSysId[i]];
			}
			civilizationId = src.getRawCivilizationId(src.raceNameIndex);
		}
		public void setRace(String r, String dr, boolean isCR, DynOptions options, int ai, CivilizationId civId) {
			raceKey		 = r;
			dataRaceKey	 = dr;
			isCustomRace = isCR;
			raceOptions	 = options;
			raceAI		 = ai;
			civilizationId = civId;
		}
		public void raceAI(int ai)	{ raceAI = ai; }
		public int  raceAI()		{ return raceAI; }
		private Long randomSource()	{ return randomSource == null? Rotp.rand().nextLong() : randomSource; }
	}
}
