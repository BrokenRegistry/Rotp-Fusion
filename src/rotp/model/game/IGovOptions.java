package rotp.model.game;

import static rotp.model.game.DefaultValues.MOO1_DEFAULT;
import static rotp.model.game.DefaultValues.ROTP_DEFAULT;

import rotp.model.galaxy.Galaxy;
import rotp.model.game.GovernorOptions.GatesGovernor;
import rotp.ui.util.ParamBoolean;
import rotp.ui.util.ParamFloat;
import rotp.ui.util.ParamInteger;
import rotp.ui.util.ParamList;

public interface IGovOptions {
	
	// ==================== Governor Options ====================
	//
	String GOV_UI		= "GOVERNOR_";
	int	NOT_GOVERNOR	= 0;
	int	NO_REFRESH		= 0;
	int GOV_REFRESH		= 1;
	int GOV_RESET		= 2;
	
	// AutoTransport Options
	ParamBoolean autoTransportAI	= new ParamBoolean(GOV_UI, "AUTO_TRANSPORT", false);
	ParamBoolean autotransportFull	= new ParamBoolean(GOV_UI, "AUTOTRANSPORT_GOV", false);
	ParamBoolean autotransportAll	= new ParamBoolean(GOV_UI, "TRANSPORT_UNGOVERNED", false);
	ParamBoolean transportNoRich	= new ParamBoolean(GOV_UI, "TRANSPORT_RICH_OFF", true);
	ParamBoolean transportPoorX2	= new ParamBoolean(GOV_UI, "TRANSPORT_POOR_DBL", true);
	ParamInteger transportMaxDist	= new ParamInteger(GOV_UI, "TRANSPORT_MAX_TURNS", 5)
			.setLimits(1, 15)
			.setIncrements(1, 3, 5);

	// StarGates Options
	// Using an Enum object instead of a list will break the game save if the enum is changed! 
	ParamList	 starGateOption		= initStarGateOption();
	static ParamList initStarGateOption() {
		ParamList list = new ParamList(GOV_UI, "STARGATES_OPTIONS", GatesGovernor.Rich.name());
		list.showFullGuide(true);
		for (GatesGovernor value: GatesGovernor.values())
			list.put(value.name(), GOV_UI + "STARGATES_" + value.name().toUpperCase());
		return list;
	}

	// Colony Options
	ParamInteger missileBasesMin	= new ParamInteger(GOV_UI, "MIN_MISSILE_BASES", 0)
			.setLimits(0, 1000)
			.setIncrements(1, 5, 20);
	ParamBoolean shieldAlones		= new ParamBoolean(GOV_UI, "SHIELD_WITHOUT_BASES", false);
	ParamBoolean autoSpend			= new ParamBoolean(GOV_UI, "AUTOSPEND", false);
	ParamInteger reserveForSlow		= new ParamInteger(GOV_UI, "RESERVE", 0)
			.setLimits(0, 100000)
			.setIncrements(10, 50, 200);
	ParamBoolean shipBuilding		= new ParamBoolean(GOV_UI, "SHIP_BUILDING", true);
	ParamBoolean maxGrowthMode		= new ParamBoolean(GOV_UI, "LEGACY_GROWTH_MODE", true);
	ParamInteger terraformEarly		= new ParamInteger(GOV_UI, "TERRAFORM_EARLY", 0)
			.setLimits(0, 400)
			.setIncrements(1, 5, 20)
			.pctValue(true);
	ParamBoolean followColonyRequests	= new ParamBoolean(GOV_UI, "FOLLOW_COLONY_REQUESTS", false);
	ParamBoolean reserveFromRich	= new ReserveFromRich();
	class ReserveFromRich extends ParamBoolean {
		ReserveFromRich() { super(GOV_UI, "RESERVE_FROM_RICH", false); }
		@Override public Boolean set(Boolean b)	{
			Boolean val = super.set(b);
			Galaxy galaxy = GameSession.instance().galaxy();
			if (galaxy != null)
				galaxy.player().redoGovTurnDecisionsRich();
			return val;
		}
	}

	// Intelligence Options
	ParamBoolean auto_Infiltrate	= new ParamBoolean(GOV_UI, "AUTO_INFILTRATE", true);
	ParamBoolean auto_Spy			= new ParamBoolean(GOV_UI, "AUTO_SPY", false);
	ParamBoolean respectPromises	= new ParamBoolean(GOV_UI, "SPARE_XENOPHOBES", false); // Keep the name for backward compatibility

	// Aspect Options
	ParamBoolean originalPanel		= new ParamBoolean(GOV_UI, "ORIGINAL_PANEL", false);
	ParamBoolean customSize			= new ParamBoolean(GOV_UI, "CUSTOM_SIZE", true);
	ParamInteger brightnessPct		= new ParamInteger(GOV_UI, "BRIGHTNESS", 100)
			.setLimits(20, 300)
			.setIncrements(1, 5, 20)
			.pctValue(true);
	ParamInteger sizeFactorPct		= new ParamInteger(GOV_UI, "SIZE_FACTOR", 100)
			.setLimits(20, 200)
			.setIncrements(1, 5, 20)
			.pctValue(true);
	ParamInteger horizontalPosition	= new ParamInteger(GOV_UI, "POSITION_X", 0)
			.setLimits(null, null)
			.setIncrements(1, 5, 20);
	ParamInteger verticalPosition	= new ParamInteger(GOV_UI, "POSITION_Y", 0)
			.setLimits(null, null)
			.setIncrements(1, 5, 20);

	// Fleet Options
	ParamBoolean auto_Scout			= new ParamBoolean(GOV_UI, "AUTO_SCOUT", true);
	ParamInteger autoScoutCount		= new ParamInteger(GOV_UI, "AUTO_SCOUT_COUNT",	1)
			.setLimits(1, 9999)
			.setIncrements(1, 5, 20);
	ParamBoolean govAutoColonize	= new ParamBoolean(GOV_UI, "AUTO_COLONIZE", true);
	ParamInteger autoColonyCount	= new ParamInteger(GOV_UI, "AUTO_COLONY_COUNT", 1)
			.setLimits(1, 9999)
			.setIncrements(1, 5, 20);
	ParamBoolean auto_Attack		= new ParamBoolean(GOV_UI, "AUTO_ATTACK", false);
	ParamInteger autoAttackCount	= new ParamInteger(GOV_UI, "AUTO_ATTACK_COUNT", 1)
			.setLimits(1, 9999)
			.setIncrements(1, 5, 20);
    // if true, new colonies will have auto ship building set to "on"
	ParamBoolean autoShipsDefault	= new ParamBoolean(GOV_UI, "AUTOSHIPS_BY_DEFAULT", false);

	// Other Options
	ParamBoolean animatedImage		= new ParamBoolean(GOV_UI, "ANIMATED_IMAGE", true);
	ParamBoolean auto_Apply			= new ParamBoolean(GOV_UI, "AUTO_APPLY", true);
	ParamBoolean governorByDefault	= new ParamBoolean(GOV_UI, "ON_BY_DEFAULT", true)
			.setDefaultValue(MOO1_DEFAULT, false)
			.setDefaultValue(ROTP_DEFAULT, false);

	// Advanced Tuning options: Not in the floating windows
	ParamInteger workerToFactoryROI	= new ParamInteger(GOV_UI, "WORKER_TO_FACTORY_ROI", 150)
			.setLimits(100, 1000)
			.setIncrements(5, 20, 100);
	ParamInteger maxColoniesForROI	= new ParamInteger(GOV_UI, "MAX_COLONIES_FOR_ROI", 6)
			.setLimits(0, 1000)
			.setIncrements(1, 5, 20);
	ParamBoolean showTriggeredROI	= new ParamBoolean(GOV_UI, "SHOW_TRIGGERED_ROI", true);

	String INDUSTRY			= "INDUSTRY";
	String ECOLOGY			= "ECOLOGY";
	String PLANET_BASED		= "PLANET_BASED";
	String GOV_CHOICE		= "GOV_CHOICE";
	String SUBSIDY_NORMAL	= "SUBSIDY_NORMAL";
	ParamList subsidyNormalUse	= new ParamList(GOV_UI, SUBSIDY_NORMAL, GOV_CHOICE)
			.showFullGuide(true)
			.put(INDUSTRY,		SUBSIDY_NORMAL + "_" + INDUSTRY)
			.put(ECOLOGY,		SUBSIDY_NORMAL + "_" + ECOLOGY)
			.put(PLANET_BASED,	SUBSIDY_NORMAL + "_" + PLANET_BASED)
			.put(GOV_CHOICE,	SUBSIDY_NORMAL + "_" + GOV_CHOICE);

	String SUBSIDY_TERRAFORM	= "SUBSIDY_TFORM";
	ParamList subsidyTerraformUse	= new ParamList(GOV_UI, SUBSIDY_TERRAFORM, GOV_CHOICE)
			.showFullGuide(true)
			.put(INDUSTRY,		SUBSIDY_TERRAFORM + "_" + INDUSTRY)
			.put(ECOLOGY,		SUBSIDY_TERRAFORM + "_" + ECOLOGY)
			.put(PLANET_BASED,	SUBSIDY_TERRAFORM + "_" + PLANET_BASED)
			.put(GOV_CHOICE,	SUBSIDY_TERRAFORM + "_" + GOV_CHOICE);

	ParamInteger terraformFactoryPct	= new ParamInteger(GOV_UI, "TERRAFORM_FACTORY_PCT", 60)
			.setLimits(0, 100)
			.setIncrements(1, 5, 20);
	ParamInteger terraformPopulationPct	= new ParamInteger(GOV_UI, "TERRAFORM_POP_PCT", 75)
			.setLimits(0, 100)
			.setIncrements(1, 5, 20);
	ParamInteger terraformPopulation	= new ParamInteger(GOV_UI, "TERRAFORM_POPULATION", 5)
			.setLimits(0, 300)
			.setIncrements(1, 5, 20);
	ParamInteger terraformCost2Income	= new ParamInteger(GOV_UI, "TERRAFORM_COST", 100)
			.setLimits(0, 500)
			.setIncrements(1, 5, 20);
	ParamInteger defaultShipTakePct		= new ParamInteger(GOV_UI, "DEFAULT_SHIP_TAKE", 100)
			.setLimits(10, 100)
			.setIncrements(1, 5, 20);

	ParamInteger colonyDistanceWeight	= new ParamInteger(GOV_UI, "COLONY_DISTANCE_WEIGHT", 50)
			.isCfgFile(true)
			.setLimits(0, 100)
			.setIncrements(1, 5, 20);

	ParamBoolean compensateGrowth		= new ParamBoolean(GOV_UI, "COMPENSATE_GROWTH", true);
	ParamFloat minColonyGrowth			= new ParamFloat(GOV_UI, "COLONY_MIN_GROWTH", 2.0f)
			.setLimits(0f, 10f)
			.setIncrements(0.1f, 0.5f, 2f)
			.guiFormat("0.0");
	ParamInteger colonyEarlyBoostPct	= new ParamInteger(GOV_UI, "COLONY_EARLY_BOOST", 50)
			.setLimits(0, 100)
			.setIncrements(1, 5, 20);
	ParamBoolean earlyBaseBuilding		= new ParamBoolean(GOV_UI, "EARLY_BASE_BUILDING", false);
	ParamInteger earlyBaseBoostPct		= new ParamInteger(GOV_UI, "EARLY_BASE_BOOST_PCT", 50)
			.setLimits(0, 100)
			.setIncrements(1, 5, 20);
}
