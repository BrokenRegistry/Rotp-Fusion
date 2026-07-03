package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class GovPopulationOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = GOVERNOR_POPULATION_UI_KEY;
	static final String HEAD_ID	  = GOV_UI;

	@Override public String optionId()	{ return OPTION_ID; }
	@Override public String headId()	{ return HEAD_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				GOVERNOR_BASE_OPTIONS_TITLE,
				maxGrowthMode,
				terraformEarly
				)));
		map.add(new SafeListParam(Arrays.asList(
				GOVERNOR_ADVANCED_OPTIONS_TITLE,

				HEADER_SPACER_50,
				new ParamTitle("GOVERNOR_TERRAFORM"),
				terraformFactoryPct,
				terraformPopulationPct,
				terraformPopulation,
				terraformCost2Income,

				HEADER_SPACER_50,
				new ParamTitle("GOVERNOR_COLONY_GROWTH"),
				compensateGrowth,
				minColonyGrowth,
				colonyEarlyBoostPct
				)));
		map.add(new SafeListParam(Arrays.asList(
				RELEVANT_TITLE,
				subsidyTerraformUse,
				subsidyNormalUse,

				LINE_SPACER_25,
				workerToFactoryROI,
				maxColoniesForROI,
				showTriggeredROI,

				LINE_SPACER_25,
				isManageableGovernor
				)));
		return map;
	};
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						maxGrowthMode,
						terraformEarly
						));
		return minorList;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						maxGrowthMode,
						terraformEarly,

						HEADER_SPACER_50,
						new ParamTitle("GOVERNOR_TERRAFORM"),
						terraformFactoryPct,
						terraformPopulationPct,
						terraformPopulation,
						terraformCost2Income,

						HEADER_SPACER_50,
						new ParamTitle("GOVERNOR_COLONY_GROWTH"),
						compensateGrowth,
						minColonyGrowth,
						colonyEarlyBoostPct
						));
		return majorList;
	}
}
