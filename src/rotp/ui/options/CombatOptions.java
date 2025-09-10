package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class CombatOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = COMBAT_OPTIONS_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }
	@Override public boolean isCfgFile()		{ return true; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("WEAPON_ANIMATIONS"),
				showResultDelay,
				newWeaponSound, playerSoundEcho,
				echoSoundDecay, echoSoundDelay, echoSoundHullDelay,

				HEADER_SPACER_50,
				beamWindupFrames, beamHoldFrames,
				heavyBeamHoldFrames, shieldFadingFrames
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("SHIELD_ANIMATIONS"),
				shieldType, alwaysShowsShield, 
	
				HEADER_SPACER_50,
				beamAnimationFPS, shieldEnveloping, shieldBorder,
				shieldTransparency, shieldFlickering, shieldNoisePct,
				weaponZposition, weaponZRandom,
	
				HEADER_SPACER_50,
				startShieldDemo
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("ASTEROIDS"),
				moo1PlanetLocation, moo1AsteroidsLocation,
				asteroidsVanish, asteroidsVanishProbPerMille,
				moo1AsteroidsProperties,
				HEADER_SPACER_50,
				minLowAsteroids, maxLowAsteroids,
				minHighAsteroids, maxHighAsteroids,
				HEADER_SPACER_50,
				baseNoAsteroidsProbPct, stepNoAsteroidsProbPct,
				baseLowAsteroidsProbPct, stepLowAsteroidsProbPct,
				richNoAsteroidsModPct, ultraRichNoAsteroidsModPct
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("XILMI_AI_OPTIONS"),
				playerAttackConfidence, playerDefenseConfidence,
				aiAttackConfidence, aiDefenseConfidence,

				HEADER_SPACER_50,
				new ParamTitle("GAME_OTHER"),
				maxCombatTurns,
				retreatRestrictions, retreatRestrictionTurns,
				moo1RetreatRules
				)));
		return map;
	};
}
