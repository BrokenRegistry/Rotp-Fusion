package rotp.ui.options;

import java.util.Arrays;

import rotp.model.galaxy.OrionGuardianShip;
import rotp.model.galaxy.SpaceAmoeba;
import rotp.model.galaxy.SpaceCrystal;
import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.model.tech.Tech;
import rotp.ui.util.ParamTitle;

final class MoO1RulesOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = MOO1_RULES_OPTIONS_UI_KEY;
	
	@Override public String optionId()			{ return OPTION_ID; }
	@Override public boolean isCfgFile()		{ return false; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("MOO1_SPACE_COMBAT_START"),
				rallyCombat,

				HEADER_SPACER_50,
				new ParamTitle("MOO1_SPACE_COMBAT_LAYOUT"),
				moo1PlanetLocation,
				moo1AsteroidsLocation,
				moo1AsteroidsProperties,
				asteroidsVanish,

				HEADER_SPACER_50,
				new ParamTitle("MOO1_SPACE_COMBAT_RULES"),
				moo1CombatResolution,
				moo1ShieldRules,
				moo1RetreatRules,
				maxCombatTurns,

				HEADER_SPACER_50,
				new ParamTitle("MOO1_SPACE_COMBAT_END"),
				retreatRestrictionTurns,
				retreatRestrictions,
				retreatDestination,
				hyperComRetreatExtended
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("MOO1_GROUND_COMBAT"),
				maxLandingTroopsAmount,
				maxLandingTroops,

				HEADER_SPACER_50,
				new ParamTitle("MOO1_MONSTERS_COMBAT"),
				OrionGuardianShip.isMoO1Monster,
				SpaceAmoeba.isMoO1Monster,
				amoebaMaxSystems,
				SpaceCrystal.isMoO1Monster,
				crystalMaxSystems,
				piratesMaxSystems,
				monstersLevel,

				HEADER_SPACER_50,
				new ParamTitle("MOO1_SHIP_DESIGN"),
				missileBaseModifier,
				missileShipModifier,
				Tech.moo1Miniaturization
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("MOO1_RANDOMNESS"),
				researchMoo1,
				persistentArtifact,
				persistentRNG,

				HEADER_SPACER_50,
				new ParamTitle("DEBUG_RELEVANT"),
				defaultSettings
				)));
		return map;
	}
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						defaultSettings
						));
		return minorList;
	}
}
