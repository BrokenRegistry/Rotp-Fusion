package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class SystemsOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = SYSTEMS_OPTIONS_UI_KEY;
	
	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("ARTIFACT_OPTIONS"),
				artifactPlanetMult, artifactPlanetOffset,
				LINE_SPACER_25,
				orionPlanetProb,
				LINE_SPACER_25,
				allowRichPoorArtifact,
				
				HEADER_SPACER_50,
				new ParamTitle("GAME_OTHER"),
				orionToEmpireModifier
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("RICH_OPTIONS"),
				ultraPoorPlanetMult, ultraPoorPlanetOffset,
				poorPlanetMult, poorPlanetOffset,
				LINE_SPACER_25,
				richPlanetMult, richPlanetOffset,
				ultraRichPlanetMult, ultraRichPlanetOffset
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("HOMEWORLD_NEIGHBORHOOD"),
				firstRingSystemNumber,
				firstRingHabitable,
				firstRingRadius,
				secondRingSystemNumber,
				secondRingHabitable,
				secondRingRadius,

				HEADER_SPACER_50,
				new ParamTitle("LINKED_OPTIONS"),
				starDensity,
				sizeSelection,
				dynStarsPerEmpire
				)));
		return map;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						planetQuality,
						minDistArtifactPlanet,
						LINE_SPACER_25,
						guardianMonsters,
						guardianMonstersLevel,
						guardianMonstersProbability
						));
		return majorList;
	}
}
