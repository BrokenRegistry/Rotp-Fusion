package rotp.ui.options;

import java.util.Arrays;

import rotp.model.combat.ShipCombatManager;
import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class NewRulesBeta extends AbstractOptionsSubUI {
	static final String OPTION_ID = NEW_RULES_BETA_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("NEW_ALPHA")
				)));

		SafeListParam list = new SafeListParam("NEW_BETA");
		list.add(new ParamTitle("NEW_BETA"));
		list.addAll(Arrays.asList(
				new ParamTitle("RETREAT_RULES"),
				retreatDestination,
				hyperComRetreatExtended,
				noEnemyOnRetreatDestination
				));
		map.add(list);

		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("NEW_SAFE"),
				ShipCombatManager.repulsorMode
				)));

		map.add(AllSubUI.getHandle(NEW_OPTIONS_BETA_UI_KEY).getUiMajor(true));
		return map;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						ShipCombatManager.repulsorMode
						));
		return majorList;
	}
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
//						maxLandingTroops,
//						maxLandingTroopsAmount,
//						maxLandingTroopsFactor
						));
		return minorList;
	}
}
