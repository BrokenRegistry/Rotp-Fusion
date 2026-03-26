package rotp.ui.options;

import java.util.Arrays;

import rotp.model.events.RandomEventMonsters;
import rotp.model.galaxy.GuardianAmoeba;
import rotp.model.galaxy.GuardianCrystal;
import rotp.model.galaxy.GuardianPirates;
import rotp.model.galaxy.OrionGuardianShip;
import rotp.model.galaxy.SpaceAmoeba;
import rotp.model.galaxy.SpaceCrystal;
import rotp.model.galaxy.SpaceCuttlefish;
import rotp.model.galaxy.SpaceJellyfish;
import rotp.model.galaxy.SpacePirates;
import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class MonstersRules extends AbstractOptionsSubUI {
	static final String OPTION_ID = MONSTER_RULES_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("GUARD_MONSTERS_RULES"),
				guardianMonstersLevel,

				HEADER_SPACER_50,
				OrionGuardianShip.guardOrionLevelPct,
				OrionGuardianShip.isMoO1Monster,

				LINE_SPACER_25,
				GuardianPirates.guardPiratesLevelPct,

				LINE_SPACER_25,
				GuardianAmoeba.guardAmoebaLevelPct,
				GuardianAmoeba.isMoO1Monster,

				LINE_SPACER_25,
				GuardianCrystal.guardCrystalLevelPct,
				GuardianCrystal.isMoO1Monster,

				HEADER_SPACER_50,
				SpaceJellyfish.guardJellyfishLevelPct,
				SpaceCuttlefish.guardCuttlefishLevelPct
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("ROAMING_MONSTERS_RULES"),
				OrionGuardianShip.isMoO1Monster,

				HEADER_SPACER_50,
				monstersLevel,
				monstersGiveLoots,
				monstersGNNNotification,

				HEADER_SPACER_50,
				RandomEventMonsters.monstersMinDistance,
				RandomEventMonsters.monstersMaxDistance,
				RandomEventMonsters.monstersSpeed,

				HEADER_SPACER_50,
				RandomEventMonsters.monstersArePicky
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("EVENT_MONSTERS_RULES"),
				piratesDelayTurn,
				piratesReturnTurn,
				piratesMaxSystems,
				SpacePirates.piratesLevelPct,

				HEADER_SPACER_50,
				amoebaDelayTurn,
				amoebaReturnTurn,
				amoebaMaxSystems,
				SpaceAmoeba.amoebaLevelPct,
				SpaceAmoeba.isMoO1Monster,

				HEADER_SPACER_50,
				crystalDelayTurn,
				crystalReturnTurn,
				crystalMaxSystems,
				SpaceCrystal.crystalLevelPct,
				SpaceCrystal.isMoO1Monster
				)));

		return map;
	}
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						OrionGuardianShip.isMoO1Monster,
						guardianMonstersLevel
						));
		return minorList;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						guardianMonstersLevel,
						LINE_SPACER_25,
						OrionGuardianShip.guardOrionLevelPct,
						OrionGuardianShip.isMoO1Monster,
						LINE_SPACER_25,
						GuardianPirates.guardPiratesLevelPct,
						GuardianAmoeba.guardAmoebaLevelPct,
						GuardianAmoeba.isMoO1Monster,
						LINE_SPACER_25,
						GuardianCrystal.guardCrystalLevelPct,
						GuardianCrystal.isMoO1Monster,
						LINE_SPACER_25,
						SpaceJellyfish.guardJellyfishLevelPct,
						SpaceCuttlefish.guardCuttlefishLevelPct
						));
		return majorList;
	}
}
