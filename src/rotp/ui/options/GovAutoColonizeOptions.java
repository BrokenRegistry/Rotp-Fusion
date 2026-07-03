package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class GovAutoColonizeOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = GOVERNOR_AUTO_COLONY_UI_KEY;
	static final String HEAD_ID	  = GOV_UI;
	
	@Override public String optionId()	{ return OPTION_ID; }
	@Override public String headId()	{ return HEAD_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				GOVERNOR_BASE_OPTIONS_TITLE,
				govAutoColonize,
				autoColonyCount,
				colonyDistanceWeight,

				HEADER_SPACER_50,
				new ParamTitle("GOVERNOR_SHIP_DESIGN"),
				autoTagAutoColon,

				HEADER_SPACER_50,
				RELEVANT_TITLE,
				autoColonize_
				)));
		map.add(new SafeListParam(Arrays.asList(
				GOVERNOR_ADVANCED_OPTIONS_TITLE,
				fleetAutoColonizeMode,
				autoColonizeTuned,
				autoColonizeMaxTime,

				LINE_SPACER_25,
				autoColonizeMultiple,
				autoColonizeSaveTime,
				secondColonyWeightPct,

				LINE_SPACER_25,
				armedColonizerGuard,
				armedColonizerFight
				)));
		return map;
	};
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						govAutoColonize,
						autoColonyCount
						));
		return minorList;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						govAutoColonize,
						autoColonyCount,
						colonyDistanceWeight,

						LINE_SPACER_25,
						fleetAutoColonizeMode,
						autoColonizeTuned,
						autoColonizeMaxTime,

						LINE_SPACER_25,
						autoColonizeMultiple,
						autoColonizeSaveTime,
						secondColonyWeightPct,

						LINE_SPACER_25,
						armedColonizerGuard,
						armedColonizerFight,

						LINE_SPACER_25,
						autoColonize_
						));
		return majorList;
	}
}
