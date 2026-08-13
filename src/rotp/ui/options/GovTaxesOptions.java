package rotp.ui.options;

import java.util.Arrays;

import rotp.model.empires.Empire;
import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class GovTaxesOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = GOVERNOR_TAXES_UI_KEY;
	static final String HEAD_ID	  = GOV_UI;

	@Override public String optionId()	{ return OPTION_ID; }
	@Override public String headId()	{ return HEAD_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("GOVERNOR_FUNDRAISING"),
				governorRaiseFunds,
				redoBudgetRaiseAllowed,

				HEADER_SPACER_50,
				redoBudgetOnOptionChange,
				redoBudgetOnTransport,
				redoBudgetOnTaxChange,
				redoBudgetOnColony,
				redoBudgetOnSpendings,

				HEADER_SPACER_50,
				reserveForPlayer,
				reservePlayerPerMille,

				HEADER_SPACER_50,
				reserveNextTurn,
				reserveNextTurnPct,

				HEADER_SPACER_50,
				reserveMax,
				reserveMaxPct,

				HEADER_SPACER_50,
				reserveFromRich
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("GOVERNOR_SPENDING"),
				governorGrantFunds,
				redoBudgetGrantAllowed,

				HEADER_SPACER_50,
				carryUnfunded,
				fundHelpRandomEvent,
				autoSpendOnArtefacts,
				autoSpendOnNewColonies,

				HEADER_SPACER_50,
				autoSpendOnNewColoniesFirst,
				autospendMaxIndustryPct,

				HEADER_SPACER_50,
				subsidyTerraformUse,
				subsidyNormalUse
				)));
		map.add(new SafeListParam(Arrays.asList(
				RELEVANT_TITLE,
				divertExcessToResearch,
				Empire.playerTaxLevelPct,

				HEADER_SPACER_50,
				maxMissingPopulation,
				maxMissingFactories,

				HEADER_SPACER_50,
				isManageableGovernor,

				HEADER_SPACER_100,
				autospendImmediateTransfer
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
						governorRaiseFunds,
						redoBudgetRaiseAllowed,
						governorGrantFunds,
						redoBudgetGrantAllowed,

						LINE_SPACER_25,
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
