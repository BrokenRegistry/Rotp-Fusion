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

import static rotp.model.planet.PlanetType.ARID;
import static rotp.model.planet.PlanetType.BARREN;
import static rotp.model.planet.PlanetType.DEAD;
import static rotp.model.planet.PlanetType.DESERT;
import static rotp.model.planet.PlanetType.INFERNO;
import static rotp.model.planet.PlanetType.JUNGLE;
import static rotp.model.planet.PlanetType.MINIMAL;
import static rotp.model.planet.PlanetType.NONE;
import static rotp.model.planet.PlanetType.OCEAN;
import static rotp.model.planet.PlanetType.RADIATED;
import static rotp.model.planet.PlanetType.STEPPE;
import static rotp.model.planet.PlanetType.TERRAN;
import static rotp.model.planet.PlanetType.TOXIC;
import static rotp.model.planet.PlanetType.TUNDRA;
import static rotp.ui.util.ParamFlagColor.flagCount;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;

import rotp.model.ai.FleetPlan;
import rotp.model.colony.Colony;
import rotp.model.events.SystemScoutedEvent;
import rotp.model.galaxy.IMappedObject;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.IFlagOptions;
import rotp.model.planet.Planet;
import rotp.model.planet.PlanetType;
import rotp.util.Base;
import rotp.util.ImageManager;

public class SystemView implements IMappedObject, IFlagOptions, Base, Serializable {
    private static final long serialVersionUID = 1L;
    protected static final int UNIMPORTANT   = 0;
    protected static final int INNER_SYSTEM  = 1;
    protected static final int BORDER_SYSTEM = 2;
    protected static final int ATTACK_TARGET = 3;
    public	  static final int SET_RALLY     = 1;
    public	  static final int PREVIEW_RALLY = 0;
    public	  static final int CLEAR_PREVIEW = -1;

    // BR: Flag locations
    private static final int SIDE		= 133;			// Flag icon width and height
    private static final int MID1		= SIDE/2+8;		// Right position of Left flag
    private static final int MID2		= SIDE - MID1;	// Left position of Right flag
    private static final int MAST		= 97; 			// Left position of Mast in source flag
    private static final int FLAG_W		= MID1;			// Cut width
    private static final float LF_SCALE	= 0.75f;		// Little flags scale ratio
    private static final int LF_CUT		= 5;			// Little flags bottom cut
    private static final int LF_WIDTH	= Math.round(LF_SCALE * SIDE); // Little flags Width
    private static final int LF_LEFT	= (SIDE-LF_WIDTH) /2;	// Little flags Left Margin
    private static final int LF_RIGHT	= LF_LEFT + LF_WIDTH;	// Little flags Right Margin
    private static final int LF_BOTTOM	= SIDE;					// Little flags Bottom Position
    private static final int LF_TOP		= SIDE - LF_WIDTH;		// Little flags Top Position

    private static final List<String> flagImageNameList = Arrays.asList (
			"Flag_None",
			"Flag_White",
			"Flag_Red",
			"Flag_Blue",
			"Flag_Green",
			"Flag_Yellow",
			"Flag_Aqua",
			"Flag_Orange",
			"Flag_LtBlue",
			"Flag_Purple",
			"Flag_Pink",
			"Flag_DkGreen"
			);
    private static final List<String> mapFlagImageNameList = Arrays.asList (
			"Flag_NoneM",
			"Flag_WhiteM",
			"Flag_RedM",
			"Flag_BlueM",
			"Flag_GreenM",
			"Flag_YellowM",
			"Flag_AquaM",
			"Flag_OrangeM",
			"Flag_LtBlueM",
			"Flag_PurpleM",
			"Flag_PinkM",
			"Flag_DkGreenM"
			);

    public  static final int FLAG_NONE = 0;

    // BR: flagColorCount
    private static final int BASE_MASK_COLOR = 127; // "only" 128 colors to avoid sign management
    private static final int[] COLOR_SHIFT = {1, 256, 65536, 16777216};
    private static final int[] COLOR_MASK = { 
    		BASE_MASK_COLOR * COLOR_SHIFT[0],
    		BASE_MASK_COLOR * COLOR_SHIFT[1],
    		BASE_MASK_COLOR * COLOR_SHIFT[2],
    		BASE_MASK_COLOR * COLOR_SHIFT[3]};
    private static final int[] COLOR_IMASK = { // Inverted Mask
    		-1 - COLOR_MASK[0],
    		-1 - COLOR_MASK[1],
    		-1 - COLOR_MASK[2],
    		-1 - COLOR_MASK[3]};

    public static SystemView create(int sysId, int empId) {
        return new SystemView(sysId,empId);
    }

    public final int ownerId; // Owner of the view
    public final int sysId;
    private StarSystem relocationSystem;
    private float hostilityLevel = 0;
    private int locationSecurity = INNER_SYSTEM;
    private float scoutTime = 0;
    private float spyTime = 0;

    // viewed variables
    private Empire vEmpire; // Owner of the system
    private String vName = "";
    private Planet vPlanet;
    private String vPlanetTypeKey;
    private final List<ShipFleet> vOrbitingFleets = new ArrayList<>();
    private final List<ShipFleet> vExitingFleets = new ArrayList<>();
    private boolean vGuarded = false;
    private int vBases = 0;
    private int vShieldLevel = 0;
    private int vFactories = 0;
    private int vPopulation = 0;
    private int vCurrentSize = 0;
    private int vArtifacts = 0;
    private boolean vStargate = false;
    private int flagColor = FLAG_NONE;
    private boolean forwardRallies = options().defaultForwardRally();

    private transient Empire owner;
    private transient PlanetType vPlanetType;
    private transient FleetPlan fleetPlan;

    /**
     * @return Ordered List of Flag Colors // BR:
    public static List<String> getFlagList() {
    	return Arrays.asList("NONE", "WHITE", "RED", "BLUE", "GREEN", "YELLOW"
    				 , "AQUA", "ORANGE", "LIGHT BLUE", "PURPLE", "PINK");
    } */
    public List<ShipFleet> orbitingFleets()  { return vOrbitingFleets; }
    public List<ShipFleet> exitingFleets()   { return vExitingFleets; }
    public StarSystem system()               { return galaxy().system(sysId); }
    public String name()                     { return vName; }
    public void name(String s) {
        system().name(s);
        vName = s;
    }
    public Empire owner() { 
        if (owner == null)
            owner = galaxy().empire(ownerId);
        return owner; 
    }
    public Empire empire()                   { return vEmpire; }
    public int empId()                       { return id(vEmpire); }
    public int population()                  { return vPopulation; }
    public int bases()                       { return vBases; }
    public int shieldLevel()                 { return vShieldLevel; }
    public int factories()                   { return vFactories; }
    public int currentSize()                 { return vCurrentSize; }
    public int maxTransToFill()              {
        if (colony().inRebellion())
            return 0;
        return colony().maxTransportToFill();
    }
    public int maxTransNoLoss()              {
        if (colony().inRebellion())
            return 0;
        return colony().maxTransportNoLoss();
    }
    public boolean showTransports()          { return colony().showTransports(); }
    public int artifacts()                   { return vArtifacts; }
    public boolean stargate()                { return vStargate; }
    public void removeStargate()             { vStargate = false;
        if (isColonized())
            colony().removeStargate();
    }
    public Planet planet()                   { return vPlanet; }
    public int locationSecurity()            { return locationSecurity; }
    public float hostilityLevel()            { return hostilityLevel; }
    public float spyTime()                   { return spyTime; }
    public float scoutTime()                 { return scoutTime; }
    public boolean isGuarded()               { return vGuarded; }
    public StarSystem rallySystem()          { return relocationSystem; }
    public float spyTurn()                   { return spyTime - galaxy().beginningYear(); }
    public float scoutTurn()                 { return scoutTime - galaxy().beginningYear(); }
    public StarSystem rallyNearestStarGate(int action, Float speed) {
    	return rallyNearestSystem(owner().systemsWithStargate(), action, speed);
    }
    public StarSystem rallyNearestSystem(List<StarSystem> destList, int action, Float speed)  {
    	if (action == CLEAR_PREVIEW)
    		system().rallySprite().previewDestRally(null);
    	if (destList.isEmpty())
        	return null;

    	StarSystem from = system();
    	if (speed == null)
    		speed = options().chainRallySpeed(owner());
    	
    	//float topSpeed = owner().tech().topSpeed();
    	int destSize = destList.size();
    	int minTime = Integer.MAX_VALUE;
    	float minDist = Float.MAX_VALUE;
    	int minIdx = -1;
    	for (int dest=0; dest<destSize; dest++) {
    		int travelTime = (int) Math.ceil(from.travelTimeTo(destList.get(dest), speed));
    		float distance = from.distanceTo(destList.get(dest));
       		if (travelTime < minTime) {
    			minTime = travelTime;
    			minDist = distance;
    			minIdx  = dest;
        	}
       		// Check for min distance for older non TopSpeed units
       		else if (travelTime == minTime && distance < minDist) {
    			minDist = distance;
    			minIdx  = dest;	       			
       		}
    	}
		StarSystem destSys = destList.get(minIdx);
    	if (action == SET_RALLY)
    	   	rallySystem(destSys);
    	else if (action == PREVIEW_RALLY)
    		from.rallySprite().previewDestRally(destSys);
    	return destSys;
    }
    public void rallySys(StarSystem sys, int action) { // BR: used by chain rally
    	if (canRallyTo(sys)) {
    		switch (action) {
        	case SET_RALLY:
        		rallySystem(sys);
        		return;
        	case PREVIEW_RALLY:
        		system().rallySprite().previewDestRally(sys);
        		return;
        	case CLEAR_PREVIEW:
        		system().rallySprite().previewDestRally(null);
        		return;
        	default:
        		System.err.println("Wrong Rally action !!!");
        	}
    	}
    }
    public void rallySystem(StarSystem sys)  {
        if (canRallyTo(sys)) {
            relocationSystem = (sys == system()) ? null : sys;
            system().rallySprite().clear();
        }
    }
    public boolean forwardRallies()          { return forwardRallies; }
    public void toggleForwardRallies()       { forwardRallies = !forwardRallies; }
    public void setForwardRallies(boolean b) { forwardRallies = b; }
    public void stopRally() { 
        relocationSystem = null;
        system().rallySprite().stop(); 
    }
    public void goExtinct() {
        vEmpire = null;
        vBases = 0;
        vShieldLevel = 0;
        vFactories = 0;
        vPopulation = 0;
        vStargate = false;
        clearHostility();
    }
    public int flagColorId()  { return flagColor; }
    private void setVestigesFlagColor(Planet planet, int id) {
    	int color = flagRuinsNoneColor.getIndex();
    	if (planet.isAntaran())
    		color = flagRuinsAntaranColor.getIndex();
    	else if (planet.isOrionArtifact())
    		color = flagRuinsOrionColor.getIndex();
		setFlagColor(color, id);
    }
    private void setResourceFlagColor(Planet planet, int id) {
    	int color = 0;
    	if (planet.isResourceNormal())
    		color = flagAssetNormalColor.getIndex();
    	if (planet.type().key() == NONE) // Asteroid won't be shown as Normal
    		color = flagNoneColor.getIndex();

    	// For backward compatibility.
    	if (planet.isAntaran())
    		color = flagAntaranColor.getIndex();
    	else if (planet.isOrionArtifact())
    		color = flagOrionColor.getIndex();

    	if (planet.isResourceUltraPoor())
    		color = flagUltraPoorColor.getIndex();
    	else if (planet.isResourcePoor())
    		color = flagPoorColor.getIndex();
    	else if (planet.isResourceRich())
    		color = flagRichColor.getIndex();
    	else if (planet.isResourceUltraRich())
    		color = flagUltraRichColor.getIndex();
		setFlagColor(color, id);
    }
    private void setEnvFlagColor(Planet planet, int id) {
    	int color = 0;
    	if (planet.isEnvironmentNormal())
    		color = flagEnvNormalColor.getIndex();
    	if (planet.type().key() == NONE)
    		color = flagEnvNoneColor.getIndex();
    	else if (planet.isEnvironmentNone())
    		color = flagEnvNoneColor.getIndex();
    	else if (planet.isEnvironmentHostile())
    		color = flagEnvHostileColor.getIndex();
    	else if (planet.isEnvironmentFertile())
    		color = flagEnvFertileColor.getIndex();
    	else if (planet.isEnvironmentGaia())
    		color = flagEnvGaiaColor.getIndex();
		setFlagColor(color, id);
    }
    private void setTypeFlagColor(Planet planet, int id) {
    	int color;
		switch (planet.type().key()) {
	    case NONE:
	    	color = flagAsteroidColor.getIndex();
	    	break;
	    case RADIATED:
	    	color = flagRadiatedColor.getIndex();
	    	break;
	    case TOXIC:
	    	color = flagToxicColor.getIndex();
	    	break;
	    case INFERNO:
	    	color = flagInfernoColor.getIndex();
	    	break;
	    case DEAD :
	    	color = flagDeadColor.getIndex();
	    	break;
	    case TUNDRA:
	    	color = flagTundraColor.getIndex();
	    	break;
	    case BARREN:
	    	color = flagBarrenColor.getIndex();
	    	break;
	    case MINIMAL:
	    	color = flagMinimalColor.getIndex();
	    	break;
	    case DESERT:
	    	color = flagDesertColor.getIndex();
	    	break;
	    case STEPPE:
	    	color = flagSteppeColor.getIndex();
	    	break;
	    case ARID:
	    	color = flagAridColor.getIndex();
	    	break;
	    case OCEAN:
	    	color = flagOceanColor.getIndex();
	    	break;
	    case JUNGLE:
	    	color = flagJungleColor.getIndex();
	    	break;
	    case TERRAN:	
		default:
			color = flagTerranColor.getIndex();
			break;
		}
		setFlagColor(color, id);
    }
    private void setTechFlagColor(Planet planet, int id) {
    	int color;
		switch (planet.type().key()) {
	    case NONE:
	    	color = flagTechNoneColor.getIndex();
	    	break;
	    case RADIATED:
	    	color = flagTechRadiatedColor.getIndex();
	    	break;
	    case TOXIC:
	    case INFERNO:
	    	color = flagTechToxicColor.getIndex();
	    	break;
	    case DEAD :
	    	color = flagTechDeadColor.getIndex();
	    	break;
	    case TUNDRA:
	    	color = flagTechTundraColor.getIndex();
	    	break;
	    case BARREN:
	    	color = flagTechBarrenColor.getIndex();
	    	break;
	    case MINIMAL:
	    case DESERT:
	    case STEPPE:
	    case ARID:
	    	color = flagTechStandardColor.getIndex();
	    	break;
	    case OCEAN:
	    case JUNGLE:
	    case TERRAN:	
		default:
			color = flagTechGoodColor.getIndex();
			break;
		}
		if (planet.isEnvironmentFertile())
	    	color = flagTechFertileColor.getIndex();
	    else if (planet.isEnvironmentGaia())
	    	color = flagTechGaiaColor.getIndex();
		setFlagColor(color, id);
    }
    private void setNebulaFlagColor(Planet planet, int id) {
    	int color;
    	if (planet.starSystem().inNebula())
    		color = flagInNebulaColor.getIndex();
    	else
    		color = flagNotNebulaColor.getIndex();
		setFlagColor(color, id);
    }
    
    private void clearFlagColor(Planet planet, int id) { setFlagColor(0, id); }
    private void setFlagColor(int color, int id)	{ // BR: flagColorCount
    	flagColor = getMixedColor(flagColor, color, id);
    }
    int getFlagColor(int id)				{ return getBaseColor(flagColor, id); }
    public	String getFlagColorName(int id)	{ return getColorName(getFlagColor(id)); }
    private	String getColorName(int id)		{ return flagImageNameList.get(id).replace("Flag_", "");  }
    
    public	boolean hasFlag(int id)			{ return getFlagColor(id) != FLAG_NONE; }
    public	boolean hasFlagColor(int id, int color) { return getFlagColor(id) == color; }
    private int getMixedColor(int mixedColor, int color, int id) { // BR: flagColorCount
    	return getFilteredColor(mixedColor, id) | getShiftedColor(color, id);
    }
    
    private int getFilteredColor(int mixedColor, int id) { // BR: flagColorCount
    	int idx = min(4, max(1, id))-1;
    	return (mixedColor & COLOR_IMASK[idx]);
    }
    private int getBaseColor(int mixedColor, int id) { // BR: flagColorCount
    	int idx = min(4, max(1, id))-1;
    	return (mixedColor & COLOR_MASK[idx]) / COLOR_SHIFT[idx];
    }
    private int getShiftedColor(int baseColor, int id) { // BR: flagColorCount
    	int idx = min(4, max(1, id))-1;
    	return (baseColor & COLOR_MASK[0]) * COLOR_SHIFT[idx];
    }
    private static BufferedImage bufferedImage(Image left, Image right) { // BR: flagColorCount
    	BufferedImage result = new BufferedImage(
                SIDE, SIDE, BufferedImage.TYPE_INT_ARGB);
    	Graphics g = result.getGraphics();
    	g.drawImage(right, MID2, 0, SIDE, SIDE, MAST, 0, MAST-FLAG_W, SIDE, null);
    	g.drawImage(left,  0,    0, MID1, SIDE, MAST-FLAG_W, 0, MAST, SIDE, null);
    	g.dispose();
    	return result;
    }
    private static Image joinImage(Image bL, Image bR, Image tR, Image tL) { // BR: flagColorCount
    	BufferedImage result = bufferedImage(bL, bR);
    	Graphics g = result.getGraphics();
		g.drawImage(joinImage(tL, tR), LF_LEFT, LF_TOP,  LF_RIGHT, LF_BOTTOM, 0, 0, SIDE, SIDE-LF_CUT, null);
		g.dispose();
    	return new ImageIcon(result).getImage();
    }
    private static Image joinImage(Image bL, Image bR, Image tR) { // BR: flagColorCount
    	return joinImage(bL, bR, tR, null);    	
    }
    private static Image joinImage(Image left, Image right) { // BR: flagColorCount
    	return new ImageIcon(bufferedImage(left, right)).getImage();
    }
    public Image flagBackGround(String name) { // BR: flagColorCount
    	Image fh = ImageManager.current().image(name);
    	switch (options().selectedFlagColorCount()) {
    	case 2:
    		return joinImage(fh, fh);
    	case 3:
    		return joinImage(fh, fh, fh);
    	case 4:
    		return joinImage(fh, fh, fh, fh);
    	default:
    		return fh;
    	}
    }
    public Image flagImage() { // BR: flagColorCount
    	// Don't show more than one black flag!
    	Image flag1;
    	boolean haveFlag = false;
    	for (int i=1; i<=options().selectedFlagColorCount(); i++)
    		haveFlag |= getFlagColor(i)>0;
    	if (haveFlag)
    		flag1 = flagImage(getFlagColor(1));
    	else
    		flag1 = image(flagImageNameList.get(FLAG_NONE));

    	switch (options().selectedFlagColorCount()) {
    	case 2:
    		return joinImage(flag1, flagImage(getFlagColor(2)));
    	case 3:
    		return joinImage(flag1, flagImage(getFlagColor(2)),
    				flagImage(getFlagColor(3)));
    	case 4:
    		return joinImage(flag1, flagImage(getFlagColor(2)),
    				flagImage(getFlagColor(3)), flagImage(getFlagColor(4)));
    	default:
    		return flag1;
    	}
    }
    public Image mapFlagImage() { // BR: flagColorCount
    	switch (options().selectedFlagColorCount()) {
    	case 2:
    		return joinImage(mapFlagImage(getFlagColor(1)), mapFlagImage(getFlagColor(2)));
    	case 3:
    		return joinImage(mapFlagImage(getFlagColor(1)), mapFlagImage(getFlagColor(2)),
    				mapFlagImage(getFlagColor(3)));
    	case 4:
    		return joinImage(mapFlagImage(getFlagColor(1)), mapFlagImage(getFlagColor(2)),
    				mapFlagImage(getFlagColor(3)), mapFlagImage(getFlagColor(4)));
    	default:
    		return mapFlagImage(getFlagColor(1));
    	}
    }
    private Image flagImage(int flagColor) { // BR: flagColorCount
    	if (flagColor <= 0 || flagColor >= flagImageNameList.size())
    		return null;
    	return image(flagImageNameList.get(flagColor));
    }
    private Image mapFlagImage(int flagColor) { // BR: flagColorCount
    	if (flagColor <= 0 || flagColor >= mapFlagImageNameList.size())
    		return null;
    	return image(mapFlagImageNameList.get(flagColor));
    }
    // BR: Fix for "Precursor Relic" event; Now every one see the same thing!
    public PlanetType planetType() {
    	if (scouted())
    		return system().planet().type();
    	else
    		return planetType_(); // BR: former call
    }
    // BR: Fix for "Precursor Relic" event; Now everyone sees the same thing!
    private PlanetType planetType_() {
        if (vPlanetTypeKey == null)
            return null;
        if (vPlanetType == null)
            vPlanetType = PlanetType.keyed(vPlanetTypeKey);
        return vPlanetType;
    }
    public void clearFleetPlan() {
        if (fleetPlan != null)
            fleetPlan.clear();
    }
    public boolean hasFleetPlan() {
        return (fleetPlan != null) 
           && (fleetPlan.needsShips() || fleetPlan.isRetreating());
    }
    public FleetPlan fleetPlan()  {
        if (fleetPlan == null)
            fleetPlan = new FleetPlan(ownerId, sysId);
        return fleetPlan;
    }
    public void raiseHostility()                   { hostilityLevel++; }
    public void resetSystemData()                  { setLocationSecurity(); }
    public void refreshSystemEntryScan() {
        vGuarded = system().hasMonster();      
        if (vGuarded)
            setName();
    }
    private void autoFlagAssignation(Planet p, String assignation, int id) {
    	switch (assignation) {
	    	case AUTO_FLAG_TYPE:
	    		setTypeFlagColor(p, id);
	    		return;
	    	case AUTO_FLAG_ENV:
	    		setEnvFlagColor(p, id);
	    		return;
	    	case AUTO_FLAG_ASSET:
	    		setResourceFlagColor(p, id);
	    		return;
	    	case AUTO_FLAG_RUINS:
	    		setVestigesFlagColor(p, id);
	    		return;
	    	case AUTO_FLAG_TECH:
	    		setTechFlagColor(p, id);
	    		return;
	    	case AUTO_FLAG_NEBULA:
	    		setNebulaFlagColor(p, id);
	    		return;
	    	case AUTO_FLAG_CLEAR:
	    		clearFlagColor(p, id);
	    		return;
	    	case AUTO_FLAG_NOT:
			default:
				return;
    	}
    }
    public void forceAutoFlagColor() {
    	if (scouted()) 
    		autoFlagPlanet(system().planet());
    }
    private void autoFlagPlanet(Planet p) {
    	autoFlagAssignation(p, options().selectedAutoFlagAssignation1(), 1);
    	autoFlagAssignation(p, options().selectedAutoFlagAssignation2(), 2);    	
    	autoFlagAssignation(p, options().selectedAutoFlagAssignation3(), 3);    	
    	autoFlagAssignation(p, options().selectedAutoFlagAssignation4(), 4);    	
    }
    public void refreshFullScan() {
        if (!scouted()) {
            log("Orbital scan scouts new system: ", system().name());
            owner().shareSystemInfoWithAllies(this);
            if (owner().isPlayer())
            	autoFlagPlanet(system().planet());
            if (owner().isPlayerControlled()) {
                session().addSystemScouted(system());
                if (system().empire() != player())
                    system().addEvent(new SystemScoutedEvent(player().id));
            }
        }
        scoutTime = galaxy().currentYear();
        spyTime = galaxy().currentYear();
        setName();
        setEmpire();
        setPlanetData();
        setColonyData();
        setOrbitingFleets();
        refreshSystemEntryScan();
                
        if (system().hasBonusTechs())
            owner().plunderAncientTech(system());
    }
    public void refreshAllySharingScan() {
	    if (!scouted() && owner().isPlayer())
	        	autoFlagPlanet(system().planet());
        if (owner().isPlayerControlled() && !scouted()) {
            log("Ally shares new system data: ", system().name());
            session().addSystemScoutedByAllies(system());
        }

        scoutTime = galaxy().currentYear();
        setName();
        setEmpire();
        setPlanetData();
    }
    public void refreshLongRangePlanetScan() {
        if (!scouted()) {
            log("Long range planet scan scouts new system: ", system().name());
            owner().shareSystemInfoWithAllies(this);
            if (owner().isPlayer())
            	autoFlagPlanet(system().planet());
            if (owner().isPlayerControlled())
                session().addSystemScoutedByAstronomers(system());
        }

        scoutTime = galaxy().currentYear();
        setName();
        setEmpire();
        setPlanetData();
        setOrbitingFleets();
    }
    public void refreshLongRangeShipScan() {
        if (!scouted()) {
            log("Long range ship scan scouts new system: ", system().name());
            owner().shareSystemInfoWithAllies(this);
            if (owner().isPlayer())
            	autoFlagPlanet(system().planet());
            if (owner().isPlayer())
                session().addSystemScouted(system());
        }

        scoutTime = galaxy().currentYear();
        setName();
        setEmpire();
        setPlanetData();
        setOrbitingFleets();
    }
    public void refreshSpyScan() {
        setName();
        setEmpire();
        setColonyData();
        spyTime = galaxy().currentYear();
    }
    public void setEmpire() {
        // if the empire has changed, reset the spy time
        Empire prevEmpire = vEmpire;
        vEmpire = system().empire();
        if (vEmpire != prevEmpire)
            spyTime = 0;
        
        if (!owner().aggressiveWith(id(vEmpire)))
            clearHostility();
    }
    private void setPlanetData() {
        vArtifacts = system().planet().artifacts();
        vPlanet = system().planet();
        // BR: Fix for "Precursor Relic" event
        // vPlanetTypeKey = system().planet().type().key();
        if (vPlanetTypeKey != system().planet().type().key()) {
        	vPlanetTypeKey = system().planet().type().key();
        	// BR: Force actualization
        	vPlanetType = null;
        	planetType_();
        }
        // May be: Update flags 
        if (owner().isPlayer())
        	autoFlagPlanet(system().planet());
        vCurrentSize = (int) system().planet().currentSize();
    }
    private void setColonyData() {
        Colony col = system().colony();
        vStargate = system().isColonized() && col.hasStargate();
        vBases = 0;
        vFactories = 0;
        vPopulation = 0;
        vShieldLevel = 0;
        if (isColonized()) {
            float actualPop = col.population();
            vBases = col.defense().missileBases();
            vFactories = (int) col.industry().factories();
            vPopulation = actualPop < 1 ? (int) Math.ceil(actualPop) : (int) actualPop;
            vShieldLevel = col.defense().shieldLevel();
            vCurrentSize = (int) system().planet().currentSize();
        }
    }
    private void setOrbitingFleets() {
        orbitingFleets().clear();
        exitingFleets().clear();
        vGuarded = system().hasMonster();
        vOrbitingFleets.clear();
        vOrbitingFleets.addAll(system().orbitingFleets());
        vExitingFleets.clear();
        vExitingFleets.addAll(system().exitingFleets());
    }
    public void clearFleetInfo() {
        orbitingFleets().clear();
        exitingFleets().clear();
    }

    public float distance()                  { return owner().sv.distance(system().id); }
    public boolean hasRallyPoint()           { return rallySystem() != null; }
    public Colony colony()                   { return system() == null ? null : system().colony(); }
    public boolean abandoned()               { return system() == null ? false : system().abandoned(); }
    public Integer deltaPopulation()         { return isColonized() ? colony().deltaPopulation() : 0; }
    public Integer deltaFactories()          { return isColonized() ? colony().industry().deltaFactories() : 0; }
    public Integer deltaBases()              { return isColonized() ? colony().defense().deltaBases() : 0; }
    public BufferedImage planetTerrain()     { return planetType() == null ? null : planetType().terrainImage(); }

    public boolean resourceUltraRich()       { return (planet() != null) && planet().isResourceUltraRich(); }
    public boolean resourceRich()            { return (planet() != null) && planet().isResourceRich(); }
    public boolean resourceNormal()          { return (planet() != null) && planet().isResourceNormal(); }
    public boolean resourcePoor()            { return (planet() != null) && planet().isResourcePoor(); }
    public boolean resourceUltraPoor()       { return (planet() != null) && planet().isResourceUltraPoor(); }
    public boolean artifact()                { return (planet() != null) && planet().isArtifact(); }
    public boolean orionArtifact()           { return (planet() != null) && planet().isOrionArtifact(); }
    
    public boolean environmentHostile()      { return (planet() != null) && planet().isEnvironmentHostile(); }
    public boolean environmentFertile()      { return (planet() != null) && planet().isEnvironmentFertile(); }
    public boolean environmentGaia()         { return (planet() != null) && planet().isEnvironmentGaia(); }

    public void resetFlagColor()			 {
		if(isShiftOrCtrlDown() && scouted())
    		autoFlagPlanet(vPlanet);
    	else
    		flagColor = FLAG_NONE;
    }
    private int getFlagId()					 {
    	if(isCtrlDown()) // Left - Right
        	if(isShiftDown()) // Top - Bottom
        		return 3;
        	else
        		return 2;
    	else
        	if(isShiftDown()) // Top - Bottom
        		return 4;
        	else
        		return 1;
    }
    public int toggleFlagColor(boolean reverse) { // BR: flagColorCount
    	int id = getFlagId();
    	int oldFlagColor = getFlagColor(id);
    	int newFlagColor = toggleFlagColor(reverse, oldFlagColor);
    	setFlagColor(newFlagColor, id);
    	return newFlagColor;
    }
    public void setFlagColor(int newFlagColor)	{  setFlagColor(newFlagColor, getFlagId()); }
    private int toggleFlagColor(boolean reverse, int flagColor) { // BR: flagColorCount
		if (reverse) {
			flagColor--;
			if (flagColor < 0)
				return flagCount-1;
			return flagColor;
		} else {
			flagColor++;
			if (flagColor >= flagCount)
				return 0;
			return flagColor;
		}
    }
    public String resourceType()			{
        if (artifact() || orionArtifact())
            return "PLANET_ARTIFACTS";
        if (resourceUltraPoor())
            return "PLANET_ULTRA_POOR";
        else if (resourcePoor())
            return "PLANET_POOR";
        else if (resourceUltraRich())
            return "PLANET_ULTRA_RICH";
        else if (resourceRich())
            return "PLANET_RICH";
        else
            return "";
    }
    public String ecologyType()				{
        if (environmentHostile())
            return "PLANET_HOSTILE";
        else if (environmentFertile())
            return "PLANET_FERTILE";
        if (environmentGaia())
            return "PLANET_GAIA";
        else
            return "";
    }
    public boolean canSabotageBases()        { return bases() > 0; }
    public boolean canSabotageFactories()    { return factories() > 0; }
    public boolean canInciteRebellion()      { 
        if (!isColonized())
            return false;
        if (colony().inRebellion())
            return false;
        if (colony().isCapital())
            return false;
        // we cannot incite a rebellion in an enemy system that the enemy cannot
        // currently colonize (to send transports to stop rebellion). This can
        // occur if, for example, the system is degraded with a random event
        if (!empire().canColonize(system()))
            return false;
        // special case: we cannot incite rebellion against a final war enemy 
        // if we are rebelling against the New Republic
        Empire leader = galaxy().council().leader();
        if (leader != null) {
            if (owner().viewForEmpire(vEmpire).embassy().finalWar()) {                   
                if ((vEmpire == leader) || vEmpire.viewForEmpire(leader).embassy().unity())
                    return false;
            }
        }
        return true;
    }

    public boolean scouted()                 { return (owner() == empire()) || (scoutTime() > 0); }
    public boolean spied()                   { return (owner() == empire()) || (spyTime() > 0); }
    public int lastReportYear()              { return (owner() == empire()) ? galaxy().currentYear() : (int) spyTime(); }
    public int lastReportTurn()              { return (int) max(spyTurn(), scoutTurn()); }
    public int spyReportAge()                { return galaxy().currentYear() - lastReportYear(); }

    public boolean canRallyTo(StarSystem sys) { return sys.empire() == owner(); }

    public float distanceTo(SystemView v)	 { return system().distanceTo(v.system()); }

    public int desiredMissileBases() {
        return (empire() == owner()) ? colony().defense().maxBases() : 0;
    }
    public boolean innerSystem()             { return locationSecurity() == INNER_SYSTEM; }
    public boolean supportSystem()           { return false; }
    public boolean borderSystem()            { return locationSecurity() == BORDER_SYSTEM; }
    public boolean attackTarget()            { return locationSecurity() == ATTACK_TARGET; }

    public boolean isColonized()             { return (empire() != null) && (colony() != null); }
    public boolean isInEmpire()              { return owner() == system().empire();}
    
    public boolean isAlert() {
        if (vName.isEmpty())
            return false;
        if (scouted() && system().hasEvent())
            return true;
        if(system().empire() == player())
        {
            for (ShipFleet fl: orbitingFleets()) {
                if (fl.isPotentiallyArmed(player())) {
                    if (player().atWarWith(fl.empId())) { 
                        return true;
                    }  
                }
            }
        }
        return (scouted() && isColonized() && colony().inRebellion());
    }
    public String descriptiveName() {
        if (!isColonized()) {
            if (!scouted()) 
                return text("MAIN_UNSCOUTED");
            else if (system().planet().isEnvironmentNone())
                return text("MAIN_NO_PLANETS");
            else if (abandoned())
                return text("MAIN_ABANDONED");
            else
                return text("MAIN_NO_COLONIES");
        }
        String name;
        if (!scouted())
            name = text("PLANET_WORLD",empire().raceName());
        else if (empire().isHomeworld(system()))
            name = text("PLANET_HOMEWORLD",empire().raceName());
        else if (empire().isColony(system()))
            name = text("PLANET_COLONY",empire().raceName());
        else
            name = text("PLANET_WORLD",empire().raceName());
        
        name = empire().replaceTokens(name, "alien");
        return name;
    }

    @Override
    public String toString()       { return concat("View: ", name()); }
    @Override
    public float x()               	  			{ return system().x();  }
    @Override
    public float y()             				  { return system().y();  }

    public boolean hasActiveTransport()        { return isColonized() && colony().transport().isActive(); }
    public boolean hasFleetForCiv (Empire c) {
        return system().hasFleetForEmpire(c);
    }
    public boolean inRange(float range) {
        if (distance() <= range)
            return true;
        for (Empire ally: owner().allies()) {
            if (ally.sv.withinRange(system().id, range))
                return true;
        }
        return false;
    }
    public boolean inShipRange()  { return inRange(owner().tech().shipRange()); }
    public int maxPopToGive(float targetPopPct) {
        if (!colony().canTransport())
            return 0;

        int p1 = colony().maxTransportsAllowed();
        int p2 = (int) (colony().population() - (targetPopPct * system().planet().currentSize()));
        return Math.min(p1,p2);
    }
    public float defenderCombatAdj() {
        if (empire() == owner())
            return colony().defenderCombatAdj();

        EmpireView cv = owner().viewForEmpire(empire());

        if (cv == null)
            return 0;

        return cv.spies().tech().troopCombatAdj(true);
    }
    public int rallyTurnsTo(StarSystem dest) {
            return (int) Math.ceil(system().rallyTimeTo(dest));
    }
    public int maxTransportsToReceive() {
        if (isColonized()) {
            if (owner() == empire())
                return currentSize() - (int) colony().workingPopulation();
            else
                return currentSize();
        }
        return 0;
    }
    public int maxTransportsToSend() {
        return population() <= 1 ? 0 : population() / 2;
    }
    private SystemView(int sId, int empId) {
        ownerId = empId;
        sysId = sId;
        if (player().id == ownerId)
        	setForwardRallies(options().defaultForwardRally());
    }
    private void setLocationSecurity() {
        if (distance() > owner().scoutRange()) {
            locationSecurity = UNIMPORTANT;
            return;
        }
        locationSecurity = INNER_SYSTEM;
        float dangerRange = owner().shipRange() + 1;
        for (int i=0; i< system().numNearbySystems();i++) {
            StarSystem sys = system().nearbySystem(i);
            if (distanceTo(sys) > dangerRange)
                break;
            int empId = owner().sv.empId(sys.id);
            if (empId != Empire.NULL_ID) {
                if (owner().atWarWith(empId)) {
                    locationSecurity = ATTACK_TARGET;
                    break;
                }
                if (!owner().friendlyWith(empId))
                    locationSecurity = BORDER_SYSTEM;
            }
        }
    }
    private void setName() {
        if (!system().unnamed())
            vName = system().name();
        else if (vName.isEmpty()) {  // BR: for custom Races
//        	vName = owner().dataRace().worldsPrefix
//        			+ owner().race().randomSystemName(owner())
//        			+ owner().dataRace().worldsSuffix;
        	vName = owner().randomSystemName(owner());
        	// BR: Update the system name too, to avoid multiple naming
        }
    }
    private void clearHostility()                   { hostilityLevel = 0; }
}
