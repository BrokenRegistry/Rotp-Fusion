package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;

final class GovBuildingOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = GOVERNOR_BUILDING_UI_KEY;
	static final String HEAD_ID	  = GOV_UI;

	@Override public String optionId()	{ return OPTION_ID; }
	@Override public String headId()	{ return HEAD_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				GOVERNOR_BASE_OPTIONS_TITLE,
				missileBasesMin,
				shieldAlones,
				shipBuilding
				)));
		map.add(new SafeListParam(Arrays.asList(
				GOVERNOR_ADVANCED_OPTIONS_TITLE,
				earlyBaseBuilding,
				earlyBaseBoostPct,

				LINE_SPACER_25,
				workerToFactoryROI,
				maxColoniesForROI,
				showTriggeredROI,

				LINE_SPACER_25,
				defaultShipTakePct
				)));
		map.add(new SafeListParam(Arrays.asList(
				RELEVANT_TITLE,
				starGateOption,

				LINE_SPACER_25,
				subsidyTerraformUse,
				subsidyNormalUse,

				LINE_SPACER_25,
				isManageableGovernor
				)));
		return map;
	};
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						missileBasesMin,
						shieldAlones,
						shipBuilding,

						LINE_SPACER_25,
						starGateOption
						));
		return minorList;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						missileBasesMin,
						shieldAlones,
						shipBuilding,

						LINE_SPACER_25,
						starGateOption,

						LINE_SPACER_25,
						earlyBaseBuilding,
						earlyBaseBoostPct,

						LINE_SPACER_25,
						workerToFactoryROI,
						maxColoniesForROI,
						showTriggeredROI,

						LINE_SPACER_25,
						defaultShipTakePct
						));
		return majorList;
	}
}
