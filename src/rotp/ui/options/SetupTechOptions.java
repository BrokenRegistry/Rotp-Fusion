package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class SetupTechOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = SETUP_TECH_OPTIONS_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("TECH_PLANET"),
				techBarren,
				techTundra,
				techDead,
				techInferno,
				techToxic,
				techIrradiated,
				techCloning,
				techAtmospheric,
				techGaia,
				techIndustry2
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("TECH_SPACE"),
				techStargate,
				techHyperspace,
				techThorium
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("TECH_COMBAT"),
				techCloaking,
				techTransport
				)));
		return map;
	};
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						techIrradiated,
						techAtmospheric,
						techStargate
						));
		return minorList;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						techIrradiated,
						techCloning,
						techAtmospheric,
						techGaia,
						techIndustry2,
						techStargate,
						techHyperspace,
						techThorium,
						techCloaking,
						techTransport
						));
		return majorList;
	}

}
