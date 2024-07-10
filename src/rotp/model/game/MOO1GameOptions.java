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
package rotp.model.game;

import java.awt.Color;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import rotp.Rotp;
import rotp.model.empires.Empire;
import rotp.model.empires.Race;
import rotp.model.events.RandomEvent;
import rotp.model.galaxy.GalaxyBitmapShape;
import rotp.model.galaxy.GalaxyBullseyeShape; // modnar, custom shape, long generation times
import rotp.model.galaxy.GalaxyClusterShape; // modnar, custom shape
import rotp.model.galaxy.GalaxyEllipticalShape;
import rotp.model.galaxy.GalaxyFractalShape; // modnar, custom shape, long generation times
import rotp.model.galaxy.GalaxyGridShape; // modnar, custom shape
import rotp.model.galaxy.GalaxyLorenzShape; // modnar, custom shape, long generation times
import rotp.model.galaxy.GalaxyMazeShape; // modnar, custom shape
import rotp.model.galaxy.GalaxyRectangularShape;
import rotp.model.galaxy.GalaxyShape;
import rotp.model.galaxy.GalaxyShurikenShape; // modnar, custom shape, long generation times
import rotp.model.galaxy.GalaxySpiralArmsShape; // modnar, custom shape
import rotp.model.galaxy.GalaxySpiralShape;
import rotp.model.galaxy.GalaxySwirlClustersShape; // modnar, custom shape
// modnar: add new map shapes
import rotp.model.galaxy.GalaxyTextShape; // modnar, custom shape
import rotp.model.galaxy.StarSystem;
import rotp.model.galaxy.StarType;
import rotp.model.planet.Planet;
import rotp.model.planet.PlanetType;
import rotp.model.tech.TechEngineWarp;
import rotp.ui.RotPUI;
import rotp.ui.UserPreferences;
import rotp.ui.game.SetupGalaxyUI;
import rotp.ui.util.IParam;
import rotp.ui.util.SpecificCROption;
import rotp.util.Base;
import rotp.util.Rand;

//public class MOO1GameOptions implements Base, IGameOptions, DynamicOptions, Serializable {
public class MOO1GameOptions implements Base, IGameOptions, Serializable {
	
    private static final long serialVersionUID = 1L;
    private static final float BASE_RESEARCH_MOD = 30f;
    private static final boolean beepsOnError = false;
    private final String[] opponentRaces = new String[maxOpponents()];
    private final List<Integer> colors = new ArrayList<>();
    private final List<Color> empireColors = new ArrayList<>();

    // Race UI
    private final NewPlayer player = new NewPlayer();

    // GalaxyUI
    private String selectedGalaxySize;
    private String selectedGalaxyShape;
    private String selectedGalaxyShapeOption1;
    private String selectedGalaxyShapeOption2;
    private String selectedGameDifficulty;
    private int selectedNumberOpponents;
    private String selectedStarDensityOption;
    private String selectedOpponentAIOption;
    private final String[] specificOpponentAIOption = new String[maxOpponents()+1];
    private String[] specificOpponentCROption = new String[maxOpponents()+1];

    @SuppressWarnings("unused")
	private boolean communityAI = false;  // unused
    @SuppressWarnings("unused")
	private boolean disableColonizePrompt = false; // unused
   
    // Advanced Options UI
    private String selectedGalaxyAge;
    private String selectedResearchRate;
    private String selectedTechTradeOption;
    private String selectedRandomEventOption;
    private String selectedWarpSpeedOption;
    private String selectedNebulaeOption;
    private String selectedCouncilWinOption;
    private String selectedPlanetQualityOption;
    private String selectedTerraformingOption;
    private String selectedFuelRangeOption;
    private String selectedRandomizeAIOption;
    private String selectedAIHostilityOption;
    private String selectedColonizingOption;
    private String selectedAutoplayOption;
    // BR: Dynamic options
    private DynOptions dynamicOptions = new DynOptions();

    private transient GalaxyShape galaxyShape;
    private transient int id = UNKNOWN_ID;

    public MOO1GameOptions() { init(); }
    public MOO1GameOptions(boolean init) { if(init) init(); }
    private void init() {
        // initOpponentRaces();
        randomizeColors();
        setBaseSettingsToDefault();
    }
    public void validateOnLoad() {
   		if (dynamicOptions == null) // compatibility tentative... probably not enough!
   			dynamicOptions = new DynOptions();

    }
	@Override public IGameOptions opts()		 { return this;	}
	@Override public DynOptions dynOpts()		 { return dynamicOptions; }
    @Override public int id()                    { return id; }
    @Override public void id(int id)             { this.id = id; }
    @Override
    public int numPlayers()                      { return 1; }
    @Override
    public int numColors()                       { return 16; } // modnar: added new colors, but this value should stay == numRaces
    @Override
    public NewPlayer selectedPlayer()            { return player; }
/*
    @Override
    public boolean communityAI()                 { return communityAI; }
    @Override
    public void communityAI(boolean b)           { communityAI = b; }
    */
    @Override
    public String selectedGalaxySize()           { return selectedGalaxySize; }
    @Override
    public void selectedGalaxySize(String s)     {
        int prevNumOpp = defaultOpponentsOptions();
        selectedGalaxySize = s; 
        if (selectedNumberOpponents() == prevNumOpp)
            selectedNumberOpponents(defaultOpponentsOptions());
    }
    @Override
    public String selectedGalaxyShape()          { return selectedGalaxyShape; }
    @Override
    public void selectedGalaxyShape(String s)    { selectedGalaxyShape = s; setGalaxyShape(); }
    @Override
    public String selectedGalaxyShapeOption1()       { return selectedGalaxyShapeOption1; }
    @Override
    public void selectedGalaxyShapeOption1(String s) { selectedGalaxyShapeOption1 = s; }
    @Override
    public String selectedGalaxyShapeOption2()       { return selectedGalaxyShapeOption2; }
    @Override
    public void selectedGalaxyShapeOption2(String s) { selectedGalaxyShapeOption2 = s; }
    @Override
    public String selectedGalaxyAge()           { return selectedGalaxyAge; }
    @Override
    public void selectedGalaxyAge(String s)     { selectedGalaxyAge = s; }
    @Override
    public String selectedGameDifficulty()       { return selectedGameDifficulty; }
    @Override
    public void selectedGameDifficulty(String s) { selectedGameDifficulty = s; }
    @Override
    public String selectedResearchRate()         { return selectedResearchRate == null ? RESEARCH_NORMAL : selectedResearchRate; }
    @Override
    public void selectedResearchRate(String s)   { selectedResearchRate = s; }
    @Override
    public String selectedTechTradeOption()         { return selectedTechTradeOption == null ? TECH_TRADING_YES : selectedTechTradeOption; }
    @Override
    public void selectedTechTradeOption(String s)   { selectedTechTradeOption = s; }
    @Override
    public String selectedRandomEventOption()       { return selectedRandomEventOption == null ? RANDOM_EVENTS_NO_MONSTERS : selectedRandomEventOption; }
    @Override
    public void selectedRandomEventOption(String s) { selectedRandomEventOption = s; }
    @Override
    public String selectedWarpSpeedOption()         { return selectedWarpSpeedOption == null ? WARP_SPEED_NORMAL : selectedWarpSpeedOption; }
    @Override
    public void selectedWarpSpeedOption(String s)   { selectedWarpSpeedOption = s; }
    @Override
    public String selectedNebulaeOption()           { return selectedNebulaeOption == null ? NEBULAE_NORMAL : selectedNebulaeOption; }
    @Override
    public void selectedNebulaeOption(String s)     { selectedNebulaeOption = s; }
    @Override
    public String selectedCouncilWinOption()        { return selectedCouncilWinOption == null ? COUNCIL_REBELS : selectedCouncilWinOption; }
    @Override
    public void selectedCouncilWinOption(String s)  { selectedCouncilWinOption = s; }
    @Override
    public String selectedStarDensityOption()       { return selectedStarDensityOption == null ? STAR_DENSITY_NORMAL : selectedStarDensityOption; }
    @Override
    public void selectedStarDensityOption(String s) { selectedStarDensityOption = s; }
    @Override
    public String selectedPlanetQualityOption()       { return selectedPlanetQualityOption == null ? PLANET_QUALITY_NORMAL : selectedPlanetQualityOption; }
    @Override
    public void selectedPlanetQualityOption(String s) { selectedPlanetQualityOption = s; }
    @Override
    public String selectedTerraformingOption()       { return selectedTerraformingOption == null ? TERRAFORMING_NORMAL : selectedTerraformingOption; }
    @Override
    public void selectedTerraformingOption(String s) { selectedTerraformingOption = s; }
    @Override
    public String selectedColonizingOption()       { return selectedColonizingOption == null ? COLONIZING_NORMAL : selectedColonizingOption; }
    @Override
    public void selectedColonizingOption(String s) { selectedColonizingOption = s; }
    @Override
    public String selectedFuelRangeOption()       { return selectedFuelRangeOption == null ? FUEL_RANGE_NORMAL : selectedFuelRangeOption; }
    @Override
    public void selectedFuelRangeOption(String s) { selectedFuelRangeOption = s; }
    @Override
    public String selectedRandomizeAIOption()       { return selectedRandomizeAIOption == null ? RANDOMIZE_AI_NONE : selectedRandomizeAIOption; }
    @Override
    public void selectedRandomizeAIOption(String s) { selectedRandomizeAIOption = s; }
    @Override
    public String selectedAutoplayOption()          { return selectedAutoplayOption == null ? AUTOPLAY_OFF : selectedAutoplayOption; }
    @Override
    public void selectedAutoplayOption(String s)    { selectedAutoplayOption = s; }
    @Override
    public String selectedOpponentAIOption()       { 
    	return selectedOpponentAIOption == null ? defaultAI.aliensKey : selectedOpponentAIOption; } // modnar: default to modnar AI
    @Override
    public void selectedOpponentAIOption(String s) { selectedOpponentAIOption = s; }
    @Override
    public String specificOpponentAIOption(int n)  { 
            if ((specificOpponentAIOption == null) || (specificOpponentAIOption.length < n))
                return selectedOpponentAIOption();
            else
                return specificOpponentAIOption[n];
    }
    @Override
    public void specificOpponentAIOption(String s, int n) { 
        if (n < specificOpponentAIOption.length)
            specificOpponentAIOption[n] = s;
    }
    @Override 
    public String specificOpponentCROption(int n)  {
            if ((specificOpponentCROption == null) || (specificOpponentCROption.length < n))
                return selectedUseGlobalCROptions();
            else
                return specificOpponentCROption[n];
    }
    @Override
    public void specificOpponentCROption(String s, int n) { 
        if (n < specificOpponentCROption.length)
            specificOpponentCROption[n] = s;
    }
    @Override
    public String selectedAIHostilityOption()       { return selectedAIHostilityOption == null ? AI_HOSTILITY_NORMAL : selectedAIHostilityOption; }
    @Override
    public void selectedAIHostilityOption(String s) { selectedAIHostilityOption = s; }
    @Override
    public int selectedNumberOpponents()         { return selectedNumberOpponents; }
    @Override
    public void selectedNumberOpponents(int i)   { selectedNumberOpponents = i; }
    @Override
    public String selectedPlayerRace()           { return selectedPlayer().race(); }
    @Override
    // public void selectedPlayerRace(String s)  { selectedPlayer().race = s;  resetSelectedOpponentRaces(); }
    public void selectedPlayerRace(String s)     { // BR: Reset on demand only
    	selectedPlayer().race(s);
    	// Check if MAX_OPPONENT_TYPE reached
        int count = 0;
        for (int i=0; i<opponentRaces.length; i++) {
        	if (s.equals(opponentRaces[i])) {
        		count++;
        		if (count >= MAX_OPPONENT_TYPE)
        			opponentRaces[i] = null;
        	}
        }
    }
    @Override
    public int selectedPlayerColor()             { return selectedPlayer().color(); }
    @Override
    public void selectedPlayerColor(int i)       { selectedPlayer().color(i); }
    @Override
    public String selectedLeaderName()           { return selectedPlayer().leaderName(); }
    @Override
    public void selectedLeaderName(String s)     { selectedPlayer().leaderName(s.trim()); }
    @Override
    public String selectedHomeWorldName()        { return selectedPlayer().homeWorldName(); }
    @Override
    public void selectedHomeWorldName(String s)  { selectedPlayer().homeWorldName(s.trim()); }
    @Override
    public String[] selectedOpponentRaces()      { return opponentRaces; }
    @Override
    public String selectedOpponentRace(int i)    { return (i>=opponentRaces.length && i>0) ? null : opponentRaces[i]; }
    @Override
    public void selectedOpponentRace(int i, String s) {
         if (i < opponentRaces.length && i>=0)
    	   		opponentRaces[i] = s;
    }
    @Override
    public int maximumOpponentsOptions() {
    	// BR: customize min Star per empire
    	int maxEmpires;
        if (selectedGalaxySize.equals(SIZE_DYNAMIC))
        	maxEmpires = (int) ((maximumSystems()-1) / selectedDynStarsPerEmpire());
        else {
        	maxEmpires = min((numberStarSystems()-1) / selectedMinStarsPerEmpire()
        		, colors.size(), MAX_OPPONENT_TYPE * startingRaceOptions().size());
        	maxEmpires = max(0, maxEmpires);
        }
        // \BR:
        int maxOpponents = SetupGalaxyUI.MAX_DISPLAY_OPPS;
       	return min(maxOpponents, maxEmpires-1);
    }
    @Override
    public int defaultOpponentsOptions() {
    	// BR: customize preferred Star per empire
    	int maxEmpires;
        if (selectedGalaxySize.equals(SIZE_DYNAMIC))
        	maxEmpires = (int) ((maximumSystems()-1) / selectedDynStarsPerEmpire());
        else
        	maxEmpires = min((int)Math.ceil((numberStarSystems()-1)/selectedPrefStarsPerEmpire())
        		, colors.size(), MAX_OPPONENT_TYPE*startingRaceOptions().size());
        // \BR:
        int maxOpponents = SetupGalaxyUI.MAX_DISPLAY_OPPS;
        return min(maxOpponents, maxEmpires-1);
    }
    @Override
    public String name() { return "SETUP_RULESET_ORION"; }
    @Override
    public void copyForRestart(IGameOptions oldOpt) { // BR for Restart with new options
        if (!(oldOpt instanceof MOO1GameOptions))
            return;
        MOO1GameOptions opt = (MOO1GameOptions) oldOpt;
        selectedGalaxySize			= opt.selectedGalaxySize;
        selectedGalaxyShape			= opt.selectedGalaxyShape;
        selectedGalaxyShapeOption1	= opt.selectedGalaxyShapeOption1;
        selectedGalaxyShapeOption2	= opt.selectedGalaxyShapeOption2;
        selectedNebulaeOption		= opt.selectedNebulaeOption;
        selectedNumberOpponents		= opt.selectedNumberOpponents;
        
        SafeListParam list = systemsOptionsUI.optionsList;
        for (IParam param : list)
        	param.copyOption(oldOpt, this, true);

        setGalaxyShape(); 
        selectedGalaxyShapeOption1 = opt.selectedGalaxyShapeOption1;
        selectedGalaxyShapeOption2 = opt.selectedGalaxyShapeOption2;
    }
    @Override
    public GalaxyShape galaxyShape()   {
   		if (galaxyShape == null)
        	setGalaxyShape(selectedGalaxyShapeOption1, selectedGalaxyShapeOption2);
            //setGalaxyShape();
        return galaxyShape;
    }
    private void setGalaxyShape(String option1, String option2) {
    	setBaseGalaxyShape();
    	if (option1 == null)
    		selectedGalaxyShapeOption1 = galaxyShape.defaultOption1();
    	else
    		selectedGalaxyShapeOption1 = option1;
    	if (option2 == null)
    		selectedGalaxyShapeOption2 = galaxyShape.defaultOption2();
    	else
    		selectedGalaxyShapeOption2 = option2;
    }
    private void setGalaxyShape() {
    	if(isRandomGalaxy()) {
    		setRandomGalaxyShape();
    		return;
    	}
    	setBaseGalaxyShape();
        selectedGalaxyShapeOption1 = galaxyShape.defaultOption1();
        selectedGalaxyShapeOption2 = galaxyShape.defaultOption2();
    }
    @Override public float densitySizeFactor() {
    	return densitySizeFactor(selectedStarDensityOption());
    }
    @Override public boolean isRandomGalaxy() {
    	return selectedGalaxyShape.equals(SHAPE_RANDOM)
    			|| selectedGalaxyShape.equals(SHAPE_RANDOM_2);
    }
    private void setRandomGalaxyShape() {
		Rand rand = new Rand(selectedGalaxyRandSource());
		int rnd;
		for (int i=0; i<100; i++) {
			rnd = rand.nextInt(12);
			// System.out.println("setRandomGalaxyShape() rand = " + rnd);
		}
		if (selectedGalaxyShape.equals(SHAPE_RANDOM))
			rnd = rand.nextInt(6);
		else
			rnd = rand.nextInt(12);
		// System.out.println("setRandomGalaxyShape() rand = " + rnd);
		if (galaxyShape == null) // BR: to avoid crash!
			galaxyShape = new GalaxyRectangularShape(this);
        selectedGalaxyShapeOption1 = galaxyShape.randomOption();
        selectedGalaxyShapeOption2 = galaxyShape.randomOption();
    	switch (rnd) {
			case 11:
	            galaxyShape = new GalaxyLorenzShape(this); break;
			case 10:
	            galaxyShape = new GalaxySwirlClustersShape(this); break;
			case 9:
	            galaxyShape = new GalaxyGridShape(this); break;
			case 8:
	            galaxyShape = new GalaxyMazeShape(this); break;
			case 7:
	            galaxyShape = new GalaxyFractalShape(this); break;
			case 6:
	            galaxyShape = new GalaxyBullseyeShape(this); break;
			case 5:
	            galaxyShape = new GalaxyShurikenShape(this); break;
			case 4:
	            galaxyShape = new GalaxyClusterShape(this); break;
			case 3:
	            galaxyShape = new GalaxySpiralArmsShape(this); break;
	        case 2:
	            galaxyShape = new GalaxySpiralShape(this); break;
	        case 1:
	            galaxyShape = new GalaxyEllipticalShape(this); break;
	        case 0:
	        default:
	            galaxyShape = new GalaxyRectangularShape(this);
    	}
        if (rotp.Rotp.noOptions("setRandomGalaxyShape()"))
        	return;
		shapeOption1().reInit(new ArrayList<>()); // New shape -> Reset the list
		shapeOption1().defaultValue(galaxyShape.randomOption());
		shapeOption2().reInit(new ArrayList<>()); // New shape -> Reset the list
		shapeOption2().defaultValue(galaxyShape.randomOption());
//        selectedGalaxyShapeOption1 = galaxyShape.randomOption();
//        selectedGalaxyShapeOption2 = galaxyShape.randomOption();
    }

    private void setBaseGalaxyShape() { // BR: Init and rest sub options
        switch(selectedGalaxyShape) {
            case SHAPE_ELLIPTICAL:
                galaxyShape = new GalaxyEllipticalShape(this); break;
            case SHAPE_SPIRAL:
                galaxyShape = new GalaxySpiralShape(this); break;
            // modnar: add new map shapes
            case SHAPE_TEXT:
                galaxyShape = new GalaxyTextShape(this); break;
            case SHAPE_CLUSTER:
                galaxyShape = new GalaxyClusterShape(this); break;
			case SHAPE_SWIRLCLUSTERS:
                galaxyShape = new GalaxySwirlClustersShape(this); break;
			case SHAPE_GRID:
                galaxyShape = new GalaxyGridShape(this); break;
			case SHAPE_SPIRALARMS:
                galaxyShape = new GalaxySpiralArmsShape(this); break;
			case SHAPE_MAZE:
                galaxyShape = new GalaxyMazeShape(this); break;
			case SHAPE_SHURIKEN:
                galaxyShape = new GalaxyShurikenShape(this); break;
			case SHAPE_BULLSEYE:
                galaxyShape = new GalaxyBullseyeShape(this); break;
			case SHAPE_LORENZ:
                galaxyShape = new GalaxyLorenzShape(this); break;
			case SHAPE_FRACTAL:
                galaxyShape = new GalaxyFractalShape(this); break;
			case SHAPE_BITMAP:
                galaxyShape = new GalaxyBitmapShape(this); break;
            case SHAPE_RECTANGLE:
            default:
                galaxyShape = new GalaxyRectangularShape(this);
        }
        if (rotp.Rotp.noOptions("setBaseGalaxyShape()"))
        	return;
		shapeOption1().reInit(galaxyShapeOptions1()); // New shape -> Reset the list
		shapeOption1().defaultValue(galaxyShape.defaultOption1());
		shapeOption2().reInit(galaxyShapeOptions2()); // New shape -> Reset the list
		shapeOption2().defaultValue(galaxyShape.defaultOption2());
    }
    @Override
    public int numGalaxyShapeOption1() {
    	if (isRandomGalaxy())
    		return 0;
    	return galaxyShape.numOptions1();
    }
    @Override
    public int numGalaxyShapeOption2() {
    	if (isRandomGalaxy())
    		return 0;
    	return galaxyShape.numOptions2();
    }
    @Override
    public int numberStarSystems() {  // BR: For Profile Manager comments
    	return numberStarSystems(selectedGalaxySize());
    }
    @Override
    public int numberStarSystems(String size) { return galaxySizeMap(true, this).get(size); }

//    @Override
//    public int numberStarSystems(String size) { // BR: For Profile Manager comments
//        // MOO Strategy Guide, Table 3-2, p.50
//	    /*
//	    switch (selectedGalaxySize()) {
//	            case SIZE_SMALL:  return 24;
//	            case SIZE_MEDIUM: return 48;
//	            case SIZE_LARGE1:  return 70;
//	            case SIZE_HUGE:   return 108;
//	            default: return 48;
//	    }
//	    */
//	    switch (size) {
//	        case SIZE_MICRO:      return 24; // BR: added original moo small size
//	        case SIZE_TINY:       return 33;
//	        case SIZE_SMALL:      return 50;
//	        case SIZE_SMALL2:     return 70;
//	        case SIZE_MEDIUM:     return 100;
//	        case SIZE_MEDIUM2:    return 150;
//	        case SIZE_LARGE:      return 225;
//	        case SIZE_LARGE2:     return 333;
//	        case SIZE_HUGE:       return 500;
//	        case SIZE_HUGE2:      return 700;
//	        case SIZE_MASSIVE:    return 1000;
//	        case SIZE_MASSIVE2:   return 1500;
//	        case SIZE_MASSIVE3:   return 2250;
//	        case SIZE_MASSIVE4:   return 3333;
//	        case SIZE_MASSIVE5:   return 5000;
//	        case SIZE_INSANE:     return 10000;
//	        case SIZE_LUDICROUS:  return 100000;
//	        case SIZE_MAXIMUM:    return maximumSystems();
//	        case SIZE_DYNAMIC: // BR: Added an option to select from the opponents number
//	        default:
//	        	return min(maximumSystems(), 
//	        			1 + Math.round(selectedDynStarsPerEmpire() // +1 for Orion
//	        					* (selectedNumberOpponents()+1))); // +1 for player
//	    }
//	}
    @Override
    public int numberNebula() {
        if (selectedNebulaeOption().equals(NEBULAE_NONE))
            return 0;
        
        float freq = 1.0f;
        switch(selectedNebulaeOption()) {
            case NEBULAE_RARE:     freq = 0.25f; break;
            case NEBULAE_UNCOMMON: freq = 0.5f; break;
            case NEBULAE_COMMON:   freq = 2.0f; break;
            case NEBULAE_FREQUENT: freq = 4.0f; break;
        }
        // MOO Strategy Guide, Table 3-3, p.51
        /*
        switch (selectedGalaxySize()) {
        case SIZE_SMALL:     return roll(0,1);
        case SIZE_MEDIUM:    return roll(1,2);
        case SIZE_LARGE:     return roll(2,3);
        case SIZE_HUGE:      return roll(2,4);
        case SIZE_LUDICROUS: return roll(10,20);
        default: return roll(1,2);
        }
        */
        int nStars = numberStarSystems();
        float sizeMult = nebulaSizeMult();
        int nNeb = (int) nStars/20;
        
        return (int) (freq*nNeb/sizeMult/sizeMult);
    }
    @Override
    public float nebulaSizeMult() {
        int nStars = numberStarSystems();
        if (nStars < 200)
            return 1;
        else 
            return min(10,sqrt(nStars/200f));
    }
    @Override
    public int selectedAI(Empire e) {
        if (e.isPlayer()) {
    		return IGameOptions.autoPlayAIset().id(selectedAutoplayOption());
        }
        else
        	if (OPPONENT_AI_SELECTABLE.equals(selectedOpponentAIOption())) {
        		return IGameOptions.globalAIset().id(specificOpponentAIOption(e.id));
        	}
	        else {
        		return IGameOptions.globalAIset().id(selectedOpponentAIOption());
	        }
    }
    @Override
    public float hostileTerraformingPct() { 
        switch(selectedTerraformingOption()) {
            case TERRAFORMING_NONE:  return 0.0f;
            case TERRAFORMING_REDUCED: return 0.5f;
            default:  return 1.0f;
        }
    }
    private float fastResearch(int techLevel, float[] a) {
    	return BASE_RESEARCH_MOD * (a[1]/(techLevel+a[2]) + a[3]);
    }
    private float slowResearch(int techLevel, float[] a) {
    	return BASE_RESEARCH_MOD * ((a[1]*techLevel*sqrt(techLevel) + a[2]) / techLevel - a[3]);
    }
    @Override
    public float researchCostBase(int techLevel) {
        // this is a flat research rate adjustment. The method that calls this to calculate
        // the research cost already factors in the tech level (squared), the map sizes, and
        // the number of opponents.
        
        // the various "slowing" options increase the research cost for higher tech levels
        
        // modnar: adjust research costs to asymptotically reach their original scaling
        // mainly to keep low tech level costs similar to RESEARCH_NORMAL (1.00)
        // also corrects for old_SLOW's cheaper techLevel==2 and same cost techLevel==3
        //
        // techLevel:     2     3     4     5     6     7     8     9     10     20     30     40     50    100
        // old_SLOW:     0.82  1.00  1.15  1.29  1.41  1.53  1.63  1.73  1.83   2.58   3.16   3.65   4.08   5.77
        // new_SLOW:     1.15  1.17  1.25  1.34  1.44  1.53  1.62  1.71  1.80   2.53   3.12   3.62   4.06   5.81
        // old_SLOWER:   1.41  1.73  2.00  2.24  2.45  2.65  2.83  3.00  3.16   4.47   5.48   6.32   7.07  10.00
        // new_SLOWER:   1.20  1.25  1.40  1.58  1.77  1.96  2.14  2.32  2.49   3.97   5.14   6.14   7.03  10.52
        // old_SLOWEST:  3.16  3.87  4.47  5.00  5.48  5.92  6.32  6.71  7.07  10.00  12.25  14.14  15.81  22.36
        // new_SLOWEST:  1.24  1.36  1.75  2.21  2.68  3.15  3.61  4.06  4.49   8.17  11.10  13.60  15.81  24.55
        
        switch(selectedResearchRate()) {
            // modnar: add fast research option
            case RESEARCH_FAST:
            	return fastResearch(techLevel, R_PARAM_FAST);
                // return amt*(1.0f/(techLevel+2.0f) + 0.5f);    // modnar: asymptotically approach 2x faster
            case RESEARCH_SLOW:
            	return slowResearch(techLevel, R_PARAM_SLOW);
                //return amt*((0.6f*techLevel*sqrt(techLevel)+1.0f)/techLevel - 0.2f); // modnar: asymptotically similar
                //return amt*sqrt(techLevel/3.0f); // approx. 4x slower for level 50
            case RESEARCH_SLOWER:
            	return slowResearch(techLevel, R_PARAM_SLOWER);
            	//return amt * slow(techLevel, 1.2f, 1.5f);
                //return amt*((1.2f*techLevel*sqrt(techLevel)+2.0f)/techLevel - 1.5f); // modnar: asymptotically similar
                //return amt*sqrt(techLevel);   // approx. 7x slower for level 50
            case RESEARCH_CRAWLING:
            	return slowResearch(techLevel, R_PARAM_CRAWLING);
                //return amt*((3.0f*techLevel*sqrt(techLevel)+5.0f)/techLevel - 5.5f); // modnar: asymptotically similar
                //return amt*sqrt(techLevel*5); // approx. 16x slower for level 50
            case RESEARCH_IMPEDED:
            	return slowResearch(techLevel, R_PARAM_IMPEDED);
            case RESEARCH_LETHARGIC:
            	return slowResearch(techLevel, R_PARAM_LETHARGIC);
            case RESEARCH_NORMAL:
            default:  
                return BASE_RESEARCH_MOD;   // no additional slowing. 
        }
    }
    @Override
    public  int baseAIRelationsAdj()       { 
        switch(selectedAIHostilityOption()) {
            case AI_HOSTILITY_LOWEST:  return 30;
            case AI_HOSTILITY_LOWER:   return 20;
            case AI_HOSTILITY_LOW:     return 10;
            case AI_HOSTILITY_HIGH:    return -10;
            case AI_HOSTILITY_HIGHER:  return -20;
            case AI_HOSTILITY_HIGHEST: return -30;
            default: return 0;
        } 
    }

    @Override
    public boolean canTradeTechs(Empire e1, Empire e2) {
        switch(selectedTechTradeOption()) {
            case TECH_TRADING_YES: return true;
            case TECH_TRADING_NO:
            case TECH_TRADING_NO_AID:  return false;
            case TECH_TRADING_ALLIES:
            case TECH_TRADING_ALLIES_AID: return e1.alliedWith(e2.id);
        }
        return true;
    }
    @Override
    public boolean canOfferTechs(Empire e1, Empire e2) {
        switch(selectedTechTradeOption()) {
            case TECH_TRADING_YES:
            case TECH_TRADING_NO:
            case TECH_TRADING_ALLIES: return true;
            case TECH_TRADING_NO_AID: return false;
            case TECH_TRADING_ALLIES_AID: return e1.alliedWith(e2.id);
        }
        return true;
    }
    @Override
    public boolean allowRandomEvent(RandomEvent ev) {
        switch(selectedRandomEventOption()) {
            case RANDOM_EVENTS_ON:  return true;
            case RANDOM_EVENTS_OFF: return false;
            case RANDOM_EVENTS_NO_MONSTERS: return !ev.monsterEvent();
            case RANDOM_EVENTS_TECH_MONSTERS: return ev.techDiscovered() ;
            case RANDOM_EVENTS_ONLY_MONSTERS: return ev.monsterEvent() && ev.techDiscovered() ;
        }
        return true;
    }
    @Override
    public int warpSpeed(TechEngineWarp tech) {
        switch(selectedWarpSpeedOption()) {
            case WARP_SPEED_NORMAL:
            	return tech.baseWarp();
            // case WARP_SPEED_FAST: return fibonacci(tech.baseWarp());
            // modnar: adjust Fast Warp down at advanced Engines
            //         use [A033638] https://oeis.org/A033638
            //         a(n) = floor(n^2/4)+1
            // Normal:     1, 2, 3, 4, 5,  6,  7,  8,  9
            // FastMOD:    1, 2, 3, 5, 7, 10, 13, 17, 21
            // Fibonacci:  1, 2, 3, 5, 8, 13, 21, 34, 55
            case WARP_SPEED_FAST:
            	return quarterSquaresPlusOne(tech.baseWarp());
            case WARP_SPEED_FAST_F: // BR: restored original Fast option
            	return fibonacci(tech.baseWarp());
        }
        return tech.baseWarp();
    }
    @Override
    public String randomStarType() {
        float[] pcts;

        // normalPcts represents star type distribution per MOO1 Official Strategy Guide
        //                     RED, ORANG, YELL, BLUE,WHITE, PURP
        float[] normalPcts = { .30f, .55f, .70f, .85f, .95f, 1.0f };
        float[] youngPcts  = { .20f, .40f, .55f, .85f, .95f, 1.0f };
        float[] oldPcts    = { .50f, .65f, .75f, .80f, .85f, 1.0f };

        int typeIndex = 0;
        switch(selectedGalaxyAge()) {
            case GALAXY_AGE_YOUNG:  pcts = youngPcts; break;
            case GALAXY_AGE_OLD:    pcts = oldPcts; break;
            default:                pcts = normalPcts; break;
        }
        
        float r = random();
        for (int i=0;i<pcts.length;i++) {
            if (r <= pcts[i]) {
                typeIndex = i;
                break;
            }
        }

        switch(typeIndex) {
            case 0:  return StarType.RED;
            case 1:  return StarType.ORANGE;
            case 2:  return StarType.YELLOW;
            case 3:  return StarType.BLUE;
            case 4:  return StarType.WHITE;
            case 5:  return StarType.PURPLE;
            default: return StarType.RED;
        }
    }
    // BR: Made this String Array public
    /**
     * @return List of all planetTypes Key 
     * in the sequence used by randomPlanet()
     */
    private static String[] planetTypes() {
    	return new String[] {
    			PlanetType.NONE,
    			PlanetType.RADIATED,
    			PlanetType.TOXIC,
    			PlanetType.INFERNO,
    			PlanetType.DEAD,
    			PlanetType.TUNDRA,
    			PlanetType.BARREN,
    			PlanetType.MINIMAL,
    			PlanetType.DESERT,
    			PlanetType.STEPPE,
    			PlanetType.ARID,
    			PlanetType.OCEAN,
    			PlanetType.JUNGLE,
    			PlanetType.TERRAN
    			};
    } // \BR
    
    @Override
    public Planet randomPlanet(StarSystem s) {
        Planet p = new Planet(s);
        String[] planetTypes = planetTypes(); // BR: Made this String Array public
        float[] pcts;

        float[] redPcts =    { .05f, .10f, .15f, .20f, .25f, .30f, .35f, .40f, .50f, .60f, .75f, .85f, .95f, 1.0f };
        float[] greenPcts =  { .05f, .10f, .15f, .20f, .25f, .30f, .35f, .40f, .45f, .55f, .65f, .75f, .85f, 1.0f };
        float[] yellowPcts = { .00f, .00f, .00f, .05f, .05f, .10f, .15f, .20f, .25f, .30f, .40f, .50f, .60f, 1.0f };
        float[] bluePcts =   { .15f, .25f, .35f, .45f, .55f, .65f, .75f, .85f, .90f, .95f, 1.0f, 1.0f, 1.0f, 1.0f };
        float[] whitePcts =  { .10f, .15f, .25f, .35f, .45f, .55f, .65f, .75f, .85f, .90f, .95f, 1.0f, 1.0f, 1.0f };
        float[] purplePcts = { .20f, .45f, .60f, .75f, .85f, .90f, .95f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f };

        int typeIndex = 0;
        switch (s.starType().key()) {
            case StarType.RED:    pcts = redPcts;    break;
            case StarType.ORANGE: pcts = greenPcts;  break;
            case StarType.YELLOW: pcts = yellowPcts; break;
            case StarType.BLUE:   pcts = bluePcts;   break;
            case StarType.WHITE:  pcts = whitePcts;  break;
            case StarType.PURPLE: pcts = purplePcts; break;
            default:
                pcts = redPcts; break;
        }

        float r;
        
        // modnar: change PLANET_QUALITY settings, comment out poor to great settings
        // BR: restored; both are compatible ==> Player choice!
        // BR: added Hell and Heaven!
        switch(selectedPlanetQualityOption()) {
            case PLANET_QUALITY_HELL:     r = random() * 0.5f; break;
            case PLANET_QUALITY_POOR:     r = random() * 0.8f; break;
            case PLANET_QUALITY_MEDIOCRE: r = random() * 0.9f; break;
            case PLANET_QUALITY_GOOD:     r = 0.1f + (random() * 0.9f); break;
            case PLANET_QUALITY_GREAT:    r = 0.2f + (random() * 0.8f); break;
            case PLANET_QUALITY_HEAVEN:   r = 0.5f + (random() * 0.5f); break;
            case PLANET_QUALITY_NORMAL:
            default:					  r = random(); break;
        }
        
        for (int i=0;i<pcts.length;i++) {
            if (r <= pcts[i]) {
                typeIndex = i;
                break;
            }
        }
        p.initPlanetType(planetTypes[typeIndex]);

        checkForHostileEnvironment(p, s);

        checkForPoorResources(p, s);
        if (p.isResourceNormal())
            checkForRichResources(p, s);
        if (p.isResourceNormal() || allowRichPoorArtifact())
            checkForArtifacts(p, s);
        return p;
    }
    @Override
    public String randomPlayerStarType(Race r)     { return StarType.YELLOW; }
    @Override
    public String randomRaceStarType(Race r)       { 
        List<String> types = new ArrayList<>();
        types.add(StarType.RED);
        types.add(StarType.ORANGE);
        types.add(StarType.YELLOW);

        return random(types); 
    }
    @Override
    public String randomOrionStarType()       { 
        List<String> types = new ArrayList<>();
        types.add(StarType.RED);
        types.add(StarType.ORANGE);
        types.add(StarType.YELLOW);

        return random(types); 
    }
    @Override
    public Planet orionPlanet(StarSystem s) {
        Planet p = new Planet(s);
        p.initPlanetType("PLANET_TERRAN");
        return p;
    }
    @Override
    public Planet randomPlayerPlanet(Race r, StarSystem s) {
        Planet p = new Planet(s);
        p.initPlanetType(r.homeworldPlanetType);
        return p;
    }
    // @Override public List<String> galaxySizeOptions()     { return IGameOptions.getGalaxySizeOptions(); }
    // @Override public List<String> galaxyShapeOptions()    { return IGameOptions.getGalaxyShapeOptions(); }
    @Override public List<String> galaxyShapeOptions1()   { return galaxyShape.options1(); }
    @Override public List<String> galaxyShapeOptions2()   { return galaxyShape.options2(); }
    @Override public List<String> galaxyAgeOptions()      { return IGameOptions.getGalaxyAgeOptions(); }
    @Override public List<String> gameDifficultyOptions() { return IGameOptions.getGameDifficultyOptions(); }
    @Override public List<String> researchRateOptions()   { return IGameOptions.getResearchRateOptions(); }
    @Override public List<String> techTradingOptions()    { return IGameOptions.getTechTradingOptions(); }
    @Override public List<String> randomEventOptions()    { return IGameOptions.getRandomEventOptions(); }
    @Override public List<String> warpSpeedOptions()      { return IGameOptions.getWarpSpeedOptions(); }
    @Override public List<String> nebulaeOptions()        { return IGameOptions.getNebulaeOptions(); }
    @Override public List<String> councilWinOptions()     { return IGameOptions.getCouncilWinOptions(); }
    @Override public List<String> starDensityOptions()    { return IAdvOptions.getStarDensityOptions(); }
    @Override public List<String> aiHostilityOptions()    { return IGameOptions.getAiHostilityOptions(); }
    @Override public List<String> planetQualityOptions()  { return IGameOptions.getPlanetQualityOptions(); }
    @Override public List<String> terraformingOptions()   { return IGameOptions.getTerraformingOptions(); }
    @Override public List<String> colonizingOptions()     { return IGameOptions.getColonizingOptions(); }
    @Override public List<String> fuelRangeOptions()      { return IGameOptions.getFuelRangeOptions(); }
    @Override public List<String> randomizeAIOptions()    { return IGameOptions.getRandomizeAIOptions(); }
    @Override public List<String> autoplayOptions()       { return IGameOptions.autoPlayAIset().getAutoPlay(); }
    @Override public List<String> opponentAIOptions()     { return IGameOptions.globalAIset().getAliens(); }
    @Override
    public List<String> specificOpponentAIOptions() { // BR: new access to base specific opponents
    	return IGameOptions.specificAIset().getAliens();
    }
    @Override
    public List<String> newRaceOffOptions()	  { return baseRaceOptions(); }
    @Override
    public List<String> startingRaceOptions() {  return allRaceOptions(); }
    @Override
    public List<Integer> possibleColors()	  { return new ArrayList<>(colors); }
    @Override public void setAndGenerateGalaxy() {
       	setGalaxyShape(selectedGalaxyShapeOption1, selectedGalaxyShapeOption2);
    	if(isRandomGalaxy()) {
    		setRandomGalaxyShape();
    		generateGalaxy();
    		return;
    	}
       	generateGalaxy();
    }
    private void generateGalaxy() { galaxyShape().quickGenerate(); }
    @Override
    public Color color(int i)     {
    	if (i<0 || i>=empireColors.size())
    		randomizeColors();
    	return empireColors.get(i);
    }
    @Override
    public void randomizeColors() {
		// modnar: add new colors
        empireColors.clear();
		empireColors.add(new Color(237,28,36));   // red
		empireColors.add(new Color(0,166,81));    // green
		empireColors.add(new Color(247,229,60));  // yellow
		empireColors.add(new Color(9,131,214));   // blue
		empireColors.add(new Color(255,127,0));   // orange
		empireColors.add(new Color(145,51,188));  // purple
		empireColors.add(new Color(0,255,255));   // modnar: aqua
		empireColors.add(new Color(255,0,255));   // modnar: fuchsia
		empireColors.add(new Color(132,57,20));   // brown
		empireColors.add(new Color(255,255,255)); // white
		empireColors.add(new Color(0,255,0));     // modnar: lime
		empireColors.add(new Color(128,128,128)); // modnar: grey
		empireColors.add(new Color(220,160,220)); // modnar: plum*
		empireColors.add(new Color(160,220,250)); // modnar: light blue*
		empireColors.add(new Color(170,255,195)); // modnar: mint*
		empireColors.add(new Color(128,128,0));   // modnar: olive**
		//empireColors.add(new Color(255,215,180)); // modnar: apricot*
		
        //empireColors.add(new Color(9,131,214));   // blue
        //empireColors.add(new Color(132,57,20));   // brown
        //empireColors.add(new Color(0,166,81));    // green
        //empireColors.add(new Color(255,127,0));   // orange
        //empireColors.add(new Color(247,127,230)); // pink
        //empireColors.add(new Color(145,51,188));  // purple
        //empireColors.add(new Color(237,28,36));   // red
        //empireColors.add(new Color(56,232,186));  // teal
        //empireColors.add(new Color(247,229,60));  // yellow
        //empireColors.add(new Color(255,255,255)); // white

        colors.clear();
        //primary color list
        List<Integer> list1 = new ArrayList<>();
        list1.add(0);
        list1.add(1);
        list1.add(2);
        list1.add(3);
        list1.add(4);
        list1.add(5);
        list1.add(6);
        list1.add(7);
        list1.add(8);
        list1.add(9);
		
        //secondary color list
        List<Integer> list1a = new ArrayList<>();
        list1a.add(10);
        list1a.add(11);
        list1a.add(12);
        list1a.add(13);
		list1a.add(14);
        list1a.add(15);

        // start repeating the 10-color list for copies of races (up to 5 per race)
		// modnar: due to new Races, get 16 colors
        List<Integer> list2 = new ArrayList<>(list1);
        list2.addAll(list1a);
        List<Integer> list3 = new ArrayList<>(list2);
        List<Integer> list4 = new ArrayList<>(list2);
        List<Integer> list5 = new ArrayList<>(list2);
            
        Collections.shuffle(list1);
        Collections.shuffle(list1a);
        Collections.shuffle(list2);
        Collections.shuffle(list3);
        Collections.shuffle(list4);
        Collections.shuffle(list5);
		// modnar: due to new colors, only add first 16 colors of shuffled lists, subList(0,16)
        colors.addAll(list1);
        colors.addAll(list1a.subList(0,6));
        colors.addAll(list2.subList(0,16));
        colors.addAll(list3.subList(0,16));
        colors.addAll(list4.subList(0,16));
        colors.addAll(list5.subList(0,16));
    }
    // private void initOpponentRaces() {}
    private void checkForHostileEnvironment(Planet p, StarSystem s) {
        // these planet types and no chance for poor resources -- skip
        switch(p.type().key()) {
            case PlanetType.NONE:
                p.makeEnvironmentNone();
                break;
            case PlanetType.RADIATED:
            case PlanetType.TOXIC:
            case PlanetType.INFERNO:
            case PlanetType.DEAD:
            case PlanetType.TUNDRA:
            case PlanetType.BARREN:
                p.makeEnvironmentHostile();
                break;
            case PlanetType.DESERT:
            case PlanetType.STEPPE:
            case PlanetType.ARID:
            case PlanetType.OCEAN:
            case PlanetType.JUNGLE:
            case PlanetType.TERRAN:
                if (random() < .083333)
                    p.enrichSoil();  // become fertile
                break;
        }
    }
    private void checkForPoorResources(Planet p, StarSystem s) {
        // these planet types and no chance for poor resources -- skip
        float r1 = 0;
        float r2 = 0;
        switch(p.type().key()) {
            case PlanetType.NONE:
            	return; // BR: No way to force asteroids to poor!
            case PlanetType.RADIATED:
            case PlanetType.TOXIC:
            case PlanetType.INFERNO:
            case PlanetType.DEAD:
            case PlanetType.TUNDRA:
            case PlanetType.BARREN:
            case PlanetType.JUNGLE:
            case PlanetType.TERRAN:
            	break; // BR: to allow special customization

            default:
                switch(s.starType().key()) {
                case StarType.BLUE:
                case StarType.WHITE:
                case StarType.YELLOW:
                    r1 = .025f; r2 = .10f;
                    break;
                case StarType.RED:
                    r1 = .06f;  r2 = .20f;
                    break;
                case StarType.ORANGE:
                    r1 = .135f; r2 = .30f;
                    break;
                case StarType.PURPLE:
                    // can never have poor/ultra poor // BR: except told otherwise!
                	break;
                default:
                    throw new RuntimeException(concat("Invalid star type for options: ", s.starType().key()));
            }
        }
        // modnar: change PLANET_QUALITY settings, 20% more Poor with LARGER, 20% less Poor with RICHER
        switch(selectedPlanetQualityOption()) {
            case PLANET_QUALITY_LARGER:   r1 *= 1.2f; r2 *= 1.2f; break;
            case PLANET_QUALITY_RICHER:   r1 *= 0.8f; r2 *= 0.8f; break;
            case PLANET_QUALITY_NORMAL:   break;
            default:    break;
        }
        // BR: Special player customization
        r1 = ultraPoorPlanetProb(r1);
        r2 = poorPlanetProb(r2);
        
        float r = random();
        if (r < r1)
            p.setResourceUltraPoor();
        else if (r < r2)
            p.setResourcePoor();
    }
    private void checkForRichResources(Planet p, StarSystem s) {
    	if (p.type().key() == PlanetType.NONE)
    		return; // BR: asteroids !!!
        // planet/star ratios per Table 3-9a of Strategy Guide
        float r1 = 0;
        float r2 = 0;
        switch(s.starType().key()) {
            case StarType.RED:
            case StarType.WHITE:
            case StarType.YELLOW:
            case StarType.ORANGE:
                switch(p.type().key()) {
                    case PlanetType.RADIATED:   r1 = .2625f; r2 = .35f; break;
                    case PlanetType.TOXIC:      r1 = .225f;  r2 = .30f; break;
                    case PlanetType.INFERNO:    r1 = .1875f; r2 = .25f; break;
                    case PlanetType.DEAD:       r1 = .15f;   r2 = .20f; break;
                    case PlanetType.TUNDRA:     r1 = .1125f; r2 = .15f; break;
                    case PlanetType.BARREN:     r1 = .075f;  r2 = .10f; break;
                    case PlanetType.MINIMAL:    r1 = .0375f; r2 = .05f; break;
                }
                break;
            case StarType.BLUE:
                switch(p.type().key()) {
                    case PlanetType.RADIATED:   r1 = .2925f; r2 = .45f; break;
                    case PlanetType.TOXIC:      r1 = .26f;   r2 = .40f; break;
                    case PlanetType.INFERNO:    r1 = .2275f; r2 = .35f; break;
                    case PlanetType.DEAD:       r1 = .195f;  r2 = .30f; break;
                    case PlanetType.TUNDRA:     r1 = .1625f; r2 = .25f; break;
                    case PlanetType.BARREN:     r1 = .13f;   r2 = .20f; break;
                    case PlanetType.MINIMAL:    r1 = .0975f; r2 = .15f; break;
                    case PlanetType.DESERT:     r1 = .065f;  r2 = .10f; break;
                    case PlanetType.STEPPE:     r1 = .0325f; r2 = .05f; break;
                }
                break;
            case StarType.PURPLE:
                switch(p.type().key()) {
                    case PlanetType.RADIATED:   r1 = .30f;   r2 = .60f; break;
                    case PlanetType.TOXIC:      r1 = .275f;  r2 = .55f; break;
                    case PlanetType.INFERNO:    r1 = .25f;   r2 = .50f; break;
                    case PlanetType.DEAD:       r1 = .225f;  r2 = .45f; break;
                    case PlanetType.TUNDRA:     r1 = .20f;   r2 = .40f; break;
                    case PlanetType.BARREN:     r1 = .175f;  r2 = .35f; break;
                    case PlanetType.MINIMAL:    r1 = .15f;   r2 = .30f; break;
                }
                break;
            default:
                throw new RuntimeException(concat("Invalid star type for options: ", s.starType().key()));
        }

        // modnar: change PLANET_QUALITY settings, 20% less Rich with LARGER, 50% more Rich with RICHER
        switch(selectedPlanetQualityOption()) {
            case PLANET_QUALITY_LARGER:   r1 *= 0.8f; r2 *= 0.8f; break;
            case PLANET_QUALITY_RICHER:   r1 *= 1.5f; r2 *= 1.5f; break;
            case PLANET_QUALITY_NORMAL:   break;
            default:    break;
        }
        // BR: Special player customization
        r1 = richPlanetProb(r1);
        r2 = ultraRichPlanetProb(r2);
        
        float r = random();
        if (r < r1)
            p.setResourceRich();
        else if (r < r2)
            p.setResourceUltraRich();
    }
    private void checkForArtifacts(Planet p, StarSystem s) {
    	if (p.type().key() == PlanetType.NONE)
    		return; // BR: asteroids !!!
        // modnar: no Artifact planets if randomTechStart selected
        float rArtifact = 1.0f;
        // modnar: change PLANET_QUALITY settings, 50% more Artifact with RICHER
        switch(selectedPlanetQualityOption()) {
            case PLANET_QUALITY_LARGER:   break;
            case PLANET_QUALITY_RICHER:   rArtifact *= 1.5f; break;
            case PLANET_QUALITY_NORMAL:   break;
            default:    break;
        }
        if (randomTechStart.get()) {
            rArtifact *= 0.0f; // modnar: no Artifact planets if randomTechStart selected
        }
        switch(p.type().key()) {
            case PlanetType.STEPPE:
            case PlanetType.ARID:
            case PlanetType.OCEAN:
            case PlanetType.JUNGLE:
            case PlanetType.TERRAN:
            	break;
            default:
            	rArtifact *= 0.0f;
        }
        float r = random();
        if (r < 0.1f * rArtifact) // modnar: change artifact ratio for testing, original 0.10
        	p.setArtifact();
        else if (r <= orionPlanetProb())
        	p.setOrionArtifact();
    }
    // ========== All Menu Options ==========
    private void setBaseSettingsToDefault() {
    	setBaseGalaxySettingsToDefault();
    	setBaseRaceSettingsToDefault();
        setAdvancedOptionsToDefault();
    }
    // ========== Race Menu Options ==========
    @Override public void setRandomPlayerRace() { // BR:
    	if (rotp.Rotp.noOptions("setBaseRaceSettingsToDefault()")) {
    		selectedPlayerRace(random(baseRaceOptions()));
    		return;
    	}
        if (selectedShowNewRaces()) // BR: limit randomness
        	selectedPlayerRace(random(allRaceOptions()));
        else
        	selectedPlayerRace(random(baseRaceOptions()));
        player.update(this);
    }
    private void setBaseRaceSettingsToDefault() { // BR:
//    	if (!rotp.Rotp.noOptions("setBaseRaceSettingsToDefault()"))
//    		playerIsCustom.setFromDefault(false, false);
     	setRandomPlayerRace();
        selectedPlayerColor(0);
    }
    private void copyBaseRaceSettings(MOO1GameOptions dest) { // BR:
    	dest.selectedPlayerRace(selectedPlayerRace());
    	dest.selectedPlayerColor(selectedPlayerColor());
    }
    // ========== Galaxy Menu Options ==========
    private void setBaseGalaxySettingsToDefault() { // BR:
        selectedGalaxySize = SIZE_SMALL;
        selectedGalaxyShape = SHAPE_RECTANGLE;
        setGalaxyShape();
        selectedNumberOpponents = defaultOpponentsOptions();
        for (int i=0;i<opponentRaces.length;i++)
        	opponentRaces[i] = null;
        
//        if (rotp.Rotp.noOptions("setBaseGalaxySettingsToDefault()"))
//        	selectedPlayerRace(random(allRaceOptions()));
//        else if (selectedShowNewRaces()) // BR: limit randomness
//        	selectedPlayerRace(random(allRaceOptions()));
//        else
//        	selectedPlayerRace(random(baseRaceOptions()));
        selectedGameDifficulty = DIFFICULTY_NORMAL;
        selectedOpponentAIOption = defaultAI.aliensKey;
        for (int i=0;i<specificOpponentAIOption.length;i++)
		    specificOpponentAIOption[i] = defaultAI.aliensKey;
        if(specificOpponentCROption != null) {
        	String defVal = SpecificCROption.defaultSpecificValue().value;
	        for (int i=0;i<specificOpponentCROption.length;i++)
			    specificOpponentCROption[i] = defVal;
        }
    }
    private void copyBaseGalaxySettings(MOO1GameOptions dest) { // BR:
    	dest.selectedGalaxySize  = selectedGalaxySize;
    	dest.selectedGalaxyShape = selectedGalaxyShape;
    	dest.setGalaxyShape(selectedGalaxyShapeOption1, selectedGalaxyShapeOption2);
    	dest.selectedNumberOpponents = selectedNumberOpponents;
    	dest.selectedGameDifficulty	 = selectedGameDifficulty;
        for (int i=0; i<dest.opponentRaces.length; i++)
        	dest.opponentRaces[i] = opponentRaces[i];
        if(dest.specificOpponentCROption != null)
        	for (int i=0; i<dest.specificOpponentCROption.length; i++)
        		dest.specificOpponentCROption[i] = specificOpponentCROption[i];
    	copyAliensAISettings(dest);
    }
    // ========== Other Menu ==========
    @Override public  void setAdvancedOptionsToDefault() {
        selectedGalaxyAge = GALAXY_AGE_NORMAL;
        selectedPlanetQualityOption = PLANET_QUALITY_NORMAL;
        selectedTerraformingOption = TERRAFORMING_NORMAL;
        selectedColonizingOption = COLONIZING_NORMAL;
        selectedResearchRate = RESEARCH_NORMAL;
        selectedTechTradeOption = TECH_TRADING_YES;
        selectedRandomEventOption = RANDOM_EVENTS_NO_MONSTERS;
        selectedWarpSpeedOption = WARP_SPEED_NORMAL;
        selectedFuelRangeOption = FUEL_RANGE_NORMAL;
        selectedNebulaeOption = NEBULAE_NORMAL;
        selectedCouncilWinOption = COUNCIL_REBELS;
        selectedStarDensityOption = STAR_DENSITY_NORMAL;
        selectedRandomizeAIOption = RANDOMIZE_AI_NONE;
        selectedAutoplayOption = AUTOPLAY_OFF;
        selectedAIHostilityOption = AI_HOSTILITY_NORMAL;
    }
    private void copyAdvancedOptions(MOO1GameOptions dest) { // BR:
        dest.selectedGalaxyAge			= selectedGalaxyAge;
        dest.selectedPlanetQualityOption =selectedPlanetQualityOption;
        dest.selectedTerraformingOption = selectedTerraformingOption;
        dest.selectedColonizingOption	= selectedColonizingOption;
        dest.selectedResearchRate		= selectedResearchRate;
        dest.selectedTechTradeOption	= selectedTechTradeOption;
        dest.selectedRandomEventOption	= selectedRandomEventOption;
        dest.selectedWarpSpeedOption	= selectedWarpSpeedOption;
        dest.selectedFuelRangeOption	= selectedFuelRangeOption;
        dest.selectedNebulaeOption		= selectedNebulaeOption;
        dest.selectedCouncilWinOption	= selectedCouncilWinOption;
        dest.selectedStarDensityOption	= selectedStarDensityOption;
        dest.selectedRandomizeAIOption	= selectedRandomizeAIOption;
        dest.selectedAutoplayOption		= selectedAutoplayOption;
        dest.selectedAIHostilityOption	= selectedAIHostilityOption;
   }
    // ==================== Generalized options' Tools methods ====================
    //
    private void copyAllBaseSettings(MOO1GameOptions dest) {
		copyBaseGalaxySettings(dest);
		copyBaseRaceSettings(dest);
		copyAdvancedOptions(dest);
    }
    private void copyPanelBaseSettings(MOO1GameOptions dest, SafeListParam pList) {
   		if (pList == optionsGalaxy()) {
   			copyBaseGalaxySettings(dest);
   			return;
   		}
   		if (pList == optionsRace()) {
   			copyBaseRaceSettings(dest);
   			return;
   		}
   		if (pList == allModOptions()) {
    		System.err.println("Old call of copyPanelBaseSettings(allModOptions)");
    		copyAllBaseSettings(dest);
   		}
    }
    private void setAllNonCfgGameSettingsToDefault() { // settings saved in game file.
       	for (IParam param : allModOptions())
       		if (param != null && !param.isCfgFile()) // Exclude .cfg parameters
	       		param.setFromDefault(true, false);
    }
    private void setAllNonCfgBaseSettingsToDefault() {
		setAdvancedOptionsToDefault();
		setBaseRaceSettingsToDefault();
		setBaseGalaxySettingsToDefault();
    }
    private void setPanelGameSettingsToDefault(SafeListParam pList,
    		boolean excludeCfg, boolean excludeSubMenu) {
    	if (pList == null)
    		return;
    	if (pList == allModOptions()) { // Should no more be used
    		System.err.println("Old call of setModSettingsToDefault(allModOptions)");
    		setAllNonCfgGameSettingsToDefault();
    	}
    	else 
	       	for (IParam param : pList)
	       		if (param != null
	       				&& !(excludeCfg && param.isCfgFile())
	       				&& !(excludeSubMenu && param.isSubMenu()))
		       		param.setFromDefault(excludeCfg, excludeSubMenu);
    }
    private void setPanelBaseSettingsToDefault(SafeListParam pList) {
   		if (pList == null)
   			return;
   		if (pList == optionsGalaxy()) {
   			setBaseGalaxySettingsToDefault();
   			return;
   		}
   		if (pList == optionsRace()) {
   			setBaseRaceSettingsToDefault();
   			return;
   		}
   		if (pList == allModOptions()) {
    		System.err.println("Old call of setPanelBaseSettingsToDefault(allModOptions)");
    		setAllNonCfgBaseSettingsToDefault();
  			return;
   		}
    }
    private void transfert(String fileName, boolean set) {
    	// to avoid loosing former cfg settings.
//    	MOO1GameOptions opts = loadOptions(fileName);
//		displayYear.transfert(opts, set);
//		showAlliancesGNN.transfert(opts, set);
//		showNextCouncil.transfert(opts, set);
//		showLimitedWarnings.transfert(opts, set);
//		techExchangeAutoRefuse.transfert(opts, set);
//		autoColonize_.transfert(opts, set);
//		autoBombard_.transfert(opts, set);
//		divertExcessToResearch.transfert(opts, set);
//		defaultMaxBases.transfert(opts, set);
//		saveOptions(opts, fileName);
    }
    // ==================== New Options files public access ====================
    //
    @Override public void loadStartupOptions() {
        System.out.println("==================== reset all options() ====================");
        resetAllNonCfgSettingsToDefault();
        System.out.println("==================== loadStartupOptions() ====================");
    	if (menuStartup.isUser()) {
    		updateAllNonCfgFromFile(USER_OPTIONS_FILE);
    		transfert(USER_OPTIONS_FILE, true);
    	}
    	else if (menuStartup.isGame()) {
    		updateAllNonCfgFromFile(GAME_OPTIONS_FILE);
    		transfert(GAME_OPTIONS_FILE, true);
    	}
    	else if (menuStartup.isDefault())
    		resetAllNonCfgSettingsToDefault();
    	else { // default = action.isLast()
    		updateAllNonCfgFromFile(LAST_OPTIONS_FILE);
    		transfert(LAST_OPTIONS_FILE, true);
    	}
		transfert(USER_OPTIONS_FILE, false);
		transfert(GAME_OPTIONS_FILE, false);
		transfert(LAST_OPTIONS_FILE, false);
		
		rotp.ui.UserPreferences.initialList(false);

    	if (!selectedPlayerIsCustom()) {
    		setRandomPlayerRace();
    	}
    }
    @Override public void copyAliensAISettings(IGameOptions dest) { // BR:
    	MOO1GameOptions d = (MOO1GameOptions) dest; 	
    	d.selectedOpponentAIOption = selectedOpponentAIOption;
        for (int i=0; i<d.specificOpponentAIOption.length; i++)
        	d.specificOpponentAIOption[i] = specificOpponentAIOption[i];
    }
    @Override public void resetAllNonCfgSettingsToDefault() {
    	setAllNonCfgGameSettingsToDefault();
    	setAllNonCfgBaseSettingsToDefault();
       	if (!Rotp.noOptions) // Better safe than sorry
       		for (IParam param : allModOptions())
        		param.initDependencies(IParam.VALID_DEPENDENCIES);
    }
    @Override public void resetPanelSettingsToDefault(SafeListParam pList,
    		boolean excludeCfg, boolean excludeSubMenu) {
    	setPanelGameSettingsToDefault(pList, excludeCfg, excludeSubMenu);
    	setPanelBaseSettingsToDefault(pList);
       	if (!Rotp.noOptions) // Better safe than sorry
       		for (IParam param : allModOptions())
        		param.initDependencies(IParam.VALID_DEPENDENCIES);
    }
    @Override public void saveOptionsToFile(String fileName) {
    	saveOptions(this, fileName);
    }
    @Override public void saveOptionsToFile(String fileName, SafeListParam pList) {
    	// No dependencies update on load... this will on final load.
    	if (pList == null)
    		return;
    	// Load the previous state
    	MOO1GameOptions fileOptions = loadOptions(fileName);
    	// Then merge with the listed options
       	for (IParam param : pList)
       		if (param != null) {
       			param.copyOption(this, fileOptions, false); // don't update tool
       		}
        saveOptions(fileOptions, fileName);
    }
    @Override public void updateAllNonCfgFromFile(String fileName) {
    	MOO1GameOptions source = loadOptions(fileName);
       	for (IParam param : allModOptions()) {
       		if (param != null) {
	       		if (param.isCfgFile()) { // Exclude .cfg parameters
	       			param.updateOptionTool();
	       		}
	       		else {
	       			param.copyOption(source, this, true); // Copy and update tool
	       		}
       		}
       	}
       	if (!Rotp.noOptions) // Better safe than sorry
       		for (IParam param : allModOptions())
        		param.initDependencies(IParam.VALID_DEPENDENCIES);
        source.copyAllBaseSettings(this);
    }
    @Override public void updateFromFile(String fileName, SafeListParam pList) {
   	if (pList == null)
    		return;
    	MOO1GameOptions source = loadOptions(fileName);
    	
    	if (pList == RotPUI.mainOptionsUI().activeList()) {
           	for (IParam param : pList) {
           		if (param != null) {
           			param.copyOption(source, this, true); // update tool
           		}
           	}
    	}
    	else {
	       	for (IParam param : pList) {
	       		if (param != null) {
		       		if (param.isCfgFile()) { // Exclude .cfg parameters
		       			param.updateOptionTool();
		       		}
		       		else {
		       			param.copyOption(source, this, true); // Copy and update tool
		       		}
	       		}
	       	}
    	}
       	if (!Rotp.noOptions) // Better safe than sorry
       		for (IParam param : allModOptions())
        		param.initDependencies(IParam.VALID_DEPENDENCIES);

        source.copyPanelBaseSettings(this, pList);
    }
    @Override public void prepareToSave(boolean secure) {
    	// probably not necessary! but it's called during screen swap and won't delay the game
    	//System.out.println("prepareToSave() " + optionName());
    	for (IParam param : allModOptions()) {
    		if (param != null) {
    			param.prepareToSave(this);
    		}
    	}
    	if (secure) {
    		// No computer info in game files... Folder path may contains player name!
    		dynOpts().setString(saveDirectory.getLangLabel(), "");
    		dynOpts().setString(bitmapGalaxyLastFolder().getLangLabel(), "");
    	}
    }
    @Override public void UpdateOptionsTools() {
    	// probably overkill, but no needs to be picky
    	//System.out.println("UpdateOptionsTools() " + optionName());
    	for (IParam param : allModOptions()) {
    		if (param != null && !param.isCfgFile()) { // cfg file if updated live!
    			param.updateOptionTool();
    		}
    	}
    	UserPreferences.load();
    }
    @Override public MOO1GameOptions copyAllOptions() {
		try {
			MOO1GameOptions opts = copyObjectData();
//			opts.setGalaxyShape(); 
//			opts.selectedGalaxyShapeOption1 = selectedGalaxyShapeOption1;
//			opts.selectedGalaxyShapeOption2 = selectedGalaxyShapeOption2;
			return opts;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
    // ========== New Options Static files management methods ==========
    //
    // !!! Must remain static: Called before option are fully initialized.
    //
    public static void copyOptionsFromLiveToLast() {
    	// No dependencies update yet... This will be done later
    	MOO1GameOptions live = loadOptions(LIVE_OPTIONS_FILE);
    	saveOptions(live, Rotp.jarPath(), LAST_OPTIONS_FILE);
    }
    private static MOO1GameOptions loadOptions(String fileName) {
    	MOO1GameOptions dest = loadOptions(Rotp.jarPath(), fileName);
   		return dest;
    }
    // BR: save options to zip file
    private static void saveOptions(MOO1GameOptions options, String fileName) {
    	saveOptions(options, Rotp.jarPath(), fileName);
    }
    private static void saveOptions(MOO1GameOptions options, String path, String fileName) {
		File saveFile = new File(path, fileName);
		try {
			saveOptionsTE(options, saveFile);
		} catch (IOException ex) {
			ex.printStackTrace();
           	System.err.println("Options.save -- IOException: "+ ex.toString());
		}    		
    }
    // BR: save options to zip  file
    private static void saveOptionsTE(MOO1GameOptions options, File saveFile) throws IOException {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(saveFile));
        ZipEntry e = new ZipEntry("GameOptions.dat");
        out.putNextEntry(e);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream objOut = null;
        try {
            objOut = new ObjectOutputStream(bos);
            objOut.writeObject(options);
            objOut.flush();
            byte[] data = bos.toByteArray();
            out.write(data, 0, data.length);
        }
        finally {
            try {
            bos.close();
            out.close();
            }
            catch(IOException ex) {
    			ex.printStackTrace();
            	System.err.println("Options.save -- IOException: "+ ex.toString());
            }            
        }
    }
    // BR: Options files initialization
    private static MOO1GameOptions initMissingOptionFile(String path, String fileName) {
    	if (beepsOnError)
    		Toolkit.getDefaultToolkit().beep();
		MOO1GameOptions newOptions = new MOO1GameOptions();
		newOptions.prepareToSave(false);
    	saveOptions(new MOO1GameOptions(), path, fileName);			
		return newOptions;    	
    }
    // BR: Load options from file
    private static MOO1GameOptions loadOptions(String path, String fileName) {
       	MOO1GameOptions newOptions;
		File loadFile = new File(path, fileName);
		if (loadFile.exists()) {
			newOptions = loadOptionsTE(loadFile);
            if (newOptions == null) {
            	System.err.println("Bad option version: " + loadFile.getAbsolutePath());
            	newOptions = initMissingOptionFile(path, fileName);
            }
    	} else {
			System.err.println("File not found: " + loadFile.getAbsolutePath());
			newOptions = initMissingOptionFile(path, fileName);
		}
		return newOptions;
    }
    // BR: Load options from file
    private static MOO1GameOptions loadOptionsTE(File saveFile) {
       	MOO1GameOptions newOptions;
    	try (ZipFile zipFile = new ZipFile(saveFile)) {
            ZipEntry ze = zipFile.entries().nextElement();
            InputStream zis = zipFile.getInputStream(ze);
            newOptions = loadObjectData(zis);
        }
        catch(IOException e) {
        	System.err.println("Bad option version " + saveFile.getAbsolutePath());
        	newOptions = null;
        }
		return newOptions;
    }
    private static MOO1GameOptions loadObjectData(InputStream is) {
        try {
        	MOO1GameOptions newOptions;
            try (InputStream buffer = new BufferedInputStream(is)) {
                ObjectInput input = new ObjectInputStream(buffer);
                newOptions = (MOO1GameOptions) input.readObject();
            }
            if (newOptions.specificOpponentCROption == null) {
            	newOptions.specificOpponentCROption = new String[MAX_OPPONENTS+1];
                String defVal = SpecificCROption.defaultSpecificValue().value;
                for (int i=0;i<newOptions.specificOpponentCROption.length;i++)
                	newOptions.specificOpponentCROption[i] = defVal;
            }
            return newOptions;
        }
        catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }
    private MOO1GameOptions copyObjectData() throws Exception {
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try {
			ByteArrayOutputStream bos =  new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			// serialize and pass the object
			oos.writeObject(this);
			oos.flush();
			ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
			ois = new ObjectInputStream(bin);
			// return the new object
			return (MOO1GameOptions) ois.readObject();
		}
		catch(Exception e) {
			System.out.println("Exception in ObjectCloner = " + e);
			throw(e);
		}
		finally {
			oos.close();
			ois.close();
		}
    }
}
