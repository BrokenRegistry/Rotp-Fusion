package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class MoO1GalaxyOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = MOO1_GALAXY_OPTIONS_UI_KEY;
	
	@Override public String optionId()			{ return OPTION_ID; }
	@Override public boolean isCfgFile()		{ return false; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("MOO1_GALAXY_SIZE"),
				sizeSelection,
				minStarsPerEmpire,
				prefStarsPerEmpire,
				empiresSpreadingFactor,
				orionToEmpireModifier
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("MOO1_HOME_VICINITY"),
				firstRingRadius,
				firstRingSystemNumber,
				firstRingHabitable,
				secondRingHabitable
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("MOO1_RANDOMNESS"),
				researchMoo1,
				persistentArtifact,
				persistentRNG,

				HEADER_SPACER_50,
				new ParamTitle("DEBUG_RELEVANT"),
				defaultSettings
				)));
		return map;
	}
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						defaultSettings
						));
		return minorList;
	}
}
