package rotp.ui.options;

import java.util.Arrays;

import rotp.Rotp;
import rotp.model.game.RulesetManager;
import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

public final class SetupParameters extends AbstractOptionsSubUI {
	static final String OPTION_ID = SETUP_PARAMETERS_UI_KEY;
	
	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{ return setupParametersMap(); }

	public static SafeListPanel setupParametersMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);

		SafeListParam list = new SafeListParam(AllSubUI.getHandle(SETUP_GALAXY_OPTIONS_UI_KEY).getUiMajor(false));
		list.add(HEADER_SPACER_100);
		list.addAll(AllSubUI.getHandle(SETUP_NEBULA_OPTIONS_UI_KEY).getUiMajor(false));
		map.add(list);

		list = new SafeListParam(AllSubUI.getHandle(SETUP_HOMEWORLD_UI_KEY).getUiMajor(false));
		list.add(HEADER_SPACER_100);
		list.addAll(AllSubUI.getHandle(SYSTEMS_OPTIONS_UI_KEY).getUiMajor(false));
		map.add(list);

		list = new SafeListParam(AllSubUI.getHandle(SETUP_TECH_OPTIONS_UI_KEY).getUiMajor(false));
		list.add(HEADER_SPACER_100);
		list.addAll(AllSubUI.getHandle(SETUP_RANDOM_OPP_UI_KEY).getUiMajor(false));
		map.add(list);

		list = new SafeListParam(AllSubUI.getHandle(SETUP_RESTART_OPTIONS_UI_KEY).getUiMajor(false));
		list.add(HEADER_SPACER_100);
		list.addAll(AllSubUI.getHandle(IRONMAN_OPTIONS_UI_KEY).getUiMinor(false));
		list.add(HEADER_SPACER_100);
		list.addAll(new SafeListParam(Arrays.asList(
				new ParamTitle("GAME_OTHER"),
				randomizeAI,
				showAllAI,
				autoplay
				)));
		list.add(HEADER_SPACER_100);
		list.add(AllSubUI.getHandle(SETTING_MENU_PREF_UI_KEY).getUI());
		list.add(HEADER_SPACER_100);
		if (!Rotp.noOptions() && RulesetManager.current().isSetupMode())
			list.addAll(AllSubUI.getHandle(PRE_GAME_OPTIONS_UI_KEY).getUiMinor(false));
		else
			list.add(AllSubUI.getHandle(PRE_GAME_OPTIONS_UI_KEY).getUI());
		map.add(list);
		return map;
	}
}
