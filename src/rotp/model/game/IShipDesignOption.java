package rotp.model.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rotp.ui.util.ParamBoolean;
import rotp.ui.util.ParamInteger;
import rotp.ui.util.ParamList;

public interface IShipDesignOption extends IBaseOptsTools {
	String ASD_AI		= "AI";
	String ASD_SPECIES	= "SPECIES";
	String ASD_USER		= "USER";
	
	int ASD_SIZE	= 0;
	int ASD_SPACE	= 1;
	int ASD_OTHER	= 2;
	int ASD_BOOLEAN	= 3;
	int ASD_SPECIAL	= 4;

	int COST_MULT_S	= 0;
	int COST_MULT_M	= 1;
	int COST_MULT_L	= 2;
	int COST_MULT_H	= 3;
	int MODULE_SPACE		= 4;
	int SHIELD_WEIGHT_FB	= 5;
	int SHIELD_WEIGHT_D		= 6;
	int ECM_WEIGHT_FD		= 7;
	int ECM_WEIGHT_B		= 8;
	int MANEUVER_WEIGHT_BD	= 9;
	int MANEUVER_WEIGHT_F	= 10;
	int ARMOR_WEIGHT_FB		= 11;
	int ARMOR_WEIGHT_D		= 12;
	int SPECIALS_WEIGHT		= 13;
	int SPEED_MATCHING		= 14;
	int REINFORCED_ARMOR	= 15;
	int BIO_WEAPONS			= 16;
	int PREF_PULSARS		= 17;
	int PREF_CLOAK			= 18;
	int PREF_REPAIR			= 19;
	int PREF_INERTIAL		= 20;
	int PREF_MISS_SHIELD	= 21;
	int PREF_REPULSOR		= 22;
	int PREF_STASIS			= 23;
	int PREF_STREAM_PROJECTOR	= 24;
	int PREF_WARP_DISSIPATOR	= 25;
	int PREF_TECH_NULLIFIER	= 26;
	int PREF_BEAM_FOCUS		= 27;

	ParamBoolean autoTagAutoColon		= new ParamBoolean(AUTO_SHIP_DESIGN, "AUTO_TAG_COLON", false)
			.isCfgFile(true);
	default boolean autoTagAutoColon()	{ return autoTagAutoColon.get(); }
	ParamBoolean autoTagAutoScout		= new ParamBoolean(AUTO_SHIP_DESIGN, "AUTO_TAG_SCOUT", false)
			.isCfgFile(true);
	default boolean autoTagAutoScout()	{ return autoTagAutoScout.get(); }
	
	ParamShipDesignMode autoShipDesignSize		= new ParamShipDesignMode( "SIZE");
	ParamShipDesignMode autoShipDesignSpace		= new ParamShipDesignMode( "SPACE");
	ParamShipDesignMode autoShipDesignBoolean	= new ParamShipDesignMode( "BOOLEAN");
	ParamShipDesignMode autoShipDesignSpecial	= new ParamShipDesignMode( "SPECIAL");
	default boolean useShipDesignModsSpace()	{ return !autoShipDesignSpace.isASDBest(); }

	ParamInteger shipDesignSpecialTruePct		= new ParamInteger(AUTO_SHIP_DESIGN, "SPECIAL_TRUE", 100)
			.pctValue(true).setLimits(0, 1000).setIncrements(1, 5, 20);

	ParamInteger shipDesignSpecialFalsePct		= new ParamInteger(AUTO_SHIP_DESIGN, "SPECIAL_FALSE", 0)
			.pctValue(true).setLimits(0, 100).setIncrements(1, 5, 20);

	paramShipDesign shipDesignCostMultSmall		= new paramShipDesign(ASD_SIZE, COST_MULT_S, "COST_MULT_SMALL", 100);

	paramShipDesign shipDesignCostMultMedium	= new paramShipDesign(ASD_SIZE, COST_MULT_M, "COST_MULT_MEDIUM", 100);

	paramShipDesign shipDesignCostMultLarge		= new paramShipDesign(ASD_SIZE, COST_MULT_L, "COST_MULT_LARGE", 100);

	paramShipDesign shipDesignCostMultHuge		= new paramShipDesign(ASD_SIZE, COST_MULT_H, "COST_MULT_HUGE", 100);

	paramShipDesign shipDesignModuleSpace		= new paramShipDesign(ASD_OTHER, MODULE_SPACE, "MODULE_SPACE", 50)
			.setLimits(10, 100).setIncrements(1, 5, 10);

	paramShipDesign shipDesignShieldFightBomb	= new paramShipDesign(ASD_SPACE, SHIELD_WEIGHT_FB, "SHIELD_WEIGHT_FB", 400);

	paramShipDesign shipDesignShieldDestroyer	= new paramShipDesign(ASD_SPACE, SHIELD_WEIGHT_D, "SHIELD_WEIGHT_D", 400);

	paramShipDesign shipDesignEcmFightDestroy	= new paramShipDesign(ASD_SPACE, ECM_WEIGHT_FD, "ECM_WEIGHT_FD", 300);

	paramShipDesign shipDesignEcmBomber			= new paramShipDesign(ASD_SPACE, ECM_WEIGHT_B, "ECM_WEIGHT_B", 300);

	paramShipDesign shipDesignManeuverBD		= new paramShipDesign(ASD_SPACE, MANEUVER_WEIGHT_BD, "MANEUVER_WEIGHT_BD", 200);

	paramShipDesign shipDesignManeuverFighter	= new paramShipDesign(ASD_SPACE, MANEUVER_WEIGHT_F, "MANEUVER_WEIGHT_F", 200);

	paramShipDesign shipDesignArmorFightBomb	= new paramShipDesign(ASD_SPACE, ARMOR_WEIGHT_FB, "ARMOR_WEIGHT_FB", 300);

	paramShipDesign shipDesignArmorDestroyer	= new paramShipDesign(ASD_SPACE, ARMOR_WEIGHT_D, "ARMOR_WEIGHT_D", 300);

	paramShipDesign shipDesignSpecialWeight		= new paramShipDesign(ASD_SPACE, SPECIALS_WEIGHT, "SPECIALS_WEIGHT", 400);

	paramShipDesign shipDesignSpeedMatching		= new paramShipDesign(ASD_BOOLEAN, SPEED_MATCHING, "SPEED_MATCHING", 0);

	paramShipDesign shipDesignReinforcedArmor	= new paramShipDesign(ASD_BOOLEAN, REINFORCED_ARMOR, "REINFORCED_ARMOR", 100);

	paramShipDesign shipDesignBioWeapon			= new paramShipDesign(ASD_BOOLEAN, BIO_WEAPONS, "BIO_WEAPONS", 100);

	paramShipDesign shipDesignPrefPulsar		= new paramShipDesign(ASD_SPECIAL, PREF_PULSARS, "PREF_PULSARS", 100);

	paramShipDesign shipDesignPrefCloak			= new paramShipDesign(ASD_SPECIAL, PREF_CLOAK, "PREF_CLOAK", 100);

	paramShipDesign shipDesignPrefRepair		= new paramShipDesign(ASD_SPECIAL, PREF_REPAIR, "PREF_REPAIR", 100);

	paramShipDesign shipDesignPrefInertial		= new paramShipDesign(ASD_SPECIAL, PREF_INERTIAL, "PREF_INERTIAL", 100);

	paramShipDesign shipDesignPrefMissShield	= new paramShipDesign(ASD_SPECIAL, PREF_MISS_SHIELD, "PREF_MISS_SHIELD", 100);

	paramShipDesign shipDesignPrefRepulsor		= new paramShipDesign(ASD_SPECIAL, PREF_REPULSOR, "PREF_REPULSOR", 100);

	paramShipDesign shipDesignPrefStasis		= new paramShipDesign(ASD_SPECIAL, PREF_STASIS, "PREF_STASIS", 100);

	paramShipDesign shipDesignPrefStreamProj	= new paramShipDesign(ASD_SPECIAL, PREF_STREAM_PROJECTOR, "PREF_STREAM_PROJ", 100);

	paramShipDesign shipDesignPrefWarpDissip	= new paramShipDesign(ASD_SPECIAL, PREF_WARP_DISSIPATOR, "PREF_WARP_DISSIP", 100);

	paramShipDesign shipDesignPrefTechNull		= new paramShipDesign(ASD_SPECIAL, PREF_TECH_NULLIFIER, "PREF_TECH_NULLIF", 100);

	paramShipDesign shipDesignPrefBeamFocus		= new paramShipDesign(ASD_SPECIAL, PREF_BEAM_FOCUS, "PREF_BEAM_FOCUS", 100);

	default float[] shipDesignMod(float[] species)	{
		boolean	customSize		= autoShipDesignSize.get().equals(ASD_USER);
		boolean	customSpace		= autoShipDesignSpace.get().equals(ASD_USER);
		boolean	customBoolean	= autoShipDesignBoolean.get().equals(ASD_USER);
		boolean	customSpecial	= autoShipDesignSpecial.get().equals(ASD_USER);
		boolean	speciesSize		= autoShipDesignSize.get().equals(ASD_SPECIES);
		boolean	speciesSpace	= autoShipDesignSpace.get().equals(ASD_SPECIES);
		boolean	speciesBoolean	= autoShipDesignBoolean.get().equals(ASD_SPECIES);
		boolean	speciesSpecial	= autoShipDesignSpecial.get().equals(ASD_SPECIES);
		float	trueValue		= shipDesignSpecialTruePct.get()/100f;
		float	falseValue		= shipDesignSpecialFalsePct.get()/100f;
		float[] shipDesignMod	= new float[28];
		shipDesignMod[COST_MULT_S]		= 1f;
		shipDesignMod[COST_MULT_M]		= 1f;
		shipDesignMod[COST_MULT_L]		= 1f;
		shipDesignMod[COST_MULT_H]		= 1f;
		shipDesignMod[MODULE_SPACE]		= 0.5f;
		shipDesignMod[SHIELD_WEIGHT_FB]	= 4f;
		shipDesignMod[SHIELD_WEIGHT_D]	= 4f;
		shipDesignMod[ECM_WEIGHT_FD]	= 3f;
		shipDesignMod[ECM_WEIGHT_B]		= 3f;
		shipDesignMod[MANEUVER_WEIGHT_BD]	= 2f;
		shipDesignMod[MANEUVER_WEIGHT_F]	= 2f;
		shipDesignMod[ARMOR_WEIGHT_FB]	= 3f;
		shipDesignMod[ARMOR_WEIGHT_D]	= 3f;
		shipDesignMod[SPECIALS_WEIGHT]	= 4f;
		shipDesignMod[SPEED_MATCHING]	= 1f;
		shipDesignMod[REINFORCED_ARMOR]	= 1f;
		shipDesignMod[BIO_WEAPONS]		= 1f;
		shipDesignMod[PREF_PULSARS]		= 1f;
		shipDesignMod[PREF_CLOAK]		= 10f;
		shipDesignMod[PREF_REPAIR]		= 1f;
		shipDesignMod[PREF_INERTIAL]	= 1f;
		shipDesignMod[PREF_MISS_SHIELD]	= 1f;
		shipDesignMod[PREF_REPULSOR]	= 1f;
		shipDesignMod[PREF_STASIS]		= 1f;
		shipDesignMod[PREF_STREAM_PROJECTOR]= 1f;
		shipDesignMod[PREF_WARP_DISSIPATOR]	= 1f;
		shipDesignMod[PREF_TECH_NULLIFIER]	= 1f;
		shipDesignMod[PREF_BEAM_FOCUS]		= 1f;

		for (paramShipDesign p : shipDesignList()) {
			switch (p.type) {
				case ASD_SIZE:
					if (customSize)
						shipDesignMod[p.id] = p.getFloat();
					else if (speciesSize)
						shipDesignMod[p.id] = species[p.id];
					continue;
				case ASD_SPACE:
				case ASD_OTHER:
					if (customSpace)
						shipDesignMod[p.id] = p.getFloat();
					else if (speciesSpace)
						shipDesignMod[p.id] = species[p.id];
					continue;
				case ASD_SPECIAL:
					if (customSpecial)
						shipDesignMod[p.id] = p.getFloat();
					else if (speciesSpecial)
						shipDesignMod[p.id] = species[p.id] == 0? falseValue : trueValue;
					continue;
				case ASD_BOOLEAN:
					if (customBoolean)
						shipDesignMod[p.id] = p.getFloat();
					else if (speciesBoolean)
						shipDesignMod[p.id] = species[p.id] == 0? falseValue : trueValue;
					continue;
			}
		}
		return shipDesignMod;
	}
	default List<paramShipDesign> shipDesignList()		{
		return new ArrayList<>(Arrays.asList(
				shipDesignCostMultSmall,	// 0
				shipDesignCostMultMedium,	// 1
				shipDesignCostMultLarge,	// 2
				shipDesignCostMultHuge,		// 3
				shipDesignModuleSpace,		// 4
				shipDesignShieldFightBomb,	// 5
				shipDesignShieldDestroyer,	// 6
				shipDesignEcmFightDestroy,	// 7
				shipDesignEcmBomber,		// 8
				shipDesignManeuverBD,		// 9
				shipDesignManeuverFighter,	// 10
				shipDesignArmorFightBomb,	// 11
				shipDesignArmorDestroyer,	// 12
				shipDesignSpecialWeight,	// 13
				shipDesignSpeedMatching,	// 14
				shipDesignReinforcedArmor,	// 15
				shipDesignBioWeapon,		// 16
				shipDesignPrefPulsar,		// 17
				shipDesignPrefCloak,		// 18
				shipDesignPrefRepair,		// 19
				shipDesignPrefInertial,		// 20
				shipDesignPrefMissShield,	// 21
				shipDesignPrefRepulsor,		// 22
				shipDesignPrefStasis,		// 23
				shipDesignPrefStreamProj,	// 24
				shipDesignPrefWarpDissip,	// 25
				shipDesignPrefTechNull,		// 26
				shipDesignPrefBeamFocus		// 27
				));
	}
	final class ParamShipDesignMode extends ParamList	{
		private ParamShipDesignMode(String name)	{
			super(AUTO_SHIP_DESIGN, name, ASD_AI);
			showFullGuide(true);
			isValueInit(false);
			put(ASD_AI,			AUTO_SHIP_DESIGN + ASD_AI);
			put(ASD_SPECIES,	AUTO_SHIP_DESIGN + ASD_SPECIES);
//			put(ASD_USER,		AUTO_SHIP_DESIGN + ASD_USER);
		}
		private boolean isASDBest()	{ return get().equals(ASD_AI); }
	}
	final class paramShipDesign extends ParamInteger	{
		private final int type;
		private final int id;
		paramShipDesign (int type, int id, String name, Integer defaultValue)	{
			super(AUTO_SHIP_DESIGN, name, defaultValue);
			this.type = type;
			this.id	  = id;
			pctValue(true);
			switch (type) {
				case ASD_SIZE:
					setLimits(0, 500);
					setIncrements(1, 5, 20);
					break;
				case ASD_SPACE:
					setLimits(0, 1000);
					setIncrements(1, 5, 20);
					break;
				case ASD_SPECIAL:
					setLimits(0, 1000);
					setIncrements(1, 5, 20);
					break;
				case ASD_BOOLEAN:
					setLimits(0, 100);
					setIncrements(100, 100, 100);
					break;
				case ASD_OTHER:
				default:
					break;
			}
		}
		@Override public float getFloat()	{ return get()/100f; }
		@Override public paramShipDesign setLimits(Integer min, Integer max) {
			super.setLimits(min, max);
			return this;
		}
		@Override public paramShipDesign setIncrements(Integer baseInc, Integer shiftInc, Integer ctrlInc) {
			super.setIncrements(baseInc, shiftInc, ctrlInc, shiftInc*ctrlInc/baseInc);
			return this;
		}
		@Override public paramShipDesign pctValue(boolean pctValue) {
			super.pctValue(pctValue);
			return this;
		}
	}
}
