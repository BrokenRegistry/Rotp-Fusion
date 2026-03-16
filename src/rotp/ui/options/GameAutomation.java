package rotp.ui.options;

import java.util.Arrays;

import rotp.model.combat.ShipCombatManager;
import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;

final class GameAutomation extends AbstractOptionsSubUI {
	static final String OPTION_ID = GAME_AUTOMATION_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				//AllSubUI.getHandle(GOVERNOR_UI_KEY).getUI(),
				HEADER_SPACER_50,
				transportAutoEco,
				autoTerraformEnding,
				divertExcessToResearch,

				LINE_SPACER_25,
				autoBombard_,
				autoColonize_,
				ShipCombatManager.fleetAutoCombat,
				ShipCombatManager.showAutoCombatResults,

				LINE_SPACER_25,
				techExchangeAutoRefuse
				)));
		//map.add(list);
		
		SafeListParam list = new SafeListParam(Arrays.asList(
				defaultForwardRally,
				defaultChainRally,
				chainRallySpeed,
				rallyCombat,
				rallyCombatLoss,

				HEADER_SPACER_50,
				scoutAndColonyOnly,

				HEADER_SPACER_50,
				trackUFOsAcrossTurns,
				HEADER_SPACER_50
				));
		list.addAll(AllSubUI.getHandle(GOVERNOR_UI_KEY).getUiMinor(false));
		map.add(list);

		map.add(AllSubUI.getHandle(GNN_AND_POPUP_FILTER_UI_KEY).getUiMajor(true));
		return map;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						autoBombard_,
						autoColonize_,
						techExchangeAutoRefuse,

						LINE_SPACER_25,
						defaultForwardRally,
						defaultChainRally,
						chainRallySpeed,
						rallyCombat,
						rallyCombatLoss,

						LINE_SPACER_25,
						transportAutoEco,
						autoTerraformEnding,

						LINE_SPACER_25,
						divertExcessToResearch,
						trackUFOsAcrossTurns,

						LINE_SPACER_25
						));
		majorList.addAll(AllSubUI.getHandle(GOVERNOR_UI_KEY).getUiMinor(false));
		return majorList;
	}
}
