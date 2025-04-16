package rotp.ui.options;

import rotp.Rotp;
import rotp.model.game.RulesetManager;
import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;

public final class RulesOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = RULES_OPTIONS_UI_KEY;
	
	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{ return rulesOptionsMap(); }

	public static SafeListPanel rulesOptionsMap()	{ // To be called by preloaded UI  
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		SafeListParam list;

		list = new SafeListParam("GAME_DIFFICULTY");
		list.addAll(AllSubUI.getHandle(GAME_DIFFICULTY_UI_KEY).getUiMajor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(GALAXY_RULES_UI_KEY).getUiMajor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(COLONY_RULES_UI_KEY).getUiMajor(false));
		map.add(list);

		list = new SafeListParam("GAME_RELATIONS");
		list.addAll(AllSubUI.getHandle(COUNCIL_OPTIONS_UI_KEY).getUiMajor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(DIPLOMACY_OPTIONS_UI_KEY).getUiMajor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(SPY_OPTIONS_UI_KEY).getUiMajor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(IRONMAN_LITTLE_UI_KEY).getUiMajor(false));
		map.add(list);

		list = new SafeListParam("");
		list.addAll(AllSubUI.getHandle(RANDOM_EVENTS_UI_KEY).getUiMinor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(GOVERNOR_UI_KEY).getUiMinor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(SHIP_COMBAT_RULES_UI_KEY).getUiMinor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(DEBUG_OPTIONS_UI_KEY).getUiMinor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(NEW_RULES_BETA_UI_KEY).getUiMajor(false));
		map.add(list);

		list = new SafeListParam("");
		list.addAll(AllSubUI.getHandle(SHIP_RULES_UI_KEY).getUiMajor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(COMBAT_XILMI_AI_UI_KEY).getUiMajor(false));
		list.add(HEADER_SPACER_100);
		list.add(RELEVANT_TITLE);
		list.add(AllSubUI.getHandle(IN_GAME_OPTIONS_UI_KEY).getUI());
		list.add(HEADER_SPACER_50);
		if (!Rotp.noOptions() && RulesetManager.current().isSetupMode())
			list.addAll(AllSubUI.getHandle(SETTINGS_OPTIONS_UI_KEY).getUiMinor(false));
		else
			list.add(AllSubUI.getHandle(SETTINGS_OPTIONS_UI_KEY).getUI());
		map.add(list);
		return map;
	}
}
