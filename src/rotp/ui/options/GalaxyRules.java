package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class GalaxyRules extends AbstractOptionsSubUI {
	static final String OPTION_ID = GALAXY_RULES_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("PLANET_RULES"),
				popGrowthFactor,
				terraforming,
				colonizing,
				researchRate
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("SPACE_RULES"),
				warpSpeed,
				warpSpeedPct,
				fuelRange,

				LINE_SPACER_25,
				dynamicRange,
				dynamicRangePerMille,
				dynamicRangeQuad,

				LINE_SPACER_25,
				darkGalaxy,

				LINE_SPACER_25,
				realNebulaSize,
				realNebulaShape
				)));
		SafeListParam list = new SafeListParam(Arrays.asList(
				new ParamTitle("RETREAT_RULES"),
				retreatDestination,
				hyperComRetreatExtended,
				noEnemyOnRetreatDestination,

//				LINE_SPACER_25,
//				markRetreatOnArrivalAsRetreating,
//				markDiplomaticRetreatAsRetreating,

				HEADER_SPACER_100
				));
		list.add(RELEVANT_TITLE);
		list.addAll(Arrays.asList(
				specialPeaceTreaty,
				realNebulaeOpacity
				));
		map.add(list);

		return map;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						darkGalaxy,
						warpSpeed,
						warpSpeedPct,
						fuelRange,
						dynamicRange,
						realNebulaSize,
						realNebulaShape,
						realNebulaeOpacity,
						researchRate,
						popGrowthFactor
						));
		return majorList;
	}
}
