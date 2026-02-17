package rotp.model.ai.governor;

import static rotp.model.ships.ShipDesignLab.MAX_DESIGNS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import rotp.model.empires.Empire;
import rotp.model.galaxy.Galaxy;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.GameSession;
import rotp.model.game.IGovOptions;
import rotp.model.planet.PlanetType;
import rotp.model.ships.ShipDesign;
import rotp.model.ships.ShipDesignLab;
import rotp.ui.util.ParamList;

public abstract class ParamFleetAuto extends ParamList	{
	protected static final String FLEET_AUTO_ALONE	= "FLEET_AUTO_SEND_ALONE";	// Single design (Old Way)
	protected static final String FLEET_AUTO_TEAMS	= "FLEET_AUTO_SEND_TEAMS";	// Teamed design
	protected static final String FLEET_AUTO_ALL	= "FLEET_AUTO_SEND_ALL";	// All designs
	protected static final String FLEET_AUTO_ANY	= "FLEET_AUTO_SEND_ANY";	// Old Way
	
	public ParamFleetAuto(String name, String defaultCfgLabel)	{
		super(IGovOptions.GOV_UI, name, defaultCfgLabel);
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
			StarSystem sys = sf.system();
			// auto-scout on mission 
			if (sd.isAutoScout() && IGovOptions.auto_Scout.get())
				return !(defendUncolonized && sys.empire() == null && sys.hasPlanet());
			if (systemHasHostileIncoming.contains(sys.id))
				return false;
			if (sys.empire() != null && empire != sys.empire())
				// Don't send out armed ships orbiting enemy systems.
				// It's OK to send away armed ships orbiting uncolonized planets if there are no incoming
				// enemy fleets and that's checked above.
				return false;
			else
				return true;
		};
		return notOnDefense;
	}
	AutoSendFleet getAutoSendFleet(ShipFleet fleet)	{
		return new AutoSendFleet(fleet);
	}
	boolean alreadyHasDesignsOrbiting(ShipFleet fleet, int sysId)	{
		return new AutoSendFleet(fleet, sysId).hasSubFleet();
	}
	boolean alreadyHasDesignsOnRoute(ShipFleet fleet, int sysId)	{
		return new AutoSendFleet(fleet, sysId).hasSubFleet();
	}
	public final class SubFleetList extends ArrayList<SubFleet> {
		private static final long serialVersionUID = 1L;
		private final Empire empire;
		public SubFleetList(Empire empire)	{
			super();
			this.empire = empire;
		}
		public boolean splitAndAdd(ShipFleet fleet)	{
			AutoSendFleet autoSendFleet = new AutoSendFleet(fleet);
			boolean hasSubFleet = false;
			while (autoSendFleet.hasSubFleet()) {
				hasSubFleet |= add(autoSendFleet.getSubFleet());
			}
			return hasSubFleet;
		}
		void sortForAttack()	{
			// non-extended range to extended range
			// Cheapest to most expensive
			// sort ships fastest to slowest, send out fastest Attack ships first
			sort((d1, d2) -> {
				int rangeDiff = Boolean.compare(d1.isExtendedRange(), d2.isExtendedRange());
				if (rangeDiff != 0)
					return rangeDiff;
				// desc order
				float warpDiff = d2.warpSpeed() - d1.warpSpeed();
				// ascendent order, cheapest first
				int costDiff = d1.cost() - d2.cost();
				if (warpDiff != 0)
					return  (int) Math.signum(warpDiff);
				else
					return costDiff;
			});
		}
		void sortForColonize()	{
			// non-extended range to extended range
			// least hostile to most hostile colony bases, send out least hostile colony bases first
			// sort ships fastest to slowest, send out fastest colony ships first
			sort((fleet1, fleet2) -> {
				int rangeDiff = Boolean.compare(fleet1.isExtendedRange(), fleet2.isExtendedRange());
				// desc order

				// Sort by range if not equals
				if (rangeDiff != 0)
					return rangeDiff;

				// Sort by environment if not equals
				int envDiff = fleet1.environment() - fleet2.environment();
				if (envDiff != 0)
					return envDiff;

				// Sort by speed if not equals
				float warpDiff = fleet2.warpSpeed() - fleet1.warpSpeed();
				// ascendent order, cheapest first
				int costDiff = fleet1.cost() - fleet2.cost();
				if (warpDiff != 0)
					return (int) Math.signum(warpDiff);
				else
					// then sort by cost.
					return costDiff;
			});
		}
		void sortByWarpSpeed()	{
			sort((sf1, sf2) ->  {
				float warpDiff = sf2.warpSpeed() - sf1.warpSpeed();
				// Quick first
				if (warpDiff != 0)
					return (int) Math.signum(warpDiff);
				// Short range first
				int rangeDiff = Boolean.compare(sf1.isExtendedRange(), sf2.isExtendedRange());
				int costDiff = sf2.cost() - sf1.cost(); // send colony first
				if (rangeDiff != 0) 
					return rangeDiff;
				else // Expensive first (colony or armed scout)
					return costDiff;
			});
		}
		void sortByTimeToSys(int sysId)	{
			StarSystem sys = empire.sv.system(sysId);
			sort((sf1, sf2) -> (int)Math.signum(
					sf1.fleet.travelTimeAdjusted(sys, sf1.warpSpeed) -
					sf2.fleet.travelTimeAdjusted(sys, sf2.warpSpeed)) );
		}
		float minWarpSpeed()	{
			float minWarpSpeed = -1;
			for (SubFleet subFleet : this)
				if (minWarpSpeed < 0 || subFleet.warpSpeed() < minWarpSpeed)
					minWarpSpeed = subFleet.warpSpeed();
			return minWarpSpeed;
		}
		boolean hasExtendedRange()	{
			for (SubFleet subFleet : this)
				if (subFleet.isExtendedRange())
					return true;
			return false;
		}
		boolean canColonize(PlanetType pt)	{
			if (pt == null)
				return false;
			for (SubFleet subFleet : this)
				if (subFleet.canColonize(pt))
					return true;
			return false;
		}
	}
	boolean fitForSystem(SubFleet subFleet, int sysId) {
		for (int i=0; i<MAX_DESIGNS; i++) {
			if (subFleet.shipCounts[i]>0) {
				ShipDesign design = subFleet.fleet.design(i);
				if (designFitForSystem().test(design, sysId))
					return true;
			}
		}
		return false;
	}
	final record SubFleet (
			ShipFleet fleet,
			int[] shipCounts,
			float warpSpeed,
			int cost,
			int environment,
			boolean isExtendedRange)	{
		private int numShips()			{ return IntStream.of(shipCounts()).sum(); }
		boolean entireFleet()			{ return fleet().numShips() == numShips(); }
		private boolean canColonize(PlanetType pt)	{
			if (pt == null)
				return false;
			return pt.hostility() <= environment;
		}
	}
	private final class AutoSendFleet {
		private ShipFleet fleet;
		private int[] shipCounts;
		private int[] requests;
		private int[] nextCounts;
		private int designCount, requestNum, fleetCost, environment;
		private float warpSpeed;
		// private int fleetCount;
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
			//fleetCount	= 0;
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
				//fleetCount++;
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
		public boolean hasSubFleet()		{
			if (nextCounts != null)
				return true;
			buildNextFleet();
			return nextCounts != null;
		}
/*		public boolean fitForSystem(int id)	{
			return new AutoSendFleet(fleet, id).hasSubFleet();
		}
		public boolean alreadyFit(ShipFleet fleet, int sysId)	{
			return new AutoSendFleet(fleet, sysId).hasSubFleet();
		}
		public int subFleetCount()			{ return fleetCount; }
		public ShipFleet fleet()			{ return fleet; }
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
		} */
		private boolean buildNextFleet()	{
			if (nextCounts != null)
				return true;
			nextCounts	= new int[MAX_DESIGNS];
			designCount	= 0;
			fleetCost	= 0;
			warpSpeed	= Float.MAX_VALUE;
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
