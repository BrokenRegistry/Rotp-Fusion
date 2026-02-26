package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class ShipRules extends AbstractOptionsSubUI {
	static final String OPTION_ID = SHIP_RULES_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("SHIP_DESIGN"),
				shipSpaceFactor,
				missileShipModifier,

				LINE_SPACER_25,
				scrapRefundOption,
				scrapRefundFactor,

				new ParamTitle("PREF_SHIP_SIZE_IMPACT"),
				prefShipSizeImpacts,
				prefShipSizeCostFactor,
				prefShipSizeSpaceFactor,
				prefShipSizeHitFactor
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("BOMBARD_AND_LANDING"),
				targetBombard,
				bombingTarget,

				LINE_SPACER_25,
				maxLandingTroops,
				maxLandingTroopsAmount,
				maxLandingTroopsFactor,
				maxLandingTroopsIAFactor
				)));
		map.add(new SafeListParam(Arrays.asList(
				RELEVANT_TITLE,
				warpSpeed,
				fuelRange,

				LINE_SPACER_25,
				dynamicRange,
				dynamicRangePerMille,
				dynamicRangeQuad,

				LINE_SPACER_25,
				missileBaseModifier
				)));
		return map;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						shipSpaceFactor,
						missileBaseModifier,
						missileShipModifier,
						targetBombard,
						bombingTarget,
						rallyCombat,
						rallyCombatLoss,
						maxLandingTroops,
						maxLandingTroopsAmount,
						maxLandingTroopsFactor
						));
		return majorList;
	}
}
