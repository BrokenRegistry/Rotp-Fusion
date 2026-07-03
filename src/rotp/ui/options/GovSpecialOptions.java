package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class GovSpecialOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = GOVERNOR_SPECIAL_UI_KEY;
	static final String HEAD_ID	  = GOV_UI;
	static boolean old = false;

	@Override public String optionId()	{ return OPTION_ID; }
	@Override public String headId()	{ return HEAD_ID; }

	@Override public SafeListPanel optionsMap()	{
		old = false;
		return old? optionsMap1() : optionsMap2();
	}
	private SafeListPanel optionsMap1()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("GOVERNOR_TERRAFORM"),
				terraformFactoryPct,
				terraformPopulationPct,
				terraformPopulation,
				terraformCost2Income,

				HEADER_SPACER_100,
				new ParamTitle("GOVERNOR_COLONY_GROWTH"),
				maxGrowthMode,
				compensateGrowth,
				minColonyGrowth,
				colonyEarlyBoostPct,

				LINE_SPACER_25,
				earlyBaseBuilding,
				earlyBaseBoostPct
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("GOVERNOR_SUBSIDIES"),
				subsidyTerraformUse,
				subsidyNormalUse,

				HEADER_SPACER_100,
				new ParamTitle("GOVERNOR_OTHER_LIMITS"),
				defaultShipTakePct,
				workerToFactoryROI,
				maxColoniesForROI,
				showTriggeredROI,

				LINE_SPACER_25,
				colonyDistanceWeight,

				LINE_SPACER_25,
				trainSpiesASAP,
				contactUpdateSpending
				)));

		map.add(AllSubUI.getHandle(AUTO_SEND_FLEET_UI_KEY).getUiMajor(false));
		return map;
	};
	private SafeListPanel optionsMap2()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		SafeListParam list = new SafeListParam("");
		list.addAll(AllSubUI.getHandle(GOVERNOR_TRANSPORT_UI_KEY).getUiMinor(false));
		list.add(HEADER_SPACER_100);
		list.addAll(AllSubUI.getHandle(GOVERNOR_POPULATION_UI_KEY).getUiMajor(false));
		map.add(list);

		list = new SafeListParam("");
		list.addAll(AllSubUI.getHandle(GOVERNOR_BUILDING_UI_KEY).getUiMajor(false));
		list.add(HEADER_SPACER_100);
		list.add(AllSubUI.getHandle(GOVERNOR_ASPECT_UI_KEY).getUI());
		map.add(list);

		list = new SafeListParam("");
		list.addAll(AllSubUI.getHandle(GOVERNOR_TAXES_UI_KEY).getUiMajor(false));
		list.add(HEADER_SPACER_100);
		list.addAll(AllSubUI.getHandle(GOVERNOR_INTELLIGENCE_UI_KEY).getUiMajor(false));
		map.add(list);

		list = new SafeListParam("");
		list.addAll(AllSubUI.getHandle(AUTO_SEND_FLEET_UI_KEY).getUiMajor(false));
		list.addAll(Arrays.asList(
				HEADER_SPACER_100,
				new ParamTitle(HEAD_ID + "OTHER_OPTIONS"),
				isManageableGovernor,

				LINE_SPACER_25,
				governorByDefault,
				auto_Apply
				));
		map.add(list);

		return map;
	};
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						workerToFactoryROI
						));
		return minorList;
	}
}
