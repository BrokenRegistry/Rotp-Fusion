package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class RandomEventsOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = RANDOM_EVENTS_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("RANDOM_EVENTS_GLOBAL"),
				randomEvents,
				eventsStartTurn,
				eventsPace,
				eventsFavorWeak,
				fixedEventsMode,
				monstersGiveLoots,
				monstersLevel,
				monstersGNNNotification,

				HEADER_SPACER_50,
				guardianMonstersLevel,
				isMoO1Monster
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("RANDOM_EVENTS_MONSTERS"),
				piratesDelayTurn,
				piratesReturnTurn,
				piratesMaxSystems,
				piratesLevelMultiplier,

				HEADER_SPACER_50,
				amoebaDelayTurn,
				amoebaReturnTurn,
				amoebaMaxSystems,

				HEADER_SPACER_50,
				crystalDelayTurn,
				crystalReturnTurn,
				crystalMaxSystems,

				HEADER_SPACER_50,
				monsterMinDistance,
				monsterMaxDistance,
				monsterSpeed
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("RANDOM_EVENTS_DELAYS"),
				donationDelayTurn,
				depletedDelayTurn,
				enrichedDelayTurn,
				fertileDelayTurn,
				virusDelayTurn,
				earthquakeDelayTurn,
				accidentDelayTurn,
				rebellionDelayTurn,
				derelictDelayTurn,
				assassinDelayTurn,
				plagueDelayTurn,
				supernovaDelayTurn,
				piracyDelayTurn,
				cometDelayTurn,
				relicDelayTurn,
				sizeBoostDelayTurn,
				gauntletDelayTurn
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("RANDOM_EVENTS_RETURNS"),
				donationReturnTurn,
				depletedReturnTurn,
				enrichedReturnTurn,
				fertileReturnTurn,
				virusReturnTurn,
				earthquakeReturnTurn,
				accidentReturnTurn,
				rebellionReturnTurn,
				derelictReturnTurn,
				assassinReturnTurn,
				plagueReturnTurn,
				supernovaReturnTurn,
				piracyReturnTurn,
				cometReturnTurn,
				relicReturnTurn,
				sizeBoostReturnTurn,
				gauntletReturnTurn
				)));
		return map;
	}
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						randomEvents
						));
		return minorList;
	}
}
