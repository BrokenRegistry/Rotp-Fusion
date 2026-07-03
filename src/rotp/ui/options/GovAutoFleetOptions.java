package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.model.tech.Tech;
import rotp.ui.util.ParamTitle;

final class GovAutoFleetOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = GOVERNOR_AUTO_FLEET_UI_KEY;
	static final String HEAD_ID	  = GOV_UI;

	@Override public String optionId()	{ return OPTION_ID; }
	@Override public String headId()	{ return HEAD_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		SafeListParam list;

		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("GOVERNOR_SHIP_DESIGN"),
				autoShipDesignSpace,
				autoTagAutoScout,
				autoTagAutoColon,

				HEADER_SPACER_100,
				RELEVANT_TITLE,
				keepShipDesignName,
				LINE_SPACER_25,
				scoutAndColonyOnly,
				LINE_SPACER_25,
				Tech.moo1Miniaturization
				//LINE_SPACER_25,
				//autoShipsDefault	// TODO: for future use
				)));

		list = new SafeListParam("");
		list.addAll(AllSubUI.getHandle(GOVERNOR_AUTO_SCOUT_UI_KEY).getUiMajor(false));
		map.add(list);

		list = new SafeListParam("");
		list.addAll(AllSubUI.getHandle(GOVERNOR_AUTO_ATTACK_UI_KEY).getUiMajor(false));
		map.add(list);

		list = new SafeListParam("");
		list.addAll(AllSubUI.getHandle(GOVERNOR_AUTO_COLONY_UI_KEY).getUiMajor(false));
		map.add(list);
		return map;
	};
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey());
		minorList.addAll(AllSubUI.getHandle(GOVERNOR_AUTO_SCOUT_UI_KEY).getUiMinor(false));
		minorList.add(LINE_SPACER_25);
		minorList.addAll(AllSubUI.getHandle(GOVERNOR_AUTO_COLONY_UI_KEY).getUiMinor(false));
		minorList.add(LINE_SPACER_25);
		minorList.addAll(AllSubUI.getHandle(GOVERNOR_AUTO_ATTACK_UI_KEY).getUiMinor(false));
		return minorList;
	}
}
