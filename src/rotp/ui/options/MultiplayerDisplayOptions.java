package rotp.ui.options;

import java.util.Set;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.util.LanguageManager;

/**
 * The settings a player may change during a hot-seat or play-by-email match: display, map, panels,
 * news filters, help and names. Gameplay settings are fixed for the match.
 */
public final class MultiplayerDisplayOptions extends AbstractOptionsSubUI {
	public static final String OPTION_ID = "MULTIPLAYER_DISPLAY";
	/** Option screens that only change what this computer shows. */
	private static final Set<String> DISPLAY_SCREENS = Set.of(OPTION_ID,
			VISUAL_OPTIONS_UI_KEY, ZOOM_OPTIONS_UI_KEY, GAME_MENU_PREF_UI_KEY, SETTING_MENU_PREF_UI_KEY,
			GNN_AND_POPUP_FILTER_UI_KEY, HELP_AND_ADVICE_UI_KEY, FLAG_OPTIONS_UI_KEY,
			NAME_OPTIONS_UI_KEY, NAME_OPTIONS_FR_UI_KEY);

	public static boolean displayOnly(String optionId)	{ return DISPLAY_SCREENS.contains(optionId); }
	/** The mixed user-interface screen opens as this display-only screen during a match. */
	public static boolean replacesDuringMatch(String optionId) { return SETTINGS_OPTIONS_UI_KEY.equals(optionId); }

	@Override public String optionId()			{ return OPTION_ID; }
	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		SafeListParam list = AllSubUI.getHandle(VISUAL_OPTIONS_UI_KEY).getUiMajor(false);
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(ZOOM_OPTIONS_UI_KEY).getUiMinor(false));
		map.add(list);
		list = AllSubUI.getHandle(GAME_MENU_PREF_UI_KEY).getUiMinor(false);
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(SETTING_MENU_PREF_UI_KEY).getUiMinor(false));
		map.add(list);
		list = new SafeListParam("GAME_VARIOUS");
		list.addAll(AllSubUI.getHandle(GNN_AND_POPUP_FILTER_UI_KEY).getUiMajor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(HELP_AND_ADVICE_UI_KEY).getUiMajor(false));
		list.add(HEADER_SPACER_50);
		list.addAll(AllSubUI.getHandle(FLAG_OPTIONS_UI_KEY).getUiMajor(false));
		String langDir = LanguageManager.selectedLanguageDir();
		if (langDir.equalsIgnoreCase("EN")) {
			list.add(HEADER_SPACER_50);
			list.add(AllSubUI.getHandle(NAME_OPTIONS_UI_KEY).getUI());
		}
		else if (langDir.equalsIgnoreCase("FR")) {
			list.add(HEADER_SPACER_50);
			list.add(AllSubUI.getHandle(NAME_OPTIONS_FR_UI_KEY).getUI());
		}
		map.add(list);
		return map;
	}
}
