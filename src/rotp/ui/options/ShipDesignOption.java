package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

final class ShipDesignOption extends AbstractOptionsSubUI {
	static final String OPTION_ID = SHIP_DESIGN_OPTIONS_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }
	@Override public boolean isCfgFile()		{ return true; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		SafeListParam list;

		list = new SafeListParam("");
		list.addAll(AllSubUI.getHandle(AUTO_SEND_FLEET_UI_KEY).getUiMajor(false));
//		list.addAll(new SafeListParam(Arrays.asList(
//				HEADER_SPACER_100,
//				new ParamTitle(AUTO_SHIP_DESIGN + "SIZE"),
//				autoShipDesignSize,
//				HEADER_SPACER_50,
//				shipDesignCostMultSmall,
//				shipDesignCostMultMedium,
//				shipDesignCostMultLarge,
//				shipDesignCostMultHuge
//				)));
		map.add(list);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle(AUTO_SHIP_DESIGN + "SPACE"),
				autoShipDesignSpace,

				HEADER_SPACER_100,
				RELEVANT_TITLE,
				keepShipDesignName
				)));

//		map.add(new SafeListParam(Arrays.asList(
//				new ParamTitle(AUTO_SHIP_DESIGN + "SPACE"),
//				autoShipDesignSpace,
//				LINE_SPACER_25,
//				shipDesignModuleSpace,
//				shipDesignShieldFightBomb,
//				shipDesignShieldDestroyer,
//				shipDesignEcmFightDestroy,
//				shipDesignEcmBomber,
//				shipDesignManeuverBD,
//				shipDesignManeuverFighter,
//				shipDesignArmorFightBomb,
//				shipDesignArmorDestroyer,
//				shipDesignSpecialWeight,
//
//				HEADER_SPACER_100,
//				new ParamTitle(AUTO_SHIP_DESIGN + "PREF"),
//				autoShipDesignBoolean,
//				LINE_SPACER_25,
//				shipDesignSpeedMatching,
//				shipDesignReinforcedArmor,
//				shipDesignBioWeapon
//				)));

//		map.add(new SafeListParam(Arrays.asList(
//				new ParamTitle(AUTO_SHIP_DESIGN + "SPECIAL"),
//				autoShipDesignSpecial,
//				shipDesignSpecialTruePct,
//				shipDesignSpecialFalsePct,
//				LINE_SPACER_25,
//				shipDesignPrefPulsar,
//				shipDesignPrefCloak,
//				shipDesignPrefRepair,
//				shipDesignPrefInertial,
//				shipDesignPrefMissShield,
//				shipDesignPrefRepulsor,
//				shipDesignPrefStasis,
//				shipDesignPrefStreamProj,
//				shipDesignPrefWarpDissip,
//				shipDesignPrefTechNull,
//				shipDesignPrefBeamFocus,
//
//				HEADER_SPACER_100,
//				RELEVANT_TITLE,
//				keepShipDesignName
//				)));
		return map;
	};
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						autoShipDesignSpace //,
//						autoShipDesignSize,
//						autoShipDesignBoolean,
//						autoShipDesignSpecial
						));
		return minorList;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						autoShipDesignSpace //,
//						autoShipDesignSize,
//						autoShipDesignBoolean,
//						autoShipDesignSpecial,
//						LINE_SPACER_25,
//						shipDesignSpecialTruePct,
//						shipDesignSpecialFalsePct
						));
		return majorList;
	}

}
