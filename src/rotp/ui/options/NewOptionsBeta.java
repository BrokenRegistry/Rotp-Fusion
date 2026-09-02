package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.game.AdvisorPanel;
import rotp.ui.util.ParamTitle;

final class NewOptionsBeta extends AbstractOptionsSubUI {
	static final String OPTION_ID = NEW_OPTIONS_BETA_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }
	@Override public boolean isCfgFile()		{ return true; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("NEW_ALPHA"),
				debugAutoRun
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("NEW_BETA"),
				rallyCombat,
				rallyCombatLoss
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("NEW_SAFE"),
				optionPanelAlignment,
				AdvisorPanel.helpShowAdvisor,
				AdvisorPanel.advisorFontSize,
				AdvisorPanel.advisorIconSize
				)));
		return map;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						debugAutoRun,
						optionPanelAlignment,
						AdvisorPanel.helpShowAdvisor,
						AdvisorPanel.advisorFontSize,
						AdvisorPanel.advisorIconSize
						));
		return majorList;
	}
}
