package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class GameDifficulty extends AbstractOptionsSubUI {
	static final String OPTION_ID = GAME_DIFFICULTY_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("DIRECT_DIFFICULTY"),
				difficultySelection,
				customDifficulty,
				dynamicDifficulty
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("GAME_VARIANS"),
				challengeMode,
				darkGalaxy
				)));
		return map;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						difficultySelection,
						customDifficulty,
						dynamicDifficulty,
						challengeMode
						));
		return majorList;
	}
}
