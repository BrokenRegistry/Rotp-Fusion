package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class CombatAsteroids extends AbstractOptionsSubUI {
	static final String OPTION_ID = COMBAT_ASTEROID_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("ASTEROID_DENSITY"),
				moo1PlanetLocation,
				moo1AsteroidsLocation,
				asteroidsVanish,
				asteroidsVanishProbPerMille,
				moo1AsteroidsProperties
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("ASTEROID_PROBABILITY"),
				minLowAsteroids,
				maxLowAsteroids,
				minHighAsteroids,
				maxHighAsteroids
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("ASTEROID_IMPACT"),
				baseNoAsteroidsProbPct,
				stepNoAsteroidsProbPct,
				baseLowAsteroidsProbPct,
				stepLowAsteroidsProbPct,
				richNoAsteroidsModPct,
				ultraRichNoAsteroidsModPct
				)));
		return map;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						moo1PlanetLocation,
						moo1AsteroidsLocation,
						asteroidsVanish,
						asteroidsVanishProbPerMille,
						moo1AsteroidsProperties
						));
		return majorList;
	}
}
