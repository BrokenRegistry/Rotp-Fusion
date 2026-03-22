package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.model.planet.Planet;
import rotp.ui.util.ParamTitle;

public final class PreGameOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = PRE_GAME_OPTIONS_UI_KEY;
	
	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{ return preGameOptionsMap(); }

	public static SafeListPanel preGameOptionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("START_GALAXY_OPTIONS"),
				galaxyAge, starDensity,
				empiresSpreadingFactor,
				looseNeighborhood,
				minStarsPerEmpire, prefStarsPerEmpire, dynStarsPerEmpire,

				HEADER_SPACER_50,
				new ParamTitle("NEBULAE_OPTION"),
				nebulae, nebulaPlacing,
				nebulaEnrichment, nebulaHomeworld,
				realNebulaSize,
				realNebulaShape,
				realNebulaeOpacity
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("START_EMPIRE_OPTIONS"),
				orionLikeHomeworld, artifactsHomeworld,
				fertileHomeworld, gaiaHomeworld,
				richHomeworld, ultraRichHomeworld,
				companionWorlds, battleScout, randomTechStart, randomizeAI,

				HEADER_SPACER_50,
				new ParamTitle("START_PLANET_OPTIONS"),
				planetQuality, minDistArtifactPlanet,
				Planet.guardianMonsters,
				guardianMonstersLevel,
				Planet.guardianMonstersProbability,

				// headerSpacer,
				// new ParamTitle("SUB_PANEL_OPTIONS"),
				AllSubUI.systemSubUI()
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("START_TECH_CONTROL"),
				techIrradiated, techCloning, techAtmospheric,
				techCloaking, techStargate, techGaia, techHyperspace,
				techIndustry2, techThorium, techTransport,

				HEADER_SPACER_50,
				new ParamTitle("START_RANDOM_ALIENS"),
				randomAlienRacesTargetMax, randomAlienRacesTargetMin, randomAlienRaces,
				randomAlienRacesMax, randomAlienRacesMin, randomAlienRacesSmoothEdges
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("RESTART_OPTIONS"),
				restartChangesPlayerRace, restartChangesPlayerAI,
				restartChangesAliensAI, restartAppliesSettings,

				HEADER_SPACER_50,
				new ParamTitle("MENU_OPTIONS"),
				useFusionFont, compactOptionOnly,
				
				// headerSpacer,
				// new ParamTitle("MENU_OPTIONS"),
				HEADER_SPACER_50,
				new ParamTitle("BETA_TEST"),
				AllSubUI.ironmanSubUI(),
				ironmanMode,	// ironmanOptionsUI,

				HEADER_SPACER_50,
				new ParamTitle("GAME_OTHER"),
				showAllAI,
				autoplay
				)));
		return map;
	}
}
