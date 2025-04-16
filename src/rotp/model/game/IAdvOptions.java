package rotp.model.game;

import static rotp.model.game.IGameOptions.AI_HOSTILITY_NORMAL;
import static rotp.model.game.IGameOptions.AUTOPLAY_OFF;
import static rotp.model.game.IGameOptions.COLONIZING_NORMAL;
import static rotp.model.game.IGameOptions.COUNCIL_IMMEDIATE;
import static rotp.model.game.IGameOptions.COUNCIL_REBELS;
import static rotp.model.game.IGameOptions.FUEL_RANGE_NORMAL;
import static rotp.model.game.IGameOptions.GALAXY_AGE_NORMAL;
import static rotp.model.game.IGameOptions.NEBULAE_NORMAL;
import static rotp.model.game.IGameOptions.PLANET_QUALITY_NORMAL;
import static rotp.model.game.IGameOptions.RANDOMIZE_AI_NONE;
import static rotp.model.game.IGameOptions.RANDOM_EVENTS_NO_MONSTERS;
import static rotp.model.game.IGameOptions.RESEARCH_NORMAL;
import static rotp.model.game.IGameOptions.TECH_TRADING_YES;
import static rotp.model.game.IGameOptions.TERRAFORMING_NORMAL;
import static rotp.model.game.IGameOptions.WARP_SPEED_NORMAL;
import static rotp.model.game.IGameOptions.getAiHostilityOptions;
import static rotp.model.game.IGameOptions.getColonizingOptions;
import static rotp.model.game.IGameOptions.getCouncilWinOptions;
import static rotp.model.game.IGameOptions.getFuelRangeOptions;
import static rotp.model.game.IGameOptions.getGalaxyAgeOptions;
import static rotp.model.game.IGameOptions.getNebulaeOptions;
import static rotp.model.game.IGameOptions.getPlanetQualityOptions;
import static rotp.model.game.IGameOptions.getRandomEventOptions;
import static rotp.model.game.IGameOptions.getRandomizeAIOptions;
import static rotp.model.game.IGameOptions.getResearchRateOptions;
import static rotp.model.game.IGameOptions.getTechTradingOptions;
import static rotp.model.game.IGameOptions.getTerraformingOptions;
import static rotp.model.game.IGameOptions.getWarpSpeedOptions;
import static rotp.model.game.ISystemsOptions.firstRingRadius;
import static rotp.model.game.ISystemsOptions.firstRingSystemNumber;
import static rotp.model.game.ISystemsOptions.radiusToNumStars;
import static rotp.model.game.ISystemsOptions.secondRingRadius;
import static rotp.model.game.ISystemsOptions.secondRingSystemNumber;
import static rotp.model.game.ISystemsOptions.surfaceSecurityFactor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import rotp.ui.options.AllSubUI;
import rotp.ui.util.LinkData;
import rotp.ui.util.LinkValue;
import rotp.ui.util.ParamList;

// Duplicates Options, Race Menu Options and Galaxy Options
//
public interface IAdvOptions extends IBaseOptsTools {

	String STAR_DENSITY_LONELY   = "SETUP_STAR_DENSITY_LONELY";
	String STAR_DENSITY_U_WIDE   = "SETUP_STAR_DENSITY_U_WIDE";
	String STAR_DENSITY_V_WIDE   = "SETUP_STAR_DENSITY_V_WIDE";
	String STAR_DENSITY_WIDER    = "SETUP_STAR_DENSITY_WIDER";
	String STAR_DENSITY_WIDE     = "SETUP_STAR_DENSITY_WIDE";
	String STAR_DENSITY_LOWEST   = "SETUP_STAR_DENSITY_LOWEST";
	String STAR_DENSITY_LOWER    = "SETUP_STAR_DENSITY_LOWER";
	String STAR_DENSITY_LOW      = "SETUP_STAR_DENSITY_LOW";
	String STAR_DENSITY_NORMAL   = "SETUP_STAR_DENSITY_NORMAL";
	String STAR_DENSITY_HIGH     = "SETUP_STAR_DENSITY_HIGH";
	String STAR_DENSITY_HIGHER   = "SETUP_STAR_DENSITY_HIGHER";
	String STAR_DENSITY_HIGHEST  = "SETUP_STAR_DENSITY_HIGHEST";

    public static List<String> getStarDensityOptions() {
        List<String> list = new ArrayList<>();
        list.add(STAR_DENSITY_LONELY);
        list.add(STAR_DENSITY_U_WIDE);
        list.add(STAR_DENSITY_V_WIDE);
        list.add(STAR_DENSITY_WIDER);
        list.add(STAR_DENSITY_WIDE);
        list.add(STAR_DENSITY_LOWEST);
        list.add(STAR_DENSITY_LOWER);
        list.add(STAR_DENSITY_LOW);
        list.add(STAR_DENSITY_NORMAL);
        list.add(STAR_DENSITY_HIGH);
        list.add(STAR_DENSITY_HIGHER);
        list.add(STAR_DENSITY_HIGHEST);
        return list;
    }
	static LinkedHashMap<String, Float> densityMap() {
		LinkedHashMap<String, Float> map = new LinkedHashMap<>();
	    map.put(STAR_DENSITY_LONELY,	4f);
	    map.put(STAR_DENSITY_U_WIDE,	3f);
	    map.put(STAR_DENSITY_V_WIDE,	2.3f);
	    map.put(STAR_DENSITY_WIDER,		1.8f);
	    map.put(STAR_DENSITY_WIDE,		1.5f);
	    map.put(STAR_DENSITY_LOWEST,	1.3f);
	    map.put(STAR_DENSITY_LOWER,		1.2f);
	    map.put(STAR_DENSITY_LOW,		1.1f);
	    map.put(STAR_DENSITY_NORMAL,	1f);
	    map.put(STAR_DENSITY_HIGH,		0.9f);
	    map.put(STAR_DENSITY_HIGHER,	0.8f);
	    map.put(STAR_DENSITY_HIGHEST,	0.7f);
		return map;
	}
	static List<String> getGalaxyDensityOptions(float maxDensity) {
		LinkedHashMap<String, Float> map = densityMap();
		List<String> list = new ArrayList<>();
		for (Entry<String, Float> entry : map.entrySet())
			if (maxDensity >= entry.getValue())
				list.add(entry.getKey());
		if (list.isEmpty())
			list.add(STAR_DENSITY_HIGHEST);
		return list;
	}
	static String getDensityKey(float minDensity) {
		LinkedHashMap<String, Float> map = densityMap();
		for (Entry<String, Float> entry : map.entrySet())
			if (minDensity >= entry.getValue())
				return entry.getKey();
		return null;
	}
	static float getDensitySizeFactor(String density)	{ return densityMap().get(density); }
	default float densitySizeFactor(String density)	{ return getDensitySizeFactor(density); }
	//float densitySizeFactor();
	default float systemBuffer(String density)	{  return 1.9f * getDensitySizeFactor(density); }

//	static float getSystemBuffer(String densityOption)	{ 
//		return 1.9f * getDensitySizeFactor(densityOption);
//	}

	// ==================== Duplicates for Base Advanced Options ====================
	//
	ParamList galaxyAge = new GalaxyAge(); // Duplicate Do not add the list
	class GalaxyAge extends ParamList {
		GalaxyAge() {
			super(ADV_UI, "GALAXY_AGE", getGalaxyAgeOptions(), GALAXY_AGE_NORMAL);
			isDuplicate(true);
			showFullGuide(true);
		}
		@Override public String getOptionValue(IGameOptions options) {
			return options.selectedGalaxyAge();
		}
		@Override public void setOptionValue(IGameOptions options, String newValue) {
			options.selectedGalaxyAge(newValue);
		}
	}
	
	default ParamList starDensity()	{ return starDensity; }
	ParamList starDensity = new StarDensity(); // Duplicate Do not add the list
	class StarDensity extends ParamList {
		StarDensity() {
			super(ADV_UI, "STAR_DENSITY", getStarDensityOptions(), STAR_DENSITY_NORMAL);
			isDuplicate(true);
			showFullGuide(true);
		}
		@Override public void initDependencies(int level)	{
			if (level == 0) {
				resetLinks();
				// Density numerical value = spreading
				// When spreading goes system number should go down.
				addLink(firstRingSystemNumber,  DO_LOCK, GO_UP, GO_DOWN, "Ring 1");
				addLink(secondRingSystemNumber, DO_LOCK, GO_UP, GO_DOWN, "Ring 2");
			}
			else {
				IGameOptions opts = opts();
				float density1 = density(opts.firstRingRadius(), opts.firstRingSystemNumber());
				float density2 = density(opts.secondRingRadius(), opts.secondRingSystemNumber());
				float maxDensity = Math.min(density1, density2);
				reInit(getGalaxyDensityOptions(maxDensity));
				boolean invalid = isInvalidLocalValue(get());
				if (invalid) {
					invalid = isInvalidLocalValue(get());
					if (invalid) {
						set(STAR_DENSITY_HIGHEST);
					}
				}
				super.initDependencies(level);
			}
       		}
		@Override public boolean isValidValue()	{ return isValidDoubleCheck(); }
		@Override protected void convertValueToLink(LinkData rec)	{
			// Convert the current state
			switch (rec.key) {
				case "Ring 1":
					rec.aimValue = new LinkValue(radiusToNumStars(firstRingRadius.get()));
					return;
				case "Ring 2":
					rec.aimValue = new LinkValue(radiusToNumStars(secondRingRadius.get()));
					return;
				default:
					super.convertValueToLink(rec);
			}
		}
		@Override protected Boolean getDirectionOfChange(String before, String after) {
			float valBefore	= getDensitySizeFactor(before);
			float valAfter	= getDensitySizeFactor(after);
			if (valAfter > valBefore)
				return GO_UP;
			if (valAfter < valBefore)
				return GO_DOWN;
			return null;
		}
		@Override public String getOptionValue(IGameOptions options) {
			return options.selectedStarDensityOption();
		}
		@Override public void setOptionValue(IGameOptions options, String newValue) {
			options.selectedStarDensityOption(newValue);
		}
		private float density(float radius, int NumStar)	{
			float systemBuffer = (float) (radius / (Math.sqrt(NumStar) * surfaceSecurityFactor));
			float density = systemBuffer / 1.9f;
			return density;
		}
	}

	ParamList nebulae = new Nebulae(); // Duplicate Do not add the list
	class Nebulae extends ParamList {
		Nebulae() {
			super(ADV_UI, "NEBULAE", getNebulaeOptions(), NEBULAE_NORMAL);
			isDuplicate(true);
			showFullGuide(true);
		}
		@Override public String getOptionValue(IGameOptions options) {
			return options.selectedNebulaeOption();
		}
		@Override public void setOptionValue(IGameOptions options, String newValue) {
			options.selectedNebulaeOption(newValue);
		}
	}
	default ParamList getNebula()	{ return nebulae; }

	ParamList randomEvents = new RandomEvents(); // Duplicate Do not add the list
	class RandomEvents extends ParamList {
		RandomEvents() {
			super(ADV_UI, "RANDOM_EVENTS", getRandomEventOptions(), RANDOM_EVENTS_NO_MONSTERS);
			isDuplicate(true);
			showFullGuide(true);
		}
		@Override public String getOptionValue(IGameOptions options) {
			return options.selectedRandomEventOption();
		}
		@Override public void setOptionValue(IGameOptions options, String newValue) {
			options.selectedRandomEventOption(newValue);
		}
	}

	ParamList planetQuality	= new PlanetQuality(); // Duplicate Do not add the list
	class PlanetQuality extends ParamList {
		PlanetQuality() {
			super(ADV_UI, "PLANET_QUALITY", getPlanetQualityOptions(), PLANET_QUALITY_NORMAL);
			isDuplicate(true);
			showFullGuide(true);
		}
		@Override public String getOptionValue(IGameOptions options) {
			return options.selectedPlanetQualityOption();
		}
		@Override public void setOptionValue(IGameOptions options, String newValue) {
			options.selectedPlanetQualityOption(newValue);
		}
	}

	ParamList terraforming		= new Terraforming(); // Duplicate Do not add the list
	class Terraforming extends ParamList {
		Terraforming() {
			super(ADV_UI, "TERRAFORMING", getTerraformingOptions(), TERRAFORMING_NORMAL);
			isDuplicate(true);
			showFullGuide(true);
		}
		@Override public String getOptionValue(IGameOptions options) {
			return options.selectedTerraformingOption();
		}
		@Override public void setOptionValue(IGameOptions options, String newValue) {
			options.selectedTerraformingOption(newValue);
		}
	}

	ParamList colonizing		= new Colonizing(); // Duplicate Do not add the list
	class Colonizing extends ParamList {
		Colonizing() {
			super(ADV_UI, "COLONIZING", getColonizingOptions(), COLONIZING_NORMAL);
			isDuplicate(true);
			showFullGuide(true);
		}
		@Override public String getOptionValue(IGameOptions options) {
			return options.selectedColonizingOption();
		}
		@Override public void setOptionValue(IGameOptions options, String newValue) {
			options.selectedColonizingOption(newValue);
		}
	}

	ParamList councilWin = new CouncilWin(); // Duplicate Do not add the list
	class CouncilWin extends ParamList {
		CouncilWin() {
			super(ADV_UI, "COUNCIL_WIN", getCouncilWinOptions(), COUNCIL_REBELS);
			setDefaultValue(MOO1_DEFAULT, COUNCIL_IMMEDIATE);
			isDuplicate(true);
			showFullGuide(true);
		}
		@Override public String getOptionValue(IGameOptions options) {
			return options.selectedCouncilWinOption();
		}
		@Override public void setOptionValue(IGameOptions options, String newValue) {
			options.selectedCouncilWinOption(newValue);
		}
		@Override protected String descriptionId() {
			return "SETTINGS_COUNCIL_DESC";
		}
	}

	ParamList randomizeAI = new RandomizeAI(); // Duplicate Do not add the list
	class RandomizeAI extends ParamList {
		RandomizeAI() {
			super(ADV_UI, "RANDOMIZE_AI", getRandomizeAIOptions(), RANDOMIZE_AI_NONE);
			isDuplicate(true);
			showFullGuide(false);
		}
		@Override public String getOptionValue(IGameOptions options) {
			return options.selectedRandomizeAIOption();
		}
		@Override public void setOptionValue(IGameOptions options, String newValue) {
			options.selectedRandomizeAIOption(newValue);
		}
	}

	ParamList autoplay = new Autoplay(); // Duplicate Do not add the list
	default ParamList autoplay()	{ return autoplay; }
	class Autoplay extends ParamList {
		Autoplay() {
			super(ADV_UI, "AUTOPLAY", IGameOptions.autoPlayAIset().getAutoPlay(), AUTOPLAY_OFF);
			isDuplicate(true);
			showFullGuide(false);
		}
		@Override public void reInit(List<String> list) {
			if (list == null)
				super.reInit(IGameOptions.autoPlayAIset().getAutoPlay());
			else
				super.reInit(list);
		}
		@Override public String getOptionValue(IGameOptions options) {
			return options.selectedAutoplayOption();
		}
		@Override public void setOptionValue(IGameOptions options, String newValue) {
			options.selectedAutoplayOption(newValue);
		}
	}

	ParamList researchRate		= new ResearchRate(); // Duplicate Do not add the list
	class ResearchRate extends ParamList {
		ResearchRate() {
			super(ADV_UI, "RESEARCH_RATE", getResearchRateOptions(), RESEARCH_NORMAL);
			isDuplicate(true);
			showFullGuide(true);
		}
		@Override public String getOptionValue(IGameOptions options) {
			return options.selectedResearchRate();
		}
		@Override public void setOptionValue(IGameOptions options, String newValue) {
			options.selectedResearchRate(newValue);
		}
	}

	ParamList warpSpeed	= new WarpSpeed(); // Duplicate Do not add the list
	class WarpSpeed extends ParamList {
		WarpSpeed() {
			super(ADV_UI, "WARP_SPEED", getWarpSpeedOptions(), WARP_SPEED_NORMAL);
			isDuplicate(true);
			showFullGuide(true);
		}
		@Override public String getOptionValue(IGameOptions options) {
			return options.selectedWarpSpeedOption();
		}
		@Override public void setOptionValue(IGameOptions options, String newValue) {
			options.selectedWarpSpeedOption(newValue);
		}
	}

	ParamList fuelRange	= new FuelRange(); // Duplicate Do not add the list
	class FuelRange extends ParamList {
		FuelRange() {
			super(ADV_UI, "FUEL_RANGE", getFuelRangeOptions(), FUEL_RANGE_NORMAL);
			isDuplicate(true);
			showFullGuide(true);
		}
		@Override public String getOptionValue(IGameOptions options) {
			return options.selectedFuelRangeOption();
		}
		@Override public void setOptionValue(IGameOptions options, String newValue) {
			options.selectedFuelRangeOption(newValue);
			if (GameSession.instance().status().inProgress())
				GameSession.instance().galaxy().resetAllAI();
		}
	}

	ParamList techTrading = new TechTrading(); // Duplicate Do not add the list
	class TechTrading extends ParamList {
		TechTrading() {
			super(ADV_UI, "TECH_TRADING", getTechTradingOptions(), TECH_TRADING_YES);
			isDuplicate(true);
			showFullGuide(true);
		}
		@Override public String getOptionValue(IGameOptions options) {
			return options.selectedTechTradeOption();
		}
		@Override public void setOptionValue(IGameOptions options, String newValue) {
			options.selectedTechTradeOption(newValue);
		}
	}

	ParamList aiHostility	= new AiHostility(); // Duplicate Do not add the list
	class AiHostility extends ParamList {
		AiHostility() {
			super(ADV_UI, "AI_HOSTILITY", getAiHostilityOptions(), AI_HOSTILITY_NORMAL);
			isDuplicate(true);
			showFullGuide(true);
		}
		@Override public String getOptionValue(IGameOptions options) {
			return options.selectedAIHostilityOption();
		}
		@Override public void setOptionValue(IGameOptions options, String newValue) {
			options.selectedAIHostilityOption(newValue);
		}
	}
	// ==================== GUI List Declarations ====================
	//
	static SafeListParam advancedOptions() {
		SafeListParam options  = new SafeListParam(
				Arrays.asList(
						galaxyAge, starDensity,
						nebulae, planetQuality,
						terraforming,
						null,
						randomEvents, aiHostility,
						councilWin, randomizeAI,
						autoplay,
						null,
						researchRate, warpSpeed,
						fuelRange, techTrading,
						colonizing,
						null,
						AllSubUI.getHandle(SETUP_PARAMETERS_UI_KEY).getUI(),
						AllSubUI.getHandle(RULES_OPTIONS_UI_KEY).getUI(),
						AllSubUI.getHandle(SETTINGS_OPTIONS_UI_KEY).getUI(),
						AllSubUI.preGameSubUI(),
						AllSubUI.inGameSubUI()
						));
		return options;
	}
}
