package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class AutoSendFleetOption extends AbstractOptionsSubUI {
	static final String OPTION_ID = AUTO_SEND_FLEET_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }
	@Override public boolean isCfgFile()		{ return false; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("GOVERNOR_AUTO_SCOUT"),
				fleetAutoScoutMode,
				autoScoutSmart,
				autoScoutMaxTime,

				LINE_SPACER_25,
				autoScoutMultiple,
				autoScoutSaveTime,
				secondScoutWeightPct,

				LINE_SPACER_25,
				armedScoutGuard,

				LINE_SPACER_25,
				autoTagAutoScout
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("GOVERNOR_AUTO_ATTACK"),
				fleetAutoAttackMode,
				autoAttackEmpire,

				HEADER_SPACER_100,
				new ParamTitle(AUTO_SHIP_DESIGN + "SPACE"),
				autoShipDesignSpace,

				HEADER_SPACER_100,
				RELEVANT_TITLE,
				keepShipDesignName
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("GOVERNOR_AUTO_COLONIZE"),
				fleetAutoColonizeMode,
				autoColonizeTuned,
				autoColonizeMaxTime,

				LINE_SPACER_25,
				autoColonizeMultiple,
				autoColonizeSaveTime,
				secondColonyWeightPct,

				LINE_SPACER_25,
				armedColonizerGuard,
				armedColonizerFight,

				LINE_SPACER_25,
				autoColonize_,

				LINE_SPACER_25,
				autoTagAutoColon
				)));
		return map;
	};
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						fleetAutoScoutMode,
						fleetAutoColonizeMode,
						fleetAutoAttackMode
						));
		return minorList;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						fleetAutoScoutMode,
						autoScoutSmart,
						autoScoutMultiple,
						autoScoutMaxTime,
						armedScoutGuard,
						autoTagAutoScout,

						HEADER_SPACER_50,
						fleetAutoColonizeMode,
						armedColonizerGuard,
						autoColonize_,
						autoTagAutoColon,

						HEADER_SPACER_50,
						fleetAutoAttackMode,
						autoAttackEmpire
						));
		return majorList;
	}

}
