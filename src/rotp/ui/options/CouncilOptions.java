package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;

final class CouncilOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = COUNCIL_OPTIONS_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				councilWin,
				counciRequiredPct,
				councilPlayerVote,

				HEADER_SPACER_50,
				endOfGameCondition,
				endOfGameTurn
				)));
		return map;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						councilWin,
						counciRequiredPct,
						endOfGameCondition,
						endOfGameTurn
						));
		return majorList;
	}
}
