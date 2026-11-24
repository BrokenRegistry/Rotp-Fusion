package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class SetupGalaxyOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = SETUP_GALAXY_OPTIONS_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("GALAXY_CONTENT"),
				galaxyAge,
				starDensity,
				fillerSystemsPct,
				looseNeighborhood,
				empiresSpreadingFactor,

				HEADER_SPACER_50,
				connectEmpires,
				maxEmpiresConnextionGap,
				removeNonRoutedConnections
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("GALAXY_SIZE"),
				minStarsPerEmpire,
				prefStarsPerEmpire,
				dynStarsPerEmpire
				)));
		return map;
	};
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						galaxyAge,
						starDensity,
						fillerSystemsPct
						));
		return minorList;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						minStarsPerEmpire,
						prefStarsPerEmpire,
						dynStarsPerEmpire,
						galaxyAge,
						starDensity,
						fillerSystemsPct,
						looseNeighborhood,

						LINE_SPACER_25,
						empiresSpreadingFactor,
						connectEmpires,
						maxEmpiresConnextionGap,
						removeNonRoutedConnections
						));
		return majorList;
	}

}
