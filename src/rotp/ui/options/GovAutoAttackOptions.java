package rotp.ui.options;

import java.util.Arrays;

import rotp.model.combat.ShipCombatManager;
import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class GovAutoAttackOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = GOVERNOR_AUTO_ATTACK_UI_KEY;
	static final String HEAD_ID	  = GOV_UI;
	
	@Override public String optionId()	{ return OPTION_ID; }
	@Override public String headId()	{ return HEAD_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				GOVERNOR_BASE_OPTIONS_TITLE,
				auto_Attack,
				autoAttackCount
				)));
		map.add(new SafeListParam(Arrays.asList(
				GOVERNOR_ADVANCED_OPTIONS_TITLE,
				fleetAutoAttackMode,
				autoAttackEmpire,
				LINE_SPACER_25,
				ShipCombatManager.fleetAutoCombat,
				ShipCombatManager.showAutoCombatResults
				)));
		return map;
	};
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						auto_Attack,
						autoAttackCount
						));
		return minorList;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						auto_Attack,
						autoAttackCount,

						fleetAutoAttackMode,
						autoAttackEmpire,
						LINE_SPACER_25,
						ShipCombatManager.fleetAutoCombat,
						ShipCombatManager.showAutoCombatResults
						));
		return majorList;
	}
}
