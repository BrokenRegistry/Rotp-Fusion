package rotp.ui.options;

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
		list1.add(new ParamTitle("COMBAT_RESOLUTION"));
		list1.add(moo1ShieldRules);
		list1.add(moo1CombatResolution);
		map.add(list1);
		map.add(AllSubUI.getHandle(WEAPON_ANIMATION_UI_KEY).getUiMajor(true));
		map.add(AllSubUI.getHandle(SHIELD_ANIMATION_UI_KEY).getUiMajor(true));

		return map;
	}
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = AllSubUI.getHandle(COMBAT_TIMING_UI_KEY).majorList();
		return minorList;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey());
		majorList.addAll(AllSubUI.getHandle(WEAPON_ANIMATION_UI_KEY).getUiMajor(false));
		majorList.add(HEADER_SPACER_50);
		majorList.addAll(AllSubUI.getHandle(SHIELD_ANIMATION_UI_KEY).getUiMajor(false));
		return majorList;
	}
}
