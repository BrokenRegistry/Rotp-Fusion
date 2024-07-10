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
import java.util.ArrayList;
import java.util.List;

import rotp.model.ai.AIEntry;
import rotp.model.ai.AIList;
import rotp.model.empires.Empire;
import rotp.model.empires.Race;
import rotp.model.events.RandomEvent;
import rotp.model.galaxy.GalaxyShape;
import rotp.model.galaxy.StarSystem;
import rotp.model.planet.Planet;
import rotp.model.tech.TechEngineWarp;
import rotp.ui.game.SetupGalaxyUI;

public interface IGameOptions extends IModOptions {
    public static final int MAX_OPPONENTS = SetupGalaxyUI.MAX_DISPLAY_OPPS;
    public static final int MAX_OPPONENT_TYPE = 5;
//    public static final String SIZE_DYNAMIC = "SETUP_GALAXY_SIZE_DYNAMIC";
//    public static final String SIZE_MICRO = "SETUP_GALAXY_SIZE_MICRO";
//    public static final String SIZE_TINY = "SETUP_GALAXY_SIZE_TINY";
//    public static final String SIZE_SMALL = "SETUP_GALAXY_SIZE_SMALL";
//    public static final String SIZE_SMALL2 = "SETUP_GALAXY_SIZE_SMALL2";
//    public static final String SIZE_MEDIUM = "SETUP_GALAXY_SIZE_AVERAGE";
//    public static final String SIZE_MEDIUM2 = "SETUP_GALAXY_SIZE_AVERAGE2";
//    public static final String SIZE_LARGE = "SETUP_GALAXY_SIZE_LARGE";
//    public static final String SIZE_LARGE2 = "SETUP_GALAXY_SIZE_LARGE2";
//    public static final String SIZE_HUGE = "SETUP_GALAXY_SIZE_HUGE";
//    public static final String SIZE_HUGE2 = "SETUP_GALAXY_SIZE_HUGE2";
//    public static final String SIZE_MASSIVE = "SETUP_GALAXY_SIZE_MASSIVE";
//    public static final String SIZE_MASSIVE2 = "SETUP_GALAXY_SIZE_MASSIVE2";
//    public static final String SIZE_MASSIVE3 = "SETUP_GALAXY_SIZE_MASSIVE3";
//    public static final String SIZE_MASSIVE4 = "SETUP_GALAXY_SIZE_MASSIVE4";
//    public static final String SIZE_MASSIVE5 = "SETUP_GALAXY_SIZE_MASSIVE5";
//    public static final String SIZE_INSANE = "SETUP_GALAXY_SIZE_INSANE";
//    public static final String SIZE_LUDICROUS = "SETUP_GALAXY_SIZE_LUDICROUS";
//    public static final String SIZE_MAXIMUM = "SETUP_GALAXY_SIZE_MAXIMUM";
//
//    public static final String SHAPE_RECTANGLE = "SETUP_GALAXY_SHAPE_RECTANGLE";
//    public static final String SHAPE_ELLIPTICAL = "SETUP_GALAXY_SHAPE_ELLIPSE";
//    public static final String SHAPE_SPIRAL = "SETUP_GALAXY_SHAPE_SPIRAL";
//    // modnar: add new map shapes
//    public static final String SHAPE_TEXT = "SETUP_GALAXY_SHAPE_TEXT";
//    public static final String SHAPE_LORENZ = "SETUP_GALAXY_SHAPE_LORENZ";
//    public static final String SHAPE_FRACTAL = "SETUP_GALAXY_SHAPE_FRACTAL";
//    public static final String SHAPE_MAZE = "SETUP_GALAXY_SHAPE_MAZE";
//    public static final String SHAPE_SHURIKEN = "SETUP_GALAXY_SHAPE_SHURIKEN";
//    public static final String SHAPE_BULLSEYE = "SETUP_GALAXY_SHAPE_BULLSEYE";
//    public static final String SHAPE_GRID = "SETUP_GALAXY_SHAPE_GRID";
//    public static final String SHAPE_CLUSTER = "SETUP_GALAXY_SHAPE_CLUSTER";
//    public static final String SHAPE_SWIRLCLUSTERS = "SETUP_GALAXY_SHAPE_SWIRLCLUSTERS";
//    public static final String SHAPE_SPIRALARMS = "SETUP_GALAXY_SHAPE_SPIRALARMS";
//    public static final String SHAPE_BITMAP     = "SETUP_GALAXY_SHAPE_BITMAP";
//    public static final String SHAPE_RANDOM     = "SETUP_GALAXY_SHAPE_RANDOM";
//    public static final String SHAPE_RANDOM_2   = "SETUP_GALAXY_SHAPE_RANDOM_2";

    public static final String DIFFICULTY_EASIEST = "SETUP_DIFFICULTY_EASIEST";
    public static final String DIFFICULTY_EASIER  = "SETUP_DIFFICULTY_EASIER";
    public static final String DIFFICULTY_EASY    = "SETUP_DIFFICULTY_EASY";
    public static final String DIFFICULTY_NORMAL  = "SETUP_DIFFICULTY_NORMAL";
    public static final String DIFFICULTY_HARD    = "SETUP_DIFFICULTY_HARD";
    public static final String DIFFICULTY_HARDER  = "SETUP_DIFFICULTY_HARDER";
    public static final String DIFFICULTY_HARDEST = "SETUP_DIFFICULTY_HARDEST";
    // modnar: add custom difficulty level option, set in Remnants.cfg
    public static final String DIFFICULTY_CUSTOM  = "SETUP_DIFFICULTY_CUSTOM";

    public static final String RESEARCH_NORMAL    = "SETUP_RESEARCH_RATE_NORMAL";
    // modnar: add fast research option
    public static final String RESEARCH_FAST      = "SETUP_RESEARCH_RATE_FAST";
    public static final String RESEARCH_SLOW      = "SETUP_RESEARCH_RATE_SLOW";
    public static final String RESEARCH_SLOWER    = "SETUP_RESEARCH_RATE_SLOWER";
    // BR: rearranged the list to fit the definition... Yes it looks strange
    public static final String RESEARCH_LETHARGIC = "SETUP_RESEARCH_RATE_SLOWEST"; // for backward compatibility
    public static final String RESEARCH_CRAWLING  = "SETUP_RESEARCH_RATE_IMPEDED";
    public static final String RESEARCH_IMPEDED   = "SETUP_RESEARCH_RATE_LETHARGIC";
    
    public static float[] slowFactors (float src) {
    	// convert old formula factor to new formula factors
    	double r2 = Math.log10(src)/10 + 1.2; // rate for tech 2
    	double a  = (Math.sqrt(50*src)-r2) / (Math.sqrt(50)-Math.sqrt(2)-4.0/5.0);
    	double b  = a/0.6;
    	double c  = a * Math.sqrt(2) + b/2 - r2;
    	float[] factors = new float[4];
    	factors[0] = src;
    	factors[1] = (float) a;
    	factors[2] = (float) b;
    	factors[3] = (float) c;
    	return factors;
    }
    public static final float[] R_PARAM_FAST      = new float[] {0.005392f, 1f, 2f, 0.5f};
    public static final float[] R_PARAM_NORMAL    = new float[] {1f/50, 1f};
    public static final float[] R_PARAM_SLOW      = slowFactors(1f/3); // from old formula factor
    public static final float[] R_PARAM_SLOWER    = slowFactors(1f);
    public static final float[] R_PARAM_LETHARGIC = slowFactors(5f);
    public static final float[] R_PARAM_CRAWLING  = slowFactors(25f);
    public static final float[] R_PARAM_IMPEDED   = slowFactors(125f);

    default float oldSlowFactor() {
    	switch(selectedResearchRate()) {
	    	case RESEARCH_FAST:
	    	case RESEARCH_NORMAL:
    			return 0f;
	    	case RESEARCH_SLOW:
	    		return R_PARAM_SLOW[0];
	    	case RESEARCH_SLOWER:
	    		return R_PARAM_SLOWER[0];
	    	case RESEARCH_LETHARGIC:
	    		return R_PARAM_LETHARGIC[0];
	    	case RESEARCH_CRAWLING:
	    		return R_PARAM_CRAWLING[0];
	    	case RESEARCH_IMPEDED:
	    		return R_PARAM_IMPEDED[0];
    		default:
    			return 1f;
    	}
    }
    
    public static final String TECH_TRADING_YES        = "SETUP_TECH_TRADING_YES";
    public static final String TECH_TRADING_ALLIES     = "SETUP_TECH_TRADING_ALLIES";
    public static final String TECH_TRADING_ALLIES_AID = "SETUP_TECH_TRADING_ALLIES_AID";
    public static final String TECH_TRADING_NO         = "SETUP_TECH_TRADING_NO";
    public static final String TECH_TRADING_NO_AID     = "SETUP_TECH_TRADING_NO_AID";

    public static final String GALAXY_AGE_NORMAL = "SETUP_GALAXY_AGE_NORMAL";
    public static final String GALAXY_AGE_YOUNG  = "SETUP_GALAXY_AGE_YOUNG";
    public static final String GALAXY_AGE_OLD    = "SETUP_GALAXY_AGE_OLD";

    public static final String RANDOM_EVENTS_ON  = "SETUP_RANDOM_EVENTS_ON";
    public static final String RANDOM_EVENTS_OFF = "SETUP_RANDOM_EVENTS_OFF";
    public static final String RANDOM_EVENTS_NO_MONSTERS = "SETUP_RANDOM_EVENTS_NO_MONSTERS";
    public static final String RANDOM_EVENTS_TECH_MONSTERS = "SETUP_RANDOM_EVENTS_TECH_MONSTERS";
    public static final String RANDOM_EVENTS_ONLY_MONSTERS = "SETUP_RANDOM_EVENTS_ONLY_MONSTERS";

    public static final String WARP_SPEED_NORMAL = "SETUP_WARP_SPEED_NORMAL";
    public static final String WARP_SPEED_FAST   = "SETUP_WARP_SPEED_FAST";
    public static final String WARP_SPEED_FAST_F = "SETUP_WARP_SPEED_FAST2";

    public static final String NEBULAE_NONE      = "SETUP_NEBULA_NONE";
    public static final String NEBULAE_RARE      = "SETUP_NEBULA_RARE";
    public static final String NEBULAE_UNCOMMON  = "SETUP_NEBULA_UNCOMMON";
    public static final String NEBULAE_NORMAL    = "SETUP_NEBULA_NORMAL";
    public static final String NEBULAE_COMMON    = "SETUP_NEBULA_COMMON";
    public static final String NEBULAE_FREQUENT  = "SETUP_NEBULA_FREQUENT";

    public static final String COUNCIL_IMMEDIATE = "SETUP_COUNCIL_IMMEDIATE";
    public static final String COUNCIL_REBELS    = "SETUP_COUNCIL_REBELS";
    public static final String COUNCIL_NONE      = "SETUP_COUNCIL_NONE";
    public static final String COUNCIL_NO_ALLIANCES  = "SETUP_COUNCIL_NO_ALLIANCES";
    public static final String COUNCIL_REALMS_BEYOND = "SETUP_COUNCIL_REALMS_BEYOND";

    // modnar: change PLANET_QUALITY settings, add larger and richer
    public static final String PLANET_QUALITY_LARGER = "SETUP_PLANET_QUALITY_LARGER";
    public static final String PLANET_QUALITY_RICHER = "SETUP_PLANET_QUALITY_RICHER";
    public static final String PLANET_QUALITY_HELL   = "SETUP_PLANET_QUALITY_HELL";
    public static final String PLANET_QUALITY_POOR   = "SETUP_PLANET_QUALITY_POOR";
    public static final String PLANET_QUALITY_MEDIOCRE  = "SETUP_PLANET_QUALITY_MEDIOCRE";
    public static final String PLANET_QUALITY_NORMAL = "SETUP_PLANET_QUALITY_NORMAL";
    public static final String PLANET_QUALITY_GOOD   = "SETUP_PLANET_QUALITY_GOOD";
    public static final String PLANET_QUALITY_GREAT  = "SETUP_PLANET_QUALITY_GREAT";
    public static final String PLANET_QUALITY_HEAVEN = "SETUP_PLANET_QUALITY_HEAVEN";

    public static final String TERRAFORMING_NORMAL   = "SETUP_TERRAFORMING_NORMAL";
    public static final String TERRAFORMING_REDUCED  = "SETUP_TERRAFORMING_REDUCED";
    public static final String TERRAFORMING_NONE     = "SETUP_TERRAFORMING_NONE";

    public static final String COLONIZING_NORMAL      = "SETUP_COLONIZING_NORMAL";
    public static final String COLONIZING_RESTRICTED  = "SETUP_COLONIZING_RESTRICTED";

    public static final String FUEL_RANGE_CUT      = "SETUP_FUEL_RANGE_CUT";
    public static final String FUEL_RANGE_LOW      = "SETUP_FUEL_RANGE_LOW";
    public static final String FUEL_RANGE_NORMAL   = "SETUP_FUEL_RANGE_NORMAL";
    public static final String FUEL_RANGE_HIGH     = "SETUP_FUEL_RANGE_HIGH";
    public static final String FUEL_RANGE_HIGHER   = "SETUP_FUEL_RANGE_HIGHER";
    public static final String FUEL_RANGE_HIGHEST  = "SETUP_FUEL_RANGE_HIGHEST";

    public static final String RANDOMIZE_AI_NONE        = "SETUP_RANDOMIZE_AI_NONE";
    public static final String RANDOMIZE_AI_PERSONALITY = "SETUP_RANDOMIZE_AI_PERSONALITY";
    public static final String RANDOMIZE_AI_ABILITY     = "SETUP_RANDOMIZE_AI_ABILITY";
    public static final String RANDOMIZE_AI_BOTH        = "SETUP_RANDOMIZE_AI_BOTH";

    public static final String AI_HOSTILITY_LOWEST   = "SETUP_AI_HOSTILITY_LOWEST";
    public static final String AI_HOSTILITY_LOWER    = "SETUP_AI_HOSTILITY_LOWER";
    public static final String AI_HOSTILITY_LOW      = "SETUP_AI_HOSTILITY_LOW";
    public static final String AI_HOSTILITY_NORMAL   = "SETUP_AI_HOSTILITY_NORMAL";
    public static final String AI_HOSTILITY_HIGH     = "SETUP_AI_HOSTILITY_HIGH";
    public static final String AI_HOSTILITY_HIGHER   = "SETUP_AI_HOSTILITY_HIGHER";
    public static final String AI_HOSTILITY_HIGHEST  = "SETUP_AI_HOSTILITY_HIGHEST";

    public static final String OPPONENT_AI_BASE       = "SETUP_OPPONENT_AI_BASE";
    public static final String OPPONENT_AI_MODNAR     = "SETUP_OPPONENT_AI_MODNAR";
    public static final String OPPONENT_AI_ROOKIE     = "SETUP_OPPONENT_AI_ROOKIE";
    public static final String OPPONENT_AI_XILMI      = "SETUP_OPPONENT_AI_XILMI";
    public static final String OPPONENT_AI_HYBRID     = "SETUP_OPPONENT_AI_HYBRID";
    public static final String OPPONENT_AI_CRUEL      = "SETUP_OPPONENT_AI_CRUEL";
    public static final String OPPONENT_AI_FUN        = "SETUP_OPPONENT_AI_FUN";
    public static final String OPPONENT_AI_PERSONALITY= "SETUP_OPPONENT_AI_PERSONALITY";
    public static final String OPPONENT_AI_RANDOM     = "SETUP_OPPONENT_AI_RANDOM";
    public static final String OPPONENT_AI_RANDOM_BASIC="SETUP_OPPONENT_AI_RANDOM_BASIC";
    public static final String OPPONENT_AI_RANDOM_ADV = "SETUP_OPPONENT_AI_RANDOM_ADV";
    public static final String OPPONENT_AI_RANDOM_NOBAR="SETUP_OPPONENT_AI_RANDOM_NOBAR";
    public static final String OPPONENT_AI_SELECTABLE = "SETUP_OPPONENT_AI_SELECT";

    public static final String AUTOPLAY_OFF           = "SETUP_AUTOPLAY_OFF";
    public static final String AUTOPLAY_AI_BASE       = "SETUP_OPPONENT_AI_BASE";
    public static final String AUTOPLAY_AI_MODNAR     = "SETUP_OPPONENT_AI_MODNAR";
    public static final String AUTOPLAY_AI_ROOKIE     = "SETUP_OPPONENT_AI_ROOKIE";
    public static final String AUTOPLAY_AI_XILMI      = "SETUP_OPPONENT_AI_XILMI";
    public static final String AUTOPLAY_AI_HYBRID     = "SETUP_OPPONENT_AI_HYBRID";
    public static final String AUTOPLAY_AI_CRUEL      = "SETUP_OPPONENT_AI_CRUEL";
    public static final String AUTOPLAY_AI_FUN        = "SETUP_OPPONENT_AI_FUN";
    public static final String AUTOPLAY_AI_PERSONALITY= "SETUP_OPPONENT_AI_PERSONALITY";
    public static final String AUTOPLAY_AI_RANDOM     = "SETUP_OPPONENT_AI_RANDOM";
    public static final String AUTOPLAY_AI_RANDOM_BASIC="SETUP_OPPONENT_AI_RANDOM_BASIC";
    public static final String AUTOPLAY_AI_RANDOM_ADV = "SETUP_OPPONENT_AI_RANDOM_ADV";
    public static final String AUTOPLAY_AI_RANDOM_NOBAR="SETUP_OPPONENT_AI_RANDOM_NOBAR";
    // AI encoded values; This list can be extended without being sorted!
    public static final int BASE		= 0;	// Base
    public static final int MODNAR		= 1;	// MODNAR
    public static final int ROOKIE		= 2;	// ROOkie
    public static final int XILMI		= 3;	// Roleplay
    public static final int HYBRID		= 4;	// Hybrid
    public static final int FUSION		= 5;	// Legacy
    public static final int FUN			= 6;	// Fun
    public static final int PERSONALITY	= 7;	// Personality
    public static final int RANDOM			= 8;
    public static final int RANDOM_BASIC	= 9;
    public static final int RANDOM_ADVANCED	= 10;
    public static final int RANDOM_NO_RELATIONBAR = 11;
    // Base AI Entries
    AIEntry selectableAI  = new AIEntry(-1,						OPPONENT_AI_SELECTABLE,		OPPONENT_AI_SELECTABLE);
    AIEntry offAI		  = new AIEntry(FUSION,					AUTOPLAY_OFF,				AUTOPLAY_OFF);
    AIEntry baseAI		  = new AIEntry(BASE,					AUTOPLAY_AI_BASE,			OPPONENT_AI_BASE);
    AIEntry modnarAI	  = new AIEntry(MODNAR,					AUTOPLAY_AI_MODNAR,			OPPONENT_AI_MODNAR);
    AIEntry rookieAI	  = new AIEntry(ROOKIE,					AUTOPLAY_AI_ROOKIE,			OPPONENT_AI_ROOKIE);
    AIEntry xilmiAI		  = new AIEntry(XILMI, 					AUTOPLAY_AI_XILMI,			OPPONENT_AI_XILMI);
    AIEntry hybridAI	  = new AIEntry(HYBRID,					AUTOPLAY_AI_HYBRID,			OPPONENT_AI_HYBRID);
    AIEntry fusionAI	  = new AIEntry(FUSION, 				AUTOPLAY_AI_CRUEL,			OPPONENT_AI_CRUEL);
    AIEntry funAI		  = new AIEntry(FUN,					AUTOPLAY_AI_FUN,			OPPONENT_AI_FUN);
    AIEntry personalityAI = new AIEntry(PERSONALITY,			AUTOPLAY_AI_PERSONALITY,	OPPONENT_AI_PERSONALITY);
    AIEntry randomAI	  = new AIEntry(RANDOM,					AUTOPLAY_AI_RANDOM,			OPPONENT_AI_RANDOM);
    AIEntry randomBasicAI = new AIEntry(RANDOM_BASIC,			AUTOPLAY_AI_RANDOM_BASIC,	OPPONENT_AI_RANDOM_BASIC);
    AIEntry randomAdvAI	  = new AIEntry(RANDOM_ADVANCED,		AUTOPLAY_AI_RANDOM_ADV,		OPPONENT_AI_RANDOM_ADV);
    AIEntry randomNoBarAI = new AIEntry(RANDOM_NO_RELATIONBAR,	AUTOPLAY_AI_RANDOM_NOBAR,	OPPONENT_AI_RANDOM_NOBAR);
    AIEntry defaultAI 	  = xilmiAI;
    // AI SubSet Builder: These sequences can be changed to fit GUI requirements
    public static AIList optionalAIset() {
    	AIList list = new AIList();
    	if (!showAllAI.get())
    		return list;
    	list.add(baseAI);
    	list.add(modnarAI);
    	return list;
    };
    public static AIList baseAIset() {
    	AIList list = optionalAIset();
    	list.add(rookieAI);
    	return list;
    }
    public static AIList advancedAIset() {
    	AIList list = new AIList();
     	list.add(xilmiAI);
     	list.add(hybridAI);
     	list.add(personalityAI);
     	list.add(funAI);
    	list.add(fusionAI);
    	return list;
    }
    public static AIList mandatoryAIset() {
    	AIList list = new AIList();
    	list.add(rookieAI);
     	list.addAll(advancedAIset());
    	return list;
    }
    public static AIList noRelationBarAIset() {
    	AIList list = new AIList();
    	list.add(fusionAI);
     	list.add(funAI);
     	list.add(personalityAI);
    	return list;
    }
    public static AIList randomAIset() {
    	AIList list = new AIList();
     	list.add(randomAI);
     	list.add(randomBasicAI);
     	list.add(randomAdvAI);
     	list.add(randomNoBarAI);
    	return list;
    }

    // AI Subset Getter: These sequences can be changed to fit GUI requirements
    public static AIList allAIset() {
    	AIList list = optionalAIset();
     	list.addAll(mandatoryAIset());
    	return list;
    }
    public static AIList changePlayAIset() {
    	AIList list = new AIList();
    	list.add(offAI);
    	list.addAll(optionalAIset());
    	list.addAll(mandatoryAIset());
    	return list;
    }
    public static AIList autoPlayAIset() {
    	AIList list = changePlayAIset();
    	list.addAll(randomAIset());
    	return list;
    }
    public static AIList changeAlienAIset() {
    	AIList list = optionalAIset();
    	list.addAll(mandatoryAIset());
    	return list;
    }
    public static AIList specificAIset() {
    	AIList list = changeAlienAIset();
    	list.addAll(randomAIset());
    	return list;
    }
    public static AIList globalAIset() {
    	AIList list = specificAIset();
    	list.add(selectableAI);
    	return list;
    }
    public default boolean isAutoPlay()           { return !selectedAutoplayOption().equals(AUTOPLAY_OFF); }
	public default boolean autoRunAILocked()      { return debugAutoRun() && !isAutoPlay(); }
    //public default boolean communityAI()        { return false; }
    public default boolean selectableAI()         { return selectedOpponentAIOption().equals(OPPONENT_AI_SELECTABLE); }
    //public default boolean usingExtendedRaces() { return (selectedNumberOpponents()+1) > startingRaceOptions().size(); }
    //public default void communityAI(boolean b)  { }
    public default int maxOpponents()             { return MAX_OPPONENTS; }
    public default float hostileTerraformingPct() { return 1.0f; }
    public default boolean restrictedColonization() { return selectedColonizingOption().equals(COLONIZING_RESTRICTED); }
    public default int baseAIRelationsAdj()       { return 0; }
    public default int selectedAI(Empire e)       { return defaultAI.id; }
    public default boolean randomizeAIPersonality()  {
        switch (selectedRandomizeAIOption()) {
            case RANDOMIZE_AI_PERSONALITY:
            case RANDOMIZE_AI_BOTH:
                return true;
            default:
                return false;
        }
    }
    public default boolean randomizeAIAbility()  {
        switch (selectedRandomizeAIOption()) {
            case RANDOMIZE_AI_ABILITY:
            case RANDOMIZE_AI_BOTH:
                return true;
            default:
                return false;
        }
    }
    public String name();
    public void setAdvancedOptionsToDefault();

    public int numberStarSystems();
	public int numberStarSystems(String size);
    public int numberNebula();
    public default float nebulaSizeMult()                { return 1.0f; }
    public List<Integer> possibleColors();
    public float researchCostBase(int techLevel);
    public boolean canTradeTechs(Empire e1, Empire e2);
    public boolean canOfferTechs(Empire e1, Empire e2);
    public int warpSpeed(TechEngineWarp tech);
    public boolean allowRandomEvent(RandomEvent ev);
    public String randomStarType();
    public String randomPlayerStarType(Race r);
    public String randomRaceStarType(Race r);
    public String randomOrionStarType();
    public Planet randomPlanet(StarSystem s);
    public Planet randomPlayerPlanet(Race r, StarSystem s);
    public Planet orionPlanet(StarSystem s);
    public void randomizeColors();
    public GalaxyShape galaxyShape();
    public void setAndGenerateGalaxy();
    public boolean isRandomGalaxy();

    public int numColors();
    public Color color(int i);

    // selectable options
    // public List<String> galaxySizeOptions(); // BR:Moved to IGalaxyOptions
    // public List<String> galaxyShapeOptions();
    public List<String> galaxyShapeOptions1();
    public List<String> galaxyShapeOptions2();
    public List<String> galaxyAgeOptions();
    public List<String> researchRateOptions();
    public List<String> techTradingOptions();
    public List<String> randomEventOptions();
    public List<String> warpSpeedOptions();
    public List<String> nebulaeOptions();
    public List<String> councilWinOptions();
    public List<String> starDensityOptions();
    public List<String> aiHostilityOptions();
    public List<String> planetQualityOptions();
    public List<String> terraformingOptions();
    public List<String> colonizingOptions();
    public List<String> fuelRangeOptions();
    public List<String> randomizeAIOptions();
    public List<String> autoplayOptions();
    public List<String> opponentAIOptions();
    public List<String> specificOpponentAIOptions();

    public List<String> gameDifficultyOptions();
    public int maximumOpponentsOptions();
    public int defaultOpponentsOptions();
    public List<String> startingRaceOptions();
    public List<String> newRaceOffOptions();

    public String selectedGalaxySize();
    public void selectedGalaxySize(String s);
    public String selectedGalaxyShape();
    public void selectedGalaxyShape(String s);
    public String selectedGalaxyAge();
    public void selectedGalaxyAge(String s);
    public String selectedResearchRate();
    public void selectedResearchRate(String s);
    public String selectedTechTradeOption();
    public void selectedTechTradeOption(String s);
    public String selectedRandomEventOption();
    default boolean disabledRandomEvents() {
    	return selectedRandomEventOption().equals(RANDOM_EVENTS_OFF);
    };
    default boolean techRandomEvents() {
    	String sre = selectedRandomEventOption();
    	return sre.equals(RANDOM_EVENTS_TECH_MONSTERS)
    			|| sre.equals(RANDOM_EVENTS_ONLY_MONSTERS);
    };
    public void selectedRandomEventOption(String s);
    public String selectedWarpSpeedOption();
    public void selectedWarpSpeedOption(String s);
    public String selectedNebulaeOption();
    public void selectedNebulaeOption(String s);
    public String selectedCouncilWinOption();
    public void selectedCouncilWinOption(String s);
    public String selectedStarDensityOption();
    public void selectedStarDensityOption(String s);
    public String selectedAIHostilityOption();
    public void selectedAIHostilityOption(String s);
    public String selectedPlanetQualityOption();
    public void selectedPlanetQualityOption(String s);
    public String selectedTerraformingOption();
    public void selectedTerraformingOption(String s);
    public String selectedColonizingOption();
    public void selectedColonizingOption(String s);
    public String selectedFuelRangeOption();
    public void selectedFuelRangeOption(String s);
    public String selectedRandomizeAIOption();
    public void selectedRandomizeAIOption(String s);
    public String selectedOpponentAIOption();
    public void selectedOpponentAIOption(String s);
    public String specificOpponentAIOption(int empId);
    public void specificOpponentAIOption(String s, int empId);
    public String selectedAutoplayOption();
    public void selectedAutoplayOption(String s);
    public String specificOpponentCROption(int empId);
    public void specificOpponentCROption(String s, int empId);

    public String selectedGalaxyShapeOption1();
    public void selectedGalaxyShapeOption1(String s);
    public String selectedGalaxyShapeOption2();
    public void selectedGalaxyShapeOption2(String s);

    public int numGalaxyShapeOption1();
    public int numGalaxyShapeOption2();

    public String selectedGameDifficulty();
    public void selectedGameDifficulty(String s);
    public int selectedNumberOpponents();
    public void selectedNumberOpponents(int i);

    public int numPlayers();
    public NewPlayer selectedPlayer();
    public String selectedPlayerRace();
    public void selectedPlayerRace(String s);
    public void setRandomPlayerRace(); // BR:
    public int selectedPlayerColor();
    public void selectedPlayerColor(int i);
    public String selectedLeaderName();
    public void selectedLeaderName(String s);
    public String selectedHomeWorldName();
    public void selectedHomeWorldName(String s);
    public String[] selectedOpponentRaces();
    public String selectedOpponentRace(int i);
    public void selectedOpponentRace(int i, String s);

    default void copyForRestart(IGameOptions opt) { } // BR: for Restart with new options
    //default void copyOptions(IGameOptions opt) { }
    default boolean immediateCouncilWin()    { return selectedCouncilWinOption().equals(COUNCIL_IMMEDIATE); }
    default boolean noGalacticCouncil()      { return selectedCouncilWinOption().equals(COUNCIL_NONE); }
    default boolean realmsBeyondCouncil()    { return selectedCouncilWinOption().equals(COUNCIL_REALMS_BEYOND); }
    default boolean noAllianceCouncil()      {
    	return realmsBeyondCouncil() || selectedCouncilWinOption().equals(COUNCIL_NO_ALLIANCES);
    }
    default float fuelRangeMultiplier() {
        switch(selectedFuelRangeOption()) { // BR: restored and added 2
            case FUEL_RANGE_CUT:    return 0.67f;
            case FUEL_RANGE_LOW:    return 0.8f;
            case FUEL_RANGE_NORMAL: return 1;
            case FUEL_RANGE_HIGH:   return 2; // original: return 2
            case FUEL_RANGE_HIGHER: return 3; // original: return 3
            case FUEL_RANGE_HIGHEST: return 5; // original: return 5
            default: return 1;
        }
    }
    default String nextOpponentAI() {
        List<String> opts = opponentAIOptions();
        int index = opts.indexOf(selectedOpponentAIOption())+1;
        return index >= opts.size() ? opts.get(0) : opts.get(index);
    }
    default String prevOpponentAI() {
        List<String> opts = opponentAIOptions();
        int index = opts.indexOf(selectedOpponentAIOption())-1;
        return index < 0 ? opts.get(opts.size()-1) : opts.get(index);
    }
    default void nextSpecificOpponentAI(int i) {
        List<String> allAIs = specificOpponentAIOptions();
        // BR: Add user filter
        String currAI = specificOpponentAIOption(i);

        // if currAI not on the list: index=-1 then result=0 -> OK
        int nextIndex = currAI == null ? 0 : allAIs.indexOf(currAI)+1;
        if (nextIndex >= allAIs.size())
            nextIndex = 0;

        String nextAI = allAIs.get(nextIndex);
        specificOpponentAIOption(nextAI, i);
    }
    default void prevSpecificOpponentAI(int i) {
        List<String> allAIs = specificOpponentAIOptions();
        String currAI = specificOpponentAIOption(i);

        // if currAI not on the list: index=-1 then result=0 -> OK
        int nextIndex = currAI == null ? 0 : allAIs.indexOf(currAI)-1;
        if (nextIndex < 0)
            nextIndex = allAIs.size()-1;

        String nextAI = allAIs.get(nextIndex);
        specificOpponentAIOption(nextAI, i);
    }
    default void nextOpponent(int i) {
        String player = selectedPlayerRace();
        // BR: Race filtration
        List<String> allOpps = getNewRacesOnOffList();
        String[] selectedOpps = selectedOpponentRaces();
        String currOpp = this.selectedOpponentRace(i);

        int nextIndex = currOpp == null ? 0 : allOpps.indexOf(currOpp)+1;
        if (nextIndex >= allOpps.size())
            nextIndex = -1;
        while (true) {
            String nextOpp = nextIndex < 0 ? null : allOpps.get(nextIndex);
            int count = (nextOpp != null) && nextOpp.equals(player) ? 1 : 0;
            for (String opp: selectedOpps) {
                if ((nextOpp != null) && nextOpp.equals(opp))
                    count++;
            }
            if (count < MAX_OPPONENT_TYPE) {
                selectedOpponentRace(i, nextOpp);
                return;
            }
            nextIndex++;
            if (nextIndex >= allOpps.size())
                nextIndex = -1;
        }
    }
    default void prevOpponent(int i) {
        String player = selectedPlayerRace();
        // BR: Race filtration
        // List<String> allOpps = startingRaceOptions();
        List<String> allOpps = getNewRacesOnOffList();
        String[] selectedOpps = selectedOpponentRaces();
        String currOpp = selectedOpponentRace(i);
        int lastIndex = allOpps.size()-1;

        int prevIndex = currOpp == null ? lastIndex : allOpps.indexOf(currOpp)-1;
        while (true) {
            String prevOpp = prevIndex < 0 ? null : allOpps.get(prevIndex);
            int count = (prevOpp != null) && prevOpp.equals(player) ? 1 : 0;
            for (String opp: selectedOpps) {
                if ((prevOpp != null) && prevOpp.equals(opp))
                    count++;
            }
            if (count < MAX_OPPONENT_TYPE) {
                selectedOpponentRace(i, prevOpp);
                return;
            }
            prevIndex--;
            if (prevIndex < -1)
                prevIndex = lastIndex;
        }
    }
    // modnar: change difficulty production modifiers
    // from 0.5, 0.75, 0.9, 1.0, 1.1, 1.4, 2.0
    // to   0.7, 0.85, 1.0, 1.2, 1.4, 1.7, 2.0 (smoother step-to-step increases between 1.0 to 2.0)
    // modnar: add custom difficulty level option, set in Remnants.cfg
    // UserPreferences.customDifficulty(), custom difficulty range: 20% to 500%
    default float aiProductionModifier() {
    	return aiProductionModifier(selectedGameDifficulty());
    }
    default float aiProductionModifier(String difficulty) {
        switch(difficulty) {
            case DIFFICULTY_EASIEST: return 0.55f;
            case DIFFICULTY_EASIER:  return 0.75f;
            case DIFFICULTY_EASY:    return 0.90f;
            case DIFFICULTY_NORMAL:  return 1.0f;
            case DIFFICULTY_HARD:    return 1.1f;
            case DIFFICULTY_HARDER:  return 1.25f;
            case DIFFICULTY_HARDEST: return 1.45f;
            case DIFFICULTY_CUSTOM:  return (float)(0.01f*selectedCustomDifficulty());
            default: return 1.0f;
        }
    }
    // modnar: change difficulty waste modifiers, with production changes above
    // modnar: if custom difficulty level option is set to less than 100%, also change waste modifiers
    default float aiWasteModifier() {
        switch(selectedGameDifficulty()) {
            case DIFFICULTY_EASIEST: return 0.55f;
            case DIFFICULTY_EASIER:  return 0.75f;
            case DIFFICULTY_EASY:    return 0.9f;
            case DIFFICULTY_CUSTOM:  return (float)(Math.min(1.0f, 0.01f*selectedCustomDifficulty()));
            default: return 1.0f;
        }
    }
    // modnar: change PLANET_QUALITY settings, set planet size bonus, 150% for LARGER, 80% for RICHER
    default float planetSizeBonus() {
        switch(selectedPlanetQualityOption()) {
            case PLANET_QUALITY_LARGER:   return 1.5f;
            case PLANET_QUALITY_RICHER:   return 0.8f;
            case PLANET_QUALITY_NORMAL:   return 1.0f;
            default: return 1.0f;
        }
    }
    default List<String> getNewRacesOnOffList() {
		if (showNewRaces().get()) {
			return allRaceOptions();
		}
		return baseRaceOptions();
    }

    // Was in MOO1GameOptions
    public static List<String> getAiHostilityOptions() {
        List<String> list = new ArrayList<>();
        list.add(AI_HOSTILITY_LOWEST);
        list.add(AI_HOSTILITY_LOWER);
        list.add(AI_HOSTILITY_LOW);
        list.add(AI_HOSTILITY_NORMAL);
        list.add(AI_HOSTILITY_HIGH);
        list.add(AI_HOSTILITY_HIGHER);
        list.add(AI_HOSTILITY_HIGHEST);
        return list;
    }
    public static List<String> getColonizingOptions() {
        List<String> list = new ArrayList<>();
        list.add(COLONIZING_NORMAL);
        list.add(COLONIZING_RESTRICTED);
        return list;
    }
    public static List<String> getCouncilWinOptions() {
        List<String> list = new ArrayList<>();
        list.add(COUNCIL_IMMEDIATE);
        list.add(COUNCIL_REBELS);
        list.add(COUNCIL_NONE);
        list.add(COUNCIL_NO_ALLIANCES);
        list.add(COUNCIL_REALMS_BEYOND);
        return list;
    }
    public static List<String> getFuelRangeOptions() { // BR: restored and added 2
        List<String> list = new ArrayList<>();
        list.add(FUEL_RANGE_CUT);
        list.add(FUEL_RANGE_LOW);
        list.add(FUEL_RANGE_NORMAL);
        list.add(FUEL_RANGE_HIGH);
        list.add(FUEL_RANGE_HIGHER);
        list.add(FUEL_RANGE_HIGHEST);
        return list;
    }
    public static List<String> getGalaxyAgeOptions() {
        List<String> list = new ArrayList<>();
        list.add(GALAXY_AGE_YOUNG);
        list.add(GALAXY_AGE_NORMAL);
        list.add(GALAXY_AGE_OLD);
        return list;
    }
    public static List<String> getNebulaeOptions() {
        List<String> list = new ArrayList<>();
        list.add(NEBULAE_NONE);
        list.add(NEBULAE_RARE);
        list.add(NEBULAE_UNCOMMON);
        list.add(NEBULAE_NORMAL);
        list.add(NEBULAE_COMMON);
        list.add(NEBULAE_FREQUENT);
        return list;
    }
    public static List<String> getPlanetQualityOptions() {
        List<String> list = new ArrayList<>();
        // modnar: change PLANET_QUALITY settings, add larger and richer, comment out poor to great settings
        // BR: Restored vanilla choices... They are not incompatible
        list.add(PLANET_QUALITY_HELL);
        list.add(PLANET_QUALITY_POOR);
        list.add(PLANET_QUALITY_MEDIOCRE);
        list.add(PLANET_QUALITY_NORMAL);
        list.add(PLANET_QUALITY_GOOD);
        list.add(PLANET_QUALITY_GREAT);
        list.add(PLANET_QUALITY_HEAVEN);
        list.add(PLANET_QUALITY_LARGER);
        list.add(PLANET_QUALITY_RICHER);
        return list;
    }
    public static List<String> getRandomEventOptions() {
        List<String> list = new ArrayList<>();
        list.add(RANDOM_EVENTS_ON);
        list.add(RANDOM_EVENTS_NO_MONSTERS);
        list.add(RANDOM_EVENTS_OFF);
        list.add(RANDOM_EVENTS_TECH_MONSTERS);
        list.add(RANDOM_EVENTS_ONLY_MONSTERS);
        return list;
    }
    public static List<String> getRandomizeAIOptions() {
        List<String> list = new ArrayList<>();
        list.add(RANDOMIZE_AI_NONE);
        list.add(RANDOMIZE_AI_PERSONALITY);
        list.add(RANDOMIZE_AI_ABILITY);
        list.add(RANDOMIZE_AI_BOTH);
        return list;
    }
    public static List<String> getResearchRateOptions() {
        List<String> list = new ArrayList<>();
        // modnar: add fast research option
        list.add(RESEARCH_FAST);
        list.add(RESEARCH_NORMAL);
        list.add(RESEARCH_SLOW);
        list.add(RESEARCH_SLOWER);
        // BR: add extremely slow research option
        list.add(RESEARCH_LETHARGIC); // former slowest compatible
        list.add(RESEARCH_CRAWLING);
        list.add(RESEARCH_IMPEDED);
        return list;
    }
    public static List<String> getTechTradingOptions() {
        List<String> list = new ArrayList<>();
        list.add(TECH_TRADING_YES);
        list.add(TECH_TRADING_ALLIES);
        list.add(TECH_TRADING_ALLIES_AID);
        list.add(TECH_TRADING_NO);
        list.add(TECH_TRADING_NO_AID);
        return list;
    }
    public static List<String> getTerraformingOptions() {
        List<String> list = new ArrayList<>();
        list.add(TERRAFORMING_NORMAL);
        list.add(TERRAFORMING_REDUCED);
        list.add(TERRAFORMING_NONE);
        return list;
    }
    public static List<String> getWarpSpeedOptions() {
        List<String> list = new ArrayList<>();
        list.add(WARP_SPEED_NORMAL);
        list.add(WARP_SPEED_FAST);
        list.add(WARP_SPEED_FAST_F);
        return list;
    }
    public static List<String> getGameDifficultyOptions() {
        List<String> list = new ArrayList<>();
        list.add(DIFFICULTY_EASIEST);
        list.add(DIFFICULTY_EASIER);
        list.add(DIFFICULTY_EASY);
        list.add(DIFFICULTY_NORMAL);
        list.add(DIFFICULTY_HARD);
        list.add(DIFFICULTY_HARDER);
        list.add(DIFFICULTY_HARDEST);
        // modnar: add custom difficulty level option, set in Remnants.cfg
        list.add(DIFFICULTY_CUSTOM);
        return list;
    }
}
