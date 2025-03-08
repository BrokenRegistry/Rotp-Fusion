package rotp.model.game;

import static rotp.model.game.DefaultValues.MOO1_DEFAULT;
import static rotp.model.game.DefaultValues.ROTP_DEFAULT;
import static rotp.model.ships.ShipDesignLab.MAX_DESIGNS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import rotp.model.empires.Empire;
import rotp.model.empires.EmpireView;
import rotp.model.galaxy.Galaxy;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.GovernorOptions.GatesGovernor;
import rotp.model.planet.PlanetType;
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

	ParamBoolean armedScoutGuard		= new ParamBoolean(GOV_UI, "ARMED_SCOUT_GUARD", false);
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

	class ParamFleetAutoScout extends ParamFleetAuto	{
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
		@Override public SubFleetList newSubFleetList(Empire empire) {
			return new SubFleetList(empire);
		}
		@Override protected BiPredicate<ShipFleet, ShipDesign> notOnDefenseMission()	{
			return notOnDefenseMission (armedScoutGuard.get());
		}
	}
	class ParamFleetAutoColonize extends ParamFleetAuto	{
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
		@Override public SubFleetList newSubFleetList(Empire empire) {
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
				
	}
	class ParamFleetAutoAttack extends ParamFleetAuto	{
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
	abstract class ParamFleetAuto extends ParamList	{
		protected static final String FLEET_AUTO_ALONE	= "FLEET_AUTO_SEND_ALONE";	// Single design (Old Way)
		protected static final String FLEET_AUTO_TEAMS	= "FLEET_AUTO_SEND_TEAMS";	// Teamed design
		protected static final String FLEET_AUTO_ALL	= "FLEET_AUTO_SEND_ALL";	// All designs
		protected static final String FLEET_AUTO_ANY	= "FLEET_AUTO_SEND_ANY";	// Old Way
		
		public ParamFleetAuto(String name, String defaultCfgLabel)	{
			super(GOV_UI, name, defaultCfgLabel);
			showFullGuide(true);
			put(FLEET_AUTO_ANY,		FLEET_AUTO_ANY);
			put(FLEET_AUTO_ALONE,	FLEET_AUTO_ALONE);
			put(FLEET_AUTO_TEAMS,	FLEET_AUTO_TEAMS);
			put(FLEET_AUTO_ALL,		FLEET_AUTO_ALL);
		}
		public abstract int sendCount(ShipDesign sd);
		public abstract int[] getautoShipRequest(ShipDesignLab lab);
		public abstract SubFleetList newSubFleetList(Empire empire);
		protected BiPredicate<ShipDesign, Integer> designFitForSystem() {
			BiPredicate<ShipDesign, Integer> designFitForSystem = (sd, si) -> true;
			return designFitForSystem;
		}
		protected BiPredicate<ShipFleet, ShipDesign> notOnDefenseMission()	{ return notOnDefenseMission (false); }
		protected BiPredicate<ShipFleet, ShipDesign> notOnDefenseMission(boolean defendUncolonized)	{
			// check if we have hostile incoming fleets. If so, don't send out ships
			// there's no method to do that, so let's build one.
			Galaxy galaxy = GameSession.instance().galaxy();
			Empire empire = galaxy.player();
			Set<Integer> systemHasHostileIncoming = new HashSet<>();
			Set<Integer> hostiles = empire.hostiles().stream()
					.map(h -> h.empId())
					.collect(Collectors.toSet());
			for (ShipFleet sf : galaxy.ships.inTransitFleets()) {
				if (sf != null
						&& empire == sf.destination().empire()
						&& hostiles.contains(sf.empire().id)) {
					//	System.out.println("Must defend system "+sf.destination().name());
					systemHasHostileIncoming.add(sf.destSysId());
				}
			}
			// also, don't send out fleet if orbiting enemy planet
			BiPredicate<ShipFleet, ShipDesign> notOnDefense = (sf,sd) -> {
				// unarmed ships don't contribute to defense
				if (!sd.isArmed())
					return true;
				if (systemHasHostileIncoming.contains(sf.system().id))
					return false;
				if (defendUncolonized && sf.system().empire() == null && sf.system().hasPlanet())
					return false;
				if (sf.system().empire() != null && empire != sf.system().empire())
					// Don't send out armed ships orbiting enemy systems.
					// It's OK to send away armed ships orbiting uncolonized planets if there are no incoming
					// enemy fleets and that's checked above.
					return false;
				else
					return true;
			};
			return notOnDefense;
		}
		public AutoSendFleet getAutoSendFleet(ShipFleet fleet)	{
			return new AutoSendFleet(fleet);
		}
		public boolean alreadyHasDesignsOrbiting(ShipFleet fleet, int sysId)	{
			return new AutoSendFleet(fleet, sysId).hasSubFleet();
		}
		public boolean alreadyHasDesignsOnRoute(ShipFleet fleet, int sysId)	{
			return new AutoSendFleet(fleet, sysId).hasSubFleet();
		}
		public class SubFleetList extends ArrayList<SubFleet> {
			private static final long serialVersionUID = 1L;
			private final Empire empire;
			private SubFleetList(Empire empire)	{
				super();
				this.empire = empire;
			}
			public boolean add(ShipFleet fleet)	{
				AutoSendFleet autoSendFleet = new AutoSendFleet(fleet);
				boolean hasSubFleet = false;
				while (autoSendFleet.hasSubFleet()) {
					hasSubFleet |= add(autoSendFleet.getSubFleet());
				}
				return hasSubFleet;
			}
			public void sortForAttack()	{
				// non-extended range to extended range
				// Cheapest to most expensive
				// sort ships fastest to slowest, send out fastest Attack ships first
				sort((d1, d2) -> {
					int rangeDiff = Boolean.compare(d1.isExtendedRange(), d2.isExtendedRange());
					// desc order
					int warpDiff = d2.warpSpeed() - d1.warpSpeed();
					// ascendent order, cheapest first
					int costDiff = d1.cost() - d2.cost();
					if (rangeDiff != 0)
						return rangeDiff;
					else if (warpDiff != 0)
						return warpDiff;
					else
						return costDiff;
				});
			}
			public void sortForColonize()	{
				// non-extended range to extended range
				// least hostile to most hostile colony bases, send out least hostile colony bases first
				// sort ships fastest to slowest, send out fastest colony ships first
				sort((d1, d2) -> {
					int rangeDiff = Boolean.compare(d1.isExtendedRange(), d2.isExtendedRange());
					// desc order
					int warpDiff = d2.warpSpeed() - d1.warpSpeed();
					int envDiff = d1.environment() - d2.environment();
					// ascendent order, cheapest first
					int costDiff = d1.cost() - d2.cost();
					if (rangeDiff != 0)
						return rangeDiff;
					else if (envDiff != 0)
						return envDiff;
					else if (warpDiff != 0)
						return warpDiff;
					else
						return costDiff;
				});
			}
			public void sortByWarpSpeed()	{
				sort((sf1, sf2) ->  {
					int rangeDiff = Boolean.compare(sf1.isExtendedRange(), sf2.isExtendedRange());
					int warpDiff = sf2.warpSpeed() - sf1.warpSpeed();
					int costDiff = sf2.cost() - sf1.cost(); // send colony first
					// Quick first
					if (warpDiff != 0)
						return warpDiff;
					// Short range first
					else if (rangeDiff != 0) 
						return rangeDiff;
					else // Expensive first (colony or armed scout)
						return costDiff;
				});
			}
			public void sortByTimeToSys(int sysId)	{
				StarSystem sys = empire.sv.system(sysId);
				sort((sf1, sf2) -> (int)Math.signum(
						sf1.fleet.travelTimeAdjusted(sys, sf1.warpSpeed) -
						sf2.fleet.travelTimeAdjusted(sys, sf2.warpSpeed)) );
			}
			public int minWarpSpeed()	{
				int minWarpSpeed = -1;
				for (SubFleet subFleet : this)
					if (minWarpSpeed < 0 || subFleet.warpSpeed() < minWarpSpeed)
						minWarpSpeed = subFleet.warpSpeed();
				return minWarpSpeed;
			}
			public boolean hasExtendedRange()	{
				for (SubFleet subFleet : this)
					if (subFleet.isExtendedRange())
						return true;
				return false;
			}
			public boolean canColonize(PlanetType pt)	{
				if (pt == null)
					return false;
				for (SubFleet subFleet : this)
					if (subFleet.canColonize(pt))
						return true;
				return false;
			}
		}
		public boolean fitForSystem(SubFleet subFleet, int sysId) {
			for (int i=0; i<MAX_DESIGNS; i++) {
				if (subFleet.shipCounts[i]>0) {
					ShipDesign design = subFleet.fleet.design(i);
					if (designFitForSystem().test(design, sysId))
						return true;
				}
			}
			return false;
		}
		public record SubFleet (
				ShipFleet fleet,
				int[] shipCounts,
				int warpSpeed,
				int cost,
				int environment,
				boolean isExtendedRange)	{
			public int numShips()			{ return IntStream.of(shipCounts()).sum(); }
			public boolean entireFleet()	{ return fleet().numShips() == numShips(); }
			public boolean canColonize(PlanetType pt)	{
				if (pt == null)
					return false;
				return pt.hostility() <= environment;
			}
		}
		public class AutoSendFleet {
			private ShipFleet fleet;
			private int[] shipCounts;
			private int[] requests;
			private int[] nextCounts;
			private int designCount, fleetCount, requestNum, warpSpeed, fleetCost, environment;
			private boolean isExtRange;

			private AutoSendFleet(ShipFleet fleet)	{
				this.fleet	= fleet;
				init(notOnDefenseMission(), null, 0);
			}
			private AutoSendFleet(ShipFleet fleet, int sysId)	{
				this.fleet	= fleet;
				init(null, designFitForSystem(), sysId);
			}
			private void init(BiPredicate<ShipFleet, ShipDesign> filter,
					BiPredicate<ShipDesign, Integer> fit, int sysId)	{
				shipCounts	= fleet.numCopy();
				requests	= getautoShipRequest(fleet.empire().shipLab());
				nextCounts	= null;
				designCount	= 0;
				fleetCount	= 0;
				requestNum	= 0;
				environment = -1;
				for (int i=0; i<MAX_DESIGNS; i++) {
					if (requests[i] > 0) {
						requestNum++;
						if (shipCounts[i] / requests[i] > 0) {
							ShipDesign design = fleet.design(i);
							if (fit != null) {
								if (fit.test(design, sysId))
									designCount++;
								continue;
							}
							else if (design.isAutoAttack() // don't send attack ships orbiting enemy planets
									&& fleet.empire() != fleet.system().empire()
									&& fleet.system().hasPlanet())
								shipCounts[i] = 0;
							else if (filter != null && !filter.test(fleet, design))
								shipCounts[i] = 0;
							else
								designCount++;
						}
					}
				}
				if (designCount == 0)
					return;
				// evaluate sub fleet potential (will consume shipCounts)
				int[] shipCopy = shipCounts.clone();
				while (buildNextFleet()) {
					fleetCount++;
					getSubFleet();
				}
				// Restore shipCounts
				shipCounts	= shipCopy;
			}

			public SubFleet getSubFleet()		{
				buildNextFleet();
				SubFleet subFleet = new SubFleet(fleet, nextCounts, warpSpeed, fleetCost, environment, isExtRange);
				nextCounts = null;
				return subFleet;
			}
			public boolean fitForSystem(int id)	{
				return new AutoSendFleet(fleet, id).hasSubFleet();
			}
			public boolean alreadyFit(ShipFleet fleet, int sysId)	{
				return new AutoSendFleet(fleet, sysId).hasSubFleet();
			}
			public int subFleetCount()			{ return fleetCount; }
			public ShipFleet fleet()			{ return fleet; }
			public boolean hasSubFleet()		{
				if (nextCounts != null)
					return true;
				buildNextFleet();
				return nextCounts != null;
			}
			public int warpSpeed()				{
				if (buildNextFleet())
					return warpSpeed;
				System.err.println("Error: Empty autoSendFleet asked for speed");
				return 1;
			}
			public boolean isExtendedRange()	{
				if (buildNextFleet())
					return isExtRange;
				System.err.println("Error: Empty autoSendFleet asked for extendedRange");
				return false;
			}
			public int[] getNextCounts()		{
				if (nextCounts == null)
					buildNextFleet();
				if (nextCounts != null) {
					int[]  counts = nextCounts;
					nextCounts = null;
					fleetCount--;
					return counts;
				}
				else 
					return new int[MAX_DESIGNS];
			}
			private boolean buildNextFleet()	{
				if (nextCounts != null)
					return true;
				nextCounts	= new int[MAX_DESIGNS];
				designCount	= 0;
				fleetCost	= 0;
				warpSpeed	= Integer.MAX_VALUE;
				isExtRange	=  true;
				switch (get()) {
					case FLEET_AUTO_ALONE:
						for (int i = 0; i < MAX_DESIGNS; i++) {
							if (requests[i] == 0)
								continue;
							shipCounts[i] -= requests[i];
							if (shipCounts[i] >= 0) {
								nextCounts[i] = requests[i];
								designCount++;
								ShipDesign design = fleet.design(i);
								warpSpeed  = design.warpSpeed();
								isExtRange = design.isExtendedRange();
								fleetCost  = design.cost();
								if (design.hasColonySpecial())
									environment = design.colonySpecial().tech().environment();
								return true;
							}
						}
						nextCounts = null;
						return false;
	
					case FLEET_AUTO_TEAMS:
						for (int i = 0; i < MAX_DESIGNS; i++) {
							if (requests[i] == 0)
								continue;
							shipCounts[i] -= requests[i];
							if (shipCounts[i] >= 0) {
								nextCounts[i] = requests[i];
								designCount++;
								ShipDesign design = fleet.design(i);
								warpSpeed = Math.min(warpSpeed, design.warpSpeed());
								isExtRange &= design.isExtendedRange();
								fleetCost  += design.cost();
								if (design.hasColonySpecial())
									environment = Math.max(environment, design.colonySpecial().tech().environment());
							}
						}
						if (designCount <= 1) {
							nextCounts = null;
							return false;
						}
						return true;
	
					case FLEET_AUTO_ALL:
						for (int i = 0; i < MAX_DESIGNS; i++) {
							if (requests[i] == 0)
								continue;
							shipCounts[i] -= requests[i];
							if (shipCounts[i] >= 0) {
								nextCounts[i] = requests[i];
								designCount++;
								ShipDesign design = fleet.design(i);
								warpSpeed = Math.min(warpSpeed, design.warpSpeed());
								isExtRange &= design.isExtendedRange();
								fleetCost  += design.cost();
								if (design.hasColonySpecial())
									environment = Math.max(environment, design.colonySpecial().tech().environment());
							}
						}
						if (designCount != requestNum) {
							nextCounts = null;
							return false;
						}
						return true;
	
					case FLEET_AUTO_ANY:
						for (int i = 0; i < MAX_DESIGNS; i++) {
							if (requests[i] == 0)
								continue;
							shipCounts[i] -= requests[i];
							if (shipCounts[i] >= 0) {
								nextCounts[i] = requests[i];
								designCount++;
								ShipDesign design = fleet.design(i);
								warpSpeed = Math.min(warpSpeed, design.warpSpeed());
								isExtRange &= design.isExtendedRange();
								fleetCost  += design.cost();
								if (design.hasColonySpecial())
									environment = Math.max(environment, design.colonySpecial().tech().environment());
							}
						}
						if (designCount == 0) {
							nextCounts = null;
							return false;
						}
						return true;
				}
				nextCounts = null;
				return false;
			}
		}
	}
}
