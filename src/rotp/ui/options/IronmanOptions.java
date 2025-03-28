package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class IronmanOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = IRONMAN_OPTIONS_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }
	@Override public boolean isCfgFile()		{ return true; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("IRONMAN_MAIN"),
				ironmanMode
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("IRONMAN_CUSTOM"),
				ironmanNoLoad,
				ironmanLoadDelay,
				allowSpeciesDetails
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("IRONMAN_RANDOM"),
				fixedEventsMode,
				persistentArtifact,
				researchMoo1,
				persistentRNG
				)));
		return map;
	};
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						ironmanMode
						));
		return minorList;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						ironmanMode,
						ironmanNoLoad,
						ironmanLoadDelay,
						allowSpeciesDetails,
						LINE_SPACER_25,
						fixedEventsMode,
						persistentArtifact,
						researchMoo1,
						persistentRNG
						));
		return majorList;
	}

}
