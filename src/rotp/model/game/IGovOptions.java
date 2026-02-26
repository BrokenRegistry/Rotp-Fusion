package rotp.model.game;

import static rotp.model.game.DefaultValues.MOO1_DEFAULT;
import static rotp.model.game.DefaultValues.ROTP_DEFAULT;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import rotp.model.ai.governor.ParamFleetAuto;
import rotp.model.empires.Empire;
import rotp.model.empires.EmpireView;
import rotp.model.galaxy.Galaxy;
import rotp.model.galaxy.ShipFleet;
import rotp.model.game.GovernorOptions.GatesGovernor;
import rotp.model.ships.ShipDesign;
import rotp.model.ships.ShipDesignLab;
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
			if (GameSession.instance().isReady())
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

	static void tagAutoScout(String id)	{
		autoScoutMultiple.updated(true);
		autoScoutMaxTime.updated(true);
		autoScoutSaveTime.updated(true);
		secondScoutWeightPct.updated(true);
	}
	ParamBoolean armedScoutGuard		= new ParamBoolean(GOV_UI, "ARMED_SCOUT_GUARD", false);
	ParamBoolean autoScoutSmart			= new ParamBoolean(GOV_UI, "AUTO_SCOUT_SMART", false)
			.setUpdateParameters(IGovOptions::tagAutoScout, "");
	ParamBoolean autoScoutMultiple		= new ParamAutoScoutSmartBool(GOV_UI, "AUTO_SCOUT_NEAR_FIRST", true)
			.setUpdateParameters(IGovOptions::tagAutoScout, "");
	ParamInteger autoScoutMaxTime		= new ParamAutoScoutSmartInt(GOV_UI, "AUTO_SCOUT_MAX_TIME", 8)
			.setLimits(0, 100)
			.setIncrements(1, 5, 20);
	ParamInteger autoScoutSaveTime		= new ParamAutoScoutMultiInt(GOV_UI, "AUTO_SCOUT_SAVE_TIME", 1)
			.setLimits(1, 100)
			.setIncrements(1, 5, 20);
	ParamInteger secondScoutWeightPct 	= new ParamAutoScoutMultiInt(GOV_UI, "2ND_SCOUT_WEIGHT_PCT", 0)
			.setLimits(0, 100)
			.setIncrements(1, 5, 20);
	final class ParamAutoScoutSmartBool extends ParamBoolean	{
		public ParamAutoScoutSmartBool(String gui, String name, Boolean defaultValue) {
			super(gui, name, defaultValue);
		}
		@Override public boolean isGhost()	{ return !autoScoutSmart.get(); }
	}
	final class ParamAutoScoutSmartInt extends ParamInteger	{
		public ParamAutoScoutSmartInt(String gui, String name, Integer defaultValue) {
			super(gui, name, defaultValue);
		}
		@Override public boolean isGhost()	{ return !autoScoutSmart.get(); }
	}
	final class ParamAutoScoutMultiInt extends ParamInteger	{
		public ParamAutoScoutMultiInt(String gui, String name, Integer defaultValue) {
			super(gui, name, defaultValue);
		}
		@Override public boolean isGhost()	{ return !autoScoutSmart.get() || !autoScoutMultiple.get(); }
	}

	static void tagAutoColonize(String id)	{
		autoColonizeMultiple.updated(true);
		autoColonizeMaxTime.updated(true);
		autoColonizeSaveTime.updated(true);
		secondColonyWeightPct.updated(true);
	}
	ParamBoolean armedColonizerGuard	= new ParamBoolean(GOV_UI, "ARMED_COLONIZER_GUARD", false);
	ParamBoolean armedColonizerFight	= new ParamBoolean(GOV_UI, "ARMED_COLONIZER_FIGHT", false);

	ParamBoolean autoColonizeTuned		= new ParamBoolean(GOV_UI, "AUTO_COLONY_TUNED", false)
			.setUpdateParameters(IGovOptions::tagAutoColonize, "");
	ParamBoolean autoColonizeMultiple	= new ParamAutoColoTunedBool(GOV_UI, "AUTO_COLONY_MULTIPLE", false)
			.setUpdateParameters(IGovOptions::tagAutoColonize, "");
	ParamInteger autoColonizeMaxTime	= new ParamAutoColoTunedInt(GOV_UI, "AUTO_COLONY_MAX_TIME", 10)
			.setLimits(0, 100)
			.setIncrements(1, 5, 20)
			.specialZero(GOV_UI + "COLO_UNLIMITED_TIME");
	ParamInteger autoColonizeSaveTime	= new ParamAutoColoMultiInt(GOV_UI, "AUTO_COLONY_SAVE_TIME", 1)
			.setLimits(1, 100)
			.setIncrements(1, 5, 20);
	ParamInteger secondColonyWeightPct 	= new ParamAutoColoMultiInt(GOV_UI, "2ND_COLONY_WEIGHT_PCT", 0)
			.setLimits(0, 100)
			.setIncrements(1, 5, 20);
	final class ParamAutoColoTunedBool extends ParamBoolean	{
		public ParamAutoColoTunedBool(String gui, String name, Boolean defaultValue) {
			super(gui, name, defaultValue);
		}
		@Override public boolean isGhost()	{ return !autoColonizeTuned.get(); }
	}
	final class ParamAutoColoTunedInt extends ParamInteger	{
		public ParamAutoColoTunedInt(String gui, String name, Integer defaultValue) {
			super(gui, name, defaultValue);
		}
		@Override public boolean isGhost()	{ return !autoColonizeTuned.get(); }
	}
	final class ParamAutoColoMultiInt extends ParamInteger	{
		public ParamAutoColoMultiInt(String gui, String name, Integer defaultValue) {
			super(gui, name, defaultValue);
		}
		@Override public boolean isGhost()	{ return !autoColonizeTuned.get() || !autoColonizeMultiple.get(); }
	}

	ParamBoolean trainSpiesASAP			= new ParamBoolean(GOV_UI, "TRAIN_SPIES_ASAP", true);
	ParamBoolean contactUpdateSpending	= new ParamBoolean(GOV_UI, "CONTACT_UPDATE_SPENDING", false);

	AutoAttackEmpire autoAttackEmpire	= new AutoAttackEmpire();
	class AutoAttackEmpire extends ParamList	{
		static final String AUTO_ATTACK_EMPIRE		= "AUTO_ATTACK_EMPIRE";
		static final String AUTO_ATTACK_NONE		= "AUTO_ATTACK_NONE";
		static final String AUTO_ATTACK_WAR			= "AUTO_ATTACK_WAR";
		static final String AUTO_ATTACK_MENACING	= "AUTO_ATTACK_MENACING";
		static final String AUTO_ATTACK_HOSTILE		= "AUTO_ATTACK_HOSTILE";
		static final String AUTO_ATTACK_NO_ENTENTE	= "AUTO_ATTACK_NO_ENTENTE";
		static final String AUTO_ATTACK_ALL			= "AUTO_ATTACK_ALL";

		public AutoAttackEmpire()	{
			super(GOV_UI, AUTO_ATTACK_EMPIRE, AUTO_ATTACK_MENACING);
			showFullGuide(true);
			put(AUTO_ATTACK_NONE,		AUTO_ATTACK_NONE);
			put(AUTO_ATTACK_WAR,		AUTO_ATTACK_WAR);
			put(AUTO_ATTACK_MENACING,	AUTO_ATTACK_MENACING);
			put(AUTO_ATTACK_HOSTILE,	AUTO_ATTACK_HOSTILE);
			put(AUTO_ATTACK_NO_ENTENTE,	AUTO_ATTACK_NO_ENTENTE);
			put(AUTO_ATTACK_ALL,		AUTO_ATTACK_ALL);
		}
		public List<Integer> targetEmpires(Empire empire)	{
			List<Integer> targetEmpires = new ArrayList<>();
			if (empire == null)
				return targetEmpires;
			switch (get()) {
				case AUTO_ATTACK_NONE:
					return targetEmpires;
				case AUTO_ATTACK_WAR:
					for (EmpireView v : empire.empireViews()) {
					 	if ((v!= null) && !v.extinct() && v.embassy().anyWar())
					 		targetEmpires.add(v.empId());
					}
					return targetEmpires;
				case AUTO_ATTACK_MENACING:
					for (EmpireView v : empire.empireViews()) {
					 	if ((v!= null) && !v.extinct() && v.embassy().menacing())
					 		targetEmpires.add(v.empId());
					}
					return targetEmpires;
				case AUTO_ATTACK_HOSTILE:
					for (EmpireView v : empire.empireViews()) {
					 	if ((v!= null) && !v.extinct() && v.embassy().hostile())
					 		targetEmpires.add(v.empId());
					}
					return targetEmpires;
				case AUTO_ATTACK_NO_ENTENTE:
					for (EmpireView v : empire.empireViews()) {
					 	if ((v!= null) && !v.extinct() && v.embassy().noEntente())
					 		targetEmpires.add(v.empId());
					}
					return targetEmpires;
				case AUTO_ATTACK_ALL:
					for (EmpireView v : empire.empireViews()) {
					 	if ((v!= null) && !v.extinct() && v.empId() != empire.id)
					 		targetEmpires.add(v.empId());
					}
					return targetEmpires;
			}
			return targetEmpires;
		}
		
	}

	ParamFleetAutoScout	   fleetAutoScoutMode	 = new ParamFleetAutoScout();
	ParamFleetAutoColonize fleetAutoColonizeMode = new ParamFleetAutoColonize();
	ParamFleetAutoAttack   fleetAutoAttackMode	 = new ParamFleetAutoAttack();

	final class ParamFleetAutoScout extends ParamFleetAuto	{
		static final String FLEET_AUTO_SCOUT = "FLEET_AUTO_SCOUT";
		public ParamFleetAutoScout()	{
			super(FLEET_AUTO_SCOUT, FLEET_AUTO_ALONE);
		}
		@Override public int sendCount(ShipDesign design)	{
			return design.autoScoutShipCount();
		}
		@Override public int[] getautoShipRequest(ShipDesignLab lab)	{
			return lab.autoScoutShipCount();
		}
		@Override public SubFleetList newSubFleetList(Empire empire)	{
			return new SubFleetList(empire);
		}
		@Override protected BiPredicate<ShipFleet, ShipDesign> notOnDefenseMission()	{
			return notOnDefenseMission(armedScoutGuard.get());
		}
	}
	final class ParamFleetAutoColonize extends ParamFleetAuto	{
		static final String FLEET_AUTO_COLONIZE = "FLEET_AUTO_COLONIZE";
		public ParamFleetAutoColonize()	{
			super(FLEET_AUTO_COLONIZE, FLEET_AUTO_ALONE);
		}
		@Override public int sendCount(ShipDesign design)	{
			return design.autoColonizeShipCount();
		}
		@Override public int[] getautoShipRequest(ShipDesignLab lab)	{
			return lab.autoColonizeShipCount();
		}
		@Override public SubFleetList newSubFleetList(Empire empire)	{
			return new SubFleetList(empire);
		}
		@Override protected BiPredicate<ShipDesign, Integer> designFitForSystem()	{
			Empire empire = GameSession.instance().galaxy().player();
			BiPredicate<ShipDesign, Integer> designFitForSystem =
					(sd, si) -> (empire.ignoresPlanetEnvironment()
						&& empire.acceptedPlanetEnvironment(empire.sv.system(si).planet().type()))
						|| (empire.canColonize(si)
						&& sd.colonySpecial().canColonize(empire.sv.system(si).planet().type()));
			return designFitForSystem;
		}
		@Override protected BiPredicate<ShipFleet, ShipDesign> notOnDefenseMission()	{
			if(armedColonizerGuard.get())
				return notOnDefenseMission(false);
			return null;
		}
	}
	final class ParamFleetAutoAttack extends ParamFleetAuto	{
		static final String FLEET_AUTO_ATTACK = "FLEET_AUTO_ATTACK";
		public ParamFleetAutoAttack()	{
			super(FLEET_AUTO_ATTACK, FLEET_AUTO_ALONE);
		}
		@Override public int sendCount(ShipDesign design)	{
			return design.autoAttackShipCount();
		}
		@Override public int[] getautoShipRequest(ShipDesignLab lb)	{
			return lb.autoAttackShipCount();
		}
		@Override public SubFleetList newSubFleetList(Empire empire) {
			return new SubFleetList(empire);
		}
	}
}
