package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class GovOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = GOVERNOR_UI_KEY;
	static final String HEAD_ID	  = GOV_UI;
	static final String NAME_KEY  = "SETUP_MENU";
	static final String TITLE_KEY = "SETUP_TITLE";

	@Override public String optionId()			{ return OPTION_ID; }
	@Override public String headId()			{ return HEAD_ID; }
	@Override public String uiNameKey()			{ return NAME_KEY; }
	@Override public String uiTitleKey()		{ return TITLE_KEY; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		SafeListParam list = new SafeListParam("");
		list.addAll(AllSubUI.getHandle(GOVERNOR_TRANSPORT_UI_KEY).getUiMinor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(GOVERNOR_POPULATION_UI_KEY).getUiMinor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(GOVERNOR_BUILDING_UI_KEY).getUiMinor(false));
		map.add(list);

		list = new SafeListParam("");
//		list.addAll(AllSubUI.getHandle(GOVERNOR_COLONIES_UI_KEY).getUiMajor(false));
//		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(GOVERNOR_TAXES_UI_KEY).getUiMinor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(GOVERNOR_INTELLIGENCE_UI_KEY).getUiMinor(false));
		list.addAll(Arrays.asList(
				HEADER_SPACER_100,
				new ParamTitle(HEAD_ID + "OTHER_OPTIONS"),
				isManageableGovernor,

				LINE_SPACER_25,
				governorByDefault,
				auto_Apply
				));
		map.add(list);

		list = new SafeListParam("");
		list.addAll(AllSubUI.getHandle(GOVERNOR_AUTO_FLEET_UI_KEY).getUiMinor(false));
		list.add(HEADER_SPACER_100);
		list.add(AllSubUI.getHandle(GOVERNOR_ASPECT_UI_KEY).getUI());
		list.add(HEADER_SPACER_100);
		list.add(AllSubUI.getHandle(GOVERNOR_SPECIAL_UI_KEY).getUI());
		map.add(list);

		return map;
	};
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						auto_Infiltrate
						));
		return minorList;
	}
}
