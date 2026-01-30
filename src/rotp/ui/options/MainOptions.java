package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.IParam;

public final class MainOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = MAIN_OPTIONS_UI_KEY;

	// ==================== GUI List Declarations ====================
	//
	//String NAME_GUI_ID	= "MAIN_OPTIONS";

	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		SafeListParam options = vanillaSettingsUI();
		
		// Vanilla UI to compact UI conversion
		SafeListParam column = new SafeListParam("");
		for (IParam<?> opt : options) {
			if (opt == null) {
				map.add(column);
				column = new SafeListParam("");
			}
			else
				column.add(opt);
		}
		return map;
	}

	// vanilla UI format
	public static SafeListParam vanillaSettingsUI() {
		SafeListParam options  = new SafeListParam(OPTION_ID,
				Arrays.asList(
						displayMode, graphicsMode,
						texturesMode, sensitivityMode,
						selectedScreen,

						null,
						soundVolume, musicVolume,
						colorSet, gameOverTitles,
						defaultSettings,
						
						null,
						backupTurns, backupKeep, saveDirectory,
						originalSpeciesOnly, showAllAI,

						null,
						disableAutoHelp,
						disableAdvisor,
						menuStartup,
						AllSubUI.getHandle(SETTINGS_OPTIONS_UI_KEY).getUI(),
						AllSubUI.getHandle(DEBUG_OPTIONS_UI_KEY).getUI()
						));
		return options;
	}

}
