package rotp.ui.options;

import rotp.Rotp;
import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.util.LanguageManager;

public final class SettingsOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = SETTINGS_OPTIONS_UI_KEY;
	
	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{ return inGameOptionsMap(); }

	public static SafeListPanel inGameOptionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		SafeListParam list = AllSubUI.getHandle(GAME_AUTOMATION_UI_KEY).getUiMajor(false);
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(SHIP_DESIGN_OPTIONS_UI_KEY).getUiMinor(false));
		map.add(list);

		list = new SafeListParam("GAME_VARIOUS");
		list.addAll(AllSubUI.getHandle(GNN_AND_POPUP_FILTER_UI_KEY).getUiMajor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(COLONY_SETTINGS_UI_KEY).getUiMajor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(COMPUTER_OPTIONS_UI_KEY).getUiMajor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(HELP_AND_ADVICE_UI_KEY).getUiMajor(false));
		map.add(list);

		list = new SafeListParam("GAME_VARIOUS");
		list.addAll(AllSubUI.getHandle(FLAG_OPTIONS_UI_KEY).getUiMajor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(SHIP_COMBAT_SETTINGS_UI_KEY).getUiMinor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(NEW_OPTIONS_BETA_UI_KEY).getUiMajor(false));
		map.add(list);

		list = AllSubUI.getHandle(VISUAL_OPTIONS_UI_KEY).getUiMajor(false);
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(GAME_MENU_PREF_UI_KEY).getUiMinor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(SETTING_MENU_PREF_UI_KEY).getUiMinor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(ZOOM_OPTIONS_UI_KEY).getUiMinor(false));
		if (!Rotp.noOptions()) {
			String langDir = LanguageManager.selectedLanguageDir();
			if (langDir.equalsIgnoreCase("EN")) {
				list.add(HEADER_SPACER_50);
				list.add(AllSubUI.getHandle(NAME_OPTIONS_UI_KEY).getUI());
			}
			else if (langDir.equalsIgnoreCase("FR")) {
				list.add(HEADER_SPACER_50);
				list.add(AllSubUI.getHandle(NAME_OPTIONS_FR_UI_KEY).getUI());
			}
		}
		map.add(list);
		return map;
	}
}
