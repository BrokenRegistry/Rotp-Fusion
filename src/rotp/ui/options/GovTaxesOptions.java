package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;

final class GovTaxesOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = GOVERNOR_TAXES_UI_KEY;
	static final String HEAD_ID	  = GOV_UI;

	@Override public String optionId()	{ return OPTION_ID; }
	@Override public String headId()	{ return HEAD_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				GOVERNOR_BASE_OPTIONS_TITLE,
				reserveForPlayer,
				autoSpendOnArtefacts,
				autoSpendOnNewColonies,
				reserveFromRich
				)));
		map.add(new SafeListParam(Arrays.asList(
				GOVERNOR_ADVANCED_OPTIONS_TITLE,
				autoSpendOnNewColoniesFirst,
				autospendMaxIndustryPct,

				LINE_SPACER_25,
				subsidyTerraformUse,
				subsidyNormalUse
				)));
		map.add(new SafeListParam(Arrays.asList(
				RELEVANT_TITLE,
				divertExcessToResearch,
				maxMissingPopulation,
				maxMissingFactories,

				LINE_SPACER_25,
				isManageableGovernor
				)));
		return map;
	};
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						reserveForPlayer,
						autoSpendOnArtefacts,
						autoSpendOnNewColonies,
						reserveFromRich,

						LINE_SPACER_25,
						divertExcessToResearch
						));
		return minorList;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						reserveForPlayer,
						autoSpendOnArtefacts,
						autoSpendOnNewColonies,
						reserveFromRich,

						LINE_SPACER_25,
						autoSpendOnNewColoniesFirst,
						autospendMaxIndustryPct,

						LINE_SPACER_25,
						subsidyTerraformUse,
						subsidyNormalUse,

						LINE_SPACER_25,
						divertExcessToResearch,
						maxMissingPopulation,
						maxMissingFactories
						));
		return majorList;
	}
}
