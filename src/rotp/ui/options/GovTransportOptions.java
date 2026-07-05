package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class GovTransportOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = GOVERNOR_TRANSPORT_UI_KEY;
	static final String HEAD_ID	  = GOV_UI;

	@Override public String optionId()	{ return OPTION_ID; }
	@Override public String headId()	{ return HEAD_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle(HEAD_ID + "TRANSPORT_OPTIONS"),
				autoTransportAI,
				autotransportFull,
				autotransportAll,
				transportNoRich,
				transportPoorX2,
				transportExcludeBesieged,
				transportMaxDist
				)));
		return map;
	};
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						autoTransportAI,
						autotransportFull,
						autotransportAll,
						transportNoRich,
						transportPoorX2,

						LINE_SPACER_25,
						transportExcludeBesieged,
						transportMaxDist
						));
		return minorList;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						autoTransportAI,
						autotransportFull,
						autotransportAll,
						transportNoRich,
						transportPoorX2,

						LINE_SPACER_25,
						transportExcludeBesieged,
						transportMaxDist
						));
		return majorList;
	}
}
