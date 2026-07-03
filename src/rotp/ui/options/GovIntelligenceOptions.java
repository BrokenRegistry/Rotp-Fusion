package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;

final class GovIntelligenceOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = GOVERNOR_INTELLIGENCE_UI_KEY;
	static final String HEAD_ID	  = GOV_UI;

	@Override public String optionId()	{ return OPTION_ID; }
	@Override public String headId()	{ return HEAD_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				GOVERNOR_BASE_OPTIONS_TITLE,
				auto_Infiltrate,
				auto_Spy,
				respectPromises
				)));
		map.add(new SafeListParam(Arrays.asList(
				RELEVANT_TITLE,
				trainSpiesASAP,
				contactUpdateSpending
				)));
		return map;
	};
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						auto_Infiltrate,
						auto_Spy,
						respectPromises
						));
		return minorList;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						auto_Infiltrate,
						auto_Spy,
						respectPromises,

						LINE_SPACER_25,
						trainSpiesASAP,
						contactUpdateSpending
						));
		return majorList;
	}
}
