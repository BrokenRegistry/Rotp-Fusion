package rotp.ui.options;

import java.util.Arrays;

import rotp.model.combat.ShipCombatManager;
import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class ShipCombatRules extends AbstractOptionsSubUI {
	static final String OPTION_ID = SHIP_COMBAT_RULES_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		SafeListParam list1 = new SafeListParam("");
		list1.addAll(AllSubUI.getHandle(COMBAT_ASTEROID_UI_KEY).getUiMajor(false));
		list1.add(HEADER_SPACER_50);
		list1.addAll(AllSubUI.getHandle(COMBAT_XILMI_AI_UI_KEY).getUiMajor(false));
		list1.add(HEADER_SPACER_50);
		list1.addAll(AllSubUI.getHandle(COMBAT_TIMING_UI_KEY).getUiMajor(false));
		list1.add(HEADER_SPACER_50);
//		list1.add(new ParamTitle("COMBAT_RESOLUTION"));
//		list1.add(moo1ShieldRules);
//		list1.add(moo1CombatResolution);
//		list1.add(ShipCombatManager.repulsorMode);
		map.add(list1);

		list1 = new SafeListParam("");
		list1.addAll(Arrays.asList(
				new ParamTitle("COMBAT_RESOLUTION"),
				moo1ShieldRules,
				moo1CombatResolution,

				LINE_SPACER_25,
				ShipCombatManager.repulsorMode,

				HEADER_SPACER_100,
				new ParamTitle("RETREAT_RULES"),
				retreatDestination,
				hyperComRetreatExtended,
				noEnemyOnRetreatDestination
				));
		map.add(list1);

		list1 = new SafeListParam("");
		list1.addAll(AllSubUI.getHandle(WEAPON_ANIMATION_UI_KEY).getUiMajor(true));
//		list1.addAll(Arrays.asList(
//				HEADER_SPACER_100,
//				new ParamTitle("RETREAT_RULES"),
//				retreatDestination,
//				hyperComRetreatExtended,
//				noEnemyOnRetreatDestination
//				));
		map.add(list1);

		map.add(AllSubUI.getHandle(SHIELD_ANIMATION_UI_KEY).getUiMajor(true));

		return map;
	}
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = AllSubUI.getHandle(COMBAT_TIMING_UI_KEY).majorList();
		minorList.addAll(Arrays.asList(
				HEADER_SPACER_50,
				retreatDestination,
				hyperComRetreatExtended,
				noEnemyOnRetreatDestination
				));
		return minorList;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey());
		majorList.addAll(AllSubUI.getHandle(WEAPON_ANIMATION_UI_KEY).getUiMajor(false));
		majorList.add(HEADER_SPACER_50);
		majorList.addAll(AllSubUI.getHandle(SHIELD_ANIMATION_UI_KEY).getUiMajor(false));
		majorList.addAll(Arrays.asList(
				HEADER_SPACER_50,
				retreatDestination,
				hyperComRetreatExtended,
				noEnemyOnRetreatDestination
				));
		return majorList;
	}
}
