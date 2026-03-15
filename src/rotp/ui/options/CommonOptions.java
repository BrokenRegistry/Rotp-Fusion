package rotp.ui.options;

import java.util.Arrays;

import rotp.model.galaxy.StarSystem;
import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.util.ParamTitle;

final class CommonOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = COMMON_OPTIONS_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("COMPUTER_OPTIONS"),
				graphicsMode, texturesMode, sensitivityMode,
				soundVolume, musicVolume,

				HEADER_SPACER_50,
				new ParamTitle("MENU_APPEARANCE"),
				colorSet, galaxyPreviewColorStarsSize,
				minListSizePopUp, menuStartup,
				noFogOnIcons, showAlternateAnimation,
				useFusionFont, compactOptionOnly,
				loadSaveWidth
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("ZOOM_FONT"),
				StarSystem.mapFontFactor, StarSystem.showNameMinFont, StarSystem.showInfoFontRatio,

				HEADER_SPACER_50,
				new ParamTitle("ZOOM_FLEET"),
				GalaxyMapPanel.showFleetFactor, GalaxyMapPanel.showFlagFactor, GalaxyMapPanel.showPathFactor,

				HEADER_SPACER_50,
				new ParamTitle("ZOOM_REPLAY"),
				finalReplayZoomOut, empireReplayZoomOut, replayTurnPace,

				HEADER_SPACER_50,
				new ParamTitle("GAME_OTHER"),
				shipBasedMissiles,
				scoutAndColonyOnly
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("BACKUP_OPTIONS"),
				backupTurns, backupKeep, saveDirectory,

				HEADER_SPACER_50,
				new ParamTitle("GAME_UI_PREFERENCES"),
				showPendingOrders,
				raceStatusLog, raceStatusView,
				disableAdvisor, disableAutoHelp,
				originalSpeciesOnly, displayFreeTech,

				// headerSpacer,
				// new ParamTitle("GAME_VARIOUS"),
				HEADER_SPACER_50,
				new ParamTitle("SUB_PANEL_OPTIONS"),
				AllSubUI.debugSubUI(),
				AllSubUI.combatSubUI(),
				AllSubUI.nameSubUI(),
				AllSubUI.nameFrSubUI()
				)));
		return map;
	};
}
