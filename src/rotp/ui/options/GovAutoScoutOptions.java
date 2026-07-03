package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class GovAutoScoutOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = GOVERNOR_AUTO_SCOUT_UI_KEY;
	static final String HEAD_ID	  = GOV_UI;
	
	@Override public String optionId()	{ return OPTION_ID; }
	@Override public String headId()	{ return HEAD_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				GOVERNOR_BASE_OPTIONS_TITLE,
				auto_Scout,
				autoScoutCount,

				HEADER_SPACER_50,
				new ParamTitle("GOVERNOR_SHIP_DESIGN"),
				autoTagAutoScout,

				HEADER_SPACER_50,
				RELEVANT_TITLE,
				autoColonize_
				)));
		map.add(new SafeListParam(Arrays.asList(
				GOVERNOR_ADVANCED_OPTIONS_TITLE,
				fleetAutoScoutMode,
				autoScoutSmart,
				autoScoutMaxTime,

				LINE_SPACER_25,
				autoScoutMultiple,
				autoScoutSaveTime,
				secondScoutWeightPct,

				LINE_SPACER_25,
				armedScoutGuard
				)));
		return map;
	};
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						auto_Scout,
						autoScoutCount
						));
		return minorList;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						auto_Scout,
						autoScoutCount,

						LINE_SPACER_25,
						fleetAutoScoutMode,
						autoScoutSmart,
						autoScoutMaxTime,

						LINE_SPACER_25,
						autoScoutMultiple,
						autoScoutSaveTime,
						secondScoutWeightPct,

						LINE_SPACER_25,
						armedScoutGuard
						));
		return majorList;
	}
}
