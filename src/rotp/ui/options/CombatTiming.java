package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;

final class CombatTiming extends AbstractOptionsSubUI {
	static final String OPTION_ID = COMBAT_TIMING_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				maxCombatTurns,
				retreatRestrictions,
				retreatRestrictionTurns,
				moo1RetreatRules
				)));
		return map;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						maxCombatTurns,
						retreatRestrictions,
						retreatRestrictionTurns,
						moo1RetreatRules
						));
		return majorList;
	}
}
