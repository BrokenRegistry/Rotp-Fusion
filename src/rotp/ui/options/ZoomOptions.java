package rotp.ui.options;

import java.util.Arrays;

import rotp.model.galaxy.StarSystem;
import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.model.ships.ShipLibrary;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.main.overlay.MapOverlayNone;
import rotp.ui.sprites.FlightPathSprite;
import rotp.ui.util.ParamTitle;

final class ZoomOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = ZOOM_OPTIONS_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }
	@Override public boolean isCfgFile()		{ return true; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("ZOOM_FONT"),
				StarSystem.mapFontFactor,
				StarSystem.showNameMinFont,
				StarSystem.showInfoFontRatio,

				HEADER_SPACER_50,
				new ParamTitle("ZOOM_FLEET"),
				GalaxyMapPanel.showFleetFactor,
				GalaxyMapPanel.showFlagFactor,
				GalaxyMapPanel.showPathFactor
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("TRANSPORT_VISIBILITY"),
				FlightPathSprite.minimumInvasionDisplay,
				FlightPathSprite.minimumTransportDisplay,
				LINE_SPACER_25,
				FlightPathSprite.systemTransportOpacity,
				FlightPathSprite.systemTroopOpacity,
				FlightPathSprite.playerTransportOpacity,
				FlightPathSprite.playerTroopOpacity,
				FlightPathSprite.invaderTroopOpacity,
				FlightPathSprite.alienTransportOpacity,
				LINE_SPACER_25,
				FlightPathSprite.rallyOpacity,
				HEADER_SPACER_50,
				new ParamTitle("MAP_SHIP_OPACITY"),
				ShipLibrary.shipSpritesAlpha
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("ZOOM_REPLAY"),
				finalReplayZoomOut,
				empireReplayZoomOut,
				replayTurnPace,

				HEADER_SPACER_100,
				RELEVANT_TITLE,
				MapOverlayNone.autoWarView
				)));
		return map;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						StarSystem.mapFontFactor,
						StarSystem.showNameMinFont,
						StarSystem.showInfoFontRatio,
						LINE_SPACER_25,
						GalaxyMapPanel.showFleetFactor,
						GalaxyMapPanel.showFlagFactor,
						GalaxyMapPanel.showPathFactor,
						LINE_SPACER_25,
						finalReplayZoomOut,
						empireReplayZoomOut,
						replayTurnPace,
						LINE_SPACER_25,
						ShipLibrary.shipSpritesAlpha
						));
		return majorList;
	}
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						GalaxyMapPanel.showFlagFactor,
						GalaxyMapPanel.showFleetFactor,
						StarSystem.mapFontFactor
						));
		return minorList;
	}
}
